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

package com.mediatek.apst.util.command.backup;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.mediatek.apst.util.command.RawBlockResponse;
import com.mediatek.apst.util.command.app.AsyncGetAllAppInfoRsp;
import com.mediatek.apst.util.entity.app.ApplicationInfo;

public class GetAppForBackupRsp extends RawBlockResponse {
	 //==============================================================
    // Constants
    //==============================================================

    private static final long serialVersionUID = 2L;

    //==============================================================
    // Fields
    //==============================================================
    //==============================================================
    // Constructors
    //==============================================================
    public GetAppForBackupRsp(int requestToken) {
        super(FEATURE_BACKUP, requestToken);
    }

    //==============================================================
    // Getters
    //==============================================================
    //==============================================================
    // Setters
    //==============================================================
    //==============================================================
    // Methods
    //==============================================================
    /**
     * Gets the applications info list.
     * @return The applications info list.
     */
    public ArrayList<ApplicationInfo> getResults(){
        ByteBuffer buffer = ByteBuffer.wrap(this.getRaw());
        int count = buffer.getInt();
        ArrayList<ApplicationInfo> results = 
            new ArrayList<ApplicationInfo>(count);
        for (int i = 0; i < count; i++){
            ApplicationInfo item = new ApplicationInfo();
            item.readRaw(buffer);
            results.add(item);
        }
        return results;
    }
    
    public static Builder builder(int rawBlockSize){
        return new Builder(rawBlockSize);
    }
    
    public static Builder builder(){
        return new Builder();
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    /**
     * Class Name: GetAppForBackupRsp.Builder
     * <p>Package: com.mediatek.apst.util.command.backup
     * <p>Created on: 2012-4-11
     * <p>
     * <p>Description: 
     * <p>Builder class help to create and fill a GetAppForBackupRsp.
     * <p>
     * @author mtk54045 jing.jia
     * @version V1.0
     */
    public static class Builder extends RawBlockResponse.Builder {

        private int count;
        
        protected Builder(int rawBlockSize){
            super(rawBlockSize, FEATURE_BACKUP);
            this.count = 0;
        }
        
        protected Builder(){
            super(FEATURE_BACKUP);
            this.count = 0;
        }
        
        //@Override
        protected RawBlockResponse onCreateCommand(int featureId){
            return new GetAppForBackupRsp(-1);
        }
        
        /**
         * Append an application info into the raw bytes buffer.
         * @param appInfo The application info object to append.
         * @return The Builder instance itself.
         * @throws BufferOverflowException
         */
        public Builder appendAppInfo(ApplicationInfo appInfo) 
        throws BufferOverflowException {
            buffer().mark();
            try {
                if (0 == count){
                    // Reserve 4 bytes to store the count
                    buffer().putInt(0);
                }
                appInfo.writeRaw(buffer());
                ++count;
            } catch (BufferOverflowException e){
                buffer().reset();
                throw e;
            }
            return this;
        }
        
        /**
         * Returns a GetAppForBackupRsp with data supplied to the builder.
         * @return The GetAppForBackupRsp instance.
         */
        public GetAppForBackupRsp build(){
            if (buffer().position() == 0){
                // Always have first 4 bytes to store the count
                buffer().putInt(0);
            } else {
                buffer().putInt(0, count);
            }
            return (GetAppForBackupRsp)super.build();
        }
        
    }
	
}
