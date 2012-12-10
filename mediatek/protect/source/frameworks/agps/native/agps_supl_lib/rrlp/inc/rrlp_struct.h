/******************************************************************************
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
 *   rrlp_struct.h
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
 * SH Yang (mtk01600)
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
 * May 3 2009 mtk01600
 * [MAUI_01679988] [A-GPS] RRLP Control-Plane & R5 check-in
 * 
 *
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


#ifndef _RRLP_STRUCT_H
#define _RRLP_STRUCT_H

/******************************************************************************
 * RRLP Struct
 ******************************************************************************/
 
/***************************************************************************/
/********************************** Common *********************************/
/***************************************************************************/
#ifdef __RRLP_REL_5__
typedef struct rrlp_r5_ext_ref_ie_struct
{
        kal_uint8       smlc_code;        /* 0-63 */
        kal_uint32     trans_id;            /* 0-262143 */
}rrlp_r5_ext_ref_ie_struct;
#endif

typedef struct rrlp_session_context_struct 
{
        rrlp_state_enum rrlp_proc_state;        /* RRLP Procedure State. Only one RRLP procedure shall be
                                                                        supported at a time. */        
        kal_uint8           rrlp_ref_no;                /* (R99) reference number in RRLP */                                                                     
        kal_uint16         rec_assist_bitmap;     /* received assist bitmap in one assistance data  */   
        
#ifdef __RRLP_REL_5__       
        kal_bool            is_r5_ext_ref_ie_used;  /* KAL_TRUE if r5 ext ref ie is used; KAL_FALSE otherwise */
        rrlp_r5_ext_ref_ie_struct r5_ext_ref_ie;    /* Valid only if is_r5_ext_ref_ie_used is KAL_TRUE */
#endif        

#if 0
        //So far the trans_id cannot tell C-plane or U-plane. To avoid confusion
        //caused by C-plane/U-plane fast switch, remove these variables to global
        //context.
        /* RRLP <-> GPS Internal Use */
        /* Each session shall have unique id or gps may respond to wrong party */
        kal_uint8          pos_trans_id;                /* transaction id for position req */
        kal_uint8          meas_trans_id;             /* transaction id for meas req */
        kal_uint8          assist_trans_id;            /* transaction id for assist data req */        
#endif                          
}rrlp_session_context_struct;
/***************************************************************************/
/************************** Struct for C-Plane******************************/
/***************************************************************************/
#ifdef __AGPS_CONTROL_PLANE__

/* Timer */
typedef struct
{
    eventid event_id;
    kal_timer_func_ptr callback_func;
    void *arg;
}
rrlp_timer_table_struct;

typedef struct
{
    stack_timer_struct              base_timer;      
    event_scheduler                 *event_scheduler_ptr;
    rrlp_timer_table_struct        rrlp_timer_table[RRLP_MAX_NUM_OF_TIMER]; /* Remember to init callback_func before use */
}rrlp_timer_struct;

/* RATCM FSM */
typedef void (*rrlp_cp_fsm_hdlr) (rrlp_cp_ev_enum, rrlp_session_context_struct const * const, void*);
extern const rrlp_cp_fsm_hdlr rrlp_cp_fsm_table[];

#endif /* __AGPS_CONTROL_PLANE__ */


/***************************************************************************/
/************************** Struct for U-Plane******************************/
/***************************************************************************/
#ifdef __AGPS_USER_PLANE__

#endif

#endif /* _RRLP_STRUCT_H */
