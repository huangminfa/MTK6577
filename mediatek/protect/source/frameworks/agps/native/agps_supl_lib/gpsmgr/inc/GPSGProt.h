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
 *  GPSGProt.h
 *
 * Project:
 * --------
 *  
 *
 * Description:
 * ------------
 *  GPS Exported APIs
 *
 * Author:
 * -------
 * Sha Tao
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * May 23 2008 mbj06018
 * [MAUI_01046026] [GIS_Sunavi]Check in BT GPS support
 * 
 *
 * May 5 2008 mbj06078
 * [MAUI_00757780] [AGPS]Revise notify timer machanism
 * 
 *
 * Apr 21 2008 mbj06078
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Apr 20 2008 mbj06078
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Mar 18 2008 mbj06078
 * [MAUI_00637624] [AGPS] Check in W08.12
 * 
 *
 * Feb 25 2008 mbj06078
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#if defined(__GPS_SUPPORT__) || defined(__BT_GPS_SUPPORT__)

#ifndef _GPSGPROT_H_
#define _GPSGPROT_H_

#include "supl2mmi_struct.h"
#include "mdi_gps.h"
#include "typedef.h"

#if defined(__AGPS_SUPPORT__) && defined(__SUPL_SUPPORT__)
#if !defined(__AGPS_SWIP_REL__)
#define MMI_GPS_MGR_NOTIFY_DELAY  40000
#define MMI_GPS_MGR_COMFIRM_DELAY  40000
#endif

typedef enum
{
    MMI_GPS_WORK_MODE_MA,
    MMI_GPS_WORK_MODE_MB,
    MMI_GPS_WORK_MODE_NORMAL,
    MMI_GPS_WORK_NO_WORK,
    MMI_GPS_WORK_MODE_END
} mmi_gps_work_mode_enum;
#endif

typedef enum
{
    MMI_GPS_SETTING_PREFER_SIM_NO1 = 0,
    MMI_GPS_SETTING_PREFER_SIM_NO2,
    MMI_GPS_SETTING_PREFER_SIM_NO3,
    MMI_GPS_SETTING_PREFER_SIM_NO4,
    MMI_GPS_SETTING_PREFER_SIM_NO5,
    MMI_GPS_SETTING_PERFER_SIM_END,
    MMI_GPS_SETTING_PREFER_SIM_ALWAYS_ASK
} mmi_gps_setting_prefer_sim_enum;

typedef enum
{
    MMI_GPS_RECEIVER_YES = 0,
    MMI_GPS_RECEIVER_NO,
    MMI_GPS_RECEIVER_EXTERNAL
} mmi_gps_receiver_enum;

//<< from maui 2011/02/24:  maui/mcu/plutommi/mtkapp/gps/gpsinc/GPSGProt.h
typedef enum
{
    MMI_GPS_SRV_NOTIFY_TYPE_POPUP = 0,
    MMI_GPS_SRV_NOTIFY_TYPE_CONFIRM,
    MMI_GPS_SRV_NOTIFY_TYPE_CLOSE_UI,
    MMI_GPS_SRV_NOTIFY_TYPE_END
} mmi_gps_srv_notify_type_enum;

typedef struct
{
    mmi_gps_srv_notify_type_enum type;
    U32 id;
    WCHAR *notify_p;
} mmi_gps_srv_notify_type_struct;

typedef void (*mmi_gps_setting_dataaccount_cb)(U32 dataacout,S32 err_code, void *user_data);
typedef void (*srv_gps_notify_callback)(mmi_gps_srv_notify_type_enum type, mmi_gps_srv_notify_type_struct *notify, void *user_data);
//>>

#ifndef __AGPS_SWIP_REL__
extern void InitGPSSetting(void);
#endif

#ifndef __AGPS_SWIP_REL__
    extern mmi_ret mmi_gps_mgr_init(mmi_event_struct *evt);
#else
    extern void    mmi_gps_mgr_init(void);
#endif

#if defined(__AGPS_SUPPORT__) && defined(__SUPL_SUPPORT__)
#ifndef __AGPS_SWIP_REL__
extern void mmi_gps_mgr_da_file(S32 session_id, S32 mime_type, S32 mime_subtype, S32 action, PU16 file_path, PS8 url, PS8 mime_type_string);
#else
extern void mmi_gps_mgr_ni_data_hdlr(void *data, U32 len);
#endif

extern S32 mmi_gps_mgr_close_gps(
    U16 port,
    mdi_gps_uart_work_mode_enum mode, 
    void (*gps_callback)(mdi_gps_parser_info_enum type, void *buffer, U32 length),
    MMI_BOOL is_re_aid);
extern S32 mmi_gps_mgr_open_gps(
    U16 port,
    mdi_gps_uart_work_mode_enum gps_mode,
    mmi_gps_work_mode_enum work_mode,
    supl_mmi_qop_struct *qop,
    void (*gps_callback)(mdi_gps_parser_info_enum type, void *buffer, U32 length),
    MMI_BOOL is_re_aid);
//!!
void mmi_gps_mgr_send_all_assist_ind_hdlr();
//
#endif 

extern S32 mmi_gis_gps_get_port_number(void);
extern S32 mmi_gps_get_port_number(void);
extern U8 mmi_gps_get_receiver_setting(void);

#ifdef __AGPS_SWIP_REL__
extern void mmi_gps_mgr_call_end(void);
extern void mmi_gps_mgr_supl_mmi_status_ind_hdlr(void *msg);
extern void mmi_gps_mgr_notify_send_rsp(supl_mmi_result_enum result, U16 index);
#endif

#if defined(__MMI_SPP_SUPPORT__) && defined(__BT_GPS_SUPPORT__)
extern MMI_BOOL mmi_gps_setting_close_spp_conn(void *param);
extern void* mmi_gps_setting_register_conn_callback(void (*callback)(void));
extern void mmi_gps_setting_conn_bt_gps_device(void);
#endif

//<< from maui 2011/02/24:  maui/mcu/plutommi/mtkapp/gps/gpsinc/GPSGProt.h
MMI_BOOL mmi_gps_setting_get_is_agps_on(U8 sim);         // Please use ALPS's mmi_gps_mgr_adp_can_use_agps() instead
#if 0
MMI_BOOL mmi_gps_setting_get_data_account_settings(U8 sim,U32 *data_account, U32 *o_port, U16 *o_addr);
#endif
S32 mmi_gps_setting_get_sim(void);

/*****************************************************************************
* FUNCTION
*  mmi_gps_setting_get_time_sync
* DESCRIPTION
*  save GPS setting and (mmi_void_funcptr_type)mmi_frm_scrn_close_active_id
* PARAMETERS
*  void
* RETURNS           
*  void
*****************************************************************************/
extern MMI_BOOL mmi_gps_setting_get_time_sync(void);
//>>

#endif /* _GPSGPROT_H_ */

#endif /* __GPS_SUPPORT__ || __BT_GPS_SUPPORT__ */

