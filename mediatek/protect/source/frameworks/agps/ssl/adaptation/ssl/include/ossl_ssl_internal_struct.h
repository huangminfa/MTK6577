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
 *   ossl_ssl_internal_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains structures in SSL wrapper
 *
 * Author:
 * -------
 *   Wyatt Sun
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
 *============================================================================
 ****************************************************************************/
#ifndef _OSSL_SSL_INTERNAL_STRUCT_H_
#define _OSSL_SSL_INTERNAL_STRUCT_H_

/******************************************************************************
 * Max number of client authentication modes can be specified by applications.
 * SSL_MAX_CLIENT_AUTH_MODES = SSL_LIMITS_MAXNUM_CLIENT_AUTH_MODES.
 ******************************************************************************/
#define SSL_MAX_CLIENT_AUTH_MODES (7) 


/******************************************************************************
 * Array size storing the peer information for session cache.
 * internal.
 ******************************************************************************/
#define SEC_MAX_PEER_DATA_SIZE (32)


/******************************************************************************
 * Max number of acceptable authority names in certificate request from server.
 ******************************************************************************/
#define SEC_MAX_AUTH_NAMES (32)


/* Used in sec_ssl_get_certreq_auth_names() to filter cert types. */
typedef enum
{
    SEC_CERT_TYPE_RSA_SIGN = 1, /* RSA */
    SEC_CERT_TYPE_DSS_SIGN = 2  /* DSA */
} sec_cert_type_enum;


/* SSL context sides, internal. */
typedef enum 
{
    SSL_UNKNOWN_SIDE = 0,
    SSL_CLIENT_SIDE = 1, /* Client side. */
    SSL_SERVER_SIDE = 2  /* Server side. */ 
} ssl_context_side_enum;


struct ssl_ctx
{
    /* Set: sec_ssl_ctx_new() */
    SSL_CTX             *ctx;
    ssl_context_side_enum   conn_side;

    /* Set: sec_ssl_ctx_set_new_ssl_option() */
    /* Ref: OpenSSL currently does not support this parameter */
    kal_uint16          write_frag_len;

    /* Set: sec_ssl_ctx_set_new_ssl_option() */
    /* Ref: sec_ssl_new(), only conn->peer and conn->peer_data is used. */
    sec_sess_rec        peer;      /* peer.data points to the next field: ctx->peer_data[] */
    kal_uint8           peer_data[SEC_MAX_PEER_DATA_SIZE];

    /* Set: sec_ssl_ctx_set_io_funcs() */
    sec_ssl_read_cb     read_callback;
    sec_ssl_write_cb    write_callback;

    /* Set: sec_ssl_ctx_set_new_ssl_option() */
    /* Ref: sec_ssl_new(), only the conn->io_ref is used.
     *      When doing I/O, ctx->read_callback() and ctx->write_callback()
     *      is invoked by the argument conn->io_ref. */
    void                *io_ref;

    /* Set: sec_ssl_ctx_set_alert_func() */
    /* Ref: sec_ssl_info_dispatch_callback(), a static callback set to SSL
     *      library by SSL_CTX_set_info_callback(), i.e.,
     *      ctx->info_callback = sec_ssl_info_dispatch_callback(). */
    sec_ssl_alert_func  internal_alert_func;

    /* Set: sec_ssl_ctx_set_new_ssl_option() */
    /* Ref: sec_ssl_new(), only the conn->alert_ref is used.
     *      When receiving an alert, ctx->internal_alert_func() is invoked by
     *      the argument conn->alert_ref. */
    void                *alert_ref; 

    /* Set: sec_ssl_ctx_set_cert_verify_callback() */
    /* Ref: sec_ssl_generic_cert_verify(), a static callback set to SSL library
     *      by SSL_CTX_set_cert_verify_callback(), i.e.,
     *      ctx->app_verify_callback = sec_ssl_generic_cert_verify(). */
    sec_ssl_cert_verify_callback       app_cert_verify_func;
    void                              *app_cert_verify_arg;

    #define SSL_PASSWD_BUF_LEN         (64)
    ssl_password_cb                    passwd_cb;
    void                               *passwd_cb_userdata;
}; /* OpenSSL */


struct ssl_conn
{
    /* Set: sec_ssl_new() */
    ssl_ctx             *ctx;
    SSL                 *conn;

    /* Set: sec_ssl_new() */
    /* Ref: sec_ssl_connect() */
    sec_sess_rec        peer;
    kal_uint8           peer_data[SEC_MAX_PEER_DATA_SIZE];

    //sec_ssl_cipher      cipher;

    /* Set: sec_ssl_new() */
    /* Ref: app IO callback function set by sec_ssl_ctx_set_io_funcs(),
    *       When reading/writing data, ctx->read_callback() and
            ctx->write_callback() is invoked by the argument conn->io_re. */
    void                *io_ref;

    /* Peer cert*/
    sec_cert_struct peer_cert;

    /* Ref: app alert callback function set by sec_ssl_ctx_set_alert_func(),
    *       When receiving an alert, ctx->internal_alert_func() is invoked by
    *       the argument conn->alert_ref. */
    void                *alert_ref;

    /* Set: sec_ssl_disable_client_auth() */
    /* Ref: sec_ssl_client_cert_cb(), a static callback set to SSL library by
     *      SSL_CTX_set_client_cert_cb(), i.e.,
     *      ctx->client_cert_cb = sec_ssl_client_cert_cb(). */
    kal_bool            disable_client_auth; /* if set, skip sending client certificate on client authentication */

    /* Set: sec_ssl_client_cert_cb, a static callback set to SSL library by
     *      SSL_CTX_set_client_cert_cb(), i.e.,
     *      ctx->client_cert_cb = sec_ssl_client_cert_cb(). */
    /* Ref: sec_ssl_get_certreq_auth_names() */
    kal_uint8           cert_types_len;     /* ssl3_get_certificate_request() */
    kal_uint8           cert_types[SEC_MAX_CERT_TYPES];

    /* Set: sec_ssl_client_cert_cb(), a static callback set to SSL library by
     *      SSL_CTX_set_client_cert_cb(), i.e.,
     *      ctx->client_cert_cb = sec_ssl_client_cert_cb(). */
    /* Ref: sec_ssl_get_certreq_auth_names() */
    kal_uint8           auth_name_cnt;
    sec_auth_names      auth_name[SEC_MAX_AUTH_NAMES];

    /* Set/Ref: sec_ssl_connect() */
    kal_uint8           resume;   /* 0:new session; 1:resuming; 2:resumed */

    /* Set: sec_ssl_read(), sec_ssl_write() */
    kal_int32           last_err;

    /* Set: sec_ssl_log_peer_cert_warnings() */
    /* Ref: sec_ssl_generic_cert_verify() */
    /* warnings at wrapper layer converted by cvrt_ssl_cert_warning() */
    kal_uint32          warns[SEC_MAX_CERT_CHAIN_LEN];

    /* OpenSSL level warning retrieved by X509_STORE_CTX_get_error() */
    kal_uint32          orig_ssl_warns[SEC_MAX_CERT_CHAIN_LEN];


#ifdef _CHECK_CNAME_
    /* Set: sec_ssl_check_peer_cert_name() */
    kal_char            *cname;
#endif /* _CHECK_CNAME_ */
//} ssl_conn; /* OpenSSL */
}; /* OpenSSL */


typedef enum {
    SEC_SSL_CA_FILE,
    SEC_SSL_CA_PATH
} ca_location_type;


typedef enum {
    SEC_SSL_READ,
    SEC_SSL_WRITE
} ssl_io_action;

#endif /* !_OSSL_SSL_INTERNAL_STRUCT_H_ */
