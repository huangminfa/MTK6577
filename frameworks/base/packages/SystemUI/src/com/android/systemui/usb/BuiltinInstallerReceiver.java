package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import android.hardware.usb.UsbManager;
import android.os.storage.IMountService;
import android.os.RemoteException;
import android.util.Log;
// This class is used to close BuiltInstallerActivity and ims service unMount if it is mounted
public class BuiltinInstallerReceiver extends BroadcastReceiver {
    private static final String TAG = "BuiltinInstallerReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_STATE.equals(action)) {
            boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
            if (!connected && BuiltinInstallerActivity.getSharedStatus()) {
                try {
                        IMountService ims = BuiltinInstallerActivity.getMountService();
                        if (ims == null) {
                           Log.e(TAG, "Cant get mount service");
                        }
                        ims.shareCDRom(false);
                        BuiltinInstallerActivity.setSharedStatus(false);
                        Log.i(TAG, "STOP sharing builtin installer");
                    } catch (RemoteException e) {
                        Log.e(TAG, "Cant call mount service");
                    }
            }
        }
    }
}    
