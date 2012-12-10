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

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <dlfcn.h>
#include <string.h>

#include "bt_em.h"
#include "cust_bt.h"


typedef unsigned long DWORD;
typedef unsigned long* PDWORD;
typedef unsigned long* LPDWORD;
typedef unsigned short USHORT;
typedef unsigned char UCHAR;
typedef unsigned char BYTE;
typedef unsigned long HANDLE;
typedef void VOID;
typedef void* LPCVOID;
typedef void* LPVOID;
typedef void* LPOVERLAPPED;
typedef unsigned char* PUCHAR;
typedef unsigned char* PBYTE;
typedef unsigned char* LPBYTE;

#define TRUE           1
#define FALSE          0


#define LOG_TAG         "BT_EM "
#include <cutils/log.h>

#define BT_EM_DEBUG     1
#define ERR(f, ...)     LOGE("%s: " f, __func__, ##__VA_ARGS__)
#define WAN(f, ...)     LOGW("%s: " f, __func__, ##__VA_ARGS__)
#if BT_EM_DEBUG
#define DBG(f, ...)     LOGD("%s: " f, __func__, ##__VA_ARGS__)
#define TRC(f)          LOGW("%s #%d", __func__, __LINE__)
#else
#define DBG(...)        ((void)0)
#define TRC(f)          ((void)0)
#endif

#ifndef BT_DRV_MOD_NAME
#define BT_DRV_MOD_NAME     "bluetooth"
#endif

#define DONOT_USE_RFKILL            1
#ifdef  DONOT_USE_RFKILL
#define BTWLAN_DEVNAME              "/dev/btwlan_em"
#define BTWLAN_EM_IOC_MAGIC         0xf6
#define BTWLAN_EM_IOCTL_SET_BTPWR   _IOWR(BTWLAN_EM_IOC_MAGIC, 0, uint32_t)
#endif


struct uart_t {
    char *type;
    int  m_id;
    int  p_id;
    int  proto;
    int  init_speed;
    int  speed;
    int  flags;
    char *bdaddr;
    int  (*init) (int fd, struct uart_t *u, struct termios *ti);
    int  (*post) (int fd, struct uart_t *u, struct termios *ti);
};

/* define uart_t.flags */
#define FLOW_CTL_HW     0x0001
#define FLOW_CTL_SW     0x0002
#define FLOW_CTL_NONE   0x0000
#define FLOW_CTL_MASK   0x0003


//===============        Global Variables         =======================

static int   bt_fd = -1;
static int   bt_rfkill_id = -1;
static char *bt_rfkill_state_path = NULL;

// mtk bt library
static void *glib_handle = NULL;
typedef int (*INIT)(int fd, struct uart_t *u, struct termios *ti);
typedef int (*UNINIT)(int fd);
typedef int (*WRITE)(int fd, unsigned char *buffer, unsigned long len);
typedef int (*READ)(int fd, unsigned char *buffer, unsigned long len);

INIT    mtk = NULL;
UNINIT  bt_restore = NULL;
WRITE   bt_send_data = NULL;
READ    bt_receive_data = NULL;


//default patch
//--------------------------------6620--------------------------------
#if defined MTK_MT6620
static char COMBO_BUILT_IN_PATCH_FILE_NAME_E1[] = "/system/etc/firmware/mt6620_patch_hdr.bin";
static char COMBO_BUILT_IN_PATCH_FILE_NAME_E3[] = "/system/etc/firmware/mt6620_patch_e3_hdr.bin";
static char COMBO_BUILT_IN_PATCH_FILE_NAME_E6[] = "/system/etc/firmware/mt6620_patch_e6_hdr.bin";
//--------------------------------6622--------------------------------
#elif defined MTK_MT6622
#define BT_PATCH_EXT_ENABLE   0
#if BT_PATCH_EXT_ENABLE
static char BT_UPDATE_PATCH_EXT_FILE_NAME[64] = "/data/MTK_MT6622_E2_Patch_ext.nb0";
static char BT_BUILT_IN_PATCH_EXT_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6622_E2_Patch_ext.nb0";
#endif
static char BT_UPDATE_PATCH_FILE_NAME[64] = "/data/MTK_MT6622_E2_Patch.nb0";
static char BT_BUILT_IN_PATCH_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6622_E2_Patch.nb0";
//--------------------------------6626--------------------------------
#elif defined MTK_MT6626
#define BT_PATCH_EXT_ENABLE   0
#if BT_PATCH_EXT_ENABLE
static char BT_UPDATE_PATCH_EXT_FILE_NAME[64] = "/data/MTK_MT6626_E2_Patch_ext.nb0";
static char BT_BUILT_IN_PATCH_EXT_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6626_E2_Patch_ext.nb0";
#endif
static char BT_UPDATE_PATCH_FILE_NAME[64] = "/data/MTK_MT6626_E2_Patch.nb0";
static char BT_BUILT_IN_PATCH_FILE_NAME[64] = "/system/etc/firmware/MTK_MT6626_E2_Patch.nb0";
//--------------------------------6628--------------------------------
#elif defined MTK_MT6628
static char COMBO_BUILT_IN_PATCH_FILE_NAME_E1[] = "/system/etc/firmware/mt6628_patch_e1_hdr.bin";
static char COMBO_BUILT_IN_PATCH_FILE_NAME_E2[] = "/system/etc/firmware/mt6628_patch_e2_hdr.bin"; 
#endif

static BT_HW_ECO  bt_hw_eco;

/**************************************************************************
  *                         F U N C T I O N S                             *
***************************************************************************/

static BOOL BT_DisableSleepMode(void);


#ifndef MTK_COMBO_SUPPORT
static int bt_init_rfkill(void) 
{
    char path[128];
    char buf[32];
    int fd, id;
    ssize_t sz;
    
    TRC();
    
    for (id = 0; id < 10 ; id++) {
        snprintf(path, sizeof(path), "/sys/class/rfkill/rfkill%d/type", id);
        fd = open(path, O_RDONLY);
        if (fd < 0) {
            ERR("Open %s fails: %s(%d)\n", path, strerror(errno), errno);
            return -1;
        }
        sz = read(fd, &buf, sizeof(buf));
        close(fd);
        if (sz >= (ssize_t)strlen(BT_DRV_MOD_NAME) && 
            memcmp(buf, BT_DRV_MOD_NAME, strlen(BT_DRV_MOD_NAME)) == 0) {
            bt_rfkill_id = id;
            break;
        }
    }

    if (id == 10)
        return -1;

    asprintf(&bt_rfkill_state_path, "/sys/class/rfkill/rfkill%d/state", 
        bt_rfkill_id);
    
    return 0;
}

static int bt_set_power(int on) 
{
    int sz;
    int fd = -1;
    int ret = -1;
#ifdef  DONOT_USE_RFKILL
    const uint32_t buf = (on ? 1 : 0);   
#else
    const char buf = (on ? '1' : '0');
#endif

    TRC();

#ifdef  DONOT_USE_RFKILL
    fd = open(BTWLAN_DEVNAME, O_RDWR);
    if (fd < 0) {
        ERR("Open %s to set BT power fails: %s(%d)", BTWLAN_DEVNAME,
             strerror(errno), errno);
        goto out;
    }
    
    ret = ioctl(fd, BTWLAN_EM_IOCTL_SET_BTPWR, &buf);
    if(ret < 0) {
        ERR("Set BT power %d fails: %s(%d)\n", buf, 
             strerror(errno), errno);
        goto out;
    }
    
#else
    if (bt_rfkill_id == -1) {
        if (bt_init_rfkill()) goto out;
    }

    fd = open(bt_rfkill_state_path, O_WRONLY);
    if (fd < 0) {
        ERR("Open %s to set BT power fails: %s(%d)", bt_rfkill_state_path,
             strerror(errno), errno);
        goto out;
    }
    sz = write(fd, &buf, 1);
    if (sz < 0) {
        ERR("Write %s fails: %s(%d)", bt_rfkill_state_path, 
             strerror(errno), errno);
        goto out;
    }
    ret = 0;
#endif

out:
    if (fd >= 0) close(fd);
    return ret;
}
#endif

BOOL EM_BT_init(void)
{
    struct uart_t u;

    TRC();

#ifndef MTK_COMBO_SUPPORT
    /* in case BT is powered on before test */
    bt_set_power(0);
    
    if(bt_set_power(1) < 0) {
        ERR("BT power on fails\n");
        return -1;
    }
#endif

    glib_handle = dlopen("libbluetooth_mtk.so", RTLD_LAZY);
    if (!glib_handle){
        ERR("%s\n", dlerror());
        goto error;
    }
    
    mtk = dlsym(glib_handle, "mtk");
    bt_restore = dlsym(glib_handle, "bt_restore");
    bt_send_data = dlsym(glib_handle, "bt_send_data");
    bt_receive_data = dlsym(glib_handle, "bt_receive_data");

    if (!mtk || !bt_restore || !bt_send_data || !bt_receive_data){
        ERR("Can't find function symbols %s\n", dlerror());
        goto error;
    }

#ifndef MTK_COMBO_SUPPORT
    u.flags &= ~FLOW_CTL_MASK;
    u.flags |= FLOW_CTL_SW;
    u.speed = CUST_BT_BAUD_RATE;
#endif

    bt_fd = mtk(-1, &u, NULL);
    if (bt_fd < 0)
        goto error;

    DBG("BT is enabled success\n");

#ifndef MTK_COMBO_SUPPORT
    /* BT EM driver DONOT handle sleep mode and EINT,
       so disable Host and Controller sleep in EM 
       on standalone chip;
       on combo chip, THIS IS NO NEED
       */
    BT_DisableSleepMode();
#endif

    return TRUE;

error:
    if (glib_handle){
        dlclose(glib_handle);
        glib_handle = NULL;
    }

#ifndef MTK_COMBO_SUPPORT
    bt_set_power(0);
#endif

    return FALSE;
}

void EM_BT_deinit(void)
{
    TRC();
    
    if (!glib_handle || !bt_restore){
        ERR("mtk bt library is unloaded!\n");
    }
    else{
        if (bt_fd < 0){
            ERR("bt driver fd is invalid!\n");
        }
        else{
            bt_restore(bt_fd);
            bt_fd = -1;
        }
        dlclose(glib_handle);
        glib_handle = NULL;
    }

#ifndef MTK_COMBO_SUPPORT
    bt_set_power(0); /* shutdown BT */
#endif

    return;
}

BOOL EM_BT_write(
    unsigned char *peer_buf, 
    int  peer_len)
{
    TRC();
    
    if (peer_buf == NULL){
        ERR("NULL write buffer\n");
        return FALSE;
    }
    
    if ((peer_buf[0] != 0x01) && (peer_buf[0] != 0x02) && (peer_buf[0] != 0x03)){
        ERR("Invalid packet type 0x%02x\n", peer_buf[0]);
        return FALSE;    
    }
    
    if (!glib_handle || !bt_send_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    if (bt_send_data(bt_fd, peer_buf, peer_len) < 0){
        return FALSE;
    }
    
    return TRUE;
}

BOOL EM_BT_read(
    unsigned char *peer_buf, 
    int  peer_len,
    int *piResultLen)
{
    UCHAR ucHeader = 0;
    int iLen = 0, pkt_len = 0, count = 0;
	  
    TRC();
	  
    if (peer_buf == NULL){
        ERR("NULL read buffer\n");
        return FALSE;
    }
    
    if (!glib_handle || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }

LOOP:
    if(bt_receive_data(bt_fd, &ucHeader, sizeof(ucHeader)) < 0){
        count ++;
        if (count < 3)
            goto LOOP;
        else{
            ERR("Read packet header fails\n");
            return FALSE;
        }
    }
    
    peer_buf[0] = ucHeader;
    iLen ++;
    
    switch (ucHeader)
    {
        case 0x04:
            DBG("Receive HCI event\n");
            if(bt_receive_data(bt_fd, &peer_buf[1], 2) < 0){
                ERR("Read event header fails\n");
                *piResultLen = iLen;
                return FALSE;
            }
            
            iLen += 2;
            pkt_len = (int)peer_buf[2];
            if((iLen + pkt_len) > peer_len){
                ERR("Read buffer overflow! packet len %d\n", iLen + pkt_len);
                *piResultLen = iLen;
                return FALSE;
            }
            
            if(bt_receive_data(bt_fd, &peer_buf[3], pkt_len) < 0){
                ERR("Read event param fails\n");
                *piResultLen = iLen;
                return FALSE;
            }
            
            iLen += pkt_len;
            *piResultLen = iLen;
            break;
                
        case 0x02:
            DBG("Receive ACL data\n");
            if(bt_receive_data(bt_fd, &peer_buf[1], 4) < 0){
                ERR("Read ACL header fails\n");
                *piResultLen = iLen;
                return FALSE;
            }
            
            iLen += 4;
            pkt_len = (((int)peer_buf[4]) << 8);
            pkt_len += peer_buf[3];//little endian
            if((iLen + pkt_len) > peer_len){
                ERR("Read buffer overflow! packet len %d\n", iLen + pkt_len);
                *piResultLen = iLen;
                return FALSE;
            }
            
            if(bt_receive_data(bt_fd, &peer_buf[5], pkt_len) < 0){
                ERR("Read ACL data fails\n");
                *piResultLen = iLen;
                return FALSE;
            }
            
            iLen += pkt_len;
            *piResultLen = iLen;
            break;
            
        case 0x03:
            DBG("Receive SCO data\n");
            if(bt_receive_data(bt_fd, &peer_buf[1], 3) < 0){
                ERR("Read SCO header fails\n");
                *piResultLen = iLen;
                return FALSE;
            }
            
            iLen += 3;
            pkt_len = (int)peer_buf[3];
            if((iLen + pkt_len) > peer_len){
                ERR("Read buffer overflow! packet len %d\n", iLen + pkt_len);
                *piResultLen = iLen;
                return FALSE;
            }
            
            if(bt_receive_data(bt_fd, &peer_buf[4], pkt_len) < 0){
                ERR("Read SCO data fails\n");
                *piResultLen = iLen;
                return FALSE;
            }
            
            iLen += pkt_len;
            *piResultLen = iLen;
            break;                
            
        default:
            ERR("Unexpected BT packet header %02x\n", ucHeader);
            *piResultLen = iLen;
            return FALSE;
    }
    
    return TRUE;
}

static BOOL BT_DisableSleepMode(void)
{
    UCHAR   HCI_VS_SLEEP[] = 
                {0x01, 0x7A, 0xFC, 0x07, 0x00, 0x40, 0x1F, 0x00, 0x00, 0x00, 0x04};
    UCHAR   pAckEvent[7];
    UCHAR   ucEvent[] = {0x04, 0x0E, 0x04, 0x01, 0x7A, 0xFC, 0x00};
    
    TRC();
    
    if (!glib_handle || !bt_send_data || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    if(bt_send_data(bt_fd, HCI_VS_SLEEP, sizeof(HCI_VS_SLEEP)) < 0){
        ERR("Send disable sleep mode command fails errno %d\n", errno);
        return FALSE;
    }
    
    if(bt_receive_data(bt_fd, pAckEvent, sizeof(pAckEvent)) < 0){
        ERR("Receive event fails errno %d\n", errno);
        return FALSE;
    }
    
    if(memcmp(pAckEvent, ucEvent, sizeof(ucEvent))){
        ERR("Receive unexpected event\n");
        return FALSE;
    }
    
    return TRUE;
}


void EM_BT_getChipInfo(BT_CHIP_ID *chip_id, BT_HW_ECO *eco_num)
{
    BT_CHIP_ID id;
      
    UCHAR HCI_VS_GET_HW_VER[] = {0x01, 0xD1, 0xFC, 0x04, 0x00, 0x00, 0x00, 0x80};
    UCHAR pAckEvent[11];
    UCHAR ucEvent[] = {0x04, 0x0E, 0x08, 0x01, 0xD1, 0xFC, 0x00};
    
    TRC();
    
#ifdef MTK_MT6611
    id = BT_CHIP_ID_MT6611;
#elif defined MTK_MT6612
    id = BT_CHIP_ID_MT6612;
#elif defined MTK_MT6616
    id = BT_CHIP_ID_MT6616;
#elif defined MTK_MT6620
    id = BT_CHIP_ID_MT6620;
#elif defined MTK_MT6622
    id = BT_CHIP_ID_MT6622;
#elif defined MTK_MT6626
    id = BT_CHIP_ID_MT6626;
#elif defined MTK_MT6628
    id = BT_CHIP_ID_MT6628;
#endif
    *chip_id = id;
    *eco_num = BT_HW_ECO_UNKNOWN;
    
    // Try to get chip HW ECO
    if (!glib_handle || !bt_send_data || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return;
    }
    
    if(bt_send_data(bt_fd, HCI_VS_GET_HW_VER, sizeof(HCI_VS_GET_HW_VER)) < 0){
        ERR("Send get HW version command fails errno %d\n", errno);
        return;
    }
    
    if(bt_receive_data(bt_fd, pAckEvent, sizeof(pAckEvent)) < 0){
        ERR("Receive event fails errno %d\n", errno);
        return;
    }
    
    if(memcmp(pAckEvent, ucEvent, sizeof(ucEvent))){
        ERR("Receive unexpected event\n");
    }
    else{
        DBG("event 0xbbaa: %02x %02x\n", pAckEvent[8], pAckEvent[7]);
        
    #ifdef MTK_COMBO_SUPPORT
        /* Combo chip */
        switch (pAckEvent[7]){
        #ifdef MTK_MT6620
          case 0x00:
               bt_hw_eco = BT_HW_ECO_E1;
               break;
          case 0x01:
               bt_hw_eco = BT_HW_ECO_E2;
               break;
          case 0x10:
               bt_hw_eco = BT_HW_ECO_E3;
               break;
          case 0x11:
               bt_hw_eco = BT_HW_ECO_E4;
               break;
          case 0x30:
               bt_hw_eco = BT_HW_ECO_E6;
               break;
          case 0x31:
               bt_hw_eco = BT_HW_ECO_E7;
               break;
        #else
          case 0x00:
               bt_hw_eco = BT_HW_ECO_E1;
               break;
          case 0x10:
               bt_hw_eco = BT_HW_ECO_E2;
               break;
        #endif
          default:
               bt_hw_eco = BT_HW_ECO_UNKNOWN;
               break;
        }
    #else
        /* Standalone BT chip */
        switch (pAckEvent[7]){
          case 0x00:
               bt_hw_eco = BT_HW_ECO_E1;
               break;
          case 0x01:
               bt_hw_eco = BT_HW_ECO_E2;
               break;
          case 0x02:
               bt_hw_eco = BT_HW_ECO_E3;
               break;
          case 0x03:
               bt_hw_eco = BT_HW_ECO_E4;
               break;
          default:
               bt_hw_eco = BT_HW_ECO_UNKNOWN;
               break;
        }
    #endif
        *eco_num = bt_hw_eco;
    }
    
    return;
}

void EM_BT_getPatchInfo(char *patch_id, unsigned long *patch_len)
{
    DWORD   dwPatchExtLen = 0;
    FILE*   pPatchExtFile = NULL;
    DWORD   dwPatchLen = 0;
    FILE*   pPatchFile = NULL;
    size_t  szReadLen = 0;
    char    patch_hdr[30] = "N/A";
    
    TRC();

#ifdef MTK_COMBO_SUPPORT
    DBG("Load Combo firmware patch\r\n");
    
    switch (bt_hw_eco){
    #ifdef MTK_MT6620
      case BT_HW_ECO_E1:
      case BT_HW_ECO_E2:
           pPatchFile = fopen(COMBO_BUILT_IN_PATCH_FILE_NAME_E1, "rb");
           if(pPatchFile != NULL) {
               WAN("Open %s\r\n", COMBO_BUILT_IN_PATCH_FILE_NAME_E1);
           }
           else {
               ERR("Can't get valid patch file\r\n");
           }
           break;
      case BT_HW_ECO_E3:
      case BT_HW_ECO_E4:
      case BT_HW_ECO_E5:
      	   pPatchFile = fopen(COMBO_BUILT_IN_PATCH_FILE_NAME_E3, "rb");
           if(pPatchFile != NULL){
               WAN("Open %s\r\n", COMBO_BUILT_IN_PATCH_FILE_NAME_E3);
           }
           else {
               ERR("Can't get valid patch file\r\n");
           }
      	   break;
      case BT_HW_ECO_E6:
      case BT_HW_ECO_E7:
      	   pPatchFile = fopen(COMBO_BUILT_IN_PATCH_FILE_NAME_E6, "rb");
           if(pPatchFile != NULL){
               WAN("Open %s\r\n", COMBO_BUILT_IN_PATCH_FILE_NAME_E6);
           }
           else {
               ERR("Can't get valid patch file\r\n");
           }
      	   break;
    #else
      case BT_HW_ECO_E1:
           pPatchFile = fopen(COMBO_BUILT_IN_PATCH_FILE_NAME_E1, "rb");
           if(pPatchFile != NULL) {
               WAN("Open %s\r\n", COMBO_BUILT_IN_PATCH_FILE_NAME_E1);
           }
           else {
               ERR("Can't get valid patch file\r\n");
           }
           break;
      case BT_HW_ECO_E2:
           pPatchFile = fopen(COMBO_BUILT_IN_PATCH_FILE_NAME_E2, "rb");
           if(pPatchFile != NULL) {
               WAN("Open %s\r\n", COMBO_BUILT_IN_PATCH_FILE_NAME_E2);
           }
           else {
               ERR("Can't get valid patch file\r\n");
           }
           break;
    #endif
      default:
      	   ERR("No ECO version, don't known which patch to load\n");
      	   break;
    }
    
    if(pPatchFile != NULL){
       if(fseek(pPatchFile, 0, SEEK_END) != 0){
            ERR("fseek patch file fails errno: %d\n", errno);
        }
        else{
            dwPatchLen = ftell(pPatchFile);
            DBG("Patch file size %d\n", (int)dwPatchLen);
            if (dwPatchLen <= 28){
                // patch header needs 28 bytes at least
                ERR("Patch error len!\n");
            }
            
            /* back to file beginning */
            rewind(pPatchFile);
            
            memset(patch_hdr, 0, sizeof(patch_hdr));
            szReadLen = fread(patch_hdr, 1, 16, pPatchFile);
            if (szReadLen < 16){
               patch_hdr[szReadLen] = '\0';
            }
            else{
               patch_hdr[14] = ' ';
               szReadLen = fread(patch_hdr + 15, 1, 4, pPatchFile);
               szReadLen += 15;
               patch_hdr[szReadLen] = '\0';
            }
            
            DBG("Patch hdr: %s\n", patch_hdr);
        }
    }
    
    *patch_len = dwPatchLen;
    strcpy(patch_id, patch_hdr);
    
#else
    DBG("Load BT firmware patch\r\n");

#if BT_PATCH_EXT_ENABLE
    pPatchExtFile = fopen(BT_UPDATE_PATCH_EXT_FILE_NAME, "rb"); 
    
    /* if there is no adhoc file, use built-in patch file under system etc firmware */
    if(pPatchExtFile == NULL){
        pPatchExtFile = fopen(BT_BUILT_IN_PATCH_EXT_FILE_NAME, "rb");
        if(pPatchExtFile != NULL) {
            WAN("Open %s\r\n", BT_BUILT_IN_PATCH_EXT_FILE_NAME);
        }
        else {
            ERR("Can't get valid patch ext file\r\n");
        }
    }
    else {
        WAN("Open %s\r\n", BT_UPDATE_PATCH_EXT_FILE_NAME);       
    }
    
    /* file exists */
    if(pPatchExtFile != NULL){
        if(fseek(pPatchExtFile, 0, SEEK_END) != 0){
            ERR("fseek patch ext file fails errno: %d\n", errno);
        }else{
            dwPatchExtLen = ftell(pPatchExtFile);
            DBG("Patch ext file size %d\n", (int)dwPatchExtLen);
        }
    }
#endif

    /* Use data directory first. for future update test convinience */
    pPatchFile = fopen(BT_UPDATE_PATCH_FILE_NAME, "rb"); 
    
    /* if there is no adhoc file, use built-in patch file under system etc firmware */
    if(pPatchFile == NULL){
        pPatchFile = fopen(BT_BUILT_IN_PATCH_FILE_NAME, "rb");
        if(pPatchFile != NULL) {
            WAN("Open %s\r\n", BT_BUILT_IN_PATCH_FILE_NAME);
        }
        else {
            ERR("Can't get valid patch file\r\n");
        }
    }
    else {
        WAN("Open %s\r\n", BT_UPDATE_PATCH_FILE_NAME);       
    }
    
    /* file exists */
    if(pPatchFile != NULL){
        if(fseek(pPatchFile, 0, SEEK_END) != 0){
            ERR("feek patch file fails errno: %d\n", errno);
        }else{            
            dwPatchLen = ftell(pPatchFile);
            DBG("Patch file size %d\n", (int)dwPatchLen);
        }
    }
    
    *patch_len = dwPatchLen + dwPatchExtLen;
    // Standalone chip no patch header info
    strcpy(patch_id, "N/A");
#endif

    return;
}
