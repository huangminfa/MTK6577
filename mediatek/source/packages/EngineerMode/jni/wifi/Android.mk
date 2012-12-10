ifeq ($(MTK_ENGINEERMODE_APP), yes)
ifeq ($(MTK_WLAN_SUPPORT), yes)

LOCAL_MODULE_TAGS := user

#ifneq ($(BUILD_WITHOUT_PV),true)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


LOCAL_SRC_FILES:= \
    com_mediatek_engineermode_wifi_EMWifi.cpp

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libnativehelper \
    libutils \
    libskia \
    libui \
    libcutils


LOCAL_STATIC_LIBRARIES := lib_wifi_em


LOCAL_PRELINK_MODULE := false

LOCAL_C_INCLUDES += \
    external/tremor/Tremor \
    frameworks/base/core/jni \
    $(PV_INCLUDES) \
    $(JNI_H_INCLUDE) \
    $(call include-path-for, corecg graphics)

#LOCAL_CFLAGS +=

LOCAL_LDLIBS := -lpthread

LOCAL_MODULE:= libemwifi_jni

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
        dbg.cpp    \
        CAdapter.cpp \
        CLocalOID.cpp \
        mt5921.cpp \
        mt6620.cpp \
        WiFi_EM_API.cpp 
#        test_wifi_em.cpp

LOCAL_SHARED_LIBRARIES := \
        libcutils \
        libutils

LOCAL_MODULE := \
        lib_wifi_em

include $(BUILD_STATIC_LIBRARY)


#endif
endif
endif
