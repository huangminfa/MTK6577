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

package  com.android.pqtuningtool.photoeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import  com.android.pqtuningtool.R;

/**
 * Effects menu that contains toggles mapping to corresponding groups of effects.
 */
public class EffectsMenu extends RestorableView {

    /**
     * Listener of toggle changes.
     */
    public interface OnToggleListener {

        /**
         * Listens to the selected status and mapped effects-id of the clicked toggle.
         *
         * @return true to make the toggle selected; otherwise, make it unselected.
         */
        boolean onToggle(boolean isSelected, int effectsId);
    }

    public EffectsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int childLayoutId() {
        return R.layout.photoeditor_effects_menu;
    }

    public void setOnToggleListener(OnToggleListener listener) {
        setToggleRunnalbe(listener, R.id.exposure_button, R.layout.photoeditor_effects_exposure);
        setToggleRunnalbe(listener, R.id.artistic_button, R.layout.photoeditor_effects_artistic);
        setToggleRunnalbe(listener, R.id.color_button, R.layout.photoeditor_effects_color);
        setToggleRunnalbe(listener, R.id.fix_button, R.layout.photoeditor_effects_fix);
    }

    private void setToggleRunnalbe(final OnToggleListener listener, final int toggleId,
            final int effectsId) {
        setClickRunnable(toggleId, new Runnable() {

            @Override
            public void run() {
                boolean selected = findViewById(toggleId).isSelected();
                setViewSelected(toggleId, listener.onToggle(selected, effectsId));
            }
        });
    }

    public void clearSelected() {
        ViewGroup menu = (ViewGroup) findViewById(R.id.toggles);
        for (int i = 0; i < menu.getChildCount(); i++) {
            View toggle = menu.getChildAt(i);
            if (toggle.isSelected()) {
                setViewSelected(toggle.getId(), false);
            }
        }
    }
}
