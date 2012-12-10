/*******************************************************************************
* Copyright (c) 2007 MediaTek Inc.
*
* All rights reserved. Copying, compilation, modification, distribution
* or any other use whatsoever of this material is strictly prohibited
* except in accordance with a Software License Agreement with
* MediaTek Inc.
********************************************************************************
*/

/*******************************************************************************
* LEGAL DISCLAIMER
*
* BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND
* AGREES THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK
* SOFTWARE") RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE
* PROVIDED TO BUYER ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY
* DISCLAIMS ANY AND ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT
* LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
* PARTICULAR PURPOSE OR NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE
* ANY WARRANTY WHATSOEVER WITH RESPECT TO THE SOFTWARE OF ANY THIRD PARTY
* WHICH MAY BE USED BY, INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK
* SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY
* WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE
* FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S SPECIFICATION OR TO
* CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
* BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
* LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL
* BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT
* ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY
* BUYER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
* THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
* WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT
* OF LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING
* THEREOF AND RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN
* FRANCISCO, CA, UNDER THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE
* (ICC).
********************************************************************************
*/

/*******************************************************************************
 * Filename:
 * ---------
 *  pal_adp_sap.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL SAP definition for adaptation layer
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

#ifndef _PAL_ADP_SAP_H
#define _PAL_ADP_SAP_H

/*******************************************************************************
*                         C O M P I L E R   F L A G S
********************************************************************************
*/

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/
#if 0
//#ifndef _PAL_MAUI_

/* PAL SAP enum */
typedef enum
{
    /****************************************************************************
     *  Messages: WNDRV -> PAL
     ****************************************************************************/
    MSG_ID_PAL_MSG_CODE_BEGIN = 0,
    MSG_ID_PAL_WNDRV_MAC_START_IND = MSG_ID_PAL_MSG_CODE_BEGIN,
    MSG_ID_PAL_WNDRV_MAC_CONNECT_IND,
    MSG_ID_PAL_WNDRV_MAC_CONNECT_FAIL_IND,
    MSG_ID_PAL_WNDRV_MAC_DISCONNECT_IND,
    MSG_ID_PAL_WNDRV_MAC_CANCEL_IND,
    MSG_ID_PAL_WNDRV_QUERY_STATUS_IND,
    /* For Data Link Manager */
    MSG_ID_PAL_WNDRV_DATA_IND,
    MSG_ID_PAL_WNDRV_DATA_BLOCK_FREE_NUM_IND,
    
    /****************************************************************************
     *  Messages: BT -> PAL
     ****************************************************************************/
    /* For PAL Manager */
    MSG_ID_PAL_BT_START,
    MSG_ID_PAL_BT_READ_LOCAL_VERSION_INFO_COMMAND = MSG_ID_PAL_BT_START,
    MSG_ID_PAL_BT_READ_LOCAL_AMP_INFO_COMMAND,
    MSG_ID_PAL_BT_READ_LOCAL_AMP_ASSOC_COMMAND,
    MSG_ID_PAL_BT_RESET_COMMAND,
    MSG_ID_PAL_BT_READ_DATA_BLOCK_SIZE_COMMAND,
    MSG_ID_PAL_BT_SET_EVENT_MASK_PAGE2_COMMAND,     // new add (2010/05/18)
    /* For Physical Link Manager */
    MSG_ID_PAL_BT_READ_LINK_QUALITY_COMMAND,
    MSG_ID_PAL_BT_READ_RSSI_COMMAND,
    MSG_ID_PAL_BT_WRITE_REMOTE_AMP_ASSOC_COMMAND,
    MSG_ID_PAL_BT_CREATE_PHYSICAL_LINK_COMMAND,
    MSG_ID_PAL_BT_ACCEPT_PHYSICAL_LINK_COMMAND,
    MSG_ID_PAL_BT_READ_LINK_SUPERVISON_TIMEOUT_COMMAND,
    MSG_ID_PAL_BT_DISCONNECT_PHYSICAL_LINK_COMMAND,
    MSG_ID_PAL_BT_WRITE_LINK_SUPERVISON_TIMEOUT_COMMAND,
    /* For Logical Link Manager */
    MSG_ID_PAL_BT_CREATE_LOGICAL_LINK_COMMAND,
    MSG_ID_PAL_BT_ACCEPT_LOGICAL_LINK_COMMAND,
    MSG_ID_PAL_BT_FLOW_SPEC_MODIFY_COMMAND,
    MSG_ID_PAL_BT_DISCONNECT_LOGICAL_LINK_COMMAND,
    MSG_ID_PAL_BT_LOGICAL_LINK_CANCEL_COMMAND,
    MSG_ID_PAL_BT_READ_LOGICAL_LINK_ACCEPT_TIMEOUT_COMMAND,
    MSG_ID_PAL_BT_WRITE_LOGICAL_LINK_ACCEPT_TIMEOUT_COMMAND,
    /* For Data Link Manager */
    MSG_ID_PAL_BT_ENHANCED_FLUSH_COMMAND,           // new add (2010/05/18)
    MSG_ID_PAL_BT_DATA_COMMAND
} pal_sap_enum;


/* PAL module type enum */
typedef enum
{
    MOD_PAL = 0,
    MOD_BT,
    MOD_WNDRV
} pal_mod_type_enum;


/* PAL SAP type enum */
typedef enum
{
    PAL_WNDRV_SAP = 0,
    PAL_BT_SAP
} pal_sap_type_enum;

#endif

/*******************************************************************************
*                            P U B L I C   D A T A
********************************************************************************
*/

/*******************************************************************************
*                           P R I V A T E   D A T A
********************************************************************************
*/

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/

/*******************************************************************************
*                  F U N C T I O N   D E C L A R A T I O N S
********************************************************************************
*/

/*******************************************************************************
*                              F U N C T I O N S
********************************************************************************
*/
#endif /* _PAL_ADP_SAP_H */

