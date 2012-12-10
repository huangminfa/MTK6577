#
# libcameraservice_mtk
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES += MtkCameraService.cpp

LOCAL_C_INCLUDES := \
    $(TOP)/frameworks/base/services/camera/libcameraservice \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include

LOCAL_STATIC_LIBRARIES := \

LOCAL_WHOLE_STATIC_LIBRARIES := \

LOCAL_SHARED_LIBRARIES := \

LOCAL_MODULE := libcameraservice_mtk

include $(BUILD_STATIC_LIBRARY)

