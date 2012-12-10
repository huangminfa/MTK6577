package com.android.phone;

import android.view.Menu;
import android.view.MenuItem;

import com.android.internal.telephony.Connection;

public interface IVTInCallScreen {
    void updateVTScreen(VTCallUtils.VTScreenMode mode);
    void setVTScreenMode(VTCallUtils.VTScreenMode mode);
    VTCallUtils.VTScreenMode getVTScreenMode();
    void internalAnswerVTCallPre();
    void resetVTFlags();
    void dismissVTDialogs();
    void updateVideoCallRecordState(final int state);
    void setVTVisible(final boolean bIsVisible);
    boolean onDisconnectVT(final Connection connection, final int slotId, final boolean isForeground);
    void updateElapsedTime(final long elapsedTime);
    void onDestroy();
    void setupMenuItems(Menu menu);
    boolean onOptionsItemSelected(MenuItem item);
    boolean handleOnScreenMenuItemClick(MenuItem menuItem);
    void initCommonVTState();
    void initDialingVTState();
    void initDialingSuccessVTState();
    void refreshAudioModePopup();
    void stopRecord();
    void setVTDisplayScreenMode(final boolean isFullScreenMode);
    void onStop();
    void NotifyLocaleChange();
    void onReceiveVTManagerStartCounter();
}
