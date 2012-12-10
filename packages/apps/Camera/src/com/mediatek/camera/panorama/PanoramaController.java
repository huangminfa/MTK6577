package com.mediatek.camera.panorama;

import com.android.camera.R;
import com.android.camera.CameraHolder;
import com.android.camera.Util;
import com.mediatek.camera.ui.ProgressIndicator;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;

import com.mediatek.xlog.Xlog;
import android.hardware.Camera;
import android.hardware.Camera.Size;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class PanoramaController implements Camera.AUTORAMAMVCallback
	,Camera.AUTORAMACallback {
	private static final String TAG = "PanoramaController";

	// ICS==>
	private View mPanoView;
	private ViewGroup mDirectionSigns[] = new ViewGroup[4];// up,down,left,right
	private ViewGroup mCenterIndicator;
	private View mNaviWindow;
	private ProgressIndicator mProgressIndicator;
	private Drawable mNormalWindowDrawable;
    private ViewGroup mCollimatedArrowsDrawable; 
	private Camera mCameraDevice;
	private Handler mHandler;
	private int mPreviewWidth;
	private int mPreviewHeight;
	// <==ICS

	private static final int DIRECTION_RIGHT = 0;
	private static final int DIRECTION_LEFT = 1;
	private static final int DIRECTION_UP = 2;
	private static final int DIRECTION_DOWN = 3;
	private static final int DIRECTION_UNKNOWN = 4;

	private int mDirection = DIRECTION_UNKNOWN;
	private int mState;
	private int mDisplayOrientaion;
	private boolean mStopProcess = false;
	private Object lock = new Object();

	private static final int IDLE = 0;
	private static final int STARTED = 1;
	private static final int MERGING = 2;

	private static final int TARGET_DISTANCE_HORIZONTAL = 160;
	private static final int TARGET_DISTANCE_VERTICAL = 120;
	private static final int PANO_3D_OVERLAP_DISTANCE = 32;

	private static final int NUM_3D_AUTORAMA_CAPTURE = 18;
	private static final int NUM_AUTORAMA_CAPTURE = 9;
	private int mCurrentNum = 0;

	private boolean mStopping;
	private boolean mShowingCollimatedDrawable;

	private int mHalfSourceWidth = 0;
	private int mHalfSourceHeight = 0;
	private AnimationController mAnimation;
	private Activity mActivity;

	public PanoramaController(Activity activity, CaptureEventListener l) {
		mActivity = activity;
		initializeViews(activity);
		mProgressIndicator = new ProgressIndicator(activity,
				ProgressIndicator.TYPE_PANO);
		mProgressIndicator.setVisibility(View.GONE);
		mCaptureEventListener = l;

		mHandler = new Handler(Looper.getMainLooper());
	}

	public void setCamera(Camera camera) {
		mCameraDevice = camera;
	}

	public void setDisplayOrientation(int displayOrientation) {
		mDisplayOrientaion = displayOrientation;
	}

	private void initializeViews(Activity activity) {
		mPanoView = activity.findViewById(R.id.pano_view);

		mDirectionSigns[DIRECTION_RIGHT] = (ViewGroup)activity
				.findViewById(R.id.pano_right);
		mDirectionSigns[DIRECTION_LEFT] = (ViewGroup)activity
				.findViewById(R.id.pano_left);
		mDirectionSigns[DIRECTION_UP] = (ViewGroup)activity
				.findViewById(R.id.pano_up);
		mDirectionSigns[DIRECTION_DOWN] = (ViewGroup)activity
				.findViewById(R.id.pano_down);

		mCenterIndicator = (ViewGroup) activity.findViewById(R.id.center_indicator);
		mNaviWindow = activity.findViewById(R.id.navi_window);
		mAnimation = new AnimationController(mDirectionSigns,
				(ViewGroup)mCenterIndicator.getChildAt(0));

		android.content.res.Resources res = activity.getResources();
		mNormalWindowDrawable = res
				.getDrawable(R.drawable.ic_pano_normal_window);
		mCollimatedArrowsDrawable = (ViewGroup)activity
                .findViewById(R.id.static_center_indicator);
	}

	public boolean hasCaptured() {
		Xlog.d(TAG, "hasCaptured mCurrentNum: " + mCurrentNum);
		return mState != IDLE && mCurrentNum > 0;
	}

	private void resetDirectionIcons() {
	    int visible = View.VISIBLE;
	    if (Util.getS3DMode()) {
	        setOrientationIndicator(DIRECTION_DOWN);
            mCenterIndicator.setVisibility(View.VISIBLE);
            mAnimation.startCenterAnimation();
            visible = View.GONE;
	    } else {
            mCenterIndicator.setVisibility(View.GONE);
	    }
		for (int i = 0; i < 4; i++) {
			mDirectionSigns[i].setSelected(false);
			mDirectionSigns[i].setVisibility(visible);
		}
	}

	private void updateDirection(int direction) {
		Xlog.d(TAG, "updateDirection mDirection: " + mDirection
				+ " direction: " + direction);
		if (mDirection != direction) {
			mDirection = direction;
			if (mDirection != DIRECTION_UNKNOWN) {
				((PanoramaActivity) mActivity).movingTips();
				setOrientationIndicator(direction);
				mCenterIndicator.setVisibility(View.VISIBLE);
				mAnimation.startCenterAnimation();
				for (int i = 0; i < 4; i++) {
					mDirectionSigns[i].setVisibility(View.INVISIBLE);
				}
			} else {
				mCenterIndicator.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void update3DDirection(int direction) {
		if (mDirection != direction) {
			mDirection = direction;
			if (mDirection != DIRECTION_UNKNOWN) {
				((PanoramaActivity) mActivity).movingTips();
				if (mDirection == DIRECTION_RIGHT) {
					setOrientationIndicator(DIRECTION_RIGHT);
					mCenterIndicator.setVisibility(View.VISIBLE);
					mAnimation.startCenterAnimation();
					mDirectionSigns[DIRECTION_RIGHT].setVisibility(View.INVISIBLE);
				} else {
					//
				}
			} else {
				mDirectionSigns[DIRECTION_RIGHT].setVisibility(View.VISIBLE);
				mAnimation.startDirectionAnimation();
			}
		}
	}

	public View.OnClickListener mOkOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (mCaptureEventListener != null) {
				mCaptureEventListener.onKeyPressed(true);
			}
		}
	};

	public View.OnClickListener mCancelOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (mCaptureEventListener != null) {
				mCaptureEventListener.onKeyPressed(false);
			}
		}
	};

	private CaptureEventListener mCaptureEventListener;

	public interface CaptureEventListener {
		void onCaptureDone(boolean isMerge);

		void onMergeStarted();

		void onKeyPressed(boolean okKey);
	}

	public boolean start() {
		if (mCameraDevice != null && mState == IDLE && !mStopping) {
			mState = STARTED;
			mCurrentNum = 0;
			mShowingCollimatedDrawable = false;
			mDirection = DIRECTION_UNKNOWN;

			doStart();

			resetDirectionIcons();
			mPanoView.setVisibility(View.VISIBLE);
			mProgressIndicator.setVisibility(View.VISIBLE);
			return true;
		} else {
			Xlog.d(TAG, "start mState: " + mState);
			return false;
		}
	}

	private void stopAsync(final boolean isMerge) {
		Xlog.d(TAG, "stopAsync mStopping: " + mStopping);

		if (mStopping)
			return;

		mStopping = true;
		Thread stopThread = new Thread(new Runnable() {
			public void run() {
				doStop(isMerge);
				mHandler.post(new Runnable() {
					public void run() {
						mStopping = false;
						if (!isMerge) {
							// if isMerge is true, onHardwareStopped
							// will be called in onCapture.
							onHardwareStopped(false);
							hidePanoramaUI();
						}
					}
				});

				synchronized (lock) {
					mStopProcess = false;
					lock.notifyAll();
				}
			}
		});
		synchronized (lock) {
			mStopProcess = true;
		}
		stopThread.start();
	}

	private void doStart() {
		Xlog.d(TAG, "doStart");
		mCameraDevice.setAUTORAMACallback(this);
		mCameraDevice.setAUTORAMAMVCallback(this);
		if (Util.getS3DMode()) {
		    mCameraDevice.start3DSHOT(NUM_AUTORAMA_CAPTURE);
		} else {
            mCameraDevice.startAUTORAMA(NUM_AUTORAMA_CAPTURE);
		}
	}

	private void doStop(boolean isMerge) {
		Xlog.d(TAG, "doStop isMerge " + isMerge);

		if (mCameraDevice != null) {
			CameraHolder holder = CameraHolder.instance();
			synchronized (holder) {
				if (holder.isSameCameraDevice(mCameraDevice)) {
					// means that hw was shutdown 
					// and no need to call stop anymore.
                    if (Util.getS3DMode()) {
                        mCameraDevice.stop3DSHOT(isMerge ? 1 : 0);
                    } else {
                        mCameraDevice.stopAUTORAMA(isMerge ? 1 : 0);
                    }
				} else {
					Xlog.w(TAG, "doStop device is release? ");
				}
			}
		}
	}

	private void onHardwareStopped(boolean isMerge) {
		Xlog.d(TAG, "onHardwareStopped isMerge: " + isMerge);

		if (isMerge) {
			mCameraDevice.setAUTORAMACallback(null);
			mCameraDevice.setAUTORAMAMVCallback(null);
		}

		if (mCaptureEventListener != null) {
			mCaptureEventListener.onCaptureDone(isMerge);
		}
	}

	public void hidePanoramaUI() {
        mProgressIndicator.setProgress(0);// reset to 0
        mProgressIndicator.setVisibility(View.INVISIBLE);
        mPanoView.setVisibility(View.INVISIBLE);
        mNaviWindow.setVisibility(View.INVISIBLE);
	}

	public void stop(boolean isMerge) {
		Xlog.d(TAG, "stop mState: " + mState);

		if (mCameraDevice != null && mState == STARTED) {
			mState = isMerge ? MERGING : IDLE;
			if (!isMerge) {
				mCameraDevice.setAUTORAMACallback(null);
				mCameraDevice.setAUTORAMAMVCallback(null);
			} else {
				if (mCaptureEventListener != null) {
					mCaptureEventListener.onMergeStarted();
				}
			}
			stopAsync(isMerge);
			mDirection = DIRECTION_UNKNOWN;
			mCenterIndicator.setVisibility(View.INVISIBLE);
		}
	}

	public void onCapture() {
		Xlog.i(TAG, "onCapture: " + mCurrentNum + ",mState: " + mState);
		if (mState == IDLE)
			return;

		if (mCurrentNum == NUM_AUTORAMA_CAPTURE || mState == MERGING) {
			Xlog.i(TAG, "autorama done");
			mState = IDLE;
			onHardwareStopped(true);
		} else if (mCurrentNum >= 0 && mCurrentNum < NUM_AUTORAMA_CAPTURE) {
            mProgressIndicator.setProgress(mCurrentNum + 1);
			if (mCurrentNum == 0 && !Util.getS3DMode()) {
				mAnimation.startDirectionAnimation();
			} else if (0 < mCurrentNum) {
			    if (mCurrentNum == 1) {
			        ((PanoramaActivity) mActivity).mDoneButton.setEnabled(true);
			    }
	            mNaviWindow.setVisibility(View.INVISIBLE);
	            mAnimation.stopCenterAnimation();
	            mCenterIndicator.setVisibility(View.GONE);
	            mCollimatedArrowsDrawable.setVisibility(View.VISIBLE);
	            if (mShowingCollimatedDrawable) {
	                 mHandler.removeCallbacksAndMessages(null);
	            }
	            mShowingCollimatedDrawable = true;
	            mHandler.postDelayed(new Runnable() {
	                 public void run() {
	                     mShowingCollimatedDrawable = false;
	                     mCollimatedArrowsDrawable.setVisibility(View.GONE);
	                     mAnimation.startCenterAnimation();
	                     mCenterIndicator.setVisibility(View.VISIBLE);
	                 }
	            }, 500);
			}
		} else {
			Xlog.w(TAG, "onCapture is called in abnormal state");
		}

		mCurrentNum++;
		if (mCurrentNum == NUM_AUTORAMA_CAPTURE) {
			stop(true);
			mAnimation.stopCenterAnimation();
			mCenterIndicator.setVisibility(View.INVISIBLE);
		}
	}

    private void drawNaviWindow() {
        int sourceCenterX = mPanoView.getWidth() / 2;
        int sourceCenterY = mPanoView.getHeight() / 2 + 64;
        if (mHalfSourceWidth == 0) {
            mHalfSourceWidth = mNaviWindow.getWidth() >> 1;
            mHalfSourceHeight = mNaviWindow.getHeight() >> 1;
        }
        mNaviWindow.setRotation(0);
        Log.v("Test", "sourceCenterX=" + sourceCenterX + " sourceCenterY=" + sourceCenterY+ " mCurrentNum="+mCurrentNum);
        mNaviWindow.layout(sourceCenterX - mHalfSourceWidth, sourceCenterY - mHalfSourceHeight,
                sourceCenterX + mHalfSourceWidth, sourceCenterY + mHalfSourceHeight);
        if (mCurrentNum == 1) mNaviWindow.setVisibility(View.VISIBLE);
    }

	public void onFrame(int xy, int direction) {

		if (direction == DIRECTION_UNKNOWN || mShowingCollimatedDrawable
				|| mState != STARTED || mCurrentNum < 1) {
            if (Util.getS3DMode() && mCurrentNum <= 1) drawNaviWindow();
			return;
		}
		// Xlog.i(TAG, "onFrame xy = "+xy + " direction = "+direction);
		short x = (short) ((xy & 0xFFFF0000) >> 16);
		short y = (short) (xy & 0x0000FFFF);
		if (Util.getS3DMode()) {
			// assume that the activity's requested orientation is same as the lcm
			// orientation. if not,the following caculation would be wrong!!
			if (mDisplayOrientaion == 180) {
				direction = 1 - direction;
				x = (short) -x;
				y = (short) -y;
			}
			Log.i(TAG, "direction " + direction + " x:" + x + " y: " + y);

			int halfPrewWidth = mPreviewWidth >> 1;
			int halfPrewHeight = mPreviewHeight >> 1;

			if (mHalfSourceWidth == 0) {
				mHalfSourceWidth = mNaviWindow.getWidth() >> 1;
				mHalfSourceHeight = mNaviWindow.getHeight() >> 1;
			}

			int sourceCenterY = 0;
			int sourceCenterX = 0;

			// M:Due to screenOrientatino is set to portrait, so x and y is somehow
			// exchanged to fit this layout.
			if (direction != DIRECTION_UNKNOWN) {
	            int moveDistanceHorizontal = 64;
                sourceCenterY = halfPrewWidth + moveDistanceHorizontal -
                        (moveDistanceHorizontal * x) / PANO_3D_OVERLAP_DISTANCE;
				sourceCenterX = halfPrewHeight +
						(halfPrewHeight * y) / 240;
			}
			Log.i(TAG, "After transfer CenterX: " + sourceCenterX + " CenterY " + sourceCenterY);

            Center center = clampCenterXY(sourceCenterX, sourceCenterY);
            sourceCenterX = center.centerX;
            sourceCenterY = center.centerY;

			mNaviWindow.layout(sourceCenterX - mHalfSourceWidth, sourceCenterY - mHalfSourceHeight,
					sourceCenterX + mHalfSourceWidth, sourceCenterY + mHalfSourceHeight);

			if (mDisplayOrientaion == 90 && direction != DIRECTION_UNKNOWN) {
				direction = 3 - direction;
			}
			mNaviWindow.setVisibility(View.VISIBLE);
			return;
		} else {
			// assume that the activity's requested orientation is same as the lcm
			// orientation. if not,the following caculation would be wrong!!
			if (mDisplayOrientaion == 180) {
				if (direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) {
					direction = 1 - direction;
				} else {
					direction = 5 - direction;
				}
				x = (short) -x;
				y = (short) -y;
			}
			Log.i(TAG, "direction " + direction + " x:" + x + " y: " + y);

			int halfPrewWidth = mPreviewWidth >> 1;
			int halfPrewHeight = mPreviewHeight >> 1;

			if (mHalfSourceWidth == 0) {
				mHalfSourceWidth = mNaviWindow.getWidth() >> 1;
				mHalfSourceHeight = mNaviWindow.getHeight() >> 1;
			}

			int sourceCenterY = 0;
			int sourceCenterX = 0;

			int moveDistanceHorizontal = halfPrewWidth - mHalfSourceHeight;
			int moveDistanceVertical = halfPrewHeight - mHalfSourceHeight;

			// M:Due to screenOrientatino is set to portrait, so x and y is somehow
			// exchanged to fit this layout.
			switch (direction) {
			case DIRECTION_RIGHT:
				sourceCenterY = mPreviewWidth - mHalfSourceHeight -
						(moveDistanceHorizontal * x) / TARGET_DISTANCE_HORIZONTAL;
				sourceCenterX = halfPrewHeight +
						(halfPrewHeight * y) / TARGET_DISTANCE_VERTICAL;
				mNaviWindow.setRotation(0);
				break;
			case DIRECTION_LEFT:
				sourceCenterY = mHalfSourceHeight - 
						(moveDistanceHorizontal * x) / TARGET_DISTANCE_HORIZONTAL;
				sourceCenterX = halfPrewHeight + (halfPrewHeight * y) / TARGET_DISTANCE_VERTICAL;

                mNaviWindow.setRotation(180);
				break;
			case DIRECTION_UP:
				sourceCenterY = halfPrewWidth -
						(halfPrewWidth * x) / TARGET_DISTANCE_HORIZONTAL;
				sourceCenterX = halfPrewHeight + moveDistanceVertical +
						(moveDistanceVertical * y) / TARGET_DISTANCE_VERTICAL;

				mNaviWindow.setRotation(-90);
				break;
			case DIRECTION_DOWN:
				sourceCenterY = halfPrewWidth -
						(halfPrewWidth * x) / TARGET_DISTANCE_HORIZONTAL;
				sourceCenterX = mHalfSourceHeight + 
						(moveDistanceVertical * y) / TARGET_DISTANCE_VERTICAL;

                mNaviWindow.setRotation(90);
				break;
			}
			Log.i(TAG, "After transfer CenterX: " + sourceCenterX + " CenterY " + sourceCenterY);

			Center center = clampCenterXY(sourceCenterX, sourceCenterY);
			sourceCenterX = center.centerX;
			sourceCenterY = center.centerY;

			mNaviWindow.layout(sourceCenterX - mHalfSourceWidth, sourceCenterY - mHalfSourceHeight,
					sourceCenterX + mHalfSourceWidth, sourceCenterY + mHalfSourceHeight);

			if (mDisplayOrientaion == 90) {
				if (direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) {
					direction = 3 - direction;
				} else {
					direction -= 2;
				}
			}
			updateDirection(direction);
			mNaviWindow.setVisibility(View.VISIBLE);
		}
	}

	public void checkStopProcess() {
		while (mStopProcess) {
			waitLock();
		}
	}

	private void waitLock() {
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (Exception e) {
			// do nothing
		}
	}

	public Center clampCenterXY(int x, int y) {
		if (x < mHalfSourceWidth) x = mHalfSourceWidth;
		if (x + mHalfSourceWidth > mPanoView.getWidth()) x = mPanoView.getWidth() - mHalfSourceWidth;

		if (y < mHalfSourceHeight) y = mHalfSourceHeight;
		if (y + mHalfSourceHeight > mPanoView.getHeight()) y = mPanoView.getHeight() - mHalfSourceHeight;
		return new Center(x, y);
	}

	private class Center {
		int centerX;
		int centerY;
		public Center(int x, int y) {
			centerX = x;
			centerY = y;
		}
	}

	public void setOrientation(int orientation) {
		mProgressIndicator.setOrientation(orientation);
	}

	public void setOrientationIndicator(int direction) {
		if (direction == DIRECTION_RIGHT) {
			((Rotatable)mCenterIndicator).setOrientation(0);
			((Rotatable)mCollimatedArrowsDrawable).setOrientation(0);
		} else if (direction == DIRECTION_LEFT) {
			((Rotatable)mCenterIndicator).setOrientation(180);
			((Rotatable)mCollimatedArrowsDrawable).setOrientation(180);
		} else if (direction == DIRECTION_UP) {
			((Rotatable)mCenterIndicator).setOrientation(90);
			((Rotatable)mCollimatedArrowsDrawable).setOrientation(90);
		} else if (direction == DIRECTION_DOWN) {
			((Rotatable)mCenterIndicator).setOrientation(270);
			((Rotatable)mCollimatedArrowsDrawable).setOrientation(270);
		}
	}

	public void setPreviewSize(int width, int height) {
		mPreviewWidth = width;
		mPreviewHeight = height;
	}
}

