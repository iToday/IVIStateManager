LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS :=-llog

LOCAL_MODULE    := libSerialPort
LOCAL_SRC_FILES := SerialPort.cpp Serial.cpp SelectSerial.cpp

LOCAL_SHARED_LIBRARIES :=libutils libandroid_runtime libhardware

#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../include 

include $(BUILD_SHARED_LIBRARY)


