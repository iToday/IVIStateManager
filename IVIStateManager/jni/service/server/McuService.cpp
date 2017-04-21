#include "McuService.h"
#include <stdlib.h>
#include <stdio.h>
#include <sys/sysinfo.h>
#include <errno.h>
#include <time.h>
#include <utils/Mutex.h>
#include <utils/Condition.h>
#include <utils/Errors.h>
#include <utils/Mutex.h>
#include <utils/RWLock.h>
#include <utils/Thread.h>
#include <fcntl.h>

#define TAG "McuService"

#include "../../include/ilog.h"

namespace android {

	char* McuService::mDevName = NULL;
		
	#define DEFAULT_PORT "/dev/ttyFIQ0" ///dev/ttyS0
	
		
	McuService::McuService(){
		
		if (mDevName == NULL){
			setDevice(DEFAULT_PORT);
		}
		
		mListeners = new ListenerList;
		
		LOGI("McuService serial port : %s  \n", mDevName);

		McuPort* port = new McuPort(mDevName);
			
		mMcuSerial = new McuSerial(port, new McuData(this), new McuUpgradeData(), this);
		
		mMcuSerial->init();

		mNetLink = new Netlink(this);
		
		mNetLink->Open(25);

		if (pthread_create(&mHeartbeatThread, NULL, heart_beat_thread_func, (void *)this) != 0){
			LOGI("McuService create heart beat thread failed  \n");
		}
		
	}

	McuService::~McuService(){
		
		if (mMcuSerial != NULL){

			mMcuSerial->uninit();

			delete mMcuSerial;

			mMcuSerial = NULL;
		}

		if (mNetLink != NULL){
			mNetLink->Close();
			delete mNetLink;

			mNetLink = NULL;
		}


		if(mListeners != NULL){

			delete mListeners;

			mListeners = NULL;
		}
		
		if (mDevName != NULL){
				delete [] mDevName;
				
				mDevName = NULL;
		}
				
	}
	
	void McuService::setDevice(const char* path){
				
		if (mDevName != NULL)
			delete [] mDevName;
			
		mDevName = new char[strlen(path) + 1]{0};

		memcpy(mDevName , path, strlen(path) );
	}
	
	int McuService::onNewData(unsigned char* buffer, int len){

		Listener *lis;

		LOGI("McuService::onNewData cmd %d ", buffer[0]);

		if((lis = mListeners->find(buffer[0])) != NULL){

			if(lis->mListener != NULL){
				// ctrl + len size is 2 byte
				lis->mListener->onRecv(buffer + 2, len - 2);
			}
		}

		return 0;
	}

	/**
	 * mcu upgrade progress
	 */
	int McuService::onProgress(int status, int progress){

		uint8 buffer[2] = {status, progress};

		return dispatch(CTRL_MCU_UPGRADE, buffer, sizeof(buffer));
	}

	void* McuService::heart_beat_thread_func(void* args){
		
		McuService* service = (McuService*) args;
		
		fprintf(stderr,"McuService heart_beat_thread_func %d  \n", service);
		
		if (service == NULL)
			return 0;
			
		while (1){
			
			service->sendHeartbeat();				
			sleep(1);
		}
		
		return 0;
	}
	
	/**
	 * 心跳信息，用来复位异常的ARM
	 */
	void McuService::sendHeartbeat(){
		uint8 ctrl = 0x0B;
		char data = 0;
		//writes(ctrl, &data, 1);
	}
	
	int McuService::dispatch(const unsigned char* data, int len){

		Listener *lis;
		
		if((lis = mListeners->find(data[0])) != NULL){
					
			if(lis->mListener != NULL){
				lis->mListener->onRecv(data,len);
			}
		}
	
		return 0;
	}
	
	//mcuserial data
	int McuService::dispatch(uint8 ctrl, const uint8* data, int len){
		
		Listener *lis;
					
		if((lis = mListeners->find(ctrl)) != NULL){
				
			if(lis->mListener != NULL){
				
				lis->mListener->onRecv(data ,len);
				return 0;
			}								
		}
		
		return -1;
	}


	int McuService::writes(int ctrl, const char* data, int len){

		if (mMcuSerial != NULL){
			return mMcuSerial->write(ctrl, (const uint8*)data, len);
		}
		
		return -1;

	}

	int McuService::addListener(const sp<IListener>& listener){

		int type = 0;
		Listener *lis;
		
		if (listener != NULL){
			
			type = listener->getType();

			lis = mListeners->find(type);
			
			if(lis != NULL){
				
				if(lis->mListener != NULL)
					lis->mListener->asBinder(lis->mListener)->unlinkToDeath(lis->mDeathObserver);
					
				listener->asBinder(listener)->linkToDeath(lis->mDeathObserver);
				lis->mListener = listener;
				
			}else {
				
				Listener *temp = new Listener(listener,type);
				mListeners->insert(temp);
			}
		}
		return 0;
	}

	int McuService::startUpgrade(const char* path){
		return mMcuSerial->startUpgrade(path);
	}

	int McuService::endUpgrade(){
		return mMcuSerial->endUpgrade();
	}

}
