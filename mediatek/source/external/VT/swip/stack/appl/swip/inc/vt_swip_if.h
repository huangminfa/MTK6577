/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
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
* Filename:
* ---------
*  vt_swip_if.h
*
* Project:
* --------
*   MAUI
*
* Description:
* ------------
*   SWIP interface declarations
*
* Author:
* -------
*  SH Yang
*
*==============================================================================
*           HISTORY
* Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 *------------------------------------------------------------------------------
* Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
*==============================================================================
*******************************************************************************/

#ifndef __VT_SWIP_IF__
#define __VT_SWIP_IF__

#include "med_vt_struct.h"
#include "vt_swip_struct.h"
#include "vt_em_if.h"

//[2010-05-31] Chiwei: Add for removing warnings
#include "mmi_vt_struct.h"
#ifdef __cplusplus
extern "C" {
#endif

/****************************************************************************
  ****************************************************************************
  *
  * Function Redefinition
  *
  ****************************************************************************
  ****************************************************************************/

#define vt_put_downlink_audio_data         VtStk_AudioPutRxPacket
#define vt_downlink_video_Q_enqueue_packet VtStk_VideoPutRxPacket
#define VideoCall_Enc_UpdateI              VtStk_VideoEncFastUpdate
#define vt_audio_set_av_skew               VtStk_AudioSetMaxSkew
#define vt_video_get_vos                   VtStk_VideoGetDecConfig
#define SP3GVT_DL_PutSpeechFrame(a,b,c,d)  VtStk_AudioPutRxPacket(a,b,c)


/****************************************************************************
  ****************************************************************************
  *
  * SWIP <-> Application Interfaces 
  *
  ****************************************************************************
  ****************************************************************************/
VtStk_Status VtStk_InitStack();
void VtStk_EndStack();
VtStk_Status VtStk_CallActivateReq(kal_uint8 call_id);
VtStk_Status VtStk_TransportThreadActive();
VtStk_Status VtStk_CallDeactivateReq(kal_uint8 call_id, kal_uint8 end_type);
VtStk_Status VtStk_UserInputInd(kal_char* data, kal_uint8 size);

VtStk_Status VtStk_CallActivateCnf(void* para);
VtStk_Status VtStk_CallDeactivateCnf(void* para);
VtStk_Status VtStk_CallDiscInd(void*);


/****************************************************************************
  ****************************************************************************
  *
  * SWIP <-> Audio Interfaces 
  *
  ****************************************************************************
  ****************************************************************************/
VtStk_Status VtStk_mediaChannelConfig(void* para);
#define IN
#define OUT

void VtStk_AudioPutRxPacket(    
        IN kal_uint8                 *pBuffer,
        IN kal_uint32                size,
        IN kal_bool                bAnyError);

void VtStk_AudioSetMaxSkew(
    IN kal_uint32 skew);


/****************************************************************************
  ****************************************************************************
  *
  * SWIP <-> Video Interfaces 
  *
  ****************************************************************************
  ****************************************************************************/

kal_bool VtStk_isStkMediaLoopback();

void VtStk_VideoPutRxPacket(
    IN kal_uint8     *pBuffer,
    IN kal_uint32    size,
    IN kal_bool      is_any_error);

void VtStk_VideoSetLocalQuality(void *para);

void VtStk_VideoSetH263Resolution(void* para);

kal_int32 VtStk_VideoEncFastUpdate(void);

kal_int32 VtStk_VideoGetDecConfig(
    IN kal_int32  type,
    IN kal_uint8* buffer,
    kal_uint32 *size);

kal_int32 VtStk_VideoGetPeerDecConfig(
    IN kal_int32  type,
    IN kal_uint8* buffer,
    kal_uint32 *size);


/****************************************************************************
  ****************************************************************************
  *
  * SWIP <-> Transceiver Interfaces 
  *
  ****************************************************************************
  ****************************************************************************/

/****************************************************************************
 ****************************************************************************/
VtStk_Status VtStk_mediaLoopbackActReq(vt_mdi_loopback_mode_enum mode_option);
VtStk_Status VtStk_mediaLoopbackDeactReq(void);
void VtStk_EndStack();

/****************************************************************************
  ****************************************************************************
  *
  * SWIP <-> Audio Interfaces 
  *
  ****************************************************************************
  ****************************************************************************/


void vtStk_AudioPutTxPacket(IN kal_uint8* data,IN kal_uint32 size,IN kal_uint8  session_id);



/****************************************************************************
  ****************************************************************************
  *
  * SWIP <-> Video Interfaces 
  *
  ****************************************************************************
  ****************************************************************************/

void vtStk_VideoPutTxPacket(IN kal_uint8* data,IN kal_uint32 size, IN kal_uint8  type,IN kal_uint8  session_id);

void VtStk_AudioSetCodecReady(IN kal_uint8 bAudioReady);
VtStk_Status VtStk_VideoReqFastUpdate(void);
VtStk_Status VtStk_VideoSetPeerQuality(vt_vq_option_enum choice);

VtStk_Status VtStk_EMSetConfigReq(l4cvt_em_set_config_req_struct* em_config);

void vt_send_channel_active_message(int flag);

#ifdef __cplusplus
}
#endif

#endif
