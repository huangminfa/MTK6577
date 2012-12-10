ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

COMMON_PATH := $(LOCAL_PATH)/common

ifeq ($(filter $(strip $(MTK_PLATFORM)),MT6583),)
PLATFORM_PATH := $(MTK_PATH_PLATFORM)external/meta
include $(call all-makefiles-under,$(LOCAL_PATH))
include $(call all-makefiles-under,$(PLATFORM_PATH))
endif

endif

