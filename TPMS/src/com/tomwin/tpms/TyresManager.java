package com.tomwin.tpms;

import java.util.ArrayList;

import com.tomwin.tpms.IntObserver.OnIntDataChange;

public class TyresManager {
	
	public static final String LEFT_FRONT = "tyres.left.front";
	
	public static final int LEFT_FRONT_INDEX = 0x00;
	
	public static final String RIGHT_FRONT = "tyres.right.front";
	
	public static final int RIGHT_FRONT_INDEX = 0x01;
	
	public static final String LEFT_REAR = "tyres.left.rear";
	
	public static final int LEFT_REAR_INDEX = 0x10;
	
	public static final String RIGHT_REAR = "tyres.right.rear";
	
	public static final int RIGHT_REAR_INDEX = 0x11;
	
	//switch
	public static final int LEFT_FRONT_RIGHT_FRONT = 0x01;
	
	public static final int LEFT_FRONT_LEFT_REAR = 0x02;
	
	public static final int LEFT_FRONT_RIGHT_REAR = 0x03;
	
	public static final int RIGHT_FRONT_LEFT_REAR = 0x04;
	
	public static final int RIGHT_FRONT_RIGHT_REAR = 0x05;
	
	public static final int LEFT_REAR_RIGHT_REAR = 0x06;
	
	
	//默认高压警报！6 = 320 7 = 330  每加一加10kpa
	public static final int HIGH_LIMIT = 6;
	//默认低压警报！0 = 180 1 = 190  每加一加10kpa
	public static final int LOW_LIMIT = 0;
	//默认高温警报！5 = 75度 6 = 80度 每加一五度
	public static final int T_HIGH_LIMIT = 4;

	private ArrayList<Tyres> mTyress = new ArrayList<Tyres>();
	
	private IntObserver mHighPressureLevelObserver;
	
	private IntObserver mLowPressureLevelObserver;
	
	private IntObserver mHighTempLevelObserver;
	
	private int mHighPressureLevel;
	
	private int mLowPressureLevel;
	
	private int mHighTempLevel;
	
	public TyresManager(){
		
		mTyress.add(new Tyres(LEFT_FRONT, LEFT_FRONT_INDEX));
		mTyress.add(new Tyres(RIGHT_FRONT, RIGHT_FRONT_INDEX));
		mTyress.add(new Tyres(LEFT_REAR, LEFT_REAR_INDEX));
		mTyress.add(new Tyres(RIGHT_REAR, RIGHT_REAR_INDEX));
		
		mHighPressureLevelObserver = new IntObserver(IVIDataManager.HIGH_PRESSURE);
		
		mHighPressureLevel = mHighPressureLevelObserver.getValue(HIGH_LIMIT);
		
		mHighPressureLevelObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mHighPressureLevel = newState;
				return 0;
			}
			
		});
		
		mLowPressureLevelObserver = new IntObserver(IVIDataManager.LOW_PRESSURE);
		
		mLowPressureLevel = mLowPressureLevelObserver.getValue(LOW_LIMIT);
		
		mLowPressureLevelObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mLowPressureLevel = newState;
				return 0;
			}
			
		});
		
		mHighTempLevelObserver = new IntObserver(IVIDataManager.HIGH_TEMP);
		
		mHighTempLevel = mHighTempLevelObserver.getValue(T_HIGH_LIMIT);
		
		mHighTempLevelObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mHighTempLevel = newState;
				return 0;
			}
			
		});
	}
	
	public Tyres findTyres(int index){
		
		for (Tyres tyres : mTyress){
			if (tyres.getIndex() == index){
				return tyres;
			}
		}
		
		return null;
	}
	
	public boolean isHaveWarning(){
		
		for (Tyres tyres : mTyress){
			if (tyres.getBattery() > 0 || tyres.getLeak() > 0)
				return true;
			
			if (tyres.isHighPressure(mHighPressureLevel) 
					|| tyres.isLowPressure(mLowPressureLevel)
					|| tyres.isHighTemp(mHighTempLevel)){
				return true;
			}
		}
		
		return false;
	}

	public ArrayList<Tyres> getTyres() {
		return mTyress;
	}
}
