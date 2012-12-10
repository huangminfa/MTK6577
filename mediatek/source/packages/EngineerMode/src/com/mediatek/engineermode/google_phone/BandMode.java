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

package com.mediatek.engineermode.google_phone;


import com.mediatek.engineermode.R;

import android.app.Activity;
import android.app.AlertDialog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.AsyncResult;
import android.util.Log;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;


/**
 * Radio Band Mode Selection Class
 *
 * It will query baseband about all available band modes and display them
 * in screen. It will display all six band modes if the query failed.
 *
 * After user select one band, it will send the selection to baseband.
 *
 * It will alter user the result of select operation and exit, no matter success
 * or not.
 *
 */
public class BandMode extends Activity {
    private static final String LOG_TAG = "phone";
    private static final boolean DBG = false;

    private static final int EVENT_BAND_SCAN_COMPLETED = 100;
    private static final int EVENT_BAND_SELECTION_DONE = 200;

    private static final String[] BAND_NAMES = new String[] {
            "Automatic",
            "EURO Band",
            "USA Band",
            "JAPAN Band",
            "AUS Band",
            "AUS2 Band"
    };

    private ListView mBandList;
    private ArrayAdapter mBandListAdapter;
    private BandListItem mTargetBand = null;
    private DialogInterface mProgressPanel;

    private Phone mPhone = null;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.band_mode);

        setTitle(getString(R.string.band_mode_title));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);

        mPhone = PhoneFactory.getDefaultPhone();

        mBandList = (ListView) findViewById(R.id.band);
        mBandListAdapter = new ArrayAdapter<BandListItem>(this,
                android.R.layout.simple_list_item_1);
        mBandList.setAdapter(mBandListAdapter);
        mBandList.setOnItemClickListener(mBandSelectionHandler);



        loadBandList();
    }

    private AdapterView.OnItemClickListener mBandSelectionHandler =
            new AdapterView.OnItemClickListener () {
                public void onItemClick(AdapterView parent, View v,
                        int position, long id) {

                    getWindow().setFeatureInt(
                            Window.FEATURE_INDETERMINATE_PROGRESS,
                            Window.PROGRESS_VISIBILITY_ON);

                    mTargetBand = (BandListItem) parent.getAdapter().getItem(position);

                    if (DBG) log("Select band : " + mTargetBand.toString());

                    Message msg =
                            mHandler.obtainMessage(EVENT_BAND_SELECTION_DONE);
                    mPhone.setBandMode(mTargetBand.getBand(), msg);
                }
            };

    static private class BandListItem {
        private int mBandMode = Phone.BM_UNSPECIFIED;

        public BandListItem(int bm) {
            mBandMode = bm;
        }

        public int getBand() {
            return mBandMode;
        }

        public String toString() {
            return BAND_NAMES[mBandMode];
        }
    }

    private void loadBandList() {
        String str = getString(R.string.band_mode_loading);

        if (DBG) log(str);


        //ProgressDialog.show(this, null, str, true, true, null);
        mProgressPanel = new AlertDialog.Builder(this)
            .setMessage(str)
            .show();

        Message msg = mHandler.obtainMessage(EVENT_BAND_SCAN_COMPLETED);
        mPhone.queryAvailableBandMode(msg);

    }

    private void bandListLoaded(AsyncResult result) {
        if (DBG) log("network list loaded");

        if (mProgressPanel != null) mProgressPanel.dismiss();

        clearList();

        boolean addBandSuccess = false;
        BandListItem item;

        if (result.result != null) {
            int bands[] = (int[])result.result;
            int size = bands[0];

            if (size > 0) {
                for (int i=1; i<size; i++) {
                    item = new BandListItem(bands[i]);
                    mBandListAdapter.add(item);
                    if (DBG) log("Add " + item.toString());
                }
                addBandSuccess = true;
            }
        }

        if (addBandSuccess == false) {
            if (DBG) log("Error in query, add default list");
            for (int i=0; i<Phone.BM_BOUNDARY; i++) {
                item = new BandListItem(i);
                mBandListAdapter.add(item);
                if (DBG) log("Add default " + item.toString());
            }
        }
        mBandList.requestFocus();
    }

    private void displayBandSelectionResult(Throwable ex) {
        String status = getString(R.string.band_mode_set)
                +" [" + mTargetBand.toString() + "] ";

        if (ex != null) {
            status = status + getString(R.string.band_mode_failed);
        } else {
            status = status + getString(R.string.band_mode_succeeded);
        }

        mProgressPanel = new AlertDialog.Builder(this)
            .setMessage(status)
            .setPositiveButton(android.R.string.ok, null).show();
    }

    private void clearList() {
        while(mBandListAdapter.getCount() > 0) {
            mBandListAdapter.remove(
                    mBandListAdapter.getItem(0));
        }
    }

    private void log(String msg) {
        Log.d(LOG_TAG, "[BandsList] " + msg);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_BAND_SCAN_COMPLETED:
                    ar = (AsyncResult) msg.obj;

                    bandListLoaded(ar);
                    break;

                case EVENT_BAND_SELECTION_DONE:
                    ar = (AsyncResult) msg.obj;

                    getWindow().setFeatureInt(
                            Window.FEATURE_INDETERMINATE_PROGRESS,
                            Window.PROGRESS_VISIBILITY_OFF);

                    displayBandSelectionResult(ar.exception);
                    break;
            }
        }
    };


}
