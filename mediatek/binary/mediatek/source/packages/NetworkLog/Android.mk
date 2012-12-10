LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# Only compile source java files in this apk. 
LOCAL_MODULE := ActivityNetwork
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MODULE_CLASS := APPS


#LOCAL_SDK_VERSION := current

LOCAL_CERTIFICATE := platform

LOCAL_SHARED_LIBRARIES := \
        liblog \
		libcutils

include $(BUILD_PREBUILT)

# Use the following include to make our test apk.
