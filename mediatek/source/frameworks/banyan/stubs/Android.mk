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

# Variables for stub API.
stub_intermediates := \
	$(TARGET_OUT_COMMON_INTERMEDIATES)/JAVA_LIBRARIES/mediatek-api-stubs_current_intermediates
stub_src_dir := $(stub_intermediates)/src
stub_classes_dir := $(stub_intermediates)/classes
stub_full_target := $(stub_intermediates)/classes.jar
stub_jar := $(HOST_OUT_JAVA_LIBRARIES)/mediatek-android.jar
mediatek_framework_res_package := $(call intermediates-dir-for,APPS,mediatek-res,,COMMON)/package-export.apk

$(stub_full_target): MEDIATEK_FRAMEWORK_RES_PACKAGE := $(mediatek_framework_res_package)

mtk_api_stubs_src_list := $(call all-java-files-under,$(MTK_FRAMEWORKS_BASE_JAVA_SRC_DIRS))

# For now, must specify package names whose sources will be built into the stub library.
stub_package_list := com.mediatek.telephony:com.mediatek.telephony.gemini

mediatek-res-source-path := APPS/mediatek-res_intermediates/src

# Stub library source.
# Generate stub source for making MTK SDK shared library.
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(mtk_api_stubs_src_list)
#LOCAL_INTERMEDIATE_SOURCES := \
#	$(mediatek-res-source-path)/com/mediatek/R.java \
#	$(mediatek-res-source-path)/com/mediatek/Manifest.java 

LOCAL_MODULE := mediatek-api-stubs

LOCAL_MODULE_CLASS := JAVA_LIBRARIES

LOCAL_DROIDDOC_OPTIONS:= \
		-stubpackages $(stub_package_list) \
		-stubs $(stub_src_dir) \
		-api $(MTK_INTERNAL_PLATFORM_API_FILE) \
		-nodocs

include $(BUILD_DROIDDOC)

$(full_target): mediatek-res-package-target
$(shell mkdir -p $(OUT_DOCS))

# The target needs mediatek-res files.

$(MTK_INTERNAL_PLATFORM_API_FILE): $(full_target)

# Keep module path for build dependency.
mediatek_api_stubs_src := $(full_target)

# Create stub shared library only on banyan_addon build.
ifeq ($(TARGET_PRODUCT),banyan_addon)

# Documentation.
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under,$(MTK_FRAMEWORKS_BASE_JAVA_SRC_DIRS))

LOCAL_MODULE:= mediatek-sdk
LOCAL_DROIDDOC_OPTIONS := \
    -title "MediaTek SDK" \
    -offlinemode \
    -hdf android.whichdoc online

LOCAL_MODULE_CLASS := JAVA_LIBRARIES

include $(BUILD_DROIDDOC)

# Stub library.
# ==========================================================================

# Reuse class paths from "mediatek-android" module build. Need them to build stub source.
stub_classpath := $(subst $(space),:,$(full_java_libs))

# Build stubs first if in banyan_addon target.

$(stub_full_target): $(mediatek_api_stubs_src)
ifneq ($(PARTIAL_BUILD), true)	
	@echo Compiling MTK SDK stubs: $@
	rm -rf $(stub_classes_dir)
	mkdir -p $(stub_classes_dir)
	find $(stub_src_dir) -name "*.java" > $(stub_intermediates)/java-source-list
	$(TARGET_JAVAC) -encoding ascii -bootclasspath "" \
		-classpath $(stub_classpath) \
		-g $(xlint_unchecked) \
		-extdirs "" -d $(stub_classes_dir) \
		\@$(stub_intermediates)/java-source-list \
		|| ( rm -rf $(stub_classes_dir) ; exit 41 )
	
	if [ ! -f $(MEDIATEK_FRAMEWORK_RES_PACKAGE) ]; then \
	echo Missing file $(MEDIATEK_FRAMEWORK_RES_PACKAGE); \
	rm -rf $(stub_classes_dir); \
	exit 1; \
	fi;
	
	unzip -qo $(MEDIATEK_FRAMEWORK_RES_PACKAGE) -d $(stub_classes_dir)
	(cd $(stub_classes_dir) && rm -rf classes.dex META-INF)
	mkdir -p $(dir $@)
	jar -cf $@ -C $(stub_classes_dir) .
	jar -u0f $@ -C $(stub_classes_dir) resources.arsc
	
$(stub_jar): $(stub_full_target) 
	mkdir -p $(dir $@)
	cp $< $@
endif

endif
