package com.tomwin.tpms;
import com.tomwin.tpms.Tyres;
interface IRemoteListener{

	void onStateChange(in Tyres tyres);

	void onId(in Tyres tyres);

	void onBattery(in Tyres tyres) ;

	void onPairState(int index, int state);

	void onSwitch(int index);
}