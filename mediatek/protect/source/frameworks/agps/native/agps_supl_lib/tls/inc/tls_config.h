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

/*******************************************************************************
 * Filename:
 * ---------
 *   tls_config.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   Global config file for TLS.
 *
 * Author:
 * -------
 *   Wyatt Sun
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Jul 16 2009 mtk01264
 * [MAUI_01721116] [EMA]:Auto check doesn't work on Yahoo account sometimes.
 * New compile option to enable/disable client authentication, defualt is not supported
 *
 * Mar 17 2009 mtk01264
 * [MAUI_01646558] [TLS] Check-in OpenSSL and revise TLS
 * Check in revised TLS
 *
 * Feb 20 2008 mtk01264
 * [MAUI_00621628] [TLS] remove checking module id
 * Turn off _TLS_CHECK_MODULE_
 *
 * Nov 30 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Add compile option for debugging
 *
 * Nov 29 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Check in TLS task
 *
 * Oct 21 2007 mtk01264
 * [MAUI_00563283] [TLS] Check in TLS task
 * Add TLS task
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef _TLS_CONFIG_H_
#define _TLS_CONFIG_H_

/* Define to 1 to check CNAME in received certificate */
//#undef _CHECK_CNAME_
#define _CHECK_CNAME_


/* Define to 1 to defence MITM attack while accepting invalid certificate */
#undef _DEFENCE_MITM_
//#define _DEFENCE_MITM_


/* Workaround the bug that Email does not pass the same port while
 * invoking tls_new_conn() */
//#undef _FINGERPRINT_DB_PORT_WORKAROUND_
#define _FINGERPRINT_DB_PORT_WORKAROUND_ 1


/* #undef _TLS_DEFAULT_VERIFY_CALLBACK_ */

/* Define to 1 to accept inject message */
#ifdef __PRODUCTION_RELEASE__
#undef TLS_INJ_MSG
#else /* !__PRODUCTION_RELEASE__ */
#define TLS_INJ_MSG 1
#endif /* !__PRODUCTION_RELEASE__ */

#define _BY_TIMER_ 0
#define _BY_SESSION_ 1
#define _TLS_RENEW_SESS_DB_ _BY_TIMER_

#define ASCII_ONLY 1
/* #undef _SUPPORT_CLIENT_AUTH_ */

//#undef _TLS_DEBUG_

#endif /* !_TLS_CONFIG_H_ */


