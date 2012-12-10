/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.bluetooth.hid;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


//import com.mediatek.bluetooth.BluetoothProfile;
import com.mediatek.bluetooth.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHid;
import android.bluetooth.IBluetooth;
import android.bluetooth.BluetoothProfileManager;
import android.bluetooth.BluetoothProfileManager.Profile;
import android.bluetooth.IBluetoothHid;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothProfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.widget.Toast;

import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

/**
 * @author MTK80684
 *
 */
public class BluetoothHidService extends Service{

	/**
	 * 
	 */
	 /* Native data */
    	private int mNativeData;
	
	static {
	   System.loadLibrary("exthid_jni");
	   }
	
	   /* Native functions */
	   private native void cleanServiceNative();
	   private native void forceClearServerNative();
	   private native boolean initServiceNative();
	   private native boolean listentoSocketNative();
	   private native void wakeupListenerNative();
	   private native void stopListentoSocketNative();
	   /* Native functions for HID Server */
	   private native void serverAuthorizeReqNative(String BT_Addr, boolean result);
    	private native void serverActivateReqNative();
    	private native void serverDeactivateReqNative();
    	private native void serverConnectReqNative(String BT_Addr);
    	private native void serverDisconnectReqNative(String BT_Addr);

	private native void serverUnplugReqNative(String BT_Addr);
	private native void serverSendReportReqNative(String BT_Addr);
	private native void serverSetReportReqNative(String BT_Addr);
	private native void serverGetReportReqNative(String BT_Addr);
	private native void serverSetProtocolReqNative(String BT_Addr);
	private native void serverGetProtocolReqNative(String BT_Addr);
	private native void serverSetIdleReqNative(String BT_Addr);
	private native void serverGetIdleReqNative(String BT_Addr);

	private static final String TAG = "[BT][HID][BluetoothHidService]";
	private static final String BT_HID_SETTING_INFO="BT_HID_SETTING_INFO";
	private static final String BT_HID_NOT_FOUNT="BT_HID_NOT_FOUNT";
	Context cx=this;
    /* Notification manager service */
    private NotificationManager mNM = null;
	/* Flag for debug messages */
	private static final boolean DEBUG = true;

    /* Start of HID ID space */
    private static final int HID_ID_START = 10;    //BluetoothProfile.getProfileStart(BluetoothProfile.ID_HID);
	public static final String FINISH_ACTION =  "com.mediatek.bluetooth.hid.finish";

    /* HID Server Notification IDs */
    private static int hid_connect_notify	= HID_ID_START + 1;
	/* Server state */
	Map<String,String> stateMap=new HashMap<String,String>();
	Map notifyMap=new HashMap();
    private int mServerState;
	Preference mPreference=null;
	PreferenceCategory pc ;
	private static boolean	service_disable;
	private IBluetooth mBluetoothService;
	
	Intent update_state_intent = new Intent(BluetoothProfileManager.ACTION_PROFILE_STATE_UPDATE);

	private class SocketListenerThread extends Thread {
		public boolean stopped;
		@Override
		public void run() {
		    while (!stopped) {
				if (!listentoSocketNative()) {
				    stopped = true;
				}
		    }
		    if(DEBUG)
		    	Xlog.d(TAG, "SocketListener stopped.");
		}
		public void shutdown() {
			// TODO Auto-generated method stub
			stopped = true;
           	wakeupListenerNative();
			
		}
	}
	
	private class actionTimeoutThread extends Thread {
		public String BT_Addr;
		public String state;
		private	boolean	stoped = false;
		@Override
		public void run() {
			actionTimeout(BT_Addr, state);
		}
		
		public void shutdown(){
			stoped=true;
		}
		private void actionTimeout(String BT_Addr, String state){
	    	boolean timeout = false;
			int cnt = 0;
			if(!stateMap.containsKey(BT_Addr)){
				if(DEBUG)
					Xlog.e(TAG,"ERROR: stateMap not contain "+BT_Addr);
				return;
			}
				
			
			while (!stateMap.get(BT_Addr).equals(state)&& !stoped) {
				if (cnt >= 60000) {
				    timeout = true;
				    break;
				}

				try {
				    Thread.sleep(100);
				} catch (Exception e) {
					if(DEBUG)
						Xlog.e(TAG, "Waiting for action was interrupted.");
				}
				cnt += 100;		    
			}

			if (timeout) {	    
				if(DEBUG)Xlog.w(TAG, "Waiting action time-out. Force return.");
			    if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
			    	serverDisconnectReqNative(BT_Addr);
			    	sendServiceMsg(BluetoothHid.MBTEVT_HID_HOST_CONNECT_FAIL, BT_Addr);
			    }
			    else if(state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT)){
			    	sendServiceMsg(BluetoothHid.MBTEVT_HID_HOST_DISCONNECT_FAIL, BT_Addr);
			    }
			}
	    }		
		
	}

    /* A thread that keep listening to the socket for incoming ILM */
    private SocketListenerThread mSocketListener = null;
    private actionTimeoutThread mConnectTimeout = null;
    private actionTimeoutThread mDisconnectTimeout = null; 
    
   
    private void connectHidDevice(String BT_Addr){
    	if(mConnectTimeout == null)
    		mConnectTimeout=new actionTimeoutThread();
    	mConnectTimeout.setName("hidConnectTimeoutThread");
    	mConnectTimeout.BT_Addr=BT_Addr;
    	mConnectTimeout.state=BluetoothHid.BT_HID_DEVICE_CONNECT;
    	mServerState=BluetoothHid.BT_HID_STATE_CONNECTING;
	    //stateMap.remove(BT_Addr);
	    //stateMap.put(BT_Addr, BluetoothHid.BT_HID_DEVICE_CONNECTING);
	    /*
	    pc=BluetoothHidActivity.getDeviceList();
	    if(pc!=null){
	    	mPreference=pc.findPreference(BT_Addr);
	 	    
	 	    if(mPreference!=null){
	 	    	mPreference.setSummary(R.string.bluetooth_hid_summary_connecting);
	 	    	mPreference.setEnabled(false);
	 	    }	
	    }   
	    */
	    updateActivityUI(BT_Addr,R.string.bluetooth_hid_summary_connecting,false);
	    updateSettingsState(BluetoothHid.BT_HID_DEVICE_CONNECTING, getBluetoothDevice(BT_Addr));
    	    updateDeviceState(BT_Addr, BluetoothHid.BT_HID_DEVICE_CONNECTING);
	    serverConnectReqNative(BT_Addr);
	    if(!mConnectTimeout.isAlive())
	    	mConnectTimeout.start();
    }
    
    private void disconnectHidDevice(String BT_Addr){
    	if(stateMap.containsKey(BT_Addr)){
    		if(stateMap.get(BT_Addr).equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
		    	if(mDisconnectTimeout == null)
		    		mDisconnectTimeout=new actionTimeoutThread();
		    	mDisconnectTimeout.setName("hidDisconnectTimeoutThread");
		    	mDisconnectTimeout.BT_Addr=BT_Addr;
		    	mDisconnectTimeout.state=BluetoothHid.BT_HID_DEVICE_DISCONNECT;
		    	mServerState=BluetoothHid.BT_HID_STATE_DISCONNECTING;
			    //stateMap.remove(BT_Addr);
			    //stateMap.put(BT_Addr, BluetoothHid.BT_HID_DEVICE_DISCONNECTING);
			    /*
			    pc=BluetoothHidActivity.getDeviceList();
			    if(pc!=null){
			    	mPreference=pc.findPreference(BT_Addr);
				    
				    if(mPreference!=null){
					    mPreference.setSummary(R.string.bluetooth_hid_summary_disconnecting);
					    mPreference.setEnabled(false);	
				    }	
			    }   */
			   
			    
				updateActivityUI(BT_Addr,R.string.bluetooth_hid_summary_disconnecting,false);
			    updateSettingsState(BluetoothHid.BT_HID_DEVICE_DISCONNECTING, getBluetoothDevice(BT_Addr));
		    	    updateDeviceState(BT_Addr, BluetoothHid.BT_HID_DEVICE_DISCONNECTING);
			    serverDisconnectReqNative(BT_Addr);
			    if(!mDisconnectTimeout.isAlive())
			    	mDisconnectTimeout.start();
    		}
    		else
    			if(DEBUG)Xlog.e(TAG, "error state to disconnect");
    	}

    }
    /********************************************************************************************
     * Binder Interface Objects Definitionas and Binder Callbacks
     ********************************************************************************************/

        /* The binder object for launching HID client and requesting connection status. */
        private final IBluetoothHid.Stub mHid = new IBluetoothHid.Stub() {
			public void connect(BluetoothDevice device) throws RemoteException {
				// TODO Auto-generated method stub
				String BT_Addr=device.getAddress();
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer Connect");

				String state = getDeviceState(BT_Addr);
				if(state != null){
					if(!state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT))
						connectHidDevice(BT_Addr);
					else
						if(DEBUG)Xlog.d(TAG, "already connected");
				}
				else					
					connectHidDevice(BT_Addr);
			}
	
			public void disconnect(BluetoothDevice device) throws RemoteException {
				// TODO Auto-generated method stub
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer Disconnect");
				String BT_Addr=device.getAddress();

				disconnectHidDevice(BT_Addr);
			}			

			public int getState(BluetoothDevice device) throws RemoteException {
				// TODO Auto-generated method stub
				if(stateMap.isEmpty())
					//return BluetoothProfileManager.STATE_DISCONNECTED;
					return BluetoothInputDevice.STATE_DISCONNECTED;
				if(stateMap.containsKey(device.getAddress())){
					String tmpStr=stateMap.get(device.getAddress()).toString();
					
					if(tmpStr.equals(BluetoothHid.BT_HID_DEVICE_CONNECTING) 
							|| tmpStr.equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE))
						//return BluetoothProfileManager.STATE_CONNECTING;
						return BluetoothInputDevice.STATE_CONNECTING;
					else if(tmpStr.equals(BluetoothHid.BT_HID_DEVICE_CONNECT))
						//return BluetoothProfileManager.STATE_CONNECTED;
						return BluetoothInputDevice.STATE_CONNECTED;
					else if(tmpStr.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT))
						//return BluetoothProfileManager.STATE_DISCONNECTED;
						return BluetoothInputDevice.STATE_DISCONNECTED;
					else if(tmpStr.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECTING)
							|| tmpStr.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG))
						//return BluetoothProfileManager.STATE_DISCONNECTING;
						return BluetoothInputDevice.STATE_DISCONNECTING;
				}						
				 
				//return BluetoothProfileManager.STATE_DISCONNECTED;
				return BluetoothInputDevice.STATE_DISCONNECTED;
				
			}

			public BluetoothDevice[] getCurrentDevices()throws RemoteException {
				// TODO Auto-generated method stub
				if(DEBUG)Xlog.d(TAG, "getCurrentDevices");
			//	BluetoothDevice[] deviceList=new BluetoothDevice[5];
				Set<BluetoothDevice> deviceList=new HashSet<BluetoothDevice>();
				Set<BluetoothDevice> pairedDevices=BluetoothAdapter.getDefaultAdapter().getBondedDevices();

				if(pairedDevices != null){
					for(BluetoothDevice tmpDevice:pairedDevices){
						if(stateMap.containsKey(tmpDevice.getAddress()))
							if(stateMap.get(tmpDevice.getAddress()).equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
					//			deviceList[deviceIndex++]=tmpDevice;
								deviceList.add(tmpDevice);
							}
					}
				}
				
				//return deviceList;
				if(DEBUG)Xlog.v(TAG, "getCurrentDevices:deviceList.size="+deviceList.size()); 
				return deviceList.toArray(new BluetoothDevice[deviceList.size()]);
			}

        };
        /********************************************************************************************
         * Callback Functions for HID Profile Manager
         ********************************************************************************************/

            /* AIDL callback to Hid Profile Manager in Bluetooth Settings */
        private void updateSettingsState(String state, BluetoothDevice device) {
            int preState = convertStatusToInt(stateMap.get(device.getAddress()));
            int curState = convertStatusToInt(state);

            Intent tmpInt = new Intent(BluetoothInputDevice.ACTION_CONNECTION_STATE_CHANGED);
            tmpInt.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            tmpInt.putExtra(BluetoothInputDevice.EXTRA_PREVIOUS_STATE, preState);
            tmpInt.putExtra(BluetoothInputDevice.EXTRA_STATE, curState);

            if ((!state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT))
                    || (state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT) && !hasOtherConnectedHidDevice(device))) {
                sendBroadcast(tmpInt);
            }

            try {
                mBluetoothService.sendConnectionStateChange(device, BluetoothProfile.INPUT_DEVICE,
                        curState, preState);
            } catch (RemoteException e) {
                Log.e(TAG, "sendConnectionStateChange Exception: " + e);
            }

            if (DEBUG)
                Xlog.v(TAG, "updateSettingsState");
        }

        private int convertStatusToInt(String oriState) {
            if (null == oriState) {
                return BluetoothInputDevice.STATE_DISCONNECTED;
            }

            if (oriState.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)) {
                return BluetoothInputDevice.STATE_CONNECTED;
            } else if (oriState.equals(BluetoothHid.BT_HID_DEVICE_CONNECTING) 
                    || oriState.equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE)) {
                return BluetoothInputDevice.STATE_CONNECTING;
            } else if (oriState.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT)) {
                return BluetoothInputDevice.STATE_DISCONNECTED;
            } else if (oriState.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECTING) 
                    || oriState.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG)) {
                return BluetoothInputDevice.STATE_DISCONNECTING;
            }

            return BluetoothInputDevice.STATE_DISCONNECTED;
        }

        private boolean hasOtherConnectedHidDevice(BluetoothDevice exceptDevice) {
            boolean bResult = false;
            Set<String> c = stateMap.keySet();
            Iterator it = c.iterator();

            while (it.hasNext()) {
                String tmp = (String) it.next();
                if (stateMap.get(tmp) == null)
                    continue;
                if (stateMap.get(tmp).equals(BluetoothHid.BT_HID_DEVICE_CONNECT)
                        && !tmp.equals(exceptDevice.getAddress())) {
                    bResult = true;
                    break;
                }
            }
            
            return bResult;
        }

		private void updateActivityUI(String BT_Addr, int summary, boolean enable)
		{
			if(DEBUG)Xlog.v(TAG, "updateActivityUI");				   
			Intent tmpInt=new Intent(BluetoothHidActivity.ACTION_SUMMARY_CHANGED);
			
			tmpInt.putExtra(BluetoothHidActivity.EXTRA_DEVICE, BT_Addr);
			tmpInt.putExtra(BluetoothHidActivity.EXTRA_SUMMARY, summary);
			tmpInt.putExtra(BluetoothHidActivity.EXTRA_ENABLE, enable);
			sendBroadcast(tmpInt);
		}
		
         private String getDeviceState(String BT_Addr){
        	 if(stateMap.isEmpty())
				return null;
			if(stateMap.containsKey(BT_Addr))
				return stateMap.get(BT_Addr).toString();
			else 
				return null;
         }

         private final IBluetoothHidServerNotify.Stub mHidServerNotify = new IBluetoothHidServerNotify.Stub() {

			public void activateReq() {
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer Activate: ");
			    serverActivateReqNative();
			}
		
			public void deactivateReq(){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer DeactivateReq");
			    serverDeactivateReqNative();
				}
			public void connectReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer Connect");

			    connectHidDevice(BT_Addr);
				}
		    public void disconnectReq(String BT_Addr){
		    	if(DEBUG)Xlog.d(TAG, "BluetoothHidServer Disconnect");

			    disconnectHidDevice(BT_Addr);
		    	}
			public void unplugReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer unplug");
				/*
			    pc=BluetoothHidActivity.getDeviceList();
			    if(pc!=null){
			    	mPreference=pc.findPreference(BT_Addr);
			 	    
			 	    if(mPreference!=null){
			 	    	mPreference.setSummary(R.string.bluetooth_hid_summary_disconnecting);
			 	    	mPreference.setEnabled(false);
			 	    }	
			    } */
				updateActivityUI(BT_Addr,R.string.bluetooth_hid_summary_disconnecting,false);
			    updateSettingsState(BluetoothHid.BT_HID_DEVICE_DISCONNECTING, getBluetoothDevice(BT_Addr));
			    serverUnplugReqNative(BT_Addr);
				}
			public void sendReportReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer sendReport");
			    serverSendReportReqNative(BT_Addr);
				}
			public void setReportReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer setReport");
			    serverSetReportReqNative(BT_Addr);
				}
			public void getReportReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer getReport");
			    serverGetReportReqNative(BT_Addr);
    		}
			public void setProtocolReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer setProtocol");
			    serverSetProtocolReqNative(BT_Addr);
    		}
			public void getProtocolReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer getProtocol");
			    serverGetProtocolReqNative(BT_Addr);
    		}
			public void setIdleReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer setIdle");
			    serverSetIdleReqNative(BT_Addr);
    		}
			public void getIdleReq(String BT_Addr){
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer getIdle");
			    serverGetIdleReqNative(BT_Addr);
    		}

			public String getStateByAddr(String BT_Addr) throws RemoteException {
				// TODO Auto-generated method stub
				return getDeviceState(BT_Addr);				
			}

			public void clearService() throws RemoteException {
				// TODO Auto-generated method stub
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer clearService");
				localClearService();
			}

			public void authorizeReq(String BT_Addr,boolean result) throws RemoteException {
				// TODO Auto-generated method stub
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer authorizeReq");
				serverAuthorizeReqNative(BT_Addr,result);
			}
			
			public void finishActionReq()throws RemoteException {
				// TODO Auto-generated method stub
				if(DEBUG)Xlog.d(TAG, "BluetoothHidServer finishActionReq");
				Intent intent = new Intent(FINISH_ACTION);
				sendBroadcast(intent);
			}
    };
 
	@Override
		public void onCreate() {
			// TODO Auto-generated method stub
			printLog("Enter onCreate()");
			/* Request system services */
			mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (mNM == null) {
				if(DEBUG)Xlog.e(TAG, "Get Notification-Manager failed. Stop HID service.");
			    stopSelf();
			}
			
			IntentFilter filter = new IntentFilter();
		    filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
	        registerReceiver(mReceiver, filter);
	        
			localCreateService();
			
            IBinder b = ServiceManager.getService(BluetoothAdapter.BLUETOOTH_SERVICE);
            if (null == b) {
                throw new RuntimeException("Bluetooth service not available");
            }
            mBluetoothService = IBluetooth.Stub.asInterface(b);

        super.onCreate();
    }
		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			
			super.onDestroy();
			if(DEBUG)Xlog.d(TAG, "onDestroy()");
			unregisterReceiver(mReceiver);
			localClearService();
		}
		
		void localClearService(){
			boolean timeout = false;
			int cnt = 0;

			if (mServerState != BluetoothHid.BT_HID_STATE_DISACTIVE) {
				service_disable = true;
			    serverDeactivateReqNative();

			    while (mServerState != BluetoothHid.BT_HID_STATE_DISACTIVE) {
				if (cnt >= 5000) {
				    timeout = true;
				    break;
				}

				try {
				    Thread.sleep(100);
				} catch (Exception e) {
					if(DEBUG)Xlog.e(TAG, "Waiting for server deregister-cnf was interrupted.");
				}
				cnt += 100;
			    }
			}

			if (timeout) {
			    /* WARNNING: 
			     *     If we are here, BT task may be crashed or too busy. So we skip waiting
			     *  DEREGISTER_SERVER_CNF and just clear server context.
			     */
				if(DEBUG)Xlog.w(TAG, "Waiting DEREGISTER_SERVER_CNF time-out. Force clear server context.");
			    mServerState = BluetoothHid.BT_HID_STATE_DISACTIVE;
			    forceClearServerNative();
			    sendServiceMsg(BluetoothHid.MBTEVT_HID_HOST_DISABLE_FAIL,null);
			}
			if (mSocketListener != null) {
			    try {
			    	printLog("mSocketListener close.");	
			    	mSocketListener.shutdown();
			    	mSocketListener.join();
			    	mSocketListener = null;
			    	printLog("mSocketListener close OK.");	
			    } catch (InterruptedException e) {
			    	if(DEBUG)Xlog.e(TAG, "mSocketListener close error.");
			    }
			}
			
			if (mConnectTimeout != null) {
			    try {
				    if(DEBUG)Xlog.i(TAG,"mConnectTimeout close.");
					mConnectTimeout.shutdown();
					mConnectTimeout.join();
					mConnectTimeout = null;
					if(DEBUG)Xlog.i(TAG,"mConnectTimeout close OK.");	
			    } catch (InterruptedException e) {
			    	if(DEBUG)Xlog.e(TAG, "mConnectTimeout close error.");
			    }
			}
			
			if (mDisconnectTimeout != null) {
			    try {
			    	if(DEBUG)Xlog.i(TAG,"mDisconnectTimeout close.");	
			    	mDisconnectTimeout.shutdown();
			    	mDisconnectTimeout.join();
			    	mDisconnectTimeout = null;
			    	if(DEBUG)Xlog.i(TAG,"mDisconnectTimeout close OK.");	
			    } catch (InterruptedException e) {
			    	if(DEBUG)Xlog.e(TAG, "mDisconnectTimeout close error.");
			    }
			}
			
			stopListentoSocketNative();
			cleanServiceNative();			
		}

		void localCreateService(){
			if(mServerState != BluetoothHid.BT_HID_STATE_ACTIVE){
	    		update_state_intent.putExtra(BluetoothProfileManager.EXTRA_PROFILE, BluetoothProfileManager.Profile.Bluetooth_HID);
	    		update_state_intent.putExtra(BluetoothProfileManager.EXTRA_NEW_STATE, BluetoothProfileManager.STATE_ENABLING);
				sendBroadcast(update_state_intent);
				if (initServiceNative()) {
					printLog("Succeed to init BluetoothHidService.");
					if (mSocketListener == null) {
						mSocketListener = new SocketListenerThread();
						mSocketListener.setName("BTHidSocketListener");
						mSocketListener.stopped = false;
						mSocketListener.start();
						printLog("SocketListener started.");
					}
					service_disable = false;
					/* Default values for HID server settings */
					mServerState = BluetoothHid.BT_HID_STATE_DISACTIVE;
					serverActivateReqNative();
					if(DEBUG)Xlog.d(TAG,"Pre-enable HID Server");
				}else {
					if(DEBUG)Xlog.d(TAG,"Failed to init BluetoothHidService.");
				}
			}
		}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		String action = intent.getAction();
		if(DEBUG)Xlog.i(TAG, "Enter onBind(): " + action);

		if (IBluetoothHid.class.getName().equals(action)) {
		    return mHid;
		
		} else 
		    return mHidServerNotify;						
	}

	
	/* Utility function: printLog */
	private void printLog(String msg) {
	if (DEBUG) Xlog.d(TAG, msg);
	}
	
	private void updateDeviceState(String deviceAddr, String state){
		if(stateMap.containsKey(deviceAddr))
			stateMap.remove(deviceAddr);			    			
		stateMap.put(deviceAddr, state);
	}
	
	/* Handler associated with the main thread */
	private Handler mServiceHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(DEBUG)Xlog.i(TAG, "handleMessage(): " + msg.what);
			try{
				int notify_id=0;
				Bundle data = msg.getData();
			    String deviceAddr = (data != null) ? data.getString(BluetoothHid.DEVICE_ADDR) : null;
			    String deviceName=new String();
			    
			    Notification noti=null;
			    if(deviceAddr!=null){
				    pc=BluetoothHidActivity.getDeviceList();
			    	if(pc!=null)
			    		mPreference = pc.findPreference(deviceAddr);
			    	
			    	BluetoothDevice mBD = getBluetoothDevice(deviceAddr);
				    if(mBD != null){
					    if((deviceName = mBD.getName()) == null)
						    deviceName=getDeviceName(deviceAddr);	
				    }	    
					else{
						deviceName=getDeviceName(deviceAddr);
						if(deviceName==null)
							if(mPreference!=null)
								deviceName=mPreference.getTitle().toString();
					}	
				    			    
			    }

			    switch(msg.what)
			    {
			    	case BluetoothHid.MBTEVT_HID_HOST_ENABLE_SUCCESS:
			    		mServerState=BluetoothHid.BT_HID_STATE_ACTIVE;
			    		update_state_intent.putExtra(BluetoothProfileManager.EXTRA_NEW_STATE, BluetoothProfileManager.STATE_ENABLED);
						sendBroadcast(update_state_intent);
					
					Intent bind_intent = new Intent(BluetoothInputDevice.ACTION_BIND_SERVICE);
					sendBroadcast(bind_intent);
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_ENABLE_FAIL:
			    		update_state_intent.putExtra(BluetoothProfileManager.EXTRA_NEW_STATE, BluetoothProfileManager.STATE_ABNORMAL);
						sendBroadcast(update_state_intent);
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_DISABLE_SUCCESS:
			    		mServerState=BluetoothHid.BT_HID_STATE_DISACTIVE;
			    		update_state_intent.putExtra(BluetoothProfileManager.EXTRA_NEW_STATE, BluetoothProfileManager.STATE_DISABLED);
						sendBroadcast(update_state_intent);
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_DISABLE_FAIL:
			    		update_state_intent.putExtra(BluetoothProfileManager.EXTRA_NEW_STATE, BluetoothProfileManager.STATE_ABNORMAL);
						sendBroadcast(update_state_intent);
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_CONNECT_SUCCESS:
			    		Toast.makeText(cx, getString(R.string.bluetooth_hid_connect_ok,deviceName), Toast.LENGTH_LONG).show();
			    		mServerState=BluetoothHid.BT_HID_STATE_CONNECTED;
			    		updateSettingsState(BluetoothHid.BT_HID_DEVICE_CONNECT, getBluetoothDevice(deviceAddr));
			    		updateDeviceState(deviceAddr,BluetoothHid.BT_HID_DEVICE_CONNECT);
						/*
						if(mPreference!=null){
			    			mPreference.setSummary(R.string.bluetooth_hid_summary_connected);
			    			mPreference.setEnabled(true);
			    		}
			    			*/
						updateActivityUI(deviceAddr,R.string.bluetooth_hid_summary_connected,true);
			    		if(!notifyMap.containsKey(deviceAddr)){
			    			notifyMap.put(deviceAddr, hid_connect_notify);
			    			hid_connect_notify++;
			    		}
			    		
			    		notify_id=Integer.parseInt(notifyMap.get(deviceAddr).toString());
			    		noti=genHidNotification(notify_id,deviceName,deviceAddr,BluetoothHid.BT_HID_DEVICE_CONNECT,false);
			    		mNM.notify(notify_id, noti);

						
			    		if (mConnectTimeout != null) {
						    try {
						    	if(DEBUG)Xlog.i(TAG,"mConnectTimeout close.");
								mConnectTimeout.shutdown();
								mConnectTimeout.join();
								mConnectTimeout = null;
								if(DEBUG)Xlog.i(TAG,"mConnectTimeout close OK.");	
						    } catch (InterruptedException e) {
						    	if(DEBUG)Xlog.e(TAG, "mConnectTimeout close error.");
						    }
						}
			    		
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_CONNECT_FAIL:
			    		if(stateMap.containsKey(deviceAddr)){
			    			if(stateMap.get(deviceAddr).equals(BluetoothHid.BT_HID_DEVICE_CONNECTING)
			    					|| stateMap.get(deviceAddr).equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE)){
			    				Toast.makeText(cx, getString(R.string.bluetooth_hid_connect_fail,deviceName), Toast.LENGTH_LONG).show();
					    		updateSettingsState(BluetoothHid.BT_HID_DEVICE_DISCONNECT, getBluetoothDevice(deviceAddr));
			    				updateDeviceState(deviceAddr,BluetoothHid.BT_HID_DEVICE_DISCONNECT);
					    		/*if(mPreference!=null){
					    			mPreference.setSummary(R.string.bluetooth_hid_summary_not_connected);
					    			mPreference.setEnabled(true);
					    		}*/
								updateActivityUI(deviceAddr,R.string.bluetooth_hid_summary_not_connected,true);
			    			}
			    		}
			    		
			    		
			    		if (mConnectTimeout != null) {
						    try {
						    	if(DEBUG)Xlog.i(TAG,"mConnectTimeout close.");
								mConnectTimeout.shutdown();
								mConnectTimeout.join();
								mConnectTimeout = null;
								if(DEBUG)Xlog.i(TAG,"mConnectTimeout close OK.");	
						    } catch (InterruptedException e) {
						    	if(DEBUG)Xlog.e(TAG, "mConnectTimeout close error.");
						    }
						}
			    		if(notifyMap.containsKey(deviceAddr))
			    			notify_id=Integer.parseInt(notifyMap.get(deviceAddr).toString());
			    		mNM.cancel(notify_id);
			    					    		
			     		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_DISCONNECT_SUCCESS:
			    		if(service_disable == false)
			    			Toast.makeText(cx, getString(R.string.bluetooth_hid_disconnect_ok,deviceName), Toast.LENGTH_LONG).show();
			    		mServerState=BluetoothHid.BT_HID_STATE_DISCONNECTED;
			    		
		    			if(stateMap.containsKey(deviceAddr)){
			    			if(stateMap.get(deviceAddr).equals(BluetoothHid.BT_HID_DEVICE_UNPLUG)){
				    			updateSettingsState(BluetoothHid.BT_HID_DEVICE_DISCONNECT, getBluetoothDevice(deviceAddr));
				    			stateMap.remove(deviceAddr);
				    			stateMap.put(deviceAddr, BluetoothHid.BT_HID_DEVICE_UNPLUG_DISCONNECT);
				    			if(mPreference!=null)
				    				pc.removePreference(mPreference);
				    		}			    			
				    		else{
				    			updateSettingsState(BluetoothHid.BT_HID_DEVICE_DISCONNECT, getBluetoothDevice(deviceAddr));
				    			stateMap.remove(deviceAddr);
				    			stateMap.put(deviceAddr, BluetoothHid.BT_HID_DEVICE_DISCONNECT);
					    		if(notifyMap.containsKey(deviceAddr)){
					    			notify_id=Integer.parseInt(notifyMap.get(deviceAddr).toString());
					    			mNM.cancel(notify_id);
					    		}
								/*
				    			if(mPreference!=null){
				    				mPreference.setSummary(R.string.bluetooth_hid_summary_not_connected);
				    				mPreference.setEnabled(true);
				    			}*/
							updateActivityUI(deviceAddr,R.string.bluetooth_hid_summary_not_connected,true);
				    		}
		    			}

		    			
		    			if (mDisconnectTimeout != null) {
						    try {
						    	if(DEBUG)Xlog.i(TAG,"mDisconnectTimeout close.");	
								mDisconnectTimeout.shutdown();
								mDisconnectTimeout.join();
								mDisconnectTimeout = null;
								if(DEBUG)Xlog.i(TAG,"mDisconnectTimeout close OK.");	
						    } catch (InterruptedException e) {
						    	if(DEBUG)Xlog.e(TAG, "mDisconnectTimeout close error.");
						    }
						}
		    			
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_DISCONNECT_FAIL:
			    		if(stateMap.containsKey(deviceAddr)){
			    			if(stateMap.get(deviceAddr).equals(BluetoothHid.BT_HID_DEVICE_DISCONNECTING)
			    					|| stateMap.get(deviceAddr).equals(BluetoothHid.BT_HID_DEVICE_UNPLUG)){
			    				if(service_disable == false)
			    					Toast.makeText(cx, getString(R.string.bluetooth_hid_disconnect_fail,deviceName), Toast.LENGTH_LONG).show();

					    		updateSettingsState(BluetoothHid.BT_HID_DEVICE_CONNECT, getBluetoothDevice(deviceAddr));
					    		updateDeviceState(deviceAddr,BluetoothHid.BT_HID_DEVICE_CONNECT);
					    		/*if(mPreference!=null){
					    			mPreference.setSummary(R.string.bluetooth_hid_summary_connected);
					    			mPreference.setEnabled(true);
					    		}*/
							updateActivityUI(deviceAddr,R.string.bluetooth_hid_summary_connected,true);
			    			}
			    		}
			    		
			    		if (mDisconnectTimeout != null) {
						    try {
						    	if(DEBUG)Xlog.i(TAG,"mDisconnectTimeout close.");	
								mDisconnectTimeout.shutdown();
								mDisconnectTimeout.join();
								mDisconnectTimeout = null;
								if(DEBUG)Xlog.i(TAG,"mDisconnectTimeout close OK.");	
						    } catch (InterruptedException e) {
						    	if(DEBUG)Xlog.e(TAG, "mDisconnectTimeout close error.");
						    }
						}
					break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_SEND_CONTROL_SUCCESS:
			    		Toast.makeText(cx, getString(R.string.bluetooth_hid_unplug_ok,deviceName), Toast.LENGTH_LONG).show();
			    		mServerState=BluetoothHid.BT_HID_STATE_DISCONNECTED;
			    		updateDeviceState(deviceAddr,BluetoothHid.BT_HID_DEVICE_UNPLUG);

			    		if(notifyMap.containsKey(deviceAddr)){
			    			notify_id=Integer.parseInt(notifyMap.get(deviceAddr).toString());
			    			mNM.cancel(notify_id);
			    		}
			    		break;
						
			    	case BluetoothHid.MBTEVT_HID_HOST_SEND_CONTROL_FAIL:
			    		Toast.makeText(cx, getString(R.string.bluetooth_hid_unplug_fail,deviceName), Toast.LENGTH_LONG).show();
			    		break;
			    	case BluetoothHid.MBTEVT_HID_HOST_RECEIVE_AUTHORIZE:
				    	if(!notifyMap.containsKey(deviceAddr)){
			    			notifyMap.put(deviceAddr, hid_connect_notify);
			    			hid_connect_notify++;
			    		}
				    		
				    	updateSettingsState(BluetoothHid.BT_HID_DEVICE_CONNECTING, getBluetoothDevice(deviceAddr));
				    	updateDeviceState(deviceAddr,BluetoothHid.BT_HID_DEVICE_AUTHORIZE);
				    	/*
					 	if(mPreference!=null){
					 	    mPreference.setSummary(R.string.bluetooth_hid_summary_connecting);
					 	    mPreference.setEnabled(false);
					 	}						   
			    		*/
			    		
						updateActivityUI(deviceAddr,R.string.bluetooth_hid_summary_connecting,false);
			    		notify_id=Integer.parseInt(notifyMap.get(deviceAddr).toString());
			    		noti=genHidNotification(notify_id,deviceName,deviceAddr,BluetoothHid.BT_HID_DEVICE_AUTHORIZE,true);
			    		mNM.notify(notify_id, noti);
			    		break;
			    }
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid stateMap error");
				e.printStackTrace();
			}
			if(BluetoothHid.MBTEVT_HID_HOST_CONNECT_SUCCESS ==msg.what
					||BluetoothHid.MBTEVT_HID_HOST_CONNECT_FAIL==msg.what
					||BluetoothHid.MBTEVT_HID_HOST_DISCONNECT_SUCCESS==msg.what
					||BluetoothHid.MBTEVT_HID_HOST_DISCONNECT_FAIL==msg.what
					||BluetoothHid.MBTEVT_HID_HOST_SEND_CONTROL_SUCCESS==msg.what
					||BluetoothHid.MBTEVT_HID_HOST_SEND_CONTROL_FAIL==msg.what){
				Intent intent = new Intent(FINISH_ACTION);
				sendBroadcast(intent);
			}
 
		}
	};
	
	/* Utility function: sendServiceMsg */
	private void sendServiceMsg(int what, String addr) {
		Message msg = Message.obtain();
		
		if(DEBUG)Xlog.i(TAG,"sendServiceMsg status="+what+"address="+addr);

		if(what==BluetoothHid.MBTEVT_HID_HOST_DISABLE_SUCCESS || what==BluetoothHid.MBTEVT_HID_HOST_DISABLE_FAIL)
			mServerState=BluetoothHid.BT_HID_STATE_DISACTIVE;

		if(what==BluetoothHid.MBTEVT_HID_HOST_ENABLE_SUCCESS)
			mServerState=BluetoothHid.BT_HID_STATE_ACTIVE;
		
		msg.what = what;

		Bundle data=new Bundle();
		data.putString(BluetoothHid.DEVICE_ADDR, addr);
		msg.setData(data);
	
		mServiceHandler.sendMessage(msg);
	}
	 /* Utility function: genHidNotification */
    private Notification genHidNotification(int type,String deviceName, String deviceaddr, String action, boolean needSound) {

		Context context = getApplicationContext();
		Intent tmpIntent=new Intent();
		Notification tmpNoti = null;
		PendingIntent tmpContentIntent = null;
		int icon_id = -1;
		String clazz = null, ticker = null, title = null;
		if(DEBUG)Xlog.i(TAG, "genHidNotification "+deviceaddr);

		icon_id = R.drawable.bthid_ic_notify_wireless_keyboard;
		clazz = BluetoothHidAlert.class.getName();
		tmpIntent.setClassName(getPackageName(), clazz)
	      .putExtra(BluetoothHid.DEVICE_ADDR, deviceaddr);
	
		if(action.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
			ticker = getString(R.string.bluetooth_hid_connected_notify_ticker);
			title = getString(R.string.bluetooth_hid_connected_notify_title);
			tmpNoti = new Notification(icon_id, ticker, System.currentTimeMillis());
			tmpNoti.flags = Notification.FLAG_ONGOING_EVENT;
			
			tmpIntent.putExtra(BluetoothHid.ACTION,BluetoothHid.BT_HID_DEVICE_DISCONNECT);
			tmpContentIntent = PendingIntent.getActivity(getApplicationContext(), type,tmpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			tmpNoti.setLatestEventInfo(context, title, getString(R.string.bluetooth_hid_connected_notify_message, deviceName), tmpContentIntent);
		}
		else if(action.equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE)){
			ticker = getString(R.string.bluetooth_hid_connect_request_notify_ticker);
			title = getString(R.string.bluetooth_hid_connect_request_notify_title);
			tmpNoti = new Notification(icon_id, ticker, System.currentTimeMillis());
			tmpNoti.flags = Notification.FLAG_ONLY_ALERT_ONCE;
			if(needSound){
				tmpNoti.defaults |= Notification.DEFAULT_SOUND;
				tmpNoti.defaults |= Notification.DEFAULT_VIBRATE;
			}
				
			
			tmpIntent.putExtra(BluetoothHid.ACTION,BluetoothHid.BT_HID_DEVICE_AUTHORIZE);
			tmpContentIntent = PendingIntent.getActivity(getApplicationContext(), type,tmpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			tmpNoti.setLatestEventInfo(context, title, getString(R.string.bluetooth_hid_connect_request_notify_message, deviceName), tmpContentIntent);
		}
		
		return tmpNoti;
    }
    
    private BluetoothDevice getBluetoothDevice(String BT_Addr){
    			
		return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(BT_Addr);
		
    }
    
    
    private String getDeviceName(String BT_Addr){
    	SharedPreferences settings=getSharedPreferences(BT_HID_SETTING_INFO, 0);		
		
		int preIndex=0;
		int preferenceCount=settings.getInt("preferenceCount", 0);
				
		for(preIndex=0;preIndex<preferenceCount;preIndex++){
			String tmpAddr=settings.getString("deviceAddr"+Integer.toString(preIndex), BT_HID_NOT_FOUNT);
			if(tmpAddr.equals(BT_Addr))
				return settings.getString("deviceName"+Integer.toString(preIndex), BT_HID_NOT_FOUNT);
		}
		return null;
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	int notify_id=0;
		    Notification noti=null;
        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        	if(device == null){
        		if(DEBUG)Xlog.e(TAG,"ERROR: device null");
        		return;
        	}
        		
        	String deviceAddr = device.getAddress();
        	String deviceName = device.getName();
        	String state = getDeviceState(deviceAddr);
        	String notify_s = null;
        	if(notifyMap.containsKey(deviceAddr))
        		notify_s = notifyMap.get(deviceAddr).toString();
        	if(notify_s == null){
        		if(DEBUG)Xlog.e(TAG,"ERROR: notify_s null");
        		return;
        	}
        	if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())){
        		int bonded_state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,BluetoothDevice.ERROR);
        		if (bonded_state == BluetoothDevice.BOND_NONE){
        			if(deviceAddr!=null){
        				pc=BluetoothHidActivity.getDeviceList();
				    	if(pc!=null)
				    		mPreference = pc.findPreference(deviceAddr);
				    	
				    	if(mPreference!=null)
							pc.removePreference(mPreference);
        			}
        		}
	    			
        	}
        	if(state != null)
	            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(intent.getAction())) {
	            	if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
	            		notify_id=Integer.parseInt(notify_s);
			    		noti=genHidNotification(notify_id,deviceName,deviceAddr,BluetoothHid.BT_HID_DEVICE_CONNECT,false);
			    		mNM.notify(notify_id, noti);
	            	}
	            	else if(state.equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE)){
	            		notify_id=Integer.parseInt(notify_s);
			    		noti=genHidNotification(notify_id,deviceName,deviceAddr,BluetoothHid.BT_HID_DEVICE_AUTHORIZE,false);
			    		mNM.notify(notify_id, noti);
	            	}
		    		
	            }       
        }
    };
}
