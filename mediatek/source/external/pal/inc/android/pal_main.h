/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

/*******************************************************************************
 * Filename:
 * ---------
 *  pal_main.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL task
 *
 * Author:
 * -------
 *  Saker Hsia (mtk02327)
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/
#ifndef _PAL_MAIN_H_
#define _PAL_MAIN_H_

#include <sys/socket.h>
#include <netpacket/packet.h>
#include <sys/un.h>

#include <linux/netlink.h>

/* PAL SAP interface */
typedef struct _PAL_SAP_INTERFACE
{
    int wndrv_control_fd;   /* WNDRV control file descriptor */
    int wndrv_data_fd;      /* WNDRV data file descriptor */
    int timer_fd;   /* PAL ctrl message file descriptor */
    int bthost_fd;  /* BT host file descriptor */
    int inject_fd;  /* inject msg file descriptor */
    int hal_fd;  /* HAL file descriptor */
    int chip_reset_fd;  /* File descriptor for whole chip reset event */
    int fgIsCmdSocketOn;  /* Indicate if cmd socket (/dev/ampc0) is on */
    struct sockaddr_ll pal_wndrv_addr;
    struct sockaddr_un pal_timer_addr;
    struct sockaddr_un pal_inject_addr;
    struct sockaddr_un pal_bt_addr;
    struct sockaddr_un pal_bt_peer_addr;
    struct sockaddr_un inject_addr;
    struct sockaddr_un pal_hal_addr;    
    int pal_timer_addr_len;
} PAL_SAP_INTERFACE;

/* PAL timer event */
typedef struct _PAL_TIMER_EVENT
{
    void (*func)(void*);    /* callback func pointer */
    void *arg;  /* callback func argument */
} PAL_TIMER_EVENT;

/* PAL HCI log information */
typedef struct _PAL_HCI_LOG
{
    UINT8   hci_log_status; /* 0:off, 1:on */
    FILE*   hci_file;
} PAL_HCI_LOG;

/* Whole chip reset socket related parameters */
struct genlmsghdr {
    __u8    cmd;
    __u8    version;
    __u16   reserved;
};
#define GENL_ID_CTRL    NLMSG_MIN_TYPE
#define GENL_HDRLEN     NLMSG_ALIGN(sizeof(struct genlmsghdr))
#define GENLMSG_DATA(glh) ((void *)((int)NLMSG_DATA(glh) + GENL_HDRLEN))
#define GENLMSG_PAYLOAD(glh) (NLMSG_PAYLOAD(glh, 0) - GENL_HDRLEN)
#define NLA_DATA(na) ((void *)((char*)(na) + NLA_HDRLEN))

enum {
    CTRL_CMD_UNSPEC,
    CTRL_CMD_NEWFAMILY,
    CTRL_CMD_DELFAMILY,
    CTRL_CMD_GETFAMILY,
    CTRL_CMD_NEWOPS,
    CTRL_CMD_DELOPS,
    CTRL_CMD_GETOPS,
    CTRL_CMD_NEWMCAST_GRP,
    CTRL_CMD_DELMCAST_GRP,
    CTRL_CMD_GETMCAST_GRP, /* unused */
    __CTRL_CMD_MAX,
};
#define CTRL_CMD_MAX (__CTRL_CMD_MAX - 1)

enum {
    CTRL_ATTR_UNSPEC,
    CTRL_ATTR_FAMILY_ID,
    CTRL_ATTR_FAMILY_NAME,
    CTRL_ATTR_VERSION,
    CTRL_ATTR_HDRSIZE,
    CTRL_ATTR_MAXATTR,
    CTRL_ATTR_OPS,
    CTRL_ATTR_MCAST_GROUPS,
    __CTRL_ATTR_MAX,
};

typedef struct _tagGenericNetlinkPacket {
    struct nlmsghdr n;
    struct genlmsghdr g;
    char buf[256];
} GENERIC_NETLINK_PACKET, *P_GENERIC_NETLINK_PACKET;

enum {
    CMD_RESET_START,
    CMD_RESET_END,
    CMD_UNKNOWN,
    __CMD_RST_STATE_MAX,
};

extern UINT8 pal_hci_log_switch (void);

extern void* pal_allocate_local_buffer ( UINT32 size );
extern void pal_free_local_buffer ( void* buf );

extern void* pal_get_local_data ( void* buf );
extern void pal_allocate_data_buffer ( void** buf, UINT32 data_len, UINT32 header_len, UINT32 tail_lan );
extern void pal_flush_tx_data ( void* buf );
extern void pal_flush_rx_data ( void* buf );
extern void pal_flush_rx_management_data ( void* buf );
extern void pal_send_wndrv_cmd( void* cmd_buf, UINT32 len );
extern void pal_send_wndrv_tx_data( void* data_buf, UINT32 len );
extern void pal_send_bt_event( void* event_buf, UINT32 len );
extern INT32 pal_wndrv_cmd_path_on ( void );
extern void pal_wndrv_cmd_path_off ( void );
extern INT32 pal_wndrv_data_path_on ( void );
extern void pal_wndrv_data_path_off ( void );


/*****************************************************************************
 * FUNCTION
 *  pal_main
 * DESCRIPTION
 *  Message handler
 * PARAMETERS
 *  ilm_ptr     [IN]     message data
 * RETURNS
 *  void
 *****************************************************************************/
extern void pal_main(ilm_struct *ilm_ptr);

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_set_timer_event
 * DESCRIPTION
 *
 * PARAMETERS
 *  *func:  callback function pointer
 *  *arg: argument pointer
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_pal_set_timer_event ( void (*func)(void*), void *arg );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_timer_enqueue
 * DESCRIPTION
 *
 * PARAMETERS
 *  event:  SIGALARM event
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_pal_timer_enqueue( int event );

#endif /* _PAL_MAIN_H_ */
