#############################################
# Build Java Package
#############################################

$(info [BlueAngel][TIMEC]: $(call my-dir))

# copy resource to build folder
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/timec/src) \
	${MY_BUILD_PREFIX}/profiles/timec/src/com/mediatek/bluetooth/service/IBluetoothTimec.aidl \

#############################################
# End of file
#############################################
