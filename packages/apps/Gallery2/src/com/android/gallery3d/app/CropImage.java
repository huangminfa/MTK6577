/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.ui.BitmapTileProvider;
import com.android.gallery3d.ui.CropView;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.InterruptableOutputStream;
import com.android.gallery3d.util.MtkLog;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Context;
import android.content.ComponentName;

import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.StereoHelper;

/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImage extends AbstractGalleryActivity {
    private static final String TAG = "CropImage";
    public static final String ACTION_CROP = "com.android.camera.action.CROP";

    private static final int MAX_PIXEL_COUNT = 5 * 1000000; // 5M pixels
    private static final int MAX_FILE_INDEX = 1000;
    private static final int TILE_SIZE = 512;
    private static final int BACKUP_PIXEL_COUNT = 480000; // around 800x600

    private static final int MSG_LARGE_BITMAP = 1;
    private static final int MSG_BITMAP = 2;
    private static final int MSG_SAVE_COMPLETE = 3;
    private static final int MSG_SHOW_SAVE_ERROR = 4;
    private static final int MSG_SECOND_BITMAP = 5;

    //message that communicate with 3D wallpaper service
    private final static int MSG_SET_3D_WALLPAPER = 101;
    private final static int MSG_3D_WALLPAPER_RESULT_OK = 102;
    private final static int MSG_3D_WALLPAPER_RESULT_ERROR = 103;

    private static final int MAX_BACKUP_IMAGE_SIZE = 320;
    private static final int DEFAULT_COMPRESS_QUALITY = 90;
    private static final String TIME_STAMP_NAME = "'IMG'_yyyyMMdd_HHmmss";

    // Change these to Images.Media.WIDTH/HEIGHT after they are unhidden.
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";

    public static final String KEY_RETURN_DATA = "return-data";
    public static final String KEY_CROPPED_RECT = "cropped-rect";
    public static final String KEY_ASPECT_X = "aspectX";
    public static final String KEY_ASPECT_Y = "aspectY";
    public static final String KEY_SPOTLIGHT_X = "spotlightX";
    public static final String KEY_SPOTLIGHT_Y = "spotlightY";
    public static final String KEY_OUTPUT_X = "outputX";
    public static final String KEY_OUTPUT_Y = "outputY";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_DATA = "data";
    public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    public static final String KEY_OUTPUT_FORMAT = "outputFormat";
    public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    public static final String KEY_NO_FACE_DETECTION = "noFaceDetection";

    private static final String KEY_STATE = "state";

    private static final int STATE_INIT = 0;
    private static final int STATE_LOADED = 1;
    private static final int STATE_SAVING = 2;
    private static final int STATE_SAVE_DONE = 3;

    public static final String DOWNLOAD_STRING = "download";
    public static final File DOWNLOAD_BUCKET = new File(
            Environment.getExternalStorageDirectory(), DOWNLOAD_STRING);

    public static final String CROP_ACTION = "com.android.camera.action.CROP";

    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();

    public static final String SET_3D_WALLPAPER =
                            "com.mediatek.stereo3dwallpaper.SET_WALLPAPER";
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();
    private boolean mSetStereoWallpaper = false;
    private Messenger mService = null;
    private boolean mIsBounded;
    private Intent mStereoWallpaperIntent;
    private Uri mStereoWallpaperUri = null;
    private Future<Bitmap> mLoadSecondBitmapTask;
    private boolean mDecodedSecondImage = false;

    private int mState = STATE_INIT;

    private CropView mCropView;

    private boolean mDoFaceDetection = true;

    private Handler mMainHandler;

    // We keep the following members so that we can free them

    // mBitmap is the unrotated bitmap we pass in to mCropView for detect faces.
    // mCropView is responsible for rotating it to the way that it is viewed by users.
    private Bitmap mBitmap;
    private BitmapTileProvider mBitmapTileProvider;
    private BitmapRegionDecoder mRegionDecoder;
    private Bitmap mBitmapInIntent;
    private boolean mUseRegionDecoder = false;

    private ProgressDialog mProgressDialog;
    private Future<BitmapRegionDecoder> mLoadTask;
    private Future<Bitmap> mLoadBitmapTask;
    private Future<Intent> mSaveTask;

    private MediaItem mMediaItem;

    //cached object to control it in this activity.
    private TileImageViewAdapter mTIVA;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // Initialize UI
        setContentView(R.layout.cropimage);
        mCropView = new CropView(this);
        getGLRoot().setContentPane(mCropView);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);

        mMediaItem = getMediaItemFromIntentData();
        if (mMediaItem == null) {
            Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.i(TAG,"create:mMediaItem="+mMediaItem);

        mSetStereoWallpaper = 
                mIsStereoDisplaySupported &&
                ((mMediaItem.getSupportedOperations() & 
                  MediaItem.SUPPORT_STEREO_DISPLAY) != 0) &&
                ((mMediaItem.getSupportedOperations() & 
                  MediaItem.SUPPORT_CONVERT_TO_3D) == 0) &&
                null != getIntent().getExtras() &&
                getIntent().getExtras().getBoolean(KEY_SET_AS_WALLPAPER, false);
        Log.d(TAG,"onCreate:mSetStereoWallpaper="+mSetStereoWallpaper);

        if (mSetStereoWallpaper) {
            //
            mCropView.setStereoWallpaperMode(true);
            //modify action bar title
            actionBar.setTitle(R.string.stereo3d_preview_title);
        }

        mMainHandler = new SynchronizedHandler(getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LARGE_BITMAP: {
                        mProgressDialog.dismiss();
                        onBitmapRegionDecoderAvailable((BitmapRegionDecoder) message.obj);
                        break;
                    }
                    case MSG_BITMAP: {
                        mProgressDialog.dismiss();
                        onBitmapAvailable((Bitmap) message.obj);
                        break;
                    }
                    case MSG_SHOW_SAVE_ERROR: {
                        mProgressDialog.dismiss();
                        setResult(RESULT_CANCELED);
                        Toast.makeText(CropImage.this,
                                CropImage.this.getString(R.string.save_error),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    case MSG_SAVE_COMPLETE: {
                        mProgressDialog.dismiss();
                        setResult(RESULT_OK, (Intent) message.obj);
                        finish();
                        break;
                    }
                    case MSG_SECOND_BITMAP: {
                        //added to treat the decoded second image of stereo photo
                        onSecondBitmapAvailable((Bitmap) message.obj);
                        break;
                    }
                }
            }
        };

        setCropParameters();
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putInt(KEY_STATE, mState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.crop, menu);

        //when setting stereo wallpaper, display "SET" instead of "CROP"
        if (mSetStereoWallpaper) {
            MenuItem item = menu.findItem(R.id.save);
            if (item != null) item.setTitle(R.string.stereo3d_preview_button_set);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.cancel: {
                setResult(RESULT_CANCELED);
                finish();
                break;
            }
            case R.id.save: {
                onSaveClicked();
                break;
            }
        }
        return true;
    }

    private class SaveOutput implements Job<Intent> {
        private final RectF mCropRect;

        public SaveOutput(RectF cropRect) {
            mCropRect = cropRect;
        }

        public Intent run(JobContext jc) {
            RectF cropRect = mCropRect;
            Bundle extra = getIntent().getExtras();

            Rect rect = new Rect(
                    Math.round(cropRect.left), Math.round(cropRect.top),
                    Math.round(cropRect.right), Math.round(cropRect.bottom));

            Intent result = new Intent();
            result.putExtra(KEY_CROPPED_RECT, rect);
            Bitmap cropped = null;
            boolean outputted = false;
            if (extra != null) {
                Uri uri = (Uri) extra.getParcelable(MediaStore.EXTRA_OUTPUT);
                if (uri != null) {
                    if (jc.isCancelled()) return null;
                    outputted = true;
                    cropped = getCroppedImage(jc, rect);
                    if (!saveBitmapToUri(jc, cropped, uri)) return null;
                }
                if (extra.getBoolean(KEY_RETURN_DATA, false)) {
                    if (jc.isCancelled()) return null;
                    outputted = true;
                    if (cropped == null) cropped = getCroppedImage(jc, rect);
                    result.putExtra(KEY_DATA, cropped);
                }
                if (extra.getBoolean(KEY_SET_AS_WALLPAPER, false)) {
                    if (jc.isCancelled()) return null;
                    outputted = true;
                    //if 3d wallpaper is to be set, we bind to 3D wallpaper
                    //service and send uri to it.
                    if (mSetStereoWallpaper) {
                        mStereoWallpaperUri = getIntent().getData();
                        Log.i(TAG,"saveoutput:run:mStereoWallpaperUri="+mStereoWallpaperUri);
                        //bind remote service
                        if (!doBindService()) {
                            //we have to change the state to save done because
                            //progress dialog will never disappear when this
                            //variable remains unchanged. This may be Google
                            //default issue, which other cases do not encounter.
                            mState = STATE_SAVE_DONE;
                            return null;
                        }
                    } else {
                        if (cropped == null) cropped = getCroppedImage(jc, rect);
                        if (!setAsWallpaper(jc, cropped)) return null;
                    }
                }
            }
            if (!outputted) {
                if (jc.isCancelled()) return null;
                if (cropped == null) cropped = getCroppedImage(jc, rect);
                Uri data = saveToMediaProvider(jc, cropped);
                if (data != null) result.setData(data);
            }
            mState = STATE_SAVE_DONE;
            return result;
        }
    }

    public static String determineCompressFormat(MediaObject obj) {
        String compressFormat = "JPEG";
        if (obj instanceof MediaItem) {
            String mime = ((MediaItem) obj).getMimeType();
            if (mime.contains("png") || mime.contains("gif")) {
              // Set the compress format to PNG for png and gif images
              // because they may contain alpha values.
              compressFormat = "PNG";
            }
        }
        return compressFormat;
    }

    private boolean setAsWallpaper(JobContext jc, Bitmap wallpaper) {
        try {
            WallpaperManager.getInstance(this).setBitmap(wallpaper);
        } catch (IOException e) {
            Log.w(TAG, "fail to set wall paper", e);
        }
        return true;
    }

    private File saveMedia(
            JobContext jc, Bitmap cropped, File directory, String filename) {
        // Try file-1.jpg, file-2.jpg, ... until we find a filename
        // which does not exist yet.
        File candidate = null;
        String fileExtension = getFileExtension();
        for (int i = 1; i < MAX_FILE_INDEX; ++i) {
            candidate = new File(directory, filename + "-" + i + "."
                    + fileExtension);
            try {
                if (candidate.createNewFile()) break;
            } catch (IOException e) {
                Log.e(TAG, "fail to create new file: "
                        + candidate.getAbsolutePath(), e);
                return null;
            }
        }
        if (!candidate.exists() || !candidate.isFile()) {
            throw new RuntimeException("cannot create file: " + filename);
        }

        candidate.setReadable(true, false);
        candidate.setWritable(true, false);

        try {
            FileOutputStream fos = new FileOutputStream(candidate);
            try {
                saveBitmapToOutputStream(jc, cropped,
                        convertExtensionToCompressFormat(fileExtension), fos);
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "fail to save image: "
                    + candidate.getAbsolutePath(), e);
            candidate.delete();
            return null;
        }

        if (jc.isCancelled()) {
            candidate.delete();
            return null;
        }

        return candidate;
    }

    private Uri saveToMediaProvider(JobContext jc, Bitmap cropped) {
        if (PicasaSource.isPicasaImage(mMediaItem)) {
            return savePicasaImage(jc, cropped);
        } else if (mMediaItem instanceof LocalImage) {
            Uri uri = saveLocalImage(jc, cropped);
            if (mIsDrmSupported) {
                Path path = mMediaItem.getPath();
                int mtkInclusion = null == path ? 0 : path.getMtkInclusion();
                if (mtkInclusion != 0) {
                uri = uri.buildUpon().appendQueryParameter("mtkInclusion", 
                     String.valueOf(mtkInclusion)).build();
                }
            }
            return uri;
        } else {
            return saveGenericImage(jc, cropped);
        }
    }

    private Uri savePicasaImage(JobContext jc, Bitmap cropped) {
        if (!DOWNLOAD_BUCKET.isDirectory() && !DOWNLOAD_BUCKET.mkdirs()) {
            throw new RuntimeException("cannot create download folder");
        }

        String filename = PicasaSource.getImageTitle(mMediaItem);
        int pos = filename.lastIndexOf('.');
        if (pos >= 0) filename = filename.substring(0, pos);
        File output = saveMedia(jc, cropped, DOWNLOAD_BUCKET, filename);
        if (output == null) return null;

        copyExif(mMediaItem, output.getAbsolutePath(), cropped.getWidth(), cropped.getHeight());

        long now = System.currentTimeMillis() / 1000;
        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, PicasaSource.getImageTitle(mMediaItem));
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, PicasaSource.getDateTaken(mMediaItem));
        values.put(Images.Media.DATE_MODIFIED, now);
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.MIME_TYPE, getOutputMimeType());
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());
        values.put(WIDTH, cropped.getWidth());
        values.put(HEIGHT, cropped.getHeight());

        double latitude = PicasaSource.getLatitude(mMediaItem);
        double longitude = PicasaSource.getLongitude(mMediaItem);
        if (GalleryUtils.isValidLocation(latitude, longitude)) {
            values.put(Images.Media.LATITUDE, latitude);
            values.put(Images.Media.LONGITUDE, longitude);
        }
        return getContentResolver().insert(
                Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Uri saveLocalImage(JobContext jc, Bitmap cropped) {
        LocalImage localImage = (LocalImage) mMediaItem;

        File oldPath = new File(localImage.filePath);
        File directory = new File(oldPath.getParent());

        String filename = oldPath.getName();
        int pos = filename.lastIndexOf('.');
        if (pos >= 0) filename = filename.substring(0, pos);
        File output = saveMedia(jc, cropped, directory, filename);
        if (output == null) return null;

        copyExif(oldPath.getAbsolutePath(), output.getAbsolutePath(),
                cropped.getWidth(), cropped.getHeight());

        long now = System.currentTimeMillis() / 1000;
        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, localImage.caption);
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, localImage.dateTakenInMs);
        values.put(Images.Media.DATE_MODIFIED, now);
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.MIME_TYPE, getOutputMimeType());
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());
        values.put(WIDTH, cropped.getWidth());
        values.put(HEIGHT, cropped.getHeight());

        if (GalleryUtils.isValidLocation(localImage.latitude, localImage.longitude)) {
            values.put(Images.Media.LATITUDE, localImage.latitude);
            values.put(Images.Media.LONGITUDE, localImage.longitude);
        }
        return getContentResolver().insert(
                Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Uri saveGenericImage(JobContext jc, Bitmap cropped) {
        if (!DOWNLOAD_BUCKET.isDirectory() && !DOWNLOAD_BUCKET.mkdirs()) {
            throw new RuntimeException("cannot create download folder");
        }

        long now = System.currentTimeMillis();
        String filename = new SimpleDateFormat(TIME_STAMP_NAME).
                format(new Date(now));

        File output = saveMedia(jc, cropped, DOWNLOAD_BUCKET, filename);
        if (output == null) return null;

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, filename);
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, now);
        values.put(Images.Media.DATE_MODIFIED, now / 1000);
        values.put(Images.Media.DATE_ADDED, now / 1000);
        values.put(Images.Media.MIME_TYPE, getOutputMimeType());
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());
        values.put(WIDTH, cropped.getWidth());
        values.put(HEIGHT, cropped.getHeight());

        return getContentResolver().insert(
                Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private boolean saveBitmapToOutputStream(
            JobContext jc, Bitmap bitmap, CompressFormat format, OutputStream os) {
        // We wrap the OutputStream so that it can be interrupted.
        final InterruptableOutputStream ios = new InterruptableOutputStream(os);
        jc.setCancelListener(new CancelListener() {
                public void onCancel() {
                    ios.interrupt();
                }
            });
        try {
            bitmap.compress(format, DEFAULT_COMPRESS_QUALITY, os);
            return !jc.isCancelled();
        } finally {
            jc.setCancelListener(null);
            Utils.closeSilently(os);
        }
    }

    private boolean saveBitmapToUri(JobContext jc, Bitmap bitmap, Uri uri) {
        try {
            return saveBitmapToOutputStream(jc, bitmap,
                    convertExtensionToCompressFormat(getFileExtension()),
                    getContentResolver().openOutputStream(uri));
        } catch (FileNotFoundException e) {
            Log.w(TAG, "cannot write output", e);
        }
        return true;
    }

    private CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png")
                ? CompressFormat.PNG
                : CompressFormat.JPEG;
    }

    private String getOutputMimeType() {
        return getFileExtension().equals("png") ? "image/png" : "image/jpeg";
    }

    private String getFileExtension() {
        String requestFormat = getIntent().getStringExtra(KEY_OUTPUT_FORMAT);
        String outputFormat = (requestFormat == null)
                ? determineCompressFormat(mMediaItem)
                : requestFormat;

        outputFormat = outputFormat.toLowerCase();
        return (outputFormat.equals("png") || outputFormat.equals("gif"))
                ? "png" // We don't support gif compression.
                : "jpg";
    }

    private void onSaveClicked() {
        Bundle extra = getIntent().getExtras();
        RectF cropRect = mCropView.getCropRectangle();
        if (cropRect == null) return;
        mState = STATE_SAVING;
        int messageId = extra != null && extra.getBoolean(KEY_SET_AS_WALLPAPER)
                ? R.string.wallpaper
                : R.string.saving_image;
        mProgressDialog = ProgressDialog.show(
                this, null, getString(messageId), true, false);
        if (null != mTIVA) {
            //when save clicked, we know that no further decoder task
            //in TileImageViewAdapter is meaningful, so we clear it to
            //to avoid blocking by its synchronizing RegionDecoder when
            //we call getCroppedImage() for very long time
            //Note: we should also add protection in TileImageViewAdapter
            //to avoid race condition.
            if (!mSetStereoWallpaper) {
                Log.i(TAG,"onSaveClick:clear TileImageViewAdapter");
                mTIVA.clear();
            } else {
                Log.i(TAG,"onSaveClick:set null RegionDecoder");
                mTIVA.setStereo(null, mBitmap,mBitmap.getWidth(),mBitmap.getHeight());
            }
        }
        mSaveTask = getThreadPool().submit(new SaveOutput(cropRect),
                new FutureListener<Intent>() {
            public void onFutureDone(Future<Intent> future) {
                mSaveTask = null;
                if (STATE_SAVE_DONE != mState) {
                    MtkLog.w(TAG, "save task: save state != STATE_SAVE_DONE, cancel and return...");
                    mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    return;
                }
                // The future has been canceled, but the saving process
                // has actually been done, so we continue to send MSG_SAVE_COMPLETE
                // and therefore common out the following line
                // if (future.isCancelled()) return;
                Intent intent = future.get();
                if (intent != null) {
                    //Added for stereo wallpaper
                    if (mSetStereoWallpaper) {
                        mStereoWallpaperIntent = intent;
                        return;
                    }

                    Log.v(TAG,"SaveTask:onFutureDone:send complete message");
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(
                            MSG_SAVE_COMPLETE, intent));
                } else {
                    Log.v(TAG,"SaveTask:onFutureDone:send error message");
                    mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                }
            }
        });
    }

    private Bitmap getCroppedImage(JobContext jc, Rect rect) {
        Utils.assertTrue(rect.width() > 0 && rect.height() > 0);

        Bundle extras = getIntent().getExtras();
        // (outputX, outputY) = the width and height of the returning bitmap.
        int outputX = rect.width();
        int outputY = rect.height();
        if (extras != null) {
            outputX = extras.getInt(KEY_OUTPUT_X, outputX);
            outputY = extras.getInt(KEY_OUTPUT_Y, outputY);
        }

        if (outputX * outputY > MAX_PIXEL_COUNT) {
            float scale = (float) Math.sqrt(
                    (double) MAX_PIXEL_COUNT / outputX / outputY);
            Log.w(TAG, "scale down the cropped image: " + scale);
            outputX = Math.round(scale * outputX);
            outputY = Math.round(scale * outputY);
        }

        // (rect.width() * scaleX, rect.height() * scaleY) =
        // the size of drawing area in output bitmap
        float scaleX = 1;
        float scaleY = 1;
        Rect dest = new Rect(0, 0, outputX, outputY);
        if (extras == null || extras.getBoolean(KEY_SCALE, true) || 
            extras.getBoolean(KEY_SCALE_UP_IF_NEEDED, false)) {
            scaleX = (float) outputX / rect.width();
            scaleY = (float) outputY / rect.height();
            if (extras == null || !extras.getBoolean(
                    KEY_SCALE_UP_IF_NEEDED, false)) {
                if (scaleX > 1f) scaleX = 1;
                if (scaleY > 1f) scaleY = 1;
            }
        }

        // Keep the content in the center (or crop the content)
        int rectWidth = Math.round(rect.width() * scaleX);
        int rectHeight = Math.round(rect.height() * scaleY);
        dest.set(Math.round((outputX - rectWidth) / 2f),
                Math.round((outputY - rectHeight) / 2f),
                Math.round((outputX + rectWidth) / 2f),
                Math.round((outputY + rectHeight) / 2f));

        if (mBitmapInIntent != null) {
            Bitmap source = mBitmapInIntent;
            Bitmap result = Bitmap.createBitmap(
                    outputX, outputY, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(source, rect, dest, null);
            return result;
        }

        if (mUseRegionDecoder) {
            int rotation = mMediaItem.getFullImageRotation();
            rotateRectangle(rect, mCropView.getImageWidth(),
                    mCropView.getImageHeight(), 360 - rotation);
            rotateRectangle(dest, outputX, outputY, 360 - rotation);

            BitmapFactory.Options options = new BitmapFactory.Options();
            int sample = BitmapUtils.computeSampleSizeLarger(
                    Math.max(scaleX, scaleY));
            options.inSampleSize = sample;
            if ((rect.width() / sample) == dest.width()
                    && (rect.height() / sample) == dest.height()
                    && rotation == 0) {
                // To prevent concurrent access in GLThread
                synchronized (mRegionDecoder) {
                    Bitmap bmp = null;
                    try {
                        Log.i(TAG,"getCroppedImage:decodeRegion(rect="+rect+"..)");
                        bmp = mRegionDecoder.decodeRegion(rect, options);
                        Log.v(TAG,"getCroppedImage:decodeRegion() returns"+bmp);
                    } catch (OutOfMemoryError e) {
                        Log.w(TAG,"getCroppedImage:out of memory when decoding:"+e);
                        bmp = null;
                    }
                    //As there is a chance no enough dvm memory for decoded Bitmap,
                    //Skia will return a null Bitmap. In this case, we have to
                    //downscale the decoded Bitmap by increase the options.inSampleSize
                    if (null == bmp) {
                        final int maxTryNum = 8;
                        for (int i=0; i < maxTryNum; i++) {
                            //we increase inSampleSize to expect a smaller Bitamp
                            options.inSampleSize *= 2;
                            Log.w(TAG,"getCroppedImage:try for sample size " +
                                    options.inSampleSize);
                            try {
                                Log.i(TAG,"getCroppedImage:decodeRegion(rect="+rect+"..)");
                                bmp = mRegionDecoder.decodeRegion(rect, options);
                                Log.v(TAG,"getCroppedImage:decodeRegion() returns"+bmp);
                            } catch (OutOfMemoryError e) {
                                Log.w(TAG,"getCroppedImage:out of memory when decoding:"+e);
                                bmp = null;
                            }
                            if (null != bmp) break;
                        }
                        if (null == bmp) {
                            Log.e(TAG,"getCroppedImage:failed to get a Bitmap");
                            return null;
                        }
                        //modify outputX,outputY if needed.
                        if (extras != null) {
                            if (outputX == extras.getInt(KEY_OUTPUT_X, 0) &&
                                outputY == extras.getInt(KEY_OUTPUT_Y, 0)) {
                                //if outputX & outputY is determined from extras,
                                //remains unchanged
                            } else {
                                //if outputX & outputY is determined by rect, change it
                                outputX = bmp.getWidth();
                                outputY = bmp.getHeight();
                            }
                        } else {
                            //if extra is null, outputX & outputY is directly determined
                            //from rect and MAX_PIXEL_COUNT
                            outputX = bmp.getWidth();
                            outputY = bmp.getHeight();
                        }
                    }
                    //modify rect
                    rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

                    Bitmap ret = null;
                    if (extras != null && extras.getBoolean(KEY_SCALE_UP_IF_NEEDED, false)
                            && ((scaleX > 1f) || (scaleY > 1f))) {
                        ret = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
                        Canvas c = new Canvas(ret);
                        c.drawBitmap(bmp, rect, new Rect(0, 0, outputX, outputY), null);
                        bmp.recycle();
                    } else {
                        ret = bmp;
                    }
                    
                    return ret;
                }
            }
            Bitmap result = Bitmap.createBitmap(
                    outputX, outputY, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            rotateCanvas(canvas, outputX, outputY, rotation);
            drawInTiles(jc, canvas, mRegionDecoder, rect, dest, sample);
            return result;
        } else {
            int rotation = mMediaItem.getRotation();
            rotateRectangle(rect, mCropView.getImageWidth(),
                    mCropView.getImageHeight(), 360 - rotation);
            rotateRectangle(dest, outputX, outputY, 360 - rotation);
            Bitmap result = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            rotateCanvas(canvas, outputX, outputY, rotation);
            canvas.drawBitmap(mBitmap,
                    rect, dest, new Paint(Paint.FILTER_BITMAP_FLAG));
            return result;
        }
    }

    private static void rotateCanvas(
            Canvas canvas, int width, int height, int rotation) {
        canvas.translate(width / 2, height / 2);
        canvas.rotate(rotation);
        if (((rotation / 90) & 0x01) == 0) {
            canvas.translate(-width / 2, -height / 2);
        } else {
            canvas.translate(-height / 2, -width / 2);
        }
    }

    private static void rotateRectangle(
            Rect rect, int width, int height, int rotation) {
        if (rotation == 0 || rotation == 360) return;

        int w = rect.width();
        int h = rect.height();
        switch (rotation) {
            case 90: {
                rect.top = rect.left;
                rect.left = height - rect.bottom;
                rect.right = rect.left + h;
                rect.bottom = rect.top + w;
                return;
            }
            case 180: {
                rect.left = width - rect.right;
                rect.top = height - rect.bottom;
                rect.right = rect.left + w;
                rect.bottom = rect.top + h;
                return;
            }
            case 270: {
                rect.left = rect.top;
                rect.top = width - rect.right;
                rect.right = rect.left + h;
                rect.bottom = rect.top + w;
                return;
            }
            default: throw new AssertionError();
        }
    }

    private void drawInTiles(JobContext jc, Canvas canvas,
            BitmapRegionDecoder decoder, Rect rect, Rect dest, int sample) {
        int tileSize = TILE_SIZE * sample;
        Rect tileRect = new Rect();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inSampleSize = sample;
        canvas.translate(dest.left, dest.top);
        canvas.scale((float) sample * dest.width() / rect.width(),
                (float) sample * dest.height() / rect.height());
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        for (int tx = rect.left, x = 0;
                tx < rect.right; tx += tileSize, x += TILE_SIZE) {
            for (int ty = rect.top, y = 0;
                    ty < rect.bottom; ty += tileSize, y += TILE_SIZE) {
                tileRect.set(tx, ty, tx + tileSize, ty + tileSize);
                if (tileRect.intersect(rect)) {
                    Bitmap bitmap;
                    //add protection in case rect is not valid
                    try {
                        // To prevent concurrent access in GLThread
                        synchronized (decoder) {
                            MtkLog.v(TAG, "drawInTiles() begin decodeRegion()");
                            if (jc != null && jc.isCancelled()) {
                                MtkLog.w(TAG, "drawInTiles() job was canceled!");
                                return;
                            }
                            bitmap = decoder.decodeRegion(tileRect, options);
                            MtkLog.v(TAG, "drawInTiles() end decodeRegion()");
                        }
                        canvas.drawBitmap(bitmap, x, y, paint);
                        bitmap.recycle();
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG,"drawInTiles:got exception:"+e);
                    }
                }
            }
        }
    }

    private void onBitmapRegionDecoderAvailable(
            BitmapRegionDecoder regionDecoder) {

        if (regionDecoder == null) {
            //Toast.makeText(this, R.string.load_image_fail, Toast.LENGTH_SHORT).show();
            //finish();
            //There is a chance that a image has no region decoder but can decode
            //thumbnail. some we change to decode thumbnail when fail.
            Log.w(TAG,"onBitmapRegionDecoderAvailable:failed and start to load thumb");
            startLoadBitmapTask();
            return;
        }
        mRegionDecoder = regionDecoder;
        mUseRegionDecoder = true;
        mState = STATE_LOADED;

        BitmapFactory.Options options = new BitmapFactory.Options();
        int width = regionDecoder.getWidth();
        int height = regionDecoder.getHeight();

        if (mIsStereoDisplaySupported &&
            ((mMediaItem.getSupportedOperations() & 
              MediaItem.SUPPORT_STEREO_DISPLAY) != 0)) {
            width = StereoHelper.adjustDim(true, mMediaItem.getStereoLayout(),
                                               width);
            height = StereoHelper.adjustDim(false, mMediaItem.getStereoLayout(),
                                                height);
        }

        options.inSampleSize = BitmapUtils.computeSampleSize(width, height,
                BitmapUtils.UNCONSTRAINED, BACKUP_PIXEL_COUNT);
        MtkLog.v(TAG, "onBitmapRegionDecoderAvailable() begin decodeRegion()");
        mBitmap = regionDecoder.decodeRegion(
                new Rect(0, 0, width, height), options);
        MtkLog.v(TAG, "onBitmapRegionDecoderAvailable() end decodeRegion()");
        if (mBitmap == null) {
            Log.e(TAG, "region decoder failed to decode the image!");
            Toast.makeText(this, R.string.load_image_fail, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //mCropView.setDataModel(new TileImageViewAdapter(mBitmap, 
        //        regionDecoder).setEnablePQ(false), mMediaItem.getFullImageRotation());
        TileImageViewAdapter tiva = new TileImageViewAdapter(mBitmap, regionDecoder);
        tiva.setEnablePQ(false);

        //adjust full image dimesion if needed
        if (mIsStereoDisplaySupported &&
            ((mMediaItem.getSupportedOperations() & 
              MediaItem.SUPPORT_STEREO_DISPLAY) != 0)) {
            tiva.setStereo(regionDecoder, mBitmap, width, height);
        }

        //we hold this object
        mTIVA = tiva;
        mCropView.setDataModel(tiva, mMediaItem.getFullImageRotation());
        if (mDoFaceDetection) {
            mCropView.detectFaces(mBitmap);
        } else {
            mCropView.initializeHighlightRectangle();
        }
    }

    private void onBitmapAvailable(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, R.string.load_image_fail, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mUseRegionDecoder = false;
        mState = STATE_LOADED;

        mBitmap = bitmap;
        //BitmapFactory.Options options = new BitmapFactory.Options();
        mCropView.setDataModel(new BitmapTileProvider(bitmap, 512),
                mMediaItem.getRotation());
        if (mDoFaceDetection) {
            mCropView.detectFaces(bitmap);
        } else {
            mCropView.initializeHighlightRectangle();
        }

        if (mSetStereoWallpaper) {
            mLoadSecondBitmapTask = getThreadPool().submit(
                    new LoadSecondBitmapDataTask(mMediaItem),
                    new FutureListener<Bitmap>() {
                public void onFutureDone(Future<Bitmap> future) {
                    mLoadSecondBitmapTask = null;
                    Bitmap bitmap = future.get();
                    if (future.isCancelled()) {
                        if (bitmap != null) bitmap.recycle();
                        return;
                    }
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(
                            MSG_SECOND_BITMAP, bitmap));
                }
            });
        }
    }

    private void onSecondBitmapAvailable(Bitmap bitmap) {
        Log.i(TAG,"onSecondBitmapAvailable()");
        if (bitmap == null) {
            Log.w(TAG,"onSecondBitmapAvailable:got null second image");
            return;
        }

        //BitmapFactory.Options options = new BitmapFactory.Options();
        mCropView.setDataModel(new BitmapTileProvider(bitmap, 512),
                mMediaItem.getRotation());
        TileImageViewAdapter tiva = new TileImageViewAdapter();
        tiva.setBackupImage(mBitmap,mBitmap.getWidth(),mBitmap.getHeight());
        tiva.setSecondImage(bitmap);
        //we hold this object
        mTIVA = tiva;
        //after loaded second image, enter stereo mode
        mCropView.setStereoMode(true);
        mCropView.setDataModel(tiva, mMediaItem.getFullImageRotation());
    }

    private void setCropParameters() {
        Bundle extras = getIntent().getExtras();
        if (extras == null)
            return;
        int aspectX = extras.getInt(KEY_ASPECT_X, 0);
        int aspectY = extras.getInt(KEY_ASPECT_Y, 0);
        if (aspectX != 0 && aspectY != 0) {
            mCropView.setAspectRatio((float) aspectX / aspectY);
        }

        float spotlightX = extras.getFloat(KEY_SPOTLIGHT_X, 0);
        float spotlightY = extras.getFloat(KEY_SPOTLIGHT_Y, 0);
        if (spotlightX != 0 && spotlightY != 0) {
            mCropView.setSpotlightRatio(spotlightX, spotlightY);
        }
    }

    private void initializeData() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey(KEY_NO_FACE_DETECTION)) {
                mDoFaceDetection = !extras.getBoolean(KEY_NO_FACE_DETECTION);
            }

            mBitmapInIntent = extras.getParcelable(KEY_DATA);

            if (mBitmapInIntent != null) {
                mBitmapTileProvider =
                        new BitmapTileProvider(mBitmapInIntent, MAX_BACKUP_IMAGE_SIZE);
                mCropView.setDataModel(mBitmapTileProvider, 0);
                if (mDoFaceDetection) {
                    mCropView.detectFaces(mBitmapInIntent);
                } else {
                    mCropView.initializeHighlightRectangle();
                }
                mState = STATE_LOADED;
                return;
            }
        }

        mProgressDialog = ProgressDialog.show(
                this, null, getString(R.string.loading_image), true, false);

        //as we have shift the create of MediaItem to onCreate,
        //we add a protection here
        if (null == mMediaItem) {
            mMediaItem = getMediaItemFromIntentData();
        }
        if (mMediaItem == null) {
            mProgressDialog.dismiss();
            Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        boolean supportedByBitmapRegionDecoder =
            (mMediaItem.getSupportedOperations() & MediaItem.SUPPORT_FULL_IMAGE) != 0;

        //when set 3D stereo wallpaper, we don't show crop box, we won't decode tile
        supportedByBitmapRegionDecoder = supportedByBitmapRegionDecoder &&
                                         !mSetStereoWallpaper;

        if (supportedByBitmapRegionDecoder) {
            mLoadTask = getThreadPool().submit(new LoadDataTask(mMediaItem),
                    new FutureListener<BitmapRegionDecoder>() {
                public void onFutureDone(Future<BitmapRegionDecoder> future) {
                    mLoadTask = null;
                    BitmapRegionDecoder decoder = future.get();
                    if (future.isCancelled()) {
                        if (decoder != null) decoder.recycle();
                        return;
                    }
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(
                            MSG_LARGE_BITMAP, decoder));
                }
            });
        } else {
            //mLoadBitmapTask = getThreadPool().submit(new LoadBitmapDataTask(mMediaItem),
            //        new FutureListener<Bitmap>() {
            //    public void onFutureDone(Future<Bitmap> future) {
            //        mLoadBitmapTask = null;
            //        Bitmap bitmap = future.get();
            //        if (future.isCancelled()) {
            //            if (bitmap != null) bitmap.recycle();
            //            return;
            //        }
            //        mMainHandler.sendMessage(mMainHandler.obtainMessage(
            //                MSG_BITMAP, bitmap));
            //    }
            //});
            //we shift the code to a function because we want to reuse this
            //code when get region decoder failed
            startLoadBitmapTask();
        }
    }

    private void startLoadBitmapTask() {
        mLoadBitmapTask = getThreadPool().submit(new LoadBitmapDataTask(mMediaItem),
                new FutureListener<Bitmap>() {
            public void onFutureDone(Future<Bitmap> future) {
                mLoadBitmapTask = null;
                Bitmap bitmap = future.get();
                if (future.isCancelled()) {
                    if (bitmap != null) bitmap.recycle();
                    return;
                }
                mMainHandler.sendMessage(mMainHandler.obtainMessage(
                        MSG_BITMAP, bitmap));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mState == STATE_INIT) initializeData();
        // if (mState == STATE_SAVING) onSaveClicked();        
        if (mState == STATE_SAVING) onSaveResumed();

        // TODO: consider to do it in GLView system
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            mCropView.resume();
        } finally {
            root.unlockRenderThread();
        }
    }

    private void onSaveResumed() {
        Bundle extra = getIntent().getExtras();
        int messageId = extra != null && extra.getBoolean(KEY_SET_AS_WALLPAPER)
                ? R.string.wallpaper
                : R.string.saving_image;
        mProgressDialog = ProgressDialog.show(
                this, null, getString(messageId), true, false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Future<BitmapRegionDecoder> loadTask = mLoadTask;
        if (loadTask != null && !loadTask.isDone()) {
            // load in progress, try to cancel it
            loadTask.cancel();
            loadTask.waitDone();
            mProgressDialog.dismiss();
        }

        Future<Bitmap> loadBitmapTask = mLoadBitmapTask;
        if (loadBitmapTask != null && !loadBitmapTask.isDone()) {
            // load in progress, try to cancel it
            loadBitmapTask.cancel();
            loadBitmapTask.waitDone();
            mProgressDialog.dismiss();
        }

        Future<Bitmap> loadSecondBitmapTask = mLoadSecondBitmapTask;
        if (loadSecondBitmapTask != null && !loadSecondBitmapTask.isDone()) {
            // load in progress, try to cancel it
            loadSecondBitmapTask.cancel();
            loadSecondBitmapTask.waitDone();
        }

        Future<Intent> saveTask = mSaveTask;
        if (saveTask != null && !saveTask.isDone()) {
            // save in progress, try to cancel it
            saveTask.cancel();
            saveTask.waitDone();
            mProgressDialog.dismiss();
            //unbind 3d wallpaper service when the task cancels
            if (mSetStereoWallpaper) {
                doUnbindService();
            }
        }
        
        // avoid continuing to show progress dialog after the activity
        // became paused, which would lead a bad token exception
        // see ALPS00118643
        if ((mProgressDialog != null) && (mProgressDialog.isShowing())) {
            mProgressDialog.dismiss();
        }
        // ALPS00118643
        
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            mCropView.pause();
        } finally {
            root.unlockRenderThread();
        }
    }

    private MediaItem getMediaItemFromIntentData() {
        Uri uri = getIntent().getData();
        DataManager manager = getDataManager();
        if (uri == null) {
            Log.w(TAG, "no data given");
            return null;
        }
        Path path = manager.findPathByUri(uri);
        if (path == null) {
            Log.w(TAG, "cannot get path for: " + uri);
            return null;
        }
        //MediaItem item = (MediaItem) manager.getMediaObject(path);
        MediaItem item = null;
        try {
            item = (MediaItem) manager.getMediaObject(path);
        } catch (RuntimeException e) {
            Log.e(TAG, "cannot get item for path: " + path.toString());
            return null;
        }
        
        // Since it's very possible that crop activity's process stays in background once used,
        // the DataManager in this process might still be the old one and might be out-synced with
        // the DataManager in the main process, we update rotation info from DB
        // each time we open the file to crop.
        if (item != null && (item instanceof LocalImage)) {
            Cursor c = null;
            try {
                c = getContentResolver().query(uri, new String[] {Images.ImageColumns.ORIENTATION}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    ((LocalImage) item).rotation = c.getInt(0);
                }
            } catch (Exception e) {
                // in case any exception happens, we simply do not update the rotation info in item.
                Log.e(TAG, "Exception when trying to fetch orientation info", e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        
        return item;
    }

    private class LoadDataTask implements Job<BitmapRegionDecoder> {
        MediaItem mItem;

        public LoadDataTask(MediaItem item) {
            mItem = item;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            return mItem == null ? null : mItem.requestLargeImage().run(jc);
        }
    }

    private class LoadBitmapDataTask implements Job<Bitmap> {
        MediaItem mItem;

        public LoadBitmapDataTask(MediaItem item) {
            mItem = item;
        }
        public Bitmap run(JobContext jc) {
            //for picture quality enhancement, we don't want enhanced Bitmap
            //to be cropped and saved, causing double enhancement
            //Logic modified to support stereo wallpaper: as the actually
            //operations to set wall paper is done be 3D wallpaper app, we
            //only have to display the stereo image, so we don't have to
            //decode the origin image without picture quality enhancement.
            if (MediatekFeature.isPictureQualityEnhanceSupported() &&
                !mSetStereoWallpaper) {
                return mItem == null
                        ? null
                        : mItem.requestImageWithPostProc(false,
                                         MediaItem.TYPE_THUMBNAIL).run(jc);
            }
            return mItem == null
                    ? null
                    : mItem.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
        }
    }

    private class LoadSecondBitmapDataTask implements Job<Bitmap> {
        MediaItem mItem;

        public LoadSecondBitmapDataTask(MediaItem item) {
            mItem = item;
        }
        public Bitmap run(JobContext jc) {
            return mItem == null
                    ? null
                    : mItem.requestImage(MediaItem.TYPE_THUMBNAIL,
                                               null).run(jc).secondFrame;
        }
    }

    private static final String[] EXIF_TAGS = {
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_PROCESSING_METHOD};

    private static void copyExif(MediaItem item, String destination, int newWidth, int newHeight) {
        try {
            ExifInterface newExif = new ExifInterface(destination);
            PicasaSource.extractExifValues(item, newExif);
            newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(newWidth));
            newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(newHeight));
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(0));
            newExif.saveAttributes();
        } catch (Throwable t) {
            Log.w(TAG, "cannot copy exif: " + item, t);
        }
    }

    private static void copyExif(String source, String destination, int newWidth, int newHeight) {
        try {
            ExifInterface oldExif = new ExifInterface(source);
            ExifInterface newExif = new ExifInterface(destination);

            newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(newWidth));
            newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(newHeight));
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(0));

            for (String tag : EXIF_TAGS) {
                String value = oldExif.getAttribute(tag);
                if (value != null) {
                    newExif.setAttribute(tag, value);
                }
            }

            // Handle some special values here
            String value = oldExif.getAttribute(ExifInterface.TAG_APERTURE);
            if (value != null) {
                try {
                    float aperture = Float.parseFloat(value);
                    newExif.setAttribute(ExifInterface.TAG_APERTURE,
                            String.valueOf((int) (aperture * 10 + 0.5f)) + "/10");
                } catch (NumberFormatException e) {
                    Log.w(TAG, "cannot parse aperture: " + value);
                }
            }

            // TODO: The code is broken, need to fix the JHEAD lib
            /*
            value = oldExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            if (value != null) {
                try {
                    double exposure = Double.parseDouble(value);
                    testToRational("test exposure", exposure);
                    newExif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, value);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "cannot parse exposure time: " + value);
                }
            }

            value = oldExif.getAttribute(ExifInterface.TAG_ISO);
            if (value != null) {
                try {
                    int iso = Integer.parseInt(value);
                    newExif.setAttribute(ExifInterface.TAG_ISO, String.valueOf(iso) + "/1");
                } catch (NumberFormatException e) {
                    Log.w(TAG, "cannot parse exposure time: " + value);
                }
            }*/
            newExif.saveAttributes();
        } catch (Throwable t) {
            Log.w(TAG, "cannot copy exif: " + source, t);
        }
    }


    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_3D_WALLPAPER_RESULT_OK:
                    Log.v(TAG,"IncomingHandler:Received from service: OK");
                    doUnbindService();
                    if (null != mStereoWallpaperIntent) {
                        //send complete msg
                        Log.i(TAG,"IncomingHandler:send comlete mesage");
                        mMainHandler.sendMessage(mMainHandler.obtainMessage(
                                MSG_SAVE_COMPLETE, mStereoWallpaperIntent));
                        //reset stereo wallpaper intent
                        mStereoWallpaperIntent = null;
                    } else {
                        Log.e(TAG,"IncomingHandler:send ERROR mesage");
                        mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    }
                    break;
                case MSG_3D_WALLPAPER_RESULT_ERROR:
                    Log.v(TAG,"IncomingHandler:Received from service: ERROR");
                    doUnbindService();
                    Log.e(TAG,"IncomingHandler:send ERROR mesage");
                    mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    //reset stereo wallpaper intent
                    mStereoWallpaperIntent = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, MSG_SET_3D_WALLPAPER);
                msg.replyTo = mMessenger;
                msg.obj = mStereoWallpaperUri;
                Log.i(TAG,"onServiceConnected:msg.obj="+mStereoWallpaperUri);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.w(TAG,"onServiceConnected:got "+e);
                return;
            }
            
            Log.d(TAG,"onServiceConnected:service connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG,"onServiceDisconnected(className="+className+")");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            Log.v(TAG,"onServiceConnected:service disconnected");
        }
    };
    
    boolean doBindService() {
        Log.i(TAG,"doBindService()");
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        mIsBounded = bindService(new Intent(SET_3D_WALLPAPER), mConnection,
                                    Context.BIND_AUTO_CREATE);
        Log.d(TAG,"doBindService:mIsBounded="+mIsBounded);
        return mIsBounded;
    }
    
    void doUnbindService() {
        Log.i(TAG,"doUnbindService()");
        if (mIsBounded) {
            // Detach our existing connection.
            Log.d(TAG,"doUnbindService:call unbindService()");
            // As CropImage activity may have already stopped, 
            // we temporarily add protect. Need further check!
            try {
                unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "doUnbindService:got exception when unbind..");
                e.printStackTrace();
            }
            mIsBounded = false;
        }
    }

}
