package com.mediatek.media3d.photo;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import com.mediatek.media3d.Media3D;
import com.mediatek.media3d.MediaDbItem;
import com.mediatek.media3d.MediaUtils;

public final class ImageDbItem extends MediaDbItem {
    private static String TAG = "ImageDbItem";

    public static final int MEDIA_ID_INDEX = 0;
    public static final int MEDIA_CAPTION_INDEX = 1;
    public static final int MEDIA_MIME_TYPE_INDEX = 2;
    public static final int MEDIA_LATITUDE_INDEX = 3;
    public static final int MEDIA_LONGITUDE_INDEX = 4;
    public static final int MEDIA_DATE_TAKEN_INDEX = 5;
    public static final int MEDIA_DATE_ADDED_INDEX = 6;
    public static final int MEDIA_DATE_MODIFIED_INDEX = 7;
    public static final int MEDIA_DATA_INDEX = 8;
    public static final int MEDIA_ORIENTATION_OR_DURATION_INDEX = 9;
    public static final int MEDIA_BUCKET_ID_INDEX = 10;
    public static final int MEDIA_STEREO_TYPE_INDEX = 11;

    public String mMimeType;
    public double mLatitude;
    public double mLongitude;
    public long mDateTakenInMs;
    public long mDateAddedInSec;
    public long mDateModifiedInSec;
    public float mRotation;
    public int mStereoType;

    public ImageDbItem(ContentResolver cr, long id, Uri uri, String dataPath, String displayName) {
        super(cr, id, uri, dataPath, displayName);
    }

    public Bitmap getThumbnail() {
        return Images.Thumbnails.getThumbnail(mContentResolver, mId, Images.Thumbnails.MINI_KIND, null);
    }

    public String getPath() {
        return mDataPath;
    }

    public Bitmap getThumbnail(int maxWidth, int maxHeight) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "getThumbnail (dst): " + maxWidth + "x" + maxHeight);
        }
        if (maxWidth == 0 && maxHeight == 0) {
            return getThumbnail();
        }
        Options option = new Options();
        if (mStereoType == 2) {
            option.inSampleSize = calculateSubSampleSize(
                    Images.Thumbnails.MINI_KIND, maxWidth, maxHeight, true);
        } else {
            option.inSampleSize = calculateSubSampleSize(
                Images.Thumbnails.MINI_KIND, maxWidth, maxHeight, false);
        }

        Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mId,
            Images.Thumbnails.MINI_KIND, option);
        if (miniThumb == null) {
            if (Media3D.DEBUG) {
                Log.v(TAG, "bitmap create failed!!");
            }
            return null;
        }

        if (Media3D.DEBUG) {
            Log.v(TAG, "getThumbnail (src): " + miniThumb.getWidth() + "x" + miniThumb.getHeight());
        }

        Bitmap b;
        if (mStereoType == 2) {
            b = MediaUtils.cropHalfAndFit(maxWidth, maxHeight, miniThumb, true);
        }else {
            b = MediaUtils.resizeBitmapFitTarget(maxWidth, maxHeight, miniThumb);
            miniThumb.recycle();
        }
        return b;
    }

    public long getDuration() {
        return 0;
    }

    public void cancelThumbnailRequest() {
        Images.Thumbnails.cancelThumbnailRequest(mContentResolver, mId);
    }
}
