LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := main.c

LOCAL_STATIC_LIBRARIES := libc libcutils
LOCAL_SHARED_LIBRARIES := libcutils libc libstdc++ libdl liblog

LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_MODULE := task_service
LOCAL_MODULE_PATH := $(TARGET_OUT_BIN)
include $(BUILD_EXECUTABLE)
