package com.itoday.ivi;

import com.itoday.ivi.state.CompositeState;

public class NaviBroadcastState extends CompositeState {

	public NaviBroadcastState(String from, int keyPriority) {
		super(from, keyPriority);
	}

	@Override
	public boolean onKey(int key, int action) {
		//调节音量时调节导航音量
		return super.onKey(key, action);
	}

}
