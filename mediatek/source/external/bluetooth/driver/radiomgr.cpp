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

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <termios.h>

#include "bt_kal.h"


#ifndef MTK_COMBO_SUPPORT
static SETUP_UART_PARAM pSetup_uart_param = NULL;
#endif

#define UPDATE_PATCH_DIR                 "/data/"
#define BUILT_IN_PATCH_DIR               "/system/etc/firmware/"

//default patch
//--------------------------------6620--------------------------------
#if defined MTK_MT6620
// Patch download by WMT
//--------------------------------6622--------------------------------
#elif defined MTK_MT6622
#if BT_PATCH_EXT_ENABLE
static char BT_UPDATE_PATCH_EXT_FILE_NAME[64] = "/data/MTK_MT6622_E2_Patch_ext.nb0";
static char BT_BUILT_IN_PATCH_EXT_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6622_E2_Patch_ext.nb0";
#endif
static char BT_UPDATE_PATCH_FILE_NAME[64] = "/data/MTK_MT6622_E2_Patch.nb0";
static char BT_BUILT_IN_PATCH_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6622_E2_Patch.nb0";
//--------------------------------6626--------------------------------
#elif defined MTK_MT6626
#if BT_PATCH_EXT_ENABLE
static char BT_UPDATE_PATCH_EXT_FILE_NAME[64] = "/data/MTK_MT6626_E2_Patch_ext.nb0";
static char BT_BUILT_IN_PATCH_EXT_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6626_E2_Patch_ext.nb0";
#endif
static char BT_UPDATE_PATCH_FILE_NAME[64] = "/data/MTK_MT6626_E2_Patch.nb0";
static char BT_BUILT_IN_PATCH_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6626_E2_Patch.nb0";
//--------------------------------6628--------------------------------
#elif defined MTK_MT6628
// Patch download by WMT
#endif

/**************************************************************************
 *              F U N C T I O N   D E C L A R A T I O N S                 *
***************************************************************************/

extern "C"
DWORD GORM_Init(
    HANDLE  hComFile,
    PUCHAR  pucPatchExtData,
    DWORD   dwPatchExtLen,
    PUCHAR  pucPatchData,
    DWORD   dwPatchLen,
    PBYTE   ucNvRamData,
    DWORD   dwBaud,
    DWORD   dwHostBaud,
    DWORD   dwUseFlowControl
);

/**************************************************************************
 *                          F U N C T I O N S                             *
***************************************************************************/

extern "C"
BOOL BT_InitDevice(
    HANDLE  hComPortFile,
    PBYTE   ucNvRamData,
    DWORD   dwBaud,
    DWORD   dwHostBaud,
    DWORD   dwUseFlowControl,
    SETUP_UART_PARAM  setup_uart_param
    )
{
    DWORD   dwStatus;
    LPBYTE  pbPatchExtBin = NULL;
    DWORD   dwPatchExtLen = 0;
    FILE*   pPatchExtFile = NULL;
    LPBYTE  pbPatchBin = NULL;
    DWORD   dwPatchLen = 0;
    FILE*   pPatchFile = NULL;
    
    LOG_DBG("BT_InitDevice\r\n");

#ifndef MTK_COMBO_SUPPORT
    tcflush(hComPortFile, TCIOFLUSH);
    
    pSetup_uart_param = setup_uart_param;
    if(pSetup_uart_param == NULL){
        LOG_ERR("UART setup callback is null\r\n");
        return FALSE;
    }
    
    LOG_DBG("BT load firmware patch\r\n");

#if BT_PATCH_EXT_ENABLE
    pPatchExtFile = fopen(BT_UPDATE_PATCH_EXT_FILE_NAME, "rb"); 
    
    /* if there is no adhoc file, use built-in patch file under system etc firmware */
    if(pPatchExtFile == NULL){
        pPatchExtFile = fopen(BT_BUILT_IN_PATCH_EXT_FILE_NAME, "rb");
        if(pPatchExtFile != NULL) {
            LOG_WAN("Open %s\r\n", BT_BUILT_IN_PATCH_EXT_FILE_NAME);
        }
        else {
            LOG_ERR("Can't get valid patch ext file\r\n");
        }
    }
    else {
        LOG_WAN("Open %s\r\n", BT_UPDATE_PATCH_EXT_FILE_NAME);       
    }
    
    /* file exists */
    if(pPatchExtFile != NULL){
        if(fseek(pPatchExtFile, 0, SEEK_END) != 0){
            LOG_ERR("fseek patch ext file fails errno: %d\n", errno);
        }
        else{
            long lFileLen = 0;
            
            lFileLen = ftell(pPatchExtFile);
            if (lFileLen > 0){
                LOG_DBG("Patch ext file size %d\n", (int)lFileLen);
                rewind(pPatchExtFile);
                
                pbPatchExtBin = (unsigned char*)malloc(lFileLen);
                if (pbPatchExtBin){
                    size_t szReadLen = fread(pbPatchExtBin, 1, lFileLen, pPatchExtFile);
                
                    dwPatchExtLen = szReadLen;
                    if(szReadLen != (size_t)lFileLen){
                        LOG_ERR("fread patch ext len error, file len: %d, read len: %d\n", (int)lFileLen, (int)szReadLen);
                        free(pbPatchExtBin);
                        pbPatchExtBin = NULL;
                        dwPatchExtLen = 0;
                    }
                }else{
                    LOG_ERR("Allocate patch ext memory fails\n");
                }
            }else{
                LOG_ERR("Patch ext error len %d\n", (int)lFileLen);
            }
        }
    }
#endif

    /* Use data directory first. for future update test convinience */
    pPatchFile = fopen(BT_UPDATE_PATCH_FILE_NAME, "rb"); 

    /* if there is no adhoc file, use built-in patch file under system etc firmware */
    if(pPatchFile == NULL){
        pPatchFile = fopen(BT_BUILT_IN_PATCH_FILE_NAME, "rb");
        if(pPatchFile != NULL) {
            LOG_WAN("Open %s\r\n", BT_BUILT_IN_PATCH_FILE_NAME);
        }
        else {
            LOG_ERR("Can't get valid patch file\r\n");
        }
    }
    else {
        LOG_WAN("Open %s\r\n", BT_UPDATE_PATCH_FILE_NAME);       
    }
    
    /* file exists */
    if(pPatchFile != NULL){
        if(fseek(pPatchFile, 0, SEEK_END) != 0){
            LOG_ERR("fseek patch file fails errno: %d\n", errno);
        }
        else{
            long lFileLen = 0;
            
            lFileLen = ftell(pPatchFile);
            if (lFileLen > 0){
                LOG_DBG("Patch file size %d\n", (int)lFileLen);
                /* back to file beginning */
                rewind(pPatchFile);
                
                pbPatchBin = (unsigned char*)malloc(lFileLen);
                if(pbPatchBin){
                    size_t szReadLen = 0;
                    szReadLen = fread(pbPatchBin, 1, lFileLen, pPatchFile);
                
                    dwPatchLen = szReadLen;
                    if(szReadLen != (size_t)lFileLen){
                        LOG_ERR("fread patch len error, file len: %d, read len: %d\n", (int)lFileLen, (int)szReadLen);
                        free(pbPatchBin);
                        pbPatchBin = NULL;
                        dwPatchLen = 0;
                    }
                }else{
                    LOG_ERR("Allocate patch memory fails\n");
                }                
            }else{
                LOG_ERR("Patch error len %d\n", (int)lFileLen);
            }
        }
    }
#endif


    /* Invoke HCI transport entrance */
    dwStatus = GORM_Init(
        hComPortFile,
        pbPatchExtBin,   //patch ext 
        dwPatchExtLen,   
        pbPatchBin,      //patch
        dwPatchLen,
        ucNvRamData,
        dwBaud,
        dwHostBaud,
        dwUseFlowControl
        );


#ifndef MTK_COMBO_SUPPORT
    if(pbPatchExtBin){
        free(pbPatchExtBin);
        pbPatchExtBin = NULL;
    }  
    
    if(pPatchExtFile){
        fclose(pPatchExtFile);
        pPatchExtFile = NULL;
    }
    
    if(pbPatchBin){
        free(pbPatchBin);
        pbPatchBin = NULL;
    }
    
    if(pPatchFile){
        fclose(pPatchFile);
        pPatchFile = NULL;
    }
#endif
    
    if(dwStatus != 0 ){
        LOG_ERR("GORM fails return code %d\r\n", (int)dwStatus);
        return FALSE;
    }
    
    return TRUE;
}

extern "C"
BOOL BT_DeinitDevice(
    HANDLE  hComPortFile
    )
{
    LOG_DBG("BT_DeinitDevice\r\n");

#if defined(MTK_MT6616) || defined(MTK_MT6626)
    tcflush(hComPortFile, TCIOFLUSH);
    BT_WakeMagic(hComPortFile, TRUE);
    BT_HCIReset(hComPortFile);
    BT_SetBaudRate(hComPortFile, 115200, 115200, 0);
    BT_ForceAlwaysSleep(hComPortFile);
#endif

    return TRUE;
}

extern "C"
BOOL BT_SendHciCommand(
    HANDLE  hComPortFile,
    HciCommandType wOpCode, 
    DWORD   len, 
    HciCommand* hciCommand 
    )
{
    UCHAR   HCI_CMD[256+4] = {0x01, 0x00, 0x00, 0x00};
    
    HCI_CMD[1] = (UCHAR)wOpCode;
    HCI_CMD[2] = (UCHAR)(wOpCode >> 8);
    HCI_CMD[3] = (UCHAR)len;
    
    LOG_DBG("OpCode 0x%04x len %d\n", wOpCode, (int)len);
    
    if(len > 0){
        memcpy(&HCI_CMD[4], hciCommand->parms, len);
    }
    
    if(bt_send_data(hComPortFile, HCI_CMD, len + 4) < 0){
        LOG_ERR("Write HCI command fails errno %d\r\n", errno);
        return FALSE;
    }
    
    return TRUE;
}

static BOOL BT_ReadPacketHeader(
    HANDLE  hComPortFile,
    UCHAR*  pPacketType,//cmd, event, ACL data, SCO data
    PDWORD  pRemainLen, //remaining len for variable part
    USHORT* pusOpCode,  //cmd OpCode
    USHORT* pusConnHandle, //connect handle, flags
    UCHAR*  pEventCode  //event code
    )
{
    UCHAR   pCmdHdr[3];
    UCHAR   pAclHdr[4];
    UCHAR   pScoHdr[3];
    UCHAR   pEventHdr[2];
    UCHAR   type = 0;
    
    /* Read UART header */
    
    if(bt_receive_data(hComPortFile, &type, 1) < 0){
        LOG_ERR("Read packet header fails\r\n");
        return FALSE;
    }
    
    *pPacketType = type;
    
    switch(type){
        case 1: //cmd
            if(bt_receive_data(hComPortFile, pCmdHdr, 3) < 0)
            {
                LOG_ERR("Read cmd header fails %d\r\n", errno);
                return FALSE;
            }
            
            *pusOpCode = (((USHORT)pCmdHdr[0]) | (((USHORT)pCmdHdr[1]) << 8));
            *pRemainLen = pCmdHdr[2];
            break;
            
        case 2: //ACL data
            if(bt_receive_data(hComPortFile, pAclHdr, 4) < 0)
            {
                LOG_ERR("Read ACL header fails %d\r\n", errno);
                return FALSE;
            }
            
            *pusConnHandle = (((USHORT)pAclHdr[0]) | (((USHORT)pAclHdr[1]) << 8));
            *pRemainLen = (((USHORT)pAclHdr[2]) | (((USHORT)pAclHdr[3]) << 8));
            break;
            
        case 3: //SCO data
            if(bt_receive_data(hComPortFile, pScoHdr, 3) < 0)
            {
                LOG_ERR("Read SCO header fails %d\r\n", errno);
                return FALSE;
            }
            
            *pusConnHandle = (((USHORT)pScoHdr[0]) | (((USHORT)pScoHdr[1]) << 8));
            *pRemainLen = pScoHdr[2];
            break;
            
        case 4: //event
            if(bt_receive_data(hComPortFile, pEventHdr, 2) < 0)
            {
                LOG_ERR("Read event header fails %d\r\n", errno);
                return FALSE;
            }
            
            *pEventCode = pEventHdr[0];
            *pRemainLen = pEventHdr[1];
            break;
            
        default: //other
            LOG_ERR("Unkown packet type %02x\r\n", type);
            return FALSE;
            break;
    }
    
    return TRUE;
}

static BOOL BT_ReadPacket(
    HANDLE  hComPortFile,
    PUCHAR  pPacket,
    DWORD   dwMaxBufSz,
    PDWORD  pdwPacketLen
    )
{
    UCHAR   packetType;
    DWORD   remainLen;
    USHORT  usOpCode, usConnHandle;
    UCHAR   ucEventCode;
    DWORD   dwPktLen = 0;
    int     i = 0;
    
    if(dwMaxBufSz == 0){
        LOG_ERR("Read packet output is too short \r\n");
        return FALSE;
    }

START:
    if(BT_ReadPacketHeader(
        hComPortFile,
        &packetType,
        &remainLen,
        &usOpCode,
        &usConnHandle,
        &ucEventCode) == FALSE){
        
        if(i < 7){
            LOG_ERR("Get %02x for packet type\n", packetType);
            i++;
            goto START;
        }
        else{
            LOG_ERR("Read packet header fails\r\n");
            return FALSE;
        }
    }
    
    pPacket[0] = packetType;
    dwPktLen ++;
    
    /* Command packet */
    if(packetType == 1){
        if(dwMaxBufSz < (4 + remainLen)){
            LOG_ERR("Read command packet output is too short \r\n");
            return FALSE;
        }
        
        pPacket[dwPktLen] = (UCHAR)usOpCode;
        pPacket[dwPktLen + 1] = (UCHAR)(usOpCode >> 8);
        dwPktLen += 2;
        
        pPacket[dwPktLen] = (UCHAR)remainLen;
        dwPktLen ++;
        
        if(bt_receive_data(hComPortFile, pPacket + dwPktLen, remainLen) < 0)
        {
            LOG_ERR("Read remain packet fails %d \r\n", errno);
            return FALSE;
        }    
        
        dwPktLen += remainLen;
        *pdwPacketLen = dwPktLen;
        
        return TRUE;
    }
    
    /* ACL data */
    if(packetType == 2){
        if(dwMaxBufSz < (5 + remainLen)){
            LOG_ERR("Read acl packet output is too short \r\n");
            return FALSE;
        }
        
        pPacket[dwPktLen] = (UCHAR)usConnHandle;
        pPacket[dwPktLen + 1] = (UCHAR)(usConnHandle >> 8);
        dwPktLen += 2;

        pPacket[dwPktLen] = (UCHAR)remainLen;
        pPacket[dwPktLen + 1] = (UCHAR)(remainLen >> 8);
        dwPktLen += 2;
        
        if(bt_receive_data(hComPortFile, pPacket + dwPktLen, remainLen) < 0)
        {
            LOG_ERR("Read remain packet fails %d \r\n", errno);
            return FALSE;
        }    
        
        dwPktLen += remainLen;
        *pdwPacketLen = dwPktLen;
        
        return TRUE;
    }
    
    /* SCO data */
    if(packetType == 3){
        if(dwMaxBufSz < (4 + remainLen)){
            LOG_ERR("Read sco packet output is too short \r\n");
            return FALSE;
        }
        
        pPacket[dwPktLen] = (UCHAR)usConnHandle;
        pPacket[dwPktLen + 1] = (UCHAR)(usConnHandle >> 8);
        dwPktLen += 2;
        
        pPacket[dwPktLen] = (UCHAR)remainLen;
        dwPktLen ++;
        
        if(bt_receive_data(hComPortFile, pPacket + dwPktLen, remainLen) < 0)
        {
            LOG_ERR("Read remain packet fails %d \r\n", GetLastError());
            return FALSE;
        }    
        
        
        dwPktLen += remainLen;
        *pdwPacketLen = dwPktLen;
        
        return TRUE;
    }
    
    /* Event packet */
    if(packetType == 4){
        if(dwMaxBufSz < (3 + remainLen)){
            LOG_ERR("Read event packet output is too short \r\n");
            return FALSE;
        }
        
        pPacket[dwPktLen] = ucEventCode;
        pPacket[dwPktLen + 1] = (UCHAR)remainLen;
        dwPktLen += 2;
        
        if(bt_receive_data(hComPortFile, pPacket + dwPktLen, remainLen) < 0)
        {
            LOG_ERR("Read remain packet fails %d \r\n", errno);
            return FALSE;
        }    
        
        dwPktLen += remainLen;
        *pdwPacketLen = dwPktLen;
        
        return TRUE;
    }
    
    LOG_ERR("Unkown packet type \r\n");
    return FALSE;
}

extern "C"
BOOL BT_ReadExpectedEvent(
    HANDLE  hComPortFile,
    PUCHAR  pEventPacket,
    DWORD   dwMaxBufSz,
    UCHAR   ucExpectedEventCode,
    PDWORD  pdwPacketLen,
    BOOLEAN fCheckCompleteOpCode,//if event code is Command Complete Event, whether to check OpCode
    USHORT  usExpectedOpCode,
    BOOLEAN fCheckCommandStatus,//if event code is Command Status Event, whether to check status
    UCHAR   ucExpectedStatus
    )
{
    USHORT  usEventOpCode;
    UCHAR   ucEventCode, ucCommandStatus;
    
    if(BT_ReadPacket(hComPortFile, 
        pEventPacket,
        dwMaxBufSz,
        pdwPacketLen) == FALSE){

        LOG_ERR("Read packet fails\r\n");
        return FALSE;
    }
    
    /* Expect Event only */
    if(pEventPacket[0] != 4){
        LOG_ERR("Unexpected packet type\r\n");
        return FALSE;
    }
    
    ucEventCode = pEventPacket[1];
    
    if(ucEventCode != ucExpectedEventCode){
        LOG_ERR("Unexpected event code\r\n");
        return FALSE;
    }
    
    if(ucEventCode == 0x0E){
        if(fCheckCompleteOpCode){
            usEventOpCode = ((USHORT)pEventPacket[4]) |( ((USHORT)pEventPacket[5]) << 8);

            if(usEventOpCode != usExpectedOpCode){
                LOG_ERR("Unexpected OpCode\r\n");
                return FALSE;
            }
        }
        if(fCheckCommandStatus){
            ucCommandStatus = pEventPacket[6];

            if(ucCommandStatus != ucExpectedStatus){
                LOG_ERR("Unexpected status %02x\r\n", ucCommandStatus);
                return FALSE;
            }            
        }
    }
    
    if(ucEventCode == 0x0F){
        if(fCheckCompleteOpCode){
            usEventOpCode = ((USHORT)pEventPacket[5]) |( ((USHORT)pEventPacket[6]) << 8);

            if(usEventOpCode != usExpectedOpCode){
                LOG_ERR("Unexpected OpCode\r\n");
                return FALSE;
            }
        }
        
        if(fCheckCommandStatus){
            ucCommandStatus = pEventPacket[3];

            if(ucCommandStatus != ucExpectedStatus){
                LOG_ERR("Unexpected status %02x\r\n", ucCommandStatus);
                return FALSE;
            }
        }
    }

    return TRUE;
}

extern "C" 
BOOL BT_WakeMagic(
    HANDLE  hComPortFile,
    BOOL    fgWaitResponse
    )
{
    DWORD   dwReadLength = 0;
    UCHAR   bMagicNum = 0xFF;
    UCHAR   pAckEvent[20];

    if(bt_send_data(hComPortFile, &bMagicNum, 1) < 0){      
        LOG_ERR("Write magic fails %d\r\n", errno);
        return FALSE;
    }
    
    if(fgWaitResponse == FALSE){
        return TRUE;
    }
    
    if(BT_ReadExpectedEvent(
        hComPortFile, 
        pAckEvent, 
        sizeof(pAckEvent),
        0x0E,//Wake up return command complete
        &dwReadLength,
        TRUE,
        0xFCC0,
        TRUE,
        0x00) == FALSE){
        
        LOG_ERR("Read event fails %d\r\n", errno);
        return FALSE;
    }
    
    return TRUE;
}

#ifndef MTK_COMBO_SUPPORT
/* 
 * Before use HCI_VS_UART_CONFIG command to change UART settings of controller, 
 * be sure to execute this command first.
 */
static BOOL BT_PreSetUart(
    HANDLE  hComPortFile
    )
{
    UCHAR   HCI_PRE_CONFIG[] = {0x01, 0xD0, 0xFC, 0x08, 0x60, 0x01, 0x05, 0x80, 0x00, 0x00, 0x00, 0x00};
    DWORD   dwReadLength = 0;
    UCHAR   pAckEvent[20];
    
    if(bt_send_data(hComPortFile, HCI_PRE_CONFIG, sizeof(HCI_PRE_CONFIG)) < 0){	
        LOG_ERR("Write pre config command fails\r\n");
        return FALSE;
    }
    
    if(BT_ReadExpectedEvent(
        hComPortFile, 
        pAckEvent, 
        sizeof(pAckEvent),
        0x0E,
        &dwReadLength,
        TRUE,
        0xFCD0,
        TRUE,
        0x00) == FALSE){
        
        LOG_ERR("Read complete event fails\r\n");
        return FALSE;
    }
    
    return TRUE;
}

extern "C"
BOOL BT_SetBaudRate(
    HANDLE  hComPortFile,
    DWORD   baudRate, 
    DWORD   dwHostBaud,
    DWORD   dwFlowControl
    )
{
    UCHAR   HCI_VS_UART_CONFIG[] = {0x01, 0x77, 0xFC, 0x02, 0x00, 0x00};
    DWORD   dwReadLength = 0;
    UCHAR   pAckEvent[20];

    /*
         * MTK change UART settings handshake procedure:
         * 1. Send MTK_SET_UART command (0xFC77) to Controller
         * 2. Controller return HCI_COMMAND_STATUS event
         * 3. Host uses to new UART settings and then send 0xFF to inform Controller
         * 4. Controller uses new UART settings to return COMMAND_COMPLETE event of WakeUp command (0xFCC0), 
         *     and then return COMMAND_COMPLETE event of MTK_SET_UART command (0xFC77)
         */
    switch(baudRate)
    {
        case 9600:
            HCI_VS_UART_CONFIG[4] = 0x01;
            break;
        case 14400:
            HCI_VS_UART_CONFIG[4] = 0x02;
            break;
        case 19200:
            HCI_VS_UART_CONFIG[4] = 0x03;
            break;
        case 28800:
            HCI_VS_UART_CONFIG[4] = 0x04;
            break;
        case 33900:
            HCI_VS_UART_CONFIG[4] = 0x05;
            break;
        case 38400:
            HCI_VS_UART_CONFIG[4] = 0x06;
            break;
        case 57600:
            HCI_VS_UART_CONFIG[4] = 0x07;
            break;
        case 115200:
            HCI_VS_UART_CONFIG[4] = 0x08;
            break;
        case 230400:
            HCI_VS_UART_CONFIG[4] = 0x09;
            break;
        case 460800:
            HCI_VS_UART_CONFIG[4] = 0x0A;
            break;
        case 921600:
            HCI_VS_UART_CONFIG[4] = 0x0B;
            break;
        case 3250000:
        case 3200000:
            HCI_VS_UART_CONFIG[4] = 0x0C;
            break;
        default:
            HCI_VS_UART_CONFIG[4] = 0x00;
            break;          
    }    
    
    switch(dwFlowControl){
        case 0:
            break;
        case 0x01:/* HW */
            HCI_VS_UART_CONFIG[5] = 0x40;
            if(BT_PreSetUart(hComPortFile) == FALSE){
                return FALSE;
            }
            break;            
        case 0x02:/* SW */
            HCI_VS_UART_CONFIG[5] = 0x80; //Xon off
            break;
        default:
            LOG_ERR("Unkown flow type %d \r\n", (int)dwFlowControl);
            break;            
    }
    
    LOG_DBG("Host baud %d HCI_VS_UART_CONFIG %02x %02x %02x %02x %02x %02x\r\n",
        (int)dwHostBaud,
        HCI_VS_UART_CONFIG[0], HCI_VS_UART_CONFIG[1], HCI_VS_UART_CONFIG[2], 
        HCI_VS_UART_CONFIG[3], HCI_VS_UART_CONFIG[4], HCI_VS_UART_CONFIG[5]);


    // Config BT chip baudrate
    if(bt_send_data(hComPortFile, HCI_VS_UART_CONFIG, sizeof(HCI_VS_UART_CONFIG))< 0){	
        LOG_ERR("Write UART config command fails\r\n");
        return FALSE; 	
    }
    
    // Command status event
    if(BT_ReadExpectedEvent(
        hComPortFile, 
        pAckEvent, 
        sizeof(pAckEvent),
        0x0F,
        &dwReadLength,
        TRUE,
        0xFC77,
        TRUE,
        0x00) == FALSE){
        
        LOG_ERR("Read command status event fails\r\n");
        return FALSE;
    }

    // Config host baudrate
    if(pSetup_uart_param(hComPortFile, dwHostBaud, dwFlowControl) < 0){
        LOG_ERR("Setup host uart baudrate fails\r\n");
        return FALSE;
    }
    
    if(BT_WakeMagic(hComPortFile, TRUE) == FALSE){
         LOG_ERR("Wakeup fails\r\n");
         return FALSE;        
    }
    
    // Command complete event
    if(BT_ReadExpectedEvent(
        hComPortFile, 
        pAckEvent, 
        sizeof(pAckEvent),
        0x0E,
        &dwReadLength,
        TRUE,
        0xFC77,
        TRUE,
        0x00) == FALSE){

        LOG_ERR("Read UART complete event fails\r\n");
        return FALSE;
    }
    
    return TRUE;
}

extern "C"
BOOL BT_DownPatch(
    HANDLE  hComPortFile,
    LPBYTE  cbPatch,
    DWORD   dwPatchLen
    )
{
    DWORD   dwReadLength;
    UCHAR   pReadBuffer[20];    

    // Vendor Specific HCI CMD
    UCHAR   HCI_VS_PATCH_START[]= {0x01, 0xC4, 0xFC, 0xF5, 0x00, 0xF0, 0x00, 0x00, 0x00};   //[data]
    UCHAR   HCI_VS_PATCH_CONTINUE[]= {0x01, 0xC4, 0xFC, 0xF5, 0x01, 0xF0, 0x00, 0x00, 0x00};//[data] 240 Byets/time
    UCHAR   HCI_VS_PATCH_END[]= {0x01, 0xC4, 0xFC, 0xF5, 0x02, 0xF0, 0x00, 0x00, 0x00};     //[data] last[3]=last[5]+5, last[5] is the rest data to send
    
    UCHAR   ucTmpBuf[240];
    
    // Start download patch   
    if(dwPatchLen >= 240) //0xf0 = 240 
    {
        if(bt_send_data(hComPortFile, HCI_VS_PATCH_START, sizeof(HCI_VS_PATCH_START)) < 0){
            LOG_ERR("Write patch start header fails\r\n");
            return FALSE;
        }
        
        memcpy(ucTmpBuf, cbPatch, 240);
        
        if(bt_send_data(hComPortFile, ucTmpBuf, 240) < 0){	
            LOG_ERR("Write patch start data fails\r\n");	
            return FALSE;
        }    
        
        if(BT_ReadExpectedEvent(
            hComPortFile, 
            pReadBuffer, 
            sizeof(pReadBuffer),
            0x0E,
            &dwReadLength,
            TRUE,
            0xFCC4,
            TRUE,
            0x00) == FALSE){
            
            LOG_ERR("Read complete event fails\r\n");
            return FALSE; 
        }
        
        dwPatchLen -= 240;
        cbPatch += 240;
    }
    
    // Continue download patch 
    while(dwPatchLen >= 240) //0xf0 = 240 
    {
        if(bt_send_data(hComPortFile, HCI_VS_PATCH_CONTINUE, sizeof(HCI_VS_PATCH_CONTINUE)) < 0){
            LOG_ERR("Write patch continue header fails\r\n");
            return FALSE;
        }
        
        memcpy(ucTmpBuf, cbPatch, 240);
        
        if(bt_send_data(hComPortFile, ucTmpBuf, 240) < 0){	
            LOG_ERR("Write patch continue data fails\r\n");	
            return FALSE;
        }   
        
        if(BT_ReadExpectedEvent(
            hComPortFile, 
            pReadBuffer, 
            sizeof(pReadBuffer),
            0x0E,
            &dwReadLength,
            TRUE,
            0xFCC4,
            TRUE,
            0x00) == FALSE){
            
            LOG_ERR("Read complete event fails\r\n");
            return FALSE; 
        }	
        
        dwPatchLen -= 240;
        cbPatch += 240;   
    }
    
    // Download the rest data less than 0xf0
    if(dwPatchLen < 240) //0xf0 = 240
    {
        HCI_VS_PATCH_END[5] = (UCHAR)dwPatchLen;
        HCI_VS_PATCH_END[3] = HCI_VS_PATCH_END[5]+5;
        
        if(bt_send_data(hComPortFile, HCI_VS_PATCH_END, sizeof(HCI_VS_PATCH_END)) < 0){
            LOG_ERR("Write patch end header fails\r\n");
            return FALSE;
        }
        
        if(dwPatchLen){
            memcpy(ucTmpBuf, cbPatch, dwPatchLen);
            
            if(bt_send_data(hComPortFile, ucTmpBuf, dwPatchLen) < 0){	
                LOG_ERR("Write patch continue data fails\r\n");	
                return FALSE;
            }   
        }
        
        if(BT_ReadExpectedEvent(
            hComPortFile, 
            pReadBuffer, 
            sizeof(pReadBuffer),
            0x0E,
            &dwReadLength,
            TRUE,
            0xFCC4,
            TRUE,
            0x00) == FALSE){
            
            LOG_ERR("Read complete event fails\r\n");
            return FALSE; 
        }
    }
    
    LOG_DBG("Download patch success\r\n");
    
    return TRUE;
}
#endif

extern "C" 
BOOL BT_ForceAlwaysSleep(HANDLE hComPortFile)
{
    UCHAR   HCI_VS_SET_ALWAYS_SLEEP[] = {0x01, 0xD8, 0xFC, 0x0};
    
    if(bt_send_data(hComPortFile, HCI_VS_SET_ALWAYS_SLEEP, sizeof(HCI_VS_SET_ALWAYS_SLEEP)) < 0){    
        LOG_ERR("Write always sleep command fails\r\n");
        return FALSE;
    }
    
    return TRUE;  
}

extern "C"
BOOL BT_HCIReset(HANDLE hComPortFile)
{
    UCHAR   HCI_RESET[] = {0x01, 0x03, 0x0C, 0x0};
    DWORD   dwReadLength = 0;
    UCHAR   pAckEvent[20];
    
    if(bt_send_data(hComPortFile, HCI_RESET, sizeof(HCI_RESET)) < 0){       
        LOG_ERR("Write HCI reset command fails\r\n");
        return FALSE;
    }
    
    if(BT_ReadExpectedEvent(
        hComPortFile, 
        pAckEvent, 
        sizeof(pAckEvent),
        0x0E,
        &dwReadLength,
        TRUE,
        0x0C03,
        TRUE,
        0x0) == FALSE){
        
        LOG_ERR("Read reset complete event fails\n");
        return FALSE;
    }
    
    return TRUE;
}
