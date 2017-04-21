package com.itoday.ivi.vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.os.Handler;
import android.util.Log;

import com.itoday.ivi.platform.IVIPlatform;
import com.itoday.ivi.state.LockScreenState;
import com.itoday.ivi.state.NaviState;
import com.itoday.ivi.state.NormalState;
import com.itoday.ivi.state.OffScreenState;
import com.itoday.ivi.state.PhoneState;
import com.itoday.ivi.state.ReversingState;
import com.itoday.ivi.state.SilentState;
import com.itoday.ivi.state.SleepState;
import com.itoday.ivi.state.StandbyState;
import com.itoday.ivi.state.VoiceState;
import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;


/**
 * 这是状态机逻辑实现部分
 * @author itoday
 *
 */
@SuppressWarnings("rawtypes")
class StateMachine implements Comparator{
	
	private static final String TAG = "CarStateMachine";
	
	private ArrayList<CompositeState> cstates = new ArrayList<CompositeState>();
	
	private ArrayList<CompositeState> sortCSS = new ArrayList<CompositeState>();
	
	private NormalState normal;
	
	private OffScreenState offscreen;
	
	private PhoneState phone;
	
	private ReversingState reversing;
	
	private StandbyState standby;
	
	private LockScreenState lockScreen;
	
	private SleepState sleep;
	
	private NaviState navi;
	
	private VoiceState voice;
	
	private SilentState silent;
	
	private Thread thread = new Thread(new StateEnage());
	
	private IVIPlatform mPlatform;
	
	private boolean runable = true;
	
	private static final boolean DEBUG = true;
	
	private class StateEnage implements Runnable {
		
		private AtomicState lastMute;
		
		private AtomicState lastScreen;
		
		private AtomicState lastTouch;
		
		@Override
		public void run() {
			while (runable) {
				
				update();
				
				try {
					synchronized (cstates) {
						cstates.wait();
					}
				} catch (InterruptedException e) {
					Log.w(TAG, "Interrupted: " + e);
					
					Thread.currentThread().interrupt();
				}
				
			}
		}
		
		private void update() {
			AtomicState asMute;
			AtomicState asScreen;
			AtomicState asTouch;
			
			synchronized (cstates) {
				asMute = getTargetState(AtomicState.TYPE_MUTE);
				asTouch = getTargetState(AtomicState.TYPE_TOUCH);
				asScreen = getTargetState(AtomicState.TYPE_SCREEN);
			}
			
			if (DEBUG)
				Log.d(TAG, "thread run: mute :" + asMute + "   \n last mute: " + lastMute);
			
			if (asMute != null && !asMute.equalsState(lastMute)){
				//设置静音状态,所有声音静音，调用硬件静音
				mPlatform.setMute(asMute.getState() == AtomicState.ON);
				lastMute = asMute;
			}
			
			if (DEBUG)
				Log.d(TAG, "thread run: screen :" + asScreen + "   \n last screen: " + lastScreen);
			
			if (asScreen != null &&  !asScreen.equalsState(lastScreen)){
				
				//设置屏幕开关
				mPlatform.setBacklight(asScreen.getState());
				lastScreen = asScreen;
			}
			
			if (asTouch != null && !asTouch.equalsState(lastTouch)){
				
				if (DEBUG)
					Log.d(TAG, "thread run: touch :" + asTouch);
				
				//设置触摸状态
				mPlatform.setTouch(asTouch.getState());
				lastTouch = asTouch;
			}
		}
	}
	
	/**
	 * 状态机构造函数
	 */
	public StateMachine(IVIPlatform platform, Handler handle){
		
		mPlatform = platform;
		
		normal = new NormalState(handle);
		
		offscreen = new OffScreenState(handle);
		
		phone = new PhoneState(handle);
		
		reversing = new ReversingState(handle);
		
		standby = new StandbyState(handle);
		
		lockScreen = new LockScreenState(handle);
		
		sleep = new SleepState(handle);
		
		navi = new NaviState(handle);
		
		voice = new VoiceState(handle);
		
		silent = new SilentState(handle);
		
		cstates.add(normal);
		sortCSS.add(normal);
		
		thread.start();
		
	}
	
	/**
	 * 设置关屏状态
	 * @param onon
	 */
	public void setOffScreen(boolean on){
		
		Log.d(TAG, "setOffScreen " + on);
		
		if (on){
			add(offscreen);
		} else {
			remove(offscreen);
		}
	}
	
	/**
	 * 设置安静模式
	 * @param on
	 */
	public void setSilent(boolean on){
		
		Log.d(TAG, "setSilent " + on);
		
		if (on)
			add(silent);
		else
			remove(silent);
	}
	
	/**
	 * 设置电话状态
	 * @param on
	 */
	public void setPhone(boolean on){
		
		Log.d(TAG, "setPhone " + on);
		
		if (on){
			add(phone);
		} else {
			remove(phone);
		}
	}
	
	/**
	 * 设置倒车状态
	 * @param on
	 */
	public void setReversing(boolean on){
		
		Log.d(TAG, "setReversing " + on);
		
		if (on){
			add(reversing);
		} else {
			remove(reversing);
		}
	}
	
	/**
	 * 设置待机状态
	 * @param on
	 */
	public void setStandby(boolean on){
		
		Log.d(TAG, "setStandby " + on);
		
		if (on){
			add(standby);
		} else {
			remove(standby);
		}
	}
	
	public void setSleep(boolean on){
		
		Log.d(TAG, "setSleep " + on);
		
		if (on){
			add(sleep);
		} else {
			remove(sleep);
		}
	}
	
	/**
	 * 设置锁屏状态
	 */
	public void setLockScreen(boolean on){
		if (on){
			add(lockScreen);
		} else {
			remove(lockScreen);
		}
	}
	
	/**
	 * 设置语音识别状态
	 * @param on
	 */
	public void setVoice(boolean on){
		if (on){
			add(voice);
		} else {
			remove(voice);
		}
	}
	
	/**
	 * 设置导航播报状态
	 * @param on
	 */
	public void setNavi(boolean on){
		if (on){
			add(navi);
		} else {
			remove(navi);
		}
	}
	
	/**
	 * 处理按键
	 * 处理流程如下：
	 * 先交给优先级最高的状态处理，如果最高优先级状态已处理直接返回，若未处理，再交给次优先级状态处理。。。依次类推
	 * 直到有一种状态处理了按键或全部未处理，则返回
	 * @param key 按键值
	 * @param action 按键状态
	 * @param step 
	 * @return true 表示按键已处理，false 表示按键未处理
	 */
	public boolean onKey(int key, int action, int step){
		
		synchronized (this) {
			for (int i = 0; i < sortCSS.size(); i++) {
				if (DEBUG)
					Log.d(TAG, " onKey  :: " + sortCSS.get(i).toString());
				if (sortCSS.get(i).onKey(key, action, step))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean add(CompositeState state){
		
		if (DEBUG)
			Log.d(TAG, " add before :: " + cstates.toString());
		
		if (cstates.contains(state))
			return false;
		
		synchronized(cstates){
		
			
			if (cstates.add(state)){
				
				updateSortCompositeStatesByPrority();
				
				cstates.notifyAll();
				
				return true;
			}
		}
		
		if (DEBUG)
			Log.d(TAG, " add after :: " + cstates.toString());
		
		return false;
	}
	
	private boolean remove(CompositeState state){
		
		if (!cstates.contains(state))
			Log.d(TAG, "cstates no state " + state);
		
		synchronized (cstates) {
			
		
		if (cstates.remove(state)){
			
				for (CompositeState cs : sortCSS) {
					if (cs.equals(state)){
						sortCSS.remove(cs);
						break;
					}
				}
				
				synchronized(cstates){
					cstates.notifyAll();
				}
				
				return true;
			}
		}
		
		return false;
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
			Log.d(TAG, " getTopProrityCompositeState :: " + cstates.toString());
		
		synchronized (this) {
			for (int i = 0; i < cstates.size(); i++) {
				CompositeState cs = cstates.get(i);
				
				if (csTop == null || cs.comparePriority(csTop))
					csTop = cs;
			}
		}
		
		if (DEBUG)
			Log.d(TAG, "top composite state: " + csTop);
		
		return csTop;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<CompositeState> updateSortCompositeStatesByPrority(){
		
		synchronized (this) {
			sortCSS.clear();
			sortCSS = (ArrayList<CompositeState>) cstates.clone();
			Collections.sort(sortCSS, this);
		}
		
		if (DEBUG)
			Log.d(TAG, " updateSortCompositeStatesByPrority :: " + sortCSS.toString());
		
		return sortCSS;
	}
	
	/**
	 * 获取当前优先级最高的AtomicState
	 * 如果存在相同优先级，则取后添加的状态
	 * @param type
	 * @return
	 */
	public AtomicState getTargetState(String type){
		
		AtomicState atomicState = null;
		
		if (DEBUG)
			Log.d(TAG, type + " :: " + cstates.toString());
			
		for (int i = cstates.size() -1; i >= 0; i--) {
			CompositeState cs = cstates.get(i);
			AtomicState as = cs.getAtomicStateByType(type);
			
			if (as == null)
				continue;
			
			if (atomicState == null){
				atomicState = as;
			}else {
				if (atomicState.comparePriority(as))
					atomicState = as;
			}
		}
		return atomicState;
	}

	@Override
	public int compare(Object lhs, Object rhs) {
		
		CompositeState left = (CompositeState) lhs;
		CompositeState right = (CompositeState) rhs;
		
		return left.comparePriority(right) ? -1 : 1;
	}
	
	/**
	 * 停止状态引擎
	 */
	public void stop(){
		runable = false;
		
		synchronized (cstates) {
			try {
				cstates.notifyAll();
			} catch (IllegalMonitorStateException e) {
				e.printStackTrace();
			}
		}
	}
}
