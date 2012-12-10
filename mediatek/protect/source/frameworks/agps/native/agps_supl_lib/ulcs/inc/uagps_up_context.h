/******************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2007
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
*******************************************************************************/

/******************************************************************************
 * Filename:
 * --------------------------------------------------------
 *   uagps_up_context.h
 *
 * Project:
 * --------------------------------------------------------
 *   MONZA
 *
 * Description:
 * --------------------------------------------------------
 *   The UAGPS_UP context.
 *
 * Author:
 * --------------------------------------------------------
 *   David Niu
 *
 * --------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 07 30 2010 david.niu
 * [MAUI_02600931] [3G AGPS CP] AGPS related code checked in MT6276_DVT and MAUI
 * AGPS check-in for MT6276_DVT
 *
 * May 15 2010 mtk02119
 * [MAUI_02525171] [3G AGPS CP] ULCS code check-in for modification about Measurement Control Failure m
 * 
 *
 * May 3 2010 mtk02119
 * [MAUI_02412594] [3G AGPS CP] ULCS code check-in for new CP implementation and UP modification
 * 
 *
 * Sep 4 2009 mtk02119
 * [MAUI_01936271] [3G AGPS][User Plane] Check-in 3G AGPS UP related files into MAUI on W09.37
 * add to source control recursely
 *
 * Jul 27 2009 mtk02119
 * [MAUI_01928347] [MT6268_AGPS_Dev] Modify UAGPS_UP module related code for R6 ASN and some problem fi
 * 
 *
 * Jul 13 2009 mtk02119
 * [MAUI_01719929] [MT6268_AGPS_Dev] Modify UAGPS_UP module related code after IT and OMA ETS test
 * 
 *
 * Jun 29 2009 mtk02119
 * [MAUI_01711600] [MT6268_AGPS_Dev] Add UAGPS_UP module related code
 * add to source control recursely
 *
 *****************************************************************************/

#ifndef _UAGPS_UP_CONTEXT_H
#define _UAGPS_UP_CONTEXT_H


/*****************************************************************************
 * Macros Without Parameters
 *****************************************************************************/


/*****************************************************************************
 * Macros With Parameters
 *****************************************************************************/


/*****************************************************************************
 * Enums without tags
 *****************************************************************************/


/*****************************************************************************
 * Type Definitions
 *****************************************************************************/
typedef struct
{
    kal_uint8 horizontalAccuracy;
    kal_bool verticalAccuracyValid;
    kal_uint8 verticalAccuracy;
} UAGPS_UP_PosAccuracy;

typedef struct
{
    kal_bool accuracyValid;
    kal_uint8 accuracy; 
} UAGPS_UP_MeasAccuracy;


typedef struct
{
    kal_bool posReq;

    kal_bool environmentValid;
    kal_uint8 environment;

    union
    {
        UAGPS_UP_PosAccuracy posAccuracy;
        UAGPS_UP_MeasAccuracy measAccuracy;
    }
    choice;

    /* modify to kal_uint32 for consistent with UAGPS_CP  */
    kal_uint32 reportingPeriod;
} UAGPS_UP_PendingPosOrMeasReq;

typedef struct
{
    kal_bool 		                    isSuplSessionStarted;
    kal_uint16		                    currentMeasurementIds; // bitmap
    kal_uint16		                    additionalAssistanceDataReq;                          	// bitmap one-to-one mapping for currentMeasurementIds  
    kal_uint16		                    currentMeasurementControlTransactions; // bitmap
    kal_uint16		                    currentPendingforAssistanceDataTransactions; // bitmap => add for wait more assistance data to start next transaction
  	kal_uint8		                    assistanceDataReqTransactions; // From 17 -> 255, for RRC_maxNoOfMeas = 16
    UAGPS_UP_PendingPosOrMeasReq        pendingPosOrMeasReq[RRC_maxNoOfMeas];
} UAGPS_UP_Context;


/*****************************************************************************
 * Declarations Of Exported Globals
 *****************************************************************************/


#endif /* _UAGPS_UP_CONTEXT_H */

