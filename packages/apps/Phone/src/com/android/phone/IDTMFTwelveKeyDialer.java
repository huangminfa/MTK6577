package com.android.phone;

import android.view.KeyEvent;

public interface IDTMFTwelveKeyDialer {
    public void clearInCallScreenReference();
    public void startDialerSession();
    public void stopDialerSession();
    public boolean onDialerKeyDown(KeyEvent event);
    public boolean onDialerKeyUp(KeyEvent event);
    public boolean isOpened();
    public void openDialer(boolean animate);
    public void closeDialer(boolean animate);
    public void clearDigits();
    public void startTone(char c);
    public void startLocalToneIfNeeded(char c);
    boolean isKeyEventAcceptable (KeyEvent event);
    public void stopTone();
    public void stopLocalToneIfNeeded();
    public void handleBurstDtmfConfirmation();
}
