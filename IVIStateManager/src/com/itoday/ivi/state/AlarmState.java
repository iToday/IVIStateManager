package com.itoday.ivi.state;

import android.os.Handler;

import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.state.base.CompositeState;

/**
 * 报警状态
 * @author itoday
 * 如胎压报警
 */
public class AlarmState extends CompositeState {

	private static final String ALARM = "alarm";

	/**
	 * 构造函数
	 * @param from
	 * @param keyPriority 按键优先级，定义见{@link CompositeState}
	 */
	public AlarmState(Handler handle, int keyPriority) {
		super(handle, keyPriority);
	}

	@Override
	public boolean onKey(int key, int action, int step) {
		//调节音量时调节报警音量
		//报警时不能静音
		switch (key) {
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
		case IVIKeyEvent.KEYCODE_VOLUME_UP:
			
			//调节警报音量
			break;
		default:
			//报警时优先级最高，屏蔽其他按键操作
			return true;
		}
		return super.onKey(key, action, step);
	}
	
}
