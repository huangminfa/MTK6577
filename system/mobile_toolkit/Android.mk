ifeq ($(strip $(MTK_BICR_SUPPORT)),yes)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := iAmCdRom.iso
LOCAL_MODULE_TAGS := user
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(TARGET_OUT)/mobile_toolkit
include $(BUILD_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))

endif

#
# Copy eSurfing CDROM image to /system/etc/
#

ifeq ($(strip $(EVDO_DT_SUPPORT)),yes)
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := eSurfing.iso
LOCAL_MODULE_TAGS := user
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(TARGET_OUT)/mobile_toolkit
include $(BUILD_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))

endif
