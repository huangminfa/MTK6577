/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2006
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
 *  custom_supl_config.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains SUPL customized function prototypes.
 *
 * Author:
 * -------
 *  Leo Hu
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
 * Dec 11 2009 mtk00563
 * [MAUI_02008938] [AGPS] It lack  "Both" item at Payload settings of GPS
 * 
 *
 * Nov 30 2009 mtk00563
 * [MAUI_01794946] [AGPS] it always popup error window when use AGPS on pioneer68
 * 
 *
 * Apr 24 2008 mtk00563
 * [MAUI_00665039] [AGPS]SUPL TLS Setting item will not show state first time
 * 
 *
 * Apr 22 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Apr 22 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 ============================================================================
 ****************************************************************************/

#ifndef __CUSTOM_SUPL_CONFIG_H__
#define __CUSTOM_SUPL_CONFIG_H__

#define SUPL_MAX_GPS_INT        30
#define SUPL_MAX_CONN_INT       60
#define SUPL_TIMER_VERIFY_VALUE 60

#define    SUPL_CUSTOM_USE_TLS_SI   1
#define    SUPL_CUSTOM_USE_TLS_NI   2
#define    SUPL_CUSTOM_USE_TLS_BOTH 3

typedef enum
{
    SUPL_CUSTOM_SESSION_SI,
    SUPL_CUSTOM_SESSION_NI,
    SUPL_CUSTOM_SESSION_SI_TRIG,
    SUPL_CUSTOM_SESSION_NI_TRIG,
    SUPL_CUSTOM_SESSION_END
} supl_custom_session_type_enum;

typedef enum
{
    SUPL_CUSTOM_POLICY_REJECT_NEW,
    SUPL_CUSTOM_POLICY_ACCEPT_AND_QUEUE_NEW,
    SUPL_CUSTOM_POLICY_ACCEPT_NEW_AND_ABORT_CURRENT,
    SUPL_CUSTOM_POLICY_END
} supl_custom_policy_enum;

#define SUPL_CUST_CAP_RRLP      0x0001
/* the following items should be used only when __UAGPS_UP_SUPPORT__ is defined */
#define SUPL_CUST_CAP_RRC       0x0002
#define SUPL_CUST_CAP_RRLP_RRC  (SUPL_CUST_CAP_RRLP | SUPL_CUST_CAP_RRC)

extern kal_uint32 supl_custom_get_multi_session_policy(supl_custom_session_type_enum current, supl_custom_session_type_enum new);
extern kal_uint16 supl_custom_get_gps_interval(void);
extern kal_uint16 supl_custom_get_conn_interval(void);
extern kal_bool supl_custom_NI_use_tls(void);
extern kal_bool supl_custom_SI_use_tls(void);
extern void supl_custom_set_SI_use_tls(kal_bool enable);
extern void supl_custom_set_NI_use_tls(kal_bool enable);
extern void supl_custom_set_capability(kal_uint16 cap);
extern kal_uint16 supl_custom_get_capability(void);

#endif

