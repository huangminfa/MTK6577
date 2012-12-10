package com.mediatek.audioprofile;

oneway interface IAudioProfileListener {

    void onAudioProfileChanged(String profileKey);
    void onRingerModeChanged(int ringerMode);
    void onRingerVolumeChanged(int oldVolume, int newVolume, String extra);
}

