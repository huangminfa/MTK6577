#  only if use yusu audio will build this.
ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := \
    $(MTK_PATH_PLATFORM)/hardware/audio/aud_drv
    
LOCAL_SRC_FILES+= \
    AudioAmpReg.cpp 
  
LOCAL_SHARED_LIBRARIES += \
    libcutils \
    libutils \
    libaudio.primary.default 

LOCAL_MODULE:= AudioAmpReg

LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

endif

