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
 * Copyright (C) 2007 The Android Open Source Project
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

package com.mediatek.notebook;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.notebook.NoteAdapter.NoteItem;

import android.app.ActionBar;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.TextView;

public final class NotePad 
{
    public static final String AUTHORITY = "com.mediatek.notebook.NotePad";

    private NotePad() {}

    public static final class Notes implements BaseColumns {

        private Notes() {}

        public static final String TABLE_NAME = "notes";

        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/notes";

        private static final String PATH_NOTE_ID = "/notes/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        private static final String PATH_LIVE_FOLDER = "/live_folders/notes";

        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);

        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);

        public static final Uri CONTENT_ID_URI_PATTERN
            = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final Uri LIVE_FOLDER_URI
            = Uri.parse(SCHEME + AUTHORITY + PATH_LIVE_FOLDER);
        
        public static int SAVE_NOTE_FLAG = 0;

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        public static String DEFAULT_SORT_ORDER = "modified DESC";

        public static final String COLUMN_NAME_TITLE = "title";

        public static final String COLUMN_NAME_NOTE = "note";

        public static final String COLUMN_NAME_CREATE_DATE = "created";

        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
        
        public static final String COLUMN_NAME_GROUP = "notegroup";
        
        public static final String[] MONTH = new String[]
	    {
	        "Jan","Feb","Mar","Apr","May","June","July","Aug","Sep","Oct","Nov","Dec"
	    };
        
        public static ActionBar actionbar;
        
        public static int NOTE_COUNT;
        
        public static boolean DELETE_FLAG = false;
        
        public static boolean SAVE_NONE_NOTE = false;
        
        public static boolean SAVE_NONE_FILE = false;
        
        public static boolean SAVE_FILE = false;
        
        public static boolean SDCARD_FULL;
        
        public static NoteAdapter ADAPTER;
        
        public static int DELETE_NUM;
               
        public static NoteDelete NOTE_DELETE;
       
        public static final String[] PROJECTION = new String[] {
	    	            NotePad.Notes._ID,
	    	            NotePad.Notes.COLUMN_NAME_NOTE,
	    	            NotePad.Notes.COLUMN_NAME_GROUP,
	    	            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
	    	            NotePad.Notes.COLUMN_NAME_CREATE_DATE};
        
        public static int NOTIFICATION_ID;
    }
}
