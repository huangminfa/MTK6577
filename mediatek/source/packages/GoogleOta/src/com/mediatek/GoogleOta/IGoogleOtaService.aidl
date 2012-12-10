package com.mediatek.GoogleOta;

interface IGoogleOtaService {
    void queryNewVersion();
    void queryNewVersionAbort();
    void startDlPkg();
    void resetDescriptionInfo();
    void setUpdateType(in int type);
    void cancelDlPkg();
    void pauseDlPkg();
    void runningBg();
    void setStartFlag();
}
