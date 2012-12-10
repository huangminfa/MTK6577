LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES =       \
	OMXCodec_ut.cpp  

LOCAL_SHARED_LIBRARIES := \
	libstagefright libbinder libmedia libutils

LOCAL_C_INCLUDES:= \
	$(JNI_H_INCLUDE) \
	frameworks/base/media/libstagefright \
	$(TOP)/frameworks/base/include/media/stagefright/openmax \
	$(TOP)/frameworks/base/include/media/stagefright

LOCAL_MODULE:= omxcodec_ut

include $(BUILD_EXECUTABLE)
