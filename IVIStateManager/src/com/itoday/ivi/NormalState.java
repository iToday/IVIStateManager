package com.itoday.ivi;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;

/**
 * 正常运行状态
 * @author itoday
 *
 */
public class NormalState extends CompositeState {

	public NormalState() {
		super("normal", PRIORITY_LOWEST);
		
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_LOWEST, AtomicState.TYPE_MUTE, "normal"));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_LOWEST, AtomicState.TYPE_SCREEN, "normal"));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_LOWEST, AtomicState.TYPE_TOUCH, "normal"));
	}

	public boolean onKey(int key, int action){
		
		return true;
	}
	
	public boolean equals(CompositeState dest){
		
		if (dest instanceof NormalState){
			return super.equals(dest);
		}
		return false;
	}
}
