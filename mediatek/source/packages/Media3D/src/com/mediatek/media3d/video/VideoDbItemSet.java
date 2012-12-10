package com.mediatek.media3d.video;

import android.provider.MediaStore;
import com.mediatek.media3d.MediaDbItem;
import com.mediatek.media3d.MediaDbItemSet;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;

public class VideoDbItemSet extends MediaDbItemSet {
    // the URI of video
    private static final Uri CONTENT_URI = Media.EXTERNAL_CONTENT_URI;
    private static final String [] PROJECTION = new String [] {
        Media._ID,
        Media.DATA,
        Media.DURATION,
        Media.MINI_THUMB_MAGIC,
        Media.DISPLAY_NAME
    };
    private static final int INDEX_ID = 0;
    private static final int INDEX_DATA = 1;
    private static final int INDEX_DURATION = 2;
    private static final int INDEX_MIMI_THUMB_MAGIC = 3;
    private static final int INDEX_DISPLAY_NAME = 4;
    private static final int INDEX_STEREO_TYPE = 5;


    VideoDbItemSet(ContentResolver cr, String bucketId) {
        super(cr, CONTENT_URI, bucketId);
    }

    public MediaDbItem getItemAtCursor(Cursor c) {
        Long id = c.getLong(INDEX_ID);
        String dataPath = c.getString(INDEX_DATA);
        Long duration = c.getLong(INDEX_DURATION);
        String displayName = c.getString(INDEX_DISPLAY_NAME);
        MediaDbItem mi  = (MediaDbItem) new VideoDbItem(mContentResolver, id, getCurrentUri(id),
                          dataPath, displayName, duration, 0);
        return mi;
    }

    @Override
    protected String selection() {
        if (mBucketId == null) {
            return null;
        } else {
            return Media.BUCKET_ID + " = '" + mBucketId + "'";
        }
    }

    // private static final String SORT_ASCENDING = " ASC";
    private static final String SORT_DESCENDING = " DESC";

    // TODO: sort by date.
    @Override
    protected String sortOrder() {
        return Media.DATE_TAKEN + SORT_DESCENDING;
    }

    @Override
    protected String [] projection() {
        return PROJECTION;
    }


    public boolean isEmpty() {
        return (getItemCount() == 0);
    }
}