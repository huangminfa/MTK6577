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

//MTK_OP01_PROTECT_START

#define LOG_TAG "CMMBExtractor"
#include <utils/Log.h>

#include <arpa/inet.h>

#include <ctype.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaBufferGroup.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>
#include <utils/String8.h>
#include<media/stagefright/MediaErrors.h>

#include "include/CMMBExtractor.h"


namespace android {

class CMMBSource : public MediaSource {
public:
    CMMBSource(const sp<MetaData> &format,
                const sp<DataSource> &dataSource);

    virtual status_t start(MetaData *params = NULL);
    virtual status_t stop();

    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options = NULL);

protected:
    virtual ~CMMBSource();

private:
    Mutex mLock;

    sp<MetaData> mFormat;
    sp<DataSource> mDataSource;

    bool mStarted;
    bool mIsAVC;

    MediaBufferGroup *mGroup;

    MediaBuffer *mBuffer;

    uint8_t *mSrcBuffer;


    CMMBSource(const CMMBSource &);
    CMMBSource &operator=(const CMMBSource &);
};




CMMBExtractor::CMMBExtractor(const sp<DataSource> &source)
    : mDataSource(source),
      CMMBMetaData(new MetaData),
      VideoTrack(NULL),
      AudioTrack(NULL),
      mHaveMetadata(false){
      //LOGE("CMMBExtractor::CMMBExtractor");
      
}

CMMBExtractor::~CMMBExtractor() {


}


sp<MetaData> CMMBExtractor::getMetaData() {
    return NULL;
}


size_t CMMBExtractor::countTracks() {
    return 0;
}

sp<MetaData> CMMBExtractor::getTrackMetaData(
        size_t index, uint32_t flags) {

    return NULL;
}

sp<MediaSource> CMMBExtractor::getTrack(size_t index) {

    return NULL;
}


status_t CMMBExtractor::readMetaData()
{

    return INVALID_OPERATION;
}



///////////////////////////////////////////////////////////
CMMBSource::CMMBSource(
        const sp<MetaData> &format,
        const sp<DataSource> &dataSource)
    : mFormat(format),
      mDataSource(dataSource),
      mStarted(false),
      mIsAVC(false),
      mGroup(NULL),
      mBuffer(NULL),
      mSrcBuffer(NULL){
}

CMMBSource::~CMMBSource() {

}


status_t CMMBSource::start(MetaData *params) {
     return false;
}



status_t CMMBSource::stop() {
    return false;
}


//get meta data.
sp<MetaData> CMMBSource::getFormat() {
    return NULL;
}



status_t CMMBSource::read(
        MediaBuffer **out, const ReadOptions *options) {
    return false;

}


}  // namespace android

//MTK_OP01_PROTECT_END
