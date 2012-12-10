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

//*****************************************************************
//
//File provided by ccci_driver owner for AGPS commucation with modem task!
//
//*****************************************************************
#ifndef __CCCI_IPC_CLIENT_H__
#define __CCCI_IPC_CLIENT_H__

#ifdef __cplusplus
extern "C" {
#endif

#include  <sys/ioctl.h>
#include  <stdio.h>
#include  <linux/ccci_ipc_task_ID.h>
#include  "typedef.h"
#include  "mtk_service.h"

//Moved to "typedef.h"
#ifndef __CDMA_AGPS_SUPPORT__
#ifndef uint32
	typedef unsigned int uint32;
#endif

#ifndef uint16
	typedef unsigned short uint16;
#endif

#ifndef uint8
	typedef unsigned char uint8;
#endif
#endif

typedef enum
{
	IPC_MSG_ID_SYS_BEGIN = 0x80000000,
	IPC_MSG_ID_SYS_RANGE = 100
}IPC_MSG_ID_CODE_BEGIN;

typedef enum
{
	IPC_MSG_ID_INVALID_TYPE = 0x80000000,
   	 //------- Include  msg_id file------------------ 
   	 #include"mmi_ss_msg_id.h"
 	//---------------------------------------------
	IPC_MSG_ID_END
}mtk_agps_mmi_ss_msg_type;

#define CCCI_IPC_DEV_MAJOR  183
#define MAX_NUM_IPC_TASKS   10
#define CCCI_IPC_MAGIC 'P'
#define CCCI_IPC_RESET_RECV	_IO(CCCI_IPC_MAGIC,0)
#define CCCI_IPC_RESET_SEND	_IO(CCCI_IPC_MAGIC,1)
#define CCCI_IPC_WAIT_MD_READY	_IO(CCCI_IPC_MAGIC,2)

#define AGPS_CCCI_IPC_RESET_RECV(fd)   ioctl(fd,CCCI_IPC_RESET_RECV)  //Clear driver buffer before to receive msg
#define AGPS_CCCI_IPC_RESET_SEND(fd)   ioctl(fd,CCCI_IPC_RESET_SEND)  //reset driver state to re-send msg if error happened .
#define AGPS_CCCI_IPC_WAIT_MD_READY(fd)   ioctl(fd,CCCI_IPC_WAIT_MD_READY)

 
/*typedef struct {
	uint8  ref_count;
	uint16 msg_len;
	uint8 data[0];
} local_para_struct ;

typedef struct {
   uint16	pdu_len; 
   uint8	ref_count; 
   uint8   	pb_resvered; 
   uint16	free_header_space; 
   uint16	free_tail_space;
   uint8	data[0];
}peer_buff_struct ;

typedef struct ipc_ilm_struct 
{
    uint32           src_mod_id;
    uint32           dest_mod_id;
    uint32           sap_id;
    uint32           msg_id;
    local_para_struct    *local_para_ptr;
    peer_buff_struct     *peer_buff_ptr;
}ipc_ilm_t;
*/
extern int cm_agps_ccci_open(int TASK_ID, int non_block);
extern int cm_agps_ccci_close(int TASK_ID, int fd);
extern int cm_agps_ccci_read(int fd, void *buff, int buff_size);
extern int cm_agps_ccci_write(int fd, mtk_ilm_struct *ilm);

extern int cm_agps_uart_init(int non_block);
extern int cm_agps_uart_close(int fd);
extern int cm_agps_uart_read(int fd, void *buff, int buff_size);
extern int cm_agps_uart_write(int fd, void *buff, int buff_size);

#ifdef __cpluscplus
}
#endif

#endif  /* !__CCCI_IPC_CLIENT_H__ */







