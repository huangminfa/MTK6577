# Copyright 2010 MediaTek AEE Project

ifeq ($(HAVE_AEE_FEATURE),yes)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := aee_aed
LOCAL_STATIC_LIBRARIES := libaee_aed_partial libaee_unwind libaee_unwind_support libdrvb
LOCAL_SHARED_LIBRARIES := libaed liblog

include $(BUILD_EXECUTABLE)

SYMLINKS := $(TARGET_OUT)/bin/debuggerd
$(SYMLINKS): DEBUGGERD_BINARY := aee_aed
$(SYMLINKS): $(LOCAL_INSTALLED_MODULE) $(LOCAL_PATH)/Android.mk
	@echo "Symlink: $@ -> $(DEBUGGERD_BINARY)"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf $(DEBUGGERD_BINARY) $@

ALL_DEFAULT_INSTALLED_MODULES += $(SYMLINKS)
ALL_MODULES.$(LOCAL_MODULE).INSTALLED :=\
	$(ALL_MODULES.$(LOCAL_MODULE).INSTALLED) $(SYMLINKS)

endif