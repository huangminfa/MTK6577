/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2006
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

/*****************************************************************************
 *
 * Filename:
 * ---------
 *  mtk_service.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   The AGPS SWIP adaption layer.
 *
 * Author:
 * -------
 *  Leo Hu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

/* MTK services */

#ifndef __MTK_SERVICE_H__
#define __MTK_SERVICE_H__

typedef enum {
    MTK_TD_UL = 0x01 << 0,   /* Uplink Direction */
    MTK_TD_DL = 0x01 << 1,   /* Downlink Direction */
    MTK_TD_CTRL = 0x01 << 2, /* Control Plane. Both directions */
    MTK_TD_RESET = 0x01 << 3 /* Reset buffer content to 0 */
} mtk_transfer_direction;

//#if (0)
#define MTK_LOCAL_PARA_HDR \
   kal_uint8  ref_count; \
   kal_uint16 msg_len;

#define MTK_PEER_BUFF_HDR \
   kal_uint16 pdu_len; \
   kal_uint8  ref_count; \
   kal_uint8  pb_resvered; \
   kal_uint16 free_header_space; \
   kal_uint16 free_tail_space;

//#define MTK_LOCAL_PARA_HDR mtk_local_para_struct
typedef struct mtk_local_para_struct {
    MTK_LOCAL_PARA_HDR
} mtk_local_para_struct;

typedef struct
{
    MTK_PEER_BUFF_HDR
} mtk_peer_buff_struct;


typedef struct
{
    module_type       src_mod_id;
    module_type       dest_mod_id;
    sap_type          sap_id;
    msg_type          msg_id;
    mtk_local_para_struct *local_para_ptr;
    mtk_peer_buff_struct  *peer_buff_ptr;
} mtk_ilm_struct;

#define MTK_SEND_ILM( src_mod, dest_mod, sap, ilm_ptr)\
{ \
   ilm_ptr->src_mod_id  = src_mod;  \
   ilm_ptr->dest_mod_id = dest_mod; \
   mtk_msg_send_ext_queue(ilm_ptr); \
}

//$$#ifndef __USE_KAL_SERVICE__
#define TD_UL MTK_TD_UL
#define TD_DL MTK_TD_DL
#define TD_CTRL MTK_TD_CTRL
#define TD_RESET MTK_TD_RESET
#define construct_local_para mtk_construct_local_para
#define construct_peer_buff mtk_construct_peer_buff
#define get_pdu_ptr mtk_get_pdu_ptr
#define hold_peer_buff mtk_hold_peer_buff
#define update_peer_buff_hdr mtk_update_peer_buff_hdr
#define free_peer_buff mtk_free_peer_buff
#define allocate_ilm mtk_allocate_ilm
#define free_ilm mtk_free_ilm
#define free_ctrl_buffer mtk_free_ctrl_buffer
#define free_local_para mtk_free_local_para
#define LOCAL_PARA_HDR MTK_LOCAL_PARA_HDR
#define PEER_BUFF_HDR MTK_PEER_BUFF_HDR
#define local_para_struct mtk_local_para_struct
#define msg_send_ext_queue mtk_msg_send_ext_queue
#define peer_buff_struct mtk_peer_buff_struct
#define ilm_struct mtk_ilm_struct
#define get_int_ctrl_buffer _mtk_get_int_ctrl_buffer
#define SEND_ILM MTK_SEND_ILM
//$$#endif
#define mtk_allocate_ilm(mod_id) _mtk_allocate_ilm(mod_id, __FILE__, __LINE__)
#define mtk_free_ilm(ilm_ptr) _mtk_free_ilm(ilm_ptr, __FILE__, __LINE__)

#define mtk_construct_local_para(size, param) _mtk_construct_local_para(size, param, __FILE__, __LINE__)
#define mtk_free_local_para(ptr) _mtk_free_local_para(ptr, __FILE__, __LINE__)

#define mtk_construct_peer_buff(pdu_len, header_len, tail_len, param) _mtk_construct_peer_buff(pdu_len, header_len, tail_len, param, __FILE__, __LINE__)
#define mtk_free_peer_buff(ptr) _mtk_free_peer_buff(ptr, __FILE__, __LINE__)

#define construct_peer_buff_ext(pdu, pdu_len) _construct_peer_buff_ext(pdu, pdu_len, __FILE__, __LINE__)
#define get_ctrl_buffer(size) _mtk_get_int_ctrl_buffer(size, __FILE__, __LINE__)

#define mtk_get_ctrl_buffer(size) _mtk_get_int_ctrl_buffer(size, __FILE__, __LINE__)
#define mtk_free_ctrl_buffer(ptr) _mtk_free_ctrl_buffer(ptr, __FILE__, __LINE__)

extern ilm_struct *_mtk_allocate_ilm(module_type mod_id, kal_char *file, kal_uint32 line);
extern void _mtk_free_ilm(ilm_struct *ilm_ptr, kal_char *file, kal_uint32 line);
extern void *_mtk_construct_local_para(kal_uint32 size, kal_uint8 param, kal_char *file, kal_uint32 line);
extern void _mtk_free_local_para(void *ptr, kal_char *file, kal_uint32 line);
extern void *_mtk_construct_peer_buff(kal_uint16 pdu_len, kal_uint16 header_len, kal_uint16 tail_len, kal_uint8 param, kal_char *file, kal_uint32 line);
extern void _mtk_free_peer_buff(void *ptr, kal_char *file, kal_uint32 line);
extern void *_construct_peer_buff_ext(void *pdu, kal_uint16 pdu_len, kal_char *file, kal_uint32 line);
extern void *_mtk_get_int_ctrl_buffer(kal_uint32 buff_size, kal_char *file_name, kal_uint32 line);
extern void _mtk_free_ctrl_buffer(void *ptr, kal_char *file, kal_uint32 line);

extern void *mtk_get_pdu_ptr(mtk_peer_buff_struct *peer_buff_ptr, kal_uint16 *length_ptr);
extern kal_bool mtk_hold_peer_buff(mtk_peer_buff_struct *peer_buff_ptr);
extern void mtk_update_peer_buff_hdr(mtk_peer_buff_struct *peer_buff_ptr, kal_uint16 new_hdr_len,
                         kal_uint16 new_pdu_len, kal_uint16 new_tail_len);

extern void supl_retrieve_int_msg(mtk_ilm_struct **ilm_ptr);

//== added during porting
extern char *get_thd_name();
extern char *agps_task_name[];
extern char *agps_mod_name[];

#define GET_MOD_NAME(m)  (agps_mod_name[m])
#define GET_THD_NAME()   (get_thd_name())
#define GET_TASK_NAME(t) (agps_task_name[t])

#endif
