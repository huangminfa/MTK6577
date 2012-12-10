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

package  com.android.pqtuningtool.mpo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;

import  com.android.pqtuningtool.mpo.MAVView;
import  com.android.pqtuningtool.util.Log;
import  com.android.pqtuningtool.R;

import com.mediatek.mpo.MpoDecoder;

public class MavViewer extends Activity {
    
    private static final String TAG = "MavViewer";
    private MAVView mMultiAngleView;
    private Bitmap[] mMavBitmapArr;
    public BitmapFactory.Options mOptions = new BitmapFactory.Options();
    public static final int MSG_UPDATE_MAVVIEW = 1;
    public static final String CAMERA_MAV_IMAGE_BUCKET_NAME = 
        Environment.getExternalStorageDirectory().toString()
        + "/DCIM/Camera/Mav";
    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;
    private Sensor mGyroSensor;
    private MpoDecoder mMpoDecoder = null;
    private String mMpoFilePath;
    private int mTotalCount = 0;
    private int mCurrentFrame = 0;
    private int mMiddleFrame = 0;
    private static int PREFER_SIZE = (int) (640 / 1.414f);
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mav_viewer);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        
        mMultiAngleView = (MAVView) findViewById(R.id.mavview);
        mProgressBar = (ProgressBar) findViewById(R.id.mavviewer_progressbar);
        mOptions.inPreferSize = PREFER_SIZE;

        Intent intent = getIntent();
        if (intent.hasExtra("mpoFilePath")) {
            mMpoFilePath = intent.getStringExtra("mpoFilePath");
        }

        if (null == mMpoFilePath) {
            // try to fetch file path from intent uri
            Uri uri = intent.getData();
            if (uri != null && uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                // query media DB to fetch the file path
                Cursor c = getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA}, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
                if (c != null && c.moveToFirst()) {
                    mMpoFilePath = c.getString(1);
                }
                if (c != null) {
                    c.close();
                }
            }
            if (null == mMpoFilePath) {
                Log.e(TAG,"onCreate: get null MPO file path in extra of intent");
                finish();
                return;
            }
        }
        
        Thread getImages = new Thread(new Runnable() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                });

                mMpoDecoder = MpoDecoder.decodeFile(mMpoFilePath);
                if (null == mMpoDecoder) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            finish();
                        }
                    });
                    return;
                }

                mTotalCount = mMpoDecoder.frameCount();
                Log.i(TAG, mMpoDecoder.width() + "x" + mMpoDecoder.height());
                mOptions.inSampleSize = calcuSampleSize((int) (mMpoDecoder.width() / PREFER_SIZE));
                mMiddleFrame = (int) (mTotalCount / 2);
                mMavBitmapArr = new Bitmap[mTotalCount];

                //get first frame to be shown, and post it to UI for quick view
                final Bitmap firstShowBitmap = mMpoDecoder.frameBitmap(mMiddleFrame, mOptions);

                mHandler.post(
                    new Runnable() {
                        public void run() {
                            mMultiAngleView.setImageBitmap(firstShowBitmap);
                        }
                    }
                );
                
                int curFrame = 0;
                while (curFrame < mTotalCount) {
                    //Log.i(TAG, "curFrame : " + curFrame);
                    if (curFrame != mMiddleFrame) {
                        mMavBitmapArr[curFrame] = mMpoDecoder.frameBitmap(curFrame, mOptions);
                        Log.i(TAG, "mMavBitmapArr[" + curFrame + "] : " + mMavBitmapArr[curFrame]);
                    } else {
                        mMavBitmapArr[curFrame] = firstShowBitmap;
                    }
                    ++curFrame;
                }

                mMultiAngleView.setBitmapArr(mMavBitmapArr);
                
                mHandler.post(new Runnable() {
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        //set firstShowBitmap again, as original set may fail and screen is black
                        mMultiAngleView.setImageBitmap(firstShowBitmap);
                        //allow sensor to control frame number
                        mMultiAngleView.mResponsibility = true;
                    }
                });
                
                //mMultiAngleView.mResponsibility = true;

            }

        });
        
        getImages.start();


        
    }
    
    private int calcuSampleSize(int input) {
        if (input <= 0) {
            return -1;
        }
        int sizeOf2P = 0;
        while (input > 0) {
            sizeOf2P++;
            input >>>= 1;
        }
        return (int) Math.pow((double) 2, (double) sizeOf2P);
    }
    
    protected void onResume() {
       super.onResume();
       Log.i(TAG, "onResume()");
       mSensorManager.registerListener(mMultiAngleView, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
       mSensorManager.registerListener(mMultiAngleView.mRectifySensorListener, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
    
    }
    
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        mSensorManager.unregisterListener(mMultiAngleView);
        mSensorManager.unregisterListener(mMultiAngleView.mRectifySensorListener);
    }
    
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        mMultiAngleView.mResponsibility = false;
        mMultiAngleView.resycleBitmapArr();
    }
    
}
