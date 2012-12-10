package com.mediatek.media3d;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;

public class ResourceItemSet implements MediaItemSet {

    private MediaItem[] mItems;
    private Resources mResources;

    private static class AssetFileItem implements MediaItem {
        private final String mThumbnail;
        private final String mPhoto;

        AssetFileItem(String thumbnail, String photo) {
            mThumbnail = thumbnail;
            mPhoto = photo;
        }

        public Bitmap getThumbnail(int width, int height) {
            return BitmapFactory.decodeFile(mThumbnail);
        }

        public Uri getUri() {
            return Uri.parse("file://" + mPhoto);
        }

        public String getFilePath() {
            return mPhoto;
        }

        public long getDuration() {
            return 0;
        }

        @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
        public void cancelThumbnailRequest() {
            // Cancel blocking thumbnail request.
        }
    }

    private class RawResourceItem implements MediaItem {
        private final int mThumbnailResId;
        private final int mDataResId;

        RawResourceItem(int thumbnailResId, int dataResId) {
            mThumbnailResId = thumbnailResId;
            mDataResId = dataResId;
        }

        public Bitmap getThumbnail(int width, int height) {
            return MediaUtils.decodeResourceBitmap(mResources, mThumbnailResId, width, height);
        }

        public Uri getUri() {
            return Uri.parse("android.resource://com.mediatek.media3d/" + mDataResId);
        }

        public String getFilePath() {
            return "";
        }

        public long getDuration() {
            return 1000;
        }

        @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
        public void cancelThumbnailRequest() {
            // Cancel blocking thumbnail request.
        }
    }

    public ResourceItemSet(Resources resources, int[] thumbnails, int[] files) {
        mResources = resources;
        mItems = new RawResourceItem[thumbnails.length];
        for (int i = 0; i < thumbnails.length; ++i) {
            mItems[i] = new RawResourceItem(thumbnails[i], files[i]);
        }
    }

    public ResourceItemSet(AssetManager assetManager, String path) {
        try {
            String[] files = assetManager.list(path);
            mItems = new AssetFileItem[files.length];
            for (int i = 0; i < mItems.length; i++) {
                String image = path + "/" + files[i];
                mItems[i] = new AssetFileItem(image, image);
            }
        } catch (IOException e) {
            ;
        }
    }

    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.length;
    }

    public MediaItem getItem(int index) {
        if (index < 0 || index >= getItemCount()) {
            return null;
        }
        return mItems[index];
    }

    public void close() {
        mItems = null;
    }
}
