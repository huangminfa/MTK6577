package com.mediatek.bluetooth.avrcp;
import com.mediatek.bluetooth.avrcp.IBTAvrcpMusicCallback;

interface IBTAvrcpMusic{
    void registerCallback( IBTAvrcpMusicCallback callback);
    void unregisterCallback( IBTAvrcpMusicCallback callback);
	boolean regNotificationEvent(byte eventId, int interval);
	boolean setPlayerApplicationSettingValue(byte attrId, byte value);
	
	byte[] getCapabilities();
	void play();
	void stop();
	void pause();
	void resume();
	void prev();
	void next();
	void prevGroup();
	void nextGroup();
	
	boolean setEqualizeMode(int equalizeMode);
	int getEqualizeMode();
	boolean setShuffleMode(int shufflemode);
	int getShuffleMode();
	boolean setRepeatMode(int repeatmode);
	int getRepeatMode();
	boolean setScanMode(int scanMode);
	int getScanMode();	

	boolean informDisplayableCharacterSet(int charset);
	boolean informBatteryStatusOfCT();
	
	byte getPlayStatus();
	long position();
	long duration();

	long getAudioId();
	String getTrackName();
	String getAlbumName();
	long getAlbumId();
	String getArtistName();
	
	void enqueue(in long [] list, int action);
	long [] getNowPlaying();
	String getNowPlayingItemName(in long id);

	void open(in long [] list, int position);
	int getQueuePosition();
	void setQueuePosition(int index);
		
}
