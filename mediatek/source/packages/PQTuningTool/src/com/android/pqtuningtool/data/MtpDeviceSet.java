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
 * Copyright (C) 2010 The Android Open Source Project
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

package  com.android.pqtuningtool.data;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.app.GalleryApp;
import  com.android.pqtuningtool.util.MediaSetUtils;

import android.mtp.MtpDeviceInfo;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// MtpDeviceSet -- MtpDevice -- MtpImage
public class MtpDeviceSet extends MediaSet {
    private static final String TAG = "MtpDeviceSet";

    private GalleryApp mApplication;
    private final ArrayList<MediaSet> mDeviceSet = new ArrayList<MediaSet>();
    private final ChangeNotifier mNotifier;
    private final MtpContext mMtpContext;
    private final String mName;

    public MtpDeviceSet(Path path, GalleryApp application, MtpContext mtpContext) {
        super(path, nextVersionNumber());
        mApplication = application;
        mNotifier = new ChangeNotifier(this, Uri.parse("mtp://"), application);
        mMtpContext = mtpContext;
        mName = application.getResources().getString(R.string.set_label_mtp_devices);
    }

    private void loadDevices() {
        DataManager dataManager = mApplication.getDataManager();
        // Enumerate all devices
        mDeviceSet.clear();
        List<android.mtp.MtpDevice> devices = mMtpContext.getMtpClient().getDeviceList();
        Log.v(TAG, "loadDevices: " + devices + ", size=" + devices.size());
        for (android.mtp.MtpDevice mtpDevice : devices) {
            int deviceId = mtpDevice.getDeviceId();
            Path childPath = mPath.getChild(deviceId);
            MtpDevice device = (MtpDevice) dataManager.peekMediaObject(childPath);
            if (device == null) {
                device = new MtpDevice(childPath, mApplication, deviceId, mMtpContext);
            }
            Log.d(TAG, "add device " + device);
            mDeviceSet.add(device);
        }

        Collections.sort(mDeviceSet, MediaSetUtils.NAME_COMPARATOR);
        for (int i = 0, n = mDeviceSet.size(); i < n; i++) {
            mDeviceSet.get(i).reload();
        }
    }

    public static String getDeviceName(MtpContext mtpContext, int deviceId) {
        android.mtp.MtpDevice device = mtpContext.getMtpClient().getDevice(deviceId);
        if (device == null) {
            return "";
        }
        MtpDeviceInfo info = device.getDeviceInfo();
        if (info == null) {
            return "";
        }
        String manufacturer = info.getManufacturer().trim();
        String model = info.getModel().trim();
        return manufacturer + " " + model;
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return index < mDeviceSet.size() ? mDeviceSet.get(index) : null;
    }

    @Override
    public int getSubMediaSetCount() {
        return mDeviceSet.size();
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            loadDevices();
        }
        return mDataVersion;
    }
}
