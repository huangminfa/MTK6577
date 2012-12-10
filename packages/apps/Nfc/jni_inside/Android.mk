ifdef MTK_NFC_INSIDE

LOCAL_PATH := $(call my-dir)
        
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    com_android_nfc_NativeLlcpConnectionlessSocket.cpp \
    com_android_nfc_NativeLlcpServiceSocket.cpp \
    com_android_nfc_NativeLlcpSocket.cpp \
    com_android_nfc_NativeNfcManager.cpp \
    com_android_nfc_NativeNfcTag.cpp \
    com_android_nfc_NativeP2pDevice.cpp \
    com_android_nfc_NativeNfcSecureElement.cpp \
    com_android_nfc_list.cpp \
    com_android_nfc.cpp \
    utilities.c

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \
    external/libnfc-opennfc/open_nfc/open_nfc/interfaces \
    external/libnfc-opennfc/open_nfc/open_nfc/porting/common

LOCAL_SHARED_LIBRARIES := \
    libnativehelper \
    libcutils \
    libutils \
    libopen_nfc_client_jni

#LOCAL_CFLAGS += -O0 -g

# To use OpenNFCExtentions
ifdef EXTENDED_OPEN_NFC
LOCAL_C_INCLUDES += external/libnfc-opennfc/open_nfc_extension 
LOCAL_CFLAGS += -DEXTENDED_OPEN_NFC
LOCAL_SHARED_LIBRARIES +=  libopen_nfc_ext
endif

LOCAL_MODULE := libnfc_jni
LOCAL_MODULE_TAGS := optional eng

include $(BUILD_SHARED_LIBRARY)

endif
