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
package  com.android.pqtuningtool.data;

import  com.android.pqtuningtool.common.Entry;
import  com.android.pqtuningtool.common.EntrySchema;


@Entry.Table("download")
public class DownloadEntry extends Entry {
    public static final EntrySchema SCHEMA = new EntrySchema(DownloadEntry.class);

    public static interface Columns extends Entry.Columns {
        public static final String HASH_CODE = "hash_code";
        public static final String CONTENT_URL = "content_url";
        public static final String CONTENT_SIZE = "_size";
        public static final String ETAG = "etag";
        public static final String LAST_ACCESS = "last_access";
        public static final String LAST_UPDATED = "last_updated";
        public static final String DATA = "_data";
    }

    @Column(value = "hash_code", indexed = true)
    public long hashCode;

    @Column("content_url")
    public String contentUrl;

    @Column("_size")
    public long contentSize;

    @Column("etag")
    public String eTag;

    @Column(value = "last_access", indexed = true)
    public long lastAccessTime;

    @Column(value = "last_updated")
    public long lastUpdatedTime;

    @Column("_data")
    public String path;

    @Override
    public String toString() {
        // Note: THIS IS REQUIRED. We used all the fields here. Otherwise,
        //       ProGuard will remove these UNUSED fields. However, these
        //       fields are needed to generate database.
        return new StringBuilder()
                .append("hash_code: ").append(hashCode).append(", ")
                .append("content_url").append(contentUrl).append(", ")
                .append("_size").append(contentSize).append(", ")
                .append("etag").append(eTag).append(", ")
                .append("last_access").append(lastAccessTime).append(", ")
                .append("last_updated").append(lastUpdatedTime).append(",")
                .append("_data").append(path)
                .toString();
    }
}
