package com.itoday.ivi.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class IVIToolKit {
	
	private static final String TAG = "IVIToolKit";
	
	/**
	 * 收音机启动接口
	 */
	public static final String ACTION_RADIO = "tomwin.intent.action.radio";
	
	/**
	 * 本地音乐启动接口
	 */
	public static final String ACTION_LOCAL_MUSIC = "tomwin.intent.action.music";
	
	/**
	 * 本地视频启动接口
	 */
	public static final String ACTION_LOCAL_MOVIE = "tomwin.intent.action.movie";
	
	/**
	 * 导航启动接口
	 */
	public static final String ACTION_NAVI = "tomwin.intent.action.navi";
	
	/**
	 * 蓝牙启动接口
	 */
	public static final String ACTION_BLUETOOTH = "tomwin.intent.action.bluetooth";
	
	/**
	 * EQ设置启动接口
	 */
	public static final String ACTION_EQ = "tomwin.intent.action.eq";
	
	/**
	 * MCU升级启动接口
	 */
	public static final String ACTION_MCU_UPGRADE = "tomwin.intent.action.mcu.upgrade";
	
	/**
	 * 方控学习接口
	 */
	public static final String ACTION_WHEEL = "tomwin.intent.action.wheel";
	
	/**
	 * 车载设置启动接口
	 */
	public static final String ACTION_IVI_SETTINGS = "tomwin.intent.action.settings";
	
	/**
	 * DVD启动接口
	 */
	public static final String ACTION_DVD = "tomwin.intent.action.dvd";
	
	/**
	 * 蓝牙音乐启动接口
	 */
	public static final String ACTION_BT_MUSIC = "tomwin.intent.action.bt.music";
	
	/**
	 * 电话本启动接口
	 */
	public static final String ACTION_PHONE_BOOK = "tomwin.intent.action.phone.book";
	
	/**
	 * 收音机设置启动接口
	 */
	public static final String ACTION_RADIO_SET = "tomwin.intent.action.radio.set";

	/**
	 * 语音识别
	 */
	public static final String ACTION_VOICE = "tomwin.intent.action.voice";
	
	/**
	 * 使用说明
	 */
	public static final String ACTION_HELP = "tomwin.intent.action.help";
	
	/**
	 * 启动校屏界面
	 */
	public static final String ACTION_CALIBRATION = "tomwin.intent.action.calibration";
	
	/**
	 * 蓝牙广播
	 */
	public static final String ACTION_BLUETOOTH_CALL = "android.intent.mcu.BT_CALL";
	
	public static final String BLUETOOTH_CMD_KEY = "values";
	
	/**
	 * 接听电话
	 */
	public static final int BLUETOOTH_CALL = 1;
	
	/**
	 * 挂断电话
	 */
	public static final int BLUETOOTH_ENDCALL = 2;
	
	
	public static void sendBroadcast(Context context, String action, String key, int value){
		
		Intent intent = new Intent(action);
		
		intent.putExtra(key, value);
		
		context.sendBroadcast(intent);
	}
	
	/**
	 * 通过包名启动程序
	 * @param context
	 * @param pkg 包名
	 * @return true 成功， false 失败
	 */
	public static boolean startActivityWithPkg(Context context, String pkg){
		
		PackageManager pm = context.getPackageManager();
		
		Intent intent = pm.getLaunchIntentForPackage(pkg);
		
		Log.d(TAG, "startActivityWithPkg " + pkg);
		
		if (intent != null){
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}else
			return false;
		
		return true;
	}
	
	public static  boolean isActivityRunningBg(Context context, String packagename){
		
		 ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");  
		 List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(20);  
		 int rtiSize = rti.size();
		 
		 int i = 0;
		 for (i=0; i<rtiSize; i++) {
			 
			if(rti.get(i).baseActivity.getPackageName().contains(packagename))
				return true;
		 }
		 return false;  
	}
	
	public static void popBgAppFromStack(Context context, String packagename)	{
		
		 ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");  
		 List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(20);  
		 
		 int rtiSize = rti.size();
		 int i = 0;
		 for (i=0; i<rtiSize; i++){
			 
			if(rti.get(i).baseActivity.getPackageName().contains(packagename)){
				mActivityManager.moveTaskToFront(rti.get(i).id, 0,null);
                break;
			}
		 }
	}
	
	public static boolean isInRunningActivity(Context context, String packagename){
		 ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");  
		 List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);  
		 if(packagename.equals(rti.get(0).topActivity.getPackageName()))
				return true;
		 return false;  
	}
	
	public static void startActivity(Context context, String packName){
		
		Log.d(TAG, "startActivity " + packName);
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(packName);
		if (intent != null){
			if(isInRunningActivity(context, packName)){
				sendKeyEvent(KeyEvent.KEYCODE_HOME);
			}
			else
			{
			  if(isActivityRunningBg(context, packName))
				  	popBgAppFromStack(context, packName);
				else
					context.startActivity(intent);
			}	
		}
	}
	
	/**
	 * 通过Action 启动程序
	 * @param context
	 * @param action 启动接口
	 * @return true 成功， false 失败
	 */
	public static boolean startActivityWithAction(Context context, String action){
	
		Intent intent = new Intent(action);
		
		try {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			
			return true;
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 写入数据到文件
	 * @param path 文件绝对路径
	 * @param value 数据字符串
	 * @return 写入的个数，
	 */
	public static int writeToFile(String path, String value){
		
		File file = new File(path);    
		  
        FileOutputStream fos = null;
        
		try {
			fos = new FileOutputStream(file);
			byte [] bytes = value.getBytes();   
			  
	        try {
	        	
				fos.write(bytes);
				return bytes.length;
				
			} catch (IOException e) {
				e.printStackTrace();
			}   
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		}finally {
			
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
  
		return 0;
	}
	
	/**
	 * 读取文件内容
	 * @param path
	 * @return
	 */
	public static byte[] readFile(String path){
		
		byte[] buffer = null;
		
        try {
            
            FileInputStream fin = new FileInputStream(path);
            int length = fin.available();
            
            buffer = new byte[length];
            fin.read(buffer);
            
            fin.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return buffer;
	}
	
	/**
	 * 将数组写入文件
	 * @param path 文件绝对路径
	 * @param buf 数字缓冲，不能为null
	 * @return 写入个数,0表示失败
	 */
	public static int write2File(String path, byte[] buf){
		
		FileOutputStream fout = null;
		int ret = 0;
		
		try{
			fout = new FileOutputStream(path);
			fout.write(buf);
			ret = buf.length;
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(fout != null)
					fout.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return ret; 
	}
	
	/**
	 * 将数组转化为16进制字符串
	 * @param value
	 * @return
	 */
	public static String arrayToHexStr(byte[] value){
		
	      StringBuffer sb = new StringBuffer(value.length);
	      
		  String sTemp;
		  
		  for (int i = 0; i < value.length; i++) {
			  
			   sTemp = Integer.toHexString(0xFF & value[i]);
			   if (sTemp.length() < 2)
			    sb.append(0);
			   
			   sb.append(sTemp.toUpperCase());
		   
		  }
		  
		  String hex = sb.toString();
		  
		  Log.d(TAG, "str:::" +hex);
		  
		  return hex;
	}
	
	
	/**
	 * 将16进制字符串转换为数组
	 * @param hex
	 * @return
	 */
	public static byte[] hexStrToArray(String hex){
		
		   int len = (hex.length() / 2);
		   
		   byte[] result = new byte[len];
		   char[] achar = hex.toCharArray();
		   
		   for (int i = 0; i < len; i++) {
			    int pos = i * 2;
			    result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
			    
			    Log.d(TAG, "result::" + result[i]);	    
		   }
		   
		   return result;
	 }
	

	 private static  int toByte(char c) {
		    byte b = (byte) "0123456789ABCDEF".indexOf(c);
		    return b;
	 }

	/**
	 * 获取MCU升级文件的版本
	 * @param path 升级文件的路径
	 * @return 版本信息， 失败为null
	 */
	public static String getMcuUpgradeFileVersion(String path){
		
		try{
			File file = new File(path);
			
			if(file.exists()){
				
				FileInputStream fin = new FileInputStream(file);
				
				int len;
				
				byte[] buffer = null;
				
				try {
					len = fin.available();
					
					buffer = new byte[len];
					
					fin.read(buffer);
					
					int i;
					
					for(i = 0; i < (len - 0x80); i++)
						if(buffer[0x80 + i] == 0) 
							break;
					
					String str = new String(buffer,0x80,i);
					
					Log.d(TAG,"Mcu file len =" + len + ", version = " + str);
					
					return str;
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
					
					if (fin != null)
						fin.close();
				}
				
			} else {
				Log.d(TAG, path + " not exists");
				return null;
			}
		}catch(java.io.FileNotFoundException e1){
			Log.e(TAG, e1.toString());
		}
		catch(java.io.IOException e2){
			Log.e(TAG,"Read error");
		} 
		
		return null;
	}
	
	/**
	 * 获取当前顶部Activity包名
	 * @param context
	 * @return null 或 包名
	 */
	public static String getTopActivityPackageName(Context context){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1) ;
		if(runningTaskInfos != null && runningTaskInfos.size() > 0){
    		ComponentName cn = runningTaskInfos.get(0).topActivity; 
    		if (cn != null){
    			return cn.getPackageName();
    		}
    	}
		return null;
	}
	
	/**
	 * 获取当前顶部Activty的名称
	 * @param context
	 * @return  null 或 包名
	 */
	public static String getTopActivityName(Context context){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1) ;
		if(runningTaskInfos != null){
			ComponentName cn = runningTaskInfos.get(0).topActivity; 
			if (cn != null){
				return cn.getClassName();
			}
		}
		return null;
	}
	
	public static boolean isAppInstalled(Context context,String packagename){
		
		PackageInfo packageInfo;        
		try {
	        packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
	     }catch (NameNotFoundException e) {
	        packageInfo = null;
	        e.printStackTrace();
	     }

		return packageInfo != null;
	}
	
	public static void killAppByPackage(final Context context, final String packageName) {
        Log.d(TAG, "require close " + packageName);
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            activityManager.forceStopPackage(packageName);
        } catch(Exception e) {
            Log.e(TAG, "close " + packageName + " error");
            e.printStackTrace();
        }
     }
	 
	 public static void sendKeyEvent(int keyCode,int state) {
	     long now = SystemClock.uptimeMillis();
	     injectKeyEvent(new KeyEvent(now, now, state, keyCode, 0, 0,KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
	 }
		
	public static void sendKeyEvent(int keyCode) {
	     long now = SystemClock.uptimeMillis();
	     injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
	     now = SystemClock.uptimeMillis();
	     injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
	}
		  
	private static void injectKeyEvent(KeyEvent event) {
	    InputManager.getInstance().injectInputEvent(event,InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
	}
}
