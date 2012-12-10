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
#define LOG_TAG "CMMBProvider_test"
#include <utils/Log.h>


#include <arpa/inet.h>

#include <ctype.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <media/stagefright/Utils.h>
#include <utils/String8.h>
#include <stdio.h>
#include <media/stagefright/CMMBProvider_test.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaDebug.h>
//#include <media/stagefright/FileSource.h>

#define SPS_TEST_LEN  (15)//(23)
#define PPS_TEST_LEN  (8)


namespace android {

CMMBFileSource::CMMBFileSource()
 {
    fullbuffer_audio = NULL;
    fullbuffer_video = NULL;

    FileMetadata.metadata_audio = NULL;
    FileMetadata.metadata_video = NULL;

#if 1  // test only video
    uint32_t total;

    //sps pps
    filesource_video = new FileSource("/sdcard/test_video.mp4");
    filesource_video->initCheck();
    filesource_video->getSize(&filesize_video);
	
    fullbuffer_video = NULL;
    fullbuffer_video = (uint8_t *)malloc(MAX_BUFFER);
    if (NULL == fullbuffer_video)
    {
         LOGE("CMMB Video test malloc fullbuffer error");
	  return;
    }    

    if (filesize_video > MAX_BUFFER)
    {
         uint32_t gotsize;
         gotsize = filesource_video->readAt(0, fullbuffer_video, MAX_BUFFER);
	  if (gotsize =! MAX_BUFFER)
	  {
             LOGE("CMMB video filesource read error , size = %d", gotsize);
	  }
    }
	
    if (filesize_video < MAX_BUFFER)
    {
         uint32_t gotsize;
         gotsize = filesource_video->readAt(0, fullbuffer_video, filesize_video);
	  if (gotsize =! filesize_video)
	  {
             LOGE("CMMB video filesource read error , size = %d", gotsize);
	  }
    }
    

    
    FileMetadata.metadata_video = &Filemetadata_video;


    Filemetadata_video.algorithm_type = CMMB_VIDEO_ALGORITHM_H264;
    Filemetadata_video.bitrate = 300 * 1024;
    Filemetadata_video.x_coord = 0;
    Filemetadata_video.y_coord = 0;
    Filemetadata_video.disp_priority = 0;
    Filemetadata_video.x_resolution = 320;
    Filemetadata_video.y_resolution = 240;
    Filemetadata_video.frame_rate = 25;
    Filemetadata_video.h264 = &Fileh264;


    	
    Fileh264.sps_count = 1;                
    Fileh264.sps_len[0] = SPS_TEST_LEN;               
    Fileh264.pps_count = 1;                
    Fileh264.pps_len[0] = PPS_TEST_LEN;

   readposition_fullbuffer_video = 0;	



    memcpy(Fileh264.sps[0],  fullbuffer_video, SPS_TEST_LEN);
    readposition_fullbuffer_video += SPS_TEST_LEN;	

    memcpy(Fileh264.pps[0],  (fullbuffer_video + readposition_fullbuffer_video), PPS_TEST_LEN);
    readposition_fullbuffer_video += PPS_TEST_LEN;	

    //¶àÓàÒ»¸öSEI
    //readposition_fullbuffer_video += 16;
    //PREFIX_SEI_SIZE



	LOGE("CMMB video TestOpenFile sucess return ");
#endif

#if 1

    filesource_audio = new FileSource("/sdcard/test_audio.mp4");
    filesource_audio->initCheck();
    filesource_audio->getSize(&filesize_audio);
	
    fullbuffer_audio = NULL;
    fullbuffer_audio = (uint8_t *)malloc(MAX_BUFFER);
    if (NULL == fullbuffer_audio)
    {
         LOGE("CMMB Audio test malloc fullbuffer error audio");
	  return;
    }    

    if (filesize_audio > MAX_BUFFER)
    {
         uint32_t gotsize;
         gotsize = filesource_audio->readAt(0, fullbuffer_audio, MAX_BUFFER);
	  if (gotsize =! MAX_BUFFER)
	  {
             LOGE("CMMB Audio filesource read error , size = %d", gotsize);
	  }
    }
	
    if (filesize_audio < MAX_BUFFER)
    {
         uint32_t gotsize;
         gotsize = filesource_audio->readAt(0, fullbuffer_audio, filesize_audio);
	  if (gotsize =! filesize_audio)
	  {
             LOGE("CMMB Audio filesource read error , size = %d", gotsize);
	  }
    }
    LOGE("CMMB audio FileSource::CMMBFileSource data = %x, %x, %x, %x", *fullbuffer_audio, *(fullbuffer_audio+1),*(fullbuffer_audio+2),*(fullbuffer_audio+3));
    

    
    
    FileMetadata.metadata_audio = &Filemetadata_audio;

    Filemetadata_audio.algorithm_type = CMMB_AUDIO_ALGORITHM_HE_AAC;
    Filemetadata_audio.sample_rate = 48000;//44100;
    Filemetadata_audio.bitrate = 32 * 1000;//123.45 * 1000; //b

    AACHeader[0] = *(fullbuffer_audio + 1);
    AACHeader[1] = *(fullbuffer_audio + 1 + 1);
    AACHeader[2] = *(fullbuffer_audio + 1 + 2);


    readposition_fullbuffer_audio = 0;
#endif

    LOGE("CMMBTestOpenFile sucess return ");
    
}


CMMBFileSource::~CMMBFileSource() {
#if 0
	if (hSrcFile != NULL) {
        fclose(hSrcFile);
        hSrcFile = NULL;
    }
#endif
    if (NULL != fullbuffer_audio)
    {
         free(fullbuffer_audio);
	  fullbuffer_audio = NULL;
    }
    if (NULL != fullbuffer_video)
    {
        free(fullbuffer_video);
        fullbuffer_video = NULL;
    }
}


bool CMMBFileSource::FindFrameBoundary(void *pStream,  uint32_t MaxBuffSize, uint32_t * frameEndByteOffset)
{
       int i;
       unsigned char *begin;
       begin = (unsigned char *)pStream;
       
	for(i=0; i< (MaxBuffSize-5); i++, begin++)
	{
		if( begin[0]==0 && begin[1]==0 && begin[2]==0 && begin[3]==0x1 && begin[4]==0x6 && (begin != pStream) )
		{
		      *frameEndByteOffset = (int)(begin - (unsigned char *)pStream);
			return true;
		}
	}
       return false;
   

}

//read audio frame
TCmmbFrameHeader* CMMBFileSource::CmmbReadAudioFrame ()
{
   uint32_t offset;
   uint8_t *  buffer;
   static uint32_t FrameNo = 0;
   uint32_t found;
   uint32_t foundflag = 0;
   TCmmbFrameHeader *AudioFrame;
   uint32_t i;

   LOGE("CMMB file readaudio frame, frameNo = %d", FrameNo);
   if (readposition_fullbuffer_audio >= filesize_audio)
   {
       LOGE("CmmbReadAudioFrame end of frame");
   	return NULL;
   }

   

   buffer = fullbuffer_audio + readposition_fullbuffer_audio;
   LOGE("CmmbReadAudioFrame readposition_fullbuffer = %d ,data = %x, %x, %x, %x", readposition_fullbuffer_audio, *(buffer), *(buffer + 1),*(buffer+2),*(buffer+3));

   
   // ADTS find audio prefix 0xFFF
   for(i = 0; i < (filesize_audio - readposition_fullbuffer_audio); i++)
   {
       //if ((*(buffer + i) == 0xFF) &&  ((*(buffer + i + 1) & 0xf0) == 0xf0) && (0 != i))
       //if ((*(buffer + i) == 0xFF) &&  ((*(buffer + i + 1) ) == 0xF9) && (0 != i))
       if ((*(buffer + i) == 0xFF) &&  ((*(buffer + i + 1) & 0xf0) == 0xf0) 
	   	                                 &&  (*(buffer + i + 1) == AACHeader[0])
	   	                                 &&  (*(buffer + i + 2) == AACHeader[1])
	   	                                 &&  (*(buffer + i + 3) == AACHeader[2])
	   	                                 && (0 != i))
       {
            found = i;
	     foundflag = 1;
	     break;		
	}
   }

   if (0 == foundflag)
   {
       LOGE("CmmbReadAudioFrame not found audio frame boundary i = %d", i);
	return NULL;
   }
   
   AudioFrame = (TCmmbFrameHeader*)malloc(sizeof(TCmmbFrameHeader) + i);
   if (NULL == AudioFrame)
   {
        LOGE("cmmb test CmmbReadAudioFrame malloc error");
        return NULL;
   }

   memcpy(((uint8_t *)AudioFrame + sizeof(TCmmbFrameHeader)), (fullbuffer_audio + readposition_fullbuffer_audio), i);
   
   AudioFrame->sample_count = FrameNo;  // num of this frame
   AudioFrame->time_stamp = 23 * FrameNo;      // time stamp of this frame
   AudioFrame->time_scale = 1;       // timescale of this track.
   AudioFrame->frame_size = i;       // size of the frame       
    
   readposition_fullbuffer_audio += i;
   FrameNo ++ ;
   LOGE("CmmbReadAudioFrame return AudioFrame = %x, i = %d, framesize = %d", AudioFrame, i, AudioFrame->frame_size);
   
   return AudioFrame;
}

//read video rame
TCmmbFrameHeader* CMMBFileSource::CmmbReadVideoFrame ()
{
   static uint32_t FrameNo = 0;
   TCmmbFrameHeader* VideoFrame;
   uint32_t frameendoffset;
   LOGE("CMMB file readvideo frame, frameNo = %d, start code = %x, %x, %x, %x, remainfilesize = %d", FrameNo,
   	                                                                                                                      *(fullbuffer_video + readposition_fullbuffer_video),
   	                                                                                                                      *(fullbuffer_video + readposition_fullbuffer_video + 1),
   	                                                                                                                      *(fullbuffer_video + readposition_fullbuffer_video + 2),
   	                                                                                                                      *(fullbuffer_video + readposition_fullbuffer_video + 3),
   	                                                                                                                      (filesize_video - readposition_fullbuffer_video) );
   if (readposition_fullbuffer_video >= filesize_video)
   {
       LOGE("CmmbReadVideoFrame end of frame");
   	return NULL;
   }
   if (false == FindFrameBoundary((void *)(fullbuffer_video + readposition_fullbuffer_video), (filesize_video - readposition_fullbuffer_video), &frameendoffset))
   {
         LOGE("CMMBFileSource video can't find frameboundary");
	  return NULL;
   }
      
   
   VideoFrame = (TCmmbFrameHeader*)malloc(frameendoffset + sizeof(TCmmbFrameHeader) - PREFIX_SEI_SIZE);
   if (NULL == VideoFrame)
   {
        LOGE("cmmb test CmmbReadVideoFrame malloc error");
   }
   memcpy(((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader)), (fullbuffer_video + readposition_fullbuffer_video + PREFIX_SEI_SIZE), frameendoffset - PREFIX_SEI_SIZE);
   readposition_fullbuffer_video += frameendoffset;
   LOGE("CMMB file readvideo frame header = %x, %x, %x, %x, %x, %x, %x, %x", *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader)),
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 1),
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 2),
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 3),
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 4),  
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 5),
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 6),
   	                                                                                          *((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader) + 7)   );

   VideoFrame->sample_count = FrameNo;  
   VideoFrame->time_stamp = 33 * FrameNo;      
   VideoFrame->time_scale = 1;       
   VideoFrame->frame_size = frameendoffset - PREFIX_SEI_SIZE;  

   FrameNo ++;
   
   return VideoFrame;
}


//when finish used frame , free  interface.
bool CMMBFileSource::CmmbFreeFrame (TCmmbFrameHeader* frame)
{
   LOGE("CMMB free frame");
   if (NULL != frame)
      free((void *)frame);
   else
     LOGE("cmmb test free frame fail.");
   
   return OK;
}

//get metadata
cmmb_metadata* CMMBFileSource::CmmbGetMetadata ()
{ 
   LOGE("CMMB get metadata");
   return &FileMetadata;
}

//free 
bool CMMBFileSource::CmmbFreeMetadata (cmmb_metadata* data)
{
   LOGE("CmmbFreeMetadata");
   return OK;
}



}  // namespace android

#if 0
void CMMBTestOpenFile(int fd)
{
   uint32_t total;
   bool Ret;

    /// Open the file.
    //hSrcFile = fopen("/mnt/sdcard/DSCAM_H264_track2.h264", "rb");
    hSrcFile = fopen("/sdcard/test.mp4", "rb");
    LOGE("into CMMBTestOpenFile");
    hSrcFile = fdopen(fd, "rb");
    if(NULL == hSrcFile)
    {
    	LOGE("cmmb test create file error, hSrcFile = %x", hSrcFile);
    	return;
    }


    //sps pps
    FileMetadata.metadata_video = &Filemetadata_video;
    FileMetadata.metadata_audio = NULL;

    Filemetadata_video.algorithm_type = CMMB_VIDEO_ALGORITHM_H264;
    Filemetadata_video.bitrate = 300 * 1024;
    Filemetadata_video.x_coord = 0;
    Filemetadata_video.y_coord = 0;
    Filemetadata_video.disp_priority = 0;
    Filemetadata_video.x_resolution = 320;
    Filemetadata_video.y_resolution = 240;
    Filemetadata_video.frame_rate = 25;
    Filemetadata_video.h264 = &Fileh264;


    	
    Fileh264.sps_count = 1;                
    Fileh264.sps_len[0] = 23;               
    Fileh264.pps_count = 1;                
    Fileh264.pps_len[0] = 9;
	
    fullbuffer = NULL;
    fullbuffer = (uint8_t *)malloc(MAX_BUFFER);
    if (NULL == fullbuffer)
    {
         LOGE("CMMB test malloc fullbuffer error");
         fclose(hSrcFile);
	  return;
    }

    total == fread(fullbuffer, 1, 23, hSrcFile);
    //total == fread(Fileh264.sps[0], 1, 23, hSrcFile);
    if (0 == total)
    {
         LOGE("CMMB test readfile sps error, hSrcFile = %x", hSrcFile);
	  fclose(hSrcFile);
	  return;
    }
	
    total == fread(Fileh264.pps[0], 1, 9, hSrcFile);
    if (0 == total)
    {
         LOGE("CMMB test readfile pps error");
	  fclose(hSrcFile);
	  return;
    }


    
    fullbuffer_size == fread(fullbuffer, 1, MAX_BUFFER, hSrcFile);
    if (0 == fullbuffer_size)
    {
         LOGE("CMMB test readfile sps error");
	  free(fullbuffer);
	  fclose(hSrcFile);
	  return;
    }
    readposition_fullbuffer = 16;
    LOGE("CMMBTestOpenFile sucess return ");
     
}

void CMMBTestCloseFile()
{
    free(fullbuffer);
    fclose(hSrcFile);
}


bool FindFrameBoundary(void *pStream,  uint32_t MaxBuffSize, uint32_t * frameEndByteOffset)
{
       int i;
       unsigned char *begin;
       begin = (unsigned char *)pStream;
       
	for(i=0; i< (MaxBuffSize-4); i++, begin++)
	{
		if( begin[0]==0 && begin[1]==0 && begin[2]==1 && begin[3]==0x6 && (begin != pStream) )
		{
		      *frameEndByteOffset = (int)(begin - (unsigned char *)pStream);
			return true;
		}
	}
       return false;
   

}

//read audio frame
TCmmbFrameHeader* CmmbReadAudioFrame ()
{
   
   return NULL;
}

//read video rame
TCmmbFrameHeader* CmmbReadVideoFrame ()
{
   static uint32_t FrameNo = 0;
   TCmmbFrameHeader* VideoFrame;
   uint32_t frameendoffset;
   
   FindFrameBoundary((void *)(fullbuffer + readposition_fullbuffer), (fullbuffer_size - readposition_fullbuffer), &frameendoffset);
      
   
   VideoFrame = (TCmmbFrameHeader*)malloc(frameendoffset + sizeof(TCmmbFrameHeader));
   if (NULL == VideoFrame)
   {
        LOGE("cmmb test CmmbReadVideoFrame malloc error");
   }
   memcpy(((uint8_t *)VideoFrame + sizeof(TCmmbFrameHeader)), (fullbuffer + readposition_fullbuffer), frameendoffset);
   readposition_fullbuffer += frameendoffset;

   VideoFrame->sample_count = FrameNo;  
   VideoFrame->time_stamp = 33 * FrameNo;      
   VideoFrame->time_scale = 1;       
   VideoFrame->frame_size = frameendoffset;  

   FrameNo ++;
   
   return VideoFrame;
}


//when finish used frame , free  interface.
bool CmmbFreeFrame (TCmmbFrameHeader* frame)
{
   if (NULL != frame)
      free((void *)frame);
   else
     LOGE("cmmb test free frame fail.");
   
   return 1;
}

//get metadata
cmmb_metadata* CmmbGetMetadata ()
{ 
   return &FileMetadata;
}

//free 
bool CmmbFreeMetadata (cmmb_metadata* data)
{
   return 1;
}
#endif
//MTK_OP01_PROTECT_END

