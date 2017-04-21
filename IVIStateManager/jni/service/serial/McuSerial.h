/********************************************************************************
*the mcu 's serial operator and protocol
*
*
*\author: iToday
*\date:		2015-9-9 21:33
*\email:	xubaoan@yftech.com
*\version:1.0
********************************************************************************
*/


#ifndef ANDROID_MCU_H
#define ANDROID_MCU_H

#include "../serial/SelectSerial.h"
#include "../../include/protocol.h"
#include "McuUpgrade.h"
#include <pthread.h>

#define TAG "Mcu"

namespace android{
					
	
	class McuPort: public SelectSerialPort{	
	public:
		McuPort(const char* name);
		virtual ~McuPort(){}
		
		virtual int initPort();
			
	};
	
	class McuData: public SerialData{		
			
			static const uint8 MIN_PACKAGE_LEN = SYNC_LEN + LENGTH_LEN + CHECKSUM_LEN + CTRL_LEN;
			
		private:
			SerialDataDispatch* pDispatch;
			
		public:
			
			McuData(SerialDataDispatch* dispatch);
			
			virtual ~McuData(){}
			
			void setDispatch(SerialDataDispatch* dispatch){
				pDispatch = dispatch;
			}
						
			virtual int pack(uint8 ctrl, const uint8* data, int len, uint8*& package, int* plen);
			
			
			virtual int unpack(const uint8* data, int len); 
			
			
			virtual int identifyPackage(const uint8* data, int len, uint8* frame, int* flen);
			
		private:			
			uint8 getChecksum(const uint8* data, int len);
			
			inline int getPackageLength(int dataLen){
				return dataLen + SYNC_LEN + LENGTH_LEN + CHECKSUM_LEN + CTRL_LEN;
			}
	};

	class McuSerial{
		private:			
			SerialPort*  pPort;
			SerialData*  pData;
			
			McuUpgradeData* pUpgradeData;

			UpgradeListener* pListener;

			pthread_t read_thread;

			bool mUpgrade;
		
		public:
			McuSerial(SerialPort* port, SerialData* data, McuUpgradeData* upgradeData, UpgradeListener* listener);
			virtual ~McuSerial(){}
			
			static void* read_thread_func(void* args);
			// Thread interface
    		bool threadLoop();
				
			int init();
			
			int uninit();
						
			int write(uint8 ctrl, const uint8* data, int len);
			
			int startUpgrade(const char* path);
			int endUpgrade();

			int writeFrame(bool goNext);
	};
	
	
}
#endif
