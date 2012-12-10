#############################################
# Build Java Package
#############################################

$(info [BlueAngel][TIMES]: $(call my-dir))

# copy resource to build folder
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/times/src) \
	${MY_BUILD_PREFIX}/profiles/times/src/com/mediatek/bluetooth/service/IBluetoothTimes.aidl \

#############################################
# End of file
#############################################
