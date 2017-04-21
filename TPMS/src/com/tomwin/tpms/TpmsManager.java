package com.tomwin.tpms;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.tomwin.hardware.SerialPort;
import com.tomwin.hardware.SerialPort.Listener;

public class TpmsManager implements Listener, ITpmsListener{
	
	private static final String tag = "TpmsManager";
	
	private static final int MSG_ALARM = 0x01;

	private SerialPort mPort;
	private Parser mParser;
	
	private TyresManager mManager;
	
	private Handler mHandle;
	
	private RemoteCallbackList<IRemoteListener> mListeners = new RemoteCallbackList<IRemoteListener>();
	
	public TpmsManager(Context context, Handler handle){
		
		IVIDataManager.setup(context);
		
		mHandle = handle;
		
		mManager = new TyresManager();
		mParser = new Parser(mManager, this);
		
		mPort = new SerialPort("/dev/ttyMT5");//ttyS5
		mPort.open(19200);
		mPort.setListener(this);
	}
	
	public boolean isHaveWarning(){
		return mManager.isHaveWarning();
	}

	@Override
	public void onNewData(byte[] buffer) {
		
		if (mParser != null)
			mParser.parse(buffer);
	}
	
	private void write(byte[] buffer){
		mPort.write(buffer, buffer.length);
	}
	
	public void queryId(){
		write(mParser.getQueryIdPack());
	}
	
	public void switchPos(int index){
		write(mParser.getSwitchPack(index));
	}
	
	public void queryBattery(){
		write(mParser.getQueryBatteryPack());
	}
	
	public void stopPair(){
		write(mParser.getStopPairPack());
	}
	
	public void pair(int index){
		write(mParser.getPairPack(index));
	}

	@Override
	public void onStateChange(Tyres tyres) {

		Message msg = mHandle.obtainMessage(MSG_ALARM);
		msg.obj = tyres;
		mHandle.sendMessage(msg);
		
		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onStateChange(tyres);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
		
	}

	@Override
	public void onId(Tyres tyres) {

		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onId(tyres);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
		
	}

	@Override
	public void onBattery(Tyres tyres) {

		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onBattery(tyres);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
		
	}

	@Override
	public void onPairState(int index, int state) {
		
		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onPairState(index, state);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
	}

	@Override
	public void onSwitch(int index) {
		
		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onSwitch(index);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
	}

	public void registerListener(IRemoteListener listener) {
		mListeners.register(listener);
	}

	public void unregisterListener(IRemoteListener listener) {
		mListeners.unregister(listener);
	}

	public void requestTyres() {
		
		Log.d(tag, "requestTyres");
		
		for (Tyres tyres : mManager.getTyres()){
			
			int i = mListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mListeners.getBroadcastItem(i).onStateChange(tyres);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mListeners.finishBroadcast();
		}
		
	}
}
