LOCAL_PATH := $(call my-dir)

define all-apk-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.apk" -and -not -name ".*") \
 )
endef

define all-xml-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.apk" -and -not -name ".*") \
 )
endef

include $(CLEAR_VARS)
copy_from := $(call all-apk-files-under)
copy_from += $(call all-xml-files-under)
copy_to := $(addprefix :system/,$(copy_from))
copy_from := $(addprefix $(LOCAL_PATH)/,$(copy_from))
copy_from := $(join $(copy_from),$(copy_to))

$(warning $(copy_from))
PRODUCT_COPY_FILES += $(copy_from)
