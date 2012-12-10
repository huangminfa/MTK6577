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

package com.android.pqtuningtool.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Files;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.pqtuningtool.common.BitmapUtils;
import com.android.pqtuningtool.R;
import com.android.pqtuningtool.pqjni.PictureQualityJni;
import com.android.pqtuningtool.util.Future;
import com.android.pqtuningtool.util.FutureListener;
import com.android.pqtuningtool.util.MediatekFeature;
import com.android.pqtuningtool.util.MtkLog;
import com.android.pqtuningtool.util.PictureQualityOptions;

public class PictureQualityTool extends Activity {
    /** Called when the activity is first created. */

    private static final String TAG = "PictureQualityTool";
    public static final String ACTION_PQ = "com.android.camera.action.PQ";
    private static final int BACKUP_PIXEL_COUNT = 480000; // around 800x600


    private PictureQualityJni PQTool;
    private ImageView mImageView;
    private Bitmap mBitmap;

    private SeekBar mSeekBarSkyTone ;
    private SeekBar mSeekBarSkinTone;
    private SeekBar mSeekBarGrassTone;
    private SeekBar mSeekBarSharpness;
    private SeekBar mSeekBarColor;

    private TextView mTextViewSkyTone;
    private TextView mTextViewSkinTone;
    private TextView mTextViewGrassTone;
    private TextView mTextViewSkyToneMin;
    private TextView mTextViewSkinToneMin;
    private TextView mTextViewGrassToneMin;
    private TextView mTextViewSharpness;
    private TextView mTextViewColor;
    private TextView mTextViewSkyToneRange;
    private TextView mTextViewSkinToneRange;
    private TextView mTextViewGrassToneRange;
    private TextView mTextViewSharpnessRange;
    private TextView mTextViewColorRange;
    private int mSkyToneRange;
    private int mSkinToneRange;
    private int mGrassToneRange;
    private int mColorRange;
    private int mSharpnessRange;
    private int mSkyToneOption;
    private int mSkinToneOption;
    private int mGrassToneOption;
    private int mColorOption;
    private int mSharpnessOption;
    public String pqUri;
    public BitmapFactory.Options options = new BitmapFactory.Options();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.picture_quality_tool);
        Bundle bundle = this.getIntent().getExtras();
        pqUri = bundle.getString("PQUri");

        //for picture quality enhancement
        if (MediatekFeature.isPictureQualityEnhanceSupported()) {
            options.inPostProc = true;
        }
        options.inSampleSize = 1;
        initPQToolView();

        mSeekBarSharpness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                PQTool.nativeSetSharpnessIndex(mSharpnessOption);
                onReDisplayPQImage();
                MtkLog.i(TAG, "Sharpness Index is " + PQTool.nativeGetSharpnessIndex());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                mSharpnessOption = (progress * (mSharpnessRange-1)) / 100;
                mTextViewSharpness.setText("Sharpness:  " + mSharpnessOption);
            }
        });


        mSeekBarColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                PQTool.nativeSetColorIndex(mColorOption);
                MtkLog.i(TAG, "Color Index is " + PQTool.nativeGetColorIndex());
                onReDisplayPQImage();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                mColorOption = (progress * (mColorRange-1)) / 100;
                mTextViewColor.setText("Color:   " + mColorOption);
            }
        });


        mSeekBarSkinTone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                PQTool.nativeSetSkinToneIndex(mSkinToneOption);
                MtkLog.i(TAG, "SkinTone Index is " + PQTool.nativeGetSkinToneIndex());
                onReDisplayPQImage();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                mSkinToneOption = (progress * (mSkinToneRange-1)) / 100;
                mTextViewSkinTone.setText("SkinTone:   " + (mSkinToneOption-3));
            }
        });



        mSeekBarGrassTone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                PQTool.nativeSetGrassToneIndex(mGrassToneOption);
                MtkLog.i(TAG, "GrassTone Index is " + PQTool.nativeGetGrassToneIndex());
                onReDisplayPQImage();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                mGrassToneOption = (progress * (mGrassToneRange-1)) / 100;
                mTextViewGrassTone.setText("GrassTone:  " + (mGrassToneOption-3));
            }
        });

        mSeekBarSkyTone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                PQTool.nativeSetSkyToneIndex(mSkyToneOption);
                MtkLog.i(TAG, "SkyTone Index is " + PQTool.nativeGetSkyToneIndex());
                onReDisplayPQImage();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                mSkyToneOption = (progress * (mSkyToneRange-1)) / 100;
                mTextViewSkyTone.setText("SkyTone:  " + (mSkyToneOption-3));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.picturequality, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.cancel: {
                finish();
                break;
            }
            case R.id.save: {
                onSaveClicked();
                finish();
                break;
            }
        }
        return true;
    }

    private void initPQToolView() {

        mSkyToneRange = PQTool.nativeGetSkyToneRange();
        mSkinToneRange = PQTool.nativeGetSkinToneRange();
        mGrassToneRange = PQTool.nativeGetGrassToneRange();
        mColorRange= PQTool.nativeGetColorRange();
        mSharpnessRange = PQTool.nativeGetSharpnessRange();

        mTextViewSkyToneMin = (TextView)findViewById(R.id.textView1);
        mTextViewSkinToneMin = (TextView)findViewById(R.id.textView4);
        mTextViewGrassToneMin = (TextView)findViewById(R.id.textView5);

        mTextViewSkyToneMin.setText(Integer.toString(mSkyToneRange/2+1-mSkyToneRange));
        mTextViewSkinToneMin.setText(Integer.toString(mSkinToneRange/2+1-mSkinToneRange));
        mTextViewGrassToneMin.setText(Integer.toString(mGrassToneRange/2+1-mGrassToneRange));

        mImageView = (ImageView)findViewById(R.id.imageView);
        mTextViewSharpnessRange = (TextView)findViewById(R.id.textView_sharpness);
        mTextViewColorRange = (TextView)findViewById(R.id.textView_color);
        mTextViewSkyToneRange = (TextView)findViewById(R.id.textView_skyTone);
        mTextViewSkinToneRange = (TextView)findViewById(R.id.textView_skinTone);
        mTextViewGrassToneRange = (TextView)findViewById(R.id.textView_grassTone);

        mTextViewSharpnessRange.setText(Integer.toString(mSharpnessRange-1));
        mTextViewColorRange.setText(Integer.toString(mColorRange-1));
        mTextViewSkyToneRange.setText(Integer.toString((mSkyToneRange-1)/2));
        mTextViewSkinToneRange.setText(Integer.toString((mSkinToneRange-1)/2));
        mTextViewGrassToneRange.setText(Integer.toString((mGrassToneRange-1)/2));

        mTextViewSharpness = (TextView)findViewById(R.id.textView_sharpness_progress);
        mTextViewSharpness.setText("Sharpness:  " + Integer.toString(PQTool.nativeGetSharpnessIndex()));
        mSeekBarSharpness  = (SeekBar)findViewById(R.id.seekBar_sharpness);
        mSeekBarSharpness.setProgress(PQTool.nativeGetSharpnessIndex() * 100 / (mSharpnessRange-1));

        mTextViewSkyTone = (TextView)findViewById(R.id.textView_skyTone_progress);
        mTextViewSkyTone.setText("SkyTone:  " + Integer.toString(PQTool.nativeGetSkyToneIndex()-3));
        mSeekBarSkyTone = (SeekBar)findViewById(R.id.seekBar_skyTone);
        mSeekBarSkyTone.setProgress(PQTool.nativeGetSkyToneIndex() * 100 / (mSkyToneRange-1));

        mTextViewGrassTone = (TextView)findViewById(R.id.textView_grassTone_progress);
        mTextViewGrassTone.setText("GrassTone:  " + Integer.toString(PQTool.nativeGetGrassToneIndex()-3));
        mSeekBarGrassTone = (SeekBar)findViewById(R.id.seekBar_grassTone);
        mSeekBarGrassTone.setProgress(PQTool.nativeGetGrassToneIndex() * 100 / (mGrassToneRange-1));

        mTextViewSkinTone = (TextView)findViewById(R.id.textView_skinTone_progress);
        mTextViewSkinTone.setText("SkinTone:  " + Integer.toString(PQTool.nativeGetSkinToneIndex()-3));
        mSeekBarSkinTone = (SeekBar)findViewById(R.id.seekBar_skinTone);
        mSeekBarSkinTone.setProgress(PQTool.nativeGetSkinToneIndex() * 100 / (mSkinToneRange-1));

        mTextViewColor = (TextView)findViewById(R.id.textView_color_progress);
        mTextViewColor.setText("Color:  " + Integer.toString(PQTool.nativeGetColorIndex()));
        mSeekBarColor  = (SeekBar)findViewById(R.id.seekBar_color);
        mSeekBarColor.setProgress(PQTool.nativeGetColorIndex() * 100 / (mColorRange-1));

        options.inJustDecodeBounds = true;
        try {
            mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(pqUri)), null, options);
        } catch (FileNotFoundException e) {
            MtkLog.e(TAG, "bitmapfactory decodestream fail");
        }
        int width = options.outWidth;
        int height = options.outWidth;
        options.inSampleSize = BitmapUtils.computeSampleSize(width, height,
                BitmapUtils.UNCONSTRAINED, BACKUP_PIXEL_COUNT);
        options.inJustDecodeBounds = false;
        onReDisplayPQImage();
    }

    private void onReDisplayPQImage() {
        try {
            mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(pqUri)), null, options);
        } catch (FileNotFoundException e) {
            MtkLog.e(TAG, "bitmapfactory decodestream fail");
        }
        mImageView.setImageBitmap(mBitmap);
    }

    private void onSaveClicked() {
        Intent intent = new Intent();
        Bundle bundle =  new Bundle();
        bundle.putInt("color", PQTool.nativeGetColorIndex());
        bundle.putInt("sharpness", PQTool.nativeGetSharpnessIndex());
        bundle.putInt("skyTone", PQTool.nativeGetSkyToneIndex());
        bundle.putInt("skinTone", PQTool.nativeGetSkinToneIndex());
        bundle.putInt("grassTone", PQTool.nativeGetGrassToneIndex());
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }
}