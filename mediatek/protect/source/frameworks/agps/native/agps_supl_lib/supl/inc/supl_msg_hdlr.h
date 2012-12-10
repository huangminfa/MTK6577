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
 *  supl_msg_hdlr.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains function prototypes of following files.
 *   1. supl_mmi_msg_hdlr.c
 *   2. supl_lcsp_msg_hdlr.c
 *   3. supl_gps_msg_hdlr.c
 *
 * Author:
 * -------
 *  Leo Hu
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
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * Apr 18 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _SUPL_MSG_HDLR_H
#define _SUPL_MSG_HDLR_H

#ifndef _SUPL_ULP_HDLR_H
/* error */
#endif
/* MMI messages */
#define supl_mmi_send_status_ind(s_id, status) supl_mmi_send_status_ind_ext(s_id, status, SUPL_MMI_RESULT_OK, SUPL_MMI_CAUSE_NONE)
extern void supl_mmi_send_status_ind_ext(kal_uint16 session_id, kal_uint32 status, kal_uint32 result, kal_uint32 cause);
#if 0  // removed because no one will use it 
extern void supl_mmi_send_notify_ind(kal_uint16 session_id, supl_mmi_notify_struct *notify);
#endif

extern void supl_mmi_msg_hdlr(ilm_struct *ilm_ptr);

/* LCSP messages */
extern void supl_lcsp_send_start_req(kal_uint16 session_id);
extern void supl_lcsp_send_end_req(void);
extern void supl_lcsp_send_data_ind(void *data);
//extern void supl_lcsp_send_data_cnf(kal_uint32 result, kal_uint32 cause );
extern void supl_lcsp_send_data_cnf(kal_bool result);
#if 0 //__ULP_VER_1_5__
extern void supl_lcsp_send_location_id_req(kal_uint16 session_id);
extern void supl_lcsp_send_multi_location_id_ind(kal_uint16 session_id);
#endif
extern void supl_lcsp_msg_hdlr(ilm_struct *ilm_ptr);

/* GPS messages */
#if 0 //__ULP_VER_1_5__
extern void supl_gps_send_get_pos_req(kal_uint16 session_id, void *data);
#endif
#if _FP_CR_SYNC_MAUI_02918147__NO_SUPL_GPS_MSG_HDLR == 0
extern void supl_gps_msg_hdlr(ilm_struct *ilm_ptr);
#endif

/* ULP messages */
extern void supl_ulp_msg_hdlr(ilm_struct *ilm_msg);
extern void supl_ulp_send_supl_start(kal_uint16 session_id);
extern void supl_ulp_send_supl_pos_init(kal_uint16 session_id, supl_mmi_position_struct *pos, kal_uint8 *ver);
extern void supl_ulp_send_supl_pos(kal_uint16 session_id, kal_uint16 payload_length, kal_uint8 *payload);
extern void supl_ulp_send_supl_end(kal_uint16 session_id, supl_ulp_status_enum status, supl_mmi_position_struct *pos);


/* connect messages */
extern void supl_conn_send_create_req(kal_uint16 session_id);
extern void supl_conn_send_send_req(kal_uint16 session_id, void *peer_buf);
extern void supl_conn_send_close_req(kal_uint16 session_id);
extern void supl_conn_msg_hdlr(ilm_struct *ilm_ptr);


#endif /* _SUPL_MSG_HDLR_H */

