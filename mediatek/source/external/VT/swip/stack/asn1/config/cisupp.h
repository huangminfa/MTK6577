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

/***************************************************************************

  cisupp.h  --  CI helper functions interface

****************************************************************************/

#ifndef __CISUPP_H
#define __CISUPP_H

#include "rtree.h"
#include "rpool.h"
#include "rvmutex.h"

#ifdef __cplusplus
extern "C" {
#endif

#define CI_BITSTR_ID        "!CIBITSTR!"
#define CI_BITSTR_ID_LEN    10

typedef struct
{
    HRPOOLELEM  name; /* Name of this configuration parameter */
    RvBool      isString; /* RV_TRUE if the value is a string type */
    RvInt32     value; /* length of 'str' or integer value */
    HRPOOLELEM  str; /* Value if it's a string value */
} cfgValue, *pcfgValue;


/* HCFG struct */
typedef struct
{
    HRTREE      tree;
    HRPOOL      pool;
    RvLogMgr    *logMgr; /* Log manager holding this configuration handle */
    RvLogSource log; /* CI log source for errors */
    RvBool      isMutexConstructed;
    RvMutex     mutex; /* Mutex preserving this object */
} cfgHandle;



#define CONFIG_RPOOL_BLOCK_SIZE 32
#define MAX_CONFIG_TEMP_BUFFER_SIZE 512


/* helper functions to identify a data source (i.e. file, registry) */
typedef int (ciIDFunc)      (const char *source);
/* helper functions to estimate the size of the data in the data source  */
typedef int (ciEstimateFunc)(cfgHandle *cfg, const char *source, int *nodes, int *data);
/* helper functions to load the data */
typedef int (ciBuildFunc)   (cfgHandle *cfg, const char *source, HRTREE tree, HRPOOL pool);
/* helper functions to save the data */
typedef int (ciOutputFunc)  (const char *target, HRTREE tree, HRPOOL pool);


                /* == interface to load/save functions == */

/* estimate minimum amounts of: */
void
ciEstimateCfgSize(
                  cfgHandle *cfg,
                  const char *source,  /* configuration source identifier */
                  int *nodes,          /* number of nodes */
                  int *data            /* size of all data */
                  );

/* bulid a configuration R-Tree from the source */
HRTREE
ciBuildRTree(cfgHandle *cfg,
             const char *source,        /* configuration source identifier */
             int nodes,                 /* number of nodes from estimate */
             HRPOOL pool);              /* preallocated pool for data */

/* output the configuration to the target */
int
ciOutputRTree(const char *target,      /* target for tree */
              HRTREE tree,             /* configuration tree*/
              HRPOOL pool              /* preallocated pool for data */
              );


/* == bit string support == */

/* builds an internal representation of a bit string */
int /* returns: the length of the output string */
ciBuildBitString(const char *str,      /* the bit string buffer */
                 int bits,             /* the number of bits in the buffer */
                 OUT char *bitstr      /* output buffer - can be NULL */
                 );

/* checks if a string is a bit string buffer */
int /* returns: <0 - not bit string, otherwise number of bits */
ciIsBitString(const char *str,         /* string to check */
              int length               /* length of string to check */
              );

/* returns the data section inside the bit string buffer */
const char *ciGetBitStringData(const char *str);


#ifdef __cplusplus
}
#endif

#endif /* __CISUPP_H */


