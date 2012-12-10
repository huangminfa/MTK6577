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
#define LOG_TAG "ATSParser"
#include <utils/Log.h>

#include "ATSParser.h"

#include "AnotherPacketSource.h"
#include "ESQueue.h"
#include "include/avc_utils.h"

#include <media/stagefright/foundation/ABitReader.h>
#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MetaData.h>
#include <media/IStreamSource.h>
#include <utils/KeyedVector.h>

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DEMUXER_BLOCK_CAPABILITY   
#ifdef MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
#include "vdec_drv_if.h"
#include "val_types.h"
#endif
#endif

#include <cutils/properties.h>
typedef enum
{
    AAC_NULL = 0,
    AAC_AAC_MAIN = 1,
    AAC_AAC_LC = 2,
    AAC_AAC_SSR = 3,
    AAC_AAC_LTP = 4,
    AAC_SBR = 5,
    AAC_AAC_SCALABLE = 6,
    AAC_TWINVQ = 7,
    AAC_ER_AAC_LC = 17,
    AAC_ER_AAC_LTP = 19,
    AAC_ER_AAC_SCALABLE = 20,
    AAC_ER_TWINVQ = 21,
    AAC_ER_BSAC = 22,
    AAC_ER_AAC_LD = 23
}AACObjectType ;
#endif //#ifndef ANDROID_DEFAULT_CODE

//#undef QUERY_FROM_DRV
namespace android {

// I want the expression "y" evaluated even if verbose logging is off.
#define MY_LOGV(x, y) \
    do { unsigned tmp = y; LOGV(x, tmp); } while (0)

static const size_t kTSPacketSize = 188;

struct ATSParser::Program : public RefBase {
    Program(ATSParser *parser, unsigned programNumber, unsigned programMapPID);

    bool parsePID(
            unsigned pid, unsigned payload_unit_start_indicator,
            ABitReader *br, status_t *err);

    void signalDiscontinuity(
            DiscontinuityType type, const sp<AMessage> &extra);

    void signalEOS(status_t finalResult);

    sp<MediaSource> getSource(SourceType type);

#ifndef ANDROID_DEFAULT_CODE
    int64_t getPTS();
    bool firstPTSIsValid();
    bool getDequeueState();
    void notifyFlush();
#endif

    int64_t convertPTSToTimestamp(uint64_t PTS);
    bool PTSTimeDeltaEstablished() const {
        return mFirstPTSValid;
    }

    unsigned number() const { return mProgramNumber; }

    void updateProgramMapPID(unsigned programMapPID) {
        mProgramMapPID = programMapPID;
    }
#ifndef ANDROID_DEFAULT_CODE
    ATSParser *mParser;
#endif

private:
#ifdef ANDROID_DEFAULT_CODE
    ATSParser *mParser;
#endif
    unsigned mProgramNumber;
    unsigned mProgramMapPID;
    KeyedVector<unsigned, sp<Stream> > mStreams;
    bool mFirstPTSValid;
    uint64_t mFirstPTS;
#ifndef ANDROID_DEFAULT_CODE
    uint64_t mFirstTimestamp;
    struct PendingTimestamp{
        int64_t us;
        int32_t pendingtimes;
    } mPendingTimestamp;
#endif

    status_t parseProgramMap(ABitReader *br);

    DISALLOW_EVIL_CONSTRUCTORS(Program);
};

struct ATSParser::Stream : public RefBase {
    Stream(Program *program, unsigned elementaryPID, unsigned streamType);

    unsigned type() const { return mStreamType; }
    unsigned pid() const { return mElementaryPID; }
    void setPID(unsigned pid) { mElementaryPID = pid; }

    status_t parse(
            unsigned payload_unit_start_indicator,
            ABitReader *br);

    void signalDiscontinuity(
            DiscontinuityType type, const sp<AMessage> &extra);

    void signalEOS(status_t finalResult);

    sp<MediaSource> getSource(SourceType type);

#ifndef ANDROID_DEFAULT_CODE
    int64_t getPTS();
	bool isSupportedStream( sp<MetaData> StreamMeta);
    void signalDiscontinuity_local(DiscontinuityType type);
    bool BufferIsEmpty(){
        LOGD("buffersize = %d", (int)mBuffer->size());
        return (mBuffer->size() == 0);
    };
#endif

protected:
    virtual ~Stream();

private:
    Program *mProgram;
    unsigned mElementaryPID;
    unsigned mStreamType;

#ifndef ANDROID_DEFAULT_CODE
    bool seeking;
    int64_t mMaxTimeUs;
    bool mSupportedStream;
 //   int64_t mFirstPTS;
//    bool mFirstPTSValid;
#endif

    sp<ABuffer> mBuffer;//[qian] use to put a complete PES packet
    sp<AnotherPacketSource> mSource;//[qian] use to put the access unit and meta, setup a source
    bool mPayloadStarted;
    ElementaryStreamQueue *mQueue;//[qian]use to put the ES data, origional real payload

    status_t flush();
    status_t parsePES(ABitReader *br);

    void onPayloadData(
            unsigned PTS_DTS_flags, uint64_t PTS, uint64_t DTS,
            const uint8_t *data, size_t size);

    void extractAACFrames(const sp<ABuffer> &buffer);
    bool isAudio() const;
    bool isVideo() const;

    DISALLOW_EVIL_CONSTRUCTORS(Stream);
};

////////////////////////////////////////////////////////////////////////////////

ATSParser::Program::Program(
        ATSParser *parser, unsigned programNumber, unsigned programMapPID)
    : mParser(parser),
      mProgramNumber(programNumber),
      mProgramMapPID(programMapPID),
      mFirstPTSValid(false),
      mFirstPTS(0) {
    LOGV("new program number %u", programNumber);
#ifndef ANDROID_DEFAULT_CODE
    mFirstTimestamp = 0;
    mPendingTimestamp.us = -1;
    mPendingTimestamp.pendingtimes = 0;
#endif

/*
#ifndef ANDROID_DEFAULT_CODE
    mPMTBuffer = new ABuffer(2 * 184);
    mPMTBuffer->setRange(0, 0);
#endif
*/
}

bool ATSParser::Program::parsePID(
        unsigned pid, unsigned payload_unit_start_indicator,
        ABitReader *br, status_t *err) {
    *err = OK;

    if (pid == mProgramMapPID) {

        if (payload_unit_start_indicator) {

            unsigned skip = br->getBits(8);
            br->skipBits(skip * 8);
        }

        *err = parseProgramMap(br);

        return true;
    }

    ssize_t index = mStreams.indexOfKey(pid);
    if (index < 0) {
        return false;
    }

    *err = mStreams.editValueAt(index)->parse(
            payload_unit_start_indicator, br);

    return true;
}

void ATSParser::Program::signalDiscontinuity(
        DiscontinuityType type, const sp<AMessage> &extra) {
#ifndef ANDROID_DEFAULT_CODE

    if (extra != NULL) {
        int64_t timeUs;
        if (extra->findInt64("FirstTimeUs", &timeUs)) {

            CHECK(mPendingTimestamp.pendingtimes == 0);
            for (size_t i = 0; i < mStreams.size(); ++i) {
                if (!mStreams.editValueAt(i)->BufferIsEmpty()) {
                    mPendingTimestamp.pendingtimes ++;
                }
            }

            if (mPendingTimestamp.pendingtimes > 0) {
                mPendingTimestamp.us = timeUs;
            } else {
                LOGD("mFirstTimestamp = %lld", timeUs);
                mFirstTimestamp = timeUs;
                mFirstPTSValid = false;
            }
        }
    }
#endif
    for (size_t i = 0; i < mStreams.size(); ++i) {
        mStreams.editValueAt(i)->signalDiscontinuity(type, extra);
    }
}

void ATSParser::Program::signalEOS(status_t finalResult) {
    for (size_t i = 0; i < mStreams.size(); ++i) {
        mStreams.editValueAt(i)->signalEOS(finalResult);
    }
}

struct StreamInfo {
    unsigned mType;
    unsigned mPID;
};

status_t ATSParser::Program::parseProgramMap(ABitReader *br) {
    unsigned table_id = br->getBits(8);
    LOGV("  table_id = %u", table_id);
    CHECK_EQ(table_id, 0x02u);

    unsigned section_syntax_indicator = br->getBits(1);
    LOGV("  section_syntax_indicator = %u", section_syntax_indicator);
    CHECK_EQ(section_syntax_indicator, 1u);

    CHECK_EQ(br->getBits(1), 0u);
    MY_LOGV("  reserved = %u", br->getBits(2));

    unsigned section_length = br->getBits(12);
    LOGV("  section_length = %u", section_length);
    CHECK_EQ(section_length & 0xc00, 0u);
    CHECK_LE(section_length, 1021u);

    MY_LOGV("  program_number = %u", br->getBits(16));
    MY_LOGV("  reserved = %u", br->getBits(2));
    MY_LOGV("  version_number = %u", br->getBits(5));
    MY_LOGV("  current_next_indicator = %u", br->getBits(1));
    MY_LOGV("  section_number = %u", br->getBits(8));
    MY_LOGV("  last_section_number = %u", br->getBits(8));
    MY_LOGV("  reserved = %u", br->getBits(3));
    MY_LOGV("  PCR_PID = 0x%04x", br->getBits(13));
    MY_LOGV("  reserved = %u", br->getBits(4));

    unsigned program_info_length = br->getBits(12);
    LOGV("  program_info_length = %u", program_info_length);
    CHECK_EQ(program_info_length & 0xc00, 0u);
   
 #if 0
            if(program_info_length*8>br->numBitsLeft())
            {
                LOGE("[TS_ERROR:func=%s, line=%d]: program_info_length=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, program_info_length,br->numBitsLeft());
                  br->skipBits(br->numBitsLeft());
                 return OK;
            }
 #else    
              br->skipBits(program_info_length * 8);  // skip descriptors
#endif
   

    Vector<StreamInfo> infos;

    // infoBytesRemaining is the number of bytes that make up the
    // variable length section of ES_infos. It does not include the
    // final CRC.
    size_t infoBytesRemaining = section_length - 9 - program_info_length - 4;

    while (infoBytesRemaining > 0) {
        CHECK_GE(infoBytesRemaining, 5u);//[qian] one group is 5U

        unsigned streamType = br->getBits(8);
        LOGV("    stream_type = 0x%02x", streamType);

        MY_LOGV("    reserved = %u", br->getBits(3));

        unsigned elementaryPID = br->getBits(13);
        LOGV("    elementary_PID = 0x%04x", elementaryPID);

        MY_LOGV("    reserved = %u", br->getBits(4));

        unsigned ES_info_length = br->getBits(12);
        LOGV("    ES_info_length = %u", ES_info_length);
        CHECK_EQ(ES_info_length & 0xc00, 0u);
 #if 0
        if((infoBytesRemaining - 5)  < ES_info_length)
        {
                LOGE("[TS_ERROR:func=%s, line=%d]: infoBytesRemaining =%d   ES_info_lengtht %d,section_length=%d,program_info_length=%d,ES_info_length=%d",__FUNCTION__, __LINE__, infoBytesRemaining,ES_info_length,section_length,program_info_length,ES_info_length);
                 br->skipBits(br->numBitsLeft());
                return OK;
        }
 #else
        CHECK_GE(infoBytesRemaining - 5, ES_info_length);
 #endif

#if 0
        br->skipBits(ES_info_length * 8);  // skip descriptors
#else
        unsigned info_bytes_remaining = ES_info_length;
        while (info_bytes_remaining >= 2) {
            MY_LOGV("      tag = 0x%02x", br->getBits(8));

            unsigned descLength = br->getBits(8);
            LOGV("      len = %u", descLength);

            CHECK_GE(info_bytes_remaining, 2 + descLength);
            
 #if 0
            if(descLength >(ES_info_length-2))
            {
                LOGE("[TS_ERROR:func=%s, line=%d]: descLength=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, descLength,br->numBitsLeft());
                 br->skipBits((ES_info_length-2)*8);
                 info_bytes_remaining=0;
                 break ;
                
            }
 #else   

             br->skipBits(descLength * 8);
#endif
            

            info_bytes_remaining -= descLength + 2;
        }
        CHECK_EQ(info_bytes_remaining, 0u);
#endif

        StreamInfo info;
        info.mType = streamType;
        info.mPID = elementaryPID;
        infos.push(info);

        infoBytesRemaining -= 5 + ES_info_length;
    }

    CHECK_EQ(infoBytesRemaining, 0u);
    MY_LOGV("  CRC = 0x%08x", br->getBits(32));

    bool PIDsChanged = false;
    for (size_t i = 0; i < infos.size(); ++i) {
        StreamInfo &info = infos.editItemAt(i);

        ssize_t index = mStreams.indexOfKey(info.mPID);

        if (index >= 0 && mStreams.editValueAt(index)->type() != info.mType) {
            LOGI("uh oh. stream PIDs have changed.");
            PIDsChanged = true;
            break;
        }
    }

    if (PIDsChanged) {
#if 0
        LOGI("before:");
        for (size_t i = 0; i < mStreams.size(); ++i) {
            sp<Stream> stream = mStreams.editValueAt(i);

            LOGI("PID 0x%08x => type 0x%02x", stream->pid(), stream->type());
        }

        LOGI("after:");
        for (size_t i = 0; i < infos.size(); ++i) {
            StreamInfo &info = infos.editItemAt(i);

            LOGI("PID 0x%08x => type 0x%02x", info.mPID, info.mType);
        }
#endif

        // The only case we can recover from is if we have two streams
        // and they switched PIDs.

        bool success = false;

        if (mStreams.size() == 2 && infos.size() == 2) {
            const StreamInfo &info1 = infos.itemAt(0);
            const StreamInfo &info2 = infos.itemAt(1);

            sp<Stream> s1 = mStreams.editValueAt(0);
            sp<Stream> s2 = mStreams.editValueAt(1);

            bool caseA =
                info1.mPID == s1->pid() && info1.mType == s2->type()
                    && info2.mPID == s2->pid() && info2.mType == s1->type();

            bool caseB =
                info1.mPID == s2->pid() && info1.mType == s1->type()
                    && info2.mPID == s1->pid() && info2.mType == s2->type();

            if (caseA || caseB) {
                unsigned pid1 = s1->pid();
                unsigned pid2 = s2->pid();
                s1->setPID(pid2);
                s2->setPID(pid1);

                mStreams.clear();
                mStreams.add(s1->pid(), s1);
                mStreams.add(s2->pid(), s2);

                success = true;
            }
        }

        if (!success) {
            LOGI("Stream PIDs changed and we cannot recover.");
            return ERROR_MALFORMED;
        }
    }

    for (size_t i = 0; i < infos.size(); ++i) {
        StreamInfo &info = infos.editItemAt(i);

        ssize_t index = mStreams.indexOfKey(info.mPID);

        if (index < 0) {
            sp<Stream> stream = new Stream(this, info.mPID, info.mType);
            mStreams.add(info.mPID, stream);
			LOGD("mStreams:StreamP=%p,mPID=0x%x,mType=0x%x,size=%d",this,info.mPID,info.mType,mStreams.size());
        }
    }

    return OK;
}

sp<MediaSource> ATSParser::Program::getSource(SourceType type) {
    size_t index = (type == AUDIO) ? 0 : 0;

    for (size_t i = 0; i < mStreams.size(); ++i) {
        sp<MediaSource> source = mStreams.editValueAt(i)->getSource(type);
        if (source != NULL) {
            if (index == 0) {
                return source;
            }
            --index;
        }
    }

    return NULL;
}

#ifndef ANDROID_DEFAULT_CODE
int64_t ATSParser::Program::getPTS() {

	int64_t maxPTS=0;
    for (size_t i = 0; i < mStreams.size(); ++i) {
    	int64_t pts = mStreams.editValueAt(i)->getPTS();
        if (maxPTS <pts) {
        	maxPTS=pts;
        }
    }

    return maxPTS;
}

bool ATSParser::Program::getDequeueState() {
	return  mParser->getDequeueState();
}

bool ATSParser::Program::firstPTSIsValid() {
	return mFirstPTSValid;
}

void ATSParser::Program::notifyFlush() {
    if (mPendingTimestamp.us != -1) {
        CHECK(mPendingTimestamp.pendingtimes > 0);
        mPendingTimestamp.pendingtimes --;
        //
        //if current buffer of every Stream is flushed
        //update PendingTimestamp to mFirstTimeStamp
        if (mPendingTimestamp.pendingtimes == 0) {
            mFirstTimestamp = mPendingTimestamp.us;
            mPendingTimestamp.us = -1;
            mFirstPTSValid = false;
            LOGD("reset firstPTS, firstTimestamp = %lld", mFirstTimestamp);
        } else {
            LOGD("wait another stream to flushed");
        } 
    }
}
#endif

int64_t ATSParser::Program::convertPTSToTimestamp(uint64_t PTS) {
    if (!(mParser->mFlags & TS_TIMESTAMPS_ARE_ABSOLUTE)) {

       if (!mFirstPTSValid) {
            mFirstPTS = PTS;
            mFirstPTSValid = true;

            LOGE("convertPTSToTimestamp: init mFirstPTS=%lld",mFirstPTS);
            PTS = 0;
       } 
#ifndef ANDROID_DEFAULT_CODE 
       else if (PTS < mFirstPTS) 
       {    

           int64_t timeUs = mFirstTimestamp - (mFirstPTS - PTS) * 100 / 9;

           if (timeUs < 0) {
               LOGE("convertPTSToTimestamp: current PTS(%lld) is smaller than firstPTS (%lld)",PTS, mFirstPTS);
               return -1;
           } else {
               return timeUs;
           }
           
       }		
#else
       else if (PTS < mFirstPTS) 
       {
           PTS=0;
       }
#endif
       else 
       {
           PTS -= mFirstPTS;
       }
    }

#ifndef ANDROID_DEFAULT_CODE
    return (PTS * 100) / 9 + mFirstTimestamp;
#else
 
    return (PTS * 100) / 9;
#endif
}

////////////////////////////////////////////////////////////////////////////////

ATSParser::Stream::Stream(
        Program *program, unsigned elementaryPID, unsigned streamType)
    : mProgram(program),
      mElementaryPID(elementaryPID),
      mStreamType(streamType),
      mPayloadStarted(false),
#ifndef ANDROID_DEFAULT_CODE
      seeking(false),
      mMaxTimeUs(0),
      mSupportedStream(true),
//      mFirstPTS(0xFFFFFFFF),
   //   mFirstPTSValid(false),
#endif
      mQueue(NULL) {
    switch (mStreamType) {
        case STREAMTYPE_H264:
            mQueue = new ElementaryStreamQueue(ElementaryStreamQueue::H264);
            break;
        case STREAMTYPE_MPEG2_AUDIO_ADTS:
            mQueue = new ElementaryStreamQueue(ElementaryStreamQueue::AAC);
            break;
        case STREAMTYPE_MPEG1_AUDIO:
        case STREAMTYPE_MPEG2_AUDIO:
            mQueue = new ElementaryStreamQueue(
                    ElementaryStreamQueue::MPEG_AUDIO);
            break;

        case STREAMTYPE_MPEG1_VIDEO:
        case STREAMTYPE_MPEG2_VIDEO:
            mQueue = new ElementaryStreamQueue(
                    ElementaryStreamQueue::MPEG_VIDEO);
            break;

        case STREAMTYPE_MPEG4_VIDEO:
            mQueue = new ElementaryStreamQueue(
                    ElementaryStreamQueue::MPEG4_VIDEO);
            break;

        default:
            break;
    }

    LOGD("new stream PID 0x%02x, type 0x%02x", elementaryPID, streamType);

    if (mQueue != NULL) {
        mBuffer = new ABuffer(192 * 1024);
        mBuffer->setRange(0, 0);
    }
}

ATSParser::Stream::~Stream() {
    delete mQueue;
    mQueue = NULL;
}

status_t ATSParser::Stream::parse(
        unsigned payload_unit_start_indicator, ABitReader *br) {
#ifndef ANDROID_DEFAULT_CODE
        if(mSource.get()!=NULL  && mSource->isEOS() )
        {  
                 return OK;
        }
#endif
    if (mQueue == NULL) {
        return OK;
    }

    if (payload_unit_start_indicator) {
		//[qian]a frame will end untill meet the next payload_unit_start_indicator
        if (mPayloadStarted) {
            // Otherwise we run the danger of receiving the trailing bytes
            // of a PES packet that we never saw the start of and assuming
            // we have a a complete PES packet.

            status_t err = flush();
#ifdef ANDROID_DEFAULT_CODE
            if (err != OK) {
                return err;
            }
#endif
        }

        mPayloadStarted = true;//[qian] ,has handle the last complete PES packet
    }

    if (!mPayloadStarted) {
        return OK;
    }

    size_t payloadSizeBits = br->numBitsLeft();
    CHECK_EQ(payloadSizeBits % 8, 0u);

    size_t neededSize = mBuffer->size() + payloadSizeBits / 8;
    if (mBuffer->capacity() < neededSize) {
        // Increment in multiples of 64K.
        neededSize = (neededSize + 65535) & ~65535;

        LOGI("resizing buffer to %d bytes", neededSize);

        sp<ABuffer> newBuffer = new ABuffer(neededSize);
        memcpy(newBuffer->data(), mBuffer->data(), mBuffer->size());
        newBuffer->setRange(0, mBuffer->size());
        mBuffer = newBuffer;
    }

    memcpy(mBuffer->data() + mBuffer->size(), br->data(), payloadSizeBits / 8);
    mBuffer->setRange(0, mBuffer->size() + payloadSizeBits / 8);

    return OK;
}

bool ATSParser::Stream::isVideo() const {
    switch (mStreamType) {
        case STREAMTYPE_H264:
        case STREAMTYPE_MPEG1_VIDEO:
        case STREAMTYPE_MPEG2_VIDEO:
        case STREAMTYPE_MPEG4_VIDEO:
            return true;

        default:
            return false;
    }
}

bool ATSParser::Stream::isAudio() const {
    switch (mStreamType) {
        case STREAMTYPE_MPEG1_AUDIO:
        case STREAMTYPE_MPEG2_AUDIO:
        case STREAMTYPE_MPEG2_AUDIO_ADTS:
            return true;

        default:
            return false;
    }
}
void ATSParser::Stream::signalDiscontinuity(
        DiscontinuityType type, const sp<AMessage> &extra) {
#ifndef ANDROID_DEFAULT_CODE

    if (type == DISCONTINUITY_NONE) {
        return;
    }

    if (mProgram->mParser->mFlags & TS_SOURCE_IS_LOCAL) {
        return signalDiscontinuity_local(type);
    }

#endif

    LOGD("handle %s discontinuity: 0x%08x", isAudio()?"audio":"video", (int)type);
    if (mQueue == NULL) {
        return;
    }

    mPayloadStarted = false;
    mBuffer->setRange(0, 0);
    bool clearFormat = false;
    if (isAudio()) {
        if (type & DISCONTINUITY_AUDIO_FORMAT) {
            clearFormat = true;
        }
    } else {
        if (type & DISCONTINUITY_VIDEO_FORMAT) {
            clearFormat = true;
        }
    }

    mQueue->clear(clearFormat);

    if (type & DISCONTINUITY_TIME) {
            uint64_t resumeAtPTS;
            if (extra != NULL
                    && extra->findInt64(
                        IStreamListener::kKeyResumeAtPTS,
                        (int64_t *)&resumeAtPTS)) {
    			int64_t resumeAtMediaTimeUs= resumeAtPTS;
                extra->setInt64("resume-at-mediatimeUs", resumeAtMediaTimeUs);
            }
    }


    if (mSource != NULL) {
        mSource->queueDiscontinuity(type, extra);
    }
}

#ifndef ANDROID_DEFAULT_CODE
void ATSParser::Stream::signalDiscontinuity_local(DiscontinuityType type) {
    if (mQueue == NULL) {
        return;
    }

    mPayloadStarted = false;
    mBuffer->setRange(0, 0);

	if (!mProgram->getDequeueState()) {
		if (type & DISCONTINUITY_SEEK) {
			mMaxTimeUs = 0;
		}
		return;
	}
    bool clearFormat = false;
    if (isAudio()) {
        if (type & DISCONTINUITY_AUDIO_FORMAT) {
            clearFormat = true;
        }
    } else {
        if (type & DISCONTINUITY_VIDEO_FORMAT) {
            clearFormat = true;
        }
    }

    mQueue->clear(clearFormat);

    if (type & DISCONTINUITY_SEEK) {
    	mQueue->setSeeking();
		if(mSource.get())       //TODO: clear the data can implemented in mSource
		{
			mSource->clear();
            LOGD("source cleared, %d", mSource == NULL);
		}
		else
		{
			LOGE("[error]this stream has not source\n");
		}
	}

}

#endif

void ATSParser::Stream::signalEOS(status_t finalResult) {
    if (mSource != NULL) {
        mSource->signalEOS(finalResult);
    }
}

status_t ATSParser::Stream::parsePES(ABitReader *br) {
    unsigned packet_startcode_prefix = br->getBits(24);

    LOGV("packet_startcode_prefix = 0x%08x", packet_startcode_prefix);

    if (packet_startcode_prefix != 1) {
        LOGV("Supposedly payload_unit_start=1 unit does not start "
             "with startcode.");

        return ERROR_MALFORMED;
    }

    CHECK_EQ(packet_startcode_prefix, 0x000001u);

    unsigned stream_id = br->getBits(8);
    LOGV("stream_id = 0x%02x", stream_id);

    unsigned PES_packet_length = br->getBits(16);
    LOGV("PES_packet_length = %u", PES_packet_length);

    if (stream_id != 0xbc  // program_stream_map
            && stream_id != 0xbe  // padding_stream
            && stream_id != 0xbf  // private_stream_2
            && stream_id != 0xf0  // ECM
            && stream_id != 0xf1  // EMM
            && stream_id != 0xff  // program_stream_directory
            && stream_id != 0xf2  // DSMCC
            && stream_id != 0xf8) {  // H.222.1 type E
        CHECK_EQ(br->getBits(2), 2u);

        MY_LOGV("PES_scrambling_control = %u", br->getBits(2));
        MY_LOGV("PES_priority = %u", br->getBits(1));
        MY_LOGV("data_alignment_indicator = %u", br->getBits(1));
        MY_LOGV("copyright = %u", br->getBits(1));
        MY_LOGV("original_or_copy = %u", br->getBits(1));

        unsigned PTS_DTS_flags = br->getBits(2);
        LOGV("PTS_DTS_flags = %u", PTS_DTS_flags);

        unsigned ESCR_flag = br->getBits(1);
        LOGV("ESCR_flag = %u", ESCR_flag);

        unsigned ES_rate_flag = br->getBits(1);
        LOGV("ES_rate_flag = %u", ES_rate_flag);

        unsigned DSM_trick_mode_flag = br->getBits(1);
        LOGV("DSM_trick_mode_flag = %u", DSM_trick_mode_flag);

        unsigned additional_copy_info_flag = br->getBits(1);
        LOGV("additional_copy_info_flag = %u", additional_copy_info_flag);

        MY_LOGV("PES_CRC_flag = %u", br->getBits(1));
        MY_LOGV("PES_extension_flag = %u", br->getBits(1));

        unsigned PES_header_data_length = br->getBits(8);
        LOGV("PES_header_data_length = %u", PES_header_data_length);

        unsigned optional_bytes_remaining = PES_header_data_length;

        uint64_t PTS = 0, DTS = 0;

        if (PTS_DTS_flags == 2 || PTS_DTS_flags == 3) {
            CHECK_GE(optional_bytes_remaining, 5u);

            CHECK_EQ(br->getBits(4), PTS_DTS_flags);

            PTS = ((uint64_t)br->getBits(3)) << 30;
            CHECK_EQ(br->getBits(1), 1u);
            PTS |= ((uint64_t)br->getBits(15)) << 15;
            CHECK_EQ(br->getBits(1), 1u);
            PTS |= br->getBits(15);
            CHECK_EQ(br->getBits(1), 1u);

            LOGV("PTS = %llu", PTS);
            // LOGI("PTS = %.2f secs", PTS / 90000.0f);

            optional_bytes_remaining -= 5;

            if (PTS_DTS_flags == 3) {
                CHECK_GE(optional_bytes_remaining, 5u);

                CHECK_EQ(br->getBits(4), 1u);

                DTS = ((uint64_t)br->getBits(3)) << 30;
                CHECK_EQ(br->getBits(1), 1u);
                DTS |= ((uint64_t)br->getBits(15)) << 15;
                CHECK_EQ(br->getBits(1), 1u);
                DTS |= br->getBits(15);
                CHECK_EQ(br->getBits(1), 1u);

                LOGV("DTS = %llu", DTS);

                optional_bytes_remaining -= 5;
            }
        }

        if (ESCR_flag) {
            CHECK_GE(optional_bytes_remaining, 6u);

            br->getBits(2);

            uint64_t ESCR = ((uint64_t)br->getBits(3)) << 30;
            CHECK_EQ(br->getBits(1), 1u);
            ESCR |= ((uint64_t)br->getBits(15)) << 15;
            CHECK_EQ(br->getBits(1), 1u);
            ESCR |= br->getBits(15);
            CHECK_EQ(br->getBits(1), 1u);

            LOGV("ESCR = %llu", ESCR);
            MY_LOGV("ESCR_extension = %u", br->getBits(9));

            CHECK_EQ(br->getBits(1), 1u);

            optional_bytes_remaining -= 6;
        }

        if (ES_rate_flag) {
            CHECK_GE(optional_bytes_remaining, 3u);

            CHECK_EQ(br->getBits(1), 1u);
            MY_LOGV("ES_rate = %u", br->getBits(22));
            CHECK_EQ(br->getBits(1), 1u);

            optional_bytes_remaining -= 3;
        }
		//[qian]skip the remaining optional PES packert header and stuffing bytes
 #if 0
            if(optional_bytes_remaining *8 >br->numBitsLeft())
            {
                LOGE("[TS_ERROR:func=%s, line=%d]: optional_bytes_remaining=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, optional_bytes_remaining,br->numBitsLeft());
                 br->skipBits(br->numBitsLeft());
                 return OK;
            }
 #else   
             br->skipBits(optional_bytes_remaining * 8);
#endif 		
        

        // ES data follows.
		//[qian] if the PES_packet_length=0 (only in Video). mean the PES packet
		//length not sure. untill meet the next PES packet header
        if (PES_packet_length != 0) {
#ifdef ANDROID_DEFAULT_CODE
            CHECK_GE(PES_packet_length, PES_header_data_length + 3);
#endif

            unsigned dataLength =
                PES_packet_length - 3 - PES_header_data_length;

            if (br->numBitsLeft() < dataLength * 8) {
                LOGE("[TS_ERROR]PES packet does not carry enough data to contain "
                     "payload. (numBitsLeft = %d, required = %d)",
                     br->numBitsLeft(), dataLength * 8);

                return ERROR_MALFORMED;
            }
            
 #if 0
            if(dataLength *8 >br->numBitsLeft())
            {
                 LOGE("[TS_ERROR:func=%s, line=%d]: dataLength=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, dataLength,br->numBitsLeft());
                 dataLength = br->numBitsLeft()/8;
            }
#else  
            CHECK_GE(br->numBitsLeft(), dataLength * 8);
#endif
            onPayloadData(PTS_DTS_flags, PTS, DTS, br->data(), dataLength);
 
             br->skipBits(dataLength * 8);
             
            
        } else {
            onPayloadData(
                    PTS_DTS_flags, PTS, DTS,
                    br->data(), br->numBitsLeft() / 8);

            size_t payloadSizeBits = br->numBitsLeft();
            CHECK_EQ(payloadSizeBits % 8, 0u);

            LOGV("There's %d bytes of payload.", payloadSizeBits / 8);
        }
    } else if (stream_id == 0xbe) {  // padding_stream
        CHECK_NE(PES_packet_length, 0u);
 #if 0
            if(PES_packet_length*8 >br->numBitsLeft())
            {
                LOGE("[TS_ERROR:func=%s, line=%d]: PES_packet_length=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, PES_packet_length,br->numBitsLeft());
                 br->skipBits(br->numBitsLeft());
                return OK;
            }
 #else   
        br->skipBits(PES_packet_length * 8);
#endif   
    } else {
        CHECK_NE(PES_packet_length, 0u);
 #if 0
            if(PES_packet_length*8 >br->numBitsLeft())
            {
                LOGE("[TS_ERROR:func=%s, line=%d]: PES_packet_length=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, PES_packet_length,br->numBitsLeft());
                 br->skipBits(br->numBitsLeft());
                return OK;
            }
 #else   
        br->skipBits(PES_packet_length * 8);
#endif       
    }

    return OK;
}

status_t ATSParser::Stream::flush() {
    if (mBuffer->size() == 0) {
        return OK;
    }

    LOGV("flushing stream 0x%04x size = %d", mElementaryPID, mBuffer->size());

    ABitReader br(mBuffer->data(), mBuffer->size());

    status_t err = parsePES(&br);
#ifndef ANDROID_DEFAULT_CODE
    mProgram->notifyFlush();

#endif
    mBuffer->setRange(0, 0);

    return err;
}

void ATSParser::Stream::onPayloadData(
        unsigned PTS_DTS_flags, uint64_t PTS, uint64_t DTS,
        const uint8_t *data, size_t size) {
    

    int64_t timeUs = 0ll;  // no presentation timestamp available.
    if (PTS_DTS_flags == 2 || PTS_DTS_flags == 3) {
        timeUs = mProgram->convertPTSToTimestamp(PTS);
    }	
//    LOGD("@debug: %s timeUs = %lld (%lld)", isVideo()?"video":"audio", timeUs, PTS);
#ifndef ANDROID_DEFAULT_CODE
	//if (timeUs > mMaxTimeUs && timeUs!=0xFFFFFFFF){
	if (timeUs > mMaxTimeUs ){
		mMaxTimeUs = timeUs;
	}
       if (!mProgram->getDequeueState()) {
		return;
	}
	if(timeUs==-1)
	{
		LOGE("onPayloadData:Skip a data payoad,isVideo()=%d", isVideo());
		return;
	}
	if(!mSupportedStream)
	{
		return;
	}
    
    
#endif
	
    status_t err = mQueue->appendData(data, size, timeUs);

    if (err != OK) {
		LOGE("onPayloadData error1");
        return;
    }

    sp<ABuffer> accessUnit;
    while ((accessUnit = mQueue->dequeueAccessUnit()) != NULL) {
        if (mSource == NULL) {
            sp<MetaData> meta = mQueue->getFormat();
				
            if (meta != NULL) {
                LOGD("Stream PID 0x%08x of type 0x%02x now has data.",
                     mElementaryPID, mStreamType);
#ifndef ANDROID_DEFAULT_CODE
				if(!isSupportedStream(meta))
				{
					mSupportedStream=false;
					return;
				}
#endif
 				mSource = new AnotherPacketSource(meta);
                mSource->queueAccessUnit(accessUnit);
            }
        } else if (mQueue->getFormat() != NULL) {
            // After a discontinuity we invalidate the queue's format
            // and won't enqueue any access units to the source until
            // the queue has reestablished the new format.

            if (mSource->getFormat() == NULL) {
                mSource->setFormat(mQueue->getFormat());
            }
            mSource->queueAccessUnit(accessUnit);
        }
    }
}

sp<MediaSource> ATSParser::Stream::getSource(SourceType type) {
    switch (type) {
        case VIDEO:
        {
            if (isVideo()) {
                return mSource;
            }
            break;
        }

        case AUDIO:
        {
            if (isAudio()) {
                return mSource;
            }
            break;
        }

        default:
            break;
    }

    return NULL;
}

#ifndef ANDROID_DEFAULT_CODE
int64_t ATSParser::Stream::getPTS() 
{
	return mMaxTimeUs;

}
/*
bool  ATSParser::Stream::getFirstPTS(int64_t *PTS) 
{
	*PTS = mFirstPTS;
	return mFirstPTSValid;

}
*/

bool  ATSParser::Stream::isSupportedStream( sp<MetaData> StreamMeta)
{
	char value[PROPERTY_VALUE_MAX];
       int  _res =0;
	bool ignoreaudio =0;
	bool ignorevideo =0;
	   
	property_get("ts.ignoreaudio", value, "0");
	  _res = atoi(value);
	if (_res) ignoreaudio =1;

	property_get("ts.ignorevideo", value, "0");
	  _res = atoi(value);
	if (_res) ignorevideo = 1;



	if(isVideo())
	{
		int32_t width,height,MaxWidth,MaxHeight;
#ifdef MTK_DEMUXER_BLOCK_CAPABILITY		
#ifdef MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
		
		VDEC_DRV_QUERY_VIDEO_FORMAT_T qinfo;
		VDEC_DRV_QUERY_VIDEO_FORMAT_T outinfo;
		memset(&qinfo,0,sizeof(VDEC_DRV_QUERY_VIDEO_FORMAT_T));
		memset(&outinfo,0,sizeof(VDEC_DRV_QUERY_VIDEO_FORMAT_T));
		
		switch (mStreamType) {
	        case STREAMTYPE_H264:
			{
				qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_H264;
				break;
			}
	        case STREAMTYPE_MPEG1_VIDEO:
			{
				qinfo.u4VideoFormat =	VDEC_DRV_VIDEO_FORMAT_MPEG1;
				break;
			}
	        case STREAMTYPE_MPEG2_VIDEO:
			{
				qinfo.u4VideoFormat =  VDEC_DRV_VIDEO_FORMAT_MPEG2;
				break;
			}
	        case STREAMTYPE_MPEG4_VIDEO:
			{
				qinfo.u4VideoFormat = VDEC_DRV_VIDEO_FORMAT_MPEG4;
	            break;
			}
	        default:
			{
				LOGE("[TS capability error]Unsupport video format!!!mStreamType=0x%x ", mStreamType);
				return false;
	        }
	    }
		
		VDEC_DRV_MRESULT_T ret;		
		ret = eVDecDrvQueryCapability(VDEC_DRV_QUERY_TYPE_VIDEO_FORMAT, &qinfo, &outinfo);

//resolution			
		MaxWidth= outinfo.u4Width;
		MaxHeight = outinfo.u4Height;
		StreamMeta->findInt32(kKeyWidth, &width);
		StreamMeta->findInt32(kKeyHeight, &height);
		LOGE("[TS DRV capability info] ret =%d ,MaxWidth=%d, MaxHeight=%d ,profile=%d,level=%d",  ret,MaxWidth , MaxHeight,outinfo.u4Profile,outinfo.u4Level);
		
		if((ret == VDEC_DRV_MRESULT_OK )&&(width > MaxWidth || height > MaxHeight ||width<32 || height <32 ))
		{
			LOGE("[TS capability error]Unsupport video resolution!!! width %d> MaxWidth %d || height %d > MaxHeight %d ", width ,MaxWidth ,height,MaxHeight);
			return false;
		}
#else //MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
		if(StreamMeta!=NULL)
		{
			StreamMeta->findInt32(kKeyWidth, &width);
			StreamMeta->findInt32(kKeyHeight, &height);
		}
		else
		{
			LOGE("[TS capability error]No mFormat" );
			return false;
		}
		if ((width > 1280) || (height > 720) ||
				((width*height) > (1280*720)) || (width <= 0) || (height <= 0))
		{
			LOGE("[TS capability error]Unsupport video resolution!!!width %d> 1280 || height %d > 720 ", width ,height );
			return false;
		}

//profile and level
		if(mStreamType == STREAMTYPE_H264)
		{
			bool err=false;
			uint32_t type;
		        const void *data;
		        size_t size;
		       unsigned profile, level;
			if(StreamMeta->findData(kKeyAVCC, &type, &data, &size))
			{
			    const uint8_t *ptr = (const uint8_t *)data;

			    // verify minimum size and configurationVersion == 1.
			    if (size < 7 || ptr[0] != 1) 
			   {
			        return false;
			    }

			    profile = ptr[1];
			    level = ptr[3];
				
				if(level>31)
				{
                    //workaround: let youku can play http live streaming
                    if ((mProgram->mParser)->mFlags & TS_SOURCE_IS_LOCAL) {
    					LOGE("[TS capability error]Unsupport H264 leve!!!level=%d  >31", level);
	    				return false;
                    }
				}
			}
			else
			{
				LOGE("[TS_ERROR]:can not find the kKeyAVCC");
				return false;
			}
		}
#endif ////MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
#endif//#ifdef MTK_DEMUXER_BLOCK_CAPABILITY
 
		if(ignorevideo)
		{
			LOGE("[TS_ERROR]:we ignorevideo");
			return false;
		}
			
    }
   else if(isAudio())
   {
#ifdef MTK_DEMUXER_BLOCK_CAPABILITY   
#ifdef MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
	
#else
	 if(mStreamType == STREAMTYPE_MPEG2_AUDIO_ADTS)
	 {
		bool err=false;
		uint32_t type;
	        const void *data;
	        size_t size;
	        uint8_t  audio_config[2],object_type;
		if(StreamMeta->findData(kKeyESDS, &type, &data, &size))
	       {
			audio_config[0]=*((uint8_t*)(data+size-2));
			audio_config[1]=*((uint8_t*)(data+size-1));
			object_type = ((audio_config[0] >> 3) & 7);
			  // only support LC,LTP,SBR audio object
			  if   ((object_type != AAC_AAC_LC) &&
			        (object_type != AAC_AAC_LTP) &&
			        (object_type != AAC_SBR) &&
			         (object_type != 29) )
		      {        
		           LOGE("[TS capability error]Unsupport AAC  profile!!!object_type =  %d  audio_config=0x%x,0x%x", object_type,audio_config[0],audio_config[1]);
			    return false;
		      }
			
		}
		else
		{
			LOGE("[TS_ERROR]:can not find  the kKeyESDS");
		       return false;
		}

	 }
#endif // MTK_DEMUXER_QUERY_CAPABILITY_FROM_DRV_SUPPORT
#endif//#ifdef MTK_DEMUXER_BLOCK_CAPABILITY		
	if(ignoreaudio)
	{
		LOGE("[TS_ERROR]:we ignoreaudio");
		return false;
	}		

   }

	return true;
}
#endif


////////////////////////////////////////////////////////////////////////////////

ATSParser::ATSParser(uint32_t flags)
    : mFlags(flags)
#ifndef ANDROID_DEFAULT_CODE
,mNeedDequeuePES(true)
#endif
{
	LOGD("mFlags=0x%x",mFlags);
}

ATSParser::~ATSParser() {
}

status_t ATSParser::feedTSPacket(const void *data, size_t size) {
    CHECK_EQ(size, kTSPacketSize);

    ABitReader br((const uint8_t *)data, kTSPacketSize);
    return parseTS(&br);
}

void ATSParser::signalDiscontinuity(
        DiscontinuityType type, const sp<AMessage> &extra) {
    for (size_t i = 0; i < mPrograms.size(); ++i) {
        mPrograms.editItemAt(i)->signalDiscontinuity(type, extra);
    }
}

void ATSParser::signalEOS(status_t finalResult) {
    CHECK_NE(finalResult, (status_t)OK);

    for (size_t i = 0; i < mPrograms.size(); ++i) {
        mPrograms.editItemAt(i)->signalEOS(finalResult);
    }
}

void ATSParser::parseProgramAssociationTable(ABitReader *br) {
    unsigned table_id = br->getBits(8);
    LOGV("  table_id = %u", table_id);
    CHECK_EQ(table_id, 0x00u);

    unsigned section_syntax_indictor = br->getBits(1);
    LOGV("  section_syntax_indictor = %u", section_syntax_indictor);
    CHECK_EQ(section_syntax_indictor, 1u);

    CHECK_EQ(br->getBits(1), 0u);
    MY_LOGV("  reserved = %u", br->getBits(2));

    unsigned section_length = br->getBits(12);
    LOGV("  section_length = %u", section_length);
    CHECK_EQ(section_length & 0xc00, 0u);//[qian] high 4 bit not use, should be 0

    MY_LOGV("  transport_stream_id = %u", br->getBits(16));
    MY_LOGV("  reserved = %u", br->getBits(2));
    MY_LOGV("  version_number = %u", br->getBits(5));
    MY_LOGV("  current_next_indicator = %u", br->getBits(1));
    MY_LOGV("  section_number = %u", br->getBits(8));
    MY_LOGV("  last_section_number = %u", br->getBits(8));

    size_t numProgramBytes = (section_length - 5 /* header */ - 4 /* crc */);
    CHECK_EQ((numProgramBytes % 4), 0u);
	//[qian]{(program_number,program_map_PID),...}
    for (size_t i = 0; i < numProgramBytes / 4; ++i) {
        unsigned program_number = br->getBits(16);
        LOGV("    program_number = %u", program_number);

        MY_LOGV("    reserved = %u", br->getBits(3));

        if (program_number == 0) {
            MY_LOGV("    network_PID = 0x%04x", br->getBits(13));
        } else {
            unsigned programMapPID = br->getBits(13);

            LOGV("    program_map_PID = 0x%04x", programMapPID);

            bool found = false;
            for (size_t index = 0; index < mPrograms.size(); ++index) {
                const sp<Program> &program = mPrograms.itemAt(index);

                if (program->number() == program_number) {
                    program->updateProgramMapPID(programMapPID);
                    found = true;
                    break;
                }
            }

            if (!found) {
                mPrograms.push(
                        new Program(this, program_number, programMapPID));

				LOGD("mPrograms:ProgramP=%p,program_number=%d,programMapPID=%d,size=%d",this,program_number,programMapPID,mPrograms.size());
            }
        }
    }

    MY_LOGV("  CRC = 0x%08x", br->getBits(32));
}

status_t ATSParser::parsePID(
        ABitReader *br, unsigned PID,
        unsigned payload_unit_start_indicator) {
    if (PID == 0) {
		//[qian] has a byte to mean the offset
        if (payload_unit_start_indicator) {
            unsigned skip = br->getBits(8);//read the offset
            br->skipBits(skip * 8);//skip the offset data
        }
        parseProgramAssociationTable(br);
        return OK;
    }

    bool handled = false;
    for (size_t i = 0; i < mPrograms.size(); ++i) {
        status_t err;
        if (mPrograms.editItemAt(i)->parsePID(
                    PID, payload_unit_start_indicator, br, &err)) {
            if (err != OK) {
                return err;
            }

            handled = true;
            break;
        }
    }

    if (!handled) {
        LOGV("PID 0x%04x not handled.", PID);
    }

    return OK;
}

void ATSParser::parseAdaptationField(ABitReader *br) {
    unsigned adaptation_field_length = br->getBits(8);
 #ifndef ANDROID_DEFAULT_CODE
            if(adaptation_field_length*8>br->numBitsLeft())
            {
                LOGE("[TS_ERROR:func=%s, line=%d]: adaptation_field_length=%d >  br->numBitsLeft %d",__FUNCTION__, __LINE__, adaptation_field_length,br->numBitsLeft());
                br->skipBits(br->numBitsLeft());
                return ;
            }
 #endif
    if (adaptation_field_length > 0) {
        br->skipBits(adaptation_field_length * 8);  // XXX
    }
}

status_t ATSParser::parseTS(ABitReader *br) {
	//static uint64_t TS_count=0;
   // TS_count++;
	//LOGE("[error]parseTS:  %lld TS  ", TS_count  );
    unsigned sync_byte = br->getBits(8);
	
#ifndef ANDROID_DEFAULT_CODE	

    if(sync_byte!=0x47u)    {
		
		if(mFlags & TS_SOURCE_IS_LOCAL)
		{

			LOGE("[error]parseTS:  LOCAL ,return error as sync_byte=0x%x",sync_byte);
			return BAD_VALUE;
		}
		else
		{	
			LOGE("[error]parseTS:  Live ,skip this TS as sync_byte=0x%x",sync_byte);
			return OK;
	       }
	}
#else
    CHECK_EQ(sync_byte, 0x47u);
#endif    
    MY_LOGV("transport_error_indicator = %u", br->getBits(1));

    unsigned payload_unit_start_indicator = br->getBits(1);
    LOGV("payload_unit_start_indicator = %u", payload_unit_start_indicator);

    MY_LOGV("transport_priority = %u", br->getBits(1));

    unsigned PID = br->getBits(13);
    LOGV("PID = 0x%04x", PID);

    MY_LOGV("transport_scrambling_control = %u", br->getBits(2));

    unsigned adaptation_field_control = br->getBits(2);
    LOGV("adaptation_field_control = %u", adaptation_field_control);

    unsigned continuity_counter = br->getBits(4);
    LOGV("continuity_counter = %u", continuity_counter);

    // LOGI("PID = 0x%04x, continuity_counter = %u", PID, continuity_counter);

    if (adaptation_field_control == 2 || adaptation_field_control == 3) {
        parseAdaptationField(br);
    }

    if (adaptation_field_control == 1 || adaptation_field_control == 3) {
 #ifndef ANDROID_DEFAULT_CODE
            if( br->numBitsLeft()==0)
            {
                LOGE("[TS_ERROR:func=%s, line=%d]:   br->numBitsLeft %d",__FUNCTION__, __LINE__,  br->numBitsLeft());
                return OK;
            }
 #endif        
        return parsePID(br, PID, payload_unit_start_indicator);
    }

    return OK;
}

#ifndef ANDROID_DEFAULT_CODE
bool ATSParser::findPAT(const void *data, size_t size) {
	//LOGE("isPAT---");
	CHECK_EQ(size, kTSPacketSize);
	ABitReader br((const uint8_t *) data, kTSPacketSize);
	unsigned sync_byte = br.getBits(8);
	//LOGE("isPAT-sync_byte=0x%x ",sync_byte );
	//CHECK_EQ(sync_byte, 0x47u);
	if(sync_byte!=0x47u)
	{
		LOGE("[error]isPAT-sync_byte=0x%x ",sync_byte );
		return false;
	}
	br.getBits(1);
	unsigned payload_unit_start_indicator = br.getBits(1);
	br.getBits(1);

	unsigned PID = br.getBits(13);
	if (PID == 0)
		return true;
	else
		return false;
}

void ATSParser::setDequeueState(bool needDequeuePES) {
	mNeedDequeuePES = needDequeuePES;
}

bool ATSParser::getDequeueState() {
	return mNeedDequeuePES;
}
//get duration
int64_t ATSParser::getMaxPTS() {
	int64_t maxPTS=0;
	for (size_t i = 0; i < mPrograms.size(); ++i) {
		int64_t pts = mPrograms.editItemAt(i)->getPTS();
		if (maxPTS < pts) {
			maxPTS = pts;
		}
	}
	return maxPTS;
}

bool ATSParser::firstPTSIsValid() {
	for (size_t i = 0; i < mPrograms.size(); ++i) {
		if (mPrograms.editItemAt(i)->firstPTSIsValid()) {
			return true;
		}
	}
	return false;
}
#endif

sp<MediaSource> ATSParser::getSource(SourceType type) {
    int which = -1;  // any

    for (size_t i = 0; i < mPrograms.size(); ++i) {
        const sp<Program> &program = mPrograms.editItemAt(i);

        if (which >= 0 && (int)program->number() != which) {
            continue;
        }

        sp<MediaSource> source = program->getSource(type);

        if (source != NULL) {
            return source;
        }
    }

    return NULL;
}

bool ATSParser::PTSTimeDeltaEstablished() {
    if (mPrograms.isEmpty()) {
        return false;
    }

    return mPrograms.editItemAt(0)->PTSTimeDeltaEstablished();
}

}  // namespace android
