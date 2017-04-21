#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_DEX_PREOPT := true

LOCAL_PRIVILEGED_MODULE := true

#LOCAL_STATIC_JAVA_LIBRARIES := android-common android-support-v13

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/com/itoday/ivi/IKeyListener.aidl \
	src/com/itoday/ivi/ITuner.aidl \
	src/com/itoday/ivi/ITunerListener.aidl \
	src/com/itoday/ivi/IUpgradeListener.aidl \
	src/com/itoday/ivi/IVehicle.aidl



LOCAL_PACKAGE_NAME := IVIMainService

LOCAL_CERTIFICATE := platform

	
LOCAL_JNI_SHARED_LIBRARIES := libmcu libtuner libMcuService

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_PROGUARD_ENABLED:= disabled  

include $(BUILD_PACKAGE)

#
# the IVIMainService.jar
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES :=  src/com/itoday/ivi/data/IntObserver.java \
		src/com/itoday/ivi/data/IVIDataManager.java \
		src/com/itoday/ivi/sdk/IVITunerManager.java \
		src/com/itoday/ivi/sdk/IVIManager.java	\
		src/com/itoday/ivi/config/ConfigManager.java \
		src/com/itoday/ivi/config/Module.java	\
		src/com/itoday/ivi/platform/IVIDevice.java \
		src/com/itoday/ivi/platform/IVIKeyEvent.java \
		src/com/itoday/ivi/platform/IVIPhone.java \
		src/com/itoday/ivi/platform/IVIAudio.java \
		src/com/itoday/ivi/platform/IVIChannel.java \
		src/com/itoday/ivi/platform/IVINavi.java \
		src/com/itoday/ivi/platform/IVIChannel.java \
		src/com/itoday/ivi/platform/IVIPlatform.java \
		src/com/itoday/ivi/platform/IVITuner.java \
		src/com/itoday/ivi/platform/IVIApp.java \
		src/com/itoday/ivi/IKeyListener.aidl \
		src/com/itoday/ivi/ITuner.aidl \
		src/com/itoday/ivi/ITunerListener.aidl \
		src/com/itoday/ivi/IUpgradeListener.aidl \
		src/com/itoday/ivi/IVehicle.aidl

LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := framework
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE:= IVIMainSDK

LOCAL_DEX_PREOPT := false

LOCAL_OVERRIDES_PACKAGES := Launcher3 \
    Calendar \
    Calculator \
    LauncherCar \
    Launcher2 \
    Camera2 \
    DeskClock \
    Email \
    Exchange \
    FMRadio \
    Gallery2 \
    InCallUI \
    Mms \
    Music \
    QuickSearchBox \
    SoundRecorder \
    RkApkinstaller \
    DownloadProviderUi \
    SchedulePowerOnOff \
    Rk3grVideoPlayer \
    FmTransmitter \
    Development \
    SoundRecorder
    
# List of classes and interfaces which should be loaded by the Zygote.
include $(BUILD_JAVA_LIBRARY)

##################################################################

include $(call all-makefiles-under,$(LOCAL_PATH))



