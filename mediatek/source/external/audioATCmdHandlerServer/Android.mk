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

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := \
	TvoutCmdHandler.cpp \
	AudioCmdHandlerService.cpp \
        AudioCmdHandler.cpp

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
        libmedia \
	libbinder \
	libtvoutpattern \
	libaudiocustparam \
	libaudiocompensationfilter \
	libheadphonecompensationfilter

LOCAL_C_INCLUDES := \
	$(TOP)/frameworks/base/include \
	$(MTK_PATH_SOURCE)/kernel/drivers/video \
	$(MTK_PATH_SOURCE)/frameworks/tvout/pattern \
	$(MTK_PATH_SOURCE)/external \
	#	$(KERNEL_HEADERS) \
	
ifeq ($(strip $(BOARD_USES_GENERIC_AUDIO)),true)
  LOCAL_CFLAGS += -DGENERIC_AUDIO
else
  LOCAL_CFLAGS += -DMTK_AUDIO  
endif

# WB Speech Support
ifeq ($(MTK_WB_SPEECH_SUPPORT),yes)
  LOCAL_CFLAGS += -DMTK_WB_SPEECH_SUPPORT
endif
#WB Speech Support

# Audio HD Record
ifeq ($(MTK_AUDIO_HD_REC_SUPPORT),yes)
LOCAL_CFLAGS += -DMTK_AUDIO_HD_REC_SUPPORT
endif
# Audio HD Record

# Dual Mic Support
ifeq ($(MTK_DUAL_MIC_SUPPORT),yes)
  LOCAL_CFLAGS += -DMTK_DUAL_MIC_SUPPORT
endif
# Dual Mic Support

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := audiocmdservice_atci

ifeq ($(TARGET_SIMULATOR),TRUE)
	LOCAL_LDLIBS += -lpthread
endif

include $(BUILD_EXECUTABLE)
