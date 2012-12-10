package com.android.phone;

import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import com.android.internal.telephony.Connection;

public class VTInCallScreenProxy implements IVTInCallScreen {
    private static final String LOG_TAG = "VTInCallScreenProxy";
    boolean mIsInflate = false;
    boolean mIsLocaleChanged = false;
    VTInCallScreen mVTInCallScreen;
    InCallScreen mInCallScreen;
    DTMFTwelveKeyDialerProxy mDialerProxy;

    public VTInCallScreenProxy(InCallScreen inCallScreen, 
                               DTMFTwelveKeyDialerProxy dialerProxy) {
        //mVTInCallScreen = new VTInCallScreen(context);
        mInCallScreen = inCallScreen;
        mDialerProxy = dialerProxy;
    }

    public void initVTInCallScreen() {
        if (null == mInCallScreen) {
            log("mInCallScreen is null, just return");
            return;
        }
        if (mIsInflate) {
            log("already inflate, just return");
            return;
        }
        // Inflate the ViewStub, look up and initialize the UI elements.
        ViewStub stub = (ViewStub) mInCallScreen.findViewById(R.id.vtInCallScreenStub);
        stub.inflate();
        mVTInCallScreen = (VTInCallScreen) mInCallScreen.findViewById(R.id.VTInCallCanvas);
        mVTInCallScreen.setInCallScreenInstance(mInCallScreen);
        mVTInCallScreen.setDialerProxy(mDialerProxy);
        mVTInCallScreen.initVTInCallScreen();
        if (mIsLocaleChanged) {
            mVTInCallScreen.NotifyLocaleChange();
            mIsLocaleChanged = false;
        }
        mVTInCallScreen.registerForVTPhoneStates();
        mIsInflate = true;
        VTInCallScreenFlags.getInstance().mVTIsInflate = true;
    }

    public void registerForVTPhoneStates() {
        if(null == mVTInCallScreen)
            return;
        mVTInCallScreen.registerForVTPhoneStates();
    }

    public void openVTInCallCanvas() {
        if(null == mVTInCallScreen)
            return;
        mVTInCallScreen.openVTInCallCanvas();
    }

    public void closeVTInCallCanvas() {
        if(null == mVTInCallScreen)
            return;
        mVTInCallScreen.closeVTInCallCanvas();
    }

    public void unregisterForVTPhoneStates() {
        if(null == mVTInCallScreen)
            return;
        mVTInCallScreen.unregisterForVTPhoneStates();
    }

    public void updateVTScreen(VTCallUtils.VTScreenMode mode) {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.updateVTScreen(mode);
    }

    public void setVTScreenMode(VTCallUtils.VTScreenMode mode) {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.setVTScreenMode(mode);
    }

    public VTCallUtils.VTScreenMode getVTScreenMode() {
        if(null == mVTInCallScreen || !mIsInflate )
            return VTCallUtils.VTScreenMode.VT_SCREEN_CLOSE;
        return mVTInCallScreen.getVTScreenMode();
    }

    public void internalAnswerVTCallPre() {
        initVTInCallScreen();
        if(null == mVTInCallScreen) {
            log("inflate failed");
            return;
        }
        mVTInCallScreen.internalAnswerVTCallPre();
    }

    public void resetVTFlags() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.resetVTFlags();
    }

    public void dismissVTDialogs() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.dismissVTDialogs();
    }

    public void updateVideoCallRecordState(int state) {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.updateVideoCallRecordState(state);
    }

    public void setVTVisible(final boolean bIsVisible) {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.setVTVisible(bIsVisible);
    }

    public boolean onDisconnectVT(final Connection connection, final int slotId, 
                               final boolean isForeground) {
        if(null == mVTInCallScreen || !mIsInflate )
            return false;
        return mVTInCallScreen.onDisconnectVT(connection, slotId, isForeground);
    }
    
    public void updateElapsedTime(final long elapsedTime){
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.updateElapsedTime(elapsedTime);
    }
    
    public void onDestroy() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.onDestroy();
    }
    
    public void setupMenuItems(Menu menu) {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.setupMenuItems(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
        if(null == mVTInCallScreen || !mIsInflate )
            return false;
        return mVTInCallScreen.onOptionsItemSelected(item);
    }
    
    public boolean handleOnScreenMenuItemClick(MenuItem menuItem) {
        if(null == mVTInCallScreen || !mIsInflate )
            return false;
        return mVTInCallScreen.handleOnScreenMenuItemClick(menuItem);
    }
    
    public void initCommonVTState() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.initCommonVTState();
    }
    
    public void initDialingSuccessVTState() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.initDialingSuccessVTState();
    }
    
    public void initDialingVTState() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.initDialingVTState();
    }
    
    public void refreshAudioModePopup() {
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.refreshAudioModePopup();
    }
    
    public void stopRecord() {
        log("stopRecord");
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.stopRecord();
    }
    
    public void setVTDisplayScreenMode(final boolean isFullScreenMode){
        log("setVTDisplayScreenMode");
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.setVTDisplayScreenMode(isFullScreenMode);
    }
    
    public void onStop() {
        log("onStop");
        if(null == mVTInCallScreen || !mIsInflate )
            return;
        mVTInCallScreen.onStop();
    }

    public void NotifyLocaleChange() {
        log("NotifyLocaleChange");
        if(null == mVTInCallScreen || !mIsInflate ) {
            mIsLocaleChanged = true;
            return;
        }
        mVTInCallScreen.NotifyLocaleChange();
    }
    
    public void onReceiveVTManagerStartCounter() {
        log("onReceiveVTManagerStartCounter");
        if(null == mVTInCallScreen || !mIsInflate ) {
            return;
        }
        mVTInCallScreen.onReceiveVTManagerStartCounter();
    }
    
    public void showReCallDialog(final int resid, final String number, final int slot) {
        log("showReCallDialog");
        if(null == mVTInCallScreen || !mIsInflate ) {
            return;
        }
        mVTInCallScreen.showReCallDialog(resid, number, slot);
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
