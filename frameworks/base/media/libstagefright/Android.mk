LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

$(call make-private-dependency,\
  $(BOARD_CONFIG_DIR)/configs/StageFright.mk \
)

include frameworks/base/media/libstagefright/codecs/common/Config.mk

LOCAL_MTK_PATH:=../../../../mediatek/source/frameworks/base/media/libstagefright

LOCAL_SRC_FILES:=                         \
        ACodec.cpp                        \
        AACExtractor.cpp                  \
        AACWriter.cpp                     \
        AMRExtractor.cpp                  \
        AMRWriter.cpp                     \
        AudioPlayer.cpp                   \
        AudioSource.cpp                   \
        AwesomePlayer.cpp                 \
        CameraSource.cpp                  \
        CameraSourceTimeLapse.cpp         \
        VideoSourceDownSampler.cpp        \
        DataSource.cpp                    \
        DRMExtractor.cpp                  \
        ESDS.cpp                          \
        FileSource.cpp                    \
        HTTPBase.cpp                      \
        JPEGSource.cpp                    \
        MP3Extractor.cpp                  \
        MPEG2TSWriter.cpp                 \
        MPEG4Extractor.cpp                \
        MPEG4Writer.cpp                   \
        MediaBuffer.cpp                   \
        MediaBufferGroup.cpp              \
        MediaDefs.cpp                     \
        MediaExtractor.cpp                \
        MediaSource.cpp                   \
        MediaSourceSplitter.cpp           \
        MetaData.cpp                      \
        NuCachedSource2.cpp               \
        OMXClient.cpp                     \
        OMXCodec.cpp                      \
        OggExtractor.cpp                  \
        SampleIterator.cpp                \
        SampleTable.cpp                   \
        StagefrightMediaScanner.cpp       \
        StagefrightMetadataRetriever.cpp  \
        SurfaceMediaSource.cpp            \
        ThrottledSource.cpp               \
        TimeSource.cpp                    \
        TimedEventQueue.cpp               \
        Utils.cpp                         \
        VBRISeeker.cpp                    \
        WAVExtractor.cpp                  \
        WVMExtractor.cpp                  \
        XINGSeeker.cpp                    \
        avc_utils.cpp                     \
	$(LOCAL_MTK_PATH)/OggWriter.cpp   \
	$(LOCAL_MTK_PATH)/PCMWriter.cpp   \

ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)

ifeq ($(MULTI_CH_PLAYBACK_SUPPORT),yes)
LOCAL_CFLAGS += -DFLAC_MULTI_CH_SUPPORT
endif

LOCAL_SRC_FILES += \
        $(LOCAL_MTK_PATH)/FLACExtractor.cpp

ifeq ($(MTK_AUDIO_APE_SUPPORT),yes)
LOCAL_CFLAGS += -DMTK_AUDIO_APE_SUPPORT

LOCAL_SRC_FILES += \
        $(LOCAL_MTK_PATH)/APEExtractor.cpp

endif

ifeq ($(strip $(MTK_S3D_SUPPORT)),yes)
LOCAL_CFLAGS += -DMTK_S3D_SUPPORT
endif

else
LOCAL_SRC_FILES += \
        FLACExtractor.cpp
endif

ifeq ($(HAVE_CMMB_FEATURE), yes)
   LOCAL_SRC_FILES += \
         $(LOCAL_MTK_PATH)/cmmb/CMMBDataSource.cpp   \
         $(LOCAL_MTK_PATH)/cmmb/CMMBExtractor.cpp
endif

ifeq ($(strip $(MTK_DRM_APP)),yes)
LOCAL_CFLAGS += -DMTK_DRM_APP
endif

ifeq ($(MTK_SWIP_VORBIS),yes)
  LOCAL_CFLAGS += -DUSE_MTK_DECODER      
ifeq ($(MULTI_CH_PLAYBACK_SUPPORT),yes)
  LOCAL_CFLAGS += -DVORBIS_MULTI_CH_SUPPORT
endif
endif 

LOCAL_C_INCLUDES:= \
	$(JNI_H_INCLUDE) \
        $(TOP)/external/flac/include \
        $(TOP)/external/tremolo \
        $(TOP)/frameworks/base/media/libstagefright/rtsp \
        $(TOP)/external/openssl/include \
        $(MTK_PATH_SOURCE)/frameworks/base/media/libstagefright/pmem_util \
        $(MTK_PATH_SOURCE)/frameworks/base/include \
        $(MTK_PATH_SOURCE)/frameworks/base/media/libstagefright/include \
	    $(TOP)/mediatek/source/frameworks/base/media/libstagefright \
	    $(TOP)/mediatek/source/external/amr \
        $(TOP)/mediatek/source/external/awb \
        $(TOP)/external/skia/include/images \
        $(TOP)/external/skia/include/core \
	$(TOP)/mediatek/source/external/vorbisenc \

ifeq ($(HAVE_CMMB_FEATURE), yes)
  LOCAL_C_INCLUDES += \
        $(TOP)/mediatek/source/frameworks/base/media/libstagefright/cmmb \
        $(TOP)/mediatek/source/frameworks/cmmb/include 
endif

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
  LOCAL_C_INCLUDES += $(TOP)/frameworks/base/include/media/stagefright/openmax
else
  LOCAL_C_INCLUDES += $(TOP)/mediatek/source/hardware/omx/core/inc \
                      $(TOP)/$(MTK_PATH_SOURCE)/kernel/include/linux
endif


ifeq ($(HAVE_CMMB_FEATURE), yes)
LOCAL_CFLAGS += -DMTK_CMMB_SUPPORT
endif

LOCAL_SHARED_LIBRARIES := \
        libbinder         \
        libmedia          \
        libutils          \
        libcutils         \
        libui             \
        libsonivox        \
        libvorbisidec     \
        libstagefright_yuv \
        libcamera_client \
        libdrmframework  \
        libcrypto        \
        libssl           \
        libgui           \

ifeq ($(strip $(MTK_BSP_PACKAGE)),no)
LOCAL_SHARED_LIBRARIES += \
        libdrmmtkutil
endif

ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)
LOCAL_SHARED_LIBRARIES += libstagefright_pmemutil
endif

LOCAL_STATIC_LIBRARIES := \
        libstagefright_color_conversion \
        libstagefright_aacenc \
        libstagefright_amrnbenc \
        libstagefright_amrwbenc \
        libstagefright_avcenc \
        libstagefright_m4vh263enc \
        libstagefright_matroska \
        libstagefright_timedtext \
        libvpx \
        libstagefright_mpeg2ts \
        libstagefright_httplive \
        libstagefright_rtsp \
        libstagefright_id3 \
        libFLAC

ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
ifeq ($(MTK_AUDIO_APE_SUPPORT),yes)
LOCAL_C_INCLUDES += \
        $(TOP)/mediatek/source/external/apedec

LOCAL_STATIC_LIBRARIES +=\
        libstagefright_apetag

LOCAL_SHARED_LIBRARIES += \
        libapedec_mtk

endif
endif

ifeq ($(MULTI_CH_PLAYBACK_SUPPORT),yes)
	ifeq ($(MTK_SWIP_AAC),yes)
		LOCAL_CFLAGS += -DUSE_MTK_AAC
LOCAL_C_INCLUDES += \
	$(TOP)/mediatek/source/external/aacdec/new

LOCAL_STATIC_LIBRARIES += \
	libheaacdec_mtk \
	libdrvb
	endif
else
LOCAL_STATIC_LIBRARIES += libstagefright_aacdec
endif

LOCAL_SHARED_LIBRARIES += libskia

#ifeq ($(HAVE_CMMB_FEATURE), yes)
#      LOCAL_SHARED_LIBRARIES += \
#                  libcmmbsource     
#endif

#ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
#	LOCAL_STATIC_LIBRARIES += libstagefright_vorbisenc
#endif

#ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
#	LOCAL_SHARED_LIBRARIES += \
#        libamrenc \
#        libawb \
#        libvorbisenc_mtk
#endif

ifeq ($(strip $(HAVE_AWBENCODE_FEATURE)),yes)
    LOCAL_CFLAGS += -DHAVE_AWBENCODE
endif

ifeq ($(strip $(HAVE_AACENCODE_FEATURE)),yes)
    LOCAL_CFLAGS += -DHAVE_AACENCODE
endif

################################################################################

# The following was shamelessly copied from external/webkit/Android.mk and
# currently must follow the same logic to determine how webkit was built and
# if it's safe to link against libchromium.net

# V8 also requires an ARMv7 CPU, and since we must use jsc, we cannot
# use the Chrome http stack either.
ifneq ($(strip $(ARCH_ARM_HAVE_ARMV7A)),true)
  USE_ALT_HTTP := true
endif

# See if the user has specified a stack they want to use
HTTP_STACK = $(HTTP)
# We default to the Chrome HTTP stack.
DEFAULT_HTTP = chrome
ALT_HTTP = android

ifneq ($(HTTP_STACK),chrome)
  ifneq ($(HTTP_STACK),android)
    # No HTTP stack is specified, pickup the one we want as default.
    ifeq ($(USE_ALT_HTTP),true)
      HTTP_STACK = $(ALT_HTTP)
    else
      HTTP_STACK = $(DEFAULT_HTTP)
    endif
  endif
endif

ifeq ($(HTTP_STACK),chrome)

LOCAL_SHARED_LIBRARIES += \
        liblog           \
        libicuuc         \
        libicui18n       \
        libz             \
        libdl            \

LOCAL_STATIC_LIBRARIES += \
        libstagefright_chromium_http

LOCAL_SHARED_LIBRARIES += libstlport libchromium_net
include external/stlport/libstlport.mk

LOCAL_CPPFLAGS += -DCHROMIUM_AVAILABLE=1

endif  # ifeq ($(HTTP_STACK),chrome)

################################################################################

LOCAL_SHARED_LIBRARIES += \
        libstagefright_amrnb_common \
        libstagefright_enc_common \
        libstagefright_avc_common \
        libstagefright_foundation \
        libdl
        
ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)        
ifeq ($(MTK_PLATFORM),$(filter $(MTK_PLATFORM),MT6575 MT6577))
LOCAL_SHARED_LIBRARIES += \
    libmhal
endif  
endif

LOCAL_CFLAGS += -Wno-multichar
LOCAL_CFLAGS += -DOSCL_IMPORT_REF= -DOSCL_EXPORT_REF= -DOSCL_UNUSED_ARG=

###
# 	ANDROID_DEFAULT_HTTP_STREAM is used to check android default http streaming
# 	how to: LOCAL_CFLAGS += ANDROID_DEFAULT_HTTP_STREAM 
# 	notice: if ANDROID_DEFAULT_CODE define, ANDROID_DEFAULT_HTTP_STREAM must be defined
ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE
LOCAL_CFLAGS += -DANDROID_DEFAULT_HTTP_STREAM
else
#LOCAL_CFLAGS += -DANDROID_DEFAULT_HTTP_STREAM


LOCAL_C_INCLUDES += \
        $(TOP)/mediatek/source/external/mhal/src/core/drv/inc
        
LOCAL_SHARED_LIBRARIES += \
        libvcodecdrv     


ifeq ($(strip $(MTK_S3D_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_S3D_SUPPORT
endif
ifeq ($(strip $(MTK_FLV_PLAYBACK_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_FLV_PLAYBACK_SUPPORT
endif
ifeq ($(strip $(MTK_RMVB_PLAYBACK_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_RMVB_PLAYBACK_SUPPORT
endif
ifeq ($(strip $(MTK_ASF_PLAYBACK_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_ASF_PLAYBACK_SUPPORT
endif
ifeq ($(strip $(MTK_AVI_PLAYBACK_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_AVI_PLAYBACK_SUPPORT
	LOCAL_SRC_FILES += $(LOCAL_MTK_PATH)/MtkAVIExtractor.cpp
endif

ifeq ($(strip $(MTK_OGM_PLAYBACK_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_OGM_PLAYBACK_SUPPORT
	LOCAL_SRC_FILES += $(LOCAL_MTK_PATH)/OgmExtractor.cpp
endif

ifeq ($(strip $(MTK_MTKPS_PLAYBACK_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_MTKPS_PLAYBACK_SUPPORT
	LOCAL_STATIC_LIBRARIES +=\
		 libstagefright_mpegps
endif

LOCAL_SRC_FILES += \
	$(LOCAL_MTK_PATH)/MtkAACExtractor.cpp \
	$(LOCAL_MTK_PATH)/TableOfContentThread.cpp

endif

ifeq ($(strip $(MTK_AUDIO_HD_REC_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_AUDIO_HD_REC_SUPPORT
endif

ifeq ($(strip $(MTK_HIGH_QUALITY_THUMBNAIL)),yes)
LOCAL_CFLAGS += -DMTK_HIGH_QUALITY_THUMBNAIL
endif

LOCAL_MODULE:= libstagefright

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),no)
BYPASS_NONNDK_BUILD := no
include $(MTK_PATH_SOURCE)external/nonNDK/nonndk.mk
endif

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
