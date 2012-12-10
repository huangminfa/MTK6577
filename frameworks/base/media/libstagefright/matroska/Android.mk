LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=                 \
        MatroskaExtractor.cpp

LOCAL_C_INCLUDES:= \
        $(JNI_H_INCLUDE) \
        $(TOP)/external/libvpx/mkvparser \
        $(TOP)/frameworks/base/include/media/stagefright/openmax \
        $(TOP)/frameworks/base/include \

LOCAL_CFLAGS += -Wno-multichar

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE

else
LOCAL_C_INCLUDES += \
        $(TOP)/mediatek/source/external/mhal/src/core/drv/inc \
        $(TOP)/$(MTK_PATH_SOURCE)/kernel/include/linux \

endif

LOCAL_MODULE:= libstagefright_matroska

include $(BUILD_STATIC_LIBRARY)
