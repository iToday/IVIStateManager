package com.itoday.ivi.vehicle;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVINavi;
import com.itoday.ivi.platform.IVIToolKit;

public class AppModeManager {
	
	private static final String TAG = "AppModeManager";

	private static final String KEY_RECOVERY_MEDIA = "recovery.app.meida";
	
	private static final String KEY_RECOVERY_NAVI = "recovery.app.navi";
	
	private static final String mRadioName = "com.tomwin.fmradio";
	
	private static final String mDVDName = "com.tomwin.twdvd";
	
	private static final String mAvinName = "com.tomwin.cvbs";
	
	private static final String mVideoName = "com.tomwin.myvideo";
	
	private static final String mAudioName = "com.tomwin.audio";
	
	private static final String mBlueThName = "com.tomwin.bluetooth";
	
	private static final String mMapName = "com.tomwin.gpsshortcut";
	
	private static final int MEDIA_STORED = 1;
	
	private static final int NAVI_STORED = 2;
	
	private static final int ALL_STORED = 3;
	
	private String[] mSrcModeList = {
		mRadioName,
		mAudioName,
		mVideoName,
		mDVDName,
		mAvinName,
		mBlueThName,
		mMapName,
		};
	
	private String[] mMediaModeList = {
		mRadioName,
		mAudioName,
		mVideoName,
		mDVDName,
		mAvinName,
		mBlueThName,
		};
	
	private String[] mRecoverableAppList = {
		mRadioName,
		mDVDName,
		mAvinName,
		mAudioName,
		mVideoName,
	};
	
	private Context mContext;
	
	private ActivityManager mActivityManager;
	
	private int mSrcIndex = -1;
	
	public AppModeManager(Context context){
		mContext = context;
		
		mActivityManager = (ActivityManager) mContext.getSystemService("activity");  
	}
	
	public String src(){
		
		return getNextApp(mSrcModeList);
	}
	
	public String media(){
		
		return getNextApp(mMediaModeList);
	}

	private String getNextApp(String[] list){
		
		String top = IVIToolKit.getTopActivityPackageName(mContext);
		
		Log.d(TAG, "getNextApp : top " + top );
		
		for(int i = 0; i < list.length; i++){
			
			Log.d(TAG, "getNextApp : list[ " + i + "]: "+ list[i] );
			if(top.equals(list[i])){
				
				mSrcIndex = i;
			}
		}
		
		return getNextValidApp(mContext, mSrcIndex, list);		
	}
	
	private String getNextValidApp(Context context, int cur, String[] list){
		
		int start = cur;
		
		Log.d(TAG, "getNextValidApp : start " + start );
		
		do{
			cur ++;
			
			if (cur > list.length - 1)
				cur = 0;
			
			if (start == cur)// traverse all
				break;
			
		} while (!IVIToolKit.isAppInstalled(context, list[cur]));
		
		mSrcIndex = cur;
		
		return list[cur];
		
	}
	
	private boolean isAppSaveable(RunningTaskInfo app){
					
		String pname = app.topActivity.getPackageName();
		
		if(pname.equals(mAvinName)){
			return !(app.topActivity.getShortClassName().equals(".ReverseActivity"));
			
		} else {
			
			for(int i = 0; i < mRecoverableAppList.length; i++){
				if(pname.equals(mRecoverableAppList[i])){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isStored(int flag, int stored){
		return (flag & stored) == flag;
	}
	
	public void backup(){
		
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(100);
		Iterator<RunningTaskInfo> i = rti.iterator();
		
		int stored = 0;
		
		while(i.hasNext() && !isStored(ALL_STORED, stored)) {
			
		 	RunningTaskInfo info = (RunningTaskInfo)i.next();
		 	String pkg = info.topActivity.getPackageName();
		 	
		 	Log.d(TAG, "running package " + pkg);
		 	
			if(!isStored(MEDIA_STORED, stored) && isAppSaveable(info)){
				stored |= MEDIA_STORED;
				IVIDataManager.instance().putString(KEY_RECOVERY_MEDIA, pkg);
				
			}else if(!isStored(NAVI_STORED, stored) && IVINavi.isNaviApplication(pkg)){
				IVIDataManager.instance().putString(KEY_RECOVERY_NAVI, pkg);
				stored |= NAVI_STORED;
			}
		}
	}
	
	public void recovery(){
		
		Handler handler = new Handler();
		
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				String pkg = IVIDataManager.instance().getString(KEY_RECOVERY_MEDIA);
				
				Log.d(TAG, "recovery media :" + pkg);
				
				if((pkg != null) && !pkg.isEmpty()){
					
					IVIToolKit.startActivityWithPkg(mContext, pkg);
				} 
				
				IVIDataManager.instance().putString(KEY_RECOVERY_MEDIA, "");
			}
			
		}, 0);
		
		
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				String pkg = IVIDataManager.instance().getString(KEY_RECOVERY_NAVI);
				
				Log.d(TAG, "recovery navi :" + pkg);
				
				if((pkg != null) && !pkg.isEmpty()){
					
					IVIToolKit.startActivityWithPkg(mContext, pkg);
				} 
				
				IVIDataManager.instance().putString(KEY_RECOVERY_NAVI, "");
			}
			
		}, 500);
	}

}
