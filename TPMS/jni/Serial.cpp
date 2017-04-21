#include "Serial.h"

namespace android{

	//#define EXTPROC	0200000

	SerialPort::SerialPort(const char* name){
		
		pthread_mutex_init(&writeMutex,NULL);
		
		if (name != 0){
			spName = new char[strlen(name) + 1];
			memcpy(spName, name, strlen(name));
		}
		
		fd = -1;
	}
	
	SerialPort::~SerialPort(){

		if (spName != 0){
			delete[] spName;
			spName = 0;
		}
		
		Close();
		
		pthread_mutex_destroy(&writeMutex);
		
	}
	
	const char* SerialPort::getName(){
		
		return spName;
	}
	
	int SerialPort::Open(){
		
		if (fd != -1){
			Close();
		}
		
		fd = open(spName, O_RDWR|O_NDELAY|O_NOCTTY);
		
		return fd;
	}
	
	int SerialPort::Close(){
		
		if (fd > 0){
			close(fd);
			fd = -1;
		}
		
		return 0;
	}
	
	int SerialPort::Read(uint8* recv, int len){
		
		if (fd > 0 && recv != 0)
			return read(fd, recv, len);
			
		return -1;
	}
	
	int SerialPort::Write(const uint8* send, int len){
		
		if (fd > 0 && send != 0){
			
			int writed_len = 0;
			
			pthread_mutex_lock(&writeMutex);
			
			do {
				
				writed_len = write(fd, send, len);
				
			} while (writed_len != len);
			
			pthread_mutex_unlock(&writeMutex);
			
			return writed_len;
		} 
		
			fprintf(stderr,"fd error %d data size %d\n", fd, send);
		
		return -1;
	}
			
	int SerialPort::setParity(int flow_ctrl, int databits, int stopbits, int parity){
		
		struct termios options; 
		
		if( tcgetattr( fd,&options)  !=  0) { 
			perror("SetupSerial 1");     
			return -1;  
		}
		
		options.c_cflag &= ~CSIZE; 
		
		switch (databits){   
		case 7:		
			options.c_cflag |= CS7; 
			break;
		case 8:     
			options.c_cflag |= CS8;
			break;   
		default:    
			fprintf(stderr,"Unsupported data size\n"); return -1;  
		}
		
		switch (parity){   
			case 'n':
			case 'N':    
				options.c_cflag &= ~PARENB;
				options.c_iflag &= ~INPCK;
				break;  
			case 'o':   
			case 'O':     
				options.c_cflag |= (PARODD | PARENB);
				options.c_iflag |= INPCK;
				break;  
			case 'e':  
			case 'E':   
				options.c_cflag |= PARENB;
				options.c_cflag &= ~PARODD;
				options.c_iflag |= INPCK;
				break;
			case 'S': 
			case 's':  /*as no parity*/   
			  options.c_cflag &= ~PARENB;
				options.c_cflag &= ~CSTOPB;break;  
			default:   
				fprintf(stderr,"Unsupported parity\n");    
				return -1;  
			}  

			switch (stopbits){   
				case 1:    
					options.c_cflag &= ~CSTOPB;  
					break;  
				case 2:    
					options.c_cflag |= CSTOPB;  
				   break;
				default:    
					 fprintf(stderr,"Unsupported stop bits\n");  
					 return -1; 
			} 
			

		options.c_lflag &= ~(ECHO | ECHOE | ICANON|ISIG);

	    switch(flow_ctrl){      
	       case 0 :
	              options.c_cflag &= ~CRTSCTS;
	              break;
	       case 1 :
	              options.c_cflag |= CRTSCTS;
	              break;
	       case 2 :
	              options.c_cflag |= IXON | IXOFF | IXANY;
	              break;
	    }
		/* Set input parity option */ 
		if (parity != 'n')
			options.c_iflag |= INPCK;


		options.c_iflag &= ~(INLCR|ICRNL);

		options.c_iflag &= ~(IXON);

		options.c_iflag &= ~(INLCR | ICRNL | IGNCR);
		options.c_oflag &= ~(ONLCR | OCRNL | ONOCR | ONLRET);
			

		tcflush(fd,TCIFLUSH);

		if (tcsetattr(fd,TCSANOW,&options) != 0){
			return -1;
		}
		return 0;
	}
	
	int SerialPort::setSpeed(int speed){
		int   i; 
	  int   status; 
	  struct termios   Opt;
	  
	  const int speed_arr[] = { B38400, B19200, B9600, B4800, B2400, B1200, B300,
	          B38400, B19200, B9600, B4800, B2400, B1200, B300,B115200 };
	          
	  const int name_arr[] = {38400,  19200,  9600,  4800,  2400,  1200,  300,
			  38400, 19200,  9600, 4800, 2400, 1200,  300, 115200};
	          
	   if (fd == -1)
	   	return -1;
	   	       
	  tcgetattr(fd, &Opt); 
	  
	  for( i= 0;  i < sizeof(speed_arr) / sizeof(int); i++){ 
	  	
	    if(speed == name_arr[i]){   
	    	  
	      tcflush(fd,TCIOFLUSH);   
	      cfsetispeed(&Opt, speed_arr[i]);  
	      cfsetospeed(&Opt, speed_arr[i]);   
	      Opt.c_cflag |= (CLOCAL | CREAD);
	      status = tcsetattr(fd, TCSANOW, &Opt);
	        
	      if  (status != 0){        
	        return -1;     
	      } 
	      
	      tcflush(fd,TCIOFLUSH);   
	    }    
	  }

	  return 0;
	}
}
