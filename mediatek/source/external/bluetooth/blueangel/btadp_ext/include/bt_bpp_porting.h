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
 * bt_bpp_porting.h
 *
 * Project:
 * --------
 * YuSu
 *
 * Description:
 * ------------
 * APIs
 *
 * Author:
 * -------
 * Paul Chuang
 *
 ****************************************************************************/

#ifndef __BT_BPP_PORTING_H__
#define __BT_BPP_PORTING_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "bt_bpp_porting_datatype.h"



typedef void (*BTMTK_BPP_JNI_CALLBACK)(U8 event, S8* parameters[], U8 count);
BT_BOOL btmtk_bpp_set_jni_callback(BTMTK_BPP_JNI_CALLBACK pfCallback, int apisock);

void btmtk_bpp_handle_message(ilm_struct* msg);


void btmtk_bpp_enable(void);
void btmtk_bpp_disable(void);
// GetPrinterAttributes :
// attr_bitmask: bitmask for attribute to be retrieve.
// see "bt_bpp_printer_attribute_bitmask" defination
void btmtk_bpp_getprinterattribute(T_BDADDR BdAddr, int bitmask_attr);
void btmtk_bpp_print(T_BDADDR BdAddr, T_BPP_OBJECT *BppObject);
void btmtk_bpp_auth_response(T_OBEX_AUTH *auth_reply);
void btmtk_bpp_disconnect(void);

//MBT_BOOL btmtk_bpp_is_dev_connected(T_MBT_BDADDR rem_bdaddr);
//MBT_BOOL btmtk_bpp_is_connected(void);



#ifdef __cplusplus
}
#endif
#endif //#ifndef __BT_BIP_PORTING_H__

