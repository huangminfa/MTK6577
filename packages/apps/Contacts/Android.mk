LOCAL_PATH:= $(call my-dir)
#This is used for building out an library for RCS-e contact
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
	/src/com/mediatek/contacts/extention/ICallLogExtention.java \
	/src/com/mediatek/contacts/extention/IContactExtention.java \

LOCAL_MODULE := com.mediatek.contacts.extention
include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)

ifeq (OP01,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
ifeq ($(MTK_VT3G324M_SUPPORT), yes)
LOCAL_MANIFEST_FILE := cmcc/AndroidManifest.xml
endif
endif 

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.phone.common \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    com.android.phone.common \
    com.mediatek.CellConnUtil \
    com.mediatek.contacts.extention

LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_REQUIRED_MODULES := libvariablespeed

LOCAL_PACKAGE_NAME := Contacts
LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
