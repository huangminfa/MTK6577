#############################################
# Build Java Package
#############################################

$(info [BlueAngel][MAP]: $(call my-dir))

# copy resource to res
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# add aidl include path
LOCAL_AIDL_INCLUDES += $(call my-dir)/src

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/map/src) \
	${MY_BUILD_PREFIX}/profiles/map/src/com/mediatek/bluetooth/map/IBluetoothMapSettingCallback.aidl \
	${MY_BUILD_PREFIX}/profiles/map/src/com/mediatek/bluetooth/map/IBluetoothMapSetting.aidl 
LOCAL_JAVA_LIBRARIES += mediatek-framework
#############################################
# End of file
#############################################
