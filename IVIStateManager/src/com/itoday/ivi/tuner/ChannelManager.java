package com.itoday.ivi.tuner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.itoday.ivi.platform.IVIChannel;
import com.itoday.ivi.platform.IVITuner;

public class ChannelManager {
	
	private static final String TAG = "ChannelManager";
	
	private static final String AM_PATH = "am.xml";
	
	private static final String FM_PATH = "fm.xml";
	
	private Context mContext;
	
	private ArrayList<IVIChannel> mFms = new ArrayList<IVIChannel>();
	
	private ArrayList<IVIChannel> mAms = new ArrayList<IVIChannel>();
	
	private boolean needStore = false;
	
	public ChannelManager(Context context){
		mContext = context;
		
		load(AM_PATH, mAms);
		load(FM_PATH, mFms);
	}

	public void release(){
		
		if (needStore){
			store(AM_PATH, mAms);
			store(FM_PATH, mFms);
			
			needStore = false;
		}
	}
	
	public ArrayList<IVIChannel> getFavors(int band){
		
		return band == IVITuner.Band.FM ? mFms : mAms;
	}
	
	public int findFreqPos(int freq, int band){
		
		ArrayList<IVIChannel> channels = band == IVITuner.Band.FM ? mFms : mAms;
		
		return findFreqPos(freq, channels);
	}
	
	public IVIChannel getChannel(int band, int pos){
		
		ArrayList<IVIChannel> channels = band == IVITuner.Band.FM ? mFms : mAms;
		
		if (pos < 0)
			pos = channels.size() - 1;
		else if (pos >= channels.size())
			pos = 0;
		
		if (channels.size() > 0)
			return channels.get(pos);
		else
			return null;
	}
	
	public int findChannelPos(IVIChannel channel){
		
		ArrayList<IVIChannel> channels = channel.getBand() == IVITuner.Band.FM ? mFms : mAms;
		
		return findFreqPos(channel.getFreq(), channels);
	}
	
	private int findFreqPos(int freq, ArrayList<IVIChannel> channels){
		
		for (int index = 0; index < channels.size(); index ++){
			IVIChannel channel = channels.get(index);
			
			if (channel.getFreq() == freq){
				return index;
			}
		}
		
		return -1;
	}
	
	public IVIChannel getFirst(int band){
		ArrayList<IVIChannel> favors = mFms; 
		
		if (band == IVITuner.Band.AM){
			favors = mAms;
		}
		
		if (favors.size() > 0)
			return favors.get(0);
		
		return null;
	}
	
	public int getFavorsSize(int band){
		if (band == IVITuner.Band.FM)
			return mFms.size();
		else
			return mAms.size();
	}
	
	public void clear(int band){
		if (band == IVITuner.Band.FM)
			mFms.clear();
		else
			mAms.clear();
		
		needStore = true;
	}
	
	public void clear(){
		
		mFms.clear();
		mAms.clear();
	}
	
	public int  addChannel(IVIChannel channel){
		
		int key = 0;
		if (channel.getBand() == IVITuner.Band.FM){
			mFms.add(channel);
			key = mFms.size();
		}else{
			mAms.add(channel);
			key = mAms.size();
		}
		
		needStore = true;
		
		return key;
	}
	
	public void setChannel(int index, IVIChannel channel){
		
		int band = channel.getBand();
		int freq = channel.getFreq();
		
		ArrayList<IVIChannel> channels = band == IVITuner.Band.FM ? mFms : mAms;
		
		int pos = findFreqPos(freq, channels);
		
		if (pos >= 0){//存在重复电台
			
			if (channels.size() > index){//将重复电台与覆盖电台调换位置
				IVIChannel oldChannel = channels.get(index);
				channels.set(pos, oldChannel);
				channels.set(index, channel);
			}else { //删除重复电台，并重新添加到尾部
				channels.remove(pos);
				channels.add(channel);
			}
			
		} else { //无重复电台
			
			if (channels.size() > index)
				channels.set(index, channel);
			else
				channels.add(channel);
		}
		
		needStore = true;
	}
	
	private void load(String path, ArrayList<IVIChannel> favors){
		 XmlPullParser parser = Xml.newPullParser(); 
		 try {
			FileInputStream fs=  mContext.openFileInput(path);
			
			try {
				
				parser.setInput(fs, "UTF-8");
				int eventType = parser.getEventType();
				
				int key = 0;
				int freq = 0;
				int band = 0;
				String station = null;
				
				while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {  
					switch (eventType) {  
					case XmlPullParser.START_TAG: {
						String name = parser.getName();  
						
						if (name.equals("channel")){
							
						} else if (name.equals("freq")){
							freq = Integer.valueOf(parser.nextText());
						} else if (name.equals("band")){
							band = Integer.valueOf(parser.nextText());
						}  else if (name.equals("name")){
							station = parser.nextText();
						} else if (name.equals("index")){
							key = Integer.valueOf(parser.nextText());
						}
					}break;
					case XmlPullParser.END_TAG: {
						String name = parser.getName(); 
						
						if (name.equals("channel"))
							favors.add(key, new IVIChannel(freq, band, station));
					}break;
					}
				}
				
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	private void store(String path, ArrayList<IVIChannel> favors){
		
		String str = toXmlString(favors);
		OutputStream out = null;
		
		Log.d(TAG, "store :" + path + " \n" + str);
		
		try {
			out = mContext.openFileOutput(path, Context.MODE_PRIVATE);
			
			OutputStreamWriter outw = new OutputStreamWriter(out); 
			
			try {
				outw.write(str);
				outw.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private String toXmlString(ArrayList<IVIChannel> favors){
		
		XmlSerializer serializer = Xml.newSerializer(); 
		
		StringWriter writer = new StringWriter();
		
		try {
			serializer.setOutput(writer);
			
			serializer.startDocument("utf-8", true);    
			 
			serializer.startTag(null, "favors"); 
			
			serializer.attribute(null, "type", "list");  
			
			for (int key = 0; key < favors.size(); key ++) {
				
			    IVIChannel val = (IVIChannel) favors.get(key);
			    
			    serializer.startTag(null, "channel");
			    
			    serializer.startTag(null, "index");
			    serializer.text(key + ""); 
			    serializer.endTag(null, "index");  
			    
			    serializer.startTag(null, "freq");
			    serializer.text(val.getFreq() + ""); 
			    serializer.endTag(null, "freq");
			    
			    serializer.startTag(null, "band");
			    serializer.text(val.getBand() + ""); 
			    serializer.endTag(null, "band");  
			    
			    serializer.startTag(null, "name");
			    serializer.text(val.getName() + ""); 
			    serializer.endTag(null, "name");  
			    
			    serializer.endTag(null, "channel"); 
			}
			
			serializer.endTag(null, "favors"); 
			serializer.endDocument();  
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		return writer.toString();
	}
}
