LOCAL_PATH := $(call my-dir)

define all-zip-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.zip" -and -not -name ".*") \
 )
endef

define all-mp3-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.mp3" -and -not -name ".*") \
 )
endef

include $(CLEAR_VARS)

copy_from := $(call all-zip-files-under)
copy_from += $(call all-mp3-files-under)
copy_to := $(addprefix :system/media/,$(copy_from))
copy_from := $(addprefix $(LOCAL_PATH)/,$(copy_from))
copy_from := $(join $(copy_from),$(copy_to))

$(warning $(copy_from))
PRODUCT_COPY_FILES += $(copy_from)
