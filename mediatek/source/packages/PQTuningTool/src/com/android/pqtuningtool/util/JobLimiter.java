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

import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.util.ThreadPool.Job;
import  com.android.pqtuningtool.util.ThreadPool.JobContext;

import java.util.LinkedList;

// Limit the number of concurrent jobs that has been submitted into a ThreadPool
@SuppressWarnings("rawtypes")
public class JobLimiter implements FutureListener {
    private static final String TAG = "JobLimiter";

    // State Transition:
    //      INIT -> DONE, CANCELLED
    //      DONE -> CANCELLED
    private static final int STATE_INIT = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_CANCELLED = 2;

    private final LinkedList<JobWrapper<?>> mJobs = new LinkedList<JobWrapper<?>>();
    private final ThreadPool mPool;
    private int mLimit;

    private static class JobWrapper<T> implements Future<T>, Job<T> {
        private int mState = STATE_INIT;
        private Job<T> mJob;
        private Future<T> mDelegate;
        private FutureListener<T> mListener;
        private T mResult;

        public JobWrapper(Job<T> job, FutureListener<T> listener) {
            mJob = job;
            mListener = listener;
        }

        public synchronized void setFuture(Future<T> future) {
            if (mState != STATE_INIT) return;
            mDelegate = future;
        }

        @Override
        public void cancel() {
            FutureListener<T> listener = null;
            synchronized (this) {
                if (mState != STATE_DONE) {
                    listener = mListener;
                    mJob = null;
                    mListener = null;
                    if (mDelegate != null) {
                        mDelegate.cancel();
                        mDelegate = null;
                    }
                }
                mState = STATE_CANCELLED;
                mResult = null;
                notifyAll();
            }
            if (listener != null) listener.onFutureDone(this);
        }

        @Override
        public synchronized boolean isCancelled() {
            return mState == STATE_CANCELLED;
        }

        @Override
        public boolean isDone() {
            // Both CANCELLED AND DONE is considered as done
            return mState !=  STATE_INIT;
        }

        @Override
        public synchronized T get() {
            while (mState == STATE_INIT) {
                // handle the interrupted exception of wait()
                Utils.waitWithoutInterrupt(this);
            }
            return mResult;
        }

        @Override
        public void waitDone() {
            get();
        }

        @Override
        public T run(JobContext jc) {
            Job<T> job = null;
            synchronized (this) {
                if (mState == STATE_CANCELLED) return null;
                job = mJob;
            }
            T result  = null;
            try {
                result = job.run(jc);
            } catch (Throwable t) {
                Log.w(TAG, "error executing job: " + job, t);
            }
            FutureListener<T> listener = null;
            synchronized (this) {
                if (mState == STATE_CANCELLED) return null;
                mState = STATE_DONE;
                listener = mListener;
                mListener = null;
                mJob = null;
                mResult = result;
                notifyAll();
            }
            if (listener != null) listener.onFutureDone(this);
            return result;
        }
    }

    public JobLimiter(ThreadPool pool, int limit) {
        mPool = Utils.checkNotNull(pool);
        mLimit = limit;
    }

    public synchronized <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        JobWrapper<T> future = new JobWrapper<T>(Utils.checkNotNull(job), listener);
        mJobs.addLast(future);
        submitTasksIfAllowed();
        return future;
    }

    public <T> Future<T> submit(Job<T> job) {
        return submit(job, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void submitTasksIfAllowed() {
        while (mLimit > 0 && !mJobs.isEmpty()) {
            JobWrapper wrapper = mJobs.removeFirst();
            if (!wrapper.isCancelled()) {
                --mLimit;
                wrapper.setFuture(mPool.submit(wrapper, this));
            }
        }
    }

    @Override
    public synchronized void onFutureDone(Future future) {
        ++mLimit;
        submitTasksIfAllowed();
    }
}
