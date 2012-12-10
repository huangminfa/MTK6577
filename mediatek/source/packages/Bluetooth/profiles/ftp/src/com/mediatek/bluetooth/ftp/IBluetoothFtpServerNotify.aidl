package com.mediatek.bluetooth.ftp;

interface IBluetoothFtpServerNotify {

	void authResult(boolean res);
	void disconnect();
	void updateNotify(int notify);

}
