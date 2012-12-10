LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(MTK_EMULATOR_SUPPORT),yes)
LOCAL_CFLAGS += -DEMULATOR_PROJECT
endif

LOCAL_SRC_FILES:= \
	screencap.cpp

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libbinder \
	libskia \
    libui \
    libgui

LOCAL_MODULE:= screencap

LOCAL_MODULE_TAGS := optional

LOCAL_C_INCLUDES += \
	external/skia/include/core \
	external/skia/include/effects \
	external/skia/include/images \
	external/skia/src/ports \
	external/skia/include/utils \
	mediatek/source/kernel/drivers/video 

include $(BUILD_EXECUTABLE)
