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
 *		Name:					time_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				April 2005
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/time_if.h#1 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005
]*/

/*! \file
 *		Since the porting layer is pure binary in nature, the interface
 *		must be strictly procedural. Thus no platform dependent constants,
 *		defines, additional include files, etc can appear in the interface.
 *
 * \brief
 *		External time API's
 */

#ifndef _OMC_TIME_IF_H_
#define _OMC_TIME_IF_H_

#ifdef __cplusplus
extern "C" {
#endif

/*!
 * The length of the ISO8601 UTC string reprsentation of a timestamp (excluding
 * the NUL terminator).
 */
#define OMC_TIMESTR_LENGTH		20


/*!
===============================================================================
 * Fetch the current time.
 *
 * \return	The timestamp.
===============================================================================
 */
extern IU32 OMC_time(void);


/*!
===============================================================================
 * Convert a timestamp into an ISO8601 UTC string representation. Such as
 * "2005-04-06T14:27:31Z" This string is always OMC_TIMESTR_LENGTH characters
 * long.
 *
 * \param	time		The timestamp.
 *
 * \retval	string		Pointer to a buffer to receive the string. This must
 *						be at least OMC_TIMESTR_LENGTH+1 bytes long.
===============================================================================
 */
extern void OMC_timestr(IU32 time, char *string);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_TIME_IF_H_ */
