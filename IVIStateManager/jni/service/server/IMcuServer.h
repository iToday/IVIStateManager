#ifndef ANDROID_XXSERVER_H
#define ANDROID_XXSERVER_H

#include <stdint.h>
#include <sys/types.h>
#include <utils/Errors.h>
#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/BinderService.h>
#include "../listener/IListener.h"


namespace android {

	class IMcuServer : public IInterface {

	public:
		DECLARE_META_INTERFACE(McuServer);

		virtual int addListener(const sp<IListener>& listener) = 0;

		virtual int writes(int ctrl, const char* pData, int len) = 0;

		virtual int startUpgrade(const char* path) = 0;

		virtual int endUpgrade() = 0;
	};


	class BnMcuServer : public BnInterface<IMcuServer> {

	public:
	virtual status_t onTransact( uint32_t code,
					const Parcel& data,
					Parcel* reply,
					uint32_t flags = 0);
	};

}

#endif
