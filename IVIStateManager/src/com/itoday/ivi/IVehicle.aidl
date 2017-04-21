package com.itoday.ivi;

import com.itoday.ivi.IKeyListener;
import com.itoday.ivi.IUpgradeListener;

interface IVehicle{
	
	/**
	 * 静音控制
	 * @param on
	 * @return
	 */
	int setMute(boolean on);
	
	boolean isMute();
	
	/**
	 * 设置主音量
	 * @param volume
	 * @return
	 */
	int setMainVolume(int volume);
	
	int getMainVolume();
	
	/**
	 * 打开音频源
	 * @param source
	 * @return
	 */
	int openAudioSource(int source);
	
	/**
	*关闭音频源
	**/
	int closeAudioSource(int source);
	
	int getAudioSource();
	
	/**
	 * 设置灯光颜色
	 * @param color
	 * @return
	 */
	int setLightColor(int color);
	
	int getLightColor();
	
	/**
	 * 设置喇叭音量
	 * @param fr
	 * @param fl
	 * @param rr
	 * @param rl
	 * @return
	 */
	int setSpeakerVolume(int fr, int fl, int rr, int rl );
	
	int getFrSpeakerVolume();
	
	int getFlSpeakerVolume();
	
	int getRrSpeakerVolume();
	
	int getRlSpeakerVolume();
	
	/**
	 * 设置当前运行模块
	 * @param module
	 * @return
	 */
	int setRunning(int module);
	
	int getRunning();
	
	/**
	 * 设置设备电源
	 * @param dev
	 * @param state
	 * @return
	 */
	int setDevPower(int dev, int state);
	
	/**
	 * 按键学习
	 * @param key
	 * @return
	 */
	int setupKey(int key);
	
	/**
	 * 清楚按键
	 * @return
	 */
	int resetKeys();
	
	/**
	*查询按键的学习状态
	*/
	int requestKeysState();
	
	/**
	 * 向设备发送按键
	 * @param dev
	 * @param key
	 * @return
	 */
	int sendKeyToDev(int dev, int key);
	
	void registerKeySetupListener(in IKeyListener listener);
	
	void unregisterKeySetupListener(in IKeyListener listener);
	
	void registerUpgradeListener(in IUpgradeListener listener);
	
	void unregisterUpgradeListener(in IUpgradeListener listener);
	
	/**
	 * 获取设备版本
	 * @param dev
	 * @return
	 */
	int requestVersion(int dev);
	
	/**
	 * 设置升级状态
	 * @param state
	 * @return
	 */
	int setUpgrade(int state);
	
	/**
	 * 设置输入音量
	 * @param dev
	 * @param volume
	 * @return
	 */
	int setInputVolume(int dev, int volume);
	
	int getInputVolume(int dev);
	
	/**
	 * 设置低音增益
	 * @param subwoofer
	 * @return
	 */
	int setSubwooferGain(int subwoofer);
	
	int getSubwooferGain();
	
	/**
	 * 设置混音模式
	 * @param mix
	 * @return
	 */
	int setMixGain(int mix);
	
	int getMixGain();
	
	
	/**
	 * 设置低音增益
	 * @param gain
	 * @return
	 */
	int setBassGain(int band, int gain);
	
	int getBassGain(int band);
	
	/**
	 * 设置中音增益
	 * @param gain
	 * @return
	 */
	int setMidGain(int band, int gain);
	
	int getMidGain(int band);
	
	/**
	 * 设置高音增益
	 * @param gain
	 * @return
	 */
	int setTrebleGain(int band, int gain);
	
	int getTrebleGain(int band);
	
	/**
	 * 设置响度
	 * @param gain
	 * @return
	 */
	int setLoudness(int gain);
	
	int getLoudness();
	
	/**
	 * 同步时间
	 * @param time
	 * @return
	 */
	int setTime(int time);
	
	/**
	 * 获取时间
	 * @return
	 */
	int requestTimeSync();
	
	/**
	*MCU开始升级
	*/
	int startUpgrade(String path);
	
	/**
	*结束升级
	*/
	int endUpgrade();
	
	/**
	*设置系统属性
	*/
	int setSystemProperties(String name, String value);
	
	/**
	*获取系统属性
	*/
	String getSystemProperties(String name);
}