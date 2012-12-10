LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    AudioParameter.cpp
LOCAL_MODULE:= libmedia_helper
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE
else

ifeq ($(strip $(MTK_S3D_SUPPORT)),yes)
LOCAL_CFLAGS += -DMTK_S3D_SUPPORT
endif

endif

LOCAL_MTK_PATH:=../../../../mediatek/source/frameworks/base/media/libmedia

ifeq ($(strip $(BOARD_USES_GENERIC_AUDIO)),true)
  LOCAL_CFLAGS += -DGENERIC_AUDIO
else
  LOCAL_CFLAGS += -DMTK_AUDIO  
endif

LOCAL_CFLAGS += -DVOLUME_NEWMAP

LOCAL_SRC_FILES:= \
    AudioTrack.cpp \
    IAudioFlinger.cpp \
    IAudioFlingerClient.cpp \
    IAudioTrack.cpp \
    IAudioRecord.cpp \
    AudioRecord.cpp \
    AudioSystem.cpp \
    mediaplayer.cpp \
    IMediaPlayerService.cpp \
    IMediaPlayerClient.cpp \
    IMediaRecorderClient.cpp \
    IMediaPlayer.cpp \
    IMediaRecorder.cpp \
    IStreamSource.cpp \
    Metadata.cpp \
    mediarecorder.cpp \
    IMediaMetadataRetriever.cpp \
    mediametadataretriever.cpp \
    ToneGenerator.cpp \
    JetPlayer.cpp \
    IOMX.cpp \
    IAudioPolicyService.cpp \
    MediaScanner.cpp \
    MediaScannerClient.cpp \
    autodetect.cpp \
    IMediaDeathNotifier.cpp \
    MediaProfiles.cpp \
    IEffect.cpp \
    IEffectClient.cpp \
    AudioEffect.cpp \
    Visualizer.cpp \
    MemoryLeakTrackUtil.cpp \
    fixedfft.cpp.arm \
  
LOCAL_SRC_FILES+= \
    $(LOCAL_MTK_PATH)/AudioPCMxWay.cpp \
    $(LOCAL_MTK_PATH)/ATVCtrl.cpp \
    $(LOCAL_MTK_PATH)/IATVCtrlClient.cpp \
    $(LOCAL_MTK_PATH)/IATVCtrlService.cpp \

LOCAL_SHARED_LIBRARIES := \
	libui libcutils libutils libbinder libsonivox libicuuc libexpat \
        libcamera_client libstagefright_foundation \
        libgui libdl 

ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_SHARED_LIBRARIES += \
        libvcodecdrv     
endif
        
LOCAL_STATIC_LIBRARIES += \
        libmedia_helper

LOCAL_WHOLE_STATIC_LIBRARY := libmedia_helper
LOCAL_MODULE:= libmedia

LOCAL_C_INCLUDES := \
    $(JNI_H_INCLUDE) \
    $(call include-path-for, graphics corecg) \
    $(TOP)/frameworks/base/include/media/stagefright/openmax \
    $(TOP)/mediatek/source/external/mhal/src/core/drv/inc \
    $(TOP)/$(MTK_PATH_SOURCE)/kernel/include/linux \
    external/icu4c/common \
    external/expat/lib \
    system/media/audio_effects/include \
    $(MTK_PATH_SOURCE)/frameworks/base/include 
    
ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
  LOCAL_CFLAGS += -DMTK_AUDIO
  LOCAL_C_INCLUDES+= \
   $(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio \
   $(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio/aud_drv
endif    

ifeq ($(strip $(HAVE_AACENCODE_FEATURE)),yes)
    LOCAL_CFLAGS += -DHAVE_AACENCODE
endif

ifeq ($(strip $(MTK_AUDIO_HD_REC_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_AUDIO_HD_REC_SUPPORT
endif


include $(BUILD_SHARED_LIBRARY)
