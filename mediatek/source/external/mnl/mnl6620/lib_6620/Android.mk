LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libmnl.a libsupl.a libhotstill.a libagent.a

include $(BUILD_MULTI_PREBUILT)

