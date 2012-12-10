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

#if 0
typedef enum
{
  _BYTE_  = 0x5000,
  _WORD_,
  _LONG_
};
#endif

#include "vcodec_OAL.h"
#include "val_types.h"

#define  SP5_VCodecQueryMemType                       VCodecDrvQueryMemType                                 
#define  SP5_VCodecQueryPhysicalAddr                  VCodecDrvQueryPhysicalAddr                               
#define  SP5_VCodecSwitchMemType                      VCodecDrvSwitchMemType                                
#define  SP5_VCodecFlushCachedBuffer                  VCodecDrvFlushCachedBuffer                            
#define  SP5_VCodecInvalidateCachedBuffer             VCodecDrvInvalidateCachedBuffer                       
#define  SP5_VCodecFlushCachedBufferAll               VCodecDrvFlushCachedBufferAll                         
#define  SP5_VCodecInvalidateCachedBufferAll          VCodecDrvInvalidateCachedBufferAll                    
#define  SP5_VCodecFlushInvalidateCacheBufferAll      VCodecDrvFlushInvalidateCacheBufferAll                
#define  SP5_VCodecMemSet                             VCodecDrvMemSet                                       
#define  SP5_VCodecMemCopy                            VCodecDrvMemCopy                                      
#define  SP5_VCodecAssertFail                         VCodecDrvAssertFail                                   
#define  SP5_VCodecMMAP                               VCodecDrvMMAP                                         
#define  SP5_VCodecUnMMAP                             VCodecDrvUnMMAP                                       
#define  SP5_VCodecWaitISR                            VCodecDrvWaitISR                                      
#define  SP5_VCodecLockHW                             VCodecDrvLockHW                                       
#define  SP5_VCodecUnLockHW                           VCodecDrvUnLockHW                                     
#define  SP5_VcodecTraceLog0                          VCodecDrvTraceLog0                                    
#define  SP5_VcodecTraceLog1                          VCodecDrvTraceLog1                                    
#define  SP5_VcodecTraceLog2                          VCodecDrvTraceLog2                                    
#define  SP5_VcodecTraceLog4                          VCodecDrvTraceLog4                                    
#define  SP5_VcodecTraceLog8                          VCodecDrvTraceLog8                                    
#define  SP5_VdoMemAllocAligned                       VCodecDrvMemAllocAligned                              
#define  SP5_VdoMemFree                               VCodecDrvMemFree                                      
#define  SP5_VdoIntMalloc                             VCodecDrvIntMalloc                                    
#define  SP5_VdoIntFree                               VCodecDrvIntFree                                      
#define  SP5_RegSync                                  VCodecDrvRegSync                                      
#define  SP5_RegSyncWriteB                            VCodecDrvRegSyncWriteB                                
#define  SP5_RegSyncWriteW                            VCodecDrvRegSyncWriteW                                
#define  SP5_RegSyncWriteL                            VCodecDrvRegSyncWriteL      
#define  SP5_VCodecInitHWLock                         VCodecDrvInitHWLock
#define  SP5_VCodecDeInitHWLock                       VCodecDrvDeInitHWLock

VAL_RESULT_T eValDeInit(VAL_HANDLE_T *a_phHalHandle);
VAL_RESULT_T eValInit(VAL_HANDLE_T *a_phHalHandle);

void SP5_VCodecQueryMemType( IN void *pBuffer_VA,
		                      IN unsigned int u4Size,
		                      OUT VCODEC_MEMORY_TYPE_T *peMemType);
void SP5_VCodecQueryPhysicalAddr(IN void *pBuffer_VA,                           
		                         OUT void **pBufferOut_PA);
int SP5_VCodecSwitchMemType(IN void *pBuffer_VA,
		                     IN unsigned int u4Size,
		                     IN VCODEC_MEMORY_TYPE_T eMemType,
		                     OUT void **pBufferOut_VA);
void SP5_VCodecFlushCachedBuffer(IN void *pBuffer_VA,
		                         IN unsigned int u4Size);		                     
void SP5_VCodecInvalidateCachedBuffer(IN void *pBuffer_VA, 
		                              IN unsigned int u4Size);		

void SP5_VCodecFlushCachedBufferAll();
void SP5_VCodecInvalidateCachedBufferAll(); 
void SP5_VCodecFlushInvalidateCacheBufferAll();
void SP5_VCodecMemSet(IN void *pBuffer_VA, 
		                IN char cValue, 
		                IN unsigned int u4Length );    
void SP5_VCodecMemCopy(IN void *pvDest, 
		                 IN const void *pvSrc, 
		                 IN unsigned int u4Length);
void SP5_VCodecAssertFail( IN char *ptr, 
			                 IN int i4Line, 
		                     IN int i4Arg);
void SP5_VCodecMMAP(VCODEC_OAL_MMAP_T *prParam);
void SP5_VCodecUnMMAP(VCODEC_OAL_MMAP_T *prParam);
int SP5_VCodecWaitISR(VCODEC_OAL_ISR_T *prParam);	
void SP5_VCodecLockHW(VCODEC_OAL_HW_LOCK_T *prParam);
void SP5_VCodecUnLockHW(VCODEC_OAL_HW_LOCK_T *prParam);
void SP5_VcodecTraceLog0(IN VCODEC_LOG_GROUP_T eGroup, 
		                  IN VCODEC_LOG_INDEX_T eIndex
						   );
void SP5_VcodecTraceLog1(IN VCODEC_LOG_GROUP_T eGroup, 
		                  IN VCODEC_LOG_INDEX_T eIndex, 
						    IN UINT64 arg
							);

void SP5_VcodecTraceLog2(IN VCODEC_LOG_GROUP_T eGroup,
		                  IN  VCODEC_LOG_INDEX_T eIndex,
		                  IN  UINT64 arg1,
		                  IN  UINT64 arg2
						   );

void SP5_VcodecTraceLog4(IN VCODEC_LOG_GROUP_T eGroup,
		                  IN  VCODEC_LOG_INDEX_T eIndex,
		                  IN  UINT64 arg1,
		                  IN  UINT64 arg2,IN  UINT64 arg3,
		                  IN  UINT64 arg4
						   );

void SP5_VcodecTraceLog8(IN VCODEC_LOG_GROUP_T eGroup,
		                  IN  VCODEC_LOG_INDEX_T eIndex,
		                  IN  UINT64 arg1,
		                  IN  UINT64 arg2,
		                  IN  UINT64 arg3, 
		                  IN  UINT64 arg4,
		                  IN  UINT64 arg5,
		                  IN  UINT64 arg6,
		                  IN  UINT64 arg7,
		                  IN  UINT64 arg8
						  );


void SP5_VdoMemAllocAligned(void *handle, unsigned int size, unsigned int u4AlignSize, VCODEC_MEMORY_TYPE_T cachable, VCODEC_BUFFER_T *pBuf);
void SP5_VdoMemFree(void *handle, VCODEC_BUFFER_T *pBuf);
void SP5_VdoIntMalloc(HANDLE  handle, unsigned int size, unsigned int alignedsize, VCODEC_BUFFER_T *prBuffer_adr);
void SP5_VdoIntFree(HANDLE handle, VCODEC_BUFFER_T *prBuffer_adr);
void SP5_RegSync(int type, unsigned int v, unsigned int a);


// MACRO 

//#include "mach/sync_write.h"

#define SP5_REGSYNC_WriteB(v, a) \
				mt65xx_reg_sync_writeb(v, a);

#define SP5_REGSYNC_WriteW(v, a) \
				mt65xx_reg_sync_writew(v, a);

#define SP5_REGSYNC_WriteL(v, a) \
				mt65xx_reg_sync_writel(v, a);


void SP5_RegSyncWriteB(unsigned int v, unsigned int a);
void SP5_RegSyncWriteW(unsigned int v, unsigned int a);
void SP5_RegSyncWriteL(unsigned int v, unsigned int a);