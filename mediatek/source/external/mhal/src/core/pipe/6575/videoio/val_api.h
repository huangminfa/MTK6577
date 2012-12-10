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

/** 
 * @file 
 *   val_api.h 
 *
 * @par Project:
 *   MT6575 
 *
 * @par Description:
 *   Video Abstraction Layer APIs
 *
 * @par Author:
 *   Jackal Chen (mtk02532)
 *
 * @par $Revision: #1 $
 * @par $Modtime:$
 * @par $Log:$
 *
 */

#ifndef _VAL_API_H_
#define _VAL_API_H_

#include "val_types.h"

#ifdef __cplusplus
extern "C" {
#endif

/*=============================================================================
 *                             Function Declaration
 *===========================================================================*/

VAL_UINT32_T eVideoDecInitMVA(VAL_VOID_T** a_pvHandle);
VAL_UINT32_T eVideoDecAllocMVA(VAL_VOID_T* a_pvHandle, VAL_UINT32_T a_u4Va, VAL_UINT32_T* ap_u4Pa, VAL_UINT32_T a_u4Size);
VAL_UINT32_T eVideoDecFreeMVA(VAL_VOID_T* a_pvHandle, VAL_UINT32_T a_u4Va, VAL_UINT32_T a_u4Pa, VAL_UINT32_T a_u4Size);
VAL_UINT32_T eVideoDecDeInitMVA(VAL_VOID_T* a_pvHandle);

/**
* @par Function       
*   eVideoMp4EncAllocMVA
* @par Description    
*   alloc m4u modified memory for mpeg4 encoder
* @param              
*   a_u4Va              [IN]        The virtual address of malloc memory 
* @param              
*   a_u4Pa              [IN/OUT]    The modified virtual address of malloc memory
* @param             
*   a_u4Size            [IN]        The size of virtual address of malloc memory
* @par Returns        
*   VAL_RESULT_T
*/
VAL_UINT32_T eVideoMp4EncAllocMVA(
    VAL_VOID_T** a_pvHandle,
    VAL_UINT32_T a_u4Va, 
    VAL_UINT32_T* ap_u4Pa, 
    VAL_UINT32_T a_u4Size
);

/**
* @par Function       
*   eVideoMp4EncFreeMVA
* @par Description    
*   alloc m4u modified memory for mpeg4 encoder
* @param              
*   a_u4Va              [IN]        The virtual address of malloc memory 
* @param              
*   a_u4Pa              [IN]        The modified virtual address of malloc memory
* @param             
*   a_u4Size            [IN]        The size of virtual address of malloc memory
* @par Returns        
*   VAL_RESULT_T
*/
VAL_UINT32_T eVideoMp4EncFreeMVA(
    VAL_VOID_T* a_pvHandle,
    VAL_UINT32_T a_u4Va, 
    VAL_UINT32_T ap_u4Pa, 
    VAL_UINT32_T a_u4Size
);

/**
* @par Function       
*   eVideoMemAlloc
* @par Description    
*   alloc memory for video codec driver
* @param              
*   a_prParam           [IN/OUT]    The VAL_MEMORY_T structure
* @param             
*   a_u4ParamSize       [IN]        The size of VAL_MEMORY_T
* @par Returns        
*   VAL_RESULT_T
*/
VAL_RESULT_T eVideoMemAlloc(
    VAL_MEMORY_T *a_prParam, 
    VAL_UINT32_T a_u4ParamSize
);

/**
* @par Function       
*   eVideoMemFree
* @par Description    
*   free memory for video codec driver 
* @param              
*   a_prParam           [IN/OUT]    The VAL_MEMORY_T structure
* @param              
*   a_u4ParamSize       [IN]        The size of VAL_MEMORY_T
* @par Returns        
*   VAL_RESULT_T
*/
VAL_RESULT_T eVideoMemFree(
    VAL_MEMORY_T *a_prParam, 
    VAL_UINT32_T a_u4ParamSize
);

/**
* @par Function       
*   eVideoMemSet
* @par Description    
*   set a specific value to a range of memory 
* @param              
*   a_prParam           [IN]        The VAL_MEMORY_T structure
* @param              
*   a_u4ParamSize       [IN]        The size of a_prParam
* @param              
*   a_u4Value           [IN]        The specific value
* @param              
*   a_u4Size            [IN]        The range of memory
* @par Returns        
*   VAL_RESULT_T
*/
VAL_RESULT_T eVideoMemSet(
    VAL_MEMORY_T *a_prParam,
    VAL_UINT32_T a_u4ParamSize,
    VAL_INT32_T a_u4Value,
    VAL_UINT32_T a_u4Size
);

/**
* @par Function       
*   eVideoMemCpy
* @par Description    
*   copy a range of memory from src memory to dst memory 
* @param              
*   a_prParamDst        [IN]        The VAL_MEMORY_T structure
* @param              
*   a_u4ParamDstSize    [IN]        The size of a_prParamDst
* @param              
*   a_prParamSrc        [IN]        The VAL_MEMORY_T structure
* @param              
*   a_u4ParamSrcSize    [IN]        The size of a_prParamSrc
* @param              
*   a_u4Size            [IN]        The range of memory
* @par Returns        
*   VAL_RESULT_T
*/
VAL_RESULT_T eVideoMemCpy(
    VAL_MEMORY_T *a_prParamDst,
    VAL_UINT32_T a_u4ParamDstSize,
    VAL_MEMORY_T *a_prParamSrc,
    VAL_UINT32_T a_u4ParamSrcSize,
    VAL_UINT32_T a_u4Size
);

VAL_RESULT_T eVideoInitLockHW( VAL_VCODEC_OAL_HW_REGISTER_T *prParam, int size);
VAL_RESULT_T eVideoDeInitLockHW( VAL_VCODEC_OAL_HW_REGISTER_T *prParam, int size);
VAL_RESULT_T eVideoVcodecSetThreadID (VAL_VCODEC_THREAD_ID_T *a_prThreadID);

#ifdef __cplusplus
}
#endif

#endif // #ifndef _VAL_API_H_
