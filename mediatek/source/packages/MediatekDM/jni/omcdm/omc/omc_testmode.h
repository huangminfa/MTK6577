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
 *		Name:					omc_testmode.h
 *
 *		Project:				OMC
 *
 *		Created On:				October 2005
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/omc_testmode.h#4 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005 - 2006
]*/

/*! \file
 *			Test mode interface declarations.
 *
 * \brief
 *			Test mode interface
 */

#ifndef _OMC_OMC_TESTMODE_H_
#define _OMC_OMC_TESTMODE_H_

#ifdef OMC_ENABLE_TEST_MODE

/*
 * Set this flag to force the client to use the default client
 * authentication and to ignore the value in the account (if
 * any).
 */
extern IBOOL TEST_forceDefaultClientAuth;

/*
 * Set this flag to force the client to use the default server
 * authentication and to ignore the value in the account (if
 * any).
 */
extern IBOOL TEST_forceDefaultServerAuth;

/*
 * Set this flag to force the client to use the default encoding
 * and to ignore the value in the account (if any).
 */
extern IBOOL TEST_forceDefaultEncoding;

/*
 * Set this flag to force the client to use the session ID in
 * TEST_sessionId. This will override the session ID sentin a
 * trigger message.
 */
extern IBOOL TEST_forceSessionId;
extern IU16 TEST_sessionId;


#ifdef OMADS

/*
 * Set these flags to force the client to get or put the devinf
 * during the session sync initialization phase.
 */
extern IBOOL TEST_getDevinf;
extern IBOOL TEST_putDevinf;

/*
 * Set these flags to force the client to send Ext values with
 * the devinf or EMI values with the metinf (for some commands).
 */
#ifdef SUPPORT_DEVINF_EXT
extern IBOOL TEST_sendExt;
#endif
#ifdef SUPPORT_METINF_EMI
extern IBOOL TEST_sendEmi;
#endif


#endif

#endif /* OMC_ENABLE_TEST_MODE */

#endif /* !_OMC_OMC_TESTMODE_H_ */
