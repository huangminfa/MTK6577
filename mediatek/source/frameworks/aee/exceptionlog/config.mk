ifeq (yes,$(HAVE_AEE_FEATURE))
# included by frameworks/base/Android.mk indirectly,
# so traverse from frameworks/base.
LOCAL_SRC_FILES += \
	../../mediatek/source/frameworks/aee/exceptionlog/java/com/mediatek/exceptionlog/IExceptionLogService.aidl
endif

