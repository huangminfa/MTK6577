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

package com.mediatek.engineermode.syslogger;

public abstract class SysUtils {
    
    //public final static String PROP_MD = "com.mediatek.mdlogger.Running";
    /* mdlog property change */
    public final static String PROP_MD = "debug.mdlogger.Running";
    //public final static String PROP_MOBILE = "com.mediatek.mobilelog.Running";

    /* mobilelog property change  --> zhengchao xu*/
    public final static String PROP_MOBILE = "debug.MB.running";
//    public final static String PROP_NETWORK = "com.mediatek.network.Running";
    /* netlog property change */
    public final static String PROP_NETWORK = "persist.radio.netlog.Running";
    public static final String PROP_EXTMD = "com.mtk.extmdlogger.Running";
    
//    public final static String PER_LOG2SD = "persist.sys.log2sd.defaultpath";
    public final static String PER_LOG2SD = "persist.radio.log2sd.path";
    public final static String INTERNALSDTEXT = "Path: /mnt/sdcard";
    public final static String EXTERNALSDTEXT = "Path: /mnt/sdcard2";
    public final static String EXTERNALSDPATH = "/mnt/sdcard2";
    
    public static final String PROP_ON = "1";
    public static final String PROP_OFF = "0";

    public static final String BROADCAST_ACTION = "com.mediatek.syslogger.action";
    public static final String BROADCAST_KEY_SRC_FROM = "From";
    public static final String BROADCAST_KEY_SRC_TO = "To";
    public static final String BROADCAST_VAL_SRC_MD = "ModemLog";
    public static final String BROADCAST_VAL_SRC_MOBILE = "MobileLog";
    public static final String BROADCAST_VAL_SRC_NETWORK = "ActivityNetworkLog";
    public static final String BROADCAST_VAL_SRC_EXTMD = "ExtModemLog";
    public static final String BROADCAST_VAL_SRC_HQ = "CommonUI";
    public static final String BROADCAST_VAL_SRC_UNK = "Unknown";
    public static final String BROADCAST_KEY_COMMAND = "Command";
    public static final String BROADCAST_VAL_COMMAND_START = "Start";
    public static final String BROADCAST_VAL_COMMAND_STOP = "Stop";
    public static final String BROADCAST_VAL_COMMAND_UNK = "Unknown";
    public static final String BROADCAST_KEY_RETURN = "Return";
    public static final String BROADCAST_VAL_RETURN_OK = "Normal";
    public static final String BROADCAST_VAL_RETURN_ERR = "Error";
    public static final String BROADCAST_KEY_SELFTEST = "Satan";
    public static final String BROADCAST_VAL_SELFTEST = "Angel";

    public static final int EVENT_OP_SEARCH_START = 101;
    public static final int EVENT_OP_SEARCH_FIN = 103;
    public static final int EVENT_OP_ERR = 104;
    public static final int EVENT_OP_EXCEPTION = 105;
    public static final int EVENT_OP_TIMEOUT = 106;
    public static final int EVENT_OP_MSG = 107;
    public static final int EVENT_OP_UPDATE_CK = 108;
    public static final int EVENT_TICK = 109;
    
    // -- From Log2Server
    public static final String ACTION_EXP_HAPPENED = "com.mediatek.log2server.EXCEPTION_HAPPEND";
    public static final String EXTRA_KEY_EXP_PATH = "path";
    public static final String EXTRA_KEY_EXP_NAME = "db_filename";
    public static final String EXTRA_KEY_EXP_ZZ = "zz_filename";
    
    public static final int ZZ_INTERNAL_LENGTH = 10; // split by ","
    public static final String PATH_SEPARATER = "/";   
    //add for mobile log begin
    public static final String MODEM_LOG_FOLDER = "mtklog/mdlog";
    public static final String MOBILE_LOG_FOLDER = "mtklog/mobilelog";
    public static final String NET_LOG_FOLDER = "mtklog/netlog";
    public static final String EXTMODEM_LOG_FOLDER = "mtklog/extmdlog";
    public static final String ZIP_TAG_LOG_FOLDER = "mtklog/taglog";
    public static final String ZIP_LOG_SUFFIX = ".zip";
    //add for mobile log end
    
    public static final int LOG_QUANTITY = 3;   // Mobile/Modem/Net//ExtModem
    
    // TagLogService event
    public static final int EVENT_GET_EXCEPTION_TYPE = 201;
    public static final int EVENT_ZIP_ALL_LOG = 202;
    public static final int EVENT_CREATE_INPUTDIALOG = 203;
    public static final int EVENT_ALL_LOGTOOL_STOPED = 205;
    public static final int EVENT_ZIP_LOG_SUCCESS = 206;
    public static final int EVENT_ZIP_LOG_FAIL = 207;
    public static final int EVENT_LOCK_OF_SDSPACE = 208;
    public static final int EVENT_CHECK_INPUTDIALOG_TIMEOUT = 209;
    
    
    public static final int DIALOG_INPUT = 301;
    public static final int DIALOG_ALL_LOGTOOL_STOPED = 302;
    public static final int DIALOG_LOCK_OF_SDSPACE = 303;
    public static final int DIALOG_ZIP_LOG_SUCCESS = 304;
    public static final int DIALOG_ZIP_LOG_FAIL = 305;
    public static final int DIALOG_START_PROGRESS = 306;
    public static final int DIALOG_END_PROGRESS = 307;
    
    public static final int SD_NORMAL = 401;
    public static final int SD_LOCK_OF_SPACE = 402;
    public static final int SD_NOT_EXIST = 403;
    public static final int SD_NOT_WRITABLE = 404;
    
    public static final String TITLE_INPUT_TAG = "[Tag Log] Please input tag (letters&numbers&'_'&' ' only):";
    public static final String TITLE_ALL_LOGTOOL_STOPED = "All log tools are stopped!";
    public static final String MSG_ALL_LOGTOOL_STOPED = "Want you start all log tools?";
    public static final String TITLE_LOCK_OF_SDSPACE = "[Tag Log] Lack of SDCard space!";
    public static final String MSG_LOCK_OF_SDSPACE = "Please release some space";
    public static final String TITLE_ZIP_LOG_FAIL = "[Tag Log] Failed!";
    public static final String MSG_ZIP_LOG_FAIL = "Zip Tag log failed!";
    public static final String TITLE_WARNING = "[Tag Log] Warning!";
    public static final String MSG_SD_NOT_EXIST = "The SD Card doesn't exist";
    public static final String MSG_SD_NOT_WRITABLE = "The SD Card is not writtable";
    
    public static final String EXCP_TAGLOG_TYPE = "Kernel Module";
    public static final String EXCP_TAGLOG_DESCRIPTION = "";
    public static final String EXCP_TAGLOG_PROCESS = "manual dump";

    // tag log switch start 
    public static final String TAGLOG_SWITCH = "taglog_switch";
    public static final String TAGLOG_SWITCH_KEY = "taglog_switch_key";
    public static final String ACTION_TAGLOG_SWITCH= "com.mediatek.taglog.switch";
    // tag log switch end 
    
    // MTKLog 
    public static final String ACTION_TAGLOG_TO_LOG2SERVER = "com.mediatek.syslogger.taglog";
    public static final String BROADCAST_KEY_TAGLOG_RESULT = "TaglogResult"; 
    public static final String BROADCAST_KEY_MDLOG_PATH = "ModemLogPath"; 
    public static final String BROADCAST_KEY_MOBILELOG_PATH = "MobileLogPath";
    public static final String BROADCAST_KEY_NETLOG_PATH = "NetLogPath";
    public static final String BROADCAST_VAL_TAGLOG_CANCEL = "Cancel";
    public static final String BROADCAST_VAL_TAGLOG_SUCCESS = "Successful";
    public static final String BROADCAST_VAL_TAGLOG_FAILED = "Failed"; 
    public static final String BROADCAST_VAL_LOGTOOL_STOPPED = "LogToolStopped";
    
    // Grab modem log
    public static final String ACTION_MDLOG_READY = "com.mediatek.mdlogger.MEMORYDUMP_DONE";
    public static final String BROADCAST_KEY_MDEXP_LOGPATH = "LogPath";
}
