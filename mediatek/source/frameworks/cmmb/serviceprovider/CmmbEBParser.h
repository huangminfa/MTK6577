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

#ifndef _CMMB_EB_H
#define _CMMB_EB_H

#include "CmmbHelper.h"

/*****************************************************************************
* Define
*****************************************************************************/
#define CMMB_EB_MAX_SEGMENT 256
#define CMMB_EB_MAX_SEGMENT_MASK_SIZE (CMMB_EB_MAX_SEGMENT / 32)
#define CMMB_EB_MAX_MSG_NUM 16

#define CMMB_EB_MAX_LANG_DESC_NUM 16
#define CMMB_EB_MAX_AUX_INFO_NUM 16
#define CMMB_EB_NO_AUX_INFO 15
#define CMMB_EB_LANG_LEN 3


#define CMMB_EB_MAX_NAME_LEN 255
#define CMMB_EB_MAX_NAME_BUF_SIZE ((CMMB_EB_MAX_NAME_LEN + 1) * sizeof(char))
#define CMMB_EB_MAX_DATA_LEN (2 * 8192)

#define CMMB_EB_BUFFER_SIZE (4 * 8192)
/*****************************************************************************
* Enumeration
*****************************************************************************/
typedef enum
{
    CMMB_EB_SIGNAL_TYPE_CURRENT_NETWORK = 0,
    CMMB_EB_SIGNAL_TYPE_OTHER_NETWORK ,
    CMMB_EB_SIGNAL_TYPE_NUM
} cmmb_eb_signal_type_enum;

/*****************************************************************************
* Structure
*****************************************************************************/
typedef struct cmmb_eb_aux_info_struct_t{
    UINT8     data_type;
    UINT16   data_len;
    UINT8*   data;
} cmmb_eb_aux_info_struct;

typedef struct cmmb_eb_lang_desc_struct_t{
    UINT32 lang;
    UINT16 text_len;
    char *text;
    UINT8 provider_name_len;
    char *provider_name;
    UINT16 service_ref_id;
    UINT8 aux_info_idx;
    cmmb_eb_aux_info_struct *aux_info;
} cmmb_eb_lang_desc_struct;

typedef struct cmmb_eb_msg_current_network_t{
    struct cmmb_eb_msg_current_network_t *next_p;
    int is_new;

    cmmb_eb_signal_type_enum signal_type;

    UINT16 id_msg_id;
    UINT16 id_net_id;
    UINT8  id_net_level;

    UINT8 msg_type;
    UINT8 msg_level;
    UINT8 char_set;
    UINT32 start_date;
    UINT32 start_time;
    UINT32 duration;
    UINT8 lang_desc_count;
    cmmb_eb_lang_desc_struct *lang_desc[CMMB_EB_MAX_LANG_DESC_NUM];
    UINT8 aux_info_count;
    cmmb_eb_aux_info_struct *aux_info[CMMB_EB_MAX_AUX_INFO_NUM];
} cmmb_eb_msg_current_network_struct;

typedef struct cmmb_eb_msg_other_network_t{
    struct cmmb_eb_msg_other_network_t *next_p;
    int is_new;

    cmmb_eb_signal_type_enum signal_type;

    UINT16 id_msg_id;
    UINT16 id_net_id;
    UINT8  id_net_level;

    UINT8 msg_type;
    UINT8 msg_level;
    UINT8 net_level;
    UINT16 net_id;
    UINT8 freq;
    UINT32 center_freq;
    UINT8 bandwidth;
} cmmb_eb_msg_other_network_struct;

typedef struct cmmb_eb_segment_struct_t
{
	UINT16 seg_len;
	UINT8 *seg_data;
} cmmb_eb_segment_struct;

typedef struct cmmb_eb_msg_ctrl_unit_struct_t
{
	UINT8 protocol_ver;
	UINT8 protocol_ver_lowest;
	UINT8 net_level;
	UINT16 net_id;
	UINT16 msg_id;
	UINT8 seg_sn_last;
	UINT32 seg_mask[CMMB_EB_MAX_SEGMENT_MASK_SIZE];
	cmmb_eb_segment_struct seg_unit[CMMB_EB_MAX_SEGMENT];
	UINT32 total_data_len;
	UINT32 total_seg_count;
	UINT8 *msg_data;
} cmmb_eb_msg_ctrl_unit_struct;

typedef struct cmmb_eb_context_struct_t
{
	void* buffer;	
	cmmb_eb_msg_ctrl_unit_struct *msg_ctrl[CMMB_EB_MAX_MSG_NUM];
} cmmb_eb_context_struct;


/*****************************************************************************
* External Global Function
*****************************************************************************/
int cmmb_eb_process(void* context, UINT8* data_part, UINT32 size);
int cmmb_eb_reset(void* context);
int cmmb_eb_close(void* context);
void cmmb_eb_free_msg_buffer(cmmb_eb_msg_for_ap *buf_ap, cmmb_eb_msg_current_network_struct *buf_p);
#endif /* _CMMB_EB_H */
