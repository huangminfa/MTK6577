#############################################
# Build Java Package
#############################################

$(info [BlueAngel][PBAP]: $(call my-dir))

# copy resource to res
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/profiles/pbap/src) \

LOCAL_JAVA_LIBRARIES += javax.obex
LOCAL_STATIC_JAVA_LIBRARIES += com.android.vcard
#############################################
# End of file
#############################################
