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

package com.android.FunctionTest;

public class CommandResult {
    long mTimeConsumed = 0;
    boolean mSucceed = false;
    TimeMeasurer mTimeMeasurer = null;
    private boolean mTimeout = false;
    private String mName = null;
    
    private class TimeMeasurer {
        private long mStartTime;
        private long mStopTime;

        public void startMeasure() {
            mStartTime = System.currentTimeMillis();
        }
        
        public void stopMeasure() {
            mStopTime = System.currentTimeMillis();
        }
        
        public long getStartTime() {
            return mStartTime;
        }
        
        public long getTimeConsumed() {
            return (mStopTime - mStartTime);
        }
        
        public void reset() {
            mStartTime = mStopTime = 0;
        }
    }
    
    public CommandResult() {
        mTimeMeasurer = new TimeMeasurer();
        mTimeMeasurer.startMeasure();
        mName = null;
    }

    public CommandResult(String name) {
        mTimeMeasurer = new TimeMeasurer();
        mTimeMeasurer.startMeasure();
        mName = name;
    }
    
    public void setResult(boolean result) {
        mTimeMeasurer.stopMeasure();
        mSucceed = result;
        if (result) {
            mTimeout = false;
        }
    }

    public void setResult(boolean result, boolean timeout) {
        mTimeMeasurer.stopMeasure();
        mSucceed = result;
        mTimeout = timeout;
    }
    
    public boolean getResult() {
        return mSucceed;
    }
    
    public long getStartTime() {
        return mTimeMeasurer.getStartTime();
    }
    
    public long getTimeConsumed() {
        return mTimeMeasurer.getTimeConsumed();
    }

    public boolean isCommandTimeout() {
        return mTimeout;
    }
    
    public String getName() {
        return mName;
    }
}
