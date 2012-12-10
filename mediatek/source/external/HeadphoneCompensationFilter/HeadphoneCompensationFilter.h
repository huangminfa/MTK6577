/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
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
 * HeadphoneCompensationFilter.h
 *
 * Project:
 * --------
 *   Android
 *
 * Description:
 * ------------
 *   This file implements Headphone Compensation Filter
 *
 * Author:
 * -------
 *   Tina Tsai (mtk01981)
 *
 *------------------------------------------------------------------------------
 * $Revision: #2 $
 * $Modtime:$
 * $Log:$
 *
 *******************************************************************************/

#ifndef _HEADPHONE_COMPENSATION_FILTER_H_
#define _HEADPHONE_COMPENSATION_FILTER_H_

#include <stdint.h>
#include <sys/types.h>
#include <cutils/log.h>
#include "CFG_AUDIO_File.h"

extern "C" {
#include "../bessound/BesSound_exp.h"
#include "../bessound/BesLoudness_exp.h"
}

#define FRAMELEN 512

//#define ENABLE_LOG_BESLOUDNESS

namespace android
{
// ----------------------------------------------------------------------------

class HeadphoneCompensationFilter
{
public:
    HeadphoneCompensationFilter();
    bool Init(void) ;
    void Deinit(void);
    void LoadHCFParameter();
    void SetHCFPreviewParameter(AUDIO_ACF_CUSTOM_PARAM_STRUCT* PreviewParam);
    void SetWorkMode(uint32_t chNum, uint32_t smpRate, uint32_t workMode);
    bool Start(void);
    bool Stop(void);
    void ResetBuffer(void);
    void Process(const short *pInputBuffer, int *InputSampleCount, short *pOutputBuffer, int *OutputSampleCount);
    ~HeadphoneCompensationFilter();
    BS_Handle *mpBloudHandle;  // loudness handle
    AUDIO_ACF_CUSTOM_PARAM_STRUCT audioParam;

private:
    char *mpWorkingBuf;  // ACF working buffer
    short *mpHCFInputBuf;  // ACF input buffer
    short *mpHCFInput_W;  // ACF input buffer
    short *mpHCFInput_Wleft;  // ACF input buffer
    BLOUD_InitParam mInitParam;    // loudness param
    BS_EngineInfo megInfo;  // engine infomation
    unsigned int mintrSize, mtmpSize;
    int mchNum, mexProcCount, mHCFInput_Count, mWorkMode;
    bool mActive;

};

// ----------------------------------------------------------------------------
}; // namespace android

#endif

