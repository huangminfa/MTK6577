#
# libcamera_client_mtk
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    MtkCameraParameters.cpp \
    MtkCamera.cpp \

LOCAL_C_INCLUDES:= \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include

LOCAL_STATIC_LIBRARIES := \

LOCAL_WHOLE_STATIC_LIBRARIES := \

LOCAL_MODULE := libcamera_client_mtk

include $(BUILD_STATIC_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

