/********************************************************************************
*the mcu 's upgrade
*
*
*\author: iToday
*\date:		2015-9-9 21:33
*\email:	xba1987@126.com
*\version:1.0
********************************************************************************
*/


#ifndef ANDROID_MCU_UPGRADE_H
#define ANDROID_MCU_UPGRADE_H

#include "../serial/SelectSerial.h"
#include "../../include/protocol.h"
#include "McuSerial.h"
#include <pthread.h>

#define TAG "McuUpgrade"

namespace android{

#define FILE_PATH_LENGTH 512

#define MCU_UPGRADE_START  1
#define MCU_UPGRADE_DOING  2
#define MCU_UPGRADE_END	   3

#define ACK 0x06
#define NCK 0x15


#define FRAME_SIZE	128

	class UpgradeListener{
		public:
		virtual ~UpgradeListener(){}

		virtual int onProgress(int status, int progress) = 0;
	};

	class McuUpgradeData{
	private:
		uint8* mBuffer;

		int mBufferSize;

		int mPos;
	public:
		McuUpgradeData();
		~McuUpgradeData();

		int load(const char* path);

		bool isEnd();

		int getProgress();

		int getCurFrame();

		int getCurFrameCheckSum();

		uint8* nextFrame();
		uint8* curFrame();
		uint8* findFirstFrame();

	};
	
}
#endif
