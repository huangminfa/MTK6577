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
*  vt_option_cfg.h
*
* Project:
* --------
*   MAUI
*
* Description:
* ------------
*   VT Compile option definition
*
* Author:
* -------
*  SH Yang
*
*==============================================================================
*           HISTORY
* Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 *------------------------------------------------------------------------------
* Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
*==============================================================================
*******************************************************************************/
#ifndef __VT_OPTION_CFG__
#define __VT_OPTION_CFG__

//SH Yang (2009/03/19): add for MODIS H223 Raw data parsing
//Usage: Please set file path to vt_simulated_incoming_file
//and use catcher to inject message, MOD_VT, Index = 255


#define SIM_NONE            0
#define SIM_STK_RX_FILE     1
#define SIM_MD_RX_FILE      2
#define SIM_MD_TX_LOOPBACK  3

#define LB_NONE             0
#define LB_TCV              1
#define LB_NETWORK          2
#define LB_MEDIA            3

/***************************************************************************
  * Debug Switch Macros 
 ***************************************************************************/ 
//#define VT_SIM_MODE                      SIM_NONE // SIM_NONE, SIM_STK_RX_FILE, SIM_MD_RX_FILE, SIM_MD_TX_LOOPBACK
////#define VT_LB_MODE                     LB_MEDIA // LB_NONE, LB_TCV, LB_NETWORK, LB_MEDIA
//#define VT_LB_MODE                     LB_NETWORK // LB_MEDIA is local test
#define VT_LB_MODE	LB_NETWORK
#define VT_SIM_MODE	SIM_NONE

//local file look back
//#define VT_LB_MODE	LB_NONE
//#define VT_SIM_MODE	SIM_STK_RX_FILE

#define VT_TCV_DBG_LOG                          1 // switch debug log on/off
#define VT_STK_DBG_LOG                          0 // switch debug log on/off
#define VT_EVT_DBG_LOG                          0 // enable/disable event debug log
#define VT_TCV_RAND_INACCURATE_TICK             0 // enable/disable random tick drop
//#define VT_TCV_LOOPBACK                         0 // switch vt_tcv loopback on/off
//#define VT_STK_LOOPBACK                LB_NETWORK // switch vt_stk network loopback mode
#define VT_TCV_STK_Q_DBG_LOG                    1 // dbg log for the Q between tcv & stk

//-------- Test Case ---------//
#define VT_TCVRX_TO_FILE      (0)
#define VT_TCVTX_TO_FILE      (0)
#define VT_TCVRX_RAND_INTVL   (0)
#define VT_TCVTX_RAND_INTVL   (0)
#define VT_TCVTX_MIN_INTVL    (5)
//----------------------------//

#endif
