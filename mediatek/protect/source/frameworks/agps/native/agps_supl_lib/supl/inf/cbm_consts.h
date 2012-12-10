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

/*****************************************************************************
*
* Filename:
* ---------
* cbm_consts.h
*
* Project:
* --------
*   MAUI
*
* Description:
* ------------
*   This file contains the CBM enums and constants
*
* Author:
* -------
* Leona Chou
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
 * Apr 14 2010 mtk01890
 * [MAUI_02395332] [CBM] check in all 10A changes
 * 
* 
*
*------------------------------------------------------------------------------
* Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
*============================================================================
****************************************************************************/
#ifndef _CBM_CONSTS_H
#define _CBM_CONSTS_H


/*************************************************************************
* Constant values
*************************************************************************/
/* all network account id */
#define CBM_ALL_NWK_ACCT_ID     (0x3f)
/* invalid network account id */
#define CBM_INVALID_NWK_ACCT_ID (0x3e)
/* dcd network account id */
#define CBM_DCD_ACCT_ID	(0x3d)
/* external modem network account id */
#define CBM_EXT_MODEM_ACCT_ID	(0x3c)
/* SATCE csd network account id */
#define CBM_SAT_CSD_ACCT_ID	(0x3b)
/* SATCE gprs network account id */
#define CBM_SAT_PS_ACCT_ID (0x3a)
/* loopback network account id */
#define CBM_LOOPBACK_ACCT_ID	(0x39)
/* wifi account id */
#define CBM_WIFI_ACCT_ID    (0x38)
/* default network account id. 
 * when application use this network account id,
 * smart setting will be applied.
 */
#define CBM_DEFAULT_ACCT_ID		(0x37)


/* invalid module id */
#define CBM_INVALID_MOD_ID  (0)
/* invalid application id */
#define CBM_INVALID_APP_ID  (0)

/* max applicaiton id */
#define CBM_MAX_APP_ID          (0xff)
/* max account number in 32-bits account id */
#define CBM_MAX_ACCT_NUM        (2)
/* all qos id */
#define CBM_ALL_QOS_ID          (0xff)


/*************************************************************************
* Enum values
*************************************************************************/

/* sim id */
typedef enum
{
    CBM_SIM_ID_SIM1, /* sim card one */
    CBM_SIM_ID_SIM2, /* sim card two */
    CBM_SIM_ID_TOTAL /* total sim card number */
} cbm_sim_id_enum;

/* Bearer state */
typedef enum
{
    CBM_DEACTIVATED             = 0x01, /* deactivated */
    CBM_ACTIVATING              = 0x02, /* activating */
    CBM_ACTIVATED               = 0x04, /* activated */
    CBM_DEACTIVATING            = 0x08, /* deactivating */
    CBM_CSD_AUTO_DISC_TIMEOUT   = 0x10, /* csd auto disconnection timeout */
    CBM_GPRS_AUTO_DISC_TIMEOUT  = 0x20, /* gprs auto disconnection timeout */
    CBM_NWK_NEG_QOS_MODIFY      = 0x040, /* negotiated network qos modify notification */
    CBM_BEARER_STATE_TOTAL
} cbm_bearer_state_enum;

typedef enum
{
    /* no bearer event */
    CBM_APP_BEARER_EVT_NONE = 0,
    /* always ask event */
    CBM_APP_BEARER_EVT_ALWAYS_ASK = 0x01,
    /* bearer fallback level one event */
    CBM_APP_BEARER_EVT_FB_L1       = 0x02, 
    /* bearer fallback level one with new connection event */
    CBM_APP_BEARER_EVT_FB_L1_WITH_NEW_CONN           = 0x04, 
    /* support non-auto bearer fallback level two with new connection */
    CBM_APP_BEARER_EVT_FB_L2_WITH_NEW_CONN   = 0x08, 
    CBM_APP_BEARER_EVT_TOTAL
} cbm_app_bearer_event_enum;

/* bearer type */
typedef enum
{
    CBM_BEARER_NONE, /* no valid bearer */
    CBM_CSD = 0x01, /* CSD bearer */
    CBM_BEARER_GSM_CSD = CBM_CSD, /* CSD bearer */
    CBM_GPRS = 0x02, /* GPRS bearer */
    CBM_BEARER_GSM_GPRS = CBM_GPRS, /* GPRS bearer */
    CBM_EDGE = 0x04, /* EDGE bearer */
    CBM_BEARER_EDGE = CBM_EDGE, /* EDGE bearer */
    
    CBM_WIFI = 0x08, /* WIFI bearer */
    CBM_BEARER_WIFI = CBM_WIFI, /* WIFI bearer */
    CBM_LOOPBACK = 0x10, /* loopback */
    CBM_BEARER_LOOPBACK = CBM_LOOPBACK, /* loopback */

    CBM_UMTS = 0x20, /* 3G UMTS (WCDMA) bearer */
    CBM_BEARER_UMTS = CBM_UMTS, /* 3G UMTS (WCDMA) bearer */


    CBM_3G = CBM_UMTS, /* Not real activated bearer type. This is 3G general name.
                          It contains all 3G bearer types, such as WCDMA, STDWCDMA, etc. */
    CBM_BEARER_3G = CBM_UMTS, /* Not real activated bearer type. This is 3G general name.
                                 It contains all 3G bearer types, such as WCDMA, STDWCDMA, etc. */

    CBM_PS  = CBM_GPRS | CBM_EDGE | CBM_BEARER_3G, /* Not real activated bearer type. This is packet service general name.
                                                      It contains all packet service types, such as GPRS, EDGE, UMTS, etc. */
    CBM_BEARER_PS = CBM_PS, /* Not real activated bearer type. This is packet service general name.
                               It contains all packet service types, such as GPRS, EDGE, UMTS, etc. */
                               
    CBM_BEARER_TOTAL = 0xff
} cbm_bearer_enum;

/* error cause */
typedef enum
{
    CBM_OK                  = 0,  /* success */
    CBM_ERROR               = -1, /* error */
    CBM_WOULDBLOCK          = -2, /* would block */
    CBM_LIMIT_RESOURCE      = -3, /* limited resource */
    CBM_INVALID_ACCT_ID     = -4, /* invalid account id*/
    CBM_INVALID_AP_ID       = -5, /* invalid application id*/
    CBM_INVALID_SIM_ID      = -6, /* invalid SIM id */
    CBM_BEARER_FAIL         = -7, /* bearer fail */
    CBM_DHCP_ERROR          = -8, /* DHCP get IP error */
    CBM_CANCEL_ACT_BEARER   = -9, /* cancel the account query screen, such as always ask or bearer fallback screen */
    CBM_DISC_BY_CM          = -10 /* bearer is deactivated by the connection management */
} cbm_result_error_enum;


/* DOM-NOT_FOR_SDK-BEGIN */
/* Define Max application numbers */
#ifdef __IPSEC__
#define ABM_IPSEC_APP_NUM   1
#else
#define ABM_IPSEC_APP_NUM   0
#endif

#ifdef WAP_SUPPORT
#define ABM_WAP_APP_NUM     1
#else
#define ABM_WAP_APP_NUM     0
#endif

#ifdef STREAM_SUPPORT
#define ABM_STREAM_APP_NUM  1
#else
#define ABM_STREAM_APP_NUM  0
#endif

#ifdef __J2ME__
#define ABM_JAVA_APP_NUM    1
#else
#define ABM_JAVA_APP_NUM    0
#endif

#ifdef __EMAIL__
#define ABM_EMAIL_APP_NUM   1
#else
#define ABM_EMAIL_APP_NUM   0
#endif

#ifdef __VOIP__
#define ABM_VOIP_APP_NUM    1
#else
#define ABM_VOIP_APP_NUM    0
#endif

#ifdef __SYNCML_SUPPORT__
#define ABM_SYNCML_APP_NUM    1
#else
#define ABM_SYNCML_APP_NUM    0
#endif

#ifdef MMS_SUPPORT
#define ABM_MMS_APP_NUM     1
#else
#define ABM_MMS_APP_NUM     0
#endif

#ifdef __WIFI_SUPPORT__
#define ABM_WIFI_APP_NUM     1
#else
#define ABM_WIFI_APP_NUM     0
#endif

#ifdef __SATCE__
#define ABM_SAT_APP_NUM     1
#else
#define ABM_SAT_APP_NUM     0
#endif

#ifdef __SUPL_SUPPORT__
#define ABM_SUPL_APP_NUM    1
#else
#define ABM_SUPL_APP_NUM    0
#endif

#ifdef __SSL_SUPPORT__
#define ABM_SSL_APP_NUM    1
#else
#define ABM_SSL_APP_NUM    0
#endif
/* DOM-NOT_FOR_SDK-END */

/* couting total application numbers which are supported to use network service */
typedef enum
{
    ABM_APP_NUM_SPARED = 8,                                  /*  Spared app number: 8 */
    ABM_IPSEC_APP_NUM_END = ABM_APP_NUM_SPARED + ABM_IPSEC_APP_NUM,   /*  IPSEC app number: 8+1 */
    ABM_WAP_APP_NUM_END = ABM_IPSEC_APP_NUM_END + ABM_WAP_APP_NUM,     /*  WAP app number: 9+1 */
    ABM_STREAM_APP_NUM_END = ABM_WAP_APP_NUM_END + ABM_STREAM_APP_NUM,      /*  Stream app number: 10+1 */
    ABM_JAVA_APP_NUM_END = ABM_STREAM_APP_NUM_END+ ABM_JAVA_APP_NUM,/*  Java app number: 11+1 */
    ABM_EMAIL_APP_NUM_END = ABM_JAVA_APP_NUM_END + ABM_EMAIL_APP_NUM,   /*  EMAIL app number: 12+1 */
    ABM_VOIP_APP_NUM_END = ABM_EMAIL_APP_NUM_END + ABM_VOIP_APP_NUM,  /*  VOIP app number: 13+1 */
    ABM_SYNCML_APP_NUM_END = ABM_VOIP_APP_NUM_END + ABM_SYNCML_APP_NUM,  /*  SYNCML app number: 14+1 */
    ABM_MMS_APP_NUM_END = ABM_SYNCML_APP_NUM_END + ABM_MMS_APP_NUM,  /*  MMS app number: 15+1 */
    ABM_WIFI_APP_NUM_END = ABM_MMS_APP_NUM_END + ABM_WIFI_APP_NUM,  /*  WIFI app number: 16+1 */
    ABM_SAT_APP_NUM_END = ABM_WIFI_APP_NUM_END + ABM_SAT_APP_NUM,  /*  WIFI app number: 17+1 */
    ABM_SUPL_APP_NUM_END = ABM_SAT_APP_NUM_END + ABM_SUPL_APP_NUM,  /*  WIFI app number: 18+1 */
    ABM_SSL_APP_NUM_END = ABM_SUPL_APP_NUM_END + ABM_SSL_APP_NUM,  /*  WIFI app number: 19+1 */
    
    ABM_MAX_APP_NUM = ABM_SSL_APP_NUM_END                 /* Max app number: 20 */
} abm_max_app_num_enum;

#endif /* _CBM_CONSTS_H */

