package com.itoday.ivi;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;

/**
 * 待机状态，关屏/声音/触摸
 * @author itoday
 *
 */
public class StandbyState extends CompositeState {

	public StandbyState() {
		super("standby", PRIORITY_HIGH );
		
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_MUTE, "standby"));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_SCREEN, "standby"));
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_TOUCH, "standby"));
	}

	public boolean onKey(int key, int action){
		
		return true;//屏蔽所有按键
	}
	
	public boolean equals(CompositeState dest){
		
		if (dest instanceof StandbyState){
			return super.equals(dest);
		}
		
		return false;
	}
}
