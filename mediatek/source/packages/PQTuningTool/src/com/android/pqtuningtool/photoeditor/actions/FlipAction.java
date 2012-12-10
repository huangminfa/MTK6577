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

import android.content.Context;
import android.util.AttributeSet;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.photoeditor.PhotoView;
import  com.android.pqtuningtool.photoeditor.filters.FlipFilter;

/**
 * An action handling flip effect.
 */
public class FlipAction extends EffectAction {

    private static final float DEFAULT_ANGLE = 0.0f;
    private static final float DEFAULT_FLIP_SPAN = 180.0f;

    private FlipFilter filter;
    private float horizontalFlipDegrees;
    private float verticalFlipDegrees;
    private Runnable queuedFlipChange;
    private FlipView flipView;

    public FlipAction(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void doBegin() {
        filter = new FlipFilter();

        flipView = factory.createFlipView();
        flipView.setOnFlipChangeListener(new FlipView.OnFlipChangeListener() {

            // Directly transform photo-view because running the flip filter isn't fast enough.
            PhotoView photoView = (PhotoView) flipView.getRootView().findViewById(
                    R.id.photo_view);

            @Override
            public void onAngleChanged(float horizontalDegrees, float verticalDegrees,
                    boolean fromUser) {
                if (fromUser) {
                    horizontalFlipDegrees = horizontalDegrees;
                    verticalFlipDegrees = verticalDegrees;
                    updateFlipFilter(false);
                    transformPhotoView(horizontalDegrees, verticalDegrees);
                }
            }

            @Override
            public void onStartTrackingTouch() {
                // no-op
            }

            @Override
            public void onStopTrackingTouch() {
                roundFlipDegrees();
                updateFlipFilter(false);
                transformPhotoView(horizontalFlipDegrees, verticalFlipDegrees);
                flipView.setFlippedAngles(horizontalFlipDegrees, verticalFlipDegrees);
            }

            private void transformPhotoView(final float horizontalDegrees,
                    final float verticalDegrees) {
                // Remove the outdated flip change before queuing a new one.
                if (queuedFlipChange != null) {
                    photoView.remove(queuedFlipChange);
                }
                queuedFlipChange = new Runnable() {

                    @Override
                    public void run() {
                        photoView.flipPhoto(horizontalDegrees, verticalDegrees);
                    }
                };
                photoView.queue(queuedFlipChange);
            }
        });
        flipView.setFlippedAngles(DEFAULT_ANGLE, DEFAULT_ANGLE);
        flipView.setFlipSpan(DEFAULT_FLIP_SPAN);
        horizontalFlipDegrees = 0;
        verticalFlipDegrees = 0;
        queuedFlipChange = null;
    }

    @Override
    public void doEnd() {
        flipView.setOnFlipChangeListener(null);
        // Round the current flip degrees in case flip tracking has not stopped yet.
        roundFlipDegrees();
        updateFlipFilter(true);
    }

    /**
     * Rounds flip degrees to multiples of 180 degrees.
     */
    private void roundFlipDegrees() {
        if (horizontalFlipDegrees % 180 != 0) {
            horizontalFlipDegrees = Math.round(horizontalFlipDegrees / 180) * 180;
        }
        if (verticalFlipDegrees % 180 != 0) {
            verticalFlipDegrees = Math.round(verticalFlipDegrees / 180) * 180;
        }
    }

    private void updateFlipFilter(boolean outputFilter) {
        // Flip the filter if the flipped degrees are at the opposite directions.
        filter.setFlip(((int) horizontalFlipDegrees / 180) % 2 != 0,
                ((int) verticalFlipDegrees / 180) % 2 != 0);
        notifyFilterChanged(filter, outputFilter);
    }
}
