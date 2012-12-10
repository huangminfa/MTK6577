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
 *		Name:					omc_constants.h
 *
 *		Project:				OMC
 *
 *		Created On:				July 2004
 *
 *		Derived From:			//depot/main/base/omc/omc_config.h#20
 *
 *		Version:				$Id: //depot/main/base/omc/omc_constants.h#12 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \file
 *		Constants for OMC.
 *
 * \brief	Constants
 */


#ifndef _OMC_OMC_CONSTANTS_H_
#define _OMC_OMC_CONSTANTS_H_

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Miscellaneous.
 */
extern const IU8 CONST_omcVersion[];
extern const char CONST_hexDigits[];
#ifdef OMADM
extern const IU8 CONST_localhost[];
#endif

/*
 * Miscellaneous SyncML.
 */
extern const IU8 CONST_nameSeparator[];
extern const IU8 CONST_rootName[];
extern const IU8 CONST_colon[];
#ifdef OMADM
extern const IU8 CONST_dmVersion[];
extern const IU8 CONST_dmProtoVersion[];
extern const IU8 CONST_dmMimeTypeXML[];
extern const IU8 CONST_dmMimeTypeWBXML[];
extern const struct sml_pcdata_s CONST_dmVersionPcdata;
extern const struct sml_pcdata_s CONST_dmProtoPcdata;
#endif
#ifdef OMADS
extern const IU8 CONST_dsVersion[];
extern const IU8 CONST_dsProtoVersion[];
extern const IU8 CONST_dsMimeTypeXML[];
extern const IU8 CONST_dsMimeTypeWBXML[];
extern const struct sml_pcdata_s CONST_dsVersionPcdata;
extern const struct sml_pcdata_s CONST_dsProtoPcdata;
#endif

/*
 * SyncML command names.
 */
extern const IU8 CONST_add[];
extern const IU8 CONST_alert[];
extern const IU8 CONST_atomic[];
extern const IU8 CONST_copy[];
extern const IU8 CONST_delete[];
extern const IU8 CONST_exec[];
extern const IU8 CONST_get[];
extern const IU8 CONST_map[];
extern const IU8 CONST_move[];
extern const IU8 CONST_put[];
extern const IU8 CONST_replace[];
extern const IU8 CONST_results[];
extern const IU8 CONST_search[];
extern const IU8 CONST_sequence[];
extern const IU8 CONST_status[];
extern const IU8 CONST_sync[];
extern const IU8 CONST_syncHdr[];

/*
 * SyncML property names.
 */
#ifdef OMADM
extern const IU8 CONST_name[];
extern const IU8 CONST_type[];
extern const IU8 CONST_format[];
extern const IU8 CONST_acl[];
extern const IU8 CONST_size[];
#ifdef SUPPORT_OPTIONAL_PROPS
extern const IU8 CONST_title[];
extern const IU8 CONST_tStamp[];
extern const IU8 CONST_verNo[];
#endif
#endif

/*
 * SyncML format strings.
 */
extern const IU8 CONST_node[];
extern const IU8 CONST_bin[];
extern const IU8 CONST_b64[];
#ifdef OMADM
extern const IU8 CONST_chr[];
#endif

/*
 * SyncML authentication strings.
 */
extern const IU8 CONST_auth_none[];
extern const IU8 CONST_auth_basic[];
extern const IU8 CONST_auth_md5[];
extern const IU8 CONST_auth_hmac[];

extern const IU8 CONST_md5[];


#ifdef OMADM /* { */

/*
 * DevInfo tree paths.
 */
extern const IU8 CONST_devinfo_devId[];
extern const IU8 CONST_devinfo_man[];
extern const IU8 CONST_devinfo_mod[];
extern const IU8 CONST_devinfo_dmV[];
extern const IU8 CONST_devinfo_lang[];

/*
 * DM account node names.
 */
extern const IU8 CONST_clientPW[];
extern const IU8 CONST_clientNonce[];
extern const IU8 CONST_serverPW[];
extern const IU8 CONST_serverNonce[];
extern const IU8 CONST_addr[];
extern const IU8 CONST_userName[];
extern const IU8 CONST_serverId[];
extern const IU8 CONST_addrType[];
extern const IU8 CONST_portNbr[];
extern const IU8 CONST_conRef[];
extern const IU8 CONST_authPref[];
/* extern const IU8 CONST_name[]; 		Already defined */

extern const IU32 CONST_serverIdLen;

/*
 * Bootstrapping paths etc.
 */
extern const IU8 CONST_dmRootURI[];
extern const IU8 CONST_bootAccPrefix[];
extern const IU8 CONST_conRootURI[];

extern const IU32 CONST_dmRootURILen;
extern const IU32 CONST_bootAccPrefixLen;
extern const IU32 CONST_conRootURILen;

#endif /* OMADM } */


#ifdef OMADS /* { */

/*
 * DevInf.
 */
extern const IU8 CONST_typeDevinfXml[];
extern const IU8 CONST_typeDevinfWbxml[];
extern const IU8 CONST_devinfUri[];
extern const IU8 CONST_devinfVerdtd[];

#endif /* OMADS } */


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_OMC_CONSTANTS_H_ */
