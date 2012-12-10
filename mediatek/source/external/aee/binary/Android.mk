ifeq ($(TARGET_ARCH),arm)

LOCAL_PATH:= $(call my-dir)

ifeq ($(HAVE_AEE_FEATURE),yes)
include $(CLEAR_VARS)

LOCAL_PREBUILT_EXECUTABLES := \
	bin/aee \
	bin/aee_core_forwarder \
	bin/aee_dumpstate \
	bin/rtt

LOCAL_MODULE_TAGS := user

include $(BUILD_MULTI_PREBUILT)

endif # HAVE_AEE_FEATURE=yes

include $(CLEAR_VARS)

LOCAL_PREBUILT_LIBS := \
	lib/libaed.so \
	lib/libaed_static.a \
	lib/libaee_aed_partial.a \
	lib/libaee_unwind.a

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)


endif # TARGET_ARM == arm
