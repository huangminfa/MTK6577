LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	dcfdecoder_jni.cpp 

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
	
LOCAL_C_INCLUDES += \
 	$(MTK_PATH_SOURCE)/external/mhal/inc \
	external/skia/include/core \
	external/skia/include/effects \
	external/skia/include/images \
	external/skia/src/ports \
	external/skia/include/utils \
	frameworks/base/core/jni/android/graphics \
	frameworks/base/include/drm \
	$(MTK_PATH_SOURCE)/frameworks/base/include/drm

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libpixelflinger \
	libhardware \
	libutils \
	libskia \
	libandroid_runtime \
	libdrmframework \
	libdrmmtkutil

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := libdcfdecoderjni

LOCAL_MODULE_TAGS := user

include $(BUILD_SHARED_LIBRARY)

