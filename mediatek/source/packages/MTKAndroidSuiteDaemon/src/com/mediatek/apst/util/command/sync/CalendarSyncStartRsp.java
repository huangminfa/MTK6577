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

package com.mediatek.apst.util.command.sync;

import com.mediatek.apst.util.command.ResponseCommand;

/**
 * Class Name: CalendarSyncStartRsp
 * <p>Package: com.mediatek.apst.util.command.sync
 * <p>Created on: 2011-05-25
 * <p>
 * <p>Description: 
 * <p>Response for starting a calendar sync. This response tell which type of 
 * sync to do next, either slow sync or fast sync.
 * <p>
 * @author mtk81022 Shaoying Han
 * @version V1.0
 */
public class CalendarSyncStartRsp extends ResponseCommand {
	//==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private boolean syncNeedReinit;
    
    private long lastSyncDate;
    
    private long localAccountId;
    
    private boolean isSyncAble;

	//==============================================================
    // Constructors                                                 
    //==============================================================
    public CalendarSyncStartRsp(int token){
        super(FEATURE_CALENDAR_SYNC, token);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    /**
     * Whether need to do slow sync. If not, we should do fast sync.
     * @return True if it need to do slow sync, false to do fast sync.
     */
    public boolean isSyncNeedReinit(){
        return syncNeedReinit;
    }
    
    /**
     * Gets the date of last sync.
     * @return The date of last sync.
     */
    public long getLastSyncDate(){
        return lastSyncDate;
    }
    
    public boolean isSyncAble() {
		return isSyncAble;
	}

	public long getLocalAccountId() {
		return localAccountId;
	}

    //==============================================================
    // Setters                                                      
    //==============================================================
    /**
     * Sets whether need to do slow sync. If set not, we should do fast sync.
     * @param syncNeedReinit Set true if it need to do slow sync, false to do 
     * fast sync.
     */
    public void setSyncNeedReinit(boolean syncNeedReinit){
        this.syncNeedReinit = syncNeedReinit;
    }
    
    /**
     * Sets the date of last sync.
     * @param lastSyncDate The date of last sync.
     */
    public void setLastSyncDate(long lastSyncDate){
        this.lastSyncDate = lastSyncDate;
    }
    
    public void setSyncAble(boolean isSyncAble) {
		this.isSyncAble = isSyncAble;
	}
    
    public void setLocalAccountId(long localAccountId) {
		this.localAccountId = localAccountId;
	}
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
