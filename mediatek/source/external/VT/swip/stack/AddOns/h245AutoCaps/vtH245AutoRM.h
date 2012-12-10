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

#ifndef __VTH245AUTORM_H__
#define __VTH245AUTORM_H__

#include "termDefs.h"
#include "cmctrl.h"
#include "H245AutoCapsInternal.h"

#ifdef USE_H263_SWITCHING_VIDEO_SIZE

#define RM_UPDATE_STATE(a,b,c) do{\
(a)->rmState = b;\
(a)->rmAbandonVideoChan = c;\
}while(0)

/******************************************************************************
 * MTK: vtH245RMClear
 * ----------------------------------------------------------------------------
 * General: clear all pre-store channel parameters
 *
 * Return Value: None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  
 * Output: 
 *****************************************************************************/
RVAPI void RVCALLCONV vtH245RMClear(
    IN  HAUTOCAPSCALL       hAutoCapsCall);

/******************************************************************************
 * RvH245AutoCapsBuildCapNodeIdFromModeElemH263 (MTK)
 * ----------------------------------------------------------------------------
 * General: build capability node from modeElem node 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  
 * Output: 
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vtH245RMBuildDataTypeNodeFromModeElemH263(
    IN  HPVT            hVal,
    IN  HAPPCALL        haCall,
    IN  RvPvtNodeId     modeNodeId,
    IN  RvPvtNodeId     capNodeId,
    OUT RvPvtNodeId     *dataTypeNodeId);

/******************************************************************************
 * MTK: vtH245RMGetPreferredAL
 * ----------------------------------------------------------------------------
 * General: Get the preferred AL and encode to H223LogicalChannelParameters node
 *          format.    
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 *
 * Input:  
 *  autoCapsInfo           
 *  hsChan                  - The channel in which its AL paramters are going to 
 *                            be saved.
 * 
 * Output: 
 *  h223NodeId              - A pointer to the nodeId that saves the AL 
 *                            inforamtion
 *****************************************************************************/
RvStatus RVCALLCONV vtH245RMGetPreferredAL(
    IN  RvH245AutoCapsCall* autoCapsInfo,
    IN  HCHAN               hsChan,
    OUT  RvPvtNodeId        *h223NodeId);
    
/******************************************************************************
 * vtH245RMValidateRequestedAL (MTK)
 * ----------------------------------------------------------------------------
 * General: Verify that the h223 modes specified in requestMode message are 
 *          supported by local.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 *
 * Input:  
 *  eChannelType             - Channel type to validate.
 *  autoCapsInfo             - The information structure that holds
 *                             all the information needed for the validation.
 *  localCapsNodeId          - local capability node id
 *  requestedModeId          - H223ModeParameters node id
 *
 * Output: 
 *  bIsValid                 - A pointer to the matching result
 *****************************************************************************/
RvStatus RVCALLCONV vtH245RMValidateRequestedAL(
    IN     cmChannelDataType          eChannelType,
    IN     RvH245AutoCapsCall         *autoCapsInfo,
    IN     RvPvtNodeId                localCapsNodeId,
    IN     RvPvtNodeId                requestedModeId,
    OUT    RvBool                     *bIsValid);

/******************************************************************************
 * MTK: vtSaveParameters
 * ----------------------------------------------------------------------------
 * General: save old channel parameters in case we will need when we have to 
 *          re-open it.  
 *
 * Return Value: None
 * ----------------------------------------------------------------------------
 * Arguments:
 *
 * Input:  
 *  hSrcChanP   -   old channel object
 *  hApp        -   
 *  haCall      -   autoCaps handle
 *  modeElemTypeNodeId  -   type node id of the mode element in the requestMode
 *                          message.
 * Output: 
 *****************************************************************************/
RvStatus vtSaveChannelParams(
    IN TermChannelObj *hSrcChanP,
    IN HAPP           hApp,
    IN HAPPCALL       haCall,   /* autoCapsCall */
    IN RvPvtNodeId    modeElemTypeNodeId);

/******************************************************************************
 * MTK: vtH245RMOpenChannelWithParameters
 * ----------------------------------------------------------------------------
 * General: To open a new channel with pre-defined settings.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 *
 * Input:
 *  hAutoCapsCall   
 * Output: 
 *  None
 *****************************************************************************/
RvStatus vtH245RMOpenChannelWithParameters(
    IN  HAUTOCAPSCALL       hAutoCapsCall);
    
#endif

#endif

