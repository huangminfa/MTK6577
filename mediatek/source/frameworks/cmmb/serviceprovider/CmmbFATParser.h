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

#ifndef _FATPARSER_H_
#define _FATPARSER_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "CmmbFATElements.h"
#include <expat.h>     /*expat-2.0.1 XML parser*/

struct CFileServiceDemuxer;
class CFatParser
{
public:
     CFatParser(CFileServiceDemuxer*);
    ~CFatParser();
    bool parse (char* data,int len);
	CFileServiceDemuxer* m_pDemuxer;
    
    enum FatParser_Status_E
    {
        parsingIdle,
        parsingFAT,
        parsingFDI,
        parsingFDIDirName,
        parsingDirID,
        parsingFAI,
        parsingTransferInfo,
        parsingTransferInfofile_name,
        parsingContentInfo,
        parsingContentInfofile_length,
        parsingContentMIME_type,
        parsingContentfile_encoding,
        parsingSegmentationInfo,
        parsingSegmentationInfoslice_length,
        parsingSegmentationInfoA_block_count,
        parsingSegmentationInfoB_block_count,
        parsingSegmentationInfototal_block_count,
        parsingSegmentationInfoslice_count_for_A,
        parsingSegmentationInfoslice_count_for_B,
        parsingSegmentationInfofec_slice_count_for_A,
        parsingSegmentationInfofec_slice_count_for_B,

        parsingFECInfoType,
        parsingFECIngoTypealgorithm,

        FatParser_Status_E_Last
    } state;

    UINT16       MinVersion; 
    UINT16       MaxVersion; 
    UINT8       depth;
    UINT8       done;

    FDIType    eFDIElement; // Temporarily Store Directory Information
    FAIType    tFAIElement; // Temporarily Store File Information

private:
    XML_Parser parser;

public:

};

#ifdef __cplusplus
}
#endif

#endif

