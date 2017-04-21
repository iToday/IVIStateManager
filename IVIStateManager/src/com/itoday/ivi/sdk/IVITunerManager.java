package com.itoday.ivi.sdk;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.itoday.ivi.ITuner;
import com.itoday.ivi.ITunerListener;
import com.itoday.ivi.platform.IVIChannel;
import com.itoday.ivi.platform.IVITuner;

/**
 * 收音机SDK，供界面调用
 * @author itoday
 *
 */
public class IVITunerManager {
	
	private static final String tag = "IVITunerManager";
	
	private static final String SERVICE_ACTION = "android.intent.action.TunerService";
	
	private Context mContext;
	
	private OnTunerListener mTunerListener;
	
	private ITuner mTuner;
	
	private Handler mHandler = new Handler();
	
	private ITunerListener.Stub remoteListener = new ITunerListener.Stub() {
		
		@Override
		public void onState(final int scanType, final int newStatus, final int oldStatus)
				throws RemoteException {
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					if (mTunerListener != null)
						mTunerListener.onState(scanType, newStatus, oldStatus);
				}
			});
			
		}
		
		@Override
		public void onSignal(final int freq, final int band, final int level) throws RemoteException {
			
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					if (mTunerListener != null)
						mTunerListener.onSignal(freq, band, level);
				}
				
			});
		}
		
		@Override
		public void onFreq(final int newFreq, final int newBand, final int oldFreq, final int oldBand)
				throws RemoteException {
			
		
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					if (mTunerListener != null)
						mTunerListener.onFreq(newFreq, newBand, oldFreq, oldBand);
				}
				
			});
		}

		@Override
		public void onFavorList(final int band, final List<IVIChannel> favors, final int playIndex)
				throws RemoteException {
			

			mHandler.post(new Runnable(){

				@Override
				public void run() {
					if (mTunerListener != null)
						mTunerListener.onFavorList(band, favors, playIndex);
				}
				
			});
		}

		@Override
		public void onCommand(final int cmd) throws RemoteException {
			
			
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					if (mTunerListener != null)
						mTunerListener.onRemoteCommand(cmd);
				}
			});
		}

	};
	
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			
			mTuner = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			
			mTuner = ITuner.Stub.asInterface(arg1);
			
			try {
				mTuner.registerTunerListener(remoteListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			if (mTunerListener != null)
				mTunerListener.onActive(true);
		}
	};
	
	/**
	 * 构造函数
	 * @param context 
	 * @param listener 监听服务绑定结果
	 */
	public IVITunerManager(Context context, OnTunerListener listener){
		
		mContext = context;
		mTunerListener = listener;
		
		Intent intent = new Intent(SERVICE_ACTION);
		intent.setPackage("com.itoday.ivi");
		mContext.startService(intent);
		mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * 在不使用时，必须释放
	 */
	public void release(){
		
		try {
			if (mTuner != null)
				mTuner.unregisterTunerListener(remoteListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (mContext != null)
			mContext.unbindService(conn);
		
		mTuner = null;
		
		if (mTunerListener != null)
			mTunerListener.onActive(false);
	}
	
	/**
	 * 判断是否已绑定服务
	 * @return
	 */
	public boolean isActive(){
		return mTuner != null;
	}
	
	/**
	 * 收藏频率
	 * @param index 索引
	 * @param channel 频率 ，定义见{@link IVIChannel}
	 * @return -1 失败
	 */
	public int setFavor(int index, IVIChannel channel){
		try {
			if (isActive())
				return mTuner.setFavor(index, channel);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取当前收音区域FM最小频点
	 * @param area 收音区域  定义见{@link IVITuner.Area}
	 * @return FM最小频点，失败返回-1
	 */
	public int getFmMin(int area){
		
		try {
			if (isActive())
				return mTuner.getFmMin(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取指定收音区域FM最大频点
	 * @param area 收音区域  定义见{@link IVITuner.Area}
	 * @return FM 最大频点，失败返回-1；
	 */
	public int getFmMax(int area){
		
		try {
			if (isActive())
				return mTuner.getFmMax(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取指定收音区域FM步进
	 * @param area 收音区域 定义见{@link IVITuner.Area}
	 * @return 失败返回-1
	 */
	public int getFmStep(int area){
		
		try {
			if (isActive())
				return mTuner.getFmStep(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取指定收音区域AM最小频率
	 * @param area 定义见{@link IVITuner.Area}
	 * @return 失败返回 -1；
	 */
	public int getAmMin(int area){
		try {
			if (isActive())
				return mTuner.getAmMin(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取指定收音区域AM最大频率
	 * @param area 定义见{@link IVITuner.Area}
	 * @return 失败返回 -1；
	 */
	public int getAmMax(int area){
		try {
			if (isActive())
				return mTuner.getAmMax(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取指定收音区域AM步进
	 * @param area 定义见{@link IVITuner.Area}
	 * @return 失败返回 -1；
	 */
	public int getAmStep(int area){
		try {
			if (isActive())
				return mTuner.getAmStep(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 打开收音机设备
	 * @return 失败返回 -1；
	 */
	public int open(){
		try {
			if (isActive())
				return mTuner.open();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 关闭收音机设备
	 * @return 失败返回 -1；
	 */
	public int close(){
		try {
			if (isActive())
				return mTuner.close();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 设置播放频率
	 * @param channel 电台 定义见{@link IVIChannel}
	 * @return 失败返回 -1；
	 */
	public int setFreq(IVIChannel channel){
		try {
			if (isActive() && channel != null)
				return mTuner.setFreq(channel);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取当前播放频率
	 * @return 失败返回null；
	 */
	public IVIChannel getFreq(){
		try {
			if (isActive())
				return mTuner.getFreq();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获取状态
	 * @return 失败返回 -1；
	 */
	public int getState(){
		try {
			if (isActive())
				return mTuner.getState();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	
	/**
	 * 上一个频点
	 * @return 失败返回 -1；
	 */
	public int seekUp(){
		try {
			if (isActive())
				return mTuner.seekUp();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 下一个频点
	 * @return 失败返回 -1；
	 */
	public int seekDown(){
		try {
			if (isActive())
				return mTuner.seekDown();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 上扫描
	 * @return 失败返回 -1；
	 */
	public int scanUp(){
		try {
			if (isActive())
				return mTuner.scanUp();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 下扫描
	 * @return 失败返回 -1；
	 */
	public int scanDown(){
		try {
			if (isActive())
				return mTuner.scanDown();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 开始扫描保存，
	 * @return 失败返回 -1；
	 */
	public int scanSave(){
		try {
			if (isActive())
				return mTuner.scanSave();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 停止搜索，包括scan up/down save
	 * @return 失败返回 -1；
	 */
	public int stop(){
		try {
			if (isActive())
				return mTuner.stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 判断当前播放的频率是否立体声电台
	 * @return 1为立体声， 0为 非立体声 ， -1 获取失败
	 */
	public int isStereo() {
		try {
			if (isActive())
				return mTuner.isStereo();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	/**
	 * 设置立体声开关
	 * @param on 1为打开 ， 0 为关闭
	 * @return
	 */
	public int setStereo(int on){
		try {
			if (isActive())
				return mTuner.setStereo(on);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	/**
	 * 设置远近程
	 * @param loc 1为近程， 0为远程
	 * @return -1 设置失败
	 */
	public int setLoc(int loc)  {
		try {
			if (isActive())
				return mTuner.setLoc(loc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
		
	}
	
	/**
	 * 获取电台列表
	 * @param band 波段 {@link  IVITuner.Band}
	 * @return 电台列表， 如果失败返回null
	 */
	public List<IVIChannel> getFavorLists(int band){
		try {
			if (isActive())
				return mTuner.getFavorLists(band);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 设置收音区域
	 * @param area {@link  IVITuner.Area}
	 * @return -1 表示失败， 成功返回设置的area
	 */
	public int setArea(int area){
		
		try {
			if (isActive())
				return mTuner.setArea(area);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取当前的收益区域
	 * @return 1 表示失败， 成功返回area {@link  IVITuner.Area}
	 */
	public int getArea(){
		
		try {
			if (isActive())
				return mTuner.getArea();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 监听服务绑定状态，绑定成功后才能成功调用其他接口
	 * @author itoday
	 * 
	 */
	public interface OnTunerListener {
		/**
		 * 服务状态变化，必须在active 为true时，才能调用接口，否则调用失败
		 * @param active
		 */
		void onActive(boolean active);
		
		/**
		 * 电台信号，扫描电台回调
		 * @param freq 频率
		 * @param band 波段  {@link  IVITuner.Band}
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
		 * @param newBand 波段 {@link  IVITuner.Band}
		 * @param oldFreq 
		 * @param oldBand 波段 {@link  IVITuner.Band}
		 */
		void onFreq(int newFreq, int newBand, int oldFreq, int oldBand);
		
		/**
		 * 服务控制界面退出，隐藏或显示
		 * @param cmd {@link IVITuner.RemoteCommand}
		 */
		void onRemoteCommand(int cmd);
		
		/**
		 * 频道列表变化通知
		 * @param band 波段 {@link  IVITuner.Band}
		 * @param favors 频道列表
		 * @param playIndex 当前播放的频率在列表中的位置， -1 表示不在列表中，
		 */
		void onFavorList(int band, List<IVIChannel> favors, int playIndex);
	}
}
