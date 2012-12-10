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

#ifndef _RV_TERM_H223_H_
#define _RV_TERM_H223_H_

/***********************************************************************
termh223.h

h223 glue handling
Handles the call procedures.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"
#include "termh223.h"

#ifdef __cplusplus
extern "C" {
#endif

#if (RV_H223_USE_SPLIT == RV_YES)
    

/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/




/*-----------------------------------------------------------------------*/
/*                           CALLBACK HEADERS                            */
/*-----------------------------------------------------------------------*/




/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termH223Init
 * ----------------------------------------------------------------------------
 * General: Initialize the use of term H223 by the test application.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH223Init(
    IN  TermObj         *term);

/******************************************************************************
 * termH223SetCfg
 * ----------------------------------------------------------------------------
 * General: Update configuration information used by the H.223 Split remote
 *          applications.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH223SetCfg(
    IN  TermObj         *term);

/******************************************************************************
 * termH223HandleStreamingMessage
 * ----------------------------------------------------------------------------
 * General: handle messages received over a specific streaming connection
 *
 * Return Value: none
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   term        - term object
 *          idx         - index of connection
 *          pbuffer     - pointer to serialized buffer
 *          bufferLength- length of serialized buffer
 *          opCode      - opcode of message
 *
 * Output: None.
 *****************************************************************************/
void termH223HandleStreamingMessage(
        IN  TermObj     *term,
        IN  RvInt32     idx,
        IN	RvUint8		*pbuffer,
        IN	RvUint16	bufferLength,
        IN	RvUint8		opCode);

/******************************************************************************
 * termH223CallDial
 * ----------------------------------------------------------------------------
 * General: Dial out a call to a given IP address (not optional).
 *          All settings for this call are previously set before calling 
 *          this function.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         destStr      - Destination address. Can NOT be NULL.
 * Output: None.
*****************************************************************************/
void termH223CallDial(
            IN TermCallObj  *call,
            IN const RvChar *destStr);

/******************************************************************************
 * termH223CallDestruct
 * ----------------------------------------------------------------------------
 * General: Destruct a call
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call                 - Call handle to use.
 * Output: None.
*****************************************************************************/
void termH223CallDestruct(
            IN TermCallObj  *call);

/******************************************************************************
 * termH223ChannelConstruct
 * ----------------------------------------------------------------------------
 * General: Constructing an H223 channel
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  chan                 - Channel to construct.
 * Output: None.
*****************************************************************************/
RvStatus termH223ChannelConstruct(
            IN TermChannelObj   *chan);

/******************************************************************************
 * termH223ChannelDestruct
 * ----------------------------------------------------------------------------
 * General: Destructing an H223 channel
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  chan                 - Channel to destruct.
 * Output: None.
*****************************************************************************/
void termH223ChannelDestruct(
            IN TermChannelObj   *chan);


/******************************************************************************
 * termH223ChannelSendData
 * ----------------------------------------------------------------------------
 * General: sending data on a specific channel
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  chan                 - Channel.
 *         data                 - data to send
 *         size                 - size of data
 * Output: None.
*****************************************************************************/
RvStatus termH223ChannelSendData(
            IN  TermChannelObj  *chan,
            IN  RvUint8         *data,
            IN  RvUint16        size);

/******************************************************************************
 * termH223GetWatchdogResource
 * ----------------------------------------------------------------------------
 * General: retrieving resources from split side
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term                 - term object.
 * Output: None.
*****************************************************************************/
RvStatus termH223GetWatchdogResource(
        IN  TermObj     *term);
            
#else

#define termH223Init(_term) RV_OK
#define termH223SetCfg(_term)
#define termH223HandleStreamingMessage(_term, _idx, _pbuffer, _bufferLength, _opCode)
#define termH223CallDial(_call, _destStr)
#define termH223CallDestruct(_call)
#define termH223ChannelSendData(_chan, _data, _size) RV_OK
#define termH223GetWatchdogResource(_term) RV_OK


#endif /* (RV_H223_USE_SPLIT == RV_YES) */

#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_H223_H_ */
