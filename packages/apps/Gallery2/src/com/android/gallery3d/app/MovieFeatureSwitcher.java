package com.android.gallery3d.app;

import android.net.Uri;

public interface MovieFeatureSwitcher {
    boolean isEnabledStereoVideo();
    boolean isEnabledStereoAudio();
    boolean isEnabledLoop();
    boolean isEnabledVideoList();
    boolean isEnabledBookmark();
    boolean isEnabledServerDetail();
    boolean isEnabledInputUri();
    boolean isEnabledStreamingSettings();
    boolean isEnabledStop();
    boolean isEnabledFullscreenNotification(Uri originalUri, MovieItem movieInfo);
}