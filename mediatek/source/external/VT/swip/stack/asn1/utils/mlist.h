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

#ifndef _MLIST_H
#define _MLIST_H

#include "ra.h"

#ifdef __cplusplus
extern "C" {
#endif

/* MLIST element pointer declaration */
typedef void *LISTElement;

RV_DECLARE_HANDLE(HLIST); /* list handler */


/************************************************************************
 * mlistConstruct
 * purpose: Create an MLIST object.
 * input  : elemSize            - Size of elements in the MLIST in bytes.
 *          maxNumOfElements    - Number of elements in MLIST.
 *          name                - Name of MLIST (used in log messages).
 *          logMgr              - Log manager to use.
 * output : none
 * return : handle to MLIST constructed on success.
 *          NULL on failure.
 ************************************************************************/
RVINTAPI HLIST RVCALLCONV
mlistConstruct(
    IN int              elemSize,
    IN int              maxNumOfElements,
    IN const RvChar*    name,
    IN RvLogMgr*        logMgr);


/************************************************************************
 * mlistDestruct
 * purpose: Free memort acquired by MLIST
 * input  : mList   - MLIST handle
 * output : none
 * return : none
 ************************************************************************/
RVINTAPI void RVCALLCONV
mlistDestruct(IN HLIST mList);


/************************************************************************
 * mlistAddElement
 * purpose: Add an empty list element. This element is a list by its own.
 *          It can be linked to another list using mlistInsert()
 * input  : mList   - MLIST handle
 * output : none
 * return : Handle of new list element on success
 *          NULL on failure
 ************************************************************************/
RVINTAPI LISTElement RVCALLCONV
mlistAddElement(IN HLIST mList);


/************************************************************************
 * mlistInsert
 * purpose: Insert an element into a list. The inserted item shouldn't
 *          belong to any list (i.e. - it was just created using
 *          mlistAddElement).
 * input  : destListElem    - Destination list element. The inserted
 *                            item is added before or after this element
 *          insertedElem    - The item inserted to the list
 *          insertAfter     - RV_TRUE if element should be inserted after the
 *                            destination element, RV_FALSE if it should be
 *                            inserted before it
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 * note   : Both elements should be in the same MLIST!
 ************************************************************************/
RVINTAPI int RVCALLCONV
mlistInsert(
    IN LISTElement  destListElem,
    IN LISTElement  insertedElem,
    IN RvBool       insertAfter);


/************************************************************************
 * mlistDeleteElement
 * purpose: Delete a single element from a list, fixing all connection of
 *          that list.
 * input  : mList       - MLIST to use
 *          deletedElem - Element to delete
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
RVINTAPI int RVCALLCONV
mlistDeleteElement(
    IN HLIST        mList,
    IN LISTElement  deletedElem);


/************************************************************************
 * mlistDeleteList
 * purpose: Delete a list of connected elements from MLIST
 * input  : mList       - MLIST to use
 *          element     - Element in the list to delete
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
RVINTAPI int RVCALLCONV
mlistDeleteList(
    IN HLIST        mList,
    IN LISTElement  element);


/************************************************************************
 * mlistNext
 * purpose: Get next element from a list
 * input  : element - Current element in list
 * output : none
 * return : Handle of the next element on success
 *          NULL on failure (when current element is the tail of the list)
 ************************************************************************/
RVINTAPI LISTElement RVCALLCONV
mlistNext(IN LISTElement element);


/************************************************************************
 * mlistPrev
 * purpose: Get previous element from a list
 * input  : element - Current element in list
 * output : none
 * return : Handle of the previous element on success
 *          NULL on failure (when current element is the head of the list)
 ************************************************************************/
LISTElement mlistPrev(IN LISTElement element);




#ifdef __cplusplus
}
#endif


#endif  /* MLIST_H */

