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
 * Source last modified: $Id: rv_format_info.h,v 1.1.1.1.2.1 2005/05/04 18:20:57 hubbe Exp $
 * 
 * REALNETWORKS CONFIDENTIAL--NOT FOR DISTRIBUTION IN SOURCE CODE FORM
 * Portions Copyright (c) 1995-2005 RealNetworks, Inc.
 * All Rights Reserved.
 * 
 * The contents of this file, and the files included with this file,
 * are subject to the current version of the Real Format Source Code
 * Porting and Optimization License, available at
 * https://helixcommunity.org/2005/license/realformatsource (unless
 * RealNetworks otherwise expressly agrees in writing that you are
 * subject to a different license).  You may also obtain the license
 * terms directly from RealNetworks.  You may not use this file except
 * in compliance with the Real Format Source Code Porting and
 * Optimization License. There are no redistribution rights for the
 * source code of this file. Please see the Real Format Source Code
 * Porting and Optimization License for the rights, obligations and
 * limitations governing use of the contents of the file.
 * 
 * RealNetworks is the developer of the Original Code and owns the
 * copyrights in the portions it created.
 * 
 * This file, and the files included with this file, is distributed and
 * made available on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, AND REALNETWORKS HEREBY DISCLAIMS ALL
 * SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT
 * OR NON-INFRINGEMENT.
 * 
 * Technology Compatibility Kit Test Suite(s) Location:
 * https://rarvcode-tck.helixcommunity.org
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK ***** */

#ifndef RV_FORMAT_INFO_H
#define RV_FORMAT_INFO_H

#ifdef __cplusplus
extern "C" {
#endif  /* #ifdef __cplusplus */

#include "rvtypes.h"

typedef struct rv_codec_opaque_data
{
	U32	uSPO_extra_flag;
	U32	ubitstream_version;
	U32 uEncodeSize[7];         // RV 9 and RV 10 only;
} rv_codec_opaque_data;


typedef struct rv_format_info_struct
{
    U32   ulLength;
    U32   ulMOFTag;
    U32   ulSubMOFTag;
// Morris Yang 20110411 [
    U16   ulWidth;
    U16   ulHeight;
    U16   ulBitCount;
    U16   ulPadWidth;
    U16   ulPadHeight;
//] 
    U32   ufFramesPerSecond;
    rv_codec_opaque_data    pOpaqueData;
} rv_format_info;


/*
 * RV frame struct.
 */
typedef struct rv_segment_struct
{
    RV_Boolean bIsValid;
    U32 ulOffset;
} rv_segment;

typedef struct rv_frame_struct
{
    U32             ulDataLen;
    U32             ulTimestamp;
// Morris Yang 20110411 [
    U16             usSequenceNum;
    U16             usFlags;
// ]
    RV_Boolean       bLastPacket;
    U32             ulNumSegments;
    rv_segment*      pSegment;
} rv_frame;



/*
 * Macro: HX_GET_MAJOR_VERSION()
 *
 * Given the encoded product version,
 * returns the major version number.
 */
#define HX_GET_MAJOR_VERSION(prodVer)   ((prodVer >> 28) & 0xF)

/*
 * Macro: HX_GET_MINOR_VERSION()
 *
 * Given the encoded product version,
 * returns the minor version number.
 */
#define HX_GET_MINOR_VERSION(prodVer)   ((prodVer >> 20) & 0xFF)   

    
#define BYTE_SWAP_UINT16(A)  ((((U16)(A) & 0xff00) >> 8) | \
                              (((U16)(A) & 0x00ff) << 8))
#define BYTE_SWAP_UINT32(A)  ((((U32)(A) & 0xff000000) >> 24) | \
                              (((U32)(A) & 0x00ff0000) >> 8)  | \
                              (((U32)(A) & 0x0000ff00) << 8)  | \
                              (((U32)(A) & 0x000000ff) << 24))                            

#ifdef __cplusplus
}
#endif  /* #ifdef __cplusplus */

#endif /* #ifndef RV_FORMAT_INFO_H */
