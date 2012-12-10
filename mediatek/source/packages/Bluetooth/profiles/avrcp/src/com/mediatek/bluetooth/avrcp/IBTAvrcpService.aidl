package com.mediatek.bluetooth.avrcp;

interface IBTAvrcpService{
	byte getStatus();
	boolean connect(String sAddr);
	boolean disconnect();
	boolean connectBrowse();
	boolean disconnectBrowse();	
	boolean setDebugElementAttribute();
	boolean selectPlayerId(int player_id);
	boolean debugPTSAttributes(int mode);	
}
