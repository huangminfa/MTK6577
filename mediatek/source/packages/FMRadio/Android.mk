ifeq ($(MTK_FM_SUPPORT),yes)
ifeq ($(MTK_FM_RX_SUPPORT),yes)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

#LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += src/com/mediatek/FMRadio/IFMRadioService.aidl

LOCAL_PACKAGE_NAME := FMRadio

LOCAL_JNI_SHARED_LIBRARIES := libfmjni

include $(BUILD_PACKAGE)


# ============================================================

include $(call all-makefiles-under,$(LOCAL_PATH))

endif
endif
