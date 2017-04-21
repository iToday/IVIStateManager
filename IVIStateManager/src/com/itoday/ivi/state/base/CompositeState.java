package com.itoday.ivi.state.base;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


/**
 * 复合状态，如电话/倒车/待机等
 * 复合状态包括一个或多个AtomicState
 * @author itoday
 *
 */
public class CompositeState extends Object {
	
	/**
	 * 按键优先级
	 */
	public static final int PRIORITY_HIGH = 0;
	
	public static final int PRIORITY_NORMAL = 1;
	
	public static final int PRIORITY_LOW = 2;
	
	public static final int PRIORITY_LOWEST = 3;
	
	public static final int MSG_START_ACTIVITY = 1;
	
	public static final int MSG_SCREEN_DISPLAY = 2;
	
	public static final int MSG_SRC_MODE = 3;
	
	public static final int MSG_MEDIA_MODE = 4;
	
	public static final int MSG_SEND_BROADCAST = 5;
	
	protected  ArrayList<AtomicState> astates = new ArrayList<AtomicState>();
	
	private int keyPriority;
	
	private Handler mHandle;
	
	/**
	 * 构造函数
	 * @param from
	 * @param keyPriority
	 */
	public CompositeState( Handler handle, int keyPriority){
		this.keyPriority = keyPriority;
		mHandle = handle;
	}
	
	/**
	 * 获取原子状态
	 * @param type
	 * @return
	 */
	public AtomicState getAtomicStateByType(String type){
		
		for (AtomicState state : astates) {
			if (state.isType(type))
				return state;
		}
		
		return null;
	}
	
	/**
	 * 处理按键
	 * @param key
	 * @param action
	 * @param step 
	 * @return true 表示按键已处理，false 表示按键未处理
	 */
	public boolean onKey(int key, int action, int step){
		return false;
	}
	
	/**
	 * 比较按键优先级
	 * @param dest
	 * @return 优先级大于返回true
	 */
	public boolean comparePriority(CompositeState dest){
		return dest != null && keyPriority < dest.keyPriority;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o == null || !(o instanceof CompositeState))
			return false;
		
		CompositeState dest = (CompositeState) o;
		
		if (this.keyPriority == dest.keyPriority 
				&& astates.size() == dest.astates.size()){
			for (int index = 0; index < astates.size(); index ++) {

				if (!astates.get(index).equals(dest.astates.get(index)))
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [astates=" + astates + ", keyPriority=" 	+ keyPriority + "] \n";
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
		
	public void startActivityWithAction(String action){
		
		Message msg = mHandle.obtainMessage(CompositeState.MSG_START_ACTIVITY);
		msg.obj = action;
		mHandle.sendMessage(msg);
	}
	
	public void setScreen(boolean on){
		
		Message msg = mHandle.obtainMessage(CompositeState.MSG_SCREEN_DISPLAY);
		msg.arg1 = on ? 1 : 0;
		mHandle.sendMessage(msg);
	}
	
	public void startSrcMode(){
		
		Message msg = mHandle.obtainMessage(CompositeState.MSG_SRC_MODE);
		mHandle.sendMessage(msg);
	}
	
	public void startMediaMode(){
		
		Message msg = mHandle.obtainMessage(CompositeState.MSG_MEDIA_MODE);
		mHandle.sendMessage(msg);
	}
	
	public void sendBroadcast(String action, String key, int value){
		
		Message msg = mHandle.obtainMessage(CompositeState.MSG_SEND_BROADCAST);
		
		Bundle bundle = new Bundle();
		bundle.putString("action", action);
		bundle.putString("key", key);
		bundle.putInt("value", value);
		
		msg.setData(bundle);
		
		mHandle.sendMessage(msg);
	}
	
}
