#define LOG_TAG "radio.hardward"

#include <hardware/hardware.h>
#include "../include/radio.h"
#include <fcntl.h>
#include <errno.h>
#include <cutils/log.h>
#include <cutils/atomic.h>
#include <linux/ioctl.h>

#define MODULE_NAME "iToday_radio"
#define MODULE_AUTHOR "Today"

#define	IOCSETFREQ		_IOC(_IOC_WRITE, 'r', 1, 0)
#define	IOCGETRSSI		_IOC(_IOC_READ|_IOC_WRITE, 'r', 2, 0)
#define IOCSETMUTE		_IOC(_IOC_WRITE,'r', 3, 0)
#define IOCGETSTEREO	_IOC(_IOC_READ, 'r', 4, 0)
#define IOCSETVOLUME	_IOC(_IOC_WRITE, 'r', 5, 0)
#define IOCSETSTEREO	_IOC(_IOC_WRITE, 'r', 6, 0)
#define IOCGETRDSINFO	_IOC(_IOC_READ, 'r', 7, 0)
#define IOCISRDS		_IOC(_IOC_READ, 'r', 8, 0)

static int radio_device_open(const struct hw_module_t* module, const char* name, struct hw_device_t** device);

static int radio_device_close(struct hw_device_t* device);

static int set_freq(struct radio_device_t *dev, int frequency, int band);
	
static int get_rssi(struct radio_device_t *dev, int frequency, int band);
	
static int set_stereo(struct radio_device_t *dev, int stereo);

static int is_stereo(struct radio_device_t *dev,int freq);

static int set_mute(struct radio_device_t *dev, int mute);

static int set_volume(struct radio_device_t *dev, int volume);

static int get_rds(struct radio_device_t * dev, unsigned char* buffer, int* len);

static int is_support_rds(struct radio_device_t* dev);

static struct hw_module_methods_t radio_module_methods = {
	open: radio_device_open
};

struct radio_module_t HAL_MODULE_INFO_SYM = {
	common: {
		tag: HARDWARE_MODULE_TAG,
		version_major: 1,
		version_minor: 0,
		id: RADIO_HARDWARE_MODULE_ID,
		name: MODULE_NAME,
		author: MODULE_AUTHOR,
		methods: &radio_module_methods,
	}
};

static int radio_device_open(const struct hw_module_t* module, const char* name, struct hw_device_t** device) {
	
	struct radio_device_t* dev;
	int oflags;
	
	dev = (struct radio_device_t*)malloc(sizeof(struct radio_device_t));
	
	if(!dev) {
		return -EFAULT;
	}

	memset(dev, 0, sizeof(struct radio_device_t));
	dev->common.tag = HARDWARE_DEVICE_TAG;
	dev->common.version = 0;
	dev->common.module = (hw_module_t*)module;
	dev->common.close = radio_device_close;
	dev->set_freq = set_freq;
	dev->get_rssi = get_rssi;
	dev->set_stereo = set_stereo;
	dev->is_stereo = is_stereo;
	dev->set_mute = set_mute;
	dev->set_volume = set_volume;
	dev->get_rds = get_rds;
	dev->is_support_rds = is_support_rds;
	
	if((dev->fd = open(name, O_RDWR)) == -1) {
		ALOGW("radio Stub: failed to open %s -- %s.", name, strerror(errno));
		free(dev);
		return -EFAULT;
	}
	
	*device = &(dev->common);

	return 0;
}

static int radio_device_close(struct hw_device_t* device) {

	void*  dummy;
	struct radio_device_t* radio_device = (struct radio_device_t*)device;

	if(radio_device) {		
		
		close(radio_device->fd);
		free(radio_device);
	}
	
	return 0;
}

static int set_freq(struct radio_device_t *dev, int frequency, int band)
{
	 int args[2] = {frequency, band};

	 if (dev != NULL)
			return ioctl(dev->fd, IOCSETFREQ, args);

	 return RADIO_ERROR;
}
	
static int get_rssi(struct radio_device_t *dev, int frequency, int band)
{
	int args[2] = {frequency, band};

	if (dev != NULL){
		int nRet = ioctl(dev->fd, IOCGETRSSI, args);

		return args[0];
	}

	return RADIO_ERROR;
}

int get_rds(struct radio_device_t * dev, unsigned char* buffer, int* len)
{

	if (dev != NULL)
		return ioctl(dev->fd, IOCGETRDSINFO, buffer);

	return RADIO_ERROR;
}

int is_support_rds(struct radio_device_t* dev)
{
	int rds = 0;

	if (dev != NULL)
		ioctl(dev->fd, IOCISRDS, &rds);

	return rds;
}
	
static int set_stereo(struct radio_device_t *dev, int stereo)
{
	if (dev != NULL){
		int nRet = ioctl(dev->fd, IOCSETSTEREO, &stereo);
		return nRet;
	}
		
	return RADIO_ERROR;
}

static int is_stereo(struct radio_device_t *dev,int freq){

	if (dev != NULL){
		int nRet = ioctl(dev->fd, IOCGETSTEREO,&freq);
		return nRet;
	}

	return RADIO_ERROR;
}

static int set_mute(struct radio_device_t *dev, int mute){

	if (dev != NULL){
		int nRet = ioctl(dev->fd, IOCSETMUTE,&mute);
		return nRet;
	}

	return RADIO_ERROR;
}

static int set_volume(struct radio_device_t *dev, int volume){

	if (dev != NULL){
		int nRet = ioctl(dev->fd, IOCSETVOLUME,&volume);
		return nRet;
	}

	return RADIO_ERROR;
}
