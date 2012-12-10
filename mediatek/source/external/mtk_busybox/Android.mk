#ifneq ($(TARGET_SIMULATOR),true)
#LOCAL_PATH:= $(call my-dir)
#include $(CLEAR_VARS)

#LOCAL_SRC_FILES:= ifconfig.c \
#				  interface.c \
#				  libbb/inet_common.c

LOCAL_MODULE_TAGS := user
#LOCAL_MODULE := ifconfig
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/include  

#include $(BUILD_EXECUTABLE)
#endif




LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := user
LOCAL_SRC_FILES := interface.c \
				   libbb/messages.c \
				   libbb/inet_common.c \
				   libbb/fgets_str.c \
				   libbb/getopt32.c \
				   libbb/printable.c \
				   libbb/ptr_to_globals.c \
				   libbb/xfuncs.c \
				   libbb/verror_msg.c \
				   libbb/platform.c \
				   libbb/xconnect.c \
				   libbb/xfuncs_printf.c \
				   libbb/llist.c \
				   libbb/default_error_retval.c \
				   libbb/xfunc_die.c \
				   libbb/xatonum.c \
				   libbb/full_write.c \
				   libbb/safe_write.c \
				   libbb/skip_whitespace.c \
				   libbb/perror_msg.c \
				   libbb/wfopen.c \
				   libbb/fflush_stdout_and_exit.c \
				   libbb/recursive_action.c \
				   libbb/read.c \
				   libbb/concat_subpath_file.c \
				   libbb/concat_path_file.c \
				   libbb/last_char_is.c \
				   libbb/bb_basename.c \
				   libbb/bb_strtonum.c \
				   libbb/safe_strncpy.c \
				   libbb/xreadlink.c

				  

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
					$(KERNEL_HEADERS)

LOCAL_CFLAGS += -Wall -O2
LOCAL_MODULE := libinterface
LOCAL_SHARED_LIBRARIES :=
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := user
LOCAL_SRC_FILES:= ifconfig.c

LOCAL_MODULE := mtk_ifconfig
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
					$(KERNEL_HEADERS)
LOCAL_CFLAGS :=
LOCAL_STATIC_LIBRARIES := libinterface
LOCAL_SHARED_LIBRARIES := libsysutils libcutils libnetutils libcrypto
include $(BUILD_EXECUTABLE)



include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := user
LOCAL_SRC_FILES:= route.c

LOCAL_MODULE := mtk_route
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
					$(KERNEL_HEADERS)

LOCAL_CFLAGS :=
LOCAL_STATIC_LIBRARIES := libinterface
LOCAL_SHARED_LIBRARIES := libsysutils libcutils libnetutils libcrypto
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := user
LOCAL_SRC_FILES:= netstat.c

LOCAL_MODULE := mtk_netstat
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
					$(KERNEL_HEADERS)
LOCAL_CFLAGS :=
LOCAL_STATIC_LIBRARIES := libinterface
LOCAL_SHARED_LIBRARIES := libsysutils libcutils libnetutils libcrypto
include $(BUILD_EXECUTABLE)



