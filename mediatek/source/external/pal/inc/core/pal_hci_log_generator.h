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
 *  pal_hci_log_generator.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL HCI Log Generator Definition
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
#ifndef _PAL_HCI_LOG_GENERATOR_H_
#define _PAL_HCI_LOG_GENERATOR_H_

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
#include "pal_system_call.h"
#include "pal_internal.h"

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/
/* Command opcode pack/unpack */
#define cmd_opcode_pack(ogf, ocf)	(UINT16)((ocf & 0x03ff)|(ogf << 10))
#define cmd_opcode_ogf(op)		    (op >> 10)
#define cmd_opcode_ocf(op)		    (op & 0x03ff)

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/
#define HCI_UART_TYPE_SIZE   1
#define HCI_UART_CMD_TYPE   1
#define HCI_UART_ACL_DATA_TYPE  2
#define HCI_UART_EVENT_TYPE   4
#define HCI_RAW_HDR_SIZE    3

#define HCI_MAX_ACL_SIZE    1024
#define HCI_MAX_SCO_SIZE	255
#define HCI_MAX_EVENT_SIZE	260
//#define HCI_MAX_FRAME_SIZE  (HCI_MAX_ACL_SIZE + 4)
#define HCI_MAX_FRAME_SIZE  sizeof(ilm_struct)

/* To/from host definitions */
#define HCI_FROM_HOST       0x00
#define HCI_TO_HOST         0x01

/* HCI Packet types */
#define HCI_COMMAND_PKT		0x01
#define HCI_ACLDATA_PKT		0x02
#define HCI_SCODATA_PKT		0x03
#define HCI_EVENT_PKT		0x04
#define HCI_VENDOR_PKT		0xff

/* HCI Error codes */
#define HCI_UNKNOWN_COMMAND			        0x01
#define HCI_NO_CONNECTION			        0x02
#define HCI_HARDWARE_FAILURE		        0x03
#define HCI_PAGE_TIMEOUT			        0x04
#define HCI_AUTHENTICATION_FAILURE  	    0x05
#define HCI_PIN_OR_KEY_MISSING		        0x06
#define HCI_MEMORY_FULL				        0x07
#define HCI_CONNECTION_TIMEOUT		        0x08
#define HCI_MAX_NUMBER_OF_CONNECTIONS		0x09
#define HCI_MAX_NUMBER_OF_SCO_CONNECTIONS	0x0a
#define HCI_ACL_CONNECTION_EXISTS		    0x0b
#define HCI_COMMAND_DISALLOWED			    0x0c
#define HCI_REJECTED_LIMITED_RESOURCES		0x0d
#define HCI_REJECTED_SECURITY			    0x0e
#define HCI_REJECTED_PERSONAL			    0x0f
#define HCI_HOST_TIMEOUT			        0x10
#define HCI_UNSUPPORTED_FEATURE			    0x11
#define HCI_INVALID_PARAMETERS			    0x12
#define HCI_OE_USER_ENDED_CONNECTION		0x13
#define HCI_OE_LOW_RESOURCES			    0x14
#define HCI_OE_POWER_OFF			        0x15
#define HCI_CONNECTION_TERMINATED		    0x16
#define HCI_REPEATED_ATTEMPTS			    0x17
#define HCI_PAIRING_NOT_ALLOWED			    0x18
#define HCI_UNKNOWN_LMP_PDU			        0x19
#define HCI_UNSUPPORTED_REMOTE_FEATURE	    0x1a
#define HCI_SCO_OFFSET_REJECTED			    0x1b
#define HCI_SCO_INTERVAL_REJECTED		    0x1c
#define HCI_AIR_MODE_REJECTED			    0x1d
#define HCI_INVALID_LMP_PARAMETERS		    0x1e
#define HCI_UNSPECIFIED_ERROR			    0x1f
#define HCI_UNSUPPORTED_LMP_PARAMETER_VALUE	0x20
#define HCI_ROLE_CHANGE_NOT_ALLOWED		0x21
#define HCI_LMP_RESPONSE_TIMEOUT		0x22
#define HCI_LMP_ERROR_TRANSACTION_COLLISION	0x23
#define HCI_LMP_PDU_NOT_ALLOWED			0x24
#define HCI_ENCRYPTION_MODE_NOT_ACCEPTED	0x25
#define HCI_UNIT_LINK_KEY_USED			0x26
#define HCI_QOS_NOT_SUPPORTED			0x27
#define HCI_INSTANT_PASSED			0x28
#define HCI_PAIRING_NOT_SUPPORTED		0x29
#define HCI_TRANSACTION_COLLISION		0x2a
#define HCI_QOS_UNACCEPTABLE_PARAMETER		0x2c
#define HCI_QOS_REJECTED			0x2d
#define HCI_CLASSIFICATION_NOT_SUPPORTED	0x2e
#define HCI_INSUFFICIENT_SECURITY		0x2f
#define HCI_PARAMETER_OUT_OF_RANGE		0x30
#define HCI_ROLE_SWITCH_PENDING			0x32
#define HCI_SLOT_VIOLATION			0x34
#define HCI_ROLE_SWITCH_FAILED			0x35
#define HCI_EIR_TOO_LARGE			0x36
#define HCI_SIMPLE_PAIRING_NOT_SUPPORTED	0x37
#define HCI_HOST_BUSY_PAIRING			0x38

/* ACL flags */
#define ACL_CONT		    0x01
#define ACL_START		    0x02
#define ACL_ACTIVE_BCAST	0x04
#define ACL_PICO_BCAST		0x08

/* Baseband links */
#define SCO_LINK	0x00
#define ACL_LINK	0x01
#define ESCO_LINK	0x02

/* -----  HCI Commands ----- */
/* Link Control */
#define OGF_LINK_CTL		    0x01
#define OCF_INQUIRY			    0x0001
#define OCF_INQUIRY_CANCEL		0x0002
#define OCF_PERIODIC_INQUIRY	0x0003
#define OCF_CREATE_PHY_LINK     0x0035
#define OCF_ACCEPT_PHY_LINK     0x0036
#define OCF_DISCONNECT_PHY_LINK 0x0037
#define OCF_CREATE_LOGICAL_LINK 0x0038
#define OCF_ACCEPT_LOGICAL_LINK 0x0039
#define OCF_DISCONNECT_LOGICAL_LINK 0x003A
#define OCF_LOGICAL_LINK_CANCEL 0x003B
#define OCF_FLOW_SPEC_MODIFY    0x003C

/* Link Policy */
#define OGF_LINK_POLICY		    0x02
#define OCF_HOLD_MODE			0x0001

/* Host Controller and Baseband */
#define OGF_HOST_CTL		    0x03
#define OCF_SET_EVENT_MASK		0x0001
#define OCF_RESET			    0x0003
#define OCF_SET_EVENT_FLT		0x0005
#define OCF_READ_LINK_SUP_TIMEOUT 0x0036
#define OCF_WRITE_LINK_SUP_TIMEOUT 0x0037
#define OCF_ENHANCED_FLUSH      0x005F
#define OCF_READ_LOGICAL_LINK_ACCEPT_TIMEOUT 0x0061
#define OCF_WRITE_LOGICAL_LINK_ACCEPT_TIMEOUT 0x0062
#define OCF_SHORT_RANGE_MODE    0x006B

/* Informational Parameters */
#define OGF_INFO_PARAM		    0x04
#define OCF_READ_LOCAL_VERSION	0x0001
#define OCF_READ_DATA_BLOCK_SIZE 0x000A

/* Status params */
#define OGF_STATUS_PARAM	    0x05
#define OCF_READ_FAILED_CONTACT_COUNTER		0x0001
#define OCF_READ_LINK_QUALITY   0x0003
#define OCF_READ_RSSI   0x0005
#define OCF_READ_LOCAL_AMP_INFO	0x0009
#define OCF_READ_LOCAL_AMP_ASSOC 0x000A
#define OCF_WRITE_REMOTE_AMP_ASSOC 0x000B

/* ---- HCI Events ---- */
#define EVT_INQUIRY_COMPLETE	0x01
#define EVT_INQUIRY_RESULT		0x02
#define EVT_CMD_COMPLETE        0x0E
#define EVT_CMD_STATUS          0x0F
#define EVT_ENHANCED_FLUSH_COMPLETE     0x39
#define EVT_PHY_LINK_COMPLETE   0x40
#define EVT_CHANNEL_SELECTED    0x41
#define EVT_DISCON_PHY_LINK_COMPLETE    0x42
#define EVT_PHY_LINK_LOSS_EARLY_WARN    0x43
#define EVT_LOGICAL_LINK_COMPLETE   0x45
#define EVT_DIS_LOGICAL_LINK_COMPLETE   0x46
#define FLOW_SPEC_MODIFY_COMPLETE   0x47
#define EVT_NUM_OF_COMPLETE_DATA_BLOCK  0x48
#define EVT_SHORT_RANGE_MODE_CHANGE_COMPLETE 0x4C
#define EVT_AMP_STATUS_CHANGE   0x4D

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/
/* hcidump packet */
typedef struct hcidump_hdr_ {
	UINT16	    len;
	UINT8		in;
	UINT8		pad;
	UINT32	    ts_sec;
	UINT32	    ts_usec;
}hcidump_hdr;
#define HCIDUMP_HDR_SIZE (sizeof(struct hcidump_hdr_))

/* BT SNOOP packet */
typedef struct btsnoop_pkt_ {
	UINT32      size;		/* Original Length */
	UINT32	    len;		/* Included Length */
	UINT32	    flags;		/* Packet Flags */
	UINT32	    drops;		/* Cumulative Drops */
	UINT32	    ts[2];		/* Timestamp microseconds */
	UINT8		data[0];	/* Packet Data */
}btsnoop_pkt;
#define BTSNOOP_PKT_SIZE (sizeof(struct btsnoop_pkt_))

/* --------  HCI Packet structures  -------- */
#define HCI_TYPE_LEN    1

typedef struct {	
    UINT16	    opcode;		/* OCF & OGF */	
    UINT8	    plen;
}hci_command_hdr;
#define HCI_COMMAND_HDR_SIZE    3

typedef struct {	
    UINT8		evt;	
    UINT8		plen;
}hci_event_hdr;
#define HCI_EVENT_HDR_SIZE  2

typedef struct {	
    UINT16	    handle;		/* Handle & Flags(PB, BC) */	
    UINT16	    dlen;
}hci_acl_hdr;
#define HCI_ACL_HDR_SIZE 	4

typedef struct {	
    UINT16	    handle;	
    UINT8		dlen;
}hci_sco_hdr;
#define HCI_SCO_HDR_SIZE    3

typedef struct {	
    UINT16	    device;	
    UINT16	    type;	
    UINT16	    plen;
}hci_msg_hdr;
#define HCI_MSG_HDR_SIZE    6

/*******************************************************************************
*                  F U N C T I O N   D E C L A R A T I O N S
********************************************************************************
*/
/* pal_hci_log_generator.c */
extern void pal_hci_log_hdlr_init( void );
extern INT32 pal_hci_dump_log_on( void );
extern void pal_hci_dump_log_off( void );
extern UINT32 pal_hci_dump_log_is_on( void );
extern UINT32 pal_hci_converter( UINT32 msg_type, UINT8* hci_raw, void* hci_packet );
extern void pal_hci_dump_log_generator( FILE* file_name, UINT8 fg_send, UINT8 pkt_type, UINT32 pkt_len, UINT8* hci_raw );
extern void pal_hci_snoop_log_generator( UINT8 fg_send, UINT8 pkt_type, UINT32 pkt_len, UINT8* hci_raw );

#endif /* _PAL_HCI_LOG_GENERATOR_H_ */

