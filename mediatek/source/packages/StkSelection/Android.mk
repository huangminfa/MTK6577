# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifneq ($(strip $(MTK_EMULATOR_SUPPORT)), yes)
ifndef MTK_TB_WIFI_3G_MODE

ifeq ($(GEMINI), yes)
ifdef OPTR_SPEC_SEG_DEF
    ifeq ($(OPTR_SPEC_SEG_DEF),NONE)
        LOCAL_MODULE_TAGS := user
        
        LOCAL_SRC_FILES := $(call all-subdir-java-files)
       
        LOCAL_PACKAGE_NAME := StkSelection
        LOCAL_CERTIFICATE := platform
        LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common \
                          com.mediatek.CellConnUtil
        LOCAL_JAVA_LIBRARIES += mediatek-framework 
       
        include $(BUILD_PACKAGE)
    endif
           
    ifeq (OP01,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
        LOCAL_MODULE_TAGS := user
        
        LOCAL_SRC_FILES := $(call all-subdir-java-files)
        
        LOCAL_PACKAGE_NAME := StkSelection
        LOCAL_CERTIFICATE := platform
        LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common \
                           com.mediatek.CellConnUtil
        LOCAL_JAVA_LIBRARIES += mediatek-framework 
        
        include $(BUILD_PACKAGE)
   endif
endif
endif
endif
endif









