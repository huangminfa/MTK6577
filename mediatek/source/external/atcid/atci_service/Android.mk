# Copyright 2006 The Android Open Source Project

# XXX using libutils for simulator build only...
#


LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


LOCAL_MODULE:= atci_service
LOCAL_MODULE_TAGS:=eng

LOCAL_SRC_FILES:= \
    src/atci_service.c \
    src/atci_generic_cmd_dispatch.c \
    src/atci_audio_cmd.cpp \
    src/atci_telephony_cmd.c \
    src/atci_system_cmd.c \
    ../atci/src/atcid_util.c \
    ../atci/src/at_tok.c

 
LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libmedia \
    libbinder \
    liblog

LOCAL_C_INCLUDES += \
        $(KERNEL_HEADERS) \
        $(TOP)/frameworks/base/include

LOCAL_C_INCLUDES += ${LOCAL_PATH}/../atci/src

ifeq ($(MTK_GPS_SUPPORT),yes)

LOCAL_SRC_FILES += \
    src/atci_gps_cmd.c

LOCAL_CFLAGS += \
    -DENABLE_GPS_AT_CMD

endif

ifeq ($(MTK_NFC_SUPPORT),yes)

LOCAL_SRC_FILES += \
    src/atci_nfc_cmd.c

LOCAL_CFLAGS += \
    -DENABLE_NFC_AT_CMD

endif


#Haman changed for Arima version
#ifeq ($(MTK_WLAN_SUPPORT),yes)
#  ifeq ($(MTK_WLAN_CHIP),MT6620)
#
#LOCAL_SRC_FILES += \
#    src/atci_wlan_cmd.c
#
#LOCAL_SHARED_LIBRARIES += \
#	liblgerft
#
#LOCAL_C_INCLUDES += \
#    $(MTK_PATH_SOURCE)/external/liblgerft
#
#LOCAL_CFLAGS += \
#    -DENABLE_WLAN_AT_CMD
#
#  endif
#endif
#change end

# Add Flags and source code for MMC AT Command
LOCAL_SRC_FILES += \
    src/atci_mmc_cmd.c
LOCAL_CFLAGS += \
    -DENABLE_MMC_AT_CMD

# Add Flags and source code for CODECRC AT Command
LOCAL_SRC_FILES += \
    src/atci_code_cmd.c
LOCAL_CFLAGS += \
    -DENABLE_CODECRC_AT_CMD

#Add Flags and source code for  backlight and  vibrator AT Command
LOCAL_SRC_FILES += \
    src/atci_lcdbacklight_vibrator_cmd.c 
LOCAL_CFLAGS += \
    -DENABLE_BLK_VIBR_AT_CMD

#Add Flags and source code for kpd AT Command
LOCAL_SRC_FILES += \
    src/atci_kpd_cmd.c
LOCAL_CFLAGS += \
    -DENABLE_KPD_AT_CMD

include $(BUILD_EXECUTABLE)




