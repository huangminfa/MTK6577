LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := arm/unwind.c arm/pr-support.c
LOCAL_CFLAGS := -Wall
LOCAL_MODULE := libaee_unwind_support

LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/..

include $(BUILD_STATIC_LIBRARY)
