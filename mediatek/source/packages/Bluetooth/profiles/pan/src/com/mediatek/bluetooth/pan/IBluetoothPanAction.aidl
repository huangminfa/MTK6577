package com.mediatek.bluetooth.pan;

interface IBluetoothPanAction {

    void disconnectPanDeviceAction(String BT_Addr);

    void authorizeRspAction(String BT_Addr,boolean result);

}

