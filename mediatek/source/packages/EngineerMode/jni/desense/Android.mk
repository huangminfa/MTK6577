ifeq ($(MTK_ENGINEERMODE_APP), yes)
LOCAL_MODULE_TAGS := user

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES :=dsense_jni.cpp 
									
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE) \
		$(MTK_PATH_SOURCE)/kernel/drivers/video \
		$(TOP)/frameworks/base/include/media
		
		
LOCAL_SHARED_LIBRARIES := \
	  libnativehelper \
    libandroid_runtime \
	  libutils \
	  libmedia
	
LOCAL_MODULE := libem_dsense_jni
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)

endif

