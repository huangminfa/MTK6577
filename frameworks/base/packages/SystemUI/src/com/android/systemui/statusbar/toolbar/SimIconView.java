package com.android.systemui.statusbar.toolbar;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "Notification toolbar".
 */
public class SimIconView extends LinearLayout {
    private static final String TAG = "SimIconView";
    
    private Drawable mSelectedIcon;

    private TextView mSimName;
    private TextView mSimOpName;
    private TextView mSimType;
    private ImageView mSimIcon;
    private ImageView mSimStateView;
    private RelativeLayout mSimBgView;
    private boolean mSelected;
    private int mSlotId = -1;

    public SimIconView(Context context) {
        this(context, null);
    }

    public SimIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSelected = false;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mSimIcon = (ImageView) findViewById(R.id.sim_icon);
        mSimName = (TextView) findViewById(R.id.sim_name);
        mSimType = (TextView) findViewById(R.id.sim_type);
        mSimStateView = (ImageView) findViewById(R.id.sim_state);
        mSimOpName = (TextView) findViewById(R.id.sim_op_name);
        mSimBgView = (RelativeLayout) findViewById(R.id.sim_bg_view);
        
        mSelectedIcon = getContext().getResources().getDrawable(R.drawable.zzz_sim_card_enable_background);
    }

    public void updateSimIcon(SIMInfo info) {
        Xlog.i(TAG, "updateSimIcon called, simName is " + info.mDisplayName + ", simNumber is " + info.mNumber);
        if (info.mNumber != null && !info.mNumber.isEmpty()) {
            mSimName.setText(getFormatedNumber(info.mNumber, info.mDispalyNumberFormat));
        } else {
            mSimName.setText("");
        }
        mSimIcon.setBackgroundResource(info.mSimBackgroundRes);
        mSimOpName.setText(info.mDisplayName);
        int slotId = info.mSlot;
        int simState = TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(slotId);
        Xlog.i(TAG, "updateSimIcon called, simState is " + simState + ", slotId is " + slotId);
        updateSimState(simState);
    }
    
    public void updateSimState(int simState) {
    	int resId = SIMHelper.getSIMStateIcon(simState);
	if(resId > -1)    	
	    	mSimStateView.setImageResource(resId);

    	
    }
    
    public void set3GIconVisibility(boolean visible) {
    	mSimType.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setClickListener(View.OnClickListener l) {
        if (l != null) {
        	mSimIcon.setOnClickListener(l);
        }
    }

    public void setTagForSimIcon(Object obj) {
    	mSimIcon.setTag(obj);
    }
    
    public void setOpName(int resId) {
    	mSimOpName.setText(resId);
    }
    
    public TextView getOpName() {
    	return mSimOpName;
    }
    
    public ImageView getSimIcon() {
    	return mSimIcon;
    }

    private String getFormatedNumber(String number, int format) {
        switch (format) {
        case (SimInfo.DISPLAY_NUMBER_FIRST):
            if (number.length() <= 4) {
                return number;
            }
            return number.substring(0, 4);
        case (SimInfo.DISPLAY_NUMBER_LAST):
            if (number.length() <= 4) {
                return number;
            }
            return number.substring(number.length() - 4, number.length());
        case (SimInfo.DISPALY_NUMBER_NONE):
            return "";
        default:
            return "";
        }
    }
    
    public void setSimIconViewResource(int resId) {
    	mSimIcon.setBackgroundResource(resId);
    }

    public boolean isSelected() {
        return mSelected;
    }
    
    public void enlargeTouchRegion() {
    	Rect bounds = new Rect();
    	bounds.left = 0;
        bounds.right = this.getMeasuredWidth();
        bounds.top = 0;
        bounds.bottom = this.getMeasuredHeight();
        TouchDelegate touchDelegate = new TouchDelegate(bounds, mSimIcon);
        SimIconView.this.setTouchDelegate(touchDelegate);
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
        if (selected) {
        	mSimBgView.setBackgroundDrawable(mSelectedIcon);
        } else {
        	mSimBgView.setBackgroundDrawable(null);
        }
    }
    /**
     * @hide
     */
    public void setSlotId(int id){
    	mSlotId = id;
    }
    /**
     * @hide
     */
    public int getSlotId(){
    	return mSlotId;
    }
}
