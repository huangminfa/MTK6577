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

package com.android.gallery3d.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryActionBar;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.CustomMenu.DropDownMenu;
import com.android.gallery3d.ui.MenuExecutor.ProgressListener;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.StereoConvertor;
import com.android.gallery3d.util.StereoHelper;
import com.android.gallery3d.data.MediaItem;

public class ActionModeHandler implements ActionMode.Callback {
    private static final String TAG = "ActionModeHandler";

    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();

    private static final int SUPPORT_MULTIPLE_MASK = MediaObject.SUPPORT_DELETE
            | MediaObject.SUPPORT_ROTATE | MediaObject.SUPPORT_SHARE
            | MediaObject.SUPPORT_CACHE | MediaObject.SUPPORT_IMPORT;

    public interface ActionModeListener {
        public boolean onActionItemClicked(MenuItem item);
    }

    private final GalleryActivity mActivity;
    private final MenuExecutor mMenuExecutor;
    private final SelectionManager mSelectionManager;
    private Menu mMenu;
    private DropDownMenu mSelectionMenu;
    private ActionModeListener mListener;
    private Future<?> mMenuTask;
    private final Handler mMainHandler;
    private ShareActionProvider mShareActionProvider;

    private ProgressDialog mProgressDialog;
    private Future<?> mConvertIntentTask;

    public ActionModeHandler(
            GalleryActivity activity, SelectionManager selectionManager) {
        mActivity = Utils.checkNotNull(activity);
        mSelectionManager = Utils.checkNotNull(selectionManager);
        mMenuExecutor = new MenuExecutor(activity, selectionManager);
        mMainHandler = new Handler(activity.getMainLooper());
    }

    public ActionMode startActionMode() {
        Activity a = (Activity) mActivity;
        final ActionMode actionMode = a.startActionMode(this);
        CustomMenu customMenu = new CustomMenu(a);
        View customView = LayoutInflater.from(a).inflate(
                R.layout.action_mode, null);
        actionMode.setCustomView(customView);
        mSelectionMenu = customMenu.addDropDownMenu(
                (Button) customView.findViewById(R.id.selection_menu),
                R.menu.selection);
        updateSelectionMenu();
        customMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return onActionItemClicked(actionMode, item);
            }
        });
        return actionMode;
    }

    public void setTitle(String title) {
        mSelectionMenu.setTitle(title);
    }

    public void setActionModeListener(ActionModeListener listener) {
        mListener = listener;
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        boolean result;
        if (mListener != null) {
            result = mListener.onActionItemClicked(item);
            if (result) {
                mSelectionManager.leaveSelectionMode();
                return result;
            }
        }
        ProgressListener listener = null;
        if (item.getItemId() == R.id.action_import) {
            listener = new ImportCompleteListener(mActivity);
        }
        result = mMenuExecutor.onMenuClicked(item, listener);
        if (item.getItemId() == R.id.action_select_all) {
            updateSupportedOperation();
            updateSelectionMenu();
        }
        return result;
    }

    public void updateSelectionMenu() {
        // update title
        int count = mSelectionManager.getSelectedCount();
        String format = mActivity.getResources().getQuantityString(
                R.plurals.number_of_items_selected, count);
        setTitle(String.format(format, count));
        // For clients who call SelectionManager.selectAll() directly, we need to ensure the
        // menu status is consistent with selection manager.
        MenuItem item = mSelectionMenu.findItem(R.id.action_select_all);
        if (item != null) {
            if (mSelectionManager.inSelectAllMode()) {
                item.setChecked(true);
                item.setTitle(R.string.deselect_all);
            } else {
                item.setChecked(false);
                item.setTitle(R.string.select_all);
            }
        }
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.operation, menu);

        mShareActionProvider = GalleryActionBar.initializeShareActionProvider(menu);
        OnShareTargetSelectedListener listener = new OnShareTargetSelectedListener() {
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                mSelectionManager.leaveSelectionMode();

                //when set share intent, we should first check if there is stereo
                //image inside, and set this info into to Bundle inside the intent
                //When this function runs, we should check that whether we should
                //prompt a dialog. If No, returns false and continue original
                //rountin. If Yes, change the content of Bundle inside intent, and
                //re-start the intent by ourselves.
                if (mIsStereoDisplaySupported && null != intent.getExtras() &&
                    intent.getExtras().getBoolean(
                                 StereoHelper.INCLUDED_STEREO_IMAGE, false)) {
                    checkStereoIntent(intent);
                    return true;
                }

                return false;
            }
        };

        mShareActionProvider.setOnShareTargetSelectedListener(listener);
        mMenu = menu;
        return true;
    }

    public void onDestroyActionMode(ActionMode mode) {
        mSelectionManager.leaveSelectionMode();
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    // Menu options are determined by selection set itself.
    // We cannot expand it because MenuExecuter executes it based on
    // the selection set instead of the expanded result.
    // e.g. LocalImage can be rotated but collections of them (LocalAlbum) can't.
    private void updateMenuOptions(JobContext jc) {
        mMainHandler.post(new Runnable() {
        	public void run() {
        		MenuExecutor.updateSupportedMenuEnabled(mMenu, MediaObject.SUPPORT_ALL, false);
        	}
        });
        ArrayList<Path> paths = mSelectionManager.getSelected(false);

        int operation = MediaObject.SUPPORT_ALL;
        DataManager manager = mActivity.getDataManager();
        int type = 0;
        for (Path path : paths) {
            if (jc.isCancelled()) return;
            int support = manager.getSupportedOperations(path);
            type |= manager.getMediaType(path);
            operation &= support;
        }

        final String mimeType = MenuExecutor.getMimeType(type);
        if (paths.size() == 0) {
            operation = 0;
        } else if (paths.size() == 1) {
            if (!GalleryUtils.isEditorAvailable((Context) mActivity, mimeType)) {
                operation &= ~MediaObject.SUPPORT_EDIT;
            }
        } else {
            operation &= SUPPORT_MULTIPLE_MASK;
        }

        final int supportedOperation = operation;

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mMenuTask = null;
                MenuExecutor.updateMenuOperation(mMenu, supportedOperation);
                MenuExecutor.updateSupportedMenuEnabled(mMenu, MediaObject.SUPPORT_ALL, true);
            }
        });
    }

    // Share intent needs to expand the selection set so we can get URI of
    // each media item
    private void updateSharingIntent(JobContext jc) {
        if (mShareActionProvider == null) return;
        ArrayList<Path> paths = mSelectionManager.getSelected(true);
        if (paths.size() == 0) return;

        final ArrayList<Uri> uris = new ArrayList<Uri>();

        DataManager manager = mActivity.getDataManager();
        int type = 0;

        boolean includedStereoImage = false;
        int mediaType = 0;

        final Intent intent = new Intent();
        for (Path path : paths) {
            if (jc.isCancelled()) {
                Log.w(TAG, "updateSharingIntent is cancelled, return...");
                return;
            }
            int support = manager.getSupportedOperations(path);
            mediaType = manager.getMediaType(path);
            type |= mediaType;

            if ((support & MediaObject.SUPPORT_SHARE) != 0) {
                uris.add(manager.getContentUri(path));
            }

            if (mIsStereoDisplaySupported &&
                MediaObject.MEDIA_TYPE_IMAGE == mediaType &&
                (support & MediaObject.SUPPORT_STEREO_DISPLAY) != 0 &&
                (support & MediaObject.SUPPORT_CONVERT_TO_3D) == 0) {
                //we found a stereo image, record this info
                includedStereoImage = true;
            }
        }

        final int size = uris.size();
        if (size > 0) {
            final String mimeType = MenuExecutor.getMimeType(type);
            if (size > 1) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE).setType(mimeType);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                intent.setAction(Intent.ACTION_SEND).setType(mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            }
            intent.setType(mimeType);

            //there is some stereo image in the list, we record this info
            if (mIsStereoDisplaySupported && includedStereoImage) {
                Log.i(TAG,"updateSharingIntent:stereo image included in intent");
                intent.putExtra(StereoHelper.INCLUDED_STEREO_IMAGE, true);
            }

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "Sharing intent is ready: action = " + intent.getAction());
                    mShareActionProvider.setShareIntent(intent);
                }
            });
        }
    }

    public void updateSupportedOperation(Path path, boolean selected) {
        // TODO: We need to improve the performance
        updateSupportedOperation();
    }

    public void updateSupportedOperation() {
        if (mMenuTask != null) {
            mMenuTask.cancel();
        }

        // Disable share action until share intent is in good shape
        if (mShareActionProvider != null) {
            Log.v(TAG, "Disable sharing until intent is ready");
            mShareActionProvider.setShareIntent(null);
        }

        // Generate sharing intent and update supported operations in the background
        mMenuTask = mActivity.getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                updateMenuOptions(jc);
                updateSharingIntent(jc);
                return null;
            }
        });
    }

    public void pause() {
        if (mMenuTask != null) {
            mMenuTask.cancel();
            mMenuTask = null;
        }
        if (mConvertIntentTask != null) {
            mConvertIntentTask.cancel();
            mConvertIntentTask = null;
        }
        mMenuExecutor.pause();
        // set share intent as null to avoid menu flash
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(null);
        }
    }

    public void resume() {
        if (mSelectionManager.inSelectionMode()) updateSupportedOperation();
    }

    private void checkStereoIntent(Intent intent) {
        if (null == intent || null == intent.getComponent()) {
            Log.e(TAG,"checkStereoIntent:invalid intent:"+intent);
            return;
        }

        String packageName = intent.getComponent().getPackageName();
        Log.d(TAG,"checkStereoIntent:packageName="+packageName);
        //this judgement is very simple, need to enhance in the future
        boolean onlyShareAs2D = "com.android.mms".equals(packageName);
        showStereoShareDialog(intent, onlyShareAs2D);
    }

    private void showStereoShareDialog(Intent intent, boolean shareAs2D) {
        int positiveCap = 0;
        int negativeCap = 0;
        int title = 0;
        int message = 0;
        boolean multipleSelected = intent.getAction() == Intent.ACTION_SEND_MULTIPLE;

        if (shareAs2D) {
            positiveCap = android.R.string.ok;
            negativeCap = android.R.string.cancel;
            title = R.string.stereo3d_convert2d_dialog_title;
            if (multipleSelected) {
                message = R.string.stereo3d_share_convert_text_multiple;
            } else {
                message = R.string.stereo3d_share_convert_text_single;
            }
        } else {
            positiveCap = R.string.stereo3d_share_dialog_button_2d;
            negativeCap = R.string.stereo3d_share_dialog_button_3d;
            title = R.string.stereo3d_share_dialog_title;
            if (multipleSelected) {
                message = R.string.stereo3d_share_dialog_text_multiple;
            } else {
                message = R.string.stereo3d_share_dialog_text_single;
            }
        }
        final Intent shareIntent = intent;
        final boolean onlyShareAs2D = shareAs2D;
        final AlertDialog.Builder builder =
                        new AlertDialog.Builder((Context)mActivity);

        DialogInterface.OnClickListener clickListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (DialogInterface.BUTTON_POSITIVE == which) {
                        convertAndShare(shareIntent);
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
        try {
            ((Activity)mActivity).startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            android.widget.Toast.makeText(((Activity)mActivity), 
                ((Activity)mActivity).getString(R.string.activity_not_found),
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void convertAndShare(final Intent intent) {
        Log.i(TAG,"convertAndShare(intent="+intent+")");
        if (mConvertIntentTask != null) {
            mConvertIntentTask.cancel();
        }
        //show converting dialog
        int messageId = R.string.stereo3d_convert2d_progress_text;
        mProgressDialog = ProgressDialog.show(
                ((Activity)mActivity), null, 
                ((Activity)mActivity).getString(messageId), true, false);
        //create a job that convert intents and start sharing intent.
        mConvertIntentTask = mActivity.getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                //the majer process!
                processIntent(jc, intent);
                //dismis progressive dialog when we done
                mMainHandler.post(new Runnable() {
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

    private void processIntent(JobContext jc, Intent intent) {
        DataManager manager = mActivity.getDataManager();
        Path itemPath = null;
        MediaItem item = null;
        int support = 0;
        Uri convertedUri = null;
        if (jc.isCancelled()) return;
        if (intent.getAction() == Intent.ACTION_SEND_MULTIPLE) {
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            ArrayList<Uri> newUris = StereoConvertor.convertMultiple(jc, mActivity, uris);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, newUris);
        } else if (intent.getAction() == Intent.ACTION_SEND) {
            Uri uri = (Uri)intent.getExtra(Intent.EXTRA_STREAM);
            Log.v(TAG,"processIntent:send single:uri="+uri);
            itemPath = manager.findPathByUri(uri);
            item = (MediaItem) manager.getMediaObject(itemPath);
            if (StereoHelper.isStereoImage(item)) {
                convertedUri = StereoConvertor.convertSingle(jc, (Context)mActivity,
                                                             uri, item.getMimeType());
                Log.i(TAG,"processIntent:got new Uri="+convertedUri);
                //temporarily workaround
                if (null == convertedUri) {
                    Log.e(TAG,"processIntent:convert failed, insert original");
                    convertedUri = manager.getContentUri(itemPath);
                }
                intent.putExtra(Intent.EXTRA_STREAM, convertedUri);
            }
        }
    }
}
