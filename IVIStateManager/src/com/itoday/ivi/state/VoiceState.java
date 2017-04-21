package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.state.base.CompositeState;

public class VoiceState extends CompositeState {
	
	private static final String VOICE = "voice";

	public VoiceState(Handler handle) {
		super(handle, PRIORITY_LOW);
		
	}

	@Override
	public boolean onKey(int key, int action, int step) {
		//按键复用，电话接听，挂断按键控制语音唤醒/结束
		switch (key) {
		case IVIKeyEvent.KEYCODE_ANSWER:
			
			return true;
		case IVIKeyEvent.KEYCODE_ENDCALL:
			
			return true;
		default:
			break;
		}
		return super.onKey(key, action, step);
	}
	
	

}
