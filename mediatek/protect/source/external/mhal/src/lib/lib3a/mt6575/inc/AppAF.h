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

#ifndef _APP_AF_H
#define _APP_AF_H

#include "MTKAF.h"
#include <sys/time.h>


/*******************************************************************************
*
********************************************************************************/
class AppAF : public MTKAF {
public:

    /////////////////////////////////////////////////////////////////////////
    //
    //   AppAF () -
    //! \brief AppAF module constructor.
    //!
    //
    /////////////////////////////////////////////////////////////////////////

    AppAF();

    /////////////////////////////////////////////////////////////////////////
    //
    //   ~AppAF () -
    //! \brief AppAF module destructor.
    //!
    //
    /////////////////////////////////////////////////////////////////////////

    virtual ~AppAF();

    /////////////////////////////////////////////////////////////////////////
    //
    //   initAF () -
    //! \brief initAF
    //!
    //
    /////////////////////////////////////////////////////////////////////////

	virtual MRESULT initAF(AF_INPUT_T &a_sAFInput, AF_OUTPUT_T &a_sAFOutput);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 triggerAF() -
	//! \brief se  triggerAF
	//!
	//
	/////////////////////////////////////////////////////////////////////////

	virtual MRESULT triggerAF();

	/////////////////////////////////////////////////////////////////////////
	//
	//	 stopAF() -
	//! \brief se  stopAF
	//!
	//
	/////////////////////////////////////////////////////////////////////////

	virtual MRESULT stopAF();

    /////////////////////////////////////////////////////////////////////////
    //
    //   handleAF() - AF Entry Point
    //
    //   input : AF_INPUT_T &a_sAFInput, AF_OUTPUT_T &a_sAFOutput
    //   output: error message
    /////////////////////////////////////////////////////////////////////////

    virtual MRESULT handleAF(AF_INPUT_T &a_sAFInput, AF_OUTPUT_T &a_sAFOutput);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getDebugInfo() -
	//!
	//!
	//
	/////////////////////////////////////////////////////////////////////////

	virtual void getDebugInfo(AF_DEBUG_INFO_T &a_DebugInfo);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 setAFFactor() -
	//!
	//!
	//
	/////////////////////////////////////////////////////////////////////////

	virtual void setAFFactor(const AF_PARAM_T &a_sAFParam, const AF_NVRAM_T &a_sAFNvram, const AF_STAT_CONFIG_T &a_sAFStatConfig);

    /////////////////////////////////////////////////////////////////////////
    //
    //   setAFMode() - input AF Mode
    //
    //   input : enum AF mode
    //   output: error message
    /////////////////////////////////////////////////////////////////////////

    virtual MRESULT setAFMode(LIB3A_AF_MODE_T a_eAFMode);

    /////////////////////////////////////////////////////////////////////////
    //
    //   setAFMeter() - input AF Metering Mode
    //
    //   input : enum AF Meter
    //   output: error message
    /////////////////////////////////////////////////////////////////////////

    virtual MRESULT setAFMeter(LIB3A_AF_METER_T a_eAFMeter);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 setAFZone () -
	//!
	//!  input : enum AF zone
	//	 output: error message
	/////////////////////////////////////////////////////////////////////////

	virtual MRESULT setAFZone(LIB3A_AF_ZONE_T a_eAFZone);

	/////////////////////////////////////////////////////////////////////////
	//
	/////////////////////////////////////////////////////////////////////////

	virtual LIB3A_AF_MODE_T getAFMode();

	/////////////////////////////////////////////////////////////////////////
	//
	/////////////////////////////////////////////////////////////////////////

	virtual LIB3A_AF_METER_T getAFMeter();

	/////////////////////////////////////////////////////////////////////////
	//
	/////////////////////////////////////////////////////////////////////////

	virtual void setAFCoef(AF_COEF_T a_sAFCoef);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 isAFFinish() -
	//
	//	 input : none
	//	 output: 1 for finish, 0 for not yet
	/////////////////////////////////////////////////////////////////////////

	virtual MBOOL isAFFinish();

	/////////////////////////////////////////////////////////////////////////
	//
	//	 setFDWin () -
	//!
	//!  input : FD Win info
	//	 output: error message
	/////////////////////////////////////////////////////////////////////////

	virtual MRESULT setFDWin(const FD_INFO_T a_sFDInfo);

	/////////////////////////////////////////////////////////////////////////
	//
    //   setAFMoveSpotPos () -                                                                      
    //!                                                                   
    //!  input :                                  
    //   output: 
    /////////////////////////////////////////////////////////////////////////                  
    
    virtual void setAFMoveSpotPos(MUINT32 a_u4Xoffset,MUINT32 a_u4Yoffset,MUINT32 a_u4Width,MUINT32 a_u4Height,MUINT32 a_u4OffsetW,MUINT32 a_u4OffsetH,MUINT8 a_uOri);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 AFDrawRect () -
	//!
	//!  input :
	//	output: void
	/////////////////////////////////////////////////////////////////////////

	virtual void AFDrawRect(MUINT32 a_u4BuffAddr,MUINT32 a_u4Width,MUINT32 a_u4Height,MUINT32 a_u4OffsetW,MUINT32 a_u4OffsetH,MUINT8 a_uOri);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 setMFPos() - set MF Pos
	//
	//	 input : MINT32 a_i4Pos
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual void setMFPos(MINT32 a_i4Pos);

	/////////////////////////////////////////////////////////////////////////
	//
    //   clearAFWinResult() - clear AF Window result
    //
    //   input : none
    //   output: none
    /////////////////////////////////////////////////////////////////////////
    
    virtual void clearAFWinResult();

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getAFWinResult() - output AF Window result
	//
	//	 input : AF_WIN_RESULT_T
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual void getAFWinResult(AF_WIN_RESULT_T &a_sAFWinResult);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getAFBestPos() - get AF Best Pos
	//
	//	 input : none
	//	 output: MINT32
	/////////////////////////////////////////////////////////////////////////

	virtual MINT32 getAFBestPos();

	/////////////////////////////////////////////////////////////////////////
	//
    //   setFocusDistanceRange() -
    //
    //   input : none
    //   output: MINT32
    /////////////////////////////////////////////////////////////////////////
    
    virtual MINT32 setFocusDistanceRange(MINT32 a_i4Distance_N, MINT32 a_i4Distance_M);

    /////////////////////////////////////////////////////////////////////////
    //
    //   getFocusDistance() -
    //
    //   input : none
    //   output: MINT32
    /////////////////////////////////////////////////////////////////////////

    virtual MINT32 getFocusDistance(MINT32 &a_i4Near, MINT32 &a_i4Curr, MINT32 &a_i4Far);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getAFValue() - get AF Value
	//
	//	 input : none
	//	 output: MUINT32
	/////////////////////////////////////////////////////////////////////////

	virtual MUINT32 getAFValue();

/////////////////////////////////////////////////////////////////////////
    //
    //	 setAFFullStep() -
    //
    //	 input : MINT32 a_i4Step
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void setAFFullStep(MINT32 a_i4Step);

/////////////////////////////////////////////////////////////////////////
	//
	//	 getAFPos() - get AF Pos
	//
	//	 input : none
	//	 output: MINT32
	/////////////////////////////////////////////////////////////////////////

	virtual MINT32 getAFPos();

/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//1   Protected
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
protected:

    typedef enum
    {
        AF_DIR_LEFT       = -1,
        AF_DIR_RIGHT      =  1

    } AF_DIR_T;

	typedef enum
	{
		AF_STATUS_DONE = 0,
		AF_STATUS_INIT,
		AF_STATUS_PRERUN,
		AF_STATUS_SEEK,
		AF_STATUS_MOVETOBEST,
		AF_STATUS_DIRCHG,
		AF_STATUS_WAIT1,
		AF_STATUS_WAIT2,
		AF_STATUS_WAIT3   // add wait3 status

	} AF_STATUS_T;

	typedef enum
	{
		AF_CALI_STATUS_DONE = 0,
		AF_CALI_STATUS_INIT,
		AF_CALI_STATUS_WAIT,
		AF_CALI_STATUS_RUN

	} AF_CALI_STATUS_T;

	typedef enum
	{
		FD_STATUS_NONE = 0,
		FD_STATUS_DETECTED

	} FD_STATUS_T;

    typedef struct
    {
        MINT32 i4TotIdx;
        MINT32 i4MaxIdx[AF_WIN_NUM_MATRIX];
        MINT32 i4MinIdx[AF_WIN_NUM_MATRIX];
        MINT32 i4LMinIdx[AF_WIN_NUM_MATRIX];
        MINT32 i4RMinIdx[AF_WIN_NUM_MATRIX];
	MUINT32 u4MainThres[AF_WIN_NUM_MATRIX];
	MUINT32 u4SubThres[AF_WIN_NUM_MATRIX];
        MINT32 i4PeakScore[AF_WIN_NUM_MATRIX];

        MINT32 i4BestPos[AF_WIN_NUM_MATRIX];
	MBOOL  bPeakIsFound[AF_WIN_NUM_MATRIX];
	MBOOL  bSrchIsFinish[AF_WIN_NUM_MATRIX];

        MINT32 i4Pos[PATH_LENGTH];
        AF_STAT_T sStat[PATH_LENGTH];

        MINT32 i4MatrixWinIdx[AF_WIN_NUM_MATRIX];
        MINT32 i4MatrixWinCnt;

    } AF_PATH_T;

	/////////////////////////////////////////////////////////////////////////
	//
	//	 setAFPos() - set AF Pos
	//
	//	 input : MINT32
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual void setAFPos(MINT32 a_i4Pos);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 isAFMotorStop() -
	//
	//	 input : none
	//	 output: MBOOL
	/////////////////////////////////////////////////////////////////////////

    virtual MBOOL isAFMotorStop();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 setCoefInit
    //   brief setCoefInit
    //
    //   input : none
    //   output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void setCoefInit();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 runSpotAFS() - run Spot AF-S
    //
    //	 input : none
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    //virtual void runSpotAFS();

    /////////////////////////////////////////////////////////////////////////
    //
    //   runMatrixAFS() - run Matrix AF-S
    //
    //   input : none
    //   output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void runMatrixAFS();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 runAFC() - run AF-C
    //
    //	 input : none
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void runAFC();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 runMF() - run MF
    //
    //	 input : none
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void runMF();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 runAFCalibration() - run AF Calibration
    //
    //	 input : none
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void runAFCalibration();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 runAFFullScan() - run AF Full Scan
    //
    //	 input : none
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void runAFFullScan();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 runMatrixJudge() -
    //
    //	 input : none
    //	 output: MINT32
    /////////////////////////////////////////////////////////////////////////

    virtual MINT32 runMatrixJudge();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 findMaxMin()
    //
    //	 input :MINT32 a_i4WinIdx
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void findMaxMin(MINT32 a_i4WinIdx);

    /////////////////////////////////////////////////////////////////////////
    //
    //   findPeak() -
    //
    //   input : MINT32 a_i4WinNum
    //   output: MINT32
    /////////////////////////////////////////////////////////////////////////

    virtual MINT32 findPeak(MINT32 a_i4WinNum);


    virtual MINT32 CurveFit(MINT32 *i4x, MUINT32 *u4y);

    /////////////////////////////////////////////////////////////////////////
    //
    //	 isFVChange() -
    //
    //	 input : none
    //	 output: MBOOL
    /////////////////////////////////////////////////////////////////////////

    virtual MBOOL isFVChange();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 isPeak() -
    //
    //	 input : MINT32 a_i4WinIdx
    //	 output: MINT32
    /////////////////////////////////////////////////////////////////////////

    virtual MINT32 isPeak(MINT32 a_i4WinIdx);

    /////////////////////////////////////////////////////////////////////////
    //
    //	 getStepSize() -
    //
    //	 input : none
    //	 output: MINT32
    /////////////////////////////////////////////////////////////////////////

    virtual void getStepSize();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 setAFWinConfig() -
    //
    //	 input : none
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void setAFWinConfig();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 AFDebug() -
    //
    //	 input : a_u4FieldID, a_u4FieldValue, a_fgLineKeep
    //	 output: none
    /////////////////////////////////////////////////////////////////////////

    virtual void AFDebug(MUINT32 a_u4FieldID, MUINT32 a_u4FieldValue, MBOOL a_fgLineKeep);

    /////////////////////////////////////////////////////////////////////////
    //
    //	 getAFStat() - get AF statistic
    //
    //   input : none
    //	output: AF_STAT_T
    /////////////////////////////////////////////////////////////////////////

    virtual AF_STAT_T getAFStat();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 pauseFocus() - pauseFocus
    //
    //   input : none
    //	output: none
    /////////////////////////////////////////////////////////////////////////

	virtual void pauseFocus();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 isFocused() - return Focus OK of NG
    //
    //   input : none
    //	output: MBOOL
    /////////////////////////////////////////////////////////////////////////

	virtual MBOOL isFocused();

    /////////////////////////////////////////////////////////////////////////
    //
    //	 resetFocus() - reset Focus Position
    //
    //   input : none
    //	output: none
    /////////////////////////////////////////////////////////////////////////

	virtual void resetFocus();

	/////////////////////////////////////////////////////////////////////////
	//
	//	 setFocusAreas() - setFocusAreas
	//
	//	 input : none
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual void setFocusAreas(MINT32 a_i4Cnt, AREA_T *a_psFocusArea);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getFocusAreas() - getFocusAreas
	//
	//	 input : none
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual void getFocusAreas(MINT32 &a_i4Cnt, AREA_T **a_psFocusArea);

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getMaxNumFocusAreas() - getMaxNumFocusAreas
	//
	//	 input : none
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual MINT32 getMaxNumFocusAreas();

	/////////////////////////////////////////////////////////////////////////
	//
	//	 getAFWinResultCore() - output AF Window result
	//
	//	 input : AF_WIN_RESULT_T
	//	 output: none
	/////////////////////////////////////////////////////////////////////////

	virtual void getAFWinResultCore(AF_WIN_RESULT_T &a_sAFWinResult);

    virtual void isAEStable(MBOOL a_bAEStable);
    

    // --- Member Structure ---
    AF_PATH_T  	     m_sAFPath;
    AF_NVRAM_T       m_sAFNvram;
    AF_PARAM_T       m_sAFPara;
    AF_STEP_T        m_sAFStep;
    AF_OUTPUT_T      m_sAFOutput;
    AF_WIN_T         m_sFDWin;
    AF_WIN_T         m_sFDPreWin;
    AF_STAT_T        m_sAFStat;

    timeval          m_sAFtime1;
    timeval          m_sAFtime2;
    #if FRAME_TIME
    timeval          m_sFmtime1;
    #endif

    // --- Member Variable ---
    AF_STATUS_T   m_eAFStatus;
    AF_STATUS_T   m_eAFCStatus;    
    AF_MARK_T     m_eAFCMark;
    AF_MARK_T     m_eAFCMark_FocusedOrNot;	// only report OK or NG
    LIB3A_AF_MODE_T     m_eAFMode;
    LIB3A_AF_METER_T    m_eAFMeter;
    LIB3A_AF_METER_T    m_eRealMeter;
    LIB3A_AF_ZONE_T     m_eAFZone;
    FD_STATUS_T   m_eFDStatus;
    FD_STATUS_T   m_eFDPreStatus;
    AF_CALI_STATUS_T m_eAFCaliStatus;


    MBOOL    m_bUpdateAFStatConfig;
    MBOOL    m_bAFSBusy;
    MINT32   m_i4AFDir;
    MINT32   m_i4WinNum;
    MINT32   m_i4StepIdx;
    MINT32   m_i4StepPos;
    MINT32   m_i4LBound, m_i4RBound;
    MINT32   m_i4TotNum;
    MINT32   m_i4BestPos;
    MINT32   m_i4FullStep;

    // --- Matrix AF ---
    MBOOL   m_bFinishSearch;
    MBOOL   m_bSecondSearch;

    // --- AFC ---
    MBOOL   m_bAFCBusy;
    MINT32  m_i4PreBestPos[2];
    MINT32  m_i4StepSize;
    MUINT32 m_u4FV;
    MUINT32 m_u4LV;
    MINT32  m_i4DirChgCnt;
    MBOOL   m_bFDContiTrigger;
    MBOOL   m_bAFCFirstVlu;
    MBOOL   m_bAFSTrigger;
	MBOOL   m_bAFCTrigger;
    MINT32  m_i4AFCCnt;
	MBOOL   m_bAFC_Pause;

    // --- FD ---
    AF_WIN_RESULT_T m_sFDResult;

    // --- PreRun ---
    MINT32  m_i4PreRunCnt;

    // --- Debug Parser ---
    MINT32  m_i4DebugValidCnt;
    AF_DEBUG_INFO_T m_sAFDebugInfo;

    // --- AFInput ---
    MINT32  m_i4SceneLV;
    MINT32  m_i4AFSLV;
    FOCUS_INFO_T m_sFocusInfo;
    EZOOM_WIN_T m_sEZoom;
    EZOOM_WIN_T m_sEZoomOri;
    EZOOM_WIN_T m_sMoveSpot;

    MBOOL   m_bFDon;
    MBOOL   m_bisPreview;
    MBOOL   m_bMoveSpot;

    MINT32  m_i4ZoneN[17];
    MINT32  m_i4ZoneM[5];
    
    MINT32  m_i4Left;
    MINT32  m_i4Right;
    MINT32  m_i4Up;
    MINT32  m_i4Bottom;

	// Focus Window Controled by AP	
	MBOOL	        m_bFocusWin_APControl;
	MINT32          m_i4AREA_Cnt;
	AREA_T          m_sAREA[AF_WIN_NUM_MATRIX];
	AF_WIN_T        m_sAFWin;
    AF_WIN_RESULT_T m_sAFResult;

    AF_COEF_T m_sAFCoef;
    MBOOL           m_bAEStable;
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//1   Private
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
private:


};

#endif

