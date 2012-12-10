/*
 * Copyright (C) 2007 Google Inc.
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

package com.android.systemui.usb;

import com.android.systemui.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.storage.IMountService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.util.Log;

import java.util.List;

/**
 * This activity is shown to the user for him/her to enable USB CD-Rom for built-in installer
 * on-demand (that is, when the USB cable is connected). It uses the alert
 * dialog style. It will be launched from usb service.
 */
public class BuiltinInstallerActivity extends Activity
        implements  OnCheckedChangeListener,OnDismissListener {
    private static final String TAG = "BuiltinInstallerActivity";

    private boolean mDestroyed;
    private boolean mDontShow;
    static public boolean mShared = false;
    private static final int DLG_MOUNT_BICR = 1;

    // UI thread
    private Handler mUIHandler;
    
    public static boolean getSharedStatus(){
        return mShared;
    }
    
    public static void setSharedStatus(boolean state){
        mShared = state;
    }
    /** Used to detect when the USB cable is unplugged, so we can call finish() */
    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_STATE)) {
                Log.d(TAG,"intent="+intent);
                handleUsbStateChanged(intent);
            }
        }
    };

    public static IMountService getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);        
        mUIHandler = new Handler();
        scheduleShowDialog(DLG_MOUNT_BICR);
        registerReceiver(mUsbStateReceiver, new IntentFilter(UsbManager.ACTION_USB_STATE));        
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbStateReceiver);        
        mDestroyed = true;
    }




    private void handleUsbStateChanged(Intent intent) {
        boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
        if (!connected) {
            // It was disconnected from the plug, so finish
            Log.d(TAG, "USB Disconnect! close UI mShared=" + mShared);

            /*If already shared, call mount service to unshare
             * Change : this is moved to receiver since the activity may be destoryed here.
            */

            /*turn off the UI*/
            finish();

        } else {
            Log.d(TAG, "USB Connect!");
        }
    }

    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case DLG_MOUNT_BICR:

                Builder builder = new Builder(this);
                View view = getLayoutInflater().inflate(R.layout.builtin_installer, null);
                TextView textView = (TextView) view.findViewById(R.id.dlg_message);
                textView.setText(R.string.builtin_installer_dlg_message);

                CompoundButton checkBox = (CompoundButton) view.findViewById(R.id.always_allowed);
                checkBox.setOnCheckedChangeListener(this);
                builder.setView(view);

                builder.setTitle(R.string.builtin_installer_title_message)
                    .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Click OK, mDontShow+" + mDontShow);

                            try {
                                IMountService ims = getMountService();
                                if (ims == null) {
                                    // Display error dialog
                                    Log.e(TAG, "Cant get mount service");
                                }
                                ims.shareCDRom(true);
                                mShared = true;
                                Log.i(TAG, "Share builtin installer");
                            } catch (RemoteException e) {
                                Log.e(TAG, "Cant call mount service");
                            }
                        }
                    })
                    .setNegativeButton(R.string.dlg_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Click CANCEL, do nothing!!! mDontShow=" + mDontShow);
                        }
                    });
                Dialog dlg = builder.create();
                dlg.setOnDismissListener(this);
                return dlg;
        }
        return null;
    }

    private void scheduleShowDialog(final int id) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mDestroyed) {
                    removeDialog(id);
                    showDialog(id);
                }
            }
        });
    }


    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        mDontShow = isChecked;
        Log.d(TAG, "mDontShow+" + mDontShow);
    }
    
    public void onDismiss(DialogInterface dialog){
        Log.d(TAG, "onDismiss");
        if(mDontShow) {
            SystemProperties.set("sys.usb.mtk_bicr_support","yes_hide");
        }        
        finish();        
    }    
}
