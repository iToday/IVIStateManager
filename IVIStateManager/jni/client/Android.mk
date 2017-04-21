LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS :=-llog

LOCAL_MODULE    := libmcu
LOCAL_SRC_FILES := Mcu.cpp \
	McuProxy.cpp

LOCAL_SHARED_LIBRARIES :=libutils libbinder libandroid_runtime libMcuService

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../include 

include $(BUILD_SHARED_LIBRARY)
