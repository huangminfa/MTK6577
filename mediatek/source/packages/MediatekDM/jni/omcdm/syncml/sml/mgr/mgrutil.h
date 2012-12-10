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
 *      Name:				mgrutil.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/mgr/mgrutil.h#5 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            SyncML core code
 *
 *      (c) Copyright Insignia Solutions plc, 2004 - 2005
 *
]*/

/**
 * @file
 * SyncML API for freeing SyncML C structures
 *
 * @target_system   all
 * @target_os       all
 * @description Definitions for internal use within the SyncML implementation
 */



/*
 * Copyright Notice
 * Copyright (c) Ericsson, IBM, Lotus, Matsushita Communication
 * Industrial Co., Ltd., Motorola, Nokia, Openwave Systems, Inc.,
 * Palm, Inc., Psion, Starfish Software, Symbian, Ltd. (2001).
 * All Rights Reserved.
 * Implementation of all or part of any Specification may require
 * licenses under third party intellectual property rights,
 * including without limitation, patent rights (such a third party
 * may or may not be a Supporter). The Sponsors of the Specification
 * are not responsible and shall not be held responsible in any
 * manner for identifying or failing to identify any or all such
 * third party intellectual property rights.
 *
 * THIS DOCUMENT AND THE INFORMATION CONTAINED HEREIN ARE PROVIDED
 * ON AN "AS IS" BASIS WITHOUT WARRANTY OF ANY KIND AND ERICSSON, IBM,
 * LOTUS, MATSUSHITA COMMUNICATION INDUSTRIAL CO. LTD, MOTOROLA,
 * NOKIA, PALM INC., PSION, STARFISH SOFTWARE AND ALL OTHER SYNCML
 * SPONSORS DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO ANY WARRANTY THAT THE USE OF THE INFORMATION
 * HEREIN WILL NOT INFRINGE ANY RIGHTS OR ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT
 * SHALL ERICSSON, IBM, LOTUS, MATSUSHITA COMMUNICATION INDUSTRIAL CO.,
 * LTD, MOTOROLA, NOKIA, PALM INC., PSION, STARFISH SOFTWARE OR ANY
 * OTHER SYNCML SPONSOR BE LIABLE TO ANY PARTY FOR ANY LOSS OF
 * PROFITS, LOSS OF BUSINESS, LOSS OF USE OF DATA, INTERRUPTION OF
 * BUSINESS, OR FOR DIRECT, INDIRECT, SPECIAL OR EXEMPLARY, INCIDENTAL,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES OF ANY KIND IN CONNECTION WITH
 * THIS DOCUMENT OR THE INFORMATION CONTAINED HEREIN, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH LOSS OR DAMAGE.
 *
 * The above notice and this paragraph must be included on all copies
 * of this document that are made.
 *
 */



#ifndef _MGR_UTIL_H
#define _MGR_UTIL_H


/* Prototypes of exported SyncML API functions */
SML_API Ret_t smlFreeProtoElement(VoidPtr_t pProtoElement);
SML_API void smlFreePcdata(SmlPcdataPtr_t pPcdata);
SML_API void smlFreePcdataList(SmlPcdataListPtr_t list);

SML_API void smlFreeSyncHdr(SmlSyncHdrPtr_t pSyncHdr);
SML_API void smlFreeSync(SmlSyncPtr_t pSync);
SML_API void smlFreeGeneric(SmlGenericCmdPtr_t pGenericCmd);
SML_API void smlFreeAlert(SmlAlertPtr_t pAlert);
SML_API void smlFreeAtomic(SmlAtomicPtr_t pAtomic);
#if (defined EXEC_SEND || defined EXEC_RECEIVE)
SML_API void smlFreeExec(SmlExecPtr_t pExec);
#endif
SML_API void smlFreeGetPut(SmlPutPtr_t pGetPut);
SML_API void smlFreeMap(SmlMapPtr_t pMap);
SML_API void smlFreeResults(SmlResultsPtr_t pResults);
#if (defined SEARCH_SEND || defined SEARCH_RECEIVE)
SML_API void smlFreeSearch(SmlSearchPtr_t pSearch);
#endif
SML_API void smlFreeStatus(SmlStatusPtr_t pStatus);
SML_API void smlFreeCredPtr(SmlCredPtr_t pCred);
SML_API void smlFreeChalPtr(SmlChalPtr_t pChal);
SML_API void smlFreeTargetPtr(SmlTargetPtr_t pTarget);
SML_API void smlFreeSourcePtr(SmlSourcePtr_t pSource);
SML_API void smlFreeSourceTargetParentPtr(SmlSourceParentPtr_t pSourceTargetParent);
SML_API void smlFreeSourceList(SmlSourceListPtr_t pSourceList);
SML_API void smlFreeSourceRefList(SmlSourceRefListPtr_t pSourceRefList);
SML_API void smlFreeTargetRefList(SmlTargetRefListPtr_t pTargetRefList);
SML_API void smlFreeItemPtr(SmlItemPtr_t pItem);
SML_API void smlFreeItemList(SmlItemListPtr_t pItemList);
SML_API void smlFreeMapItemPtr(SmlMapItemPtr_t pMapItem);
SML_API void smlFreeMapItemList(SmlMapItemListPtr_t pMapItemList);
SML_API void smlFreeFilterPtr(SmlFilterPtr_t pFilter);
SML_API void smlFreeFieldOrRecordPtr(SmlFieldPtr_t pFieldOrRecord);

#ifdef __USE_METINF__
SML_API void smlFreeMetinfAnchor(SmlMetInfAnchorPtr_t data);
SML_API void smlFreeMetinfMem(SmlMetInfMemPtr_t data);
SML_API void smlFreeMetinfMetinf(SmlMetInfMetInfPtr_t data);
#endif
#ifdef __USE_DEVINF__
SML_API void smlFreeDevInfDatastore(SmlDevInfDatastorePtr_t data);
SML_API void smlFreeDevInfDatastoreList(SmlDevInfDatastoreListPtr_t data);
SML_API void smlFreeDevInfXmitList(SmlDevInfXmitListPtr_t data);
SML_API void smlFreeDevInfXmit(SmlDevInfXmitPtr_t data);
SML_API void smlFreeDevInfDSMem(SmlDevInfDSMemPtr_t data);
SML_API void smlFreeDevInfSynccap(SmlDevInfSyncCapPtr_t data);
SML_API void smlFreeDevInfExt(SmlDevInfExtPtr_t data);
SML_API void smlFreeDevInfExtList(SmlDevInfExtListPtr_t data);
SML_API void smlFreeDevInfCTCap(SmlDevInfCTCapPtr_t data);
SML_API void smlFreeDevInfCTCapList(SmlDevInfCTCapListPtr_t data);
SML_API void smlFreeDevInfFilterCap(SmlDevInfFilterCapPtr_t data);
SML_API void smlFreeDevInfFilterCapList(SmlDevInfFilterCapListPtr_t data);
SML_API void smlFreeDevInfDevInf(SmlDevInfDevInfPtr_t data);
#endif
#ifndef __SML_LITE__  /* these API calls are NOT included in the Toolkit lite version */
SML_API String_t smlPcdata2String( SmlPcdataPtr_t pcdata );
SML_API SmlPcdataPtr_t smlString2Pcdata( String_t str );
SML_API SmlPcdataPtr_t smlPcdataDup(SmlPcdataPtr_t pcdata);
SML_API MemSize_t smlGetFreeBuffer(InstanceID_t id);
#endif

#ifdef __USE_ALLOCFUNCS__
SML_API SmlPcdataPtr_t smlAllocPcdata();
SML_API SmlPcdataListPtr_t smlAllocPcdataList();
SML_API SmlChalPtr_t smlAllocChal();
SML_API SmlCredPtr_t smlAllocCred();
SML_API SmlSourcePtr_t smlAllocSource();
SML_API SmlTargetPtr_t smlAllocTarget();
SML_API SmlSourceListPtr_t smlAllocSourceList();
SML_API SmlSyncHdrPtr_t smlAllocSyncHdr();
SML_API SmlItemPtr_t smlAllocItem();
SML_API SmlItemListPtr_t smlAllocItemList();
SML_API SmlGenericCmdPtr_t smlAllocGeneric();
SML_API SmlAddPtr_t smlAllocAdd();
SML_API SmlCopyPtr_t smlAllocCopy();
SML_API SmlReplacePtr_t smlAllocReplace();
SML_API SmlDeletePtr_t smlAllocDelete();
SML_API SmlAlertPtr_t smlAllocAlert();
SML_API SmlAtomicPtr_t smlAllocAtomic();
SML_API SmlSequencePtr_t smlAllocSequence();
SML_API SmlSyncPtr_t smlAllocSync();
SML_API SmlExecPtr_t smlAllocExec();
SML_API SmlGetPtr_t smlAllocGet();
SML_API SmlPutPtr_t smlAllocPut();
SML_API SmlMapItemPtr_t smlAllocMapItem();
SML_API SmlMapItemListPtr_t smlAllocMapItemList();
SML_API SmlMapPtr_t smlAllocMap();
SML_API SmlResultsPtr_t smlAllocResults();
SML_API SmlSearchPtr_t smlAllocSearch();
SML_API SmlTargetRefListPtr_t smlAllocTargetRefList();
SML_API SmlSourceRefListPtr_t smlAllocSourceRefList();
SML_API SmlStatusPtr_t smlAllocStatus();
SML_API SmlFilterPtr_t smlAllocFilter();
SML_API SmlUnknownProtoElementPtr_t smlAllocUnknownProtoElement();
#ifdef __USE_METINF__
SML_API SmlMetInfMetInfPtr_t smlAllocMetInfMetInf();
SML_API SmlMetInfAnchorPtr_t smlAllocMetInfAnchor();
SML_API SmlMetInfMemPtr_t smlAllocMetInfMem();
#endif // MetInf

#ifdef __USE_DEVINF__
SML_API SmlDevInfExtPtr_t smlAllocDevInfExt();
SML_API SmlDevInfExtListPtr_t smlAllocDevInfExtList();
SML_API SmlDevInfSyncCapPtr_t smlAllocDevInfSyncCap();
SML_API SmlDevInfCTCap11DataPtr_t smlAllocDevInfCTCap11Data();
SML_API SmlDevInfCTCap11DataListPtr_t smlAllocDevInfCTCap11DataList();
SML_API SmlDevInfCTCap11PropPtr_t smlAllocDevInfCTCap11Prop();
SML_API SmlDevInfCTCap11PropListPtr_t smlAllocDevInfCTCap11PropList();
SML_API SmlDevInfCTCapPtr_t smlAllocDevInfCTCap11();
SML_API SmlDevInfCTCapListPtr_t smlAllocDevInfCTCap11List();
SML_API SmlDevInfDSMemPtr_t smlAllocDevInfDSMem();
SML_API SmlDevInfXmitPtr_t smlAllocDevInfXmit();
SML_API SmlDevInfXmitListPtr_t smlAllocDevInfXmitList();
SML_API SmlDevInfDatastorePtr_t smlAllocDevInfDatastore();
SML_API SmlDevInfDatastoreListPtr_t smlAllocDevInfDatastoreList();
SML_API SmlDevInfDevInfPtr_t smlAllocDevInfDevInf();
#endif // DevInf
#endif // AllocFuncs
#endif // MgrUtil.h
