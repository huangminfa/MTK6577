LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=       \
        AAMRAssembler.cpp           \
        AAVCAssembler.cpp           \
        AH263Assembler.cpp          \
        AMPEG4AudioAssembler.cpp    \
        AMPEG4ElementaryAssembler.cpp \
        APacketSource.cpp           \
        ARawAudioAssembler.cpp      \
        ARTPAssembler.cpp           \
        ARTPConnection.cpp          \
        ARTPSource.cpp              \
        ARTPWriter.cpp              \
        ARTSPConnection.cpp         \
        ARTSPController.cpp         \
        ASessionDescription.cpp     \

LOCAL_C_INCLUDES:= \
	$(JNI_H_INCLUDE) \
	$(TOP)/frameworks/base/include/media/stagefright/openmax \
        $(TOP)/frameworks/base/media/libstagefright/include \
        $(TOP)/external/openssl/include \
		$(MTK_PATH_CUSTOM)/native

$(call make-private-dependency,\
  $(BOARD_CONFIG_DIR)/configs/avc_hardware.mk \
) 

ifeq ($(strip $(BOARD_USES_6573_MFV_HW)),true)
  LOCAL_CFLAGS += -DMT6573_MFV_HW
endif
# --->

ifeq ($(MTK_DRM_APP),yes)
  LOCAL_CFLAGS += -DMTK_DRM_APP
endif

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE
else
ifeq ($(strip $(MTK_RTP_OVER_RTSP_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_RTP_OVER_RTSP_SUPPORT
endif

ifeq ($(strip $(MTK_RTSP_BITRATE_ADAPTATION_SUPPORT)),yes)
  LOCAL_CFLAGS += -DMTK_RTSP_BITRATE_ADAPTATION_SUPPORT
endif

ifeq ($(strip $(MTK_BSP_PACKAGE)),yes)
	LOCAL_CFLAGS += -DMTK_BSP_PACKAGE
endif

LOCAL_C_INCLUDES += \
    $(TOP)/mediatek/source/external/mhal/src/core/drv/inc \
    $(TOP)/$(MTK_PATH_SOURCE)/kernel/include/linux 

endif

LOCAL_MODULE:= libstagefright_rtsp

ifeq ($(TARGET_ARCH),arm)
    LOCAL_CFLAGS += -Wno-psabi
endif

include $(BUILD_STATIC_LIBRARY)

################################################################################

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=         \
        rtp_test.cpp

LOCAL_SHARED_LIBRARIES := \
	libstagefright liblog libutils libbinder libstagefright_foundation libvcodecdrv   

LOCAL_STATIC_LIBRARIES := \
        libstagefright_rtsp

LOCAL_C_INCLUDES:= \
	$(JNI_H_INCLUDE) \
	frameworks/base/media/libstagefright \
	$(TOP)/frameworks/base/include/media/stagefright/openmax

LOCAL_CFLAGS += -Wno-multichar

ifeq ($(MTK_DRM_APP),yes)
  LOCAL_CFLAGS += -DMTK_DRM_APP
endif

LOCAL_MODULE_TAGS := debug

LOCAL_MODULE:= rtp_test

# include $(BUILD_EXECUTABLE)
