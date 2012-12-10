LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

AudioDriverIncludePath := aud_drv

LOCAL_SRC_FILES:=               \
    AudioFlinger.cpp            \
    AudioMixer.cpp.arm          \
    AudioResampler.cpp.arm      \
    AudioResamplerSinc.cpp.arm  \
    AudioResamplerCubic.cpp.arm \
    AudioPolicyService.cpp      \
    
ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
  LOCAL_CFLAGS += -DMTK_AUDIO
else
  LOCAL_CFLAGS += -DGENERIC_AUDIO
endif


ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
  LOCAL_SRC_FILES += \
      AudioResamplermtk.cpp
endif
   
LOCAL_C_INCLUDES := \
    system/media/audio_effects/include
    
ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
  LOCAL_C_INCLUDES += \
      $(MTK_PATH_SOURCE)/frameworks/base/include/media \
      $(MTK_PATH_PLATFORM)/hardware/audio/aud_drv \
      $(MTK_PATH_PLATFORM)/hardware/audio
endif

LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libbinder \
    libmedia \
    libhardware \
    libhardware_legacy \
    libeffects \
    libdl \
    libpowermanager

ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
  LOCAL_SHARED_LIBRARIES += \
      libblisrc
endif

LOCAL_STATIC_LIBRARIES := \
    libcpustats \
    libmedia_helper

# SRS Processing
ifeq ($(strip $(HAVE_SRSAUDIOEFFECT_FEATURE)),yes)
LOCAL_CFLAGS += -DHAVE_SRSAUDIOEFFECT
include mediatek/binary/3rd-party/free/SRS_AudioEffect/srs_processing/AF_PATCH.mk
endif
# SRS Processing

ifeq ($(strip $(TARGET_BUILD_VARIANT)),eng)
  LOCAL_CFLAGS += -DDEBUG_AUDIO_PCM
endif

# MATV ANALOG SUPPORT
ifeq ($(HAVE_MATV_FEATURE),yes)
  ifeq ($(MTK_MATV_ANALOG_SUPPORT),yes)
    LOCAL_CFLAGS += -DMATV_AUDIO_LINEIN_PATH
  endif
endif
# MATV ANALOG SUPPORT

LOCAL_MODULE:= libaudioflinger

include $(BUILD_SHARED_LIBRARY)
