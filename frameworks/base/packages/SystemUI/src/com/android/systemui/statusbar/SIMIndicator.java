package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony.SIMInfo;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.R;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "SIM indicator".
 * This widget display an SIM card indicator with name, status icon and colored background.
 */
public final class SIMIndicator extends RelativeLayout {
    private static final String TAG = "SIMIndicator";

    private ImageView mBgView;
    private ViewGroup mSimInfoLayout;
    private ImageView mStateView;
    private TextView mTextView;
    private boolean mStateViewShouldShow = false;

    private SIMInfo mCurrentSIMInfo;
    private boolean mShowing;

    private int mMinWidth, mMaxWidth;

    public SIMIndicator(Context context) {
        this(context, null);
    }

    public SIMIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMinWidth = this.getResources().getDimensionPixelSize(R.dimen.status_bar_sim_indicator_min_width);
        mMaxWidth = this.getResources().getDimensionPixelSize(R.dimen.status_bar_sim_indicator_max_width);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
        filter.addAction(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED);
        //filter.addAction(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        filter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (!isShowing()) {
                    return;
                }
                String action = intent.getAction();
                Xlog.d(TAG, "onReceive, intent action is " + action + ".");
                if (action.equals(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED)
                        || action.equals(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED)
                        //|| action.equals(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED)
                        ) {
                    long simId = intent.getLongExtra("simid", -1);
                    if (action.equals(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED) && simId == android.provider.Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
                        setSIPInfo();
                    } else {
                        setSIMInfo(simId);
                    }
                } else if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
                    int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
                    if (mCurrentSIMInfo != null) {
                        if (mCurrentSIMInfo.mSlot == slotId) {
                            int simStatus = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
                            if (simStatus != -1) {
                                Xlog.d(TAG, "updateSIMState.");
                                updateStateView(SIMHelper.getSIMStateIcon(simStatus));
                            }
                        }
                    }
                } else if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
                    SIMHelper.updateSIMInfos(context);
                    int type = intent.getIntExtra("type", -1);
                    long simId = intent.getLongExtra("simid", -1);
                    if (type == 1) {
                        // color changed
                        setSIMInfo(simId);
                    }
                }
            }
        }, filter);
    }
	/**
	 * @hide
	 */
    public void setVisibility(int v){
    	super.setVisibility(v);
    	mBgView.setVisibility(v);
    	mTextView.setVisibility(v);
    	mStateView.setVisibility(v);
    	if(!mStateViewShouldShow) {
    		mStateView.setVisibility(View.GONE);
    	}
    }
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBgView = (ImageView) findViewById(R.id.sim_indicator_bg);
        mSimInfoLayout = (ViewGroup) findViewById(R.id.sim_info);
        mStateView = (ImageView)findViewById(R.id.sim_indicator_image);
        mTextView = (TextView)findViewById(R.id.sim_indicator_text);
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void setShowing(boolean showing) {
        this.mShowing = showing;
    }

    public boolean setSIPInfo() {
        Xlog.d(TAG, "setSIPInfo.");
        mBgView.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
        updateStateView(0);
        mTextView.setText(this.getResources().getString(R.string.gemini_intenet_call));
        mCurrentSIMInfo = null;
        return true;
    }

    public boolean setSIMInfo(long simId) {
        Xlog.d(TAG, "setSIMInfo, simId is " + simId + ".");
        if (simId <= 0) {
            Xlog.d(TAG, "setSIMInfo error, the simId is <= 0.");
            return false;
        }
        SIMInfo simInfo = SIMHelper.getSIMInfo(mContext, simId);
        if (simInfo == null) {
            Xlog.d(TAG, "setSIMInfo error, the simInfo is null.");
            return false;
        }
        setSIMInfo(simInfo);
        return true;
    }

    public void setSIMInfo(SIMInfo simInfo) {
        Xlog.d(TAG, "setSIMInfo, mDisplayName is " + simInfo.mDisplayName + ".");
        mBgView.setBackgroundResource(simInfo.mSimBackgroundRes);
        updateStateView(SIMHelper.getSIMStateIcon(simInfo));
        mTextView.setText(simInfo.mDisplayName);
        mCurrentSIMInfo = simInfo;
    }

    public void clearSIMInfo() {
        Xlog.d(TAG, "clearSIMInfo.");
        mBgView.setBackgroundDrawable(null);
        mStateView.setImageDrawable(null);
        mStateView.setVisibility(View.GONE);
        mTextView.setText("");
        mCurrentSIMInfo = null;
    }

    private void updateStateView(int stateResId) {
        mStateView.setImageResource(stateResId);
        int simInfoLayoutPadding = mSimInfoLayout.getPaddingLeft() + mSimInfoLayout.getPaddingRight();
        int minWidth = mMinWidth - simInfoLayoutPadding;
        int maxWidth = mMaxWidth - simInfoLayoutPadding;
        if (stateResId > 0) {
        	mStateViewShouldShow = true;
            mStateView.setVisibility(View.VISIBLE);
            minWidth -=  mStateView.getWidth();
            maxWidth -=  mStateView.getWidth();
        } else {
        	mStateViewShouldShow = false;
            mStateView.setVisibility(View.GONE);
        }
        mTextView.setMinWidth(minWidth);
        mTextView.setMaxWidth(maxWidth);
    }
	/**
	 * @hide
	 */
    public SIMInfo getCurrentSIMInfo(){
    	return mCurrentSIMInfo;
    }
}
