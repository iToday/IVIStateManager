package com.tomwin.tpms;
import com.tomwin.tpms.IRemoteListener;

interface ITpms{

	void registerListener(in IRemoteListener listener);
	
	void unregisterListener(in IRemoteListener listener);

	void queryId();
	
	void switchPos(int index);
	
	void queryBattery();
	
	void stopPair();
	
	void pair(int index);
	
	void requestTyres();
}
	