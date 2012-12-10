LOCAL_PATH := $(call my-dir)

define all-apk-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.apk" -and -not -name ".*") \
 )
endef

#app apk
include $(CLEAR_VARS)
TARGET_GAPPS_APK := $(TARGET_OUT_DATA)

copy_from := $(call all-apk-files-under)


copy_to := $(addprefix $(TARGET_GAPPS_APK)/,$(copy_from))
copy_from := $(addprefix $(LOCAL_PATH)/,$(copy_from))

$(copy_to) : $(TARGET_GAPPS_APK)/% : $(LOCAL_PATH)/% | $(ACP)
	$(transform-prebuilt-to-target)

ALL_PREBUILT += $(copy_to)

ifneq (,$(ONE_SHOT_MAKEFILE))
ALL_MODULES = $(ALL_PREBUILT)
endif
