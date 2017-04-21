#ifndef ANDROID_XX_SERVICE_H
#define ANDROID_XX_SERVICE_H

#include <termios.h>  
#include <string.h>
#include <fcntl.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <unistd.h>
#include "../serial/McuSerial.h"
#include "../netlink/Netlink.h"
#include "IMcuServer.h"
#include "ListenerList.h"

//#pragma pack(8)

namespace android {

	class McuService :
			public BinderService<McuService>,
			public BnMcuServer,
			public SerialDataDispatch,
			public UpgradeListener,
			public NetlinkListener
	{
		friend class BinderService<McuService>;

	public:
			McuService();

			~McuService();

			static char const* getServiceName() { return "McuService"; }
			
			static void setDevice(const char* path);
			
	private:

		McuSerial    *mMcuSerial;

		Netlink		*mNetLink;

		ListenerList *mListeners;
		
		pthread_t 	 mHeartbeatThread;
		
		static char*  mDevName;	
		
		static void* heart_beat_thread_func(void* args);
		
		void sendHeartbeat();

		int startUpgrade(const char* path);

		int endUpgrade();

		virtual int  dispatch(uint8 ctrl, const uint8* data, int len);

		virtual int  dispatch(const unsigned char* data, int len);

		virtual int  addListener(const sp<IListener>& listener);

		virtual int  writes(int ctrl, const char* data, int len);

		virtual int onProgress(int status, int progress);

		//net link
		virtual int onNewData(unsigned char* buffer, int len);

	};

}
#endif
