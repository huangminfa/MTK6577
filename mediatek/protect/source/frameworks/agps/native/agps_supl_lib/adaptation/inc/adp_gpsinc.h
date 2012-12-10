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
 *  adp_gpsinc.h
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
 * 10 13 2011 ck.chiang
 * [ALPS00080084] [Need Patch] [Volunteer Patch] Our phone should response CP NILR even if A-GPS is turned off. (Permanent CP)
 * Pernament CP
 * ; Support CP NILR even if AGPS setting is disabled
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

#define GPS_AGPS_POS_REQ_TIMER_MAX 3
#define GPS_AGPS_MEAS_REQ_TIMER_MAX 3

typedef enum
{
    GPS_SLEEP_MODE_TIMER = 0,
    GPS_UART_RING_BUFFER_EXP_TIMER,
    GPS_UART_POWER_ON_CHECK_VERSION_TIMER,
    GPS_UART_POWER_ON_CHECK_CHIP_ID_TIMER,
    GPS_UART_ENABLE_DEBUG_INFO_TIMER,
    GPS_UART_AUTHENTICATION_TIMER,
    GPS_UART_SWITCH_MODE_TIMER,
    GPS_AGPS_ASSIST_CMD_WRITE_TIMER,
    GPS_AGPS_ASSIST_CMD_WRITE_EXP_TIMER,
    GPS_AGPS_QUERY_TIMER,
    GPS_AGPS_POS_REQ_BASE_TIME,
    GPS_AGPS_POS_REQ_TIME_END = GPS_AGPS_POS_REQ_BASE_TIME + GPS_AGPS_POS_REQ_TIMER_MAX - 1,
    GPS_AGPS_MEAS_REQ_BASE_TIME,
    GPS_AGPS_MEAS_REQ_TIME_END =GPS_AGPS_MEAS_REQ_BASE_TIME + GPS_AGPS_MEAS_REQ_TIMER_MAX - 1,
//C.K. Chiang marked--> For Permanent CP
    //Add by Baochu wang
    //CP_MOLR_BEGIN_WAIT_GPS_READY,
    //CP_MOLR_SEND_LOCATION,
    //CP_NILR_OPEN_GPS,
    //CP_UP_VERIFY_NOTIFY,
//C.K. Chiang marked<--

	MAX_NUM_OF_GPS_TIMER
} gps_timer_enum;

//C.K. Chiang add--> For Permanent CP
//Permanent Timer with agpsd
typedef enum
{
    CP_MOLR_BEGIN_WAIT_GPS_READY,
    CP_MOLR_SEND_LOCATION,
    CP_NILR_OPEN_GPS,
    CP_UP_VERIFY_NOTIFY,
//C.K. Chiang add<--
#ifdef __CDMA_AGPS_SUPPORT__
    CDMA_AGPS_QUERY_TIMER,
#endif
    MAX_NUM_OF_CP_GPS_TIMER
} gps_cp_timer_enum;

extern kal_bool gps_msg_hdlr(ilm_struct *ilm_ptr);
extern void gps_stop_timer(kal_uint8 timer_id);
extern void gps_start_timer(kal_uint8 timer_id, kal_uint32 period, kal_timer_func_ptr timer_expiry, void *arg);
