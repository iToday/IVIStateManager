LOCAL_PATH := $(call my-dir)

#############################################################
include $(CLEAR_VARS)

LOCAL_LDLIBS :=-llog

LOCAL_MODULE := radio.default
LOCAL_SRC_FILES := radio/radio.c

LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw

LOCAL_SHARED_LIBRARIES :=libutils libandroid_runtime

#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../include 

include $(BUILD_SHARED_LIBRARY)

##############################################################
include $(CLEAR_VARS)

LOCAL_LDLIBS :=-llog

LOCAL_MODULE    := libtuner
LOCAL_SRC_FILES := Tuner.cpp TunerProxy.cpp RdsParser.cpp ./radio/band.cpp

LOCAL_SHARED_LIBRARIES :=libutils libandroid_runtime libhardware

#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../include 

include $(BUILD_SHARED_LIBRARY)


