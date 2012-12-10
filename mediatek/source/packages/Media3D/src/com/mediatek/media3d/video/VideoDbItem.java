package com.mediatek.media3d.video;

import android.provider.MediaStore;
import com.mediatek.media3d.MediaDbItem;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;

import android.provider.MediaStore.Video;
import android.provider.MediaStore.Images;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import com.mediatek.media3d.MediaUtils;

public class VideoDbItem extends MediaDbItem {

    private static String TAG = "VDOOBJECT";
    protected long mDuration;
    protected int mStereoType;

    VideoDbItem(ContentResolver cr, long id, Uri uri, String dataPath, String displayName, long videoDuration, int stereoType) {
        super(cr, id, uri, dataPath, displayName);
        mDuration = videoDuration;
        mStereoType = stereoType;
    }

    public long getDuration() {
        return mDuration;
    }

    public Bitmap getThumbnail() {
        return Video.Thumbnails.getThumbnail(mContentResolver, mId, Images.Thumbnails.MINI_KIND, null);
    }

    private Rect getTargetRect(int srcWidth, int srcHeight, int maxWidth, int maxHeight) {
        float r = (float)srcHeight / srcWidth;

        int height = (int)(maxWidth * r);
        int margin = (maxHeight - height) / 2;
        return new Rect(0 , margin, maxWidth, margin + height);
    }

    public Bitmap getThumbnail(int maxWidth, int maxHeight) {
        Log.v(TAG, "getThumbnail: " + maxWidth + "x" + maxHeight);
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

        Bitmap videoThumb = Video.Thumbnails.getThumbnail(mContentResolver, mId,
                            Images.Thumbnails.MINI_KIND, option);

        if (videoThumb == null) {
            Log.v(TAG, "bitmap create failed!!");
            return null;
        }

        Bitmap b;
        if (mStereoType == 2) {
            b = MediaUtils.cropHalfAndFit(maxWidth, maxHeight, videoThumb, true);
        } else {
            int srcWidth = videoThumb.getWidth();
            int srcHeight = videoThumb.getHeight();

            Rect srcRect = new Rect(0, 0, srcWidth, srcHeight);
            Rect dstRect = getTargetRect(srcWidth, srcHeight, maxWidth, maxHeight);
            b = Bitmap.createBitmap(maxWidth, maxHeight, videoThumb.getConfig());
            if (b == null) {
                Log.v(TAG, "bitmap create failed!!");
                return null;
            }
            b.eraseColor(Color.BLACK);
            Canvas canvas = new Canvas(b);
            canvas.drawBitmap(videoThumb, srcRect, dstRect, null);
            videoThumb.recycle();
        }
        return b;
    }

    public void cancelThumbnailRequest() {
        Video.Thumbnails.cancelThumbnailRequest(mContentResolver, mId);
    }
}
