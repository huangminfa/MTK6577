#############################################
# Build Java Package
#############################################

$(info [BlueAngel][BT40]: $(call my-dir))

# copy resource to build folder
$(shell cp -ar $(call my-dir)/res/* $(MY_BUILD_PATH)/res)

# include java source and aidl files
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/common/bt40/src)

# include test code
ifeq ($(LOCAL_MODULE_TAGS), eng)
LOCAL_SRC_FILES += \
	$(call all-java-files-under, ${MY_BUILD_PREFIX}/common/bt40/dev)
endif

#############################################
# End of file
#############################################
