/*
 * Copyright (C) 2009 The Android Open Source Project
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

#define LOG_TAG "MPEG4Extractor"
#include <utils/Log.h>

#include "include/MPEG4Extractor.h"
#include "include/SampleTable.h"
#include "include/ESDS.h"
#include "timedtext/TimedTextPlayer.h"

#include <arpa/inet.h>

#include <ctype.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaBufferGroup.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>
#include <utils/String8.h>
#ifndef ANDROID_DEFAULT_CODE
#include <sys/sysconf.h>
#include <asm/page.h>
#include <vdec_drv_if.h>
#include "val_types.h"
//#include <m4v_config_parser.h>
#include "include/avc_utils.h"

#include "include/ESDS.h"
#define QUICKTIME_SUPPORT
#endif
namespace android {
#ifndef ANDROID_DEFAULT_CODE
static int32_t VIDEO_MAX_FPS = 120;
static uint32_t VIDEO_MAX_RESOLUTION = 1920*1080;      // check too large sample size
#endif

class MPEG4Source : public MediaSource {
public:
    // Caller retains ownership of both "dataSource" and "sampleTable".
    MPEG4Source(const sp<MetaData> &format,
                const sp<DataSource> &dataSource,
                int32_t timeScale,
                const sp<SampleTable> &sampleTable);

    virtual status_t start(MetaData *params = NULL);
    virtual status_t stop();

    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options = NULL);
#ifndef ANDROID_DEFAULT_CODE
	uint64_t getStartTimeOffsetUs()
	{
		return (uint64_t)mSampleTable->getStartTimeOffset()*mTimescale*1000000;
	}
#endif

protected:
    virtual ~MPEG4Source();

private:
    Mutex mLock;

    sp<MetaData> mFormat;
    sp<DataSource> mDataSource;
    int32_t mTimescale;
    sp<SampleTable> mSampleTable;
    uint32_t mCurrentSampleIndex;

    bool mIsAVC;
    size_t mNALLengthSize;

    bool mStarted;

    MediaBufferGroup *mGroup;

    MediaBuffer *mBuffer;

    bool mWantsNALFragments;

    uint8_t *mSrcBuffer;

    size_t parseNALSize(const uint8_t *data) const;

    MPEG4Source(const MPEG4Source &);
    MPEG4Source &operator=(const MPEG4Source &);
};

// This custom data source wraps an existing one and satisfies requests
// falling entirely within a cached range from the cache while forwarding
// all remaining requests to the wrapped datasource.
// This is used to cache the full sampletable metadata for a single track,
// possibly wrapping multiple times to cover all tracks, i.e.
// Each MPEG4DataSource caches the sampletable metadata for a single track.

struct MPEG4DataSource : public DataSource {
    MPEG4DataSource(const sp<DataSource> &source);

    virtual status_t initCheck() const;
    virtual ssize_t readAt(off64_t offset, void *data, size_t size);
    virtual status_t getSize(off64_t *size);
    virtual uint32_t flags();

    status_t setCachedRange(off64_t offset, size_t size);

protected:
    virtual ~MPEG4DataSource();

private:
    Mutex mLock;

    sp<DataSource> mSource;
    off64_t mCachedOffset;
    size_t mCachedSize;
    uint8_t *mCache;

    void clearCache();

    MPEG4DataSource(const MPEG4DataSource &);
    MPEG4DataSource &operator=(const MPEG4DataSource &);
};

MPEG4DataSource::MPEG4DataSource(const sp<DataSource> &source)
    : mSource(source),
      mCachedOffset(0),
      mCachedSize(0),
      mCache(NULL) {
}

MPEG4DataSource::~MPEG4DataSource() {
    clearCache();
}

void MPEG4DataSource::clearCache() {
    if (mCache) {
        free(mCache);
        mCache = NULL;
    }

    mCachedOffset = 0;
    mCachedSize = 0;
}

status_t MPEG4DataSource::initCheck() const {
    return mSource->initCheck();
}

ssize_t MPEG4DataSource::readAt(off64_t offset, void *data, size_t size) {
    Mutex::Autolock autoLock(mLock);

    if (offset >= mCachedOffset
            && offset + size <= mCachedOffset + mCachedSize) {
        memcpy(data, &mCache[offset - mCachedOffset], size);
        return size;
    }

    return mSource->readAt(offset, data, size);
}

status_t MPEG4DataSource::getSize(off64_t *size) {
    return mSource->getSize(size);
}

uint32_t MPEG4DataSource::flags() {
    return mSource->flags();
}

status_t MPEG4DataSource::setCachedRange(off64_t offset, size_t size) {
    Mutex::Autolock autoLock(mLock);

    clearCache();

    mCache = (uint8_t *)malloc(size);

    if (mCache == NULL) {
        return -ENOMEM;
    }

    mCachedOffset = offset;
    mCachedSize = size;

    ssize_t err = mSource->readAt(mCachedOffset, mCache, mCachedSize);

    if (err < (ssize_t)size) {
        clearCache();

        return ERROR_IO;
    }

    return OK;
}

////////////////////////////////////////////////////////////////////////////////

static void hexdump(const void *_data, size_t size) {
    const uint8_t *data = (const uint8_t *)_data;
    size_t offset = 0;
    while (offset < size) {
        printf("0x%04x  ", offset);

        size_t n = size - offset;
        if (n > 16) {
            n = 16;
        }

        for (size_t i = 0; i < 16; ++i) {
            if (i == 8) {
                printf(" ");
            }

            if (offset + i < size) {
                printf("%02x ", data[offset + i]);
            } else {
                printf("   ");
            }
        }

        printf(" ");

        for (size_t i = 0; i < n; ++i) {
            if (isprint(data[offset + i])) {
                printf("%c", data[offset + i]);
            } else {
                printf(".");
            }
        }

        printf("\n");

        offset += 16;
    }
}

static const char *FourCC2MIME(uint32_t fourcc) {
    switch (fourcc) {
        case FOURCC('m', 'p', '4', 'a'):
            return MEDIA_MIMETYPE_AUDIO_AAC;

        case FOURCC('s', 'a', 'm', 'r'):
            return MEDIA_MIMETYPE_AUDIO_AMR_NB;

        case FOURCC('s', 'a', 'w', 'b'):
            return MEDIA_MIMETYPE_AUDIO_AMR_WB;

        case FOURCC('m', 'p', '4', 'v'):
            return MEDIA_MIMETYPE_VIDEO_MPEG4;

        case FOURCC('s', '2', '6', '3'):
        case FOURCC('h', '2', '6', '3'):
        case FOURCC('H', '2', '6', '3'):
            return MEDIA_MIMETYPE_VIDEO_H263;

        case FOURCC('a', 'v', 'c', '1'):
            return MEDIA_MIMETYPE_VIDEO_AVC;
#ifndef ANDROID_DEFAULT_CODE
		case FOURCC('.', 'm', 'p', '3'):
			return MEDIA_MIMETYPE_AUDIO_MPEG;
#endif

        default:
            CHECK(!"should not be here.");
            return NULL;
    }
}

MPEG4Extractor::MPEG4Extractor(const sp<DataSource> &source)
    : mDataSource(source),
      mInitCheck(NO_INIT),
      mHasVideo(false),
#ifndef ANDROID_DEFAULT_CODE
      mHasAudio(false),
#endif
      mFirstTrack(NULL),
      mLastTrack(NULL),
      mFileMetaData(new MetaData),
      mFirstSINF(NULL),
      mIsDrm(false) {
#ifndef ANDROID_DEFAULT_CODE
	LOGD("=====================================\n"); 
    LOGD("[MP4 Playback capability info]£º\n"); 
    LOGD("=====================================\n"); 
    LOGD("Resolution = \"[(8,8) ~ (864£¬480)]\" \n"); 
    LOGD("Support Codec = \"Video:MPEG4, H263, H264 ; Audio: AAC, AMR-NB/WB\" \n"); 
    LOGD("Profile_Level = \"MPEG4: Simple Profile ; H263: Baseline ; H264: Baseline/3.1, Main/3.1\" \n"); 
    LOGD("Max frameRate =  120fps \n"); 
    LOGD("Max Bitrate  = H264: 6Mbps  (720*480@30fps) ; MPEG4/H263: 20Mbps (864*480@30fps)\n"); 
    LOGD("=====================================\n"); 
#endif
}

MPEG4Extractor::~MPEG4Extractor() {
    Track *track = mFirstTrack;
    while (track) {
        Track *next = track->next;

        delete track;
        track = next;
    }
    mFirstTrack = mLastTrack = NULL;

    SINF *sinf = mFirstSINF;
    while (sinf) {
        SINF *next = sinf->next;
        delete sinf->IPMPData;
        delete sinf;
        sinf = next;
    }
    mFirstSINF = NULL;
}

sp<MetaData> MPEG4Extractor::getMetaData() {
    status_t err;
    if ((err = readMetaData()) != OK) {
        return new MetaData;
    }
#ifndef ANDROID_DEFAULT_CODE
    // set flag for handle the case: video too long to audio
    mFileMetaData->setInt32(kKeyVideoPreCheck, 1);
#endif
    return mFileMetaData;
}

size_t MPEG4Extractor::countTracks() {
    status_t err;
    if ((err = readMetaData()) != OK) {
        return 0;
    }

    size_t n = 0;
#ifndef ANDROID_DEFAULT_CODE
		size_t timeOffsetTrackNum = 0;
		Track *timeOffsetTrack1 = NULL;
		Track *timeOffsetTrack2 = NULL;
#endif
    Track *track = mFirstTrack;
    while (track) {
        ++n;
#ifndef ANDROID_DEFAULT_CODE
				if (track->mStartTimeOffset != 0)
				{
					timeOffsetTrackNum++;
					if (timeOffsetTrackNum > 2)
					{
						LOGW("Unsupport edts list, %d tracks have time offset!!", timeOffsetTrackNum);
						track->mStartTimeOffset = 0;
						timeOffsetTrack1->mStartTimeOffset = 0;
						timeOffsetTrack2->mStartTimeOffset = 0;
					}
					else
					{
						if (timeOffsetTrack1 == NULL)
							timeOffsetTrack1 = track;
						else
						{
							timeOffsetTrack2 = track;
							if (timeOffsetTrack1->mStartTimeOffset > track->mStartTimeOffset)
							{
								timeOffsetTrack1->mStartTimeOffset -= track->mStartTimeOffset;
								track->mStartTimeOffset = 0;
							}
							else
							{
								track->mStartTimeOffset -= timeOffsetTrack1->mStartTimeOffset;
								timeOffsetTrack1->mStartTimeOffset = 0;
							}
						}
					}
				}
#endif
        track = track->next;
    }

    return n;
}

sp<MetaData> MPEG4Extractor::getTrackMetaData(
        size_t index, uint32_t flags) {
    status_t err;
    if ((err = readMetaData()) != OK) {
        return NULL;
    }

    Track *track = mFirstTrack;
    while (index > 0) {
        if (track == NULL) {
            return NULL;
        }

        track = track->next;
        --index;
    }

    if (track == NULL) {
        return NULL;
    }

    if ((flags & kIncludeExtensiveMetaData)
            && !track->includes_expensive_metadata) {
        track->includes_expensive_metadata = true;

        const char *mime;
        CHECK(track->meta->findCString(kKeyMIMEType, &mime));
        if (!strncasecmp("video/", mime, 6)) {
            uint32_t sampleIndex;
#ifndef ANDROID_DEFAULT_CODE
            uint64_t sampleTime;
#else
            uint32_t sampleTime;
#endif
            if (track->sampleTable->findThumbnailSample(&sampleIndex) == OK
                    && track->sampleTable->getMetaDataForSample(
                        sampleIndex, NULL /* offset */, NULL /* size */,
                        &sampleTime) == OK) {
#ifndef ANDROID_DEFAULT_CODE//hai.li for Issue: ALPS32414
								if (mMovieTimeScale != 0)
									track->sampleTable->setStartTimeOffset((uint64_t)(track->mStartTimeOffset/mMovieTimeScale)*track->timescale);
								track->meta->setInt64(
										kKeyThumbnailTime,
										((int64_t)sampleTime * 1000000 + (track->timescale >> 1)) / track->timescale 
										+ ((int64_t)track->sampleTable->getStartTimeOffset())*1000000/track->timescale);
#else
                track->meta->setInt64(
                        kKeyThumbnailTime,
                        ((int64_t)sampleTime * 1000000) / track->timescale);
#endif
            }
        }
    }
#ifndef ANDROID_DEFAULT_CODE
	if (flags & kIncludeInterleaveInfo)
	{
		off64_t offset;
		track->sampleTable->getMetaDataForSample(0, &offset, NULL,(uint64_t*)NULL, NULL);
		track->meta->setInt64(kKeyFirstSampleOffset, offset);
		LOGD("First sample offset in %s track is %lld", track->mIsVideo?"Video":"Audio", (int64_t)offset);
	}
#endif

    return track->meta;
}

status_t MPEG4Extractor::readMetaData() {
    if (mInitCheck != NO_INIT) {
        return mInitCheck;
    }

    off64_t offset = 0;
    status_t err;
    while ((err = parseChunk(&offset, 0)) == OK) {
    }

    if (mInitCheck == OK) {
#ifndef ANDROID_DEFAULT_CODE//hai.li
		if (mHasAudio && !mHasVideo) {
			int32_t isOtherBrand = 0;
			if (mFileMetaData->findInt32(kKeyIs3gpBrand, &isOtherBrand) && isOtherBrand)
			{				
				LOGD("File Type is audio/3gpp");
				mFileMetaData->setCString(kKeyMIMEType, "audio/3gpp");
			}
#ifdef QUICKTIME_SUPPORT
			else if (mFileMetaData->findInt32(kKeyIsQTBrand, &isOtherBrand) && isOtherBrand)
			{				
				LOGD("File Type is audio/quicktime");
				mFileMetaData->setCString(kKeyMIMEType, "audio/quicktime");
			}
#endif
			else
			{
				LOGD("File Type is audio/mp4");
				mFileMetaData->setCString(kKeyMIMEType, "audio/mp4");
			}
		} else {
			int32_t isOtherBrand = 0;
			if (mHasVideo && mFileMetaData->findInt32(kKeyIs3gpBrand, &isOtherBrand) && isOtherBrand)
			{				
				LOGD("File Type is video/3gpp");
				mFileMetaData->setCString(kKeyMIMEType, "video/3gpp");
			}
#ifdef QUICKTIME_SUPPORT
			else if (mFileMetaData->findInt32(kKeyIsQTBrand, &isOtherBrand) && isOtherBrand)
			{				
				LOGD("File Type is video/quicktime");
				mFileMetaData->setCString(kKeyMIMEType, "video/quicktime");
			}
#endif
			else
			{
				LOGD("File Type is video/mp4");
				mFileMetaData->setCString(kKeyMIMEType, "video/mp4");
			}
		}

#else
        if (mHasVideo) {
            mFileMetaData->setCString(kKeyMIMEType, "video/mp4");
        } else {
            mFileMetaData->setCString(kKeyMIMEType, "audio/mp4");
        }
#endif
        mInitCheck = OK;
    } else {
        mInitCheck = err;
    }

    CHECK_NE(err, (status_t)NO_INIT);
    return mInitCheck;
}

char* MPEG4Extractor::getDrmTrackInfo(size_t trackID, int *len) {
    if (mFirstSINF == NULL) {
        return NULL;
    }

    SINF *sinf = mFirstSINF;
    while (sinf && (trackID != sinf->trackID)) {
        sinf = sinf->next;
    }

    if (sinf == NULL) {
        return NULL;
    }

    *len = sinf->len;
    return sinf->IPMPData;
}

// Reads an encoded integer 7 bits at a time until it encounters the high bit clear.
int32_t readSize(off64_t offset,
        const sp<DataSource> DataSource, uint8_t *numOfBytes) {
    uint32_t size = 0;
    uint8_t data;
    bool moreData = true;
    *numOfBytes = 0;

    while (moreData) {
        if (DataSource->readAt(offset, &data, 1) < 1) {
            return -1;
        }
        offset ++;
        moreData = (data >= 128) ? true : false;
        size = (size << 7) | (data & 0x7f); // Take last 7 bits
        (*numOfBytes) ++;
    }

    return size;
}

status_t MPEG4Extractor::parseDrmSINF(off64_t *offset, off64_t data_offset) {
    uint8_t updateIdTag;
    if (mDataSource->readAt(data_offset, &updateIdTag, 1) < 1) {
        return ERROR_IO;
    }
    data_offset ++;

    if (0x01/*OBJECT_DESCRIPTOR_UPDATE_ID_TAG*/ != updateIdTag) {
        return ERROR_MALFORMED;
    }

    uint8_t numOfBytes;
    int32_t size = readSize(data_offset, mDataSource, &numOfBytes);
    if (size < 0) {
        return ERROR_IO;
    }
    int32_t classSize = size;
    data_offset += numOfBytes;

    while(size >= 11 ) {
        uint8_t descriptorTag;
        if (mDataSource->readAt(data_offset, &descriptorTag, 1) < 1) {
            return ERROR_IO;
        }
        data_offset ++;

        if (0x11/*OBJECT_DESCRIPTOR_ID_TAG*/ != descriptorTag) {
            return ERROR_MALFORMED;
        }

        uint8_t buffer[8];
        //ObjectDescriptorID and ObjectDescriptor url flag
        if (mDataSource->readAt(data_offset, buffer, 2) < 2) {
            return ERROR_IO;
        }
        data_offset += 2;

        if ((buffer[1] >> 5) & 0x0001) { //url flag is set
            return ERROR_MALFORMED;
        }

        if (mDataSource->readAt(data_offset, buffer, 8) < 8) {
            return ERROR_IO;
        }
        data_offset += 8;

        if ((0x0F/*ES_ID_REF_TAG*/ != buffer[1])
                || ( 0x0A/*IPMP_DESCRIPTOR_POINTER_ID_TAG*/ != buffer[5])) {
            return ERROR_MALFORMED;
        }

        SINF *sinf = new SINF;
        sinf->trackID = U16_AT(&buffer[3]);
        sinf->IPMPDescriptorID = buffer[7];
        sinf->next = mFirstSINF;
        mFirstSINF = sinf;

        size -= (8 + 2 + 1);
    }

    if (size != 0) {
        return ERROR_MALFORMED;
    }

    if (mDataSource->readAt(data_offset, &updateIdTag, 1) < 1) {
        return ERROR_IO;
    }
    data_offset ++;

    if(0x05/*IPMP_DESCRIPTOR_UPDATE_ID_TAG*/ != updateIdTag) {
        return ERROR_MALFORMED;
    }

    size = readSize(data_offset, mDataSource, &numOfBytes);
    if (size < 0) {
        return ERROR_IO;
    }
    classSize = size;
    data_offset += numOfBytes;

    while (size > 0) {
        uint8_t tag;
        int32_t dataLen;
        if (mDataSource->readAt(data_offset, &tag, 1) < 1) {
            return ERROR_IO;
        }
        data_offset ++;

        if (0x0B/*IPMP_DESCRIPTOR_ID_TAG*/ == tag) {
            uint8_t id;
            dataLen = readSize(data_offset, mDataSource, &numOfBytes);
            if (dataLen < 0) {
                return ERROR_IO;
            } else if (dataLen < 4) {
                return ERROR_MALFORMED;
            }
            data_offset += numOfBytes;

            if (mDataSource->readAt(data_offset, &id, 1) < 1) {
                return ERROR_IO;
            }
            data_offset ++;

            SINF *sinf = mFirstSINF;
            while (sinf && (sinf->IPMPDescriptorID != id)) {
                sinf = sinf->next;
            }
            if (sinf == NULL) {
                return ERROR_MALFORMED;
            }
            sinf->len = dataLen - 3;
            sinf->IPMPData = new char[sinf->len];

            if (mDataSource->readAt(data_offset + 2, sinf->IPMPData, sinf->len) < sinf->len) {
                return ERROR_IO;
            }
            data_offset += sinf->len;

            size -= (dataLen + numOfBytes + 1);
        }
    }

    if (size != 0) {
        return ERROR_MALFORMED;
    }

    return UNKNOWN_ERROR;  // Return a dummy error.
}

static void MakeFourCCString(uint32_t x, char *s) {
    s[0] = x >> 24;
    s[1] = (x >> 16) & 0xff;
    s[2] = (x >> 8) & 0xff;
    s[3] = x & 0xff;
    s[4] = '\0';
}

struct PathAdder {
    PathAdder(Vector<uint32_t> *path, uint32_t chunkType)
        : mPath(path) {
        mPath->push(chunkType);
    }

    ~PathAdder() {
        mPath->pop();
    }

private:
    Vector<uint32_t> *mPath;

    PathAdder(const PathAdder &);
    PathAdder &operator=(const PathAdder &);
};

static bool underMetaDataPath(const Vector<uint32_t> &path) {
    return path.size() >= 5
        && path[0] == FOURCC('m', 'o', 'o', 'v')
        && path[1] == FOURCC('u', 'd', 't', 'a')
        && path[2] == FOURCC('m', 'e', 't', 'a')
        && path[3] == FOURCC('i', 'l', 's', 't');
}

// Given a time in seconds since Jan 1 1904, produce a human-readable string.
static void convertTimeToDate(int64_t time_1904, String8 *s) {
    time_t time_1970 = time_1904 - (((66 * 365 + 17) * 24) * 3600);

    char tmp[32];
    strftime(tmp, sizeof(tmp), "%Y%m%dT%H%M%S.000Z", gmtime(&time_1970));

    s->setTo(tmp);
}

status_t MPEG4Extractor::parseChunk(off64_t *offset, int depth) {
    uint32_t hdr[2];
    if (mDataSource->readAt(*offset, hdr, 8) < 8) {
        return ERROR_IO;
    }
    uint64_t chunk_size = ntohl(hdr[0]);
    uint32_t chunk_type = ntohl(hdr[1]);
    off64_t data_offset = *offset + 8;

    if (chunk_size == 1) {
        if (mDataSource->readAt(*offset + 8, &chunk_size, 8) < 8) {
            return ERROR_IO;
        }
        chunk_size = ntoh64(chunk_size);
        data_offset += 8;

        if (chunk_size < 16) {
            // The smallest valid chunk is 16 bytes long in this case.
            return ERROR_MALFORMED;
        }
    } else if (chunk_size < 8) {
        // The smallest valid chunk is 8 bytes long.
        return ERROR_MALFORMED;
    }

    char chunk[5];
    MakeFourCCString(chunk_type, chunk);

#if 0
    static const char kWhitespace[] = "                                        ";
    const char *indent = &kWhitespace[sizeof(kWhitespace) - 1 - 2 * depth];
    printf("%sfound chunk '%s' of size %lld\n", indent, chunk, chunk_size);

    char buffer[256];
    size_t n = chunk_size;
    if (n > sizeof(buffer)) {
        n = sizeof(buffer);
    }
    if (mDataSource->readAt(*offset, buffer, n)
            < (ssize_t)n) {
        return ERROR_IO;
    }

    hexdump(buffer, n);
#endif

    PathAdder autoAdder(&mPath, chunk_type);

    off64_t chunk_data_size = *offset + chunk_size - data_offset;

    if (chunk_type != FOURCC('c', 'p', 'r', 't')
            && chunk_type != FOURCC('c', 'o', 'v', 'r')
            && mPath.size() == 5 && underMetaDataPath(mPath)) {
        off64_t stop_offset = *offset + chunk_size;
        *offset = data_offset;
        while (*offset < stop_offset) {
            status_t err = parseChunk(offset, depth + 1);
            if (err != OK) {
                return err;
            }
        }

        if (*offset != stop_offset) {
            return ERROR_MALFORMED;
        }

        return OK;
    }

    switch(chunk_type) {
#ifndef ANDROID_DEFAULT_CODE
		case FOURCC('f', 't', 'y', 'p'):
		{
			uint8_t header[4];
            if (mDataSource->readAt(
                        data_offset, header, 4)
                    < 4) {
                return ERROR_IO;
            }
			//LOGD("HEADER=%x,%x,%x,%x", header[0], header[1], header[2], header[3]);
			if (!memcmp(header, "3gp", 3))
			{
				LOGD("3GPP is true");
				mFileMetaData->setInt32(kKeyIs3gpBrand, true);
			}
#ifdef QUICKTIME_SUPPORT
			else if (!memcmp(header, "qt", 2))
			{
				mFileMetaData->setInt32(kKeyIsQTBrand, true);
			}
#endif
			
			
            *offset += chunk_size;
            break;
		}
#endif
        case FOURCC('m', 'o', 'o', 'v'):
        case FOURCC('t', 'r', 'a', 'k'):
        case FOURCC('m', 'd', 'i', 'a'):
        case FOURCC('m', 'i', 'n', 'f'):
        case FOURCC('d', 'i', 'n', 'f'):
        case FOURCC('s', 't', 'b', 'l'):
        case FOURCC('m', 'v', 'e', 'x'):
        case FOURCC('m', 'o', 'o', 'f'):
        case FOURCC('t', 'r', 'a', 'f'):
        case FOURCC('m', 'f', 'r', 'a'):
        case FOURCC('u', 'd', 't', 'a'):
        case FOURCC('i', 'l', 's', 't'):
#ifndef ANDROID_DEFAULT_CODE
		case FOURCC('e', 'd', 't', 's'): //added by hai.li to support track time offset
#ifdef QUICKTIME_SUPPORT
		case FOURCC('w', 'a', 'v', 'e'): //for .mov
#endif
#endif
        {
            if (chunk_type == FOURCC('s', 't', 'b', 'l')) {
                LOGV("sampleTable chunk is %d bytes long.", (size_t)chunk_size);

                if (mDataSource->flags()
                        & (DataSource::kWantsPrefetching
                            | DataSource::kIsCachingDataSource)) {
                    sp<MPEG4DataSource> cachedSource =
                        new MPEG4DataSource(mDataSource);

                    if (cachedSource->setCachedRange(*offset, chunk_size) == OK) {
                        mDataSource = cachedSource;
                    }
                }

                mLastTrack->sampleTable = new SampleTable(mDataSource);
            }

            bool isTrack = false;
            if (chunk_type == FOURCC('t', 'r', 'a', 'k')) {
                isTrack = true;

                Track *track = new Track;
                track->next = NULL;
                if (mLastTrack) {
                    mLastTrack->next = track;
                } else {
                    mFirstTrack = track;
                }
                mLastTrack = track;

                track->meta = new MetaData;
                track->includes_expensive_metadata = false;
                track->skipTrack = false;
                track->timescale = 0;
                track->meta->setCString(kKeyMIMEType, "application/octet-stream");
            }

            off64_t stop_offset = *offset + chunk_size;
            *offset = data_offset;
            while (*offset < stop_offset) {
#if !defined(ANDROID_DEFAULT_CODE) && defined(QUICKTIME_SUPPORT)//for .mov file
				if (stop_offset - *offset == 4
						&& chunk_type == FOURCC('u', 'd', 't', 'a')) {
					uint32_t terminate_code;
					mDataSource->readAt(*offset, &terminate_code, 4);
					if (0 == terminate_code)
					{
						*offset += 4;//terminate code 0x00000000
						LOGD("Terminal code for 0x%8.8x", chunk_type);
					}
				}
				else {
					status_t err = parseChunk(offset, depth + 1);
					if (err != OK) {
						return err;
					}
				}
#else
                status_t err = parseChunk(offset, depth + 1);
                if (err != OK) {
                    return err;
                }
#endif
            }

            if (*offset != stop_offset) {
                return ERROR_MALFORMED;
            }

            if (isTrack) {
#ifndef ANDROID_DEFAULT_CODE//hai.li
				if (mLastTrack->durationUs == 0)
				{
					LOGE("%s track duration is 0", mLastTrack->mIsVideo?"Video": "Audio");
					mLastTrack->skipTrack = true;
					//return UNKNOWN_ERROR;
				}
				if(mLastTrack->sampleCount ==0)
				{
					LOGE("%s track sampleCount is 0", mLastTrack->mIsVideo?"Video": "Audio");
					mLastTrack->skipTrack = true;
				}
				if (mLastTrack->mIsVideo)
				{
					if (mLastTrack->mMaxSize > VIDEO_MAX_RESOLUTION*3/2)
					{
						mLastTrack->skipTrack = true;
						LOGE("ERROR: Sample size is wrong!maxSize:%d,VIDEO_MAX_RESOLUTION:%d",
                                                       mLastTrack->mMaxSize, VIDEO_MAX_RESOLUTION);
					}
					//CHECK(mLastTrack->durationUs != 0);
					if (mLastTrack->durationUs != 0)
					{
						int64_t frame_rate = mLastTrack->sampleCount * 1000000LL / mLastTrack->durationUs;
						const char* mime;
						if ((frame_rate > VIDEO_MAX_FPS) && (mLastTrack->sampleCount > 1))
						{
							LOGE("[MP4 capability error]Unsupport video frame rate!!!fps = %lld", frame_rate);
							mLastTrack->skipTrack = true;
							mHasVideo = false;
						}
						else if (mLastTrack->meta->findCString(kKeyMIMEType, &mime) &&
								!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_AVC))
						{
#ifdef MTK_S3D_SUPPORT
							if (mLastTrack->skipTrack != true && mHasVideo) {
								LOGD("Parse sei");
								size_t size;
								size_t offset;
								void *data;
								if (OK == getFirstNal(mLastTrack, &offset, &size)) {
									data = malloc(size);
									if (NULL == data)
										return UNKNOWN_ERROR;
									if (mDataSource->readAt(
												offset, data, size)
											< size) {
										LOGE("read first nal fail!!");
										return ERROR_IO;
									}

									struct SEIInfo sei;
									video_stereo_mode mode;
									sei.payload_type = 45;//3d info
									sei.pvalue = (void*)&mode;
									if (OK == ParseSpecificSEI((uint8_t*)data, size, &sei)) {
										LOGD("Video stereo mode=%d", mode);
										mLastTrack->meta->setInt32(kKeyVideoStereoMode, mode);
									}
								}
								
							}
#endif
						}
						else if (mLastTrack->meta->findCString(kKeyMIMEType, &mime) &&
								!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_MPEG4))
						{
							int32_t isCodecInfoInFirstFrame;
							if (mLastTrack->meta->findInt32(kKeyCodecInfoIsInFirstFrame, &isCodecInfoInFirstFrame)
								&& (isCodecInfoInFirstFrame != 0))
							{
								status_t err = setCodecInfoFromFirstFrame(mLastTrack);
								if (err != OK) {
									LOGE("setCodecInfoFromFirstFrame error %d", err);
									return err;
								}
							}
						}
					
					}
					if (mLastTrack->skipTrack)
					{
						mFileMetaData->setInt32(kKeyHasUnsupportVideo, true);
						LOGD("MP4 has unsupport video track");
					}
				}
		 	// <--- Morris Yang check audio
				else if (mLastTrack->mIsAudio){
					const char *mime;
				    if (mLastTrack->meta->findCString(kKeyMIMEType, &mime) &&
						!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_MPEG))
					{
					    int32_t isCodecInfoInFirstFrame;
					    if (mLastTrack->meta->findInt32(kKeyCodecInfoIsInFirstFrame, &isCodecInfoInFirstFrame)
						    && (isCodecInfoInFirstFrame != 0))
					    {
						status_t err = setCodecInfoFromFirstFrame(mLastTrack);
						if (err != OK) {
						    LOGE("setCodecInfoFromFirstFrame error %d", err);
						    return err;
						}
					    }
					}
					int32_t num_channels;
					if (mLastTrack->meta->findInt32(kKeyChannelCount, &num_channels) && (num_channels > 2)) {
						LOGE ("[MP4 capability error]Unsupported num of channels: (%d), ignore audio track", num_channels);
						mLastTrack->skipTrack = true;
						mHasAudio = false;//hai.li
					}

					int32_t aacObjectType;
					if (mLastTrack->meta->findCString(kKeyMIMEType, &mime)) {
					    if ((!strcasecmp(MEDIA_MIMETYPE_AUDIO_AAC, mime)) && mLastTrack->meta->findInt32(kKeyAacObjType, &aacObjectType)) {

						if ((aacObjectType != 2)    // AAC LC (Low Complexity) 
						  && (aacObjectType != 4)      // AAC LTP (Long Term Prediction)
						  && (aacObjectType != 5)      // SBR (Spectral Band Replication) 
					         && (aacObjectType != 29))   // PS (Parametric Stereo)          
						{
							LOGE ("[AAC capability error]Unsupported audio object type: (%d), , ignore audio track", aacObjectType);
							mLastTrack->skipTrack = true;
							mHasAudio = false;//hai.li
						}
					    }
					}
					if (mLastTrack->skipTrack)
					{
						mHasAudio = false;
					}
				}
				else {//not video or audio track
					mLastTrack->skipTrack = true;
				}
			// --->
#endif//#ifndef ANDROID_DEFAULT_CODE
                if (mLastTrack->skipTrack) {
                    Track *cur = mFirstTrack;

                    if (cur == mLastTrack) {
                        delete cur;
                        mFirstTrack = mLastTrack = NULL;
                    } else {
                        while (cur && cur->next != mLastTrack) {
                            cur = cur->next;
                        }
                        cur->next = NULL;
                        delete mLastTrack;
                        mLastTrack = cur;
                    }

                    return OK;
                }

                status_t err = verifyTrack(mLastTrack);

                if (err != OK) {
                    return err;
                }
            } else if (chunk_type == FOURCC('m', 'o', 'o', 'v')) {
                mInitCheck = OK;

                if (!mIsDrm) {
                    return UNKNOWN_ERROR;  // Return a dummy error.
                } else {
                    return OK;
                }
            }
            break;
        }

        case FOURCC('t', 'k', 'h', 'd'):
        {
            status_t err;
            if ((err = parseTrackHeader(data_offset, chunk_data_size)) != OK) {
                return err;
            }

            *offset += chunk_size;
            break;
        }

#ifndef ANDROID_DEFAULT_CODE
		case FOURCC('e', 'l', 's', 't')://added by hai.li to support track time offset
		{
			if (chunk_data_size < 4) {
				LOGE("ERROR_MALFORMED, LINE=%d", __LINE__);
                return ERROR_MALFORMED;
            }
			uint8_t header[8];
			uint8_t version;
			uint32_t entry_count;
            if (mDataSource->readAt(
                        data_offset, header, sizeof(header))
                    < (ssize_t)sizeof(header)) {
                return ERROR_IO;
            }
			version = header[0];
			entry_count = U32_AT(&header[4]);

			//LOGE("header high=%d, header low=%d, entry_count=%d", *((uint32_t*)header), *((uint32_t*)(header+4)), entry_count);
			if (entry_count > 2)
			{
				LOGW("Unsupported edit list, entry_count=%d > 2", entry_count);//The second entry is assumed as the duration of the track and normal play
				entry_count = 2;
			}
			else if (entry_count == 2)
				LOGW("edit list entry_count=2, Assume the second entry is the duration of the track and normal play");

			mLastTrack->mElstEntryCount = entry_count;
			mLastTrack->mElstEntries = new Track::ElstEntry[entry_count];
			if (version == 1) {
				for (uint32_t i = 0; i < entry_count; i++)
				{
					uint8_t buffer[20];
					if (mDataSource->readAt(
							data_offset+4+4+i*20, buffer, sizeof(buffer))
							< (ssize_t)sizeof(buffer)){
						return ERROR_IO;
					}
					mLastTrack->mElstEntries[i].SegDuration = U64_AT(buffer);
					mLastTrack->mElstEntries[i].MediaTime = (int64_t)U64_AT(&buffer[8]);
					mLastTrack->mElstEntries[i].MediaRateInt = (int16_t)U16_AT(&buffer[16]);
					mLastTrack->mElstEntries[i].MediaRateFrac = (int16_t)U16_AT(&buffer[18]);
				}
			} else {
				for (uint32_t i = 0; i < entry_count; i++)
				{
					uint8_t buffer[12];
					if (mDataSource->readAt(
							data_offset+4+4+i*12, buffer, sizeof(buffer))
							< (ssize_t)sizeof(buffer)){
						return ERROR_IO;
					}
					mLastTrack->mElstEntries[i].SegDuration = U32_AT(buffer);
					if (0xffffffff == U32_AT(&buffer[4]))
						mLastTrack->mElstEntries[i].MediaTime = -1LL;
					else
						mLastTrack->mElstEntries[i].MediaTime = (int64_t)U32_AT(&buffer[4]);
					LOGD("MediaTime=%d, entry.mediatime=%lld, i=%d", U32_AT(&buffer[4]), mLastTrack->mElstEntries[i].MediaTime, i);
					LOGD("SegDuration=%d, entry.SegDuration=%lld, i=%d", U32_AT(&buffer[0]), mLastTrack->mElstEntries[i].SegDuration, i);
					mLastTrack->mElstEntries[i].MediaRateInt = (int16_t)U16_AT(&buffer[8]);
					mLastTrack->mElstEntries[i].MediaRateFrac = (int16_t)U16_AT(&buffer[10]);
				}
			}
			if ((mLastTrack->mElstEntries[0].MediaRateInt != 1) ||
					(mLastTrack->mElstEntries[0].MediaRateFrac != 0)){
					LOGW("Unsupported edit list, MediaRate=%d.%d != 1", 
							mLastTrack->mElstEntries[0].MediaRateInt, mLastTrack->mElstEntries[0].MediaRateFrac);
			}
			 
			if (mLastTrack->mElstEntries[0].SegDuration > (uint64_t)((1LL>>32)-1))
			{
				LOGW("Unsupported edit list, TimeOffset=%lld", mLastTrack->mElstEntries[0].SegDuration);
				mLastTrack->mStartTimeOffset = 0;
			}
			else if (mLastTrack->mElstEntries[0].MediaTime != -1){//added by vend_am00032
				mLastTrack->mStartTimeOffset = 0;
				LOGW("Unsupported edit list, MediaTime=%lld", mLastTrack->mElstEntries[0].MediaTime);
			}
			else {
				mLastTrack->mStartTimeOffset = (uint32_t)mLastTrack->mElstEntries[0].SegDuration;
			}
			*offset += chunk_size;
			break;			
		}
#ifdef MTK_S3D_SUPPORT
		case FOURCC('c', 'p', 'r', 't'):
		{
			if (mPath.size() >= 3
				&& mPath[mPath.size() - 2] == FOURCC('u', 'd', 't', 'a')
				&& mPath[mPath.size() - 3] == FOURCC('t', 'r', 'a', 'k')) {
				if (chunk_data_size >= 29) {
					uint8_t buffer[29];
					if (mDataSource->readAt(data_offset, buffer, 29) < 29) {
						return ERROR_IO;
					}
					const char* mtk_3d_tag = "MTK-3d-video-mode:";
					if (!memcmp(mtk_3d_tag, buffer+6, 19)) {
						int32_t mtk_3d_mode = U32_AT(&buffer[25]);
						LOGD("mtk 3d mode = %d", mtk_3d_mode);
						mLastTrack->meta->setInt32(kKeyVideoStereoMode, mtk_3d_mode);
					}
				}
			}

			*offset += chunk_size;
			break;
		}
#endif
#endif
        case FOURCC('m', 'd', 'h', 'd'):
        {
            if (chunk_data_size < 4) {
                return ERROR_MALFORMED;
            }

            uint8_t version;
            if (mDataSource->readAt(
                        data_offset, &version, sizeof(version))
                    < (ssize_t)sizeof(version)) {
                return ERROR_IO;
            }

            off64_t timescale_offset;

            if (version == 1) {
                timescale_offset = data_offset + 4 + 16;
            } else if (version == 0) {
                timescale_offset = data_offset + 4 + 8;
            } else {
                return ERROR_IO;
            }

            uint32_t timescale;
            if (mDataSource->readAt(
                        timescale_offset, &timescale, sizeof(timescale))
                    < (ssize_t)sizeof(timescale)) {
                return ERROR_IO;
            }

            mLastTrack->timescale = ntohl(timescale);

            int64_t duration;
            if (version == 1) {
                if (mDataSource->readAt(
                            timescale_offset + 4, &duration, sizeof(duration))
                        < (ssize_t)sizeof(duration)) {
                    return ERROR_IO;
                }
                duration = ntoh64(duration);
            } else {
                int32_t duration32;
                if (mDataSource->readAt(
                            timescale_offset + 4, &duration32, sizeof(duration32))
                        < (ssize_t)sizeof(duration32)) {
                    return ERROR_IO;
                }
                duration = ntohl(duration32);
            }
#ifndef ANDROID_DEFAULT_CODE//hai.li
			mLastTrack->durationUs = (duration * 1000000) / mLastTrack->timescale;
			mLastTrack->meta->setInt64(
					kKeyDuration, mLastTrack->durationUs);
#else			
            mLastTrack->meta->setInt64(
                    kKeyDuration, (duration * 1000000) / mLastTrack->timescale);
#endif
            uint8_t lang[2];
            off64_t lang_offset;
            if (version == 1) {
                lang_offset = timescale_offset + 4 + 8;
            } else if (version == 0) {
                lang_offset = timescale_offset + 4 + 4;
            } else {
                return ERROR_IO;
            }

            if (mDataSource->readAt(lang_offset, &lang, sizeof(lang))
                    < (ssize_t)sizeof(lang)) {
                return ERROR_IO;
            }

            // To get the ISO-639-2/T three character language code
            // 1 bit pad followed by 3 5-bits characters. Each character
            // is packed as the difference between its ASCII value and 0x60.
            char lang_code[4];
            lang_code[0] = ((lang[0] >> 2) & 0x1f) + 0x60;
            lang_code[1] = ((lang[0] & 0x3) << 3 | (lang[1] >> 5)) + 0x60;
            lang_code[2] = (lang[1] & 0x1f) + 0x60;
            lang_code[3] = '\0';

            mLastTrack->meta->setCString(
                    kKeyMediaLanguage, lang_code);

            *offset += chunk_size;
            break;
        }

        case FOURCC('s', 't', 's', 'd'):
        {
            if (chunk_data_size < 8) {
                return ERROR_MALFORMED;
            }

            uint8_t buffer[8];
            if (chunk_data_size < (off64_t)sizeof(buffer)) {
                return ERROR_MALFORMED;
            }

            if (mDataSource->readAt(
                        data_offset, buffer, 8) < 8) {
                return ERROR_IO;
            }

            if (U32_AT(buffer) != 0) {
                // Should be version 0, flags 0.
                return ERROR_MALFORMED;
            }

            uint32_t entry_count = U32_AT(&buffer[4]);

            if (entry_count > 1) {
                // For 3GPP timed text, there could be multiple tx3g boxes contain
                // multiple text display formats. These formats will be used to
                // display the timed text.
                const char *mime;
                CHECK(mLastTrack->meta->findCString(kKeyMIMEType, &mime));
                if (strcasecmp(mime, MEDIA_MIMETYPE_TEXT_3GPP)) {
                    // For now we only support a single type of media per track.
                    mLastTrack->skipTrack = true;
                    *offset += chunk_size;
                    break;
                }
            }

            off64_t stop_offset = *offset + chunk_size;
            *offset = data_offset + 8;
            for (uint32_t i = 0; i < entry_count; ++i) {
                status_t err = parseChunk(offset, depth + 1);
                if (err != OK) {
                    return err;
                }
            }

            if (*offset != stop_offset) {
                return ERROR_MALFORMED;
            }
            break;
        }

        case FOURCC('m', 'p', '4', 'a'):
        case FOURCC('s', 'a', 'm', 'r'):
        case FOURCC('s', 'a', 'w', 'b'):
#ifndef ANDROI_DEFAULT_CODE
		case FOURCC('.', 'm', 'p', '3'):
#endif
        {
#ifndef ANDROID_DEFAULT_CODE//hai.li
			mHasAudio = true;
			mLastTrack->mIsAudio = true;
#endif
            uint8_t buffer[8 + 20];
            if (chunk_data_size < (ssize_t)sizeof(buffer)) {
#if !defined(ANDROID_DEFAULT_CODE) && defined(QUICKTIME_SUPPORT)//for .mov file		
				
				if (mPath.size() >= 2
						&& mPath[mPath.size() - 2] == FOURCC('w', 'a', 'v', 'e')) {
					*offset += chunk_size;
					return OK;
				}
				else {
					LOGE("ERROR_MALFORMED, LINE=%d", __LINE__);
					return ERROR_MALFORMED;
				}
#else
                // Basic AudioSampleEntry size.
                return ERROR_MALFORMED;
#endif
            }

            if (mDataSource->readAt(
                        data_offset, buffer, sizeof(buffer)) < (ssize_t)sizeof(buffer)) {
                return ERROR_IO;
            }

            uint16_t data_ref_index = U16_AT(&buffer[6]);
            uint16_t num_channels = U16_AT(&buffer[16]);

            uint16_t sample_size = U16_AT(&buffer[18]);
            uint32_t sample_rate = U32_AT(&buffer[24]) >> 16;

            if (!strcasecmp(MEDIA_MIMETYPE_AUDIO_AMR_NB,
                            FourCC2MIME(chunk_type))) {
                // AMR NB audio is always mono, 8kHz
                num_channels = 1;
                sample_rate = 8000;
            } else if (!strcasecmp(MEDIA_MIMETYPE_AUDIO_AMR_WB,
                               FourCC2MIME(chunk_type))) {
                // AMR WB audio is always mono, 16kHz
                num_channels = 1;
                sample_rate = 16000;
            }

#ifndef ANDROID_DEFAULT_CODE
            else if(!strcasecmp(MEDIA_MIMETYPE_AUDIO_MPEG,
                               FourCC2MIME(chunk_type))){
		mLastTrack->meta->setInt32(kKeyCodecInfoIsInFirstFrame, true);
	    }
#endif

#if 0
            printf("*** coding='%s' %d channels, size %d, rate %d\n",
                   chunk, num_channels, sample_size, sample_rate);
#endif

            mLastTrack->meta->setCString(kKeyMIMEType, FourCC2MIME(chunk_type));
            mLastTrack->meta->setInt32(kKeyChannelCount, num_channels);
            mLastTrack->meta->setInt32(kKeySampleRate, sample_rate);

            off64_t stop_offset = *offset + chunk_size;
            *offset = data_offset + sizeof(buffer);
#ifndef ANDROID_DEFAULT_CODE
#ifdef QUICKTIME_SUPPORT//hai.li for .mov
			if (1 == U16_AT(&buffer[8]))//sound media version == 1
				*offset += 16;//4*4byte
#endif
			if (num_channels > 2)
			{
				mLastTrack->skipTrack = true;
				*offset = stop_offset;
			}	
#endif
            while (*offset < stop_offset) {
                status_t err = parseChunk(offset, depth + 1);
                if (err != OK) {
                    return err;
                }
            }

            if (*offset != stop_offset) {
                return ERROR_MALFORMED;
            }
            break;
        }

        case FOURCC('m', 'p', '4', 'v'):
        case FOURCC('s', '2', '6', '3'):
        case FOURCC('H', '2', '6', '3'):
        case FOURCC('h', '2', '6', '3'):
        case FOURCC('a', 'v', 'c', '1'):
        {
            mHasVideo = true;

            uint8_t buffer[78];
            if (chunk_data_size < (ssize_t)sizeof(buffer)) {
                // Basic VideoSampleEntry size.
                return ERROR_MALFORMED;
            }

            if (mDataSource->readAt(
                        data_offset, buffer, sizeof(buffer)) < (ssize_t)sizeof(buffer)) {
                return ERROR_IO;
            }

            uint16_t data_ref_index = U16_AT(&buffer[6]);
            uint16_t width = U16_AT(&buffer[6 + 18]);
            uint16_t height = U16_AT(&buffer[6 + 20]);

            // The video sample is not stand-compliant if it has invalid dimension.
            // Use some default width and height value, and
            // let the decoder figure out the actual width and height (and thus
            // be prepared for INFO_FOMRAT_CHANGED event).
            if (width == 0)  width  = 352;
            if (height == 0) height = 288;

            // printf("*** coding='%s' width=%d height=%d\n",
            //        chunk, width, height);

            mLastTrack->meta->setCString(kKeyMIMEType, FourCC2MIME(chunk_type));
            mLastTrack->meta->setInt32(kKeyWidth, width);
            mLastTrack->meta->setInt32(kKeyHeight, height);
#ifndef ANDROID_DEFAULT_CODE//hai.li
	    mLastTrack->mIsVideo = true;
	    VDEC_DRV_QUERY_VIDEO_FORMAT_T qinfo;
	    VDEC_DRV_QUERY_VIDEO_FORMAT_T outinfo;
	    memset(&qinfo, 0, sizeof(qinfo));
	    memset(&outinfo, 0, sizeof(outinfo));
	    qinfo.u4Width = width;
	    qinfo.u4Height = height;

	    if (!strcasecmp(MEDIA_MIMETYPE_VIDEO_H263,
				    FourCC2MIME(chunk_type))) {
		    mLastTrack->mIsH263 = true;           
		    qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
	    }
	    else if (!strcasecmp(MEDIA_MIMETYPE_VIDEO_MPEG4,
				    FourCC2MIME(chunk_type))) {
		    qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
	    }
	    else if (!strcasecmp(MEDIA_MIMETYPE_VIDEO_AVC,
				    FourCC2MIME(chunk_type))) {
                    qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_H264;
	    }
	    LOGD("Video: %dx%d, profile(%d), level(%d)", 
			     qinfo.u4Width, qinfo.u4Height, qinfo.u4Profile, qinfo.u4Level);

	    VDEC_DRV_MRESULT_T ret = eVDecDrvQueryCapability(VDEC_DRV_QUERY_TYPE_VIDEO_FORMAT, &qinfo, &outinfo);

	    LOGD("eVDecDrvQueryCapability return %d", ret);
            VIDEO_MAX_RESOLUTION = outinfo.u4Width*outinfo.u4Height;
/*
	    if (qinfo.u4Width > outinfo.u4Width || qinfo.u4Height > outinfo.u4Width ||
			     qinfo.u4Width*qinfo.u4Height > outinfo.u4Width*outinfo.u4Height || (width <= 0) || (height <= 0)) {
		    LOGE("[MP4 capability error]Unsupport video demension!!!width=%d, height=%d", width, height);
		    mLastTrack->skipTrack = true;
		    mHasVideo = false;
	    }
*/
#endif//#ifndef ANDROID_DEFAULT_CODE

            off64_t stop_offset = *offset + chunk_size;
            *offset = data_offset + sizeof(buffer);
            while (*offset < stop_offset) {
#if !defined(ANDROID_DEFAULT_CODE) && defined(QUICKTIME_SUPPORT)//for .mov file
				if (stop_offset - *offset < 8)
					*offset = stop_offset;//Maybe terminate box? 0x00000000
				else {
					status_t err = parseChunk(offset, depth + 1);
					if (err != OK) {
						return err;
					}
				}
#else
                status_t err = parseChunk(offset, depth + 1);
                if (err != OK) {
                    return err;
                }
#endif
            }

            if (*offset != stop_offset) {
                return ERROR_MALFORMED;
            }
            break;
        }

        case FOURCC('s', 't', 'c', 'o'):
        case FOURCC('c', 'o', '6', '4'):
        {
            status_t err =
                mLastTrack->sampleTable->setChunkOffsetParams(
                        chunk_type, data_offset, chunk_data_size);

            if (err != OK) {
                return err;
            }
            *offset += chunk_size;
            break;
        }

        case FOURCC('s', 't', 's', 'c'):
        {
            status_t err =
                mLastTrack->sampleTable->setSampleToChunkParams(
                        data_offset, chunk_data_size);

            if (err != OK) {
                return err;
            }

            *offset += chunk_size;
            break;
        }

        case FOURCC('s', 't', 's', 'z'):
        case FOURCC('s', 't', 'z', '2'):
        {
            status_t err =
                mLastTrack->sampleTable->setSampleSizeParams(
                        chunk_type, data_offset, chunk_data_size);

            if (err != OK) {
                return err;
            }
#ifndef ANDROID_DEFAULT_CODE//hai.li to check unsupport video
			mLastTrack->sampleCount = mLastTrack->sampleTable->getSampleCount();
#endif

            size_t max_size;
            err = mLastTrack->sampleTable->getMaxSampleSize(&max_size);

#ifndef ANDROID_DEFAULT_CODE//hai.li for ISSUE: ALPS35871
           mLastTrack->mMaxSize = max_size;
#endif
            if (err != OK) {
                return err;
            }

            // Assume that a given buffer only contains at most 10 fragments,
            // each fragment originally prefixed with a 2 byte length will
            // have a 4 byte header (0x00 0x00 0x00 0x01) after conversion,
            // and thus will grow by 2 bytes per fragment.
            mLastTrack->meta->setInt32(kKeyMaxInputSize, max_size + 10 * 2);
            *offset += chunk_size;

            // Calculate average frame rate.
            const char *mime;
            CHECK(mLastTrack->meta->findCString(kKeyMIMEType, &mime));
            if (!strncasecmp("video/", mime, 6)) {
                size_t nSamples = mLastTrack->sampleTable->countSamples();
                int64_t durationUs;
                if (mLastTrack->meta->findInt64(kKeyDuration, &durationUs)) {
                    if (durationUs > 0) {
                        int32_t frameRate = (nSamples * 1000000LL +
                                    (durationUs >> 1)) / durationUs;
                        mLastTrack->meta->setInt32(kKeyFrameRate, frameRate);
                    }
                }
            }

            break;
        }

        case FOURCC('s', 't', 't', 's'):
        {
            status_t err =
                mLastTrack->sampleTable->setTimeToSampleParams(
                        data_offset, chunk_data_size);

            if (err != OK) {
                return err;
            }

            *offset += chunk_size;
            break;
        }

        case FOURCC('c', 't', 't', 's'):
        {
            status_t err =
                mLastTrack->sampleTable->setCompositionTimeToSampleParams(
                        data_offset, chunk_data_size);

            if (err != OK) {
                return err;
            }

            *offset += chunk_size;
            break;
        }

        case FOURCC('s', 't', 's', 's'):
        {
            status_t err =
                mLastTrack->sampleTable->setSyncSampleParams(
                        data_offset, chunk_data_size);

            if (err != OK) {
                return err;
            }

            *offset += chunk_size;
            break;
        }

        // @xyz
        case FOURCC('\xA9', 'x', 'y', 'z'):
        {
            // Best case the total data length inside "@xyz" box
            // would be 8, for instance "@xyz" + "\x00\x04\x15\xc7" + "0+0/",
            // where "\x00\x04" is the text string length with value = 4,
            // "\0x15\xc7" is the language code = en, and "0+0" is a
            // location (string) value with longitude = 0 and latitude = 0.
            if (chunk_data_size < 8) {
                return ERROR_MALFORMED;
            }

            // Worst case the location string length would be 18,
            // for instance +90.0000-180.0000, without the trailing "/" and
            // the string length + language code.
            char buffer[18];

            // Substracting 5 from the data size is because the text string length +
            // language code takes 4 bytes, and the trailing slash "/" takes 1 byte.
            off64_t location_length = chunk_data_size - 5;
            if (location_length >= (off64_t) sizeof(buffer)) {
                return ERROR_MALFORMED;
            }

            if (mDataSource->readAt(
                        data_offset + 4, buffer, location_length) < location_length) {
                return ERROR_IO;
            }

            buffer[location_length] = '\0';
            mFileMetaData->setCString(kKeyLocation, buffer);
            *offset += chunk_size;
            break;
        }

        case FOURCC('e', 's', 'd', 's'):
        {
            if (chunk_data_size < 4) {
                return ERROR_MALFORMED;
            }
#ifndef ANDROID_DEFAULT_CODE			
            if (chunk_data_size > 4000){
                     return ERROR_BUFFER_TOO_SMALL;
            }
                      
            uint8_t  *buffer = (uint8_t *)malloc(chunk_data_size);

            if (buffer == NULL) {
              return -ENOMEM;
            }


            if (mDataSource->readAt(
                        data_offset, buffer, chunk_data_size) < chunk_data_size) {
				free(buffer);
				LOGE("ERROR_IO, LINE=%d", __LINE__);
                return ERROR_IO;
            }

            if (U32_AT(buffer) != 0) {
                // Should be version 0, flags 0.
				free(buffer);
				LOGE("ERROR_MALFORMED, LINE=%d", __LINE__);
                return ERROR_MALFORMED;
            }
#else
            uint8_t buffer[256];
            if (chunk_data_size > (off64_t)sizeof(buffer)) {
                return ERROR_BUFFER_TOO_SMALL;
            }

            if (mDataSource->readAt(
                        data_offset, buffer, chunk_data_size) < chunk_data_size) {
                return ERROR_IO;
            }

            if (U32_AT(buffer) != 0) {
                // Should be version 0, flags 0.
                return ERROR_MALFORMED;
            }
#endif
            mLastTrack->meta->setData(
                    kKeyESDS, kTypeESDS, &buffer[4], chunk_data_size - 4);

            if (mPath.size() >= 2
                    && mPath[mPath.size() - 2] == FOURCC('m', 'p', '4', 'a')) {
                // Information from the ESDS must be relied on for proper
                // setup of sample rate and channel count for MPEG4 Audio.
                // The generic header appears to only contain generic
                // information...

                status_t err = updateAudioTrackInfoFromESDS_MPEG4Audio(
                        &buffer[4], chunk_data_size - 4);
#ifndef ANDROID_DEFAULT_CODE			
                if (err != OK) {
					mLastTrack->skipTrack = true;
                }

				const char* mime;
				if (mLastTrack->meta->findCString(kKeyMIMEType, &mime) &&
					(!strcmp(mime, MEDIA_MIMETYPE_AUDIO_MPEG))) {
					LOGE("Is MP3 Audio, remove esds codec info");
					mLastTrack->meta->remove(kKeyESDS);
				}
#else
                if (err != OK) {
                    return err;
                }
#endif
            }

#ifndef ANDROID_DEFAULT_CODE
			if (mPath.size() >= 2
					&& mPath[mPath.size() - 2] == FOURCC('m', 'p', '4', 'v')) {
				
				//mLastTrack->meta->remove(kKeyESDS);//We should send esds to decoder for 3rd party applications, e.x. VideoEditor.
				ESDS esds(&buffer[4], chunk_data_size - 4);
				if (esds.InitCheck() == OK) {
					const void *codec_specific_data;
					size_t codec_specific_data_size;
					esds.getCodecSpecificInfo(
							&codec_specific_data, &codec_specific_data_size);
					mLastTrack->meta->setData(kKeyMPEG4VOS, 0, codec_specific_data, codec_specific_data_size);
				}
				else if (ERROR_UNSUPPORTED == esds.InitCheck())
				{
					LOGW("Get vos from the first frame");
					mLastTrack->meta->setInt32(kKeyCodecInfoIsInFirstFrame, true);
				}
				else {
					LOGE("Parse esds error, skip video track");
					mLastTrack->skipTrack = true;
				}

			}
#endif

            *offset += chunk_size;
#ifndef ANDROID_DEFAULT_CODE			
			free(buffer);
#endif  // ANDROID_DEFAULT_CODE
            break;
        }

        case FOURCC('a', 'v', 'c', 'C'):
        {
#ifndef ANDROID_DEFAULT_CODE			
			if (chunk_data_size > 1792){
                     return ERROR_BUFFER_TOO_SMALL;
            }
                      
            uint8_t *buffer = (uint8_t *)malloc(chunk_data_size);

            if (buffer == NULL) {
              return -ENOMEM;
            }

            if (mDataSource->readAt( data_offset, buffer, chunk_data_size) < chunk_data_size) {
				   free(buffer);
				   LOGE("ERROR_IO, LINE=%d", __LINE__);
                   return ERROR_IO;
            }
#else
            char buffer[256];
            if (chunk_data_size > (off64_t)sizeof(buffer)) {
                return ERROR_BUFFER_TOO_SMALL;
            }

            if (mDataSource->readAt(
                        data_offset, buffer, chunk_data_size) < chunk_data_size) {
                return ERROR_IO;
            }
#endif
            mLastTrack->meta->setData(
                    kKeyAVCC, kTypeAVCC, buffer, chunk_data_size);

            *offset += chunk_size;
#ifndef ANDROID_DEFAULT_CODE			
			free(buffer);
#endif	
            break;
        }

        case FOURCC('d', '2', '6', '3'):
        {
            /*
             * d263 contains a fixed 7 bytes part:
             *   vendor - 4 bytes
             *   version - 1 byte
             *   level - 1 byte
             *   profile - 1 byte
             * optionally, "d263" box itself may contain a 16-byte
             * bit rate box (bitr)
             *   average bit rate - 4 bytes
             *   max bit rate - 4 bytes
             */
            char buffer[23];
#ifndef ANDROID_DEFAULT_CODE//Some files do not comply this rule
			if (chunk_data_size > 23)
#else
            if (chunk_data_size != 7 &&
                chunk_data_size != 23)
#endif
            {
                LOGE("Incorrect D263 box size %lld", chunk_data_size);
                return ERROR_MALFORMED;
            }

            if (mDataSource->readAt(
                    data_offset, buffer, chunk_data_size) < chunk_data_size) {
                return ERROR_IO;
            }

            mLastTrack->meta->setData(kKeyD263, kTypeD263, buffer, chunk_data_size);

            *offset += chunk_size;
            break;
        }

        case FOURCC('m', 'e', 't', 'a'):
        {
            uint8_t buffer[4];
            if (chunk_data_size < (off64_t)sizeof(buffer)) {
                return ERROR_MALFORMED;
            }

            if (mDataSource->readAt(
                        data_offset, buffer, 4) < 4) {
                return ERROR_IO;
            }

            if (U32_AT(buffer) != 0) {
                // Should be version 0, flags 0.

                // If it's not, let's assume this is one of those
                // apparently malformed chunks that don't have flags
                // and completely different semantics than what's
                // in the MPEG4 specs and skip it.
                *offset += chunk_size;
                return OK;
            }

            off64_t stop_offset = *offset + chunk_size;
            *offset = data_offset + sizeof(buffer);
            while (*offset < stop_offset) {
                status_t err = parseChunk(offset, depth + 1);
                if (err != OK) {
                    return err;
                }
            }

            if (*offset != stop_offset) {
                return ERROR_MALFORMED;
            }
            break;
        }

        case FOURCC('d', 'a', 't', 'a'):
        {
            if (mPath.size() == 6 && underMetaDataPath(mPath)) {
                status_t err = parseMetaData(data_offset, chunk_data_size);

                if (err != OK) {
                    return err;
                }
            }

            *offset += chunk_size;
            break;
        }

        case FOURCC('m', 'v', 'h', 'd'):
        {
            if (chunk_data_size < 12) {
                return ERROR_MALFORMED;
            }

#ifndef ANDROID_DEFAULT_CODE
			uint8_t header[24];
#else
            uint8_t header[12];
#endif
            if (mDataSource->readAt(
                        data_offset, header, sizeof(header))
                    < (ssize_t)sizeof(header)) {
                return ERROR_IO;
            }

            int64_t creationTime;
            if (header[0] == 1) {
                creationTime = U64_AT(&header[4]);
#ifndef ANDROID_DEFAULT_CODE
				mMovieTimeScale = U32_AT(&header[20]);
#endif
            } else if (header[0] != 0) {
                return ERROR_MALFORMED;
            } else {
                creationTime = U32_AT(&header[4]);
#ifndef ANDROID_DEFAULT_CODE
				mMovieTimeScale = U32_AT(&header[12]);
#endif
            }

            String8 s;
            convertTimeToDate(creationTime, &s);

            mFileMetaData->setCString(kKeyDate, s.string());

            *offset += chunk_size;
            break;
        }

        case FOURCC('m', 'd', 'a', 't'):
        {
            if (!mIsDrm) {
                *offset += chunk_size;
                break;
            }

            if (chunk_size < 8) {
                return ERROR_MALFORMED;
            }

            return parseDrmSINF(offset, data_offset);
        }

        case FOURCC('h', 'd', 'l', 'r'):
        {
            uint32_t buffer;
            if (mDataSource->readAt(
                        data_offset + 8, &buffer, 4) < 4) {
                return ERROR_IO;
            }

            uint32_t type = ntohl(buffer);
            // For the 3GPP file format, the handler-type within the 'hdlr' box
            // shall be 'text'
            if (type == FOURCC('t', 'e', 'x', 't')) {
                mLastTrack->meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_TEXT_3GPP);
            }

            *offset += chunk_size;
            break;
        }

        case FOURCC('t', 'x', '3', 'g'):
        {
            uint32_t type;
            const void *data;
            size_t size = 0;
            if (!mLastTrack->meta->findData(
                    kKeyTextFormatData, &type, &data, &size)) {
                size = 0;
            }

            uint8_t *buffer = new uint8_t[size + chunk_size];

            if (size > 0) {
                memcpy(buffer, data, size);
            }

            if ((size_t)(mDataSource->readAt(*offset, buffer + size, chunk_size))
                    < chunk_size) {
                delete[] buffer;
                buffer = NULL;

                return ERROR_IO;
            }

            mLastTrack->meta->setData(
                    kKeyTextFormatData, 0, buffer, size + chunk_size);

            delete[] buffer;

            *offset += chunk_size;
            break;
        }

        case FOURCC('c', 'o', 'v', 'r'):
        {
            if (mFileMetaData != NULL) {
                LOGV("chunk_data_size = %lld and data_offset = %lld",
                        chunk_data_size, data_offset);
                uint8_t *buffer = new uint8_t[chunk_data_size + 1];
                if (mDataSource->readAt(
                    data_offset, buffer, chunk_data_size) != (ssize_t)chunk_data_size) {
                    delete[] buffer;
                    buffer = NULL;

                    return ERROR_IO;
                }
                const int kSkipBytesOfDataBox = 16;
                mFileMetaData->setData(
                    kKeyAlbumArt, MetaData::TYPE_NONE,
                    buffer + kSkipBytesOfDataBox, chunk_data_size - kSkipBytesOfDataBox);
            }

            *offset += chunk_size;
            break;
        }

        default:
        {
            *offset += chunk_size;
            break;
        }
    }

    return OK;
}

status_t MPEG4Extractor::parseTrackHeader(
        off64_t data_offset, off64_t data_size) {
    if (data_size < 4) {
        return ERROR_MALFORMED;
    }

    uint8_t version;
    if (mDataSource->readAt(data_offset, &version, 1) < 1) {
        return ERROR_IO;
    }

    size_t dynSize = (version == 1) ? 36 : 24;

    uint8_t buffer[36 + 60];

    if (data_size != (off64_t)dynSize + 60) {
        return ERROR_MALFORMED;
    }

    if (mDataSource->readAt(
                data_offset, buffer, data_size) < (ssize_t)data_size) {
        return ERROR_IO;
    }

    uint64_t ctime, mtime, duration;
    int32_t id;

    if (version == 1) {
        ctime = U64_AT(&buffer[4]);
        mtime = U64_AT(&buffer[12]);
        id = U32_AT(&buffer[20]);
        duration = U64_AT(&buffer[28]);
    } else {
        CHECK_EQ((unsigned)version, 0u);

        ctime = U32_AT(&buffer[4]);
        mtime = U32_AT(&buffer[8]);
        id = U32_AT(&buffer[12]);
        duration = U32_AT(&buffer[20]);
    }

    mLastTrack->meta->setInt32(kKeyTrackID, id);

    size_t matrixOffset = dynSize + 16;
    int32_t a00 = U32_AT(&buffer[matrixOffset]);
    int32_t a01 = U32_AT(&buffer[matrixOffset + 4]);
    int32_t dx = U32_AT(&buffer[matrixOffset + 8]);
    int32_t a10 = U32_AT(&buffer[matrixOffset + 12]);
    int32_t a11 = U32_AT(&buffer[matrixOffset + 16]);
    int32_t dy = U32_AT(&buffer[matrixOffset + 20]);

#if 0
    LOGI("x' = %.2f * x + %.2f * y + %.2f",
         a00 / 65536.0f, a01 / 65536.0f, dx / 65536.0f);
    LOGI("y' = %.2f * x + %.2f * y + %.2f",
         a10 / 65536.0f, a11 / 65536.0f, dy / 65536.0f);
#endif

    uint32_t rotationDegrees;

    static const int32_t kFixedOne = 0x10000;
    if (a00 == kFixedOne && a01 == 0 && a10 == 0 && a11 == kFixedOne) {
        // Identity, no rotation
        rotationDegrees = 0;
    } else if (a00 == 0 && a01 == kFixedOne && a10 == -kFixedOne && a11 == 0) {
        rotationDegrees = 90;
    } else if (a00 == 0 && a01 == -kFixedOne && a10 == kFixedOne && a11 == 0) {
        rotationDegrees = 270;
    } else if (a00 == -kFixedOne && a01 == 0 && a10 == 0 && a11 == -kFixedOne) {
        rotationDegrees = 180;
    } else {
        LOGW("We only support 0,90,180,270 degree rotation matrices");
        rotationDegrees = 0;
    }

    if (rotationDegrees != 0) {
        mLastTrack->meta->setInt32(kKeyRotation, rotationDegrees);
    }

    // Handle presentation display size, which could be different
    // from the image size indicated by kKeyWidth and kKeyHeight.
    uint32_t width = U32_AT(&buffer[dynSize + 52]);
    uint32_t height = U32_AT(&buffer[dynSize + 56]);
    mLastTrack->meta->setInt32(kKeyDisplayWidth, width >> 16);
    mLastTrack->meta->setInt32(kKeyDisplayHeight, height >> 16);

    return OK;
}

status_t MPEG4Extractor::parseMetaData(off64_t offset, size_t size) {
    if (size < 4) {
        return ERROR_MALFORMED;
    }

    uint8_t *buffer = new uint8_t[size + 1];
    if (mDataSource->readAt(
                offset, buffer, size) != (ssize_t)size) {
        delete[] buffer;
        buffer = NULL;

        return ERROR_IO;
    }

    uint32_t flags = U32_AT(buffer);

    uint32_t metadataKey = 0;
    switch (mPath[4]) {
        case FOURCC(0xa9, 'a', 'l', 'b'):
        {
            metadataKey = kKeyAlbum;
            break;
        }
        case FOURCC(0xa9, 'A', 'R', 'T'):
        {
            metadataKey = kKeyArtist;
            break;
        }
        case FOURCC('a', 'A', 'R', 'T'):
        {
            metadataKey = kKeyAlbumArtist;
            break;
        }
        case FOURCC(0xa9, 'd', 'a', 'y'):
        {
            metadataKey = kKeyYear;
            break;
        }
        case FOURCC(0xa9, 'n', 'a', 'm'):
        {
            metadataKey = kKeyTitle;
            break;
        }
        case FOURCC(0xa9, 'w', 'r', 't'):
        {
            metadataKey = kKeyWriter;
            break;
        }
        case FOURCC('c', 'o', 'v', 'r'):
        {
            metadataKey = kKeyAlbumArt;
            break;
        }
        case FOURCC('g', 'n', 'r', 'e'):
        {
            metadataKey = kKeyGenre;
            break;
        }
        case FOURCC(0xa9, 'g', 'e', 'n'):
        {
            metadataKey = kKeyGenre;
            break;
        }
        case FOURCC('c', 'p', 'i', 'l'):
        {
            if (size == 9 && flags == 21) {
                char tmp[16];
                sprintf(tmp, "%d",
                        (int)buffer[size - 1]);

                mFileMetaData->setCString(kKeyCompilation, tmp);
            }
            break;
        }
        case FOURCC('t', 'r', 'k', 'n'):
        {
            if (size == 16 && flags == 0) {
                char tmp[16];
                sprintf(tmp, "%d/%d",
                        (int)buffer[size - 5], (int)buffer[size - 3]);

                mFileMetaData->setCString(kKeyCDTrackNumber, tmp);
            }
            break;
        }
        case FOURCC('d', 'i', 's', 'k'):
        {
            if (size == 14 && flags == 0) {
                char tmp[16];
                sprintf(tmp, "%d/%d",
                        (int)buffer[size - 3], (int)buffer[size - 1]);

                mFileMetaData->setCString(kKeyDiscNumber, tmp);
            }
            break;
        }

        default:
            break;
    }

    if (size >= 8 && metadataKey) {
        if (metadataKey == kKeyAlbumArt) {
            mFileMetaData->setData(
                    kKeyAlbumArt, MetaData::TYPE_NONE,
                    buffer + 8, size - 8);
        } else if (metadataKey == kKeyGenre) {
            if (flags == 0) {
                // uint8_t genre code, iTunes genre codes are
                // the standard id3 codes, except they start
                // at 1 instead of 0 (e.g. Pop is 14, not 13)
                // We use standard id3 numbering, so subtract 1.
                int genrecode = (int)buffer[size - 1];
                genrecode--;
                if (genrecode < 0) {
                    genrecode = 255; // reserved for 'unknown genre'
                }
                char genre[10];
                sprintf(genre, "%d", genrecode);

                mFileMetaData->setCString(metadataKey, genre);
            } else if (flags == 1) {
                // custom genre string
                buffer[size] = '\0';

                mFileMetaData->setCString(
                        metadataKey, (const char *)buffer + 8);
            }
        } else {
            buffer[size] = '\0';

            mFileMetaData->setCString(
                    metadataKey, (const char *)buffer + 8);
        }
    }

    delete[] buffer;
    buffer = NULL;

    return OK;
}

sp<MediaSource> MPEG4Extractor::getTrack(size_t index) {
    status_t err;
    if ((err = readMetaData()) != OK) {
        return NULL;
    }

    Track *track = mFirstTrack;
    while (index > 0) {
        if (track == NULL) {
            return NULL;
        }

        track = track->next;
        --index;
    }

    if (track == NULL) {
        return NULL;
    }

#ifndef ANDROID_DEFAULT_CODE
		if ((track->mElstEntries != NULL) && 
			(track->mStartTimeOffset != 0) &&
			(mMovieTimeScale != 0))
		{	
			track->sampleTable->setStartTimeOffset((uint64_t)track->mStartTimeOffset*track->timescale/mMovieTimeScale);
			LOGD("track->mStartTimeOffset=%d, track->timescale=%d, mMovieTimeScale=%d", track->mStartTimeOffset, track->timescale, mMovieTimeScale);
			
			const char *mime;
			CHECK(track->meta->findCString(kKeyMIMEType, &mime));
			if (!strncasecmp("audio/", mime, 6)) {
				uint64_t PadSampleNum = track->sampleTable->getStartTimeOffset();
				if (PadSampleNum >= 512*1024)
				{
					LOGW("Unsupported too large audio time offset: %d samples!!", PadSampleNum);
					track->sampleTable->setStartTimeOffset(0);
					track->mStartTimeOffset = 0;
				}
				LOGE("audio time offset=%d", track->sampleTable->getStartTimeOffset());
				if (track->sampleTable->getStartTimeOffset() != 0)
					track->meta->setInt32(kKeyAudioPadEnable, true);
			}
		}//added by hai.li to support track time offset
    track->meta->setInt32(kKeySupportTryRead, 1);
#endif
    return new MPEG4Source(
            track->meta, mDataSource, track->timescale, track->sampleTable);
}

// static
status_t MPEG4Extractor::verifyTrack(Track *track) {
    const char *mime;
    CHECK(track->meta->findCString(kKeyMIMEType, &mime));

    uint32_t type;
    const void *data;
    size_t size;
    if (!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_AVC)) {
        if (!track->meta->findData(kKeyAVCC, &type, &data, &size)
                || type != kTypeAVCC) {
            return ERROR_MALFORMED;
        }
    } 
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_MPEG4)
            || !strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_AAC)) {
        if (!track->meta->findData(kKeyESDS, &type, &data, &size)
                || type != kTypeESDS) {
            return ERROR_MALFORMED;
        }
    }
    if (!track->sampleTable->isValid()) {
        // Make sure we have all the metadata we need.
        return ERROR_MALFORMED;
    }

    return OK;
}

status_t MPEG4Extractor::updateAudioTrackInfoFromESDS_MPEG4Audio(
        const void *esds_data, size_t esds_size) {
    ESDS esds(esds_data, esds_size);

    uint8_t objectTypeIndication;
    if (esds.getObjectTypeIndication(&objectTypeIndication) != OK) {
        return ERROR_MALFORMED;
    }

    if (objectTypeIndication == 0xe1) {
        // This isn't MPEG4 audio at all, it's QCELP 14k...
        mLastTrack->meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_QCELP);
#ifndef ANDROID_DEFAULT_CODE
		mLastTrack->skipTrack = true;
		LOGD("Skip qcelp audio track");
#endif
        return OK;
    }
#ifndef ANDROID_DEFAULT_CODE                                        //xingyu.zhou
	if (objectTypeIndication == 0x6B || objectTypeIndication == 0x69) {
		mLastTrack->meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_MPEG);
		return OK;
	}
#else
    if (objectTypeIndication  == 0x6b) {
        // The media subtype is MP3 audio
        // Our software MP3 audio decoder may not be able to handle
        // packetized MP3 audio; for now, lets just return ERROR_UNSUPPORTED
        LOGE("MP3 track in MP4/3GPP file is not supported");
        return ERROR_UNSUPPORTED;
    }
#endif
    const uint8_t *csd;
    size_t csd_size;
    if (esds.getCodecSpecificInfo(
                (const void **)&csd, &csd_size) != OK) {
        return ERROR_MALFORMED;
    }

#if 0
    printf("ESD of size %d\n", csd_size);
    hexdump(csd, csd_size);
#endif

    if (csd_size == 0) {
        // There's no further information, i.e. no codec specific data
        // Let's assume that the information provided in the mpeg4 headers
        // is accurate and hope for the best.

        return OK;
    }

    if (csd_size < 2) {
        return ERROR_MALFORMED;
    }

    uint32_t objectType = csd[0] >> 3;
#ifndef ANDROID_DEFAULT_CODE
   mLastTrack->meta->setInt32(kKeyAacObjType, objectType);
#endif

    if (objectType == 31) {
        return ERROR_UNSUPPORTED;
    }

    uint32_t freqIndex = (csd[0] & 7) << 1 | (csd[1] >> 7);
    int32_t sampleRate = 0;
    int32_t numChannels = 0;
    if (freqIndex == 15) {
        if (csd_size < 5) {
            return ERROR_MALFORMED;
        }

        sampleRate = (csd[1] & 0x7f) << 17
                        | csd[2] << 9
                        | csd[3] << 1
                        | (csd[4] >> 7);

        numChannels = (csd[4] >> 3) & 15;
    } else {
        static uint32_t kSamplingRate[] = {
            96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
            16000, 12000, 11025, 8000, 7350
        };

        if (freqIndex == 13 || freqIndex == 14) {
            return ERROR_MALFORMED;
        }

        sampleRate = kSamplingRate[freqIndex];
        numChannels = (csd[1] >> 3) & 15;
    }

    if (numChannels == 0) {
        return ERROR_UNSUPPORTED;
    }

    int32_t prevSampleRate;
    CHECK(mLastTrack->meta->findInt32(kKeySampleRate, &prevSampleRate));

    if (prevSampleRate != sampleRate) {
        LOGV("mpeg4 audio sample rate different from previous setting. "
             "was: %d, now: %d", prevSampleRate, sampleRate);
    }

    mLastTrack->meta->setInt32(kKeySampleRate, sampleRate);

    int32_t prevChannelCount;
    CHECK(mLastTrack->meta->findInt32(kKeyChannelCount, &prevChannelCount));

    if (prevChannelCount != numChannels) {
        LOGV("mpeg4 audio channel count different from previous setting. "
             "was: %d, now: %d", prevChannelCount, numChannels);
    }

    mLastTrack->meta->setInt32(kKeyChannelCount, numChannels);

    return OK;
}

#ifndef ANDROID_DEFAULT_CODE
static bool get_mp3_info(
        uint32_t header, size_t *frame_size,
        int *out_sampling_rate = NULL, int *out_channels = NULL,
        int *out_bitrate = NULL) {
    *frame_size = 0;

    if (out_sampling_rate) {
        *out_sampling_rate = 0;
    }

    if (out_channels) {
        *out_channels = 0;
    }

    if (out_bitrate) {
        *out_bitrate = 0;
    }

    if ((header & 0xffe00000) != 0xffe00000) {
		LOGD("line=%d", __LINE__);
        return false;
    }

    unsigned version = (header >> 19) & 3;

    if (version == 0x01) {
		LOGD("line=%d", __LINE__);
        return false;
    }

    unsigned layer = (header >> 17) & 3;

    if (layer == 0x00) {
		LOGD("line=%d", __LINE__);
        return false;
    }

    unsigned protection = (header >> 16) & 1;

    unsigned bitrate_index = (header >> 12) & 0x0f;

    if (bitrate_index == 0 || bitrate_index == 0x0f) {
        // Disallow "free" bitrate.
        
		LOGD("line=%d", __LINE__);
        return false;
    }

    unsigned sampling_rate_index = (header >> 10) & 3;

    if (sampling_rate_index == 3) {
		
		LOGD("line=%d", __LINE__);
        return false;
    }

    static const int kSamplingRateV1[] = { 44100, 48000, 32000 };
    int sampling_rate = kSamplingRateV1[sampling_rate_index];
    if (version == 2 /* V2 */) {
        sampling_rate /= 2;
    } else if (version == 0 /* V2.5 */) {
        sampling_rate /= 4;
    }

    unsigned padding = (header >> 9) & 1;

    if (layer == 3) {
        // layer I

        static const int kBitrateV1[] = {
            32, 64, 96, 128, 160, 192, 224, 256,
            288, 320, 352, 384, 416, 448
        };

        static const int kBitrateV2[] = {
            32, 48, 56, 64, 80, 96, 112, 128,
            144, 160, 176, 192, 224, 256
        };

        int bitrate =
            (version == 3 /* V1 */)
                ? kBitrateV1[bitrate_index - 1]
                : kBitrateV2[bitrate_index - 1];

        if (out_bitrate) {
            *out_bitrate = bitrate;
        }

        *frame_size = (12000 * bitrate / sampling_rate + padding) * 4;
    } else {
        // layer II or III

        static const int kBitrateV1L2[] = {
            32, 48, 56, 64, 80, 96, 112, 128,
            160, 192, 224, 256, 320, 384
        };

        static const int kBitrateV1L3[] = {
            32, 40, 48, 56, 64, 80, 96, 112,
            128, 160, 192, 224, 256, 320
        };

        static const int kBitrateV2[] = {
            8, 16, 24, 32, 40, 48, 56, 64,
            80, 96, 112, 128, 144, 160
        };

        int bitrate;
        if (version == 3 /* V1 */) {
            bitrate = (layer == 2 /* L2 */)
                ? kBitrateV1L2[bitrate_index - 1]
                : kBitrateV1L3[bitrate_index - 1];
        } else {
            // V2 (or 2.5)

            bitrate = kBitrateV2[bitrate_index - 1];
        }

        if (out_bitrate) {
            *out_bitrate = bitrate;
        }

        if (version == 3 /* V1 */) {
            *frame_size = 144000 * bitrate / sampling_rate + padding;
        } else {
            // V2 or V2.5
            *frame_size = 72000 * bitrate / sampling_rate + padding;
        }
    }

    if (out_sampling_rate) {
        *out_sampling_rate = sampling_rate;
    }

    if (out_channels) {
        int channel_mode = (header >> 6) & 3;

        *out_channels = (channel_mode == 3) ? 1 : 2;
    }

    return true;
}

status_t MPEG4Extractor::setCodecInfoFromFirstFrame(Track *track)
{
	off64_t frame_offset;
	size_t  frame_size;
	void*   frame_data = NULL;
	track->sampleTable->getMetaDataForSample(0, &frame_offset, &frame_size, (uint64_t*)NULL, NULL);
	frame_data = malloc(frame_size);
	if (NULL == frame_data){
		LOGE("malloc first frame data buffer fail!");
		return ERROR_BUFFER_TOO_SMALL;
	}
	
	if (mDataSource->readAt(
				frame_offset, frame_data, frame_size)
			< (int32_t)frame_size) {
		LOGE("read first frame fail!!");
		return ERROR_IO;
	}
	
	const char* mime;
	if (!track->meta->findCString(kKeyMIMEType, &mime))
	{
		LOGE("No mime type track!!");
		return UNKNOWN_ERROR;
	}

	if (!strcmp(mime, MEDIA_MIMETYPE_VIDEO_MPEG4))
	{
		size_t vosend;
		for (vosend=0; (vosend < 200) && (vosend < frame_size-4); vosend++)
		{
			if (0xB6010000 == *(uint32_t*)((uint8_t*)frame_data + vosend))
			{
				break;//Send VOS until VOP
			}
		}
		track->meta->setData(kKeyMPEG4VOS, 0, frame_data, vosend);
		for (uint32_t i=0; i<vosend; i++)
			LOGD("VOS[%d] = 0x%x", i, *((uint8_t *)frame_data + i));
	}

	if (!strcmp(mime, MEDIA_MIMETYPE_AUDIO_MPEG))
	{
		uint32_t header = *(uint32_t*)(frame_data);
		header = ((header >> 24) & 0xff) | ((header >> 8) & 0xff00) | ((header << 8) & 0xff0000) | ((header << 24) & 0xff000000); 
		LOGD("HEADER=0x%x", header);
		size_t  out_framesize;
		int32_t out_sampling_rate;
		int32_t out_channels;
		int32_t out_bitrate;
		if(get_mp3_info(header, &out_framesize, &out_sampling_rate, &out_channels, &out_bitrate))
		{
		    LOGD("mp3: out_framesize=%d, sample_rate=%d, channel_count=%d, out_bitrate=%d", 
			    out_framesize, out_sampling_rate, out_channels, out_bitrate);
		    track->meta->setInt32(kKeySampleRate, out_sampling_rate);
		    track->meta->setInt32(kKeyChannelCount, out_channels);
		}
		else
		{
		    LOGE("Get mp3 info fail");   // should not return error, or else the whole file can not play. 
		}
	}
	free(frame_data);
	return OK;
}

#ifdef MTK_S3D_SUPPORT
status_t MPEG4Extractor::getFirstNal(Track *track, size_t *nal_offset, size_t *nal_size)
{
	
	off64_t frame_offset;
	size_t  frame_size;

	if (NULL == nal_offset || NULL == nal_size)
		return UNKNOWN_ERROR;
	
	//Get Nal length size-->
	uint32_t type;
	const void *data;
	size_t size;
	size_t nalLengthSize;
	CHECK(track->meta->findData(kKeyAVCC, &type, &data, &size));
	
	const uint8_t *ptr = (const uint8_t *)data;
	
	CHECK(size >= 7);
	CHECK_EQ((unsigned)ptr[0], 1u);  // configurationVersion == 1
	
	// The number of bytes used to encode the length of a NAL unit.
	nalLengthSize = 1 + (ptr[4] & 3);
	//Get Nal length size<--
	
	track->sampleTable->getMetaDataForSample(0, &frame_offset, &frame_size,(uint64_t*)NULL, NULL);

	uint8_t buffer[4];
	if (mDataSource->readAt(
				frame_offset, buffer, nalLengthSize)
			< nalLengthSize) {
		LOGE("read first nal size fail!!");
		return ERROR_IO;
	}

    switch (nalLengthSize) {
        case 1:
            *nal_size = buffer[0];
			break;
        case 2:
            *nal_size = U16_AT(buffer);
			break;
        case 3:
            *nal_size = ((size_t)buffer[0] << 16) | U16_AT(&buffer[1]);
			break;
        case 4:
            *nal_size = U32_AT(buffer);
			break;
    }

	if (frame_size < nalLengthSize + *nal_size) {
		LOGE("incomplete first NAL unit.frame_size=%d, nalLengthSize=%d, nal_size=%d", frame_size, nalLengthSize, *nal_size);
		return ERROR_MALFORMED;
	}
/*
	nal_data = malloc(nal_size);

	if (NULL == nal_data) {
		LOGE("nal_data = malloc(nal_size) FAIL!!");
		return ERROR_BUFFER_TOO_SMALL;
	}
	
	if (mDataSource->readAt(
				frame_offset + nalLengthSize, nal_data, nal_size)
			< nalLengthSize) {
		LOGE("read first nal fail!!");
		return ERROR_IO;
	}
*/

	*nal_offset = frame_offset + nalLengthSize;
	LOGD("First Nal offset=%d, size=%d", *nal_offset, *nal_size);

	return OK;
	
	
}
#endif
#endif

////////////////////////////////////////////////////////////////////////////////

MPEG4Source::MPEG4Source(
        const sp<MetaData> &format,
        const sp<DataSource> &dataSource,
        int32_t timeScale,
        const sp<SampleTable> &sampleTable)
    : mFormat(format),
      mDataSource(dataSource),
      mTimescale(timeScale),
      mSampleTable(sampleTable),
      mCurrentSampleIndex(0),
      mIsAVC(false),
      mNALLengthSize(0),
      mStarted(false),
      mGroup(NULL),
      mBuffer(NULL),
      mWantsNALFragments(false),
      mSrcBuffer(NULL) {
    const char *mime;
    bool success = mFormat->findCString(kKeyMIMEType, &mime);
    CHECK(success);

    mIsAVC = !strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_AVC);

    if (mIsAVC) {
        uint32_t type;
        const void *data;
        size_t size;
        CHECK(format->findData(kKeyAVCC, &type, &data, &size));

        const uint8_t *ptr = (const uint8_t *)data;

        CHECK(size >= 7);
        CHECK_EQ((unsigned)ptr[0], 1u);  // configurationVersion == 1

        // The number of bytes used to encode the length of a NAL unit.
        mNALLengthSize = 1 + (ptr[4] & 3);
    }
}

MPEG4Source::~MPEG4Source() {
    if (mStarted) {
        stop();
    }
}

status_t MPEG4Source::start(MetaData *params) {
    Mutex::Autolock autoLock(mLock);

    CHECK(!mStarted);

    int32_t val;
    if (params && params->findInt32(kKeyWantsNALFragments, &val)
        && val != 0) {
        mWantsNALFragments = true;
    } else {
        mWantsNALFragments = false;
    }

    mGroup = new MediaBufferGroup;

    int32_t max_size;
    CHECK(mFormat->findInt32(kKeyMaxInputSize, &max_size));

    mGroup->add_buffer(new MediaBuffer(max_size));

    mSrcBuffer = new uint8_t[max_size];

    mStarted = true;

    return OK;
}

status_t MPEG4Source::stop() {
    Mutex::Autolock autoLock(mLock);

    CHECK(mStarted);

    if (mBuffer != NULL) {
        mBuffer->release();
        mBuffer = NULL;
    }

    delete[] mSrcBuffer;
    mSrcBuffer = NULL;

    delete mGroup;
    mGroup = NULL;

    mStarted = false;
    mCurrentSampleIndex = 0;

    return OK;
}

sp<MetaData> MPEG4Source::getFormat() {
    Mutex::Autolock autoLock(mLock);

    return mFormat;
}

size_t MPEG4Source::parseNALSize(const uint8_t *data) const {
    switch (mNALLengthSize) {
        case 1:
            return *data;
        case 2:
            return U16_AT(data);
        case 3:
            return ((size_t)data[0] << 16) | U16_AT(&data[1]);
        case 4:
            return U32_AT(data);
    }

    // This cannot happen, mNALLengthSize springs to life by adding 1 to
    // a 2-bit integer.
    CHECK(!"Should not be here.");

    return 0;
}

status_t MPEG4Source::read(
        MediaBuffer **out, const ReadOptions *options) {
    Mutex::Autolock autoLock(mLock);

    CHECK(mStarted);
#ifndef ANDROID_DEFAULT_MODE
    if (out != NULL)
#endif
    *out = NULL;

    int64_t targetSampleTimeUs = -1;
#ifndef ANDROID_DEFAULT_CODE//added by hai.li to support track time offset
	int64_t startTimeOffsetUs = ((int64_t)mSampleTable->getStartTimeOffset())*1000000/mTimescale;
#endif	
    int64_t seekTimeUs;
    ReadOptions::SeekMode mode;
#ifndef ANDROID_DEFAULT_CODE
	bool isTryRead = false;
#endif
    if (options && options->getSeekTo(&seekTimeUs, &mode)) {
#ifndef ANDROID_DEFAULT_CODE//added by hai.li to support track time offset
//		LOGE("SEEK TIME1=%lld", seekTimeUs);
		
		LOGD("seekTimeUs=%lld, seekMode=%d", seekTimeUs, mode);

		if (ReadOptions::SEEK_TRY_READ == mode) {
			isTryRead = true;
			mode = ReadOptions::SEEK_CLOSEST_SYNC;
		}
		if (startTimeOffsetUs != 0)
		{
			if (seekTimeUs < startTimeOffsetUs)
			{
				seekTimeUs = 0;
			}
			else
			{
				seekTimeUs -= startTimeOffsetUs;
			}
					
		}
//		LOGE("SEEK TIME2=%lld", seekTimeUs);
#endif
        uint32_t findFlags = 0;
        switch (mode) {
            case ReadOptions::SEEK_PREVIOUS_SYNC:
                findFlags = SampleTable::kFlagBefore;
                break;
            case ReadOptions::SEEK_NEXT_SYNC:
                findFlags = SampleTable::kFlagAfter;
                break;
            case ReadOptions::SEEK_CLOSEST_SYNC:
            case ReadOptions::SEEK_CLOSEST:
                findFlags = SampleTable::kFlagClosest;
                break;
            default:
                CHECK(!"Should not be here.");
                break;
        }

        uint32_t sampleIndex;
#ifdef ENABLE_PERF_JUMP_KEY_MECHANISM
		if (ReadOptions::SEEK_NEXT_SYNC == mode) {
			status_t err = mSampleTable->findSyncSampleNear(
					mCurrentSampleIndex, &sampleIndex, SampleTable::kFlagAfter);
			if (err != OK) {
				if (err == ERROR_OUT_OF_RANGE) {
					err = ERROR_END_OF_STREAM;
				}
				return err;
			}
			syncSampleIndex = sampleIndex;
			LOGD("SEEK_JUMP_NEXT_KEY, mCurrentSampleIndex=%d, sampleIndex=%d", mCurrentSampleIndex, sampleIndex);
		}
		else {
#endif
#ifndef ANDROID_DEFAULT_CODE//hai.li for Issue: ALPS32414
		status_t err = mSampleTable->findSampleAtTime(
				(uint64_t)(seekTimeUs * mTimescale + 500000ll) / 1000000,
				&sampleIndex, findFlags);
#else
        status_t err = mSampleTable->findSampleAtTime(
                seekTimeUs * mTimescale / 1000000,
                &sampleIndex, findFlags);
#endif
        if (mode == ReadOptions::SEEK_CLOSEST) {
            // We found the closest sample already, now we want the sync
            // sample preceding it (or the sample itself of course), even
            // if the subsequent sync sample is closer.
            findFlags = SampleTable::kFlagBefore;
        }

        uint32_t syncSampleIndex;
        if (err == OK) {
            err = mSampleTable->findSyncSampleNear(
                    sampleIndex, &syncSampleIndex, findFlags);
        }
#ifndef ANDROID_DEFAULT_CODE
        uint64_t sampleTime;
#else
        uint32_t sampleTime;
#endif
        if (err == OK) {
            err = mSampleTable->getMetaDataForSample(
                    sampleIndex, NULL, NULL, &sampleTime);
        }

        if (err != OK) {
            if (err == ERROR_OUT_OF_RANGE) {
                // An attempt to seek past the end of the stream would
                // normally cause this ERROR_OUT_OF_RANGE error. Propagating
                // this all the way to the MediaPlayer would cause abnormal
                // termination. Legacy behaviour appears to be to behave as if
                // we had seeked to the end of stream, ending normally.
                err = ERROR_END_OF_STREAM;
            }
            return err;
        }
#ifdef ENABLE_PERF_JUMP_KEY_MECHANISM
		}
#endif

#ifndef ANDROID_DEFAULT_CODE//hai.li
#ifdef ENABLE_PERF_JUMP_KEY_MECHANISM
		if (mode == ReadOptions::SEEK_CLOSEST ||
			mode == ReadOptions::SEEK_JUMP_NEXT_KEY) 
#else
        if (mode == ReadOptions::SEEK_CLOSEST) 
#endif
		{
#ifndef ANDROID_DEFAULT_CODE
	        uint64_t sampleTime;
#else
	        uint32_t sampleTime;
#endif
	        CHECK_EQ((status_t)OK, mSampleTable->getMetaDataForSample(
                    sampleIndex, NULL, NULL, &sampleTime));
            targetSampleTimeUs = (sampleTime * 1000000ll) / mTimescale + startTimeOffsetUs;
			LOGE("targetSampleTimeUs=%lld", targetSampleTimeUs);
        }
#else
        if (mode == ReadOptions::SEEK_CLOSEST) {
            targetSampleTimeUs = (sampleTime * 1000000ll) / mTimescale;
        }
#endif

#if 0
        uint32_t syncSampleTime;
        CHECK_EQ(OK, mSampleTable->getMetaDataForSample(
                    syncSampleIndex, NULL, NULL, &syncSampleTime));

        LOGI("seek to time %lld us => sample at time %lld us, "
             "sync sample at time %lld us",
             seekTimeUs,
             sampleTime * 1000000ll / mTimescale,
             syncSampleTime * 1000000ll / mTimescale);
#endif

        mCurrentSampleIndex = syncSampleIndex;
        if (mBuffer != NULL) {
            mBuffer->release();
            mBuffer = NULL;
        }

        // fall through
    }

    off64_t offset;
    size_t size;
#ifndef ANDROID_DEFAULT_CODE
    uint64_t cts;
#else
    uint32_t cts;
#endif
    bool isSyncSample;
    bool newBuffer = false;
    if (mBuffer == NULL) {
        newBuffer = true;

        status_t err =
            mSampleTable->getMetaDataForSample(
                    mCurrentSampleIndex, &offset, &size, &cts, &isSyncSample);

        if (err != OK) {
#ifndef ANDROID_DEFAULT_CODE//added by hai.li for Issue:ALPS34394
			if (err == ERROR_OUT_OF_RANGE)
				err = ERROR_END_OF_STREAM;//Awesomeplayer only can handle this as eos
#endif
            return err;
        }

#ifndef ANDROID_DEFAULT_CODE
			if (isTryRead) {
				LOGD("Try read");
				ssize_t result =
					mDataSource->readAt(offset, NULL, size);
				if ((size_t)result == size) {
					LOGD("Try read return ok");
					return OK;
				} else {
					LOGD("Try read fail!");
					return INFO_TRY_READ_FAIL;
				}
			}
#endif
        err = mGroup->acquire_buffer(&mBuffer);

        if (err != OK) {
            CHECK(mBuffer == NULL);
            return err;
        }
    }

    if (!mIsAVC || mWantsNALFragments) {
        if (newBuffer) {
            ssize_t num_bytes_read =
                mDataSource->readAt(offset, (uint8_t *)mBuffer->data(), size);

            if (num_bytes_read < (ssize_t)size) {
                mBuffer->release();
                mBuffer = NULL;

                return ERROR_IO;
            }

            CHECK(mBuffer != NULL);
            mBuffer->set_range(0, size);
            mBuffer->meta_data()->clear();
#ifndef ANDROID_DEFAULT_CODE//modified by hai.li to support track time offset
            	mBuffer->meta_data()->setInt64(
                	   kKeyTime, ((int64_t)(cts+mSampleTable->getStartTimeOffset())* 1000000) / mTimescale);
#else
            mBuffer->meta_data()->setInt64(
                    kKeyTime, ((int64_t)cts * 1000000) / mTimescale);
#endif
            if (targetSampleTimeUs >= 0) {
                mBuffer->meta_data()->setInt64(
                        kKeyTargetTime, targetSampleTimeUs);
            }

            if (isSyncSample) {
                mBuffer->meta_data()->setInt32(kKeyIsSyncFrame, 1);
            }

            ++mCurrentSampleIndex;
        }

        if (!mIsAVC) {
            *out = mBuffer;
            mBuffer = NULL;

            return OK;
        }

        // Each NAL unit is split up into its constituent fragments and
        // each one of them returned in its own buffer.
#ifdef ANDROID_DEFAULT_CODE  //ALPS00238811
        CHECK(mBuffer->range_length() >= mNALLengthSize);
#endif
        const uint8_t *src =
            (const uint8_t *)mBuffer->data() + mBuffer->range_offset();

        size_t nal_size = parseNALSize(src);
#ifndef ANDROID_DEFAULT_CODE
		if ((mBuffer->range_length() < mNALLengthSize + nal_size) ||
				(mNALLengthSize + nal_size < mNALLengthSize))//When uint type nal_size is very large, e.g. 0xffff or 0xffffffff, the summary is small. In this case, there are some problems in later flow.
#else
		if (mBuffer->range_length() < mNALLengthSize + nal_size) 
#endif
		{
			LOGW("incomplete NAL unit.mBuffer->range_length()=%d, mNALLengthSize=%d, nal_size=0x%8.8x", mBuffer->range_length(), mNALLengthSize, nal_size);

#ifndef ANDROID_DEFAULT_CODE
			if (mBuffer->range_length() < mNALLengthSize) {
				*out = mBuffer;
				mBuffer = NULL;
				
				return OK;
			}
			else {
				mBuffer->set_range(mBuffer->range_offset() + mNALLengthSize, mBuffer->range_length() - mNALLengthSize);
				*out = mBuffer;
				mBuffer = NULL;
				return OK;
			}
#else
            mBuffer->release();
            mBuffer = NULL;

            return ERROR_MALFORMED;
#endif
        }

        MediaBuffer *clone = mBuffer->clone();
        CHECK(clone != NULL);
        clone->set_range(mBuffer->range_offset() + mNALLengthSize, nal_size);

        CHECK(mBuffer != NULL);
        mBuffer->set_range(
                mBuffer->range_offset() + mNALLengthSize + nal_size,
                mBuffer->range_length() - mNALLengthSize - nal_size);
#ifdef ANDROID_DEFAULT_CODE  //ALPS00238811
        if (mBuffer->range_length() == 0) 
#else
		if ((mBuffer->range_length() < mNALLengthSize) || (0 == nal_size)) 
#endif
		{
            mBuffer->release();
            mBuffer = NULL;
        }

        *out = clone;

        return OK;
    } else {
        // Whole NAL units are returned but each fragment is prefixed by
        // the start code (0x00 00 00 01).
        ssize_t num_bytes_read = 0;
        int32_t drm = 0;
        bool usesDRM = (mFormat->findInt32(kKeyIsDRM, &drm) && drm != 0);
        if (usesDRM) {
            num_bytes_read =
                mDataSource->readAt(offset, (uint8_t*)mBuffer->data(), size);
        } else {
            num_bytes_read = mDataSource->readAt(offset, mSrcBuffer, size);
        }

        if (num_bytes_read < (ssize_t)size) {
            mBuffer->release();
            mBuffer = NULL;

            return ERROR_IO;
        }

        if (usesDRM) {
            CHECK(mBuffer != NULL);
            mBuffer->set_range(0, size);

        } else {
            uint8_t *dstData = (uint8_t *)mBuffer->data();
            size_t srcOffset = 0;
            size_t dstOffset = 0;

            while (srcOffset < size) {
                bool isMalFormed = (srcOffset + mNALLengthSize > size);
                size_t nalLength = 0;
                if (!isMalFormed) {
                    nalLength = parseNALSize(&mSrcBuffer[srcOffset]);
                    srcOffset += mNALLengthSize;
                    isMalFormed = srcOffset + nalLength > size;
                }

                if (isMalFormed) {
                    LOGE("Video is malformed");
                    mBuffer->release();
                    mBuffer = NULL;
                    return ERROR_MALFORMED;
                }

                if (nalLength == 0) {
                    continue;
                }

                CHECK(dstOffset + 4 <= mBuffer->size());

                dstData[dstOffset++] = 0;
                dstData[dstOffset++] = 0;
                dstData[dstOffset++] = 0;
                dstData[dstOffset++] = 1;
                memcpy(&dstData[dstOffset], &mSrcBuffer[srcOffset], nalLength);
                srcOffset += nalLength;
                dstOffset += nalLength;
            }
            CHECK_EQ(srcOffset, size);
            CHECK(mBuffer != NULL);
            mBuffer->set_range(0, dstOffset);
        }

        mBuffer->meta_data()->clear();
#ifndef ANDROID_DEFAULT_CODE//modified by hai.li to support track time offset
        	mBuffer->meta_data()->setInt64(
            	    kKeyTime, ((int64_t)(cts+mSampleTable->getStartTimeOffset()) * 1000000) / mTimescale);
#else
        mBuffer->meta_data()->setInt64(
                kKeyTime, ((int64_t)cts * 1000000) / mTimescale);
#endif
        if (targetSampleTimeUs >= 0) {
            mBuffer->meta_data()->setInt64(
                    kKeyTargetTime, targetSampleTimeUs);
        }

        if (isSyncSample) {
            mBuffer->meta_data()->setInt32(kKeyIsSyncFrame, 1);
        }

        ++mCurrentSampleIndex;

        *out = mBuffer;
        mBuffer = NULL;

        return OK;
    }
}

MPEG4Extractor::Track *MPEG4Extractor::findTrackByMimePrefix(
        const char *mimePrefix) {
    for (Track *track = mFirstTrack; track != NULL; track = track->next) {
        const char *mime;
        if (track->meta != NULL
                && track->meta->findCString(kKeyMIMEType, &mime)
                && !strncasecmp(mime, mimePrefix, strlen(mimePrefix))) {
            return track;
        }
    }

    return NULL;
}

static bool LegacySniffMPEG4(
        const sp<DataSource> &source, String8 *mimeType, float *confidence) {
    uint8_t header[8];

    ssize_t n = source->readAt(4, header, sizeof(header));
    if (n < (ssize_t)sizeof(header)) {
        return false;
    }

    if (!memcmp(header, "ftyp3gp", 7) || !memcmp(header, "ftypmp42", 8)
        || !memcmp(header, "ftyp3gr6", 8) || !memcmp(header, "ftyp3gs6", 8)
        || !memcmp(header, "ftyp3ge6", 8) || !memcmp(header, "ftyp3gg6", 8)
        || !memcmp(header, "ftypisom", 8) || !memcmp(header, "ftypM4V ", 8)
        || !memcmp(header, "ftypM4A ", 8) || !memcmp(header, "ftypf4v ", 8)
        || !memcmp(header, "ftypkddi", 8) || !memcmp(header, "ftypM4VP", 8)) {
        *mimeType = MEDIA_MIMETYPE_CONTAINER_MPEG4;
        *confidence = 0.4;

        return true;
    }

    return false;
}

static bool isCompatibleBrand(uint32_t fourcc) {
    static const uint32_t kCompatibleBrands[] = {
        FOURCC('i', 's', 'o', 'm'),
        FOURCC('i', 's', 'o', '2'),
        FOURCC('a', 'v', 'c', '1'),
        FOURCC('3', 'g', 'p', '4'),
        FOURCC('m', 'p', '4', '1'),
        FOURCC('m', 'p', '4', '2'),

        // Won't promise that the following file types can be played.
        // Just give these file types a chance.
        FOURCC('q', 't', ' ', ' '),  // Apple's QuickTime
        FOURCC('M', 'S', 'N', 'V'),  // Sony's PSP

        FOURCC('3', 'g', '2', 'a'),  // 3GPP2
        FOURCC('3', 'g', '2', 'b'),
    };

    for (size_t i = 0;
         i < sizeof(kCompatibleBrands) / sizeof(kCompatibleBrands[0]);
         ++i) {
        if (kCompatibleBrands[i] == fourcc) {
            return true;
        }
    }

    return false;
}

// Attempt to actually parse the 'ftyp' atom and determine if a suitable
// compatible brand is present.
// Also try to identify where this file's metadata ends
// (end of the 'moov' atom) and report it to the caller as part of
// the metadata.
static bool BetterSniffMPEG4(
        const sp<DataSource> &source, String8 *mimeType, float *confidence,
        sp<AMessage> *meta) {
    // We scan up to 128 bytes to identify this file as an MP4.
    static const off64_t kMaxScanOffset = 128ll;

    off64_t offset = 0ll;
    bool foundGoodFileType = false;
    off64_t moovAtomEndOffset = -1ll;
    bool done = false;

#ifndef ANDROID_DEFAULT_CODE//hai.li: some files have no 'ftyp' atom, but they can be played in 2.2 version
    uint8_t header[12];
    // If type is not ftyp,mdata,moov or free, return false directly. Or else, it may be mpeg4 file.
    if (source->readAt(0, header, 12) != 12
		    || (memcmp("ftyp", &header[4], 4) && memcmp("mdat", &header[4], 4) 
			    && memcmp("moov", &header[4], 4) && memcmp("free", &header[4], 4))) {
	    //LOGE("return false, type=0x%8.8x", *((uint32_t *)&header[4]));
	    return false;
    }
    *mimeType = MEDIA_MIMETYPE_CONTAINER_MPEG4;
    *confidence = 0.1f;
#endif  //ANDROID_DEFAULT_CODE

    while (!done && offset < kMaxScanOffset) {
        uint32_t hdr[2];
        if (source->readAt(offset, hdr, 8) < 8) {
            return false;
        }

        uint64_t chunkSize = ntohl(hdr[0]);
        uint32_t chunkType = ntohl(hdr[1]);
        off64_t chunkDataOffset = offset + 8;

        if (chunkSize == 1) {
            if (source->readAt(offset + 8, &chunkSize, 8) < 8) {
                return false;
            }

            chunkSize = ntoh64(chunkSize);
            chunkDataOffset += 8;

            if (chunkSize < 16) {
                // The smallest valid chunk is 16 bytes long in this case.
                return false;
            }
        } else if (chunkSize < 8) {
            // The smallest valid chunk is 8 bytes long.
            return false;
        }

        off64_t chunkDataSize = offset + chunkSize - chunkDataOffset;

        switch (chunkType) {
            case FOURCC('f', 't', 'y', 'p'):
            {
                if (chunkDataSize < 8) {
                    return false;
                }

                uint32_t numCompatibleBrands = (chunkDataSize - 8) / 4;
                for (size_t i = 0; i < numCompatibleBrands + 2; ++i) {
                    if (i == 1) {
                        // Skip this index, it refers to the minorVersion,
                        // not a brand.
                        continue;
                    }

                    uint32_t brand;
                    if (source->readAt(
                                chunkDataOffset + 4 * i, &brand, 4) < 4) {
                        return false;
                    }

                    brand = ntohl(brand);

                    if (isCompatibleBrand(brand)) {
                        foundGoodFileType = true;
                        break;
                    }
                }
	        if (!foundGoodFileType) {
#ifndef ANDROID_DEFAULT_CODE  //ALPS00112506 Don't use isCompatibleBrand to judge whether play or not
	            LOGW("Warning:ftyp brands is not isCompatibleBrand 1");
#else //ANDROID_DEFAULT_CODE
		    return false;
#endif
		}

                break;
            }

            case FOURCC('m', 'o', 'o', 'v'):
            {
                moovAtomEndOffset = offset + chunkSize;

                done = true;
                break;
            }

            default:
                break;
        }

        offset += chunkSize;
    }

#ifndef ANDROID_DEFAULT_CODE  //ALPS00112506 Don't use isCompatibleBrand to judge whether play or not
    //If foundGoodFileType, set confidence from 0.1f to 0.4f. Or else confidence is 0.1f
    if (foundGoodFileType) {
	    *mimeType = MEDIA_MIMETYPE_CONTAINER_MPEG4;
	    *confidence = 0.4f;
    }
#else  // ANDROID_DEFAULT_CODE 
    if (!foundGoodFileType) {
	    return false;
    }

    *mimeType = MEDIA_MIMETYPE_CONTAINER_MPEG4;
    *confidence = 0.4f;
#endif

    if (moovAtomEndOffset >= 0) {
        *meta = new AMessage;
        (*meta)->setInt64("meta-data-size", moovAtomEndOffset);

        LOGV("found metadata size: %lld", moovAtomEndOffset);
    }

    return true;
}

bool SniffMPEG4(
        const sp<DataSource> &source, String8 *mimeType, float *confidence,
        sp<AMessage> *meta) {
    if (BetterSniffMPEG4(source, mimeType, confidence, meta)) {
        return true;
    }

    if (LegacySniffMPEG4(source, mimeType, confidence)) {
        LOGW("Identified supported mpeg4 through LegacySniffMPEG4.");
        return true;
    }

    return false;
}

}  // namespace android

