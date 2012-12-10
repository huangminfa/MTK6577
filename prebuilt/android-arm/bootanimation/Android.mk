LOCAL_PATH := $(call my-dir)
BOOT_ANI_FILE := $(LOCAL_PATH)/bootanimation.zip
BOOT_ANI_FILE1 := $(LOCAL_PATH)/bootanimation1.zip
ifeq ($(BOOT_ANI_FILE), $(wildcard $(BOOT_ANI_FILE)))
include $(CLEAR_VARS)

LOCAL_MODULE := bootanimation.zip
LOCAL_MODULE_TAGS := user
LOCAL_MODULE_CLASS := media
LOCAL_MODULE_PATH := $(TARGET_OUT)/media
LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)
endif

ifeq ($(BOOT_ANI_FILE1), $(wildcard $(BOOT_ANI_FILE1)))
include $(CLEAR_VARS)

LOCAL_MODULE := bootanimation1.zip
LOCAL_MODULE_TAGS := user
LOCAL_MODULE_CLASS := media
LOCAL_MODULE_PATH := $(TARGET_OUT)/media
LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)
endif
