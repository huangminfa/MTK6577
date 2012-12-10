LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_ARM_MODE := arm
LOCAL_PRELINK_MODULE := false

LOCAL_SRC_FILES:= Meta_CCAP_Para.cpp

LOCAL_C_INCLUDES += \
    $(MTK_PATH_SOURCE)/external/meta/common/inc \
    $(MTK_PATH_SOURCE)/external/mhal/src/core/scenario/6575/cameradebug/inc \
    $(MTK_PATH_SOURCE)/external/mhal/src/custom/inc \
    $(MTK_PATH_SOURCE)/external/mhal/inc \
    $(TOP)/$(MTK_PATH_CUSTOM)/kernel/imgsensor/inc \
    $(MTK_PATH_CUSTOM)/hal/inc



LOCAL_SHARED_LIBRARIES:= libc libacdk libft

LOCAL_MODULE := libccap

include $(BUILD_STATIC_LIBRARY)
