# Copyright 2006 The Android Open Source Project

# XXX using libutils for simulator build only...
#


LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


LOCAL_MODULE:= atcid
LOCAL_MODULE_TAGS:= eng

LOCAL_SRC_FILES:= \
    src/atcid_serial.c \
    src/atcid.c \
    src/atcid_cmd_dispatch.c \
    src/atcid_cust_cmd.c \
    src/atcid_util.c \
    src/at_tok.c

 
LOCAL_SHARED_LIBRARIES := \
    libcutils 
ifeq ($(MTK_WLAN_SUPPORT),yes)
    LOCAL_SHARED_LIBRARIES += libwifitest
endif

LOCAL_C_INCLUDES += \
        $(KERNEL_HEADERS) \
        $(MTK_PATH_SOURCE)/kernel/drivers/video \
        $(MTK_PATH_SOURCE)/kernel/drivers/leds

include $(BUILD_EXECUTABLE)




