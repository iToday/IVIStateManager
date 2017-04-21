#include "../include/band.h"
#define FMmin 			87500
#define FMmax 			108000
#define FMTuneStep 		100


#define LWmax 			288
#define LWmin 			144
#define LWTuneStep 		9


#define MWmax 			1620 //k216
#define MWmin 			522
#define MWTuneStep 		9

#define SWmax 			27000
#define SWmin 			2300
#define SWTuneStep 		5

#define OIRTmax 		74000
#define OIRTmin 		65000
#define OIRTTuneStep 	10

#define MIN_FREQ			0
#define MAX_FREQ			1
#define TUNE_STEP			2
#define SCALE_FREQ		3
#define DEFAULT_FREQ  4

static int band_msg[6][6] = {
		/*FM MIN,FM MAX, STEP, AM MIN, AM_MAX,STEP*/
		// China
		{87500, 108000, 100, 522, 1620, 9},
		// EUROPE
		{87500,108000, 50, 522,1620, 9},
		// USA
		{87500,107900,100, 530,1710,10},
		// korea
		{87500,108000,100,522,1620, 9},
		// JAPAN
		{76000,90000,100,522,1629,9},
		// OIRT RUSSIA
		{65000,77000,30,522,1620,9},

	};

		
int get_min(int band, int area){
	return band_msg[area][band == FM ? FM_MIN : AM_MIN];
}

int get_max(int band, int area){
	return  band_msg[area][band == FM ? FM_MAX : AM_MAX];
}

int get_step(int band, int area){
	return  band_msg[area][band == FM ? FM_STEP : AM_STEP];
}

int get_default(int band, int area){
	return  band_msg[area][band == FM ? FM_MIN : AM_MIN];
}

bool is_valid_band(int band){
	
	return band == FM || band == AM;
	
}

bool is_valid_freq(int band, int freq, int area){
	
	if (!is_valid_band(band))
		return false;
		
	//not in range
	if (freq < get_min(band, area) || freq > get_max(band, area))
		return false;
	
	//not valid step
	if ((freq - get_min(band, area)) % get_step(band, area) != 0)
		return false;
		
	return true;
}
		
