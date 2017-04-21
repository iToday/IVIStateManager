#ifndef ANDROID_PROTOCOL_H
#define ANDROID_PROTOCOL_H

/**
**协议格式定义
**   SYNC1 + SYNC2 + CTRL + DATA_LEN + DATA + SUM
**/	
//数据同步字1
#define  SYNC1  0x5A		
//数据同步字2
#define SYNC2  0xA5

//同步字的位置
#define SYNC_POS  0						
//控制字的位置
#define CTRL_POS  2
//长度字的位置
#define LENGTH_POS  3
//信息字的开始位置
#define DATA_POS  4
		
//同步字长度
#define SYNC_LEN  2
//长度字长度
#define LENGTH_LEN  1
//校验字长度
#define CHECKSUM_LEN  1
//控制字长度
#define CTRL_LEN  1

//控制字定义
#define CTRL_SYSTEM  		0x0
#define CTRL_CANBOX		0x1
#define CTRL_TIME		0x2
#define CTRL_VOLUME		0x3
#define CTRL_KEY		0x4
#define CTRL_FLAG		0x5
#define CTRL_COLOR_LED		0x6
#define CTRL_POWER		0x7
#define CTRL_VERSION		0x8
#define CTRL_ACC_STATUS 	0x9
#define CTRL_TV_POWER		0xa
#define CTRL_HAND_SHAKE 	0xb
//#define CTRL_CANBOX     	0xc
#define CTRL_UPDATE			0xd
#define CTRL_GPS_VOLUME 	0xe
#define CTRL_IPOD_STATUS 	0xf
#define CTRL_BT_POWER		0x10
#define CTRL_DVR_POWER		0x11
#define CTRL_ANT_POWER		0x12
#define CTRL_EX_AMP		0x13

#define CTRL_MCU_UPGRADE 0xF0

//当前系统状态
enum SYSTEM {
	MAIN = 0,
	RADIO,
	TV,
	KTV,
	MUSIC,
	MOVIE,
	NAVI,
	AVIN,
	BACK_CAR,
	BLUETOOTH,
	FRONT_AVIN,
	DVD,
	USB,
	SDCARD,
	PICTURE,
	EQ,
	A2DP,
	CMMB,
	IPOD,
	VDISC,
	EX_DVD,
	AVM, //全景泊车
	IEXPLORER,
	WIFI_SET,
	HELP_ICON,
	HDMI,
	CAR_SWITCH,
	ANALOG_TV,
	UNKNOWN,
	DVR,
	CAMERA,
};

enum KEY_CTRL{
	KEY_VALUE,
	KEY_SET,
	KEY_DVD,
	KEY_READ,
	KEY_TOUCH_KEY,
	KEY_DVD_POWER,
	KEY_TOUCH_COOR,
	KEY_DRV,
};

enum DEVICE{
	DEV_TV = 0xA,
	DEV_IPOD = 0xF,
	DEV_BT = 0x10,
	DEV_DVR,
	DEV_FM_ANT,
};


enum AUDIO{
	MUTE = 0x01,
	OTHER = 0x05,
};

enum AUDIO_EX{

	SOURCE = 0x05,
	INPUT_GAIN = 0x06,
	VOLUME = 0x20,
	FR_SPEAKER_GAIN = 0x28,
	FL_SPEAKER_GAIN = 0x29,
	RR_SPEAKER_GAIN = 0x2a,
	RL_SPEAKER_GAIN = 0x2b,
	SUBWOOFER_GAIN = 0x2c,
	MIX_GAIN = 0x30,
	BASS_Q = 0x41,
	MID_Q = 0x44,
	TREBLE_Q = 0x47,
	BASS_GAIN = 0x51,
	MID_GAIN = 0x54,
	TRELE_GAIN = 0x57,
	LOUDNESS_GAIN = 0x75
};

enum TIME{
	SET = 0x01,
	GET = 0x02,
};

enum CANBOX{
	BESTURN_X80 = 0x01,
	REQUEST = 0x02,
};
#endif
