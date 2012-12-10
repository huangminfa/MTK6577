LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_SRC_FILES := \
HeadphoneCompensationFilter.cpp \
HeadphoneCompFltCustParam.cpp

LOCAL_C_INCLUDES := \
	$(MTK_PATH_SOURCE)/external/nvram/libnvram


LOCAL_PRELINK_MODULE := false 

LOCAL_SHARED_LIBRARIES := \
    libbessound_mtk \
    libnvram \
    libnativehelper \
    libcutils \
    libutils 
	
LOCAL_MODULE := libheadphonecompensationfilter

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
