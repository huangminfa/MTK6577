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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mediatek.bluetooth.R;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothHid;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDevicePicker;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

public class BluetoothHidActivity extends PreferenceActivity {
	
//public class BluetoothHidActivity extends Activity {
	public static final String INTENT =
		"android.bluetooth.devicepicker.action.LAUNCH";
	public static final String EXTRA_NEED_AUTH =
		 "android.bluetooth.devicepicker.extra.NEED_AUTH";
	public static final String EXTRA_FILTER_TYPE =
		 "android.bluetooth.devicepicker.extra.FILTER_TYPE";
	public static final String EXTRA_LAUNCH_PACKAGE =
		 "android.bluetooth.devicepicker.extra.LAUNCH_PACKAGE";
	public static final String EXTRA_LAUNCH_CLASS =
		 "android.bluetooth.devicepicker.extra.DEVICE_PICKER_LAUNCH_CLASS";
	public static final String THIS_PACKAGE_NAME="com.mediatek.bluetooth";
	
	public static final String ACTION_SUMMARY_CHANGED="com.mediatek.bluetooth.BluetoothHidActivity.ACTION_SUMMARY_CHANGED";
	public static final String EXTRA_DEVICE="com.mediatek.bluetooth.BluetoothHidActivity.extra.EXTRA_DEVICE";
	public static final String EXTRA_SUMMARY="com.mediatek.bluetooth.BluetoothHidActivity.extra.EXTRA_SUMMARY";
	public static final String EXTRA_ENABLE="com.mediatek.bluetooth.BluetoothHidActivity.extra.EXTRA_ENABLE";
	
	public static final String ACTION_DEVICE_ADDED="com.mediatek.bluetooth.BluetoothHidActivity.ACTION_DEVICE_ADDED";
	/** Ask device picker to show BT devices that support HID */
	private static final boolean DEBUG = true;
    private static final int BLUETOOTH_DEVICE_REQUEST = 1;
    private static final String BLUETOOTH_DEVICE_ENABLE = "com.mediatek.bluetooth.action.DEVICE_ENABLE";
    private static final String EXTRA_ENABLE_RESULT = "com.mediatek.bluetooth.extra.ENABLE_RESULT";
    
	private static final String ADD_NEW_DEVICE="add_new_device";
	private static final String HID_DEVICE_LIST="hid_device_list";
	public static final String BT_HID_SETTING_INFO="BT_HID_SETTING_INFO";
	public static final String BT_HID_NOT_FOUNT="BT_HID_NOT_FOUNT";
	private IBluetoothHidServerNotify mServerNotify=null;
	private static boolean BluetoothHidPts=false;
	private static boolean	enableBT = false;
	private static boolean	serviceBinded = false;
	private Button connect;
	private Button disconnect;
	private Button deactivate;
	private Button activate;
	private Button unplug;
	private Button sendreport;
	private Button setreport;
	private Button getreport;
	private String BT_Addr;
	private static final int	BluetoothEnableDialog = 1;
	private static final int 	REQUEST_ENABLE_BT=2;

    private static final int CONTEXT_ITEM_CONNECT = Menu.FIRST + 1;
    private static final int CONTEXT_ITEM_DISCONNECT = Menu.FIRST + 2;
    private static final int CONTEXT_ITEM_UNPLUG = Menu.FIRST + 3;
    private static final int CONTEXT_ITEM_SET_PROTOCOL = Menu.FIRST + 4;
    private static final int CONTEXT_ITEM_GET_PROTOCOL = Menu.FIRST + 5;
    private static final int CONTEXT_ITEM_SET_REPORT = Menu.FIRST + 6;
    private static final int CONTEXT_ITEM_GET_REPORT = Menu.FIRST + 7;
    private static final int CONTEXT_ITEM_SET_IDLE = Menu.FIRST + 8;
    private static final int CONTEXT_ITEM_GET_IDLE = Menu.FIRST + 9;
    private static final int CONTEXT_ITEM_SEND_REPORT = Menu.FIRST + 10;

	final private String BT_Addr_Nokia="00:0E:ED:9C:F0:08";
	final private String BT_Addr_Logitech="00:07:61:80:A7:89";
	final private String BT_Addr_PTS="00:13:EF:F0:D8:87";
	final private String TAG = "[BT][HID][BluetoothHidActivity]";
	private BluetoothAdapter mBluetoothAdapter = null;
	Context ct=this;

	
	static PreferenceCategory	mDeviceList;


	Intent intentToDevicePicker = new Intent(INTENT);
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		if(preference.getKey().equals(ADD_NEW_DEVICE)){
			if(DEBUG)Xlog.i(TAG,"Add New Devices");
			intentToDevicePicker.putExtra(EXTRA_NEED_AUTH, true);
			intentToDevicePicker.putExtra(EXTRA_FILTER_TYPE,BluetoothDevicePicker.FILTER_TYPE_HID);
			intentToDevicePicker.putExtra(EXTRA_LAUNCH_PACKAGE,THIS_PACKAGE_NAME);
			intentToDevicePicker.putExtra(EXTRA_LAUNCH_CLASS,BluetoothHidReceiver.class.getName());
			//intentToDevicePicker.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intentToDevicePicker);	
			return true;
		}
		if(preference instanceof Preference){
			try{
				String	state=getmServerNotify().getStateByAddr(preference.getKey());
				if(DEBUG)Xlog.i(TAG, "device state="+state);
				
				if(state==null
						||state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT)
						||state.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG_DISCONNECT)
						||state.equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE)){
					
						if(DEBUG)Xlog.i(TAG,"hid connect "+preference.getTitle().toString());
						getmServerNotify().connectReq(preference.getKey().toString());
						return true;
					
				}		
					
				else if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
					if(DEBUG)Xlog.i(TAG,"hid disconnect "+preference.getTitle().toString());
					//getmServerNotify().disconnectReq(pre.getKey().toString());
					Intent tmpIntent=new Intent();
					tmpIntent.setClassName(getPackageName(), BluetoothHidAlert.class.getName())
				      .putExtra(BluetoothHid.DEVICE_ADDR, preference.getKey().toString())
				      .putExtra(BluetoothHid.ACTION,BluetoothHid.BT_HID_DEVICE_DISCONNECT);
					startActivity(tmpIntent);
					return true;
				}
			}catch (Exception e) {
					// TODO Auto-generated catch block
					if(DEBUG)Xlog.i(TAG,"hid connect error");
					e.printStackTrace();
				}	
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.bt_hid_test);
		if(DEBUG)Xlog.i(TAG,"onCreate");
		addPreferencesFromResource(R.xml.bt_hid_preference_setting);
		setTitle(R.string.bluetooth_hid_lable);
		mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter==null){
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mDeviceList=(PreferenceCategory)findPreference(HID_DEVICE_LIST);
		if(mDeviceList != null)
			mDeviceList.setOrderingAsAdded(true);
		registerForContextMenu(getListView());
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothHidActivity.ACTION_SUMMARY_CHANGED);
		filter.addAction(BluetoothHidActivity.ACTION_DEVICE_ADDED);
		registerReceiver(mReceiver, filter);
		
		if(!mBluetoothAdapter.isEnabled() && !enableBT){
			//showDialog(BluetoothEnableDialog);
			if(DEBUG)Xlog.w(TAG, "bluetooth is not available! ");
			if(DEBUG)Xlog.v(TAG, "turning on bluetooth......");
           
		        enableBT = true;
		            
		        Intent intentEnable=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intentEnable,REQUEST_ENABLE_BT);	
			return;
		}
			
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(DEBUG)Xlog.i(TAG,"onActivityResult, resultCode = "+resultCode);
		switch(requestCode){
			case REQUEST_ENABLE_BT:
				if(resultCode==Activity.RESULT_OK){
					Toast.makeText(this, R.string.bluetooth_hid_bt_ok, Toast.LENGTH_LONG).show();
					this.startService(new Intent(BluetoothHidActivity.this,BluetoothHidService.class));
					if(mBluetoothAdapter.getState()!=BluetoothAdapter.STATE_TURNING_OFF)
						if(!this.bindService(new Intent(BluetoothHidActivity.this,BluetoothHidService.class), mHidServerNotifyConn, Context.BIND_AUTO_CREATE)){
							serviceBinded = false;
							finish();
						}
						else
							serviceBinded = true;
					else
						finish();
					if(DEBUG)Xlog.i(TAG,"hid success bind service in onActivityResult");
        		        	enableBT = false;
					/*
					IntentFilter filter = new IntentFilter();
				    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
				    filter.addAction(BluetoothHidActivity.ACTION_SUMMARY_CHANGED);
				    filter.addAction(BluetoothHidActivity.ACTION_DEVICE_ADDED);
			        registerReceiver(mReceiver, filter);
			        */
				}
				else{					
	                //Toast.makeText(this, R.string.bluetooth_hid_bt_fail, Toast.LENGTH_LONG).show();
                	enableBT = false;
                	//this.finish();					
				}
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();		
		if(DEBUG)Xlog.w(TAG,"onStart, enableBT="+enableBT);
		Bundle data = getIntent().getExtras();
		if(data !=null){
			if(DEBUG)Xlog.i(TAG,"check BLUETOOTH_HID_PTS in onStart");
			String action = new String();
			if((action = data.getString("BLUETOOTH_HID_PTS"))!= null)
				BluetoothHidPts = action.equals("TRUE")? true : false;			
		}

			if(!enableBT){
				this.startService(new Intent(BluetoothHidActivity.this,BluetoothHidService.class));
				if(mBluetoothAdapter.getState()!=BluetoothAdapter.STATE_TURNING_OFF)
					if(!this.bindService(new Intent(BluetoothHidActivity.this,BluetoothHidService.class), mHidServerNotifyConn, Context.BIND_AUTO_CREATE)){
						serviceBinded = false;
						finish();
					}
					else
						serviceBinded = true;				
				else
					finish();
				if(DEBUG)Xlog.i(TAG,"hid success bind service in onStart");
				/*
				IntentFilter filter = new IntentFilter();
			    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
				filter.addAction(BluetoothHidActivity.ACTION_SUMMARY_CHANGED);
				filter.addAction(BluetoothHidActivity.ACTION_DEVICE_ADDED);
		        registerReceiver(mReceiver, filter);	
		        */
			}
			//enableBT = false;		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(DEBUG)Xlog.i(TAG,"onPause");
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		if(DEBUG)Xlog.i(TAG,"onRestart");
		if(!mBluetoothAdapter.isEnabled() && !enableBT){
			//showDialog(BluetoothEnableDialog);
			if(DEBUG)Xlog.w(TAG, "bluetooth is not available! ");
			if(DEBUG)Xlog.v(TAG, "turning on bluetooth......");
           
		        enableBT = true;
		            
		        Intent intentEnable=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intentEnable,REQUEST_ENABLE_BT);	
			return;
		}

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(DEBUG)Xlog.i(TAG,"onResume");	
		if(!mBluetoothAdapter.isEnabled() && enableBT == false){
			//showDialog(BluetoothEnableDialog);
			if(DEBUG)Xlog.w(TAG, "bluetooth is not available! ");
			Toast.makeText(this, R.string.bluetooth_hid_bt_fail, Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	private void restorePreferenceList(){
		if(mServerNotify == null)
			return;
		SharedPreferences settings=getSharedPreferences(BT_HID_SETTING_INFO, 0);
		int preferenceCount=mDeviceList.getPreferenceCount();		
		int preferenceIndex=0;
		Preference tmpPre=new Preference(this);
		if(DEBUG)Xlog.i(TAG,"In restorePreferenceList,preferenceCount="+preferenceCount);
		settings.edit().putInt("preferenceCount", preferenceCount)
						.commit();
		for(preferenceIndex=0;preferenceIndex<preferenceCount;preferenceIndex++){
			tmpPre=mDeviceList.getPreference(preferenceIndex);
			settings.edit().putString("deviceAddr"+Integer.toString(preferenceIndex), tmpPre.getKey().toString())
							.putString("newAdd"+Integer.toString(preferenceIndex), "FALSE")
							.commit();
		}	
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if(mBluetoothAdapter.isEnabled()){				
			restorePreferenceList();
			if(serviceBinded == true){
				if(DEBUG)Xlog.i(TAG,"onStop:unbindservice, enable="+ enableBT);
				enableBT = false;
				this.unbindService(mHidServerNotifyConn);
				serviceBinded = false;
				/*
				if(DEBUG)Xlog.i(TAG,"onStop: unregister broadcastReceiver");
				this.unregisterReceiver(mReceiver);
				*/
			}
			mDeviceList.removeAll();
			//this.stopService(new Intent(BluetoothHidActivity.this,BluetoothHidService.class));
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(DEBUG)Xlog.i(TAG,"onDestroy");
		BluetoothHidPts = false;
		if(DEBUG)Xlog.i(TAG,"onDestroy: unregister broadcastReceiver");
		this.unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	private ServiceConnection mHidServerNotifyConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			if(DEBUG)Xlog.i(TAG,"onServiceConnected");
			mServerNotify=IBluetoothHidServerNotify.Stub.asInterface(service);
			if(mServerNotify == null){
				if(DEBUG)Xlog.e(TAG,"ERROR:mServerNotify null");
				return;
			}
			getPreferenceList();
		}

		public void onServiceDisconnected(ComponentName className) {
			mServerNotify=null;
		}
	};

	private void getPreferenceList(){		
		Set<BluetoothDevice>	pairedDevices;
		pairedDevices=BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		if(pairedDevices == null)
			return;
		
		SharedPreferences settings=getSharedPreferences(BT_HID_SETTING_INFO, 0);		
		int i;
		boolean find;
		int preIndex=0;
		int preferenceCount=settings.getInt("preferenceCount", 0);
		
		String state = new String();
				
		for(preIndex=0;preIndex<preferenceCount;preIndex++){
			Preference tmpPre=new Preference(ct);
			String tmpAddr=settings.getString("deviceAddr"+Integer.toString(preIndex), BT_HID_NOT_FOUNT);
			String newAdd=settings.getString("newAdd"+Integer.toString(preIndex), BT_HID_NOT_FOUNT);
			
			String tmpName=new String();
		
			tmpPre.setKey(tmpAddr);
			BluetoothDevice mBD = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(tmpAddr);
			if(mBD != null)
				tmpName = mBD.getName();
			tmpPre.setTitle(tmpName);				
			if(DEBUG)Xlog.i(TAG,"in getPreferenceList "+tmpName+":"+newAdd);
			try{
				state=mServerNotify.getStateByAddr(tmpAddr);
				if(DEBUG)Xlog.i(TAG,"in getPreferenceList "+tmpName+":"+state);
				if(state==null
						||state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT)
						||state.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG_DISCONNECT))
					tmpPre.setSummary(R.string.bluetooth_hid_summary_not_connected);
				else if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT))
					tmpPre.setSummary(R.string.bluetooth_hid_summary_connected);
				else if(state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECTING)
						||state.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG)){
					tmpPre.setSummary(R.string.bluetooth_hid_summary_disconnecting);
					tmpPre.setEnabled(false);
				}
				else if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECTING)
						||state.equals(BluetoothHid.BT_HID_DEVICE_AUTHORIZE)){
					tmpPre.setSummary(R.string.bluetooth_hid_summary_connecting);
					tmpPre.setEnabled(false);
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid retrieve preferences error");
				tmpPre.setSummary(R.string.bluetooth_hid_summary_not_connected);
				e.printStackTrace();	
			}	
			
			if(mDeviceList.findPreference(tmpAddr) == null){
				if(state != null){
					if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT))				
						mDeviceList.addPreference(tmpPre);
				}
			}
			
			if(mDeviceList.findPreference(tmpAddr) == null){
				if(state != null)
					if(state.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG_DISCONNECT)
							&& newAdd.equals("FALSE"))
						continue;
				if(pairedDevices.size()>0){
					if(DEBUG)Xlog.i(TAG,"in getPreferenceList pairedDevices.size="+pairedDevices.size());
					for(BluetoothDevice tmpDevice:pairedDevices){
						if(tmpDevice.getAddress().equals(tmpAddr) )
							mDeviceList.addPreference(tmpPre);
					}
				}
			}					
		}
		
		for(BluetoothDevice tmpDevice:pairedDevices){
			try{
				state = mServerNotify.getStateByAddr(tmpDevice.getAddress());
				if(state != null){
					if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
					Preference tmpPre=new Preference(ct);
					tmpPre.setKey(tmpDevice.getAddress());
					tmpPre.setTitle(tmpDevice.getName());	
					tmpPre.setSummary(R.string.bluetooth_hid_summary_connected);	
					if(mDeviceList.findPreference(tmpDevice.getAddress()) == null)				
						mDeviceList.addPreference(tmpPre);
					}
				}					
					
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid retrieve preferences error");
				e.printStackTrace();	
			}
			
		}

	}
	
	public static PreferenceCategory getDeviceList(){
		return mDeviceList;
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		Preference pre = getDeviceFromMenuInfo(item.getMenuInfo());
		if(pre == null)
			return false;
		switch(item.getItemId()){
		case CONTEXT_ITEM_CONNECT:
			try{
				if(DEBUG)Xlog.i(TAG,"hid connect "+pre.getTitle().toString());
				getmServerNotify().connectReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid connect error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_DISCONNECT:
			try{
				if(DEBUG)Xlog.i(TAG,"hid disconnect "+pre.getTitle().toString());
				//getmServerNotify().disconnectReq(pre.getKey().toString());
				Intent tmpIntent=new Intent();
				tmpIntent.setClassName(getPackageName(), BluetoothHidAlert.class.getName())
			      .putExtra(BluetoothHid.DEVICE_ADDR, pre.getKey().toString())
			      .putExtra(BluetoothHid.ACTION,BluetoothHid.BT_HID_DEVICE_DISCONNECT);
				this.startActivity(tmpIntent);
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid disconnect error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_UNPLUG:
			try{
				if(DEBUG)Xlog.i(TAG,"hid unplug "+pre.getTitle().toString());
				//getmServerNotify().unplugReq(pre.getKey().toString());
				Intent tmpIntent=new Intent();
				tmpIntent.setClassName(getPackageName(), BluetoothHidAlert.class.getName())
			      .putExtra(BluetoothHid.DEVICE_ADDR, pre.getKey().toString())
			      .putExtra(BluetoothHid.ACTION,BluetoothHid.BT_HID_DEVICE_UNPLUG);
				this.startActivity(tmpIntent);
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid unplug error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_SET_IDLE:
			try{
				if(DEBUG)Xlog.i(TAG,"hid SET_IDLE "+pre.getTitle().toString());
				getmServerNotify().setIdleReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid SET_IDLE error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_GET_IDLE:
			try{
				if(DEBUG)Xlog.i(TAG,"hid GET_IDLE "+pre.getTitle().toString());
				getmServerNotify().getIdleReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid GET_IDLE error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_SET_REPORT:
			try{
				if(DEBUG)Xlog.i(TAG,"hid SET_REPORT "+pre.getTitle().toString());
				getmServerNotify().setReportReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid SET_REPORT error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_GET_REPORT:
			try{
				if(DEBUG)Xlog.i(TAG,"hid GET_REPORT "+pre.getTitle().toString());
				getmServerNotify().getReportReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid GET_REPORT error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_SET_PROTOCOL:
			try{
				if(DEBUG)Xlog.i(TAG,"hid SET_PROTOCOL "+pre.getTitle().toString());
				getmServerNotify().setProtocolReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid SET_PROTOCOL error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_GET_PROTOCOL:
			try{
				if(DEBUG)Xlog.i(TAG,"hid GET_PROTOCOL "+pre.getTitle().toString());
				getmServerNotify().getProtocolReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid GET_PROTOCOL error");
				e.printStackTrace();
			}
		case CONTEXT_ITEM_SEND_REPORT:
			try{
				if(DEBUG)Xlog.i(TAG,"hid SEND_REPORT "+pre.getTitle().toString());
				getmServerNotify().sendReportReq(pre.getKey().toString());
				return true;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if(DEBUG)Xlog.i(TAG,"hid SEND_REPORT error");
				e.printStackTrace();
			}
		}
		return false;
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		try{
			Preference pre = getDeviceFromMenuInfo(menuInfo);
			if(pre == null)
				return;
			if(!pre.getKey().equals(ADD_NEW_DEVICE)){
				String	state=getmServerNotify().getStateByAddr(pre.getKey());
				if(DEBUG)Xlog.i(TAG, "device state="+state);
				menu.setHeaderTitle(pre.getTitle());
				if(state==null){
					menu.add(0, CONTEXT_ITEM_CONNECT, 0, R.string.bluetooth_hid_connect);	
				}
								
				else if (state.equals(BluetoothHid.BT_HID_DEVICE_DISCONNECT)
						||state.equals(BluetoothHid.BT_HID_DEVICE_UNPLUG_DISCONNECT)){
					menu.add(0, CONTEXT_ITEM_CONNECT, 0, R.string.bluetooth_hid_connect);
				}			
					
				else if(state.equals(BluetoothHid.BT_HID_DEVICE_CONNECT)){
					menu.add(0, CONTEXT_ITEM_DISCONNECT, 0, R.string.bluetooth_hid_disconnect);
					menu.add(0, CONTEXT_ITEM_UNPLUG, 0, R.string.bluetooth_hid_unplug);
					if(BluetoothHidPts != false)
					{
						menu.add(0, CONTEXT_ITEM_GET_IDLE, 0, "Get_idle");
						menu.add(0, CONTEXT_ITEM_SET_IDLE, 0, "Set_idle");
						menu.add(0, CONTEXT_ITEM_GET_REPORT, 0, "Get_report");
						menu.add(0, CONTEXT_ITEM_SET_REPORT, 0, "Set_report");
						menu.add(0, CONTEXT_ITEM_GET_PROTOCOL, 0, "Get_protocol");
						menu.add(0, CONTEXT_ITEM_SET_PROTOCOL, 0, "Set_protocol");
						menu.add(0, CONTEXT_ITEM_SEND_REPORT, 0, "Send_report");
						
					}
				}
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			if(DEBUG)Xlog.i(TAG,"hid getStateByAddr error");
			e.printStackTrace();
		}
		
	}
	
	private Preference getDeviceFromMenuInfo(ContextMenuInfo menuInfo) {
        if ((menuInfo == null) || !(menuInfo instanceof AdapterContextMenuInfo)) {
            return null;
        }

        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        Preference pref = (Preference) getPreferenceScreen().getRootAdapter().getItem(adapterMenuInfo.position);
        if (pref == null) {
            return null;
        }

        return pref;
    }
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	public IBluetoothHidServerNotify getmServerNotify() {
		return mServerNotify;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            	int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

	    	    switch(btState) {
	    		case BluetoothAdapter.STATE_TURNING_OFF:
	    			if(DEBUG)Xlog.i(TAG,"hid activity receiver receives BT OFF intent");
	    			restorePreferenceList();				
	    		
				if(serviceBinded == true){
					serviceBinded = false;
		    			if(DEBUG)Xlog.i(TAG,"mReceiver,unbindservice");
		    			ct.unbindService(mHidServerNotifyConn);
				}
				if(enableBT == false)
	    				finish();
	    	    }
            }
			else if(BluetoothHidActivity.ACTION_SUMMARY_CHANGED.equals(intent.getAction())){
				if(DEBUG)Xlog.i(TAG,"Update summary");
				Preference mPreference=null;
				String BT_Addr = intent.getStringExtra(BluetoothHidActivity.EXTRA_DEVICE);
				int summary = intent.getIntExtra(BluetoothHidActivity.EXTRA_SUMMARY, -1);
				boolean enable = intent.getBooleanExtra(BluetoothHidActivity.EXTRA_ENABLE, true);
				if(mDeviceList!=null){
					mPreference=mDeviceList.findPreference(BT_Addr);
						
					if(mPreference!=null){
						mPreference.setSummary(summary);
						mPreference.setEnabled(enable);	
					}	
				}	
			}
			else if(BluetoothHidActivity.ACTION_DEVICE_ADDED.equals(intent.getAction())){
				if(DEBUG)Xlog.i(TAG,"New device added");
				getPreferenceList();
			}
        }
    };

	
}
