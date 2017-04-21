/********************************************************************************
*the serial port operator  
*read with select
*
*\author: iToday
*\date:		2014-8-5 21:33
*\email:	xubaoan@yftech.com
*\version:1.0
********************************************************************************
*/

#ifndef ANDROID_SELECT_SERIAL_H
#define ANDROID_SELECT_SERIAL_H
#include "Serial.h"
namespace android{
	class SelectSerialPort: public SerialPort{
		private:
			fd_set rd;
		public:
			SelectSerialPort(const char* name);
			virtual ~SelectSerialPort();
			virtual int Open();
			virtual int Read(uint8* recv, int len);
	};
}
#endif