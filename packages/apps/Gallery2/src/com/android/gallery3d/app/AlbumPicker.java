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

package com.android.gallery3d.app;

import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.util.MediatekFeature;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;

public class AlbumPicker extends PickerActivity {

    public static final String KEY_ALBUM_PATH = "album-path";
    
    private static final String MIMETYPE_VIDEO = "video/*";
    private static final String CURSOR_MIMETYPE_VIDEO = ContentResolver.CURSOR_DIR_BASE_TYPE + "/video";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.select_album);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Bundle data = extras == null ? new Bundle() : new Bundle(extras);

        data.putBoolean(Gallery.KEY_GET_ALBUM, true);
        
        // support for Video live wallpaper
        // decide top set path by mime type of intent
        String mimeType = intent.getType();
        String path = null;
        if (MediatekFeature.hasCustomizedForVLW() && 
                (MIMETYPE_VIDEO.equalsIgnoreCase(mimeType) || CURSOR_MIMETYPE_VIDEO.equalsIgnoreCase(mimeType))) {
            path = getDataManager().getTopSetPath(DataManager.INCLUDE_VIDEO);
        } else {
            path = getDataManager().getTopSetPath(DataManager.INCLUDE_IMAGE);
        }
        data.putString(AlbumSetPage.KEY_MEDIA_PATH, path);
        getStateManager().startState(AlbumSetPage.class, data);
    }
}
