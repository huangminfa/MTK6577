LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
            $(call all-subdir-java-files) \
	    com/android/server/EventLogTags.logtags \
	    com/android/server/am/EventLogTags.logtags

MTK_SERVICES_JAVA_PATH := ../../../../mediatek/source/frameworks/base/services/java

LOCAL_SRC_FILES += $(call all-java-files-under,$(MTK_SERVICES_JAVA_PATH))

LOCAL_MODULE:= services

LOCAL_JAVA_LIBRARIES := android.policy

LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true

include $(BUILD_JAVA_LIBRARY)

include $(BUILD_DROIDDOC)
