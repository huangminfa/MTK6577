LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	Camera.cpp \
	CameraParameters.cpp \
	ICamera.cpp \
	ICameraClient.cpp \
	ICameraService.cpp \
	ICameraRecordingProxy.cpp \
	ICameraRecordingProxyListener.cpp

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libbinder \
	libhardware \
	libui \
	libgui

LOCAL_MODULE:= libcamera_client

ifeq "yes" "$(strip $(MTK_CAMERA_BSP_SUPPORT))"
    LOCAL_WHOLE_STATIC_LIBRARIES := libcamera_client_mtk
endif

include $(BUILD_SHARED_LIBRARY)

ifeq "yes" "$(strip $(MTK_CAMERA_BSP_SUPPORT))"
    include $(call all-makefiles-under,$(LOCAL_PATH))
endif