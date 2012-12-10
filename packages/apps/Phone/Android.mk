LOCAL_PATH:= $(call my-dir)
# This is used for building out an library for RCS-e share
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
	/src/com/mediatek/phone/extension/rcse/ICallScreenHost.java \
	/src/com/mediatek/phone/extension/rcse/ICallScreenPlugIn.java \
    /src/com/mediatek/phone/extension/rcse/Constants.java \

LOCAL_MODULE := com.mediatek.phone.extension.rcse
include $(BUILD_STATIC_JAVA_LIBRARY)

# Static library with some common classes for the phone apps.
# To use it add this line in your Android.mk
#  LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_SRC_FILES := \
	src/com/android/phone/CallLogAsync.java \
	src/com/android/phone/HapticFeedback.java

LOCAL_MODULE := com.android.phone.common
include $(BUILD_STATIC_JAVA_LIBRARY)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

ifeq ($(MTK_BT_SUPPORT), yes)
LOCAL_SRC_FILES := $(filter-out src/com/android/phone/BluetoothAtPhonebook.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out src/com/android/phone/BluetoothHandsfree.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out src/com/android/phone/BluetoothHeadsetService.java, $(LOCAL_SRC_FILES))
else
LOCAL_SRC_FILES := $(filter-out src/com/mediatek/blueangel/BluetoothAtPhonebook.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out src/com/mediatek/blueangel/BluetoothHandsfree.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out src/com/mediatek/blueangel/BluetoothHeadsetService.java, $(LOCAL_SRC_FILES))
endif

LOCAL_SRC_FILES += \
        src/com/android/phone/EventLogTags.logtags \
        src/com/android/phone/INetworkQueryService.aidl \
        src/com/android/phone/INetworkQueryServiceCallback.aidl \
        src/com/android/phone/IPhoneRecorder.aidl\
        src/com/android/phone/IPhoneRecordStateListener.aidl

LOCAL_PACKAGE_NAME := Phone
LOCAL_CERTIFICATE := platform
LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common \
                               com.mediatek.CellConnUtil \
                               com.mediatek.phone.extension.rcse

LOCAL_JAVA_LIBRARIES += mediatek-framework

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
