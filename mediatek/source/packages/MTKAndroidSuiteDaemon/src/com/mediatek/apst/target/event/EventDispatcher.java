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

package com.mediatek.apst.target.event;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcher {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static EventDispatcher mInstance = new EventDispatcher();
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private List<IBatteryListener> mBatteryListeners;

    private List<IPackageListener> mPackageListeners;
    
    private List<ISdStateListener> mSdStateListeners;
    
    private List<ISimStateListener> mSimStateListeners;
    
    private List<ISmsListener> mSmsListeners;
    
    private List<IMmsListener> mMmsListeners;
    
    private List<IContactsListener> mContactsListeners;
    
    private List<ICalendarEventListener> mCalendarEventListeners;
    
   
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    private EventDispatcher(){
        // Currently only one listener(MainService)
        this.mBatteryListeners = new ArrayList<IBatteryListener>(1);
        this.mPackageListeners = new ArrayList<IPackageListener>(1);
        this.mSdStateListeners = new ArrayList<ISdStateListener>(1);
        this.mSimStateListeners = new ArrayList<ISimStateListener>(1);
        this.mSmsListeners = new ArrayList<ISmsListener>(1);
        this.mContactsListeners = new ArrayList<IContactsListener>(1);
        this.mMmsListeners=new ArrayList<IMmsListener>(1);
        this.mCalendarEventListeners = new ArrayList<ICalendarEventListener>(1);
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
    public synchronized static void registerBatteryListener(
            IBatteryListener listener){
        if (null != listener){
            mInstance.mBatteryListeners.add(listener);
        }
    }
    
    public synchronized static void registerPackageListener(
            IPackageListener listener){
        if (null != listener){
            mInstance.mPackageListeners.add(listener);
        }
    }
    
    public synchronized static void registerSdStateListener(
            ISdStateListener listener){
        if (null != listener){
            mInstance.mSdStateListeners.add(listener);
        }
    }
    
    public synchronized static void registerSimStateListener(
            ISimStateListener listener){
        if (null != listener){
            mInstance.mSimStateListeners.add(listener);
        }
    }
    
    public synchronized static void registerSmsListener(ISmsListener listener){
        if (null != listener){
            mInstance.mSmsListeners.add(listener);
        }
    }
    
    public synchronized static void registerContactsListener(
            IContactsListener listener){
        if (null != listener){
            mInstance.mContactsListeners.add(listener);
        }
    }
    
    public synchronized static void registerMmsListener(
            IMmsListener listener){
        if (null != listener){
            mInstance.mMmsListeners.add(listener);
        }
    }
    
    public synchronized static void registerCalendarEventListener(
            ICalendarEventListener listener){
        if (null != listener){
            mInstance.mCalendarEventListeners.add(listener);
        }
    }
    
    public synchronized static void unregisterListener(IEventListener listener){
        mInstance.mBatteryListeners.remove(listener);
        mInstance.mPackageListeners.remove(listener);
        mInstance.mSdStateListeners.remove(listener);
        mInstance.mSimStateListeners.remove(listener);
        mInstance.mSmsListeners.remove(listener);
        mInstance.mContactsListeners.remove(listener);
        mInstance.mCalendarEventListeners.remove(listener);
    }
    
    public static void dispatchBatteryStateChangedEvent(Event event){
        for (IBatteryListener listener : mInstance.mBatteryListeners) {
            listener.onBatteryStateChanged(event);
        }
    }
    
    public static void dispatchPackageAddedEvent(Event event){
        for (IPackageListener listener : mInstance.mPackageListeners) {
            listener.onPackageAdded(event);
        }
    }
    
    public static void dispatchPackageDataClearedEvent(Event event){
        for (IPackageListener listener : mInstance.mPackageListeners) {
            listener.onPackageDataCleared(event);
        }
    }
    
    public static void dispatchSdStateChangedEvent(Event event){
        for (ISdStateListener listener : mInstance.mSdStateListeners) {
            listener.onSdStateChanged(event);
        }
    }
    
    public static void dispatchSimStateChangedEvent(Event event){
        for (ISimStateListener listener : mInstance.mSimStateListeners) {
            listener.onSimStateChanged(event);
        }
    }
    
    public static void dispatchSmsSentEvent(Event event){
        for (ISmsListener listener : mInstance.mSmsListeners) {
            listener.onSmsSent(event);
        }
    }
    
    public static void dispatchSmsReceivedEvent(Event event){
        for (ISmsListener listener : mInstance.mSmsListeners) {
            listener.onSmsReceived(event);
        }
    }
    
    public static void dispatchSmsInsertedEvent(Event event){
        for (ISmsListener listener : mInstance.mSmsListeners) {
            listener.onSmsInserted(event);
        }
    }
    
    public static void dispatchMmsInsertedEvent(Event event){
        for (IMmsListener listener : mInstance.mMmsListeners) {
            listener.onMmsInserted(event);
        }
    }
    
    public static void dispatchMmsReceiveEvent(Event event){
        for (IMmsListener listener : mInstance.mMmsListeners) {
            listener.onMmsReceived(event);
        }
    }
    
    
    public static void dispatchContactsContentChangedEvent(Event event){
        for (IContactsListener listener : mInstance.mContactsListeners) {
            listener.onContactsContentChanged(event);
        }
    }
    
    public static void dispatchCalendarEventChangedEvent(Event event){
        for (ICalendarEventListener listener : mInstance.mCalendarEventListeners) {
            listener.onCalendarEventContentChanged(event);
        }
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
