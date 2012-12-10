LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=                 \
        AnotherPacketSource.cpp   \
        ATSParser.cpp             \
        ESQueue.cpp               \
        MPEG2PSExtractor.cpp      \
        MPEG2TSExtractor.cpp      \

LOCAL_C_INCLUDES:= \
	$(JNI_H_INCLUDE) \
	$(TOP)/frameworks/base/include/media/stagefright/openmax \
        $(TOP)/frameworks/base/media/libstagefright

LOCAL_MODULE:= libstagefright_mpeg2ts

ifeq ($(TARGET_ARCH),arm)
    LOCAL_CFLAGS += -Wno-psabi
endif

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE

else

#LOCAL_CFLAGS += -DMTK_DEMUXER_BLOCK_CAPABILITY
ifeq ($(strip $(MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT)), yes)
LOCAL_CFLAGS += -DMTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
LOCAL_C_INCLUDES += \
    $(TOP)/mediatek/source/external/mhal/src/core/drv/inc \
    $(TOP)/$(MTK_PATH_SOURCE)/kernel/include/linux
endif
endif

include $(BUILD_STATIC_LIBRARY)
