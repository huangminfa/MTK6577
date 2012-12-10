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

extern "C" {
#include "common.h"
#include "miniui.h"
#include "ftm.h"
}
#include "AcdkIF.h"
#include "AcdkCCTFeature.h"

#define TAG                  "[MATV_PREV] "

static int bSendDataToACDK(ACDK_CCT_FEATURE_ENUM	FeatureID,
						   UINT8*					pInAddr,
						   UINT32					nInBufferSize,
						   UINT8*					pOutAddr,
						   UINT32					nOutBufferSize,
						   UINT32*					pRealOutByeCnt)
{
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 

    rAcdkFeatureInfo.puParaIn = pInAddr; 
    rAcdkFeatureInfo.u4ParaInLen = nInBufferSize; 
    rAcdkFeatureInfo.puParaOut = pOutAddr; 
    rAcdkFeatureInfo.u4ParaOutLen = nOutBufferSize; 
    rAcdkFeatureInfo.pu4RealParaOutLen = pRealOutByeCnt; 
    

    return (MDK_IOControl(FeatureID, &rAcdkFeatureInfo));
}

#ifdef __cplusplus
extern "C" {
#endif
int matv_preview_init()
{
    LOGD(TAG"Open ACDK \n"); 
    if (MDK_Open() == 0)
    {
        LOGE(TAG"MDK_Open() Fail \n"); 
        return -1;
    }
    //set ATV mode
    LOGD(TAG"Set ATV Mode \n"); 
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 
    INT32 srcDev = 2; //ATV
    rAcdkFeatureInfo.puParaIn = (UINT8*)&srcDev; 
    rAcdkFeatureInfo.u4ParaInLen = sizeof(INT32); 
    rAcdkFeatureInfo.puParaOut = NULL; 
    rAcdkFeatureInfo.u4ParaOutLen = 0; 
    rAcdkFeatureInfo.pu4RealParaOutLen = NULL; 
    MDK_IOControl(ACDK_CCT_FEATURE_SET_SRC_DEV, &rAcdkFeatureInfo);

    LOGD(TAG"Init ACDK \n"); 
    if (MDK_Init() == 0) 
    {
        LOGE(TAG"MDK_Init() Fail \n"); 
        return -1; 
    }
    return 0;
}

int matv_preview_deinit()
{
    MDK_DeInit(); 
    MDK_Close();
    return 0;
}

int matv_preview_start()
{
    LOGD(TAG"ACDK_CCT_OP_PREVIEW_LCD_START\n"); 

    ACDK_CCT_CAMERA_PREVIEW_STRUCT rCCTPreviewConfig; 
    rCCTPreviewConfig.fpPrvCB = NULL; 
    rCCTPreviewConfig.u2PreviewWidth = 320; 
    rCCTPreviewConfig.u2PreviewHeight = 240; 
  
    unsigned int u4RetLen = 0; 
    

    bool bRet = bSendDataToACDK (ACDK_CCT_OP_PREVIEW_LCD_START, (unsigned char *)&rCCTPreviewConfig, 
                                 sizeof(ACDK_CCT_CAMERA_PREVIEW_STRUCT),
                                 NULL,
                                 0, 
                                 &u4RetLen);    

    if (!bRet)
    {
        LOGE(TAG"ACDK_CCT_OP_PREVIEW_LCD_START Fail\n"); 
    }
    //camera_state = CAMERA_STATE_PREVIEW; 
    
    return 0;
}

int matv_preview_stop()
{
    LOGD(TAG"ACDK_CCT_OP_PREVIEW_LCD_STOP \n"); 

    unsigned int u4RetLen = 0; 
    bool bRet = bSendDataToACDK(ACDK_CCT_OP_PREVIEW_LCD_STOP, NULL, 0, NULL, 0, &u4RetLen);

    if (!bRet)
    {
        return 1;
    }
    //camera_state = CAMERA_STATE_IDLE; 
    return 0;
}

int matv_preview_reset_layer_buffer(void)
{
    unsigned int u4RetLen = 0; 
    bool bRet = 0; 
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 

    rAcdkFeatureInfo.puParaIn = NULL; 
    rAcdkFeatureInfo.u4ParaInLen = 0; 
    rAcdkFeatureInfo.puParaOut = NULL; 
    rAcdkFeatureInfo.u4ParaOutLen = 0; 
    rAcdkFeatureInfo.pu4RealParaOutLen = &u4RetLen; 
    
    bRet = MDK_IOControl(ACDK_CCT_FEATURE_RESET_LAYER_BUFFER, &rAcdkFeatureInfo);
    if (!bRet)
    {
        return 1;
    }
    return 0;      
}

#ifdef __cplusplus
};
#endif

