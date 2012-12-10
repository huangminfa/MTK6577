ifneq ($(strip $(MTK_USES_STAGEFRIGHT_DEFAULT_CODE)),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

include $(call all-makefiles-under,$(LOCAL_PATH))

endif
