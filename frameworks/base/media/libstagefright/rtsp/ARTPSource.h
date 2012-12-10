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

#ifndef A_RTP_SOURCE_H_

#define A_RTP_SOURCE_H_

#include <stdint.h>

#include <media/stagefright/foundation/ABase.h>
#include <utils/List.h>
#include <utils/RefBase.h>

namespace android {

struct ABuffer;
struct AMessage;
struct ARTPAssembler;
struct ASessionDescription;
#ifndef ANDROID_DEFAULT_CODE 
struct AString;

#ifdef  MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
struct APacketSource;
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE

struct ARTPSource : public RefBase {
    ARTPSource(
            uint32_t id,
            const sp<ASessionDescription> &sessionDesc, size_t index,
            const sp<AMessage> &notify);

    void processRTPPacket(const sp<ABuffer> &buffer);
#ifndef ANDROID_DEFAULT_CODE 
    void timeUpdate(uint32_t rtpTime, uint64_t ntpTime, uint32_t rtpSeq);
#endif // #ifndef ANDROID_DEFAULT_CODE
    void timeUpdate(uint32_t rtpTime, uint64_t ntpTime);
    void byeReceived();

    List<sp<ABuffer> > *queue() { return &mQueue; }

    void addReceiverReport(const sp<ABuffer> &buffer);
    void addFIR(const sp<ABuffer> &buffer);

#ifndef ANDROID_DEFAULT_CODE 
    bool timeEstablished() const {
        // we can handle timestamp on only 1 SR
        // Waiting 2 SRs makes a long waiting
        return mNumTimes >= 1;
    }
    void SRUpdate(uint64_t ntpTime);
    void flushRTPPackets();
    void resetTimes() {
        mNumTimes = 0;
    }
    void addSDES(const AString& cname, const sp<ABuffer> &buffer);
    void updateExpectedTimeoutUs(const int32_t& samples);
    void updateExpectedTimeoutUs(const int64_t& duration);
    int64_t getExpectedTimeoutUs() const { return mExpectedTimeoutUs; }
    void useFirstTimestamp();
    static const int64_t kAccessUnitTimeoutUs = 3000000ll;
	
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	void addNADUApp(sp<APacketSource> &pApacketSource,const sp<ABuffer> &buffer);
#endif
#endif // #ifndef ANDROID_DEFAULT_CODE
private:
    uint32_t mID;
    uint32_t mHighestSeqNumber;
    int32_t mNumBuffersReceived;

    List<sp<ABuffer> > mQueue;
    sp<ARTPAssembler> mAssembler;

    uint64_t mLastNTPTime;
    int64_t mLastNTPTimeUpdateUs;

    bool mIssueFIRRequests;
    int64_t mLastFIRRequestUs;
    uint8_t mNextFIRSeqNo;

    sp<AMessage> mNotify;
#ifndef ANDROID_DEFAULT_CODE 
    size_t mNumTimes;
    uint64_t mNTPTime[2];
    uint32_t mRTPTime[2];
    bool mHighestSeqNumberSet;
    uint32_t mClockRate;
    int64_t mExpectedTimeoutUs;
    uint64_t RTP2NTP(uint32_t rtpTime) const;

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	//uint64_t mNumPacketReceived;
	uint32_t mLastPacketRtpTime;
	int64_t mLastPacketRecvTimeUs; //in RTP timestamp units
	
	uint32_t m_uiInterarrivalJitter;
	double m_dInterarrivalJitter;
	
	uint32_t mNumLastRRPackRecv;
	uint32_t mLastRRPackRecvSeqNum;

	uint32_t mFirstPacketSeqNum;

#endif
#endif // #ifndef ANDROID_DEFAULT_CODE

    bool queuePacket(const sp<ABuffer> &buffer);

    DISALLOW_EVIL_CONSTRUCTORS(ARTPSource);
};

}  // namespace android

#endif  // A_RTP_SOURCE_H_
