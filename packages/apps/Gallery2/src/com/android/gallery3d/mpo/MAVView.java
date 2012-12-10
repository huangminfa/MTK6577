package com.android.gallery3d.mpo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;

import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.EdgeEffect;
import android.widget.OverScroller;
import android.app.Activity;

import com.android.gallery3d.util.Log;

public class MAVView extends MPOView implements OnGestureListener {
	
    private static final String TAG = "MAVView";
    private Bitmap[] mBitmapArr;
    private float[] mRectifyValue = {0, 0, 0};
    private boolean mFirstTime = true;
    private int mLastIndex = 0xFFFF;
    private static int BASE_ANGLE = 15;
    private Matrix mBaseMatrix = new Matrix();
    private boolean mResponsibility = false;
    public RectifySensorListener mRectifySensorListener = new RectifySensorListener();
	

    public MAVView(Context context) {
        super(context);
        initView(context);
    }
    
    public MAVView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);       
        if ((mBitmapArr != null) && (mBitmapArr.length > 0)) {
            if (0xFFFF == mLastIndex) {
                // sensor has not been triggered yet, and then directly set the middle frame
                setImageBitmap(mBitmapArr[(int) (mBitmapArr.length / 2)]);
            } else {
                // set image according to the sensor result
                if (mEnableTouchMode) {
                    setImageBitmap(mBitmapArr[mLastIndex]);
                } else {
                    setImageBitmap(mBitmapArr[mBitmapArr.length - mLastIndex -1]);
                }
            }
        } else {
            if (!mResponsibility && mFirstShowBitmap != null) {
                setImageBitmap(mFirstShowBitmap);
            }
        }
    }

    class RectifySensorListener implements SensorEventListener {
    	
        public void onSensorChanged(SensorEvent event) {
            mRectifyValue[0] = event.values[0];
            mRectifyValue[1] = event.values[1];
            mRectifyValue[2] = event.values[2];
        }
        
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!mResponsibility) {
            return;
        }
        //workaround for Gygro sensor HW limitation.
        //As sensor continues to report small movement, wrongly
        //indicating that the phone is slowly moving, we should
        //filter the small movement.
        final float xSmallRotateTH = 0.03f;
        //xSmallRotateTH indicating the threshold of max "small
        //rotation". This varible is determined by experiments
        //based on MT6575 platform. May be adjusted on other chips.
        
        float valueToUse = 0;
        int newRotation = mDisplay.getRotation();
        if (mOrientation != newRotation) {
            // orientation has changed, reset calculations
            mOrientation = newRotation;
            mValue = 0;
            angle[0] = 0;
            angle[1] = 0;
            angle[2] = 0;
            mFirstTime = true;
        }
        switch (mOrientation) {
        case Surface.ROTATION_0:
            valueToUse = event.values[1];
            break;
        case Surface.ROTATION_90:
            // no need to re-map
            valueToUse = event.values[0];
            break;
        case Surface.ROTATION_180:
            // we do not have this rotation on our device
            valueToUse = -event.values[1];
            break;
        case Surface.ROTATION_270:
            valueToUse = -event.values[0];
            break;
        default:
            valueToUse = event.values[0];
        }
        if (Math.abs(valueToUse) < xSmallRotateTH) {
            return;
        }
        

        mValue = valueToUse + OFFSET;
    	if (timestamp != 0 && Math.abs(mValue) > TH) {
    	    final float dT = (event.timestamp - timestamp) * NS2S;

    	    angle[1] += mValue * dT * 180 / Math.PI;
    	    if (mFirstTime) {
    	        angle[0] = angle[1] - BASE_ANGLE;
    	        angle[2] = angle[1] + BASE_ANGLE;
    	        mFirstTime = false;
    	    } else if (angle[1] <= angle[0]) {
    	        angle[0] = angle[1];
    	        angle[2] = angle[0] + 2 * BASE_ANGLE;
    	    } else if (angle[1] >= angle[2]) {
    	        angle[2] = angle[1];
    	        angle[0] = angle[2] - 2 * BASE_ANGLE;
    	    }
    	    if (mBitmapArr != null) {
    	        int index = (int) (angle[1] - angle[0]) * mBitmapArr.length / (2 * BASE_ANGLE);
    	        if (index >= 0 && index < mBitmapArr.length) {
    	        	if (mLastIndex == 0xFFFF || mLastIndex != index) {
    	        		Log.i(TAG, "setImageBitmap: bitmap[" + (mBitmapArr.length - index - 1) + "]");
    	                setImageBitmap(mBitmapArr[mBitmapArr.length - index -1]);
    	                mLastIndex = index;
    	        	}
    	        }
    	    }

//    	    Log.i(TAG, "angle[0]: " + angle[0] + " angle[1]: " + angle[1] + " angle[2]" + angle[2]);
    	}
    	timestamp = event.timestamp;

    }
    
    private float mValue = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp = 0;
    private float angle[] = {0,0,0};
    private static final float TH = 0.001f;
    private static final float OFFSET = 0.0f;
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }
    
    public void setImageRotation(int rotation) {
    	  mImageRotation = rotation;
Log.v(TAG,"setImageRotation(rotation="+rotation+")");
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) {
        if (!checkSelf() || !checkBitmap(bm)) {
            Log.w(TAG,"setImageBitmap:either Bitmap or ImageView's dimen invalid");
            return;
        }
        //set imageView's drawable
        super.setImageBitmap(bm);
        //change imageView's matrix
        setBitmapMatrix(bm);
    }
    
    public void setBitmapArr(Bitmap[] bitmapArr) {
        mBitmapArr = bitmapArr;
    }

    private boolean checkBitmap(Bitmap bitmap) {
        if (null == bitmap) {
            Log.w(TAG,"checkBitmap:in passed Bitmap is null!");
            return false;
        }

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        if (w <= 0 || h <= 0) {
            Log.w(TAG,"checkBitmap:invalid dimension of Bitmap!");
            return false;
        }

        return true;
    }

    private boolean checkSelf() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) {
            Log.w(TAG,"checkSelf:invalid dimension of ImageView!");
            return false;
        }

        return true;
    }

    private void setBitmapMatrix(Bitmap bitmap) {
        if (!checkSelf() || !checkBitmap(bitmap)) {
            Log.w(TAG,"setBitmapMatrix:either Bitmap or ImageView's dimen invalid");
            mBaseMatrix.reset();
            setImageMatrix(mBaseMatrix);
            return;
        }

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = 0;
        float h = 0;
        if ((mImageRotation / 90) % 2 != 0) {
            w = bitmap.getHeight();
            h = bitmap.getWidth();
        } else {
            w = bitmap.getWidth();
            h = bitmap.getHeight();
        }

        float widthScale = viewWidth / w;
        float heightScale = viewHeight / h;

        float scale = Math.min(widthScale, heightScale);

        mBaseMatrix.reset();
        
        if (mImageRotation != 0) {
            Matrix matrix = new Matrix();
            // We want to do the rotation at origin, but since the bounding
            // rectangle will be changed after rotation, so the delta values
            // are based on old & new width/height respectively.
            int cx = bitmap.getWidth() / 2;
            int cy = bitmap.getHeight() / 2;
            matrix.preTranslate(-cx, -cy);
            matrix.postRotate(mImageRotation);
            matrix.postTranslate(w / 2, h / 2);
            mBaseMatrix.postConcat(matrix);
        }

        mBaseMatrix.postScale(scale, scale);
        mBaseMatrix.postTranslate(
        		(viewWidth - w * scale) / 2F,
        		(viewHeight - h * scale) / 2F);
        //set ImageView's matrix
        setImageMatrix(mBaseMatrix);
    }
    
    public void recycleBitmapArr() {
        if (mBitmapArr != null) {
            for (int i = 0; i < mBitmapArr.length; ++i) {
                Log.i(TAG, "bitmap[" + i + "] is recycled");
            	if (mBitmapArr[i] != null) {
                    mBitmapArr[i].recycle();
                    mBitmapArr[i] = null;
            	}
            }
        }
    }


    /* The following fields/methods are added for MAV touch navigation support */
    private boolean mEnableTouchMode = false;
    private Display mDisplay;
    private int mWidth;
    private int mTouchThreshold = -1;
    private GestureDetector mGestureDetector;
    private int mLastTmpIndex = -1;
    private OverScroller mScroller;
    private Thread mFlingThread;
    private boolean mFlingOnGoing = false;
    private EdgeEffect mEffectRight;
    private EdgeEffect mEffectLeft;
    private boolean mAbsorbingFling = false;
    private int mFlingDirection = FLING_DIRECTION_INVALID;
    private static final int FLING_DIRECTION_LEFT = 1;
    private static final int FLING_DIRECTION_RIGHT = 2;
    private static final int FLING_DIRECTION_INVALID = -1;
    private float mScrollDistance = 0;
    private int mOrientation = -1;
    private int mImageRotation = 0;
    private Bitmap mFirstShowBitmap = null;
    // mDistDivider is used to decide how long user has to scroll on the screen
    // to pass through all of the frames of this MPO file
    private int mDistDivider = 3;
    private float mOverScrollPoint = 0;

    
    private void initView(Context context) {
        mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
        mGestureDetector = new GestureDetector(this);
        mScroller = new OverScroller(context);
        mEffectRight = new EdgeEffect(context);
        mEffectLeft = new EdgeEffect(context);
        updateDimensionParams();
        Log.i(TAG, "initView: initial rotation=" + mOrientation);
    }
    
    public void setFirstShowBitmap(Bitmap bm) {
        mFirstShowBitmap = bm;
    }
    
    public void setTouchModeEnabled(boolean enabled) {
        mEnableTouchMode = enabled;
    }
    
    public void configChanged(Context context) {
        // update all dimensions
        updateDimensionParams();
        Log.e(TAG, "configChanged: new width=" + mWidth + ", new height=" + mDisplay.getHeight());
    }
    
    public void setResponsibility(boolean responsible) {
        mResponsibility = responsible;
        if (responsible && mEnableTouchMode
                && mBitmapArr != null && mBitmapArr.length >= 1) {
            // in touch mode, the mLastIndex will indicate current frame;
            // therefore, it should be initialized here
            mLastIndex = mBitmapArr.length / 2;
            mLastTmpIndex = mLastIndex;
            updateDimensionParams();
        }
    }
    
    private void updateDimensionParams() {
        if (mDisplay == null) {
            return;
        }
        mWidth = mDisplay.getWidth();
        if (mEffectRight != null) {
            // since the effect is drawn vertically, effect width is display height
            mEffectRight.setSize(mDisplay.getHeight(), mWidth);
        }
        if (mEffectLeft != null) {
            mEffectLeft.setSize(mDisplay.getHeight(), mWidth);
        }
        if (mResponsibility && mBitmapArr != null && mBitmapArr.length > 0) {
            mTouchThreshold = Math.round((float) mWidth / (float) mBitmapArr.length / (float) mDistDivider);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // in case the device is not equipped with a valid gyro-sensor,
        // users can navigate the MAV file using fingers
        if (!mEnableTouchMode) {
            return true;
        }
        switch (e.getAction()) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
            // current motion finished, update the last index
            // and prepare for next motion event.
            mLastIndex = mLastTmpIndex ;
            if (mEffectRight != null) {
                mEffectRight.onRelease();
            }
            if (mEffectLeft != null) {
                mEffectLeft.onRelease();
            }
            break;
        }
        mGestureDetector.onTouchEvent(e);
        return true;
    }

    /* gesture callbacks from GestureDetector */
    public boolean onDown(MotionEvent e) {
        if (!mResponsibility) {
            // ignore all events before images are ready
            return true;
        }
        if (mFlingOnGoing && mFlingThread != null) {
            mFlingOnGoing = false;
            mFlingThread.interrupt();
            mEffectRight.finish();
            mEffectLeft.finish();
        }
        mScroller.forceFinished(true);
        mFlingDirection = FLING_DIRECTION_INVALID;
        mScrollDistance = 0;
        mOverScrollPoint = 0;
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        if (!mResponsibility) {
            // ignore all events before images are ready
            return true;
        }
        Log.d(TAG, "onFling: vX=" + velocityX + ", vY=" + velocityY);
        mLastIndex = mLastTmpIndex;
        mFlingDirection = velocityX < 0 ? FLING_DIRECTION_RIGHT : FLING_DIRECTION_LEFT; 
        
        final int initialVelocityX = (int) velocityX;
        // first stop previous fling
        if (mFlingOnGoing && mFlingThread != null) {
            mFlingOnGoing = false;
            mFlingThread.interrupt();
            mEffectRight.finish();
            mEffectLeft.finish();
        }
        if (mAbsorbingFling) {
            mAbsorbingFling = false;
        }
        
        // then start current fling
        int maxX = mWidth;
        final int startX = (mLastIndex + 1) * mTouchThreshold * mDistDivider;
        mScroller.fling(startX, 0, -initialVelocityX, 0, 0, maxX, 0, 0, 0, 0);
        mFlingThread = new Thread() {
            public void run() {
                mFlingOnGoing = true;
                while(!mScroller.isFinished() && mFlingOnGoing && !Thread.currentThread().isInterrupted()) {
                    mScroller.computeScrollOffset();
                    final int scrollX = (mScroller.getCurrX() - startX) / mDistDivider;       // we care only about X value
                    MAVView.this.post(new Runnable() {
                        public void run() {
                            scrollDeltaDistance(scrollX);
                        }
                    });
                    // draw over-scroll effect when necessary
                    if (mScroller.isOverScrolled() && !mAbsorbingFling) {
                        int curVelocity = (int) mScroller.getCurrVelocity();
                        if (mFlingDirection == FLING_DIRECTION_RIGHT && mEffectRight.isFinished()) {
                            mEffectRight.onAbsorb(curVelocity);
                            mAbsorbingFling = true;
                        } else if (mFlingDirection == FLING_DIRECTION_LEFT && mEffectLeft.isFinished()) {
                            mEffectLeft.onAbsorb(curVelocity);
                            mAbsorbingFling = true;
                        }
                        if (mAbsorbingFling) {
                            postInvalidate();
                        }
                    }
                    try {
                        // sampling interval is 20ms
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                mFlingOnGoing = false;
            }
        };
        mFlingThread.setName("scroller-fling");
        mFlingThread.start();
        return true;
    }


    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        if (!mResponsibility) {
            // ignore all events before images are ready
            return true;
        }
        
        if ((mScrollDistance == 0) 
                || (distanceX < 0 && mScrollDistance > 0) 
                || (distanceX > 0 && mScrollDistance < 0)) {
            // direction has changed
            // update last index
            mLastIndex = mLastTmpIndex;
            mOverScrollPoint = 0;
            mScrollDistance = distanceX;
            if (distanceX < 0) {
                if (!mEffectRight.isFinished()) {
                    mEffectRight.onRelease();
                }
            } else if (distanceX > 0) {
                if (!mEffectLeft.isFinished()) {
                    mEffectLeft.onRelease();
                }
            }
        } else {
            mScrollDistance += distanceX;
            if (mLastTmpIndex == 0 && distanceX < 0 && mOverScrollPoint == 0) {
                // over scroll started
                mOverScrollPoint = mScrollDistance;
            } else if (mLastTmpIndex == (mBitmapArr.length - 1) && distanceX > 0 && mOverScrollPoint == 0) {
                // over scroll started
                mOverScrollPoint = mScrollDistance;
            }
            final int startX = (mLastIndex + 1) * mTouchThreshold * mDistDivider;
            if (mLastTmpIndex == 0 && distanceX < 0) {
              if (!mEffectRight.isFinished()) {
                  mEffectRight.onRelease();
              }
              mEffectLeft.onPull((mScrollDistance - mOverScrollPoint) / mWidth);
              invalidate();
            } else if (mBitmapArr != null && mLastTmpIndex == (mBitmapArr.length - 1) && distanceX > 0) {
                if (!mEffectLeft.isFinished()) {
                    mEffectLeft.onRelease();
                }
                mEffectRight.onPull((mScrollDistance - mOverScrollPoint) / (float) mWidth);
                invalidate();
            }
        }
        
        scrollDeltaDistance((int) mScrollDistance);
        return true;
    }
    
    private void scrollDeltaDistance(int deltaX) {
        int cnt = deltaX / mTouchThreshold;     // number of passed frames for this scroll position
        if (cnt != 0) {     // positive or negative
            int tmpIndex = mLastIndex + cnt;
            if (tmpIndex < 0) {
                tmpIndex = 0;
            } else if (tmpIndex >= mBitmapArr.length) {
                tmpIndex = mBitmapArr.length - 1;
            }
            setImageBitmap(mBitmapArr[tmpIndex]);
            mLastTmpIndex = tmpIndex;
        }
    }


    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
    
    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw edge effects on ImageView's boundary
        if (!mEffectRight.isFinished()) {
            int saveCount = canvas.save();
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            canvas.rotate(90);
            canvas.translate(0, -w);
            if (mEffectRight.draw(canvas)) {
                invalidate();
            }
            canvas.restoreToCount(saveCount);
        }
        
        if (!mEffectLeft.isFinished()) {
            int saveCount = canvas.save();
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            canvas.rotate(-90);
            canvas.translate(-h, 0);
            if (mEffectLeft.draw(canvas)) {
                invalidate();
            }
            canvas.restoreToCount(saveCount);
        }
    }

}
