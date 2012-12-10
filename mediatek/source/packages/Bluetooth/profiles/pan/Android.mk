#############################################
# Build Java Package
#############################################

$(info [BlueAngel][PAN]: $(call my-dir))

# copy resource to res
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/pan/src) \
	${MY_BUILD_PREFIX}/profiles/pan/src/com/mediatek/bluetooth/pan/IBluetoothPanAction.aidl 

#############################################
# End of file
#############################################
