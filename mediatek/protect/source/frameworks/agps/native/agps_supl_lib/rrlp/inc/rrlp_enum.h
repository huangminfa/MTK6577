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
 ******************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by MediaTek Inc.
*
********************************************************************************/


#ifndef _RRLP_ENUM_H
#define _RRLP_ENUM_H

/******************************************************************************
 * RRLP Enums
 ******************************************************************************/
 
/******************************** Common ********************************/
typedef enum
{
   RRLP_IDLE = 0,
   RRLP_ASSIST,
   RRLP_POS,
   RRLP_MEAS,
   RRLP_NUM_STATES
} rrlp_state_enum;


typedef enum
{
    RRLP_INACTIVE = 0,
    RRLP_USER_PLANE,
    RRLP_CTRL_PLANE
}rrlp_bearer_type_enum;

typedef enum
{
    RRLP_MSG_UNKNOWN,
    RRLP_MSG_MEAS_POS_REQ,
    RRLP_MSG_MEAS_POS_RSP,    
    RRLP_MSG_ASSIST_DATA,
    RRLP_MSG_ASSIST_DATA_ACK,    
    RRLP_MSG_PROTOCOL_ERR,    
    RRLP_MSG_END
} rrlp_msg_type_enum;

typedef enum
{
    RRLP_MSG_ERR_CAUSE_NONE,
    RRLP_MSG_ERR_CAUSE_MESSAGE_TOO_SHORT,
    RRLP_MSG_ERR_CAUSE_MISS_INFO_OR_COMP,
    RRLP_MSG_ERR_CAUSE_INCORRECT_DATA,
    RRLP_MSG_ERR_CAUSE_UNFORESEEN_COMP,
    RRLP_MSG_ERR_CAUSE_SESSION_ABORT,
    RRLP_MSG_ERR_CAUSE_END
} rrlp_msg_err_cause_enum;
/************************** Enum for RRLP vs. GAS **************************/
#ifdef __AGPS_CONTROL_PLANE__

typedef enum{
    /* RATCM_INACT */
    CP_INACT = 0,

    /* RATCM_ACT */
    CP_ACT_RR_RX,
    CP_ACT_RR_TX,
    CP_ACT_RR_RXTX,
    CP_ACT_RR_INACT,    
    CP_PAUSE    
}rrlp_cp_state_enum;

typedef enum{
    
    /* Events triggered by RR Msg */
    RRLP_RECV_RR_FIRST_SEGMENT_EV = 1,
    RRLP_RR_LINK_REL_EV,
    RRLP_RECV_RR_MANAGEMENT_CMD_EV,
    RRLP_RECV_RR_DATA_IND_EV,
    RRLP_RECV_RR_DATA_CNF_EV,
    RRLP_RECV_RR_SEGMENT_DISCARD_EV,
    
    /* Events triggered by RRLP */
    RRLP_SEND_DATA_REQ_EV,    
    RRLP_PROC_STATE_TO_IDLE_EV
        
}rrlp_cp_ev_enum;

typedef enum{
    RRLP_GPS_CHIP_POWER_DOWN,       /* GPS power down */
    RRLP_GPS_CHIP_POWER_ON,             /* GPS power on */
    RRLP_GPS_CHIP_PROLONG_MODE,     /* GPS power on */
    RRLP_GPS_CHIP_SLEEP_MODE            /* GPS power on */
}rrlp_gps_chip_status_enum;

typedef enum{
    RRLP_GPS_PROLONG_MODE_TIMER_ID = 0,
    RRLP_GPS_SLEEP_MODE_TIMER_ID,
    RRLP_MAX_NUM_OF_TIMER
}rrlp_timer_id_enum;

typedef enum{
    RESET_CP_LINK_RELEASE,
    RESET_CP_PROC_END
}rrlp_reset_cause_enum;
#endif /* __AGPS_CONTROL_PLANE__ */


/************************** Enum for RRLP vs. SUPL **************************/


/***************** Enum for (RRLP vs. GAS) and (RRLP vs. SUPL) *****************/
typedef enum
{
    RRLP_PAYLOAD_MEASURE_RSP,
    RRLP_PAYLOAD_POSITION_RSP,
    RRLP_PAYLOAD_LOCATION_ERR,
    RRLP_PAYLOAD_ASSIST_ACK,
    RRLP_PAYLOAD_FINAL_ASSIST_ACK,
    RRLP_PAYLOAD_PROTOCOL_ERROR,
    RRLP_PAYLOAD_END
} rrlp_payload_enum;


#endif /* _RRLP_ENUM_H */
