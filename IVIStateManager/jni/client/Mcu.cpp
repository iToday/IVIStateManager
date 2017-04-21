#include <jni.h>
#include <assert.h>
#include <android/log.h>
#include <stdio.h>
#include <time.h>

#include "../include/protocol.h"
#include "McuProxy.h"
#include "../include/ilog.h"
#include <jni.h>
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

#define TAG "Mcu_jni"

namespace android {

	static jmethodID method_reportDevice;

	static jmethodID method_reportKey;

	static jmethodID method_reportVersion;

	static jmethodID method_reportCanbusInfo;
	
	static jmethodID method_reportPower;

	static jmethodID method_reportKeySetupResult;

	static jmethodID method_reportKeySetupStatus;

	static jmethodID method_reportUpgrade;

	static jobject mCallbacksObj = NULL;

	static Mcu* mMcu = NULL;
	
	void OnDevice(int device, int state);

	void OnKey(int key, int state, int step);

	void OnVersion(const unsigned char * data, int len);

	void OnCanbus(const unsigned char * data, int len);

	void OnPower(int on);

	void OnTime(int time);

	void OnKeySetupResult(int adc1, int adc2, int adc3);

	void OnKeySetupStatus(int status);

	void OnUpgrade(int state, int progress);

	int setCurrentTimeMillis(int64_t millis);

	static McuCallbacks callBacks = {
		OnDevice,
		OnKey,
		OnVersion,
		OnTime,
		OnCanbus,
		OnPower,
		OnKeySetupResult,
		OnKeySetupStatus,
		OnUpgrade
	};

	/**
	 * 打开设备
	 */
	jint open(JNIEnv* env, jobject obj){

		if (mCallbacksObj == NULL)
				mCallbacksObj = env->NewGlobalRef(obj);

		jclass clazz = env->GetObjectClass(obj);

		method_reportDevice = env->GetMethodID(clazz, "onDevice", "(II)V");

		method_reportKey = env->GetMethodID(clazz, "onKey", "(III)V");

		method_reportVersion = env->GetMethodID(clazz, "onVersion", "(ILjava/lang/String;)V");

		method_reportCanbusInfo = env->GetMethodID(clazz, "onCanBusInfo", "(ILjava/lang/String;)V");
		
		method_reportPower = env->GetMethodID(clazz, "onPower", "(I)V");

		method_reportKeySetupResult = env->GetMethodID(clazz, "onKeySetupResult", "(III)V");

		method_reportKeySetupStatus = env->GetMethodID(clazz, "onKeySetupStatus", "(I)V");

		method_reportUpgrade = env->GetMethodID(clazz, "onUpgrade", "(II)V");


		mMcu = new Mcu(&callBacks);
		mMcu->open();
		
		return 0;
	}
	/**
	 * 关闭设备
	 */
	jint close(JNIEnv* env, jobject obj){

		if (mMcu != NULL){

			mMcu->close();

			delete mMcu;
			mMcu = NULL;
		}

		env->DeleteGlobalRef(mCallbacksObj);
		mCallbacksObj = NULL;

		return 0;
	}

	/**
	 * 学习按键
	 */
	jint setupKey(JNIEnv* env, jobject obj, jint key){

		if (mMcu == NULL)
			return -1;

		char cKey[2];
		cKey[0] = KEY_SET;
		cKey[1] = key;

		return mMcu->send(CTRL_KEY,cKey,sizeof(cKey));
	}

	/**
	 * 清空按键
	 */
	jint resetKeys(JNIEnv* env, jobject obj){
		if (mMcu == NULL)
			return -1;

		char cKey[1];
		cKey[0] = KEY_VALUE;

		return mMcu->send(CTRL_KEY,cKey,sizeof(cKey));
	}

	/**
	 * 发送按键
	 */
	jint  sendKeyToDev(JNIEnv* env, jobject obj, jint dev, jint key){
		if (mMcu == NULL)
			return -1;

		char cKey[2];
		cKey[0] = KEY_VALUE;
		cKey[1] = key;

		return mMcu->send(CTRL_KEY,cKey,sizeof(cKey));
	}

	/**
	 * 设置按键灯颜色
	 */
	jint setLightColor(JNIEnv* env, jobject obj, jint color){
		if (mMcu == NULL)
			return -1;

		char cColor = color;

		return mMcu->send(CTRL_COLOR_LED,&cColor,sizeof(cColor));
	}

	/**
	 * 获取版本
	 */
	jint requestVersion(JNIEnv* env, jobject obj, jint dev){
		if (mMcu == NULL)
			return -1;

		mMcu->requestMcuVersion();
		
		return 0;
	}

	jint setUpgrade(JNIEnv* env, jobject obj, jint state){
		if (mMcu == NULL)
			return -1;

		char cState = state;

		return mMcu->send(CTRL_UPDATE, &cState, sizeof(cState));
	}

	jint setRunning(JNIEnv* env, jobject obj, jint state){
		if (mMcu == NULL)
			return -1;

		char cState = state;

		return mMcu->send(CTRL_SYSTEM, &cState, sizeof(cState));
	}

	jint setDevPower(JNIEnv* env, jobject obj, jint dev, jint state){
		if (mMcu == NULL)
			return -1;

		if (dev != DEV_BT && dev != DEV_DVR
				&& dev != DEV_IPOD && dev != DEV_TV
				&& dev != DEV_FM_ANT){
			LOGE("setDevPower error : invalid device ");
			return -1;
		}

		char cState[1];
		cState[0] = state;

		return mMcu->send(dev, cState, sizeof(cState));
	}

	jint setAudioSource(JNIEnv* env, jobject obj, jint source){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = SOURCE;
		cState[2] = source;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setNaviNotify(JNIEnv* env, jobject obj, jint state){
		if (mMcu == NULL)
			return -1;

		char cState[2];
		cState[0] = 0x01;
		cState[1] = state;

		return mMcu->send(CTRL_GPS_VOLUME, cState, sizeof(cState));
	}

	jint setInputVolume(JNIEnv* env, jobject obj, jint volume){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = INPUT_GAIN;

		if (volume < 0)
			cState[2] = 0x80 | (-volume);
		else
			cState[2] = volume;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint initInputVolume(JNIEnv* /*env*/, jobject /*obj*/, jint main, jint phone, jint tv, jint aux, jint fm, jint ipod){

		if (mMcu == NULL)
			return -1;

		char cState[8];

		cState[0] = OTHER;
		cState[1] = INPUT_GAIN;
		cState[2] = main;
		cState[3] = phone;
		cState[4] = tv;
		cState[5] = aux;
		cState[6] = fm;
		cState[7] = ipod;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setMainVolume(JNIEnv* /*env*/, jobject /*obj*/, jint volume){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = VOLUME;
		cState[2] = volume;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}
	
	jint setMute(JNIEnv* env, jobject obj, jint mute){
		
		if (mMcu == NULL)
			return -1;

		char cState[2];
		cState[0] = MUTE;
		cState[1] = mute;
		//cState[2] = mute;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setSpeakerVolume(JNIEnv* env, jobject obj, jint fr, jint fl, jint rr, jint rl ){
		if (mMcu == NULL)
			return -1;

		char cFR[3], cFL[3], cRR[3], cRL[3];
		cFR[0] = OTHER;
		cFR[1] = FR_SPEAKER_GAIN;
		cFR[2] = fr;

		cFL[0] = OTHER;
		cFL[1] = FL_SPEAKER_GAIN;
		cFL[2] = fl;

		cRR[0] = OTHER;
		cRR[1] = RR_SPEAKER_GAIN;
		cRR[2] = rr;

		cRL[0] = OTHER;
		cRL[1] = RL_SPEAKER_GAIN;
		cRL[2] = rl;

		mMcu->send(CTRL_VOLUME, cFR, sizeof(cFR));
		mMcu->send(CTRL_VOLUME, cFL, sizeof(cFL));
		mMcu->send(CTRL_VOLUME, cRR, sizeof(cRR));

		return mMcu->send(CTRL_VOLUME, cRL, sizeof(cRL));
	}

	jint setSubwooferGain(JNIEnv* env, jobject obj, jint subwoofer){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = SUBWOOFER_GAIN;
		cState[2] = subwoofer;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setMixGain(JNIEnv* env, jobject obj, jint mix){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = MIX_GAIN;
		cState[2] = mix;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setBassQ(JNIEnv* env, jobject obj, jint bass){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = BASS_Q;
		cState[2] = bass;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setMidQ(JNIEnv* env, jobject obj, jint mid){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = MID_Q;
		cState[2] = mid;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setTrebleQ(JNIEnv* env, jobject obj, jint treble){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = TREBLE_Q;
		cState[2] = treble;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setBassGain(JNIEnv* env, jobject obj, jint gain){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = BASS_GAIN;

		if (gain < 0) gain |= -128; //将高位置为1

		cState[2] = gain;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setMidGain(JNIEnv* env, jobject obj, jint gain){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = MID_GAIN;

		if (gain < 0) gain |= -128; //将高位置为1

		cState[2] = gain;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setTrebleGain(JNIEnv* env, jobject obj, jint gain){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = TRELE_GAIN;

		if (gain < 0) gain |= -128; //将高位置为1

		cState[2] = gain;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	jint setLoudness(JNIEnv* env, jobject obj, jint gain){
		if (mMcu == NULL)
			return -1;

		char cState[3];
		cState[0] = OTHER;
		cState[1] = LOUDNESS_GAIN;
		cState[2] = gain;

		return mMcu->send(CTRL_VOLUME, cState, sizeof(cState));
	}

	/******设置时间************************************/
	jint setTime(JNIEnv* env, jobject obj, jint time){
		if (mMcu == NULL)
			return -1;

		char cState[2];
		cState[0] = SET;
		cState[1] = time;

		return mMcu->send(CTRL_TIME, cState, sizeof(cState));
	}

	jint requestTimeSync(JNIEnv* env, jobject obj){
		if (mMcu == NULL)
			return -1;

		char cState[1];
		cState[0] = GET;

		return mMcu->send(CTRL_TIME, cState, sizeof(cState));
	}

	/******以下为Canbus操作****************************/

	jint requestCanInfo(JNIEnv* env, jobject obj, jint id){
		if (mMcu == NULL)
			return -1;

		char cState[2];
		cState[0] = REQUEST;
		cState[1] = id;

		return mMcu->send(CTRL_CANBOX, cState, sizeof(cState));
	}

	jint setCanMode(JNIEnv* env, jobject obj, jint mode, jint bitrate){

		if (mMcu == NULL)
			return -1;

		char cState[4];
		cState[0] = mode;
		cState[1] = (bitrate >> 16) & 0xff;
		cState[2] = (bitrate >> 8) & 0xff;
		cState[3] = bitrate & 0xff;

		return mMcu->send(CTRL_CANBOX, cState, sizeof(cState));
	}

	jint startUpgrade(JNIEnv* env, jobject obj, jstring path){

		char cpath[512] = {0};
		const char *str = env->GetStringUTFChars( path, 0);

		memcpy(cpath, str, strlen(str));
		env->ReleaseStringUTFChars(path, str);

		return mMcu->startUpgrade(cpath);
	}

	jint endUpgrade(JNIEnv* env, jobject obj){
		return mMcu->endUpgrade();
	}

	void OnDevice(int device, int state){
		
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		env->CallVoidMethod(mCallbacksObj, method_reportDevice, device, state);
	}

	void OnKey(int key, int state, int step){
		
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		env->CallVoidMethod(mCallbacksObj, method_reportKey, key, state, step);
	}

	void OnVersion(const unsigned char * data, int len){

		char version[256] = {0};
		
		for (int i = 0; i < len ; i ++){
			LOGD("version : %x:%c", data[i],data[i]);
		}

		memcpy(version, data , len);
		
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		jstring jver = env->NewStringUTF(version);

		env->CallVoidMethod(mCallbacksObj, method_reportVersion, 0, jver);

		env->DeleteLocalRef(jver);
	}

	void OnCanbus(const unsigned char * data, int len){
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		char version[256] = {0};

		memcpy(version, data , len);

		jstring jver = env->NewStringUTF(version);

		env->CallVoidMethod(mCallbacksObj, method_reportCanbusInfo, 0, jver);

		env->DeleteLocalRef(jver);
	}

	void OnPower(int on){
		
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		env->CallVoidMethod(mCallbacksObj, method_reportPower, on);
		
	}

	void OnKeySetupResult(int adc1, int adc2, int adc3){

		JNIEnv* env = AndroidRuntime::getJNIEnv();

		env->CallVoidMethod(mCallbacksObj, method_reportKeySetupResult, adc1, adc2, adc3);
	}

	void OnKeySetupStatus(int status){

		JNIEnv* env = AndroidRuntime::getJNIEnv();

		env->CallVoidMethod(mCallbacksObj, method_reportKeySetupStatus, status);
	}

	void OnUpgrade(int state, int progress){
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		env->CallVoidMethod(mCallbacksObj, method_reportUpgrade, state, progress);
	}

	void OnTime(int time){
		setCurrentTimeMillis(time);
	}

	/*
	 * Set the current time.  This only works when running as root.
	 */
	int setCurrentTimeMillis(int64_t millis){
	    struct timeval tv;

	    int ret = 0;

	    if (millis <= 0 || millis / 1000LL >= INT_MAX) {
	        return -1;
	    }

	    tv.tv_sec = (time_t) (millis / 1000LL);
	    tv.tv_usec = (suseconds_t) ((millis % 1000LL) * 1000LL);

	    LOGD("Setting time of day to sec=%d\n", (int) tv.tv_sec);

	    if (settimeofday(&tv, NULL) != 0) {
	        LOGW("Unable to set clock to %d.%d: %s\n",
	            (int) tv.tv_sec, (int) tv.tv_usec, strerror(errno));
	        ret = -1;
	    }

	    return ret;
	}

	static JNINativeMethod sMethods[] = {
		 /* name, signature, funcPtr */
		//key
		{"setupKey", "(I)I", (void *)setupKey},
		{"resetKeys", "()I", (void*)resetKeys},
		{"sendKeyToDev", "(II)I", (void*)sendKeyToDev},
		//other
		{"setLightColor", "(I)I", (void*)setLightColor},
		{"requestVersion", "(I)I", (void*)requestVersion},
		{"setUpgrade", "(I)I", (void*)setUpgrade},
		{"setRunning", "(I)I", (void*)setRunning},
		{"setDevPower", "(II)I", (void*)setDevPower},
		{"setAudioSource", "(I)I", (void*)setAudioSource},
		{"setNaviNotify", "(I)I", (void*)setNaviNotify},
		//volume
		{"setInputVolume", "(I)I", (void*)setInputVolume},
		{"initInputVolume", "(IIIIII)I", (void*)initInputVolume},
		{"setMainVolume", "(I)I", (void*)setMainVolume},
		{"setSpeakerVolume", "(IIII)I", (void*)setSpeakerVolume},
		{"setMute", "(I)I", (void*)setMute},
		//eq
		{"setSubwooferGain", "(I)I", (void*)setSubwooferGain},
		{"setMixGain", "(I)I", (void*)setMixGain},
		{"setBassQ", "(I)I", (void*)setBassQ},
		{"setMidQ", "(I)I", (void*)setMidQ},
		{"setTrebleQ", "(I)I", (void*)setTrebleQ},
		{"setBassGain", "(I)I", (void*)setBassGain},
		{"setMidGain", "(I)I", (void*)setMidGain},
		{"setTrebleGain", "(I)I", (void*)setTrebleGain},
		{"setLoudness", "(I)I", (void*)setLoudness},
		//time
		{"setTime", "(I)I", (void*)setTime},
		{"requestTimeSync", "()I", (void*)requestTimeSync},
		//canbus
		{"requestCanInfo", "(I)I", (void*)requestCanInfo},
		{"setCanMode", "(II)I", (void*)setCanMode},
		{"open", "()I", (void*)open},
		{"close", "()I", (void*)close},
		{"startUpgrade", "(Ljava/lang/String;)I", (void*)startUpgrade},
		{"endUpgrade", "()I", (void*)endUpgrade},
	};

	/*
	 * Register native JNI-callable methods.
	 *
	 * "className" looks like "java/lang/String".
	 */
	int jniRegisterNativeMethods(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods)
	{
		jclass clazz;

		clazz = env->FindClass(className);

		if (clazz == NULL) {
			return -1;
		}

		int result = 0;
		if (env->RegisterNatives( clazz, gMethods, numMethods) < 0) {
			result = -1;
		}

		env->DeleteLocalRef(clazz);

		return result;
	}


}  // namespace android
using namespace android;
	extern "C" jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
	{
		JNIEnv* env = NULL;
		jint result = -1;

		LOGV("JNI_OnLoad");

		if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
			goto bail;
		}

		assert(env != NULL);

		if(AndroidRuntime::registerNativeMethods(env,  "com/itoday/ivi/platform/allwinner/Mcu", sMethods, NELEM(sMethods)) < 0)
			 goto bail;

		LOGV("JNI_OnLoad success");

		result = JNI_VERSION_1_4;
		goto success;

	bail:
		LOGE("JNI_OnLoad failed");
	success:

		return result;

	}
