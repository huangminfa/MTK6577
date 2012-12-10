LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := MobileLog
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MODULE_CLASS := APPS


LOCAL_JNI_SHARED_LIBRARIES := libmobilelog_jni
LOCAL_SHARED_LIBRARIES := \
        liblog \
		libcutils


LOCAL_CERTIFICATE := platform


include $(BUILD_PREBUILT)
#include $(LOCAL_PATH)/jni/Android.mk
# Use the folloing include to make our test apk.
 
