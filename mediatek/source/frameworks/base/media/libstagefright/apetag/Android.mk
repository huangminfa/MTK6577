LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(MTK_AUDIO_APE_SUPPORT),yes)
LOCAL_SRC_FILES := \
	apetag.cpp
	
ifeq ($(MTK_DRM_APP),yes)
  LOCAL_CFLAGS += -DMTK_DRM_APP
endif

LOCAL_MODULE := libstagefright_apetag

include $(BUILD_STATIC_LIBRARY)

endif
