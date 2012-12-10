/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

package com.mediatek.android.content;

import com.mediatek.apst.target.util.Debugger;

import android.database.Cursor;

public abstract class AsyncCursorParser {
    //==============================================================
    // Constants                                                    
    //==============================================================
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private Cursor mCursor;
    
    private int mCount;
    
    private int mPosition;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public AsyncCursorParser(Cursor c){
        this.mCursor = c;
        mPosition = 0;
        mCount = (null == c) ? 0 : c.getCount();
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public Cursor getCursor(){
        return mCursor;
    }

    public int getCount(){
        return mCount;
    }
    
    public int getPosition(){
        return mPosition;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    protected void setCursor(Cursor c){
        this.mCursor = c;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public int getCursorPosition(){
        if (null != mCursor){
            return mCursor.getPosition();
        } else {
            return -1;
        }
    }
    
    public boolean moveToNext(){
        if (null != mCursor){
            return mCursor.moveToNext();
        } else {
            return false;
        }
    }
    
    public boolean moveToPrevious(){
        if (null != mCursor){
            return mCursor.moveToPrevious();
        } else {
            return false;
        }
    }
    
    
    public void resetCursor(Cursor c){
        if (null != mCursor && !mCursor.isClosed()){
            mCursor.close();
            Debugger.logD(new Object[]{c}, "Cursor Closed!");
        }
        mCursor = c;
        mPosition = 0;
        mCount = (null == c) ? 0 : c.getCount();
    }
    
    /**
     * Call it to do the parse work.
     */
    public void parse(){
        Debugger.logI("Parse begin...");
        if (null == mCursor){
            Debugger.logW("Curosr is null.");
            return;
        }
        
        onParseStart();
        
        try {
            while (mCursor.moveToNext()) {
                ++mPosition;
                onNewRow(mCursor);
                if (isBlockReady()) {
                    onBlockReady();
                }
            }
        } catch (IllegalStateException e) {
            Debugger.logE(new Object[] {}, ">>>>>>>>>>Catched IllegalStateException!");
            onBlockReadyForEx();            
            e.printStackTrace();            
        } finally {
            onParseOver();
            Debugger.logI("Parse finished.");
        }     
    }
    
    /**
     * Override it
     */
    protected abstract void onParseStart();
    
    /**
     * Override it
     * @param c Cursor to parse
     */
    protected abstract void onNewRow(Cursor c);
    
    /**
     * Override it
     * @return Is block ready
     */
    public abstract boolean isBlockReady();
    
    /**
     * Override it
     */
    protected abstract void onBlockReady();
    
    /**
     * Override it
     * Add by Yu, catch exception then stop parse
     */
    protected abstract void onBlockReadyForEx();
    
    /**
     * Override it
     */
    protected abstract void onParseOver();
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
