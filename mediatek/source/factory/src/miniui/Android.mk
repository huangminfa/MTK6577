LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := miniui.c graphics.c events.c resources.c

HAVE_CUST_INC_PATH := $(shell test -d mediatek/custom/$(TARGET_PRODUCT)/factory/inc && echo yes)

ifeq ($(HAVE_CUST_INC_PATH),yes)
	LOCAL_CUST_INC_PATH := mediatek/custom/$(TARGET_PRODUCT)/factory/inc
else
	LOCAL_CUST_INC_PATH := mediatek/custom/generic/factory/inc
endif

LOCAL_C_INCLUDES +=\
    external/libpng\
    external/zlib\
	$(LOCAL_PATH)/../../inc \
	mediatek/custom/common/factory/inc \
	$(LOCAL_CUST_INC_PATH)

LOCAL_MODULE := libminiui
# Add to fix Android 2.3 build error, and need to add libminiui to user_tags.mk for user build
LOCAL_MODULE_TAGS := user

include $(BUILD_STATIC_LIBRARY)
