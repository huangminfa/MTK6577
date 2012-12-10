/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
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

package com.android.music;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupMenu.OnDismissListener;

public class FakeMenu {

    private static final String TAG = "FakeMenu";
    private Activity mActivity = null;
    private View mOverflowMenuButton;
    private PopupMenu mLastPopupMenu = null;
    private Boolean mIsPopupMenuShowing = false;
    private Boolean mLastPopupMenuState = false;
    private Boolean mHasMenukey = true;
    
    public FakeMenu(Activity activity)
    {
        mActivity = activity;
        mHasMenukey = ViewConfiguration.get(mActivity).hasPermanentMenuKey();
        //mHasMenukey = false;
        MusicLogUtils.d(TAG, "FakeMenu: mHasMenukey = " + mHasMenukey);
        createFakeMenu();
    }
    
    private void createFakeMenu() {
        if (mActivity == null || mHasMenukey) {
            MusicLogUtils.d(TAG, "createFakeMenu Quit when thers has Menu Key");
            return;
        }
        mOverflowMenuButton = mActivity.findViewById(R.id.overflow_menu);
        mOverflowMenuButton.setVisibility(View.VISIBLE);
        View parent = (View)mOverflowMenuButton.getParent();
        if (parent != null) {
            parent.setVisibility(View.VISIBLE);
        }
        mOverflowMenuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MusicLogUtils.d(TAG, "createFakeMenu:onClick()");
                if (v.getId() == R.id.overflow_menu) {
                    final PopupMenu popupMenu = new PopupMenu(mActivity, mOverflowMenuButton);
                    mLastPopupMenu = popupMenu;
                    final Menu menu = popupMenu.getMenu();
                    mActivity.onCreateOptionsMenu(menu);
                    popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            return mActivity.onOptionsItemSelected(item);
                        }
                    });
                    popupMenu.setOnDismissListener(new OnDismissListener() {
                        public void onDismiss(PopupMenu menu) {
                            mIsPopupMenuShowing = false;
                            MusicLogUtils.d(TAG, "createFakeMenu:onDismiss() called");
                            return ;
                        }
                    });
                    mActivity.onPrepareOptionsMenu(menu);
                    mIsPopupMenuShowing = true;
                    if (popupMenu != null) {
                        MusicLogUtils.d(TAG, "createFakeMenu:popupMenu.show()");
                        popupMenu.show();
                    }
                }
            }
        });
    }

    // snapshot the PopMenu status for config change.
    public void snapshotFakeMenu() {
        if (mHasMenukey) {
            return;
        }
        mLastPopupMenuState = mIsPopupMenuShowing;
        MusicLogUtils.d(TAG, "recordState:mLastPopupMenuState=" + mLastPopupMenuState);
    }

    // change the FakeMenu when config is change.
    public void changeFakeMenu() {
        if (mHasMenukey) {
            return;
        }        
        final Boolean popupMenuShowing = mLastPopupMenuState;
        if (popupMenuShowing) {
            if (mLastPopupMenu != null) {
                mLastPopupMenu.dismiss();
                MusicLogUtils.d(TAG, "changeFakeMenu:mLastPopupMenu.dismiss()");
            }
        }
        MusicLogUtils.d(TAG, "changeFakeMenu:popupMenuShowing=" + popupMenuShowing);
        createFakeMenu();
        if (popupMenuShowing) {
            MusicLogUtils.d(TAG, "changeFakeMenu:performClick()");
            if (mOverflowMenuButton != null) {
                mOverflowMenuButton.performClick();
            }
        }
    }
}
