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
**
** Copyright (C) 2008, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#define LOG_TAG "CameraService"
//
#include <media/AudioSystem.h>
//
#include <CameraService.h>
#include <CameraHardwareInterface.h>
#include <camera/CameraParameters.h>
//
#include <camera/MtkCamera.h>


namespace android {


/******************************************************************************
*
*******************************************************************************/
void
CameraService::
Client::
playRecordingSound()
{
    CameraParameters params(mHardware->getParameters());
    int value = 0;
    value = params.getInt("rec-mute-ogg");
    if (value != 1) {
        mCameraService->playSound(SOUND_RECORDING);
    }
}


/******************************************************************************
*
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtNotify(int32_t ext1, int32_t ext2)
{
    int32_t const extMsgType = ext1;
    switch  (extMsgType)
    {
    case MTK_CAMERA_MSG_EXT_NOTIFY_BURST_SHUTTER:
        handleMtkExtBurstShutter(ext1, ext2);
        break;
    case MTK_CAMERA_MSG_EXT_NOTIFY_CONTINUOUS_SHUTTER:
        handleMtkExtContinuousShutter(ext1, ext2);
        break;
    case MTK_CAMERA_MSG_EXT_NOTIFY_CONTINUOUS_END:
        handleMtkExtContinuousEnd(ext1, ext2);
        break;
    //
    default:
        handleGenericNotify(MTK_CAMERA_MSG_EXT_NOTIFY, ext1, ext2);
        break;
    }
}


/******************************************************************************
*   Burst Shutter Callback Handler
*       ext2: count-down shutter number; 0: the last one shutter.
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtBurstShutter(int32_t ext1, int32_t ext2)
{
    handleShutter();
    if  (0 < ext2) {
        //  not the last one burst shutter.
        enableMsgType(CAMERA_MSG_SHUTTER);
        LOGD("[handleMtkExtBurstShutter] count-down burst shutter number:%d - enableMsgType(CAMERA_MSG_SHUTTER) \r\n", ext2);
    }
    else {
        LOGD("[handleMtkExtBurstShutter] the last one burst shutter \r\n");
    }
}


/******************************************************************************
*   Burst Shot (EV Shot)
*       int[0]: the total shut count.
*       int[1]: count-down shut number; 0: the last one shut.
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtDataBurstShot(const sp<IMemory>& dataPtr, camera_frame_metadata_t *metadata)
{
    MtkCamMsgExtDataHelper MtkExtDataHelper;
    if  ( ! MtkExtDataHelper.init(dataPtr) ) {
        LOGE("[%s] MtkCamMsgExtDataHelper::init fail - dataPtr(%p), this(%p) \r\n", __FUNCTION__, dataPtr.get(), this);
        return;
    }
    //
    uint_t const*const pExtParam = (uint_t const*)MtkExtDataHelper.getExtParamBase();
    uint_t const uTotalShutCount = pExtParam[0];
    uint_t const uCountdownIndex = pExtParam[1];
    //
    size_t const    imageSize   = MtkExtDataHelper.getExtParamSize()    - sizeof(uint_t) * 2;
    ssize_t const   imageOffset = MtkExtDataHelper.getExtParamOffset()  + sizeof(uint_t) * 2;
    sp<MemoryBase> image = new MemoryBase(MtkExtDataHelper.getHeap(), imageOffset, imageSize);
    //
    MtkExtDataHelper.uninit();
    //
    //
    if  (0 < uCountdownIndex) {
        //  not the last one burst shut.
        LOGD("[%s] count-down burst shut number:%d/%d - (size, offset)=(%d, %ld) \r\n", __FUNCTION__, uCountdownIndex, uTotalShutCount, imageSize, imageOffset);
    }
    else {
        disableMsgType(CAMERA_MSG_COMPRESSED_IMAGE);
        LOGD("[%s] the last one burst shut - (size, offset)=(%d, %ld) \r\n", __FUNCTION__, imageSize, imageOffset);
    }
    //
    if (image == 0) {
        LOGE("[%s] fail to new MemoryBase \r\n", __FUNCTION__);
        return;
    }
    //
    sp<ICameraClient> c = mCameraClient;
    mLock.unlock();
    if (c != 0) {
        c->dataCallback(CAMERA_MSG_COMPRESSED_IMAGE, image, NULL);
    }
}

/******************************************************************************
*  Continuous Shutter Callback Handler
*       ext2: current continuous shutter number.
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtContinuousShutter(int32_t ext1, int32_t ext2)
{
    
    //if (mPlayShutterSound) {
    //    mCameraService->playSound(SOUND_SHUTTER);
    //}

    sp<ICameraClient> c = mCameraClient;
    if (c != 0) {
        mLock.unlock();
        c->notifyCallback(CAMERA_MSG_SHUTTER, 0, 0);
        if (!lockIfMessageWanted(CAMERA_MSG_SHUTTER)) return;
    }
    //disableMsgType(CAMERA_MSG_SHUTTER);

    mLock.unlock();
    
    //enableMsgType(CAMERA_MSG_SHUTTER);
    LOGD("[handleMtkExtContinuousShutter] current continuous shutter number:%d \n", ext2);
}

/******************************************************************************
*   Continuous EndCallback Handler
* 
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtContinuousEnd(int32_t ext1, int32_t ext2)
{
    disableMsgType(CAMERA_MSG_SHUTTER);
    disableMsgType(CAMERA_MSG_COMPRESSED_IMAGE);
    handleGenericNotify(MTK_CAMERA_MSG_EXT_NOTIFY, ext1, ext2);
    LOGD("[handleMtkExtContinuousEnd] total continuous shut number is %d \n", ext2);
}


/******************************************************************************
*   Continuous Shot
*       int[0]: current continuous shut number.
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtDataContinuousShot(const sp<IMemory>& dataPtr, camera_frame_metadata_t *metadata)
{
    MtkCamMsgExtDataHelper MtkExtDataHelper;
    if  ( ! MtkExtDataHelper.init(dataPtr) ) {
        LOGE("[%s] MtkCamMsgExtDataHelper::init fail - dataPtr(%p), this(%p) \r\n", __FUNCTION__, dataPtr.get(), this);
        return;
    }
    //
    uint_t const*const pExtParam = (uint_t const*)MtkExtDataHelper.getExtParamBase();
    uint_t const uCurShutCount = pExtParam[0];
    //
    size_t const    imageSize   = MtkExtDataHelper.getExtParamSize()    - sizeof(uint_t) * 1;
    ssize_t const   imageOffset = MtkExtDataHelper.getExtParamOffset()  + sizeof(uint_t) * 1;
    sp<MemoryBase> image = new MemoryBase(MtkExtDataHelper.getHeap(), imageOffset, imageSize);
    //
    MtkExtDataHelper.uninit();

    LOGD("[%s] current continuous shut number:%d - (size, offset)=(%d, %ld) \r\n", __FUNCTION__,  uCurShutCount, imageSize, imageOffset);

    //
    if (image == 0) {
        LOGE("[%s] fail to new MemoryBase \r\n", __FUNCTION__);
        return;
    }
    //
    sp<ICameraClient> c = mCameraClient;
    mLock.unlock();
    if (c != 0) {
        c->dataCallback(CAMERA_MSG_COMPRESSED_IMAGE, image, NULL);
    }
}
/******************************************************************************
*
*******************************************************************************/
void
CameraService::
Client::
handleMtkExtData(const sp<IMemory>& dataPtr, camera_frame_metadata_t *metadata)
{
    MtkCamMsgExtDataHelper MtkExtDataHelper;

    if  ( ! MtkExtDataHelper.init(dataPtr) ) {
        LOGE("[handleMtkExtData] MtkCamMsgExtDataHelper::init fail - dataPtr(%p), this(%p)", dataPtr.get(), this);
        return;
    }

    void*   const pvExtParam   = MtkExtDataHelper.getExtParamBase();
    size_t  const ExtParamSize = MtkExtDataHelper.getExtParamSize();
    switch  (MtkExtDataHelper.getExtMsgType())
    {
    case MTK_CAMERA_MSG_EXT_DATA_BURST_SHOT:
        handleMtkExtDataBurstShot(dataPtr, metadata);
        break;
    //
    case MTK_CAMERA_MSG_EXT_DATA_CONTINUOUS_SHOT:
        handleMtkExtDataContinuousShot(dataPtr, metadata);
        break;
    //
    default:
        handleGenericData(MTK_CAMERA_MSG_EXT_DATA, dataPtr, metadata);
        break;
    }
    MtkExtDataHelper.uninit();
}


};
