package com.itoday.ivi.vehicle;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.platform.IVIDevice.DeviceID;
import com.itoday.ivi.platform.IVINavi;
import com.itoday.ivi.platform.IVIPhone;
import com.itoday.ivi.platform.IVIPlatform;
import com.itoday.ivi.platform.IVIPlatform.OnIVIInfoChange;
import com.itoday.ivi.platform.IVIToolKit;
import com.itoday.ivi.state.base.CompositeState;

/**
 * 负责车身信息及CanBus数据
 * @author itoday
 *
 */
public class VehicleManager implements OnIVIInfoChange{
	
	private static final String TAG = "VehicleManager";
	
	private ContentResolver mRecolver;
	
	private OnCanbusInfoChange mCanbusListener;
	
	private ArrayList<IVIDevice> devices = new ArrayList<IVIDevice>();
	
	private StateMachine mStateMachine;
	
	private IVIPlatform mPlatform;
	
	private CarAudioManager mCarAudioManager;
	
	private AppModeManager mModeManager;
	
	private int mRunning = -1;
	
	private int mLightColor = 0;
	
	private Context mContext;
	
	private Handler mHandle = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what){
			case CompositeState.MSG_START_ACTIVITY:{
				
				String action = (String) msg.obj;
				
				if (action != null){
					
					if (action.equals(IVIToolKit.ACTION_NAVI)){
						
						String pkg = IVINavi.getRunningNaviApp(mContext);
						
						if (pkg == null)
							pkg = Settings.System.getString(mContext.getContentResolver(), "GPS_PKG_NAME");
						
						if (pkg != null)
							IVIToolKit.startActivity(mContext, pkg);
						else
							IVIToolKit.startActivityWithAction(mContext, action);
						
					} else if (action.equals(IVIToolKit.ACTION_RADIO)){ 
						
						IVIToolKit.startActivity(mContext, "com.tomwin.fmradio");
						
					} else if (action.equals(IVIToolKit.ACTION_LOCAL_MUSIC)){ 
						
						IVIToolKit.startActivity(mContext, "com.tomwin.audio");
						
					} else {
						
						IVIToolKit.startActivityWithAction(mContext, (String)msg.obj);
					}
				}
			}break;
			case CompositeState.MSG_MEDIA_MODE:
				IVIToolKit.startActivityWithPkg(mContext, mModeManager.media());
				break;
			case CompositeState.MSG_SCREEN_DISPLAY:
				mStateMachine.setOffScreen(msg.arg1 == 0);
				break;
			case CompositeState.MSG_SRC_MODE:
				IVIToolKit.startActivityWithPkg(mContext, mModeManager.src());
				break;
			case CompositeState.MSG_SEND_BROADCAST:{
				Bundle data = msg.getData();
				
				String action = data.getString("action");
				String key = data.getString("key");
				int value = data.getInt("value");
				
				IVIToolKit.sendBroadcast(mContext, action, key, value);
			}break;
			default:
				break;
			}
		}
		
	};
	
	/**
	 * 构造函数
	 * @param context
	 */
	public VehicleManager(Context context, IVIPlatform platform, CarAudioManager carAudo){
		
		mPlatform = platform;
		mContext = context;
		mCarAudioManager = carAudo;
		
		mModeManager = new AppModeManager(context);
		
		mRecolver = context.getContentResolver();
		
		mStateMachine = new StateMachine(mPlatform, mHandle);
		
		IVIDevice device = new IVIDevice(DeviceID.DEV_ACC, DeviceID.ACC, IVIDevice.ON);
		devices.add(device);
		
		device = new IVIDevice(DeviceID.DEV_LAMP, DeviceID.LAMP, IVIDevice.OFF);
		devices.add(device);
		
		device = new IVIDevice(DeviceID.DEV_BRAKE, DeviceID.BRAKE, IVIDevice.OFF);
		devices.add(device);
		
		device = new IVIDevice(DeviceID.DEV_REVERING, DeviceID.REVERING, IVIDevice.OFF);
		devices.add(device);
		
		//mModeManager.recovery();
		
	}
	
	public void setPlatform(IVIPlatform platform){
		mPlatform = platform;
	}

	@Override
	public int onDevice(int dev, int state) {
		
		Log.i(TAG, "onDevice dev :" + dev + " state:" + state);
		
		for (IVIDevice device : devices) {
			
			if (device.getId() == dev){
				
				if (device.setState(state)){
					
					android.provider.Settings.System.putInt(mRecolver, device.getName(), state);
					
					switch (dev) {
					case IVIDevice.DeviceID.DEV_ACC:
						
						if (mCarAudioManager != null && device.isOn())
							mCarAudioManager.apply();
						
						if (device.isOff()){
							mModeManager.backup();
						}
						
						mStateMachine.setSleep(!device.isOn());										
						break;
					case IVIDevice.DeviceID.DEV_REVERING:
						mStateMachine.setReversing(device.isOn());
						break;
					case IVIDevice.DeviceID.DEV_LAMP:
						mPlatform.setDayOrNight(device.isOff());
						break;
					default:
						break;
					}
				}
				break;
			}
		}
		
		return 0;
	}

	@Override
	public boolean onKey(int key, int state, int step) {
		Log.i(TAG, "onKey key :" + key + " state:" + state + " step:" + step);
		
		if (state == KeyEvent.ACTION_UP){
			playClickSound();
		}
		return mStateMachine.onKey(key, state, step);
	}

	private void playClickSound() {
		
		AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager != null) {
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        } else {
            Log.w(TAG, "performVirtualKeyClickSound");
        }
	}

	/**
	 * 注册Canbus信息监听器
	 * @param canbusListener
	 */
	public void setCanbusListener(OnCanbusInfoChange canbusListener) {
		this.mCanbusListener = canbusListener;
	}

	/**
	 * 车身信息监听器
	 * @author itoday
	 *
	 */
	public interface OnVehicleInfoChange{
		//ACC状态
		void onAcc(boolean on);
		//大灯状态
		void onLamp(boolean on);
		//倒车状态
		void onReversing(boolean on);
		//手刹状态
		void onBrake(boolean on);

	}
	
	/**
	 * CanBus监听器
	 * @author itoday
	 *
	 */
	public interface OnCanbusInfoChange{
		/**
		 * 
		 * @param info CanBus信息
		 * @param ex 用来扩展需要
		 */
		void onCanbusInfoChange(int param, String info);
	}

	@Override
	public int onCanInfo(int param, String info) {
		
		if (mCanbusListener != null)
			mCanbusListener.onCanbusInfoChange(param, info);
		return 0;
	}

	@Override
	public int onPhoneTalkStateChange(int state) {
		
		mCarAudioManager.muteRearSpeaker(state != IVIPhone.IDLE);
		mStateMachine.setPhone(state != IVIPhone.IDLE);
		return 0;
	}

	@Override
	public int onNaviStateChange(int state) {
		
		mStateMachine.setNavi(state == IVIDevice.ON);
		return 0;
	}

	@Override
	public int onVoiceStateChange(int state) {
		mStateMachine.setVoice(state == IVIDevice.ON);
		return 0;
	}
	
	@Override
	public int onStandbyStateChange(int state) {
		mStateMachine.setStandby(state == IVIDevice.ON);
		return 0;
	}
	
	@Override
	public int onSilentChange(boolean state) {
		mStateMachine.setSilent(state);
		return 0;
	}
	/**
	 * 设置灯光颜色
	 * @param color
	 * @return
	 */
	public  int setLightColor(int color){
		mLightColor = color;
		
		return mPlatform.setLightColor(color);
	}
	
	/**
	 * 设置当前运行模块
	 * @param module
	 * @return
	 */
	public  int setRunning(int module){
		mRunning = module;
		return mPlatform.setRunning(module);
	}
	
	/**
	 * 设置设备电源
	 * @param dev
	 * @param state
	 * @return
	 */
	public  int setDevPower(int dev, int state){
		return mPlatform.setDevPower(dev, state);
	}
	
	/**
	 * 获取设备版本
	 * @param dev
	 * @return
	 */
	public  int requestVersion(int dev){
		return mPlatform.requestVersion(dev);
	}
	
	/**
	 * 设置升级状态
	 * @param state
	 * @return
	 */
	public  int setUpgrade(int state){
		return mPlatform.setUpgrade(state);
	}
	
	/**
	 * 同步时间
	 * @param time
	 * @return
	 */
	public  int setTime(int time){
		return mPlatform.setTime(time);
	}
	
	/**
	 * 获取时间
	 * @return
	 */
	public  int requestTimeSync(){
		return mPlatform.requestTimeSync();
	}
	
	/**
	 * 获取当前七彩灯颜色
	 * @return
	 */
	public int getLightColor(){
		return mLightColor;
	}
	
	/**
	 * 获取当前运行程序
	 * @return
	 */
	public int getRunning(){
		return mRunning;
	}
}
