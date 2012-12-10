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

import android.app.Activity;
import android.app.NotificationManagerPlus;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.mbbms.IMD;
import com.mediatek.mbbms.IMDManager;
import com.mediatek.mbbms.ServerStatus;
import com.mediatek.mbbms.MBBMSStore.SG;

public class InteractivityDetailActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "InteractivityDetailActivity";
    private static final boolean LOG = true;
    
    private static final int POS_UNKNOWN = -1;
    private static final int TOKEN_INIT = 0;
    private static final int TOKEN_REFRESH = 1;
    
    private QueryHandler mQueryHandler;
    private Cursor mCursor;
    
    private int mServiceRowId;
    private String mServiceId;
    private String mContentId;
    private String mGroupId;
    private int mGroupPosition;
    private String mMediaUri;
    private boolean mFromSG;
    private long mCurrentDataRowId = POS_UNKNOWN;
    
    private Button mPrevious;
    private Button mNext;
    private WebView mWeb;
    private TextView mEmpty;
    private IMDDirector mWebProxy;
    private SyncIMDTask mSyncIMDtask;
    private ModeSwitchManager mModeSwitchManager;
    private NotificationManagerPlus mNMP;
    
    private static final String[] PROJECTION = new String[]{
        SG.InteractivityData._ID,                       //0
        SG.InteractivityData.INTERACTIVITY_DATA_ID,     //1
        SG.InteractivityData.DESCRIPTION,               //2
        SG.InteractivityData.HAS_READ,                  //3
        SG.InteractivityData.SERVICE_ID_REF,            //4
        SG.InteractivityData.CONTENT_ID_REF,            //5
        SG.InteractivityData.INTERACTIVITY_MEDIA_DOCUMENT_POINTER, //6
        SG.InteractivityData.INTERACTIVITY_MEDIA_URL,   //7
        SG.InteractivityData.FROM_SG,                   //8
    };
    
    private static final int COL_ROW_ID = 0;
    private static final int COL_DATA_ID = 1;
    private static final int COL_DESCRIPTION = 2;
    private static final int COL_HAS_READ = 3;
    private static final int COL_SERVICE_ID = 4;
    private static final int COL_CONTENT_ID = 5;
    private static final int COL_GROUP_ID = 6;
    private static final int COL_MEDIA_URI = 7;
    private static final int COL_FROM_SG = 8;
    
    private static final int MENU_HALF = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interactivity_detail);
        initialize(savedInstanceState);
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

	private void initialize(Bundle savedInstanceState) {
        if (LOG) Log.v(TAG, "initialize(" + savedInstanceState + ")");
        Bundle data = getIntent().getExtras();
        mServiceRowId = data.getInt(Utils.EXTRA_SERVICE_ROW_ID);
        mServiceId = data.getString(Utils.EXTRA_SERVICE_ID);//service mode
        mContentId = data.getString(Utils.EXTRA_CONTENT_ID);//content mode
        mFromSG = data.getBoolean(Utils.EXTRA_INTERACTIVITY_FROM_SG, false);
        if (mFromSG) {//get it from imd manager
            mGroupId = data.getString(Utils.EXTRA_INTERACTIVITY_GROUP_ID);
            mGroupPosition = data.getInt(Utils.EXTRA_INTERACTIVITY_GROUP_POSITION, POS_UNKNOWN);
        } else {
            updateCurrent(data.getLong(Utils.EXTRA_INTERACTIVITY_DATA_ROW_ID, POS_UNKNOWN));//data mode
        }
        
        mPrevious = (Button) findViewById(R.id.previous);
        mPrevious.setOnClickListener(this);
        mNext = (Button) findViewById(R.id.next);
        mNext.setOnClickListener(this);
        mWeb = (WebView) findViewById(R.id.web);
        mEmpty = (TextView) findViewById(android.R.id.empty);
        //control the web view accoring imd info
        mWebProxy = new IMDDirector(this, null, mWeb);
        mModeSwitchManager = new ModeSwitchManager(this,null,savedInstanceState);
        mNMP = new NotificationManagerPlus.ManagerBuilder(this).create();
        
        mQueryHandler = new QueryHandler(getContentResolver());
        getContentResolver().registerContentObserver(
                SG.InteractivityData.CONTENT_URI,
                true, 
                mContentObserver);
        refreshList(TOKEN_INIT);
        if (LOG) Log.v(TAG, "initialize() mServiceRowId=" + mServiceRowId + ", mServiceId=" + mServiceId
                + " mContentId=" + mContentId);
    }
    
    private void updateCurrent(long dataRowId) {
        if (LOG) Log.v(TAG, "updateCurrent(" + dataRowId + ")");
        mCurrentDataRowId = dataRowId;
//        setTitle(getString(R.string.interactivity_item, mCurrentDataRowId));
    }
    
    private ContentObserver mContentObserver = new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange) {
            refreshList(TOKEN_REFRESH);
            if (LOG) Log.v(TAG, "mContentObserver.onChange(" + selfChange + ")");
        }
        
    };
    
    private void refreshList(int token) {
        if (LOG) Log.v(TAG, "refreshList(" + token + ")");
        mQueryHandler.startQuery(token, 0,
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
    
    private void bindView() {
        if (mCursor == null) {
            mPrevious.setEnabled(false);
            mNext.setEnabled(false);
            showEmpty();
            return;
        }
        mPrevious.setEnabled(!mCursor.isFirst());
        mNext.setEnabled(!mCursor.isLast());
        //rewrite the group id for update and sync imd
        mGroupId = mCursor.getString(COL_GROUP_ID);
        mMediaUri = mCursor.getString(COL_MEDIA_URI);
        
        IMD imd = IMDManager.getInstance(getContentResolver()).get(mGroupId);
        if (LOG) Log.i(TAG, "bindView() imd=" + imd + ", mMediaUri=" + mMediaUri);
        if (imd == null && mMediaUri != null) {
            if (mSyncIMDtask != null) {
                mSyncIMDtask.cancel(true);
            }
            mSyncIMDtask = new SyncIMDTask();
            mSyncIMDtask.execute();
        } else {
            if (imd != null && imd.startGroup != null) {
                showWeb();
                mWebProxy.showIMD(imd);
            } else {
                showEmpty();
                Log.w(TAG, "error imd=" + imd);
            }
        }
    }
    
    public void onClick(View v) {
       switch(v.getId()) {
       case R.id.previous:
           if (mCursor != null) {
               mCursor.moveToPrevious();
               updateCurrent(mCursor.getLong(COL_ROW_ID));
               bindView();
           }
           break;
       case R.id.next:
           if (mCursor != null) {
               mCursor.moveToNext();
               updateCurrent(mCursor.getLong(COL_ROW_ID));
               bindView();
           }
           break;
       default:
           Log.w(TAG, "onClick(" + v + ") unhandled onClick()");
           break;
       }
    }
    
    private void gotoPlayer(boolean half) {
        Intent intent = new Intent();
        intent.setClass(this, PlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Utils.EXTRA_SERVICE_ROW_ID, mServiceRowId);
        intent.putExtra(Utils.EXTRA_HALF_SCREEN, half);//half screen or not
        intent.putExtra(Utils.EXTRA_INTERACTIVITY_GROUP_ID, mGroupId);        
        intent.putExtra(Utils.EXTRA_INTERACTIVITY_GROUP_POSITION, mGroupPosition);        		
        startActivity(intent);
        finishActivity(Utils.REQUEST_CODE_FOR_FINISHED);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (!mFromSG) {//from play screen
            menu.add(0, MENU_HALF, 0, R.string.goto_half);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MENU_HALF == item.getItemId()) {
            gotoPlayer(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /*package*/class QueryHandler extends AsyncQueryHandler {

        /*package*/ QueryHandler(ContentResolver cr) {
            super(cr);
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie,
                Cursor cursor) {
            mCursor = cursor;
            if (mCursor == null || mCursor.getCount() == 0) {
                Log.e(TAG, "Cannot get interactivity data from provider! " + cursor);
                if (cursor != null) {
                    cursor.close();
                }
                mCursor = null;
                bindView();
                return;
            }
            if (mCursor.getCount() > 1) {//show the button
                mPrevious.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
            }
            //find the specified interactivity data
            while(cursor.moveToNext()) {
                if (mCurrentDataRowId != POS_UNKNOWN) {//find row id
                    if (mCurrentDataRowId == mCursor.getLong(COL_ROW_ID)) {
                        break;
                    }
                } else if (mGroupId != null) {//find group id
                    //mGroupId will be assigned from intent firstly.
                    if (mGroupId.equalsIgnoreCase(mCursor.getString(COL_GROUP_ID))) {
                        break;
                    }
                }
            }
            if (cursor.isAfterLast()) {
                //not find the best position
                //normally, it will not occur.
                Log.w(TAG, "Cannot get the correct row! move to first.");
                cursor.moveToFirst();
                updateCurrent(mCursor.getLong(COL_ROW_ID));
            }
            bindView();
            if (LOG && cursor != null) Log.i(TAG, "onQueryComplete() position=" + cursor.getPosition());
        }
        
    }
    
    private ProgressDialog mProgressDialog;
    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(getString(R.string.update_sg_interactivity));
        mProgressDialog.show();
    }
    
    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
    
    private class SyncIMDTask extends AsyncTask<Void, Void, ServerStatus> {

        @Override
        protected void onPreExecute() {
            showProgress();
        }
        
        @Override
        protected ServerStatus doInBackground(Void... params) {
            ServerStatus status = ServiceManager.getServiceManager(InteractivityDetailActivity.this)
                    .doIMDRequest(mServiceId, mContentId, mMediaUri, mGroupId);
            return status;
        }
        
        @Override
        protected void onCancelled() {
            showEmpty();
            hideProgress();
        }
        
        @Override
        protected void onPostExecute(ServerStatus result) {
            if (LOG) Log.v(TAG, "onPostExecute(" + result + ") cancelled=" + isCancelled());
            if (isCancelled()) {
                return;
            }
            hideProgress();
            String message = null;
            if (Utils.isSuccess(result)) {
                message = Utils.getErrorDescription(getResources(), result, null);
                showWeb();
                //Have registed content observer for IMD changes.
                //It will refresh the current IMD list in database
                //After be refreshed, group id will be positioned.
            } else {
                showEmpty();
                message = Utils.getErrorDescription(getResources(), result, getString(R.string.error_sync_interactivity));
            }
            if (message != null) {
                Toast.makeText(InteractivityDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }
        
    }
    
    private void showEmpty() {
        mEmpty.setVisibility(View.VISIBLE);
        mWeb.setVisibility(View.GONE);
    }
    
    private void showWeb() {
        mEmpty.setVisibility(View.GONE);
        mWeb.setVisibility(View.VISIBLE);
    }
}
