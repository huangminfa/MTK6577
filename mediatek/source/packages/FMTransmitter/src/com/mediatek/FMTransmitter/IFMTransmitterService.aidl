package com.mediatek.FMTransmitter;


interface IFMTransmitterService
{
    //Tx
    boolean isFMTxSupport();
    boolean openTxDevice();
    boolean closeTxDevice();
    boolean isTxDeviceOpen();
    boolean powerUpTx(float frequency);
    boolean powerDownTx();
    boolean repowerTx(float frequency);
    boolean isTxPowerUp();
    boolean isSearching();
    boolean turnToFrequency(float frequency);
    float[] searchChannelsForTx(float frequency, int direction, int number);
    //RDS
    boolean isRDSTxSupport();
    boolean isRDSOn();
    boolean setRDSTxEnabled(boolean state);
    boolean setRDSText(in int pi, in char[] ps, in int[] rdsText, int rdsCnt);
    boolean setAudioPathToFMTx();
    boolean setAudioPathOutofFMTx();
    boolean initService(float iCurrentFrequency);
    boolean isServiceInit();
    float getCurFrequency();
    void sendPowerdownFMRxMsg();
    boolean isEarphonePluged();
    
}
