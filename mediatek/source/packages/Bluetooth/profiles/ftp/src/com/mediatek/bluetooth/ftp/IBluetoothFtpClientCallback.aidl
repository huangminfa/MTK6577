package com.mediatek.bluetooth.ftp;

import android.os.Bundle;

interface IBluetoothFtpClientCallback {

    /* Post the UI event to FTP client activity */
    void postEvent(int event, int param);

	/* Post the UI event to FTP client activity with Bundle data */
	void postEventWithData(int event, in Bundle data);
}
