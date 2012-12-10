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
 *		Project:			OMC
 *
 *		Name:				smldtd.h
 *
 *		Derived From:		Original
 *
 *		Created On:			May 2004
 *
 *		Version:			$Id: //depot/main/base/syncml/sml/smldtd.h#7 $
 *
 *		Coding Standards:	3.0
 *
 *		Purpose:			SyncML core code
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/**
 * @file
 * SyncML DTD specific type definitions
 *
 * @target_system	all
 * @target_os		all
 * @description Definition of structures representing DTD elements
 */


/********************************************************************/
/* @note															*/
/* These definitions are based on the DTD dated from July, 7th, 00 */
/********************************************************************/

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


#ifndef _SML_DTD_H
#define _SML_DTD_H


/*************************************************************************/
/*	Definitions															 */
/*************************************************************************/


#include <syncml/sml/smldef.h>


/*
 * Forward references for types
 */
typedef struct sml_pcdata_s					 SmlPcdata_t,			   *SmlPcdataPtr_t;
typedef struct sml_pcdata_list_s			 SmlPcdataList_t,		   *SmlPcdataListPtr_t;
typedef struct sml_chal_s					 SmlChal_t,				   *SmlChalPtr_t;
typedef struct sml_cred_s					 SmlCred_t,				   *SmlCredPtr_t;
typedef struct sml_filter_s					 SmlFilter_t,			   *SmlFilterPtr_t;
typedef struct sml_source_s					 SmlSource_t,			   *SmlSourcePtr_t;
typedef struct sml_source_list_s			 SmlSourceList_t,		   *SmlSourceListPtr_t;
typedef struct sml_target_s					 SmlTarget_t,			   *SmlTargetPtr_t;
typedef struct sml_source_or_target_parent_s SmlSourceParent_t,		   *SmlSourceParentPtr_t;
typedef struct sml_source_or_target_parent_s SmlTargetParent_t,		   *SmlTargetParentPtr_t;
typedef struct sml_sync_hdr_s				 SmlSyncHdr_t,			   *SmlSyncHdrPtr_t;
typedef struct sml_item_s					 SmlItem_t,				   *SmlItemPtr_t;
typedef struct sml_item_list_s				 SmlItemList_t,			   *SmlItemListPtr_t;
typedef struct sml_generic_s				 SmlAdd_t,				   *SmlAddPtr_t;
typedef struct sml_generic_s				 SmlCopy_t,				   *SmlCopyPtr_t;
typedef struct sml_generic_s				 SmlMove_t,				   *SmlMovePtr_t;
typedef struct sml_generic_s				 SmlReplace_t,			   *SmlReplacePtr_t;
typedef struct sml_generic_s				 SmlDelete_t,			   *SmlDeletePtr_t;
typedef struct sml_generic_s				 SmlGenericCmd_t,		   *SmlGenericCmdPtr_t;
typedef struct sml_alert_s					 SmlAlert_t,			   *SmlAlertPtr_t;
typedef struct sml_atomic_s					 SmlAtomic_t,			   *SmlAtomicPtr_t;
typedef struct sml_atomic_s					 SmlSequence_t,			   *SmlSequencePtr_t;
typedef struct sml_sync_s					 SmlSync_t,				   *SmlSyncPtr_t;
typedef struct sml_exec_s					 SmlExec_t,				   *SmlExecPtr_t;
typedef struct sml_get_put_s				 SmlPut_t,				   *SmlPutPtr_t;
typedef struct sml_get_put_s				 SmlGet_t,				   *SmlGetPtr_t;
typedef struct sml_map_item_s				 SmlMapItem_t,			   *SmlMapItemPtr_t;
typedef struct sml_map_item_list_s			 SmlMapItemList_t,		   *SmlMapItemListPtr_t;
typedef struct sml_map_s					 SmlMap_t,				   *SmlMapPtr_t;
typedef struct sml_results_s				 SmlResults_t,			   *SmlResultsPtr_t;
typedef struct sml_search_s					 SmlSearch_t,			   *SmlSearchPtr_t;
typedef struct sml_target_ref_list_s		 SmlTargetRefList_t,	   *SmlTargetRefListPtr_t;
typedef struct sml_source_ref_list_s		 SmlSourceRefList_t,	   *SmlSourceRefListPtr_t;
typedef struct sml_status_s					 SmlStatus_t,			   *SmlStatusPtr_t;
typedef struct sml_field_or_record_s		 SmlField_t,			   *SmlFieldPtr_t;
typedef struct sml_field_or_record_s		 SmlRecord_t,			   *SmlRecordPtr_t;
typedef struct sml_unknown_proto_element_s	 SmlUnknownProtoElement_t, *SmlUnknownProtoElementPtr_t;



/**
 * ===========================
 * Common used SyncML Elements
 * ===========================
 */



/**
 * PCDATA - types of synchronization data which SyncML supports
 */
typedef enum {
	SML_PCDATA_UNDEFINED = 0,
	SML_PCDATA_STRING,	  /**< String type */
	SML_PCDATA_OPAQUE,	  /**< Opaque type */
	SML_PCDATA_EXTENSION, /**< Extention type - specified by PcdataExtension_t */
	SML_PCDATA_CDATA	  /**< XML CDATA type	 */
} SmlPcdataType_t;


/**
 * PCDATA - types of extensions for PCData elements
 */
typedef enum {
	SML_EXT_UNDEFINED = 0,
	SML_EXT_METINF, /**< Meta Information */
	SML_EXT_DEVINF, /**< Device Information */
	SML_EXT_LAST	/**< last codepage, needed for loops! */
} SmlPcdataExtension_t;



/**
 * PCDATA - into this structure SyncML wraps the synchronization data itself
 */
struct sml_pcdata_s
{
	SmlPcdataType_t		  contentType;	 /**< The type of data which a PCDATA structure contains */
	SmlPcdataExtension_t  extension;	 /**< PCData Extension type */
	MemSize_t			  length;		 /**< length of the data in this PCDATA structure */
	VoidPtr_t			  content;		 /**< Pointer to the data itself */
	SmlPcdataPtr_t		  next;			 /**< Pointer to the next PCDATA element or NULL */
};

/** generic list of PCData elements */
struct sml_pcdata_list_s
{
	SmlPcdataPtr_t		data;
	SmlPcdataListPtr_t	next;
};

/*
 * Various flags which are actually declared and (EMPTY) elements in
 * SyncML. This assumes at least a 16-bit architecture for the
 * underlying OS. We need to review this if that is deemed a problem.
 */
#define SmlArchive_f			0x0001		/**< Delete flags */
#define SmlSftDel_f				0x0002		/**< Delete flags */
#define SmlMoreData_f			0x0004		/**< MoreData flag */
#define SmlNoResults_f			0x0008		/**< No Results flag  */
#define SmlNoResp_f				0x0010		/**< No Response flag */
#define SmlFinal_f				0x0020		/**< Header flag */
#ifdef __USE_METINF__
#define SmlMetInfSharedMem_f	0x0040		/**< MetInf Shared Memory flag */
#endif
#ifdef __USE_DEVINF__
#define SmlDevInfSharedMem_f	0x0080		/**< DevInf Shared Memory flag */
/* SCTSTK - 18/03/2002, S.H. 2002-04-05 : SyncML 1.1 */
#define SmlDevInfUTC_f			0x0100		/**< DevInf UTC flag */
#define SmlDevInfNOfM_f			0x0200		/**< DevInf Support n of m flag */
#define SmlDevInfLargeObject_f	0x0400		/**< DevInf Support large objects flag */
#define SmlDevInfFieldLevel_f	0x0800		/**< DevInf Support field level flag */
#define SmlDevInfNoTruncate_f	0x1000		/**< DevInf No truncate flag */
#define SmlDevInfHierarchical_f	0x2000		/**< DevInf Support hierarchical sync flag */
#endif
/*      SPARE                   0x4000 */
/*      SPARE                   0x8000 */

/**
 * Chal
 */
struct sml_chal_s
{
	SmlPcdataPtr_t			 meta;
};

/**
 * Credentials
 */
struct sml_cred_s
{
	SmlPcdataPtr_t			 meta;		 // opt.
	SmlPcdataPtr_t			 data;
};

/**
 * Filter
 */
struct sml_filter_s
{
	SmlPcdataPtr_t		meta;
	SmlFieldPtr_t		field;			// opt.
	SmlRecordPtr_t		record;			// opt.
	SmlPcdataPtr_t		filterType;		// opt.
};

/**
 * Filter Field or Record
 */
struct sml_field_or_record_s
{
	SmlItemPtr_t		item;
};

/**
 * Source location
 */
struct sml_source_s
{
	SmlPcdataPtr_t			 locURI;
	SmlPcdataPtr_t			 locName;	 // opt.
};

/**
 * Source location list
 */
struct sml_source_list_s
{
	SmlSourcePtr_t		source;
	SmlSourceListPtr_t	next;
};

/**
 * Target location
 */
struct sml_target_s
{
	SmlPcdataPtr_t			 locURI;
	SmlPcdataPtr_t			 locName;	 // opt.
	SmlFilterPtr_t			 filter;	 // opt. 1.2
};


/**
 * Source or target parent location
 */
struct sml_source_or_target_parent_s
{
	SmlPcdataPtr_t			 locURI;
};


/*
 * ==============================
 * SyncML Message Header Elements
 * ==============================
 */


/**
 * SyncML header
 * As the header is needed for each SyncML message, it's also the parameter
 * of the startMessage call.
 */
struct sml_sync_hdr_s
{
	SmlProtoElement_t	elementType;	// Internal Toolkit Field
	SmlPcdataPtr_t		version;
	SmlPcdataPtr_t		proto;
	SmlPcdataPtr_t		sessionID;
	SmlPcdataPtr_t		msgID;
	Flag_t				flags;			// NoResp
	SmlTargetPtr_t		target;
	SmlSourcePtr_t		source;
	SmlPcdataPtr_t		respURI;		// opt.
	SmlCredPtr_t		cred;			// opt.
	SmlPcdataPtr_t		meta;			// opt.
};

// SyncML Body and SyncML container is not needed, as there are function calls
// (smlStartMessage(), smlEndMessage()) that let the framework know when to start and end
// the SyncML document



/*
 * =========================
 * Data description elements
 * =========================
 */


/**
 * Data in SyncML is encapsulated in an "item" element.
 */
struct sml_item_s
{
	SmlTargetPtr_t		  target;		// opt.
	SmlSourcePtr_t		  source;		// opt.
	SmlSourceParentPtr_t  sourceParent; // opt.
	SmlTargetParentPtr_t  targetParent; // opt.
	SmlPcdataPtr_t		  meta;			// opt.
	SmlPcdataPtr_t		  data;			// opt.
	Flag_t				  flags;		// opt. for MoreData
};

struct sml_item_list_s
{
	SmlItemPtr_t		  item;
	SmlItemListPtr_t	  next;
};


/*
 * ==============================================
 * SyncML Commands (Protocol Management Elements)
 * ==============================================
 */

/**
 * Generic commands:
 * Add, Copy, Replace, Delete
 */
struct sml_generic_s
{
	SmlProtoElement_t	  elementType;	// Internal Toolkit Field
	SmlPcdataPtr_t		  cmdID;
	Flag_t				  flags;		// NoResp, Archive (Delete), SftDel (Delete)
	SmlCredPtr_t		  cred;			// opt.
	SmlPcdataPtr_t		  meta;			// opt.
	SmlItemListPtr_t	  itemList;
};

/**
 * Alert command:
 */
struct sml_alert_s
{
	SmlProtoElement_t	  elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		  cmdID;
	Flag_t				  flags;	   // NoResp
	SmlCredPtr_t		  cred;		   // opt.
	SmlPcdataPtr_t		  data;		   // opt.
	SmlPcdataPtr_t		  correlator;  // opt. (SyncML 1.2)
	SmlItemListPtr_t	  itemList;
};


/**
 * Atomic/Sequence command:
 */
struct sml_atomic_s
{
	SmlProtoElement_t	  elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		  cmdID;
	Flag_t				  flags;	  // NoResp
	SmlPcdataPtr_t		  meta;		  // opt.
};


/**
 * Sync command:
 */
struct sml_sync_s
{
	SmlProtoElement_t	  elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		  cmdID;
	Flag_t				  flags;	  // NoResp
	SmlCredPtr_t		  cred;		  // opt.
	SmlTargetPtr_t		  target;	  // opt.
	SmlSourcePtr_t		  source;	  // opt.
	SmlPcdataPtr_t		  meta;		  // opt.
	SmlPcdataPtr_t		  noc;		  // opt. (SyncML 1.1)
};


/**
 * Exec command:
 */
struct sml_exec_s
{
	SmlProtoElement_t	  elementType;
	SmlPcdataPtr_t		  cmdID;
	Flag_t				  flags;	  // NoResp
	SmlCredPtr_t		  cred;		  // opt.
	SmlPcdataPtr_t		  meta;		  // opt.
	SmlPcdataPtr_t		  correlator; // opt. (SyncML 1.2)
	SmlItemPtr_t		  item;
};


/**
 * Get and Put command:
 */
struct sml_get_put_s
{
	SmlProtoElement_t	elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		cmdID;
	Flag_t				flags;	  	// NoResp
	SmlPcdataPtr_t		lang;		// opt.
	SmlCredPtr_t		cred;		// opt.
	SmlPcdataPtr_t		meta;		// opt.
	SmlItemListPtr_t	itemList;
};


/**
 * Map command:
 */
struct sml_map_item_s
{
	SmlTargetPtr_t			 target;
	SmlSourcePtr_t			 source;
};

struct sml_map_item_list_s
{
	SmlMapItemPtr_t			mapItem;
	SmlMapItemListPtr_t		next;
};

struct sml_map_s
{
	SmlProtoElement_t	elementType; // InternalToolkit Field
	SmlPcdataPtr_t		cmdID;
	SmlTargetPtr_t		target;
	SmlSourcePtr_t		source;
	SmlCredPtr_t		cred;		  // opt.
	SmlPcdataPtr_t		meta;		  // opt.
	SmlMapItemListPtr_t	mapItemList;
};


/**
 * Results command:
 */
struct sml_results_s
{
	SmlProtoElement_t	elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		cmdID;
	SmlPcdataPtr_t		msgRef;	  // opt.
	SmlPcdataPtr_t		cmdRef;
	SmlPcdataPtr_t		meta;		  // opt.
	SmlPcdataPtr_t		targetRef;  // opt.
	SmlPcdataPtr_t		sourceRef;  // opt.
	SmlItemListPtr_t	itemList;
};


/**
 * Search command:
 */
struct sml_search_s
{
	SmlProtoElement_t	elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		cmdID;
	Flag_t				flags;	  // NoResp, NoResults
	SmlCredPtr_t		cred;		  // opt.
	SmlTargetPtr_t		target;	  // opt.
	SmlSourceListPtr_t	sourceList;
	SmlPcdataPtr_t		lang;		  // opt.
	SmlPcdataPtr_t		meta;
	SmlPcdataPtr_t		data;
};


/**
 * Status command:
 */
struct sml_target_ref_list_s
{
	SmlPcdataPtr_t			targetRef;
	SmlTargetRefListPtr_t	next;
};

struct sml_source_ref_list_s
{
	SmlPcdataPtr_t			sourceRef;
	SmlSourceRefListPtr_t	next;
};

struct sml_status_s
{
	SmlProtoElement_t	  elementType; // Internal Toolkit Field
	SmlPcdataPtr_t		  cmdID;
	SmlPcdataPtr_t		  msgRef; // Opt.
	SmlPcdataPtr_t		  cmdRef;
	SmlPcdataPtr_t		  cmd;
	SmlTargetRefListPtr_t targetRefList;  // opt.
	SmlSourceRefListPtr_t sourceRefList;  // opt.
	SmlCredPtr_t		  cred;			  // opt.
	SmlChalPtr_t		  chal;			  // opt.
	SmlPcdataPtr_t		  data;
	SmlItemListPtr_t	  itemList;		  // opt.
};


/**
 * a little helper for typecasting
 */
struct sml_unknown_proto_element_s
{
	SmlProtoElement_t	elementType;  // Internal Toolkit Field
};

#endif
