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
 *  pal_core.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL Kernel Functions Definition
 *
 * Author:
 * -------
 *  Nelson Chang (mtk02783)
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
#ifndef _PAL_KERNEL_H_
#define _PAL_KERNEL_H_

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
/*common header*/

/*specific header*/
#include "mtkpal_porting.h"

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/
#define LOGICAL_LINK_LIMITATION  (10)

#define PAL_RESV_PHY_HANDLE      (0) // reserved physical link
#define PAL_RESV_PHY_CHANNEL     (0) // reserved channel, not used for physical link connection
#define PAL_RESV_LOGICAL_CHANNEL (0) // reserved channel, not used for logical link connection
#define PAL_DEFAULT_PHY_CHANNEL  (11)// default WNDRV connection used channel

#define PAL_SSID_LEN             (21) // SSID length of SSID for WNDRV

#define PAL_CONNECTINE_ACCEPT_TIMEOUT   (10000)   // ms of connection accept timeout

// for authentication module
#define PAL_AUTHENTICATION_SUCCESS      (0)
#define PAL_AUTHENTICATION_FAILED       (1)

// for authentication module
#define PAL_LINK_SUPERVISION_TIMEOUT    (5000) // ms of link supervision timeout
#define PAL_LINK_SUPERVISION_REQ_PERIOD (4000) // ms of link supervision request period

/* workaround for PAL turn on data interface dynamically */
#define PAL_ORIGINATOR_4_WAY_DELAY (3000)


/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/
/* system APIs */
/* debug */
typedef INT32           (*DBG_PRINT)(const char *str, ...);
typedef INT32           (*DBG_ASSERT)(INT32 expr, const char *file, INT32 line);
/* timer */
typedef void            (*TIMER_START)(UINT8 timer_id, UINT32 period, void (*func)(void*), void* arg);
typedef void            (*TIMER_STOP)(UINT8 timer_id);
/* kernel lib */
typedef void*           (*SYS_MEMCPY)(void *dest, const void *src, UINT32 n);
typedef void*           (*SYS_MEMSET)(void *s, INT32 c, UINT32 n);
typedef INT32           (*SYS_MEMCMP)(void *dest, const void *src, UINT32 n);
typedef INT32           (*SYS_SPRINTF)(char *str, const char *format, ...);
typedef UINT32          (*SYS_MSLEEP)(UINT32 msec);
/* interface control */
typedef INT32          (*WNDRV_DATA_PATH_ON)(void);
typedef void           (*WNDRV_DATA_PATH_OFF)(void);

typedef struct
{
    /* debug */
    DBG_PRINT       cb_dbg_print;
    DBG_ASSERT      cb_dbg_assert;    
    /* timer */
    TIMER_START     cb_timer_start;
    TIMER_STOP      cb_timer_stop;
    /* kernel lib */
    SYS_MEMCPY      cb_memcpy;
    SYS_MEMSET      cb_memset;
    SYS_MEMCMP      cb_memcmp;
    SYS_SPRINTF     cb_sprintf;
    SYS_MSLEEP      cb_msleep;
    /* interface control */
    WNDRV_DATA_PATH_ON  cb_wndrv_data_path_on;
    WNDRV_DATA_PATH_OFF cb_wndrv_data_path_off;
}mtkpal_callback;

/* exported functions */
/*******************************************************************************
*                  F U N C T I O N   D E C L A R A T I O N S
********************************************************************************
*/
/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_wndrv_get_80211_cap
* DESCRIPTION
*  Get IEEE80211 capabilites info
* PARAMETERS
*  None.
* RETURNS 
*  g_pal_local_amp_assoc.u4Pal_80211_capabilities
*****************************************************************************/
extern UINT32 mtk_wcn_pal_wndrv_get_80211_cap ( void );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_init
* DESCRIPTION
*  Initialize callback functions and internal data of PAL kernel
* PARAMETERS
*   None.
* RETURNS 
*      1. -1: Register callback function failed
*      2. -2: Initialize PAL internal data failed
*      3.  0: PAL initialization success
*****************************************************************************/
extern INT32 mtk_wcn_pal_init(void);

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_deinit
* DESCRIPTION
*  deinit PAL kernel
* PARAMETERS
*  void
* RETURNS 
*  INT32    0 = success, others = failure
*****************************************************************************/
extern INT32 mtk_wcn_pal_deinit(void);

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_set_host_test_drop_packet_times
* DESCRIPTION
*  Set g_pal_host_test_set.ucDrop_tx_pkt_num
* PARAMETERS
*  drop_ptk_times   [IN]  Drop tx packet times
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_set_host_test_drop_packet_times  ( UINT8 drop_ptk_times );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_get_test_mode
* DESCRIPTION
*  Get the value of test mode setting
* PARAMETERS
*  void
* RETURNS 
*  INT8  g_pal_local_parameter_set.ucTest_mode_switch:  0 = test, 1 = normal
*****************************************************************************/
extern INT8 mtk_wcn_pal_get_test_mode(void);

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_set_local_amp_info
* DESCRIPTION
*  Set local amp info parameters from inject message
* PARAMETERS
*  type         [IN] amp info type of setting
*  rw           [IN] rw=0:read, rw=1:set
*  parameter    [IN] set amp info parameter value
* RETURNS 
*  none.
*****************************************************************************/
extern void mtk_wcn_pal_set_local_amp_info ( UINT8 type, UINT8 rw, void* parameter );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_set_local_amp_assoc
* DESCRIPTION
*  Set local amp assoc parameters from inject message
* PARAMETERS
*  type         [IN] amp info type of setting
*  rw           [IN] rw=0:read, rw=1:set
*  parameter    [IN] set amp info parameter value
* RETURNS
*  none.
*****************************************************************************/
extern void mtk_wcn_pal_set_local_amp_assoc ( UINT8 type, UINT8 rw, UINT32* parameter );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_set_local_amp_configuration
* DESCRIPTION
*  Set local amp configuration parameters from inject message
* PARAMETERS
*  type         [IN] amp info type of setting
*  rw           [IN] rw=0:read, rw=1:set
*  parameter    [IN] set amp configure parameter value
* RETURNS 
*  none.
*****************************************************************************/
extern void mtk_wcn_pal_set_local_amp_configuration ( UINT8 type, UINT8 rw, UINT32* parameter );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_send_security_frame_handler
 * DESCRIPTION
 *  send security frame data handler
 * PARAMETERS
 *  msg     [IN]    security data message
 *  len     [IN]    security data message length
 * RETURNS
 *  none.
 *****************************************************************************/
extern void mtk_wcn_pal_send_security_frame_handler ( void* msg, UINT16 len );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_get_security_mode
 * DESCRIPTION
 *  Get the security mode is enabling or not
 * PARAMETERS
 *  none.
 * RETURNS
 *  The value of g_pal_local_parameter_set.ucSecurity_mode_switch
 *****************************************************************************/
extern UINT8 mtk_wcn_pal_get_security_mode (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_get_phy_link_timeout_mode
 * DESCRIPTION
 *  Get the physical link timeout mode
 * PARAMETERS
 *  none.
 * RETURNS
 *  The value of g_pal_local_parameter_set.ucPhyLink_timeout_mode_switch
 *****************************************************************************/
extern UINT8 mtk_wcn_pal_get_phy_link_timeout_mode (void);

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_report_authentication_result
 * DESCRIPTION
 *  Report authentication result report
 * PARAMETERS
 *  status  [IN]    4-way handshake status code
 * RETURNS
 *  none.
 *****************************************************************************/
extern void mtk_wcn_pal_report_authentication_result ( UINT8 status );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_manager
* DESCRIPTION
*  PAL Manager
* PARAMETERS
*  msg_type     [IN] message type
*  msg          [IN] HCI command message pointer
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_manager( UINT32 msg_type, void* msg );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_phy_link_manager
* DESCRIPTION
*  PHysical Link Manager
* PARAMETERS
*  handle       [IN] physical link handle
*  msg_type     [IN] message type
*  msg          [IN] command message pointer
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_phy_link_manager( UINT8 handle, UINT32 msg_type, void* msg );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_logical_link_manager
* DESCRIPTION
*  Logical Link Manager
* PARAMETERS
*  handle       [IN] physical/logical link handle
*  msg_type     [IN] message type
*  msg          [IN] command message pointer
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_logical_link_manager( UINT16 handle, UINT32 msg_type, void* msg );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_data_manager
* DESCRIPTION
*  Data Manager
* PARAMETERS
*  handle       [IN] physical/logical link handle
*  msg_type     [IN] message type
*  msg          [IN] command message pointer
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_data_manager( UINT16 handle, UINT32 msg_type, void* msg );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_pal_set_link_quality
 * DESCRIPTION
 *  Set wireless link quality by wndrv
 * PARAMETERS
 *  none.
 * RETURNS
 *	link quality: 0x00 - 0xFF
 *****************************************************************************/
extern void mtk_wcn_pal_set_link_quality( UINT8 link_quality );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_wndrv_add_disconnect_phy_link
* DESCRIPTION
*  Add disconnecting 
* PARAMETERS
*  peer_mac: MAC addr of peer device
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_wndrv_add_disconnect_phy_link ( UINT8* peer_mac );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_wndrv_remove_disconnect_phy_link
* DESCRIPTION
*  Initialize callback functions and internal data of PAL kernel
* PARAMETERS
*  None.
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_wndrv_remove_disconnect_phy_link (void);

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_wndrv_get_disconnect_phy_mac
* DESCRIPTION
*  Initialize callback functions and internal data of PAL kernel
* PARAMETERS
*  None.
* RETURNS 
*  	MAC addr of disconnecting peer device
*****************************************************************************/
extern UINT8* mtk_wcn_pal_wndrv_get_disconnect_phy_mac (void);

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_destroy_phy_links
* DESCRIPTION
*  Destroy all physical links
* PARAMETERS
*  none.
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_destroy_phy_links ( void );

/*****************************************************************************
* FUNCTION
*  mtk_wcn_pal_set_wndrv_cmd_interface
* DESCRIPTION
*  Destroy all physical links
* PARAMETERS
*  none.
* RETURNS 
*  None.
*****************************************************************************/
extern void mtk_wcn_pal_set_wndrv_cmd_interface ( INT32 status );

#endif /* _PAL_KERNEL_H_ */

