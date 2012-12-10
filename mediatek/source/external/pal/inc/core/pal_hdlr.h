/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
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
 *  pal_wndrv_hdlr.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL WNDRV Message handler Definition
 *
 * Author:
 * -------
 *  Saker Hsia (mtk02327)
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/
#ifndef _PAL_HDLR_H_
#define _PAL_HDLR_H_

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
#include "mtkpal_porting.h"

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/
#define PAL_SEND_MSG_TO_WNDRV( _ilm_ptr )                           \
{                                                                   \
    SEND_ILM( MOD_PAL, MOD_WNDRV, PAL_WNDRV_SAP, _ilm_ptr );        \
}

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/


/*******************************************************************************
*                  F U N C T I O N   D E C L A R A T I O N S
********************************************************************************
*/
/* pal_wndrv_hdlr.c */
extern void pal_wndrv_mac_start_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_mac_connect_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_mac_connect_fail_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_mac_disconnect_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_mac_cancel_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_data_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_data_block_free_num_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_query_status_ind_handler(ilm_struct *ilm_ptr);
extern void pal_wndrv_sync_tsf_ind_handler(ilm_struct *ilm_ptr);
extern void pal_send_wndrv_req ( void* msg, UINT32 type );	// send wndrv req message
extern void pal_send_wndrv_add_key_req(local_para_struct *local_para_ptr);

/* pal_bt_hci_hdlr.c */
extern UINT32 pal_bt_get_sap_size (UINT32 type);
extern void pal_bt_read_local_version_info_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_local_amp_info_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_reset_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_data_block_size_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_link_quality_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_set_event_mask_page2_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_rssi_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_local_amp_assoc_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_write_remote_amp_assoc_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_create_physical_link_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_accept_physical_link_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_link_supervision_timeout_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_disconnect_physical_link_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_write_link_supervision_timeout_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_create_logical_link_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_accept_logical_link_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_flow_spec_modify_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_disconnect_logical_link_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_logical_link_cancel_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_read_logical_link_accept_timeout_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_write_logical_link_accept_timeout_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_enhanced_flush_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_short_range_mode_command_handler (ilm_struct *ilm_ptr);
extern void pal_bt_data_command_handler (ilm_struct *ilm_ptr);
extern void pal_send_hci_event ( void* msg, UINT32 type );	// send hci req message

extern FILE* pal_set_hci_log_dump( UINT8* );
extern void pal_clear_hci_log_dump( void );

/* pal_inject_msg.c */
extern void pal_tst_inject_string_handler(ilm_struct * ilm_ptr);
#endif /* _PAL_HDLR_H_ */
