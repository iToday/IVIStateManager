package com.itoday.ivi.platform.allwinner;

import com.itoday.ivi.platform.IVIDevice;

import android.os.Handler;
import android.util.Log;
/**
 * 
 * @author itoday
 *不能设置为PUBLIC
 */
class Mcu {
	
	/**
	 * 方控按键定义
	 */
	public static final int WHEEL_KEY_VALUE = 0;
    public static final int WHEEL_KEY_VOLUMEADD = 1;
    public static final int WHEEL_KEY_VOLUMESUB = 2;
    public static final int WHEEL_KEY_PLAYPAUSE = 3;
    public static final int WHEEL_KEY_PLAYPRE = 4;
    public static final int WHEEL_KEY_PLAYNEXT = 5;
    public static final int WHEEL_KEY_HANGUP = 6;//挂断
    public static final int WHEEL_KEY_MUTE = 7;
    public static final int WHEEL_KEY_MODE = 8;
    public static final int WHEEL_KEY_ANSWER= 9;
    public static final int WHEEL_KEY_NAVIGATION = 10;
    public static final int WHEEL_KEY_POWER = 11;
    public static final int WHEEL_KEY_RADIO = 12;
    
    public static final int WHEEL_QUERY = 0xff;
    
    /**
     * MCU按键定义
     */
    public static final int KEY_MENU				= 3;	//KEYCODE_HOME
    public static final int KEY_HOME            = 3;
    public static final int KEY_BACK            = 4;
    public static final int KEY_CALL            = 5;
    public static final int KEY_ENDCALL         = 6;
    public static final int KEY_NUM0				= 7;	//KEYCODE_0
    public static final int KEY_NUM1				= 8;	//KEYCODE_1
    public static final int KEY_NUM2				= 9;	//KEYCODE_2
    public static final int KEY_NUM3				= 10;   //KEYCODE_3
    public static final int KEY_NUM4				= 11;   //KEYCODE_4
    public static final int KEY_NUM5				= 12;   //KEYCODE_5
    public static final int KEY_NUM6				= 13;   //KEYCODE_6
    public static final int KEY_NUM7				= 14;   //KEYCODE_7
    public static final int KEY_NUM8				= 15;   //KEYCODE_8
    public static final int KEY_NUM9				= 16;   //KEYCODE_9
    public static final int KEY_UP					= 19;	//KEYCODE_DPAD_UP
    public static final int KEY_DOWN				= 20;	//KEYCODE_DPAD_DOWN
    public static final int KEY_LEFT				= 21;	//KEYCODE_DPAD_LEFT
    public static final int KEY_RIGHT				= 22;	//KEYCODE_DPAD_RIGHT
    public static final int KEY_VOL_UP				= 24;	//KEYCODE_VOLUME_UP
    public static final int KEY_VOL_DOWN			= 25;   //KEYCODE_VOLUME_DOWN
    public static final int KEY_POWER				= 26;   //KEYCODE_POWER
			//KEYCODE_A=29 ~ KEYCODE_Z=54: 用于多功能按键的中间代换键值
    public static final int KEY_ENTER				= 66;	//KEYCODE_ENTER
    public static final int KEY_STOP				= 86;	//KEYCODE_MEDIA_STOP
    public static final int KEY_NEXT				= 87;   //KEYCODE_MEDIA_NEXT
    public static final int KEY_PREV				= 88;   //KEYCODE_MEDIA_PREVIOUS
    public static final int KEY_FB					= 89;	//KEYCODE_MEDIA_REWIND
    public static final int KEY_FF					= 90;	//KEYCODE_MEDIA_FAST_FORWARD
    public static final int KEY_SELECT              = 109;	//KEYCODE_BUTTON_SELECT
    public static final int KEY_MODE				= 110;	//KEYCODE_BUTTON_MODE
    public static final int KEY_PLAY				= 126;	//KEYCODE_MEDIA_PLAY
    public static final int KEY_PAUSE				= 127;  //KEYCODE_MEDIA_PAUSE
    public static final int KEY_EJECT				= 129;	//KEYCODE_MEDIA_EJECT
    public static final int KEY_MUTE				= 164;	//KEYCODE_VOLUME_MUTE
    public static final int KEY_TV					= 170;	//KEYCODE_TV
    public static final int KEY_MUSIC				= 209	;//KEYCODE_MUSIC
    public static final int KEY_DVD					= 223;
    public static final int KEY_FMAM				= 224;
    public static final int KEY_GPS					= 225;
    public static final int KEY_BLUETOOTH			= 226;
    public static final int KEY_EQ					= 227;
    public static final int KEY_SETUP				= 228;
    public static final int KEY_CHECKSCREEN			= 229;
    public static final int KEY_APS					= 230;	//收音机自动搜台
    public static final int KEY_ZOOM				= 231;
    public static final int KEY_ANGLE               = 232;
    public static final int KEY_SLOW                = 233;
    public static final int KEY_AMS_RPT             = 234;
    public static final int KEY_AUDIO               = 235;
    public static final int KEY_DVD_TITLE			= 236;
    public static final int KEY_DVD_SUBTITLE		= 237;
    public static final int KEY_LOC_RDM             = 238;
    public static final int KEY_BND_SYS             = 239;
    public static final int KEY_NUM10PLUS           = 240;
    public static final int KEY_ST_PROG             = 241;
    public static final int KEY_GOTO	            = 242;
    public static final int KEY_OSD	                = 243;
    public static final int KEY_DVD_PBC				= 244;
    public static final int KEY_BLACK				= 249;
    public static final int KEY_LONGPLAY			= 245;	//长按
    public static final int KEY_LONGPREV			= 246;	//长按
    public static final int KEY_LONGNEXT			= 247;	//长按
    public static final int KEY_DVD_PLAY			= 248;	//长按 DVD/PLAY 长按暂停/播放
    public static final int KEY_KEEP_PWR			= 250;	//长按
    public static final int KEY_INVALID				= 255;
	
	private OnMcuInfoChange mListener;
	
	//防止阻塞JNI调用
	private Handler handler = new Handler();
	
	private int mMute = IVIDevice.OFF;
	
	/**
	 * 构造函数
	 * @param listener
	 */
	public Mcu(OnMcuInfoChange listener){
		this.mListener = listener;
	}
	
	/**
	 * 静音
	 * @param mute
	 * @return
	 */
	public int mute(int mute){
		
		mMute = mute;
		
		return setMute(mute);
	}
	
	/**
	 * 设置音量，自动解除静音
	 * @param volume
	 * @return
	 */
	public int setVolume(int volume){
		
		if (volume == 0)
			mute(IVIDevice.OFF);//mute while volume 0
		else if (mMute == IVIDevice.OFF)
			mute(IVIDevice.ON);
		
		return setMainVolume(volume);
	}
	

	public  void onDevice(final int dev, final int state){
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (mListener != null)
					mListener.onDevice(dev, state);
			}
		});
	}
	
	public  void onKey(final int key, final int state, final int step){
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (mListener != null)
					mListener.onKey(key, state, step);
			}
		});
	}
	
	public void onKeySetupResult(final int adc1, final int adc2, final int adc3){
		
		handler.post(new Runnable(){

			@Override
			public void run() {
				if (mListener != null)
					mListener.onKeySetupResult(adc1, adc2, adc3);
			}
			
		});
	}
	
	public void onKeySetupStatus(final int status){
		handler.post(new Runnable(){

			@Override
			public void run() {
				if (mListener != null)
					mListener.onKeySetupStatus(status);
			}
			
		});
	}
	
	public  void onVersion(final int index, final String version){
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (mListener != null)
					mListener.onVersion(index, version);
			}
		});
	}
	
	public void onCanBusInfo(final int param, final String values){
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (mListener != null)
					mListener.onCanInfo(param, values);
			}
		});
	}
	
	public void onPower(final int state){
		
		handler.post(new Runnable(){

			@Override
			public void run() {
				
				if (mListener != null){
					
					if (state == KEY_POWER)
						mListener.onKey(KEY_POWER, -1, -1);
					else // KEY_KEEP_PWR
						mListener.onPower(state);
				}
			}
			
		});
	}
	
	public void onUpgrade(final int state, final int progress){
		
		handler.post(new Runnable(){

			@Override
			public void run() {
				
				if (mListener != null)
					mListener.onUpgrade(state, progress);
			}
			
		});
	}
	
	public interface OnMcuInfoChange{
		
		//原车状态
		int onDevice(int dev, int state);
		
		void onUpgrade(int state, int progress);

		//关机申请
		void onPower(int state);

		//原车按键
		boolean onKey(int key, int state, int step);
		
		//MCU版本信息
		int onVersion(int index, String version);
		
		//CANBUS数据
		int onCanInfo(int param, String info);
		
		int onKeySetupResult(int adc1, int adc2, int adc3);
		
		int onKeySetupStatus(int status);
	}
	
	public native int open();

	public native int close();
	
	public native int startUpgrade(String path);
	
	public native int endUpgrade();
	
	/********以下为按键操作***************************/
	public native int setupKey(int key);
	
	public native int resetKeys();
	
	public native int sendKeyToDev(int dev, int key);
	
	/**********************************************/
	public native int setLightColor(int color);
	
	public native int requestVersion(int dev);
	
	public native int setUpgrade(int state);
	
	public native int setRunning(int module);
	
	public native int setDevPower(int dev, int state);
	
	public native int setAudioSource(int source);
	
	public native int setNaviNotify(int state);
	
	/******下面是音量控制接口*********************************/
	/**
	 * 
	 * @param mute 0：静音/1：开启
	 * @return
	 */
	private native int setMute(int mute);
	
	public native int setInputVolume(int volume);
	
	public native int initInputVolume(int main, int phone, int tv, int aux, int fm, int ipod);
	
	private native int setMainVolume(int volume);
	
	public native int setSpeakerVolume(int fr, int fl, int rr, int rl);
	
	/****下面是EQ设置接口************************************/
	public native int setSubwooferGain(int subwoofer);
	
	public native int setMixGain(int mix);
	
	public native int setBassQ(int bass);
	
	public native int setMidQ(int mid);
	
	public native int setTrebleQ(int treble);
	
	public native int setBassGain(int gain);
	
	public native int setMidGain(int gain);
	
	public native int setTrebleGain(int gain);
	
	public native int setLoudness(int gain);
	
	/******设置时间************************************/
	public native int setTime(int time);
	
	public native int requestTimeSync();
	
	/******以下为Canbus操作****************************/
	
	public native int requestCanInfo(int id);
	
	public native int setCanMode(int mode, int bitrate);

	static {
		
		Log.d("Mcu.java", "loadLibrary");
		
		System.loadLibrary("mcu");
	}
	
}
