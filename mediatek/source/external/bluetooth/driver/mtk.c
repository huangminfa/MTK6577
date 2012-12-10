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
#include <string.h>
#include <termios.h>
#include <time.h>
#include <fcntl.h>
#include <linux/serial.h> 

/* use nvram */
#include "CFG_BT_File.h"
#include "CFG_BT_Default.h"
#include "CFG_file_lid.h"
#include "libnvram.h"

#include "bt_kal.h"


/* This file name must be referred to nvram_bt.c */
#define BT_NVRAM_DATA_CLONE_FILE_NAME    "/data/BT_Addr"

	
/* defined in hciattach.c */
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

/* update uart_t.flags */
#define FLOW_CTL        0x0001
#define FLOW_CTL_HW     0x0001
#define FLOW_CTL_SW     0x0002
#define FLOW_CTL_NONE   0x0000
#define FLOW_CTL_MASK   0x0003


/**************************************************************************
  *                         F U N C T I O N S                             *
***************************************************************************/

extern
BOOL BT_InitDevice(
    HANDLE  hComPortFile,
    PBYTE   ucNvRamData,
    DWORD   dwBaud,
    DWORD   dwHostBaud,
    DWORD   dwUseFlowControl,
    SETUP_UART_PARAM  setup_uart_param
);

extern
BOOL BT_DeinitDevice(
    HANDLE  hComPortFile
);


/* Initialize UART driver */
static int init_uart(char *dev)
{
    struct termios ti;
    struct serial_struct ss;
    int fd, i;
    
    LOG_TRC();
     
    fd = open(dev, O_RDWR | O_NOCTTY | O_NONBLOCK);
    if (fd < 0) {
        LOG_ERR("Can't open serial port\n");
        return -1;
    }

#ifndef MTK_COMBO_SUPPORT
    tcflush(fd, TCIOFLUSH);
	  
    if (tcgetattr(fd, &ti) < 0) {
        LOG_ERR("Can't get UART port setting");
        return -1;
    }
    
    cfmakeraw(&ti);
    
    ti.c_cflag |= CLOCAL;
    ti.c_lflag = 0;
    
    ti.c_cflag &= ~CRTSCTS;
    ti.c_iflag &= ~(IXON | IXOFF | IXANY | 0x80000000);
    
    if (tcsetattr(fd, TCSANOW, &ti) < 0) {
        LOG_ERR("Can't set UART port setting");
        return -1;
    }
    
    /* Clear the cust flag */
    if((ioctl(fd, TIOCGSERIAL, &ss)) < 0){
        LOG_ERR("Can't get serial_struct info: %s\n", strerror(errno));
        return -1;
    }
	  
    if (ss.flags & ASYNC_SPD_CUST) {
        ss.flags &= ~ASYNC_SPD_CUST;
        if((ioctl(fd, TIOCSSERIAL, &ss)) < 0){
            LOG_ERR("Can't set serial_struct: %s\n", strerror(errno));
            return -1;
        }
    }
    
    /* Set initial baudrate */
    cfsetospeed(&ti, B115200);
    cfsetispeed(&ti, B115200);
    
    if (tcsetattr(fd, TCSANOW, &ti) < 0) {
        LOG_ERR("Can't set UART port setting");
        return -1;
    }
    
    tcflush(fd, TCIOFLUSH);
#endif
    return fd;
}

#ifndef MTK_COMBO_SUPPORT
static int uart_speed(int s)
{
    switch (s) {
    case 9600:
         return B9600;
    case 19200:
         return B19200;
    case 38400:
         return B38400;
    case 57600:
         return B57600;
    case 115200:
         return B115200;
    case 230400:
         return B230400;
    case 460800:
         return B460800;
    case 500000:
         return B500000;
    case 576000:
         return B576000;
    case 921600:
         return B921600;
    case 1000000:
         return B1000000;
    case 1152000:
         return B1152000;
    case 1500000:
         return B1500000;
    case 2000000:
         return B2000000;
#ifdef B2500000
    case 2500000:
         return B2500000;
#endif
#ifdef B3000000
    case 3000000:
         return B3000000;
#endif
#ifdef B3500000
    case 3500000:
         return B3500000;
#endif
#ifdef B4000000
    case 4000000:
         return B4000000;
#endif
    default:
         return B57600;
    }
}

static int set_speed(int fd, struct termios *ti, int speed)
{
    struct serial_struct ss;
    int baudenum = uart_speed(speed);

    if ((baudenum == B57600) && (speed != 57600)) {
        LOG_DBG("non-standard baudrate: %d\n", speed);
        
        tcflush(fd, TCIFLUSH); /*handle unrecevie char*/
        if((ioctl(fd, TIOCGSERIAL, &ss)) < 0){
            LOG_ERR("Can't get serial_struct info: %s\n", strerror(errno)); 
            return -1;
        }
        ss.flags |= ASYNC_SPD_CUST;
        ss.custom_divisor = ss.baud_base / speed;
        if((ss.custom_divisor * speed) != ss.baud_base){
            /* This means has fractions notify UART driver */
            ss.custom_divisor = speed | (1<<31);
        }
        if((ioctl(fd, TIOCSSERIAL, &ss)) < 0){
            LOG_ERR("Can't set serial_struct: %s\n", strerror(errno));
            return -2;
        }
        
        return 0;
    } 
    else {
        LOG_DBG("standard baudrate: %d -> 0x%08x\n", speed, baudenum);
        
        if((ioctl(fd, TIOCGSERIAL, &ss)) < 0){
            LOG_ERR("Can't get serial_struct info: %s\n", strerror(errno));
            return -1;
        }
        ss.flags &= ~ASYNC_SPD_CUST;
        if((ioctl(fd, TIOCSSERIAL, &ss)) < 0){
            LOG_ERR("Can't set serial_struct: %s\n", strerror(errno));
            return -2;
        }
        
        cfsetospeed(ti, baudenum);
        cfsetispeed(ti, baudenum);
        return tcsetattr(fd, TCSANOW, ti);
    }
}

/* Used as host uart param setup callback */
int setup_uart_param(
    unsigned long hComPort, 
    int iBaudrate, 
    int iFlowControl)
{
    struct termios ti;
    int  fd;
    
    LOG_DBG("setup_uart_param %d %d\n", iBaudrate, iFlowControl);
    
    fd = (int)hComPort;
    if (fd < 0) {
        LOG_ERR("Invalid serial port");
        return -1;
    }
    
    tcflush(fd, TCIOFLUSH);
    
    if (tcgetattr(fd, &ti) < 0) {
        LOG_ERR("Can't get port settings");
        return -1;
    }
    
    cfmakeraw(&ti);
    
    ti.c_cflag |= CLOCAL;
    
    ti.c_cflag &= ~CRTSCTS;
    ti.c_iflag &= ~(IXON | IXOFF | IXANY | 0x80000000);
    
    /* HW flow control */
    if (iFlowControl == 1){
        ti.c_cflag |= CRTSCTS;
    }
    else if (iFlowControl == 2){
        /* SW flow control */
        //ti.c_iflag |= (IXON | IXOFF | IXANY);
        ti.c_iflag |= 0x80000000;
    }
    
    if (tcsetattr(fd, TCSANOW, &ti) < 0) {
        LOG_ERR("Can't set port settings");
        return -1;
    }
    
    /* Set baudrate */
    if (set_speed(fd, &ti, iBaudrate) < 0) {
        LOG_ERR("Can't set initial baud rate");
        return -1;
    }
    
    tcflush(fd, TCIOFLUSH);
    
    return fd;
}
#endif

static int bt_read_nvram(unsigned char *ucNvRamData)
{
    F_ID bt_nvram_fd = {0};
    int rec_size = 0;
    int rec_num = 0;
    int bt_cfgfile_fd = -1;
    ap_nvram_btradio_mt6610_struct bt_nvram;
    
    LOG_TRC();
    
    /* Try Nvram first */
    bt_nvram_fd = NVM_GetFileDesc(AP_CFG_RDEB_FILE_BT_ADDR_LID, &rec_size, &rec_num, ISWRITE);
    if(bt_nvram_fd.iFileDesc >= 0){
        if(rec_num != 1){
            LOG_ERR("Unexpected record num %d", rec_num);
            NVM_CloseFileDesc(bt_nvram_fd);
            return -1;
        }
        
        if(rec_size != sizeof(ap_nvram_btradio_mt6610_struct)){
            LOG_ERR("Unexpected record size %d ap_nvram_btradio_mt6610_struct %d",
                    rec_size, sizeof(ap_nvram_btradio_mt6610_struct));
            NVM_CloseFileDesc(bt_nvram_fd);
            return -1;
        }
        
        if(read(bt_nvram_fd.iFileDesc, &bt_nvram, rec_num*rec_size) < 0){
            LOG_ERR("Read NVRAM fails errno %d\n", errno);
            NVM_CloseFileDesc(bt_nvram_fd);
            return -1;
        }
        
        NVM_CloseFileDesc(bt_nvram_fd);
    }
    else{
        LOG_WAN("Open BT NVRAM fails errno %d\n", errno);
        
        bt_cfgfile_fd = open(BT_NVRAM_DATA_CLONE_FILE_NAME, O_RDONLY);
        if(bt_cfgfile_fd < 0){
            LOG_ERR("Open %s fails\n", BT_NVRAM_DATA_CLONE_FILE_NAME);
            return -1;
        }
        
        if(read(bt_cfgfile_fd, &bt_nvram, 1*sizeof(ap_nvram_btradio_mt6610_struct)) < 0){
            LOG_ERR("Read %s fails\n", BT_NVRAM_DATA_CLONE_FILE_NAME);
            close(bt_cfgfile_fd);
            return -1;
        }
        
        close(bt_cfgfile_fd);
    }
    
    memcpy(ucNvRamData, &bt_nvram, sizeof(ap_nvram_btradio_mt6610_struct));
    
    return 0;
}


int mtk(int fd, struct uart_t *u, struct termios *ti)
{
    int bt_fd = -1;

#ifdef MTK_COMBO_SUPPORT
    unsigned char ucNvRamData[] = {
                               #if defined(MTK_MT6620)
                                   0x00, 0x00, 0x46, 0x66, 0x20, 0x01,
                               #else
                                   0x00, 0x00, 0x46, 0x66, 0x28, 0x01,
                               #endif
                                   0x60, 0x00,
                                   0x23, 0x10, 0x00, 0x00,
                               #if defined(MTK_MT6620)
                                   0x06, 0x80, 0x00, 0x06, 0x03, 0x06,
                               #else
                                   0x07, 0x80, 0x00, 0x06, 0x05, 0x07,
                               #endif
                                   0x03, 0x40, 0x1F, 0x40, 0x1F, 0x00, 0x04,
                                   0x80, 0x00,
                                   0xFF, 0xFF, 0xFF};
#else
    unsigned char ucNvRamData[] = {
                               #if defined(MTK_MT6622)
                                   0x00, 0x00, 0x46, 0x66, 0x22, 0x01,
                               #else
                                   0x00, 0x00, 0x46, 0x66, 0x26, 0x01,
                               #endif
                                   0x40, /*CapId*/
                                   0x01, /*LinkKeyType*/
                                   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*UintKey*/
                                   0x00, 0x02, 0x10, /*Encryption*/
                                   0x00, /*PinCodeType*/
                                   0x60, 0x00, /*Voice*/
                                   0x23, 0x00, 0x00, 0x00, /*Codec*/
                                   0x06, 0x80, 0x00, 0x06, 0x03,0x06, /*Radio */
                                   0x03, 0x40, 0x1F, 0x40, 0x1F, 0x00, 0x04, /*Sleep*/
                                   0x80, 0x00, /*BtFTR*/
                                   0xFF, 0xFF, 0xFF, /*TxPWOffset*/
                                   0x01, /*ECLK_SEL[1]*/
                                   0x00, /*Reserved1[1]*/
                                   0x00, 0x00, /*Reserved2[2]*/
                                   0x00, 0x00, 0x00, 0x00, /*Reserved3[4]*/
                                   0x00, 0x00, 0x00, 0x00, /*Reserved4[4]*/
                                   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*Reserved5[16]*/
                                   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}; /*Reserved6[16]*/
#endif
    int speed = 0, iUseFlowControl =0;
    SETUP_UART_PARAM uart_setup_callback = NULL;
    
    
    LOG_DBG("%s %d\n", __FILE__, __LINE__);
    
    if (fd < 0) {
        /* For BlueAngel, comm port has not initialized */
        bt_fd = init_uart(CUST_BT_SERIAL_PORT);
        if (bt_fd < 0){
            LOG_ERR("Can't initialize" CUST_BT_SERIAL_PORT);
            return -1;
        }
        fd = bt_fd;
    }

#ifndef MTK_COMBO_SUPPORT
    if((u->flags & FLOW_CTL_MASK) == FLOW_CTL_SW){
        iUseFlowControl = 2;
    } else if((u->flags & FLOW_CTL_MASK) == FLOW_CTL_HW){
        iUseFlowControl = 1;
    } else {
        iUseFlowControl = 0;
    }
    
    speed = u->speed;
    uart_setup_callback = setup_uart_param;
#endif

    /* read nvram data */
    if(bt_read_nvram(ucNvRamData) < 0){
        LOG_ERR("Read nvram data fails\n");
        goto error;
    }

#ifdef MTK_COMBO_SUPPORT
    LOG_WAN("[BDAddr %02x-%02x-%02x-%02x-%02x-%02x][Voice %02x %02x][Codec %02x %02x %02x %02x] \
            [Radio %02x %02x %02x %02x %02x %02x][Sleep %02x %02x %02x %02x %02x %02x %02x][BtFTR %02x %02x][TxPWOffset %02x %02x %02x]\n",
            ucNvRamData[0], ucNvRamData[1], ucNvRamData[2], ucNvRamData[3], ucNvRamData[4], ucNvRamData[5],
            ucNvRamData[6], ucNvRamData[7],
            ucNvRamData[8], ucNvRamData[9], ucNvRamData[10], ucNvRamData[11],
            ucNvRamData[12], ucNvRamData[13], ucNvRamData[14], ucNvRamData[15], ucNvRamData[16], ucNvRamData[17],
            ucNvRamData[18], ucNvRamData[19], ucNvRamData[20], ucNvRamData[21], ucNvRamData[22], ucNvRamData[23], ucNvRamData[24],
            ucNvRamData[25], ucNvRamData[26],
            ucNvRamData[27], ucNvRamData[28], ucNvRamData[29]);
#else
    LOG_WAN("[BDAddr %02x-%02x-%02x-%02x-%02x-%02x][CapId %02x][LinkKeyType %02x] \
            [UintKey %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x] \
            [Encryption %02x %02x %02x][PinCodeType %02x][Voice %02x %02x][Codec %02x %02x %02x %02x] \
            [Radio %02x %02x %02x %02x %02x %02x][Sleep %02x %02x %02x %02x %02x %02x %02x][BtFTR %02x %02x][TxPWOffset %02x %02x %02x] \
            [ECLK_SEL %02x]\n",
            ucNvRamData[0], ucNvRamData[1], ucNvRamData[2], ucNvRamData[3], ucNvRamData[4], ucNvRamData[5],
            ucNvRamData[6], ucNvRamData[7],
            ucNvRamData[8], ucNvRamData[9], ucNvRamData[10], ucNvRamData[11], ucNvRamData[12], ucNvRamData[13], ucNvRamData[14], ucNvRamData[15],
            ucNvRamData[16], ucNvRamData[17], ucNvRamData[18], ucNvRamData[19], ucNvRamData[20], ucNvRamData[21], ucNvRamData[22], ucNvRamData[23],
            ucNvRamData[24], ucNvRamData[25], ucNvRamData[26], ucNvRamData[27],
            ucNvRamData[28], ucNvRamData[29],
            ucNvRamData[30], ucNvRamData[31], ucNvRamData[32], ucNvRamData[33],
            ucNvRamData[34], ucNvRamData[35], ucNvRamData[36], ucNvRamData[37], ucNvRamData[38], ucNvRamData[39],
            ucNvRamData[40], ucNvRamData[41], ucNvRamData[42], ucNvRamData[43], ucNvRamData[44], ucNvRamData[45], ucNvRamData[46],
            ucNvRamData[47], ucNvRamData[48],
            ucNvRamData[49], ucNvRamData[50], ucNvRamData[51],
            ucNvRamData[52]);
#endif


    if(BT_InitDevice(
        fd,
        ucNvRamData,
        speed,
        speed,
        iUseFlowControl,
        uart_setup_callback) == FALSE){

        LOG_ERR("Initialize BT device fails\n");        
        goto error;
    }

    LOG_WAN("mtk() success\n");
    return fd;

error:
    if(bt_fd >= 0)
        close(bt_fd);
    return -1;
}

int bt_restore(int fd)
{
    LOG_DBG("%s %d\n", __FILE__, __LINE__);
    
    BT_DeinitDevice(fd);
    close(fd);
    
    return 0; 
}

int write_comm_port(int fd, unsigned char *buffer, unsigned long len)
{
    int nWritten = 0;
    int bytesToWrite = len;

    if (fd < 0){
        LOG_ERR("No available comm port\n");
        return -EIO;
    }
    
    while (bytesToWrite > 0){
        nWritten = write(fd, buffer, bytesToWrite);
        if (nWritten < 0){
            if(errno == EINTR || errno == EAGAIN)
                break;
            else
                return -errno; // errno used for whole chip reset
        }
        bytesToWrite -= nWritten;
        buffer += nWritten;
    }

    return (len - bytesToWrite);
}

int read_comm_port(int fd, unsigned char *buffer, unsigned long len)
{
    int nRead = 0;
    int bytesToRead = len;

    if (fd < 0){
        LOG_ERR("No available comm port\n");
        return -EIO;
    }
    
    nRead = read(fd, buffer, bytesToRead);
    if (nRead < 0){
        if(errno == EINTR || errno == EAGAIN)
            return 0;
        else
            return -errno; // errno used for whole chip reset
    }
	
    return nRead;
}

int bt_send_data(int fd, unsigned char *buffer, unsigned long len)
{
    int bytesWritten = 0;
    int bytesToWrite = len;

    // Send len bytes data in buffer
    while (bytesToWrite > 0){
        bytesWritten = write_comm_port(fd, buffer, bytesToWrite);
        if (bytesWritten < 0){
            return -1;
        }
        bytesToWrite -= bytesWritten;
        buffer += bytesWritten;
    }

    return 0;
}

int bt_receive_data(int fd, unsigned char *buffer, unsigned long len)
{
    int bytesRead = 0;
    int bytesToRead = len;
    
    int ret = 0;
    struct timeval tv;
    fd_set readfd;
    
    tv.tv_sec = 5;  //SECOND
    tv.tv_usec = 0;   //USECOND
    FD_ZERO(&readfd);
    
    // Hope to receive len bytes
    while (bytesToRead > 0){
    
        FD_SET(fd, &readfd);
        ret = select(fd + 1, &readfd, NULL, NULL, &tv);
        
        if (ret > 0){
            bytesRead = read_comm_port(fd, buffer, bytesToRead);
            if (bytesRead < 0){
                return -1;
            }
            else{
                bytesToRead -= bytesRead;
                buffer += bytesRead;
            }
        }
        else if (ret == 0){
            LOG_ERR("Read comm port timeout = 5000ms!\n");
            return -1;
        }
        else if ((ret == -1) && (errno == EINTR)){
            LOG_ERR("select error EINTR\n");
        }
        else{
            LOG_ERR("select error %s(%d)!\n", strerror(errno), errno);
            return -1;
        }
    }

    return 0;
}
