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
 * custom_mmi_default_value.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *    This file is for customers to config/customize their parameters of MMI.
 *
 * Author:
 * -------
 * Cylen Yao (mtk00911)
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
 * May 3 2010 mtk01597
 * [MAUI_02395202] SS / call setting revise for app/srv split and 10A pluto fw
 * 
 *
 * Apr 16 2010 mtk33064
 * [MAUI_02095754] [10 A DA] Changes
 * UPP
 *
 * Apr 15 2010 mtk80241
 * [MAUI_02179275] [New feature] DS + UDX + Note 10A
 * 
 *
 * Apr 14 2010 mtk01890
 * [MAUI_02395332] [CBM] check in all 10A changes
 * 
 *
 * Apr 14 2010 mtk01597
 * [MAUI_02395202] SS / call setting revise for app/srv split and 10A pluto fw
 * 
 *
 * Apr 14 2010 mtk80244
 * [MAUI_02179311] [Vcard] pluto_10A
 * 
 *
 * Apr 14 2010 mtk80241
 * [MAUI_02179275] [New feature] DS + UDX + Note 10A
 * 
 *
 * Apr 14 2010 MTK80016
 * [MAUI_02179067] [CBS] Cell Broadcast Service App/Srv Split Check In
 * 
 *
 * Apr 14 2010 mtk01202
 * [MAUI_02395019] [CSMCC][Revise] Remove CSD from CSM to MMI
 * 
 *
 * Apr 13 2010 mtk02283
 * [MAUI_02333004] [UM] Checkin conversation box
 * 
 *
 * Apr 13 2010 mtk02176
 * [MAUI_02373549] [Gadget] code check in
 * 
 *
 * Apr 7 2010 mtk80274
 * [MAUI_02176957] [10A] Check-in Shortcuts
 * 
 *
 * Mar 17 2010 mtk80027
 * [MAUI_02165149] [OG_HS32] Orange HomeScreen 3.2 check-in
 * 
 *
 * Feb 28 2010 mtk02176
 * [MAUI_02364446] [Gadget] code check in
 * 
 *
 * Feb 10 2010 mtk33057
 * [MAUI_02147431] [FM]when repower fm radio, the sound was played in mute mode
 * 
 *
 * Jan 31 2010 mtk80199
 * [MAUI_02344874] Calenadr_Add event and no edit the data,but the RSK will change
 * 
 *
 * Jan 27 2010 mtk80199
 * [MAUI_02143010] LGLT V32/V33 Activity
 * 
 *
 * Jan 25 2010 mtk80046
 * [MAUI_02142158] [OG_HS31_IT] check-in homescreen V3.1
 * 
 *
 * Jan 23 2010 mtk80027
 * [MAUI_02141897] [OG_HS31] Orange HomeScreen 3.1 check-in
 * 
 *
 * Jan 23 2010 mtk33054
 * [MAUI_02067227] Checkins for IME UI/UE Enhance
 * 
 *
 * Jan 20 2010 MTK33079
 * [MAUI_02063182] Q03C Browser V02
 * Browser V02 feature check in
 *
 * Jan 20 2010 mtk01393
 * [MAUI_02336999] [Audio ResGen] check in new audio resource gen process to 09B and Trunk 10.05 (HQ)
 * 
 *
 * Jan 16 2010 mtk33056
 * [MAUI_02067227] Checkins for IME UI/UE Enhance
 * 
 *
 * Dec 23 2009 mtk80518
 * [MAUI_02130301] CU Gemini 1.0 DM SR and MainMenu Checkin
 * 
 *
 * Nov 29 2009 mtk80496
 * [MAUI_01921485] @CMCC streaming
 * 
 *
 * Nov 25 2009 mtk80018
 * [MAUI_01918621] [AGPS] engineer mode add payload setting
 * 
 *
 * Nov 24 2009 mti00173
 * [MAUI_02026902] App/Srv checkin for UMMS
 * 
 *
 * Nov 24 2009 mti00178
 * [MAUI_01996818] [CERTMAN_MMI] Check-in new feature for OCSP settings
 * 
 *
 * Nov 24 2009 mtk01585
 * [MAUI_01996831] [Idle] Check-in Dialer Search feature
 * Check-in feature.
 *
 * Oct 30 2009 MTK80016
 * [MAUI_01906705] [SMS2.0] srv/app split check in
 * 
 *
 * Oct 30 2009 mtk80244
 * [MAUI_01905805] [PHB] App/Srv Split
 * 
 *
 * Oct 29 2009 mtk02508
 * [MAUI_01974405] [PHB] App/Srv Split
 * remove type declaration to MCD codegen
 *
 * Oct 29 2009 mtk80244
 * [MAUI_01905805] [PHB] App/Srv Split
 * 
 *
 * Oct 29 2009 mtk01202
 * [MAUI_01974405] [PHB] App/Srv Split
 * 
 *
 * Oct 27 2009 mti00150
 * [MAUI_01623715] [Indian/Muslim revise]Check-ins for Indian/Muslim calendar revise.
 * Check-ins for Indian/Muslim Calendar revise.
 *
 * Oct 27 2009 mtk80199
 * [MAUI_01905303] [app/srv split] Calendar app/srv split
 * 
 *
 * Oct 7 2009 mti00161
 * [MAUI_01619381] Wifi support on dual sim feature checkin
 * WLAN Profile Feature
 *
 * Sep 21 2009 mtk01890
 * [MAUI_01957995] [SOC] network bearer management revision
 * 
 *
 * Sep 3 2009 mtk80013
 * [MAUI_01949580] CTA dual-SIM standard check-in
 * 
 *
 * Sep 1 2009 mtk80274
 * [MAUI_01893624] [DM CU] Remind user after fota update success.
 * 
 *
 * Aug 24 2009 mtk80241
 * [MAUI_01891066] [Sync]Can't display the full device name
 * 
 *
 * Aug 21 2009 mti00191
 * [MAUI_01610300] Adding RDS support to MAUI branch
 * 
 *
 * Aug 7 2009 mti00204
 * [MAUI_01760745] [wap] do not change the URL, but will popup "invalid URL"
 * Removing the User defined homepage and the Homepage settings feature
 *
 * Aug 6 2009 mti00150
 * [MAUI_01606427] Azaan Alarms check-in
 * Check-ins for Azaan alarm application.
 *
 * Aug 5 2009 mtk01936
 * [MAUI_01715758] [MAUI][09A][R5R6_Dev][WISE] Allow Internal Application to set all GPRS related param
 * 
 *
 * Aug 5 2009 mtk80241
 * [MAUI_01877999] [Note Sync] Note Sync Check in
 * 
 *
 * Aug 4 2009 mtk01890
 * [MAUI_01715758] [MAUI][09A][R5R6_Dev][WISE] Allow Internal Application to set all GPRS related param
 * 
 *
 * Jul 9 2009 MTK80248
 * [MAUI_01873616] [New Feature Check IN][Support OMA Client Provisioning on Streaming and AGPS]
 * 
 *
 * Jul 7 2009 mtk80199
 * [MAUI_01875532] [New feature] Java support for CMCC, add details and alarm time in task and event
 * 
 *
 * Jun 30 2009 mtk80013
 * [MAUI_01873581] [Auto Test] auto test coding for phonesetup and calibration check in 08B 09A MAUI
 * 
 *
 * Jun 11 2009 mtk80013
 * [MAUI_01868205] [Venus MMI] Venus main menu effect menu check in for Venus Main menu
 * 
 *
 * May 14 2009 mtk02218
 * [MAUI_01685445] [Baidu] Baidu Search check-in
 * 
 *
 * May 6 2009 mtk80131
 * [MAUI_01418718] Remove FOTA_DM Setting
 * 
 *
 * Apr 18 2009 mtk80241
 * [MAUI_01421358] [Syncml][New Feature] [Syncml MMI revise] check in
 * 
 *
 * Apr 18 2009 mtk80013
 * [MAUI_01423756] [Note app] Check in Note application new feature
 * 
 *
 * Apr 16 2009 mtk01254
 * [MAUI_01669967] [NVRAM] check in custpack enhance file
 * 
 *
 * Apr 16 2009 mtk80241
 * [MAUI_01421358] [Syncml][New Feature] [Syncml MMI revise] check in
 * 
 *
 * Apr 16 2009 mtk80241
 * [MAUI_01421358] [Syncml][New Feature] [Syncml MMI revise] check in
 * 
 *
 * Mar 20 2009 mti00182
 * [MAUI_01565815] ECompass New feature check-in
 * ECompass application check-in
 *
 * Mar 19 2009 mtk01566
 * [MAUI_01650404] [MMI] [VF UE] Auto reject by SMS
 * 
 *
 * Mar 19 2009 mtk80064
 * [MAUI_01392397] [clipboard]the zi set changed after power cycle.
 * 
 *
 * Mar 18 2009 mtk80141
 * [MAUI_01410586] [VF PHB] merger to MAUI 09A
 * 
 *
 * Mar 18 2009 mtk80037
 * [MAUI_01410107] [Provisioning Inbox] Check in code
 * 
 *
 * Mar 18 2009 mtk01700
 * [MAUI_01649251] [VT] New features -
 * 
 *
 * Mar 13 2009 mtk80013
 * [MAUI_01407558] [VF UE] network setup cell info feature check in to MAUI
 * 
 *
 * Feb 21 2009 mtk00563
 * [MAUI_01634468] [A8box] 20090220 patch
 * 
 *
 * Feb 20 2009 mtk02003
 * [MAUI_01284035] [JAVA]New feature
 * 
 *
 * Feb 20 2009 mtk80021
 * [MAUI_01398545] [Phonebook] VF UE - PB copy SIM at 1st startup support
 * 
 *
 * Jan 16 2009 mti00150
 * [MAUI_01548222] [AP1][INTERNAL][HIJRI CALENDAR]
 * Check-ins for Hijri Calendar application.
 *
 * Dec 24 2008 mti00149
 * [MAUI_01537201] Add FM Scheduler application
 * Add FM Scheduler LID
 *
 * Nov 28 2008 mbj06013
 * [MAUI_01343116] [time zone]Time zone enhancement with two layer home city and auto update date time
 * 
 *
 * Nov 27 2008 MBJ07024
 * [MAUI_01342148] [New feature][SyncML] Device sync support
 * 
 *
 * Nov 25 2008 mtk01393
 * [MAUI_01284299] [Sound Effects] check in Bass Enhancement
 * 
 *
 * Nov 12 2008 mbj08032
 * [MAUI_01424461] [VF] [sound recorder] the default storage can not change after Restore factory setti
 * 
 *
 * Nov 2 2008 mtk01583
 * [MAUI_01267044] [MMI][Phonebook] support 2nd email address for MSA1.0
 * 
 *
 * Nov 1 2008 mti00048
 * [MAUI_00918106] [WAP][Obigo Q05A] Vodafone feature TCD-USRX-REQ-010277 (Post Session Display)
 * 
 *
 * Oct 31 2008 mbj07029
 * [MAUI_01330766] [UDX] Add UDX (Universal Data eXchange) feature
 * 
 *
 * Oct 31 2008 mbj06021
 * [MAUI_01330766] [UDX] Add UDX (Universal Data eXchange) feature
 * 
 *
 * Oct 30 2008 MBJ07024
 * [MAUI_01330380] [Java] MSA 1.0 Support
 * 
 *
 * Oct 30 2008 mtk01566
 * [MAUI_01265293] [MMI]][UCM] White list feature
 * 
 *
 * Oct 28 2008 mbj08025
 * [MAUI_01329073] [New feature]
 * 
 *
 * Oct 28 2008 MTK02176
 * [MAUI_01263440] [EM]Engineer Mode Ineternet Application check in
 * 
 *
 * Oct 13 2008 mbj06077
 * [MAUI_01248961] [MEDPLY] Check-in new Media Player feature - MBJ
 * fix build error
 *
 * Oct 13 2008 mbj06077
 * [MAUI_01248961] [MEDPLY] Check-in new Media Player feature - MBJ
 * 
 *
 * Oct 10 2008 mbj07029
 * [MAUI_01121617] [Phonebook] Email length revise
 * 
 *
 * Oct 8 2008 mbj07025
 * [MAUI_01248961] [MEDPLY] Check-in new Media Player feature - MBJ
 * 
 *
 * Oct 7 2008 mtk01202
 * [MAUI_01235799] For esp00000b57:customize Primary and Secondary DNS IP
 * 
 *
 * Oct 6 2008 mtk01890
 * [MAUI_01235799] For esp00000b57:customize Primary and Secondary DNS IP
 * 
 *
 * Oct 3 2008 mtk01341
 * [MAUI_01246272] Add intial values of EM->Audio->Debug Info
 * 
 *
 * Oct 3 2008 mtk01566
 * [MAUI_01249175] [MMI] VF_REQ_3071
 * 
 *
 * Sep 24 2008 mbj07029
 * [MAUI_01116858] [Phonebook] email length 70 to 60 back
 * 
 *
 * Sep 23 2008 mbj06113
 * [MAUI_01116282] AVK check in(0840 only check in MAUI)
 * 
 *
 * Sep 8 2008 mtk01566
 * [MAUI_01200695] Call_the both caller ID auto change as "Send ID"
 * 
 *
 * Sep 6 2008 mtk01583
 * [MAUI_01234433] [MMI][Phonebook] enlarge email field to 70 bytes
 * 
 *
 * Sep 5 2008 MBJ07024
 * [MAUI_01106094] [New Feature] SyncML Task Sync Support
 * 
 *
 * Sep 4 2008 MBJ07024
 * [MAUI_01111030] [New Feature]SyncML Vodafone default account support
 * 
 *
 * Aug 8 2008 mtk01583
 * [MAUI_00818487] [MMI][Phonebook][Call][Msg] save contact notify after MO and MT, call and msg
 * 
 *
 * Aug 7 2008 mbj07031
 * [MAUI_01099444] [PhoneBook] CF5 ICE (In Case of Emergency)
 * 
 *
 * Aug 6 2008 mbj07033
 * [MAUI_01098825] [IPSEC]  Support the IP Security in the Maui.
 * 
 *
 * Aug 5 2008 mbj06040
 * [MAUI_01098648] [SIM Provisioning] Check in SPA and related changed code
 * 
 *
 * Aug 5 2008 mbj06054
 * [MAUI_01098285] [BT] Select drive storage
 * 
 *
 * Jul 17 2008 MBJ07024
 * [MAUI_00803230] Organizer_Delete contact from JAVA,then add contact from phonebook, the birthday dis
 * 
 *
 * Jul 14 2008 mbj06020
 * [MAUI_00800944] [SMS][EMS]Support OGDR non-standard character feature
 * 
 *
 * Jul 14 2008 mbj06020
 * [MAUI_00800944] [SMS][EMS]Support OGDR non-standard character feature
 * 
 *
 * Jul 10 2008 mtk01393
 * [MAUI_00801968] [OP11 Sound] Check in OP11 sound new feature
 * 
 *
 * Jul 10 2008 MBJ06016
 * [MAUI_01085548] [MMI][SMS] Homescreen Stage-2 SMS and Voicemail Support
 * 
 *
 * Jul 9 2008 mtk01583
 * [MAUI_00775629] [CallLog] the nvram length error for __L4_MAX_NAME_60__
 * 
 *
 * Jul 8 2008 mtk00911
 * [MAUI_00791730] [DRM] VF-Phase1 (TW)
 * 
 *
 * Jun 28 2008 MBJ07024
 * [MAUI_01081932] [Organizer]Birthday alert memory reduce
 * 
 *
 * Jun 24 2008 MBJ07024
 * [MAUI_01079710] [Organizer]When the calendar reminder time coming ,check the time clock in idle ,the
 * 
 *
 * Jun 17 2008 MBJ07024
 * [MAUI_01077673] [1] Fatal Error (807, 111b3d70) - MMI
 * 
 *
 * Jun 16 2008 mbj06078
 * [MAUI_00578361] [Change feature]Game volume control not follow user profile setting.
 * 
 *
 * Jun 15 2008 MBJ07024
 * [MAUI_01070926] [New Feature][Organizer] OGDR Diary&Search&Status Support
 * 
 *
 * Jun 12 2008 MBJ07024
 * [MAUI_00262900] Idle mode_In Chinese mode, use English words for time and date in the idle .
 * 
 *
 * May 27 2008 mtk01602
 * [MAUI_00778420] SMS bootstrapping
 * Add new strucure to CSD profile for SMS Bootstrap.
 *
 * May 27 2008 mtk01890
 * [MAUI_00778420] SMS bootstrapping
 * 
 *
 * May 24 2008 mti00131
 * [MAUI_00846975] Unified Profile & Provisioning (UPP) application check-in in W08.22 Maintunk with UP
 * WAP_PROF checkin.
 *
 * May 23 2008 MBJ07024
 * [MAUI_01046282] [Syncml OTA] SMS bootstrap update support
 * 
 *
 * May 17 2008 mtk01583
 * [MAUI_00773741] [call log] call log on homescreen
 * 
 *
 * May 17 2008 MBJ07024
 * [MAUI_00982983] [SyncML client] Edit proxy port,it should play error tone when input more than 5 dig
 * 
 *
 * May 16 2008 MBJ07024
 * [MAUI_01040598] [OGDR] Calendar & Dual Colck for home screen
 * 
 *
 * May 16 2008 mbj07029
 * [MAUI_01040550] [Phonebook][HS] orange vip contact
 * 
 *
 * May 15 2008 mbj06077
 * [MAUI_01038564] [New Feature]Audio Player multiple list support
 * 
 *
 * Apr 25 2008 mbj06013
 * [MAUI_00668587] [HomeScreen]PhoneSetup orange homescreen menu check in W08.18
 * 
 *
 * Apr 18 2008 mtk02218
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * Add NVRAM_EM_AGPSLOG_ENABLED
 *
 * Mar 21 2008 mtk01254
 * [MAUI_00735053] [Java] Dual Sim Mode Support
 * 
 *
 * Mar 20 2008 MBJ07024
 * [MAUI_00285067] [SyncML] Edit proxy port,it shoud play error tone when input more than 5 digits.
 * 
 *
 * Mar 17 2008 mtk02159
 * [MAUI_00733238] Add Memory profiling switch in NVRAM
 * 
 *
 * Mar 11 2008 mtk01877
 * [MAUI_00730644] [Camcorder]MMI Camcorder check in
 * 
 *
 * Mar 7 2008 mtk01215
 * [MAUI_00728353] Setting_The behavior on idle of SIM1 and SIM2 are not sync when auto update od time
 * Store NW name of SIM2 into NVRAM.
 *
 * Feb 20 2008 mbj06013
 * [MAUI_00627331] [Phone Setup]Screen Switch Effect feature check in W08.09
 * 
 *
 * Feb 20 2008 mbj06013
 * [MAUI_00627332] [Phone Setup]Motion Mainmenu and Dialer check in W08.09
 * 
 *
 * Feb 19 2008 mtk01583
 * [MAUI_00513539] [Idle mode ] Can't show the SIM2 missed call after power on again .
 * 
 *
 * Feb 1 2008 Mbj06038
 * [MAUI_00523978] Bluetooth receiving drive switch feature integration
 * 
 *
 * Jan 30 2008 mbj06013
 * [MAUI_00522859] [PhoneSetup]Check in DCD code to MAUI
 * 
 *
 * Jan 25 2008 mtk01393
 * [MAUI_00612021] [Gemini] patch Gemini back to 07B and Trunk
 * 
 *
 * Jan 25 2008 mtk01393
 * [MAUI_00565072] File Manager_Can't save audio volume after reboot.
 * 
 *
 * Jan 23 2008 mtk00612
 * [MAUI_00610461] [Video] Multiple profile support
 * 
 *
 * Jan 23 2008 mtk00911
 * [MAUI_00606892] [GEMINI][NVRAM] Config new projects.
 * 
 *
 * Dec 28 2007 mbj06013
 * [MAUI_00511565] [Network Setup]Network Max PLMN number from 10 to 24 if def LOW_COST_SUPPORT
 * 
 *
 * Dec 28 2007 mtk01393
 * [MAUI_00597882] [Sound Effect] new editable EQ strucure and rename scenario
 * 
 *
 * Dec 3 2007 mbj06013
 * [MAUI_00499996] [PhoneSetup]Calendar Idle Screen support check in
 * 
 *
 * Dec 3 2007 mtk01583
 * [MAUI_00584663] [MMI][Phonebook][JSR75] support address(7 fields), preferred number, image, geograph
 * 
 *
 * Nov 29 2007 MBJ07024
 * [MAUI_00498652] [New Feature] Syncml DS bookmark sync support
 * 
 *
 * Nov 26 2007 mtk00612
 * [MAUI_00581655] Video Streaming_ The string of steaming URL can not display normal
 * 
 *
 * Nov 3 2007 mbj06018
 * [MAUI_00489164] [GIS]GIS check in
 * 
 *
 * Nov 2 2007 mtk01583
 * [MAUI_00567039] [MMI][PHB] phb for IMPS wv13 enhancement
 * 
 *
 * Nov 1 2007 mtk01215
 * [MAUI_00420997] [Calendar] Add a new task, the task just ring one time.
 * Add snooze reminder for todolist.
 *
 * Nov 1 2007 mbj07032
 * [MAUI_00483028] Trace Group modify of extra
 * 
 *
 * Nov 1 2007 MBJ06082
 * [MAUI_00488189] FM Radio add Mono\Stereo
 * 
 *
 * Oct 31 2007 mtk01393
 * [MAUI_00486879] IMPS new feature
 * 
 *
 * Oct 24 2007 mtk00612
 * [MAUI_00560852] [Video Streaming] Reduce NVMRA memory size
 * 
 *
 * Oct 11 2007 mtk00612
 * [MAUI_00555544] Video Stream_The Video can play over JAVA to open video player,But it can't play in
 * 
 *
 * Oct 8 2007 mtk01215
 * [MAUI_00232073] Calendar_Can we add new tasks when there are only birthday tasks in list?
 * Add new value for calendar view type
 *
 * Oct 5 2007 mtk01393
 * [MAUI_00556020] [AUD] New audio equailzer and audio compensation filter feature
 * 
 *
 * Sep 26 2007 mti00054
 * [MAUI_00038902] nvram LID changes for WAP & MMS activated profile index
 * 
 *
 * Sep 25 2007 mti00048
 * [MAUI_00038573] [WAP][Obigo Q05A] changed the common nvram field names for Browser Application setti
 * 
 *
 * Sep 17 2007 mtk01254
 * [MAUI_00548273] [Java] Add network icon setting
 * 
 *
 * Sep 14 2007 mtk01890
 * [MAUI_00547991] [VoIP] checking in voip log feature
 * 
 *
 * Sep 9 2007 mtk01583
 * [MAUI_00464185] [Phonebook ]_Show English string in Chinese mode .
 * change the alertType in PHB_CALLER_GROUP_STRUCT from MMI_ALERT_TYPE to kal_uint8
 *
 * Sep 8 2007 mtk01393
 * [MAUI_00532396] [MMI][Video Telephony] Check in MMI VT feature
 * 
 *
 * Sep 8 2007 mtk00973
 * [MAUI_00532396] [MMI][Video Telephony] Check in MMI VT feature
 * 
 *
 * Aug 23 2007 MBJ06021
 * [MAUI_00460732] [Change feature][Network setup] Add LOW_COST_SUPPORT support for preferred PLMN
 * 
 *
 * Aug 13 2007 mbj06083
 * [MAUI_00237035] [1] Assert fail: buf_ptr != 0 gdi_layer.c 320 - MMI
 * 
 *
 * Aug 10 2007 MBJ06080
 * [MAUI_00446481] [new feature] language learning
 * 
 *
 * Aug 9 2007 mbj06020
 * [MAUI_00457273] [Message new feature: background send] check in, Maintrunk and 07A
 * 
 *
 * Aug 6 2007 mbj06013
 * [MAUI_00455750] [DM][New Feature] DM Profile Provisioning
 * 
 *
 * Jul 16 2007 mtk01583
 * [MAUI_00416466] [MMI][vCard][Phonebook] vCard 3.0 support
 * 
 *
 * Jul 14 2007 mbj06067
 * [MAUI_00446481] [new feature] language learning
 * add mmi nuvram record of Language Learning
 *
 * Jun 27 2007 mbj06013
 * [MAUI_00442059] [Phone Setup]modify NVRAM ID error of avatar serial number
 * 
 *
 * Jun 27 2007 MBJ06082
 * [MAUI_00409289] [Engineer Mode]Can't inter Engineer Mode if no format.
 * 
 *
 * Jun 22 2007 MBJ06082
 * [MAUI_00441095] FM add audio quality
 * 
 *
 * Jun 21 2007 mbj06013
 * [MAUI_00440708] [Phone Setup]avatar function change for MAUI_00233414
 * 
 *
 * Jun 16 2007 MBJ06082
 * [MAUI_00439580] sound recorder add audio quality
 * 
 *
 * Jun 5 2007 MBJ06080
 * [MAUI_00435936] [E-dictionary-TTS] buil error
 * 
 *
 * May 29 2007 MBJ06080
 * [MAUI_00434035] [Dictionary-TTS] change seting screen, and add speak speed control
 * 
 *
 * May 28 2007 mtk00911
 * [MAUI_00397627] [Obigo][Q05A]Check in MMS application and changes made by PMT
 * 
 *
 * May 28 2007 mtk00911
 * [MAUI_00397627] [Obigo][Q05A]Check in MMS application and changes made by PMT
 * 
 *
 * May 21 2007 MBJ06080
 * [MAUI_00431838] [new feature] ditionary tts
 * 
 *
 * May 21 2007 mtk01495
 * [MAUI_00395017] [Q05A WAP] Check vBookmark-related files for PMT colleague
 * Check in vBookmark-related files for PMT colleague
 *
 * May 18 2007 mtk01583
 * [MAUI_00390755] 6225平台電話本容量增加需求
 * 
 *
 * May 18 2007 mbj06016
 * [MAUI_00430558] [New Feature][DM] PS, MMI, CCA, DS, WAP, MMS, DataAccount, self-registe and network
 * 
 *
 * May 18 2007 mbj06029
 * [MAUI_00430950] [New Feature][DS provisioning & DM support]
 * 
 *
 * May 1 2007 wcpadmin
 * rebase maintrunk.MAUI_MBJ on 2007-5-1 2:25:59.
 * 
 *
 * Apr 30 2007 wcpadmin
 * rebase maintrunk.MAUI_MSZ on 04/30/2007 02:30:32 PM.
 * 
 *
 * Apr 27 2007 mtk00612
 * [MAUI_00372535] [Streaming] Operator new features - MMI
 
 * Apr 27 2007 mtk00612
 * [MAUI_00384722] Video Player_Video Player can not auto repeat play after switching play screen size
 * 
 *
 * Apr 26 2007 mbj06051
 * [MAUI_00232483] SyncML_ After the  proxy username and password transmit to WPS task,the value has be
 * define proxy username and password length as 30 to keep identical with wap.
 *
 * Apr 24 2007 wcpadmin
 * rebase maintrunk.MAUI_MBJ on 2007-4-24 0:25:15.
 * 
 *
 * Apr 23 2007 wcpadmin
 * rebase maintrunk.MAUI_MSZ on 04/23/2007 10:36:59 AM.
 * 
 *
 * Apr 13 2007 MBJ06023
 * [MAUI_00374320] Not able to assign a name to a drawing in image editor app
 * 
 *
 * Apr 6 2007 MTK01136
 * [MAUI_00421683] [Network setup] Do opertation(Create New\Delete\Change Priority) in Preferred Networ
 * Only accept 50 preferred networks in MMI
 *
 * Mar 26 2007 wcpadmin
 * rebase maintrunk.MAUI_MSZ on 03/26/2007 11:03:50 AM.
 * 
 *
 * Mar 26 2007 mtk01729
 * [MAUI_00375717] [MMI][InputMethod] Mixed Language support
 * 
 *
 * Mar 26 2007 mtk01729
 * [MAUI_00375717] [MMI][InputMethod] Mixed Language support
 * 
 *
 * Mar 26 2007 mbj06078
 * [MAUI_00423074] [Game]VRMJ change feature
 * 
 *
 * Mar 19 2007 mtk00911
 * [MAUI_00373748] [AVATAR] New feature: 3D Avatar
 * 
 *
 * Mar 17 2007 wcpadmin
 * rebase maintrunk.MAUI_MSZ on 03/17/2007 11:19:34 PM.
 * 
 *
 * Mar 1 2007 mtk00676
 * [MAUI_00368733] [Need Patch] 06B.W07.09 build error for MT6228_SYNC_LCM
 * 
 *
 * Jan 22 2007 mtk01583
 * [MAUI_00420846] [call management ]:After power cycle ,missed call icon disappeared .
 * add NVRAM_CHIST_HAVE_MISSED_CALL
 *
 * Dec 25 2006 MTK01341
 * [MAUI_00353653] FOTA Support.
 * 
 *
 * Dec 14 2006 mtk00798
 * [MAUI_00227934] [1] Assert fail: selected SettingScreenProf.c 3498 - MMI
 * 
 *
 * Dec 12 2006 mtk00612
 * [MAUI_00349838] [1] Assert fail: 0 stream_fsm.c 768 - MED
 * 
 *
 * Dec 4 2006 mtk01136
 * [MAUI_00334334] [MMI][Network Setup][new feature] WLAN and GSM Preferred mode
 * 
 *
 * Nov 13 2006 mtk00612
 * [MAUI_00342775] [Stream] Integration
 * 
 *
 * Nov 13 2006 mtk01215
 * [MAUI_00342569] [MMI][NITZ] Support Network Name Display
 * Save nitz network name information to NVRMA.
 *
 * Nov 6 2006 MTK01341
 * [MAUI_00340910] [EM] New Request: Network Events Notification Mech.
 * Network Events notification mech. support.
 *
 * Nov 1 2006 mtk01215
 * [MAUI_00337628] [MMI]Calendar Revise
 * kal_bool size could not be calculated correctly for Shadow NVRAM.
 *
 * Oct 29 2006 mtk00701
 * [MAUI_00338947] [Jataayu WAP]Check in PMT's changes to w06.44
 * Added s a short data enum NVRAM_JMMS_LANGUAGE_CHANGE
 *
 * Oct 23 2006 mtk00973
 * [MAUI_00337663] [MMI][Unified Composer] Check in unified composer feature
 * 
 *
 * Oct 22 2006 mtk01215
 * [MAUI_00337628] [MMI]Calendar Revise
 * 
 *
 * Oct 16 2006 mtk00609
 * [MAUI_00335399] [MMI][Phonebook] Add Name List Filter for SIM only, Phone only or Both
 * 
 *
 * Oct 11 2006 mtk01215
 * [MAUI_00225737] enter address_the counter is consistent
 * Update download string length
 *
 * Oct 11 2006 mtk00862
 * [MAUI_00199255] [MMI][VoIP] Check in MMI VoIP related code
 * 
 *
 * Oct 10 2006 MTK01341
 * [MAUI_00335115] [FM] FM Radio Test Item Support.
 * FM Radio Test Item Support.
 *
 * Oct 4 2006 mtk01278
 * [MAUI_00017065] Default wallpaper display after restore factory
 * PMT patch: to activate default theme when we clicked on restore factory setting
 *
 * Oct 1 2006 mtk01136
 * [MAUI_00323758] [MMI][SyncML][new feature] SyncML application
 * Remove PC profile 1
 *
 * Sep 27 2006 mtk01215
 * [MAUI_00332791] [Need Patch] Rollback MAUI_00225737 for sanity test fail
 * 
 *
 * Sep 26 2006 mtk01278
 * [MAUI_00323953] Fun and Games-Plug out USB,download theme setting will become default
 * 
 *
 * Sep 25 2006 mtk01215
 * [MAUI_00331037] Settings - Phone Setup - Time and Date - Home City
 * Modify NVRAM_TIMEZONE from DS_DOUBLE to DS_SHORT.
 *
 * Sep 24 2006 mtk01215
 * [MAUI_00225737] enter address_the counter is consistent
 * Update download counter.
 *
 * Sep 14 2006 mtk00676
 * [MAUI_00329853] [FMRDO] Keep FM Radio frequency after exit FM Radio screen.
 * 
 *
 * Sep 1 2006 mtk00701
 * [MAUI_00326378] [Jataayu WAP]Check in PMT's files to w06.36
 * Added NVRAM byte enum NVRAM_BRW_IMAGE_SELECTION_ON_OFF & double enum NVRAM_JMMS_RESTORE_RETRIEVAL_SETTINGS
 *
 * Aug 27 2006 mtk01136
 * [MAUI_00323758] [MMI][SyncML][new feature] SyncML application
 * 
 *
 * Aug 14 2006 mtk00322
 * [MAUI_00321482] [MMI][SWFlash][NewFeature] SWFlash new feature check in
 * add swflash.
 *
 * Aug 7 2006 mtk00911
 * [MAUI_00213967] [DLT] PMT Patch 2006/06/12~
 * 
 *
 * Jul 18 2006 mtk00609
 * [MAUI_00206617] [MMI][PHB]Add VoIP field
 * 
 *
 * Jul 17 2006 mtk00676
 * [MAUI_00211430] [AUTO VM] Add auto record and debug info feature.
 * 
 *
 * Jul 17 2006 mtk00911
 * [MAUI_00211115] [Jataayu] Patch for 06.29
 * 
 *
 * Jul 11 2006 mtk00609
 * [MAUI_00206617] [MMI][PHB]Add VoIP field
 * 
 *
 * Jul 10 2006 mtk00911
 * [MAUI_00208938] [Jataayu] Patch for 06.28
 * 
 *
 * Jul 9 2006 mtk00911
 * [MAUI_00208938] [Jataayu] Patch for 06.28
 * 06.28
 *
 * Jul 2 2006 mtk01215
 * [MAUI_00207192] [MMI][WC]Revise World Clock
 * Add Double cache -- NVRAM_TIMEZONE.
 *
 * Jun 25 2006 mtk01393
 * [MAUI_00205406] [Audply][Lyrics] check in new mmi code
 * 
 *
 * Jun 15 2006 mtk00612
 * [MAUI_00202933] [TVOUT] Add audio path adjust
 * 
 *
 * Jun 5 2006 MTK01341
 * [MAUI_00199720] [Engineer Mode] Audio Support for 16-Level Volume & TV-out.
 * Add Audio 16-Level Volume & TV-Out & Auto-VM support.
 *
 * May 29 2006 mtk01166
 * [MAUI_00198006] [ mmi/med ] new feature check in - barcode reader.
 * 
 *
 * May 22 2006 mtk00676
 * [MAUI_00191242] FM Radio - suggest  add  "speaker on" function
 * 
 *
 * May 15 2006 mtk00911
 * [MAUI_00194066] [NVRAM] Jataayu patch of 06.20
 * 
 *
 * May 15 2006 mtk00911
 * [MAUI_00194044] [NVRAM] Help to add Ebook related files.
 * Ebook
 *
 * Apr 25 2006 mtk00911
 * [MAUI_00190005] [NVRAM] Fix DLTheme codegen error
 * 
 *
 * Apr 24 2006 mtk00911
 * [MAUI_00188983] [NVRAM] Revise customization process
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef CUSTOM_MMI_DEFAULT_VALUE_H
#define CUSTOM_MMI_DEFAULT_VALUE_H

#include "kal_non_specific_general_types.h"

#if !defined(__MAUI_BASIC__) && !defined(__L1_STANDALONE__)
#include "mcd_l4_common.h"
#endif 
#include "customer_ps_inc.h"
#include "custom_phb_config.h"

#include "custom_user_profiles_defs.h"

#if !defined(__MTK_TARGET__) || defined(NVRAM_AUTO_GEN)
#define __align(x)
#endif 

#define ENCODING_LENGTH       2

/*
 * Theme
 */
#if defined(__MMI_THEMES_V2_SUPPORT__)
#define DefaultThmID             1
#define DefaultThmIndex          0
#define MAX_ENTER_ADDRESS_SIZE    255
#define MAX_URL_ENTRIES 5

typedef struct
{
    kal_uint16 total_entries;
    kal_int8 g_thm_recent_url[MAX_URL_ENTRIES][(MAX_ENTER_ADDRESS_SIZE + 1)];
} thm_nvram_download_list;
#endif /* defined(__MMI_DOWNLOADABLE_THEMES_SUPPORT__) */ 

/* 
 * profile {
 */
#define MAX_ELEMENTS 7

#define LEVEL1 0
#define LEVEL2 1
#define LEVEL3 2
#define LEVEL4 3
#define LEVEL5 4
#define LEVEL6 5
#define LEVEL7 6


#define THEME_DEFAULT 0
#define SMALL 1
#define MEDIUM 2
#define LARGE 3

#define MAX_ACTIVITIES_PER_DAY 10
#define NUMBER_OF_WEEKDAYS 7

typedef enum
{
    MMI_ALERT_NONE,
    MMI_RING,
    MMI_VIBRATION_ONLY,
    MMI_VIBRATION_AND_RING,
    MMI_VIBRATION_THEN_RING,
    MMI_SILENT
} MMI_ALERT_TYPE;

#ifdef __MMI_CONNECT_NOTICE__
typedef enum
{
    MMI_NOTICE_NONE,
    MMI_NOTICE_TONE_ONLY,
    MMI_NOTICE_VIB_ONLY,
    MMI_NOTICE_TONE_AND_VIB
} MMI_CONNECT_NOTICE_TYPE;
#endif /* __MMI_CONNECT_NOTICE__ */ 

typedef struct
{
    kal_uint8 setsec;
} SET_TIMER;

typedef struct
{
    kal_uint8 status;
    unsigned short lcdBacklight;
    SET_TIMER timer;
} LIGHT;

typedef struct
{
    kal_uint16 ringTone;
    kal_uint16 videoCallTone;
    kal_uint16 IMPSContactOnlineTone;
    kal_uint16 IMPSNewMessageTone;
    kal_uint16 IMPSNewInvitationTone;
    kal_uint16 IMPSChatroomNotificationTone;    
    kal_int16 powerOffTone;
    kal_int16 powerOnTone;
    kal_int16 coverOpenTone;
    kal_int16 coverCloseTone;
    kal_int16 messageTone;
    kal_int16 mmsTone;
    kal_int16 smsTone;
    kal_int16 emailTone;
    kal_int16 voiceTone;    
    kal_uint8 keypadTone;
    kal_uint16 alarmTone;
    kal_uint16 card2_ringTone;
    kal_int16 card2_messageTone;
} TONE_SETUP;

typedef struct
{
    unsigned short coverAnswer;
    unsigned short anyKey;
    unsigned short automatic;
} MMI_ANSWERING_MODE;

typedef struct
{
    unsigned short errorTone;
    unsigned short connectTone;
    unsigned short campOnTone;
    unsigned short warningTone;
} EXTRA_TONE;

typedef enum
{
    MMI_INTELLIGENT_CALL_ALERT_OFF,
    MMI_INTELLIGENT_CALL_ALERT_ON
} MMI_INTELLIGENT_CALL_ALERT_TYPE;

typedef struct
{
    kal_uint8 ringVolumeLevel;
    kal_uint8 keypadVolumeLevel;
    kal_uint8 loudSpeakerVolumeLevel;
    MMI_ALERT_TYPE mtCallAlertTypeEnum;
    LIGHT light;
    kal_uint8 impsStatus;
    kal_uint8 ringTypeEnum;
    TONE_SETUP toneSetup;
    MMI_ANSWERING_MODE answeringMode;
    kal_uint8 fontSizeEnum;
    MMI_INTELLIGENT_CALL_ALERT_TYPE intelligentCallAlert;
    EXTRA_TONE extraTone;
} PROFILE;

/* 
 * ToDoList {
 */

#ifdef __ASCII
#define ENCODING_LENGTH       1
#endif 

#ifdef __UCS2_ENCODING
#define ENCODING_LENGTH       2
#endif 

#define MAX_TODO_NOTE_LEN            (36)
#define  MAX_TODO_LIST_NOTE          (MAX_TODO_NOTE_LEN * ENCODING_LENGTH)

#define MAX_TDL_LOCATION_SIZE        (36)
#define MAX_TDL_LOCATION_LEN         (MAX_TDL_LOCATION_SIZE - 1)

#define MAX_TDL_DETAILS_SIZE         (101)
#define MAX_TDL_DETAILS_LEN          (MAX_TDL_DETAILS_SIZE - 1)


#define MAX_DAY_IN_WEEK             (7)
#define MAX_TODO_LIST_TASK          (10)

typedef struct MYTIME
{
    kal_uint16 nYear;
    kal_uint8 nMonth;
    kal_uint8 nDay;
    kal_uint8 nHour;
    kal_uint8 nMin;
    kal_uint8 nSec;
    kal_uint8 DayIndex; /* 0=Sunday */
} MYTIME;


typedef struct
{
    /* The order shouldnot be changed */
    MYTIME start_time;
    MYTIME end_time;  
    
    /* If alarm_type is "custom", then this field present the alarm time user prefered. */
    MYTIME alarm_time;                      
    kal_uint8 present;
    kal_uint8 repeat; 
    kal_uint8 days;                         /* Week days */
    kal_uint8 alarm_type;                   /* Weather alarm_type is On or Off or other values */
    kal_uint8 category;                     /* meeting, course, call, ... */
    kal_uint8 snooze;                       /* snooze count */
    kal_uint8 priority;                     /* priority of task */    
    kal_uint8 status;
    kal_uint8 subject[MAX_TODO_LIST_NOTE];  /* subject of an Task */
}tdl_init_struct;  /* Total 102 bytes */


typedef struct
{
    /* The order shouldnot be changed start */
    MYTIME start_time;                     
    MYTIME end_time;  
    
    /* If alarm_type is "custom", then this field present the alarm time user prefered. */
    MYTIME alarm_time;                      
    kal_uint8 present;
    kal_uint8 repeat; 
    kal_uint8 days;                         /* Week days */
    kal_uint8 alarm_type;                   /* Weather alarm_type is On or Off or other values */
    kal_uint8 category;                     /* meeting, course, call, ... */
    kal_uint8 snooze;                       /* snooze count */
    kal_uint8 priority;                     /* priority of task */    
    kal_uint8 status;
    kal_uint8 subject[MAX_TODO_LIST_NOTE];  /* subject of an Task */
    /* The order shouldnot be changed end */
    kal_uint8 location[MAX_TDL_LOCATION_SIZE * ENCODING_LENGTH]; /* location of an Task */
    kal_uint8 details[(MAX_TDL_DETAILS_SIZE) * ENCODING_LENGTH];
    MYTIME complete_time;                   /* complete time */
} ToDoListNode; /*  Total Size is 384 Bytes. */

typedef struct
{
    MYTIME first_launch;       /* The first time alarm launched */
    kal_uint8 type;                  /* alarm type, before 5 mins... */
    kal_uint8 snooze;    
    kal_uint16 tone;                  /* Tone ID, for future use */
}srv_tdl_alarm_struct;

typedef struct
{
    MYTIME start_repeat;  /* The date/time to start repeat */
    MYTIME expir_time;    /* The date/time to end repeat */
    kal_uint8 interval;
    kal_uint8 rule;              /* daily, monthly, weekly, yearly, custom */
    kal_uint8 dows;              /* For Weekly repeat, the dates to repeat */
    /*  
        Refer to vCalendar RRULE, for Monthly/Yearly repeat only
        srv_tdl_repeat_rule_type_enum
        1: By day 
        2: By position 
     */
    kal_uint8 type; 
    kal_uint8 month_sequence;    /* For Yearly repeat, by position rule. Such as "Yearly MAR XXX repeat" */
    kal_uint8 week_sequence;     /* For Yearly repeat, by position rule. Such as "Yearly Mar 1ST week XXX repeat" */
    kal_uint8 day_sequence;      /* For Monthly/Yearly repeat, by days rule. Such as "Monthly 23th day repeat" */  
    kal_uint8 week_day;          /* For Monthly/Yearly repeat, by position rule. Such as "Yearly Mar 1st week MONTHDAY repeat" */  
}srv_tdl_repeat_struct;

typedef struct
{
    /* The order shouldnot be changed start */
    MYTIME start_time;                     
    MYTIME end_time;
    MYTIME created_time;
    kal_uint8 present;
    kal_uint8 category;  /* Reminder, Meeting, Course, Date, Call, Anniversary, Birthday */
    kal_uint8 vcal;      /* vEvent or vTodo */
    kal_uint8 all_day;
    kal_uint32 extend;    /* Extended for future use */    
    srv_tdl_alarm_struct alarm;
    srv_tdl_repeat_struct repeat;    
    kal_uint8 subject[MAX_TODO_LIST_NOTE];    /* subject of an event */
    kal_uint8 location[MAX_TDL_LOCATION_SIZE * ENCODING_LENGTH];  /* location of an event */
    /* The order shouldnot be changed end */
    kal_uint8 details[MAX_TDL_DETAILS_SIZE * ENCODING_LENGTH];    /* details of an event */  
}srv_tdl_event_struct; 

typedef struct
{
    /* The order shouldnot be changed start */
    MYTIME due_time; 
    MYTIME created_time; 
    MYTIME complete_time;      
    kal_uint8 present;
    kal_uint8 priority;   /* priority of task */   
    kal_uint8 status;    
    kal_uint8 vcal;       /* vEvent or vTodo */
    srv_tdl_alarm_struct alarm;
    srv_tdl_repeat_struct repeat;    
    kal_uint8 subject[MAX_TODO_LIST_NOTE];    /* subject of an event */
    /* The order shouldnot be changed end */
    kal_uint8 details[MAX_TDL_DETAILS_SIZE * ENCODING_LENGTH];    /* details of an event */   
}srv_tdl_task_struct;

typedef struct
{
    /* The order shouldnot be changed start */
    MYTIME start_time;                     
    MYTIME end_time;
    MYTIME created_time;
    kal_uint8 present;
    kal_uint8 category;  /* Reminder, Meeting, Course, Date, Call, Anniversary, Birthday */
    kal_uint8 vcal;      /* vEvent or vTodo */
    kal_uint8 all_day;
    kal_uint32 extend;    /* Extended for future use */
    srv_tdl_alarm_struct alarm;
    srv_tdl_repeat_struct repeat;    
    kal_uint8 subject[MAX_TODO_LIST_NOTE];    /* subject of an event */
    kal_uint8 location[MAX_TDL_LOCATION_SIZE * ENCODING_LENGTH];
    /* The order shouldnot be changed end */
}srv_tdl_event_short_struct; 

typedef struct
{
    /* The order shouldnot be changed start */
    MYTIME due_time; 
    MYTIME created_time; 
    MYTIME complete_time;      
    kal_uint8 present;
    kal_uint8 priority;   /* priority of task */   
    kal_uint8 status;    
    kal_uint8 vcal;       /* vEvent or vTodo */
    srv_tdl_alarm_struct alarm;
    srv_tdl_repeat_struct repeat;    
    kal_uint8 subject[MAX_TODO_LIST_NOTE];    /* subject of an event */
    /* The order shouldnot be changed end */
}srv_tdl_task_short_struct;


#define NVRAM_TODO_LIST_RECORD_SIZE    sizeof(ToDoListNode)
#define NVRAM_TODO_LIST_RECORD_TOTAL      NUM_OF_TDL

typedef struct
{
	kal_uint8 pbName[(MMI_PHB_NAME_LENGTH + 1) * ENCODING_LENGTH];
	kal_uint8 pbNumber[MMI_PHB_NUMBER_LENGTH + 1];
	kal_uint16 bday_year;
    kal_uint8  bday_month;
    kal_uint8  bday_day;
	kal_uint16 phb_idx;
	kal_uint8  reminder;
	kal_uint8  present;
	kal_uint8  del_flag;
} nvram_ef_tdl_br_struct;

#define NVRAM_EF_TDL_BR_SIZE    sizeof(nvram_ef_tdl_br_struct)
#define NVRAM_EF_TDL_BR_TOTAL  100	/* the max birthday cpacity */

#ifdef __MMI_TODOLIST__
#define NVRAM_EF_TDL_EVENT_SIZE   sizeof(srv_tdl_event_struct)
#define NVRAM_EF_TDL_EVENT_TOTAL  NUM_OF_CAL

#define NVRAM_EF_TDL_TASK_SIZE    sizeof(srv_tdl_task_struct)
#define NVRAM_EF_TDL_TASK_TOTAL   NUM_OF_TASK
#endif /* __MMI_TODOLIST__ */
/* 
 * Download image {
 */
#define MAX_IMAGE_NAME_EXTENSION_WIDTH    (5*ENCODING_LENGTH)
#define MAX_IMAGE_NAME_WIDTH           ((13*ENCODING_LENGTH)   +  MAX_IMAGE_NAME_EXTENSION_WIDTH + ENCODING_LENGTH)
typedef struct
{
    kal_uint8 ImageName[MAX_IMAGE_NAME_WIDTH];
    kal_int32 ImagesId;
} DYNIMAGEINFO;

/* } */

/* 
 * downloaded/composed audio {
 */
#define MAX_IMAGE_NAME_EXTENSION_WIDTH    (5*ENCODING_LENGTH)
#define MAX_AUDIO_NAME_WIDTH           ((13*ENCODING_LENGTH)   +  MAX_IMAGE_NAME_EXTENSION_WIDTH + ENCODING_LENGTH)
typedef struct
{
    kal_uint8 AudioName[MAX_AUDIO_NAME_WIDTH];
    kal_uint16 AudioId;
} DYNAUDIOINFO;

/* } */

/* 
 * SMS {
 */
#ifdef GEN_FOR_PC
#define MAX_EMS_DOWNLOAD_OBJECT_NAME_LEN     13

typedef struct EMS_OBJECT_NAME
{
    kal_uint8 object_name[(MAX_EMS_DOWNLOAD_OBJECT_NAME_LEN *ENCODING_LENGTH) + ENCODING_LENGTH];
} EMS_OBJECT_NAME;

typedef struct
{
    EMS_OBJECT_NAME emsPictureNames1;
    EMS_OBJECT_NAME emsPictureNames2;
    EMS_OBJECT_NAME emsPictureNames3;
    EMS_OBJECT_NAME emsPictureNames4;
    EMS_OBJECT_NAME emsPictureNames5;
} nvram_ems_my_picture_name_struct;

typedef struct
{
    EMS_OBJECT_NAME emsAnimationNames1;
    EMS_OBJECT_NAME emsAnimationNames2;
    EMS_OBJECT_NAME emsAnimationNames3;
    EMS_OBJECT_NAME emsAnimationNames4;
    EMS_OBJECT_NAME emsAnimationNames5;
} nvram_ems_my_animation_name_struct;

typedef struct
{
    EMS_OBJECT_NAME emsMelodyNames1;
    EMS_OBJECT_NAME emsMelodyNames2;
    EMS_OBJECT_NAME emsMelodyNames3;
    EMS_OBJECT_NAME emsMelodyNames4;
    EMS_OBJECT_NAME emsMelodyNames5;
} nvram_ems_my_melody_name_struct;
#endif /* GEN_FOR_PC */ 

/* } */

/*
 * alarm use 
 */
/* number of theme is always 1 */
#define  NUM_OF_THM_ALM          1
/* number range of tdl is 1~50, because of limitation of category */
#define  NUM_OF_TDL           50
#define  NUM_OF_CAL			  (25)
#define  NUM_OF_TASK		  (25)
/* number range of alarm is 1~15 */
#if defined( __MMI_OP11_HOMESCREEN_0301__) || defined(__MMI_OP11_HOMESCREEN_0302__)
#define  NUM_OF_ALM           7
#else
#define  NUM_OF_ALM           5
#endif
/* number range of spof is 1~50 */
#define  NUM_OF_SPOF          4
/* number of factory is always 1 */
#define  NUM_OF_FAC_ALM       1

#if defined(__MMI_FM_RADIO_SCHEDULE_REC__)
#define  NUM_OF_FMSR          4
#else 
#define  NUM_OF_FMSR          0
#endif 

#ifdef __MMI_FM_RADIO_SCHEDULER__
#define  NUM_OF_FMRDO          9
#else 
#define  NUM_OF_FMRDO          0
#endif 

#ifdef __MMI_BIRTHDAY_REMINDER__
#define NUM_OF_BR			  1
#else
#define NUM_OF_BR			  0
#endif /* __MMI_BIRTHDAY_REMINDER__ */

#ifdef __SYNCML_SUPPORT__
#define    NUM_OF_SYNCML      3  
#else
#define    NUM_OF_SYNCML      0  
#endif /* __SYNCML_SUPPORT__ */
#ifdef __MMI_AZAAN_ALARM__
#define NUM_OF_AZAAN        5
#else
#define NUM_OF_AZAAN        0
#endif

#define  ALM_NUM_OF_ALAMRS (NUM_OF_TDL + NUM_OF_THM_ALM + NUM_OF_ALM + NUM_OF_SPOF + NUM_OF_FAC_ALM + NUM_OF_FMSR + NUM_OF_BR + NUM_OF_FMRDO + NUM_OF_SYNCML + NUM_OF_AZAAN)

#define  NVRAM_ALM_QUEUE_SIZE    (ALM_NUM_OF_ALAMRS * 10)       /* sizeof(alm_queue_node_struct)) */
#define  NVRAM_ALM_QUEUE_TOTAL      1

#ifdef __MMI_ALM_AUDIO_OPTIONS__
#define MAX_NAME_INPUT_LEN 21
#endif 

typedef struct
{
    unsigned char Hour;
    unsigned char Min;
    unsigned char State;
    unsigned char Freq;
    unsigned char Snooze;   /* number of snooze time */
    unsigned char Days;

#ifdef __MMI_ALM_CUST_VOLUME__
    unsigned char Volume;
#endif 
#ifdef __MMI_ALM_AUDIO_OPTIONS__
    unsigned char AudioOption;          /* audio option, which is either tone or FM radio */
    unsigned short AudioOptionValue;    /* Ring Tone ID or FM Frequency */
#endif /* __MMI_ALM_AUDIO_OPTIONS__ */ 
#ifdef __MMI_ALM_SNOOZE_SETTING__
    unsigned char SnoozeInterval;
#endif 
#ifdef __MMI_ALM_ALERT_TYPE__
    unsigned char AlertType;
#endif 
} alm_nvram_struct;

typedef struct
{
    unsigned char Type;
    unsigned char Status;
    unsigned char Hour;
    unsigned char Min;
} spof_nvram_struct;

#define NVRAM_ALM_ALARM_DATA_TOTAL  1
#define NVRAM_ALM_ALARM_DATA_SIZE      NUM_OF_ALM * sizeof(alm_nvram_struct)

#define  NVRAM_ALM_SPOF_DATA_SIZE      NUM_OF_SPOF * sizeof(spof_nvram_struct)
#define  NVRAM_ALM_SPOF_DATA_TOTAL     1

#define MAX_NW_LEN 24

#define  NVRAM_NITZ_NW_NAME_SIZE      sizeof(nitz_nw_name_struct)

#ifdef __MMI_DUAL_SIM__
#define  NVRAM_NITZ_NW_NAME_TOTAL     2
#else /*__MMI_DUAL_SIM__*/
#define  NVRAM_NITZ_NW_NAME_TOTAL     1
#endif /*__MMI_DUAL_SIM__*/

typedef struct
{
    kal_uint8 plmn[6];  //#define MAX_PLMN_LEN_MMI 6 in SimDetectionDef.h
    kal_uint8 name[MAX_NW_LEN *ENCODING_LENGTH];
    kal_uint8 add_ci;
} nitz_nw_name_struct;

/* } */

/* 
 * Data account
 */
typedef struct
{
    // must align with qos_struct
    // Please see 3GPP TS24.008 Table 10.5.156 Quality of service information element for detail explaination

    kal_uint8 qos_length;   /* QOS identifer bit, this value must be 16, or the GPRS bearer will not accept */
    kal_uint8 unused1;      /* Unused Bit */
    
    kal_uint8 delay_class;
    /* 
      * Delay Class 
      * 001 Delay Class 1 
      * 010 Delay Class 2
      * 011 Delay Class 3
      * 100 Delay Class 4 (best effort)
      * ...
      */
    
    kal_uint8 reliability_class;
    /* 
      * Reliability Class 
      * 001 Interpreted as 010
      * 010 UnACK GTP, ACK LLC, ACK RLC, Protected Data
      * 011 UnACK GTP, UnACK LLC, ACK RLC, Protected Data
      * 100 UnACK GTP, UnACK LLC, UnACK RLC, Prottected Data
      * 101 UnACK GTP, UnACK LLC, UnACK RLC, Unprotected Data
      * ...
      */
    
    kal_uint8 peak_throughput;
    /* 
      * Peak Throughput
      * 0001 Up to 1000 octet/s
      * 0010 Up to 2000 octet/s
      * 0011 Up to 4000 octet/s
      * 0100 Up to 8000 octet/s
      * 0101 Up to 16000 octet/s
      * 0110 Up to 32000 octet/s
      * 0111 Up to 64000 octet/s
      * 1000 Up to 128000 octet/s
      * 1001 Up to 256000 octet/s
      * ...
      */
    
    kal_uint8 unused2;      /* Unused Bit */
    
    kal_uint8 precedence_class;
    /* 
      * Precedence Class
      * 001 High Priority
      * 010 Normal Priority
      * 011 Low Priority
      * ...
      */
    
    kal_uint8 unused3;      /* Unused Bit */
    
    kal_uint8 mean_throughput;
    /* 
      * Mean Throughput
      * 00001 Up to 100 octet/h
      * 00010 Up to 200 octet/h
      * 00011 Up to 500 octet/h
      * ...
      */
    
    kal_uint8 traffic_class;
    /* 
      * Traffic Class
      * 001 Conversational class
      * 010 Streaming class
      * 011 Interactive class
      * 100 Background class
      * ...
      */
    
    kal_uint8 delivery_order;
    /* 
      * Delivery Order
      * 01 With delivery order (yes)
      * 10 Without delivery order (no)
      * ...
      */
    
    kal_uint8 delivery_of_err_sdu;
    /* 
      * Delivery of erroneous SDUs
      * 001 No detect (-)
      * 010 Erroneous SDUs are delivered (yes)
      * 011 Erroneous SDUs are not delivered (no)
      * ...
      */
    
    kal_uint8 max_sdu_size;
    /* 
      * Maximum SDU Size
      * ...
      * 10010111 1502 octets
      * 10011000 1510 octets
      * 10011001 1520 octets
      * ...
      */

    kal_uint8 max_bitrate_up_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 max_bitrate_down_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 residual_bit_err_rate;
    /* 
      * Residual Bit Error Rate
      * 0001 5*10^-2
      * 0010 1*10^-2
      * 0011 5*10^-3
      * 0100 4*10^-3      
      * 0101 1*10^-3      
      * 0110 1*10^-4      
      * ...
      */
    
    kal_uint8 sdu_err_ratio;
    /* 
      * SDU Error Ratio
      * 0001 1*10^-2
      * 0010 7*10^-3
      * 0011 1*10^-3
      * 0100 1*10^-4     
      * 0101 1*10^-5      
      * 0110 1*10^-6      
      * ...
      */
    
    kal_uint8 transfer_delay; // see 24.008 Table 10.5.156
    
    kal_uint8 traffic_hndl_priority;
    /* 
      * Traffic Handling Priority
      * 01 Priority level 1
      * 10 Priority level 2
      * 11 Priority level 3
      * ...
      */
    
    kal_uint8 guarntd_bit_rate_up_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 guarntd_bit_rate_down_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 unused4; /* Unused Bit */
    
    kal_uint8 signalling_indication;
    /* 
      * Signaling Indication
      * 0 Not optimised for signalling traffic
      * 1 Optimised for signalling traffic
      * ...
      */
    
    kal_uint8 source_statistics_descriptor;
    /* 
      * Source Statistics Descriptor
      * 0000 Unknown
      * 0001 Speech
      * ...
      */
    
    kal_uint8 ext_max_bitrate_down_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 ext_guarntd_bit_rate_down_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 ext_max_bitrate_up_lnk; // see 24.008 Table 10.5.156
    
    kal_uint8 ext_guarntd_bit_rate_up_lnk; // see 24.008 Table 10.5.156
} nvram_editor_qos_struct;

typedef struct
{
    nvram_editor_qos_struct req_qos;
    nvram_editor_qos_struct min_qos;
} nvram_ef_abm_ps_qos_profile_struct;

/* } */

#define MAX_SYNCML_SV_PROFILES         3
#define MAX_SYNCML_USERNAME           32
#define MAX_SYNCML_PASSWORD           32
#define MAX_SYNCML_SERVER_ADDR_LEN   128
#define MAX_SYNCML_DATABASE_ADDR_LEN  64
#define MAX_SYNCML_PROFILE_NAME       32
#define MAX_SYNCML_PROXY_USERNAME     40    /*SRV_DTCNT_PROF_MAX_PX_AUTH_ID_LEN*/
#define MAX_SYNCML_PROXY_PASSWORD     40    /*SRV_DTCNT_PROF_MAX_PX_AUTH_PW_LEN*/
#define MAX_SYNCML_PROXY_ID_LEN       40    /*SRV_DTCNT_PROF_MAX_PX_ID_LEN*/
#define MAX_SYNCML_PROXY_IP_ADDR_LEN  16
#define MAX_SYNCML_PROXY_PORT_LEN      6
#define MAX_SYNCML_HASH_BUF_LEN		  16    /*SRV_DTCNT_PROVURL_HASH_LEN*/
#define MAX_SYNCML_DEV_NUM		   	   3    /* Device sync */
#define MAX_SYNCML_DEV_NAME_LEN   	  64    /* Device sync */
#define MAX_SYNCML_REMOTE_ANCHOR_SIZE 17

#define FACTORY_RESTORE_DEFAULT_SPEED_DIAL                  0
#define FACTORY_RESTORE_DEFAULT_CONTRAST_SUBLCD_LEVEL_VALUE    7
#define FACTORY_RESTORE_DEFAULT_CONTRAST_MAINLCD_LEVEL_VALUE   7
#define FACTORY_RESTORE_DEFAULT_WELCOME_TEXT_STATUS            0
#define  FACTORY_RESTORE_DEFAULT_APHORISM_TEXT_STATUS       1

#ifdef __MMI_OP11_HOMESCREEN_US__
#define FACTORY_RESTORE_DEFAULT_OWNER_NO_STATUS             1
#else
#define FACTORY_RESTORE_DEFAULT_OWNER_NO_STATUS             0
#endif  /* __MMI_OP11_HOMESCREEN_US__ */

#define FACTORY_RESTORE_DEFAULT_STATUS_SHOW_DT_TIME_STATUS     1
#define FACTORY_RESTORE_DEFAULT_AUTOUPDATE_TIME_VALUE       0
#define FACTORY_RESTORE_DEFAULT_SCREENSAVER_STATUS          0
#define FACTORY_RESTORE_DEFAULT_SCREENSAVER_WAITING_TIME    30
#define  FACTORY_RESTORE_DEFAULT_PROFILES_ACTIVATED_ID         0

#define  MAXIMUM_EMS_DOWNLOAD_IMELODY     5
#define  MAXIMUM_DOWNLOAD_IMAGES          15
#define  MAXIMUM_DOWNLOAD_AUDIO           15
#define  MAXIMUM_COMPOSED_RINGTONE        5

#define MAXIMUM_FULLSCREEN_FACTORY_IMAGES       7
#define MAXIMUM_ASSOCIATED_FACTORY_IMAGES       5
#define  MAXIMUM_FACTORY_TONES            5
#define  MAXIMUM_FACTORY_IMELODY          5

#define FACTORY_RESTORE_DEFAULT_AUDPLY_LIST_AUTO_GEN  1
#define FACTORY_RESTORE_DEFAULT_AUDPLY_VOLUME  4
#define FACTORY_RESTORE_DEFAULT_AUDPLY_PREFER_LIST  0
#define FACTORY_RESTORE_DEFAULT_AUDPLY_SKIN  0
#define FACTORY_RESTORE_DEFAULT_AUDPLY_REPEAT  0
#define FACTORY_RESTORE_DEFAULT_AUDPLY_SHUFFLE  0
#define FACTORY_RESTORE_DEFAULT_AUDPLY_BACKGROUND_PLAY  1

#define FACTORY_RESTORE_DEFAULT_SNDREC_STORAGE  0
#define FACTORY_RESTORE_DEFAULT_SNDREC_FORMAT  0
#define FACTORY_RESTORE_DEFAULT_SNDREC_QUALITY 0 

#define FACTORY_RESTORE_DEFAULT_FMRDO_BACKGROUND_PLAY  1
#define FACTORY_RESTORE_DEFAULT_FMRDO_SKIN  0
#define FACTORY_RESTORE_DEFAULT_FMRDO_VOLUME  4

#define FACTORY_RESTORE_DEFAULT_MTPNP_SETTING   0

#define EM_NW_EVENT_MAX_NUM_OF_EVENTS         100

#define DM_TFTP_MAX_SERVER_NAME_LEN     64
#define DM_TFTP_MAX_FILENAME_LEN        260
#define DM_HTTP_MAX_URL_LEN             128

#define VIDEO_MAX_HISTORY_URL_LEN       256
#define VIDEO_MAX_HISTORY_NAME_LEN      40

#define VIDEO_MAX_PREDEFINED_URL_LEN    128
#define VIDEO_MAX_PREDEFINED_NAME_LEN   40

#define VIDEO_MAX_PROFILE_NAME_LEN      40
#define VIDEO_MAX_PROFILE_NAME_BUF_SIZE ((VIDEO_MAX_PROFILE_NAME_LEN + 1) * ENCODING_LENGTH)
#define VIDEO_PORXY_IP_COUNT            4   /* ip4 */

#define STREAMING_OTA_HASH_LEN          16
/* Note application */
#define MMI_MAX_NOTE_ITEM	50	/* can't bigger than MAX_SUB_MENUS */
#define MMI_MAX_NOTE_LEN	100		

/*
 * This is the defined value of supported preferred networks in MMI.
 * The maximum supported preferred networks in SMU is MAX_SUPPORT_EF_PLMNSEL_REC. (sim_common.h)
 * MMI can support at most the same as MAX_SUPPORT_EF_PLMNSEL_REC.
 * Some specific SIMs allocate too many preferred networks but are not able to access all of them.
 * Therefore, MMI default allows 50 preferred networks.
 */
#ifdef LOW_COST_SUPPORT
#define MAX_PREFFERED_PLMN  24
#else
#define MAX_PREFFERED_PLMN  50
#endif /* LOW_COST_SUPPORT */

typedef struct
{
    kal_uint16 ActivityId[MAX_ACTIVITIES_PER_DAY];
    float CaloriePerActivity[MAX_ACTIVITIES_PER_DAY];
    kal_uint16 MinutesPerActivity[MAX_ACTIVITIES_PER_DAY];
} ActivityDetails;

typedef struct
{
    kal_uint8 shortcut_state;
    kal_uint16 menu_id;
} shct_item_struct;

typedef struct
{
    unsigned char   Hour;
    unsigned char   Min;
    unsigned char   State;
    unsigned short   AudioValue;
    unsigned char   AlertType;
}mmi_hijri_azaan_nvram_struct;

#define NVRAM_AZAAN_ALARM_TOTAL  1
#define NVRAM_AZAAN_ALARM_SIZE      NUM_OF_AZAAN * sizeof(mmi_hijri_azaan_nvram_struct)


#define MMI_MEDPLY_MAX_PROFILE_NAME_LEN          (40)
#define MMI_MEDPLY_MAX_PROFILE_NAME_BUF_SIZE     (MMI_MEDPLY_MAX_PROFILE_NAME_LEN + 1)*ENCODING_LENGTH
#define MMI_MEDPLY_PROXY_IP_COUNT                (4)
#define MEDPLY_MAX_HISTORY_NAME_LEN              (40)
#ifdef __OP11__
#define MEDPLY_MAX_HISTORY_URL_LEN               (256 * 2)
#else
#define MEDPLY_MAX_HISTORY_URL_LEN               (256)
#endif
/************************************ 
* PLUTO MMI NVRAM ITEM
************************************/

/* PLUTO MMI [BYTE -8bit] */
typedef enum
{
    NVRAM_PHONELOCK_INDEX,                  /* 0 */
    NVRAM_AUTOLOCK_TIMEOUT_INDEX,
    NVRAM_SETTING_PHONELOCK,
    NVRAM_SETTING_AUTOKEYPADLOCK,
    NVRAM_SETTING_AUTOKEYPADLOCK_TIME,      /* 5 */
    NVRAM_SETTING_WELCOME_TEXT,
    NVRAM_SETTING_APHORISM_TEXT,
    NVRAM_SETTING_SPEED_DIAL,
    NVRAM_SETTING_CONTRAST_LEVEL,           /* 10 */
    NVRAM_SETTING_AUTOUPDATE_DT_TIME,
    NVRAM_SETTING_OWNER_NO,
    NVRAM_SETTING_STATUS_DT_TIME,
    NVRAM_SETTING_LANG,
    NVRAM_CALLSET_CALLERID,
    NVRAM_CALLSET_SIM2_CALLERID,
    NVRAM_CALLSET_CUG_ACTIVATED_ID,
    NVRAM_CALLSET_CUG_SIM2_ACTIVATED_ID,
    NVRAM_CALLSET_CTR_MODE,
    NVRAM_CALLSET_ANSWER_MODE,
    NVRAM_CALLSET_CALL_TIME_DISPLAY,
    NVRAM_CALLSET_AUTOREDIAL,
    NVRAM_CALLSET_REJ_BY_SMS,
    NVRAM_CALLSET_CONNECT_NOTICE,
    NVRAM_CALLSET_IP_NUMBER_SETTING,
    NVRAM_CALLSET_IP_NUMBER_SETTING_SIM2,
    NVRAM_CALLSET_BLACKLIST_MODE,
    NVRAM_CALLSET_WHITELIST_MODE,
    NVRAM_CALLSET_QUICK_END_FLAG,
    NVRAM_CALLSET_CTM_MODE,
    NVRAM_CALLSET_CTM_SETTING,
    NVRAM_SETTING_CONTRAST_SUBLCD_LEVEL,
    NVRAM_HELP_ENABLED_FLAG,
    NVRAM_PROFILES_ACTIVATED,
    NVRAM_CH_QUICK_END_FLAG,
    NVRAM_SMSCOUNTERS_SENT,                 /* 20 */
    NVRAM_SMSCOUNTERS_RECD,
    NVRAM_DOWNLOAD_IMAGE,
    NVRAM_DOWNLOAD_TONE,
    CURRENT_CITY,
    FOREIGN_CITY,
    TIME_FORMAT,                            /* 25 */
    DATE_FORMAT,
    NVRAM_GAME_SNAKE_LEVEL,
    LUNAR_CALENDAR,
    NVRAM_DOWNLOAD_CITY1,
    NVRAM_DOWNLOAD_CITY2,                   /* 30 */
    NVRAM_SCREENSAVER_STATUS,
    NVRAM_SCREENSAVER_WAITING,
    NVRAM_GAME_RICHES_STATUS,               /* 35 */
    NVRAM_RICHES_OPTION_STATUS,
    NVRAM_PROFILES_LASTACTIVATED,
    TOH_CURRENT_LEVEL,
    SMASH_CURRENT_LEVEL,
    NVRAM_SMS_ACTIVE_PROFILE_INDEX,         /* 40 */
    SMS_CONCAT_MSG_REF_VALUE,
    NVRAM_COLORBALLS_OPTION_STATUS,
    NVRAM_PROFILES_LOUDSPKR,
    RESTORE_DEFAULT_SPEED_DIAL,
    RESTORE_DEFAULT_CONTRAST_SUBLCD_LEVEL,  /* 45 */
    RESTORE_DEFAULT_CONTRAST_LEVEL,
    RESTORE_DEFAULT_WELCOME_TEXT,
    RESTORE_DEFAULT_APHORISM_TEXT,
    RESTORE_DEFAULT_OWNER_NO,
    RESTORE_DEFAULT_STATUS_SHOW_DT_TIME,    /* 50 */
    RESTORE_DEFAULT_AUTOUPDATE_TIME,
    RESTORE_DEFAULT_SCREENSAVER_STATUS,
    RESTORE_DEFAULT_SCREENSAVER_WAITING,
    RESTORE_DEFAULT_PROFILES_ACTIVATED,
    RESTORE_DEFAULT_TIME_FORMAT,            /* 55 */
    RESTORE_DEFAULT_DATE_FORMAT,
    NVRAM_DOWNLOAD_MELODY,
    NVRAM_NORMAL_MODE_VOICE_LEVEL,
    NVRAM_LDSPK_MODE_VOICE_LEVEL,
    NVRAM_HDSET_MODE_VOICE_LEVEL,           /* 60 */
    NVRAM_NITZTIMEZONE,
    NVRAM_WALLPAPER_ROTATE_STATUS,
    NVRAM_GX_MAJUNG_LEVEL,
    NVRAM_GX_MEMORY_LEVEL,
    NVRAM_DEFAULT_USB_APP,                  /* 65 */
    NVRAM_FMRDO_BACKGROUND_PLAY,
    NVRAM_FMRDO_SKIN,
    NVRAM_FMRDO_VOLUME,
    NVRAM_AUDPLY_LIST_AUTO_GEN,
    NVRAM_AUDPLY_VOLUME,                    /* 70 */
    NVRAM_AUDPLY_PREFER_LIST,
    NVRAM_AUDPLY_SKIN,
    NVRAM_AUDPLY_REPEAT,
    NVRAM_AUDPLY_SHUFFLE,
    NVRAM_AUDPLY_BACKGROUND_PLAY,           /* 75 */
    NVRAM_SNDREC_STORAGE,
    NVRAM_SNDREC_FORMAT,
    NVRAM_SET_MAINMENU_STYLE,
    NVRAM_EF_FMGR_SORT_OPTION_LID,          /* 80 */
    NVRAM_GFX_AUDIO_SETTING,
    NVRAM_GFX_AUDIO_VOLUMN,
    NVRAM_GFX_VIBRATION_SETTING,
    NVRAM_GX_PUZZLE_LEVEL,
    NVRAM_GX_STAIR_LEVEL,                   /* 85 */
    NVRAM_SETTING_DEF_ENCODING,
    NVRAM_STOPWATCH_TOTAL_RECORDS,
    NVRAM_JAVA_SETTING_SOUND,
    NVRAM_JAVA_SETTING_PROF_ID,
    NVRAM_JAVA_SETTING_BKLIGHT_ID,    
    NVRAM_ALM_SNOOZE_TIME,                  /* 90 */
    NVRAM_VDOPLY_VOLUMN,
    NVRAM_SETTING_OPN_DISPLAY,
    RESTORE_DEFAULT_OPN_DISPLAY,
    NVRAM_VRSD_RCG_SETTING,
    NVRAM_PHB_SPEAK_NAME_ON_OFF,            /* 100 */
    NVRAM_JAVA_SETTING_VIB,
    NVRAM_JAVA_SETTING_BKL,
    NVRAM_JAVA_SETTING_LED,
    NVRAM_VRSI_RCG_SETTING,
    NVRAM_IRDA_STORAGE,                     /* 105 */
    NVRAM_PEN_SPEED,
    NVRAM_PEN_COLOR,
    NVRAM_SETTING_DT_DST,
    NVRAM_SSC_DISABLE_FLAG,
    NVRAM_ABREPEAT_VOLUME,                  /* 110 */
    NVRAM_IMGVIEW_VIEW_SYTLE,
    NVRAM_ABREPEAT_STORAGE,
    NVRAM_WC_DST,
    NVRAM_GFX_BACKGROUND_MUSIC_SETTING,
    NVRAM_GFX_SOUND_EFFECT_SETTING,         /* 115 */
    NVRAM_PHNSET_IDLE_CLOCK_TYPE,
    RESOTRE_DEFAULT_IDLE_CLOCK_TYPE,
    NVRAM_FMRDO_RECORD_FORMAT,
    NVRAM_FMRDO_RECORD_STORAGE,
    NVRAM_BYTE_FONT_SIZE,                   /* 120 */
    NVRAM_BYTE_LED_SETTING,
    NVRAM_BYTE_BL_SETTING_LEVEL,
    NVRAM_BYTE_BL_SETTING_HFTIME,
    NVRAM_BYTE_COVER_CLOSE_BACK_TO_IDLE,
    NVRAM_BYTE_COVER_CLOSE_REJECT_CALL,     /* 125 */
    NVRAM_BYTE_COVER_CLOSE_KEYPAD_LOCK,
    NVRAM_MOTION_DICE_DICE_COUNT,
    NVRAM_MOTION_DICE_EVENT_ON,
    NVRAM_AUDPLY_SPECTRUM_DISPLAY_STYLE,
    NVRAM_FTP_ACCESS_RIGHT,
    NVRAM_CCONV_SETTING_SELECTED_LOCAL_CURRENCY,
    NVRAM_CCONV_SETTING_SELECTED_FOREIGN_CURRENCY,
    NVRAM_CCONV_COMPUTE_SELECTED_LOCAL_CURRENCY,
    NVRAM_CCONV_COMPUTE_SELECTED_FOREIGN_CURRENCY,
    NVRAM_CCONV_SETTING_NOUSE1,
    NVRAM_CCONV_SETTING_NOUSE2,
    NVRAM_CCONV_SETTING_NOUSE3,
    NVRAM_CCONV_SETTING_NOUSE4,
    NVRAM_BYTE_AUDIO_REVERB_INDEX,
    NVRAM_POC_ACTIVITY_PRESENT,
    NVRAM_POC_ACTIVITY,
    NVRAM_POC_MOOD_PRESENT,
    NVRAM_POC_MOOD,
    NVRAM_POC_ANS_MODE,
    NVRAM_POC_CALL_BAR_MODE,
    NVRAM_POC_ALERT_BAR_MODE,
    NVRAM_POC_MAO_MODE,
    NVRAM_POC_PRIVACY_MODE,
    NVRAM_POC_START_BOOTUP,
    NVRAM_POC_USE_XDM,
    NVRAM_POC_USE_PRES,
    NVRAM_POC_PROFILE_ACTIVE,
    NVRAM_JAVA_SETTING_VK,
    NVRAM_EM_SWDBG_MODE,
    NVRAM_GX_FRUIT_LEVEL,
    NVRAM_GX_VSMJ_LEVEL,
    NVRAM_BRW_SHOW_IMAGE_ON_OFF,
    NVRAM_BRW_SHOW_HISTORY_AS,
    NVRAM_PH_ACTIVATED_WAP_PROFILE_INDEX,
    NVRAM_PH_ACTIVATED_MMS_PROFILE_INDEX,
    NVRAM_BRW_SHOW_SERVICE_INBOX_MSG,
    NVRAM_FMRDO_SPEAKER_STATUS,
    NVRAM_FMRDO_RDS_SETTING,
    NVRAM_FMRDO_RDS_TP_SETTING,
    NVRAM_FMRDO_RDS_AF_SETTING,
    NVRAM_AUTO_VM_SETTING_VM_SUPPORT,
    NVRAM_AUTO_VM_SETTING_AUTO_VM,    
    NVRAM_AUDPLY_LYRICS_DISPLAY,
    NVRAM_SWFLASH_STORAGE,
    NVRAM_SWFLASH_DATA_ACCOUNT,
    NVRAM_SWFLASH_VOLUME,
	NVRAM_BRW_IMAGE_SELECTION_ON_OFF,
    NVRAM_VOIP_PROF_ACTIVATED,
    NVRAM_CLNDR_PERIOD_INTERVAL,
    NVRAM_UC_PREFERRED_MSG_TYPE,
    NVRAM_VDOPLY_ACTIVE_PROFILE_IDX,  
    NVRAM_VDOPLY_PROXY_IP1,          /* not used */
    NVRAM_VDOPLY_PROXY_IP2,          /* not used */    
    NVRAM_VDOPLY_PROXY_IP3,          /* not used */
    NVRAM_VDOPLY_PROXY_IP4,          /* not used */
    NVRAM_VDOPLY_PROXY_ON_OFF,       /* not used */   
    NVRAM_NETSET_PREFERRED_MODE,
    NVRAM_BYTE_AUDIO_SURROUND_INDEX,
	NVRAM_BRW_SETTINGS_RENDER_MODE,
	NVRAM_BRW_SETTINGS_NAVIGATE_MODE,
	/*Added by shariq on 150507 for font size*//*START*/
	NVRAM_BRW_SETTINGS_FONT_SIZE,
	/*Added by shariq on 150507 for font size*//*END*/
	NVRAM_BRW_SETTINGS_THUMBNAIL,
	NVRAM_BRW_SETTINGS_SCREEN_SIZE,
	NVRAM_BRW_SETTINGS_ENCODE_METHOD_INDEX,
	NVRAM_BRW_SETTINGS_SHOW_VIDEO,
	NVRAM_BRW_SETTINGS_PLAY_SOUND,
	NVRAM_BRW_SETTINGS_LOAD_UNKOWN_MEDIA,
	NVRAM_BRW_SETTINGS_CSS,
	NVRAM_BRW_SETTINGS_SCRIPTS,
	NVRAM_BRW_SETTINGS_CACHE,
	NVRAM_BRW_SETTINGS_COOKIE,
	NVRAM_BRW_SETTINGS_SHORTCUTS,
	NVRAM_BRW_SETTINGS_SECURITY_WARNINGS,  
	NVRAM_BRW_SETTINGS_SEND_DEVICE_ID,
	/* __MMI_BRW_POST_SESSION_INFO__ */ 
	NVRAM_BRW_SETTINGS_POST_SESSION_INFO,
	/* __MMI_BRW_POST_SESSION_INFO__ */ 
	NVRAM_BRW_RECENT_PAGES_SORTING_METHOD,
	NVRAM_BRW_SETTINGS_SELECT_SIM_SETTING,
    NVRAM_DM_SELF_REGISTER_RESULT,
    NVRAM_DM_SELF_REGISTER_SIM,
    NVRAM_DICTIANRY_TTS_ON_OFF,
    NVRAM_DICTIANRY_TTS_SPEAK_SPEED,
    NVRAM_DICTIANRY_TTS_SPEAK_VOLUME,
    NVRAM_SNDREC_QUALITY,
    NVRAM_FMRDO_RECORD_QUALITY,
    NVRAM_LANGLN_DRIVE,
    NVRAM_LANGLN_WK_REMINDER,
    NVRAM_LANGLN_WK_RMD_HOUR,
    NVRAM_LANGLN_WK_RMD_MIN,
    NVRAM_LANGLN_DK_TTS_STATUS,
    NVRAM_DM_OTA_PROFILE_INIT,
    NVRAM_DM_FOTA_UPDATE_FLAG,
    NVRAM_DM_LOCK_STATUS,
    NVRAM_SMS_BACKGROUND_SAVE_TO_SENT,
    NVRAM_LANGLN_DK_TTS_VOLUME,

    NVRAM_MOTION_DICE_SOUND_ON,
    NVRAM_MOTION_DICE_VOL,
    NVRAM_VT_ANSWER_MODE,
    NVRAM_VT_DIAL_MODE,
    NVRAM_VT_AUDIO_OPT_MIC,
    NVRAM_VT_AUDIO_OPT_SPEAKER,

    NVRAM_VOIP_LOG_ACTIVATE,
    NVRAM_VOIP_LOG_STORAGE,
    NVRAM_VOIP_LOG_DELETE_TIME,
    NVRAM_JAVA_SETTING_NET_ICON,
	NVRAM_BRW_SHOW_IMAGE_MODE,
    NVRAM_BRW_ACTIVATED_WAP_PROFILE_INDEX,
    NVRAM_BRW_ACTIVATED_MMS_PROFILE_INDEX,
    NVRAM_BYTE_AUDIO_EFFECT_INDEX,
    NVRAM_CLNDR_VIEW_TYPE,
    NVRAM_FMRDO_CHANNEL_MONO,
    NRRAM_GPS_LOGGING_SWITCH,
    NVRAM_SET_CALENDAR_IDLESCREEN,
    NVRAM_SET_DUALCLOCK_IDLESCREEN,
    NVRAM_MTPNP_SETTING,
    NVRAM_MTPNP_STATUS,
    NVRAM_SLAVE_ACTIVE_PROFILE_IDX,
    NVRAM_SLAVE_NORMAL_MODE_VOICE_LEVEL,
    NVRAM_SLAVE_LDSPK_MODE_VOICE_LEVEL,
    NVRAM_SLAVE_HDSET_MODE_VOICE_LEVEL,
    NVRAM_CARD2_OWNER_NUMBER,
    NVRAM_MTPNP_SMS_SHOW_STYLE,
    
    /* below define restore value */
    RESTORE_DEFAULT_MTPNP_SETTING,
    RESTORE_DEFAULT_CARD2_OWNER_NUMBER,
    RESTORE_DEFAULT_MTPNP_SMS_SHOW_STYLE,
    /* restore value */

    NVRAM_AUDPLY_SINGLE_VOL,
    NVRAM_SET_DCD_SETTING,
    NVRAM_BT_RECEIVE_DRIVE,
    NVRAM_BT_RECEIVE_DRIVE_ASK,
    NVRAM_SET_SCREFFECT_STYLE,      /* __MMI_SCREEN_SWITCH_EFFECT__ */
    NVRAM_SET_DIAL_STYLE,         /* __MMI_MOTION_DIAL__ */
    NRRAM_CAMCO_BEFORE_SWITCH_CAPSIZE,
    NVRAM_MEMORY_PROFILING_ENABLE,
    NVRAM_JAVA_SETTING_SIM_ID,
    NVRAM_JAVA_SETTING_PROF2_ID,

    NVRAM_EM_AGPSLOG_ENABLED,

    NVRAM_SET_HOMESCREEN,

    /*Media Player*/
	NVRAM_MEDPLY_PREFER_LIST,
    NVRAM_MEDIA_PLAYER_VOLUME,
    NVRAM_MEDIA_PLAYER_SINGLE_VOLUME,
    NVRAM_MEDIA_PLAYER_MUTE,
    NVRAM_MEDIA_PLAYER_SINGLE_MUTE,
    NVRAM_EF_MEDPLY_SETTINGS_ACTIVE_PROFILE_INX,
    NVRAM_EF_MEDPLY_SETTINGS_REPEAT_PLAY,
    NVRAM_EF_MEDPLY_SETTINGS_SHUFFLE,

    NVRAM_DT_SEP_FORMAT,
    NVRAM_MOTION_DJ_VOLUMN,    
    NVRAM_RMGR_DB_CHECK,

	NVRAM_MSG_HS_VOICEMAIL_RECENT_EVENT_ID,	/* __MMI_OP11_HOMESCREEN__ */
    NVRAM_SMS_MISCELL_SETTING, /* __EMS_NON_STD_7BIT_CHAR__ __EMS_DYNAMIC_SEG_NUM__ */

    /* Activate IPSec on start */
    NVRAM_IPSEC_ACTIVATE_ON_START,
    NVRAM_IPSEC_ACTIVATE_PROF_INDEX,

    NVRAM_VENDOR_TEST1,

    /* Internet Application */
    NVRAM_EM_INET_ACTIVE_USER_AGENT,
    NVRAM_EM_INET_ACTIVE_ACCEPT_HEADER,
    NVRAM_EM_INET_ACTIVE_MMS_VERSION,

    NVRAM_VODAFONE_FLAG,
    NVRAM_UDX_DATA_USE_WAY, /* __MMI_UDX_SUPPORT__ */

    NVRAM_BYTE_AUDIO_BASS_ENHANCEMENT_INDEX,

    NVRAM_AUTOUPDATE_DT_STATUS,

    NVRAM_HIJRI_CORRECTION,

    /* VF UE - PB copy SIM when startup */
    NVRAM_PHB_STARTUP_COPY_SIM,
    NVRAM_PHB_STARTUP_COPY_SIM2,

	NVRAM_NETSET_CELL_INFO_STATUS,
    NVRAM_VT_MIRROR,
    NVRAM_VT_VIDEO_QUALITY,
    
    NVRAM_IME_SMART_MODE_CONFIG,
    NVRAM_IME_AUTO_CAPITALIZATION_FLAG,
    NVRAM_IME_AUTO_COMPLETION_FLAG,
    NVRAM_IME_ALPHABETIC_WORD_PREDICTION_FLAG,
    NVRAM_IME_CHINESE_WORD_PREDICTION_FLAG,
    NVRAM_IME_INPUT_PATTERN,
    NVRAM_PIM_DEFAULT_ACCOUNT,

    NVRAM_VUI_PHNSET_HOMESCREEN,
    NVRAM_VUI_PHNSET_MAINMENU_EFFECT,

    NVRAM_SETTING_AUTO_TEST,
    
    NVRAM_DUALMODE_MODE_SWITCH_TYPE,
    
    NVRAM_WAP_PROF_ACTIVE_WAP_WLAN_PROFILE_INDEX,

    NVRAM_CLNDR_INFO_VAL,

    NVRAM_SMS_PREFER_STORAGE,

    NVRAM_GPS_PAYLOAD_STORAGE,

    NVRAM_IDLE_DIALER_SEARCH_SETTING,

    NVRAM_CERTMAN_OCSP_SETTING,
    NVRAM_SETTING_PREFER_MMS_STORAGE_MODE,
    NVRAM_UM_CONVERSATION_BOX_SETTING, 


    NVRAM_OP11_HS32_CLOCK_SIZE,   /* __MMI_OP11_HOMESCREEN_0301__ || __MMI_OP11_HOMESCREEN_0302__*/
    NVRAM_OP11_HS32_HISTORY_CHANGED,/* __MMI_OP11_HOMESCREEN_0301__ || __MMI_OP11_HOMESCREEN_0302__*/
    NVRAM_OP11_HS32_SHCT_PHB_CHANGED,/* __MMI_OP11_HOMESCREEN_0301__ || __MMI_OP11_HOMESCREEN_0302__*/
    NVRAM_OP11_HS32_SHCT_EML_CHANGED, /* __MMI_OP11_HOMESCREEN_0301__ || __MMI_OP11_HOMESCREEN_0302__*/
    NVRAM_OP11_HS32_VIEW_MODE, /* (defined(__MMI_OP11_HOMESCREEN_0302__) && !defined(__MMI_OP11_HOMESCREEN_US__)) */
    NVRAM_FMRDO_MUTE,
    
    NVRAM_WGTMGR_WGT_SORT_ORDER,
    NVRAM_WGTMGR_WGT_NETWORK_ACCESS,
    NVRAM_WGTMGR_WGT_NEED_UPDATE_NAME,
    NVRAM_WGTMGR_PRE_INSTALL_DONE,
    NVRAM_PHB_NAME_DISPLAY_TYPE,

    /* don not remove last element */
    NVRAM_LAST_BYTE_ELEMENT
} BYTEDATA;

/* PLUTO MMI [SHORT - 16bit] */
typedef enum
{
    NVRAM_SHORTDATA1_INDEX,             /* 0 */
    NVRAM_SHORTDATA2_INDEX,
    NVRAM_SNAKE_LEVEL1_SCORE,
    NVRAM_SNAKE_LEVEL2_SCORE,
    NVRAM_SNAKE_LEVEL3_SCORE,
    NVRAM_SNAKE_LEVEL4_SCORE,           /* 5 */
    NVRAM_F1RACE_SCORE,
    NVRAM_CURRENT_SCREENSVER_ID,
    NVRAM_PHB_STORAGE_LOCATION,
    NVRAM_FUNANDGAMES_SETWALLPAPER,
    NVRAM_GAME_DOLL_GRADE,              /* 10 */
    TOH_LEVEL1_HIGHEST_SCORE,
    TOH_LEVEL2_HIGHEST_SCORE,
    TOH_LEVEL3_HIGHEST_SCORE,
    TOH_LEVEL4_HIGHEST_SCORE,
    NVRAM_RICHES_EASY_SCORE,            /* 15 */
    NVRAM_RICHES_NORMAL_SCORE,
    NVRAM_RICHES_HARD_SCORE,
    NVRAM_COLORBALLS_EASY_SCORE,
    NVRAM_COLORBALLS_NORMAL_SCORE,
    NVRAM_COLORBALLS_HARD_SCORE,        /* 20 */
    NVRAM_SMASH_NORMAL_SCORE,
    NVRAM_SMASH_SPEEDY_SCORE,
    NVRAM_SMASH_ACCURACY_SCORE,
    NVRAM_CALLSET_CTR_TIME,
    NVRAM_CALLSET_QUICK_END_TIME,
    NVRAM_SETTING_PREFER_INPUT_METHOD,  /* 25 */
    RESTORE_DEFAULT_CURRENT_SCREENSVER_ID,
    RESTORE_DEFAULT_FUNANDGAMES_SETWALLPAPER,
    RESTORE_PREFER_INPUT_METHOD,
    NVRAM_GX_NINJA_SCORE,
    NVRAM_GX_MAJUNG_EASY_SCORE,         /* 30 */
    NVRAM_GX_MAJUNG_NORMAL_SCORE,
    NVRAM_GX_MAJUNG_HARD_SCORE,
    NVRAM_GX_MEMORY_EASY_SCORE,
    NVRAM_GX_MEMORY_NORMAL_SCORE,
    NVRAM_GX_MEMORY_HARD_SCORE,         /* 35 */
    NVRAM_GX_COPTER_SCORE,
    NVRAM_GX_PUZZLE_EASY_SCORE,
    NVRAM_GX_PUZZLE_NORMAL_SCORE,
    NVRAM_GX_PUZZLE_HARD_SCORE,
    NVRAM_GX_PANDA_SCORE,               /* 40 */
    NVRAM_GX_STAIR_EASY_SCORE,
    NVRAM_GX_STAIR_NORMAL_SCORE,
    NVRAM_GX_STAIR_HARD_SCORE,
    NVRAM_GX_UFO_SCORE,
    NVRAM_GX_DANCE_SCORE,               /* 45 */
    NVRAM_CAM_FILENAME_SEQ_NO,
    NVRAM_IMG_VIEWER_FILE_PATH,
    NVRAM_VDOREC_FILENAME_SEQ_NO,
    NVRAM_VDOPLY_SNAPSHOT_SEQ_NO,
    NVRAM_RESTORE_POWER_ON_DISPLAY,     /* 50 */
    NVRAM_RESTORE_POWER_OFF_DISPLAY,
    NVRAM_CURRENT_POWER_ON_DISPLAY,
    NVRAM_CURRENT_POWER_OFF_DISPLAY,
    NVRAM_POWER_ON_DISPLAY_SUCCESS,
    NVRAM_POWER_OFF_DISPLAY_SUCCESS,    /* 55 */
    NVRAM_SCREENSAVER_DISPLAY_SUCCESS,
    NVRAM_POWER_ON_FORCE_STOP_TIME,
    NVRAM_POWER_OFF_FORCE_STOP_TIME,
    NVRAM_SETWALLPAPER_SUB,
    RESTORE_DEFAULT_SETWALLPAPER_SUB,   /* 60 */
    NVRAM_GX_MAGICSUSHI_SCORE,
    NVRAM_SCR_SNAPSHOT_SEQ_NO,
    NVRAM_SETTING_TVOUT_FORMAT,
    NVRAM_SETTING_TVOUT_Y_GAMMA,        /* 65 */
    NVRAM_AUDIO_MIC_VOLUME_SET,
    NVRAM_SMS_CONCAT_MSG_REF_VALUE,
    NVRAM_GX_FRUIT_NO_USE,
    NVRAM_GX_FRUIT_EASY_SCORE,
    NVRAM_GX_FRUIT_NORMAL_SCORE,        /* 70 */
    NVRAM_GX_FRUIT_HARD_SCORE,
    NVRAM_AUDIO_SPEED_VALUE,
    NVRAM_SETTING_TVOUT_U,
    NVRAM_SETTING_TVOUT_V,
    NVRAM_IMGVIEW_STORAGE,              /* 75 */
    NVRAM_VDOPLY_STORAGE,
    NVRAM_VDOPLY_OPTION_BRIGHTNESS,
    NVRAM_VDOPLY_OPTION_CONTRAST,
    NVRAM_VDOPLY_OPTION_REPEAT,
    NVRAM_ALARM_FIRSTSAVE,              /* 80 */
    NVRAM_SMSCOUNTERS_SENT_SHORT,
    NVRAM_SMSCOUNTERS_RECD_SHORT,
    NVRAM_GX_VSMJ_LEVEL1,
    NVRAM_GX_VSMJ_LEVEL2,
    NVRAM_GX_VSMJ_LEVEL3,               /* 85 */
    NVRAM_IMGTILE_FILENAME_SEQ_NO,
    NVRAM_BRW_TIME_OUT_VALUE,
    NVRAM_EBOOK_NFO_LID,
    NVRAM_EBOOK_LAST_HIGHLIGHT_ID,
    NVRAM_BARCODE_FILENAME_SEQ_NO,      /* 90 */
    NVRAM_SETTING_TVOUT_AUD_PATH,
    NVRAM_FMRDO_LAST_CHANNEL,
    NVRAM_TIMEZONE,
    NVRAM_FRN_TIMEZONE,
    NVRAM_FM_FM_RADIO_CHANNEL_1,
    NVRAM_FM_FM_RADIO_CHANNEL_2,        /* 95 */
    NVRAM_FM_FM_RADIO_CHANNEL_3,
    NVRAM_PHB_NAME_LIST_FILTER,
    NVRAM_JMMS_LANGUAGE_CHANGE,
    NVRAM_VDOPLY_STREAM_SEQ_NO,
    NVRAM_CHIST_HAVE_MISSED_CALL,       /* 100 */
    NVRAM_SETTING_WRITING_LANG,
    NVRAM_SETTING_WRITING_LANG_SUB,
	//NVRAM_BRW_SETTINGS_ZOOM_VALUE,
    NVRAM_UMMS_LANGUAGE_CHANGE,
    NVRAM_PHB_VCARD_VERSION,
	NVRAM_BRW_PAGE_TIME_OUT_VALUE,
    NVRAM_EDITABLE_EQ_RENAME_FLAG,
    NVRAM_SLAVE_SMSCOUNTERS_SENT,
    NVRAM_SLAVE_SMSCOUNTERS_RECV,
    NVRAM_SLAVE_CHIST_HAVE_MISSED_CALL,    /* 110 */
    NVRAM_CHIST_NUM_MISSED_CALL_BEFORE_VIEW,
    NVRAM_AUDPLY_LAST_PLAYLIST_INDEX,
    NVRAM_WAP_PROF_ACTIVE_WAP_PROFILE_INDEX,
    NVRAM_WAP_PROF_ACTIVE_MMS_PROFILE_INDEX,
    NVRAM_SPA_LAST_SIM_STATUS,
    NVRAM_SPA_CUR_SIM_STATUS,
    NVRAM_SPA_EF_PROCESSED_FLAGS,
    NVRAM_PHB_SAVE_CONTACT_NOTIFY,

    NVRAM_MEDPLY_LAST_PLAYLIST_INDEX,
    NVRAM_MEDPLY_SNAPSHOT_SEQ_NO,
    NVRAM_EC_DEFAULT_CITY,

    NVRAM_SEARCH_WEB_BAIDU_HOT_KEY_SETTING,
    NVRAM_SEARCH_WEB_BAIDU_DEFAULT_CATEGORY,

    NVRAM_WGTMGR_WGT_SEQ_ID,
    NVRAM_WGTMGR_WGT_DATA_ACCOUNT_FIRST,
    NVRAM_WGTMGR_WGT_DATA_ACCOUNT_LAST,

    NVRAM_WAP_PROF_ACTIVE_WAP_SIM1_DTCNT_INDEX,
    NVRAM_WAP_PROF_ACTIVE_WAP_SIM2_DTCNT_INDEX,
    NVRAM_WAP_PROF_ACTIVE_MMS_SIM1_DTCNT_INDEX,
    NVRAM_WAP_PROF_ACTIVE_MMS_SIM2_DTCNT_INDEX,

    /* don not remove last element */
    NVRAM_LAST_SHORT_ELEMENT
} SHORTDATA;

/* PLUTO MMI [DOUBLE - 64bit] */
typedef enum
{
    CURRENCY_CONVERTOR_EXCHANGE_RATE,  
    NVRAM_SETTING_ENABLED_WRITING_LANGUAGE,/* 0 */
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE1,
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE2,
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE3,
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE4,
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE5,   /* 5 */
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE6,
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE7,
    NVRAM_RESTORE_DEFAULT_PEN_CALIBRATION_VALUE8,
    NVRAM_PEN_CALIBRATION_VALUE1,
    NVRAM_PEN_CALIBRATION_VALUE2,                   /* 10 */
    NVRAM_PEN_CALIBRATION_VALUE3,
    NVRAM_PEN_CALIBRATION_VALUE4,
    NVRAM_PEN_CALIBRATION_VALUE5,
    NVRAM_PEN_CALIBRATION_VALUE6,
    NVRAM_PEN_CALIBRATION_VALUE7,                   /* 15 */
    NVRAM_PEN_CALIBRATION_VALUE8,
    THEME_MANAGER_MAX_THEME_ID,
    THEME_MANAGER_CURR_THEME_ID,
    NVRAM_CCONV_EXCHANGE_RATE1,
    NVRAM_CCONV_EXCHANGE_RATE2,
    NVRAM_CCONV_EXCHANGE_RATE3,
    NVRAM_CCONV_EXCHANGE_RATE4,
    NVRAM_CCONV_EXCHANGE_RATE5,
    NVRAM_JMMS_RETRIEVAL_SETTINGS,
    NVRAM_JMMS_JAVA_MESSAGE_ID,
    NVRAM_JMMS_NORMAL_MESSAGE_ID,
    NVRAM_JMMS_CHINESE_TRANSACTION_ID,
    NVRAM_AUDIO_DEBUG_INFO1,
    NVRAM_AUDIO_DEBUG_INFO2,
    NVRAM_JMMS_RESTORE_RETRIEVAL_SETTINGS,
    NVRAM_USB_PREVIOUS_THEME_INDEX,
    THEME_MANAGER_DEFAULT_THEME_ID,
    NVRAM_VDOPLY_PROXY_PORT,        /* not used */  
    NVRAM_DISPCHAR_AVATAR_WPSS,
    NARAM_DISPCHAR_AVATAR_ONOFF,
    NVRAM_VDOPLY_HIGHEST_UDP_PORT,  /* not used */
    NVRAM_VDOPLY_LOWEST_UDP_PORT,   /* not used */     
    NVRAM_UMMS_RETRIEVAL_SETTINGS,
    NVRAM_UMMS_RESTORE_RETRIEVAL_SETTINGS,
    NVRAM_MMSV01_RETRIEVAL_SETTINGS, /*Should need to wrap in macro??*/
    NVRAM_MMSV01_RESTORE_RETRIEVAL_SETTINGS,
    NVRAM_DISPCHAR_AVATAR_WPSS_SERIALNUM,
    NVRAM_DISPCHAR_AVATAR_ONOFF_SERIALNUM,
    NVRAM_CCONV_EXCHANGE_RATE6,
    NVRAM_CCONV_EXCHANGE_RATE7,
    NVRAM_CCONV_EXCHANGE_RATE8,
    NVRAM_CCONV_EXCHANGE_RATE9,
    NVRAM_AUDIO_DEBUG_INFO3,
    NVRAM_AUDIO_DEBUG_INFO4,
    NVRAM_MEDPLY_LAST_PLAYLIST_ID,
    
    NVRAM_A8BOX_DATA_ACNT,
    NVRAM_A8BOX_SETTINGS,

    NVRAM_WGTMGR_WGT_PHONE_LANG,
    /* don not remove last element */
    NVRAM_LAST_DOUBLE_ELEMENT
} DOUBLEDATA;

#endif /* CUSTOM_MMI_DEFAULT_VALUE_H */ /* _CUSTOM_MMI_DEFAULT_VALUE_PLUTO_H */

