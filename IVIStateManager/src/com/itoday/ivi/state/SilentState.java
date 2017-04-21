package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;
/**
 * 锁屏状态，显示屏保，关触摸
 * @author itoday
 * 一般显示时钟界面/频谱界面
 */
public class SilentState extends CompositeState {

	private static final String SILENT = "silent";

	/**
	 * 构造函数
	 */
	public SilentState(Handler handle) {
		super(handle, PRIORITY_LOWEST);
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_MUTE, SILENT));
	}

}
