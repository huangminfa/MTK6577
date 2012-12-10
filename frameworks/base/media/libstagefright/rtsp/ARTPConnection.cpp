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

//#define LOG_NDEBUG 0
#define LOG_TAG "ARTPConnection"
#include <utils/Log.h>

#include "ARTPConnection.h"

#include "ARTPSource.h"
#include "ASessionDescription.h"
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
#include "APacketSource.h"
#endif

#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/foundation/AString.h>
#include <media/stagefright/foundation/hexdump.h>

#include <arpa/inet.h>
#include <sys/socket.h>
#ifndef ANDROID_DEFAULT_CODE 
#include <sys/ioctl.h>
#endif // #ifndef ANDROID_DEFAULT_CODE

namespace android {

static const size_t kMaxUDPSize = 1500;

static uint16_t u16at(const uint8_t *data) {
    return data[0] << 8 | data[1];
}

static uint32_t u32at(const uint8_t *data) {
    return u16at(data) << 16 | u16at(&data[2]);
}

static uint64_t u64at(const uint8_t *data) {
    return (uint64_t)(u32at(data)) << 32 | u32at(&data[4]);
}

// static
const int64_t ARTPConnection::kSelectTimeoutUs = 1000ll;
#ifndef ANDROID_DEFAULT_CODE 
static int64_t kAccessUnitTimeoutUs = ARTPSource::kAccessUnitTimeoutUs;
static int64_t kCheckAliveInterval = 500000ll;
static int64_t kAccessUnitTimeoutUsMargin = 500000ll;
static int64_t kRRInterval = 4000000ll;
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
static int64_t kRRIntervalBitrateAdap = 2000000ll;
#endif 
static int64_t kInjectPollInterval = 100000ll;
#endif // #ifndef ANDROID_DEFAULT_CODE

struct ARTPConnection::StreamInfo {
    int mRTPSocket;
    int mRTCPSocket;
    sp<ASessionDescription> mSessionDesc;
    size_t mIndex;
    sp<AMessage> mNotifyMsg;
    KeyedVector<uint32_t, sp<ARTPSource> > mSources;

    int64_t mNumRTCPPacketsReceived;
    int64_t mNumRTPPacketsReceived;
    struct sockaddr_in mRemoteRTCPAddr;

    bool mIsInjected;
#ifndef ANDROID_DEFAULT_CODE 
    bool mCheckPending;
    int32_t mCheckGeneration;
    int32_t mNumPacketsTouched;
    int64_t mLastPacketTimeUs;
    bool mTimeUpdated;
    uint32_t mRTPTime;
    uint64_t mNTPTime;
    uint32_t mRTPSeqNo;
    AString mCName;
    bool mIsSSRCSet;
    int32_t mSSRC;
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	size_t mNaduFrequence;
	sp<APacketSource> mPacketSource;
	uint8_t mBitAdaptSentRRCount;
	
#endif
#endif // #ifndef ANDROID_DEFAULT_CODE
};

ARTPConnection::ARTPConnection(uint32_t flags)
#ifndef ANDROID_DEFAULT_CODE 
    : mFlags(flags | kFakeTimestamps),
#else
    : mFlags(flags),
#endif // #ifndef ANDROID_DEFAULT_CODE
      mPollEventPending(false),
      mLastReceiverReportTimeUs(-1) {
}

ARTPConnection::~ARTPConnection() {
}

void ARTPConnection::addStream(
        int rtpSocket, int rtcpSocket,
        const sp<ASessionDescription> &sessionDesc,
        size_t index,
        const sp<AMessage> &notify,
#ifndef ANDROID_DEFAULT_CODE 
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		bool injected, int32_t* pSSRC,RtspBitRateAdapParam* pbitRateAdapParam) {		
#else

        bool injected, int32_t* pSSRC) {
#endif
#else
        bool injected) {
#endif // #ifndef ANDROID_DEFAULT_CODE
    sp<AMessage> msg = new AMessage(kWhatAddStream, id());
    msg->setInt32("rtp-socket", rtpSocket);
    msg->setInt32("rtcp-socket", rtcpSocket);
    msg->setObject("session-desc", sessionDesc);
    msg->setSize("index", index);
    msg->setMessage("notify", notify);
    msg->setInt32("injected", injected);
#ifndef ANDROID_DEFAULT_CODE 
    if (pSSRC != NULL) {
        msg->setInt32("ssrc", *pSSRC);
    }
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	if(pbitRateAdapParam){
		msg->setPointer("apacket-source",(pbitRateAdapParam->mAPacketSource).get());
		msg->setSize("naduFreq",pbitRateAdapParam->mNaduFreq);
	}
	else
		LOGE("addStream,pbitRateAdapParam pointer is NULL");
#endif
#endif // #ifndef ANDROID_DEFAULT_CODE
    msg->post();
}

void ARTPConnection::removeStream(int rtpSocket, int rtcpSocket) {
    sp<AMessage> msg = new AMessage(kWhatRemoveStream, id());
    msg->setInt32("rtp-socket", rtpSocket);
    msg->setInt32("rtcp-socket", rtcpSocket);
    msg->post();
}

static void bumpSocketBufferSize(int s) {
    int size = 256 * 1024;
    CHECK_EQ(setsockopt(s, SOL_SOCKET, SO_RCVBUF, &size, sizeof(size)), 0);
}

// static
void ARTPConnection::MakePortPair(
#ifndef ANDROID_DEFAULT_CODE 
        // caller should check min and max
        int *rtpSocket, int *rtcpSocket, unsigned *rtpPort, 
        int min, int max) {
#else
        int *rtpSocket, int *rtcpSocket, unsigned *rtpPort) {
#endif // #ifndef ANDROID_DEFAULT_CODE
    *rtpSocket = socket(AF_INET, SOCK_DGRAM, 0);
    CHECK_GE(*rtpSocket, 0);

    bumpSocketBufferSize(*rtpSocket);

    *rtcpSocket = socket(AF_INET, SOCK_DGRAM, 0);
    CHECK_GE(*rtcpSocket, 0);

    bumpSocketBufferSize(*rtcpSocket);

#ifndef ANDROID_DEFAULT_CODE 
    int start = min;
    start++;
    start &= ~1;
#else
    unsigned start = (rand() * 1000)/ RAND_MAX + 15550;
    start &= ~1;
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
    int range = max - start;
    if (range > 0)
        start += rand() % range;
    start &= ~1;

    for (int port = start; port < max + 1; port += 2) {
#else
    for (unsigned port = start; port < 65536; port += 2) {
#endif // #ifndef ANDROID_DEFAULT_CODE
        struct sockaddr_in addr;
        memset(addr.sin_zero, 0, sizeof(addr.sin_zero));
        addr.sin_family = AF_INET;
        addr.sin_addr.s_addr = htonl(INADDR_ANY);
        addr.sin_port = htons(port);

        if (bind(*rtpSocket,
                 (const struct sockaddr *)&addr, sizeof(addr)) < 0) {
            continue;
        }

        addr.sin_port = htons(port + 1);

        if (bind(*rtcpSocket,
                 (const struct sockaddr *)&addr, sizeof(addr)) == 0) {
            *rtpPort = port;
            return;
        }
    }

    TRESPASS();
}

void ARTPConnection::onMessageReceived(const sp<AMessage> &msg) {
    switch (msg->what()) {
        case kWhatAddStream:
        {
            onAddStream(msg);
            break;
        }

        case kWhatRemoveStream:
        {
            onRemoveStream(msg);
            break;
        }

        case kWhatPollStreams:
        {
            onPollStreams();
            break;
        }

        case kWhatInjectPacket:
        {
            onInjectPacket(msg);
            break;
        }

#ifndef ANDROID_DEFAULT_CODE 
        case kWhatFakeTimestamps:
        {
            onFakeTimestamps();
            break;
        }

        case kWhatResetTimestamps:
        {
            onResetTimestamps();
            break;
        }

        case kWhatStartCheckAlives:
        {
            onStartCheckAlives();
            break;
        }

        case kWhatStopCheckAlives:
        {
            onStopCheckAlives();
            break;
        }

        case kWhatCheckAlive:
        {
            onCheckAlive(msg);
            break;
        }

        case kWhatEnableSR:
        {
            mFlags &= ~kFakeTimestamps;
            break;
        }

        case kWhatTimeUpdate:
        {
            onTimeUpdate(msg);
            break;
        }

        case kWhatUseFirstTimestamp:
        {
            onUseFirstTimestamp();
            break;
        }

        case kWhatInjectPollStreams:
        {
            onPostInjectEvent();
            break;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE

        default:
        {
            TRESPASS();
            break;
        }
    }
}

void ARTPConnection::onAddStream(const sp<AMessage> &msg) {
    mStreams.push_back(StreamInfo());
    StreamInfo *info = &*--mStreams.end();
#ifndef ANDROID_DEFAULT_CODE 
    info->mTimeUpdated = false;
#endif // #ifndef ANDROID_DEFAULT_CODE

    int32_t s;
    CHECK(msg->findInt32("rtp-socket", &s));
    info->mRTPSocket = s;
#ifndef ANDROID_DEFAULT_CODE 
    struct sockaddr_in addr;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    info->mCName.setTo("stagefright@");
    info->mCName.append(inet_ntoa(addr.sin_addr));
    info->mIsSSRCSet = msg->findInt32("ssrc", &info->mSSRC);
#endif // #ifndef ANDROID_DEFAULT_CODE
    CHECK(msg->findInt32("rtcp-socket", &s));
    info->mRTCPSocket = s;

    int32_t injected;
    CHECK(msg->findInt32("injected", &injected));

    info->mIsInjected = injected;

    sp<RefBase> obj;
    CHECK(msg->findObject("session-desc", &obj));
    info->mSessionDesc = static_cast<ASessionDescription *>(obj.get());

    CHECK(msg->findSize("index", &info->mIndex));

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	info->mNaduFrequence = 0;
	msg->findSize("naduFreq",&info->mNaduFrequence);
	if((info->mNaduFrequence) > 0){
		//mSupportBitrateAdapt = true;
		kRRInterval = kRRIntervalBitrateAdap; //change the sent RR frequency
	}
	
	LOGI("onAddStream,info->mIndex=%d,info->mNaduFrequence=%d",info->mIndex,info->mNaduFrequence);
	
	void* pTempSource = NULL;
	APacketSource* pApacketSource = NULL;
	msg->findPointer("apacket-source",&pTempSource);		
	pApacketSource = (APacketSource*)pTempSource;
	info->mPacketSource = pApacketSource;

	info->mBitAdaptSentRRCount = 0;	
#endif
	
    CHECK(msg->findMessage("notify", &info->mNotifyMsg));

    info->mNumRTCPPacketsReceived = 0;
    info->mNumRTPPacketsReceived = 0;
    memset(&info->mRemoteRTCPAddr, 0, sizeof(info->mRemoteRTCPAddr));

#ifdef MTK_RTP_OVER_RTSP_SUPPORT
    if (!injected) {
        postPollEvent();
    } else {
        postInjectEvent();
    }
#else
    if (!injected) {
        postPollEvent();
    }
#endif
}

void ARTPConnection::onRemoveStream(const sp<AMessage> &msg) {
    int32_t rtpSocket, rtcpSocket;
    CHECK(msg->findInt32("rtp-socket", &rtpSocket));
    CHECK(msg->findInt32("rtcp-socket", &rtcpSocket));

    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()
           && (it->mRTPSocket != rtpSocket || it->mRTCPSocket != rtcpSocket)) {
        ++it;
    }

    if (it == mStreams.end()) {
        return;
    }

    mStreams.erase(it);
}

void ARTPConnection::postPollEvent() {
    if (mPollEventPending) {
        return;
    }

    sp<AMessage> msg = new AMessage(kWhatPollStreams, id());
    msg->post();

    mPollEventPending = true;
}

void ARTPConnection::onPollStreams() {
    mPollEventPending = false;

    if (mStreams.empty()) {
        return;
    }

    struct timeval tv;
    tv.tv_sec = 0;
    tv.tv_usec = kSelectTimeoutUs;

    fd_set rs;
    FD_ZERO(&rs);

    int maxSocket = -1;
    for (List<StreamInfo>::iterator it = mStreams.begin();
         it != mStreams.end(); ++it) {
        if ((*it).mIsInjected) {
            continue;
        }

        FD_SET(it->mRTPSocket, &rs);
        FD_SET(it->mRTCPSocket, &rs);

        if (it->mRTPSocket > maxSocket) {
            maxSocket = it->mRTPSocket;
        }
        if (it->mRTCPSocket > maxSocket) {
            maxSocket = it->mRTCPSocket;
        }
    }

    if (maxSocket == -1) {
        return;
    }

    int res = select(maxSocket + 1, &rs, NULL, NULL, &tv);
#ifndef ANDROID_DEFAULT_CODE 
    if (res < 0) {
        LOGE("select error %d, stop streaming", errno);
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    if (res > 0) {
        List<StreamInfo>::iterator it = mStreams.begin();
        while (it != mStreams.end()) {
            if ((*it).mIsInjected) {
                ++it;
                continue;
            }

            status_t err = OK;
            if (FD_ISSET(it->mRTPSocket, &rs)) {
                err = receive(&*it, true);
#ifndef ANDROID_DEFAULT_CODE
                if (err < 0) {
                    LOGE("receive err %d in RTP", err);
                }
#endif
            }
            if (err == OK && FD_ISSET(it->mRTCPSocket, &rs)) {
                err = receive(&*it, false);
#ifndef ANDROID_DEFAULT_CODE
                if (err < 0) {
                    LOGE("receive err %d in RTCP", err);
                }
#endif
            }

            if (err == -ECONNRESET) {
                // socket failure, this stream is dead, Jim.

                LOGW("failed to receive RTP/RTCP datagram.");
                it = mStreams.erase(it);
                continue;
            }

            ++it;
        }
    }

#ifndef ANDROID_DEFAULT_CODE
    sendRR();
    if (!mStreams.empty()) {
        postPollEvent();
    }
}

void ARTPConnection::sendRR() {
#endif // #ifndef ANDROID_DEFAULT_CODE
    int64_t nowUs = ALooper::GetNowUs();
#ifndef ANDROID_DEFAULT_CODE 
    if (mLastReceiverReportTimeUs <= 0
            || mLastReceiverReportTimeUs + kRRInterval <= nowUs) {
#else
    if (mLastReceiverReportTimeUs <= 0
            || mLastReceiverReportTimeUs + 5000000ll <= nowUs) {
        sp<ABuffer> buffer = new ABuffer(kMaxUDPSize);
#endif // #ifndef ANDROID_DEFAULT_CODE
        List<StreamInfo>::iterator it = mStreams.begin();
        while (it != mStreams.end()) {
#ifndef ANDROID_DEFAULT_CODE 
            // for rtp over rtsp ...
            sp<ABuffer> buffer = new ABuffer(kMaxUDPSize);
#endif // #ifndef ANDROID_DEFAULT_CODE
            StreamInfo *s = &*it;

            if (s->mIsInjected) {
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                buffer->data()[0] = 0x24;
                buffer->data()[1] = s->mRTCPSocket;
#else
                ++it;
                continue;
#endif
            }

#ifndef ANDROID_DEFAULT_CODE 
            if (s->mNumRTPPacketsReceived == 0 && s->mNumRTCPPacketsReceived == 0) {
#else
            if (s->mNumRTCPPacketsReceived == 0) {
#endif
                // We have never received any RTCP packets on this stream,
                // we don't even know where to send a report.
                ++it;
                continue;
            }

#ifdef MTK_RTP_OVER_RTSP_SUPPORT
            if (s->mIsInjected) {
                buffer->setRange(4, 0);
            } else {
                buffer->setRange(0, 0);
            }
#else
            buffer->setRange(0, 0);
#endif

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT 
			bool b_needSendNADU = false;	
			if(s->mNaduFrequence > 0 && (++(s->mBitAdaptSentRRCount) == s->mNaduFrequence) )
			{
				b_needSendNADU = true;
				s->mBitAdaptSentRRCount = 0;
			}
#endif
            for (size_t i = 0; i < s->mSources.size(); ++i) {
                sp<ARTPSource> source = s->mSources.valueAt(i);

                source->addReceiverReport(buffer);
#ifndef ANDROID_DEFAULT_CODE 
                source->addSDES(s->mCName, buffer);

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT 
				if(b_needSendNADU){
						source->addNADUApp(s->mPacketSource,buffer);	
				}				
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE

                if (mFlags & kRegularlyRequestFIR) {
                    source->addFIR(buffer);
                }
            }

            if (buffer->size() > 0) {
                LOGV("Sending RR...");

#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                if (s->mIsInjected) {
                    int size = buffer->size();
                    buffer->setRange(0, 4 + size);
                    buffer->data()[2] = (size & 0xff00) >> 8;
                    buffer->data()[3] = (size & 0xff);

                    sp<AMessage> notify = s->mNotifyMsg->dup();
                    notify->setInt32("receiver-report", true);
                    notify->setObject("buffer", buffer);
                    notify->post();
                } else {
#endif
                ssize_t n;
                do {
                    n = sendto(
                        s->mRTCPSocket, buffer->data(), buffer->size(), 0,
                        (const struct sockaddr *)&s->mRemoteRTCPAddr,
                        sizeof(s->mRemoteRTCPAddr));
                } while (n < 0 && errno == EINTR);

                if (n <= 0) {
                    LOGW("failed to send RTCP receiver report (%s).",
                         n == 0 ? "connection gone" : strerror(errno));

#ifndef ANDROID_DEFAULT_CODE
                    mLastReceiverReportTimeUs = nowUs;
                    ++it;
#else
                    it = mStreams.erase(it);
#endif
                    continue;
                }

#ifndef ANDROID_DEFAULT_CODE 
                if (n != (ssize_t)buffer->size()) {
                    LOGW("Sending RR error: sent bytes %d, expected bytes %d, errno %d",
                            (int)n, buffer->size(), errno);
                }
#else
                CHECK_EQ(n, (ssize_t)buffer->size());
#endif // #ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                }
#endif

                mLastReceiverReportTimeUs = nowUs;
            }

            ++it;
        }
    }

#ifdef ANDROID_DEFAULT_CODE
    if (!mStreams.empty()) {
        postPollEvent();
    }
#endif
}

status_t ARTPConnection::receive(StreamInfo *s, bool receiveRTP) {
    LOGV("receiving %s", receiveRTP ? "RTP" : "RTCP");

    CHECK(!s->mIsInjected);

#ifndef ANDROID_DEFAULT_CODE 
    // save the space
    int fd = receiveRTP ? s->mRTPSocket : s->mRTCPSocket;
    int size = 0;
    int ret = ioctl(fd, FIONREAD, &size);
    if (ret < 0 || size == 0) {
        LOGE("ret %d err %d size %d", ret, errno, size);
        return -1;
    }
    sp<ABuffer> buffer = new ABuffer(size);
#else
    sp<ABuffer> buffer = new ABuffer(65536);
#endif // #ifndef ANDROID_DEFAULT_CODE

    socklen_t remoteAddrLen =
#ifndef ANDROID_DEFAULT_CODE 
        (s->mNumRTCPPacketsReceived == 0)
#else
        (!receiveRTP && s->mNumRTCPPacketsReceived == 0)
#endif
            ? sizeof(s->mRemoteRTCPAddr) : 0;

    ssize_t nbytes;
    do {
        nbytes = recvfrom(
            receiveRTP ? s->mRTPSocket : s->mRTCPSocket,
            buffer->data(),
            buffer->capacity(),
            0,
            remoteAddrLen > 0 ? (struct sockaddr *)&s->mRemoteRTCPAddr : NULL,
            remoteAddrLen > 0 ? &remoteAddrLen : NULL);
    } while (nbytes < 0 && errno == EINTR);

#ifndef ANDROID_DEFAULT_CODE 
    if (receiveRTP && s->mNumRTCPPacketsReceived == 0) {
        int port = ntohs(s->mRemoteRTCPAddr.sin_port);
        s->mRemoteRTCPAddr.sin_port = htons(++port);
    }
#endif

    if (nbytes <= 0) {
#ifndef ANDROID_DEFAULT_CODE 
        LOGE("nbytes %ld", nbytes);
#endif // #ifndef ANDROID_DEFAULT_CODE
        return -ECONNRESET;
    }

#ifndef ANDROID_DEFAULT_CODE 
    // touch on both RTP and RTCP packets
    s->mNumPacketsTouched++;
    s->mLastPacketTimeUs = ALooper::GetNowUs();
#endif // #ifndef ANDROID_DEFAULT_CODE

    buffer->setRange(0, nbytes);

    // LOGI("received %d bytes.", buffer->size());

    status_t err;
    if (receiveRTP) {
        err = parseRTP(s, buffer);
    } else {
        err = parseRTCP(s, buffer);
    }

    return err;
}

status_t ARTPConnection::parseRTP(StreamInfo *s, const sp<ABuffer> &buffer) {
    if (s->mNumRTPPacketsReceived++ == 0) {
        sp<AMessage> notify = s->mNotifyMsg->dup();
        notify->setInt32("first-rtp", true);
        notify->post();
    }

    size_t size = buffer->size();

    if (size < 12) {
        // Too short to be a valid RTP header.
#ifndef ANDROID_DEFAULT_CODE 
        LOGE("size < 12 %d", size);
#endif // #ifndef ANDROID_DEFAULT_CODE
        return -1;
    }

    const uint8_t *data = buffer->data();

    if ((data[0] >> 6) != 2) {
        // Unsupported version.
#ifndef ANDROID_DEFAULT_CODE 
        LOGE("version %d", data[0]);
#endif // #ifndef ANDROID_DEFAULT_CODE
        return -1;
    }

    if (data[0] & 0x20) {
        // Padding present.

        size_t paddingLength = data[size - 1];

        if (paddingLength + 12 > size) {
            // If we removed this much padding we'd end up with something
            // that's too short to be a valid RTP header.
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("padding %d %d", paddingLength, size);
#endif // #ifndef ANDROID_DEFAULT_CODE
            return -1;
        }

        size -= paddingLength;
    }

    int numCSRCs = data[0] & 0x0f;

    size_t payloadOffset = 12 + 4 * numCSRCs;

    if (size < payloadOffset) {
        // Not enough data to fit the basic header and all the CSRC entries.
#ifndef ANDROID_DEFAULT_CODE 
        LOGE("offset %d %d", payloadOffset, size);
#endif // #ifndef ANDROID_DEFAULT_CODE
        return -1;
    }

    if (data[0] & 0x10) {
        // Header eXtension present.

        if (size < payloadOffset + 4) {
            // Not enough data to fit the basic header, all CSRC entries
            // and the first 4 bytes of the extension header.
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("offset %d %d", payloadOffset, size);
#endif // #ifndef ANDROID_DEFAULT_CODE

            return -1;
        }

        const uint8_t *extensionData = &data[payloadOffset];

        size_t extensionLength =
            4 * (extensionData[2] << 8 | extensionData[3]);

        if (size < payloadOffset + 4 + extensionLength) {
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("extensionLength %d %d %d", extensionLength, payloadOffset, size);
#endif // #ifndef ANDROID_DEFAULT_CODE
            return -1;
        }

        payloadOffset += 4 + extensionLength;
    }

    uint32_t srcId = u32at(&data[8]);

    sp<ARTPSource> source = findSource(s, srcId);
#ifndef ANDROID_DEFAULT_CODE 
    if (source == NULL)
        return OK;
#endif // #ifndef ANDROID_DEFAULT_CODE

    uint32_t rtpTime = u32at(&data[4]);

    sp<AMessage> meta = buffer->meta();
    meta->setInt32("ssrc", srcId);
    meta->setInt32("rtp-time", rtpTime);
    meta->setInt32("PT", data[1] & 0x7f);
    meta->setInt32("M", data[1] >> 7);

    buffer->setInt32Data(u16at(&data[2]));
    buffer->setRange(payloadOffset, size - payloadOffset);

    source->processRTPPacket(buffer);

    return OK;
}

status_t ARTPConnection::parseRTCP(StreamInfo *s, const sp<ABuffer> &buffer) {
    if (s->mNumRTCPPacketsReceived++ == 0) {
        sp<AMessage> notify = s->mNotifyMsg->dup();
        notify->setInt32("first-rtcp", true);
        notify->post();
    }

    const uint8_t *data = buffer->data();
    size_t size = buffer->size();

    while (size > 0) {
        if (size < 8) {
            // Too short to be a valid RTCP header
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("rtcp size < 8 %d", size);
#endif // #ifndef ANDROID_DEFAULT_CODE
            return -1;
        }

        if ((data[0] >> 6) != 2) {
            // Unsupported version.
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("rtcp version %d", data[0]);
#endif // #ifndef ANDROID_DEFAULT_CODE
            return -1;
        }

        if (data[0] & 0x20) {
            // Padding present.

            size_t paddingLength = data[size - 1];

            if (paddingLength + 12 > size) {
                // If we removed this much padding we'd end up with something
                // that's too short to be a valid RTP header.
#ifndef ANDROID_DEFAULT_CODE 
                LOGE("rtcp padding %d %d", paddingLength, size);
#endif // #ifndef ANDROID_DEFAULT_CODE
                return -1;
            }

            size -= paddingLength;
        }

        size_t headerLength = 4 * (data[2] << 8 | data[3]) + 4;

        if (size < headerLength) {
            // Only received a partial packet?
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("size < headerLength %d %d", headerLength, size);
#endif // #ifndef ANDROID_DEFAULT_CODE
            return -1;
        }

        switch (data[1]) {
            case 200:
            {
                parseSR(s, data, headerLength);
                break;
            }

            case 201:  // RR
            case 202:  // SDES
            case 204:  // APP
                break;

            case 205:  // TSFB (transport layer specific feedback)
            case 206:  // PSFB (payload specific feedback)
                // hexdump(data, headerLength);
                break;

            case 203:
            {
                parseBYE(s, data, headerLength);
                break;
            }

            default:
            {
                LOGW("Unknown RTCP packet type %u of size %d",
                     (unsigned)data[1], headerLength);
                break;
            }
        }

        data += headerLength;
        size -= headerLength;
    }

    return OK;
}

status_t ARTPConnection::parseBYE(
        StreamInfo *s, const uint8_t *data, size_t size) {
    size_t SC = data[0] & 0x3f;

    if (SC == 0 || size < (4 + SC * 4)) {
        // Packet too short for the minimal BYE header.
        return -1;
    }

    uint32_t id = u32at(&data[4]);

    sp<ARTPSource> source = findSource(s, id);
#ifndef ANDROID_DEFAULT_CODE 
    if (source == NULL)
        return OK;
#endif // #ifndef ANDROID_DEFAULT_CODE

    source->byeReceived();

    return OK;
}

status_t ARTPConnection::parseSR(
        StreamInfo *s, const uint8_t *data, size_t size) {
    size_t RC = data[0] & 0x1f;

    if (size < (7 + RC * 6) * 4) {
        // Packet too short for the minimal SR header.
        return -1;
    }

    uint32_t id = u32at(&data[4]);
    uint64_t ntpTime = u64at(&data[8]);
    uint32_t rtpTime = u32at(&data[16]);

#if 0
    LOGI("XXX timeUpdate: ssrc=0x%08x, rtpTime %u == ntpTime %.3f",
         id,
         rtpTime,
         (ntpTime >> 32) + (double)(ntpTime & 0xffffffff) / (1ll << 32));
#endif

    sp<ARTPSource> source = findSource(s, id);

#ifndef ANDROID_DEFAULT_CODE 
    if (source == NULL)
        return 0;
    if ((mFlags & kFakeTimestamps) == 0) {
        source->timeUpdate(rtpTime, ntpTime);
    }
    source->SRUpdate(ntpTime);
#else
    source->timeUpdate(rtpTime, ntpTime);
#endif // #ifndef ANDROID_DEFAULT_CODE

    return 0;
}

sp<ARTPSource> ARTPConnection::findSource(StreamInfo *info, uint32_t srcId) {
#ifndef ANDROID_DEFAULT_CODE 
    if (info->mIsSSRCSet && srcId != (uint32_t)info->mSSRC) {
        LOGW("ignore invalid ssrc %x, expect %x", srcId, info->mSSRC);
        return NULL;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    sp<ARTPSource> source;
    ssize_t index = info->mSources.indexOfKey(srcId);
    if (index < 0) {
        index = info->mSources.size();

        source = new ARTPSource(
                srcId, info->mSessionDesc, info->mIndex, info->mNotifyMsg);

        info->mSources.add(srcId, source);
#ifndef ANDROID_DEFAULT_CODE 
        LOGI("new source ssrc %x", srcId);
        info->mIsSSRCSet = true;
        info->mSSRC = srcId;
        if (info->mTimeUpdated) {
            LOGI("RTP comes back later than response of PLAY, do a timeUpdate");
            source->timeUpdate(info->mRTPTime, info->mNTPTime, info->mRTPSeqNo);
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
    } else {
        source = info->mSources.valueAt(index);
    }

    return source;
}

void ARTPConnection::injectPacket(int index, const sp<ABuffer> &buffer) {
    sp<AMessage> msg = new AMessage(kWhatInjectPacket, id());
    msg->setInt32("index", index);
    msg->setObject("buffer", buffer);
    msg->post();
}

void ARTPConnection::onInjectPacket(const sp<AMessage> &msg) {
    int32_t index;
    CHECK(msg->findInt32("index", &index));

    sp<RefBase> obj;
    CHECK(msg->findObject("buffer", &obj));

    sp<ABuffer> buffer = static_cast<ABuffer *>(obj.get());

    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()
           && it->mRTPSocket != index && it->mRTCPSocket != index) {
        ++it;
    }

    if (it == mStreams.end()) {
        TRESPASS();
    }

    StreamInfo *s = &*it;

#ifndef ANDROID_DEFAULT_CODE 
    // touch on both RTP and RTCP packets
    s->mNumPacketsTouched++;
    s->mLastPacketTimeUs = ALooper::GetNowUs();
#endif // #ifndef ANDROID_DEFAULT_CODE

    status_t err;
    if (it->mRTPSocket == index) {
        err = parseRTP(s, buffer);
    } else {
        err = parseRTCP(s, buffer);
    }
}

#ifndef ANDROID_DEFAULT_CODE
void ARTPConnection::fakeTimestamps() {
    (new AMessage(kWhatFakeTimestamps, id()))->post();
}

void ARTPConnection::onFakeTimestamps() {
    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()) {
        StreamInfo &info = *it++;

        for (size_t j = 0; j < info.mSources.size(); ++j) {
            sp<ARTPSource> source = info.mSources.valueAt(j);

            if (!source->timeEstablished()) {
                source->timeUpdate(0, 0);
                source->timeUpdate(0 + 90000, 0x100000000ll);

                mFlags |= kFakeTimestamps;
            }
        }
    }
}

void ARTPConnection::resetTimestamps() {
    (new AMessage(kWhatResetTimestamps, id()))->post();
}

void ARTPConnection::onResetTimestamps() {
    mFlags |= kFakeTimestamps;
    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()) {
        StreamInfo &info = *it++;
        info.mTimeUpdated = false;

        for (size_t j = 0; j < info.mSources.size(); ++j) {
            sp<ARTPSource> source = info.mSources.valueAt(j);
            source->resetTimes();
        }
    }
}

void ARTPConnection::startCheckAlives() {
    (new AMessage(kWhatStartCheckAlives, id()))->post();
}

void ARTPConnection::stopCheckAlives() {
    (new AMessage(kWhatStopCheckAlives, id()))->post();
}

void ARTPConnection::onStartCheckAlives() {
    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()) {
        it->mCheckPending = false;
        it->mNumPacketsTouched = 0;
        it->mLastPacketTimeUs = ALooper::GetNowUs();

        sp<AMessage> check = new AMessage(kWhatCheckAlive, id());
        it->mCheckGeneration = (int32_t)check.get();
        check->setInt32("generation", it->mCheckGeneration);
        check->setSize("stream-index", it->mIndex);
        check->post(kAccessUnitTimeoutUs);
        LOGD("start check alives %x %d", it->mCheckGeneration, it->mIndex);
        ++it;
    }
}

void ARTPConnection::onStopCheckAlives() {
    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()) {
        LOGD("stop check alives %x %d", it->mCheckGeneration, it->mIndex);
        (it++)->mCheckPending = true;
    }
}

void ARTPConnection::onCheckAlive(const sp<AMessage> &msg) {
    size_t streamIndex;
    int32_t generation;
    CHECK(msg->findSize("stream-index", &streamIndex));
    CHECK(msg->findInt32("generation", &generation));

    List<StreamInfo>::iterator info = mStreams.begin();
    while (info != mStreams.end()) {
        if (info->mIndex == streamIndex)
            break;
        info++;
    }
    if (info == mStreams.end())
        return;

    if (info->mCheckPending || generation != info->mCheckGeneration)
        return;

    int64_t nowUs = ALooper::GetNowUs();
    if (nowUs - info->mLastPacketTimeUs <= kAccessUnitTimeoutUs) {
        msg->post(kCheckAliveInterval);
        return;
    }

    int64_t maxExpectedTimeoutUs = 0;
    for (size_t j = 0; j < info->mSources.size(); ++j) {
        sp<ARTPSource> source = info->mSources.valueAt(j);
        int64_t timeoutUs = source->getExpectedTimeoutUs();
        if (timeoutUs > maxExpectedTimeoutUs)
            maxExpectedTimeoutUs = timeoutUs;
    }

    if (maxExpectedTimeoutUs > 0) {
        // in case we have received a packet of big duration
        // we may receive nothing during the period, don't trigger EOS
        int64_t diff = maxExpectedTimeoutUs + kAccessUnitTimeoutUsMargin - nowUs;
        if (diff > 0) {
            LOGI("ARTPConnection: don't trigger timeout, our expect timeout is"
                   " %lld, now: %lld", maxExpectedTimeoutUs, nowUs);
            msg->post(diff);
            return;
        }
    }

    for (size_t j = 0; j < info->mSources.size(); ++j) {
        sp<ARTPSource> source = info->mSources.valueAt(j);
        source->flushRTPPackets();
    }

    msg->post(kCheckAliveInterval);
    info->mLastPacketTimeUs = nowUs;

    sp<AMessage> notify = info->mNotifyMsg->dup();
    notify->setInt32("stream-timeout", true);
    notify->post();
}

void ARTPConnection::enableSRTimestamp() {
    (new AMessage(kWhatEnableSR, id()))->post();
}

void ARTPConnection::timeUpdate(int socket, uint32_t rtpTime, uint64_t ntpTime, uint32_t rtpSeq) {
    sp<AMessage> msg = new AMessage(kWhatTimeUpdate, id());
    msg->setInt32("rtp-socket", socket);
    msg->setInt32("rtp-time", rtpTime);
    msg->setInt64("ntp-time", ntpTime);
    msg->setInt32("rtp-seq", rtpSeq);
    msg->post();
}

void ARTPConnection::onTimeUpdate(const sp<AMessage> &msg) {
    int socket;
    uint32_t rtpTime;
    uint64_t ntpTime;
    uint32_t rtpSeq;
    CHECK(msg->findInt32("rtp-socket", &socket));
    CHECK(msg->findInt32("rtp-time", (int32_t*)&rtpTime));
    CHECK(msg->findInt64("ntp-time", (int64_t*)&ntpTime));
    CHECK(msg->findInt32("rtp-seq", (int32_t*)&rtpSeq));

    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()) {
        StreamInfo &info = *it++;
        if (info.mRTPSocket != socket)
            continue;

        info.mTimeUpdated = true;
        info.mRTPTime = rtpTime;
        info.mNTPTime = ntpTime;
        info.mRTPSeqNo = rtpSeq;
        for (size_t j = 0; j < info.mSources.size(); ++j) {
            sp<ARTPSource> source = info.mSources.valueAt(j);
            source->timeUpdate(rtpTime, ntpTime, rtpSeq);
        }
    }
}

void ARTPConnection::useFirstTimestamp() {
    (new AMessage(kWhatUseFirstTimestamp, id()))->post();
}

void ARTPConnection::onUseFirstTimestamp() {
    // skip fake timestamp model
    if (mFlags & kFakeTimestamps)
        return;

    bool updated = false;
    List<StreamInfo>::iterator it = mStreams.begin();
    while (it != mStreams.end()) {
        StreamInfo &info = *it++;

        for (size_t j = 0; j < info.mSources.size(); ++j) {
            sp<ARTPSource> source = info.mSources.valueAt(j);
            updated = updated || source->timeEstablished();
        }
    }

    // skip if one stream has established
    if (updated)
        return;

    LOGI("use first timestamp model for bad server");
    it = mStreams.begin();
    while (it != mStreams.end()) {
        StreamInfo &info = *it++;

        for (size_t j = 0; j < info.mSources.size(); ++j) {
            sp<ARTPSource> source = info.mSources.valueAt(j);
            source->useFirstTimestamp();
        }
    }
}

void ARTPConnection::postInjectEvent() {
    if (mPollEventPending) {
        return;
    }

    sp<AMessage> msg = new AMessage(kWhatInjectPollStreams, id());
    msg->post(kInjectPollInterval);

    mPollEventPending = true;
}

void ARTPConnection::onPostInjectEvent() {
    mPollEventPending = false;
    postInjectEvent();
    sendRR();
}

#endif // #ifndef ANDROID_DEFAULT_CODE

}  // namespace android

