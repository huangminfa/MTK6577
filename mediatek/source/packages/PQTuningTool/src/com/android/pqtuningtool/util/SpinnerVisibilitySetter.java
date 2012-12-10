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
 * Copyright (C) 2011 The Android Open Source Project
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

package  com.android.pqtuningtool.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.util.WeakHashMap;

/**
 * This class manages the visibility of the progress spinner in the action bar for an
 * Activity. It filters out short-lived appearances of the progress spinner by only
 * showing the spinner if it hasn't been hidden again before the end of a specified
 * delay period. It also enforces a minimum display time once the spinner is made visible.
 * This meant to cut down on the frequent "flashes" of the progress spinner.
 */
public class SpinnerVisibilitySetter {

    private static final int MSG_SHOW_SPINNER = 1;
    private static final int MSG_HIDE_SPINNER = 2;

    // Amount of time after a show request that the progress spinner is actually made visible.
    // This means that any show/hide requests that happen subsequently within this period
    // of time will be ignored.
    private static final long SPINNER_DISPLAY_DELAY = 1000;

    // The minimum amount of time the progress spinner must be visible before it can be hidden.
    private static final long MIN_SPINNER_DISPLAY_TIME = 2000;

    static final WeakHashMap<Activity, SpinnerVisibilitySetter> sInstanceMap =
            new WeakHashMap<Activity, SpinnerVisibilitySetter>();

    private long mSpinnerVisibilityStartTime = -1;
    private Activity mActivity;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SHOW_SPINNER:
                    removeMessages(MSG_SHOW_SPINNER);
                    if (mSpinnerVisibilityStartTime >= 0) break;
                    mSpinnerVisibilityStartTime = SystemClock.elapsedRealtime();
                    mActivity.setProgressBarIndeterminateVisibility(true);
                    break;
                case MSG_HIDE_SPINNER:
                    removeMessages(MSG_HIDE_SPINNER);
                    if (mSpinnerVisibilityStartTime < 0) break;
                    long t = SystemClock.elapsedRealtime() - mSpinnerVisibilityStartTime;
                    if (t >= MIN_SPINNER_DISPLAY_TIME) {
                        mSpinnerVisibilityStartTime = -1;
                        mActivity.setProgressBarIndeterminateVisibility(false);
                    } else {
                        sendEmptyMessageDelayed(MSG_HIDE_SPINNER, MIN_SPINNER_DISPLAY_TIME - t);
                    }
                    break;
            }
        }
    };

    /**
     *  Gets the <code>SpinnerVisibilitySetter</code> for the given <code>activity</code>.
     *
     *  This method must be called from the main thread.
     */
    public static SpinnerVisibilitySetter getInstance(Activity activity) {
        synchronized(sInstanceMap) {
            SpinnerVisibilitySetter setter = sInstanceMap.get(activity);
            if (setter == null) {
                setter = new SpinnerVisibilitySetter(activity);
                sInstanceMap.put(activity, setter);
            }
            return setter;
        }
    }

    private SpinnerVisibilitySetter(Activity activity) {
        mActivity = activity;
    }

    public void setSpinnerVisibility(boolean visible) {
        if (visible) {
            mHandler.removeMessages(MSG_HIDE_SPINNER);
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_SPINNER, SPINNER_DISPLAY_DELAY);
        } else {
            mHandler.removeMessages(MSG_SHOW_SPINNER);
            mHandler.sendEmptyMessage(MSG_HIDE_SPINNER);
        }
    }
}
