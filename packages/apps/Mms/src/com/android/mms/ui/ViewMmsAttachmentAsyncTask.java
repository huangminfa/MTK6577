/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.layout.LayoutManager;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPersister;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;


/**
 * This ViewMmsAttachmentAsyncTask class is used to do something in a background thread and once
 * that finishes, do other things on the UI thread. If the background runnable task takes longer
 * than half a second, a progress dialog is displayed.
 */
public class ViewMmsAttachmentAsyncTask extends AsyncTask<Boolean, Void, Void> {
    private Context mContext;
    private SlideshowModel mSlideshow;
    private Uri mUri;
    private static final String TAG = "ViewMmsAttachment";
    private static final int DELAYTIME = 500;
    private static SlideModel slide = null;
    private boolean mIsSimple;
    private boolean mIsShowDialog;
    private ProgressDialog mProgressDialog;
    private Handler mHandler;

    /**
     * Creates the Task with the specified string id to be shown in the dialog
     */
    public ViewMmsAttachmentAsyncTask(Context context, Uri msgUri, SlideshowModel slideshow,
            boolean isShowDialog) {
        mContext = context;
        mUri = msgUri;
        mSlideshow = slideshow;
        mIsSimple = (slideshow == null) ? false : slideshow.isSimple();
        mHandler = new Handler();
        mIsShowDialog = isShowDialog;
        if (mIsShowDialog) {
            if (mProgressDialog == null) {
                mProgressDialog = createProgressDialog();
            }
            mProgressDialog.setMessage(mContext.getText(R.string.sync_mms_to_db));
        } else {
            mProgressDialog = null;
        }
    }

    /**
     * Initializes the progress dialog with its intended settings.
     */
    private ProgressDialog createProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * Activates a progress spinner on the UI. This assumes the UI has invoked
     * this Task.
     */
    @Override
    protected void onPreExecute() {
        // activate spinner after half a second
        mHandler.postDelayed(mShowProgressDialogRunnable, DELAYTIME);
    }

    /**
     * Perform the specified tasks on a background thread
     */
    @Override
    protected Void doInBackground(final Boolean... params) {
        final boolean isDirty = params[0];
        try {
            if (mSlideshow != null) {
                if (isDirty) {
                    ((SlideEditorActivity) mContext).setDirty(false);
                    // If a slideshow was provided, save it to disk first.
                    PduPersister persister = PduPersister.getPduPersister(mContext);
                    try {
                        PduBody pb = mSlideshow.toPduBody();
                        persister.updateParts(mUri, pb);
                        mSlideshow.sync(pb);
                    } catch (MmsException e) {
                        Log.e(TAG, "Unable to save message for preview");
                        return null;
                    }
                }
                slide = mSlideshow.get(0);
            }
        } finally {
            // Cancel pending display of the progress bar if the background task
            // has finished before the progress bar has popped up.
            mHandler.removeCallbacks(mShowProgressDialogRunnable);
        }
        return null;
    }

    /**
     * Deactivates the progress spinner on the UI. This assumes the UI has
     * invoked this Task.
     */
    @Override
    protected void onPostExecute(Void result) {
        dismissProgressDialog();
        mProgressDialog = null;
        if (mIsShowDialog) {
            if (mIsSimple && slide != null && !slide.hasAudio()) {
                MessageUtils.viewSimpleSlideshow(mContext, mSlideshow);
            } else {
                // Launch the slideshow activity to play/view.
                Intent intent;
                if (mIsSimple && slide.hasAudio()) {
                    intent = new Intent(mContext, SlideshowActivity.class);
                } else {
                    intent = new Intent(mContext, MmsPlayerActivity.class);
                }
                intent.setData(mUri);
                mContext.startActivity(intent);
            }
        }
    }

    // Shows the activity's progress spinner. Should be canceled if exiting the activity.
    private Runnable mShowProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if ((mProgressDialog != null) && !(((SlideEditorActivity) mContext).isFinishing())) {
                mProgressDialog.show();
            }
        }
    };

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
