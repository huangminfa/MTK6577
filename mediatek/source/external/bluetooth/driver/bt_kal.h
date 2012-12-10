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
 *   bt_kal.h
 *
 * Project:
 * --------
 *   MTK Bluetooth Chip
 *
 * Description:
 * ------------
 *   This file contains functions provide the service to Bluetooth Host
 *   make the operation of command and event of MTK Bluetooth chip
 *
 * Author:
 * -------
 *   CH Yeh (mtk01089)
 *
 *==============================================================================
 * 				HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log: bt_kal.h $
 *
 * 12 08 2010 enlai.chu
 * [ALPS00136988] [Need Patch] [Volunteer Patch][Bluetooth][ZTE73V1]Merge Bluetooth codes.
 * add set custom baudrate
 *
 * 11 06 2010 chunhui.li
 * [ALPS00134714] [Bluetooth] Bluetooth patch ext RAM mechanism implement
 * update BT FW patch mechanism.
 *
 * 10 13 2010 enlai.chu
 * [ALPS00128730] [Need Patch] [Volunteer Patch]Disable BT poll debug message
 * .
 *
 * 09 03 2010 enlai.chu
 * [ALPS00124264] [Need Patch] [Volunteer Patch]BT+FM co-work
 * Check-in for [ALPS00003732] [FM Radio][Android 2.1]When Launch FM Radio,we can not re-open BT to resolve FM on, BT off on issue.
 *
 * 08 26 2010 chunhui.li
 * [ALPS00123360] [FM][Android2.1]when play FM in background,it cannot power on bluetooth
 * resolve the BT power off-on failed issue when BT has activity.
 *
 * 03 04 2010 ch.yeh
 * [ALPS00001276][BT]Migration to Android 2.1 
 * .
 *
 * 02 26 2010 ch.yeh
 * [ALPS00001276][BT]Migration to Android 2.1 
 * Add mtk specific BT initilizing soure code
 *  \main\maintrunk.MT6611CE_UART\9 2009-04-16 13:10:20 GMT mtk01089
 *  move certain Custom call out of compiling flag
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/

#ifndef _BT_KAL_H
#define _BT_KAL_H

#include "os_dep.h"
#include "hci.h"
#include "cust_bt.h"

#define FLOW_CONTROL_NONE             0
#define FLOW_CONTROL_HW               1
#define FLOW_CONTROL_SW               2

/* BtStatus */
#define BT_STATUS_SUCCESS             0
#define BT_STATUS_FAILED              1
#define BT_STATUS_PENDING             2
#define BT_STATUS_BUSY                3
#define BT_STATUS_NO_RESOURCES        4
#define BT_STATUS_NOT_FOUND           5
#define BT_STATUS_DEVICE_NOT_FOUND    6
#define BT_STATUS_CONNECTION_FAILED   7
#define BT_STATUS_TIMEOUT             8
#define BT_STATUS_NO_CONNECTION       9
#define BT_STATUS_INVALID_PARM        10
#define BT_STATUS_IN_PROGRESS         11
#define BT_STATUS_RESTRICTED          12
#define BT_STATUS_INVALID_TYPE        13
#define BT_STATUS_HCI_INIT_ERR        14
#define BT_STATUS_NOT_SUPPORTED       15
#define BT_STATUS_IN_USE              16
#define BT_STATUS_SDP_CONT_STATE      17
#define BT_STATUS_CANCELLED           18
#define BT_STATUS_NOSERVICES          19
#define BT_STATUS_SCO_REJECT          20
#define BT_STATUS_CHIP_REASON         21
#define BT_STATUS_BLOCK_LIST          22
#define BT_STATUS_SCATTERNET_REJECT   23

/* Used to event packet buffer */
#define MAX_EVENT_SIZE	    256

typedef DWORD BtStatus;


//Extending Patch RAM mechanism macro
#if defined MTK_MT6620
#define BT_PATCH_EXT_ENABLE   0x0
#elif defined MTK_MT6622
#define BT_PATCH_EXT_ENABLE   0x0
#elif defined MTK_MT6626
#define BT_PATCH_EXT_ENABLE   0x0
#elif defined MTK_MT6628
#define BT_PATCH_EXT_ENABLE   0x0
#endif


typedef struct PATCH_SETTING
{
    DWORD dwPatchAddr;
    DWORD dwPatchExtVal;
    DWORD dwPatchBaseVal;
    DWORD dwPatchLenResetAddr;
    DWORD dwPatchLenResetVal;
} PATCH_SETTING;


#define LEtoHost16(_P_UC) (((USHORT)((PUCHAR)_P_UC)[0]) | (((USHORT)((PUCHAR)_P_UC)[1]) << 8))

typedef int (*SETUP_UART_PARAM)(unsigned long hComPort, int iBaudrate, int iFlowControl);


#ifdef __cplusplus
extern "C"
{
#endif
BOOL BT_SetBaudRate(
    HANDLE   hComPortFile,
    DWORD    baudRate, 
    DWORD    hostBaud,
    DWORD    dwFlowControl);

BOOL BT_WakeMagic(
    HANDLE   hComPortFile,
    BOOL     fgWaitResponse);

BOOL BT_DownPatch(
    HANDLE  hComPortFile,
    LPBYTE  cbPatch,
    DWORD   dwPatchLen);

BOOL BT_ForceAlwaysSleep(HANDLE hComPortFile);

BOOL BT_HCIReset(HANDLE hComPortFile);
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C"
{
#endif
BOOL BT_SendHciCommand(
    HANDLE   hComPortFile,
    HciCommandType wOpCode, 
    DWORD    len, 
    HciCommand* hciCommand 
    );

BOOL BT_ReadExpectedEvent(
    HANDLE   hComPortFile,
    PUCHAR   pEventPacket,
    DWORD    dwMaxOutputLen,
    UCHAR    ucExpectedEventCode,
    PDWORD   pdwPacketLen,
    BOOLEAN  fCheckCompleteOpCode,
    USHORT   usExpectedOpCode,
    BOOLEAN  fCheckCommandStatus,
    UCHAR    ucExpectedStatus
    );
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C"
{
#endif
int bt_send_data(int fd, unsigned char *buffer, unsigned long len);

int bt_receive_data(int fd, unsigned char *buffer, unsigned long len);
#ifdef __cplusplus
}
#endif

#if defined (DEBUG) || defined (_DEBUG)
void dumpBuf(WCHAR*   pwucBuf, DWORD   dwBufLen);
#endif
#endif
