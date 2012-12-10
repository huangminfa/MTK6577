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
 *   ssl_trc.h
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


#ifndef _SSL_TRC_H_
#define _SSL_TRC_H_


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
    #define SSL_TRC_140E8042 "func: ssl_f_ssl_ctrl, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_140750DD "func: ssl_f_ssl23_connect, reason:ssl_r_ssl23_doing_session_id_reuse, file:%s, line:%d"
    #define SSL_TRC_140750FF "func: ssl_f_ssl23_connect, reason:ssl_r_unknown_state, file:%s, line:%d"
    #define SSL_TRC_140740BF "func: ssl_f_ssl23_client_hello, reason:ssl_r_no_protocols_available, file:%s, line:%d"
    #define SSL_TRC_140740B5 "func: ssl_f_ssl23_client_hello, reason:ssl_r_no_ciphers_available, file:%s, line:%d"
    #define SSL_TRC_14074044 "func: ssl_f_ssl23_client_hello, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_14077102 "func: ssl_f_ssl23_get_server_hello, reason:ssl_r_unsupported_protocol, file:%s, line:%d"
    #define SSL_TRC_14077007 "func: ssl_f_ssl23_get_server_hello, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140770FC "func: ssl_f_ssl23_get_server_hello, reason:ssl_r_unknown_protocol, file:%s, line:%d"
    #define SSL_TRC_140780E5 "func: ssl_f_ssl23_read, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_140ED0E5 "func: ssl_f_ssl23_peek, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_140790E5 "func: ssl_f_ssl23_write, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_140730FF "func: ssl_f_ssl23_accept, reason:ssl_r_unknown_state, file:%s, line:%d"
    #define SSL_TRC_1407612A "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_record_too_small, file:%s, line:%d"
    #define SSL_TRC_1407609C "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_http_request, file:%s, line:%d"
    #define SSL_TRC_1407609B "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_https_proxy_request, file:%s, line:%d"
    #define SSL_TRC_140760D6 "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_record_too_large, file:%s, line:%d"
    #define SSL_TRC_140760D5 "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_record_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_14076102 "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_unsupported_protocol, file:%s, line:%d"
    #define SSL_TRC_140760FC "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_unknown_protocol, file:%s, line:%d"
    #define SSL_TRC_1407B0FF "func: ssl_f_ssl2_connect, reason:ssl_r_unknown_state, file:%s, line:%d"
    #define SSL_TRC_1406D128 "func: ssl_f_get_server_hello, reason:ssl_r_message_too_long, file:%s, line:%d"
    #define SSL_TRC_1406D0D8 "func: ssl_f_get_server_hello, reason:ssl_r_reuse_cert_length_not_zero, file:%s, line:%d"
    #define SSL_TRC_1406D0D9 "func: ssl_f_get_server_hello, reason:ssl_r_reuse_cert_type_not_zero, file:%s, line:%d"
    #define SSL_TRC_1406D0DA "func: ssl_f_get_server_hello, reason:ssl_r_reuse_cipher_list_not_zero, file:%s, line:%d"
    #define SSL_TRC_1406D0B8 "func: ssl_f_get_server_hello, reason:ssl_r_no_cipher_list, file:%s, line:%d"
    #define SSL_TRC_1406D041 "func: ssl_f_get_server_hello, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1406D0B9 "func: ssl_f_get_server_hello, reason:ssl_r_no_cipher_match, file:%s, line:%d"
    #define SSL_TRC_1406D044 "func: ssl_f_get_server_hello, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1406D12B "func: ssl_f_get_server_hello, reason:ssl_r_ssl2_connection_id_too_long, file:%s, line:%d"
    #define SSL_TRC_140650B5 "func: ssl_f_client_hello, reason:ssl_r_no_ciphers_available, file:%s, line:%d"
    #define SSL_TRC_140660CE "func: ssl_f_client_master_key, reason:ssl_r_problems_mapping_cipher_functions, file:%s, line:%d"
    #define SSL_TRC_14066044 "func: ssl_f_client_master_key, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1406608B "func: ssl_f_client_master_key, reason:ssl_r_cipher_table_src_error, file:%s, line:%d"
    #define SSL_TRC_140660D0 "func: ssl_f_client_master_key, reason:ssl_r_public_key_encrypt_error, file:%s, line:%d"
    #define SSL_TRC_140A7044 "func: ssl_f_client_finished, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_14064066 "func: ssl_f_client_certificate, reason:ssl_r_bad_authentication_type, file:%s, line:%d"
    #define SSL_TRC_1406406A "func: ssl_f_client_certificate, reason:ssl_r_bad_data_returned_by_callback, file:%s, line:%d"
    #define SSL_TRC_1406E0C8 "func: ssl_f_get_server_verify, reason:ssl_r_peer_error, file:%s, line:%d"
    #define SSL_TRC_1406E088 "func: ssl_f_get_server_verify, reason:ssl_r_challenge_is_different, file:%s, line:%d"
    #define SSL_TRC_1406C0D4 "func: ssl_f_get_server_finished, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d"
    #define SSL_TRC_1406C0C8 "func: ssl_f_get_server_finished, reason:ssl_r_peer_error, file:%s, line:%d"
    #define SSL_TRC_1406C0E7 "func: ssl_f_get_server_finished, reason:ssl_r_ssl_session_id_is_different, file:%s, line:%d"
    #define SSL_TRC_1407E00B "func: ssl_f_ssl2_set_certificate, reason:err_r_x509_lib, file:%s, line:%d"
    #define SSL_TRC_1407E041 "func: ssl_f_ssl2_set_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1407E086 "func: ssl_f_ssl2_set_certificate, reason:ssl_r_certificate_verify_failed, file:%s, line:%d"
    #define SSL_TRC_1407E0ED "func: ssl_f_ssl2_set_certificate, reason:ssl_r_unable_to_extract_public_key, file:%s, line:%d"
    #define SSL_TRC_1407E0D2 "func: ssl_f_ssl2_set_certificate, reason:ssl_r_public_key_not_rsa, file:%s, line:%d"
    #define SSL_TRC_140BC0C0 "func: ssl_f_ssl_rsa_public_encrypt, reason:ssl_r_no_publickey, file:%s, line:%d"
    #define SSL_TRC_140BC0D1 "func: ssl_f_ssl_rsa_public_encrypt, reason:ssl_r_public_key_is_not_rsa, file:%s, line:%d"
    #define SSL_TRC_140BC004 "func: ssl_f_ssl_rsa_public_encrypt, reason:err_r_rsa_lib, file:%s, line:%d"
    #define SSL_TRC_1407C0CE "func: ssl_f_ssl2_enc_init, reason:ssl_r_problems_mapping_cipher_functions, file:%s, line:%d"
    #define SSL_TRC_1407C041 "func: ssl_f_ssl2_enc_init, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140F1044 "func: ssl_f_ssl2_generate_key_material, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140EC0E5 "func: ssl_f_ssl2_read_internal, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_140EC0AF "func: ssl_f_ssl2_read_internal, reason:ssl_r_non_sslv2_initial_packet, file:%s, line:%d"
    #define SSL_TRC_140EC11B "func: ssl_f_ssl2_read_internal, reason:ssl_r_illegal_padding, file:%s, line:%d"
    #define SSL_TRC_140EC071 "func: ssl_f_ssl2_read_internal, reason:ssl_r_bad_mac_decode, file:%s, line:%d"
    #define SSL_TRC_140EC07E "func: ssl_f_ssl2_read_internal, reason:ssl_r_bad_state, file:%s, line:%d"
    #define SSL_TRC_140700D3 "func: ssl_f_read_n, reason:ssl_r_read_bio_not_set, file:%s, line:%d"
    #define SSL_TRC_1407F0E5 "func: ssl_f_ssl2_write, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_140D407F "func: ssl_f_write_pending, reason:ssl_r_bad_write_retry, file:%s, line:%d"
    #define SSL_TRC_140D4104 "func: ssl_f_write_pending, reason:ssl_r_write_bio_not_set, file:%s, line:%d"
    #define SSL_TRC_1407A0B3 "func: ssl_f_ssl2_accept, reason:ssl_r_no_certificate_set, file:%s, line:%d"
    #define SSL_TRC_1407A0FF "func: ssl_f_ssl2_accept, reason:ssl_r_unknown_state, file:%s, line:%d"
    #define SSL_TRC_1406B0D4 "func: ssl_f_get_client_master_key, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d"
    #define SSL_TRC_1406B0C8 "func: ssl_f_get_client_master_key, reason:ssl_r_peer_error, file:%s, line:%d"
    #define SSL_TRC_1406B0B9 "func: ssl_f_get_client_master_key, reason:ssl_r_no_cipher_match, file:%s, line:%d"
    #define SSL_TRC_1406B11C "func: ssl_f_get_client_master_key, reason:ssl_r_key_arg_too_long, file:%s, line:%d"
    #define SSL_TRC_1406B044 "func: ssl_f_get_client_master_key, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1406B128 "func: ssl_f_get_client_master_key, reason:ssl_r_message_too_long, file:%s, line:%d"
    #define SSL_TRC_1406B0BD "func: ssl_f_get_client_master_key, reason:ssl_r_no_privatekey, file:%s, line:%d"
    #define SSL_TRC_1406B0CE "func: ssl_f_get_client_master_key, reason:ssl_r_problems_mapping_cipher_functions, file:%s, line:%d"
    #define SSL_TRC_1406B076 "func: ssl_f_get_client_master_key, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d"
    #define SSL_TRC_1406B107 "func: ssl_f_get_client_master_key, reason:ssl_r_wrong_number_of_key_bits, file:%s, line:%d"
    #define SSL_TRC_1406A0D4 "func: ssl_f_get_client_hello, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d"
    #define SSL_TRC_1406A0C8 "func: ssl_f_get_client_hello, reason:ssl_r_peer_error, file:%s, line:%d"
    #define SSL_TRC_1406A09E "func: ssl_f_get_client_hello, reason:ssl_r_invalid_challenge_length, file:%s, line:%d"
    #define SSL_TRC_1406A128 "func: ssl_f_get_client_hello, reason:ssl_r_message_too_long, file:%s, line:%d"
    #define SSL_TRC_1406A07D "func: ssl_f_get_client_hello, reason:ssl_r_bad_ssl_session_id_length, file:%s, line:%d"
    #define SSL_TRC_1406A0B3 "func: ssl_f_get_client_hello, reason:ssl_r_no_certificate_set, file:%s, line:%d"
    #define SSL_TRC_1406A044 "func: ssl_f_get_client_hello, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1406A041 "func: ssl_f_get_client_hello, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_14072041 "func: ssl_f_server_hello, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140720B4 "func: ssl_f_server_hello, reason:ssl_r_no_certificate_specified, file:%s, line:%d"
    #define SSL_TRC_140690D4 "func: ssl_f_get_client_finished, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d"
    #define SSL_TRC_140690C8 "func: ssl_f_get_client_finished, reason:ssl_r_peer_error, file:%s, line:%d"
    #define SSL_TRC_14069044 "func: ssl_f_get_client_finished, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1406908F "func: ssl_f_get_client_finished, reason:ssl_r_connection_id_is_different, file:%s, line:%d"
    #define SSL_TRC_140F0044 "func: ssl_f_server_verify, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140EF044 "func: ssl_f_server_finish, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140710C7 "func: ssl_f_request_certificate, reason:ssl_r_peer_did_not_return_a_certificate, file:%s, line:%d"
    #define SSL_TRC_140710DB "func: ssl_f_request_certificate, reason:ssl_r_short_read, file:%s, line:%d"
    #define SSL_TRC_14071044 "func: ssl_f_request_certificate, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_14071075 "func: ssl_f_request_certificate, reason:ssl_r_bad_response_argument, file:%s, line:%d"
    #define SSL_TRC_14071128 "func: ssl_f_request_certificate, reason:ssl_r_message_too_long, file:%s, line:%d"
    #define SSL_TRC_1407100B "func: ssl_f_request_certificate, reason:err_r_x509_lib, file:%s, line:%d"
    #define SSL_TRC_14071041 "func: ssl_f_request_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_14071068 "func: ssl_f_request_certificate, reason:ssl_r_bad_checksum, file:%s, line:%d"
    #define SSL_TRC_140BB0BD "func: ssl_f_ssl_rsa_private_decrypt, reason:ssl_r_no_privatekey, file:%s, line:%d"
    #define SSL_TRC_140BB0D1 "func: ssl_f_ssl_rsa_private_decrypt, reason:ssl_r_public_key_is_not_rsa, file:%s, line:%d"
    #define SSL_TRC_140BB004 "func: ssl_f_ssl_rsa_private_decrypt, reason:err_r_rsa_lib, file:%s, line:%d"
    #define SSL_TRC_1408C09A "func: ssl_f_ssl3_get_finished, reason:ssl_r_got_a_fin_before_a_ccs, file:%s, line:%d"
    #define SSL_TRC_1408C06F "func: ssl_f_ssl3_get_finished, reason:ssl_r_bad_digest_length, file:%s, line:%d"
    #define SSL_TRC_1408C095 "func: ssl_f_ssl3_get_finished, reason:ssl_r_digest_check_failed, file:%s, line:%d"
    #define SSL_TRC_14093007 "func: ssl_f_ssl3_output_cert_chain, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_1409300B "func: ssl_f_ssl3_output_cert_chain, reason:err_r_x509_lib, file:%s, line:%d"
    #define SSL_TRC_1408E0F4 "func: ssl_f_ssl3_get_message, reason:ssl_r_unexpected_message, file:%s, line:%d"
    #define SSL_TRC_1408E098 "func: ssl_f_ssl3_get_message, reason:ssl_r_excessive_message_size, file:%s, line:%d"
    #define SSL_TRC_1408E007 "func: ssl_f_ssl3_get_message, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_1409C041 "func: ssl_f_ssl3_setup_buffers, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_14084044 "func: ssl_f_ssl3_connect, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140840FF "func: ssl_f_ssl3_connect, reason:ssl_r_unknown_state, file:%s, line:%d"
    #define SSL_TRC_14083044 "func: ssl_f_ssl3_client_hello, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140830B5 "func: ssl_f_ssl3_client_hello, reason:ssl_r_no_ciphers_available, file:%s, line:%d"
    #define SSL_TRC_14092072 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_bad_message_type, file:%s, line:%d"
    #define SSL_TRC_1409210A "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_wrong_ssl_version, file:%s, line:%d"
    #define SSL_TRC_1409212C "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_ssl3_session_id_too_long, file:%s, line:%d"
    #define SSL_TRC_14092110 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_attempt_to_reuse_session_in_different_context, file:%s, line:%d"
    #define SSL_TRC_140920F8 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_unknown_cipher_returned, file:%s, line:%d"
    #define SSL_TRC_14092105 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_wrong_cipher_returned, file:%s, line:%d"
    #define SSL_TRC_140920C5 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_old_session_cipher_not_returned, file:%s, line:%d"
    #define SSL_TRC_14092101 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_unsupported_compression_algorithm, file:%s, line:%d"
    #define SSL_TRC_140920DF "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_parse_tlsext, file:%s, line:%d"
    #define SSL_TRC_140920E0 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_serverhello_tlsext, file:%s, line:%d"
    #define SSL_TRC_14092073 "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_bad_packet_length, file:%s, line:%d"
    #define SSL_TRC_14090072 "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_bad_message_type, file:%s, line:%d"
    #define SSL_TRC_14090041 "func: ssl_f_ssl3_get_server_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1409009F "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_14090087 "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_cert_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_1409000D "func: ssl_f_ssl3_get_server_certificate, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_14090086 "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_certificate_verify_failed, file:%s, line:%d"
    #define SSL_TRC_1408D041 "func: ssl_f_ssl3_get_key_exchange, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1408D079 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_rsa_modulus_length, file:%s, line:%d"
    #define SSL_TRC_1408D003 "func: ssl_f_ssl3_get_key_exchange, reason:err_r_bn_lib, file:%s, line:%d"
    #define SSL_TRC_1408D078 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_rsa_e_length, file:%s, line:%d"
    #define SSL_TRC_1408D044 "func: ssl_f_ssl3_get_key_exchange, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1408D005 "func: ssl_f_ssl3_get_key_exchange, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_1408D06E "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_dh_p_length, file:%s, line:%d"
    #define SSL_TRC_1408D06C "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_dh_g_length, file:%s, line:%d"
    #define SSL_TRC_1408D06D "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_dh_pub_key_length, file:%s, line:%d"
    #define SSL_TRC_1408D0EB "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_tried_to_use_unsupported_cipher, file:%s, line:%d"
    #define SSL_TRC_1408D13A "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_unable_to_find_ecdh_parameters, file:%s, line:%d"
    #define SSL_TRC_1408D010 "func: ssl_f_ssl3_get_key_exchange, reason:err_r_ec_lib, file:%s, line:%d"
    #define SSL_TRC_1408D136 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_ecgroup_too_large_for_cipher, file:%s, line:%d"
    #define SSL_TRC_1408D132 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_ecpoint, file:%s, line:%d"
    #define SSL_TRC_1408D0F4 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_unexpected_message, file:%s, line:%d"
    #define SSL_TRC_1408D108 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_wrong_signature_length, file:%s, line:%d"
    #define SSL_TRC_1408D076 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d"
    #define SSL_TRC_1408D07B "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_signature, file:%s, line:%d"
    #define SSL_TRC_1408D099 "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_extra_data_in_message, file:%s, line:%d"
    #define SSL_TRC_14087106 "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_wrong_message_type, file:%s, line:%d"
    #define SSL_TRC_140870E8 "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_tls_client_cert_req_with_anon_cipher, file:%s, line:%d"
    #define SSL_TRC_14087041 "func: ssl_f_ssl3_get_certificate_request, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1408709F "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_14087084 "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_ca_dn_too_long, file:%s, line:%d"
    #define SSL_TRC_1408700D "func: ssl_f_ssl3_get_certificate_request, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_14087083 "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_ca_dn_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_1411B072 "func: ssl_f_ssl3_get_new_session_ticket, reason:ssl_r_bad_message_type, file:%s, line:%d"
    #define SSL_TRC_1411B09F "func: ssl_f_ssl3_get_new_session_ticket, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_1411C09F "func: ssl_f_ssl3_new_session_ticket, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_1411C041 "func: ssl_f_ssl3_new_session_ticket, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1409109F "func: ssl_f_ssl3_get_server_done, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_14098044 "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_14098077 "func: ssl_f_ssl3_send_client_key_exchange, reason:ssl_r_bad_rsa_encrypt, file:%s, line:%d"
    #define SSL_TRC_140980EE "func: ssl_f_ssl3_send_client_key_exchange, reason:ssl_r_unable_to_find_dh_parameters, file:%s, line:%d"
    #define SSL_TRC_14098005 "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_14098041 "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_14098010 "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_ec_lib, file:%s, line:%d"
    #define SSL_TRC_1409802B "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_ecdh_lib, file:%s, line:%d"
    #define SSL_TRC_14099004 "func: ssl_f_ssl3_send_client_verify, reason:err_r_rsa_lib, file:%s, line:%d"
    #define SSL_TRC_1409900A "func: ssl_f_ssl3_send_client_verify, reason:err_r_dsa_lib, file:%s, line:%d"
    #define SSL_TRC_14099044 "func: ssl_f_ssl3_send_client_verify, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1409706A "func: ssl_f_ssl3_send_client_certificate, reason:ssl_r_bad_data_returned_by_callback, file:%s, line:%d"
    #define SSL_TRC_14082044 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_14082130 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_bad_ecc_cert, file:%s, line:%d"
    #define SSL_TRC_140820AA "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_rsa_signing_cert, file:%s, line:%d"
    #define SSL_TRC_140820A5 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dsa_signing_cert, file:%s, line:%d"
    #define SSL_TRC_140820A9 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_rsa_encrypting_cert, file:%s, line:%d"
    #define SSL_TRC_140820A3 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dh_key, file:%s, line:%d"
    #define SSL_TRC_140820A4 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dh_rsa_cert, file:%s, line:%d"
    #define SSL_TRC_140820A2 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dh_dsa_cert, file:%s, line:%d"
    #define SSL_TRC_140820A7 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_export_tmp_rsa_key, file:%s, line:%d"
    #define SSL_TRC_140820A6 "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_export_tmp_dh_key, file:%s, line:%d"
    #define SSL_TRC_140820FA "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_unknown_key_exchange_type, file:%s, line:%d"
    #define SSL_TRC_140EE044 "func: ssl_f_ssl3_generate_key_block, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1408108E "func: ssl_f_ssl3_change_cipher_state, reason:ssl_r_compression_library_error, file:%s, line:%d"
    #define SSL_TRC_14081044 "func: ssl_f_ssl3_change_cipher_state, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_14081041 "func: ssl_f_ssl3_change_cipher_state, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1409D08A "func: ssl_f_ssl3_setup_key_block, reason:ssl_r_cipher_or_hash_unavailable, file:%s, line:%d"
    #define SSL_TRC_1409D041 "func: ssl_f_ssl3_setup_key_block, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_14086081 "func: ssl_f_ssl3_enc, reason:ssl_r_block_cipher_pad_is_wrong, file:%s, line:%d"
    #define SSL_TRC_140D5041 "func: ssl_f_ssl3_ctrl, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140D5043 "func: ssl_f_ssl3_ctrl, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140D5004 "func: ssl_f_ssl3_ctrl, reason:err_r_rsa_lib, file:%s, line:%d"
    #define SSL_TRC_140D5042 "func: ssl_f_ssl3_ctrl, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_140D5005 "func: ssl_f_ssl3_ctrl, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_140D502B "func: ssl_f_ssl3_ctrl, reason:err_r_ecdh_lib, file:%s, line:%d"
    #define SSL_TRC_140D50E1 "func: ssl_f_ssl3_ctrl, reason:ssl_r_ssl3_ext_invalid_servername, file:%s, line:%d"
    #define SSL_TRC_140D5044 "func: ssl_f_ssl3_ctrl, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140D50E2 "func: ssl_f_ssl3_ctrl, reason:ssl_r_ssl3_ext_invalid_servername_type, file:%s, line:%d"
    #define SSL_TRC_140E9041 "func: ssl_f_ssl3_callback_ctrl, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_14085004 "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_rsa_lib, file:%s, line:%d"
    #define SSL_TRC_14085042 "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_14085005 "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_1408502B "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_ecdh_lib, file:%s, line:%d"
    #define SSL_TRC_14085010 "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_ec_lib, file:%s, line:%d"
    #define SSL_TRC_14085113 "func: ssl_f_ssl3_ctx_ctrl, reason:ssl_r_invalid_ticket_keys_length, file:%s, line:%d"
    #define SSL_TRC_14095044 "func: ssl_f_ssl3_read_n, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140950D3 "func: ssl_f_ssl3_read_n, reason:ssl_r_read_bio_not_set, file:%s, line:%d"
    #define SSL_TRC_1408F044 "func: ssl_f_ssl3_get_record, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1408F10B "func: ssl_f_ssl3_get_record, reason:ssl_r_wrong_version_number, file:%s, line:%d"
    #define SSL_TRC_1408F0C6 "func: ssl_f_ssl3_get_record, reason:ssl_r_packet_length_too_long, file:%s, line:%d"
    #define SSL_TRC_1408F096 "func: ssl_f_ssl3_get_record, reason:ssl_r_encrypted_length_too_long, file:%s, line:%d"
    #define SSL_TRC_1408F0CD "func: ssl_f_ssl3_get_record, reason:ssl_r_pre_mac_length_too_long, file:%s, line:%d"
    #define SSL_TRC_1408F0A0 "func: ssl_f_ssl3_get_record, reason:ssl_r_length_too_short, file:%s, line:%d"
    #define SSL_TRC_1408F119 "func: ssl_f_ssl3_get_record, reason:ssl_r_decryption_failed_or_bad_record_mac, file:%s, line:%d"
    #define SSL_TRC_1408F08C "func: ssl_f_ssl3_get_record, reason:ssl_r_compressed_length_too_long, file:%s, line:%d"
    #define SSL_TRC_1408F06B "func: ssl_f_ssl3_get_record, reason:ssl_r_bad_decompression, file:%s, line:%d"
    #define SSL_TRC_1408F092 "func: ssl_f_ssl3_get_record, reason:ssl_r_data_length_too_long, file:%s, line:%d"
    #define SSL_TRC_1409E0E5 "func: ssl_f_ssl3_write_bytes, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_14068044 "func: ssl_f_do_ssl3_write, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1406808D "func: ssl_f_do_ssl3_write, reason:ssl_r_compression_failure, file:%s, line:%d"
    #define SSL_TRC_1409F07F "func: ssl_f_ssl3_write_pending, reason:ssl_r_bad_write_retry, file:%s, line:%d"
    #define SSL_TRC_1409F080 "func: ssl_f_ssl3_write_pending, reason:ssl_r_bio_not_set, file:%s, line:%d"
    #define SSL_TRC_14094044 "func: ssl_f_ssl3_read_bytes, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140940E5 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d"
    #define SSL_TRC_14094091 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_data_between_ccs_and_finished, file:%s, line:%d"
    #define SSL_TRC_14094064 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_app_data_in_handshake, file:%s, line:%d"
    #define SSL_TRC_14094069 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_bad_hello_request, file:%s, line:%d"
    #define SSL_TRC_140940F6 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_unknown_alert_type, file:%s, line:%d"
    #define SSL_TRC_14094067 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_bad_change_cipher_spec, file:%s, line:%d"
    #define SSL_TRC_14094085 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_ccs_received_early, file:%s, line:%d"
    #define SSL_TRC_140940F5 "func: ssl_f_ssl3_read_bytes, reason:ssl_r_unexpected_record, file:%s, line:%d"
    #define SSL_TRC_140800B3 "func: ssl_f_ssl3_accept, reason:ssl_r_no_certificate_set, file:%s, line:%d"
    #define SSL_TRC_14080044 "func: ssl_f_ssl3_accept, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140800FF "func: ssl_f_ssl3_accept, reason:ssl_r_unknown_state, file:%s, line:%d"
    #define SSL_TRC_1408A10B "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_wrong_version_number, file:%s, line:%d"
    #define SSL_TRC_1408A134 "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_cookie_mismatch, file:%s, line:%d"
    #define SSL_TRC_1408A0B7 "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_ciphers_specified, file:%s, line:%d"
    #define SSL_TRC_1408A09F "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_1408A0D7 "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_required_cipher_missing, file:%s, line:%d"
    #define SSL_TRC_1408A0BB "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_compression_specified, file:%s, line:%d"
    #define SSL_TRC_1408A0DF "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_parse_tlsext, file:%s, line:%d"
    #define SSL_TRC_1408A09D "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_clienthello_tlsext, file:%s, line:%d"
    #define SSL_TRC_1408A0B6 "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_ciphers_passed, file:%s, line:%d"
    #define SSL_TRC_1408A0C1 "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_shared_cipher, file:%s, line:%d"
    #define SSL_TRC_140F2044 "func: ssl_f_ssl3_send_server_hello, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1409B11A "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_error_generating_tmp_rsa_key, file:%s, line:%d"
    #define SSL_TRC_1409B0AC "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_missing_tmp_rsa_key, file:%s, line:%d"
    #define SSL_TRC_1409B0AB "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_missing_tmp_dh_key, file:%s, line:%d"
    #define SSL_TRC_1409B044 "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_1409B005 "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_1409B137 "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_missing_tmp_ecdh_key, file:%s, line:%d"
    #define SSL_TRC_1409B02B "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_ecdh_lib, file:%s, line:%d"
    #define SSL_TRC_1409B136 "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_ecgroup_too_large_for_cipher, file:%s, line:%d"
    #define SSL_TRC_1409B13B "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_unsupported_elliptic_curve, file:%s, line:%d"
    #define SSL_TRC_1409B041 "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1409B0FA "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_unknown_key_exchange_type, file:%s, line:%d"
    #define SSL_TRC_1409B007 "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_buf, file:%s, line:%d"
    #define SSL_TRC_1409B004 "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_rsa, file:%s, line:%d"
    #define SSL_TRC_1409B00A "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_dsa, file:%s, line:%d"
    #define SSL_TRC_1409B02A "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_ecdsa, file:%s, line:%d"
    #define SSL_TRC_1409B0FB "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_unknown_pkey_type, file:%s, line:%d"
    #define SSL_TRC_14096007 "func: ssl_f_ssl3_send_certificate_request, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_1408B0AD "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_tmp_rsa_pkey, file:%s, line:%d"
    #define SSL_TRC_1408B0A8 "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_rsa_certificate, file:%s, line:%d"
    #define SSL_TRC_1408B0EA "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_tls_rsa_encrypted_value_length_is_wrong, file:%s, line:%d"
    #define SSL_TRC_1408B076 "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d"
    #define SSL_TRC_1408B074 "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_bad_protocol_version_number, file:%s, line:%d"
    #define SSL_TRC_1408B094 "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_dh_public_value_length_is_wrong, file:%s, line:%d"
    #define SSL_TRC_1408B0EC "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_unable_to_decode_dh_certs, file:%s, line:%d"
    #define SSL_TRC_1408B0AB "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_tmp_dh_key, file:%s, line:%d"
    #define SSL_TRC_1408B082 "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_bn_lib, file:%s, line:%d"
    #define SSL_TRC_1408B005 "func: ssl_f_ssl3_get_client_key_exchange, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_1408B000 "func: ssl_f_ssl3_get_client_key_exchange, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_1408B137 "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_tmp_ecdh_key, file:%s, line:%d"
    #define SSL_TRC_140880AE "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_missing_verify_message, file:%s, line:%d"
    #define SSL_TRC_140880BA "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_no_client_cert_received, file:%s, line:%d"
    #define SSL_TRC_140880DC "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_signature_for_non_signing_certificate, file:%s, line:%d"
    #define SSL_TRC_14088085 "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_ccs_received_early, file:%s, line:%d"
    #define SSL_TRC_1408809F "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_14088109 "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_wrong_signature_size, file:%s, line:%d"
    #define SSL_TRC_14088076 "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d"
    #define SSL_TRC_1408807A "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_bad_rsa_signature, file:%s, line:%d"
    #define SSL_TRC_14088070 "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_bad_dsa_signature, file:%s, line:%d"
    #define SSL_TRC_14088044 "func: ssl_f_ssl3_get_cert_verify, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140890C7 "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_peer_did_not_return_a_certificate, file:%s, line:%d"
    #define SSL_TRC_140890E9 "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_tls_peer_did_not_respond_with_certificate_list, file:%s, line:%d"
    #define SSL_TRC_14089106 "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_wrong_message_type, file:%s, line:%d"
    #define SSL_TRC_14089041 "func: ssl_f_ssl3_get_client_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_1408909F "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_14089087 "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_cert_length_mismatch, file:%s, line:%d"
    #define SSL_TRC_1408900D "func: ssl_f_ssl3_get_client_certificate, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140890B0 "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_no_certificates_returned, file:%s, line:%d"
    #define SSL_TRC_140890B2 "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_no_certificate_returned, file:%s, line:%d"
    #define SSL_TRC_1409A044 "func: ssl_f_ssl3_send_server_certificate, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140670FE "func: ssl_f_d2i_ssl_session, reason:ssl_r_unknown_ssl_version, file:%s, line:%d"
    #define SSL_TRC_1406710F "func: ssl_f_d2i_ssl_session, reason:ssl_r_bad_length, file:%s, line:%d"
    #define SSL_TRC_140A2041 "func: ssl_f_ssl_cert_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140DD041 "func: ssl_f_ssl_cert_dup, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140DD005 "func: ssl_f_ssl_cert_dup, reason:err_r_dh_lib, file:%s, line:%d"
    #define SSL_TRC_140DD003 "func: ssl_f_ssl_cert_dup, reason:err_r_bn_lib, file:%s, line:%d"
    #define SSL_TRC_140DD010 "func: ssl_f_ssl_cert_dup, reason:err_r_ec_lib, file:%s, line:%d"
    #define SSL_TRC_140DD112 "func: ssl_f_ssl_cert_dup, reason:ssl_r_library_bug, file:%s, line:%d"
    #define SSL_TRC_140DE043 "func: ssl_f_ssl_cert_inst, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140DE041 "func: ssl_f_ssl_cert_inst, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140E1041 "func: ssl_f_ssl_sess_cert_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140CF00B "func: ssl_f_ssl_verify_cert_chain, reason:err_r_x509_lib, file:%s, line:%d"
    #define SSL_TRC_140CF0C2 "func: ssl_f_ssl_verify_cert_chain, reason:ssl_r_no_verify_callback, file:%s, line:%d"
    #define SSL_TRC_140B9041 "func: ssl_f_ssl_load_client_ca_file, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140D8041 "func: ssl_f_ssl_add_file_cert_subjects_to_stack, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140D710E "func: ssl_f_ssl_add_dir_cert_subjects_to_stack, reason:ssl_r_path_too_long, file:%s, line:%d"
    #define SSL_TRC_140D7002 "func: ssl_f_ssl_add_dir_cert_subjects_to_stack, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140E7041 "func: ssl_f_ssl_cipher_strength_sort, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140A6041 "func: ssl_f_ssl_create_cipher_list, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140A5133 "func: ssl_f_ssl_comp_add_compression_method, reason:ssl_r_compression_id_not_within_private_range, file:%s, line:%d"
    #define SSL_TRC_140A5135 "func: ssl_f_ssl_comp_add_compression_method, reason:ssl_r_duplicate_compression_id, file:%s, line:%d"
    #define SSL_TRC_140A5041 "func: ssl_f_ssl_comp_add_compression_method, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140A40BC "func: ssl_f_ssl_clear, reason:ssl_r_no_method_specified, file:%s, line:%d"
    #define SSL_TRC_140A4044 "func: ssl_f_ssl_clear, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140AA0E6 "func: ssl_f_ssl_ctx_set_ssl_version, reason:ssl_r_ssl_library_has_no_ciphers, file:%s, line:%d"
    #define SSL_TRC_140BA0C3 "func: ssl_f_ssl_new, reason:ssl_r_null_ssl_ctx, file:%s, line:%d"
    #define SSL_TRC_140BA0E4 "func: ssl_f_ssl_new, reason:ssl_r_ssl_ctx_has_no_default_ssl_version, file:%s, line:%d"
    #define SSL_TRC_140BA041 "func: ssl_f_ssl_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140DB111 "func: ssl_f_ssl_ctx_set_session_id_context, reason:ssl_r_ssl_session_id_context_too_long, file:%s, line:%d"
    #define SSL_TRC_140DA111 "func: ssl_f_ssl_set_session_id_context, reason:ssl_r_ssl_session_id_context_too_long, file:%s, line:%d"
    #define SSL_TRC_140C0007 "func: ssl_f_ssl_set_fd, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140C4007 "func: ssl_f_ssl_set_wfd, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140C2007 "func: ssl_f_ssl_set_rfd, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140A80B1 "func: ssl_f_ssl_ctx_check_private_key, reason:ssl_r_no_certificate_assigned, file:%s, line:%d"
    #define SSL_TRC_140A80BE "func: ssl_f_ssl_ctx_check_private_key, reason:ssl_r_no_private_key_assigned, file:%s, line:%d"
    #define SSL_TRC_140A3043 "func: ssl_f_ssl_check_private_key, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140A30B1 "func: ssl_f_ssl_check_private_key, reason:ssl_r_no_certificate_assigned, file:%s, line:%d"
    #define SSL_TRC_140A30BE "func: ssl_f_ssl_check_private_key, reason:ssl_r_no_private_key_assigned, file:%s, line:%d"
    #define SSL_TRC_140DF114 "func: ssl_f_ssl_read, reason:ssl_r_uninitialized, file:%s, line:%d"
    #define SSL_TRC_1410E114 "func: ssl_f_ssl_peek, reason:ssl_r_uninitialized, file:%s, line:%d"
    #define SSL_TRC_140D0114 "func: ssl_f_ssl_write, reason:ssl_r_uninitialized, file:%s, line:%d"
    #define SSL_TRC_140D00CF "func: ssl_f_ssl_write, reason:ssl_r_protocol_is_shutdown, file:%s, line:%d"
    #define SSL_TRC_140E0114 "func: ssl_f_ssl_shutdown, reason:ssl_r_uninitialized, file:%s, line:%d"
    #define SSL_TRC_1410D0B9 "func: ssl_f_ssl_ctx_set_cipher_list, reason:ssl_r_no_cipher_match, file:%s, line:%d"
    #define SSL_TRC_1410F0B9 "func: ssl_f_ssl_set_cipher_list, reason:ssl_r_no_cipher_match, file:%s, line:%d"
    #define SSL_TRC_140A1097 "func: ssl_f_ssl_bytes_to_cipher_list, reason:ssl_r_error_in_received_cipher_list, file:%s, line:%d"
    #define SSL_TRC_140A1041 "func: ssl_f_ssl_bytes_to_cipher_list, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140A90C4 "func: ssl_f_ssl_ctx_new, reason:ssl_r_null_ssl_method_passed, file:%s, line:%d"
    #define SSL_TRC_140A910D "func: ssl_f_ssl_ctx_new, reason:ssl_r_x509_verification_setup_problems, file:%s, line:%d"
    #define SSL_TRC_140A90A1 "func: ssl_f_ssl_ctx_new, reason:ssl_r_library_has_no_ciphers, file:%s, line:%d"
    #define SSL_TRC_140A90F1 "func: ssl_f_ssl_ctx_new, reason:ssl_r_unable_to_load_ssl2_md5_routines, file:%s, line:%d"
    #define SSL_TRC_140A90F2 "func: ssl_f_ssl_ctx_new, reason:ssl_r_unable_to_load_ssl3_md5_routines, file:%s, line:%d"
    #define SSL_TRC_140A90F3 "func: ssl_f_ssl_ctx_new, reason:ssl_r_unable_to_load_ssl3_sha1_routines, file:%s, line:%d"
    #define SSL_TRC_140A9041 "func: ssl_f_ssl_ctx_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140B6044 "func: ssl_f_ssl_get_server_send_cert, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140B7044 "func: ssl_f_ssl_get_sign_pkey, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140B4090 "func: ssl_f_ssl_do_handshake, reason:ssl_r_connection_type_not_set, file:%s, line:%d"
    #define SSL_TRC_140C5042 "func: ssl_f_ssl_undefined_function, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_140F4042 "func: ssl_f_ssl_undefined_void_function, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_140F3042 "func: ssl_f_ssl_undefined_const_function, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_140A0042 "func: ssl_f_ssl_bad_method, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define SSL_TRC_140B8007 "func: ssl_f_ssl_init_wbio_buffer, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140C6043 "func: ssl_f_ssl_use_certificate, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140C6041 "func: ssl_f_ssl_use_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140C8007 "func: ssl_f_ssl_use_certificate_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140C8002 "func: ssl_f_ssl_use_certificate_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140C807C "func: ssl_f_ssl_use_certificate_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d"
    #define SSL_TRC_140C8009 "func: ssl_f_ssl_use_certificate_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140C8000 "func: ssl_f_ssl_use_certificate_file, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_140C700D "func: ssl_f_ssl_use_certificate_asn1, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140CC043 "func: ssl_f_ssl_use_rsaprivatekey, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140CC041 "func: ssl_f_ssl_use_rsaprivatekey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140CC006 "func: ssl_f_ssl_use_rsaprivatekey, reason:err_r_evp_lib, file:%s, line:%d"
    #define SSL_TRC_140C10F7 "func: ssl_f_ssl_set_pkey, reason:ssl_r_unknown_certificate_type, file:%s, line:%d"
    #define SSL_TRC_140CE007 "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140CE002 "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140CE07C "func: ssl_f_ssl_use_rsaprivatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d"
    #define SSL_TRC_140CE009 "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140CE000 "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_140CD00D "func: ssl_f_ssl_use_rsaprivatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140C9043 "func: ssl_f_ssl_use_privatekey, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140C9041 "func: ssl_f_ssl_use_privatekey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140CB007 "func: ssl_f_ssl_use_privatekey_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140CB002 "func: ssl_f_ssl_use_privatekey_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140CB07C "func: ssl_f_ssl_use_privatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d"
    #define SSL_TRC_140CB009 "func: ssl_f_ssl_use_privatekey_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140CB000 "func: ssl_f_ssl_use_privatekey_file, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_140CA00D "func: ssl_f_ssl_use_privatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140AB043 "func: ssl_f_ssl_ctx_use_certificate, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140AB041 "func: ssl_f_ssl_ctx_use_certificate, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140BF10C "func: ssl_f_ssl_set_cert, reason:ssl_r_x509_lib, file:%s, line:%d"
    #define SSL_TRC_140BF0F7 "func: ssl_f_ssl_set_cert, reason:ssl_r_unknown_certificate_type, file:%s, line:%d"
    #define SSL_TRC_140AD007 "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140AD002 "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140AD07C "func: ssl_f_ssl_ctx_use_certificate_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d"
    #define SSL_TRC_140AD009 "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140AD000 "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_140AC00D "func: ssl_f_ssl_ctx_use_certificate_asn1, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140B1043 "func: ssl_f_ssl_ctx_use_rsaprivatekey, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140B1041 "func: ssl_f_ssl_ctx_use_rsaprivatekey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140B1006 "func: ssl_f_ssl_ctx_use_rsaprivatekey, reason:err_r_evp_lib, file:%s, line:%d"
    #define SSL_TRC_140B3007 "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140B3002 "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140B307C "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d"
    #define SSL_TRC_140B3009 "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140B3000 "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_140B200D "func: ssl_f_ssl_ctx_use_rsaprivatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140AE043 "func: ssl_f_ssl_ctx_use_privatekey, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define SSL_TRC_140AE041 "func: ssl_f_ssl_ctx_use_privatekey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140B0007 "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140B0002 "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140B007C "func: ssl_f_ssl_ctx_use_privatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d"
    #define SSL_TRC_140B0009 "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140B0000 "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_reason_none, file:%s, line:%d"
    #define SSL_TRC_140AF00D "func: ssl_f_ssl_ctx_use_privatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d"
    #define SSL_TRC_140DC007 "func: ssl_f_ssl_ctx_use_certificate_chain_file, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140DC002 "func: ssl_f_ssl_ctx_use_certificate_chain_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define SSL_TRC_140DC009 "func: ssl_f_ssl_ctx_use_certificate_chain_file, reason:err_r_pem_lib, file:%s, line:%d"
    #define SSL_TRC_140BD041 "func: ssl_f_ssl_session_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140B5103 "func: ssl_f_ssl_get_new_session, reason:ssl_r_unsupported_ssl_version, file:%s, line:%d"
    #define SSL_TRC_140B5044 "func: ssl_f_ssl_get_new_session, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140D9110 "func: ssl_f_ssl_get_prev_session, reason:ssl_r_attempt_to_reuse_session_in_different_context, file:%s, line:%d"
    #define SSL_TRC_140D9115 "func: ssl_f_ssl_get_prev_session, reason:ssl_r_session_id_context_uninitialized, file:%s, line:%d"
    #define SSL_TRC_140C30F0 "func: ssl_f_ssl_set_session, reason:ssl_r_unable_to_find_ssl_method, file:%s, line:%d"
    #define SSL_TRC_140BE007 "func: ssl_f_ssl_session_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define SSL_TRC_140D108E "func: ssl_f_tls1_change_cipher_state, reason:ssl_r_compression_library_error, file:%s, line:%d"
    #define SSL_TRC_140D1044 "func: ssl_f_tls1_change_cipher_state, reason:err_r_internal_error, file:%s, line:%d"
    #define SSL_TRC_140D1041 "func: ssl_f_tls1_change_cipher_state, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140D308A "func: ssl_f_tls1_setup_key_block, reason:ssl_r_cipher_or_hash_unavailable, file:%s, line:%d"
    #define SSL_TRC_140D3041 "func: ssl_f_tls1_setup_key_block, reason:err_r_malloc_failure, file:%s, line:%d"
    #define SSL_TRC_140D2081 "func: ssl_f_tls1_enc, reason:ssl_r_block_cipher_pad_is_wrong, file:%s, line:%d"
#else
BEGIN_TRACE_MAP(MOD_OSSL_SSL)

    TRC_MSG(SSL_TRC_140E8042, "func: ssl_f_ssl_ctrl, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140750DD, "func: ssl_f_ssl23_connect, reason:ssl_r_ssl23_doing_session_id_reuse, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140750FF, "func: ssl_f_ssl23_connect, reason:ssl_r_unknown_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140740BF, "func: ssl_f_ssl23_client_hello, reason:ssl_r_no_protocols_available, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140740B5, "func: ssl_f_ssl23_client_hello, reason:ssl_r_no_ciphers_available, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14074044, "func: ssl_f_ssl23_client_hello, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14077102, "func: ssl_f_ssl23_get_server_hello, reason:ssl_r_unsupported_protocol, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14077007, "func: ssl_f_ssl23_get_server_hello, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140770FC, "func: ssl_f_ssl23_get_server_hello, reason:ssl_r_unknown_protocol, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140780E5, "func: ssl_f_ssl23_read, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140ED0E5, "func: ssl_f_ssl23_peek, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140790E5, "func: ssl_f_ssl23_write, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140730FF, "func: ssl_f_ssl23_accept, reason:ssl_r_unknown_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407612A, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_record_too_small, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407609C, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_http_request, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407609B, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_https_proxy_request, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140760D6, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_record_too_large, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140760D5, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_record_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14076102, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_unsupported_protocol, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140760FC, "func: ssl_f_ssl23_get_client_hello, reason:ssl_r_unknown_protocol, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407B0FF, "func: ssl_f_ssl2_connect, reason:ssl_r_unknown_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D128, "func: ssl_f_get_server_hello, reason:ssl_r_message_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D0D8, "func: ssl_f_get_server_hello, reason:ssl_r_reuse_cert_length_not_zero, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D0D9, "func: ssl_f_get_server_hello, reason:ssl_r_reuse_cert_type_not_zero, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D0DA, "func: ssl_f_get_server_hello, reason:ssl_r_reuse_cipher_list_not_zero, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D0B8, "func: ssl_f_get_server_hello, reason:ssl_r_no_cipher_list, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D041, "func: ssl_f_get_server_hello, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D0B9, "func: ssl_f_get_server_hello, reason:ssl_r_no_cipher_match, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D044, "func: ssl_f_get_server_hello, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406D12B, "func: ssl_f_get_server_hello, reason:ssl_r_ssl2_connection_id_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140650B5, "func: ssl_f_client_hello, reason:ssl_r_no_ciphers_available, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140660CE, "func: ssl_f_client_master_key, reason:ssl_r_problems_mapping_cipher_functions, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14066044, "func: ssl_f_client_master_key, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406608B, "func: ssl_f_client_master_key, reason:ssl_r_cipher_table_src_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140660D0, "func: ssl_f_client_master_key, reason:ssl_r_public_key_encrypt_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A7044, "func: ssl_f_client_finished, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14064066, "func: ssl_f_client_certificate, reason:ssl_r_bad_authentication_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406406A, "func: ssl_f_client_certificate, reason:ssl_r_bad_data_returned_by_callback, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406E0C8, "func: ssl_f_get_server_verify, reason:ssl_r_peer_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406E088, "func: ssl_f_get_server_verify, reason:ssl_r_challenge_is_different, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406C0D4, "func: ssl_f_get_server_finished, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406C0C8, "func: ssl_f_get_server_finished, reason:ssl_r_peer_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406C0E7, "func: ssl_f_get_server_finished, reason:ssl_r_ssl_session_id_is_different, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407E00B, "func: ssl_f_ssl2_set_certificate, reason:err_r_x509_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407E041, "func: ssl_f_ssl2_set_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407E086, "func: ssl_f_ssl2_set_certificate, reason:ssl_r_certificate_verify_failed, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407E0ED, "func: ssl_f_ssl2_set_certificate, reason:ssl_r_unable_to_extract_public_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407E0D2, "func: ssl_f_ssl2_set_certificate, reason:ssl_r_public_key_not_rsa, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BC0C0, "func: ssl_f_ssl_rsa_public_encrypt, reason:ssl_r_no_publickey, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BC0D1, "func: ssl_f_ssl_rsa_public_encrypt, reason:ssl_r_public_key_is_not_rsa, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BC004, "func: ssl_f_ssl_rsa_public_encrypt, reason:err_r_rsa_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407C0CE, "func: ssl_f_ssl2_enc_init, reason:ssl_r_problems_mapping_cipher_functions, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407C041, "func: ssl_f_ssl2_enc_init, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140F1044, "func: ssl_f_ssl2_generate_key_material, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EC0E5, "func: ssl_f_ssl2_read_internal, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EC0AF, "func: ssl_f_ssl2_read_internal, reason:ssl_r_non_sslv2_initial_packet, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EC11B, "func: ssl_f_ssl2_read_internal, reason:ssl_r_illegal_padding, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EC071, "func: ssl_f_ssl2_read_internal, reason:ssl_r_bad_mac_decode, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EC07E, "func: ssl_f_ssl2_read_internal, reason:ssl_r_bad_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140700D3, "func: ssl_f_read_n, reason:ssl_r_read_bio_not_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407F0E5, "func: ssl_f_ssl2_write, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D407F, "func: ssl_f_write_pending, reason:ssl_r_bad_write_retry, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D4104, "func: ssl_f_write_pending, reason:ssl_r_write_bio_not_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407A0B3, "func: ssl_f_ssl2_accept, reason:ssl_r_no_certificate_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407A0FF, "func: ssl_f_ssl2_accept, reason:ssl_r_unknown_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B0D4, "func: ssl_f_get_client_master_key, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B0C8, "func: ssl_f_get_client_master_key, reason:ssl_r_peer_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B0B9, "func: ssl_f_get_client_master_key, reason:ssl_r_no_cipher_match, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B11C, "func: ssl_f_get_client_master_key, reason:ssl_r_key_arg_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B044, "func: ssl_f_get_client_master_key, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B128, "func: ssl_f_get_client_master_key, reason:ssl_r_message_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B0BD, "func: ssl_f_get_client_master_key, reason:ssl_r_no_privatekey, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B0CE, "func: ssl_f_get_client_master_key, reason:ssl_r_problems_mapping_cipher_functions, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B076, "func: ssl_f_get_client_master_key, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406B107, "func: ssl_f_get_client_master_key, reason:ssl_r_wrong_number_of_key_bits, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A0D4, "func: ssl_f_get_client_hello, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A0C8, "func: ssl_f_get_client_hello, reason:ssl_r_peer_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A09E, "func: ssl_f_get_client_hello, reason:ssl_r_invalid_challenge_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A128, "func: ssl_f_get_client_hello, reason:ssl_r_message_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A07D, "func: ssl_f_get_client_hello, reason:ssl_r_bad_ssl_session_id_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A0B3, "func: ssl_f_get_client_hello, reason:ssl_r_no_certificate_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A044, "func: ssl_f_get_client_hello, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406A041, "func: ssl_f_get_client_hello, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14072041, "func: ssl_f_server_hello, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140720B4, "func: ssl_f_server_hello, reason:ssl_r_no_certificate_specified, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140690D4, "func: ssl_f_get_client_finished, reason:ssl_r_read_wrong_packet_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140690C8, "func: ssl_f_get_client_finished, reason:ssl_r_peer_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14069044, "func: ssl_f_get_client_finished, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406908F, "func: ssl_f_get_client_finished, reason:ssl_r_connection_id_is_different, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140F0044, "func: ssl_f_server_verify, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EF044, "func: ssl_f_server_finish, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140710C7, "func: ssl_f_request_certificate, reason:ssl_r_peer_did_not_return_a_certificate, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140710DB, "func: ssl_f_request_certificate, reason:ssl_r_short_read, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14071044, "func: ssl_f_request_certificate, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14071075, "func: ssl_f_request_certificate, reason:ssl_r_bad_response_argument, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14071128, "func: ssl_f_request_certificate, reason:ssl_r_message_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1407100B, "func: ssl_f_request_certificate, reason:err_r_x509_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14071041, "func: ssl_f_request_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14071068, "func: ssl_f_request_certificate, reason:ssl_r_bad_checksum, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BB0BD, "func: ssl_f_ssl_rsa_private_decrypt, reason:ssl_r_no_privatekey, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BB0D1, "func: ssl_f_ssl_rsa_private_decrypt, reason:ssl_r_public_key_is_not_rsa, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BB004, "func: ssl_f_ssl_rsa_private_decrypt, reason:err_r_rsa_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408C09A, "func: ssl_f_ssl3_get_finished, reason:ssl_r_got_a_fin_before_a_ccs, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408C06F, "func: ssl_f_ssl3_get_finished, reason:ssl_r_bad_digest_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408C095, "func: ssl_f_ssl3_get_finished, reason:ssl_r_digest_check_failed, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14093007, "func: ssl_f_ssl3_output_cert_chain, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409300B, "func: ssl_f_ssl3_output_cert_chain, reason:err_r_x509_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408E0F4, "func: ssl_f_ssl3_get_message, reason:ssl_r_unexpected_message, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408E098, "func: ssl_f_ssl3_get_message, reason:ssl_r_excessive_message_size, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408E007, "func: ssl_f_ssl3_get_message, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409C041, "func: ssl_f_ssl3_setup_buffers, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14084044, "func: ssl_f_ssl3_connect, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140840FF, "func: ssl_f_ssl3_connect, reason:ssl_r_unknown_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14083044, "func: ssl_f_ssl3_client_hello, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140830B5, "func: ssl_f_ssl3_client_hello, reason:ssl_r_no_ciphers_available, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14092072, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_bad_message_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409210A, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_wrong_ssl_version, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409212C, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_ssl3_session_id_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14092110, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_attempt_to_reuse_session_in_different_context, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140920F8, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_unknown_cipher_returned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14092105, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_wrong_cipher_returned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140920C5, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_old_session_cipher_not_returned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14092101, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_unsupported_compression_algorithm, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140920DF, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_parse_tlsext, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140920E0, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_serverhello_tlsext, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14092073, "func: ssl_f_ssl3_get_server_hello, reason:ssl_r_bad_packet_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14090072, "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_bad_message_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14090041, "func: ssl_f_ssl3_get_server_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409009F, "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14090087, "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_cert_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409000D, "func: ssl_f_ssl3_get_server_certificate, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14090086, "func: ssl_f_ssl3_get_server_certificate, reason:ssl_r_certificate_verify_failed, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D041, "func: ssl_f_ssl3_get_key_exchange, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D079, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_rsa_modulus_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D003, "func: ssl_f_ssl3_get_key_exchange, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D078, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_rsa_e_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D044, "func: ssl_f_ssl3_get_key_exchange, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D005, "func: ssl_f_ssl3_get_key_exchange, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D06E, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_dh_p_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D06C, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_dh_g_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D06D, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_dh_pub_key_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D0EB, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_tried_to_use_unsupported_cipher, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D13A, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_unable_to_find_ecdh_parameters, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D010, "func: ssl_f_ssl3_get_key_exchange, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D136, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_ecgroup_too_large_for_cipher, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D132, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_ecpoint, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D0F4, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_unexpected_message, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D108, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_wrong_signature_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D076, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D07B, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_bad_signature, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408D099, "func: ssl_f_ssl3_get_key_exchange, reason:ssl_r_extra_data_in_message, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14087106, "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_wrong_message_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140870E8, "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_tls_client_cert_req_with_anon_cipher, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14087041, "func: ssl_f_ssl3_get_certificate_request, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408709F, "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14087084, "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_ca_dn_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408700D, "func: ssl_f_ssl3_get_certificate_request, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14087083, "func: ssl_f_ssl3_get_certificate_request, reason:ssl_r_ca_dn_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1411B072, "func: ssl_f_ssl3_get_new_session_ticket, reason:ssl_r_bad_message_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1411B09F, "func: ssl_f_ssl3_get_new_session_ticket, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1411C09F, "func: ssl_f_ssl3_new_session_ticket, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1411C041, "func: ssl_f_ssl3_new_session_ticket, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409109F, "func: ssl_f_ssl3_get_server_done, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14098044, "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14098077, "func: ssl_f_ssl3_send_client_key_exchange, reason:ssl_r_bad_rsa_encrypt, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140980EE, "func: ssl_f_ssl3_send_client_key_exchange, reason:ssl_r_unable_to_find_dh_parameters, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14098005, "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14098041, "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14098010, "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409802B, "func: ssl_f_ssl3_send_client_key_exchange, reason:err_r_ecdh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14099004, "func: ssl_f_ssl3_send_client_verify, reason:err_r_rsa_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409900A, "func: ssl_f_ssl3_send_client_verify, reason:err_r_dsa_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14099044, "func: ssl_f_ssl3_send_client_verify, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409706A, "func: ssl_f_ssl3_send_client_certificate, reason:ssl_r_bad_data_returned_by_callback, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14082044, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14082130, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_bad_ecc_cert, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820AA, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_rsa_signing_cert, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A5, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dsa_signing_cert, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A9, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_rsa_encrypting_cert, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A3, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dh_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A4, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dh_rsa_cert, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A2, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_dh_dsa_cert, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A7, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_export_tmp_rsa_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820A6, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_missing_export_tmp_dh_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140820FA, "func: ssl_f_ssl3_check_cert_and_algorithm, reason:ssl_r_unknown_key_exchange_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140EE044, "func: ssl_f_ssl3_generate_key_block, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408108E, "func: ssl_f_ssl3_change_cipher_state, reason:ssl_r_compression_library_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14081044, "func: ssl_f_ssl3_change_cipher_state, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14081041, "func: ssl_f_ssl3_change_cipher_state, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409D08A, "func: ssl_f_ssl3_setup_key_block, reason:ssl_r_cipher_or_hash_unavailable, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409D041, "func: ssl_f_ssl3_setup_key_block, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14086081, "func: ssl_f_ssl3_enc, reason:ssl_r_block_cipher_pad_is_wrong, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D5041, "func: ssl_f_ssl3_ctrl, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D5043, "func: ssl_f_ssl3_ctrl, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D5004, "func: ssl_f_ssl3_ctrl, reason:err_r_rsa_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D5042, "func: ssl_f_ssl3_ctrl, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D5005, "func: ssl_f_ssl3_ctrl, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D502B, "func: ssl_f_ssl3_ctrl, reason:err_r_ecdh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D50E1, "func: ssl_f_ssl3_ctrl, reason:ssl_r_ssl3_ext_invalid_servername, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D5044, "func: ssl_f_ssl3_ctrl, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D50E2, "func: ssl_f_ssl3_ctrl, reason:ssl_r_ssl3_ext_invalid_servername_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140E9041, "func: ssl_f_ssl3_callback_ctrl, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14085004, "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_rsa_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14085042, "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14085005, "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408502B, "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_ecdh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14085010, "func: ssl_f_ssl3_ctx_ctrl, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14085113, "func: ssl_f_ssl3_ctx_ctrl, reason:ssl_r_invalid_ticket_keys_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14095044, "func: ssl_f_ssl3_read_n, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140950D3, "func: ssl_f_ssl3_read_n, reason:ssl_r_read_bio_not_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F044, "func: ssl_f_ssl3_get_record, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F10B, "func: ssl_f_ssl3_get_record, reason:ssl_r_wrong_version_number, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F0C6, "func: ssl_f_ssl3_get_record, reason:ssl_r_packet_length_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F096, "func: ssl_f_ssl3_get_record, reason:ssl_r_encrypted_length_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F0CD, "func: ssl_f_ssl3_get_record, reason:ssl_r_pre_mac_length_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F0A0, "func: ssl_f_ssl3_get_record, reason:ssl_r_length_too_short, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F119, "func: ssl_f_ssl3_get_record, reason:ssl_r_decryption_failed_or_bad_record_mac, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F08C, "func: ssl_f_ssl3_get_record, reason:ssl_r_compressed_length_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F06B, "func: ssl_f_ssl3_get_record, reason:ssl_r_bad_decompression, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408F092, "func: ssl_f_ssl3_get_record, reason:ssl_r_data_length_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409E0E5, "func: ssl_f_ssl3_write_bytes, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14068044, "func: ssl_f_do_ssl3_write, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406808D, "func: ssl_f_do_ssl3_write, reason:ssl_r_compression_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409F07F, "func: ssl_f_ssl3_write_pending, reason:ssl_r_bad_write_retry, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409F080, "func: ssl_f_ssl3_write_pending, reason:ssl_r_bio_not_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14094044, "func: ssl_f_ssl3_read_bytes, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140940E5, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_ssl_handshake_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14094091, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_data_between_ccs_and_finished, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14094064, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_app_data_in_handshake, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14094069, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_bad_hello_request, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140940F6, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_unknown_alert_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14094067, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_bad_change_cipher_spec, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14094085, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_ccs_received_early, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140940F5, "func: ssl_f_ssl3_read_bytes, reason:ssl_r_unexpected_record, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140800B3, "func: ssl_f_ssl3_accept, reason:ssl_r_no_certificate_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14080044, "func: ssl_f_ssl3_accept, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140800FF, "func: ssl_f_ssl3_accept, reason:ssl_r_unknown_state, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A10B, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_wrong_version_number, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A134, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_cookie_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A0B7, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_ciphers_specified, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A09F, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A0D7, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_required_cipher_missing, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A0BB, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_compression_specified, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A0DF, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_parse_tlsext, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A09D, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_clienthello_tlsext, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A0B6, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_ciphers_passed, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408A0C1, "func: ssl_f_ssl3_get_client_hello, reason:ssl_r_no_shared_cipher, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140F2044, "func: ssl_f_ssl3_send_server_hello, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B11A, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_error_generating_tmp_rsa_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B0AC, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_missing_tmp_rsa_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B0AB, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_missing_tmp_dh_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B044, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B005, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B137, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_missing_tmp_ecdh_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B02B, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_ecdh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B136, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_ecgroup_too_large_for_cipher, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B13B, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_unsupported_elliptic_curve, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B041, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B0FA, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_unknown_key_exchange_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B007, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_buf, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B004, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_rsa, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B00A, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_dsa, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B02A, "func: ssl_f_ssl3_send_server_key_exchange, reason:err_lib_ecdsa, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409B0FB, "func: ssl_f_ssl3_send_server_key_exchange, reason:ssl_r_unknown_pkey_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14096007, "func: ssl_f_ssl3_send_certificate_request, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B0AD, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_tmp_rsa_pkey, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B0A8, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_rsa_certificate, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B0EA, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_tls_rsa_encrypted_value_length_is_wrong, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B076, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B074, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_bad_protocol_version_number, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B094, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_dh_public_value_length_is_wrong, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B0EC, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_unable_to_decode_dh_certs, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B0AB, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_tmp_dh_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B082, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_bn_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B005, "func: ssl_f_ssl3_get_client_key_exchange, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B000, "func: ssl_f_ssl3_get_client_key_exchange, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408B137, "func: ssl_f_ssl3_get_client_key_exchange, reason:ssl_r_missing_tmp_ecdh_key, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140880AE, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_missing_verify_message, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140880BA, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_no_client_cert_received, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140880DC, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_signature_for_non_signing_certificate, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14088085, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_ccs_received_early, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408809F, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14088109, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_wrong_signature_size, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14088076, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_bad_rsa_decrypt, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408807A, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_bad_rsa_signature, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14088070, "func: ssl_f_ssl3_get_cert_verify, reason:ssl_r_bad_dsa_signature, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14088044, "func: ssl_f_ssl3_get_cert_verify, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140890C7, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_peer_did_not_return_a_certificate, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140890E9, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_tls_peer_did_not_respond_with_certificate_list, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14089106, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_wrong_message_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14089041, "func: ssl_f_ssl3_get_client_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408909F, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_14089087, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_cert_length_mismatch, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1408900D, "func: ssl_f_ssl3_get_client_certificate, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140890B0, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_no_certificates_returned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140890B2, "func: ssl_f_ssl3_get_client_certificate, reason:ssl_r_no_certificate_returned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1409A044, "func: ssl_f_ssl3_send_server_certificate, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140670FE, "func: ssl_f_d2i_ssl_session, reason:ssl_r_unknown_ssl_version, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1406710F, "func: ssl_f_d2i_ssl_session, reason:ssl_r_bad_length, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A2041, "func: ssl_f_ssl_cert_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DD041, "func: ssl_f_ssl_cert_dup, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DD005, "func: ssl_f_ssl_cert_dup, reason:err_r_dh_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DD003, "func: ssl_f_ssl_cert_dup, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DD010, "func: ssl_f_ssl_cert_dup, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DD112, "func: ssl_f_ssl_cert_dup, reason:ssl_r_library_bug, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DE043, "func: ssl_f_ssl_cert_inst, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DE041, "func: ssl_f_ssl_cert_inst, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140E1041, "func: ssl_f_ssl_sess_cert_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CF00B, "func: ssl_f_ssl_verify_cert_chain, reason:err_r_x509_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CF0C2, "func: ssl_f_ssl_verify_cert_chain, reason:ssl_r_no_verify_callback, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B9041, "func: ssl_f_ssl_load_client_ca_file, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D8041, "func: ssl_f_ssl_add_file_cert_subjects_to_stack, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D710E, "func: ssl_f_ssl_add_dir_cert_subjects_to_stack, reason:ssl_r_path_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D7002, "func: ssl_f_ssl_add_dir_cert_subjects_to_stack, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140E7041, "func: ssl_f_ssl_cipher_strength_sort, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A6041, "func: ssl_f_ssl_create_cipher_list, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A5133, "func: ssl_f_ssl_comp_add_compression_method, reason:ssl_r_compression_id_not_within_private_range, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A5135, "func: ssl_f_ssl_comp_add_compression_method, reason:ssl_r_duplicate_compression_id, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A5041, "func: ssl_f_ssl_comp_add_compression_method, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A40BC, "func: ssl_f_ssl_clear, reason:ssl_r_no_method_specified, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A4044, "func: ssl_f_ssl_clear, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AA0E6, "func: ssl_f_ssl_ctx_set_ssl_version, reason:ssl_r_ssl_library_has_no_ciphers, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BA0C3, "func: ssl_f_ssl_new, reason:ssl_r_null_ssl_ctx, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BA0E4, "func: ssl_f_ssl_new, reason:ssl_r_ssl_ctx_has_no_default_ssl_version, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BA041, "func: ssl_f_ssl_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DB111, "func: ssl_f_ssl_ctx_set_session_id_context, reason:ssl_r_ssl_session_id_context_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DA111, "func: ssl_f_ssl_set_session_id_context, reason:ssl_r_ssl_session_id_context_too_long, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C0007, "func: ssl_f_ssl_set_fd, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C4007, "func: ssl_f_ssl_set_wfd, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C2007, "func: ssl_f_ssl_set_rfd, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A80B1, "func: ssl_f_ssl_ctx_check_private_key, reason:ssl_r_no_certificate_assigned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A80BE, "func: ssl_f_ssl_ctx_check_private_key, reason:ssl_r_no_private_key_assigned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A3043, "func: ssl_f_ssl_check_private_key, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A30B1, "func: ssl_f_ssl_check_private_key, reason:ssl_r_no_certificate_assigned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A30BE, "func: ssl_f_ssl_check_private_key, reason:ssl_r_no_private_key_assigned, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DF114, "func: ssl_f_ssl_read, reason:ssl_r_uninitialized, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1410E114, "func: ssl_f_ssl_peek, reason:ssl_r_uninitialized, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D0114, "func: ssl_f_ssl_write, reason:ssl_r_uninitialized, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D00CF, "func: ssl_f_ssl_write, reason:ssl_r_protocol_is_shutdown, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140E0114, "func: ssl_f_ssl_shutdown, reason:ssl_r_uninitialized, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1410D0B9, "func: ssl_f_ssl_ctx_set_cipher_list, reason:ssl_r_no_cipher_match, file:%s, line:%d")
    TRC_MSG(SSL_TRC_1410F0B9, "func: ssl_f_ssl_set_cipher_list, reason:ssl_r_no_cipher_match, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A1097, "func: ssl_f_ssl_bytes_to_cipher_list, reason:ssl_r_error_in_received_cipher_list, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A1041, "func: ssl_f_ssl_bytes_to_cipher_list, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A90C4, "func: ssl_f_ssl_ctx_new, reason:ssl_r_null_ssl_method_passed, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A910D, "func: ssl_f_ssl_ctx_new, reason:ssl_r_x509_verification_setup_problems, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A90A1, "func: ssl_f_ssl_ctx_new, reason:ssl_r_library_has_no_ciphers, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A90F1, "func: ssl_f_ssl_ctx_new, reason:ssl_r_unable_to_load_ssl2_md5_routines, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A90F2, "func: ssl_f_ssl_ctx_new, reason:ssl_r_unable_to_load_ssl3_md5_routines, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A90F3, "func: ssl_f_ssl_ctx_new, reason:ssl_r_unable_to_load_ssl3_sha1_routines, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A9041, "func: ssl_f_ssl_ctx_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B6044, "func: ssl_f_ssl_get_server_send_cert, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B7044, "func: ssl_f_ssl_get_sign_pkey, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B4090, "func: ssl_f_ssl_do_handshake, reason:ssl_r_connection_type_not_set, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C5042, "func: ssl_f_ssl_undefined_function, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140F4042, "func: ssl_f_ssl_undefined_void_function, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140F3042, "func: ssl_f_ssl_undefined_const_function, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140A0042, "func: ssl_f_ssl_bad_method, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B8007, "func: ssl_f_ssl_init_wbio_buffer, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C6043, "func: ssl_f_ssl_use_certificate, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C6041, "func: ssl_f_ssl_use_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C8007, "func: ssl_f_ssl_use_certificate_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C8002, "func: ssl_f_ssl_use_certificate_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C807C, "func: ssl_f_ssl_use_certificate_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C8009, "func: ssl_f_ssl_use_certificate_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C8000, "func: ssl_f_ssl_use_certificate_file, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C700D, "func: ssl_f_ssl_use_certificate_asn1, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CC043, "func: ssl_f_ssl_use_rsaprivatekey, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CC041, "func: ssl_f_ssl_use_rsaprivatekey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CC006, "func: ssl_f_ssl_use_rsaprivatekey, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C10F7, "func: ssl_f_ssl_set_pkey, reason:ssl_r_unknown_certificate_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CE007, "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CE002, "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CE07C, "func: ssl_f_ssl_use_rsaprivatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CE009, "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CE000, "func: ssl_f_ssl_use_rsaprivatekey_file, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CD00D, "func: ssl_f_ssl_use_rsaprivatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C9043, "func: ssl_f_ssl_use_privatekey, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C9041, "func: ssl_f_ssl_use_privatekey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CB007, "func: ssl_f_ssl_use_privatekey_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CB002, "func: ssl_f_ssl_use_privatekey_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CB07C, "func: ssl_f_ssl_use_privatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CB009, "func: ssl_f_ssl_use_privatekey_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CB000, "func: ssl_f_ssl_use_privatekey_file, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140CA00D, "func: ssl_f_ssl_use_privatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AB043, "func: ssl_f_ssl_ctx_use_certificate, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AB041, "func: ssl_f_ssl_ctx_use_certificate, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BF10C, "func: ssl_f_ssl_set_cert, reason:ssl_r_x509_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BF0F7, "func: ssl_f_ssl_set_cert, reason:ssl_r_unknown_certificate_type, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AD007, "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AD002, "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AD07C, "func: ssl_f_ssl_ctx_use_certificate_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AD009, "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AD000, "func: ssl_f_ssl_ctx_use_certificate_file, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AC00D, "func: ssl_f_ssl_ctx_use_certificate_asn1, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B1043, "func: ssl_f_ssl_ctx_use_rsaprivatekey, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B1041, "func: ssl_f_ssl_ctx_use_rsaprivatekey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B1006, "func: ssl_f_ssl_ctx_use_rsaprivatekey, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B3007, "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B3002, "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B307C, "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B3009, "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B3000, "func: ssl_f_ssl_ctx_use_rsaprivatekey_file, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B200D, "func: ssl_f_ssl_ctx_use_rsaprivatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AE043, "func: ssl_f_ssl_ctx_use_privatekey, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AE041, "func: ssl_f_ssl_ctx_use_privatekey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B0007, "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B0002, "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B007C, "func: ssl_f_ssl_ctx_use_privatekey_file, reason:ssl_r_bad_ssl_filetype, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B0009, "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B0000, "func: ssl_f_ssl_ctx_use_privatekey_file, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140AF00D, "func: ssl_f_ssl_ctx_use_privatekey_asn1, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DC007, "func: ssl_f_ssl_ctx_use_certificate_chain_file, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DC002, "func: ssl_f_ssl_ctx_use_certificate_chain_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140DC009, "func: ssl_f_ssl_ctx_use_certificate_chain_file, reason:err_r_pem_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BD041, "func: ssl_f_ssl_session_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B5103, "func: ssl_f_ssl_get_new_session, reason:ssl_r_unsupported_ssl_version, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140B5044, "func: ssl_f_ssl_get_new_session, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D9110, "func: ssl_f_ssl_get_prev_session, reason:ssl_r_attempt_to_reuse_session_in_different_context, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D9115, "func: ssl_f_ssl_get_prev_session, reason:ssl_r_session_id_context_uninitialized, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140C30F0, "func: ssl_f_ssl_set_session, reason:ssl_r_unable_to_find_ssl_method, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140BE007, "func: ssl_f_ssl_session_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D108E, "func: ssl_f_tls1_change_cipher_state, reason:ssl_r_compression_library_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D1044, "func: ssl_f_tls1_change_cipher_state, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D1041, "func: ssl_f_tls1_change_cipher_state, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D308A, "func: ssl_f_tls1_setup_key_block, reason:ssl_r_cipher_or_hash_unavailable, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D3041, "func: ssl_f_tls1_setup_key_block, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(SSL_TRC_140D2081, "func: ssl_f_tls1_enc, reason:ssl_r_block_cipher_pad_is_wrong, file:%s, line:%d")

END_TRACE_MAP(MOD_OSSL_SSL)

#endif /* __AGPS_SWIP_REL__*/

#endif /* !_SSL_TRC_H_ */
