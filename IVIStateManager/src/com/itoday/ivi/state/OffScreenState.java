package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;

/**
 * 关屏状态
 * @author itoday
 * 关屏状态下用户可触摸开屏
 */
public class OffScreenState extends CompositeState {

	private static final String OFF_SCREEN = "OffScreen";

	/**
	 * 构造函数
	 */
	public OffScreenState(Handler handle) {
		super(handle, PRIORITY_NORMAL);
		
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_LOW, AtomicState.TYPE_SCREEN, OFF_SCREEN));
	}

	@Override
	public boolean onKey(int key, int action, int step){
		
		switch (key) {
		case IVIKeyEvent.KEYCODE_VOLUME_UP:
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
		case IVIKeyEvent.KEYCODE_MEDIA_NEXT:
		case IVIKeyEvent.KEYCODE_MEDIA_PREV:
			return false; //此类按键不处理，交给其他状态处理
		default:
			setScreen(true); //其他案件开屏
			break;
		}
		 //解除关屏状态
		
		return true; //其他按键解除关屏状态
		
	}
}
