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

package com.mediatek.engineermode;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import android.os.SystemProperties;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

public class EngineerMode extends Activity implements OnItemClickListener {
    /** Called when the activity is first created. */

    private List<String> mListData;
    public static final int MODEM_MASK_WCDMA = 0x04;
	public static final int MODEM_MASK_TDSCDMA = 0x08;

    final String itemString[] = { "BatteryLog", "TouchScreen", "GPRS", "Audio",
            "Camera", "NetworkInfo", "AutoAnswer", "Bluetooth", "WiFi", "NFC", 
            "YGPS", "LocationBasedService", "CDS Information", "VideoTelephony", "Display",
	    "Memory", "IO", "Power", "Baseband","FM Transmitter", "FM Receiver", "SIMMeLock",
            "SystemLog", "De-Sense", "Matv", "CMMB", "CPU Freq Test", "CPU Stress Test", "Modem",
            "USB", "ModemTest", "Device Manager", "Digital Standard",
            "BandMode", /*"Repeat Call Test",*/ "Log2Server", "Tag Log", "TV Out",
            "CFU", "SWLA", "SDCardTest", "RAT Mode", "SettingsFont", "Debug Utils", "Network Setting",
            "Dual Talk NetworkInfo","Dual Talk BandMode", "BSP Telephony Dev", "Thermal"};

    public static final int SETTINGS_ID = Menu.FIRST;
    public static final String TAG = "EM";

    private boolean sIsVoiceCapable = true;
    private boolean isVoiceCapable(){
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        sIsVoiceCapable = (telephony != null && telephony.isVoiceCapable());
	Xlog.i(TAG, "sIsVoiceCapable : " + sIsVoiceCapable);
        return sIsVoiceCapable;
    }

    private boolean sIsWifiOnly = false;    
    
    private boolean isTDDType = false;
    private boolean isFTTOrTDDType(){
       String mt = SystemProperties.get("gsm.baseband.capability");
       if (mt != null) {
			try {
				int mask = Integer.valueOf(mt);
				if ((mask & MODEM_MASK_TDSCDMA) != 0) {
					Xlog.i(TAG, "MODEM_MASK_TDSCDMA : " + isTDDType);
					isTDDType = true;
				} else if ((mask & MODEM_MASK_WCDMA) != 0) {
					Xlog.i(TAG, "MODEM_MASK_WCDMA : " + isTDDType);
					isTDDType = false;
				} 
			} catch (NumberFormatException e) {
				Xlog.i(TAG, "isFDDType : " + isTDDType);
			}
		}
       return isTDDType;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        isVoiceCapable();
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        sIsWifiOnly = (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
        isFTTOrTDDType();
        ListView mainMenuListView = (ListView) findViewById(R.id.ListView_mainmenu);
        if (mainMenuListView == null) {
            Xlog.w(TAG, "clocwork worked...");
            // not return and let exception happened.
        }
        mainMenuListView.setOnItemClickListener(this);
        mListData = getData();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mListData);

        mainMenuListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView mainMenuListView = (ListView) findViewById(R.id.ListView_mainmenu);

        mListData = getData();
        ListAdapter adap = mainMenuListView.getAdapter();
        if (adap.getCount() == mListData.size()) {
            int i=0;
            for (; i<mListData.size(); i++) {
                if(adap.getItem(i).toString().equals(mListData.get(i))) {
                    continue;
                } else {
                    break;
                }
            }
            if (i==mListData.size()) {
                return;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mListData);

        mainMenuListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SETTINGS_ID, 0, "Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem aMenuItem) {

        switch (aMenuItem.getItemId()) {
        case SETTINGS_ID:
            Intent intent = new Intent(this, Settings.class);
            this.startActivity(intent);
            break;

        }
        return super.onOptionsItemSelected(aMenuItem);
    }

    private List<String> getData() {
        List<String> items = new ArrayList<String>();

        SharedPreferences pre = PreferenceManager
                .getDefaultSharedPreferences(this);

        for (int i = 0; i < itemString.length; i++) {
            if (true == pre.getBoolean(itemString[i], true)) {
                items.add(itemString[i]);
            }
        }
        // items.remove("CPU Freq Test");//coz its driver is not ready.
        items.remove("Modem");// coz its driver is not ready.
        // items.remove("USB");//coz its driver is not ready.
 //       if (ChipSupport.GetChip() != ChipSupport.MTK_6575_SUPPORT) {
 //           items.remove("USB");
 //       }

        if (ChipSupport.GetChip() == ChipSupport.MTK_6516_SUPPORT || 
                ChipSupport.GetChip() == ChipSupport.MTK_6573_SUPPORT) {
            items.remove("USB");
        }
        if(!FeatureOption.MTK_DT_SUPPORT){
        	items.remove("Dual Talk NetworkInfo");
        	items.remove("Dual Talk BandMode");
        }

        // if (FeatureOption.MTK_GEMINI_SUPPORT) {
        // items.remove("Digital Standard");// Only for CMCC single card.
        // }

        // only support TD single-card phone Yu
        if (ModemCategory.GetModemType() != ModemCategory.MODEM_TD
                || FeatureOption.MTK_GEMINI_SUPPORT) {
            items.remove("Digital Standard");
            //items.remove("CFU");
        }

        //if(NfcAdapter.getDefaultAdapter(getApplicationContext()) == null) {
    	   items.remove("NFC");
        //}

        if (!FeatureOption.MTK_LOG2SERVER_APP) {
            
            items.remove("Log2Server");

        }
        
        if (!FeatureOption.MTK_SMSREG_APP) {
            
            items.remove("Device Manager");

        }

        if (ChipSupport.GetChip() != ChipSupport.MTK_6573_SUPPORT) {
            items.remove("CPU Freq Test");
            items.remove("De-Sense");
        }

        if (ChipSupport.GetChip() == ChipSupport.MTK_6516_SUPPORT) {
            items.remove("Power");
        }

        if (false == ChipSupport.IsFeatureSupported(ChipSupport.MTK_FM_SUPPORT)) {
            items.remove("FM Receiver");
            items.remove("FM Transmitter");
        } else {
            if (false == ChipSupport.IsFeatureSupported(ChipSupport.MTK_FM_TX_SUPPORT)) {
                items.remove("FM Transmitter");
            }
        }
        // AGPS is not ready if MTK_AGPS_APP isn't defined
        if (false == ChipSupport.IsFeatureSupported(ChipSupport.MTK_AGPS_APP)
                || false == ChipSupport.IsFeatureSupported(ChipSupport.MTK_GPS_SUPPORT)) {
            items.remove("LocationBasedService");
        }
        if (false == ChipSupport.IsFeatureSupported(ChipSupport.MTK_GPS_SUPPORT)) {

            items.remove("YGPS");
        }
        // MATV is not ready if HAVE_MATV_FEATURE isn't defined
        if (false == ChipSupport
                .IsFeatureSupported(ChipSupport.HAVE_MATV_FEATURE)) {
            items.remove("Matv");
        }
        // BT is not ready if MTK_BT_SUPPORT isn't defined
        if (false == ChipSupport.IsFeatureSupported(ChipSupport.MTK_BT_SUPPORT)) {
            items.remove("Bluetooth");
        }
        // wifi is not ready if MTK_WLAN_SUPPORT isn't defined
        if (false == ChipSupport
                .IsFeatureSupported(ChipSupport.MTK_WLAN_SUPPORT)) {
            items.remove("WiFi");
        }
        if (!FeatureOption.MTK_TVOUT_SUPPORT) {
            items.remove("TV Out");
        }

        if(!sIsVoiceCapable || sIsWifiOnly) {
            items.remove("AutoAnswer");
            items.remove("Repeat Call Test");
            items.remove("VideoTelephony");
        }

        if(sIsWifiOnly){
            items.remove("GPRS");
            items.remove("Modem");
            items.remove("NetworkInfo");
            items.remove("Baseband");
            items.remove("SIMMeLock");
            items.remove("BandMode");
            items.remove("RAT Mode");
            items.remove("SWLA");
            items.remove("ModemTest");
        }

        if (!FeatureOption.MTK_BSP_PACKAGE) {
            items.remove("BSP Telephony Dev");
        }

        if (FeatureOption.EVDO_DT_SUPPORT) {
            //items.remove("SIMMeLock");
            items.remove("Dual Talk NetworkInfo");
            items.remove("Dual Talk BandMode");
        }

         // if(isTDDType){
             // items.remove("RAT Mode");
         // }

        // Test By Jinbo:
        Xlog.w(TAG, "Print items:");
        for (String item : items) {
            Xlog.w(TAG, item);
        }
        return items;
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        try {
            Intent intent = new Intent();
            if ("Audio" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.audio."
                        + mListData.get(arg2));
            } else if ("TouchScreen" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.touchscreen."
                                + mListData.get(arg2));
            } else if ("Camera" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.camera."
                        + mListData.get(arg2));
            } else if ("WiFi" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.wifi."
                        + mListData.get(arg2));
            } else if ("Bluetooth" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.bluetooth.BTList");
            } else if ("NetworkInfo" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.networkinfo."
                                + mListData.get(arg2));
            } else if ("Digital Standard" == mListData.get(arg2)) {
                intent.setClassName(this,
                                "com.mediatek.engineermode.digitalstandard.DigitalStandard");
            } else if ("Device Manager" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.devicemgr.DeviceMgr");
            } else if ("YGPS" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName("com.mediatek.ygps",
                        "com.mediatek.ygps.YGPSActivity"));
            } else if ("LocationBasedService" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName("com.mediatek.lbs.em",
                        "com.mediatek.lbs.em.MyTabActivity"));
            } else if ("VideoTelephony" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.videotelephone."
                                + mListData.get(arg2));
            } else if ("Display" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.display."
                        + mListData.get(arg2));
            } else if ("Memory" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.memory."
                        + mListData.get(arg2));
            } else if ("IO" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.io."
                        + mListData.get(arg2));
            } else if ("Power" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.power."
                        + mListData.get(arg2));
            } else if ("Baseband" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.baseband."
                        + mListData.get(arg2));
            } else if ("FM Transmitter" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName(
                        "com.mediatek.FMTransmitter",
                        "com.mediatek.FMTransmitter.FMTxEMActivity"));
            } else if ("FM Receiver" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName("com.mediatek.FMRadio",
                        "com.mediatek.FMRadio.FMRadioEMActivity"));
            } else if ("De-Sense" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.desense."
                        + "DesenseActivity");
            } else if ("Matv" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName("com.mediatek.matv",
                        "com.mediatek.matv.Matv"));
            } else if ("CPU Freq Test" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.cpufreq.Cpufreq");
            } else if ("CPU Stress Test" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.cpustress.CpuStressTest");
            } else if ("USB" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.usb.USBTest");
            } else if ("SIMMeLock" == mListData.get(arg2)) {
                SimMeLockEntry.StartActivity(intent);
            } else if ("CMMB" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.cmmb."
                        + "CmmbActivity");
            } else if ("SystemLog" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.syslogger.SysLogger");
            } else if ("Modem" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.modem."
                        + "ModemActivity");
            } else if ("ModemTest" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.modemtest."
                                + "ModemTestActivity");
            } else if ("Repeat Call Test" == mListData.get(arg2)) {
                intent.setClassName(this, "com.mediatek.engineermode.phone."
                        + "PhoneAutoTestTool");
            } else if ("BandMode" == mListData.get(arg2)) {
            	
                intent.setClassName(this,
                    "com.mediatek.engineermode.bandselect.BandModeSIMSelect");
            	
            } else if ("Log2Server" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.log2server.DialogSwitch");
            } else if ("NFC" == mListData.get(arg2)) {
                intent.setClassName(this,
			"com.mediatek.engineermode.nfc.NfcEntry");
            } else if ("TV Out" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.tvout.TVOutEntry");
            } else if ("CFU" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.cfu.CfuActivity");
            } else if ("SWLA" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.swla.SwlaActivity");
            } else if ("SDCardTest" == mListData.get(arg2)) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.sdtest.SDLogActivity");
            } else if ("RAT Mode" ==mListData.get(arg2)){
              intent.setClassName(this,
                        "com.mediatek.engineermode.google_phone.SIMSelect");
            } else if ("SettingsFont" == mListData.get(arg2)) {
                intent.setClassName(this,
                       "com.mediatek.engineermode.settingsfontsize.SettingsFontSize");
            } else if ("Debug Utils" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName(
                       "com.mediatek.mobilelog", "com.mediatek.mobilelog.debugtool.DebugToolboxActivity"));
            } else if ("CDS Information" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName(
                       "com.mediatek.connectivity", "com.mediatek.connectivity.CdsInfoActivity"));
            } else if ("Network Setting".equals(mListData.get(arg2))) {
                intent.setClassName(this, "com.mediatek.engineermode.networksetting.NetWorkSettings");
            } else if("Dual Talk NetworkInfo"==mListData.get(arg2)){
            	intent.setClassName(this, "com.mediatek.engineermode.dualtalk_networkinfo.NetworkInfo");
            } else if("Dual Talk BandMode"==mListData.get(arg2)){
            	intent.setClassName(this, "com.mediatek.engineermode.dualtalk_bandselect.BandStart");
	    } else if ("Tag Log".equals(mListData.get(arg2))) {
                intent.setClassName(this, "com.mediatek.engineermode.syslogger.TagLogSwitch");
            } else if ("BSP Telephony Dev" == mListData.get(arg2)) {
                intent.setComponent(new ComponentName("com.mtk.telephony", "com.mtk.telephony.BSPPackageDevToolActivity"));
            } else if ("Thermal" == mListData.get(arg2)){
				intent.setComponent(new ComponentName("com.mediatek.thermalmanager",
	            "com.mediatek.thermalmanager.MTKThermalManagerActivity"));
            } else {
                intent.setClassName(this, "com.mediatek.engineermode."
                        + mListData.get(arg2));
            }
            
            this.startActivity(intent);
            
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Internal Error");
            builder.setMessage("JE, Can not find some packages. Please report to RD. Thx.");
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }
    }
}
