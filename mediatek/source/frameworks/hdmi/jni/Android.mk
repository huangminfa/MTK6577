LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := user
LOCAL_SHARED_LIBRARIES += liblog libnativehelper
LOCAL_PRELINK_MODULE := false
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) \
					$(LOCAL_PATH) \
					$(MTK_PATH_SOURCE)/kernel/drivers/hdmitx

LOCAL_SRC_FILES := libmtkhdmi_jni.cpp
LOCAL_MODULE := libmtkhdmi_jni
include $(BUILD_SHARED_LIBRARY)



