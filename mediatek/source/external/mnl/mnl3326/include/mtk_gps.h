/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
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
 *   mtk_gps.h
 *
 * Description:
 * ------------
 *   Prototype of MTK navigation library
 *
 ****************************************************************************/

#ifndef MTK_GPS_H
#define MTK_GPS_H


#include "mtk_gps_type.h"


#ifdef __cplusplus
  extern "C" {
#endif



/* ================= Application layer functions ================= */

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_init
 * DESCRIPTION
 *  Initialize MTK Nav Library
 * PARAMETERS
 *  start_type      [IN]  auto/warm/cold/factory start
 *  default_cfg     [IN]  factory default configuration
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_init (mtk_gps_start_type start_type, const mtk_init_cfg* default_cfg);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_run
 * DESCRIPTION
 *  The main routine for the MTK Nav Library task
 * PARAMETERS
 *  application_cb      [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_run (mtk_gps_callback application_cb);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_uninit
 * DESCRIPTION
 *  Free the resources used by MTK Nav Library
 *****************************************************************************/
void
mtk_gps_uninit (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_update_gps_data
 * DESCRIPTION
 *  Force to write NV-RAM data to storage file
 * PARAMETERS
 *
 * RETURNS
 *  None
 *****************************************************************************/
void
mtk_gps_update_gps_data (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_data_input
 * DESCRIPTION
 *
 * PARAMETERS
 *  buffer      [IN]
 *  length      [IN]
 *  p_accepted_length [OUT]  indicate how many data was actually accepted into library
 *                          if this value is not equal to length, then it means library internal
 *                          fifo is full, please let library task can get cpu usage to digest input data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_gps_data_input (const char* buffer, mtk_uint32 length, mtk_uint32* p_accepted_length);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_rtcm_input
 * DESCRIPTION
 *  accept RTCM differential correction data
 * PARAMETERS
 *  buffer      [IN]   the content of RTCM data
 *  length      [IN]   the length of RTCM data (no more than 1KB)
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_gps_rtcm_input (const char* buffer, mtk_uint32 length);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_nmea_input
 * DESCRIPTION
 *  accept NMEA (PMTK) sentence raw data
 * PARAMETERS
 *  buffer      [IN]   the content of NMEA (PMTK) data
 *  length      [IN]   the length of NMEA (PMTK) data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32 mtk_gps_nmea_input (const char* buffer, mtk_uint32 length);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_agps_input
 * DESCRIPTION
 *  accept NMEA (PMTK) sentence raw data (only for agent using)
 * PARAMETERS
 *  buffer      [IN]   the content of NMEA (PMTK) data
 *  length      [IN]   the length of NMEA (PMTK) data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32 
mtk_gps_agps_input (const char* buffer, mtk_uint32 length);



/* ====================== Utility functions ====================== */
/*  These functions must be used in application_cb() callback
    function specified in mtk_gps_run()                            */

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_position
 * DESCRIPTION
 *  obtain detailed fix information
 * PARAMETERS
 *  pvt_data    [OUT]  pointer to detailed fix information
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_get_position (mtk_gps_position* pvt_data);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_sv_info
 * DESCRIPTION
 *  obtain detailed information of all satellites
 * PARAMETERS
 *  sv_data     [OUT]  pointer to satellites information
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_gps_get_sv_info (mtk_gps_sv_info* sv_data);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_param
 * DESCRIPTION
 *  Get the current setting of the GPS receiver
 * PARAMETERS
 *  key         [IN]   the configuration you want to know
 *  value       [OUT]  the current setting
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 * EXAMPLE
 *  // get the current DGPS mode
 *  mtk_param_dgps_config param_dgps_config;
 *  mtk_gps_get_param(MTK_PARAM_DGPS_CONFIG, &param_dgps_config);
 *  printf("DGPS mode=%d", (int)param_dgps_config.dgps_mode);
 *****************************************************************************/
mtk_int32
mtk_gps_get_param (mtk_gps_param key, void* value, mtk_uint16 srcMod, mtk_uint16 dstMod);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_set_param
 * DESCRIPTION
 *  Change the behavior of the GPS receiver
 * PARAMETERS
 *  key         [IN]   the configuration needs to be changed
 *  value       [IN]   the new setting
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_set_param (mtk_gps_param key, const void* value, mtk_uint16 srcMod, mtk_uint16 dstMod);

/*****************************************************************************
 * FUNCTION
 *  mtk_agps_get_param
 * DESCRIPTION
 *  Get the current setting of the AGPS Agent
 * PARAMETERS
 *  key         [IN]   the configuration you want to know
 *  value       [OUT]  the current setting
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_agps_get_param (mtk_gps_param key, void* value, mtk_uint16 srcMod, mtk_uint16 dstMod);

/*****************************************************************************
 * FUNCTION
 *  mtk_agps_set_param
 * DESCRIPTION
 *  Change the behavior of the AGPS Agent
 * PARAMETERS
 *  key         [IN]   the configuration needs to be changed
 *  value       [IN]   the new setting
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_agps_set_param (mtk_gps_param key, const void* value, mtk_uint16 srcMod, mtk_uint16 dstMod);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_set_pmtk_data
 * DESCRIPTION
 *  send PMTK command to GPS receiver
 * PARAMETERS
 *  prPdt       [IN]   pointer to the data structure of the PMTK command
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_set_pmtk_data (const MTK_PMTK_DATA_T *prPdt, mtk_uint16 srcMod, mtk_uint16 dstMod); 

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_pmtk_response
 * DESCRIPTION
 *  obtain detailed information of PMTK response
 * PARAMETERS
 *  rs_data     [OUT]  pointer to PMTK response data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_gps_get_pmtk_response (MTK_PMTK_RESPONSE_T *prRsp);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_set_position
 * DESCRIPTION
 *  Set the receiver's initial position
 *  Notes: To make the initial position take effect, please invoke restart
 *         (hot start/warm start) after this function
 * PARAMETERS
 *  LLH         [IN]  LLH[0]: receiver latitude in degrees (positive for North)
 *                    LLH[1]: receiver longitude in degrees (positive for East)
 *                    LLH[2]: receiver WGS84 height in meters
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_set_position (const double LLH[3]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_set_time
 * DESCRIPTION
 *  Set the current GPS time
 *  Note:       The time will not be set if the receiver has better knowledge
 *              of the time than the new value.
 * PARAMETERS
 *  weekno      [IN]   GPS week number (>1024)
 *  TOW         [IN]   time of week (in second; 0.0~684800.0)
 *  timeRMS     [IN]   estimated RMS error of the TOW value (sec^2)
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_set_time (mtk_uint16 weekno, double tow, float timeRMS);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_set_ephemeris
 * DESCRIPTION
 *  Upload ephemeris
 * PARAMETERS
 *  svid        [IN]   GPS satellite PRN (1~32)
 *  data        [IN]   binary ephemeris words from words 3-10 of subframe 1-3
 *                     all parity bits (bit 5-0) have been removed
 *                     data[0]: bit 13-6 of word 3, subframe 1
 *                     data[1]: bit 21-14 of word 3, subframe 1
 *                     data[2]: bit 29-22 of word 3, subframe 1
 *                     data[3]: bit 13-6 of word 4, subframe 1
 *                     ......
 *                     data[71]: bit 29-22 of word 10, subframe 3
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_set_ephemeris (mtk_uint8 svid, const char data[72]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_set_almanac
 * DESCRIPTION
 *  Upload almanac
 * PARAMETERS
 *  svid        [IN]   GPS satellite PRN (1~32)
 *  weekno      [IN]   the week number of the almanac data record
 *  data        [IN]   binary almanac words from words 3-10 of either subframe 4
 *                     pages 2-10 or subframe 5 pages 1-24
 *                     all parity bits (bit 5-0) have been removed
 *                     data[0]: bit 13-6 of word 3
 *                     data[1]: bit 21-14 of word 3
 *                     data[2]: bit 29-22 of word 3
 *                     data[3]: bit 13-6 of word 4
 *                     ......
 *                     data[23]: bit 29-22 of word 10
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_set_almanac (mtk_uint8 svid, mtk_uint16 weekno, const char data[24]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_ephemeris
 * DESCRIPTION
 *  Download ephemeris
 * PARAMETERS
 *  svid        [IN]   GPS satellite PRN (1~32)
 *  data        [OUT]  binary ephemeris words from words 3-10 of subframe 1-3
 *                     all parity bits (bit 5-0) have been removed
 *                     data[0]: bit 13-6 of word 3, subframe 1
 *                     data[1]: bit 21-14 of word 3, subframe 1
 *                     data[2]: bit 29-22 of word 3, subframe 1
 *                     data[3]: bit 13-6 of word 4, subframe 1
 *                     ......
 *                     data[71]: bit 29-22 of word 10, subframe 3
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_get_ephemeris (mtk_uint8 svid, char data[72]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_almanac
 * DESCRIPTION
 *  Download almanac
 * PARAMETERS
 *  svid        [IN]   GPS satellite PRN (1~32)
 *  p_weekno    [OUT]  pointer to the week number of the almanac data record
 *  data        [OUT]  binary almanac words from words 3-10 of either subframe 4
 *                     pages 2-10 or subframe 5 pages 1-24
 *                     all parity bits (bit 5-0) have been removed
 *                     data[0]: bit 13-6 of word 3
 *                     data[1]: bit 21-14 of word 3
 *                     data[2]: bit 29-22 of word 3
 *                     data[3]: bit 13-6 of word 4
 *                     ......
 *                     data[23]: bit 29-22 of word 10
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_get_almanac (mtk_uint8 svid, mtk_uint16* p_weekno, char data[24]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_clear_ephemeris
 * DESCRIPTION
 *  clear the ephemeris of the specified PRN
 * PARAMETERS
 *  svid        [IN]   GPS satellite PRN (1~32)
 *****************************************************************************/
void
mtk_gps_clear_ephemeris (mtk_uint8 svid);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_clear_almanac
 * DESCRIPTION
 *  clear the almanac of the specified PRN
 * PARAMETERS
 *  svid        [IN]   GPS satellite PRN (1~32)
 *****************************************************************************/
void
mtk_gps_clear_almanac (mtk_uint8 svid);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_sbas_msg_amount
 * DESCRIPTION
 *  Get the number of SBAS message blocks received in this epoch.
 *  Later on, please use mtk_gps_get_sbas_msg() to get the message 
 *  content one by one.
 * PARAMETERS
 *  p_msg_amount  [OUT]   The number of SBAS message blocks received in this epoch
 * RETURNS
 *  
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_get_sbas_msg_amount (mtk_uint32* p_msg_amount);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_sbas_msg
 * DESCRIPTION
 *  After calling mtk_gps_get_sbas_msg_amount(), we know the
 *  number of SBAS messages received in this epoch.
 *  mtk_gps_get_sbas_msg() gives a way to access each message
 *  data 
 * PARAMETERS
 *  index        [IN]   which message you want to read
 *  pSVID        [OUT]  the PRN of the SBAS satellite
 *  pMsgType     [OUT]  the SBAS message type
 *  pParityError [OUT]  nonzero(parity error); zero(parity check pass)
 *  data         [OUT]  The 212-bit message data excluding the preamble,
 *                      message type field, and the parity check.
 *                      Regarding to endian, the data[0] is the beginning the
 *                      message, such as IODP field. The data[26] is the end of
 *                      message, and the bit 3..0 are padding zero.
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 * EXAMPLE
 *   //dump the SBAS message to UART output
 *   int  i, count; 
 *   unsigned char PRN, MsgType, ParityError;
 *   char data[27];
 *
 *   mtk_gps_get_sbas_msg_amount(&count);
 *   for (i = 0; i < count; i++)
 *   {
 *      mtk_gps_get_sbas_msg(i, &PRN, &MsgType, &ParityError, data);
 *   }
 *****************************************************************************/
mtk_int32
mtk_gps_get_sbas_msg (mtk_int32 index, unsigned char* pSVID,
     unsigned char* pMsgType, unsigned char* pParityError, char data[27]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_chn_status
 * DESCRIPTION
 *  Get Channel SNR and Clock Drift Status in Channel Test Mode
 * PARAMETERS
 *  ChnSNR       [OUT]  Channel SNR
 *  ClkDrift     [OUT]  Clock Drift
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *  if you do not enter test mode first or Channel tracking not ready,
 *  return MTK_GPS_ERROR
 *****************************************************************************/
mtk_int32
mtk_gps_get_chn_test(float ChnSNR[16], float *ClkDrift);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_jammer_test
 * DESCRIPTION
 *  Obtain the CW jammer estimation result
 * PARAMETERS
 *  Freq         [OUT]  jammer frequency offset in KHz
 *  JNR          [OUT]  JNR of the associated jammer
 *  jammer_peaks [OUT]  The number (0~195) of jammer peaks if ready
 *                      0 means no jammer detected
 *                      Negative if not ready
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_get_jammer_test(mtk_int32 *jammer_peaks, short Freq[195], mtk_uint16 JNR[195]);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_get_phase_test
 * DESCRIPTION
 *  Obtain the last phase error calibration result
 * PARAMETERS
 *  result       [OUT]  0~64 (success)
 *                      Negative (failure or not ready)
 *                      The return value is 64*{|I|/sqrt(I*I + Q*Q)}
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_get_phase_test(mtk_int32 *result);


//*************************************************************************************
// mtk_gps_get_sat_accurate_snr  :  Get the accurate SNR of all satellites
//
//    Note  :  SNR is an array with 32 float.
// Example:
//    float SNR[32];
//    mtk_gps_get_sat_accurate_snr(SNR);
//    //Get SNR of SV 17
//    MTK_UART_OutputData("SV17: SNR = %lf", SNR[16]);
//    
// =====> SV17: SNR = 38.1

void mtk_gps_get_sat_accurate_snr(mtk_r4 SNR[32]);


/*****************************************************************************
 * FUNCTION
 *  mtk_gps_time_change_notify
 * DESCRIPTION
 *  Notify MNL to handle RTC time change
 * PARAMETERS
 *  i4RtcDiff      [IN]  System RTC time changed: old rtc time - new rtc time
 * RETURNS
 *  
 *****************************************************************************/
void
mtk_gps_time_change_notify(mtk_int32 i4RtcDiff);

/*****************************************************************************
 * FUNCTION
 *  mtk_gps_system_config
 * DESCRIPTION
 *  Configure the settings in MNL
 * PARAMETERS
 *  i4Type      [IN]  System configuration category number
 *  u4Config   [IN]  Configuration of the category
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_gps_system_config(mtk_int32 i4Type, mtk_uint32 u4Config );

/* =================== Porting layer functions =================== */
/*            The function body needs to be implemented            */

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_time_read
 * DESCRIPTION
 *
 * PARAMETERS
 *  utctime     [OUT]
 * RETURNS
 *  success (MTK_GPS_SUCCESS)
 *  failed (MTK_GPS_ERROR)
 *  system time changed since last call (MTK_GPS_ERROR_TIME_CHANGED)
 *****************************************************************************/
mtk_int32
mtk_sys_time_read (mtk_time* utctime);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_time_write
 * DESCRIPTION
 *
 * PARAMETERS
 *  utctime     [IN]
 * RETURNS
 *  success (MTK_GPS_SUCCESS)
 *  failed (MTK_GPS_ERROR)
 *  system time changed since last call (MTK_GPS_ERROR_TIME_CHANGED)
 *****************************************************************************/
mtk_int32
mtk_sys_time_write (mtk_time utctime);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_task_sleep
 * DESCRIPTION
 *
 * PARAMETERS
 *  milliseconds [IN]
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_task_sleep (mtk_uint32 milliseconds);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_open
 * DESCRIPTION
 *
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_storage_open (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_close
 * DESCRIPTION
 *
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_storage_close (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_delete
 * DESCRIPTION
 *
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_storage_delete (void);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_read
 * DESCRIPTION
 *
 *  blocking read until reaching 'length' or EOF
 * PARAMETERS
 *  buffer      [OUT]
 *  offset      [IN]
 *  length      [IN]
 *  p_nRead     [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_storage_read (void* buffer, mtk_uint32 offset, mtk_uint32 length,
                      mtk_uint32* p_nRead);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_write
 * DESCRIPTION
 *
 * PARAMETERS
 *  buffer      [IN]
 *  offset      [IN]
 *  length      [IN]
 *  p_nWritten  [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_storage_write (const void* buffer, mtk_uint32 offset, mtk_uint32 length,
                       mtk_uint32* p_nWritten);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_create
 * DESCRIPTION
 *  Create an event object for gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_create(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_delete
 * DESCRIPTION
 *  Delete the event object of gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_delete(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_set
 * DESCRIPTION
 *  Set the event object of gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_set(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_wait
 * DESCRIPTION
 *  Wait for the event object of gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_wait(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_cnt
 * DESCRIPTION
 *
 * PARAMETERS
 * RETURNS
 *  message count of main GPS thread
 *****************************************************************************/
mtk_int32
mtk_sys_msg_cnt (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_recv
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_msg_recv (mtk_gps_msg** msg);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_send
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_msg_send (const mtk_gps_msg* msg);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_create
 * DESCRIPTION
 *  Create an event object for Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_create(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_delete
 * DESCRIPTION
 *  Delete the event object of Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_delete(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_set
 * DESCRIPTION
 *  Set the event object of Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_set(void);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_wait
 * DESCRIPTION
 *  Wait for the event object of Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_wait(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_cnt
 * DESCRIPTION
 *
 * PARAMETERS
 * RETURNS
 *  message count of main GPS thread
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_cnt (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_recv
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_recv (mtk_gps_msg** msg);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_send
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_send (const mtk_gps_msg* msg);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_reset
 * DESCRIPTION
 *
 * PARAMETERS
 *  none
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_reset (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_alloc
 * DESCRIPTION
 *
 * PARAMETERS
 *  size        [IN]   the length of the whole mtk_gps_msg structure
 * RETURNS
 *  pointer to the created message if succeeded
 *  NULL (0) if failed
 *****************************************************************************/
mtk_gps_msg*
mtk_sys_msg_alloc (mtk_uint16 size);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_free
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_msg_free (mtk_gps_msg* msg);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_mem_alloc
 * DESCRIPTION
 *  Allocate a block of memory
 * PARAMETERS
 *  size        [IN]   the length of the whole memory to be allocated
 * RETURNS
 *  On success, return the pointer to the allocated memory
 *  NULL (0) if failed
 *****************************************************************************/
void*
mtk_sys_mem_alloc (mtk_uint32 size);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_mem_free
 * DESCRIPTION
 *  Release unused memory
 * PARAMETERS
 *  pmem         [IN]
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_mem_free (void* pmem);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_data_output
 * DESCRIPTION
 *  Transmit data to the GPS chip
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_data_output (char* buffer, mtk_uint32 length);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_data_output_dbg
 * DESCRIPTION
 *  Transmit debug data out to task
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  buffer         [IN] data pointer
 *  length         [IN] size of data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_data_output_dbg (char* buffer, mtk_uint32 length);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_data_output_dbg
 * DESCRIPTION
 *  Transmit debug data out to task
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  buffer         [IN] data pointer
 *  length         [IN] size of data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_data_output_dbg_uart (char* buffer, mtk_uint32 length);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_output_dbg
 * DESCRIPTION
 *  Transmit gps debug data out to task
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_output_dbg (char* buffer, mtk_uint32 length);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_if_set_spd
 * DESCRIPTION
 *  Set baud rate at host side from GPS lib
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  baudrate         [IN]
 *  hw_fc            [IN]
 * RETURNS
 *  success(0)
 *****************************************************************************/
mtk_int32
mtk_sys_if_set_spd (mtk_uint32 baudrate, mtk_uint8 hw_fc);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_create_mutex
 * DESCRIPTION
 *  Create a mutex object
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
  * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_create_mutex(mtk_mutex_enum mutex_idx);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_take_mutex
 * DESCRIPTION
 *  Request ownership of a mutex and if it's not available now, then block the thread execution
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_take_mutex(mtk_mutex_enum mutex_idx);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_give_mutex
 * DESCRIPTION
 *  Release a mutex ownership
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_give_mutex(mtk_mutex_enum mutex_idx);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_destroy_mutex
 * DESCRIPTION
 *  Destroy a mutex object
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_destroy_mutex(mtk_mutex_enum mutex_idx);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_start_result_handler
 * DESCRIPTION
 *  Handler routine for the result of restart command
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  result         [IN]  the result of restart
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32 
mtk_sys_start_result_handler(mtk_gps_start_result result);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_spi_poll
 * DESCRIPTION
 *  Polling data input routine for SPI during dsp boot up stage.
 *  If use UART interface, this function can do nothing at all.
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  void
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_spi_poll(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_set_spi_mode
 * DESCRIPTION
 *  Set SPI interrupt/polling and support burst or not.
 *  If use UART interface, this function can do nothing at all.
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  enable_int         [IN]  1 for enter interrupt mode , 0 for entering polling mode
 *  enable_burst       [IN]  1 for enable burst transfer, 0 for disable burst transfer
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_set_spi_mode(mtk_uint8 enable_int, mtk_uint8 enable_burst);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_dsp_boot_begin_handler
 * DESCRIPTION
 *  Handler routine for porting layer implementation right before GPS DSP boot up
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  none
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32 
mtk_sys_dsp_boot_begin_handler(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_dsp_boot_end_handler
 * DESCRIPTION
 *  Handler routine for porting layer implementation right after GPS DSP boot up
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  none
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32 
mtk_sys_dsp_boot_end_handler(void);


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_frame_sync_meas
 * DESCRIPTION
 * PARAMETERS
 *  pFrameTime [OUT] frame time of the issued frame pulse (seconds)
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/

mtk_int32 mtk_sys_frame_sync_meas(mtk_r8 *pdfFrameTime);

mtk_int32 mtk_sys_frame_sync_enable_sleep_mode(mtk_bool mode);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_epo_open
 * DESCRIPTION
 *  Open EPO file
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_epo_open (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_epo_close
 * DESCRIPTION
 *  Close EPO file
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_epo_close (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_epo_read
 * DESCRIPTION
 *  Read EPO file
 *  (blocking read until reaching 'length' or EOF)
 * PARAMETERS
 *  buffer      [OUT]
 *  offset      [IN]
 *  length      [IN]
 *  p_nRead     [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_epo_read (void* buffer, mtk_uint32 offset, mtk_uint32 length,
                      mtk_uint32* p_nRead);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_create
 * DESCRIPTION
 *  Create an event object for BEE module
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_create(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_delete
 * DESCRIPTION
 *  Delete the event object of BEE module
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_delete(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_set
 * DESCRIPTION
 *  Set the event object of BEE module
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_set(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_wait
 * DESCRIPTION
 *  Wait for the event object of BEE module, blocking mode
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_wait(void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_pmtk_cmd_cb
 * DESCRIPTION
 *  Notify porting layer that MNL has received one PMTK command.
 * PARAMETERS
 *  u2CmdNum        [IN]  The received PMTK command number.
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_pmtk_cmd_cb(mtk_uint16 u2CmdNum);

/*****************************************************************************
* FUNCTION
*  mtk_sys_time_tick_get
* DESCRIPTION
*  get the current system tick of target platform (msec)
* PARAMETERS
*  none
* RETURNS
*  system time tick
*****************************************************************************/
mtk_uint32
mtk_sys_time_tick_get (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_time_tick_get_max
 * DESCRIPTION
 *  get the maximum system tick of target platform (msec)
 * PARAMETERS
 *  none
 * RETURNS
 *  system time tick
 *****************************************************************************/
mtk_uint32
mtk_sys_time_tick_get_max (void);

#ifdef __cplusplus
   }
#endif

#endif /* MTK_GPS_H */
