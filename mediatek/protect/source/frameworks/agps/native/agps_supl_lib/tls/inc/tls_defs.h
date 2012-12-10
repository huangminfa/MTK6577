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
 *   tls_socket.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   Header file of tls_socket.c
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
 * Apr 28 2010 mtk01264
 * [MAUI_02093135] TLS/SECLIB mem pool increase for Q03C STK
 * Reserver 12KB for Obigo over TLS
 *
 * Mar 31 2009 mtk01264
 * [MAUI_01655762] JAVA(OpenSSL)_ it can't get message
 * Support retrieving peer certificate on session resumption.
 *
 * Mar 17 2009 mtk01264
 * [MAUI_01646558] [TLS] Check-in OpenSSL and revise TLS
 * Check in revised TLS
 *
 * May 5 2008 mtk01264
 * [MAUI_00766879] [TLS] fix compile warning in MoDIS
 * Fix annoying MoDIS compile warnings.
 *
 * Nov 30 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Increase memory pool for 2 global contexts of Email
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
#ifndef _TLS_DEFS_H_
#define _TLS_DEFS_H_

/* per ctx: root_cert = 32*4=128; passwd=64; cert_fname=64; auth_fname=64;
 * per conn: invalid_cert=1024; conn_ctx=324; 
 * for 1 ctx with 1 conn: total = 1604 */
#ifdef OPERA_V10_BROWSER
#define TLS_MEM_SIZE  (1024 * 12)
#define MAX_TLS_MEM_HISTORY (500)
#else /* !OPERA_V10_BROWSER */
#define TLS_MEM_SIZE  (2000 * TLS_MAX_GLOBAL_CTX)
#define MAX_TLS_MEM_HISTORY (300)
#endif /* !OPERA_V10_BROWSER */


/* z:\tls\ctx_1\root_cert\, z:\tls\ctx_1\user_cert\, z:\tls\ctx_1\priv_key\ */

#define TLS_BASE_DIR                  "/data/agps_supl"//"z:\\@tls"
#define TLS_HOME_DIR                  "/data/agps_supl"//L"z:\\@tls"

#define TLS_CTX_DIR_TEMPLATE          TLS_BASE_DIR"/ctx_0"
#define TLS_CTX_DIR                   TLS_BASE_DIR"/ctx_%d"
#define TLS_ROOT_CERT_DIR_TEMPLATE    TLS_CTX_DIR_TEMPLATE"/root_cert"
#define TLS_ROOT_CERT_DIR             TLS_CTX_DIR"/root_cert"
#define TLS_USER_CERT_DIR_TEMPLATE    TLS_CTX_DIR_TEMPLATE"/user_cert"
#define TLS_USER_CERT_DIR             TLS_CTX_DIR"/user_cert"
#define TLS_PRIV_KEY_DIR_TEMPLATE     TLS_CTX_DIR_TEMPLATE"/priv_key"
#define TLS_PRIV_KEY_DIR              TLS_CTX_DIR"/priv_key"


#define TLS_CONN_DIR_TEMPLATE               TLS_BASE_DIR"/conn_00"
#define TLS_CONN_DIR                        TLS_BASE_DIR"/conn_%02d"
#define TLS_CONN_PEER_CERT_FILE_TEMPLATE    TLS_CONN_DIR_TEMPLATE"/peer_cert.der"
#define TLS_CONN_PEER_CERT_FILE             TLS_CONN_DIR"/peer_cert.der"
#define TLS_CONN_PEER_AUTH_NAMES_TEMPLATE   TLS_CONN_DIR_TEMPLATE"/authnames.der"
#define TLS_CONN_PEER_AUTH_NAMES            TLS_CONN_DIR"/authnames.der"

/***************************************************************************
 * Default supported SSL/TLS versions. Internal.
 * Ref. tls_new_ctx().
 ***************************************************************************/
#define TLS_DEFAULT_VERSION (TLSv1 | SSLv3)


#define TLS_UNUSED(var)     (void)(var)
#define TLS_MAX_BIT_SHIFT   (31)

#ifdef _DEFENCE_MITM_
#define TLS_SHA1_HASH_LENGTH        (20)
#endif /* _DEFENCE_MITM_ */


#define ASSOC_CONN_SET(ctx_id, s)    \
    (tls_global_ctx[(ctx_id)].assoc_conn[(s)/32] |= (0x1 << (TLS_MAX_BIT_SHIFT - ((s) % 32))))

#define ASSOC_CONN_CLR(ctx_id, s)    \
    (tls_global_ctx[(ctx_id)].assoc_conn[(s)/32] &= ~(0x1 << (TLS_MAX_BIT_SHIFT - ((s) % 32))))

#define ASSOC_CONN_ISSET(ctx, s)    \
    (tls_global_ctx[(ctx_id)].assoc_conn[(s)/32] & (0x1 << (TLS_MAX_BIT_SHIFT - ((s) % 32))))


#define HSHK_READY_NOTIFY_SET(ctx_id, s)    \
    (tls_global_ctx[(ctx_id)].hshk_ready_notify[(s)/32] |= (0x1 << (TLS_MAX_BIT_SHIFT - ((s) % 32))))

#define HSHK_READY_NOTIFY_CLR(ctx_id, s)     \
    (tls_global_ctx[(ctx_id)].hshk_ready_notify[(s)/32] &= ~(0x1 << (TLS_MAX_BIT_SHIFT - ((s) % 32))))

#define HSHK_READY_NOTIFY_ISSET(ctx_id, s)   \
    (tls_global_ctx[(ctx_id)].hshk_ready_notify[(s)/32] & (0x1 << (TLS_MAX_BIT_SHIFT - ((s) % 32))))


#define TLS_CHECK_READY()                                       \
    do {                                                        \
        if (!tls_ready)                                         \
        {                                                       \
            kal_lib_trace(TRACE_ERROR, TLS_TASK_NOT_READY);     \
            return TLS_ERR_TASK_NOT_READY;                      \
        }                                                       \
    } while (0)
    

#define TLS_GLOBAL_MUTEX_LOCK                                   \
    do {                                                        \
        kal_take_mutex(tls_global_lock);                        \
        sec_ssl_clear_library_error();                          \
    } while (0)

#define TLS_GLOBAL_MUTEX_UNLOCK                                 \
    do {                                                        \
        kal_give_mutex(tls_global_lock);                        \
    } while (0)

/* 
 * Format of xid between TLS and CERTMAN/MMI_CERTMAN
 *
 * cert type
 * +--------+--------+--------+--------+
 * |   CIUPR| ctx_id |conn_id |root_   |
 * |        |        |        |cert_cnt|
 * +--------+--------+--------+--------+
 */
#define ROOT_CERT        (0x01u << 24)  /* 0x01000000 */
#define PRIV_KEY         (0x01u << 25)  /* 0x02000000 */
#define USER_CERT        (0x01u << 26)  /* 0x04000000 */
#define INVALID_CERT     (0x01u << 27)  /* 0x08000000 */
#define CLIENT_AUTH      (0x01u << 28)  /* 0x10000000 */

#define tls_ctx_gen_xid(ctx_id, type)  (((ctx_id) << 16 )| (type))
#define xid_to_ctx(xid)                (((xid) >> 16) & 0xFF)
#define xid_to_ctx_certtype(xid)       ((xid) & (0xFFu << 24))


/* 
 * Format of xid between TLS and CERTMAN/MMI_CERTMAN
 *
 * cert_type
 * +--------+--------+--------+--------+
 * |   CIUPR| ctx_id |conn_id |root_   |
 * |        |        |        |cert_cnt|
 * +--------+--------+--------+--------+
 */
#define tls_conn_gen_xid(s, type)     ((s) << 8 | (type))
#define xid_to_conn(xid)              (((xid) >> 8) & 0xFF )
#define xid_to_conn_certtype(xid)     ((xid) & (0xFFu << 24))



/* 
 * Format of xid between TLS and CERTMAN/MMI_CERTMAN
 *
 * cert_type
 * +--------+--------+--------+--------+
 * |   CIUPR| ctx_id |conn_id |root_   |
 * |        |        |        |cert_cnt|
 * +--------+--------+--------+--------+
 */
#define tls_wait_cert_cnt(ctx_id)  ((tls_global_ctx[(ctx_id)].wait_cert) & 0xFF )



#ifdef __MTK_TARGET__
extern int sprintf(char *, const char *, ...);
#endif /* __MTK_TARGET__ */

#endif /* !_TLS_DEFS_H_ */


