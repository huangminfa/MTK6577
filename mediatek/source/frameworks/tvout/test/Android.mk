LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES += liblog libnativehelper libtvoutpattern
LOCAL_PRELINK_MODULE := false
LOCAL_C_INCLUDES += \
		$(JNI_H_INCLUDE) \
		$(LOCAL_PATH) \
		$(MTK_PATH_SOURCE)/kernel/drivers/video \
		$(TOP)/external/zlib \
		$(MTK_PATH_SOURCE)/frameworks/media/tvout/pattern

LOCAL_SRC_FILES := \
        testHQA.cpp \

LOCAL_MODULE := testHQA
LOCAL_MODULE_TAGS := tests

#include $(BUILD_EXECUTABLE)


