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

#ifndef __MTK_VT_DEFS_H__
#define __MTK_VT_DEFS_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <errno.h>
#include <fcntl.h> // For 'O_RDWR' & 'O_EXCL'
#include <android/log.h>
#include <semaphore.h>
#include <pthread.h>

#include "vt_kal_def.h"
#include "vt_swip_struct.h"
#include "vt_option_cfg.h"

#define VT_OS_TYPE_LINUX

//#define TCV_STAN_ALONE
//#define TCV_STAN_ALONE_2

//#define VTS_TEST 1
//#define DUMP_TO_FILE_DL
//#define DUMP_TO_FILE_UL

#include "mtk_vt_log.h"

#define DBG_AGPS_SUPL "/data/vt/dbg_log"

#define PTTY_MUX "/dev/radio/pttyvt" 

#define IN
#define OUT
#define INOUT

//////////////////////////////////////////////////////////////////////
// Debug Macros 
//////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////
#define VT_YES               0
#define VT_ALWAYS_ASK		 1
#define VT_NO				 2

#define VT_HIDEME_FREEZE_ME	 2
#define VT_HIDEME_YES		 1
#define VT_HIDEME_NO		 0

#define VT_ZERO				 0
#define VT_FALSE             0
#define VT_TRUE              1
#define VT_INVALID_FD        0xFFFFFFFF

//#define VT_TCV_PKT_SZ        80
#define VT_TCV_PKT_SZ        160
#define VT_TCV_PKT_SZx2     (VT_TCV_PKT_SZ<<1)
#define VT_TCV_PKT_SZx4     (VT_TCV_PKT_SZx2<<1)

//#define VT_TCV_TX_PERIOD     10   //ms
#define VT_TCV_TX_PERIOD     19   //ms


#define VT_RBUF_LEN          VT_TCV_PKT_SZx2
#define VT_WBUF_LEN          VT_TCV_PKT_SZx2

#define VT_MAX_RETRY_CNT     10
#define VT_MAX_POLL_CNT      10
#define VT_DBG_BUFF_SZ       32
#define VT_DBG_COLUMN_CNT    16
#define VT_DBG_ROUND_POS(i) (i*VT_DBG_COLUMN_CNT)

//Jeffery
//#define CSR_TX_BUFFER_SIZE   ( 641) //(VT_TRANSPORT_SIZE * 20 + 1)
#define CSR_TX_BUFFER_SIZE   (2401) //(VT_TRANSPORT_SIZE * 10 + 1)
#define CSR_RX_BUFFER_SIZE   (2401) //(VT_TRANSPORT_SIZE * 10 + 1)

#define CSR_TX_BUF_THRESHOLD (CSR_TX_BUFFER_SIZE>>1)
#define CSR_RX_BUF_THRESHOLD (CSR_RX_BUFFER_SIZE>>1)


#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif
#define VTS_SET_NULL(v)         ((v)=0)
#define VTS_SET_TRUE(v)         ((v)=TRUE)
#define VTS_SET_FALSE(v)        ((v)=FALSE)
#define VTS_SET_MAXWORD(v)      ((v)=0xffff)
#define VTS_SET_INVALID(v)      ((v)=0xffffffff)
#define VTS_SET_DVALUE(v,d)     ((v)=(d))
#define VTS_IS_TRUE(v)          ((v == TRUE)?TRUE:FALSE)


typedef enum {
  VT_RET_FAIL = -1,
  VT_RET_TRUE =  0,
  VT_RET_SOCKET_INIT_FAIL,
  VT_RET_CNT
} vt_ret_enum;

typedef enum {
  MOD_NIL,
  MOD_TCV,
  MOD_STK,
  MOD_SVC,
  MOD_CNT
} vt_module_enum;

typedef enum {
  VT_TASK_TCV = 0,
  VT_TASK_TCV_RX,
  VT_TASK_TCV_TX,
  VT_TASK_STK,
  VT_TASK_DLAP,
  VT_TASK_DLVP,
  VT_TASK_ULAP,
  VT_TASK_ULVP,
  VT_TASK_SVC,
  VT_TASK_CNT
} vt_task_enum;


typedef enum {
  VT_TIMER_TCV = 0,
  VT_TIMER_CNT
} vt_timer_enum;

typedef enum {
  MSG_ID_VT_TCV_FD = 0,
  MSG_ID_VT_TCV_START,
  MSG_ID_VT_TCV_STOP,
  MSG_ID_VT_TCV_EXIT,
  MSG_ID_VT_TCV_CNT
} vt_msg_id_enum;

typedef enum {
  MUX_FD_ADD,
  MUX_FD_REMOVE,
  MUX_FD_EXIT,
  MUX_FD_CNT
} vt_tcvr_cmd_enum;

typedef struct {
    int cmd;
    int fd;
} vt_tcv_cmd_struct;

//typedef enum 
//{
//	KAL_FALSE,
//	KAL_TRUE
//} kal_bool;
//
//typedef  signed char    kal_int8;
//typedef  signed short   kal_int16;   
//typedef  signed int     kal_int32;
//typedef  char           kal_char;
//
//typedef  unsigned char  kal_uint8;
//typedef  unsigned short kal_uint16;
//typedef  unsigned int   kal_uint32;
//typedef unsigned int    module_type;



//typedef unsigned int    sap_type;
//typedef unsigned int    msg_type;
//typedef struct
//{
//	module_type            src_mod_id;
//	module_type            dest_mod_id;
//	sap_type               sap_id;
//	msg_type               msg_id;
//	mtk_local_para_struct *local_para_ptr;
//	mtk_peer_buff_struct  *peer_buff_ptr;
//} mtk_ilm_struct;

typedef ilm_struct  mtk_ilm_struct;
typedef mtk_ilm_struct *pmtk_ilm_struct;

#define MTK_LOCAL_PARA_HDR         \
   kal_uint8    ref_count;         \
   kal_uint16   msg_len;

#define MTK_PEER_BUFF_HDR          \
   kal_uint16   pdu_len;           \
   kal_uint8    ref_count;         \
   kal_uint8    pb_resvered;       \
   kal_uint16   free_header_space; \
   kal_uint16   free_tail_space;

typedef struct mtk_local_para_struct {
    MTK_LOCAL_PARA_HDR
} mtk_local_para_struct;

typedef struct mtk_peer_buff_struct {
    MTK_PEER_BUFF_HDR
} mtk_peer_buff_struct;

typedef enum VTSMSGINTERID{
	//msg id for stack
	//VTMSG_ID_STK_INIT = 0,
	//VTMSG_ID_STK_DEINIT,
	//VTMSG_ID_STK_ACT_REQ,
	//VTMSG_ID_STK_DEACT_REQ,
	//VTMSG_ID_STK_USER_INPUT,
	//VTMSG_ID_STK_LB_ACT,
	//VTMSG_ID_STK_LB_DEACT,
	//VTMSG_ID_STK_SET_PEER_QUAL,
	//VTMSG_ID_STK_AUD_PUT_TX,
	//VTMSG_ID_STK_VID_PUT_TX,
	//VTMSG_ID_STK_REQ_I_FRAME,
	VTMSG_ID_STK_HANDLE_BY_SVC_BEGIN = 0,
	VTMSG_ID_STK_GET_DEC_CONF,
	VTMSG_ID_STK_ACT_CNF,
	VTMSG_ID_STK_DEACT_CNF,
	VTMSG_ID_STK_CALL_DISC_IND,
	VTMSG_ID_STK_CHAN_CONF,
	VTMSG_ID_STK_AUD_PUT_RX,		
	VTMSG_ID_STK_SET_MAX_SKEW,
	VTMSG_ID_STK_VID_PUT_RX,
	VTMSG_ID_STK_SET_LOCAL_QUAL,
	VTMSG_ID_STK_SET_RES,
	VTMSG_ID_STK_ENC_I_FRAME,
	//[MTK81058/20110616] Added for CMCC
	VTMSG_ID_STK_CHANNEL_ACTIVE,
	VTMSG_ID_STK_CHANNEL_INACTIVE,
	VTMSG_ID_STK_START_COUNTER,	
	VTMSG_ID_STK_INCOMING_VIDEO_CHANNEL_CONNECTED,
	VTMSG_ID_STK_HANDLE_BY_SVC_END,
	//msg id for tranceiver
	//msg id for MAL
	VTMSG_ID_MAL_FOR_PEER_I_FRAME,
	//msg id for threads
	VTMSG_ID_DATA_NOTIFY,
	VTMSG_ID_PROCESS_RATE,
	VTMSG_ID_THREAD_SUSPEND,
	VTMSG_ID_THREAD_RUN,
	//for manager msg
	VTMSG_ID_LOCK_PEER_VIDEO,
	VTMSG_ID_UNLOCK_PEER_VIDEO,
	VTMSG_ID_VIDEO_CHANNEL_READY,
	VTMSG_ID_START_RECORD_PEER_VIDEO,
	VTMSG_ID_STOP_RECORD_PEER_VIDEO,
	VTMSG_ID_START_RECORD_PEER_VIDEO_AUDIO,
	VTMSG_ID_STOP_RECORD_PEER_VIDEO_AUDIO,	
	//for svc thread to exit
	VTMSG_ID_SVC_THREAD_EXIT = 0x08FF,
}VTSMSGINTERID;

//#define SENDMSGTODLAP(msgId,extData) SENDMESSAGE(MOD_SVC,VT_TASK_DLAP,msgId,extData,SOCKET_VT_DLAP)
//#define SENDMSGTODLVP(msgId,extData) SENDMESSAGE(MOD_SVC,VT_TASK_DLVP,msgId,extData,SOCKET_VT_DLVP)
//#define SENDMSGTOULAP(msgId,extData) SENDMESSAGE(MOD_SVC,VT_TASK_ULAP,msgId,extData,SOCKET_VT_ULAP)
//#define SENDMSGTOULVP(msgId,extData) SENDMESSAGE(MOD_SVC,VT_TASK_ULVP,msgId,extData,SOCKET_VT_ULVP)

#define VTS_INVALID_NR		  0xFFFFFFFF


#define DEC_CONFIG_BUF_SIZE 256
#define VTS_VIDEO_PACK_MAX		2048
#define VTS_AUDIO_PACK_MAX		1024
#define VTS_QUEUE_NOTE_NR		12

typedef struct VTSCodecConfig{
	MTK_LOCAL_PARA_HDR
	int len;
	unsigned char *data; 
	sem_t sem;
}VTSDecConfig, * PtrVTSDecConfig;


#ifdef __cplusplus
}
#endif

#endif /* __MTK_VT_DEFS_H__ */
