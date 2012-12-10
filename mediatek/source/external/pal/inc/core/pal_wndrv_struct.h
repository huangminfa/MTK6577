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
 *  pal_wndrv_struct.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL related MSG structure
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
#ifndef __PAL_WNDRV_STRUCT_H__
#define __PAL_WNDRV_STRUCT_H__

#ifndef _PAL_MAUI_
#include "mtkpal_porting.h"

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/

#define PAL_STATUS_SUCCESS  0x00000000
#define PAL_STATUS_FAILED   0x00000001

/* src mac addr + dest mac addr + proto type */
#define PAL_ETHERNET_MAC_SPACE 14
#define PAL_ETHERNET_HDR_LEN    8

#define PAL_MAC_ADDR_LEN    6
#define PAL_OUI_LEN         3

#define PAL_SSID_MAX_LEN    32

#define PAL_KEY_MAX_LEN     32

#define PAL_ROLE_ORIGINATOR 0
#define PAL_ROLE_RESPONDER  1

/* hci commnads */
#define PAL_AMP_ASSOC_FRAG_SIZE (248)

//#define PAL_AMP_KEY_SIZE        (32)
#define PAL_AMP_KEY_SIZE        (248)

#define PAL_FLOW_SPEC_SIZE      (16)

#define PAL_MAX_PDU_SIZE        (1500-8)
#define PAL_MAX_SDU_SIZE        (1500-8)

#define PAL_LOGICAL_LINK_NUM    (10)

#define PAL_MAX_CHAN_NUM (50)


/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/

typedef enum
{
    PAL_WNDRV_DESTROY_CONN = 0,
    PAL_WNDRV_DISCOVERY_TIMEOUT,
    PAL_WNDRV_INACTIVE_TIMEOUT,
    PAL_WNDRV_INAVAILABILITY,
    PAL_WNDRV_LOSE_PEER_CONN
} pal_wndrv_mac_disconnect_ind_status;

/*
   NOTICE :
   If you want to use MSG_ID_PAL_RAW_DATA,
   the data structure must be pal_raw_data_struct !!!
*/

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_start_ind_struct
*
*  DESCRIPTION
*     Notify start on channel if success.
***************************************************************************/
typedef struct
{
   kal_uint32       u4status; // 0: start success,
} pal_wndrv_mac_start_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_connect_ind_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{   
    kal_uint32      u4status;
} pal_wndrv_mac_connect_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_connect_fail_ind_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
   kal_uint8    ucError_code;     // error code (Reason)
} pal_wndrv_mac_connect_fail_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_disconnect_ind_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
	kal_uint8    aucDest_mac_addr[ PAL_MAC_ADDR_LEN ]; // support for multi-phy links, separate which phy link disconnect
   kal_uint32       u4status;
} pal_wndrv_mac_disconnect_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_cancel_ind_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{   
   kal_uint32   u4Status;     // status (Reason)
} pal_wndrv_mac_cancel_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_data_ind
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
   kal_uint8	aucSrc_mac_addr[ PAL_MAC_ADDR_LEN ];
   kal_uint8	aucDest_mac_addr[ PAL_MAC_ADDR_LEN ];
   kal_uint16	u2Proto_type;
   // Add by Nelson start (2010/04/18)
/*   
   kal_uint8	ucLsap;
   kal_uint8	ucDsap;
   kal_uint8	ucControl;
   kal_uint32	u4Oui;		// 3 bytes are meaningful
   kal_uint16	u2Ptotocol;
*/
   // Add by Nelson end (2010/04/18)
   kal_uint8    aucEthernet_hdr[ PAL_ETHERNET_HDR_LEN ];
   void*		ucData_p;   
} pal_wndrv_data_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_data_block_free_num_ind_struct
*
*  DESCRIPTION
*     Request number of freed data blocks.
***************************************************************************/
typedef struct
{
   kal_uint8	ucFree_data_block_num;
} pal_wndrv_data_block_free_num_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_query_status_ind
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
    kal_uint8   aucLocal_mac_addr[ PAL_MAC_ADDR_LEN ];	 // Local channel number    
    kal_uint8   ucAvailability; /* BoW network availability */
    kal_uint8   ucNumOfChannel; /* number of preferred channels */
    kal_uint8   aucChannelNum[ PAL_MAX_CHAN_NUM ];    /* preferred channel list */
} pal_wndrv_query_status_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_sync_tsf_ind_struct
*
*  DESCRIPTION
*     Sync TSF time with SCO time.
***************************************************************************/
typedef struct
{
    kal_uint32  u4Tsf_time_lower;
    kal_uint32  u4Tsf_time_upper;
    kal_uint32  u4Tsf_sys_time;
    kal_uint32  u4Sco_time;
    kal_uint32  u4Sco_sys_time;
} pal_wndrv_sync_tsf_ind_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_start_req_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
   kal_uint8    ucChannelNum;   
   kal_uint16	u2BeaconInterval;      
   kal_uint8    aucSsid[ PAL_SSID_MAX_LEN ];
   kal_uint16   u2SsidLen;
   kal_uint8	ucTimeoutDiscovery;
   kal_uint8	ucTimeoutInactivity;
   kal_uint8	ucPAL_Capabilities;  			// Bit0: 0=no guaranteed, 1=garanteed
   kal_int8		cMaxTxPower;   
   kal_uint8    ucRole;
   kal_uint8    aucDest_mac_addr[ PAL_MAC_ADDR_LEN ];
} pal_wndrv_mac_start_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_connect_req_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
   kal_uint8    ucRole; 						// PAL_ROLE_ORIGINATOR or PAL_ROLE_RESPONDER   
   kal_uint8    aucDest_mac_addr[ PAL_MAC_ADDR_LEN ];   
   kal_uint8    aucSsid[ PAL_SSID_MAX_LEN ];
   kal_uint16   u2SsidLen;
   kal_uint8	aucBSSID[ PAL_MAC_ADDR_LEN ];	// originator mac address
} pal_wndrv_mac_connect_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_add_key_req_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
   kal_uint32   u4Key_index;
   kal_uint32   u4Key_Length;
   kal_uint8        aucDstMacAddr[PAL_MAC_ADDR_LEN]; // BSSID?
   kal_uint32   u4Key_rsc[2];
   kal_uint8    aucKey_material[PAL_KEY_MAX_LEN];
} pal_wndrv_add_key_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_disconnect_req_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{      
   kal_uint8    aucSsid[ PAL_SSID_MAX_LEN ];   
   kal_uint16   u2SsidLen;
   kal_uint8    aucPeer_mac_addr[PAL_MAC_ADDR_LEN];
} pal_wndrv_mac_disconnect_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_mac_cancel_req_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
} pal_wndrv_mac_cancel_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_data_req_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
    kal_uint8	aucSrc_mac_addr[ PAL_MAC_ADDR_LEN ];
    kal_uint8	aucDest_mac_addr[ PAL_MAC_ADDR_LEN ];
    kal_uint16	u2Proto_type;
    kal_uint8   aucEthernet_hdr[ PAL_ETHERNET_HDR_LEN ];
    void*		ucData_p;
    kal_uint8   ucUser_priority;
} pal_wndrv_data_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_query_status_struct
*
*  DESCRIPTION
*     Request data transmission.
***************************************************************************/
typedef struct
{
} pal_wndrv_query_status_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_set_activity_report_req_struct
*
*  DESCRIPTION
*     Set Activity report request.
***************************************************************************/
typedef struct
{
    kal_uint8	aucPeer_mac_addr[ PAL_MAC_ADDR_LEN ];
    kal_uint8	ucSchedule_known;
    kal_uint8	ucNum_reports;
    kal_uint32	u4Start_time;
    kal_uint32	u4Duration;
    kal_uint32	u4Periodicity;
} pal_wndrv_set_activity_report_req_struct;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     pal_wndrv_short_range_mode_req_struct
*
*  DESCRIPTION
*     Short range mode request.
***************************************************************************/
typedef struct
{
    kal_uint8	aucPeer_mac_addr[ PAL_MAC_ADDR_LEN ];
    kal_uint8	ucTx_power;
    kal_uint8	resv;
} pal_wndrv_short_range_mode_req_struct;

#endif

#endif /* __PAL_WNDRV_STRUCT_H__ */
