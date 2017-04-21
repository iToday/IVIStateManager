package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;

/**
 * 倒车状态
 * @author itoday
 * 倒车状态时，其他没有中断或继续两种状态
 */
public class ReversingState extends CompositeState {

	private static final String REVERSING = "Reversing";

	/**
	 * 构造函数
	 */
	public ReversingState(Handler handle) {
		super(handle, PRIORITY_NORMAL);
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_MUTE, REVERSING));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_SCREEN, REVERSING));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_TOUCH, REVERSING));
	}

	@Override
	public boolean onKey(int key, int action, int step){
		return true; //其他按键屏蔽掉
	}
}
