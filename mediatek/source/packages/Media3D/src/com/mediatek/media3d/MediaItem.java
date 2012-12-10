package com.mediatek.media3d;

import android.graphics.Bitmap;
import android.net.Uri;

public interface MediaItem {
    Bitmap getThumbnail(int width, int height);
    void cancelThumbnailRequest();
    Uri getUri();
    String getFilePath();
    long getDuration();
}
