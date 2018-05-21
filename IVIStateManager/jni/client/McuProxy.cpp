#include "McuProxy.h"
#include <binder/IBinder.h>
#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>
#include "../include/ilog.h"
#include "../include/protocol.h"

#define CONTROL_WORD_SIZE  4
#define MAX_TIME 4

namespace android{
	
	
	McuListener::McuListener(int t, McuCallbacks* callback){
		
		type = t;
		callbacks = callback;
	}

	int McuListener::onRecv(const unsigned char *pData, int len){

		if (callbacks == NULL){
			LOGE("callbacks is NULL");
			return -1;
		}

		switch(type){
		case CTRL_ACC_STATUS:
			callbacks->onDevice(CTRL_ACC_STATUS, pData[0]);
			break;
		case CTRL_FLAG:
			callbacks->onDevice(CTRL_FLAG, pData[0]);
			break;
		case CTRL_TIME:
			callbacks->onTime(pData[0] << 24 | pData[1] << 16 | pData[2] << 8 | pData[3]);
			break;
		case CTRL_KEY:
			LOGD("CTRL_KEY data is 0x%x , 0x%x", pData[0], pData[1]);
			if (pData[0] == KEY_READ)
				callbacks->onKeySetupResult(pData[1], pData[2], pData[3] );
			else if (pData[0] == KEY_VALUE)
				callbacks->onKey(pData[1], -1, -1);
			else if (pData[0] == KEY_SET)
				callbacks->onKeySetupStatus(pData[2] << 8 | pData[1]);
			else if (pData[0] == KEY_TOUCH_KEY)
				callbacks->onKey(pData[1], pData[2], pData[3]);
			else
				LOGE("unhandled key command");
			break;
		case CTRL_VERSION:
			callbacks->onVersion(pData , len);
			break;
			
		case CTRL_POWER:
			callbacks->onPower(pData[0]);
			break;
		
		case CTRL_CANBOX:
			callbacks->onCanInfo(pData , len);
			break;
		case CTRL_MCU_UPGRADE:
			callbacks->onUpgrade(pData[0], pData[1]);
			break;
		default:
			LOGE("unhandled command 0x%x", type);
			break;
		}
		
		return 0;
	}	

	int McuListener::getType(){
		return type;
	}
		
	void McuListener::setCallbacks(struct McuCallbacks *callback){
		callbacks = callback;
	}
	
	Mcu::Mcu(struct McuCallbacks *callbacks) {

 		mMcuServer = NULL;

 		serverDeath = new McuDeathObserver(mMcuServer);

		accListener = new McuListener(CTRL_ACC_STATUS, callbacks);

		devListener =  new McuListener(CTRL_FLAG, callbacks);

		keyListener = new McuListener(CTRL_KEY, callbacks);

		verListener = new McuListener(CTRL_VERSION, callbacks);

		canListener = new McuListener(CTRL_CANBOX, callbacks);

		timeListener = new McuListener(CTRL_TIME, callbacks);

		sleepListener = new McuListener(CTRL_POWER, callbacks);

		upgradepListener = new McuListener(CTRL_MCU_UPGRADE, callbacks);
	 }
	 
	 Mcu::~Mcu(){

	 }

	 void  Mcu::requestMcuVersion(){

	 	if (mMcuServer != NULL){

 			char ctrl = CTRL_VERSION;
 			char data = 0;
 			mMcuServer->writes(ctrl, &data, sizeof(data));
 		} else
 			LOGE("service is NULL");
	 }
	 
		 
	 int Mcu::send(int cmd, char *pBuffer, int len){

		 if (mMcuServer != NULL){

 			int reLen = mMcuServer->writes(cmd, (const char*)pBuffer, len);
		
			return reLen;
 		}
 		
 		LOGE("service is NULL");

		return -1;
	 }

	 int Mcu::open() {

		const String16 name("McuService");

		for (int i=0 ; i< MAX_TIME ; i++) {

		  status_t err = getService(name, &mMcuServer);

		  if (err == NAME_NOT_FOUND) {
				
				LOGE("find Service  failed");
			  usleep(250000);
			  continue;

		  } else if (err != NO_ERROR) {
				LOGE("getService failed %d ", err);
			  return err;
		  }

		  break;
		}

		if (mMcuServer == NULL)
			return NAME_NOT_FOUND;

		mMcuServer->asBinder()->linkToDeath(serverDeath);

		mMcuServer->addListener(devListener);
		mMcuServer->addListener(keyListener);
		mMcuServer->addListener(verListener); //need remove
		mMcuServer->addListener(canListener);
		mMcuServer->addListener(accListener);
		mMcuServer->addListener(timeListener);
		mMcuServer->addListener(sleepListener);
		mMcuServer->addListener(upgradepListener);
		  
		return NO_ERROR;
	 }
	
	 int Mcu::close(){

 	 	if (mMcuServer != NULL){
 			mMcuServer->asBinder()->unlinkToDeath(serverDeath);
 		}
 		
 		LOGE("service is NULL");
 		return 0;
	 }
	 
	int Mcu::startUpgrade(const char* path){

		if (mMcuServer != NULL){

			int ret = mMcuServer->startUpgrade(path);

			return ret;
		}

		LOGE("service is NULL");

		return -1;
	}

	int Mcu::endUpgrade(){
		 if (mMcuServer != NULL){

			int ret = mMcuServer->endUpgrade();

			return ret;
		}

		LOGE("service is NULL");

		return -1;
	}
}
