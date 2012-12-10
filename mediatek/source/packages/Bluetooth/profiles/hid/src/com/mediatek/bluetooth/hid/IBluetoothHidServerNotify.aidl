package com.mediatek.bluetooth.hid;

interface IBluetoothHidServerNotify {

	void activateReq(); 	
    void deactivateReq();
    void connectReq(String BT_Addr);
    void disconnectReq(String BT_Addr);
    void unplugReq(String BT_Addr);
    void sendReportReq(String BT_Addr);
    void setReportReq(String BT_Addr);
    void getReportReq(String BT_Addr);
    void setProtocolReq(String BT_Addr);
    void getProtocolReq(String BT_Addr);
    void setIdleReq(String BT_Addr);
    void getIdleReq(String BT_Addr);
    String getStateByAddr(String BT_Addr);
    void clearService();
    void authorizeReq(String BT_Addr,boolean result);
    void finishActionReq();
}

