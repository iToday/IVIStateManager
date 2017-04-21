package com.itoday.ivi.platform.allwinner;

import java.io.FileOutputStream;
import java.io.IOException;

import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.platform.IVIToolKit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.SystemProperties;

public class Touch extends BroadcastReceiver{
	
	private static String TAG = "Touch";
	
	private static String ACTION_TOUCH_PARAMS = "com.tomwin.action.touch.params";
	
	private static String TOUCH_PARAM_PATH = "/proc/goodix_tool";
	
    private static final String TOUCH_MODE_PATH = "/sys/tomwin/touch";
    
    public static final int TOUCH_MODE_KEY = 1;//按键触摸	
	
	public static final int TOUCH_MODE_PANNEL = 2;//界面触摸
	
	public static final int TOUCH_MODE_OFF = 0;//关闭触摸
	
	public static final int TOUCH_MODE_ALL = TOUCH_MODE_KEY | TOUCH_MODE_PANNEL;
	
	private Context mContext;
	
	public Touch(Context context){
		mContext = context;
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_TOUCH_PARAMS);
		mContext.registerReceiver(this, filter);
		
		loadTouchParams();
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {

		String action = arg1.getAction();
		
		if (ACTION_TOUCH_PARAMS.equals(action)){
			
			byte[] val = arg1.getByteArrayExtra("touch");
			
			String params = IVIToolKit.arrayToHexStr(val);
			
			int length = params.length()/2;
			String a = null,b = null;
			a = params.substring(0, length);
			b = params.substring(length, length*2);
			 
			Log.d(TAG, ACTION_TOUCH_PARAMS + " : " + params);
			
			SystemProperties.set("persist.sys.touch1", a);
			SystemProperties.set("persist.sys.touch2", b);
		}
		
	}
	
	private void loadTouchParams(){
		String a  = SystemProperties.get("persist.sys.touch1", null);
		String b  = SystemProperties.get("persist.sys.touch2", null);
		
		String params = a+b;
		
		if (params != null){
			
			byte[] bytes = IVIToolKit.hexStrToArray(params);
			Log.d(TAG, "loadTouchParams:" + bytes);
		
			IVIToolKit.write2File(TOUCH_PARAM_PATH, bytes);
		}
	}
	
	public void release(){
		mContext.unregisterReceiver(this);
	}
	
	public int setTouch(int state) {
		
		return IVIToolKit.writeToFile(TOUCH_MODE_PATH, "" + TOUCH_MODE_ALL);
	}

}
