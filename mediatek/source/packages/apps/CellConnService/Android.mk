# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)

# Static library with some common classes for the phone apps.
# To use it add this line in your Android.mk
#  LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.CellConnUtil

include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := user
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
    src/com/mediatek/CellConnService/IPhoneStatesCallback.aidl \
    src/com/mediatek/CellConnService/IPhoneStatesMgrService.aidl \
    src/com/mediatek/CellConnService/CellConnMgr.java \
	
LOCAL_MODULE := com.mediatek.CellConnUtil
include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.CellConnUtil

LOCAL_SRC_FILES := \
    src/com/mediatek/CellConnService/service/PhoneStatesMgrService.java \
    src/com/mediatek/CellConnService/service/ConfirmDlgActivity.java \
    src/com/mediatek/CellConnService/IPhoneStatesCallback.aidl \
    src/com/mediatek/CellConnService/IPhoneStatesMgrService.aidl \

LOCAL_PACKAGE_NAME := CellConnService
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
