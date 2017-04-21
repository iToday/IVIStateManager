#ifndef ANDROID_CANBUS_INTERFACE_H
#define ANDROID_CANBUS_INTERFACE_H
#include <hardware/hardware.h>


__BEGIN_DECLS

#define RADIO_HARDWARE_MODULE_ID "radio"

typedef   unsigned char   byte;

#define RADIO_ERROR  -1

#define RDS_BUFFER_SIZE 16

struct radio_module_t {
	struct hw_module_t common;
};

struct radio_device_t {
	
	struct hw_device_t common;
	
	int fd;
	
	int (*set_freq)(struct radio_device_t *dev, int frequency, int band);
	
	int (*get_rssi)(struct radio_device_t *dev, int frequency, int band);

	int (*get_rds)(struct radio_device_t * dev, unsigned char* buffer, int* len);

	int (*is_support_rds)(struct radio_device_t* dev);

	int (*set_stereo)(struct radio_device_t *dev, int freq);

	int (*is_stereo)(struct radio_device_t *dev, int freq);
	
	int (*set_mute)(struct radio_device_t *dev, int mute);
	
	int (*set_volume)(struct radio_device_t *dev, int volume);
	
};

__END_DECLS

#endif
