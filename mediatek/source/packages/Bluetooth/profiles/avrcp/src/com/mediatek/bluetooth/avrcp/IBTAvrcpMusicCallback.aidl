package com.mediatek.bluetooth.avrcp;

interface IBTAvrcpMusicCallback{
	void notifyPlaybackStatus(byte status);
	void notifyTrackChanged(long id);
	void notifyTrackReachStart();
	void notifyTrackReachEnd();
	void notifyPlaybackPosChanged();
	void notifyAppSettingChanged();
	void notifyNowPlayingContentChanged();
	void notifyVolumehanged(byte volume);
}
