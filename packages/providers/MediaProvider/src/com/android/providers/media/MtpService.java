/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.providers.media;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDatabase;
import android.mtp.MtpServer;
import android.mtp.MtpStorage;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

//Added for USB Develpment debug, more log for more debuging help
import com.mediatek.xlog.SXlog;
import java.lang.Integer;
//Added for USB Develpment debug, more log for more debuging help

//Added Modification for ALPS00255822, bug from WHQL test
import android.provider.MediaStore;
import android.os.UEventObserver;
//Added Modification for ALPS00255822, bug from WHQL test

//Add for update Storage
import com.mediatek.featureoption.FeatureOption;
//Add for update Storage

public class MtpService extends Service {
    private static final String TAG = "MtpService";

    // We restrict PTP to these subdirectories
    private static final String[] PTP_DIRECTORIES = new String[] {
        Environment.DIRECTORY_DCIM,
        Environment.DIRECTORY_PICTURES,
    };

	//Added Modification for ALPS00255822, bug from WHQL test
    private static final String MTP_OPERATION_DEV_PATH =
			"DEVPATH=/devices/virtual/misc/mtp_usb";
	//Added Modification for ALPS00255822, bug from WHQL test

	//Add for update Storage
	private boolean 						  mIsSDExist = false;
	private static final String SD_EXIST   = "SD_EXIST";
	private static final String ACTION_DYNAMIC_SD_SWAP = "com.mediatek.SD_SWAP";
	//Add for update Storage
	
    private void addStorageDevicesLocked() {
        if (mPtpMode) {
            // In PTP mode we support only primary storage
            addStorageLocked(mStorageMap.get(mVolumes[0].getPath()));
        } else {
            for (MtpStorage storage : mStorageMap.values()) {
                addStorageLocked(storage);
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

			//ALPS00120037, add log for support MTP debugging
			Log.w(TAG, "ACTION_USER_PRESENT: BroadcastReceiver: onReceive: synchronized");
			//ALPS00120037, add log for support MTP debugging

            final String action = intent.getAction();
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
                synchronized (mBinder) {
					//Added Modification for ALPS00273682/ALPS00279547
					Log.w(TAG, "ACTION_USER_PRESENT: BroadcastReceiver: mMtpDisabled " + mMtpDisabled);
					//Added Modification for ALPS00273682/ALPS00279547
                    // Unhide the storage units when the user has unlocked the lockscreen
                    if (mMtpDisabled) {
                        addStorageDevicesLocked();
                        mMtpDisabled = false;
                    }
                }
            }
			//Added Modification for ALPS00273682/ALPS00279547
			if (!mMtpDisabled)
			{
				Log.w(TAG, "The KeyGuard unlock has been received, ");
				unregisterReceiver(mReceiver);
			}
			//Added Modification for ALPS00273682/ALPS00279547
        }
    };

    //Added for Storage Update
    private final BroadcastReceiver mReceiver_locale = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "ACTION_LOCALE_CHANGED: BroadcastReceiver: onReceive: synchronized");

            final String action = intent.getAction();
			if(Intent.ACTION_LOCALE_CHANGED .equals(action) && !mMtpDisabled)
			{
                synchronized (mBinder) {
					Log.w(TAG, "ACTION_LOCALE_CHANGED : BroadcastReceiver: onReceive: synchronized");
					/*for (MtpStorage storage : mStorageMap.values()) {
						updateStorageLocked(storage);*/
					
					StorageVolume[] volumes = mStorageManager.getVolumeList();
					mVolumes = volumes;

					for (int i = 0; i < mVolumes.length; i++) 
					{
						StorageVolume volume = mVolumes[i];
						updateStorageLocked(volume);
					}
               	}
			
			}
						
        }
    };
    private final BroadcastReceiver mReceiver_swapSD = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "ACTION_DYNAMIC_SD_SWAP: BroadcastReceiver: onReceive: synchronized");

            final String action = intent.getAction();
			boolean swapSD;
			if(ACTION_DYNAMIC_SD_SWAP.equals(action) && !mMtpDisabled)
			{
                synchronized (mBinder) {
					mIsSDExist = intent.getBooleanExtra(SD_EXIST, false);
					
					Log.w(TAG, "ACTION_DYNAMIC_SD_SWAP : BroadcastReceiver: swapSD = " + mIsSDExist);

					StorageVolume[] volumes = mStorageManager.getVolumeList();
					mVolumes = volumes;

					for (int i = 0; i < mVolumes.length; i++) 
					{
						StorageVolume volume = mVolumes[i];
						updateStorageLocked(volume);
					}
               	}
			
			}
						
        }
    };
    //Added for Storage Update

    private final StorageEventListener mStorageEventListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {

            synchronized (mBinder) {
			//ALPS00120037, add log for support MTP debugging
			Log.w(TAG, "onStorageStateChanged: onReceive: synchronized");
			//ALPS00120037, add log for support MTP debugging

                Log.d(TAG, "onStorageStateChanged " + path + " " + oldState + " -> " + newState);
                if (Environment.MEDIA_MOUNTED.equals(newState)) {
                    volumeMountedLocked(path);
                } else if (Environment.MEDIA_MOUNTED.equals(oldState)) {
                    MtpStorage storage = mStorageMap.remove(path);
                    if (storage != null) {
                        removeStorageLocked(storage);
                    }
                }
            }
        }
    };

    private MtpDatabase mDatabase;
    private MtpServer mServer;
    private StorageManager mStorageManager;
    private boolean mMtpDisabled; // true if MTP is disabled due to secure keyguard
    private boolean mPtpMode;
    private final HashMap<String, MtpStorage> mStorageMap = new HashMap<String, MtpStorage>();
    private StorageVolume[] mVolumes;

    @Override
    public void onCreate() {
        // lock MTP if the keyguard is locked and secure
        KeyguardManager keyguardManager =
                (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        mMtpDisabled = keyguardManager.isKeyguardLocked() && keyguardManager.isKeyguardSecure();
		
		//Added Modification for ALPS00273682/ALPS00279547
		Log.w(TAG, "onCreate: mMtpDisabled " + mMtpDisabled);

		if(mMtpDisabled)
		//Added Modification for ALPS00273682/ALPS00279547
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
		//Added for Storage Update
        registerReceiver(mReceiver_locale, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
    	if(FeatureOption.MTK_2SDCARD_SWAP)
        {
            registerReceiver(mReceiver_swapSD, new IntentFilter(ACTION_DYNAMIC_SD_SWAP));
        }
		//Added for Storage Update

        mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        synchronized (mBinder) {

            mStorageManager.registerListener(mStorageEventListener);
            StorageVolume[] volumes = mStorageManager.getVolumeList();
            mVolumes = volumes;
			//ALPS00241636, add log for support MTP debugging
            SXlog.d(TAG, "onCreate: volumes.length=" + volumes.length);
			//ALPS00241636, add log for support MTP debugging
            for (int i = 0; i < volumes.length; i++) {
                String path = volumes[i].getPath();
                String state = mStorageManager.getVolumeState(path);
				//ALPS00241636, add log for support MTP debugging
                SXlog.d(TAG, "onCreate: path of volumes["+i+"]=" + path);
                SXlog.d(TAG, "onCreate: state of volumes["+i+"]=" + state);
				//ALPS00241636, add log for support MTP debugging
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                   volumeMountedLocked(path);
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (mBinder) {
			//ALPS00120037, add log for support MTP debugging
			Log.w(TAG, "onStartCommand: synchronized");
			//ALPS00120037, add log for support MTP debugging
            if (mServer == null) {
                mPtpMode = (intent == null ? false
                        : intent.getBooleanExtra(UsbManager.USB_FUNCTION_PTP, false));
                Log.d(TAG, "starting MTP server in " + (mPtpMode ? "PTP mode" : "MTP mode"));
                String[] subdirs = null;
                if (mPtpMode) {
                    int count = PTP_DIRECTORIES.length;
                    subdirs = new String[count];
                    for (int i = 0; i < count; i++) {
                        File file =
                                Environment.getExternalStoragePublicDirectory(PTP_DIRECTORIES[i]);
                        // make sure this directory exists
                        file.mkdirs();
                        subdirs[i] = file.getPath();
						//ALPS00279419, add log for support MTP debugging
						SXlog.d(TAG, "starting MTP server in " + (mPtpMode ? "PTP mode" : "MTP mode") + ", Add subdirs[" + i + "] = "+ subdirs[i] + " in to MtpDatabase");
						//ALPS00279419, add log for support MTP debugging
                    }
                }
				//ALPS00279419, add log for support MTP debugging
				SXlog.d(TAG, "starting MTP server in " + (mPtpMode ? "PTP mode" : "MTP mode") + ", Add volume = " + mVolumes[0].getPath() + " in to MtpDatabase");
				//ALPS00279419, add log for support MTP debugging

                mDatabase = new MtpDatabase(this, MediaProvider.EXTERNAL_VOLUME,
                        mVolumes[0].getPath(), subdirs);
                mServer = new MtpServer(mDatabase, mPtpMode);
                if (!mMtpDisabled) {
                    addStorageDevicesLocked();
                }
                mServer.start();
				//Added Modification for ALPS00255822, bug from WHQL test
				//mUEventObserver.startObserving(MTP_OPERATION_PATH);
				mUEventObserver.startObserving(MTP_OPERATION_DEV_PATH);
				//Added Modification for ALPS00255822, bug from WHQL test
				

            }
			//ALPS00120037, renew the MtpServer if the thread run is unloaded
			else
			{		
				Log.w(TAG, "onStartCommand: synchronized, mServer is not null!!");
				
				if(mServer.getStatus())	
				{
					Log.w(TAG, "onStartCommand: synchronized, mServer is not null but has been Endup!!");
					Log.w(TAG, "onStartCommand: synchronized, delete this one, wait for next startcommand");

					mPtpMode = (intent == null ? false
							: intent.getBooleanExtra(UsbManager.USB_FUNCTION_PTP, false));
					Log.d(TAG, "starting MTP server in " + (mPtpMode ? "PTP mode" : "MTP mode"));
					String[] subdirs = null;
					if (mPtpMode) {
						int count = PTP_DIRECTORIES.length;
						subdirs = new String[count];
						for (int i = 0; i < count; i++) {
							File file =
									Environment.getExternalStoragePublicDirectory(PTP_DIRECTORIES[i]);
							// make sure this directory exists
							file.mkdirs();
							subdirs[i] = file.getPath();
							//ALPS00279419, add log for support MTP debugging
							SXlog.d(TAG, "starting MTP server in " + (mPtpMode ? "PTP mode" : "MTP mode") + ", Add subdirs[ = " + i + "] = "+ subdirs[i] + " in to MtpDatabase");
							//ALPS00279419, add log for support MTP debugging
						}
					}
					//ALPS00279419, add log for support MTP debugging
					SXlog.d(TAG, "starting MTP server in " + (mPtpMode ? "PTP mode" : "MTP mode") + ", Add volume = " + mVolumes[0].getPath() + " in to MtpDatabase");
					//ALPS00279419, add log for support MTP debugging
					mDatabase = new MtpDatabase(this, MediaProvider.EXTERNAL_VOLUME,
							mVolumes[0].getPath(), subdirs);

					mServer = new MtpServer(mDatabase, mPtpMode);
					
					if (!mMtpDisabled) {
						addStorageDevicesLocked();
					}
					
					mServer.start();
					//Added Modification for ALPS00255822, bug from WHQL test
					//mUEventObserver.startObserving(MTP_OPERATION_PATH);
					SXlog.d(TAG, "renew the uevent observer");
					
					mUEventObserver.startObserving(MTP_OPERATION_DEV_PATH);
					//Added Modification for ALPS00255822, bug from WHQL test
					
				}
				else
					Log.w(TAG, "onStartCommand: synchronized, mServer is not null and run well!!");
			}
			//ALPS00120037, renew the MtpServer if the thread run is unloaded
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
		//Added Modification for ALPS00273682/ALPS00279547
		Log.w(TAG, "onDestroy: mMtpDisabled " + mMtpDisabled);
		if (mMtpDisabled)
		{
			//Added Modification for ALPS00273682/ALPS00279547
        	unregisterReceiver(mReceiver);
			//Added Modification for ALPS00273682/ALPS00279547
		}
		//Added Modification for ALPS00273682/ALPS00279547
        mStorageManager.unregisterListener(mStorageEventListener);
	//Added for Storage Update
        unregisterReceiver(mReceiver_locale);
    	if(FeatureOption.MTK_2SDCARD_SWAP)
        {
            unregisterReceiver(mReceiver_swapSD);
        }
	//Added for Storage Update
    }

    private final IMtpService.Stub mBinder =
            new IMtpService.Stub() {
        public void sendObjectAdded(int objectHandle) {
            synchronized (mBinder) {
			//ALPS00120037, add log for support MTP debugging
			//Log.w(TAG, "mBinder: sendObjectAdded!!");
			//ALPS00120037, add log for support MTP debugging
                if (mServer != null) {
                    mServer.sendObjectAdded(objectHandle);
                }
            }
        }

        public void sendObjectRemoved(int objectHandle) {
            synchronized (mBinder) {
			//ALPS00120037, add log for support MTP debugging
			//Log.w(TAG, "mBinder: sendObjectRemoved!!");
			//ALPS00120037, add log for support MTP debugging
                if (mServer != null) {
                    mServer.sendObjectRemoved(objectHandle);
                }
            }
        }

		//ALPS00289309, update Object
		public void sendObjectInfoChanged(int objectHandle) {
			synchronized (mBinder) {
			Log.w(TAG, "mBinder: sendObjectInfoChanged, objectHandle = 0x" + Integer.toHexString(objectHandle));
				if (mServer != null) {
					mServer.sendObjectInfoChanged(objectHandle);
				}
			}
		}
		//ALPS00289309, update Object
	
		//Added for Storage Update
        public void sendStorageInfoChanged(MtpStorage storage) {
            synchronized (mBinder) {
                Log.w(TAG, "mBinder: sendObjectInfoChanged, storage.getStorageId = 0x" + Integer.toHexString(storage.getStorageId()));
                if (mServer != null) {
                    mServer.sendStorageInfoChanged(storage);
                }
            }
        }
		//Added for Storage Update

    };

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    private void volumeMountedLocked(String path) {
	        //Add for update Storage
		StorageVolume[] volumes = mStorageManager.getVolumeList();
		mVolumes = volumes; 	
	        //Add for update Storage
		
        for (int i = 0; i < mVolumes.length; i++) {
            StorageVolume volume = mVolumes[i];
            if (volume.getPath().equals(path)) {
                int storageId = MtpStorage.getStorageId(i);
                long reserveSpace = volume.getMtpReserveSpace() * 1024 * 1024;

				//ALPS00241636, add log for support MTP debugging
                SXlog.d(TAG, "volumeMountedLocked: storageId of volumes["+i+"]=" + Integer.toHexString(storageId));
                SXlog.d(TAG, "volumeMountedLocked: reserveSpace of volumes["+i+"]=" + reserveSpace);
				//ALPS00241636, add log for support MTP debugging

                MtpStorage storage = new MtpStorage(volume);
                mStorageMap.put(path, storage);
                if (!mMtpDisabled) {
                    // In PTP mode we support only primary storage
                    if (i == 0 || !mPtpMode) {
                        addStorageLocked(storage);
                    }
                }
                break;
            }
        }
    }

    private void addStorageLocked(MtpStorage storage) {
        if(storage == null) {
            Log.w(TAG, "addStorageLocked: Null storage!");
            return;
        }
        Log.d(TAG, "addStorageLocked " + storage.getStorageId() + " " + storage.getPath());
        if (mDatabase != null) {
			//ALPS00241636, add log for support MTP debugging
            SXlog.d(TAG, "addStorageLocked: add storage "+storage.getPath()+" into MtpDatabase");
			//ALPS00241636, add log for support MTP debugging
            mDatabase.addStorage(storage);
        }
        if (mServer != null) {
			//ALPS00241636, add log for support MTP debugging
            SXlog.d(TAG, "addStorageLocked: add storage "+storage.getPath()+" into MtpServer");
			//ALPS00241636, add log for support MTP debugging
            mServer.addStorage(storage);
        }
    }
	//Added for Storage Update
    private void updateStorageLocked(StorageVolume volume) {
        MtpStorage storage = new MtpStorage(volume);
        //String path = storage.getPath();
        //boolean desSet;
        
        Log.d(TAG, "updateStorageLocked " + storage.getStorageId() + " = " +storage.getStorageId());
        /*if (mDatabase != null) {
            //ALPS00241636, add log for support MTP debugging
            SXlog.d(TAG, "addStorageLocked: add storage "+storage.getPath()+" into MtpDatabase");
            //ALPS00241636, add log for support MTP debugging
            mDatabase.addStorage(storage);
        }*/
        if (mServer != null) {
            //ALPS00241636, add log for support MTP debugging
            SXlog.d(TAG, "updateStorageLocked: updateStorageLocked storage "+storage.getPath()+" into MtpServer");
            //ALPS00241636, add log for support MTP debugging
            mServer.updateStorage(storage);
        }
    }
	//Added for Storage Update

    private void removeStorageLocked(MtpStorage storage) {
        Log.d(TAG, "removeStorageLocked " + storage.getStorageId() + " " + storage.getPath());
        if (mDatabase != null) {
            mDatabase.removeStorage(storage);
        }
        if (mServer != null) {
            mServer.removeStorage(storage);
        }
    }

		
	//Added Modification for ALPS00255822, bug from WHQL test
	private final UEventObserver mUEventObserver = new UEventObserver() {
		@Override
		public void onUEvent(UEventObserver.UEvent event) {
			Log.w(TAG, "USB UEVENT: " + event.toString());

			String mtp = event.get("MTP");

			if (mtp != null)
			{						
				Log.w(TAG, "mMtpSessionEnd: end the session");
				mServer.endSession();
			}
			else
				Log.w(TAG, "Not MTP string");
				
		}
	};
	//Added Modification for ALPS00255822, bug from WHQL test
}

