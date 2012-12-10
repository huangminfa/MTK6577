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

#ifndef _RV_TERM_DEFS_H_
#define _RV_TERM_DEFS_H_

/***********************************************************************
termDefs.h

Definitions required for running a terminal application.

Possible definitions:
- USE_GEF           - Use Generic Extensibility Framework add-on
- USE_H245AUTOCAPS  - Use H.245 AutoCaps add-on
- USE_ISDN          - USE ISDN CAPI 2.0 API
- USE_WCDMA         - USE WCDMA AT commands API
- USE_SERIAL        - USE serial connection API
- USE_MEDIA_VIEWER  - USE codecs library

Other definitions:
- RV_H223_USE_SPLIT - Use H.223 Split add-on
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/

/* Declare the version of 3G-324M Protocol Toolkit we're using */
/* This will result the exact set of definitions we have declared */
#define RV_3G324M_VERSION_DEFINITIONS 300
#define USE_GEF
#define USE_H245AUTOCAPS

#include "Rv3G324m.h"

#ifdef USE_H245AUTOCAPS
#include "RvH245AutoCaps.h"
#endif

#ifdef USE_GEF
#include "RvGef.h"
#endif

#ifdef USE_HANDSET
#include "termCodecs.h"
#endif

#ifdef USE_MEDIA_VIEWER
#include "RvProLabMediaViewer.h"
#endif

#if (RV_H223_USE_SPLIT == RV_YES)
#include "H223Serialize.h"
#endif

#include "rvsemaphore.h"
/* The following are here for various operating systems */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>


#ifdef __cplusplus
extern "C" {
#endif
#include "VT_common_enum.h"
#include "vt_option_cfg.h"

/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/

#if (RV_H223_USE_SPLIT == RV_YES)
#define MAX_H223_STREAMING_CONNECTIONS  10
#define MAX_H223_WATCHDOG_ENTRIES       50
#endif

//#define VT_TRANSPORT_SIZE (80)
#define VT_TRANSPORT_SIZE (160)
#define VT_TRANSPORT_SIZEx2 (320)

#define VT_PACKET_SIZE 1024
#define VT_PROCESS_INTERVAL (6*KAL_TICKS_10_MSEC)
#define VT_WATCHDOG_INTERVAL (KAL_TICKS_1_SEC)
//#define VT_TRANSMIT_INTERVAL (KAL_TICKS_10_MSEC)
#define VT_TRANSMIT_INTERVAL (20)
#define VT_FASTUPDATE_INTERVAL (KAL_TICKS_1_SEC * 2)

#if (VT_SIM_MODE == SIM_STK_RX_FILE) 
#define VT_SIMULATED_INCOMING_INTERVAL (2*KAL_TICKS_10_MSEC)
#endif
#define VT_CALL_KEEPER_TIMEOUT (180*KAL_TICKS_1_SEC)

typedef struct TermObj_tag TermObj;
typedef struct TermCallObj_tag TermCallObj;
typedef struct TermChannelObj_tag TermChannelObj;


RV_DECLARE_HANDLE(HashHANDLE);

RV_DECLARE_HANDLE(BufferHANDLE);

/* Lock object. Non-recursive. */
RV_DECLARE_HANDLE(LockHANDLE);

typedef enum
{
    CallRcvMsgUnknow = -1,
    CallRcvMsgCon,           
    CallAckMsgCon,           
    CallRcvMsgDiscon,           
    CallAckMsgDiscon,
    CallRcvMsgLast
} CallRcvMsgState;

typedef enum
{
    TermResourceCall,             /* Call object */
    TermResourceChannel           /* Channel object */
} TermResourceType;



typedef enum
{
    TermCallInternal,   /* Internal call, emulated in the same process */
    TermCallNormal,
    TermCallSimulated
} TermCallType;


typedef enum
{
    wcdmaNoCall,
    wcdmaCallDialOut,
    wcdmaCallRingIn,
    wcdmaCallConnected,
    wcdmaCallDisconnecting
} TermWcdmaCallState;


typedef enum
{
    serialNoCall,
    serialCallDialOut,
    serialCallRingIn,
    serialCallConnected,
    serialCallDisconnecting
} TermSerialCallState;




/* Main thread function */
typedef int (* ThreadMainFunc) (IN TermObj **pTerm, IN void *context);





/******************************************************************************
 * termMallocEv
 * ----------------------------------------------------------------------------
 * General: Dynamic allocation function to call (the equivalent of malloc()).
 *
 * Return Value: Pointer to allocated memory on success, NULL on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         size         - Size of allocation requested.
 * Output: None.
 *****************************************************************************/
typedef void* (*termMallocEv)(
    IN TermObj              *term,
    IN RvSize_t             size);


/******************************************************************************
 * termFreeEv
 * ----------------------------------------------------------------------------
 * General: Request user to allocate a resource identifier for an object.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         ptr          - Pointer of memory to free.
 * Output: None.
 *****************************************************************************/
typedef RvStatus (*termFreeEv)(
    IN TermObj              *term,
    IN void                 *ptr);


/******************************************************************************
 * termCurrentTimeEv
 * ----------------------------------------------------------------------------
 * General: Gives the current relative time in milliseconds.
 *          This function isn't really a thread-related function, but it is
 *          OS dependent, so we put it here.
 *
 * Return Value: Current time in ms
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 * Output: None
 *****************************************************************************/
typedef RvUint32 (*termCurrentTimeEv)(
    IN TermObj              *term);


/******************************************************************************
 * termThreadCurrentEv
 * ----------------------------------------------------------------------------
 * General: Give an OS specific thread handle. This can be used to check the
 *          thread's id.
 *
 * Return Value: Current thread's unique id
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 * Output: None
 *****************************************************************************/
typedef int (*termThreadCurrentEv)(
    IN TermObj              *term);


/******************************************************************************
 * termThreadCreateEv
 * ----------------------------------------------------------------------------
 * General: Create and start the execution of a thread
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  pTerm    - Terminal object to use.
 *         func     - Function to start running in the new thread
 *         context  - Context passed to the thread function
 * Output: None
 *****************************************************************************/
typedef RvStatus (*termThreadCreateEv)(
    IN TermObj          **pTerm,
    IN ThreadMainFunc   func,
    IN void             *context);


/******************************************************************************
 * termThreadSleepEv
 * ----------------------------------------------------------------------------
 * General: Sleep for a given amount of time.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         ms           - Time to sleep in milliseconds.
 * Output: None
 *****************************************************************************/
typedef RvStatus (*termThreadSleepEv)(
    IN TermObj              *term,
    IN RvUint32             ms);


/******************************************************************************
 * termThreadTimerEv
 * ----------------------------------------------------------------------------
 * General: Wait for a given amount of time for function invocation.
 *          The function might be called from any active thread.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  pTerm    - Terminal object to use.
 *         ms       - Milliseconds to wait until function is called.
 *         func     - Function to run after the interval passes.
 *         context  - Context passed to the thread function.
 * Output: None
 *****************************************************************************/
typedef RvStatus (*termThreadTimerEv)(
    IN TermObj          **pTerm,
    IN RvUint32         ms,
    IN ThreadMainFunc   func,
    IN void             *context);


/******************************************************************************
 * termLockInitEv
 * ----------------------------------------------------------------------------
 * General: Initialize a lock. This is a non-recursive lock.
 *
 * Return Value: Lock object on success.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 * Output: None.
 *****************************************************************************/
typedef LockHANDLE (*termLockInitEv)(
    IN TermObj              *term);


/******************************************************************************
 * termLockEndEv
 * ----------------------------------------------------------------------------
 * General: Destruct a lock.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         lock         - Lock object to use.
 * Output: None.
 *****************************************************************************/
typedef RvStatus (*termLockEndEv)(
    IN TermObj              *term,
    IN LockHANDLE           lock);


/******************************************************************************
 * termLockLockEv
 * ----------------------------------------------------------------------------
 * General: Lock a lock.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         lock         - Lock object to use.
 * Output: None.
 *****************************************************************************/
typedef RvStatus (*termLockLockEv)(
    IN TermObj              *term,
    IN LockHANDLE           lock);


/******************************************************************************
 * termLockUnlockEv
 * ----------------------------------------------------------------------------
 * General: Unlock a lock.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         lock         - Lock object to use.
 * Output: None.
 *****************************************************************************/
typedef RvStatus (*termLockUnlockEv)(
    IN TermObj              *term,
    IN LockHANDLE           lock);


/******************************************************************************
 * termAllocateResourceIdEv
 * ----------------------------------------------------------------------------
 * General: Request user to allocate a resource identifier for an object.
 *
 * Return Value: Allocated resource on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term         - Terminal object to use.
 *         resourceType - Type of resource we need id for.
 * Output: None.
 *****************************************************************************/
typedef RvInt32 (*termAllocateResourceIdEv)(
    IN TermObj              *term,
    IN TermResourceType     resourceType);


/******************************************************************************
 * termAppSetTraceEv
 * ----------------------------------------------------------------------------
 * General: Sets tracing on configuration variables of active terminal.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  None.
 * Output: None.
 *****************************************************************************/
typedef void (*termAppSetTraceEv)(void);


/******************************************************************************
 * termCallNewEv
 * ----------------------------------------------------------------------------
 * General: Indication of a new call object that was created as is being used.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - The call object.
 * Output: None.
 *****************************************************************************/
typedef void (*termCallNewEv)(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallCloseEv
 * ----------------------------------------------------------------------------
 * General: Indication of a call closure.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - The call object.
 * Output: None.
 *****************************************************************************/
typedef void (*termCallCloseEv)(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallActivateEv
 * ----------------------------------------------------------------------------
 * General: Indication of a saved call being activated (High Availability feature).
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - The call object.
 * Output: None.
 *****************************************************************************/
typedef void (*termCallActivateEv)(
    IN TermCallObj          *call);


/******************************************************************************
 * termChannelNewEv
 * ----------------------------------------------------------------------------
 * General: Indication of a new channel object that was created as is being
 *          used.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel      - The channel object.
 * Output: None.
 *****************************************************************************/
typedef void (*termChannelNewEv)(
    IN TermChannelObj       *channel);

/******************************************************************************
 * termChannelNewEv
 * ----------------------------------------------------------------------------
 * General: Indication of a new MPC channel object.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  mpcChannel      - The MPC channel object.
 * Output: None.
 *****************************************************************************/
typedef void (*termMonaMPCChannelNewEv)(
    IN TermChannelObj *mpcChannel);

/******************************************************************************
 * termMessageEv
 * ----------------------------------------------------------------------------
 * General: Indication of a message being sent or received by the endpoint.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term     - Terminal object to use.
 *         call     - Call this message belongs to (NULL if none).
 *         nodeId   - Message node id.
 *         name     - Message's name.
 *         isSend   - RV_TRUE for send, RV_FALSE for receive.
 * Output: None.
 *****************************************************************************/
typedef void (*termMessageEv)(
    IN TermObj              *term,
    IN TermCallObj          *call,
    IN RvPvtNodeId          nodeId,
    IN const RvChar         *name,
    IN RvBool               isSend);


/******************************************************************************
 * termEventIndicationEv
 * ----------------------------------------------------------------------------
 * General: Indication of a message being sent or received by the endpoint.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         eventType        - Type of the event indicated.
 *         call             - Call this event belongs to (NULL if none).
 *         eventStr         - Event string.
 * Output: None.
 *****************************************************************************/
typedef void (*termEventIndicationEv)(
    IN TermObj              *term,
    IN const RvChar         *eventType,
    IN TermCallObj          *call,
    IN const RvChar         *eventStr);


/******************************************************************************
 * termLogEv
 * ----------------------------------------------------------------------------
 * General: Indication of a message that can be logged somewhere.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         call             - Call this event belongs to (NULL if none).
 *         logMessage       - Log message.
 * Output: None.
 *****************************************************************************/
typedef void (*termLogEv)(
    IN TermObj              *term,
    IN TermCallObj          *call,
    IN const RvChar         *logMessage);


/******************************************************************************
 * termFileReadEv
 * ----------------------------------------------------------------------------
 * General: Read callback to use for a simulated call.
 *
 * Return Value: Bytes read on success, negative value when done.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call             - Call object to use.
 *         fName            - File name to open. NULL if we only want to read.
 *         size             - Buffer size.
 * Output: buf              - Buffer to read the file into.
 *****************************************************************************/
typedef RvInt32 (*termFileReadEv)(
    IN  TermCallObj         *call,
    IN  const RvChar        *fName,
    IN  RvSize_t            size,
    OUT RvUint8             *buf);


/******************************************************************************
 * termChannelIndicationEv
 * ----------------------------------------------------------------------------
 * General: Indication about an incoming frame on a given channel object.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel      - Channel that received the frame.
 *         context      - Context given to application about this channel.
 *         pBuf         - Pointer to the media frame received.
 *         bufSize      - Size of the frame received.
 *         delay        - Time in milliseconds that passed since last frame
 *                        was received.
 * Output: None.
 *****************************************************************************/
typedef void (*termChannelIndicationEv)(
    IN TermChannelObj       *channel,
    IN void                 *context,
    IN RvUint8              *pBuf,
    IN RvSize_t             bufSize,
    IN RvUint32             delay);


/******************************************************************************
 * termTcpDialEv
 * ----------------------------------------------------------------------------
 * General: Dial a given call using TCP.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call object to use.
 *         destAddr     - String of the destination address to connect to.
 * Output: None.
 *****************************************************************************/

typedef RvStatus (*termTcpDialEv)(
    IN TermCallObj          *call,
    IN const RvChar         *destAddr);


/******************************************************************************
 * termTcpDropEv
 * ----------------------------------------------------------------------------
 * General: Drop a given call's TCP connection.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call object to use.
 * Output: None.
 *****************************************************************************/
typedef RvStatus (*termTcpDropEv)(
    IN TermCallObj          *call);

#if (VT_SIM_MODE == SIM_STK_RX_FILE) 
/******************************************************************************
 * termFileRead
 * ----------------------------------------------------------------------------
 * General: Read callback to use for a simulated call.
 *
 * Return Value: Bytes read on success, negative value when done.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call             - Call object to use.
 *         fName            - File name to open. NULL if we only want to read.
 *         size             - Buffer size.
 * Output: buf              - Buffer to read the file into.
 *****************************************************************************/
//typedef RvInt32 (*termFileReadEv)(
//    IN  TermCallObj         *call,
//    IN  const RvChar        *fName,
//    IN  RvSize_t                size,
//    OUT RvUint8             *buf);
#endif

/* List of this module's callbacks */
typedef struct
{
    termMallocEv                termMalloc;
    termFreeEv                  termFree;
    termCurrentTimeEv           termCurrentTime;
    //termThreadCurrentEv         termThreadCurrent;
    //termThreadCreateEv          termThreadCreate;
    //termThreadSleepEv           termThreadSleep;
    termThreadTimerEv           termThreadTimer;
    termLockInitEv              termLockInit;
    termLockEndEv               termLockEnd;
    termLockLockEv              termLockLock;
    termLockUnlockEv            termLockUnlock;
    termAllocateResourceIdEv    termAllocateResourceId;
    termCallNewEv               termCallNew;
    termCallCloseEv             termCallClose;
    termCallActivateEv          termCallActivate;
    termChannelNewEv            termChannelNew;
    termMonaMPCChannelNewEv     termMonaMPCChannelNew;
    termMessageEv               termMessage;
    //termEventIndicationEv       termEventIndication;
    termLogEv                   termLog;
#if (VT_SIM_MODE == SIM_STK_RX_FILE)   
    termFileReadEv              termFileRead;
#endif
    termChannelIndicationEv     termChannelIndication;

    //termTcpDialEv               termTcpDial;
    //termTcpDropEv               termTcpDrop;

} TermCallbacks;



/* Error patterns file struct. Used when trying to check
   incoming bit streams while applying error patterns to them. */
typedef struct
{
    RvUint8                 *pBuf; /* Errors pattern file stored in memory to use in this call */
    RvSize_t                bufSize; /* Size in bytes of the errors pattern */
    RvSize_t                offset; /* Current offset in the errors pattern */
} TermErrorPattern;

#define VT_AUDIO_MASK 1
#define VT_VIDEO_MASK 2

/* Call object */
struct TermCallObj_tag
{
    TermObj                 *term; /* Terminal's object */
    RvInt32                 id; /* Id given to the call */
    RvUint8                 randomColor[3]; /* Random color to use for this call */

    HCALL                   hsCall; /* Call handle in the CM for this call */
    TermCallObj             *pRemoteCall; /* Pointer to the call associated with this one (if we have such) */
    int                     numOfChannels; /* Number of channels opened in this call */
    TermChannelObj          *firstChannel; /* First channel opened (regarded as a linked-list) */
    TermChannelObj          *lastChannel; /* Last channel opened (regarded as a linked-list) */
    Rv3G324mCallState       state; /* Latest call state that was indicated */
    Rv3G324mCallStateMode   stateMode; /* Latest state mode that was indicated */
    TermCallType            callType; /* Type of call */
    Rv3G324mCallStateMode   syncStateMode; /* Synchronization state mode. Used for information */

    RvBool                  isIndependent; /* RV_TRUE for independent calls, not handled too much by this library */
    RvBool                  isIncoming; /* RV_TRUE if the call is an incoming call
                                           RV_FALSE if the call is an outgoing call */
    RvBool                  bCallDropped; /* RV_TRUE if we are already trying to drop the call */
    RvBool                  needToSendMuxTable; /* RV_TRUE if we're waiting that all channels we offered
                                                   get connected so we can send the mux table */
    RvBool                  waitingForMuxTableAck; /* RV_TRUE if we're waiting for an ack on our mux table */
    RvUint16                mesEntries; /* bitmask of MES entries in last incoming MES message */
    RvBool                  hadPendingChannels; /* RV_TRUE if we already handled pending channels. It is used
                                                   to send a mux table to the remote side when the remote side
                                                   sent his mux table before opening his channels */
    /* High Availability */    
    RvBool                  bCallTermCrashing;   /*RV_TRUE if call terminal is in the process of crashing*/
    RvBool                  bCallIsSaved;       /*an indication if its a saved or active call*/
    RvUint32                currentRandomValue;     /*The last random number that was given to the stack*/
	#if 0
    TermCallObj             *relatedSBCall;     /*  the related call in the stand-by stack. If not NULL,
                                                    the related call will be active except for sending data
                                                    on the isdn channel or the tcp socket, until it gets to the
                                                    Rv3G324mCallStateConnectedChannels state */

    BufferHANDLE            relatedSBBuffer;        /*The buffer that is used for the Rv3G324mCallSend in
                                                    an active stand-by call*/
    #endif
    /* Simulated/internal calls */
/*    BufferHANDLE            transportBuffer;  Transport buffer to use for this call. This is used
                                                to simulate a real network behavior. */
    void                    *simulatedContext; /* Context to use for simulated calls */

    /* ISDN calls */
    void                    *isdnHandle; /* ISDN call handle */
    RvBool                  bHardwareStuffing; /* Are we doing hardware stuffing for mux-level 0? */

    /* WCDMA calls */
    RvBool                  bWcdmaThreads; /* Should the treads keep running? */

    /* Serial calls */
    RvBool                  bSerialThreads; /* Should the treads keep running? */

    /* H.245 AutoCaps */
#ifdef USE_H245AUTOCAPS
    HAUTOCAPSCALL           hH245AutoCaps;     /* Handle to the H.245 AutoCaps call object */
#endif

    
    /* ACP */
    RvBool                  bUseACP;            /* Use ACP in call setup if supported by other side */
    RvBool                  ACPSendEnabled;  /*if RV_TRUE media can be sent on channels*/
    RvUint8                 acpAudioEntry;       /* Mux table entry number for audio in ACN */
    RvUint8                 acpVideoEntry;       /* Mux table entry number for audio in ACN */
    /* 2008/05/26 */
    RvUint8                 acpValidEntries;    /* Bit mask for mux acp audio/video entry */
                                                               /* Bit 1: True if audio acp entry has been set */
                                                               /* Bit 2: True if video acp entry has been set */    
    /* MONA */   
    RvBool                   bUseMONA;          /* Use MONA in call setup if supported by other side */         
    RvBool                   bUseMCPRxOnly;     /* Support receiving MPC channels only */
    RvBool                   bPMMsgReceived;    /* Should be set to RV_TRUE when a preference message is received */

    /*MPC*/
    //RvUint16                mpcMediaTypes;       /*A bitmask of media type of the receiving MPC channel*/
    
    /* MPC -- modified by MTK */
    RvUint16                rxMpcMediaTypes;
    RvUint16                txMpcMediaTypes;
    
    /* mpc channels */
    //TermChannelObj          *mpcAudioChannel;    
    //TermChannelObj          *mpcVideoChannel;

    /* mpc channels -- modified by MTK */
    TermChannelObj          *rxMpcAudioChannel;  
    TermChannelObj          *txMpcAudioChannel; 
    TermChannelObj          *rxMpcVideoChannel;
    TermChannelObj          *txMpcVideoChannel;
    
    /* H.223 Split call */
#if (RV_H223_USE_SPLIT == RV_YES)
    RvInt32                 idx; /* Index of the connection */
    void*                   splitCallContext;
    RvBool                  networkDisconnect;
    RvBool                  bSendMuxtable;
#endif

    /* Error patterns */
    TermErrorPattern        errorPattern; /* Error pattern to use */

#ifdef USE_MEDIA_VIEWER
    /* Media viewing library */
    MediaViewerHandleType   hMediaViewer; /* Media viewer library handle. NULL if codecs not supported */
#endif

    /* Binary stream logs */
    //FILE                    *pIncomingFile; /* Incoming data stream from remote endpoint */
//    FILE                    *pOutgoingFile; /* Outgoing data stream from local endpoint */
    LockHANDLE              filesLock; /* Mutex for writing data stream files */

    LockHANDLE              mediaDropLock; /* Lock for the media when we're dropping the call */

    LockHANDLE              lock; /* Lock protecting this struct */

    TermCallObj             *next; /* Next call with same networking */
    TermCallObj             *prev; /* Previous call with same networking */

    RvInt8 endType;
    
    //[MTK81058/20110616] Added for CRBT
    RvUint8 RemoteTerminalType;
};



/* Channel object */
struct TermChannelObj_tag
{
    TermCallObj             *call; /* Call object of this channel */
    RvInt32                 id; /* Id given to the channel */
    TermChannelObj          *nextChannel; /* Next channel that belongs to this call */
    TermChannelObj          *prevChannel; /* Previous channel that belongs to this call */

    cmChannelState_e        state; /* Channel's state */
    cmChannelStateMode_e    stateMode; /* Channel's state mode */
    RvBool                  mark; /* Used for printing the channels */
    cmChannelDataType       type; /* Type of channel */
    RvChar                  dataTypeName[30]; /* Name of the data type used in this channel */
    RvPvtNodeId             dataTypeNode; /* Data type of the channel */
    RvInt32                 frameSize; /* Size of frames/rate supported by this channel */
    HCHAN                   hsChan; /* Channel handle in the CM for this channel */
    RvBool                  isOriginChan; /* Was this channel opened by us directly? */
    RvBool                  isOutgoing; /* RV_TRUE for outgoing channels */
    RvBool                  isIndependent; /* RV_TRUE for independent calls, not handled too much by this library */
    RvBool                  isPrevStateRingBack; /*true if previous state was ring back*/
    RvBool                  bIsBidirectional; /* Is this a bidirectional channel? */
    RvBool                  bIsRtx; /* Does this bidirectional channel supports retransmissions? */
    RvBool                  bIsReverseNullData; /* if RV_TRUE the dataType value in the reverse parameter
                                                   is nullData. Meaningless otherwise*/
    RvBool                  bAllowSending; /* Defines whether the the channel is being dropped
                                              and therefore the sending should be stopped */
#if (RV_H223_USE_SPLIT == RV_YES)
    void*                   splitChannelContext; /* H.223 Split context to use for this call */
#endif

    /* Outgoing channel media parameters */
    RvUint8                 *pDataFile; /* Pointer to the file we are sending *///??Matt
    RvSize_t                dataFileSize; /* Size of the file being sent *///??Matt
    RvBool                  bInstructionsFile; /* RV_TRUE for media files that contains "instructions" *///??Matt
    RvBool                  bCyclic; /* RV_TRUE if we want to continuously send the file *///??Matt
    RvUint32                sendRate;   /* send rate (or packet size for audio) *///??Matt
    RvUint32                nextPacketTime; /* Time in ms that the next packet should be sent *///??Matt
    RvUint32                readOffset; /* Holds the reading position in the input file. For outgoing channel */ //??Matt
    RvBool                  finished;   /* RV_TRUE if we finished sending on this channel */  //??Matt
    RvBool                  bMesSent; /* Defines whether MES messages included this channel */
    RvBool                  bEntrySent; /*Defines that a MES was sent on this channel only */

    /* Incoming channel media parameters */
    RvUint32                lastPacketTime; /* Time in ms that the last packet have been received */
    RvUint16                nextSequenceNumber; /* Next sequence number we're expecting */
    RvUint16                sequenceNumberLimit; /* Highest value of the sequence number */
    void                    *context; /* Context to use for channel frame indications */

    /* Auto mimic */
    RvBool                  bLoopbackMedia; /* Are we going to loopback media sent to this channel? */
    TermChannelObj          *pMimicChannel; /* The channel associated with this channel. Used for AutoMimic and ReplayMedia */
    BufferHANDLE            mimicChannelBuffer; /* The buffer that holds the sending information of the mimicked channel */
    RvSize_t                bytesReceived; /* Bytes received on this channel */

    /* H.245 AutoCaps */
#ifdef USE_H245AUTOCAPS
    RvH245AutoCapsChannelResponse eChannelResponse;     /* The recommended response for incoming channel */
    cmRejectLcnReason             eRejectReason;        /* The recommended reject reason for incoming channel */
#endif

#ifdef USE_MEDIA_VIEWER
    RvBool                  bUseMediaViewer; /* Are we handling this channel's media? */
#endif
    /*MPC*/
    Rv3G324mCallMonaMPCType		mpcChannelType;
    HCHAN						inhsChan;   /*stack handle of incoming MPC channel*/
    HCHAN						outhsChan;  /*stack handle of outoing MPC cahnnel*/
    RvBool						MonaMPCsendEnabled; /*RV_TRUE if media send on the channel is allowed*/

#ifdef USE_HANDSET
    MediaCodecType          eIncomingCodecType;
    MediaCodecType          eOutgoingCodecType;
    MediaDecoder            *mediaDecoder;
    MediaEncoder            *mediaEncoder;
#endif
};


typedef struct
{
    RvBool      bUse; /* RV_TRUE if we want this channel to be automatically opened */
    RvChar      name[20]; /* Name of channel from configuration to open */
    RvChar      alConfig[32]; /* Configuration of the adaptation layer used */
    RvBool      bIsDuplex; /* RV_TRUE for a duplex channel */
    RvBool      bNullData; /* RV_TRUE if it's a duplex channel with nullData on the reverse parameters */
    RvInt32     rate; /* Rate to use for such channels */
} TermAutoChannelConfig;


typedef struct
{
    /* Add-ons and samples */
    RvBool                  bDisableAutoCaps; /* RV_TRUE to disable the use of H.245 AutoCaps add-on */
    RvBool                  bDisableMediaViewer; /* RV_TRUE to disable the use of the media viewer demo */

    RvBool                  bIndependent; /* RV_TRUE if we want new calls to be configured as independent */
    RvBool                  bAutoAnswer; /* RV_TRUE to automatically connect incoming calls */
    RvBool                  bAutoOpenChannels; /* RV_TRUE to open channels automatically */    
    RvBool                  bAutoEarlyMES; /* RV_TRUE to send MES with channels opened automatically */
    RvBool                  bAutoAcceptChannels; /* Automatically accept incoming channel requests? */
    RvBool                  bAutoDropChannels; /* Automatically drop channels when requested? */
    RvBool                  bAutoMimicChannels; /* Automatically mimic incoming channel requests? */
    RvBool                  bSendFastUpdate; /* RV_TRUE to send fast update when channels get connected */
    RvBool                  bIsdnEuropean3G; /* Are we supporting European 3G calls? */
    RvBool                  bIsdnHardwareStuffing; /* Are we doing hardware stuffing for mux-level 0? */
    RvBool                  bIsdnCheckStuffing; /* Are we counting incoming stuffing bytes? */
    RvBool                  bAutoCaps; /* Are we using Auto caps for opening channels? */
    RvBool                  bReflectWcdmaBuffers; /* Should we reflect the sent and received WCDMA buffers? */
    RvBool                  bAutoCapsReplayMedia; /*replay media when using autoCaps*/
    RvUint32                maxCCSRLSegmentSize; /* Maximum size of a CCSRL segment allowed */
    RvUint32                isdnBufferCorrectionSize; /* Size of consecutive character that will be discarded */
    RvUint32                simulationSpeed; /* Speed of simulation */

    RvBool                  bLogStreamBuffers; /* Are we saving the stream buffers we have? */
    RvChar                  logStreamBasename[128]; /* Basename of stream log files *///can be discard

    RvBool                  bMultipleH245Messages; /* Are we allowing to send multiple H.245 messages? */
    RvBool                  bUseWnsrp; /* Using WNSRP mode for the H.223 control */
    RvBool                  bNsrpRetransmitOnIdle; /* Are we retransmitting NSRP on mux idle? */
    /* ACN */
    RvBool                  bUseACP;                /* Use Advanced Connect Negotiation for call setup*/
    RvUint8                 acpAudioEntry;          /* Entry number of audio only in mux table for ACN */
    RvUint8                 acpVideoEntry;          /* Entry number of video only in mux table for ACN */

#if (RV_3G324M_USE_MONA == RV_YES)
    /* MONA+MPC */
    RvBool                  bUseMONA;                /* Use MONA (MPC + ACP) */
	RvBool					bUseMCPRxOnly;			/* Support only receiving MCP channels */ 
	RvInt16                 mpcMediaTypes;			/* A bitmask of media type of the MPC channel */
#endif /* USE_MONA */
	/* H.249 */
    RvBool                  bAddH249;               /* If RV_TRUE H249 capabilities will be added to TCS */
	

    Rv3G324mCallMuxLevel    muxLevel; /* Initial mux-level to use for calls */

    /* Retransmissions policy */
    RvBool                  bRetransmitOnBadCRC; /* Do we require retransmissions on bad CRC? */
    RvInt32                 retransmitLag; /* How many frames back do we want retransmissions for? */

    /* Manual mux table */
    RvBool                  bManMuxClear; /* Automatically clear mux table before sending MES? */
    RvBool                  bManMuxAuto; /* Automatically set manual mux entries before sending MES? */
    RvUint32                manMuxEntry[4][6]; /* 6 manual mux entries to set automatically if bMaxMuxAuto==RV_TRUE */

    /* Automatic channel opening */
    TermAutoChannelConfig   autoChannel[4]; /* Automatic channel configurations */
    cmH223ALxMConfig        alxM[3][2]; /* Pre-configurations for ALxM channel settings */

    RvBool                  bIgnoreCorruptFrames; /* Are we ignoring corrupt frames? */
    RvBool                  bAddGarbage; /* Are we adding garbage? */
    RvBool                  bErrorsCutBuffer; /* Are we "cutting" buffers read due to errors? */
    RvUint32                bitErrorRate; /* BER to use (1/bit error rate) */

    /* H249 */
    RvUint32                numberOfSoftKeys; 
} TermConfig;

/* Terminal instance object */
struct TermObj_tag
{
    TermCallbacks           cb; /* Callbacks implemented */

    TermConfig              cfg; /* Modifiable configuration parameters */

    void                    *userData; /* User data associated with this terminal. Not used in the GUI */
    HAPP                    hApp; /* Application instance of the stack */
    HPVT                    hVal; /* PVT used */

    HashHANDLE              calls; /* Calls table */
    HashHANDLE              channels; /* Channels table */

    RvInt32                 maxCalls; /* Maximum number of supported calls */
    RvInt32                 maxBufSize; /* Maximum buffer size supported by the stack */

    /*high availability*/
#if 0
    TermObj                 *termRelated; /* the stand-by terminal in the active and the active terminal
                                           in the stand-by */
    RvBool                  activateSBCalls; /* if RV_TRUE, calls on both terminals will be active
                                             until they get to the Rv3G324mCallStateConnectedChannels state,
                                             except that the calls on the stand-by terminal don't
                                             send data on the isdn channel or tcp socket */
#endif                                             
    RvBool                  bTermStackCrashed; /* RV_TRUE if the if the terminal and its stack are in a process
                                                  of crashing */
//    RvBool                  bStandBy; /* RV_TRUE if this is the standby terminal */
    RvBool                  bTestEnding; /* RV_TRUE if the active terminal and the test is ending */

    /* Internal & Simulated calls */
    TermCallObj             *transportFirstCall; /* First call that is handled by this network */
    LockHANDLE              transportMutex; /* Mutex protecting the list */
    LockHANDLE              transportStopMutex; /* Mutex protecting stopping procedure */
    RvBool                  transportThreadActive; /* RV_TRUE if the transport thread is active */
    RvBool                  bTransportIsReceiving; /* RV_TRUE if we're currently receiving information */

    /* ISDN calls */
    RvBool                  bIsdnSupported; /* Are we supporting ISDN? */
    RvBool                  bIsdnCallsDetached; /*True after isdn thread detached the terminal calls*/
    unsigned short          isdnApplicationId; /* Application ID that we registered with the ISDN controller */
    int                     isdnNumBChannels; /* Number of B-Channels supported by all of the ISDN controllers in the machine */
    LockHANDLE              isdnLock; /* Lock to use for the ISDN APIs */
    LockHANDLE              isdnGetMessageStopMutex; /* Mutex protecting stopping procedure of "Get Message" thread */
    void                    *isdnBChannels; /* B-Channels used for ISDN */
    int                     isdnBuffers; /* Number of buffers being sent */

    /* WCDMA calls */
    RvBool                  bWcdmaSupported; /* Are we supporting WCDMA? */
#ifdef USE_WCDMA
    HANDLE                  hWcdmaCom; /* handle of the file used to communicate with the WCDMA modem */
    LockHANDLE              wcdmaSendStopMutex; /* Mutex protecting stopping procedure */
    LockHANDLE              wcdmaRecvStopMutex; /* Mutex protecting stopping procedure */
#endif /* USE_WCDMA */
    TermWcdmaCallState      eWcdmaCallState; /* state variable */
    TermCallObj             *pWcdmaCall; /* the WCDMA call */

    /* Serial calls */
    RvBool                  bSerialSupported; /* Are we supporting serial connections? */
#ifdef USE_SERIAL
    HANDLE                  hSerialCom; /* handle of the file used to communicate with the serial connection */
    LockHANDLE              serialSendAndReceiveStopMutex; /* Mutex protecting stopping procedure */
#endif /* USE_WCDMA */
    TermSerialCallState     eSerialCallState; /* state variable */
    TermCallObj             *pSerialCall; /* the serial call */

    /* GEF */
    RvUint32                timesGefInit; /* times the GEF add-on was initialized */

    /* H.245 AutoCaps */
#ifdef USE_H245AUTOCAPS
    HH245AUTOCAPS           hH245AutoCaps; /* Handle to the H.245 AutoCaps module */
#endif

    RvBool                  bEnding; /* Are we closing this object? */
    RvBool                  resetError; /* Do we need to reset the error string? */
    RvChar                  lastError[256]; /* Last error string we know of */

    /* MONA */
    RvPvtNodeId             MONACapEntryId; /*MONA capability entry nodeId*/
    /* H.249 */
    #if 0
    RvPvtNodeId             H249ModalInterfaceEntryId;  /* Modal Interface capability entry in TCS */
    RvPvtNodeId             H249NavigationKeyEntryId;   /* Navigation Key capability entry in TCS */
    RvPvtNodeId             H249SoftKeysEntryId;        /* Soft Keys capability entry in TCS */
    RvPvtNodeId             H249PointingDeviceEntryId;  /* Pointing Device capability entry in TCS */
    #endif
    /* Resources */
    RvUint32                curAllocs;
    RvUint32                maxAllocs;
    RvUint32                curMemory;
    RvUint32                maxMemory;
    RvUint32                curCalls;
    RvUint32                totalCalls;
    RvUint32                totalLCSendBytes;
    RvUint32                totalLCSendCount;
    RvUint32                totalLCReleaseBytes;
    RvUint32                totalLCReleaseCount;
    RvUint32                lostFramesBySequenceNumber;
    RvUint32                frameCrcErrors;
    RvUint32                isdnRecvStuffingBytes;
    RvUint32                isdnRecvTotalBytes;
    RvUint32                isdnSendStuffingBytes;
    RvUint32                isdnSendTotalBytes;
    RvUint32                wcdmaRecvStuffingBytes;
    RvUint32                wcdmaRecvTotalBytes;
    RvUint32                wcdmaSendStuffingBytes;
    RvUint32                wcdmaSendTotalBytes;
    RvUint32                serialRecvStuffingBytes;
    RvUint32                serialRecvTotalBytes;
    RvUint32                serialSendStuffingBytes;
    RvUint32                serialSendTotalBytes;

#if (RV_H223_USE_SPLIT == RV_YES)
    RvInt32                 nsrpTimeout; /* Timeout for NSRP as found in the configuration */
    struct  {
        H223SerializeWatchdogEntry      entries[MAX_H223_WATCHDOG_ENTRIES];
        RvSize_t                        entriesNum;
    } watchdogResources[MAX_H223_STREAMING_CONNECTIONS];
#endif
//Matt
    RvSemaphore                 IsDialing;
    RvBool                     IsReverseBits;
    vt_loopback_mode_enum            loopbackMode;
    RvBool                     isActive;
    CallRcvMsgState                     callMsgState;
};



/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/

#define VideoCall_SetActiveCodec(TYPE) VideoCall_SetDecActiveCodec(TYPE); VideoCall_SetEncActiveCodec(TYPE)

#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_DEFS_H_ */

