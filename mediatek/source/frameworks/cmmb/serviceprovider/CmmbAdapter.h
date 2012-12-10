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

#ifndef CMMB_ADAPTER_H
#define CMMB_ADAPTER_H

// CmmbAdapter.h
#include "CmmbHelper.h"



int CmmbChipQuery();
CmmbResult CmmbDriverInit();
CmmbResult CmmbDriverTerminate();

CmmbResult Tune(UINT32 frequency);
CmmbResult GetTs0();
CmmbResult StopTs0();
CmmbResult StartTs0();
CmmbResult StartService(CmmbService& srvInfo);
CmmbResult StopService(UINT32 serviceHdl);
CmmbResult CmmbDriverUamDataExchange( 
	const UINT8* pWriteBuf, 
	UINT32 writeLen, 
	UINT8* pOutReadBuf, 
	UINT32 readBufSize, 
	UINT32* pOutReadLen,
	UINT16* pStatusWord
);
CmmbResult CmmbDriverUamInit();
CmmbResult CmmbDriverSetCaSaltKeys(
	UINT32 serviceHdl,
	UINT8 ecmType,
	const UINT8 videoSalt[CMMB_CA_SALT_SIZE],
	const UINT8 audioSalt[CMMB_CA_SALT_SIZE],
	const UINT8 dataSalt[CMMB_CA_SALT_SIZE]
);
CmmbResult CmmbDriverSetCaControlWords( 
	UINT32 serviceHdl,
	UINT32 sbufrmIdx, 
	const TCmmbCaCwPair& controlWords
	);

UINT32 GetSignalQuality();
UINT32 GetSignalFrequency();
UINT32 GetModemState();
UINT32 GetSignalRSSI();
UINT32 GetSignalSNR();
UINT32 GetSignalBER();
UINT32 GetSignalPwr();
UINT32 GetSignalCarrierOffset();
bool CmmbDriverZeroSpiBuf();

typedef struct _CmmbAdapterFunc{
CmmbResult (*CmmbDriverInit)();
CmmbResult (*CmmbDriverTerminate)();
CmmbResult (*Tune)(UINT32 );
CmmbResult (*GetTs0)();
CmmbResult (*StopTs0)();
CmmbResult (*StartTs0)();
CmmbResult (*StartService)(CmmbService& );
CmmbResult (*StopService)(UINT32 );
CmmbResult (*CmmbDriverUamDataExchange)( 
    const UINT8* , 
    UINT32 , 
    UINT8* , 
    UINT32 , 
    UINT32* ,
    UINT16* );
CmmbResult (*CmmbDriverUamInit)();
CmmbResult (*CmmbDriverSetCaSaltKeys)(
    UINT32 ,
    UINT8 ,
    const UINT8 videoSalt[CMMB_CA_SALT_SIZE],
    const UINT8 audioSalt[CMMB_CA_SALT_SIZE],
    const UINT8 dataSalt[CMMB_CA_SALT_SIZE]
);
CmmbResult (*CmmbDriverSetCaControlWords)( 
    UINT32 ,
    UINT32 , 
    const TCmmbCaCwPair& );

UINT32 (*GetSignalQuality)();
UINT32 (*GetSignalFrequency)();
UINT32 (*GetModemState)();
UINT32  (*GetSignalRSSI)();
UINT32  (*GetSignalSNR)();
UINT32  (*GetSignalBER)();
UINT32  (*GetSignalPWR)();
UINT32  (*GetSignalCarrierOffset)();
bool (*CmmbDriverZeroSpiBuf)();
}CmmbAdapterFuncStruct;

#endif // CMMB_ADAPTER_H