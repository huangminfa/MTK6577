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

package  com.android.pqtuningtool.ui;

import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.data.Path;

import android.graphics.Rect;

/**
 * Drawer class responsible for drawing selectable frame.
 */
public abstract class SelectionDrawer {
    public static final int DATASOURCE_TYPE_NOT_CATEGORIZED = 0;
    public static final int DATASOURCE_TYPE_LOCAL = 1;
    public static final int DATASOURCE_TYPE_PICASA = 2;
    public static final int DATASOURCE_TYPE_MTP = 3;
    public static final int DATASOURCE_TYPE_CAMERA = 4;

    public abstract void prepareDrawing();
    public abstract void draw(GLCanvas canvas, Texture content,
            int width, int height, int rotation, Path path,
            int dataSourceType, int mediaType, /* boolean isPanorama */ int subType,
            int labelBackgroundHeight, boolean wantCache, boolean isCaching);
    public abstract void drawFocus(GLCanvas canvas, int width, int height);

    public void draw(GLCanvas canvas, Texture content, int width, int height,
            int rotation, Path path, int mediaType, /* boolean isPanorama */ int subType) {
        draw(canvas, content, width, height, rotation, path,
                DATASOURCE_TYPE_NOT_CATEGORIZED, mediaType, /* isPanorama */ subType,
                0, false, false);
    }

    public static void drawWithRotation(GLCanvas canvas, Texture content,
            int x, int y, int width, int height, int rotation) {
        if (rotation != 0) {
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            canvas.rotate(rotation, 0, 0, 1);
        }

        content.draw(canvas, x, y, width, height);

        if (rotation != 0) {
            canvas.restore();
        }
    }

    public static void drawFrame(GLCanvas canvas, NinePatchTexture frame,
            int x, int y, int width, int height) {
        Rect p = frame.getPaddings();
        frame.draw(canvas, x - p.left, y - p.top, width + p.left + p.right,
                 height + p.top + p.bottom);
    }
}
