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

/*[
 *		Name:					globals_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/globals_if.h#27
 *
 *		Version:				$Id: //depot/main/base/omc/globals_if.h#10 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \file
 * 		The OMC agent does not use any static variables. Instead all
 * 		such variables are stored in a "global variable" structure whose
 *		address	is fetched from \ref OMC_getGlobals(). (This makes it easier to
 *		port OMC to OSes that do not allow applications to use static data)
 *
 *  \brief
 *		Global variables for the OMC agent.
 */

#ifndef _OMC_GLOBALS_IF_H_
#define _OMC_GLOBALS_IF_H_

#ifdef __cplusplus
extern "C" {
#endif

/*!
 *  \cond INC_GLOBALS
 */

#define GLOBAL_DEF_YIELD	int sTaTe /* Avoid including omc_timeslice.h */

/******************************************************************************
 *****                     Global variable structure                      *****
 ******************************************************************************/

#if !(defined(OMADM) || defined(OMADS))
#error *** ERROR: At least one of OMADM and OMADS must be defined
#endif

typedef struct OMC_Globals_s
{
#if !defined(PROD_MIN)
	IU32						DEBUG_flags;
#endif
#ifdef OMC_MEM_DEBUG
	void*						MEM_allocData;
#endif


/********** fumo_storage.c **********/

#ifdef FUMO
	OMC_SessionDataPtr			FUST_owner;
	IBOOL						FUST_fotoIsOpen;
#endif


/********** mgr.c **********/

	struct syncml_info_s*		MGR_pGlobalAnchor;


/********** omc_control.c **********/

	struct {
		GLOBAL_DEF_YIELD;
#ifdef OMADM
		OMC_TreeSessionPtr		tsp;
#endif
	} TRG_OMC_init;


/********** obj_datastore.c **********/

#ifdef OMADS
	struct OBJ_Datastore_s*		OBJ_datastores;
#endif


/********** tree_find.c **********/

#ifdef TREE_IN_MEMORY
	struct TREE_NodeStruct*		TREE_root;
#endif

} OMC_Globals, *OMC_GlobalsPtr;

/******************************************************************************
 *****                     Global variable structure                      *****
 ******************************************************************************/

/*! \endcond */


/*!
================================================================================
 * Fetch the address of the global variables structure.
 *
 * \return	The address of the globals.
 *
 * \par Example code:
 * \code
 *	#include <omc/omc_if.h>
 *	#include <omc/globals_if.h>
 *
 *	LOCAL OMC_Globals globals;
 *
 *	// =====================================================
 *	//  Fetch the address of the global variables structure.
 *	//
 *	//  RETURN:	The address of the globals.
 *	// =====================================================
 *
 *	GLOBAL OMC_GlobalsPtr OMC_getGlobals(void)
 *	{
 *		return &globals;
 *	}
 * \endcode
================================================================================
 */
extern OMC_GlobalsPtr OMC_getGlobals(void);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_GLOBALS_IF_H_ */
