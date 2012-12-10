ifeq ($(MTK_BT_SUPPORT),yes)
#############################################
# Build Java Package
#############################################

$(info [BlueAngel] building BlueAngel package...)

MY_MODULE_PATH := $(call my-dir)
include $(MY_MODULE_PATH)/build/Android.mk

#############################################
# End of file
#############################################
endif
