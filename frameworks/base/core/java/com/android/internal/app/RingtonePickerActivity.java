/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.app;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The {@link RingtonePickerActivity} allows the user to choose one from all of the
 * available ringtones. The chosen ringtone's URI will be persisted as a string.
 *
 * @see RingtoneManager#ACTION_RINGTONE_PICKER
 */
public final class RingtonePickerActivity extends AlertActivity implements
        AdapterView.OnItemSelectedListener, Runnable, DialogInterface.OnClickListener,
        AlertController.AlertParams.OnPrepareListViewListener {

    private static final String TAG = "RingtonePickerActivity";

    private static final int DELAY_MS_SELECTION_PLAYED = 300;

    private static final String SAVE_CLICKED_POS = "clicked_pos";
    
    private static final int ADD_MORE_RINGTONES = 1;

    private RingtoneManager mRingtoneManager;
    
    private Cursor mCursor;
    private Handler mHandler;

    /** The position in the list of the 'Silent' item. */
    private int mSilentPos = -1;
    
    /** The position in the list of the 'Default' item. */
    private int mDefaultRingtonePos = -1;

    /** The position in the list of the last clicked item. */
    private int mClickedPos = -1;
    
    /** The position in the list of the ringtone to sample. */
    private int mSampleRingtonePos = -1;

    /** Whether this list has the 'Silent' item. */
    private boolean mHasSilentItem;
    
    /** The Uri to place a checkmark next to. */
    private Uri mExistingUri;
    
    /** The number of static items in the list. */
    private int mStaticItemCount;
    
    /** Whether this list has the 'Default' item. */
    private boolean mHasDefaultItem;
    
    /** The Uri to play when the 'Default' item is clicked. */
    private Uri mUriForDefaultItem;
    
    /** Whether this list has the 'More Ringtongs' item. */
    private boolean mHasMoreRingtonesItem = false;
    
    /** The position in the list of the 'More Ringtongs' item. */
    private int mMoreRingtonesPos = -1;
    
    /** The ringtone type to show and add in the list. */
    private int mType = -1;
    /**
     * A Ringtone for the default ringtone. In most cases, the RingtoneManager
     * will stop the previous ringtone. However, the RingtoneManager doesn't
     * manage the default ringtone for us, so we should stop this one manually.
     */
    private Ringtone mDefaultRingtone;
    
    private DialogInterface.OnClickListener mRingtoneClickListener =
            new DialogInterface.OnClickListener() {

        /*
         * On item clicked
         */
        public void onClick(DialogInterface dialog, int which) {
            
            if (which == mMoreRingtonesPos){
                //Show MusicPicker activity to let user choose song to be ringtone 
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.setType("application/ogg");
                intent.setType("application/x-ogg");
                startActivityForResult(intent, ADD_MORE_RINGTONES);
            } else {
                // Save the position of most recently clicked item
                mClickedPos = which;
                // Play clip
                playRingtone(which, 0);
            }
        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        Intent intent = getIntent();

        /*
         * Get whether to show the 'Default' item, and the URI to play when the
         * default is clicked
         */
        mHasDefaultItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        mUriForDefaultItem = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        if (mUriForDefaultItem == null) {
            mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
        }

        if (savedInstanceState != null) {
            mClickedPos = savedInstanceState.getInt(SAVE_CLICKED_POS, -1);
        }
        // Get whether to show the 'Silent' item
        mHasSilentItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        
        // Get whether to show the 'More Ringtones' item
        mHasMoreRingtonesItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_MORE_RINGTONES, false);
        
        // Give the Activity so it can do managed queries
        mRingtoneManager = new RingtoneManager(this);

        // Get whether to include DRM ringtones
        boolean includeDrm = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM,
                true);
        mRingtoneManager.setIncludeDrm(includeDrm);
        
        // Get the type of ringtones to show
        mType = intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
        if (mType != -1) {
            mRingtoneManager.setType(mType);
        }
        
        mCursor = mRingtoneManager.getCursor();
        
        // The volume keys will control the stream that we are choosing a ringtone for
        setVolumeControlStream(mRingtoneManager.inferStreamType());

        // Get the URI whose list item should have a checkmark
        mExistingUri = intent
                .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);

        final AlertController.AlertParams p = mAlertParams;
        p.mCursor = mCursor;
        p.mOnClickListener = mRingtoneClickListener;
        p.mLabelColumn = MediaStore.Audio.Media.TITLE;
        p.mIsSingleChoice = true;
        p.mOnItemSelectedListener = this;
        p.mPositiveButtonText = getString(com.android.internal.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
        p.mPositiveButtonListener = this;
        p.mOnPrepareListViewListener = this;

        p.mTitle = intent.getCharSequenceExtra(RingtoneManager.EXTRA_RINGTONE_TITLE);
        if (p.mTitle == null) {
            p.mTitle = getString(com.android.internal.R.string.ringtone_picker_title);
        }
        
        setupAlert();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CLICKED_POS, mClickedPos);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: savedInstanceState = " + savedInstanceState + ",mClickedPos = " +
                mClickedPos + ",this = " + this);
        super.onRestoreInstanceState(savedInstanceState);
        mClickedPos = savedInstanceState.getInt(SAVE_CLICKED_POS, mClickedPos);
    }

    public void onPrepareListView(ListView listView) {
        Log.d(TAG, "onPrepareListView>>>: mClickedPos = " + mClickedPos);
        if (mHasDefaultItem) {
            mDefaultRingtonePos = addDefaultRingtoneItem(listView);
            
            if (RingtoneManager.isDefault(mExistingUri)) {
                mClickedPos = mDefaultRingtonePos;
            }
        }
        // Add "More Ringtone" to the top of listview to let user choose more ringtone
        if (mHasMoreRingtonesItem) {
            mMoreRingtonesPos = addMoreRingtonesItem(listView);
        }
        
        if (mHasSilentItem) {
            mSilentPos = addSilentItem(listView);
            
            // The 'Silent' item should use a null Uri
            if (mExistingUri == null) {
                mClickedPos = mSilentPos;
            }
        }

        if (mClickedPos == -1) {
            mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mExistingUri));
        }
        
        // Put a checkmark next to an item.
        mAlertParams.mCheckedItem = mClickedPos;
        Log.d(TAG, "onPrepareListView<<<: mClickedPos = " + mClickedPos);
    }

    /**
     * Adds a static item to the top of the list. A static item is one that is not from the
     * RingtoneManager.
     * 
     * @param listView The ListView to add to.
     * @param textResId The resource ID of the text for the item.
     * @return The position of the inserted item.
     */
    private int addStaticItem(ListView listView, int textResId) {
        TextView textView = (TextView) getLayoutInflater().inflate(
                com.android.internal.R.layout.select_dialog_singlechoice_holo, listView, false);
        textView.setText(textResId);
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }
    
    private int addDefaultRingtoneItem(ListView listView) {
        return addStaticItem(listView, com.android.internal.R.string.ringtone_default);
    }
    
    private int addSilentItem(ListView listView) {
        return addStaticItem(listView, com.android.internal.R.string.ringtone_silent);
    }
    
    private int addMoreRingtonesItem(ListView listView) {
        TextView textView = (TextView) getLayoutInflater().inflate(
                com.android.internal.R.layout.simple_list_item_1, listView, false);
        textView.setText(com.android.internal.R.string.Add_More_Ringtones);
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }
    
    /*
     * On click of Ok/Cancel buttons
     */
    public void onClick(DialogInterface dialog, int which) {
        boolean positiveResult = which == DialogInterface.BUTTON_POSITIVE;
        
        // Stop playing the previous ringtone
        mRingtoneManager.stopPreviousRingtone();
        
        if (positiveResult) {
            Intent resultIntent = new Intent();
            Uri uri = null;
            
            if (mClickedPos == mDefaultRingtonePos) {
                // Set it to the default Uri that they originally gave us
                uri = mUriForDefaultItem;
            } else if (mClickedPos == mSilentPos) {
                // A null Uri is for the 'Silent' item
                uri = null;
            } else {
                uri = mRingtoneManager.getRingtoneUri(getRingtoneManagerPosition(mClickedPos));
            }

            resultIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        getWindow().getDecorView().post(new Runnable() {
            public void run() {
                mCursor.deactivate();
            }
        });

        finish();
    }
    
    /*
     * On item selected via keys
     */
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        playRingtone(position, DELAY_MS_SELECTION_PLAYED);
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private void playRingtone(int position, int delayMs) {
        mHandler.removeCallbacks(this);
        mSampleRingtonePos = position;
        mHandler.postDelayed(this, delayMs);
    }
    
    public void run() {
        
        if (mSampleRingtonePos == mSilentPos) {
            mRingtoneManager.stopPreviousRingtone();
            return;
        }
        
        /*
         * Stop the default ringtone, if it's playing (other ringtones will be
         * stopped by the RingtoneManager when we get another Ringtone from it.
         */
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
            mDefaultRingtone = null;
        }
        
        Ringtone ringtone;
        if (mSampleRingtonePos == mDefaultRingtonePos) {
            if (mDefaultRingtone == null) {
                mDefaultRingtone = RingtoneManager.getRingtone(this, mUriForDefaultItem);
            }
            ringtone = mDefaultRingtone;
            
            /*
             * Normally the non-static RingtoneManager.getRingtone stops the
             * previous ringtone, but we're getting the default ringtone outside
             * of the RingtoneManager instance, so let's stop the previous
             * ringtone manually.
             */
            mRingtoneManager.stopPreviousRingtone();
            
        } else {
            ringtone = mRingtoneManager.getRingtone(getRingtoneManagerPosition(mSampleRingtonePos));
        }
        
        if (ringtone != null) {
            ringtone.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAnyPlayingRingtone();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAnyPlayingRingtone();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        super.onDestroy();
    }
    
    private void stopAnyPlayingRingtone() {

        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
        }
        
        if (mRingtoneManager != null) {
            mRingtoneManager.stopPreviousRingtone();
        }
    }
    
    private int getRingtoneManagerPosition(int listPos) {
        return listPos - mStaticItemCount;
    }
    
    private int getListPosition(int ringtoneManagerPos) {
        
        // If the manager position is -1 (for not found), return that
        if (ringtoneManagerPos < 0) return ringtoneManagerPos;
        
        return ringtoneManagerPos + mStaticItemCount;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ADD_MORE_RINGTONES:
                ListView listView = mAlert.getListView();
                if (null == listView) {
                    Log.e(TAG, "onActivityResult: listview is null, return!");
                    return;
                }
                if (resultCode == RESULT_OK) {
                    Uri uri = (null == intent ? null : intent.getData());
                    if (uri != null ) {
                        setRingtone(this, uri);
                        Log.v(TAG, "onActivityResult: RESULT_OK, so set to be ringtone!");
                        
                    }
                    // Reset the checked position after choosing a song to be ringtone
                    ListAdapter adapter = listView.getAdapter();
                    ListAdapter headAdapter = adapter;
                    if (null != headAdapter && (headAdapter instanceof HeaderViewListAdapter)) {
                        // Get the cursor adapter with the listview
                        adapter = ((HeaderViewListAdapter) headAdapter).getWrappedAdapter();
                        mCursor = mRingtoneManager.getNewCursor();
                        ((SimpleCursorAdapter) adapter).changeCursor(mCursor);
                        Log.d(TAG, "onActivityResult: notify adapter update listview with new cursor!");
                    } else {
                        Log.e(TAG, "onActivityResult: cursor adapter is null!");
                    }
                    // Get position from ringtone list with this uri, if the return position is
                    // valid value, set it to be current clicked position
                    int position = getListPosition(mRingtoneManager.getRingtonePosition(uri));
                    Log.d(TAG, "onActivityResult: get the position of uri = " + uri
                            + ", position = " + position);
                    if (position != -1) {
                        mClickedPos = position;
                        mAlertParams.mCheckedItem = mClickedPos;
                    } else {
                        Log.w(TAG, "onActivityResult: get position is invalid!");
                    }
                } else {
                    Log.v(TAG, "onActivityResult: Cancel to choose more ringtones, so do nothing!");
                }
                listView.setItemChecked(mClickedPos, true);
                listView.setSelection(mClickedPos);
                Log.d(TAG, "onActivityResult: set position to be checked: mClickedPos = "
                        + mClickedPos);
                break;
        }
    }
    
    private void setRingtone(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        // Set the flag in the database to mark this as a ringtone
        try {
            if (mType == -1) {
                Log.e(TAG, "Unknown ringtone type = " + mType);
                return;
            }
            ContentValues values = new ContentValues(1);
            if ((RingtoneManager.TYPE_RINGTONE == mType) || (RingtoneManager.TYPE_VIDEO_CALL == mType)) {
                values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
            } else if (RingtoneManager.TYPE_ALARM == mType) {
                values.put(MediaStore.Audio.Media.IS_ALARM, "1");
            } else if (RingtoneManager.TYPE_NOTIFICATION == mType) {
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, "1");
            } else {
                Log.e(TAG, "Unsupport ringtone type =  " + mType);
                return;
            }
            resolver.update(uri, values, null, null);
        } catch (UnsupportedOperationException ex) {
            // most likely the card just got unmounted
            Log.e(TAG, "couldn't set ringtone flag for uri " + uri);
            return;
        }
    }
    
}
