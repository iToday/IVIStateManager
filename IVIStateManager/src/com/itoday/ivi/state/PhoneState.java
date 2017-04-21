package com.itoday.ivi.state;

import android.os.Handler;
import android.view.KeyEvent;

import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.platform.IVIToolKit;
import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;

/**
 * 电话状态
 * @author itoday
 * 电话时其他媒体中断
 */
public class PhoneState extends CompositeState {
	
	private static final String PHONE = "Phone";

	/**
	 * 构造函数
	 */
	public PhoneState(Handler handle) {
		super(handle, PRIORITY_HIGH);
		//this can load from config file
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_MUTE, PHONE));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_NORMAL, AtomicState.TYPE_SCREEN, PHONE));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_HIGH, AtomicState.TYPE_TOUCH, PHONE));
	}

	@Override
	public boolean onKey(int key, int action, int step){
		switch (key) {
		case IVIKeyEvent.KEYCODE_VOLUME_UP:
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
			return false;
		case IVIKeyEvent.KEYCODE_ENDCALL:
			sendBroadcast(IVIToolKit.ACTION_BLUETOOTH_CALL, IVIToolKit.BLUETOOTH_CMD_KEY, IVIToolKit.BLUETOOTH_ENDCALL);
			break;
		case IVIKeyEvent.KEYCODE_ANSWER:
			//IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_CALL);
			sendBroadcast(IVIToolKit.ACTION_BLUETOOTH_CALL, IVIToolKit.BLUETOOTH_CMD_KEY, IVIToolKit.BLUETOOTH_CALL);
			break; //电话时只处理这些按键,其他按键屏蔽
		default:
			break;
		}
		
		return true;
	}
	
}