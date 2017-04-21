#ifndef ANDROID_TUNER_RDS_H
#define ANDROID_TUNER_RDS_H

#include "include/radio.h"
#include "include/band.h"
#include <utils/Thread.h>
#include <stdint.h>
#include <sys/types.h>

#define RDS_TYPE_0A     ( 0 * 2 + 0)
#define RDS_TYPE_0B     ( 0 * 2 + 1)
#define RDS_TYPE_1A     ( 1 * 2 + 0)
#define RDS_TYPE_1B     ( 1 * 2 + 1)
#define RDS_TYPE_2A     ( 2 * 2 + 0)
#define RDS_TYPE_2B     ( 2 * 2 + 1)
#define RDS_TYPE_3A     ( 3 * 2 + 0)
#define RDS_TYPE_3B     ( 3 * 2 + 1)
#define RDS_TYPE_4A     ( 4 * 2 + 0)
#define RDS_TYPE_4B     ( 4 * 2 + 1)
#define RDS_TYPE_5A     ( 5 * 2 + 0)
#define RDS_TYPE_5B     ( 5 * 2 + 1)
#define RDS_TYPE_6A     ( 6 * 2 + 0)
#define RDS_TYPE_6B     ( 6 * 2 + 1)
#define RDS_TYPE_7A     ( 7 * 2 + 0)
#define RDS_TYPE_7B     ( 7 * 2 + 1)
#define RDS_TYPE_8A     ( 8 * 2 + 0)
#define RDS_TYPE_8B     ( 8 * 2 + 1)
#define RDS_TYPE_9A     ( 9 * 2 + 0)
#define RDS_TYPE_9B     ( 9 * 2 + 1)
#define RDS_TYPE_10A    (10 * 2 + 0)
#define RDS_TYPE_10B    (10 * 2 + 1)
#define RDS_TYPE_11A    (11 * 2 + 0)
#define RDS_TYPE_11B    (11 * 2 + 1)
#define RDS_TYPE_12A    (12 * 2 + 0)
#define RDS_TYPE_12B    (12 * 2 + 1)
#define RDS_TYPE_13A    (13 * 2 + 0)
#define RDS_TYPE_13B    (13 * 2 + 1)
#define RDS_TYPE_14A    (14 * 2 + 0)
#define RDS_TYPE_14B    (14 * 2 + 1)
#define RDS_TYPE_15A    (15 * 2 + 0)
#define RDS_TYPE_15B    (15 * 2 + 1)

#define CORRECTED_NONE          0
#define CORRECTED_ONE_TO_TWO    1
#define CORRECTED_THREE_TO_FIVE 2
#define UNCORRECTABLE           3
#define ERRORS_CORRECTED(data,block) ((data>>block)&0x03)

#define BLER_SCALE_MAX 200  // Block Errors are reported in .5% increments


#define AF_COUNT_MIN 225
#define AF_COUNT_MAX (AF_COUNT_MIN + 25)
#define AF_FREQ_MIN 0
#define AF_FREQ_MAX 204
#define AF_FREQ_TO_U16F(freq) (8750+((freq-AF_FREQ_MIN)*10))

#define RDS_PI_VALIDATE_LIMIT  4
#define RDS_PTY_VALIDATE_LIMIT 4
#define PS_VALIDATE_LIMIT 2
#define RT_VALIDATE_LIMIT 2

#define PS_DATA_BUFF_SIZE 8
#define RT_DATA_BUFF_SIZE 64

using namespace android;

class IRdsListener;

/**
 * RDS 信息解析器
 */
class RdsParser{
private:
	unsigned char afCount;
	unsigned short afList[25];

	unsigned char rtblocks[4];
	unsigned char group_type;      // bits 4:1 = type,  bit 0 = version
	unsigned char addr;
	unsigned char errorCount;
	unsigned char abflag;

	unsigned short piDisplay;
	unsigned char ptyDisplay;
	unsigned char psDisplay[PS_DATA_BUFF_SIZE];
	unsigned char rtDislpay[RT_DATA_BUFF_SIZE];

	unsigned char rtSimple[RT_DATA_BUFF_SIZE];    // Simple Displayed Radio Text
	unsigned char rtTmp0[RT_DATA_BUFF_SIZE];      // Temporary Radio Text (high probability)
	unsigned char rtTmp1[RT_DATA_BUFF_SIZE];      // Temporary radio text (low probability)
	unsigned char rtCnt[RT_DATA_BUFF_SIZE];       // Hit count of high probabiltiy radio text
	unsigned char rtFlag;          // Radio Text A/B flag
	unsigned char rtFlagValid;     // Radio Text A/B flag is valid
	unsigned char rtsFlag;         // Radio Text A/B flag
	unsigned char rtsFlagValid;    // Radio Text A/B flag is valid

	unsigned char BleA;
	unsigned char BleB;
	unsigned char BleC;
	unsigned char BleD;

	unsigned char rdsBlerMax[4];

	// RDS Program Service

	unsigned char psTmp0[PS_DATA_BUFF_SIZE];       // Temporary PS text (high probability)
	unsigned char psTmp1[PS_DATA_BUFF_SIZE];       // Temporary PS text (low probability)
	unsigned char psCnt[PS_DATA_BUFF_SIZE];        // Hit count of high probability PS text

	// RDS Clock Time and Date
	unsigned char ctDayHigh;       // Modified Julian Day high bit
	unsigned char ctDayLow;        // Modified Julian Day low 16 bits
	unsigned char ctHour;          // Hour
	unsigned char ctMinute;        // Minute
	unsigned char ctOffset;        // Local Time Offset from UTC
private:
	IRdsListener* listener;

public:
	RdsParser(IRdsListener* client);
	~RdsParser();
	bool parse(unsigned char *info, int len);
private:
	void update_clock(unsigned short b, unsigned short c, unsigned short d);
	void update_alt_freq(unsigned short current_alt_freq);
	void update_rt_advance(unsigned char abFlag, unsigned char count, unsigned char addr, unsigned char * byte);
	void display_rt(void);
	void update_rt_simple(unsigned char abFlag, unsigned char count, unsigned char addr, unsigned char * chars);
	void update_ps(unsigned char addr, unsigned char byte);
	void update_pty(unsigned char current_pty);
	void update_pi(unsigned short current_pi);
};

/**
 * RDS 监听器
 */
class IRdsListener{
public:
	virtual ~IRdsListener(){}
	virtual void onPi(unsigned char pi) = 0;
	virtual void onPty(unsigned char pty) = 0;
	virtual void onPs(char* ps) = 0;
	virtual void onAltFreqs(int* freqs, int len) = 0;
	virtual void onRadioText(char* text) = 0;
};
#endif
