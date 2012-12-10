LOCAL_PATH:= $(call my-dir)

#
# libmediaplayerservice
#

include $(CLEAR_VARS)

$(call make-private-dependency,\
  $(BOARD_CONFIG_DIR)/configs/StageFright.mk \
)

LOCAL_SRC_FILES:=               \
    MediaRecorderClient.cpp     \
    MediaPlayerService.cpp      \
    MetadataRetrieverClient.cpp \
    TestPlayerStub.cpp          \
    MidiMetadataRetriever.cpp   \
    MidiFile.cpp                \
    StagefrightPlayer.cpp       \
    StagefrightRecorder.cpp

LOCAL_SHARED_LIBRARIES :=     		\
	libcutils             			\
	libutils              			\
	libbinder             			\
	libvorbisidec         			\
	libsonivox            			\
	libmedia              			\
	libcamera_client      			\
	libandroid_runtime    			\
	libstagefright        			\
	libstagefright_omx    			\
	libstagefright_foundation       \
	libgui                          \
	libdl                       \
	libhardware_legacy          \

ifeq ($(strip $(MTK_TB_DEBUG_SUPPORT)),yes)
LOCAL_SHARED_LIBRARIES +=     \
  libmtkdcplayer
endif

LOCAL_SHARED_LIBRARIES+= \
    libdrmframework

ifeq ($(strip $(MTK_BSP_PACKAGE)),no)
LOCAL_SHARED_LIBRARIES += \
        libdrmmtkutil
endif

LOCAL_STATIC_LIBRARIES := \
        libstagefright_nuplayer                 \
        libstagefright_rtsp                     \

ifeq ($(HAVE_MATV_FEATURE),yes)
  LOCAL_CFLAGS += -DMTK_MATV_SUPPORT
endif	

ifeq ($(MTK_FM_SUPPORT),yes)
  LOCAL_CFLAGS += -DMTK_FM_SUPPORT
endif

ifeq ($(strip $(BOARD_USES_GENERIC_AUDIO)),true)
  LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE
endif

ifeq ($(strip $(MTK_S3D_SUPPORT)),yes)
LOCAL_CFLAGS += -DMTK_S3D_SUPPORT
endif


ifneq ($(strip $(BOARD_USES_GENERIC_AUDIO)),true)
ifneq ($(strip $(HAVE_MATV_FEATURE))_$(strip $(MTK_FM_SUPPORT)), no_no)
  LOCAL_SHARED_LIBRARIES += libmtkplayer
endif	
endif

ifeq ($(strip $(MTK_DRM_APP)),yes)
LOCAL_CFLAGS += -DMTK_DRM_APP
endif

LOCAL_C_INCLUDES :=                                                 \
	$(JNI_H_INCLUDE)                                                \
	$(call include-path-for, graphics corecg)                       \
	$(TOP)/frameworks/base/include/media/stagefright/openmax \
	$(TOP)/frameworks/base/media/libstagefright/include             \
	$(TOP)/frameworks/base/media/libstagefright/rtsp                \
  $(TOP)/external/tremolo/Tremolo                                 \
  $(TOP)/external                                                 \

ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
LOCAL_C_INCLUDES+= \
	$(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/media/libmediaplayerservice  \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/libs/libmtkplayer                 \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include                      \
	$(TOP)/$(MTK_PATH_CUSTOM)/hal/audioflinger/audio

LOCAL_MTK_PATH:=../../../../mediatek/source/frameworks/base/media/libmediaplayerservice
LOCAL_SRC_FILES+= \
  	$(LOCAL_MTK_PATH)/NotifySender.cpp
LOCAL_C_INCLUDES+= \
   $(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio/aud_drv     \
   $(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio/LAD
endif

ifeq ($(strip $(BOARD_USES_6575_MFV_HW)),true)
LOCAL_CFLAGS += -DMT6575
endif

ifeq ($(strip $(MTK_AUDIO_HD_REC_SUPPORT)), yes)
	LOCAL_CFLAGS += -DMTK_AUDIO_HD_REC_SUPPORT
endif

LOCAL_MODULE:= libmediaplayerservice

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

