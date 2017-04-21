/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "SerialPortJNI"

//#include "utils/Log.h"

#include "jni.h"
//#include "JNIHelp.h"
//#include "android_runtime/AndroidRuntime.h"
#include "SelectSerial.h"

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <Android/log.h>

using namespace android;

static SelectSerialPort* g_port;

static jobject mListenerObj = NULL;
static jmethodID method_reportNewData = NULL;

static long int read_thread;

static JavaVM* g_JavaVM;

#define SERIAL_READ_BUFFER_SIZE 32
#define TAG "JNI-SerialPort"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

static void* read_thread_func(void* args){

	JNIEnv* env;
	JavaVM* vm = g_JavaVM;

	vm->AttachCurrentThread(&env, NULL);

	LOGD(" read thread func start port is %d", g_port);

	while (g_port != NULL){

		unsigned char data[SERIAL_READ_BUFFER_SIZE] = {0};
		int read_len = 0;

		if (g_port != NULL)
			read_len = g_port->Read(data, SERIAL_READ_BUFFER_SIZE);

		LOGD(" read data size %d", read_len);
		LOGD(" read data : 0x%x, 0x%x, 0x%x, 0x%x, 0x%x, 0x%x, 0x%x, 0x%x", data[0],  data[1],  data[2],  data[3],  data[4],  data[5],  data[6],  data[7]);

		if (method_reportNewData != NULL && read_len > 0){

			jbyteArray array = env->NewByteArray(read_len);

			if (array != NULL){

				env->SetByteArrayRegion(array, 0, read_len, (jbyte*)data);

				env->CallVoidMethod(mListenerObj, method_reportNewData, array);

				env->DeleteLocalRef(array);
			} else
				LOGD(" report failed  NewByteArray return NULL");
		} else
			LOGD(" report failed  method_reportNewData is %d", method_reportNewData);

	}

	LOGD(" read thread func exit port is %d", g_port);
	vm->DetachCurrentThread();
}

class SelectPort : public SelectSerialPort{
	public:
		SelectPort(const char* name): SelectSerialPort(name){
			Open();
			initPort();
		}
		virtual ~SelectPort(){

		}

		virtual int initPort(){
			return setParity(0, 8, 1, 'n');
		}
};

static int open_port(JNIEnv *env, jobject thiz, jstring path, jint speed){

	const char *str = env->GetStringUTFChars(path, NULL);

	g_port = new SelectPort(str);

	 env->ReleaseStringUTFChars(path, str);

	if (g_port != NULL){

		g_port->setSpeed(speed);

		if (pthread_create(&read_thread, NULL, read_thread_func, NULL) != 0){
			LOGD("create thread failed\n");
			return -1;
		}

		return 0;
	}

	LOGD("open port failed\n");
	return -1;
}

static jint close_port(JNIEnv *env, jobject thiz){

	if (g_port != NULL){
		g_port->Close();

		g_port = NULL;

		return 0;
	}

	return -1;
}


static jint write_port(JNIEnv *env, jobject thiz, jbyteArray array, jint length){

	jbyte* buf = (jbyte *)malloc(length);
	if (!buf) {
		LOGD(" write_port failed , no space memory");
		return -1;
	}

	env->GetByteArrayRegion(array, 0, length, buf);

	LOGD(" write data : 0x%x, 0x%x, 0x%x, 0x%x, 0x%x, 0x%x, ", buf[0],  buf[1],  buf[2],  buf[3],  buf[4],  buf[5]);

	jint ret = g_port->Write((unsigned char*)buf, length);
	free(buf);

	return ret;
}

static void setListener(JNIEnv *env, jobject thiz, jobject listener){

	mListenerObj = env->NewGlobalRef(listener);

	jclass clazz = env->GetObjectClass(listener);

	method_reportNewData = env->GetMethodID(clazz, "onNewData", "([B)V");

	LOGD(" setListener method_reportNewData is %d" , method_reportNewData);
}

static JNINativeMethod method_table[] = {
    {"native_open","(Ljava/lang/String;I)I",(void *)open_port},
    {"native_close","()I",(void *)close_port},
    {"native_write","([BI)I",(void *)write_port},
    {"native_set_listener","(Lcom/tomwin/hardware/SerialPort$Listener;)V", (void*)setListener},
};

/*
* Register several native methods for one class.
*/
static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods)
{
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL) {
		LOGD(" registerNativeMethods clazz is NULL");
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		LOGD(" registerNativeMethods failed ");
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

int register_android_hardware_SerialPort(JNIEnv *env)
{
    return registerNativeMethods(env, "com/tomwin/hardware/SerialPort",
            method_table, sizeof(method_table) / sizeof(method_table[0]));
}


extern "C" jint JNICALL JNI_OnLoad(JavaVM* vm, void* )
{
	JNIEnv* env = NULL;
	jint result = -1;

	g_JavaVM = vm;
	LOGD("JNI_OnLoad");

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		goto bail;
	}

	//assert(env != NULL);

	register_android_hardware_SerialPort(env);

	LOGD("JNI_OnLoad success");

	result = JNI_VERSION_1_4;
	goto success;

bail:
	LOGE("JNI_OnLoad failed");
success:

	return result;

}
