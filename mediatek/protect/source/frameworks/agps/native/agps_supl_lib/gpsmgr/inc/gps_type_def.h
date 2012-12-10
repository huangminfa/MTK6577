/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef __GPS_TYPE_DEF_H__
#define __GPS_TYPE_DEF_H__

#ifdef __AGPS_SWIP_REL__

#include "typedef.h"

#define MDI_RES_GPS_UART_SUCCEED                            0
#define MDI_RES_GPS_UART_READY_TO_WRITE                     1
#define MDI_RES_GPS_UART_ERR_PORT_ALREADY_OPEN              -7001
#define MDI_RES_GPS_UART_ERR_PORT_NUMBER_WRONG              -7002
#define MDI_RES_GPS_UART_ERR_PARAM_ERROR                    -7003
#define MDI_RES_GPS_UART_ERR_PORT_ALREADY_CLOSED            -7004
#define MDI_RES_GPS_UART_ERR_PORT_ERR_UNKNOW                -7005
#define MDI_RES_GPS_UART_ERR_PORT_ERR_NOT_OPEN              -7006
#define MDI_RES_GPS_UART_ERR_NO_SLOT                        -7007
#define FS_READ_ONLY             0x00000100L

typedef U8*         PU8;
typedef S8*         PS8;
typedef void (*FuncPtr) (void);

//<< from maui/mcu/plutommi/Framework/Interface/MMIDataType.h
#define MMI_RET_OK          (0)         /* the return value of mmi_ret */
#define MMI_RET_DONT_CARE   (-499)      /* the return value of mmi_ret */
#define MMI_RET_ERR         (-500)      /* the return value of mmi_ret */

#define MMI_EVT_PARAM_HEADER    \
    U16 evt_id;                 \
    U16 size;                   \
    void *user_data;

#define mmi_id              MMI_ID
#define mmi_ret             MMI_RET

typedef U16                 MMI_ID;         /* app id, group id, screen id */

typedef S32                 mmi_ret;

#ifndef __BUILD_DOM__
    typedef struct _mmi_event_struct
    {
        MMI_EVT_PARAM_HEADER
    }mmi_event_struct;
#endif /* __BUILD_DOM__ */

typedef U8(*PsIntFuncPtr) (void *);

typedef enum
{
    MMI_SIM_NONE = 0, /* No bit-wise operation for none case */
    MMI_SIM1 = 0x0001,
    MMI_SIM2 = 0x0002,
    MMI_SIM3 = 0x0004,
    MMI_SIM4 = 0x0008,
    MMI_SIM_END_OF_ENUM
}mmi_sim_enum;

typedef enum
{
    MMI_NETWORK_NONE = 0, /* No bit-wise operation for none case */
    MMI_GSM = 0x0100,
    MMI_WCDMA = 0x0200,
    MMI_NETWORK_END_OF_ENUM
} mmi_network_enum;
//>>

//<< from mcu/plutommi/MMI/MiscFramework/MiscFrameworkInc/SimDetectionDef.h
#define MAX_PLMN_LEN_MMI            6
//>>

//<< from mcu/plutommi/plutommi/framework/gui/gui_inc/Gui_data_types.h
typedef	U16					UI_character_type;
typedef UI_character_type*	UI_string_type;
//>>

#endif /* __AGPS_SWIP_REL__ */

#endif /* __GPS_TYPE_DEF_H__ */
