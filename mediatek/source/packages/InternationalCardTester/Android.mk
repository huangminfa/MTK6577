LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(EVDO_DT_SUPPORT), yes)
LOCAL_MODULE_TAGS := eng
else
LOCAL_MODULE_TAGS := optional
endif

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_PACKAGE_NAME := InternationalCardTester
LOCAL_CERTIFICATE := platform

ifeq ($(EVDO_DT_SUPPORT), yes)
include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
