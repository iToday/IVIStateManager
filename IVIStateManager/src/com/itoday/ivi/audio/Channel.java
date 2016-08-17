package com.itoday.ivi.audio;

/**
 * 音频通道，媒体，通知，电话，警告
 * @author itoday
 *
 */
public class Channel {
	
	public static final String MEDIA = "media";
	
	public static final String NAVI = "navi";
	
	public static final String PHONE = "phone";
	
	public static final String ALARM = "alarm";
	
	public static final int PRIORITY_HIGH = 0;
	
	public static final int PRIORITY_NORMAL = 1;
	
	public static final int PRIORITY_LOW = 2;
	
	public static final int PRIORITY_LOWEST = 3;
	
	/**
	 * 通道名称
	 */
	private String name;
	
	/**
	 * 通道音量, 
	 */
	private int volume;
	
	/**
	 * 静音状态,不再需要，音量0可认为静音
	 */
	//private boolean mute;
	
	/**
	 *音量优先级，如导航播报时媒体音量压低（驱动未实现），静音时，导航音量最低5，通话音量最低5等
	 */
	private int volPriority;
	
	/**
	 * 状态来源，静音/导航/语音识别/正常运行,用来打印分析
	 */
	private String from;

	public Channel(String name, int volume,  int priority, String from) {
		super();
		this.name = name;
		this.volume = volume;
		this.volPriority = priority;
		this.from = from;
	}

	public String getName() {
		return name;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}
	
	@Override
	public String toString() {
		return "Channel [name=" + name + ", volume=" + volume
				+ ", volPriority=" + volPriority + ", from=" + from + "]";
	}
}
