# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifneq ($(strip $(MTK_EMULATOR_SUPPORT)), yes)
ifndef MTK_TB_WIFI_3G_MODE

ifeq (OP02,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
LOCAL_MANIFEST_FILE := cu/AndroidManifest.xml
endif

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := Stk1
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
endif
endif
