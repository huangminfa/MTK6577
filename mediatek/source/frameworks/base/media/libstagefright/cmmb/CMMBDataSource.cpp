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
#include "CMMBDataSource.h"
#include <media/stagefright/MediaDebug.h>
#include <dlfcn.h>                  

               

namespace android {

CMMBDataSource::CMMBDataSource(int fd)
{
    const char* dlerr;
    //do null
    pVideoFrame = NULL;
    FrameOffset = 0;

    
    libcmmbsp_handle = dlopen("/data/data/com.mediatek.cmmb.app/lib/libcmmbsp.so",RTLD_NOW);                           // dlopen ´ò¿ªso

    dlerr = dlerror();
    if (dlerr != NULL) 
	    LOGE("CMMBDataSource: dlopen() error: %s\n", dlerr);
    if(!libcmmbsp_handle)
    {
	    LOGE("CMMBDataSource dlopen /data/data/com.mediatek.cmmb.app/lib/libcmmbsp fail,then open /system/lib/so");
	    libcmmbsp_handle = dlopen("libcmmbsp.so",RTLD_NOW);
	    if(!libcmmbsp_handle)
	    {
		    LOGE("CMMBDataSource dlopen /system/lib/so fail");
	    }
    }
    if(libcmmbsp_handle){
            LOGI("dlopen success,then dlsym");
	    // get CmmbFreeVideoFrame function point 
	    F_CmmbFreeVideoFrame = (CmmbFreeVideoFrame_T)dlsym(libcmmbsp_handle,"CmmbFreeVideoFrame");    
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbFreeVideoFrame dlsym() error: %s\n", dlerr);
                    F_CmmbFreeVideoFrame=NULL;
	    }
	    // get CmmbReadVideoFrame function point 
	    F_CmmbReadVideoFrame = (CmmbReadVideoFrame_T)dlsym(libcmmbsp_handle,"CmmbReadVideoFrame");    
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbReadVideoFrame dlsym() error: %s\n", dlerr);
                    F_CmmbReadVideoFrame = NULL;
	    }
	    // get CmmbReadAudioFrame function point
	    F_CmmbReadAudioFrame=(CmmbReadAudioFrame_T)dlsym(libcmmbsp_handle,"CmmbReadAudioFrame");    
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbReadAudioFrame dlsym() error: %s\n", dlerr);
                    F_CmmbReadAudioFrame=NULL;
	    }
	    // get CmmbFreeAudioFrame function point 
	    F_CmmbFreeAudioFrame = (CmmbFreeAudioFrame_T)dlsym(libcmmbsp_handle,"CmmbFreeAudioFrame");   
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbFreeAudioFrame dlsym() error: %s\n", dlerr);
                    F_CmmbFreeAudioFrame=NULL;
	    }
	    // get CmmbFlushAVFrame function point 
	    F_CmmbFlushAVFrame = (CmmbFlushAVFrame_T)dlsym(libcmmbsp_handle,"CmmbFlushAVFrame");    
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbFlushAVFrame dlsym() error: %s\n", dlerr);
                    F_CmmbFlushAVFrame =NULL;
	    }
	    // get CmmbGetMetadata function point 
	    F_CmmbGetMetadata = (CmmbGetMetadata_T)dlsym(libcmmbsp_handle,"CmmbGetMetadata");    
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbGetMetadata dlsym() error: %s\n", dlerr);
                    F_CmmbGetMetadata = NULL;
	    }

	    // get CmmbFlushOldestFrame function point 
	    F_CmmbFlushOldestFrame = (CmmbFlushOldestFrame_T)dlsym(libcmmbsp_handle,"CmmbFlushOldestFrame");    
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "CMMBDataSource::CMMBDataSource CmmbFlushOldestFrame dlsym() error: %s\n", dlerr);
                    F_CmmbFlushOldestFrame = NULL;
	    }
    }

    /*release redundancy frame to push forward playback*/
    if(F_CmmbFlushOldestFrame)
    {
	    F_CmmbFlushOldestFrame();
    }
    else
    {
	    LOGE("Error F_CmmbFlushOldestFrame=NULL");
    }
    //test	
 #if 0   
    filesource = new CMMBFileSource;
#endif	
}


CMMBDataSource::~CMMBDataSource() {
#if 1
	if(F_CmmbFlushAVFrame)
	{
		F_CmmbFlushAVFrame();
	}
	else
	{
		LOGE("cmmbdatasource::~cmmbdatasource() error F_CmmbFlushAVFrame=NULL");
	}

#else
	CmmbFlushAVFrame();
#endif  

if(NULL != libcmmbsp_handle)
	dlclose(libcmmbsp_handle);

	libcmmbsp_handle = NULL;

    F_CmmbFreeVideoFrame=NULL;
    F_CmmbReadVideoFrame=NULL;
    F_CmmbReadAudioFrame=NULL;
    F_CmmbFreeAudioFrame=NULL;
    F_CmmbFlushAVFrame=NULL;
    F_CmmbGetMetadata=NULL;
    F_CmmbFlushOldestFrame = NULL;
}

status_t CMMBDataSource::initCheck() const {
    //LOGE("CMMBDataSource::initCheck");
    return OK;
}

bool CMMBDataSource::FindNaluBoundary(uint8_t* srcBuffer, uint32_t *offset,  uint32_t bufferMaxSize, uint32_t * EndSize)
{
       int i;
       uint8_t *begin;
	bool IsFoundNaluBoundaryInStart = false;
	int  preOffset;

	preOffset = *offset;
	
       begin = ((uint8_t *)srcBuffer + *offset);

	for(i=*offset; i< (bufferMaxSize-4); i++, begin++)
	{
		if( begin[0]==0 && begin[1]==0 && begin[2]==0 && begin[3]==0x1)
		{
		      *offset = i;
		      IsFoundNaluBoundaryInStart = true;
		      break;
		}
	}

	if(i != preOffset)
	{
           LOGE("CMMBDataSource::FindNaluBoundary maybe error, first bytes are not sync, %x, %x, %x, %x",
		   	                                                                                                        *(srcBuffer + preOffset),
		   	                                                                                                        *(srcBuffer + preOffset + 1),
		   	                                                                                                        *(srcBuffer + preOffset + 2),
		   	                                                                                                        *(srcBuffer + preOffset + 3) );
	}

       if (false == IsFoundNaluBoundaryInStart)
       {
            if(*offset < (bufferMaxSize-4))
                LOGE("cmmb FindNaluBoundary not found nalue start code");
	     return false;
       }

	   
	begin = ((uint8_t *)srcBuffer + *offset);
       
	for(i=*offset; i< (bufferMaxSize-4); i++, begin++)
	{
		if( begin[0]==0 && begin[1]==0 && begin[2]==0 && begin[3]==0x1 && (begin != (srcBuffer + *offset)) )
		{
		      *offset += 4;
		      *EndSize = (int)(begin - (unsigned char *)srcBuffer - *offset);
			return true;
		}
	}
       *offset += 4;
	*EndSize = (int)(bufferMaxSize - *offset);
       return true;
}

ssize_t CMMBDataSource::readAt(off64_t offset, void *data, size_t size) {
    Mutex::Autolock autoLock(mLock);

     //LOGE("CMMBDataSource::readAt, mIsAVC = %d", (!offset));
    if (NULL == data)
    {
        return 0;
    }

    if (0 == offset) //video
    { 
#if 1
        uint32_t EndSize;
        static int32_t FrameCount = 0;
	 static uint32_t preTimestamp = 0xFFFFFFFF;

FoundBoundary:
	
	 if ((NULL != pVideoFrame)
	 	   && (true == FindNaluBoundary(pVideoFrame->VideoFrameBuf, &FrameOffset, pVideoFrame->VideoFrameLen, &EndSize) )  )
	 {
	     memcpy((void *)data, (void *)pVideoFrame, sizeof(TCmmbVideoFrame));
	     memcpy((uint8_t *)data + sizeof(TCmmbVideoFrame), ((uint8_t*)(pVideoFrame->VideoFrameBuf) + FrameOffset),  EndSize);
	     FrameOffset += EndSize;
	     //LOGE("CMMBDataSource::readAt video findnalu frameoffset = %d", FrameOffset);
	     return (sizeof(TCmmbVideoFrame) + EndSize);
	 }
	 else
	 {
		 if (NULL != pVideoFrame)
		 {
#if 1
			 if(F_CmmbFreeVideoFrame)
			 {
				 F_CmmbFreeVideoFrame(pVideoFrame);
			 }
			 else
			 {
				 LOGE("Error F_CmmbFreeVideoFrame =NULL");
			 }

#else
			 CmmbFreeVideoFrame(pVideoFrame); 
#endif
			 pVideoFrame = NULL;
		 }
#if 1		
		 if(F_CmmbReadVideoFrame)
		 {
			 pVideoFrame = (TCmmbVideoFrame*)(F_CmmbReadVideoFrame());
		 }
		 else
		 {
			 LOGE("Error F_CmmbReadVideoFrame =NULL");
		 }
#else
		 pVideoFrame = CmmbReadVideoFrame();
#endif
       	 if (NULL == pVideoFrame)
       	 {
                   LOGE("CMMBDataSource::readAt videoframe is null");
       	     return 0;
       	 }
		 if (0xFFFFFFFF == preTimestamp)
		 {
                   preTimestamp = pVideoFrame->timestamp * 10 / 225;
		 }
		 else
		 {
                   if ((((pVideoFrame->timestamp * 10 / 225) - preTimestamp) > 50) 
			    || (((pVideoFrame->timestamp * 10 / 225) - preTimestamp) < 30))
                   {
                        //LOGE("CMMBDataSource::readAt  video timestamp maybe error, pretimestamp = %d, timestamp = %d",  preTimestamp,
							                                                                                                                   //    ((pVideoFrame->timestamp * 10 / 225)));
		     }
		 }
		 preTimestamp = pVideoFrame->timestamp * 10 / 225;
		 
       	 FrameOffset = 0;
		/* LOGE("CMMB readVideoFrame, readattimestamp = %d, count = %d ,[[ %x, %x, %x, %x, %x, %x, %x, %x",  (pVideoFrame->timestamp * 10 / 225), FrameCount, *((uint8_t *)pVideoFrame->VideoFrameBuf),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf + 1),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf + 2),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf + 3),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf + 4),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf + 5),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf + 6),
		 	                                                                             *((uint8_t *)pVideoFrame->VideoFrameBuf+ 7)  );*/
		 FrameCount ++;
		 goto FoundBoundary;
	 }

#else
        TCmmbFrameHeader* pVideoFrame;

	 pVideoFrame = filesource->CmmbReadVideoFrame();
	 if (NULL == pVideoFrame)
	 {
            LOGE("CMMBDataSource::readAt videoframe is null");
	     return 0;
	 }
	 
	 //copy data.
	 memcpy((void *)data, (void*)pVideoFrame, (sizeof(TCmmbFrameHeader) + pVideoFrame->frame_size));


	 filesource->CmmbFreeFrame(pVideoFrame); 
	 return (sizeof(TCmmbFrameHeader) + pVideoFrame->frame_size);
#endif	 

    }
    else if (1 == offset) //audio
    {
        TCmmbAudioFrame* pAudioFrame;
        uint32_t framesize;
	 static uint32_t frameNo = 0;
	 static uint32_t preTimestamp_audio = 0xFFFFFFFF;
#if 1
	 if(F_CmmbReadAudioFrame)
	 {
		 pAudioFrame = (TCmmbAudioFrame*)(F_CmmbReadAudioFrame());
	 }
	 else
	 {
		 LOGE("Error F_CmmbReadAudioFrame =NULL");
	 }
#else
	 pAudioFrame = CmmbReadAudioFrame();
#endif
	 if (NULL == pAudioFrame)
	 {
            LOGE("CMMBDataSource::readAt audioframe is null");
	     return 0;
	 }

		 if (0xFFFFFFFF == preTimestamp_audio)
		 {
                   preTimestamp_audio = pAudioFrame->timestamp * 10 / 225;
		 }
		 else
		 {
                   if ((((pAudioFrame->timestamp * 10 / 225) - preTimestamp_audio) > 50) 
			    || (((pAudioFrame->timestamp * 10 / 225) - preTimestamp_audio) < 30))
                   {
                        /*LOGE("CMMBDataSource::readAt audio timestamp maybe error, pretimestamp = %d, timestamp = %d",  preTimestamp_audio,
							                                                                                                                       ((pAudioFrame->timestamp * 10 / 225)));*/
		     }
		 }
		 preTimestamp_audio = pAudioFrame->timestamp * 10 / 225;	 

	 //copy data
	 //LOGE("CMMBDataSource audio Readat  readattimestamp = %d, framesize = %d, frameNo = %d",  (pAudioFrame->timestamp * 10 / 225), pAudioFrame->AudioFrameLen, frameNo);
	 frameNo ++;
	 memcpy((void *)data, (void*)pAudioFrame, (sizeof(TCmmbAudioFrame)));
        memcpy(((uint8_t *)data + sizeof(TCmmbAudioFrame)), (void*)pAudioFrame->AudioFrameBuf,  pAudioFrame->AudioFrameLen);
	 framesize = pAudioFrame->AudioFrameLen;
	 
#if 1
	 if(F_CmmbFreeAudioFrame)
	 {
		 F_CmmbFreeAudioFrame(pAudioFrame);
	 }
	 else
	 {
		 LOGE("Error F_CmmbFreeAudioFrame =NULL");
	 }
#else
	 CmmbFreeAudioFrame(pAudioFrame);
#endif	 

	 return (sizeof(TCmmbAudioFrame) + framesize);
	 
    }
    else
    {
        LOGE("readat offset param. error");
	 return 0;
    }

    
}

status_t CMMBDataSource::getSize(off_t *size) {

     //LOGE("CMMBDataSource::getSize");
    //don't implement.
    return ERROR_UNSUPPORTED;
}

void* CMMBDataSource::getMetadata(){
	
    TCmmbMetadata* metadata;
    LOGE("CMMBDataSource::getMetadata");
#if 1	
    if(F_CmmbGetMetadata)
    {
	    metadata = (TCmmbMetadata*)(F_CmmbGetMetadata());
    }
    else
    {
	    LOGE("Error F_CmmbGetMetadata =NULL");
    }
#else
    metadata = CmmbGetMetadata();
    //test
    //metadata->VideoMetadata = NULL;
#endif
    if (NULL == metadata)
    {
        LOGE("getMetadata null ");
        return NULL;
    }
    //LOGE("finish CmmbGetMetadata");

    return (void *)metadata;
}


void   CMMBDataSource::freeMetadata(void * metadata)
{
     //LOGE("CMMBDataSource::freeMetadata");
  #if 0
     filesource->CmmbFreeMetadata((cmmb_metadata*)metadata);
  #else
     
  #endif
}



}  // namespace android
//MTK_OP01_PROTECT_END

