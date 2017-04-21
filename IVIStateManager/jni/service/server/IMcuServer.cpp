#include <stdint.h>
#include <sys/types.h>

#include <utils/Errors.h>
#include <utils/RefBase.h>

#include <binder/Parcel.h>
#include <binder/IInterface.h>

#include "IMcuServer.h"


namespace android {
// ----------------------------------------------------------------------------
enum{
    ADD_LISTENER = IBinder::FIRST_CALL_TRANSACTION,
    WRITE_DATA,
    START_UPGRADE,
    END_UPGRADE
};

class BpMcuServer : public BpInterface<IMcuServer>{

public:

	BpMcuServer(const sp<IBinder>& impl)
	: BpInterface<IMcuServer>(impl)
	{

	}


	virtual int addListener(const sp<IListener>& listener){

		Parcel data, reply;

		data.writeInterfaceToken(IMcuServer::getInterfaceDescriptor());
		data.writeStrongBinder(listener->asBinder(listener));

		remote()->transact(ADD_LISTENER, data, &reply);

		return reply.readInt32();
	}


	virtual int writes(int ctrl, const char* pData, int len){
		Parcel data, reply; 

		data.writeInterfaceToken(IMcuServer::getInterfaceDescriptor());
		data.writeInt32(ctrl);
		data.writeInt32(len);
		if(pData){
			data.write(pData,len);
		}

	   remote()->transact(WRITE_DATA, data, &reply);

	   return reply.readInt32();
	}

	virtual int startUpgrade(const char* path){
		Parcel data, reply;

		data.writeInterfaceToken(IMcuServer::getInterfaceDescriptor());
		int len = strlen(path);
		data.writeInt32(len);
		data.write(path,len);

	   remote()->transact(START_UPGRADE, data, &reply);

	   return reply.readInt32();
	}

	virtual int endUpgrade(){
		Parcel data, reply;

		data.writeInterfaceToken(IMcuServer::getInterfaceDescriptor());

	    remote()->transact(END_UPGRADE, data, &reply);

	    return reply.readInt32();
	}

};

IMPLEMENT_META_INTERFACE(McuServer, "android.today.McuServer");


// ----------------------------------------------------------------------

status_t BnMcuServer::onTransact(uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags)
{

    switch(code) {
    case ADD_LISTENER:{
		CHECK_INTERFACE(IMcuServer, data, reply);

		sp<IListener> listener = interface_cast<IListener>(data.readStrongBinder());

		reply->writeInt32(addListener(listener));

		return NO_ERROR;
	}break;
  
    case WRITE_DATA:{

		CHECK_INTERFACE(IMcuServer, data, reply);
		int ctrl = data.readInt32();
		int len = data.readInt32();
		int *pData = NULL;
		if (data.dataAvail() > 0) {
			pData = new int[len];
			data.read(pData,len);
		}

		reply->writeInt32(writes(ctrl, (const char*)pData, len));

		if(pData)
			delete [] pData;

		return NO_ERROR;
	}break;
    case START_UPGRADE:{
    	CHECK_INTERFACE(IMcuServer, data, reply);
    	int len = data.readInt32();

    	int* path = NULL;

    	if (data.dataAvail() > 0){
    		path = new int[len + 1]{0};
    		data.read(path, len);
    	}

    	reply->writeInt32(startUpgrade((const char*)path));

    	return NO_ERROR;
    }break;
    case END_UPGRADE:{
    	CHECK_INTERFACE(IMcuServer, data, reply);
    	reply->writeInt32(endUpgrade());
    	return NO_ERROR;
    }break;
    }

    return BBinder::onTransact(code, data, reply, flags);
}

}
