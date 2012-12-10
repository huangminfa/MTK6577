/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.music;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RenamePlaylist extends Activity
{
    private static final String TAG = "RenamePlaylist";
    private static final int ALERT_DIALOG_KEY = 0;
    private View mView;
    private EditText mPlaylist;
    private String mPrompt;
    private Button mSaveButton;
    private long mRenameId;
    private long mExistingId;
    private String mOriginalName;
    private MusicDialog mDialog;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        MusicLogUtils.d(TAG, "onCreate");
        
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_NO_TITLE);        
        mView = getLayoutInflater().inflate(R.layout.create_playlist, null);          
        mPlaylist = (EditText)mView.findViewById(R.id.playlist);


        mRenameId = icicle != null ? icicle.getLong("rename")
                : getIntent().getLongExtra("rename", -1);
        mExistingId = icicle != null ? icicle.getLong("existing", -1)
                : getIntent().getLongExtra("existing", -1);
        mOriginalName = nameForId(mRenameId);
        String defaultname = icicle != null ? icicle.getString("defaultname") : mOriginalName;
        
        if (mRenameId < 0 || mOriginalName == null || defaultname == null) {
            MusicLogUtils.i(TAG, "Rename failed: " + mRenameId + "/" + defaultname);
            finish();
            return;
        }
        
        String promptformat;
        if (mOriginalName.equals(defaultname)) {
            promptformat = getString(R.string.rename_playlist_same_prompt);
        } else {
            promptformat = getString(R.string.rename_playlist_diff_prompt);
        }
                
        mPrompt = String.format(promptformat, mOriginalName, defaultname);
        mPlaylist.setText(defaultname);
        mPlaylist.setSelection(defaultname.length());
        mPlaylist.addTextChangedListener(mTextWatcher);
        
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_EJECT);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
    }
    
    TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // don't care about this one
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // check if playlist with current name exists already, and warn the user if so.
            setSaveButton();
        };
        public void afterTextChanged(Editable s) {
            // don't care about this one
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        MusicLogUtils.d(TAG, "onCreateDialog id=" + id);
        switch (id) {            
        case ALERT_DIALOG_KEY:
            mDialog = new MusicDialog(this,mButtonClicked, mView);
            if (mDialog != null) {
                mDialog.setTitle(mPrompt);
                mDialog.setPositiveButton(getResources().getString(R.string.delete_confirm_button_text));            
                mDialog.setNeutralButton(getResources().getString(R.string.cancel));
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setCancelable(true);
                return mDialog;
            }
            MusicLogUtils.d(TAG, "onCreateDialog fail");
        default:
            return null;
        }
    }
    
    private DialogInterface.OnClickListener  mButtonClicked = new DialogInterface.OnClickListener () {
        public void onClick(DialogInterface mDialogInterface, int button) {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                String name = mPlaylist.getText().toString();
                if (name != null && name.length() > 0) {
                    ContentResolver resolver = getContentResolver();
                    if (mExistingId >= 0) {
                        // There is another playlist which has the same name with renamed one
                        // we should overwrite existing one, i.e. delete it from database
                        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                MediaStore.Audio.Playlists._ID + "=?",
                                new String[] {Long.valueOf(mExistingId).toString()});
                        MusicLogUtils.d(TAG, "to overwrite, delete the existing one");
                    }
                    ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Audio.Playlists.NAME, name);
                    resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                            values,
                            MediaStore.Audio.Playlists._ID + "=?",
                            new String[] { Long.valueOf(mRenameId).toString()});
                    
                    setResult(RESULT_OK);
                    Toast.makeText(RenamePlaylist.this, R.string.playlist_renamed_message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (button == DialogInterface.BUTTON_NEUTRAL) {
                finish();
            }
        }
    };

    private void setSaveButton() {
        String typedname = mPlaylist.getText().toString();
        MusicLogUtils.d(TAG, "setSaveButton " + mSaveButton);        
        if (mSaveButton == null) {
            if (mDialog != null) {
                 mSaveButton = (Button)mDialog.getPositiveButton();
            } else {
                return;
            }
        }
        
        if (mSaveButton != null) {
            if (typedname.trim().length() == 0) {
                mSaveButton.setEnabled(false);
            } else {
                mSaveButton.setEnabled(true);
                    final long id = idForplaylist(typedname);
                    if (id >= 0 && !mOriginalName.equals(typedname)) {
                    mSaveButton.setText(R.string.create_playlist_overwrite_text);
                        mExistingId = id;
                } else {
                    mSaveButton.setText(R.string.create_playlist_create_text);
                        mExistingId = -1;
                }
            }
        } else {
            MusicLogUtils.e(TAG, "setSaveButton with null SaveButton!");
        }
    }
    
    private int idForplaylist(String name) {
        Cursor c = MusicUtils.query(this, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists._ID },
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[] { name },
                MediaStore.Audio.Playlists.NAME);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }
            c.close();
            c = null;
        }
        return id;
    }
    
    private String nameForId(long id) {
        Cursor c = MusicUtils.query(this, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists.NAME },
                MediaStore.Audio.Playlists._ID + "=?",
                new String[] { Long.valueOf(id).toString() },
                MediaStore.Audio.Playlists.NAME);
        String name = null;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                name = c.getString(0);
            }
        }
        c.close();
        return name;
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outcicle) {
        outcicle.putString("defaultname", mPlaylist.getText().toString());
        outcicle.putLong("rename", mRenameId);
        outcicle.putLong("existing", mExistingId);
    }
    
    @Override
    public void onResume() {
        //MusicLogUtils.d(TAG, "onResume");
        showDialog(ALERT_DIALOG_KEY);
        setSaveButton();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        unregisterReceiverSafe(mScanListener);
        super.onDestroy();
    }
    
    /*
     * This listener gets called when the media scanner starts up or finishes, and
     * when the sd card is unmounted.
     */
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MusicLogUtils.d(TAG, "mScanListener.onReceive:" + action);
            if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                // When SD card is eject or unmounted, finish the delete activity
                finish();
                MusicLogUtils.d(TAG, "SD card is ejected, finish delete activity!");
            } 
        }
    };
    
    /**
     * Unregister a receiver, but eat the exception that is thrown if the
     * receiver was never registered to begin with. This is a little easier
     * than keeping track of whether the receivers have actually been
     * registered by the time onDestroy() is called.
     */
    private void unregisterReceiverSafe(BroadcastReceiver receiver) {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }
    
}
