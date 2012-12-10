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

/* ***** BEGIN LICENSE BLOCK ***** 
 * Version: RCSL 1.0 and Exhibits. 
 * REALNETWORKS CONFIDENTIAL--NOT FOR DISTRIBUTION IN SOURCE CODE FORM 
 * Portions Copyright (c) 1995-2002 RealNetworks, Inc. 
 * All Rights Reserved. 
 * 
 * The contents of this file, and the files included with this file, are 
 * subject to the current version of the RealNetworks Community Source 
 * License Version 1.0 (the "RCSL"), including Attachments A though H, 
 * all available at http://www.helixcommunity.org/content/rcsl. 
 * You may also obtain the license terms directly from RealNetworks. 
 * You may not use this file except in compliance with the RCSL and 
 * its Attachments. There are no redistribution rights for the source 
 * code of this file. Please see the applicable RCSL for the rights, 
 * obligations and limitations governing use of the contents of the file. 
 * 
 * This file is part of the Helix DNA Technology. RealNetworks is the 
 * developer of the Original Code and owns the copyrights in the portions 
 * it created. 
 * 
 * This file, and the files included with this file, is distributed and made 
 * available on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER 
 * EXPRESS OR IMPLIED, AND REALNETWORKS HEREBY DISCLAIMS ALL SUCH WARRANTIES, 
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS 
 * FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. 
 * 
 * Technology Compatibility Kit Test Suite(s) Location: 
 * https://rarvcode-tck.helixcommunity.org 
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK ***** */ 

/*/////////////////////////////////////////////////////////////////////////// */
/*    RealNetworks, Inc. Confidential and Proprietary Information. */
/* */
/*    Copyright (c) 1995-2002 RealNetworks, Inc. */
/*    All Rights Reserved. */
/* */
/*    Do not redistribute. */
/* */
/*/////////////////////////////////////////////////////////////////////////// */

/*/////////////////////////////////////////////////////////////////////////// */
/*    INTEL Corporation Proprietary Information */
/* */
/*    This listing is supplied under the terms of a license */
/*    agreement with INTEL Corporation and may not be copied */
/*    nor disclosed except in accordance with the terms of */
/*    that agreement. */
/* */
/*    Copyright (c) 1995 - 1999 Intel Corporation. */
/*    All Rights Reserved. */
/* */
/*/////////////////////////////////////////////////////////////////////////// */
/* $Header: /cvsroot/rarvcode-video/codec/rv89combo/rv89combo_c/cdeclib/decdefs.h,v 1.4 2004/10/06 23:32:25 rascar Exp $ */
/*/////////////////////////////////////////////////////////////////////////// */
#ifndef DECDEFS_H__
#define DECDEFS_H__
/******input mode***/
#define _RV9_CMD_INPUT_	  
//#define _RV_ARRAY_INPUT_
//#define _RV_CATCHER_LOG_INPUT_
/****** Tool ******/
//#define MT6253
//#define MT6236
//#define MT6253E
//#define MT6276

//#define RV_RV8_SUPPORT

//#define _RV_INFO_
//#define _RV_SHOW_TIME_
//#define _RV_SHOW_IPMCREC_TIME_
//#define ShowBlockType
#define _RV_ERROR_RPT_
//#define _RV_SHOW_B_FRAME_MODE_
//#define _RV_USE_RESIZER_LC
//#define _RV_USE_RESIZER_
#define _RV_FORCE_DEBLOCKING_MODE_ 0
#define _RV_FORCE_DECODE_B_FRAME_

#if defined(_RV_FORCE_DEBLOCKING_MODE_)
	#if (_RV_FORCE_DEBLOCKING_MODE_== 0)
		#define _RV_FORCE_DEBLOCKING_MODE_Q0_
    #elif(_RV_FORCE_DEBLOCKING_MODE_== 1)
		#define _RV_FORCE_DEBLOCKING_MODE_Q1_
	#elif(_RV_FORCE_DEBLOCKING_MODE_== 2)
		#define _RV_FORCE_DEBLOCKING_MODE_Q2_
	#else
		#error unknow RV Force De-blocking Mode!!
	#endif
#endif


/****** YUV Output and CRC check ******/

//#define _RV_DOWNSCALING_
//#define _YUV_OUTPUT_
//#define _YUV_DEC_ORDER_OUTPUT_
//#define _RV_CRC_OUT_
#define _RV_CRC_CHECK_
//#define _RV_CRC_DECODER_ORDER_CHECK_
//#define _YUV_FRAME_OUTPUT_
//#define _RV_DEBUG_INFO_

/****** Frame drop ******/

//#define _RV_VAR_DIM_MODE_
//#define _RV9_SkipFrame_
//#define _RV9_ADAPTIVE_FRAME_DEBLOCKING_

#if defined(_RV_CATCHER_LOG_INPUT_)
	#define _RV_VAR_DIM_MODE_
	#define _RV9_SkipFrame_
	#define _RV9_ADAPTIVE_FRAME_DEBLOCKING_
#endif
#ifdef _RV9_ADAPTIVE_FRAME_DEBLOCKING_
	#define _RV9_ADAPTIVE_FRAME_DEBLOCKING_NOMVD_
#endif
//#define _RV9_ST_FAST_B_FRAME_
#if defined(_RV9_ST_FAST_B_FRAME_)
	#define _RV_USE_RESIZER_
	#define ST_B_FRAME_TOTAL_PIXEL 480*324
	#define LCM_WIDTH  480//640 
	#define LCM_HEIGHT 320//324
	#define _RV9_ST_FAST_B_FRAME_4tapsMC_
#define _RV_FAST_RB_
#endif

#if defined(_RV_VAR_DIM_MODE_)
	#define _RV9_ADAPTIVE_FRAME_DEBLOCKING_
	#define _RV9_SkipFrame_
	//#define _RV_USE_RESIZER_	
#endif

#if defined(_RV_USE_RESIZER_)||defined(_RV_USE_RESIZER_LC)
	#define LCM_WIDTH  480//640 
	#define LCM_HEIGHT 320//324
#endif

#if defined(_RV9_SkipFrame_)||defined(_RV9_ADAPTIVE_FRAME_DEBLOCKING_)||defined(_RV_VAR_DIM_MODE_)
	#define TOTAL_FRAME_BUFFER 32
	#define TIME_FACTOR 10       /*for simulation*/
	#define BS_BUFF_SIZ        300*1024  //byte
	#define ReBuffering

#endif

/****** Complexity config *******/
#if defined(MT6236) || defined(MT6236B)
	//#define _RV_Row_Base_Ref_MV_
#endif
//#define _RV9_RESAMPLING_
 
#define _RV9_FAST_IDCT_            /*Frast IDCT for ST version*/
#define _RV_6tapsMC_			    /* MC interpolation Luma rounding option for ST version*/
//#define _RV9_IP_DEBLOCKING_OFF_    /* Disable I/P Frame de-blocking(de-blocking buffer be removed) for ST*/
//#define _RV9_B_DEBLOCKING_OFF_     /* Disable B Frame de-blocking  (must ENABLE I/P frame deblocking first) for ST*/
//#define _RV_DEBLOCKING_NO_MVD

#define _RV9_MC_RND_			 /* MC interpolation Luma rounding option for LC and ST*/
#define _RV9_MC_LOOP_UNFOLD     /* MC interpolaiton loop-unfold for LC*/
	
#define _RV9_LP441_  0       
#define _RV9_LP1100_ 0
//#define _RV9_LOW_COMPLEXITY_ONLY_

#ifdef _RV9_IP_DEBLOCKING_OFF_
	#define _RV9_B_DEBLOCKING_OFF_
#endif

#define RV_ARM_OPT
#define RV_ARM_MC
#define RV_USE_DITHERING
#define RV_HW_DEBLOCK
//#define RV_ARMv6_ASM
#define RV_NEON_ASM
//#define USE_FRANK_DECODER

#ifdef ShowBlockType

	//Green
	#define MBTYPE_INTRA_COLOR            0   
	//Pink
	#define MBTYPE_INTRA_16x16_COLOR     230  



	#define MBTYPE_SKIPPED_COLOR		0xFF
	#define MBTYPE_INTER_COLOR			0xFF
	#define MBTYPE_INTER_4V_COLOR		0xFF
	#define MBTYPE_INTER_8x16V_COLOR	0xFF
	#define MBTYPE_INTER_16x8V_COLOR	0xFF
	#define MBTYPE_INTER_16x16_COLOR	0xFF
	#define MBTYPE_DIRECT_COLOR			0xFF
	#define MBTYPE_FORWARD_COLOR		0xFF
	#define MBTYPE_BACKWARD_COLOR		0xFF
	#define MBTYPE_BIDIR_COLOR			0xFF

	//Blue block
	#define FIRSTMB_COLOR_OF_IFRAME			220
	//Pink
	#define FIRSTMB_COLOR_OF_PFRAME			210

	//Green
	#define FIRSTMB_COLOR_OF_SKIP_BFRAME    10
	//Green
	#define FIRSTMB_COLOR_OF_BFRAME			10 

#endif

#ifdef _RV_SHOW_B_FRAME_MODE_
	#define BFRAME_OF_ST_FAST			10 //Green
	#define BFRAME_OF_ST  			   220 //Red
	#define BFRAME_OF_LC 				10 //Green

#endif

#endif /* DECDEFS_H__ */
