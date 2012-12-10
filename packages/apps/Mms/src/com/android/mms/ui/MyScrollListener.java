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
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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
package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.CursorAdapter;
import android.widget.AbsListView.OnScrollListener;

import com.mediatek.xlog.Xlog;

public class MyScrollListener implements OnScrollListener {
    private static String TAG = "Mms/ScrollListener";
    private int HANDLE_FLING_THREAD_WAIT_TIME = 200;
    private String mThreadName = "ConversationList_Scroll_Tread";
    private int mMinCursorCount = 100;
    private Thread myThread = null;
    private boolean mNeedDestroy = false;
    private MyRunnable runnable = new MyRunnable(true);

    public MyScrollListener (int minCursorCount, String threadName) {
        mMinCursorCount = minCursorCount;
        mThreadName = threadName;
    }

    public void destroyThread() {
        synchronized (runnable) {
            Xlog.d(TAG, "destroy thread.");
            mNeedDestroy = true;
            runnable.setNeedRun(false);
            runnable.notifyAll();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        MessageCursorAdapter mlistAdapter = (MessageCursorAdapter) view.getAdapter();
        if (mlistAdapter != null && mlistAdapter.getCount() >= mMinCursorCount) {// run below code when threads' count more than 100.
            if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {// on touch
                Xlog.d(TAG, "OnScrollListener.onScrollStateChanged(): on touch state.");
                mlistAdapter.setIsScrolling(false);
            } else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) { // scrolling
                Xlog.d(TAG, "OnScrollListener.onScrollStateChanged(): scrolling...");
                mlistAdapter.setIsScrolling(true);
                synchronized (runnable) {
                    runnable.notifyAll();
                    runnable.setConversationListAdapter(mlistAdapter);
                    runnable.setNeedRun(true);
                }
                if (myThread == null) {
                    myThread = new Thread(runnable, mThreadName);
                    myThread.start();
                }
            } else if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {// not in scrolling
                Xlog.d(TAG, "OnScrollListener.onScrollStateChanged(): stop scrolling!");
                if (myThread != null) {
                    synchronized (runnable) {
                        runnable.setNeedRun(false);
                    }
                }
                mlistAdapter.setIsScrolling(false);
                mlistAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    private class MyRunnable implements Runnable {
        private boolean mNeedRun = true;
        MessageCursorAdapter mListAdapter = null;
        private int count = 0;
        private int bindViewTimes = 3;
        private int bindDefaultViewTimes = bindViewTimes - 1;

        public MyRunnable(boolean needRun) {
            mNeedRun = needRun;
        }

        public void setNeedRun(boolean needRun) {
            mNeedRun = needRun;
            if (!mNeedRun) {
                count = 0;
            }
        }
        public void setConversationListAdapter(MessageCursorAdapter listAdapter) {
            mListAdapter = listAdapter;
        }

        @Override
        public void run() {
            Object obj = new Object();
            while (!mNeedDestroy) {
                while(mNeedRun) {
                    Xlog.d(TAG, "OnScrollListener.run(): count=" + count);
                    /*
                     * percent of true: (bindViewTimes - 1) / bindViewTimes;
                     * percent of false: 1 / bindViewTimes.
                     */
                    if (count % bindViewTimes != bindDefaultViewTimes) {
                        mListAdapter.setIsScrolling(true);
                    } else {
                        mListAdapter.setIsScrolling(false);
                    }
                    count++;
                    if (mNeedDestroy) {
                        return;
                    }
                    synchronized (obj) {
                        try {
                            obj.wait(HANDLE_FLING_THREAD_WAIT_TIME);
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                    }
                }
                if (mNeedDestroy) {
                    return;
                }
                Xlog.d(TAG, "OnScrollListener.run(): listener is wait.");
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                }
            }
        }
    }
}
