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

package com.mediatek.FMRadio;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.featureoption.FeatureOption;

public class FMRecorderDialog extends Dialog implements OnClickListener {
    private static final String TAG = "FMRecorderDialog";
    // Linux system supports up to 255 characters
    private static final int ROCORDING_FILENAME_MAX_SIZE = 255;
    // Recording filename suffix .3gpp length is 5
    private static final int ROCORDING_FILENAME_SUFFIX_SIZE = 5;
    private final Context mContext;
    private Button mButtonSave;
    private Button mButtonDiscard;
    private EditText mRecordingNameEditText;
    private String mDefaultRecordingName;
    private View mDivider;
    private TextView mTitleTextView;

    private IFMRadioService mService = null;
    private String mSDDirectory = null;
    // The default filename need't to check whether exist
    private boolean mIsNeedCheckFilenameExist = false;

    /**
     * The constructor to construct a custom dialog
     * 
     * @param context
     *            the context of FileManagerActivity
     * @param IFMRadioService
     *            FRMadio service
     */
    public FMRecorderDialog(Context context, IFMRadioService iFMRadioservice) {
        super(context);
        mContext = context;
        mService = iFMRadioservice;
        mSDDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        showRenameDialog();
    }

    /**
     * This method register callback and set filter to Edit, in order to make sure that user input
     * is legal. The input can't be illegal filename and can't be too long.
     */
    private void setTextChangedCallback() {
        setEditTextFilter(mRecordingNameEditText);
        mRecordingNameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Filename changed, so we should check the new filename is whether exist.
                mIsNeedCheckFilenameExist = true;
                // Characters not allowed
                if (s.toString().trim().length() <= 0 || s.toString().matches(".*[/\\\\:*?\"<>|].*")) {
                    mButtonSave.setEnabled(false);
                } else {
                    mButtonSave.setEnabled(true);
                }
            }
        });
    }

    /**
     * The method to show a rename dialog
     */
    private void showRenameDialog() {
        this.setContentView(R.layout.fm_recorder_dialog);

        mButtonSave = (Button) this.findViewById(R.id.fm_recording_btn_save);
        mButtonSave.setEnabled(true);
        mButtonSave.setOnClickListener(this);

        mButtonDiscard = (Button) this.findViewById(R.id.fm_recording_btn_discard);
        mButtonDiscard.setOnClickListener(this);
        
        mDivider = this.findViewById(R.id.divider);
        mTitleTextView = (TextView)this.findViewById(R.id.fm_recording_title_bar);
        
        if (FeatureOption.MTK_THEMEMANAGER_APP) {
            Resources res = mContext.getResources();
            int color = res.getThemeMainColor();
            if (color != 0) {
                mTitleTextView.setTextColor(color);
                mDivider.setBackgroundColor(color);
            }
        }
        // Set the recording edit text
        mRecordingNameEditText = (EditText) this.findViewById(R.id.fm_recording_text);
        mRecordingNameEditText.setSingleLine(true);
        try {
            mDefaultRecordingName = mService.getRecordingName();
        } catch (Exception ex) {
            FMRadioLogUtils.e(TAG, "Exception while getRecordingName: ", ex);
        }
        if (mDefaultRecordingName != null) {
            mRecordingNameEditText.setText(mDefaultRecordingName);
            mRecordingNameEditText.selectAll();
        }
        mRecordingNameEditText.setHint(mContext.getResources().getString(
                R.string.edit_recording_name_hint));
        mRecordingNameEditText.requestFocus();
        FMRadioLogUtils.d(TAG, "savedialog: focused=" + mRecordingNameEditText.isFocused());
        setTextChangedCallback();
    }

    /**
     * This method is used to set filter to EditText which is used for user entering filename. This
     * filter will ensure that the inputed filename wouldn't be too long. If so, the inputed info
     * would be rejected.
     * 
     * @param edit
     *            The EditText for filter to be registered.
     */
    private void setEditTextFilter(EditText edit) {
        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                int oldSize = 0;
                int sourceSize = 0;

                // original
                String name = mRecordingNameEditText.getText().toString();
                oldSize = name.getBytes().length;
                FMRadioLogUtils.v(TAG, "Original file name: " + name);
                FMRadioLogUtils.v(TAG, "Original size: " + oldSize);

                // new add sequences
                String seq = source.toString();
                sourceSize = seq.getBytes().length;
                FMRadioLogUtils.v(TAG, "New source: " + seq);
                FMRadioLogUtils.v(TAG, "New source size: " + sourceSize);

                // New file name size
                int newSize = oldSize + sourceSize;
                FMRadioLogUtils.v(TAG, "New file name size: " + newSize);
                // Linux system supports up to 255
                // So remove suffix ,the edit text can only input 255-5=250
                // If the file name size bigger than 255,forbid to input word
                if (newSize > ROCORDING_FILENAME_MAX_SIZE - ROCORDING_FILENAME_SUFFIX_SIZE) {
                    FMRadioLogUtils.d(TAG, "The file name size is max,can not input words");
                    return "";
                } else {
                    return null;
                }

            }
        };

        edit.setFilters(new InputFilter[] { filter });
    }

    @Override
    public void onClick(View v) {
        String msg = null;

        switch (v.getId()) {

        case R.id.fm_recording_btn_save:
            // Check the recording name whether exist
            String RecordingNameToSave = mRecordingNameEditText.getText().toString().trim();
            File recordingFolderPath = new File(mSDDirectory, "FM Recording");
            File recordingFileToSave =
                    new File(recordingFolderPath, RecordingNameToSave
                            + FMRecorder.RECORDING_FILE_EXTENSION);

            // If the new name is same as default name ,need't to check!
            if (mDefaultRecordingName == null) {
                FMRadioLogUtils.e(TAG, "Error:recording file is not exist!");
                return;
            }
            if (mDefaultRecordingName.equals(RecordingNameToSave)) {
                mIsNeedCheckFilenameExist = false;
                FMRadioLogUtils.d(TAG,
                        "New name is the same as default name,so need not check whether exist!");
            }
            FMRadioLogUtils.d(TAG, "Is the recording file name need to check whether exist :"
                    + mIsNeedCheckFilenameExist);
            if (recordingFileToSave.exists() && mIsNeedCheckFilenameExist) {
                // show a toast notification if renaming a file/folder to the same name
                msg =
                        mRecordingNameEditText.getText().toString() + " "
                                + mContext.getResources().getString(R.string.already_exists);
                FMRadioLogUtils.d(TAG, "file " + RecordingNameToSave.toString()
                        + ".3gpp is already exists!");
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    mService.saveRecording(RecordingNameToSave);
                    FMRadioLogUtils.d(TAG,"FM recording file is saved in "
                                            + recordingFileToSave.getAbsolutePath());
                } catch (Exception ex) {
                    FMRadioLogUtils.e(TAG, "Exception while saveRecording file: ", ex);
                }

                dismiss();
            }

            break;

        case R.id.fm_recording_btn_discard:
            try {
                mService.saveRecording(null);
            } catch (Exception ex) {
                FMRadioLogUtils.e(TAG, "Exception while saveRecording file: ", ex);
            }
            dismiss();
            FMRadioLogUtils.d(TAG,"Discard FM recording file. ");
            break;

        default:
            break;
        }
    }
}