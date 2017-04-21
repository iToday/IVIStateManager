package com.itoday.ivi.platform;

/**
 * 
 * @author itoday
 *
 */
public class IVIDevice {
	
	/**
	 * 设备关闭
	 */
	public static final int OFF = 0;
	
	/**
	 * 设备打开
	 */
	public static final int ON = 1;
	
	/**
	 * 设备不存在
	 */
	public static final int NO = -1;
	
	
	//设备ID
	private int id;
	//设备名称
	private String name;
	//设备开关状态
	private int state;
	//扩展状态，如通话状态，播放装态
	private int exState;
	
	/**
	 * 构造函数
	 * @param id 设备ID
	 * @param name 设备名称
	 * @param state 设备状态
	 */
	public IVIDevice(int id, String name, int state) {
		super();
		this.id = id;
		this.name = name;
		this.state = state;
		
		exState = NO;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getState() {
		return state;
	}
	
	public boolean setState(int state) {
		
		if (this.state == state)
			return false;
		
		this.state = state;
		
		return true;
	}
	
	public boolean isExist(){
		return state != NO;
	}
	
	public boolean isOn(){
		return state == ON;
	}
	
	public boolean isOff(){
		return state == OFF;
	}
	
	public int getExState() {
		return exState;
	}

	public void setExState(int exState) {
		this.exState = exState;
	}

	/**
	 * 设备ID
	 * @author itoday
	 *
	 */
	public class DeviceID{
		/**
		 * 大灯设备
		 */
		public static final int DEV_LAMP = 1;
		
		public static final String LAMP = "lamp";
		
		/**
		 * ACC设备
		 */
		public static final int DEV_ACC = 2;
		
		public static final String ACC = "acc";
		
		/**
		 * 手刹设备
		 */
		public static final int DEV_BRAKE = 3;
		
		public static final String BRAKE = "brake";
		
		/**
		 * 倒车设备
		 */
		public static final int DEV_REVERING = 4;
		
		public static final String REVERING = "revering";
		
		/**
		 * 触摸设备
		 */
		public static final int DEV_TOUCH = 5;
		
		public static final String TOUCH = "touch";
		
		/**
		 * 显示屏
		 */
		public static final int DEV_BACKLIGHT = 6;
		
		public static final String BACKLIGHT = "backlight";
		
		/**
		 * IPOD设备
		 */
		public static final int DEV_IPOD = 7;
		
		public static final String IPOD = "ipod";
		
		/**
		 * DVR设备
		 */
		public static final int DEV_DVR = 8;
		
		public static final String DVR = "dvr";
		
		/**
		 * 360全景泊车
		 */
		public static final int DEV_AVM = 9;
		
		public static final String AVM = "avm";
		
		/**
		 * 电视设备
		 */
		public static final int DEV_TV = 10;
		
		public static final String TV = "tv";
		
		/**
		 * Can设备
		 */
		public static final int DEV_CANBUS = 11;
		
		public static final String CANBUS = "canbus";
		
		/**
		 * 收音机设备
		 */
		public static final int DEV_RADIO = 12;
		
		public static final String RADIO = "radio";
		
		/**
		 * 蓝牙设备
		 */
		public static final int DEV_BT = 13;
		
		public static final String BT = "bt";
		
		/**
		 * 雷达设备
		 */
		public static final int DEV_RADAR = 14;
		
		public static final String RADAR = "radar";
		
		/**
		 * MCU设备
		 */
		public static final int DEV_MCU = 15;
		
		public static final String MCU = "mcu";
		
		/**
		 * 其他设备，用来扩展
		 */
		public static final int DEV_OTHER = 255;
		
		private DeviceID(){
			throw new IllegalAccessError("DeviceID");
		}
	}

}
