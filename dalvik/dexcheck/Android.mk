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
# dexcheck, used to check the DEX method count if over threshold
#
LOCAL_PATH:= $(call my-dir)

dexcheck_src_files := \
		dexcheck.cpp

dexcheck_c_includes := \
		dalvik \
		$(JNI_H_INCLUDE)

dexcheck_shared_libraries :=

dexcheck_static_libraries := \
		libdex

##
##
## Build the host command line tool dexcheck
##
##
include $(CLEAR_VARS)
LOCAL_MODULE := dexcheck
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(dexcheck_src_files)
LOCAL_C_INCLUDES := $(dexcheck_c_includes)
LOCAL_SHARED_LIBRARIES := $(dexcheck_shared_libraries)
LOCAL_STATIC_LIBRARIES := $(dexcheck_static_libraries) liblog

ifneq ($(strip $(USE_MINGW)),)
LOCAL_STATIC_LIBRARIES += libz
else
LOCAL_LDLIBS += -lpthread -lz
endif

include $(BUILD_HOST_EXECUTABLE)
