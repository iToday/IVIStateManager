package com.itoday.ivi.platform;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 电台
 * @author itoday
 *
 */
public class IVIChannel implements Parcelable{
	
	private int mFreq;
	
	private int mBand;
	
	private String mName;
	
	public IVIChannel(int freq, int band, String name){
		
		mFreq = freq;
		mBand = band;
		mName = name;
	}

	public int getFreq() {
		return mFreq;
	}

	public void setFreq(int freq) {
		this.mFreq = freq;
	}

	public int getBand() {
		return mBand;
	}

	public void setBand(int band) {
		this.mBand = band;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	
	@Override
	public String toString() {
		return "IVIChannel [mFreq=" + mFreq + ", mBand=" + mBand + ", mName="
				+ mName + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		
		dest.writeInt(mFreq);
		dest.writeInt(mBand);
		dest.writeString(mName);
	}
	
	public static final Parcelable.Creator<IVIChannel> CREATOR  = new Parcelable.Creator<IVIChannel>() {

		@Override
		public IVIChannel[] newArray(int size) {
			return new IVIChannel[size];
		}
		@Override
		public IVIChannel createFromParcel(Parcel source) {
			return new IVIChannel( source.readInt(), source.readInt(),source.readString());
		}

	};
	
	public void readFromParcel(Parcel _reply) {
		
		this.mFreq = _reply.readInt();
		this.mBand = _reply.readInt();
		this.mName = _reply.readString();
	}
	
}
