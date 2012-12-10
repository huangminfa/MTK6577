package com.mediatek.media3d.photo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore.Images;
import com.mediatek.media3d.MediaDbItem;
import com.mediatek.media3d.MediaDbItemSet;

public class ImageDbItemSet extends MediaDbItemSet {
    private static final String TAG = "ImageDbItemSet";

    ImageDbItemSet(ContentResolver cr, String bucketId) {
        super(cr, Images.Media.EXTERNAL_CONTENT_URI, bucketId);
    }

    @Override
    protected String selection() {
        return String.format("%s in (%s)", Images.ImageColumns.BUCKET_ID, mBucketId);
    }

    private static final boolean IS_DESCEND_SORT = true;
    private static final String DESCEND_SORT = " DESC";
    private static final String ASCEND_SORT = " ASC";

    private static final String DEFAULT_IMAGE_SORT_ORDER = Images.ImageColumns.DATE_TAKEN + (IS_DESCEND_SORT ? DESCEND_SORT : ASCEND_SORT);

    @Override
    protected String sortOrder() {
        return DEFAULT_IMAGE_SORT_ORDER;
    }

    private static final String[] PROJECTION_IMAGES = new String[] {
        Images.ImageColumns._ID, Images.ImageColumns.TITLE,
        Images.ImageColumns.MIME_TYPE, Images.ImageColumns.LATITUDE, Images.ImageColumns.LONGITUDE,
        Images.ImageColumns.DATE_TAKEN, Images.ImageColumns.DATE_ADDED, Images.ImageColumns.DATE_MODIFIED,
        Images.ImageColumns.DATA, Images.ImageColumns.ORIENTATION, Images.ImageColumns.BUCKET_ID};

    @Override
    protected String[] projection() {
        return PROJECTION_IMAGES;
    }

    private static final String BASE_CONTENT_STRING_IMAGES = (Images.Media.EXTERNAL_CONTENT_URI).toString() + "/";

    @Override
    protected MediaDbItem getItemAtCursor(Cursor c) {
        long id = c.getLong(ImageDbItem.MEDIA_ID_INDEX);
        String dataPath = c.getString(ImageDbItem.MEDIA_DATA_INDEX);
        String displayName = c.getString(ImageDbItem.MEDIA_CAPTION_INDEX);

        final ImageDbItem item = new ImageDbItem(mContentResolver, id, getCurrentUri(id), dataPath, displayName);
        populateMediaItemSetFromCursor(item, c, BASE_CONTENT_STRING_IMAGES);

        return item;
    }

    private static final void populateMediaItemSetFromCursor(final ImageDbItem item, final Cursor cursor, final String baseUri) {
        item.mMimeType = cursor.getString(ImageDbItem.MEDIA_MIME_TYPE_INDEX);
        item.mLatitude = cursor.getDouble(ImageDbItem.MEDIA_LATITUDE_INDEX);
        item.mLongitude = cursor.getDouble(ImageDbItem.MEDIA_LONGITUDE_INDEX);
        item.mDateTakenInMs = cursor.getLong(ImageDbItem.MEDIA_DATE_TAKEN_INDEX);
        item.mDateAddedInSec = cursor.getLong(ImageDbItem.MEDIA_DATE_ADDED_INDEX);
        item.mDateModifiedInSec = cursor.getLong(ImageDbItem.MEDIA_DATE_MODIFIED_INDEX);
        if (item.mDateTakenInMs == item.mDateModifiedInSec) {
            item.mDateTakenInMs = item.mDateModifiedInSec * 1000;
        }

        final int orientationDurationValue = cursor.getInt(ImageDbItem.MEDIA_ORIENTATION_OR_DURATION_INDEX);
        item.mRotation = orientationDurationValue;

        item.mStereoType = 0;
    }
}