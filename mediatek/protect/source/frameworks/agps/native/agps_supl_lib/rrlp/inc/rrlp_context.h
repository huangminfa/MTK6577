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
 *   rrlp_context.h
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
 * Nov 7 2009 mtk01600
 * [MAUI_01981828] [Compile Warning Removal][AGPS] rrlp warning
 * 
 *
 * Oct 13 2009 mtk01600
 * [MAUI_01949555] [AGPS][CP] 70.9.4.3, assistance data missing error
 * 
 *
 * Aug 10 2009 mtk01600
 * [MAUI_01936101] [AGPS 2G CP] AGPS Feature check-in
 * 
 *
 * Dec 26 2008 mtk01600
 * [MAUI_01305588] New Cell-ID arch check-in
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


#ifndef _RRLP_CONTEXT_H
#define _RRLP_CONTEXT_H

#include "rrlp_enum.h"
#include "rrlp_struct.h"

/******************************************************************************
 * Definition
 ******************************************************************************/

#define IN 
#define OUT 
#define INOUT
#define RRLP_UNUSED_ARG(_arg) if (_arg) {}  

#define RRLP_INC_TRAN_ID(sctx_ptr, x)  (x == 255) ? 0 : x++
#if 0 /* For supporting multiple sessions */
#define RRLP_INC_TRAN_ID(sctx_ptr, x) rrlp_update_gps_tx_id(sctx_ptr, x)
#define CYCLIC_INCREMENT8(counter, addition, limit)   ((counter) = (kal_uint8)(((kal_uint8)(counter) + (addition)) % (limit)))
#endif

//#ifndef RRLP_DEBUG_ALL
//#define RRLP_DEBUG_ALL
//#endif

#define CYCLIC_DECREMENT8(counter, addition, limit) \
    {\
        if ((kal_uint8)(counter) >= (kal_uint8)(addition))\
        {\
            (counter) = (kal_uint8)((kal_uint8)(counter) - (kal_uint8)(addition));\
        }\
        else\
        {\
            (counter) = (kal_uint8)((kal_uint8)(limit) - (kal_uint8)((kal_uint8)(addition) - (kal_uint8)(counter)));\
        }\
    }

#define RRLP_INC_SAT_ID(x)  x++

#define MAX_NO_OF_TDMA_FRAMES       2715648
#define RRLP_FN_OFFSET(x,y)  ((x < y) ? (y - x): \
                       ((y + MAX_NO_OF_TDMA_FRAMES - x) % MAX_NO_OF_TDMA_FRAMES))

   
#define RRLP_RESET_SESSION_CTX(x_ptr) do{\
if(x_ptr->rrlp_proc_state == RRLP_MEAS || x_ptr->rrlp_proc_state == RRLP_POS)\
{\
rrlp_send_gps_abort_req();\
}\
kal_mem_set(x_ptr, 0, sizeof(rrlp_session_context_struct));\
}while(0);

#ifdef __RRLP_REL_5__
#define RRLP_MAX_PDU_SIZE 242   /* RRLP R5 */
#endif

#ifdef __AGPS_CONTROL_PLANE__
/* RRLP Timer Definition */
#define RRLP_BASE_TIMER_ID 0
#define RRLP_PROLONG_MODE_TIMER_TIMEOUT_VALUE 6000
#define RRLP_SLEEP_MODE_TIMER_TIMEOUT_VALUE 4000
#define RRLP_CP_FSM(state, ev, sctx_p,arg)  (* (rrlp_cp_fsm_table[state])) (ev, sctx_p, arg)
#endif

/******************************************************************************
 * Type Definitions
 ******************************************************************************/

typedef struct rrlp_context_struct
{
    //Session context. Currently, only one session is supported. 
    rrlp_session_context_struct rrlp_session_ctx;

    /* Session attribute */
    rrlp_bearer_type_enum bearer_type; /* RRLP_USER_PLANE or RRLP_CTRL_PLANE */
    
    //So far the trans_id cannot tell C-plane or U-plane. To avoid confusion
    //caused by C-plane/U-plane fast switch, remove these variables from session
    //context to rrlp_context_struct
    kal_uint8          pos_trans_id;                /* transaction id for position req */
    kal_uint8          meas_trans_id;             /* transaction id for meas req */
    kal_uint8          assist_trans_id;            /* transaction id for assist data req */
   
    KAL_ADM_ID         mem_pool_id;

    /* 2009-09-10 for assistance missing handling */
    kal_bool		    b_conventional_GPS;		/* TRUE if using conventional GPS mode */
    kal_bool		    b_recv_1st_gps_rsp;		/* set to TRUE if RRLP has received MEAS/POS rsp in a session */
	 
#ifdef __AGPS_CONTROL_PLANE__
    /* A-GPS C-Plane variables related to Data Path (GAS) */
    kal_uint8           gas_data_mui;
    kal_uint8           num_data_req_sent; 
    kal_uint8           first_mui_waited; 

    /* A-GPS C-Plane variables related to GPS chip */
    rrlp_gps_chip_status_enum                        gps_chip_status;
    
    /* A-GPS C-Plane variables related to RRLP_RATCM STATE */
    rrlp_cp_state_enum      rrlp_cp_state;
    kal_int32                      gas_cnf_waited;  
    rrlp_timer_struct           rrlp_timer;    
#endif

    
        
#ifdef  RRLP_DEBUG_ALL
    kal_bool        dbg_send_large_rsp;
    kal_int32       dbg_rrlp_apdu_len;
#endif
} rrlp_context_struct;

typedef rrlp_context_struct *RRLP_CONTEXT_PTR;

extern RRLP_CONTEXT_PTR rrlp_ptr_g;


#endif /* _MAC_CONTEXT_H */
