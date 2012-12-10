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

/*******************************************************************************
 * Filename:
 * ---------
 *	smsal_l4c_defs.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains the defined constants which related to 
 *   interface of SMSAL and L4C.
 *
 * Author:
 * -------
 *	Kevin Chien
 *
 *==============================================================================
 * 				HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 10 13 2010 yuming.hsu
 * [MAUI_02827360] [Maui][MPM] check in MMI Protocol Modulation revise
 * [MPM] SMS part
 *
 * Jul 8 2010 mtk02589
 * [MAUI_02579988] [SMS][Update] Remove __MTK_3G_MRAT_ARCH__ compile option & unused code
 * 
 *
 * Sep 4 2009 mtk02589
 * [MAUI_01701424] [SMSAL] [Revise] CB Channel only stored in NVRAM
 * 
 *
 * Sep 5 2008 mtk02219
 * [MAUI_01233608] [SMSAL][Update] CBS-DCS up to 30 entity and consider USIM EFli file
 * 
 *
 * Aug 11 2008 mtk01202
 * [MAUI_00819881] [Monza][CB] 2G CB checkin
 * 
 *
 * Feb 22 2008 mtk01202
 * [MAUI_00584886] [R4R5] sms checkin
 * 
 *
 * Sep 7 2007 mtk01202
 * [MAUI_00533677] [SMS] Merge from MONZA to MAUI
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by Sasken Communication Technologies Limited.
*
********************************************************************************/

#ifndef _SMSAL_L4C_DEFS_H
#define _SMSAL_L4C_DEFS_H

#include "ps_public_enum.h"     //mtk02589 MPM

#define SMSAL_PAGE_SIZE             (40)        /* 40 messages/page */                                              
#define SMSAL_ONE_MSG_LEN           (160)
#define SMSAL_ADDR_LEN              (11)
#define SMSAL_ME_ADDR_LEN           (21)
#define SMSAL_INVALID_INDEX         (0xffff)
#define SMSAL_MAX_TPDU_SIZE         (175)


/* 
 * !! Attention !!
 *
 * the following constants are related to CB : 
 * the CB in RMC/MMI MUST be consistent with this defined values !! 
 *
 */

#include "l3_inc_enums.h"

#define SMSAL_CB_PER_7BIT_LEN          (93)
#define SMSAL_CB_PER_UCS2_LEN          (88)
#define SMSAL_CB_PER_UCS2_CONT_LEN     (82)


#define SMSAL_PS_CBMI_MAX_ENTRY        (60)
#if defined(__CB_CHANNEL_ONLY_STORED_IN_NVRAM__)
#define SMSAL_CBMI_SIM_ENTRY           (0)
#else
#define SMSAL_CBMI_SIM_ENTRY           (20)
#endif
#if defined(__CB_CHANNEL_ONLY_STORED_IN_NVRAM__)
#define SMSAL_CBMI_ME_ENTRY            (40)
#else
#define SMSAL_CBMI_ME_ENTRY            (20)
#endif
#define SMSAL_DEFAULT_CBMI_ENTRY       (10)
#define SMSAL_DEFAULT_DCS_ENTRY        (21)  /* CBS DCS 20 language + unspecified (other language) */

#define SMSAL_MMI_CBMI_MAX_ENTRY       (SMSAL_CBMI_SIM_ENTRY + SMSAL_CBMI_ME_ENTRY)
#define SMSAL_MMI_DCS_MAX_ENTRY        (SMSAL_CB_MAX_ENTRY)

#define SMSAL_CB_UNUSED_CBMI        (0xffff) /* unused CBMIR 0xffffffff */
#define SMSAL_CB_UNUSED_DCS         (0x0080) /* one byte */
#define SMSAL_CB_UNUSED_ISO639      (0xffff) /* two byte */

#define SMSAL_CB_HOMEZONE_MI        (0x00dd) /* HomeZone Message ID 221 */
#define SMSAL_CB_HOMEZONE_SN        (0x0010) /* HomeZone Series Number */
#define SMSAL_CB_HOMEZONE_DCS       (0x0000) /* HomeZone DCS */

#endif /* _SMSAL_L4C_DEFS_H */


