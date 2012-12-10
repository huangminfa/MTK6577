LOCAL_PATH:= $(call my-dir)

# Effect factory library
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	EffectsFactory.c

LOCAL_SHARED_LIBRARIES := \
	libcutils

LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
LOCAL_MODULE:= libeffects

LOCAL_SHARED_LIBRARIES += libdl

LOCAL_C_INCLUDES := \
    system/media/audio_effects/include

PRODUCT_COPY_FILES += $(TOP)/frameworks/base/media/libeffects/data/audio_effects.conf:system/etc/audio_effects.conf

include $(BUILD_SHARED_LIBRARY)
