ifeq ($(HAVE_CMMB_FEATURE), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


        LOCAL_SRC_FILES: = CMMBDataSource.cpp   \
                            CMMBExtractor.cpp





LOCAL_C_INCLUDES:= \
        $(TOP)/mediatek/source/frameworks/cmmb/include \
        $(TOP)/frameworks/base/media/libstagefright/include
        
        
#LOCAL_SHARED_LIBRARIES := libcmmbsp         
LOCAL_SHARED_LIBRARIES := libdl
         

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE:= libcmmbsource

include $(BUILD_SHARED_LIBRARY)
endif