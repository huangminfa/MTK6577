/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE"
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
 *   ossl_pki_adp_trc.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   This is openssl pki adaptation trace map definition.
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
 * Nov 24 2009 mtk01264
 * [MAUI_01996819] [OCSP] Check-in OCSP for OpenSSL solution
 * Add OCSP function in PKI wrapper
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
#ifndef _OSSL_PKI_ADP_TRC_H_
#define _OSSL_PKI_ADP_TRC_H_

#if 0
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
#endif

#if __AGPS_SWIP_REL__

    /* ----------------- TRACE_FUNC trace class ------------------- */
    /* pki.c */
    // #define OSSL_PKI_LIB_INIT "pki_lib_init()"   
    /* pki_pem.c */
    #define OSSL_PKI_PEM_DECODE_BY_INDEX "pki_pem_decode_by_index(), index : %d"   
    #define OSSL_PKI_PEM_ENCODE "pki_pem_encode()"   

    /* ----------------- TRACE_STATE trace class ------------------- */

    /* ----------------- TRACE_INFO trace class ------------------- */
    #define OSSL_PKI_OCSP_VERIFY_STATUS_GOOD "Certificate is good, to check timestamp validity"   
    #define OSSL_PKI_OCSP_VERIFY_THISUPDATE "This update: %d/%d/%d %d:%d:%d"
    #define OSSL_PKI_OCSP_VERIFY_NEXTUPDATE "Next update: %d/%d/%d %d:%d:%d"
    #define OSSL_PKI_OCSP_VERIFY_REVOKEDATE "Revoked at: %d/%d/%d %d:%d:%d"
    #define OSSL_PKI_OCSP_VERIFY_NOW "Now: %d/%d/%d %d:%d:%d, nsec=%d, maxage=%d"   

    /* ----------------- TRACE_WARNING trace class ------------------- */
    /* pki_pem.c */
    #define OSSL_PKI_PEM_DECODE_BY_INDEX_NOT_FOUND "pki_pem_decode_by_index() -- can't find cert"   
    #define OSSL_PKI_PEM_ENCODE_FAILED "pki_pem_encode() -- PEM write bio failed"   
    #define OSSL_PKI_OCSP_VERIFY_STATUS_UNKNOWN "Certificate status is unknown"   
    #define OSSL_PKI_OCSP_MULTIPLE_OCSP_URL_IN_CERT "Warning: %d OCSP url in cert"   

    /* ----------------- TRACE_ERROR trace class ------------------- */
    #define OSSL_PKI_OCSP_VERIFY_NO_CERT_ID "No cert ID in OCSP request"   
    #define OSSL_PKI_OCSP_VERIFY_NO_STATUS "No cert status of the query cert id in OCSP response"   
    #define OSSL_PKI_OCSP_VERIFY_STATUS_REVOKED "Certificate is revoked for %d"   
    #define OSSL_PKI_OCSP_VERIFY_VALIDITY "OCSP status time is invalid"   
    #define OSSL_PKI_OCSP_ADD_NO_CERT "OCSP: no issuer certificate specified"
    #define OSSL_PKI_OCSP_CREATE_REQ_FAILED "OCSP: error creating OCSP request"
    #define OSSL_PKI_OCSP_CERT_VERIFY_CALLBACK "OCSP verify cert: depth=%d, ok=%d, warning=%d"

    /* ----------------- TRACE_GROUP_1() trace class ------------------- */

    /* ----------------- TRACE_GROUP_2() trace class ------------------- */
#else
BEGIN_TRACE_MAP(MOD_OSSL_PKIADP)

    /* ----------------- TRACE_FUNC trace class ------------------- */
    /* pki.c */
    // TRC_MSG(OSSL_PKI_LIB_INIT, "pki_lib_init()")   
    /* pki_pem.c */
    TRC_MSG(OSSL_PKI_PEM_DECODE_BY_INDEX, "pki_pem_decode_by_index(), index : %d")   
    TRC_MSG(OSSL_PKI_PEM_ENCODE, "pki_pem_encode()")   


    /* ----------------- TRACE_STATE trace class ------------------- */

    /* ----------------- TRACE_INFO trace class ------------------- */
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_STATUS_GOOD, "Certificate is good, to check timestamp validity")   
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_THISUPDATE, "This update: %d/%d/%d %d:%d:%d")
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_NEXTUPDATE, "Next update: %d/%d/%d %d:%d:%d")
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_REVOKEDATE, "Revoked at: %d/%d/%d %d:%d:%d")
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_NOW, "Now: %d/%d/%d %d:%d:%d, nsec=%d, maxage=%d")   

    /* ----------------- TRACE_WARNING trace class ------------------- */
    /* pki_pem.c */
    TRC_MSG(OSSL_PKI_PEM_DECODE_BY_INDEX_NOT_FOUND, "pki_pem_decode_by_index() -- can't find cert")   
    TRC_MSG(OSSL_PKI_PEM_ENCODE_FAILED, "pki_pem_encode() -- PEM write bio failed")   
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_STATUS_UNKNOWN, "Certificate status is unknown")   
    TRC_MSG(OSSL_PKI_OCSP_MULTIPLE_OCSP_URL_IN_CERT, "Warning: %d OCSP url in cert")   

    /* ----------------- TRACE_ERROR trace class ------------------- */
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_NO_CERT_ID, "No cert ID in OCSP request")   
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_NO_STATUS, "No cert status of the query cert id in OCSP response")   
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_STATUS_REVOKED, "Certificate is revoked for %d")   
    TRC_MSG(OSSL_PKI_OCSP_VERIFY_VALIDITY, "OCSP status time is invalid")   
    TRC_MSG(OSSL_PKI_OCSP_ADD_NO_CERT, "OCSP: no issuer certificate specified")
    TRC_MSG(OSSL_PKI_OCSP_CREATE_REQ_FAILED, "OCSP: error creating OCSP request")
    TRC_MSG(OSSL_PKI_OCSP_CERT_VERIFY_CALLBACK, "OCSP verify cert: depth=%d, ok=%d, warning=%d")

    /* ----------------- TRACE_GROUP_1() trace class ------------------- */

    /* ----------------- TRACE_GROUP_2() trace class ------------------- */

END_TRACE_MAP(MOD_OSSL_PKIADP)

#endif /* __AGPS_SWIP_REL__ */

#endif /* !_OSSL_PKI_ADP_TRC_H_ */


