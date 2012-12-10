ifneq ($(TARGET_PRODUCT),generic)
ifneq ($(TARGET_SIMULATOR),true)
ifeq ($(TARGET_ARCH),arm)
ifneq ($(MTK_EMULATOR_SUPPORT),yes)

# factory program
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

GENERIC_CUSTOM_PATH := mediatek/custom/generic/factory
HAVE_CUST_FOLDER := $(shell test -d mediatek/custom/$(TARGET_PRODUCT)/factory && echo yes)
ifeq ($(HAVE_CUST_FOLDER),yes)
CUSTOM_PATH := mediatek/custom/$(TARGET_PRODUCT)/factory
else
CUSTOM_PATH := $(GENERIC_CUSTOM_PATH)
endif
commands_factory_local_path := $(LOCAL_PATH)

CORE_SRC_FILES := \
	src/factory.c \
	src/util/at_command.c \
	src/util/utils.c \
	src/util/uart_op.c 
TEST_SRC_FILES := \
	src/test/ftm.c\
	src/test/ftm_mods.c\
	src/test/ftm_camera.cpp\
 	src/test/ftm_keys.c\
 	src/test/ftm_lcd.c\
 	src/test/ftm_led.c\
 	src/test/ftm_memcard.c\
	src/test/ftm_rtc.cpp\
	src/test/ftm_gsensor.c\
	src/test/ftm_gs_cali.c\
 	src/test/ftm_msensor.c\
 	src/test/ftm_touch.c\
	src/test/ftm_vibrator.c\
 	src/test/ftm_headset.cpp\
 	src/test/ftm_usb.cpp\
 	src/test/ftm_otg.cpp\
	src/test/ftm_idle.c \
 	src/test/ftm_jogball.c \
 	src/test/ftm_ofn.c \
	src/test/ftm_alsps.c \
	src/test/ftm_barometer.c \
 	src/test/ftm_gyroscope.c \
 	src/test/ftm_gyro_cali.c	\
	src/test/ftm_sim.c \
	src/test/ftm_signaltest.c \
	src/test/ftm_speaker.cpp	\
	src/test/ftm_emi.c 

ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
TEST_SRC_FILES += \
	src/test/ftm_audio.cpp\
	src/test/ftm_audio_Common.cpp
endif

	
ifeq ($(MTK_WLAN_SUPPORT), yes)
TEST_SRC_FILES += \
	src/test/ftm_wifi.c \
	src/test/ftm_wifi_op.c 
endif

ifeq ($(HAVE_CMMB_FEATURE),yes)
TEST_SRC_FILES += \
	src/test/ftm_cmmb_impl.cpp \
	src/test/ftm_cmmb.c
endif

ifeq ($(MTK_TVOUT_SUPPORT),yes)
TEST_SRC_FILES += \
	src/test/ftm_tvout.c
endif
ifeq ($(MTK_GPS_SUPPORT),yes)
TEST_SRC_FILES += \
	src/test/ftm_gps.c
endif
ifeq ($(MTK_NFC_SUPPORT),yes)
TEST_SRC_FILES += \
	src/test/ftm_nfc.c
endif

ifeq ($(MTK_FM_SUPPORT), yes)
ifeq ($(MTK_FM_RX_SUPPORT), yes)
TEST_SRC_FILES += \
	src/test/ftm_fm.c 
endif
ifeq ($(MTK_FM_TX_SUPPORT), yes)
TEST_SRC_FILES += \
	src/test/ftm_fmtx.c 
endif
endif

ifeq ($(HAVE_MATV_FEATURE),yes)
TEST_SRC_FILES += \
	src/test/ftm_matv_audio.cpp \
	src/test/ftm_matv.cpp \
	src/test/ftm_matv_preview.cpp \
	src/test/ftm_matv_common.cpp
endif
ifeq ($(MTK_BT_SUPPORT), yes)
TEST_SRC_FILES += \
	src/test/ftm_bt.c\
	src/test/ftm_bt_op.c
endif
ifeq ($(MTK_EMMC_SUPPORT),yes)
TEST_SRC_FILES += \
	src/test/ftm_emmc.c
else
TEST_SRC_FILES += \
	src/test/ftm_flash.c
endif
ifeq ($(MTK_NCP1851_SUPPORT),yes)
TEST_SRC_FILES += \
	src/test/ftm_battery_tbl.c
else
TEST_SRC_FILES += \
  src/test/ftm_battery.c
endif
HAVE_CUST_INC_PATH := $(shell test -d mediatek/custom/$(TARGET_PRODUCT)/factory/inc && echo yes)

ifeq ($(HAVE_CUST_INC_PATH),yes)
  $(info Apply factory custom include path for $(TARGET_DEVICE))
else
  $(info No factory custom include path for $(TARGET_DEVICE))
endif

ifeq ($(HAVE_CUST_INC_PATH),yes)
	LOCAL_CUST_INC_PATH := $(CUSTOM_PATH)/inc
else
	LOCAL_CUST_INC_PATH := $(GENERIC_CUSTOM_PATH)/inc
endif

ifeq ($(MTK_SENSOR_SUPPORT),yes)
LOCAL_CFLAGS += \
    -DMTK_SENSOR_SUPPORT
    
ifeq ($(MTK_SENSOR_MAGNETOMETER),yes)
LOCAL_CFLAGS += \
    -DMTK_SENSOR_MAGNETOMETER
endif
    

ifeq ($(MTK_NFC_SUPPORT),yes)
LOCAL_CFLAGS += \
    -DMTK_NFC_SUPPORT_FM
endif

    
ifeq ($(MTK_SENSOR_ACCELEROMETER),yes)
LOCAL_CFLAGS += \
    -DMTK_SENSOR_ACCELEROMETER
endif

ifeq ($(MTK_SENSOR_ALSPS),yes)
LOCAL_CFLAGS += \
    -DMTK_SENSOR_ALSPS
endif

ifeq ($(MTK_SENSOR_GYROSCOPE),yes)
LOCAL_CFLAGS += \
    -DMTK_SENSOR_GYROSCOPE
endif

ifeq ($(MTK_TB_WIFI_3G_MODE),3GDATA_ONLY)
LOCAL_CFLAGS += \
    -DMTK_TB_WIFI_3G_MODE_3GDATA_ONLY
endif

ifeq ($(MTK_TB_WIFI_3G_MODE),3GDATA_SMS)
LOCAL_CFLAGS += \
    -DMTK_TB_WIFI_3G_MODE_3GDATA_SMS
endif

ifeq ($(MTK_TB_WIFI_3G_MODE),WIFI_ONLY)
LOCAL_CFLAGS += \
    -DMTK_TB_WIFI_ONLY
endif

endif
ifeq (yes,$(GEMINI))
    LOCAL_CFLAGS += -DGEMINI
endif

#MTKBEGIN   [mtk80625][DualTalk]
ifeq ($(MTK_DT_SUPPORT),yes)    
LOCAL_CFLAGS += -DMTK_DT_SUPPORT
endif
#MTKEND   [mtk80625][DualTalk]


ifeq ($(MTK_DIGITAL_MIC_SUPPORT),yes)
  LOCAL_CFLAGS += -DMTK_DIGITAL_MIC_SUPPORT
endif

LOCAL_SRC_FILES := \
	$(CORE_SRC_FILES)\
	$(TEST_SRC_FILES)

LOCAL_C_INCLUDES:= \
	$(LOCAL_PATH)/inc/ \
	mediatek/custom/common/factory/inc \
	$(LOCAL_CUST_INC_PATH) \
	$(MTK_PATH_SOURCE)/external/mhal/src/custom/inc \
	$(MTK_PATH_SOURCE)/external/mhal/inc \
	frameworks/base/include/media \
	$(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio/aud_drv \
	$(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio/LAD \
	$(TOP)/$(MTK_PATH_PLATFORM)/hardware/audio \
	$(MTK_PATH_SOURCE)/external/audiocustparam \
	$(MTK_PATH_SOURCE)/frameworks/base/include/media \
	$(MTK_PATH_CUSTOM)/cgen/cfgfileinc \
	$(MTK_PATH_CUSTOM)/cgen/cfgdefault \
	$(MTK_PATH_CUSTOM)/kernel/imgsensor/inc \
	$(MTK_PATH_CUSTOM)/cgen/inc \
	$(MTK_PATH_CUSTOM)/cgen/inc \
	$(MTK_PATH_CUSTOM)/audioflinger/audio \
	$(MTK_PATH_SOURCE)/external/nvram/libnvram \
	$(MTK_PATH_SOURCE)/external/matvctrl \
	$(MTK_PATH_SOURCE)/external/sensor-tools \
	$(MTK_PATH_SOURCE)/kernel/drivers/video \
	$(MTK_PATH_CUSTOM)/hal/inc \
	$(MTK_PATH_SOURCE)/external/audiodcremoveflt \
	$(MTK_ROOT_CUSTOM_OUT)/kernel/dct \
	system/extras/ext4_utils \

LOCAL_MODULE := factory
# Add to fix Android 2.3 build error, and need to add factory to user_tags.mk for user build
LOCAL_MODULE_TAGS := user

LOCAL_STATIC_LIBRARIES :=
LOCAL_STATIC_LIBRARIES += libminzip libunz libmtdutil libmincrypt libm
LOCAL_STATIC_LIBRARIES += libminiui libpixelflinger_static libpng libz libcutils
LOCAL_STATIC_LIBRARIES += libstdc++ libc 

ifeq ($(MTK_PLATFORM),MT6573)
# FIXME {
LOCAL_SHARED_LIBRARIES:= libc libcutils libmedia libhardware_legacy libhwm libacdk libcameracustom libnvram libdl libaudiocustparam

LOCAL_C_INCLUDES += \
   mediatek/source/hardware/mtk/audio/mt657x_drv \
   $(MTK_PATH_SOURCE)/external/mhal/src/core/scenario/6573/cameradebug/inc \
   $(MTK_PATH_SOURCE)/external/mhal/src/lib/inc/acdk

ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
LOCAL_SHARED_LIBRARIES += libaudio.primary.default
endif

LOCAL_CFLAGS += -DMT6573

# FIXME }
endif

## ==> HAVE_CMMB_FEATURE
ifeq ($(HAVE_CMMB_FEATURE),yes)
ifeq ($(MTK_PLATFORM),MT6575)
LOCAL_C_INCLUDES += \
   $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/include \
   $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb

ifneq (,$(findstring Innofidei,$(MTK_CMMB_CHIP)))          
LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/innofidei 
LOCAL_CFLAGS := -DCMMB_CHIP_INNO
# siano chip used
else
LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/siano \
        $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/siano/hostlib \
        $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/siano/osw/include \
        $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/siano/osw/linux \
        $(MTK_PATH_SOURCE)/../platform/mt6575/external/meta/cmmb/siano/siano_appdriver_new
endif
endif      # MT6575

ifeq ($(MTK_PLATFORM),MT6577)
LOCAL_C_INCLUDES += \
   $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/include \
   $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb

ifneq (,$(findstring Innofidei,$(MTK_CMMB_CHIP)))          
LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/innofidei 
LOCAL_CFLAGS := -DCMMB_CHIP_INNO
# siano chip used
else
LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/siano \
        $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/siano/hostlib \
        $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/siano/osw/include \
        $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/siano/osw/linux \
        $(MTK_PATH_SOURCE)/../platform/mt6577/external/meta/cmmb/siano/siano_appdriver_new
endif
endif      # MT6577

endif 
## <== HAVE_CMMB_FEATURE

ifeq ($(MTK_PLATFORM),MT6575)
# FIXME {
#LOCAL_SHARED_LIBRARIES:= libc libcutils libmedia libhardware_legacy libhwm libacdk
LOCAL_SHARED_LIBRARIES:= libc libcutils libnvram libdl libhwm libmhal libaudiocustparam libext4_utils
LOCAL_SHARED_LIBRARIES += libacdk libcameracustom


LOCAL_C_INCLUDES += \
   mediatek/source/hardware/mtk/audio/mt657x_drv \
   $(MTK_PATH_SOURCE)/external/mhal/src/core/scenario/6575/cameradebug/inc \
   $(MTK_PATH_SOURCE)/external/mhal/src/lib/inc/acdk

ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
LOCAL_SHARED_LIBRARIES += libaudio.primary.default
endif

LOCAL_CFLAGS += -DMT6575
endif

ifeq ($(MTK_PLATFORM),MT6577)
# FIXME {
LOCAL_SHARED_LIBRARIES:= libc libcutils libnvram libdl libhwm libmhal libaudiocustparam libext4_utils
LOCAL_SHARED_LIBRARIES += libacdk libcameracustom

ifeq ($(strip $(BOARD_USES_YUSU_AUDIO)),true)
LOCAL_SHARED_LIBRARIES += libaudio.primary.default
endif

LOCAL_C_INCLUDES += \
   $(MTK_PATH_SOURCE)/external/mhal/src/core/scenario/6575/cameradebug/inc \
   $(MTK_PATH_SOURCE)/external/mhal/src/lib/inc/acdk

LOCAL_CFLAGS += -DMT6575
LOCAL_CFLAGS += -DMT6577
# FIXME }
endif


ifeq ($(HAVE_MATV_FEATURE),yes)
LOCAL_STATIC_LIBRARIES += 
LOCAL_SHARED_LIBRARIES += libmatv_cust
LOCAL_CFLAGS += -DHAVE_MATV_FEATURE
endif

ifeq ($(MTK_DUAL_MIC_SUPPORT),yes)
LOCAL_CFLAG += -DMTK_DUAL_MIC_SUPPORT
LOCAL_CFLAGS += -DFEATURE_FTM_ACSLB
endif

ifeq ($(HAVE_CMMB_FEATURE),yes)
LOCAL_CFLAGS += -DHAVE_CMMB_FEATURE
endif

#ifeq ($(TARGET_NO_FACTORYIMAGE),true)
#  LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT_SBIN)
#endif

ifeq ($(MTK_WLAN_SUPPORT),yes)
LOCAL_CFLAGS += \
    -DWIFI_DRIVER_MODULE_NAME=\"wlan_$(shell echo $(strip $(MTK_WLAN_CHIP)) | tr A-Z a-z)\"
endif

include $(BUILD_EXECUTABLE)

# copy etc/init.rc to rootfs/factory_init.rc for non-factory image mode
$(shell cp $(CUSTOM_PATH)/init.rc $(LOCAL_PATH)/etc/init.rc)
$(shell chmod 777 $(LOCAL_PATH)/etc/init.rc)

ifeq ($(TARGET_NO_FACTORYIMAGE),true)
$(shell echo -e "\nservice factory /system/bin/logwrapper /system/bin/factory\n    oneshot" >> $(LOCAL_PATH)/etc/init.rc)
include $(CLEAR_VARS)
LOCAL_MODULE := init.factory.rc
# Add to fix Android 2.3 build error, and need to add init.factory.rc to user_tags.mk for user build
LOCAL_MODULE_TAGS := eng user

LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := etc/init.rc
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)
else
$(shell echo -e "\nservice factory /system/bin/logwrapper /sbin/factory\n    oneshot" >> $(LOCAL_PATH)/etc/init.rc)
endif


include $(commands_factory_local_path)/src/mtdutil/Android.mk
include $(commands_factory_local_path)/src/miniui/Android.mk


ifeq ($(MTK_PLATFORM),MT6573)

LOCAL_PATH :=  $(CUSTOM_PATH)
include $(CLEAR_VARS)

# copy resources to rootfs/res for test pattern
DIRS := $(TARGET_OUT)/res/sound
$(DIRS):
	echo Directory: $@
	mkdir -p $@
ALL_PREBUILT += $(DIRS)

copy_from := \
	res/sound/testpattern1.wav

copy_to := $(addprefix $(TARGET_OUT)/,$(copy_from))
copy_from := $(addprefix $(LOCAL_PATH)/,$(copy_from))
$(copy_to) : $(TARGET_OUT)/% : $(LOCAL_PATH)/% | $(ACP)
	@if [ ! -h $(TARGET_ROOT_OUT)/res ]; then mkdir -p $(TARGET_ROOT_OUT); ln -s /system/res $(TARGET_ROOT_OUT)/res || echo "Makelink failed !!" ;fi
	$(transform-prebuilt-to-target)
ALL_PREBUILT += $(copy_to)

endif

#################################################################
LOCAL_PATH :=  $(CUSTOM_PATH)

include $(CLEAR_VARS)
ifeq ($(TARGET_NO_FACTORYIMAGE),true)

# copy resources to rootfs/res for non-factory image mode
DIRS := $(TARGET_OUT)/res/images
$(DIRS):
	echo Directory: $@
	mkdir -p $@
ALL_PREBUILT += $(DIRS)

copy_from := \
	res/images/lcd_test_00.png \
	res/images/lcd_test_01.png \
	res/images/lcd_test_02.png \
	res/images/lcd_test_03.png \
	res/images/lcd_test_04.png  \
	res/images/lcd_test_05.png \
	res/images/lcd_test_06.png \
	res/images/lcd_test_07.png 

copy_to := $(addprefix $(TARGET_OUT)/,$(copy_from))
copy_from := $(addprefix $(LOCAL_PATH)/,$(copy_from))
$(copy_to) : $(TARGET_OUT)/% : $(LOCAL_PATH)/% | $(ACP)
	@if [ ! -h $(TARGET_ROOT_OUT)/res ]; then mkdir -p $(TARGET_ROOT_OUT); ln -s /system/res $(TARGET_ROOT_OUT)/res || echo "Makelink failed !!" ;fi
	$(transform-prebuilt-to-target)
ALL_PREBUILT += $(copy_to)
else
$(shell if [ -h $(TARGET_ROOT_OUT)/res ] ;then rm -f $(TARGET_ROOT_OUT)/res ;  fi  )
endif
include $(CLEAR_VARS)
LOCAL_MODULE := factory.ini
# Add to fix Android 2.3 build error, and need to add factory.ini to user_tags.mk for user build
LOCAL_MODULE_TAGS := eng user

LOCAL_SRC_FILES := factory.ini
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH += $(PRODUCT_OUT)
LOCAL_MODULE_PATH += $(TARGET_OUT_ETC)
include $(BUILD_PREBUILT)
##################################################################
endif   # TARGET_ARCH == arm
endif	# !TARGET_SIMULATOR
endif
endif
