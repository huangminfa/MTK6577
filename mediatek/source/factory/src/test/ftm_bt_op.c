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

#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include "common.h"
#include "miniui.h"
#include "ftm.h"
#include "cust_bt.h"

#include <stdlib.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>

#ifdef RDA_BT_SUPPORT
#include <linux/serial.h>
#endif
#include <pthread.h>
#include <dlfcn.h>


#ifdef FEATURE_FTM_BT

typedef unsigned long DWORD;
typedef unsigned long* PDWORD;
typedef unsigned long* LPDWORD;
typedef unsigned short USHORT;
typedef unsigned char UCHAR;
typedef unsigned char BYTE;
typedef unsigned long HANDLE;
typedef unsigned char BOOL;
typedef unsigned char BOOLEAN;
typedef void VOID;
typedef void* LPCVOID;
typedef void* LPVOID;
typedef void* LPOVERLAPPED;
typedef unsigned char* PUCHAR;
typedef unsigned char* PBYTE;
typedef unsigned char* LPBYTE;

#define TRUE           1
#define FALSE          0


#define LOG_TAG         "FT_BT "
#include <cutils/log.h>

#define BT_FM_DEBUG     1
#define ERR(f, ...)     LOGE("%s: " f, __func__, ##__VA_ARGS__)
#define WAN(f, ...)     LOGW("%s: " f, __func__, ##__VA_ARGS__)
#if BT_FM_DEBUG
#define DBG(f, ...)     LOGD("%s: " f, __func__, ##__VA_ARGS__)
#define TRC(f)          LOGW("%s #%d", __func__, __LINE__)
#else
#define DBG(...)        ((void)0)
#define TRC(f)          ((void)0)
#endif

#ifndef BT_DRV_MOD_NAME
#define BT_DRV_MOD_NAME     "bluetooth"
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


typedef struct {
	unsigned char    event;
	unsigned short	 handle;
	unsigned char    len;
	unsigned char    status;
	unsigned char    parms[256];
} BT_HCI_EVENT;

typedef struct {
	unsigned short   opcode;
	unsigned char    len;
	unsigned char    cmd[256];
} BT_HCI_CMD;

/* Used to store inquiry result */
typedef struct _BT_Info{
  struct _BT_Info *pNext;
  UCHAR  btaddr[6];
  UCHAR  psr;
  UCHAR  cod[3];
  UCHAR  clkoffset[2];
  int    rssi;
  UCHAR  name[248];
} BT_Info;

//===============        Global Variables         =======================

BT_Info     *g_pBtListHear = NULL;
static BOOL  g_scan_complete = FALSE;
static BOOL  g_inquiry_complete = FALSE;

static int   bt_fd = -1;
static int   bt_rfkill_id = -1;
static char *bt_rfkill_state_path = NULL;

/* Used to read serial port */
static pthread_t rxThread;
static BOOL bKillThread = FALSE;


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


/**************************************************************************
  *                         F U N C T I O N S                             *
***************************************************************************/

static BOOL BT_Send_HciCmd(BT_HCI_CMD *hci_cmd);
static BOOL BT_Recv_HciEvent(BT_HCI_EVENT *hci_event);
static BOOL BT_SetInquiryMode(UCHAR ucInquiryMode);
static BOOL BT_SetRemoteNameReq(BT_Info *pTmpBtInfo);
static BOOL BT_Inquiry(void);

static void* BT_FM_Thread(void* pContext);


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
    const char buf = (on ? '1' : '0');
    
    TRC();
    
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

out:
    if (fd >= 0) close(fd);
    return ret;
}
#endif

#ifdef RDA_BT_SUPPORT
/* Initialize UART driver */
static int init_uart(char *dev)
{
    struct termios ti;
    struct serial_struct ss;
    int fd, i;
    
    TRC();
    
    fd = open(dev, O_RDWR | O_NOCTTY);
    if (fd < 0) {
        perror("Can't open serial port");
        return -1;
    }
#ifndef MTK_MT6620
    tcflush(fd, TCIOFLUSH);

    if (tcgetattr(fd, &ti) < 0) {
        ERR("Can't get port settings");
        return -1;
    }

    cfmakeraw(&ti);
    
    ti.c_cflag |= CLOCAL;
    ti.c_lflag = 0;
    ti.c_cc[VTIME]    = 5; /* 0.5 sec */
    ti.c_cc[VMIN]     = 0;
    
    ti.c_cflag &= ~CRTSCTS;
    ti.c_iflag &= ~(IXON | IXOFF | IXANY | 0x80000000);
    
    if (tcsetattr(fd, TCSANOW, &ti) < 0) {
        ERR("Can't set port settings");
        return -1;
    }
    
    /* Clear the cust flag */
    if((ioctl(fd, TIOCGSERIAL, &ss)) < 0){
        ERR("BAUD: error to get serial_struct info: %s\n", strerror(errno));
        return -1;
    }

    if (ss.flags & ASYNC_SPD_CUST) {
        DBG("clear ASYNC_SPD_CUST\n");
        ss.flags &= ~ASYNC_SPD_CUST;
        
        if((ioctl(fd, TIOCSSERIAL, &ss)) < 0){
           ERR("BAUD: error to set serial_struct: %s\n", strerror(errno));
           return -1;
        }
    }
    
    /* Set initial baudrate */
    cfsetospeed(&ti, B115200);
    cfsetispeed(&ti, B115200);
    
    if (tcsetattr(fd, TCSANOW, &ti) < 0) {
        ERR("Can't set port settings");
        return -1;
    }

    tcflush(fd, TCIOFLUSH);
#endif
    return fd;
}


void rdabt_write_memory(int fd,__u32 addr,__u32 *data,__u8 len,__u8 memory_type)
{
   __u16 num_to_send; 
   __u16 i,j;
   __u8 data_to_send[256]={0};
   __u32 address_convert;
   
   data_to_send[0] = 0x01;
   data_to_send[1] = 0x02;
   data_to_send[2] = 0xfd;
   data_to_send[3] = (__u8)(len*4+6);
   data_to_send[4] = (memory_type+0x80);  // add the event display 0x00 no event callback 0x80
   data_to_send[5] = len;
   if(memory_type == 0x01)
   {
      address_convert = addr*4+0x200;
      data_to_send[6] = (__u8)address_convert;
      data_to_send[7] = (__u8)(address_convert>>8);
      data_to_send[8] = (__u8)(address_convert>>16);
      data_to_send[9] = (__u8)(address_convert>>24);   
   }
   else
   {
      data_to_send[6] = (__u8)addr;
      data_to_send[7] = (__u8)(addr>>8);
      data_to_send[8] = (__u8)(addr>>16);
      data_to_send[9] = (__u8)(addr>>24);
   }
   
   for(i=0;i<len;i++,data++)
   {
       j=10+i*4;
       data_to_send[j] =  (__u8)(*data);
       data_to_send[j+1] = (__u8)((*data)>>8);
       data_to_send[j+2] = (__u8)((*data)>>16);
       data_to_send[j+3] = (__u8)((*data)>>24);
   }
   
   num_to_send = 4+data_to_send[3]; 
   write(fd,&(data_to_send[0]),num_to_send);
}

void RDA_uart_write_simple(int fd,__u8* buf,__u16 len)
{
    __u16 num_send; 
    write(fd,buf,len);
    usleep(10000);//10ms?
}

void RDA_uart_write_array(int fd,__u32 buf[][2],__u16 len,__u8 type)
{
    __u32 i;
    for(i=0;i<len;i++)
    {
      rdabt_write_memory(fd,buf[i][0],&buf[i][1],1,type);
      usleep(12000);//12ms?
    } 
}

__u32 RDA5876_PSKEY_RF[][2] =
{
    //{0x40240000,0x0004f39c}, //; houzhen 2010.02.07 for rda5990 bt
    {0x800000C0,0x00000021}, //; CHIP_PS PSKEY: Total number -----------------
    {0x800000C4,0x003F0000},
    {0x800000C8,0x00414003},
    {0x800000CC,0x004225BD},
    {0x800000D0,0x004908E4},
    {0x800000D4,0x0043B074},
    {0x800000D8,0x0044D01A},
    {0x800000DC,0x004A0600},
    {0x800000E0,0x0054A020},
    {0x800000E4,0x0055A020},
    {0x800000E8,0x0056A542},
    {0x800000EC,0x00574C18},
    {0x800000F0,0x003F0001},
    {0x800000F4,0x00410900},
    {0x800000F8,0x0046033F},
    {0x800000FC,0x004C0000},
    {0x80000100,0x004D0015},
    {0x80000104,0x004E002B},
    {0x80000108,0x004F0042},
    {0x8000010C,0x0050005A},
    {0x80000110,0x00510073},
    {0x80000114,0x0052008D},
    {0x80000118,0x005300A7},
    {0x8000011C,0x005400C4},
    {0x80000120,0x005500E3},
    {0x80000124,0x00560103},
    {0x80000128,0x00570127},
    {0x8000012C,0x0058014E},
    {0x80000130,0x00590178},
    {0x80000134,0x005A01A1},
    {0x80000138,0x005B01CE},
    {0x8000013C,0x005C01FF},
    {0x80000140,0x003F0000},
    {0x80000144,0x00000000}, //;         PSKEY: Page 0
    {0x80000040,0x10000000},
    //{0x40240000,0x0000f29c}, //; SPI2_CLK_EN PCLK_SPI2_EN 

};
 
__u32 RDA5876_PSKEY_MISC[][2] =
{
 // open sleep
 //   {0x40240000,0x2000f29c},
    {0x80000070,0x00026000},  //fix esco parameter
    {0x80000074,0x05025010},  //0xa5025010 >>0x05025010 no sleep
    {0x80000078,0x0f054001}, //sniff interval 2 0
    {0x800000a4,0x00000000}, //no host wake
    {0x80000040,0x02007000},

  //  {0x80002bec,0x00010a02},  //sleep enable
	   
    {0x40180004,0x0001a218},
    {0x40180024,0x0001a1e0},

    {0x40180008,0x0001a234},
    {0x40180028,0x00000014},
  

//{0x40180000,0x00000003}, //add by gongyu
       /*houzhen update  Mar 22 2012 */
    {0x800004f4,0x83711b98}, ///rda5990 disable 3m esco ev4 ev5  ssp
    {0x800004f0,0xf88dffff}, ///rda5990 disable edr 
    {0x40200050,0x2eb20000},  //rda5990 pta config
    {0x40200054,0xffffffff},  //rda5990 pta config
    {0x40200058,0x02D00210},  //rda5990 delay config  	//houzhen add 2012 04 013
    {0x40240000,0x0000f29c},  //config 32k
    {0x80000000,0xea00003e},//
		{0x80000100,0xe3a00020},//   mov r0,0x10 	 //  wifi frame=last 8 bits  houzhen Mar 29 2012 0x80
    {0x80000104,0xe5c50020},//   strb r0,[r5,0x20]
    {0x80000108,0xe3a00000},// mov r0  0
    {0x8000010c,0xe3a01a31},//       mov  r1,0x31000
    {0x80000110,0xe281fe29},//add pc,r1,0x290
    {0x4018000c,0x0003128c},//
    {0x4018002c,0x00032cb4},//
    {0x80000120,0x13a0f112},//
    {0x80000004,0xea000046},//
    {0x80000124,0xe5c5000c},// STRB   r0,[r5,#0xc]
    {0x80000128,0xe3a00020},//     mov r0,0x06			   //bt frame=last 8 bits     houzhen Mar 29 2012 0x30
    {0x80000130,0xe3a00a31},// mov r0,0x31000
    {0x80000134,0xe280ffad},// add pc r0,0x2b4
    {0x40180010,0x000312b0},//
    {0x40180030,0x80000120},//
    {0x40180014,0x00031234},//
    {0x40180034,0x00008bac},//
    {0x80000140,0x13a0400f},// 
    {0x40180018,0x0000bab0},//
    {0x40180038,0x80000140},//
    {0x40180000,0x0000003f},//
};

__u32 RDA5876_SWITCH_BAUDRATE[][2] =
{
//3200000
 //    {0x80000060,0x0030d400},
//3000000
//        {0x80000060,0x002dc6c0},
//1500000
//     {0x80000060,0x0016e360},
//1152000
//        {0x80000060,0x00119400},
//baud rate 921600
//     {0x80000060,0x000e1000},
//     {0x80000064,0x000e1000},
//        {0x80000040,0x00000100}
};
 
__u8 RDA_AUTOACCEPT_CONNECT[] = 
{
    0x01,0x05, 0x0c, 0x03, 0x02, 0x00, 0x02
};

void RDA5876_Pskey_RfInit(int fd)
{
    RDA_uart_write_array(fd,RDA5876_PSKEY_RF,sizeof(RDA5876_PSKEY_RF)/sizeof(RDA5876_PSKEY_RF[0]),0);
}
 
void RDA5876_Pskey_Misc(int fd)
{
    RDA_uart_write_array(fd,RDA5876_PSKEY_MISC,sizeof(RDA5876_PSKEY_MISC)/sizeof(RDA5876_PSKEY_MISC[0]),0);
    usleep(50000);
    //RDA_uart_write_array(fd, RDA5876_SWITCH_BAUDRATE ,sizeof(RDA5876_SWITCH_BAUDRATE)/sizeof(RDA5876_SWITCH_BAUDRATE[0]),0);
}
 
#define RDA_BT_IOCTL_MAGIC 'u'

#define RDA_BT_POWER_ON_IOCTL _IO(RDA_BT_IOCTL_MAGIC ,0x01)
#define RD_BT_RF_INIT_IOCTL   _IO(RDA_BT_IOCTL_MAGIC ,0x02)
#define RD_BT_DC_CAL_IOCTL    _IO(RDA_BT_IOCTL_MAGIC ,0x03)
#define RD_BT_SET_RF_SWITCH_IOCTL _IO(RDA_BT_IOCTL_MAGIC ,0x04)
#define RDA_BT_POWER_OFF_IOCTL _IO(RDA_BT_IOCTL_MAGIC ,0x05)
#define RDA_BT_EN_CLK _IO(RDA_BT_IOCTL_MAGIC ,0x06)
#define RD_BT_DC_DIG_RESET_IOCTL    _IO(RDA_BT_IOCTL_MAGIC ,0x07)

#define RDABT_DRV_NAME "/dev/rdacombo"

int rdabt_send_cmd_to_drv(int cmd, unsigned char shutdown) 
{
	static int fd = -1;
	
	if(fd <  0)
	    fd = open(RDABT_DRV_NAME, O_RDWR);
		
	if (fd < 0) {
		perror("Can't open rdabt device");
		return -1;
	}
	
	if(ioctl(fd, cmd) == -1)
	{
		perror("rdabt_send_cmd_to_drv failed \n");
	}
		
	if(shutdown)
	{
		close(fd);
		fd = -1;
	}
	
	return 0;
}

static int rdabt_poweron_init(int fd, struct uart_t *u, struct termios *ti)
{
    rdabt_send_cmd_to_drv(RDA_BT_POWER_OFF_IOCTL, 0);
    rdabt_send_cmd_to_drv(RDA_BT_POWER_ON_IOCTL, 0);   	//power on
    rdabt_send_cmd_to_drv(RDA_BT_EN_CLK, 0); 
    printf("bf RD_BT_RF_INIT_IOCTL \n");
    usleep(200000); 
    rdabt_send_cmd_to_drv(RD_BT_DC_DIG_RESET_IOCTL, 0);    //houzhen add to ensure bt powe up safely
    usleep(200000); 
    rdabt_send_cmd_to_drv(RD_BT_RF_INIT_IOCTL, 0);
    rdabt_send_cmd_to_drv(RD_BT_SET_RF_SWITCH_IOCTL, 0);

    /*houzhen update 2012 03 06*/
    RDA5876_Pskey_RfInit(fd);  
    usleep(100000); 
    rdabt_send_cmd_to_drv(RD_BT_DC_CAL_IOCTL, 1);
    usleep(10000);                                     
    RDA5876_Pskey_Misc(fd);
    usleep(200000);
 
    return 0;
}

#endif
BOOL FM_BT_init(void)
{
#ifndef RDA_BT_SUPPORT
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

    /* Enable inquiry result with RSSI */
    if(!BT_SetInquiryMode(0x01)){
        ERR("Can't set BT inquiry mode\n");
        goto error;
    }
    
    /* Create RX thread */
    g_scan_complete = FALSE;
    g_inquiry_complete = FALSE;
    bKillThread = FALSE;
    pthread_create(&rxThread, NULL, BT_FM_Thread, (void*)NULL);
    
    sched_yield();
    return TRUE;
    
error:
    if (bt_fd >= 0){
        bt_restore(bt_fd);
        bt_fd = -1;
    }
    
    if (glib_handle){
        dlclose(glib_handle);
        glib_handle = NULL;
    }

#ifndef MTK_COMBO_SUPPORT
    bt_set_power(0);
#endif

    return FALSE;
#else
   /* Create RX thread */
   g_scan_complete = FALSE;/* clear */
   g_inquiry_complete = FALSE;
   bKillThread = FALSE;
   bt_fd = init_uart(CUST_BT_SERIAL_PORT);
   if (bt_fd < 0){
        ERR("unable to initilize UART %s", CUST_BT_SERIAL_PORT);
        /* error handling */
        goto error;
    }
   DBG("initilize UART %s", CUST_BT_SERIAL_PORT);
   rdabt_poweron_init(bt_fd, NULL, NULL);

   return TRUE;

error:
    
    if (bt_fd > 0){
        close(bt_fd);
        bt_fd = -1;
    }
    return FALSE;   
#endif
}

void FM_BT_deinit(void)
{
    BT_Info *pBtInfo = NULL;
    
    TRC();
 #ifndef RDA_BT_SUPPORT  
    
    /* Stop RX thread */
    bKillThread = TRUE;
    
    /* Wait until thread exist */
    pthread_join(rxThread, NULL);
    
    
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
#endif

#ifdef RDA_BT_SUPPORT
     rdabt_send_cmd_to_drv(RDA_BT_POWER_OFF_IOCTL, 1);
#endif

    /* Clear globals */
    while(g_pBtListHear){
        pBtInfo = g_pBtListHear;
        g_pBtListHear = g_pBtListHear->pNext;
        free(pBtInfo);
    }
    
    g_inquiry_complete = FALSE;
    g_scan_complete = FALSE;
    
    return;
}

static BOOL BT_Send_HciCmd(BT_HCI_CMD *hci_cmd)
{
    UCHAR   HCI_CMD[256+4];
    
    if (!glib_handle || !bt_send_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    HCI_CMD[0] = 0x01;
    HCI_CMD[1] = (hci_cmd->opcode)&0xff;
    HCI_CMD[2] = (hci_cmd->opcode>>8)&0xff;
    HCI_CMD[3] = hci_cmd->len;
    
    DBG("OpCode %x len %d\n", hci_cmd->opcode, (int)hci_cmd->len);
    
    memcpy(&HCI_CMD[4], hci_cmd->cmd, hci_cmd->len);
    
    if(bt_send_data(bt_fd, HCI_CMD, hci_cmd->len + 4) < 0){
        ERR("Write HCI command fails errno %d\r\n", errno);
        return FALSE;
    }
    
    return TRUE;
}

static BOOL BT_Recv_HciEvent(BT_HCI_EVENT *hci_event)
{    
    hci_event->status = FALSE;
    
    if (!glib_handle || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    if(bt_receive_data(bt_fd, &hci_event->event, 1) < 0){
        ERR("Read event code fails errno %d", errno);
        return FALSE;
    }
    
    DBG("Read event code: 0x%x\n", hci_event->event);
    
    if(bt_receive_data(bt_fd, &hci_event->len, 1) < 0){
        ERR("Read event length fails errno %d", errno);
        return FALSE;
    }
    
    DBG("Read event length: 0x%x\n", hci_event->len);
    
    if(hci_event->len){
        if(bt_receive_data(bt_fd, hci_event->parms, hci_event->len) < 0){
            ERR("Read event param fails errno %d", errno);
            return FALSE;
        }
    }
    
    hci_event->status = TRUE;
    return TRUE;
}

static BOOL BT_SetInquiryMode(UCHAR ucInquiryMode)
{
    UCHAR   HCI_INQUIRY_MODE[] = {0x01, 0x45, 0x0C, 0x01, 0x00};
    UCHAR   pAckEvent[7];
    UCHAR   ucEvent[] = {0x04, 0x0E, 0x04, 0x01, 0x45, 0x0C, 0x00};
    
    HCI_INQUIRY_MODE[4] = ucInquiryMode;
    
    TRC();
    
    if (!glib_handle || !bt_send_data || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    if(bt_send_data(bt_fd, HCI_INQUIRY_MODE, sizeof(HCI_INQUIRY_MODE)) < 0){
        ERR("Send inquiry mode command fails errno %d\n", errno);
        return FALSE;
    }
    
    if(bt_receive_data(bt_fd, pAckEvent, sizeof(pAckEvent)) < 0){
        ERR("Receive event fails errno %d\r\n", errno);
        return FALSE;
    }
    
    if(memcmp(pAckEvent, ucEvent, sizeof(ucEvent))){
        ERR("Receive unexpected event\n");
        return FALSE;
    }
    
    return TRUE;
}

static BOOL BT_SetRemoteNameReq(BT_Info *pTmpBtInfo)
{
    UCHAR   ucHeader = 0;
    int     count = 0;
    BOOL    RetVal = FALSE;
    
    BT_HCI_EVENT hci_event;
    UCHAR HCI_REMOTE_NAME_REQ[] = {0x01, 0x19, 0x04, 0x0A, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x01, 0x00, 0xCC, 0xCC};
    
    HCI_REMOTE_NAME_REQ[4]  = pTmpBtInfo->btaddr[5];
    HCI_REMOTE_NAME_REQ[5]  = pTmpBtInfo->btaddr[4];
    HCI_REMOTE_NAME_REQ[6]  = pTmpBtInfo->btaddr[3];
    HCI_REMOTE_NAME_REQ[7]  = pTmpBtInfo->btaddr[2];
    HCI_REMOTE_NAME_REQ[8]  = pTmpBtInfo->btaddr[1];
    HCI_REMOTE_NAME_REQ[9]  = pTmpBtInfo->btaddr[0];
    HCI_REMOTE_NAME_REQ[10] = pTmpBtInfo->psr;
    HCI_REMOTE_NAME_REQ[12] = pTmpBtInfo->clkoffset[1];
    HCI_REMOTE_NAME_REQ[13] = pTmpBtInfo->clkoffset[0];
    
    TRC();
    
    if (!glib_handle || !bt_send_data || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    if(bt_send_data(bt_fd, HCI_REMOTE_NAME_REQ, sizeof(HCI_REMOTE_NAME_REQ)) < 0){
        ERR("Send remote name req command fails errno %d\n", errno);
        return FALSE;
    }
    
    while(!bKillThread){
        
        if(bt_receive_data(bt_fd, &ucHeader, sizeof(ucHeader)) < 0){
            count ++;
            if(count < 5)
                continue;
            else
                break;
        }
        
        /* not event */
        if(ucHeader != 0x04){
            ERR("Unexpected read header 0x%02x\n", ucHeader);
            return FALSE;
        }
        
        if(BT_Recv_HciEvent(&hci_event))
        {
            /* Wait for remote name request complete event */
            if(hci_event.event == 0x07){
                /* success */
                if(hci_event.parms[0] == 0){
                    /* FIX ME need convert UTF8 -> ASCII */
                    memcpy(pTmpBtInfo->name, &hci_event.parms[7], 248);
                    //pTmpBtInfo->name[hci_event.len] = '\0';
                    DBG("remote name %s\n", pTmpBtInfo->name);
                    RetVal = TRUE;
                    break;
                }
                else{ /* faliure such as page time out */
                    DBG("Unexpected result event %02x status %02x\n", 
                        (int)hci_event.event, (int)hci_event.parms[0]);
                    /* FIX ME need convert UTF8 -> ASCII */            
                    pTmpBtInfo->name[0] = 'U';
                    pTmpBtInfo->name[1] = 'N';
                    pTmpBtInfo->name[2] = 'K';
                    pTmpBtInfo->name[3] = 'N';
                    pTmpBtInfo->name[4] = 'O';
                    pTmpBtInfo->name[5] = 'W';
                    pTmpBtInfo->name[6] = 'N';
                    pTmpBtInfo->name[7] = '\0';
                    RetVal = TRUE;
                    break;
                }
            }
        }
        else{
            ERR("Receive event fails errno %d\r\n", errno);
            return FALSE;
        }
    }
    
    return RetVal;
}

static BOOL BT_Inquiry(void)
{
    UCHAR   HCI_INQUIRY[] = {0x01, 0x01, 0x04, 0x05, 0x33, 0x8B, 0x9E, 0x05, 0x0A};
    UCHAR   pAckEvent[7];
    // expected to receive status event
    UCHAR   ucEvent[] = {0x04, 0x0F, 0x04, 0x00, 0x01, 0x01, 0x04};
    
    TRC();
    
    if (!glib_handle || !bt_send_data || !bt_receive_data){
        ERR("mtk bt library is unloaded!\n");
        return FALSE;
    }
    if (bt_fd < 0){
        ERR("bt driver fd is invalid!\n");
        return FALSE;
    }
    
    if(bt_send_data(bt_fd, HCI_INQUIRY, sizeof(HCI_INQUIRY)) < 0){
        ERR("Send inquiry command fails errno %d\n", errno);
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

#ifdef RDA_BT_SUPPORT
#define HCI_COMMAND_PKT		0x01
#define HCI_ACLDATA_PKT		0x02
#define HCI_SCODATA_PKT		0x03
#define HCI_EVENT_PKT		0x04
#define HCI_VENDOR_PKT		0xff
#define HCI_EV_INQUIRY_RESULT		0x02
#define HCI_EV_INQUIRY_RESULT_WITH_RSSI 0x22
#define HCI_EV_INQUIRY_COMPLETE     0x01
/* 
 * Read an HCI event from the given file descriptor.
 */
int read_hci_event(int fd, unsigned char* buf, int size) 
{
	int remain, r;
	int count = 0;

	if (size <= 0)
		return -1;

	/* The first byte identifies the packet type. For HCI event packets, it
	 * should be 0x04, so we read until we get to the 0x04. */
	while (1) {
		r = read(fd, buf, 1);
        
		if (r <= 0)
			continue;
        
		if (buf[0] == HCI_EVENT_PKT)
			break;
	}
	count++;

	/* The next two bytes are the event code and parameter total length. */
	while (count < 3) {
		r = read(fd, buf + count, 3 - count);
		if (r <= 0)
			continue;
		count += r;
	}

	/* Now we read the parameters. */
	if (buf[2] < (size - 3)) 
		remain = buf[2];
	else 
		remain = size - 3;

	while ((count - 3) < remain) {
		r = read(fd, buf + count, remain - (count - 3));
		if (r <= 0)
			continue;
		count += r;
	}

    DBG("read_hci_event: %x %x %x %x %x %x %x %x \n", buf[0],  buf[1], buf[2], buf[3], buf[4],
         buf[5], buf[6], buf[7]);
	return count;
}


#endif

BOOL FM_BT_inquiry()
{
#ifndef RDA_BT_SUPPORT
    return BT_Inquiry();
#else // rda
    BT_Info *pTmpBtInfo = NULL, *pTmpBtInfoForList = NULL;

    UCHAR   HCI_INQUERY_MODE[] = {0x01, 0x45, 0x0C, 0x01, 0x01}; 
    UCHAR   mode_ucEvent[] = {0x04, 0x0E, 0x04, 0x01, 0x45, 0x0C};

    UCHAR   HCI_INQUERY[] = {0x01, 0x01, 0x04, 0x05, 0x33, 0x8B, 0x9E, 0x04, 0x00};
    //CMD STATUS
    UCHAR   inquery_ucEvent[] = {0x04, 0x0F, 0x04, 0x00, 0x01, 0x01, 0x04};

    int len = 0;
    unsigned char results_num = 0;
    UCHAR   eventData[512], *peventData;

    peventData = eventData;
    DBG("FM_BT_inquiry start \n");

    if(bt_fd < 0)
        return FALSE;

    write(bt_fd,  HCI_INQUERY_MODE,  sizeof(HCI_INQUERY_MODE));
    len = read_hci_event(bt_fd, eventData, sizeof(mode_ucEvent));
     if(len < 0)
        goto error;
     
    if(memcmp(eventData, mode_ucEvent, sizeof(mode_ucEvent)))
        ERR("FM_BT_inquiry HCI_INQUERY_MODE Unexpected read event\r\n");
        
    write(bt_fd,  HCI_INQUERY,  sizeof(HCI_INQUERY));
    len = read_hci_event(bt_fd, eventData, sizeof(inquery_ucEvent));
     if(len < 0)
        goto error;
     
    if(memcmp(eventData,  inquery_ucEvent, sizeof(inquery_ucEvent)))
        ERR("FM_BT_inquiry HCI_INQUERY Unexpected read event\r\n");

    DBG("FM_BT_inquiry \n");

retry:    
    //waite inquiry result
     peventData = eventData;
     len = read_hci_event(bt_fd, eventData, sizeof(eventData));
     if(len < 0)
        goto error;
     
     if(eventData[1] == HCI_EV_INQUIRY_RESULT)
         {
            results_num = eventData[3];
            peventData += 3;

            while(results_num -- > 0)
            {
                pTmpBtInfo = (BT_Info *)malloc(sizeof(BT_Info));
                memset(pTmpBtInfo, 0x0, sizeof(BT_Info));
              
                /* update record */
                pTmpBtInfo->btaddr[0] = peventData[6];
                pTmpBtInfo->btaddr[1] = peventData[5];
                pTmpBtInfo->btaddr[2] = peventData[4];
                pTmpBtInfo->btaddr[3] = peventData[3];
                pTmpBtInfo->btaddr[4] = peventData[2];
                pTmpBtInfo->btaddr[5] = peventData[1];
                pTmpBtInfo->psr =  peventData[7];
                pTmpBtInfo->cod[0] = peventData[12];
                pTmpBtInfo->cod[1] = peventData[11];
                pTmpBtInfo->cod[2] = peventData[10];
                pTmpBtInfo->clkoffset[0] = peventData[14];
                pTmpBtInfo->clkoffset[1] = peventData[13];
                pTmpBtInfo->rssi = -85; //-120 - 20dbm

                /* insert into list */
                if(g_pBtListHear == NULL)
                {
                    g_pBtListHear = pTmpBtInfo;
                }
                else {
                    pTmpBtInfoForList = g_pBtListHear;
                    while((pTmpBtInfoForList != NULL) && (pTmpBtInfoForList->pNext != NULL))
                    {
                        pTmpBtInfoForList = pTmpBtInfoForList->pNext;
                    }
                    pTmpBtInfoForList->pNext = pTmpBtInfo;
                }
                peventData += 15;
            }
             g_inquiry_complete = TRUE;
        }
    else if(eventData[1] == HCI_EV_INQUIRY_RESULT_WITH_RSSI)
        {
            results_num = eventData[3];
            peventData += 3;

            while(results_num -- > 0)
            {
                pTmpBtInfo = (BT_Info *)malloc(sizeof(BT_Info));
                memset(pTmpBtInfo, 0x0, sizeof(BT_Info));

               
                /* update record */
                pTmpBtInfo->btaddr[0] = peventData[6];
                pTmpBtInfo->btaddr[1] = peventData[5];
                pTmpBtInfo->btaddr[2] = peventData[4];
                pTmpBtInfo->btaddr[3] = peventData[3];
                pTmpBtInfo->btaddr[4] = peventData[2];
                pTmpBtInfo->btaddr[5] = peventData[1];
                pTmpBtInfo->psr = peventData[7];
                pTmpBtInfo->cod[0] = peventData[11];
                pTmpBtInfo->cod[1] = peventData[10];
                pTmpBtInfo->cod[2] = peventData[9];
                pTmpBtInfo->clkoffset[0] = peventData[13];
                pTmpBtInfo->clkoffset[1] = peventData[12];
                pTmpBtInfo->rssi = peventData[14] >= 128? peventData[14] -256:peventData[14]; //-120 - 20dbm

                /* insert into list */
                if(g_pBtListHear == NULL)
                {
                    g_pBtListHear = pTmpBtInfo;
                }
                else {
                    pTmpBtInfoForList = g_pBtListHear;
                    while((pTmpBtInfoForList != NULL) && (pTmpBtInfoForList->pNext != NULL))
                    {
                        pTmpBtInfoForList = pTmpBtInfoForList->pNext;
                    }
                    pTmpBtInfoForList->pNext = pTmpBtInfo;
                }
                peventData += 15;
            }
            g_inquiry_complete = TRUE;
        }
        else if(eventData[1] == HCI_EV_INQUIRY_COMPLETE)
            {
                DBG("HCI_EV_INQUIRY_COMPLETE");
                g_scan_complete = TRUE;
            }

        if(!g_scan_complete)
            goto retry;
        
    g_inquiry_complete = TRUE;
    g_scan_complete = TRUE;

    return TRUE;
error:
    DBG("FM_BT_inquiry failed \n");
    return FALSE;
#endif
}

void updateTextInfo(BT_Info *pBtInfoForList, char* output_buf, int buf_len)
{
    char   cBuf[1024];
    static int loop = 0;
    BT_Info *pInfoList = NULL;
    
    memset(cBuf, 0, sizeof(cBuf));
    
    pInfoList = pBtInfoForList;
    
    if(pBtInfoForList == NULL){
        
        if(g_inquiry_complete == FALSE) {
            sprintf(cBuf, "Status: Start inquiring...\n");
        }else{
            /* Can not find any device */ 
            sprintf(cBuf, "----End of Device List  No dev found ----\n");
        }
    }
    else if(g_scan_complete == FALSE){
        
        if(g_inquiry_complete == FALSE){
            if(loop == 0 ){
                sprintf(cBuf, "Status: Inquiring ----- \n");
                loop = 1;
            }else{
                sprintf(cBuf, "Status: Inquiring +++++ \n");
                loop = 0;
            }
        }else{
            if(loop == 0 ){
                sprintf(cBuf, "Status: Scanning ----- \n");
                loop = 1;
            }else{
                sprintf(cBuf, "Status: Scanning +++++ \n");
                loop = 0;
            }
        }
    }
    else{
        sprintf(cBuf, "Status: Scanning Completed\n");
    }

    while(pBtInfoForList){
        if(strlen((const char*)pBtInfoForList->name)){
            int str_len = 0;
            str_len = strlen((const char*)pBtInfoForList->name);
            
            strncpy(cBuf  + strlen(cBuf), (const char*)pBtInfoForList->name, 12);
            if(str_len < 12){
                strncpy(cBuf  + strlen(cBuf), "            ", 12 - str_len);
            }
            sprintf(cBuf  + strlen(cBuf), " %d\n", pBtInfoForList->rssi);
        }else{
            /* Inquiry result */
            sprintf(cBuf  + strlen(cBuf), "%02x%02x%02x%02x%02x%02x %d\n",
            pBtInfoForList->btaddr[0], pBtInfoForList->btaddr[1], pBtInfoForList->btaddr[2],
            pBtInfoForList->btaddr[3], pBtInfoForList->btaddr[4], pBtInfoForList->btaddr[5],
            pBtInfoForList->rssi);
        }
        pBtInfoForList = pBtInfoForList->pNext;
    }
    
    memcpy(output_buf, cBuf, strlen(cBuf) + 1);
    
    if(pInfoList){
        if(g_scan_complete == FALSE){
            if(g_inquiry_complete == TRUE){
                sprintf(output_buf, "%s%s", output_buf, "----End of Device List----\n");
            }
        }
        else{
            sprintf(output_buf, "%s%s", output_buf, "----End of Scan List----\n");
        }
    }
    return;
}

static void *BT_FM_Thread( void *ptr )
{
    BT_HCI_EVENT hci_event;
    BOOL     RetVal = TRUE;
    UCHAR    ucHeader = 0;
    BT_Info *pBtInfo = NULL, *pBtInfoForList = NULL;
    int      rssi = 0;
    
    TRC();
    sleep(5);
    
    while(!bKillThread){
        
        if(g_scan_complete){
            DBG("Scan complete\r\n");
            pthread_exit(NULL);
            break;
        }
        
        if (!glib_handle || !bt_receive_data){
            ERR("mtk bt library is unloaded!\n");
            goto CleanUp;
        }
        if (bt_fd < 0){
            ERR("bt driver fd is invalid!\n");
            goto CleanUp;
        }
        
        if(bt_receive_data(bt_fd, &ucHeader, sizeof(ucHeader)) < 0){
            ERR("zero byte read\n");
            continue;
        }
        
        switch (ucHeader)
        {
            case 0x04:
                DBG("Receive HCI event\n");
                if(BT_Recv_HciEvent(&hci_event))
                {
                    if(hci_event.event == 0x02 ){
                        /* Inquiry result */
                        /* should not be received */
                        goto CleanUp;
                    }
                    else if(hci_event.event == 0x01 ){
                        /* Inquiry complete */
                        if(hci_event.len != 0x01){
                            ERR("Unexpected inquiry complete len %d", (int)hci_event.len);
                            goto CleanUp;
                        }
                        
                        if(hci_event.parms[0] != 0x00){
                            ERR("Unexpected inquiry complete status %d", (int)hci_event.parms[0]);
                            goto CleanUp;
                        }
                        
                        g_inquiry_complete = TRUE;
                        DBG("Inquiry complete\n");
                        
                        /* Request to get name */
                        pBtInfo = g_pBtListHear;
                        
                        DBG("Start remote name request\n");
                        while(pBtInfo && !bKillThread){
                            
                            BT_SetRemoteNameReq(pBtInfo);
                            pBtInfo = pBtInfo->pNext;
                        }
                        
                        g_scan_complete = TRUE;
                    }
                    else if(hci_event.event == 0x22 ){
                        pBtInfo = (BT_Info *)malloc(sizeof(BT_Info));
                        memset(pBtInfo, 0, sizeof(BT_Info));
                        
                        /* Inquiry result with RSSI */
                        if(hci_event.len != 0x0F){
                            ERR("Unexpected len %d", (int)hci_event.len);
                            goto CleanUp;
                        }
                        
                        /* negative 2's complement */
                        rssi = hci_event.parms[14];
                        if(rssi >= 128){
                            rssi -= 256;
                        }
                        
                        /* Update record */
                        pBtInfo->btaddr[0] = hci_event.parms[6];
                        pBtInfo->btaddr[1] = hci_event.parms[5];
                        pBtInfo->btaddr[2] = hci_event.parms[4];
                        pBtInfo->btaddr[3] = hci_event.parms[3];
                        pBtInfo->btaddr[4] = hci_event.parms[2];
                        pBtInfo->btaddr[5] = hci_event.parms[1];
                        pBtInfo->psr = hci_event.parms[7];
                        pBtInfo->cod[0] = hci_event.parms[11];
                        pBtInfo->cod[1] = hci_event.parms[10];
                        pBtInfo->cod[2] = hci_event.parms[9];
                        pBtInfo->clkoffset[0] = hci_event.parms[13];
                        pBtInfo->clkoffset[1] = hci_event.parms[12];
                        pBtInfo->rssi = rssi; //-120 - 20dbm
                        
                        /* Insert into list */
                        if (g_pBtListHear == NULL){
                            g_pBtListHear = pBtInfo;
                        }
                        else{
                            pBtInfoForList = g_pBtListHear;
                            while((pBtInfoForList != NULL) && (pBtInfoForList->pNext != NULL))
                            {
                                pBtInfoForList = pBtInfoForList->pNext;
                            }
                            pBtInfoForList->pNext = pBtInfo;
                        }
                    }
                    else{
                        /* simply ignore it? */
                        DBG("Unexpected event 0x%2x len %d %02x-%02x-%02x-%02x\n", 
                            (int)hci_event.event, (int)hci_event.len, 
                            (int)hci_event.parms[0], (int)hci_event.parms[1], 
                            (int)hci_event.parms[2], (int)hci_event.parms[3]);
                    }
                }
                else{
                    ERR("Read event fails errno %d\n", errno);
                    goto CleanUp;
                }
                break;
                
            default:
                ERR("Unexpected BT packet header %02x\n", ucHeader);
                goto CleanUp;
        }
    }

CleanUp:
    return 0;
}
#endif
