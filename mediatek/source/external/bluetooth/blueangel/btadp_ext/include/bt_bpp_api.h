/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
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
/*****************************************************************************
 *
 * Filename:
 * ---------
 * bt_bpp_api.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   API
 *
 * Author:
 * -------
 *   Yufeng chu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision: #1 $
 * $Modtime: $
 * $Log: $
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __BT_BPP_API_H__
#define __BT_BPP_API_H__

#include "bluetooth_bpp_common.h"
#include "MBTDataType.h"
#include "MBTType.h"

//#include "bt_bpp_struct.h"

/* register callback */
//BtStatus btmtk_bpp_register_callback(BtmtkBppCallback app_callback);

/* APIs */

MBT_VOID btmtk_bpp_enable(MBT_VOID);
MBT_VOID btmtk_bpp_disable (MBT_VOID);

/* connect */
//BtStatus btmtk_bpp_connect(BD_ADDR btAddr);

/* GetPrinterAttributes :
 * attr_bitmask: bitmask for attribute to be retrieve.
 * see "bt_bpp_printer_attribute_bitmask" defination	 
 */
MBT_VOID btmtk_bpp_getprinterattribute(T_MBT_BDADDR BdAddr, MBT_INT bitmask_attr);

/* print object */
MBT_VOID btmtk_bpp_print(T_MBT_BDADDR BdAddr, T_MBT_BPP_OBJECT *MBTObject);

/* authentication response */
MBT_VOID btmtk_bpp_auth_response(T_MBT_OBEX_AUTH *auth_reply);

/* disconnect */
MBT_VOID btmtk_bpp_disconnect(MBT_VOID);

MBT_BOOL btmtk_bpp_is_dev_connected(T_MBT_BDADDR rem_bdaddr);
MBT_BOOL btmtk_bpp_is_connected(void);


#endif //#ifndef __BT_BPP_API_H__

