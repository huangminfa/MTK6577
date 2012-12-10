package com.mediatek.media3d;

public interface MediaItemSet {
    int getItemCount();
    MediaItem getItem(int index);
    void close();
}
