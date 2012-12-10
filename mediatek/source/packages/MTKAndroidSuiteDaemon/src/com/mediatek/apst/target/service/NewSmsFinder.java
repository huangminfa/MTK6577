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

package com.mediatek.apst.target.service;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.util.entity.message.Sms;

public abstract class NewSmsFinder extends Thread {
    //==============================================================
    // Constants                                                    
    //==============================================================
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private List<Clue> mFindList = new ArrayList<Clue>();
    
    private boolean mShouldTerminate = false;
    
    private long mLastSmsTimeStamp = 0;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public boolean isShouldTerminate(){
        return this.mShouldTerminate;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getClassName(){
        return "NewSmsFinder";
    }
    
    public void terminate(){
        mShouldTerminate = true;
    }
    
    /**
     * Override it to implement the SMS finding logic.
     * @param date
     * @param address
     * @param body
     * @param box
     * @return
     */
    public abstract Sms findSms(long date, String address, 
            String body, int box);
    
    public synchronized void appendTask(Clue toFind){
        mFindList.add(toFind);
    }
    
    @Override
    public void run(){
        while (!isShouldTerminate()){
            if (mFindList.size() > 0){
                Clue toFind = mFindList.remove(0);
                if (null != toFind){
                    // Retry 30 times
                    int retry = 30;
                    boolean found = false;
                    while (retry > 0){
                        retry--;
                        // Important when receiving multiple SMS in a short 
                        // time
                        if (toFind.getDate() < mLastSmsTimeStamp){
                            toFind.setDate(mLastSmsTimeStamp);
                        }
                        Sms newSms = findSms(
                                toFind.getDate(), 
                                toFind.getAddress(), 
                                toFind.getBody(), 
                                toFind.getBox());
                        if (null != newSms){
                            mLastSmsTimeStamp = newSms.getDate();
                            found = true;
                            break;
                        }
                        
                        try {
                            sleep(100L);
                        } catch (InterruptedException e) {
                            Debugger.logE(this.getClassName(), "run", 
                                    null, null, e);
                        }
                    }
                    if (!found){
                        Debugger.logW(this.getClassName(), "run", null, 
                                "Fail to find the new SMS.");
                    }
                }
            }

            try {
                sleep(2000L);
            } catch (InterruptedException e) {
                Debugger.logE(this.getClassName(), "run", null, null, e);
            }
        }
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    public static class Clue {
        
        private long date;
        private String address;
        private String body;
        private int box;
        
        public Clue(long date, String address, String body, int box){
            this.date = date;
            this.address = address;
            this.body = body;
            this.box = box;
        }
        
        public long getDate(){
            return this.date;
        }
        
        public String getAddress(){
            return this.address;
        }
        
        public String getBody(){
            return this.body;
        }
        
        public int getBox(){
            return this.box;
        }
        
        public void setDate(long date){
            this.date = date;
        }

        public void setAddress(String address){
            this.address = address;
        }

        public void setBody(String body){
            this.body = body;
        }

        public void setBox(int box){
            this.box = box;
        }
        
    }
    
}
