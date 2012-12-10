ifeq ($(MTK_GOOGLEOTA_SUPPORT), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	googleota_binder.cpp

ifeq ($(MTK_EMMC_SUPPORT),yes)
LOCAL_CFLAGS += \
    -DMTK_EMMC_SUPPORT
endif

LOCAL_C_INCLUDES += \
	../../../external/mediatek/meta/include \
	../../external/nvram/libfile_op/ \
../../external/mtd_util 


LOCAL_SHARED_LIBRARIES := \
	libutils \
	libcutils \
	libbinder \
	libfile_op 
	
LOCAL_STATIC_LIBRARIES := \
	libmtd_util
	
#LOCAL_PRELINK_MODULE := false


LOCAL_MODULE:= GoogleOtaBinder

include $(BUILD_EXECUTABLE)
endif
