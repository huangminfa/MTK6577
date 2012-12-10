#############################################
# Build Java Package
#############################################

$(info [BlueAngel][BIP]: $(call my-dir))

# copy resource to res
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# add aidl include path
LOCAL_AIDL_INCLUDES += $(call my-dir)/src

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/bip/src) 

#############################################
# End of file
#############################################
