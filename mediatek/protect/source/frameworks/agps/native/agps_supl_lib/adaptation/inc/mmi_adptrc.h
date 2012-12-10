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
 *  mmi_adptrc.h
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
 
 #ifdef __AGPS_SWIP_REL__
#define MMI_TRACE       kal_prompt_trace 
#define TRACE_GROUP_1    MOD_MMI
#define TRACE_GROUP_2    MOD_MMI
#define TRACE_GROUP_3    MOD_MMI
#define TRACE_GROUP_4    MOD_MMI
#define TRACE_GROUP_5    MOD_MMI
#define TRACE_GROUP_6    MOD_MMI


/*mdi_gpsuart */
        #define MDI_GPS_UART_TRC_CONFIGURE "[MDI_GPS_UART] mdi_gps_uart_configure port=%d baud=%d bits=%d stops=%d parity=%d"
        #define MDI_GPS_UART_TRC_CONFIGURE_2 "[MDI_GPS_UART] mdi_gps_uart_configure flowControl=%d xonChar=%d xoffChar=%d DSRCheck=%d"
        #define MDI_GPS_UART_TRC_OPEN "[MDI_GPS_UART] mdi_gps_uart_open port=%d callback=%d"
        #define MDI_GPS_UART_TRC_READ "[MDI_GPS_UART] mdi_gps_uart_read handle=%d buffer=%d length=%d &read=%d"
        #define MDI_GPS_UART_TRC_WRITE "[MDI_GPS_UART] mdi_gps_uart_write handle=%d buffer=%d length=%d &write=%d"
        #define MDI_GPS_UART_TRC_ARRIVE "[MDI_GPS_UART] mdi_gps_uart_is_data_arrive handle=%d"
        #define MDI_GPS_UART_TRC_CLOSE "[MDI_GPS_UART] mdi_gps_uart_close port=%d mode=%d callback=%d"
        #define MDI_GPS_UART_TRC_CLOSE_CHECK "[MDI_GPS_UART] mdi_gps_uart_close check index=%d port=%d"
        #define MDI_GPS_UART_TRC_SET_CLOSE_MODE "[MDI_GPS_UART] mdi_gps_uart_close set close mode=%d"
        #define MDI_GPS_UART_TRC_FINAL_CLOSE_MODE "[MDI_GPS_UART] mdi_gps_uart_close final close mode=%d"
        #define MDI_GPS_UART_TRC_INDICATE "[MDI_GPS_UART] mdi_gps_uart_indicate_hldr port=%d param=%d"
        #define MDI_GPS_UART_TRC_EXPECTED "[MDI_GPS_UART] g_gps_uart[%d].port = %d expect = %d"
        #define MDI_GPS_UART_TRC_CALLBACK "[MDI_GPS_UART] callback = %d"
        #define MDI_GPS_UART_TRC_ENABLE_PARSER "[MDI_GPS_UART] mdi_gps_enable_parser"
        #define MDI_GPS_UART_TRC_DISABLE_PARSER "[MDI_GPS_UART] mdi_gps_disable_parser"
        #define MDI_GPS_UART_TRC_SET_PARSER_MODE "[MDI_GPS_UART] mdi_gps_set_parser_mode mode=%d"
        #define MDI_GPS_UART_TRC_PARSER_READ_INFO "[MDI_GPS_UART] mdi_gps_parser_read_info nmea=%d data_ptr=%d"
        #define MDI_GPS_UART_TRC_MSG_DISPATCHER "[MDI_GPS_UART] mdi_gpsuart_message_dispatcher ilm_p->msg_id=%d"
        #define MDI_GPS_UART_TRC_DISPATCHER_READY_READ "[MDI_GPS_UART] dispatcher MSG_ID_UART_READY_TO_READ_IND"
        #define MDI_GPS_UART_TRC_DISPATCHER_READY_WRITE "[MDI_GPS_UART] dispatcher MSG_ID_UART_READY_TO_WRITE_IND"
        #define MDI_GPS_UART_TRC_DISPATCHER_PLUGOUT "[MDI_GPS_UART] dispatcher MSG_ID_UART_PLUGOUT_IND"
        #define MDI_GPS_UART_TRC_HDLR_READY_READ "[MDI_GPS_UART] mdi_gpsuart_spp_uart_ready_to_read_ind_hdler port=%d"
        #define MDI_GPS_UART_TRC_HDLR_PLUGOUT "[MDI_GPS_UART] mdi_gpsuart_spp_uart_plugout_ind_hdler port=%d"
        #define MDI_GPS_UART_TRC_HDLR_READY_WRITE "[MDI_GPS_UART] mdi_gpsuart_spp_uart_ready_to_write_ind_hdler port=%d"
        #define MDI_GPS_UART_TRC_OWNER_ERROR "[MDI_GPS_UART] PORT Owner Error"
        #define MDI_GPS_UART_TRC_READ_SIZE "[MDI_GPS_UART] mdi_gps_uart_read ret=%d"
        #define MDI_GPS_UART_TRC_OPEN_ALREADY_OPEN "[MDI_GPS_UART] MDI_RES_GPS_UART_ERR_PORT_ALREADY_OPEN"
        #define MDI_GPS_UART_TRC_FIND_SLOT "[MDI_GPS_UART] Find Slot = %d"
        #define MDI_GPS_UART_TRC_ON_BROAD_GPS "[MDI_GPS_UART] THIS port is on broad GPS"
        #define MDI_GPS_UART_TRC_DUMP_DATA "[MDI_GPS_UART] [DUMP] ret =%d"
        #define MDI_GPS_UART_TRC_DUMP_OK "[MDI_GPS_UART]  [DUMP] OK ret =%d"
        #define MDI_GPS_UART_TRC_OPEN_OK "[MDI_GPS_UART] OPEN OK"
        #define MDI_GPS_UART_TRC_OPEN_ERROR "[MDI_GPS_UART] OPEN ERROR"
        #define MDI_GPS_UART_TRC_OPEN_RETURN_OK "[MDI_GPS_UART] mdi_gps_uart_open return ok"
        #define MDI_GPS_UART_TRC_OPEN_RETURN_ERR "[MDI_GPS_UART] mdi_gps_uart_open return error"
        #define MDI_GPS_UART_TRC_RAW_CB "[MDI_GPS_UART]call in MDI raw data callback func=%d"
        #define MDI_GPS_UART_TRC_APP_IND_HDLR "[MDI_GPS_UART]MDI APP_IND_HDLR type=%d port=%d buffer%d length%d MOD=%d"
        #define MDI_GPS_UART_TRC_APP_IND_HDLR_END "[MDI_GPS_UART]MDI APP_IND_HDLR EXIT"
        #define MDI_GPS_UART_TRC_APP_IND_PORT_FOUND "[MDI_GPS_UART]Right Port Found:port=%d"
        #define MDI_GPS_UART_TRC_FILE_SPACE_PRE_ALLOCATE_OLD "[MDI_GPS_UART]logging file pre-allocate handle=%d space old=%d"
        #define MDI_GPS_UART_TRC_FILE_SPACE_PRE_ALLOCATE_NEW "[MDI_GPS_UART]logging file pre-allocate handle=%d space new=%d"
        #define MDI_GPS_UART_TRC_FILE_SPACE_PRE_ALLOCATE_FAILED "[MDI_GPS_UART]logging file pre-allocate failed code=%d"

   /*mmi_gps_parser*/
        #define MDI_GPS_PARSER_TRC_SUB_FIELD "[MDI_GPS_PARSER] sub_field error j=%d num_cmms=%d"
        #define MDI_GPS_PARSER_TRC_SUB_FIELD_STAR "[MDI_GPS_PARSER] sub_field error star not found"
        #define MDI_GPS_PARSER_TRC_GGA_FAILED "[MDI_GPS_PARSER] $GPGGA field failed "
        #define MDI_GPS_PARSER_TRC_GLL_FAILED "[MDI_GPS_PARSER] $GPGLL field failed "
        #define MDI_GPS_PARSER_TRC_GSA_FAILED "[MDI_GPS_PARSER] $GPGSA field failed "
        #define MDI_GPS_PARSER_TRC_GSV_FAILED "[MDI_GPS_PARSER] $GPGSV field failed "
        #define MDI_GPS_PARSER_TRC_RMC_FAILED "[MDI_GPS_PARSER] $GPRMC field failed "
        #define MDI_GPS_PARSER_TRC_VTG_FAILED "[MDI_GPS_PARSER] $GPVTG field failed "
    
        #define MDI_GPS_PARSER_TRC_PRO_SENTENCE "[MDI_GPS_PARSER] mdi_gps_nmea_process_sentence"
        #define MDI_GPS_PARSER_TRC_PUSH_DATA "[MDI_GPS_PARSER] push data buffer=%d length=%d"
        #define MDI_GPS_PARSER_TRC_PARSER_STR "[MDI_GPS_PARSER][state] start found"
        #define MDI_GPS_PARSER_TRC_PARSER_CHK_SUM "[MDI_GPS_PARSER][state] check sum flag found"
        #define MDI_GPS_PARSER_TRC_PARSER_NEED_END_CR "[MDI_GPS_PARSER][state] need CR"
        #define MDI_GPS_PARSER_TRC_PARSER_END_CR_OK "[MDI_GPS_PARSER][state] END CR OK"
        #define MDI_GPS_PARSER_TRC_PARSER_CHK_SUM_OK "[MDI_GPS_PARSER][state] Check sum OK"
        #define MDI_GPS_PARSER_TRC_CB_GGA "[MDI_GPS_PARSER]sentenc pro GPGGA enable=%d callback=%d"
        #define MDI_GPS_PARSER_TRC_CB_GLL "[MDI_GPS_PARSER]sentenc pro GPGLL enable=%d callback=%d"
        #define MDI_GPS_PARSER_TRC_CB_GSA "[MDI_GPS_PARSER]sentenc pro GPGSA enable=%d callback=%d"
        #define MDI_GPS_PARSER_TRC_CB_GSV "[MDI_GPS_PARSER]sentenc pro GPGSV enable=%d callback=%d is all rec=%d"
        #define MDI_GPS_PARSER_TRC_CB_RMC "[MDI_GPS_PARSER]sentenc pro GPRMC enable=%d callback=%d"
        #define MDI_GPS_PARSER_TRC_CB_VTG "[MDI_GPS_PARSER]sentenc pro GPVTG enable=%d callback=%d"

   /*mmi_gps_log*/
        #define MDI_GPS_LOG_TRC_SWITCH "[MDI_GPS_LOG]Logging Catcher=%d file=%d Agent=%d"
        #define MDI_GPS_LOG_TRC_SENTENCE_TYPE "[MDI_GPS_LOG]Sentence Catcher=%d Agent=%d"
        #define MDI_GPS_LOG_TRC_SEND_AGENT_STC "[MDI_GPS_LOG]Send NMEA sentence to agent"
        #define MDI_GPS_LOG_TRC_STC_CALLBACK "[MDI_GPS_LOG]Send NMEA sentence to callback=%d"
        #define MDI_GPS_LOG_TRC_SET_GGA_CALLBACK "[MDI_GPS_LOG]Set GGA callback=%d"
        #define MDI_GPS_LOG_TRC_SET_RMC_CALLBACK "[MDI_GPS_LOG]Set GGA callback=%d"
        #define MDI_GPS_LOG_TRC_SET_GSA_CALLBACK "[MDI_GPS_LOG]Set GGA callback=%d"
        #define MDI_GPS_LOG_TRC_SET_GSV_CALLBACK "[MDI_GPS_LOG]Set GGA callback=%d"
        #define MDI_GPS_LOG_TRC_SET_STC_CALLBACK "[MDI_GPS_LOG]Set GGA callback=%d"
        #define MDI_GPS_LOG_TRC_MAKE_PATH_CARD_DRIVER "[MDI_GPS_LOG]make path get card driver=%d"
        #define MDI_GPS_LOG_TRC_MAKE_PATH_USER_DRIVER "[MDI_GPS_LOG]make path get user driver=%d"
        #define MDI_GPS_LOG_TRC_SET_CATCHER_LOGGING "[MDI_GPS_LOG]mdi_gps_nmea_set_catcher_logging=%d"
        #define MDI_GPS_LOG_TRC_SET_CATCHER_LOGGING_RES "[MDI_GPS_LOG]g_mdi_gps_cntx.logging.catcher_logging=%d"
        #define MDI_GPS_LOG_TRC_SET_FILE_LOGGING "[MDI_GPS_LOG]mdi_gps_nmea_set_file_logging=%d"
        #define MDI_GPS_LOG_TRC_SET_FILE_LOGGING_RES "[MDI_GPS_LOG]g_mdi_gps_cntx.logging.file_logging=%d"
        #define MDI_GPS_LOG_TRC_SET_FILE_LOGGING_ADD "[MDI_GPS_LOG]file_logging FS write byte=%d written=%d"
        #define MDI_GPS_LOG_TRC_SET_AGENT_LOGGING "[MDI_GPS_LOG]mdi_gps_nmea_set_agent_logging=%d"
        #define MDI_GPS_LOG_TRC_SET_AGENT_LOGGING_RES "[MDI_GPS_LOG]g_mdi_gps_cntx.logging.agent_logging=%d"
        #define MDI_GPS_LOG_TRC_SET_DEBUG_INFO "[MDI_GPS_LOG]mdi_gps_nmea_set_debug_info=%d"
        #define MDI_GPS_LOG_TRC_SET_DEBUG_INFO_RES "[MDI_GPS_LOG]g_mdi_gps_cntx.logging.debug_info=%d"
        #define MDI_GPS_LOG_TRC_GET_SETTING "[MDI_GPS_LOG]load_logging_setting catcher =%d file=%d agent=%d debug=%d"
        #define MDI_GPS_LOG_TRC_GET_CATCHER_LOGGING "[MDI_GPS_LOG]mdi_gps_nmea_get_catcher_logging=%d"
        #define MDI_GPS_LOG_TRC_GET_FILE_LOGGING "[MDI_GPS_LOG]mdi_gps_nmea_get_file_logging=%d"
        #define MDI_GPS_LOG_TRC_GET_AGENT_LOGGING "[MDI_GPS_LOG]mdi_gps_nmea_get_agent_logging=%d"
        #define MDI_GPS_LOG_TRC_GET_DEBUG_INFO "[MDI_GPS_LOG]mdi_gps_nmea_get_agent_logging=%d"
        #define MDI_GPS_LOG_TRC_OPEN_FILE_HANDLE "[MDI_GPS_LOG]open file handle=%d"
        #define MDI_GPS_LOG_TRC_OPEN_DBG_FILE_HANDLE "[MDI_GPS_LOG]open debug info file handle=%d"
        #define MDI_GPS_LOG_TRC_CLOSE_FILE_HANDLE "[MDI_GPS_LOG]close file handle=%d"
        #define MDI_GPS_LOG_TRC_OPEN_FILE_ERROR "[MDI_GPS_LOG]open file error=%d"
        #define MDI_GPS_LOG_TRC_AGENT_WRITE_REQ "[MDI_GPS_LOG]Agent ask write data handle=%d data_ptr=%d length=%d written=%d"
        #define MDI_GPS_LOG_TRC_SET_WORK_PORT "[MDI_GPS_LOG]mdi_gps_set_work_port=%d"
        #define MDI_GPS_LOG_TRC_MAKE_NMA_FILE_ERR "[MDI_GPS_LOG]Make NMA file name error!"
        #define MDI_GPS_LOG_TRC_MAKE_DBG_FILE_ERR "[MDI_GPS_LOG]Make DBG file name error!"
        #define MDI_GPS_LOG_TRC_CLOSE_DBG_FILE "[MDI_GPS_LOG]close DBG log file handle=%d"

    /* --------------------------------------------------------------- */
    /* ----------------------- GPS Management ------------------------ */
    /* --------------------------------------------------------------- */
    #define MMI_TRC_GPS_MGR_DA_FILE         "[GPS Mgr] mmi_gps_mgr_da_file" 
    #define MMI_TRC_GPS_MGR_IS_AGPS_ON         "[GPS Mgr] Is agps on? %d" 
    #define MMI_TRC_GPS_MGR_REQ_LIST_INDEX         "[GPS Mgr] Request list index: %d" 
    #define MMI_TRC_GPS_MGR_SEND_MSG_PUSH_REQ         "[GPS Mgr] ==> MSG_ID_SUPL_MMI_PUSH_REQ" 
    #define MMI_TRC_GPS_MGR_SEND_MSG_ID         "[GPS Mgr] ID: %d" 
    #define MMI_TRC_GPS_MGR_SEND_MSG_END         "[GPS Mgr] > End <" 
    #define MMI_TRC_GPS_MGR_CHANGE_STATE         "[GPS Mgr] Change state to %d"
    #define MMI_TRC_GPS_MGR_UART_P_CALLBACK         "[GPS Mgr] mmi_gps_mgr_uart_p_callback         Type: %d"
    #define MMI_TRC_GPS_MGR_SET_SWITCH_MODE         "[GPS Mgr] Set switch mode to: %c%c"
    #define MMI_TRC_GPS_MGR_OPEN_GPS         "[GPS Mgr] mmi_gps_mgr_open_gps port: %d         uart_work_mode: %d         switch_mode: %d"
    #define MMI_TRC_GPS_MGR_RETURN         "[GPS Mgr] Return: %d"
    #define MMI_TRC_GPS_MGR_OPEN_GPS_NUM         "[GPS Mgr] Open GPS number(RD/L/LQ): %d         %d         %d"
    #define MMI_TRC_GPS_MGR_OPEN_GPS_UART         "[GPS Mgr] Open GPS UART"
    #define MMI_TRC_GPS_MGR_CLOSE_GPS_UART         "[GPS Mgr] Close GPS UART"
    #define MMI_TRC_GPS_MGR_SEND_MSG_START_REQ         "[GPS Mgr] ==> MSG_ID_SUPL_MMI_START_REQ" 
    #define MMI_TRC_GPS_MGR_SET_WORK_MODE         "[GPS Mgr] mmi_gps_mgr_set_work_mode" 
    #define MMI_TRC_GPS_MGR_SEND_START_SUPL_REQ         "[GPS Mgr] mmi_gps_mgr_send_start_supl_req" 
    #define MMI_TRC_GPS_MGR_SEND_MSG_STATUS_RSP         "[GPS Mgr] ==> MSG_ID_SUPL_MMI_STATUS_RSP" 
    #define MMI_TRC_GPS_MGR_CLOSE_GPS         "[GPS Mgr] mmi_gps_mgr_close_gps port: %d         uart_work_mode: %d"
    #define MMI_TRC_GPS_MGR_SEND_MSG_ABORT_REQ         "[GPS Mgr] ==> MSG_ID_SUPL_MMI_ABORT_REQ" 
    #define MMI_TRC_GPS_MGR_STATUS_IND_HDLR         "[GPS Mgr] mmi_gps_mgr_supl_mmi_status_ind_hdlr"
    #define MMI_TRC_GPS_MGR_STATUS_TERMINATED         "[GPS Mgr] mmi_gps_mgr_supl_mmi_status_terminated"
    #define MMI_TRC_GPS_MGR_STATUS_ACTIVATED         "[GPS Mgr] mmi_gps_mgr_supl_mmi_status_activated"
    #define MMI_TRC_GPS_MGR_STATUS_STAND_BY         "[GPS Mgr] mmi_gps_mgr_supl_mmi_status_stand_by"
    #define MMI_TRC_GPS_MGR_REQ_ID         "[GPS Mgr] ID: %d" 
    #define MMI_TRC_GPS_MGR_SHOW_POPUP         "[GPS Mgr] Show Popup" 
    #define MMI_TRC_GPS_MGR_CALLBACK_MMI         "[GPS Mgr] Callback MMI"
    #define MMI_TRC_GPS_MGR_NOTIFY_ACCEPT         "[GPS Mgr] mmi_gps_mgr_notify_accept"
    #define MMI_TRC_GPS_MGR_NOTIFY_DECLINE         "[GPS Mgr] mmi_gps_mgr_notify_decline"
    #define MMI_TRC_GPS_MGR_NOTIFY_TIME_OUT         "[GPS Mgr] mmi_gps_mgr_notify_time_out"
    #define MMI_TRC_GPS_MGR_ABORT_IND_HDLR         "[GPS Mgr] mmi_gps_mgr_supl_mmi_abort_ind_hdlr"
    #define MMI_TRC_GPS_MGR_GPS_CALLBACK         "[GPS Mgr] mmi_gps_mgr_gps_callback type: %d"
    #define MMI_TRC_GPS_MGR_FIXED         "[GPS Mgr] %cD Fixed"
    #define MMI_TRC_GPS_MGR_NO_FIXED         "[GPS Mgr] Don't Fixed"
    #define MMI_TRC_GPS_MGR_TIME_SYNC         "[GPS Mgr] Time Sync"
    #define MMI_TRC_GPS_MGR_TIME_SYNC_UTC         "[GPS Mgr] UTC:"
    #define MMI_TRC_GPS_MGR_TIME_SYNC_RTC         "[GPS Mgr] RTC:"
    #define MMI_TRC_GPS_MGR_TIME_SYNC_YMD         "[GPS Mgr] Year: %d         Month: %d         Date: %d"
    #define MMI_TRC_GPS_MGR_TIME_SYNC_HMS         "[GPS Mgr] Hour: %d         Minute: %d         Second: %d"
    #define MMI_TRC_GPS_MGR_PUSH_DATA_LEN         "[GPS Mgr] Wap push data len: %d"
    #define MMI_TRC_GPS_MGR_CURRENT_PROFILE         "[GPS Mgr] Current profile: %d"
    #define MMI_TRC_GPS_MGR_ACCOUND_ID         "[GPS Mgr] Account ID: %d"
    #define MMI_TRC_GPS_MGR_FLIGHT_MODE         "[GPS Mgr] Flight Mode"
    #define MMI_TRC_GPS_MGR_SIM_NOT_VAILD         "[GPS Mgr] SIM card is not vaild"
    #define MMI_TRC_GPS_MGR_IN_ACLL         "[GPS Mgr] In Call"
    #define MMI_TRC_GPS_MGR_ADD_TO_QUEUE         "[GPS Mgr] Add PUSH to queue"
    #define MMI_TRC_GPS_MGR_CALL_END         "[GPS Mgr] mmi_gps_mgr_call_end"
    #define MMI_TRC_GPS_MGR_SEND_PUSH_REQ         "[GPS Mgr] mmi_gps_mgr_send_push_req"
    #define MMI_TRC_GPS_MGR_SHOW_STATUS_ICON         "[GPS Mgr] Show AGPS status icon"
    #define MMI_TRC_GPS_MGR_P_INFO_CALLBACK         "[GPS Mgr] mmi_gps_mgr_p_info_callback type: %d"
    #define MMI_TRC_GPS_MGR_CHECK_ACK_SMS         "[GPS Mgr] mmi_gps_mgr_check_ack_sms"
    #define MMI_TRC_GPS_MGR_ABORT_CLOSE_GPS         "[GPS Mgr] mmi_gps_mgr_abort_close_gps index: %d"
    #define MMI_TRC_GPS_MGR_ABORT_ERROR_RSP         "[GPS Mgr] mmi_gps_mgr_abort_error_rsp index: %d"
    #define MMI_TRC_GPS_MGR_ABORT_FREE_REQ         "[GPS Mgr] mmi_gps_mgr_abort_free_req index: %d"
#endif
	
	
