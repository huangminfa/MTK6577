LOCAL_PATH:=$(call my-dir)
include $(CLEAR_VARS)
#LOCAL_ARM_MODE:=arm
LOCAL_SHARED_LIBRARIES:= libc libcutils
LOCAL_SRC_FILES:= \
    MVG_Powerloss_App.c
LOCAL_C_INCLUDES:= external/mediatek/MVG_APP
LOCAL_MODULE:=mvg_app
include $(BUILD_EXECUTABLE)


