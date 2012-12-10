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
 *   crypto_trc.h
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


#ifndef _CRYPTO_TRC_H_
#define _CRYPTO_TRC_H_


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
    #define CRYPTO_TRC_0F065041 "func: crypto_f_crypto_get_new_lockid, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F067064 "func: crypto_f_crypto_get_new_dynlockid, reason:crypto_r_no_dynlock_create_callback, file:%s, line:%d"
    #define CRYPTO_TRC_0F067041 "func: crypto_f_crypto_get_new_dynlockid, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F069041 "func: crypto_f_def_get_class, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F068041 "func: crypto_f_def_add_index, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F06C041 "func: crypto_f_int_new_ex_data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F06A041 "func: crypto_f_int_dup_ex_data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F06B041 "func: crypto_f_int_free_ex_data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0F066041 "func: crypto_f_crypto_set_ex_data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B20C0 "func: asn1_f_asn1_generate_v3, reason:asn1_r_sequence_or_set_needs_config, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B10C2 "func: asn1_f_asn1_cb, reason:asn1_r_unknown_tag, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B10BD "func: asn1_f_asn1_cb, reason:asn1_r_missing_value, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B10B5 "func: asn1_f_asn1_cb, reason:asn1_r_illegal_nested_tagging, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B10C3 "func: asn1_f_asn1_cb, reason:asn1_r_unkown_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B60BB "func: asn1_f_parse_tagging, reason:asn1_r_invalid_number, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B60BA "func: asn1_f_parse_tagging, reason:asn1_r_invalid_modifier, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B00B3 "func: asn1_f_append_exp, reason:asn1_r_illegal_implicit_tag, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B00AE "func: asn1_f_append_exp, reason:asn1_r_depth_exceeded, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B3041 "func: asn1_f_asn1_str2type, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B6 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_null_value, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30BE "func: asn1_f_asn1_str2type, reason:asn1_r_not_ascii_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B0 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_boolean, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B9 "func: asn1_f_asn1_str2type, reason:asn1_r_integer_not_ascii_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B4 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_integer, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30BF "func: asn1_f_asn1_str2type, reason:asn1_r_object_not_ascii_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B7 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_object, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30C1 "func: asn1_f_asn1_str2type, reason:asn1_r_time_not_ascii_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B8 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_time_value, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B1 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30B2 "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_hex, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30BC "func: asn1_f_asn1_str2type, reason:asn1_r_list_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30AF "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_bitstring_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B30C4 "func: asn1_f_asn1_str2type, reason:asn1_r_unsupported_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B40BB "func: asn1_f_bitstr_cb, reason:asn1_r_invalid_number, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B4041 "func: asn1_f_bitstr_cb, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D07209B "func: asn1_f_asn1_get_object, reason:asn1_r_too_long, file:%s, line:%d"
    #define CRYPTO_TRC_0D07207B "func: asn1_f_asn1_get_object, reason:asn1_r_header_too_long, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BA041 "func: asn1_f_asn1_string_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D082041 "func: asn1_f_asn1_string_type_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0AE0AC "func: asn1_f_oid_module_init, reason:asn1_r_error_loading_section, file:%s, line:%d"
    #define CRYPTO_TRC_0D0AE0AB "func: asn1_f_oid_module_init, reason:asn1_r_adding_object, file:%s, line:%d"
    #define CRYPTO_TRC_0D07F06E "func: asn1_f_asn1_seq_unpack, reason:asn1_r_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D07E070 "func: asn1_f_asn1_seq_pack, reason:asn1_r_encode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D07E041 "func: asn1_f_asn1_seq_pack, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D08806E "func: asn1_f_asn1_unpack_string, reason:asn1_r_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D07C041 "func: asn1_f_asn1_pack_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D07C070 "func: asn1_f_asn1_pack_string, reason:asn1_r_encode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C6041 "func: asn1_f_asn1_item_pack, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C6070 "func: asn1_f_asn1_item_pack, reason:asn1_r_encode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C706E "func: asn1_f_asn1_item_unpack, reason:asn1_r_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BD041 "func: asn1_f_c2i_asn1_bit_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BD000 "func: asn1_f_c2i_asn1_bit_string, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B7041 "func: asn1_f_asn1_bit_string_set_bit, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D08E000 "func: asn1_f_d2i_asn1_boolean, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D095041 "func: asn1_f_d2i_asn1_type_bytes, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D095000 "func: asn1_f_d2i_asn1_type_bytes, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D08F041 "func: asn1_f_d2i_asn1_bytes, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D08F000 "func: asn1_f_d2i_asn1_bytes, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D069043 "func: asn1_f_asn1_collate_primitive, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define CRYPTO_TRC_0D069007 "func: asn1_f_asn1_collate_primitive, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D06903F "func: asn1_f_asn1_collate_primitive, reason:err_r_missing_asn1_eos, file:%s, line:%d"
    #define CRYPTO_TRC_0D06D007 "func: asn1_f_asn1_d2i_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D0CE007 "func: asn1_f_asn1_item_d2i_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D06B041 "func: asn1_f_asn1_d2i_read_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D06B08E "func: asn1_f_asn1_d2i_read_bio, reason:asn1_r_not_enough_data, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B8041 "func: asn1_f_asn1_digest, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D06F041 "func: asn1_f_asn1_dup, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BF041 "func: asn1_f_asn1_item_dup, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D070041 "func: asn1_f_asn1_enumerated_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D08A03A "func: asn1_f_bn_to_asn1_enumerated, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D08A041 "func: asn1_f_bn_to_asn1_enumerated, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D071069 "func: asn1_f_asn1_enumerated_to_bn, reason:asn1_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D09003A "func: asn1_f_d2i_asn1_generalizedtime, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D090084 "func: asn1_f_d2i_asn1_generalizedtime, reason:asn1_r_invalid_time_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D075007 "func: asn1_f_asn1_i2d_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D074041 "func: asn1_f_asn1_i2d_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C1007 "func: asn1_f_asn1_item_i2d_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C0041 "func: asn1_f_asn1_item_i2d_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C2041 "func: asn1_f_c2i_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C2000 "func: asn1_f_c2i_asn1_integer, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D096041 "func: asn1_f_d2i_asn1_uinteger, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D096000 "func: asn1_f_d2i_asn1_uinteger, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D076041 "func: asn1_f_asn1_integer_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D08B03A "func: asn1_f_bn_to_asn1_integer, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D08B041 "func: asn1_f_bn_to_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D077069 "func: asn1_f_asn1_integer_to_bn, reason:asn1_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D07A0A0 "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_unknown_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D07A098 "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_string_too_short, file:%s, line:%d"
    #define CRYPTO_TRC_0D07A097 "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_string_too_long, file:%s, line:%d"
    #define CRYPTO_TRC_0D07A07C "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_illegal_characters, file:%s, line:%d"
    #define CRYPTO_TRC_0D07A041 "func: asn1_f_asn1_mbstring_ncopy, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D06407A "func: asn1_f_a2d_asn1_object, reason:asn1_r_first_num_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0D06408A "func: asn1_f_a2d_asn1_object, reason:asn1_r_missing_second_number, file:%s, line:%d"
    #define CRYPTO_TRC_0D064083 "func: asn1_f_a2d_asn1_object, reason:asn1_r_invalid_separator, file:%s, line:%d"
    #define CRYPTO_TRC_0D064082 "func: asn1_f_a2d_asn1_object, reason:asn1_r_invalid_digit, file:%s, line:%d"
    #define CRYPTO_TRC_0D064093 "func: asn1_f_a2d_asn1_object, reason:asn1_r_second_number_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0D06406B "func: asn1_f_a2d_asn1_object, reason:asn1_r_buffer_too_small, file:%s, line:%d"
    #define CRYPTO_TRC_0D093000 "func: asn1_f_d2i_asn1_object, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C4041 "func: asn1_f_c2i_asn1_object, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C4000 "func: asn1_f_c2i_asn1_object, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0D07B041 "func: asn1_f_asn1_object_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BC041 "func: asn1_f_i2d_asn1_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D094041 "func: asn1_f_d2i_asn1_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D094065 "func: asn1_f_d2i_asn1_set, reason:asn1_r_bad_class, file:%s, line:%d"
    #define CRYPTO_TRC_0D094068 "func: asn1_f_d2i_asn1_set, reason:asn1_r_bad_tag, file:%s, line:%d"
    #define CRYPTO_TRC_0D094088 "func: asn1_f_d2i_asn1_set, reason:asn1_r_length_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D094071 "func: asn1_f_d2i_asn1_set, reason:asn1_r_error_parsing_set_element, file:%s, line:%d"
    #define CRYPTO_TRC_0D0800A2 "func: asn1_f_asn1_sign, reason:asn1_r_unknown_object_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D08009A "func: asn1_f_asn1_sign, reason:asn1_r_the_asn1_object_identifier_is_not_known_for_this_md, file:%s, line:%d"
    #define CRYPTO_TRC_0D080041 "func: asn1_f_asn1_sign, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D080006 "func: asn1_f_asn1_sign, reason:err_r_evp_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C30A2 "func: asn1_f_asn1_item_sign, reason:asn1_r_unknown_object_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C309A "func: asn1_f_asn1_item_sign, reason:asn1_r_the_asn1_object_identifier_is_not_known_for_this_md, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C3041 "func: asn1_f_asn1_item_sign, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C3006 "func: asn1_f_asn1_item_sign, reason:err_r_evp_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D081041 "func: asn1_f_asn1_string_table_add, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A0076 "func: asn1_f_i2d_asn1_time, reason:asn1_r_expecting_a_time, file:%s, line:%d"
    #define CRYPTO_TRC_0D0AF0AD "func: asn1_f_asn1_time_set, reason:asn1_r_error_getting_time, file:%s, line:%d"
    #define CRYPTO_TRC_0D09703A "func: asn1_f_d2i_asn1_utctime, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D097084 "func: asn1_f_d2i_asn1_utctime, reason:asn1_r_invalid_time_format, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BB041 "func: asn1_f_asn1_utctime_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0890A1 "func: asn1_f_asn1_verify, reason:asn1_r_unknown_message_digest_algorithm, file:%s, line:%d"
    #define CRYPTO_TRC_0D089041 "func: asn1_f_asn1_verify, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D089006 "func: asn1_f_asn1_verify, reason:err_r_evp_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C50A1 "func: asn1_f_asn1_item_verify, reason:asn1_r_unknown_message_digest_algorithm, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C5006 "func: asn1_f_asn1_item_verify, reason:err_r_evp_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C5041 "func: asn1_f_asn1_item_verify, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D09A006 "func: asn1_f_d2i_privatekey, reason:err_r_evp_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D09A00D "func: asn1_f_d2i_privatekey, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D09A0A3 "func: asn1_f_d2i_privatekey, reason:asn1_r_unknown_public_key_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D09B006 "func: asn1_f_d2i_publickey, reason:err_r_evp_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D09B00D "func: asn1_f_d2i_publickey, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0D09B0A3 "func: asn1_f_d2i_publickey, reason:asn1_r_unknown_public_key_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D08706D "func: asn1_f_asn1_type_get_octetstring, reason:asn1_r_data_is_wrong, file:%s, line:%d"
    #define CRYPTO_TRC_0D08606D "func: asn1_f_asn1_type_get_int_octetstring, reason:asn1_r_data_is_wrong, file:%s, line:%d"
    #define CRYPTO_TRC_0D065091 "func: asn1_f_a2i_asn1_enumerated, reason:asn1_r_odd_number_of_chars, file:%s, line:%d"
    #define CRYPTO_TRC_0D065041 "func: asn1_f_a2i_asn1_enumerated, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D06508D "func: asn1_f_a2i_asn1_enumerated, reason:asn1_r_non_hex_characters, file:%s, line:%d"
    #define CRYPTO_TRC_0D065096 "func: asn1_f_a2i_asn1_enumerated, reason:asn1_r_short_line, file:%s, line:%d"
    #define CRYPTO_TRC_0D066091 "func: asn1_f_a2i_asn1_integer, reason:asn1_r_odd_number_of_chars, file:%s, line:%d"
    #define CRYPTO_TRC_0D066041 "func: asn1_f_a2i_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D06608D "func: asn1_f_a2i_asn1_integer, reason:asn1_r_non_hex_characters, file:%s, line:%d"
    #define CRYPTO_TRC_0D066096 "func: asn1_f_a2i_asn1_integer, reason:asn1_r_short_line, file:%s, line:%d"
    #define CRYPTO_TRC_0D067091 "func: asn1_f_a2i_asn1_string, reason:asn1_r_odd_number_of_chars, file:%s, line:%d"
    #define CRYPTO_TRC_0D067041 "func: asn1_f_a2i_asn1_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D06708D "func: asn1_f_a2i_asn1_string, reason:asn1_r_non_hex_characters, file:%s, line:%d"
    #define CRYPTO_TRC_0D067096 "func: asn1_f_a2i_asn1_string, reason:asn1_r_short_line, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A30A7 "func: asn1_f_i2d_privatekey, reason:asn1_r_unsupported_public_key_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A40A7 "func: asn1_f_i2d_publickey, reason:asn1_r_unsupported_public_key_type, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A2041 "func: asn1_f_i2d_rsa_net, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A2067 "func: asn1_f_i2d_rsa_net, reason:asn1_r_bad_password_read, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C806F "func: asn1_f_d2i_rsa_net, reason:asn1_r_decoding_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C8092 "func: asn1_f_d2i_rsa_net, reason:asn1_r_private_key_header_missing, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C80A6 "func: asn1_f_d2i_rsa_net, reason:asn1_r_unsupported_encryption_algorithm, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C9067 "func: asn1_f_d2i_rsa_net_2, reason:asn1_r_bad_password_read, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C909E "func: asn1_f_d2i_rsa_net_2, reason:asn1_r_unable_to_decode_rsa_private_key, file:%s, line:%d"
    #define CRYPTO_TRC_0D0C909D "func: asn1_f_d2i_rsa_net_2, reason:asn1_r_unable_to_decode_rsa_key, file:%s, line:%d"
    #define CRYPTO_TRC_0D0CA041 "func: asn1_f_pkcs5_pbe_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A7041 "func: asn1_f_pkcs5_pbe2_set, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D07803A "func: asn1_f_asn1_item_ex_d2i, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D078089 "func: asn1_f_asn1_item_ex_d2i, reason:asn1_r_missing_eoc, file:%s, line:%d"
    #define CRYPTO_TRC_0D078064 "func: asn1_f_asn1_item_ex_d2i, reason:asn1_r_aux_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D083089 "func: asn1_f_asn1_template_noexp_d2i, reason:asn1_r_missing_eoc, file:%s, line:%d"
    #define CRYPTO_TRC_0D06C07D "func: asn1_f_asn1_d2i_ex_primitive, reason:asn1_r_illegal_null, file:%s, line:%d"
    #define CRYPTO_TRC_0D06C03A "func: asn1_f_asn1_d2i_ex_primitive, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BE03A "func: asn1_f_asn1_find_end, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0BE089 "func: asn1_f_asn1_find_end, reason:asn1_r_missing_eoc, file:%s, line:%d"
    #define CRYPTO_TRC_0D06A03A "func: asn1_f_asn1_collect, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D06A0C5 "func: asn1_f_asn1_collect, reason:asn1_r_nested_asn1_string, file:%s, line:%d"
    #define CRYPTO_TRC_0D06A089 "func: asn1_f_asn1_collect, reason:asn1_r_missing_eoc, file:%s, line:%d"
    #define CRYPTO_TRC_0D08C041 "func: asn1_f_collect_data, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D068066 "func: asn1_f_asn1_check_tlen, reason:asn1_r_bad_object_header, file:%s, line:%d"
    #define CRYPTO_TRC_0D0680A8 "func: asn1_f_asn1_check_tlen, reason:asn1_r_wrong_tag, file:%s, line:%d"
    #define CRYPTO_TRC_0D079041 "func: asn1_f_asn1_item_ex_combine_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D079064 "func: asn1_f_asn1_item_ex_combine_new, reason:asn1_r_aux_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D085041 "func: asn1_f_asn1_template_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04074007 "func: rsa_f_rsa_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_04073041 "func: rsa_f_rsa_print, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0A069007 "func: dsa_f_dsa_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0A068065 "func: dsa_f_dsa_print, reason:dsa_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_0A068041 "func: dsa_f_dsa_print, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_05065007 "func: dh_f_dhparams_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_05064020 "func: dh_f_dhparams_print, reason:err_r_bio_lib, file:%s, line:%d"
    #define CRYPTO_TRC_05064043 "func: dh_f_dhparams_print, reason:err_r_passed_null_parameter, file:%s, line:%d"
    #define CRYPTO_TRC_05064041 "func: dh_f_dhparams_print, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_05064010 "func: dh_f_dhparams_print, reason:err_r_ec_lib, file:%s, line:%d"
    #define CRYPTO_TRC_05064000 "func: dh_f_dhparams_print, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0A065007 "func: dsa_f_dsaparams_print_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0A064065 "func: dsa_f_dsaparams_print, reason:dsa_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_0A064041 "func: dsa_f_dsaparams_print, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A9041 "func: asn1_f_x509_crl_add0_revoked, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0AA041 "func: asn1_f_x509_info_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A6080 "func: asn1_f_long_c2i, reason:asn1_r_integer_too_large_for_long, file:%s, line:%d"
    #define CRYPTO_TRC_0D0AB041 "func: asn1_f_x509_name_ex_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D09E03A "func: asn1_f_x509_name_ex_d2i, reason:err_r_nested_asn1_error, file:%s, line:%d"
    #define CRYPTO_TRC_0D0CB041 "func: asn1_f_x509_name_encode, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A5041 "func: asn1_f_i2d_rsa_pubkey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0A1041 "func: asn1_f_i2d_dsa_pubkey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0D0B5041 "func: asn1_f_i2d_ec_pubkey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_20072041 "func: bio_f_buffer_ctrl, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_2006C041 "func: bio_f_bio_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_2006F079 "func: bio_f_bio_read, reason:bio_r_unsupported_method, file:%s, line:%d"
    #define CRYPTO_TRC_2006F078 "func: bio_f_bio_read, reason:bio_r_uninitialized, file:%s, line:%d"
    #define CRYPTO_TRC_20071079 "func: bio_f_bio_write, reason:bio_r_unsupported_method, file:%s, line:%d"
    #define CRYPTO_TRC_20071078 "func: bio_f_bio_write, reason:bio_r_uninitialized, file:%s, line:%d"
    #define CRYPTO_TRC_2006E079 "func: bio_f_bio_puts, reason:bio_r_unsupported_method, file:%s, line:%d"
    #define CRYPTO_TRC_2006E078 "func: bio_f_bio_puts, reason:bio_r_uninitialized, file:%s, line:%d"
    #define CRYPTO_TRC_20068079 "func: bio_f_bio_gets, reason:bio_r_unsupported_method, file:%s, line:%d"
    #define CRYPTO_TRC_20068078 "func: bio_f_bio_gets, reason:bio_r_uninitialized, file:%s, line:%d"
    #define CRYPTO_TRC_20067079 "func: bio_f_bio_ctrl, reason:bio_r_unsupported_method, file:%s, line:%d"
    #define CRYPTO_TRC_20083079 "func: bio_f_bio_callback_ctrl, reason:bio_r_unsupported_method, file:%s, line:%d"
    #define CRYPTO_TRC_02001FFF "func: sys_f_fopen, reason:err_r_reason_syserr, file:%s, line:%d"
    #define CRYPTO_TRC_2006D080 "func: bio_f_bio_new_file, reason:bio_r_no_such_file, file:%s, line:%d"
    #define CRYPTO_TRC_2006D002 "func: bio_f_bio_new_file, reason:err_r_sys_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0200BFFF "func: sys_f_fread, reason:err_r_reason_syserr, file:%s, line:%d"
    #define CRYPTO_TRC_20082002 "func: bio_f_file_read, reason:err_r_sys_lib, file:%s, line:%d"
    #define CRYPTO_TRC_20074065 "func: bio_f_file_ctrl, reason:bio_r_bad_fopen_mode, file:%s, line:%d"
    #define CRYPTO_TRC_20074002 "func: bio_f_file_ctrl, reason:err_r_sys_lib, file:%s, line:%d"
    #define CRYPTO_TRC_2007E073 "func: bio_f_bio_new_mem_buf, reason:bio_r_null_parameter, file:%s, line:%d"
    #define CRYPTO_TRC_20075073 "func: bio_f_mem_write, reason:bio_r_null_parameter, file:%s, line:%d"
    #define CRYPTO_TRC_2007507E "func: bio_f_mem_write, reason:bio_r_write_to_read_only_bio, file:%s, line:%d"
    #define CRYPTO_TRC_03073064 "func: bn_f_bn_usub, reason:bn_r_arg2_lt_arg3, file:%s, line:%d"
    #define CRYPTO_TRC_03066041 "func: bn_f_bn_blinding_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0306706B "func: bn_f_bn_blinding_update, reason:bn_r_not_initialized, file:%s, line:%d"
    #define CRYPTO_TRC_0306406B "func: bn_f_bn_blinding_convert_ex, reason:bn_r_not_initialized, file:%s, line:%d"
    #define CRYPTO_TRC_0306506B "func: bn_f_bn_blinding_invert_ex, reason:bn_r_not_initialized, file:%s, line:%d"
    #define CRYPTO_TRC_0306A041 "func: bn_f_bn_ctx_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0308106D "func: bn_f_bn_ctx_start, reason:bn_r_too_many_temporary_variables, file:%s, line:%d"
    #define CRYPTO_TRC_0307406D "func: bn_f_bn_ctx_get, reason:bn_r_too_many_temporary_variables, file:%s, line:%d"
    #define CRYPTO_TRC_0306B067 "func: bn_f_bn_div, reason:bn_r_div_by_zero, file:%s, line:%d"
    #define CRYPTO_TRC_0308A067 "func: bn_f_bn_div_no_branch, reason:bn_r_div_by_zero, file:%s, line:%d"
    #define CRYPTO_TRC_0307B042 "func: bn_f_bn_exp, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define CRYPTO_TRC_0307D042 "func: bn_f_bn_mod_exp_recp, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define CRYPTO_TRC_0306D066 "func: bn_f_bn_mod_exp_mont, reason:bn_r_called_with_even_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_0307C066 "func: bn_f_bn_mod_exp_mont_consttime, reason:bn_r_called_with_even_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_03075042 "func: bn_f_bn_mod_exp_mont_word, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define CRYPTO_TRC_03075066 "func: bn_f_bn_mod_exp_mont_word, reason:bn_r_called_with_even_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_0307E042 "func: bn_f_bn_mod_exp_simple, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define CRYPTO_TRC_03076066 "func: bn_f_bn_mod_exp2_mont, reason:bn_r_called_with_even_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_0306E06C "func: bn_f_bn_mod_inverse, reason:bn_r_no_inverse, file:%s, line:%d"
    #define CRYPTO_TRC_0308B06C "func: bn_f_bn_mod_inverse_no_branch, reason:bn_r_no_inverse, file:%s, line:%d"
    #define CRYPTO_TRC_0308306A "func: bn_f_bn_gf2m_mod, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_0308506A "func: bn_f_bn_gf2m_mod_mul, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_0308806A "func: bn_f_bn_gf2m_mod_sqr, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_0308406A "func: bn_f_bn_gf2m_mod_exp, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_0308906A "func: bn_f_bn_gf2m_mod_sqrt, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_03087071 "func: bn_f_bn_gf2m_mod_solve_quad_arr, reason:bn_r_too_many_iterations, file:%s, line:%d"
    #define CRYPTO_TRC_03087074 "func: bn_f_bn_gf2m_mod_solve_quad_arr, reason:bn_r_no_solution, file:%s, line:%d"
    #define CRYPTO_TRC_0308606A "func: bn_f_bn_gf2m_mod_solve_quad, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_03071041 "func: bn_f_bn_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_03078072 "func: bn_f_bn_expand_internal, reason:bn_r_bignum_too_long, file:%s, line:%d"
    #define CRYPTO_TRC_03078069 "func: bn_f_bn_expand_internal, reason:bn_r_expand_on_static_bignum_data, file:%s, line:%d"
    #define CRYPTO_TRC_03078041 "func: bn_f_bn_expand_internal, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0307706E "func: bn_f_bn_mod_lshift_quick, reason:bn_r_input_not_reduced, file:%s, line:%d"
    #define CRYPTO_TRC_0307006A "func: bn_f_bn_mpi2bn, reason:bn_r_invalid_length, file:%s, line:%d"
    #define CRYPTO_TRC_03070068 "func: bn_f_bn_mpi2bn, reason:bn_r_encoding_error, file:%s, line:%d"
    #define CRYPTO_TRC_03069041 "func: bn_f_bn_bn2hex, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_03068041 "func: bn_f_bn_bn2dec, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0307F041 "func: bn_f_bnrand, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0307A073 "func: bn_f_bn_rand_range, reason:bn_r_invalid_range, file:%s, line:%d"
    #define CRYPTO_TRC_0307A071 "func: bn_f_bn_rand_range, reason:bn_r_too_many_iterations, file:%s, line:%d"
    #define CRYPTO_TRC_03082065 "func: bn_f_bn_div_recp, reason:bn_r_bad_reciprocal, file:%s, line:%d"
    #define CRYPTO_TRC_03079070 "func: bn_f_bn_mod_sqrt, reason:bn_r_p_is_not_prime, file:%s, line:%d"
    #define CRYPTO_TRC_03079071 "func: bn_f_bn_mod_sqrt, reason:bn_r_too_many_iterations, file:%s, line:%d"
    #define CRYPTO_TRC_0307906F "func: bn_f_bn_mod_sqrt, reason:bn_r_not_a_square, file:%s, line:%d"
    #define CRYPTO_TRC_07065041 "func: buf_f_buf_mem_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_07064041 "func: buf_f_buf_mem_grow, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_07069041 "func: buf_f_buf_mem_grow_clean, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_07068041 "func: buf_f_buf_strndup, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_07067041 "func: buf_f_buf_memdup, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0506A065 "func: dh_f_dh_builtin_genparams, reason:dh_r_bad_generator, file:%s, line:%d"
    #define CRYPTO_TRC_0506A003 "func: dh_f_dh_builtin_genparams, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_05067003 "func: dh_f_generate_key, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_05066067 "func: dh_f_compute_key, reason:dh_r_modulus_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_05066064 "func: dh_f_compute_key, reason:dh_r_no_private_value, file:%s, line:%d"
    #define CRYPTO_TRC_05066066 "func: dh_f_compute_key, reason:dh_r_invalid_pubkey, file:%s, line:%d"
    #define CRYPTO_TRC_05066003 "func: dh_f_compute_key, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_05069041 "func: dh_f_dh_new_method, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_05069026 "func: dh_f_dh_new_method, reason:err_r_engine_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0A072041 "func: dsa_f_sig_cb, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0A067041 "func: dsa_f_dsa_new_method, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0A067026 "func: dsa_f_dsa_new_method, reason:err_r_engine_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0A070003 "func: dsa_f_dsa_do_sign, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0A070065 "func: dsa_f_dsa_do_sign, reason:dsa_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_0A070064 "func: dsa_f_dsa_do_sign, reason:dsa_r_data_too_large_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_0A070000 "func: dsa_f_dsa_do_sign, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0A06B065 "func: dsa_f_dsa_sign_setup, reason:dsa_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_0A06B003 "func: dsa_f_dsa_sign_setup, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0A071065 "func: dsa_f_dsa_do_verify, reason:dsa_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_0A071066 "func: dsa_f_dsa_do_verify, reason:dsa_r_bad_q_value, file:%s, line:%d"
    #define CRYPTO_TRC_0A071067 "func: dsa_f_dsa_do_verify, reason:dsa_r_modulus_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0A071003 "func: dsa_f_dsa_do_verify, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_06080086 "func: evp_f_evp_digestinit_ex, reason:evp_r_initialization_error, file:%s, line:%d"
    #define CRYPTO_TRC_0608008B "func: evp_f_evp_digestinit_ex, reason:evp_r_no_digest_set, file:%s, line:%d"
    #define CRYPTO_TRC_0606E06F "func: evp_f_evp_md_ctx_copy_ex, reason:evp_r_input_not_initialized, file:%s, line:%d"
    #define CRYPTO_TRC_0606E026 "func: evp_f_evp_md_ctx_copy_ex, reason:err_r_engine_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0607B086 "func: evp_f_evp_cipherinit_ex, reason:evp_r_initialization_error, file:%s, line:%d"
    #define CRYPTO_TRC_0607B041 "func: evp_f_evp_cipherinit_ex, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0607B083 "func: evp_f_evp_cipherinit_ex, reason:evp_r_no_cipher_set, file:%s, line:%d"
    #define CRYPTO_TRC_0607F08A "func: evp_f_evp_encryptfinal_ex, reason:evp_r_data_not_multiple_of_block_length, file:%s, line:%d"
    #define CRYPTO_TRC_0606508A "func: evp_f_evp_decryptfinal_ex, reason:evp_r_data_not_multiple_of_block_length, file:%s, line:%d"
    #define CRYPTO_TRC_0606506D "func: evp_f_evp_decryptfinal_ex, reason:evp_r_wrong_final_block_length, file:%s, line:%d"
    #define CRYPTO_TRC_06065064 "func: evp_f_evp_decryptfinal_ex, reason:evp_r_bad_decrypt, file:%s, line:%d"
    #define CRYPTO_TRC_0607A082 "func: evp_f_evp_cipher_ctx_set_key_length, reason:evp_r_invalid_key_length, file:%s, line:%d"
    #define CRYPTO_TRC_0607C083 "func: evp_f_evp_cipher_ctx_ctrl, reason:evp_r_no_cipher_set, file:%s, line:%d"
    #define CRYPTO_TRC_0607C084 "func: evp_f_evp_cipher_ctx_ctrl, reason:evp_r_ctrl_not_implemented, file:%s, line:%d"
    #define CRYPTO_TRC_0607C085 "func: evp_f_evp_cipher_ctx_ctrl, reason:evp_r_ctrl_operation_not_implemented, file:%s, line:%d"
    #define CRYPTO_TRC_06074079 "func: evp_f_evp_pbe_cipherinit, reason:evp_r_unknown_pbe_algorithm, file:%s, line:%d"
    #define CRYPTO_TRC_06074078 "func: evp_f_evp_pbe_cipherinit, reason:evp_r_keygen_failure, file:%s, line:%d"
    #define CRYPTO_TRC_06073041 "func: evp_f_evp_pbe_alg_add, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0606F041 "func: evp_f_evp_pkcs82pkey, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0606F072 "func: evp_f_evp_pkcs82pkey, reason:evp_r_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0606F070 "func: evp_f_evp_pkcs82pkey, reason:evp_r_bn_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0606F071 "func: evp_f_evp_pkcs82pkey, reason:evp_r_bn_pubkey_error, file:%s, line:%d"
    #define CRYPTO_TRC_0606F010 "func: evp_f_evp_pkcs82pkey, reason:err_r_ec_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0606F076 "func: evp_f_evp_pkcs82pkey, reason:evp_r_unsupported_private_key_algorithm, file:%s, line:%d"
    #define CRYPTO_TRC_06071041 "func: evp_f_evp_pkey2pkcs8_broken, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_06071076 "func: evp_f_evp_pkey2pkcs8_broken, reason:evp_r_unsupported_private_key_algorithm, file:%s, line:%d"
    #define CRYPTO_TRC_06070075 "func: evp_f_pkcs8_set_broken, reason:evp_r_pkcs8_unknown_broken_type, file:%s, line:%d"
    #define CRYPTO_TRC_06087041 "func: evp_f_dsa_pkey2pkcs8, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_06087073 "func: evp_f_dsa_pkey2pkcs8, reason:evp_r_encode_error, file:%s, line:%d"
    #define CRYPTO_TRC_06084067 "func: evp_f_eckey_pkey2pkcs8, reason:evp_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_06084041 "func: evp_f_eckey_pkey2pkcs8, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_06084010 "func: evp_f_eckey_pkey2pkcs8, reason:err_r_ec_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0608400D "func: evp_f_eckey_pkey2pkcs8, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_06084073 "func: evp_f_eckey_pkey2pkcs8, reason:evp_r_encode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0608508F "func: evp_f_aes_init_key, reason:evp_r_aes_key_setup_failed, file:%s, line:%d"
    #define CRYPTO_TRC_06075072 "func: evp_f_pkcs5_pbe_keyivgen, reason:evp_r_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_06076072 "func: evp_f_pkcs5_v2_pbe_keyivgen, reason:evp_r_decode_error, file:%s, line:%d"
    #define CRYPTO_TRC_0607607D "func: evp_f_pkcs5_v2_pbe_keyivgen, reason:evp_r_unsupported_prf, file:%s, line:%d"
    #define CRYPTO_TRC_0606806A "func: evp_f_evp_pkey_decrypt, reason:evp_r_public_key_not_rsa, file:%s, line:%d"
    #define CRYPTO_TRC_0606906A "func: evp_f_evp_pkey_encrypt, reason:evp_r_public_key_not_rsa, file:%s, line:%d"
    #define CRYPTO_TRC_06067065 "func: evp_f_evp_pkey_copy_parameters, reason:evp_r_different_key_types, file:%s, line:%d"
    #define CRYPTO_TRC_06067067 "func: evp_f_evp_pkey_copy_parameters, reason:evp_r_missing_parameters, file:%s, line:%d"
    #define CRYPTO_TRC_0606A041 "func: evp_f_evp_pkey_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0607907F "func: evp_f_evp_pkey_get1_rsa, reason:evp_r_expecting_an_rsa_key, file:%s, line:%d"
    #define CRYPTO_TRC_06078081 "func: evp_f_evp_pkey_get1_dsa, reason:evp_r_expecting_a_dsa_key, file:%s, line:%d"
    #define CRYPTO_TRC_0608308E "func: evp_f_evp_pkey_get1_ec_key, reason:evp_r_expecting_a_ec_key, file:%s, line:%d"
    #define CRYPTO_TRC_06077080 "func: evp_f_evp_pkey_get1_dh, reason:evp_r_expecting_a_dh_key, file:%s, line:%d"
    #define CRYPTO_TRC_0606606A "func: evp_f_evp_openinit, reason:evp_r_public_key_not_rsa, file:%s, line:%d"
    #define CRYPTO_TRC_06066041 "func: evp_f_evp_openinit, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0606B06E "func: evp_f_evp_signfinal, reason:evp_r_wrong_public_key_type, file:%s, line:%d"
    #define CRYPTO_TRC_0606B068 "func: evp_f_evp_signfinal, reason:evp_r_no_sign_function_configured, file:%s, line:%d"
    #define CRYPTO_TRC_0606C06E "func: evp_f_evp_verifyfinal, reason:evp_r_wrong_public_key_type, file:%s, line:%d"
    #define CRYPTO_TRC_0606C069 "func: evp_f_evp_verifyfinal, reason:evp_r_no_verify_function_configured, file:%s, line:%d"
    #define CRYPTO_TRC_08069041 "func: obj_f_obj_add_object, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_08067065 "func: obj_f_obj_nid2obj, reason:obj_r_unknown_nid, file:%s, line:%d"
    #define CRYPTO_TRC_08068065 "func: obj_f_obj_nid2sn, reason:obj_r_unknown_nid, file:%s, line:%d"
    #define CRYPTO_TRC_08066065 "func: obj_f_obj_nid2ln, reason:obj_r_unknown_nid, file:%s, line:%d"
    #define CRYPTO_TRC_08064041 "func: obj_f_obj_create, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0806500D "func: obj_f_obj_dup, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_08065041 "func: obj_f_obj_dup, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0806A041 "func: obj_f_obj_name_new_index, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_09073007 "func: pem_f_pem_x509_info_read, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09074041 "func: pem_f_pem_x509_info_read_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0907400D "func: pem_f_pem_x509_info_read_bio, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09075071 "func: pem_f_pem_x509_info_write_bio, reason:pem_r_unsupported_cipher, file:%s, line:%d"
    #define CRYPTO_TRC_09064042 "func: pem_f_pem_def_callback, reason:err_r_should_not_have_been_called, file:%s, line:%d"
    #define CRYPTO_TRC_0906406D "func: pem_f_pem_def_callback, reason:pem_r_problems_getting_password, file:%s, line:%d"
    #define CRYPTO_TRC_09066007 "func: pem_f_pem_asn1_read, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09068007 "func: pem_f_pem_asn1_write, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09069071 "func: pem_f_pem_asn1_write_bio, reason:pem_r_unsupported_cipher, file:%s, line:%d"
    #define CRYPTO_TRC_0906900D "func: pem_f_pem_asn1_write_bio, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09069041 "func: pem_f_pem_asn1_write_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0906906F "func: pem_f_pem_asn1_write_bio, reason:pem_r_read_key, file:%s, line:%d"
    #define CRYPTO_TRC_0906A068 "func: pem_f_pem_do_header, reason:pem_r_bad_password_read, file:%s, line:%d"
    #define CRYPTO_TRC_0906A065 "func: pem_f_pem_do_header, reason:pem_r_bad_decrypt, file:%s, line:%d"
    #define CRYPTO_TRC_0906B06B "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_not_proc_type, file:%s, line:%d"
    #define CRYPTO_TRC_0906B06A "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_not_encrypted, file:%s, line:%d"
    #define CRYPTO_TRC_0906B070 "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_short_header, file:%s, line:%d"
    #define CRYPTO_TRC_0906B069 "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_not_dek_info, file:%s, line:%d"
    #define CRYPTO_TRC_0906B072 "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_unsupported_encryption, file:%s, line:%d"
    #define CRYPTO_TRC_09065067 "func: pem_f_load_iv, reason:pem_r_bad_iv_chars, file:%s, line:%d"
    #define CRYPTO_TRC_09071007 "func: pem_f_pem_write, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09072007 "func: pem_f_pem_write_bio, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09072041 "func: pem_f_pem_write_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_09072000 "func: pem_f_pem_write_bio, reason:err_r_reason_none, file:%s, line:%d"
    #define CRYPTO_TRC_0906C007 "func: pem_f_pem_read, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0906D041 "func: pem_f_pem_read_bio, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0906D06C "func: pem_f_pem_read_bio, reason:pem_r_no_start_line, file:%s, line:%d"
    #define CRYPTO_TRC_0906D066 "func: pem_f_pem_read_bio, reason:pem_r_bad_end_line, file:%s, line:%d"
    #define CRYPTO_TRC_0906D064 "func: pem_f_pem_read_bio, reason:pem_r_bad_base64_decode, file:%s, line:%d"
    #define CRYPTO_TRC_0906700D "func: pem_f_pem_asn1_read_bio, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0907E06F "func: pem_f_do_pk8pkey, reason:pem_r_read_key, file:%s, line:%d"
    #define CRYPTO_TRC_09078068 "func: pem_f_d2i_pkcs8privatekey_bio, reason:pem_r_bad_password_read, file:%s, line:%d"
    #define CRYPTO_TRC_0907D007 "func: pem_f_do_pk8pkey_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_09079007 "func: pem_f_d2i_pkcs8privatekey_fp, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0907B00D "func: pem_f_pem_read_bio_privatekey, reason:err_r_asn1_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0907C007 "func: pem_f_pem_read_privatekey, reason:err_r_buf_lib, file:%s, line:%d"
    #define CRYPTO_TRC_0906F06E "func: pem_f_pem_sealinit, reason:pem_r_public_key_no_rsa, file:%s, line:%d"
    #define CRYPTO_TRC_0906F041 "func: pem_f_pem_sealinit, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0906E06E "func: pem_f_pem_sealfinal, reason:pem_r_public_key_no_rsa, file:%s, line:%d"
    #define CRYPTO_TRC_0906E041 "func: pem_f_pem_sealfinal, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_09070041 "func: pem_f_pem_signfinal, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_24064064 "func: rand_f_ssleay_rand_bytes, reason:rand_r_prng_not_seeded, file:%s, line:%d"
    #define CRYPTO_TRC_0407B041 "func: rsa_f_rsa_check_key, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0407B080 "func: rsa_f_rsa_check_key, reason:rsa_r_p_not_prime, file:%s, line:%d"
    #define CRYPTO_TRC_0407B081 "func: rsa_f_rsa_check_key, reason:rsa_r_q_not_prime, file:%s, line:%d"
    #define CRYPTO_TRC_0407B07F "func: rsa_f_rsa_check_key, reason:rsa_r_n_does_not_equal_p_q, file:%s, line:%d"
    #define CRYPTO_TRC_0407B07B "func: rsa_f_rsa_check_key, reason:rsa_r_d_e_not_congruent_to_1, file:%s, line:%d"
    #define CRYPTO_TRC_04068069 "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_modulus_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_04068065 "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_bad_e_value, file:%s, line:%d"
    #define CRYPTO_TRC_04068041 "func: rsa_f_rsa_eay_public_encrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04068076 "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d"
    #define CRYPTO_TRC_04068084 "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_04066041 "func: rsa_f_rsa_eay_private_encrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04066076 "func: rsa_f_rsa_eay_private_encrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d"
    #define CRYPTO_TRC_04066084 "func: rsa_f_rsa_eay_private_encrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_04066044 "func: rsa_f_rsa_eay_private_encrypt, reason:err_r_internal_error, file:%s, line:%d"
    #define CRYPTO_TRC_04065041 "func: rsa_f_rsa_eay_private_decrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0406506C "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_data_greater_than_mod_len, file:%s, line:%d"
    #define CRYPTO_TRC_04065084 "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_04065044 "func: rsa_f_rsa_eay_private_decrypt, reason:err_r_internal_error, file:%s, line:%d"
    #define CRYPTO_TRC_04065076 "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d"
    #define CRYPTO_TRC_04065072 "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_padding_check_failed, file:%s, line:%d"
    #define CRYPTO_TRC_04067069 "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_modulus_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_04067065 "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_bad_e_value, file:%s, line:%d"
    #define CRYPTO_TRC_04067041 "func: rsa_f_rsa_eay_public_decrypt, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0406706C "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_data_greater_than_mod_len, file:%s, line:%d"
    #define CRYPTO_TRC_04067084 "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d"
    #define CRYPTO_TRC_04067076 "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d"
    #define CRYPTO_TRC_04067072 "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_padding_check_failed, file:%s, line:%d"
    #define CRYPTO_TRC_04081078 "func: rsa_f_rsa_builtin_keygen, reason:rsa_r_key_size_too_small, file:%s, line:%d"
    #define CRYPTO_TRC_04081003 "func: rsa_f_rsa_builtin_keygen, reason:err_lib_bn, file:%s, line:%d"
    #define CRYPTO_TRC_0406A041 "func: rsa_f_rsa_new_method, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0406A026 "func: rsa_f_rsa_new_method, reason:err_r_engine_lib, file:%s, line:%d"
    #define CRYPTO_TRC_04088041 "func: rsa_f_rsa_setup_blinding, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0408808C "func: rsa_f_rsa_setup_blinding, reason:rsa_r_no_public_exponent, file:%s, line:%d"
    #define CRYPTO_TRC_04088003 "func: rsa_f_rsa_setup_blinding, reason:err_r_bn_lib, file:%s, line:%d"
    #define CRYPTO_TRC_04082041 "func: rsa_f_rsa_memory_lock, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0406B06E "func: rsa_f_rsa_padding_add_none, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_0406B07A "func: rsa_f_rsa_padding_add_none, reason:rsa_r_data_too_small_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_0406F06D "func: rsa_f_rsa_padding_check_none, reason:rsa_r_data_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_04087082 "func: rsa_f_rsa_null_public_encrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d"
    #define CRYPTO_TRC_04085082 "func: rsa_f_rsa_null_private_encrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d"
    #define CRYPTO_TRC_04084082 "func: rsa_f_rsa_null_private_decrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d"
    #define CRYPTO_TRC_04086082 "func: rsa_f_rsa_null_public_decrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d"
    #define CRYPTO_TRC_04079078 "func: rsa_f_rsa_padding_add_pkcs1_oaep, reason:rsa_r_key_size_too_small, file:%s, line:%d"
    #define CRYPTO_TRC_04079041 "func: rsa_f_rsa_padding_add_pkcs1_oaep, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0407A041 "func: rsa_f_rsa_padding_check_pkcs1_oaep, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0407A06D "func: rsa_f_rsa_padding_check_pkcs1_oaep, reason:rsa_r_data_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0407A079 "func: rsa_f_rsa_padding_check_pkcs1_oaep, reason:rsa_r_oaep_decoding_error, file:%s, line:%d"
    #define CRYPTO_TRC_0406C06E "func: rsa_f_rsa_padding_add_pkcs1_type_1, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_0407006A "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_block_type_is_not_01, file:%s, line:%d"
    #define CRYPTO_TRC_04070066 "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_bad_fixed_header_decrypt, file:%s, line:%d"
    #define CRYPTO_TRC_04070071 "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_null_before_block_missing, file:%s, line:%d"
    #define CRYPTO_TRC_04070067 "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_bad_pad_byte_count, file:%s, line:%d"
    #define CRYPTO_TRC_0407006D "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_data_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0406D06E "func: rsa_f_rsa_padding_add_pkcs1_type_2, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_0407106B "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_block_type_is_not_02, file:%s, line:%d"
    #define CRYPTO_TRC_04071071 "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_null_before_block_missing, file:%s, line:%d"
    #define CRYPTO_TRC_04071067 "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_bad_pad_byte_count, file:%s, line:%d"
    #define CRYPTO_TRC_0407106D "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_data_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0407E088 "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_slen_check_failed, file:%s, line:%d"
    #define CRYPTO_TRC_0407E085 "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_first_octet_invalid, file:%s, line:%d"
    #define CRYPTO_TRC_0407E06D "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_data_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0407E086 "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_last_octet_invalid, file:%s, line:%d"
    #define CRYPTO_TRC_0407E041 "func: rsa_f_rsa_verify_pkcs1_pss, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0407E087 "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_slen_recovery_failed, file:%s, line:%d"
    #define CRYPTO_TRC_0407E068 "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_bad_signature, file:%s, line:%d"
    #define CRYPTO_TRC_0407D088 "func: rsa_f_rsa_padding_add_pkcs1_pss, reason:rsa_r_slen_check_failed, file:%s, line:%d"
    #define CRYPTO_TRC_04076070 "func: rsa_f_rsa_sign_asn1_octet_string, reason:rsa_r_digest_too_big_for_rsa_key, file:%s, line:%d"
    #define CRYPTO_TRC_04076041 "func: rsa_f_rsa_sign_asn1_octet_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04078077 "func: rsa_f_rsa_verify_asn1_octet_string, reason:rsa_r_wrong_signature_length, file:%s, line:%d"
    #define CRYPTO_TRC_04078041 "func: rsa_f_rsa_verify_asn1_octet_string, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04078068 "func: rsa_f_rsa_verify_asn1_octet_string, reason:rsa_r_bad_signature, file:%s, line:%d"
    #define CRYPTO_TRC_04075083 "func: rsa_f_rsa_sign, reason:rsa_r_invalid_message_length, file:%s, line:%d"
    #define CRYPTO_TRC_04075075 "func: rsa_f_rsa_sign, reason:rsa_r_unknown_algorithm_type, file:%s, line:%d"
    #define CRYPTO_TRC_04075074 "func: rsa_f_rsa_sign, reason:rsa_r_the_asn1_object_identifier_is_not_known_for_this_md, file:%s, line:%d"
    #define CRYPTO_TRC_04075070 "func: rsa_f_rsa_sign, reason:rsa_r_digest_too_big_for_rsa_key, file:%s, line:%d"
    #define CRYPTO_TRC_04075041 "func: rsa_f_rsa_sign, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04077077 "func: rsa_f_rsa_verify, reason:rsa_r_wrong_signature_length, file:%s, line:%d"
    #define CRYPTO_TRC_04077041 "func: rsa_f_rsa_verify, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_04077083 "func: rsa_f_rsa_verify, reason:rsa_r_invalid_message_length, file:%s, line:%d"
    #define CRYPTO_TRC_04077068 "func: rsa_f_rsa_verify, reason:rsa_r_bad_signature, file:%s, line:%d"
    #define CRYPTO_TRC_0406E06E "func: rsa_f_rsa_padding_add_sslv23, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_0407206F "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_data_too_small, file:%s, line:%d"
    #define CRYPTO_TRC_0407206B "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_block_type_is_not_02, file:%s, line:%d"
    #define CRYPTO_TRC_04072071 "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_null_before_block_missing, file:%s, line:%d"
    #define CRYPTO_TRC_04072073 "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_sslv3_rollback_attack, file:%s, line:%d"
    #define CRYPTO_TRC_0407206D "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_data_too_large, file:%s, line:%d"
    #define CRYPTO_TRC_0407F06E "func: rsa_f_rsa_padding_add_x931, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d"
    #define CRYPTO_TRC_04080089 "func: rsa_f_rsa_padding_check_x931, reason:rsa_r_invalid_header, file:%s, line:%d"
    #define CRYPTO_TRC_0408008A "func: rsa_f_rsa_padding_check_x931, reason:rsa_r_invalid_padding, file:%s, line:%d"
    #define CRYPTO_TRC_0408008B "func: rsa_f_rsa_padding_check_x931, reason:rsa_r_invalid_trailer, file:%s, line:%d"
    #define CRYPTO_TRC_20076041 "func: bio_f_ssl_new, reason:err_r_malloc_failure, file:%s, line:%d"
    #define CRYPTO_TRC_0200AFFF "func: sys_f_opendir, reason:err_r_reason_syserr, file:%s, line:%d"
#else
BEGIN_TRACE_MAP(MOD_OSSL_CRYPTO)

    TRC_MSG(CRYPTO_TRC_0F065041, "func: crypto_f_crypto_get_new_lockid, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F067064, "func: crypto_f_crypto_get_new_dynlockid, reason:crypto_r_no_dynlock_create_callback, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F067041, "func: crypto_f_crypto_get_new_dynlockid, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F069041, "func: crypto_f_def_get_class, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F068041, "func: crypto_f_def_add_index, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F06C041, "func: crypto_f_int_new_ex_data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F06A041, "func: crypto_f_int_dup_ex_data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F06B041, "func: crypto_f_int_free_ex_data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0F066041, "func: crypto_f_crypto_set_ex_data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B20C0, "func: asn1_f_asn1_generate_v3, reason:asn1_r_sequence_or_set_needs_config, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B10C2, "func: asn1_f_asn1_cb, reason:asn1_r_unknown_tag, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B10BD, "func: asn1_f_asn1_cb, reason:asn1_r_missing_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B10B5, "func: asn1_f_asn1_cb, reason:asn1_r_illegal_nested_tagging, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B10C3, "func: asn1_f_asn1_cb, reason:asn1_r_unkown_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B60BB, "func: asn1_f_parse_tagging, reason:asn1_r_invalid_number, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B60BA, "func: asn1_f_parse_tagging, reason:asn1_r_invalid_modifier, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B00B3, "func: asn1_f_append_exp, reason:asn1_r_illegal_implicit_tag, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B00AE, "func: asn1_f_append_exp, reason:asn1_r_depth_exceeded, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B3041, "func: asn1_f_asn1_str2type, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B6, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_null_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30BE, "func: asn1_f_asn1_str2type, reason:asn1_r_not_ascii_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B0, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_boolean, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B9, "func: asn1_f_asn1_str2type, reason:asn1_r_integer_not_ascii_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B4, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_integer, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30BF, "func: asn1_f_asn1_str2type, reason:asn1_r_object_not_ascii_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B7, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_object, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30C1, "func: asn1_f_asn1_str2type, reason:asn1_r_time_not_ascii_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B8, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_time_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B1, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30B2, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_hex, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30BC, "func: asn1_f_asn1_str2type, reason:asn1_r_list_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30AF, "func: asn1_f_asn1_str2type, reason:asn1_r_illegal_bitstring_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B30C4, "func: asn1_f_asn1_str2type, reason:asn1_r_unsupported_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B40BB, "func: asn1_f_bitstr_cb, reason:asn1_r_invalid_number, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B4041, "func: asn1_f_bitstr_cb, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07209B, "func: asn1_f_asn1_get_object, reason:asn1_r_too_long, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07207B, "func: asn1_f_asn1_get_object, reason:asn1_r_header_too_long, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BA041, "func: asn1_f_asn1_string_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D082041, "func: asn1_f_asn1_string_type_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0AE0AC, "func: asn1_f_oid_module_init, reason:asn1_r_error_loading_section, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0AE0AB, "func: asn1_f_oid_module_init, reason:asn1_r_adding_object, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07F06E, "func: asn1_f_asn1_seq_unpack, reason:asn1_r_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07E070, "func: asn1_f_asn1_seq_pack, reason:asn1_r_encode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07E041, "func: asn1_f_asn1_seq_pack, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08806E, "func: asn1_f_asn1_unpack_string, reason:asn1_r_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07C041, "func: asn1_f_asn1_pack_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07C070, "func: asn1_f_asn1_pack_string, reason:asn1_r_encode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C6041, "func: asn1_f_asn1_item_pack, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C6070, "func: asn1_f_asn1_item_pack, reason:asn1_r_encode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C706E, "func: asn1_f_asn1_item_unpack, reason:asn1_r_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BD041, "func: asn1_f_c2i_asn1_bit_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BD000, "func: asn1_f_c2i_asn1_bit_string, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B7041, "func: asn1_f_asn1_bit_string_set_bit, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08E000, "func: asn1_f_d2i_asn1_boolean, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D095041, "func: asn1_f_d2i_asn1_type_bytes, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D095000, "func: asn1_f_d2i_asn1_type_bytes, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08F041, "func: asn1_f_d2i_asn1_bytes, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08F000, "func: asn1_f_d2i_asn1_bytes, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D069043, "func: asn1_f_asn1_collate_primitive, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D069007, "func: asn1_f_asn1_collate_primitive, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06903F, "func: asn1_f_asn1_collate_primitive, reason:err_r_missing_asn1_eos, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06D007, "func: asn1_f_asn1_d2i_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0CE007, "func: asn1_f_asn1_item_d2i_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06B041, "func: asn1_f_asn1_d2i_read_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06B08E, "func: asn1_f_asn1_d2i_read_bio, reason:asn1_r_not_enough_data, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B8041, "func: asn1_f_asn1_digest, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06F041, "func: asn1_f_asn1_dup, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BF041, "func: asn1_f_asn1_item_dup, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D070041, "func: asn1_f_asn1_enumerated_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08A03A, "func: asn1_f_bn_to_asn1_enumerated, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08A041, "func: asn1_f_bn_to_asn1_enumerated, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D071069, "func: asn1_f_asn1_enumerated_to_bn, reason:asn1_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09003A, "func: asn1_f_d2i_asn1_generalizedtime, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D090084, "func: asn1_f_d2i_asn1_generalizedtime, reason:asn1_r_invalid_time_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D075007, "func: asn1_f_asn1_i2d_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D074041, "func: asn1_f_asn1_i2d_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C1007, "func: asn1_f_asn1_item_i2d_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C0041, "func: asn1_f_asn1_item_i2d_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C2041, "func: asn1_f_c2i_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C2000, "func: asn1_f_c2i_asn1_integer, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D096041, "func: asn1_f_d2i_asn1_uinteger, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D096000, "func: asn1_f_d2i_asn1_uinteger, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D076041, "func: asn1_f_asn1_integer_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08B03A, "func: asn1_f_bn_to_asn1_integer, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08B041, "func: asn1_f_bn_to_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D077069, "func: asn1_f_asn1_integer_to_bn, reason:asn1_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07A0A0, "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_unknown_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07A098, "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_string_too_short, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07A097, "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_string_too_long, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07A07C, "func: asn1_f_asn1_mbstring_ncopy, reason:asn1_r_illegal_characters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07A041, "func: asn1_f_asn1_mbstring_ncopy, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06407A, "func: asn1_f_a2d_asn1_object, reason:asn1_r_first_num_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06408A, "func: asn1_f_a2d_asn1_object, reason:asn1_r_missing_second_number, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D064083, "func: asn1_f_a2d_asn1_object, reason:asn1_r_invalid_separator, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D064082, "func: asn1_f_a2d_asn1_object, reason:asn1_r_invalid_digit, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D064093, "func: asn1_f_a2d_asn1_object, reason:asn1_r_second_number_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06406B, "func: asn1_f_a2d_asn1_object, reason:asn1_r_buffer_too_small, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D093000, "func: asn1_f_d2i_asn1_object, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C4041, "func: asn1_f_c2i_asn1_object, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C4000, "func: asn1_f_c2i_asn1_object, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07B041, "func: asn1_f_asn1_object_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BC041, "func: asn1_f_i2d_asn1_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D094041, "func: asn1_f_d2i_asn1_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D094065, "func: asn1_f_d2i_asn1_set, reason:asn1_r_bad_class, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D094068, "func: asn1_f_d2i_asn1_set, reason:asn1_r_bad_tag, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D094088, "func: asn1_f_d2i_asn1_set, reason:asn1_r_length_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D094071, "func: asn1_f_d2i_asn1_set, reason:asn1_r_error_parsing_set_element, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0800A2, "func: asn1_f_asn1_sign, reason:asn1_r_unknown_object_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08009A, "func: asn1_f_asn1_sign, reason:asn1_r_the_asn1_object_identifier_is_not_known_for_this_md, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D080041, "func: asn1_f_asn1_sign, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D080006, "func: asn1_f_asn1_sign, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C30A2, "func: asn1_f_asn1_item_sign, reason:asn1_r_unknown_object_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C309A, "func: asn1_f_asn1_item_sign, reason:asn1_r_the_asn1_object_identifier_is_not_known_for_this_md, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C3041, "func: asn1_f_asn1_item_sign, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C3006, "func: asn1_f_asn1_item_sign, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D081041, "func: asn1_f_asn1_string_table_add, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A0076, "func: asn1_f_i2d_asn1_time, reason:asn1_r_expecting_a_time, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0AF0AD, "func: asn1_f_asn1_time_set, reason:asn1_r_error_getting_time, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09703A, "func: asn1_f_d2i_asn1_utctime, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D097084, "func: asn1_f_d2i_asn1_utctime, reason:asn1_r_invalid_time_format, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BB041, "func: asn1_f_asn1_utctime_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0890A1, "func: asn1_f_asn1_verify, reason:asn1_r_unknown_message_digest_algorithm, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D089041, "func: asn1_f_asn1_verify, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D089006, "func: asn1_f_asn1_verify, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C50A1, "func: asn1_f_asn1_item_verify, reason:asn1_r_unknown_message_digest_algorithm, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C5006, "func: asn1_f_asn1_item_verify, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C5041, "func: asn1_f_asn1_item_verify, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09A006, "func: asn1_f_d2i_privatekey, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09A00D, "func: asn1_f_d2i_privatekey, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09A0A3, "func: asn1_f_d2i_privatekey, reason:asn1_r_unknown_public_key_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09B006, "func: asn1_f_d2i_publickey, reason:err_r_evp_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09B00D, "func: asn1_f_d2i_publickey, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09B0A3, "func: asn1_f_d2i_publickey, reason:asn1_r_unknown_public_key_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08706D, "func: asn1_f_asn1_type_get_octetstring, reason:asn1_r_data_is_wrong, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08606D, "func: asn1_f_asn1_type_get_int_octetstring, reason:asn1_r_data_is_wrong, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D065091, "func: asn1_f_a2i_asn1_enumerated, reason:asn1_r_odd_number_of_chars, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D065041, "func: asn1_f_a2i_asn1_enumerated, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06508D, "func: asn1_f_a2i_asn1_enumerated, reason:asn1_r_non_hex_characters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D065096, "func: asn1_f_a2i_asn1_enumerated, reason:asn1_r_short_line, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D066091, "func: asn1_f_a2i_asn1_integer, reason:asn1_r_odd_number_of_chars, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D066041, "func: asn1_f_a2i_asn1_integer, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06608D, "func: asn1_f_a2i_asn1_integer, reason:asn1_r_non_hex_characters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D066096, "func: asn1_f_a2i_asn1_integer, reason:asn1_r_short_line, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D067091, "func: asn1_f_a2i_asn1_string, reason:asn1_r_odd_number_of_chars, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D067041, "func: asn1_f_a2i_asn1_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06708D, "func: asn1_f_a2i_asn1_string, reason:asn1_r_non_hex_characters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D067096, "func: asn1_f_a2i_asn1_string, reason:asn1_r_short_line, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A30A7, "func: asn1_f_i2d_privatekey, reason:asn1_r_unsupported_public_key_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A40A7, "func: asn1_f_i2d_publickey, reason:asn1_r_unsupported_public_key_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A2041, "func: asn1_f_i2d_rsa_net, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A2067, "func: asn1_f_i2d_rsa_net, reason:asn1_r_bad_password_read, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C806F, "func: asn1_f_d2i_rsa_net, reason:asn1_r_decoding_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C8092, "func: asn1_f_d2i_rsa_net, reason:asn1_r_private_key_header_missing, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C80A6, "func: asn1_f_d2i_rsa_net, reason:asn1_r_unsupported_encryption_algorithm, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C9067, "func: asn1_f_d2i_rsa_net_2, reason:asn1_r_bad_password_read, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C909E, "func: asn1_f_d2i_rsa_net_2, reason:asn1_r_unable_to_decode_rsa_private_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0C909D, "func: asn1_f_d2i_rsa_net_2, reason:asn1_r_unable_to_decode_rsa_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0CA041, "func: asn1_f_pkcs5_pbe_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A7041, "func: asn1_f_pkcs5_pbe2_set, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D07803A, "func: asn1_f_asn1_item_ex_d2i, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D078089, "func: asn1_f_asn1_item_ex_d2i, reason:asn1_r_missing_eoc, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D078064, "func: asn1_f_asn1_item_ex_d2i, reason:asn1_r_aux_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D083089, "func: asn1_f_asn1_template_noexp_d2i, reason:asn1_r_missing_eoc, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06C07D, "func: asn1_f_asn1_d2i_ex_primitive, reason:asn1_r_illegal_null, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06C03A, "func: asn1_f_asn1_d2i_ex_primitive, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BE03A, "func: asn1_f_asn1_find_end, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0BE089, "func: asn1_f_asn1_find_end, reason:asn1_r_missing_eoc, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06A03A, "func: asn1_f_asn1_collect, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06A0C5, "func: asn1_f_asn1_collect, reason:asn1_r_nested_asn1_string, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D06A089, "func: asn1_f_asn1_collect, reason:asn1_r_missing_eoc, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D08C041, "func: asn1_f_collect_data, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D068066, "func: asn1_f_asn1_check_tlen, reason:asn1_r_bad_object_header, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0680A8, "func: asn1_f_asn1_check_tlen, reason:asn1_r_wrong_tag, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D079041, "func: asn1_f_asn1_item_ex_combine_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D079064, "func: asn1_f_asn1_item_ex_combine_new, reason:asn1_r_aux_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D085041, "func: asn1_f_asn1_template_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04074007, "func: rsa_f_rsa_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04073041, "func: rsa_f_rsa_print, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A069007, "func: dsa_f_dsa_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A068065, "func: dsa_f_dsa_print, reason:dsa_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A068041, "func: dsa_f_dsa_print, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05065007, "func: dh_f_dhparams_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05064020, "func: dh_f_dhparams_print, reason:err_r_bio_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05064043, "func: dh_f_dhparams_print, reason:err_r_passed_null_parameter, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05064041, "func: dh_f_dhparams_print, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05064010, "func: dh_f_dhparams_print, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05064000, "func: dh_f_dhparams_print, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A065007, "func: dsa_f_dsaparams_print_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A064065, "func: dsa_f_dsaparams_print, reason:dsa_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A064041, "func: dsa_f_dsaparams_print, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A9041, "func: asn1_f_x509_crl_add0_revoked, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0AA041, "func: asn1_f_x509_info_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A6080, "func: asn1_f_long_c2i, reason:asn1_r_integer_too_large_for_long, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0AB041, "func: asn1_f_x509_name_ex_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D09E03A, "func: asn1_f_x509_name_ex_d2i, reason:err_r_nested_asn1_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0CB041, "func: asn1_f_x509_name_encode, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A5041, "func: asn1_f_i2d_rsa_pubkey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0A1041, "func: asn1_f_i2d_dsa_pubkey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0D0B5041, "func: asn1_f_i2d_ec_pubkey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20072041, "func: bio_f_buffer_ctrl, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006C041, "func: bio_f_bio_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006F079, "func: bio_f_bio_read, reason:bio_r_unsupported_method, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006F078, "func: bio_f_bio_read, reason:bio_r_uninitialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20071079, "func: bio_f_bio_write, reason:bio_r_unsupported_method, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20071078, "func: bio_f_bio_write, reason:bio_r_uninitialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006E079, "func: bio_f_bio_puts, reason:bio_r_unsupported_method, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006E078, "func: bio_f_bio_puts, reason:bio_r_uninitialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20068079, "func: bio_f_bio_gets, reason:bio_r_unsupported_method, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20068078, "func: bio_f_bio_gets, reason:bio_r_uninitialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20067079, "func: bio_f_bio_ctrl, reason:bio_r_unsupported_method, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20083079, "func: bio_f_bio_callback_ctrl, reason:bio_r_unsupported_method, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_02001FFF, "func: sys_f_fopen, reason:err_r_reason_syserr, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006D080, "func: bio_f_bio_new_file, reason:bio_r_no_such_file, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2006D002, "func: bio_f_bio_new_file, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0200BFFF, "func: sys_f_fread, reason:err_r_reason_syserr, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20082002, "func: bio_f_file_read, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20074065, "func: bio_f_file_ctrl, reason:bio_r_bad_fopen_mode, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20074002, "func: bio_f_file_ctrl, reason:err_r_sys_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2007E073, "func: bio_f_bio_new_mem_buf, reason:bio_r_null_parameter, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20075073, "func: bio_f_mem_write, reason:bio_r_null_parameter, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_2007507E, "func: bio_f_mem_write, reason:bio_r_write_to_read_only_bio, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03073064, "func: bn_f_bn_usub, reason:bn_r_arg2_lt_arg3, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03066041, "func: bn_f_bn_blinding_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306706B, "func: bn_f_bn_blinding_update, reason:bn_r_not_initialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306406B, "func: bn_f_bn_blinding_convert_ex, reason:bn_r_not_initialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306506B, "func: bn_f_bn_blinding_invert_ex, reason:bn_r_not_initialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306A041, "func: bn_f_bn_ctx_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308106D, "func: bn_f_bn_ctx_start, reason:bn_r_too_many_temporary_variables, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307406D, "func: bn_f_bn_ctx_get, reason:bn_r_too_many_temporary_variables, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306B067, "func: bn_f_bn_div, reason:bn_r_div_by_zero, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308A067, "func: bn_f_bn_div_no_branch, reason:bn_r_div_by_zero, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307B042, "func: bn_f_bn_exp, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307D042, "func: bn_f_bn_mod_exp_recp, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306D066, "func: bn_f_bn_mod_exp_mont, reason:bn_r_called_with_even_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307C066, "func: bn_f_bn_mod_exp_mont_consttime, reason:bn_r_called_with_even_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03075042, "func: bn_f_bn_mod_exp_mont_word, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03075066, "func: bn_f_bn_mod_exp_mont_word, reason:bn_r_called_with_even_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307E042, "func: bn_f_bn_mod_exp_simple, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03076066, "func: bn_f_bn_mod_exp2_mont, reason:bn_r_called_with_even_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0306E06C, "func: bn_f_bn_mod_inverse, reason:bn_r_no_inverse, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308B06C, "func: bn_f_bn_mod_inverse_no_branch, reason:bn_r_no_inverse, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308306A, "func: bn_f_bn_gf2m_mod, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308506A, "func: bn_f_bn_gf2m_mod_mul, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308806A, "func: bn_f_bn_gf2m_mod_sqr, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308406A, "func: bn_f_bn_gf2m_mod_exp, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308906A, "func: bn_f_bn_gf2m_mod_sqrt, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03087071, "func: bn_f_bn_gf2m_mod_solve_quad_arr, reason:bn_r_too_many_iterations, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03087074, "func: bn_f_bn_gf2m_mod_solve_quad_arr, reason:bn_r_no_solution, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0308606A, "func: bn_f_bn_gf2m_mod_solve_quad, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03071041, "func: bn_f_bn_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03078072, "func: bn_f_bn_expand_internal, reason:bn_r_bignum_too_long, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03078069, "func: bn_f_bn_expand_internal, reason:bn_r_expand_on_static_bignum_data, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03078041, "func: bn_f_bn_expand_internal, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307706E, "func: bn_f_bn_mod_lshift_quick, reason:bn_r_input_not_reduced, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307006A, "func: bn_f_bn_mpi2bn, reason:bn_r_invalid_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03070068, "func: bn_f_bn_mpi2bn, reason:bn_r_encoding_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03069041, "func: bn_f_bn_bn2hex, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03068041, "func: bn_f_bn_bn2dec, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307F041, "func: bn_f_bnrand, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307A073, "func: bn_f_bn_rand_range, reason:bn_r_invalid_range, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307A071, "func: bn_f_bn_rand_range, reason:bn_r_too_many_iterations, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03082065, "func: bn_f_bn_div_recp, reason:bn_r_bad_reciprocal, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03079070, "func: bn_f_bn_mod_sqrt, reason:bn_r_p_is_not_prime, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_03079071, "func: bn_f_bn_mod_sqrt, reason:bn_r_too_many_iterations, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0307906F, "func: bn_f_bn_mod_sqrt, reason:bn_r_not_a_square, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_07065041, "func: buf_f_buf_mem_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_07064041, "func: buf_f_buf_mem_grow, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_07069041, "func: buf_f_buf_mem_grow_clean, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_07068041, "func: buf_f_buf_strndup, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_07067041, "func: buf_f_buf_memdup, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0506A065, "func: dh_f_dh_builtin_genparams, reason:dh_r_bad_generator, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0506A003, "func: dh_f_dh_builtin_genparams, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05067003, "func: dh_f_generate_key, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05066067, "func: dh_f_compute_key, reason:dh_r_modulus_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05066064, "func: dh_f_compute_key, reason:dh_r_no_private_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05066066, "func: dh_f_compute_key, reason:dh_r_invalid_pubkey, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05066003, "func: dh_f_compute_key, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05069041, "func: dh_f_dh_new_method, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_05069026, "func: dh_f_dh_new_method, reason:err_r_engine_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A072041, "func: dsa_f_sig_cb, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A067041, "func: dsa_f_dsa_new_method, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A067026, "func: dsa_f_dsa_new_method, reason:err_r_engine_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A070003, "func: dsa_f_dsa_do_sign, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A070065, "func: dsa_f_dsa_do_sign, reason:dsa_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A070064, "func: dsa_f_dsa_do_sign, reason:dsa_r_data_too_large_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A070000, "func: dsa_f_dsa_do_sign, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A06B065, "func: dsa_f_dsa_sign_setup, reason:dsa_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A06B003, "func: dsa_f_dsa_sign_setup, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A071065, "func: dsa_f_dsa_do_verify, reason:dsa_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A071066, "func: dsa_f_dsa_do_verify, reason:dsa_r_bad_q_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A071067, "func: dsa_f_dsa_do_verify, reason:dsa_r_modulus_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0A071003, "func: dsa_f_dsa_do_verify, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06080086, "func: evp_f_evp_digestinit_ex, reason:evp_r_initialization_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0608008B, "func: evp_f_evp_digestinit_ex, reason:evp_r_no_digest_set, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606E06F, "func: evp_f_evp_md_ctx_copy_ex, reason:evp_r_input_not_initialized, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606E026, "func: evp_f_evp_md_ctx_copy_ex, reason:err_r_engine_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607B086, "func: evp_f_evp_cipherinit_ex, reason:evp_r_initialization_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607B041, "func: evp_f_evp_cipherinit_ex, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607B083, "func: evp_f_evp_cipherinit_ex, reason:evp_r_no_cipher_set, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607F08A, "func: evp_f_evp_encryptfinal_ex, reason:evp_r_data_not_multiple_of_block_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606508A, "func: evp_f_evp_decryptfinal_ex, reason:evp_r_data_not_multiple_of_block_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606506D, "func: evp_f_evp_decryptfinal_ex, reason:evp_r_wrong_final_block_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06065064, "func: evp_f_evp_decryptfinal_ex, reason:evp_r_bad_decrypt, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607A082, "func: evp_f_evp_cipher_ctx_set_key_length, reason:evp_r_invalid_key_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607C083, "func: evp_f_evp_cipher_ctx_ctrl, reason:evp_r_no_cipher_set, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607C084, "func: evp_f_evp_cipher_ctx_ctrl, reason:evp_r_ctrl_not_implemented, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607C085, "func: evp_f_evp_cipher_ctx_ctrl, reason:evp_r_ctrl_operation_not_implemented, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06074079, "func: evp_f_evp_pbe_cipherinit, reason:evp_r_unknown_pbe_algorithm, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06074078, "func: evp_f_evp_pbe_cipherinit, reason:evp_r_keygen_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06073041, "func: evp_f_evp_pbe_alg_add, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606F041, "func: evp_f_evp_pkcs82pkey, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606F072, "func: evp_f_evp_pkcs82pkey, reason:evp_r_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606F070, "func: evp_f_evp_pkcs82pkey, reason:evp_r_bn_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606F071, "func: evp_f_evp_pkcs82pkey, reason:evp_r_bn_pubkey_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606F010, "func: evp_f_evp_pkcs82pkey, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606F076, "func: evp_f_evp_pkcs82pkey, reason:evp_r_unsupported_private_key_algorithm, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06071041, "func: evp_f_evp_pkey2pkcs8_broken, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06071076, "func: evp_f_evp_pkey2pkcs8_broken, reason:evp_r_unsupported_private_key_algorithm, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06070075, "func: evp_f_pkcs8_set_broken, reason:evp_r_pkcs8_unknown_broken_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06087041, "func: evp_f_dsa_pkey2pkcs8, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06087073, "func: evp_f_dsa_pkey2pkcs8, reason:evp_r_encode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06084067, "func: evp_f_eckey_pkey2pkcs8, reason:evp_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06084041, "func: evp_f_eckey_pkey2pkcs8, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06084010, "func: evp_f_eckey_pkey2pkcs8, reason:err_r_ec_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0608400D, "func: evp_f_eckey_pkey2pkcs8, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06084073, "func: evp_f_eckey_pkey2pkcs8, reason:evp_r_encode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0608508F, "func: evp_f_aes_init_key, reason:evp_r_aes_key_setup_failed, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06075072, "func: evp_f_pkcs5_pbe_keyivgen, reason:evp_r_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06076072, "func: evp_f_pkcs5_v2_pbe_keyivgen, reason:evp_r_decode_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607607D, "func: evp_f_pkcs5_v2_pbe_keyivgen, reason:evp_r_unsupported_prf, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606806A, "func: evp_f_evp_pkey_decrypt, reason:evp_r_public_key_not_rsa, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606906A, "func: evp_f_evp_pkey_encrypt, reason:evp_r_public_key_not_rsa, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06067065, "func: evp_f_evp_pkey_copy_parameters, reason:evp_r_different_key_types, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06067067, "func: evp_f_evp_pkey_copy_parameters, reason:evp_r_missing_parameters, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606A041, "func: evp_f_evp_pkey_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0607907F, "func: evp_f_evp_pkey_get1_rsa, reason:evp_r_expecting_an_rsa_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06078081, "func: evp_f_evp_pkey_get1_dsa, reason:evp_r_expecting_a_dsa_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0608308E, "func: evp_f_evp_pkey_get1_ec_key, reason:evp_r_expecting_a_ec_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06077080, "func: evp_f_evp_pkey_get1_dh, reason:evp_r_expecting_a_dh_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606606A, "func: evp_f_evp_openinit, reason:evp_r_public_key_not_rsa, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_06066041, "func: evp_f_evp_openinit, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606B06E, "func: evp_f_evp_signfinal, reason:evp_r_wrong_public_key_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606B068, "func: evp_f_evp_signfinal, reason:evp_r_no_sign_function_configured, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606C06E, "func: evp_f_evp_verifyfinal, reason:evp_r_wrong_public_key_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0606C069, "func: evp_f_evp_verifyfinal, reason:evp_r_no_verify_function_configured, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_08069041, "func: obj_f_obj_add_object, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_08067065, "func: obj_f_obj_nid2obj, reason:obj_r_unknown_nid, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_08068065, "func: obj_f_obj_nid2sn, reason:obj_r_unknown_nid, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_08066065, "func: obj_f_obj_nid2ln, reason:obj_r_unknown_nid, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_08064041, "func: obj_f_obj_create, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0806500D, "func: obj_f_obj_dup, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_08065041, "func: obj_f_obj_dup, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0806A041, "func: obj_f_obj_name_new_index, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09073007, "func: pem_f_pem_x509_info_read, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09074041, "func: pem_f_pem_x509_info_read_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0907400D, "func: pem_f_pem_x509_info_read_bio, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09075071, "func: pem_f_pem_x509_info_write_bio, reason:pem_r_unsupported_cipher, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09064042, "func: pem_f_pem_def_callback, reason:err_r_should_not_have_been_called, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906406D, "func: pem_f_pem_def_callback, reason:pem_r_problems_getting_password, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09066007, "func: pem_f_pem_asn1_read, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09068007, "func: pem_f_pem_asn1_write, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09069071, "func: pem_f_pem_asn1_write_bio, reason:pem_r_unsupported_cipher, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906900D, "func: pem_f_pem_asn1_write_bio, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09069041, "func: pem_f_pem_asn1_write_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906906F, "func: pem_f_pem_asn1_write_bio, reason:pem_r_read_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906A068, "func: pem_f_pem_do_header, reason:pem_r_bad_password_read, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906A065, "func: pem_f_pem_do_header, reason:pem_r_bad_decrypt, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906B06B, "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_not_proc_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906B06A, "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_not_encrypted, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906B070, "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_short_header, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906B069, "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_not_dek_info, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906B072, "func: pem_f_pem_get_evp_cipher_info, reason:pem_r_unsupported_encryption, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09065067, "func: pem_f_load_iv, reason:pem_r_bad_iv_chars, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09071007, "func: pem_f_pem_write, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09072007, "func: pem_f_pem_write_bio, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09072041, "func: pem_f_pem_write_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09072000, "func: pem_f_pem_write_bio, reason:err_r_reason_none, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906C007, "func: pem_f_pem_read, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906D041, "func: pem_f_pem_read_bio, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906D06C, "func: pem_f_pem_read_bio, reason:pem_r_no_start_line, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906D066, "func: pem_f_pem_read_bio, reason:pem_r_bad_end_line, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906D064, "func: pem_f_pem_read_bio, reason:pem_r_bad_base64_decode, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906700D, "func: pem_f_pem_asn1_read_bio, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0907E06F, "func: pem_f_do_pk8pkey, reason:pem_r_read_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09078068, "func: pem_f_d2i_pkcs8privatekey_bio, reason:pem_r_bad_password_read, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0907D007, "func: pem_f_do_pk8pkey_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09079007, "func: pem_f_d2i_pkcs8privatekey_fp, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0907B00D, "func: pem_f_pem_read_bio_privatekey, reason:err_r_asn1_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0907C007, "func: pem_f_pem_read_privatekey, reason:err_r_buf_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906F06E, "func: pem_f_pem_sealinit, reason:pem_r_public_key_no_rsa, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906F041, "func: pem_f_pem_sealinit, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906E06E, "func: pem_f_pem_sealfinal, reason:pem_r_public_key_no_rsa, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0906E041, "func: pem_f_pem_sealfinal, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_09070041, "func: pem_f_pem_signfinal, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_24064064, "func: rand_f_ssleay_rand_bytes, reason:rand_r_prng_not_seeded, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407B041, "func: rsa_f_rsa_check_key, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407B080, "func: rsa_f_rsa_check_key, reason:rsa_r_p_not_prime, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407B081, "func: rsa_f_rsa_check_key, reason:rsa_r_q_not_prime, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407B07F, "func: rsa_f_rsa_check_key, reason:rsa_r_n_does_not_equal_p_q, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407B07B, "func: rsa_f_rsa_check_key, reason:rsa_r_d_e_not_congruent_to_1, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04068069, "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_modulus_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04068065, "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_bad_e_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04068041, "func: rsa_f_rsa_eay_public_encrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04068076, "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04068084, "func: rsa_f_rsa_eay_public_encrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04066041, "func: rsa_f_rsa_eay_private_encrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04066076, "func: rsa_f_rsa_eay_private_encrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04066084, "func: rsa_f_rsa_eay_private_encrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04066044, "func: rsa_f_rsa_eay_private_encrypt, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04065041, "func: rsa_f_rsa_eay_private_decrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406506C, "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_data_greater_than_mod_len, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04065084, "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04065044, "func: rsa_f_rsa_eay_private_decrypt, reason:err_r_internal_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04065076, "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04065072, "func: rsa_f_rsa_eay_private_decrypt, reason:rsa_r_padding_check_failed, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04067069, "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_modulus_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04067065, "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_bad_e_value, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04067041, "func: rsa_f_rsa_eay_public_decrypt, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406706C, "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_data_greater_than_mod_len, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04067084, "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_data_too_large_for_modulus, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04067076, "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_unknown_padding_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04067072, "func: rsa_f_rsa_eay_public_decrypt, reason:rsa_r_padding_check_failed, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04081078, "func: rsa_f_rsa_builtin_keygen, reason:rsa_r_key_size_too_small, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04081003, "func: rsa_f_rsa_builtin_keygen, reason:err_lib_bn, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406A041, "func: rsa_f_rsa_new_method, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406A026, "func: rsa_f_rsa_new_method, reason:err_r_engine_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04088041, "func: rsa_f_rsa_setup_blinding, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0408808C, "func: rsa_f_rsa_setup_blinding, reason:rsa_r_no_public_exponent, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04088003, "func: rsa_f_rsa_setup_blinding, reason:err_r_bn_lib, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04082041, "func: rsa_f_rsa_memory_lock, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406B06E, "func: rsa_f_rsa_padding_add_none, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406B07A, "func: rsa_f_rsa_padding_add_none, reason:rsa_r_data_too_small_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406F06D, "func: rsa_f_rsa_padding_check_none, reason:rsa_r_data_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04087082, "func: rsa_f_rsa_null_public_encrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04085082, "func: rsa_f_rsa_null_private_encrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04084082, "func: rsa_f_rsa_null_private_decrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04086082, "func: rsa_f_rsa_null_public_decrypt, reason:rsa_r_rsa_operations_not_supported, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04079078, "func: rsa_f_rsa_padding_add_pkcs1_oaep, reason:rsa_r_key_size_too_small, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04079041, "func: rsa_f_rsa_padding_add_pkcs1_oaep, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407A041, "func: rsa_f_rsa_padding_check_pkcs1_oaep, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407A06D, "func: rsa_f_rsa_padding_check_pkcs1_oaep, reason:rsa_r_data_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407A079, "func: rsa_f_rsa_padding_check_pkcs1_oaep, reason:rsa_r_oaep_decoding_error, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406C06E, "func: rsa_f_rsa_padding_add_pkcs1_type_1, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407006A, "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_block_type_is_not_01, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04070066, "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_bad_fixed_header_decrypt, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04070071, "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_null_before_block_missing, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04070067, "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_bad_pad_byte_count, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407006D, "func: rsa_f_rsa_padding_check_pkcs1_type_1, reason:rsa_r_data_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406D06E, "func: rsa_f_rsa_padding_add_pkcs1_type_2, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407106B, "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_block_type_is_not_02, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04071071, "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_null_before_block_missing, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04071067, "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_bad_pad_byte_count, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407106D, "func: rsa_f_rsa_padding_check_pkcs1_type_2, reason:rsa_r_data_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E088, "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_slen_check_failed, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E085, "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_first_octet_invalid, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E06D, "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_data_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E086, "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_last_octet_invalid, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E041, "func: rsa_f_rsa_verify_pkcs1_pss, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E087, "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_slen_recovery_failed, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407E068, "func: rsa_f_rsa_verify_pkcs1_pss, reason:rsa_r_bad_signature, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407D088, "func: rsa_f_rsa_padding_add_pkcs1_pss, reason:rsa_r_slen_check_failed, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04076070, "func: rsa_f_rsa_sign_asn1_octet_string, reason:rsa_r_digest_too_big_for_rsa_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04076041, "func: rsa_f_rsa_sign_asn1_octet_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04078077, "func: rsa_f_rsa_verify_asn1_octet_string, reason:rsa_r_wrong_signature_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04078041, "func: rsa_f_rsa_verify_asn1_octet_string, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04078068, "func: rsa_f_rsa_verify_asn1_octet_string, reason:rsa_r_bad_signature, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04075083, "func: rsa_f_rsa_sign, reason:rsa_r_invalid_message_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04075075, "func: rsa_f_rsa_sign, reason:rsa_r_unknown_algorithm_type, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04075074, "func: rsa_f_rsa_sign, reason:rsa_r_the_asn1_object_identifier_is_not_known_for_this_md, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04075070, "func: rsa_f_rsa_sign, reason:rsa_r_digest_too_big_for_rsa_key, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04075041, "func: rsa_f_rsa_sign, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04077077, "func: rsa_f_rsa_verify, reason:rsa_r_wrong_signature_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04077041, "func: rsa_f_rsa_verify, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04077083, "func: rsa_f_rsa_verify, reason:rsa_r_invalid_message_length, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04077068, "func: rsa_f_rsa_verify, reason:rsa_r_bad_signature, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0406E06E, "func: rsa_f_rsa_padding_add_sslv23, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407206F, "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_data_too_small, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407206B, "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_block_type_is_not_02, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04072071, "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_null_before_block_missing, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04072073, "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_sslv3_rollback_attack, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407206D, "func: rsa_f_rsa_padding_check_sslv23, reason:rsa_r_data_too_large, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0407F06E, "func: rsa_f_rsa_padding_add_x931, reason:rsa_r_data_too_large_for_key_size, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_04080089, "func: rsa_f_rsa_padding_check_x931, reason:rsa_r_invalid_header, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0408008A, "func: rsa_f_rsa_padding_check_x931, reason:rsa_r_invalid_padding, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0408008B, "func: rsa_f_rsa_padding_check_x931, reason:rsa_r_invalid_trailer, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_20076041, "func: bio_f_ssl_new, reason:err_r_malloc_failure, file:%s, line:%d")
    TRC_MSG(CRYPTO_TRC_0200AFFF, "func: sys_f_opendir, reason:err_r_reason_syserr, file:%s, line:%d")

END_TRACE_MAP(MOD_OSSL_CRYPTO)

#endif /* __AGPS_SWIP_REL__ */

#endif /* !_CRYPTO_TRC_H_ */
