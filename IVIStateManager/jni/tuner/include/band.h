#ifndef TUNER_BAND_H
#define TUNER_BAND_H

#include <sys/types.h>

#include <utils/Errors.h>
#include <utils/RefBase.h>
#include <stdlib.h>
#include <binder/IInterface.h>


#define BAND_FM             0
#define BAND_FM_WB          1
#define BAND_AM_MW          2
#define BAND_AM_LW          3
#define BAND_AM_SW          4
#define BAND_OIRT           5
#define BAND_NUMBER         6

#define BAND_DEFAULT        BAND_FM

#define BAND_MIN   BAND_FM
#define BAND_MAX   BAND_OIRT

#define FM  1
#define AM  0

#define BAND_NUM 2

#define FM_MIN 0
#define FM_MAX 1
#define FM_STEP 2

#define AM_MIN 3
#define AM_MAX 4
#define AM_STEP 5



#define AREA_NUM  6

int get_min(int band, int area);
int get_max(int band, int area);
int get_step(int band, int area);

int get_default(int band, int area);

bool is_valid_band(int band);

bool is_valid_freq(int band, int freq, int area);


#endif
