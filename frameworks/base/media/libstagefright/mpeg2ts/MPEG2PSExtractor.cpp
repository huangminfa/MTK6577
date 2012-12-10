/*
 * Copyright (C) 2011 The Android Open Source Project
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
#define LOG_TAG "MPEG2PSExtractor"
#include <utils/Log.h>

#include "include/MPEG2PSExtractor.h"

#include "AnotherPacketSource.h"
#include "ESQueue.h"

#include <media/stagefright/foundation/ABitReader.h>
#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>
#include <utils/String8.h>

namespace android {

#ifndef ANDROID_DEFAULT_CODE
static const size_t kChunkSize = 8192;
#endif //#ifndef ANDROID_DEFAULT_CODE

struct MPEG2PSExtractor::Track : public MediaSource {
    Track(MPEG2PSExtractor *extractor,
          unsigned stream_id, unsigned stream_type);

    virtual status_t start(MetaData *params);
    virtual status_t stop();
    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options);

protected:
    virtual ~Track();

private:
    friend struct MPEG2PSExtractor;

    MPEG2PSExtractor *mExtractor;

    unsigned mStreamID;
    unsigned mStreamType;
    ElementaryStreamQueue *mQueue;
    sp<AnotherPacketSource> mSource;

    #ifndef ANDROID_DEFAULT_CODE
    bool seeking;
    int64_t mMaxTimeUs;
    bool mFirstPTSValid;
    uint64_t mFirstPTS;
    bool mSeekable;
    #endif

    #ifndef ANDROID_DEFAULT_CODE
    int64_t getPTS();
    bool isVideo();
    bool isAudio();
    int64_t convertPTSToTimestamp(uint64_t PTS);
    void signalDiscontinuity(); 
    #endif

    status_t appendPESData(
            unsigned PTS_DTS_flags,
            uint64_t PTS, uint64_t DTS,
            const uint8_t *data, size_t size);

    DISALLOW_EVIL_CONSTRUCTORS(Track);
};

struct MPEG2PSExtractor::WrappedTrack : public MediaSource {
    WrappedTrack(const sp<MPEG2PSExtractor> &extractor, const sp<Track> &track);

    virtual status_t start(MetaData *params);
    virtual status_t stop();
    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options);

protected:
    virtual ~WrappedTrack();

private:
    sp<MPEG2PSExtractor> mExtractor;
    sp<MPEG2PSExtractor::Track> mTrack;

    DISALLOW_EVIL_CONSTRUCTORS(WrappedTrack);
};

////////////////////////////////////////////////////////////////////////////////

MPEG2PSExtractor::MPEG2PSExtractor(const sp<DataSource> &source)
    : mDataSource(source),
      mOffset(0),
      mFinalResult(OK),
      mBuffer(new ABuffer(0)),
      mScanning(true),
      #ifndef ANDROID_DEFAULT_CODE
      mDurationUs(0),
      mSeekTimeUs(0),
      mSeeking(false),
      mSeekingOffset(0),
      mFileSize(0),
      mMinOffset(0),
      mMaxOffset(0),
      mMaxcount(0),
      mNeedDequeuePES(true),
      #endif //#ifndef ANDROID_DEFAULT_CODE
      mProgramStreamMapValid(false) {


      #ifndef ANDROID_DEFAULT_CODE
      init();
      parseMaxPTS();
      
      signalDiscontinuity();
    
      //Init Offset
      mOffset = 0;
      #else //#ifndef ANDROID_DEFAULT_CODE
    for (size_t i = 0; i < 500; ++i) {
        if (feedMore() != OK) {
            break;
        }
    }
      #endif //#ifndef ANDROID_DEFAULT_CODE
      

    // Remove all tracks that were unable to determine their format.
    for (size_t i = mTracks.size(); i-- > 0;) {
        if (mTracks.valueAt(i)->getFormat() == NULL) {
	    #ifndef ANDROID_DEFAULT_CODE
	    LOGE("NULL Foramt: %d \n", i);
	    #endif //#ifndef ANDROID_DEFAULT_CODE
            mTracks.removeItemsAt(i);
        }
    }

    mScanning = false;
}

MPEG2PSExtractor::~MPEG2PSExtractor() {
}

size_t MPEG2PSExtractor::countTracks() {
    return mTracks.size();
}

sp<MediaSource> MPEG2PSExtractor::getTrack(size_t index) {
    if (index >= mTracks.size()) {
        return NULL;
    }

    #ifndef ANDROID_DEFAULT_CODE
    bool seekable = true;
    	
    if (mTracks.size() > 1) {
        CHECK_EQ(mTracks.size(), 2u);

        sp<MetaData> meta = mTracks.editValueAt(index)->getFormat();
        const char *mime;
        CHECK(meta->findCString(kKeyMIMEType, &mime));
        
	 if (!strncasecmp("audio/", mime, 6))
	 {
	   mTracks.editValueAt(index)->mSeekable = false;
	 }
    }
    #endif //#ifndef ANDROID_DEFAULT_CODE

    return new WrappedTrack(this, mTracks.valueAt(index));
}

sp<MetaData> MPEG2PSExtractor::getTrackMetaData(size_t index, uint32_t flags) {
    if (index >= mTracks.size()) {
        return NULL;
    }

    return mTracks.valueAt(index)->getFormat();
}

sp<MetaData> MPEG2PSExtractor::getMetaData() {
    sp<MetaData> meta = new MetaData;
    meta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_CONTAINER_MPEG2PS);

    return meta;
}

uint32_t MPEG2PSExtractor::flags() const {
    #ifndef ANDROID_DEFAULT_CODE
    uint32_t flags = 0x0;

    flags = CAN_PAUSE | CAN_SEEK_BACKWARD | CAN_SEEK_FORWARD | CAN_SEEK;
    	
    return flags;
    #else //#ifndef ANDROID_DEFAULT_CODE
    return CAN_PAUSE;
    #endif //#ifndef ANDROID_DEFAULT_CODE
}

status_t MPEG2PSExtractor::feedMore() {
    Mutex::Autolock autoLock(mLock);
    #ifdef ANDROID_DEFAULT_CODE
    // How much data we're reading at a time
    static const size_t kChunkSize = 8192;
    #endif //#ifndef ANDROID_DEFAULT_CODE
    
    #ifndef ANDROID_DEFAULT_CODE
	if (mSeeking) {
		int64_t pts = getMaxPTS();
		
		LOGE("feedMore - Check Time Diff: %lld %lld ",pts/1000, mSeekTimeUs/1000);

		if (pts > 0) {
			mMaxcount++;
			if (((pts - mSeekTimeUs < 50000) && (pts - mSeekTimeUs > -50000)) ||     //Sync with TS Extractor
			     mMinOffset == mMaxOffset || 
			     mMaxcount > 13) 
			{
			  //LOGE("feedMore - Seek to Target: %lld %lld, mMaxcount %d  ", pts/1000, mSeekTimeUs/1000, mMaxcount);
			  mSeeking = false;
			  setDequeueState(true);
			}
			else 
		       {
			  signalDiscontinuity();
                       if(pts - mSeekTimeUs > 0)
			  {
			    mMaxOffset = mSeekingOffset + kChunkSize;
			    mSeekingOffset = (mMinOffset + mMaxOffset)/2;
		         }
			  else
			  {
			    mMinOffset = mSeekingOffset + kChunkSize;
			    mSeekingOffset = (mMinOffset + mMaxOffset)/2;
			  }
                       
  			  mOffset = mSeekingOffset;
			  mBuffer->setRange(0, 0);    //Need to Reset mBuffer Data
                       mOffset = SearchValidOffset(mOffset);
			}
			LOGD("pts=%lld, mSeekTimeUs=%lld, mMaxcount=%lld, mMinOffset=%lld, mMaxOffset=%lld, mSeekingOffset=%lld, mOffset = %lld ", 
				    pts/1000, mSeekTimeUs/1000, mMaxcount, mMinOffset, mMaxOffset, mSeekingOffset, mOffset);
		}
	}
#endif

    for (;;) {
        status_t err = dequeueChunk();

        if (err == -EAGAIN && mFinalResult == OK) {
            memmove(mBuffer->base(), mBuffer->data(), mBuffer->size());
            mBuffer->setRange(0, mBuffer->size());

            if (mBuffer->size() + kChunkSize > mBuffer->capacity()) {
                size_t newCapacity = mBuffer->capacity() + kChunkSize;
                sp<ABuffer> newBuffer = new ABuffer(newCapacity);
                memcpy(newBuffer->data(), mBuffer->data(), mBuffer->size());
                newBuffer->setRange(0, mBuffer->size());
                mBuffer = newBuffer;
            }

            ssize_t n = mDataSource->readAt(
                    mOffset, mBuffer->data() + mBuffer->size(), kChunkSize);

            if (n < (ssize_t)kChunkSize) {
                mFinalResult = (n < 0) ? (status_t)n : ERROR_END_OF_STREAM;
                return mFinalResult;
            }

            mBuffer->setRange(mBuffer->offset(), mBuffer->size() + n);
            mOffset += n;
        } else if (err != OK) {
            mFinalResult = err;
            return err;
        } else {
            return OK;
        }
    }
}

status_t MPEG2PSExtractor::dequeueChunk() {
    if (mBuffer->size() < 4) {
        return -EAGAIN;
    }

    if (memcmp("\x00\x00\x01", mBuffer->data(), 3)) {
	 #ifndef ANDROID_DEFAULT_CODE
         LOGE("dequeueChunk: Error: %x %x %x \n", *(mBuffer->data()), *(mBuffer->data()+1), *(mBuffer->data()+2));
	 #endif //#ifndef ANDROID_DEFAULT_CODE
        return ERROR_MALFORMED;
    }

    unsigned chunkType = mBuffer->data()[3];

    ssize_t res;

    switch (chunkType) {
        case 0xba:
        {
            res = dequeuePack();
            break;
        }

        case 0xbb:
        {
            res = dequeueSystemHeader();
            break;
        }

        default:
        {
            res = dequeuePES();
            break;
        }
    }

    if (res > 0) {
        if (mBuffer->size() < (size_t)res) {
            return -EAGAIN;
        }

        mBuffer->setRange(mBuffer->offset() + res, mBuffer->size() - res);
        res = OK;
    }

    return res;
}

ssize_t MPEG2PSExtractor::dequeuePack() {
    // 32 + 2 + 3 + 1 + 15 + 1 + 15+ 1 + 9 + 1 + 22 + 1 + 1 | +5
    #ifndef ANDROID_DEFAULT_CODE
    unsigned ConstantHdrLength = 14;
    #endif //#ifndef ANDROID_DEFAULT_CODE

    if (mBuffer->size() < 14) {
        return -EAGAIN;
    }

    #ifndef ANDROID_DEFAULT_CODE
    if ((*(mBuffer->data()+4) >> 6) != 1)
    {
      ConstantHdrLength = 12;
      //LOGD("MPEG1 File Format \n");
    }
    else
    {
      //LOGD("MPEG2 File Format \n");
    }
    #endif //#ifndef ANDROID_DEFAULT_CODE

    unsigned pack_stuffing_length = mBuffer->data()[13] & 7;

    #ifndef ANDROID_DEFAULT_CODE
    if (ConstantHdrLength == 14)
    {
      return pack_stuffing_length + ConstantHdrLength;
    }
    else
    {
      return ConstantHdrLength;
    }
    #else //#ifndef ANDROID_DEFAULT_CODE
    return pack_stuffing_length + 14;
    #endif //#ifndef ANDROID_DEFAULT_CODE
}

ssize_t MPEG2PSExtractor::dequeueSystemHeader() {
    if (mBuffer->size() < 6) {
        return -EAGAIN;
    }

    unsigned header_length = U16_AT(mBuffer->data() + 4);

    return header_length + 6;
}

#ifndef ANDROID_DEFAULT_CODE
ssize_t MPEG2PSExtractor::dequeuePES() {
    if (mBuffer->size() < 6) {
        return -EAGAIN;
    }
    unsigned PTS_DTS_flags;
    uint64_t PTS = 0;
    uint64_t DTS = 0; 
    unsigned dataLength;
    unsigned PES_packet_length = U16_AT(mBuffer->data() + 4);
    CHECK_NE(PES_packet_length, 0u);

    //LOGD("dequeuePES: %02x %02x %02x %02x %02x %02x ",
    //          *(mBuffer->data()+0),*(mBuffer->data()+1),*(mBuffer->data()+2),*(mBuffer->data()+3),*(mBuffer->data()+4),*(mBuffer->data()+5));
    
    size_t n = PES_packet_length + 6;

    if (mBuffer->size() < n) {
        return -EAGAIN;
    }

    ABitReader br(mBuffer->data(), n);

    unsigned packet_startcode_prefix = br.getBits(24);

    LOGV("packet_startcode_prefix = 0x%08x", packet_startcode_prefix);

    if (packet_startcode_prefix != 1) {
        LOGV("Supposedly payload_unit_start=1 unit does not start "
             "with startcode.");

        return ERROR_MALFORMED;
    }

    CHECK_EQ(packet_startcode_prefix, 0x000001u);

    unsigned stream_id = br.getBits(8);
    LOGV("stream_id = 0x%02x", stream_id);

    /* unsigned PES_packet_length = */br.getBits(16);

    //LOGD("dequeuePES Header: %02x %02x %02x %02x %02x %02x ",
    //          *(br.data()+0),*(br.data()+1),*(br.data()+2),*(br.data()+3),*(br.data()+4),*(br.data()+5));
        	
    if (stream_id == 0xbc) {
        // program_stream_map

        if (!mScanning) {
            return n;
        }

        mStreamTypeByESID.clear();

        /* unsigned current_next_indicator = */br.getBits(1);
        /* unsigned reserved = */br.getBits(2);
        /* unsigned program_stream_map_version = */br.getBits(5);
        /* unsigned reserved = */br.getBits(7);
        /* unsigned marker_bit = */br.getBits(1);
        unsigned program_stream_info_length = br.getBits(16);

        size_t offset = 0;
        while (offset < program_stream_info_length) {
            if (offset + 2 > program_stream_info_length) {
                return ERROR_MALFORMED;
            }

            unsigned descriptor_tag = br.getBits(8);
            unsigned descriptor_length = br.getBits(8);

            LOGI("found descriptor tag 0x%02x of length %u",
                 descriptor_tag, descriptor_length);

            if (offset + 2 + descriptor_length > program_stream_info_length) {
                return ERROR_MALFORMED;
            }

            br.skipBits(8 * descriptor_length);

            offset += 2 + descriptor_length;
        }

        unsigned elementary_stream_map_length = br.getBits(16);

        offset = 0;
        while (offset < elementary_stream_map_length) {
            if (offset + 4 > elementary_stream_map_length) {
                return ERROR_MALFORMED;
            }

            unsigned stream_type = br.getBits(8);
            unsigned elementary_stream_id = br.getBits(8);

	     //LOGI("elementary stream id 0x%02x has stream type 0x%02x",
            //     elementary_stream_id, stream_type);
	     
            mStreamTypeByESID.add(elementary_stream_id, stream_type);

            unsigned elementary_stream_info_length = br.getBits(16);

            if (offset + 4 + elementary_stream_info_length
                    > elementary_stream_map_length) {
                return ERROR_MALFORMED;
            }

            offset += 4 + elementary_stream_info_length;
        }

        /* unsigned CRC32 = */br.getBits(32);

        mProgramStreamMapValid = true;
    } else if (stream_id != 0xbe  // padding_stream
            && stream_id != 0xbf  // private_stream_2
            && stream_id != 0xf0  // ECM
            && stream_id != 0xf1  // EMM
            && stream_id != 0xff  // program_stream_directory
            && stream_id != 0xf2  // DSMCC
            && stream_id != 0xf8
            && ((stream_id >= 0xc0 && stream_id <= 0xdf) || (stream_id >= 0xe0 && stream_id <= 0xef))) {	 
	 unsigned next2bits = *(br.data())>>6;
	 if(next2bits == 2u)
	 {
	 //MPEG2 Spec
	 //LOGD("MPEG2 PES \n");
	 
	 CHECK_EQ(br.getBits(2), 2u);

        /* unsigned PES_scrambling_control = */br.getBits(2);
        /* unsigned PES_priority = */br.getBits(1);
        /* unsigned data_alignment_indicator = */br.getBits(1);
        /* unsigned copyright = */br.getBits(1);
        /* unsigned original_or_copy = */br.getBits(1);

        PTS_DTS_flags = br.getBits(2);
        //unsigned PTS_DTS_flags = br.getBits(2);
        LOGV("PTS_DTS_flags = %u", PTS_DTS_flags);

        unsigned ESCR_flag = br.getBits(1);
        LOGV("ESCR_flag = %u", ESCR_flag);

        unsigned ES_rate_flag = br.getBits(1);
        LOGV("ES_rate_flag = %u", ES_rate_flag);

        unsigned DSM_trick_mode_flag = br.getBits(1);
        LOGV("DSM_trick_mode_flag = %u", DSM_trick_mode_flag);

        unsigned additional_copy_info_flag = br.getBits(1);
        LOGV("additional_copy_info_flag = %u", additional_copy_info_flag);

        /* unsigned PES_CRC_flag = */br.getBits(1);
        /* PES_extension_flag = */br.getBits(1);

        unsigned PES_header_data_length = br.getBits(8);
        LOGV("PES_header_data_length = %u", PES_header_data_length);

        unsigned optional_bytes_remaining = PES_header_data_length; 

        if (PTS_DTS_flags == 2 || PTS_DTS_flags == 3) {
            CHECK_GE(optional_bytes_remaining, 5u);

            CHECK_EQ(br.getBits(4), PTS_DTS_flags);

            PTS = ((uint64_t)br.getBits(3)) << 30;
            CHECK_EQ(br.getBits(1), 1u);
            PTS |= ((uint64_t)br.getBits(15)) << 15;
            CHECK_EQ(br.getBits(1), 1u);
            PTS |= br.getBits(15);
            CHECK_EQ(br.getBits(1), 1u);

            //LOGD("PTS = %llu", PTS);
            
            optional_bytes_remaining -= 5;

            if (PTS_DTS_flags == 3) {
                CHECK_GE(optional_bytes_remaining, 5u);

                CHECK_EQ(br.getBits(4), 1u);

                DTS = ((uint64_t)br.getBits(3)) << 30;
                CHECK_EQ(br.getBits(1), 1u);
                DTS |= ((uint64_t)br.getBits(15)) << 15;
                CHECK_EQ(br.getBits(1), 1u);
                DTS |= br.getBits(15);
                CHECK_EQ(br.getBits(1), 1u);

                LOGV("DTS = %llu", DTS);

                optional_bytes_remaining -= 5;
            }
        }

        if (ESCR_flag) {
            CHECK_GE(optional_bytes_remaining, 6u);

            br.getBits(2);

            uint64_t ESCR = ((uint64_t)br.getBits(3)) << 30;
            CHECK_EQ(br.getBits(1), 1u);
            ESCR |= ((uint64_t)br.getBits(15)) << 15;
            CHECK_EQ(br.getBits(1), 1u);
            ESCR |= br.getBits(15);
            CHECK_EQ(br.getBits(1), 1u);

            LOGV("ESCR = %llu", ESCR);
            /* unsigned ESCR_extension = */br.getBits(9);

            CHECK_EQ(br.getBits(1), 1u);

            optional_bytes_remaining -= 6;
        }

        if (ES_rate_flag) {
            CHECK_GE(optional_bytes_remaining, 3u);

            CHECK_EQ(br.getBits(1), 1u);
            /* unsigned ES_rate = */br.getBits(22);
            CHECK_EQ(br.getBits(1), 1u);

            optional_bytes_remaining -= 3;
        }

        br.skipBits(optional_bytes_remaining * 8);
		 
        // ES data follows.

        CHECK_GE(PES_packet_length, PES_header_data_length + 3);

        dataLength =
                    PES_packet_length - 3 - PES_header_data_length;

        if (br.numBitsLeft() < dataLength * 8) {
            LOGE("PES packet does not carry enough data to contain "
                 "payload. (numBitsLeft = %d, required = %d)",
                 br.numBitsLeft(), dataLength * 8);

            return ERROR_MALFORMED;
        }

        CHECK_GE(br.numBitsLeft(), dataLength * 8);
	 }
	 else  
	 {
	   //MPEG1 Spec
          //LOGD("MPEG1 PES \n");
	   
	   unsigned offset = 0;
	   
	   while(offset < 17 && *(br.data())== 0xff)
	   {
	     br.skipBits(8);
	     offset++;
	   }
	   
	   if(offset == 17)//stuffing bytes no more than 16 bytes
	   {
	     LOGD("*********************parsePES ERROR:too much MPEG-1 stuffing*********************");
	     return 0;
	   }

	   next2bits = *(br.data())>>6;
          if(next2bits== 0x01)
	   {
	     CHECK_EQ(br.getBits(2),1u);
	     unsigned STD_buffer_scale = br.getBits(1);
	     uint32_t STD_buffer_size = br.getBits(13);
	     offset += 2;
	   }
          
	   PTS_DTS_flags = *(br.data())>>4;
	   //LOGD("Mpeg1 - PTS_DTS_flags = %d %x", PTS_DTS_flags, *(br.data()));
	   
	   if (PTS_DTS_flags == 2 || PTS_DTS_flags == 3) 
	   {
	     offset += 5;
	     br.skipBits(4);
	     PTS = ((uint64_t)br.getBits(3)) << 30;
		 
	     br.getBits(1);
	     //CHECK_EQ(br->getBits(1), 1u);
		            
	     PTS |= ((uint64_t)br.getBits(15)) << 15;

	     br.getBits(1);
	     //CHECK_EQ(br->getBits(1), 1u);
		            
	     PTS |= br.getBits(15);

	     br.getBits(1);
	     //CHECK_EQ(br->getBits(1), 1u);

	     //LOGD("mpeg1 - PTS = %llu", PTS);

	     if (PTS_DTS_flags == 3) 
	     {
 	       offset += 5;
				  
		br.getBits(4);
		//CHECK_EQ(br->getBits(4), 1u);

		DTS = ((uint64_t)br.getBits(3)) << 30;

		br.getBits(1);
		//CHECK_EQ(br->getBits(1), 1u);
		                
		DTS |= ((uint64_t)br.getBits(15)) << 15;

		br.getBits(1);
		//CHECK_EQ(br->getBits(1), 1u);
		                
		DTS |= br.getBits(15);

		br.getBits(1);
		//CHECK_EQ(br->getBits(1), 1u);
	 
              //LOGD("mpeg1 - DTS = %llu", DTS);
	     }
	   }
	   else 
	   {
	     offset += 1;
	     unsigned NO_PTSDTS_FFData = br.getBits(8) & 0xF;   
	     if (NO_PTSDTS_FFData != 0xF)
	     {
              LOGD("parsePES: Skip No PTS/DTS Error = %x \n", NO_PTSDTS_FFData);
	     }
	   }

	   dataLength = PES_packet_length - offset;
	 }

        ssize_t index = mTracks.indexOfKey(stream_id);
        if (index < 0 && mScanning) {
            unsigned streamType;

            ssize_t streamTypeIndex;
            if (mProgramStreamMapValid
                    && (streamTypeIndex =
                            mStreamTypeByESID.indexOfKey(stream_id)) >= 0) {
                streamType = mStreamTypeByESID.valueAt(streamTypeIndex);
            } else if ((stream_id & ~0x1f) == 0xc0) {
                // ISO/IEC 13818-3 or ISO/IEC 11172-3 or ISO/IEC 13818-7
                // or ISO/IEC 14496-3 audio
                streamType = ATSParser::STREAMTYPE_MPEG2_AUDIO;
            } else if ((stream_id & ~0x0f) == 0xe0) {
                // ISO/IEC 13818-2 or ISO/IEC 11172-2 or ISO/IEC 14496-2 video
                streamType = ATSParser::STREAMTYPE_MPEG2_VIDEO;
            } else {
                streamType = ATSParser::STREAMTYPE_RESERVED;
            }
	     
	     //Add Check For Audio/Video Code When No Program Stream Map
	     if (mProgramStreamMapValid == false)
	     {
	       if (streamType == ATSParser::STREAMTYPE_MPEG2_VIDEO)
	       {
	         //Video Part  
	         unsigned StartCode[5];
	         StartCode[0] = *(br.data());
		  StartCode[1] = *(br.data()+1);
		  StartCode[2] = *(br.data()+2);
		  StartCode[3] = *(br.data()+3);
		  StartCode[4] = *(br.data()+4);

		  if ((StartCode[0] == 0x0) && (StartCode[1] == 0x0) && (StartCode[2] == 0x01) && (StartCode[3] == 0xB3))    //MPEG2 or MPEG1
		  {
		    streamType = ATSParser::STREAMTYPE_MPEG2_VIDEO;
		  }
		  else if ((StartCode[0] == 0x0) && (StartCode[1] == 0x0) && (StartCode[2] == 0x00) && (StartCode[3] == 0x01) && (StartCode[4] == 0x67))    //H264
		  {
		    streamType = ATSParser::STREAMTYPE_H264;
		  }
		  else if ((StartCode[0] == 0x0) && (StartCode[1] == 0x0) && (StartCode[2] == 0x01) && (StartCode[3] == 0xB0))    //MP4
		  {
		    streamType = ATSParser::STREAMTYPE_MPEG4_VIDEO;
		  }
		  else
		  {
		    //TODO
		    streamType = ATSParser::STREAMTYPE_RESERVED;
		  }
	       }
	       else if (streamType == ATSParser::STREAMTYPE_MPEG2_AUDIO)
	       {
	         //Audio Part
	         if (IsSeeminglyValidADTSHeader(br.data(), dataLength))
	         {
	           //AAC Audio
	           streamType = ATSParser::STREAMTYPE_MPEG2_AUDIO_ADTS;
	         }
		  else if (IsSeeminglyValidMPEGAudioHeader(br.data(), dataLength))
		  {
		    streamType = ATSParser::STREAMTYPE_MPEG2_AUDIO;
		  }
		  else
		  {
		    //TODO
		    streamType = ATSParser::STREAMTYPE_RESERVED;
		  }
	       }
	     }
	     LOGD("PES - Add Track: %x %x %x \n", stream_id, streamType, mProgramStreamMapValid);

	     if (streamType != ATSParser::STREAMTYPE_RESERVED)
	     {
              index = mTracks.add(
                      stream_id, new Track(this, stream_id, streamType));
	     }
        }

        status_t err = OK;

        if (index >= 0) {
            err =
                mTracks.editValueAt(index)->appendPESData(
                    PTS_DTS_flags, PTS, DTS, br.data(), dataLength);
        }

        br.skipBits(dataLength * 8);

        if (err != OK) {
            return err;
        }
    } else if (stream_id == 0xbe) {  // padding_stream
        CHECK_NE(PES_packet_length, 0u);
        br.skipBits(PES_packet_length * 8);
    } else {
        CHECK_NE(PES_packet_length, 0u);
        br.skipBits(PES_packet_length * 8);
    }

    return n;
}
#else //#ifndef ANDROID_DEFAULT_CODE
ssize_t MPEG2PSExtractor::dequeuePES() {
    if (mBuffer->size() < 6) {
        return -EAGAIN;
    }

    unsigned PES_packet_length = U16_AT(mBuffer->data() + 4);
    CHECK_NE(PES_packet_length, 0u);

    size_t n = PES_packet_length + 6;

    if (mBuffer->size() < n) {
        return -EAGAIN;
    }

    ABitReader br(mBuffer->data(), n);

    unsigned packet_startcode_prefix = br.getBits(24);

    LOGV("packet_startcode_prefix = 0x%08x", packet_startcode_prefix);

    if (packet_startcode_prefix != 1) {
        LOGV("Supposedly payload_unit_start=1 unit does not start "
             "with startcode.");

        return ERROR_MALFORMED;
    }

    CHECK_EQ(packet_startcode_prefix, 0x000001u);

    unsigned stream_id = br.getBits(8);
    LOGV("stream_id = 0x%02x", stream_id);

    /* unsigned PES_packet_length = */br.getBits(16);

    if (stream_id == 0xbc) {
        // program_stream_map

        if (!mScanning) {
            return n;
        }

        mStreamTypeByESID.clear();

        /* unsigned current_next_indicator = */br.getBits(1);
        /* unsigned reserved = */br.getBits(2);
        /* unsigned program_stream_map_version = */br.getBits(5);
        /* unsigned reserved = */br.getBits(7);
        /* unsigned marker_bit = */br.getBits(1);
        unsigned program_stream_info_length = br.getBits(16);

        size_t offset = 0;
        while (offset < program_stream_info_length) {
            if (offset + 2 > program_stream_info_length) {
                return ERROR_MALFORMED;
            }

            unsigned descriptor_tag = br.getBits(8);
            unsigned descriptor_length = br.getBits(8);

            LOGI("found descriptor tag 0x%02x of length %u",
                 descriptor_tag, descriptor_length);

            if (offset + 2 + descriptor_length > program_stream_info_length) {
                return ERROR_MALFORMED;
            }

            br.skipBits(8 * descriptor_length);

            offset += 2 + descriptor_length;
        }

        unsigned elementary_stream_map_length = br.getBits(16);

        offset = 0;
        while (offset < elementary_stream_map_length) {
            if (offset + 4 > elementary_stream_map_length) {
                return ERROR_MALFORMED;
            }

            unsigned stream_type = br.getBits(8);
            unsigned elementary_stream_id = br.getBits(8);

            LOGI("elementary stream id 0x%02x has stream type 0x%02x",
                 elementary_stream_id, stream_type);

            mStreamTypeByESID.add(elementary_stream_id, stream_type);

            unsigned elementary_stream_info_length = br.getBits(16);

            if (offset + 4 + elementary_stream_info_length
                    > elementary_stream_map_length) {
                return ERROR_MALFORMED;
            }

            offset += 4 + elementary_stream_info_length;
        }

        /* unsigned CRC32 = */br.getBits(32);

        mProgramStreamMapValid = true;
    } else if (stream_id != 0xbe  // padding_stream
            && stream_id != 0xbf  // private_stream_2
            && stream_id != 0xf0  // ECM
            && stream_id != 0xf1  // EMM
            && stream_id != 0xff  // program_stream_directory
            && stream_id != 0xf2  // DSMCC
            && stream_id != 0xf8) {  // H.222.1 type E
        CHECK_EQ(br.getBits(2), 2u);

        /* unsigned PES_scrambling_control = */br.getBits(2);
        /* unsigned PES_priority = */br.getBits(1);
        /* unsigned data_alignment_indicator = */br.getBits(1);
        /* unsigned copyright = */br.getBits(1);
        /* unsigned original_or_copy = */br.getBits(1);

        unsigned PTS_DTS_flags = br.getBits(2);
        LOGV("PTS_DTS_flags = %u", PTS_DTS_flags);

        unsigned ESCR_flag = br.getBits(1);
        LOGV("ESCR_flag = %u", ESCR_flag);

        unsigned ES_rate_flag = br.getBits(1);
        LOGV("ES_rate_flag = %u", ES_rate_flag);

        unsigned DSM_trick_mode_flag = br.getBits(1);
        LOGV("DSM_trick_mode_flag = %u", DSM_trick_mode_flag);

        unsigned additional_copy_info_flag = br.getBits(1);
        LOGV("additional_copy_info_flag = %u", additional_copy_info_flag);

        /* unsigned PES_CRC_flag = */br.getBits(1);
        /* PES_extension_flag = */br.getBits(1);

        unsigned PES_header_data_length = br.getBits(8);
        LOGV("PES_header_data_length = %u", PES_header_data_length);

        unsigned optional_bytes_remaining = PES_header_data_length;

        uint64_t PTS = 0, DTS = 0;

        if (PTS_DTS_flags == 2 || PTS_DTS_flags == 3) {
            CHECK_GE(optional_bytes_remaining, 5u);

            CHECK_EQ(br.getBits(4), PTS_DTS_flags);

            PTS = ((uint64_t)br.getBits(3)) << 30;
            CHECK_EQ(br.getBits(1), 1u);
            PTS |= ((uint64_t)br.getBits(15)) << 15;
            CHECK_EQ(br.getBits(1), 1u);
            PTS |= br.getBits(15);
            CHECK_EQ(br.getBits(1), 1u);

            LOGV("PTS = %llu", PTS);
            // LOGI("PTS = %.2f secs", PTS / 90000.0f);

            optional_bytes_remaining -= 5;

            if (PTS_DTS_flags == 3) {
                CHECK_GE(optional_bytes_remaining, 5u);

                CHECK_EQ(br.getBits(4), 1u);

                DTS = ((uint64_t)br.getBits(3)) << 30;
                CHECK_EQ(br.getBits(1), 1u);
                DTS |= ((uint64_t)br.getBits(15)) << 15;
                CHECK_EQ(br.getBits(1), 1u);
                DTS |= br.getBits(15);
                CHECK_EQ(br.getBits(1), 1u);

                LOGV("DTS = %llu", DTS);

                optional_bytes_remaining -= 5;
            }
        }

        if (ESCR_flag) {
            CHECK_GE(optional_bytes_remaining, 6u);

            br.getBits(2);

            uint64_t ESCR = ((uint64_t)br.getBits(3)) << 30;
            CHECK_EQ(br.getBits(1), 1u);
            ESCR |= ((uint64_t)br.getBits(15)) << 15;
            CHECK_EQ(br.getBits(1), 1u);
            ESCR |= br.getBits(15);
            CHECK_EQ(br.getBits(1), 1u);

            LOGV("ESCR = %llu", ESCR);
            /* unsigned ESCR_extension = */br.getBits(9);

            CHECK_EQ(br.getBits(1), 1u);

            optional_bytes_remaining -= 6;
        }

        if (ES_rate_flag) {
            CHECK_GE(optional_bytes_remaining, 3u);

            CHECK_EQ(br.getBits(1), 1u);
            /* unsigned ES_rate = */br.getBits(22);
            CHECK_EQ(br.getBits(1), 1u);

            optional_bytes_remaining -= 3;
        }

        br.skipBits(optional_bytes_remaining * 8);

        // ES data follows.

        CHECK_GE(PES_packet_length, PES_header_data_length + 3);

        unsigned dataLength =
            PES_packet_length - 3 - PES_header_data_length;

        if (br.numBitsLeft() < dataLength * 8) {
            LOGE("PES packet does not carry enough data to contain "
                 "payload. (numBitsLeft = %d, required = %d)",
                 br.numBitsLeft(), dataLength * 8);

            return ERROR_MALFORMED;
        }

        CHECK_GE(br.numBitsLeft(), dataLength * 8);

        ssize_t index = mTracks.indexOfKey(stream_id);
        if (index < 0 && mScanning) {
            unsigned streamType;

            ssize_t streamTypeIndex;
            if (mProgramStreamMapValid
                    && (streamTypeIndex =
                            mStreamTypeByESID.indexOfKey(stream_id)) >= 0) {
                streamType = mStreamTypeByESID.valueAt(streamTypeIndex);
            } else if ((stream_id & ~0x1f) == 0xc0) {
                // ISO/IEC 13818-3 or ISO/IEC 11172-3 or ISO/IEC 13818-7
                // or ISO/IEC 14496-3 audio
                streamType = ATSParser::STREAMTYPE_MPEG2_AUDIO;
            } else if ((stream_id & ~0x0f) == 0xe0) {
                // ISO/IEC 13818-2 or ISO/IEC 11172-2 or ISO/IEC 14496-2 video
                streamType = ATSParser::STREAMTYPE_MPEG2_VIDEO;
            } else {
                streamType = ATSParser::STREAMTYPE_RESERVED;
            }

            index = mTracks.add(
                    stream_id, new Track(this, stream_id, streamType));
        }

        status_t err = OK;

        if (index >= 0) {
            err =
                mTracks.editValueAt(index)->appendPESData(
                    PTS_DTS_flags, PTS, DTS, br.data(), dataLength);
        }

        br.skipBits(dataLength * 8);

        if (err != OK) {
            return err;
        }
    } else if (stream_id == 0xbe) {  // padding_stream
        CHECK_NE(PES_packet_length, 0u);
        br.skipBits(PES_packet_length * 8);
    } else {
        CHECK_NE(PES_packet_length, 0u);
        br.skipBits(PES_packet_length * 8);
    }

    return n;
}
#endif //#ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_CODE
void MPEG2PSExtractor::setDequeueState(bool needDequeuePES) {  //For Seek  //true: When Enable Seek, false: When Get Target Seek Time
	mNeedDequeuePES = needDequeuePES;
}

bool MPEG2PSExtractor::getDequeueState() {    //For Seek
	return mNeedDequeuePES;
}

//get duration
int64_t MPEG2PSExtractor::getMaxPTS() {     //For Seek
	int64_t maxPTS=0;
	for (size_t i = 0; i < mTracks.size(); ++i) {
		int64_t pts = mTracks.editValueAt(i)->getPTS();
		if (maxPTS < pts) {
			maxPTS = pts;
		}
	}
	return maxPTS;
}

void MPEG2PSExtractor::seekTo(int64_t seekTimeUs, unsigned StreamID) {
    Mutex::Autolock autoLock(mLock);

    LOGE("seekTo:mDurationMs =%lld,seekTimeMs= %lld",mDurationUs/1000,seekTimeUs/1000);
	
    if (seekTimeUs == 0) 
    {
        mOffset = 0;
	 mSeeking = false;
	 
	 signalDiscontinuity();
    } 
    else if((mDurationUs-seekTimeUs) < 200000)
    {
      mOffset = mFileSize;
      mSeeking = false;
	  
      signalDiscontinuity(); 
    }
    else 
    {
      signalDiscontinuity();
	  
      mSeekingOffset = mOffset;
      mSeekTimeUs=seekTimeUs;
      mMinOffset = 0;
      mMaxOffset = mFileSize;
      mMaxcount=0;     
      setDequeueState(false);	  
      mSeeking=true;
      mSeekStreamID = StreamID;
    }
    mBuffer->setRange(0, 0);    //Need to Reset mBuffer Data
    mFinalResult = OK;    //Reset mFinalResult Status for Repeat Flow

    LOGE("seekTo: moffset: %lld %lld ", mOffset, mMaxOffset);

    return;
}

void MPEG2PSExtractor::parseMaxPTS() {
        size_t index = 0;
        off64_t u8SearchCount = 0;
		
       LOGD("parseMaxPTS in \n");
	   
	mDataSource->getSize(&mFileSize);
	
       setDequeueState(false);

	//Performance Issue, Only Parser File end
	u8SearchCount = mFileSize/kChunkSize;

	if (mFileSize <= kChunkSize)
	{
	  u8SearchCount = 1;
	}

       for (off64_t i = 1; i <= u8SearchCount; i++)
	{
	  //Set Search Start Offset
	  mOffset = (off64_t)((u8SearchCount - i) * kChunkSize);
	  
         //LOGD("parseMaxPTS - Search Cnt: %lld \n", i);
		 
         mOffset = SearchValidOffset(mOffset);
         mFinalResult = OK;
	  mBuffer->setRange(0, 0);
         	      
	  while (feedMore() == OK) 
	  {    
	  }

	  mDurationUs = getMaxPTS();
	  if (mDurationUs)
	  {
	    break;
	  }
       }
			
	setDequeueState(true);
	mFinalResult = OK;
	mBuffer->setRange(0, 0);
	
	//Init Max PTS
	for (index=0; index<mTracks.size(); index++)
	{
	  mTracks.valueAt(index)->mMaxTimeUs = 0x0;
	}
	
	LOGD("getMaxPTS->mDurationUs:%lld, Track Number: %d ", mDurationUs, mTracks.size());
       LOGD("parseMaxPTS out \n");
}

uint64_t MPEG2PSExtractor::getDurationUs() {
	return mDurationUs;
}

void MPEG2PSExtractor::init() {
    bool haveAudio = false;
    bool haveVideo = false;
    size_t index = 0;
    int numPacketsParsed = 0;

    LOGD("init in \n");
	
    mOffset = 0;

    while (feedMore() == OK) 
    {
        if (haveAudio && haveVideo) 
	 {
            break;
        }

	 for (index=0; index<mTracks.size(); index++)
	 {
	   if (mTracks.valueAt(index)->isVideo() && 
	   	 (mTracks.valueAt(index)->getFormat() != NULL))
	   {
	     haveVideo = true;
	     
	     LOGD("haveVideo=%d", haveVideo);
	   }
	   else
	   {
	     if (mTracks.valueAt(index)->isVideo())
	     {
	       LOGD("have Video, But no format !! \n");
	     }
	   }
	   
	   if (mTracks.valueAt(index)->isAudio() && 
	   	 (mTracks.valueAt(index)->getFormat() != NULL))
	   {
	     haveAudio = true;
		 
	     LOGD("haveAudio=%d", haveAudio);
	   }
	   else
	   {
	     if (mTracks.valueAt(index)->isAudio())
	     {
	       LOGD("have Audio, But no format !! \n");
	     }
	   }

	 }

	 if (++numPacketsParsed > 500) {
            break;
        }
    }

    mFinalResult = OK;
    mBuffer->setRange(0, 0);

    LOGI("haveAudio=%d, haveVideo=%d", haveAudio, haveVideo);
    LOGD("init out \n");
}

bool MPEG2PSExtractor::getSeeking() {
    return mSeeking;
}

void MPEG2PSExtractor::signalDiscontinuity() 
{
  mBuffer->setRange(0, 0);
  
  for (size_t i = 0; i < mTracks.size(); ++i) 
  {
    mTracks.valueAt(i)->signalDiscontinuity();
  }
}

int64_t MPEG2PSExtractor::SearchPES(const void* data, int size)
{
  uint8_t* p = (uint8_t*)data;
  int offset = 0;
  unsigned stream_id_Video = 0xFF;
  unsigned stream_id_Audio = 0xFF;
  size_t index = 0;

  for (index=0; index<mTracks.size(); index++)
  {
	   if (mTracks.valueAt(index)->isVideo())
	   {
            stream_id_Video = mTracks.valueAt(index)->mStreamID;
	   }
	   if (mTracks.valueAt(index)->isAudio())
	   {
            stream_id_Audio = mTracks.valueAt(index)->mStreamID;
	   }
  }
  
  
  while(offset < size - 4)
  {
    //find start code 
    if(p[0] == 0x00 && p[1] == 0x00 && p[2] == 0x01 && 
	 (p[3] == stream_id_Video || p[3] == stream_id_Audio))
    {
      return offset;	
    }
    else
    {
      p++;
      offset++;
    }
  }
  
  return -1;
}

int64_t MPEG2PSExtractor::SearchValidOffset(off64_t currentoffset)
{
  //Search Start Code & StreamID
  int length = 0;
  int bufsize = kChunkSize;
  char* buf = new char[bufsize];
  off64_t offset = currentoffset;
			
  //LOGE("feedMore - Search Start = %lld \n", offset);
  if (buf == NULL)
  {
    LOGE("Working Alloc Fail for Seek\n");
  }
  
  while((length = mDataSource->readAt(offset, buf, bufsize)) == bufsize)
  {
    int64_t result = SearchPES(buf, length);

    if (result >= 0)
    {
      offset = offset + result;
      //LOGE("feedMore - Seek Offset = %lld \n", mOffset);
      break;
    } 
    else
    {
      offset = offset + length;
    }
  }
  
  if(buf != NULL)
  {
    delete[] buf;
    buf = NULL;
  }

  return offset;
}

bool MPEG2PSExtractor::IsSeeminglyValidADTSHeader(const uint8_t *ptr, size_t size) {
    if (size < 3) {
        // Not enough data to verify header.
        return false;
    }

    if (ptr[0] != 0xff || (ptr[1] >> 4) != 0x0f) {
        return false;
    }

    unsigned layer = (ptr[1] >> 1) & 3;

    if (layer != 0) {
        return false;
    }

    unsigned ID = (ptr[1] >> 3) & 1;
    unsigned profile_ObjectType = ptr[2] >> 6;

    if (ID == 1 && profile_ObjectType == 3) {
        // MPEG-2 profile 3 is reserved.
        return false;
    }

    return true;
}

bool MPEG2PSExtractor::IsSeeminglyValidMPEGAudioHeader(const uint8_t *ptr, size_t size) {
    if (size < 3) {
        // Not enough data to verify header.
        return false;
    }

    if (ptr[0] != 0xff || (ptr[1] >> 5) != 0x07) {
        return false;
    }

    unsigned ID = (ptr[1] >> 3) & 3;

    if (ID == 1) {
        return false;  // reserved
    }

    unsigned layer = (ptr[1] >> 1) & 3;

    if (layer == 0) {
        return false;  // reserved
    }

    unsigned bitrateIndex = (ptr[2] >> 4);

    if (bitrateIndex == 0x0f) {
        return false;  // reserved
    }

    unsigned samplingRateIndex = (ptr[2] >> 2) & 3;

    if (samplingRateIndex == 3) {
        return false;  // reserved
    }

    return true;
}
#endif


////////////////////////////////////////////////////////////////////////////////

MPEG2PSExtractor::Track::Track(
        MPEG2PSExtractor *extractor, unsigned stream_id, unsigned stream_type)
    : mExtractor(extractor),
      mStreamID(stream_id),
      mStreamType(stream_type),
      #ifndef ANDROID_DEFAULT_CODE
      seeking(false),
      mMaxTimeUs(0),
      mFirstPTSValid(false),
      mSeekable(true),    //Default: Seekable. Will Change in getTrack() When Video/Audio Case (Disable Audio Seek)
      #endif
      mQueue(NULL) {
    bool supported = true;
    ElementaryStreamQueue::Mode mode;

    switch (mStreamType) {
        case ATSParser::STREAMTYPE_H264:
            mode = ElementaryStreamQueue::H264;
            break;
        case ATSParser::STREAMTYPE_MPEG2_AUDIO_ADTS:
            mode = ElementaryStreamQueue::AAC;
            break;
        case ATSParser::STREAMTYPE_MPEG1_AUDIO:
        case ATSParser::STREAMTYPE_MPEG2_AUDIO:
            mode = ElementaryStreamQueue::MPEG_AUDIO;
            break;

        case ATSParser::STREAMTYPE_MPEG1_VIDEO:
        case ATSParser::STREAMTYPE_MPEG2_VIDEO:
            mode = ElementaryStreamQueue::MPEG_VIDEO;
            break;

        case ATSParser::STREAMTYPE_MPEG4_VIDEO:
            mode = ElementaryStreamQueue::MPEG4_VIDEO;
            break;

        default:
            supported = false;
            break;
    }

    if (supported) {
        mQueue = new ElementaryStreamQueue(mode);
    } else {
        LOGI("unsupported stream ID 0x%02x", stream_id);
    }
}

MPEG2PSExtractor::Track::~Track() {
    delete mQueue;
    mQueue = NULL;
}

status_t MPEG2PSExtractor::Track::start(MetaData *params) {
    if (mSource == NULL) {
        return NO_INIT;
    }

    return mSource->start(params);
}

status_t MPEG2PSExtractor::Track::stop() {
    if (mSource == NULL) {
        return NO_INIT;
    }

    return mSource->stop();
}

sp<MetaData> MPEG2PSExtractor::Track::getFormat() {
    if (mSource == NULL) {
        return NULL;
    }

    return mSource->getFormat();
}

status_t MPEG2PSExtractor::Track::read(
        MediaBuffer **buffer, const ReadOptions *options) {
    if (mSource == NULL) {
        return NO_INIT;
    }

    #ifndef ANDROID_DEFAULT_CODE
    int64_t seekTimeUs;
    ReadOptions::SeekMode seekMode;
    
    if (mSeekable && options && options->getSeekTo(&seekTimeUs, &seekMode)) {     
      //LOGD("read - seekto: %x \n", mStreamID);
      mExtractor->seekTo(seekTimeUs, mStreamID);
    }
    #endif //#ifndef ANDROID_DEFAULT_CODE

    
    status_t finalResult;
    #ifndef ANDROID_DEFAULT_CODE
    while (!mSource->hasBufferAvailable(&finalResult) || mExtractor->getSeeking()) 
    #else //#ifndef ANDROID_DEFAULT_CODE
    while (!mSource->hasBufferAvailable(&finalResult)) 
    #endif //#ifndef ANDROID_DEFAULT_CODE
    {
        if (finalResult != OK) {
	     #ifndef ANDROID_DEFAULT_CODE
	     LOGE("read:ERROR_END_OF_STREAM this=%p",this );     
	     mExtractor->setDequeueState(true);
	     mSource->clear();
	     #endif //#ifndef ANDROID_DEFAULT_CODE
		 
            return ERROR_END_OF_STREAM;
        }

        status_t err = mExtractor->feedMore();

        if (err != OK) {
	   #ifndef ANDROID_DEFAULT_CODE
	   LOGE("read:signalEOS this=%p",this );
	   #endif //#ifndef ANDROID_DEFAULT_CODE
            mSource->signalEOS(err);
        }
    }

    return mSource->read(buffer, options);
}

status_t MPEG2PSExtractor::Track::appendPESData(
        unsigned PTS_DTS_flags,
        uint64_t PTS, uint64_t DTS,
        const uint8_t *data, size_t size) {
    if (mQueue == NULL) {
        return OK;
    }

    int64_t timeUs;
    if (PTS_DTS_flags == 2 || PTS_DTS_flags == 3) {
	 #ifndef ANDROID_DEFAULT_CODE
	 timeUs = convertPTSToTimestamp(PTS);
	 //LOGD("PTS US: %lld, ID: %x \n", timeUs, mStreamID);
	 #else //#ifndef ANDROID_DEFAULT_CODE
        timeUs = (PTS * 100) / 9;
        #endif //#ifndef ANDROID_DEFAULT_CODE
    } else {
        #ifndef ANDROID_DEFAULT_CODE
        timeUs = 0xFFFFFFFF;
        #else //#ifndef ANDROID_DEFAULT_CODE
        timeUs = 0;
	 #endif //#ifndef ANDROID_DEFAULT_CODE
    }

    #ifndef ANDROID_DEFAULT_CODE
    if ((timeUs > mMaxTimeUs) && (timeUs != 0xFFFFFFFF))
    {
      mMaxTimeUs = timeUs;
    }

    if (!mExtractor->getDequeueState()) 
    {
      return OK;
    }
    #endif
    
    status_t err = mQueue->appendData(data, size, timeUs);

    if (err != OK) {
        return err;
    }

    sp<ABuffer> accessUnit;
    while ((accessUnit = mQueue->dequeueAccessUnit()) != NULL) {
        if (mSource == NULL) {
            sp<MetaData> meta = mQueue->getFormat();

            if (meta != NULL) {
                LOGV("Stream ID 0x%02x now has data.", mStreamID);

                mSource = new AnotherPacketSource(meta);
                mSource->queueAccessUnit(accessUnit);
            }
        } else if (mQueue->getFormat() != NULL) {
            mSource->queueAccessUnit(accessUnit);
        }
    }

    return OK;
}

#ifndef ANDROID_DEFAULT_CODE
int64_t MPEG2PSExtractor::Track::getPTS() {
  return mMaxTimeUs;
}

bool MPEG2PSExtractor::Track::isVideo(){
    switch (mStreamType) {
        case ATSParser::STREAMTYPE_H264:
        case ATSParser::STREAMTYPE_MPEG1_VIDEO:
        case ATSParser::STREAMTYPE_MPEG2_VIDEO:
        case ATSParser::STREAMTYPE_MPEG4_VIDEO:
            return true;

        default:
            return false;
    }
}

bool MPEG2PSExtractor::Track::isAudio(){
    switch (mStreamType) {
        case ATSParser::STREAMTYPE_MPEG1_AUDIO:
        case ATSParser::STREAMTYPE_MPEG2_AUDIO:
        case ATSParser::STREAMTYPE_MPEG2_AUDIO_ADTS:
            return true;

        default:
            return false;
    }
}

int64_t MPEG2PSExtractor::Track::convertPTSToTimestamp(uint64_t PTS) {
  if (!mFirstPTSValid) 
  {
    mFirstPTSValid = true;
    mFirstPTS = PTS;
    PTS = 0;
  } 
  else if (PTS < mFirstPTS) 
  {
    PTS = 0;
  } 
  else 
  {
    PTS -= mFirstPTS;
  }
  
  return (PTS * 100) / 9;
}

void MPEG2PSExtractor::Track::signalDiscontinuity() 
{
    if (mQueue == NULL) {
        return;
    }
    
    if (!mExtractor->getDequeueState()) 
    {
      mMaxTimeUs = 0;
      return;
    }
	
    mQueue->clear(false);
	
    mQueue->setSeeking();
    if(mSource.get())
    {
      mSource->clear();
    }
    else
    {
      LOGE("[error]this stream has not source\n");
    }

    return;
}
#endif

////////////////////////////////////////////////////////////////////////////////

MPEG2PSExtractor::WrappedTrack::WrappedTrack(
        const sp<MPEG2PSExtractor> &extractor, const sp<Track> &track)
    : mExtractor(extractor),
      mTrack(track) {
}

MPEG2PSExtractor::WrappedTrack::~WrappedTrack() {
}

status_t MPEG2PSExtractor::WrappedTrack::start(MetaData *params) {
    return mTrack->start(params);
}

status_t MPEG2PSExtractor::WrappedTrack::stop() {
    return mTrack->stop();
}

sp<MetaData> MPEG2PSExtractor::WrappedTrack::getFormat() {

    #ifndef ANDROID_DEFAULT_CODE
    sp<MetaData> meta = mTrack->getFormat();

    meta->setInt64(kKeyDuration, mExtractor->getDurationUs());   //Need to Enable Seek Feature
    meta->setInt64(kKeyThumbnailTime,0);                                   //Need to Enable Seek Feature

    return meta;
    #else //#ifndef ANDROID_DEFAULT_CODE
    return mTrack->getFormat();
    #endif //#ifndef ANDROID_DEFAULT_CODE
}

status_t MPEG2PSExtractor::WrappedTrack::read(
        MediaBuffer **buffer, const ReadOptions *options) {
    return mTrack->read(buffer, options);
}

////////////////////////////////////////////////////////////////////////////////

#ifndef ANDROID_DEFAULT_CODE
#define  PACK_START_CODE              0X000001BA 
#define  SYSTEM_START_CODE          0X000001BB 

int  parsePackHeader(ABitReader * br)
{
	int length = br->numBitsLeft()/8;
	//LOGD("*********************parsePackHeader in :length = %d*********************",length);
	if (length < 12)
	{
		//LOGD("*********************parsePackHeader ERROR:the length less then 12 byte*********************");
		return 0;
	}
#if 1        // Scan 1024bytes to get start code
        const uint8_t *data = br->data();
        bool found = false;
	for(int i=0; i<length-3; i++)
	{
	    if (!memcmp(&data[i], "\x00\x00\x01\xBA", 4)) {
                 found = true;
                 break;
	    }
            br->skipBits(8);
        }
        if (!found)
        {
             LOGD("Not found PACK_START_CODE");
             return 0;
        }
        else
        {
             br->skipBits(4*8);
        }

	length = br->numBitsLeft()/8;
	if(length < 8)
	{
		LOGD("data too less");
		return 0;
	}
#else
	if (br->getBits(32) != PACK_START_CODE)
	{
		//LOGD("*********************parsePackHeader ERROR:the start code isn't 0x000001ba *********************");
		return 0;
	}
#endif
	uint64_t SRC = 0;
	int muxrate = 0;
	if (br->getBits(2) == 1u)//mpeg2
	{
	    if (length < 14)
		return 0;
	    SRC = ((uint64_t)br->getBits(3)) << 30;
	    if (br->getBits(1) != 1u)
		return 0;
	    SRC |= ((uint64_t)br->getBits(15)) << 15;
	    if (br->getBits(1) != 1u)
		return 0;
	    SRC |= br->getBits(15);
	    if (br->getBits(1) != 1u)
		return 0;

	    int SRC_Ext = br->getBits(9);
	    if (br->getBits(1) != 1u)
		return 0;
	    muxrate = br->getBits(22);
	    br->skipBits(7);
	    size_t pack_stuffing_length = br->getBits(3);
	    if (pack_stuffing_length <= 7)
	    {
		if (br->numBitsLeft() < pack_stuffing_length * 8)
		    return 0;
		br->skipBits(pack_stuffing_length * 8);
	    }

	}
	else//mpeg1
	{
	    br->skipBits(2);
	    SRC = ((uint64_t)br->getBits(3)) << 30;
	    if (br->getBits(1) != 1u)
		return 0;
	    SRC |= ((uint64_t)br->getBits(15)) << 15;
	    if (br->getBits(1) != 1u)
		return 0;
	    SRC |= br->getBits(15);
	    if (br->getBits(1) != 1u)
		return 0;
	    if (br->getBits(1) != 1u)
		return 0;
	    muxrate = br->getBits(22);
	    if (br->getBits(1) != 1u)
		return 0;
	}
	int offset = length - br->numBitsLeft()/8;
	//LOGD("*********************parsePackHeader out:offset = %d*********************",offset);
	return offset;
}


int  parseSystemHeader(ABitReader * br)
{
	int length = br->numBitsLeft()/8;
	if(length < 6)
	{
		LOGD("data too less");
		return 0;
	}
	//LOGD("*********************parseSystemHeader in :length = %d*********************",length);
#if 1        // Scan 1024bytes to get start code
        const uint8_t *data = br->data();
        bool found = false;
	for(int i=0; i<length-3; i++)
	{
	    if (!memcmp(&data[i], "\x00\x00\x01\xBB", 4)) {
                found = true;
		break;
	    }
            br->skipBits(8);        // get the actual offset
        }
        if (!found)
        {
             LOGD("Not found SYSTEM_START_CODE");
             return 0;
        }
        else
        {
             br->skipBits(8*4);   // skip the start code
        }

	length = br->numBitsLeft()/8;
	if(length < 2)
	{
		LOGD("data too less");
		return 0;
	}
#else
	if((br->getBits(32))!=SYSTEM_START_CODE)
      {
		LOGE("SYSTEM_START_CODE check error");
		return 0;
	}
#endif
	size_t header_lenth = br->getBits(16);
	if(header_lenth > br->numBitsLeft()/8)
	{
		LOGD("data too less");
		return 0;
	}
	//LOGD("-------------------header_lenth = %d--------------------",header_lenth);
	    if (br->getBits(1) != 1u)
		return 0;
	int rate_bound = br->getBits(22);
	    if (br->getBits(1) != 1u)
		return 0;
	int audio_bound = br->getBits(6);
	unsigned fixed_flag = br->getBits(1);
	unsigned CSPS_flag = br->getBits(1);
	unsigned system_audio_lock_flag = br->getBits(1);
	unsigned system_video_lock_flag = br->getBits(1);
	    if (br->getBits(1) != 1u)
		return 0;
	unsigned video_bound = br->getBits(5);
	unsigned packet_rate_restriction_flag = br->getBits(1);
	br->skipBits(7);//skip reserved_bits
	int leftsize = header_lenth - 6;
	//LOGD("-------------------leftsize = %d--------------------",leftsize);
	while(leftsize >= 3)
	{
		unsigned stream_id = br->getBits(8);
		//LOGD("------------------------stream_id = %d-----------------",stream_id);
		if(stream_id >= 0xBC || stream_id == 0xB8 || stream_id == 0xB9)
		{
			br->skipBits(16);
			leftsize -= 3;
			continue;
		}
	    if (br->getBits(2) != 3u)
		return 0;
		unsigned P_STD_buffer_bound_scale = br->getBits(1);
		int P_STD_buffer_size_bound = br->getBits(13);
		//save stream info

		leftsize -= 3;
	}
	//LOGD("-------------------------mESSources.size = %d----------------",mESSources.size());
	//LOGD("*********************parseSystemHeader out:%d*********************",length - br->numBitsLeft()/8);
	return length - br->numBitsLeft()/8;

}
#endif




bool SniffMPEG2PS(
        const sp<DataSource> &source, String8 *mimeType, float *confidence,
        sp<AMessage> *) {

#ifndef ANDROID_DEFAULT_CODE

      LOGD("*******************SniffMPEGPS in *************");
	 
	uint8_t readbuff[1024];
	memset(readbuff,0,1024);
	int length = source->readAt(0,readbuff,1024);
	LOGD("*********************source->readAt length = %d*********************",length);
	LOGD("*********************readbuff:%02x %02x %02x %02x %02x ;%02x %02x %02x %02x %02x *********************",*(readbuff+0),*(readbuff+1),*(readbuff+2),*(readbuff+3),*(readbuff+4),*(readbuff+5),*(readbuff+6),*(readbuff+7),*(readbuff+8),*(readbuff+9));
	if(length < 0)
	{
		LOGV("*********************source->readAt ERROR:length = %d*********************",length);
		return false;
	}
	ABitReader br(readbuff,(size_t)length);
	if (!parsePackHeader(&br))
	{
		LOGV("*********************parsePackHeader ERROR*********************");
		return false;
	}
	if(!parseSystemHeader(&br))
	{
		LOGV("*********************parseSystemHeader ERROR*********************");
		return false;
	}

    *confidence = 0.5f;
    mimeType->setTo(MEDIA_MIMETYPE_CONTAINER_MPEG2PS);

#else
        
    uint8_t header[5];
    if (source->readAt(0, header, sizeof(header)) < (ssize_t)sizeof(header)) {
         LOGE("SniffMPEG2PS: error1");
        return false;
    }

    if (memcmp("\x00\x00\x01\xba", header, 4) || (header[4] >> 6) != 1) {
        LOGE("SniffMPEG2PS: error2");
        return false;
    }

    *confidence = 0.25f;  // Slightly larger than .mp3 extractor's confidence

    mimeType->setTo(MEDIA_MIMETYPE_CONTAINER_MPEG2PS);
    LOGE("SniffMPEG2PS: is MPEG2PS");
#endif
    return true;
}

}  // namespace android
