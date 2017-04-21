#include "TunerProxy.h"
#include "include/band.h"
#include "jni.h"
#include "android_runtime/AndroidRuntime.h"

#include <hardware/hardware.h>

#define TAG "TunerProxy"

#include "../include/ilog.h"

using namespace android;

const char* TunerProxy::TUNER_SCANER_THREAD = "Tuner Scaner Thread";

TunerProxy::TunerProxy(const char* name, IRdsListener* rdsListener){

	if (name == NULL)
			name = DEVICE_NAME;

	mDeviceName = new char[strlen(name) + 1]{0};

	memcpy(mDeviceName, name, strlen(name));

	radio_module_t *radio_module = NULL;

	if (hw_get_module(RADIO_HARDWARE_MODULE_ID,(const hw_module_t**) &radio_module) == 0)
	{
		if (radio_module->common.methods->open(&radio_module->common, mDeviceName, (struct hw_device_t**)&mDev) != 0){
			mDev = NULL;

			LOGE(" open RADIO_HARDWARE_MODULE_ID failed \n");
		}

	} else {
		LOGE(" hw_get_module RADIO_HARDWARE_MODULE_ID failed \n");
	}

	mArea = 0;
	mBand = BAND_DEFAULT;

	mFreq = getDefault(mBand , mArea);

	mScanState = PLAYING;
	mScan = NULL;

	memset(mRssiLevel, 0 , sizeof(mRssiLevel));

	mRdsParser = new RdsParser(rdsListener);

	if (isSupportRds()){

		if (pthread_create(&rds_thread, NULL, rds_thread_func, (void *)this) != 0){
			LOGE("create rds  thread failed %s", __func__);
		}
	} else
		LOGE("not support rds %s", __func__);
}

TunerProxy::~TunerProxy(){

	if (mDev != NULL)
	{
		mDev->common.close(&mDev->common);
		mDev = NULL;
	}

	if (mRdsParser != NULL)
		delete mRdsParser;

	if (mDeviceName != NULL)
		delete []mDeviceName;

	if (mScan != NULL)
		delete mScan;

	mDeviceName = NULL;
	mRdsParser = NULL;
	mScan = NULL;
}

void* TunerProxy::rds_thread_func(void* args){
	TunerProxy* proxy = (TunerProxy*)args;

	JNIEnv* env;
	JavaVM* vm = AndroidRuntime::getJavaVM();

	vm->AttachCurrentThread(&env, NULL);

	unsigned char buffer[RDS_BUFFER_SIZE] = {0};
	int len = RDS_BUFFER_SIZE;
	int res = 0;

	do {
		res = proxy->getRds(buffer, len);

		if (res == RADIO_ERROR){
			LOGE(" get rds info failed from driver \n");
			break;
		}

		if (proxy->mRdsParser->parse(buffer, len))
			break;

	} while (true);

	vm->DetachCurrentThread();
	return NULL;
}

int TunerProxy::getRds(unsigned char *info, int& len){

	if (mDev != NULL){

		return mDev->get_rds(mDev, info, &len);
	}

	return -1;
}

bool TunerProxy::isSupportRds(){

	if (mDev != NULL)
		return mDev->is_support_rds(mDev);

	return false;
}

void* TunerProxy::scan_thread_func(void* args){

	TunerProxy* proxy = (TunerProxy*)args;

	JNIEnv* env;
	JavaVM* vm = AndroidRuntime::getJavaVM();

	vm->AttachCurrentThread(&env, NULL);

	do{
		if (!proxy->threadLoop())
			break;

		if (proxy->mExitPending){
			proxy->scan_thread = -1;
			break;
		}
	} while(true);

	vm->DetachCurrentThread();
	return NULL;
}

void TunerProxy::run(const char* name, int32_t priority){

	mExitPending = false;

	if (pthread_create(&scan_thread, NULL, scan_thread_func, (void *)this) != 0){
			LOGD("create thread failed %s", __func__);
	}
}

void TunerProxy::requestExit(){
	mExitPending = true;
}

size_t TunerProxy::requestExitAndWait(){

	mExitPending = true;

	if (scan_thread >= 0)
		pthread_join(scan_thread, NULL);//wait thread exit

	scan_thread = -1;

	return 0;
}

int TunerProxy::setFreq(int freq, int band){

	LOGD("setFreq %d, %d", freq, band);

	if (mDev != NULL){

		if (mDev->set_freq(mDev, freq, band) != RADIO_ERROR){

			if (mListener != NULL)
				mListener->OnFreq(freq, band, mFreq, mBand);

			this->mFreq = freq;
			this->mBand = band;

			return freq;
		}

		LOGE("set freq, failed %s", __func__);
	} else
		LOGE("mDev is null, failed %s", __func__);

	return RADIO_ERROR;
}

int TunerProxy::getFreq(){

	return mFreq;
}

int TunerProxy::scanUp(){

	requestExitAndWait();

	int start_freq = mFreq - getStep(mBand , mArea);

	if (!isFreqValid(mBand, start_freq))
		start_freq = getMax(mBand , mArea);

	if (mScan != NULL)
		start_freq = mScan->getScanningFreq();//接着正在扫描的频率扫描

	mScan = new ScanPrev(this);

	mScan->start(start_freq);

	run(TUNER_SCANER_THREAD, PRIORITY_URGENT_DISPLAY);

	if (mListener != NULL)
		mListener->OnScan(mScan->getScanType() ,SCANNING, mScanState);

	mScanState = SCANNING;
	return 0;
}

int TunerProxy::scanDown(){

	requestExitAndWait();

	int start_freq = mFreq + getStep(mBand , mArea);

	if (!isFreqValid(mBand, start_freq))
		start_freq = getMin(mBand , mArea);

	if (mScan != NULL)
		start_freq = mScan->getScanningFreq();//接着正在扫描的频率扫描

	mScan = new ScanNext(this);

	mScan->start(start_freq);

	run(TUNER_SCANER_THREAD, PRIORITY_URGENT_DISPLAY);

	if (mListener != NULL)
		mListener->OnScan(mScan->getScanType(), SCANNING, mScanState);

	mScanState = SCANNING;
	return 0;
}

int TunerProxy::seekUp(){
	LOGD("seekUp.. ");

	int new_freq = mFreq - getStep(mBand , mArea);

	if (!isFreqValid(mBand, new_freq)){
		LOGD("invalid new_freq is %d , band is %d, and setMin " , new_freq, mBand);
		new_freq = getMax(mBand , mArea);
	}

	return setFreq(new_freq, mBand);
}

int TunerProxy::seekDown(){

	int new_freq = mFreq + getStep(mBand , mArea);

	if (!isFreqValid(mBand, new_freq)){
		LOGD("invalid new_freq is %d , band is %d, and setMax " , new_freq, mBand);
		new_freq = getMin(mBand , mArea);
	}

	return setFreq(new_freq, mBand);
}

int TunerProxy::scanChannel(){

	LOGD("scanChannel...");
	requestExitAndWait();

	mScan = new ScanChannel(this);

	mScan->start(getMin(mBand , mArea));

	run(TUNER_SCANER_THREAD, PRIORITY_URGENT_DISPLAY);

	if (mListener != NULL)
		mListener->OnScan(mScan->getScanType(), SCANNING, mScanState);

	mScanState = SCANNING;
	return 0;
}

int TunerProxy::scanAllRadio(){

	requestExitAndWait();

	mScan = new ScanAllRadio(this);

	mScan->start( getMin(mBand , mArea));

	run(TUNER_SCANER_THREAD, PRIORITY_URGENT_DISPLAY);

	if (mListener != NULL)
		mListener->OnScan(mScan->getScanType(), SCANNING, mScanState);

	mScanState = SCANNING;

	return 0;
}

int TunerProxy::setArea(int area){

	if (area >= 0 && area < AREA_NUM)
		mArea = area;

	LOGD("setArea mArea %d, area %d", mArea, area);
	return mArea;
}

int TunerProxy::stop(){

	LOGD("stop ..");
	requestExitAndWait();

	LOGW("RadiomTuner :stop  " );

	if (mScan != NULL){

		if (mListener != NULL)
			mListener->OnScan(mScan->getScanType(), PLAYING, mScanState);

		mScanState = PLAYING;

		mScan = NULL;

	}

	return -1;
}

bool TunerProxy::isFreqValid(int band, int freq){

	return is_valid_freq(band, freq, mArea);
}

int TunerProxy::getRSSI(int freq, int band){

	if (mDev != NULL){

		return mDev->get_rssi(mDev,freq, band);
	}else
		LOGE("mDev is null, failed %s", __func__);

	return RADIO_ERROR;
}

int TunerProxy::setRssiLevel(int band, int level){

	if (!is_valid_band(band))
		return -1;

	mRssiLevel[band] = level;

	return level;
}

bool TunerProxy::isFmStero(int freq){

	if (mDev != NULL){

		return mDev->is_stereo(mDev, freq);
	}else
		LOGE("mDev is null, failed  %s", __func__);

	return false;
}

int TunerProxy::getMax(int band, int area){

	if (area == -1)
		area = mArea;

	return get_max(band, area);
}

int TunerProxy::getStep(int band, int area){

	if (area == -1)
		area = mArea;

	return get_step(band, area);
}

int TunerProxy::getMin(int band, int area){

	if (area == -1)
		area = mArea;

	return get_min(band, area);
}

int TunerProxy::getDefault(int band, int area){
	return get_default(band, area);
}

int TunerProxy::getSeekingFreq(){

	if (mScan != NULL)
		return mScan->getScanningFreq();

	return -1;
}

int TunerProxy::getScanType(){

	if (mScan == NULL)
		return SCAN_NONE;

	return mScan->getScanType();
}

int TunerProxy::scan(int freq){

	if (mDev != NULL){
		return mDev->get_rssi(mDev, freq, mBand);
	}else
		LOGE("mDev is null, failed %s", __func__);

	return RADIO_ERROR;
}

int TunerProxy::setStereo(int stereo){

	if(mDev != NULL){
		return mDev->set_stereo(mDev, stereo);
	}else
		LOGE("mDev is null, failed");

	return RADIO_ERROR;
}

int TunerProxy::setMute(int mute){

	if (mDev != NULL){

		return mDev->set_mute(mDev, mute);
	}else
		LOGE("mDev is null, failed %s", __func__);

	return RADIO_ERROR;
}

int TunerProxy::setVolume(int volume){

	if (mDev != NULL){

		return mDev->set_volume(mDev, volume);
	}else
		LOGE("mDev is null, failed %s", __func__);

	return RADIO_ERROR;
}

int TunerProxy::getBand(){

	return mBand;
}


bool TunerProxy::threadLoop(){

	if (mScan == NULL){
		return false;
	}

	int freq = mScan->getScanningFreq();

	int rssi = mScan->scan(freq);

	if (mListener != NULL)
		mListener->OnRssi(freq, mBand, rssi);

	if (mScan == NULL){
		return false;
	}

	if (mScan->canEnd( rssi >= mRssiLevel[mBand] ? true: false, freq)){

		requestExit();

		ALOGW("RadioService :threadLoop pScan->canEnd(freq) vali  %s", __func__ );

		if (mListener != NULL)
			mListener->OnScan(mScan->getScanType(),PLAYING, mScanState);

		mScanState = PLAYING;

		mScan = NULL;

		return false;//exit thread loop
	}

	mScan->getNextFreq();
	return true;
}

/**
 *
 */
int ScanBase::scan(int freq){

		if (mTuner != NULL)
			return mTuner->scan(freq);

		return -1;
}

int ScanPrev::getNextFreq(){

	int band = mTuner->getBand();
	int next = mScaningFreq - mTuner->getStep(band);

	if (mTuner->isFreqValid(band, next))
			mScaningFreq = next;
	else
		mScaningFreq = mTuner->getMax(band);

	return mScaningFreq;
}

int ScanPrev::getScanType(){
	return SCAN_UP;
}

bool ScanPrev::canEnd( bool valid, int freq){

	if (valid){
		ALOGW("RadiomTuner :threadLoop pScan->canEnd(freq:%d) valid.%d " , freq, valid);
		mTuner->setFreq(freq, mTuner->getBand());
		return true;
	}
	else if(mTuner->getFreq() == mScaningFreq){
		mTuner->setFreq(mScaningFreq, mTuner->getBand());
		return true;
	}

	return false;
}

int ScanNext::getNextFreq(){
	int band = mTuner->getBand();
	int next = mScaningFreq + mTuner->getStep(band );

	if (mTuner->isFreqValid(band, next))
		mScaningFreq = next;
	else
		mScaningFreq = mTuner->getMin(band );

	return mScaningFreq;
}

int ScanNext::getScanType(){
	return SCAN_DOWN;
}

bool ScanNext::canEnd( bool valid, int freq){
	if (valid){
		mTuner->setFreq(freq, mTuner->getBand());
		return true;
	}
	else if(mTuner->getFreq() == mScaningFreq){
		mTuner->setFreq(mScaningFreq, mTuner->getBand());
		return true;
	}

	return false;
}

bool ScanChannel::canEnd(bool valid, int freq){

int nMax = mTuner->getMax(mTuner->getBand());

if ( nMax == mScaningFreq){
	//mTuner->setFreq(mTuner->getFreq());	//恢复播放存台之前的电台，注释掉让client自己选择需要播放的电台;
		return true;
	}

	return false;
}

int ScanChannel::getScanType(){
	return SCAN_SAVE;
}

/** 扫描所有电台FM and AM
**/
int ScanAllRadio::getNextFreq(){

	int band = mTuner->getBand();
	int next = mScaningFreq + mTuner->getStep(band );

	if (mTuner->isFreqValid(band, next))
		mScaningFreq = next;
	else if (band == BAND_FM){

	  mTuner->setFreq(mTuner->getMin(BAND_AM_MW), BAND_AM_MW);
	  return getNextFreq();

	}else
		mScaningFreq = mTuner->getMin(band );

	return mScaningFreq;

}
//扫描到AM才停止
bool ScanAllRadio::canEnd( bool valid, int freq){
	int nMax = mTuner->getMax(BAND_AM_MW );

	if ( nMax == mScaningFreq){
	//mTuner->setFreq(mTuner->getFreq());	//恢复播放存台之前的电台，注释掉让client自己选择需要播放的电台;
		return true;
	}

	return false;
}

int ScanAllRadio::getScanType(){
	return SCAN_ALL;
}
