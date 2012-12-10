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
 * bt_hfg_struct.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *   struct of local parameter for hfg adp sap
 *
 * Author:
 * -------
 * Elvis Lin
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision: #1 $
 * $Modtime: $
 * $Log: $
 *
 * 09 22 2010 sh.lai
 * [ALPS00003522] [BLUETOOTH] Android 2.2 BLUETOOTH porting
 * Integrate bluetooth code from //ALPS_SW_PERSONAL/sh.lai/10YW1040OF_CB/ into //ALPS_SW/TRUNK/ALPS/.
 *
 * 09 10 2010 sh.lai
 * NULL
 * 1. Fix CR ALPS00125222 : [MTK BT]when dial out a invalidable number via Bluetooth headset,phone audio connot be connect automatically
 * Cause : Original HFG code that will create SCO when call state becomes alerting. In HFG SPEC, it create SCO when call state becomes DIALING.
 * Solution : Create SCO when call state becomes DIALING.
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __BT_HFG_STRUCT_H__
#define __BT_HFG_STRUCT_H__

#if defined (BTMTK_ON_WIN32) || defined (BTMTK_ON_WISESDK)
#include <windows.h>
#endif  /* #if defined (BTMTK_ON_WIN32) || defined (BTMTK_ON_WISESDK) */
#include "bt_struct.h"
#include "bluetooth_hfg_common.h"
#include "bluetooth_hfg_struct.h"

/*******************************************************************************
*   Due to current there is no BT common headser files, I define some common BT
*   used types here
********************************************************************************/
typedef I8 BtStatus;

#define BT_STATUS_SUCCESS 0
#define BT_STATUS_FAILED  1
#define BT_STATUS_PENDING 2
#define BT_STATUS_IN_PROGRESS 19
/*******************************************************************************
*   End of BT common definition
********************************************************************************/

/***************************************************************************** 
* Definations
*****************************************************************************/

/***************************************************************************** 
* Typedef 
*****************************************************************************/

/*---------------------------------------------------------------------------
 * HfgCallbackParms structure
 *
 * This structure is sent to the application's callback to notify it of
 * any state changes.
 */
typedef struct _HfgCallbackParameters HfgCallbackParameters;
struct _HfgCallbackParameters 
{
    //HfgEvent    event;   /* Event associated with the callback       */

    //BtStatus    status;  /* Status of the callback event             */
	I8			status;
    //BtErrorCode errCode; /* Error code (reason) on disconnect events */
	U8			errCode;

    /* For certain events, a single member of this union will be valid.
     * See HfgEvent documentation for more information.
     */
    union {
        void                        		*ptr;
        void                        		*context;
        //BtRemoteDevice         	*remDev;
        HfgHandsFreeFeatures    	features;
        kal_bool                    		enabled;
        const char             		*phoneNumber;
        const char             		*memory;        
        U16                       		index;
        U8                      		dtmf;
        U8                      		gain;
        U8                      		button; /* AT+CKPD=200 */
        HfgHold                		*hold;
        HfgHandsFreeVersion    	version;
        //HfgResponse            	*response;
        void                         		*response;
        MTK_BD_ADDR                 		*addr;
        HfgResponseHold         	respHold;    /* Only valid if HFG_USE_RESP_HOLD
                                              * is set to XA_ENABLED.
                                              */

#if defined(BT_SCO_HCI_DATA) && BT_SCO_HCI_DATA == XA_ENABLED
        HfgAudioData           		*audioData;   /* Only valid if BT_SCO_HCI_DATA is
                                              * set to XA_ENABLED.
                                              */
        BtPacket               		*audioPacket; /* Only valid if BT_SCO_HCI_DATA is
                                              * set to XA_ENABLED.
                                              */
#endif
        HfgAtData              		*data;
	const char				*charset;		/* HFG_EVENT_SELECT_CHARSET	 */
	 //HfgCHarsetType			charset; 			/* +CSCS */
        /* Phone book related */
        U16                     			pbStorage;     	/* EVENT_HFG_SELECT_PHONEBOOK */
        HfgPbRead            			*pbRead;          	/* EVENT_HFG_READ_PBENTRY */
        HfgPbFind             			*pbFind;            	/* EVENT_HFG_FIND_PBENTRY */
        HfgPbWrite           			*pbWrite;           /* EVENT_HFG_WRITE_PBENTRY */
	/* SMS related */
	HfgSMSService_cmd			service;			/* EVENT_HFG_SELECT_SMS_SERVICE */
	HfgSMSPrefStorage_cmd			*prefStorage;	/* EVENT_HFG_SELECT_PREF_MSG_STORAGE	*/
	HfgSMSFormat_cmd			format; 			/* EVENT_HFG_SELECT_MSG_FORMAT */
											/* 0: PDU mode, 1: Text mode */
	HfgSMSSrviceCentre_cmd		*serviceCentre; 	/* EVENT_HFG_SET_SERVICE_CENTRE */
	HfgSMSTextModeParam_cmd		*textParams;	/* EVENT_HFG_SET_TEXT_MODE_PARAMS */
	HfgSMSShowParams_cmd		show;			/* EVENT_HFG_SET_SHOW_PARAMS */
	HfgSMSIndSetting_cmd			*newMsgInd; 	/* EVENT_HFG_SET_NEW_MSG_INDICATION */
	HfgSMSList_cmd				stat;			/* EVENT_HFG_LIST_MSG */
	HfgSMSRead_cmd				readMsgIndex;	/* EVENT_HFG_READ_MSG */
	HfgSMSDelete_cmd				delMsgIndex;	/* EVENT_HFG_DELETE_MSG */
	HfgSMSSend_cmd				*sendMsg;		/* EVENT_HFG_SEND_MSG */
	HfgSMSSendStored_cmd			*storedMsg; 		/* EVENT_HFG_SEND_STORED_MSG */
	HfgSMSWrite_cmd				*writeMsg;		/* EVENT_HFG_WRITE_MSG */
    } p;
};

/*---------------------------------------------------------------------------
 * HfgCallback type
 *
 * A function of this type is called to indicate Hands-Free events to
 * the application.
 */
//typedef void (*HfgCallback)(/*HfgChannel*/void *Cgabbek, HfgCallbackParms *Parms);

/* End of HfgCallback */

typedef struct _HfgChannelContext
{
    void*                      hfgContext;
    kal_bool                      bHeadset;
    BTMTK_EventCallBack	callback;
    /* BtStatus */I8        status;        /* Used by message handler to return result of request */
#if defined( BTMTK_ON_LINUX )
    int                         sockfd;
#endif
}HfgChannelContext;

#endif//BT_HFG_STRUCT_H


