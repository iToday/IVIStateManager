package com.itoday.ivi;

interface IKeyListener{

	int onKeySetupResult(int adc1, int adc2, int adc3);
	int onKeySetupStatus(int status);
}