ifeq (yes,$(MTK_BT_SUPPORT))
LOCAL_SRC_FILES := $(filter-out core/java/android/bluetooth/BluetoothAudioGateway.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out core/java/android/bluetooth/BluetoothHeadset.java, $(LOCAL_SRC_FILES))
# LOCAL_SRC_FILES := $(filter-out core/java/android/bluetooth/BluetoothA2dp.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out core/java/android/bluetooth/BluetoothPan.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out core/java/android/bluetooth/BluetoothPbap.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out core/java/android/bluetooth/BluetoothSocket.java, $(LOCAL_SRC_FILES))
else 
LOCAL_SRC_FILES := $(filter-out ../../mediatek/source/frameworks/base/core/java/android/bluetooth/BluetoothAudioGateway.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out ../../mediatek/source/frameworks/base/core/java/android/bluetooth/BluetoothHeadset.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out ../../mediatek/source/frameworks/base/core/java/android/bluetooth/BluetoothPbap.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out ../../mediatek/source/frameworks/base/core/java/android/bluetooth/BluetoothSocket.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out ../../mediatek/source/frameworks/base/core/java/android/bluetooth/BluetoothPan.java, $(LOCAL_SRC_FILES))
endif

LOCAL_SRC_FILES += \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothProfileManager.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothSocket.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothBipi.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothBipr.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothBpp.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothDun.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothFtpCtrl.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothFtpServer.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothFtpServerCallback.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothSimap.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothSimapCallback.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothMap.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothOpp.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothHid.aidl \
				../../mediatek/source/frameworks/base/core/java/android/bluetooth/IBluetoothPan.aidl \
				../../mediatek/source/frameworks/base/core/java/com/mediatek/bluetooth/service/IBluetoothPrxm.aidl \
				../../mediatek/source/frameworks/base/core/java/com/mediatek/bluetooth/service/IBluetoothPrxr.aidl \
				../../mediatek/source/frameworks/base/core/java/android/nfc/INfcSecureElement.aidl \
				
aidl_files += \
	../../mediatek/source/frameworks/base/core/java/com/mediatek/bluetooth/service/BluetoothPrxmDevice.aidl \
	../../mediatek/source/frameworks/base/core/java/com/mediatek/bluetooth/service/BluetoothSocket.aidl \

