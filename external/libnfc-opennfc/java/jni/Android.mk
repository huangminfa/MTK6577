LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PRELINK_MODULE	:=	false

LOCAL_SRC_FILES			:=	\

LOCAL_C_INCLUDES		+=	$(JNI_H_INCLUDE)	\

LOCAL_SHARED_LIBRARIES	:=	libnativehelper		\
							libcutils			\
							libutils			\
							libnfc

LOCAL_CFLAGS += -O0 -g

LOCAL_MODULE := libnfc_jni
LOCAL_MODULE_TAGS := optional eng

include $(BUILD_SHARED_LIBRARY)