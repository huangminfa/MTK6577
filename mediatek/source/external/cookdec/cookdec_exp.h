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
 *
 * Filename:
 * ---------
 *   cookdec_exp.h
 *
 * Project:
 * --------
 *   
 *
 * Description:
 * ------------
 *   The Cook Decoder Interface
 *
 * Author:
 * -------
 *   Morris Yang (mtk03147)
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/
#ifndef _COOKDEC_EXP_H
#define _COOKDEC_EXP_H

typedef unsigned char UINT8;
typedef UINT8 BYTE;
typedef unsigned short int UINT16;
typedef unsigned int UINT32;

typedef int HX_RESULT;

#define MAKE_HX_FACILITY_RESULT(sev,fac,code) \
    ((HX_RESULT) (((UINT32)(sev) << 31) | ((UINT32)(fac)<<16) | ((UINT32)(code))))
    
#define HXR_OK MAKE_HX_FACILITY_RESULT(0,0,0)        /* 00000000 */

typedef struct _tagRA_CODEC_SPECIFIC_DATA {
    // ra_format_info
    UINT32 ulSampleRate;
    UINT32 ulActualRate;
    UINT16 usBitsPerSample;
    UINT16 usNumChannels;
    UINT16 usAudioQuality;
    UINT16 usFlavorIndex;
    UINT32 ulBitsPerFrame;
    UINT32 ulGranularity;
    // COOK_decParam
   int nSamples;
   int nChannels; 
   int nRegions; 
   int nFrameBits; 
   int sampRate; 
   int cplStart; 
   int cplQbits;    
} RA_CODEC_SPECIFIC_DATA;


/* Structure Definition */
typedef struct {
   void        *cook_dec_data;
} COOK_DEC_HANDLE;


typedef struct {
   int          nSamples;
   int          nChannels; 
   int          nRegions; 
   int          nFrameBits; 
   int          sampRate; 
   int          cplStart; 
   int          cplQbits;
} COOK_decParam;


/* Interface Functions */
void CokDec_GetBufferSize (
	unsigned int *internal_buf_size, 
	unsigned int *bs_buf_size,
	unsigned int *pcm_buf_size);

COOK_DEC_HANDLE *CokDec_InitDecoder (
	void              *internal_buf, 
	COOK_decParam     *pCodecInfo
);

int CokDec_Decode(
	COOK_DEC_HANDLE  *cook_dec_hdl,
	const void       *inbuf,
	unsigned int     *cunsumedBytes, 
	void             *outbuf, 
	unsigned int     *decodeSamples, 
	int              lostflag
);


#endif
