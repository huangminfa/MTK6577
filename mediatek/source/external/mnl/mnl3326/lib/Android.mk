LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libmnl.a libhotstill.a libagent.a libsupl.a
include $(BUILD_MULTI_PREBUILT)

