ifeq ($(MTK_ENGINEERMODE_APP), yes)
LOCAL_MODULE_TAGS := user

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := usb_jni.c
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
#LOCAL_SHARED_LIBRARIES += libft libutils
LOCAL_SHARED_LIBRARIES +=  libutils
LOCAL_MODULE := libem_jni
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)


endif
