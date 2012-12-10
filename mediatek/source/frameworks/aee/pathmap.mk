MTK_FRAMEWORK_BASE :=
ifeq (yes,$(HAVE_AEE_FEATURE))
    MTK_FRAMEWORK_BASE += aee/exceptionlog
endif
