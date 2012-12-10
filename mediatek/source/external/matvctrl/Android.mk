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
LOCAL_PATH := $(my-dir)

## ==> build this lib only when HAVE_MATV_FEATURE is yes
#ifeq ($(HAVE_MATV_FEATURE),yes)
## build this lib for MT5192 or MT5193
#ifeq ($(MTK_ATV_CHIP),MTK_MT5192)
ifeq ($(MTK_ATV_CHIP), $(filter $(MTK_ATV_CHIP),MTK_MT5192 MTK_MT5193))

include $(CLEAR_VARS)
ifeq ($(MTK_ATV_CHIP),MTK_MT5192)
LOCAL_PREBUILT_LIBS := libmatvctrl.a
else
LOCAL_PREBUILT_LIBS := libmatvctrl_93.a
endif
include $(BUILD_MULTI_PREBUILT)
endif
#endif
## <== build this lib only when HAVE_MATV_FEATURE is yes
