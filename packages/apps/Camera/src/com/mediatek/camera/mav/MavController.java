package com.mediatek.camera.mav;

import com.android.camera.R;
import com.android.camera.CameraHolder;
import com.mediatek.camera.ui.ProgressIndicator;
import com.android.camera.ui.Rotatable;

import com.mediatek.xlog.Xlog;
import android.hardware.Camera;
import android.hardware.Camera.Size;


import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class MavController implements Camera.MAVCallback {
	private static final String TAG = "MavController";

	private static final int IDLE = 0;
	private static final int STARTED = 1;
	private static final int MERGING = 2;		
	
	
	private static final int MAV_CAPTURE_NUM = 15;
	private int mCurrentNum = 0;
	private boolean mStopping;

	private ProgressIndicator mProgressIndicator;
    private Camera mCameraDevice;
	private Handler mHandler;
	private int mState;
	private boolean mStopProcess = false;
	private Object lock = new Object();
	
	public MavController(Activity activity,CaptureEventListener l) {
		initializeViews(activity);
		mProgressIndicator = new ProgressIndicator(activity,ProgressIndicator.TYPE_MAV);
		mProgressIndicator.setVisibility(View.GONE);
		mCaptureEventListener = l;

        mHandler = new Handler(Looper.getMainLooper());
	}

	public void setCamera(Camera camera) {
		Xlog.d(TAG, "setCamera mState: " + mState);
		mCameraDevice = camera;
	}
	
	private void initializeViews(Activity activity) {
		//mMavView = activity.findViewById(R.id.pano_view);
    }

	private void showGuideText(Context context, int type) {
		//TBD.
	}

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
			
			doStart();
			mProgressIndicator.setVisibility(View.VISIBLE);
            return true;
		} else {
			Xlog.d(TAG, "start mState: " + mState);
                        return false;
		}
	}

	private void stopAsync(final boolean isMerge) {
		Xlog.d(TAG, "stopAsync mStopping: " + mStopping);
		
		if (mStopping) return;
		
		mStopping = true;
        Thread stopThread = new Thread(new Runnable() {
        	public void run() {
				doStop(isMerge);
				mHandler.post(new Runnable() {
					 public void run() {
						 mStopping = false;
						 if (!isMerge) {//if isMerge is true, onHardwareStopped will be called in onFrame.
						 	onHardwareStopped(false);
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

	private void onHardwareStopped(boolean isMerge) {
		Xlog.d(TAG, "onHardwareStopped isMerge: " + isMerge);
		
		if (isMerge) {
		   mCameraDevice.setMAVCallback(null);								   
		}	
		
		if (mCaptureEventListener != null) {
			mCaptureEventListener.onCaptureDone(isMerge);
		}
	}

	public void stop(boolean isMerge) {
		Xlog.d(TAG, "stop mState: " + mState);
		
		if (mCameraDevice != null && mState == STARTED) {
			mState = isMerge ? MERGING : IDLE;
			if (!isMerge) {
			   mCameraDevice.setMAVCallback(null);						   		   
			} else {
			   if (mCaptureEventListener != null) {
			       mCaptureEventListener.onMergeStarted();
			   }	
			}

	        mProgressIndicator.setProgress(0);//reset to 0
	        mProgressIndicator.setVisibility(View.GONE);
			stopAsync(isMerge);
		} 
	}
		
		private void doStart() {
			Xlog.d(TAG, "doStart");
			
			mCameraDevice.setMAVCallback(this);
			mCameraDevice.startMAV(MAV_CAPTURE_NUM);
		}
		
		private void doStop(boolean isMerge) {	
			Xlog.d(TAG, "doStop isMerge "+isMerge);
			
			if (mCameraDevice != null) {
				CameraHolder holder = CameraHolder.instance();
				synchronized(holder) {
					if (holder.isSameCameraDevice(mCameraDevice)) {//means that hw was shutdown and no need to call stop anymore.
						mCameraDevice.stopMAV(isMerge ? 1 : 0);	
					} else {
						Xlog.w(TAG, "doStop device is release? ");
					}
				}
			}
		}

		
        public void onFrame() {
            Xlog.i(TAG, "onFrame: " + mCurrentNum + ",mState: "+mState);
			if (mState == IDLE) return;

			if (mCurrentNum == MAV_CAPTURE_NUM || mState == MERGING) {
                Xlog.i(TAG, "mav done");
		        mState = IDLE;
				onHardwareStopped(true);
            } else if (mCurrentNum >= 0 && mCurrentNum < MAV_CAPTURE_NUM) {
			   mProgressIndicator.setProgress((mCurrentNum+1) * ProgressIndicator.BLOCK_NUMBERS 
			   			/ MAV_CAPTURE_NUM);	   
	        } else {
                Xlog.w(TAG, "onFrame is called in abnormal state");
			}
			
			mCurrentNum++;
			if (mCurrentNum == MAV_CAPTURE_NUM) {				
				stop(true);
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

	public void setOrientation(int orientation) {
		mProgressIndicator.setOrientation(orientation);
	}
}

