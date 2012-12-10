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
 *   tls_trc.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   This is trace map definition.
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
 * 10 11 2011 archilis.wang
 * [ALPS00076515] [GIN-Dual-SIM][SUPL] Fail to generate default H-SLP address with SIM IMSI after TLS handshake failure.
 * .
 *
 * 09 21 2011 archilis.wang
 * [ALPS00066919] [GIN-Dual-SIM] [SUPL] SET can't authenticate the server via checking the server's certificate.
 * .
 *
 * Dec 5 2009 mtk01264
 * [MAUI_02007486] [TLS] Add timestamp trace for invalid certificate
 * Add trace during processing invalid certificate
 *
 * Nov 24 2009 mtk01264
 * [MAUI_01996819] [OCSP] Check-in OCSP for OpenSSL solution
 * Enable OCSP support in TLS handshake
 *
 * Mar 17 2009 mtk01264
 * [MAUI_01646558] [TLS] Check-in OpenSSL and revise TLS
 * Check in revised TLS
 *
 * Apr 15 2008 mtk01264
 * [MAUI_00756365] [TLS] inject string to enable/disable logging messages in plaintext
 * Controlling log plaintext by string injection
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
 * Display memory stats
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
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef _TLS_TRC_H_
#define _TLS_TRC_H_

#ifndef GEN_FOR_PC


#ifndef __AGPS_SWIP_REL__

#ifndef _STACK_CONFIG_H
    #error  "stack_config.h should be included before tst_config.h"
#endif /* _STACK_CONFIG_H */

#endif /* __AGPS_SWIP_REL__ */

#else /* GEN_FOR_PC */ 
#include "kal_trace.h"
#endif /* GEN_FOR_PC */ 

#ifndef __AGPS_SWIP_REL__

#ifndef _KAL_TRACE_H
    #error "kal_trace.h should be included before tst_trace.h"
#endif /* _KAL_TRACE_H */

#endif /* __AGPS_SWIP_REL__ */

#define TRACE_TLS_DATA_IO         (TRACE_GROUP_1)
#define TRACE_TLS_MEMORY          (TRACE_GROUP_5)

#if 0
BEGIN_TRACE_MAP(MOD_TLS)

    /* ----------------- TRACE_FUNC trace class ------------------- */
    /* tls_main.c */

    /* tls_api.c */
    TRC_MSG(FUNC_TLS_NEW_CTX, "tls_new_ctx(ver=%d, side=%Mtls_side_enum, app_str_id=%u)")
    TRC_MSG(FUNC_TLS_DELETE_CTX, "tls_delete_ctx(ctx_id=%d)")
    TRC_MSG(FUNC_TLS_SET_CIPHER, "tls_set_cipher(ctx_id=%d, num=%d)")
    TRC_MSG(FUNC_TLS_SET_VERIFY, "tls_set_verify(ctx_id=%d, num=%d, callback=0x%x, callback_arg=0x%x)")
    TRC_MSG(FUNC_TLS_SET_CLIENT_AUTH, "tls_set_client_auth(ctx_id=%d, num=%d)")
    TRC_MSG(FUNC_TLS_SET_PASSWD_CALLBACK, "tls_set_passwd_callback(ctx_id=%d, callback=0x%x, userdata=0x%x)")
    TRC_MSG(FUNC_TLS_SET_IDENTITY, "tls_set_identity(ctx_id=%d, cert_id=%d)")
    TRC_MSG(FUNC_TLS_NEW_CONN, "tls_new_conn(ctx_id=%d, sock_id=%d)")
    TRC_MSG(FUNC_TLS_DELETE_CONN, "tls_delete_conn(sock_id=%d)")
    TRC_MSG(FUNC_TLS_AUTO_REHANDSHAKE, "tls_auto_rehandshake(sock_id=%d, onoff=%Mkal_bool)")
    TRC_MSG(FUNC_TLS_HANDSHAKE,  "tls_handshake(sock_id=%d)")
    TRC_MSG(FUNC_TLS_REHANDSHAKE,  "tls_rehandshake(sock_id=%d)")    
    TRC_MSG(FUNC_TLS_READ, "tls_read(sock_id=%d, len=%d)")
    TRC_MSG(FUNC_TLS_WRITE, "tls_write(sock_id=%d, len=%d)")
    TRC_MSG(FUNC_TLS_SHUTDOWN, "tls_shutdown(sock_id=%d)")
    TRC_MSG(FUNC_TLS_CONNECT, "tls_connect(sock_id=%d) to %u.%u.%u.%u:%u")

    /* tls_handshake.c */

    /* tls_socket.c */

    /* tls_app.c */

    /* tls_fs.c */

    /* tls_certman.c */

    /* tls_mmi_certman.c */

    /* tls_common.c */
    TRC_MSG(TLS_MALLOC, "tls_malloc(size=%u)")
    TRC_MSG(TLS_MALLOC_FAIL, "alloc memory fail, req=%u, left=%u")
    TRC_MSG(TLS_MALLOC_OK, "allocated %u bytes, ptr=0x%x, from %u bytes, %u bytes left")
    TRC_MSG(TLS_MFREE, "tls_free()")
    TRC_MSG(TLS_MFREE_OK, "tls_free(ptr=0x%x, size=%d)")

   

    /* ----------------- TRACE_STATE trace class ------------------- */
    TRC_MSG(TLS_STATE, "socket_id=%d state=%Mtls_state_enum")
    TRC_MSG(TLS_STATE_TRANSITION, "TLS socket %d transit from %Mtls_state_enum to %Mtls_state_enum")

    /* ----------------- TRACE_INFO trace class ------------------- */
    TRC_MSG(INFO_RECV_EXT_Q, "got message from %Mmodule_type")
    TRC_MSG(RCVD_INVALID_CERT_RSP, "invalid_cert RSP: xid=0x%x, result=%d, action=%d")
    TRC_MSG(RCVD_SELECT_USER_CERT_RSP, "user_cert RSP: xid=0x%x, result=%d, user_cert=%d")
    TRC_MSG(TLS_NEW_CTX_RETURN, "tls_new_ctx() returns %d")
    TRC_MSG(TLS_READ_BYTES, "tls_read() success, return %d bytes")
    TRC_MSG(TLS_WRITE_RETURN, "tls_write(%d) returns %d")
    TRC_MSG(TLS_VERIFY_CALLBACK_EXT_ERR, "conn %d cert verify, x509->error=0x%x")
    TRC_MSG(TLS_VERIFY_CALLBACK_RETURN, "default verify callback returns %d")
    TRC_MSG(TLS_HANDSHAKE_RETURN, "TLS invokes ssl_Handshake(%d) returns 0x%x")
    TRC_MSG(TLS_WRITE_INVOKES_SEC_SSL_WRITE_RETURN, "sec_ssl_write(%d) returns %d, detail=%d")
    TRC_MSG(TLS_WRITE_WANT_WRITE, "TLS conn %d want write")
    TRC_MSG(TLS_MEM_PEAK_SIZE, "TLS peak memory allocated %d bytes")
    TRC_MSG(TLS_MEM_CURR_SIZE, "TLS current memory allocated %d bytes")
    TRC_MSG(TLS_LOG_PLAINTEXT, "TLS log plaintext %Mkal_bool")
    TRC_MSG(TLS_LOG_PLAINTEXT_ERROR, "Use \"log_text on|off\" to log plaintext")
    TRC_MSG(TLS_SIM_SUPC_PRIVKEY_ORIG, "TLS: TLS private_key (before alloc): %x")
    TRC_MSG(TLS_SIM_SUPC_PRIVKEY_ENCODED, "TLS: TLS private_key (after alloc): %x")
    TRC_MSG(TLS_SIM_SUPC_PRIVKEY_ENCODE_FAILED, "Decrypt private key from file to file failed, code : %d")
    TRC_MSG(TLS_CURRENT_TIME, "Current time: %4d/%2d/%2d %2d:%2d%2d")


    /* ----------------- TRACE_WARNING trace class ------------------- */
    TRC_MSG(TLS_TRIM_VERSION, "trim version from %d to %d")
    TRC_MSG(TLS_IS_BLOCKING_SOCKET, "associate conn ctx to blocking socket %d")
    TRC_MSG(TLS_FW_NOT_SOCKET_NOTIFY, "give up forward a socket msg %Mmsg_type")
    TRC_MSG(TLS_DROP_SOCKET_NOTIFY, "discard a socket notify to socket %d")
    TRC_MSG(TLS_DELETE_HANGING_CONN, "delete hanging connection context %d")
    TRC_MSG(TLS_WARN_OVERWRITE_CA_PATH, "overwrite ca_path of context %d")
    TRC_MSG(CERTMAN_CERT_ID_CNF_WRONG_CERT_TYPE, "wrong cert id cnf type: xid=0x%x, cert_type=0x%x")
    TRC_MSG(CERTMAN_CERT_CHAIN_CNF_WRONG_CERT_TYPE, "wrong cert chain cnf type: xid=0x%x, cert_type=0x%x")
    TRC_MSG(CERTMAN_PRIVKEY_CNF_WRONG_CERT_TYPE, "wrong private key cnf type: xid=0x%x, cert_type=0x%x")
    TRC_MSG(TLS_READ_ERROR, "[TLS] tls_read() failed, return 0x%x")
    TRC_MSG(TLS_SKIP_HANDSHAKE_DONE, "conn(%d) waiting for user response, skip sending handshake done")
    TRC_MSG(TLS_RCVD_INVALID_CERT, "conn(%d) received INVALID CERTIFICATE!")
    TRC_MSG(TLS_SKIP_RCVD_INVALID_CERT, "TLS skips handling conn(%d) INVALID CERTIFICATE!")
    TRC_MSG(TLS_RCVD_INVALID_CERT_WAIT_USER_RSP, "TLS notify user for handling conn(%d) INVALID CERTIFICATE!")
    TRC_MSG(TLS_RCVD_CLIENT_AUTH, "conn(%d) received CLIENT AUTHENTICATION!")
    TRC_MSG(TLS_SKIP_RCVD_CLIENT_AUTH, "TLS skips handling conn(%d) CLIENT AUTHENTICATION!")
    TRC_MSG(TLS_RCVD_CLIENT_AUTH_WAIT_USER_RSP, "TLS notify user for handling conn(%d) CLIENT AUTHENTICATION!")
    TRC_MSG(WARN_CHECK_PEER_NAME_NOT_SUPPORTED, "_CHECK_PEER_CNAME_ is NOT defined in TLS, check peer name ignored")

    /* ----------------- TRACE_ERROR trace class ------------------- */
    TRC_MSG(ERROR_NONSUPPORT_MSG, "non-support ILM message: message id=%Mmsg_type")
    TRC_MSG(TLS_TOO_MANY_CIPHERS, "ciphers exceeds limit: given %d >= %d)")
    TRC_MSG(TLS_INVALID_CIPHER, "invalid cipher id %d found at ciphers[%d]")
    TRC_MSG(TLS_TOO_MANY_CLIENT_AUTH_MODES, "auth modes exceeds limit: given %d >= %d)")
    TRC_MSG(TLS_INVALID_AUTH_MODE, "invalid client auth mode %d found at mode[%d]")
    TRC_MSG(TLS_ACCESS_INVALID_CTX, "context %d is invalid")
    TRC_MSG(TLS_MODULE_NOT_MATCH, "context owned by %Mmodule_type accessed by %Mmodule_type")
    TRC_MSG(TLS_ACCESS_INVALID_CONN, "connection context %d is invalid")
    TRC_MSG(TLS_ACCESS_INVALID_SOCKET, "socket id %d is invalid")
    TRC_MSG(TLS_CERT_XID_NOT_FOUND, "no matched xid for certman cnf %d")
    TRC_MSG(ERROR_NONSUPPORT_CERTMAN_MSG, "invalid msg %Mmsg_type from CERTMAN")
    TRC_MSG(ERROR_NONSUPPORT_MMI_CERTMAN_MSG, "invalid msg %Mmsg_type from MMI_CERTMAN")
    TRC_MSG(INVALID_CERT_RSP_MATCH_NO_CONN, "no conn ctx matched for invalid cert rsp xid=0x%x")
    TRC_MSG(CLIENT_AUTH_RSP_MATCH_NO_CONN, "no conn ctx matched for client auth rsp xid=0x%x")
    TRC_MSG(INVALID_CERT_RSP_WRONG_STATE,  "conn ctx %d rcvd but not waiting invalid cert rsp ")
    TRC_MSG(CLIENT_AUTH_RSP_WRONG_STATE,  "conn ctx %d rcvd but not waiting client auth rsp ")
    TRC_MSG(CERTMAN_MSG_NO_MATCH_CTX, "no matching ctx for message with xid=0x%x from CERTMAN")
    TRC_MSG(TLS_NEW_CTX_INVALID_SIDE, "tls_new_ctx() with invalid side=0x%x")
    TRC_MSG(TLS_NEW_CTX_INVALID_VER, "tls_new_ctx() with invalid version=0x%x")
    TRC_MSG(TLS_NEW_CTX_ASYM_MEM_CB, "tls_new_ctx() with asymmetric memory clalback, malloc=0x%x, mfree=0x%x")
    TRC_MSG(TLS_GET_CTX_SLOT_FAIL, "tls_get_ctx_slot() failed, all slots are occupied")
    TRC_MSG(TLS_SEC_SSL_CTX_NEW_FAILED, "sec_ssl_ctx_new() failed")
    TRC_MSG(TLS_SEC_SSL_CTX_SET_PRNG_FAILED, "sec_ssl_ctx_set_prng() failed")
    TRC_MSG(TLS_NEW_CONN_CTX_NO_MEMORY, "malloc connection context failed, ctx_id=%d, s=%d")
    TRC_MSG(TLS_TASK_NOT_READY, "TLS is not ready (waiting for CERTMAN cnf msg)")
    TRC_MSG(TLS_RCVD_ALERT, "connection(%d) received alert, level=%Mtls_alert_level_enum, desc=%Mssl_error_enum")   
    TRC_MSG(TLS_LOAD_PERSONAL_CERT_FAIL, "global_ctx %d load personal certificate (%d) failed, ret=0x%x")
    TRC_MSG(TLS_LOAD_ROOT_CA_FAIL, "global_ctx %d load trusted Root CA failed, ret=0x%x")
    TRC_MSG(TLS_SET_DEFAULT_CIPHER_LIST_FAIL, "tls_set_default_cipher_list(ctx=%d) failed, ret=0x%x")
    TRC_MSG(TLS_USE_PRIVATEKEY_FILE_FAIL, "global_ctx %d load private key (%d) failed, ret=0x%x")
    TRC_MSG(TLS_OCSP_VERIFY_CERT_FAIL, "certman_ocsp_verify_cert() returns %d")

END_TRACE_MAP(MOD_TLS)
#else
#define FUNC_TLS_NEW_CTX "tls_new_ctx(ver=%d, side=%dtls_side_enum, app_str_id=%u)"
#define FUNC_TLS_DELETE_CTX "tls_delete_ctx(ctx_id=%d)"
#define FUNC_TLS_SET_CIPHER "tls_set_cipher(ctx_id=%d, num=%d)"
#define FUNC_TLS_SET_VERIFY "tls_set_verify(ctx_id=%d, num=%d, callback=0x%x, callback_arg=0x%x)"
#define FUNC_TLS_SET_CLIENT_AUTH "tls_set_client_auth(ctx_id=%d, num=%d)"
#define FUNC_TLS_SET_PASSWD_CALLBACK "tls_set_passwd_callback(ctx_id=%d, callback=0x%x, userdata=0x%x)"
#define FUNC_TLS_SET_IDENTITY "tls_set_identity(ctx_id=%d, cert_id=%d)"
#define FUNC_TLS_NEW_CONN "tls_new_conn(ctx_id=%d, sock_id=%d)"
#define FUNC_TLS_DELETE_CONN "tls_delete_conn(sock_id=%d)"
#define FUNC_TLS_AUTO_REHANDSHAKE "tls_auto_rehandshake(sock_id=%d, onoff=%dkal_bool)"
#define FUNC_TLS_HANDSHAKE  "tls_handshake(sock_id=%d)"
#define FUNC_TLS_REHANDSHAKE  "tls_rehandshake(sock_id=%d)"    
#define FUNC_TLS_READ "tls_read(sock_id=%d, len=%d)"
#define FUNC_TLS_WRITE "tls_write(sock_id=%d, len=%d)"
#define FUNC_TLS_SHUTDOWN "tls_shutdown(sock_id=%d)"
#define FUNC_TLS_CONNECT "tls_connect(sock_id=%d) to %u.%u.%u.%u:%u"

    /* tls_handshake.c */

    /* tls_socket.c */

    /* tls_app.c */

    /* tls_fs.c */

    /* tls_certman.c */

    /* tls_mmi_certman.c */

    /* tls_common.c */
#define TLS_MALLOC "tls_malloc(size=%u)"
#define TLS_MALLOC_FAIL "alloc memory fail, req=%u, left=%u"
#define TLS_MALLOC_OK "allocated %u bytes, ptr=0x%x, from %u bytes, %u bytes left"
#define TLS_MFREE "tls_free()"
#define TLS_MFREE_OK "tls_free(ptr=0x%x, size=%d)"

   

    /* ----------------- TRACE_STATE trace class ------------------- */
#define TLS_STATE "socket_id=%d state=%dtls_state_enum"
#define TLS_STATE_TRANSITION "TLS socket %d transit from %dtls_state_enum to %dtls_state_enum"

    /* ----------------- TRACE_INFO trace class ------------------- */
#define INFO_RECV_EXT_Q "got message from %dmodule_type"
#define RCVD_INVALID_CERT_RSP "invalid_cert RSP: xid=0x%x, result=%d, action=%d"
#define RCVD_SELECT_USER_CERT_RSP "user_cert RSP: xid=0x%x, result=%d, user_cert=%d"
#define TLS_NEW_CTX_RETURN "tls_new_ctx() returns %d"
#define TLS_READ_BYTES "tls_read() success, return %d bytes"
#define TLS_WRITE_RETURN "tls_write(%d) returns %d"
#define TLS_VERIFY_CALLBACK_EXT_ERR "conn %d cert verify, x509->error=0x%x"
#define TLS_VERIFY_CALLBACK_RETURN "default verify callback returns %d"
#define TLS_HANDSHAKE_RETURN "TLS invokes ssl_Handshake(%d) returns 0x%x"
#define TLS_WRITE_INVOKES_SEC_SSL_WRITE_RETURN "sec_ssl_write(%d) returns %d, detail=%d"
#define TLS_WRITE_WANT_WRITE "TLS conn %d want write"
#define TLS_MEM_PEAK_SIZE "TLS peak memory allocated %d bytes"
#define TLS_MEM_CURR_SIZE "TLS current memory allocated %d bytes"
#define TLS_LOG_PLAINTEXT "TLS log plaintext %dkal_bool"
#define TLS_LOG_PLAINTEXT_ERROR "Use \"log_text on|off\" to log plaintext"
#define TLS_SIM_SUPC_PRIVKEY_ORIG "TLS: TLS private_key (before alloc): %x"
#define TLS_SIM_SUPC_PRIVKEY_ENCODED "TLS: TLS private_key (after alloc): %x"
#define TLS_SIM_SUPC_PRIVKEY_ENCODE_FAILED "Decrypt private key from file to file failed, code : %d"
#define TLS_CURRENT_TIME "Current time: %4d/%2d/%2d %2d:%2d%2d"


    /* ----------------- TRACE_WARNING trace class ------------------- */
#define TLS_TRIM_VERSION "trim version from %d to %d"
#define TLS_IS_BLOCKING_SOCKET "associate conn ctx to blocking socket %d"
#define TLS_FW_NOT_SOCKET_NOTIFY "give up forward a socket msg %dmsg_type"
#define TLS_DROP_SOCKET_NOTIFY "discard a socket notify to socket %d"
#define TLS_DELETE_HANGING_CONN "delete hanging connection context %d"
#define TLS_WARN_OVERWRITE_CA_PATH "overwrite ca_path of context %d"
#define CERTMAN_CERT_ID_CNF_WRONG_CERT_TYPE "wrong cert id cnf type: xid=0x%x, cert_type=0x%x"
#define CERTMAN_CERT_CHAIN_CNF_WRONG_CERT_TYPE "wrong cert chain cnf type: xid=0x%x, cert_type=0x%x"
#define CERTMAN_PRIVKEY_CNF_WRONG_CERT_TYPE "wrong private key cnf type: xid=0x%x, cert_type=0x%x"
#define TLS_READ_ERROR "[TLS] tls_read() failed, return 0x%x"
#define TLS_SKIP_HANDSHAKE_DONE "conn(%d) waiting for user response, skip sending handshake done"
#define TLS_RCVD_INVALID_CERT "conn(%d) received INVALID CERTIFICATE!"
#define TLS_SKIP_RCVD_INVALID_CERT "TLS skips handling conn(%d) INVALID CERTIFICATE!"
#define TLS_RCVD_INVALID_CERT_WAIT_USER_RSP "TLS notify user for handling conn(%d) INVALID CERTIFICATE!"
#define TLS_RCVD_CLIENT_AUTH "conn(%d) received CLIENT AUTHENTICATION!"
#define TLS_SKIP_RCVD_CLIENT_AUTH "TLS skips handling conn(%d) CLIENT AUTHENTICATION!"
#define TLS_RCVD_CLIENT_AUTH_WAIT_USER_RSP "TLS notify user for handling conn(%d) CLIENT AUTHENTICATION!"
#define WARN_CHECK_PEER_NAME_NOT_SUPPORTED "_CHECK_PEER_CNAME_ is NOT defined in TLS, check peer name ignored"

    /* ----------------- TRACE_ERROR trace class ------------------- */
#define ERROR_NONSUPPORT_MSG "non-support ILM message: message id=%dmsg_type"
#define TLS_TOO_MANY_CIPHERS "ciphers exceeds limit: given %d >= %d)"
#define TLS_INVALID_CIPHER "invalid cipher id %d found at ciphers[%d]"
#define TLS_TOO_MANY_CLIENT_AUTH_MODES "auth modes exceeds limit: given %d >= %d)"
#define TLS_INVALID_AUTH_MODE "invalid client auth mode %d found at mode[%d]"
#define TLS_ACCESS_INVALID_CTX "context %d is invalid"
#define TLS_MODULE_NOT_MATCH "context owned by %dmodule_type accessed by %dmodule_type"
#define TLS_ACCESS_INVALID_CONN "connection context %d is invalid"
#define TLS_ACCESS_INVALID_SOCKET "socket id %d is invalid"
#define TLS_CERT_XID_NOT_FOUND "no matched xid for certman cnf %d"
#define ERROR_NONSUPPORT_CERTMAN_MSG "invalid msg %dmsg_type from CERTMAN"
#define ERROR_NONSUPPORT_MMI_CERTMAN_MSG "invalid msg %dmsg_type from MMI_CERTMAN"
#define INVALID_CERT_RSP_MATCH_NO_CONN "no conn ctx matched for invalid cert rsp xid=0x%x"
#define CLIENT_AUTH_RSP_MATCH_NO_CONN "no conn ctx matched for client auth rsp xid=0x%x"
#define INVALID_CERT_RSP_WRONG_STATE  "conn ctx %d rcvd but not waiting invalid cert rsp "
#define CLIENT_AUTH_RSP_WRONG_STATE  "conn ctx %d rcvd but not waiting client auth rsp "
#define CERTMAN_MSG_NO_MATCH_CTX "no matching ctx for message with xid=0x%x from CERTMAN"
#define TLS_NEW_CTX_INVALID_SIDE "tls_new_ctx() with invalid side=0x%x"
#define TLS_NEW_CTX_INVALID_VER "tls_new_ctx() with invalid version=0x%x"
#define TLS_NEW_CTX_ASYM_MEM_CB "tls_new_ctx() with asymmetric memory clalback, malloc=0x%x, mfree=0x%x"
#define TLS_GET_CTX_SLOT_FAIL "tls_get_ctx_slot() failed, all slots are occupied"
#define TLS_SEC_SSL_CTX_NEW_FAILED "sec_ssl_ctx_new() failed"
#define TLS_SEC_SSL_CTX_SET_PRNG_FAILED "sec_ssl_ctx_set_prng() failed"
#define TLS_NEW_CONN_CTX_NO_MEMORY "malloc connection context failed, ctx_id=%d, s=%d"
#define TLS_TASK_NOT_READY "TLS is not ready (waiting for CERTMAN cnf msg)"
#define TLS_RCVD_ALERT "connection(%d) received alert, level=%dtls_alert_level_enum, desc=%dssl_error_enum"   
#define TLS_LOAD_PERSONAL_CERT_FAIL "global_ctx %d load personal certificate (%d) failed, ret=0x%x"
#define TLS_LOAD_ROOT_CA_FAIL "global_ctx %d load trusted Root CA failed, ret=0x%x"
#define TLS_SET_DEFAULT_CIPHER_LIST_FAIL "tls_set_default_cipher_list(ctx=%d) failed, ret=0x%x"
#define TLS_USE_PRIVATEKEY_FILE_FAIL "global_ctx %d load private key (%d) failed, ret=0x%x"
#define TLS_OCSP_VERIFY_CERT_FAIL "certman_ocsp_verify_cert() returns %d"
//
#endif

#endif /* !_TLS_TRC_H_ */


