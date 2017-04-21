package com.itoday.ivi.state;

import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.itoday.ivi.data.IntObserver;
import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.platform.IVIKeyEvent;
import com.itoday.ivi.platform.IVIToolKit;
import com.itoday.ivi.state.base.AtomicState;
import com.itoday.ivi.state.base.CompositeState;


/**
 * 正常运行状态
 * @author itoday
 * 开机后机器默认的运行状态
 */
public class NormalState extends CompositeState {

	private static final String NORMAL = "normal";
	
	private long lastTime = 0;
	
	private int lastAction = KeyEvent.ACTION_UP;
	
	private long lastDownTime = 0;

	/**
	 * 构造函数
	 */
	public NormalState(Handler handle) {
		super(handle, PRIORITY_LOWEST);
		
		astates.add(new AtomicState(AtomicState.OFF, AtomicState.PRIORITY_LOWEST, AtomicState.TYPE_MUTE, NORMAL));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_LOWEST, AtomicState.TYPE_SCREEN, NORMAL));
		astates.add(new AtomicState(AtomicState.ON, AtomicState.PRIORITY_LOWEST, AtomicState.TYPE_TOUCH, NORMAL));
	}

	/**
	 * 按键响应
	 */
	@Override
	public boolean onKey(int key, int action, int step){
		
		if (action == KeyEvent.ACTION_DOWN){
						
			if (lastDownTime == 0)
				lastDownTime = SystemClock.elapsedRealtime();
			
			if (((SystemClock.elapsedRealtime() - lastTime) < 200) 
				|| ((SystemClock.elapsedRealtime() - lastDownTime) < 500))
				return true;
			
			lastTime = SystemClock.elapsedRealtime();
			
			switch (key){
			case IVIKeyEvent.KEYCODE_VOLUME_DOWN:
				IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
				break;
			case IVIKeyEvent.KEYCODE_VOLUME_UP:
				IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
				break;
			
			}
			
			lastAction = action;
			return true;
		}
		
		switch (key){
		case IVIKeyEvent.KEYCODE_ANSWER:
			//IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_CALL);
			sendBroadcast(IVIToolKit.ACTION_BLUETOOTH_CALL, IVIToolKit.BLUETOOTH_CMD_KEY, IVIToolKit.BLUETOOTH_CALL);
			break;
		case IVIKeyEvent.KEYCODE_AUDIO:
			startActivityWithAction(IVIToolKit.ACTION_LOCAL_MUSIC);
			break;
		case IVIKeyEvent.KEYCODE_BACK:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_BACK);
			break;
		case IVIKeyEvent.KEYCODE_CENCEL:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_BACK);
			break;
		case IVIKeyEvent.KEYCODE_DISPLAY:
			setScreen(false);
			break;
		case IVIKeyEvent.KEYCODE_DOWN:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
			break;
		case IVIKeyEvent.KEYCODE_DVD:
			startActivityWithAction(IVIToolKit.ACTION_DVD);
			break;
		case IVIKeyEvent.KEYCODE_ENDCALL:
			//IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_ENDCALL);
			sendBroadcast(IVIToolKit.ACTION_BLUETOOTH_CALL, IVIToolKit.BLUETOOTH_CMD_KEY, IVIToolKit.BLUETOOTH_ENDCALL);
			break;
		case IVIKeyEvent.KEYCODE_ENTER:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_ENTER);
			break;
		case IVIKeyEvent.KEYCODE_EQ:
			startActivityWithAction(IVIToolKit.ACTION_EQ);
			break;
		case IVIKeyEvent.KEYCODE_HELP:
			startActivityWithAction(IVIToolKit.ACTION_HELP);
			break;
		case IVIKeyEvent.KEYCODE_HOME:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_HOME);
			break;
		case IVIKeyEvent.KEYCODE_LEFT:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA:
			startMediaMode();
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_FFW:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_NEXT:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_PAUSE:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_PLAY:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_PREV:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			break;
		case IVIKeyEvent.KEYCODE_MEDIA_REW:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_REWIND);
			break;
		case IVIKeyEvent.KEYCODE_MENU:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MENU);
			break;
		case IVIKeyEvent.KEYCODE_MIC:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_MEDIA_RECORD);
			break;
		case IVIKeyEvent.KEYCODE_MUTE:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);
			break;
		case IVIKeyEvent.KEYCODE_NAVI:
			startActivityWithAction(IVIToolKit.ACTION_NAVI);
			break;
		case IVIKeyEvent.KEYCODE_PHONE:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_CALL);
			break;
		case IVIKeyEvent.KEYCODE_POWER:{
				if (SystemClock.elapsedRealtime() - lastDownTime >= 1000){
					IntObserver standby = new IntObserver("standby");
					standby.setValue(IVIDevice.ON);
				} else 
					IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);				
			}
			break;
		case IVIKeyEvent.KEYCODE_RADIO:
			startActivityWithAction(IVIToolKit.ACTION_RADIO);
			break;
		case IVIKeyEvent.KEYCODE_RIGHT:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
			break;
		case IVIKeyEvent.KEYCODE_SEARCH:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_SEARCH);
			break;
		case IVIKeyEvent.KEYCODE_SETTING:
			startActivityWithAction(IVIToolKit.ACTION_IVI_SETTINGS);
			break;
		case IVIKeyEvent.KEYCODE_SRC:
			startSrcMode();
			break;
		case IVIKeyEvent.KEYCODE_UP:
			IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
			break;
		case IVIKeyEvent.KEYCODE_VOICE:
			startActivityWithAction(IVIToolKit.ACTION_VOICE);
			break;
		case IVIKeyEvent.KEYCODE_VOLUME_DOWN:			
				IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
			break;
		case IVIKeyEvent.KEYCODE_VOLUME_UP:			
				IVIToolKit.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
			break;
		default:
			break;
		}
		
		lastAction = action;
		lastDownTime = 0;
		return true;
	}
	
}
