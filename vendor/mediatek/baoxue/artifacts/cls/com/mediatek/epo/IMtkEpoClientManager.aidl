package com.mediatek.epo;

import android.os.Bundle;
import com.mediatek.epo.IMtkEpoStatusListener;
import com.mediatek.epo.MtkEpoFileInfo;

interface IMtkEpoClientManager {
    
    //================= API version 2 ===================

    void enable();
    void disable();
    boolean getStatus();
    int getProgress();
    
    void enableAutoDownload(boolean enable);
    boolean getAutoDownloadStatus();
    
    void addStatusListener(in IMtkEpoStatusListener listener);
    void removeStatusListener(in IMtkEpoStatusListener listener);
    
    int startDownload();
    void stopDownload();
    MtkEpoFileInfo getEpoFileInfo();

    void setUpdatePeriod(long interval); //milliseconds
    long getUpdatePeriod(); //milliseconds
    void setTimeout(int timeout); //seconds for socket connect/read
    void setRetryTimes(int times);
    void setProfile(String addr, int port, String userName, String password);
    int extraCommand(String cmd, in Bundle extra);

    //================= API version 1 ===================
}
