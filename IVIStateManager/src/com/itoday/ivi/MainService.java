package com.itoday.ivi;

import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.IAudioObserver;
import android.media.IAudioService;
import android.os.ServiceManager;
import android.util.Log;
import android.os.SystemProperties;
import android.provider.Settings;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVINavi;
import com.itoday.ivi.platform.IVIPlatform;
import com.itoday.ivi.platform.IVIPlatform.OnKeySetupListener;
import com.itoday.ivi.platform.IVIPlatform.OnUpgradeInfo;
import com.itoday.ivi.platform.allwinner.PlatformAllwinner;
import com.itoday.ivi.vehicle.CarAudioManager;
import com.itoday.ivi.vehicle.KeyManager;
import com.itoday.ivi.vehicle.VehicleManager;
import com.itoday.ivi.vehicle.VehicleManager.OnCanbusInfoChange;

/**
 * 车机系统服务
 * @author itoday
 *
 */
public class MainService extends Service {
	
	private static final String CANBUS_INFO = "info";

	private static final String TAG = "MainService";
	
	private static final String ACTION_CANBUS = "action.intent.canbus";
	
	private RemoteCallbackList<IKeyListener> mRemoteKeySetupListeners = new RemoteCallbackList<IKeyListener>();
	
	private RemoteCallbackList<IUpgradeListener> mRemoteUpgradeListeners = new RemoteCallbackList<IUpgradeListener>();
	
	private IVIPlatform mPlatform;
	
	private VehicleManager 	mVehicleManager;
	
	private CarAudioManager mCarAudioManager;
	
	private AudioManager mAudioManager;
	
	private KeyManager mKeyManager;
	
	private PowerManager mPowerManager;
	
	private IVIDataManager mDataManager;
	
	private IAudioService mAudioService;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		IVIDataManager.setup(this);
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		mPlatform = new PlatformAllwinner(this);
		
		mDataManager = IVIDataManager.instance();
		
		mCarAudioManager = new CarAudioManager(mPlatform);
		mKeyManager = new KeyManager(mPlatform);
		
		mVehicleManager = new VehicleManager(getApplicationContext(), mPlatform, mCarAudioManager);
		mVehicleManager.setCanbusListener(canbusListener);	
		
		mPlatform.setKeySetupListener(mKeySetupListener);
		mPlatform.setVehicleInfoListener(mVehicleManager);
		mPlatform.setUpgradeListener(mUpgradeListener);
		
		mPlatform.init();
		
		initOthers();
	}

	private void initOthers() {
		
		IBinder b = ServiceManager.getService(Context.AUDIO_SERVICE);
		mAudioService = IAudioService.Stub.asInterface(b);
        
        try{
        	mAudioService.registerAudioObserver(mAudioObserver);
        	
        	if (mAudioService.getStreamVolume(AudioManager.STREAM_MUSIC) > CarAudioManager.DEFAULT_VOLUME)
        		mAudioService.setStreamVolume(AudioManager.STREAM_MUSIC, CarAudioManager.DEFAULT_VOLUME, 0, getPackageName());
        		
        }catch (RemoteException e){
        	e.printStackTrace();
        }
        
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 3/*Settings.LOCATION_MODE_HIGH_ACCURACY*/);
        //start backcar service
        Intent service = new Intent("android.intent.action.BACKCAR_SERVICE");
        service.setPackage("com.tomwin.cvbs");
        startService(service);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return vehicle;
	}
	
	@Override
	public void onDestroy() {
		mPlatform.uninit();
		super.onDestroy();
	}

	private OnCanbusInfoChange canbusListener = new OnCanbusInfoChange() {
		
		@Override
		public void onCanbusInfoChange(int param, String info) {
			
			Intent intent = new Intent(ACTION_CANBUS);
			intent.putExtra(CANBUS_INFO, info);
			
			sendBroadcast(intent);
		}
	};
	
	private IVehicle.Stub vehicle = new IVehicle.Stub() {
		
		@Override
		public int setupKey(int key) throws RemoteException {
			return mKeyManager.setupKey(key);
		}
		
		@Override
		public int setUpgrade(int state) throws RemoteException {
			return mVehicleManager.setUpgrade(state);
		}
		
		@Override
		public int setTime(int time) throws RemoteException {
			return mVehicleManager.setTime(time);
		}
		
		@Override
		public int setSubwooferGain(int subwoofer) throws RemoteException {
			return mCarAudioManager.setSubwooferGain(subwoofer);
		}
		
		@Override
		public int setSpeakerVolume(int fr, int fl, int rr, int rl)
				throws RemoteException {
			return mCarAudioManager.setSpeakerVolume(fr, fl, rr, rl);
		}
		
		@Override
		public int setRunning(int module) throws RemoteException {
			return mVehicleManager.setRunning(module);
		}
		
		@Override
		public int setMute(boolean on) throws RemoteException {
			return mCarAudioManager.setMute(on);
		}
		
		@Override
		public int setMixGain(int mix) throws RemoteException {
			return mCarAudioManager.setMixGain(mix);
		}
		
		@Override
		public int setMainVolume(int volume) throws RemoteException {
			return mCarAudioManager.setMainVolume(volume);
		}
		
		@Override
		public int setLoudness(int gain) throws RemoteException {
			return mCarAudioManager.setLoudness(gain);
		}
		
		@Override
		public int setLightColor(int color) throws RemoteException {
			return mVehicleManager.setLightColor(color);
		}
		
		@Override
		public int setInputVolume(int dev, int volume) throws RemoteException {
			return mCarAudioManager.setInputVolume(dev, volume);
		}
		
		@Override
		public int setDevPower(int dev, int state) throws RemoteException {
			return mVehicleManager.setDevPower(dev, state);
		}
		
		@Override
		public int sendKeyToDev(int dev, int key) throws RemoteException {
			return mKeyManager.sendKeyToDev(dev, key);
		}
		
		@Override
		public int resetKeys() throws RemoteException {
			return mKeyManager.resetKeys();
		}
		
		@Override
		public int requestVersion(int dev) throws RemoteException {
			return mVehicleManager.requestVersion(dev);
		}
		
		@Override
		public int requestTimeSync() throws RemoteException {
			return mVehicleManager.requestTimeSync();
		}

		@Override
		public boolean isMute() throws RemoteException {
			return mCarAudioManager.isMute();
		}

		@Override
		public int getMainVolume() throws RemoteException {
			return mCarAudioManager.getMainVolume();
		}

		@Override
		public int getAudioSource() throws RemoteException {
			return mCarAudioManager.getSource();
		}

		@Override
		public int getLightColor() throws RemoteException {
			return mVehicleManager.getLightColor();
		}

		@Override
		public int getFrSpeakerVolume() throws RemoteException {
			return mCarAudioManager.getFrSpeakerGain();
		}

		@Override
		public int getFlSpeakerVolume() throws RemoteException {
			return mCarAudioManager.getFlSpeakerGain();
		}

		@Override
		public int getRrSpeakerVolume() throws RemoteException {
			return mCarAudioManager.getRrSpeakerGain();
		}

		@Override
		public int getRlSpeakerVolume() throws RemoteException {
			return mCarAudioManager.getRlSpeakerGain();
		}

		@Override
		public int getRunning() throws RemoteException {
			return mVehicleManager.getRunning();
		}

		@Override
		public int getInputVolume(int dev) throws RemoteException {
			return mCarAudioManager.getInputVolume(dev);
		}

		@Override
		public int getSubwooferGain() throws RemoteException {
			return mCarAudioManager.getSubwooferGain();
		}

		@Override
		public int getMixGain() throws RemoteException {
			return mCarAudioManager.getMixGain();
		}

		@Override
		public int getLoudness() throws RemoteException {
			return mCarAudioManager.getLoudness();
		}

		@Override
		public void registerKeySetupListener(IKeyListener listener)
				throws RemoteException {
			mRemoteKeySetupListeners.register(listener);
		}

		@Override
		public void unregisterKeySetupListener(IKeyListener listener)
				throws RemoteException {
			mRemoteKeySetupListeners.unregister(listener);
		}

		@Override
		public int openAudioSource(int source) throws RemoteException {
			return mCarAudioManager.openAudioSource(source);
		}

		@Override
		public int closeAudioSource(int source) throws RemoteException {
			return mCarAudioManager.closeAudioSource(source);
		}

		@Override
		public int setBassGain(int band, int gain) throws RemoteException {
			
			return mCarAudioManager.setBassGain(band, gain);
			 
		}

		@Override
		public int getBassGain(int band) throws RemoteException {
			return mCarAudioManager.getBassGain(band);
		}

		@Override
		public int setMidGain(int band, int gain) throws RemoteException {
			
			return mCarAudioManager.setMidGain(band, gain);
			 
		}

		@Override
		public int getMidGain(int band) throws RemoteException {
			return mCarAudioManager.getMidGain(band);
		}

		@Override
		public int setTrebleGain(int band, int gain) throws RemoteException {
			
			return mCarAudioManager.setTrebleGain(band, gain);
		}

		@Override
		public int getTrebleGain(int band) throws RemoteException {
			return mCarAudioManager.getTrebleGain(band);
		}

		@Override
		public void registerUpgradeListener(IUpgradeListener listener)
				throws RemoteException {
			mRemoteUpgradeListeners.register(listener);
		}

		@Override
		public void unregisterUpgradeListener(IUpgradeListener listener)
				throws RemoteException {
			mRemoteUpgradeListeners.unregister(listener);
		}

		@Override
		public int startUpgrade(String path) throws RemoteException {
			return mPlatform.startUpgrade(path);
		}

		@Override
		public int endUpgrade() throws RemoteException {
			return mPlatform.endUpgrade();
		}

		@Override
		public int requestKeysState() throws RemoteException {
			return mKeyManager.requestKeysState();
		}

		@Override
		public int setSystemProperties(String name, String value)
				throws RemoteException {
			
			if (name == null || name.length() == 0)
				return -1;
			
			try{
				SystemProperties.set(name, value);
			} catch (IllegalArgumentException e){
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public String getSystemProperties(String name) throws RemoteException {
			try{
				if (name != null && name.length() > 0)
					return SystemProperties.get(name);
			} catch (IllegalArgumentException e){
				e.printStackTrace();
			}
			return null;
		}
	};
	
	private OnKeySetupListener mKeySetupListener = new OnKeySetupListener() {

		private void notifyKeySetup(int adc1, int adc2, int adc3) {
			
			int i = mRemoteKeySetupListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mRemoteKeySetupListeners.getBroadcastItem(i).onKeySetupResult( adc1, adc2, adc3);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mRemoteKeySetupListeners.finishBroadcast();
		}
		
		private void notifyKeySetupStatus(int status) {
			
			int i = mRemoteKeySetupListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mRemoteKeySetupListeners.getBroadcastItem(i).onKeySetupStatus(status);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mRemoteKeySetupListeners.finishBroadcast();
		}

		@Override
		public int onKeySetupStatus(int status) {
			notifyKeySetupStatus(status);
			return 0;
		}

		@Override
		public int onKeySetupResult(int adc1, int adc2, int adc3) {
			Log.d(TAG, "onKeySetupResult adc1:" +  adc1 + "adc2:" + adc2 + "adc3:" +adc3);
			notifyKeySetup(adc1, adc2, adc3);
			return 0;
		}
	};
	
	private OnUpgradeInfo mUpgradeListener = new OnUpgradeInfo(){

		@Override
		public void onUpgrade(int state, int progress) {
			
			int i = mRemoteUpgradeListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mRemoteUpgradeListeners.getBroadcastItem(i).onUpgrade(state, progress);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mRemoteUpgradeListeners.finishBroadcast();
		}
		
	};
	
	private IAudioObserver.Stub mAudioObserver = new IAudioObserver.Stub(){
		
		private HashMap<String, Integer> clientHint = new HashMap<String, Integer>();
		
		public boolean requestAudioFocus(AudioAttributes aa,  int durationHint, String clientId, String callingPackageName, IBinder cb){
			
			Log.d(TAG, "requestAudioFoucs" + aa + clientId + ":" + durationHint + "packageName : " + callingPackageName);
			
			clientHint.remove(clientId);
			clientHint.put(clientId, durationHint);
			
			if (IVINavi.isNaviApplication(callingPackageName) || durationHint == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK){
				mDataManager.putInt(IVINavi.NAVI_STATE, 1);
			}
	    	return true;
	    }
	    
		public boolean abandonAudioFocus(String clientId,  AudioAttributes aa){
			Log.d(TAG, "abandonAudioFocus:" + clientId);
			Integer hint = clientHint.remove(clientId);
			
			if (IVINavi.isNaviApplication(clientId) ||
					(hint != null && hint.intValue() == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)){
				mDataManager.putInt(IVINavi.NAVI_STATE, 0);
			}
	    	return true;
	    }
	    
		public void onAudioFocusDied(AudioAttributes aa, int durationHint, String clientID, String callingPackageName ){
			Log.d(TAG, "onAudioFocusDied" + aa);
			
			if (IVINavi.isNaviApplication(clientID)){
				//mCarAudioManager.setMixGain(0);
				mDataManager.putInt(IVINavi.NAVI_STATE, 0);
			}
	    }
	    
		public void onTopAudioFocusChange(AudioAttributes aa, int durationHint, String clientID, String callingPackageName){
			Log.d(TAG, "onTopAudioFocusChange" + aa);
			
			if (IVINavi.isNaviApplication(clientID)){
				
			}
	    }
	    
		public void onNaviAudio(String pkg, boolean play){
			Log.d(TAG, "onNaviAudio" + pkg + play);
	    }
		public void onVoiceAudio(String pkg, boolean play){
			Log.d(TAG, "onVoiceAudio" + pkg + play);
	    }
	    
		public void adjustStreamVolume(int streamType, int direction, int flags){
			Log.d(TAG, "adjustStreamVolume" + streamType + ";" + direction + "; "+ flags);
			
			if (streamType == android.media.AudioManager.STREAM_MUSIC){
				mCarAudioManager.setMainVolume(mAudioManager.getStreamVolume(streamType));
				try {
					if (mAudioService.getRingerModeExternal() != AudioManager.RINGER_MODE_NORMAL)
						mAudioService.setRingerModeExternal(AudioManager.RINGER_MODE_NORMAL, getPackageName());
		        } catch (RemoteException e) {
		            Log.e(TAG, "Dead object in setRingerMode", e);
		        }
			}
	    }
	    
		public int  getStreamVolume(int streamType){
			Log.d(TAG, "getStreamVolume" + streamType );
	    	return 0;
	    }
		public void setStreamVolume(int streamType, int index, int flags){
			Log.d(TAG, "setStreamVolume" + streamType + ";" + index + "; "+ flags);
			
			if (streamType == android.media.AudioManager.STREAM_MUSIC){
				mCarAudioManager.setMainVolume(index);
				
				try {
					if (mAudioService.getRingerModeExternal() != AudioManager.RINGER_MODE_NORMAL)
						mAudioService.setRingerModeExternal(AudioManager.RINGER_MODE_NORMAL, getPackageName());
		        } catch (RemoteException e) {
		            Log.e(TAG, "Dead object in setRingerMode", e);
		        }
			}
	    }
	};
}
