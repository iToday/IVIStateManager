package com.itoday.ivi.platform.allwinner;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;

import android.os.UEventObserver.UEvent;
import android.os.UEventObserver;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.data.IntObserver;
import com.itoday.ivi.data.IntObserver.OnIntDataChange;
import com.itoday.ivi.platform.IVIAudio;
import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.platform.IVINavi;
import com.itoday.ivi.platform.IVIPhone;
import com.itoday.ivi.platform.IVIPlatform;
import com.itoday.ivi.platform.IVIToolKit;
import com.itoday.ivi.platform.allwinner.Mcu.OnMcuInfoChange;

/**
 * AllWinner平台
 * @author itoday
 *
 */
public class PlatformAllwinner extends IVIPlatform {
	
	private static final String TAG = "PlatformAllwinner";
	
	private static final int  DEV_CANBUS 	= 0x01;
	private static final int  DEV_FLAG 		= 0x05;
	private static final int  DEV_ACC 		= 0x09;
	private static final int  DEV_TV 		= 0x0A;
	private static final int  DEV_IPOD 		= 0x0F;
	private static final int  DEV_BT 		= 0x10;
	private static final int  DEV_DVR 		= 0x11;
	
	public static final String VOICE_STATE = "voice_state";
	
	private static final String STANDBY = "standby";
	
    private static final String REVERSE_UEVENT_MATCH = "/devices/virtual/switch/parking-switch";;
    private static final String REVERSE_STATE_PATH = "/sys/devices/virtual/switch/parking-switch/state";
    

    
    private static final String[] WHITE_APP = {
    	"com.itoday.ivi",
    	"com.android.launcher2",
    	"com.android.systemui",
    	"com.android.phone",
    	"com.android.settings",
    	"com.android.providers.settings",
    	"android",
    	"com.android.defcontainer",
    	"com.android.providers.telephony",
    	"com.android.launcher",
    	"com.android.provision",
    	"com.android.shell",
    	"com.android.providers.userdictionary",
    	"com.android.location.fused",
    	"com.android.server.telecom",
    	"com.android.packageinstaller",
    	"com.tomwin.cvbs",
    	"com.android.inputdevices",
    	"com.android.musicfx",
    	"com.android.providers.downloads",
    	"com.android.externalstorage",
    	"com.android.providers.media"
    };
    
	private Mcu mMcu;
	
	private Context mContext;
	
	private IntObserver phone;
	private IntObserver navi;
	private IntObserver voice;
	
	private IntObserver standby;
	
	private Brightness mBrightness;
	
	private Touch mTouch;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			String action = arg1.getAction();
			
			if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
				
				int ringerMode = arg1.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL);
				
				if (iviListener != null)
					iviListener.onSilentChange(ringerMode == AudioManager.RINGER_MODE_SILENT);
			}
		}
	};
	
	private UEventObserver mReverseObserver = new UEventObserver(){
		
		@Override
        public void onUEvent(UEvent event) {
			
			String state = event.get("SWITCH_STATE");
            
            Log.d(TAG, "UEventObserver responsed:" + event.toString());
            
            if(state.equals("1")) {
                if (iviListener != null)
    				 iviListener.onDevice(IVIDevice.DeviceID.DEV_REVERING, IVIDevice.ON);
            } else {
            	if (iviListener != null)
    				 iviListener.onDevice(IVIDevice.DeviceID.DEV_REVERING, IVIDevice.OFF);
            }
        }
		
	};
	
	public PlatformAllwinner(Context context) {
		super();
		mContext = context;
		
		mBrightness = new Brightness(context);
		mTouch = new Touch(context);
		
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
		mContext.registerReceiver(mReceiver, filter);
		
		mMcu = new Mcu(mcuListener);
		phone = new IntObserver(IVIPhone.PHONE_STATE);
		navi = new IntObserver(IVINavi.NAVI_STATE);
		voice = new IntObserver(VOICE_STATE);
		standby = new IntObserver(STANDBY);
		
		standby.setValue(IVIDevice.OFF);
		
		mMcu.open();
		
		mReverseObserver.startObserving(REVERSE_UEVENT_MATCH);
		
		Log.d(TAG, "startObserving " + REVERSE_UEVENT_MATCH );
		
		phone.registerDataChangeListener(phoneListener);
		navi.registerDataChangeListener(naviListener);
		voice.registerDataChangeListener(voiceListener);
		standby.registerDataChangeListener(standbyListener);
	}
	
	@Override
	public int init() {
	
		mMcu.requestVersion(0);
		
		mMcu.requestCanInfo(0);
		
		mMcu.setCanMode(0, 0);
		
		byte[] buffer = IVIToolKit.readFile(REVERSE_STATE_PATH);
		
		if (buffer.length > 0 && buffer[0] == '1'){
			 if (iviListener != null)
				 iviListener.onDevice(IVIDevice.DeviceID.DEV_REVERING, IVIDevice.ON);
		}
		
		return 0;
	}

	@Override
	public int uninit() {
		
		if (mMcu != null)
			mMcu.close();
		
		mReverseObserver.stopObserving();
		mContext.unregisterReceiver(mReceiver);
		
		mTouch.release();
		mBrightness.release();
		
		return 0;
	}
	
	private void killRunningApp(){
		
        PackageManager pManager = mContext.getPackageManager();
        
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        
        boolean isNeedKill = true;
        
        for (int i = 0; i < paklist.size(); i++) {
        	
            PackageInfo pak = (PackageInfo) paklist.get(i);
            
            isNeedKill = true;
            
            for(String app : WHITE_APP){
            	if (pak.packageName.equals(app)){
            		isNeedKill = false;
            		break;
            	}
            }
            
            if (isNeedKill){
            	IVIToolKit.killAppByPackage(mContext, pak.packageName);
            }
        }
	}
	
	private int transState(int state){
		
		switch (state){
		case 0:
			return KeyEvent.ACTION_UP;
		case 1:
			return KeyEvent.ACTION_DOWN;
		default:
			return state;
		}
	}

	private int transKey(int key) {
		
		switch (key) {
		case Mcu.KEY_GPS:
			return IVIKeyEvent.KEYCODE_NAVI;
		case Mcu.KEY_FMAM:
			return IVIKeyEvent.KEYCODE_RADIO;
		case Mcu.KEY_DVD:
			return IVIKeyEvent.KEYCODE_DVD;
		case Mcu.KEY_BLUETOOTH:
			return IVIKeyEvent.KEYCODE_PHONE;
		case Mcu.KEY_MODE:
			return IVIKeyEvent.KEYCODE_SRC;
		case Mcu.KEY_CALL:
			return IVIKeyEvent.KEYCODE_ANSWER;
		case Mcu.KEY_ENDCALL:
			return IVIKeyEvent.KEYCODE_ENDCALL;
		case Mcu.KEY_EJECT:
			return IVIKeyEvent.KEYCODE_DVD;
		case Mcu.KEY_VOL_DOWN:
			return IVIKeyEvent.KEYCODE_VOLUME_DOWN;
		case Mcu.KEY_VOL_UP:
			return IVIKeyEvent.KEYCODE_VOLUME_UP;
		case Mcu.KEY_POWER:
			return IVIKeyEvent.KEYCODE_POWER;
		case Mcu.KEY_MUTE:
			return IVIKeyEvent.KEYCODE_MUTE;
		case Mcu.KEY_PREV:
			return IVIKeyEvent.KEYCODE_MEDIA_PREV;
		case Mcu.KEY_NEXT:
			return IVIKeyEvent.KEYCODE_MEDIA_NEXT;
		case Mcu.KEY_PLAY:
			return IVIKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
		case Mcu.KEY_LONGPLAY:
			return IVIKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
		case Mcu.KEY_LONGPREV:
			return IVIKeyEvent.KEYCODE_MEDIA_PREV;
		case Mcu.KEY_LONGNEXT:
			return IVIKeyEvent.KEYCODE_MEDIA_NEXT;
		case Mcu.KEY_HOME:
			return IVIKeyEvent.KEYCODE_HOME;
		case Mcu.KEY_BACK:
			return IVIKeyEvent.KEYCODE_BACK;
		
		default:
			Log.d(TAG, "unhandled key: " + key);
			break;
		}
		return -1;
	}
	
	private int transKeySetupToMcu(int key){
		
		switch (key){
		case IVIKeyEvent.KEYCODE_VOLUME_UP:
			return Mcu.WHEEL_KEY_VOLUMEADD;
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
			return Mcu.WHEEL_KEY_VOLUMESUB;
		case IVIKeyEvent.KEYCODE_ANSWER:
			return Mcu.WHEEL_KEY_ANSWER;
		case IVIKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			return Mcu.WHEEL_KEY_PLAYPAUSE;
		case IVIKeyEvent.KEYCODE_MEDIA_PREV:
			return Mcu.WHEEL_KEY_PLAYPRE;
		case IVIKeyEvent.KEYCODE_MEDIA_NEXT:
			return Mcu.WHEEL_KEY_PLAYNEXT;
		case IVIKeyEvent.KEYCODE_MUTE:
			return Mcu.WHEEL_KEY_MUTE;
		case IVIKeyEvent.KEYCODE_POWER:
			return Mcu.WHEEL_KEY_POWER;
		case IVIKeyEvent.KEYCODE_RADIO:
			return Mcu.WHEEL_KEY_RADIO;
		case IVIKeyEvent.KEYCODE_NAVI:
			return Mcu.WHEEL_KEY_NAVIGATION;
		case IVIKeyEvent.KEYCODE_SRC:
			return Mcu.WHEEL_KEY_MODE;
		case IVIKeyEvent.KEYCODE_ENDCALL:
			return Mcu.KEY_ENDCALL;
		default:
			Log.d(TAG, "unhandled key to mcu: " + key);
			return -1;
		}
	}

	private int transDev(int dev) {
		
		switch (dev) {
		case DEV_CANBUS:
			return IVIDevice.DeviceID.DEV_CANBUS;
		case DEV_FLAG:
			return IVIDevice.DeviceID.DEV_LAMP;
		case DEV_ACC:
			return IVIDevice.DeviceID.DEV_ACC;
		case DEV_TV:
			return IVIDevice.DeviceID.DEV_TV;
		case DEV_IPOD:
			return IVIDevice.DeviceID.DEV_IPOD;
		case DEV_BT:
			return IVIDevice.DeviceID.DEV_BT;
		case DEV_DVR:
			return IVIDevice.DeviceID.DEV_DVR;

		default:
			Log.d(TAG, "unhandled dev: " + dev);
			break;
		}
		
		return -1;
	}

	private OnIntDataChange phoneListener = new OnIntDataChange(){

		@Override
		public int onIntDataChange(int newState, int oldState) {
			
			if (iviListener != null)
				return  iviListener.onPhoneTalkStateChange(newState);
			
			return 0;
		}
		
	};
	
	private OnIntDataChange standbyListener = new OnIntDataChange(){

		@Override
		public int onIntDataChange(int newState, int oldState) {
			
			 Log.d(TAG, "standbyListener:" + newState);
			
			if (iviListener != null)
				iviListener.onStandbyStateChange(newState);
			return 0;
		}
		
	};
	
	private OnIntDataChange naviListener = new OnIntDataChange() {
		
		@Override
		public int onIntDataChange(int newState, int oldState) {
			
			if (iviListener != null)
				iviListener.onNaviStateChange(newState);
			
			if (newState == 0)//end
				mMcu.setNaviNotify(0xff);
			else //start
				mMcu.setNaviNotify(newState);
			
			return 0;
		}
	};
	
	private OnIntDataChange voiceListener = new OnIntDataChange() {
		
		@Override
		public int onIntDataChange(int newState, int oldState) {
			
			if (iviListener != null)
				iviListener.onVoiceStateChange(newState);
			
			return 0;
		}
	};
	
	private OnMcuInfoChange mcuListener = new OnMcuInfoChange() {
		
		@Override
		public int onVersion(int id, String version) {
			
			IVIDataManager.instance().putString(IVIDevice.DeviceID.MCU + id, version);
			
			return 0;
		}
		
		@Override
		public boolean onKey(int key, int state, int step) {
			
			if (iviListener != null)
				return iviListener.onKey(transKey(key), transState(state), step);
			
			return false;
		}
		
		@Override
		public int onCanInfo(int param, String info) {
			
			if (iviListener != null)
				return iviListener.onCanInfo(param, info);
			
			return -1;
		}

		@Override
		public int onDevice(int dev, int state) {
			
			if (dev == DEV_FLAG){
				if (iviListener != null){
					 iviListener.onDevice(IVIDevice.DeviceID.DEV_LAMP, state & 0x01);
					return iviListener.onDevice(IVIDevice.DeviceID.DEV_BRAKE, (state & 0x02) >> 1);
				}
			} else {
			
				if (iviListener != null)
					return iviListener.onDevice(transDev(dev), state);
			}
			
			return -1;
		}

		@Override
		public void onPower(int state) {
		
			killRunningApp();
			mBrightness.shutdown(false);
		}

		@Override
		public int onKeySetupResult(int adc1, int adc2, int adc3) {
			if (keySetupListener != null)
				keySetupListener.onKeySetupResult(adc1, adc2, adc3);
			return 0;
		}

		@Override
		public int onKeySetupStatus(int status) {
			if (keySetupListener != null)
				keySetupListener.onKeySetupStatus(status);
			return 0;
		}

		@Override
		public void onUpgrade(int state, int progress) {
			if (upgradeListener != null)
				upgradeListener.onUpgrade(state, progress);
			
		}
	};
	
	@Override
	public int setTouch(int state) {
		if (state == IVIDevice.ON){//打开所有触摸
			mTouch.setTouch(Touch.TOUCH_MODE_ALL);
		}else {//只打开按键触摸
			mTouch.setTouch(Touch.TOUCH_MODE_KEY);
		}
		
		return 0;
	}

	@Override
	public int setBacklight(int state) {
		
		mBrightness.setBackLightOn(state == IVIDevice.ON);
		
		return 0;
	}

	@Override
	public int setMute(boolean on) {
		return mMcu.mute( on ? 0 : 1);
	}

	@Override
	public int setMainVolume(int volume) {
		return mMcu.setVolume(volume);
	}

	@Override
	public int setAudioSource(int source) {
		return mMcu.setAudioSource(source);
	}

	@Override
	public int setLightColor(int color) {
		return mMcu.setLightColor(color);
	}

	@Override
	public int setSpeakerVolume(int fr, int fl, int rr, int rl) {
		return mMcu.setSpeakerVolume(fr, fl, rr, rl);
	}

	@Override
	public int setRunning(int module) {
		return mMcu.setRunning(module);
	}

	@Override
	public int setDevPower(int dev, int state) {
		return mMcu.setDevPower(dev, state);
	}

	@Override
	public int setupKey(int key) {
		
		int value = transKeySetupToMcu(key);
		
		if (key >= 0)
			return mMcu.setupKey(value);
		
		return -1;
	}
	

	@Override
	public int requestKeysState() {
		return mMcu.setupKey(Mcu.WHEEL_QUERY);
	}

	@Override
	public int resetKeys() {
		return mMcu.resetKeys();
	}

	@Override
	public int sendKeyToDev(int dev, int key) {
		return mMcu.sendKeyToDev(dev, key);
	}

	@Override
	public int requestVersion(int dev) {
		return mMcu.requestVersion(dev);
	}

	@Override
	public int setUpgrade(int state) {
		return mMcu.setUpgrade(state);
	}

	@Override
	public int setInputVolume(int dev, int volume) {
		return mMcu.setInputVolume(volume);
	}

	@Override
	public int setSubwooferGain(int subwoofer) {
		return mMcu.setSubwooferGain(subwoofer);
	}

	@Override
	public int setMixGain(int mix) {
		return mMcu.setMixGain(mix);
	}

	@Override
	public int setBassQ(int bass) {
		return mMcu.setBassQ(bass);
	}

	@Override
	public int setMidQ(int mid) {
		return mMcu.setMidQ(mid);
	}

	@Override
	public int setTrebleQ(int treble) {
		return mMcu.setTrebleQ(treble);
	}

	@Override
	public int setBassGain(int gain) {
		return mMcu.setBassGain(gain);
	}

	@Override
	public int setMidGain(int gain) {
		return mMcu.setMidGain(gain);
	}

	@Override
	public int setTrebleGain(int gain) {
		return mMcu.setTrebleGain(gain);
	}

	@Override
	public int setLoudness(int gain) {
		return mMcu.setLoudness(gain);
	}

	@Override
	public int setTime(int time) {
		return mMcu.setTime(time);
	}

	@Override
	public int requestTimeSync() {
		return mMcu.requestTimeSync();
	}

	@Override
	public int startUpgrade(String path) {
		return mMcu.startUpgrade(path);
	}

	@Override
	public int endUpgrade() {
		return mMcu.endUpgrade();
	}
	
	@Override
	public int setDayOrNight(boolean day) {
	
		mBrightness.setMode(day ? Brightness.DAY : Brightness.NIGHT);
		return 0;
	}

	@Override
	public void initInputVolume(int[] InputVolume) {
		
		mMcu.initInputVolume(InputVolume[IVIAudio.AUDIO_SOURCE_MAIN], 
				InputVolume[IVIAudio.AUDIO_SOURCE_PHONE], 
				InputVolume[IVIAudio.AUDIO_SOURCE_TV], 
				InputVolume[IVIAudio.AUDIO_SOURCE_AUX], 
				InputVolume[IVIAudio.AUDIO_SOURCE_FM], 
				InputVolume[IVIAudio.AUDIO_SOURCE_IPOD]);
		
	}

}
