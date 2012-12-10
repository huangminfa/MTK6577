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
 * AudioCustParam.cpp
 *
 * Project:
 * --------
 *   Android
 *
 * Description:
 * ------------
 *   This file implements customized parameter handling
 *
 * Author:
 * -------
 *   HP Cheng (mtk01752)
 *
 *------------------------------------------------------------------------------
 * $Revision: #2 $
 * $Modtime:$
 * $Log:$
 *
 *
 *******************************************************************************/

/*=============================================================================
 *                              Include Files
 *===========================================================================*/
#if defined(PC_EMULATION)
#include "windows.h"
#else
#include "unistd.h"
#include "pthread.h"
#endif

#include <utils/Log.h>
#include <utils/String8.h>

#include "CFG_AUDIO_File.h"
#include "Custom_NvRam_LID.h"
#include "libnvram.h"
#include "CFG_Audio_Default.h"
#include <cutils/properties.h>

//#define USE_DEFAULT_CUST_TABLE

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "AudioYusuParam"

#define MAX_RETRY_COUNT 20


namespace android {

/*=============================================================================
 *                             Public Function
 *===========================================================================*/

bool checkNvramReady(void)
{
    int read_nvram_ready_retry = 0;
    int ret =0;
    char nvram_init_val[PROPERTY_VALUE_MAX];
    while(read_nvram_ready_retry < MAX_RETRY_COUNT)
    {
        read_nvram_ready_retry++;
        property_get("nvram_init",nvram_init_val,NULL);
        if(strcmp(nvram_init_val,"Ready") == 0)
        {
            ret = true;
            break;
        }
        else
        {
            usleep(500*1000);
        }
    }
    LOGD("Get nvram restore ready retry cc=%d\n",read_nvram_ready_retry);
    if(read_nvram_ready_retry >= MAX_RETRY_COUNT)
    {
        LOGW("Get nvram restore ready faild !!!\n");
        ret = false;
    }
    return ret;
}


void getDefaultSpeechParam(AUDIO_CUSTOM_PARAM_STRUCT *sphParam)
{
    // only for startup use
    LOGW("Digi_DL_Speech = %u",speech_custom_default.Digi_DL_Speech);
    LOGW("uMicbiasVolt = %u",speech_custom_default.uMicbiasVolt);
    LOGW("sizeof AUDIO_CUSTOM_PARAM_STRUCT = %d",sizeof(AUDIO_CUSTOM_PARAM_STRUCT));
    memcpy((void*)sphParam,(void*)&(speech_custom_default),sizeof(AUDIO_CUSTOM_PARAM_STRUCT));

}

int GetCustParamFromNV(AUDIO_CUSTOM_PARAM_STRUCT *pPara)
{
   int result = 0;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
        return 0;
   }
#if defined(USE_DEFAULT_CUST_TABLE)
   // a default value , should disable when NVRAM ready
   getDefaultSpeechParam(pPara);
   // get from NV ram and replace the default value
#else

   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_LID;
   int i = 0,rec_sizem,rec_size,rec_num;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   LOGD("GetCustParamFromNV audio_nvram_fd = %d",audio_nvram_fd);
   LOGD("GetCustParamFromNV rec_size = %d rec_num = %d",rec_size,rec_num);
   result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   LOGD("GetCustParamFromNV uMicbiasVolt = %d",pPara->uMicbiasVolt);
   NVM_CloseFileDesc(audio_nvram_fd);
#endif
   return result;
}

int SetCustParamToNV(AUDIO_CUSTOM_PARAM_STRUCT *pPara)
{
   // write to NV ram
   F_ID audio_nvram_fd;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_LID;
   int i = 0,rec_sizem,rec_size,rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   LOGD("SetCustParamToNV audio_nvram_fd = %d",audio_nvram_fd);
   result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

int GetAudioGainTableParamFromNV(AUDIO_GAIN_TABLE_STRUCT *pPara)
{
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_GAIN_TABLE_LID;
   int rec_sizem,rec_size,rec_num,result;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
        return 0;
   }

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   LOGD("GetCustParamFromNV audio_nvram_fd = %d rec_size = %d rec_num = %d",audio_nvram_fd,rec_size,rec_num);
   result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);
   return result;
}

int SetAudioGainTableParamToNV(AUDIO_GAIN_TABLE_STRUCT *pPara)
{
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_GAIN_TABLE_LID;
   int rec_sizem,rec_size,rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   LOGD("SetCustParamToNV audio_nvram_fd = %d",audio_nvram_fd);
   result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}


void getDefaultWBSpeechParam(AUDIO_CUSTOM_WB_PARAM_STRUCT *sphParam)
{
    // only for startup use
    /*
    LOGW("sizeof AUDIO_CUSTOM_WB_PARAM_STRUCT = %d",sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT));
    memcpy((void*)sphParam,(void*)&(wb_speech_custom_default),sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT));
    */
}


int GetCustWBParamFromNV(AUDIO_CUSTOM_WB_PARAM_STRUCT *pPara)
{
   int result = 0;
   // a default value , should disable when NVRAM ready
   //getDefaultWBSpeechParam(pPara);
   // get from NV ram and replace the default value


   F_ID audio_nvram_fd;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_WB_PARAM_LID;
   int i = 0,rec_sizem,rec_size,rec_num;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
        return 0;
   }

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   LOGD("GetCustWBParamFromNV audio_nvram_fd = %d",audio_nvram_fd);
   LOGD("GetCustWBParamFromNV rec_size = %d rec_num = %d",rec_size,rec_num);
   result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

int SetCustWBParamToNV(AUDIO_CUSTOM_WB_PARAM_STRUCT *pPara)
{

   // write to NV ram
   F_ID audio_nvram_fd;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_WB_PARAM_LID;
   int i = 0,rec_sizem,rec_size,rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   LOGD("SetCustWBParamToNV audio_nvram_fd = %d",audio_nvram_fd);
   result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

void getDefaultMedParam(AUDIO_PARAM_MED_STRUCT *pPara)
{
    // only for startup use
    LOGW("sizeof AUDIO_PARAM_MED_STRUCT = %d",sizeof(AUDIO_PARAM_MED_STRUCT));
    memcpy((void*)pPara,(void*)&(audio_param_med_default),sizeof(AUDIO_PARAM_MED_STRUCT));
}

int GetMedParamFromNV(AUDIO_PARAM_MED_STRUCT *pPara)
{
   int result =0;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
        return 0;
   }

#if defined(USE_DEFAULT_CUST_TABLE)
   // a default value , should disable when NVRAM ready
   getDefaultMedParam(pPara);
   // get from NV ram and replace the default value
#else
   F_ID audio_nvram_fd;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_PARAM_MED_LID;
   int i = 0,rec_sizem,rec_size,rec_num;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);
#endif
   return result;
}

int SetMedParamToNV(AUDIO_PARAM_MED_STRUCT *pPara)
{
   // write to NV ram
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_PARAM_MED_LID;
   int i = 0,rec_sizem,rec_size,rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

void getDefaultAudioCustomParam(AUDIO_VOLUME_CUSTOM_STRUCT *volParam)
{
    // only for startup use
    LOGW("sizeof AUDIO_VOLUME_CUSTOM_STRUCT = %d",sizeof(AUDIO_VOLUME_CUSTOM_STRUCT));
    memcpy((void*)volParam,(void*)&(audio_volume_custom_default),sizeof(AUDIO_VOLUME_CUSTOM_STRUCT));
}

// get audio custom parameter from NVRAM
int GetAudioCustomParamFromNV(AUDIO_VOLUME_CUSTOM_STRUCT *pPara)
{
    int result =0;
    F_ID audio_nvram_fd ;
    int file_lid = AP_CFG_RDCL_FILE_AUDIO_VOLUME_CUSTOM_LID;
    int i = 0,rec_sizem,rec_size,rec_num;
    if(!checkNvramReady())
    {
        LOGW("checkNvramReady fail");
        return 0;
    }
#if defined(USE_DEFAULT_CUST_TABLE)
    // a default value , should disable when NVRAM ready
    getDefaultAudioCustomParam(pPara);
    // get from NV ram and replace the default value
#else
    audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
    result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
    NVM_CloseFileDesc(audio_nvram_fd);
#endif
    return result;
}

int SetAudioCustomParamToNV(AUDIO_VOLUME_CUSTOM_STRUCT *pPara)
{
    // write to NV ram
    F_ID audio_nvram_fd;
    int file_lid = AP_CFG_RDCL_FILE_AUDIO_VOLUME_CUSTOM_LID;
    int i = 0,rec_sizem,rec_size,rec_num,result;

    audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
    result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
    NVM_CloseFileDesc(audio_nvram_fd);

    return result;
}



//////////////////////////////////////////////
// Dual Mic Custom Parameter
//////////////////////////////////////////////

void getDefaultDualMicParam(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *dualMicParam)
{
   LOGD("sizeof AUDIO_CUSTOM_PARAM_STRUCT = %d",sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT));
#if 0
   memcpy((void*)dualMicParam,(void*)&(dual_mic_custom_default),sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT));
#endif
}

// Get Dual Mic Custom Parameter from NVRAM
int Read_DualMic_CustomParam_From_NVRAM(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *pPara)
{
    if(!checkNvramReady())
    {
        LOGW("checkNvramReady fail");
         return 0;
    }

#if defined(USE_DEFAULT_CUST_TABLE)
// for test only
   // Get the Dual Mic default parameter, (Disable it when NVRAM ready)
   getDefaultDualMicParam(pPara);
   // get from NV ram and replace the default value
   return 1;
#else
    F_ID dualmic_nvram_fd;
    int file_lid = AP_CFG_RDCL_FILE_DUAL_MIC_CUSTOM_LID;
    int rec_size,rec_num,result;
    result = 0;

    dualmic_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
    LOGD("+Read_DualMic_CustomParam_From_NVRAM audio_nvram_fd = %d",dualmic_nvram_fd);
    LOGD("Read_DualMic_CustomParam_From_NVRAM, rec_size=%d, rec_num=%d",rec_size,rec_num);
    result = read(dualmic_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
    LOGD("-Read_DualMic_CustomParam_From_NVRAM");
    NVM_CloseFileDesc(dualmic_nvram_fd);
    return result;
#endif
}

// Set Dual Mic Custom Parameter from NVRAM
int Write_DualMic_CustomParam_To_NVRAM(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *pPara)
{
    F_ID dualmic_nvram_fd;
    int file_lid = AP_CFG_RDCL_FILE_DUAL_MIC_CUSTOM_LID;
    int rec_size,rec_num,result;
    result = 0;

    dualmic_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
    LOGD("+Write_DualMic_CustomParam_To_NVRAM audio_nvram_fd = %d",dualmic_nvram_fd);
    result = write(dualmic_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
    LOGD("-Write_DualMic_CustomParam_To_NVRAM");
    NVM_CloseFileDesc(dualmic_nvram_fd);
    return result;
}

//////////////////////////////////////////////
// HD Record Custom Parameter
//////////////////////////////////////////////
#if defined(MTK_AUDIO_HD_REC_SUPPORT)
/// Get HD record parameters from NVRAM
int GetHdRecordParamFromNV(AUDIO_HD_RECORD_PARAM_STRUCT *pPara)
{
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_HD_REC_PAR_LID;
   int rec_sizem,rec_size,rec_num,result;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
        return 0;
   }

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   LOGD("GetHdRecordParamFromNV rec_size = %d rec_num = %d", rec_size, rec_num);
   result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

/// Set HD record parameters to NVRAM
int SetHdRecordParamToNV(AUDIO_HD_RECORD_PARAM_STRUCT *pPara)
{
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_HD_REC_PAR_LID;
   int rec_sizem,rec_size,rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   LOGD("SetHdRecordParamToNV audio_nvram_fd = %d rec_size = %d rec_num = %d", audio_nvram_fd, rec_size, rec_num);
   result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

/// Get HD record scene tables from NVRAM
int GetHdRecordSceneTableFromNV(AUDIO_HD_RECORD_SCENE_TABLE_STRUCT *pPara)
{
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_HD_REC_SCENE_LID;
   int rec_sizem,rec_size,rec_num,result;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
        return 0;
   }

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   LOGD("GetHdRecordSceneTableFromNV rec_size = %d rec_num = %d", rec_size, rec_num);
   result = read(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

/// Set HD record scene tables to NVRAM
int SetHdRecordSceneTableToNV(AUDIO_HD_RECORD_SCENE_TABLE_STRUCT *pPara)
{
   F_ID audio_nvram_fd ;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_HD_REC_SCENE_LID;
   int rec_sizem,rec_size,rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   LOGD("SetHdRecordSceneTableToNV audio_nvram_fd = %d rec_size = %d rec_num = %d", audio_nvram_fd, rec_size, rec_num);
   result = write(audio_nvram_fd.iFileDesc, pPara , rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}
#endif

}; // namespace android
