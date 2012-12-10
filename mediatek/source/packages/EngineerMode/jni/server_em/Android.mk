ifeq ($(MTK_ENGINEERMODE_APP), yes)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

  
LOCAL_SRC_FILES := main.cpp \
		AFMThread.cpp \
		AFMSync.cpp \
		AFMSocket.cpp \
		server.cpp \
		RPCClient.cpp \
		Modules.cpp \
		ModuleBasebandRegDump.cpp \
		ModuleCpuFreqTest.cpp \
		ModuleFB0.cpp \
		ModuleCpuStress.cpp \

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
    $(MTK_PATH_SOURCE)/kernel/drivers/video \
		$(TOP)/frameworks/base/include/media
		
LOCAL_SHARED_LIBRARIES := \
	  libnativehelper \
    libandroid_runtime \
	  libutils 
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE := em_svr
LOCAL_PRELINK_MODULE := false
include $(BUILD_EXECUTABLE)

endif


