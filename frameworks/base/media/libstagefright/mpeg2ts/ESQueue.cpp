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
#define LOG_TAG "ESQueue"
#include <media/stagefright/foundation/ADebug.h>

#include "ESQueue.h"

#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/foundation/ABitReader.h>
#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>

#include "include/avc_utils.h"

namespace android {

ElementaryStreamQueue::ElementaryStreamQueue(Mode mode) :
	mMode(mode)
#ifndef ANDROID_DEFAULT_CODE
, mHasGotIFrameWhenSeeking(true)
, mSeeking(false)
,mAudioFrameDuration(20000)//20ms
#endif
{
}

sp<MetaData> ElementaryStreamQueue::getFormat() {
    return mFormat;
}

void ElementaryStreamQueue::clear(bool clearFormat) {
    if (mBuffer != NULL) {
        mBuffer->setRange(0, 0);
    }

    mRangeInfos.clear();
#ifndef ANDROID_DEFAULT_CODE
    if (mMode == H264) {
    accessUnits.clear();
	}
#endif
    if (clearFormat) {
        mFormat.clear();
    }
}

static bool IsSeeminglyValidADTSHeader(const uint8_t *ptr, size_t size) {
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

#ifndef ANDROID_DEFAULT_CODE
	//in case of find the wrong error
	uint8_t number_of_raw_data_blocks_in_frame;
	number_of_raw_data_blocks_in_frame = (ptr[6]) & 0x03;
	if(number_of_raw_data_blocks_in_frame !=0)
	{
		LOGE("Error: fake header here number_of_raw_data_blocks_in_frame=%d",number_of_raw_data_blocks_in_frame);
		 return false;
	}
#endif	

    return true;
}

static bool IsSeeminglyValidMPEGAudioHeader(const uint8_t *ptr, size_t size) {
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

status_t ElementaryStreamQueue::appendData(
        const void *data, size_t size, int64_t timeUs) {
    if (mBuffer == NULL || mBuffer->size() == 0) {
        switch (mMode) {
            case H264:
            case MPEG_VIDEO:
            {
#if 0
                if (size < 4 || memcmp("\x00\x00\x00\x01", data, 4)) {
                    return ERROR_MALFORMED;
                }
#else
                uint8_t *ptr = (uint8_t *)data;

                ssize_t startOffset = -1;
                for (size_t i = 0; i + 3 < size; ++i) {
                    if (!memcmp("\x00\x00\x00\x01", &ptr[i], 4)) {
                        startOffset = i;
                        break;
                    }
                }

                if (startOffset < 0) {
                    return ERROR_MALFORMED;
                }

                if (startOffset > 0) {
                    LOGE("found something resembling an H.264/MPEG syncword at "
                         "offset %ld",
                         startOffset);
                }

                data = &ptr[startOffset];
                size -= startOffset;
#endif
                break;
            }

            case MPEG4_VIDEO:
            {
#if 0
                if (size < 3 || memcmp("\x00\x00\x01", data, 3)) {
                    return ERROR_MALFORMED;
                }
#else
                uint8_t *ptr = (uint8_t *)data;

                ssize_t startOffset = -1;
                for (size_t i = 0; i + 2 < size; ++i) {
                    if (!memcmp("\x00\x00\x01", &ptr[i], 3)) {
                        startOffset = i;
                        break;
                    }
                }

                if (startOffset < 0) {
                    return ERROR_MALFORMED;
                }

                if (startOffset > 0) {
                    LOGE("found something resembling an H.264/MPEG syncword at "
                         "offset %ld",
                         startOffset);
                }

                data = &ptr[startOffset];
                size -= startOffset;
#endif
                break;
            }

            case AAC:
            {
                uint8_t *ptr = (uint8_t *)data;

#if 0
                if (size < 2 || ptr[0] != 0xff || (ptr[1] >> 4) != 0x0f) {
                    return ERROR_MALFORMED;
                }
#else
                ssize_t startOffset = -1;
                for (size_t i = 0; i < size; ++i) {
                    if (IsSeeminglyValidADTSHeader(&ptr[i], size - i)) {
                        startOffset = i;
                        break;
                    }
                }

                if (startOffset < 0) {
                    return ERROR_MALFORMED;
                }

                if (startOffset > 0) {
                    LOGI("found something resembling an AAC syncword at offset %ld",
                         startOffset);
                }

                data = &ptr[startOffset];
                size -= startOffset;
#endif
                break;
            }

            case MPEG_AUDIO:
            {
                uint8_t *ptr = (uint8_t *)data;

                ssize_t startOffset = -1;
                for (size_t i = 0; i < size; ++i) {
                    if (IsSeeminglyValidMPEGAudioHeader(&ptr[i], size - i)) {
                        startOffset = i;
                        break;
                    }
                }

                if (startOffset < 0) {
                    return ERROR_MALFORMED;
                }

                if (startOffset > 0) {
                    LOGI("found something resembling an MPEG audio "
                         "syncword at offset %ld",
                         startOffset);
                }

                data = &ptr[startOffset];
                size -= startOffset;
                break;
            }

            default:
                TRESPASS();
                break;
        }
    }

    size_t neededSize = (mBuffer == NULL ? 0 : mBuffer->size()) + size;
    if (mBuffer == NULL || neededSize > mBuffer->capacity()) {
        neededSize = (neededSize + 65535) & ~65535;

        LOGV("resizing buffer to size %d", neededSize);

        sp<ABuffer> buffer = new ABuffer(neededSize);
        if (mBuffer != NULL) {
            memcpy(buffer->data(), mBuffer->data(), mBuffer->size());
            buffer->setRange(0, mBuffer->size());
        } else {
            buffer->setRange(0, 0);
        }

        mBuffer = buffer;
    }

    memcpy(mBuffer->data() + mBuffer->size(), data, size);
    mBuffer->setRange(0, mBuffer->size() + size);

    RangeInfo info;
    info.mLength = size;
    info.mTimestampUs = timeUs;
    mRangeInfos.push_back(info);

#if 0
    if (mMode == AAC) {
        LOGI("size = %d, timeUs = %.2f secs", size, timeUs / 1E6);
        hexdump(data, size);
    }
#endif

    return OK;
}

sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnit() {
    switch (mMode) {
        case H264:
            return dequeueAccessUnitH264();
        case AAC:
            return dequeueAccessUnitAAC();
        case MPEG_VIDEO:
            return dequeueAccessUnitMPEGVideo();
        case MPEG4_VIDEO:
            return dequeueAccessUnitMPEG4Video();
        default:
            CHECK_EQ((unsigned)mMode, (unsigned)MPEG_AUDIO);
            return dequeueAccessUnitMPEGAudio();
    }
}
/*
	adts_frame()
	{
		adts_fixed_header();//28bit
		adts_variable_header();//28bit
		if (number_of_raw_data_blocks_in_frame == 0) {
			adts_error_check();  // 16bit
			raw_data_block();//frame data
		}
		else {
			adts_header_error_check();
			for (i = 0; i <= number_of_raw_data_blocks_in_frame;i++) {
				raw_data_block();
				adts_raw_data_block_error_check();
			}
		}
	}
*/
sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnitAAC() {
    Vector<size_t> ranges;
    Vector<size_t> frameOffsets;
    Vector<size_t> frameSizes;
    size_t auSize = 0;

    size_t offset = 0;
#ifndef ANDROID_DEFAULT_CODE
    bool fristGetFormat=false;
    bool fristGetFormatError=false;
#endif

    while (offset + 7 <= mBuffer->size()) {
        ABitReader bits(mBuffer->data() + offset, mBuffer->size() - offset);

        // adts_fixed_header = 7bytes

#ifndef ANDROID_DEFAULT_CODE
		uint8_t *data = mBuffer->data() + offset;
		size_t size = mBuffer->size() - offset;
		size_t invalidLength = 0;
		bool hasInvalidADTSHeader = false;
		uint32_t startCode = bits.getBits(12);
		//adts sync code: 0xFFF
		
		//[qian] find the sync code to make sure it the start of a aac frame
		while (!((Compare_EQ(startCode, 0xfffu)).empty())) {
			ABitReader tempBits(data, size);
			hasInvalidADTSHeader = true;
			uint8_t *ptr = data;
			ssize_t startOffset = -1;

			for (size_t i = 1; i < size; ++i) {
				if (IsSeeminglyValidADTSHeader(&ptr[i], size - i)) {
					startOffset = i;
					break;
				}
			}
			if (startOffset < 0) {
				LOGE("error here , no header???, lefte byte=%d",mBuffer->size() - offset);
				return NULL;
			}

			data = &ptr[startOffset];
			size -= startOffset;
			offset += startOffset;
			invalidLength += startOffset;

			bits.skipBits(startOffset * 8);
			tempBits.skipBits(startOffset * 8);
			startCode = tempBits.getBits(12);
		}
#else
        CHECK_EQ(bits.getBits(12), 0xfffu);
#endif

/*

		adts_fixed_header()//7bytes
		{
			syncword; 12 bslbf
			ID; 1 bslbf
			layer; 2 uimsbf
			protection_absent; 1 bslbf
			profile; 2 uimsbf
			sampling_frequency_index; 4 uimsbf
			private_bit; 1 bslbf
			channel_configuration; 3 uimsbf
			original_copy; 1 bslbf
			home; 1 bslbf
		}

*/
        bits.skipBits(3);  // ID, layer
        bool protection_absent = bits.getBits(1) != 0;

        if (mFormat == NULL) {

            unsigned profile = bits.getBits(2);
#ifndef ANDROID_DEFAULT_CODE
		if(profile==3)
		{
			LOGE("error in check aac profile");
			fristGetFormatError=true;
		}		
#else
		CHECK_NE(profile, 3u);
#endif
            unsigned sampling_freq_index = bits.getBits(4);
            bits.getBits(1);  // private_bit
            unsigned channel_configuration = bits.getBits(3);
			
#ifndef ANDROID_DEFAULT_CODE
		if(channel_configuration==0)
		{
			
			fristGetFormatError=true;
			LOGE("error in check aac channel_configuration ");
		}	
#else
		 CHECK_NE(channel_configuration, 0u);
#endif           
            bits.skipBits(2);  // original_copy, home

            mFormat = MakeAACCodecSpecificData(
                    profile, sampling_freq_index, channel_configuration);

            int32_t sampleRate;
            int32_t numChannels;
            CHECK(mFormat->findInt32(kKeySampleRate, &sampleRate));
            CHECK(mFormat->findInt32(kKeyChannelCount, &numChannels));
	      
#ifndef ANDROID_DEFAULT_CODE
            fristGetFormat=true;
		if(sampleRate>0)
			mAudioFrameDuration = 1024*1000000ll/sampleRate;//us
		LOGE("AACmAudioFrameDuration %lld sampleRate=%d",mAudioFrameDuration,sampleRate);
		
#endif

            LOGE("found AAC codec config (%d Hz, %d channels)",
                 sampleRate, numChannels);
        } else {
            // profile_ObjectType, sampling_frequency_index, private_bits,
            // channel_configuration, original_copy, home
            bits.skipBits(12);
        }

        // adts_variable_header

/*
		adts_variable_header()//7bytes
		{
			copyright_identification_bit; 1 bslbf
			copyright_identification_start; 1 bslbf
			aac_frame_length; 13 bslbf//Length of the frame including headers and error_check in bytes
			adts_buffer_fullness; 11 bslbf
			number_of_raw_data_blocks_in_frame; 2 uimsfb
		}

*/

        // copyright_identification_bit, copyright_identification_start
        bits.skipBits(2);

        unsigned aac_frame_length = bits.getBits(13);

        bits.skipBits(11);  // adts_buffer_fullness

        unsigned number_of_raw_data_blocks_in_frame = bits.getBits(2);
		//[qian]Number of raw_data_block()¡¯s that are multiplexed in the
         //adts_frame() is equal to number_of_raw_data_blocks_in_frame
			//+ 1. The minimum value is 0 indicating 1 raw_data_block()
        if (number_of_raw_data_blocks_in_frame != 0) {
            // To be implemented.
            LOGE("[TS_ERROR]only support number_of_raw_data_blocks_in_frame=0, realy=%d\n",number_of_raw_data_blocks_in_frame);
            TRESPASS();
        }

        if (offset + aac_frame_length > mBuffer->size()) {
			LOGD("break aac_frame_length=%d,mBuffer->size()=%d",aac_frame_length,mBuffer->size());
			//[qian] notice this
            break;
        }

        size_t headerSize = protection_absent ? 7 : 9;


/*
		adts_error_check()
		{
			if (protection_absent == ¡®0¡¯)
				crc_check; 16 rpchof
		}
*/

		
#ifndef ANDROID_DEFAULT_CODE

      if(fristGetFormatError){
	  	  LOGE("Error skip this AAC frame");
		  fetchTimestamp(aac_frame_length + invalidLength);
	        offset += aac_frame_length;
		  if(mFormat != NULL)  mFormat =NULL;
		   fristGetFormat=false;
		   fristGetFormatError=false;
      	}
	else //error skip this frame
	{
		if (hasInvalidADTSHeader) {
	  			ranges.push(aac_frame_length + invalidLength);
				hasInvalidADTSHeader = false;
			} else {
				ranges.push(aac_frame_length);
			}
		  frameOffsets.push(offset + headerSize);
	        frameSizes.push(aac_frame_length - headerSize);
	        auSize += aac_frame_length - headerSize;
	        offset += aac_frame_length;
	}
#else
        ranges.push(aac_frame_length);

        frameOffsets.push(offset + headerSize);
        frameSizes.push(aac_frame_length - headerSize);
        auSize += aac_frame_length - headerSize;

        offset += aac_frame_length;
#endif		
    }

    if (offset == 0) {
        return NULL;
    }

	//[qian] audio a ts packet should be a frame?
    int64_t timeUs = -1;

    for (size_t i = 0; i < ranges.size(); ++i) {
        int64_t tmpUs = fetchTimestamp(ranges.itemAt(i));

        if (i == 0) {
            timeUs = tmpUs;
        }
    }

    sp<ABuffer> accessUnit = new ABuffer(auSize);
    size_t dstOffset = 0;
    for (size_t i = 0; i < frameOffsets.size(); ++i) {
        size_t frameOffset = frameOffsets.itemAt(i);

        memcpy(accessUnit->data() + dstOffset,
               mBuffer->data() + frameOffset,
               frameSizes.itemAt(i));

        dstOffset += frameSizes.itemAt(i);
    }
#ifndef ANDROID_DEFAULT_CODE
    if (fristGetFormat) {//[qian]?
		mFormat->setInt32(kKeyMaxInputSize, offset * 1.3);
		LOGE("AAC kKeyMaxInputSize=%d ", offset);
    	}
#endif
    memmove(mBuffer->data(), mBuffer->data() + offset,
            mBuffer->size() - offset);
    mBuffer->setRange(0, mBuffer->size() - offset);

    if (timeUs >= 0) {
        accessUnit->meta()->setInt64("timeUs", timeUs);
    } else {
        LOGW("no time for AAC access unit");
    }
#ifndef ANDROID_DEFAULT_CODE

	if(mBuffer->size()>0 && frameOffsets.size()>0  && mRangeInfos.size() ==1)
	{
		RangeInfo *info =  &*mRangeInfos.begin();
		LOGD("qian AAC correct the timestamp from %lld to %lld,mAudioFrameDuration=%lld",info->mTimestampUs,info->mTimestampUs + mAudioFrameDuration * frameOffsets.size(),mAudioFrameDuration);
		info->mTimestampUs = info->mTimestampUs + (mAudioFrameDuration * frameOffsets.size());	

		 info =  &*mRangeInfos.begin();
		LOGE("qian AAC correct the timestamp is =%lld",info->mTimestampUs);		
	}
	
#endif	

    return accessUnit;
}

int64_t ElementaryStreamQueue::fetchTimestamp(size_t size) {
    int64_t timeUs = -1;
    bool first = true;

    while (size > 0) {
        CHECK(!mRangeInfos.empty());

        RangeInfo *info = &*mRangeInfos.begin();

	 #ifndef ANDROID_DEFAULT_CODE
	 //Add for Special MPEG File
	 if ((first == false) && (timeUs == 0xFFFFFFFF) && (info->mTimestampUs != 0x0))
	 {
	   LOGE("fetchTimestamp - Change: %lld  %lld \n", timeUs, info->mTimestampUs);
	   timeUs = info->mTimestampUs;
	 }
	 #endif //#ifndef ANDROID_DEFAULT_CODE

        if (first) {
            timeUs = info->mTimestampUs;
            first = false;
        }

        if (info->mLength > size) {
            info->mLength -= size;

            if (first) {
                info->mTimestampUs = -1;
            }

            size = 0;
        } else {
            size -= info->mLength;

            mRangeInfos.erase(mRangeInfos.begin());
            info = NULL;
        }
    }

    if (timeUs == 0ll) {
        LOGV("Returning 0 timestamp");
    }

    if (timeUs == 0ll) {
        LOGV("Returning 0 timestamp");
    }

    return timeUs;
}

struct NALPosition {
    size_t nalOffset;
    size_t nalSize;
};

#ifndef ANDROID_DEFAULT_CODE
void ElementaryStreamQueue::setSeeking() {
	mSeeking = true;
	mHasGotIFrameWhenSeeking = false;
}
sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnitH264() {
	if (accessUnits.empty()) {
    const uint8_t *data = mBuffer->data();
    size_t size = mBuffer->size();

    Vector<NALPosition> nals;

    size_t totalSize = 0;

    status_t err;
    const uint8_t *nalStart;
    size_t nalSize;
    bool foundSlice = false;
    bool over = false;
    unsigned slice_type =0;		
      while (!over && (err = getNextNALUnit(&data, &size, &nalStart, &nalSize)) == OK)  
	{
        	CHECK_GT(nalSize, 0u);

		 unsigned nalType = nalStart[0] & 0x1f;

		if((nalType >0 && nalType <6) || nalType == 19 )
	      {
			ABitReader br(nalStart + 1, nalSize);
			unsigned first_mb_in_slice = parseUE(&br);//[qian]?
		       slice_type = parseUE(&br);
		}  
		
	       if (mSeeking && !mHasGotIFrameWhenSeeking) 
		{
		  			
			if (nalType == 5 || slice_type ==2 ||slice_type==7) {//[qian]IDR
			       LOGE("%s  nalType=%d slice_type=%d ",__FUNCTION__,nalType,slice_type);
				mHasGotIFrameWhenSeeking = true;
				mSeeking = false;
			}
		}


	        bool flush = false;

	        if (nalType == 1 || nalType == 5) {
	            if (foundSlice) {
	                ABitReader br(nalStart + 1, nalSize);
	                unsigned first_mb_in_slice = parseUE(&br);

	                if (first_mb_in_slice == 0) {
	                    // This slice starts a new frame.

	                    flush = true;
	                }
	            }

	            foundSlice = true;
	        } else if ((nalType == 9 || nalType == 7) && foundSlice) {
	            // Access unit delimiter and SPS will be associated with the
	            // next frame.

	            flush = true;
	        }

	        if (flush) {
	            // The access unit will contain all nal units up to, but excluding
	            // the current one, separated by 0x00 0x00 0x00 0x01 startcodes.

	            size_t auSize = 4 * nals.size() + totalSize;
	            sp<ABuffer> MultiAccessUnit = new ABuffer(auSize);


            size_t dstOffset = 0;

		LOGV("accessUnit contains nal types %s", out.c_str());

            const NALPosition &pos0 = nals.itemAt(nals.size() - 1);
            size_t nextScan = pos0.nalOffset + pos0.nalSize;

            int64_t timeUs = fetchTimestamp(nextScan);
            CHECK_GE(timeUs, 0ll);
			
            for (size_t i = 0; i < nals.size(); ++i) {
		  
                const NALPosition &pos = nals.itemAt(i);
		   sp<ABuffer> accessUnit = new ABuffer(pos.nalSize);
                unsigned nalType = mBuffer->data()[pos.nalOffset] & 0x1f;

			 if (mHasGotIFrameWhenSeeking)
			 {
	                      memcpy(accessUnit->data()  ,mBuffer->data() + pos.nalOffset,pos.nalSize);
				  accessUnit->meta()->setInt64("timeUs", timeUs);
				  LOGV("i=%d,timeUs=%lld",i,timeUs);
				  accessUnits.push_back(accessUnit);
			}
			else
			{
			  //Drop AU
			}  
			memcpy(MultiAccessUnit->data() + dstOffset,"\x00\x00\x00\x01", 4);
		       memcpy(MultiAccessUnit->data() + dstOffset + 4,
							mBuffer->data() + pos.nalOffset, pos.nalSize);
			 dstOffset += pos.nalSize + 4;
                 
            }

            LOGV("accessUnit contains nal types %s", out.c_str());

            memmove(mBuffer->data(),
                    mBuffer->data() + nextScan,
                    mBuffer->size() - nextScan);

            mBuffer->setRange(0, mBuffer->size() - nextScan);

            if (mFormat == NULL) {
                mFormat = MakeAVCCodecSpecificData(MultiAccessUnit);
            }
            over = true;
			
        }

        NALPosition pos;
        pos.nalOffset = nalStart - mBuffer->data();
        pos.nalSize = nalSize;

        nals.push(pos);

        totalSize += nalSize;
    }
	}
	if (!accessUnits.empty()) {
		sp<ABuffer> accessUnit = *accessUnits.begin();
		accessUnits.erase(accessUnits.begin());
		return accessUnit;
	} else {
		return NULL;
	}
    
}

#else
sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnitH264() {
    const uint8_t *data = mBuffer->data();
    size_t size = mBuffer->size();

    Vector<NALPosition> nals;

    size_t totalSize = 0;

    status_t err;
    const uint8_t *nalStart;
    size_t nalSize;
    bool foundSlice = false;
    while ((err = getNextNALUnit(&data, &size, &nalStart, &nalSize)) == OK) {
        CHECK_GT(nalSize, 0u);

        unsigned nalType = nalStart[0] & 0x1f;
        bool flush = false;

        if (nalType == 1 || nalType == 5) {
            if (foundSlice) {
                ABitReader br(nalStart + 1, nalSize);
                unsigned first_mb_in_slice = parseUE(&br);

                if (first_mb_in_slice == 0) {
                    // This slice starts a new frame.

                    flush = true;
                }
            }

            foundSlice = true;
        } else if ((nalType == 9 || nalType == 7) && foundSlice) {
            // Access unit delimiter and SPS will be associated with the
            // next frame.

            flush = true;
        }

        if (flush) {
            // The access unit will contain all nal units up to, but excluding
            // the current one, separated by 0x00 0x00 0x00 0x01 startcodes.

            size_t auSize = 4 * nals.size() + totalSize;
            sp<ABuffer> accessUnit = new ABuffer(auSize);

#if !LOG_NDEBUG
            AString out;
#endif

            size_t dstOffset = 0;
            for (size_t i = 0; i < nals.size(); ++i) {
                const NALPosition &pos = nals.itemAt(i);

                unsigned nalType = mBuffer->data()[pos.nalOffset] & 0x1f;

#if !LOG_NDEBUG
                char tmp[128];
                sprintf(tmp, "0x%02x", nalType);
                if (i > 0) {
                    out.append(", ");
                }
                out.append(tmp);
#endif

                memcpy(accessUnit->data() + dstOffset, "\x00\x00\x00\x01", 4);

                memcpy(accessUnit->data() + dstOffset + 4,
                       mBuffer->data() + pos.nalOffset,
                       pos.nalSize);

                dstOffset += pos.nalSize + 4;
            }

            LOGV("accessUnit contains nal types %s", out.c_str());

            const NALPosition &pos = nals.itemAt(nals.size() - 1);
            size_t nextScan = pos.nalOffset + pos.nalSize;

            memmove(mBuffer->data(),
                    mBuffer->data() + nextScan,
                    mBuffer->size() - nextScan);

            mBuffer->setRange(0, mBuffer->size() - nextScan);

            int64_t timeUs = fetchTimestamp(nextScan);
            CHECK_GE(timeUs, 0ll);

            accessUnit->meta()->setInt64("timeUs", timeUs);

            if (mFormat == NULL) {
                mFormat = MakeAVCCodecSpecificData(accessUnit);
            }

            return accessUnit;
        }

        NALPosition pos;
        pos.nalOffset = nalStart - mBuffer->data();
        pos.nalSize = nalSize;

        nals.push(pos);

        totalSize += nalSize;
    }
    CHECK_EQ(err, (status_t)-EAGAIN);

    return NULL;
}
#endif


sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnitMPEGAudio() {
    uint8_t *data = mBuffer->data();
    size_t size = mBuffer->size();

    if (size < 4) {
        return NULL;
    }

    uint32_t header = U32_AT(data);

    size_t frameSize;
    int samplingRate, numChannels, bitrate, numSamples,Meta_samplerate;
#ifndef ANDROID_DEFAULT_CODE
    ssize_t offset = 0;
	bool retb=false;
	if(mFormat== NULL)
	{
		 Meta_samplerate=0;
	}
	else
	{
		mFormat->findInt32(kKeySampleRate, &Meta_samplerate);
	}
    while (1) {
		
		retb = GetMPEGAudioFrameSize(header, &frameSize, &samplingRate,
				&numChannels, &bitrate, &numSamples);
		if(!retb || (Meta_samplerate!=0 && samplingRate!=Meta_samplerate))
		{
			
			uint8_t *ptr = data;
			ssize_t startOffset = -1;
			for (size_t i = 1; i < size; ++i) {
				if (IsSeeminglyValidMPEGAudioHeader(&ptr[i], size - i)) {
					startOffset = i;

					break;
				}
			}
			if (startOffset < 0) {
				return NULL;
			}
		 
			data = &ptr[startOffset];
			size -= startOffset;
			offset += startOffset;
			header = U32_AT(data);
		}
		else if(retb)
		{
			break;
		}
			
			
	}
    offset += frameSize;
#else
    CHECK(GetMPEGAudioFrameSize(
                header, &frameSize, &samplingRate, &numChannels,
                &bitrate, &numSamples));
#endif

    if (size < frameSize) {
        return NULL;
    }

    unsigned layer = 4 - ((header >> 17) & 3);

    sp<ABuffer> accessUnit = new ABuffer(frameSize);
    memcpy(accessUnit->data(), data, frameSize);
#ifndef ANDROID_DEFAULT_CODE
	memmove(mBuffer->data(), mBuffer->data() + offset, mBuffer->size() - offset);

	mBuffer->setRange(0, mBuffer->size() - offset);

	int64_t timeUs = fetchTimestamp(offset);
#else
    memmove(mBuffer->data(),
            mBuffer->data() + frameSize,
            mBuffer->size() - frameSize);

    mBuffer->setRange(0, mBuffer->size() - frameSize);

    int64_t timeUs = fetchTimestamp(frameSize);
#endif
    CHECK_GE(timeUs, 0ll);

    accessUnit->meta()->setInt64("timeUs", timeUs);

    if (mFormat == NULL) {
        mFormat = new MetaData;
        switch (layer) {
            case 1:
                mFormat->setCString(
                        kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_MPEG_LAYER_I);
                break;
            case 2:
#ifndef ANDROID_DEFAULT_CODE
                mFormat->setCString(
                        kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_MPEG);
#else                    
                mFormat->setCString(
                        kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_MPEG_LAYER_II);
#endif
                break;
            case 3:
                mFormat->setCString(
                        kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_MPEG);
                break;
            default:
                TRESPASS();
        }
        mFormat->setInt32(kKeySampleRate, samplingRate);
        mFormat->setInt32(kKeyChannelCount, numChannels);
    }

    return accessUnit;
}

static void EncodeSize14(uint8_t **_ptr, size_t size) {
    CHECK_LE(size, 0x3fff);

    uint8_t *ptr = *_ptr;

    *ptr++ = 0x80 | (size >> 7);
    *ptr++ = size & 0x7f;

    *_ptr = ptr;
}

static sp<ABuffer> MakeMPEGVideoESDS(const sp<ABuffer> &csd) {
    sp<ABuffer> esds = new ABuffer(csd->size() + 25);

    uint8_t *ptr = esds->data();
    *ptr++ = 0x03;
    EncodeSize14(&ptr, 22 + csd->size());

    *ptr++ = 0x00;  // ES_ID
    *ptr++ = 0x00;

    *ptr++ = 0x00;  // streamDependenceFlag, URL_Flag, OCRstreamFlag

    *ptr++ = 0x04;
    EncodeSize14(&ptr, 16 + csd->size());

    *ptr++ = 0x40;  // Audio ISO/IEC 14496-3

    for (size_t i = 0; i < 12; ++i) {
        *ptr++ = 0x00;
    }

    *ptr++ = 0x05;
    EncodeSize14(&ptr, csd->size());

    memcpy(ptr, csd->data(), csd->size());

    return esds;
}

sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnitMPEGVideo() {
    const uint8_t *data = mBuffer->data();
    size_t size = mBuffer->size();

    bool sawPictureStart = false;
    int pprevStartCode = -1;
    int prevStartCode = -1;
    int currentStartCode = -1;

    size_t offset = 0;
    while (offset + 3 < size) {
        if (memcmp(&data[offset], "\x00\x00\x01", 3)) {
            ++offset;
            continue;
        }

        pprevStartCode = prevStartCode;
        prevStartCode = currentStartCode;
        currentStartCode = data[offset + 3];

        if (currentStartCode == 0xb3 && mFormat == NULL) {
            memmove(mBuffer->data(), mBuffer->data() + offset, size - offset);
            size -= offset;
            (void)fetchTimestamp(offset);
            offset = 0;
            mBuffer->setRange(0, size);
        }

        if ((prevStartCode == 0xb3 && currentStartCode != 0xb5)
                || (pprevStartCode == 0xb3 && prevStartCode == 0xb5)) {
            // seqHeader without/with extension

            if (mFormat == NULL) {
                CHECK_GE(size, 7u);

                unsigned width =
                    (data[4] << 4) | data[5] >> 4;

                unsigned height =
                    ((data[5] & 0x0f) << 8) | data[6];

                mFormat = new MetaData;
                mFormat->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_MPEG2);
                mFormat->setInt32(kKeyWidth, width);
                mFormat->setInt32(kKeyHeight, height);

                LOGI("found MPEG2 video codec config (%d x %d)", width, height);

                sp<ABuffer> csd = new ABuffer(offset);
                memcpy(csd->data(), data, offset);

                memmove(mBuffer->data(),
                        mBuffer->data() + offset,
                        mBuffer->size() - offset);

                mBuffer->setRange(0, mBuffer->size() - offset);
                size -= offset;
                (void)fetchTimestamp(offset);
                offset = 0;

                // hexdump(csd->data(), csd->size());

                sp<ABuffer> esds = MakeMPEGVideoESDS(csd);
                mFormat->setData(
                        kKeyESDS, kTypeESDS, esds->data(), esds->size());

                return NULL;
            }
        }

        if (mFormat != NULL && currentStartCode == 0x00) {
            // Picture start

            if (!sawPictureStart) {
                sawPictureStart = true;
            } else {
                sp<ABuffer> accessUnit = new ABuffer(offset);
                memcpy(accessUnit->data(), data, offset);

                memmove(mBuffer->data(),
                        mBuffer->data() + offset,
                        mBuffer->size() - offset);

                mBuffer->setRange(0, mBuffer->size() - offset);

                int64_t timeUs = fetchTimestamp(offset);
                CHECK_GE(timeUs, 0ll);

                offset = 0;

                accessUnit->meta()->setInt64("timeUs", timeUs);

                LOGV("returning MPEG video access unit at time %lld us",
                      timeUs);

                // hexdump(accessUnit->data(), accessUnit->size());

                return accessUnit;
            }
        }

        ++offset;
    }

    return NULL;
}

static ssize_t getNextChunkSize(
        const uint8_t *data, size_t size) {
    static const char kStartCode[] = "\x00\x00\x01";

    if (size < 3) {
        return -EAGAIN;
    }

    if (memcmp(kStartCode, data, 3)) {
        TRESPASS();
    }

    size_t offset = 3;
    while (offset + 2 < size) {
        if (!memcmp(&data[offset], kStartCode, 3)) {
            return offset;
        }

        ++offset;
    }

    return -EAGAIN;
}

sp<ABuffer> ElementaryStreamQueue::dequeueAccessUnitMPEG4Video() {
    uint8_t *data = mBuffer->data();
    size_t size = mBuffer->size();

    enum {
        SKIP_TO_VISUAL_OBJECT_SEQ_START,
        EXPECT_VISUAL_OBJECT_START,
        EXPECT_VO_START,
        EXPECT_VOL_START,
        WAIT_FOR_VOP_START,
        SKIP_TO_VOP_START,

    } state;

    if (mFormat == NULL) {
        state = SKIP_TO_VISUAL_OBJECT_SEQ_START;//[qian] need VOS header
    } else {
        state = SKIP_TO_VOP_START;//[qian] just need frame (VOP)
    }

    int32_t width = -1, height = -1;

    size_t offset = 0;
    ssize_t chunkSize;
    while ((chunkSize = getNextChunkSize(
                    &data[offset], size - offset)) > 0) {
        bool discard = false;

        unsigned chunkType = data[offset + 3];

        switch (state) {
            case SKIP_TO_VISUAL_OBJECT_SEQ_START:
            {
                if (chunkType == 0xb0) {
                    // Discard anything before this marker.

                    state = EXPECT_VISUAL_OBJECT_START;
                } else {
                    discard = true;
                }
                break;
            }

            case EXPECT_VISUAL_OBJECT_START:
            {
                CHECK_EQ(chunkType, 0xb5);
                state = EXPECT_VO_START;
                break;
            }

            case EXPECT_VO_START:
            {
                CHECK_LE(chunkType, 0x1f);
                state = EXPECT_VOL_START;
                break;
            }

            case EXPECT_VOL_START:
            {
                CHECK((chunkType & 0xf0) == 0x20);

                CHECK(ExtractDimensionsFromVOLHeader(
                            &data[offset], chunkSize,
                            &width, &height));

                state = WAIT_FOR_VOP_START;
                break;
            }

            case WAIT_FOR_VOP_START:
            {
                if (chunkType == 0xb3 || chunkType == 0xb6) {
                    // group of VOP or VOP start.

                    mFormat = new MetaData;
                    mFormat->setCString(
                            kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_MPEG4);

                    mFormat->setInt32(kKeyWidth, width);
                    mFormat->setInt32(kKeyHeight, height);

                    LOGI("found MPEG4 video codec config (%d x %d)",
                         width, height);

                    sp<ABuffer> csd = new ABuffer(offset);
                    memcpy(csd->data(), data, offset);//[qian]before 1st vop, should the header

                    // hexdump(csd->data(), csd->size());

                    sp<ABuffer> esds = MakeMPEGVideoESDS(csd);
                    mFormat->setData(
                            kKeyESDS, kTypeESDS,
                            esds->data(), esds->size());

                    discard = true;
                    state = SKIP_TO_VOP_START;
                }

                break;
            }

            case SKIP_TO_VOP_START:
            {
                if (chunkType == 0xb6) {
                    offset += chunkSize;

                    sp<ABuffer> accessUnit = new ABuffer(offset);
                    memcpy(accessUnit->data(), data, offset);
#ifndef ANDROID_DEFAULT_CODE
				if (mSeeking && !mHasGotIFrameWhenSeeking) {
					switch (data[4] & 0xC0) {
					case 0x00:
						mHasGotIFrameWhenSeeking = true;
						mSeeking = false;
						LOGI("I frame");
						break;
					case 0x40:
						LOGI("P frame");
						continue;
					case 0x80:
						LOGI("B frame");
						continue;
					default:
						LOGI("default");
						continue;
}
					}
#endif

                    memmove(data, &data[offset], size - offset);
                    size -= offset;
                    mBuffer->setRange(0, size);

                    int64_t timeUs = fetchTimestamp(offset);
                    CHECK_GE(timeUs, 0ll);

                    offset = 0;

                    accessUnit->meta()->setInt64("timeUs", timeUs);

                    LOGV("returning MPEG4 video access unit at time %lld us",
                         timeUs);

                    // hexdump(accessUnit->data(), accessUnit->size());

                    return accessUnit;
                } else if (chunkType != 0xb3) {
                    offset += chunkSize;
                    discard = true;
                }

                break;
            }

            default:
                TRESPASS();
        }

        if (discard) {
            (void)fetchTimestamp(offset);
            memmove(data, &data[offset], size - offset);
            size -= offset;
            offset = 0;
            mBuffer->setRange(0, size);
        } else {
            offset += chunkSize;
        }
    }

    return NULL;
}

}  // namespace android
