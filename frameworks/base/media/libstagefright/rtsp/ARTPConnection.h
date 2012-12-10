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

#ifndef A_RTP_CONNECTION_H_

#define A_RTP_CONNECTION_H_

#include <media/stagefright/foundation/AHandler.h>
#include <utils/List.h>

namespace android {

struct ABuffer;
struct ARTPSource;
struct ASessionDescription;

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
struct APacketSource;

struct RtspBitRateAdapParam{
		sp<APacketSource> mAPacketSource;
		size_t mNaduFreq;
	};

#endif

struct ARTPConnection : public AHandler {
    enum Flags {
#ifndef ANDROID_DEFAULT_CODE
        kFakeTimestamps      = 1,
#endif // #ifndef ANDROID_DEFAULT_CODE
        kRegularlyRequestFIR = 2,
    };

    ARTPConnection(uint32_t flags = 0);

    void addStream(
            int rtpSocket, int rtcpSocket,
            const sp<ASessionDescription> &sessionDesc, size_t index,
            const sp<AMessage> &notify,
#ifndef ANDROID_DEFAULT_CODE 
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		bool injected, int32_t* pSSRC = NULL,RtspBitRateAdapParam* pbitRateAdapParam  = NULL);
#else
            bool injected, int32_t* pSSRC = NULL);
#endif

#else
            bool injected);
#endif // #ifndef ANDROID_DEFAULT_CODE

    void removeStream(int rtpSocket, int rtcpSocket);

    void injectPacket(int index, const sp<ABuffer> &buffer);

    // Creates a pair of UDP datagram sockets bound to adjacent ports
    // (the rtpSocket is bound to an even port, the rtcpSocket to the
    // next higher port).
    static void MakePortPair(
#ifndef ANDROID_DEFAULT_CODE 
            int *rtpSocket, int *rtcpSocket, unsigned *rtpPort,
            int min = 0, int max = 65535);
#else
            int *rtpSocket, int *rtcpSocket, unsigned *rtpPort);
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
    void fakeTimestamps();
    void resetTimestamps();
    void startCheckAlives();
    void stopCheckAlives();
    void enableSRTimestamp();
    void timeUpdate(int socket, uint32_t rtpTime, uint64_t npt, uint32_t rtpSeq);
    void useFirstTimestamp();
#endif // #ifndef ANDROID_DEFAULT_CODE

protected:
    virtual ~ARTPConnection();
    virtual void onMessageReceived(const sp<AMessage> &msg);

private:
    enum {
        kWhatAddStream,
        kWhatRemoveStream,
        kWhatPollStreams,
        kWhatInjectPacket,
#ifndef ANDROID_DEFAULT_CODE 
        kWhatFakeTimestamps,
        kWhatInjectPollStreams,
        kWhatResetTimestamps,
        kWhatStartCheckAlives,
        kWhatStopCheckAlives,
        kWhatCheckAlive,
        kWhatEnableSR,
        kWhatTimeUpdate,
        kWhatUseFirstTimestamp,
#endif // #ifndef ANDROID_DEFAULT_CODE
    };

    static const int64_t kSelectTimeoutUs;

    uint32_t mFlags;

    struct StreamInfo;
    List<StreamInfo> mStreams;

    bool mPollEventPending;
    int64_t mLastReceiverReportTimeUs;

    void onAddStream(const sp<AMessage> &msg);
    void onRemoveStream(const sp<AMessage> &msg);
    void onPollStreams();
    void onInjectPacket(const sp<AMessage> &msg);
    void onSendReceiverReports();
#ifndef ANDROID_DEFAULT_CODE 
    void onFakeTimestamps();
    void sendRR();
    void onResetTimestamps();
    void onStartCheckAlives();
    void onStopCheckAlives();
    void onCheckAlive(const sp<AMessage> &msg);
    void onTimeUpdate(const sp<AMessage> &msg);
    void onUseFirstTimestamp();
    void postInjectEvent();
    void onPostInjectEvent();
#endif // #ifndef ANDROID_DEFAULT_CODE

    status_t receive(StreamInfo *info, bool receiveRTP);

    status_t parseRTP(StreamInfo *info, const sp<ABuffer> &buffer);
    status_t parseRTCP(StreamInfo *info, const sp<ABuffer> &buffer);
    status_t parseSR(StreamInfo *info, const uint8_t *data, size_t size);
    status_t parseBYE(StreamInfo *info, const uint8_t *data, size_t size);

    sp<ARTPSource> findSource(StreamInfo *info, uint32_t id);

    void postPollEvent();

    DISALLOW_EVIL_CONSTRUCTORS(ARTPConnection);
};

}  // namespace android

#endif  // A_RTP_CONNECTION_H_
