package com.itoday.ivi;

import android.view.KeyEvent;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;
/**
 * 锁屏状态，显示屏保，关触摸
 * @author itoday
 *
 */
public class LockScreenState extends CompositeState {

	public LockScreenState() {
		super("LockScreen", PRIORITY_NORMAL);
		
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_TOUCH, "LockScreen"));
	}

	@Override
	public boolean onKey(int key, int action) {
		
		switch (key) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_MEDIA_NEXT:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
		case KeyEvent.KEYCODE_POWER://解除锁屏
			return false; //此类按键不处理，交给其他状态处理
		default:
			break;
		}
		
		return true; //其他按键屏蔽掉
	}
	
	public boolean equals(CompositeState dest){
		
		if (dest instanceof LockScreenState){
			return super.equals(dest);
		}
		return false;
	}

}
