################################################################################
#
################################################################################
LOCAL_PATH := $(call my-dir)

################################################################################
#
################################################################################
include $(CLEAR_VARS)

#-----------------------------------------------------------
plat_path := $(LOCAL_PATH)/$(call lc,$(MTK_PLATFORM))
#link_path := $(LOCAL_PATH)/current

#remove_symbolic_link := $(shell rm -f $(link_path))
#make_symbolic_link := $(shell ln -fs --force $(plat_path) $(link_path))

#-----------------------------------------------------------
# Only include this in non emulator builds.
ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)
#    sinclude $(link_path)/Android.mk
    sinclude $(plat_path)/Android.mk
endif

