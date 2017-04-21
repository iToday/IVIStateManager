package com.itoday.ivi.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 模块
 * @author itoday
 *
 */
public class Module {
	
	public static final String NAME = "name";
	
	public static final String VALID = "valid";
	
	private String name;
	
	private boolean valid;
	
	private Map<String, String> values = new HashMap<String, String>();
	
	/**
	 * 
	 * @param name
	 * @param valid
	 * @param values
	 */
	public Module(String name,  boolean valid, Map<String, String> values){
		this.name = name;
		this.valid = valid;
		this.values = values;
	}

	/**
	 * 模块名称
	 * @return
	 */
	public String getModule() {
		return name;
	}

	/**
	 * 此配置是否有效
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}
	
	/**
	 * 获取配置
	 * @param key
	 * @return
	 */
	public String getKey(String key){
		
		return values.get(key);
	}

	@Override
	public String toString() {
		return "Module [name=" + name + ", valid=" + valid + ", values="
				+ values + "]";
	}
}
