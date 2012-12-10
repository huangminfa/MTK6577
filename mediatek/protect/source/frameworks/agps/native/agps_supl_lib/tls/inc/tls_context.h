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
 *   tls_context.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   This file describes the context structuring variables for TLS task.
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
 * Dec 1 2009 mtk01264
 * [MAUI_02003623] [Change feature Pre SQC] Affect the settings of the Online certif check (on\Off). an
 * Invalidate the SSL session cache if OCSP is switched from OFF to ON or URL is changed to eliminate the impact of session cache
 *
 * Nov 24 2009 mtk01264
 * [MAUI_01996819] [OCSP] Check-in OCSP for OpenSSL solution
 * Enable OCSP support in TLS handshake
 *
 * Mar 17 2009 mtk01264
 * [MAUI_01646558] [TLS] Check-in OpenSSL and revise TLS
 * Check in revised TLS
 *
 * Jan 21 2008 mtk01264
 * [MAUI_00609426] [TLS] remove unused variable in global context
 * remove unused variable
 *
 * Dec 19 2007 mtk01264
 * [MAUI_00593347] [Email] Assert fail: 0 tls_common.c 687 - TLS
 * Reduce write fragment parameter to avoid memory fragmentation
 *
 * Dec 17 2007 mtk01264
 * [MAUI_00592313] [TLS] Add new code to guard TLS memory management
 * Add tls_check_mem_leak() to detect memory leak
 *
 * Dec 6 2007 mtk01264
 * [MAUI_00060206] Display application name in the notification screen
 * Change tls_new_ctx() prototype to pass module name in string id
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
 *============================================================================
 ****************************************************************************/

#ifndef _TLS_CONTEXT_H_
#define _TLS_CONTEXT_H_

#ifndef _SSL_API_H
#error "ssl_api.h should be included before tls_context.h"
#endif /* !_SSL_API_H */

#ifndef _SOC_API_H
#error "soc_api.h should be included before tls_context.h"
#endif /* !_SOC_API_H */

#ifndef _TLS_CONST_H_
#error "tls_const.h should be included before tls_context.h"
#endif /* !_TLS_CONST_H_ */

#ifndef _TLS_APP_ENUMS_H_
#error "tls_app_enums.h should be included before tls_context.h"
#endif /* !_TLS_APP_ENUMS_H_ */

#ifndef _TLS_CALLBACK_H_
#error "tls_callback.h should be included before tls_context.h"
#endif /* !_TLS_CALLBACK_H_ */

#define TLS_MAX_FILENAME_LEN (130)
#define TLS_PASSWD_BUF_LEN   (64)
#define TLS_MAX_MOD_NAME_LEN (12)
#define TLS_MAX_AUTHNAMES    (32)

#ifdef _DEFENCE_MITM_
/* filled while sending invalid certificate indication to app.
 * Associate tls_conn with this record at tls_check_invalid_cert(). */
typedef struct {
    kal_uint32  time;
    kal_uint8   ip_addr[4];
    kal_uint16  port;
    kal_uint8   fingerprint[TLS_SHA1_HASH_LENGTH];
} tls_cert_fngrpt_record;
#endif /* _DEFENCE_MITM_ */


#ifdef __OCSP_SUPPORT__
/* referenced and filled while performing OCSP after successful handshake. */
typedef struct {
    kal_uint32  time;
    kal_uint8   ip_addr[4];
    kal_uint16  port;
    kal_bool    use_ocsp;
    kal_char*   ocsp_url;
} tls_ocsp_settings_record;
#endif /* __OCSP_SUPPORT__ */


typedef struct
{
    /* tls_new_ctx */
    ssl_ctx                  *ctx;          /* sec_ssl_ctx_new()/sec_ssl_ctx_free() */
    kal_uint16               app_str;

    /* context state */
    #define TLS_CTX_INUSE    (0x0001)
    kal_uint16               state;

    module_type              app_mod_id;
    tls_version_enum         version;
    tls_side_enum            side;

    /* tls_set_ciphers */
    sec_cipher_enum          ciphers[TLS_MAX_CIPHERS];
    kal_int32                cipher_num;

    /* tls_set_verify */
    kal_uint32               *root_cert_id;   /* pointer to an array of cert ids end by 0.
                                               * tls_malloc()/tls_mfree() */
    kal_wchar                ca_path[TLS_MAX_FILENAME_LEN +1];
    tls_cert_verify_callback verify_callback;
    void                     *verify_callback_arg;   /* pointer to this structure */
    
    #define                  CIPHER_LIST        (0x01u << 0)
    #define                  VERIFY_CERTS       (0x01u << 1)
    #define                  VERIFY_CALLBACK    (0x01u << 2)
    #define                  ALERT_CALLBACK     (0x01u << 3)
    #define                  IO_CALLBACK        (0x01u << 4)

    /* raise the flag when each default parameter is set */
    kal_uint32               set_custom;
    kal_uint32               set_default;

    /* tls_set_client_auth */
    ssl_auth_mode_enum       client_auth_modes[TLS_MAX_CLIENT_AUTH];

    /* tls_set_passwd_callback */
    kal_uint8                passwd[TLS_PASSWD_BUF_LEN];  /* random passwd */
    kal_uint32               passwd_len;
    tls_passwd_callback      certman_passwd_callback;
    void*                    certman_passwd_userdata;
    tls_passwd_callback      ssl_passwd_callback;
    void*                    ssl_passwd_userdata;

    /* tls_set_identity */
    kal_uint32               user_cert_id;
    kal_wchar                privkey_file[TLS_MAX_FILENAME_LEN +1];
    kal_wchar                user_cert_file[TLS_MAX_FILENAME_LEN +1];
    tls_filetype_enum        privkey_type;

    /* CERTMAN */
    kal_uint32 root_cert_xid;
    kal_uint32 user_cert_xid;
    kal_uint32 priv_key_xid;

    /* misc */
    /* a socket id associated to this global context */
    kal_uint32               assoc_conn[(MAX_IP_SOCKET_NUM+31) / 32];
    /* a socket id is waiting for notification of HANDSHAKE_READY */
    kal_uint32               hshk_ready_notify[(MAX_IP_SOCKET_NUM+31) / 32];
    kal_bool                 set_ctx_failed;
 
    kal_uint32               wait_cert;  /* ROOT_CERT, PRIV_KEY, USER_CERT */
    sockaddr_struct          *faddr[MAX_IP_SOCKET_NUM];
} tls_context_struct;


typedef struct {
    ssl_conn            *conn_ctx;  /* sec_ssl_new()/sec_ssl_free() */
    kal_int8            socket_id;
    tls_side_enum       side;
    module_type         app_mod_id;

    tls_context_struct  *ctx;       /* backward pointer to global context */
    tls_state_enum      state;

    /* auxiliary flags for remembering states */
    #define TLS_AUTO_REHANDSHAKE      (0x01)
    #define TLS_REHANDSHAKING         (0x02)
    #define TLS_HANDSHAKE_REQUESTED   (0x04)
    kal_uint32          flags;

    #define RCVD_INVALID_CERT     (0x01u << 0)
    #define INVALID_CERT_NOTIFIED (0x01u << 1)
    #define RCVD_CLIENT_AUTH      (0x01u << 2)
    #define CLIENT_AUTH_NOTIFIED  (0x01u << 3)
    #define OCSP_VERIFYING        (0x01u << 4)
    #define OCSP_VERIFIED         (0x01u << 5)
    #define PROCESSING_HANDSHAKE  (RCVD_INVALID_CERT | RCVD_CLIENT_AUTH | OCSP_VERIFYING)

    kal_uint32          cert_state; /* handling cert with mmi_certman or certman */

    /* invalid certificate */
    kal_bool            check_cert;
#ifdef _DEFENCE_MITM_
    tls_cert_fngrpt_record  *server_cert_fngrpt;
#endif /* _DEFENCE_MITM_ */
    kal_uint8           *peer_cert;             /* tls_malloc()/tls_mfree() */
    kal_uint32          peer_cert_len;
    kal_uint32          peer_cert_warning;
    kal_wchar           *peer_cert_filename;    /* tls_malloc()/tls_mfree() */

#ifdef __OCSP_SUPPORT__
    kal_uint8           ocsp_tarns_id;

    kal_uint8           *peer_cert_issuer;      /* tls_malloc()/tls_mfree() */
    kal_uint32          peer_cert_issuer_len;
#endif /* __OCSP_SUPPORT__ */

    /* client authentication */
    sec_cert_types      cert_type;      /* len, types[SEC_MAX_CERT_TYPES], in raw format */
    kal_uint8           auth_name_cnt;
    sec_auth_names      auth_names[TLS_MAX_AUTHNAMES]; /* just write them to files */
    kal_wchar           *certauth_filename;     /* tls_malloc()/tls_mfree() */
} tls_conn_context_struct;


typedef enum {
    SOCKET_UNKNOWN_MSG_TYPE   = 0,              /* 0x00 */

    BEARER_INFO_ACTIVATINGING = 0x01u << 1,     /* 0x01 */
    BEARER_INFO_ACTIVATED     = 0x01u << 2,     /* 0x02 */
    BEARER_INFO_DEACTIVATING  = 0x01u << 3,     /* 0x04 */
    BEARER_INFO_DEACTIVATED   = 0x01u << 4,     /* 0x08 */
    BEARER_INFO_IDLE_TIMEOUT  = 0x01u << 5,     /* 0x10 */

    DEACTIVATE_CNF            = 0x01u << 6      /* 0x20 */
} tls_soc_msg_enum;

typedef struct {
    module_type app_mod_id;
} tls_socket_fw_mod_struct;


typedef struct {
    kal_uint32 entries;
    kal_uint32 peak_size;
    kal_uint32 curr_size;
} tls_ctx_memory_stat_struct;


typedef struct {
    kal_uint32 peak_size;
    kal_uint32 curr_size;

    kal_uint32 malloc_fail;

    /* number of allocated block <= N */
    kal_uint32 num_16;
    kal_uint32 peak_16;

    kal_uint32 num_32;
    kal_uint32 peak_32;

    kal_uint32 num_64;
    kal_uint32 peak_64;

    kal_uint32 num_128;
    kal_uint32 peak_128;

    kal_uint32 num_256;
    kal_uint32 peak_256;

    kal_uint32 num_512;
    kal_uint32 peak_512;

    kal_uint32 num_1024;
    kal_uint32 peak_1024;
    
    kal_uint32 num_huge;
    kal_uint32 peak_huge;
} tls_memory_stat_struct;

extern kal_bool tls_ready;
extern tls_context_struct tls_global_ctx[];
extern tls_conn_context_struct *tls_conn_ctx[];
extern tls_socket_fw_mod_struct tls_socket_fw_mod_map[];
#ifdef _DEFENCE_MITM_
extern tls_cert_fngrpt_record tls_peer_cert_db[TLS_MAX_GLOBAL_CTX << 1];
#endif /* _DEFENCE_MITM_ */
extern kal_mutexid tls_global_lock;
extern KAL_ADM_ID tls_mem_adm_id;
extern kal_uint32 tls_adm_pool[];

#endif /* !_TLS_CONTEXT_H_ */

