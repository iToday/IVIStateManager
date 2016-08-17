package com.itoday.ivi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;

import com.itoday.ivi.state.AtomicState;
import com.itoday.ivi.state.CompositeState;

@SuppressWarnings("rawtypes")
public class CarStateMachine implements Comparator{
	
	private static final String tag = "CarStateMachine";
	
	private ArrayList<CompositeState> cstates = new ArrayList<CompositeState>();
	
	private ArrayList<CompositeState> sortCSS = new ArrayList<CompositeState>();
	
//	private MuteState mute = new MuteState();
	
	private NormalState normal = new NormalState();
	
	private OffScreenState offscreen = new OffScreenState();
	
	private PhoneSate phone = new PhoneSate();
	
	private ReversingState reversing = new ReversingState();
	
	private StandbyState standby = new StandbyState();
	
	private LockScreenState lockScreen = new LockScreenState();
	
	private static final boolean DEBUG = true;
	
	private Thread thread = new Thread(new Runnable() {
		
		private AtomicState lastMute;
		
		private AtomicState lastScreen;
		
		private AtomicState lastTouch;
		
		@Override
		public void run() {
			while (true) {
				
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				AtomicState asMute;
				AtomicState asScreen;
				AtomicState asTouch;
				
				synchronized (this) {
					asMute = getTargetState(AtomicState.TYPE_MUTE);
					asTouch = getTargetState(AtomicState.TYPE_TOUCH);
					asScreen = getTargetState(AtomicState.TYPE_SCREEN);
				}
				
				if (asMute != null && !asMute.equalsState(lastMute)){
					
					if (DEBUG)
						Log.d(tag, "thread run: mute :" + asMute);
					
					;//设置静音状态,所有声音静音，调用硬件静音
					lastMute = asMute;
				}
				
				if (asScreen != null &&  !asScreen.equalsState(lastScreen)){
					
					if (DEBUG)
						Log.d(tag, "thread run: screen :" + asScreen);
					
					;//设置屏幕开关
					lastScreen = asScreen;
				}
				
				if (asTouch != null && !asTouch.equalsState(lastTouch)){
					
					if (DEBUG)
						Log.d(tag, "thread run: touch :" + asTouch);
					
					;//设置触摸状态
					lastTouch = asTouch;
				}
			}
		}
	}, "CarStateMachine");
	
	public CarStateMachine(){
		
		add(normal);
		thread.start();
		thread.notify();
	}
	
	/**
	 * 设置静音状态
	 * @param on 
	 */
	/*public void setMute(boolean on){
		
		if (on){
			if (add(mute))
				thread.notify();
		} else {
			if (remove(mute))
				thread.notify();
		}
	}*/
	
	/**
	 * 设置关屏状态
	 * @param on
	 */
	public void setOffScreen(boolean on){
		
		if (on){
			if (add(offscreen))
				thread.notify();
		} else {
			if (remove(offscreen))
				thread.notify();
		}
	}
	
	/**
	 * 设置电话状态
	 * @param on
	 */
	public void setPhone(boolean on){
		
		if (on){
			if (add(phone))
				thread.notify();
		} else {
			if (remove(phone))
				thread.notify();
		}
	}
	
	/**
	 * 设置倒车状态
	 * @param on
	 */
	public void setReversing(boolean on){
		
		if (on){
			if (add(reversing))
				thread.notify();
		} else {
			if (remove(reversing))
				thread.notify();
		}
	}
	
	/**
	 * 设置待机状态
	 * @param on
	 */
	public void setStandby(boolean on){
		
		if (on){
			if (add(standby))
				thread.notify();
		} else {
			if (remove(standby))
				thread.notify();
		}
	}
	
	/**
	 * 设置锁屏状态
	 */
	public void setLockScreen(boolean on){
		if (on){
			if (add(lockScreen))
				thread.notify();
		} else {
			if (remove(lockScreen))
				thread.notify();
		}
	}
	
	/**
	 * 处理按键
	 * 处理流程如下：
	 * 先交给优先级最高的状态处理，如果最高优先级状态已处理直接返回，若未处理，再交给次优先级状态处理。。。依次类推
	 * 直到有一种状态处理了按键或全部未处理，则返回
	 * @param key 按键值
	 * @param action 按键状态
	 * @return true 表示按键已处理，false 表示按键未处理
	 */
	@SuppressWarnings("unchecked")
	public boolean onKey(int key, int action){
		
	/*	CompositeState cs = getTopProrityCompositeState();
		
		if (cs != null)
			return cs.onKey(key, action);*/
		
		if (DEBUG)
			Log.d(tag, " onKey  :: " + sortCSS.toString());
		
		synchronized (this) {
			for (int i = 0; i < sortCSS.size(); i++) {
				if (sortCSS.get(i).onKey(key, action))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean add(CompositeState state){
		
		synchronized(this){
			
			if (DEBUG)
				Log.d(tag, " add before :: " + cstates.toString());
			
			if (cstates.contains(state))
				return false;
			
			boolean res =  cstates.add(state);
			
			if (res)
				updateSortCompositeStatesByPrority();
			
			if (DEBUG)
				Log.d(tag, " add after :: " + cstates.toString());
			
			return res;
		}
	}
	
	private boolean remove(CompositeState state){
		
		synchronized (this) {
			if (!cstates.contains(state))
				Log.d(tag, "cstates no state " + state);
			
			boolean res = cstates.remove(state);
			
			if (res){//可优化，删除不需要重新排序
				//updateSortCompositeStatesByPrority();
				synchronized (this) {
					for (CompositeState cs : sortCSS) {
						if (cs.equals(state)){
							sortCSS.remove(cs);
							break;
						}
					}
				}
			}
			
			return res;
		}
	}
	
	/**
	 * 获取当前优先级最高的CompositeState
	 * 如果存在相同优先级，则取后添加的状态
	 * @return 
	 */
	@SuppressWarnings("unused")
	private CompositeState getTopProrityCompositeState(){
		
		CompositeState csTop = null;
		
		if (DEBUG)
			Log.d(tag, " getTopProrityCompositeState :: " + cstates.toString());
		
		synchronized (this) {
			for (int i = 0; i < cstates.size(); i++) {
				CompositeState cs = cstates.get(i);
				
				if (csTop == null)
					csTop = cs;
				else 	if (cs.comparePriority(csTop))
					csTop = cs;
			}
		}
		
		if (DEBUG)
			Log.d(tag, csTop.toString());
		
		return csTop;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<CompositeState> updateSortCompositeStatesByPrority(){
		
		synchronized (this) {
			sortCSS.clear();
			sortCSS = (ArrayList<CompositeState>) cstates.clone();
			Collections.sort(sortCSS, new CarStateMachine());
		}
		
		if (DEBUG)
			Log.d(tag, " updateSortCompositeStatesByPrority :: " + sortCSS.toString());
		
		return sortCSS;
	}
	
	/**
	 * 获取当前优先级最高的AtomicState
	 * 如果存在相同优先级，则取后添加的状态
	 * @param type
	 * @return
	 */
	private AtomicState getTargetState(String type){
		
		AtomicState atomicState = null;
		
		if (DEBUG)
			Log.d(tag, type + " :: " + cstates.toString());
			
		for (int i = cstates.size() -1; i >= 0; i--) {
			CompositeState cs = cstates.get(i);
			AtomicState as = cs.getAtomicStateByType(type);
			
			if (as == null)
				continue;
			
			if (atomicState == null){
				atomicState = as;
			}else {
				if (!atomicState.comparePriority(as))
					atomicState = as;
			}
		}
		return atomicState;
	}

	@Override
	public int compare(Object lhs, Object rhs) {
		CompositeState left = (CompositeState) lhs;
		CompositeState right = (CompositeState) rhs;
		
		return left.comparePriority(right) ? 1 : 0;
	}
}
