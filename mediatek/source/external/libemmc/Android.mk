ifeq ($(MTK_EMMC_SUPPORT), yes)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE:= libemmc
LOCAL_SRC_FILES := libemmc.c
LOCAL_STATIC_LIBRARIES := libcutils
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE_TAGS := optional
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libemmc
LOCAL_WHOLE_STATIC_LIBRARIES := libemmc
LOCAL_SHARED_LIBRARIES := libcutils
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)

endif

