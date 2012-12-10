LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES += liblog libnativehelper libz libtvoutpattern
LOCAL_PRELINK_MODULE := false
LOCAL_C_INCLUDES += \
		$(JNI_H_INCLUDE) \
		$(LOCAL_PATH) \
		$(MTK_PATH_SOURCE)/kernel/drivers/video \
		$(TOP)/external/zlib \
		$(MTK_PATH_SOURCE)/frameworks/tvout/pattern

LOCAL_SRC_FILES := \
		libtvout_jni.cpp

LOCAL_MODULE := libtvoutjni
include $(BUILD_SHARED_LIBRARY)

