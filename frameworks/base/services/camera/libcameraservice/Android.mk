LOCAL_PATH:= $(call my-dir)

#
# libcameraservice
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=               \
    CameraService.cpp

#//!++
ifeq "yes" "$(strip $(MTK_CAMERA_BSP_SUPPORT))"
ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)
#ifeq ($(HAVE_MATV_FEATURE),yes)
    LOCAL_CFLAGS += -DATVCHIP_MTK_ENABLE
#endif
endif
  LOCAL_C_INCLUDES:= \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/services/camera/libcameraservice

endif

ifeq "yes" "$(strip $(MTK_CAMERA_BSP_SUPPORT))"
    LOCAL_WHOLE_STATIC_LIBRARIES := libcameraservice_mtk
endif

#//!--

LOCAL_SHARED_LIBRARIES:= \
    libui \
    libutils \
    libbinder \
    libcutils \
    libmedia \
    libcamera_client \
    libgui \
    libhardware

#//!++
ifeq "yes" "$(strip $(MTK_CAMERA_BSP_SUPPORT))"
LOCAL_SHARED_LIBRARIES += libcameraprofile
LOCAL_C_INCLUDES += $(TOP)/$(MTK_PATH_SOURCE)/external/cameraprofile	
endif
#//!--

LOCAL_MODULE:= libcameraservice

include $(BUILD_SHARED_LIBRARY)
