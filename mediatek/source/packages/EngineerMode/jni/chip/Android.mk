ifeq ($(MTK_ENGINEERMODE_APP), yes)

LOCAL_MODULE_TAGS := user

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := chip_support.c feature_support.c

    
ifeq ($(MTK_FM_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_FM_SUPPORT
endif
ifeq ($(MTK_FM_TX_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_FM_TX_SUPPORT
endif
ifeq ($(MTK_RADIO_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_RADIO_SUPPORT
endif
ifeq ($(MTK_AGPS_APP), yes)
	LOCAL_CFLAGS += -DMTK_AGPS_APP
endif
ifeq ($(MTK_GPS_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_GPS_SUPPORT
endif
ifeq ($(HAVE_MATV_FEATURE), yes)
	LOCAL_CFLAGS += -DHAVE_MATV_FEATURE
endif
ifeq ($(MTK_BT_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_BT_SUPPORT
endif
ifeq ($(MTK_WLAN_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_WLAN_SUPPORT
endif
ifeq ($(MTK_TTY_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_TTY_SUPPORT
endif


LOCAL_C_INCLUDES := $(JNI_H_INCLUDE) $(LOCAL_PATH)
LOCAL_MODULE := libem_chip_support_jni
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)

endif

