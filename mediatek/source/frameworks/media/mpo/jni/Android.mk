LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#ifeq ($(MTK_PLATFORM), MT6573)
#ifeq ($(TARGET_PRODUCT), generic)
## compile nothing
#else
#ifeq ($(strip $(BOARD_USES_MT6573_JPEG_HW_DECODER)),true)

ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)

LOCAL_SRC_FILES := \
	mpodecoder_jni.cpp 

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
	
LOCAL_C_INCLUDES += \
 	$(MTK_PATH_SOURCE)/external/mhal/inc \
	external/skia/include/core \
	external/skia/include/effects \
	external/skia/include/images \
	external/skia/src/ports \
	external/skia/include/utils \
	frameworks/base/core/jni/android/graphics \
        $(MTK_PATH_SOURCE)/external/mhal/src/core/common/libmpo \
        $(MTK_PATH_SOURCE)/external/mhal/src/core/common/libmpo/mpodecoder

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libskia \
        libandroid_runtime \
        libmpo \
        libmpodecoder

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := libmpojni
include $(BUILD_SHARED_LIBRARY)
#endif
#endif

endif
