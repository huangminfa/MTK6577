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

import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SelectionManager {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectionManager";

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;

    private Set<Path> mClickedSet;
    private MediaSet mSourceMediaSet;
    private SelectionListener mListener;
    private DataManager mDataManager;
    private boolean mInverseSelection;
    private boolean mIsAlbumSet;
    private boolean mInSelectionMode;
    private boolean mAutoLeave = true;
    private int mTotal;
    private Path mPressedPath;

    private ArrayList<Path> mSelection = null;

    public interface SelectionListener {
        public void onSelectionModeChange(int mode);
        public void onSelectionChange(Path path, boolean selected);
    }

    public SelectionManager(GalleryContext galleryContext, boolean isAlbumSet) {
        mDataManager = galleryContext.getDataManager();
        mClickedSet = new HashSet<Path>();
        mIsAlbumSet = isAlbumSet;
        mTotal = -1;
    }

    // Whether we will leave selection mode automatically once the number of
    // selected items is down to zero.
    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void selectAll() {
        mInverseSelection = true;
        mClickedSet.clear();
        enterSelectionMode();
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    public void deSelectAll() {
        leaveSelectionMode();
        mInverseSelection = false;
        mClickedSet.clear();
    }

    public boolean inSelectAllMode() {
        return mInverseSelection;
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode) return;

        mInSelectionMode = true;
        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode) return;

        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        // reset total number so that the select all number will be re-calculated
        mTotal = -1;
        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }
    
    public void switchSelectAllMode(boolean selectAll) {
        if (selectAll) {
            mInverseSelection = true;
            mClickedSet.clear();
        } else {
            mInverseSelection = false;
            mClickedSet.clear();
        }
    }

    public boolean isItemSelected(Path itemId) {
        return mInverseSelection ^ mClickedSet.contains(itemId);
    }

    public int getSelectedCount() {
        int count = mClickedSet.size();
        if (mInverseSelection) {
            if (mTotal < 0) {
                mTotal = mIsAlbumSet
                        ? mSourceMediaSet.getSubMediaSetCount()
                        : mSourceMediaSet.getMediaItemCount();
            }
            count = mTotal - count;
        }
        return count;
    }

    public void toggle(Path path) {
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
        }

        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
        if (getSelectedCount() == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
    }

    public void setPressedPath(Path path) {
        mPressedPath = path;
    }

    public boolean isPressedPath(Path path) {
        return path != null && path == mPressedPath;
    }

    private static void expandMediaSet(ArrayList<Path> items, MediaSet set) {
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            expandMediaSet(items, set.getSubMediaSet(i));
        }
        int total = set.getMediaItemCount();
        int batch = 50;
        int index = 0;

        while (index < total) {
            int count = index + batch < total
                    ? batch
                    : total - index;
            ArrayList<MediaItem> list = set.getMediaItem(index, count);
            for (MediaItem item : list) {
                items.add(item.getPath());
            }
            index += batch;
        }
    }

    public ArrayList<Path> getSelected(boolean expandSet) {
        ArrayList<Path> selected = new ArrayList<Path>();
        if (mIsAlbumSet) {
            if (mInverseSelection) {
                int max = mSourceMediaSet.getSubMediaSetCount();
                for (int i = 0; i < max; i++) {
                    MediaSet set = mSourceMediaSet.getSubMediaSet(i);
                    Path id = set.getPath();
                    if (!mClickedSet.contains(id)) {
                        if (expandSet) {
                            expandMediaSet(selected, set);
                        } else {
                            selected.add(id);
                        }
                    }
                }
            } else {
                for (Path id : mClickedSet) {
                    if (expandSet) {
                        expandMediaSet(selected, mDataManager.getMediaSet(id));
                    } else {
                        selected.add(id);
                    }
                }
            }
        } else {
            if (mInverseSelection) {

                int total = mSourceMediaSet.getMediaItemCount();
                int index = 0;
                while (index < total) {
                    int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                    ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                    for (MediaItem item : list) {
                        Path id = item.getPath();
                        if (!mClickedSet.contains(id)) selected.add(id);
                    }
                    index += count;
                }
            } else {
                for (Path id : mClickedSet) {
                    selected.add(id);
                }
            }
        }
        return selected;
    }

    public void setSourceMediaSet(MediaSet set) {
        mSourceMediaSet = set;
        mTotal = -1;
    }

    public MediaSet getSourceMediaSet() {
        return mSourceMediaSet;
    }
    
    public void saveSelection() {
        if (mSelection != null) {
            mSelection.clear();
        }
        try {
            mSelection  = getSelected(false);
        } catch (Exception e) {
            // this probably means that the actual items are changing
            // while fetching selected items, so we do not save selection
            // under this situation
            /// TODO: find more suitable method to protect this part
            mSelection = null;
        }
    }
    
    public void restoreSelection() {
        Log.d(TAG, "restoreSelection");
        if (mSourceMediaSet == null || mSelection == null) {
            return;
        }
        int oldTotalCnt = mTotal;
        mTotal = mIsAlbumSet
                   ? mSourceMediaSet.getSubMediaSetCount()
                   : mSourceMediaSet.getMediaItemCount();
        Log.d(TAG, " oldTotal=" + oldTotalCnt + ", newTotal=" + mTotal);
        if (oldTotalCnt == mTotal || mTotal <= 0) return;
        Path id = null;
        ArrayList<Path> availablePaths = new ArrayList<Path>();
        // remove dirty entry
        if (mIsAlbumSet) {
            MediaSet set = null;
            for (int i = 0; i < mTotal; ++i) {
                set = mSourceMediaSet.getSubMediaSet(i);
                if (set != null) {
                    id = set.getPath();
                    if (mSelection.contains(id)) {
                        availablePaths.add(id);
                    }
                }
            }
        } else {
            ArrayList<MediaItem> items = mSourceMediaSet.getMediaItem(0, mTotal);
            if (items != null && items.size() > 0) {
                for (MediaItem item : items) {
                    id = item.getPath();
                    if (mSelection.contains(id)) {
                        availablePaths.add(id);
                    }
                }
            }
        }
        
        int newCnt = availablePaths.size();
        if (newCnt == mTotal && mTotal != 0) {
            // in select all mode
            selectAll();
        } else {
            // leave select all mode and set clicked set
            mInverseSelection = false;
            mClickedSet.clear();
            for (int i = 0; i < newCnt; ++i) {
                mClickedSet.add(availablePaths.get(i));
            }
        }
        // clear saved selection when done
        mSelection.clear();
        mSelection = null;
    }
}
