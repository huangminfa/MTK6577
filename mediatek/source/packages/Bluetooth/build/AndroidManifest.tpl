<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	package="com.mediatek.bluetooth" android:sharedUserId="android.uid.mtkbt">

	<application android:label="@string/app_label" 
		android:persistent="true"
		android:icon="@drawable/bluetooth"
		android:description="@string/app_description">
		<!-- BLUEANGEL::PLACEHOLDER -->

	</application>

	<!-- permission -->
	<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
	<uses-permission android:name="android.permission.BLUETOOTH"/>
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
	<uses-permission android:name="android.permission.READ_CONTACTS"/>
	<uses-permission android:name="android.permission.WAKE_LOCK"/>
	<uses-permission android:name="android.permission.WRITE_CONTACTS"/>
	<uses-permission android:name="android.permission.READ_PHONE_STATE" />	
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
	<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
	<uses-permission android:name="android.permission.VIBRATE"/>
	<uses-permission android:name="android.permission.FLASHLIGHT"/>
	<uses-permission android:name="com.android.email.permission.ACCESS_PROVIDER"/>
	<uses-permission android:name="com.android.email.permission.ACCESS_PROVIDER"/>
	<uses-permission android:name="android.permission.READ_SMS"/>
	<uses-permission android:name="android.permission.WRITE_SMS"/>
	<uses-permission android:name="android.permission.SEND_SMS" />
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.WRITE_WAPPUSH" />
	<uses-permission android:name="android.permission.READ_WAPPUSH" />
	<uses-permission android:name="android.permission.READ_PROFILE" />
	<uses-permission android:name="android.permission.CHANGE_CONFIGURATION" />
</manifest> 