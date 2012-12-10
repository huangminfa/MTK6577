package com.mediatek.media3d;

public interface MediaSourceListener {
    public static final int MEDIA_MOUNTED_EVENT = 0;
    public static final int MEDIA_UNMOUNTED_EVENT = 1;
    public static final int MEDIA_CONTENT_CHANGED_EVENT = 2;
    public void onChanged(int event);
}
