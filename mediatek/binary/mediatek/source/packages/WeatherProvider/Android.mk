LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := common-codec \
			       signpost-core \
			       signpost-commonshttp4

LOCAL_MODULE := MtkWeatherProvider
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MODULE_CLASS := APPS


LOCAL_CERTIFICATE := platform


#PROGUARD start#
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#PROGUARD end#


include $(BUILD_PREBUILT)
##################################################
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := common-codec:libs/commons-codec-1.5.jar \
			                signpost-core:libs/signpost-core-1.2.1.1.jar \
			                signpost-commonshttp4:libs/signpost-commonshttp4-1.2.1.1.jar

