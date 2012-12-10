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
 *  pal_internal.h
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
#ifndef _PAL_INTERNEL_H_
#define _PAL_INTERNEL_H_

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
/*common header*/

/*specific header*/
#include "mtkpal_porting.h"
#ifdef _PAL_MAUI_
#include "pal_struct.h"         // for PAL_LOGICAL_LOCAL_AMP_ASSOC_CMD_BUFF_T
#else
#include "pal_wndrv_struct.h" 
#include "pal_hci_struct.h" 
#endif

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/
#define PAL_SET_FIELD_16(_memAddr_p, _value) \
            { \
                UINT8* __cp = (UINT8*) (_memAddr_p); \
                __cp[0] = (UINT8) (_value); \
                __cp[1] = (UINT8) ((_value) >> 8); \
            }
    
#define PAL_SET_FIELD_32(_memAddr_p, _value) \
            { \
                UINT8* __cp = (UINT8*) (_memAddr_p); \
                __cp[0] = (UINT8) (_value); \
                __cp[1] = (UINT8) ((_value) >> 8); \
                __cp[2] = (UINT8) ((_value) >> 16); \
                __cp[3] = (UINT8) ((_value) >> 24); \
            }
    
#define PAL_SET_FIELD_64(_memAddr_p, _value) \
            { \
                UINT8* __cp = (UINT8*) (_memAddr_p); \
                __cp[0] = (UINT8) (_value); \
                __cp[1] = (UINT8) ((_value) >> 8); \
                __cp[2] = (UINT8) ((_value) >> 16); \
                __cp[3] = (UINT8) ((_value) >> 24); \
                __cp[4] = (UINT8) ((_value) >> 32); \
                __cp[5] = (UINT8) ((_value) >> 40); \
                __cp[6] = (UINT8) ((_value) >> 48); \
                __cp[7] = (UINT8) ((_value) >> 56); \
            }

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/
#define PHY_LINK_LIMITATION         (4)
#define PHY_LINK_ACCESS_TIMEOUT     (5) // unit: sec.
#define LOGICAL_LINK_ACCESS_TIMEOUT (5) // unit: sec.

#define PAL_MAC_ADDR_SIZE           (6)
#define PAL_CHANNEL_LIST_SIZE       (56)
#define PAL_80211PAL_VERSION_SIZE   (5)
#define PAL_AMP_KEY_LEN             (32)

#define PAL_AMP_DATA_SIZE           (360) // size of AMP data
#define PAL_TOTAL_DATA_BLOCK_NUM    (1)   // Total number of data blocks

#define PAL_RESV_LOGICAL_HANDLE             (0)   // Reserved logical link handle
#define PAL_LOGICAL_LINK_HANDLE_BASE        (256) // Logical link handle base

#define PAL_FLOW_SPEC_ID_INDEX              (0)   // Flow ID index of Flow Spec
#define PAL_FLOW_SPEC_SERVICE_TYPE_INDEX    (1)   // Service Type index of Flow Spec
#define PAL_FLOW_SPEC_MAX_SDU_INDEX         (2)   // Maximum SDU size index of Flow Spec
#define PAL_FLOW_SPEC_SDU_INTER_TIME_INDEX  (4)   // SDU inter-arrival time index of Flow Spec
#define PAL_FLOW_SPEC_ACCESS_LATENCY_INDEX  (8)   // Access latency index of Flow Spec
#define PAL_FLOW_SPEC_FLUSH_TIMEOUT_INDEX   (12)  // Flush timeout index of Flow Spec

#define PAL_80211_MAX_TX_POWER              (20)  // Maximum transmit power level  

#define PAL_NUM_TU_ONE_US                   (1024) // 1 us = 1024 TU

#define PAL_DEBUG_DATA_QUEUE_SIZE           (10)

#define PAL_AAR_BT_TRAFFIC_DURATION (1250)
#define PAL_AAR_BT_TRAFFIC_PERIODICITY (3750)

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/
/* AMP status enum */
typedef enum
{
    PHY_POWER_DOWN = 0,
    AMP_ONLY_USED_BY_BT,
    AMP_NO_CAPACITY_FOR_BT,
    AMP_LOW_CAPACITY_FOR_BT,
    AMP_MEDIUM_CAPACITY_FOR_BT,
    AMP_HIGH_CAPACITY_FOR_BT,
    AMP_FULL_CAPACITY_FOR_BT
} PAL_AMP_STATUS_T; 

/* ASSOC data information */
typedef struct _PAL_AMP_ASSOC_DATA_T
{
    UINT16  u2Amp_assoc_length;
    UINT8   aucAmp_data[ PAL_MAX_ASSOC_LEN ];
} PAL_AMP_ASSOC_DATA_T;

/* PAL AMP Info */
typedef struct _PAL_AMP_INFO_T
{
    // Local AMP Info
    UINT8   ucAmp_status;                   // enum of PAL_AMP_STATUS_T
    UINT32  u4Total_bandwidth;
    UINT32  u4Max_guaranteed_bandwidth;
    UINT32  u4Min_latency;
    UINT32  u4Max_pdu_size;
    UINT8   ucController_type;
    UINT16  u2Pal_capabilities;
    UINT16  u2Max_amp_assoc_len;
    UINT32  u4Max_flush_timeout;
    UINT32  u4Best_effort_flush_timeout;
    // Physical link info
    UINT8   ucNum_of_physical_link;
    UINT8   ucConnecting_physical_link;	    // connecting physical link handle (no connection: PAL_RESV_PHY_HANDLE)
    UINT8   ucDisconnecting_physical_link;	// the last physical link is performing disconnecting
    UINT8   ucConnecting_channel_num;	    // connecting physical link channel number
	PAL_AMP_ASSOC_DATA_T 	amp_assoc_data;	// AMP ASSOC data
	UINT8   aucDisconn_phy_link[ PHY_LINK_LIMITATION ];    /* disconnecting physical link handle array */
	UINT8   ucDisconn_phy_link_head_index;
	UINT8   ucDisconn_phy_link_num;
	// Logical link info
    UINT8   ucNum_of_logical_link;
    // wndrv interface info
    INT32   s4Wndrv_cmd_interface_status;   /* -1:cloe, >=0: open */
    INT32   s4Wndrv_data_interface_status;  /* -1:cloe, >=0: open */
} PAL_AMP_INFO_T;

/* PAL AMP ASSOC */
typedef struct _PAL_AMP_ASSOC_T
{
    UINT8   aucMac_address[ PAL_MAC_ADDR_SIZE ];
    UINT8   ucPreferred_channel_len;
    UINT8   aucPreferred_channel_list[ PAL_CHANNEL_LIST_SIZE ];
    UINT8   ucConnected_channel_len;
    UINT8   aucConnected_channel_list[ PAL_CHANNEL_LIST_SIZE ];
    UINT32  u4Pal_80211_capabilities;
    UINT8   aucPal_80211_version[ PAL_80211PAL_VERSION_SIZE ];
} PAL_AMP_ASSOC_T;

/* Dedicated AMP Key Type */
typedef enum
{
    PAL_RESV_KEY        = 0x00,
    PAL_DEBUG_COMB_KEY  = 0x03,
    PAL_UNAUTH_COMB_KEY,
    PAL_AUTH_COMB_KEY
} PAL_AMP_KEY_TYPE_T; 

/* AMP Key Info */
typedef struct _PAL_AMP_KEY_INFO_T
{
    UINT8   ucDedicated_amp_key_length;
    UINT8   ucDedicated_amp_key_type;               // enum of PAL_AMP_KEY_TYPE_T
    UINT8   aucDedicated_amp_key[PAL_AMP_KEY_LEN];
} PAL_AMP_KEY_INFO_T;

/* Role of physical link enum */
typedef enum
{
    ORIGINATOR = 0,
    RESPONDER
} PAL_PHY_ROLE_T; 

/* State of physical link enum */
typedef enum
{
    DISCONNECTED = 0,
    STARTING,
    CONNECTING,
    AUTHENTICATING,
    CONNECTED,
    DISCONNECTING
} PAL_PHY_STATE_T; 

typedef struct _PAL_PHY_LINK_COMPLETE_T
{
	UINT8	need_event;		//0: not need, 1: need
	UINT8	status;
} PAL_PHY_LINK_COMPLETE_T;

typedef struct _PAL_LINK_SUPERVISION_T
{
	UINT8	ls_req_send;    // if link supervision request is sent (0: not sent, 1: sent)
	UINT8	ls_timer_id;    // link supervision timer id
	UINT16  ls_timeout;     // link supervision timeout
    UINT16  ls_req_period;  // link supervision request period
    UINT8	ls_retry_times; // timeout retry times
} PAL_LINK_SUPERVISION_T;

typedef struct _PAL_PHY_WNDRV_INFO_T
{
    UINT8	mac_start_req_sent;    // if mac start req is sent (0: not sent, 1: sent)
	UINT8	mac_connect_req_sent;  // if mac connect req is sent (0: not sent, 1: sent)
} PAL_PHY_WNDRV_INFO_T;

/* Physical Link Info */
typedef struct _PAL_PHY_LINK_T
{
    UINT8               		ucPhy_link_handle;
    PAL_AMP_KEY_INFO_T  		key_info;
    PAL_AMP_INFO_T      		peer_dev_info;
    PAL_AMP_ASSOC_T     		peer_dev_assoc;
    PAL_PHY_ROLE_T      		phy_link_role;              // enum of PAL_PHY_ROLE_T
    PAL_PHY_STATE_T     		phy_link_state;             // enum of PAL_PHY_STATE_T
    PAL_PHY_LINK_COMPLETE_T		phy_link_complete;          // Indicate if the device need to send HCI complete event
    PAL_LINK_SUPERVISION_T      phy_link_supervision;
    PAL_PHY_WNDRV_INFO_T        phy_wndrv_info;             // Indicate if the device sent the mac_start/mac_connect req
    UINT8               		phy_link_accept_timer_id;   // timer id for PAL_PHY_LINK_CONN_ACCRPT_TIMER
} PAL_PHY_LINK_T;

/*HCI event status */
typedef enum
{
    SUCCEEDED 				= 0x00,
    UNKNOWN_HCI_CMD			= 0x01,
    UNKNOWN_CONN_ID			= 0x02,
    AUTHNETICATION_FAILED   = 0x05,
    CONN_TIMEOUT			= 0x08,
    CONN_LIMIT_EXCEED 		= 0x09,
    CONN_ACCEPTION_TIMEOUT  = 0x10,
    ACL_CONN_EXIST 			= 0x0B,
    CMD_DISALLOW			= 0x0C,
    REJECTED_LIMITED_RES 	= 0x0D,
    INVALID_HCI_CMD_PARA    = 0x12,
    CONN_TERM_BY_REMOTE_HOST= 0x13,
    CONN_TERM_BY_LOCAL_HOST	= 0x16,
    QOS_REJECTED            = 0x2D,
    CONN_NO_SUITABLE_CHANNEL= 0x39
} PAL_HCI_EVENT_STATUS_T; 

/* Flow Spec Info */
typedef struct _PAL_FLOW_SPEC_T
{
    UINT8               ucIdentifier;
    UINT8               ucService_type;
    UINT16              u2Max_sdu_size;
    UINT32              u4Sdu_inter_arrival_time;
    UINT32              u4Access_latency;
    UINT32              u4Flush_timeout;
} PAL_FLOW_SPEC_T;

/* State of logical link enum */
typedef enum
{
    PAL_LOGICAL_DISCONNECTED = 0,
    PAL_LOGICAL_CREATING,
    PAL_LOGICAL_CREATED
} PAL_LOGICAL_STATE_T;

/* Logical Link Info */
typedef struct _PAL_LOGICAL_LINK_T
{
    UINT8               ucPhy_link_handle;
    UINT16              u2Logical_link_handle;
    PAL_FLOW_SPEC_T     tx_flow_spec;
    PAL_FLOW_SPEC_T     rx_flow_spec;
    PAL_LOGICAL_STATE_T logical_link_state;     // enum of PAL_LOGICAL_STATE_T
    UINT8               ucUser_priority;        // user priority parameter for EDCA
} PAL_LOGICAL_LINK_T;

/* TypeIDs used for 802.11 MAP TLVs */
typedef enum
{
    PAL_80211_MAC_ADDR = 0x01,
    PAL_80211_PREFER_CHANNEL,
    PAL_80211_CONN_CHANNEL,
    PAL_80211_CAPABILITIES,
    PAL_80211_VERSION
} PAL_80211_TLV_T; 

/* Logical AMP ASSOC Command buffer */
typedef struct _PAL_LOGICAL_LOCAL_AMP_ASSOC_CMD_BUFF_T
{
    pal_bt_read_local_amp_assoc_command_struct  ucLocal_amp_assoc_buff;
    UINT8                                       ucIs_need_local_amp_event; // 0: not need, 1: need
} PAL_LOGICAL_LOCAL_AMP_ASSOC_CMD_BUFF_T;

/* Physical link loss reason */
typedef enum
{
    PAL_PHY_LINK_LOSS_UNKNOWN = 0,
    PAL_PHY_LINK_LOSS_RANGE_RELATED,
    PAL_PHY_LINK_LOSS_BANDWIDTH_RELATED,
    PAL_PHY_LINK_LOSS_RESOLVING_CONFLICT,
    PAL_PHY_LINK_LOSS_INTERFERENCE
} PAL_PHY_LINK_LOSS_REASON_T;

/* Protocol id for data frame */
typedef enum
{
    PAL_ACL_DATA_FRAME = 1,
    PAL_ACTIVITY_REPORT_FRAME,
    PAL_SECURITY_FRAME,
    PAL_LINK_SUPERVISION_REQ_FRMAE,    
    PAL_LINK_SUPERVISION_REPLY_FRMAE
} PAL_DATA_PROTOCOL_ID;

/* Data buffer information */
typedef struct _PAL_DATA_BUFF_INFO_T
{
    UINT8   ucTable_head_index;         // Head index of au2Logical_handle_of_tx_pkt_table
    UINT8   ucNum_of_buff_allocation;   // Number of allocation in au2Logical_handle_of_tx_pkt_table
    UINT16  au2Logical_handle_of_tx_pkt_table[PAL_TOTAL_DATA_BLOCK_NUM]; // Packets of logical handle tx sequence table
    UINT8   ucNum_of_flush_pkt_handle;      // Number of allocation in au2Flush_pkt_handle_table
    UINT8   aucFlush_pkt_handle_table[PAL_LOGICAL_LINK_NUM];             // Handle of flush packet table
} PAL_DATA_BUFF_INFO_T;

/* Local amp info setting type enum */
typedef enum
{
    PAL_SET_AMP_STATUS,
    PAL_SET_TOTAL_BANDWIDTH,
    PAL_SET_MAX_GUARANTEED_BANDWIDTH,
    PAL_SET_MIN_LATENCY,
    PAL_SET_MAX_PDU_SIZE,
    PAL_SET_PAL_CAPABILITIES,
    PAL_SET_MAX_AMP_ASSOC_LENGTH,
    PAL_SET_MAX_FLUSH_TIMEOUT,
    PAL_SET_BEST_EFFORT_FLUSH_TIMEOUT,
    PAL_SET_LOCAL_AMP_INFO_END = PAL_SET_BEST_EFFORT_FLUSH_TIMEOUT
} PAL_LOCAL_AMP_INFO_SET_TYPE;

/* Local amp assoc setting type enum */
typedef enum
{
    PAL_GET_MAC_ADDRESS,
    PAL_SET_PREFERRED_CHANNEL_LIST,
    PAL_SET_CONNECTED_CHANNEL_LIST,
    PAL_SET_80211_CAPABILITIES,
    PAL_SET_LOCAL_AMP_ASSOC_END = PAL_SET_80211_CAPABILITIES
} PAL_LOCAL_AMP_ASSOC_SET_TYPE;

/* Local amp configuration setting type enum */
typedef enum
{
    PAL_SET_PHY_LINK_CREATION_TIMEOUT,
    PAL_SET_GLOBAL_SUPERVISION_SWITCH,
    PAL_SET_LINK_SUPERVISION_TIMEOUT,
    PAL_SET_TEST_MODE_SWITCH,
    PAL_SET_SECURITY_MODE_SWITCH,
    PAL_SET_PHY_LINK_TIMEOUT_MODE_SWITCH,   /* 0: report CONN_ACCEPTION_TIMEOUT to BT, 1: report CONN_TIMEOUT to BT */
    PAL_SET_LOCAL_AMP_CONFIGURATION_END = PAL_SET_PHY_LINK_TIMEOUT_MODE_SWITCH
} PAL_LOCAL_AMP_CONFIGURATION_TYPE;

/* Local parameters setting type */
typedef struct _PAL_LOCAL_PARAMETER_SET_T
{
    UINT32  u4Phy_link_creation_timeout;
    UINT8   ucLink_supervision_switch;
    UINT8   ucLink_supervision_retry_times;
    UINT16  u2Link_supervision_timeout;
    UINT8   ucTest_mode_switch;
    UINT8   ucSecurity_mode_switch;
    UINT8   ucPhyLink_timeout_mode_switch;
} PAL_LOCAL_PARAMETER_SET_T;

/* Event mask page 2 bitmap */
typedef enum
{
    PAL_NO_EVENTS = 0x0000,
    PAL_PHY_LINK_COMPLETE_EVENT,
    PAL_CHANNEL_SELECTED_EVENT,
    PAL_DISCONNECTION_PHY_LINK_EVENT = 0x0004,
    PAL_PHY_LINK_LOSS_EARLY_WARNING_EVENT = 0x0008,
    PAL_PHY_LINK_RECOVERY_EVENT = 0x0010,
    PAL_LOGICAL_LINK_COMPLETE_EVENT = 0x0020,
    PAL_DISCONNECTION_LOGICAL_LINK_COMPLETE_EVENT = 0x0040,
    PAL_FLOW_SPEC_MODIFY_COMPLETE_EVENT = 0x0080,
    PAL_NUM_OF_COMPLETED_DATA_BLOCKS_EVENT = 0x0100,
    PAL_AMP_START_TEST_EVENT = 0x0200,
    PAL_AMP_TEST_END_EVENT = 0x0400,
    PAL_AMP_RECEIVER_REPORT_EVENT = 0x0800,
    PAL_SHORT_RANGE_MODE_CHANGE_COMPLETE_EVENT = 0x1000,
    PAL_AMP_STATUS_CHANGE_EVENT = 0x2000
} PAL_EVENT_MASK_PAGE_2_BITMAP_TYPE;

/* For host test usage parameters */
typedef struct _PAL_HOST_TEST_SET_T
{
    UINT8   ucDrop_tx_pkt_num;
} PAL_HOST_TEST_SET_T;

/* PAL debug information */
typedef struct _PAL_DEBUG_INFO_T
{
    UINT16   pkt_data_tx_size_array[PAL_DEBUG_DATA_QUEUE_SIZE];
    UINT16   pkt_data_rx_size_array[PAL_DEBUG_DATA_QUEUE_SIZE];
    UINT16   pkt_data_tx_flush_size_array[PAL_DEBUG_DATA_QUEUE_SIZE];
    UINT16   pkt_data_rx_flush_size_array[PAL_DEBUG_DATA_QUEUE_SIZE];
    UINT32   pkt_data_tx_times;
    UINT32   pkt_data_rx_times;
    UINT32   pkt_data_tx_flush_times;
    UINT32   pkt_data_rx_flush_times;
} PAL_DEBUG_INFO_T;

/*******************************************************************************
*                            P U B L I C   D A T A
********************************************************************************
*/

/*******************************************************************************
*                           P R I V A T E   D A T A
********************************************************************************
*/

#endif /* _PAL_KERNEL_H_ */

