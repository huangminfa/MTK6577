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
 * bt_oppc_hdl.h
 *
 * Project:
 * --------
 *   
 *
 * Description:
 * ------------
 *   Handle OPP client messages in external ADP queue
 *
 * Author:
 * -------
 * Daylong Chen
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __BT_OPPC_HDL_H__
#define __BT_OPPC_HDL_H__

/// Handler function for external dispatcher @see 
void btmtk_oppc_handle_message(void *);


/// Handler Functions for handle_message
//void btmtk_opp_disconnect_ind_handler(void *data);
void btmtk_opps_abort_ind_handler(void *data);
void btmtk_oppc_connect_cnf_handler(void *data);
void btmtk_oppc_abort_cnf_handler(void *data);
void btmtk_oppc_supported_formats_ind_handler(void *data);
void btmtk_oppc_pull_cnf_handler(void * data);
void btmtk_oppc_auth_ind_handler(void *data);
void btmtk_oppc_auth_rsp_handler(void *data);
void btmtk_oppc_push_cnf_handler(void *data);
void btmtk_oppc_internal_rw_handler(void *data);
void btmtk_oppc_disconnect_ind_handler(void *msg);
void btmtk_oppc_register_client_cnf_handler(void *msg);
void btmtk_oppc_inject_msg_handler(void *data);

/// Internal: Utilities
extern S8 * bt_opp_util_get_file_mime_type(U16 *name); ///< Get a MIME(ASCII) by filename

/// OPPC: Routine for exported API and handler
BT_BOOL bt_oppc_conn_server_routine(void); // start to connect the server
FHANDLE bt_oppc_first_pull_routine(U8 *data, U32 len);
BT_BOOL bt_opc_send_mem_routine(void);
BT_BOOL bt_opc_send_mem_continue_routine(goep_push_cnf_struct *pCnf);
BT_BOOL bt_opc_send_file_routine(void);
BT_BOOL bt_opc_send_file_continue_routine(goep_push_cnf_struct *pCnf);

void bt_oppc_send_pull_mem_routine(BT_BOOL bFirstPkt);
BT_BOOL btmtk_oppc_pull_mem_continue_routine(goep_pull_cnf_struct *pCnf);
void bt_oppc_send_pull_vcard_routine(BT_BOOL bFirstPkg);
void btmtk_oppc_pull_file_continue_routine(goep_pull_cnf_struct *pCnf);
void bt_oppc_update_obj_attrib_routine(S8 *path);


void bt_oppc_reset_pull_info(void);
void bt_oppc_reset_push_info(void);
#endif
