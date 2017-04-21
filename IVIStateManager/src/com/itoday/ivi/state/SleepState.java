package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;

/**
 * acc off status
 * @author iToday
 *
 */
public class SleepState extends CompositeState {
	
	private static final String SLEEP = "sleep";

	public SleepState(Handler handle) {
		super(handle, PRIORITY_HIGH);
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_MUTE, SLEEP));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_SCREEN, SLEEP));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_TOUCH, SLEEP));
	}

	@Override
	public boolean onKey(int key, int action, int step) {
		
		return true;//屏蔽所有按键
	}
	
}
