package com.itoday.ivi.platform.allwinner;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.platform.IVIToolKit;

public class Brightness {
	
	 private static final String BACKLIGHT_PATH = "/sys/tomwin/led";
	/**
	 * 白天的背光亮度
	 */
	public static final String DAY_BRIGHTNESS = "brightness_day";
	
	public static final int BRIGHTNESS_DAY_DEFAULT = 128;
	
	/**
	 * 夜晚的背光亮度
	 */
	public static final String NIGHT_BRIGHTNESS = "brightness_night";
	
	public static final int BRIGHTNESS_NIGHT_DEFAULT = 32;
	
	public static final int DAY = 0;
	
	public static final int NIGHT = 1;
	
	private PowerManager mPowerManager;
	
	private IVIDataManager mDataManager;
	
	private int mMode = DAY;
	
	private ContentObserver mContentObserver = new ContentObserver (new Handler()){

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			if (mMode == DAY){
				mDataManager.putInt(DAY_BRIGHTNESS, mDataManager.getInt(Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_DAY_DEFAULT));
			} else {
				mDataManager.putInt(NIGHT_BRIGHTNESS, mDataManager.getInt(Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_NIGHT_DEFAULT));
			}
		}
		
	};

	
	public Brightness(Context context){
		
		mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		
		mDataManager = IVIDataManager.instance();
		//监听背光变化，并保存到对应的亮度模式下
		mDataManager.registerDataChange(Settings.System.SCREEN_BRIGHTNESS, mContentObserver);
	}
	
	/**
	 * 设置背光模式
	 * @param mode
	 */
	public void setMode(int mode){
		
		mMode = mode;
		
		int level = 0;
		if (mMode == DAY){
			level = mDataManager.getInt(DAY_BRIGHTNESS, BRIGHTNESS_DAY_DEFAULT);
		} else
			level = mDataManager.getInt(NIGHT_BRIGHTNESS, BRIGHTNESS_NIGHT_DEFAULT);
		
		mPowerManager.setBacklightBrightness(level);
	}
	
	/**
	 * 设置背光开关
	 * @param on
	 */
	public void setBackLightOn(boolean on){
		if (on){
			IVIToolKit.writeToFile(BACKLIGHT_PATH, "1");
		}else{
			IVIToolKit.writeToFile(BACKLIGHT_PATH, "0");
		}
	}
	
	public void release(){
		mDataManager.unregisterDataChange(mContentObserver);
	}

	public void shutdown(boolean b) {
		mPowerManager.shutdown(b, false);
	}
}
