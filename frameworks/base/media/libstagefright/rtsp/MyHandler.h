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

#ifndef MY_HANDLER_H_

#define MY_HANDLER_H_

//#define LOG_NDEBUG 0
#ifndef ANDROID_DEFAULT_CODE 
#undef LOG_TAG
#endif // #ifndef ANDROID_DEFAULT_CODE
#define LOG_TAG "MyHandler"
#include <utils/Log.h>

#include "APacketSource.h"
#include "ARTPConnection.h"
#include "ARTSPConnection.h"
#include "ASessionDescription.h"

#include <ctype.h>
#include <cutils/properties.h>

#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/ALooper.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/MediaErrors.h>
#ifndef ANDROID_DEFAULT_CODE 
#include <media/stagefright/MetaData.h>
#endif // #ifndef ANDROID_DEFAULT_CODE

#include <arpa/inet.h>
#include <sys/socket.h>
#include <netdb.h>

#include "HTTPBase.h"

#ifndef ANDROID_DEFAULT_CODE 
#include "ARTPSource.h"
//send 3 times for pokeAHole
static int kPokeAttempts = 3;
// timeout = (trials + 1) * 3s
static int kTimeoutTrials = 5;
// live streaming 60s
static int kTimeoutLiveTrials = 19;
#endif // #ifndef ANDROID_DEFAULT_CODE
// If no access units are received within 5 secs, assume that the rtp
// stream has ended and signal end of stream.
static int64_t kAccessUnitTimeoutUs = 10000000ll;

// If no access units arrive for the first 10 secs after starting the
// stream, assume none ever will and signal EOS or switch transports.
static int64_t kStartupTimeoutUs = 10000000ll;

static int64_t kDefaultKeepAliveTimeoutUs = 60000000ll;
#ifndef ANDROID_DEFAULT_CODE 
static int64_t kMaxSRNTPDiff = 60ll << 32;
static int64_t kSRTimeoutUs = 4000000ll;
static int64_t kTearDownTimeoutUs = 3000000ll;
static int kMaxInterleave = 60; //60s Now we just support poor interleave to 60s,avoid memory malloc fail.
#endif // #ifndef ANDROID_DEFAULT_CODE

namespace android {

static void MakeUserAgentString(AString *s) {
    s->setTo("stagefright/1.1 (Linux;Android ");

#if (PROPERTY_VALUE_MAX < 8)
#error "PROPERTY_VALUE_MAX must be at least 8"
#endif

    char value[PROPERTY_VALUE_MAX];
    property_get("ro.build.version.release", value, "Unknown");
    s->append(value);
    s->append(")");
}

static bool GetAttribute(const char *s, const char *key, AString *value) {
    value->clear();

    size_t keyLen = strlen(key);

    for (;;) {
        while (isspace(*s)) {
            ++s;
        }

        const char *colonPos = strchr(s, ';');

        size_t len =
            (colonPos == NULL) ? strlen(s) : colonPos - s;

        if (len >= keyLen + 1 && s[keyLen] == '=' && !strncmp(s, key, keyLen)) {
            value->setTo(&s[keyLen + 1], len - keyLen - 1);
            return true;
        }

        if (colonPos == NULL) {
            return false;
        }

        s = colonPos + 1;
    }
}

#ifndef ANDROID_DEFAULT_CODE 
static status_t MappingRTSPError(int32_t result) {
    switch(result) {
        case -100: // ENETDOWN
        case -101: // ENETUNREACH
        case -102: // ENETRESET
        case -103: // ECONNABORTED
        case -104: // ECONNRESET
        case -108: // ESHUTDOWN
        case -110: // ETIMEDOUT
        case -111: // ECONNREFUSED
        case -112: // EHOSTDOWN
        case -113: // EHOSTUNREACH
        case 503: // Service Unavailable
            return ERROR_CANNOT_CONNECT;
        case ERROR_UNSUPPORTED:
        case 415:
            return ERROR_UNSUPPORTED;
        case 403:
            return ERROR_FORBIDDEN;
        default:
            return UNKNOWN_ERROR;
    }
}
#endif // #ifndef ANDROID_DEFAULT_CODE

struct MyHandler : public AHandler {
    enum {
        kWhatConnected                  = 'conn',
        kWhatDisconnected               = 'disc',
        kWhatSeekDone                   = 'sdon',

        kWhatAccessUnit                 = 'accU',
        kWhatEOS                        = 'eos!',
        kWhatSeekDiscontinuity          = 'seeD',
        kWhatNormalPlayTimeMapping      = 'nptM',
    };

#ifndef ANDROID_DEFAULT_CODE 
    MyHandler(
            const char *url, const sp<ALooper> &looper,
            bool uidValid = false, uid_t uid = 0,
            const KeyedVector<String8, String8> *headers = NULL)
#else
    MyHandler(
            const char *url, const sp<ALooper> &looper,
            bool uidValid = false, uid_t uid = 0)
#endif // #ifndef ANDROID_DEFAULT_CODE
        : mUIDValid(uidValid),
          mUID(uid),
          mLooper(looper),
          mNetLooper(new ALooper),
          mConn(new ARTSPConnection(mUIDValid, mUID)),
          mRTPConn(new ARTPConnection),
          mOriginalSessionURL(url),
          mSessionURL(url),
          mSetupTracksSuccessful(false),
          mSeekPending(false),
          mFirstAccessUnit(true),
          mNTPAnchorUs(-1),
          mMediaAnchorUs(-1),
          mLastMediaTimeUs(0),
          mNumAccessUnitsReceived(0),
          mCheckPending(false),
          mCheckGeneration(0),
          mTryTCPInterleaving(false),
          mTryFakeRTCP(false),
          mReceivedFirstRTCPPacket(false),
          mReceivedFirstRTPPacket(false),
          mSeekable(false),
          mKeepAliveTimeoutUs(kDefaultKeepAliveTimeoutUs),
#ifndef ANDROID_DEFAULT_CODE 
          mKeepAliveGeneration(0),
          // please add new member below this line
          mPausePending(false),
          mFirstAccessUnitNTP(0),
          mRTSPNetLooper(new ALooper),
          mLastSeekTimeTime(-1),
          mIntSeverError(false),//haizhen
          mTiouPending(false),//haizhen for 'tiou' AMessage. if pause or seek need cancel 'tiou'
          mTiouGeneration(0),//haizhen  for 'tiou' AMessage. we only need handle the the new 'tiou' 
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
          mSwitchingTCP(false),
          mTryingTCPIndex(0),
#endif
          mIsLive(false),
          mSkipDescribe(false),
          mPlaySent(false),
          mHaveVideo(false),
          mHaveAudio(false),
          mFinalStatus(OK),
          mNPTPending(true),
          mPendingByPause(false),
          mInitNPT(0),
          mExited(false),
          mNTPBase(0),
          mNPTBase(0),
          mLastNPT(0),
          mNPTMode(false),
          mRegistered(false),
          mMinUDPPort(0),
          mMaxUDPPort(65535) {
#else
          mKeepAliveGeneration(0) {
#endif // #ifndef ANDROID_DEFAULT_CODE
        mNetLooper->setName("rtsp net");
        mNetLooper->start(false /* runOnCallingThread */,
                          false /* canCallJava */,
                          PRIORITY_HIGHEST);

#ifndef ANDROID_DEFAULT_CODE 
        mRTSPNetLooper->setName("rtsp looper");
        mRTSPNetLooper->start();
#endif // #ifndef ANDROID_DEFAULT_CODE

        // Strip any authentication info from the session url, we don't
        // want to transmit user/pass in cleartext.
        AString host, path, user, pass;
        unsigned port;
#ifndef ANDROID_DEFAULT_CODE 
        if (!(ARTSPConnection::ParseURL(
                    mSessionURL.c_str(), &host, &port, &path, &user, &pass))) {
            LOGE("invalid url %s", mSessionURL.c_str());
            return;
        }
#else
        CHECK(ARTSPConnection::ParseURL(
                    mSessionURL.c_str(), &host, &port, &path, &user, &pass));
#endif // #ifndef ANDROID_DEFAULT_CODE

        if (user.size() > 0) {
            mSessionURL.clear();
            mSessionURL.append("rtsp://");
            mSessionURL.append(host);
            mSessionURL.append(":");
            mSessionURL.append(StringPrintf("%u", port));
            mSessionURL.append(path);

            LOGI("rewritten session url: '%s'", mSessionURL.c_str());
        }

        mSessionHost = host;
#ifndef ANDROID_DEFAULT_CODE 
        parseHeaders(headers);
        // for rand allocate rtp/rtcp ports
        srand(time(NULL));
		mIsDarwinServer = false;
#endif // #ifndef ANDROID_DEFAULT_CODE

//for Bitrate-Adaptation
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		 mSupBitRateAdap = true;

		char value[PROPERTY_VALUE_MAX];
		 if(mSupBitRateAdap){
			if (property_get("media.stagefright.rtsp-bitRateAdap", value, NULL)
	                && (!strcmp(value, "0") || !strcasecmp(value, "false"))) {
				mSupBitRateAdap = false;
			}

		}
		else{
			if (property_get("media.stagefright.rtsp-bitRateAdap", value, NULL)
	                && (!strcmp(value, "1") || !strcasecmp(value, "true"))) {
				mSupBitRateAdap = true;
			}

		}
		//for 'bitrate-Adap' need special handle for DSS
#endif
    }

    void connect(const sp<AMessage> &doneMsg) {
        mDoneMsg = doneMsg;

        mLooper->registerHandler(this);
#ifndef ANDROID_DEFAULT_CODE 
        mRTSPNetLooper->registerHandler(mConn);
#else
        mLooper->registerHandler(mConn);
#endif // #ifndef ANDROID_DEFAULT_CODE
        (1 ? mNetLooper : mLooper)->registerHandler(mRTPConn);
#ifndef ANDROID_DEFAULT_CODE
        mRegistered = true;
#endif

        sp<AMessage> notify = new AMessage('biny', id());
        mConn->observeBinaryData(notify);

        sp<AMessage> reply = new AMessage('conn', id());
        mConn->connect(mOriginalSessionURL.c_str(), reply);
    }

    void disconnect(const sp<AMessage> &doneMsg) {
        mDoneMsg = doneMsg;

#ifndef ANDROID_DEFAULT_CODE
        stopTCPTrying();
#endif
        (new AMessage('abor', id()))->post();
    }

    void seek(int64_t timeUs, const sp<AMessage> &doneMsg) {
#ifndef ANDROID_DEFAULT_CODE 
        sp<AMessage> msg = new AMessage('see0', id());
#else
        sp<AMessage> msg = new AMessage('seek', id());
#endif // #ifndef ANDROID_DEFAULT_CODE
        msg->setInt64("time", timeUs);
        msg->setMessage("doneMsg", doneMsg);
        msg->post();
    }

#ifndef ANDROID_DEFAULT_CODE 
    int64_t getNormalPlayTimeUs(int64_t timeUs) {
        if (mNPTPending && !mPendingByPause){
	    LOGI("[rtsp]@getNormalPlayTimeUs,mInitNPT=%lld",mInitNPT);
            return mInitNPT;
        }
            
        int64_t t = timeUs - mNTPBase + mNPTBase;
        if (t < 0){
	    LOGI("[rtsp]@getNormalPlayTimeUs,timeUs=%lld,mNTPBase=%lld,mNPTBase=%lld",timeUs,mNTPBase,mNPTBase);
            t = 0;
	}
            
        return t;
    }
#else
    int64_t getNormalPlayTimeUs() {
        int64_t maxTimeUs = 0;
        for (size_t i = 0; i < mTracks.size(); ++i) {
            int64_t timeUs = mTracks.editItemAt(i).mPacketSource
                ->getNormalPlayTimeUs();

            if (i == 0 || timeUs > maxTimeUs) {
                maxTimeUs = timeUs;
            }
        }

        return maxTimeUs;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    static void addRR(const sp<ABuffer> &buf) {
        uint8_t *ptr = buf->data() + buf->size();
        ptr[0] = 0x80 | 0;
        ptr[1] = 201;  // RR
        ptr[2] = 0;
        ptr[3] = 1;
        ptr[4] = 0xde;  // SSRC
        ptr[5] = 0xad;
        ptr[6] = 0xbe;
        ptr[7] = 0xef;

        buf->setRange(0, buf->size() + 8);
    }

    static void addSDES(int s, const sp<ABuffer> &buffer) {
        struct sockaddr_in addr;
        socklen_t addrSize = sizeof(addr);
        CHECK_EQ(0, getsockname(s, (sockaddr *)&addr, &addrSize));

        uint8_t *data = buffer->data() + buffer->size();
        data[0] = 0x80 | 1;
        data[1] = 202;  // SDES
        data[4] = 0xde;  // SSRC
        data[5] = 0xad;
        data[6] = 0xbe;
        data[7] = 0xef;

        size_t offset = 8;

        data[offset++] = 1;  // CNAME

        AString cname = "stagefright@";
        cname.append(inet_ntoa(addr.sin_addr));
        data[offset++] = cname.size();

        memcpy(&data[offset], cname.c_str(), cname.size());
        offset += cname.size();

        data[offset++] = 6;  // TOOL

        AString tool;
        MakeUserAgentString(&tool);

        data[offset++] = tool.size();

        memcpy(&data[offset], tool.c_str(), tool.size());
        offset += tool.size();

        data[offset++] = 0;

        if ((offset % 4) > 0) {
            size_t count = 4 - (offset % 4);
            switch (count) {
                case 3:
                    data[offset++] = 0;
                case 2:
                    data[offset++] = 0;
                case 1:
                    data[offset++] = 0;
            }
        }

        size_t numWords = (offset / 4) - 1;
        data[2] = numWords >> 8;
        data[3] = numWords & 0xff;

        buffer->setRange(buffer->offset(), buffer->size() + offset);
    }

#ifndef ANDROID_DEFAULT_CODE 
    bool doPokeAHole(int socket, struct sockaddr_in addr, int port, const sp<ABuffer>& buf) {
        addr.sin_port = htons(port);
        for(int i = 0; i < kPokeAttempts; i++) {
            ssize_t n = sendto(
                    socket, buf->data(), buf->size(), 0,
                    (const sockaddr *)&addr, sizeof(addr));
            if (n == (ssize_t)buf->size())
                return true;
        }
        LOGE("failed to poke a hole for port %d", port);
        return false;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    // In case we're behind NAT, fire off two UDP packets to the remote
    // rtp/rtcp ports to poke a hole into the firewall for future incoming
    // packets. We're going to send an RR/SDES RTCP packet to both of them.
#ifndef ANDROID_DEFAULT_CODE 
    bool pokeAHole(int rtpSocket, int rtcpSocket, const AString &transport, 
            int rtpPortOurs) {
#else
    bool pokeAHole(int rtpSocket, int rtcpSocket, const AString &transport) {
#endif // #ifndef ANDROID_DEFAULT_CODE
        struct sockaddr_in addr;
        memset(addr.sin_zero, 0, sizeof(addr.sin_zero));
        addr.sin_family = AF_INET;

        AString source;
        AString server_port;
        if (!GetAttribute(transport.c_str(),
                          "source",
                          &source)) {
            LOGW("Missing 'source' field in Transport response. Using "
                 "RTSP endpoint address.");

            struct hostent *ent = gethostbyname(mSessionHost.c_str());
            if (ent == NULL) {
                LOGE("Failed to look up address of session host '%s'",
                     mSessionHost.c_str());

                return false;
            }

            addr.sin_addr.s_addr = *(in_addr_t *)ent->h_addr;
        } else {
            addr.sin_addr.s_addr = inet_addr(source.c_str());
        }

        if (!GetAttribute(transport.c_str(),
                                 "server_port",
                                 &server_port)) {
            LOGI("Missing 'server_port' field in Transport response.");
            return false;
        }

        int rtpPort, rtcpPort;
#ifndef ANDROID_DEFAULT_CODE 
        // check whether client_port is modified by NAT
        // DO NOT send packets for this type of NAT
        AString client_port;
        if (GetAttribute(transport.c_str(), "client_port", &client_port)) {
            if (sscanf(client_port.c_str(), "%d-%d", &rtpPort, &rtcpPort) != 2
                    || rtpPort <= 0 || rtpPort > 65535
                    || rtcpPort <=0 || rtcpPort > 65535
                    || rtcpPort != rtpPort + 1
                    || (rtpPort & 1) != 0) {
                return true;
            }
            if (rtpPort != rtpPortOurs) {
                LOGW("pokeAHole(): NAT has modified our client_port from"
                        " %d to %d", rtpPortOurs, rtpPort);
                return true;
            }
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        if (sscanf(server_port.c_str(), "%d-%d", &rtpPort, &rtcpPort) != 2
                || rtpPort <= 0 || rtpPort > 65535
                || rtcpPort <=0 || rtcpPort > 65535
                || rtcpPort != rtpPort + 1) {
            LOGE("Server picked invalid RTP/RTCP port pair %s,"
                 " RTP port must be even, RTCP port must be one higher.",
                 server_port.c_str());

            return false;
        }

        if (rtpPort & 1) {
            LOGW("Server picked an odd RTP port, it should've picked an "
                 "even one, we'll let it pass for now, but this may break "
                 "in the future.");
        }

        if (addr.sin_addr.s_addr == INADDR_NONE) {
            return true;
        }

        if (IN_LOOPBACK(ntohl(addr.sin_addr.s_addr))) {
            // No firewalls to traverse on the loopback interface.
            return true;
        }

        // Make up an RR/SDES RTCP packet.
        sp<ABuffer> buf = new ABuffer(65536);
        buf->setRange(0, 0);
        addRR(buf);
        addSDES(rtpSocket, buf);

#ifndef ANDROID_DEFAULT_CODE 
        bool success = true;
        success = success && doPokeAHole(rtpSocket, addr, rtpPort, buf);
        success = success && doPokeAHole(rtcpSocket, addr, rtcpPort, buf);
        if (!source.empty()) {
            struct hostent *ent = gethostbyname(mSessionHost.c_str());
            if (ent == NULL) {
                LOGE("Failed to look up address of session host '%s'",
                     mSessionHost.c_str());

                return false;
            }

            if (addr.sin_addr.s_addr != *(in_addr_t *)ent->h_addr) {
                addr.sin_addr.s_addr = *(in_addr_t *)ent->h_addr;
                success = success && doPokeAHole(rtpSocket, addr, rtpPort, buf);
                success = success && doPokeAHole(rtcpSocket, addr, rtcpPort, buf);
            }
        }
        return success;
#else
        addr.sin_port = htons(rtpPort);

        ssize_t n = sendto(
                rtpSocket, buf->data(), buf->size(), 0,
                (const sockaddr *)&addr, sizeof(addr));

        if (n < (ssize_t)buf->size()) {
            LOGE("failed to poke a hole for RTP packets");
            return false;
        }

        addr.sin_port = htons(rtcpPort);

        n = sendto(
                rtcpSocket, buf->data(), buf->size(), 0,
                (const sockaddr *)&addr, sizeof(addr));

        if (n < (ssize_t)buf->size()) {
            LOGE("failed to poke a hole for RTCP packets");
            return false;
        }

        LOGV("successfully poked holes.");

        return true;
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

    virtual void onMessageReceived(const sp<AMessage> &msg) {
#ifndef ANDROID_DEFAULT_CODE 
        if (mExited)
            return;
#endif // #ifndef ANDROID_DEFAULT_CODE
        switch (msg->what()) {
            case 'conn':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("connection request completed with result %d (%s)",
                     result, strerror(-result));

#ifndef ANDROID_DEFAULT_CODE 
                if (mDoneMsg != NULL)
                    mDoneMsg->setInt32("unsupport-video", 0);
#endif
                if (result == OK) {
#ifndef ANDROID_DEFAULT_CODE 
                    if (mSkipDescribe) {
                        setupTrack(1);
                        break;
                    }
#endif // #ifndef ANDROID_DEFAULT_CODE
                    AString request;
                    request = "DESCRIBE ";
                    request.append(mSessionURL);
                    request.append(" RTSP/1.0\r\n");
                    request.append("Accept: application/sdp\r\n");
                    request.append("\r\n");

                    sp<AMessage> reply = new AMessage('desc', id());
                    mConn->sendRequest(request.c_str(), reply);
                } else {
#ifndef ANDROID_DEFAULT_CODE 
                    mFinalStatus = MappingRTSPError(result);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    (new AMessage('disc', id()))->post();
                }
                break;
            }

            case 'disc':
            {
                ++mKeepAliveGeneration;

                int32_t reconnect;
                if (msg->findInt32("reconnect", &reconnect) && reconnect) {
#ifndef ANDROID_DEFAULT_CODE 
                    mFinalStatus = OK;
#endif // #ifndef ANDROID_DEFAULT_CODE
                    sp<AMessage> reply = new AMessage('conn', id());
                    mConn->connect(mOriginalSessionURL.c_str(), reply);
                } else {
#ifndef ANDROID_DEFAULT_CODE 
                    LOGI("send eos to all tracks in disc");
                    for (size_t i = 0; i < mTracks.size(); ++i) {
                        sp<APacketSource> s = mTracks.editItemAt(i).mPacketSource;
                        if (s != NULL)
                            s->signalEOS(ERROR_END_OF_STREAM);
                    }
#endif // #ifndef ANDROID_DEFAULT_CODE
                    (new AMessage('quit', id()))->post();
                }
                break;
            }

            case 'desc':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("DESCRIBE completed with result %d (%s)",
                     result, strerror(-result));

                if (result == OK) {
                    sp<RefBase> obj;
                    CHECK(msg->findObject("response", &obj));
                    sp<ARTSPResponse> response =
                        static_cast<ARTSPResponse *>(obj.get());

                    if (response->mStatusCode == 302) {
                        ssize_t i = response->mHeaders.indexOfKey("location");
                        CHECK_GE(i, 0);

                        mSessionURL = response->mHeaders.valueAt(i);

                        AString request;
                        request = "DESCRIBE ";
                        request.append(mSessionURL);
                        request.append(" RTSP/1.0\r\n");
                        request.append("Accept: application/sdp\r\n");
                        request.append("\r\n");

                        sp<AMessage> reply = new AMessage('desc', id());
                        mConn->sendRequest(request.c_str(), reply);
                        break;
                    }

                    if (response->mStatusCode != 200) {
#ifndef ANDROID_DEFAULT_CODE 
                        result = response->mStatusCode;
#else
                        result = UNKNOWN_ERROR;
#endif // #ifndef ANDROID_DEFAULT_CODE
                    } else {

#ifndef ANDROID_DEFAULT_CODE
						//forward the server info (whether is DarwinServer) to ARTPConncetion
						//DSS 6.0.3 can not handle pss0 very good, so we will not send PSS0 to DSS
						ssize_t i = -1; 
						i = response->mHeaders.indexOfKey("server");
                       	if(i >= 0){ //if has Server header
                       		mServerInfo = response->mHeaders.valueAt(i);
                            checkServer();
                        }
					   
#endif		

                        mSessionDesc = new ASessionDescription;

                        mSessionDesc->setTo(
                                response->mContent->data(),
                                response->mContent->size());

                        if (!mSessionDesc->isValid()) {
                            LOGE("Failed to parse session description.");
                            result = ERROR_MALFORMED;
                        } else {
#ifndef ANDROID_DEFAULT_CODE 
                            int64_t tmp;
                            mIsLive = !mSessionDesc->getDurationUs(&tmp);
#endif // #ifndef ANDROID_DEFAULT_CODE
                            ssize_t i = response->mHeaders.indexOfKey("content-base");
                            if (i >= 0) {
                                mBaseURL = response->mHeaders.valueAt(i);
                            } else {
                                i = response->mHeaders.indexOfKey("content-location");
                                if (i >= 0) {
                                    mBaseURL = response->mHeaders.valueAt(i);
                                } else {
                                    mBaseURL = mSessionURL;
                                }
                            }

                            if (!mBaseURL.startsWith("rtsp://")) {
                                // Some misbehaving servers specify a relative
                                // URL in one of the locations above, combine
                                // it with the absolute session URL to get
                                // something usable...

                                LOGW("Server specified a non-absolute base URL"
                                     ", combining it with the session URL to "
                                     "get something usable...");

                                AString tmp;
                                CHECK(MakeURL(
                                            mSessionURL.c_str(),
                                            mBaseURL.c_str(),
                                            &tmp));

                                mBaseURL = tmp;
                            }

#ifndef ANDROID_DEFAULT_CODE
                            LOGI("base url %s", mBaseURL.c_str());
#endif
                            if (mSessionDesc->countTracks() < 2) {
                                // There's no actual tracks in this session.
                                // The first "track" is merely session meta
                                // data.

                                LOGW("Session doesn't contain any playable "
                                     "tracks. Aborting.");
                                result = ERROR_UNSUPPORTED;
                            } else {
                                setupTrack(1);
                            }
                        }
                    }
                }

                if (result != OK) {
#ifndef ANDROID_DEFAULT_CODE 
                    mFinalStatus = MappingRTSPError(result);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    sp<AMessage> reply = new AMessage('disc', id());
                    mConn->disconnect(reply);
                }
                break;
            }

            case 'setu':
            {
                size_t index;
                CHECK(msg->findSize("index", &index));

                TrackInfo *track = NULL;
                size_t trackIndex;
                if (msg->findSize("track-index", &trackIndex)) {
#ifndef ANDROID_DEFAULT_CODE 
                    if (mTracks.size() == 0) {
                        LOGW("SETUP %d done after abor", trackIndex);
                        break;
                    }
#endif // #ifndef ANDROID_DEFAULT_CODE
                    track = &mTracks.editItemAt(trackIndex);
                }

                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("SETUP(%d) completed with result %d (%s)",
                     index, result, strerror(-result));

                if (result == OK) {
                    CHECK(track != NULL);

                    sp<RefBase> obj;
                    CHECK(msg->findObject("response", &obj));
                    sp<ARTSPResponse> response =
                        static_cast<ARTSPResponse *>(obj.get());

                    if (response->mStatusCode != 200) {
#ifndef ANDROID_DEFAULT_CODE 
                        result = response->mStatusCode;
#else
                        result = UNKNOWN_ERROR;
#endif // #ifndef ANDROID_DEFAULT_CODE
                    } else {
                        ssize_t i = response->mHeaders.indexOfKey("session");
                        CHECK_GE(i, 0);

                        mSessionID = response->mHeaders.valueAt(i);

                        mKeepAliveTimeoutUs = kDefaultKeepAliveTimeoutUs;
                        AString timeoutStr;
                        if (GetAttribute(
                                    mSessionID.c_str(), "timeout", &timeoutStr)) {
                            char *end;
                            unsigned long timeoutSecs =
                                strtoul(timeoutStr.c_str(), &end, 10);

                            if (end == timeoutStr.c_str() || *end != '\0') {
                                LOGW("server specified malformed timeout '%s'",
                                     timeoutStr.c_str());

                                mKeepAliveTimeoutUs = kDefaultKeepAliveTimeoutUs;
                            } else if (timeoutSecs < 15) {
                                LOGW("server specified too short a timeout "
                                     "(%lu secs), using default.",
                                     timeoutSecs);

                                mKeepAliveTimeoutUs = kDefaultKeepAliveTimeoutUs;
                            } else {
                                mKeepAliveTimeoutUs = timeoutSecs * 1000000ll;

                                LOGI("server specified timeout of %lu secs.",
                                     timeoutSecs);
                            }
                        }

                        i = mSessionID.find(";");
                        if (i >= 0) {
                            // Remove options, i.e. ";timeout=90"
                            mSessionID.erase(i, mSessionID.size() - i);
                        }

#ifndef ANDROID_DEFAULT_CODE
                        if (mServerInfo.empty()) {
                            i = response->mHeaders.indexOfKey("server");
                            if (i >= 0) {
                                mServerInfo = response->mHeaders.valueAt(i);
                                checkServer();
                            }
                        }
#endif

                        sp<AMessage> notify = new AMessage('accu', id());
                        notify->setSize("track-index", trackIndex);

                        i = response->mHeaders.indexOfKey("transport");
                        CHECK_GE(i, 0);

#ifndef ANDROID_DEFAULT_CODE 
                        int32_t ssrc = -1, *pSSRC = NULL;
#endif // #ifndef ANDROID_DEFAULT_CODE
                        if (!track->mUsingInterleavedTCP) {
                            AString transport = response->mHeaders.valueAt(i);

                            // We are going to continue even if we were
                            // unable to poke a hole into the firewall...
#ifndef ANDROID_DEFAULT_CODE 
                            AString val;
                            LOGI("transport %s", transport.c_str());
                            if (GetAttribute(transport.c_str(), "ssrc", &val)) {
                                char *end;
                                ssrc = strtoul(val.c_str(), &end, 16);
                                pSSRC = &ssrc;
                                LOGI("ssrc %s(%x)", val.c_str(), *pSSRC);
                            }
                            track->mTransport = transport;
                            pokeAHole(track->mRTPSocket,
                                      track->mRTCPSocket,
                                      transport,
                                      track->mRTPPort);
#else
                            pokeAHole(
                                    track->mRTPSocket,
                                    track->mRTCPSocket,
                                    transport);
#endif // #ifndef ANDROID_DEFAULT_CODE
                        }
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
						RtspBitRateAdapParam bitRateAdapParam;
						bitRateAdapParam.mAPacketSource = track->mPacketSource;
						bitRateAdapParam.mNaduFreq = track->mNADUFreq;
#endif
                        mRTPConn->addStream(
                                track->mRTPSocket, track->mRTCPSocket,
                                mSessionDesc, index,
#ifndef ANDROID_DEFAULT_CODE 
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
								notify, track->mUsingInterleavedTCP,pSSRC,&bitRateAdapParam);
#else
                                notify, track->mUsingInterleavedTCP, pSSRC);
#endif
#else
                                notify, track->mUsingInterleavedTCP);
#endif // #ifndef ANDROID_DEFAULT_CODE

                        mSetupTracksSuccessful = true;
                    }
                }

                if (result != OK) {
                    if (track) {
                        if (!track->mUsingInterleavedTCP) {
                            // Clear the tag
                            if (mUIDValid) {
                                HTTPBase::UnRegisterSocketUserTag(track->mRTPSocket);
                                HTTPBase::UnRegisterSocketUserTag(track->mRTCPSocket);
                            }

                            close(track->mRTPSocket);
                            close(track->mRTCPSocket);
                        }

#ifndef ANDROID_DEFAULT_CODE 
                        LOGI("send eos to track %d", trackIndex);
                        TrackInfo *info = &mTracks.editItemAt(trackIndex);
                        info->mPacketSource->signalEOS(ERROR_END_OF_STREAM);
#endif
                        mTracks.removeItemsAt(trackIndex);
                    }
#ifndef ANDROID_DEFAULT_CODE 
                    if (result != ERROR_UNSUPPORTED) {
                        LOGI("send eos to all tracks");
                        for(size_t i = 0; i < mTracks.size(); ++i) {
                            TrackInfo *info = &mTracks.editItemAt(i);
                            info->mPacketSource->signalEOS(ERROR_END_OF_STREAM);
                        }
                        disc(MappingRTSPError(result));
                        break;
                    } else if (mDoneMsg != NULL) {
                        int v;
                        if (msg->findInt32("unsupport-video", &v) && v) {
                            mDoneMsg->setInt32("unsupport-video", 1);
                        }
                    }
#endif // #ifndef ANDROID_DEFAULT_CODE
                }

                ++index;
                if (index < mSessionDesc->countTracks()) {
                    setupTrack(index);
                } else if (mSetupTracksSuccessful) {
                    ++mKeepAliveGeneration;
                    postKeepAlive();

#ifndef ANDROID_DEFAULT_CODE 
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                    if (mSwitchingTCP) {
                        mSwitchingTCP = false;
                        sp<AMessage> msg = new AMessage('ply1', id());
                         if (mLastSeekTimeTime != -1){
                        	//for some sever can't support play to seektime directly
                        	//without send play 0 first
                        	//for UDP transfering to TCP, we should play 0-> pause -> play seektime
                            //msg->setInt64("play-time", mLastSeekTimeTime);
                            mPlaySent = false; 
			    sp<AMessage> reply = new AMessage('tcpS', id());   																									                       	
 			    preSeek(mLastSeekTimeTime,reply);
                            seek(mLastSeekTimeTime, reply);
                            LOGI("Switching to TCP, seek to mLastSeekTimeTime=%lld",mLastSeekTimeTime);	
 																																						
                        }
                        else{
                            msg->setInt64("play-time", 0);
                            msg->post();
                        }
                        break;
                    }
#endif
                    // finish ARTSPConnection::connect here
                    // instread of in 'accu'
                    if (mDoneMsg != NULL) {
                        mDoneMsg->setInt32("result", OK);
                        mDoneMsg->post();
                        mDoneMsg = NULL;
                    }
#else
                    AString request = "PLAY ";
                    request.append(mSessionURL);
                    request.append(" RTSP/1.0\r\n");

                    request.append("Session: ");
                    request.append(mSessionID);
                    request.append("\r\n");

                    request.append("\r\n");

                    sp<AMessage> reply = new AMessage('play', id());
                    mConn->sendRequest(request.c_str(), reply);
#endif // #ifndef ANDROID_DEFAULT_CODE
                } else {
#ifndef ANDROID_DEFAULT_CODE 
                    if (!mHaveAudio && !mHaveVideo) {
                        mFinalStatus = ERROR_UNSUPPORTED;
                    }
#endif // #ifndef ANDROID_DEFAULT_CODE
                    sp<AMessage> reply = new AMessage('disc', id());
                    mConn->disconnect(reply);
                }
                break;
            }
#ifndef ANDROID_DEFAULT_CODE //haizhen
					case 'tcpS' :
					{
						int32_t result;
						CHECK(msg->findInt32("result",&result));
						LOGI("Switching to TCP,SEEK completed with result %d(%s)",
								result,strerror(-result));
						break;	
					} 
#endif      

#ifndef ANDROID_DEFAULT_CODE 
            // split play from ARTSPController::connect
            // to give decoder a chance to find error
            case 'ply1':
            {
                AString request = "PLAY ";
#ifndef ANDROID_DEFAULT_CODE
                mPlaySent = true;
                request.append(mBaseURL);
#else
                request.append(mSessionURL);
#endif
                request.append(" RTSP/1.0\r\n");

                request.append("Session: ");
                request.append(mSessionID);
                request.append("\r\n");
#ifndef ANDROID_DEFAULT_CODE 
                int64_t timeUs;
                if (msg->findInt64("play-time", &timeUs)) {
                    LOGI("play-time %lld", timeUs);
                    request.append(
                            StringPrintf("Range: npt=%lld-\r\n", timeUs / 1000000ll));
                }
#endif // #ifndef ANDROID_DEFAULT_CODE

                request.append("\r\n");

                sp<AMessage> reply = new AMessage('play', id());
#ifndef ANDROID_DEFAULT_CODE 
                int32_t result;
                if (msg->findInt32("play", &result) && result) {
                    reply->setInt32("play", true);
                }
                int32_t isPaused;  //haizhen
                if(msg->findInt32("paus",&isPaused) && isPaused) {
                    mPendingByPause = true;
                    mNPTPending = true;
                    reply->setInt32("paus",true);
                }
#endif // #ifndef ANDROID_DEFAULT_CODE
                mConn->sendRequest(request.c_str(), reply);
                break;
            }
#endif // #ifndef ANDROID_DEFAULT_CODE

            case 'play':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("PLAY completed with result %d (%s)",
                     result, strerror(-result));

                if (result == OK) {
                    sp<RefBase> obj;
                    CHECK(msg->findObject("response", &obj));
                    sp<ARTSPResponse> response =
                        static_cast<ARTSPResponse *>(obj.get());

                    if (response->mStatusCode != 200) {
#ifndef ANDROID_DEFAULT_CODE 
                        // start per stream checker even if failed ..
                        mRTPConn->startCheckAlives();
                        result = response->mStatusCode;

                        if(result == 500){ 
                            int32_t isPaused;  //haizhen--if is internal serve error we set result ok
                            if(msg->findInt32("paus", &isPaused) && isPaused) {
                                LOGI("[rtsp]'play' response->mStatusCode ==500");	
                                mNPTPending = false; //if play reponse is fail,then still using the last mapping
                                mPendingByPause = false;
                                mIntSeverError = true; //Record have happen Internal server error, then should not response to pause
                                result = OK;
                            }
                        }

#else
                        result = UNKNOWN_ERROR;
#endif // #ifndef ANDROID_DEFAULT_CODE
                    } else {
                        parsePlayResponse(response);

#ifndef ANDROID_DEFAULT_CODE 
                        // start per stream checker
                        mRTPConn->startCheckAlives();
						//post a new 'tiou' after play
						postTryTCPTimeOutCheck();
						/*
						if (kSRTimeoutUs != 0) {

							sp<AMessage> timeout = new AMessage('tiou', id());
							mTiouPending = false; //add by haizhen
							mTiouGeneration = (int32_t)timeout.get();//add by haizhen
							timeout->setInt32("generation",mTiouGeneration);//add by haizhen
							timeout->post(kSRTimeoutUs);
							
			                        }*/
#else
                        sp<AMessage> timeout = new AMessage('tiou', id());
                        timeout->post(kStartupTimeoutUs);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    }
                }

                if (result != OK) {
#ifndef ANDROID_DEFAULT_CODE 
                    mFinalStatus = MappingRTSPError(result);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    sp<AMessage> reply = new AMessage('disc', id());
                    mConn->disconnect(reply);
                }
                else {
                    if (msg->findInt32("play", &result) && result) {
                        mDoneMsg->setInt32("result", OK);
                        mDoneMsg->post();
                        mDoneMsg = NULL;
                    }
                }

                break;
            }

            case 'aliv':
            {
                int32_t generation;
                CHECK(msg->findInt32("generation", &generation));

                if (generation != mKeepAliveGeneration) {
                    // obsolete event.
                    break;
                }

                AString request;
                request.append("OPTIONS ");
#ifndef ANDROID_DEFAULT_CODE
                request.append(mBaseURL);
#else
                request.append(mSessionURL);
#endif
                request.append(" RTSP/1.0\r\n");
                request.append("Session: ");
                request.append(mSessionID);
                request.append("\r\n");
                request.append("\r\n");

                sp<AMessage> reply = new AMessage('opts', id());
                reply->setInt32("generation", mKeepAliveGeneration);
#ifndef ANDROID_DEFAULT_CODE
                reply->setInt32("keep-tcp", 1);
                LOGI("sending keep alive");
#endif
                mConn->sendRequest(request.c_str(), reply);
                break;
            }

            case 'opts':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("OPTIONS completed with result %d (%s)",
                     result, strerror(-result));

                int32_t generation;
                CHECK(msg->findInt32("generation", &generation));

                if (generation != mKeepAliveGeneration) {
                    // obsolete event.
                    break;
                }

                postKeepAlive();
                break;
            }

            case 'abor':
            {
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                bool keepTracks = false;
                int32_t tmp;
                keepTracks = msg->findInt32("keep-tracks", &tmp);
#endif
                for (size_t i = 0; i < mTracks.size(); ++i) {
                    TrackInfo *info = &mTracks.editItemAt(i);

#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                    if (!keepTracks)
#endif
                    info->mPacketSource->signalEOS(ERROR_END_OF_STREAM);

                    if (!info->mUsingInterleavedTCP) {
                        mRTPConn->removeStream(info->mRTPSocket, info->mRTCPSocket);

                        // Clear the tag
                        if (mUIDValid) {
                            HTTPBase::UnRegisterSocketUserTag(info->mRTPSocket);
                            HTTPBase::UnRegisterSocketUserTag(info->mRTCPSocket);
                        }

                        close(info->mRTPSocket);
                        close(info->mRTCPSocket);
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                        // reuse this flag to indicate that stream is removed
                        info->mUsingInterleavedTCP = true;
#endif
                    }
                }
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                if (!keepTracks)
#endif
                mTracks.clear();
                mSetupTracksSuccessful = false;
                mSeekPending = false;
				
#ifndef ANDROID_DEFAULT_CODE //haizhen
				mPausePending = false;
#endif 
				
                mFirstAccessUnit = true;
                mNTPAnchorUs = -1;
                mMediaAnchorUs = -1;
                mNumAccessUnitsReceived = 0;
#ifdef ANDROID_DEFAULT_CODE 
                // DO NOT reset mReceivedFirstRTCPPacket, we do not want to 
                // try TCP if we have received RTCP
                mReceivedFirstRTCPPacket = false;
#endif // #ifndef ANDROID_DEFAULT_CODE
                mReceivedFirstRTPPacket = false;
                mSeekable = false;
#ifndef ANDROID_DEFAULT_CODE 
                mFirstAccessUnitNTP = 0;
                mHaveVideo = false;
                mHaveAudio = false;
#endif // #ifndef ANDROID_DEFAULT_CODE

                sp<AMessage> reply = new AMessage('tear', id());

                int32_t reconnect;
                if (msg->findInt32("reconnect", &reconnect) && reconnect) {
                    reply->setInt32("reconnect", true);
                }
#ifndef ANDROID_DEFAULT_CODE 
                else {
                    reply->setInt64("timeout", kTearDownTimeoutUs);
                }
#endif // #ifndef ANDROID_DEFAULT_CODE

                AString request;
                request = "TEARDOWN ";

                // XXX should use aggregate url from SDP here...
#ifndef ANDROID_DEFAULT_CODE
                request.append(mBaseURL);
#else
                request.append(mSessionURL);
#endif
                request.append(" RTSP/1.0\r\n");

                request.append("Session: ");
                request.append(mSessionID);
                request.append("\r\n");

                request.append("\r\n");

#ifndef ANDROID_DEFAULT_CODE
                LOGD("sending TEARDOWN");
#endif // #ifndef ANDROID_DEFAULT_CODE
                mConn->sendRequest(request.c_str(), reply);
                break;
            }

            case 'tear':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("TEARDOWN completed with result %d (%s)",
                     result, strerror(-result));

                sp<AMessage> reply = new AMessage('disc', id());

                int32_t reconnect;
                if (msg->findInt32("reconnect", &reconnect) && reconnect) {
                    reply->setInt32("reconnect", true);
                }

                mConn->disconnect(reply);
                break;
            }

            case 'quit':
            {
                if (mDoneMsg != NULL) {
                    mDoneMsg->setInt32("result", UNKNOWN_ERROR);
#ifndef ANDROID_DEFAULT_CODE 
                    if (mFinalStatus != OK)
                        mDoneMsg->setInt32("result", mFinalStatus);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    mDoneMsg->post();
                    mDoneMsg = NULL;
                }
                break;
            }

            case 'chek':
            {
                int32_t generation;
                CHECK(msg->findInt32("generation", &generation));
                if (generation != mCheckGeneration) {
                    // This is an outdated message. Ignore.
                    break;
                }

                if (mNumAccessUnitsReceived == 0) {
#if 1
                    LOGI("stream ended? aborting.");
                    (new AMessage('abor', id()))->post();
                    break;
#else
                    LOGI("haven't seen an AU in a looong time.");
#endif
                }

                mNumAccessUnitsReceived = 0;
                msg->post(kAccessUnitTimeoutUs);
                break;
            }

            case 'accu':
            {
                int32_t timeUpdate;
                if (msg->findInt32("time-update", &timeUpdate) && timeUpdate) {
                    size_t trackIndex;
                    CHECK(msg->findSize("track-index", &trackIndex));

                    uint32_t rtpTime;
                    uint64_t ntpTime;
                    CHECK(msg->findInt32("rtp-time", (int32_t *)&rtpTime));
                    CHECK(msg->findInt64("ntp-time", (int64_t *)&ntpTime));

                    onTimeUpdate(trackIndex, rtpTime, ntpTime);
                    break;
                }
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                int32_t rr;
                if (msg->findInt32("receiver-report", &rr)) {
                    sp<RefBase> obj;
                    CHECK(msg->findObject("buffer", &obj));
                    sp<ABuffer> buffer = static_cast<ABuffer *>(obj.get());
                    mConn->injectPacket(buffer);
                    break;
                }
#endif

                int32_t first;
                if (msg->findInt32("first-rtcp", &first)) {
#ifndef ANDROID_DEFAULT_CODE 
                    LOGI("receive first-rtcp");
#endif // #ifndef ANDROID_DEFAULT_CODE
                    mReceivedFirstRTCPPacket = true;
                    break;
                }

                if (msg->findInt32("first-rtp", &first)) {
#ifndef ANDROID_DEFAULT_CODE 
                    LOGI("receive first-rtp");
#endif // #ifndef ANDROID_DEFAULT_CODE
                    mReceivedFirstRTPPacket = true;
                    break;
                }

                ++mNumAccessUnitsReceived;
#ifdef ANDROID_DEFAULT_CODE 
                // use per track check instead of all
                postAccessUnitTimeoutCheck();
#endif // #ifndef ANDROID_DEFAULT_CODE

                size_t trackIndex;
                CHECK(msg->findSize("track-index", &trackIndex));

                if (trackIndex >= mTracks.size()) {
                    LOGV("late packets ignored.");
                    break;
                }

                TrackInfo *track = &mTracks.editItemAt(trackIndex);

                int32_t eos;
                if (msg->findInt32("eos", &eos)) {
                    LOGI("received BYE on track index %d", trackIndex);
#if 0
                    track->mPacketSource->signalEOS(ERROR_END_OF_STREAM);
#endif
                    return;
                }

#ifndef ANDROID_DEFAULT_CODE 
                if (msg->findInt32("stream-timeout", &eos)) {
                    LOGI("MyHandler: dead track #%d", trackIndex);
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                    if (!mReceivedFirstRTPPacket && (!mTryTCPInterleaving || mSwitchingTCP)) {
                        LOGI("don't kill track #%d which is dead before trying TCP", trackIndex);
                        return;
                    }
#endif

                    /*********************************************************************************************
                     *For some case, the track start time in sdp will larger than the npt1 from play response.
                     * We should not eos before the start time in sdp
                     *Here we use track->mLastNPT as a member to accumulate time, 
                     *then we can know whether the real play time larger than the start time or not.
                     *track->mLastNPT will be set to npt1 when seeking what is we want and is available only undering mNPTMode=ture
                     *So if not NPTMode, we eos after track->mTimeoutTrials++ >= trials, will not consider the start play time.
                     ********************************************************************************************/
                    LOGI("accu,track->mLastNPT=%lld,mNPTMode=%d",track->mLastNPT,mNPTMode);
                    if((track->mPlayStartTime * 1E6 > track->mLastNPT) && mNPTMode){
                        track->mLastNPT += ARTPSource::kAccessUnitTimeoutUs;
                        LOGI("accu,track->mLastNPT=%lld",track->mLastNPT);
                        if(track->mPlayStartTime* 1E6 > track->mLastNPT){
                            LOGI("accu,track->mPlayStartTime=%f,track->mLastNPT=%lld",track->mPlayStartTime,track->mLastNPT);
                            return;
                        }
                    }	

                    int64_t tmp = 0;
                    mSessionDesc->getDurationUs(&tmp);

                    int trials = mIsLive ? kTimeoutLiveTrials : kTimeoutTrials;
                    bool eos = track->mTimeoutTrials++ >= trials;
                    int64_t trackNPT = track->mTimeoutTrials * ARTPSource::kAccessUnitTimeoutUs
                        + track->mLastNPT;

                    LOGI("mLastNPT %lld %lld, duration %lld, mode %d, live %d", 
                            mLastNPT, track->mLastNPT, tmp, mNPTMode, mIsLive);

                    eos = eos || (mNPTMode && ((!mIsLive && trackNPT >= tmp)
                                || (mLastNPT - track->mLastNPT >= 2*ARTPSource::kAccessUnitTimeoutUs)));

                    if (eos) {
                        LOGI("signalEOS for %d", trackIndex);
                        track->mPacketSource->signalEOS(ERROR_END_OF_STREAM);
                    } else {
                        LOGI("we will wait next timeout (%d < %d)", 
                                track->mTimeoutTrials - 1, trials);
                    }
                    return;
                } else {
                    track->mTimeoutTrials = 0;
                }
#endif // #ifndef ANDROID_DEFAULT_CODE

                sp<RefBase> obj;
                CHECK(msg->findObject("access-unit", &obj));

                sp<ABuffer> accessUnit = static_cast<ABuffer *>(obj.get());

                uint32_t seqNum = (uint32_t)accessUnit->int32Data();

                if (mSeekPending) {
                    LOGV("we're seeking, dropping stale packet.");
                    break;
                }

                if (seqNum < track->mFirstSeqNumInSegment) {
                    LOGV("dropping stale access-unit (%d < %d)",
                         seqNum, track->mFirstSeqNumInSegment);
                    break;
                }

#ifndef ANDROID_DEFAULT_CODE
                uint64_t ntpTime;
                CHECK(accessUnit->meta()->findInt64(
                            "ntp-time", (int64_t *)&ntpTime));

                uint32_t rtpTime;
                CHECK(accessUnit->meta()->findInt32(
                            "rtp-time", (int32_t *)&rtpTime));
#endif // #ifndef ANDROID_DEFAULT_CODE
                if (track->mNewSegment) {
                    track->mNewSegment = false;
#ifndef ANDROID_DEFAULT_CODE
                    LOGV("first segment unit ntpTime=0x%016llx rtpTime=%u seq=%d",
                         ntpTime, rtpTime, seqNum);
#endif // #ifndef ANDROID_DEFAULT_CODE
                }

#ifndef ANDROID_DEFAULT_CODE
                if (mFirstAccessUnit) {
                    mFirstAccessUnit = false;
                    mFirstAccessUnitNTP = ntpTime;
                    LOGI("[rtsp]accu is FirstAccessUnit, mFirstAccessUnitNTP=%lld",mFirstAccessUnitNTP);
                }
#else
                onAccessUnitComplete(trackIndex, accessUnit);
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
                if (track->mFirstAccessUnit) {
                    int64_t diff = 0;
                    track->mFirstAccessUnit = false;
                    track->mFirstAccessUnitNTP = ntpTime;
                    if (!mNPTMode) {
                        diff = mFirstAccessUnitNTP - track->mFirstAccessUnitNTP;
                        diff = diff < 0 ? -diff : diff;
                        if (diff > kMaxSRNTPDiff) {
                            LOGW("NTP big difference %lld vs %lld, enable track NTP mode",
                                    mFirstAccessUnitNTP, track->mFirstAccessUnitNTP);
                            track->mUseTrackNTP = true;
                        }
                    }
                    LOGI("first segment unit ntpTime=0x%016llx rtpTime=%u seq=%d diff=%llx",
                            ntpTime, rtpTime, seqNum, diff);
                }

                uint64_t firstNTP = track->mUseTrackNTP ? track->mFirstAccessUnitNTP : mFirstAccessUnitNTP;
                if (ntpTime >= firstNTP) {
                    ntpTime -= firstNTP;
                } else {
                    LOGI("[rtsp]accu rtpTime=%d ntpTime=%lld < mFirstAccessUnitNTP=%lld", rtpTime, ntpTime, firstNTP);
                    ntpTime = 0;
                }
#endif

#ifndef ANDROID_DEFAULT_CODE 
                int64_t timeUs = (int64_t)(ntpTime * 1E6 / (1ll << 32));
                // in case that no rtptime in SETUP response
                if (!track->mPacketSource->isNPTMappingSet()) {
                    track->mPacketSource->setNormalPlayTimeMapping(rtpTime, 
                            timeUs);
                    LOGI("[rtsp]accu Re-setNormalPlayTimeMapping,rtpTime=%d,timeUs=%lld",rtpTime,timeUs); //haizhen
                }

                // setup NPT <=> timeUs mapping
                if (mNPTPending) {
                    mNTPBase = timeUs;
                    mNPTBase = track->mPacketSource->getNormalPlayTimeUs(rtpTime);
                    mNPTPending = false;
                    mPendingByPause = false;
                    LOGI("[rtsp]'accu' mNPTBase = %lld, mNTPBase = %lld, timeUs = %lld, seqNum = %d", mNPTBase, mNTPBase, timeUs, seqNum); //haizhen
                }
                track->mLastNPT = timeUs;
                mLastNPT = timeUs;

                accessUnit->meta()->setInt64("timeUs", timeUs);

#if 0
                int32_t damaged;
                if (accessUnit->meta()->findInt32("damaged", &damaged)
                        && damaged != 0) {
                    LOGI("ignoring damaged AU");
                } else
#endif
                {
                    TrackInfo *track = &mTracks.editItemAt(trackIndex);
                    track->mPacketSource->queueAccessUnit(accessUnit);
                }
#endif // #ifndef ANDROID_DEFAULT_CODE
                break;
            }

#ifndef ANDROID_DEFAULT_CODE // haizhen					
            case 'paus':  //Pre-pause, before send pause to server, we should do some preparing
            {
                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));

                if (mPausePending) {
                    doneMsg->setInt32("result", ALREADY_EXISTS);
                    doneMsg->post();
                    break;
                }	
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                //pause during swithing transport is hard to handle
                if (mSwitchingTCP) {
                    doneMsg->setInt32("result", INVALID_OPERATION);
                    doneMsg->post();
                    break;
                }
#endif
                if (mIsLive) {
                    LOGW("This is a live stream, ignoring pause request.");

                    doneMsg->setInt32("result", INVALID_OPERATION);

                    doneMsg->post();
                    break;
                }	
                mPausePending = true;
                // Disable the access unit timeout until we resumed
                // playback again.
                mCheckPending = true;
                ++mCheckGeneration;

                //mNPTPending = true; //move to play
                // mPendingByPause = true;//move to play
                // mReceivedFirstRTCPPacket = false;			
                mRTPConn->stopCheckAlives(); 

                //disable 'tiou' until we resumed
                mTiouPending = true;

                sp<AMessage> pausMsg = new AMessage('pau0', id());
                pausMsg->setMessage("doneMsg", doneMsg);
                pausMsg->post();

                break;
            }

            case 'pau0':
            {
                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));

                AString request = "PAUSE ";
#ifndef ANDROID_DEFAULT_CODE
                request.append(mBaseURL);
#else
                request.append(mSessionURL);
#endif
                request.append(" RTSP/1.0\r\n");

                request.append("Session: ");
                request.append(mSessionID);
                request.append("\r\n");

                request.append("\r\n");

                sp<AMessage> reply = new AMessage('pau1', id());

                LOGI("[rtsp]Myhandler Start Send Pause!!!");

                reply->setMessage("doneMsg", doneMsg);
                mConn->sendRequest(request.c_str(), reply);
                break;
            }
            case 'pau1':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));
                LOGI("[rtsp]PAUSE completed with result %d (%s)",
                        result, strerror(-result));
                if (result != OK) {
                    mFinalStatus = MappingRTSPError(result);
                    LOGE("pause failed, aborting.");
                    (new AMessage('abor', id()))->post();

                    mPausePending = false;

                    sp<AMessage> doneMsg;
                    CHECK(msg->findMessage("doneMsg", &doneMsg));
                    doneMsg->setInt32("result", result);
                    doneMsg->post();
                    break;
                }

                mRTPConn->resetTimestamps(); //set nNumTime = 0, need re-mapping rtp to ntp
                mPausePending = false;

                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));
                doneMsg->setInt32("result", OK);
                doneMsg->post(); //pause done

                break;
            }


#endif //ANDROID_DEFAULT_CODE

            case 'seek':
            {
                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));

                if (mSeekPending) {
#ifndef ANDROID_DEFAULT_CODE 
                    doneMsg->setInt32("result", ALREADY_EXISTS);
                    LOGE("doing last seek,return ALREADY_EXISTS directly");
#endif // #ifndef ANDROID_DEFAULT_CODE
                    doneMsg->post();
                    break;
                }

#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                // seek during swithing transport is hard to handle
                if (mSwitchingTCP) {
                    doneMsg->setInt32("result", INVALID_OPERATION);
                    doneMsg->post();
                    break;
                }
#endif

#ifndef ANDROID_DEFAULT_CODE 
                if (mIsLive) {
#else
                if (!mSeekable) {
#endif // #ifndef ANDROID_DEFAULT_CODE
                    LOGW("This is a live stream, ignoring seek request.");
#ifndef ANDROID_DEFAULT_CODE 
                    doneMsg->setInt32("result", INVALID_OPERATION);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    doneMsg->post();
                    break;
                }

                int64_t timeUs;
                CHECK(msg->findInt64("time", &timeUs));

                mSeekPending = true;

                // Disable the access unit timeout until we resumed
                // playback again.
                mCheckPending = true;
                ++mCheckGeneration;

#ifndef ANDROID_DEFAULT_CODE //add by haizhen, disable 'tiou' until we resumed
                mTiouPending = true;
#endif

#ifndef ANDROID_DEFAULT_CODE 
                mLastSeekTimeTime = timeUs;
                // flush here instead of when SEEKing completed
                for (size_t i = 0; i < mTracks.size(); ++i) {
                    sp<APacketSource> s = mTracks.editItemAt(i).mPacketSource;
                    if (s->isAtEOS()) {
                        // reactivate NAT in case we're done for a long time
                        TrackInfo *track = &mTracks.editItemAt(i);
                        if (!track->mUsingInterleavedTCP) {
                            pokeAHole(track->mRTPSocket,
                                    track->mRTCPSocket,
                                    track->mTransport,
                                    track->mRTPPort);
                        }
                    }
                    s->flushQueue();
                    s->setNormalPlayTimeUs(timeUs);
                }
                mInitNPT = timeUs;
                mNPTPending = true;
                mPendingByPause = false;
                mReceivedFirstRTCPPacket = false;
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
                mRTPConn->stopCheckAlives();
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
                doneMsg->setInt32("result", OK);
                doneMsg->post();
                break;
            }

            case 'see0':
            {
                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));
                int64_t timeUs;
                CHECK(msg->findInt64("time", &timeUs));

                int64_t tmp;
                bool live = !mSessionDesc->getDurationUs(&tmp);
                if (!live && timeUs >= tmp) {
                    LOGI("seek to the end, eos right now");
                    if (doneMsg != NULL) {
                        doneMsg->setInt32("result", OK);
                        doneMsg->post(); 
                    }

                    int i;
                    for(i = 0; i < (int)mTracks.size(); ++i) {
                        TrackInfo *track = &mTracks.editItemAt(i);
                        track->mPacketSource->signalEOS(ERROR_END_OF_STREAM);
                    }
                    break;
                }
#endif // #ifndef ANDROID_DEFAULT_CODE
#ifndef ANDROID_DEFAULT_CODE
                if (!mPlaySent) {
                    if (mServerInfo.find("MDN_HWPSS") != -1) {
                        LOGI("direct PLAY for some server");
                        sp<AMessage> reply = new AMessage('see1', id());
                        reply->setInt64("time", timeUs);
                        reply->setMessage("doneMsg", doneMsg);
                        reply->setInt32("result", OK);
                        reply->post();
                        break;
                    } else {
                        mPlaySent = true;
                        LOGI("send PLAY for first seek");
                        AString request = "PLAY ";
                        request.append(mBaseURL);
                        request.append(" RTSP/1.0\r\n");

                        request.append("Session: ");
                        request.append(mSessionID);
                        request.append("\r\n");

                        request.append("Range: npt=0-\r\n");

                        request.append("\r\n");

                        sp<AMessage> reply = new AMessage('nopl', id());
                        reply->setInt64("time", timeUs);
                        reply->setMessage("doneMsg", doneMsg);
                        mConn->sendRequest(request.c_str(), reply);
                    }
                }
#endif
                AString request = "PAUSE ";
#ifndef ANDROID_DEFAULT_CODE
                request.append(mBaseURL);
#else
                request.append(mSessionURL);
#endif
                request.append(" RTSP/1.0\r\n");

                request.append("Session: ");
                request.append(mSessionID);
                request.append("\r\n");

                request.append("\r\n");

                sp<AMessage> reply = new AMessage('see1', id());
                reply->setInt64("time", timeUs);
                reply->setMessage("doneMsg", doneMsg);
                mConn->sendRequest(request.c_str(), reply);
                break;
            }

#ifndef ANDROID_DEFAULT_CODE
            case 'nopl':
            {
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("pipeline PLAY completed with result %d (%s)",
                     result, strerror(-result));
                break;
            }
#endif
            case 'see1':
            {
#ifndef ANDROID_DEFAULT_CODE 
                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("PAUSE completed with result %d (%s)",
                     result, strerror(-result));

                if (result != OK) {
                    mFinalStatus = MappingRTSPError(result);
                    LOGE("pause failed, aborting.");
                    (new AMessage('abor', id()))->post();

                    mSeekPending = false;

                    sp<AMessage> doneMsg;
                    CHECK(msg->findMessage("doneMsg", &doneMsg));

                    doneMsg->post();
                    break;
                }
#endif // #ifndef ANDROID_DEFAULT_CODE
                // Session is paused now.
#ifdef ANDROID_DEFAULT_CODE 
                // no need flush here any more
                for (size_t i = 0; i < mTracks.size(); ++i) {
                    TrackInfo *info = &mTracks.editItemAt(i);

                    info->mPacketSource->flushQueue();
                    info->mRTPAnchor = 0;
                    info->mNTPAnchorUs = -1;
                }
#endif // #ifndef ANDROID_DEFAULT_CODE

                mNTPAnchorUs = -1;

#ifndef ANDROID_DEFAULT_CODE 
                mRTPConn->resetTimestamps();
#endif // #ifndef ANDROID_DEFAULT_CODE
                int64_t timeUs;
                CHECK(msg->findInt64("time", &timeUs));

                AString request = "PLAY ";
#ifndef ANDROID_DEFAULT_CODE
                mPlaySent = true;
                request.append(mBaseURL);
#else
                request.append(mSessionURL);
#endif
                request.append(" RTSP/1.0\r\n");

                request.append("Session: ");
                request.append(mSessionID);
                request.append("\r\n");

                request.append(
                        StringPrintf(
                            "Range: npt=%lld-\r\n", timeUs / 1000000ll));

                request.append("\r\n");

                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));

                sp<AMessage> reply = new AMessage('see2', id());
                reply->setMessage("doneMsg", doneMsg);
                mConn->sendRequest(request.c_str(), reply);
                break;
            }

            case 'see2':
            {
#ifndef ANDROID_DEFAULT_CODE 
                // don't fail if we are aborted
                if (!mSeekPending)
                    break;
#else
                CHECK(mSeekPending);
#endif // #ifndef ANDROID_DEFAULT_CODE

                int32_t result;
                CHECK(msg->findInt32("result", &result));

                LOGI("PLAY completed with result %d (%s)",
                     result, strerror(-result));

                mCheckPending = false;
#ifdef ANDROID_DEFAULT_CODE 
                // use per track check instead of all
                postAccessUnitTimeoutCheck();
#endif // #ifndef ANDROID_DEFAULT_CODE

                if (result == OK) {
                    sp<RefBase> obj;
                    CHECK(msg->findObject("response", &obj));
                    sp<ARTSPResponse> response =
                        static_cast<ARTSPResponse *>(obj.get());

                    if (response->mStatusCode != 200) {
#ifndef ANDROID_DEFAULT_CODE 
                        result = response->mStatusCode;
#else
                        result = UNKNOWN_ERROR;
#endif // #ifndef ANDROID_DEFAULT_CODE
                    } else {
                        parsePlayResponse(response);

                        ssize_t i = response->mHeaders.indexOfKey("rtp-info");
#ifdef ANDROID_DEFAULT_CODE
                        CHECK_GE(i, 0);
#endif // #ifndef ANDROID_DEFAULT_CODE

                        LOGV("rtp-info: %s", response->mHeaders.valueAt(i).c_str());

                        LOGI("seek completed.");
#ifndef ANDROID_DEFAULT_CODE 
                        mRTPConn->startCheckAlives();
						postTryTCPTimeOutCheck();
						/*
						 if (kSRTimeoutUs != 0) {
							
							sp<AMessage> timeout = new AMessage('tiou', id());
							mTiouPending = false; //add by haizhen
							mTiouGeneration = (int32_t)timeout.get();//add by haizhen
							timeout->setInt32("generation",mTiouGeneration);//add by haizhen
							timeout->post(kSRTimeoutUs);
                        			}*/
#endif // #ifndef ANDROID_DEFAULT_CODE
                    }
                }

                if (result != OK) {
#ifndef ANDROID_DEFAULT_CODE 
                    mFinalStatus = MappingRTSPError(result);
#endif // #ifndef ANDROID_DEFAULT_CODE
                    LOGE("seek failed, aborting.");
                    (new AMessage('abor', id()))->post();
                }

                mSeekPending = false;

                sp<AMessage> doneMsg;
                CHECK(msg->findMessage("doneMsg", &doneMsg));

                doneMsg->post();
                break;
            }

            case 'biny':
            {
                sp<RefBase> obj;
                CHECK(msg->findObject("buffer", &obj));
                sp<ABuffer> buffer = static_cast<ABuffer *>(obj.get());

                int32_t index;
                CHECK(buffer->meta()->findInt32("index", &index));

                mRTPConn->injectPacket(index, buffer);
                break;
            }

            case 'tiou':
            {
#ifndef ANDROID_DEFAULT_CODE 
				//add by haizhen start , for maybe 'tiou' is canceled or a new 'tiou' is post
				 int32_t generation = 0;
				msg->findInt32("generation",&generation);
				if(mTiouPending || mTiouGeneration!= generation){
					LOGI("'tiou' is cancelled or this is a old 'tiou'");
					break;
				}
				//add by haizhen stop
				
                if (mReceivedFirstRTPPacket) {
                    LOGI("SR timeout rtcp = %d", mReceivedFirstRTCPPacket);
                    if (mRTPConn != NULL)
                        mRTPConn->useFirstTimestamp();
                    mReceivedFirstRTCPPacket = true;
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
                } else if (!mTryTCPInterleaving) {
                    LOGW("Never received any data, switching transports.");

                    mTryTCPInterleaving = true;
                    mSwitchingTCP = true;
                    mTryingTCPIndex = 0;
                    mRTPConn->stopCheckAlives();

                    sp<AMessage> msg = new AMessage('abor', id());
                    msg->setInt32("reconnect", true);
                    msg->setInt32("keep-tracks", true);
                    msg->post();
                } else {
                	//we need to post 'tiou' circlely until Receive the first RTP Packet,
                	//then check whether need useFirstTimestamp after turn to TCP mode
                	postTryTCPTimeOutCheck();
					/*
			                    LOGW("Never received any data, disconnecting.");
			                    (new AMessage('abor', id()))->post();
		                    */
#endif
                }
                break;
#endif // #ifndef ANDROID_DEFAULT_CODE
                if (!mReceivedFirstRTCPPacket) {
                    if (mReceivedFirstRTPPacket && !mTryFakeRTCP) {
                        LOGW("We received RTP packets but no RTCP packets, "
                             "using fake timestamps.");

                        mTryFakeRTCP = true;

                        mReceivedFirstRTCPPacket = true;

                        fakeTimestamps();
                    } else if (!mReceivedFirstRTPPacket && !mTryTCPInterleaving) {
                        LOGW("Never received any data, switching transports.");

                        mTryTCPInterleaving = true;

                        sp<AMessage> msg = new AMessage('abor', id());
                        msg->setInt32("reconnect", true);
                        msg->post();
                    } else {
                        LOGW("Never received any data, disconnecting.");
                        (new AMessage('abor', id()))->post();
                    }
                }
                break;
            }

            default:
                TRESPASS();
                break;
        }
    }

    void postKeepAlive() {
        sp<AMessage> msg = new AMessage('aliv', id());
        msg->setInt32("generation", mKeepAliveGeneration);
        msg->post((mKeepAliveTimeoutUs * 9) / 10);
    }

    void postAccessUnitTimeoutCheck() {
        if (mCheckPending) {
            return;
        }

        mCheckPending = true;
        sp<AMessage> check = new AMessage('chek', id());
        check->setInt32("generation", mCheckGeneration);
        check->post(kAccessUnitTimeoutUs);
    }

    static void SplitString(
            const AString &s, const char *separator, List<AString> *items) {
        items->clear();
        size_t start = 0;
        while (start < s.size()) {
            ssize_t offset = s.find(separator, start);

            if (offset < 0) {
                items->push_back(AString(s, start, s.size() - start));
                break;
            }

            items->push_back(AString(s, start, offset - start));
            start = offset + strlen(separator);
        }
    }

    void parsePlayResponse(const sp<ARTSPResponse> &response) {
#ifndef ANDROID_DEFAULT_CODE 
        Vector<uint32_t> rtpTimes;
        Vector<uint32_t> rtpSeqs;
        Vector<int32_t> rtpSockets;
        bool isLive = false;

        if (mTracks.size() == 0) {
            LOGW("parsePlayResponse after abor");
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mSeekable = false;

        ssize_t i = response->mHeaders.indexOfKey("range");
        if (i < 0) {
            // Server doesn't even tell use what range it is going to
            // play, therefore we won't support seeking.
#ifndef ANDROID_DEFAULT_CODE 
            // enable SR timestamp
            LOGI("no range, using SR ntp");
            mRTPConn->enableSRTimestamp();
#endif // #ifndef ANDROID_DEFAULT_CODE
            return;
        }

        AString range = response->mHeaders.valueAt(i);
        LOGV("Range: %s", range.c_str());

        AString val;
        CHECK(GetAttribute(range.c_str(), "npt", &val));

        float npt1, npt2;
        if (!ASessionDescription::parseNTPRange(val.c_str(), &npt1, &npt2)) {
            // This is a live stream and therefore not seekable.

            LOGI("This is a live stream");
#ifndef ANDROID_DEFAULT_CODE 
            // we still need to process rtp-info
            isLive = true;
#else
            return;
#endif // #ifndef ANDROID_DEFAULT_CODE
        }

        i = response->mHeaders.indexOfKey("rtp-info");
        CHECK_GE(i, 0);

        AString rtpInfo = response->mHeaders.valueAt(i);
        List<AString> streamInfos;
        SplitString(rtpInfo, ",", &streamInfos);

        int n = 1;
        for (List<AString>::iterator it = streamInfos.begin();
             it != streamInfos.end(); ++it) {
            (*it).trim();
            LOGV("streamInfo[%d] = %s", n, (*it).c_str());

            CHECK(GetAttribute((*it).c_str(), "url", &val));

            size_t trackIndex = 0;
#ifndef ANDROID_DEFAULT_CODE 
            AString str = val;
            CHECK(MakeURL(mBaseURL.c_str(), str.c_str(), &val));
#endif // #ifndef ANDROID_DEFAULT_CODE
            while (trackIndex < mTracks.size()
                    && !(val == mTracks.editItemAt(trackIndex).mURL)) {
                ++trackIndex;
            }
#ifndef ANDROID_DEFAULT_CODE 
            if (trackIndex >= mTracks.size()) {
                LOGW("ignore unknown url in PLAY response %s", val.c_str());
                ++n;
                continue;
            }
#else
            CHECK_LT(trackIndex, mTracks.size());
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE 
            // continue instead of failure
            if (!GetAttribute((*it).c_str(), "seq", &val)) {
                ++n;
                continue;
            }
#else
            CHECK(GetAttribute((*it).c_str(), "seq", &val));
#endif // #ifndef ANDROID_DEFAULT_CODE

            char *end;
            unsigned long seq = strtoul(val.c_str(), &end, 10);

            TrackInfo *info = &mTracks.editItemAt(trackIndex);
            info->mFirstSeqNumInSegment = seq;
            info->mNewSegment = true;

#ifndef ANDROID_DEFAULT_CODE 
            // continue instead of failure
            if (!GetAttribute((*it).c_str(), "rtptime", &val)) {
                ++n;
                continue;
            }
#else
            CHECK(GetAttribute((*it).c_str(), "rtptime", &val));
#endif // #ifndef ANDROID_DEFAULT_CODE

            uint32_t rtpTime = strtoul(val.c_str(), &end, 10);

#ifndef ANDROID_DEFAULT_CODE //haizhen
			LOGI("track #%d: rtpTime=%u <=> npt=%.2f", n, rtpTime, npt1);
#else
            LOGV("track #%d: rtpTime=%u <=> npt=%.2f", n, rtpTime, npt1);
#endif

            info->mNormalPlayTimeRTP = rtpTime;
            info->mNormalPlayTimeUs = (int64_t)(npt1 * 1E6);
            
#ifndef ANDROID_DEFAULT_CODE //haizhen
			LOGI("parsePlayResponse,info->mPlayStartTime=%f, npt1=%f",info->mPlayStartTime,npt1);
			if((info->mPlayStartTime) - npt1 > (float)kMaxInterleave)	
				info->mPlayStartTime = npt1;
			
			float real_npt = ((info->mPlayStartTime) > npt1 ) ?  info->mPlayStartTime : npt1;
			LOGI("parsePlayResponse, real_npt=%f",real_npt);

			info->mPacketSource->setNormalPlayTimeMapping(
			        rtpTime, (int64_t)(real_npt * 1E6));
			/*
			float real_npt = (((info->mPlayStartTime) > npt1) 
								&&(((info->mPlayStartTime) - npt1) < (float)kMaxInterleave)) ?  info->mPlayStartTime : npt1;
				info->mPacketSource->setNormalPlayTimeMapping(
			        rtpTime, (int64_t)(real_npt * 1E6));
			*/
#else
            info->mPacketSource->setNormalPlayTimeMapping(
                    rtpTime, (int64_t)(npt1 * 1E6));
#endif


#ifndef ANDROID_DEFAULT_CODE 
            info->mLastNPT = (int64_t)(npt1 * 1E6);
            info->mTimeoutTrials = 0;
            rtpTimes.push(rtpTime);
            rtpSockets.push(info->mRTPSocket);
            rtpSeqs.push(info->mFirstSeqNumInSegment);
#endif // #ifndef ANDROID_DEFAULT_CODE
            ++n;
        }

#ifndef ANDROID_DEFAULT_CODE 
        if (mTracks.size() != rtpTimes.size()) {
            // enable SR timestamp
            LOGI("some track has no rtp-info, using SR ntp");
            mRTPConn->enableSRTimestamp();
        } else {
            mNPTMode = true;
            mLastNPT = (int64_t)(npt1 * 1E6);
            LOGI("all tracks have rtp-info, using NPT %f as ntp ", npt1);
            for(int i=0; i<(int)rtpTimes.size(); ++i) {
				if((int64_t)(npt1 * 1E6) >= mNTPBase || mSeekPending){ //haizhen--Only if Playe response npt is become bigger or seeking,we allow to timeupdate
					TrackInfo *track = &mTracks.editItemAt(i);
					float real_start = ((track->mPlayStartTime) > npt1) ?  track->mPlayStartTime : npt1; 
					LOGI("timeUpdata real_start=%f,rtpTime[%d]=%d",real_start,i,rtpTimes[i]);
					//haizhen---for some case track will not start from npt1
                	mRTPConn->timeUpdate(rtpSockets[i], rtpTimes[i], 
						(uint64_t)(real_start* (1LL<<32)), rtpSeqs[i]);
				}else{ //show that server is re-playing from begin, which meas server complete sending packets,
					LOGI("[rtsp](int64_t)(npt1 * 1E6)=%lld < mNTPBase=%lld",(int64_t)(npt1 * 1E6),mNTPBase);
					mIntSeverError = true; //we should not send any request to server
		 			//Tell APacketSource EOS
		            TrackInfo *track = &mTracks.editItemAt(i);
		            track->mPacketSource->signalEOS(ERROR_END_OF_STREAM);    	
				}
            }
            // always set mFirstAccessUnitNTP = 0 for NTP <=> NPT mapping mode
            mFirstAccessUnit = false;
            mFirstAccessUnitNTP = 0;
            if (npt2 > 0 && npt1 >= npt2) {
                mIntSeverError = true;
            }
        }
        mSeekable = !isLive;
#else
        mSeekable = true;
#endif // #ifndef ANDROID_DEFAULT_CODE

    }

    sp<MetaData> getTrackFormat(size_t index, int32_t *timeScale) {
        CHECK_GE(index, 0u);
        CHECK_LT(index, mTracks.size());

        const TrackInfo &info = mTracks.itemAt(index);

        *timeScale = info.mTimeScale;

        return info.mPacketSource->getFormat();
    }

    sp<APacketSource> getPacketSource(size_t index) {
        CHECK_GE(index, 0u);
        CHECK_LT(index, mTracks.size());

        return mTracks.editItemAt(index).mPacketSource;
    }

    size_t countTracks() const {
        return mTracks.size();
    }

private:
    struct TrackInfo {
        AString mURL;
        int mRTPSocket;
        int mRTCPSocket;
        bool mUsingInterleavedTCP;
        uint32_t mFirstSeqNumInSegment;
        bool mNewSegment;

        uint32_t mRTPAnchor;
        int64_t mNTPAnchorUs;
        int32_t mTimeScale;

        uint32_t mNormalPlayTimeRTP;
        int64_t mNormalPlayTimeUs;

        sp<APacketSource> mPacketSource;

        // Stores packets temporarily while no notion of time
        // has been established yet.
        List<sp<ABuffer> > mPackets;
#ifndef ANDROID_DEFAULT_CODE 
        bool mFirstAccessUnit;
        bool mUseTrackNTP;
        uint64_t mFirstAccessUnitNTP;
        int mRTPPort;
        AString mTransport;
        int mTimeoutTrials;
        int64_t mLastNPT;
		float mPlayStartTime; //add by haizhen
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		 unsigned long mNADUFreq;
#endif
#endif // #ifndef ANDROID_DEFAULT_CODE
    };

    sp<AMessage> mNotify;
    bool mUIDValid;
    uid_t mUID;
    sp<ALooper> mLooper;
    sp<ALooper> mNetLooper;
    sp<ARTSPConnection> mConn;
    sp<ARTPConnection> mRTPConn;
    sp<ASessionDescription> mSessionDesc;
    AString mOriginalSessionURL;  // This one still has user:pass@
    AString mSessionURL;
    AString mSessionHost;
    AString mBaseURL;
    AString mSessionID;
    bool mSetupTracksSuccessful;
    bool mSeekPending;
    bool mFirstAccessUnit;

    int64_t mNTPAnchorUs;
    int64_t mMediaAnchorUs;
    int64_t mLastMediaTimeUs;

    int64_t mNumAccessUnitsReceived;
    bool mCheckPending;
    int32_t mCheckGeneration;
    bool mTryTCPInterleaving;
    bool mTryFakeRTCP;
    bool mReceivedFirstRTCPPacket;
    bool mReceivedFirstRTPPacket;
    bool mSeekable;
    int64_t mKeepAliveTimeoutUs;
    int32_t mKeepAliveGeneration;

    Vector<TrackInfo> mTracks;

    sp<AMessage> mDoneMsg;

#ifndef ANDROID_DEFAULT_CODE 
    // please add new member below this line
    bool mPausePending;
    uint64_t mFirstAccessUnitNTP;
    sp<ALooper> mRTSPNetLooper;
    int64_t mLastSeekTimeTime;
	bool mIntSeverError; //haizhen
	bool mTiouPending; //haizhen for 'tiou' AMessage. if pause or seek need cancel 'tiou'
    int32_t mTiouGeneration; //haizhen  for 'tiou' AMessage. we only need handle the the new 'tiou' 
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
    bool mSwitchingTCP;
    int mTryingTCPIndex;
#endif
    bool mIsLive;
    bool mSkipDescribe;
    // only SETUP the first supported video/audio stream
    // FIXME There should be a track selection procedure in AwesomePlayer
    // to do a better selection
    bool mPlaySent;
    bool mHaveVideo;
    bool mHaveAudio;
    status_t mFinalStatus;
    bool mNPTPending;
    bool mPendingByPause;
    int64_t mInitNPT;
    bool mExited;
    int64_t mNTPBase;
    int64_t mNPTBase;
    int64_t mLastNPT;
    bool mNPTMode;
    bool mRegistered;
    int mMinUDPPort;
    int mMaxUDPPort;
    AString mServerInfo;
	bool mIsDarwinServer;
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
	//for Bitrate-Adaptation
	bool mSupBitRateAdap;
#endif
	
#endif // #ifndef ANDROID_DEFAULT_CODE
    void setupTrack(size_t index) {
        sp<APacketSource> source =
            new APacketSource(mSessionDesc, index);

        if (source->initCheck() != OK) {
            LOGW("Unsupported format. Ignoring track #%d.", index);

            sp<AMessage> reply = new AMessage('setu', id());
            reply->setSize("index", index);
            reply->setInt32("result", ERROR_UNSUPPORTED);
#ifndef ANDROID_DEFAULT_CODE 
            if (source->initCheck() == ERROR_UNSUPPORTED) {
                const char *mime;
                if (source->getFormat()->findCString(kKeyMIMEType, &mime)){
                    if (!strncasecmp(mime, "video/", 6)) {
                        reply->setInt32("unsupport-video", 1);
                    }
                }
            }
#endif
            reply->post();
            return;
#ifndef ANDROID_DEFAULT_CODE 
        } else {
            // skip multiple audio/video streams
            bool skip = false;
            sp<MetaData> meta = source->getFormat();
            const char *mime = "";
            CHECK(meta->findCString(kKeyMIMEType, &mime));
            if (!strncasecmp(mime, "video/", 6)) {
                if (mHaveVideo) {
                    LOGW("Skip multiple video stream. Ignoring track #%d.", 
                            index);
                    skip = true;
                } else {
                    mHaveVideo = true;
                }
            } else if (!strncasecmp(mime, "audio/", 6)) {
                if (mHaveAudio) {
                    LOGW("Skip multiple audio stream. Ignoring track #%d.", 
                            index);
                    skip = true;
                } else {
                    mHaveAudio = true;
                }
            } else {
                LOGW("Unsupported format %s. Ignoring track #%d.", mime, 
                        index);
                skip = true;
            }
            
            if (skip) {
                sp<AMessage> reply = new AMessage('setu', id());
                reply->setSize("index", index);
                reply->setInt32("result", ERROR_UNSUPPORTED);
                reply->post();
                return;
            }
#endif // #ifndef ANDROID_DEFAULT_CODE
        }

        AString url;
        CHECK(mSessionDesc->findAttribute(index, "a=control", &url));

        AString trackURL;
        CHECK(MakeURL(mBaseURL.c_str(), url.c_str(), &trackURL));

#ifdef MTK_RTP_OVER_RTSP_SUPPORT
        TrackInfo *info;
        if (mSwitchingTCP) {
            if (mTracks.size() == 0) {
                LOGW("setupTrack %d after abor", mTryingTCPIndex);
                sp<AMessage> reply = new AMessage('setu', id());
                reply->setSize("index", index);
                reply->setInt32("result", ERROR_OUT_OF_RANGE);
                reply->setSize("track-index", mTryingTCPIndex);
                reply->post();
                return;
            }
            CHECK(mTryingTCPIndex < (int32_t)mTracks.size());
            info = &mTracks.editItemAt(mTryingTCPIndex++);
            source = info->mPacketSource;
        } else {
            mTracks.push(TrackInfo());
            info = &mTracks.editItemAt(mTracks.size() - 1);
            mTryingTCPIndex = mTracks.size();
        }
#else
        mTracks.push(TrackInfo());
        TrackInfo *info = &mTracks.editItemAt(mTracks.size() - 1);
#endif
#ifndef ANDROID_DEFAULT_CODE 
        info->mTimeoutTrials = 0;
        info->mLastNPT = 0;
        info->mFirstAccessUnit = true;
        info->mUseTrackNTP = false;
#endif // #ifndef ANDROID_DEFAULT_CODE
        info->mURL = trackURL;
        info->mPacketSource = source;
        info->mUsingInterleavedTCP = false;
        info->mFirstSeqNumInSegment = 0;
        info->mNewSegment = true;
        info->mRTPAnchor = 0;
        info->mNTPAnchorUs = -1;
        info->mNormalPlayTimeRTP = 0;
        info->mNormalPlayTimeUs = 0ll;

        unsigned long PT;
        AString formatDesc;
        AString formatParams;
        mSessionDesc->getFormatType(index, &PT, &formatDesc, &formatParams);

        int32_t timescale;
        int32_t numChannels;
        ASessionDescription::ParseFormatDesc(
                formatDesc.c_str(), &timescale, &numChannels);

        info->mTimeScale = timescale;

#ifndef ANDROID_DEFAULT_CODE 	//add by haizhen start, for the a=range:xx-xx,not star at 0
        info->mPlayStartTime = 0.0;
        AString play_range;
        if(mSessionDesc->findAttribute(index, "a=range", &play_range)){
            float range1,range2; 
            if (ASessionDescription::parseNTPRange(play_range.c_str(), &range1, &range2)){

                info->mPlayStartTime = range1;
                LOGI("track %d,a=range:%.2f-%.2f",index,range1,range2);
            }
        }
#endif		//add by haizhen stop	

        LOGV("track #%d URL=%s", mTracks.size(), trackURL.c_str());

        AString request = "SETUP ";
        request.append(trackURL);
        request.append(" RTSP/1.0\r\n");

        if (mTryTCPInterleaving) {
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
            size_t interleaveIndex = 2 * (mTryingTCPIndex - 1);
#else
            size_t interleaveIndex = 2 * (mTracks.size() - 1);
#endif
            info->mUsingInterleavedTCP = true;
            info->mRTPSocket = interleaveIndex;
            info->mRTCPSocket = interleaveIndex + 1;

            request.append("Transport: RTP/AVP/TCP;interleaved=");
            request.append(interleaveIndex);
            request.append("-");
            request.append(interleaveIndex + 1);
        } else {
            unsigned rtpPort;
            ARTPConnection::MakePortPair(
#ifndef ANDROID_DEFAULT_CODE 
                    &info->mRTPSocket, &info->mRTCPSocket, &rtpPort,
                    mMinUDPPort, mMaxUDPPort);
#else
                    &info->mRTPSocket, &info->mRTCPSocket, &rtpPort);
#endif // #ifndef ANDROID_DEFAULT_CODE
#ifndef ANDROID_DEFAULT_CODE 
            info->mRTPPort = rtpPort;
#endif // #ifndef ANDROID_DEFAULT_CODE

            if (mUIDValid) {
                HTTPBase::RegisterSocketUserTag(info->mRTPSocket, mUID,
                                                (uint32_t)*(uint32_t*) "RTP_");
                HTTPBase::RegisterSocketUserTag(info->mRTCPSocket, mUID,
                                                (uint32_t)*(uint32_t*) "RTP_");
            }

            request.append("Transport: RTP/AVP/UDP;unicast;client_port=");
            request.append(rtpPort);
            request.append("-");
            request.append(rtpPort + 1);
        }

        request.append("\r\n");

//for Bitrate-Adaptation
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		if(mSupBitRateAdap){
			info->mNADUFreq = 0;
			 AString nadu_freq;
        	 if(!mIsDarwinServer && mSessionDesc->findAttribute(index, "a=3GPP-Adaptation-Support", &nadu_freq))
    	 	{
    	 		  char *end;
   				  info->mNADUFreq = strtoul(nadu_freq.c_str(), &end, 10);
    	 	}
			LOGI("NADU Frequence =%d",info->mNADUFreq);
			//if Server support Bitrate-Adaptation
			if(info->mNADUFreq > 0){
				//get Queue buffer size and target protect time
				sp<APacketSource> packSource =info->mPacketSource;
				size_t bufQueSize = packSource->getBufQueSize();
				size_t targetTimeMs = packSource->getTargetTime(); //count in ms				
				request.append("3GPP-Adaptation:url=");
				request.append("\"");
				request.append(trackURL);
				request.append("\"");	
				request.append(";size=");
				request.append(bufQueSize);
				request.append(";target-time=");
				request.append(targetTimeMs);
				request.append("\r\n");
				LOGI("sending 3GPP-Adaptation:%s",request.c_str());
			}		
		}		
#endif

#ifndef ANDROID_DEFAULT_CODE 
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
        if (mTryingTCPIndex > 1) {
#else
        if (mTracks.size() > 1) {
#endif
#else
        if (index > 1) {
#endif
            request.append("Session: ");
            request.append(mSessionID);
            request.append("\r\n");
        }

        request.append("\r\n");

        sp<AMessage> reply = new AMessage('setu', id());
        reply->setSize("index", index);
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
        reply->setSize("track-index", mTryingTCPIndex - 1);
#else
        reply->setSize("track-index", mTracks.size() - 1);
#endif
        mConn->sendRequest(request.c_str(), reply);
    }

    static bool MakeURL(const char *baseURL, const char *url, AString *out) {
        out->clear();

        if (strncasecmp("rtsp://", baseURL, 7)) {
            // Base URL must be absolute
            return false;
        }

        if (!strncasecmp("rtsp://", url, 7)) {
            // "url" is already an absolute URL, ignore base URL.
            out->setTo(url);
            return true;
        }

        size_t n = strlen(baseURL);
        if (baseURL[n - 1] == '/') {
            out->setTo(baseURL);
            out->append(url);
        } else {
#ifndef ANDROID_DEFAULT_CODE 
            // no strip chars after last '/'
            out->setTo(baseURL);
#else
            const char *slashPos = strrchr(baseURL, '/');

            if (slashPos > &baseURL[6]) {
                out->setTo(baseURL, slashPos - baseURL);
            } else {
                out->setTo(baseURL);
            }
#endif // #ifndef ANDROID_DEFAULT_CODE

            out->append("/");
            out->append(url);
        }

        return true;
    }

    void fakeTimestamps() {
        for (size_t i = 0; i < mTracks.size(); ++i) {
            onTimeUpdate(i, 0, 0ll);
        }
    }

    void onTimeUpdate(int32_t trackIndex, uint32_t rtpTime, uint64_t ntpTime) {
        LOGV("onTimeUpdate track %d, rtpTime = 0x%08x, ntpTime = 0x%016llx",
             trackIndex, rtpTime, ntpTime);

        int64_t ntpTimeUs = (int64_t)(ntpTime * 1E6 / (1ll << 32));

        TrackInfo *track = &mTracks.editItemAt(trackIndex);

        track->mRTPAnchor = rtpTime;
        track->mNTPAnchorUs = ntpTimeUs;

        if (mNTPAnchorUs < 0) {
            mNTPAnchorUs = ntpTimeUs;
            mMediaAnchorUs = mLastMediaTimeUs;
        }
    }

    void onAccessUnitComplete(
            int32_t trackIndex, const sp<ABuffer> &accessUnit) {
        LOGV("onAccessUnitComplete track %d", trackIndex);

        if (mFirstAccessUnit) {
            mDoneMsg->setInt32("result", OK);
            mDoneMsg->post();
            mDoneMsg = NULL;

            mFirstAccessUnit = false;
        }

        TrackInfo *track = &mTracks.editItemAt(trackIndex);

        if (mNTPAnchorUs < 0 || mMediaAnchorUs < 0 || track->mNTPAnchorUs < 0) {
            LOGV("storing accessUnit, no time established yet");
            track->mPackets.push_back(accessUnit);
            return;
        }

        while (!track->mPackets.empty()) {
            sp<ABuffer> accessUnit = *track->mPackets.begin();
            track->mPackets.erase(track->mPackets.begin());

            if (addMediaTimestamp(trackIndex, track, accessUnit)) {
                track->mPacketSource->queueAccessUnit(accessUnit);
            }
        }

        if (addMediaTimestamp(trackIndex, track, accessUnit)) {
            track->mPacketSource->queueAccessUnit(accessUnit);
        }
    }

    bool addMediaTimestamp(
            int32_t trackIndex, const TrackInfo *track,
            const sp<ABuffer> &accessUnit) {
        uint32_t rtpTime;
        CHECK(accessUnit->meta()->findInt32(
                    "rtp-time", (int32_t *)&rtpTime));

        int64_t relRtpTimeUs =
            (((int64_t)rtpTime - (int64_t)track->mRTPAnchor) * 1000000ll)
                / track->mTimeScale;

        int64_t ntpTimeUs = track->mNTPAnchorUs + relRtpTimeUs;

        int64_t mediaTimeUs = mMediaAnchorUs + ntpTimeUs - mNTPAnchorUs;

        if (mediaTimeUs > mLastMediaTimeUs) {
            mLastMediaTimeUs = mediaTimeUs;
        }

        if (mediaTimeUs < 0) {
            LOGV("dropping early accessUnit.");
            return false;
        }

        LOGV("track %d rtpTime=%d mediaTimeUs = %lld us (%.2f secs)",
             trackIndex, rtpTime, mediaTimeUs, mediaTimeUs / 1E6);

        accessUnit->meta()->setInt64("timeUs", mediaTimeUs);

        return true;
    }

    void postQueueAccessUnit(
            size_t trackIndex, const sp<ABuffer> &accessUnit) {
        sp<AMessage> msg = mNotify->dup();
        msg->setInt32("what", kWhatAccessUnit);
        msg->setSize("trackIndex", trackIndex);
        msg->setObject("accessUnit", accessUnit);
        msg->post();
    }

    void postQueueEOS(size_t trackIndex, status_t finalResult) {
        sp<AMessage> msg = mNotify->dup();
        msg->setInt32("what", kWhatEOS);
        msg->setSize("trackIndex", trackIndex);
        msg->setInt32("finalResult", finalResult);
        msg->post();
    }

    void postQueueSeekDiscontinuity(size_t trackIndex) {
        sp<AMessage> msg = mNotify->dup();
        msg->setInt32("what", kWhatSeekDiscontinuity);
        msg->setSize("trackIndex", trackIndex);
        msg->post();
    }

    void postNormalPlayTimeMapping(
            size_t trackIndex, uint32_t rtpTime, int64_t nptUs) {
        sp<AMessage> msg = mNotify->dup();
        msg->setInt32("what", kWhatNormalPlayTimeMapping);
        msg->setSize("trackIndex", trackIndex);
        msg->setInt32("rtpTime", rtpTime);
        msg->setInt64("nptUs", nptUs);
        msg->post();
    }

#ifndef ANDROID_DEFAULT_CODE 
    void disc(status_t result) {
        mFinalStatus = result;
        sp<AMessage> reply = new AMessage('disc', id());
        mConn->disconnect(reply);
    }

    void checkServer() {
        LOGI("server info %s", mServerInfo.c_str());
        mServerInfo.tolower();
        if((mServerInfo.size() > 0) && 
                ((-1 != mServerInfo.find("dss")) || (-1 != mServerInfo.find("darwin")))){

            mIsDarwinServer = true;
        }
    }

    void parseHeaders(const KeyedVector<String8, String8> *headers) {
#ifdef MTK_RTP_OVER_RTSP_SUPPORT
        char value[PROPERTY_VALUE_MAX];
        if (property_get("media.stagefright.force-rtp-tcp", value, NULL)
                && (!strcmp(value, "1") || !strcasecmp(value, "true"))) {
            mTryTCPInterleaving = true;
        }
#endif
        if (headers == NULL)
            return;

        int min = -1, max = -1;
        int port = -1;
        AString host;
        for (size_t i=0; i<headers->size(); ++i) {
            const char* k = headers->keyAt(i).string();
            const char* v = headers->valueAt(i).string();
            if (strlen(v) == 0)
                continue;
            if (!strcmp(k, "MIN-UDP-PORT")) {
                LOGD ("RTSP Min UDP Port: %s", v);
                min = atoi(v);
                continue;
            }
            if (!strcmp(k, "MAX-UDP-PORT")) {
                LOGD ("RTSP Max UDP Port: %s", v);
                max = atoi(v);
                continue;
            }
            if (!strcmp(k, "MTK-RTSP-PROXY-HOST")) {
                LOGD ("RTSP Proxy Host: %s", v);
                host.setTo(v);
                continue;
            }
            if (!strcmp(k, "MTK-RTSP-PROXY-PORT")) {
                LOGD ("RTSP Proxy Port: %s", v);
                port = atoi(v);
                continue;
            }
            if (!strcmp(k, "MTK-RTSP-RTP-OVER-RTSP")) {
                LOGD ("RTSP RTP over RTSP: %s", v);
                mTryTCPInterleaving = atoi(v) != 0;
            }
        }

        if (min != -1 || max != -1) {
            if (min >= 0 && max < 65536 && max > min + 5) {
                mMaxUDPPort = max;
                mMinUDPPort = min;
                LOGD ("Streaming-MIN-UDP-PORT=%d", min);
                LOGD ("Streaming-MAX-UDP-PORT=%d", max);
            } else {
                LOGW ("Ignore invalid min/max udp ports: %d/%d", min, max);
            }
        }

        if (!host.empty()) {
            if (port == -1) {
                LOGI ("use default proxy port 554");
                port = 554;
            }

            if (port < 0 || port >= 65536) {
                LOGW ("Ignore invalid proxy setting (port: %d)", port);
            } else {
                LOGD ("Streaming-Proxy=%s", host.c_str());
                LOGD ("Streaming-Proxy-Port=%d", port);
                mConn->setProxy(host, port);
            }
        }
    }

	void postTryTCPTimeOutCheck(){
		 if (kSRTimeoutUs != 0) {				
            sp<AMessage> timeout = new AMessage('tiou', id());
			mTiouPending = false; //add by haizhen
			mTiouGeneration = (int32_t)timeout.get();//add by haizhen
			timeout->setInt32("generation",mTiouGeneration);//add by haizhen
            timeout->post(kSRTimeoutUs);	
        }	
	}
	
public:
    // return in ms
    int32_t getServerTimeout() {
        return mKeepAliveTimeoutUs / 1000;
    }

    status_t setSessionDesc(sp<ASessionDescription> desc) {
        mSessionDesc = desc;
        if (!mSessionDesc->isValid())
            return ERROR_MALFORMED;

        if (mSessionDesc->countTracks() == 1u)
            return ERROR_UNSUPPORTED;

        int64_t tmp;
        mIsLive = !mSessionDesc->getDurationUs(&tmp);
        mBaseURL = mSessionURL;
        mSkipDescribe = true;
        return OK;
    }

    void play(const sp<AMessage> &doneMsg, bool bIsPaused=false) {   //haizhen
        if(mIntSeverError){
            LOGE("[rtsp]Internal server error happen,Play return immediately");
            if(doneMsg.get()){
                doneMsg->setInt32("result", OK);
                doneMsg->post(); 
            }
            return;
        }

        mDoneMsg = doneMsg;
        sp<AMessage> msg = new AMessage('ply1', id());
        msg->setInt32("play", true);
        msg->setInt32("paus",bIsPaused);
        if (!bIsPaused) {
            msg->setInt64("play-time", 0);
        }
        msg->post();
    }
	
	//add by Haizhen
	void sendPause(const sp<AMessage> &doneMsg = NULL)
	{
		 if(mIntSeverError){
			LOGE("[rtsp]Internal server error happen,SendPause return immediately");
			if(doneMsg.get()){
				doneMsg->setInt32("result", OK);
				doneMsg->post(); 
			}
			return;
		 }
		 
        sp<AMessage> msg = new AMessage('paus', id());
		if(msg.get())
		{
			if(doneMsg.get())
			{
				mDoneMsg = doneMsg;
				msg->setMessage("doneMsg", doneMsg);
			}

			msg->post();
		}
		
	}

    void stopTCPTrying() {
        if (mConn != NULL)
            mConn->stopTCPTrying();
    }

    void exit() {
        mExited = true;
    }

    // a sync call to flush packets and pause ourself to receive data
    void preSeek(int64_t timeUs, const sp<AMessage> &doneMsg) {
        sp<AMessage> msg = new AMessage('seek', id());
        msg->setInt64("time", timeUs);
        msg->setMessage("doneMsg", doneMsg);
        msg->post();
    }

    sp<ASessionDescription> getSessionDesc() {
        return mSessionDesc;
    }

    ~MyHandler() {
        if (mRegistered) {
            if (mLooper != NULL) {
                mLooper->unregisterHandler(id());
            }

            if (mRTSPNetLooper != NULL && mConn != NULL) {
                mRTSPNetLooper->unregisterHandler(mConn->id());
            }

            if (mNetLooper != NULL && mRTPConn != NULL)
                mNetLooper->unregisterHandler(mRTPConn->id());
        }
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    DISALLOW_EVIL_CONSTRUCTORS(MyHandler);
};

}  // namespace android

#endif  // MY_HANDLER_H_
