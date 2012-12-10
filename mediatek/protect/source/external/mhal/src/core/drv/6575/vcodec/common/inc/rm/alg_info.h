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

#ifndef __ALG_INFO_H
#define __ALG_INFO_H

/*****************************************************************************
 * alg_info.h
 * The file defines common algorithm context.
 *
 *
 *****************************************************************************/

#include "decdefs.h"
#include "strm_iem.h"

/*******************************************************************************
 * VDEC framework resources
 *******************************************************************************/
// Global buffer

#define	RV9DEC_MIN_WIDTH	16
#define	RV9DEC_MIN_HEIGHT	16


#define MAX_NUM_VDEC_HANDLES        1

#define NUM_OF_VDEC_QUICK_REFS      10//6
/* Internal memory  */
/* 96			k bytes for MT6236 */
/* 37.5 + 3	k bytes for MT6253 D/T */

#if defined(MT6236) || defined(MT6236B)
	#define MAX_VDEC_CONTEXT_SIZE		( 7			* 1024)
	#define MAX_VDEC_SCRATCH_L1_SIZE	((96-7)		* 1024)

	#define MAX_VDEC_SCRATCH_L20_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_L21_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_EXT_SIZE	(3423		* 1024)

	#define VDEC_CONTEXT_TYPE		INTERNAL_MEM_1
	#define VDEC_SCRATCH_L1_TYPE	INTERNAL_MEM_1
	#define VDEC_SCRATCH_L20_TYPE	INTERNAL_MEM_1
	#define VDEC_SCRATCH_L21_TYPE	INTERNAL_MEM_1
	#define VDEC_SCRATCH_EXT_TYPE	EXTERNAL_MEM_1
#elif (defined(MT6253T)||defined(MT6253))
	#define MAX_VDEC_CONTEXT_SIZE		( 8			* 1024)
	#define MAX_VDEC_SCRATCH_L1_SIZE	((37-8)		* 1024) //+ (100*1024)

	#define MAX_VDEC_SCRATCH_L20_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_L21_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_EXT_SIZE	(3423		* 1024)
	#define VDEC_CONTEXT_TYPE		INTERNAL_MEM_TCM_1
	#define VDEC_SCRATCH_L1_TYPE	INTERNAL_MEM_TCM_1
	#define VDEC_SCRATCH_L20_TYPE	INTERNAL_MEM_TCM_1
	#define VDEC_SCRATCH_L21_TYPE	INTERNAL_MEM_TCM_1
	#define VDEC_SCRATCH_EXT_TYPE	EXTERNAL_MEM_1
#elif (defined(MT6253E)||defined(MT6253L)|| defined(MT6252H) || defined(MT6252)||defined(MT6276))
	#define MAX_VDEC_CONTEXT_SIZE		( 8			* 1024)
	#define MAX_VDEC_SCRATCH_L1_SIZE	((37-8)		* 1024) //+ (100*1024)

	#define MAX_VDEC_SCRATCH_L20_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_L21_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_EXT_SIZE	(3423		* 1024)

	#define VDEC_CONTEXT_TYPE		EXTERNAL_MEM_1
	#define VDEC_SCRATCH_L1_TYPE	EXTERNAL_MEM_1
	#define VDEC_SCRATCH_L20_TYPE	EXTERNAL_MEM_1
	#define VDEC_SCRATCH_L21_TYPE	EXTERNAL_MEM_1
	#define VDEC_SCRATCH_EXT_TYPE	EXTERNAL_MEM_1
#elif defined(MT6575) || defined(MT6577)
	#define MAX_VDEC_CONTEXT_SIZE		( 8			* 1024)
	#define MAX_VDEC_SCRATCH_L1_SIZE	((37-8)		* 1024) //+ (100*1024)
    #if defined(RV_HW_DEBLOCK)
	#define MAX_VDEC_SCRATCH_L20_SIZE	( 1920*128/16 + 128)
    #else // HW
	#define MAX_VDEC_SCRATCH_L20_SIZE	( 0)
	#endif // HW
	#define MAX_VDEC_SCRATCH_L21_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_EXT_SIZE	(3423		* 1024)

	#define VDEC_CONTEXT_TYPE		EXTERNAL_MEM_1
	#define VDEC_SCRATCH_L1_TYPE	EXTERNAL_MEM_1
    #if defined(RV_HW_DEBLOCK)
	#define VDEC_SCRATCH_L20_TYPE	INTERNAL_MEM_1
    #else // HW
	#define VDEC_SCRATCH_L20_TYPE	EXTERNAL_MEM_1
	#endif // HW
	#define VDEC_SCRATCH_L21_TYPE	EXTERNAL_MEM_1
	#define VDEC_SCRATCH_EXT_TYPE	EXTERNAL_MEM_1
#else
	#define MAX_VDEC_CONTEXT_SIZE		( 8			* 1024)
	#define MAX_VDEC_SCRATCH_L1_SIZE	((96-8)		* 1024)

	#define MAX_VDEC_SCRATCH_L20_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_L21_SIZE	( 0)
	#define MAX_VDEC_SCRATCH_EXT_SIZE	(3423		* 1024)
#endif






//extern U32 vdec_quick_ref[NUM_OF_VDEC_QUICK_REFS];



/********************************************************************
 * Two pointer pointed to base address of CNTX and SC0
 * Read only variable, should not to be a lvalue
 *******************************************************************/
//#define VDEC_CUR_CNTX_P       ((alg_cntx_hdr_st *)vdec_quick_ref[0])

//#define VDEC_CUR_SC0_L1_P     ((void *)vdec_quick_ref[1])


/********************************************************************
 * Four 32-bit user data can be used as variable or pointer
 * Read/Write
 *******************************************************************/
//#define VDEC_CUR_USR0         (vdec_quick_ref[2])

//#define VDEC_CUR_USR1         (vdec_quick_ref[3])

//#define VDEC_CUR_USR2         (vdec_quick_ref[4])

//#define VDEC_CUR_USR3         (vdec_quick_ref[5])

//#define VDEC_CUR_USR4         (vdec_quick_ref[6])

//#define VDEC_CUR_USR5         (vdec_quick_ref[7])

//#define VDEC_CUR_USR6         (vdec_quick_ref[8])

//#define VDEC_CUR_USR7         (vdec_quick_ref[9])
/********************************************************************
 * Four base address of SC1, SC2, SC3 and SC4
 * Read only constant, can't be a lvalue
 *******************************************************************/
//#define VDEC_CUR_SC1_L1       ((void *)vdec_scratch_L1)

//#define VDEC_CUR_SC2_L20      ((void *)vdec_scratch_L20)

//#define VDEC_CUR_SC3_L21      ((void *)vdec_scratch_L21)

//#define VDEC_CUR_SC4_EXT      ((void *)vdec_scratch_EXT)


#endif

