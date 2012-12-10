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
 *
 * Filename:
 * ---------
 * Bt_adp_hidh.h
 *
 * Project:
 * --------
 *   BT Project
 *
 * Description:
 * ------------
 *   This file is used to 
 *
 * Author:
 * -------
 * Ting Zheng
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision: 
 * $Modtime:
 * $Log: 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/
#ifndef BT_ADP_HIDH_H
#define BT_ADP_HIDH_H

void hidha_init(void);
void hidh_handle_message(ilm_struct *ilm_ptr);
void hidha_app_callback(HidChannel *Channel, HidCallbackParms *Parms);

void btmtk_adp_hidh_handle_activate_req(ilm_struct *ilm_ptr);
void btmtk_adp_hidh_handle_deactivate_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_get_desclist_req(ilm_struct *ilm_ptr);
void btmtk_adp_hidh_handle_connect_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_disconnect_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_reconnect_req(ilm_struct *ilm_ptr);
void btmtk_adp_hidh_handle_control_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_set_report_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_get_report_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_set_protocol_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_get_protocol_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_set_idlerate_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_get_idlerate_req(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_send_output_report(ilm_struct *ilm_ptr);			
void btmtk_adp_hidh_handle_connect_authorize_rsp(ilm_struct *ilm_ptr);			

#endif
