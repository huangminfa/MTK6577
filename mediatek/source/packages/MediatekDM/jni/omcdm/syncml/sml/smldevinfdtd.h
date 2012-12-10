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
 *      Name:				smldevinfdtd.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/smldevinfdtd.h#4 $
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
 * SyncML Device Information DTD specific type definitions
 *
 * @target_system   all
 * @target_os       all
 * @description Definition of structures representing DevInf DTD elements
 * This file reflects DevInf as specified in the document
 * http://www.openmobilealliance.org/tech/affiliates/syncml/syncml_devinf_v101_20010615.pdf
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



#ifndef _SML_DEVINFDTD_H
#define _SML_DEVINFDTD_H

/* process only if we really use DevInf DTD */
#ifdef __USE_DEVINF__

/*************************************************************************/
/*  Definitions                                                          */
/*************************************************************************/


#include <syncml/sml/smldef.h>
#include <syncml/sml/smldtd.h>


typedef struct sml_devinf_ext_s {
    SmlPcdataPtr_t      xnam;
    SmlPcdataListPtr_t  xval; /* optional */
} *SmlDevInfExtPtr_t, SmlDevInfExt_t;

typedef struct sml_devinf_extlist_s {
    SmlDevInfExtPtr_t   data;
    struct sml_devinf_extlist_s *next;
} *SmlDevInfExtListPtr_t, SmlDevInfExtList_t;

typedef struct sml_devinf_synccap_s {
    SmlPcdataListPtr_t  synctype;
} *SmlDevInfSyncCapPtr_t, SmlDevInfSyncCap_t;


typedef struct sml_devinf_ctcap11_data_s {
    SmlPcdataPtr_t                name;
    SmlPcdataPtr_t                dname; /* optional, display name */

    SmlPcdataListPtr_t            valenum;
    SmlPcdataPtr_t                datatype;
    SmlPcdataPtr_t                size;
} *SmlDevInfCTCap11DataPtr_t, SmlDevInfCTCap11Data_t;

typedef struct sml_devinf_ctcap11_datalist_s {
    SmlDevInfCTCap11DataPtr_t     data;
    struct sml_devinf_ctcap11_datalist_s *next;
} *SmlDevInfCTCap11DataListPtr_t, SmlDevInfCTCap11DataList_t;

typedef struct sml_devinf_ctcap11_prop_s {
    SmlDevInfCTCap11DataPtr_t     prop;
    SmlDevInfCTCap11DataListPtr_t param;
} *SmlDevInfCTCap11PropPtr_t, SmlDevInfCTCap11Prop_t;

typedef struct sml_devinf_ctcap11_proplist_s {
    SmlDevInfCTCap11PropPtr_t     data;
    struct sml_devinf_ctcap11_proplist_s *next;
} *SmlDevInfCTCap11PropListPtr_t, SmlDevInfCTCap11PropList_t;

typedef struct sml_devinf_ctcap11_s {
    SmlPcdataPtr_t                cttype;
    SmlDevInfCTCap11PropListPtr_t prop;
} *SmlDevInfCTCap11Ptr_t, SmlDevInfCTCap11_t;

typedef struct sml_devinf_ctcap12_param_s {
    SmlPcdataPtr_t                paramname;
    SmlPcdataPtr_t                datatype;    /* optional */
    SmlPcdataListPtr_t            valenum;     /* optional */
    SmlPcdataPtr_t                displayname; /* optional */
} *SmlDevInfCTCap12ParamPtr_t, SmlDevInfCTCap12Param_t;

typedef struct sml_devinf_ctcap12_paramlist_s {
    SmlDevInfCTCap12ParamPtr_t         data;
    struct sml_devinf_ctcap12_paramlist_s *next;
} *SmlDevInfCTCap12ParamListPtr_t, SmlDevInfCTCap12ParamList_t;

typedef struct sml_devinf_ctcap12_prop_s {
    SmlPcdataPtr_t                  propname;
    SmlPcdataPtr_t                  datatype;    /* optional */
    SmlPcdataPtr_t                  maxoccur;    /* optional */
    SmlPcdataPtr_t                  maxsize;     /* optional */
    SmlPcdataListPtr_t              valenum;     /* optional */
    SmlPcdataPtr_t                  displayname; /* optional */
    SmlDevInfCTCap12ParamListPtr_t  param;       /* optional */
    Flag_t                          flags;       /* NoTruncate */
} *SmlDevInfCTCap12PropPtr_t, SmlDevInfCTCap12Prop_t;

typedef struct sml_devinf_ctcap12_proplist_s {
    SmlDevInfCTCap12PropPtr_t     data;
    struct sml_devinf_ctcap12_proplist_s *next;
} *SmlDevInfCTCap12PropListPtr_t, SmlDevInfCTCap12PropList_t;

typedef struct sml_devinf_ctcap12_s {
    SmlPcdataPtr_t                cttype;
    SmlPcdataPtr_t                verct;
    SmlDevInfCTCap12PropListPtr_t prop;
    Flag_t                        flags; /* FieldLevel */
} *SmlDevInfCTCap12Ptr_t, SmlDevInfCTCap12_t;

/*
 * This structure varies considerably between DS v1.1.2 and
 * DS v1.2 and is used in different places in the two versions.
 * Why it wasn't renamed for 1.2 is a mystery but because it
 * wasn't the handling of tokens in the parser requires that
 * the two versions of this structure are overlayed.
 */
typedef struct sml_devinf_ctcap_s {
   /*
    * Which of the two variants of the CTCap structure is
    * being stored.
    */
    SmlVersion_t                version;
    union {
        SmlDevInfCTCap11_t      ctcap11;
        SmlDevInfCTCap12_t      ctcap12;
    } u;
} *SmlDevInfCTCapPtr_t, SmlDevInfCTCap_t;

typedef struct sml_devinf_ctcaplist_s {
    SmlDevInfCTCapPtr_t data;
    struct sml_devinf_ctcaplist_s *next;
} *SmlDevInfCTCapListPtr_t, SmlDevInfCTCapList_t;


typedef struct sml_devinf_dsmem_s {
    Flag_t  flags; /* %%% luz:2003-04-28, mad flag, was PCData (completely wrong) */
    SmlPcdataPtr_t  maxmem; /* optional */
    SmlPcdataPtr_t  maxid;  /* optional */
} *SmlDevInfDSMemPtr_t, SmlDevInfDSMem_t;


typedef struct sml_devinf_xmit_s {
    SmlPcdataPtr_t  cttype;
    SmlPcdataPtr_t  verct;
} *SmlDevInfXmitPtr_t, SmlDevInfXmit_t;

typedef struct sml_devinf_xmitlist_s {
    SmlDevInfXmitPtr_t      data;
    struct sml_devinf_xmitlist_s  *next;
} *SmlDevInfXmitListPtr_t, SmlDevInfXmitList_t;


typedef struct sml_devinf_filtercap_s {
    SmlPcdataPtr_t      cttype;
    SmlPcdataPtr_t      verct;
    SmlPcdataListPtr_t  filterkey;	/* optional */
    SmlPcdataListPtr_t  propname;	/* optional */
} *SmlDevInfFilterCapPtr_t, SmlDevInfFilterCap_t;

typedef struct sml_devinf_filtercaplist_s {
    SmlDevInfFilterCapPtr_t      data;
    struct sml_devinf_filtercaplist_s  *next;
} *SmlDevInfFilterCapListPtr_t, SmlDevInfFilterCapList_t;


typedef struct sml_devinf_datastore_s {
    SmlPcdataPtr_t              sourceref;
    SmlPcdataPtr_t              displayname;   /* optional */
    SmlPcdataPtr_t              maxguidsize;   /* optional */
    SmlDevInfXmitPtr_t          rxpref;
    SmlDevInfXmitListPtr_t      rx;            /* optional */
    SmlDevInfXmitPtr_t          txpref;
    SmlDevInfXmitListPtr_t      tx;            /* optional */
    SmlDevInfCTCapListPtr_t     ctcap;         /* not 1.1 */
    SmlDevInfDSMemPtr_t         dsmem;         /* optional */
    SmlDevInfSyncCapPtr_t       synccap;
    SmlDevInfXmitListPtr_t      filterrx;      /* not 1.1, optional 1.2 */
    SmlDevInfFilterCapListPtr_t filtercap;     /* not 1.1, optional 1.2 */
    Flag_t                      flags;         /* not 1.1 */
} *SmlDevInfDatastorePtr_t, SmlDevInfDatastore_t;


typedef struct sml_devinf_datastorelist_s {
    SmlDevInfDatastorePtr_t data;
    struct sml_devinf_datastorelist_s *next;
} *SmlDevInfDatastoreListPtr_t, SmlDevInfDatastoreList_t;

typedef struct sml_devinf_devinf_s {
    SmlPcdataPtr_t      verdtd;
    SmlPcdataPtr_t      man; /* optional 1.1, compulsary 1.2 */
    SmlPcdataPtr_t      mod; /* optional 1.1, compulsary 1.2 */
    SmlPcdataPtr_t      oem; /* optional */
    SmlPcdataPtr_t      fwv; /* optional 1.1, compulsary 1.2 */
    SmlPcdataPtr_t      swv; /* optional 1.1, compulsary 1.2 */
    SmlPcdataPtr_t      hwv; /* optional 1.1, compulsary 1.2 */
    SmlPcdataPtr_t      devid;
    SmlPcdataPtr_t      devtyp;
    SmlDevInfDatastoreListPtr_t datastore;
    SmlDevInfCTCapListPtr_t     ctcap; /* optional 1.1, not 1.2 */
    SmlDevInfExtListPtr_t       ext;
	/* SCTSTK - 18/03/2002, S.H. 2002-04-05 : SyncML 1.1 */
    Flag_t				flags;
} *SmlDevInfDevInfPtr_t, SmlDevInfDevInf_t;

#endif // __USE_DEVINF__
#endif //_SML_DEVINFDTD_H_
