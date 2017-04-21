package com.itoday.ivi.state.base;
/**
 * 原子状态，如静音/屏幕/触摸等；
 * 状态单一，不可再分裂的状态；
 * @author itoday
 *
 */
public  class AtomicState extends Object{
	/**
	 * 状态优先级
	 */
	public static final int PRIORITY_HIGH = 0;
	
	public static final int PRIORITY_NORMAL = 1;
	
	public static final int PRIORITY_LOW = 2;
	
	public static final int PRIORITY_LOWEST = 3;
	
	
	/**
	 * 状态值
	 */
	public static final int ON = 1;
	
	public static final int OFF = 0;
	
	/**
	 * 状态类型
	 */
	public static final String TYPE_MUTE = "mute";
	
	public static final String TYPE_SCREEN = "screen";
	
	public static final String TYPE_TOUCH = "touch";
	
	/**
	 * 返回值
	 */
	public static final int SUCCESS = 0;
	
	public static final int FAILED = -1;
	
	private int priority = PRIORITY_LOWEST;
	
	private int state = OFF;
	
	//from will be phone / backcar / standby etc.   
	private String from = null;
	
	private String type = null;
	
	public AtomicState(int state, int prority, String type,  String from){
		this.state = state;
		this.priority = prority;
		this.type = type;
		this.from = from;
	}
	
	/**
	 * 比较两种状态的优先级
	 * @param dest
	 * @return true ：大于等于目标状态优先级，否则 false
	 */
	public boolean comparePriority(final AtomicState dest){
		return dest != null && priority >= dest.priority;
	}
	
	/**
	 * 比较两种状态是否相同
	 * @param dest
	 * @return true:与目标状态相同，否则 false
	 */
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AtomicState))
			return false;
		
		AtomicState dest = (AtomicState) o;
		
		return  priority == dest.priority 
				&& state == dest.state
				&& type.equals(dest.type)
				&& from.equals(dest.from);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * 获取当前状态
	 * @return
	 */
	public int getState(){
		return state;
	}
	
	public boolean equalsState(final AtomicState dest){
		return dest != null && dest.getState() == state;
	}
	
	/**
	 * 判断当前状态是否from相同
	 * @param from
	 * @return true 相同，否则 false
	 */
	public boolean isFrom(String from){
		return this.from != null && this.from.equals(from);
	}
	
	/**
	 * 判断当前状态是否type相同
	 * @param type
	 * @return true 相同，否则 false
	 */
	public boolean isType(String type){
		return this.type.equals(type);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [priority=" + priority + ", state=" + state
				+ ", from=" + from + ", type=" + type + "]";
	}
	
}
