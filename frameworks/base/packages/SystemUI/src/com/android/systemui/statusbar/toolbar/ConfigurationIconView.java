package com.android.systemui.statusbar.toolbar;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.banyan.widget.MTKImageView;

/**
 * [SystemUI] Support "Notification toolbar".
 */
public class ConfigurationIconView extends LinearLayout {
    private static final String TAG = "ConfigurationIconView";

    private TextView mConfigName;
    private ImageView mConfigImage;
    private ImageView mOnIndicator;
    private FrameLayout mConfigIconLayout;
    private ImageView mSwitchIngGifView;

    public ConfigurationIconView(Context context) {
        this(context, null);
    }

    public ConfigurationIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mConfigName = (TextView) findViewById(R.id.config_name);
        mConfigImage = (ImageView) findViewById(R.id.config_icon);
        mOnIndicator = (ImageView) findViewById(R.id.on_indicator);
        mConfigIconLayout = (FrameLayout) findViewById(R.id.config_icon_view);
    }

    public void setClickListener(View.OnClickListener l) {
        if (l != null) {
        	mConfigImage.setOnClickListener(l);
        }
    }

    public void setTagForIcon(Object obj) {
    	mConfigImage.setTag(obj);
    }
    
    public void setConfigName(int resId) {
    	mConfigName.setText(resId);
    }
    
    public void setOnIndicator(boolean visible) {
    	if (visible) {
    		mOnIndicator.setVisibility(View.VISIBLE);
    	} else {
    		mOnIndicator.setVisibility(View.GONE);
    	}
    }
    
    public void setConfigDrawable(int resId) {
    	mConfigImage.setImageResource(resId);
    }
    
    public ImageView getConfigView() {
    	return mConfigImage;
    }
    
    public ImageView getIndicatorView() {
    	return mOnIndicator;
    }
    
    private void initSwitchingGifView() {
    	if (mSwitchIngGifView == null) {
    		ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mSwitchIngGifView = new ImageView(mContext);
            mSwitchIngGifView.setBackgroundResource(R.drawable.zzz_panel_switch_btn);
            mConfigIconLayout.addView(mSwitchIngGifView, 0);
            mSwitchIngGifView.setVisibility(GONE);
    	}
    }
    
    public void enlargeTouchRegion() {
    	Rect bounds = new Rect();
    	bounds.left = 0;
        bounds.right = this.getMeasuredWidth();
        bounds.top = 0;
        bounds.bottom = this.getMeasuredHeight();
        TouchDelegate touchDelegate = new TouchDelegate(bounds, mConfigImage);
        ConfigurationIconView.this.setTouchDelegate(touchDelegate);
    }
    
    public ImageView getSwitchingGifView() {
    	if (mSwitchIngGifView == null) {
    		initSwitchingGifView();
    	}
    	return mSwitchIngGifView;
    }
}