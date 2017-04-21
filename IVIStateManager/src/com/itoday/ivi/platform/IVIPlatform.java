package com.itoday.ivi.platform;

/**
 * 平台需要具有下面功能接口
 * @author itoday
 * 音量
 * 背光
 * 触摸
 * 音效
 * 按键学习
 * 
 */
public abstract class IVIPlatform {
	
	protected OnIVIInfoChange iviListener;
	
	protected OnKeySetupListener keySetupListener;
	
	protected OnUpgradeInfo upgradeListener;//mcu upgrade
	
	public IVIPlatform(){

	}
	
	public void setVehicleInfoListener(OnIVIInfoChange listener){
		iviListener = listener;
	}
	
	public void setKeySetupListener(OnKeySetupListener listener){
		keySetupListener = listener;
	}
	
	public void setUpgradeListener(OnUpgradeInfo listener){
		upgradeListener = listener;
	}
	
	/**
	 * 平台初始化
	 * @return
	 */
	public abstract int init();
	
	/**
	 * 平台释放
	 * @return
	 */
	public abstract int uninit();
	
	/**
	 * 开关触摸及触摸模式
	 * @param state
	 * @return
	 */
	public abstract int setTouch(int state);
	
	/**
	 * 开关背光
	 * @param state
	 * @return
	 */
	public abstract int setBacklight(int state);
	
	/**
	 * 设置昼夜模式
	 * @param level
	 * @return
	 */
	public abstract int setDayOrNight(boolean day);
	/**
	 * 静音控制
	 * @param on
	 * @return
	 */
	public abstract int setMute(boolean on);
	
	/**
	 * 设置主音量
	 * @param volume
	 * @return
	 */
	public abstract int setMainVolume(int volume);
	
	/**
	 * 设置音频源
	 * @param source
	 * @return
	 */
	public abstract int setAudioSource(int source);
	
	/**
	 * 设置灯光颜色
	 * @param color
	 * @return
	 */
	public abstract int setLightColor(int color);
	
	/**
	 * 设置喇叭音量
	 * @param fr
	 * @param fl
	 * @param rr
	 * @param rl
	 * @return
	 */
	public abstract int setSpeakerVolume(int fr, int fl, int rr, int rl );
	
	/**
	 * 设置当前运行模块
	 * @param module
	 * @return
	 */
	public abstract int setRunning(int module);
	
	/**
	 * 设置设备电源
	 * @param dev
	 * @param state
	 * @return
	 */
	public abstract int setDevPower(int dev, int state);
	
	/**
	 * 按键学习
	 * @param key
	 * @return
	 */
	public abstract int setupKey(int key);
	
	/**
	 * 清楚按键
	 * @return
	 */
	public abstract int resetKeys();
	
	/**
	 * 清楚按键
	 * @return
	 */
	public abstract int requestKeysState();
	
	/**
	 * 向设备发送按键
	 * @param dev
	 * @param key
	 * @return
	 */
	public abstract int sendKeyToDev(int dev, int key);
	
	/**
	 * 获取设备版本
	 * @param dev
	 * @return
	 */
	public abstract int requestVersion(int dev);
	
	/**
	 * 设置升级状态
	 * @param state
	 * @return
	 */
	public abstract int setUpgrade(int state);
	
	/**
	 * 设置输入音量
	 * @param dev
	 * @param volume
	 * @return
	 */
	public abstract int setInputVolume(int dev, int volume);
	
	/**
	 * 初始化输入增益
	 * @param InputVolume
	 */
	public abstract void initInputVolume(int[] InputVolume);
	
	/**
	 * 设置低音增益
	 * @param subwoofer
	 * @return
	 */
	public abstract int setSubwooferGain(int subwoofer);
	
	/**
	 * 设置混音模式
	 * @param mix
	 * @return
	 */
	public abstract int setMixGain(int mix);
	
	/**
	 * 设置低音Q值
	 * @param bass
	 * @return
	 */
	public abstract int setBassQ(int bass);
	
	/**
	 * 设置中音Q值
	 * @param mid
	 * @return
	 */
	public abstract int setMidQ(int mid);
	
	/**
	 * 设置高音Q值
	 * @param treble
	 * @return
	 */
	public abstract int setTrebleQ(int treble);
	
	/**
	 * 设置低音增益
	 * @param gain
	 * @return
	 */
	public abstract int setBassGain(int gain);
	
	/**
	 * 设置中音增益
	 * @param gain
	 * @return
	 */
	public abstract int setMidGain(int gain);
	
	/**
	 * 设置高音增益
	 * @param gain
	 * @return
	 */
	public abstract int setTrebleGain(int gain);
	
	/**
	 * 设置响度
	 * @param gain
	 * @return
	 */
	public abstract int setLoudness(int gain);
	
	/**
	 * 同步时间
	 * @param time
	 * @return
	 */
	public abstract int setTime(int time);
	
	/**
	 * 获取时间
	 * @return
	 */
	public abstract int requestTimeSync();
	
	
	public abstract int startUpgrade(String path);
	
	public abstract int endUpgrade();
	
	/**
	 * 按键学习
	 * @author itoday
	 *
	 */
	public interface OnKeySetupListener{
		int onKeySetupResult(int adc1, int adc2, int adc3);
		int onKeySetupStatus(int status);
	}
	
	/**
	 * 系统基本状态
	 * @author itoday
	 *
	 */
	public interface OnIVIInfoChange{
		//原车状态
		int onDevice(int dev, int state);
		
		//原车按键
		boolean onKey(int key, int state, int step);
		
		//CANBUS数据
		int onCanInfo(int param, String info);
		
		/**
		 * 通话状态
		 * @param state
		 * @return
		 */
		int onPhoneTalkStateChange(int state);
		
		/**
		 * 导航播报状态
		 * @param state
		 * @return
		 */
		int onNaviStateChange(int state);
		
		/**
		 * 语音识别状态
		 * @param state
		 * @return
		 */
		int onVoiceStateChange(int state);
		
		/**
		 * 待机状态
		 * @param state
		 * @return
		 */
		int onStandbyStateChange(int state);

		/**
		 * 静音状态
		 * @param state
		 * @return
		 */
		int onSilentChange(boolean state);
	}
	
	public interface OnUpgradeInfo{
		/**
		 * MCU 升级
		 * @param state
		 * @param progress
		 */
		void onUpgrade(int state, int progress);
	}
}
