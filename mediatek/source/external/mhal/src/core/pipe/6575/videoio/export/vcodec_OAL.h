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

#ifndef VCODEC_OAL_H
#define VCODEC_OAL_H
#define IN
#define OUT

#ifndef NULL 
 #define NULL 0
#endif

#include "vcodec_if.h"
#include "vcodec_log.h"

#define VCODEC_ASSERT(expr, a) do {if(!(expr)) rVCODEC_OAL_Function.VCodecAssertFail(__FILE__, __LINE__, a); } while (0)

void VCodecQueryMemType(IN void            *pBuffer_VA,
                        IN unsigned int    u4Size,
                        OUT VCODEC_MEMORY_TYPE_T *peMemType
                        );

void VCodecQueryPhysicalAddr(IN void       *pBuffer_VA,
                          OUT void     **pBufferOut_PA
                            );

// VCodecSwitchMemType - return 0 if success.
//                       return -1 if failed, but pBufferOut_VA will be assigned with pBuffer_VA
int VCodecSwitchMemType(IN void            *pBuffer_VA,
                        IN unsigned int    u4Size,
                        IN VCODEC_MEMORY_TYPE_T eMemType,
                        OUT void           **pBufferOut_VA
                        );

// VCodecFlushCachedBuffer - u4Size is in byte
void VCodecFlushCachedBuffer(IN void         *pBuffer_VA,
                                 IN unsigned int u4Size
                                 );

// VCodecInvalidateCachedBuffer - u4Size is in byte                  
void VCodecInvalidateCachedBuffer(IN void         *pBuffer_VA, 
                               IN unsigned int 	u4Size							      
                              ); 

void VCodecFlushCachedBufferAll(void);

void VCodecInvalidateCachedBufferAll(void); 

void VCodecFlushInvalidateCacheBufferAll(void);

void  VCodecMemSet(IN void                *pBuffer_VA, 
                   IN char                cValue, 
                   IN unsigned int        u4Length 
                   );    
                                         
void  VCodecMemCopy(IN void *            pvDest ,
                    IN const void *            pvSrc ,
                    IN unsigned int      u4Length
                    );
typedef struct
{
    void *pBuffer_PA;                  ///< [IN]     The physical memory address
    unsigned int u4MemSize;      ///< [IN]     The memory size to be mapped
    void *pBuffer_VA;               ///< [OUT]     The mapped virtual memory address
} VCODEC_OAL_MMAP_T;

void VCodecMMAP(VCODEC_OAL_MMAP_T *prParam);
void VCodecUnMMAP(VCODEC_OAL_MMAP_T *prParam);


typedef enum
{
    VCODEC_OAL_VDEC,
    VCODEC_OAL_VENC
} VCODEC_OAL_CODEC;


typedef struct
{
    void *pvHandle;                  ///< [IN]     The video codec handle
    void *pvIsrFunction;            ///< [IN]     The isr function
    unsigned int u4TimeoutMs;       ///< [IN]     The timeout in ms
    VCODEC_OAL_CODEC eCodec; ///< [IN]     VDEC or VENC interrupt
} VCODEC_OAL_ISR_T;

// return value: HW is completed (1) or not (0) when function return
int VCodecWaitISR(VCODEC_OAL_ISR_T *prParam);


typedef struct
{
    void *pvHandle;                               ///< [IN]     The video codec handle
    unsigned int u4TimeoutMs;             ///< [IN]     The timeout ms
} VCODEC_OAL_HW_LOCK_T;

// return value: HW is completed (1) or not (0) when function return
int VCodecLockHW(VCODEC_OAL_HW_LOCK_T *prParam);

// return value: HW is completed (1) or not (0) when function return
int VCodecUnLockHW(VCODEC_OAL_HW_LOCK_T *prParam);

typedef struct
{
    unsigned int u4ReadAddr;            /// [IN]    memory source address in VA
    unsigned int u4ReadData;            /// [OUT]   memory data 
} VCODEC_OAL_MEM_STAUTS_T;

typedef struct
{
    unsigned int u4HWIsCompleted;                         ///< [IOUT]    HW is Completed or not, set by driver & clear by codec (0: not completed or still in lock status; 1: HW is completed or in unlock status)
    unsigned int u4HWIsTimeout;                            ///< [OUT]    HW is Timeout or not, set by driver & clear by codec (0: not in timeout status; 1: HW is in timeout status)
    unsigned int u4NumOfRegister;       ///< [IN]     Number of HW register need to store;
    VCODEC_OAL_MEM_STAUTS_T *pHWStatus;  ///< [OUT]    HW status based on input address.
} VCODEC_OAL_HW_REGISTER_T;

void VCodecInitHWLock(VCODEC_OAL_HW_REGISTER_T *prParam);

void VCodecDeInitHWLock(VCODEC_OAL_HW_REGISTER_T *prParam);
typedef struct
{

	void (*VCodecQueryMemType) ( IN void            *pBuffer_VA,
		                      IN unsigned int    u4Size,
		                      OUT VCODEC_MEMORY_TYPE_T *peMemType);

	void (*VCodecQueryPhysicalAddr)(IN void       *pBuffer_VA,
		                         OUT void     **pBufferOut_PA);

// VCodecSwitchMemType - return 0 if success.
//                       return -1 if failed, but pBufferOut_VA will be assigned with pBuffer_VA
	int (*VCodecSwitchMemType)(IN void            *pBuffer_VA,
		                     IN unsigned int    u4Size,
		                     IN VCODEC_MEMORY_TYPE_T eMemType,
		                     OUT void           **pBufferOut_VA);

	// VCodecFlushCachedBuffer - u4Size is in byte                  
	void (*VCodecFlushCachedBuffer)(IN void         *pBuffer_VA,
		                         IN unsigned int u4Size);

	// VCodecInvalidateCachedBuffer - u4Size is in byte                  
	void (*VCodecInvalidateCachedBuffer)(IN void         *pBuffer_VA, 
		                              IN unsigned int 	u4Size); 

	void (*VCodecFlushCachedBufferAll)(void);

	void (*VCodecInvalidateCachedBufferAll)(void); 

	void (*VCodecFlushInvalidateCacheBufferAll)(void);

	void  (*VCodecMemSet)(IN void                *pBuffer_VA, 
		                IN char                cValue, 
		                IN unsigned int        u4Length );    

	void  (*VCodecMemCopy)(IN void *            pvDest ,
		                 IN const void *     pvSrc ,
		                 IN unsigned int      u4Length);

	void (*VCodecAssertFail) ( IN char *ptr,
			                 IN int i4Line,
		                     IN int i4Arg);

	void (*VCodecMMAP)      (VCODEC_OAL_MMAP_T *prParam);
	void (*VCodecUnMMAP)    (VCODEC_OAL_MMAP_T *prParam	);
	int (*VCodecWaitISR)   (VCODEC_OAL_ISR_T *prParam);
	int (*VCodecLockHW)    (VCODEC_OAL_HW_LOCK_T *prParam);
	int (*VCodecUnLockHW)   (VCODEC_OAL_HW_LOCK_T *prParam);


	void (*VcodecTraceLog0)(IN VCODEC_LOG_GROUP_T eGroup, 
		                  IN VCODEC_LOG_INDEX_T eIndex
						   );

	void (*VcodecTraceLog1)(IN VCODEC_LOG_GROUP_T eGroup, 
		                  IN VCODEC_LOG_INDEX_T eIndex, 
						    IN UINT64 arg
							);

	void (*VcodecTraceLog2)(IN VCODEC_LOG_GROUP_T eGroup,
		                  IN  VCODEC_LOG_INDEX_T eIndex,
		                  IN  UINT64 arg1,
		                  IN  UINT64 arg2
						   );

	void (*VcodecTraceLog4)(IN VCODEC_LOG_GROUP_T eGroup,
		                  IN  VCODEC_LOG_INDEX_T eIndex,
		                  IN  UINT64 arg1,
		                  IN  UINT64 arg2,  
						    IN  UINT64 arg3,
		                  IN  UINT64 arg4
						   );

	void (*VcodecTraceLog8)(IN VCODEC_LOG_GROUP_T eGroup,
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


	void (*VCodecInitHWLock)(VCODEC_OAL_HW_REGISTER_T *prParam);

	void (*VCodecDeInitHWLock)(VCODEC_OAL_HW_REGISTER_T *prParam);

} VCODEC_OAL_CALLBACK_T; 

extern VCODEC_OAL_CALLBACK_T rVCODEC_OAL_Function;

int VCodecOALInit(IN VCODEC_OAL_CALLBACK_T *prVCODEC_OAL_Function);

#endif /* VCODEC_OAL_H */