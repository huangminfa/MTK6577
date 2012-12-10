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
Filename   : rvccore.h
Description: ccore main header file
             This library provides functions which require OS dependent implementations.
Note:        Build related configuration settings are done via the global default.mak file.
             Specific configurations for this library are done in the rvccoreconfig.h file
			 and use options defined by the rvccoredefs.h file.
************************************************************************
      Copyright (c) 2001,2002 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/

#ifndef RV_CCORE_H
#define RV_CCORE_H

#include "rvconfig.h"
#include "rvtypes.h"
#include "rverror.h"

#if defined(__cplusplus)
extern "C" {
#endif 

/* Prototypes: See documentation blocks below for details. */

/********************************************************************************************
 * RvCCoreInit
 * Initializes  all of the modules in the ccore library. Must be 
 * called before any other functions in this library are called.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : RV_OK if successful otherwise an error code.
 * REMARKS :
 * This function is not reentrant and it must not be called simultaneously
 * from multiple threads.
 * Further calls to this function will do nothing except keep track of the
 * number of times it has been called so that RvCCoreEnd will not actually
 * shut down until it has been called the same number of times.
 */
RvStatus RvCCoreInit(void);

/********************************************************************************************
 * RvCCoreEnd
 * Shuts down the all of the modules in the ccore library. No further
 * calls to any other functions in this library may be made after this
 * function is called.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : RV_OK if successful otherwise an error code.
 * REMARKS :
 * This function is not reentrant and it must not be called simultaneously
 * from multiple threads.
 * The shut down will not actually be performed until it has been called
 * the same number of times that RvCCoreInit was called.
 */
RvStatus RvCCoreEnd(void);

/********************************************************************************************
 * RvCCoreVersion
 * returns a pointer to a the version string for the ccore library.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : a pointer to a the version string for the ccore library.
 */
RVCOREAPI const RvChar * RVCALLCONV RvCCoreVersion(void);

/********************************************************************************************
 * RvCCoreInitialized
 * return a boolean answer, if ccore module has been already initialized.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : TRUE if ccore was already initialized, False otherwise,
 */
RVCOREAPI RvBool RVCALLCONV RvCCoreInitialized(void);

/********************************************************************************************
 * RvCCoreInterfaces
 * Get the compilation flags used with the common core.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : NULL terminated strings with the compilation flags used when the common core
 *           was compiled.
 * REMARKS :
 * Last string in this list is of length 0, so it's easy to know when to stop.
 * First 4 letters on each line are used for the Unix "what" utility, so you can skip them.
 */
RVCOREAPI const RvChar * RVCALLCONV RvCCoreInterfaces(void);

/********************************************************************************************
 * RvCCoreWhat
 * Prints out the compilation string.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : None.
 */
RVCOREAPI void RVCALLCONV RvCCoreWhat(void);


#if (RV_OS_TYPE == RV_OS_TYPE_WIN32)
/********************************************************************************************
 * RvWindowsVersion
 * Get Windows version of the platform we are running on.
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : Windows version: RV_OS_WIN32_NT4, RV_OS_WIN32_2000 or RV_OS_WIN32_XP.
 */
RVCOREAPI RvUint RVCALLCONV RvWindowsVersion(void);
#endif

#if defined(__cplusplus)
}
#endif 

#endif /* RV_CCORE_H */
