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

package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.IContentProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.android.internal.os.storage.ExternalStorageFormatter;

/**
 * Confirm and execute a format of the sdcard.
 * Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE SD CARD" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MediaFormat extends Activity {

    private static final String TAG = "MediaFormat";

    private static final int KEYGUARD_REQUEST = 55;

    private LayoutInflater mInflater;

    private View mInitialView;
    private Button mInitiateButton;

    private View mFinalView;
    private Button mFinalButton;
    
    private StorageVolume mStorageVolume;
    private String mVolumeDescription;
    private String mVolumePath;

	private ContentResolver mContentResolver;
    private final String [] MTP_PROJECTION = {
	    MediaStore.MTP_TRANSFER_FILE_PATH
	};

    private boolean mIsInternalSD;

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Mount Service to format the SD card.
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
            public void onClick(View v) {

            	if (Utils.isMonkeyRunning()) {
                    return;
                }
            	
                if(getMtpStatus()) {
                	Toast.makeText(MediaFormat.this, getVolumeString(R.string.mtp_transfer_erase_error_text), Toast.LENGTH_SHORT).show();
                	finish();
                } else {
                    Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
                    intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                    // Transfer the storage volume to the new intent
                    intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mStorageVolume);
                    startService(intent);
                    finish();
                }
            }
        };

    /**
     *  Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     */
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(this)
                .launchConfirmationActivity(request,
                        getText(R.string.media_format_gesture_prompt),
                        getVolumeString(R.string.media_format_gesture_explanation));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            establishFinalConfirmationState();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else {
            establishInitialState();
        }
    }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private Button.OnClickListener mInitiateListener = new Button.OnClickListener() {
            public void onClick(View v) {
                if(getMtpStatus()) {
                	Toast.makeText(MediaFormat.this, getVolumeString(R.string.mtp_transfer_erase_error_text), Toast.LENGTH_SHORT).show();
                	finish();
                } else {
                	if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                        establishFinalConfirmationState();
                    }
                }
            }
        };

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        if (mFinalView == null) {
            mFinalView = mInflater.inflate(R.layout.media_format_final, null);
            
            TextView FinalText = (TextView) mFinalView.findViewById(R.id.execute_media_format_textview);
            FinalText.setText(getVolumeString(R.string.media_format_final_desc));
            
            mFinalButton =
                    (Button) mFinalView.findViewById(R.id.execute_media_format);
            mFinalButton.setText(R.string.media_format_final_button_text);
            mFinalButton.setOnClickListener(mFinalClickListener);
        }

        setContentView(mFinalView);
    }

    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        if (mInitialView == null) {
            mInitialView = mInflater.inflate(R.layout.media_format_primary, null);
            
            TextView InitialText = (TextView) mInitialView.findViewById(R.id.initiate_media_format_textview);
            InitialText.setText(getVolumeString(R.string.media_format_desc));
            
            mInitiateButton =
                    (Button) mInitialView.findViewById(R.id.initiate_media_format);
            mInitiateButton.setText(getVolumeString(R.string.media_format_button_text));
            mInitiateButton.setOnClickListener(mInitiateListener);
        }

        setContentView(mInitialView);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mInitialView = null;
        mFinalView = null;
        mInflater = LayoutInflater.from(this);
        mStorageVolume = getIntent().getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
        if(mStorageVolume != null) {
            mVolumeDescription = mStorageVolume.getDescription();
            mVolumePath = mStorageVolume.getPath();
            mIsInternalSD = !mStorageVolume.isRemovable();
        }
        
        mContentResolver = getContentResolver();

        setTitle(getVolumeString(R.string.media_format_title));
        establishInitialState();
    }

    /** Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();

        if (!isFinishing()) {
            establishInitialState();
        }
    }

    //query the MTP status and update UI
    private String getMtpPath() {        
 	   Cursor cur = null;
 	   String path = null;
 	   
 	   cur = mContentResolver.query(MediaStore.getMtpTransferFileUri(), MTP_PROJECTION, null, null, null);
 	   if(cur != null){	
 	       if(cur.getCount() == 0 || cur.getCount() > 1){
                Log.w(TAG, "no record or more than one record");
                path = "ERROR";
             } else {
                 cur.moveToFirst();
 				path = cur.getString(0);
             }
 	   } 
        if (cur != null) {
            cur.close();
        }

        //if MTP transfer is over or there is no transfer, path = null;
        return path;
    }
    
    private boolean getMtpStatus() {
 	   String MtpPath = getMtpPath();
 	   Log.d(TAG, "Mtp transfer path" + MtpPath);
 	   
 	   //MTP is transferring
 	   if(MtpPath != null && !(MtpPath.equals("ERROR"))) {
 	       if (MtpPath.contains(mVolumePath + "/")) {
                return true;
 	       }
 	   } 
 	   
 	   return false;
    }

    private String getVolumeString(int StringId) {
    	if(mVolumeDescription == null || !mIsInternalSD) {
    		return getString(StringId);
    	}
    	
    	String sdCardString;
    	if(getString(StringId).getBytes().length - getString(StringId).length() > 0) {
    		sdCardString = " " + getString(R.string.sdcard_setting);
    		Log.d(TAG, sdCardString);
    	} else {
    		sdCardString = getString(R.string.sdcard_setting);
    	}
    	
    	String str = getString(StringId).replace(sdCardString, mVolumeDescription);
    	if(str != null && str.equals(sdCardString)) {
    		sdCardString = sdCardString.substring(0, 1).toLowerCase() + sdCardString.substring(1);
    		Log.d(TAG, "Upper + Lower sdcard string" + sdCardString);
    		return getString(StringId).replace(sdCardString, mVolumeDescription);
    	} else {
    		return str;
    	}
    }
    
}
