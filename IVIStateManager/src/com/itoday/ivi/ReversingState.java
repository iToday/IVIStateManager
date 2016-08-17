package com.itoday.ivi;

import android.view.KeyEvent;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;

/**
 * 倒车状态
 * @author itoday
 *
 */
public class ReversingState extends CompositeState {

	public ReversingState() {
		super("Reversing", PRIORITY_NORMAL);
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_MUTE, "Reversing"));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_SCREEN, "Reversing"));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_TOUCH, "Reversing"));
	}

	public boolean onKey(int key, int action){
		
		switch (key) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_MEDIA_NEXT:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			return false; //此类按键不处理，交给其他状态处理
		default:
			break;
		}
		
		return true; //其他按键屏蔽掉
	}
	
	public boolean equals(CompositeState dest){
		
		if (dest instanceof ReversingState){
			return super.equals(dest);
		}
		return false;
	}
}
