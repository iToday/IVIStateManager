package com.itoday.ivi;

import com.itoday.ivi.state.CompositeState;

public class AlarmState extends CompositeState {

	public AlarmState(String from, int keyPriority) {
		super(from, keyPriority);
	}

	@Override
	public boolean onKey(int key, int action) {
		//调节音量时调节报警音量
		//报警时不能静音
		return super.onKey(key, action);
	}
}
