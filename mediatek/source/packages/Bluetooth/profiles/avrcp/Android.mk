#############################################
# Build Java Package
#############################################

$(info [BlueAngel][OPP]: $(call my-dir))

# copy resource to res
#$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# add aidl include path
LOCAL_AIDL_INCLUDES += $(call my-dir)/src

# include java source and aidl files
LOCAL_SRC_FILES += \
	${MY_BUILD_PREFIX}/profiles/avrcp/src/com/mediatek/bluetooth/avrcp/IBTAvrcpMusicCallback.aidl \
	${MY_BUILD_PREFIX}/profiles/avrcp/src/com/mediatek/bluetooth/avrcp/IBTAvrcpMusic.aidl \
	${MY_BUILD_PREFIX}/profiles/avrcp/src/com/mediatek/bluetooth/avrcp/IBTAvrcpService.aidl \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/avrcp/src) \

#############################################
# End of file
#############################################
