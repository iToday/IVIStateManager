package com.itoday.ivi.sdk;

import java.util.ArrayList;

import com.itoday.ivi.IKeyListener;
import com.itoday.ivi.IUpgradeListener;
import com.itoday.ivi.IVehicle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import com.itoday.ivi.platform.*;

/**
 * 车机基本功能接口
 * @author iToday
 *
 */
public class IVIManager {
	
	private static final String SERVICE_ACTION = "android.intent.action.IVIMainService";
	
	private Context mContext;
	
	private IVehicle mVehicle;
	
	private OnActiveListener mActiveListener;
	
	private ArrayList<OnKeySetupListener> mKeySetupListener = new ArrayList<OnKeySetupListener>();
	
	private ArrayList<OnUpgradeListener> mUpgradeListener = new ArrayList<OnUpgradeListener>();
	
	private Handler mHandle = new Handler();
	
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mVehicle = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			mVehicle = IVehicle.Stub.asInterface(arg1);
			
			if (mActiveListener != null)
				mActiveListener.onActive(true);
		}
	};
	
	private IKeyListener.Stub mRemoteKeyListener = new IKeyListener.Stub() {
		
		@Override
		public int onKeySetupResult(final int adc1, final int adc2, final int adc3)
				throws RemoteException {
			
			mHandle.post(new Runnable(){

				@Override
				public void run() {
		
					synchronized (IVIManager.class) {
						
						for (OnKeySetupListener  listener : mKeySetupListener) {
							listener.onKeySetupResult(adc1, adc2, adc3);
						}
					}
				}
			});
			
			return 0;
		}

		@Override
		public int onKeySetupStatus(final int status) throws RemoteException {
			
			mHandle.post(new Runnable(){

				@Override
				public void run() {
					synchronized (IVIManager.class) {
						
						for (OnKeySetupListener  listener : mKeySetupListener) {
							listener.onKeySetupStatus(status);
						}
					}
					
				}
			});
			return 0;
		}
	};
	
	private IUpgradeListener.Stub mRemoteUpgradeListener = new IUpgradeListener.Stub() {
		
		@Override
		public void onUpgrade(final int state, final int progress) throws RemoteException {
			
			mHandle.post(new Runnable(){

				@Override
				public void run() {
					synchronized (IVIManager.class) {
						for (OnUpgradeListener listener : mUpgradeListener){
							listener.onUpgrade(state, progress);
						}
					}
				}
				
			});
		}
	};
	
	/**
	 * 
	 * @param context
	 */
	public IVIManager(Context context, OnActiveListener listener){
		mContext = context;
		mActiveListener = listener;
		
		Intent intent = new Intent(SERVICE_ACTION);
		intent.setPackage("com.itoday.ivi");
		mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * 必须释放
	 */
	public void release(){
		
		try {
			if (!mKeySetupListener.isEmpty())
				mVehicle.unregisterKeySetupListener(mRemoteKeyListener);
			
			if (!mUpgradeListener.isEmpty())
				mVehicle.unregisterUpgradeListener(mRemoteUpgradeListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (mContext != null)
			mContext.unbindService(conn);
		
		mVehicle = null;
		
		if (mActiveListener != null)
			mActiveListener.onActive(false);
	}
	
	/**
	 * 判断是否已绑定服务
	 * @return
	 */
	public boolean isActive(){
		return mVehicle != null;
	}
	
	/**
	 * 注册按键学习状态监听器
	 * @param listener
	 */
	public void registerKeySetupListener(OnKeySetupListener listener){
		synchronized (IVIManager.class) {
			if (mKeySetupListener.isEmpty()){
				try {
					mVehicle.registerKeySetupListener(mRemoteKeyListener);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mKeySetupListener.remove(listener);
			mKeySetupListener.add(listener);
		}
	}
	
	/**
	 * 取消注册按键学习监听器
	 * @param listener
	 */
	public void unregisterKeySetupListener(OnKeySetupListener listener){
		synchronized (IVIManager.class) {
			mKeySetupListener.remove(listener);
			
			if (mKeySetupListener.isEmpty()){
				try {
					mVehicle.unregisterKeySetupListener(mRemoteKeyListener);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 注册MCU升级状态监听器
	 * @param listener
	 */
	public void registerUpgradeListener(OnUpgradeListener listener){
		synchronized (IVIManager.class) {
			if (mKeySetupListener.isEmpty()){
				try {
					mVehicle.registerUpgradeListener(mRemoteUpgradeListener);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mUpgradeListener.remove(listener);
			mUpgradeListener.add(listener);
		}
	}
	
	/**
	 * 取消注册MCU升级监听器
	 * @param listener
	 */
	public void unregisterUpgradeListener(OnUpgradeListener listener){
		synchronized (IVIManager.class) {
			mUpgradeListener.remove(listener);
			
			if (mUpgradeListener.isEmpty()){
				try {
					mVehicle.unregisterUpgradeListener(mRemoteUpgradeListener);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 静音控制
	 * @param on
	 * @return -1 表示失败
	 */
	public int setMute(boolean on){
		
		try {
			if(isActive())
				return mVehicle.setMute(on);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置主音量
	 * @param volume
	 * @return -1 表示失败
	 */
	public int setMainVolume(int volume){
		
		try {
			if(isActive())
				return mVehicle.setMainVolume(volume);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 打开通道
	 * @param source
	 * @return -1 表示失败
	 */
	public int openAudioSource(int source) {

		try {
			if(isActive())
				return mVehicle.openAudioSource(source);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;	
	}

	/**
	 * 关闭通道
	 * @param source
	 * @return -1 表示失败
	 */
	public int closeAudioSource(int source) {

		try {
			if(isActive())
				return mVehicle.closeAudioSource(source);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;	
	}
	
	/**
	 * 设置灯光颜色
	 * @param color
	 * @return -1 表示失败
	 */
	public int setLightColor(int color){
		
		try {
			if(isActive())
				return mVehicle.setLightColor(color);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置喇叭音量
	 * @param fr
	 * @param fl
	 * @param rr
	 * @param rl
	 * @return -1 表示失败
	 */
	public int setSpeakerVolume(int fr, int fl, int rr, int rl ){
		
		try {
			if(isActive())
				return mVehicle.setSpeakerVolume(fr, fl, rr, rl);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置当前运行模块
	 * @param module
	 * @return -1 表示失败
	 */
	public int setRunning(int module){
		
		try {
			if(isActive())
				return mVehicle.setRunning(module);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置设备电源
	 * @param dev
	 * @param state
	 * @return -1 表示失败
	 */
	public int setDevPower(int dev, int state){
		
		try {
			if(isActive())
				return mVehicle.setDevPower(dev, state);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 按键学习
	 * @param key
	 * @return -1 表示失败
	 */
	public int setupKey(int key){
		
		try {
			if(isActive())
				return mVehicle.setupKey(key);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取所有按键状态
	 * @return -1表示失败，其他成功
	 */
	public int requestKeysState(){
		try {
			if(isActive())
				return mVehicle.requestKeysState();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	/**
	 * 清楚按键
	 * @return -1 表示失败
	 */
	public int resetKeys(){
		
		try {
			if(isActive())
				return mVehicle.resetKeys();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 向设备发送按键
	 * @param dev
	 * @param key
	 * @return -1 表示失败
	 */
	public int sendKeyToDev(int dev, int key){
		
		try {
			if(isActive())
				return mVehicle.sendKeyToDev(dev, key);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取设备版本
	 * @param dev
	 * @return -1 表示失败
	 */
	public int requestVersion(int dev){
		
		try {
			if(isActive())
				return mVehicle.requestVersion(dev);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置升级状态
	 * @param state
	 * @return -1 表示失败
	 */
	public int setUpgrade(int state){
		
		try {
			if(isActive())
				return mVehicle.setUpgrade(state);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置输入音量
	 * @param dev
	 * @param volume
	 * @return -1 表示失败
	 */
	public int setInputVolume(int dev, int volume){
		
		try {
			if(isActive())
				return mVehicle.setInputVolume(dev, volume);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置低音增益
	 * @param subwoofer
	 * @return -1 表示失败
	 */
	public int setSubwooferGain(int subwoofer){
		
		try {
			if(isActive())
				return mVehicle.setSubwooferGain(subwoofer);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置混音模式
	 * @param mix
	 * @return -1 表示失败
	 */
	public int setMixGain(int mix){
		try {
			if(isActive())
				return mVehicle.setMixGain(mix);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	

	/**
	 * 设置低音增益
	 * @param band {@link IVIAudio}
	 * @param gain
	 * @return -1 表示失败
	 */
	public int setBassGain(int band, int gain){
		
		try {
			if(isActive())
				return mVehicle.setBassGain(band, gain);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置中音增益
	 * @param band {@link IVIAudio}
	 * @param gain
	 * @return -1 表示失败
	 */
	public int setMidGain(int band, int gain){
		
		try {
			if(isActive())
				return mVehicle.setMidGain(band, gain);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置高音增益
	 * @param band {@link IVIAudio}
	 * @param gain -1 表示失败
	 * @return
	 */
	public int setTrebleGain(int band, int gain){
		
		try {
			if(isActive())
				return mVehicle.setTrebleGain(band, gain);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置响度
	 * @param gain
	 * @return
	 */
	public int setLoudness(int gain){
		try {
			if(isActive())
				return mVehicle.setLoudness(gain);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 同步时间
	 * @param time
	 * @return -1 表示失败
	 */
	public int setTime(int time){
		try {
			if(isActive())
				return mVehicle.setTime(time);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取时间
	 * @return -1 表示失败
	 */
	public int requestTimeSync(){
		
		try {
			if(isActive())
				return mVehicle.requestTimeSync();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * mcu升级
	 * @param path 升级文件路径
	 * @return -1 表示失败
	 */
	public int startUpgrade(String path){
		try {
			if(isActive())
				return mVehicle.startUpgrade(path);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 结束MCU升级
	 * @return -1 表示失败
	 */
	public int endUpgrade(){
		try {
			if(isActive())
				return mVehicle.endUpgrade();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	*设置系统属性
	*@return -1 failed, 0 success
	*/
	public int setSystemProperties(String name, String value){
		try {
			if(isActive())
				return mVehicle.setSystemProperties(name, value);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	*获取系统属性
	*@return null failed
	*/
	public String getSystemProperties(String name){
		try {
			if(isActive())
				return mVehicle.getSystemProperties(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	/**
	 * 监听服务绑定状态，绑定成功后才能成功调用其他接口
	 * @author itoday
	 *
	 */
	public interface OnActiveListener {
		void onActive(boolean active);
	}
	
	/**
	 * 按键学习监听器
	 * @author iToday
	 *
	 */
	public interface OnKeySetupListener{
		/**
		 * 学习成功后返回的结果
		 * @param adc1
		 * @param adc2
		 * @param adc3
		 * @return
		 */
		int onKeySetupResult(int adc1, int adc2, int adc3);
		/**
		 * 按键学习状态通知
		 * @param status 前16位数据有效
		 * @return
		 */
		int onKeySetupStatus(int status);
	}
	
	/**
	 * MCU升级
	 * @author iToday
	 *
	 */
	public interface OnUpgradeListener{
		/**
		 * 升级状态及进度回调
		 * @param state 状态 
		 * @param progress 进度
		 */
		void onUpgrade(int state, int progress);
	}
}
