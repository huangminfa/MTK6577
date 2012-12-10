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

/* Resource counting module
 * Internal CC module
 * Supports counting various resources such as locks, semaphores, mutexes
 * 
 * The only API function that should be used from outside CC is 
 * RvResourceCounterReportToString
 *
 * For each countable resource reports
 *  1. Total number of objects constructed
 *  2. Maximal number constructed objects simultaneously
 *  3. Average (over time) number of objects
 *
 */

#include "rvtypes.h"
#include "rvtimestamp.h"
#include "rvlog.h"



#if RV_USE_RESOURCE_COUNTING

#define MAX_RESOURCE_NAME_SIZE 32

typedef struct _RvResourceCounter {
    RvLock   lock;                     /* per-counter lock */
	struct _RvResourceCounter *next;   /* pointer to the next counter */
    const RvChar   *name;              /* name of counted resource ("Lock", "Mutex", etc) */
	RvUint32  nConstructed;            /* total number of constructed objects */
	RvUint64  startTime;               /* counting start time (time of resource counter 
                                        *  initialization)
                                        */
	RvUint64  lastTime;                /* time of the last counting operation */
	RvUint64  nAverage;                /* Average * Time */
	RvUint32  nMax;                    /* current maximum */
       /*[2010.4.15][Meggie][VCT-8B alignment]Structure adjustment */
       RvUint32  nCurrent;                /* current number of constructed objects */
} RvResourceCounter;


/******************************************************************************
 * RvResourceCounterConstruct(RvResourceCounter *self, RvChar *name)
 *  Construct resource counter pointed by 'self' named 'name'
 *  Internal function. 
 * 
 * Note:
 *  'self' should point to statically allocated object
 *  'name' should be statically allocated string (literal, for example)
 *
 */
void RvResourceCounterConstruct(RvResourceCounter *self, const RvChar *name);

/******************************************************************************
 * RvOnConstructResource(RvResourceCounter *self)
 * Should be called on counted object construction
 * Internal function
 *
 */
void RvOnConstructResource(RvResourceCounter *self);

/******************************************************************************
 * RvOnDestructResource(RvResourceCounter *self)
 * Should be called on counted object destruction
 * Internal function
 *
 */
void RvOnDestructResource(RvResourceCounter *self);

/******************************************************************************
 * RvResourceStartCount()
 *
 * Starts resource counting
 * Internal function
 */
void RvResourceStartCount(void);

void RvResourceEndCount(void);


/******************************************************************************
 * RvResourceCounterReportToString(RvChar *buffer, RvSize_t bufLen);
 * 
 * Reports resource usage to string in human-readable format
 *
 * buffer  - reports buffer, should be big enough for building report, otherwise report 
 *           will be truncated
 * buffLen - size of 'buffer' in bytes
 */
RVCOREAPI
void RvResourceCounterReportToString(RvChar *buffer, RvSize_t bufLen);

/* Declares resource 'name'. Should be used at file scope */
#define RV_DECL_RESOURCE(name) \
    static RvResourceCounter __gsResourceCounter__; \
    static RvChar __gsResourceName__[MAX_RESOURCE_NAME_SIZE] = #name;

/* Initiates resource counter. 
 * Should be called before any object of this kind is constructed
 * Module initialization function is a good place for this one
 */
#define RV_RESOURCE_INIT() RvResourceCounterConstruct(&__gsResourceCounter__, __gsResourceName__)
#define RV_RESOURCE_END()  

/* Increments resource counter. Should be called in object constructor */
#define RV_RESOURCE_INC()  RvOnConstructResource(&__gsResourceCounter__)

/* Decrements resource counter. Should be called in object destructor */
#define RV_RESOURCE_DEC()  RvOnDestructResource(&__gsResourceCounter__)

#define RV_RESOURCE_START_COUNT() RvResourceStartCount()

#define RV_RESOURCE_END_COUNT() RvResourceEndCount()


#else /* RV_USE_RESOURCE_COUNTING */

#define RV_DECL_RESOURCE(name) 
#define RV_RESOURCE_INIT() 
#define RV_RESOURCE_END()  
#define RV_RESOURCE_INC()  
#define RV_RESOURCE_DEC()  
#define RV_RESOURCE_START_COUNT() 
#define RV_RESOURCE_END_COUNT()


#endif

