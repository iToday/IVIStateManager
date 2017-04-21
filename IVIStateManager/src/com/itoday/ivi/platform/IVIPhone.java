package com.itoday.ivi.platform;

public class IVIPhone {

	/**
	 * 通话空闲
	 */
	public static final int IDLE = 0;
	
	/**
	 * 来电响铃
	 */
	public static final int INCOME_RING = 1;
	
	/**
	 * 拨号中
	 */
	public static final int OUT_CALLING = 2;
	
	/**
	 * 通话中
	 */
	public static final int TALKING = 3;
	
	/**
	 * 通话挂起
	 */
	public static final int HOLDING = 4;
	
	
	/**
	 * 连接断开
	 */
	public static final int DISCONNECTED = 0;
	
	/**
	 * 正在断开
	 */
	public static final int DISCONNECTING = 1;
	
	/**
	 * 正在连接
	 */
	public static final int CONNECTING = 2;
	
	/**
	 * 已连接
	 */
	public static final int CONNECTED = 3;
	
	/**
	 * 通话状态
	 */
	public static final String PHONE_STATE = "talk_state";
	
	public static final String CONNECT_STATE = "bt_connect_state";
	
	private IVIPhone(){
		
	}
}
