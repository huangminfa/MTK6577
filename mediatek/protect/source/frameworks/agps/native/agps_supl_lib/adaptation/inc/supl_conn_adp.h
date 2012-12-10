/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2006
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
 *  supl_conn_adp.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   The AGPS SWIP adaption layer.
 *
 * Author:
 * -------
 *  Jinghan Wang
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
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

/* network.h */
#ifndef __CONNECTION_H__
#define __CONNECTION_H__

#include "typedef.h"
#include "tls_app_enums.h"

typedef void* (*tls_malloc_callback)(kal_uint32 size, void* ref);
typedef void (*tls_mfree_callback)(void* buff, void* ref);

/*****************************************************************************
 * FUNCTION
 *  tls_new_ctx
 *
 * DESCRIPTION
 *   create TLS global context
 *
 * PARAMETERS
 *  ver                  supported SSL/TLS versions
 *  side                 client or server side
 *  malloc_callback      callback function for memory allocation
 *  mfree_callback       callback function for memory free
 *  app_str_id           string id to display in MMI on error
 *
 * RETURN
 *  >=0                       created global context id (uint8)
 *  TLS_ERR_INVALID_PARAMS    invalid parameters
 *  TLS_ERR_NO_FREE_CTX       all context are occupied
 *  TLS_ERR_PRNG_FAIL         Set PRNG failed
 *  TLS_ERR_NO_MEMORY         no memory available for allocating global context
 *  TLS_ERR_SSL_INTERNAL      SSL layer internal error
 *****************************************************************************/
extern kal_int32 tls_new_ctx(tls_version_enum ver,
                             tls_side_enum side,
                             module_type mod_id,
                             kal_uint16 app_str_id);



/*****************************************************************************
 * FUNCTION
 *  tls_delete_ctx
 *
 * DESCRIPTION
 *   delete TLS global context
 *
 * PARAMETERS
 *   ctx_id      global context id to be freed
 *
 * RETURN
 *   TLS_ERR_NONE               delete global context successfully
 *   TLS_ERR_INVALID_CONTEXT    attempt to delete context with invalid id
 *   TLS_ERR_CHECK_MODULE       check module id failed
 *****************************************************************************/
extern kal_int32 tls_delete_ctx(kal_uint8 ctx_id);

/*****************************************************************************
 * FUNCTION
 *   tls_new_conn
 *
 * DESCRIPTION
 *   create TLS connection context associated with given socket id
 *
 * PARAMETERS
 *   ctx_id      global context id storing the setting to create connection ctx
 *   sock_id     socket id to associate with the newly created connection ctx
 *   faddr       destination address to be connected to
 *
 * RETURN
 *   TLS_ERR_NONE                   create connection ocntext successfully
 *   TLS_ERR_INVALID_CONTEXT        given an invalid global context id
 *   TLS_ERR_CHECK_MODULE           check module id failed
 *   TLS_ERR_INVALID_SOCK_ID        given an invalid socket id
 *   TLS_ERR_ALREADY                the socket id is already associated to a ctx
 *   TLS_ERR_SSL_INTERNAL           SSL layer internal error
 *   TLS_ERR_WAITING_CERT           loading certificate from CERTMAN, 
 *                                  application should wait for
 *                                  TLS_HANDSHAKE_READY notification
 *****************************************************************************/
extern kal_int32 tls_new_conn(kal_uint8 ctx_id, kal_int8 sock_id, sockaddr_struct *faddr);



/*****************************************************************************
 * FUNCTION
 *   tls_delete_conn
 *
 * DESCRIPTION
 *   delete TLS connection context
 *
 * PARAMETERS
 *   sock_id        socket id associated with the connection ctx to be freed
 *
 * RETURN
 *   TLS_ERR_NONE                connection context deleted successfully
 *   TLS_ERR_NO_CONN_CTX         no associated connection context
 *   TLS_ERR_INVALID_SOCK_ID     given an invalid socket id
 *   TLS_ERR_CHECK_MODULE        check module id failed
 *****************************************************************************/
extern kal_int32 tls_delete_conn(kal_int8 sock_id);



/*****************************************************************************
 * FUNCTION
 *   tls_check_invalid_certificate
 *
 * DESCRIPTION
 *   Enalbe/Disable checking invalid certificate from peer
 *
 * PARAMETERS
 *   sock_id        socket id associate with the connection ctx to enable/disable
 *                  the invalid certificate check
 *   onoff          checking an invlid certificate or not
 *
 * RETURN
 *   TLS_ERR_NONE                 set the property successfully
 *   TLS_ERR_INVALID_SOCK_ID      given an invalid socket id
 *   TLS_ERR_NO_CONN_CTX          no associated connection context
 *   TLS_ERR_CHECK_MODULE         check module id failed
 *****************************************************************************/
kal_int32 tls_check_invalid_cert(kal_int8 sock_id, kal_bool onoff);


/*****************************************************************************
 * FUNCTION
 *   tls_handshake
 *
 * DESCRIPTION
 *   Initiate TLS handshake process
 *
 * PARAMETERS
 *   sock_id        socket id associate with the connection ctx to
 *                  perform handshake
 *
 * RETURN
 *   TLS_ERR_NONE              handshake completes successfully
 *   TLS_ERR_NO_CONN_CTX       no associated connection context
 *   TLS_ERR_INVALID_SOCK_ID   given an invalid socket id
 *   TLS_ERR_CHECK_MODULE      check module id failed
 *   TLS_ERR_WOULDBLOCK        the operation can not be complete currently,
 *                             please wait for notification indication
 *   TLS_ERR_ALREADY           the handshake process is progressing
 *   TLS_ERR_HANDSHAKED        TLS connection is handshaked.
 *                             tls_handshake() can only be called before
 *                             handshaked, application should call
 *                             tls_rehandshake() to initiate re-handshake
 *   TLS_ERR_SSL_INTERNAL      SSL layer internal error
 *   TLS_ERR_OP_DENIED         operation denied for incorrect connection state
 *****************************************************************************/
extern kal_int32 tls_handshake(kal_int8 sock_id);



/*****************************************************************************
 * FUNCTION
 *   tls_handshake
 *
 * DESCRIPTION
 *   Perfrom TLS rehandshake process
 *
 * PARAMETERS
 *   sock_id        socket id associate with the connection ctx to
 *                  perform re-handshake
 *
 * RETURN
 *   TLS_ERR_NONE               re-handshake completes successfully
 *   TLS_ERR_NO_CONN_CTX        no associated connection context
 *   TLS_ERR_INVALID_SOCK_ID    given an invalid socket id
 *   TLS_ERR_CHECK_MODULE       check module id failed
 *   TLS_ERR_WOULDBLOCK         the operation can not be complete currently,
 *                              please wait for notification indication
 *   TLS_ERR_ALREADY            the re-handshake process is progressing
 *   TLS_ERR_SSL_INTERNAL       SSL layer internal error
 *   TLS_ERR_OP_DENIED          operation denied for incorrect connection state
 *****************************************************************************/
extern kal_int32 tls_rehandshake(kal_int8 sock_id);



/*****************************************************************************
 * FUNCTION
 *   tls_auto_rehandshake
 *
 * DESCRIPTION
 *   Enalbe/Disable TLS auto rehandshake property
 *
 * PARAMETERS
 *   sock_id        socket id associate with the connection ctx to set
 *                  the auto-rehandshake property
 *
 * RETURN
 *   TLS_ERR_NONE                 set the property successfully
 *   TLS_ERR_INVALID_SOCK_ID      given an invalid socket id
 *   TLS_ERR_NO_CONN_CTX          no associated connection context
 *   TLS_ERR_CHECK_MODULE         check module id failed
 *****************************************************************************/
extern kal_int32 tls_auto_rehandshake(kal_int8 sock_id, kal_bool onoff);



/*****************************************************************************
 * FUNCTION
 *   tls_read
 *
 * DESCRIPTION
 *   read data from established TLS connection
 *
 * PARAMETERS
 *   sock_id         socket id associate with the connection ctx to read data
 *   buf             buffer to store received data
 *   len             number of bytes to be read from the connection
 *
 * RETURN
 *   >0                        bytes of successful read data
 *   TLS_ERR_INVALID_SOCK_ID   given an invalid socket id
 *   TLS_ERR_NO_CONN_CTX       no associated connection context
 *   TLS_ERR_CHECK_MODULE      check module id failed
 *   TLS_ERR_WOULDBLOCK        the lower-layer returns WOULDBLOCK
 *   TLS_ERR_HANDSHAKING       the connection is handshaking
 *   TLS_ERR_REHANDSHAKING     the connection is re-handshaking
 *   TLS_ERR_REHANDSHAKED      auto rehandshaked, application can read again
 *   TLS_ERR_REQ_HANDSHAKE     peer requestes re-handshake over a connected
 *                             connection
 *   TLS_ERR_IO_ERROR          lower-layer socket IO error
 *   TLS_ERR_NEED_HANDSHAKE    need to perform handshake before reading data
 *   TLS_ERR_REHANDSHAKE_REJ   peer rejects our renegotiation request, but the
 *                             connection is still valid
 *   TLS_ERR_CONN_CLOSED       connection closed by peer
 *   TLS_ERR_SSL_INTERNAL      SSL layer internal error
 *   TLS_ERR_OP_DENIED         operation denied for incorrect connection state
 *****************************************************************************/
extern kal_int32 tls_read(kal_int8 sock_id, kal_uint8* buf, kal_int32 len);



/*****************************************************************************
 * FUNCTION
 *   tls_write
 *
 * DESCRIPTION
 *   write data to established TLS connection
 *
 * PARAMETERS
 *   sock_id         socket id associate with the connection ctx to write data
 *   buf             buffer storing the outgoing data
 *   len             number of bytes to be write to the connection
 *
 * RETURN
 *   >0                        bytes of successful written data
 *   TLS_ERR_INVALID_SOCK_ID   given an invalid socket id
 *   TLS_ERR_NO_CONN_CTX       no associated connection context
 *   TLS_ERR_CHECK_MODULE      check module id failed
 *   TLS_ERR_WOULDBLOCK        the lower-layer returns WOULDBLOCK
 *   TLS_ERR_HANDSHAKING       the connection is handshaking
 *   TLS_ERR_REHANDSHAKING     the connection is re-handshaking
 *   TLS_ERR_IO_ERROR          lower-layer socket IO error
 *   TLS_ERR_REHANDSHAKE_REJ   peer rejects our renegotiation request, but the
 *                             connection is still valid
 *   TLS_ERR_CONN_CLOSED       connection closed by peer
 *   TLS_ERR_NEED_HANDSHAKE    need to perform handshake before reading data
 *   TLS_ERR_SSL_INTERNAL      SSL layer internal error
 *   TLS_ERR_OP_DENIED         operation denied for incorrect connection state
 *****************************************************************************/
extern kal_int32 tls_write(kal_int8 sock_id, kal_uint8* buf, kal_int32 len);



/*****************************************************************************
 * FUNCTION
 *   tls_shutdown
 *
 * DESCRIPTION
 *   close TLS connection
 *
 * PARAMETERS
 *   sock_id         socket id associate with the connection ctx to be closed
 *
 * RETURN
 *   TLS_ERR_NONE               close the TLS connection successfully
 *   TLS_ERR_INVALID_SOCK_ID    given an invalid socket id
 *   TLS_ERR_NO_CONN_CTX        no associated connection context
 *   TLS_ERR_CHECK_MODULE       check module id failed
 *   TLS_ERR_SSL_INTERNAL       SSL layer internal error
 *   TLS_ERR_OP_DENIED          operation denied for incorrect connection state
 *****************************************************************************/
extern kal_int32 tls_shutdown(kal_int8 sock_id);



/*****************************************************************************
 * FUNCTION
 *   tls_connect
 *
 * DESCRIPTION
 *   Establish TCP connection and perform TLS handshake
 *
 * PARAMETERS
 *   sock_id         socket id associate with the connection ctx to set up
 *                   connection and initiate TLS handshake
 *
 * RETURN
 *   TLS_ERR_NONE               the action completes without any error
 *   TLS_ERR_INVALID_SOCK_ID    given an invalid socket id
 *   TLS_ERR_NO_CONN_CTX        no associated connection context
 *   TLS_ERR_CHECK_MODULE       check module id failed
 *   TLS_ERR_WOULDBLOCK         the lower-layer returns WOULDBLOCK
 *   TLS_ERR_SOC_INTERNAL       socket layer internal error
 *   TLS_ERR_SSL_INTERNAL       SSL layer internal error
 *   TLS_ERR_OP_DENIED          operation denied for incorrect connection state
 *****************************************************************************/
extern kal_int32 tls_connect(kal_int8 sock_id, sockaddr_struct *addr);


#endif /* __CONNECTION_H__ */
