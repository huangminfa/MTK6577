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
 *      Name:				sml_dump.h
 *
 *      Derived From:		//depot/main/base/syncml/test/smltest_main.c#4
 *
 *      Created On:			October 2005
 *
 *      Version:			$Id: //depot/main/base/syncml/test/sml_dump.h#1 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            Printing out SyncML data structures
 *
 *      (c) Copyright Insignia Solutions plc, 2005
 *
]*/

#ifndef SYNCML_TEST_SML_DUMP_H
#define SYNCML_TEST_SML_DUMP_H

#ifdef SML_DUMP_ENABLED

/*
 * All the structure dumping functions have the same interface
 * so avoid lots of text and define a macro to declare these.
 */
#define DUMP_FUNCTION(NAME)	\
void dump_##NAME(const char *name, struct NAME *param, int level)

extern DUMP_FUNCTION(sml_pcdata_s);
extern DUMP_FUNCTION(sml_pcdata_list_s);
extern DUMP_FUNCTION(sml_chal_s);
extern DUMP_FUNCTION(sml_cred_s);
extern DUMP_FUNCTION(sml_filter_s);
extern DUMP_FUNCTION(sml_source_s);
extern DUMP_FUNCTION(sml_source_list_s);
extern DUMP_FUNCTION(sml_target_s);
extern DUMP_FUNCTION(sml_source_or_target_parent_s);
extern DUMP_FUNCTION(sml_sync_hdr_s);
extern DUMP_FUNCTION(sml_item_s);
extern DUMP_FUNCTION(sml_item_list_s);
extern DUMP_FUNCTION(sml_generic_s);
extern DUMP_FUNCTION(sml_alert_s);
extern DUMP_FUNCTION(sml_atomic_s);
extern DUMP_FUNCTION(sml_sync_s);
extern DUMP_FUNCTION(sml_exec_s);
extern DUMP_FUNCTION(sml_get_put_s);
extern DUMP_FUNCTION(sml_map_item_s);
extern DUMP_FUNCTION(sml_map_item_list_s);
extern DUMP_FUNCTION(sml_map_s);
extern DUMP_FUNCTION(sml_results_s);
extern DUMP_FUNCTION(sml_search_s);
extern DUMP_FUNCTION(sml_target_ref_list_s);
extern DUMP_FUNCTION(sml_source_ref_list_s);
extern DUMP_FUNCTION(sml_status_s);
extern DUMP_FUNCTION(sml_field_or_record_s);
extern DUMP_FUNCTION(sml_unknown_proto_element_s);
extern DUMP_FUNCTION(sml_metinf_mem_s);
extern DUMP_FUNCTION(sml_metinf_anchor_s);
extern DUMP_FUNCTION(sml_devinf_datastore_s);
extern DUMP_FUNCTION(sml_devinf_datastorelist_s);
extern DUMP_FUNCTION(sml_devinf_ctcap_s);
extern DUMP_FUNCTION(sml_devinf_ctcaplist_s);
extern DUMP_FUNCTION(sml_devinf_ext_s);
extern DUMP_FUNCTION(sml_devinf_extlist_s);
extern DUMP_FUNCTION(sml_devinf_synccap_s);
extern DUMP_FUNCTION(sml_devinf_filtercap_s);
extern DUMP_FUNCTION(sml_devinf_filtercaplist_s);
extern DUMP_FUNCTION(sml_devinf_xmit_s);
extern DUMP_FUNCTION(sml_devinf_xmitlist_s);
extern DUMP_FUNCTION(sml_devinf_dsmem_s);
extern DUMP_FUNCTION(sml_devinf_ctcap12_prop_s);
extern DUMP_FUNCTION(sml_devinf_ctcap12_proplist_s);
extern DUMP_FUNCTION(sml_devinf_ctcap12_param_s);
extern DUMP_FUNCTION(sml_devinf_ctcap12_paramlist_s);
extern DUMP_FUNCTION(sml_devinf_ctcap11_prop_s);
extern DUMP_FUNCTION(sml_devinf_ctcap11_proplist_s);
extern DUMP_FUNCTION(sml_devinf_ctcap11_data_s);
extern DUMP_FUNCTION(sml_devinf_ctcap11_datalist_s);

/*
 * Declarations of dump functions for all of the data types in the DTD.
 * These actually call the structure dumping functions defined above
 * and more than one data type may refer to the same underlying structure.
 */
#define dumpSmlPcdataPtr_t                 dump_sml_pcdata_s
#define dumpSmlPcdataListPtr_t             dump_sml_pcdata_list_s
#define dumpSmlChalPtr_t                   dump_sml_chal_s
#define dumpSmlCredPtr_t                   dump_sml_cred_s
#define dumpSmlFilterPtr_t                 dump_sml_filter_s
#define dumpSmlSourcePtr_t                 dump_sml_source_s
#define dumpSmlSourceListPtr_t             dump_sml_source_list_s
#define dumpSmlTargetPtr_t                 dump_sml_target_s
#define dumpSmlSourceParentPtr_t           dump_sml_source_or_target_parent_s
#define dumpSmlTargetParentPtr_t           dump_sml_source_or_target_parent_s
#define dumpSmlSyncHdrPtr_t                dump_sml_sync_hdr_s
#define dumpSmlItemPtr_t                   dump_sml_item_s
#define dumpSmlItemListPtr_t               dump_sml_item_list_s
#define dumpSmlAddPtr_t                    dump_sml_generic_s
#define dumpSmlCopyPtr_t                   dump_sml_generic_s
#define dumpSmlMovePtr_t                   dump_sml_generic_s
#define dumpSmlReplacePtr_t                dump_sml_generic_s
#define dumpSmlDeletePtr_t                 dump_sml_generic_s
#define dumpSmlGenericCmdPtr_t             dump_sml_generic_s
#define dumpSmlAlertPtr_t                  dump_sml_alert_s
#define dumpSmlAtomicPtr_t                 dump_sml_atomic_s
#define dumpSmlSequencePtr_t               dump_sml_atomic_s
#define dumpSmlSyncPtr_t                   dump_sml_sync_s
#define dumpSmlExecPtr_t                   dump_sml_exec_s
#define dumpSmlPutPtr_t                    dump_sml_get_put_s
#define dumpSmlGetPtr_t                    dump_sml_get_put_s
#define dumpSmlMapItemPtr_t                dump_sml_map_item_s
#define dumpSmlMapItemListPtr_t            dump_sml_map_item_list_s
#define dumpSmlMapPtr_t                    dump_sml_map_s
#define dumpSmlResultsPtr_t                dump_sml_results_s
#define dumpSmlSearchPtr_t                 dump_sml_search_s
#define dumpSmlTargetRefListPtr_t          dump_sml_target_ref_list_s
#define dumpSmlSourceRefListPtr_t          dump_sml_source_ref_list_s
#define dumpSmlStatusPtr_t                 dump_sml_status_s
#define dumpSmlFieldPtr_t                  dump_sml_field_or_record_s
#define dumpSmlRecordPtr_t                 dump_sml_field_or_record_s
#define dumpSmlUnknownProtoElementPtr_t    dump_sml_unknown_proto_element_s
#define dumpSmlMetInfMemPtr_t              dump_sml_metinf_mem_s
#define dumpSmlMetInfAnchorPtr_t           dump_sml_metinf_anchor_s
#define dumpSmlDevInfDatastorePtr_t        dump_sml_devinf_datastore_s
#define dumpSmlDevInfDatastoreListPtr_t    dump_sml_devinf_datastorelist_s
#define dumpSmlDevInfCTCapPtr_t            dump_sml_devinf_ctcap_s
#define dumpSmlDevInfCTCapListPtr_t        dump_sml_devinf_ctcaplist_s
#define dumpSmlDevInfExtPtr_t              dump_sml_devinf_ext_s
#define dumpSmlDevInfExtListPtr_t          dump_sml_devinf_extlist_s
#define dumpSmlDevInfSyncCapPtr_t          dump_sml_devinf_synccap_s
#define dumpSmlDevInfFilterCapPtr_t        dump_sml_devinf_filtercap_s
#define dumpSmlDevInfFilterCapListPtr_t    dump_sml_devinf_filtercaplist_s
#define dumpSmlDevInfXmitPtr_t             dump_sml_devinf_xmit_s
#define dumpSmlDevInfXmitListPtr_t         dump_sml_devinf_xmitlist_s
#define dumpSmlDevInfDSMemPtr_t            dump_sml_devinf_dsmem_s
#define dumpSmlDevInfCTCap12PropPtr_t      dump_sml_devinf_ctcap12_prop_s
#define dumpSmlDevInfCTCap12PropListPtr_t  dump_sml_devinf_ctcap12_proplist_s
#define dumpSmlDevInfCTCap12ParamPtr_t     dump_sml_devinf_ctcap12_param_s
#define dumpSmlDevInfCTCap12ParamListPtr_t dump_sml_devinf_ctcap12_paramlist_s
#define dumpSmlDevInfCTCap11PropPtr_t      dump_sml_devinf_ctcap11_prop_s
#define dumpSmlDevInfCTCap11PropListPtr_t  dump_sml_devinf_ctcap11_proplist_s
#define dumpSmlDevInfCTCap11DataPtr_t      dump_sml_devinf_ctcap11_data_s
#define dumpSmlDevInfCTCap11DataListPtr_t  dump_sml_devinf_ctcap11_datalist_s

/*
 * There are a few things to dump that do not have an associated structure.
 */
extern void dumpFlag_t(char *name, Flag_t f, int level);

extern void dumpEndMessage(char *name, Boolean_t final, int level);
extern void dumpEndAtomic(char *name, int level);
extern void dumpEndSequence(char *name, int level);
extern void dumpEndSync(char *name, int level);

#endif /* SML_DUMP_ENABLED */

#endif /* !SYNCML_TEST_SML_DUMP_H */
