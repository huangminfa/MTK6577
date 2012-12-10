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

 /******************************************************************************
 * Filename:
 * ---------
 *   rrlp_common_headers.h
 *
 * Project:
 * -------- 
 *   MAUI
 *
 * Description:
 * ------------
 *
 * Author:
 * -------
 * Lanslo Yang (mtk01162)
 *
 *-----------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Aug 10 2009 mtk01600
 * [MAUI_01936101] [AGPS 2G CP] AGPS Feature check-in
 * 
 *
 * Sep 11 2008 mtk01600
 * [MAUI_01236204] [MAUI][MONZA][MCD] Remove __MCD__ compile option, since it should be always on for p
 * 
 *
 * Apr 21 2008 mtk01162
 * [MAUI_00759867] [AGPRS] check-in AGPS RRLP part
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 *
 ******************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by MediaTek Inc.
*
********************************************************************************/


#ifndef _RRLP_COMMON_HEADERS_H
#define _RRLP_COMMON_HEADERS_H

#ifndef __AGPS_SWIP_REL__
#include "kal_release.h"      	/* Basic data type */
#include "stack_common.h"
#include "stack_msgs.h"
#include "app_ltlcom.h"       	/* Task message communiction */
#include "stack_ltlcom.h"
#include "syscomp_config.h"
#include "task_config.h"      	/* Task creation */
#include "stacklib.h"        	/* Basic type for dll, evshed, stacktimer */
#include "event_shed.h"       	/* Event scheduler */
#include "stack_timer.h"      	/* Stack timer */
#include "stack_utils.h"
#include "app_buff_alloc.h"
#include "data_buff_pool.h"
#include "kal_trace.h"
#include "rrlp_trc.h"
#else
#include "typedef.h"
#include "sys_serv_adp.h"
#include "mtk_service.h"
#include "rrlp_trc.h"
#endif

#ifndef __AGPS_SWIP_REL__
#include "bitop_macros.h"
#include "bitstream.h"
#endif

#include "rrlp_mem.h"

typedef enum
{
    /* Downlink messages */
    RRLP__MEAS_POS_REQ_WITH_POS_REQ_MB    ,  /* 0 */
    RRLP__MEAS_POS_REQ_WITH_MEAS_REQ_MA   ,  /* 1 */
    RRLP__ASSIST_DATA                     ,  /* 2 */

    /*Uplink messages */
    RRLP__MEAS_POS_RSP_WITH_POS_RSP       ,  /* 3 */
    RRLP__MEAS_POS_RSP_WITH_MEAS_RSP      ,  /* 4 */
    RRLP__MEAS_POS_RSP_WITH_LOC_ERR       ,  /* 5 */     
    RRLP__ASSIST_DATA_ACK                 ,  /* 6 */
    RRLP__PROTOCOL_ERR                       /* 7 */
} rrlp_peer_msg_name_enum;

#ifdef __MTK_TARGET__
#define RRLP_ON_TARGET KAL_TRUE
#else 
#ifdef __AGPS_SWIP_REL__ 
#define RRLP_ON_TARGET KAL_TRUE /* Set to KAL_FALSE if want to UT SWIP */
#else
#define RRLP_ON_TARGET KAL_FALSE
#endif
#endif

#endif /* _RRLP_COMMON_HEADERS_H */
