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

package com.mediatek.engineermode.desense;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.desense.EMDsense.PLLStruct;

public class DesensePllsActivity extends Activity implements OnItemClickListener {

    private String[] mNameArray;
    private int[] mIdArray;
    private String[] mValueArray;

    private List<String> mListData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.desense_plls_activity);

        if (!initData()) {
            Toast.makeText(this, "Can not get data!", Toast.LENGTH_SHORT).show();
            return;
        }

        ListView pllMenuListView = (ListView) findViewById(R.id.pll_menu_listview);
        pllMenuListView.setOnItemClickListener(this);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListData);
        
        pllMenuListView.setAdapter(adapter);

    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, PllDetailActivity.class);
        intent.putExtra("name", mNameArray[position]);
        intent.putExtra("id", mIdArray[position]);
        intent.putExtra("value", mValueArray[position]);
        startActivity(intent);
        
    }

    private boolean initData() {
        try {
            ArrayList<PLLStruct> list = EMDsense.PLLGetAllInfo();

            // if (null == list) {
            // list = new ArrayList<PLLStruct>();
            // }

            // if (0 == list.size()) { // only for test
            // PLLStruct struct1 = new PLLStruct();
            // struct1.id = 1;
            // struct1.name = "pll_1";
            // struct1.val = 200;
            //            
            // list.add(struct1);
            // PLLStruct struct2 = new PLLStruct();
            // struct2.id = 2;
            // struct2.name = "pll_2";
            // struct2.val = 300;
            // list.add(struct2);
            // }

            int size = list.size();
            Elog.v(DesenseActivity.TAG, "PLLGetAllInfo list size = " + size);
            mNameArray = new String[size];
            mIdArray = new int[size];
            mValueArray = new String[size];

            mListData = new ArrayList<String>();
            
            for (int i = 0; i < size; i++) {
                PLLStruct item = list.get(i);
                mNameArray[i] = item.name;
                mListData.add(item.name);
                mIdArray[i] = item.id;
                mValueArray[i] = item.hexVal;
                Elog.v(DesenseActivity.TAG, "PLLGetAllInfo name = " + item.name);
                Elog.v(DesenseActivity.TAG, "PLLGetAllInfo id = " + item.id);
                Elog.v(DesenseActivity.TAG, "PLLGetAllInfo val = " + item.hexVal);
                Elog.v(DesenseActivity.TAG, "PLLGetAllInfo list.get(i) = " + list.get(i).toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Elog.v(DesenseActivity.TAG, "DesensePllsActivity initData() Exception = " + e.toString());
            return false;
        }

    }

}
// extends ListActivity {
//
// private MyAdapter mAdapter;
// private String[] mNameArray;
// private LayoutInflater inflater;
// private int[] mIdArray;
// private String[] mValueArray;
//
// @Override
// protected void onCreate(Bundle savedInstanceState) {
// // TODO Auto-generated method stub
// super.onCreate(savedInstanceState);
// this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
// setContentView(R.layout.desense_plls_activity);
//
// if (!initData()) {
// Toast.makeText(this, "Can not get data!", Toast.LENGTH_SHORT).show();
// return;
// }
//
// mAdapter = new MyAdapter(mNameArray, mIdArray, mValueArray, inflater, this);
// Log.v(DesenseActivity.TAG, "onCreate-----------------------");
//
// setListAdapter(mAdapter);
// }
//
// private boolean initData() {
// try {
// inflater = this.getLayoutInflater();
// ArrayList<PLLStruct> list = EMDsense.PLLGetAllInfo();
//
// // if (null == list) {
// // list = new ArrayList<PLLStruct>();
// // }
//
// // if (0 == list.size()) { // only for test
// // PLLStruct struct1 = new PLLStruct();
// // struct1.id = 1;
// // struct1.name = "pll_1";
// // struct1.val = 200;
// //
// // list.add(struct1);
// // PLLStruct struct2 = new PLLStruct();
// // struct2.id = 2;
// // struct2.name = "pll_2";
// // struct2.val = 300;
// // list.add(struct2);
// // }
//
// int size = list.size();
// Log.v(DesenseActivity.TAG, "PLLGetAllInfo list size = " + size);
// mNameArray = new String[size];
// mIdArray = new int[size];
// mValueArray = new String[size];
//
// for (int i = 0; i < size; i++) {
// PLLStruct item = list.get(i);
// mNameArray[i] = item.name;
// mIdArray[i] = item.id;
// mValueArray[i] = item.hexVal;
// Log.v(DesenseActivity.TAG, "PLLGetAllInfo name = " + item.name);
// Log.v(DesenseActivity.TAG, "PLLGetAllInfo id = " + item.id);
// Log.v(DesenseActivity.TAG, "PLLGetAllInfo val = " + item.hexVal);
// Log.v(DesenseActivity.TAG, "PLLGetAllInfo list.get(i) = " +
// list.get(i).toString());
// }
// return true;
// } catch (Exception e) {
// e.printStackTrace();
// Log.v(DesenseActivity.TAG, "DesensePllsActivity initData() Exception = " +
// e.toString());
// return false;
// }
//
// }
//
// }
