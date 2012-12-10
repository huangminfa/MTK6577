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
 * bt_pbap_api.h
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
 * Tina Shen
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
#ifndef __BT_PBAP_API_H__
#define __BT_PBAP_API_H__
#include "bt_types.h"

#ifdef BTMTK_WISE_MBT_LOG
	#define PBAP_MBT_LOG(x) MBT_LOG(x) 
	#define PBAP_MBT_LOG1(x, p1)  MBT_LOG1(x, p1) 	
	#define PBAP_MBT_LOG2(x, p1, p2)	 MBT_LOG2(x, p1, p2)	 	
	#define PBAP_MBT_LOG3(x, p1, p2, p3)   MBT_LOG3(x, p1, p2, p3) 		
	#define PBAP_MBT_ERR(x) MBT_ERR(x)	 
#else
	#define PBAP_MBT_LOG(x)  (x) 
	#define PBAP_MBT_LOG1(x, p1)   (x, p1) 	
	#define PBAP_MBT_LOG2(x, p1, p2)	  (x, p1, p2)	 	
	#define PBAP_MBT_LOG3(x, p1, p2, p3)    (x, p1, p2, p3) 		
	#define PBAP_MBT__ERR(x)  (x)
#endif

kal_bool btmtk_pbap_send_active_req(int sockfd, U8 security_level, U8 support_repos, struct sockaddr_un *name, socklen_t namelen);
kal_bool btmtk_pbap_send_deactive_req(int sockfd);
kal_bool btmtk_pbap_send_authorize_rsp(int sockfd,  U8 cnf_code);
kal_bool btmtk_pbap_send_connect_rsp(int sockfd, U32 cm_conn_id,U8 cnf_code);


kal_bool btmtk_pbap_send_disconnect_req(int sockfd,  
									   U32 cm_conn_id,
									   U8  disconnect_tp_directly);

kal_bool btmtk_pbap_send_set_path_rsp(int sockfd,     U8 result);
kal_bool btmtk_pbap_send_read_entry_rsp(int sockfd, U8 result,U8* file_name,
									              U16 name_len);
kal_bool btmtk_pbap_send_read_folder_rsp(int sockfd,
	    U8 result,
    U16 phoneBookSize,
    U16 newMissedCalls,
    U8* file_name, 
    U16 name_len);

kal_bool btmtk_pbap_send_read_list_rsp(int sockfd,  U8 result,
    U16 phoneBookSize,
    U16 newMissedCalls, 
    U8* file_name, 
    U16 name_len);


kal_bool btmtk_pbap_send_obex_auth_challege_rsp(int sockfd, 
													U8 cancel,
													const char* password,  U16 password_length,
													const char* userID, U16 userID_length);
kal_bool btmtk_pbap_server_enable(void);
void btmtk_pbap_server_disable(void);
kal_bool btmtk_pbap_server_authenticate(char *p_password, char *p_userid);
kal_bool btmtk_pbap_server_close(void);
//void btmtk_pbap_wise_server_writedata(T_MBT_PBAP_OP Operation);
#endif

