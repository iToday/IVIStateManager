/********************************************************************************
*the serial port operator and data interface
*
*
*\author: iToday
*\date:		2014-8-5 21:33
*\email:	xubaoan@yftech.com
*\version:1.0
********************************************************************************
*/
#ifndef ANDROID_SERIAL_H
#define ANDROID_SERIAL_H
#include <termios.h>  
#include <string.h>
#include <fcntl.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>

namespace android{
	
	typedef unsigned char uint8;
	typedef unsigned short int uint16;
	typedef unsigned int uint32;
	
	class SerialPort {
		protected:
			int fd;
			char* spName;
			
			pthread_mutex_t writeMutex ;
			
		public:
			SerialPort(const char* name);
			virtual ~SerialPort();
			
			const char* getName();
			
			virtual int Open();
			virtual int Close();
			
			virtual int Read(uint8* recv, int len);
			virtual int Write(const uint8* send, int len);
			
			virtual int setParity(int flow_ctrl, int databits, int stopbits, int parity);
			virtual int setSpeed(int speed);
			
			virtual int initPort() = 0;
	};
	
	class SerialDataDispatch{
		public:
		virtual ~SerialDataDispatch(){}

		virtual int dispatch(uint8 ctrl, const uint8* data, int len) = 0;
	};
	
	class SerialData {
		
		public:
			virtual ~SerialData(){}

			virtual int pack(uint8 ctrl, const uint8* data, int len, uint8*& package, int* plen) = 0;

			virtual int unpack(const uint8* package, int len) = 0; 
			
			virtual int identifyPackage(const uint8* data, int len, uint8* frame, int* flen) = 0;
	};
	
}
#endif
