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
 /*******************************************************************************
 *  Copyright Statement:
 *  --------------------
 *  This software is protected by Copyright and the information contained
 *  herein is confidential. The software may not be copied and the information
 *  contained herein may not be used or disclosed except with the written
 *  permission of MediaTek Inc. (C) 2003
 *
 *******************************************************************************/

 /*******************************************************************************
 * Filename:
 * ---------
 *  gps_sap.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  GIS SAP
 *
 * Author:
 * -------
 *  Wang Hai (MBJ06018)
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Aug 6 2009 mtk80018
 * [MAUI_01886224] [AGPS 2G CP] RRLP R5
 * 
 *
 * Apr 18 2008 mtk01206
 * [MAUI_00758860] [GPS][New] Check in MT3326 related code to MAUI
 * 
 *
 * Apr 18 2008 mtk01206
 * [MAUI_00758860] [GPS][New] Check in MT3326 related code to MAUI
 * 
 *
 * Apr 17 2008 mbj06018
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Feb 13 2008 mtk01206
 * [MAUI_00618179] [GPS][Modify] Modify MT3316/MT3318 authentication process and NVRAM access scenario
 * 
 *
 * Dec 31 2007 mtk01813
 * [MAUI_00590473] Update feature options of makefile
 * help Wanghai to check-in these two file at interface\gps
 *
 * Nov 30 2007 mtk01206
 * [MAUI_00574860] [GPS][Modify] Update some GPS task files
 * 
 *
 * Nov 4 2007 mtk01206
 * [MAUI_00571439] [GPS][New] Add new GPS task for QC
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/
MSG_ID_GPS_UART_OPEN_REQ = MSG_ID_GPS_MSG_CODE_BEGIN,
MSG_ID_GPS_UART_READ_REQ,    
MSG_ID_GPS_UART_WRITE_REQ,    
MSG_ID_GPS_UART_CLOSE_REQ,
MSG_ID_GPS_UART_NMEA_LOCATION,   
MSG_ID_GPS_UART_NMEA_SENTENCE,   
MSG_ID_GPS_UART_RAW_DATA,   
MSG_ID_GPS_UART_DEBUG_RAW_DATA,   
MSG_ID_GPS_UART_P_INFO_IND,   
MSG_ID_GPS_UART_OPEN_SWITCH_REQ,
MSG_ID_GPS_UART_CLOSE_SWITCH_REQ,

MSG_ID_GPS_POS_GAD_CNF,
MSG_ID_GPS_LCSP_MSG_CODE_BEGIN = MSG_ID_GPS_POS_GAD_CNF,
MSG_ID_GPS_LCSP_MEAS_GAD_CNF,
MSG_ID_GPS_LCSP_ASSIST_DATA_CNF,
MSG_ID_GPS_LCSP_MSG_CODE_END = MSG_ID_GPS_LCSP_ASSIST_DATA_CNF,
MSG_ID_GPS_POS_GAD_REQ,
MSG_ID_GPS_LCSP_MEAS_GAD_REQ,
MSG_ID_GPS_LCSP_ASSIST_DATA_REQ,
MSG_ID_GPS_LCSP_ABORT_REQ,

MSG_ID_GPS_ASSIST_BIT_MASK_IND,
MSG_ID_GPS_LCT_POS_REQ,
MSG_ID_GPS_LCT_POS_RSP,
MSG_ID_GPS_LCT_OP_ERROR,

/* RTC -> GPS */
MSG_ID_RTC_GPS_TIME_CHANGE_IND,   
/* GPS EINT HISR -> GPS */
MSG_ID_GPS_HOST_WAKE_UP_IND,


