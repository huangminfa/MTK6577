#############################################
# Build Java Package
#############################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

$(info [BlueAngel][PKG] LOCAL_PATH=$(LOCAL_PATH))

### define build path
MY_BUILD_PATH := ${LOCAL_PATH}
MY_BUILD_PREFIX := ..

### clean first
$(shell rm -rf $(LOCAL_PATH)/AndroidManifest.xml)
$(shell rm -rf $(LOCAL_PATH)/res)
$(shell mkdir $(LOCAL_PATH)/res)

### generate AndroidManifest.xml
#"mediatek/config/" + $(FULL_PROJECT) + "/ProjectConfig.mk"
#$(info executing blueangel.py: project[$(FULL_PROJECT)], PYTHONPATH[$(PYTHONPATH)])
PY_RES := $(shell python $(LOCAL_PATH)/blueangel.py)

### include modules' mk file
ifeq ($(MTK_BT_SUPPORT), yes)
include $(MY_MODULE_PATH)/common/bt40/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_OPP), yes)
include $(MY_MODULE_PATH)/profiles/opp/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_PRXM), yes)
include $(MY_MODULE_PATH)/profiles/prxm/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_PRXR), yes)
include $(MY_MODULE_PATH)/profiles/prxr/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_SIMAP), yes)
include $(MY_MODULE_PATH)/profiles/simap/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_HIDH), yes)
include $(MY_MODULE_PATH)/profiles/hid/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_FTP), yes)
include $(MY_MODULE_PATH)/profiles/ftp/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_PBAP), yes)
include $(MY_MODULE_PATH)/profiles/pbap/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_BPP), yes)
include $(MY_MODULE_PATH)/profiles/bpp/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_BIP), yes)
include $(MY_MODULE_PATH)/profiles/bip/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_DUN), yes)
include $(MY_MODULE_PATH)/profiles/dun/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_AVRCP), yes)
include $(MY_MODULE_PATH)/profiles/avrcp/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_PAN), yes)
include $(MY_MODULE_PATH)/profiles/pan/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_MAPS), yes)
include $(MY_MODULE_PATH)/profiles/map/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_TIMES), yes)
include $(MY_MODULE_PATH)/profiles/times/Android.mk
endif
ifeq ($(MTK_BT_PROFILE_TIMEC), yes)
include $(MY_MODULE_PATH)/profiles/timec/Android.mk
endif
### config package and build
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := MtkBt
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAGS := -include $(LOCAL_PATH)/proguard.flags
include $(BUILD_PACKAGE)

#############################################
# End of file
#############################################
