LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user eng

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := MTKAndroidSuiteDaemon
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

