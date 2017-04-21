package com.itoday.ivi.platform;

/**
 * 以下为按键定义，此处按键定义只用于IVI内部,平台同意按键；
 * 不同设备的按键可通过转换函数转换
 * @author itoday
 */
public class IVIKeyEvent {
	/**
	 * 以下为通用操作按键
	 */
	public static final int KEYCODE_HOME 	= 0x01;
	
	public static final int KEYCODE_BACK 	= 0x02;
	
	public static final int KEYCODE_MENU 	= 0x03;
	
	public static final int KEYCODE_POWER 	= 0x04;
	
	public static final int KEYCODE_DISPLAY = 0x05;
	
	public static final int KEYCODE_MEDIA 	= 0x06;
	
	public static final int KEYCODE_SRC 	= 0x07;
	
	public static final int KEYCODE_UP 		= 0x08;
	
	public static final int KEYCODE_DOWN 	= 0x09;
	
	public static final int KEYCODE_LEFT 	= 0x0A;
	
	public static final int KEYCODE_RIGHT 	= 0x0B;
	
	public static final int KEYCODE_ENTER 	= 0x0C;
	
	public static final int KEYCODE_CENCEL	= 0x0D;
	
	/*****
	 * 以下为媒体按键
	 */
	public static final int KEYCODE_MUTE 				= 0x40;
	
	public static final int KEYCODE_VOLUME_UP 			= 0x41;
	
	public static final int KEYCODE_VOLUME_DOWN 		= 0x42;
	
	public static final int KEYCODE_MEDIA_PREV 			= 0x44;
	
	public static final int KEYCODE_MEDIA_NEXT 			= 0x45;
	
	public static final int KEYCODE_MEDIA_PLAY_PAUSE 	= 0x46;
	
	public static final int KEYCODE_MEDIA_PLAY 			= 0x47;
	
	public static final int KEYCODE_MEDIA_PAUSE 		= 0x48;
	
	public static final int KEYCODE_MEDIA_FFW 			= 0x49;
	
	public static final int KEYCODE_MEDIA_REW 			= 0x4A;
	
	/**
	 * 以下为功能按键
	 */
	public static final int KEYCODE_NAVI  	= 0x21;
	
	public static final int KEYCODE_RADIO 	= 0x22;
	
	public static final int KEYCODE_VOICE 	= 0x23;
	
	public static final int KEYCODE_PHONE 	= 0x24;
	
	public static final int KEYCODE_AUDIO 	= 0x25;
	
	public static final int KEYCODE_SETTING = 0x26;
	
	public static final int KEYCODE_EQ 		= 0x27;
	
	public static final int KEYCODE_HELP 	= 0x28;
	
	public static final int KEYCODE_SEARCH 	= 0x29;
	
	public static final int KEYCODE_DVD 	= 0X30;
	
	/**
	 * 以下为电话按键
	 */
	public static final int KEYCODE_ANSWER 	= 0x60;
	
	public static final int KEYCODE_ENDCALL = 0x61;
	
	public static final int KEYCODE_MIC 	= 0x62;
	
	/**
	 * 构造函数，此类不用于生成对象
	 */
	private IVIKeyEvent(){
		
	}

}
