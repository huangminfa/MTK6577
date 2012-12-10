package com.android.gallery3d.mpo;

import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Process;
import android.os.SystemClock;
import android.util.AttributeSet;

import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.EdgeEffect;
import android.widget.OverScroller;
import android.app.Activity;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Log;

public class MAVView3D extends SurfaceView 
        implements OnGestureListener, SensorEventListener, Callback {
//public class MAVView3D extends TextureView 
//        implements OnGestureListener, SensorEventListener, Callback, TextureView.SurfaceTextureListener {
	
    private static final String TAG = "MAVView3D";
    private Bitmap[] mBitmapArr;
    private boolean mFirstTime = true;
    private int mLastIndex = 0xFFFF;
    private static int BASE_ANGLE = 15;
    private Matrix mBaseMatrix = new Matrix();
    private boolean mResponsibility = false;
    
    private float mValue = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp = 0;
    private float angle[] = {0,0,0};
    private static final float TH = 0.001f;
    private static final float OFFSET = 0.0f;
    
    private Bitmap mCurrentBitmap = null;
    private Bitmap mNextBitmap = null;
    private Matrix mDrawMatrix = new Matrix();
    private Matrix mLeftDrawMatrix = null;
    private Matrix mRightDrawMatrix = null;
    private Object mRenderLock = new Object();
    private MavRenderThread mRenderThread = null;
    public boolean mRenderRequested = false;
    private Paint mPaint;
    private boolean mStereoMode = false;
    private SurfaceHolder mHolder = null;
    private boolean mSurfaceReady = false;
    private long mLastFrameTime = 0;
    private static final long TARGET_FRAME_TIME = 10;
    private int mMaxIndex = 0;

    public MAVView3D(Context context) {
        super(context);
        initView(context);
    }
    
    public MAVView3D(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        Log.d(TAG, "onLayout");
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!mResponsibility) {
            return;
        }
        //workaround for Gyro sensor HW limitation.
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
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    
    public void setImageRotation(int rotation) {
    	  mImageRotation = rotation;
Log.v(TAG,"setImageRotation(rotation="+rotation+")");
    }
    
    public void setImageBitmap(Bitmap bm) {
        if (!checkSelf() || !checkBitmap(bm)) {
            Log.w(TAG,"setImageBitmap:either Bitmap or ImageView's dimen invalid");
            return;
        }
        Log.d(TAG, ">> setImageBitmap");
//        new RuntimeException().fillInStackTrace().printStackTrace();
        
        mCurrentBitmap = bm;
        
        if (mStereoMode) {
            if (mBitmapArr != null && mBitmapArr.length > 1) {
                int nextIndex = -1;
                int arrayLen = mBitmapArr.length;
                // find the current bitmap index in bitmap array
                for (int i = 0; i < arrayLen; ++i) {
                    if (mCurrentBitmap == mBitmapArr[i]) {
                        nextIndex = i;
                        break;
                    }
                }
                if (nextIndex < 0) {
                    // not found, use current bitmap as well
                    mNextBitmap = mCurrentBitmap;
                } else if (nextIndex > arrayLen - 1) {
                    nextIndex = arrayLen - 1;
                } else {
                    ++nextIndex;
                }
                if (nextIndex >= 0 && nextIndex < arrayLen) {
                    mNextBitmap = mBitmapArr[nextIndex];
                }
            } else {
                mNextBitmap = mCurrentBitmap;
            }
        } else {
            mNextBitmap = mCurrentBitmap;
        }
        
        //change imageView's matrix
        setBitmapMatrix(bm);
        //invalidate();
        //Log.d(TAG, "setImageBitmap is called from thread: " + Thread.currentThread());
        //postInvalidate();
//        Canvas c = getHolder().lockCanvas();
//        onDraw(c);
//        getHolder().unlockCanvasAndPost(c);
        requestRender();
    }
    
    public void setBitmapArr(Bitmap[] bitmapArr) {
        mBitmapArr = bitmapArr;
        if (mBitmapArr != null) {
            mMaxIndex = mBitmapArr.length;
        }
    }

    private boolean checkBitmap(Bitmap bitmap) {
        if (null == bitmap) {
            Log.w(TAG,"checkBitmap: Bitmap is null!");
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
            Log.w(TAG,"checkSelf:invalid dimension of current view!");
            return false;
        }
        return true;
    }

    private void setBitmapMatrix(Bitmap bitmap) {
        if (!checkSelf() || !checkBitmap(bitmap)) {
            Log.w(TAG,"setBitmapMatrix:either Bitmap or ImageView's dimen invalid");
            mBaseMatrix.reset();
            mDrawMatrix = mBaseMatrix;
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
        mDrawMatrix = mBaseMatrix;
        
        // set matrix for left/right image
        mLeftDrawMatrix = new Matrix(mDrawMatrix);
        mLeftDrawMatrix.postScale((float) 0.5, 1);
        
        mRightDrawMatrix = new Matrix(mDrawMatrix);
        mRightDrawMatrix.postScale((float) 0.5, 1);
        mRightDrawMatrix.postTranslate(viewWidth / 2F, 0);
        //invalidate();
        Log.i(TAG, " setImageBitmap => requestRender");
        requestRender();
        Log.i(TAG, " setImageBitmap <= requestRender");
        Log.d(TAG, "<< setImageBitmap");
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

    
    // render thread for drawing continuous
    private class MavRenderThread extends Thread {
        @Override
        public void run() {
            //Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            while (true) {
                synchronized (mRenderLock) {
                    if (mRenderRequested) {
                        mRenderRequested = false;
                    } else {
                        try {
                            mRenderLock.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                
                try {
                    if (mSurfaceReady) {
                        Canvas c = mHolder.lockCanvas();
//                        Canvas c = lockCanvas();
                        if (c != null) {
                            drawOnCanvas(c);
                        }
                        mHolder.unlockCanvasAndPost(c);
//                        unlockCanvasAndPost(c);
                    } else {
                        Log.w(TAG, "render is called before surface is ready!");
                    }
                } catch (Exception e) {
                }
                
            }
        }
    }
    
    private void requestRender() {
        long time = SystemClock.uptimeMillis();
        synchronized (mRenderLock) {
            mRenderRequested = true;
            mRenderLock.notifyAll();
//            Log.w(TAG, "render requested");
        }
        time = SystemClock.uptimeMillis() - time;
        Log.i(TAG, "request render consumed " + time + "ms");
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
    // mDistDivider is used to decide what distance user has to scroll on the screen
    // to pass through all of the frames of this MPO file
    private int mDistDivider = 3;
    private float mOverScrollPoint = 0;

    
    private void initView(Context context) {
        mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
        mGestureDetector = new GestureDetector(context, this);
        mScroller = new OverScroller(context);
        mEffectRight = new EdgeEffect(context);
        mEffectLeft = new EdgeEffect(context);
        updateDimensionParams();
        Log.i(TAG, "initView: initial rotation=" + mOrientation);
        // prepare paint object
        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        
        mHolder = getHolder();
        mHolder.addCallback(this);
        // start render thread
        mRenderThread = new MavRenderThread();
        mRenderThread.setName("MAVView3D-render");
        mRenderThread.start();
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
        Log.d(TAG, "onTouchEvent: (" + e.getX() + ", " + e.getY() + ")");
        long time = SystemClock.uptimeMillis();
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
        Log.i(TAG, " onTouchEvent: => gesture detector");
        mGestureDetector.onTouchEvent(e);
        Log.i(TAG, " onTouchEvent: <= gesture detector");
        time = SystemClock.uptimeMillis() - time;
        Log.d(TAG, "onTouchEvent consumed: " + time + "ms");
        return true;
    }

    /* gesture callbacks from GestureDetector */
    public boolean onDown(MotionEvent e) {
        Log.e(TAG, "onDown: (" + e.getX() + ", " + e.getY() + ")");
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
        Log.e(TAG, "onFling");
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
                    MAVView3D.this.post(new Runnable() {
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
                            //postInvalidate();
                            requestRender();
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
        Log.e(TAG, "onScroll: distX=" + distanceX);
        long time = SystemClock.uptimeMillis();
        
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
            boolean overRightEdge = false;
            if (mBitmapArr != null) {
                overRightEdge = !mStereoMode && mLastTmpIndex == (mBitmapArr.length - 1);
                overRightEdge |= mStereoMode && mLastTmpIndex == (mBitmapArr.length - 2);
                overRightEdge &= distanceX > 0;
            }
            if (mLastTmpIndex == 0 && distanceX < 0) {
              if (!mEffectRight.isFinished()) {
                  mEffectRight.onRelease();
              }
              mEffectLeft.onPull((mScrollDistance - mOverScrollPoint) / mWidth);
              //invalidate();
              requestRender();
            } else if (overRightEdge) {/*mBitmapArr != null && mLastTmpIndex == (mBitmapArr.length - 1) && distanceX > 0*/
                if (!mEffectLeft.isFinished()) {
                    mEffectLeft.onRelease();
                }
                mEffectRight.onPull((mScrollDistance - mOverScrollPoint) / (float) mWidth);
                //invalidate();
                requestRender();
            }
        }
        
        Log.d(TAG, "onScroll: => scrollDeltaDistance");
        scrollDeltaDistance((int) mScrollDistance);
        Log.d(TAG, "onScroll: <= scrollDeltaDistance");
        time = SystemClock.uptimeMillis() - time;
        Log.d(TAG, "onScroll consumed: " + time + "ms");
        return true;
    }
    
    private void scrollDeltaDistance(int deltaX) {
        if (mBitmapArr == null || mBitmapArr.length == 0) {
            Log.w(TAG, "scrollDeltaDistance: bitmap array is NOT ready!!");
            return;
        }
        int arrLen = mBitmapArr.length;
        int cnt = deltaX / mTouchThreshold;     // number of passed frames for this scroll position
        if (cnt != 0) {     // positive or negative
            int tmpIndex = mLastIndex + cnt;
            if (!mStereoMode) {
                tmpIndex = Utils.clamp(tmpIndex, 0, arrLen - 1);
            } else {
                tmpIndex = Utils.clamp(tmpIndex, 0, arrLen - 2);
            }

            if (tmpIndex != mLastTmpIndex) {
                setImageBitmap(mBitmapArr[tmpIndex]);
                mLastTmpIndex = tmpIndex;
            }
        }
    }


    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
    
    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    public void drawOnCanvas(Canvas canvas) {
        long time = SystemClock.uptimeMillis();
        Log.d(TAG, ">> onDraw: canvas=" + canvas);
        boolean needRedraw = false;
        int canvasW = canvas.getWidth();
        int canvasH = canvas.getHeight();
        //super.onDraw(canvas);
        
        // clear previous content first
        Log.i(TAG, " onDraw: => draw black");
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.BLACK);
        Log.i(TAG, " onDraw: <= draw black");
        // we have to manually draw the bitmap onto the canvas
        // the matrix is also considered
        canvas.save();
        if (!mStereoMode) {
//            Log.i(TAG, " onDraw: => draw single");
            Log.i(TAG, " onDraw: bitmap w=" + mCurrentBitmap.getWidth() + ", h=" + mCurrentBitmap.getHeight());
            Log.i(TAG, " onDraw => drawBitmap");
            canvas.drawBitmap(mCurrentBitmap, mDrawMatrix, mPaint);
//            canvas.drawBitmap(mCurrentBitmap, 0, 0, null);
            Log.i(TAG, " onDraw <= drawBitmap");
//            canvas.concat(mDrawMatrix);
//            canvas.drawBitmap(mCurrentBitmap, 0, 0, mPaint);
//            Log.d(TAG, " onDraw: <= draw single");
        } else {
            // draw left and right bitmaps independently
//            Log.i(TAG, " onDraw: => draw left");
            canvas.drawBitmap(mCurrentBitmap, mLeftDrawMatrix, mPaint);
//            Log.i(TAG, " onDraw: <= draw left");
//            Log.i(TAG, " onDraw: => draw right");
            canvas.drawBitmap(mNextBitmap, mRightDrawMatrix, mPaint);
//            Log.i(TAG, " onDraw: <= draw right");
        }

        canvas.restore();
        
        // draw edge effects on ImageView's boundary
        Log.d(TAG, " onDraw: => draw effect right");
        if (!mEffectRight.isFinished()) {
            if (!mStereoMode) {
                int saveCount = canvas.save();
                canvas.rotate(90);
                canvas.translate(0, -canvasW);
                
                needRedraw |= mEffectRight.draw(canvas);
                canvas.restoreToCount(saveCount);
            } else {
                // for stereo mode draw effect 2 times
                int saveCount = canvas.save();
                canvas.scale(0.5f, 1f);
                canvas.rotate(90);
                canvas.translate(0, -canvasW);
                needRedraw |= mEffectRight.draw(canvas);
                canvas.translate(0, -canvasW);
                needRedraw |= mEffectRight.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
        Log.d(TAG, " onDraw: <= draw effect right");
        
        Log.d(TAG, " onDraw: => draw effect left");
        if (!mEffectLeft.isFinished()) {
            if (!mStereoMode) {
                int saveCount = canvas.save();
                canvas.rotate(-90);
                canvas.translate(-canvasH, 0);
                needRedraw |= mEffectLeft.draw(canvas);
                canvas.restoreToCount(saveCount);
            } else {
                // for stereo mode draw effect 2 times
                int saveCount = canvas.save();
                canvas.scale(0.5f, 1f);
                canvas.rotate(-90);
                canvas.translate(-canvasH, 0);
                needRedraw |= mEffectLeft.draw(canvas);
                canvas.translate(0, canvasW);
                needRedraw |= mEffectLeft.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
        Log.d(TAG, " onDraw: <= draw effect left");
        

        
        // constraint the frame rate
//      long end = SystemClock.uptimeMillis();
//        if (mLastFrameTime != 0) {
//            long wait = SystemClock.uptimeMillis() + TARGET_FRAME_TIME - end;
//            if (wait > 0) {
//                SystemClock.sleep(wait);
//            }
//        }
//        mLastFrameTime = SystemClock.uptimeMillis();
        
        if (needRedraw) {
            requestRender();
        }

        time = SystemClock.uptimeMillis() - time;
        Log.d(TAG, "<< onDraw: consumed " + time + "ms");
    }
    
    public void toggleStereoMode() {
        mStereoMode  = !mStereoMode;
//        if (mStereoMode) {
//            set3DLayout(WindowManager.LayoutParams.LAYOUT3D_SIDE_BY_SIDE);
//            setImageBitmap(mCurrentBitmap);
//        } else {
//            set3DLayout(WindowManager.LayoutParams.LAYOUT3D_DISABLED);
//        }
        
        int flagsEx = mStereoMode ? (WindowManager.LayoutParams.FLAG_EX_S3D_3D | 
                WindowManager.LayoutParams.FLAG_EX_S3D_SIDE_BY_SIDE) : (WindowManager.LayoutParams.FLAG_EX_S3D_2D);
        setFlagsEx(flagsEx, WindowManager.LayoutParams.FLAG_EX_S3D_MASK);
        
        if (mStereoMode) {
            setImageBitmap(mCurrentBitmap);
        }
        requestRender();
    }
    
    public boolean getStereoMode() {
        return mStereoMode;
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        holder.setFormat(PixelFormat.RGBA_8888);
        mSurfaceReady = true;
        if (mRenderThread == null) {
            mRenderThread = new MavRenderThread();
            mRenderThread.setName("MAVView3D-render");
            mRenderThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mSurfaceReady = false;
        mRenderThread.interrupt();
        boolean wait = true;
        while (wait) {
            try {
                mRenderThread.join();
                wait = false;
            } catch (InterruptedException e) {
            }
        }
        mRenderThread = null;
    }

//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
//            int height) {
//        mSurfaceReady = true;
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        mSurfaceReady = false;
//        mRenderThread.interrupt();
//        boolean wait = true;
//        while (wait) {
//            try {
//                mRenderThread.join();
//                wait = false;
//            } catch (InterruptedException e) {
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
//            int height) {
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//    }

}
