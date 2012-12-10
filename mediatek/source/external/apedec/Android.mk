LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)

ifeq ($(MTK_AUDIO_APE_SUPPORT),yes)

LOCAL_PREBUILT_LIBS := libapedec_mtk.so

include $(BUILD_MULTI_PREBUILT)

endif