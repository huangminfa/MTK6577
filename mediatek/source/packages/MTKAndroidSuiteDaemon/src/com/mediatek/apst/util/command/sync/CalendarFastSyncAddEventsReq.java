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

import java.nio.BufferOverflowException;

import com.mediatek.apst.util.command.RawBlockRequest;
import com.mediatek.apst.util.entity.calendar.CalendarEvent;

/**
 * Class Name: CalendarFastSyncAddEventsReq
 * <p>Package: com.mediatek.apst.util.command.sync
 * <p>Created on: 2011-05-27
 * <p>
 * <p>Description: 
 * <p>Request for adding multiple events in fast sync.
 * <p>
 * @author mtk81022 Shaoying Han
 * @version V1.0
 */
public class CalendarFastSyncAddEventsReq extends RawBlockRequest {
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
    public CalendarFastSyncAddEventsReq(){
        super(FEATURE_CALENDAR_SYNC);
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
     * Class Name: CalendarFastSyncAddEventsReq.Builder
     * <p>Package: com.mediatek.apst.util.command.sync
     * <p>Created on: 2011-05-27
     * <p>
     * <p>Description: 
     * <p>Builder class help to create and fill a 
     * CalendarFastSyncAddEventsReq.
     * <p>
     * @author mtk81022 Shaoying Han
     * @version V1.0
     */
    public static class Builder extends RawBlockRequest.Builder {

        private int count;
        
        protected Builder(int rawBlockSize){
            super(rawBlockSize, FEATURE_CALENDAR_SYNC);
            this.count = 0;
        }
        
        protected Builder(){
            super(FEATURE_CALENDAR_SYNC);
            this.count = 0;
        }
        
        //@Override
        protected RawBlockRequest onCreateCommand(int featureId){
            return new CalendarFastSyncAddEventsReq();
        }
        
        /**
         * Append a event into the raw bytes buffer.
         * @param event The event object to append.
         * @return The Builder instance itself.
         * @throws BufferOverflowException
         */
        public Builder appendCalendarEvent(CalendarEvent event, int versionCode) 
        throws BufferOverflowException {
            buffer().mark();
            try {
                if (0 == count){
                    // Reserve 4 bytes to store the count
                    buffer().putInt(0);
                }
                event.writeRawWithVersion(buffer(), versionCode);
                ++count;
            } catch (BufferOverflowException e){
                buffer().reset();
                throw e;
            }
            return this;
        }

        //@Override
        public void reset(){
            super.reset();
            // Reset count. This is important.
            count = 0;
        }
        
        /**
         * Returns a CalendarFastSyncAddEventsReq with data supplied 
         * to the builder.
         * @return The CalendarFastSyncAddEventsReq instance.
         */
        public CalendarFastSyncAddEventsReq build(){
            if (buffer().position() == 0){
                // Always have first 4 bytes to store the count
                buffer().putInt(0);
            } else {
                buffer().putInt(0, count);
            }
            return (CalendarFastSyncAddEventsReq)super.build();
        }
        
    }
    
}
