#include "SelectSerial.h"
namespace android{
SelectSerialPort::SelectSerialPort(const char* name): SerialPort(name){
	
}

SelectSerialPort::~SelectSerialPort(){
	
}

int SelectSerialPort::Open(){
	
	int _fd = SerialPort::Open();
		
	FD_ZERO(&rd);
  	FD_SET(fd,&rd);	
  
  	return _fd;
}

int SelectSerialPort::Read(uint8* recv, int len){
	
	FD_ZERO(&rd);
  	FD_SET(fd,&rd);
  
  	fprintf(stderr,"SelectSerialPort select before %d \n", fd);
  	
	if (select(fd + 1,&rd ,NULL ,NULL ,NULL) < 0){
		fprintf(stderr," select error %d \n", fd);
		return -1;
	}
	
	 fprintf(stderr,"SelectSerialPort select end %d \n", fd);
	 
	if (FD_ISSET(fd,&rd))
		return SerialPort::Read(recv, len);
			
	return -1;
}
}
