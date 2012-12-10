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

#ifndef A_PACKET_SOURCE_H_

#define A_PACKET_SOURCE_H_

#include <media/stagefright/foundation/ABase.h>
#include <media/stagefright/MediaSource.h>
#include <utils/threads.h>
#include <utils/List.h>

namespace android {

struct ABuffer;
struct ASessionDescription;

struct APacketSource : public MediaSource {
    APacketSource(const sp<ASessionDescription> &sessionDesc, size_t index);

    status_t initCheck() const;

    virtual status_t start(MetaData *params = NULL);
    virtual status_t stop();
    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options = NULL);

    void queueAccessUnit(const sp<ABuffer> &buffer);
    void signalEOS(status_t result);

    void flushQueue();

#ifndef ANDROID_DEFAULT_CODE 
    int64_t getNormalPlayTimeUs(uint32_t rtpTime);
#else
    int64_t getNormalPlayTimeUs();
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
    void setNormalPlayTimeUs(int64_t timeUs);
    bool isNPTMappingSet();
    bool isAtEOS();
	
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	size_t getBufQueSize(){return m_BufQueSize;} //get Whole Buffer queue size 
	size_t getTargetTime(){return m_TargetTime;}  //get target protected time of buffer queue duration for interrupt-free playback 
	bool getNSN(int32_t* uiNextSeqNum);
	size_t getFreeBufSpace();
	int32_t m_uiNextAduSeqNum;
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE
    void setNormalPlayTimeMapping(
            uint32_t rtpTime, int64_t normalPlayTimeUs);

    int64_t getQueueDurationUs(bool *eos);

protected:
    virtual ~APacketSource();

private:
    status_t mInitCheck;

    Mutex mLock;
    Condition mCondition;

    sp<MetaData> mFormat;
    List<sp<ABuffer> > mBuffers;
    status_t mEOSResult;

#ifndef ANDROID_DEFAULT_CODE 
    // indicate npt mapping is set
    bool mNPTMappingIsSet;
    // for avc nals
    bool mWantsNALFragments;
    List<sp<ABuffer> > mNALUnits;
    int64_t mAccessUnitTimeUs;

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	size_t m_BufQueSize; //Whole Buffer queue size 
	size_t m_TargetTime;  // target protected time of buffer queue duration for interrupt-free playback 
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE
    bool mIsAVC;
    bool mScanForIDR;

    uint32_t mClockRate;

    uint32_t mRTPTimeBase;
    int64_t mNormalPlayTimeBaseUs;

    int64_t mLastNormalPlayTimeUs;

    void updateNormalPlayTime_l(const sp<ABuffer> &buffer);

    DISALLOW_EVIL_CONSTRUCTORS(APacketSource);
};


}  // namespace android

#endif  // A_PACKET_SOURCE_H_
