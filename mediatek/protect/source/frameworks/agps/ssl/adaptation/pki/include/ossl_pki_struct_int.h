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
 *    ossl_pki_struct_int.h
 *
 * Project:
 * --------
 *    MAUI
 *
 * Description:
 * ------------
 *    PKI Internal Structure Definitions
 *
 * Author:
 * -------
 *    Will Chen
 *
 *==============================================================================
 * 				HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Apr 28 2009 mtk01476
 * [MAUI_01677876] [PKI]DOM SAP code revise
 * 
 *
 * Mar 17 2009 mtk01476
 * [MAUI_01646633] [Port OpenSSL]New feature(Certman+PKI) check-in
 * add to source control recursely
 *
 * 
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *============================================================================== 
 *******************************************************************************/
#ifndef OSSL_PKI_STRUCT_INT_H
#define OSSL_PKI_STRUCT_INT_H

#ifndef OSSL_PKI_INT_INCLUDES_H
#error "ossl_pki_struct_int.h cannot be included outside PKI scope"
#endif

#include "openssl/x509.h"
#include "openssl/evp.h"
#include "openssl/pkcs7.h"
#include "openssl/pkcs12.h"
#include "ossl_defs.h"

typedef struct pki_context_struct pki_context_struct;

typedef struct {
    void    *data;
    kal_uint32 size;
    X509    *cert_ctx;
} pki_x509_struct;


typedef struct {
    int name_str_nid;
    kal_uint32 max_buf_size;
} pki_name_mapping_struct;


typedef struct
{
    pki_pubkey_type_enum keytype;
    EVP_PKEY    *evp_pkey;
    void        *data;
    kal_uint32  size;
} pki_pubkey_struct;

typedef struct
{
    EVP_PKEY *evp_pkey;
} pki_privkey_struct;

typedef struct {
    PKCS8_PRIV_KEY_INFO *p8;
    void *data;
    kal_uint32 size;
} pki_pkcs8key_struct;

typedef struct {
    void *data;
    kal_uint32 size;
} pki_pkcs8enckey_struct;


typedef struct {
    PKCS7  *p7_dec_ctx;
} pki_pkcs7dec_struct;

typedef struct 
{
    kal_uint32 p7_index;
    kal_uint32 bag_index;
    STACK_OF(PKCS7)             *p7_stack;
    STACK_OF(PKCS12_SAFEBAG)    *bags_stack;
} pki_pkcs12dec_struct;


typedef struct
{
    X509_STORE                  *pki_store;
    STACK_OF(X509)              *untrusted_cert_sk;
    STACK_OF(X509)              *chain;    
    X509_STORE_CTX              pki_store_ctx;
    kal_char                    verify_path[OSSL_ADP_MAX_PATH_LEN + 1];
    kal_uint32                  val_warning;
    pki_val_warning_enum        val_warnings[10];    
    pki_filetype_enum           fileType;
    pki_valusage_enum           usage;
} pki_validate_struct;



#ifdef PKI_PKCS_EXORT
typedef struct {
    pki_context_struct *pki_ctx;
    pki_pkcs7_cnttype_enum cnttype;

    tp_CmsSignedEncodeCtx signed_enc_ctx;
} pki_pkcs7enc_struct;

typedef struct pki_safebagenc_struct pki_safebagenc_struct;
typedef struct pki_safebagenc_struct
{
    pki_safebagenc_struct *next;

    pki_context_struct *pki_ctx;
    pki_safebag_type_enum safebag_type; 
    void *data;
    kal_uint32 size; 
    tp_P12SafeBagEncodeCtx safebag_ctx;
} pki_safebagenc_struct;


typedef struct pki_safecntenc_struct pki_safecntenc_struct;
typedef struct pki_safecntenc_struct
{
    pki_safecntenc_struct *next;

    pki_context_struct *pki_ctx;
    kal_uint8 passenc;
    char *pwd;
    kal_uint32 pwd_size;
    pki_safebagenc_struct *safebag_head;
    tp_P12SafeContentsEncodeCtx safecontent_ctx;
} pki_safecntenc_struct;


typedef struct 
{
    pki_context_struct *pki_ctx;
    pki_safecntenc_struct *safecontent_head;
    tp_P12AuthSafeEncodeCtx authsafe_ctx;
} pki_pkcs12enc_struct;
#endif /* PKI_PKCS_EXORT */

#endif  /* OSSL_PKI_STRUCT_INT_H */
