package com.tomwin.tpms;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * 胎压信息
 * @author Yijun
 *
 */
public class Tyres implements Parcelable{
	
	private static final String tag = "Tyres";
	
	public static final int UNIT_KPA = 0;
	
	public static final int UNIT_PSI = 1;
	
	public static final int UNIT_BAR = 2;
	
	//TEMP
	public static final int UNIT_C = 0;
	
	public static final int UNIT_F = 1;
	
	//PAIR
	public static final int PAIRING = 0x10;// paring, 
	public static final int PAIRED = 0x18; // success, 
	public static final int STOPED = 0x16;// stoped
	
	private static final String[] UNIT_PRESSURE = {
		"Kpa",
		"Psi",
		"Bar",
	};
	
	private static final String [] UNIT_TEMP = {
		"℃",
		"℉",
	};
	
	private String name;
	
	private int index;
	
	private String id;

	private int pressure;
	
	private int temp;
	
	private int leak;
	
	private int battery;
	
	private int signal;
	
	public Tyres(String name, int index) {
		super();
		this.name = name;
		this.index = index;
	}
	
	public Tyres(Parcel source){
		name = source.readString();
		index = source.readInt();
		id = source.readString();
		pressure = source.readInt();
		temp = source.readInt();
		leak = source.readInt();
		battery = source.readInt();
		signal = source.readInt();
	}
	
	public int set(int pressure, int temp, int leak, int battery, int signal){
		
		int ret = 0;
		
		if (this.pressure != pressure){
			this.pressure = pressure;
			
			ret = 1;
		}
		
		if (this.temp != temp){
			this.temp = temp;
			
			ret = 1;
		}
		
		if (this.leak != leak){
			this.leak = leak;
			
			ret = 1;
		}
		
		if (this.battery != battery){
			this.battery = battery;
			
			ret = 1;
		}
		
		if (this.signal != signal){
			this.signal = signal;
			
			ret = 1;
		}
		
		Log.d(tag, this.toString()); 
		
		if (ret == 1)
			IVIDataManager.instance().putString(name, toString());
		
		return ret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPressure() {
		return pressure;
	}
	
	public double getPressure(int unit){
		
		double data = (pressure & 0xFF) * 3.44f;
		
		switch (unit){
		case UNIT_KPA:
			data = (Math.round(data * 10)) / 10.0f;
			break;
		case UNIT_PSI:
			data = (Math.round(data / 6.89 * 10)) / 10.0f;
			break;
		case UNIT_BAR:
			data = (Math.round(data / 10)) / 10.0f;
			break;
		default:
			break;
		}
		
		return data;
	}
	
	public boolean isHighPressure(int level){
		return ((int)(Integer.valueOf(Integer.toBinaryString(pressure & 0xFF), 2) * 3.44) 
				> (level * 10 + 250)) ? true : false;
	}
	
	public boolean isLowPressure(int level){
		return ((int)(Integer.valueOf(Integer.toBinaryString(pressure & 0xFF), 2) * 3.44) 
				< (level * 10 + 180)) ? true : false;
	}
	
	public boolean isHighTemp(int level){
		return  getTemp(UNIT_C) > (level * 5 + 50) ? true : false;
	}

	public void setPressure(int pressure) {
		
		if (this.pressure != pressure){
			
			this.pressure = pressure;
			
			IVIDataManager.instance().putString(name, toString());
		}
	}

	public int getTemp() {
		return temp;
	}
	
	public double getTemp(int unit){
		
		double data = Integer.valueOf(Integer.toBinaryString(temp & 0xFF), 2) - 50;
		
		switch (unit){
		case UNIT_F:
			data = (Math.round((data * 1.8 + 32) * 10)) / 10.f;
			break;
		case UNIT_C:
			break;
		default:
			break;
		}
		
		return data;
	}

	public void setTemp(int temp) {
		
		if (this.temp != temp){
			this.temp = temp;
			
			IVIDataManager.instance().putString(name, toString());
		}
	}

	public int getLeak() {
		return leak;
	}

	public void setLeak(int leak) {
		
		if (this.leak != leak){
			this.leak = leak;
			
			IVIDataManager.instance().putString(name, toString());
		}
	}

	public int getBattery() {
		return battery;
	}

	public void setBattery(int battery) {
		
		if (this.battery != battery){
			this.battery = battery;
			
			IVIDataManager.instance().putString(name, toString());
		}
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int signal) {
		
		if (this.signal != signal){
			this.signal = signal;
			
			IVIDataManager.instance().putString(name, toString());
		}
	}

	public int getIndex() {
		return index;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		
		if (this.id != id){
			this.id = id;
			
			IVIDataManager.instance().putString(name, toString());
		}
	}
	
	public static String getPressureUnitDescriptor(int unit){
		
		return UNIT_PRESSURE[unit];
	}
	
	public static String getTempUnitDescriptor(int unit){
		return UNIT_TEMP[unit];
	}
	
	@Override
	public String toString() {
		return "Tyres [name=" + name + ", index=" + index + ", pressure="
				+ pressure + ", temp=" + temp + ", leak=" + leak + ", battery="
				+ battery + ", signal=" + signal + ", id=" + id + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeString(name);
		dest.writeInt(index);
		dest.writeString(id);
		dest.writeInt(pressure);
		dest.writeInt(temp);
		dest.writeInt(leak);
		dest.writeInt(battery);
		dest.writeInt(signal);
	}
	
	public static final Parcelable.Creator<Tyres> CREATOR  = new Parcelable.Creator<Tyres>() {

		@Override
		public Tyres[] newArray(int size) {
			return new Tyres[size];
		}
		@Override
		public Tyres createFromParcel(Parcel source) {
			return new Tyres(source);
		}

	};
}
