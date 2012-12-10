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

#ifndef _APP_3A_H
#define _APP_3A_H

#include "MTK3A.h"

/*******************************************************************************
*
********************************************************************************/
class MTKAE;
class MTKAF;
class MTKAWB;


class App3A : public MTK3A {
public:
    App3A();
    virtual ~App3A();
    virtual MRESULT send3ACmd(MINT32 a_i4CmdID, MINT32 a_i4CmdParam);
    virtual MRESULT get3ACmd(MINT32 a_i4CmdID, MINT32 *a_i4CmdParam);
    virtual MRESULT init3A(AAA_INIT_INPUT_PARAM_T &a_r3AInitInput,
                           AAA_OUTPUT_PARAM_T &a_r3AOutput,
                           AAA_STAT_CONFIG_T &a_r3AStatConfig);
    virtual MRESULT deinit3A();
    virtual MRESULT handle3A(AAA_FRAME_INPUT_PARAM_T &a_r3AInput, AAA_OUTPUT_PARAM_T &a_r3AOutput);
    virtual MRESULT set3AState(AAA_STATE_T a_e3AState);
    virtual MRESULT	get3ADebugInfo(AAA_DEBUG_INFO_T &a_r3ADebugInfo);
    virtual MRESULT enableAE();
    virtual MRESULT disableAE();
    virtual MBOOL isAEEnable();
     virtual MRESULT lockAE();
    virtual MRESULT unlockAE();
    virtual MBOOL isAELocked();
    virtual MBOOL isStrobeOn();
   virtual MRESULT enableAWB();
    virtual MRESULT disableAWB();
	virtual MBOOL isAWBEnable();
    virtual MRESULT lockAWB();
    virtual MRESULT unlockAWB();
    virtual MBOOL isAWBLocked();
    virtual MRESULT enableAF();
    virtual MRESULT disableAF();
	virtual MBOOL isAFEnable();
    virtual MINT32 getSceneBV();
    virtual MINT32 getSceneLV();
    virtual MINT32 getAEPlineEV();
    virtual MINT32 getCCT();
	virtual MRESULT lockHalfPushAEAWB();
	virtual MRESULT unlockHalfPushAEAWB();
	virtual MRESULT resetHalfPushState();
	virtual MRESULT applyAWBParam(AWB_NVRAM_T &a_rAWBNVRAM, AWB_STAT_CONFIG_T &a_rAWBStatConfig);
	virtual MRESULT applyAEParam(AE_NVRAM_T &a_rAENVRAM);
	virtual MRESULT applyAFParam(NVRAM_LENS_PARA_STRUCT &a_rAFNVRAM);
	virtual MRESULT get3AEXIFInfo(AAA_EXIF_INFO_T *a_p3AEXIFInfo);
	virtual MRESULT getAEModeSetting(AE_MODE_CFG_T &a_rAEOutput, AAA_STATE_T  a_3AState);
	virtual MRESULT getASDInfo(AAA_ASD_INFO_T &a_ASDInfo);
	virtual MRESULT getAWBLightProb(AWB_LIGHT_PROBABILITY_T &a_rLightProb);
    MRESULT set3AFactor();
    MRESULT handle3AStateChange();
    MRESULT setAEMode(LIB3A_AE_MODE_T a_eAEMode);
    MRESULT setAEEVCompensate(LIB3A_AE_EVCOMP_T a_eAEEVComp);
    MRESULT setAEMeteringMode(LIB3A_AE_METERING_MODE_T a_eAEMeteringMode);
    MRESULT setAEISOSpeed(LIB3A_AE_ISO_SPEED_T a_eAEISOSpeed);
    MRESULT setAEStrobeMode(LIB3A_AE_STROBE_MODE_T a_eAEStrobeMode);
    MRESULT setAEFlashlightType(MINT32	a_FlashlightType);
    MRESULT setAERedeyeMode(LIB3A_AE_REDEYE_MODE_T a_eAERedeyeMode);
    MRESULT setAEFlickerMode(LIB3A_AE_FLICKER_MODE_T a_eAEFlickerMode);
    MRESULT setAEFrameRateMode(MINT32 a_eAEFrameRateMode);
    MRESULT setAEMaxFrameRate(MINT32 a_eAEMaxFrameRate);
    MRESULT setAEMinFrameRate(MINT32 a_eAEMinFrameRate);
    MRESULT setAEFlickerAutoMode(LIB3A_AE_FLICKER_AUTO_MODE_T a_eAEFlickerAutoMode);
    MRESULT setAEPreviewMode(LIB3A_AE_PREVIEW_MODE_T a_eAEPreviewMode);
    MRESULT setAFMode(LIB3A_AF_MODE_T a_eAFMode);
    MRESULT setAFMeter(LIB3A_AF_METER_T a_eAFMeter);
    MRESULT setAFZone(LIB3A_AF_ZONE_T a_eAFZone);
    void    setAFMoveSpotPos(MUINT32 a_u4Xoffset,MUINT32 a_u4Yoffset,MUINT32 a_u4Width,MUINT32 a_u4Height,MUINT32 a_u4OffsetW,MUINT32 a_u4OffsetH,MUINT8 a_uOri);
    void    setMFPos(MINT32 a_i4Pos);
    void    getAFWinResult(AF_WIN_RESULT_T &a_sAFWinResult);
    MINT32  getAFBestPos();
    MINT32  setFocusDistanceRange(MINT32 a_i4Distance_N, MINT32 a_i4Distance_M);
    MINT32  getFocusDistance(MINT32 &a_i4Near, MINT32 &a_i4Curr, MINT32 &a_i4Far);
    MUINT32 getAFValue();
    void    setAFFullStep(MINT32 a_i4Step);
    MBOOL   isAFFinish();
    void    setAFCoef(AF_COEF_T a_sAFCoef);
    void    pauseFocus();
    MBOOL   isFocused();
	void    resetFocus();
	void    setFocusAreas(MINT32 a_i4Cnt, AREA_T *a_psFocusArea);
	void    getFocusAreas(MINT32 &a_i4Cnt, AREA_T **a_psFocusArea);
    MINT32  getMaxNumFocusAreas();
    void    AFDrawRect(MUINT32 a_u4BuffAddr,MUINT32 a_u4Width,MUINT32 a_u4Height,MUINT32 a_u4OffsetW,MUINT32 a_u4OffsetH,MUINT8 a_uOri);
    MRESULT setAWBMode(LIB3A_AWB_MODE_T a_eAWBMode);

    MRESULT copyAEInof2mhal(AE_MODE_CFG_T *sAEOutputInfo, strAEOutput *sAEInfo);
    void    setAFLampInfo(MBOOL a_bAFLampOn, MBOOL a_bAFLampIsAutoMode);
    MBOOL   getAFLampIsAutoOn();
    static MTK3A* createInstance();
    MRESULT switchSensorExposureGain(AE_EXP_GAIN_MODIFY_T &rInputData, AE_EXP_GAIN_MODIFY_T &rOutputData);
    virtual MINT32 getAEMaxNumMeteringAreas();
    virtual MVOID getAEMeteringAreas(MINT32 &a_i4Cnt, AREA_T **a_psAEArea);
    virtual MVOID setAEMeteringAreas(MINT32 a_i4Cnt, AREA_T const *a_psAEArea);
    virtual MRESULT set3AMeteringModeStatus(MINT32 a_i4AEMeteringModeStatus);

    //cotta : add for strobe protection
	void setPrevMFEndTime(MUINT32 uNewTime);
	void setStrobeProtectionIntv(MUINT32 uNewTime);
	MUINT32 getPrevMFEndTime();
	MUINT32 getStrobeProtectionIntv();
    void setStrobeWDTValue(MUINT32 timeMS); //added for WDT customize
    void setIsBurstMode(MINT32 isBurst);
	MRESULT handleAEUpdataStrobe(MINT32 isStrobeFiring, strAEInput* a_Input, strAEOutput* a_Output);

private:
    MINT32 m_LedOffBv;
    MINT32 m_LedOnBv;
	MINT32 isNeedAutoFlash;
	int m_isBurstShootMode;
    MTKAE *m_pMTKAEObj;
    MTKAF *m_pMTKAFObj;
    MTKAWB *m_pMTKAWBObj;
    MUINT32 m_u4FrameCount;
    AAA_STATE_T m_e3AState;
    AAA_STATE_T m_e3ANewState;
    HALF_PUSH_STATE_T m_eHalfPushState;
    MBOOL m_bEnableAE;
    MBOOL m_bEnableAWB;
    MBOOL m_bAELock;
    MBOOL m_bAWBLock;
    MBOOL m_bEnableAF;
    MBOOL m_bOneShotAE;
    MBOOL m_bOneShotAWB;
    MBOOL m_bAWBModeChanged;
	MBOOL m_bLockHalfPushAEAWB;
    AAA_CMD_SET_T m_r3ASetting;
    MUINT32 m_u4AAACycleNum;
    AAA_DEBUG_INFO_T m_r3ADebugInfo;
    strAEOutput m_rAEOutput;
    AAA_OUTPUT_PARAM_T m_r3AOutput;
    MBOOL m_bChgState;
    MBOOL m_bChgAFMeter;
    MINT32 m_AEFlashlightType;

    MBOOL m_bAFLampOn;
    MBOOL m_bAFLampIsAutoMode;
    MBOOL m_bRunOnceShot2ATwice;
    MBOOL m_b2AOutputBackup;
    MBOOL m_bAEOutputBackup;
    MINT32       m_i4isAEFlashOn;
    AE_OUTPUT_T  m_rAEOutputBackup;
    AWB_OUTPUT_T m_rAWBOutputBackup;

    //cotta-- add for strobe protection
	MUINT32 m_MFPrevEndTime;
	MUINT32 m_strobeProtectIntv;

    //cotta-- added for WDT customize
    MUINT32 m_maxExposureTime;

};

#endif

