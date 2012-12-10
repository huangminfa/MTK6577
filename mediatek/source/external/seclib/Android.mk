LOCAL_PATH:= $(call my-dir)

###############################################################################
# SEC STATIC LIBRARY
###############################################################################
include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS += libsec.so libsbchk.a
ifneq ($(CUSTOM_SEC_AUTH_SUPPORT),yes)
LOCAL_PREBUILT_LIBS += libauth.a
endif
include $(BUILD_MULTI_PREBUILT)

###############################################################################
# SEC SBCHK APPLICATION
###############################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := sbchk
LOCAL_SRC_FILES := sbchk.c
LOCAL_STATIC_LIBRARIES := libsbchk
ifeq ($(CUSTOM_SEC_AUTH_SUPPORT),yes)
$(call config-custom-folder,custom:security/sbchk)
LOCAL_SRC_FILES += custom/cust_auth.c
else
LOCAL_SRC_FILES += auth/sec_wrapper.c
LOCAL_STATIC_LIBRARIES += libauth
endif
LOCAL_MODULE_PATH := $(TARGET_OUT_BIN)
include $(BUILD_EXECUTABLE)

###############################################################################
# SEC FILE LIST
###############################################################################
# for S_ANDRO_SFL.ini
include $(CLEAR_VARS)
LOCAL_MODULE := S_ANDRO_SFL.ini
#LOCAL_MODULE_TAGS :=
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
include $(BUILD_PREBUILT)

# for S_SECRO_SFL.ini
include $(CLEAR_VARS)
LOCAL_MODULE := S_SECRO_SFL.ini
#LOCAL_MODULE_TAGS :=
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/secro
include $(BUILD_PREBUILT)

###############################################################################
# PLATFORM DEPENDENT MODULE
###############################################################################

# SEC KERNEL MODULE
# SEC RECOVERY MODULE
# MTK DRVB MODULE
PLATFORM_PATH := $(MTK_PATH_PLATFORM)/external/security
include $(call all-makefiles-under,$(PLATFORM_PATH))
