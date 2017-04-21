package com.itoday.ivi.vehicle;

import java.util.Stack;

import android.util.Log;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVIAudio;
import com.itoday.ivi.platform.IVIPlatform;

public class CarAudioManager {
	
	private static final String tag = "AudioManager";
	
	public static final int DEFAULT_VOLUME = 20;
	
	private static final String[] INPUT_KEYS = {
		IVIAudio.MAIN_INPUT_GAIN,
		IVIAudio.PHONE_INPUT_GAIN,
		IVIAudio.TV_INPUT_GAIN,
		IVIAudio.AUX_INPUT_GAIN,
		IVIAudio.FM_INPUT_GAIN,
		IVIAudio.IPOD_INPUT_GAIN,
	};
	
	private IVIPlatform mPlatform;
	
	private IVIDataManager mDataManager;
	
	private boolean mMute = false;
	
	private int mMainVolume = DEFAULT_VOLUME; /*0 ~ 40*/
	
	private int[] mInputVolume = new int[IVIAudio.AUDIO_SOURCE_MAX]; /**-7 ~ +7*/
	
	private int mFrSpeakerGain = 0;
	
	private int mFlSpeakerGain = 0;
	
	private int mRrSpeakerGain = 0;
	
	private int mRlSpeakerGain = 0;
	
	private int mSubwooferGain = 0;
	
	private int mMixGain = 0;
	
	private int mBass80Gain = 0;
	
	private int mBass120Gain = 0;
	
	private int mMid500Gain = 0;
	
	private int mMid1000Gain = 0;
	
	private int mMid1500Gain = 0;
	
	private int mMid2500Gain = 0;
	
	
	private int mTreble7000Gain = 0;
	
	private int mTreble10000Gain = 0;
	
	private int mTreble12500Gain = 0;
	
	private int mTreble15000Gain = 0;
	
	private int mLoudness = 0;
	
	private Stack<AudioSource> mAudioSource = new Stack<AudioSource>();
	
	private boolean DEBUG_SOURCE = true;
	
	private Runnable audioSourceRunable = new Runnable(){
		
		private int mSource = IVIAudio.AUDIO_SOURCE_DEFAULT;
		
		private AudioSource findHighSource(){
			
			AudioSource high = mAudioSource.peek();
			
			for (AudioSource source : mAudioSource){
				
				if (source.isHigh(high))
					high = source;
			}
			
			return high;
		}
		
		@Override
		public void run() {
			
			while (true){
				
				if (mAudioSource.empty()){
					Log.w(tag, "audioSourceRunable stack empty set default main source:");
					
					AudioSource target = new AudioSource(IVIAudio.AUDIO_SOURCE_MAIN, AudioSource.PRIORITY_NORMAL);
					
					target.open();
					
					mAudioSource.push(target);	
				}
				
				AudioSource high = findHighSource();
				
				Log.w(tag, "audioSourceRunable TOP :" + high);
			
				if (high != null && high.isOpen())
					setAudioSource(high.getSource());
				
				try {
					synchronized(mAudioSource){
						mAudioSource.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private  int setAudioSource(int source){
			
			if (mSource == source){
				Log.w(tag, "the same audio.source ,do nothing" + source);
				return -1;
			}
			
			mSource = source;
			
			mDataManager.putInt(IVIAudio.AUDIO_SOURCE, mSource);
			
			return mPlatform.setAudioSource(source);
		}
		
	};
	
	private Thread audioSourceThread = new Thread(audioSourceRunable);
	
	public CarAudioManager(IVIPlatform platform){
		
		mPlatform = platform;
		mDataManager = IVIDataManager.instance();
		
		mMute = (mDataManager.getInt(IVIAudio.MUTE, mMute ? 1 : 0) == 1) ? true : false;
		
		mMainVolume = mDataManager.getInt(IVIAudio.MAIN_VOLUME, mMainVolume);
		mFrSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_FR_GAIN, mFrSpeakerGain);
		mFlSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_FL_GAIN, mFlSpeakerGain);
		mRrSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_RR_GAIN, mRrSpeakerGain);
		mRlSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_RL_GAIN, mRlSpeakerGain);
		mSubwooferGain = mDataManager.getInt(IVIAudio.SUBWOOFER_GAIN, mSubwooferGain);
		mMixGain = mDataManager.getInt(IVIAudio.MIX_GAIN, mMixGain);
		mBass80Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_80, mBass80Gain);
		mBass120Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_120, mBass120Gain);
		mMid500Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_500, mMid500Gain);
		mMid1000Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_1000, mMid1000Gain);
		mMid1500Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_1500, mMid1500Gain);
		mMid2500Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_2500, mMid2500Gain);
		mTreble7000Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_7000, mTreble7000Gain);
		mTreble10000Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_10000, mTreble10000Gain);
		mTreble12500Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_12500, mTreble12500Gain);
		mTreble15000Gain = mDataManager.getInt(IVIAudio.FREQ_BAND_15000, mTreble15000Gain);
		mLoudness = mDataManager.getInt(IVIAudio.LOUDNESS, mLoudness);
		
		for (int index = 0; index < mInputVolume.length; index ++)
			mInputVolume[index] = mDataManager.getInt(INPUT_KEYS[index], 5);
		
		apply();
		
		//default main source
		AudioSource target = new AudioSource(IVIAudio.AUDIO_SOURCE_MAIN, AudioSource.PRIORITY_NORMAL);
		
		target.open();
		
		mAudioSource.push(target);	
		
		audioSourceThread.start();
		
	}
	
	/**
	 * 静音后排喇叭
	 * @param mute
	 */
	public void muteRearSpeaker(boolean mute){
		
		if (mute)
			mPlatform.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, 10, 10);
		else
			mPlatform.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
	}
	
	/**
	 * 重新发送参数到MCU，初始化音效参数
	 */
	public void apply() {
		
		mPlatform.setMainVolume(mMainVolume);
		mPlatform.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
		mPlatform.setSubwooferGain(mSubwooferGain);
		mPlatform.setMixGain(mMixGain);
		setBassGain(IVIAudio.BAND_80, mBass80Gain);
		setBassGain(IVIAudio.BAND_120, mBass120Gain);
		setMidGain(IVIAudio.BAND_500, mMid500Gain);
		setMidGain(IVIAudio.BAND_1000, mMid1000Gain);
		setMidGain(IVIAudio.BAND_1500, mMid1500Gain);
		setMidGain(IVIAudio.BAND_2500, mMid2500Gain);
		setTrebleGain(IVIAudio.BAND_7000, mTreble7000Gain);
		setTrebleGain(IVIAudio.BAND_10000, mTreble10000Gain);
		setTrebleGain(IVIAudio.BAND_12500, mTreble12500Gain);
		setTrebleGain(IVIAudio.BAND_15000, mTreble15000Gain);
		mPlatform.setLoudness(mLoudness);
		
		mPlatform.initInputVolume(mInputVolume);
	}
	
	/**
	 * 静音控制
	 * @param on
	 * @returnmInputDeviceVolume
	 */
	public  int setMute(boolean on){
		
		if (mMute == on) 
			return -1;
		
		mMute = on;
		
		mDataManager.putInt(IVIAudio.MUTE, mMute ? 1 : 0);
		
		return mPlatform.setMute(on);
	}
	
	/**
	 * 设置主音量
	 * @param volume
	 * @return
	 */
	public  int setMainVolume(int volume){
		if (mMainVolume == volume)
			return  -1;
		
		mMainVolume = volume;
		
		mDataManager.putInt(IVIAudio.MAIN_VOLUME, mMainVolume);
		
		return mPlatform.setMainVolume(volume);
	}
	
	public int openAudioSource(int source){
		
		int priority = AudioSource.PRIORITY_NORMAL;
		
		if (source == IVIAudio.AUDIO_SOURCE_PHONE)
			priority = AudioSource.PRIORITY_HIGHT;
		
		AudioSource target = new AudioSource(source, priority);
		
		try {
			synchronized (mAudioSource){
				
				for (AudioSource s : mAudioSource){
					if (s.isSource(source)){
						mAudioSource.remove(s);
						target = s;
						break;
					}
				}
				
				target.open();
				
				mAudioSource.push(target);
				
				mAudioSource.notifyAll();
				
				if (DEBUG_SOURCE)
					Log.d(tag, "PUSH " + target);
			}
		} catch (IllegalMonitorStateException e) {
			e.printStackTrace();
		}
		
		//
		return 0;
	}
	
	public int closeAudioSource(int source){
		
		synchronized (mAudioSource){
			
			for (AudioSource s : mAudioSource){
				
				if (DEBUG_SOURCE)
					Log.d(tag, "close " + source + " stack:" + s);
				
				if (s.isSource(source)){
					
					if (s.close() == 0 ){
						
						mAudioSource.remove(s);
						
						try {
							mAudioSource.notifyAll();
						} catch (IllegalMonitorStateException e) {
							e.printStackTrace();
						}
					}
					break;
				}
			}
		}
		
		return 0;
		
	}
	
	public int getSource() {
		return mAudioSource.peek().getSource();
	}
	
	/**
	 * 设置喇叭音量
	 * @param fr
	 * @param fl
	 * @param rr
	 * @param rl
	 * @return
	 */
	public  int setSpeakerVolume(int fr, int fl, int rr, int rl ){
		
		if (mFlSpeakerGain == fl && mFrSpeakerGain == fr 
				&& mRrSpeakerGain == rr && mRlSpeakerGain == rl)
			return -1;
		
		mFrSpeakerGain = fr;
		mFlSpeakerGain = fl;
		mRrSpeakerGain = rr;
		mRlSpeakerGain = rl;
		
		mDataManager.putInt(IVIAudio.SPEAKER_FL_GAIN, mFlSpeakerGain);
		mDataManager.putInt(IVIAudio.SPEAKER_FR_GAIN, mFrSpeakerGain);
		mDataManager.putInt(IVIAudio.SPEAKER_RL_GAIN, mRlSpeakerGain);
		mDataManager.putInt(IVIAudio.SPEAKER_RR_GAIN, mRrSpeakerGain);
		
		return mPlatform.setSpeakerVolume(fr, fl, rr, rl);
	}
	
	/**
	 * 设置输入音量
	 * @param dev
	 * @param volume
	 * @return
	 */
	public  int setInputVolume(int dev, int volume){
		
		if (dev >= 0 && dev < IVIAudio.AUDIO_SOURCE_MAX){
			
			mInputVolume[dev] = volume;
			mDataManager.putInt(INPUT_KEYS[dev], volume);
			
			return mPlatform.setInputVolume(dev, volume);
		}
		
		return -1;
	}
	
	/**
	 * 设置低音增益
	 * @param subwoofer
	 * @return
	 */
	public  int setSubwooferGain(int subwoofer){
		if (mSubwooferGain == subwoofer)
			return -1;
		
		mSubwooferGain = subwoofer;
		
		mDataManager.putInt(IVIAudio.SUBWOOFER_GAIN, mSubwooferGain);
		
		return mPlatform.setSubwooferGain(subwoofer);
	}
	
	/**
	 * 设置混音模式
	 * @param mix
	 * @return
	 */
	public  int setMixGain(int mix){
		
		if (mMixGain == mix)
			return -1;
		
		mMixGain = mix;
		
		mDataManager.putInt(IVIAudio.MIX_GAIN, mMixGain);
		
		return mPlatform.setMixGain(mix);
	}
	
	
	/**
	 * 设置低音增益
	 * @param gain
	 * @return
	 */
	public  int setBassGain(int band, int gain){
		
		switch (band){
		case IVIAudio.BAND_120:
			mDataManager.putInt(IVIAudio.FREQ_BAND_120, gain);
			mBass120Gain = gain;
			break;
		case IVIAudio.BAND_80:
			mDataManager.putInt(IVIAudio.FREQ_BAND_80, gain);
			mBass80Gain = gain;
			break;
		default:
			return -1;
		}
		
		mPlatform.setBassQ(band);
		return mPlatform.setBassGain(gain);
	}
	
	/**
	 * 设置中音增益
	 * @param gain
	 * @return
	 */
	public  int setMidGain(int band, int gain){
		
		switch (band){
		case IVIAudio.BAND_500:
			mDataManager.putInt(IVIAudio.FREQ_BAND_500, gain);
			mMid500Gain = gain;
			break;
		case IVIAudio.BAND_1000:
			mDataManager.putInt(IVIAudio.FREQ_BAND_1000, gain);
			mMid1000Gain = gain;
			break;
		case IVIAudio.BAND_1500:
			mDataManager.putInt(IVIAudio.FREQ_BAND_1500, gain);
			mMid1500Gain = gain;
			break;
		case IVIAudio.BAND_2500:
			mDataManager.putInt(IVIAudio.FREQ_BAND_2500, gain);
			mMid2500Gain = gain;
			break;
		default:
			return -1;
		}
		
		mPlatform.setMidQ(band);
		
		return mPlatform.setMidGain(gain);
	}
	
	/**
	 * 设置高音增益
	 * @param gain
	 * @return
	 */
	public  int setTrebleGain(int band, int gain){
		
		switch (band){
		case IVIAudio.BAND_7000:
			mDataManager.putInt(IVIAudio.FREQ_BAND_7000, gain);
			mTreble7000Gain = gain;
			break;
		case IVIAudio.BAND_10000:
			mDataManager.putInt(IVIAudio.FREQ_BAND_10000, gain);
			mTreble10000Gain = gain;
			break;
		case IVIAudio.BAND_12500:
			mDataManager.putInt(IVIAudio.FREQ_BAND_12500, gain);
			mTreble12500Gain = gain;
			break;
		case IVIAudio.BAND_15000:
			mDataManager.putInt(IVIAudio.FREQ_BAND_15000, gain);
			mTreble15000Gain = gain;
			break;
		default:
			return -1;
		}
		
		mPlatform.setTrebleQ(band);
		
		return mPlatform.setTrebleGain(gain);
	}
	
	/**
	 * 设置响度
	 * @param gain
	 * @return
	 */
	public  int setLoudness(int gain){
		
		if (mLoudness == gain)
			return -1;
		
		mLoudness = gain;
		
		mDataManager.putInt(IVIAudio.LOUDNESS, mLoudness);
		
		return mPlatform.setLoudness(gain);
	}
	
	public IVIPlatform getPlatform() {
		return mPlatform;
	}

	public boolean isMute() {
		return mMute;
	}

	public int getMainVolume() {
		return mMainVolume;
	}

	public int getFrSpeakerGain() {
		return mFrSpeakerGain;
	}

	public int getFlSpeakerGain() {
		return mFlSpeakerGain;
	}

	public int getRrSpeakerGain() {
		return mRrSpeakerGain;
	}

	public int getRlSpeakerGain() {
		return mRlSpeakerGain;
	}

	public int getSubwooferGain() {
		return mSubwooferGain;
	}

	public int getMixGain() {
		return mMixGain;
	}

	
	public int getBassGain(int band) {
		switch (band){
		case IVIAudio.BAND_120:
			return mBass120Gain;
		case IVIAudio.BAND_80:
			return mBass80Gain;
		default:
			return -1;
		}
	}

	public int getMidGain(int band) {
		switch (band){
		case IVIAudio.BAND_500:
			return mMid500Gain;
		case IVIAudio.BAND_1000:
			return mMid1000Gain;
		case IVIAudio.BAND_1500:
			return mMid1500Gain ;
		case IVIAudio.BAND_2500:
			return mMid2500Gain;
		default:
			return -1;
		}
		
	}

	public int getTrebleGain(int band) {
		switch (band){
		case IVIAudio.BAND_7000:
			return mTreble7000Gain;
		case IVIAudio.BAND_10000:
			return mTreble10000Gain;
		case IVIAudio.BAND_12500:
			return mTreble12500Gain;
		case IVIAudio.BAND_15000:
			return mTreble15000Gain;
		default:
			return -1;
		}
	}

	public int getLoudness() {
		return mLoudness;
	}
	
	public int getInputVolume(int dev){
		
		if (dev >= 0 && dev < IVIAudio.AUDIO_SOURCE_MAX)
			return mInputVolume[dev];
		
		return -1;
	}

	private class AudioSource{
		
		public static final int PRIORITY_HIGHT = 2;
		
		public static final int PRIORITY_NORMAL = 1;
		
		public static final int PRIORITY_LOW = 0;
		
		private int source;
		
		private int priority;
		
		private int openCount;
		
		public AudioSource(int source, int priority){
			this.source = source;
			openCount = 0;
			this.priority = priority;
		}
		
		public boolean isHigh(AudioSource source){
			return priority > source.priority;
		}
		
		public int open(){
			
			openCount ++;
			
			return openCount;
		}
		
		public int close(){
			
			openCount --;
			
			if (openCount < 0)
				openCount = 0;
			
			return openCount;
		}
		
		public boolean isOpen(){
			return openCount > 0;
		}
		
		public int getSource(){
			return source;
		}
		
		public boolean isSource(int source){
			return this.source == source;
		}

		@Override
		public String toString() {
			return "AudioSource [source=" + source + ", openCount=" + openCount
					+ "]";
		}
	}

}
