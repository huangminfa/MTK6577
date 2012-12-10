package com.android.gallery3d.mpo;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.mpo.MAVView;
import com.android.gallery3d.util.Log;
import com.android.gallery3d.R;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.MediatekFeature.DataBundle;
import com.android.gallery3d.util.MediatekFeature.Params;
import com.android.gallery3d.util.MpoHelper;

import com.mediatek.mpo.MpoDecoder;

public class MavViewer3D extends Activity implements OnClickListener {
	
    private static final String TAG = "MavViewer3D";
    private MAVView3D mMultiAngleView;
    private Bitmap[] mMavBitmapArr;
    public BitmapFactory.Options mOptions = new BitmapFactory.Options();
    public static final int MSG_UPDATE_MAVVIEW = 1;
    public static final String CAMERA_MAV_IMAGE_BUCKET_NAME = 
        Environment.getExternalStorageDirectory().toString()
        + "/DCIM/Camera/Mav";
    private SensorManager mSensorManager;
//	private Sensor mOrientationSensor;
    private Sensor mGyroSensor;
    private MpoDecoder mMpoDecoder = null;
    private boolean mDecodeUri = false;
    
    // for mav touch support
    private boolean mHasGyroSensor = true;
    private Bitmap mFirstShowBitmap = null;
    
    private String mMpoFilePath;
    private Uri mUri = null;
    private ContentResolver mCr = null;
    private int mTotalCount = 0;
    private int mCurrentFrame = 0;
    private int mMiddleFrame = 0;
    private static final int TARGET_SIZE = 640;
    //default display
    private Display mDefaultDisplay = null;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    private int mRotation = 0;
    private boolean mRotationFetched = false;
    private static final String SHARED_PREF_NAME = "mav_viewer";
    private static final String PREFTAG_FIRSTHINTSHOWED = "firstshow";

    // for Stereo3D support
    private ImageButton mBtnToggleStereoMode;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mav_viewer_3d);
        
        mBtnToggleStereoMode = (ImageButton) findViewById(R.id.ib_toggle_stereo_mode);
        mBtnToggleStereoMode.setOnClickListener(this);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Log.i(TAG, "onCreate: gyro sensor=" + mGyroSensor);
        mHasGyroSensor = (mGyroSensor != null);
        
        mMultiAngleView = (MAVView3D) findViewById(R.id.mavview3d);
        mMultiAngleView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mProgressBar = (ProgressBar) findViewById(R.id.mavviewer_progressbar);
        
        mDefaultDisplay = getWindowManager().getDefaultDisplay(); 

        if (!mHasGyroSensor) {
            mMultiAngleView.setTouchModeEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra("mpoFilePath")) {
            mMpoFilePath = intent.getStringExtra("mpoFilePath");
        }

        if (null == mMpoFilePath) {
            // try to fetch file path from intent uri
            Uri uri = intent.getData();
            if (uri == null) {
                Log.e(TAG,"onCreate: get null uri");
                finish();
                return;
            }
            if (uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                // query media DB to fetch the file path
                Cursor c = getContentResolver().query(uri, 
                        new String[] {MediaStore.Images.ImageColumns._ID,
                                      MediaStore.Images.ImageColumns.DATA,
                                      MediaStore.Images.ImageColumns.ORIENTATION}, 
                        null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
                if (c != null && c.moveToFirst()) {
                    mMpoFilePath = c.getString(1);
                    if (!c.isNull(2)) {
                        mRotation = c.getInt(2);
                        mRotationFetched = true;
                    }
                }
                if (c != null) {
                    c.close();
                }
                //we will decode a file path
                mDecodeUri = false;
            } else {
                //we will decode directly from Uri
                mDecodeUri = true;
                mCr = getContentResolver();
                mUri = intent.getData();
            }
            if (!mDecodeUri && null == mMpoFilePath) {
                Log.e(TAG,"onCreate: get null MPO file path in extra of intent");
                finish();
                return;
            }
        } else {
            mDecodeUri = false;
        }
        
        if (!mHasGyroSensor) {
            mMultiAngleView.setTouchModeEnabled(true);
            // show up the first run hint when necessary:
            // 1. gyro sensor is absent, and
            // 2. first time user opens this viewer
            /// TODO: change this "true" to first run detection using SharedPreference
            SharedPreferences pref = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
            boolean isFirstShow = !(pref.getBoolean(PREFTAG_FIRSTHINTSHOWED, false));
            View firstRun = findViewById(R.id.firstrun);
            if (isFirstShow) {
                firstRun.setVisibility(View.VISIBLE);
                Editor ed = pref.edit();
                ed.putBoolean(PREFTAG_FIRSTHINTSHOWED, true);
                ed.commit();
            } else {
                // Remove the first run view from parent
                ViewGroup parent = (ViewGroup) firstRun.getParent();
                parent.removeView(firstRun);
            }
        }
        
        Thread getImages = new Thread(new Runnable() {
            public void run() {
            	mHandler.post(new Runnable() {
            		public void run() {
            		    mProgressBar.setVisibility(View.VISIBLE);
            		}
            	});

                if (!mDecodeUri) {
                    Log.i(TAG,"decode filepath:"+mMpoFilePath);
                    mMpoDecoder = MpoDecoder.decodeFile(mMpoFilePath);
                } else {
                    Log.i(TAG,"decode Uri:"+mUri);
                    mMpoDecoder = MpoDecoder.decodeUri(mCr,mUri);
                }
            	if (null == mMpoDecoder) {
                    Log.e(TAG,"failed to decode MpoDecoder, finish()");
            	    mHandler.post(new Runnable() {
            	        public void run() {
            	        	mProgressBar.setVisibility(View.GONE);
            	            finish();
            	        }
            	    });
            	    return;
            	}
            	
            	if (!mRotationFetched && !mDecodeUri) {
            	    // try to get rotation from media DB, then from file itself
            	    String selectionString = MediaStore.Images.ImageColumns.DATA + "=" + mMpoFilePath;
            	    Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
            	            new String[] {MediaStore.Images.ImageColumns.ORIENTATION}, 
            	            selectionString, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
            	    if (c != null && c.moveToFirst()) {
            	        mRotation = c.getInt(0);
            	        mRotationFetched = true;
            	    }
            	    
            	    if (!mRotationFetched) {
            	        // query DB failed, try exif interface
            	        try {
            	            ExifInterface exif = new ExifInterface(mMpoFilePath);
            	            int exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            	            switch (exifRotation) {
            	            case ExifInterface.ORIENTATION_NORMAL:
            	                mRotation = 0;
            	                mRotationFetched = true;
            	                break;
            	            case ExifInterface.ORIENTATION_ROTATE_90:
            	                mRotation = 90;
            	                mRotationFetched = true;
            	                break;
            	            case ExifInterface.ORIENTATION_ROTATE_180:
            	                mRotation = 180;
            	                mRotationFetched = true;
            	                break;
            	            case ExifInterface.ORIENTATION_ROTATE_270:
            	                mRotation = 270;
            	                mRotationFetched = true;
            	                break;
            	            default:
            	                Log.w(TAG, "rotation in exif is not available!");
            	            }
            	        } catch (Exception e) {
            	            Log.w(TAG, "Exception when trying to fetch orientation from exif");
            	        }
            	    }
            	}
            	
            	if (mRotationFetched) {
                    Log.i(TAG, "final rotation is: " + mRotation);
                    mMultiAngleView.setImageRotation(mRotation);
            	}

            	mTotalCount = mMpoDecoder.frameCount();
            	Log.i(TAG, mMpoDecoder.width() + "x" + mMpoDecoder.height());
            	mOptions.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                    mMpoDecoder.width(), mMpoDecoder.height(), TARGET_SIZE);
            	Log.d(TAG, "SampleSize:" + mOptions.inSampleSize);
            	mMiddleFrame = (int) (mTotalCount / 2);
            	//mMavBitmapArr = new Bitmap[mTotalCount];
                MediatekFeature.enablePictureQualityEnhance(mOptions, true);

                //get first frame to be shown, and post it to UI for quick view
                mFirstShowBitmap = mMpoDecoder.frameBitmap(mMiddleFrame, mOptions);
                if (mFirstShowBitmap == null) {
                    // decode failure, possibly due to MPO decoder already closed
                    // we do not want to continue from this point on
                    return;
                }
                mMultiAngleView.setFirstShowBitmap(mFirstShowBitmap);

            	mHandler.post(
                    new Runnable() {
                        public void run() {
                            mMultiAngleView.setImageBitmap(mFirstShowBitmap);
                        }
                    }
                );
            	
                Params params = new Params();
                params.inMpoFrames = true;
                params.inTargetDisplayWidth = mDefaultDisplay.getWidth();
                params.inTargetDisplayHeight = mDefaultDisplay.getHeight();
                MediatekFeature.enablePictureQualityEnhance(params, true);
                mMavBitmapArr = MpoHelper.decodeMpoFrames(null, params, mMpoDecoder);

                mMultiAngleView.setBitmapArr(mMavBitmapArr);
                
                mHandler.post(new Runnable() {
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        //set firstShowBitmap again, as original set may fail and screen is black
                        mMultiAngleView.setImageBitmap(mFirstShowBitmap);
                        //allow sensor to control frame number
                        mMultiAngleView.setResponsibility(true);
                    }
                });
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
       if (mHasGyroSensor) {
           mSensorManager.registerListener(mMultiAngleView, mGyroSensor,
               SensorManager.SENSOR_DELAY_GAME);
       }
    }
    
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        if (mHasGyroSensor) {
            mSensorManager.unregisterListener(mMultiAngleView);
        }
    }
    
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        mMultiAngleView.setResponsibility(false);
        mMultiAngleView.recycleBitmapArr();
        //close mMpoDecoder to release resources
        if (mMpoDecoder != null) {
            // check for null pointers here, since decode may fail
            mMpoDecoder.close();
        }
    }
    
    public void onFirstRunDismiss(View v) {
        // onClick callback for dismiss button in first run layout
        View firstRun = findViewById(R.id.firstrun);
        if  (firstRun != null) {
            firstRun.setVisibility(View.GONE);
        }
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMultiAngleView.configChanged(this);
        View firstrun = findViewById(R.id.firstrun);
        if (firstrun != null && firstrun.getVisibility() == View.VISIBLE) {
            ViewParent parent = firstrun.getParent();
            ViewGroup vg = null;
            vg = (ViewGroup) parent;
            vg.removeView(firstrun);
            firstrun = View.inflate(this, R.layout.mav_first_run, null);
            firstrun.setId(R.id.firstrun);
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vg.addView(firstrun, flp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.ib_toggle_stereo_mode:
            mMultiAngleView.toggleStereoMode();
            boolean isInStereoMode = mMultiAngleView.getStereoMode();
            int idToUse = isInStereoMode ? R.drawable.ic_2d : R.drawable.ic_3d;
            Drawable iconToUse = getResources().getDrawable(idToUse);
            ((ImageButton)v).setImageDrawable(iconToUse);
            break;
        }
    }

}
