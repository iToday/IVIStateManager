package com.tomwin.tpms;

import android.database.ContentObserver;
import android.os.Handler;

/**
 * 整数数据变化监听器
 * @author itoday
 * 用来监听系统数据库中整数数据变化
 * 在使用完成后必须调用release,释放资源
 */
public class IntObserver extends ContentObserver {
	
	private IVIDataManager dataManager;
	
	private OnIntDataChange listener;
	
	private int last = 0;
	
	private String mName;
	
	/**
	 * 构造函数
	 * @param name 数据对应的KEY值
	 */
	public IntObserver(String name) {
		super(new Handler());
		
		mName = name;
		
		dataManager = IVIDataManager.instance();
		dataManager.registerDataChange(name, this);
		
	}
	
	@Override
	public void onChange(boolean selfChange) {
		
		super.onChange(selfChange);
		
		int state = dataManager.getInt(mName, -1);
		
		if (last != state){
			
			if (listener != null)
				listener.onIntDataChange(state, last);
			
			last = state;
		}
		
	}
	
	/**
	 * 注册数据变化监听
	 * @param listener
	 */
	public void registerDataChangeListener(OnIntDataChange listener){
		this.listener = listener;
	}
	
	/**
	 * 释放资源，在不使用的时候，必须要释放
	 */
	public void release(){
		
		if (dataManager != null)
			dataManager.unregisterDataChange(this);
	}
	
	/**
	 * 获取键值
	 * @param def 默认值
	 * @return 失败返回默认值
	 */
	public int getValue(int def){
		
		if (dataManager != null)
			return dataManager.getInt(mName, def);
		
		return def;
	}
	
	/**
	 * 设置键值
	 * @param value
	 */
	public void setValue(int value){
		
		if (dataManager != null)
			 dataManager.putInt(mName, value);
	}

	/**
	 * 回调接口
	 * @author itoday
	 *
	 */
	public interface OnIntDataChange{
		int onIntDataChange(int newState, int oldState);
	}

}
