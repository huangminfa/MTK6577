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

/*******************************************************************************
 * Filename:
 * ---------
 *  supl_fsm.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains function prototypes of supl_fsm.c.
 *
 * Author:
 * -------
 *  Leo Hu
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
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * May 19 2008 mtk00563
 * [MAUI_00763856] [AGPS] revise SUPL trace info.
 * 
 *
 * Apr 18 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/

#ifndef _SUPL_FSM_H
#define _SUPL_FSM_H
//$$
#include "mtk_service.h"

extern void supl_session_init(void);
extern void supl_session_reset(kal_uint16 session_id);
extern kal_int16 supl_session_take(kal_uint16);

extern void supl_session_enqueue(supl_event_enum event, kal_uint16 session_id, void *param);
extern void supl_session_dequeue(kal_bool execute);
extern kal_bool supl_session_if_preempt(kal_uint16 session_id);

kal_bool supl_fsm_if_in_process(kal_uint32 *session_type, kal_uint16 *session_id);

extern kal_bool supl_fsm_if_need_send_cnf(kal_uint16 session_id);

extern void supl_fsm_terminated_hdlr (kal_uint32 event, kal_uint16 session_id, void *data);

extern void supl_fsm_func(supl_event_enum event, kal_uint16 session_id, void *data);

extern void supl_conn_reset(kal_uint16 index);
extern void supl_conn_fsm_func(supl_conn_event_enum event, kal_uint16 session_id, void *data);

extern void supl_send_int_msg(kal_uint32 src_id, kal_uint32 dest_id, msg_type msg_id, local_para_struct *local_para_ptr, peer_buff_struct *peer_buff_ptr);
extern kal_uint32 supl_fsm_get_curr_state(kal_uint16 session_id);

extern void supl_util_override_filter(kal_char *filter_str, kal_uint16 len);
extern void supl_util_reset_filter(void);

#if _FP_CR_SYNC_MAUI_02938153__FSM != 0
    extern void supl_fsm_handle_to_send_msg(supl_sim_id_enum sim_id);
#endif

#endif /* _SUPL_FSM_H */
