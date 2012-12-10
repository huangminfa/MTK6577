ifeq ($(MTK_ENGINEERMODE_APP), yes)

LOCAL_MODULE_TAGS := user

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := meta_gpio.c gpio_jni.c msdc_jni.c
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE) $(LOCAL_PATH) mediatek/source/external/meta/common/inc mediatek/source/kernel/include
LOCAL_SHARED_LIBRARIES := libft libutils
LOCAL_MODULE := libem_gpio_jni
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)

###############################################################################
# SELF TEST
###############################################################################
BUILD_SELF_TEST := false
ifeq ($(BUILD_SELF_TEST), true)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := meta_gpio_test.c
LOCAL_C_INCLUDES := external/mediatek/meta/common/inc
LOCAL_MODULE := meta_gpio_test
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_SHARED_LIBRARIES := libft
LOCAL_STATIC_LIBRARIES := libmeta_gpio
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_UNSTRIPPED_PATH := $(TARGET_ROOT_OUT_SBIN_UNSTRIPPED)
include $(BUILD_EXECUTABLE)
endif

endif


