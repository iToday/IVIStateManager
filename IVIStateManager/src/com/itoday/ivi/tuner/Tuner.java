package com.itoday.ivi.tuner;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVIChannel;
import com.itoday.ivi.platform.IVITuner;

public class Tuner {
	
	private static final int FM_LOC_RSSI = 0x22; //FM 本地搜索标准
	
	private static final int FM_FAR_RSSI = 0x18;//FM 远程搜索标准
	
	private static final int AM_RSSI = 0x19;//AM锁台标准
	
	private int mFmRssiLevel = FM_LOC_RSSI;
	
	private Context mContext;
	
	private IVIDataManager mDataManager;
	
	private ArrayList<IVIChannel> mChannels = new ArrayList<IVIChannel>();
	
	private OnTunerListener mListener;
	
	private Handler mHandle = new Handler();
	
	private int mFreq;
	
	private int mBand;
	
	private int mArea;
	
	private int mState;
	
	private int mStereo;
	
	private int mLocOrFar;
	
	public Tuner(OnTunerListener listener){
		
		mListener = listener;
		mDataManager = IVIDataManager.instance();
		
		mArea = mDataManager.getInt(IVITuner.AREA, IVITuner.Area.AREA_EUROPE);
		mBand = mDataManager.getInt(IVITuner.BAND, IVITuner.Band.FM);
		mFreq = mDataManager.getInt(mBand == IVITuner.Band.FM ? IVITuner.LAST_FM_FREQ : IVITuner.LAST_AM_FREQ, 87500);
		
		mStereo = mDataManager.getInt(IVITuner.STEREO, IVITuner.Stereo.CLOSE);
		mLocOrFar = mDataManager.getInt(IVITuner.LOC_FAR, IVITuner.FmMode.FAR);
	}
		
	public int getFmMin(int area){
		
		return native_getMin(IVITuner.Band.FM, area);
	}
	
	public int getFmMax(int area){
		
		return native_getMax(IVITuner.Band.FM, area);
	}
	
	public int getFmStep(int area){
		
		return native_getStep(IVITuner.Band.FM, area);
	}
	
	public int getAmMin(int area){
		
		return native_getMin(IVITuner.Band.AM, area);
	}
	
	public int getAmMax(int area){
		
		return native_getMax(IVITuner.Band.AM, area);
	}
	
	public int getAmStep(int area){
		
		return native_getStep(IVITuner.Band.AM, area);
	}
	
	/**
	 * 打开收音机设备
	 * @return
	 */
	public int open(){
		
		
		int res =  native_open();
		
		int freq = mDataManager.getInt(IVITuner.LAST_AM_FREQ, -1);
		
		if (freq == -1)
			mDataManager.putInt(IVITuner.LAST_AM_FREQ, native_getMin(IVITuner.Band.AM, mArea));
		
		freq = mDataManager.getInt(IVITuner.LAST_FM_FREQ, -1);
		
		if (freq == -1)
			mDataManager.putInt(IVITuner.LAST_FM_FREQ, native_getMin(IVITuner.Band.FM, mArea));
		
		//setArea(mArea);
		
		mArea = native_setArea(mArea);
		
		mDataManager.putInt(IVITuner.AREA, mArea);
		
		setFreq(mFreq, mBand);
		setStereo(mStereo);
		
		setFmMode(mLocOrFar);
		
		native_setRssiLevel(IVITuner.Band.FM, mFmRssiLevel);
		native_setRssiLevel(IVITuner.Band.AM, AM_RSSI);
		
		return res;
	}
	
	/**
	 * 关闭收音机设备
	 * @return
	 */
	public int close(){
		
		mState = IVITuner.Status.CLOSED;
		return native_close();
	}
	
	public int setArea(int area){
		
		mArea = native_setArea(area);
		
		if (mArea == area){
			
			mDataManager.putInt(IVITuner.AREA, mArea);
			
			int min = native_getMin(mBand, mArea);
			setFreq(min, mBand);
		}
		
		return mArea;
	}
	
	public int getArea(){
		
		return mArea;
	}
	
	/**
	 * 设置频率
	 * @param freq
	 * @return
	 */
	public int setFreq(int freq , int band){
		
		if (freq == native_setFreq(freq , band)){
			
			mState = IVITuner.Status.PLAYING;
			
			mFreq = freq;
			mBand = band;
			
			mDataManager.putInt(band == IVITuner.Band.FM ? IVITuner.LAST_FM_FREQ : IVITuner.LAST_AM_FREQ, freq);
			
			mDataManager.putInt(IVITuner.BAND, band);
			
			return freq;
		}
		
		return -1;
	}
	
	/**
	 * 获取当前频率
	 * @return
	 */
	public int getFreq(){
		
		return mFreq;
	}
	
	/**mChannels
	 * 获取当前状态
	 * @return {@link IVITuner}
	 */
	public int getState(){
		return mState;
	}
	/**
	 * 获取当前波段
	 * @return {@link IVITuner}
	 */
	public int getBand(){
		
		return mBand;
	}
	
	/**
	 * 
	 * @param band
	 * @return
	 */
	public int getRssiLevel(int band) {
		
		if (band == IVITuner.Band.AM)
			return AM_RSSI;
		
		return mFmRssiLevel;
	}
	
	/**
	 * 
	 * @param loc
	 * @return
	 */
	public int setFmMode(int loc){
		mFmRssiLevel = (loc == IVITuner.FmMode.LOC ? FM_LOC_RSSI : FM_FAR_RSSI);
		
		mDataManager.putInt(IVITuner.LOC_FAR, loc);
		return mFmRssiLevel;
	}
	
	/**
	 * 上一频点
	 * @return
	 */
	public int seekUp(){
		
		return native_seekUp();
	}
	
	/**
	 * 下一频点
	 * @return
	 */
	public int seekDown(){
		return native_seekDown();
	}
	
	/**
	 * 上扫描
	 * @return
	 */
	public int scanUp(){
		mState = IVITuner.Status.SCANNING;
		return native_scanUp();
	}
	
	/**
	 * 下扫描
	 * @return
	 */
	public int scanDown(){
		mState = IVITuner.Status.SCANNING;
		return native_scanDown();
	}
	
	/**
	 * 扫描保存
	 * @return
	 */
	public int scanSave(){
		mState = IVITuner.Status.SCANNING;
		return native_scanSave();
	}
	
	/**
	 * 停止扫描
	 * @return
	 */
	public int stop(){
		return native_stop();
	}
	
	public int setMute(int mute){
		return native_setMute(mute);
	}
	
	public int setVolume(int volume){
		return native_setVolume(volume);
	}
	
	public int setStereo(int stereo){
		mDataManager.putInt(IVITuner.STEREO, stereo);
		return native_setStereo(stereo);
	}
	
	public int getStereo(int freq){
		return native_getStereo(freq);
	}
	
	public int getRSSI(int freq, int band){
		return native_getRSSI(freq, band);
	}
	
	public boolean isSupportRds(){
		return native_isSupportRds();
	}
	
	public void setRdsListener(OnRdsListener listener){
		native_setRdsListener(listener);
	}
	
	/**
	 * 电台信号，扫描电台回调
	 * @param freq 频率
	 * @param band 波段
	 * @param level 电台信号
	 * 
	 * 此函数由JNI层主动调用
	 */
	private void onSignal(final int freq, final int band, final int level){
		
		mHandle.post(new Runnable() {
			
			@Override
			public void run() {
				if(mListener != null){
					mListener.onSignal(freq, band, level);
				}				
			}
		});
		
	}
	
	/**
	 * 扫描状态变化
	 * @param scanType 扫描类型
	 * @param newStatus 当前的状态
	 * @param oldStatus 之前状态
	 * 
	 * 此函数由JNI层主动调用
	 */
	private void onState(final int scanType, final int newStatus, final int oldStatus){
		mHandle.post(new Runnable() {
			
			@Override
			public void run() {
				if(mListener != null){
					mListener.onState(scanType, newStatus, oldStatus);
				}
			}
		});
		
	}
	
	private void onFreq(final int freqNew, final int bandNew, final int freqOld, final int bandOld){
		mHandle.post(new Runnable() {
			
			@Override
			public void run() {
				if(mListener != null){
					mListener.onFreq(freqNew, bandNew, freqOld, bandOld);
				}
			}
		});
	}
		
	public interface OnTunerListener{
		
		/**
		 * 电台信号，扫描电台回调
		 * @param freq 频率
		 * @param band 波段
		 * @param level 电台信号
		 */
		void onSignal(int freq,int band, int level);
		
		/**
		 * 扫描状态变化
		 * @param scanType 扫描类型
		 * @param newStatus 当前的状态
		 * @param oldStatus 之前状态
		 */
		void onState(int scanType, int newStatus, int oldStatus);
		
		/**
		 * 频率变化
		 * @param newFreq
		 * @param newBand
		 * @param oldFreq
		 * @param oldBand
		 */
		void onFreq(int newFreq, int newBand, int oldFreq, int oldBand);
	}

	/**
	 * RDS信息监听
	 * @author iToday
	 *
	 */
	public interface OnRdsListener{
		
		void onPI(int pi);
		
		void onPTY(int pty);
		
		void onPS(String ps);
		
		void onAltFreqs(int[] freqs);
		
		void onRadioText(String text);
	}
	
	private native int native_open();
	
	private native int native_close();
	
	private native int native_setFreq(int freq , int band);
	
	private native int native_getMax(int band, int area);
	
	private native int native_getMin(int band, int area);
	
	private native int native_getStep(int band, int area);
	
	private native int native_setArea(int area);
	
	private native int native_seekUp();
	
	private native int native_seekDown();
	
	private native int native_scanUp();
	
	private native int native_scanDown();
	
	private native int native_scanSave();
	
	private native int native_setMute(int mute);
	
	private native int native_setVolume(int volume);
	
	private native int native_setStereo(int stereo);
	
	private native int native_getStereo(int freq);
	
	private native int native_getRSSI(int freq, int band);
	
	private native int native_stop();
	
	private native int native_setRssiLevel(int band, int level);
	
	private native boolean native_isSupportRds();
	
	private native void native_setRdsListener(OnRdsListener listener);
	
	static {
		System.loadLibrary("tuner");
	}
}
