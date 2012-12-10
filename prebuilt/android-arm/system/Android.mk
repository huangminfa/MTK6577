LOCAL_PATH := $(call my-dir)

define all-apk-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.apk" -and -not -name ".*") \
 )
endef

include $(CLEAR_VARS)
TARGET_APK := $(TARGET_OUT)

copy_from := $(call all-apk-files-under)


copy_to := $(addprefix $(TARGE_APK)/,$(copy_from))
copy_from := $(addprefix $(LOCAL_PATH)/,$(copy_from))

$(copy_to) : $(TARGET_APK)/% : $(LOCAL_PATH)/% | $(ACP)
	$(transform-prebuilt-to-target)

ALL_PREBUILT += $(copy_to)

ifneq (,$(ONE_SHOT_MAKEFILE))
ALL_MODULES = $(ALL_PREBUILT)
endif
