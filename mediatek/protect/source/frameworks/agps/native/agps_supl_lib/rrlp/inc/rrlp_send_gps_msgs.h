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

 /******************************************************************************
 * Filename:
 * ---------
 *   rrlp_send_gps_msgs.h
 *
 * Project:
 * -------- 
 *   MAUI
 *
 * Description:
 * ------------
 *
 * Author:
 * -------
 * Lanslo Yang (mtk01162)
 *
 *-----------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Aug 10 2009 mtk01600
 * [MAUI_01936101] [AGPS 2G CP] AGPS Feature check-in
 * 
 *
 * Apr 21 2008 mtk01162
 * [MAUI_00759867] [AGPRS] check-in AGPS RRLP part
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 *
 ******************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by MediaTek Inc.
*
********************************************************************************/


#ifndef _RRLP_SEND_GPS_MSGS_H
#define _RRLP_SEND_GPS_MSGS_H


/*****************************************************************************
 * exported function prototypes
 *****************************************************************************/
 
extern void rrlp_send_gps_abort_req(void);

extern void rrlp_send_gps_pos_req(
    IN rrlp_session_context_struct *rrlp_sctx_ptr,
    IN kal_bool req_time_assist_meas, 
    IN kal_uint8 accuracy,  
    IN kal_bool env_valid, 
    IN kal_uint8 env, 
    IN kal_uint8 delay);

extern void rrlp_send_gps_meas_req(
        IN rrlp_session_context_struct *rrlp_sctx_ptr,
        IN kal_bool req_time_assist_meas,  
        IN kal_bool accuracy_valid, 
        IN kal_uint8 accuracy,
        IN kal_bool env_valid, 
        IN kal_uint8 env, 
        IN kal_uint8 delay);

extern kal_bool rrlp_send_gps_assist_data_req(
    IN rrlp_session_context_struct *rrlp_sctx_ptr,
    IN kal_bool req_time_assist_meas, 
    IN gps_assist_data_type_enum type, 
    IN void *assist_data,
    IN kal_bool fgLastAssist); //!!Chiwei: fgLastAssist is added in porting
 
#ifdef __AGPS_CONTROL_PLANE__
kal_int32 rrlp_send_gps_uart_open(void);
kal_int32 rrlp_send_gps_uart_close(void);
#endif

#endif /* _RRLP_SEND_GPS_MSGS_H */
