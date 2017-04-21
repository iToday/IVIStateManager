#include "McuSerial.h"
#include <stdio.h>
#include "../../include/ilog.h"

namespace android{
	                                                             
	#define SERIAL_READ_BUFFER_MAX  4096
	#define SERIAL_READ_BUFFER_SIZE 256
	
	#define DEBUG_SERIAL_DATA     1
	
	McuPort::McuPort(const char* name): SelectSerialPort(name){
		///dev/ttyMT3
	}
		
 	int McuPort::initPort(){
 				
 		int res = setSpeed(115200);
 		
 		if (res == 0)
 			res = setParity(0, 8, 1, 'n');
 		else
 			LOGI("McuPort::initPort setSpeed failed\n");
 			
 		return res; 		
	}
	
	McuData::McuData(SerialDataDispatch* dispatch){
		
		pDispatch = dispatch;
	}
	
	int McuData::pack(uint8 ctrl, const uint8* data, int len, uint8*& package, int* plen){
		
	  	int package_length = getPackageLength(len);
	  	
	  	package = new uint8[package_length];
	  	
	  	if (package == 0)
	  		return -1;
	  		
	  	memset(package, 0, package_length);
	  		
	   	package[SYNC_POS] = SYNC1;
			package[SYNC_POS + 1] = SYNC2;
	   	package[LENGTH_POS] = len;
	  	package[CTRL_POS] = ctrl;
	  	
	  	if (len > 0)
	  		memcpy(package + DATA_POS, data, len);  
	  		
	  	package[package_length - CHECKSUM_LEN] = getChecksum(package/* + SYNC_LEN*/, package_length - CHECKSUM_LEN /*- SYNC_LEN*/);
	  	
	  	if (plen != 0)
	  		*plen = package_length;
  		
  	return package_length;
  }
  
  int McuData::unpack(const uint8* package, int len){
  	
  	int ctrl = package[CTRL_POS];
  	const uint8* data = package + DATA_POS;
  	int data_len = package[LENGTH_POS];
  
#ifdef 	DEBUG_SERIAL_DATA  	

		LOGI("McuSerial::unpack len:%d \n [",  len);
		
		for (int i = 0; i < len; i ++)
			LOGI(" 0x%x ", package[i]);
			
		LOGI(" ] \n ");
#endif
  	
  	if (pDispatch != 0)
  		return pDispatch->dispatch(ctrl, data, data_len);	
  		
  	return -1;
  } 
  
  int McuData::identifyPackage(const uint8* data, int len, uint8* frame, int* flen){
  	
  	const uint8* pack_first = 0;
  	int pack_count = 0;
  	int totle_pack_count = 0;
  	
  	int checksum = 0;
  	
  	for (int i=0; i<len - 1 && MIN_PACKAGE_LEN <= len - i;) {
  		
		if ((data[i + SYNC_POS] == SYNC1) && (data[i + SYNC_POS + 1] == SYNC2)) {
			
			pack_first = &data[i];
			
			pack_count = pack_first[LENGTH_POS] + SYNC_LEN + LENGTH_LEN + CHECKSUM_LEN + CTRL_LEN;
			
#ifdef DEBUG_SERIAL_DATA
			LOGI("McuSerial::identifyPackage pack_first pos %d  pack_count:%d \n", i, pack_count);
#endif			
										
			if ((len - totle_pack_count) < pack_count){//the length is longer than save_pos
					i++;
				break;
			}

			checksum = getChecksum(pack_first /*+ SYNC_LEN*/, pack_count - CHECKSUM_LEN /*- SYNC_LEN*/);
			
#ifdef DEBUG_SERIAL_DATA				
			LOGI("McuSerial::identifyPackage checksum %x  \n", checksum);
#endif	

			if (checksum == pack_first[pack_count - CHECKSUM_LEN]){
	
				if (frame == NULL){
					unpack(pack_first, pack_count);
				} else if (*flen >= pack_count){
					//frame must larger than pack_count
					memcpy(frame, pack_first, pack_count);
					*flen = pack_count;
				}//else
				//	flen = 0;
				
				i += pack_count;
				totle_pack_count = i;
				continue;
			}		
		}
		i++;
	}
	
	return totle_pack_count;
  }
  
	uint8 McuData::getChecksum(const uint8* data, int len){
	
		if (data != 0 && len > 0){
			int check = 0;
			for (int i=0; i<len; i++)
				check += data[i];
			
			return /*(0 - check)*/check & 0xff ;
		}
		return 0;
	}
	


	McuSerial:: McuSerial(SerialPort* port, SerialData* data , McuUpgradeData* upgradeData, UpgradeListener* listener){
		
		pPort = port;
		pData = data;

		pUpgradeData = upgradeData;
		pListener = listener;

		mUpgrade = false;
	}	
	
	int  McuSerial::init(){
		
		if (pPort != 0){
			
			if (pPort->Open() > 0){
				
				if (pPort->initPort() < 0)
					return -1;
				else
					LOGD("McuSerial::init initPort ok\n");
			}else
				LOGD("McuSerial::init open failed\n");
		}	
		
		if (pthread_create(&read_thread, NULL, read_thread_func, (void *)this) != 0){
			LOGD("create thread failed\n");
		 	return -1;
		}
				
		LOGD("McuSerial::init open success\n");
		
		return 0;
	}
	
	void* McuSerial::read_thread_func(void* args){

		McuSerial* serial = (McuSerial*) args;
		
		if (serial == 0)
			return 0;
			
		while (1){
			if (!serial->threadLoop())
				break;
		}
		
		return 0;
	}

	int  McuSerial::uninit(){
		
		if (pPort != 0){
			pPort->Close();
		}	
		return 0;	
	}
	
	bool McuSerial::threadLoop(){
		
		uint8 data[SERIAL_READ_BUFFER_SIZE] = {0};
		int read_len = 0;
		
		static uint8 serial_buffer[SERIAL_READ_BUFFER_MAX] = {0}; 
		static int buffer_length = 0;
		
		read_len = pPort->Read(data, SERIAL_READ_BUFFER_SIZE);
	
#ifdef 	DEBUG_SERIAL_DATA
 	
		LOGI("McuSerial::threadLoop read package len:%x \n [ ",  read_len);
			
		for (int i = 0; i < read_len; i ++)
			LOGI(" 0x%x ", data[i]);	
			
		LOGI(" ]\n");
#endif
		
		if (read_len > 0){

			if (buffer_length + read_len > SERIAL_READ_BUFFER_MAX || buffer_length < 0)//�������
				buffer_length = 0;

			memcpy(serial_buffer + buffer_length, data, read_len);

			buffer_length += read_len;

			int packages_len = 0;

			if (mUpgrade){
				uint8 frame[256] = {0};
				int size = 256;
				packages_len = pData->identifyPackage(((const uint8*)serial_buffer), buffer_length, frame, &size);

				if (size > 0){
					int ctrl = frame[CTRL_POS];
					int data = frame[DATA_POS];

					if (ctrl == CTRL_UPDATE){
						writeFrame(data == ACK);
					}
				}
			}else{

				packages_len = pData->identifyPackage(((const uint8*)serial_buffer), buffer_length, NULL, 0);
			}

			if (packages_len > 0){
				buffer_length -= packages_len;
				if (buffer_length > 0)
					memcpy(serial_buffer, serial_buffer + packages_len, buffer_length);
			}
		}		
		
		return true;
	}
	
	int McuSerial::startUpgrade(const char* path){

		if (pUpgradeData->load(path) <= 0){

			LOGE("file load failed %s", path);
			return -1;
		}

		uint8 data = 0X52;
		write(CTRL_UPDATE, &data,  sizeof(data));

		mUpgrade = true;

		return writeFrame(false);

	}

	int McuSerial::endUpgrade(){

		mUpgrade = false;

		uint8 head[2];// = {0xfb, 0x04);
		head[0] = 0xFB;
		head[1] = 0x04;

		return pPort->Write(head, sizeof(head)); //write end
	}

	int McuSerial::writeFrame(bool goNext){

		uint8* package = NULL;

		if (goNext)
			package = pUpgradeData->nextFrame();
		else
			package = pUpgradeData->curFrame();

		if (package == NULL){
			LOGI("McuUpgrade Upgrade end!");
			pListener->onProgress(MCU_UPGRADE_END, pUpgradeData->getProgress());
			return -1;
		}

		uint8 head[2];// = {0xfb, 0x01);
		head[0] = 0xFB;
		head[1] = 0x01;

		pPort->Write(head, sizeof(head)); //write head

		int frame = pUpgradeData->getCurFrame();

		uint8 frme[2];
		frme[0] = frame>>8;//page num
		frme[1] = frame;

		pPort->Write(frme, sizeof(frme)); //write frame number

		pPort->Write(package, FRAME_SIZE); //write frame data

		int checkSum = pUpgradeData->getCurFrameCheckSum();

		uint8 check[2];
		check[0] = ((checkSum>>8)&0xFF);
		check[1] = (checkSum & 0xFF);

		pPort->Write(check, sizeof(check)); // write checksum
		LOGI("McuUpgrade frame 0x%x checkSum 0x%x", frame, checkSum );
		pListener->onProgress(MCU_UPGRADE_DOING, pUpgradeData->getProgress());

		return 0;
	}

	int  McuSerial::write(uint8 ctrl, const uint8* data, int len){
		
		fprintf(stderr,"McuSerial write ctrl %d \n", ctrl);
		
		if (pPort == 0 || pData == 0){
				fprintf(stderr,"pPort == 0 || pData == 0 end %d : %d \n", pPort , pData);
				return -1;
		}
				
		uint8* package = 0;
		int res = -1;
		
		int package_len = pData->pack(ctrl, data, len, package, NULL);
		
		if (package_len > 0)
			res = pPort->Write(package, package_len);
			
#ifdef 	DEBUG_SERIAL_DATA 	
		LOGI("McuSerial::write package res:%d \n [", res);
			
		for (int i = 0; i < package_len; i ++)
			LOGI(" 0x%x ", package[i]);	
			
		LOGI(" ]\n");
				
#endif		

		delete[] package;	
		
		return res;
	}
	
			
}
