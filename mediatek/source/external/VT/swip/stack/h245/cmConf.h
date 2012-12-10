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
        Copyright (c) 2002 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..

RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/



#ifndef _CONFH_
#define _CONFH_

#include "pvaltree.h"


#ifdef __cplusplus
extern "C" {
#endif


typedef enum
{
    confNonStandard=1,
    confNullData,
    confVideoData,
    confAudioData,
    confData,

    /* Not supported data types */
    confEncryptionData,
    confH235Control,
    confH235Media,
    confMultiplexedStream,
    confRedundancyEncoding,
    confMultiplePayloadStream,
    confFec
} confDataType;



int /* RV_TRUE or negative value on failure */
confLoad(
     /* Load configuration parameters */
     IN  HPVT hVal,
     IN  int confRootId,
     IN  char *prefix /* to configuration parameters */
     );

int /* RV_TRUE or negative value on failure */
confGetDataTypeName(
            /* Generate dataName using field name as in H.245 standard. */
            IN  HPVT hVal,
            IN  int dataTypeId, /* Data type node id */
            IN  int dataNameSize,
            OUT char *dataName, /* copied into user allocate space [128 chars] */
            OUT confDataType* type, /* of data */
            OUT RvInt32* typeId /* node id of media type */
            );

int
confGetDataType(
        /* Search channel name in channels conf. and create appropriate dataType tree */
        IN  HPVT hVal,
        IN  int confRootId,
        IN  const RvChar* channelName, /* in channels conf */
        OUT int dataTypeId, /* node id: user supplied */
        OUT RvBool* isDynamicPayload, /* true if dynamic */
        RvBool nonH235 /*If true means remove h235Media.mediaType level */
        );

int /* real number of channels or negative value on failure */
confGetChannelNames(
            /* build array of channels names as in configuration */
            IN  HPVT hVal,
            IN  int confRootId,
            IN  int nameArSize, /* number of elements in nameArray */
            IN  int nameLength, /* length of each name in array */
            OUT char** nameArray /* user allocated array of strings */
            );


int /* RV_TRUE if found. negative value if not found */
confGetModeEntry(
         /* Search mode name in configuration. */
         IN  HPVT hVal,
         IN  int confRootId,
         IN  char *modeName, /* in conf. */
         OUT RvInt32 *entryId /* mode entry id */
         );

#ifdef __cplusplus
}
#endif

#endif



