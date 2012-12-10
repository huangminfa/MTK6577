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
 *		Name:					omc_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/ssp_if.h#16
 *
 *		Version:				$Id: //depot/main/base/omc/omc_if.h#20 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \file
 *			Since the porting layer is pure binary in nature, the interface
 *			must be strictly procedural. Thus no platform dependent constants,
 *			defines, additional include files etc can appear in the interface.
 *
 * \brief
 *			Defines the external OMC APIs
 */

#ifndef _OMC_OMC_IF_H_
#define _OMC_OMC_IF_H_

/* Parameter or return types of these APIs must either be defined in terms of
 * standard C types or Insignia types. System types (e.g. size_t) should not
 * be used. */

#include <insignia/std.h>

#include <omc/targ_config.h>
#include <omc/omc_config.h>
#include <omc/omc_error.h>

#include <omc/types_if.h>

#include <omc/alloc_if.h>
#include <omc/comms_if.h>
#include <omc/debug_if.h>
#include <omc/device_if.h>
#include <omc/memory_if.h>
#include <omc/mmi_if.h>
#include <omc/stdlib_if.h>
#include <omc/string_if.h>
#include <omc/trigger_if.h>


#ifdef OMADM

#include <omc/tma/tma.h>

#ifdef OMC_TREE_FGS
#include <omc/tree/tstore_if.h>
#endif

#ifdef FUMO
#include <omc/fumo/fumo_mmi_if.h>
#include <omc/fumo/fumo_trigger_if.h>
#ifdef OMADL
#include <omc/dl/omadl_comms_if.h>
#include <omc/dl/omadl_dev_if.h>
#endif
#endif

#endif /* OMADM */


#ifdef OMADS

#include <omc/obj/datastore_if.h>
#include <omc/dsacc/dsacc_if.h>
#include <omc/dsext/dsext_if.h>

#endif /* OMADS */

/*
 * This comes last so that all standard types are defined.
 */
#include <omc/globals_if.h>

#include "../../mdm/common.h"

#endif /* !_OMC_OMC_IF_H_ */
