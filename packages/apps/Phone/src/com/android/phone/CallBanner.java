package com.android.phone;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CallBanner extends RelativeLayout{
    
    private static final String LOG_TAG = "CallBanner";
    private static final boolean DBG = true;//(PhoneApp.DBG_LEVEL >= 2);

    public TextView mName;
    public TextView mPhoneNumber;
    public TextView mLabel;
    public TextView mCallTypeLabel;
    public TextView mSocialStatus;
    public TextView mOperatorName;
    public TextView mSimIndicator;
    public ViewGroup mMainCallBanner;
    public TextView mCallStateLabel;
    // Info about phone number GeoDescription when contact info is displayed
    public TextView mPhoneNumberGeoDescription;

    public CallBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (DBG) log("CallBanner onFinishInflate(this = " + this + ")...");

        mOperatorName = (TextView) findViewById(R.id.operatorName);
        mSimIndicator = (TextView) findViewById(R.id.simIndicator);

        mName = (TextView) findViewById(R.id.name);
        mPhoneNumber = (TextView) findViewById(R.id.phoneNumber);
        mLabel = (TextView) findViewById(R.id.label);
        mCallTypeLabel = (TextView) findViewById(R.id.callTypeLabel);
        mSocialStatus = (TextView) findViewById(R.id.socialStatus);
        mMainCallBanner = (ViewGroup) findViewById(R.id.call_banner_up_part);
        mCallStateLabel = (TextView) findViewById(R.id.callStateLabel);
        mPhoneNumberGeoDescription = (TextView) findViewById(R.id.phoneNumberGeoDescription);
    }
    
    /*
    public void setCallStateLabel(final String callStateLabel) {
        mCallStateLabel.setText(callStateLabel);
    }
    
    public void setSimIndicator(final String simIndicator) {
        mSimIndicator.setText(simIndicator);
    }
    
    public void setOperatorName(final String operatorName) {
        mOperatorName.setText(operatorName);
    }
    */
    
    private static void log(final String msg) {
        Log.d(LOG_TAG, msg);
    }
}
