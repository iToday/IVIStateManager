package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.state.base.CompositeState;

/**
 * 导航播报状态
 * @author itoday
 * 导航播报时媒体有压低和中断两种状态
 */
public class NaviState extends CompositeState {

	private static final String NAVI = "navi";

	/**
	 * 构造函数
	 * @param from
	 * @param keyPriority
	 */
	public NaviState(Handler handle) {
		super(handle, PRIORITY_LOW);
	}

	@Override
	public boolean onKey(int key, int action, int step) {
		
		switch (key) {
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
		case IVIKeyEvent.KEYCODE_VOLUME_UP:
			//调节音量时调节导航音量
			break;
		default:
			break;
		}
		return super.onKey(key, action, step);
	}

}
