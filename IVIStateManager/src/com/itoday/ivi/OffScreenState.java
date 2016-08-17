package com.itoday.ivi;

import android.view.KeyEvent;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;

/**
 * 关屏状态
 * @author itoday
 *
 */
public class OffScreenState extends CompositeState {

	public OffScreenState() {
		super("OffScreen", PRIORITY_NORMAL);
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_LOW, AtomicState.TYPE_SCREEN, "OffScreen"));
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
		 //解除关屏状态
		
		return true; //其他按键解除关屏状态
		
	}
	
	public boolean equals(CompositeState dest){
		
		if (dest instanceof OffScreenState){
			return super.equals(dest);
		}
		return false;
	}
}
