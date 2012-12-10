LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

include $(call all-makefiles-under,$(LOCAL_PATH))

ifneq ($(MTK_EMULATOR_SUPPORT), yes)
PLATFORM_PATH := $(MTK_PATH_PLATFORM)hardware
include $(call all-makefiles-under,$(PLATFORM_PATH))
endif
