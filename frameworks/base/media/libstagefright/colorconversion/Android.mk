LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE
endif

ifeq "$(strip $(MTK_75DISPLAY_ENHANCEMENT_SUPPORT))" "yes"
  LOCAL_CFLAGS += -DMTK_75DISPLAY_ENHANCEMENT_SUPPORT
endif

LOCAL_SRC_FILES:=                     \
        ColorConverter.cpp            \
        SoftwareRenderer.cpp

LOCAL_C_INCLUDES := \
        $(TOP)/frameworks/base/include/media/stagefright/openmax \
        $(TOP)/$(MTK_PATH_SOURCE)/external/mhal/inc
        

LOCAL_MODULE:= libstagefright_color_conversion

include $(BUILD_STATIC_LIBRARY)
