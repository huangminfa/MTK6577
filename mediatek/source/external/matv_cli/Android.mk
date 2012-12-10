LOCAL_PATH:= $(call my-dir)

## ==> build this lib only when HAVE_MATV_FEATURE is yes
ifeq ($(HAVE_MATV_FEATURE),yes)
## build this lib for MT5192 or MT5193
#ifeq ($(MTK_ATV_CHIP),MTK_MT5192)
ifeq ($(MTK_ATV_CHIP), $(filter $(MTK_ATV_CHIP),MTK_MT5192 MTK_MT5193))

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=       \
	matv_cli.cpp

LOCAL_SHARED_LIBRARIES := libcutils libutils libbinder libmedia

LOCAL_C_INCLUDES:= $(TOP)/$(MTK_PATH_SOURCE)/frameworks/base/include  

LOCAL_CFLAGS += -Wno-multichar

LOCAL_MODULE:= matv_cli

include $(BUILD_EXECUTABLE)
endif
endif
## <== build this lib only when HAVE_MATV_FEATURE is yes
