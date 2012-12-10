/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
**
** Copyright 2008, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#ifndef _APP_AWB_H
#define _APP_AWB_H

#include "MTKAWB.h"

typedef struct
{
	MINT32 i4Xr;
	MINT32 i4Yr;
} AWB_ROTATED_XY_COORDINATE_T;

/*******************************************************************************
*
********************************************************************************/
class AppAWB : public MTKAWB {
public:
    AppAWB();
    virtual ~AppAWB();
	virtual MRESULT initAWB(AWB_OUTPUT_T &a_rAWBOutput);
	virtual MRESULT setAWBFactor(const AWB_PARAM_T &a_rAWBParam,
								 const AWB_NVRAM_T &a_rAWBNVRAM,
								 AWB_STAT_CONFIG_T (&a_rAWBStatConfig)[LIB3A_AWB_MODE_NUM],
								 LIB3A_AWB_MODE_T a_eAWBMode);
	virtual MRESULT setAWBStatConfig(const AWB_STAT_CONFIG_T &a_rAWBStatConfig);
	virtual MRESULT setAWBMode(LIB3A_AWB_MODE_T a_eAWBMode);
	virtual void getDebugInfo(AWB_DEBUG_INFO_T &a_rAWBDebugInfo);
	virtual MRESULT handleAWB(AWB_INPUT_T &a_rAWBInput, AWB_OUTPUT_T &a_rAWBOutput);
	virtual MRESULT handleStrobeAWB(strFlashAWBInfo &a_rFlashAWBInfo, AWB_OUTPUT_T &a_rAWBOutput);
	virtual MINT32 getSceneLV();
	virtual MINT32 getCCT();
	virtual MRESULT getASDInfo(AAA_ASD_INFO_T &a_ASDInfo);
	virtual MRESULT getLightProb(AWB_LIGHT_PROBABILITY_T &a_rLightProb);

	MRESULT updateAWBStatConfigParam(AWB_STAT_CONFIG_T (&a_rAWBStatConfig)[LIB3A_AWB_MODE_NUM]);
	MRESULT adjustLightArea(AWB_STAT_CONFIG_T (&a_rAWBStatConfig)[LIB3A_AWB_MODE_NUM]);
    void LIMIT(AWB_GAIN_T &a_rAWBGain, MUINT32 a_u4LowerBound, MUINT32 a_u4UpperBound);
	void estimateCCT(LIB3A_AWB_MODE_T a_eCurAWBMode, const AWB_GAIN_T &a_rAWBGain);
	void reformatAWBGain(LIB3A_AWB_MODE_T a_eCurAWBMode, AWB_GAIN_T &a_rAWBGainOutput, const AWB_GAIN_T &a_rAWBGain);
	MRESULT checkAWBStat(MINT32 a_i4StartIndex, MINT32 a_i4EndIndex);
	void calculateLightRotatedXY();
	void calibrateAWBStat(MINT32 a_i4StartIndex, MINT32 a_i4EndIndex);
	MUINT32 MIN(MUINT32 a_u4Value1, MUINT32 a_u4Value2, MUINT32 a_u4Value3);
	MINT32 LOG10(MUINT32 a_u4Value);
	MINT32 getSquareRoot(MINT32 a_i4Value);
	MUINT32 getAntiLog(MINT32 a_i4Value);
	MBOOL isAWBStable(MUINT32 a_u4CurrentRGain, MUINT32 a_u4CurrentBGain, MUINT32 a_u4TargetRGain, MUINT32 a_u4TargetBGain);
	MRESULT getLightProb0();
	MRESULT getLightProb1();
	MRESULT getLightProb();
	MRESULT estimateLight();
	MINT32 getDaylightLocusTargetOffsetRatio(MINT32 a_i4DaylightLocusOffset);
	MINT32 getGreenOffsetThr(MINT32 a_i4DaylightLocusOffset);
	MINT32 getWeight_Tungsten(MINT32 a_i4MagentaOffset);
	MINT32 getWeight_WarmFluorescent(MINT32 a_i4GreenOffset);
	MINT32 getWeight_Shade(MINT32 a_i4GreenMagentaOffset);
	MRESULT predictAWBGain_Tungsten();
    MRESULT predictAWBGain_WarmFluorescent();
	MRESULT predictAWBGain_Fluorescent();
	MRESULT predictAWBGain_CWF();
	MRESULT predictAWBGain_Daylight();
	MRESULT predictAWBGain_DaylightFluorescent();
	MRESULT predictAWBGain_Shade();
	MRESULT predictAWBGain(AWB_GAIN_T &a_rAWBGain);
	MRESULT AWB();
	MRESULT PWB(LIB3A_AWB_MODE_T a_eCurrentAWBMode);
	MRESULT predictPWBGain(LIB3A_AWB_MODE_T a_eCurrentAWBMode, AWB_GAIN_T &a_rAWBGain);
	MRESULT predictPWBGain_LightArea1();

	// inline function
	inline MINT32 SQUARE(MINT32 a_i4Value)
    {
        return (a_i4Value * a_i4Value);
    }

	inline MINT32 ABS(MINT32 a_i4Value)
    {
        if (a_i4Value > 0)
        {
            return (a_i4Value);
        }
        else
		{
            return (0 - a_i4Value);
		}
    }

	inline MUINT32 RATIO(MUINT32 a_u4Value1, MUINT32 a_u4Value2)
    {
        if (a_u4Value1 >= ((MUINT32)1 << 22)) // 2^22
        {
            return ((a_u4Value1 + (a_u4Value2 >> 10)) / (a_u4Value2 >> 9));
        }
        else
		{
            return (((a_u4Value1 << 9) + (a_u4Value2 >> 1)) / a_u4Value2);
		}
    }

	inline MINT32 INTERPOLATE(MINT32 a_i4X, MINT32 a_i4X0, MINT32 a_i4X1, MINT32 a_i4Y0, MINT32 a_i4Y1)
    {
        return (a_i4Y0 + ((((a_i4X - a_i4X0) * (a_i4Y1 - a_i4Y0)) + ((a_i4X1 - a_i4X0) >> 1)) / (a_i4X1 - a_i4X0)));
    }

    inline void setDebugTag(AWB_DEBUG_INFO_T &a_rAWBDebugInfo, MINT32 a_i4ID, MINT32 a_i4Value)
    {
        a_rAWBDebugInfo.Tag[a_i4ID].u4FieldID = AAATAG(AAA_DEBUG_AWB_MODULE_ID, a_i4ID, 0);
        a_rAWBDebugInfo.Tag[a_i4ID].u4FieldValue = a_i4Value;
    }

#if PC_SIMU //-----------------------------------------------------------------------------------------------
    friend class AppAWBDebug;
#endif //----------------------------------------------------------------------------------------------------

private:
	AWB_PARAM_T m_rAWBParam;
	AWB_NVRAM_T m_rAWBNVRAM;
	AWB_STAT_CONFIG_T m_rAWBStatConfig;
	AWB_LIGHT_PROBABILITY_T m_rLightProb;
	AWB_LIGHT_PROBABILITY_T m_rLightProb4CCT;
	AWB_ALGORITHM_DATA_T m_rAWB;
	STROBE_AWB_ALGORITHM_DATA_T m_rStrobeAWB;
	PWB_ALGORITHM_DATA_T m_rPWB;
    AWB_SPEED_MODE_T m_eAWBSpeedMode;
	LIB3A_AWB_MODE_T m_eAWBMode;
	AWB_GAIN_T m_rAWBGain;
	AWB_GAIN_T m_rStrobeAWBGain;
	AWB_GAIN_T m_rDaylightWBGain;
	AWB_GAIN_T m_rAWBGainOutput;
	AWB_GAIN_T m_rAWBCalGain;
	AWB_GAIN_T m_rASDAWBGain_D65;
	AWB_GAIN_T m_rASDAWBGain_CWF;
	AWB_GAIN_T m_rASDAWBGain;
	AWB_STAT_T m_rAWBStat;
	CCT_INFO_T m_rCCTInfo;
	AWB_ROTATED_XY_COORDINATE_T m_rLightAvgRotatedXY[AWB_LIGHT_NUM];
	strFlashAWBInfo m_rFlashAWBInfo;

    MINT32 m_i4SceneLV;
	MINT32 m_i4CCT;
	MINT32 m_i4FluorescentIndex;
	MINT32 m_i4DaylightFluorescentIndex;
    MUINT32 m_u4LightMode;
	MUINT32 m_u4TotalPaxelNum;
	MUINT32 m_u4NeutralPaxelNum;
	MUINT32 m_u4NeutralPaxelNumThr;
	MUINT32 m_u4NeutralPaxelNumThr_CWF;
	MUINT32 m_u4NeutralPaxelNumThr_DF;
        MINT32 m_i4SlopeNumerator; // Daylight locus slope numerator
	MINT32 m_i4SlopeDenominator; // Daylight locus slope denominator
	MINT32 m_i4SlopeNumeratorSquare; // Square of daylight locus slope numerator
	MINT32 m_i4SlopeDenominatorSquare; // Square of daylight locus slope denominator
    MINT32 m_i4SlopeNumeratorXDenominator; // Daylight locus slope numerator x denominator
    MBOOL m_bAWBStatOK;
    MBOOL m_bAWBStable;
    MBOOL m_bIsStrobeFired; // AWB for AF lamp or pre-flash
    MBOOL m_bAWBModeChange;
};

#endif

