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
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.ViewSwitcher;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.util.MtkLog;

/**
 * Action bar that contains buttons such as undo, redo, save, etc.
 */
public class ActionBar extends RestorableView {

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int childLayoutId() {
        return R.layout.photoeditor_actionbar;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // Show the action-bar title only when there's still room for it; otherwise, hide it.
        int width = 0;
        for (int i = 0; i < getChildCount(); i++) {
            width += getChildAt(i).getWidth();
        }
        findViewById(R.id.action_bar_title).setVisibility(((width > r - l)) ? INVISIBLE: VISIBLE);
        MtkLog.i("ActionBar", "onLayout " + width + r + l);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateButtons(false, false);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showSaveOrShare();
        MtkLog.i("ActionBar", "onConfigurationChanged ");
    }

    /**
     * Save/share button may need being switched when undo/save enabled status is changed/restored.
     */
    private void showSaveOrShare() {
        // Show share-button only after photo is edited and saved; otherwise, show save-button.
        boolean showShare = findViewById(R.id.undo_button).isEnabled()
                && !findViewById(R.id.save_button).isEnabled();
        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.save_share_buttons);
        int next = switcher.getNextView().getId();
        if ((showShare && (next == R.id.share_button))
                || (!showShare && (next == R.id.save_button))) {
            switcher.showNext();
        }
    }

    public void updateButtons(boolean canUndo, boolean canRedo) {
        setViewEnabled(R.id.undo_button, canUndo);
        setViewEnabled(R.id.redo_button, canRedo);
        setViewEnabled(R.id.save_button, canUndo);
        showSaveOrShare();
    }

    public void updateSave(boolean canSave) {
        setViewEnabled(R.id.save_button, canSave);
        showSaveOrShare();
    }

    public void clickBack() {
        findViewById(R.id.action_bar_back).performClick();
    }

    public void clickSave() {
        findViewById(R.id.save_button).performClick();
    }

    public boolean canSave() {
        return findViewById(R.id.save_button).isEnabled();
    }
}
