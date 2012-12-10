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
 * Filename:
 * ---------
 * rmmi_common_enum.h
 *
 * Project:
 * --------
 *   MT6208
 *
 * Description:
 * ------------
 *   This file is intends for ...
 *
 * Author:
 * -------
 * Amanda Gau
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules ´£¤É¦Ü Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules ´£¤É¦Ü Level 2 on ALPS.ICS2.MP
 *
 * 02 19 2011 chenhao.gong
 * [MAUI_02652312] [AT]Support Speech Tuning Tool AT command
 * .
 *
 * 01 26 2011 lexel.yu
 * [MAUI_02866202] [L4] ROM/RAM size reduction
 * Integration change. 
 *
 * 12 30 2010 stan.chen
 * [MAUI_02855912] [L4C] Support 3PPP Dial-up
 * Integration change. 
 *
 * 12 15 2010 hong.yu
 * [MAUI_02825294] [Gemini+][L4C] Support for N card
 * Merge Gemini+ to Maui
 *
 * 12 15 2010 danny.kuo
 * [MAUI_02825294] [Gemini+][L4C] Support for N card
 * Integration change.
 *
 * 12 15 2010 danny.kuo
 * [MAUI_02848691] [RHR] Check-in RHR revise to trunk
 * Integration change.
 *
 * 12 01 2010 hogan.hou
 * [MAUI_02839761] [ATCI][Revise] ULC AT command set
 * disable at+etest to read autotest report and reduce to queue length to 192 (for at+cnum)
 *
 * 11 28 2010 lexel.yu
 * [MAUI_02617721] [ATCI] [Revise] General modem
 * Integration change.
 *
 * 11 26 2010 hong.yu
 * [MAUI_02842445] [OP01][L4 AT] TD command support
 * Integration change.
 *
 * 11 26 2010 chenhao.gong
 * [MAUI_02642150] [CMCC AT]CMCC proprietary AT command
 * .
 *
 * 11 23 2010 hogan.hou
 * [MAUI_02839761] [ATCI][Revise] ULC AT command set
 * ULC AT
 *
 * 10 17 2010 danny.kuo
 * [MAUI_02828429] [RHR][MAUIW1038OF_RHR] Integration to W10.43 check-in by PS5
 * Integration change.
 *
 * 10 13 2010 hong.yu
 * [MAUI_02827360] [Maui][MPM] check in MMI Protocol Modulation revise
 * .
 *
 * 10 03 2010 danny.kuo
 * [MAUI_02825239] [ATCI] Revise the hash value computing method which include the leading symbol into the hash value 1
 * 10 01 2010 hong.yu
 * [MAUI_02825164] AT revise
 * .
 *
 * 10 01 2010 hong.yu
 * [MAUI_02825164] AT revise
 * .
 *
 * 09 23 2010 danny.kuo
 * [MAUI_02617721] [ATCI] [Revise] General modem
 * Integration change.
 *
 * 09 21 2010 hong.yu
 * [MAUI_02634907] [3G TDD][TJ FT] the speech call is dis after MO CS data call
 * .
 *
 * 09 07 2010 hogan.hou
 * [MAUI_02617721] [ATCI] [Revise] General modem
 * <saved by Perforce>
 *
 * 09 04 2010 chenhao.gong
 * [MAUI_02634334] [L4]Move None used MMI-L4 interface to a separate block
 * Add back imei action
 *
 * 09 04 2010 chenhao.gong
 * [MAUI_02634334] [L4]Move None used MMI-L4 interface to a separate block
 * .
 *
 * 08 05 2010 danny.kuo
 * [MAUI_02603165] [ATCI] AT command customized enable/disable
 * .
 *
 * Jul 13 2010 mtk02508
 * [MAUI_02573625] [CMUX][L4C][TVT] Modem VT support
 * 
 *
 * Jul 8 2010 mtk02514
 * [MAUI_02581632] [ATCI] Let AT+EPBSE can be customized
 * 
 *
 * May 21 2010 mtk80396
 * [MAUI_02368605] [L4C EQ][code revise] Remove unused context
 * 
 *
 * May 4 2010 mtk02285
 * [MAUI_02368059] [L4C][ATCI][EQ] AT command via USB COM 2 support
 * 
 *
 * Mar 26 2010 mtk01616
 * [MAUI_02379358] [V32/V33] Factory AT command AT%BTTM , AT%ECALL , AT%FMR
 * 
 *
 * Mar 16 2010 mtk02285
 * [MAUI_02368059] [L4C][ATCI][EQ] AT command via USB COM 2 support
 * 
 *
 * Mar 12 2010 mtk01616
 * [MAUI_02360158] [LIPTON] AT Command input string length
 * enlarge max data queue length to support long AT command input
 *
 * Feb 11 2010 mtk02480
 * [MAUI_02356228] [DUMA] BT SIM Access Profile
 * 
 *
 * Feb 2 2010 mtk01616
 * [MAUI_02334110] [ATCI] SLIM_AT revise
 * remove RMMI VM support
 *
 * Jan 22 2010 mtk01616
 * [MAUI_02334110] [ATCI] SLIM_AT revise
 * 
 *
 * Dec 10 2009 mtk02088
 * [MAUI_01923990] [AT] +CMGD, the return value seems abnormal
 * AT+CMGD=? route to MMI using MSG_ID_MMI_SMS_ATCMD_EQSI_IND to query supporting range of index. MMI need handle mem=MT
 *
 * Nov 27 2009 mtk02508
 * [MAUI_01790248] [3G-Gemini][AT COMMAND] ath can't return OK after disc the data call
 * route data call to UCM; add RMMI_UCM_ATD_DATA
 *
 * Nov 27 2009 mtk02514
 * [MAUI_02001420] [AT] revise AT+CPBW
 * 
 *
 * Nov 24 2009 mtk01616
 * [MAUI_01975514] [ATCI][Revise] Reduce code size in FAX AT command hdlr
 * 
 *
 * Oct 29 2009 mtk02088
 * [MAUI_01667870] [Wise] Check in code for wise development
 * 
 *
 * Sep 29 2009 mtk02514
 * [MAUI_01962388] [AT] add a new command AT+EPBSE to set preferred band
 * 
 *
 * Sep 29 2009 mtk02514
 * [MAUI_01962351] [AT] add a new URC +CIEV:7 , 2 to indicate the state of SIM memory exceed when there
 * 
 *
 * Aug 6 2009 mtk02514
 * [MAUI_01929083] [ATCI] AT Extended Customer Command table
 * 
 *
 * Jun 12 2009 mtk02514
 * [MAUI_01732595] [AT COMMAND]ATD dail a data call not OK return
 * 
 *
 * Apr 15 2009 MTK02088
 * [MAUI_01668995] [WISE] sms
 * 
 *
 * Mar 14 2009 mtk02480
 * [MAUI_01645841] [DUMA] WinMo Engineer Mode Support
 * 
 *
 * Feb 25 2009 mtk01616
 * [MAUI_01633933] [DUMA] Add ESPEECH URC to notify AP to turn on/off the speech
 * 
 *
 * Feb 18 2009 mtk02514
 * [MAUI_01625312] [ATCI] general AT command interface
 * modify ATCI I/O buffer size
 * 
 *
 * Feb 17 2009 mtk02514
 * [MAUI_01631683] [AT ]Add a new feature: PHB storage LA which is a merged storage of  DC, RC, MC for
 * 
 *
 * Jan 20 2009 mtk01616
 * [MAUI_01546142] [Monza2G Libra35]  ATD>SM making Voice call whether semi-colon is used or not
 * 
 *
 * Jan 6 2009 mtk01616
 * [MAUI_01305242] [ATCI][Revise] Proprietary URC framework
 * 
 *
 * Dec 30 2008 mtk01616
 * [MAUI_01305242] [ATCI][Revise] Proprietary URC framework
 * 
 *
 * Nov 4 2008 mtk01497
 * [MAUI_01264994] [WM] Implement AT command related to SIM-ME lock
 * Implement SIM-ME lock feature for AT+CLCK
 *
 * Oct 30 2008 mtk01612
 * [MAUI_01264994] [WM] Implement AT command related to SIM-ME lock
 * Add AT+ESMLCK, AT+ESMLA for SIM-ME lock on WM.
 *
 * Oct 30 2008 mtk01497
 * [MAUI_01264994] [WM] Implement AT command related to SIM-ME lock
 * 
 *
 * Oct 13 2008 mtk02285
 * [MAUI_01253584] [ATCI][CMUX][L4C] Gemini modem interface
 * 
 *
 * Oct 13 2008 mtk02285
 * [MAUI_01253176] [L4C][CSM][UEM] Revise the compile option for WM-related telephony behaviors
 * 
 *
 * Oct 10 2008 mtk02285
 * [MAUI_01253232] [ATCI][CMUX][UART] cmux virtual port number revise
 * 
 *
 * Sep 3 2008 MTK02088
 * [MAUI_01090595] [Phone suite][L4C][SMSAL] add AT+EQSI to make sure the index definition
 * Add command +EQSI
 *
 * Jul 30 2008 mtk01616
 * [MAUI_00810772] [L4C] Enlarge multiple command length
 * 
 *
 * Jul 29 2008 mtk01616
 * [MAUI_00812258] [CMUX][Revise] Independent port configuration
 * 
 *
 * Jul 18 2008 mtk02285
 * [MAUI_00800688] [L4C] Reformat L4C code
 * 
 *
 * Jul 9 2008 mtk01616
 * [MAUI_00792277] [WM] CRING and CCWA for Line 2 MT call
 * 
 *
 * Jun 8 2008 mtk01616
 * [MAUI_01036678] [PBAP]Phone dial out the last number when enter Dialed log on client.
 * 
 *
 * Apr 18 2008 mtk01497
 * [MAUI_00756768] [ATCI] Create MOD_ATCI to handle AT related functions
 * 
 *
 * Apr 7 2008 mtk00924
 * [MAUI_00741069] [WM] replace __WINDOWS_MOBILE__ and __WINCE__ with __SMART_PHONE_MODEM__
 * 
 *
 * Feb 24 2008 mtk00924
 * [MAUI_00609757] [WM] check in Window Mobile modification
 * 
 *
 * Feb 4 2008 mtk00924
 * [MAUI_00616170] [GEMINI][RAC][AT][MMI] Support 2 IMEI
 * 
 *
 * Jan 24 2008 mtk01616
 * [MAUI_00610943] [L4C] merge Gemini code to mainturnk
 * 
 *
 * Jan 24 2008 mtk00924
 * [MAUI_00582286] [GEMINI][L4] modification for GEMINI
 * 
 *
 * Sep 12 2007 mtk00924
 * [MAUI_00525370] [MT6601 BQB DUN] TP/APS/BV-02-I - NT is busy
 * add BUSY URC
 *
 * Sep 5 2007 mtk01616
 * [MAUI_00542189] [1]assert fail:mmi_ucm_get_held_group(MMI_UCM_VOICE_CALL_TPYE,0,g_ucm_p->call_misc.i
 * 
 *
 * Aug 12 2007 mtk01616
 * [MAUI_00533530] [L4C][New feature]  AT for UCM
 * 
 *
 * Jul 22 2007 mtk00924
 * [MAUI_00419444] [BCHS][Removal] Remove BCHS related codes
 * 
 *
 * Jun 18 2007 mtk00758
 * [MAUI_00406640] [UPF27][BT] pass AT+CIND? to L4 while receiving this command from HFP
 * 
 *
 * Jun 4 2007 mtk00924
 * [MAUI_00400601] [L4C][Update] check in for EDGE load
 * 
 *
 * Apr 23 2007 mtk00924
 * [MAUI_00384849] [Bluetooth SPP Client] SPP Client and number of virtual ports modification
 * extend L4C source id for BT SPP client
 *
 * Apr 2 2007 mtk00924
 * [MAUI_00372309] ï¿½Ð¦bATï¿½Ò¶ï¿½ï¿½ï¿½ï¿½{ï¿½pï¿½Uï¿½\ï¿½ï¿½
 * modify +EMMISTR to write MMI data to UART transparently
 *
 * Nov 24 2006 mtk00924
 * [MAUI_00226950] ERROR returns when use ath command
 * 
 *
 * Oct 23 2006 MTK00758
 * [MAUI_00337656] [BT]VoIP over BT
 * 
 *
 * Sep 30 2006 mtk00924
 * [MAUI_00333902] [AT][update]Add trace information for RMMI
 * 
 *
 * Jul 14 2006 MTK00758
 * [MAUI_00210638] [BT]IT check in
 * 
 *
 * May 12 2006 mtk00924
 * [MAUI_00186129] [L4C]remove compile warning
 * remove compile warning
 *
 * Apr 24 2006 mtk00924
 * [MAUI_00189516] [UEM] UEM revise
 * AT modification for UEM revise
 *
 * Mar 25 2006 mtk00924
 * [MAUI_00220171] at+cmee=2 the result code should display memory full
 * Modifying error code of +cmee
 *
 * Mar 20 2006 mtk00924
 * [MAUI_00220003] [AT]  +cmgl show some characters abnormal
 * AT should handle extension characters
 *
 * Mar 11 2006 mtk00758
 * [MAUI_00178684] [Bluetooth]ESI bluetooth protocol stack
 * 
 *
 * Nov 28 2005 mtk00924
 * [MAUI_00158333] [AT][NewFeature]DT task for file transfer
 * DT task for file transfer
 *
 * Nov 17 2005 mtk00758
 * [MAUI_00153701] [L4C][Bluetooth]integration
 * 
 *
 * Nov 13 2005 mtk00924
 * [MAUI_00155719] [AT][Update] modifying +CSCS to support UCS2 0x81
 * Modifying +CSCS to support UCS2 0x81. 
 *
 * Nov 13 2005 mtk00758
 * [MAUI_00153701] [L4C][Bluetooth]integration
 * 
 *
 * Oct 31 2005 mtk00758
 * [MAUI_00153701] [L4C][Bluetooth]integration
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by Sasken Communication Technologies Limited.
*
********************************************************************************/

#ifndef _RMMI_COMMON_ENUM_H
#define _RMMI_COMMON_ENUM_H
#ifdef __CMUX_SUPPORT__
#include "cmux_vp_num.h"
#endif

//#include "csmss_common_enums.h"
//#include "smu_common_enums.h"
//#include "ps_public_enum.h"

//For feature phone project which need long AT command support 
#if defined (__LONG_AT_CMD_SUPPORT__) 
#define MAX_DATA_QUEUE_LENGTH       2048  
#elif defined (__ULC_AT__)
#define MAX_DATA_QUEUE_LENGTH       192 
#elif defined (__SLIM_AT__)
#define MAX_DATA_QUEUE_LENGTH       350
#elif defined (__GATI_ENABLE__)
#define MAX_DATA_QUEUE_LENGTH	1500
#elif defined (__MMI_FMI__)
#define MAX_DATA_QUEUE_LENGTH       512
#else 
#define MAX_DATA_QUEUE_LENGTH       2048  
#endif 

#if defined(__DISABLE_SIM2_AT_SUPPORT__)
#define RMMI_MAX_SIM_NUM      (1)
#else
#define RMMI_MAX_SIM_NUM      (L4_MAX_SIM_NUM)
#endif

//mtk00924: output queue
//#define MAX_DATA_OUTPUT_QUEUE_LENGTH       2048 

#define MAX_UART_LENGTH             128

#define RMMI_MAX_EXT_CMD_NAME_LEN   10

#define RMMI_MAX_ERR_STR_LEN        80
//#define RMMI_MAX_ERROR_NUM          256
#define RMMI_MAX_ERROR_NUM          160

#define RMMI_MAX_ARG_NUM            16

#define RMMI_VALIDATOR_ERROR        255
#define RMMI_DEF_VALUE              0

#define RMMI_SHORT_RSP_LEN          80

#define RMMI_MAX_MELODY_LEN         64

#define RMMI_EXTEND_SYMBOL_HASH_BASE 79235168           // 38^5

#if defined(__OP01__)
#define CMCC_AT_CMD_MAIN_VER   2
#define CMCC_AT_CMD_MAJOR_VER  0
#define CMCC_AT_CMD_MINOR_VER  0
#endif

enum
{
        RMMI_FIRST_CHANNEL,

    #ifdef __CMUX_SUPPORT__
        RMMI_CMUX_CHANNELS_START = RMMI_FIRST_CHANNEL,
        RMMI_CMUX_CHANNELS_END = (RMMI_CMUX_CHANNELS_START + CMUX_VP_NUM_FOR_SINGLE_SIM - 1),
    #else
    #ifdef __BTMTK__
        RMMI_BT_CHANNELS_START,
        RMMI_BT_CHANNELS_END = RMMI_BT_CHANNELS_START + VIRTUAL_PORTS_NUM - 1,
    #endif /*__BTMTK__*/
    #endif /*__CMUX_SUPPORT__*/
    
    #ifdef __USB_MULTIPLE_COMPORT_SUPPORT__
   		RMMI_USB2_CHANNEL,
   	#endif
    
        RMMI_MAX_CHANNEL_NUMBER
};

#if (RMMI_MAX_CHANNEL_NUMBER > 9) //from RMMI_SRC to RMMI_9 and exclude RMMI_SAT
#error rmmi source number is less than UART port number!!
#endif /* __USB_MULTIPLE_COMPORT_SUPPORT__, mtk02285_usb2 */

enum
{
#if defined(__LONG_MULTIPLE_CMD_SUPPORT__)
    MAX_MULTIPLE_CMD_INFO_LEN = 350,
#elif defined(__SLIM_AT__) 
    /* mtk01616_100112: Only support multiple basic cmd(ex:ATL will use),no multiple extended cmd. 20 shall be enough */
    MAX_MULTIPLE_CMD_INFO_LEN = 20,
#else 
    MAX_MULTIPLE_CMD_INFO_LEN = 40,
#endif 
#ifdef __GATI_ENABLE__
    MAX_SINGLE_CMD_INFO_LEN = 1500,
#else
    MAX_SINGLE_CMD_INFO_LEN = 700,
#endif
    MAX_PRE_ALLOCATED_BASIC_CMD_STRUCT_NODES = 3
};

typedef enum
{
    RMMI_NONE_PRESENT = 0,
    RMMI_EXTENDED_COMMAND_PRESENT,
    RMMI_BASIC_COMMAND_PRESENT,
    RMMI_WRONG_PREV_COMMAND,
    RMMI_EXECUTION_COMMAND_PRESENT
} rmmi_cmd_present_enum;

typedef enum
{
    RMMI_DISABLE_REPORT = 0,
    RMMI_ENABLE_REPORT
} rmmi_report_mode_enum;

typedef enum
{
    RMMI_DISABLE_NW_REG_REPORT,
    RMMI_ENABLE_NW_REG_REPORT,
    RMMI_ENABLE_LOC_REPORT
} rmmi_nw_reg_report_mode_enum;

typedef enum
{
    RMMI_ECHO_OFF,
    RMMI_ECHO_ON
} rmmi_cmd_echo_enum;

typedef enum
{
    RMMI_SUPPRESS_OFF,
    RMMI_SUPRESS_ON
} rmmi_result_code_suppress_enum;

typedef enum
{
    RMMI_NUM_PARTIAL_HEAD_TAIL,
    RMMI_VERBOSE_FULL_HEAD_TAIL
} rmmi_result_code_format;

typedef enum
{
    RMMI_SIMPLE_ERROR,
    RMMI_NUMERIC_ERROR,
    RMMI_TEXT_ERROR
} rmmi_error_report_mode_enum;

typedef enum
{
    RMMI_INVALID_CMD_TYPE = 0,
    RMMI_PREV_CMD,
    RMMI_BASIC_CMD,
    RMMI_EXTENDED_CMD,
    RMMI_EXTENDED_CUSTOM_CMD,  // __RMMI_EXTEND_CUSTOM_CMD__
    RMMI_CUSTOMER_CMD
} rmmi_cmd_type_enum;



typedef enum
{
    RMMI_EXTEND_HASH_PLUS = RMMI_EXTEND_SYMBOL_HASH_BASE*0,             // + : 0*38^5
    RMMI_EXTEND_HASH_HAT = RMMI_EXTEND_SYMBOL_HASH_BASE*1,              // ^ : 1*38^5
    RMMI_EXTEND_HASH_PERCENT = RMMI_EXTEND_SYMBOL_HASH_BASE*2,       // % : 2*38^5
    RMMI_EXTEND_HASH_MONEY = RMMI_EXTEND_SYMBOL_HASH_BASE*3          // $ : 3*38^5
} rmmi_extend_symbol_hash_enum;

typedef enum
{
    RMMI_WRONG_MODE,
    RMMI_SET_OR_EXECUTE_MODE,
    RMMI_READ_MODE,
    RMMI_TEST_MODE,
    RMMI_ACTIVE_MODE
} rmmi_cmd_mode_enum;

typedef enum
{
    RMMI_CHSET_IRA,
    RMMI_CHSET_HEX,
    RMMI_CHSET_GSM,
    RMMI_CHSET_PCCP437,
    RMMI_CHSET_PCDN,
    RMMI_CHSET_88591,
    RMMI_CHSET_UCS2,
#ifdef __PHB_0x81_SUPPORT__
    RMMI_CHSET_UCS2_0X81,
#endif 
    RMMI_CHSET_TOTAL_NUM
} rmmi_chset_enum;

typedef enum
{
    RMMI_PLMN_LONG_ALPHA,
    RMMI_PLMN_SHORT_ALPHA,
    RMMI_PLMN_NUMERIC
} rmmi_plmn_format_enum;


typedef enum
{
    RMMI_RCODE_OK,
    RMMI_RCODE_CONNECT,
    RMMI_RCODE_RING,
    RMMI_RCODE_NO_CARRIER,
    RMMI_RCODE_ERROR,
    RMMI_RCODE_BUSY = 7
} rmmi_rsp_type_enum;

typedef enum
{
    RMMI_SPACE = ' ',
    RMMI_EQUAL = '=',
    RMMI_COMMA = ',',
    RMMI_SEMICOLON = ';',
    RMMI_COLON = ':',
    RMMI_AT = '@',
    RMMI_HAT = '^',
    RMMI_DOUBLE_QUOTE = '"',
    RMMI_QUESTION_MARK = '?',
    RMMI_EXCLAMATION_MARK = '!',
    RMMI_FORWARD_SLASH = '/',
    RMMI_L_ANGLE_BRACKET = '<',
    RMMI_R_ANGLE_BRACKET = '>',
    RMMI_L_SQ_BRACKET = '[',
    RMMI_R_SQ_BRACKET = ']',
    RMMI_L_CURLY_BRACKET = '{',
    RMMI_R_CURLY_BRACKET = '}',
    RMMI_CHAR_STAR = '*',
    RMMI_CHAR_POUND = '#',
    RMMI_CHAR_AMPSAND = '&',
    RMMI_CHAR_PERCENT = '%',
    RMMI_CHAR_PLUS = '+',
    RMMI_CHAR_MINUS = '-',
    RMMI_CHAR_DOT = '.',
    RMMI_CHAR_ULINE = '_',
    RMMI_CHAR_TILDE = '~',
    RMMI_CHAR_REVERSE_SOLIDUS = '\\',
    RMMI_CHAR_VERTICAL_LINE = '|',
    RMMI_END_OF_STRING_CHAR = '\0',
    RMMI_CHAR_0 = '0',
    RMMI_CHAR_1 = '1',
    RMMI_CHAR_2 = '2',
    RMMI_CHAR_3 = '3',
    RMMI_CHAR_4 = '4',
    RMMI_CHAR_5 = '5',
    RMMI_CHAR_6 = '6',
    RMMI_CHAR_7 = '7',
    RMMI_CHAR_8 = '8',
    RMMI_CHAR_9 = '9',
    RMMI_CHAR_A = 'A',
    RMMI_CHAR_B = 'B',
    RMMI_CHAR_C = 'C',
    RMMI_CHAR_D = 'D',
    RMMI_CHAR_E = 'E',
    RMMI_CHAR_F = 'F',
    RMMI_CHAR_G = 'G',
    RMMI_CHAR_H = 'H',
    RMMI_CHAR_I = 'I',
    RMMI_CHAR_J = 'J',
    RMMI_CHAR_K = 'K',
    RMMI_CHAR_L = 'L',
    RMMI_CHAR_M = 'M',
    RMMI_CHAR_N = 'N',
    RMMI_CHAR_O = 'O',
    RMMI_CHAR_P = 'P',
    RMMI_CHAR_Q = 'Q',
    RMMI_CHAR_R = 'R',
    RMMI_CHAR_S = 'S',
    RMMI_CHAR_T = 'T',
    RMMI_CHAR_U = 'U',
    RMMI_CHAR_V = 'V',
    RMMI_CHAR_W = 'W',
    RMMI_CHAR_X = 'X',
    RMMI_CHAR_Y = 'Y',
    RMMI_CHAR_Z = 'Z',
    rmmi_char_a = 'a',
    rmmi_char_b = 'b',
    rmmi_char_c = 'c',
    rmmi_char_d = 'd',
    rmmi_char_e = 'e',
    rmmi_char_f = 'f',
    rmmi_char_g = 'g',
    rmmi_char_h = 'h',
    rmmi_char_i = 'i',
    rmmi_char_j = 'j',
    rmmi_char_k = 'k',
    rmmi_char_l = 'l',
    rmmi_char_m = 'm',
    rmmi_char_n = 'n',
    rmmi_char_o = 'o',
    rmmi_char_p = 'p',
    rmmi_char_q = 'q',
    rmmi_char_r = 'r',
    rmmi_char_s = 's',
    rmmi_char_t = 't',
    rmmi_char_u = 'u',
    rmmi_char_v = 'v',
    rmmi_char_w = 'w',
    rmmi_char_x = 'x',
    rmmi_char_y = 'y',
    rmmi_char_z = 'z',
    RMMI_R_BRACKET = ')',  
    RMMI_L_BRACKET = '(', 
    RMMI_MONEY = '$'
} rmmi_char_enum;

typedef enum
{
    /* 07.07 Sec 9.2.1 */
    RMMI_ERR_PHONE_FAILURE,
    OPERATION_NOT_ALLOWED_ERR = 3,
    RMMI_ERR_OPERATION_NOT_SUPPORTED = 4,
    PH_SIM_PIN_REQUIRED = 5,
   PH_FSIM_PIN_REQUIRED = 6, //Kinki: SIM-ME lock [MAUI_01264994]
   PH_FSIM_PUK_REQUIRED = 7, //Kinki: SIM-ME lock [MAUI_01264994]
    SIM_NOT_INSERTED = 10,
    SIM_PIN_REQUIRED = 11,
    SIM_PUK_REQUIRED = 12,
    SIM_FAILURE = 13,
    SIM_BUSY = 14,
    SIM_WRONG = 15,
    INCORRECT_PASSWD = 16,
    SIM_PIN2_REQUIRED = 17,
    SIM_PUK2_REQUIRED = 18,
    RMMI_ERR_MEM_FULL = 20,
    RMMI_ERR_INVALID_INDEX = 21,
    RMMI_ERR_NO_FOUND = 22,
    TEXT_ERRSTRING_TOO_LONG_ERR = 24,
    INVALID_CHARACTERS_IN_TEXT_ERRSTRING_ERR = 25,
    DIAL_ERRSTRING_TOO_LONG_ERR = 26, 
    INVALID_CHARACTERS_IN_DIAL_ERRSTRING_ERR = 27,
    NO_NW_SERVICE = 30,
    RMMI_ERR_NETWORK_TIMEOUT = 31,
    RMMI_ERR_NETWORK_NOT_ALLOWED = 32,
    RMMI_ERR_NW_PERSON_PIN_REQUIRED = 40,
    RMMI_ERR_NW_PERSON_PUK_REQUIRED = 41,
    RMMI_ERR_NW_SUB_PERSON_PIN_REQUIRED = 42,
    RMMI_ERR_NW_SUB_PERSON_PUK_REQUIRED = 43,
    RMMI_ERR_SP_PERSON_PIN_REQUIRED = 44,
    RMMI_ERR_SP_PERSON_PUK_REQUIRED = 45,
    RMMI_ERR_CORP_PERSON_PIN_REQUIRED = 46,
    RMMI_ERR_CORP_PERSON_PUK_REQUIRED = 47,
    RMMI_ERR_UNKNOWN = 100,

    RMMI_ERR_ILLEGAL_MS = 103,
    RMMI_ERR_ILLEGAL_ME = 106,
    RMMI_ERR_GPRS_NOT_ALLOWED = 107,
    RMMI_ERR_PLMN_NOT_ALLOWED = 111,
    RMMI_ERR_LA_NOT_ALLOWED = 112,
    RMMI_ERR_ROAMING_AREA_NOT_ALLOWED = 113,
    RMMI_ERR_SERV_OPTION_NOT_SUPPORTED = 132,
    RMMI_ERR_REQ_SERV_OPTION_NOT_SUBSCRIBED = 133,
    RMMI_ERR_SERV_OPTION_TEMP_OUT_OF_ORDER = 134,
    RMMI_ERR_GPRS_UNSPECIFIED_ERROR = 148,
    RMMI_ERR_PDP_AUTH_FAIL = 149,
    RMMI_ERR_INVALID_MOBILE_CLASS = 150,

   RMMI_ERR_LINK_NS_SP_PERSON_PIN_REQUIRED = 151, //Kinki: SIM-ME lock [MAUI_01264994]
   RMMI_ERR_LINK_NS_SP_PERSON_PUK_REQUIRED = 152, //Kinki: SIM-ME lock [MAUI_01264994]
   RMMI_ERR_LINK_SIM_C_PERSON_PIN_REQUIRED = 153, //Kinki: SIM-ME lock [MAUI_01264994]
   RMMI_ERR_LINK_SIM_C_PERSON_PUK_REQUIRED = 154, //Kinki: SIM-ME lock [MAUI_01264994]

    /* following are proprietary error cause : the cause below WON'T be showed as +CME ERROR. */
    RMMI_ERR_COMMAND_CONFLICT = 302,    //same as operation not allowed in 07.05 Sec 3.2.5
    // will be convert to +CME ERROR: 3  or +CMS ERROR: 302
    RMMI_NO_ERR = 600,

    /* following: error is related to syntax, invalid parameters.. */
    /* according to spec 07.07 Sec 9.1, only "ERROR" will be returned to TE side */
    RMMI_ERR_UNRECOGNIZED_CMD = 601,
    RMMI_ERR_RETURN_ERROR = 602,
    RMMI_ERR_SYNTEX_ERROR = 603,
    RMMI_ERR_UNSPECIFIED = 604, //unspecified parsing error
    RMMI_ERR_DATA_TRANSFER_ALREADY = 605,
    RMMI_ERR_ACTION_ALREADY = 606,
    RMMI_ERR_NOT_AT_CMD = 607,
    RMMI_ERR_MULTI_CMD_TOO_LONG = 608,
    RMMI_ERR_ABORT_COPS = 609,
    RMMI_ERR_NO_CALL_DISC = 610,
    RMMI_ERR_BT_SAP_UNDEFINED = 611,
    RMMI_ERR_BT_SAP_NOT_ACCESSIBLE = 612,
    RMMI_ERR_BT_SAP_CARD_REMOVED = 613,

    RMMI_ERR_AT_NOT_ALLOWED_BY_CUSTOMER = 614
} rmmi_err_id_enum;


#ifndef __AGPS_SWIP_REL__ //Add by Baochu to fix building warning

typedef enum
{
    FAC_NOT_SUPPORTED,
    SS_FAC_BEGIN = L4_BAOC,
    SS_FAC_END = L4_BIC,
#ifdef __MOD_SMU__
    SIM_FAC_BEGIN = TYPE_CHV1,  /* need to add sim sec type */
    SIM_FAC_END = TYPE_IMSI_LOCK
#endif /* __MOD_SMU__ */ 
} rmmi_clck_fac_enum;

#endif //#ifndef __AGPS_SWIP_REL__


typedef enum
{
    RMMI_PLAY = 1,
    RMMI_STOP,
    RMMI_PAUSE,
    RMMI_RESUME
} rmmi_audio_mode_enum;

typedef enum
{
    CCBS_Interrotage = 14,
    CCBS_EarseCCEntry = 77
} rmmi_ccbs_opcode_enum;

typedef enum
{
    RMMI_FS_OPEN,
    RMMI_FS_CLOSE,
    RMMI_FS_READ,
    RMMI_FS_WRITE,
    RMMI_FS_DELETE,
    RMMI_FS_DIR,
    RMMI_FS_CREATEDIR,
    RMMI_FS_DELETEDIR,
    RMMI_FS_RENAME
} rmmi_fs_opcode_enum;

typedef enum
{
    CSSI_CFU_ACTIVE,
    CSSI_CFC_ACTIVE,
    CSSI_CALL_FORWARDED,
    CSSI_CALL_WAITING,
    CSSI_CUG_CALL,
    CSSI_OUTGOING_BARRED,
    CSSI_INCOMING_BARRED,
    CSSI_CLIR_REJECTED,
    CSSI_CALL_DEFLECTED
} rmmi_cssi_enum;       //refer to 07.07 +CSSN

typedef enum
{
    CSSU_FORWARDED_CALL,
    CSSU_CUG_CALL,
    CSSU_CALL_HOLD,
    CSSU_CALL_RETRIEVED,
    CSSU_MPTY_CALL,
    CSSU_HOLDCALL_RELEASED,
    CSSU_FORWARD_CHECK_SS,
    CSSU_ECT_ALERTING,
    CSSU_ECT_ACTIVE,
    CSSU_DEFLECTED_CALL,
    CSSU_INCOMING_FORWARDED
} rmmi_cssu_enum;       //refer to 07.07 +CSSN

/* mtk00714 add on 2004/03/02 */
/* first 10 items (except GPRS_REQ) 
   should be consistant with "l4ccsm_cc_call_mode_enum" */
typedef enum
{
    RMMI_CRING_VOICE,
    RMMI_CRING_DATA,
    RMMI_CRING_FAX,
    RMMI_CRING_VOICE_DATA,
    RMMI_CRING_ALT_VOICE_DATA,
    RMMI_CRING_ALT_VOICE_FAX,
    RMMI_CRING_DATA_VOICE,
    RMMI_CRING_ALT_DATA_VOICE,
    RMMI_CRING_ALT_FAX_VOICE,
    RMMI_CRING_UNKNOWN_TYPE,
    RMMI_CRING_GPRS,
    RMMI_CRING_VOICE_AUX,
    RMMI_CRING_VIDEO
} rmmi_cring_type_enum;

typedef enum
{
    RMMI_DCS_DEFAULT = 0x00,    /* GSM 7-bit */
    RMMI_DCS_8BIT = 0x04,       /* 8-bit */
    RMMI_DCS_UCS2 = 0x08        /* UCS2 */
}
rmmi_dcs_enum;

typedef enum
{
    RMMI_PARSE_OK,
    RMMI_PARSE_ERROR,   //out of range
    RMMI_PARSE_NOT_FOUND,
    RMMI_PARSE_TEXT_TOO_LONG
} rmmi_validator_cause_enum;

typedef enum
{
    RMMI_PHB_NONE,
    RMMI_PHB_SM,
    RMMI_PHB_ME,
    RMMI_PHB_FD,
    RMMI_PHB_LD,
    RMMI_PHB_MC,
    RMMI_PHB_RC,
    RMMI_PHB_MT,
    RMMI_PHB_EN,
    RMMI_PHB_ON,
    RMMI_PHB_DC,
    /* mtk02514 ** MAUI_01321633  ** 2009/02/16 **********
    * for the storage containing LD(DC), MC, RC                                     //mtk02514_la
    * We only support AT+CPBS, AT+CPBR for the RMMI_PHB_LA
    ************************************************/
    RMMI_PHB_LA        
} rmmi_phb_type_enum;

typedef enum
{
    RMMI_EIND_SMS_READY_BIT,
    RMMI_EIND_PHB_READY_BIT = 1,
    RMMI_EIND_PLMN_CHANGED_BIT = 2,
    RMMI_EIND_EONS_CHANGED_BIT = 3,    
    RMMI_EIND_AT_READY_BIT = 7
} rmmi_eind_bit_enum;

typedef enum
{
    RMMI_ESMLA_BIT,        /* bit 1 is for +ESMLA */
    RMMI_ECFU_BIT,         /* bit 2 is for +ECFU */
    RMMI_ECELLINFO_BIT,	   /* bit 3 is for +ECELLINFO */   
    RMMI_ENWINFO_BIT,      /* bit 4 is for +ENWINFO */      
    RMMI_ESPEECH_BIT,       /* bit 5 is for +ESPEECH */
    RMMI_EUSIM_BIT       /* bit 6 is for +EUSIM */
} rmmi_einfo_bit_enum;

typedef enum
{
    RMMI_IMEI_NO_ACTION = 0,
    RMMI_IMEI_READ,
    RMMI_IMEI_WRITE,
    RMMI_IMEI_2_WRITE,
    RMMI_IMEI_3_WRITE,
    RMMI_IMEI_4_WRITE
} rmmi_imei_action_enum;

/* battchg    1 */
/* signal     2 */
/* service    3 */
/* message    4 */
/* call       5 */
/* roam       6 */
/* smsfull    7 */
/* call_setup 8 */

#if defined(__BTMTK__)
//defined later
typedef enum
{
    RMMI_CIND_BEGIN = 0,
    RMMI_CIND_SERVICE,
    RMMI_CIND_CALL,
    RMMI_CIND_CALLSETUP,
    RMMI_CIND_CALLHELD,
    RMMI_CIND_BATTCHG,
    RMMI_CIND_SIGNAL,
    RMMI_CIND_ROAM,
    RMMI_CIND_END
} rmmi_cind_enum;
#else /* defined(__BTMTK__) */ 
typedef enum
{
    RMMI_CIND_BEGIN = 0,
    RMMI_CIND_BATTCHG,
    RMMI_CIND_SIGNAL,
    RMMI_CIND_SERVICE,
    RMMI_CIND_MESSAGE,
    RMMI_CIND_CALL,
    RMMI_CIND_ROAM,
    RMMI_CIND_SMSMEMSTATUS,
    RMMI_CIND_CALLSETUP,
    RMMI_CIND_END
} rmmi_cind_enum;
#endif /* defined(__BTMTK__) */ 

typedef enum
{
    RMMI_CIND_CSUP_NONE = 0,
    RMMI_CIND_CSUP_INCOMING,
    RMMI_CIND_CSUP_OUTGOING,
    RMMI_CIND_CSUP_ALERT,
    RMMI_CIND_CSU_PEND
} rmmi_cind_callsetup;

typedef enum
{
    RMMI_CIND_CC_NONE = 0,
    RMMI_CIND_CC_ACTIVE = 1
} rmmi_cind_call;

typedef enum
{
    RMMI_CIND_HELD_NONE = 0,
    RMMI_CIND_HELD_ACTIVE_HOLD = 1,
    RMMI_CIND_HELD_HOLD = 2
} rmmi_cind_callheld;

/*
 * typedef enum
 * {
 * RMMI_CCSR_IDLE,
 * RMMI_CCSR_CALLING,
 * RMMI_CCSR_CONNECTING,
 * RMMI_CCSR_ACTIVE,
 * RMMI_CCSR_HOLD,
 * RMMI_CCSR_WAITING,
 * RMMI_CCSR_ALERTING,
 * RMMI_CCSR_BUSY
 * 
 * } rmmi_ccsr_rsp_code_enum;
 * 
 */

typedef enum
{
    RMMI_EADP_NO_ACTION = 0,
    RMMI_EADP_GET,
    RMMI_EADP_SET
} rmmi_eadp_action_enum;

typedef enum
{
    RMMI_EAPS_NO_ACION = 0,
    RMMI_EAPS_GET,
    RMMI_EAPS_SET
} rmmi_eaps_action_enum;

/* For VoIP, call management is controlled by MMI */
typedef enum
{
    RMMI_CM_ATD = 0,
    RMMI_CM_ATH,
    RMMI_CM_ATA,
    RMMI_CM_CHLD,
    RMMI_CM_BLDN
} rmmi_cm_action_enum;

/* for +EMMISTR proprietary command */
typedef enum
{
    RMMI_EMMISTR_DISABLE = 0,
    RMMI_EMMISTR_ENABLE,
    RMMI_EMMISTR_DATA_FROM_MMI
} rmmi_emmistr_action_enum;

/* for +EQSI proprietary command */
typedef enum
{
    RMMI_SMSAL_SM = 0,
    RMMI_SMSAL_ME = 1,
    RMMI_SMSAL_MT = 2 
} rmmi_smsal_storage_enum;

#if defined(__GEMINI__) && !defined(__MMI_FMI__)
typedef enum 
{
    RMMI_EDSIM_READ_FOR_WRITING_0 = 0, // corresponds to +edsim=0
    RMMI_EDSIM_READ_FOR_WRITING_1, // corresponds to +edsim=1
    RMMI_EDSIM_READ_FOR_WRITING_2, // corresponds to +edsim=2
    RMMI_EDSIM_READ_FOR_WRITING_3, // corresponds to +edsim=3
    RMMI_EDSIM_NORMAL=0xAA,
    RMMI_EDSIM_WRITING=0xEE,
    RMMI_EDSIM_READING=0xFF
} rmmi_edsim_set_mode_state_enum;
#endif /* defined(__GEMINI__) && !defined(__MMI_FMI__) */

typedef enum
{
    RMMI_SML_NP_CATEGORY,
    RMMI_SML_NSP_CATEGORY,
    RMMI_SML_SP_CATEGORY,
    RMMI_SML_CP_CATEGORY,
    RMMI_SML_SIM_CATEGORY
} rmmi_sml_catagory_enum;

/* mtk01616_090116: for atd> memory dial to distinguish call_type */
typedef enum
{
    RMMI_MEM_DIAL_NONE = 0,
    RMMI_MEM_DIAL_VOICE = 1,
    RMMI_MEM_DIAL_DATA = 2    
} rmmi_mem_dial_enum;

//mtk01616_091024: ATCI internal used enum,intended for rmmi_fax_support_check()
typedef enum
{
    RMMI_SERV_CLASS_0   = 0x01,
    RMMI_SERV_CLASS_1   = 0x02,
    RMMI_SERV_CLASS_1_0 = 0x04,
    RMMI_SERV_CLASS_2   = 0x08,
    RMMI_SERV_CLASS_2_0 = 0x10
} rmmi_fax_check_type_enum;

typedef enum
{
  	RMMI_USM_ATCMD_CGSMS,
	RMMI_USM_ATCMD_CSCA,
	RMMI_USM_ATCMD_CSMP,
	RMMI_USM_ATCMD_CSCB,
  	RMMI_USM_ATCMD_CMGL,
  	RMMI_USM_ATCMD_CMGR,
  	RMMI_USM_ATCMD_CMSS,
  	RMMI_USM_ATCMD_CMGW,
  	RMMI_USM_ATCMD_CMGD,
  	RMMI_USM_ATCMD_CNMI,
  	RMMI_USM_ATCMD_NULL
} rmmi_usm_atcmd_enum;	
typedef enum
{
	RMMI_USM_DOMAIN_PS_ONLY, 
	RMMI_USM_DOMAIN_CS_ONLY,
	RMMI_USM_DOMAIN_PS_PREFERRED, 
	RMMI_USM_DOMAIN_CS_PREFERRED,
	RMMI_USM_DOMAIN_NULL
} rmmi_usm_domain_enum;
typedef enum
{
  	RMMI_USM_SM = 0,			
  	RMMI_USM_ME = 1, 			
  	RMMI_USM_SR = 2, 
  	RMMI_USM_BM =3,
  	RMMI_USM_TA = 4,
  	RMMI_USM_SM_PREFER = 5,
  	RMMI_USM_ME_PREFER = 6,
  	RMMI_USM_MT = 7,
  	RMMI_USMSTORAGE_UNSPECIFIED = 7
} rmmi_usm_memory_enum;
typedef enum
{
  	RMMI_USM_REC_UNREAD = 0,
	RMMI_USM_REC_READ,
	RMMI_USM_STO_UNSENT,
	RMMI_USM_STO_SENT,
	RMMI_USM_ALL,
	RMMI_USM_STAT_NULL
}rmmi_usm_message_stat_enum;
typedef enum
{
  	RMMI_FKPD_PRESS = 0,
	RMMI_FKPD_RELEASE,
	RMMI_FKPD_NULL
}rmmi_fkpd_stat_enum;

typedef enum
{
    RMMI_DIAG_FOR_TST = 0,
    RMMI_DIAG_FOR_AT,
    RMMI_DIAG_FOR_UNKNOWN = 0xff
} rmmi_diag_func_enum; /* __USB_MULTIPLE_COMPORT_SUPPORT__, mtk02285_usb2 */


typedef enum 
{
   RMMI_FM_TURN_OFF,
   RMMI_FM_SET,
   RMMI_FM_SEARCH_UP,
   RMMI_FM_SEARCH_DOWN
}rmmi_fm_opcode_enum;

typedef enum 
{
   RMMI_CPBW_ENCODE_IRA,
   RMMI_CPBW_ENCODE_UCS2,
   RMMI_CPBW_ENCODE_UCS2_81,
   RMMI_CPBW_ENCODE_UCS2_82,
   RMMI_CPBW_ENCODE_MAX
}rmmi_cpbw_encode_enum;

#ifdef __OP01__
typedef enum //for ^ORIG, ^CONF, ^CONN, ^CEND
{
    RMMI_MO_STATE_NONE,
    RMMI_MO_STATE_ORIG,
    RMMI_MO_STATE_CONF,
    RMMI_MO_STATE_CONN,
    RMMI_MO_STATE_CEND
} rmmi_mo_state_enum;

typedef enum
{
    RMMI_MO_CALL_TYPE_VOICE = 0,
    RMMI_MO_CALL_TYPE_CS_DATA = 1,
    RMMI_MO_CALL_TYPE_PS_DATA = 2,
    RMMI_MO_CALL_TYPE_EMERGENCY = 9,
    RMMI_MO_CALL_TYPE_UNKNOWN
} rmmi_mo_call_type_enum;

typedef enum
{
    CM_CALL_END_OFFLINE = 0, //respond NO CARRIER, no ^CEND
    CM_CALL_END_NO_SRV = 27, //respond NO CARRIER, no ^CEND
    CM_CALL_END_CLIENT_END = 29,
    CM_CALL_END_CONF_FAILED = 101,
    CM_CALL_END_NETWORK_END = 104
} rmmi_cm_call_end_status_enum;

typedef enum //refer to clcc_state_enum
{
    RMMI_DSCI_STATE_ACTIVE = 0,
    RMMI_DSCI_STATE_HELD,
    RMMI_DSCI_STATE_MO_DIALING,
    RMMI_DSCI_STATE_MO_ALERT,
    RMMI_DSCI_STATE_MT_INCOMING,
    RMMI_DSCI_STATE_MT_WAITING,
    RMMI_DSCI_STATE_CALL_END,
    RMMI_DSCI_STATE_NONE
} rmmi_dsci_call_state_enum;

typedef enum
{
    RMMI_DSCI_TYPE_VOICE = 0,
    RMMI_DSCI_TYPE_DATA
} rmmi_dsci_call_type_enum;

typedef enum
{
    RMMI_DSCI_ASYNC = 0,
    RMMI_DSCI_SYNC,
    RMMI_DSCI_REL_ASYNC,
    RMMI_DSCI_REL_SYNC,
    RMMI_DSCI_UNKNOWN_DATA_TYPE
} rmmi_dsci_data_type_enum;
#endif /* __OP01__ */

#endif /* _RMMI_COMMON_ENUM_H */
