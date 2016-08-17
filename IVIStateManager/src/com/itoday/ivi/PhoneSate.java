package com.itoday.ivi;

import android.view.KeyEvent;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;

/**
 * 电话状态
 * @author itoday
 *
 */
public class PhoneSate extends CompositeState {
	
	public PhoneSate() {
		super("Phone", PRIORITY_HIGH);
		//this can load from config file
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_MUTE, "Phone"));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_SCREEN, "Phone"));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_TOUCH, "Phone"));
	}

	public boolean onKey(int key, int action){
		switch (key) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_ENDCALL:
		case KeyEvent.KEYCODE_CALL:
			//break; //电话时只处理这些按键,其他按键屏蔽
		default:
			break;
		}
		
		return true;
	}
	
	public boolean equals(CompositeState dest){
		
		if (dest instanceof PhoneSate){
			return super.equals(dest);
		}
		return false;
	}
	
}