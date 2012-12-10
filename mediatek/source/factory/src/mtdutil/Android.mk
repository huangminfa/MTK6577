LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := mtdutils.c mounts.c

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../inc

LOCAL_MODULE := libmtdutil
# Add to fix Android 2.3 build error, and need to add libmtdutil to user_tags.mk for user build
LOCAL_MODULE_TAGS := user

include $(BUILD_STATIC_LIBRARY)
