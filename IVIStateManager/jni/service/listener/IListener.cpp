#include "IListener.h"
#include <binder/Parcel.h>
namespace android{

	enum{
		DEVICE_RECV_DATA = IBinder::FIRST_CALL_TRANSACTION,
		DEVICE_TYPE,
	};
	
	class BpListener: public BpInterface<IListener>{
	public:
		BpListener(const sp<IBinder>& impl)
			:BpInterface<IListener>(impl){
    	
	  }

    virtual int onRecv(const  unsigned char *pdata, int len){
    	
    	Parcel data, reply; 
    	
    	data.writeInterfaceToken(IListener::getInterfaceDescriptor());      
        data.writeInt32(len);
      
        if(pdata != NULL)
     		data.write(pdata,len);
     		
        remote()->transact(DEVICE_RECV_DATA, data, &reply);
      
        return reply.readInt32();
  
    }
	  
    virtual int getType(){
    	
    	Parcel data, reply;
			
    	data.writeInterfaceToken(IListener::getInterfaceDescriptor());   
        remote()->transact(DEVICE_TYPE, data, &reply);
      
        return reply.readInt32();
    }
};
	
	
IMPLEMENT_META_INTERFACE(Listener, "android.today.Listener");

status_t BnListener::onTransact(uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags){

   switch(code){
   	
   	case DEVICE_RECV_DATA:{

   	CHECK_INTERFACE(IListener, data, reply);
   		
    	int len = data.readInt32();
      	int *pData = NULL;

    	if (data.dataAvail() > 0) {
    		pData = new int[len];
    		data.read(pData,len); 
    	}
    	
    	reply->writeInt32(onRecv((const unsigned char*)pData, len));
    	
    	delete [] pData;
    	
   		return NO_ERROR;
   	}
	break;
   		
    case DEVICE_TYPE:{
    	
    	CHECK_INTERFACE(IListener, data, reply);
    	
    	reply->writeInt32(getType());
    	return NO_ERROR;
    }
  	break;
  }

  return BBinder::onTransact(code, data, reply, flags);                                   
}
	
	
	
}
