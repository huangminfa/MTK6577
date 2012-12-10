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

#ifndef _VT_CSR_Q_H
#define _VT_CSR_Q_H
extern vt_csr_Q_struct vt_csr_downlink_queue;
extern vt_csr_Q_struct vt_csr_uplink_queue;
extern  kal_uint8 VT_CSR_default_buffer[92];

void vt_csr_queue_reset(void);

void vt_csr_queue_init(void);

void vt_csr_send_downlink_data_ind(void);

void vt_csr_send_uplink_data_ind(void);

void vt_uplink_Q_overflow(void);

RvInt32 vt_queue_check_underflow(
    vt_csr_Q_struct *queue, 
    RvUint32 len) ;
    
RvInt32 vt_queue_check_overflow(
    vt_csr_Q_struct *queue, 
    RvUint32 len) ;

RvUint8 * vt_csr_queue_get_read_buff(
    vt_csr_Q_struct *queue,
    RvUint32 * buff_len,
    kal_bool IsCheck);
    
RvUint8 * vt_csr_queue_get_write_buff( 
    vt_csr_Q_struct *queue,
    RvUint32 * buff_len, 
    kal_bool IsCheck);

void vt_csr_queue_consume_write_buff( 
    vt_csr_Q_struct *queue, 
    RvUint32  buff_len);
    
void vt_csr_queue_consume_read_buff(
    vt_csr_Q_struct *queue, 
    RvUint32  buff_len);

kal_uint16 vt_csr_queue_query_used_size(
    vt_csr_Q_struct *queue);

void vt_prepare_for_transmit(void);

void vt_init_defaut_csr_buffer(Rv3G324mCallStateMode Mode);

kal_uint32 vt_csr_queue_consume_and_query_read_buff(
    vt_csr_Q_struct *queue,
    RvUint32 buff_len);

#endif
