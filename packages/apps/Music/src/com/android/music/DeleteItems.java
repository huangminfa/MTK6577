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
import android.app.ProgressDialog;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.app.StatusBarManager;

public class DeleteItems extends Activity
{
    private static final String TAG = "DeleteItems";
    private long [] mItemList;

    private static final int PROGRESS_DIALOG_KEY = 0;
    private static final int ALERT_DIALOG_KEY = 1;

    // Status of deleting
    private final static int START_DELETING = 0;
    private final static int FINISH = 1;
    String mDesc = null;
    private Handler mHandler = new Handler() {
    @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_DELETING) {
                // Do the time-consuming job in its own thread to avoid blocking anyone
                new Thread(new Runnable() {
                    public void run() {
                        doDeleteItems();
                    }
                }).start();
            } else if (msg.what == FINISH) {
                //dismissDialog(PROGRESS_DIALOG_KEY);

                // Notify user the deletion is done
                String message = getResources().getQuantityString(
                    R.plurals.NNNtracksdeleted, mItemList.length, Integer.valueOf(mItemList.length));
                Toast.makeText(DeleteItems.this, message, Toast.LENGTH_SHORT).show();

                finish();
            }
        }
    };    

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //requestWindowFeature(Window.FEATURE_LEFT_ICON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle b = getIntent().getExtras();
        mDesc = b.getString("description");
        mItemList = b.getLongArray("items");        
        
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_EJECT);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
    }
    
    private DialogInterface.OnClickListener  mButtonClicked = new DialogInterface.OnClickListener () {
        public void onClick(DialogInterface dialogInterface, int button) {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                showDialog(PROGRESS_DIALOG_KEY);
                Message msg = mHandler.obtainMessage(START_DELETING);
                mHandler.sendMessage(msg);
            } else if (button == DialogInterface.BUTTON_NEUTRAL) {
                finish();
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case PROGRESS_DIALOG_KEY:
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(R.string.delete_progress_title);
            dialog.setMessage(getResources().getString(R.string.delete_progress_message)); 
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
            
        case ALERT_DIALOG_KEY:
            MusicDialog dialog1 = new MusicDialog(this,mButtonClicked,null);
            dialog1.setTitle(R.string.delete_item);
            dialog1.setPositiveButton(getResources().getString(R.string.delete_confirm_button_text));            
            dialog1.setNeutralButton(getResources().getString(R.string.cancel));
            dialog1.setMessage(mDesc);
            dialog1.setCanceledOnTouchOutside(false);
            dialog1.setCancelable(true);
            return dialog1;
            
        default:
            return null;
        }
    }

    private void doDeleteItems() {
        // Do the deletion
        MusicUtils.deleteTracks(DeleteItems.this, mItemList);

        // Tell them we are done
        Message msg = mHandler.obtainMessage(FINISH);
        mHandler.sendMessage(msg);
    }

    @Override
    public void onPause() {
        try {
            StatusBarManager statusBar = (StatusBarManager) 
                    getSystemService(Context.STATUS_BAR_SERVICE);
            statusBar.disable(StatusBarManager.DISABLE_NONE);
        } catch (Exception e) {
            // Just in case
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        showDialog(ALERT_DIALOG_KEY);
        try {
            StatusBarManager statusBar = (StatusBarManager) 
                    getSystemService(Context.STATUS_BAR_SERVICE);
            statusBar.disable(StatusBarManager.DISABLE_EXPAND);
        } catch (Exception e) {
            // Just in case
        }
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
