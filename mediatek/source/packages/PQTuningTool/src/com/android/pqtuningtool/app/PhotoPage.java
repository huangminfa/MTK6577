/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

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

package  com.android.pqtuningtool.app;

import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.data.DataManager;
import  com.android.pqtuningtool.data.MediaDetails;
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.MediaObject;
import  com.android.pqtuningtool.data.MediaSet;
import  com.android.pqtuningtool.data.MtpDevice;
import  com.android.pqtuningtool.data.Path;
import  com.android.pqtuningtool.picasasource.PicasaSource;
import  com.android.pqtuningtool.ui.DetailsHelper;
import  com.android.pqtuningtool.ui.DetailsHelper.CloseListener;
import  com.android.pqtuningtool.ui.DetailsHelper.DetailsSource;
import  com.android.pqtuningtool.ui.FilmStripView;
import  com.android.pqtuningtool.ui.GLCanvas;
import  com.android.pqtuningtool.ui.GLView;
import  com.android.pqtuningtool.ui.ImportCompleteListener;
import  com.android.pqtuningtool.ui.MenuExecutor;
import  com.android.pqtuningtool.ui.PhotoView;
import  com.android.pqtuningtool.ui.PositionRepository;
import  com.android.pqtuningtool.ui.PositionRepository.Position;
import  com.android.pqtuningtool.ui.SelectionManager;
import  com.android.pqtuningtool.ui.SynchronizedHandler;
import  com.android.pqtuningtool.ui.UserInteractionListener;
import  com.android.pqtuningtool.util.GalleryUtils;

import android.bluetooth.BluetoothAdapter;
import  com.android.pqtuningtool.data.LocalMediaItem;
import  com.android.pqtuningtool.data.LocalImage;
import  com.android.pqtuningtool.data.LocalVideo;
import  com.android.pqtuningtool.util.MediatekFeature;
import  com.android.pqtuningtool.util.DrmHelper;
import  com.android.pqtuningtool.util.MpoHelper;
import  com.android.pqtuningtool.util.MtkLog;
import com.android.pqtuningtool.util.PictureQualityOptions;

import android.drm.DrmStore;
import android.drm.DrmManagerClient;
import android.content.DialogInterface;

public class PhotoPage extends ActivityState
        implements PhotoView.PhotoTapListener, FilmStripView.Listener,
        UserInteractionListener {
    private static final String TAG = "PhotoPage";
    private static final int REQUEST_PQ = 4;

    private static final int MSG_HIDE_BARS = 1;

    private static final int HIDE_BARS_TIMEOUT = 3500;

    private static final int REQUEST_SLIDESHOW = 1;
    private static final int REQUEST_CROP = 2;
    private static final int REQUEST_CROP_PICASA = 3;

    public static final String KEY_MEDIA_SET_PATH = "media-set-path";
    public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
    public static final String KEY_INDEX_HINT = "index-hint";

    //added to support Mediatek features
    private static final boolean mIsDrmSupported =
                                          MediatekFeature.isDrmSupported();
    private static int mDrmMicroThumbDim;
    private int mDrmInclusion = 0;

    private GalleryApp mApplication;
    private SelectionManager mSelectionManager;

    private PhotoView mPhotoView;
    private PhotoPage.Model mModel;
    private FilmStripView mFilmStripView;
    private DetailsHelper mDetailsHelper;
    private boolean mShowDetails;
    private Path mPendingSharePath;

    // mMediaSet could be null if there is no KEY_MEDIA_SET_PATH supplied.
    // E.g., viewing a photo in gmail attachment
    private MediaSet mMediaSet;
    private Menu mMenu;

    private final Intent mResultIntent = new Intent();
    private int mCurrentIndex = 0;
    private Handler mHandler;
    private boolean mShowBars = true;
    private ActionBar mActionBar;
    private MyMenuVisibilityListener mMenuVisibilityListener;
    private boolean mIsMenuVisible;
    private boolean mIsInteracting;
    private MediaItem mCurrentPhoto = null;
    private MenuExecutor mMenuExecutor;
    private boolean mIsActive;
    private ShareActionProvider mShareActionProvider;

    public static interface Model extends PhotoView.Model {
        public void resume();
        public void pause();
        public boolean isEmpty();
        public MediaItem getCurrentMediaItem();
        public int getCurrentIndex();
        public void setCurrentPhoto(Path path, int indexHint);
        //added to support DRM rights consumption
        public void enterConsumeMode();
        public boolean enteredConsumeMode();
    }

    private class MyMenuVisibilityListener implements OnMenuVisibilityListener {
        public void onMenuVisibilityChanged(boolean isVisible) {
            mIsMenuVisible = isVisible;
            refreshHidingMessage();
        }
    }

    private final GLView mRootPane = new GLView() {

        @Override
        protected void renderBackground(GLCanvas view) {
            view.clearBuffer();
        }

        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            mPhotoView.layout(0, 0, right - left, bottom - top);
            PositionRepository.getInstance(mActivity).setOffset(0, 0);
            int filmStripHeight = 0;
            if (mFilmStripView != null) {
                mFilmStripView.measure(
                        MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY),
                        MeasureSpec.UNSPECIFIED);
                filmStripHeight = mFilmStripView.getMeasuredHeight();
                mFilmStripView.layout(0, bottom - top - filmStripHeight,
                        right - left, bottom - top);
            }
            if (mShowDetails) {
                mDetailsHelper.layout(left, GalleryActionBar.getHeight((Activity) mActivity),
                        right, bottom);
            }
        }
    };

    private void initFilmStripView() {
        Config.PhotoPage config = Config.PhotoPage.get((Context) mActivity);
        mFilmStripView = new FilmStripView(mActivity, mMediaSet,
                config.filmstripTopMargin, config.filmstripMidMargin, config.filmstripBottomMargin,
                config.filmstripContentSize, config.filmstripThumbSize, config.filmstripBarSize,
                config.filmstripGripSize, config.filmstripGripWidth);
        mRootPane.addComponent(mFilmStripView);
        mFilmStripView.setListener(this);
        mFilmStripView.setUserInteractionListener(this);
        mFilmStripView.setFocusIndex(mCurrentIndex);
        mFilmStripView.setStartIndex(mCurrentIndex);
        mRootPane.requestLayout();
        if (mIsActive) mFilmStripView.resume();
        if (!mShowBars) mFilmStripView.setVisibility(GLView.INVISIBLE);
    }

    @Override
    public void onCreate(Bundle data, Bundle restoreState) {
        mActionBar = ((Activity) mActivity).getActionBar();
        mSelectionManager = new SelectionManager(mActivity, false);
        mMenuExecutor = new MenuExecutor(mActivity, mSelectionManager);

        mPhotoView = new PhotoView(mActivity);
        mPhotoView.setPhotoTapListener(this);
        mRootPane.addComponent(mPhotoView);
        mApplication = (GalleryApp)((Activity) mActivity).getApplication();

        mDrmMicroThumbDim = DrmHelper.getDrmMicroThumbDim((Activity) mActivity);

        String setPathString = data.getString(KEY_MEDIA_SET_PATH);
        Path itemPath = Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH));

        if (setPathString != null) {
            if (mIsDrmSupported) {
                mDrmInclusion = DrmHelper.getDrmInclusionFromData(data);
                mMediaSet = mActivity.getDataManager().getMediaSet(setPathString,
                                                                  mDrmInclusion);
                mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
                mMediaSet = (MediaSet)
                        mActivity.getDataManager().getMediaObject(setPathString,
                                                                  mDrmInclusion);
            } else {
                mMediaSet = mActivity.getDataManager().getMediaSet(setPathString);
                mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
                mMediaSet = (MediaSet)
                        mActivity.getDataManager().getMediaObject(setPathString);
            }

            if (mMediaSet == null) {
                Log.w(TAG, "failed to restore " + setPathString);
            }
            PhotoDataAdapter pda = new PhotoDataAdapter(
                    mActivity, mPhotoView, mMediaSet, itemPath, mCurrentIndex);
            mModel = pda;
            mPhotoView.setModel(mModel);

            mResultIntent.putExtra(KEY_INDEX_HINT, mCurrentIndex);
            setStateResult(Activity.RESULT_OK, mResultIntent);

            pda.setDataListener(new PhotoDataAdapter.DataListener() {

                @Override
                public void onPhotoChanged(int index, Path item) {
                    if (mFilmStripView != null) mFilmStripView.setFocusIndex(index);
                    mCurrentIndex = index;
                    mResultIntent.putExtra(KEY_INDEX_HINT, index);
                    if (item != null) {
                        mResultIntent.putExtra(KEY_MEDIA_ITEM_PATH, item.toString());
                        MediaItem photo = mModel.getCurrentMediaItem();
                        if (photo != null) updateCurrentPhoto(photo);
                    } else {
                        mResultIntent.removeExtra(KEY_MEDIA_ITEM_PATH);
                    }
                    setStateResult(Activity.RESULT_OK, mResultIntent);
                }

                @Override
                public void onLoadingFinished() {
                    GalleryUtils.setSpinnerVisibility((Activity) mActivity, false);
                    if (!mModel.isEmpty()) {
                        mCurrentIndex = mModel.getCurrentIndex();
                        if (mFilmStripView != null) {
                            mFilmStripView.setFocusIndex(mModel.getCurrentIndex());
                        }
                        MediaItem photo = mModel.getCurrentMediaItem();
                        MtkLog.i(TAG, "onLoadingFinished : updateCurrentPhoto");
                        if (photo != null) updateCurrentPhoto(photo);
                        //added to start consume drm dialog, if needed
                        if (mIsDrmSupported && photo instanceof LocalMediaItem) {
                            tryConsumeDrmRights((LocalMediaItem)photo);
                        }
                    } else if (mIsActive) {
                        mActivity.getStateManager().finishState(PhotoPage.this);
                    }
                }

                @Override
                public void onLoadingStarted() {
                    GalleryUtils.setSpinnerVisibility((Activity) mActivity, true);
                }

                @Override
                public void onPhotoAvailable(long version, boolean fullImage) {
                    if (mFilmStripView == null) initFilmStripView();
                }
            });
        } else {
            // Get default media set by the URI
            MediaItem mediaItem = (MediaItem)
                    mActivity.getDataManager().getMediaObject(itemPath);
            mModel = new SinglePhotoDataAdapter(mActivity, mPhotoView, mediaItem);
            mPhotoView.setModel(mModel);
            updateCurrentPhoto(mediaItem);
            MtkLog.i(TAG, "oncreate : updateCurrentPhoto");
        }

        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIDE_BARS: {
                        hideBars();
                        break;
                    }
                    default: throw new AssertionError(message.what);
                }
            }
        };

        // start the opening animation
        mPhotoView.setOpenedItem(itemPath);
    }

    private void updateShareURI(Path path) {
        if (mShareActionProvider != null) {
            DataManager manager = mActivity.getDataManager();
            int type = manager.getMediaType(path);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(MenuExecutor.getMimeType(type));
            intent.putExtra(Intent.EXTRA_STREAM, manager.getContentUri(path));
            mShareActionProvider.setShareIntent(intent);
            mPendingSharePath = null;
        } else {
            // This happens when ActionBar is not created yet.
            mPendingSharePath = path;
        }
    }

    private void setTitle(String title) {
        if (title == null) return;
        boolean showTitle = mActivity.getAndroidContext().getResources().getBoolean(
                R.bool.show_action_bar_title);
        MtkLog.i(TAG, "showTitle"  +  showTitle);
        if (showTitle)
            mActionBar.setTitle(title);
        else
            mActionBar.setTitle("");
    }

    private void updateCurrentPhoto(MediaItem photo) {
        MtkLog.i(TAG, "updateCurrentPhoto start");
        if (mCurrentPhoto == photo) {
            if (photo != null) {
                updateShareURI(photo.getPath());
                MtkLog.i(TAG, "mCurrentPhoto == photo return");
            }
            return;
        }
        mCurrentPhoto = photo;
        if (mCurrentPhoto == null) return;
        MtkLog.i(TAG, "updateCurrentPhoto middle");
        updateMenuOperations();
        if (mShowDetails) {
            mDetailsHelper.reloadDetails(mModel.getCurrentIndex());
        }
        setTitle(photo.getName());
        MtkLog.i(TAG, "updateCurrentPhoto---setTitle(photo.getName());---photo Name  " + photo.getName());
        mPhotoView.showVideoPlayIcon(
                photo.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO);

        if (MediatekFeature.isMpoSupported()) {
            //show mav icon
            mPhotoView.showMpoViewIcon(
                (photo.getSubType() & MediaObject.SUBTYPE_MAV) != 0);
        }

        updateShareURI(photo.getPath());
    }

    private void updateMenuOperations() {
        if (mMenu == null) return;
        MenuItem item = mMenu.findItem(R.id.action_slideshow);
        if (item != null) {
            item.setVisible(canDoSlideShow());
        }
        if (mCurrentPhoto == null) return;
        int supportedOperations = mCurrentPhoto.getSupportedOperations();
        if (!GalleryUtils.isEditorAvailable((Context) mActivity, "image/*")) {
            supportedOperations &= ~MediaObject.SUPPORT_EDIT;
        }
        MenuExecutor.updateMenuOperation(mMenu, supportedOperations);
    }

    private boolean canDoSlideShow() {
        if (mMediaSet == null || mCurrentPhoto == null) {
            return false;
        }
        if (mCurrentPhoto.getMediaType() != MediaObject.MEDIA_TYPE_IMAGE) {
            return false;
        }
        if (mMediaSet instanceof MtpDevice) {
            return false;
        }
        return true;
    }

    private void showBars() {
        if (mShowBars) return;
        mShowBars = true;
        mActionBar.show();
        WindowManager.LayoutParams params = ((Activity) mActivity).getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
        ((Activity) mActivity).getWindow().setAttributes(params);
        if (mFilmStripView != null) {
            mFilmStripView.show();
        }
    }

    private void hideBars() {
        if (!mShowBars) return;
        mShowBars = false;
        mActionBar.hide();
        WindowManager.LayoutParams params = ((Activity) mActivity).getWindow().getAttributes();
        params.systemUiVisibility = View. SYSTEM_UI_FLAG_LOW_PROFILE;
        ((Activity) mActivity).getWindow().setAttributes(params);
        if (mFilmStripView != null) {
            mFilmStripView.hide();
        }
    }

    private void refreshHidingMessage() {
        mHandler.removeMessages(MSG_HIDE_BARS);
        if (!mIsMenuVisible && !mIsInteracting) {
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_BARS, HIDE_BARS_TIMEOUT);
        }
    }

    @Override
    public void onUserInteraction() {
        showBars();
        refreshHidingMessage();
    }

    public void onUserInteractionTap() {
        if (mShowBars) {
            hideBars();
            mHandler.removeMessages(MSG_HIDE_BARS);
        } else {
            showBars();
            refreshHidingMessage();
        }
    }

    @Override
    public void onUserInteractionBegin() {
        showBars();
        mIsInteracting = true;
        refreshHidingMessage();
    }

    @Override
    public void onUserInteractionEnd() {
        mIsInteracting = false;

        // This function could be called from GL thread (in SlotView.render)
        // and post to the main thread. So, it could be executed while the
        // activity is paused.
        if (mIsActive) refreshHidingMessage();
    }

    @Override
    protected void onBackPressed() {
        if (mShowDetails) {
            hideDetails();
        } else {
            PositionRepository repository = PositionRepository.getInstance(mActivity);
            repository.clear();
            if (mCurrentPhoto != null) {
                Position position = new Position();
                position.x = mRootPane.getWidth() / 2;
                position.y = mRootPane.getHeight() / 2;
                position.z = -1000;
                repository.putPosition(
                        Long.valueOf(System.identityHashCode(mCurrentPhoto.getPath())),
                        position);
            }
            super.onBackPressed();
        }
    }

    @Override
    protected boolean onCreateActionBar(Menu menu) {
        MenuInflater inflater = ((Activity) mActivity).getMenuInflater();
        inflater.inflate(R.menu.photo, menu);
        mShareActionProvider = GalleryActionBar.initializeShareActionProvider(menu);
        if (mPendingSharePath != null) updateShareURI(mPendingSharePath);
        mMenu = menu;
        mShowBars = true;
        updateMenuOperations();
        return true;
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        MediaItem current = mModel.getCurrentMediaItem();

        if (current == null) {
            // item is not ready, ignore
            return true;
        }

        int currentIndex = mModel.getCurrentIndex();
        Path path = current.getPath();

        DataManager manager = mActivity.getDataManager();
        int action = item.getItemId();
        switch (action) {
            case R.id.action_slideshow: {
                Bundle data = new Bundle();
                data.putString(SlideshowPage.KEY_SET_PATH, mMediaSet.getPath().toString());
                data.putString(SlideshowPage.KEY_ITEM_PATH, path.toString());
                data.putInt(SlideshowPage.KEY_PHOTO_INDEX, currentIndex);
                data.putBoolean(SlideshowPage.KEY_REPEAT, true);
                mActivity.getStateManager().startStateForResult(
                        SlideshowPage.class, REQUEST_SLIDESHOW, data);
                return true;
            }
            case R.id.action_crop: {
                Activity activity = (Activity) mActivity;
                Intent intent = new Intent(CropImage.CROP_ACTION);
                intent.setClass(activity, CropImage.class);
                intent.setData(manager.getContentUri(path));
                activity.startActivityForResult(intent, PicasaSource.isPicasaImage(current)
                        ? REQUEST_CROP_PICASA
                        : REQUEST_CROP);
                return true;
            }
            case R.id.action_picture_quality: {
                Activity activity = (Activity) mActivity;
                Intent intent = new Intent(PictureQualityTool.ACTION_PQ);
                intent.setClass(activity, PictureQualityTool.class);
                intent.setData(manager.getContentUri(path));
                Bundle pqBundle = new Bundle();
                pqBundle.putString("PQUri", manager.getContentUri(path).toString());
                intent.putExtras(pqBundle);
                MtkLog.i(TAG, "startActivity PQ");
                activity.startActivityForResult(intent, REQUEST_PQ);
                return true;
            }
            case R.id.action_print: {
                //add for Bluetooth Print
                Activity activity = (Activity) mActivity;
                String mimeType;
                Log.v(TAG, "Print for " + path);
                int type = manager.getMediaType(path);
                if(type != MediaObject.MEDIA_TYPE_IMAGE)
                    return false;
                else
                    mimeType = "image/*";
                Intent intent = new Intent();
                intent.setAction("mediatek.intent.action.PRINT");
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                intent.setType(mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, manager.getContentUri(path));
                try {
                    activity.startActivity(Intent.createChooser(intent,
                                           activity.getText(R.string.printFile)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, R.string.no_way_to_print,
                                   Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.action_protect_info: {
                //add for drm protection info
                if (!mIsDrmSupported) return true;
                final DrmManagerClient drmManagerClient =
                                 DrmHelper.getDrmManagerClient((Context)mActivity);
                if (null != drmManagerClient) {
                    drmManagerClient.showProtectionInfoDialog((Activity)mActivity,
                                              manager.getContentUri(path));
                } else {
                    Log.e(TAG,"onItemSelected:get drm manager client failed!");
                }
                return true;
            }
            case R.id.action_details: {
                if (mShowDetails) {
                    hideDetails();
                } else {
                    showDetails(currentIndex);
                }
                return true;
            }
            case R.id.action_setas:
            case R.id.action_confirm_delete:
            case R.id.action_rotate_ccw:
            case R.id.action_rotate_cw:
            case R.id.action_show_on_map:
            case R.id.action_edit:
                mSelectionManager.deSelectAll();
                mSelectionManager.toggle(path);
                mMenuExecutor.onMenuClicked(item, null);
                return true;
            case R.id.action_import:
                mSelectionManager.deSelectAll();
                mSelectionManager.toggle(path);
                mMenuExecutor.onMenuClicked(item,
                        new ImportCompleteListener(mActivity));
                return true;
            default :
                return false;
        }
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
    }

    private void showDetails(int index) {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mActivity, mRootPane, new MyDetailsSource());
            mDetailsHelper.setCloseListener(new CloseListener() {
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.reloadDetails(index);
        mDetailsHelper.show();
    }

    public void onSingleTapUp(int x, int y) {
        MediaItem item = mModel.getCurrentMediaItem();
        if (item == null) {
            // item is not ready, ignore
            return;
        }

        boolean playVideo =
                (item.getSupportedOperations() & MediaItem.SUPPORT_PLAY) != 0;

        if (playVideo) {
            // determine if the point is at center (1/6) of the photo view.
            // (The position of the "play" icon is at center (1/6) of the photo)
            int w = mPhotoView.getWidth();
            int h = mPhotoView.getHeight();
            playVideo = (Math.abs(x - w / 2) * 12 <= w)
                && (Math.abs(y - h / 2) * 12 <= h);
        }

        if (playVideo) {
            if (MediatekFeature.isMpoSupported() &&
                (item.getSubType() & MediaObject.SUBTYPE_MAV) != 0) {
                MpoHelper.playMpo((Activity) mActivity, item.getContentUri());
                return;
            }
            playVideo((Activity) mActivity, item.getPlayUri(), item.getName());
        } else {
            if (mIsDrmSupported) {
                boolean consume = false;
                // determine if the point is at drm micro thumb of the photo view.
                int w = mPhotoView.getWidth();
                int h = mPhotoView.getHeight();
                consume = (Math.abs(x - w / 2) * 2 <= mDrmMicroThumbDim)
                       && (Math.abs(y - h / 2) * 2 <= mDrmMicroThumbDim);
                if (consume && item instanceof LocalMediaItem &&
                    tryConsumeDrmRights((LocalMediaItem)item)) {
                    return;
                }
            }
            onUserInteractionTap();
        }
    }

    public static void playVideo(Activity activity, Uri uri, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "video/*");
            intent.putExtra(Intent.EXTRA_TITLE, title);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, activity.getString(R.string.video_err),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean tryConsumeDrmRights(LocalMediaItem item) {
        if (item instanceof LocalVideo) {
            return false;
        }
        if (mIsDrmSupported && !mModel.enteredConsumeMode() &&
            (item.getSupportedOperations() & MediaItem.SUPPORT_CONSUME_DRM) != 0) {
            showDrmDialog((Context) mActivity, (LocalMediaItem)item);
            return true;
        }
        return false;
    }

    public void showDrmDialog(Context context, LocalMediaItem item) {
        if (!mIsDrmSupported || !item.isDrm()) {
            Log.w(TAG, "showDrmDialog() is call for non-drm media!");
            return;
        }
        if (item instanceof LocalVideo) {
            Log.v(TAG, "showDrmDialog:encoutered LocalVideo, ignor");
            return;
        }

        final LocalImage imageItem = (LocalImage)item;

        int rights = DrmHelper.checkRightsStatus(context, imageItem.filePath,
                                       DrmStore.Action.DISPLAY);
        final DrmManagerClient drmManagerClient =
                                    DrmHelper.getDrmManagerClient(context);
        if (DrmStore.RightsStatus.RIGHTS_VALID == rights){
            drmManagerClient.showConsumeDialog(context,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        if (DialogInterface.BUTTON_POSITIVE == which) {
                            drmManagerClient.consume(imageItem.filePath,
                                             DrmStore.Action.DISPLAY);
                            //consume
                            mModel.enterConsumeMode();
                            //hide bar
                            hideBars();
                        }
                        dialog.dismiss();
                    }
                },
                new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {}
                }
            );
        } else {
            if (DrmStore.RightsStatus.SECURE_TIMER_INVALID == rights) {
                drmManagerClient.showSecureTimerInvalidDialog(context,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.dismiss();
                        }
                    },
                    new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {}
                    }
                );
            } else {
                 drmManagerClient.showLicenseAcquisitionDialog(
                                            context, imageItem.filePath);
            }
        }
    }

    // Called by FileStripView.
    // Returns false if it cannot jump to the specified index at this time.
    public boolean onSlotSelected(int slotIndex) {
        return mPhotoView.jumpTo(slotIndex);
    }

    @Override
    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) break;
                    Path path = mApplication
                            .getDataManager().findPathByUri(data.getData());
                    if (path != null) {
                        mModel.setCurrentPhoto(path, mCurrentIndex);
                    }
                }
                break;
            case REQUEST_CROP_PICASA: {
                int message = resultCode == Activity.RESULT_OK
                        ? R.string.crop_saved
                        : R.string.crop_not_saved;
                Toast.makeText(mActivity.getAndroidContext(),
                        message, Toast.LENGTH_SHORT).show();
                break;
            }
            case REQUEST_PQ: {
                if (data == null) break;
                Bundle bundle = data.getExtras();
                if (bundle == null) break;
                int skyToneOption = bundle.getInt("skyTone");
                int skinToneOption = bundle.getInt("skinTone");
                int grassToneOption = bundle.getInt("grassTone");
                int colorOption = bundle.getInt("color");
                int sharpnessOption = bundle.getInt("sharpness");
                PictureQualityOptions.setSkyToneOption(skyToneOption);
                PictureQualityOptions.setSkinToneOptin(skinToneOption);
                PictureQualityOptions.setGrassToneOptin(grassToneOption);
                PictureQualityOptions.setColorOptin(colorOption);
                PictureQualityOptions.setSharpnessOptin(sharpnessOption);
                break;
            }
            case REQUEST_SLIDESHOW: {
                if (data == null) break;
                String path = data.getStringExtra(SlideshowPage.KEY_ITEM_PATH);
                int index = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
                if (path != null) {
                    mModel.setCurrentPhoto(Path.fromString(path), index);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        if (mFilmStripView != null) {
            mFilmStripView.pause();
        }
        DetailsHelper.pause();
        mPhotoView.pause();
        mModel.pause();
        mHandler.removeMessages(MSG_HIDE_BARS);
        mActionBar.removeOnMenuVisibilityListener(mMenuVisibilityListener);
        mMenuExecutor.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActive = true;
        setContentPane(mRootPane);
        mModel.resume();
        mPhotoView.resume();
        if (mFilmStripView != null) {
            mFilmStripView.resume();
        }
        if (mMenuVisibilityListener == null) {
            mMenuVisibilityListener = new MyMenuVisibilityListener();
        }
        mActionBar.addOnMenuVisibilityListener(mMenuVisibilityListener);
        onUserInteraction();
    }

    private class MyDetailsSource implements DetailsSource {
        private int mIndex;

        @Override
        public MediaDetails getDetails() {
            return mModel.getCurrentMediaItem().getDetails();
        }

        @Override
        public int size() {
            return mMediaSet != null ? mMediaSet.getMediaItemCount() : 1;
        }

        @Override
        public int findIndex(int indexHint) {
            mIndex = indexHint;
            return indexHint;
        }

        @Override
        public int getIndex() {
            return mIndex;
        }
    }
}
