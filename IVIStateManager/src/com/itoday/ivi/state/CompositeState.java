package com.itoday.ivi.state;

import java.util.ArrayList;


/**
 * 复合状态，如电话/倒车/待机等
 * 复合状态包括一个或多个AtomicState
 * @author itoday
 *
 */
public class CompositeState extends Object {
	
	/**
	 * 按键优先级
	 */
	public static final int PRIORITY_HIGH = 0;
	
	public static final int PRIORITY_NORMAL = 1;
	
	public static final int PRIORITY_LOW = 2;
	
	public static final int PRIORITY_LOWEST = 3;
	
	protected  ArrayList<AtomicState> astates = new ArrayList<AtomicState>();
	
	private int keyPriority;
	
	public CompositeState( String from, int keyPriority){
		this.keyPriority = keyPriority;
	}
	
	public AtomicState getAtomicStateByType(String type){
		
		for (AtomicState state : astates) {
			if (state.isType(type))
				return state;
		}
		
		return null;
	}
	
	/**
	 * 处理按键
	 * @param key
	 * @param action
	 * @return true 表示按键已处理，false 表示按键未处理
	 */
	public boolean onKey(int key, int action){
		return true;
	}
	
	public boolean comparePriority(CompositeState dest){
		return dest != null && keyPriority > dest.keyPriority;
	}
	
	public boolean equals(CompositeState dest){
		
		if (this.keyPriority == dest.keyPriority && astates.size() == dest.astates.size()){
			for (int index = 0; index < astates.size(); index ++) {

				if (!astates.get(index).equals(dest.astates.get(index)))
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [astates=" + astates + ", keyPriority=" 	+ keyPriority + "]";
	}
	
	
}
