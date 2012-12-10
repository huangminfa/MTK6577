/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*******************************************************************************
 *
 * Filename:
 * ---------
 *   globeconfig.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of globe varible
 *
 * Author:
 * -------
 *   Bo Shang (MTK80204) 2/11/2012
 *
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 * 
 * 09 29 2012 bo.shang
 * [ALPS00366041] [MK5][W1226] òž×CALPS00359238(For_FIHTPE77_CU_ICS2_ALPS.ICS2.MP.V1_P40)ÈÔŸo·¨¸Ä×ƒLOG Path
 * .
 *
 * 06 25 2012 bo.shang
 * [ALPS00306231] [Must Resolved][6517ICS-TDD][Rose][Free Test][Network Log]The network log can't be opened(5/5).
 * .
 *
 * 06 14 2012 bo.shang
 * [ALPS00270150] [URGENT]Init service faults
 * .
 *
 * 04 13 2012 bo.shang
 * [ALPS00268549] [modem log tool new feature]Modem log tool should support factory mode
 * .
 *
 * 02 11 2012 bo.shang
 * [ALPS00234569] [Athens15V1]Way to Change log path from emmc to sdcard in network tool
 * .
 *
 *******************************************************************************/

#ifndef GLOBECONFIG_H_
#define GLOBECONFIG_H_
namespace mdlogger{
/**
 * Micro define
 */
#define MAX_PATH_LEN            256


/**
 * const varible 
 */
#define LOG_TO_SD_PROPERTY "debug.log2sd.defaultpath"
#define  SD_LOG_PATH_DEFAULT "/mnt/sdcard"


#define SD_LOG_ROOT_SUFFIX "/mtklog/mdlog"
#define SD_LOG_CONFIG_SUFFIX "/mtklog/config"

#define CATCHER_FILTER_PATH_SUFFIX "/mtklog/config/catcher_filter.bin"
#define	CATCHER_FILTER_PATH_OLD_SUFFIX "/mtklog/mdlog/catcher_filter.bin"

#define BOOTMODE_PATH "/sys/class/BOOT/BOOT/boot/boot_mode"

#define MDLOGGER_CUST_FILE "/system/etc/mtklog-config.prop"
#define MDLOGGER_RUNNING_KEY "com.mediatek.log.modem.enabled"
#define MDLOGGER_LOG_PATH_KEY "persist.radio.log2sd.path"

#define FACTORY_BOOT 4
#define ATE_FACTORY_BOOT 6
        
}

#endif /* GLOBECONFIG_H_ */
