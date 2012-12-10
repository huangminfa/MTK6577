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
 *  adp_wndrv_cmd_struct.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  WNDRV commnad structure for adaptation
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
#ifndef __ADP_WNDRV_CMD_STRUCT_H__
#define __ADP_WNDRV_CMD_STRUCT_H__

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
#include "adp_wndrv_typedef.h"

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/
#define BOW_MIN_PWR_DBM (-30)
#define BOW_MAX_PWR_DBM (30)

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/
/***************************************************************************
*  PRIMITIVE STRUCTURE
*     adp_wndrv_cmd_id
*
*  DESCRIPTION
*     WNDRV command id.
***************************************************************************/
typedef enum
{    
    BOW_CMD_ID_GET_MAC_STATUS = 1,
    BOW_CMD_ID_SETUP_CONNECTION,
    BOW_CMD_ID_DESTROY_CONNECTION,
    BOW_CMD_ID_SET_PTK,
    BOW_CMD_ID_READ_RSSI,
    BOW_CMD_ID_READ_LINK_QUALITY,
    BOW_CMD_ID_SHORT_RANGE_MODE,
    BOW_CMD_ID_SET_ACTIVITY_REPORT
} adp_wndrv_cmd_id;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     AMPC_COMMAND_HEADER_T
*
*  DESCRIPTION
*     AMP command header.
***************************************************************************/
typedef struct _AMPC_COMMAND_HEADER_T 
{ 
    UINT_8 ucCommandId; 
    UINT_8 ucSeqNumber; 
    UINT_16 u2PayloadLength;
} AMPC_COMMAND_HEADER_T, *P_AMPC_COMMAND_HEADER_T;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     AMPC_COMMAND
*
*  DESCRIPTION
*     AMP command format struct.
***************************************************************************/
typedef struct _AMPC_COMMAND
{
    AMPC_COMMAND_HEADER_T rHeader;
    UINT_8 aucPayload[0];
} AMPC_COMMAND, *P_AMPC_COMMAND;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_GET_MAC_STATUS
*
*  DESCRIPTION
*     Get MAC status.
***************************************************************************/
typedef struct _BOW_GET_MAC_STATUS
{
} BOW_GET_MAC_STATUS;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_CMD_ID_SETUP_CONNECTION
*
*  DESCRIPTION
*     Setup physical link connection.
***************************************************************************/
#if 0
typedef struct _CHANNEL_DESC_T { 
    UINT_8 ucChannelNum;
    UINT_8 ucChannelBand;
} CHANNEL_DESC, P_CHANNEL_DESC;

typedef struct _BOW_SETUP_CONNECTION {
    UINT_8 aucPeerAddress[6];
    UINT_16 u2BeaconInterval;
    UINT_8 ucTimeoutDiscovery;
    UINT_8 ucTimeoutInactivity;
    UINT_8 ucRole;
    UINT_8 ucPAL_Capabilities;
    INT_8 cMaxTxPower;
    UINT_8 ucChannelListNum;
    CHANNEL_DESC arChannelList[1];
} BOW_SETUP_CONNECTION, *P_BOW_SETUP_CONNECTION;
#endif

typedef struct _BOW_SETUP_CONNECTION {
    UINT_8 ucChannelNum;
    UINT_8 ucReserved1;
    UINT_8 aucPeerAddress[6];
    UINT_16 u2BeaconInterval;
    UINT_8 ucTimeoutDiscovery;
    UINT_8 ucTimeoutInactivity;
    UINT_8 ucRole;
    UINT_8 ucPAL_Capabilities;
    INT_8 cMaxTxPower;
    UINT_8 ucReserved2;
} BOW_SETUP_CONNECTION, *P_BOW_SETUP_CONNECTION;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_DESTROY_CONNECTION
*
*  DESCRIPTION
*     Destroy physical link connection.
***************************************************************************/
typedef struct _BOW_DESTROY_CONNECTION {
    UINT_8  aucPeerAddress[6];
    UINT_8  aucReserved[2];
} BOW_DESTROY_CONNECTION, *P_BOW_DESTROY_CONNECTION;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_SET_PTK
*
*  DESCRIPTION
*     Set and install PTK.
***************************************************************************/
typedef struct _BOW_SET_PTK {
    UINT_8  aucPeerAddress[6];
    UINT_8  aucReserved[2];
    UINT_8  aucTemporalKey[16];
} BOW_SET_PTK, *P_BOW_SET_PTK;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_READ_RSSI
*
*  DESCRIPTION
*     Read RSSI.
***************************************************************************/
typedef struct _BOW_READ_SSI {
    UINT_8  aucPeerAddress[6];
    UINT_8  aucReserved[2];
} BOW_READ_RSSI, *P_BOW_READ_RSSI;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_READ_LINK_QUALITY
*
*  DESCRIPTION
*     Read link quality.
***************************************************************************/
typedef struct _BOW_READ_LINK_QUALITY {
    UINT_8  aucPeerAddress[6];
    UINT_8  aucReserved[2];
} BOW_READ_LINK_QUALITY, *P_BOW_READ_LINK_QUALITY;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_SHORT_RANGE_MODE
*
*  DESCRIPTION
*     Request to enter short range mode.
***************************************************************************/
typedef struct _BOW_SHORT_RANGE_MODE {
    UINT_8  aucPeerAddress[6];
    INT_8   cTxPower;
    UINT_8  ucReserved;
} BOW_SHORT_RANGE_MODE, *P_BOW_SHORT_RANGE_MODE;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_SET_ACTIVITY_REPORT
*
*  DESCRIPTION
*     Used to set Activity report.
***************************************************************************/
typedef struct _BOW_SET_ACTIVITY_REPORT {
    UINT_8  aucPeerAddress[6];
    UINT_8  ucScheduleKnown;
    UINT_8  ucNumReports;
    UINT_32  u4StartTime;
    UINT_32  u4Duration;
    UINT_32  u4Periodicity;
} BOW_SET_ACTIVITY_REPORT, *P_BOW_SET_ACTIVITY_REPORT;

/***************************************************************************
*  PRIMITIVE STRUCTURE
*     BOW_TX_DATA
*
*  DESCRIPTION
*     BOW Tx packet format.
***************************************************************************/
typedef struct _BOW_TX_DATA_HEADER_T {    
    UINT_8 aucDestAddr[6];
    UINT_8 aucSrcAddr[6];
    UINT_8 aucTypeLen[2];
    UINT_8 aucLLCHeader[8];
} BOW_TX_DATA_HEADER_T, *P_BOW_TX_DATA_HEADER_T;

typedef struct _BOW_TX_DATA {
    BOW_TX_DATA_HEADER_T rDataHeader;
    UINT_8 aucFrameBody[0];
} BOW_TX_DATA, *P_BOW_TX_DATA;

#endif /* __ADP_WNDRV_CMD_STRUCT_H__ */

