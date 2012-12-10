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

import  com.android.pqtuningtool.app.GalleryApp;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class MediaSetTest extends AndroidTestCase {
    @SuppressWarnings("unused")
    private static final String TAG = "MediaSetTest";

    @SmallTest
    public void testComboAlbumSet() {
        GalleryApp app = new GalleryAppMock(null, null, null);
        Path.clearAll();
        DataManager dataManager = app.getDataManager();

        dataManager.addSource(new ComboSource(app));
        dataManager.addSource(new MockSource(app));

        MockSet set00 = new MockSet(Path.fromString("/mock/00"), dataManager, 0, 2000);
        MockSet set01 = new MockSet(Path.fromString("/mock/01"), dataManager, 1, 3000);
        MockSet set10 = new MockSet(Path.fromString("/mock/10"), dataManager, 2, 4000);
        MockSet set11 = new MockSet(Path.fromString("/mock/11"), dataManager, 3, 5000);
        MockSet set12 = new MockSet(Path.fromString("/mock/12"), dataManager, 4, 6000);

        MockSet set0 = new MockSet(Path.fromString("/mock/0"), dataManager, 7, 7000);
        set0.addMediaSet(set00);
        set0.addMediaSet(set01);

        MockSet set1 = new MockSet(Path.fromString("/mock/1"), dataManager, 8, 8000);
        set1.addMediaSet(set10);
        set1.addMediaSet(set11);
        set1.addMediaSet(set12);

        MediaSet combo = dataManager.getMediaSet("/combo/{/mock/0,/mock/1}");
        assertEquals(5, combo.getSubMediaSetCount());
        assertEquals(0, combo.getMediaItemCount());
        assertEquals("/mock/00", combo.getSubMediaSet(0).getPath().toString());
        assertEquals("/mock/01", combo.getSubMediaSet(1).getPath().toString());
        assertEquals("/mock/10", combo.getSubMediaSet(2).getPath().toString());
        assertEquals("/mock/11", combo.getSubMediaSet(3).getPath().toString());
        assertEquals("/mock/12", combo.getSubMediaSet(4).getPath().toString());

        assertEquals(10, combo.getTotalMediaItemCount());
    }
}
