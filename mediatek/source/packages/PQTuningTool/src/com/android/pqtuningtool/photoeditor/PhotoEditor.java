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

/*
 * Copyright (C) 2010 The Android Open Source Project
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

package  com.android.pqtuningtool.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.util.MtkLog;
import  com.android.pqtuningtool.util.MtkUtils;

/**
 * Main activity of the photo editor that opens a photo and prepares tools for photo editing.
 */
public class PhotoEditor extends Activity {

    private static final String SAVE_URI_KEY = "save_uri";

    private Uri sourceUri;
    private Uri saveUri;
    private FilterStack filterStack;
    private ActionBar actionBar;
    private EffectsBar effectsBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoeditor_main);
        //SpinnerProgressDialog.initialize((ViewGroup) findViewById(R.id.toolbar));
        if (LOG) MtkLog.v(TAG, "onCreate() " + this);
        //MtkUtils.logMemory("PhotoEditor.onCreate()");
        
        Intent intent = getIntent();
        if (Intent.ACTION_EDIT.equalsIgnoreCase(intent.getAction())) {
            sourceUri = intent.getData();
        }
        if (LOG) MtkLog.v(TAG, "onCreate() sourceUri=" + sourceUri);
        
        actionBar = (ActionBar) findViewById(R.id.action_bar);
        filterStack = new FilterStack((PhotoView) findViewById(R.id.photo_view),
                new FilterStack.StackListener() {

                    @Override
                    public void onStackChanged(boolean canUndo, boolean canRedo) {
                        actionBar.updateButtons(canUndo, canRedo);
                    }
        }, savedInstanceState);
        if (savedInstanceState != null) {
            saveUri = savedInstanceState.getParcelable(SAVE_URI_KEY);
            actionBar.updateSave(saveUri == null);
        }

        // Effects-bar is initially disabled until photo is successfully loaded.
        effectsBar = (EffectsBar) findViewById(R.id.effects_bar);
        effectsBar.initialize(filterStack);
        effectsBar.setEnabled(false);

        actionBar.setClickRunnable(R.id.undo_button, createUndoRedoRunnable(true));
        actionBar.setClickRunnable(R.id.redo_button, createUndoRedoRunnable(false));
        actionBar.setClickRunnable(R.id.save_button, createSaveRunnable());
        actionBar.setClickRunnable(R.id.share_button, createShareRunnable());
        actionBar.setClickRunnable(R.id.action_bar_back, createBackRunnable());
    }

    private void openPhoto() {
        SpinnerProgressDialog.showDialog();
        LoadScreennailTask.Callback callback = new LoadScreennailTask.Callback() {

            @Override
            public void onComplete(final Bitmap result) {
                filterStack.setPhotoSource(result, new OnDoneCallback() {

                    @Override
                    public void onDone() {
                        SpinnerProgressDialog.dismissDialog();
                        effectsBar.setEnabled(result != null);
                    }
                });
            }
        };
        new LoadScreennailTask(this, callback).execute(sourceUri);
    }

    private Runnable createUndoRedoRunnable(final boolean undo) {
        return new Runnable() {

            @Override
            public void run() {
                effectsBar.exit(new Runnable() {

                    @Override
                    public void run() {
                        SpinnerProgressDialog.showDialog();
                        OnDoneCallback callback = new OnDoneCallback() {

                            @Override
                            public void onDone() {
                                SpinnerProgressDialog.dismissDialog();
                            }
                        };
                        if (undo) {
                            filterStack.undo(callback);
                        } else {
                            filterStack.redo(callback);
                        }
                    }
                });
            }
        };
    }

    private Runnable createSaveRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                effectsBar.exit(new Runnable() {

                    @Override
                    public void run() {
                        SpinnerProgressDialog.showDialog();
                        filterStack.getOutputBitmap(new OnDoneBitmapCallback() {

                            @Override
                            public void onDone(Bitmap bitmap) {
                                SaveCopyTask.Callback callback = new SaveCopyTask.Callback() {

                                    @Override
                                    public void onComplete(Uri result) {
                                        SpinnerProgressDialog.dismissDialog();
                                        saveUri = result;
                                        actionBar.updateSave(saveUri == null);
                                    }
                                };
                                new SaveCopyTask(PhotoEditor.this, sourceUri, callback).execute(
                                        bitmap);
                            }
                        });
                    }
                });
            }
        };
    }

    private Runnable createShareRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                effectsBar.exit(new Runnable() {

                    @Override
                    public void run() {
                        if (saveUri != null) {
                            if (LOG) MtkLog.v(TAG, "Share.run() saveUri=" + saveUri);
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, saveUri);
                            intent.setType("image/*");
                            startActivity(intent);
                        }
                    }
                });
            }
        };
    }

    private Runnable createBackRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                // Exit effects or go back to the previous activity on pressing back button.
                if (!effectsBar.exit(null)) {
                    // Pop-up a dialog to save unsaved photo.
                    if (actionBar.canSave()) {
                        new YesNoCancelDialogBuilder(PhotoEditor.this, new Runnable() {

                            @Override
                            public void run() {
                                actionBar.clickSave();
                            }
                        }, new Runnable() {

                            @Override
                            public void run() {
                                finish();
                            }
                        }, R.string.save_photo).show();
                    } else {
                        if (LOG) MtkLog.v(TAG, "createBackRunnable finish" + this);
                        finish();
                    }
                }
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        filterStack.saveStacks(outState);
        outState.putParcelable(SAVE_URI_KEY, saveUri);
    }

    @Override
    public void onBackPressed() {
        if (LOG) MtkLog.v(TAG, "onBackPressed() " + this);
        actionBar.clickBack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (LOG) MtkLog.v(TAG, "onPause() " + this);
        filterStack.onPause();
        // Dismiss any running progress dialog as all operations are paused.
        SpinnerProgressDialog.dismissDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LOG) MtkLog.v(TAG, "onResume() " + this);
        SpinnerProgressDialog.initialize((ViewGroup) findViewById(R.id.toolbar));
        filterStack.onResume();
        openPhoto();
    }
    
    private static final String TAG = "PhotoEditor";
    private static final boolean LOG = true;
    
    //just for ActivityManager test
    @Override
    protected void onStart() {
        super.onStart();
        if (LOG) MtkLog.v(TAG, "onStart() " + this);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (LOG) MtkLog.v(TAG, "onStop() " + this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (LOG) MtkLog.v(TAG, "onDestroy() " + this);
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        if (LOG) MtkLog.v(TAG, "onRestart() " + this);
    }
}
