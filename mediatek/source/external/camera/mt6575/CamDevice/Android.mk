################################################################################
#
################################################################################

LOCAL_PATH := $(call my-dir)

################################################################################
#
################################################################################
include $(CLEAR_VARS)

#-----------------------------------------------------------
LOCAL_SRC_FILES += CamDeviceManager.cpp
LOCAL_SRC_FILES += ICamDevice.cpp
LOCAL_SRC_FILES += CamDevice.cpp
LOCAL_SRC_FILES += CamBufProvider.cpp

#-----------------------------------------------------------
LOCAL_C_INCLUDES += $(LOCAL_PATH)/..
LOCAL_C_INCLUDES += $(TOP)/$(MTK_PATH_SOURCE)/external
LOCAL_C_INCLUDES += $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include
LOCAL_C_INCLUDES += $(TOP)/$(MTK_PATH_SOURCE)/external/cameraprofile

#-----------------------------------------------------------
LOCAL_MODULE := camera.cam.device

#-----------------------------------------------------------
include $(BUILD_STATIC_LIBRARY)

