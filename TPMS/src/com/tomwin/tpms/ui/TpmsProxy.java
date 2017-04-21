package com.tomwin.tpms.ui;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.tomwin.tpms.IRemoteListener;
import com.tomwin.tpms.ITpms;
import com.tomwin.tpms.TpmsService;
import com.tomwin.tpms.Tyres;
import com.tomwin.tpms.TyresManager;

public class TpmsProxy {
	
	private static final String tag = "TpmsProxy";
	
	
	public static final int LEFT_FRONT_INDEX = TyresManager.LEFT_FRONT_INDEX;
	
	public static final int RIGHT_FRONT_INDEX = TyresManager.RIGHT_FRONT_INDEX;
	
	public static final int LEFT_REAR_INDEX = TyresManager.LEFT_REAR_INDEX;
	
	public static final int RIGHT_REAR_INDEX = TyresManager.RIGHT_REAR_INDEX;
	
	//switch
	public static final int LEFT_FRONT_RIGHT_FRONT = TyresManager.LEFT_FRONT_RIGHT_FRONT;
	
	public static final int LEFT_FRONT_LEFT_REAR = TyresManager.LEFT_FRONT_LEFT_REAR;
	
	public static final int LEFT_FRONT_RIGHT_REAR = TyresManager.LEFT_FRONT_RIGHT_REAR;
	
	public static final int RIGHT_FRONT_LEFT_REAR = TyresManager.RIGHT_FRONT_LEFT_REAR;
	
	public static final int RIGHT_FRONT_RIGHT_REAR = TyresManager.RIGHT_FRONT_RIGHT_REAR;
	
	public static final int LEFT_REAR_RIGHT_REAR = TyresManager.LEFT_REAR_RIGHT_REAR;
	
	private ArrayList<OnTpmsListener> mListeners = new ArrayList<OnTpmsListener>();
	
	private ITpms mBinder = null;
	
	private static TpmsProxy instance;
	
	private Handler mHandler = new Handler();
	
	private Context mContext;
	
	private ServiceConnection conn = new  ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			mBinder = ITpms.Stub.asInterface(arg1);
			
			try {
				mBinder.registerListener(mRemoteListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					for (OnTpmsListener listener: mListeners)
						listener.onActive(true);
				}
				
			});
			
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBinder = null;
			
			for (OnTpmsListener listener: mListeners)
				listener.onActive(false);
		}
		
	};
	
	private IRemoteListener.Stub mRemoteListener = new IRemoteListener.Stub() {
		
		@Override
		public void onSwitch(final int index) throws RemoteException {
			
			Log.d(tag, "onSwitch " + index);
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					for (OnTpmsListener listener: mListeners)
						listener.onSwitch(index);
				}
			});
			
		}
		
		@Override
		public void onStateChange(final Tyres tyres) throws RemoteException {
			
			Log.d(tag, "onStateChange " + tyres);
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					for (OnTpmsListener listener: mListeners)
						listener.onStateChange(tyres);
				}
				
			});
		
		}
		
		@Override
		public void onPairState(final int index, final int state) throws RemoteException {
			
			Log.d(tag, "onPairState index :" + index + " state :" + state);
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					for (OnTpmsListener listener: mListeners)
						listener.onPairState(index, state);
				}
				
			});
		}
		
		@Override
		public void onId(final Tyres tyres) throws RemoteException {
			
			Log.d(tag, "onId " + tyres);
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					for (OnTpmsListener listener: mListeners)
						listener.onId(tyres);
				}
				
			});
			
		}
		
		@Override
		public void onBattery(final Tyres tyres) throws RemoteException {
			
			Log.d(tag, "onBattery " + tyres);
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					for (OnTpmsListener listener: mListeners)
						listener.onBattery(tyres);
				}
				
			});
			
		}
	};

	private TpmsProxy(Context context){
		
		Intent intent = new Intent(context, TpmsService.class);
		context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
		mContext = context;
	}
	
	public static TpmsProxy setUp(Context context){
		
		if (instance == null)
			instance = new TpmsProxy(context);
		
		return instance;
	}
	
	public static TpmsProxy getInstance(){
		
		if (instance != null)
			return instance;
		
		throw new NullPointerException("Context is null, you must call setUp(), before getInstance()");
	}
	
	public void release(){
		try {
			mContext.unbindService(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void registerListener(OnTpmsListener listener){
		mListeners.remove(listener);
		mListeners.add(listener);
	}
	
	public void unregisterListener(OnTpmsListener listener){
		mListeners.remove(listener);
	}

	public void queryId(){
		if (isActive()){
			try {
				mBinder.queryId();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else 
			Log.d(tag, "service not bind");
	}
	
	public void requestTyres(){
		if (isActive()){
			try {
				mBinder.requestTyres();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else 
			Log.d(tag, "service not bind");
	}
	
	public void switchPos(int index){
		if (isActive()){
			try {
				mBinder.switchPos(index);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else 
			Log.d(tag, "service not bind");
	}
	
	public void queryBattery(){
		if (isActive()){
			try {
				mBinder.queryBattery();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else 
			Log.d(tag, "service not bind");
	}
	
	public void stopPair(){
		if (isActive()){
			try {
				mBinder.stopPair();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else 
			Log.d(tag, "service not bind");
	}
	
	public void pair(int index){
		if (isActive()){
			try {
				mBinder.pair(index);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else 
			Log.d(tag, "service not bind");
	}
	
	public boolean isActive(){
		
		return mBinder != null;
	}
	
	public interface OnTpmsListener{
		void onActive(boolean active);
		void onStateChange(Tyres tyres);
		void onId(Tyres tyres);
		void onBattery(Tyres tyres);
		void onPairState(int index, int state);
		void onSwitch(int index);
	}
}
