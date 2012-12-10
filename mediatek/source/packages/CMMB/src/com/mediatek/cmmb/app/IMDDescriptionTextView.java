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

package com.mediatek.cmmb.app;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class IMDDescriptionTextView extends TextView {
	private static final String TAG = "CMMB::IMDDescriptionTextView";	
	private int mOriginWidth = -1;
	private static final int AUTO_HIDE_DURATION = 5000;//required by CMCC.
	private boolean mEffectSet;
	private boolean mCanceled;	
	private DisplayCompleteListener mDisplayCompleteListener;
	private Handler mHandler = new Handler();
	public IMDDescriptionTextView(Context context) {
		super(context);		
	}
	
    public IMDDescriptionTextView(Context context,
	            AttributeSet attrs) {
	    	super(context, attrs);
	}	

	@Override	
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		Log.d(TAG, "onLayout");	
		if (!mEffectSet) {	
			mEffectSet = true;
			mCanceled = false;
			
	    	//We will initialize mOriginWidth with its real width here.
	    	//note that the output of the formula we use to compute will change after the invoking of setAsWideAsText,
	    	//so we should keep mOriginWidth unchanged.
	    	if (mOriginWidth == -1) {
				mOriginWidth = (getRight() - getLeft() - getCompoundPaddingLeft() - getCompoundPaddingRight());
	    	}	
			
			if (canMarquee()) {				
				int movement = (int)getLayout().getLineWidth(0) - mOriginWidth;
				Animation a = new TranslateAnimation(0,-movement,0,0); 
				a.setDuration(movement * 40);  
				a.setStartOffset(1000);
				//It seems that REVERSE is smoother than RESTART.
				a.setRepeatMode(Animation.RESTART); 
				a.setRepeatCount(1);
				a.setInterpolator(new LinearInterpolator());
				a.setAnimationListener(new Animation.AnimationListener() {
		            public void onAnimationStart(Animation animation) {
		            	Log.d(TAG, "onAnimationStart");
		            	setAsWideAsText();
		            }
		 
		            public void onAnimationEnd(Animation animation) {
		            	Log.d(TAG, "onAnimationEnd");
		            	//call it to be invalidate and text back to the beginning.
		            	clearAnimation();
		            	mEffectSet = false;
		            	if (mDisplayCompleteListener != null && !mCanceled) {
		            		mDisplayCompleteListener.onDisplayComplete();
		            	}
		            }
		 
		            public void onAnimationRepeat(Animation animation) {
		            }
		        });			
				setAnimation(a);
			} else {
				mHandler.postDelayed(new Runnable(){
					public void run() {
		            	mEffectSet = false;
		            	if (mDisplayCompleteListener != null && !mCanceled) {
		            		mDisplayCompleteListener.onDisplayComplete();
		            	}			
					}				
				}, AUTO_HIDE_DURATION);
			}
		}		
	}	
	
    private void setAsWideAsText() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        int line_width = (int)getLayout().getLineWidth(0);
        Log.d(TAG, "setAsWideAsText line_width = "+line_width);	
        lp.width = line_width;
        setLayoutParams(lp);
    }

	private boolean canMarquee() {
    	Log.d(TAG, "canMarquee width = "+mOriginWidth+", lineWidth = "+getLayout().getLineWidth(0));	
        return mOriginWidth > 0 && getLayout().getLineWidth(0) > mOriginWidth;
    }	

	public void cancelEffect() {
		if (mEffectSet) {
			mEffectSet = false;
			mCanceled = true;
			clearAnimation();
			mHandler.removeCallbacksAndMessages(null);
		}
	}
    
	public interface DisplayCompleteListener {
		public void onDisplayComplete();
	}	
	
	public void setDisplayCompleteListener(DisplayCompleteListener l) {
		mDisplayCompleteListener = l;
	}	    
}
