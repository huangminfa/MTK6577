/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.camera;

import com.android.camera.ui.PopupManager;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.content.res.Configuration;
import com.mediatek.featureoption.FeatureOption;
import android.content.res.Resources;
import android.content.res.Configuration;
/**
 * A widget that includes three mode selections {@code RotateImageView}'s and
 * a current mode indicator.
 */
public class ModePicker extends RelativeLayout implements View.OnClickListener,
    PopupManager.OnOtherPopupShowedListener, Rotatable {
    public static final int MODE_CAMERA = 0;
    public static final int MODE_VIDEO = 1;
    public static final int MODE_PANORAMA = 2;
    public static final int MODE_MAV = 3;

    private static final String TAG = "ModePicker";
    // Total mode number
    private static final int MODE_NUM = 4;
    private int CURRENT_MODE_NUM = 3;

    /** A callback to be called when the user wants to switch activity. */
    public interface OnModeChangeListener {
        // Returns true if the listener agrees that the mode can be changed.
        public boolean onModeChanged(int newMode);
    }

    private final int DISABLED_COLOR;
    private final int CURRENT_MODE_BACKGROUND;

    private OnModeChangeListener mListener;
    private View mModeSelectionFrame;
    private RotateImageView mModeSelectionIcon[];
    private View mCurrentModeFrame;
    private RotateImageView mCurrentModeIcon[];
    private View mCurrentModeBar;
    private boolean mSelectionEnabled;


    private int mCurrentMode = 0;
    private Animation mFadeIn, mFadeOut;

    // mediaTek add start
    private RelativeLayout mChooseModeView;
    private RelativeLayout mChooseModeLessView;
    private int mSmallSizePixel;
    private int SUPPORTED_MODE_NUM = 4;
    private boolean sIsScreenLarge = false;
    private RotateImageView mAllModeIcon[];
    // mediaTek add end
    private Resources res;
    public ModePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        DISABLED_COLOR = context.getResources().getColor(R.color.icon_disabled_color);
        CURRENT_MODE_BACKGROUND = R.drawable.btn_mode_background;
        mFadeIn = AnimationUtils.loadAnimation(
                context, R.anim.mode_selection_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(
                context, R.anim.mode_selection_fade_out);
        mFadeOut.setAnimationListener(mAnimationListener);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
        res = context.getResources();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        int screenSize = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        boolean sIsScreenLarge = ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) || (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE));
        if ( false == sIsScreenLarge ){
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                int bgColor = res.getThemeMainColor();
                if (bgColor != 0) {
                    getRootView().findViewById(R.id.ModePickerSeperatorBG).setBackgroundColor(bgColor);
                }
            }
        }

        mModeSelectionFrame = findViewById(R.id.mode_selection);
    	mModeSelectionFrame.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				//to prevent the touch action from being dispatched to the icon(shutter icon) below mode picker.
				return true;
			}
        });

        mModeSelectionIcon = new RotateImageView[MODE_NUM];
        mModeSelectionIcon[MODE_PANORAMA] =
                (RotateImageView) findViewById(R.id.mode_panorama);
        mModeSelectionIcon[MODE_VIDEO] =
                (RotateImageView) findViewById(R.id.mode_video);
        mModeSelectionIcon[MODE_CAMERA] =
                (RotateImageView) findViewById(R.id.mode_camera);
        mModeSelectionIcon[MODE_MAV] =
                (RotateImageView) findViewById(R.id.mode_mav);
        mModeSelectionIcon[MODE_PANORAMA].setVisibility(View.VISIBLE);
        mModeSelectionIcon[MODE_VIDEO].setVisibility(View.VISIBLE);
        mModeSelectionIcon[MODE_CAMERA].setVisibility(View.VISIBLE);
        mModeSelectionIcon[MODE_MAV].setVisibility(View.VISIBLE);

        // The current mode frame is for Phone UI only.
        if (findViewById(R.id.current_mode) != null) {
            mCurrentModeIcon = new RotateImageView[CURRENT_MODE_NUM];
            mCurrentModeIcon[0] = (RotateImageView) findViewById(R.id.mode_0);
            mCurrentModeIcon[1] = (RotateImageView) findViewById(R.id.mode_1);
            mCurrentModeIcon[2] = (RotateImageView) findViewById(R.id.mode_2);
            mCurrentModeFrame = findViewById(R.id.current_mode);
            mAllModeIcon = new RotateImageView[5];
            mAllModeIcon[0] = (RotateImageView) findViewById(R.id.mode_0);
            mAllModeIcon[1] = (RotateImageView) findViewById(R.id.mode_1);
            mAllModeIcon[2] = (RotateImageView) findViewById(R.id.mode_2);
            mAllModeIcon[3] = (RotateImageView) findViewById(R.id.mode_less_0);
            mAllModeIcon[4] = (RotateImageView) findViewById(R.id.mode_less_1);
        } else {
            // current_mode_bar is only for tablet.
            mCurrentModeBar = findViewById(R.id.current_mode_bar);
            if (FeatureOption.MTK_THEMEMANAGER_APP)
            {
                int bgColor = res.getThemeMainColor();
                if (bgColor != 0)
                {
                    mCurrentModeBar.setBackgroundColor(bgColor);
                }
            }
            enableModeSelection(true);
        }
        mChooseModeView = (RelativeLayout) findViewById(R.id.choose_mode);
        mChooseModeLessView = (RelativeLayout) findViewById(R.id.choose_mode_less);
        registerOnClickListener();
    }

    private void registerOnClickListener() {
        if (mCurrentModeFrame != null) {
            mCurrentModeFrame.setOnClickListener(this);
        }
        for (int i = 0; i < MODE_NUM; ++i) {
            mModeSelectionIcon[i].setOnClickListener(this);
        }
    }

    @Override
    public void onOtherPopupShowed() {
        if (mSelectionEnabled) enableModeSelection(false);
    }

    private AnimationListener mAnimationListener = new AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            mCurrentModeFrame.setVisibility(View.VISIBLE);
            mModeSelectionFrame.setVisibility(View.GONE);
			//delays some time to let the animation to be finished completely.
			getHandler().postDelayed(new Runnable(){
				public void run() {
					changeToSelectedMode();
				}
			},20);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    };

    private void enableModeSelection(boolean enabled) {
        if (mCurrentModeFrame != null) {
            mSelectionEnabled = enabled;
            // Animation Effect is applied on Phone UI only.
            mModeSelectionFrame.startAnimation(enabled ? mFadeIn : mFadeOut);
            if (enabled) {
                mModeSelectionFrame.setVisibility(View.VISIBLE);
                mCurrentModeFrame.setVisibility(View.GONE);
            }
        }
        updateModeState();
    }

    private void changeToSelectedMode() {
        if (mListener != null) {
            if (mListener.onModeChanged(mCurrentMode)) {
                Log.e(TAG, "failed:onModeChanged:" + mCurrentMode);
            }
        }
    }

    public void onClick(View view) {
        if (view == mCurrentModeFrame) {
            PopupManager.getInstance(getContext()).notifyShowPopup(this);
            enableModeSelection(true);
        } else {
            // Set the selected mode as the current one and switch to it.
            for (int i = 0; i < MODE_NUM; ++i) {
                if (view == mModeSelectionIcon[i] && (mCurrentMode != i)) {
                    setCurrentMode(i);
                    break;
                }
            }
            if (mCurrentModeBar == null) {
                enableModeSelection(false);
            } else {
                changeToSelectedMode();
            }
        }
    }

    public void setOnModeChangeListener(OnModeChangeListener listener) {
        mListener = listener;
    }

    public void setCurrentMode(int mode) {
        mCurrentMode = mode;
        updateModeState();
    }

    public boolean onModeChanged(int mode) {
        setCurrentMode(mode);
        return true;
    }

    public void setOrientation(int orientation) {
        for (int i = 0; i < MODE_NUM; ++i) {
            mModeSelectionIcon[i].setOrientation(orientation);
        }
        // we add here to avoid unused animation.
        // because the mCurrentModeIcon may contain old orientation.
        for (int i = 0; i <5; ++i) {
        	if (mCurrentModeFrame != null && mAllModeIcon[i] != null) {
        	    mAllModeIcon[i].setOrientation(orientation);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        // Enable or disable the frames.
        if (mCurrentModeFrame != null) mCurrentModeFrame.setEnabled(enabled);
        mModeSelectionFrame.setEnabled(enabled);

        // Enable or disable the icons.
        for (int i = 0; i < MODE_NUM; ++i) {
            mModeSelectionIcon[i].setEnabled(enabled);
        }
        for (int i = 0; i < CURRENT_MODE_NUM; ++i) {
        	if (mCurrentModeFrame != null) {
        		mCurrentModeIcon[i].setEnabled(enabled);
        	}
        }
        if (enabled) updateModeState();
    }

    private void highlightView(ImageView view, boolean enabled) {
        if (enabled) {
            view.clearColorFilter();
        } else {
            view.setColorFilter(DISABLED_COLOR, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void updateModeState() {
        // Grey-out the unselected icons for Phone UI.
        if (mCurrentModeFrame != null) {
            for (int i = 0; i < MODE_NUM; ++i) {
                highlightView(mModeSelectionIcon[i], (i == mCurrentMode));
            }
        }

        // Update the current mode icons on the Phone UI. The selected mode
        // should be in the center of the current mode icon bar.
        if (mCurrentModeFrame != null) {
            if (SUPPORTED_MODE_NUM < 3) {
                for (int i = 0, j = 0; i < SUPPORTED_MODE_NUM; i++) {
                    while (j < MODE_NUM && mModeSelectionIcon[j].getVisibility() == View.GONE) j++;
                    if (MODE_NUM <= j) return;
                    if (j == mCurrentMode) {
                        mCurrentModeIcon[i].getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        mCurrentModeIcon[i].getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        if (mNeedCollapse) {
                            mCurrentModeIcon[i].setVisibility(View.GONE);
                        }
                    } else {
                        mCurrentModeIcon[i].getLayoutParams().width = mSmallSizePixel;
                        mCurrentModeIcon[i].getLayoutParams().height = mSmallSizePixel;
                    }
                    Log.v("T", "i=" + i + " j="+ j + " mCurrentMode="+mCurrentMode);
                    mCurrentModeIcon[i].setImageDrawable(
                            mModeSelectionIcon[j++].getDrawable());
                    mCurrentModeIcon[i].requestLayout();
                }
                return;
            }

            for (int i = 0, j = 0; i < CURRENT_MODE_NUM; i++) {
                int target;
                if (i == 1) {
                    // The second icon is always the selected mode.
                    target = mCurrentMode;
                } else {
                    // Set the icons in order of camera, video and panorama.
                    while (j < SUPPORTED_MODE_NUM && (
                    		j == mCurrentMode || mModeSelectionIcon[j].getVisibility() == View.GONE)) j++;
                    target = j++;
                    if (mNeedCollapse) {
                        mCurrentModeIcon[i].setVisibility(View.GONE);
                    }
                }
                if (target < SUPPORTED_MODE_NUM) {
	                mCurrentModeIcon[i].setImageDrawable(
	                        mModeSelectionIcon[target].getDrawable());
                } else {
                	Log.v(TAG, "updateModeState target=" + target);
                }
            }
        }
    }

    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Layout the current mode indicator bar.
        if (mCurrentModeBar != null) {
            int viewWidth = mModeSelectionIcon[MODE_CAMERA].getWidth();
            int iconWidth = ((ImageView) mModeSelectionIcon[MODE_CAMERA])
                    .getDrawable().getIntrinsicWidth();
            int padding = (viewWidth - iconWidth) / 2;
            int l = mModeSelectionFrame.getLeft() + mCurrentMode * viewWidth;
            mCurrentModeBar.layout((l + padding),
                    (bottom - top - mCurrentModeBar.getHeight()),
                    (l + padding + iconWidth),
                    (bottom - top));
        }
        if (!mFirstCalculate) {
        	mNeedCollapse = computeCollapse();
        	if(mNeedCollapse){
        		updateModeState();
        	}
        }
    }
    private boolean computeCollapse() {
    	Log.v(TAG, "computeCollapse");
    	if (mCurrentModeIcon == null) return false;
    	mFirstCalculate = true;
    	int sum = 0;
    	for (int i = 0; i < CURRENT_MODE_NUM; i++) {
    		sum += mCurrentModeIcon[i].getHeight();
    	}
        int minHeight = 0;
    	if ((!Util.isMAVSupport()) && (!Util.isPANORAMASupport())){
    	    minHeight = sum - (mCurrentModeIcon[0].getHeight()* 3)/8;
    	}else {
    	    minHeight = sum - (mCurrentModeIcon[0].getHeight()* 3)/4;
    	}
    	int availableHeight = getHeight() - mCurrentModeIcon[0].getTop()* 2;
        if (minHeight != 0 && (minHeight > availableHeight)) {
    		return true;
    	}
    	return false;
    }
    private boolean mNeedCollapse = false;
    private boolean mFirstCalculate = false;

    public void setModeSupport() {
        setModeVisibility(Util.isMAVSupport(), MODE_MAV);
        setModeVisibility(Util.isPANORAMASupport(), MODE_PANORAMA);
        setModeVisibility(Util.isVideoCameraSupport(), MODE_VIDEO);

        if (SUPPORTED_MODE_NUM < 3) {
			initializeLessMode();
		} else {
		    initializeMoreMode();
		}
        updateModeState();
    }

    private void setModeVisibility(boolean support, int mode) {
        if (support && mModeSelectionIcon[mode].getVisibility() != View.VISIBLE) {
            mModeSelectionIcon[mode].setVisibility(View.VISIBLE);
            SUPPORTED_MODE_NUM ++;
        } else if (!support && mModeSelectionIcon[mode].getVisibility() != View.GONE) {
            mModeSelectionIcon[mode].setVisibility(View.GONE);
            SUPPORTED_MODE_NUM --;
        }
    }

    private void initializeMoreMode() {
        if (mCurrentModeFrame == null || View.VISIBLE == mChooseModeView.getVisibility()) return; 
        CURRENT_MODE_NUM = 3;
        mCurrentModeIcon = new RotateImageView[CURRENT_MODE_NUM];
        mCurrentModeIcon[0] = (RotateImageView) findViewById(R.id.mode_0);
        mCurrentModeIcon[1] = (RotateImageView) findViewById(R.id.mode_1);
        mCurrentModeIcon[2] = (RotateImageView) findViewById(R.id.mode_2);

        if (mCurrentModeFrame != null) {
            mChooseModeView.setVisibility(View.VISIBLE);
            mChooseModeLessView.setVisibility(View.GONE);
        }
    }

    private void initializeLessMode() {
    	CURRENT_MODE_NUM = 2;
    	int screenSize = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
    	sIsScreenLarge = ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) || (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE));
    	if (!sIsScreenLarge){
	        mCurrentModeIcon = new RotateImageView[CURRENT_MODE_NUM];
	        mCurrentModeIcon[0] = (RotateImageView) findViewById(R.id.mode_less_0);
	        mCurrentModeIcon[1] = (RotateImageView) findViewById(R.id.mode_less_1);
	        if (0 == mSmallSizePixel) {
	            mSmallSizePixel = mCurrentModeIcon[0].getLayoutParams().width;
	        }

	        if (mCurrentModeFrame != null) {
	            mChooseModeView.setVisibility(View.GONE);
	            mChooseModeLessView.setVisibility(View.VISIBLE);
	        }
    	}
    }

}
