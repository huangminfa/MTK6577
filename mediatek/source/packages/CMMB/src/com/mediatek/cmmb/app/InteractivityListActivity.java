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

package com.mediatek.cmmb.app;

import android.app.ListActivity;
import android.app.NotificationManagerPlus;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mediatek.mbbms.MBBMSStore.SG;

public class InteractivityListActivity extends ListActivity {
    private static final String TAG  = "InteractivityListActivity";
    private static final boolean LOG = true;
    
    private static final int POS_UNKNOWN = -1;
    private static final int TOKEN_INIT = 0;
    private static final int TOKEN_REFRESH = 1;
    
    private InteractivityAdapter mListAdapter;
    private int mServiceRowId;
    private String mServiceName;
    private String mServiceId;
    private String mContentId;
    private boolean mFromSG;
    private int mBestPos;
    private ModeSwitchManager mModeSwitchManager;
    private NotificationManagerPlus mNMP;
    
    private static final String[] PROJECTION = new String[]{
        SG.InteractivityData._ID,                    //0
        SG.InteractivityData.INTERACTIVITY_DATA_ID,  //1
        SG.InteractivityData.DESCRIPTION,            //2
        SG.InteractivityData.HAS_READ,               //3
//        SG.InteractivityData.SERVICE_ID_REF,        //4
//        SG.InteractivityData.CONTENT_ID_REF,        //5
    };
    
    private static final int COL_ROW_ID = 0;
    private static final int COL_DATA_ID = 1;
    private static final int COL_DESCRIPTION = 2;
    private static final int COL_HAS_READ = 3;
//    private static final int COL_SERVICE_ID = 4;
//    private static final int COL_CONTENT_ID = 5;
    
    private void test() {
        Intent intent = new Intent();
        intent.putExtra(Utils.EXTRA_SERVICE_ROW_ID, 1);
        intent.putExtra(Utils.EXTRA_SERVICE_NAME, "test");
        intent.putExtra(Utils.EXTRA_SERVICE_ID, "1");
//        intent.putExtra(Utils.EXTRA_CONTENT_ID, "1");
        setIntent(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.interactivity);
//        test();
        mModeSwitchManager = new ModeSwitchManager(this,null,savedInstanceState);
        mNMP = new NotificationManagerPlus.ManagerBuilder(this).create();
        initialize();
        refreshList(TOKEN_INIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNMP.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNMP.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mModeSwitchManager.onActivityStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mModeSwitchManager.onActivityStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mModeSwitchManager.onSaveInstanceState(outState);
    }
    
    private void initialize() {
        Bundle data = getIntent().getExtras();
        mServiceRowId = data.getInt(Utils.EXTRA_SERVICE_ROW_ID);
        mServiceName = data.getString(Utils.EXTRA_SERVICE_NAME);
        mServiceId = data.getString(Utils.EXTRA_SERVICE_ID);
        mContentId = data.getString(Utils.EXTRA_CONTENT_ID);
        mFromSG = data.getBoolean(Utils.EXTRA_INTERACTIVITY_FROM_SG, false);
        
        setTitle(getString(R.string.service_interactivity, mServiceName));
        
        mListAdapter = new InteractivityAdapter(this, R.layout.interactivity_item, null,
                new String[]{}, new int[]{});
        getListView().setAdapter(mListAdapter);
        getContentResolver().registerContentObserver(
                SG.InteractivityData.CONTENT_URI,
                true, 
                mContentObserver);
        showProgress();
        if (LOG) Log.v(TAG, "initialize() mServiceRowId=" + mServiceRowId
                + ", mServiceId=" + mServiceId + ", mContentId=" + mContentId);
    }

    private ContentObserver mContentObserver = new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange) {
            refreshList(TOKEN_REFRESH);
            if (LOG) Log.v(TAG, "mContentObserver.onChange(" + selfChange + ")");
        }
        
    };
    
    private void refreshList(int token) {
        mListAdapter.getQueryHandler().startQuery(token, 0,
                SG.InteractivityData.CONTENT_URI,
                PROJECTION,
                getWhere(),
                getWhereArgs(),
                null);
    }
    
    private String getWhere() {
        StringBuilder where = new StringBuilder();
        if (mServiceId != null) {
//            where.append(SG.InteractivityData.HAS_READ);
//            where.append("=0 and ");
            where.append(SG.InteractivityData.FROM_SG);
            where.append("=? and ");
            where.append(SG.InteractivityData.SERVICE_ID_REF);
            where.append("=? and ");
            where.append(SG.InteractivityData.CONTENT_ID_REF);
            if (mContentId != null) {
                where.append("=? ");
            } else {
                where.append(" is null ");
            }
        }
        if (LOG) Log.v(TAG, "getWhere() return " + where.toString());
        return where.toString();
    }
    
    private String[] getWhereArgs() {
        String[] args = null;
        String fromSg = mFromSG ? "1" : "0";
        if (mServiceId != null) {
            if (mContentId != null) {
                args = new String[] {fromSg, mServiceId, mContentId};
            } else {
                args = new String[] {fromSg, mServiceId};
            }
        }
        return args;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ViewHolder holder = (ViewHolder) v.getTag();
        gotoDetail(holder.rowID);
    }
    
    private void gotoDetail(long rowId) {
        Intent intent = new Intent(this, InteractivityDetailActivity.class);
        intent.putExtra(Utils.EXTRA_SERVICE_ROW_ID, mServiceRowId);
        intent.putExtra(Utils.EXTRA_SERVICE_ID, mServiceId);
        intent.putExtra(Utils.EXTRA_CONTENT_ID, mContentId);
        intent.putExtra(Utils.EXTRA_INTERACTIVITY_DATA_ROW_ID, rowId);
        startActivityForResult(intent, Utils.REQUEST_CODE_FOR_FINISHED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }

    /*package*/ class InteractivityAdapter extends SimpleCursorAdapter {
        
        /*package*/class QueryHandler extends AsyncQueryHandler {

            /*package*/ QueryHandler(ContentResolver cr) {
                super(cr);
            }
            
            @Override
            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                if (LOG) Log.v(TAG, "onQueryComplete(" + token + "," + cookie + "," + cursor + ")");
                if (cursor == null || cursor.getCount() == 0) {
                    return;
                }
                if (token == TOKEN_INIT) {
                    mBestPos = POS_UNKNOWN;
//                    if (mDataID != null) {
//                        while(cursor.moveToNext()) {
//                            if (mDataID.equalsIgnoreCase(cursor.getString(COL_DATA_ID))) {
//                                mBestPos = cursor.getPosition();
//                                break;
//                            }
//                        }
//                    }
                }
                InteractivityAdapter.this.changeCursor(cursor);
                getListView().setSelection(mBestPos);
                hideProgress();
                if (LOG && cursor != null) Log.i(TAG, "onQueryComplete() mBestPos=" + mBestPos);
            }
            
        }
        
        private QueryHandler mQueryHandler;
        
        /*package*/ QueryHandler getQueryHandler() {
            return mQueryHandler;
        }
        
        /*package*/ InteractivityAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to);
            mQueryHandler = new QueryHandler(context.getContentResolver());
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.rowID = cursor.getLong(COL_ROW_ID);
            holder.dataID = cursor.getString(COL_DATA_ID);
            holder.description.setText(cursor.getString(COL_DESCRIPTION));
            int read = cursor.getInt(COL_HAS_READ);
            if (read == 1) {
                holder.panel.setBackgroundResource(R.drawable.background_read);
            } else {
                holder.panel.setBackgroundResource(R.drawable.background_unread);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            ViewHolder holder = new ViewHolder();
            holder.description = (TextView) v.findViewById(R.id.item_text);
            holder.panel = v.findViewById(R.id.item_panel);
            v.setTag(holder);
            return v;
        }
        
    }
    
    /*package*/ class ViewHolder {
        long rowID;
        String dataID;
        TextView description;
        View panel;
        
        @Override
        public String toString() {
            return new StringBuilder()
            .append("ViewHolder(rowID=")
            .append(rowID)
            .append(", dataID=")
            .append(dataID)
            .append(", description=")
            .append(description)
            .append(")")
            .toString();
        }
    }
    
    private void showProgress() {
        setProgressBarIndeterminateVisibility(true);
    }
    
    private void hideProgress() {
    	setProgressBarIndeterminateVisibility(false);
    }
}
