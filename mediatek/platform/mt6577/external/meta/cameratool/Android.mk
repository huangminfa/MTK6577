LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#ifeq ($(MTK_PLATFORM),MT6575)
ifeq ($(MTK_PLATFORM),$(filter $(MTK_PLATFORM),MT6575 MT6575T MT6577))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif

