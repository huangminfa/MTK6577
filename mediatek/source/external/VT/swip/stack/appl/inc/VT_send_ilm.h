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

#ifndef _VT_SEND_ILM_
#define _VT_SEND_ILM_

#include "vt_option_cfg.h"

void vt_send_em_msg(
    module_type src_mod_id, 
    module_type dst_mod_id, 
    kal_uint16 msg_id, 
    kal_uint16 sap_id, 
    void *local_param_ptr,
    void* peer_buf_ptr);

void vt_send_msg(
    module_type src_mod_id, 
    module_type dst_mod_id, 
    kal_uint16 msg_id, 
    kal_uint16 sap_id, 
    void *local_param_ptr,
    void* peer_buf_ptr);
    
extern void vcall_send_video_replenish_data_ready_ind(kal_uint8 session_id);
  
extern void vt_med_send_audio_loopback_data(kal_uint8 *data, kal_uint32 size);

extern void vt_med_send_video_loopback_data(kal_uint8 *data, kal_uint32 size);

extern void vt_med_send_chl_off_status_ind(void);

extern void vt_med_send_chl_status_ind(    
    void *chl_status);

extern void vt_csm_send_activate_cnf(
    RvInt32 CallId,
    kal_bool result,
    RvUint8 cause);


extern void vt_csm_send_deactivate_cnf(
    kal_uint32 CallId, 
    kal_uint8 endType, 
    kal_bool result, 
    kal_uint8 cause);
//##########################################
//          temp solution
//##########################################
extern void vt_chl_status_init(void);
extern void vt_med_send_adjust_video_quality_ind(
                    kal_uint8 video_quality);
#ifndef __VT_SWIP__
void vt_send_med_dbg_dl_h245_msg_ind(kal_int32 msg_len, kal_uint8* msg);
void vt_send_med_dbg_ul_h245_msg_ind(kal_int32 msg_len, kal_uint8* msg) ;                  
#else
#define vt_send_med_dbg_dl_h245_msg_ind(a, b)   do{} while(0);
#define vt_send_med_dbg_ul_h245_msg_ind(a, b)   do{} while(0);
#endif
void vt_sp3g_send_callback_ind(kal_uint8 event);

void vt_csm_send_disc_ind(
    RvInt32 CallId, 
    kal_uint8 endType);

#if  (VT_SIM_MODE == SIM_STK_RX_FILE) && !defined(__VT_SWIP__)
void vt_modis_send_csm_activate_req();
void vt_modis_send_csm_deactivate_req();
#endif

#endif
