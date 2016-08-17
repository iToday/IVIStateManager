package com.itoday.ivi.audio;

import java.util.ArrayList;


public class VolumeManager {

	
	/**
	 * 设置静音状态
	 * @param on
	 * @return
	 */
	public boolean setMute(boolean on){
		return true;
	}
	
	/**
	 * 设置导航播报状态
	 * @param playing
	 * @return
	 */
	public boolean setNavi(boolean playing){
		return true;
	}
	
	/**
	 * 设置语音识别状态
	 * @param on
	 * @return
	 */
	public boolean setVoice(boolean on){
		return true;
	}
	
	/**
	 * 设置电话状态
	 * @param on
	 * @return
	 */
	public boolean setPhone(boolean on){
		return true;
	}
	
	public int setVolume(int volume){
		return volume;
	}
	
	public int getVolume(){
		return 0;
	}
	
	public int setStreamVolume(int stream, int volume){
		return volume;
	}
	
	public int getStreamVolume(int stream){
		return 0;
	}
	
	class Volume{
		private ArrayList<Channel> volumes = new ArrayList<Channel>();
		
		public Volume(){
			
		}
		
		public boolean add(Channel channel){
			volumes.remove(channel);
			return volumes.add(channel);
		}
	}
	
	class Mute extends Volume{
		public Mute(){
			add(new Channel(Channel.MEDIA, getMinVolume(Channel.MEDIA), Channel.PRIORITY_NORMAL, "Mute"));
			add(new Channel(Channel.NAVI, getMinVolume(Channel.NAVI), Channel.PRIORITY_NORMAL, "Mute"));
			add(new Channel(Channel.PHONE, getMinVolume(Channel.PHONE), Channel.PRIORITY_NORMAL, "Mute"));
		}
		
		private int getMinVolume(String name){
			return 0;
		}
	}
	
	class NaviBroadcast extends Volume{
		
	}
	
	class Alarm extends Volume{
		
	}
	
	class Phone extends Volume {
		
	}
}
