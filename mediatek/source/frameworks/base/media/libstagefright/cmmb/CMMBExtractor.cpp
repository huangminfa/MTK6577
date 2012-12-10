/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

//MTK_OP01_PROTECT_START
#define LOG_TAG "CMMBExtractor"
#include <utils/Log.h>

#include <arpa/inet.h>

#include <ctype.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaBufferGroup.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>
#include <utils/String8.h>
#include<media/stagefright/MediaErrors.h>

#include "CMMBExtractor.h"


namespace android {

class CMMBSource : public MediaSource {
public:
    CMMBSource(const sp<MetaData> &format,
                const sp<DataSource> &dataSource);

    virtual status_t start(MetaData *params = NULL);
    virtual status_t stop();

    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options = NULL);

protected:
    virtual ~CMMBSource();

private:
    Mutex mLock;

    sp<MetaData> mFormat;
    sp<DataSource> mDataSource;

    bool mStarted;
    bool mIsAVC;

    MediaBufferGroup *mGroup;

    MediaBuffer *mBuffer;

    uint8_t *mSrcBuffer;


    CMMBSource(const CMMBSource &);
    CMMBSource &operator=(const CMMBSource &);
};




CMMBExtractor::CMMBExtractor(const sp<DataSource> &source)
    : mDataSource(source),
      CMMBMetaData(new MetaData),
      VideoTrack(NULL),
      AudioTrack(NULL),
      mHaveMetadata(false){
      //LOGE("CMMBExtractor::CMMBExtractor");
      
}

CMMBExtractor::~CMMBExtractor() {
	//LOGE("CMMBExtractor::~CMMBExtractor in");
	if (VideoTrack)
		delete VideoTrack;
	if (AudioTrack)
		delete AudioTrack;

	VideoTrack = AudioTrack = NULL;
	//LOGE("CMMBExtractor::~CMMBExtractor out");
}


sp<MetaData> CMMBExtractor::getMetaData() {
    //LOGE("CMMBExtractor::getMetaData in");
    status_t err;
    if ((err = readMetaData()) != OK) {
        return new MetaData;
    }
    //LOGE("CMMBExtractor::getMetaData out");
    return CMMBMetaData;
}


size_t CMMBExtractor::countTracks() {

   status_t err;
   //LOGE("CMMBExtractor::countTracks in ");
   if ((err = readMetaData()) != OK) {
       return 0;
   }

   size_t n = 0;
   
   if (NULL != VideoTrack)
   	n++;
   if (NULL != AudioTrack)
   	n++;
   //LOGE("CMMBExtractor::countTracks out track count = %d", n);
   return n;
}

sp<MetaData> CMMBExtractor::getTrackMetaData(
        size_t index, uint32_t flags) {

    status_t err;
    //LOGE("CMMBExtractor::getTrackMetaData in");
    if ((err = readMetaData()) != OK) {
        return NULL;
    }

    if ((0 == index) && (NULL != VideoTrack))
		return VideoTrack->meta;

    if (( (1 == index) && (NULL != AudioTrack) )
	    || ((0 == index) && (NULL == VideoTrack)))
		return AudioTrack->meta;

    return NULL;
}

sp<MediaSource> CMMBExtractor::getTrack(size_t index) {
    status_t err;
    //LOGE("CMMBExtractor::getTrack in");
    if ((err = readMetaData()) != OK) {
        return NULL;
    }

    if ((0 == index) && (NULL != VideoTrack))
        return new CMMBSource(
                 VideoTrack->meta, mDataSource);

    if ( ((1 == index) && (NULL != AudioTrack))
	    || ((0 == index) && (NULL == VideoTrack)))
        return new CMMBSource(
                 AudioTrack->meta, mDataSource);



    return NULL;
}


status_t CMMBExtractor::readMetaData()
{
    TCmmbMetadata* metadata;
    if (mHaveMetadata) {
        return OK;
    }
    //LOGE("CMMBExtractor::readMetaData in");

    //meta data is ready.
    mHaveMetadata = true;

    metadata = (TCmmbMetadata*)(mDataSource->getMetadata());
    if (NULL == metadata){
	 LOGE("CMMBExtractor::readMetaData metadata null");
	 return INVALID_OPERATION;
    }

    
    LOGE("CMMBExtractor::readMetaData come into video tag");
    if ((NULL != metadata->VideoMetadata) && (CMMB_VIDEO_ALGORITHM_H264 == metadata->VideoMetadata->video_algorithm)){ //video
	 CMMBMetaData->setCString(kKeyMIMEType, "video/mp4");

	 VideoTrack = new Track();
	 if (VideoTrack)
	 {     
	        VideoTrack->meta = new MetaData;
	        VideoTrack->meta->setCString(kKeyMIMEType, "video/avc"); 
		 VideoTrack->meta->setInt32(kKeyIsCmmb, 1);
		 LOGE("CMMBExtractor::readMetaData have avc track, width = %d, height = %d, bitrate = %d", metadata->VideoMetadata->video_x_resolution,
		 	                                                                                                                                     metadata->VideoMetadata->video_y_resolution,
		 	                                                                                                                                     metadata->VideoMetadata->video_bitrate );
		 if ( (metadata->VideoMetadata->video_x_resolution != 0)
		 	&& (metadata->VideoMetadata->video_y_resolution != 0))
		 {
       	     VideoTrack->meta->setInt32(kKeyWidth, metadata->VideoMetadata->video_x_resolution);
       	     VideoTrack->meta->setInt32(kKeyHeight, metadata->VideoMetadata->video_y_resolution);
		 }
		 else
		 {
       	     VideoTrack->meta->setInt32(kKeyWidth, 320);
       	     VideoTrack->meta->setInt32(kKeyHeight, 240);
		 }
		 
       	 VideoTrack->meta->setInt32(kKeyBitRate, metadata->VideoMetadata->video_bitrate);
               //VideoTrack->meta->setInt32(kKeySampleRate, metadata->metadata_video->kKeySampleRate);
       	 if (NULL != metadata->VideoMetadata->h264_dec_config[0])
       	 {		
#if 0   //For read file prefix:0x00000001
                     ESDSStruct esds;
                     ESDSSuffixStruct esdssuffix;
		       uint8_t esdswrap[sizeof(ESDSStruct) + sizeof(ESDSSuffixStruct)  + MAX_H264_SPS_LEN + MAX_H264_PPS_LEN - 8];

			esds.version = 1;
			esds.profile  = *(metadata->VideoMetadata->h264_dec_config[0]->sps[0] + 4 + 1);
			esds.level    = *(metadata->VideoMetadata->h264_dec_config[0]->sps[0] + 4 + 3);
			esds.numsps =1;
			esds.spslength = (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) << 8 + 0;
			LOGE("sps length = %d,, %d", esds.spslength, (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4));

			esdssuffix.numpps = 1;
			esdssuffix.ppslength = (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0] - 4) << 8 + 0;
			LOGE("pps length = %d,, %d", esdssuffix.ppslength, (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0] - 4));

			memcpy((void*)esdswrap, (void *)&esds, sizeof(ESDSStruct));
			memcpy(esdswrap + sizeof(ESDSStruct) ,
				             (metadata->VideoMetadata->h264_dec_config[0]->sps[0] + 4),
				             (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4));

			memcpy(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4),
				           (void*)&(esdssuffix.numpps),
				           sizeof(uint8_t));
			memcpy(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + sizeof(uint8_t),
				           (void*)&(esdssuffix.ppslength),
				           sizeof(uint16_t));
			memcpy(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + sizeof(uint8_t) + sizeof(uint16_t),
				           metadata->VideoMetadata->h264_dec_config[0]->pps[0] + 4,
				           metadata->VideoMetadata->h264_dec_config[0]->pps_len[0] - 4);

       	       VideoTrack->meta->setData(
                               kKeyAVCC, kTypeAVCC, esdswrap, 
                               sizeof(ESDSStruct) + sizeof(uint8_t) + sizeof(uint16_t) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0] - 4));
			LOGE("CMMBExtractor::readMetaData pps meta data = %x, %x, %x, %x,   size = %d",
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + sizeof(ESDSSuffixStruct)),
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + sizeof(ESDSSuffixStruct) + 1),
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + sizeof(ESDSSuffixStruct) + 2),
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + sizeof(ESDSSuffixStruct) + 3),
				                                                      sizeof(ESDSStruct) + sizeof(ESDSSuffixStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0] - 4) + (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0] - 4));
			
#else
                     ESDSStruct esds;
                     ESDSSuffixStruct esdssuffix;
		       uint8_t esdswrap[sizeof(ESDSStruct) + sizeof(ESDSSuffixStruct)  + MAX_H264_SPS_LEN + MAX_H264_PPS_LEN];

			esds.version = 1;
			esds.profile  = *(metadata->VideoMetadata->h264_dec_config[0]->sps[0] + 1);
			esds.level    = *(metadata->VideoMetadata->h264_dec_config[0]->sps[0] + 3);
			esds.numsps =1;
			esds.spslength = (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) << 8 + 0;
			LOGE("sps length = %d,, %d", esds.spslength, (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]));

			esdssuffix.numpps = 1;
			esdssuffix.ppslength = (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0]) << 8 + 0;
			LOGE("pps length = %d,, %d", esdssuffix.ppslength, (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0]));

			memcpy((void*)esdswrap, (void *)&esds, sizeof(ESDSStruct));
			memcpy(esdswrap + sizeof(ESDSStruct) ,
				             (metadata->VideoMetadata->h264_dec_config[0]->sps[0]),
				             (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]));

			memcpy(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]),
				           (void*)&(esdssuffix.numpps),
				           sizeof(uint8_t));
			memcpy(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + sizeof(uint8_t),
				           (void*)&(esdssuffix.ppslength),
				           sizeof(uint16_t));
			memcpy(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + sizeof(uint8_t) + sizeof(uint16_t),
				           metadata->VideoMetadata->h264_dec_config[0]->pps[0],
				           metadata->VideoMetadata->h264_dec_config[0]->pps_len[0]);

       	       VideoTrack->meta->setData(
                               kKeyAVCC, kTypeAVCC, esdswrap, 
                               sizeof(ESDSStruct) + sizeof(uint8_t) + sizeof(uint16_t) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0]));
			/*LOGE("CMMBExtractor::readMetaData pps meta data = %x, %x, %x, %x,   size = %d",
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + sizeof(ESDSSuffixStruct)),
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + sizeof(ESDSSuffixStruct) + 1),
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + sizeof(ESDSSuffixStruct) + 2),
				                                                     *(esdswrap + sizeof(ESDSStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + sizeof(ESDSSuffixStruct) + 3),
				                                                      sizeof(ESDSStruct) + sizeof(ESDSSuffixStruct) + (metadata->VideoMetadata->h264_dec_config[0]->sps_len[0]) + (metadata->VideoMetadata->h264_dec_config[0]->pps_len[0]));*/
#endif
			

               }
	 }

	 
	 
    }

    LOGE("CMMBExtractor::readMetaData come into audio tag");
    if ((NULL != metadata->AudioMetadata) &&
		((CMMB_AUDIO_ALGORITHM_HE_AAC == metadata->AudioMetadata->audio_algorithm) 
		    || (CMMB_AUDIO_ALGORITHM_AAC == metadata->AudioMetadata->audio_algorithm))){ //audio
	 CMMBMetaData->setCString(kKeyMIMEType, "audio/mp4");

	 AudioTrack = new Track();
	 if ((AudioTrack)  )
	 {
	        uint8_t audioesds[2];
	        LOGE("CMMBExtractor::readMetaData have audio track sample rate = %d", metadata->AudioMetadata->audio_sample_rate);
	        AudioTrack->meta = new MetaData;
	        AudioTrack->meta->setCString(kKeyMIMEType, "audio/mp4a-latm");
		 AudioTrack->meta->setInt32(kKeyIsCmmb, 1);
		 if (CMMB_AUDIO_ALGORITHM_HE_AAC == metadata->AudioMetadata->audio_algorithm)
		 {
       	     AudioTrack->meta->setInt32(kKeySampleRate, metadata->AudioMetadata->audio_sample_rate * 2);
	 	 }
		 else
		 {
                   AudioTrack->meta->setInt32(kKeySampleRate, metadata->AudioMetadata->audio_sample_rate);
		 }
       	 //AudioTrack->meta->setInt32(kKeyBitRate, metadata->metadata_audio->bitrate);
		 AudioTrack->meta->setInt32(kKeyChannelCount, 2);

 #if 1
                     ESDSStruct_Audio esds;
       	 	uint8_t esdswrap[sizeof(ESDSStruct_Audio) + 2];
                     uint32_t SampleRateIndex;
			uint32_t sampleRate;
					 
			//audioesds[0] = 0x13;//0x12;
	              //audioesds[1] = 0x10;
	              //profile channel count guding.
	              sampleRate = metadata->AudioMetadata->audio_sample_rate;
			/*if (CMMB_AUDIO_ALGORITHM_HE_AAC == metadata->AudioMetadata->audio_algorithm)
			{
                         sampleRate = sampleRate >> 1;
			}*/
			audioesds[0] = 0x10;
	              audioesds[1] = 0x10;
				  
                     for(int i = 0; i < 16; i++)
                     {
                         if (sampleRate == ADTSSampleFreqTable[i])
                         {
                               SampleRateIndex = i;
				   break;
                         }
			}
					 
			audioesds[0] |= SampleRateIndex >> 1;
			audioesds[1] |= (SampleRateIndex&1) << 7;
			

               	esds.tag_decoderspecificinfo = 0x5;//kTag_DecoderSpecificInfo
               	esds.size_specificInfo = 2; //specific info size;

               	esds.tag_DecoderConfigDescriptor = 0x4; //kTag_DecoderConfigDescriptor
               	esds.size_DecoderConfigDescriptor = esds.size_specificInfo + 2 + 12 + 1; //largest is 127  total size of decoderconfigdescriptior.
               	esds.ObjectTypeIndication = 32; 
			
               	esds.tag_esdescriptor = 0x3;  //kTag_ESDescriptor 
               	esds.size_esdescriptor = esds.size_DecoderConfigDescriptor + 2 + 2 + 1; //largest is 127  total size of esdescriptior.
                     esds.es_id = 1;  //"es1"
                     esds.flag = 0;    // 0
               
               	memcpy((void *)esdswrap, (void *)&esds, sizeof(ESDSStruct_Audio));
                     memcpy(esdswrap + sizeof(ESDSStruct_Audio), 
                                  (void *)audioesds, 
                                  2);
			//LOGE("audio setData size = %d", sizeof(ESDSStruct_Audio) + 2);		 
                     
       	       AudioTrack->meta->setData(
                               kKeyESDS, kTypeESDS, esdswrap, 
                               sizeof(ESDSStruct_Audio) + 2);

#endif		 

		 
	 }	 
    }

    

    //free meta data.
    mDataSource->freeMetadata(metadata);
    //LOGE("CMMBExtractor::readMetaData out");

    return OK;

}



///////////////////////////////////////////////////////////
CMMBSource::CMMBSource(
        const sp<MetaData> &format,
        const sp<DataSource> &dataSource)
    : mFormat(format),
      mDataSource(dataSource),
      mStarted(false),
      mIsAVC(false),
      mGroup(NULL),
      mBuffer(NULL),
      mSrcBuffer(NULL){
    const char *mime;
    //LOGE("CMMBSource::CMMBSource in");
    bool success = mFormat->findCString(kKeyMIMEType, &mime);
    CHECK(success);

    mIsAVC = !strcasecmp(mime, MEDIA_MIMETYPE_VIDEO_AVC);
    //LOGE("CMMBSource::CMMBSource out mISAVC = %d", mIsAVC);
}

CMMBSource::~CMMBSource() {
    if (mStarted) {
        stop();
    }
}


status_t CMMBSource::start(MetaData *params) {
    Mutex::Autolock autoLock(mLock);

    CHECK(!mStarted);
    //LOGE("CMMBSource::start in, mISAVC = %d", mIsAVC);

    mGroup = new MediaBufferGroup;

    int32_t max_size;
	
    max_size = (mIsAVC) ? MAX_CMMB_VIDEO_FRAMESIZE : MAX_CMMB_AUDIO_FRAMESIZE;
    

    mGroup->add_buffer(new MediaBuffer(max_size));

    mSrcBuffer = new uint8_t[max_size];

    mStarted = true;

    //LOGE("CMMBSource::start out mISAVC = %d", mIsAVC);

    return OK;
}



status_t CMMBSource::stop() {
    Mutex::Autolock autoLock(mLock);

    CHECK(mStarted);
    //LOGE("CMMBSource::stop in, mISAVC = %d", mIsAVC);

    if (mBuffer != NULL) {
        mBuffer->release();
        mBuffer = NULL;
    }
	
    delete[] mSrcBuffer;
    mSrcBuffer = NULL;
	
    delete mGroup;
    mGroup = NULL;

    mStarted = false;
    //LOGE("CMMBSource::stop out, mISAVC = %d", mIsAVC);

    return OK;
}


//get meta data.
sp<MetaData> CMMBSource::getFormat() {
    Mutex::Autolock autoLock(mLock);

    return mFormat;
}



status_t CMMBSource::read(
        MediaBuffer **out, const ReadOptions *options) {
    Mutex::Autolock autoLock(mLock);

    //LOGE("CMMBSource::read in, mISAVC = %d", mIsAVC);

    CHECK(mStarted);

    *out = NULL;

    off_t offset;
    size_t size;
    uint32_t dts;
    status_t err;

    err = mGroup->acquire_buffer(&mBuffer);
    

    if (err != OK) {
        CHECK_EQ(mBuffer, NULL);
        return err;
    }
    //LOGE("CMMBSource::read acquire buffer ok, mISAVC = %d", mIsAVC);

    if (!mIsAVC ) {//audio
    
           TCmmbAudioFrame* pFrameHeader;
	    uint32_t CRCLength = 0;
	    uint32_t AudioFrameSize;
	    uint8_t *pBuffer;
	    static uint32_t preAudioFrameTimestamp = 0xFFFFFFFF;
	    uint32_t  NowAudioFrameTimestamp;
           ssize_t num_bytes_read =
               mDataSource->readAt(1, (uint8_t *)mBuffer->data(), 0);
   
           if (0 == num_bytes_read ) {
               mBuffer->release();
               mBuffer = NULL;
   
               return ERROR_END_OF_STREAM;
           }



	    
	    //work around num_bytes_read is not good everytime.
	    //num_bytes_read = 

           //CRC not check, service provider do it.
           
	    /*if (((uint8_t *)(mBuffer->data()))[1] & 0x01)
	    {
                CRCLength = 2;
	    }*/  
	    
	    CRCLength = 0;
           pBuffer = (uint8_t *)(mBuffer->data()) + sizeof(TCmmbAudioFrame);
           AudioFrameSize = (uint32_t)((pBuffer[3] & 0x03) << 11)   | // take the lowest 2 bits in the 4th byte of the header
                        (uint32_t)(pBuffer[4] << 3)         |     // take the whole 5th byte(8 bits) of the header
                        (uint32_t)((pBuffer[5] & 0xe0) >> 5);     // take the highest 3 bits in the 6th byte of the header
           
          
           pFrameHeader = (TCmmbAudioFrame*)(mBuffer->data());

	  if (0xFFFFFFFF == preAudioFrameTimestamp)
	   {
              preAudioFrameTimestamp = pFrameHeader->timestamp;
		//LOGE("First audio frame timestamp = %d", pFrameHeader->timestamp);
	   } 
	   /*else if (pFrameHeader->timestamp != (preAudioFrameTimestamp + 960))
	   {
              LOGE("lost audio frame pFrameHeader->timestamp = %d, preAudioFrameCount = %d", pFrameHeader->timestamp, preAudioFrameTimestamp); 
	   }*/
	   preAudioFrameTimestamp = pFrameHeader->timestamp;
   
           CHECK(mBuffer != NULL);
           //mBuffer->set_range(sizeof(TCmmbFrameHeader) + ADTS_HEADER_LENGTH_CMMB + CRCLength, 
		   	            // (num_bytes_read - sizeof(TCmmbFrameHeader) - ADTS_HEADER_LENGTH_CMMB - CRCLength));
           mBuffer->set_range(sizeof(TCmmbAudioFrame) + ADTS_HEADER_LENGTH_CMMB + CRCLength, (AudioFrameSize - ADTS_HEADER_LENGTH_CMMB));
		   
           mBuffer->meta_data()->clear();
           mBuffer->meta_data()->setInt64(
                   kKeyTime, ((int64_t)pFrameHeader->timestamp * 1000 * 10 / 225));
	    //LOGE("CMMBSource readaudio pFrameHeader->time_stamp = %d, num_bytes_read = %d",
		//	                                                                                   pFrameHeader->timestamp,
		//	                                                                                   num_bytes_read);
	     /*LOGE("audio frame = %x, %x, %x, %x, %x, %x, %x, %x ,{%x, %x, %x, %x}", *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame) + CRCLength),
		 	                                                                               *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength + 1),
		 	                                                                               *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength + 2),
		 	                                                                                *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength+ 3),
		 	                                                                               *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength + 4),
		 	                                                                               *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength + 5),
		 	                                                                               *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength + 6),
		 	                                                                               *((uint8_t*)pFrameHeader+ ADTS_HEADER_LENGTH_CMMB +sizeof(TCmmbAudioFrame)+ CRCLength+ 7),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 3),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 2),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 1),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 0),
		 	                                                                               num_bytes_read);*/

        *out = mBuffer;
        mBuffer = NULL;

        return OK;
 
    } else {//video
        // Whole NAL units are returned but each fragment is prefixed by
        // the start code (0x00 00 00 01).

           TCmmbVideoFrame* pFrameHeader;	
	    static uint32_t preVideoFrameTimestamp = 0xFFFFFFFF;
	    uint32_t  NowVideoFrameTimestamp;
           ssize_t num_bytes_read =
               mDataSource->readAt(0, (uint8_t *)mBuffer->data(), 0);
	    static uint32_t FrameNo = 0;

	    //LOGE("CMMBExtrator read video NAL FrameNo = %d", FrameNo);
	    FrameNo ++;
   
           if (sizeof(TCmmbVideoFrame) >= num_bytes_read ) {
               mBuffer->release();
               mBuffer = NULL; 
	        LOGE("CMMBExtractor read video size is zero");
   
               return ERROR_END_OF_STREAM;
           }
            pFrameHeader = (TCmmbVideoFrame*)(mBuffer->data());
			
         /*  if (0xFFFFFFFF == preVideoFrameTimestamp)
           {
               preVideoFrameTimestamp = pFrameHeader->timestamp;
		 LOGE("First video frame timestamp = %d", pFrameHeader->timestamp);
           } 
           else if ( (pFrameHeader->timestamp != (preVideoFrameTimestamp + 900))
		   	            && (pFrameHeader->timestamp != preVideoFrameTimestamp))
           {
               LOGE("lost video frame pFrameHeader->timestamp = %d, preVideoFrameTimestamp = %d", pFrameHeader->timestamp, preVideoFrameTimestamp); 
           }
           preVideoFrameTimestamp = pFrameHeader->timestamp;	

	    LOGE("video frame = %x, %x, %x, %x, %x, %x, %x, %x ,{%x, %x, %x, %x}", *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame)),
		 	                                                                               *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame) + 1),
		 	                                                                               *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame) + 2),
		 	                                                                                *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame)+ 3),
		 	                                                                               *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame) + 4),
		 	                                                                               *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame) + 5),
		 	                                                                               *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame) + 6),
		 	                                                                               *((uint8_t*)pFrameHeader +sizeof(TCmmbVideoFrame)+ 7),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 3),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 2),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 1),
		 	                                                                               *((uint8_t*)pFrameHeader + num_bytes_read - 1 - 0));*/
   
           CHECK(mBuffer != NULL);
           mBuffer->set_range(sizeof(TCmmbVideoFrame), (num_bytes_read - sizeof(TCmmbVideoFrame)));
           mBuffer->meta_data()->clear();
           mBuffer->meta_data()->setInt64(
                   kKeyTime, ((int64_t)pFrameHeader->timestamp * 1000 * 10 / 225) );
	    //LOGE("CMMBSource readvideo pFrameHeader->time_stamp = %d",
		//	                                                                                   pFrameHeader->timestamp);
	
        *out = mBuffer;
        mBuffer = NULL;
	//LOGE("CMMBSource::read video out");

        return OK;
    }
}


}  // namespace android

//MTK_OP01_PROTECT_END
