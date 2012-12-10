LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include frameworks/base/media/libstagefright/codecs/common/Config.mk

LOCAL_MODULE_TAGS := optional

LOCAL_PRELINK_MODULE := false
 	
LOCAL_SRC_FILES := \
	VorbisEncoder.cpp

LOCAL_C_INCLUDES := \
	$(TOP)/frameworks/base/media/libstagefright/include \
	$(TOP)/frameworks/base/media/libstagefright/codecs/common/include \
	$(TOP)/frameworks/base/include \
	$(TOP)/mediatek/source/external/vorbisenc \
	$(TOP)/mediatek/source/frameworks/base/media/libstagefright/include \

	
LOCAL_MODULE := libstagefright_vorbisenc

LOCAL_ARM_MODE := arm

LOCAL_STATIC_LIBRARIES :=

LOCAL_SHARED_LIBRARIES := libvorbisenc_mtk

include $(BUILD_STATIC_LIBRARY)

