ifeq ($(MTK_ENGINEERMODE_APP), yes)
LOCAL_MODULE_TAGS := user

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES += liblog libnativehelper
LOCAL_PRELINK_MODULE := false
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) $(LOCAL_PATH) 
LOCAL_SRC_FILES := memorydump_jni.c sleepmode_jni.c
LOCAL_MODULE := libem_modem_jni
include $(BUILD_SHARED_LIBRARY)

endif