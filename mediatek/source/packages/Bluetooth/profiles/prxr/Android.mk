#############################################
# Build Java Package
#############################################

$(info [BlueAngel][PRXR]: $(call my-dir))

# copy resource to build folder
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# add aidl include path
LOCAL_AIDL_INCLUDES += $(call my-dir)/src

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/prxr/src)
	
#	${MY_BUILD_PREFIX}/profiles/prxr/src/com/mediatek/bluetooth/service/IBluetoothPrxr.aidl

#############################################
# End of file
#############################################
