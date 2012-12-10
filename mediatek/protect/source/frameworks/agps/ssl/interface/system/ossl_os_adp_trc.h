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
 *   ossl_os_adp_trc.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   This is openssl system adaptation trace map definition.
 *
 * Author:
 * -------
 *   Will Chen
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
 * Mar 20 2009 mtk01476
 * [MAUI_01650512] [Port OpenSSL]fine tune certman memory pool size
 * 
 *
 * Mar 17 2009 mtk01476
 * [MAUI_01646633] [Port OpenSSL]New feature(Certman+PKI) check-in
 * add to source control recursely
 *
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef _OSSL_OS_ADP_TRC_H_
#define _OSSL_OS_ADP_TRC_H_

#ifndef GEN_FOR_PC

#ifndef _STACK_CONFIG_H
    #error  "stack_config.h should be included before tst_config.h"
#endif /* _STACK_CONFIG_H */

#else /* GEN_FOR_PC */ 
#include "kal_trace.h"
#endif /* GEN_FOR_PC */ 


#ifndef _KAL_TRACE_H
    #error "kal_trace.h should be included before tst_trace.h"
#endif /* _KAL_TRACE_H */

//#define TRACE_OSSL_ADP_MEMORY       (TRACE_GROUP_1)
#define TRACE_OSSL_ADP_CHE          (TRACE_GROUP_2)

#if __AGPS_SWIP_REL__
    /* ----------------- TRACE_FUNC trace class ------------------- */
    /* ossl_lpdir.c */
    #define OSSL_LP_FIND_FILE "lp_find_file(directory: %s, ctx: %d)"
    #define OSSL_LP_FIND_FILE_END "lp_find_file_end(ctx: %d, *ctx: %d)"

    /* ossl_fs.c */
    #define OSSL_ADP_GETMODE "ossl_adp_getmode(mode: %s, flag: %d)"


    /* ----------------- TRACE_STATE trace class ------------------- */

    /* ----------------- TRACE_INFO trace class ------------------- */
    /* ossl_lpdir.c */
    #define OSSL_LP_FIND_FILE_FIRST_FAIL "lp_find_file()-> ret: %d = FS_FindFirst()"
    #define OSSL_LP_FIND_FILE_NEXT_FAIL "lp_find_file()-> FS_FindNext failed"
    #define OSSL_LP_FIND_FILE_END_CLOSE_FAIL "lp_find_file_end()-> ret: %d = FS_FindClose()"

    /* ossl_fs.c */
    #define OSSL_ADP_STAT "ossl_adp_stat()-> ret: %d = FS_GetAttributes()"
    #define OSSL_ADP_FOPEN "ossl_adp_fopen(file_pointer: %d, Open_flag: %d)"
    #define OSSL_ADP_FOPEN_FAIL "ossl_adp_fopen -> ret: %d = FS_Open()"
    #define OSSL_ADP_FGETS "ossl_adp_fgets(buf: %d, size: %d)"
    #define OSSL_ADP_FGETS_FOUND_LINE "ossl_adp_fgets() -> found line"
    #define OSSL_ADP_FGETS_ERR "ossl_adp_fgets(FS_read(ret: %d, read: %d), start from EOF?(%d))"
    #define OSSL_ADP_GETMODE_FLAG "ossl_adp_getmode(flag: %d)"
    /* ----------------- TRACE_WARNING trace class ------------------- */

    /* ----------------- TRACE_ERROR trace class ------------------- */

    /* ----------------- TRACE_GROUP_1 trace class ------------------- */
    /* ossl_trace.c */
    #define OSSL_ADP_TRACE_CLASS "ossl_adp_trace_class(trace: %d, lib: %d)"   
    /* ossl_mutex.c */
    #define OSSL_ADP_MUTEX_INIT "ossl_adp_mutex_init"
    #define OSSL_ADP_LOCKING_CALLBACK "ossl_adp_locking_callback(mode:%d, mutex_id: %d, file: %s, line: %d)"

    /* ----------------- TRACE_GROUP_2(CHE) trace class ------------------- */
    #define OSSL_ADP_MD4_INIT_FUNC "ossl_adp_MD4_Init(), &MD4_CTX = %d"
    #define OSSL_ADP_MD4_UPDATE_FUNC "ossl_adp_MD4_Update(), &MD4_CTX = %d"
    #define OSSL_ADP_MD4_FINAL_FUNC "ossl_adp_MD4_Final(), &MD4_CTX = %d"
    #define OSSL_ADP_MD4_FUNC "ossl_adp_MD4(), &output_md = %d"
    #define OSSL_ADP_MD5_INIT_FUNC "ossl_adp_MD5_Init(), &MD5_CTX = %d"
    #define OSSL_ADP_MD5_UPDATE_FUNC "ossl_adp_MD5_Update(), &MD5_CTX = %d"
    #define OSSL_ADP_MD5_FINAL_FUNC "ossl_adp_MD5_Final(), &MD5_CTX = %d"
    #define OSSL_ADP_MD5_FUNC "ossl_adp_MD5(), &output_md = %d"
    #define OSSL_ADP_SHA1_INIT_FUNC "ossl_adp_SHA1_Init(), &SHA_CTX = %d"
    #define OSSL_ADP_SHA1_UPDATE_FUNC "ossl_adp_SHA1_Update(), &SHA_CTX = %d"
    #define OSSL_ADP_SHA1_FINAL_FUNC "ossl_adp_SHA1_Final(), &SHA_CTX = %d"
    #define OSSL_ADP_SHA1_FUNC "ossl_adp_SHA1(), &output_md = %d"
    #define OSSL_ADP_RC4_SET_KEY_FUNC_INV_KEY "ossl_adp_RC4_set_key() - kay data == NULL"
    #define OSSL_ADP_RC4_SET_KEY_FUNC "ossl_adp_RC4_set_key(), &RC4_KEY = %d"
    #define OSSL_ADP_RC4_FUNC "ossl_adp_RC4(), &RC4_KEY = %d"
    #define OSSL_ADP_AES_SET_KEY_FUNC_INV_KEY "ossl_adp_AES_set_key() - kay data == NULL"
    #define OSSL_ADP_AES_SET_KEY_FUNC "ossl_adp_AES_set_key(), &AES_KEY = %d"
    #define OSSL_ADP_AES_CBC_ENCRYPT_FUNC "ossl_adp_AES_cbc_encrypt(), &AES_KEY = %d"
    #define OSSL_ADP_AES_CTX_CLEANUP_FUNC "ossl_adp_AES_ctx_cleanup(), &AES_KEY = %d"
    #define OSSL_ADP_DES_SET_KEY_FUNC_INV_KEY "ossl_adp_DES_set_key() - kay data == NULL"
    #define OSSL_ADP_DES_SET_KEY_FUNC "ossl_adp_DES_set_key(), &DES_KEY = %d"
    #define OSSL_ADP_DES_CBC_ENCRYPT_FUNC "ossl_adp_DES_cbc_encrypt(), &DES_KEY = %d"
    #define OSSL_ADP_DES_CTX_CLEANUP_FUNC "ossl_adp_DES_ctx_cleanup(), &DES_KEY = %d"
    #define OSSL_ADP_3DES_SET_KEY_FUNC_INV_KEY "ossl_adp_3DES_set_key() - kay data == NULL"
    #define OSSL_ADP_3DES_SET_KEY_FUNC "ossl_adp_3DES_set_key(), &3DES_KEY = %d, key_num=%d"
    #define OSSL_ADP_3DES_CBC_ENCRYPT_FUNC "ossl_adp_3DES_cbc_encrypt(), &3DES_KEY = %d"
    #define OSSL_ADP_3DES_CTX_CLEANUP_FUNC "ossl_adp_3DES_ctx_cleanup(), &3DES_KEY = %d"
#else
BEGIN_TRACE_MAP(MOD_OSSL_OSADP)

    /* ----------------- TRACE_FUNC trace class ------------------- */
    /* ossl_lpdir.c */
    TRC_MSG(OSSL_LP_FIND_FILE, "lp_find_file(directory: %s, ctx: %d)")
    TRC_MSG(OSSL_LP_FIND_FILE_END, "lp_find_file_end(ctx: %d, *ctx: %d)")

    /* ossl_fs.c */
    TRC_MSG(OSSL_ADP_GETMODE, "ossl_adp_getmode(mode: %s, flag: %d)")


    /* ----------------- TRACE_STATE trace class ------------------- */

    /* ----------------- TRACE_INFO trace class ------------------- */
    /* ossl_lpdir.c */
    TRC_MSG(OSSL_LP_FIND_FILE_FIRST_FAIL, "lp_find_file()-> ret: %d = FS_FindFirst()")
    TRC_MSG(OSSL_LP_FIND_FILE_NEXT_FAIL, "lp_find_file()-> FS_FindNext failed")
    TRC_MSG(OSSL_LP_FIND_FILE_END_CLOSE_FAIL, "lp_find_file_end()-> ret: %d = FS_FindClose()")

    /* ossl_fs.c */
    TRC_MSG(OSSL_ADP_STAT, "ossl_adp_stat()-> ret: %d = FS_GetAttributes()")
    TRC_MSG(OSSL_ADP_FOPEN, "ossl_adp_fopen(file_pointer: %d, Open_flag: %d)")
    TRC_MSG(OSSL_ADP_FOPEN_FAIL, "ossl_adp_fopen -> ret: %d = FS_Open()")
    TRC_MSG(OSSL_ADP_FGETS, "ossl_adp_fgets(buf: %d, size: %d)")
    TRC_MSG(OSSL_ADP_FGETS_FOUND_LINE, "ossl_adp_fgets() -> found line")
    TRC_MSG(OSSL_ADP_FGETS_ERR, "ossl_adp_fgets(FS_read(ret: %d, read: %d), start from EOF?(%d))")
    TRC_MSG(OSSL_ADP_GETMODE_FLAG, "ossl_adp_getmode(flag: %d)")


    /* ----------------- TRACE_WARNING trace class ------------------- */

    /* ----------------- TRACE_ERROR trace class ------------------- */


    /* ----------------- TRACE_GROUP_1 trace class ------------------- */
    /* ossl_trace.c */
    TRC_MSG(OSSL_ADP_TRACE_CLASS, "ossl_adp_trace_class(trace: %d, lib: %d)")   
    /* ossl_mutex.c */
    TRC_MSG(OSSL_ADP_MUTEX_INIT, "ossl_adp_mutex_init")
    TRC_MSG(OSSL_ADP_LOCKING_CALLBACK, "ossl_adp_locking_callback(mode:%d, mutex_id: %d, file: %s, line: %d)")

    /* ----------------- TRACE_GROUP_2(CHE) trace class ------------------- */
    TRC_MSG(OSSL_ADP_MD4_INIT_FUNC, "ossl_adp_MD4_Init(), &MD4_CTX = %d")
    TRC_MSG(OSSL_ADP_MD4_UPDATE_FUNC, "ossl_adp_MD4_Update(), &MD4_CTX = %d")
    TRC_MSG(OSSL_ADP_MD4_FINAL_FUNC, "ossl_adp_MD4_Final(), &MD4_CTX = %d")
    TRC_MSG(OSSL_ADP_MD4_FUNC, "ossl_adp_MD4(), &output_md = %d")
    TRC_MSG(OSSL_ADP_MD5_INIT_FUNC, "ossl_adp_MD5_Init(), &MD5_CTX = %d")
    TRC_MSG(OSSL_ADP_MD5_UPDATE_FUNC, "ossl_adp_MD5_Update(), &MD5_CTX = %d")
    TRC_MSG(OSSL_ADP_MD5_FINAL_FUNC, "ossl_adp_MD5_Final(), &MD5_CTX = %d")
    TRC_MSG(OSSL_ADP_MD5_FUNC, "ossl_adp_MD5(), &output_md = %d")
    TRC_MSG(OSSL_ADP_SHA1_INIT_FUNC, "ossl_adp_SHA1_Init(), &SHA_CTX = %d")
    TRC_MSG(OSSL_ADP_SHA1_UPDATE_FUNC, "ossl_adp_SHA1_Update(), &SHA_CTX = %d")
    TRC_MSG(OSSL_ADP_SHA1_FINAL_FUNC, "ossl_adp_SHA1_Final(), &SHA_CTX = %d")
    TRC_MSG(OSSL_ADP_SHA1_FUNC, "ossl_adp_SHA1(), &output_md = %d")
    TRC_MSG(OSSL_ADP_RC4_SET_KEY_FUNC_INV_KEY, "ossl_adp_RC4_set_key() - kay data == NULL")
    TRC_MSG(OSSL_ADP_RC4_SET_KEY_FUNC, "ossl_adp_RC4_set_key(), &RC4_KEY = %d")
    TRC_MSG(OSSL_ADP_RC4_FUNC, "ossl_adp_RC4(), &RC4_KEY = %d")
    TRC_MSG(OSSL_ADP_AES_SET_KEY_FUNC_INV_KEY, "ossl_adp_AES_set_key() - kay data == NULL")
    TRC_MSG(OSSL_ADP_AES_SET_KEY_FUNC, "ossl_adp_AES_set_key(), &AES_KEY = %d")
    TRC_MSG(OSSL_ADP_AES_CBC_ENCRYPT_FUNC, "ossl_adp_AES_cbc_encrypt(), &AES_KEY = %d")
    TRC_MSG(OSSL_ADP_AES_CTX_CLEANUP_FUNC, "ossl_adp_AES_ctx_cleanup(), &AES_KEY = %d")
    TRC_MSG(OSSL_ADP_DES_SET_KEY_FUNC_INV_KEY, "ossl_adp_DES_set_key() - kay data == NULL")
    TRC_MSG(OSSL_ADP_DES_SET_KEY_FUNC, "ossl_adp_DES_set_key(), &DES_KEY = %d")
    TRC_MSG(OSSL_ADP_DES_CBC_ENCRYPT_FUNC, "ossl_adp_DES_cbc_encrypt(), &DES_KEY = %d")
    TRC_MSG(OSSL_ADP_DES_CTX_CLEANUP_FUNC, "ossl_adp_DES_ctx_cleanup(), &DES_KEY = %d")
    TRC_MSG(OSSL_ADP_3DES_SET_KEY_FUNC_INV_KEY, "ossl_adp_3DES_set_key() - kay data == NULL")
    TRC_MSG(OSSL_ADP_3DES_SET_KEY_FUNC, "ossl_adp_3DES_set_key(), &3DES_KEY = %d, key_num=%d")
    TRC_MSG(OSSL_ADP_3DES_CBC_ENCRYPT_FUNC, "ossl_adp_3DES_cbc_encrypt(), &3DES_KEY = %d")
    TRC_MSG(OSSL_ADP_3DES_CTX_CLEANUP_FUNC, "ossl_adp_3DES_ctx_cleanup(), &3DES_KEY = %d")

END_TRACE_MAP(MOD_OSSL_OSADP)

#endif /* __AGPS_SWIP_REL__ */

#endif /* !_OSSL_ADP_TRC_H_ */


