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
/*****************************************************************************
 *
 * Filename:
 * ---------
 * mdi_datatype.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  Audio interfce header file
 *
 * Author:
 * -------
 * Brian (MTK00427)
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
 * Jun 10 2010 mtk01564
 * [MAUI_02198391] [Phone settings] power on video can choise RMVB file
 * 
 *
 * May 12 2010 mtk01564
 * [MAUI_02419724] [mATV] stage 2 code check in CR
 * 
 *
 * Apr 25 2010 mtk01166
 * [MAUI_02405499] 6253D block Video preview + GPRS
 * 
 *
 * Apr 23 2010 mtk01877
 * [MAUI_02404894] [MDI][CAM] MT6253D camera & GPRS concurrency exclusive
 * 
 *
 * Nov 28 2009 mtk01429
 * [MAUI_02002832] add new error code to bitstream API
 * 
 *
 * Jul 10 2009 mtk01429
 * [MAUI_01717598] Check-in OGDR specific streaming/ PDL support to MAUI/09A
 * 
 *
 * Jun 18 2009 mtk01564
 * [MAUI_01479092] [CMMB] while recording, prompt Error
 * 
 *
 * Jun 4 2009 mtk01429
 * [MAUI_01836971] [media player]during open the link, popup a error
 * 
 *
 * Apr 14 2009 mtk01166
 * [MAUI_01728616] [1] Assert fail: lcd_update_idp_series.c 823 - MED
 * 
 *
 * Apr 1 2009 mtk00676
 * [MAUI_01659085] [MDI] Move MDI audio error to -9000 and add general MDI string for failure popup
 * 
 *
 * Mar 21 2009 mtk01429
 * [MAUI_01652264] fix daily build compile error -> MDI_RES_WEBCAM/ MED_RES_TVOUT
 * 
 *
 * Mar 13 2009 mtk01564
 * [MAUI_01642368] Pop up rule
 * 
 *
 * Mar 6 2009 mtk01564
 * [MAUI_01642368] Pop up rule
 * 
 *
 * Feb 6 2009 mtk01564
 * [MAUI_01322717] CMMB_The playing panel won't disapear while A2DP turn on.
 * 
 *
 * Dec 26 2008 mtk00676
 * [MAUI_01305832] [BITSTREAM] Check-in new audio/video bitstream API
 * 
 *
 * Nov 26 2008 mtk01564
 * [MAUI_01285242] MobileTV: Background Call Support
 * 
 *
 * Nov 4 2008 mbj06018
 * [MAUI_01332102] [Motion]Motion sensor enhance add user_data check in
 * 
 *
 * Oct 8 2008 mbj06018
 * [MAUI_01120902] [Motion]Motion sensor MDI enhance check in
 * 
 *
 * Sep 17 2008 mtk01166
 * [MAUI_01232094] [1] Fatal Error: 1=305 2=88880027 - L4
 * 
 *
 * Aug 8 2008 mtk01166
 * [MAUI_01190839] [1] Fatal Error (305, 88880026) - L4
 * 
 *
 * Jul 13 2008 mtk01564
 * [MAUI_00802160] T-DMB_MMI won't pop up "Mobile TV recorder is stopped" in IDLE.
 * 
 *
 * May 28 2008 mtk01564
 * [MAUI_00778883] TV Out_T-DMB_The channel name of DAB won't scroll in the pweviewing window in TV.
 * 
 *
 * May 23 2008 mbj06018
 * [MAUI_01046192] [AGPS]Check in RTC syn EM104 return can write at mdi_gps
 * 
 *
 * May 16 2008 mtk00676
 * [MAUI_00773405] [SCO] Add parameter to pass SCO connect error cause to MMI
 * 
 *
 * Apr 12 2008 mtk01166
 * [MAUI_00750748] T-DMB_MMI should better to pop up "Root dir full" before it start to play same as Vi
 * 
 *
 * Mar 23 2008 mtk00612
 * [MAUI_00736676] [Camcorder] Check-in
 * 
 *
 * Mar 21 2008 mtk00612
 * [MAUI_00735852] [PDCF] New feature check in
 * 
 *
 * Mar 10 2008 mtk00612
 * [MAUI_00725721] Video Recorder_The recorder has no effect when setting effect from Effect setting.
 * 
 *
 * Jan 25 2008 mtk01166
 * [MAUI_00611630] [mdi][cam] Revise
 * 
 *
 * Jan 24 2008 mtk00612
 * [MAUI_00610100] [New Feature] Java CMCC Control
 * 
 *
 * Jan 23 2008 mtk00612
 * [MAUI_00610461] [Video] Multiple profile support
 * 
 *
 * Jan 23 2008 mtk00612
 * [MAUI_00609900] [New Feature] J2ME Shares Media Memory
 * 
 *
 * Dec 30 2007 mtk01166
 * [MAUI_00554952] [MTV][MDI][MMI] revise v0.2
 * 
 *
 * Nov 3 2007 mbj06018
 * [MAUI_00489164] [GIS]GIS check in
 * 
 *
 * Oct 20 2007 mbj06019
 * [MAUI_00482146] Trace group adjustment
 * 
 *
 * Sep 14 2007 mbj06078
 * [MAUI_00469883] [GIS] Add GIS (Mapber) to MAUI
 * 
 *
 * Sep 12 2007 mtk01166
 * [MAUI_00546443] [Barcode] New Interface checked in.
 * 
 *
 * Sep 5 2007 mtk00612
 * [MAUI_00532410] [VT] Check-in VT related code
 * 
 *
 * Jul 16 2007 mtk01166
 * [MAUI_00417534] Mobile TV Player, check in
 * 
 *
 * Jul 13 2007 mtk01166
 * [MAUI_00416344] Xenon Flash Support
 * 
 *
 * Jul 13 2007 mtk01166
 * [MAUI_00432148] [2D Bar Code]Decode for long time and without remind.
 * 
 *
 * Jun 29 2007 mbj06113
 * [MAUI_00442119] Mflash new feather check in(for beijing)
 * 
 *
 * Jun 15 2007 mtk00612
 * [MAUI_00405003] [Multimedia] DRM2.0
 * 
 *
 * May 17 2007 mtk00612
 * [MAUI_00232391] videoplayer_the audio is abnormal
 * 
 *
 * Apr 27 2007 mtk00612
 * [MAUI_00387201] [Video] Java stop time control.
 * 
 *
 * Apr 1 2007 mtk00612
 * [MAUI_00377933] [Revise] Remove MMI_ON_TARGET and MMI_ON_WIN32
 * 
 *
 * Mar 5 2007 mtk00612
 * [MAUI_00369664] [MT6225] Peformance issuse due to storage too slow.
 * 
 *
 * Feb 13 2007 mtk00676
 * [MAUI_00366607] [MDI] Modify MMA interface to support callback and keep state
 * 
 *
 * Feb 1 2007 mtk00612
 * [MAUI_00363554] [Patch] Solve MT6205B / MT6217 compile error due to Video feature is not turned on.
 * 
 *
 * Feb 1 2007 mtk00612
 * [MAUI_00363554] [Patch] Solve MT6205B / MT6217 compile error due to Video feature is not turned on.
 * 
 *
 * Jan 29 2007 mtk00612
 * [MAUI_00362591] [Video] Add video roi feature,
 * 
 *
 * Nov 24 2006 mtk00612
 * [MAUI_00342775] [Stream] Integration
 * 
 *
 * Oct 11 2006 MTK01166
 * [MAUI_00334738] [BarcodeReader] 1. MT6227 platform  2. Append Structure support.
 * 
 *
 * Sep 7 2006 MTK01166
 * [MAUI_00327787] [BarcodeReader:banding, error handle] [APP-Based ASM+3D game] [ImageViewer: M3D/SVG
 * 
 *
 * Aug 31 2006 mtk00322
 * [MAUI_00224706] Assert fail: img_context_png_config.upper_buffer != NULL img_msg_handler.c 2049 - ME
 * fix memory leak.
 *
 * Aug 15 2006 mtk00322
 * [MAUI_00321482] [MMI][SWFlash][NewFeature] SWFlash new feature check in
 * add swflash.
 *
 * Aug 8 2006 MTK01166
 * [MAUI_00224245] Image tile-wrong prompt
 * 
 *
 * Dec 20 2005 mtk00798
 * [MAUI_00162527] [Patch] fix a compile error in mdi_audio.h
 * 
 *
 * Oct 3 2005 mtk00612
 * [MAUI_00126880] [MMI][Video] Horizontal Video
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _MDI_DATATYPE_H_
#define _MDI_DATATYPE_H_

#include "kal_non_specific_general_types.h"     /* include this for kal data type */
#include "MMIDataType.h"
#include "lcd_if.h" 


typedef U16 mdi_state;
typedef S32 mdi_handle;
typedef S32 mdi_result;
typedef void (*mdi_callback) (mdi_result result);
typedef void (*mdi_bt_callback) (U8 profile, mdi_result result, U16 error_cause);
typedef BOOL(*mdi_bg_callback) (mdi_result result);
typedef void (*mdi_mma_callback) (kal_int32 handle, kal_int32 result);

typedef U16 jdi_state;
typedef S32 jdi_handle;
typedef S32 jdi_result;
typedef void (*jdi_callback) (jdi_result result);
typedef BOOL(*jdi_bg_callback) (jdi_result result);
typedef void (*jdi_mma_callback) (kal_int32 handle, kal_int32 event);

typedef struct 
{
    U16 lcd_start_x;
    U16 lcd_start_y;
    U16 lcd_end_x;
    U16 lcd_end_y;
    U16 display_width;
    U16 display_height;
    U16 roi_offset_x;
    U16 roi_offset_y;
    U8 *image_buffer_p;
    U32 image_buffer_size;
    U32 image_color_format;
    U16 lcd_rotate;
    MMI_BOOL is_visual_update;
    MMI_BOOL tv_output;
    U16 tv_output_mode;
    U16 tv_output_width;
    U16 tv_output_height;
    U16 tv_output_offset_x;
    U16 tv_output_offset_y;
    U32 tv_output_buffer_size;
    U8 *tv_output_buffer1_address;
    U8 *tv_output_buffer2_address;
} mdi_blt_para_struct;

#define MDI_HANDLE   mdi_handle
#define MDI_RESULT   mdi_result

#define JDI_HANDLE   jdi_handle
#define JDI_RESULT   jdi_result

/* camera 1000 */
#define MDI_RES_CAMERA_SUCCEED                        0
#define MDI_RES_CAMERA_XENONFLASH_CHARGING            1001
#define MDI_RES_CAMERA_ERR_FAILED                     -1001
#define MDI_RES_CAMERA_ERR_DISK_FULL                  -1002
#define MDI_RES_CAMERA_ERR_WRITE_PROTECTION           -1003
#define MDI_RES_CAMERA_ERR_NO_DISK                    -1004
#define MDI_RES_CAMERA_ERR_HW_NOT_READY               -1005
#define MDI_RES_CAMERA_ERR_MEMORY_NOT_ENOUGH          -1006
#define MDI_RES_CAMERA_ERR_PREVIEW_FAILED             -1007
#define MDI_RES_CAMERA_ERR_CAPTURE_FAILED             -1008
#define MDI_RES_CAMERA_ERR_ROOT_DIR_FULL              -1009
#define MDI_RES_CAMERA_ERR_XENONFLASH_TIMEOUT         -1010
#define MDI_RES_CAMERA_ERR_XENONFLASH_LOW_BATTERY     -1011
#define MDI_RES_CAMERA_ERR_CBM_BEARER_ERROR           -1012

/* webcam 1500 */
#define MDI_RES_WEBCAM_SUCCEED                      0
#define MDI_RES_WEBCAM_DRV_ABORT                    1501
#define MDI_RES_WEBCAM_ERR_FAILED                   -1502

/* video recorder 2000 */
#define MDI_RES_VDOREC_SUCCEED                        0
#define MDI_RES_VDOREC_DISK_FULL                      2001
#define MDI_RES_VDOREC_REACH_SIZE_LIMIT               2002
#define MDI_RES_VDOREC_RECORD_ALREADY_STOP            2003
#define MDI_RES_VDOREC_ERR_FAILED                     -2001
#define MDI_RES_VDOREC_ERR_DISK_FULL                  -2002
#define MDI_RES_VDOREC_ERR_WRITE_PROTECTION           -2003
#define MDI_RES_VDOREC_ERR_NO_DISK                    -2004
#define MDI_RES_VDOREC_ERR_HW_NOT_READY               -2005
#define MDI_RES_VDOREC_ERR_PREVIEW_FAILED             -2006
#define MDI_RES_VDOREC_ERR_RESUME_FAILED              -2007
#define MDI_RES_VDOREC_ERR_PAUSE_FAILED               -2008
#define MDI_RES_VDOREC_ERR_SAVE_FILE_FAILED           -2009
#define MDI_RES_VDOREC_ERR_POWER_ON_FAILED            -2010
#define MDI_RES_VDOREC_ERR_RECORD_FAILED              -2011
#define MDI_RES_VDOREC_ERR_STORAGE_TOO_SLOW           -2012
#define MDI_RES_VDOREC_ERR_CBM_BEARER_ERROR           -2013

/* vdioe player 3000 */
#define MDI_RES_VDOPLY_SUCCEED                        0
#define MDI_RES_VDOPLY_SEEK_PARTIAL_DONE              3001
#define MDI_RES_VDOPLY_PROGRESSIVE_FILE_NOT_ENOUGH    3002
#define MDI_RES_VDOPLY_REACH_STOP_TIME                3003
#define MDI_RES_VDOPLY_STREAM_BUFFER_OVERFLOW         3004
#define MDI_RES_VDOPLY_STREAM_BUFFER_UNDERFLOW        3005
#define MDI_RES_VDOPLY_STREAM_DRM_NEED_RIGHT          3006 
#define MDI_RES_VDOPLY_ALREADY_FINISHED                3007
#define MDI_RES_VDOPLY_ERR_FAILED                     -3001
#define MDI_RES_VDOPLY_ERR_OPEN_FILE_FAILED           -3002
#define MDI_RES_VDOPLY_ERR_FILE_TOO_LARGE             -3003
#define MDI_RES_VDOPLY_ERR_SNAPSHOT_FAILED            -3004
#define MDI_RES_VDOPLY_ERR_PLAY_FAILED                -3005
#define MDI_RES_VDOPLY_ERR_SEEK_FAILED                -3006
#define MDI_RES_VDOPLY_ERR_SNAPSHOT_DISK_FULL         -3007
#define MDI_RES_VDOPLY_ERR_SNAPSHOT_WRITE_PROTECTION  -3008
#define MDI_RES_VDOPLY_ERR_SNAPSHOT_NO_DISK           -3009
#define MDI_RES_VDOPLY_ERR_SNAPSHOT_ROOT_DIR_FULL     -3010
#define MDI_RES_VDOPLY_ERR_STREAM_CONNECT_FAILED      -3011
#define MDI_RES_VDOPLY_ERR_INVALID_RESOULTION         -3012
#define MDI_RES_VDOPLY_ERR_NETWORK_DISCONNECT         -3013
#define MDI_RES_VDOPLY_ERR_UNSUPPORTED_FORMAT         -3014
#define MDI_RES_VDOPLY_ERR_NETWORK_FORBIDDEN          -3015
#define MDI_RES_VDOPLY_ERR_MEMORY_INSUFFICIENT        -3016
#define MDI_RES_VDOPLY_ERR_DRM_PROHIBITED             -3017     /* no rights */
#define MDI_RES_VDOPLY_ERR_DRM_DURATION_USED          -3018
#define MDI_RES_VDOPLY_ERR_FRAMERATE_TOO_HIGH         -3019
#define MDI_RES_VDOPLY_ERR_STREAM_BUFFER_FAILED       -3020

 

/* vdioe telephony 3500 */
#define MDI_RES_VDOTEL_SUCCEED                        0
#define MDI_RES_VDOTEL_ERR_FAILED                    -3501

/* streaming record  3700 */
#define MDI_RES_STREAM_REC_SUCCEED                          0
#define MDI_RES_STREAM_REC_ERR_FAILED                       -3701
#define MDI_RES_STREAM_REC_ERR_DISK_FULL                    -3702
#define MDI_RES_STREAM_REC_ERR_DRM_ENC_FAILED               -3703
#define MDI_RES_STREAM_REC_ERR_DURATION_TOO_SHORT           -3704

/* SDP     3800 */
#define MDI_RES_SDP_SUCCEED                                 0
#define MDI_RES_SDP_INVALID_FORMAT                          -3801
#define MDI_RES_SDP_MEMORY_NOT_ENOUGH                       -3802
#define MDI_RES_SDP_ERR_FILE_TOO_LARGE                      -3803
#define MDI_RES_SDP_ERR_OPEN_FILE_FAILED                    -3804
#define MDI_RES_SDP_NON_PDL_FORMAT                          -3805


/* swflash 4000 */
#define MDI_RES_SWFLASH_SUCCEED                        0

#define MDI_RES_SWFLASH_ERR_FAILED                     -4001
#define MDI_RES_SWFLASH_ERR_CREATE_HCIMFILE            -4002
#define MDI_RES_SWFLASH_ERR_GET_CIM_BUFFER             -4003
#define MDI_RES_SWFLASH_ERR_SET_CIM_BUFFER             -4004
#define MDI_RES_SWFLASH_BUSY                           -4005
#define MDI_RES_SWFLASH_ERR_FILE_ERROR                 -4006
#define MDI_RES_SWFLASH_ERR_ENGINE_ERROR               -4007
#define MDI_RES_SWFLASH_ERR_NETWORK_ERROR              -4008

#define MDI_RES_SWFLASH_PARTIAL_CMP                    -4010

#define MDI_RES_SWFLASH_ERR_OPEN_FILE                  -4011
#define MDI_RES_SWFLASH_ERR_GET_FILE_SIZE              -4012
#define MDI_RES_SWFLASH_ERR_WRONG_FILE_SIZE            -4013
#define MDI_RES_SWFLASH_ERR_GET_MED_MEM                -4014
#define MDI_RES_SWFLASH_ERR_READ_FILE                  -4015
#define MDI_RES_SWFLASH_ERR_WRITE_FILE                 -4016
#define MDI_RES_SWFLASH_ERR_READ_FILE_PARTIAL          -4017
#define MDI_RES_SWFLASH_ERR_WRITE_FILE_PARTIAL         -4018

/* reserve 10 spaces for following */
#define MDI_RES_SWFLASH_ERR_CREATE_CIM                 -4020

#define MDI_RES_SWFLASH_CIM_NULL                       -4030
#define MDI_RES_SWFLASH_OPEN_FAIL                      -4031
#define MDI_RES_SWFLASH_CLOSE_FAIL                     -4032
#define MDI_RES_SWFLASH_PLAY_FAIL                      -4033
#define MDI_RES_SWFLASH_STOP_FAIL                      -4034
#define MDI_RES_SWFLASH_PAUSE_FAIL                     -4035
#define MDI_RES_SWFLASH_RESUME_FAIL                    -4036
#define MDI_RES_SWFLASH_ERR_DECIPHER	               -4037
#define MDI_RES_SWFLASH_ERR_CREATE_INSTANCE	           -4038
#define MDI_RES_SWFLASH_ERR_LOAD	                   -4039
#define MDI_RES_SWFLASH_ERR_ENGINE_FILE_PARSE	       -4040

/* barcode reader 5000 */
#define MDI_RES_BARCODEREADER_DECODE_OK               0
#define MDI_RES_BARCODEREADER_IMAGE_ERROR               -5001
#define MDI_RES_BARCODEREADER_MODULE_ERROR              -5002
#define MDI_RES_BARCODEREADER_OUT_OF_MEMORY             -5003
#define MDI_RES_BARCODEREADER_OVER_VERSION              -5004
#define MDI_RES_BARCODEREADER_DECODE_TIMEOUT            -5005
#define MDI_RES_BARCODEREADER_ERROR_CODE_TYPE                -5006


/* TVOUT 5000 */
#define MDI_RES_TV_SUCCEED                              0
#define MDI_RES_TV_ENABLE                               5001
#define MDI_RES_TV_DISABLE                              5002
#define MDI_RES_TV_NOT_READY                            -5003
#define MDI_RES_TV_FAILED                               -5004
#define MDI_RES_TV_NOT_OWNER                            -5005
#define MDI_RES_TV_ALREADY_STOPPED                      -5006

/* Mobile TV 6000 */
#define MDI_RES_MTV_COMPLETE_SAVING             6001
#define MDI_RES_MTV_UNSAVE_RECORDING_IND        6002
#define MDI_RES_MTV_SUCCEED                     0

#define MDI_RES_MTV_FAILED                      (-6001)
#define MDI_RES_MTV_ERR_DISC_FULL               (-6002)
#define MDI_RES_MTV_ERR_DEVICE_BUSY             (-6003)
#define MDI_RES_MTV_ERR_AUDIO_ERROR             (-6004)
#define MDI_RES_MTV_ERR_VIDEO_ERROR             (-6005)
#define MDI_RES_MTV_ERR_PAUSE_FAILED            (-6006)
#define MDI_RES_MTV_ERR_RECORD_FAILED           (-6007)
#define MDI_RES_MTV_ERR_RECORD_RESUME_FAILED    (-6008)
#define MDI_RES_MTV_ERR_STORAGE_NOT_READY       (-6000)
#define MDI_RES_MTV_ERR_SET_SPEED_FAILED        (-6010)
#define MDI_RES_MTV_FIND_UNSAVED_RECORDING_FAIL (-6011)
#define MDI_RES_MTV_START_PLAY_FAIL             (-6012)
#define MDI_RES_MTV_ERR_DISC_IO_ERROR           (-6013)



/* UART port 7000 */
#define MDI_RES_GPS_UART_SUCCEED                            0
#define MDI_RES_GPS_UART_READY_TO_WRITE                     1
#define MDI_RES_GPS_UART_ERR_PORT_ALREADY_OPEN              -7001
#define MDI_RES_GPS_UART_ERR_PORT_NUMBER_WRONG              -7002
#define MDI_RES_GPS_UART_ERR_PARAM_ERROR                    -7003
#define MDI_RES_GPS_UART_ERR_PORT_ALREADY_CLOSED            -7004
#define MDI_RES_GPS_UART_ERR_PORT_ERR_UNKNOW                -7005
#define MDI_RES_GPS_UART_ERR_PORT_ERR_NOT_OPEN              -7006
#define MDI_RES_GPS_UART_ERR_NO_SLOT                        -7007

/* Motion 7100 */
#define MDI_RES_MOTION_SUCCEED                              0
#define MDI_RES_MOTION_NO_SLOT                              (-7101)
#define MDI_RES_MOTION_CB_NOT_FOUND                         (-7102)

/* bitstream 8000 */
#define MDI_RES_BITSTREAM_SUCCEED                   0
#define MDI_RES_BITSTREAM_BUFFER_OVERFLOW           8001
#define MDI_RES_BITSTREAM_BUFFER_UNDERFLOW          8002
#define MDI_RES_BITSTREAM_EVENT_NONE                8003
#define MDI_RES_BITSTREAM_EVENT_DATA_REQUEST        8004
#define MDI_RES_BITSTREAM_EVENT_ERROR               8005
#define MDI_RES_BITSTREAM_RECOVER                   8006        /* mcu too busy to decode this video */
#define MDI_RES_BITSTREAM_ERR_FAILED                -8001
#define MDI_RES_BITSTREAM_ERR_INVALID_RESOULTION    -8002
#define MDI_RES_BITSTREAM_ERR_UNSUPPORTED_FORMAT    -8003
#define MDI_RES_BITSTREAM_ERR_INVALID_BITSTREAM     -8004
#define MDI_RES_BITSTREAM_ERR_MEMORY_INSUFFICIENT   -8005
#define MDI_RES_BITSTREAM_ERR_INSUFFICIENT_MEMORY   -8006  /* MED memory is not enough, should check MED memory scenario */
#define MDI_RES_BITSTREAM_ERR_INVALID_FORMAT        -8007  /* there is some error while decoding the frame, the frame may not be a valid one */
#define MDI_RES_BITSTREAM_NOT_SUPPORTED             -8008  /* Something which is not supported by Bitstream API, e.g. incorrect invoking sequence, features not available */
#define MDI_RES_BITSTREAM_INVALID_PARAMETER         -8009  /* The parameter passed through the API is invalid */

/* Audio 9000 */
#define MDI_AUDIO_SUCCESS               0

#define MDI_AUDIO_FAIL                  -9001
#define MDI_AUDIO_BUSY                  -9002
#define MDI_AUDIO_DISC_FULL             -9003
#define MDI_AUDIO_OPEN_FILE_FAIL        -9004
#define MDI_AUDIO_END_OF_FILE           -9005
#define MDI_AUDIO_TERMINATED            -9006   /* Only used in MMI */
#define MDI_AUDIO_BAD_FORMAT            -9007   /* Error from aud driver */
#define MDI_AUDIO_INVALID_FORMAT        -9008   /* Error from MED */
#define MDI_AUDIO_ERROR                 -9009
#define MDI_AUDIO_NO_DISC               -9010
#define MDI_AUDIO_NO_SPACE              -9011   /* MED audio not used */
#define MDI_AUDIO_INVALID_HANDLE        -9012   /* MMI not used */
#define MDI_AUDIO_NO_HANDLE             -9013   /* MMI not used */
#define MDI_AUDIO_RESUME                -9014   /* Only used in MMI */
#define MDI_AUDIO_BLOCKED               -9015   /* Only used in MMI */
#define MDI_AUDIO_MEM_INSUFFICIENT      -9016
#define MDI_AUDIO_BUFFER_INSUFFICIENT   -9017
#define MDI_AUDIO_FILE_EXIST            -9018   /* MMI not used */
#define MDI_AUDIO_WRITE_PROTECTION      -9019
#define MDI_AUDIO_PARAM_ERROR           -9020
#define MDI_AUDIO_UNSUPPORTED_CHANNEL   -9021
#define MDI_AUDIO_UNSUPPORTED_FREQ      -9022
#define MDI_AUDIO_UNSUPPORTED_TYPE      -9023
#define MDI_AUDIO_UNSUPPORTED_OPERATION -9024
#define MDI_AUDIO_PARSER_ERROR          -9025

#define MDI_AUDIO_AUDIO_ERROR           -9027
#define MDI_AUDIO_MP4_NO_AUDIO_TRACK    -9032
#define MDI_AUDIO_STOP_FM_RECORD        -9065   /* Only used in MMI */
#define MDI_AUDIO_UNSUPPORTED_SPEED     -9066
#define MDI_AUDIO_DECODER_NOT_SUPPORT   -9101
#define MDI_AUDIO_DEMO_END              -9116
#define MDI_AUDIO_HFP_SCO_CONNECTED     -9200   /* Only used in MMI */
#define MDI_AUDIO_DRM_PROHIBIT          -9201   /* Only used in MMI */
#define MDI_AUDIO_DRM_TIMEOUT           -9202   /* Only used in MMI */


#ifdef LCD_LAYER_ROTATE_NORMAL
#define MDI_LCD_ROTATE_0              LCD_LAYER_ROTATE_NORMAL
#define MDI_LCD_ROTATE_90             LCD_LAYER_ROTATE_90
#define MDI_LCD_ROTATE_180            LCD_LAYER_ROTATE_180
#define MDI_LCD_ROTATE_270            LCD_LAYER_ROTATE_270
#define MDI_LCD_ROTATE_0_MIRROR       LCD_LAYER_MIRROR
#define MDI_LCD_ROTATE_90_MIRROR      LCD_LAYER_MIRROR_ROTATE_90
#define MDI_LCD_ROTATE_180_MIRROR     LCD_LAYER_MIRROR_ROTATE_180
#define MDI_LCD_ROTATE_270_MIRROR     LCD_LAYER_MIRROR_ROTATE_270
#else
/* driver not defined */
#define MDI_LCD_ROTATE_0              0
#define MDI_LCD_ROTATE_90             1
#define MDI_LCD_ROTATE_180            2
#define MDI_LCD_ROTATE_270            3
#define MDI_LCD_ROTATE_0_MIRROR       4
#define MDI_LCD_ROTATE_90_MIRROR      5
#define MDI_LCD_ROTATE_180_MIRROR     6
#define MDI_LCD_ROTATE_270_MIRROR     7
#endif 

#endif /* _MDI_DATATYPE_H_ */ 

