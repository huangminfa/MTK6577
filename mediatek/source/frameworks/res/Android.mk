
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)


LOCAL_PACKAGE_NAME := mediatek-res
LOCAL_CERTIFICATE := platform

LOCAL_AAPT_FLAGS := -x

LOCAL_NO_MTKRES := true

LOCAL_MODULE_TAGS := optional

# Install this alongside the libraries.
LOCAL_MODULE_PATH := $(TARGET_OUT_JAVA_LIBRARIES)

# Create package-export.apk, which other packages can use to get
# PRODUCT-agnostic resource data like IDs and type definitions.
LOCAL_EXPORT_PACKAGE_RESOURCES := true




include $(BUILD_PACKAGE)

# define a global intermediate target that other module may depend on.
.PHONY: mediatek-res-package-target
mediatek-res-package-target: $(LOCAL_BUILT_MODULE)
