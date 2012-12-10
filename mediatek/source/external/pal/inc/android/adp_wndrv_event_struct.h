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
 *  adp_wndrv_event_struct.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  WNDRV event structure for adaptation
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
#ifndef __ADP_WNDRV_EVENT_STRUCT_H__
#define __ADP_WNDRV_EVENT_STRUCT_H__

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
#include "adp_wndrv_typedef.h"
#include "adp_wndrv_cmd_struct.h"

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/
#define WNDRV_CHANNEL_BANDWIDTH_KHZ (5000)
#define WNDRV_CHANNEL_ONE_KHZ (2412000)
#define WNDRV_CHANNEL_FORTY_KHZ (2472000)

#define WNDRV_ETH_HEADER_SIZE (14)
#define WNDRV_LLC_HEADER_SIZE (8)

#define MAX_BOW_NUMBER_OF_CHANNEL (14)

/* Nelson(TODO): Need to confirm */
#define WNDRV_MAX_CHANNEL_NUM (50)

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/
/***************************************************************************
*  PRIMITIVE STRUCTURE
*     adp_wndrv_event_id
*
*  DESCRIPTION
*     WNDRV event id.
***************************************************************************/
typedef enum
{    
    BOW_EVENT_ID_COMMAND_STATUS = 1,
    BOW_EVENT_ID_MAC_STATUS,
    BOW_EVENT_ID_LINK_CONNECTED,
    BOW_EVENT_ID_LINK_DISCONNECTED,
    BOW_EVENT_ID_RSSI,
    BOW_EVENT_ID_LINK_QUALITY,
    BOW_EVENT_ID_SYNC_TSF
} adp_wndrv_event_id;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     AMPC_EVENT_HEADER_T
*
*  DESCRIPTION
*     AMP event header format struct.
***************************************************************************/
typedef struct _AMPC_EVENT_HEADER_T {
    UINT_8 ucEventId;
    UINT_8 ucSeqNumber;
    UINT_16 u2PayloadLength; 
} AMPC_EVENT_HEADER_T, *P_AMPC_EVENT_HEADER_T;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     AMPC_EVENT
*
*  DESCRIPTION
*     AMP event format struct.
***************************************************************************/
typedef struct _AMPC_EVENT {
    AMPC_EVENT_HEADER_T rHeader;
    UINT_8 aucPayload[0];
} AMPC_EVENT, *P_AMPC_EVENT;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_COMMAND_STATUS
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_COMMAND_STATUS {
    UINT_8  ucStatus;
    UINT_8  ucReserved[3];
} BOW_COMMAND_STATUS, *P_BOW_COMMAND_STATUS;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_MAC_STATUS
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
#if 0
typedef enum _ENUM_NETWORK_TYPE_T {
    NETWORK_TYPE_AIS = 0,
//    NETWORK_TYPE_P2P,
    NETWORK_TYPE_BOW,
}ENUM_NETWORK_TYPE_T, *P_ENUM_NETWORK_TYPE_T;

typedef struct _BOW_MAC_SUMMARY_T {
    UINT_8  ucNetworkInUse;
    UINT_8  ucNetworkType;
    UINT_8  aucMacAddr[6];
    UINT_32 u4FreqInKHz;
#if 0   /* TODO (Nelson): Add this element after w10.52 branch */
    UINT_8  ucNetworkAvailability;
    UINT_8  aucReserved[3];
#endif
} BOW_MAC_SUMMARY, *P_BOW_MAC_SUMMARY;

typedef struct _BOW_MAC_STATUS {
    BOW_MAC_SUMMARY rMacStatus[2];
} BOW_MAC_STATUS, *P_BOW_MAC_STATUS;
#endif

typedef enum _ENUM_BAND_T {   
    BAND_NULL,
    BAND_2G4,
    BAND_5G,
    BAND_NUM
    
} ENUM_BAND_T, *P_ENUM_BAND_T;

typedef struct _RF_CHANNEL_INFO_T {
    UINT_8  eBand;
    UINT_8  ucChannelNum;
} RF_CHANNEL_INFO_T, *P_RF_CHANNEL_INFO_T;

typedef struct _BOW_MAC_STATUS {   
    UINT_8  aucMacAddr[6];
    UINT_8  ucAvailability;
    UINT_8  ucNumOfChannel;   
    RF_CHANNEL_INFO_T   paucChannelList[WNDRV_MAX_CHANNEL_NUM];
} BOW_MAC_STATUS, *P_BOW_MAC_STATUS;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_LINK_CONNECTED
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_LINK_CONNECTED {
    UINT_8  ucSelectedChannel;
    UINT_8  aucReserved;
    UINT_8  aucPeerAddress[6];
} BOW_LINK_CONNECTED, *P_LINK_CONNECTED;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_LINK_DISCONNECTED
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_LINK_DISCONNECTED {
    UINT_8  ucReason;
    UINT_8  aucReserved;
    UINT_8  aucPeerAddress[6];
} BOW_LINK_DISCONNECTED, *P_LINK_DISCONNECTED;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_RSSI
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_RSSI {
    INT_8   cRssi;
    UINT_8  aucReserved[3];
} BOW_RSSI, *P_BOW_RSSI;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_LINK_QUALITY
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_LINK_QUALITY {
    UINT_8  ucLinkQuality;
    UINT_8  aucReserved[3];
} BOW_LINK_QUALITY, *P_BOW_LINK_QUALITY;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_SYNC_TSF
*
*  DESCRIPTION
*     Sync BOW TSF time with SCO time event.
***************************************************************************/
typedef struct _BOW_SYNC_TSF {
    UINT_32  u4TsfTimeLower;
    UINT_32  u4TsfTimeUpper;
    UINT_32  u4TsfSysTime;
    UINT_32  u4ScoTime;
    UINT_32  u4ScoSysTime;
} BOW_SYNC_TSF, *P_BOW_SYNC_TSF;

#if 0
/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_CHANNEL_LIST
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_CHANNEL_LIST {
    UINT_8 ucChannelListNum;
    UINT_8 ucReserved;
    CHANNEL_DESC arChannelList[1];
} BOW_CHANNEL_LIST, *P_BOW_CHANNEL_LIST;
#endif

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_RX_DATA
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef struct _BOW_RX_DATA {
    UINT_8 aucDestAddr[6];
    UINT_8 aucSrcAddr[6];    
    UINT_8 aucTypeLen[2];
    UINT_8 aucLLCHeader[8];
    UINT_8 aucFrameBody[0];
} BOW_RX_DATA, *P_BOW_RX_DATA;

#endif /* __ADP_WNDRV_EVENT_STRUCT_H__ */

