/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2009
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
 * AudioCustParam.h
 *
 * Project:
 * --------
 *   Android
 *
 * Description:
 * ------------
 *   This file implements custom parameter setting.
 *
 * Author:
 * -------
 *   HP Cheng (mtk01752)
 *
 *******************************************************************************/

#ifndef _AUDIO_CUST_PARAM_H_
#define _AUDIO_CUST_PARAM_H_


/*=============================================================================
 *                              Include Files
 *===========================================================================*/
#include "CFG_AUDIO_File.h"
#include <utils/Log.h>
#include <utils/String8.h>


namespace android {

/*=============================================================================
 *                              Class definition
 *===========================================================================*/

bool checkNvramReady(void);

// functions
int GetCustParamFromNV(AUDIO_CUSTOM_PARAM_STRUCT *pPara);

int SetCustParamToNV(AUDIO_CUSTOM_PARAM_STRUCT *pPara);

// WB Speech functions
int GetCustWBParamFromNV(AUDIO_CUSTOM_WB_PARAM_STRUCT * pPara);

int SetCustWBParamToNV(AUDIO_CUSTOM_WB_PARAM_STRUCT *pPara);

// get med param parameter from NVRAM
int GetMedParamFromNV(AUDIO_PARAM_MED_STRUCT *pPara);

int SetMedParamToNV(AUDIO_PARAM_MED_STRUCT *pPara);

// get audio custom parameter from NVRAM
int GetAudioCustomParamFromNV(AUDIO_VOLUME_CUSTOM_STRUCT *pPara);

int SetAudioCustomParamToNV(AUDIO_VOLUME_CUSTOM_STRUCT *pPara);

// get audio custom parameter from NVRAM
int GetAudioGainTableParamFromNV(AUDIO_GAIN_TABLE_STRUCT *pPara);

int SetAudioGainTableParamToNV(AUDIO_GAIN_TABLE_STRUCT *pPara);


int Read_DualMic_CustomParam_From_NVRAM(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *pPara);
int Write_DualMic_CustomParam_To_NVRAM(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *pPara);

#if defined(MTK_AUDIO_HD_REC_SUPPORT)
// Get/Set Audio HD record parameters from/to NVRAM
int GetHdRecordParamFromNV(AUDIO_HD_RECORD_PARAM_STRUCT *pPara);
int SetHdRecordParamToNV(AUDIO_HD_RECORD_PARAM_STRUCT *pPara);

// Get/Set Audio HD record scene table from/to NVRAM
int GetHdRecordSceneTableFromNV(AUDIO_HD_RECORD_SCENE_TABLE_STRUCT *pPara);
int SetHdRecordSceneTableToNV(AUDIO_HD_RECORD_SCENE_TABLE_STRUCT *pPara);
#endif


}; // namespace android

#endif  //_AUDIO_YUSU_CUST_PARAM_H_




