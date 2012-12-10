/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.email.activity;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import com.android.email.R;

/**
 * An adapter to store icons and strings for attachment type list.
 */
public class AttachmentTypeSelectorAdapter extends IconListAdapter {

    public final static int ADD_IMAGE               = 0;
    public final static int ADD_MUSIC               = 1;
    public final static int ADD_VIDEO               = 2;
    public final static int ADD_CONTACT             = 3;
    public final static int ADD_FILE                = 4;
    public final static int ADD_CALENDAR            = 5;

    public AttachmentTypeSelectorAdapter(Context context) {
        super(context, getData(context));
    }

    public int buttonToCommand(int whichButton) {
        AttachmentListItem item = (AttachmentListItem)getItem(whichButton);
        return item.getCommand();
    }

    protected static List<IconListItem> getData(Context context) {
        List<IconListItem> data = new ArrayList<IconListItem>(4);
        
        addItem(data, context.getString(R.string.attach_image),
                R.drawable.ic_launcher_gallery, ADD_IMAGE);
        
        addItem(data, context.getString(R.string.attach_sound),
                R.drawable.ic_launcher_musicplayer_2, ADD_MUSIC);
        
        addItem(data, context.getString(R.string.attach_video),
                R.drawable.ic_launcher_video_player, ADD_VIDEO);
        
        addItem(data, context.getString(R.string.attach_contact),
                R.drawable.ic_launcher_contacts, ADD_CONTACT);
        
        addItem(data, context.getString(R.string.attach_calender),
                R.drawable.ic_launcher_calendar, ADD_CALENDAR);
        
        addItem(data, context.getString(R.string.attach_file),
                R.drawable.ic_launcher_filemanager, ADD_FILE);
        
        return data;
        
    }

    protected static void addItem(List<IconListItem> data, String title,
            int resource, int command) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, command);
        data.add(temp);
    }

    public static class AttachmentListItem extends IconListAdapter.IconListItem {
        private int mCommand;

        public AttachmentListItem(String title, int resource, int command) {
            super(title, resource);

            mCommand = command;
        }

        public int getCommand() {
            return mCommand;
        }
    }
}
