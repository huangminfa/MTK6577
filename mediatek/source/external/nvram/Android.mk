LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
		libfile_op \
		libnvram   \
		nvram_agent_binder \
		libnvram_daemon_callback \
	))

include $(subdirs)

include $(MTK_PATH_SOURCE)/external/nvram/nvram_daemon/Android.mk

ifeq ($(MTK_NVRAM_SECURITY),yes)
include $(MTK_PATH_SOURCE)/external/nvram/libnvram_sec/Android.mk
endif

