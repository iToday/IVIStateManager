package com.tomwin.tpms;

interface ITpmsListener {

	void onStateChange(Tyres tyres);
	void onId(Tyres tyres);
	void onBattery(Tyres tyres);
	void onPairState(int index, int state);
	void onSwitch(int index);
}
