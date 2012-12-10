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
#define LOG_TAG "APacketSource"
#include <utils/Log.h>

#include "APacketSource.h"

#include "ARawAudioAssembler.h"
#include "ASessionDescription.h"

#include "avc_utils.h"

#include <ctype.h>

#include <media/stagefright/foundation/ABitReader.h>
#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/foundation/AString.h>
#include <media/stagefright/foundation/base64.h>
#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MetaData.h>
#include <utils/Vector.h>

#ifndef ANDROID_DEFAULT_CODE 

#define RTSP_CHECK_VIDEO_BY_DRV
#ifdef RTSP_CHECK_VIDEO_BY_DRV
#include "vdec_drv_if.h"
//#include "val_types.h"
#endif //RTSP_CHECK_VIDEO_BY_DRV

#if defined(MT6575) || defined(MT6577)
static int kMaxBitrateH264 = 4000000ll;
static int kMaxBitrateMPEG4 = 4000000ll;
#else
static int kMaxBitrateH264 = 6000000ll;
static int kMaxBitrateMPEG4 = 20000000ll;
#endif
static int kMaxInputSizeAMR = 2000;

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
static int kWholeBufSize = 40000000; //40Mbytes
static int kTargetTime = 2000;  //ms
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE

namespace android {

#ifndef ANDROID_DEFAULT_CODE 
static bool GetAttribute(const char *s, const char *key, AString *value, bool checkExist = false) {
#else
static bool GetAttribute(const char *s, const char *key, AString *value) {
#endif // #ifndef ANDROID_DEFAULT_CODE
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

#ifndef ANDROID_DEFAULT_CODE 
        if (checkExist && len == keyLen && !strncmp(s, key, keyLen)) {
            value->setTo("1");
            return true;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE

        if (colonPos == NULL) {
            return false;
        }

        s = colonPos + 1;
    }
}

static sp<ABuffer> decodeHex(const AString &s) {
    if ((s.size() % 2) != 0) {
        return NULL;
    }

    size_t outLen = s.size() / 2;
    sp<ABuffer> buffer = new ABuffer(outLen);
    uint8_t *out = buffer->data();

    uint8_t accum = 0;
    for (size_t i = 0; i < s.size(); ++i) {
        char c = s.c_str()[i];
        unsigned value;
        if (c >= '0' && c <= '9') {
            value = c - '0';
        } else if (c >= 'a' && c <= 'f') {
            value = c - 'a' + 10;
        } else if (c >= 'A' && c <= 'F') {
            value = c - 'A' + 10;
        } else {
            return NULL;
        }

        accum = (accum << 4) | value;

        if (i & 1) {
            *out++ = accum;

            accum = 0;
        }
    }

    return buffer;
}

static sp<ABuffer> MakeAVCCodecSpecificData(
        const char *params, int32_t *width, int32_t *height) {
    *width = 0;
    *height = 0;

    AString val;
    if (!GetAttribute(params, "profile-level-id", &val)) {
#ifndef ANDROID_DEFAULT_CODE
        LOGW("no profile-level-id is found");
        val.setTo("4DE01E");
#else
        return NULL;
#endif
    }

    sp<ABuffer> profileLevelID = decodeHex(val);
    CHECK(profileLevelID != NULL);
    CHECK_EQ(profileLevelID->size(), 3u);

    Vector<sp<ABuffer> > paramSets;

    size_t numSeqParameterSets = 0;
    size_t totalSeqParameterSetSize = 0;
    size_t numPicParameterSets = 0;
    size_t totalPicParameterSetSize = 0;

    if (!GetAttribute(params, "sprop-parameter-sets", &val)) {
        return NULL;
    }

    size_t start = 0;
    for (;;) {
        ssize_t commaPos = val.find(",", start);
        size_t end = (commaPos < 0) ? val.size() : commaPos;

        AString nalString(val, start, end - start);
        sp<ABuffer> nal = decodeBase64(nalString);
        CHECK(nal != NULL);
        CHECK_GT(nal->size(), 0u);
        CHECK_LE(nal->size(), 65535u);

        uint8_t nalType = nal->data()[0] & 0x1f;
        if (numSeqParameterSets == 0) {
            CHECK_EQ((unsigned)nalType, 7u);
        } else if (numPicParameterSets > 0) {
            CHECK_EQ((unsigned)nalType, 8u);
        }
        if (nalType == 7) {
            ++numSeqParameterSets;
            totalSeqParameterSetSize += nal->size();
        } else  {
            CHECK_EQ((unsigned)nalType, 8u);
            ++numPicParameterSets;
            totalPicParameterSetSize += nal->size();
        }

        paramSets.push(nal);

        if (commaPos < 0) {
            break;
        }

        start = commaPos + 1;
    }

    CHECK_LT(numSeqParameterSets, 32u);
    CHECK_LE(numPicParameterSets, 255u);

    size_t csdSize =
        1 + 3 + 1 + 1
        + 2 * numSeqParameterSets + totalSeqParameterSetSize
        + 1 + 2 * numPicParameterSets + totalPicParameterSetSize;

    sp<ABuffer> csd = new ABuffer(csdSize);
    uint8_t *out = csd->data();

    *out++ = 0x01;  // configurationVersion
    memcpy(out, profileLevelID->data(), 3);
    out += 3;
    *out++ = (0x3f << 2) | 1;  // lengthSize == 2 bytes
    *out++ = 0xe0 | numSeqParameterSets;

    for (size_t i = 0; i < numSeqParameterSets; ++i) {
        sp<ABuffer> nal = paramSets.editItemAt(i);

        *out++ = nal->size() >> 8;
        *out++ = nal->size() & 0xff;

        memcpy(out, nal->data(), nal->size());

        out += nal->size();

        if (i == 0) {
            FindAVCDimensions(nal, width, height);
            LOGI("dimensions %dx%d", *width, *height);
        }
    }

    *out++ = numPicParameterSets;

    for (size_t i = 0; i < numPicParameterSets; ++i) {
        sp<ABuffer> nal = paramSets.editItemAt(i + numSeqParameterSets);

        *out++ = nal->size() >> 8;
        *out++ = nal->size() & 0xff;

        memcpy(out, nal->data(), nal->size());

        out += nal->size();
    }

    // hexdump(csd->data(), csd->size());

    return csd;
}

#ifndef ANDROID_DEFAULT_CODE 
static bool checkAACConfig(uint8_t *config) {
    int aacObjectType = config[0] >> 3;
    if ((aacObjectType != 2)    // AAC LC (Low Complexity) 
            && (aacObjectType != 4)      // AAC LTP (Long Term Prediction)
            && (aacObjectType != 5)      // SBR (Spectral Band Replication) 
            && (aacObjectType != 29))   // PS (Parametric Stereo)          
    {
        LOGE ("[AAC capability error]Unsupported audio object type: (%d), , ignore audio track", aacObjectType);
        return false;
    }
    LOGI("aacObjectType %d", aacObjectType);
    return true;
}
#endif // #ifndef ANDROID_DEFAULT_CODE

sp<ABuffer> MakeAACCodecSpecificData(const char *params) {
    AString val;
    CHECK(GetAttribute(params, "config", &val));

    sp<ABuffer> config = decodeHex(val);
    CHECK(config != NULL);
    CHECK_GE(config->size(), 4u);

    const uint8_t *data = config->data();
    uint32_t x = data[0] << 24 | data[1] << 16 | data[2] << 8 | data[3];
    x = (x >> 1) & 0xffff;

    static const uint8_t kStaticESDS[] = {
        0x03, 22,
        0x00, 0x00,     // ES_ID
        0x00,           // streamDependenceFlag, URL_Flag, OCRstreamFlag

        0x04, 17,
        0x40,                       // Audio ISO/IEC 14496-3
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,

        0x05, 2,
        // AudioSpecificInfo follows
    };

#ifndef ANDROID_DEFAULT_CODE
    if ((((x >> 8) & 0xff) >> 3) == 5) {
        CHECK_GE(config->size(), 6u);
        x = data[2] << 24 | data[3] << 16 | data[4] << 8 | data[5];
        x = (x >> 1);
        LOGI("sbr detected %x", x);

        sp<ABuffer> csd = new ABuffer(sizeof(kStaticESDS) + 4);
        memcpy(csd->data(), kStaticESDS, sizeof(kStaticESDS));
        csd->data()[1] += 2;
        csd->data()[6] += 2;
        csd->data()[21] += 2;
        csd->data()[sizeof(kStaticESDS)] = (x >> 24) & 0xff;
        csd->data()[sizeof(kStaticESDS) + 1] = (x >> 16) & 0xff;
        csd->data()[sizeof(kStaticESDS) + 2] = (x >> 8) & 0xff;
        csd->data()[sizeof(kStaticESDS) + 3] = x & 0xff;

        return csd;
    }
#endif

    sp<ABuffer> csd = new ABuffer(sizeof(kStaticESDS) + 2);
    memcpy(csd->data(), kStaticESDS, sizeof(kStaticESDS));
    csd->data()[sizeof(kStaticESDS)] = (x >> 8) & 0xff;
    csd->data()[sizeof(kStaticESDS) + 1] = x & 0xff;

#ifndef ANDROID_DEFAULT_CODE 
    if (!checkAACConfig(csd->data() + sizeof(kStaticESDS))) {
        return NULL;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    // hexdump(csd->data(), csd->size());

    return csd;
}

// From mpeg4-generic configuration data.
sp<ABuffer> MakeAACCodecSpecificData2(const char *params) {
    AString val;
    unsigned long objectType;
    if (GetAttribute(params, "objectType", &val)) {
        const char *s = val.c_str();
        char *end;
        objectType = strtoul(s, &end, 10);
        CHECK(end > s && *end == '\0');
    } else {
        objectType = 0x40;  // Audio ISO/IEC 14496-3
    }

    CHECK(GetAttribute(params, "config", &val));

    sp<ABuffer> config = decodeHex(val);
    CHECK(config != NULL);

    // Make sure size fits into a single byte and doesn't have to
    // be encoded.
    CHECK_LT(20 + config->size(), 128u);

    const uint8_t *data = config->data();

    static const uint8_t kStaticESDS[] = {
        0x03, 22,
        0x00, 0x00,     // ES_ID
        0x00,           // streamDependenceFlag, URL_Flag, OCRstreamFlag

        0x04, 17,
        0x40,                       // Audio ISO/IEC 14496-3
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,

        0x05, 2,
        // AudioSpecificInfo follows
    };

    sp<ABuffer> csd = new ABuffer(sizeof(kStaticESDS) + config->size());
    uint8_t *dst = csd->data();
    *dst++ = 0x03;
    *dst++ = 20 + config->size();
    *dst++ = 0x00;  // ES_ID
    *dst++ = 0x00;
    *dst++ = 0x00;  // streamDependenceFlag, URL_Flag, OCRstreamFlag
    *dst++ = 0x04;
    *dst++ = 15 + config->size();
    *dst++ = objectType;
    for (int i = 0; i < 12; ++i) { *dst++ = 0x00; }
    *dst++ = 0x05;
    *dst++ = config->size();
    memcpy(dst, config->data(), config->size());

#ifndef ANDROID_DEFAULT_CODE 
    if (!checkAACConfig(config->data())) {
        return NULL;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    // hexdump(csd->data(), csd->size());

    return csd;
}

static size_t GetSizeWidth(size_t x) {
    size_t n = 1;
    while (x > 127) {
        ++n;
        x >>= 7;
    }
    return n;
}

static uint8_t *EncodeSize(uint8_t *dst, size_t x) {
    while (x > 127) {
        *dst++ = (x & 0x7f) | 0x80;
        x >>= 7;
    }
    *dst++ = x;
    return dst;
}

static bool ExtractDimensionsMPEG4Config(
        const sp<ABuffer> &config, int32_t *width, int32_t *height) {
    *width = 0;
    *height = 0;

    const uint8_t *ptr = config->data();
    size_t offset = 0;
    bool foundVOL = false;
    while (offset + 3 < config->size()) {
        if (memcmp("\x00\x00\x01", &ptr[offset], 3)
                || (ptr[offset + 3] & 0xf0) != 0x20) {
            ++offset;
            continue;
        }

        foundVOL = true;
        break;
    }

    if (!foundVOL) {
        return false;
    }

    return ExtractDimensionsFromVOLHeader(
            &ptr[offset], config->size() - offset, width, height);
}

static sp<ABuffer> MakeMPEG4VideoCodecSpecificData(
        const char *params, int32_t *width, int32_t *height) {
    *width = 0;
    *height = 0;

    AString val;
    CHECK(GetAttribute(params, "config", &val));

    sp<ABuffer> config = decodeHex(val);
    CHECK(config != NULL);

    if (!ExtractDimensionsMPEG4Config(config, width, height)) {
        return NULL;
    }

    LOGI("VOL dimensions = %dx%d", *width, *height);

    size_t len1 = config->size() + GetSizeWidth(config->size()) + 1;
    size_t len2 = len1 + GetSizeWidth(len1) + 1 + 13;
    size_t len3 = len2 + GetSizeWidth(len2) + 1 + 3;

    sp<ABuffer> csd = new ABuffer(len3);
    uint8_t *dst = csd->data();
    *dst++ = 0x03;
    dst = EncodeSize(dst, len2 + 3);
    *dst++ = 0x00;  // ES_ID
    *dst++ = 0x00;
    *dst++ = 0x00;  // streamDependenceFlag, URL_Flag, OCRstreamFlag

    *dst++ = 0x04;
    dst = EncodeSize(dst, len1 + 13);
    *dst++ = 0x01;  // Video ISO/IEC 14496-2 Simple Profile
    for (size_t i = 0; i < 12; ++i) {
        *dst++ = 0x00;
    }

    *dst++ = 0x05;
    dst = EncodeSize(dst, config->size());
    memcpy(dst, config->data(), config->size());
    dst += config->size();

    // hexdump(csd->data(), csd->size());

    return csd;
}

static bool GetClockRate(const AString &desc, uint32_t *clockRate) {
    ssize_t slashPos = desc.find("/");
    if (slashPos < 0) {
        return false;
    }

    const char *s = desc.c_str() + slashPos + 1;

    char *end;
    unsigned long x = strtoul(s, &end, 10);

    if (end == s || (*end != '\0' && *end != '/')) {
        return false;
    }

    *clockRate = x;

    return true;
}

#ifndef ANDROID_DEFAULT_CODE 

static bool checkVideoResolution(const char* vformat,int32_t width, int32_t height) {
    return true;

#ifdef RTSP_CHECK_VIDEO_BY_DRV
	// limit resolution according to the capability of platform
    // FIXME it is better to check resolution in OMXCodec, not here
    uint32_t MaxWidth = 720;
    uint32_t MaxHeight = 480;
	uint32_t MaxValue = 720;
	
	VDEC_DRV_QUERY_VIDEO_FORMAT_T qinfo;
	VDEC_DRV_QUERY_VIDEO_FORMAT_T outinfo;
	memset(&qinfo,0,sizeof(VDEC_DRV_QUERY_VIDEO_FORMAT_T));
	memset(&outinfo,0,sizeof(VDEC_DRV_QUERY_VIDEO_FORMAT_T));
	
	VDEC_DRV_MRESULT_T ret;	
	if(!strcmp(MEDIA_MIMETYPE_VIDEO_AVC,vformat)){
		qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_H264;
	}
	else if(!strcmp(MEDIA_MIMETYPE_VIDEO_H263,vformat)){
		qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
	}
	else if(!strcmp(MEDIA_MIMETYPE_VIDEO_MPEG4,vformat)){
		qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
	}
	else
		LOGW("checkVideoResolution not support Format=%s",vformat);
	
	ret = eVDecDrvQueryCapability(VDEC_DRV_QUERY_TYPE_VIDEO_FORMAT, &qinfo, &outinfo);

	if(ret ==  VDEC_DRV_MRESULT_OK){
		MaxWidth= outinfo.u4Width;
		MaxHeight = outinfo.u4Height;
		LOGI("checkVideoResolution, format=%s,support maxwidth=%d,maxheight=%d",vformat,MaxWidth,MaxHeight);
	}
 
	MaxValue = (MaxWidth >= MaxHeight) ? MaxWidth : MaxHeight;
	
    return (width <= (int32_t)MaxValue) && width > 0 &&
        (height <= (int32_t)MaxValue) && height > 0 &&
        (width * height <= MaxWidth*MaxHeight);

#else

    // limit resolution according to the capability of platform
    // FIXME it is better to check resolution in OMXCodec, not here
#ifdef MT6573_MFV_HW
    const int32_t _MAX_VIDEO_W_H_ = 864;
    const int32_t _MAX_VIDEO_DIM_ = 864*480;
#elif defined(MT6575) || defined(MT6577)
    const int32_t _MAX_VIDEO_W_H_ = 1280;
    const int32_t _MAX_VIDEO_DIM_ = 1280*720;
#else
    const int32_t _MAX_VIDEO_W_H_ = 720;
    const int32_t _MAX_VIDEO_DIM_ = 720*480;
#endif
    return width <= _MAX_VIDEO_W_H_ && width > 0 &&
        height <= _MAX_VIDEO_W_H_ && height > 0 &&
        (width * height <= _MAX_VIDEO_DIM_);

#endif
}

#endif // #ifndef ANDROID_DEFAULT_CODE

APacketSource::APacketSource(
        const sp<ASessionDescription> &sessionDesc, size_t index)
    : mInitCheck(NO_INIT),
      mFormat(new MetaData),
      mEOSResult(OK),
#ifndef ANDROID_DEFAULT_CODE 
      mNPTMappingIsSet(false),
      mWantsNALFragments(false),
      mAccessUnitTimeUs(-1),

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize(kWholeBufSize), //40Mbytes
		m_TargetTime(kTargetTime), //ms
		m_uiNextAduSeqNum(-1),
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE
      mIsAVC(false),
      mScanForIDR(true),
      mRTPTimeBase(0),
      mNormalPlayTimeBaseUs(0),
      mLastNormalPlayTimeUs(0) {
    unsigned long PT;
    AString desc;
    AString params;
    sessionDesc->getFormatType(index, &PT, &desc, &params);

    CHECK(GetClockRate(desc, &mClockRate));

    int64_t durationUs;
    if (sessionDesc->getDurationUs(&durationUs)) {
        mFormat->setInt64(kKeyDuration, durationUs);
    } else {
#ifndef ANDROID_DEFAULT_CODE 
        // set duration to 0 for live streaming
        mFormat->setInt64(kKeyDuration, 0ll);
#else
        mFormat->setInt64(kKeyDuration, 60 * 60 * 1000000ll);
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

#ifndef ANDROID_DEFAULT_CODE 
    int32_t maxBitrate = -1;
    AString val;
#endif // #ifndef ANDROID_DEFAULT_CODE

    mInitCheck = OK;
    if (!strncmp(desc.c_str(), "H264/", 5)) {
#ifndef ANDROID_DEFAULT_CODE 
        maxBitrate = kMaxBitrateH264;

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE
        mIsAVC = true;

        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_AVC);

        int32_t width, height;
        if (!sessionDesc->getDimensions(index, PT, &width, &height)) {
            width = -1;
            height = -1;
        }

#ifndef ANDROID_DEFAULT_CODE
        LOGI("width %d height %d", width, height);
#endif

        int32_t encWidth, encHeight;
        sp<ABuffer> codecSpecificData =
            MakeAVCCodecSpecificData(params.c_str(), &encWidth, &encHeight);

        if (codecSpecificData != NULL) {
#ifndef ANDROID_DEFAULT_CODE
            // always use enc width/height if available
            if (encWidth > 0 && encHeight > 0) {
#else
            if (width < 0) {
#endif
                // If no explicit width/height given in the sdp, use the dimensions
                // extracted from the first sequence parameter set.
                width = encWidth;
                height = encHeight;
            }

            mFormat->setData(
                    kKeyAVCC, 0,
                    codecSpecificData->data(), codecSpecificData->size());
        } else if (width < 0) {
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("[H264 capability error]Unsupported H264 video, bad params %s", params.c_str());
#endif // #ifndef ANDROID_DEFAULT_CODE
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }

#ifndef ANDROID_DEFAULT_CODE 
        if (!checkVideoResolution(MEDIA_MIMETYPE_VIDEO_AVC,width, height)) {
            LOGE("[H264 capability error]Unsupported H264 video, width %d, height %d", width, height);
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setInt32(kKeyWidth, width);
        mFormat->setInt32(kKeyHeight, height);
    } else if (!strncmp(desc.c_str(), "H263-2000/", 10)
            || !strncmp(desc.c_str(), "H263-1998/", 10)) {
#ifndef ANDROID_DEFAULT_CODE 
        maxBitrate = kMaxBitrateMPEG4;
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif
#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_H263);

        int32_t width, height;
        if (!sessionDesc->getDimensions(index, PT, &width, &height)) {
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("[H263 capability error]Unsupported H263 video, no resolution info");
#endif // #ifndef ANDROID_DEFAULT_CODE
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }

#ifndef ANDROID_DEFAULT_CODE
        LOGI("width %d height %d", width, height);
#endif

#ifndef ANDROID_DEFAULT_CODE 
        if (!checkVideoResolution(MEDIA_MIMETYPE_VIDEO_H263,width, height)) {
            LOGE("[H263 capability error]Unsupported H263 video, width %d, height %d", width, height);
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setInt32(kKeyWidth, width);
        mFormat->setInt32(kKeyHeight, height);
    } else if (!strncmp(desc.c_str(), "MP4A-LATM/", 10)) {
        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_AAC);

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif
        int32_t sampleRate, numChannels;
        ASessionDescription::ParseFormatDesc(
                desc.c_str(), &sampleRate, &numChannels);

        mFormat->setInt32(kKeySampleRate, sampleRate);
        mFormat->setInt32(kKeyChannelCount, numChannels);

        sp<ABuffer> codecSpecificData =
            MakeAACCodecSpecificData(params.c_str());

#ifndef ANDROID_DEFAULT_CODE 
        if (codecSpecificData == NULL) {
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setData(
                kKeyESDS, 0,
                codecSpecificData->data(), codecSpecificData->size());
    } else if (!strncmp(desc.c_str(), "AMR/", 4)) {
        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_AMR_NB);
		
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif

        int32_t sampleRate, numChannels;
        ASessionDescription::ParseFormatDesc(
                desc.c_str(), &sampleRate, &numChannels);

#ifndef ANDROID_DEFAULT_CODE
        if (sampleRate != 8000) {
            LOGW("bad AMR clock rate %d", sampleRate);
            sampleRate = 8000;
        }
#endif
        mFormat->setInt32(kKeySampleRate, sampleRate);
        mFormat->setInt32(kKeyChannelCount, numChannels);

        if (sampleRate != 8000 || numChannels != 1) {
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("[AMR capability error]Unsupported AMR audio, sample rate %d, channels %d", sampleRate, numChannels);
#endif // #ifndef ANDROID_DEFAULT_CODE
            mInitCheck = ERROR_UNSUPPORTED;
        }
#ifndef ANDROID_DEFAULT_CODE 
        AString value;
        bool valid = 
            (GetAttribute(params.c_str(), "octet-align", &value, true) && value == "1")
            && (!GetAttribute(params.c_str(), "crc", &value, true) || value == "0")
            && (!GetAttribute(params.c_str(), "interleaving", &value, true));
        if (!valid) {
            LOGE("[AMR capability error]Unsupported AMR audio, params %s", params.c_str());
            mInitCheck = ERROR_UNSUPPORTED;
        }
        mFormat->setInt32(kKeyMaxInputSize, kMaxInputSizeAMR);
#endif
    } else if (!strncmp(desc.c_str(), "AMR-WB/", 7)) {
        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_AMR_WB);
		
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif
        int32_t sampleRate, numChannels;
        ASessionDescription::ParseFormatDesc(
                desc.c_str(), &sampleRate, &numChannels);

#ifndef ANDROID_DEFAULT_CODE
        if (sampleRate != 16000) {
            LOGW("bad AMR clock rate %d", sampleRate);
            sampleRate = 16000;
        }
#endif
        mFormat->setInt32(kKeySampleRate, sampleRate);
        mFormat->setInt32(kKeyChannelCount, numChannels);

        if (sampleRate != 16000 || numChannels != 1) {
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("[AMR capability error]Unsupported AMR audio, sample rate %d, channels %d", sampleRate, numChannels);
#endif // #ifndef ANDROID_DEFAULT_CODE
            mInitCheck = ERROR_UNSUPPORTED;
        }
#ifndef ANDROID_DEFAULT_CODE 
        AString value;
        bool valid = 
            (GetAttribute(params.c_str(), "octet-align", &value, true) && value == "1")
            && (!GetAttribute(params.c_str(), "crc", &value, true) || value == "0")
            && (!GetAttribute(params.c_str(), "interleaving", &value, true));
        if (!valid) {
            LOGE("[AMR capability error]Unsupported AMR audio, params %s", params.c_str());
            mInitCheck = ERROR_UNSUPPORTED;
        }
        mFormat->setInt32(kKeyMaxInputSize, kMaxInputSizeAMR);
#endif
#ifndef ANDROID_DEFAULT_CODE
    } else if (!strncmp(desc.c_str(), "MP4V-ES/", 8) ||
            (!strncmp(desc.c_str(), "mpeg4-generic/", 14) && GetAttribute(params.c_str(), "streamType", &val) 
             && !strcmp(val.c_str(), "4"))) {
#else
    } else if (!strncmp(desc.c_str(), "MP4V-ES/", 8)) {
#endif
#ifndef ANDROID_DEFAULT_CODE 
        maxBitrate = kMaxBitrateMPEG4;
		
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_MPEG4);

        int32_t width, height;
        if (!sessionDesc->getDimensions(index, PT, &width, &height)) {
            width = -1;
            height = -1;
        }

#ifndef ANDROID_DEFAULT_CODE
        LOGI("width %d height %d", width, height);
#endif

        int32_t encWidth, encHeight;
        sp<ABuffer> codecSpecificData =
            MakeMPEG4VideoCodecSpecificData(
                    params.c_str(), &encWidth, &encHeight);

        if (codecSpecificData != NULL) {
            mFormat->setData(
                    kKeyESDS, 0,
                    codecSpecificData->data(), codecSpecificData->size());

            if (width < 0) {
                width = encWidth;
                height = encHeight;
            }
#ifndef ANDROID_DEFAULT_CODE 
            // don't support bad config
        } else {
#else
        } else if (width < 0) {
#endif // #ifndef ANDROID_DEFAULT_CODE
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("[MPEG4 capability error]Unsupported MPEG4 video, params %s", params.c_str());
#endif // #ifndef ANDROID_DEFAULT_CODE
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }

#ifndef ANDROID_DEFAULT_CODE 
        if (!checkVideoResolution(MEDIA_MIMETYPE_VIDEO_MPEG4,width, height)) {
            LOGE("[MPEG4 capability error]Unsupported MPEG4 video, width %d, height %d", width, height);
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setInt32(kKeyWidth, width);
        mFormat->setInt32(kKeyHeight, height);
    } else if (!strncasecmp(desc.c_str(), "mpeg4-generic/", 14)) {
        AString val;
        if (!GetAttribute(params.c_str(), "mode", &val)
                || (strcasecmp(val.c_str(), "AAC-lbr")
                    && strcasecmp(val.c_str(), "AAC-hbr"))) {
            mInitCheck = ERROR_UNSUPPORTED;
#ifndef ANDROID_DEFAULT_CODE 
            LOGE("[RTSP capability error]Unsupported mpeg4-generic params %s", params.c_str());
#endif // #ifndef ANDROID_DEFAULT_CODE
            return;
        }
		
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif
        mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_AAC);

        int32_t sampleRate, numChannels;
        ASessionDescription::ParseFormatDesc(
                desc.c_str(), &sampleRate, &numChannels);

        mFormat->setInt32(kKeySampleRate, sampleRate);
        mFormat->setInt32(kKeyChannelCount, numChannels);

        sp<ABuffer> codecSpecificData =
            MakeAACCodecSpecificData2(params.c_str());

#ifndef ANDROID_DEFAULT_CODE 
        if (codecSpecificData == NULL) {
            mInitCheck = ERROR_UNSUPPORTED;
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mFormat->setData(
                kKeyESDS, 0,
                codecSpecificData->data(), codecSpecificData->size());
    } else if (ARawAudioAssembler::Supports(desc.c_str())) {
        ARawAudioAssembler::MakeFormat(desc.c_str(), mFormat);		
#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
		m_BufQueSize = kWholeBufSize; //bytes
		m_TargetTime = kTargetTime;//ms
#endif
    } else {
#ifndef ANDROID_DEFAULT_CODE 
        LOGE("[RTSP capability error]Unsupported mime %s", desc.c_str());
#endif // #ifndef ANDROID_DEFAULT_CODE
        mInitCheck = ERROR_UNSUPPORTED;
    }
#ifndef ANDROID_DEFAULT_CODE 
    int32_t bitrate;
    if (maxBitrate > 0 && sessionDesc->getBitrate(index, &bitrate) && 
            bitrate > 2 * maxBitrate) {
        LOGE("[RTSP capability error]Unsupported bitrate %d, mime %s", bitrate, desc.c_str());
        mInitCheck = ERROR_UNSUPPORTED;
        return;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
}

APacketSource::~APacketSource() {
}

status_t APacketSource::initCheck() const {
    return mInitCheck;
}

status_t APacketSource::start(MetaData *params) {
#ifndef ANDROID_DEFAULT_CODE 
    // support pv codec
    Mutex::Autolock autoLock(mLock);

    int32_t val;
    if (params && params->findInt32(kKeyWantsNALFragments, &val)
        && val != 0) {
        mWantsNALFragments = true;
    } else {
        mWantsNALFragments = false;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    return OK;
}

status_t APacketSource::stop() {
#ifndef ANDROID_DEFAULT_CODE
    signalEOS(ERROR_END_OF_STREAM);
#endif
    return OK;
}

sp<MetaData> APacketSource::getFormat() {
    return mFormat;
}

status_t APacketSource::read(
        MediaBuffer **out, const ReadOptions *) {
    *out = NULL;

    Mutex::Autolock autoLock(mLock);
    while (mEOSResult == OK && mBuffers.empty()) {
        mCondition.wait(mLock);
    }

    if (!mBuffers.empty()) {
        const sp<ABuffer> buffer = *mBuffers.begin();

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT 
		m_uiNextAduSeqNum = buffer->int32Data();
#endif
        updateNormalPlayTime_l(buffer);

        int64_t timeUs;
        CHECK(buffer->meta()->findInt64("timeUs", &timeUs));

        MediaBuffer *mediaBuffer = new MediaBuffer(buffer);
        mediaBuffer->meta_data()->setInt64(kKeyTime, timeUs);

        *out = mediaBuffer;

        mBuffers.erase(mBuffers.begin());
        return OK;
    }

    return mEOSResult;
}

void APacketSource::updateNormalPlayTime_l(const sp<ABuffer> &buffer) {
    uint32_t rtpTime;
    CHECK(buffer->meta()->findInt32("rtp-time", (int32_t *)&rtpTime));

    mLastNormalPlayTimeUs =
#ifndef ANDROID_DEFAULT_CODE 
        // fix conversion bug
        ((double)((int32_t)(rtpTime - mRTPTimeBase)) / mClockRate)
            * 1000000ll
            + mNormalPlayTimeBaseUs;
#else
        (((double)rtpTime - (double)mRTPTimeBase) / mClockRate)
            * 1000000ll
            + mNormalPlayTimeBaseUs;
#endif // #ifndef ANDROID_DEFAULT_CODE
}

#ifndef ANDROID_DEFAULT_CODE 
static void CopyTimes(const sp<ABuffer> &to, const sp<ABuffer> &from) {
    uint64_t ntpTime;
    CHECK(from->meta()->findInt64("ntp-time", (int64_t *)&ntpTime));

    uint32_t rtpTime;
    CHECK(from->meta()->findInt32("rtp-time", (int32_t *)&rtpTime));

    to->meta()->setInt64("ntp-time", ntpTime);
    to->meta()->setInt32("rtp-time", rtpTime);

    // Copy the seq number.
    to->setInt32Data(from->int32Data());
}
#endif // #ifndef ANDROID_DEFAULT_CODE

void APacketSource::queueAccessUnit(const sp<ABuffer> &buffer) {
    int32_t damaged;
    if (buffer->meta()->findInt32("damaged", &damaged) && damaged) {
        LOGV("discarding damaged AU");
        return;
    }

#ifndef ANDROID_DEFAULT_CODE 
    {
        Mutex::Autolock autoLock(mLock);
        if (mEOSResult == ERROR_END_OF_STREAM) {
            LOGE("don't queue data after ERROR_END_OF_STREAM");
            return;
        }
    }
#endif

    if (mScanForIDR && mIsAVC) {
        // This pretty piece of code ensures that the first access unit
        // fed to the decoder after stream-start or seek is guaranteed to
        // be an IDR frame. This is to workaround limitations of a certain
        // hardware h.264 decoder that requires this to be the case.

#ifndef ANDROID_DEFAULT_CODE 
        // only check the first byte of nal fragment
        // AAVCAssembler only send nal fragment now
        if ((buffer->data()[0] & 0x1f) != 5) {
            LOGV("skipping AU while scanning for next IDR frame.");
            return;
        } 
        if (!mWantsNALFragments)
            CHECK(buffer->meta()->findInt64("timeUs", &mAccessUnitTimeUs));
#else
        if (!IsIDR(buffer)) {
            LOGV("skipping AU while scanning for next IDR frame.");
            return;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE

        mScanForIDR = false;
    }

#ifndef ANDROID_DEFAULT_CODE 
    // combine access units here for AVC
    if (mIsAVC && !mWantsNALFragments) {
        int64_t timeUs;
        CHECK(buffer->meta()->findInt64("timeUs", &timeUs));
        if (timeUs != mAccessUnitTimeUs) {
            size_t totalSize = 0;
            for (List<sp<ABuffer> >::iterator it = mNALUnits.begin();
                    it != mNALUnits.end(); ++it) {
                totalSize += 4 + (*it)->size();
            }

            sp<ABuffer> accessUnit = new ABuffer(totalSize);
            size_t offset = 0;
            for (List<sp<ABuffer> >::iterator it = mNALUnits.begin();
                    it != mNALUnits.end(); ++it) {
                memcpy(accessUnit->data() + offset, "\x00\x00\x00\x01", 4);
                offset += 4;

                sp<ABuffer> nal = *it;
                memcpy(accessUnit->data() + offset, nal->data(), nal->size());
                offset += nal->size();
            }

            CopyTimes(accessUnit, *mNALUnits.begin());
            accessUnit->meta()->setInt64("timeUs", mAccessUnitTimeUs);
            
            Mutex::Autolock autoLock(mLock);
            mBuffers.push_back(accessUnit);
            mCondition.signal();
            mNALUnits.clear();
        }
        mNALUnits.push_back(buffer);
        mAccessUnitTimeUs = timeUs;
        return;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    Mutex::Autolock autoLock(mLock);
#ifndef ANDROID_DEFAULT_CODE 
    int64_t timeUs;
    CHECK(buffer->meta()->findInt64("timeUs", &timeUs));
    if (mAccessUnitTimeUs != -1 && mAccessUnitTimeUs > timeUs) {
        LOGW("discard late access unit %lld < %lld", timeUs, mAccessUnitTimeUs);
        return;
    }
    mAccessUnitTimeUs = timeUs;
#endif // #ifndef ANDROID_DEFAULT_CODE
    mBuffers.push_back(buffer);
    mCondition.signal();
}

void APacketSource::signalEOS(status_t result) {
    CHECK(result != OK);

    Mutex::Autolock autoLock(mLock);
    mEOSResult = result;
    mCondition.signal();
}

void APacketSource::flushQueue() {
    Mutex::Autolock autoLock(mLock);
    mBuffers.clear();

    mScanForIDR = true;
#ifndef ANDROID_DEFAULT_CODE 
    // reset eos
    mEOSResult = OK;
#endif // #ifndef ANDROID_DEFAULT_CODE
}

#ifndef ANDROID_DEFAULT_CODE 
int64_t APacketSource::getNormalPlayTimeUs(uint32_t rtpTime) {
    Mutex::Autolock autoLock(mLock);
    return 
        ((double)((int32_t)(rtpTime - mRTPTimeBase)) / mClockRate)
            * 1000000ll
            + mNormalPlayTimeBaseUs;
}
#else
int64_t APacketSource::getNormalPlayTimeUs() {
    Mutex::Autolock autoLock(mLock);
    return mLastNormalPlayTimeUs;
}
#endif

#ifndef ANDROID_DEFAULT_CODE 
void APacketSource::setNormalPlayTimeUs(int64_t timeUs) {
    Mutex::Autolock autoLock(mLock);
    mLastNormalPlayTimeUs = timeUs;
    mAccessUnitTimeUs = -1;
}

bool APacketSource::isNPTMappingSet() {
    Mutex::Autolock autoLock(mLock);
    return mNPTMappingIsSet;
}

bool APacketSource::isAtEOS() {
    Mutex::Autolock autoLock(mLock);
    return mEOSResult == ERROR_END_OF_STREAM;
}
#endif // #ifndef ANDROID_DEFAULT_CODE

void APacketSource::setNormalPlayTimeMapping(
        uint32_t rtpTime, int64_t normalPlayTimeUs) {
    Mutex::Autolock autoLock(mLock);

#ifndef ANDROID_DEFAULT_CODE 
    mNPTMappingIsSet = true;
#endif // #ifndef ANDROID_DEFAULT_CODE
    mRTPTimeBase = rtpTime;
    mNormalPlayTimeBaseUs = normalPlayTimeUs;
}

int64_t APacketSource::getQueueDurationUs(bool *eos) {
    Mutex::Autolock autoLock(mLock);

    *eos = (mEOSResult != OK);

    if (mBuffers.size() < 2) {
        return 0;
    }

    const sp<ABuffer> first = *mBuffers.begin();
    const sp<ABuffer> last = *--mBuffers.end();

    int64_t firstTimeUs;
    CHECK(first->meta()->findInt64("timeUs", &firstTimeUs));

    int64_t lastTimeUs;
    CHECK(last->meta()->findInt64("timeUs", &lastTimeUs));

    if (lastTimeUs < firstTimeUs) {
        LOGE("Huh? Time moving backwards? %lld > %lld",
             firstTimeUs, lastTimeUs);

        return 0;
    }

    return lastTimeUs - firstTimeUs;
}

#ifdef MTK_RTSP_BITRATE_ADAPTATION_SUPPORT
bool APacketSource::getNSN(int32_t* uiNextSeqNum){

    Mutex::Autolock autoLock(mLock);
	if(!mBuffers.empty() && mEOSResult == OK){
		if(m_uiNextAduSeqNum!= -1){		
			*uiNextSeqNum = m_uiNextAduSeqNum;
			return true;
		}
		*uiNextSeqNum = (*mBuffers.begin())->int32Data();
		return true;
	}
	return false;
}

size_t APacketSource::getFreeBufSpace(){
	Mutex::Autolock autoLock(mLock);
	//size_t freeBufSpace = m_BufQueSize;
	size_t bufSizeUsed = 0;
	if(mBuffers.empty() || mEOSResult != OK){
		return m_BufQueSize;
	}

	List<sp<ABuffer> >::iterator it = mBuffers.begin();
	while (it != mBuffers.end()) {
		bufSizeUsed += (*it)->size();
		it++;	
	}
	if(bufSizeUsed >= m_BufQueSize)
		return 0;
	
	return 	m_BufQueSize - bufSizeUsed;	  
}
	
#endif

}  // namespace android
