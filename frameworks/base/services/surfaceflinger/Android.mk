LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    Layer.cpp 								\
    LayerBase.cpp 							\
    LayerDim.cpp 							\
    LayerScreenshot.cpp						\
    DdmConnection.cpp						\
    DisplayHardware/DisplayHardware.cpp 	\
    DisplayHardware/DisplayHardwareBase.cpp \
    DisplayHardware/HWComposer.cpp 			\
    GLExtensions.cpp 						\
    MessageQueue.cpp 						\
    SurfaceFlinger.cpp 						\
    SurfaceTextureLayer.cpp 				\
    Transform.cpp 							\

# link MTK internal impl
#---------------------------------------------------------------------------------------------------
LOCAL_STATIC_LIBRARIES += libsurfaceflinger-mtk
#---------------------------------------------------------------------------------------------------

LOCAL_CFLAGS:= -DLOG_TAG=\"SurfaceFlinger\"
LOCAL_CFLAGS += -DGL_GLEXT_PROTOTYPES -DEGL_EGLEXT_PROTOTYPES

ifeq ($(TARGET_BOARD_PLATFORM), omap3)
	LOCAL_CFLAGS += -DNO_RGBX_8888
endif
ifeq ($(TARGET_BOARD_PLATFORM), omap4)
	LOCAL_CFLAGS += -DHAS_CONTEXT_PRIORITY
endif
ifeq ($(TARGET_BOARD_PLATFORM), s5pc110)
	LOCAL_CFLAGS += -DHAS_CONTEXT_PRIORITY -DNEVER_DEFAULT_TO_ASYNC_MODE
	LOCAL_CFLAGS += -DREFRESH_RATE=56
endif

LOCAL_CFLAGS += -DADVANCED_HWC

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libhardware \
	libutils \
	libEGL \
	libGLESv1_CM \
	libbinder \
	libui \
	libgui \
	libskia \

# this is only needed for DDMS debugging
LOCAL_SHARED_LIBRARIES += libdvm libandroid_runtime

LOCAL_C_INCLUDES := \
	external/skia/include/core \
	external/skia/include/images \
	$(call include-path-for, corecg graphics) \

LOCAL_C_INCLUDES += hardware/libhardware/modules/gralloc

# check platform config
#---------------------------------------------------------------------------------------------------
LOCAL_C_INCLUDES += $(MTK_PATH_SOURCE)/hardware/mmumapper

ifeq ($(MTK_TVOUT_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_TVOUT_SUPPORT
endif

ifeq ($(MTK_HDMI_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_HDMI_SUPPORT
endif

ifeq ($(MTK_S3D_SUPPORT), yes)
	LOCAL_CFLAGS += -DMTK_S3D_SUPPORT
endif
#---------------------------------------------------------------------------------------------------
LOCAL_MODULE:= libsurfaceflinger


include $(BUILD_SHARED_LIBRARY)
