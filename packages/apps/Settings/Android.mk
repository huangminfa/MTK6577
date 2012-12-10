LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle \
				mediatek-framework
LOCAL_STATIC_JAVA_LIBRARIES := guava \
				com.mediatek.CellConnUtil
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    src/com/mediatek/bindersync/BindWork.aidl \

ifneq ($(MTK_BT_PROFILE_MANAGER), yes)
LOCAL_SRC_FILES := $(filter-out src/com/android/settings/bluetoothangel%, $(LOCAL_SRC_FILES))
else 
LOCAL_SRC_FILES := $(filter-out src/com/android/settings/bluetoothZ%, $(LOCAL_SRC_FILES))
endif
LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
