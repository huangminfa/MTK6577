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
 *		Name:					device_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				July 2005
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/device_if.h#3 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005
]*/

/*! \file
 *		Defines the API used to fetch device information.
 *
 * \brief	Device info API
 */

#ifndef _OMC_DEVICE_IF_H_
#define _OMC_DEVICE_IF_H_

#ifdef __cplusplus
extern "C" {
#endif


/*!
===============================================================================
 * Fetch the device id string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getDevId(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the device type string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getDevTyp(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the manufacturer string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getMan(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the model string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getMod(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the OEM string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getOEM(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the firmware version string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getFwV(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the software version string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getSwV(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the hardware version string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getHwV(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Fetch the language string.
 *
 * \param	udp			User session data pointer.
 *
 * \param	context		Ignored!
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_DEV_getLang(OMC_UserDataPtr udp, void *context,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_DEVICE_IF_H_ */
