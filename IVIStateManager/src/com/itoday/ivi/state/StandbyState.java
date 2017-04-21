package com.itoday.ivi.state;

import android.os.Handler;
import android.view.KeyEvent;

import com.itoday.ivi.data.IntObserver;
import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;


/**
 * 待机状态，关声音,关屏，不关触摸
 * @author itoday
 * power into standby
 */
public class StandbyState extends CompositeState {

	private static final String STANDBY = "standby";

	/**
	 * 构造函数
	 */
	public StandbyState(Handler handle) {
		super(handle, PRIORITY_HIGH );
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_MUTE, STANDBY));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_SCREEN, STANDBY));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_TOUCH, STANDBY));
	}

	@Override
	public boolean onKey(int key, int action, int step){
		
		switch(key){
		case IVIKeyEvent.KEYCODE_POWER:
		case IVIKeyEvent.KEYCODE_MUTE:
			
			if (action != KeyEvent.ACTION_DOWN){
				IntObserver standby = new IntObserver("standby");
				standby.setValue(IVIDevice.OFF);
			}
			
			break;
		default:
			break;
		}
		return true;//屏蔽所有按键
	}
	
}
