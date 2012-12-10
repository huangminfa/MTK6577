package com.mediatek.camera.ui;

import com.android.camera.R;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateLayout;

import android.app.Activity;
import android.view.View;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mediatek.xlog.Xlog;

public class ProgressIndicator {
	
	private static final String TAG = "ProgressIndicator";
	public static final int TYPE_MAV = 1;
	public static final int TYPE_PANO = 2;
	public static final int TYPE_SINGLE3D = 3;

	public static final int BLOCK_NUMBERS = 9;
	public static final int BLOCK_NUMBERS_SINGLE3D = 2;

	//those sizes are designed for mdpi in pixels, you need to change progress_indicator.xml when change the values here.
	private final int mPanoBlockSizes[] = {17, 15, 13, 12, 11, 12, 13, 15, 17};
	private final int mMavBlockSizes[] =  {11, 12, 13, 15, 17, 15, 13, 12, 11};
	private final int mSingle3DBlockSizes[] =  {11, 11};
	
	public static final int MAV_CAPTURE_NUM = 15;
	public static final int PANORAMA_CAPTURE_NUM = 9;

	private int mBlockPadding = 4;

	private View mProgressView;
	private ImageView mProgressBars;
	private final int mOrientation;
	private static final int HEIGHT_WITH_ZOOMBAR = 30;
	private static final int HEIGHT_WITHOUT_ZOOMBAR = 5; 
	
	public ProgressIndicator(Activity activity, int indicatorType) {
		mProgressView = activity.findViewById(R.id.progress_indicator);
		mProgressView.setVisibility(View.VISIBLE);
		mProgressBars = (ImageView)activity.findViewById(R.id.progress_bars);
		mOrientation = activity.getRequestedOrientation();
		
		Resources res = activity.getResources();
		final float scale = res.getDisplayMetrics().density;
		if (indicatorType == TYPE_MAV) {
			if (scale != 1.0f) {
				mBlockPadding = (int)(mBlockPadding * scale + 0.5f);
				for (int i = 0; i < BLOCK_NUMBERS; i++) {
					mMavBlockSizes[i] = (int)(mMavBlockSizes[i] * scale + 0.5f);
				}
			}
			mProgressBars.setImageDrawable(new ProgressBarDrawable(activity,mProgressBars,mMavBlockSizes
				,mBlockPadding));
		} else if (indicatorType == TYPE_PANO) {
			if (scale != 1.0f) {
				mBlockPadding = (int)(mBlockPadding * scale + 0.5f);
				for (int i = 0; i < BLOCK_NUMBERS; i++) {
					mPanoBlockSizes[i] = (int)(mPanoBlockSizes[i] * scale + 0.5f);
					Xlog.i(TAG, "mPanoBlockSizes[i]: " + mPanoBlockSizes[i]);
				}
			}		
			mProgressBars.setImageDrawable(new ProgressBarDrawable(activity,mProgressBars,mPanoBlockSizes
				,mBlockPadding));
		} else if (indicatorType == TYPE_SINGLE3D) {
			if (scale != 1.0f) {
				mBlockPadding = (int)(mBlockPadding * scale + 0.5f);
				for (int i = 0; i < BLOCK_NUMBERS_SINGLE3D; i++) {
					mSingle3DBlockSizes[i] = (int)(mSingle3DBlockSizes[i] * scale + 0.5f);
				}
			}
			mProgressBars.setImageDrawable(new ProgressBarDrawable(activity,mProgressBars,mSingle3DBlockSizes
				,mBlockPadding));
		}
		setOrientation(0);
	}

    public void setVisibility(int visibility) {
		mProgressView.setVisibility(visibility);
	}

	public void setProgress(int progress) {
		Xlog.i(TAG, "setProgress: " + progress);
		mProgressBars.setImageLevel(progress);
	}

	public void setOrientation(int orientation) {
        LinearLayout progressViewLayout = (LinearLayout) mProgressView;
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(progressViewLayout.getLayoutParams());
        if (orientation == 0 || orientation == 180) {
            rp.setMargins(rp.leftMargin, rp.topMargin, rp.rightMargin, HEIGHT_WITH_ZOOMBAR);
        } else {
            rp.setMargins(rp.leftMargin, rp.topMargin, rp.rightMargin, HEIGHT_WITHOUT_ZOOMBAR);
        }

        rp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressViewLayout.setLayoutParams(rp);
        progressViewLayout.requestLayout();
    }
}
