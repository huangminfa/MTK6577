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
 * BesLoudness_exp.h
 *
 * Project:
 * --------
 * SWIP
 *
 * Description:
 * ------------
 * BesLoudness interface
 *
 * Author:
 * -------
 * HP Cheng
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision$
 * $Modtime$
 * $Log$
 *
 * 10 07 2010 richie.hsieh
 * [WCPSP00000522] [BesSound SWIP] Assertion removal
 * .
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

/* interface functions */
#ifndef BLOUDEXP_H
#define BLOUDEXP_H

#include "BesSound_exp.h"

/* NULL definition */
#ifndef NULL
#define NULL    0
#endif

typedef struct {
    unsigned int HSF_Coeff[9][4];    //high pass filter coefficient (same as before)
    unsigned int BPF_Coeff[4][6][3]; //band pass filter coefficient (same as before)
    unsigned int DRC_Forget_Table[9][2]; //DRC forget table, (預留用) 若全帶0, 則使用內建default值
    unsigned int WS_Gain_Max; // WS MAX Gain, (預留用) 若帶0, 則使用內建default值
    unsigned int WS_Gain_Min; // WS MIN Gain, (預留用) 若帶0, 則使用內建default值
    unsigned int Filter_First; // 0: DRC First, 1: Filter First
    char Gain_Map_In[5];  // in DB, 若全帶0, Gain_Map_In, Gain_Map_Out 使用內建default值
    char Gain_Map_Out[5];               // in DB
}BLOUD_CustomParam;

/* Structure definition */
typedef struct {
    unsigned int Channel_Num;   // Channel Number: 1,2
    unsigned int Sample_Rate;   // Sampling Rate: 8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000
    unsigned int Filter_Mode;   // Filter mode: 0: disable filter, 1: compensation filter, 2: loudness filter
    unsigned int Loudness_Mode; // gain mapping mode: 0: Disable, 1: DRC, 2: DRC + Wave Shaping, 3: DRC + Agressive Wave Shaping
    BLOUD_CustomParam *pCustom_Param; // 帶入custom parameter structure的 pointer
}BLOUD_InitParam;
// Note that only when Filter_Mode == 2, Wave_Shaping could be set to 1 or 2
// Note that when Filter_Mode == 0, Gain_Mapping should not = 0

int BLOUD_SetHandle(BS_Handle *pHandle);

#endif
