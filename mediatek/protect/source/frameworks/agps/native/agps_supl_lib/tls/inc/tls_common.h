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
 *   tls_common.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   Header file of tls_common.c
 *
 * Author:
 * -------
 *   Wyatt Sun
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Mar 17 2009 mtk01264
 * [MAUI_01646558] [TLS] Check-in OpenSSL and revise TLS
 * Check in revised TLS
 *
 * Dec 20 2007 mtk01264
 * [MAUI_00593347] [Email] Assert fail: 0 tls_common.c 687 - TLS
 * Remove unused code
 *
 * Dec 17 2007 mtk01264
 * [MAUI_00592313] [TLS] Add new code to guard TLS memory management
 * Add tls_check_mem_leak() to detect memory leak
 *
 * Dec 6 2007 mtk01264
 * [MAUI_00060206] Display application name in the notification screen
 * Change tls_new_ctx() prototype to pass module name in string id
 *
 * Nov 30 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Add memory peak stat
 *
 * Nov 29 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Check in TLS task
 *
 * Oct 21 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Add TLS task
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef _TLS_COMMON_H_
#define _TLS_COMMON_H_

struct tls_mem_log
{
    void *ptr;
    kal_uint32 size;
    kal_uint32 line;
    void* ref;
};

extern const sec_sess_rec tls_global_peer_id;

extern kal_int32 tls_get_ctx_slot(void);
extern void tls_free_ctx_slot(kal_uint8 id);

typedef const ssl_method* (*ssl_method_func)(void);

extern ssl_method_func tls_ssl_ver_to_method(tls_version_enum ver, tls_side_enum side);

extern kal_bool tls_is_valid_auth_mode(tls_auth_mode_enum mode);

extern ssl_auth_mode_enum tls_cvrt_tls2ssl_auth_mode(tls_auth_mode_enum mode);

extern void
tls_cvrt_raw2tls_auth_mode(tls_side_enum side,
                           tls_auth_mode_enum *tls_auth_type,
                           kal_uint8 *raw_auth_type,
                           kal_uint8 len);

extern void
tls_init_conn_ctx(kal_int8 s, module_type mod, tls_side_enum side,
                  tls_context_struct* ctx);

extern void tls_init_ctx(kal_uint8 ctx_id,
                         module_type mod_id,
                         tls_version_enum ver,
                         tls_side_enum side,
                         kal_uint16 app_str_id);

extern kal_int32 tls_validate_ctx(kal_uint8 ctx_id);
extern kal_int32 tls_validate_conn(kal_int8 s);

extern kal_int32 tls_common_new_conn(kal_uint8 ctx_id, kal_int8 s);

extern void tls_common_delete_conn(kal_int8 s);

extern kal_int32 tls_common_set_client_auth(kal_uint8 ctx_id,
                                            tls_auth_mode_enum modes[],
                                            kal_uint8 num);

extern void tls_set_certman_level_passwd_callback(kal_uint8 ctx_id,
                                                  tls_passwd_callback callback,
                                                  void* userdata);

extern void tls_set_ssl_level_passwd_callback(kal_uint8 ctx_id,
                                              tls_passwd_callback callbck,
                                              void* userdata);

extern kal_bool tls_is_assoc_conn_handshaking(kal_uint8 ctx_id);


extern ssl_filetype_enum tls_cvrt_tls2ssl_filetype(tls_filetype_enum type);

extern void tls_delete_assoc_conn(kal_uint8 ctx_id);

extern kal_uint8 tls_parent_ctx_id(kal_int8 s);

extern kal_int32 tls_rw_state_check(tls_conn_context_struct *conn);
extern kal_int32 tls_rw_result_check(kal_int32 result, kal_int8 s);


extern void* tls_malloc(kal_uint32 size, void* ref);
extern void tls_mfree(void* ptr, void* ref);

extern void tls_check_mem_leak(kal_uint8 ctx_id, void *ref);

extern sec_cipher_enum cvrt_cipher_tls2sec(const tls_cipher_enum c);

extern void tls_process_tst_inject_str(kal_uint8 idx, char *inj_str);


#ifndef __MTK_TARGET__
kal_char* ssl_err_code_to_str(kal_int32 ret);
kal_char* tls_err_code_to_str(kal_int32 ret);
#endif /* __MTK_TARGET__ */

#endif /* !_TLS_COMMON_H_ */


