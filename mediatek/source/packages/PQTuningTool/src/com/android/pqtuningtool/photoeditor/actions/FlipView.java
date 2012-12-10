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
import android.view.MotionEvent;

/**
 * View that handles touch-events to track flipping directions and angles.
 */
class FlipView extends FullscreenToolView {

    /**
     * Listens to flip changes.
     */
    public interface OnFlipChangeListener {

        void onAngleChanged(float horizontalDegrees, float verticalDegrees, boolean fromUser);

        void onStartTrackingTouch();

        void onStopTrackingTouch();
    }

    private static final float FIXED_DIRECTION_THRESHOLD = 20;

    private OnFlipChangeListener listener;
    private float maxFlipSpan;
    private float touchStartX;
    private float touchStartY;
    private float currentHorizontalDegrees;
    private float currentVerticalDegrees;
    private float lastHorizontalDegrees;
    private float lastVerticalDegrees;
    private boolean fixedDirection;
    private boolean fixedDirectionHorizontal;

    public FlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnFlipChangeListener(OnFlipChangeListener listener) {
        this.listener = listener;
    }

    public void setFlippedAngles(float horizontalDegrees, float verticalDegrees) {
        refreshAngle(horizontalDegrees, verticalDegrees, false);
    }

    /**
     * Sets allowed degrees for every flip before flipping the view.
     */
    public void setFlipSpan(float degrees) {
        // Flip-span limits allowed flipping degrees of every flip for usability purpose; the max.
        // flipped angles could be accumulated and larger than allowed flip-span.
        maxFlipSpan = degrees;
    }

    private float calculateAngle(boolean flipHorizontal, float x, float y) {
        // Use partial length along the moving direction to calculate the flip angle.
        float maxDistance = (flipHorizontal ? getWidth() : getHeight()) * 0.35f;
        float moveDistance = flipHorizontal ? (x - touchStartX) : (touchStartY - y);

        if (Math.abs(moveDistance) > maxDistance) {
            moveDistance = (moveDistance > 0) ? maxDistance : -maxDistance;

            if (flipHorizontal) {
                touchStartX = x - moveDistance;
            } else {
                touchStartY = moveDistance + y;
            }
        }
        return (moveDistance / maxDistance) * maxFlipSpan;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        if (isEnabled()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fixedDirection = false;
                    lastHorizontalDegrees = currentHorizontalDegrees;
                    lastVerticalDegrees = currentVerticalDegrees;
                    touchStartX = ev.getX();
                    touchStartY = ev.getY();

                    if (listener != null) {
                        listener.onStartTrackingTouch();
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Allow only one direction for flipping during movements, and make the
                    // direction fixed once it exceeds threshold.
                    float x = ev.getX();
                    float y = ev.getY();
                    boolean flipHorizontal = fixedDirection ? fixedDirectionHorizontal
                            : (Math.abs(x - touchStartX) >= Math.abs(y - touchStartY));
                    float degrees = calculateAngle(flipHorizontal, x, y);
                    if (!fixedDirection && (Math.abs(degrees) > FIXED_DIRECTION_THRESHOLD)) {
                        fixedDirection = true;
                        fixedDirectionHorizontal = flipHorizontal;
                    }

                    if (flipHorizontal) {
                        refreshAngle(lastHorizontalDegrees + degrees, lastVerticalDegrees, true);
                    } else {
                        refreshAngle(lastHorizontalDegrees, lastVerticalDegrees + degrees, true);
                    }
                   break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (listener != null) {
                        listener.onStopTrackingTouch();
                    }
                    break;
            }
        }
        return true;
    }

    private void refreshAngle(float horizontalDegrees, float verticalDegrees, boolean fromUser) {
        currentHorizontalDegrees = horizontalDegrees;
        currentVerticalDegrees = verticalDegrees;
        if (listener != null) {
            listener.onAngleChanged(horizontalDegrees, verticalDegrees, fromUser);
        }
    }
}
