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
 * AudioCompFltCustParam.cpp
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
 *   Tina Tsai (mtk01981)
 *
 *------------------------------------------------------------------------------
 * $Revision: #2 $
 * $Modtime:$
 * $Log:$
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

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "AudioCompFltCustParam"

namespace android {

#include "CFG_Audio_Default.h"
#include <cutils/properties.h>
#define MAX_RETRY_COUNT 20


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


void getDefaultAudioCompFltParam(AUDIO_ACF_CUSTOM_PARAM_STRUCT *audioParam)
{
    memcpy((void*)audioParam, (void*)&(audio_custom_default), sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
}

int  GetAudioCompFltCustParamFromNV(AUDIO_ACF_CUSTOM_PARAM_STRUCT *audioParam)
{
   int result = 0;

   F_ID audio_nvram_fd;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_COMPFLT_LID;
   int i = 0, rec_sizem, rec_size, rec_num;
   if(!checkNvramReady())
   {
       LOGW("checkNvramReady fail");
   }

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   result = read(audio_nvram_fd.iFileDesc, audioParam, rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}

int  SetAudioCompFltCustParamToNV(AUDIO_ACF_CUSTOM_PARAM_STRUCT *audioParam)
{

   // write to NV ram
   F_ID audio_nvram_fd;
   int file_lid = AP_CFG_RDCL_FILE_AUDIO_COMPFLT_LID;
   int i = 0, rec_sizem, rec_size, rec_num,result;

   audio_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   result = write(audio_nvram_fd.iFileDesc, audioParam, rec_size*rec_num);
   NVM_CloseFileDesc(audio_nvram_fd);

   return result;
}


}; // namespace android
