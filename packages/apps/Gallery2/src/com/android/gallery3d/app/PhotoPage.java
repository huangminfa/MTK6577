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

import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.MtpDevice;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.FilmStripView;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.ImportCompleteListener;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.PositionRepository;
import com.android.gallery3d.ui.PositionRepository.Position;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.UserInteractionListener;
import com.android.gallery3d.util.GalleryUtils;

import android.bluetooth.BluetoothAdapter;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalVideo;
import com.android.gallery3d.ui.ConvergenceBarManager;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.DrmHelper;
import com.android.gallery3d.util.MpoHelper;
import com.android.gallery3d.util.StereoConvertor;
import com.android.gallery3d.util.StereoHelper;
import android.drm.DrmStore;
import android.drm.DrmManagerClient;
import android.content.DialogInterface;
//import android.content.res.Configuration;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.app.ProgressDialog;
import android.os.Handler;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.view.ViewGroup;

public class PhotoPage extends ActivityState
        implements PhotoView.PhotoTapListener, FilmStripView.Listener,
        UserInteractionListener, PhotoView.StereoModeChangeListener {
    private static final String TAG = "PhotoPage";

    private static final int MSG_HIDE_BARS = 1;
    //added for stereo display feature
    private static final int MSG_UPDATE_MENU = 2;

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
    private int mMtkInclusion = 0;

    // added for stereo 3D switching
    private static final boolean mIsStereoDisplaySupported = 
            MediatekFeature.isStereoDisplaySupported();
    //private static final boolean mIsDisplay2dAs3dSupported = 
    //        MediatekFeature.isDisplay2dAs3dSupported();

    private static final int STEREO_MODE_2D = 0;
    private static final int STEREO_MODE_3D = 1;
    private int mStereoMode = STEREO_MODE_3D;
    private ProgressDialog mProgressDialog;
    private Future<?> mConvertIntentTask;
    
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
    //private int mScreenOrientation = Configuration.ORIENTATION_UNDEFINED;
    private boolean mDisableBarChanges = false;

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
        //added to trigger stereo full image for 2d image
        public void triggerStereoFullImage();
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
        if (mIsStereoDisplaySupported) {
            mPhotoView.setStereoModeChangeListener(this);
        }
        mRootPane.addComponent(mPhotoView);
        mApplication = (GalleryApp)((Activity) mActivity).getApplication();

        if (mIsDrmSupported || mIsStereoDisplaySupported) {
            mMtkInclusion = data.getInt(DrmHelper.DRM_INCLUSION, 
                                        DrmHelper.NO_DRM_INCLUSION);
            mDrmMicroThumbDim = DrmHelper.getDrmMicroThumbDim((Activity) mActivity);
        }

        String setPathString = data.getString(KEY_MEDIA_SET_PATH);
        Path itemPath = null; 
        if (mIsDrmSupported || mIsStereoDisplaySupported) {
            itemPath = Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH),
                                       mMtkInclusion);
        } else {
            itemPath = Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH));
        }

        if (setPathString != null) {
            if (mIsDrmSupported || mIsStereoDisplaySupported) {
                mMediaSet = mActivity.getDataManager().getMediaSet(setPathString,
                                                                  mMtkInclusion);
                mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
                mMediaSet = (MediaSet)
                        mActivity.getDataManager().getMediaObject(setPathString,
                                                                  mMtkInclusion);
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

                        //init film strip view when first image is loaded in case
                        //that all previous MediaItem failed to load and
                        //onPhotoAvailable() is never called before.
                        if (mFilmStripView == null) initFilmStripView();

                        if (mFilmStripView != null) {
                            mFilmStripView.setFocusIndex(mModel.getCurrentIndex());
                        }
                        MediaItem photo = mModel.getCurrentMediaItem();
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
            MediaItem mediaItem = null;
            try {
                mediaItem = (MediaItem)
                        mActivity.getDataManager().getMediaObject(itemPath);
            } catch (Exception e) {
                Log.e(TAG, "Exception in getMediaObject(): ", e);
                Log.e(TAG, "quitting PhotoPage!");
                mActivity.getStateManager().finishState(this);
                return;
            }
            mModel = new SinglePhotoDataAdapter(mActivity, mPhotoView, mediaItem);
            mPhotoView.setModel(mModel);
            updateCurrentPhoto(mediaItem);
        }

        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIDE_BARS: {
                        hideBars();
                        break;
                    }
                    case MSG_UPDATE_MENU: {
                        //added for stereo display feature;
                        if (mIsStereoDisplaySupported) {
                            Log.v(TAG,"handleMessage:update menu operations()");
                            updateMenuOperations();
                            break;
                        }
                    }
                    default: throw new AssertionError(message.what);
                }
            }
        };

        // start the opening animation
        mPhotoView.setOpenedItem(itemPath);
        
        // M: added for stereo image manual convergence tuning
        mConvBarManager = new ConvergenceBarManager(mActivity.getAndroidContext(),
            (ViewGroup) ((View)mActivity.getGLRoot()).getParent());
        mConvBarManager.setConvergenceListener(mConvChangeListener);
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
        if (showTitle)
            mActionBar.setTitle(title);
        else
            mActionBar.setTitle("");
    }

    private void updateCurrentPhoto(MediaItem photo) {
        if (mCurrentPhoto == photo) {
            if (photo != null) {
                updateShareURI(photo.getPath());
            }

            // update stereo mode menu
            if (mIsStereoDisplaySupported) {
                resetStereoMode();
                updateMenuOperations();
                mPhotoView.setStoredProgress(photo.getConvergence());
                if (!mInConvTuningModeWhenPause) {
                    mStoredProgress = photo.getConvergence();
                }
            }

            return;
        }
        mCurrentPhoto = photo;
        if (mCurrentPhoto == null) return;

        if (mIsStereoDisplaySupported) {
            // update stereo mode menu
            resetStereoMode();
            String mimeType = mCurrentPhoto.getMimeType();
            boolean isImage = (mimeType != null ? mimeType.startsWith("image") : false);
            int supportedOperations = mCurrentPhoto.getSupportedOperations();
//            if (isImage && (supportedOperations & MediaObject.SUPPORT_STEREO_DISPLAY) != 0 &&
//                (supportedOperations & MediaObject.SUPPORT_CONVERT_TO_3D) == 0) {
            if ((supportedOperations & MediaObject.SUPPORT_STEREO_DISPLAY) != 0 &&
                    (supportedOperations & MediaObject.SUPPORT_CONVERT_TO_3D) == 0) {
                mPhotoView.allowStereoMode(true);
                // M: added for convergence feature
                if (isImage) {
                    showStereoHint();
                }
            } else {
                mPhotoView.allowStereoMode(false);
            }
            //set convergence queried from database
            mPhotoView.setStoredProgress(photo.getConvergence());
            if (!mInConvTuningModeWhenPause) {
                mStoredProgress = photo.getConvergence();
            }
        }

        updateMenuOperations();
        if (mShowDetails) {
            mDetailsHelper.reloadDetails(mModel.getCurrentIndex());
        }
        setTitle(photo.getName());
        mPhotoView.showVideoPlayIcon(
                photo.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO);

        if (MediatekFeature.isMpoSupported()) {
            //show mav icon
            mPhotoView.showMpoViewIcon(
                (photo.getSubType() & MediaObject.SUBTYPE_MPO_MAV) != 0);
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
        
        // added for stereo 3D display
        if (MediaObject.MEDIA_TYPE_IMAGE == mCurrentPhoto.getMediaType()) {
            //for image, we can display switch mode menu item
            //if (mIsDisplay2dAs3dSupported) {
            //    //force display switch mode menu
            //    supportedOperations |= MediaObject.SUPPORT_STEREO_DISPLAY;
            //}
            if (mIsStereoDisplaySupported &&  
                (supportedOperations & MediaObject.SUPPORT_STEREO_DISPLAY) != 0) {
                supportedOperations |= (mStereoMode == STEREO_MODE_3D) ? 
                    MediaObject.SUPPORT_SWITCHTO_2D : MediaObject.SUPPORT_SWITCHTO_3D;
            }
            if ((MediaObject.SUPPORT_CONV_TUNING & supportedOperations) != 0) {
                //we should check if ac is switched on/off
                if (StereoHelper.getACEnabled((Context)mActivity, true)) {
                    supportedOperations |= MediaObject.SUPPORT_AUTO_CONV;
                } else {
                    supportedOperations &= ~ MediaObject.SUPPORT_CONV_TUNING;
                }
            }
        } else {
            //for video, we hide the switch mode menu item according to spec
            supportedOperations &= ~MediaObject.SUPPORT_STEREO_DISPLAY;
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
        if (mDisableBarChanges) return;
        
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
        if (mDisableBarChanges) return;
        
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
            if (mInConvergenceTuningMode) {
                //leaveConvergenceTuningMode();
                mConvBarManager.dismissFirstRun();
                mConvBarManager.leaveConvTuningMode(false);
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
    }

    @Override
    protected boolean onCreateActionBar(Menu menu) {
        MenuInflater inflater = ((Activity) mActivity).getMenuInflater();
        inflater.inflate(R.menu.photo, menu);
        mShareActionProvider = GalleryActionBar.initializeShareActionProvider(menu);

        //added to support stereo display feature
        addShareSelectedListener();

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
                //add for DRM feature: pass drm inclusio info to next ActivityState
                if (mIsDrmSupported || mIsStereoDisplaySupported) {
                    data.putInt(DrmHelper.DRM_INCLUSION, mMtkInclusion);
                }
                mActivity.getStateManager().startStateForResult(
                        SlideshowPage.class, REQUEST_SLIDESHOW, data);
                return true;
            }
            case R.id.action_crop: {
                if (mIsStereoDisplaySupported &&
                    0 != (current.getSupportedOperations() & 
                          MediaObject.SUPPORT_STEREO_DISPLAY) &&
                    0 == (current.getSupportedOperations() & 
                          MediaObject.SUPPORT_CONVERT_TO_3D)) {
                    Log.i(TAG,"onItemSelected:for stereo image, show dialog");
                    showConvertCropDialog(current);
                    return true;
                }
                Activity activity = (Activity) mActivity;
                Intent intent = new Intent(CropImage.CROP_ACTION);
                intent.setClass(activity, CropImage.class);
                Uri uri = manager.getContentUri(path);
                uri = MediatekFeature.addMtkInclusion(uri, path);
                intent.setData(uri);
                activity.startActivityForResult(intent, PicasaSource.isPicasaImage(current)
                        ? REQUEST_CROP_PICASA
                        : REQUEST_CROP);
                return true;
            }
            case R.id.action_switch_stereo_mode: {
                switchStereoMode();
                updateMenuOperations();
                refreshHidingMessage();
                return true;
            }
/*
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

                StereoHelper.startStereoPrintActivity((Context) mActivity, intent);
                //try {
                //    activity.startActivity(Intent.createChooser(intent, 
                //                           activity.getText(R.string.printFile)));
                //} catch (android.content.ActivityNotFoundException ex) {
                //    Toast.makeText(activity, R.string.no_way_to_print, 
                //                   Toast.LENGTH_SHORT).show();
                //}
                return true;
            }
*/
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

            case R.id.action_print:

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
                
            // added for stereo3D manual convergence
            case R.id.action_adjust_convergence:
                tryEnterDepthTuningMode();
                return true;
                
            // M: added for stereo3D auto convergence switching
            case R.id.action_switch_auto_convergence:
                item.setChecked(!item.isChecked());
                // switch auto convergence on/off
                mPhotoView.setAcEnabled(item.isChecked());
                mPhotoView.invalidate();
                StereoHelper.setACEnabled((Context)mActivity, true, item.isChecked());
                updateMenuOperations();
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
                (item.getSubType() & MediaObject.SUBTYPE_MPO_MAV) != 0) {
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
            case REQUEST_SLIDESHOW: {
                if (data == null) break;
                String path = data.getStringExtra(SlideshowPage.KEY_ITEM_PATH);
                int index = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
                if (path != null) {
                    if (mIsDrmSupported || mIsStereoDisplaySupported) {
                        mModel.setCurrentPhoto(
                                  Path.fromString(path, mMtkInclusion), index);
                    } else {
                        mModel.setCurrentPhoto(Path.fromString(path), index);
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        
        int stateCount = mActivity.getStateManager().getStateCount();
        Log.v(TAG, "onPause: state count=" + stateCount);

        // when stateCount equals 0, this means that no more states are available
        // in StateManager, and activity will be finished;
        // for this case, do NOT reset stereo mode to avoid unnecessary mode change
        // and layout refresh.
        if (mIsStereoDisplaySupported && stateCount > 0) {
            //exit stereo mode
            Log.i(TAG,"onPause:exit stero mode");
            mActivity.getGLRoot().setStereoMode(false);
        }

        if (mFilmStripView != null) {
            mFilmStripView.pause();
        }
        DetailsHelper.pause();
        mPhotoView.pause();
        mModel.pause();
        mHandler.removeMessages(MSG_HIDE_BARS);
        mActionBar.removeOnMenuVisibilityListener(mMenuVisibilityListener);
        mMenuExecutor.pause();

        if (mIsStereoDisplaySupported) {
            //remove message
            mHandler.removeMessages(MSG_UPDATE_MENU);
            //cancel convert task if needed
            if (mConvertIntentTask != null) {
                mConvertIntentTask.cancel();
                mConvertIntentTask = null;
            }
            if (mInConvTuningModeWhenPause) {
                mTempProgressWhenPause = mPhotoView.mConvergenceProgress;
                //leaveConvergenceTuningMode();
                mConvBarManager.dismissFirstRun();
                mConvBarManager.leaveConvTuningMode(false);
                //as leave conv tuning mode will reset this flag, we restore it.
                mInConvTuningModeWhenPause = true;
            }
        }
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

        if (mIsStereoDisplaySupported) {
            resetStereoMode();
            updateMenuOperations();
        }
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

    //the Stereo display feature adds a 3D/2D converting icon.
    //Displaying 3D icon meaning that when clicked, it will enter 3D
    //display mode.
    //Displaying 2D icon meaning that when clicked, it will enter 2D
    //display mode.

    //The principle of 3D/2D icon and 3D/2D display mode with second image
    //loaded is that:
    //  When picture is displayed in 3D mode, icon is 2D, clickable
    //  When picture is displayed in 2D mode, icon is 3D, clickable

    //The principle of 3D/2D icon and 3D/2D display mode without second 
    //image loaded is that:
    //  When current picture is a truely 3D picture, icon is 2D, not clickable
    //  When current picture is a truely 2D picture, icon is 3D, not clickable

    //The click behavior of 3D/2D icon is that:
    // If it is not clickable: do nothing
    // If it is clickable:
    //    If icon is 3D, change picutre display mode to 3D, and show 2D icon,
    //        and Zoom out if needed.
    //    If icon is 2D, change picutre display mode to 2D, and show 3D icon

    //The behavior of double click picture and 3D/2D icon is that:
    // When picture is displayed in 3D mode, double click will cause it changed
    //     to 2D mode, and change icon to 3D.
    // When picture is displayed in 2D mode, double click will follow exactly
    //     routin.
    // When picture is displayed in zoomed 2D mode (currently it should be 2D,
    //     logic may change in the future), if double click cause picture zoomed
    //     out to original position, the display mode depends on previous state
    //     before we changed to zoomed in state:
    //    When previous state is 2D display mode, we will remain in 2D mode
    //    When previous state is 3D display mdoe, we will return to 3D mode

    public void onChangedToStereoMode(boolean stereoMode) {
        Log.i(TAG, "onChangedToStereoMode(stereoMode="+stereoMode+")");
        if (!isStereoStateReady()) {
            return;
        }
        if (mInConvTuningModeWhenPause) {
            mInConvTuningModeWhenPause = false;
            if (!mInConvergenceTuningMode) {
                //post a runnable to update
                mHandler.post(new Runnable() {
                    public void run() {
                        tryEnterDepthTuningMode(mTempProgressWhenPause);
                    }
                });
            }
        }
        if (STEREO_MODE_3D == mStereoMode && stereoMode ||
            STEREO_MODE_2D == mStereoMode && !stereoMode) return;
        //zoom out if needed
        if (STEREO_MODE_2D == mStereoMode) {
            mPhotoView.onResetZoomedState();
        }
        mStereoMode = 1 - mStereoMode;
        updateMenuOperationsInViewThread();
    }

    public void updateMenuOperationsInViewThread() {
        mHandler.removeMessages(MSG_UPDATE_MENU);
        mHandler.sendEmptyMessage(MSG_UPDATE_MENU);
    }

    private boolean isStereoStateReady() {
        if (mCurrentPhoto == null || !mIsStereoDisplaySupported ||
            null == mModel.getSecondImage()) {
            return false;
        } else {
            return true;
        }
    }
    
    // added for supporting stereo 3D/2D mode switching
    private void switchStereoMode() {
        if (!isStereoStateReady()) {
            return;
        }
        if (STEREO_MODE_2D == mStereoMode) {
            //if in 2D mode, and user want swith to 3D mode, we should 
            //firstly zoom to original state
            mPhotoView.onResetZoomedState();
            //Also, we trigger stereo full image task
            if (null != mModel) {
                mModel.triggerStereoFullImage();
            }
        }
        mStereoMode = 1 - mStereoMode;
        int supportedOperations = mCurrentPhoto.getSupportedOperations();
        setPhotoViewStereoMode(mStereoMode, supportedOperations);
    }
    
    private void resetStereoMode() {
        if (mCurrentPhoto == null || !mIsStereoDisplaySupported) {
            return;
        }
        int supportedOperations = mCurrentPhoto.getSupportedOperations();
        if ((supportedOperations & MediaObject.SUPPORT_STEREO_DISPLAY) != 0 &&
            (supportedOperations & MediaObject.SUPPORT_CONVERT_TO_3D) == 0) {
            Log.i(TAG,"resetStereoMode:3d mode");
            mStereoMode = STEREO_MODE_3D;
        } else {
            Log.i(TAG,"resetStereoMode:2d mode");
            mStereoMode = STEREO_MODE_2D;
        }
        setPhotoViewStereoMode(mStereoMode, supportedOperations);
    }

    private void setPhotoViewStereoMode(int stereoMode, int supportedOperations) {
        if (STEREO_MODE_3D == stereoMode && ((supportedOperations & 
            MediaObject.SUPPORT_STEREO_DISPLAY) != 0)) {
            Log.i(TAG,"setPhotoViewStereoMode:now in 3D mode, show stereo");
            mPhotoView.allowStereoMode(true);
            mPhotoView.setStereoMode(true);
        } else {
            Log.i(TAG,"setPhotoViewStereoMode:now in 2D mode, stop stereo");
            mPhotoView.allowStereoMode(false);
            mPhotoView.setStereoMode(false);
        }
    }

    private void showConvertCropDialog(final MediaItem item) {
        final AlertDialog.Builder builder =
                            new AlertDialog.Builder((Context)mActivity);

        DialogInterface.OnClickListener clickListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (DialogInterface.BUTTON_POSITIVE == which) {
                        //we don't convert here because CropImage can
                        //help do the same thing
                        startCropIntent(item);
                    }
                    dialog.dismiss();
                }
            };

        String crop = ((Activity) mActivity).getString(R.string.crop_action);
        String convertCrop = ((Activity) mActivity).getString(
                         R.string.stereo3d_convert2d_dialog_text,crop);

        builder.setPositiveButton(android.R.string.ok, clickListener);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setTitle(R.string.stereo3d_convert2d_dialog_title)
               .setMessage(convertCrop);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startCropIntent(final MediaItem item) {
        Activity activity = (Activity) mActivity;
        Intent intent = new Intent(CropImage.CROP_ACTION);
        intent.setClass(activity, CropImage.class);
        Uri uri = item.getContentUri();
        uri = MediatekFeature.addMtkInclusion(uri, item.getPath());
        intent.setData(uri);
        activity.startActivityForResult(intent, PicasaSource.isPicasaImage(item)
                ? REQUEST_CROP_PICASA
                : REQUEST_CROP);
    }

    private void addShareSelectedListener() {
        if (null == mShareActionProvider) return;
        OnShareTargetSelectedListener listener = new OnShareTargetSelectedListener() {
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                //when set share intent, we should first check if there is stereo
                //image inside, and set this info into to Bundle inside the intent
                //When this function runs, we should check that whether we should
                //prompt a dialog. If No, returns false and continue original
                //rountin. If Yes, change the content of Bundle inside intent, and
                //re-start the intent by ourselves.
                if (mIsStereoDisplaySupported) {
                    Log.i(TAG,"addShareSelectedListener:intent="+intent);
                    return checkIntent(intent);
                }
                return false;
            }
        };

        mShareActionProvider.setOnShareTargetSelectedListener(listener);

    }

    private boolean checkIntent(Intent intent) {
        if (null == intent) return false;
        if (intent.getAction() != Intent.ACTION_SEND) {
            Log.w(TAG, "checkIntent: unintented action type");
            return false;
        }
        Uri uri = (Uri)intent.getExtra(Intent.EXTRA_STREAM);
Log.i(TAG,"checkIntent:uri="+uri);
        if (null == uri) {
            Log.e(TAG, "checkIntent:got null uri");
            return false;
        }
        DataManager manager = mActivity.getDataManager();
        Path itemPath = manager.findPathByUri(uri);
Log.v(TAG,"checkIntent:itemPath="+itemPath);
        MediaItem item = (MediaItem) manager.getMediaObject(itemPath);
Log.v(TAG,"checkIntent:item="+item);
        int support = manager.getSupportedOperations(itemPath);
Log.i(TAG,"checkIntent:support:"+support);
        if ((support & MediaObject.SUPPORT_STEREO_DISPLAY) != 0 &&
            (support & MediaObject.SUPPORT_CONVERT_TO_3D) == 0 &&
            MediaObject.MEDIA_TYPE_IMAGE == item.getMediaType()) {
Log.i(TAG,"checkIntent:found a stereo image");
            checkIntent(intent, item);
            return true;
        } else {
            //for normal image or video, follow original routin
            return false;
        }
    }

    private void checkIntent(Intent intent, MediaItem item) {
        if (null == intent || null == intent.getComponent()) {
            Log.e(TAG,"checkStereoIntent:invalid intent:"+intent);
            return;
        }

        String packageName = intent.getComponent().getPackageName();
        Log.d(TAG,"checkStereoIntent:packageName="+packageName);
        //this judgement is very simple, need to enhance in the future
        boolean onlyShareAs2D = "com.android.mms".equals(packageName);
        showStereoShareDialog(intent, item, onlyShareAs2D);
    }

    private void showStereoShareDialog(Intent intent, 
                    final MediaItem item, boolean shareAs2D) {
        int positiveCap = 0;
        int negativeCap = 0;
        int title = 0;
        int message = 0;
        if (shareAs2D) {
            positiveCap = android.R.string.ok;
            negativeCap = android.R.string.cancel;
            title = R.string.stereo3d_convert2d_dialog_title;
            message = R.string.stereo3d_share_convert_text_single;
        } else {
            positiveCap = R.string.stereo3d_share_dialog_button_2d;
            negativeCap = R.string.stereo3d_share_dialog_button_3d;
            title = R.string.stereo3d_share_dialog_title;
            message = R.string.stereo3d_share_dialog_text_single;
        }
        final Intent shareIntent = intent;
        final boolean onlyShareAs2D = shareAs2D;
        final AlertDialog.Builder builder =
                        new AlertDialog.Builder((Context)mActivity);

        DialogInterface.OnClickListener clickListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (DialogInterface.BUTTON_POSITIVE == which) {
                        convertAndShare(shareIntent, item);
                    } else {
                        if (!onlyShareAs2D) {
                            safeStartIntent(shareIntent);
                        }
                    }
                    dialog.dismiss();
                }
            };
        builder.setPositiveButton(positiveCap, clickListener);
        builder.setNegativeButton(negativeCap, clickListener);
        builder.setTitle(title)
               .setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void safeStartIntent(Intent intent) {
        Log.i(TAG,"safeStartIntent:start intent:" + intent);
        try {
            ((Activity)mActivity).startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            android.widget.Toast.makeText(((Activity)mActivity), 
                ((Activity)mActivity).getString(R.string.activity_not_found),
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void convertAndShare(final Intent intent, final MediaItem item) {
        Log.i(TAG,"convertAndShare(intent="+intent+",item="+item+")");
        if (mConvertIntentTask != null) {
            mConvertIntentTask.cancel();
        }
        //show converting dialog
        int messageId = R.string.stereo3d_convert2d_progress_text;
        mProgressDialog = ProgressDialog.show((Activity)mActivity, null, 
                ((Activity)mActivity).getString(messageId), true, false);
        //create a job that convert intents and start sharing intent.
        mConvertIntentTask = mActivity.getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                //the majer process!
                Uri convertedUri = StereoConvertor.convertSingle(jc, (Context)mActivity,
                             item.getContentUri(), item.getMimeType());
                intent.putExtra(Intent.EXTRA_STREAM, convertedUri);
                //dismis progressive dialog when we done
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConvertIntentTask = null;
                        if (null != mProgressDialog) {
                            Log.v(TAG,"mConvertIntentTask:dismis ProgressDialog");
                            mProgressDialog.dismiss();
                        }
                    }
                });
                //start new intent
                if (!jc.isCancelled()) {
                    safeStartIntent(intent);
                }
                return null;
            }
        });
    }

    private void tryEnterDepthTuningMode() {
        tryEnterDepthTuningMode(mPhotoView.mConvergenceProgress);
    }

    private void tryEnterDepthTuningMode(int progress) {
        if (!isStereoStateReady()) {
            Log.w(TAG, "tryEnterDepthTuningMode:not ready!");
            return;
        }
        Log.d(TAG, "tryEnterDepthTuningMode:progress=" + progress);
        mConvBarManager.enterConvTuningMode(
            (ViewGroup) ((View)mActivity.getGLRoot()).getParent(),
            mPhotoView.mConvergenceValues, mPhotoView.mActiveFlags,
            progress);
        mInConvTuningModeWhenPause = true;
    }

    // for stereo3D phase 2: convergence manual tuning
    private boolean mInConvergenceTuningMode = false;
    private boolean mInConvTuningModeWhenPause = false;
    private int mStoredProgress;
    private int mTempProgressWhenPause;
    private ConvergenceBarManager mConvBarManager;
    private ConvergenceBarManager.ConvergenceChangeListener
        mConvChangeListener = new ConvergenceBarManager.ConvergenceChangeListener() {
        
        @Override
        public void onLeaveConvTuningMode(boolean saveValue, int value) {
            mInConvergenceTuningMode = false;
            
            // 1. re-enable sliding image
            // 2. re-enable zooming-in/out (both by double tap and by multi-touch)
            if (mPhotoView != null) {
                mPhotoView.enterConvMode(false);
            }
            
            // 3. re-enable (but do not show directly) actionbar and filmstripview
            enableBarChanges();
            
            // 4. re-enable options menu: options menu is re-enabled in onPrepareActionBar
            
            // 5. handle convergence value saving process
            if (saveValue) {
                /// TODO: save current value to DB
                int progress;
                if (mPhotoView != null) {
                    progress = mPhotoView.mConvergenceProgress;
                    mPhotoView.setStoredProgress(progress);
                    mPhotoView.invalidate();
                    Log.d(TAG,"onLeaveConvTuningMode:saving progress " + progress);
                    StereoHelper.updateConvergence((Context) mActivity, 
                                      progress, mModel.getCurrentMediaItem());
                    mStoredProgress = progress;
                }
            } else {
                // reset current value
                Log.d(TAG,"onLeaveConvTuningMode:reset progress " + mStoredProgress);
                if (mPhotoView != null) {
                    mPhotoView.setConvergenceProgress(mStoredProgress);
                    mPhotoView.invalidate();
                }
            }
            // 6. record onpause status
            mInConvTuningModeWhenPause = false;
        }
        
        @Override
        public void onEnterConvTuningMode() {
            mInConvergenceTuningMode = true;
            
            // 1. hide and disable actionbar and filmstripview
            mHandler.removeMessages(MSG_HIDE_BARS);
            hideBars();
            disableBarChanges();
            
            // 2. disable options menu: options menu is disabled in onPrepareActionBar

            // 3. disable sliding image

            if (mPhotoView == null)  return;

            // 4. disable zooming-in/out (both by double tap and by multi-touch)
            mPhotoView.enterConvMode(true);
            
            // 5. record current convergence progress
            if (mStoredProgress < 0) {
                mStoredProgress = mPhotoView.mConvergenceProgress;
                Log.i(TAG, "enter conv mode:mStoredProgress=" + mStoredProgress);
            }

            // 6. Scale to suggested position
            mPhotoView.onZoomToSuggestedScale();

            // 7. reset to 3D stereo mode
            resetStereoMode();
        }
        
        @Override
        public void onConvValueChanged(int value) {
            Log.i(TAG, "convergence value changed to: " + value);

            if (mPhotoView != null) {
                mPhotoView.setConvergenceProgress(value);
                mPhotoView.invalidate();
            }
        }

        @Override
        public void onFirstRunHintShown() {
            // we borrow convergence tuning sequence here
            onEnterConvTuningMode();
        }

        @Override
        public void onFirstRunHintDismissed() {
            // we borrow convergence tuning sequence here
            mInConvergenceTuningMode = false;
            
            // 1. re-enable sliding image
            // 2. re-enable zooming-in/out (both by double tap and by multi-touch)
            if (mPhotoView != null) {
                mPhotoView.enterConvMode(false);
            }
            
            // 3. re-enable (but do not show directly) actionbar and filmstripview
            enableBarChanges();
            
            // 4. re-enable options menu: options menu is re-enabled in onPrepareActionBar
        }
    };
    
    @Override
    protected boolean onPrepareActionBar(Menu menu) {
        return !mInConvergenceTuningMode;
    }

    // for performance auto test
    public void disableBarChanges() {
        mDisableBarChanges  = true;
    }
    
    public void enableBarChanges() {
        mDisableBarChanges = false;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (!mInConvergenceTuningMode || mConvBarManager == null) {
            return;
        }
        Log.d(TAG, "onConfigurationChanged");
        mConvBarManager.reloadFirstRun();
        mConvBarManager.reloadConvergenceBar();
    }

    private void showStereoHint() {
        // M: added for manual convergence
        if (mConvBarManager != null) {
            mConvBarManager.onStereoMediaOpened(true);
        }
    }
    
}
