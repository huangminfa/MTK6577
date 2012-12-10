ifeq ($(MTK_ENGINEERMODE_APP), yes)
ifeq ($(MTK_BT_SUPPORT), yes)
LOCAL_MODULE_TAGS := user

#ifneq ($(BUILD_WITHOUT_PV),true)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    com_mediatek_engineermode_bluetooth_bttest.cpp 


LOCAL_SHARED_LIBRARIES := \
    libbluetoothem_mtk \
    libbluetooth_relayer \
	  libnativehelper \
    libandroid_runtime \
	  libutils \
	  libui


LOCAL_STATIC_LIBRARIES := 


LOCAL_PRELINK_MODULE :=false

LOCAL_C_INCLUDES += \
    external/tremor/Tremor \
    mediatek/source/external/bluetooth/driver \
    frameworks/base/core/jni \
    $(PV_INCLUDES) \
    $(JNI_H_INCLUDE) \
    $(call include-path-for, corecg graphics)

LOCAL_CFLAGS +=

LOCAL_LDLIBS := -lpthread

LOCAL_MODULE:= libembttest_jni

include $(BUILD_SHARED_LIBRARY)

#endif

endif
endif