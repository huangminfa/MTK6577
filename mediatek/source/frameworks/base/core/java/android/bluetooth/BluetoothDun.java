/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE. 
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/
/*******************************************************************************
 *
 * Filename:
 * ---------
 * BluetoothDun.java
 *
 * Project:
 * --------
 *   BT Project
 *
 * Description:
 * ------------
 *   This file is used to provide DUN service access api
 *
 * Author:
 * -------
 * Ting Zheng
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision: 
 * $Modtime:
 * $Log: 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/
package android.bluetooth;

import android.bluetooth.BluetoothProfileManager;
import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public final class BluetoothDun implements BluetoothProfileManager.BluetoothProfileBehavior
{
    private static final String TAG = "BluetoothDun";
    private static final boolean DBG = true;

    /**
     * Intent indicating timeout for user confirmation, which is sent to
     * BluetoothDunActivity
     */
    public static final String STATE_CHANGED_ACTION =
            BluetoothProfileManager.ACTION_STATE_CHANGED;
	

    /**
     * Intent indicating timeout for user confirmation, which is sent to
     * BluetoothDunActivity
     */
    public static final String EXTRA_STATE =
            BluetoothProfileManager.EXTRA_NEW_STATE;

    public static final String EXTRA_PRE_STATE =
            BluetoothProfileManager.EXTRA_PREVIOUS_STATE;
		

    public static final int STATE_DISCONNECTED = BluetoothProfileManager.STATE_DISCONNECTED;	
    public static final int STATE_CONNECTING = BluetoothProfileManager.STATE_CONNECTING; 
    public static final int STATE_CONNECTED = BluetoothProfileManager.STATE_CONNECTED;	
    public static final int STATE_DISCONNECTING = BluetoothProfileManager.STATE_DISCONNECTING;	


    private static IBluetoothDun mService;
    private final Context mContext;


    public BluetoothDun(Context context)
    {
        mContext = context;
        if (!context.bindService(new Intent(IBluetoothDun.class.getName()), mConnection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Could not bind to Bluetooth Dun Service");
        }
    }

    protected void finalize() throws Throwable 
    {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Close the connection to the backing service.
     * Other public functions of BluetoothDun will return default error
     * results once close() has been called. Multiple invocations of close()
     * are ok.
     */
    public synchronized void close() 
    {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
            mConnection = null;
        }
    }

    public Set<BluetoothDevice> getConnectedDevices()
    {
        if (mService != null) {
            HashSet<BluetoothDevice> devices = new HashSet<BluetoothDevice>();
            BluetoothDevice connDev = null;			
            try {			
                connDev = mService.dunGetConnectedDevice();	
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
            if (connDev != null)
            {
                devices.add(connDev);
                return devices;				
            }
        } else {
            Log.w(TAG, "getConnectedDevices error: not attached to DUN service");
        }
        return null;		
    }

    public boolean connect(BluetoothDevice device)
    {
        return false;
    }

    public boolean disconnect(BluetoothDevice device)
    {
        if (mService != null) {
            try {
                mService.dunDisconnect();			
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "disconnect error: not attached to DUN service");
        }
        return false;		
    }

    public int getState(BluetoothDevice device)
    {
        if (mService != null) {
            try {
                Set<BluetoothDevice> remoteDevices = null;
				
                remoteDevices = getConnectedDevices();

                if (device == null || remoteDevices == null || !remoteDevices.contains(device)) {
                    return BluetoothProfileManager.STATE_DISCONNECTED;
                }
                return mService.dunGetState();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "get state error: not attached to DUN service");
        }
        return BluetoothProfileManager.STATE_UNKNOWN;		
    }

    public void setBluetoothTethering(boolean value) {
        if (DBG) log("setBluetoothTethering(" + value + ")");
        if (mService == null) {
            Log.d(TAG, "Service is not ready");
            return;
        }
        try {
            mService.setBluetoothTethering(value);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
        }
    }

    public boolean isTetheringOn() {
        if (mService == null) {
            Log.d(TAG, "Service is not ready");
            return false;
        }
        try {
            return mService.isTetheringOn();
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() 
    {
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            if (DBG) log("Proxy object connected");
            mService = IBluetoothDun.Stub.asInterface(service);
        }
        public void onServiceDisconnected(ComponentName className) {
            if (DBG) log("Proxy object disconnected");
            mService = null;
        }
    };

    private static void log(String msg)
    {
        Log.d(TAG, msg);
    }

}
