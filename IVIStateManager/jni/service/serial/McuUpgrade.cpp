#include "McuSerial.h"
#include <stdio.h>
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>

#define TAG "McuUpgrade"

#include "../../include/ilog.h"

namespace android{
	                                                             
	#define SERIAL_READ_BUFFER_MAX  4096
	#define SERIAL_READ_BUFFER_SIZE 256
	
	#define DEBUG_SERIAL_DATA     1

	McuUpgradeData::McuUpgradeData(){
		mBuffer = NULL;
		mBufferSize = 0;
		mPos = 0;
	}

	McuUpgradeData::~McuUpgradeData(){

		if (mBuffer != NULL)
			delete [] mBuffer;

		mBuffer = NULL;
		mPos = 0;
	}

	int McuUpgradeData::load(const char* path){

		if (path == NULL) return -1;

		int fd = open(path, O_RDONLY);

		if (fd >= 0){

			int length = lseek(fd,0L,SEEK_END);

			mBuffer = new uint8[length]{0};

			lseek(fd,0,SEEK_SET);

			int ret = read(fd, mBuffer, length);

			close(fd);

			if (ret != length){
				LOGE("read error, file size %d, read ret %d\n", length, ret);

				return -1;
			}

			mBufferSize = length;

			return mBufferSize;
		}

		return -1;
	}

	bool McuUpgradeData::isEnd(){
		return mPos >= (mBufferSize - 1);
	}

	int McuUpgradeData::getProgress(){
		return ((mPos + 1) * 100.0f) / mBufferSize;
	}

	uint8* McuUpgradeData::curFrame(){

		return mBuffer + mPos;
	}

	uint8* McuUpgradeData::nextFrame(){

		if (isEnd())
			return NULL;

		mPos += FRAME_SIZE;

		if (isEnd())
			return NULL;

		return mBuffer + mPos;
	}

	uint8* McuUpgradeData::findFirstFrame(){
		mPos = 0;
		return mBuffer;
	}

	int McuUpgradeData::getCurFrame(){
		return mPos / FRAME_SIZE;
	}

	int McuUpgradeData::getCurFrameCheckSum(){

		int checkSum = 0;

		for (int index = 0; index < FRAME_SIZE; index ++){
			checkSum += (mBuffer [mPos + index] & 0xff);
		}

		return checkSum;
	}

}
