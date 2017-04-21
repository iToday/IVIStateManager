package com.tomwin.tpms;

import android.util.Log;

public class Parser {
	
	private static final String TAG = "Parser";
	
	private static final byte SYNC_1 = 0x55;
	
	private static final byte SYNC_2 = (byte) 0xAA;
	
	private static final int STATE_LEN = 8;
	
	private static final int PAIR_LEN = 6;
	
	private static final int ID_LEN = 9;
	
	private static final int BATTERY_LEN = 0x0a;
	
	private static final int SWITCH_LEN = 7;
	
	
	private static final int SYNC_1_POS = 0;
	
	private static final int SYNC_2_POS = 1;
	
	private static final int LEN_POS = 2;
	
	private static final int SENSOR_POS = 3;
	
	private static final int TEMP_POS = 5;
	
	private static final int PRESSURE_POS = 4;
	
	private static final int WARNING_POS = 6;
	
	// 漏气
	private static final int BIT_LEAK = 3;
	//电压低
	private static final int BIT_BATTERY_LOW = 4;
	//信号异常
	private static final int BIT_SIGNAL_ERROR = 5;
	
	private TyresManager mManager;
	private ITpmsListener mListener;
	
	private byte[] mBuffer = new byte[4096];
	private int mValidDataSize = 0;
	
	public Parser(TyresManager manager, ITpmsListener listener){
		mManager = manager;
		mListener = listener;
	}
	
	public int parse(byte[] buffer){
		
		if (buffer.length >= 0){
			
			if (buffer.length >= mBuffer.length)
				return -1;
			
			if ((mValidDataSize + buffer.length) >= mBuffer.length){
				mValidDataSize = 0;
			}
			
			System.arraycopy(buffer, 0, mBuffer, mValidDataSize, buffer.length);
			mValidDataSize += buffer.length;
			
			int newStart = 0;
			
			for (int index = 0; index < mValidDataSize - LEN_POS; index ++){
				
				if (mBuffer[index + SYNC_1_POS] == SYNC_1 && mBuffer[index + SYNC_2_POS] == SYNC_2 ){
					
					log("buffer len is " + mBuffer[index + LEN_POS]);
					
					if (mBuffer[index + LEN_POS] == STATE_LEN){
						
						parseState(mBuffer, index);
						
						index += STATE_LEN;
						
						newStart = index;
					} else if (mBuffer[index + LEN_POS] == SWITCH_LEN){
						
						parseSwitch(mBuffer, index);
						index += SWITCH_LEN;
						
						newStart = index;
						
					}else if (mBuffer[index + LEN_POS] == BATTERY_LEN){
						
						parseBattery(mBuffer, index);
						index += BATTERY_LEN;
						
						newStart = index;
						
					}else if (mBuffer[index + LEN_POS] == ID_LEN){
						
						parseId(mBuffer, index);
						index += ID_LEN;
						
						newStart = index;
						
					}else if (mBuffer[index + LEN_POS] == PAIR_LEN){
						
						parsePair(mBuffer, index);
						index += PAIR_LEN;
						
						newStart = index;
					}
				}
			}
			
			if (newStart > 0){
				
				byte[] newBuffer = mBuffer.clone();
				System.arraycopy(newBuffer, newStart, mBuffer, 0, mValidDataSize - newStart);
				mValidDataSize = mValidDataSize - newStart;
				
				if (mValidDataSize < 0) mValidDataSize = 0;
			}
		}
		
		return 0;
	}
	
	
	
	private void parseId(byte[] buffer, int start){
		
		int index  = buffer[start + LEN_POS + 1];
		
		String id = byteToHexString(buffer[start + LEN_POS + 2]) 
				+ byteToHexString(buffer[start + LEN_POS + 3]) 
				+ byteToHexString(buffer[start + LEN_POS + 4]) 
				+ byteToHexString(buffer[start + LEN_POS + 5]);
		
		switch(index){
		case 4 :
			index = TyresManager.RIGHT_REAR_INDEX;
			break;
		case 1:
			index = TyresManager.LEFT_FRONT_INDEX;
			break;
		case 2:
			index = TyresManager.RIGHT_FRONT_INDEX;
			break;
		case 3:
			index = TyresManager.LEFT_REAR_INDEX;
			break;
		}
		
		Tyres tyres = mManager.findTyres(index);
		
		if (tyres != null){
		
			tyres.setId(id);
		
			if (mListener != null)
				mListener.onId(tyres);
		} else 
			Log.d(TAG, "no find tyres index " + index);
	}
	
	private void parseBattery(byte[] buffer, int start){
		
		int lf = buffer[start + LEN_POS + 3];
		int rf = buffer[start + LEN_POS + 4];
		int lr = buffer[start + LEN_POS + 5];
		int rr = buffer[start + LEN_POS + 6];
		
		Tyres tyres = mManager.findTyres(TyresManager.LEFT_FRONT_INDEX);
		
		if (tyres != null){
			
			tyres.setBattery(lf);
			
			if (mListener != null)
				mListener.onBattery(tyres);
		}
		
		tyres = mManager.findTyres(TyresManager.RIGHT_FRONT_INDEX);
		
		if (tyres != null){
			
			tyres.setBattery(rf);
			
			if (mListener != null)
				mListener.onBattery(tyres);
		}
		
		tyres = mManager.findTyres(TyresManager.LEFT_REAR_INDEX);
		
		if (tyres != null){
			
			tyres.setBattery(lr);
			
			if (mListener != null)
				mListener.onBattery(tyres);
		}
		
		tyres = mManager.findTyres(TyresManager.RIGHT_REAR_INDEX);
		
		if (tyres != null){
			
			tyres.setBattery(rr);
			
			if (mListener != null)
				mListener.onBattery(tyres);
		}
	}
	
	private void parseSwitch(byte[] buffer, int start){
		int index = -1;
		
		if (buffer[start + LEN_POS + 1] == 0x30){
			
			if (buffer[start + LEN_POS + 2] == 0x10){
				
				if (buffer[start + LEN_POS + 3] == 0x11)
					index = TyresManager.LEFT_REAR_RIGHT_REAR;
				
			} else if (buffer[start + LEN_POS + 2] == 0x01){
				
				if (buffer[start + LEN_POS + 3] == 0x11)
					index = TyresManager.RIGHT_FRONT_RIGHT_REAR;
				else  if (buffer[start + LEN_POS + 3] == 0x10)
					index = TyresManager.RIGHT_FRONT_LEFT_REAR;
				
			} else if (buffer[start + LEN_POS + 2] == 0x00){
				
				if (buffer[start + LEN_POS + 3] == 0x11)
					index = TyresManager.LEFT_FRONT_RIGHT_REAR;
				else  if (buffer[start + LEN_POS + 3] == 0x10)
					index = TyresManager.LEFT_FRONT_LEFT_REAR;
				else  if (buffer[start + LEN_POS + 3] == 0x01)
					index = TyresManager.LEFT_FRONT_RIGHT_FRONT;
			}
		}
		
		if (index != -1 && mListener != null)
			mListener.onSwitch(index);
			
	}
	
	private void parsePair(byte[] buffer, int start){
		
		int index = buffer[start + LEN_POS + 2];
		int state = buffer[start + LEN_POS + 1];//0x10 paring, 0x18 success, 0x16 stoped
		
		if (mListener != null)
			mListener.onPairState(index, state);
		
	}

	private void parseState(byte[] buffer, int start) {
		
		int sensor = buffer[start + SENSOR_POS];
		int pressure = buffer[start + PRESSURE_POS];
		int temp = buffer[start + TEMP_POS];
		
		int leak = (buffer[start + WARNING_POS] >> BIT_LEAK) & 1;
		int battery = (buffer[start + WARNING_POS] >> BIT_BATTERY_LOW) & 1;
		int signal = (buffer[start + WARNING_POS] >> BIT_SIGNAL_ERROR) & 1;
		
		Tyres tyres = mManager.findTyres(sensor);
		
		if (tyres != null){
			tyres.set(pressure, temp, leak, battery, signal);
			
			if (mListener != null)
				mListener.onStateChange(tyres);
		}
	}
	
	private byte getCheckSum(byte[] buffer, int len){
		
		byte sum = buffer[0];
		
		for (int index = 0; index < len - 1; index ++){
			sum ^= buffer[index + 1];
		}
		
		return sum;
	}
	
	public byte[] getQueryIdPack(){
		
		byte len = 6;
		
		byte[] buffer = new byte[len];
		buffer[SYNC_1_POS] = SYNC_1;
		buffer[SYNC_2_POS] = (byte) SYNC_2;
		buffer[LEN_POS] = len;
		buffer[LEN_POS + 1] = 7;
		buffer[LEN_POS + 2] = 0;
		buffer[LEN_POS + 3] = getCheckSum(buffer, len -1);
		
		return buffer;
	}
	
	public byte[] getSwitchPack(int index){
		
		byte len = 7;
		
		byte[] buffer = new byte[len];
		buffer[SYNC_1_POS] = SYNC_1;
		buffer[SYNC_2_POS] = (byte) SYNC_2;
		buffer[LEN_POS] = len;
		buffer[LEN_POS + 1] = 3;
		
		switch (index ){
		case TyresManager.LEFT_FRONT_RIGHT_FRONT:
			buffer[LEN_POS + 2] = 0x00;
			buffer[LEN_POS + 3] = 0x01;
			break;
		case TyresManager.LEFT_FRONT_LEFT_REAR:
			buffer[LEN_POS + 2] = 0x00;
			buffer[LEN_POS + 3] = 0x10;
			break;
		case TyresManager.LEFT_FRONT_RIGHT_REAR:
			buffer[LEN_POS + 2] = 0x00;
			buffer[LEN_POS + 3] = 0x11;
			break;
		case TyresManager.LEFT_REAR_RIGHT_REAR:
			buffer[LEN_POS + 2] = 0x10;
			buffer[LEN_POS + 3] = 0x11;
			break;
		case TyresManager.RIGHT_FRONT_LEFT_REAR:
			buffer[LEN_POS + 2] = 0x01;
			buffer[LEN_POS + 3] = 0x10;
			break;
		case TyresManager.RIGHT_FRONT_RIGHT_REAR:
			buffer[LEN_POS + 2] = 0x01;
			buffer[LEN_POS + 3] = 0x11;
			break;
		}
		
		buffer[LEN_POS + 4] = getCheckSum(buffer, len -1);
		
		return buffer;
	}
	
	public byte[] getQueryBatteryPack(){
		
		byte len = 6;
		
		byte[] buffer = new byte[len];
		buffer[SYNC_1_POS] = SYNC_1;
		buffer[SYNC_2_POS] = (byte) SYNC_2;
		buffer[LEN_POS] = len;
		buffer[LEN_POS + 1] = 2;
		buffer[LEN_POS + 2] = 1;
		buffer[LEN_POS + 3] = getCheckSum(buffer, len -1);
		
		return buffer;
	}
	
	public byte[] getStopPairPack(){
		
		byte len = 6;
		
		byte[] buffer = new byte[len];
		buffer[SYNC_1_POS] = SYNC_1;
		buffer[SYNC_2_POS] = SYNC_2;
		buffer[LEN_POS] = len;
		buffer[LEN_POS + 1] = 6;
		buffer[LEN_POS + 2] = 0;
		buffer[LEN_POS + 3] = getCheckSum(buffer, len -1);
		
		return buffer;
	}
	
	public byte[] getPairPack(int index){
		
		byte len = 6;
		
		byte[] buffer = new byte[len];
		buffer[SYNC_1_POS] = SYNC_1;
		buffer[SYNC_2_POS] = SYNC_2;
		buffer[LEN_POS] = len;
		buffer[LEN_POS + 1] = 1;
		buffer[LEN_POS + 2] = (byte) index;
		buffer[LEN_POS + 3] = getCheckSum(buffer, len -1);
		
		return buffer;
	}
	
	public static String byteToHexString(byte b) {
		String stmp = Integer.toHexString(b & 0xFF);
		stmp = (stmp.length() == 1) ? "0" + stmp : stmp;
		return stmp.toUpperCase();
	}
	
	private void log(String msg){
		Log.d(TAG, msg);
	}
}
