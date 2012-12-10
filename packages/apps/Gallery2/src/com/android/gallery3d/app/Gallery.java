/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.io.File;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryUtils;

import android.provider.MediaStore.Images.Media;
import android.database.Cursor;
import com.android.gallery3d.util.DrmHelper;
import com.android.gallery3d.util.StereoHelper;
import com.android.gallery3d.util.MediatekFeature;

public final class Gallery extends AbstractGalleryActivity implements OnCancelListener {
    public static final String EXTRA_SLIDESHOW = "slideshow";
    public static final String EXTRA_CROP = "crop";

    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
    public static final String KEY_GET_CONTENT = "get-content";
    public static final String KEY_GET_ALBUM = "get-album";
    public static final String KEY_TYPE_BITS = "type-bits";
    public static final String KEY_MEDIA_TYPES = "mediaTypes";

    private static final String TAG = "Gallery";

    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();

    private GalleryActionBar mActionBar;
    private Dialog mVersionCheckDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.main);
        mActionBar = new GalleryActionBar(this);

        if (savedInstanceState != null) {
            getStateManager().restoreFromState(savedInstanceState);
        } else {
            initializeByIntent();
        }
    }

    private void initializeByIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
            startGetContent(intent);
        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            // We do NOT really support the PICK intent. Handle it as
            // the GET_CONTENT. However, we need to translate the type
            // in the intent here.
            Log.w(TAG, "action PICK is not supported");
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image")) intent.setType("image/*");
                if (type.endsWith("/video")) intent.setType("video/*");
            }
            startGetContent(intent);
        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(action)
                || ACTION_REVIEW.equalsIgnoreCase(action)){
            startViewAction(intent);
        } else {
            startDefaultPage();
        }
    }

    public void startDefaultPage() {
        PicasaSource.showSignInReminder(this);
        Bundle data = new Bundle();
        data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                getDataManager().getTopSetPath(DataManager.INCLUDE_ALL));

        //add for DRM feature
        if (MediatekFeature.isDrmSupported()) {
            //when start default page, we query all drm media, any risk???
            Log.d(TAG,"startDefaultPage:we query all drm media");
            data.putInt(android.drm.DrmStore.DrmExtra.EXTRA_DRM_LEVEL,
                        android.drm.DrmStore.DrmExtra.DRM_LEVEL_ALL);
        }

        if (MediatekFeature.isStereoDisplaySupported()) {
            if (null != getIntent().getExtras()) {
                data.putBoolean(StereoHelper.STEREO_EXTRA,
                      getIntent().getExtras().getBoolean(
                                                  StereoHelper.STEREO_EXTRA,false));
            }
        }
        getStateManager().startState(AlbumSetPage.class, data);
        mVersionCheckDialog = PicasaSource.getVersionCheckDialog(this);
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.setOnCancelListener(this);
        }
    }

    private void startGetContent(Intent intent) {
        Bundle data = intent.getExtras() != null
                ? new Bundle(intent.getExtras())
                : new Bundle();
        data.putBoolean(KEY_GET_CONTENT, true);
        int typeBits = GalleryUtils.determineTypeBits(this, intent);
        data.putInt(KEY_TYPE_BITS, typeBits);
        data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                getDataManager().getTopSetPath(typeBits));
        getStateManager().setLaunchGalleryOnTop(true);
        getStateManager().startState(AlbumSetPage.class, data);
    }

    private String getContentType(Intent intent) {
        String type = intent.getType();
        if (type != null) return type;

        Uri uri = intent.getData();
        try {
            return getContentResolver().getType(uri);
        } catch (Throwable t) {
            Log.w(TAG, "get type fail", t);
            return null;
        }
    }

    private void startViewAction(Intent intent) {
        Boolean slideshow = intent.getBooleanExtra(EXTRA_SLIDESHOW, false);
        getStateManager().setLaunchGalleryOnTop(true);
        if (slideshow) {
            getActionBar().hide();
            DataManager manager = getDataManager();
            Path path = manager.findPathByUri(intent.getData());
            if (path == null || manager.getMediaObject(path)
                    instanceof MediaItem) {
                path = Path.fromString(
                        manager.getTopSetPath(DataManager.INCLUDE_IMAGE));
            }
            String setpath = path != null? path.toString() : null;
            if (null != intent.getStringExtra(SlideshowPage.KEY_SET_PATH)) {
                setpath = intent.getStringExtra(SlideshowPage.KEY_SET_PATH);
            }
            Bundle data = new Bundle();
            data.putString(SlideshowPage.KEY_SET_PATH, setpath);
            data.putBoolean(SlideshowPage.KEY_RANDOM_ORDER, true);
            data.putBoolean(SlideshowPage.KEY_REPEAT, true);
            getStateManager().startState(SlideshowPage.class, data);
        } else {
            Bundle data = new Bundle();
            DataManager dm = getDataManager();
            Uri uri = intent.getData();
            String contentType = getContentType(intent);
            if (contentType == null) {
                Toast.makeText(this,
                        R.string.no_such_item, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            if (uri == null) {
                int typeBits = GalleryUtils.determineTypeBits(this, intent);
                data.putInt(KEY_TYPE_BITS, typeBits);
                data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                        getDataManager().getTopSetPath(typeBits));
                getStateManager().setLaunchGalleryOnTop(true);
                getStateManager().startState(AlbumSetPage.class, data);
            } else if (contentType.startsWith(
                    ContentResolver.CURSOR_DIR_BASE_TYPE)) {
                int mediaType = intent.getIntExtra(KEY_MEDIA_TYPES, 0);
                if (mediaType != 0) {
                    uri = uri.buildUpon().appendQueryParameter(
                            KEY_MEDIA_TYPES, String.valueOf(mediaType))
                            .build();
                }
                Path setPath = dm.findPathByUri(uri);
                MediaSet mediaSet = null;
                if (setPath != null) {
                    mediaSet = (MediaSet) dm.getMediaObject(setPath);
                }
                if (mediaSet != null) {
                    if (mediaSet.isLeafAlbum()) {
                        data.putString(AlbumPage.KEY_MEDIA_PATH, setPath.toString());
                        getStateManager().startState(AlbumPage.class, data);
                    } else {
                        data.putString(AlbumSetPage.KEY_MEDIA_PATH, setPath.toString());
                        getStateManager().startState(AlbumSetPage.class, data);
                    }
                } else {
                    startDefaultPage();
                }
            } else {
                //change file:///mnt/sdcard... type uri to context://media/external...
                //if possible.
                uri = tryContextMediaUri(uri);
                //add fro DRM feature
                if (mIsDrmSupported || mIsStereoDisplaySupported) {
                    //when start default page, we query all drm media, any risk???
                    Log.d(TAG,"startViewAction:we query all drm media");
                    data.putInt(android.drm.DrmStore.DrmExtra.EXTRA_DRM_LEVEL,
                                android.drm.DrmStore.DrmExtra.DRM_LEVEL_ALL);
                    //add for DRM feature: pass drm inclusio info to ActivityState
                    //int mtkInclusion = DrmHelper.getDrmInclusionFromData(data);
                    int mtkInclusion = MediatekFeature.getInclusionFromData(data);
                    data.putInt(DrmHelper.DRM_INCLUSION, mtkInclusion);
                }

                Path itemPath = dm.findPathByUri(uri);

                if (itemPath == null) {
                    Toast.makeText(this,
                            R.string.no_such_item, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Path albumPath = null;
                try {
                    albumPath = dm.getDefaultSetOf(itemPath);
                } catch (RuntimeException e) {
                    Log.e(TAG,"got RuntimeException "+e);
                    Log.e(TAG,"can not create proper album path object!");
                    Toast.makeText(this,
                            R.string.no_such_item, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // TODO: Make this parameter public so other activities can reference it.
                boolean singleItemOnly = intent.getBooleanExtra("SingleItemOnly", false);
                if (!singleItemOnly && albumPath != null) {
                    data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
                            albumPath.toString());
                }
                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, itemPath.toString());
                getStateManager().startState(PhotoPage.class, data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return getStateManager().createOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return getStateManager().prepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            return getStateManager().itemSelected(item);
        } finally {
            root.unlockRenderThread();
        }
    }

    @Override
    public void onBackPressed() {
        // send the back event to the top sub-state
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            getStateManager().onBackPressed();
        } finally {
            root.unlockRenderThread();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            getStateManager().destroy();
        } finally {
            root.unlockRenderThread();
        }
    }

    @Override
    protected void onResume() {
        Utils.assertTrue(getStateManager().getStateCount() > 0);
        super.onResume();
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.dismiss();
        }
    }

    @Override
    public GalleryActionBar getGalleryActionBar() {
        return mActionBar;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialog == mVersionCheckDialog) {
            mVersionCheckDialog = null;
        }
    }

    private Uri tryContextMediaUri(Uri uri) {
        if (null == uri) return null;
        
        String scheme = uri.getScheme();
        if (!ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri;
        } else {
            //ALPS00258426
            //description:Black screen is shown when opening the downloaded 
            // picture in notification bar
            //solution:
            //when opening a deleted image by gallery2,we first check whether
            // the image file exist, if not, return null
            String path = uri.getPath();
            if(!new File(path).exists()) {
                return null;
            }
        }

        Cursor cursor = null;
        try {
            //for file kinds of uri, query media database
            cursor = Media.query(
                    getContentResolver(), Media.getContentUri("external"), 
                    new String[] {Media._ID, Media.BUCKET_ID},
                    "_data=(?)", new String[] {uri.getPath()},
                    null);// " bucket_id ASC, _id ASC");
            if (null != cursor && cursor.moveToNext()) {
                long id = cursor.getLong(0);
                final String imagesUri = Media.getContentUri("external").toString();
                uri = Uri.parse(imagesUri + "/"+ id);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }

        return uri;
    }
}
