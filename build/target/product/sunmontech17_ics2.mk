PRODUCT_PACKAGES := \
#    FMRadio
#    MyTube \
#    VideoPlayer


$(call inherit-product, $(SRC_TARGET_DIR)/product/common.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/telephony.mk)

# Overrides
PRODUCT_BRAND  := alps
PRODUCT_NAME   := $(TARGET_PRODUCT)
PRODUCT_DEVICE := $(TARGET_PRODUCT)

# This is for custom project language configuration.
PRODUCT_LOCALES := $(MTK_PRODUCT_LOCALES)

PRODUCT_COPY_FILES += frameworks/base/data/etc/android.hardware.camera.front.xml:system/etc/permissions/android.hardware.camera.front.xml

ifeq ($(MTK_AGPS_APP),yes)
   PRODUCT_COPY_FILES += mediatek/source/frameworks/agps/etc/agps_profiles_conf.xml:system/etc/agps_profiles_conf.xml
endif
