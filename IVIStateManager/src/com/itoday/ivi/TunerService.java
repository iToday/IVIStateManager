package com.itoday.ivi;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVIAudio;
import com.itoday.ivi.platform.IVIChannel;
import com.itoday.ivi.platform.IVITuner;
import com.itoday.ivi.sdk.IVIManager;
import com.itoday.ivi.sdk.IVIManager.OnActiveListener;
import com.itoday.ivi.tuner.ChannelManager;
import com.itoday.ivi.tuner.RdsData;
import com.itoday.ivi.tuner.Tuner;
import com.itoday.ivi.tuner.Tuner.OnRdsListener;
import com.itoday.ivi.tuner.Tuner.OnTunerListener;

/**
 * 收音机服务
 * @author itoday
 *
 */
public class TunerService extends Service {
	
	private static final String tag = "TunerService";
	
	private static final String CMD_PARAM = "service.start.command";
	
	private IVIChannel mPlayChannel = new IVIChannel(87500, IVITuner.Band.FM, null);
	
	private CallBacks mListeners = new CallBacks();
	
	private ChannelManager mChannels;
	
	private AudioManager mAudioManager;
	
    private ComponentName mComponentName;
    
    private IVIManager mIviManager;
    
    private RdsData mRdsInfo;
    
    private int lastFocusStatus = AudioManager.AUDIOFOCUS_LOSS;
    
    private int scanType = IVITuner.Scan.SCAN_NONE;
	
	private OnTunerListener mTunerListener = new OnTunerListener() {
		
		@Override
		public void onFreq(int newFreq, int newBand, int oldFreq, int oldBand) {
			
			notifyFreqChange(newFreq, newBand, oldFreq, oldBand);
			
			int playIndex = mChannels.findFreqPos(newFreq, newBand);
			
			mPlayChannel.setFreq(newFreq);
			mPlayChannel.setBand(newBand);
			
			notifyFavorListChange(newBand, mChannels.getFavors(newBand), playIndex);
		}
		
		@Override
		public void onSignal(int freq, int band, int level) {
			
			if (scanType == IVITuner.Scan.SCAN_SAVE){
				onScanSaveSignal(freq, band, level);
			}
			
			notifyFreqSignal(freq, band, level);
		}
		
		@Override
		public void onState(int type, int newStatus, int oldStatus) {
			
			scanType = type;
			
			notifyTunerState(scanType, newStatus, oldStatus);
			
			if (oldStatus == IVITuner.Status.PLAYING
					&& newStatus == IVITuner.Status.SCANNING
					&& scanType == IVITuner.Scan.SCAN_SAVE){
				
				mChannels.clear(mTuner.getBand());
				
				notifyFavorListChange(mTuner.getBand(), new ArrayList<IVIChannel>(), 0);
			}else if (oldStatus == IVITuner.Status.SCANNING && newStatus == IVITuner.Status.PLAYING){
				if (lastFocusStatus != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
					mTuner.setMute(0);
				
				scanType = IVITuner.Scan.SCAN_NONE;
			}
			
		}

		private void notifyTunerState(int scanType, int newStatus, int oldStatus) {
			
			int i = mListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mListeners.getBroadcastItem(i).onState(scanType, newStatus, oldStatus);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mListeners.finishBroadcast();
		}
		
		private void onScanSaveSignal(int freq, int band, int level) {
			
			if (level >= mTuner.getRssiLevel(band)){
				
				int size = mChannels.addChannel(new IVIChannel(freq, band, null));
				
				notifyFavorListChange(band, mChannels.getFavors(band), size - 1);
			}
		}

		private void notifyFreqSignal(int freq, int band, int level) {
			int i = mListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mListeners.getBroadcastItem(i).onSignal(freq, band, level);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mListeners.finishBroadcast();
		}
		
		private void notifyFreqChange(int newFreq, int newBand, int oldFreq,
				int oldBand) {
			int i = mListeners.beginBroadcast();
		    while (i > 0) {
		        i--;
		        try {
		        	mListeners.getBroadcastItem(i).onFreq(newFreq, newBand, oldFreq, oldBand);
		        } catch (RemoteException e) {
		        	e.printStackTrace();
		        }
		    }
		    mListeners.finishBroadcast();
		}
	};
	
	private void notifyFavorListChange(int band, List<IVIChannel> favors, int playIndex){
		
		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onFavorList(band, favors, playIndex);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
	}
	
	private Tuner mTuner;// = new Tuner(mTunerListener);
	
	private class CallBacks extends RemoteCallbackList<ITunerListener>{

		@Override
		public void onCallbackDied(ITunerListener callback, Object cookie) {
			super.onCallbackDied(callback, cookie);
			
			Log.d(tag, "ITunerListener died");
			abandonAudioFocus();
		}
		
	}
	
	private OnActiveListener mIviListener = new OnActiveListener(){

		@Override
		public void onActive(boolean active) {
			
			if (active && lastFocusStatus == AudioManager.AUDIOFOCUS_GAIN)
				mIviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_FM);
		}
		
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(tag, "onCreate");
		IVIDataManager.setup(getApplicationContext());
		
		mTuner = new Tuner(mTunerListener);
		
		mIviManager = new IVIManager(this, mIviListener);
		
		mChannels = new ChannelManager(this);
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
		
		mPlayChannel.setBand(mTuner.getBand());
		mPlayChannel.setFreq(mTuner.getFreq());
		
		mTuner.open();
		
		if (mTuner.isSupportRds()){
			mTuner.setRdsListener(mRdsListener);
		}
	}
	
	
	@Override
	public void onDestroy() {
		mChannels.release();
		mTuner.close();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent != null){
			
			int cmd = intent.getIntExtra(CMD_PARAM, -1);
			
			switch(cmd){
			case KeyEvent.KEYCODE_MEDIA_NEXT:{
				
				int pos = mChannels.findChannelPos(mPlayChannel);
				if (pos < 0){
					mTuner.setMute(1);
					mTuner.scanDown();
				}else{
					IVIChannel channel = mChannels.getChannel(mPlayChannel.getBand(), ++pos);
					
					if (channel != null)
						mTuner.setFreq(channel.getFreq(), channel.getBand());
				}
			}break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:{
				
				int pos = mChannels.findChannelPos(mPlayChannel);
				
				if (pos < 0){
					mTuner.setMute(1);
					mTuner.scanUp();
				}else{
					
					IVIChannel channel = mChannels.getChannel(mPlayChannel.getBand(), --pos );
					
					if (channel != null)
						mTuner.setFreq(channel.getFreq(), channel.getBand());
				}
			}break;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				mTuner.seekDown();
				break;
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				mTuner.seekUp();
				break;
			default:
				Log.d(tag, "unhandled start command " + cmd);
				break;
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return mBinder;
	}
	
	private ITuner.Stub mBinder = new ITuner.Stub() {
		
		@Override
		public int stop() throws RemoteException {
			return mTuner.stop();
		}
		
		@Override
		public int seekUp() throws RemoteException {
			return mTuner.seekUp();
		}
		
		@Override
		public int seekDown() throws RemoteException {
			return mTuner.seekDown();
		}
		
		@Override
		public int scanUp() throws RemoteException {
			mTuner.setMute(1);
			return mTuner.scanUp();
		}
		
		@Override
		public int scanSave() throws RemoteException {
			mTuner.setMute(1);
			return mTuner.scanSave();
		}
		
		@Override
		public int scanDown() throws RemoteException {
			mTuner.setMute(1);
			return mTuner.scanDown();
		}
		
		@Override
		public int open() throws RemoteException {
			requestAudioFocus();
			return 0;//mTuner.open();
		}
		
		@Override
		public int getState() throws RemoteException {
			return mTuner.getState();
		}
		
		@Override
		public int getFmStep(int area) throws RemoteException {
			return mTuner.getFmStep(area);
		}
		
		@Override
		public int getFmMin(int area) throws RemoteException {
			return mTuner.getFmMin(area);
		}
		
		@Override
		public int getFmMax(int area) throws RemoteException {
			return mTuner.getFmMax(area);
		}
		
		@Override
		public int getAmStep(int area) throws RemoteException {
			return mTuner.getAmStep(area);
		}
		
		@Override
		public int getAmMin(int area) throws RemoteException {
			return mTuner.getAmMin(area);
		}
		
		@Override
		public int getAmMax(int area) throws RemoteException {
			return mTuner.getAmMax(area);
		}
		
		@Override
		public int close() throws RemoteException {
			abandonAudioFocus();
			return 0;
		}

		@Override
		public int isStereo() throws RemoteException {
			return mTuner.getStereo(mPlayChannel.getFreq());
		}

		@Override
		public int setStereo(int on) throws RemoteException {
			return mTuner.setStereo(on);
		}

		@Override
		public int setLoc(int loc) throws RemoteException {
			
			return mTuner.setFmMode(loc);
		}

		@Override
		public void registerTunerListener(ITunerListener listener)
				throws RemoteException {
			mListeners.register(listener);
		}

		@Override
		public void unregisterTunerListener(ITunerListener listener)
				throws RemoteException {
			mListeners.unregister(listener);
		}

		@Override
		public List<IVIChannel> getFavorLists(int band) throws RemoteException {
			
			return mChannels.getFavors(band);
		}

		@Override
		public int setFreq(IVIChannel channel) throws RemoteException {
			
			boolean needMute = channel.getBand() != mPlayChannel.getBand();
			
			if (needMute){
				mTuner.stop();
				mIviManager.setMute(true);	
			}
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
			int res = mTuner.setFreq(channel.getFreq(), channel.getBand());
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			 
			if (needMute)
				mIviManager.setMute(false);			
			
			return res;
		}

		@Override
		public IVIChannel getFreq() throws RemoteException {
			return mPlayChannel;
		}

		@Override
		public int setArea(int area) throws RemoteException {
			return mTuner.setArea(area);
		}

		@Override
		public int getArea() throws RemoteException {
			return mTuner.getArea();
		}

		@Override
		public int setFavor(int index, IVIChannel channel)
				throws RemoteException {
			
			mChannels.setChannel(index, channel);
			
			notifyFavorListChange(channel.getBand(), mChannels.getFavors(channel.getBand()), index);
			
			return index;
		}
	};
	
	public static class MediaButtonReceiver extends BroadcastReceiver {
		
		void startServiceWithCommand(Context context, int command){
			Intent intent = new Intent(context, TunerService.class);
			intent.putExtra(CMD_PARAM, command);
			context.startService(intent);
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			
			if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
				KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if(event != null && event.getAction() == KeyEvent.ACTION_UP){
					
					int keycode = event.getKeyCode();
					
					startServiceWithCommand(context, keycode);
				}
			}
		}
	}
	
	private void requestAudioFocus(){
		
		int res = mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		
		if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
				mAudioManager.registerMediaButtonEventReceiver(mComponentName);
			
			if (lastFocusStatus == AudioManager.AUDIOFOCUS_LOSS)
				mIviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_FM);
		
			lastFocusStatus = AudioManager.AUDIOFOCUS_GAIN;
			
		}
		
		Log.d(tag, "requestAudioFocus lastFocusStatus:" + lastFocusStatus);
	}
	
	private void abandonAudioFocus(){
		
		mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
		mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
		
		if (lastFocusStatus != AudioManager.AUDIOFOCUS_LOSS)
			mIviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_FM);
		
		mChannels.release();
		
		lastFocusStatus = AudioManager.AUDIOFOCUS_LOSS;
		
		Log.d(tag, "abandonAudioFocus lastFocusStatus:" + lastFocusStatus);
	}
	
	private OnAudioFocusChangeListener onAudioFocusChangeListener = new OnAudioFocusChangeListener() {
		
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                	abandonAudioFocus(); 
                	sendRemoteCommand(IVITuner.RemoteCommand.EXIT);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                	mTuner.setMute(1);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    break;
                case AudioManager.AUDIOFOCUS_GAIN://增加接听电话后状态
                	if (scanType == IVITuner.Scan.SCAN_NONE)
                		mTuner.setMute(0);
                	break;
                default:
                    break;
            }
            
            lastFocusStatus = focusChange;
        }
    };
    
	private void sendRemoteCommand(int cmd) {
		
		int i = mListeners.beginBroadcast();
	    while (i > 0) {
	        i--;
	        try {
	        	mListeners.getBroadcastItem(i).onCommand(cmd);
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        }
	    }
	    mListeners.finishBroadcast();
	}
	
	private OnRdsListener mRdsListener = new OnRdsListener(){

		@Override
		public void onPI(int pi) {
			mRdsInfo.setPi(pi);
		}

		@Override
		public void onPTY(int pty) {
			mRdsInfo.setPty(pty);
		}

		@Override
		public void onPS(String ps) {
			mRdsInfo.setPs(ps);
		}

		@Override
		public void onAltFreqs(int[] freqs) {
			mRdsInfo.setAltFreqs(freqs);
		}

		@Override
		public void onRadioText(String text) {
			mRdsInfo.setRt(text);
		}
		
	};
}
