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

#ifndef _APP_AE_H
#define _APP_AE_H

#include "MTKAE.h"

#define HW_AE_TOTLA_BLOCKS 16384   //128 X 128
#define APPAE_NEED_MODIFY  1  // define some place need be update in future

#define EXP_LIMIT_AF_ASSIST ((MUINT32)20000)  //! 10 ms use in AF Assist mode

#define AE_DEBUG_INFO_VERSION  0x00

#define AUTO_FLICKER_FRAME_RATE_MAX   298
#define AUTO_FLICKER_FRAME_RATE_MID   148
#define AUTO_FLICKER_FRAME_RATE_MIN    48

#define AE_WIN_NUM_MATRIX     1    // only support 1 window
#define AE_WIN_OFFSET          1000   // for android window define
#define MAX_AE_STAT_WIN_OFFSET 510

enum
{
    AE_DIR_UP=0,
    AE_DIR_DOWN=1,
    AE_DIR_RIGHT=2,
    AE_DIR_LEFT=3,

    AE_DIR_NONE=4
};

typedef struct
{
    MUINT8 *pBlock;   //point to of AE/AWB block statistic
    MUINT32 Mode;        // mode 0: AE block 1 :AWB block
    MUINT32 u4WinNumX;   //total window numbers in X
    MUINT32 u4WinNumY;   //total window numbers in Y
    MUINT32 u4Xstart;    // start X of ROI of histogram area
    MUINT32 u4Ystart;    // start Y of ROI of histogram area
    MUINT32 u4Xend;      // end X of ROI of histogram area
    MUINT32 u4Yend;      // end Yof ROI of histogram area
} HistCoreIn ;

typedef struct
{
    MUINT32 u4Mean;
    MUINT32 u4Count;
    MUINT32* pu4Hist;
}HistCoreOut;

//typedef struct AEMOVE AEMoving;     //!<: RGB_GAINS structure

#define AEFLASH_BAYER_BASE 0

#if AEFLASH_BAYER_BASE==1 //RGB base
    #define AEFLASH_STAT_NUM 3
    #define AEFLASH_STAT_BYTE_PER_WIN 4
#else //Y base
    #define AEFLASH_STAT_NUM 1
    #define AEFLASH_STAT_BYTE_PER_WIN 1
#endif
#define XGRID   5 //m_strPFEvalStatInfo.u4WinX //statistics most coarse window number
#define YGRID   5 //m_strPFEvalStatInfo.u4WinY
#define RATIOBASE   256
#define XRATIO  256//200 //128 // 1/2 of u4WindowNumX Q0.0.8
#define YRATIO  256//200 //128
#define AEFLASH_FRM_NUM 3
#define AEFLASH_STAT_DATA_ALIGN (8-1) //align to 8 bytes
#define AEFLASH_STAT_HI 250 //@8bit
#define AEFLASH_STAT_LO 10  //@8bit
#define AEFLASH_VALID_WIN_LO (RATIOBASE/8)
#define AEFLASH_VALID_AVGL_LO (10)
#define AEFLASH_HIST_SIZE 64 //for MT6516
#define AEFLASH_HIST_STEP 4  //for MT6516
#define AEFLASH_SWSAMPLE_X 1
#define AEFLASH_SWSAMPLE_Y 1
#define AEFLASH_REDEYE_FRM 15
#define AEFLASH_REDEYE_WIDTH_US 3
#define AEFLASH_REDEYE_FIRE_NUM 1
//fixed POWER LED strength
#define AEFLASH_1ST_FIRE_STRENGTH 12 //for YUSU 0~31
#define AEFLASH_2ND_FIRE_STRENGTH 12  //for YUSU 0~31

typedef enum {
    PF_STATE_IDLE = 0,
    PF_STATE_INIT,      //config preview w/o flash
    PF_STATE_INIT_EX1,  //initial extend frame1
    PF_STATE_REDEYE,
    PF_STATE_0,         //w/o flash end
    PF_STATE_1,         //config prevuew W/  flash 1
    PF_STATE_2,         //config preview W/  flash 2
    PF_STATE_3,         //calculate w/o flash
    PF_STATE_4,         //calculate w/ flash 1
    PF_STATE_4_1,       //calculate w/ flash 1
    PF_STATE_4_2,       //calculate w/ flash 1
    PF_STATE_5,         //calculate w/ flash 2
    PF_STATE_6,         //for saving debug file

    PF_STATE_MAX
}ePreFlashIState;

typedef enum {
    PF_STAT_0=0,
    PF_STAT_1=1,
    PF_STAT_2=2
}ePreFlashStatistic;

typedef enum {
    PF_1ST_0=0,
    PF_2ND_1=1,
}ePreFlashIndex;

typedef enum {
    PF_MODE_LINEAR,
    PF_MODE_AETABLE
}ePreFlashEvSettingMode;

typedef enum {
    PF_INFO_NF_STAT_BUF=0,
    PF_INFO_WF_STAT_BUF,
    PF_INFO_TARGET_LEVEL,
    PF_INFO_SW_STAT_SIZE_X,
    PF_INFO_SW_STAT_SIZE_Y,
    PF_INFO_MAX
}ePreFlashInfo;

typedef struct
{
    MUINT32 u4TotalWin;
    MUINT32 u4ValidWin;
    MUINT32 u4AvgL;
}strWinInfo;

typedef struct{
    MUINT32  u4WinX;
    MUINT32  u4WinY;
    MVOID    *pPFHist[AEFLASH_FRM_NUM];
    MVOID    *pPFSWStatBuf[AEFLASH_FRM_NUM];
    strWinInfo  winInfo[AEFLASH_FRM_NUM];
    MVOID    *pPFHWStatBuf[AEFLASH_FRM_NUM];
    MVOID    *pucPFEffWin;
}strPreFlashStatInfo;


#define AE_DEBUG_USE

/*******************************************************************************
*
********************************************************************************/
class AppAE : public MTKAE {
public:

/////////////////////////////////////////////////////////////////////////
//
// AppAE () -
//! \brief AppAE module constructor.
//!
//
/////////////////////////////////////////////////////////////////////////
    AppAE();

/////////////////////////////////////////////////////////////////////////
//
//  AppAE () -
//! \brief AppAE module constructor.
//!
//
/////////////////////////////////////////////////////////////////////////
    virtual ~AppAE();

/////////////////////////////////////////////////////////////////////////
//
//  eanble AE () -
//! \brief se  eanble AE
//!
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT enableAE();

/////////////////////////////////////////////////////////////////////////
//
//  disableAE AE () -
//! \brief se  disableAE
//!
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT disableAE();

/////////////////////////////////////////////////////////////////////////
//
//  setAEParameter() -
//!
//!
//
/////////////////////////////////////////////////////////////////////////
virtual void setAEParameter(struct_AE_Para &a_strAEPara);

/////////////////////////////////////////////////////////////////////////
//
//  getDebugInfo() -
//!
//!
//
/////////////////////////////////////////////////////////////////////////
virtual void getDebugInfo(AE_DEBUG_INFO_T &a_DebugInfo);

/////////////////////////////////////////////////////////////////////////
//
//  setAEMeteringMode () -
//!
//!  input : enum ae metering mode
//  output: error message
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEMeteringMode(LIB3A_AE_METERING_MODE_T  a_eMeteringMode);

/////////////////////////////////////////////////////////////////////////
//
//   setAEMode () -
//!
//!  input : enum AE mode
//   output: error message
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEMode(LIB3A_AE_MODE_T  a_eAEMode);

/////////////////////////////////////////////////////////////////////////
//
//  setAvValue () -
//!
//!  input : enum Aperture value (Fno.)
//  output: error message
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAvValue(eApetureValue  a_eAV);

/////////////////////////////////////////////////////////////////////////
//
//  setTvValue () -
//!
//!  input : enum TV value (exposure time .)
//  output: error message
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setTvValue(eTimeValue  a_eTV);

/////////////////////////////////////////////////////////////////////////
//
//  setIsoSpeed() -
//!
//!  input : enum ISO speed
//  output: error message
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setIsoSpeed(LIB3A_AE_ISO_SPEED_T	a_eISO);

/////////////////////////////////////////////////////////////////////////
//
//  setStrobeMode() -
//!
//!  input : enum strobe mode
//  output: error message
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setStrobeMode(LIB3A_AE_STROBE_MODE_T	a_eStrobeMode);

/////////////////////////////////////////////////////////////////////////
//
//  enableRedEye() -
//!
//!  input :  enum Red eye
//  output: void
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setRedEye(LIB3A_AE_REDEYE_MODE_T a_eRedEye );

/////////////////////////////////////////////////////////////////////////
//
//  getAETableIndex ()-
//! \brief Get index in AE table
//! \param none
//!
//!
//! \return index
//
/////////////////////////////////////////////////////////////////////////
virtual MUINT32 getAETableIndex(void);

/////////////////////////////////////////////////////////////////////////
//
//  getAEIndexEVSetting ()-
//! \brief get EV setting according table index
//! \param [in] u4indx	AE table index
//! 	   [out] a_aeout EV setting
//!
//! \return MRESULT
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAEIndexEVSetting(MUINT32 u4idx,strAEOutput *a_aeout);

/////////////////////////////////////////////////////////////////////////
//
// getSoftwareHist() -
//  return Software histogram data
//!
//!  input :  void
//  output:  a_pu4Histgram , u4 256  buffer
/////////////////////////////////////////////////////////////////////////
virtual void getSoftwareHist(MUINT32* a_pu4Histgram);

/////////////////////////////////////////////////////////////////////////
//
//  getAECWValue() -
//
//  input :  void
//  output:  AE Central weighting value
/////////////////////////////////////////////////////////////////////////
virtual MUINT32 getAECwValue(void);

/////////////////////////////////////////////////////////////////////////
//
//  getAECondition() -
//
//  input :  void
//  output:  getAECondition
/////////////////////////////////////////////////////////////////////////
virtual MUINT32 getAECondition(void);

/////////////////////////////////////////////////////////////////////////
//
//   getAEFaceDiffIndex() -
//
//  input :  void
//  output:  getAEFaceDiffIndex
/////////////////////////////////////////////////////////////////////////
virtual MINT16 getAEFaceDiffIndex(void);

/////////////////////////////////////////////////////////////////////////
//
//  getAEBlockVaule() -
//
//  input :  void
//  output:  a_pAEBlockResult , buffer to store AE nxn block result
/////////////////////////////////////////////////////////////////////////
virtual void  getAEBlockVaule(MUINT32 *a_pAEBlockResult);

/////////////////////////////////////////////////////////////////////////
//
//  setEVCompensate () -
//! \brief set AE EV Compensateion value
//!
//! \param [in] a_eEVComp  enum EV compensation setting
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setEVCompensate(LIB3A_AE_EVCOMP_T a_eEVComp);

/////////////////////////////////////////////////////////////////////////
//
//  setAEFlickerMode () -
//! \brief set AE Flicker frequence
//!
//! \param [in] a_eAEFlickerMode  enum flicker frequence setting
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEFlickerMode(LIB3A_AE_FLICKER_MODE_T a_eAEFlickerMode);

/////////////////////////////////////////////////////////////////////////
//
//  setAEFlickerAutoMode () -
//! \brief set AE Flicker auto value
//!
//! \param [in] a_eAEFlickerAutoMode  enum flicker auto mode setting
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEFlickerAutoMode(LIB3A_AE_FLICKER_AUTO_MODE_T a_eAEFlickerAutoMode);

/////////////////////////////////////////////////////////////////////////
//
//	 setAEPreviewMode () -
//! \brief set AE preview mode
//!
//! \param [in] a_eAEPreviewMode  enum preview mode setting
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEPreviewMode(LIB3A_AE_PREVIEW_MODE_T a_eAEPreviewMode);

/////////////////////////////////////////////////////////////////////////
//
//	 getAEFlickerAutoMode() -
//!
//!  input :  none
//	output: flicker auto mode value
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAEFlickerAutoMode(LIB3A_AE_FLICKER_AUTO_MODE_T *a_eAEFlickerAutoMode);

/////////////////////////////////////////////////////////////////////////
//
//	 getAEPreviewMode() -
//!
//!  input :  none
//	output: preview mode value
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAEPreviewMode(LIB3A_AE_PREVIEW_MODE_T *a_eAEPreviewMode);

/////////////////////////////////////////////////////////////////////////
//
//  setAEFrameRateMode () -
//! \brief set AE Frame rate frequence
//!
//! \param [in] a_eAEFrameRateMode  enum frame rate frequence setting
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEFrameRateMode(MINT32 a_eAEFrameRateMode);

/////////////////////////////////////////////////////////////////////////
//
//	 setAEMaxFrameRate() -
//!
//!  input :  max frame rate value
//	 output: void
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEMaxFrameRate(MINT32 a_eAEMaxFrameRate);

/////////////////////////////////////////////////////////////////////////
//
//	 setAEMinFrameRate() -
//!
//!  input :  min frame rate value
//	 output: void
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEMinFrameRate(MINT32 a_eAEMinFrameRate);

/////////////////////////////////////////////////////////////////////////
//
//	 getAEMaxFrameRate() -
//!
//!  input :  none
//	 output: max frame rate value
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAEMaxFrameRate(MINT32 *a_eAEMaxFrameRate);

/////////////////////////////////////////////////////////////////////////
//
//	 getAEMinFrameRate() -
//!
//!  input :  none
//	 output: min frame rate value
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAEMinFrameRate(MINT32 *a_eAEMinFrameRate);

/////////////////////////////////////////////////////////////////////////
//
//	 getAESupportFrameRateNum() -
//!
//!  input :  none
//	 output: support frame rate num
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAESupportFrameRateNum(MINT32 *a_eAEFrameRateNum);

/////////////////////////////////////////////////////////////////////////
//
//	 getAESupportFrameRateRange() -
//!
//!  input :  none
//	 output: support frame rate range
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAESupportFrameRateRange(MINT32 *a_eAEFrameRateRange);

/////////////////////////////////////////////////////////////////////////
//
//  setAEConvergeSpeed () -
//! \brief set AE converge speed
//!
//! \param [in] BrightConSpeed	-AE converge spped
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEConvergeSpeed(MUINT32 BrightConSpeed, MUINT32 DarkConSpeed);

/////////////////////////////////////////////////////////////////////////
//
//  switchAETable () -
//! \brief run time swithc AE table
//!
//! \param [in]  strAETable a_AeTable  - AE table structure
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT switchAETable( strAETable &a_AeTable);

/////////////////////////////////////////////////////////////////////////
//
//  getEVValue ()-
//! \brief GET EV value in current AE condition
//!
//! \param void
//!
//!
//! \return EV value 10base
//
/////////////////////////////////////////////////////////////////////////
virtual MUINT32 getEVValue(void);

/////////////////////////////////////////////////////////////////////////
//
//  turnOnAE ()-
//! \brief Test Funtion Turn on AE
//!
//! \param void
//!
//!
//! \return void
//
/////////////////////////////////////////////////////////////////////////
virtual void turnOnAE(void);

/////////////////////////////////////////////////////////////////////////
//
//  turnOffAE ()-
//! \brief Test Function Turn off AE
//!
//! \param void
//!
//!
//! \return void
//
/////////////////////////////////////////////////////////////////////////
virtual void turnOffAE(void);

/////////////////////////////////////////////////////////////////////////
//
//  setAECondition();
//! \brief   set AE condition
//!
//! \param [in]  strSetAEConditionIn , structure of AE control flag
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MVOID setAECondition(strSetAEConditionIn &a_Condition);

/////////////////////////////////////////////////////////////////////////
//
//   getAESetting
//! \brief  get AE setting in different state
//!
//! \param
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAESetting(strAEOutput* a_Output,eAESTATE  a_AeState = AE_STATE_CAPTURE);

/////////////////////////////////////////////////////////////////////////
//
//  switchCapureDiffEVState () -
//! \brief switch ae  to "different EV capture" state
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
void switchCapureDiffEVState(strAEOutput *aeoutput, MINT8 iDiffIdx);

/////////////////////////////////////////////////////////////////////////
//
//   switchSensorExposureGain () -
// \brief switch sensor exposure time and gain to fit sensor requirement.
//
//
// \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT switchSensorExposureGain(AE_EXP_GAIN_MODIFY_T &rInputData, AE_EXP_GAIN_MODIFY_T &rOutputData);


/////////////////////////////////////////////////////////////////////////
//
//   getAEMaxNumMeteringAreas
//! \brief  get AE max number metering areas
//!
//! \param
//!
//! \return numbers of metering area
//
/////////////////////////////////////////////////////////////////////////
virtual MINT32 getAEMaxNumMeteringAreas();


/////////////////////////////////////////////////////////////////////////
//
//   getAEMeteringAreas
//! \brief  get AE metering areas information
//!
//! \param
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MVOID getAEMeteringAreas(MINT32 &a_i4Cnt, AREA_T **a_psAEArea);

/////////////////////////////////////////////////////////////////////////
//
//   setAEMeteringAreas
//! \brief  set AE metering areas information
//!
//! \param
//!
//! \return void.
//
/////////////////////////////////////////////////////////////////////////
virtual MVOID setAEMeteringAreas(MINT32 a_i4Cnt, AREA_T const *a_psAEArea);

/////////////////////////////////////////////////////////////////////////
//
//   setAEMeteringModeStatus() -
//! \brief set AE metering mode to let AE understand the current mode.
//!
//! \param [in] a_i4AEMeteringModeStatus 0 : video preview, 1:video recording, 2:camera preview
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEMeteringModeStatus(MINT32 a_i4AEMeteringModeStatus);

/////////////////////////////////////////////////////////////////////////
//
//   setAEStrobeModeStatus() -
//! \brief set AE strobe mode status to let AE understand the strobe mode.
//!
//! \param [in] a_i4AEMeteringModeStatus 0 : preview mode, 1 : pre-capture mode
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAEStrobeModeStatus(MINT32 a_i4AEStrobeModeStatus);

/////////////////////////////////////////////////////////////////////////
//
//   modifyAEWindowSetting
//! \brief  modify the AE window information
//!
//! \param
//!
//! \return void.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT modifyAEWindowSetting(AE_STAT_CONFIG_T &strAEStatCFG, EZOOM_WIN_T &sEZoomSize, FD_INFO_T &sFaceWinInfo, MBOOL *bAECFGUpdate, MBOOL *bFaceAEWinUpdate);

/////////////////////////////////////////////////////////////////////////
//
//   isAEMeteringWinEnable() -
//! \brief AE meterwindow enable or not.
//!
//! \param NONE
//!
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MBOOL isAEMeteringWinEnable(void);

/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//1   Protected
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//protected:


/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//1   Private
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//private:
/////////////////////////////////////////////////////////////////////////
//
//  setAEConfig() -
//!
//!  input : structure -- strAECofig
//  output: void
/////////////////////////////////////////////////////////////////////////
//void setAEConfig(strAEConfig &a_AeCfg);

/////////////////////////////////////////////////////////////////////////
//
//  setWinConfig() -
//!
//!  input : structure -- strBlcokConfig
//  output: void
/////////////////////////////////////////////////////////////////////////
//void setWinConfig(AWBSTAT_CONFIG_BLK &a_AeWinCfg);

/////////////////////////////////////////////////////////////////////////
//
//  setAEConfig() -
//!
//!  input : structure -- strBlcokConfig
//  output: void
/////////////////////////////////////////////////////////////////////////
//void setHistConfig(strHistConfig  &a_HistCfg);

/////////////////////////////////////////////////////////////////////////
//  setAEConfig() -
//!
//!  input : structure -- strBlcokConfig
//  output: void
/////////////////////////////////////////////////////////////////////////
MRESULT searchAETable(const struct_AE_Para *a_aepara ,eAETableID id,  strAETable** a_ppPreAETable);

/////////////////////////////////////////////////////////////////////////
//  setAEConfig() -
//!
//!  input : structure -- strBlcokConfig
//  output: void
/////////////////////////////////////////////////////////////////////////
MRESULT setWeightTable(const struct_AE_Para *a_aepara ,eWeightingID id,  strWeightTable** a_ppWeightTbl);

/////////////////////////////////////////////////////////////////////////
//
//  getHistogram() -
//!
//!  input :
//  output:
/////////////////////////////////////////////////////////////////////////
void getHistogram();

/////////////////////////////////////////////////////////////////////////
//
//  getAEBlock() -
//!
//!  input :
//  output:
/////////////////////////////////////////////////////////////////////////
void getAEBlock();

/////////////////////////////////////////////////////////////////////////
//
//  getAESatistic() -
//!
//!  input :
//  output:
/////////////////////////////////////////////////////////////////////////
void getAESatistic();

/////////////////////////////////////////////////////////////////////////
//
//  setAESatisticBufferAddr() -
//  set AE statistic buffer addresss
//!
//!  input :  AE statistic buffer
//  output:  void
/////////////////////////////////////////////////////////////////////////
//virtual void setAESatisticBufferAddr(void* pAEBuffer);
//virtual void setAESatisticBufferAddr(void* a_pAEBuffer, void* a_AWBBuffer);
virtual void setAESatisticBufferAddr(void* a_pAEBuffer, void* a_FlareBuffer, void* a_AEHisBuffer, MUINT32 u4BlockCnt, void* a_FaceHisBuffer);

/////////////////////////////////////////////////////////////////////////
//
//  calAEBlockInfo() -
//  calculate AE block information,
// ! according AE statistic Dram data (exp:100x100)  get block information (exp :5x5)
//
//!  input :  void
//  output:  void
/////////////////////////////////////////////////////////////////////////
void getAEBlockInfo(void);

/////////////////////////////////////////////////////////////////////////
//
//  getHistgramInfo() -
//  calculate Histogram information ,
//  According Eagle 96bin information transfer to strHistInfo (HighY, LowY Maxbin ...)
//!  input :  void
//  output:  void
/////////////////////////////////////////////////////////////////////////
void getHistgramInfo(void);

/////////////////////////////////////////////////////////////////////////
//
//  getBinY() -
//  get each bin's Y value
//!  input :  bin index  0~95
//  output:  void
/////////////////////////////////////////////////////////////////////////
//transfer from histogram index --> Y value
// C model histogram format
// 0~63   -->>	0~31    bin_no=Y/2
// 64~191 -->>	32~63  bin_no=Y/4+16
// 192~255 -->> 64~96   bin_no=Y/2-32
MUINT32 getBinY(MUINT32 a_bin);

/////////////////////////////////////////////////////////////////////////
//
//  getAEStatisticData()
//
//!  Get AE statistic data
//   from AE original satatistic data
//   to get histogram information, Central weighting value
//
//
/////////////////////////////////////////////////////////////////////////
void getAEStatisticData(void);

/////////////////////////////////////////////////////////////////////////
//
//  getAEStatisticData()
//
//!  Get recommend Central Weighting Target
//  input : None
//  output : Recommend CW target
//
/////////////////////////////////////////////////////////////////////////
MUINT32 getRecommendCWTarget(void);

/////////////////////////////////////////////////////////////////////////
//
//  handleAE()
//
//!  Handle AE
//  input : None
//  output : Error code
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT handleAE(strAEInput* a_Input,strAEOutput* a_Output);

/////////////////////////////////////////////////////////////////////////
//
//  checkBackLight
//  check backlight condition
//  input : None
//  output : a_cwm--> recommend central weighting value for backlight system
//   return : hit backlight condition or not
//
/////////////////////////////////////////////////////////////////////////
MBOOL checkBackLight(MUINT32 &a_u4Cwv);

/////////////////////////////////////////////////////////////////////////
//  checkOverExposure()
//  check whether over exposure or not
//
//  input : None
//  output : a_cwm--> recommend central weighting value for anti over exposure system
//  return : hit over exposure or not
//
/////////////////////////////////////////////////////////////////////////
MBOOL checkOverExposure(MUINT32 &a_u4Cwv);

/////////////////////////////////////////////////////////////////////////
//  checkHistogramStretch
//  check whether need stretch histogram or not
//
//  input : None
//  output : a_cwm--> recommend central weighting value for stretch histogram  system
//  return : hit conditionor not (need stretch or not)
//
/////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////
//
//  checkFaceAE
//  check FaceAE  condition
//  input : None
//  output : a_cwm--> recommend central weighting value for FaceAE system
//  return : hit Face AE or not
//
/////////////////////////////////////////////////////////////////////////
MBOOL checkFaceAE(MUINT32 a_u4CWTarget, MUINT32 &a_u4Cwv);
MBOOL  checkHistogramStretch(MUINT32 &a_u4Cwv);
MINT32 getDeltaIndex(void);
MINT32 getSenstivityDeltaIndex(MUINT32 u4NextSenstivity);
MRESULT modifyAECaptureParams(strEvSetting *pCaptureSetting, MINT32 DiffEV, MINT32 gainRatio);
MINT32 Ratio2DIndex(MUINT32 ratio);
void   resetTimeLPF(void);
MUINT32 doTimeLPF(MUINT32 a_u4idx, MUINT32 a_u4level);
MUINT32 getIndexLPF(MUINT32 a_u4Idx,MUINT32 a_delta);
MUINT32 getIndexLPF(MUINT32 a_u4Idx,MINT32 a_i4delta);
MINT32 AeLimitCaptureEv(MINT32 CurIdx, MINT32 LastIdx, MINT32 LastOriIdx);

////////////////////////////
//  getAWBBlockInfo()
//  check how many block is close to pure green block
//  save percentage in m_pAWBSatCount
//
/////////////////////////////
void getAWBBlockInfo(void);

/////////////////////////////////////////////////////////////////////////
//
//   initAE () -
//! \brief Initialize AE module, set Initial exposure time, Iris ,Gain
//!
//!
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT initAE(strAEOutput *a_aeout);

/////////////////////////////////////////////////////////////////////////
//
//  switchToCatpureState () -
//! \brief switch ae  to "capture" state
//!
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
MRESULT switchCapureState(strAEOutput *aeoutput);

/////////////////////////////////////////////////////////////////////////
//
//  switchPreviewState () -
//! \brief switch ae  to "Preview" state
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
void switchPreviewState(strAEOutput *aeoutput);

/////////////////////////////////////////////////////////////////////////
//
//  switchAFAssistState () -
//! \brief switch ae  to "AF assist" state
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
void switchAFAssistState(strAEOutput *aeoutput);

/////////////////////////////////////////////////////////////////////////
//
//  switchAELock () -
//! \brief switch ae  to "AE lock" state
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
void switchAELock(strAEOutput *aeoutput);

/////////////////////////////////////////////////////////////////////////
//
//   backupPreviewAEStableInfo () -
//\brief back up preview AE state
//
//
//
/////////////////////////////////////////////////////////////////////////
void  backupPreviewAEStableIndex();

/////////////////////////////////////////////////////////////////////////
//
//   switchPreviewAERestore () -
//\brief switch ae  to  preview AE restore state
//
//
//
/////////////////////////////////////////////////////////////////////////
void  restorePreviewAEIndex();

/////////////////////////////////////////////////////////////////////////
//
//  lockAE(BOOL bLockAE)
//! \brief lock the AE
//!
//!
//! \return 0 if success, 1 if failed.
//
/////////////////////////////////////////////////////////////////////////
void lockAE(BOOL bLockAE);

/////////////////////////////////////////////////////////////////////////
//
//  getAEFrameRate () -
//! \brief get AE frame rate state
//!
//!
//! \return none.
//
/////////////////////////////////////////////////////////////////////////
void getAEFrameRate(strAEOutput *aeoutput);

/////////////////////////////////////////////////////////////////////////
//
//  getAERoughlyISO () -
//! \brief get AE roughly ISO speed
//!
//!
//! \return none.
//
/////////////////////////////////////////////////////////////////////////
void getAERoughlyISO(strAEOutput *aeoutput);

/////////////////////////////////////////////////////////////////////////
//
//  getAEPLineMappingID () -
//! \brief get AE mapping PLine  ID
//!
//!
//! \return none.
//
/////////////////////////////////////////////////////////////////////////
MRESULT  getAEPLineMappingID(LIB3A_AE_MODE_T  a_eAEModeID, eAETableID *pAEPLineID);

/////////////////////////////////////////////////////////////////////////
//
//  setExposureTime () -
//! \brief set exposure time
//!
//!
//! \return  void
//
/////////////////////////////////////////////////////////////////////////
void setExposureTime(MUINT32 a_usec);

/////////////////////////////////////////////////////////////////////////
//
//  setExposureTime () -
//! \brief  set exposure time
//!
//!
//! \return  void
//
/////////////////////////////////////////////////////////////////////////
void setIris(MUINT32 Iris);

/////////////////////////////////////////////////////////////////////////
//
//  setAFEGain () -
//!\brief  set AFE gain
//!
//!
//!\return void
//
/////////////////////////////////////////////////////////////////////////
void setAFEGain(MUINT32 afegain);

/////////////////////////////////////////////////////////////////////////
//
//  TransferGain2MicroDb () -
//! \brief  transfer Gain level to microDB
//!
//!
//! Transfer gain from 128 base to AFE digital number
//!
//! \param[in] a_u4Gain Gain leve 128 base
//!
//! \return AFE gain digital number
//
/////////////////////////////////////////////////////////////////////////
MUINT32	TransferGain2MicroDb(MUINT32 a_u4Gain);

/////////////////////////////////////////////////////////////////////////
//
//  TransferMicroDb2Gain () -
//! \brief  transfer microDB to gain
//!
//!
//! Transfer micro DB to gain 128 base
//!
//! \param[in] micro DB
//!
//! \return	 gain level 128 base
//
/////////////////////////////////////////////////////////////////////////
MUINT32	TransferMicroDb2Gain(MUINT32 a_u4Gain);

/////////////////////////////////////////////////////////////////////////
//
//  set3AFactor
//! \brief Pass All 3A factor to sub module
//!
//! \param in  struct_3A_Factor
//!
//! \return void
//
/////////////////////////////////////////////////////////////////////////
//virtual void set3AFactor(const AAA_PARAM_T *a_p3AFactor, const AE_NVRAM_T *a_pAENVramFactor, AAA_CMD_SET_T *a_p3AMode);
virtual void set3AFactor(const AAA_PARAM_T *a_p3AFactor, const AE_NVRAM_T *a_pAENVramFactor, AE_STAT_CONFIG_T *a_p3AConfig, AAA_CMD_SET_T *a_p3AMode);

/////////////////////////////////////////////////////////////////////////
//
//!  getFlare(void);
//! \brief get flare level in 8 bit domain
//!
//! \param[none]
//!
//! \return flare
//
/////////////////////////////////////////////////////////////////////////
virtual MUINT32 getFlare(void);

/////////////////////////////////////////////////////////////////////////
//
//!    getBVvalue(void);
//! \brief get BV value
//!
//! \param[none]
//!
//! \return BV
//
/////////////////////////////////////////////////////////////////////////
virtual MINT32 getBVvalue(void);

/////////////////////////////////////////////////////////////////////////
//
//!    getAEPlineEVvalue(void);
//! \brief get AE Pline EV value
//!
//! \param[none]
//!
//! \return AE Pline EV
//
/////////////////////////////////////////////////////////////////////////
virtual MINT32 getAEPlineEVvalue(void);

/////////////////////////////////////////////////////////////////////////
//
//!  getFlareGainOffset();
//! \brief get flare gain and offset in 8 bit domain
//!
//! \param in AE mode and flare structure
//!
//! \return none
//
/////////////////////////////////////////////////////////////////////////
virtual void getFlareGainOffset(eAESTATE eAEMode, strFlareCFG *a_FlareGainOffset);

/////////////////////////////////////////////////////////////////////////
//
//  handlePreFlash()
//
//! pre-flash metering process
//  input : u4VDCnt
//  output : Error code
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT handlePreFlash(strAEOutput &a_pStoEvSetting,strPreFlashExtraInfo &a_pStoExInfo,ePreFlashOState &a_pPFOState);

/////////////////////////////////////////////////////////////////////////
//
//   allocPreFlashBufs()
//
//!  allocate buffers for preflash process
//   input : none
//   output : Error code
//
/////////////////////////////////////////////////////////////////////////
MRESULT allocPreFlashBufs(void);

/////////////////////////////////////////////////////////////////////////
//
//   freePreFlashBufs()
//
//!  free buffers for preflash process
//   input : none
//   output : Error code
//
/////////////////////////////////////////////////////////////////////////
MRESULT freePreFlashBufs(void);

/////////////////////////////////////////////////////////////////////////
//
// decidePreFlashExpNStrength () -
//! \brief  decide preflash exposure and duty time by F#,ISO,...
//!
//! \param[in] a_PreFlash
//!
//! \return error code.0:success
//
/////////////////////////////////////////////////////////////////////////
MRESULT  decidePreFlashExpNStrength(strPreFlashParam &a_PreFlash);

/////////////////////////////////////////////////////////////////////////
//
//  calStillFlash () -
//! \brief calculate still flash exposure parameters.
//!
//! \param[in] a_PreFlash
//!
//! \return  error code.0:success
//
/////////////////////////////////////////////////////////////////////////
MRESULT  calStillFlash(strPreFlashParam &a_PreFlash);

/////////////////////////////////////////////////////////////////////////
//
//  calStillFlash () -
//! \brief calculate still flash exposure parameters.
//!
//! \param[in] u4FlashTime
//!
//! \param[in] u4FlashGain
//!
//! \return  flash time
//
/////////////////////////////////////////////////////////////////////////
MUINT32	calFlashTime(MUINT32 u4FlashTime,MUINT32 u4FlashGain,strStrobePTbl* psStrobeTable);

/////////////////////////////////////////////////////////////////////////
//
//  getPFStat () -
//! \brief collect statistics for flash metering
//!
//! \param[in] a_u4Idx ,buffer index
//!
//! \return
//
/////////////////////////////////////////////////////////////////////////
MUINT32 getPreFlashStat(ePreFlashStatistic a_Idx);

/////////////////////////////////////////////////////////////////////////
//
//  isAFLampAutoOn () -
//! \brief need AFLampAutoOn or not.
//!
//! \param NONE
//!
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////

virtual MBOOL isAFLampAutoOn(void);

/////////////////////////////////////////////////////////////////////////
//
//  isAEFlashOn () -
//! \brief need preflash metering or not.
//!
//! \param NONE
//!
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MBOOL isAEFlashOn(void);

/////////////////////////////////////////////////////////////////////////
//
//  getPreFlashInfo()-
//! \brief get preflash stat. buffer and target level.
//!
//! \param a_pInfo
//!
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MUINT32 getPreFlashInfo(strFlashAWBInfo *a_pInfo);

/////////////////////////////////////////////////////////////////////////
//
//  evalPreFlashStat () -
//! \brief evaluate preflash statistics data,determine intermedia value.
//!
//! \param[in]
//!
//! \return
//
/////////////////////////////////////////////////////////////////////////
MVOID evalPreFlashStat(void);

/////////////////////////////////////////////////////////////////////////
//
//  isValidPFStat () -
//! \brief evaluate valid preflash statistics data.
//!
//! \param[in] a_pu4NFL,a_pu4WFL1,a_pu4WFL12,a_u4Idx
//!
//! \return TRUE:valid area, FALSE:skip area
//
/////////////////////////////////////////////////////////////////////////
MBOOL isValidPFStat(MUINT32 *a_pu4NFL,MUINT32 *a_pu4WFL1,MUINT32 *a_pu4WFL12,MUINT32 a_u4Idx);

/////////////////////////////////////////////////////////////////////////
//
//  transPreFlashSetting () -
//! \brief to transfer Ev settings to linear or aetable domain.
//!
//! \param[in] a_pstrEvSetting,eMode
//!
//! \return NONE
//
/////////////////////////////////////////////////////////////////////////
//MVOID transPreFlashSetting(strEvSetting *a_pstrEvSetting,ePreFlashEvSettingMode eMode);

/////////////////////////////////////////////////////////////////////////
//
//  deriveSubWinInfo () -
//! \brief to cal. sub SW win statistics info.
//!
//! \param[out] u4SX,u4SY,u4EX,u4EY,u4SubX,u4SubY
//!
//! \return NONE
//
/////////////////////////////////////////////////////////////////////////
MVOID deriveSubWinInfo(MUINT32 *u4SX,MUINT32 *u4SY,MUINT32 *u4EX,MUINT32 *u4EY,MUINT32 *u4SubX,MUINT32 *u4SubY);

/////////////////////////////////////////////////////////////////////////
//
//  removeWinStat () -
//! \brief to remove window statistics from global statistics.
//!
//! \param[in] pSrc,u4LineInByte,u4WinX,u4WinY
//!
//! \return NONE
//
/////////////////////////////////////////////////////////////////////////
MVOID removeWinStat(MUINT8 *a_pucSrc[AEFLASH_FRM_NUM], MUINT32 u4Offset, MUINT32 u4LineInByte, MUINT32 u4WinX, MUINT32 u4WinY);

/////////////////////////////////////////////////////////////////////////
//
//  getPreFlashHistInfo () -
//! \brief collect preflash image histogram info.
//!
//! \param[in] a_pucHist
//!
//! \param[out] a_pstrHistInfo
//!
//! \return NONE
//
/////////////////////////////////////////////////////////////////////////
MVOID getPreFlashHistInfo(MVOID *a_pucHist[AEFLASH_FRM_NUM],strHistInfo *a_pstrHistInfo);

/////////////////////////////////////////////////////////////////////////
//
//  trans2CapStoAWBStat () -
//! \brief statistics for capture strobe AWB.
//!
//! \param[in]
//!
//! \param[out]
//!
//! \return NONE
//
/////////////////////////////////////////////////////////////////////////
MVOID trans2CapStoAWBStat(void);

/////////////////////////////////////////////////////////////////////////
//
//  calHist () -
//! \brief calculate histogram kernel
//!
//! \param[in] a_pHistInfo Histogram configuration
//!
//! \param[out] a_pu4Hist Histogram buffer
//!
//! \param[out] a_u4Count total of histogram
//!
//!
//! \return  flash time
//
/////////////////////////////////////////////////////////////////////////
MRESULT  calHistCore(const HistCoreIn* a_pHistInfo ,HistCoreOut* a_pHistResult);

/////////////////////////////////////////////////////////////////////////
//
// calSoftwareHist () - calculate software histogram
//! \brief calculate histogram
//!
//! \param [none]
//!
//!
//! \return  MRESULT
//
/////////////////////////////////////////////////////////////////////////
MRESULT calSoftwareHist(void);

/////////////////////////////////////////////////////////////////////////
//
//  checkDSCDirection () - according AE information to guess DSC direction
//! \brief calculate histogram
//!
//! \param [none]
//!
//!
//! \return  MUINT32 direction
//
/////////////////////////////////////////////////////////////////////////
MUINT32 checkDSCDirection(void);

/////////////////////////////////////////////////////////////////////////
//
//   setAETable ()-
//! \brief set AE table
//!
//! \param a_AETableID  AE Table ID
//!
//!
//! \return 0 if success, errcode if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT setAETable(eAETableID a_AETableID);
MRESULT searchPreviewIndexLimit( );

/////////////////////////////////////////////////////////////////////////
//
//   getISOValue ()-
//! \brief transfer ISO speed from enum to unsigned int
//!
//! \param in  a_IsoEnum  iso speed enum
//! \param out a_iso iso speed int
//!
//! \return  MRESULT error code
//
/////////////////////////////////////////////////////////////////////////
MRESULT getISOValue(LIB3A_AE_ISO_SPEED_T a_IsoEnum,MUINT32 *a_iso);

/////////////////////////////////////////////////////////////////////////
//
//   testAE ()-
//! \brief set AE table
//!
//! \param a_AETableID  AE Table ID
//!
//!
//! \return 0 if success, errcode if failed.
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT testAE(MUINT32 testID);

/////////////////////////////////////////////////////////////////////////
//
//   setFaceROI ()-
//! \brief set Face ROI
//!
//! \param a_FaceROI face ROI base on window information
//!
//!
//! \return MRESULT
//
/////////////////////////////////////////////////////////////////////////
//virtual MRESULT setFaceROI(srtNu3AFaceRoi &a_FaceROI,MBOOL bWithFace);

/////////////////////////////////////////////////////////////////////////
//
//   enableFaceAE ()-
//! \brief enable Face AE
//!
//! \param a_EnableFaceAE face AE
//!
//! \return MRESULT
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT enableFaceAE(MBOOL a_EnableFaceAE);

/////////////////////////////////////////////////////////////////////////
//
//    getAEInterInfo
//! \brief get AE internal information
//!
//! \param [out] a_AEInterInfo  AE internal information
//!
//! \return MRESULT
//
/////////////////////////////////////////////////////////////////////////
virtual MRESULT getAEInterInfo(strAEInterInfo  *a_AEInterInfo);

/////////////////////////////////////////////////////////////////////////
//
//    getLCEIndicator
//! \brief  AE to decide turn on LCE or not
//!
//! \param
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MBOOL getLCEIndicator( );

/////////////////////////////////////////////////////////////////////////
//
//   enableAEDebugInfo();
//! \brief  enable AE debug information print out CLI
//!
//! \param [in]  MBOOL enable
//!
//! \return MBOOL
//
/////////////////////////////////////////////////////////////////////////
virtual MVOID enableAEDebugInfo(MBOOL enable);

/////////////////////////////////////////////////////////////////////////
//
//  processAE () -
//! \brief AE main function
//!
//!
//! \return void
//
/////////////////////////////////////////////////////////////////////////
void processAE(strAEOutput *aeoutput);

virtual MRESULT prepareFrameRateInfo();
virtual MINT32 getEVCompensateIndex(LIB3A_AE_EVCOMP_T a_eEVIndex);

/////////////////////////////////////////////////////////////////////////
//
//   getFaceYforLCE
//! \brief get Face Y for LCE (after gamma)
//!
//! \param [in]strFaceYforLCEin
//!    [out] FaceY;
//!
//! \return MRESULT
//
/////////////////////////////////////////////////////////////////////////
//virtual MRESULT getFaceYforLCE(strFaceYforLCEin* strIn, MUINT32 *FaceY);

AE_DEBUG_USE virtual MRESULT setAETableIndex(MUINT32 a_idx);

//2 data

// factor need be assign  value
void* m_pAEStsBufferAdd;      //AE statistic buffer
void* m_pAWBStsBufferAdd;
void* m_pAEHisBufferAdd;
void* m_pFlareHisBufferAdd;
void* m_pFaceStsBufferAdd;   

MUINT32 m_u4BlockCnt;
MUINT32 m_pAWBSatCount;
eAESTATE m_eAeState;
MUINT32 m_u4flare;
LIB3A_AE_ISO_SPEED_T  m_eISOSpeed;
LIB3A_AE_MODE_T     m_eAEMode;


MUINT32 m_u4Index;       // current AE index
MUINT32 m_u4PreviewIndexBackup;
MUINT32 m_u4IndexMax,m_u4IndexMin;  //boundary of AE table
MUINT32 m_u4CWValue;     // center weighting value
MUINT32 m_u4AvgWValue;     // average weighting value
MUINT32 m_u4CWRecommend; // recommend cw target (after backlight, anti-overxposure...)
MUINT32 m_u4CentralY;            //Y value of central block
MUINT32 m_pu4AEBlock[AE_BLOCK_NO][AE_BLOCK_NO];   // AE block value in Algorithm level
MUINT32 m_u4AECondition;         // record backlight, hist stretch. over exp... condition , hit or not
MUINT32 m_u4BacklightCWM;
MUINT32 m_u4AntiOverExpCWM;
MUINT32 m_u4HistoStretchCWM;
MINT32 m_i4EVLockIndex;
MUINT32 m_u4LockIndex; 

const MUINT32 *m_pu4GFilter;         // Gaussian;                 /

const AAA_PARAM_T *         m_p3AFactor;
strHistInfo     m_sHistInfo;              //histogram infomation , mind from original histogram 96bin
//strHistConfig   m_sHistConfig;        // control histogram
//AWBSTAT_CONFIG_BLK  m_sWinConfig;         //AE window configuration , hardware level
//strAEConfig     m_AeConfig;              // AE behavior configuration
//AE_DEVICES_INFO_T m_AeDeviceConfig;
//AE_HIST_CFG_T m_sHistConfig;
strAEParamCFG m_AEParamConfig;
AE_CCT_CFG_T m_AeCCTConfig;
AE_STAT_CONFIG_T  *m_AEStatConfig;

const AE_NVRAM_T* m_pAENVRAMFactor;

strTimeLPF      m_sTimeLPF;                 //time domain lpf

strLpfConfig    m_sLPFCConfig;


strWeightTable *m_pWtTbl;                      //weighting table

MUINT32 m_pu4CentralHist[256];
MUINT32 m_pu4FullHist[256];
MUINT32 m_pu4GroundHist[256];
MUINT32 m_pu4FaceHist[256];
MUINT32 m_pu4FlareHist[10];

MUINT32 m_u4CentralHistCount;
MUINT32 m_u4FullHistCount;
MUINT32 m_u4GroundHistCount;
MUINT32 m_u4FaceHistCount;
MUINT32 m_u4FlareHistCount;

strAETable *m_pPreviewTableForward;
strAETable *m_pPreviewTableBackward;
strAETable *m_pPreviewTableCurrent;

strAETable *m_pCaptureTable;
strEvSetting m_CaptureSetting;

LIB3A_AE_EVCOMP_T   m_eEVCompValue;
LIB3A_AE_FLICKER_MODE_T m_eFlickerValue;
LIB3A_AE_FLICKER_AUTO_MODE_T m_eFlickerAutoValue;
LIB3A_AE_FLICKER_AUTO_MODE_T m_eFlickerAutoBackupValue;   // restore for the 
LIB3A_AE_PREVIEW_MODE_T m_eAEPreviewMode;
LIB3A_AE_PREVIEW_MODE_T m_eAEPrePreviewMode;
MINT32 m_eFrameRateValue;

MBOOL m_bEnableAE;
MBOOL m_bAEon;        //default Eanable , only turn off in debug mode
                     // if disalbe ,All AE system will freeze , even transfer from capture to preview

//srtNu3AFaceRoi m_FaceROI ; //window base
MBOOL m_bWithFace;
MUINT32 m_u4FaceY;
MINT16 m_i2FaceDiffIndex;

LIB3A_AE_STROBE_MODE_T m_eAEFlashMode;
MBOOL m_bAEFlashOn;
MBOOL m_bRedEyeFlashOn;
MINT32 m_i4AEMeteringModeStatus;
MINT32 m_i4StrobeStatus;
MUINT32 m_u4AETarget;
MBOOL m_bAETable;

MINT32 m_i4LowHighPercentage;
MUINT32 m_PreAEOrgIndex;  // for auto paranoma
MUINT32 m_PreAEIndex;  // for auto paranoma

ePreFlashIState m_ePreFlashIState;
strPreFlashParam m_PreFlashParam;
strPreFlashStatInfo m_strPFEvalStatInfo;
strPreFlashExtraInfo m_strAEPFExInfo;
MINT32 m_i4AEPFFlashIdx;
MUINT32 m_pPFHWStatBuf[AEFLASH_FRM_NUM][XGRID*YGRID];
MUINT32 m_pPFHist[AEFLASH_FRM_NUM][AEFLASH_HIST_SIZE];
MUINT32 m_pPFSWStatBuf[AEFLASH_FRM_NUM][XGRID*YGRID];
MUINT8  m_pPFEffWin[XGRID*YGRID];

MUINT32 m_u4Dir;             //check direction of image
MUINT32 m_u4DirAvg[4];

MUINT8* m_pAEStatsMap;
MINT32  m_BV;

MBOOL  m_bAEPrintOutDebug;
LIB3A_AE_METERING_MODE_T m_AEMetering;

MINT32  m_u4MaxFrameRate;
MINT32  m_u4MinFrameRate;
MINT32  m_u4SupportFrameRateNum;
MINT32  m_u4SupportFrameRateRange[30];

MBOOL  m_bAEFlatness;
MBOOL  m_bAEOneShot;
MBOOL  m_bAENoChange;

AREA_T m_strAEMeterWinArea[AE_WIN_NUM_MATRIX];
MINT32 m_i4AreaWinCnt;
MBOOL m_AEMeteringEnable;
MBOOL m_bLockAE;	

AE_DEBUG_USE strAEOutput m_CaptureAESetting;
AE_DEBUG_USE MUINT32      m_u4CaptureIdx;

};

#endif

