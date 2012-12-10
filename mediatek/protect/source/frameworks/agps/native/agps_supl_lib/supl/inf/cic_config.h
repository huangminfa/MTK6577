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
 * cic_config.h
 *
 * Project:
 * --------
 *   MT6208
 *
 * Description:
 * ------------
 *   Global config file for Certicom packages.
 *
 * Author:
 * -------
 * Wyatt Sun
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
 * Jul 16 2007 MTK01264
 * [MAUI_00417116] [SSL] Check in  SSL solution with low-level APIs
 * Check in sslplus5
 *
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef _CIC_CONFIG_H_
#define _CIC_CONFIG_H_

#define _MTK_SSL_CALLBACK_ 1

#define SB_LITTLE_ENDIAN 1

#define _MTK_DATA_TYPES_ 1

#define SB_LINKED_MEM_CB 1
// #define USE_SBGSE 1
#define HURELEASE 1

#define ARM7 1
#define SB_YIELD_LEVEL 8
//#define NO_SEED_ENTROPY (1)
#define ISB_DES_ROLL 1
#define SB_ARC4_PROCESS_SBWORD 1
#define SB_ZMOD_USE_MONTGOMERY 1
#define SB_Z_64PAD 1
#define SB_ARM7 1
// #define SB_USE_FAR_FUNCS 1
#define SB_NOMEMLOCK 1
#define USE_PSS 1
// #define NDEBUG 1
#define DEBUG 1
#define HU_MINOR 1
// #define USE_TP_PSS 1  // compile error: sigParams not found
// #define _MSC_VER 1
#define SB_MAJOR 1
// #define TP_PBE_P12_AES128_SHA256_ID 1

#define DTLSRAD_NOT_SUPPORTED 1

#define _NO_STDINT_H 1
#define _NO_LIMITS_H 1

// #define NO_GSE_MAPPINGS (1) // compile error: SSL_PRIV_HU_TAG_SB not defined


#endif /* !_CIC_CONFIG_H_ */


