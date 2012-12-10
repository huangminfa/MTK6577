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

/*******************************************************************************
 * Filename:
 * ---------
 *   supl_exp.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This is trace map definition of SUPL task for SWIP.
 *
 * Author:
 * -------
 *   Jinghan Wang
 *
 *==============================================================================
 * 				HISTORY
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
 *==============================================================================
 *******************************************************************************/

#ifndef _SUPL_TRC_H
#define _SUPL_TRC_H

#ifdef __AGPS_SWIP_REL__

#define SUPL_TRACE_GROUP    TRACE_GROUP_1

/* FUNC */
#define SUPL_FUNC_LOG(TAG)

/* STATE */
#define SUPL_STATE_CHANGE_LOG(sid, old_state, new_state) 
#define SUPL_CONN_STATE_CHANGE_LOG(sid, old_state, new_state) 

/* INFO */
#define SUPL_FSM_EVENT_LOG(sid, state, event) 

#define SUPL_CONN_FSM_EVENT_LOG(sid, state, event) 
/* GPS_GET_POS, GPS_NO_POS */
#define SUPL_INFO_LOG(TAG)                      
#define SUPL_INFO_LOG1(TAG, ARG1)               
#define SUPL_INFO_LOG2(TAG, ARG1, ARG2)         
#define SUPL_INFO_LOG3(TAG, ARG1, ARG2, ARG3)   


/* WARNNING */
#define SUPL_WARNING_LOG(TAG)                   
#define SUPL_WARNING_LOG1(TAG, ARG1)            
#define SUPL_WARNING_LOG2(TAG, ARG1,ARG2)       
#define SUPL_WARNING_LOG3(TAG, ARG1,ARG2,ARG3)  


/* ERROR */
#define SUPL_ULP_PROTO_ERR_LOG(type)            
#define SUPL_ERROR_LOG(TAG)                     
#define SUPL_ERROR_LOG1(TAG,ARG1)               
#define SUPL_ERROR_LOG2(TAG,ARG1,ARG2)          
#define SUPL_ERROR_LOG3(TAG,ARG1,ARG2,ARG3)     

/* TRACE PEER */
#define SUPL_TRACE_PEER_LOG(TAG)                

/* SUPL TRACE GROUP */

/* FSM transition */
#define SUPL_FSM_RECV_ULP_MSG_LOG(type)         
#define SUPL_FSM_SEND_ULP_MSG_LOG(type)         
/* Timer */
#define SUPL_TIMER_EXPIRY_LOG(tid)              
#define SUPL_TIMER_START_LOG(tid, tval)         
#define SUPL_TIMER_STOP_LOG(tid)                


/* ULP Messages */
#define SUPL_ULP_DECODE_MSG_LOG(type)           
#define SUPL_ULP_ENCODE_MSG_LOG(type)           

#define SUPL_ULP_STATUS_LOG(code)               
#define SUPL_ULP_POS_METHOD_LOG(type)           
#define SUPL_ULP_QOP_LOG(HA, VA, AGE, DELAY)    
#define SUPL_ULP_LID_LOG(rat, mcc, mnc, lac, ci, c)  
#define SUPL_ULP_HDR_VERSION_LOG(v1, v2, v3)    
#define SUPL_ULP_HDR_SET_SESSION_ID_LOG(v1, v2) 
#define SUPL_ULP_NOTIFY_TYPE_LOG(type)          
#define SUPL_ULP_IPV4_LOG(ip1, ip2, ip3, ip4)   
#define SUPL_ULP_SLP_MODE_LOG(type)             
#define SUPL_ULP_HDR_SLP_SESSION_ID_LOG(v1, v2, v3, v4, v5)

#define SUPL_ULP_VER_LOG(v1, v2, v3, v4, v5, v6, v7, v8)

#define SUPL_ULP_FILTER_LOG(v1, v2, v3, v4, v5, v6, v7, v8, v9)
#define SUPL_ULP_POSITION_LOG(time, week, year, month, day, hour, min, sec, la, lo, al)

#define SUPL_RECV_IMSI_LOG( v1, v2, v3, v4, v5, v6, v7, v8, v9)
#define SUPL_IMSI_SET_ID_LOG( v1, v2, v3, v4, v5, v6, v7, v8)

typedef enum {
    SUPL_L4_MSG_HDLR_FUNC_TRC,
    SUPL_L4_NBR_CELL_INFO_REG_CNF,
    SUPL_L4_NBR_CELL_INFO_IND,
} SUPL_SWIP_TRC;

#endif /* __AGPS_SWIP_REL__ */

#endif /* _SUPL_TRC_H */
