package com.itoday.ivi.vehicle;

import com.itoday.ivi.platform.IVIPlatform;

public class KeyManager {
	
	private IVIPlatform mPlatform;
	
	public KeyManager(IVIPlatform platform){
		mPlatform = platform;
	}
	
	/**
	 * 按键学习
	 * @param key
	 * @return
	 */
	public  int setupKey(int key){
		return mPlatform.setupKey(key);
	}
	
	/**
	 * 清楚按键
	 * @return
	 */
	public  int resetKeys(){
		return mPlatform.resetKeys();
	}
	
	/**
	 * 查询案件的学习状态
	 * @return
	 */
	public int requestKeysState(){
		return mPlatform.requestKeysState();
	}
	/**
	 * 向设备发送按键
	 * @param dev
	 * @param key
	 * @return
	 */
	public  int sendKeyToDev(int dev, int key){
		return mPlatform.sendKeyToDev(dev, key);
	}
	
}
