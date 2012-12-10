ifeq ($(MTK_GOOGLEOTA_SUPPORT), yes)
LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := \
 	$(call all-java-files-under,src) \
        src/com/mediatek/GoogleOta/IGoogleOtaService.aidl \

LOCAL_PACKAGE_NAME := GoogleOta
LOCAL_CERTIFICATE := platform
#LOCAL_MODULE:= com.mediatek.GoogleOta

include $(BUILD_PACKAGE)

endif

