LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES += liblog libnativehelper libz
LOCAL_PRELINK_MODULE := false
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) $(LOCAL_PATH) $(MTK_PATH_SOURCE)/kernel/drivers/video $(TOP)/external/zlib

LOCAL_SRC_FILES := \
        tvout_patterns.cpp \

LOCAL_MODULE := libtvoutpattern
include $(BUILD_SHARED_LIBRARY)

