ifeq ($(HAVE_MATV_FEATURE), yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.atv.adapter

        
LOCAL_PACKAGE_NAME := MATVEM
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
endif
