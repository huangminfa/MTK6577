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

#ifndef VT_MSG_HDLR
#define VT_MSG_HDLR

extern void vt_csm_activate_req_hdlr(ilm_struct *ilm);

extern void vt_csm_deactivate_req_hdlr(ilm_struct *ilm);

extern void vt_csr_downlink_data_ind_hdlr(ilm_struct *ilm);

extern void vt_csr_uplink_data_ind_hdlr(ilm_struct *ilm);

extern kal_uint32 vt_csr_consume_downlink_data(kal_uint32 quota_remained);


extern void vt_test_req_hdlr(ilm_struct *ilm);

extern void vt_mdi_loopback_activate_req_hdlr(ilm_struct *ilm);

extern void vt_mdi_loopback_deactivate_req_hdlr(ilm_struct *ilm);

extern void vt_inject_msg_hdlr(ilm_struct *ilm_ptr);

extern void vt_loop_video_data_hdlr(ilm_struct *ilm);

extern void vt_loop_audio_data_hdlr(ilm_struct *ilm);

extern void vt_med_video_pause(ilm_struct *ilm);

extern void vt_med_video_resume(ilm_struct *ilm);

void vt_uii_handler(ilm_struct *ilm_ptr);

void vt_SP3G_callback_ind_hdlr(ilm_struct *ilm_ptr);

void vt_em_request_default_config_hdlr(ilm_struct* ilm_ptr);

void vt_l4c_em_config_hdlr(ilm_struct* ilm_ptr);

void vt_med_video_misc_cmd(ilm_struct* ilm_ptr);
//void vt_set_em_enable(kal_bool value);

void vt_em_update_hdlr(ilm_struct* ilm_ptr);

void vt_globals_reset(void);

void vt_med_codec_open_ack(ilm_struct* ilm_ptr);

void vt_med_put_ul_video_hdlr(ilm_struct *ilm_ptr);

void vt_csr_consume_dl_ind_hdlr(ilm_struct* ilm_ptr);

void vt_csm_msg_hdlr(ilm_struct* ilm);

void vt_med_msg_hdlr(ilm_struct* ilm);

void vt_mmi_msg_hdlr(ilm_struct* ilm);

void vt_self_msg_hdlr(ilm_struct* ilm);

void vt_csr_msg_hdlr(ilm_struct* ilm);

void vt_l1sp_msg_hdlr(ilm_struct* ilm);

void vt_tst_msg_hdlr(ilm_struct* ilm);

void vt_l4c_msg_hdlr(ilm_struct* ilm);

void vt_timer_msg_hdlr(ilm_struct* ilm);
#endif
