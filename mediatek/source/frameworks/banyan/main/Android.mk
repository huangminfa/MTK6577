#
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
#

# This makefile shows how to build your own shared library that can be
# shipped on the system of a phone, and included additional examples of
# including JNI code with the library and writing client applications against it.

LOCAL_PATH := $(call my-dir)

# MediaTek resource dependency definition.
mediatek-res-source-path := APPS/mediatek-res_intermediates/src
mediatek_res_R_stamp := \
	$(call intermediates-dir-for,APPS,mediatek-res,,COMMON)/src/R.stamp

# MediaTek framework library.
# ============================================================
include $(CLEAR_VARS)

LOCAL_MODULE := mediatek-framework

LOCAL_SRC_FILES := $(call all-java-files-under,$(MTK_FRAMEWORKS_BASE_JAVA_SRC_DIRS))

LOCAL_INTERMEDIATE_SOURCES := \
	$(mediatek-res-source-path)/com/mediatek/R.java \
	$(mediatek-res-source-path)/com/mediatek/Manifest.java 
ifeq (false,$(MTK_BSP_PACKGE))
ifeq (true,$(PARTIAL_BUILD))
LOCAL_PROGUARD_ENABLED := disabled
else
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_SOURCE := javaclassfile
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_EXCLUDED_JAVA_CLASSES += \
           com/mediatek/pluginmanager/*
           
endif
endif

include $(BUILD_JAVA_LIBRARY)
