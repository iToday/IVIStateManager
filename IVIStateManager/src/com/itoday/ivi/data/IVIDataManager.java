package com.itoday.ivi.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.System;

public class IVIDataManager {
	
	private ContentResolver mResolver;
	
	private static Context mContext;
	
	private static IVIDataManager manager;
	
	/**
	 * 信息存储
	 * @param context
	 */
	private IVIDataManager(){
		mResolver = mContext.getContentResolver();
	}
	
	/**
	 * 初始化，在调用其他接口之前必须先调用此接口初始化
	 * @param context
	 */
	public static void setup(Context context){
		if (mContext == null)
			mContext = context;
	}
	
	public static IVIDataManager instance(){
		
		if (mContext == null)
			throw new NullPointerException("context is not set");
		
		synchronized(IVIDataManager.class){
			
			if (manager == null)
				manager = new IVIDataManager();
		}
		
		return manager;
	}
	
	public void putInt(String key, int value){
		
		System.putInt(mResolver, key, value);
	}
	
	public int getInt(String key, int def){
		
		return System.getInt(mResolver, key, def);
	}
	
	public void putString(String key, String value){
		
		System.putString(mResolver, key, value);
	}
	
	public String getString(String key){
		
		return System.getString(mResolver, key);
	}
	
	public void registerDataChange(String name, ContentObserver observer){
		
		Uri uri = Settings.System.getUriFor(name);
		
		mResolver.registerContentObserver(uri, false, observer);
	}
	
	public void unregisterDataChange(ContentObserver observer){
		mResolver.unregisterContentObserver(observer);
	}
	
}
