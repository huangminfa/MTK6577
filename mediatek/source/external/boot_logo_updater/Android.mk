#########################################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES := boot_logo_updater.c\
		   bootlogo.c

LOCAL_STATIC_LIBRARIES := libc libcutils
LOCAL_SHARED_LIBRARIES := libcutils libc libstdc++ libz libdl liblog

LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_MODULE := boot_logo_updater
LOCAL_MODULE_PATH := $(TARGET_OUT_BIN)
LOCAL_C_INCLUDES += $(TOP)/external/zlib/
include $(BUILD_EXECUTABLE)


#########################################################
include $(CLEAR_VARS)

LOCAL_MODULE := boot_logo
LOCAL_MODULE_TAGS := user
LOCAL_MODULE_CLASS := DATA
LOCAL_MODULE_PATH := $(TARGET_OUT)/media/images

LOCAL_GENERATE_CUSTOM_FOLDER := custom:uboot/logo
LOCAL_SRC_FILES := custom/$(LOCAL_MODULE)

include $(BUILD_PREBUILT)


#########################################################
include $(CLEAR_VARS)

LOCAL_MODULE := boot_logo1
LOCAL_MODULE_TAGS := user
LOCAL_MODULE_CLASS := DATA
LOCAL_MODULE_PATH := $(TARGET_OUT)/media/images

LOCAL_GENERATE_CUSTOM_FOLDER := custom:uboot/logo
LOCAL_SRC_FILES := custom/$(LOCAL_MODULE)

include $(BUILD_PREBUILT)
BAOXUE_LOGO_CUSPATH := $(LOCAL_PATH)/custom

BAOXUE_KERNEL_LOGO_SRC := $(BAOXUE_LOGO_CUSPATH)/$(BOOT_LOGO)/$(BOOT_LOGO)_kernel.bmp \
	$(BAOXUE_LOGO_CUSPATH)/$(BOOT_LOGO)/$(BOOT_LOGO)_kernel1.bmp

$(BAOXUE_LOGO_CUSPATH)/boot_logo $(BAOXUE_LOGO_CUSPATH)/boot_logo1 : $(BAOXUE_KERNEL_LOGO_SRC)
	$(hide) cd $(BAOXUE_LOGO_CUSPATH);./update $(BOOT_LOGO)
	-rm $(BAOXUE_LOGO_CUSPATH)/$(BOOT_LOGO).raw
