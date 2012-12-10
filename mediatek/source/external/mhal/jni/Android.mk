ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)

ifeq ($(MTK_PLATFORM), $(filter $(MTK_PLATFORM) MT6575,MT6577))

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	6575/mhal_jni.cpp
	
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
	
LOCAL_C_INCLUDES += \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/src/core/drv/6575/isp \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/src/core/drv/6575/inc \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/src/core/drv/common/inc \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/src/core/pipe/common/inc \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/src/core/pipe/6575/inc \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/src/core/pipe/6575/display_isp \
  $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/inc \
  $(TOP)/$(MTK_PATH_CUSTOM)/hal/inc/display_isp_tuning \

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libmhalmdp \
#  libandroid_runtime \

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := libPQjni

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

endif

endif
