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
#define LOG_TAG "MatroskaExtractor"
#include <utils/Log.h>

#include "MatroskaExtractor.h"

#include "mkvparser.hpp"

#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>
#include <utils/String8.h>
#include "../include/avc_utils.h"
#include <media/stagefright/foundation/ABuffer.h>

#ifndef ANDROID_DEFAULT_CODE
#include <vdec_drv_if.h>
#include "val_types.h"

// big endian fourcc
#define BFOURCC(c1, c2, c3, c4) \
    (c4 << 24 | c3 << 16 | c2 << 8 | c1)
#endif

namespace android {

#ifndef ANDROID_DEFAULT_CODE
#define MKV_RIFF_WAVE_FORMAT_PCM            (0x0001)
#define MKV_RIFF_WAVE_FORMAT_ALAW           (0x0006)
#define MKV_RIFF_WAVE_FORMAT_MULAW          (0x0007)
#define MKV_RIFF_WAVE_FORMAT_MPEGL12        (0x0050)
#define MKV_RIFF_WAVE_FORMAT_MPEGL3         (0x0055)
#define MKV_RIFF_WAVE_FORMAT_AMR_NB         (0x0057)
#define MKV_RIFF_WAVE_FORMAT_AMR_WB         (0x0058)
#define MKV_RIFF_WAVE_FORMAT_AAC            (0x00ff)
#define MKV_RIFF_IBM_FORMAT_MULAW           (0x0101)
#define MKV_RIFF_IBM_FORMAT_ALAW            (0x0102)
#define MKV_RIFF_WAVE_FORMAT_WMAV1          (0x0160)
#define MKV_RIFF_WAVE_FORMAT_WMAV2          (0x0161)
#define MKV_RIFF_WAVE_FORMAT_WMAV3          (0x0162)
#define MKV_RIFF_WAVE_FORMAT_WMAV3_L        (0x0163)
#define MKV_RIFF_WAVE_FORMAT_AAC_AC         (0x4143)
#define MKV_RIFF_WAVE_FORMAT_VORBIS         (0x566f)
#define MKV_RIFF_WAVE_FORMAT_VORBIS1        (0x674f)
#define MKV_RIFF_WAVE_FORMAT_VORBIS2        (0x6750)
#define MKV_RIFF_WAVE_FORMAT_VORBIS3        (0x6751)
#define MKV_RIFF_WAVE_FORMAT_VORBIS1PLUS    (0x676f)
#define MKV_RIFF_WAVE_FORMAT_VORBIS2PLUS    (0x6770)
#define MKV_RIFF_WAVE_FORMAT_VORBIS3PLUS    (0x6771)
#define MKV_RIFF_WAVE_FORMAT_AAC_pm         (0x706d)
#define MKV_RIFF_WAVE_FORMAT_GSM_AMR_CBR    (0x7A21)
#define MKV_RIFF_WAVE_FORMAT_GSM_AMR_VBR    (0x7A22)

static const uint32_t kMP3HeaderMask = 0xfffe0c00;//0xfffe0cc0 add by zhihui zhang no consider channel mode
static const char *MKVwave2MIME(uint16_t id) {
    switch (id) {
        case  MKV_RIFF_WAVE_FORMAT_AMR_NB:
        case  MKV_RIFF_WAVE_FORMAT_GSM_AMR_CBR:
        case  MKV_RIFF_WAVE_FORMAT_GSM_AMR_VBR:
            return MEDIA_MIMETYPE_AUDIO_AMR_NB;

        case  MKV_RIFF_WAVE_FORMAT_AMR_WB:
            return MEDIA_MIMETYPE_AUDIO_AMR_WB;

        case  MKV_RIFF_WAVE_FORMAT_AAC:
        case  MKV_RIFF_WAVE_FORMAT_AAC_AC:
        case  MKV_RIFF_WAVE_FORMAT_AAC_pm:       
            return MEDIA_MIMETYPE_AUDIO_AAC;

        case  MKV_RIFF_WAVE_FORMAT_VORBIS:
        case  MKV_RIFF_WAVE_FORMAT_VORBIS1:
        case  MKV_RIFF_WAVE_FORMAT_VORBIS2:        
        case  MKV_RIFF_WAVE_FORMAT_VORBIS3:
        case  MKV_RIFF_WAVE_FORMAT_VORBIS1PLUS:
        case  MKV_RIFF_WAVE_FORMAT_VORBIS2PLUS:
        case  MKV_RIFF_WAVE_FORMAT_VORBIS3PLUS:
            return MEDIA_MIMETYPE_AUDIO_VORBIS;

        case  MKV_RIFF_WAVE_FORMAT_MPEGL12:
        case  MKV_RIFF_WAVE_FORMAT_MPEGL3:
            return MEDIA_MIMETYPE_AUDIO_MPEG;
/*
        case MKV_RIFF_WAVE_FORMAT_MULAW:
        case MKV_RIFF_IBM_FORMAT_MULAW:
            return MEDIA_MIMETYPE_AUDIO_G711_MLAW;

        case MKV_RIFF_WAVE_FORMAT_ALAW:
        case MKV_RIFF_IBM_FORMAT_ALAW:
            return MEDIA_MIMETYPE_AUDIO_G711_ALAW;

        case MKV_RIFF_WAVE_FORMAT_PCM:
            return MEDIA_MIMETYPE_AUDIO_RAW;
*/
        default:
            LOGW("unknown wave %x", id);
            return "";
    };
}



static const uint32_t AACSampleFreqTable[16] =
{
    96000, /* 96000 Hz */
    88200, /* 88200 Hz */
    64000, /* 64000 Hz */
    48000, /* 48000 Hz */
    44100, /* 44100 Hz */
    32000, /* 32000 Hz */
    24000, /* 24000 Hz */
    22050, /* 22050 Hz */
    16000, /* 16000 Hz */
    12000, /* 12000 Hz */
    11025, /* 11025 Hz */
    8000, /*  8000 Hz */
    -1, /* future use */
    -1, /* future use */
    -1, /* future use */
    -1  /* escape value */
};

static bool findAACSampleFreqIndex(uint32_t freq, uint8_t &index)
{
	uint8_t i;
	uint8_t num = sizeof(AACSampleFreqTable)/sizeof(AACSampleFreqTable[0]);
	for (i=0; i < num; i++) {
		if (freq == AACSampleFreqTable[i])
			break;
	}
	if (i > 11)
		return false;

	index = i;
	return true;
}


static const char *BMKVFourCC2MIME(uint32_t fourcc) {
    switch (fourcc) {
        case BFOURCC('m', 'p', '4', 'a'):
            return MEDIA_MIMETYPE_AUDIO_AAC;

        case BFOURCC('s', 'a', 'm', 'r'):
            return MEDIA_MIMETYPE_AUDIO_AMR_NB;

        case BFOURCC('s', 'a', 'w', 'b'):
            return MEDIA_MIMETYPE_AUDIO_AMR_WB;

        case BFOURCC('x', 'v', 'i', 'd'):
        case BFOURCC('X', 'V', 'I', 'D'):
        case BFOURCC('d', 'i', 'v', 'x'):
        case BFOURCC('D', 'I', 'V', 'X'):
        case BFOURCC('D', 'X', '5', '0'):
        case BFOURCC('m', 'p', '4', 'v'):
            return MEDIA_MIMETYPE_VIDEO_MPEG4;

        case BFOURCC('s', '2', '6', '3'):
        case BFOURCC('H', '2', '6', '3'):
        case BFOURCC('h', '2', '6', '3'):
            return MEDIA_MIMETYPE_VIDEO_H263;

        case BFOURCC('a', 'v', 'c', '1'):
        case BFOURCC('A', 'V', 'C', '1'):
        case BFOURCC('H', '2', '6', '4'):
        case BFOURCC('h', '2', '6', '4'):
            return MEDIA_MIMETYPE_VIDEO_AVC;

        default:
            LOGW("unknown fourcc 0x%8.8x", fourcc);
            return "";
    }
}

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

static int mkv_mp3HeaderStartAt(const uint8_t *start, int length, uint32_t header) {
    uint32_t code = 0;
    int i = 0;

    for(i=0; i<length; i++){
		//LOGD("start[%d]=%x", i, start[i]);
        code = (code<<8) + start[i];
		//LOGD("code=0x%8.8x, mask=0x%8.8x", code, kMP3HeaderMask);
        if ((code & kMP3HeaderMask) == (header & kMP3HeaderMask)) {
            // some files has no seq start code
            return i - 3;
        }
    }

    return -1;
}
#endif

struct DataSourceReader : public mkvparser::IMkvReader {
    DataSourceReader(const sp<DataSource> &source)
        : mSource(source) {
    }

    virtual int Read(long long position, long length, unsigned char* buffer) {
        CHECK(position >= 0);
        CHECK(length >= 0);

        if (length == 0) {
            return 0;
        }

        ssize_t n = mSource->readAt(position, buffer, length);

        if (n <= 0) {
		LOGE("readAt %d bytes, Read return -1", n);
		LOGE("position= %d, length= %d", position, length);
		return -1;
            //modify by vend_am00038 for solving ALPS00045653 
            //exit(0);
        }

        return 0;
    }

    virtual int Length(long long* total, long long* available) {
        off64_t size;
        if (mSource->getSize(&size) != OK) {
            *total = -1;
            *available = (long long)((1ull << 63) - 1);

            return -1;
        }

        if (total) {
            *total = size;
        }

        if (available) {
            *available = size;
        }

        return 0;
    }

private:
    sp<DataSource> mSource;

    DataSourceReader(const DataSourceReader &);
    DataSourceReader &operator=(const DataSourceReader &);
};

////////////////////////////////////////////////////////////////////////////////

struct BlockIterator {
    BlockIterator(MatroskaExtractor *extractor, unsigned long trackNum);

    bool eos() const;

    void advance();
	//added by vend_am00033 start for seeking backward
#ifndef ANDROID_DEFAULT_CODE
	void backward();
	bool backward_eos(const mkvparser::Cluster*, const mkvparser::BlockEntry*);
#endif
	//added by vend_am00033 end
    void reset();
    void seek(int64_t seekTimeUs);

    const mkvparser::Block *block() const;
    int64_t blockTimeUs() const;

private:
    MatroskaExtractor *mExtractor;
    unsigned long mTrackNum;
#ifndef ANDROID_DEFAULT_CODE
	unsigned long mTrackType;
#endif

    const mkvparser::Cluster *mCluster;
    const mkvparser::BlockEntry *mBlockEntry;
    long mBlockEntryIndex;

    void advance_l();

    BlockIterator(const BlockIterator &);
    BlockIterator &operator=(const BlockIterator &);
};

struct MatroskaSource : public MediaSource {
    MatroskaSource(
            const sp<MatroskaExtractor> &extractor, size_t index);

    virtual status_t start(MetaData *params);
    virtual status_t stop();

    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options);

protected:
    virtual ~MatroskaSource();

private:
    enum Type {
        AVC,
        AAC,
#ifndef ANDROID_DEFAULT_CODE
		VP8,
		VORBIS,
		MPEG4,
		RV,
		MP2_3,
		COOK,
#endif
        OTHER
    };

    sp<MatroskaExtractor> mExtractor;
    size_t mTrackIndex;
    Type mType;
    bool mIsAudio;
    BlockIterator mBlockIter;
    size_t mNALSizeLen;  // for type AVC
    List<MediaBuffer *> mPendingFrames;

    status_t advance();

    status_t readBlock();
    void clearPendingFrames();

    MatroskaSource(const MatroskaSource &);
    MatroskaSource &operator=(const MatroskaSource &);

#ifndef ANDROID_DEFAULT_CODE
	status_t findMP3Header(uint32_t *header);
	unsigned char* mTrackContentAddData;
	size_t mTrackContentAddDataSize;
	bool mNewFrame;
	int64_t	mCurrentTS;
	bool mFirstFrame;
	uint32_t mMP3Header;
public:
	void setCodecInfoFromFirstFrame();
#endif

};

MatroskaSource::MatroskaSource(
        const sp<MatroskaExtractor> &extractor, size_t index)
    : mExtractor(extractor),
      mTrackIndex(index),
      mType(OTHER),
      mIsAudio(false),
      mBlockIter(mExtractor.get(),
                 mExtractor->mTracks.itemAt(index).mTrackNum),
      mNALSizeLen(0) {
#ifndef ANDROID_DEFAULT_CODE
	mCurrentTS = 0;
	mFirstFrame = true;
	(mExtractor->mTracks.itemAt(index)).mTrack->GetContentAddInfo(&mTrackContentAddData, &mTrackContentAddDataSize);
#endif
	sp<MetaData> meta = mExtractor->mTracks.itemAt(index).mMeta;

    const char *mime;
    CHECK(meta->findCString(kKeyMIMEType, &mime));
    mIsAudio = !strncasecmp("audio/", mime, 6);

    if (!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_AVC)) {
        mType = AVC;
        uint32_t dummy;
        const uint8_t *avcc;
        size_t avccSize;
#ifndef ANDROID_DEFAULT_CODE
		if (!meta->findData(kKeyAVCC, &dummy, (const void **)&avcc, &avccSize))
		{
			sp<MetaData> metadata = NULL;
			while (metadata == NULL)
			{
				clearPendingFrames();
			    while (mPendingFrames.empty()) 
				{
			        status_t err = readBlock();

			        if (err != OK) 
					{
			            clearPendingFrames();
			            break;
			        }
			    }
				
				if(!mPendingFrames.empty())
				{
					MediaBuffer *buffer = *mPendingFrames.begin();
					sp < ABuffer >  accessUnit = new ABuffer(buffer->range_length());
					LOGD("bigbuf->range_length() = %d",buffer->range_length());
					memcpy(accessUnit->data(),buffer->data(),buffer->range_length());
					metadata = MakeAVCCodecSpecificData(accessUnit);
				}
			}
	        CHECK(metadata->findData(
	                    kKeyAVCC, &dummy, (const void **)&avcc, &avccSize));
			LOGD("avccSize = %d ",avccSize);
	        CHECK_GE(avccSize, 5u);
			meta->setData(kKeyAVCC, 0, avcc, avccSize);
			mBlockIter.reset();
			clearPendingFrames();
		}
#endif
        CHECK(meta->findData(
                    kKeyAVCC, &dummy, (const void **)&avcc, &avccSize));

        CHECK_GE(avccSize, 5u);

        mNALSizeLen = 1 + (avcc[4] & 3);
        LOGV("mNALSizeLen = %d", mNALSizeLen);
    } else if (!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_AAC)) {
        mType = AAC;
    }
#ifndef ANDROID_DEFAULT_CODE
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_VPX))
	{
		mType = VP8;
	}	
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_VORBIS))
	{
		mType = VORBIS;
	}
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_MPEG4))
	{
		mType = MPEG4;
	}	
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_REAL_VIDEO))
	{
		mType = RV;
	}
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_MPEG))
	{
		mType = MP2_3;
		
		if (findMP3Header(&mMP3Header) != OK)
		{
			LOGW("No mp3 header found");
		}
		LOGD("mMP3Header=0x%8.8x", mMP3Header);
	}	
	else if (!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_REAL_AUDIO))
	{
		mType = COOK;
	}
#endif
}

MatroskaSource::~MatroskaSource() {
    clearPendingFrames();
}

#ifndef ANDROID_DEFAULT_CODE
status_t MatroskaSource::findMP3Header(uint32_t * header)
{
	if (header != NULL)
		*header = 0;
	
	uint32_t code = 0;
	while (0 == *header) {
		while (mPendingFrames.empty()) 
		{
			status_t err = readBlock();
		
			if (err != OK) 
			{
				clearPendingFrames();
				return err;
			}
		}
		MediaBuffer *frame = *mPendingFrames.begin();
		size_t size = frame->range_length();
		size_t offset = frame->range_offset();
		size_t i;
		size_t frame_size;
		for (i=0; i<size; i++) {
			LOGD("data[%d]=%x", i, *((uint8_t*)frame->data()+offset+i));
			code = (code<<8) + *((uint8_t*)frame->data()+offset+i);
			if (get_mp3_info(code, &frame_size, NULL, NULL, NULL)) {
				*header = code;
				mBlockIter.reset();
				clearPendingFrames();
				return OK;
			}
		}
	}

	return ERROR_END_OF_STREAM;
}
#endif

status_t MatroskaSource::start(MetaData *params) {
    mBlockIter.reset();
#ifndef ANDROID_DEFAULT_CODE
	mNewFrame = true;
#endif

    return OK;
}

status_t MatroskaSource::stop() {
    clearPendingFrames();

    return OK;
}

sp<MetaData> MatroskaSource::getFormat() {
    return mExtractor->mTracks.itemAt(mTrackIndex).mMeta;
}

#ifndef ANDROID_DEFAULT_CODE
void MatroskaSource::setCodecInfoFromFirstFrame() {
	LOGD("setCodecInfoFromFirstFrame");
	clearPendingFrames();
	mBlockIter.seek(0);
	status_t err = readBlock();
	if (err != OK) {
		LOGE("read codec info from first block fail!");
		mBlockIter.reset();
		clearPendingFrames();
		return;
	}
	if (mPendingFrames.empty()) {
		return;
	}
	if (MPEG4 == mType) {
		/*if (0xB0010000 != *(uint32_t*)((uint8_t*)((*mPendingFrames.begin())->data())))
		{
			LOGE("Can not find VOS in the first frame");//first frame is not start from VOS
			return;
		}*/
		size_t vosend;
		for (vosend=0; (vosend<200) && (vosend<(*mPendingFrames.begin())->range_length()-4); vosend++)
		{
			if (0xB6010000 == *(uint32_t*)((uint8_t*)((*mPendingFrames.begin())->data()) + vosend))
			{
				break;//Send VOS until VOP
			}
		}
		getFormat()->setData(kKeyMPEG4VOS, 0, (*mPendingFrames.begin())->data(), vosend);
		for (int32_t i=0; i<vosend; i++)
			LOGD("VOS[%d] = 0x%x", i, *((uint8_t *)((*mPendingFrames.begin())->data())+i));
	}
	else if (MP2_3 == mType) {
		uint32_t header = *(uint32_t*)((uint8_t*)(*mPendingFrames.begin())->data()+(*mPendingFrames.begin())->range_offset());
		header = ((header >> 24) & 0xff) | ((header >> 8) & 0xff00) | ((header << 8) & 0xff0000) | ((header << 24) & 0xff000000); 
		LOGD("HEADER=0x%x", header);
		size_t frame_size;
		int32_t out_sampling_rate;
		int32_t out_channels;
		int32_t out_bitrate;
		if(!get_mp3_info(header, &frame_size, &out_sampling_rate, &out_channels, &out_bitrate))
		{
			LOGE("Get mp3 info fail");
			return;
		}
		LOGD("mp3: frame_size=%d, sample_rate=%d, channel_count=%d, out_bitrate=%d", 
			frame_size, out_sampling_rate, out_channels, out_bitrate);
		if (out_channels > 2)
		{
			LOGE("Unsupport mp3 channel count %d", out_channels);
			return;
		}
		getFormat()->setInt32(kKeySampleRate, out_sampling_rate);
        getFormat()->setInt32(kKeyChannelCount, out_channels);
		
		
	}
	
	mBlockIter.reset();
	clearPendingFrames();
}
#endif

////////////////////////////////////////////////////////////////////////////////

BlockIterator::BlockIterator(
        MatroskaExtractor *extractor, unsigned long trackNum)
    : mExtractor(extractor),
      mTrackNum(trackNum),
      mCluster(NULL),
      mBlockEntry(NULL),
      mBlockEntryIndex(0) {
#ifndef ANDROID_DEFAULT_CODE
	mTrackType = mExtractor->mSegment->GetTracks()->GetTrackByNumber(trackNum)->GetType();
#endif
    reset();
}

bool BlockIterator::eos() const {
    return mCluster == NULL || mCluster->EOS();
}

void BlockIterator::advance() {
    Mutex::Autolock autoLock(mExtractor->mLock);
    advance_l();
}

void BlockIterator::advance_l() {
    while (!eos()) {
        if (mBlockEntry != NULL) {
            mBlockEntry = mCluster->GetNext(mBlockEntry);
        } else if (mCluster != NULL) {
            mCluster = mExtractor->mSegment->GetNext(mCluster);

            if (eos()) {
                break;
            }

            mBlockEntry = mCluster->GetFirst();
        }

        if (mBlockEntry != NULL
                && mBlockEntry->GetBlock()->GetTrackNumber() == mTrackNum) {
            break;
        }
    }
}


//added by vend_am00033 start for seeking backward
#ifndef ANDROID_DEFAULT_CODE
void BlockIterator::backward()
{
	while ((mCluster != NULL) && (mCluster != &mExtractor->mSegment->m_eos))
	{
        if (mBlockEntry != NULL) {
            mBlockEntry = mCluster->GetPrev(mBlockEntry);
        } else if (mCluster != NULL) {
            mCluster = mExtractor->mSegment->GetPrev(mCluster);

            if (mCluster == &mExtractor->mSegment->m_eos) {
                break;
            }

            mBlockEntry = mCluster->GetLast();
        }

        if (mBlockEntry != NULL
                && mBlockEntry->GetBlock()->GetTrackNumber() == mTrackNum) {
            break;
        }

	}
}

bool BlockIterator::backward_eos(const mkvparser::Cluster* oldCluster, const mkvparser::BlockEntry* oldBlock)
{
	if (mCluster == &mExtractor->mSegment->m_eos)
	{
		//cannot seek I frame backward, so we seek I frame forward again
		mCluster = oldCluster;
		mBlockEntry = oldBlock;
		while (!eos() && (mTrackType != 2) && !block()->IsKey())
		{
			advance();
		}

		return true;
	}
	return false;
}
#endif
//added by vend_am00033 end

void BlockIterator::reset() {
    Mutex::Autolock autoLock(mExtractor->mLock);

    mCluster = mExtractor->mSegment->GetFirst();
    mBlockEntry = mCluster->GetFirst();
    while (!eos() && block()->GetTrackNumber() != mTrackNum) {
        advance_l();
    }
//    mBlockEntry = NULL;
    mBlockEntryIndex = 0;
}

void BlockIterator::seek(int64_t seekTimeUs) {
    Mutex::Autolock autoLock(mExtractor->mLock);

    mCluster = mExtractor->mSegment->FindCluster(seekTimeUs * 1000ll);
    mBlockEntry = mCluster != NULL ? mCluster->GetFirst() : NULL;
//    mBlockEntry = NULL;
    mBlockEntryIndex = 0;

#ifndef ANDROID_DEFAULT_CODE//hai.li
	//added by vend_am00033 start for seeking backward
	const mkvparser::Cluster* startCluster = mCluster;//cannot be null
	const mkvparser::Cluster* iframe_cluster = NULL;
	const mkvparser::BlockEntry* iframe_block = NULL;
	bool find_iframe = false;
	assert(startCluster != NULL);
	if (mBlockEntry)
	{
		if ((mTrackType != 2) && (block()->GetTrackNumber() == mTrackNum) && (block()->IsKey()))
		{
			find_iframe = true;
			iframe_cluster = mCluster;
			iframe_block = mBlockEntry;
		}
	}
	//added by vend_am00033 end
	while (!eos() && ((block()->GetTrackNumber() != mTrackNum) || (blockTimeUs() < seekTimeUs))) 
#else
    while (!eos() && block()->GetTrackNumber() != mTrackNum)
#endif
	{
        	advance_l();
//added by vend_am00033 start for seeking backward
#ifndef ANDROID_DEFAULT_CODE
		if (mBlockEntry)
		{
			if ((mTrackType != 2) && (block()->GetTrackNumber() == mTrackNum) && (block()->IsKey()))
			{
				find_iframe = true;
				iframe_cluster = mCluster;
				iframe_block = mBlockEntry;
			}

		}
#endif
//added by vend_am00033 end
    }

//added by vend_am00033 start for seeking backward
#ifndef ANDROID_DEFAULT_CODE
	if (!eos() && (mTrackType != 2) && (!block()->IsKey()))
	{
		if (!find_iframe)
		{
			const mkvparser::Cluster* oldCluster = mCluster;
			const mkvparser::BlockEntry* oldBlock = mBlockEntry;
			mCluster = mExtractor->mSegment->GetPrev(startCluster);

			if (backward_eos(oldCluster, oldBlock))
				return;
			
			mBlockEntry = mCluster != NULL ? mCluster->GetLast(): NULL;

			while ((mCluster != &mExtractor->mSegment->m_eos) && 
				((block()->GetTrackNumber() != mTrackNum) || (!block()->IsKey())))
			{
				backward();
			}

			if (backward_eos(oldCluster, oldBlock))
				return;

		}
		else
		{
			mCluster = iframe_cluster;
			mBlockEntry = iframe_block;
		}
	}
#endif
//added by vend_am00033 end

#ifndef ANDROID_DEFAULT_CODE//hai.li
	while (!eos() && !mBlockEntry->GetBlock()->IsKey() && (mTrackType != 2)/*Audio*/)
#else
    while (!eos() && !mBlockEntry->GetBlock()->IsKey())
#endif
	{
        advance_l();
       }
}

const mkvparser::Block *BlockIterator::block() const {
    CHECK(!eos());

    return mBlockEntry->GetBlock();
}

int64_t BlockIterator::blockTimeUs() const {
    return (mBlockEntry->GetBlock()->GetTime(mCluster) + 500ll) / 1000ll;
}

////////////////////////////////////////////////////////////////////////////////

static unsigned U24_AT(const uint8_t *ptr) {
    return ptr[0] << 16 | ptr[1] << 8 | ptr[2];
}

static size_t clz(uint8_t x) {
    size_t numLeadingZeroes = 0;

    while (!(x & 0x80)) {
        ++numLeadingZeroes;
        x = x << 1;
    }

    return numLeadingZeroes;
}

void MatroskaSource::clearPendingFrames() {
    while (!mPendingFrames.empty()) {
        MediaBuffer *frame = *mPendingFrames.begin();
        mPendingFrames.erase(mPendingFrames.begin());

        frame->release();
        frame = NULL;
    }
}

#define BAIL(err) \
    do {                        \
        if (bigbuf) {           \
            bigbuf->release();  \
            bigbuf = NULL;      \
        }                       \
                                \
        return err;             \
    } while (0)

status_t MatroskaSource::readBlock() {
    CHECK(mPendingFrames.empty());

    if (mBlockIter.eos()) {
        return ERROR_END_OF_STREAM;
    }

    const mkvparser::Block *block = mBlockIter.block();

    size_t size = block->GetSize();
    int64_t timeUs = mBlockIter.blockTimeUs();

    int32_t isSync = block->IsKey();
    MediaBuffer *bigbuf = NULL;
#ifndef ANDROID_DEFAULT_CODE
	unsigned lacing = (block->Flags() >> 1) & 3;

	if (0 == lacing)
	{
		
		bigbuf = new MediaBuffer(size+mTrackContentAddDataSize);
		//LOGD("mTrackContentAddDataSize = %d",mTrackContentAddDataSize);
		if (mTrackContentAddDataSize != 0)
			memcpy(bigbuf->data(), mTrackContentAddData, mTrackContentAddDataSize);

		
		long res = block->Read(
				mExtractor->mReader, (unsigned char *)bigbuf->data()+mTrackContentAddDataSize);
		
		if (res != 0) {
			bigbuf->release();
			bigbuf = NULL;
		
			return ERROR_END_OF_STREAM;
		}
	}
	else {
#endif

    	bigbuf = new MediaBuffer(size);

	    long res = block->Read(
	            mExtractor->mReader, (unsigned char *)bigbuf->data());

	    if (res != 0) {
	        bigbuf->release();
	        bigbuf = NULL;

	        return ERROR_END_OF_STREAM;
	    }
#ifndef ANDROID_DEFAULT_CODE
	}
#endif
    mBlockIter.advance();

    bigbuf->meta_data()->setInt64(kKeyTime, timeUs);
    bigbuf->meta_data()->setInt32(kKeyIsSyncFrame, isSync);
	
#ifdef ANDROID_DEFAULT_CODE
    unsigned lacing = (block->Flags() >> 1) & 3;
#endif
    if (lacing == 0) {
        mPendingFrames.push_back(bigbuf);
        return OK;
    }

    LOGV("lacing = %u, size = %d", lacing, size);

    const uint8_t *data = (const uint8_t *)bigbuf->data();
    // hexdump(data, size);

    if (size == 0) {
        BAIL(ERROR_MALFORMED);
    }

    unsigned numFrames = (unsigned)data[0] + 1;
    ++data;
    --size;

    Vector<uint64_t> frameSizes;

    switch (lacing) {
        case 1:  // Xiph
        {
            for (size_t i = 0; i < numFrames - 1; ++i) {
                size_t frameSize = 0;
                uint8_t byte;
                do {
                    if (size == 0) {
                        BAIL(ERROR_MALFORMED);
                    }
                    byte = data[0];
                    ++data;
                    --size;

                    frameSize += byte;
                } while (byte == 0xff);

                frameSizes.push(frameSize);
            }

            break;
        }

        case 2:  // fixed-size
        {
            if ((size % numFrames) != 0) {
                BAIL(ERROR_MALFORMED);
            }

            size_t frameSize = size / numFrames;
            for (size_t i = 0; i < numFrames - 1; ++i) {
                frameSizes.push(frameSize);
            }

            break;
        }

        case 3:  // EBML
        {
            uint64_t lastFrameSize = 0;
            for (size_t i = 0; i < numFrames - 1; ++i) {
                uint8_t byte;

                if (size == 0) {
                    BAIL(ERROR_MALFORMED);
                }
                byte = data[0];
                ++data;
                --size;

                size_t numLeadingZeroes = clz(byte);

                uint64_t frameSize = byte & ~(0x80 >> numLeadingZeroes);
                for (size_t j = 0; j < numLeadingZeroes; ++j) {
                    if (size == 0) {
                        BAIL(ERROR_MALFORMED);
                    }

                    frameSize = frameSize << 8;
                    frameSize |= data[0];
                    ++data;
                    --size;
                }

                if (i == 0) {
                    frameSizes.push(frameSize);
                } else {
                    size_t shift =
                        7 - numLeadingZeroes + 8 * numLeadingZeroes;

                    int64_t delta =
                        (int64_t)frameSize - (1ll << (shift - 1)) + 1;

                    frameSize = lastFrameSize + delta;

                    frameSizes.push(frameSize);
                }

                lastFrameSize = frameSize;
            }
            break;
        }

        default:
            TRESPASS();
    }

#if 0
    AString out;
    for (size_t i = 0; i < frameSizes.size(); ++i) {
        if (i > 0) {
            out.append(", ");
        }
        out.append(StringPrintf("%llu", frameSizes.itemAt(i)));
    }
    LOGV("sizes = [%s]", out.c_str());
#endif

#ifndef ANDROID_DEFAULT_CODE
	if (mType == AAC || mType == MP2_3) {
		if (0 == mTrackContentAddDataSize) {
			bigbuf->set_range(data-(const uint8_t *)bigbuf->data(), size);
			mPendingFrames.push_back(bigbuf);
			return OK;
		}
		else {
			MediaBuffer *mbuf = new MediaBuffer(size+(frameSizes.size()+1)*mTrackContentAddDataSize);
			mbuf->meta_data()->setInt64(kKeyTime, timeUs);
			mbuf->meta_data()->setInt32(kKeyIsSyncFrame, isSync);
			size_t mbuf_offset = 0;
		    for (size_t i = 0; i < frameSizes.size(); ++i) {
		        uint64_t frameSize = frameSizes.itemAt(i);

		        if (size < frameSize) {
		            BAIL(ERROR_MALFORMED);
		        }

				memcpy(mbuf->data()+mbuf_offset, mTrackContentAddData, mTrackContentAddDataSize);
				mbuf_offset += mTrackContentAddDataSize;
		        memcpy(mbuf->data()+mbuf_offset, data, frameSize);
				mbuf_offset += frameSize;

		        data += frameSize;
		        size -= frameSize;
		    }

			memcpy(mbuf->data()+mbuf_offset, mTrackContentAddData, mTrackContentAddDataSize);
			mbuf_offset += mTrackContentAddDataSize;
			memcpy(mbuf->data()+mbuf_offset, data, size);

			bigbuf->release();
			bigbuf = NULL;
			
			
		    mPendingFrames.push_back(mbuf);

		    return OK;
		}
	}
	else {
#endif

	    for (size_t i = 0; i < frameSizes.size(); ++i) {
	        uint64_t frameSize = frameSizes.itemAt(i);

	        if (size < frameSize) {
	            BAIL(ERROR_MALFORMED);
	        }
#ifndef ANDROID_DEFAULT_CODE
			MediaBuffer *mbuf = new MediaBuffer(frameSize+mTrackContentAddDataSize);
#else
	        MediaBuffer *mbuf = new MediaBuffer(frameSize);
#endif
	        mbuf->meta_data()->setInt64(kKeyTime, timeUs);
	        mbuf->meta_data()->setInt32(kKeyIsSyncFrame, isSync);
#ifndef ANDROID_DEFAULT_CODE
			if (mTrackContentAddDataSize > 0) {
				memcpy(mbuf->data(), mTrackContentAddData, mTrackContentAddDataSize);
			}
	        memcpy(mbuf->data()+mTrackContentAddDataSize, data, frameSize);
#else			
	        memcpy(mbuf->data(), data, frameSize);
#endif
	        mPendingFrames.push_back(mbuf);

	        data += frameSize;
	        size -= frameSize;
	    }

		size_t offset = bigbuf->range_length() - size;
#ifndef ANDROID_DEFAULT_CODE
		MediaBuffer *mbuf = new MediaBuffer(size + mTrackContentAddDataSize);
		if (mTrackContentAddDataSize > 0) {
			memcpy(mbuf->data(), mTrackContentAddData, mTrackContentAddDataSize);
		}
		mbuf->meta_data()->setInt64(kKeyTime, timeUs);
		mbuf->meta_data()->setInt32(kKeyIsSyncFrame, isSync);
		memcpy(mbuf->data()+mTrackContentAddDataSize, bigbuf->data()+offset, size);
	    mPendingFrames.push_back(mbuf);
		bigbuf->release();
		bigbuf = NULL;
		
#else
	    bigbuf->set_range(offset, size);

	    mPendingFrames.push_back(bigbuf);
#endif
	    return OK;
#ifndef ANDROID_DEFAULT_CODE
	}
#endif
}

#undef BAIL

status_t MatroskaSource::read(
        MediaBuffer **out, const ReadOptions *options) {
    *out = NULL;

    int64_t seekTimeUs;
    ReadOptions::SeekMode mode;
//added by vend_am00033 start for preroll mechanism
#ifndef ANDROID_DEFAULT_CODE
	bool seeking = false;
#endif
//added by vend_am00033 end
    if (options && options->getSeekTo(&seekTimeUs, &mode) && !mExtractor->isLiveStreaming()) {
        clearPendingFrames();
        mBlockIter.seek(seekTimeUs);
//added by vend_am00033 start for preroll mechanism
#ifndef ANDROID_DEFAULT_CODE
		seeking = true;
#endif
//added by vend_am00033 end
    }

again:
    while (mPendingFrames.empty()) {
        status_t err = readBlock();

        if (err != OK) {
            clearPendingFrames();

            return err;
        }
    }

    MediaBuffer *frame = *mPendingFrames.begin();
#ifndef ANDROID_DEFAULT_CODE
	if (seeking || mFirstFrame)
	{
		mFirstFrame = false;
		frame->meta_data()->findInt64(kKeyTime, &mCurrentTS);
		LOGD("mCurrentTS=%lld", mCurrentTS);
	}
#else
    mPendingFrames.erase(mPendingFrames.begin());
#endif
    size_t size = frame->range_length();

    if (mType != AVC) {
//added by vend_am00033 start for preroll mechanism
#ifndef ANDROID_DEFAULT_CODE
		if (seeking && (mType == VP8 || mType == MPEG4))
		{
			frame->meta_data()->setInt64(kKeyTargetTime, seekTimeUs);
		}

		if (MP2_3 == mType) {
			int32_t start = -1;
			while (start < 0) {
				start = mkv_mp3HeaderStartAt((const uint8_t*)frame->data()+frame->range_offset(), frame->range_length(), mMP3Header);
				//LOGD("start=%d", start);
				if (start >= 0)
					break;
				frame->release();
				mPendingFrames.erase(mPendingFrames.begin());
				while (mPendingFrames.empty()) {
					status_t err = readBlock();
			
					if (err != OK) {
						clearPendingFrames();
			
						return err;
					}
				}
				frame = *mPendingFrames.begin();				
				frame->meta_data()->findInt64(kKeyTime, &mCurrentTS);
				LOGD("mCurrentTS1=%lld", mCurrentTS);
			} 

			frame->set_range(frame->range_offset()+start, frame->range_length()-start);

			
			uint32_t header = *(uint32_t*)((uint8_t*)frame->data()+frame->range_offset());
			header = ((header >> 24) & 0xff) | ((header >> 8) & 0xff00) | ((header << 8) & 0xff0000) | ((header << 24) & 0xff000000); 
			//LOGD("HEADER=%8.8x", header);
			size_t frame_size;
			int out_sampling_rate;
			int out_channels;
			int out_bitrate;
			if (!get_mp3_info(header, &frame_size, &out_sampling_rate, &out_channels, &out_bitrate)) {
				LOGE("MP3 Header read fail!!");
				return ERROR_UNSUPPORTED;
			}
			MediaBuffer *buffer = new MediaBuffer(frame_size);
			if (frame_size > frame->range_length()) {
				memcpy(buffer->data(), frame->data()+frame->range_offset(), frame->range_length());
				size_t needSize = frame_size - frame->range_length();
				frame->release();
				mPendingFrames.erase(mPendingFrames.begin());
				while (mPendingFrames.empty()) {
					status_t err = readBlock();
			
					if (err != OK) {
						clearPendingFrames();
			
						return err;
					}
				}
				frame = *mPendingFrames.begin();
				size_t offset = frame->range_offset();
				size_t size = frame->range_length();
				if (size < needSize)
				{
					LOGE("Unsupport MP3 frame locate size=%d, needSize=%d", size, needSize);
					return ERROR_UNSUPPORTED;
				}
				memcpy(buffer->data()+frame_size-needSize, frame->data()+frame->range_offset(), needSize);
				frame->set_range(offset+needSize, size-needSize);
			}
			else {
				size_t offset = frame->range_offset();
				size_t size = frame->range_length();
				memcpy(buffer->data(), frame->data()+offset, frame_size);
				frame->set_range(offset+frame_size, size-frame_size);
			}
			if (frame->range_length() < 4) {
				frame->release();
				frame = NULL;
				mPendingFrames.erase(mPendingFrames.begin());
			}
			buffer->meta_data()->setInt64(kKeyTime, mCurrentTS);
			mCurrentTS += (int64_t)frame_size*8000ll/out_bitrate;
			*out = buffer;
			return OK;
			
		}
		else {
#endif
//added by vend_am00033 end

        	*out = frame;
#ifndef ANDROID_DEFAULT_CODE
		mPendingFrames.erase(mPendingFrames.begin());
		//mNewFrame = true;//everytime we erase pending frame, we should set mNewFrame to true
#endif

        return OK;
#ifndef ANDROID_DEFAULT_CODE
		}
#endif
    }

    if (size < mNALSizeLen) {
        frame->release();
        frame = NULL;
#ifndef ANDROID_DEFAULT_CODE
		mPendingFrames.erase(mPendingFrames.begin());
		//mNewFrame = true;
#endif

        return ERROR_MALFORMED;
    }

#ifndef ANDROID_DEFAULT_CODE//hai.li
	//1. Maybe more than one NALs in one sample, so we should parse and send
	//these NALs to decoder one by one, other than skip data.
	//2. Just send the pure NAL data to decoder. (No NAL size field or start code)
    uint8_t *data = (uint8_t *)frame->data() + frame->range_offset();
	size_t NALsize;
	if (frame->range_length() >=4 && *(data+0) == 0x00 && *(data+1) == 0x00 && *(data+2) == 0x00 && *(data+3) == 0x01)
	{
		mNALSizeLen = 4;
		MediaBuffer *tmpbuffer = *mPendingFrames.begin();

		uint8_t * data = (uint8_t*)tmpbuffer->data() + tmpbuffer->range_offset();
		int size = tmpbuffer->range_length();
		//LOGD("accessunit size = %d",size);
		int tail = 4;
		while(tail <= size - 4)
		{
			if((*(data+tail+0) == 0x00 && *(data+tail+1) == 0x00 && *(data+tail+2) == 0x00 && *(data+tail+3) == 0x01) || tail == size -4)
			{
				int nalsize = 0;
				if(tail == size -4)
				{
					nalsize = size;
				}
				else
				{
					nalsize = tail;
				}
				NALsize = nalsize - 4;
				break;
			}
			tail++;
		}
	}
	else
	{
	switch (mNALSizeLen) {
		case 1: NALsize = data[0]; break;
		case 2: NALsize = U16_AT(&data[0]); break;
		case 3: NALsize = U24_AT(&data[0]); break;
		case 4: NALsize = U32_AT(&data[0]); break;
		default:
			TRESPASS();
	}
    if (size < NALsize + mNALSizeLen) {
        frame->release();
        frame = NULL;
		mPendingFrames.erase(mPendingFrames.begin());
		//mNewFrame = true;
		
        return ERROR_MALFORMED;
	    }
    }
    MediaBuffer *buffer = new MediaBuffer(NALsize);
    int64_t timeUs;
    CHECK(frame->meta_data()->findInt64(kKeyTime, &timeUs));
    int32_t isSync;
    CHECK(frame->meta_data()->findInt32(kKeyIsSyncFrame, &isSync));

    buffer->meta_data()->setInt64(kKeyTime, timeUs);
    buffer->meta_data()->setInt32(kKeyIsSyncFrame, isSync);
    memcpy((uint8_t *)buffer->data(),
           (const uint8_t *)frame->data() + frame->range_offset() + mNALSizeLen,
           NALsize);
	//buffer->set_range(0, NALsize);
	frame->set_range(frame->range_offset() + mNALSizeLen + NALsize
					,frame->range_length() - mNALSizeLen - NALsize);

	if (frame->range_length() == 0)
	{
		frame->release();
		frame = NULL;
		mPendingFrames.erase(mPendingFrames.begin());
		//mNewFrame = true;
	}
	//else
	//	mNewFrame = false;//current frame remains data, so we should not add encoding content header for the frame
//added by vend_am00033 start for preroll mechanism
	if (seeking)
	{
		buffer->meta_data()->setInt64(kKeyTargetTime, seekTimeUs);
	}
//added by vend_am00033 end

#else
    // In the case of AVC content, each NAL unit is prefixed by
    // mNALSizeLen bytes of length. We want to prefix the data with
    // a four-byte 0x00000001 startcode instead of the length prefix.
    // mNALSizeLen ranges from 1 through 4 bytes, so add an extra
    // 3 bytes of padding to the buffer start.
    static const size_t kPadding = 3;

    MediaBuffer *buffer = new MediaBuffer(size + kPadding);

    int64_t timeUs;
    CHECK(frame->meta_data()->findInt64(kKeyTime, &timeUs));
    int32_t isSync;
    CHECK(frame->meta_data()->findInt32(kKeyIsSyncFrame, &isSync));

    buffer->meta_data()->setInt64(kKeyTime, timeUs);
    buffer->meta_data()->setInt32(kKeyIsSyncFrame, isSync);

    memcpy((uint8_t *)buffer->data() + kPadding,
           (const uint8_t *)frame->data() + frame->range_offset(),
           size);


    frame->release();
    frame = NULL;

    uint8_t *data = (uint8_t *)buffer->data();

    size_t NALsize;
    switch (mNALSizeLen) {
        case 1: NALsize = data[kPadding]; break;
        case 2: NALsize = U16_AT(&data[kPadding]); break;
        case 3: NALsize = U24_AT(&data[kPadding]); break;
        case 4: NALsize = U32_AT(&data[kPadding]); break;
        default:
            TRESPASS();
    }

    if (size < NALsize + mNALSizeLen) {
        buffer->release();
        buffer = NULL;

        return ERROR_MALFORMED;
    }

    if (size > NALsize + mNALSizeLen) {
        LOGW("discarding %d bytes of data.", size - NALsize - mNALSizeLen);
    }

    // actual data starts at &data[kPadding + mNALSizeLen]

    memcpy(&data[mNALSizeLen - 1], "\x00\x00\x00\x01", 4);
    buffer->set_range(mNALSizeLen - 1, NALsize + 4);
#endif

    *out = buffer;

    return OK;
}

////////////////////////////////////////////////////////////////////////////////

MatroskaExtractor::MatroskaExtractor(const sp<DataSource> &source)
    : mDataSource(source),
      mReader(new DataSourceReader(mDataSource)),
      mSegment(NULL),
      mExtractedThumbnails(false),
      mIsWebm(false) {
    off64_t size;
    mIsLiveStreaming =
        (mDataSource->flags()
            & (DataSource::kWantsPrefetching
                | DataSource::kIsCachingDataSource))
        && mDataSource->getSize(&size) != OK;

    mkvparser::EBMLHeader ebmlHeader;
    long long pos;

	
	LOGD("=====================================\n"); 
    LOGD("[MKV Playback capability info]£º\n"); 
    LOGD("=====================================\n"); 
    LOGD("Resolution = \"[(8,8) ~ (640£¬480)]\" \n"); 
    LOGD("Support Codec = \"Video:VP8 ; Audio: VORBIS\" \n"); 
    //LOGD("Profile_Level = \"\" \n"); 
    //LOGD("Max frameRate =  120fps \n"); 
    LOGD("Max Bitrate  = 1Mbps  (640*480@30fps)\n"); 
    LOGD("=====================================\n"); 
    if (ebmlHeader.Parse(mReader, pos) < 0) {
        return;
    }

    if (ebmlHeader.m_docType && !strcmp("webm", ebmlHeader.m_docType)) {
        mIsWebm = true;
    }

    long long ret =
        mkvparser::Segment::CreateInstance(mReader, pos, mSegment);

    if (ret) {
        CHECK(mSegment == NULL);
        return;
    }

    if (isLiveStreaming()) {
        ret = mSegment->ParseHeaders();
        CHECK_EQ(ret, 0);

        long len;
        ret = mSegment->LoadCluster(pos, len);
        CHECK_EQ(ret, 0);
    } else {
        ret = mSegment->Load();
    }

    if (ret < 0) {
        delete mSegment;
        mSegment = NULL;
        return;
    }

	
#ifndef ANDROID_DEFAULT_CODE
    mFileMetaData = new MetaData;
    //mFileMetaData->setCString(kKeyMIMEType, MEDIA_MIMETYPE_CONTAINER_MATROSKA);
    mFileMetaData->setInt32(kKeyVideoPreCheck, 1);
#endif

    addTracks();
}

MatroskaExtractor::~MatroskaExtractor() {
    delete mSegment;
    mSegment = NULL;

    delete mReader;
    mReader = NULL;
}

size_t MatroskaExtractor::countTracks() {
    return mTracks.size();
}

sp<MediaSource> MatroskaExtractor::getTrack(size_t index) {
    if (index >= mTracks.size()) {
        return NULL;
    }

#ifndef ANDROID_DEFAULT_CODE
	sp<MediaSource> matroskasource = new MatroskaSource(this, index);
	int32_t isinfristframe = false;
	LOGD("index=%d", index);
	if (mTracks.itemAt(index).mMeta->findInt32(kKeyCodecInfoIsInFirstFrame, &isinfristframe)
		&& isinfristframe) {
		LOGD("Codec info is in first frame");;
		(static_cast<MatroskaSource*>(matroskasource.get()))->setCodecInfoFromFirstFrame(); 
	}
	return matroskasource;
#else
    return new MatroskaSource(this, index);
#endif
}

sp<MetaData> MatroskaExtractor::getTrackMetaData(
        size_t index, uint32_t flags) {
    if (index >= mTracks.size()) {
        return NULL;
    }

    if ((flags & kIncludeExtensiveMetaData) && !mExtractedThumbnails
            && !isLiveStreaming()) {
        findThumbnails();
        mExtractedThumbnails = true;
    }

    return mTracks.itemAt(index).mMeta;
}

bool MatroskaExtractor::isLiveStreaming() const {
    return mIsLiveStreaming;
}

static void addESDSFromAudioSpecificInfo(
        const sp<MetaData> &meta, const void *asi, size_t asiSize) {
    static const uint8_t kStaticESDS[] = {
        0x03, 22,
        0x00, 0x00,     // ES_ID
        0x00,           // streamDependenceFlag, URL_Flag, OCRstreamFlag

        0x04, 17,
        0x40,                       // Audio ISO/IEC 14496-3
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,

        0x05,
        // AudioSpecificInfo (with size prefix) follows
    };

    // Make sure all sizes can be coded in a single byte.
    CHECK(asiSize + 22 - 2 < 128);
    size_t esdsSize = sizeof(kStaticESDS) + asiSize + 1;
    uint8_t *esds = new uint8_t[esdsSize];
    memcpy(esds, kStaticESDS, sizeof(kStaticESDS));
    uint8_t *ptr = esds + sizeof(kStaticESDS);
    *ptr++ = asiSize;
    memcpy(ptr, asi, asiSize);

    // Increment by codecPrivateSize less 2 bytes that are accounted for
    // already in lengths of 22/17
    esds[1] += asiSize - 2;
    esds[6] += asiSize - 2;

    meta->setData(kKeyESDS, 0, esds, esdsSize);

    delete[] esds;
    esds = NULL;
}

void addVorbisCodecInfo(
        const sp<MetaData> &meta,
        const void *_codecPrivate, size_t codecPrivateSize) {
    // printf("vorbis private data follows:\n");
    // hexdump(_codecPrivate, codecPrivateSize);

    CHECK(codecPrivateSize >= 3);

    const uint8_t *codecPrivate = (const uint8_t *)_codecPrivate;
    CHECK(codecPrivate[0] == 0x02);

    size_t len1 = codecPrivate[1];
    size_t len2 = codecPrivate[2];

    CHECK(codecPrivateSize > 3 + len1 + len2);

    CHECK(codecPrivate[3] == 0x01);
    meta->setData(kKeyVorbisInfo, 0, &codecPrivate[3], len1);

    CHECK(codecPrivate[len1 + 3] == 0x03);

    CHECK(codecPrivate[len1 + len2 + 3] == 0x05);
    meta->setData(
            kKeyVorbisBooks, 0, &codecPrivate[len1 + len2 + 3],
            codecPrivateSize - len1 - len2 - 3);
}

void MatroskaExtractor::addTracks() {
    const mkvparser::Tracks *tracks = mSegment->GetTracks();
#ifndef ANDROID_DEFAULT_CODE
	bool hasVideo = false;
	bool hasAudio = false;
#endif

    for (size_t index = 0; index < tracks->GetTracksCount(); ++index) {
        mkvparser::Track *track = tracks->GetTrackByIndex(index);

		if (track == NULL)
		{
            // Apparently this is currently valid (if unexpected) behaviour of the mkv parser lib.
			LOGW("Unsupport track type");
			continue;
		}


        const char *const codecID = track->GetCodecId();
        LOGV("codec id = %s", codecID);
        LOGV("codec name = %s", track->GetCodecNameAsUTF8());

        size_t codecPrivateSize;
        const unsigned char *codecPrivate =
            track->GetCodecPrivate(codecPrivateSize);

        enum { VIDEO_TRACK = 1, AUDIO_TRACK = 2 };

        sp<MetaData> meta = new MetaData;

        switch (track->GetType()) {
            case VIDEO_TRACK:
            {
                const mkvparser::VideoTrack *vtrack =
                    static_cast<const mkvparser::VideoTrack *>(track);

#ifndef ANDROID_DEFAULT_CODE
				long long width = vtrack->GetWidth();
				long long height = vtrack->GetHeight();

				VDEC_DRV_QUERY_VIDEO_FORMAT_T qinfo;
				VDEC_DRV_QUERY_VIDEO_FORMAT_T outinfo;
				memset(&qinfo, 0, sizeof(qinfo));
				memset(&outinfo, 0, sizeof(outinfo));
				qinfo.u4Width = width;
				qinfo.u4Height = height;
				/*if (width > 1280 || height > 1280 || width * height > 1280*720)
				{
					LOGW("Unsupport video size %lldx%lld", width, height);
					mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
					continue;
				}
*/
                meta->setInt32(kKeyWidth, width);
                meta->setInt32(kKeyHeight, height);
#endif

                if (!strcmp("V_MPEG4/ISO/AVC", codecID)) {
                    meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_AVC);
#ifndef ANDROID_DEFAULT_CODE
					if (NULL == codecPrivate){
						LOGE("Unsupport AVC Video: No codec info");
						mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
						continue;
					}

					unsigned char level = codecPrivate[3];
					qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_H264;
			
/*					if (level > 31) {
						LOGE("Unsupport AVC Video level: %d", level);
						mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
						continue;
					}*/
#endif
					
                    meta->setData(kKeyAVCC, 0, codecPrivate, codecPrivateSize);
					LOGD("Video Codec: AVC");
                } else if (!strcmp("V_VP8", codecID)) {
                    meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_VPX);
#ifndef ANDROID_DEFAULT_CODE
					qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_VP8;
#endif
					LOGD("Video Codec: VP8");
                } 
#ifndef ANDROID_DEFAULT_CODE
				else if ((!strcmp("V_MPEG4/ISO/ASP", codecID)) ||
						 (!strcmp("V_MPEG4/ISO/SP", codecID))) {
					meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_MPEG4);
					qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
					LOGD("Video Codec: MPEG4");
					if (codecPrivate != NULL)
						meta->setData(kKeyMPEG4VOS, 0, codecPrivate, codecPrivateSize);
					else {
						LOGW("No specific codec private data, find it from the first frame");
						meta->setInt32(kKeyCodecInfoIsInFirstFrame,true);
					}
				}
/*				else if (!strcmp("V_REAL/RV40", codecID)) {
					meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_REAL_VIDEO);
					size_t RVCodecInfoSize = codecPrivateSize+26;//make RV Codec info
					uint32_t RVMOFTag=0x5649444f;//'VIDO'
					uint32_t RVSubMOFTag=0x52563430;//'RV40'
					uint16_t RVWidth=vtrack->GetWidth();
					uint16_t RVHeight=vtrack->GetHeight();
					uint16_t RVBitCount=0x000c;
					uint16_t RVPadWidth=0;
					uint16_t RVPadHeight=0;
					uint32_t RVFramesPerSecond=0x001e0000;
					
					unsigned char* RVCodecInfo = (unsigned char*)malloc(RVCodecInfoSize+6);
					memcpy(RVCodecInfo, &RVCodecInfoSize, 4);
					memcpy(RVCodecInfo+4, &RVMOFTag, 4);
					memcpy(RVCodecInfo+8, &RVSubMOFTag, 4);
					memcpy(RVCodecInfo+12, &RVWidth, 2);
					memcpy(RVCodecInfo+14, &RVHeight, 2);
					memcpy (RVCodecInfo+16, &RVBitCount, 2);
					memcpy (RVCodecInfo+18, &RVPadWidth, 2);
					memcpy (RVCodecInfo+20, &RVPadHeight, 2);
					memcpy (RVCodecInfo+22, &RVFramesPerSecond, 4);
					//memset(RVCodecInfo+16, 0, 10);
					memcpy(RVCodecInfo+26, codecPrivate, codecPrivateSize);
					//uint8_t temp=117;
					//memcpy(RVCodecInfo+26+codecPrivateSize, &temp, 1);
					//temp=100;
					//memcpy(RVCodecInfo+26+codecPrivateSize+1, &temp, 1);
					meta->setData(kKeyRVC, 0, RVCodecInfo, RVCodecInfoSize+6);
					free(RVCodecInfo);
				}*/
				else if (!strcmp("V_MS/VFW/FOURCC", codecID)) {
					LOGD("Video CodecID: V_MS/VFW/FOURCC");
					if ((NULL == codecPrivate) || (codecPrivateSize < 20)) {
						LOGE("Unsupport video: V_MS/VFW/FOURCC has no invalid private data, codecPrivate=%p, codecPrivateSize=%d", codecPrivate, codecPrivateSize);
						mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
						continue;
					} else {
						uint32_t fourcc = *(uint32_t *)(codecPrivate + 16);
						const char* mime = BMKVFourCC2MIME(fourcc);
						LOGD("V_MS/VFW/FOURCC type is %s", mime);
						if (!strncasecmp("video/", mime, 6)) {
							meta->setCString(kKeyMIMEType, mime);
						} else {
							LOGE("V_MS/VFW/FOURCC continue");
							mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
							continue;
						}
						if (!strcmp(mime, MEDIA_MIMETYPE_VIDEO_MPEG4)) {
							/*uint16_t width = *(uint16_t *)(codecPrivate + 4);
							uint16_t height = *(uint16_t *)(codecPrivate + 8);
							uint32_t w_h_in_vos = (((uint32_t)width << 14) & 0x07ffc000) | height;
							LOGD("w_h_in_vos=0x%8.8x", w_h_in_vos);
							uint8_t *tempMPEG4VOS = (uint8_t *)malloc(sizeof(TempMPEG4VOS));
							if (NULL == tempMPEG4VOS) {
								LOGD("NULL == tempMPEG4VOS");
							} else {
								memcpy((void*)tempMPEG4VOS, (void*)TempMPEG4VOS, sizeof(TempMPEG4VOS));
								uint32_t w_h_bytes_in_vos = (tempMPEG4VOS[25] | (tempMPEG4VOS[24]<<8) | (tempMPEG4VOS[23]<<16) | (tempMPEG4VOS[22]<<24)) | w_h_in_vos;
								w_h_bytes_in_vos = ((w_h_bytes_in_vos>>24) & 0xff) | ((w_h_bytes_in_vos>>8) & 0xff00) | ((w_h_bytes_in_vos<<8) & 0xff0000) | ((w_h_bytes_in_vos<<24) & 0xff000000);
								memcpy((void*)(tempMPEG4VOS + 22), (void*)&w_h_bytes_in_vos, 4);
								LOGD("(tempMPEG4VOS1 + 22)=0x%8.8x", *(uint32_t*)(tempMPEG4VOS + 22));
								meta->setData(kKeyMPEG4VOS, 0, tempMPEG4VOS, sizeof(TempMPEG4VOS));
								for (int32_t i=0;i<sizeof(TempMPEG4VOS);i++){
									LOGD("tempMPEG4VOS[%d]=%x", i, tempMPEG4VOS[i]);
								}
								free(tempMPEG4VOS);
							}*/
							meta->setInt32(kKeyCodecInfoIsInFirstFrame,true);
							qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
						} else if (!strcmp(mime, MEDIA_MIMETYPE_VIDEO_AVC)) {
							qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_H264;
						} else if (!strcmp(mime, MEDIA_MIMETYPE_VIDEO_H263)) {
							qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_H263;
						}
					}
				}

				else {
					LOGW("Unsupport video codec: %s", codecID);
					mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
                    continue;
                }

				LOGD("Video: %s, %dx%d, profile(%d), level(%d)", 
					codecID, qinfo.u4Width, qinfo.u4Height, qinfo.u4Profile, qinfo.u4Level);
/*
				VDEC_DRV_MRESULT_T ret = eVDecDrvQueryCapability(VDEC_DRV_QUERY_TYPE_VIDEO_FORMAT, &qinfo, &outinfo);

				LOGD("eVDecDrvQueryCapability return %d", ret);
				if (qinfo.u4Width > outinfo.u4Width ||
					qinfo.u4Width*qinfo.u4Height > outinfo.u4Width*outinfo.u4Height) {
					LOGW("Unsupport video: %s, %dx%d", 
						codecID, qinfo.u4Width, qinfo.u4Height);
					mFileMetaData->setInt32(kKeyHasUnsupportVideo,true);
                    continue;
                }*/
#endif
#ifdef ANDROID_DEFAULT_CODE
                meta->setInt32(kKeyWidth, vtrack->GetWidth());
                meta->setInt32(kKeyHeight, vtrack->GetHeight());
#else
				hasVideo = true;
#endif
                break;
            }

            case AUDIO_TRACK:
            {
                const mkvparser::AudioTrack *atrack =
                    static_cast<const mkvparser::AudioTrack *>(track);

                if (!strncasecmp("A_AAC", codecID, 5)) {
#ifndef ANDROID_DEFAULT_CODE
					unsigned char aacCodecInfo[2]={0, 0};
					if (codecPrivateSize >= 2) {
						uint8_t profile;
						profile = (codecPrivate[0] >> 3) & 0x1f;//highest 5 bit
						if ((profile != 2) //LC
							&& (profile != 4) //LTP
							&& (profile != 5) //SBR
							&& (profile != 29)) //PS
						{
							LOGW("Unspport AAC: profile %d", profile);
							continue;
						}
						uint8_t chan_num;
						chan_num = (codecPrivate[1] >> 3) & 0x0f;
						if ((chan_num > 2) || (chan_num < 1))
						{
							LOGD("Unsupport AAC: channel count=%d", chan_num);
							continue;
						}
					} else if (NULL == codecPrivate) {
					
						if (!strcasecmp("A_AAC", codecID)) {
							LOGW("Unspport AAC: No profile");
							continue;
						}
						else  {
							uint8_t freq_index=-1;
							uint8_t profile;
							if (!findAACSampleFreqIndex((uint32_t)atrack->GetSamplingRate(), freq_index)) {
								LOGE("Unsupport AAC freq");
								continue;
							}

							if ((atrack->GetChannels() > 2) || (atrack->GetChannels() < 1)) {
								LOGE("Unsupport AAC channel count %lld", atrack->GetChannels());
								continue;
							}

							if (!strcasecmp("A_AAC/MPEG4/LC", codecID) ||
								!strcasecmp("A_AAC/MPEG2/LC", codecID))
								profile = 2;
							else if (!strcasecmp("A_AAC/MPEG4/LC/SBR", codecID) ||
								!strcasecmp("A_AAC/MPEG2/LC/SBR", codecID))
								profile = 5;
							else if (!strcasecmp("A_AAC/MPEG4/LTP", codecID))
								profile = 4;
							else {
								LOGE("Unsupport AAC Codec profile %s", codecID);
								continue;
							}

							codecPrivate = aacCodecInfo;
							codecPrivateSize = 2;
							aacCodecInfo[0] |= (profile << 3) & 0xf1;   // put it into the highest 5 bits
							aacCodecInfo[0] |= ((freq_index >> 1) & 0x07);	   // put 3 bits		
							aacCodecInfo[1] |= ((freq_index << 7) & 0x80); // put 1 bit	  
							aacCodecInfo[1] |= ((unsigned char)atrack->GetChannels()<< 3);
							LOGD("Make codec info 0x%x, 0x%x", aacCodecInfo[0], aacCodecInfo[1]);
							
						}
					}else {
						LOGE("Incomplete AAC Codec Info %d byte", codecPrivateSize);
						continue;
					}
#endif
					addESDSFromAudioSpecificInfo(
							meta, codecPrivate, codecPrivateSize);
#ifndef ANDROID_DEFAULT_CODE
					meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_AAC);
					LOGD("Audio Codec: %s", codecID);
#endif
                } else if (!strcmp("A_VORBIS", codecID)) {
                    meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_VORBIS);

                    addVorbisCodecInfo(meta, codecPrivate, codecPrivateSize);
					LOGD("Audio Codec: VORBIS");
                } 
#ifndef ANDROID_DEFAULT_CODE
				else if ((!strcmp("A_MPEG/L1", codecID)) ||
						 (!strcmp("A_MPEG/L2", codecID)) ||
						(!strcmp("A_MPEG/L3", codecID))){
					meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_MPEG);
					LOGD("Audio Codec: MPEG");
					if (atrack->GetChannels() > 2) {
						LOGE("Unsupport MP3 Channel count=%lld", atrack->GetChannels());
						continue;
					}
					if ((atrack->GetSamplingRate() < 0.1) || (atrack->GetChannels() == 0))
					{
						meta->setInt32(kKeyCodecInfoIsInFirstFrame,true);
					}
				}
				else if ((!strcmp("A_MS/ACM", codecID))) {
					if ((NULL == codecPrivate) || (codecPrivateSize < 8)) {
						LOGE("Unsupport audio: A_MS/ACM has no invalid private data, codecPrivate=%p, codecPrivateSize=%d", codecPrivate, codecPrivateSize);
						continue;
					}
					
					else {
						uint16_t ID = *(uint16_t *)codecPrivate;
						const char* mime = MKVwave2MIME(ID);
						LOGD("A_MS/ACM type is %s", mime);
						if (!strncasecmp("audio/", mime, 6)) {
							meta->setCString(kKeyMIMEType, mime);
						} else {
							LOGE("A_MS/ACM continue");
							continue;
						}
						//uint32_t channel_count = *(uint16_t*)(codecPrivate+2);
						//uint32_t sample_rate = *(uint32_t*)(codecPrivate+4);
						
						//meta->setInt32(kKeySampleRate, sample_rate);
						//meta->setInt32(kKeyChannelCount, channel_count);
					}
				}
#endif
				else {
					LOGW("Unsupport audio codec: %s", codecID);
                    continue;
                }

                meta->setInt32(kKeySampleRate, atrack->GetSamplingRate());
                meta->setInt32(kKeyChannelCount, atrack->GetChannels());
#ifndef ANDROID_DEFAULT_CODE
				LOGD("Samplerate=%f, channelcount=%lld", atrack->GetSamplingRate(), atrack->GetChannels());
				meta->setInt32(kKeyMaxInputSize, 16384);
				hasAudio = true;
#endif
				
                break;
            }

            default:
                continue;
        }

        long long durationNs = mSegment->GetDuration();
        meta->setInt64(kKeyDuration, (durationNs + 500) / 1000);

        mTracks.push();
        TrackInfo *trackInfo = &mTracks.editItemAt(mTracks.size() - 1);
        trackInfo->mTrackNum = track->GetNumber();
        trackInfo->mMeta = meta;
#ifndef ANDROID_DEFAULT_CODE
		trackInfo->mTrack = track;
		if (!hasVideo && hasAudio){
			//mFileMetaData->setCString(kKeyMIMEType, "audio/x-matroska");
			mFileMetaData->setCString(
					            kKeyMIMEType,
					            mIsWebm ? "audio/webm" : "audio/x-matroska");
		}
		else{
			//mFileMetaData->setCString(kKeyMIMEType, "video/x-matroska");

			mFileMetaData->setCString(
					            kKeyMIMEType,
					            mIsWebm ? "video/webm" : "video/x-matroska");
		}
#endif
    }
}

void MatroskaExtractor::findThumbnails() {
    for (size_t i = 0; i < mTracks.size(); ++i) {
        TrackInfo *info = &mTracks.editItemAt(i);

        const char *mime;
        CHECK(info->mMeta->findCString(kKeyMIMEType, &mime));

        if (strncasecmp(mime, "video/", 6)) {
            continue;
        }

        BlockIterator iter(this, info->mTrackNum);
        int32_t i = 0;
        int64_t thumbnailTimeUs = 0;
        size_t maxBlockSize = 0;
        while (!iter.eos() && i < 20) {
            if (iter.block()->IsKey()) {
                ++i;

                size_t blockSize = iter.block()->GetSize();
                if (blockSize > maxBlockSize) {
                    maxBlockSize = blockSize;
                    thumbnailTimeUs = iter.blockTimeUs();
                }
            }
            iter.advance();
        }
        info->mMeta->setInt64(kKeyThumbnailTime, thumbnailTimeUs);
    }
}

sp<MetaData> MatroskaExtractor::getMetaData() {
#ifndef ANDROID_DEFAULT_CODE
	mFileMetaData->setCString(
			            kKeyMIMEType,
			            mIsWebm ? "video/webm" : "video/x-matroska");
	LOGE("getMetaData , %s",mIsWebm ? "video/webm" : "video/x-matroska");
	return mFileMetaData;
#else
    sp<MetaData> meta = new MetaData;

    meta->setCString(
            kKeyMIMEType,
            mIsWebm ? "video/webm" : MEDIA_MIMETYPE_CONTAINER_MATROSKA);

    return meta;
#endif
}

uint32_t MatroskaExtractor::flags() const {
    uint32_t x = CAN_PAUSE;
    if (!isLiveStreaming()) {
        x |= CAN_SEEK_BACKWARD | CAN_SEEK_FORWARD | CAN_SEEK;
    }

    return x;
}

bool SniffMatroska(
        const sp<DataSource> &source, String8 *mimeType, float *confidence,
        sp<AMessage> *) {
    DataSourceReader reader(source);
    mkvparser::EBMLHeader ebmlHeader;
    long long pos;
    if (ebmlHeader.Parse(&reader, pos) < 0) {
        return false;
    }

    mimeType->setTo(MEDIA_MIMETYPE_CONTAINER_MATROSKA);
    *confidence = 0.6;

    return true;
}

}  // namespace android
