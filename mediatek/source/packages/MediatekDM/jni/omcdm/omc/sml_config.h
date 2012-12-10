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
 *      Project:    	    OMC
 *
 *      Name:				sml_config.h
 *
 *      Derived From:		//depot/main/os/win/32/syncml/define.h#4
 *
 *      Created On:			October 2005
 *
 *      Version:			$Id: //depot/main/base/omc/sml_config.h#3 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            OMC specific SyncML toolkit configuration
 *
 *      (c) Copyright Insignia Solutions plc, 2004 - 2005
 *
]*/

#ifndef _OMC_SML_CONFIG_H_
#define _OMC_SML_CONFIG_H_

/*
 * Allow both XML and WBXML messages
 */
#define __SML_WBXML__
#define __SML_XML__

/* Do not enable "alloc helpers" as they are just a waste of space! */
/* #define __USE_ALLOCFUNCS__ */

/*
 * Allow the necessary PCDATA extensions.
 */
#define __USE_EXTENSIONS__
#define __USE_METINF__
#ifdef OMADS
#define __USE_DEVINF__
#endif

/*
 * Allow the necessary commands
 *
 * Commands used by both DM and DS
 */
#define ATOMIC_RECEIVE
#define COPY_RECEIVE
#define EXEC_RECEIVE
#define SEQUENCE_RECEIVE

/*
 * Commands used by DM only
 */
#if defined(OMADM) || defined(ENABLE_ALL_SYNCML_COMMANDS)
/* None! */
#endif

/*
 * Commands used by DS only
 */
#if defined(OMADS) || defined(ENABLE_ALL_SYNCML_COMMANDS)
#define MOVE_RECEIVE
/* Why isn't there a PUT_RECEIVE? */
#define RESULT_RECEIVE
#define SEARCH_RECEIVE
/* Why isn't there a SYNC_RECEIVE? */

#define ADD_SEND
#define ATOMIC_SEND
#define COPY_SEND
#define EXEC_SEND
#define GET_SEND
/* Why isn't there a MAP_SEND? */
#define MOVE_SEND
#define SEARCH_SEND
/* Why isn't there a SYNC_SEND? */
#endif

/*
 * Commands used by neither DM nor DS.
 * (Only listed here so the defines don't get forgotten about.)
 */
#ifdef ENABLE_ALL_SYNCML_COMMANDS
#define MAP_RECEIVE
#define MAPITEM_RECEIVE

/* Why isn't there a PUT_SEND? */
#define SEQUENCE_SEND
#endif

#endif /* _OMC_SML_CONFIG_H_ */
