# Copyright 2005 The Android Open Source Project

###############################################################################
# Configuration
###############################################################################

###############################################################################
# build start
###############################################################################
ifeq ($(MTK_GPS_SUPPORT), yes)
LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)
# for MTK Navigation Library
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6620)
include $(MY_LOCAL_PATH)/mnl6620/lib_6620/Android.mk
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6628)
include $(MY_LOCAL_PATH)/mnl6628/lib_6628/Android.mk
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3332)
include $(MY_LOCAL_PATH)/mnl3332/lib_6620/Android.mk
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3336)
include $(MY_LOCAL_PATH)/mnl3336/lib_3336/Android.mk
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3326)
include $(MY_LOCAL_PATH)/mnl3326/lib_3326/Android.mk
endif

# for MT6620 and MT3326 difference
MNL6620_SRC_FILES := \
          mnl6620/mnl_process_6620.c \
          mnl6620/mtk_gps_6620.c \
          mnl6620/mnl_common_6620.c
          
MNL3336_SRC_FILES := \
          mnl3336/mnl_process_3336.c \
          mnl3336/mtk_gps_3336.c \
          mnl3336/mnl_common_3336.c
          
MNL3326_SRC_FILES := \
          mnl3326/mnl_process.c \
          mnl3326/mtk_gps.c \
          mnl3326/mnl_common.c \
          mnl3326/MTK_Sys.c
          
MNL6628_SRC_FILES := \
          mnl6628/mnl_process_6620.c \
          mnl6628/mtk_gps_6620.c \
          mnl6628/mnl_common_6620.c
          
MNL3332_SRC_FILES := \
          mnl3332/mnl_process_6620.c \
          mnl3332/mtk_gps_6620.c \
          mnl3332/mnl_common_6620.c
          
MND6620_SRC_FILES := \
          mnl6620/mnld_6620.c \
          mnl6620/mnl_common_6620.c
          
MND3336_SRC_FILES := \
          mnl3336/mnld_3336.c \
          mnl3336/mnl_common_3336.c
          
MND3326_SRC_FILES := \
          mnl3326/mnld.c \
          mnl3326/mnl_common.c
          
MND6628_SRC_FILES := \
          mnl6628/mnld_6620.c \
          mnl6628/mnl_common_6620.c
          
MND3332_SRC_FILES := \
          mnl3332/mnld_6620.c \
          mnl3332/mnl_common_6620.c
#------------------------------------------------------------------------------
# for GPS driver demo 
ifeq ($(BUILD_DEMO), true)
include $(MY_LOCAL_PATH)/demo/Android.mk
endif
#------------------------------------------------------------------------------
#compile libmnlp

include $(CLEAR_VARS)
LOCAL_PATH := $(MY_LOCAL_PATH)
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6620)
LOCAL_SRC_FILES := \
         $(MNL6620_SRC_FILES) 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6628)
LOCAL_SRC_FILES := \
         $(MNL6628_SRC_FILES) 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3332)
LOCAL_SRC_FILES := \
         $(MNL3332_SRC_FILES) 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3336)
LOCAL_SRC_FILES := \
         $(MNL3336_SRC_FILES) 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3326)
LOCAL_SRC_FILES := \
         $(MNL3326_SRC_FILES)
endif
LOCAL_MODULE := libmnlp
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_UNSTRIPPED_PATH := $(TARGET_ROOT_OUT_SBIN_UNSTRIPPED)
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6620)
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/mnl6620/include_6620 \
                   $(MTK_PATH_SOURCE)/external/nvram/libnvram \
                   $(MTK_PATH_CUSTOM)/cgen/cfgfileinc \
                   $(MTK_PATH_CUSTOM)/cgen/cfgdefault \
                   $(MTK_PATH_CUSTOM)/cgen/inc  
LOCAL_SHARED_LIBRARIES += libcutils libc libm libdeltat libnvram
LOCAL_STATIC_LIBRARIES += libmnl libcutils libc libm libsupl libagent libhotstill 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6628)
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/mnl6628/include_6628 \
                   $(MTK_PATH_SOURCE)/external/nvram/libnvram \
                   $(MTK_PATH_CUSTOM)/cgen/cfgfileinc \
                   $(MTK_PATH_CUSTOM)/cgen/cfgdefault \
                   $(MTK_PATH_CUSTOM)/cgen/inc  
LOCAL_SHARED_LIBRARIES += libcutils libc libm libdeltat libnvram
LOCAL_STATIC_LIBRARIES += libmnl libcutils libc libm libsupl libagent libhotstill 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3332)
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/mnl3332/include_6620 \
                   $(MTK_PATH_SOURCE)/external/nvram/libnvram \
                   $(MTK_PATH_CUSTOM)/cgen/cfgfileinc \
                   $(MTK_PATH_CUSTOM)/cgen/cfgdefault \
                   $(MTK_PATH_CUSTOM)/cgen/inc  
LOCAL_SHARED_LIBRARIES += libcutils libc libm libdeltat libnvram
LOCAL_STATIC_LIBRARIES += libmnl libcutils libc libm libsupl libagent libhotstill 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3336)
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/mnl3336/include_3336 \
                   $(MTK_PATH_SOURCE)/external/nvram/libnvram \
                   $(MTK_PATH_CUSTOM)/cgen/cfgfileinc \
                   $(MTK_PATH_CUSTOM)/cgen/cfgdefault \
                   $(MTK_PATH_CUSTOM)/cgen/inc  
LOCAL_SHARED_LIBRARIES += libcutils libc libm libdeltat libnvram
LOCAL_STATIC_LIBRARIES += libmnl libcutils libc libm libsupl libagent libhotstill 
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3326)
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/mnl3326/include \
                   $(MTK_PATH_SOURCE)/external/nvram/libnvram \
                   $(MTK_PATH_CUSTOM)/cgen/cfgfileinc \
                   $(MTK_PATH_CUSTOM)/cgen/cfgdefault \
                   $(MTK_PATH_CUSTOM)/cgen/inc
LOCAL_SHARED_LIBRARIES += libcutils libc libm libdeltat libnvram
LOCAL_STATIC_LIBRARIES += libmnl libagent libhotstill libsupl
endif
include $(BUILD_EXECUTABLE)

#------------------------------------------------------------------------------
#compile mnld
include $(CLEAR_VARS)
LOCAL_PATH := $(MY_LOCAL_PATH)
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6620)
LOCAL_SRC_FILES := \
         $(MND6620_SRC_FILES)
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT6628)
LOCAL_SRC_FILES := \
         $(MND6628_SRC_FILES)
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3332)
LOCAL_SRC_FILES := \
         $(MND3332_SRC_FILES)
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3336)
LOCAL_SRC_FILES := \
         $(MND3336_SRC_FILES)
endif
ifeq ($(MTK_GPS_CHIP), MTK_GPS_MT3326)
LOCAL_SRC_FILES := \
         $(MND3326_SRC_FILES)
endif
LOCAL_MODULE := mnld
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_UNSTRIPPED_PATH := $(TARGET_ROOT_OUT_SBIN_UNSTRIPPED)
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES += libcutils libc libm
include $(BUILD_EXECUTABLE)
endif
