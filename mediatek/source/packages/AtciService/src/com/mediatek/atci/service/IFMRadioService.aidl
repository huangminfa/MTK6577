package com.mediatek.atci.service;

interface IFMRadioService {
	boolean openDevice();
	boolean closeDevice();
	boolean isDeviceOpen();
	boolean powerUp(float frequency);
	boolean powerDown();
	boolean isPowerUp();
	boolean tune(float frequency);
	float seek(float frequency, boolean isUp);
	int setMute(boolean mute);
	void useEarphone(boolean use);
	boolean isEarphoneUsed();
	void initService(int iCurrentStation);
	boolean isServiceInit();
	int getFrequency();
	void resumeFMAudio();
	int switchAntenna(int antenna);
}
