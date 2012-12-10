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
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import  com.android.pqtuningtool.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Toolbar that contains all tools and controls their idle/awake behaviors from UI thread.
 */
public class Toolbar extends RelativeLayout {

    private final ToolbarIdleHandler idleHandler;

    public Toolbar(Context context, AttributeSet attrs) {
        super(context, attrs);

        idleHandler = new ToolbarIdleHandler(context);
        setOnHierarchyChangeListener(idleHandler);
        idleHandler.killIdle();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        idleHandler.killIdle();
        return super.dispatchTouchEvent(ev);
    }

    private static class ToolbarIdleHandler implements OnHierarchyChangeListener {

        private static final int MAKE_IDLE = 1;
        private static final int TIMEOUT_IDLE = 8000;

        private final List<View> childViews = new ArrayList<View>();
        private final Handler mainHandler;
        private final Animation fadeIn;
        private final Animation fadeOut;
        private boolean idle;

        public ToolbarIdleHandler(Context context) {
            mainHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MAKE_IDLE:
                            if (!idle) {
                                idle = true;
                                for (View view : childViews) {
                                    view.startAnimation(fadeOut);
                                }
                            }
                            break;
                    }
                }
            };

            fadeIn = AnimationUtils.loadAnimation(context, R.anim.photoeditor_fade_in);
            fadeOut = AnimationUtils.loadAnimation(context, R.anim.photoeditor_fade_out);
        }

        public void killIdle() {
            mainHandler.removeMessages(MAKE_IDLE);
            if (idle) {
                idle = false;
                for (View view : childViews) {
                    view.startAnimation(fadeIn);
                }
            }
            mainHandler.sendEmptyMessageDelayed(MAKE_IDLE, TIMEOUT_IDLE);
        }

        @Override
        public void onChildViewAdded(View parent, View child) {
            // All child views, except photo-view, will fade out on inactivity timeout.
            if (child.getId() != R.id.photo_view) {
                childViews.add(child);
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            childViews.remove(child);
        }
    }
}
