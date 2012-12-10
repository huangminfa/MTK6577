LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := deltat.c
LOCAL_SHARED_LIBRARIES := libcutils
LOCAL_MODULE := libdeltat
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := deltat.c
LOCAL_STATIC_LIBRARIES := libcutils
LOCAL_MODULE := libdeltat
include $(BUILD_HOST_SHARED_LIBRARY)
