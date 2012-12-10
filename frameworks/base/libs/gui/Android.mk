LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	ISensorEventConnection.cpp \
	ISensorServer.cpp \
	ISurfaceTexture.cpp \
	Sensor.cpp \
	SensorChannel.cpp \
	SensorEventQueue.cpp \
	SensorManager.cpp \
	SurfaceTexture.cpp \
	SurfaceTextureClient.cpp \
	ISurfaceComposer.cpp \
	ISurface.cpp \
	ISurfaceComposerClient.cpp \
	IGraphicBufferAlloc.cpp \
	LayerState.cpp \
	Surface.cpp \
	SurfaceComposerClient.cpp \

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libbinder \
	libhardware \
	libhardware_legacy \
	libui \
	libEGL \
	libGLESv2 \


# mod area by mtk03712
#---------------------------------------------------------------------------------------------------
# link MTK internal impl
LOCAL_STATIC_LIBRARIES += libgui-mtk

ifeq ($(MTK_EMULATOR_SUPPORT), yes)
LOCAL_CFLAGS += -DEMULATOR_SUPPORT
else
LOCAL_C_INCLUDES += \
    $(MTK_PATH_PLATFORM) \
    $(MTK_PATH_SOURCE)/external/mhal/inc

LOCAL_CFLAGS += -DUSE_MDP

LOCAL_SHARED_LIBRARIES += libmhalmdp
endif
#---------------------------------------------------------------------------------------------------

ifeq ($(ALPS00357741), yes)
LOCAL_CFLAGS += -DALPS00357741
endif


LOCAL_MODULE:= libgui

ifeq ($(TARGET_BOARD_PLATFORM), tegra)
	LOCAL_CFLAGS += -DALLOW_DEQUEUE_CURRENT_BUFFER
endif

include $(BUILD_SHARED_LIBRARY)

ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call first-makefiles-under,$(LOCAL_PATH))
endif
