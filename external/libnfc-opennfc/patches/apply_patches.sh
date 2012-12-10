#!/bin/sh
PATCHES_PATH=$ANDROID_BUILD_TOP/external/libnfc-opennfc/patches

echo "applying build.patch..."
cd $ANDROID_BUILD_TOP/build
git apply $PATCHES_PATH/build.patch
echo "applied build.patch\n"

echo "applying external_libnfc-nxp.patch..."
cd $ANDROID_BUILD_TOP/external/libnfc-nxp
git apply $PATCHES_PATH/external_libnfc-nxp.patch
echo "applied external_libnfc-nxp.patch\n"

echo "applying frameworks_base.patch..."
cd $ANDROID_BUILD_TOP/frameworks/base
git apply $PATCHES_PATH/frameworks_base.patch
echo "applied frameworks_base.patch\n"

echo "applying packages_apps_Nfc.patch..."
cd $ANDROID_BUILD_TOP/packages/apps/Nfc
git apply $PATCHES_PATH/packages_apps_Nfc.patch
echo "applied packages_apps_Nfc.patch\n"

echo "applying system_core.patch..."
cd $ANDROID_BUILD_TOP/system/core
git apply $PATCHES_PATH/system_core.patch
echo "applied system_core.patch"
