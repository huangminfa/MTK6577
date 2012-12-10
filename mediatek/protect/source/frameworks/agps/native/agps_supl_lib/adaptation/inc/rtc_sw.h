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
 *    rtc_sw.h
 *
 * Project:
 * --------
 *   Maui_Software
 *
 * Description:
 * ------------
 *   This file is intends for RTC driver and adaption.
 *
 * Author:
 * -------
 *  Jensen Hu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:   1.15  $
 * $Modtime:   20 Jun 2005 19:59:48  $
 * $Log:   //mtkvs01/vmdata/Maui_sw/archives/mcu/interface/hwdrv/rtc_sw.h-arc  $
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Feb 25 2008 MTK01845
 * [MAUI_00623297] [Drv][RTC] Driver add Drv_Trace function
 * 
 *
 * Sep 14 2007 mtk01283
 * [MAUI_00548220] [Drv] To resolve compile warnings on Maui driver code
 * 
 *
 * Sep 11 2007 MTK01845
 * [MAUI_00546197] [Drv][Header file] Re-organize rtc_sw.h to separate exported APIs from other functio
 * Re-organize function prototype into 3 sections: Exported API, export for driver level module used, unknown functions
 *
 * Sep 1 2007 mtk01283
 * [MAUI_00541110] [Drv][Compile option] Check in MT6238 compile option to MainTrunk
 * 
 *
 * Jan 29 2007 mtk01283
 * [MAUI_00362858] [Drv][Compile Option]Check in Driver Feature Management compile option
 * 
 *
 * Dec 4 2006 mtk01283
 * [MAUI_00348451] [Drv][RTC]Add 2 more APIs of XOSC register read/write for engineer mode
 * 
 *
 * Nov 6 2006 mtk01283
 * [MAUI_00341174] [Drv][RTC]Enhance RTC SW calibration including adding poweroff calibration.
 * 
 *
 * Aug 24 2006 mtk01283
 * [MAUI_00324218] [Drv]Add compile option MT6225
 * 
 *
 * Jul 13 2006 mtk01283
 * [MAUI_00210172] [Drv][Compile Option]Add compile option MT6230
 * 
 *
 * May 12 2006 MTK01283
 * [MAUI_00193607] [Drv][Feature]Remove compile option MT6230
 * 
 *
 * Apr 1 2006 MTK01283
 * [MAUI_00184151] [Drv][New Feature]Add MT6230 compile option
 * 
 *
 * Mar 28 2006 mtk00502
 * [MAUI_00182950] [BMT][Enhancement]modify NFB power on proecedure
 * 
 *
 * Dec 26 2005 mtk00502
 * [MAUI_00161337] [Drv][New Feature]Add MT6219B compile option
 * 
 *
 * Dec 13 2005 mtk00502
 * [MAUI_00161337] [Drv][New Feature]Add MT6226M compile option
 * 
 * 
 *    Rev 1.15   20 Jun 2005 20:00:34   mtk00502
 * add MT6226/MT6227 compile option
 * Resolution for 11617: [DRV][New Feature]Add MT6226/M6227 compile option
 * 
 *    Rev 1.14   May 17 2005 00:29:16   BM_Trunk
 * Karlos:
 * add copyright and disclaimer statement
 * 
 *    Rev 1.13   Mar 19 2005 14:49:36   mtk00502
 * add 6228 compile option
 * Resolution for 10304: [Drirver][New Fearture]Support MT6228
 * 
 *    Rev 1.12   Jan 18 2005 00:34:28   BM
 * append new line in W05.04
 * 
 *    Rev 1.11   Oct 22 2004 16:22:26   mtk00502
 * add a new interface RTC_is_MS_FirstPowerOn to check if MS is first power on this time
 * Resolution for 8360: [MMI][Enhancement] Add support for date/time setting reminder
 * 
 *    Rev 1.10   Oct 12 2004 11:34:14   mtk00479
 * Add compile option of MT6217
 * Resolution for 8195: [Drv][NewFeature]Add compile option of MT6217
 * 
 *    Rev 1.9   May 14 2004 10:04:22   mtk00479
 * Add USB charging function
 * Resolution for 5419: [Drv][AddFeature]Add USB charging function
 * 
 *    Rev 1.8   Feb 27 2004 20:14:52   mtk00502
 * add 6219 compilt option
 * Resolution for 4005: [Driver][New Feature]add 6219 compile option
 * 
 *    Rev 1.7   Feb 27 2004 17:06:44   mtk00502
 * to solve SCR 1022
 * Resolution for 1022: MMI- Cannot Power On
 * 
 *    Rev 1.5.10.1   Feb 27 2004 17:05:28   mtk00502
 * to solve SCR 1022
 * Resolution for 1022: MMI- Cannot Power On
 * 
 *    Rev 1.6.4.1   Feb 24 2004 11:41:12   mtk00502
 * add 6219 compile option
 * Resolution for 4005: [Driver][New Feature]add 6219 compile option
 * 
 *    Rev 1.6   Oct 31 2003 15:27:26   mtk00502
 * add 6218B compile option
 * Resolution for 3233: [Driver][New Feature] Add 6218B compile option
 * 
 *    Rev 1.5   Jun 12 2003 16:10:24   mtk00288
 * add MT6218 definitions
 * Resolution for 1952: [Drivers][add Feature]MT6218 Peripherals driver
 * 
 *    Rev 1.4   May 12 2003 10:45:32   mtk00288
 * add the definiton of RTC_AUTOPDN.
 * Resolution for 1743: [MT6205B pwinc&rtc][bug fix]Target power off abnormally, but HW doesn't de-active BBwakeup
 * 
 *    Rev 1.3   Apr 16 2003 22:10:44   mtk00288
 * revice MT6205B RTC registers
 * Resolution for 1580: [LCD][BugFix]Color LCD display flickers problem
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _RTC_SW1_H
#define _RTC_SW1_H
typedef struct __rtc 
{
	kal_uint8		rtc_sec;    /* seconds after the minute   - [0,59]  */
	kal_uint8		rtc_min;    /* minutes after the hour     - [0,59]  */
	kal_uint8		rtc_hour;   /* hours after the midnight   - [0,23]  */
	kal_uint8		rtc_day;    /* day of the month           - [1,31]  */
	kal_uint8		rtc_mon;    /* months 		               - [1,12] */
	kal_uint8		rtc_wday;   /* days in a week 		      - [1,7] */
	kal_uint8		rtc_year;   /* years                      - [0,127] */
} t_rtc;
#endif

