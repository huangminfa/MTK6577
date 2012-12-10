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

package  com.android.pqtuningtool.photoeditor.actions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.photoeditor.PhotoView;

/**
 * Factory to create tools that will be used by effect actions.
 */
public class EffectToolFactory {

    public enum ScalePickerType {
        LIGHT, SHADOW, COLOR, GENERIC
    }

    private final ViewGroup effectToolPanel;
    private final LayoutInflater inflater;

    public EffectToolFactory(ViewGroup effectToolPanel, LayoutInflater inflater) {
        this.effectToolPanel = effectToolPanel;
        this.inflater = inflater;
    }

    private View createFullscreenTool(int toolId) {
        // Create full screen effect tool on top of photo-view and place it within the same
        // view group that contains photo-view.
        View photoView = effectToolPanel.getRootView().findViewById(R.id.photo_view);
        ViewGroup parent = (ViewGroup) photoView.getParent();
        FullscreenToolView view = (FullscreenToolView) inflater.inflate(toolId, parent, false);
        view.setPhotoBounds(((PhotoView) photoView).getPhotoBounds());
        parent.addView(view, parent.indexOfChild(photoView) + 1);
        return view;
    }

    private View createPanelTool(int toolId) {
        View view = inflater.inflate(toolId, effectToolPanel, false);
        effectToolPanel.addView(view, 0);
        return view;
    }

    private int getScalePickerBackground(ScalePickerType type) {
        switch (type) {
            case LIGHT:
                return R.drawable.photoeditor_scale_seekbar_light;

            case SHADOW:
                return R.drawable.photoeditor_scale_seekbar_shadow;

            case COLOR:
                return R.drawable.photoeditor_scale_seekbar_color;
        }
        return R.drawable.photoeditor_scale_seekbar_generic;
    }

    public ScaleSeekBar createScalePicker(ScalePickerType type) {
        ScaleSeekBar scalePicker = (ScaleSeekBar) createPanelTool(
                R.layout.photoeditor_scale_seekbar);
        scalePicker.setBackgroundResource(getScalePickerBackground(type));
        return scalePicker;
    }

    public ColorSeekBar createColorPicker() {
        return (ColorSeekBar) createPanelTool(R.layout.photoeditor_color_seekbar);
    }

    public DoodleView createDoodleView() {
        return (DoodleView) createFullscreenTool(R.layout.photoeditor_doodle_view);
    }

    public TouchView createTouchView() {
        return (TouchView) createFullscreenTool(R.layout.photoeditor_touch_view);
    }

    public FlipView createFlipView() {
        return (FlipView) createFullscreenTool(R.layout.photoeditor_flip_view);
    }

    public RotateView createRotateView() {
        return (RotateView) createFullscreenTool(R.layout.photoeditor_rotate_view);
    }

    public CropView createCropView() {
        return (CropView) createFullscreenTool(R.layout.photoeditor_crop_view);
    }
}
