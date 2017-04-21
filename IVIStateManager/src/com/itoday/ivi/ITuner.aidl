package com.itoday.ivi;
import com.itoday.ivi.ITunerListener;
import com.itoday.ivi.platform.IVIChannel;
interface ITuner{

	void registerTunerListener(in ITunerListener listener);
	
	void unregisterTunerListener(in ITunerListener listener);

	int getFmMin(int area);
	
	int getFmMax(int area);
	
	int getFmStep(int area);
	
	int getAmMin(int area);
	
	int getAmMax(int area);
	
	int getAmStep(int area);
	
	int open();
	
	int close();
	
	int setFreq(in IVIChannel channel);
	
	int setFavor(int index, in IVIChannel channel);
	
	IVIChannel getFreq();
	
	int isStereo();
	
	List<IVIChannel> getFavorLists(int band);
	
	int setStereo(int on);
	
	int setArea(int area);
	
	int setLoc(int loc);
	
	int getState();
	
	int getArea();
	
	int seekUp();
	
	int seekDown();
	
	int scanUp();
	
	int scanDown();
	
	int scanSave();
	
	int stop();
}
	