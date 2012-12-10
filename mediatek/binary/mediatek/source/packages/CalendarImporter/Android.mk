LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := CalendarImporter
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MODULE_CLASS := APPS

LOCAL_STATIC_JAVA_LIBRARIES := android-common \
        calendar-common


#PROGUARD start#
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#PROGUARD end#

ifeq ($(strip $(MTK_SIGNATURE_CUSTOMIZATION)),yes)
  LOCAL_CERTIFICATE := releasekey
else
  LOCAL_CERTIFICATE := testkey
endif
include $(BUILD_PREBUILT)

# Use the following include to make our test apk.
