LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
            $(call all-subdir-java-files)

#LOCAL_NO_STANDARD_LIBRARIES := true
#LOCAL_JAVA_LIBRARIES := core ext framework
#$(call dist-for-goals, $(LOCAL_BUILT_MODULE):DMAgent.jar)

LOCAL_MODULE_TAGS := user

LOCAL_MODULE:= DMAgent
LOCAL_MODULE_CLASS := JAVA_LIBRARIES

#LOCAL_DX_FLAGS := --core-library

include $(BUILD_JAVA_LIBRARY)

include $(BUILD_DROIDDOC)

