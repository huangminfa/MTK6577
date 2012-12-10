#
# ipo daemon
#
ifeq ($(MTK_IPO_SUPPORT), yes)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= ipodmain.c \
                  ipodcommon.c \
                  ipodlights.c \
                  bootlogo.c \
                  boot_logo_updater.c

ifeq ($(MTK_TB_WIFI_3G_MODE), WIFI_ONLY)
LOCAL_CFLAGS += -DMTK_TB_WIFI_ONLY
endif

LOCAL_C_INCLUDES += $(MTK_PATH_SOURCE)/kernel/drivers/video/
LOCAL_C_INCLUDES += $(MTK_PATH_SOURCE)/external/mtd_util/
LOCAL_C_INCLUDES += $(MTK_PATH_CUSTOM)/uboot/inc
LOCAL_C_INCLUDES += $(MTK_PATH_CUSTOM)/kernel/dct/
LOCAL_C_INCLUDES += $(MTK_PATH_PLATFORM)/uboot/inc
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_C_INCLUDES += $(TOP)/external/zlib/
LOCAL_MODULE:= ipod

LOCAL_SHARED_LIBRARIES := libcutils libc libstdc++ libz libdl liblog
LOCAL_STATIC_LIBRARIES := libmtd_util

LOCAL_PRELINK_MODULE := false

include $(BUILD_EXECUTABLE)

endif
