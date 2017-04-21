#ifndef ANDROID_XX_LISTENER_H
#define ANDROID_XX_LISTENER_H
#include <binder/IInterface.h>

namespace android{
	class IListener: public IInterface{

	public:
		DECLARE_META_INTERFACE(Listener);

		virtual int onRecv(const unsigned char *pData, int len) = 0;

		virtual int getType() = 0;

	}; 
	
	class BnListener: public BnInterface<IListener> {
	public:
    		virtual status_t onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
	};
}
#endif
