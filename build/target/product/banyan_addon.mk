# List of apps and optional libraries (Java and native) to put in the add-on system image.
#PRODUCT_PACKAGES := \
#	PlatformLibraryClient \
#	com.example.android.platform_library \
#	libplatform_library_jni

PRODUCT_PACKAGES := \
        SDKGallery \
        mediatek-res

# Manually copy the optional library XML files in the system image.
#PRODUCT_COPY_FILES := \
#    device/sample/frameworks/PlatformLibrary/com.example.android.platform_library.xml:system/etc/permissions/com.example.android.platform_library.xml

# name of the add-on
PRODUCT_SDK_ADDON_NAME := banyan_addon

# Copy the manifest and hardware files for the SDK add-on.
# The content of those files is manually created for now.
PRODUCT_SDK_ADDON_COPY_FILES :=

ifneq ($(strip $(BUILD_MTK_SDK)),toolset)
PRODUCT_SDK_ADDON_COPY_FILES += \
    mediatek/source/device/banyan_addon/manifest.ini:manifest.ini \
    mediatek/source/device/banyan_addon/hardware.ini:hardware.ini \
    $(call find-copy-subdir-files,*,mediatek/source/device/banyan_addon/skins,skins)
endif

ifneq ($(strip $(BUILD_MTK_SDK)),api)
PRODUCT_SDK_ADDON_COPY_FILES += \
    $(call find-copy-subdir-files,*,mediatek/source/frameworks/banyan/tools,tools)
endif

# Copy the jar files for the optional libraries that are exposed as APIs.
PRODUCT_SDK_ADDON_COPY_MODULES :=

# Name of the doc to generate and put in the add-on. This must match the name defined
# in the optional library with the tag
#    LOCAL_MODULE:= mediatek-sdk
# in the documentation section.
PRODUCT_SDK_ADDON_DOC_MODULES := mediatek-sdk

PRODUCT_SDK_ADDON_COPY_HOST_OUT :=

# mediatek-android.jar stub library is generated separately (defined in
# mediatek/source/frameworks/banyan_addon/Android.mk) and copied to MTK
# SDK pacakge by using "PRODUCT_SDK_ADDON_COPY_HOST_OUT".
ifneq ($(strip $(BUILD_MTK_SDK)),toolset)
PRODUCT_SDK_ADDON_COPY_HOST_OUT += \
    framework/mediatek-android.jar:libs/mediatek-android.jar \
    bin/emulator:emulator/linux/emulator \
    bin/emulator-arm:emulator/linux/emulator-arm \
    bin/emulator-x86:emulator/linux/emulator-x86 \
    bin/emulator.exe:emulator/windows/emulator.exe \
    bin/emulator-arm.exe:emulator/windows/emulator-arm.exe \
    bin/emulator-x86.exe:emulator/windows/emulator-x86.exe
endif

PRODUCT_COPY_FILES += mediatek/source/frameworks/telephony/etc/apns-conf.xml:system/etc/apns-conf.xml
PRODUCT_COPY_FILES += mediatek/source/frameworks/telephony/etc/spn-conf.xml:system/etc/spn-conf.xml

# This add-on extends the default sdk product.
$(call inherit-product, $(SRC_TARGET_DIR)/product/sdk.mk)

PRODUCT_LOCALES := ldpi mdpi hdpi

# Real name of the add-on. This is the name used to build the add-on.
# Use 'make PRODUCT-<PRODUCT_NAME>-sdk_addon' to build the add-on.
PRODUCT_NAME := banyan_addon
PRODUCT_DEVICE := banyan_addon
PRODUCT_BRAND := banyan_addon
