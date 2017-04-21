#include <jni.h>
#include <assert.h>
#include <android/log.h>
#include <stdio.h>
#include <time.h>
#include "android_runtime/AndroidRuntime.h"

#define TAG "Radio_jni"

#include "../include/ilog.h"
#include "TunerProxy.h"

namespace android {

	class TunerListener ;

	static jmethodID method_reportSignal = NULL;

	static jmethodID method_reportFreq = NULL;

	static jmethodID method_reportScanState = NULL;

	static jobject mCallbacksObj = NULL;

	//rds call backs

	static jobject mRdsCallbackObj = NULL;

	static jmethodID method_reportPI = NULL;

	static jmethodID method_reportPTY = NULL;

	static jmethodID method_reportPS = NULL;

	static jmethodID method_reportAltFreqs = NULL;

	static jmethodID method_reportRadioText = NULL;

	static JNIEnv* mEnv = NULL;

	static TunerProxy* tuner = NULL;

	static ITunerListener* mListener;

	void onSignal(int freq,int band, int level){

		JNIEnv* env = AndroidRuntime::getJNIEnv();

		if (method_reportSignal != NULL){
			env->CallVoidMethod(mCallbacksObj, method_reportSignal, freq, band, level);
		} else
			LOGD("report signal failed , not find java method");
	}

	void onFreqChanage(int freqNew, int bandNew, int freqOld, int bandOld){
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		if (method_reportFreq != NULL)
			env->CallVoidMethod(mCallbacksObj, method_reportFreq, freqNew, bandNew, freqOld, bandOld);
		else
			LOGD("report freq change failed , not find java method");
	}

	void onSeekStatusChange(int scanType, int newStatus, int oldStatus){
		JNIEnv* env = AndroidRuntime::getJNIEnv();

		if (method_reportScanState != NULL)
			env->CallVoidMethod(mCallbacksObj, method_reportScanState, scanType, newStatus, oldStatus);
		else
			LOGD("report seek status failed , not find java method");
	}

	class RdsListener : public IRdsListener{

	public:
		RdsListener(){
		}

		~RdsListener(){
		}

		void onPi(unsigned char pi){
			JNIEnv* env = AndroidRuntime::getJNIEnv();

			if (method_reportPI != NULL)
				env->CallVoidMethod(mRdsCallbackObj, method_reportPI, pi);
			else
				LOGD("report rds pi failed , not find java method");
		}

		void onPty(unsigned char pty){
			JNIEnv* env = AndroidRuntime::getJNIEnv();

			if (method_reportPTY != NULL)
				env->CallVoidMethod(mRdsCallbackObj, method_reportPTY, pty);
			else
				LOGD("report rds pty failed , not find java method");
		}

		void onPs(char* ps){
			JNIEnv* env = AndroidRuntime::getJNIEnv();

			jstring str = env->NewStringUTF(ps);

			if (str == NULL)
				return;

			if (method_reportPS != NULL)
				env->CallVoidMethod(mRdsCallbackObj, method_reportPS, str);
			else
				LOGD("report rds ps failed , not find java method");

			env->DeleteLocalRef(str);
		}

		void onAltFreqs(int* freqs, int len){
			JNIEnv* env = AndroidRuntime::getJNIEnv();

			jintArray array = env->NewIntArray(len);

			if (array == NULL)
				return;

			env->SetIntArrayRegion(array, 0, len, freqs);

			if (method_reportAltFreqs != NULL)
				env->CallVoidMethod(mRdsCallbackObj, method_reportAltFreqs, array);
			else
				LOGD("report rds ps failed , not find java method");

			env->DeleteLocalRef(array);
		}

		void onRadioText(char* text){

			JNIEnv* env = AndroidRuntime::getJNIEnv();

			jstring str = env->NewStringUTF(text);

			if (str == NULL)
				return;

			if (method_reportRadioText != NULL)
				env->CallVoidMethod(mRdsCallbackObj, method_reportRadioText, str);
			else
				LOGD("report rds ps failed , not find java method");

			env->DeleteLocalRef(str);
		}
	};

	class TunerListener : public ITunerListener {
		
		public:
		TunerListener(){

		}
		virtual ~TunerListener(){

		}

		virtual void OnScan(int type, int newState, int oldState){
			onSeekStatusChange(type, newState, oldState);
		}

		virtual void OnRssi(int freq, int band, int rssi){
			onSignal(freq, band, rssi);
		}

		virtual void OnFreq(int newFreq, int newBand, int oldFreq, int oldBand){

			onFreqChanage(newFreq, newBand, oldFreq, oldBand);
		}
	};

	/**
	 * 打开设备
	 */
	jint open(JNIEnv* env, jobject obj){

		if (mCallbacksObj == NULL)
				mCallbacksObj = env->NewGlobalRef(obj);

		jclass clazz = env->GetObjectClass(obj);

		method_reportSignal = env->GetMethodID(clazz, "onSignal", "(III)V");

		method_reportFreq = env->GetMethodID(clazz, "onFreq", "(IIII)V");

		method_reportScanState = env->GetMethodID(clazz, "onState", "(III)V");

		tuner = new TunerProxy(DEVICE_NAME, new RdsListener());
		
		mListener = new TunerListener();

		tuner->setListener(mListener);

		return 0;
	}
	/**
	 * 关闭设备
	 */
	jint close(JNIEnv* env, jobject ){

		if (tuner != NULL){
			delete tuner;
			tuner = NULL;
		}

		env->DeleteGlobalRef(mCallbacksObj);
		mCallbacksObj = NULL;
		
		delete mListener;

		return 0;
	}

	int set_freq(JNIEnv* , jobject , jint freq , jint band){
		if (tuner != NULL)
			return tuner->setFreq(freq, band);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int seek_up(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->seekUp();

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int seek_down(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->seekDown();

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int scan_up(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->scanUp();

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int scan_down(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->scanDown();

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int scan_save(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->scanChannel();

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int set_mute(JNIEnv* , jobject , jint mute){
		if (tuner != NULL)
			return tuner->setMute(mute);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int set_volume(JNIEnv* , jobject , jint volume){
		if (tuner != NULL)
			return tuner->setVolume(volume);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int set_stereo(JNIEnv* , jobject , jint stereo){
		if (tuner != NULL)
			return tuner->setStereo(stereo);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int get_stereo(JNIEnv* , jobject , jint freq){
		if (tuner != NULL)
			return tuner->isFmStero(freq);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int get_rssi(JNIEnv* , jobject , jint freq, jint band){
		if (tuner != NULL)
			return tuner->getRSSI(freq, band);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int stop(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->stop();

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int getMax(JNIEnv* , jobject , jint band, jint area){
		if (tuner != NULL)
			return tuner->getMax(band, area);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int getMin(JNIEnv* , jobject , jint band, jint area){
		if (tuner != NULL)
			return tuner->getMin(band, area);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int getStep(JNIEnv* , jobject , jint band, jint area){
		if (tuner != NULL)
			return tuner->getStep(band, area);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int setArea(JNIEnv* , jobject , jint area){
		if (tuner != NULL)
			return tuner->setArea(area);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	int setRssiLevel(JNIEnv* , jobject ,int band, int level){
		if (tuner != NULL)
			return tuner->setRssiLevel(band, level);

		LOGE("tuner is null, failed %s", __func__);
		return -1;
	}

	bool isSupportRds(JNIEnv* , jobject ){
		if (tuner != NULL)
			return tuner->isSupportRds();

		return false;
	}

	void setRdsListener(JNIEnv* env, jobject , jobject listener){
		mRdsCallbackObj = env->NewGlobalRef(listener);

		jclass clazz = env->GetObjectClass(listener);

		method_reportPI = env->GetMethodID(clazz, "onPI", "(I)V");
		method_reportPTY = env->GetMethodID(clazz, "onPTY", "(I)V");
		method_reportPS = env->GetMethodID(clazz, "onPS", "(Ljava/lang/String;)V");
		method_reportAltFreqs = env->GetMethodID(clazz, "onAltFreqs", "([I)V");
		method_reportRadioText = env->GetMethodID(clazz, "onRadioText", "(Ljava/lang/String;)V");
	}

	static JNINativeMethod sMethods[] = {
		 /* name, signature, funcPtr */
		{"native_open", "()I", (void *)open},
		{"native_close", "()I", (void*)close},
		{"native_setFreq", "(II)I", (void*)set_freq},
		{"native_seekUp", "()I", (void*)seek_up},
		{"native_seekDown", "()I", (void*)seek_down},
		{"native_scanUp", "()I", (void*)scan_up},
		{"native_scanDown", "()I", (void*)scan_down},
		{"native_scanSave", "()I", (void*)scan_save},
		{"native_setVolume", "(I)I", (void*)set_volume},
		{"native_setStereo", "(I)I", (void*)set_stereo},
		{"native_getStereo", "(I)I", (void*)get_stereo},
		{"native_getRSSI", "(II)I", (void*)get_rssi},
		{"native_stop", "()I", (void*)stop},
		{"native_getMax","(II)I", (void*)getMax},
		{"native_getMin","(II)I", (void*)getMin},
		{"native_getStep","(II)I", (void*)getStep},
		{"native_setArea","(I)I", (void*)setArea},
		{"native_setRssiLevel","(II)I", (void*)setRssiLevel},
		{"native_setMute","(I)I", (void*)set_mute},
		{"native_isSupportRds", "()Z", (void*)isSupportRds},
		{"native_setRdsListener","(Lcom/itoday/ivi/tuner/Tuner$OnRdsListener;)V", (void*)setRdsListener},

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

	extern "C" jint JNICALL JNI_OnLoad(JavaVM* vm, void* )
	{
		JNIEnv* env = NULL;
		jint result = -1;

		LOGV("JNI_OnLoad");

		if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
			goto bail;
		}

		assert(env != NULL);

		mEnv = env;

		if(jniRegisterNativeMethods(env, "com/itoday/ivi/tuner/Tuner",
				sMethods, sizeof(sMethods)/sizeof(sMethods[0])) < 0)
			 goto bail;


		LOGV("JNI_OnLoad success");

		result = JNI_VERSION_1_4;
		goto success;

	bail:
		LOGE("JNI_OnLoad failed");
	success:

		return result;

	}
}  // namespace android
