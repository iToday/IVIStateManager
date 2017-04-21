package com.tomwin.tpms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.tomwin.hardware.AudioTrackPlayer;
import com.tomwin.tpms.ui.MainActivity;

public class TpmsService extends Service {
	
	private static final String tag = "TpmsService";
	
	private static final int MSG_ALARM = 0x01;
	
	private static final int MSG_SHOW_ENABLE = 0x02;
	
	private static final String ALARM_PLAYING = "alarm.playing";

	private TpmsManager mManager;
	
	private IntObserver mSoundAlarm;
	
	private AudioManager mAudioManager;
	
//	private AudioTrackPlayer mPlayer;
	
	private boolean mNotShowView = false;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if (msg.what == MSG_ALARM){
				
				Log.d(tag, "MSG_ALARM isHaveWarning " + mManager.isHaveWarning());
				
				if (mManager.isHaveWarning()){
					
					if (!mNotShowView){
						
						playAlarm((Tyres)msg.obj);
						
						showWarning();
						mNotShowView = true;
						removeMessages(MSG_SHOW_ENABLE);
						sendEmptyMessageDelayed(MSG_SHOW_ENABLE, 120 * 1000);
					}
					
				}else
					stopAlarm();
				
			} else if (msg.what == MSG_SHOW_ENABLE){
				
				mNotShowView = false;
			}
		}
	};
	
	private OnAudioFocusChangeListener mListener = new OnAudioFocusChangeListener(){

		@Override
		public void onAudioFocusChange(int focusChange) {
			
		}
		
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		
		IVIDataManager.setup(getApplicationContext());
		
		mManager = new TpmsManager(this, mHandler);
		mSoundAlarm = new IntObserver(IVIDataManager.SOUND_ALARM);
		
	//	mPlayer = new AudioTrackPlayer();
		
	//	mPlayer.load( getResources().openRawResource(R.raw.alarm2));
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	}
	
	private void playAlarm(Tyres tyre){
		
		//showWarning();
		
		if (mSoundAlarm.getValue(1) == 1){
			
			IVIDataManager.instance().putInt(ALARM_PLAYING, 1);
			
			/*if (!mPlayer.isPlaying())*/{
				//mAudioManager.requestAudioFocus(mListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
				
				//mPlayer.start();
			}
		}
	}
	
	private void stopAlarm(){
		IVIDataManager.instance().putInt(ALARM_PLAYING, 0);
		//mAudioManager.abandonAudioFocus(mListener);
		//if (mPlayer.isPlaying())
		//	mPlayer.pause();
	}
	
	public void showWarning(){
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		
		startActivity(intent);
	}
	
	private ITpms.Stub mBinder = new ITpms.Stub() {
		
		@Override
		public void unregisterListener(IRemoteListener listener)
				throws RemoteException {
			mManager.unregisterListener(listener);
		}
		
		@Override
		public void switchPos(int index) throws RemoteException {
			mManager.switchPos(index);
		}
		
		@Override
		public void stopPair() throws RemoteException {
			mManager.stopPair();
		}
		
		@Override
		public void registerListener(IRemoteListener listener)
				throws RemoteException {
			mManager.registerListener(listener);
		}
		
		@Override
		public void queryId() throws RemoteException {
			mManager.queryId();
		}
		
		@Override
		public void queryBattery() throws RemoteException {
			mManager.queryBattery();
		}
		
		@Override
		public void pair(int index) throws RemoteException {
			mManager.pair(index);
		}

		@Override
		public void requestTyres() throws RemoteException {
			mManager.requestTyres();
		}
	};

}
