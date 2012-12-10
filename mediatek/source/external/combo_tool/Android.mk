# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Configuration
BUILD_PATCH  := false
BUILD_WMT_CFG   := false

ifeq ($(MTK_COMBO_SUPPORT), yes)

LOCAL_PATH := $(call my-dir)

ifeq ($(MTK_COMBO_CHIP), MT6628)

    BUILD_PATCH := true
    BUILD_WMT_CFG := true

    ifeq ($(BUILD_WMT_CFG), true)
        $(call config-custom-folder, cfg_folder:hal/ant)
    endif
    ifeq ($(BUILD_PATCH), true)
    $(call config-custom-folder, patch_folder:hal/combo)
    endif
    
    ##### INSTALL WMT.CFG FOR COMBO CONFIG #####
    ifeq ($(BUILD_WMT_CFG), true)
    include $(CLEAR_VARS)
    LOCAL_MODULE := WMT.cfg
    LOCAL_MODULE_TAGS := user
    LOCAL_MODULE_CLASS := ETC
    LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
    LOCAL_SRC_FILES := cfg_folder/$(LOCAL_MODULE)
    include $(BUILD_PREBUILT)
    endif

    ##### INSTALL CONNECTIVITY COMBO PATCH #####
    ifeq ($(BUILD_PATCH), true)
    include $(CLEAR_VARS)
    LOCAL_MODULE := mt6628_patch_e1_hdr.bin
    LOCAL_MODULE_TAGS := user
    LOCAL_MODULE_CLASS := ETC
    LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
    LOCAL_SRC_FILES := patch_folder/$(LOCAL_MODULE)
    include $(BUILD_PREBUILT)
    endif 
 
    ifeq ($(BUILD_PATCH), true)
    include $(CLEAR_VARS)
    LOCAL_MODULE := mt6628_patch_e2_hdr.bin
    LOCAL_MODULE_TAGS := user
    LOCAL_MODULE_CLASS := ETC
    LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
    LOCAL_SRC_FILES := patch_folder/$(LOCAL_MODULE)
    include $(BUILD_PREBUILT)
    endif
else
# applied to MT6620
    ifeq ($(MTK_COMBO_CHIP), MT6620E2)
        BUILD_PATCH := true
        BUILD_WMT_CFG := true
    endif

    ifeq ($(MTK_COMBO_CHIP), MT6620E3)
        BUILD_PATCH := true
        BUILD_WMT_CFG := true
    endif
    
    ifeq ($(BUILD_WMT_CFG), true)
        ifeq ($(BUILD_PATCH), true)
            $(call config-custom-folder,custom:hal/combo custom2:hal/ant)
        else
            $(call config-custom-folder,custom2:hal/ant)
        endif
    else
        ifeq ($(BUILD_PATCH), true)
            $(call config-custom-folder,custom:hal/combo)
        endif
    endif

##### INSTALL WMT.CFG FOR COMBO CONFIG #####
    ifeq ($(BUILD_WMT_CFG), true)
    include $(CLEAR_VARS)
    LOCAL_MODULE := WMT.cfg
    LOCAL_MODULE_TAGS := user
    LOCAL_MODULE_CLASS := ETC
    LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
    LOCAL_SRC_FILES := custom2/$(LOCAL_MODULE)
    include $(BUILD_PREBUILT)
    endif

##### INSTALL CONNECTIVITY COMBO PATCH #####

    ifeq ($(BUILD_PATCH), true)
        include $(CLEAR_VARS)
        ifeq ($(MTK_COMBO_CHIP), MT6620E2)
            LOCAL_MODULE := mt6620_patch_hdr.bin
        else
            LOCAL_MODULE := mt6620_patch_e3_hdr.bin
        endif
            LOCAL_MODULE_TAGS := user
            LOCAL_MODULE_CLASS := ETC
            LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
            LOCAL_SRC_FILES := custom/$(LOCAL_MODULE)
            include $(BUILD_PREBUILT)
    endif

    ifeq ($(BUILD_PATCH), true)
        include $(CLEAR_VARS)
        LOCAL_MODULE := mt6620_patch_e6_hdr.bin
        LOCAL_MODULE_TAGS := user
        LOCAL_MODULE_CLASS := ETC
        LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
        LOCAL_SRC_FILES := custom/$(LOCAL_MODULE)
        include $(BUILD_PREBUILT)
    endif
endif
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
