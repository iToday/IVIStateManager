package com.itoday.ivi.platform;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

public class IVINavi {

	public static final String NAVI_STATE = "navi_state";
	
	private static final String[] mMapList = {
//		"com.autonavi.xmgd.navigator", 		//高德导航
//		"com.autonavi.xmgd.navigator.tob",	//高德导航-intel定制版
		"com.autonavi.",				//高德地图
//		"com.autonavi.minimap.custom",		//高德地图intel

		"cld.navi.",			//凯立德
		"com.amap",
//		"cld.navi.mainframe",				//凯立德导航
//
//		"com.tencent.qqmusic",				//QQ音乐
		"com.tencent.map",					//腾讯地图

		"com.google.android.apps.maps",		//Google Maps

		"com.sogou.map.android.maps",		//搜狗地图

		"com.baidu.navi",					//百度导航
		//"com.baidu.navi.hd",				//百度导航HD
		"com.baidu.BaiduMa",				//百度地图
		"com.baidu.baidunavis",			//百度地图HD

		"cn.com.tiros.android.navidog",		//导航犬

		"com.mapbar.android.mapbarmap",		//图吧导航
		//"com.mapbar.android.trybuynavi",	//图吧导航（GPS离线版）

		"com.tigerknows",					//老虎地图

		"com.autonavi.cmccmap",				//和地图

		"com.uu.uueeye",					//悠悠驾车

		"com.erlinyou.worldlist",			//世界旅游导航地图

		"com.shanghaionstar",				//安吉星

		"com.pdager",						//天翼导航

		"com.didi.activity",				//嘀嘀打车

		"cn.yicha.mmi.facade2850",			//手机导航

		"com.here.app.maps"				//诺基亚Here地图
	};

	public static boolean isNaviApplication(String app){
		
		int len = mMapList.length;
		for(int i = 0; i < len; i++)
			if(app.contains(mMapList[i]))
				return true;
		
		return false;
	}
	
	public static String getRunningNaviApp(Context context){
		
		ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");  
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(100);
		Iterator<RunningTaskInfo> i = rti.iterator();
		
		String str = null;
		
		while(i.hasNext()) {
			
		   str = ((RunningTaskInfo)i.next()).topActivity.getPackageName();
		   if(isNaviApplication(str)){
		   		break;
		   } else 
			   str = null;
		}
		
		return str;
	}
}
