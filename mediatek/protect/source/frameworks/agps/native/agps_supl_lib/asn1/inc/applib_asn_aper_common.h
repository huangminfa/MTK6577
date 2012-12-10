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
 * applib_asn_aper_common.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   This file is header file for APER common function.
 *
 * Author:
 * -------
 * Wayne Chen (mtk01370)
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
 * Apr 17 2008 mtk01370
 * [MAUI_00758127] [ASN.1] Add ASN.1 in Applib task
 * 
 *
 * Apr 17 2008 mtk01370
 * [MAUI_00758127] [ASN.1] Add ASN.1 in Applib task
 * 
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

#if !defined _APER_H_
#define _APER_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "applib_asn_common.h"
#include "applib_asn_memory.h"
#include "applib_mtkasn_global.h"


extern void AperEncodeOID(applib_AsnContext *pContext,  applib_OID *pOid);
extern void AperDecodeOID(applib_AsnContext *pContext,  applib_OID *pOid);

extern void AperEncodeBitString(applib_AsnContext *pContext, U32 lBound, U32 uBound, U8 *pData, U32 uLen);
extern U32  AperDecodeBitString(applib_AsnContext *pContext, U32 lBound, U32 uBound, U8 *pData);

extern void AperEncodeOpenType(applib_AsnContext *pContext,  U8 * pData, U32 uSize);
extern U32 AperDecodeOpenTypeLength(applib_AsnContext *pContext);
extern U32 AperDecodeOpenType(applib_AsnContext *pContext,  U8 * pData, U32 uSize);

extern void AperPutNonConstraintNumber(applib_AsnContext *pContext,  S32 uNum);
extern S32 AperGetNonConstraintNumber(applib_AsnContext *pContext);

extern U32 AperPutLengthValue(applib_AsnContext *pContext, U32 lBound, U32 uBound, U32 uLen);
extern U32 AperGetLengthValue(applib_AsnContext *pContext, U32 lBound, U32 uBound);

extern void AperPutSmallNumber(applib_AsnContext *pContext, U32 uNum);
extern U32 AperGetSmallNumber(applib_AsnContext *pContext);

extern void AperPutSemiConstraintNumber(applib_AsnContext *pContext, S32 lBound, S32 uNum);
extern S32 AperGetSemiConstraintNumber(applib_AsnContext *pContext, S32 lBound);

extern void AperPutWholeConstraintNumber(applib_AsnContext *pContext, S32 lBound, S32 uBound, U32 uNum);
extern U32 AperGetWholeConstraintNumber(applib_AsnContext *pContext, S32 lBound, S32 uBound);


// APER STRING Encode function
extern void  AperEncodeGeneralString(applib_AsnContext *pContext,  applib_asn_GeneralString *pStr);
extern void  AperEncodeBMPString(applib_AsnContext *pContext,  applib_asn_BMPString *pStr, U32 minSize, U32 maxSize, applib_ASN_TwoByteAlphabet  *pAlphabet);
extern void  AperEncodeIA5String(applib_AsnContext *pContext,  applib_asn_IA5String *pStr, U32 minSize, U32 maxSize, applib_ASN_OneByteAlphabet  *pAlphabet);
extern void  AperEncodeNumericString(applib_AsnContext *pContext,  applib_asn_NumericString *pStr, U32 minSize, U32 maxSize, applib_ASN_OneByteAlphabet  *pAlphabet);
extern void  AperDecodeNumericStringA(applib_AsnContext *pContext,  applib_asn_NumericString *pStr, U32 minSize, U32 maxSize, applib_ASN_OneByteAlphabet  *pAlphabet);
extern void	AperDecodeIA5StringA(applib_AsnContext *pContext,  applib_asn_IA5String *pStr, U32 minSize, U32 maxSize, applib_ASN_OneByteAlphabet  *pAlphabet);
extern void	AperDecodeBMPStringA(applib_AsnContext *pContext,  applib_asn_BMPString *pStr, U32 minSize, U32 maxSize, applib_ASN_TwoByteAlphabet  *pAlphabet);
extern void	AperDecodeGeneralStringA(applib_AsnContext *pContext,  applib_asn_GeneralString *pStr);
extern void	AperDecodeChoiceUnKnowItem(applib_AsnContext *pContext, applib_ChoiceUnKnow *pUnKnowItem);
extern void	AperDecodeSequcenceUnKnowItem(applib_AsnContext* pContext,  applib_UnKnowExtensionItemArray *pArray, U32	uSize, Bool  *pIsItemExist);
extern S32	GetAlphabetIndex(applib_ASN_OneByteAlphabet *pAlphabet, char *pChar);

extern  U32  GetNumberBitLength(U32  Data);
extern  Bool IsReindexOneByteAlphabet(applib_ASN_OneByteAlphabet *pAlphabet);
extern  U32  GetOneByteCharacterBitWidth(applib_ASN_OneByteAlphabet *pAlphabet);

	
#ifdef __cplusplus       
}
#endif
#endif //_APER_H_
