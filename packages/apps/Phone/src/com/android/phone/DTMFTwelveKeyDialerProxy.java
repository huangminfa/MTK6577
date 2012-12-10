package com.android.phone;

import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewStub;

public class DTMFTwelveKeyDialerProxy implements IDTMFTwelveKeyDialer {
    private static final String LOG_TAG = "DTMFTwelveKeyDialerProxy";
    private static final boolean DBG = true;

    boolean mIsInflate = false;
    InCallScreen mInCallScreen = null;
    DTMFTwelveKeyDialer mDialer = null;
	
    private boolean mIsStartDialerSession = false;

    public DTMFTwelveKeyDialerProxy(InCallScreen parent) {
        mInCallScreen = parent;
        //mDialer = new DTMFTwelveKeyDialer(parent);
    }

    public void clearInCallScreenReference() {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.clearInCallScreenReference();
    }

    public void startDialerSession() {
        if (DBG) log("mDialer: " + mDialer + "mIsInflate: " + mIsInflate);
        if(null == mDialer || !mIsInflate) {
	        mIsStartDialerSession = true;
            return;
        }
        mDialer.startDialerSession();
    }

    public void stopDialerSession() {
        if(null == mDialer || !mIsInflate) {
	        mIsStartDialerSession = false;
            return;
        }
        mDialer.stopDialerSession();
    }

    public boolean onDialerKeyDown(KeyEvent event) {
        if(null == mDialer || !mIsInflate)
            return false;
        return mDialer.onDialerKeyDown(event);
    }

    public boolean onDialerKeyUp(KeyEvent event) {
        if(null == mDialer || !mIsInflate)
            return false;
        return mDialer.onDialerKeyUp(event);
    }

    public boolean isOpened() {
        if(null == mDialer || !mIsInflate)
            return false;
        return mDialer.isOpened();
    }

    public void openDialer(boolean animate) {
        if (!mIsInflate || null == mDialer) {
            ViewStub stub = (ViewStub)mInCallScreen.findViewById(R.id.dtmf_twelve_key_dialer_stub);
            stub.inflate();
            DTMFTwelveKeyDialerView dialerView 
                = (DTMFTwelveKeyDialerView) mInCallScreen.findViewById(R.id.dtmf_twelve_key_dialer_view);
            if (DBG) log("- Found dialerView: " + dialerView);
            // Sanity-check that (regardless of the device) at least the
            // dialer view is present:
            if (dialerView == null) {
                Log.e(LOG_TAG, "onCreate: couldn't find dialerView", new IllegalStateException());
                return;
            }
            // Create the DTMFTwelveKeyDialer instance.
            mDialer = new DTMFTwelveKeyDialer(mInCallScreen, dialerView);
            mIsInflate = true;
	        if(mIsStartDialerSession)startDialerSession();
        }
        mDialer.openDialer(animate);
    }

    public void closeDialer(boolean animate) {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.closeDialer(animate);
    }

    public void clearDigits() {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.clearDigits();
    }

    public void startTone(char c) {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.startTone(c);
    }

    public void startLocalToneIfNeeded(char c) {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.startLocalToneIfNeeded(c);
    }

    public boolean isKeyEventAcceptable (KeyEvent event) {
        if(null == mDialer || !mIsInflate)
            return false;
        return mDialer.isKeyEventAcceptable(event);
    }

    public void stopTone() {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.stopTone();
    }

    public void stopLocalToneIfNeeded() {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.stopLocalToneIfNeeded();
    }

    public void handleBurstDtmfConfirmation() {
        if(null == mDialer || !mIsInflate)
            return;
        mDialer.handleBurstDtmfConfirmation();
    }

    /**
     * static logging method
     */
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
