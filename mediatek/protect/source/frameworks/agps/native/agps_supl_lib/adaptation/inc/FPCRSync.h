/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2006
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
 *  FPCRSync.h
 *
 * Project:
 * --------
 *   ALPS
 *
 * Description:
 * ------------
 *   The AGPS SWIP adaption layer.
 *
 * Author:
 * -------
 *
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 03 30 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
 /* data type definition */
#ifndef __FPCRSYNC_H__
#define __FPCRSYNC_H__

//
// Options for FP CR SYNC
//

//[MAUI_02932684] [xp5300]Indoor TTSF values with AGPS is greater than Indoor TTSF values of Stand-Alone GPS
#define _FP_CR_SYNC_MAUI_02932684_    1
// [MAUI_02903559][RRLP] CCBU check fail -- memory leak on error-return case
#define _FP_CR_SYNC_MAUI_02903559_    1
//[MAUI_02964788] [MT6276_ADAPT][NVIOT][EricssonMTL][AGPS][3G] LONGTITUDE IS ALWAYS 0 IN WESTERN
#define _FP_CR_SYNC_MAUI_02964788_    1
//[MAUI_02929319] [MT6276_ADAPT][NVIOT][Andrew][AGPS] TC 2.8 Failed - MS should record GPS position when 2D/3D fixed in SIMB case
#define _FP_CR_SYNC_MAUI_02929319_    1
//[MAUI_02918147][AGPS][SUPL] SUPL refactoring: remove unused code
#define _FP_CR_SYNC_MAUI_02918147__NO_SUPL_GPS_MSG_HDLR    1
//[MAUI_02938153][AGPS][SUPL] only register to l4c when in session
#define _FP_CR_SYNC_MAUI_02938153_            1    // Common part
#define _FP_CR_SYNC_MAUI_02938153__L4C_REG    1    // improve functions for L4C
#define _FP_CR_SYNC_MAUI_02938153__FSM        0    // Ask L4C to provide cell info "at FSM" instead of "at init function"
//[MAUI_03018028] [SUPL] ECID support
#define _FP_CR_SYNC_MAUI_03018028_            1
#define _FP_CR_SYNC_MAUI_03018028__SP         1
#define _FP_CR_SYNC_MAUI_03018028__SP2        0
#define _FP_CR_SYNC_MAUI_03018028__SP3        1

#endif /* __FPCRSYNC_H__ */
