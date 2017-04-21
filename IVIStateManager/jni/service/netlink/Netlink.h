#ifndef ANDROID_NETLINK_H
#define ANDROID_NETLINK_H

#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <string.h>
#include <asm/types.h>
#include <linux/netlink.h>
#include <linux/socket.h>
#include <errno.h>

namespace android{

	class NetlinkListener{
	public:
		virtual ~NetlinkListener(){}
		virtual int onNewData(unsigned char* buffer, int len) = 0;
	};

	class Netlink{

	private:
		NetlinkListener* mListener;

		int mSocketFd;

		pthread_t 	 mReadThread;

	public:
		Netlink(NetlinkListener * listener);

		int Open(int channel);

		int Send(unsigned char* buffer, int len);

		int Recv(unsigned char* buffer, int len);

		int Close();

	private:
		static void* read_thread_func(void* args);

	}; 
	
}
#endif
