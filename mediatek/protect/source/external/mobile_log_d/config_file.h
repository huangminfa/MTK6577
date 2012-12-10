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

#ifndef CONFIG_FILE_H
#define CONFIG_FILE_H

//
//some values of properties
//
#define CONFIGFILE "/data/data/com.mediatek.mobilelog/files/MobileLogSettings.xml"
#define BOOT_OPT_ENABLE		"true"
#define BOOT_OPT_DISABLE	"false"
#define SERVICE_RUNNING 	"service_running"
#define SERVICE_STOPPED		"service_stopped"
#define LOG_SIZE 			"300"
#define LOG_PATH_PHONE 		"/data/mtklog/mobilelog"
#define LOG_PATH_SDCARD 	"/mnt/sdcard/mtklog/mobilelog"
#define LOG_PATH_SDCARD2    "/mnt/sdcard2/mtklog/mobilelog" //add for EMMC external sdcard:/mnt/sdcard2, 2012-02-08
#define LOG_PATH_DEFAULT    "/mnt/sdcard"
#define LOG_PATH_EX         "/mnt/sdcard2"
#define LOG_PATH_SUFFIX     "/mtklog/mobilelog"
#define LOG_PATH_TEMP		"/data/log_temp"
#define LOG_PATH_IPO        "/data/log_ipo"
#define PATH_TYPE_PHONE 	"phone"
#define PATH_TYPE_SDCARD 	"sdcard"
#define LOG_CHECKED			"true"
#define LOG_UNCHECKED		"false"

#define SDCARD_SWITCH_PROP  "persist.radio.log2sd.path"  //add for EMMC external sdcard:Which sdcard should be used to store Logs, 2012-02-08 

#define LOG_CHECKED         "true"
#define LOG_UNCHECKED       "false"

#define PROP_BUILD_TYPE     "ro.build.type"
#define BUILD_TYPE_ENG      "eng"
#define BUILD_TYPE_USER     "user"

#define NAND_RESERVED       2     //MB

#define DEFAULT_BOOT        "true"
#define CUSTOMIZE_CONFIG_FILE           "/system/etc/mtklog-config.prop"
#define CUSTOMIZE_ITEM_BOOT             "com.mediatek.log.mobile.enabled"
#define CUSTOMIZE_ITEM_BOOT_ENABLE      "true"
#define CUSTOMIZE_ITEM_BOOT_DISABLE     "false"
#define CUSTOMIZE_ITEM_LOG_SIZE         "com.mediatek.log.mobile.maxsize"
#define CUSTOMIZE_ITEM_LOG_PATH         "persist.radio.log2sd.path"
#define CUSTOMIZE_ITEM_LOG_PATH_DATA    "/data"
#define CUSTOMIZE_ITEM_LOG_PATH_SDCARD  "/mnt/sdcard"
#define CUSTOMIZE_ITEM_LOG_PATH_SDCARD2 "/mnt/sdcard2"
#endif


