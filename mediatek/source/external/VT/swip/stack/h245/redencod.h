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

/***********************************************************************
        Copyright (c) 2005 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..

RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _REDENCOD_H
#define _REDENCOD_H

#include "cmH245GeneralDefs.h"

#ifdef __cplusplus
extern "C" {
#endif



typedef enum
{
  cmRedEncNonStandard=0,
  cmRedEncRtpAudio,
  cmRedEncH263Video
}cmRedundancyEncodingMethod;


typedef enum
{
  cmRoundrobin=0 ,
  cmCustom
}cmFrameToThreadMappingEnum;

typedef struct
{
  RvUint8 cmContainedThreads[256];
  RvUint8 cmContainedThreadsSize;
}cmContainedThreadsStruct;

typedef struct
{
  RvUint8                       cmNumberOfThreads;
  RvUint16                      cmFramesBetweenSyncPoints;
  cmContainedThreadsStruct      cmContainedThreads;
  cmFrameToThreadMappingEnum    cmFrameToThreadMapping;
}cmRTPH263VideoRedundancyEncoding;

typedef struct
{
  RvUint8 cmThreadNumber;
  RvUint8 cmFrameSequence[256];
  RvUint8 cmFrameSequenceSize;
}cmRTPH263RedundancyFrameMapping;


RVAPI int RVCALLCONV
cmCreateNonStandardRedMethod(   IN   HAPP            hApp,
                                IN  cmNonStandardParam *nonStandard);

RVAPI int RVCALLCONV
cmAddH263VCustomFrameMapping( IN     HAPP            hApp,
                              int nodeId,
                              cmRTPH263RedundancyFrameMapping * rtpH263RedundancyFrameMapping,
                              int rtpH263RedundancyFrameMappingSize);


RVAPI int RVCALLCONV
cmCreateRtpAudioRedMethod( IN    HAPP            hApp);

RVAPI int RVCALLCONV
cmCreateH263VideoRedMethod( IN   HAPP            hApp,
                            IN cmRTPH263VideoRedundancyEncoding * h263VRedundancyEncoding);

RVAPI int RVCALLCONV
cmGetRedundancyEncodingMethod (IN    HAPP            hApp,
                               IN int redEncMethodId,
                               OUT cmRedundancyEncodingMethod * encodingMethod);
RVAPI int RVCALLCONV
cmGetH263RedundancyEncoding (IN  HAPP            hApp,
                             IN int h263EncMethodId,
                             OUT cmRTPH263VideoRedundancyEncoding * rtpH263RedundancyEncoding);
RVAPI int RVCALLCONV
cmGetCustomFrameToThreadMapping (IN  HAPP            hApp,
                             IN int h263EncMethodId,
                             INOUT cmRTPH263RedundancyFrameMapping * rtpH263RedundancyFrameMapping,
                             INOUT int * rtpH263RedundancyFrameMappingSize );

RVAPI int RVCALLCONV
cmAddH263VCustomFrameMapping( IN     HAPP            hApp,
                              int nodeId,
                              cmRTPH263RedundancyFrameMapping * rtpH263RedundancyFrameMapping,
                              int rtpH263RedundancyFrameMappingSize);

#ifdef __cplusplus
}
#endif

#endif
