/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE. 
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

/*****************************************************************************
 *
 * Filename:
 * ---------
 *   kal_non_specific_general_types.h
 *
 * Project:
 * --------
 *   Maui_Software
 *
 * Description:
 * ------------
 *   This file provides general types definations that are not specific
 *   to any os
 *
 * Author:
 * -------
 *   Rex   Luo  (mtk00389)
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:   1.9  $
 * $Modtime:   May 16 2005 23:14:04  $
 * $Log:   //mtkvs01/vmdata/Maui_sw/archives/mcu/kal/include/kal_non_specific_general_types.h-arc  $
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Jan 2 2007 mtk01587
 * [MAUI_00355761] [init][AddFeature] Dynamic switch stack
 * Add kal_func_ptr
 *
 * Oct 31 2006 mtk00702
 * [MAUI_00339559] [kal][AddFeature] THREADX porting
 * add THREADX-related code
 *
 * May 10 2006 mtk00681
 * [MAUI_00187945] [MoDIS] Add new data type to support Monza
 * 
 *
 * May 4 2006 mtk00681
 * [MAUI_00189674] [MoDIS] Change definition of kal_bool from enum to unsigned char
 * 
 *
 * Apr 24 2006 mtk00681
 * [MAUI_00189674] [MoDIS] Change definition of kal_bool from enum to unsigned char
 * 
 *
 * Apr 17 2006 mtk00681
 * [MAUI_00187945] [MoDIS] Add new data type to support Monza
 * 
 *
 * Mar 1 2006 mtk00681
 * [MAUI_00176489] [KAL] Provide setjmp buffer typedef
 * 
 *
 * Feb 21 2006 mtk00681
 * [MAUI_00174618] [MoDIS] Fix codegen error
 * 
 *
 * Feb 17 2006 mtk00681
 * [MAUI_00173954] [MoDIS] Unify system service compile option, kal_printf and enhance timer
 * 
 *
 * Dec 1 2005 mtk00681
 * [MAUI_00159148] [MoDIS] Update system service to support Misc. features
 * 
 * 
 *    Rev 1.9   May 17 2005 00:32:58   BM_Trunk
 * Karlos:
 * add copyright and disclaimer statement
 * 
 *    Rev 1.8   Jan 18 2005 00:37:06   BM
 * append new line in W05.04
 * 
 *    Rev 1.7   May 13 2004 13:02:38   mtk00702
 * 1. Define KAL_AND as NU_AND
 * 2. Define KAL_CONSUME as NU_OR_CONSUME
 * 
 *    Rev 1.6   Aug 27 2003 09:33:40   mtk00576
 * Type ULONG64 and LONG64 are illegal types in MNT, using unsigned __int64 and __int64 instead.
 * Resolution for 2661: [KAL][Add Feature]New data type kal_uint64 added for future uasge.
 * 
 *    Rev 1.5   Aug 25 2003 12:13:36   mtk00576
 * MNT supports long long (8B).
 * Resolution for 2548: [kal][Add Feature]Adding 8B unsigned and signed data type.
 * 
 *    Rev 1.4   Aug 20 2003 14:27:18   mtk00576
 * Protect the data type long long with compiler option "WIN32".
 * Resolution for 2590: Data type "long long" is illegal data type in MNT.
 * 
 *    Rev 1.3   Aug 14 2003 15:36:36   mtk00576
 * Define new data type.
 * Resolution for 2548: [kal][Add Feature]Adding 8B unsigned and signed data type.
 * 
 *    Rev 1.2   Jun 26 2003 18:23:26   mtk00576
 * Adding generic wide-character utilities.
 * Resolution for 2124: [kal] [Enhance] Adding generic wide-character utilities.
 * 
 *    Rev 1.1   03 Dec 2002 14:17:00   mtk00389
 * modify kal_bool type
 * 
 *    Rev 1.0   Nov 30 2002 19:50:10   admin
 * Initial revision.
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

/*********************************************************************
   (C) _____ (year of first publication) Sasken communication
   Technologies Limited, All rights reserved.
*   This file provides a template for .c files. This space
*   should be used to describe the file contents
*   Component-specific prefix : xxxx
*********************************************************************/

#ifndef _KAL_NON_SPECIFIC_GENERAL_TYPES_H
#define _KAL_NON_SPECIFIC_GENERAL_TYPES_H

#include "typedef.h"

#if (0)

/*******************************************************************************
 * Type Definitions
 *******************************************************************************/
typedef unsigned char           kal_uint8;
typedef signed char             kal_int8;
typedef char                    kal_char;
typedef unsigned short          kal_wchar;

typedef unsigned short int      kal_uint16;
typedef signed short int        kal_int16;

typedef unsigned int            kal_uint32;
typedef signed int              kal_int32;

#if !defined(GEN_FOR_PC) && !defined(__MTK_TARGET__)
   //typedef ULONG64              kal_uint64;
   //typedef LONG64               kal_int64;
   typedef unsigned __int64      kal_uint64;
   typedef __int64               kal_int64;
#else
   typedef unsigned long long   kal_uint64;
   typedef signed long long     kal_int64;
#endif

#if !defined(__MTK_TARGET__)
   typedef int kal_jmpbuf[64];
#elif defined(__RVCT__)   
   typedef long long kal_jmpbuf[48];
#else
   typedef int kal_jmpbuf[32];
#endif

typedef enum 
{
  KAL_FALSE,
  KAL_TRUE
} kal_bool;

typedef void (*kal_func_ptr)(void);

/*******************************************************************************
 * Constant definition
 *******************************************************************************/
#ifndef NULL
#define NULL               0
#endif

#if defined(KAL_ON_NUCLEUS)

#define KAL_AND               NU_AND
#define KAL_CONSUME           NU_OR_CONSUME
#define KAL_AND_CONSUME       NU_AND_CONSUME
#define KAL_NO_SUSPEND        NU_NO_SUSPEND
#define KAL_OR                NU_OR
#define KAL_OR_CONSUME        NU_OR_CONSUME
#define KAL_SUSPEND           NU_SUSPEND

#elif defined (KAL_ON_OSCAR)    /* KAL_ON_NUCLEUS */

#define KAL_AND               OSC_ACTION_FULL_SET
#define KAL_CONSUME           OSC_ACTION_CLS
#define KAL_AND_CONSUME       OSC_ACTION_FULL_SET | OSC_ACTION_CLS
#define KAL_NO_SUSPEND        OSC_TIMEOUT_NONE
#define KAL_OR                OSC_ACTION_PART_SET
#define KAL_OR_CONSUME        OSC_ACTION_PART_SET | OSC_ACTION_CLS
#define KAL_SUSPEND           OSC_TIMEOUT_FOREVER

#elif defined(KAL_ON_THREADX)   /* KAL_ON_NUCLEUS */

#define KAL_AND               TX_AND
#define KAL_CONSUME           TX_OR_CLEAR
#define KAL_AND_CONSUME       TX_AND_CLEAR
#define KAL_NO_SUSPEND        TX_NO_WAIT
#define KAL_OR                TX_OR
#define KAL_OR_CONSUME        TX_OR_CLEAR
#define KAL_SUSPEND           TX_WAIT_FOREVER

#endif  /* KAL_ON_NUCLEUS */


#endif
#endif  /* _KAL_NON_SPECIFIC_GENERAL_TYPES_H */


