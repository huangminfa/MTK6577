/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.mediatek.mpo.MpoDecoder;

public class Thumbnail {
    private static final String TAG = "Thumbnail";

    public static final String LAST_THUMB_FILENAME = "last_thumb";
    private static final int BUFSIZE = 4096;

    private Uri mUri;
    private Bitmap mBitmap;
    // whether this thumbnail is read from file
    private boolean mFromFile = false;

    // Camera, VideoCamera, and Panorama share the same thumbnail. Use sLock
    // to serialize the access.
    private static Object sLock = new Object();

    public Thumbnail(Uri uri, Bitmap bitmap, int orientation) {
        mUri = uri;
        mBitmap = rotateImage(bitmap, orientation);
        if (mBitmap == null) throw new IllegalArgumentException("null bitmap");
    }

    public Uri getUri() {
        return mUri;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setFromFile(boolean fromFile) {
        mFromFile = fromFile;
    }

    public boolean fromFile() {
        return mFromFile;
    }

    private static Bitmap rotateImage(Bitmap bitmap, int orientation) {
        if (orientation != 0) {
            // We only rotate the thumbnail once even if we get OOM.
            Matrix m = new Matrix();
            m.setRotate(orientation, bitmap.getWidth() * 0.5f,
                    bitmap.getHeight() * 0.5f);

            try {
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                // If the rotated bitmap is the original bitmap, then it
                // should not be recycled.
                if (rotated != bitmap) bitmap.recycle();
                return rotated;
            } catch (Throwable t) {
                Log.w(TAG, "Failed to rotate thumbnail", t);
            }
        }
        return bitmap;
    }

    // Stores the bitmap to the specified file.
    public void saveTo(File file) {
        FileOutputStream f = null;
        BufferedOutputStream b = null;
        DataOutputStream d = null;
        synchronized (sLock) {
		    try {
		        f = new FileOutputStream(file);
		        b = new BufferedOutputStream(f, BUFSIZE);
		        d = new DataOutputStream(b);
		        d.writeUTF(mUri.toString());
		        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, d);
		        d.close();
		    } catch (IOException e) {
		        Log.e(TAG, "Fail to store bitmap. path=" + file.getPath(), e);
		    } finally {
		        Util.closeSilently(f);
		        Util.closeSilently(b);
		        Util.closeSilently(d);
		    }
    	}
    }

    // Delete the specified file which saved the thumbnail bitmap.  
    public void deleteFrom(File file) {
        synchronized (sLock) {
	    	file.delete();
    	}
    }

    // Loads the data from the specified file.
    // Returns null if failure.
    public static Thumbnail loadFrom(File file) {
        Uri uri = null;
        Bitmap bitmap = null;
        FileInputStream f = null;
        BufferedInputStream b = null;
        DataInputStream d = null;
        synchronized (sLock) {
		    try {
		        f = new FileInputStream(file);
		        b = new BufferedInputStream(f, BUFSIZE);
		        d = new DataInputStream(b);
		        uri = Uri.parse(d.readUTF());
		        bitmap = BitmapFactory.decodeStream(d);
		        d.close();
		    } catch (IOException e) {
		        Log.i(TAG, "Fail to load bitmap. " + e);
		        return null;
		    } catch (OutOfMemoryError e) {
		    	Log.e(TAG, "loadFrom file fail", e);
		    	return null;
		    } finally {
		        Util.closeSilently(f);
		        Util.closeSilently(b);
		        Util.closeSilently(d);
		    }
        }
        Thumbnail thumbnail = createThumbnail(uri, bitmap, 0);
        if (thumbnail != null) thumbnail.setFromFile(true);
        return thumbnail;
    }

    public static Thumbnail getLastThumbnail(ContentResolver resolver) {
    	Log.i(TAG, "enter getLastThumbnail");
        Media image = getLastImageThumbnail(resolver);
        Media video = getLastVideoThumbnail(resolver);
        if (image == null && video == null) return null;

        Bitmap bitmap = null;
        Media lastMedia;
        try {
	        // If there is only image or video, get its thumbnail. If both exist,
	        // get the thumbnail of the one that is newer.
	        if (image != null && (video == null || image.dateTaken >= video.dateTaken)) {
	            bitmap = Images.Thumbnails.getThumbnail(resolver, image.id,
	                    Images.Thumbnails.MINI_KIND, null);
	            lastMedia = image;
	        } else {
	            bitmap = Video.Thumbnails.getThumbnail(resolver, video.id,
	                    Video.Thumbnails.MINI_KIND, null);
	            lastMedia = video;
	        }
	        // get 2D image if it is 3D file
	        bitmap = create2DFileFromBitmap(bitmap, lastMedia.stereo3dType);
	        // Ensure database and storage are in sync.
	        if (Util.isUriValid(lastMedia.uri, resolver)) {
	            return createThumbnail(lastMedia.uri, bitmap, lastMedia.orientation);
	        }
        } catch(OutOfMemoryError e) {
        	Log.e(TAG, "getThumbnail fail", e);
        }
        Log.i(TAG, "Quit getLastThumbnail");
        return null;
    }

    private static class Media {
        public Media(long id, int orientation, long dateTaken, Uri uri, int stereo3dType) {
            this.id = id;
            this.orientation = orientation;
            this.dateTaken = dateTaken;
            this.uri = uri;
            this.stereo3dType = stereo3dType;
        }

        public final long id;
        public final int orientation;
        public final long dateTaken;
        public final Uri uri;
        public final int stereo3dType;
    }

    public static Media getLastImageThumbnail(ContentResolver resolver) {
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;

        Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
        String[] projection = new String[] {ImageColumns._ID, ImageColumns.ORIENTATION,
                ImageColumns.DATE_TAKEN, Images.Media.STEREO_TYPE};
        String selection = "(" + ImageColumns.MIME_TYPE + "='image/jpeg' OR " + 
                ImageColumns.MIME_TYPE + "='image/mpo' OR " + 
                ImageColumns.MIME_TYPE + "='image/x-jps') AND (" +
                ImageColumns.BUCKET_ID + '=' + Storage.getImageBucketID()+ " OR " +
                ImageColumns.BUCKET_ID + '=' + Storage.BUCKET_ID_3D + ")";
        Log.v(TAG, "!Util.STEREO3D_MODE=" + !Util.STEREO3D_MODE + " S3DMode=" + Util.getS3DMode());
        if (!Util.STEREO3D_MODE && !Util.getS3DMode()) {
        	selection = selection.replace(" OR " + ImageColumns.MIME_TYPE + "='image/x-jps'", " ");
        	selection = selection.replace(" OR " + ImageColumns.BUCKET_ID + '=' + Storage.BUCKET_ID_3D, " ");
        }
        String order = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";
        Log.v(TAG, "selection=" + selection);

        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
            	Log.d(TAG, "getLastImageThumbnail: " + cursor.getString(1));
                long id = cursor.getLong(0);
                return new Media(id, cursor.getInt(1), cursor.getLong(2),
                        ContentUris.withAppendedId(baseUri, id), cursor.getInt(3));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.v(TAG, "cannot search image");
        return null;
    }

    private static Media getLastVideoThumbnail(ContentResolver resolver) {
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;

        Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
        String[] projection = new String[] {VideoColumns._ID, MediaColumns.DATA,
                VideoColumns.DATE_TAKEN, Video.Media.STEREO_TYPE};
        String selection = "(" + VideoColumns.BUCKET_ID + '=' + Storage.getVideoBucketID() + " OR " +
                VideoColumns.BUCKET_ID + '=' + Storage.BUCKET_ID_3D + ")";
        if (!Util.STEREO3D_MODE && !Util.getS3DMode()) {
        	selection = selection.replace(" OR " + VideoColumns.BUCKET_ID + '=' + Storage.BUCKET_ID_3D, " ");
        }
        String order = VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "getLastVideoThumbnail: " + cursor.getString(1));
                long id = cursor.getLong(0);
                return new Media(id, 0, cursor.getLong(2),
                        ContentUris.withAppendedId(baseUri, id), cursor.getInt(3));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.v(TAG, "cannot search videothumbnail");
        return null;
    }

    public static Thumbnail createThumbnail(byte[] jpeg, int orientation, int inSampleSize,
            Uri uri, int stereo3DType) {
        // Create the thumbnail.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = null;
        try {
        	bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, options);
        	bitmap = create2DFileFromBitmap(bitmap, stereo3DType);
        } catch(OutOfMemoryError e) {
        	Log.e(TAG, "createThumbnail fail", e);
			return null;
        }
        return createThumbnail(uri, bitmap, orientation);
    }

	//mtk migration start
    public static Bitmap create2DFileFromBitmap(Bitmap bitmap, int stereo3DType) {

        Log.i(TAG, "create2DFileFromBitmap stereo3DType=" + stereo3DType);
        // split
        if (stereo3DType == MediaStore.Images.Media.STEREO_TYPE_SIDE_BY_SIDE) {
        	bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 2, bitmap.getHeight());
        } else if (stereo3DType == MediaStore.Images.Media.STEREO_TYPE_TOP_BOTTOM) {
        	bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight() / 2);
        }
        return bitmap;
    }

    public static Bitmap decodeLastPictureThumb(String filePath,int inSampleSize, int stereo3DType) {
        Bitmap lastPictureThumb = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		try {
            if (filePath.endsWith(Storage.MPO_NAME_EXT)
                    && stereo3DType != MediaStore.ThreeDimensionColumns.STEREO_TYPE_2D) {
                lastPictureThumb =
                        MpoDecoder.decodeFile(filePath).frameBitmap(0, options);
            } else {
                lastPictureThumb =
                        BitmapFactory.decodeFile(filePath, options);
                // get 2D image if it is 3D file
                lastPictureThumb = create2DFileFromBitmap(lastPictureThumb, stereo3DType);
            }
		} catch (Exception e) {
	        Log.i(TAG, "Exception in decode file path" + filePath);
        }
        Log.i(TAG, "lastPictureThumb = " + lastPictureThumb + "!!!; file path" + filePath);
        return lastPictureThumb;
    }	

    public static Thumbnail createThumbnail(String filePath, int orientation, int inSampleSize,
			Uri uri, int stereo3DType) {
        Bitmap bitmap = decodeLastPictureThumb(filePath,inSampleSize,stereo3DType);
        return createThumbnail(uri, bitmap, orientation);
    }

    public static Thumbnail createThumbnail(String filePath, int orientation, int inSampleSize,
			Uri uri) {
        return createThumbnail(filePath, orientation, inSampleSize, uri,
                MediaStore.Images.Media.STEREO_TYPE_2D);
    }
	//mtk migration end

    public static Bitmap createVideoThumbnail(FileDescriptor fd, int targetWidth) {
        return createVideoThumbnail(null, fd, targetWidth);
    }

    public static Bitmap createVideoThumbnail(String filePath, int targetWidth) {
        return createVideoThumbnail(filePath, null, targetWidth);
    }

    private static Bitmap createVideoThumbnail(String filePath, FileDescriptor fd, int targetWidth) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (filePath != null) {
                retriever.setDataSource(filePath);
            } else {
                retriever.setDataSource(fd);
            }
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (bitmap == null) return null;

        // Scale down the bitmap if it is bigger than we need.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > targetWidth) {
            float scale = (float) targetWidth / width;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    private static Thumbnail createThumbnail(Uri uri, Bitmap bitmap, int orientation) {
        if (bitmap == null) {
            Log.e(TAG, "Failed to create thumbnail from null bitmap");
            return null;
        }
        try {
            return new Thumbnail(uri, bitmap, orientation);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to construct thumbnail", e);
            return null;
        }
    }
}
