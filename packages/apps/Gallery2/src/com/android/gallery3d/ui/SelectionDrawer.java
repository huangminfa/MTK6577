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

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;

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

    //added for stereo display feature
    public static final int DATASOURCE_VIRTUAL_STEREO = 5;

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

        // Make sure we begin to draw content at the left-upper corner on the canvas
        // even if it was just rotated,
        // or there may exist 1-pixel inaccuracy as a layout result.
        // See ALPS00240415 as an example of such layout error
        if (width % 2 != 0) {
            switch (rotation / 90 % 4) {
            case 1:
                y -= 1;
                break;
            case 2:
                x -= 1;
                y -= 1;
                break;
            case 3:
                x -= 1;
                break;
            default:
                break;
            }
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
