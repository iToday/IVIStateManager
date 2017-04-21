#ifndef ANDROID_TUNER_PROXY_H
#define ANDROID_TUNER_PROXY_H

#include <binder/BinderService.h>
#include "include/radio.h"
#include "include/band.h"
#include <utils/Thread.h>
#include <stdint.h>
#include <sys/types.h>
#include "RdsParser.h"

#include <utils/Errors.h>
#include <utils/RefBase.h>

#include <binder/IInterface.h>

enum Status{
	PLAYING,
	SCANNING,
	CLOSED
};

enum Scan{
	SCAN_NONE,
	SCAN_UP,
	SCAN_DOWN,
	SCAN_SAVE,
	SCAN_ALL,
};


#define DEVICE_NAME "/dev/SI4754-A40"

#define RADIO_HARDWARE_MODULE_ID "radio"

using namespace android;

class ITunerListener{

public :

	ITunerListener(){};
	virtual ~ITunerListener(){};

	virtual void OnScan(int type, int newState, int oldState) = 0;
	virtual void OnRssi(int freq, int band, int rssi) = 0;
	virtual void OnFreq(int newFreq, int newBand, int oldFreq, int oldBand) = 0;
};

class ScanBase;

class TunerProxy{

public:
	static const char* TUNER_SCANER_THREAD ;

private:
	ITunerListener* mListener;

	struct radio_device_t *mDev;

	pthread_t scan_thread;

	pthread_t rds_thread;

	bool mExitPending;

    int mBand;

    int mFreq;

    int mArea;

    int mScanState;

    char* mDeviceName;

    int mRssiLevel[BAND_NUM];

    ScanBase* mScan;

    RdsParser* mRdsParser;

public:
    TunerProxy(const char* name, IRdsListener* rdsListener);
    
    virtual ~TunerProxy();

    void setListener(ITunerListener* listener){this->mListener = listener; }

    int setFreq(int freq, int band);

	int getFreq();

	int getBand();

	int scanUp();

	int scanDown();

	int seekUp();

	int seekDown();

	int scanChannel();

	int scanAllRadio();

	int stop();

	bool isFreqValid(int band, int freq);

	bool isFmStero(int freq);

	int getMax(int band, int area = -1);

	int getStep(int band, int area = -1);

	int getMin(int band, int area = -1);

	int getDefault(int band, int area);

	int getSeekingFreq();

	int getScanType();

	int scan(int freq);

	int setStereo(int stereo);

	int setMute(int mute);

	int setVolume(int volume);

	int setArea(int area);

	int getRSSI(int freq, int band);
	
	int setRssiLevel(int band, int level);

	bool isSupportRds();

	private:
		virtual bool threadLoop();

		static void* scan_thread_func(void* args);

		static void* rds_thread_func(void* args);

		void run(const char* name, int32_t priority);

		void requestExit();

		size_t requestExitAndWait();

		int getRds(unsigned char *info, int& len);

};

class ScanBase{

protected :

	int mScaningFreq;

	TunerProxy* mTuner;

public:
	ScanBase(TunerProxy* tuner){ mTuner = tuner;}

	virtual ~ScanBase(){}
	virtual int start(int freq) { mScaningFreq = freq; return mScaningFreq;}
	virtual int getScanningFreq(){ return mScaningFreq;}

	virtual int getNextFreq() = 0;
	virtual bool canEnd( bool valid, int freq) = 0;

	virtual int getScanType() = 0;

	virtual int scan(int freq);
};

class ScanPrev: public ScanBase{

public:
	ScanPrev( TunerProxy* tuner): ScanBase(tuner) {}
	virtual int getNextFreq();
	virtual bool canEnd( bool valid, int freq);
	virtual int getScanType();

};

class ScanNext: public ScanBase{
public:
	ScanNext(TunerProxy*  s): ScanBase(s){ }
	virtual int getNextFreq();
	virtual bool canEnd( bool valid, int freq);
	virtual int getScanType();
};

class ScanChannel: public ScanNext{
 public:
	ScanChannel( TunerProxy*  s): ScanNext(s){ }
	virtual bool canEnd( bool valid, int freq);
	virtual int getScanType();
};

class ScanAllRadio: public ScanChannel{

 public:
	ScanAllRadio( TunerProxy*  s): ScanChannel(s){ }
	virtual int getNextFreq();
	virtual bool canEnd( bool valid, int freq);
	virtual int getScanType();
};

#endif
