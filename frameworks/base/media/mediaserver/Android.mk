LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	main_mediaserver.cpp 

LOCAL_SHARED_LIBRARIES := \
	libaudioflinger \
	libcameraservice \
	libmediaplayerservice \
	libutils \
	libmemorydumper \
	libbinder

ifeq ($(HAVE_CMMB_FEATURE), yes)
  LOCAL_CFLAGS += -DMTK_CMMBSP_SUPPORT
#	LOCAL_SHARED_LIBRARIES += libcmmbsp
	LOCAL_SHARED_LIBRARIES += libdl

endif
base := $(LOCAL_PATH)/../..

ifeq ($(HAVE_MATV_FEATURE),yes)
  LOCAL_CFLAGS += -DMTK_MATV_SUPPORT
  LOCAL_SHARED_LIBRARIES += libatvctrlservice
endif

LOCAL_C_INCLUDES := \
    $(base)/services/audioflinger \
    $(base)/services/camera/libcameraservice \
    $(base)/media/libmediaplayerservice \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/memorydumper \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/libs/atvctrlservice \
    $(TOP)/$(MTK_PATH_SOURCE)/external/matvctrl  \
    $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include \
    $(TOP)/mediatek/source/frameworks/cmmb/include 
ifeq ($(MTK_VT3G324M_SUPPORT), yes)
	LOCAL_SHARED_LIBRARIES += libmtk_vt_service
	LOCAL_C_INCLUDES += $(TOP)/mediatek/source/external/VT/service/inc	
endif
LOCAL_MODULE:= mediaserver

include $(BUILD_EXECUTABLE)
