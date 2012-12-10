#
# msdkCCAP Test 
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
        ./AcdkCLITest.cpp
    

LOCAL_C_INCLUDES += \
    $(MTK_PATH_CUSTOM)/kernel/imgsensor/inc \
    $(MTK_PATH_SOURCE)/external/mhal/src/core/scenario/6575/cameradebug/inc \
    $(MTK_PATH_SOURCE)/external/mhal/src/custom/inc \
    $(MTK_PATH_SOURCE)/external/mhal/inc \
  

LOCAL_SHARED_LIBRARIES:= liblog libcutils libacdk    
    
LOCAL_MODULE:= acdktest

ifneq (yes,$(strip $(MTK_EMULATOR_SUPPORT)))
  include $(BUILD_EXECUTABLE)
endif
