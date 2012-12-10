LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=       \
        ChromiumHTTPDataSource.cpp        \
        support.cpp                     \

LOCAL_C_INCLUDES:= \
        $(JNI_H_INCLUDE) \
        frameworks/base/media/libstagefright \
        $(TOP)/frameworks/base/include/media/stagefright/openmax \
        external/chromium \
        external/chromium/android \
		$(MTK_PATH_CUSTOM)/native

LOCAL_CFLAGS += -Wno-multichar

LOCAL_SHARED_LIBRARIES += libstlport
include external/stlport/libstlport.mk

ifeq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)
LOCAL_CFLAGS += -DANDROID_DEFAULT_CODE
LOCAL_CFLAGS += -DANDROID_DEFAULT_HTTP_STREAM
endif

 

LOCAL_MODULE:= libstagefright_chromium_http

include $(BUILD_STATIC_LIBRARY)
