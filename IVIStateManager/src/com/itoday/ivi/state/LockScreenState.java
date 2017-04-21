package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;
/**
 * 锁屏状态，显示屏保，关触摸
 * @author itoday
 * 一般显示时钟界面/频谱界面
 */
public class LockScreenState extends CompositeState {

	private static final String LOCK_SCREEN = "LockScreen";

	/**
	 * 构造函数
	 */
	public LockScreenState(Handler handle) {
		super(handle, PRIORITY_NORMAL);
		
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_TOUCH, LOCK_SCREEN));
	}

	@Override
	public boolean onKey(int key, int action, int step) {
		
		switch (key) {
		case IVIKeyEvent.KEYCODE_VOLUME_UP:
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
		case IVIKeyEvent.KEYCODE_MEDIA_NEXT:
		case IVIKeyEvent.KEYCODE_MEDIA_PREV:
		case IVIKeyEvent.KEYCODE_POWER://解除锁屏
			return false; //此类按键不处理，交给其他状态处理
		default:
			break;
		}
		
		return true; //其他按键屏蔽掉
	}

}
