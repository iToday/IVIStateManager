package com.itoday.ivi;
import com.itoday.ivi.platform.IVIChannel;
interface ITunerListener{
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
		
		/**
		*频道列表变化通知
		*
		*/
		void onFavorList(int band, in List<IVIChannel> favors, int playingIndex);
		
		/**
		*服务控制界面的命令，只可以实现简单的工作
		*/
		void onCommand(int cmd);
}