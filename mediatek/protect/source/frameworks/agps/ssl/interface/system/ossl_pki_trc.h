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
 *   pki_trc.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This is trace map definition
 *
 * Author:
 * -------
 *   Wyatt Sun
 *
 *============================================================================
 *                                 HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *----------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Mar 17 2009 mtk01476
 * [MAUI_01646633] [Port OpenSSL]New feature(Certman+PKI) check-in
 * add to source control recursely
 *
 *
 *
 *----------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/


#ifndef _PKI_TRC_H_
#define _PKI_TRC_H_


#ifndef GEN_FOR_PC

#ifndef _STACK_CONFIG_H
#error "stack_config.h should be included before tst_config.h"
#endif /* _STACK_CONFIG_H */

#else /* GEN_FOR_PC */
#include "kal_trace.h"
#endif /* GEN_FOR_PC */


#ifndef _KAL_TRACE_H
#error "kal_trace.h should be included before tst_trace.h"
#endif /* _KAL_TRACE_H */

#if __AGPS_SWIP_REL__
    #define PKI_TRC_0B093007 "func: x509_f_x509_crl_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define PKI_TRC_0B07A007 "func: x509_f_x509_req_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define PKI_TRC_0B079007 "func: x509_f_x509_req_print_ex, reason:err_r_buf_lib, file:%s, line:%d"
    #define PKI_TRC_0B076007 "func: x509_f_x509_print_ex_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define PKI_TRC_0B075007 "func: x509_f_x509_name_print, reason:err_r_buf_lib, file:%s, line:%d"
    #define PKI_TRC_0B078041 "func: x509_f_x509_pubkey_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B07800D "func: x509_f_x509_pubkey_set, reason:err_r_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_0B078010 "func: x509_f_x509_pubkey_set, reason:err_r_ec_lib, file:%s, line:%d"
    #define PKI_TRC_0B07806F "func: x509_f_x509_pubkey_set, reason:x509_r_unsupported_algorithm, file:%s, line:%d"
    #define PKI_TRC_0B077041 "func: x509_f_x509_pubkey_get, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B077010 "func: x509_f_x509_pubkey_get, reason:err_r_ec_lib, file:%s, line:%d"
    #define PKI_TRC_0B077066 "func: x509_f_x509_pubkey_get, reason:x509_r_err_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_23075041 "func: pkcs12_f_pkcs12_item_pack_safebag, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23070041 "func: pkcs12_f_pkcs12_make_keybag, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23071041 "func: pkcs12_f_pkcs12_make_shkeybag, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23072041 "func: pkcs12_f_pkcs12_pack_p7data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23072064 "func: pkcs12_f_pkcs12_pack_p7data, reason:pkcs12_r_cant_pack_structure, file:%s, line:%d"
    #define PKI_TRC_23083079 "func: pkcs12_f_pkcs12_unpack_p7data, reason:pkcs12_r_content_type_not_data, file:%s, line:%d"
    #define PKI_TRC_23073041 "func: pkcs12_f_pkcs12_pack_p7encdata, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23073067 "func: pkcs12_f_pkcs12_pack_p7encdata, reason:pkcs12_r_encrypt_error, file:%s, line:%d"
    #define PKI_TRC_23082079 "func: pkcs12_f_pkcs12_unpack_authsafes, reason:pkcs12_r_content_type_not_data, file:%s, line:%d"
    #define PKI_TRC_23078065 "func: pkcs12_f_pkcs12_pbe_keyivgen, reason:pkcs12_r_decode_error, file:%s, line:%d"
    #define PKI_TRC_2307806B "func: pkcs12_f_pkcs12_pbe_keyivgen, reason:pkcs12_r_key_gen_error, file:%s, line:%d"
    #define PKI_TRC_2307806A "func: pkcs12_f_pkcs12_pbe_keyivgen, reason:pkcs12_r_iv_gen_error, file:%s, line:%d"
    #define PKI_TRC_23069068 "func: pkcs12_f_pkcs12_create, reason:pkcs12_r_invalid_null_argument, file:%s, line:%d"
    #define PKI_TRC_23077073 "func: pkcs12_f_pkcs12_pbe_crypt, reason:pkcs12_r_pkcs12_algor_cipherinit_error, file:%s, line:%d"
    #define PKI_TRC_23077041 "func: pkcs12_f_pkcs12_pbe_crypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23077074 "func: pkcs12_f_pkcs12_pbe_crypt, reason:pkcs12_r_pkcs12_cipherfinal_error, file:%s, line:%d"
    #define PKI_TRC_2306A075 "func: pkcs12_f_pkcs12_item_decrypt_d2i, reason:pkcs12_r_pkcs12_pbe_crypt_error, file:%s, line:%d"
    #define PKI_TRC_2306A065 "func: pkcs12_f_pkcs12_item_decrypt_d2i, reason:pkcs12_r_decode_error, file:%s, line:%d"
    #define PKI_TRC_2306C041 "func: pkcs12_f_pkcs12_item_i2d_encrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2306C066 "func: pkcs12_f_pkcs12_item_i2d_encrypt, reason:pkcs12_r_encode_error, file:%s, line:%d"
    #define PKI_TRC_2306C067 "func: pkcs12_f_pkcs12_item_i2d_encrypt, reason:pkcs12_r_encrypt_error, file:%s, line:%d"
    #define PKI_TRC_2306D041 "func: pkcs12_f_pkcs12_init, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2306E041 "func: pkcs12_f_pkcs12_key_gen_asc, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2306F043 "func: pkcs12_f_pkcs12_key_gen_uni, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2306F041 "func: pkcs12_f_pkcs12_key_gen_uni, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23076069 "func: pkcs12_f_pkcs12_parse, reason:pkcs12_r_invalid_null_pkcs12_pointer, file:%s, line:%d"
    #define PKI_TRC_23076041 "func: pkcs12_f_pkcs12_parse, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23076071 "func: pkcs12_f_pkcs12_parse, reason:pkcs12_r_mac_verify_failure, file:%s, line:%d"
    #define PKI_TRC_23076072 "func: pkcs12_f_pkcs12_parse, reason:pkcs12_r_parse_error, file:%s, line:%d"
    #define PKI_TRC_23081041 "func: pkcs12_f_parse_bag, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2306B079 "func: pkcs12_f_pkcs12_gen_mac, reason:pkcs12_r_content_type_not_data, file:%s, line:%d"
    #define PKI_TRC_2306B076 "func: pkcs12_f_pkcs12_gen_mac, reason:pkcs12_r_unknown_digest_algorithm, file:%s, line:%d"
    #define PKI_TRC_2306B06B "func: pkcs12_f_pkcs12_gen_mac, reason:pkcs12_r_key_gen_error, file:%s, line:%d"
    #define PKI_TRC_2307E06C "func: pkcs12_f_pkcs12_verify_mac, reason:pkcs12_r_mac_absent, file:%s, line:%d"
    #define PKI_TRC_2307E06D "func: pkcs12_f_pkcs12_verify_mac, reason:pkcs12_r_mac_generation_error, file:%s, line:%d"
    #define PKI_TRC_2307B06E "func: pkcs12_f_pkcs12_set_mac, reason:pkcs12_r_mac_setup_error, file:%s, line:%d"
    #define PKI_TRC_2307B06D "func: pkcs12_f_pkcs12_set_mac, reason:pkcs12_r_mac_generation_error, file:%s, line:%d"
    #define PKI_TRC_2307B06F "func: pkcs12_f_pkcs12_set_mac, reason:pkcs12_r_mac_string_set_error, file:%s, line:%d"
    #define PKI_TRC_2307A041 "func: pkcs12_f_pkcs12_setup_mac, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_23080069 "func: pkcs12_f_pkcs12_newpass, reason:pkcs12_r_invalid_null_pkcs12_pointer, file:%s, line:%d"
    #define PKI_TRC_23080071 "func: pkcs12_f_pkcs12_newpass, reason:pkcs12_r_mac_verify_failure, file:%s, line:%d"
    #define PKI_TRC_23080072 "func: pkcs12_f_pkcs12_newpass, reason:pkcs12_r_parse_error, file:%s, line:%d"
    #define PKI_TRC_2307D041 "func: pkcs12_f_pkcs8_encrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2307D00D "func: pkcs12_f_pkcs8_encrypt, reason:err_r_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_2307D067 "func: pkcs12_f_pkcs8_encrypt, reason:pkcs12_r_encrypt_error, file:%s, line:%d"
    #define PKI_TRC_21076041 "func: pkcs7_f_pkcs7_add_attrib_smimecap, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21077041 "func: pkcs7_f_pkcs7_simple_smimecap, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2107D020 "func: pkcs7_f_pkcs7_bio_add_digest, reason:err_r_bio_lib, file:%s, line:%d"
    #define PKI_TRC_2107D06D "func: pkcs7_f_pkcs7_bio_add_digest, reason:pkcs7_r_unknown_digest_type, file:%s, line:%d"
    #define PKI_TRC_21069070 "func: pkcs7_f_pkcs7_datainit, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d"
    #define PKI_TRC_21069020 "func: pkcs7_f_pkcs7_datainit, reason:err_r_bio_lib, file:%s, line:%d"
    #define PKI_TRC_21069067 "func: pkcs7_f_pkcs7_datainit, reason:pkcs7_r_missing_ceripend_info, file:%s, line:%d"
    #define PKI_TRC_21069041 "func: pkcs7_f_pkcs7_datainit, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21069006 "func: pkcs7_f_pkcs7_datainit, reason:err_r_evp_lib, file:%s, line:%d"
    #define PKI_TRC_2107006F "func: pkcs7_f_pkcs7_datadecode, reason:pkcs7_r_unsupported_cipher_type, file:%s, line:%d"
    #define PKI_TRC_21070070 "func: pkcs7_f_pkcs7_datadecode, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d"
    #define PKI_TRC_21070020 "func: pkcs7_f_pkcs7_datadecode, reason:err_r_bio_lib, file:%s, line:%d"
    #define PKI_TRC_2107006D "func: pkcs7_f_pkcs7_datadecode, reason:pkcs7_r_unknown_digest_type, file:%s, line:%d"
    #define PKI_TRC_21070041 "func: pkcs7_f_pkcs7_datadecode, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2107F06C "func: pkcs7_f_pkcs7_find_digest, reason:pkcs7_r_unable_to_find_message_digest, file:%s, line:%d"
    #define PKI_TRC_2107F044 "func: pkcs7_f_pkcs7_find_digest, reason:err_r_internal_error, file:%s, line:%d"
    #define PKI_TRC_21080041 "func: pkcs7_f_pkcs7_datafinal, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21080020 "func: pkcs7_f_pkcs7_datafinal, reason:err_r_bio_lib, file:%s, line:%d"
    #define PKI_TRC_21080006 "func: pkcs7_f_pkcs7_datafinal, reason:err_r_evp_lib, file:%s, line:%d"
    #define PKI_TRC_2108000D "func: pkcs7_f_pkcs7_datafinal, reason:err_r_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_2108006B "func: pkcs7_f_pkcs7_datafinal, reason:pkcs7_r_unable_to_find_mem_bio, file:%s, line:%d"
    #define PKI_TRC_2106B072 "func: pkcs7_f_pkcs7_dataverify, reason:pkcs7_r_wrong_pkcs7_type, file:%s, line:%d"
    #define PKI_TRC_2106B06A "func: pkcs7_f_pkcs7_dataverify, reason:pkcs7_r_unable_to_find_certificate, file:%s, line:%d"
    #define PKI_TRC_2106B00B "func: pkcs7_f_pkcs7_dataverify, reason:err_r_x509_lib, file:%s, line:%d"
    #define PKI_TRC_21068068 "func: pkcs7_f_pkcs7_ctrl, reason:pkcs7_r_operation_not_supported_on_this_type, file:%s, line:%d"
    #define PKI_TRC_2106806E "func: pkcs7_f_pkcs7_ctrl, reason:pkcs7_r_unknown_operation, file:%s, line:%d"
    #define PKI_TRC_2106D070 "func: pkcs7_f_pkcs7_set_content, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d"
    #define PKI_TRC_2106E070 "func: pkcs7_f_pkcs7_set_type, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d"
    #define PKI_TRC_21067071 "func: pkcs7_f_pkcs7_add_signer, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_21067041 "func: pkcs7_f_pkcs7_add_signer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21064071 "func: pkcs7_f_pkcs7_add_certificate, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_21064041 "func: pkcs7_f_pkcs7_add_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21065071 "func: pkcs7_f_pkcs7_add_crl, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_21065041 "func: pkcs7_f_pkcs7_add_crl, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2107E041 "func: pkcs7_f_pkcs7_set_digest, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2107E071 "func: pkcs7_f_pkcs7_set_digest, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_21066071 "func: pkcs7_f_pkcs7_add_recipient_info, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_2106C071 "func: pkcs7_f_pkcs7_set_cipher, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_2106C090 "func: pkcs7_f_pkcs7_set_cipher, reason:pkcs7_r_cipher_has_no_object_identifier, file:%s, line:%d"
    #define PKI_TRC_21079041 "func: pkcs7_f_b64_write_pkcs7, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21078041 "func: pkcs7_f_b64_read_pkcs7, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21078082 "func: pkcs7_f_b64_read_pkcs7, reason:pkcs7_r_decode_error, file:%s, line:%d"
    #define PKI_TRC_2107A085 "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_mime_parse_error, file:%s, line:%d"
    #define PKI_TRC_2107A087 "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_content_type, file:%s, line:%d"
    #define PKI_TRC_2107A089 "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_multipart_boundary, file:%s, line:%d"
    #define PKI_TRC_2107A088 "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_multipart_body_failure, file:%s, line:%d"
    #define PKI_TRC_2107A086 "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_mime_sig_parse_error, file:%s, line:%d"
    #define PKI_TRC_2107A08A "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_sig_content_type, file:%s, line:%d"
    #define PKI_TRC_2107A08D "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_sig_invalid_mime_type, file:%s, line:%d"
    #define PKI_TRC_2107A08C "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_pkcs7_sig_parse_error, file:%s, line:%d"
    #define PKI_TRC_2107A083 "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_invalid_mime_type, file:%s, line:%d"
    #define PKI_TRC_2107A08B "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_pkcs7_parse_error, file:%s, line:%d"
    #define PKI_TRC_2107B085 "func: pkcs7_f_smime_text, reason:pkcs7_r_mime_parse_error, file:%s, line:%d"
    #define PKI_TRC_2107B084 "func: pkcs7_f_smime_text, reason:pkcs7_r_mime_no_content_type, file:%s, line:%d"
    #define PKI_TRC_2107B083 "func: pkcs7_f_smime_text, reason:pkcs7_r_invalid_mime_type, file:%s, line:%d"
    #define PKI_TRC_2107407F "func: pkcs7_f_pkcs7_sign, reason:pkcs7_r_private_key_does_not_match_certificate, file:%s, line:%d"
    #define PKI_TRC_21074041 "func: pkcs7_f_pkcs7_sign, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2107407C "func: pkcs7_f_pkcs7_sign, reason:pkcs7_r_pkcs7_add_signature_error, file:%s, line:%d"
    #define PKI_TRC_21074091 "func: pkcs7_f_pkcs7_sign, reason:pkcs7_r_pkcs7_datasign, file:%s, line:%d"
    #define PKI_TRC_2107508F "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_invalid_null_pointer, file:%s, line:%d"
    #define PKI_TRC_21075071 "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_2107507A "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_no_content, file:%s, line:%d"
    #define PKI_TRC_21075076 "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_content_and_data_present, file:%s, line:%d"
    #define PKI_TRC_2107507B "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_no_signatures_on_data, file:%s, line:%d"
    #define PKI_TRC_2107500B "func: pkcs7_f_pkcs7_verify, reason:err_r_x509_lib, file:%s, line:%d"
    #define PKI_TRC_21075075 "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_certificate_verify_error, file:%s, line:%d"
    #define PKI_TRC_21075041 "func: pkcs7_f_pkcs7_verify, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21075081 "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_smime_text_error, file:%s, line:%d"
    #define PKI_TRC_21075069 "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_signature_failure, file:%s, line:%d"
    #define PKI_TRC_2107C08F "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_invalid_null_pointer, file:%s, line:%d"
    #define PKI_TRC_2107C071 "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_2107C08E "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_no_signers, file:%s, line:%d"
    #define PKI_TRC_2107C041 "func: pkcs7_f_pkcs7_get0_signers, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2107C080 "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_signer_certificate_not_found, file:%s, line:%d"
    #define PKI_TRC_21073041 "func: pkcs7_f_pkcs7_encrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_21073079 "func: pkcs7_f_pkcs7_encrypt, reason:pkcs7_r_error_setting_cipher, file:%s, line:%d"
    #define PKI_TRC_2107307D "func: pkcs7_f_pkcs7_encrypt, reason:pkcs7_r_pkcs7_datafinal_error, file:%s, line:%d"
    #define PKI_TRC_2107208F "func: pkcs7_f_pkcs7_decrypt, reason:pkcs7_r_invalid_null_pointer, file:%s, line:%d"
    #define PKI_TRC_21072071 "func: pkcs7_f_pkcs7_decrypt, reason:pkcs7_r_wrong_content_type, file:%s, line:%d"
    #define PKI_TRC_21072077 "func: pkcs7_f_pkcs7_decrypt, reason:pkcs7_r_decrypt_error, file:%s, line:%d"
    #define PKI_TRC_21072041 "func: pkcs7_f_pkcs7_decrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2C084043 "func: store_f_store_new_method, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2C084041 "func: store_f_store_new_method, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2C085026 "func: store_f_store_new_engine, reason:err_r_engine_lib, file:%s, line:%d"
    #define PKI_TRC_2C085043 "func: store_f_store_new_engine, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2C08502C "func: store_f_store_new_engine, reason:err_r_store_lib, file:%s, line:%d"
    #define PKI_TRC_2C0A1043 "func: store_f_store_ctrl, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2C0A1090 "func: store_f_store_ctrl, reason:store_r_no_control_function, file:%s, line:%d"
    #define PKI_TRC_2C09307F "func: store_f_store_attr_info_set_cstr, reason:store_r_already_has_a_value, file:%s, line:%d"
    #define PKI_TRC_2C09607F "func: store_f_store_attr_info_set_sha1str, reason:store_r_already_has_a_value, file:%s, line:%d"
    #define PKI_TRC_2C09407F "func: store_f_store_attr_info_set_dn, reason:store_r_already_has_a_value, file:%s, line:%d"
    #define PKI_TRC_2C09507F "func: store_f_store_attr_info_set_number, reason:store_r_already_has_a_value, file:%s, line:%d"
    #define PKI_TRC_2C0AB043 "func: store_f_store_parse_attrs_start, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2C098043 "func: store_f_store_parse_attrs_next, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2C097043 "func: store_f_store_parse_attrs_end, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_2C0AC043 "func: store_f_store_parse_attrs_endp, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_0B066067 "func: x509_f_dir_ctrl, reason:x509_r_loading_cert_dir, file:%s, line:%d"
    #define PKI_TRC_0B064071 "func: x509_f_add_cert_dir, reason:x509_r_invalid_directory, file:%s, line:%d"
    #define PKI_TRC_0B064041 "func: x509_f_add_cert_dir, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B067070 "func: x509_f_get_cert_by_subject, reason:x509_r_wrong_lookup_type, file:%s, line:%d"
    #define PKI_TRC_0B067007 "func: x509_f_get_cert_by_subject, reason:err_r_buf_lib, file:%s, line:%d"
    #define PKI_TRC_0B067041 "func: x509_f_get_cert_by_subject, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B065068 "func: x509_f_by_file_ctrl, reason:x509_r_loading_defaults, file:%s, line:%d"
    #define PKI_TRC_0B06F002 "func: x509_f_x509_load_cert_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define PKI_TRC_0B06F00D "func: x509_f_x509_load_cert_file, reason:err_r_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_0B06F064 "func: x509_f_x509_load_cert_file, reason:x509_r_bad_x509_filetype, file:%s, line:%d"
    #define PKI_TRC_0B070002 "func: x509_f_x509_load_crl_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define PKI_TRC_0B07000D "func: x509_f_x509_load_crl_file, reason:err_r_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_0B070064 "func: x509_f_x509_load_crl_file, reason:x509_r_bad_x509_filetype, file:%s, line:%d"
    #define PKI_TRC_0B084002 "func: x509_f_x509_load_cert_crl_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define PKI_TRC_0B084009 "func: x509_f_x509_load_cert_crl_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define PKI_TRC_0B071041 "func: x509_f_x509_name_add_entry, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B07206D "func: x509_f_x509_name_entry_create_by_nid, reason:x509_r_unknown_nid, file:%s, line:%d"
    #define PKI_TRC_0B073043 "func: x509_f_x509_name_entry_set_object, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_0B081041 "func: x509_f_netscape_spki_b64_decode, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B082041 "func: x509_f_netscape_spki_b64_encode, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B087043 "func: x509_f_x509at_add1_attr, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_0B087041 "func: x509_f_x509at_add1_attr, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B08806D "func: x509_f_x509_attribute_create_by_nid, reason:x509_r_unknown_nid, file:%s, line:%d"
    #define PKI_TRC_0B089041 "func: x509_f_x509_attribute_create_by_obj, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B08A00D "func: x509_f_x509_attribute_set1_data, reason:err_r_asn1_lib, file:%s, line:%d"
    #define PKI_TRC_0B08A041 "func: x509_f_x509_attribute_set1_data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B08B07A "func: x509_f_x509_attribute_get0_data, reason:x509_r_wrong_type, file:%s, line:%d"
    #define PKI_TRC_0B080074 "func: x509_f_x509_check_private_key, reason:x509_r_key_values_mismatch, file:%s, line:%d"
    #define PKI_TRC_0B080073 "func: x509_f_x509_check_private_key, reason:x509_r_key_type_mismatch, file:%s, line:%d"
    #define PKI_TRC_0B080010 "func: x509_f_x509_check_private_key, reason:err_r_ec_lib, file:%s, line:%d"
    #define PKI_TRC_0B080072 "func: x509_f_x509_check_private_key, reason:x509_r_cant_check_dh_key, file:%s, line:%d"
    #define PKI_TRC_0B080075 "func: x509_f_x509_check_private_key, reason:x509_r_unknown_key_type, file:%s, line:%d"
    #define PKI_TRC_0B07C041 "func: x509_f_x509_store_add_cert, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B07C065 "func: x509_f_x509_store_add_cert, reason:x509_r_cert_already_in_hash_table, file:%s, line:%d"
    #define PKI_TRC_0B07D041 "func: x509_f_x509_store_add_crl, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B07D065 "func: x509_f_x509_store_add_crl, reason:x509_r_cert_already_in_hash_table, file:%s, line:%d"
    #define PKI_TRC_0B09206A "func: x509_f_x509_store_ctx_get1_issuer, reason:x509_r_should_retry, file:%s, line:%d"
    #define PKI_TRC_0B074041 "func: x509_f_x509_name_oneline, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B08D07B "func: x509_f_x509_trust_set, reason:x509_r_invalid_trust, file:%s, line:%d"
    #define PKI_TRC_0B085041 "func: x509_f_x509_trust_add, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B068043 "func: x509_f_x509v3_add_ext, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define PKI_TRC_0B068041 "func: x509_f_x509v3_add_ext, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B06C06D "func: x509_f_x509_extension_create_by_nid, reason:x509_r_unknown_nid, file:%s, line:%d"
    #define PKI_TRC_0B06D041 "func: x509_f_x509_extension_create_by_obj, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B07F069 "func: x509_f_x509_verify_cert, reason:x509_r_no_cert_set_for_us_to_verify, file:%s, line:%d"
    #define PKI_TRC_0B07F041 "func: x509_f_x509_verify_cert, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B091041 "func: x509_f_check_policy, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B06E06C "func: x509_f_x509_get_pubkey_parameters, reason:x509_r_unable_to_get_certs_public_key, file:%s, line:%d"
    #define PKI_TRC_0B06E06B "func: x509_f_x509_get_pubkey_parameters, reason:x509_r_unable_to_find_parameters_in_chain, file:%s, line:%d"
    #define PKI_TRC_0B08E041 "func: x509_f_x509_store_ctx_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_0B08F041 "func: x509_f_x509_store_ctx_init, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209F041 "func: x509v3_f_v2i_ipaddrblocks, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209F073 "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_extension_name_error, file:%s, line:%d"
    #define PKI_TRC_2209F0A4 "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_invalid_safi, file:%s, line:%d"
    #define PKI_TRC_2209F0A2 "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_invalid_inheritance, file:%s, line:%d"
    #define PKI_TRC_2209F0A3 "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_invalid_ipaddress, file:%s, line:%d"
    #define PKI_TRC_2209F074 "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_extension_value_error, file:%s, line:%d"
    #define PKI_TRC_220A0041 "func: x509v3_f_v3_addr_validate_path_internal, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22077078 "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_unknown_option, file:%s, line:%d"
    #define PKI_TRC_22077079 "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_no_issuer_certificate, file:%s, line:%d"
    #define PKI_TRC_2207707B "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_unable_to_get_issuer_keyid, file:%s, line:%d"
    #define PKI_TRC_2207707A "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_unable_to_get_issuer_details, file:%s, line:%d"
    #define PKI_TRC_22077041 "func: x509v3_f_v2i_authority_keyid, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22099041 "func: x509v3_f_v2i_issuer_alt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2207B07F "func: x509v3_f_copy_issuer, reason:x509v3_r_no_issuer_details, file:%s, line:%d"
    #define PKI_TRC_2207B07E "func: x509v3_f_copy_issuer, reason:x509v3_r_issuer_decode_error, file:%s, line:%d"
    #define PKI_TRC_2207B041 "func: x509v3_f_copy_issuer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209A041 "func: x509v3_f_v2i_subject_alt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2207A07D "func: x509v3_f_copy_email, reason:x509v3_r_no_subject_details, file:%s, line:%d"
    #define PKI_TRC_2207A041 "func: x509v3_f_copy_email, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22076041 "func: x509v3_f_v2i_general_names, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2207507C "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_missing_value, file:%s, line:%d"
    #define PKI_TRC_22075041 "func: x509v3_f_v2i_general_name_ex, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22075077 "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_bad_object, file:%s, line:%d"
    #define PKI_TRC_22075076 "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_bad_ip_address, file:%s, line:%d"
    #define PKI_TRC_22075095 "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_dirname_error, file:%s, line:%d"
    #define PKI_TRC_22075093 "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_othername_error, file:%s, line:%d"
    #define PKI_TRC_22075075 "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_unsupported_option, file:%s, line:%d"
    #define PKI_TRC_22090096 "func: x509v3_f_do_dirname, reason:x509v3_r_section_not_found, file:%s, line:%d"
    #define PKI_TRC_2209C041 "func: x509v3_f_asidentifierchoice_canonize, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209E041 "func: x509v3_f_v2i_asidentifiers, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209E073 "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_extension_name_error, file:%s, line:%d"
    #define PKI_TRC_2209E0A2 "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_invalid_inheritance, file:%s, line:%d"
    #define PKI_TRC_2209E0A0 "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_invalid_asnumber, file:%s, line:%d"
    #define PKI_TRC_2209E0A1 "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_invalid_asrange, file:%s, line:%d"
    #define PKI_TRC_22066041 "func: x509v3_f_v2i_basic_constraints, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2206606A "func: x509v3_f_v2i_basic_constraints, reason:x509v3_r_invalid_name, file:%s, line:%d"
    #define PKI_TRC_22065041 "func: x509v3_f_v2i_asn1_bit_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22098080 "func: x509v3_f_x509v3_ext_nconf, reason:x509v3_r_error_in_extension, file:%s, line:%d"
    #define PKI_TRC_22097082 "func: x509v3_f_do_ext_nconf, reason:x509v3_r_unknown_extension_name, file:%s, line:%d"
    #define PKI_TRC_22097081 "func: x509v3_f_do_ext_nconf, reason:x509v3_r_unknown_extension, file:%s, line:%d"
    #define PKI_TRC_22097069 "func: x509v3_f_do_ext_nconf, reason:x509v3_r_invalid_extension_string, file:%s, line:%d"
    #define PKI_TRC_22097088 "func: x509v3_f_do_ext_nconf, reason:x509v3_r_no_config_database, file:%s, line:%d"
    #define PKI_TRC_22097067 "func: x509v3_f_do_ext_nconf, reason:x509v3_r_extension_setting_not_supported, file:%s, line:%d"
    #define PKI_TRC_22087041 "func: x509v3_f_do_ext_i2d, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22088081 "func: x509v3_f_x509v3_ext_i2d, reason:x509v3_r_unknown_extension, file:%s, line:%d"
    #define PKI_TRC_22074073 "func: x509v3_f_v3_generic_extension, reason:x509v3_r_extension_name_error, file:%s, line:%d"
    #define PKI_TRC_22074074 "func: x509v3_f_v3_generic_extension, reason:x509v3_r_extension_value_error, file:%s, line:%d"
    #define PKI_TRC_22074041 "func: x509v3_f_v3_generic_extension, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2208F094 "func: x509v3_f_x509v3_get_string, reason:x509v3_r_operation_not_defined, file:%s, line:%d"
    #define PKI_TRC_2208E094 "func: x509v3_f_x509v3_get_section, reason:x509v3_r_operation_not_defined, file:%s, line:%d"
    #define PKI_TRC_22082041 "func: x509v3_f_r2i_certpol, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22082022 "func: x509v3_f_r2i_certpol, reason:err_r_x509v3_lib, file:%s, line:%d"
    #define PKI_TRC_22082086 "func: x509v3_f_r2i_certpol, reason:x509v3_r_invalid_policy_identifier, file:%s, line:%d"
    #define PKI_TRC_22082087 "func: x509v3_f_r2i_certpol, reason:x509v3_r_invalid_section, file:%s, line:%d"
    #define PKI_TRC_2208206E "func: x509v3_f_r2i_certpol, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d"
    #define PKI_TRC_2208306E "func: x509v3_f_policy_section, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d"
    #define PKI_TRC_22083089 "func: x509v3_f_policy_section, reason:x509v3_r_expected_a_section_name, file:%s, line:%d"
    #define PKI_TRC_22083087 "func: x509v3_f_policy_section, reason:x509v3_r_invalid_section, file:%s, line:%d"
    #define PKI_TRC_2208308A "func: x509v3_f_policy_section, reason:x509v3_r_invalid_option, file:%s, line:%d"
    #define PKI_TRC_2208308B "func: x509v3_f_policy_section, reason:x509v3_r_no_policy_identifier, file:%s, line:%d"
    #define PKI_TRC_22083041 "func: x509v3_f_policy_section, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2208408D "func: x509v3_f_notice_section, reason:x509v3_r_invalid_numbers, file:%s, line:%d"
    #define PKI_TRC_2208408A "func: x509v3_f_notice_section, reason:x509v3_r_invalid_option, file:%s, line:%d"
    #define PKI_TRC_2208408E "func: x509v3_f_notice_section, reason:x509v3_r_need_organization_and_numbers, file:%s, line:%d"
    #define PKI_TRC_22084041 "func: x509v3_f_notice_section, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2208508C "func: x509v3_f_nref_nos, reason:x509v3_r_invalid_number, file:%s, line:%d"
    #define PKI_TRC_22085041 "func: x509v3_f_nref_nos, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22086041 "func: x509v3_f_v2i_crld, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22067041 "func: x509v3_f_v2i_extended_key_usage, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2206706E "func: x509v3_f_v2i_extended_key_usage, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d"
    #define PKI_TRC_22095041 "func: x509v3_f_i2s_asn1_ia5string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2206406B "func: x509v3_f_s2i_asn1_ia5string, reason:x509v3_r_invalid_null_argument, file:%s, line:%d"
    #define PKI_TRC_22064041 "func: x509v3_f_s2i_asn1_ia5string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2208B041 "func: x509v3_f_v2i_authority_info_access, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2208B08F "func: x509v3_f_v2i_authority_info_access, reason:x509v3_r_invalid_syntax, file:%s, line:%d"
    #define PKI_TRC_2208B077 "func: x509v3_f_v2i_authority_info_access, reason:x509v3_r_bad_object, file:%s, line:%d"
    #define PKI_TRC_22068041 "func: x509v3_f_x509v3_ext_add, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2206A066 "func: x509v3_f_x509v3_ext_add_alias, reason:x509v3_r_extension_not_found, file:%s, line:%d"
    #define PKI_TRC_2206A041 "func: x509v3_f_x509v3_ext_add_alias, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2208C090 "func: x509v3_f_x509v3_add1_i2d, reason:x509v3_r_error_creating_extension, file:%s, line:%d"
    #define PKI_TRC_2208C000 "func: x509v3_f_x509v3_add1_i2d, reason:err_r_reason_none, file:%s, line:%d"
    #define PKI_TRC_2209308F "func: x509v3_f_v2i_name_constraints, reason:x509v3_r_invalid_syntax, file:%s, line:%d"
    #define PKI_TRC_22093041 "func: x509v3_f_v2i_name_constraints, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209609B "func: x509v3_f_process_pci_value, reason:x509v3_r_policy_language_alreadty_defined, file:%s, line:%d"
    #define PKI_TRC_2209606E "func: x509v3_f_process_pci_value, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d"
    #define PKI_TRC_2209609D "func: x509v3_f_process_pci_value, reason:x509v3_r_policy_path_length_alreadty_defined, file:%s, line:%d"
    #define PKI_TRC_2209609C "func: x509v3_f_process_pci_value, reason:x509v3_r_policy_path_length, file:%s, line:%d"
    #define PKI_TRC_22096041 "func: x509v3_f_process_pci_value, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22096020 "func: x509v3_f_process_pci_value, reason:err_r_bio_lib, file:%s, line:%d"
    #define PKI_TRC_22096098 "func: x509v3_f_process_pci_value, reason:x509v3_r_incorrect_policy_syntax_tag, file:%s, line:%d"
    #define PKI_TRC_2209B099 "func: x509v3_f_r2i_pci, reason:x509v3_r_invalid_proxy_policy_setting, file:%s, line:%d"
    #define PKI_TRC_2209B087 "func: x509v3_f_r2i_pci, reason:x509v3_r_invalid_section, file:%s, line:%d"
    #define PKI_TRC_2209B09A "func: x509v3_f_r2i_pci, reason:x509v3_r_no_proxy_cert_policy_language_defined, file:%s, line:%d"
    #define PKI_TRC_2209B09F "func: x509v3_f_r2i_pci, reason:x509v3_r_policy_when_proxy_language_requires_no_policy, file:%s, line:%d"
    #define PKI_TRC_2209B041 "func: x509v3_f_r2i_pci, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22092041 "func: x509v3_f_v2i_policy_constraints, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209206A "func: x509v3_f_v2i_policy_constraints, reason:x509v3_r_invalid_name, file:%s, line:%d"
    #define PKI_TRC_22092097 "func: x509v3_f_v2i_policy_constraints, reason:x509v3_r_illegal_empty_extension, file:%s, line:%d"
    #define PKI_TRC_22091041 "func: x509v3_f_v2i_policy_mappings, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2209106E "func: x509v3_f_v2i_policy_mappings, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d"
    #define PKI_TRC_2208D092 "func: x509v3_f_x509_purpose_set, reason:x509v3_r_invalid_purpose, file:%s, line:%d"
    #define PKI_TRC_22089041 "func: x509v3_f_x509_purpose_add, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22070041 "func: x509v3_f_s2i_asn1_octet_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22073041 "func: x509v3_f_s2i_skey_id, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22073072 "func: x509v3_f_s2i_skey_id, reason:x509v3_r_no_public_key, file:%s, line:%d"
    #define PKI_TRC_2207D083 "func: x509v3_f_sxnet_add_id_asc, reason:x509v3_r_error_converting_zone, file:%s, line:%d"
    #define PKI_TRC_2207F041 "func: x509v3_f_sxnet_add_id_ulong, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2207E06B "func: x509v3_f_sxnet_add_id_integer, reason:x509v3_r_invalid_null_argument, file:%s, line:%d"
    #define PKI_TRC_2207E084 "func: x509v3_f_sxnet_add_id_integer, reason:x509v3_r_user_too_long, file:%s, line:%d"
    #define PKI_TRC_2207E085 "func: x509v3_f_sxnet_add_id_integer, reason:x509v3_r_duplicate_zone_id, file:%s, line:%d"
    #define PKI_TRC_2207E041 "func: x509v3_f_sxnet_add_id_integer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22080083 "func: x509v3_f_sxnet_get_id_asc, reason:x509v3_r_error_converting_zone, file:%s, line:%d"
    #define PKI_TRC_22081041 "func: x509v3_f_sxnet_get_id_ulong, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22069041 "func: x509v3_f_x509v3_add_value, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22079041 "func: x509v3_f_i2s_asn1_enumerated, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22078041 "func: x509v3_f_i2s_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2206C06D "func: x509v3_f_s2i_asn1_integer, reason:x509v3_r_invalid_null_value, file:%s, line:%d"
    #define PKI_TRC_2206C064 "func: x509v3_f_s2i_asn1_integer, reason:x509v3_r_bn_dec2bn_error, file:%s, line:%d"
    #define PKI_TRC_2206C065 "func: x509v3_f_s2i_asn1_integer, reason:x509v3_r_bn_to_asn1_integer_error, file:%s, line:%d"
    #define PKI_TRC_2206E068 "func: x509v3_f_x509v3_get_value_bool, reason:x509v3_r_invalid_boolean_string, file:%s, line:%d"
    #define PKI_TRC_2206D06C "func: x509v3_f_x509v3_parse_list, reason:x509v3_r_invalid_null_name, file:%s, line:%d"
    #define PKI_TRC_2206D06D "func: x509v3_f_x509v3_parse_list, reason:x509v3_r_invalid_null_value, file:%s, line:%d"
    #define PKI_TRC_2206F041 "func: x509v3_f_hex_to_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_2207106B "func: x509v3_f_string_to_hex, reason:x509v3_r_invalid_null_argument, file:%s, line:%d"
    #define PKI_TRC_22071070 "func: x509v3_f_string_to_hex, reason:x509v3_r_odd_number_of_digits, file:%s, line:%d"
    #define PKI_TRC_22071041 "func: x509v3_f_string_to_hex, reason:err_r_malloc_failure, file:%s, line:%d"
    #define PKI_TRC_22071071 "func: x509v3_f_string_to_hex, reason:x509v3_r_illegal_hex_digit, file:%s, line:%d"
#else
BEGIN_TRACE_MAP(MOD_OSSL_PKI)

    TRC_MSG(PKI_TRC_0B093007, "func: x509_f_x509_crl_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07A007, "func: x509_f_x509_req_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B079007, "func: x509_f_x509_req_print_ex, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B076007, "func: x509_f_x509_print_ex_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B075007, "func: x509_f_x509_name_print, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B078041, "func: x509_f_x509_pubkey_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07800D, "func: x509_f_x509_pubkey_set, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B078010, "func: x509_f_x509_pubkey_set, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07806F, "func: x509_f_x509_pubkey_set, reason:x509_r_unsupported_algorithm, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B077041, "func: x509_f_x509_pubkey_get, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B077010, "func: x509_f_x509_pubkey_get, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B077066, "func: x509_f_x509_pubkey_get, reason:x509_r_err_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23075041, "func: pkcs12_f_pkcs12_item_pack_safebag, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23070041, "func: pkcs12_f_pkcs12_make_keybag, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23071041, "func: pkcs12_f_pkcs12_make_shkeybag, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23072041, "func: pkcs12_f_pkcs12_pack_p7data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23072064, "func: pkcs12_f_pkcs12_pack_p7data, reason:pkcs12_r_cant_pack_structure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23083079, "func: pkcs12_f_pkcs12_unpack_p7data, reason:pkcs12_r_content_type_not_data, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23073041, "func: pkcs12_f_pkcs12_pack_p7encdata, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23073067, "func: pkcs12_f_pkcs12_pack_p7encdata, reason:pkcs12_r_encrypt_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23082079, "func: pkcs12_f_pkcs12_unpack_authsafes, reason:pkcs12_r_content_type_not_data, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23078065, "func: pkcs12_f_pkcs12_pbe_keyivgen, reason:pkcs12_r_decode_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307806B, "func: pkcs12_f_pkcs12_pbe_keyivgen, reason:pkcs12_r_key_gen_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307806A, "func: pkcs12_f_pkcs12_pbe_keyivgen, reason:pkcs12_r_iv_gen_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23069068, "func: pkcs12_f_pkcs12_create, reason:pkcs12_r_invalid_null_argument, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23077073, "func: pkcs12_f_pkcs12_pbe_crypt, reason:pkcs12_r_pkcs12_algor_cipherinit_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23077041, "func: pkcs12_f_pkcs12_pbe_crypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23077074, "func: pkcs12_f_pkcs12_pbe_crypt, reason:pkcs12_r_pkcs12_cipherfinal_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306A075, "func: pkcs12_f_pkcs12_item_decrypt_d2i, reason:pkcs12_r_pkcs12_pbe_crypt_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306A065, "func: pkcs12_f_pkcs12_item_decrypt_d2i, reason:pkcs12_r_decode_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306C041, "func: pkcs12_f_pkcs12_item_i2d_encrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306C066, "func: pkcs12_f_pkcs12_item_i2d_encrypt, reason:pkcs12_r_encode_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306C067, "func: pkcs12_f_pkcs12_item_i2d_encrypt, reason:pkcs12_r_encrypt_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306D041, "func: pkcs12_f_pkcs12_init, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306E041, "func: pkcs12_f_pkcs12_key_gen_asc, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306F043, "func: pkcs12_f_pkcs12_key_gen_uni, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306F041, "func: pkcs12_f_pkcs12_key_gen_uni, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23076069, "func: pkcs12_f_pkcs12_parse, reason:pkcs12_r_invalid_null_pkcs12_pointer, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23076041, "func: pkcs12_f_pkcs12_parse, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23076071, "func: pkcs12_f_pkcs12_parse, reason:pkcs12_r_mac_verify_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23076072, "func: pkcs12_f_pkcs12_parse, reason:pkcs12_r_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23081041, "func: pkcs12_f_parse_bag, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306B079, "func: pkcs12_f_pkcs12_gen_mac, reason:pkcs12_r_content_type_not_data, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306B076, "func: pkcs12_f_pkcs12_gen_mac, reason:pkcs12_r_unknown_digest_algorithm, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2306B06B, "func: pkcs12_f_pkcs12_gen_mac, reason:pkcs12_r_key_gen_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307E06C, "func: pkcs12_f_pkcs12_verify_mac, reason:pkcs12_r_mac_absent, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307E06D, "func: pkcs12_f_pkcs12_verify_mac, reason:pkcs12_r_mac_generation_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307B06E, "func: pkcs12_f_pkcs12_set_mac, reason:pkcs12_r_mac_setup_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307B06D, "func: pkcs12_f_pkcs12_set_mac, reason:pkcs12_r_mac_generation_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307B06F, "func: pkcs12_f_pkcs12_set_mac, reason:pkcs12_r_mac_string_set_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307A041, "func: pkcs12_f_pkcs12_setup_mac, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23080069, "func: pkcs12_f_pkcs12_newpass, reason:pkcs12_r_invalid_null_pkcs12_pointer, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23080071, "func: pkcs12_f_pkcs12_newpass, reason:pkcs12_r_mac_verify_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_23080072, "func: pkcs12_f_pkcs12_newpass, reason:pkcs12_r_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307D041, "func: pkcs12_f_pkcs8_encrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307D00D, "func: pkcs12_f_pkcs8_encrypt, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2307D067, "func: pkcs12_f_pkcs8_encrypt, reason:pkcs12_r_encrypt_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21076041, "func: pkcs7_f_pkcs7_add_attrib_smimecap, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21077041, "func: pkcs7_f_pkcs7_simple_smimecap, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107D020, "func: pkcs7_f_pkcs7_bio_add_digest, reason:err_r_bio_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107D06D, "func: pkcs7_f_pkcs7_bio_add_digest, reason:pkcs7_r_unknown_digest_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21069070, "func: pkcs7_f_pkcs7_datainit, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21069020, "func: pkcs7_f_pkcs7_datainit, reason:err_r_bio_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21069067, "func: pkcs7_f_pkcs7_datainit, reason:pkcs7_r_missing_ceripend_info, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21069041, "func: pkcs7_f_pkcs7_datainit, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21069006, "func: pkcs7_f_pkcs7_datainit, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107006F, "func: pkcs7_f_pkcs7_datadecode, reason:pkcs7_r_unsupported_cipher_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21070070, "func: pkcs7_f_pkcs7_datadecode, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21070020, "func: pkcs7_f_pkcs7_datadecode, reason:err_r_bio_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107006D, "func: pkcs7_f_pkcs7_datadecode, reason:pkcs7_r_unknown_digest_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21070041, "func: pkcs7_f_pkcs7_datadecode, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107F06C, "func: pkcs7_f_pkcs7_find_digest, reason:pkcs7_r_unable_to_find_message_digest, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107F044, "func: pkcs7_f_pkcs7_find_digest, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21080041, "func: pkcs7_f_pkcs7_datafinal, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21080020, "func: pkcs7_f_pkcs7_datafinal, reason:err_r_bio_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21080006, "func: pkcs7_f_pkcs7_datafinal, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2108000D, "func: pkcs7_f_pkcs7_datafinal, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2108006B, "func: pkcs7_f_pkcs7_datafinal, reason:pkcs7_r_unable_to_find_mem_bio, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106B072, "func: pkcs7_f_pkcs7_dataverify, reason:pkcs7_r_wrong_pkcs7_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106B06A, "func: pkcs7_f_pkcs7_dataverify, reason:pkcs7_r_unable_to_find_certificate, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106B00B, "func: pkcs7_f_pkcs7_dataverify, reason:err_r_x509_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21068068, "func: pkcs7_f_pkcs7_ctrl, reason:pkcs7_r_operation_not_supported_on_this_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106806E, "func: pkcs7_f_pkcs7_ctrl, reason:pkcs7_r_unknown_operation, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106D070, "func: pkcs7_f_pkcs7_set_content, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106E070, "func: pkcs7_f_pkcs7_set_type, reason:pkcs7_r_unsupported_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21067071, "func: pkcs7_f_pkcs7_add_signer, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21067041, "func: pkcs7_f_pkcs7_add_signer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21064071, "func: pkcs7_f_pkcs7_add_certificate, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21064041, "func: pkcs7_f_pkcs7_add_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21065071, "func: pkcs7_f_pkcs7_add_crl, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21065041, "func: pkcs7_f_pkcs7_add_crl, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107E041, "func: pkcs7_f_pkcs7_set_digest, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107E071, "func: pkcs7_f_pkcs7_set_digest, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21066071, "func: pkcs7_f_pkcs7_add_recipient_info, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106C071, "func: pkcs7_f_pkcs7_set_cipher, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2106C090, "func: pkcs7_f_pkcs7_set_cipher, reason:pkcs7_r_cipher_has_no_object_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21079041, "func: pkcs7_f_b64_write_pkcs7, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21078041, "func: pkcs7_f_b64_read_pkcs7, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21078082, "func: pkcs7_f_b64_read_pkcs7, reason:pkcs7_r_decode_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A085, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_mime_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A087, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A089, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_multipart_boundary, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A088, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_multipart_body_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A086, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_mime_sig_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A08A, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_no_sig_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A08D, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_sig_invalid_mime_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A08C, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_pkcs7_sig_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A083, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_invalid_mime_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107A08B, "func: pkcs7_f_smime_read_pkcs7, reason:pkcs7_r_pkcs7_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107B085, "func: pkcs7_f_smime_text, reason:pkcs7_r_mime_parse_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107B084, "func: pkcs7_f_smime_text, reason:pkcs7_r_mime_no_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107B083, "func: pkcs7_f_smime_text, reason:pkcs7_r_invalid_mime_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107407F, "func: pkcs7_f_pkcs7_sign, reason:pkcs7_r_private_key_does_not_match_certificate, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21074041, "func: pkcs7_f_pkcs7_sign, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107407C, "func: pkcs7_f_pkcs7_sign, reason:pkcs7_r_pkcs7_add_signature_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21074091, "func: pkcs7_f_pkcs7_sign, reason:pkcs7_r_pkcs7_datasign, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107508F, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_invalid_null_pointer, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21075071, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107507A, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_no_content, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21075076, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_content_and_data_present, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107507B, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_no_signatures_on_data, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107500B, "func: pkcs7_f_pkcs7_verify, reason:err_r_x509_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21075075, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_certificate_verify_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21075041, "func: pkcs7_f_pkcs7_verify, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21075081, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_smime_text_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21075069, "func: pkcs7_f_pkcs7_verify, reason:pkcs7_r_signature_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107C08F, "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_invalid_null_pointer, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107C071, "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107C08E, "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_no_signers, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107C041, "func: pkcs7_f_pkcs7_get0_signers, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107C080, "func: pkcs7_f_pkcs7_get0_signers, reason:pkcs7_r_signer_certificate_not_found, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21073041, "func: pkcs7_f_pkcs7_encrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21073079, "func: pkcs7_f_pkcs7_encrypt, reason:pkcs7_r_error_setting_cipher, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107307D, "func: pkcs7_f_pkcs7_encrypt, reason:pkcs7_r_pkcs7_datafinal_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2107208F, "func: pkcs7_f_pkcs7_decrypt, reason:pkcs7_r_invalid_null_pointer, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21072071, "func: pkcs7_f_pkcs7_decrypt, reason:pkcs7_r_wrong_content_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21072077, "func: pkcs7_f_pkcs7_decrypt, reason:pkcs7_r_decrypt_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_21072041, "func: pkcs7_f_pkcs7_decrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C084043, "func: store_f_store_new_method, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C084041, "func: store_f_store_new_method, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C085026, "func: store_f_store_new_engine, reason:err_r_engine_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C085043, "func: store_f_store_new_engine, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C08502C, "func: store_f_store_new_engine, reason:err_r_store_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C0A1043, "func: store_f_store_ctrl, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C0A1090, "func: store_f_store_ctrl, reason:store_r_no_control_function, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C09307F, "func: store_f_store_attr_info_set_cstr, reason:store_r_already_has_a_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C09607F, "func: store_f_store_attr_info_set_sha1str, reason:store_r_already_has_a_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C09407F, "func: store_f_store_attr_info_set_dn, reason:store_r_already_has_a_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C09507F, "func: store_f_store_attr_info_set_number, reason:store_r_already_has_a_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C0AB043, "func: store_f_store_parse_attrs_start, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C098043, "func: store_f_store_parse_attrs_next, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C097043, "func: store_f_store_parse_attrs_end, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2C0AC043, "func: store_f_store_parse_attrs_endp, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B066067, "func: x509_f_dir_ctrl, reason:x509_r_loading_cert_dir, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B064071, "func: x509_f_add_cert_dir, reason:x509_r_invalid_directory, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B064041, "func: x509_f_add_cert_dir, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B067070, "func: x509_f_get_cert_by_subject, reason:x509_r_wrong_lookup_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B067007, "func: x509_f_get_cert_by_subject, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B067041, "func: x509_f_get_cert_by_subject, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B065068, "func: x509_f_by_file_ctrl, reason:x509_r_loading_defaults, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06F002, "func: x509_f_x509_load_cert_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06F00D, "func: x509_f_x509_load_cert_file, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06F064, "func: x509_f_x509_load_cert_file, reason:x509_r_bad_x509_filetype, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B070002, "func: x509_f_x509_load_crl_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07000D, "func: x509_f_x509_load_crl_file, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B070064, "func: x509_f_x509_load_crl_file, reason:x509_r_bad_x509_filetype, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B084002, "func: x509_f_x509_load_cert_crl_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B084009, "func: x509_f_x509_load_cert_crl_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B071041, "func: x509_f_x509_name_add_entry, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07206D, "func: x509_f_x509_name_entry_create_by_nid, reason:x509_r_unknown_nid, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B073043, "func: x509_f_x509_name_entry_set_object, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B081041, "func: x509_f_netscape_spki_b64_decode, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B082041, "func: x509_f_netscape_spki_b64_encode, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B087043, "func: x509_f_x509at_add1_attr, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B087041, "func: x509_f_x509at_add1_attr, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08806D, "func: x509_f_x509_attribute_create_by_nid, reason:x509_r_unknown_nid, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B089041, "func: x509_f_x509_attribute_create_by_obj, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08A00D, "func: x509_f_x509_attribute_set1_data, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08A041, "func: x509_f_x509_attribute_set1_data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08B07A, "func: x509_f_x509_attribute_get0_data, reason:x509_r_wrong_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B080074, "func: x509_f_x509_check_private_key, reason:x509_r_key_values_mismatch, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B080073, "func: x509_f_x509_check_private_key, reason:x509_r_key_type_mismatch, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B080010, "func: x509_f_x509_check_private_key, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B080072, "func: x509_f_x509_check_private_key, reason:x509_r_cant_check_dh_key, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B080075, "func: x509_f_x509_check_private_key, reason:x509_r_unknown_key_type, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07C041, "func: x509_f_x509_store_add_cert, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07C065, "func: x509_f_x509_store_add_cert, reason:x509_r_cert_already_in_hash_table, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07D041, "func: x509_f_x509_store_add_crl, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07D065, "func: x509_f_x509_store_add_crl, reason:x509_r_cert_already_in_hash_table, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B09206A, "func: x509_f_x509_store_ctx_get1_issuer, reason:x509_r_should_retry, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B074041, "func: x509_f_x509_name_oneline, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08D07B, "func: x509_f_x509_trust_set, reason:x509_r_invalid_trust, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B085041, "func: x509_f_x509_trust_add, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B068043, "func: x509_f_x509v3_add_ext, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B068041, "func: x509_f_x509v3_add_ext, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06C06D, "func: x509_f_x509_extension_create_by_nid, reason:x509_r_unknown_nid, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06D041, "func: x509_f_x509_extension_create_by_obj, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07F069, "func: x509_f_x509_verify_cert, reason:x509_r_no_cert_set_for_us_to_verify, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B07F041, "func: x509_f_x509_verify_cert, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B091041, "func: x509_f_check_policy, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06E06C, "func: x509_f_x509_get_pubkey_parameters, reason:x509_r_unable_to_get_certs_public_key, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B06E06B, "func: x509_f_x509_get_pubkey_parameters, reason:x509_r_unable_to_find_parameters_in_chain, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08E041, "func: x509_f_x509_store_ctx_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_0B08F041, "func: x509_f_x509_store_ctx_init, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209F041, "func: x509v3_f_v2i_ipaddrblocks, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209F073, "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_extension_name_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209F0A4, "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_invalid_safi, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209F0A2, "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_invalid_inheritance, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209F0A3, "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_invalid_ipaddress, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209F074, "func: x509v3_f_v2i_ipaddrblocks, reason:x509v3_r_extension_value_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_220A0041, "func: x509v3_f_v3_addr_validate_path_internal, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22077078, "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_unknown_option, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22077079, "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_no_issuer_certificate, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207707B, "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_unable_to_get_issuer_keyid, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207707A, "func: x509v3_f_v2i_authority_keyid, reason:x509v3_r_unable_to_get_issuer_details, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22077041, "func: x509v3_f_v2i_authority_keyid, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22099041, "func: x509v3_f_v2i_issuer_alt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207B07F, "func: x509v3_f_copy_issuer, reason:x509v3_r_no_issuer_details, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207B07E, "func: x509v3_f_copy_issuer, reason:x509v3_r_issuer_decode_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207B041, "func: x509v3_f_copy_issuer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209A041, "func: x509v3_f_v2i_subject_alt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207A07D, "func: x509v3_f_copy_email, reason:x509v3_r_no_subject_details, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207A041, "func: x509v3_f_copy_email, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22076041, "func: x509v3_f_v2i_general_names, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207507C, "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_missing_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22075041, "func: x509v3_f_v2i_general_name_ex, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22075077, "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_bad_object, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22075076, "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_bad_ip_address, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22075095, "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_dirname_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22075093, "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_othername_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22075075, "func: x509v3_f_v2i_general_name_ex, reason:x509v3_r_unsupported_option, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22090096, "func: x509v3_f_do_dirname, reason:x509v3_r_section_not_found, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209C041, "func: x509v3_f_asidentifierchoice_canonize, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209E041, "func: x509v3_f_v2i_asidentifiers, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209E073, "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_extension_name_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209E0A2, "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_invalid_inheritance, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209E0A0, "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_invalid_asnumber, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209E0A1, "func: x509v3_f_v2i_asidentifiers, reason:x509v3_r_invalid_asrange, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22066041, "func: x509v3_f_v2i_basic_constraints, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206606A, "func: x509v3_f_v2i_basic_constraints, reason:x509v3_r_invalid_name, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22065041, "func: x509v3_f_v2i_asn1_bit_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22098080, "func: x509v3_f_x509v3_ext_nconf, reason:x509v3_r_error_in_extension, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22097082, "func: x509v3_f_do_ext_nconf, reason:x509v3_r_unknown_extension_name, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22097081, "func: x509v3_f_do_ext_nconf, reason:x509v3_r_unknown_extension, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22097069, "func: x509v3_f_do_ext_nconf, reason:x509v3_r_invalid_extension_string, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22097088, "func: x509v3_f_do_ext_nconf, reason:x509v3_r_no_config_database, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22097067, "func: x509v3_f_do_ext_nconf, reason:x509v3_r_extension_setting_not_supported, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22087041, "func: x509v3_f_do_ext_i2d, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22088081, "func: x509v3_f_x509v3_ext_i2d, reason:x509v3_r_unknown_extension, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22074073, "func: x509v3_f_v3_generic_extension, reason:x509v3_r_extension_name_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22074074, "func: x509v3_f_v3_generic_extension, reason:x509v3_r_extension_value_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22074041, "func: x509v3_f_v3_generic_extension, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208F094, "func: x509v3_f_x509v3_get_string, reason:x509v3_r_operation_not_defined, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208E094, "func: x509v3_f_x509v3_get_section, reason:x509v3_r_operation_not_defined, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22082041, "func: x509v3_f_r2i_certpol, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22082022, "func: x509v3_f_r2i_certpol, reason:err_r_x509v3_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22082086, "func: x509v3_f_r2i_certpol, reason:x509v3_r_invalid_policy_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22082087, "func: x509v3_f_r2i_certpol, reason:x509v3_r_invalid_section, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208206E, "func: x509v3_f_r2i_certpol, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208306E, "func: x509v3_f_policy_section, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22083089, "func: x509v3_f_policy_section, reason:x509v3_r_expected_a_section_name, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22083087, "func: x509v3_f_policy_section, reason:x509v3_r_invalid_section, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208308A, "func: x509v3_f_policy_section, reason:x509v3_r_invalid_option, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208308B, "func: x509v3_f_policy_section, reason:x509v3_r_no_policy_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22083041, "func: x509v3_f_policy_section, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208408D, "func: x509v3_f_notice_section, reason:x509v3_r_invalid_numbers, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208408A, "func: x509v3_f_notice_section, reason:x509v3_r_invalid_option, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208408E, "func: x509v3_f_notice_section, reason:x509v3_r_need_organization_and_numbers, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22084041, "func: x509v3_f_notice_section, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208508C, "func: x509v3_f_nref_nos, reason:x509v3_r_invalid_number, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22085041, "func: x509v3_f_nref_nos, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22086041, "func: x509v3_f_v2i_crld, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22067041, "func: x509v3_f_v2i_extended_key_usage, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206706E, "func: x509v3_f_v2i_extended_key_usage, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22095041, "func: x509v3_f_i2s_asn1_ia5string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206406B, "func: x509v3_f_s2i_asn1_ia5string, reason:x509v3_r_invalid_null_argument, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22064041, "func: x509v3_f_s2i_asn1_ia5string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208B041, "func: x509v3_f_v2i_authority_info_access, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208B08F, "func: x509v3_f_v2i_authority_info_access, reason:x509v3_r_invalid_syntax, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208B077, "func: x509v3_f_v2i_authority_info_access, reason:x509v3_r_bad_object, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22068041, "func: x509v3_f_x509v3_ext_add, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206A066, "func: x509v3_f_x509v3_ext_add_alias, reason:x509v3_r_extension_not_found, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206A041, "func: x509v3_f_x509v3_ext_add_alias, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208C090, "func: x509v3_f_x509v3_add1_i2d, reason:x509v3_r_error_creating_extension, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208C000, "func: x509v3_f_x509v3_add1_i2d, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209308F, "func: x509v3_f_v2i_name_constraints, reason:x509v3_r_invalid_syntax, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22093041, "func: x509v3_f_v2i_name_constraints, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209609B, "func: x509v3_f_process_pci_value, reason:x509v3_r_policy_language_alreadty_defined, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209606E, "func: x509v3_f_process_pci_value, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209609D, "func: x509v3_f_process_pci_value, reason:x509v3_r_policy_path_length_alreadty_defined, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209609C, "func: x509v3_f_process_pci_value, reason:x509v3_r_policy_path_length, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22096041, "func: x509v3_f_process_pci_value, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22096020, "func: x509v3_f_process_pci_value, reason:err_r_bio_lib, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22096098, "func: x509v3_f_process_pci_value, reason:x509v3_r_incorrect_policy_syntax_tag, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209B099, "func: x509v3_f_r2i_pci, reason:x509v3_r_invalid_proxy_policy_setting, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209B087, "func: x509v3_f_r2i_pci, reason:x509v3_r_invalid_section, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209B09A, "func: x509v3_f_r2i_pci, reason:x509v3_r_no_proxy_cert_policy_language_defined, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209B09F, "func: x509v3_f_r2i_pci, reason:x509v3_r_policy_when_proxy_language_requires_no_policy, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209B041, "func: x509v3_f_r2i_pci, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22092041, "func: x509v3_f_v2i_policy_constraints, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209206A, "func: x509v3_f_v2i_policy_constraints, reason:x509v3_r_invalid_name, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22092097, "func: x509v3_f_v2i_policy_constraints, reason:x509v3_r_illegal_empty_extension, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22091041, "func: x509v3_f_v2i_policy_mappings, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2209106E, "func: x509v3_f_v2i_policy_mappings, reason:x509v3_r_invalid_object_identifier, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2208D092, "func: x509v3_f_x509_purpose_set, reason:x509v3_r_invalid_purpose, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22089041, "func: x509v3_f_x509_purpose_add, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22070041, "func: x509v3_f_s2i_asn1_octet_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22073041, "func: x509v3_f_s2i_skey_id, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22073072, "func: x509v3_f_s2i_skey_id, reason:x509v3_r_no_public_key, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207D083, "func: x509v3_f_sxnet_add_id_asc, reason:x509v3_r_error_converting_zone, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207F041, "func: x509v3_f_sxnet_add_id_ulong, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207E06B, "func: x509v3_f_sxnet_add_id_integer, reason:x509v3_r_invalid_null_argument, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207E084, "func: x509v3_f_sxnet_add_id_integer, reason:x509v3_r_user_too_long, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207E085, "func: x509v3_f_sxnet_add_id_integer, reason:x509v3_r_duplicate_zone_id, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207E041, "func: x509v3_f_sxnet_add_id_integer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22080083, "func: x509v3_f_sxnet_get_id_asc, reason:x509v3_r_error_converting_zone, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22081041, "func: x509v3_f_sxnet_get_id_ulong, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22069041, "func: x509v3_f_x509v3_add_value, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22079041, "func: x509v3_f_i2s_asn1_enumerated, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22078041, "func: x509v3_f_i2s_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206C06D, "func: x509v3_f_s2i_asn1_integer, reason:x509v3_r_invalid_null_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206C064, "func: x509v3_f_s2i_asn1_integer, reason:x509v3_r_bn_dec2bn_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206C065, "func: x509v3_f_s2i_asn1_integer, reason:x509v3_r_bn_to_asn1_integer_error, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206E068, "func: x509v3_f_x509v3_get_value_bool, reason:x509v3_r_invalid_boolean_string, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206D06C, "func: x509v3_f_x509v3_parse_list, reason:x509v3_r_invalid_null_name, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206D06D, "func: x509v3_f_x509v3_parse_list, reason:x509v3_r_invalid_null_value, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2206F041, "func: x509v3_f_hex_to_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_2207106B, "func: x509v3_f_string_to_hex, reason:x509v3_r_invalid_null_argument, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22071070, "func: x509v3_f_string_to_hex, reason:x509v3_r_odd_number_of_digits, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22071041, "func: x509v3_f_string_to_hex, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(PKI_TRC_22071071, "func: x509v3_f_string_to_hex, reason:x509v3_r_illegal_hex_digit, file:%s, line:%d")

END_TRACE_MAP(MOD_OSSL_PKI)

#endif /* __AGPS_SWIP_REL__ */
#endif /* !_PKI_TRC_H_ */
