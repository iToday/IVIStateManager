#ifndef ANDROID_MCU_PROXY_H
#define ANDROID_MCU_PROXY_H

#include "../service/server/McuService.h"

#define TAG "McuClient"
namespace android{
	
typedef void (* device_callback)(int device, int state);

typedef void (* key_callback)(int key, int state, int step);

typedef void (* version_callback)(const unsigned char * data, int len);

typedef void (* canbus_callback)(const unsigned char * data, int len);

typedef void (* power_callback)(int on);

typedef void (* time_callback)(int time);

typedef void (* keysetup_result_callback)(int adc1, int adc2, int adc3);

typedef void (* keysetup_status_callback)(int status);

typedef void (* upgrade_callback)(int status, int progress);

struct McuCallbacks{
	
	device_callback onDevice;
	
	key_callback onKey;
	
	version_callback onVersion;
	
	time_callback onTime;
	
	canbus_callback onCanInfo;
	
	power_callback onPower;
	
	keysetup_result_callback onKeySetupResult;

	keysetup_status_callback onKeySetupStatus;

	upgrade_callback onUpgrade;

};

class McuListener : public BnListener{
	
	private:
		struct McuCallbacks* callbacks;
		
		int type;
		
	public:
		McuListener(int type, McuCallbacks* callbacks);
		~McuListener(){}

		virtual int onRecv(const unsigned char *pData, int len);
		virtual int getType();		
    
    	void setCallbacks(struct McuCallbacks *callbacks);
};

class McuDeathObserver : public IBinder::DeathRecipient {

private:
		sp<IMcuServer> mMcuServer;

		virtual void binderDied(const wp<IBinder>& who) {

		 // LOGW("mcuoservice died [%p]", who.unsafe_get());

		  if (mMcuServer != NULL)
			  mMcuServer = NULL;
		}
      
  public:
      McuDeathObserver(sp<IMcuServer>& server) { }
};

class Mcu{
	
  private:
  	
	 sp<IMcuServer> mMcuServer;
	 
	 //acc 状态
	 sp<McuListener> accListener;

	 //时间同步
	 sp<McuListener> timeListener;

	 //大灯，手刹状态
	 sp<McuListener> devListener;
	 
	 //按键
	 sp<McuListener> keyListener;
	 
	 //版本信息
	 sp<McuListener> verListener;
	 
	 //canbus 信息
	 sp<McuListener> canListener;

	 //
	 sp<McuListener> sleepListener;
	 //upgrade
	 sp<McuListener> upgradepListener;

	 //sp<McuListener> handshakeLitener;
	 	 
	 sp<McuDeathObserver> serverDeath;
	 
	public:
		
	 Mcu(struct McuCallbacks *callbacks);
	 ~Mcu();
	 
	 int open();
	 
	 int close();	
	 
	 void requestMcuVersion();	 
	 
	int startUpgrade(const char* path);

	int endUpgrade();

	 int send(int cmd, char *pBuffer, int len);

	 void setCallbacks(struct McuCallbacks *callbacks);		
};
}

#endif
