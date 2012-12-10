package com.mediatek.media3d;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;

public abstract class MediaDbItem implements MediaItem {

    protected ContentResolver mContentResolver;
    protected long mId;                     /* id in content resolver */
    protected Uri mUri;                     /* content URI */
    protected String mDataPath;             /* path of data stream */
    protected final String mDisplayName;

    protected MediaDbItem(ContentResolver cr, long id, Uri uri, String dataPath, String displayName) {
        mContentResolver = cr;
        mId = id;
        mUri = uri;
        mDataPath = dataPath;
        mDisplayName = displayName;
    }

    public Uri getUri() {
        return mUri;
    }

    public long getId() {
        return mId;
    }

    @Override
    public String toString() {
        return String.format(">%d:%s, uri:%s", mId, mDisplayName, mUri);
    }

    public String getName() {
        return mDisplayName;
    }

    public String getFilePath() {
        return mDataPath;
    }

    protected int calculateSubSampleSize(int kind, int maxWidth, int maxHeight, boolean sideBySide) {
        int srcWidth;
        int srcHeight;
        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
            srcWidth = sideBySide ? 512*2 : 512;
            srcHeight = 384;
        } else {
            srcWidth = sideBySide ? 96*2 : 96;
            srcHeight = 96;
        }
        return MediaUtils.calculateSubSampleSize(srcWidth, srcHeight, maxWidth, maxHeight);
    }
}