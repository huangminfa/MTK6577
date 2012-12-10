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

#ifndef _APP_EIS_H
#define _APP_EIS_H

#include "MTKEIS.h"

using namespace android;

#define EIS_DEBUG_TAG_SIZE 30
#define EIS_DEBUG_SAVE_CNT 600  // 1sec x600

typedef struct   // 304bytes
{
    eis_ori_stat_t sEIS_Ori_Stat;   // 184 bytes

    MUINT32 u4TrustW_X[2];
    MUINT32 u4TrustW_Y[2];

    MUINT32 u4ClusterV_X[5];
    MUINT32 u4ClusterV_Y[5];

    MUINT32 u4ClusterW_X[2];
    MUINT32 u4ClusterW_Y[2];

    MUINT32 u42NDLMVW_X[2];
    MUINT32 u42NDLMVW_Y[2];

    MUINT32 u4MinSADW_X[2];
    MUINT32 u4MinSADW_Y[2];

    MUINT32 u4GMV;
    MUINT32 u4CMV;
    MUINT32 u4DIV;
    MUINT32 u4REV2;    
    
} EIS_DEBUG_TAG_T;

typedef struct   // 16 bytes
{
    MUINT32 u4CamSize;
    MUINT32 u4SrcSize;    
    MUINT32 u4TarSize;
    MUINT32 u4Margin;

} EIS_DEBUG_HEAD_T;

typedef struct
{
    EIS_DEBUG_TAG_T Tag[EIS_DEBUG_TAG_SIZE];

} EIS_DEBUG_INFO_T;

/*******************************************************************************
*
********************************************************************************/
class AppEIS  : public MTKEIS {
public:
    static MTKEIS* getInstance();
    virtual MVOID destroyInstance();
        
private:
    AppEIS();    
    virtual ~AppEIS();
    virtual MINT32 init();
    virtual MINT32 uninit();
    virtual MVOID saveToFile(MINT8 *fname, MUINT8 *buf, MUINT32 size, MBOOL mode);

    virtual MVOID runGMV();    
    virtual MVOID LMV_TrustValue();
    virtual MVOID LMV_Cluster();
    virtual MVOID LMV_Center();    
    virtual MVOID LMV_2NDLMV();
    virtual MVOID LMV_MINSAD();
    
    virtual MVOID runCMV();    
    virtual MVOID CMV_VirtualSpring();    
    virtual MVOID CMV_PI();    
    virtual MVOID CMV_MPI();
    virtual MVOID PanDetect(MINT32 a_i4VoteIdx_X, MINT32 a_i4VoteIdx_Y, MBOOL &a_bIsPan_X, MBOOL &a_bIsPan_Y);    
            
public:

    virtual MVOID enableEIS(app_eis_config_t a_sEisConfig, app_eis_factor_t &a_sEisFactor);   
    virtual MVOID disableEIS();    
    virtual MINT32 handleEIS(MINT32 &a_i4CMV_X, MINT32 &a_i4CMV_Y);
    virtual MVOID setRecordInfo(MBOOL a_bEnable);
    virtual MVOID setSensitivity(EIS_SENSI a_i4Sensitivity);
	
    virtual MVOID getSWGMV(MINT32 &a_i4GMV_X, MINT32 &a_i4GMV_Y);
    virtual MVOID setEISStat(eis_stat_t a_sEIS_Stat);
    virtual MVOID setEISOriStat(eis_ori_stat_t a_sEIS_OriStat);
    virtual MVOID setDIVinfo(MINT32 a_i4DIV_H, MINT32 a_i4DIV_V);


private:

    mutable Mutex mLock;
    
    eis_stat_t   m_sEIS_Stat;
    app_eis_config_t m_sEIS_Config;
    app_eis_factor_t m_sEIS_Factor;

    MINT32 m_i4Framecnt;
    MINT32 m_i4Path;
    MINT32 m_i4GMV_X;
    MINT32 m_i4GMV_Y;
    MINT32 m_i4CMV_X;
    MINT32 m_i4CMV_Y;    
    MINT32 m_i4Margin_X;
    MINT32 m_i4Margin_Y;
    MINT32 m_i4Weight_X[16];
    MINT32 m_i4Weight_Y[16];

	MINT32 m_i4DIV_H;
	MINT32 m_i4DIV_V;	

    EIS_DEBUG_HEAD_T m_sEISDebugHead;
    EIS_DEBUG_INFO_T m_sEISDebugInfo;
    
    MINT8   m_iFileName[40];
    MUINT32 m_u4DebugCnt;
    MUINT32 m_u4SaveCnt;
    MBOOL   m_bRecordEnable;
    MBOOL   m_bEISenable;
    
    MINT32  m_i4CMV_ACC_X;
    MINT32  m_i4CMV_ACC_Y;
    
    MINT32  m_i4GMVLPF_X[3];
    MINT32  m_i4GMVLPF_Y[3];    
    MINT32  m_i4CMVLPF_X[3];
    MINT32  m_i4CMVLPF_Y[3];    

    MBOOL   m_bIsPanning_X;
    MBOOL   m_bIsPanning_Y;    
    MINT32  m_i4Pan_X[5];
    MINT32  m_i4Pan_Y[5];
    MINT32  m_i4TRUST_THRESHOLD;

    EIS_SENSI  m_i4Sensitivity;
};

#endif

