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
 *   ossl_ssladp_trc.h
 *
 * Project:
 * --------
 *   MAUI
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
 * Apr 2 2009 mtk01264
 * [MAUI_01646410] [SSL] Check in OpenSSL and revise original SSL wrapper
 * Add trace to sec_ssl_get_error
 *
 * Mar 31 2009 mtk01264
 * [MAUI_01655762] JAVA(OpenSSL)_ it can't get message
 * Support retrieving peer certificate on session resumption.
 *
 * Mar 17 2009 mtk01264
 * [MAUI_01646410] [SSL] Check in OpenSSL and revise original SSL wrapper
 * add to source control recursely
 *
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef _OSSL_SSLADP_TRC_H_
#define _OSSL_SSLADP_TRC_H_

#include "typedef.h"

#define TRACE_SSL_HSHK         (TRACE_GROUP_1)

#if __AGPS_SWIP_REL__
    
/* ----------------- TRACE_FUNC trace class ------------------- */
/* sec_api_ctx.c */
#define FUNC_SEC_SSL_CTX_NEW "sec_ssl_ctx_new()"
#define FUNC_SEC_SSL_CTX_FREE "sec_ssl_ctx_free()"
#define FUNC_SEC_SSL_CTX_SET_PRNG "sec_ssl_ctx_set_prng()"
#define FUNC_SEC_SSL_CTX_SET_CIPHER_LIST "sec_ssl_ctx_set_cipher_list()"
#define FUNC_SEC_SSL_CTX_SET_DEFAULT_PALLWD_CB "sec_ssl_ctx_set_default_passwd_cb()"
#define FUNC_SEC_SSL_CTX_SET_DEFAULT_PALLWD_CB_USERDATA "sec_ssl_ctx_set_default_passwd_cb_userdata()"
#define FUNC_SEC_SSL_CTX_USE_PRIVATEKEY_FILE "sec_ssl_ctx_use_privatekey_file()"
#define FUNC_SEC_SSL_CTX_USE_CERTIFICATE_FILE "sec_ssl_ctx_use_certificate_file()"
#define FUNC_SEC_SSL_CTX_LOAD_VERIFY_LOCATIONS "sec_ssl_ctx_load_verify_locations()"
#define FUNC_SEC_SSL_CTX_SET_NEW_SSL_OPTION "sec_ssl_ctx_set_new_ssl_option()"
#define FUNC_SEC_SSL_CTX_SET_CLIENT_AUTH_MODES "sec_ssl_ctx_set_client_auth_modes()"
#define FUNC_SEC_SSL_CTX_SET_IO_FUNCS "sec_ssl_ctx_set_io_funcs()"
#define FUNC_SEC_SSL_CTX_SET_ALERT_FUNC "sec_ssl_ctx_set_alert_func()"
#define FUNC_SEC_SSL_CTX_SET_VERIFY_CALLBACK "sec_ssl_ctx_set_cert_verify_callback()"

/* sec_api_conn.c */
#define FUNC_SEC_SSL_NEW "sec_ssl_new()"
#define FUNC_SEC_SSL_FREE "sec_ssl_free()"
#define FUNC_SEC_SSL_CONNECT "sec_ssl_connect()"
#define FUNC_SEC_SSL_ACCEPT "sec_ssl_accept()"
#define FUNC_SEC_SSL_RENEGOTIATE "sec_ssl_renegotiate()"
#define FUNC_SEC_SSL_DO_HANDSHAKE "sec_ssl_do_handshake()"
#define FUNC_SEC_SSL_SESSION_REUSED "sec_ssl_session_reused()"
#define FUNC_SEC_SSL_SESSION_ESTABLISHED "sec_ssl_session_established()"
#define FUNC_SEC_SSL_GET_MASTER_SECRET "sec_ssl_get_master_secret()"
#define FUNC_SEC_SSL_GET_CLIENT_RANDOM "sec_ssl_get_client_random()"
#define FUNC_SEC_SSL_GET_SERVER_RANDOM "sec_ssl_get_server_random()"
#define FUNC_SEC_SSL_READ "sec_ssl_read()"
#define FUNC_SEC_SSL_WRITE "sec_ssl_write()"
#define FUNC_SEC_SSL_WANT_READ "sec_ssl_want_read()"
#define FUNC_SEC_SSL_WANT_WRITE "sec_ssl_want_write()"
#define FUNC_SEC_SSL_SHUTDOWN "sec_ssl_shutdown()"
#define FUNC_SEC_SSL_GET_ERROR "sec_ssl_get_error(ret=%d)"
#define FUNC_SEC_SSL_EXTRACT_CERT "sec_ssl_extract_cert()"
#define FUNC_SEC_SSL_GET_CURR_CIPHER_INFO "sec_ssl_get_curr_cipher_info()"
#define FUNC_SEC_SSL_GET_CIPHERSUITE "sec_ssl_get_ciphersuite()"
#define FUNC_SEC_SSL_CIPHER_GET_VERSION "sec_ssl_cipher_get_version()"
#define FUNC_SEC_SSL_GET_CERTREQ_AUTH_NAMES "sec_ssl_get_certreq_auth_names()"
/* ----------------- TRACE_STATE trace class ------------------- */

/* ----------------- TRACE_INFO trace class ------------------- */
#define INFO_DUP_AUTH_NAME "Duplicate %d auth_names"
#define INFO_ALLOCATE_DUP_AUTH_NAME "Allocate auth_name[%d] at 0x%x with size = %d"
#define INFO_DEALLOCATE_DUP_AUTH_NAME "Deallocate auth_name[%d] at 0x%x with size = %d"
#define INFO_QUIT_FREE_DUP_AUTH_NAME "Quit freeing dup authname at index %d"
#define INFO_RCVD_CERT_REQ "Received cert request from peer"
#define INFO_RESUMING_SESSION "Reuse session: %02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x"
#define INFO_CACHE_SESSION "Cache new session: %02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x"
#define INFO_APP_READ_CB "SSL: app_read_cb(buflen=%d), return=%d"
#define INFO_SSL_READ_RESULT "SSL: SSL_read(buflen=%d), return=%d"
#define INFO_SSL_GET_ERROR_RETURN "SSL: SSL_get_error(), return=%d"
#define INFO_SEC_SSL_GET_ERROR_RETURN "SSL: sec_ssl_get_error(), return=%d"
#define INFO_NO_OCSP_STATUS_IN_SERVER_HELO "No OCSP status in ServerHello"
#define INFO_NO_OCSP_STAPLING_RESPONSE "No OCSP stapling response message"
#define INFO_PROCESS_OCSP_STAPLING_RESP "Processing OCSP stapling response"


/* ----------------- TRACE_WARNING trace class ------------------- */
#define WARN_SET_NULL_READ_CALLBACK "set IO read callback to NULL"
#define WARN_SET_NULL_WRITE_CALLBACK "set IO write callback to NULL"
#define WARN_SET_NULL_ALERT_CALLBACK "set alert callback to NULL"
#define WARN_SET_NULL_CERT_VERIFY_CALLBACK "Set cert verify callback to NULL"
#define WARN_AUTH_NAMES_OVERFLOWED "Received %d auth names, overflowed (%d)"
#define WARN_AUTH_NAMES_SPACE "No space for auth_name provided by app"
#define WARN_NO_AUTH_NAMES_IN_CERT_REQ "No auth_name in received cert_req"
#define WARN_AUTH_NAMES_AVAILABLE "No auth_name available for the SSL connection"
#define WARN_CERT_CHAIN_TOO_LONG "Warn, cert chain exceeding the length, %d > %d"
#define WARN_CHECK_CNAME_NOT_SUPPORTED "_CHECK_PEER_CNAME_ is NOT defined in SSL wrapper, check cname ignored"
#define WARN_SSL_CVRT_CERT_WARN "SSL convert cert warning[%d] from %d to %08x"
#define WARN_SSL_NO_OCSP_STATUS_IN_SERVERHELLO "No OCSP status in ServerHello"
#define WARN_NO_OCSP_RESPONSE "Got OCSP stapling response"


/* ----------------- TRACE_ERROR trace class ------------------- */
/* SSL API */
#define ERROR_CREATE_GLOBAL_CTX "SSL_CTX_new() error"
#define ERROR_SET_CIPHER_SUITES "SSL_CTX_set_cipher_list() error: 0x%x"
#define ERROR_CREATE_CONNECTION_CONTEXT "SSL_new() error"
#define ERROR_SSL_CONNECT "SSL_connect() error: 0x%x"
#define ERROR_SSL_ACCEPT "SSL_accept() error: 0x%x"
#define ERROR_SSL_RENEGOTIATE "SSL_renegotiate() error: 0x%x"
#define ERROR_SSL_DO_HANDSHAKE "SSL_do_handshake() error: 0x%x"
#define ERROR_SSL_GET_CURRENT_CIPHER "SSL_get_current_cipher() error"
#define ERROR_WAS_SESSION_REUSED "SSL_session_reused() error: 0x%x"
#define ERROR_READ "SSL_read() error: 0x%x"
#define ERROR_WRITE "SSL_write() error: 0x%x"
#define ERROR_SSL_WANT_READ "SSL_want_read() error: 0x%x"
#define ERROR_SSL_WANT_WRITE "SSL_want_write() error: 0x%x"

#define ERROR_SHUTDOWN "SSL_shutdown() error: 0x%x"
#define ERROR_USE_PRIVATEKEY_FILE "SSL_CTX_use_PrivateKey_file() error: 0x%x"
#define ERROR_USE_CERTIFICATE_FILE "SSL_CTX_use_certificate_file() error: 0x%x"
#define ERROR_LOAD_VERIFY_LOCATIONS "SSL_CTX_load_verify_locations() error: 0x%x"

/* FS API */
#define ERROR_FS_OPEN "FS_Open() error: 0x%x"
#define ERROR_FS_GET_FILE_SIZE "FS_GetFileSize() error: 0x%x"
#define ERROR_FS_READ "FS_Read() error: 0x%x"

/* NULL pointer in argument */
#define NULL_POINTER_SET_PRNG "NULL pointer to sec_ssl_ctx_set_prng()"
#define NULL_POINTER_SET_CIPHER "NULL pointer to sec_ssl_ctx_set_cipher_list()"
#define NULL_POINTER_SET_DEFAULT_PASSWD_CB "NULL pointer to sec_ssl_ctx_set_default_passwd_cb()"
#define NULL_POINTER_SET_DEFAULT_PASSWD_CB_USERDATA "NULL pointer to sec_ssl_ctx_set_default_passwd_cb_userdata()"
#define NULL_POINTER_USE_PRIVATEKEY_FILE "NULL pointer to sec_ssl_ctx_use_privatekey_file()"
#define NULL_POINTER_USE_CERTIFICATE_FILE "NULL pointer to sec_ssl_ctx_use_certificate_file()"
#define NULL_POINTER_LOAD_VERIFY_LOCATIONS "NULL pointer to sec_ssl_ctx_load_verify_locations()"
#define NULL_POINTER_SSL_SET_CLIENT_AUTH_MODES "NULL pointer to sec_ssl_ctx_set_client_auth_modes()"
#define NULL_POINTER_SSL_SET_IO_FUNCS "NULL pointer to sec_ssl_ctx_set_io_funcs()"
#define NULL_POINTER_SSL_SET_ALERT_FUNC "NULL pointer to sec_ssl_ctx_set_alert_func()"
#define NULL_POINTER_SSL_SET_CERT_VERIFY_FUNC "NULL pointer to sec_ssl_ctx_set_cert_verify_callback()"
#define NULL_POINTER_SET_NEW_SSL_OPTION "NULL pointer to sec_ssl_ctx_set_new_ssl_option()"

#define NULL_INT_POINTER_SET_PRNG "NULL ctx->ctx in sec_ssl_ctx_set_prng()"
#define NULL_INT_POINTER_SET_CIPHER "NULL ctx->ctx in sec_ssl_ctx_set_cipher_list()"
#define NULL_INT_POINTER_SET_DEFAULT_PASSWD_CB "NULL ctx->ctx in sec_ssl_ctx_set_default_passwd_cb()"
#define NULL_INT_POINTER_SET_DEFAULT_PASSWD_CB_USERDATA "NULL ctx->ctx in sec_ssl_ctx_set_default_passwd_cb_userdata()"
#define NULL_INT_POINTER_USE_PRIVATEKEY_FILE "NULL ctx->ctx in sec_ssl_ctx_use_privatekey_file()"
#define NULL_INT_POINTER_USE_CERTIFICATE_FILE "NULL ctx->ctx in sec_ssl_ctx_use_certificate_file()"
#define NULL_INT_POINTER_LOAD_VERIFY_LOCATIONS "NULL ctx->ctx in sec_ssl_ctx_load_verify_locations()"
#define NULL_INT_POINTER_SSL_SET_CLIENT_AUTH_MODES "NULL ctx->ctx in sec_ssl_ctx_set_client_auth_modes()"
#define NULL_INT_POINTER_SSL_SET_IO_FUNCS "NULL ctx->ctx in sec_ssl_ctx_set_io_funcs()"
#define NULL_INT_POINTER_SSL_SET_ALERT_FUNC "NULL ctx->ctx in sec_ssl_ctx_set_alert_func()"
#define NULL_INT_POINTER_SSL_SET_CERT_VERIFY_FUNC "NULL ctx->ctx in sec_ssl_ctx_set_cert_verify_callback()"
#define NULL_INT_POINTER_SET_NEW_SSL_OPTION "NULL ctx->ctx in sec_ssl_ctx_set_new_ssl_option()"

#define NULL_POINTER_SSL_NEW "NULL pointer to sec_ssl_new()"
#define NULL_POINTER_SSL_FREE "NULL pointer to sec_ssl_free()"
#define NULL_POINTER_SSL_DISABLE_CLIENT_AUTH "NULL pointer to sec_ssl_disable_client_auth()"
#define NULL_POINTER_SSL_CONNECT "NULL pointer to sec_ssl_connect()"
#define NULL_POINTER_SSL_ACCEPT "NULL pointer to sec_ssl_accept()"
#define NULL_POINTER_SSL_RENEGOTIATE "NULL pointer to sec_ssl_renegotiate()"
#define NULL_POINTER_SSL_DO_HANDSHAKE "NULL pointer to sec_ssl_do_handshake()"
#define NULL_POINTER_SSL_SESSION_REUSED "NULL pointer to sec_ssl_session_reused()"
#define NULL_POINTER_SSL_SESSION_ESTABLISHED "NULL pointer to sec_ssl_session_established()"
#define NULL_POINTER_SSL_GET_MASTER_SECRET "NULL pointer to sec_ssl_get_master_secret()"
#define NULL_POINTER_SSL_GET_CLIENT_RANDOM "NULL pointer to sec_ssl_get_client_random()"
#define NULL_POINTER_SSL_GET_SERVER_RANDOM "NULL pointer to sec_ssl_get_server_random()"
#define NULL_POINTER_SSL_READ "NULL pointer to sec_ssl_read()"
#define NULL_POINTER_SSL_WRITE "NULL pointer to sec_ssl_write()"
#define NULL_POINTER_SSL_WANT_READ "NULL pointer to sec_ssl_want_read()"
#define NULL_POINTER_SSL_WANT_WRITE "NULL pointer to sec_ssl_want_write()"
#define NULL_POINTER_SSL_SHUTDOWN "NULL pointer to sec_ssl_shutdown()"
#define NULL_POINTER_SSL_GET_ERROR "NULL pointer to sec_ssl_get_error()"
#define NULL_POINTER_SSL_EXTRACT_CERT "NULL pointer to sec_ssl_extract_cert()"
#define NULL_POINTER_SSL_GET_CIPHER_INFO "NULL pointer to sec_ssl_get_curr_cipher_info()"
#define NULL_POINTER_SSL_GET_CIPHERSUITE "NULL pointer to sec_ssl_get_ciphersuite()"
#define NULL_POINTER_SSL_GET_VERSION "NULL pointer to sec_ssl_cipher_get_version()"
#define NULL_POINTER_SSL_GET_AUTH_NAMES "NULL pointer to sec_ssl_get_certreq_auth_names()"
#define NULL_POINTER_SSL_GET_PEER_CERTIFICATE "NULL pointer to sec_ssl_get_peer_certificate()"

#define NULL_INT_POINTER_SSL_NEW "NULL ssl->conn in sec_ssl_new()"
#define NULL_INT_POINTER_SSL_FREE "NULL ssl->conn in sec_ssl_free()"
#define NULL_INT_POINTER_SSL_DISABLE_CLIENT_AUTH "NULL ssl->conn in sec_ssl_disable_client_auth()"
#define NULL_INT_POINTER_SSL_CONNECT "NULL ssl->conn in sec_ssl_connect()"
#define NULL_INT_POINTER_SSL_ACCEPT "NULL ssl->conn in sec_ssl_accept()"
#define NULL_INT_POINTER_SSL_RENEGOTIATE "NULL ssl->conn in sec_ssl_renegotiate()"
#define NULL_INT_POINTER_SSL_DO_HANDSHAKE "NULL ssl->conn in sec_ssl_do_handshake()"
#define NULL_INT_POINTER_SSL_SESSION_REUSED "NULL ssl->conn in sec_ssl_session_reused()"
#define NULL_INT_POINTER_SSL_SESSION_ESTABLISHED "NULL ssl->conn in sec_ssl_session_established()"
#define NULL_INT_POINTER_SSL_GET_MASTER_SECRET "NULL ssl->conn in sec_ssl_get_master_secret()"
#define NULL_INT_POINTER_SSL_GET_CLIENT_RANDOM "NULL ssl->conn in sec_ssl_get_client_random()"
#define NULL_INT_POINTER_SSL_GET_SERVER_RANDOM "NULL ssl->conn in sec_ssl_get_server_random()"
#define NULL_INT_POINTER_SSL_READ "NULL ssl->conn in sec_ssl_read()"
#define NULL_INT_POINTER_SSL_WRITE "NULL ssl->conn in sec_ssl_write()"
#define NULL_INT_POINTER_SSL_WANT_READ "NULL ssl->conn in sec_ssl_want_read()"
#define NULL_INT_POINTER_SSL_WANT_WRITE "NULL ssl->conn in sec_ssl_want_write()"
#define NULL_INT_POINTER_SSL_SHUTDOWN "NULL ssl->conn in sec_ssl_shutdown()"
#define NULL_INT_POINTER_SSL_GET_ERROR "NULL ssl->conn in sec_ssl_get_error()"
#define NULL_INT_POINTER_SSL_GET_CIPHER_INFO "NULL ssl->conn in sec_ssl_get_curr_cipher_info()"
#define NULL_INT_POINTER_SSL_GET_CIPHERSUITE "NULL ssl->conn in sec_ssl_get_ciphersuite()"
#define NULL_INT_POINTER_SSL_GET_VERSION "NULL ssl->conn in sec_ssl_cipher_get_version()"
#define NULL_INT_POINTER_SSL_GET_AUTH_NAMES "NULL ssl->conn in sec_ssl_get_certreq_auth_names()"
#define NULL_INT_POINTER_SSL_GET_PEER_CERTIFICATE "NULL ssl->conn in sec_ssl_get_peer_certificate()"

/* Misc */
#define TOO_MANY_CIPHERS "Too many ciphers, given %d (must be less than %d)"

#define ERROR_FREE_NULL_GLOBAL_CTX "Freeing NULL global context"
#define ERROR_FREE_NULL_INT_GLOBAL_CTX "Freeing NULL ctx->ctx to global context"
#define ERROR_NULL_METHOD "Creating new global context with NULL method"
#define ERROR_MALLOC_FAILED "sec_ssl_malloc() failed in TLS"

#define ERROR_INVALID_CIPHER "Invalid cipher %d in cipher list"
#define ERROR_INVALID_PRIVKEY_PARAM "Invalid param: sec_ssl_ctx_use_privatekey_file(type=%d)"
#define ERROR_PRIVKEY_FILE_NOT_EXIST "File not exist in loading privkey"
#define ERROR_PRIVKEY_FILE_EMPTY "Empty file in loading privkey"
#define ERROR_INVALID_CERT_PARAM "Invalid param: sec_ssl_ctx_use_certificate_file(type=%d)"
#define ERROR_CERT_FILE_NOT_EXIST "File not exist in loading cert"
#define ERROR_CERT_FILE_EMPTY "Empty file in loading cert"
#define ERROR_CA_FILE_NOT_EXIST "File not exist in loading CA"
#define ERROR_CA_PATH_NOT_EXIST "Path not exist in loading CA"
#define ERROR_UNKNOWN_CA_FILE "Unknown file type in loading CA"
#define ERROR_CERT_FILE_PATH_EMPTY "Empty file and path in loading cert"
#define ERROR_CA_FILE_EMPTY "Empty file in loading CA"
#define ERROR_CA_PATH_EMPTY "Empty path in loading CA"
#define ERROR_NO_AUTH_MODES "No auth modes specified"
#define ERROR_INVALID_AUTH_MODES "Invalid param: sec_ssl_ctx_set_client_auth_modes(%d)"
#define ERROR_BAD_AUTH_MODES_SIDE "Bad auth mode side: %d"
#define ERROR_FS_CA_PATH_EMPTY "Loading empty CA directory"
#define ERROR_OCSP_STAPLING_RESPONSE_PARSE_FAILED "OCSP stapling response parse error"

#else
BEGIN_TRACE_MAP(MOD_SSL)

    /* ----------------- TRACE_FUNC trace class ------------------- */
    /* sec_api_ctx.c */
    TRC_MSG(FUNC_SEC_SSL_CTX_NEW, "sec_ssl_ctx_new()")
    TRC_MSG(FUNC_SEC_SSL_CTX_FREE, "sec_ssl_ctx_free()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_PRNG, "sec_ssl_ctx_set_prng()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_CIPHER_LIST, "sec_ssl_ctx_set_cipher_list()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_DEFAULT_PALLWD_CB, "sec_ssl_ctx_set_default_passwd_cb()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_DEFAULT_PALLWD_CB_USERDATA, "sec_ssl_ctx_set_default_passwd_cb_userdata()")
    TRC_MSG(FUNC_SEC_SSL_CTX_USE_PRIVATEKEY_FILE, "sec_ssl_ctx_use_privatekey_file()")
    TRC_MSG(FUNC_SEC_SSL_CTX_USE_CERTIFICATE_FILE, "sec_ssl_ctx_use_certificate_file()")
    TRC_MSG(FUNC_SEC_SSL_CTX_LOAD_VERIFY_LOCATIONS, "sec_ssl_ctx_load_verify_locations()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_NEW_SSL_OPTION, "sec_ssl_ctx_set_new_ssl_option()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_CLIENT_AUTH_MODES, "sec_ssl_ctx_set_client_auth_modes()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_IO_FUNCS, "sec_ssl_ctx_set_io_funcs()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_ALERT_FUNC, "sec_ssl_ctx_set_alert_func()")
    TRC_MSG(FUNC_SEC_SSL_CTX_SET_VERIFY_CALLBACK, "sec_ssl_ctx_set_cert_verify_callback()")

    /* sec_api_conn.c */
    TRC_MSG(FUNC_SEC_SSL_NEW, "sec_ssl_new()")
    TRC_MSG(FUNC_SEC_SSL_FREE, "sec_ssl_free()")
    TRC_MSG(FUNC_SEC_SSL_CONNECT, "sec_ssl_connect()")
    TRC_MSG(FUNC_SEC_SSL_ACCEPT, "sec_ssl_accept()")
    TRC_MSG(FUNC_SEC_SSL_RENEGOTIATE, "sec_ssl_renegotiate()")
    TRC_MSG(FUNC_SEC_SSL_DO_HANDSHAKE, "sec_ssl_do_handshake()")
    TRC_MSG(FUNC_SEC_SSL_SESSION_REUSED, "sec_ssl_session_reused()")
    TRC_MSG(FUNC_SEC_SSL_SESSION_ESTABLISHED, "sec_ssl_session_established()")
    TRC_MSG(FUNC_SEC_SSL_GET_MASTER_SECRET, "sec_ssl_get_master_secret()")
    TRC_MSG(FUNC_SEC_SSL_GET_CLIENT_RANDOM, "sec_ssl_get_client_random()")
    TRC_MSG(FUNC_SEC_SSL_GET_SERVER_RANDOM, "sec_ssl_get_server_random()")
    TRC_MSG(FUNC_SEC_SSL_READ, "sec_ssl_read()")
    TRC_MSG(FUNC_SEC_SSL_WRITE, "sec_ssl_write()")
    TRC_MSG(FUNC_SEC_SSL_WANT_READ, "sec_ssl_want_read()")
    TRC_MSG(FUNC_SEC_SSL_WANT_WRITE, "sec_ssl_want_write()")
    TRC_MSG(FUNC_SEC_SSL_SHUTDOWN, "sec_ssl_shutdown()")
    TRC_MSG(FUNC_SEC_SSL_GET_ERROR, "sec_ssl_get_error(ret=%d)")
    TRC_MSG(FUNC_SEC_SSL_EXTRACT_CERT, "sec_ssl_extract_cert()")
    TRC_MSG(FUNC_SEC_SSL_GET_CURR_CIPHER_INFO, "sec_ssl_get_curr_cipher_info()")
    TRC_MSG(FUNC_SEC_SSL_GET_CIPHERSUITE, "sec_ssl_get_ciphersuite()")
    TRC_MSG(FUNC_SEC_SSL_CIPHER_GET_VERSION, "sec_ssl_cipher_get_version()")
    TRC_MSG(FUNC_SEC_SSL_GET_CERTREQ_AUTH_NAMES, "sec_ssl_get_certreq_auth_names()")


    /* ----------------- TRACE_STATE trace class ------------------- */


    /* ----------------- TRACE_INFO trace class ------------------- */
    TRC_MSG(INFO_DUP_AUTH_NAME, "Duplicate %d auth_names")
    TRC_MSG(INFO_ALLOCATE_DUP_AUTH_NAME, "Allocate auth_name[%d] at 0x%x with size = %d")
    TRC_MSG(INFO_DEALLOCATE_DUP_AUTH_NAME, "Deallocate auth_name[%d] at 0x%x with size = %d")
    TRC_MSG(INFO_QUIT_FREE_DUP_AUTH_NAME, "Quit freeing dup authname at index %d")
    TRC_MSG(INFO_RCVD_CERT_REQ, "Received cert request from peer")
    TRC_MSG(INFO_RESUMING_SESSION, "Reuse session: %02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x")
    TRC_MSG(INFO_CACHE_SESSION, "Cache new session: %02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x")
    TRC_MSG(INFO_APP_READ_CB, "SSL: app_read_cb(buflen=%d), return=%d")
    TRC_MSG(INFO_SSL_READ_RESULT, "SSL: SSL_read(buflen=%d), return=%d")
    TRC_MSG(INFO_SSL_GET_ERROR_RETURN, "SSL: SSL_get_error(), return=%d")
    TRC_MSG(INFO_SEC_SSL_GET_ERROR_RETURN, "SSL: sec_ssl_get_error(), return=%d")
    TRC_MSG(INFO_NO_OCSP_STATUS_IN_SERVER_HELO, "No OCSP status in ServerHello")
    TRC_MSG(INFO_NO_OCSP_STAPLING_RESPONSE, "No OCSP stapling response message")
    TRC_MSG(INFO_PROCESS_OCSP_STAPLING_RESP, "Processing OCSP stapling response")


    /* ----------------- TRACE_WARNING trace class ------------------- */
    TRC_MSG(WARN_SET_NULL_READ_CALLBACK, "set IO read callback to NULL")
    TRC_MSG(WARN_SET_NULL_WRITE_CALLBACK, "set IO write callback to NULL")
    TRC_MSG(WARN_SET_NULL_ALERT_CALLBACK, "set alert callback to NULL")
    TRC_MSG(WARN_SET_NULL_CERT_VERIFY_CALLBACK, "Set cert verify callback to NULL")
    TRC_MSG(WARN_AUTH_NAMES_OVERFLOWED, "Received %d auth names, overflowed (%d)")
    TRC_MSG(WARN_AUTH_NAMES_SPACE, "No space for auth_name provided by app")
    TRC_MSG(WARN_NO_AUTH_NAMES_IN_CERT_REQ, "No auth_name in received cert_req")
    TRC_MSG(WARN_AUTH_NAMES_AVAILABLE, "No auth_name available for the SSL connection")
    TRC_MSG(WARN_CERT_CHAIN_TOO_LONG, "Warn, cert chain exceeding the length, %d > %d")
    TRC_MSG(WARN_CHECK_CNAME_NOT_SUPPORTED, "_CHECK_PEER_CNAME_ is NOT defined in SSL wrapper, check cname ignored")
    TRC_MSG(WARN_SSL_CVRT_CERT_WARN, "SSL convert cert warning[%d] from %d to %08x")
    TRC_MSG(WARN_SSL_NO_OCSP_STATUS_IN_SERVERHELLO, "No OCSP status in ServerHello")
    TRC_MSG(WARN_NO_OCSP_RESPONSE, "Got OCSP stapling response")


    /* ----------------- TRACE_ERROR trace class ------------------- */
    /* SSL API */
    TRC_MSG(ERROR_CREATE_GLOBAL_CTX, "SSL_CTX_new() error")
    TRC_MSG(ERROR_SET_CIPHER_SUITES, "SSL_CTX_set_cipher_list() error: 0x%x")
    TRC_MSG(ERROR_CREATE_CONNECTION_CONTEXT, "SSL_new() error")
    TRC_MSG(ERROR_SSL_CONNECT, "SSL_connect() error: 0x%x")
    TRC_MSG(ERROR_SSL_ACCEPT, "SSL_accept() error: 0x%x")
    TRC_MSG(ERROR_SSL_RENEGOTIATE, "SSL_renegotiate() error: 0x%x")
    TRC_MSG(ERROR_SSL_DO_HANDSHAKE, "SSL_do_handshake() error: 0x%x")
    TRC_MSG(ERROR_SSL_GET_CURRENT_CIPHER, "SSL_get_current_cipher() error")
    TRC_MSG(ERROR_WAS_SESSION_REUSED, "SSL_session_reused() error: 0x%x")
    TRC_MSG(ERROR_READ, "SSL_read() error: 0x%x")
    TRC_MSG(ERROR_WRITE, "SSL_write() error: 0x%x")
    TRC_MSG(ERROR_SSL_WANT_READ, "SSL_want_read() error: 0x%x")
    TRC_MSG(ERROR_SSL_WANT_WRITE, "SSL_want_write() error: 0x%x")

    TRC_MSG(ERROR_SHUTDOWN, "SSL_shutdown() error: 0x%x")
    TRC_MSG(ERROR_USE_PRIVATEKEY_FILE, "SSL_CTX_use_PrivateKey_file() error: 0x%x")
    TRC_MSG(ERROR_USE_CERTIFICATE_FILE, "SSL_CTX_use_certificate_file() error: 0x%x")
    TRC_MSG(ERROR_LOAD_VERIFY_LOCATIONS, "SSL_CTX_load_verify_locations() error: 0x%x")

    /* FS API */
    TRC_MSG(ERROR_FS_OPEN, "FS_Open() error: 0x%x")
    TRC_MSG(ERROR_FS_GET_FILE_SIZE, "FS_GetFileSize() error: 0x%x")
    TRC_MSG(ERROR_FS_READ, "FS_Read() error: 0x%x")

    /* NULL pointer in argument */
    TRC_MSG(NULL_POINTER_SET_PRNG, "NULL pointer to sec_ssl_ctx_set_prng()")
    TRC_MSG(NULL_POINTER_SET_CIPHER, "NULL pointer to sec_ssl_ctx_set_cipher_list()")
    TRC_MSG(NULL_POINTER_SET_DEFAULT_PASSWD_CB, "NULL pointer to sec_ssl_ctx_set_default_passwd_cb()")
    TRC_MSG(NULL_POINTER_SET_DEFAULT_PASSWD_CB_USERDATA, "NULL pointer to sec_ssl_ctx_set_default_passwd_cb_userdata()")
    TRC_MSG(NULL_POINTER_USE_PRIVATEKEY_FILE, "NULL pointer to sec_ssl_ctx_use_privatekey_file()")
    TRC_MSG(NULL_POINTER_USE_CERTIFICATE_FILE, "NULL pointer to sec_ssl_ctx_use_certificate_file()")
    TRC_MSG(NULL_POINTER_LOAD_VERIFY_LOCATIONS, "NULL pointer to sec_ssl_ctx_load_verify_locations()")
    TRC_MSG(NULL_POINTER_SSL_SET_CLIENT_AUTH_MODES, "NULL pointer to sec_ssl_ctx_set_client_auth_modes()")
    TRC_MSG(NULL_POINTER_SSL_SET_IO_FUNCS, "NULL pointer to sec_ssl_ctx_set_io_funcs()")
    TRC_MSG(NULL_POINTER_SSL_SET_ALERT_FUNC, "NULL pointer to sec_ssl_ctx_set_alert_func()")
    TRC_MSG(NULL_POINTER_SSL_SET_CERT_VERIFY_FUNC, "NULL pointer to sec_ssl_ctx_set_cert_verify_callback()")
    TRC_MSG(NULL_POINTER_SET_NEW_SSL_OPTION, "NULL pointer to sec_ssl_ctx_set_new_ssl_option()")

    TRC_MSG(NULL_INT_POINTER_SET_PRNG, "NULL ctx->ctx in sec_ssl_ctx_set_prng()")
    TRC_MSG(NULL_INT_POINTER_SET_CIPHER, "NULL ctx->ctx in sec_ssl_ctx_set_cipher_list()")
    TRC_MSG(NULL_INT_POINTER_SET_DEFAULT_PASSWD_CB, "NULL ctx->ctx in sec_ssl_ctx_set_default_passwd_cb()")
    TRC_MSG(NULL_INT_POINTER_SET_DEFAULT_PASSWD_CB_USERDATA, "NULL ctx->ctx in sec_ssl_ctx_set_default_passwd_cb_userdata()")
    TRC_MSG(NULL_INT_POINTER_USE_PRIVATEKEY_FILE, "NULL ctx->ctx in sec_ssl_ctx_use_privatekey_file()")
    TRC_MSG(NULL_INT_POINTER_USE_CERTIFICATE_FILE, "NULL ctx->ctx in sec_ssl_ctx_use_certificate_file()")
    TRC_MSG(NULL_INT_POINTER_LOAD_VERIFY_LOCATIONS, "NULL ctx->ctx in sec_ssl_ctx_load_verify_locations()")
    TRC_MSG(NULL_INT_POINTER_SSL_SET_CLIENT_AUTH_MODES, "NULL ctx->ctx in sec_ssl_ctx_set_client_auth_modes()")
    TRC_MSG(NULL_INT_POINTER_SSL_SET_IO_FUNCS, "NULL ctx->ctx in sec_ssl_ctx_set_io_funcs()")
    TRC_MSG(NULL_INT_POINTER_SSL_SET_ALERT_FUNC, "NULL ctx->ctx in sec_ssl_ctx_set_alert_func()")
    TRC_MSG(NULL_INT_POINTER_SSL_SET_CERT_VERIFY_FUNC, "NULL ctx->ctx in sec_ssl_ctx_set_cert_verify_callback()")
    TRC_MSG(NULL_INT_POINTER_SET_NEW_SSL_OPTION, "NULL ctx->ctx in sec_ssl_ctx_set_new_ssl_option()")

    TRC_MSG(NULL_POINTER_SSL_NEW, "NULL pointer to sec_ssl_new()")
    TRC_MSG(NULL_POINTER_SSL_FREE, "NULL pointer to sec_ssl_free()")
    TRC_MSG(NULL_POINTER_SSL_DISABLE_CLIENT_AUTH, "NULL pointer to sec_ssl_disable_client_auth()")
    TRC_MSG(NULL_POINTER_SSL_CONNECT, "NULL pointer to sec_ssl_connect()")
    TRC_MSG(NULL_POINTER_SSL_ACCEPT, "NULL pointer to sec_ssl_accept()")
    TRC_MSG(NULL_POINTER_SSL_RENEGOTIATE, "NULL pointer to sec_ssl_renegotiate()")
    TRC_MSG(NULL_POINTER_SSL_DO_HANDSHAKE, "NULL pointer to sec_ssl_do_handshake()")
    TRC_MSG(NULL_POINTER_SSL_SESSION_REUSED, "NULL pointer to sec_ssl_session_reused()")
    TRC_MSG(NULL_POINTER_SSL_SESSION_ESTABLISHED, "NULL pointer to sec_ssl_session_established()")
    TRC_MSG(NULL_POINTER_SSL_GET_MASTER_SECRET, "NULL pointer to sec_ssl_get_master_secret()")
    TRC_MSG(NULL_POINTER_SSL_GET_CLIENT_RANDOM, "NULL pointer to sec_ssl_get_client_random()")
    TRC_MSG(NULL_POINTER_SSL_GET_SERVER_RANDOM, "NULL pointer to sec_ssl_get_server_random()")
    TRC_MSG(NULL_POINTER_SSL_READ, "NULL pointer to sec_ssl_read()")
    TRC_MSG(NULL_POINTER_SSL_WRITE, "NULL pointer to sec_ssl_write()")
    TRC_MSG(NULL_POINTER_SSL_WANT_READ, "NULL pointer to sec_ssl_want_read()")
    TRC_MSG(NULL_POINTER_SSL_WANT_WRITE, "NULL pointer to sec_ssl_want_write()")
    TRC_MSG(NULL_POINTER_SSL_SHUTDOWN, "NULL pointer to sec_ssl_shutdown()")
    TRC_MSG(NULL_POINTER_SSL_GET_ERROR, "NULL pointer to sec_ssl_get_error()")
    TRC_MSG(NULL_POINTER_SSL_EXTRACT_CERT, "NULL pointer to sec_ssl_extract_cert()")
    TRC_MSG(NULL_POINTER_SSL_GET_CIPHER_INFO, "NULL pointer to sec_ssl_get_curr_cipher_info()")
    TRC_MSG(NULL_POINTER_SSL_GET_CIPHERSUITE, "NULL pointer to sec_ssl_get_ciphersuite()")
    TRC_MSG(NULL_POINTER_SSL_GET_VERSION, "NULL pointer to sec_ssl_cipher_get_version()")
    TRC_MSG(NULL_POINTER_SSL_GET_AUTH_NAMES, "NULL pointer to sec_ssl_get_certreq_auth_names()")
    TRC_MSG(NULL_POINTER_SSL_GET_PEER_CERTIFICATE, "NULL pointer to sec_ssl_get_peer_certificate()")

    TRC_MSG(NULL_INT_POINTER_SSL_NEW, "NULL ssl->conn in sec_ssl_new()")
    TRC_MSG(NULL_INT_POINTER_SSL_FREE, "NULL ssl->conn in sec_ssl_free()")
    TRC_MSG(NULL_INT_POINTER_SSL_DISABLE_CLIENT_AUTH, "NULL ssl->conn in sec_ssl_disable_client_auth()")
    TRC_MSG(NULL_INT_POINTER_SSL_CONNECT, "NULL ssl->conn in sec_ssl_connect()")
    TRC_MSG(NULL_INT_POINTER_SSL_ACCEPT, "NULL ssl->conn in sec_ssl_accept()")
    TRC_MSG(NULL_INT_POINTER_SSL_RENEGOTIATE, "NULL ssl->conn in sec_ssl_renegotiate()")
    TRC_MSG(NULL_INT_POINTER_SSL_DO_HANDSHAKE, "NULL ssl->conn in sec_ssl_do_handshake()")
    TRC_MSG(NULL_INT_POINTER_SSL_SESSION_REUSED, "NULL ssl->conn in sec_ssl_session_reused()")
    TRC_MSG(NULL_INT_POINTER_SSL_SESSION_ESTABLISHED, "NULL ssl->conn in sec_ssl_session_established()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_MASTER_SECRET, "NULL ssl->conn in sec_ssl_get_master_secret()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_CLIENT_RANDOM, "NULL ssl->conn in sec_ssl_get_client_random()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_SERVER_RANDOM, "NULL ssl->conn in sec_ssl_get_server_random()")
    TRC_MSG(NULL_INT_POINTER_SSL_READ, "NULL ssl->conn in sec_ssl_read()")
    TRC_MSG(NULL_INT_POINTER_SSL_WRITE, "NULL ssl->conn in sec_ssl_write()")
    TRC_MSG(NULL_INT_POINTER_SSL_WANT_READ, "NULL ssl->conn in sec_ssl_want_read()")
    TRC_MSG(NULL_INT_POINTER_SSL_WANT_WRITE, "NULL ssl->conn in sec_ssl_want_write()")
    TRC_MSG(NULL_INT_POINTER_SSL_SHUTDOWN, "NULL ssl->conn in sec_ssl_shutdown()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_ERROR, "NULL ssl->conn in sec_ssl_get_error()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_CIPHER_INFO, "NULL ssl->conn in sec_ssl_get_curr_cipher_info()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_CIPHERSUITE, "NULL ssl->conn in sec_ssl_get_ciphersuite()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_VERSION, "NULL ssl->conn in sec_ssl_cipher_get_version()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_AUTH_NAMES, "NULL ssl->conn in sec_ssl_get_certreq_auth_names()")
    TRC_MSG(NULL_INT_POINTER_SSL_GET_PEER_CERTIFICATE, "NULL ssl->conn in sec_ssl_get_peer_certificate()")

    /* Misc */
    TRC_MSG(TOO_MANY_CIPHERS, "Too many ciphers, given %d (must be less than %d)")

    TRC_MSG(ERROR_FREE_NULL_GLOBAL_CTX, "Freeing NULL global context")
    TRC_MSG(ERROR_FREE_NULL_INT_GLOBAL_CTX, "Freeing NULL ctx->ctx to global context")
    TRC_MSG(ERROR_NULL_METHOD, "Creating new global context with NULL method")
    TRC_MSG(ERROR_MALLOC_FAILED, "sec_ssl_malloc() failed in TLS")

    TRC_MSG(ERROR_INVALID_CIPHER, "Invalid cipher %d in cipher list")
    TRC_MSG(ERROR_INVALID_PRIVKEY_PARAM, "Invalid param: sec_ssl_ctx_use_privatekey_file(type=%d)")
    TRC_MSG(ERROR_PRIVKEY_FILE_NOT_EXIST, "File not exist in loading privkey")
    TRC_MSG(ERROR_PRIVKEY_FILE_EMPTY, "Empty file in loading privkey")
    TRC_MSG(ERROR_INVALID_CERT_PARAM, "Invalid param: sec_ssl_ctx_use_certificate_file(type=%d)")
    TRC_MSG(ERROR_CERT_FILE_NOT_EXIST, "File not exist in loading cert")
    TRC_MSG(ERROR_CERT_FILE_EMPTY, "Empty file in loading cert")
    TRC_MSG(ERROR_CA_FILE_NOT_EXIST, "File not exist in loading CA")
    TRC_MSG(ERROR_CA_PATH_NOT_EXIST, "Path not exist in loading CA")
    TRC_MSG(ERROR_UNKNOWN_CA_FILE, "Unknown file type in loading CA")
    TRC_MSG(ERROR_CERT_FILE_PATH_EMPTY, "Empty file and path in loading cert")
    TRC_MSG(ERROR_CA_FILE_EMPTY, "Empty file in loading CA")
    TRC_MSG(ERROR_CA_PATH_EMPTY, "Empty path in loading CA")
    TRC_MSG(ERROR_NO_AUTH_MODES, "No auth modes specified")
    TRC_MSG(ERROR_INVALID_AUTH_MODES, "Invalid param: sec_ssl_ctx_set_client_auth_modes(%d)")
    TRC_MSG(ERROR_BAD_AUTH_MODES_SIDE, "Bad auth mode side: %d")
    TRC_MSG(ERROR_FS_CA_PATH_EMPTY, "Loading empty CA directory")
    TRC_MSG(ERROR_OCSP_STAPLING_RESPONSE_PARSE_FAILED, "OCSP stapling response parse error")


    /* timer */

END_TRACE_MAP(MOD_SSL)
#endif

#endif /* !_OSSL_SSLADP_TRC_H_ */


