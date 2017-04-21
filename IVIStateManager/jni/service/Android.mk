LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng
LOCAL_SRC_FILES := \
		 serial/Serial.cpp \
		 serial/SelectSerial.cpp \
		 serial/McuSerial.cpp  \
		 serial/McuUpgrade.cpp  \
		 netlink/Netlink.cpp \
		 server/IMcuServer.cpp \
		 server/McuService.cpp \
		 listener/IListener.cpp
									 
LOCAL_SHARED_LIBRARIES :=liblog libc libcutils  libutils  libbinder
								
LOCAL_PRELINK_MODULE:=false
LOCAL_MODULE    := libMcuService
include $(BUILD_SHARED_LIBRARY)


##################################################
#LOCAL_PATH:=$(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=server/main_mcuservice.cpp

LOCAL_SHARED_LIBRARIES:=libMcuService libbinder libcutils libutils liblog  libc

LOCAL_MODULE_TAGS:=optional

LOCAL_MODULE:=mcuservice
include $(BUILD_EXECUTABLE)
