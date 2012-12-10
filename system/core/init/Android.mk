# Copyright 2005 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	builtins.c \
	init.c \
	devices.c \
	property_service.c \
	property_patch.c \
	util.c \
	parser.c \
	logo.c \
	keychords.c \
	signal_handler.c \
	init_parser.c \
	ueventd.c \
	ueventd_parser.c

ifeq ($(strip $(INIT_BOOTCHART)),true)
LOCAL_SRC_FILES += bootchart.c
LOCAL_CFLAGS    += -DBOOTCHART=1
endif

LOCAL_MODULE:= init

LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT)
LOCAL_UNSTRIPPED_PATH := $(TARGET_ROOT_OUT_UNSTRIPPED)

LOCAL_STATIC_LIBRARIES := libcutils libc

ifeq ($(PARTIAL_BUILD),true)
LOCAL_CFLAGS += -DPARTIAL_BUILD
endif
ifeq ($(HAVE_AEE_FEATURE),yes)
LOCAL_CFLAGS += -DHAVE_AEE_FEATURE
LOCAL_STATIC_LIBRARIES += libaed_static
endif

#INF@MTK, add for factory {
ifeq ($(TARGET_NO_FACTORYIMAGE),true)
LOCAL_CFLAGS += -DUSE_BUILT_IN_FACTORY
endif

#INF@MTK, add for eng/usr build
ifeq ($(TARGET_BUILD_VARIANT),eng)
LOCAL_CFLAGS += -DINIT_ENG_BUILD
endif
#INF@MTK, add for emulator 
ifeq ($(MTK_EMULATOR_SUPPORT),yes)
LOCAL_CFLAGS += -DMTK_EMU_SUPPORT
endif
include $(BUILD_EXECUTABLE)

# Make a symlink from /sbin/ueventd to /init
SYMLINKS := $(TARGET_ROOT_OUT)/sbin/ueventd
$(SYMLINKS): INIT_BINARY := $(LOCAL_MODULE)
$(SYMLINKS): $(LOCAL_INSTALLED_MODULE) $(LOCAL_PATH)/Android.mk
	@echo "Symlink: $@ -> ../$(INIT_BINARY)"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf ../$(INIT_BINARY) $@

ALL_DEFAULT_INSTALLED_MODULES += $(SYMLINKS)

# We need this so that the installed files could be picked up based on the
# local module name
ALL_MODULES.$(LOCAL_MODULE).INSTALLED := \
    $(ALL_MODULES.$(LOCAL_MODULE).INSTALLED) $(SYMLINKS)
