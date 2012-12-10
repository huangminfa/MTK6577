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

package com.mediatek.engineermode.syslogger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.featureoption.FeatureOption;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

public class SysLogger extends Activity implements OnItemClickListener {

    private static final String TAG = "Syslog";
    private List<String> mListData;
    final String itemString[] = { "Common UI", "ModemLog", "MobileLog", "NetLog" , "ExtModemLog"};
    ListView mainMenuListView;
    Intent intentHQ, intentMobilelog, intentMdlogger, intentNetlog, intentExtMdlogger ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.systemlogger_list);
        Elog.d(TAG, "ANR test >>  Enter SysLogger onCreate()");
        mainMenuListView = (ListView) findViewById(R.id.ListView_syslogmenu);
        if (mainMenuListView == null) {
            Elog.e("SystemLoggerMain", "clocwork worked...");
            // not return and let exception happened.
        }
        mainMenuListView.setOnItemClickListener(this);
        initIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Elog.d(TAG, "ANR test >>  Enter SysLogger onResume()");
        mListData = getData();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mListData);

        mainMenuListView.setAdapter(adapter);
        Elog.d(TAG, "ANR test >>  Enter SysLogger onResume() end");
    }

    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO Auto-generated method stub

        try {
            if ("Common UI" == mListData.get(position)) {
                Elog.d(TAG, "ANR test >>  click Common UI");
                this.startActivity(intentHQ);
            } else if ("ModemLog" == mListData.get(position)) {
                Elog.d(TAG, "ANR test >>  click ModemLog");
                this.startActivity(intentMdlogger);                
            } else if ("MobileLog" == mListData.get(position)) {
                Elog.d(TAG, "ANR test >>  click MobileLog");
                this.startActivity(intentMobilelog);
            } else if ("NetLog" == mListData.get(position)) {
                Elog.d(TAG, "ANR test >>  click NetLog");
                this.startActivity(intentNetlog);
            } else if ("ExtModemLog" == mListData.get(position)) {
                Elog.d(TAG, "ANR test >>  click ExtModemLog");
                this.startActivity(intentExtMdlogger);
            }


        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Internal Error");
            builder.setMessage("JE, Can not find some packages. Please report to RD. Thx.");
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }

    }

    private List<String> getData() {

        List<String> items = new ArrayList<String>();
        for (int i = 0; i < itemString.length; i++) {
            items.add(itemString[i]);
        }
        if (getPackageManager().resolveActivity(intentHQ, 0) == null) {
            items.remove("Common UI");
        }
        if (getPackageManager().resolveActivity(intentMdlogger, 0) == null) {
            items.remove("ModemLog");
        }
        if (getPackageManager().resolveActivity(intentMobilelog, 0) == null) {
            items.remove("MobileLog");
        }
        if (getPackageManager().resolveActivity(intentNetlog, 0) == null) {
            items.remove("NetLog");
        }
        
        if (!FeatureOption.MTK_DT_SUPPORT || getPackageManager().resolveActivity(intentExtMdlogger, 0) == null) {
            items.remove("ExtModemLog");
            Elog.i(TAG, "FeatureOption.MTK_DT_SUPPORT = " + FeatureOption.MTK_DT_SUPPORT);
        }
        return items;
    }

    private void initIntent() {
        
        intentHQ = new Intent();
        intentMobilelog = new Intent();
        intentMdlogger = new Intent();
        intentNetlog = new Intent();
        intentExtMdlogger = new Intent();

        intentHQ.setComponent(new ComponentName("com.mediatek.engineermode",
                "com.mediatek.engineermode.syslogger.SysCommon"));

        intentMdlogger.setComponent(new ComponentName("com.mediatek.mdlogger",
        "com.mediatek.mdlogger.MDLoggerTest"));
        
        intentMobilelog.setComponent(new ComponentName(
                "com.mediatek.mobilelog", "com.mediatek.mobilelog.MobileLog"));

        intentNetlog.setComponent(new ComponentName(
                "com.android.ActivityNetwork",
                "com.android.ActivityNetwork.ActivityNetwork"));       

        intentExtMdlogger.setComponent(new ComponentName("com.mediatek.extmdlogger",
        "com.mediatek.extmdlogger.ExtMDLoggerTest"));
        

        

    }

}
