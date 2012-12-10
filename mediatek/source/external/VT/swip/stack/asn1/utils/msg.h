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
        Copyright (c) 2003 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..

RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _RV_MSG_H
#define _RV_MSG_H

#include "rvtypes.h"
#include "rverror.h"

#ifdef __cplusplus
extern "C" {
#endif


#if  0

/* Open log configuration parse it and start the listeners */
RVAPI void RVCALLCONV msOpen(void);

/* Close log listeners */
RVAPI void RVCALLCONV msClose(void);

/* Add module to debug printing list */
RVAPI int RVCALLCONV msAdd(IN const RvChar *moduleName);

/* Delete module from debug printing list */
RVAPI int RVCALLCONV msDelete(IN const RvChar *moduleName);

/* Delete all modules from debug printing list */
RVAPI int RVCALLCONV msDeleteAll(void);

/* Set the configuration file name. */
RVAPI void RVCALLCONV msFile(IN const RvChar *name);

/* Get the debug level */
RVAPI int RVCALLCONV msGetDebugLevel(void);

/* Set the log output file name.  */
RVAPI void RVCALLCONV msLogFile(IN const RvChar *name);

/* Get the current log output file name.  */
RVAPI RvStatus RVCALLCONV msGetLogFilename(IN RvUint32 nameLength, OUT RvChar *name);

/* Set the debug level */
RVAPI int RVCALLCONV msSetDebugLevel(IN int debugLevel);

/* Set function to be called when logging messages are being written */
RVAPI void RVCALLCONV msSetStackNotify(void (*sN)(IN char *line,...));

/* Add sink */
RVAPI int RVCALLCONV msSinkAdd(IN const RvChar *sinkName);

/* Delete sink */
RVAPI int RVCALLCONV msSinkDelete(IN const RvChar *sinkName);


#else

#define msOpen()
#define msClose()
#define msAdd(_moduleName) RV_ERROR_NOTSUPPORTED
#define msDelete(_moduleName) RV_ERROR_NOTSUPPORTED
#define msDeleteAll() RV_ERROR_NOTSUPPORTED
#define msFile(_name)
#define msGetDebugLevel() 3
#define msLogFile(_name)
#define msGetLogFilename(_nameLength, _name) RV_ERROR_NOTSUPPORTED
#define msSetDebugLevel(_debugLevel) 0
#define msSetStackNotify(_sN)
#define msSinkAdd(_sinkName) RV_ERROR_NOTSUPPORTED
#define msSinkDelete(_sinkName) RV_ERROR_NOTSUPPORTED


#endif  /* RV_LOGMASK */

#ifdef __cplusplus
}
#endif

#endif  /* _RV_MSG_H */
