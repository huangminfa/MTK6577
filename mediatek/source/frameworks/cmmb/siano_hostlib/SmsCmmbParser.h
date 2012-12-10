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

/*************************************************************************/
/*                                                                       */
/* Copyright (C) 2005-2009 Siano Mobile Silicon Ltd. All rights reserved */
/*                                                                       */
/* PROPRIETARY RIGHTS of Siano Mobile Silicon are involved in the        */
/* subject matter of this material.  All manufacturing, reproduction,    */
/* use, and sales rights pertaining to this subject matter are governed  */
/* by the license agreement.  The recipient of this software implicitly  */
/* accepts the terms of the license.                                     */
/*                                                                       */
/*                                                                       */
/*************************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef _SMS_CMMB_PARSER_H_
#define _SMS_CMMB_PARSER_H_

#include "SmsCmmbDemux.h"

/*************************************************************************
*			 Macros
*************************************************************************/
#define SMS_READ_BIT_FIELD(val,x,y)	( ((val) & SMS_BITS(x,y)) >> (y) )
#define SMS_BITS(x,y) ( ~( (0xFFFFFFFFUL >> (31-(y)))>>1 ) & (0xFFFFFFFFUL >> (31-(x))) )	
#define SMS_BIT(a)		(1 << (a))
#define SMS_MAX_NUM_CA_SYSTEMS (20)
#define SMS_NUM_SUPPORTED_ESG_SERVICES (10)
#define SMS_NUM_SUPPORTED_ESG_DATA_BLOCKS_PER_TYPE (30)

#define SMS_CMMB_PARSER_MAX_NIT_NEIGH_NETWORKS (20)
#define SMS_CMMB_PARSER_MAX_NIT_OTHER_FREQS (20)
#define SMS_CMMB_PARSER_MAX_NETWORK_NAME_LEN (128)
#define SMS_CMMB_PARSER_MAX_CSCT_SERVICES (80)
#define SMS_CMMB_PARSER_MAX_SUBFRAMES_PER_FRAME (16)
#define SMS_CMMB_PARSER_MAX_EB_LANG_COUNT	(16)
#define SMS_CMMB_PARSER_MAX_EB_AUXDATA_COUNT	(16)


/*************************************************************************
*			 Enums
*************************************************************************/
enum SmsCmmbEsgDataTypes_EN
{
	SMS_ESG_FIRST_TYPE				= 0,
	SMS_ESG_SERVICE_TYPE			= 1,
	SMS_ESG_SERVICE_AUX_TYPE		= 2,
	SMS_ESG_SCHEDULE_TYPE			= 3,
	SMS_ESG_CONTENT_TYPE			= 4,
	SMS_ESG_SERVICE_PARAMETER_TYPE	= 5,
	SMS_ESG_LAST_TYPE				= 6,
};

/*************************************************************************
*			 Structs
*************************************************************************/
typedef struct SmsUtilBitReader_S
{
	const UINT8*	pBuf;
	UINT32			BufSize;
	UINT32			ByteOffset;
	UINT32			StartBit;
} SmsUtilBitReader_ST;	

typedef struct SmsCaSysDesc_S
{
	UINT16 CaSysId;
	UINT16 EmmServiceId;
	UINT8 EmmDataType;
	UINT8 EcmDataType;
	UINT8 EcmTransportType;
} SmsCaSysDesc_ST;

typedef struct SmsCaDescTable_S
{
	UINT16 HeaderLengthField;
	UINT8 UpdateNumber;
	UINT8 SectionNumber;
	UINT8 SectionQuantity;
	UINT8 NumSystems;
	SmsCaSysDesc_ST CaSystemsArr[SMS_MAX_NUM_CA_SYSTEMS];
} SmsCaDescTable_ST;

typedef struct SmsEsgServiceDesc_S
{
	UINT8 Index;
	UINT16 ServiceId;
} SmsEsgServiceDesc_ST;

typedef struct SmsEsgDataBlock_S
{
	UINT8 Id;
	UINT8 VerionAndIndex;
} SmsEsgDataBlock_ST;

typedef struct SmsEsgDataType_S
{
	UINT8 TypeId;
	UINT8 NumBlocks;
	SmsEsgDataBlock_ST BlocksArr[SMS_NUM_SUPPORTED_ESG_DATA_BLOCKS_PER_TYPE];
} SmsEsgDataType_ST;

typedef struct SmsEsgTable_S
{
	UINT8 UpdateNumber;
	UINT16 SectionLength;
	UINT8 SectionNumber;
	UINT8 NumberOfSections;
	UINT16 NetworkID;
	UINT8 LocalTimeShift;
	UINT8 CharacterEncoding;
	UINT8 NumEsgServices;
	UINT8 NumDataTypes;
	SmsEsgServiceDesc_ST EsgServicesArr[SMS_NUM_SUPPORTED_ESG_SERVICES];
	SmsEsgDataType_ST DataTypesArr[SMS_ESG_LAST_TYPE];
} SmsEsgTable_ST;

typedef struct SmsCmmbParserMpxFrame_S
{
	UINT8	Id;
	UINT8	FirstTimeslot;
	UINT8	NumTimeSlots;
	UINT8	NumSubFrames;
	UINT8	RsCr;
	UINT8	InterleavingMode;
	UINT8	LdpcCr;
	UINT8	Constellation;
	UINT8	ScramblingMode;

	UINT8	SubFrameNumsArr[SMS_CMMB_PARSER_MAX_SUBFRAMES_PER_FRAME];
	UINT16	SubFrameServiceIdsArr[SMS_CMMB_PARSER_MAX_SUBFRAMES_PER_FRAME];
} SmsCmmbParserMpxFrame_ST;

typedef struct SmsCmmbParserInfo_CMCT_S
{
	UINT8	UpdateSeqNum;
	UINT8	FreqPointNum;
	UINT8	NumMpxFrames;
	SmsCmmbParserMpxFrame_ST MultiplexFramesArr[40];
} SmsCmmbParserInfo_CMCT_ST;

typedef struct SmsCmmbParserInfo_CSCT_S
{
	UINT8	UpdateSeqNum;	//4
	UINT8	SectionNumber;	//8
	UINT8	NumSections;	//8
	UINT16	SectionLength;	//16
	UINT16	NumServices;	//16
	UINT16  ServiceIdArr[SMS_CMMB_PARSER_MAX_CSCT_SERVICES];
	UINT8   ServiceFreqPtArr[SMS_CMMB_PARSER_MAX_CSCT_SERVICES];
} SmsCmmbParserInfo_CSCT_ST;

//---------------------
// Frequency Info 
typedef struct SmsCmmbFreqPoint_S
{
	UINT8	Number;			//8
	UINT8	Bandwidth;		//4
	UINT32	CenterFreq;		//32
} SmsCmmbFreqPoint_ST;


//---------------------
// Network info
typedef struct SmsNetworkDesc_S
{
	UINT16				NetworkId;
	SmsCmmbFreqPoint_ST	Freq;
} SmsNetworkDesc_ST;

//-----------------
// NIT table
typedef struct SmsCmmbParserInfo_NIT_S
{
	UINT8				UpdateSeqNum;		
	UINT8				NumOfNeighNetworks;
	UINT8				NumOfOtherFreqPoints;
	UINT8				NetworkIdMode;
	UINT32 				SystemTimeHigh;		
	UINT32 				SystemTimeLowByte;	
	UINT32 				CountryCode;			
	UINT16 				NetworkId;			// Network Level [15:12] and network number [11:0]
	SmsCmmbFreqPoint_ST	ThisFreqPoint; 
	UINT8 				NetworkNameLen;		
	UINT8  				NetworkName[SMS_CMMB_PARSER_MAX_NETWORK_NAME_LEN];
	SmsCmmbFreqPoint_ST OtherFreqPtsArr[SMS_CMMB_PARSER_MAX_NIT_OTHER_FREQS];
	SmsNetworkDesc_ST	NeigNetworksArr[SMS_CMMB_PARSER_MAX_NIT_NEIGH_NETWORKS];
} SmsCmmbParserInfo_NIT_ST;


//---------------
//EB message info
typedef struct SmsCmmbEBMsgInfo_S{
	UINT32		LangType;
	UINT8		ProviderFlag;
	UINT8		RefServiceFlag;

	UINT16		TextLen;
	const UINT8	*pText;

	UINT8		ProviderNameLen;
	const UINT8	*pProviderName;

	UINT16		RefServiceId;
	UINT8		AuxDataIndex;
}SmsCmmbEBMsgInfo_ST;

//----------------
//EB auxiliary data info
typedef struct SmsCmmbEBAuxDataInfo_S{
	UINT8		AuxDataType;
	UINT16		AuxDataLen;
	const UINT8	*pAuxData;
}SmsCmmbEBAuxDataInfo_ST;

//----------------
//EB table
typedef struct SmsCmmbParserInfo_EBT_S
{
	UINT8        ConcurMsgCount;
	UINT8       EBNum;
	UINT16		EBLen;

	UINT8 		ProtocolVersion;
	UINT8		ProtocolLowestVersion;
	UINT8		NetworkLevel;

	UINT16		NetworkNum;
	UINT16		MsgId;

	UINT8		CurSectNum;
	UINT8		LastSectNum;
	UINT16		DataLen;

	UINT8		TriggerFlag;
	UINT8		MsgType;
	UINT8		MsgLevel;
	UINT8		MsgCharSet;

	UINT32		TxTimeHigh;
	UINT32		TxTimeLowByte;
	UINT32		DurTime;
	UINT32		LangCount;
	SmsCmmbEBMsgInfo_ST	MsgInfo[SMS_CMMB_PARSER_MAX_EB_LANG_COUNT];

	UINT8		AuxDataCount;

	SmsCmmbEBAuxDataInfo_ST	AuxDataInfo[SMS_CMMB_PARSER_MAX_EB_AUXDATA_COUNT];

	UINT8		TriggerMsgType;
	UINT8		TriggerMsgLevel;
	UINT8		TriggerNetworkLevel;

	UINT16		TriggerNetworkNum;
	UINT8		TriggerFreqNum;

	UINT32		TriggerFreq;
	UINT8		TriggerBandwidth;
}SmsCmmbParserInfo_EBT_ST;

typedef enum SMSHOSTLIB_EBT_S
{
	SMSHOSTLIB_EBT_TEXT_DATA 		= 0,
	SMSHOSTLIB_EBT_PROVIDER_DATA	= 1,
	SMSHOSTLIB_EBT_AUX_DATA			= 2

}SMSHOSTLIB_EBT_ET;



/*************************************************************************
*			 Fwd Declarations
*************************************************************************/
SMSHOSTLIB_ERR_CODES_E UtilBitReader_ReadBitsSafe( SmsUtilBitReader_ST* pReaderState, UINT32 NumOfBits , UINT32 *RetVal);
UINT32 SmsUtils_CRC8Compute( UINT8 initialVector, const UINT8* pData, UINT32 len );
SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseMpxBlock(	UINT8* pBuf,
												   UINT32 BufSize,
												   SmsCmmbMpxBlock_ST* pOutHeader );

SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseDataSectionHeader(UINT8* pBuf,
															UINT32 BufSize,
															SmsCmmbSectionHeader_ST* pOutHeader );
SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseVideoSectionHeader(UINT8* pBuf,
															 UINT32 BufSize,
															 SmsCmmbSectionHeader_ST* pOutHeader );
SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseAudioSectionHeader(UINT8* pBuf,
															 UINT32 BufSize,
															 SmsCmmbSectionHeader_ST* pOutHeader );
SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseSubFrameHeader( UINT8* pBuf,
														 UINT32 BufSize,
														 SmsCmmbSubFrameHeader_ST* pOutHeader );
SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseFrameHeader( UINT8* pBuf,
													  UINT32 BufSize,
													  SmsCmmbMultiplexFrameHeader_ST* pOutFrameHeader );
SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseXpe( UINT8* pBuf,
											  UINT32 BufSize,
											  SmsCmmbXpePacket_ST* pOutXpeHeader,
											  SmsCmmbDataServiceMode_ET* pOutMode,
											  SmsCmmbXpeFatHeader_ST* pOutFatHeader,
											  SmsCmmbFileSliceHeader_ST* pOutSliceHeader );
SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseXpeFec( UINT8* pBuf,
												 UINT32 BufSize,
												 SmsCmmbXpeFecPacket_ST* pOutXpeFecHeader );







/*************************************************************************
*			 Control tables parsers
*************************************************************************/
SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserParseCaTable( UINT8* pBuf,
												  UINT32 BufSize,
												  SmsCaDescTable_ST* pOutHeader );
SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E	SmsCmmbParserTable_ESG( UINT8* pBuf,
																UINT32 BufSize,
																SmsEsgTable_ST* pOutEsgTable );



SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E SmsCmmbParserTable_xMCT(	 UINT8* pBuf,
  								UINT32 BufSize,
									SmsCmmbParserInfo_CMCT_ST* pOutCmct );

SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E SmsCmmbParserTable_xSCT( UINT8* pBuf,
													UINT32 BufSize,
													SmsCmmbParserInfo_CSCT_ST* pOutCsct );
SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E SmsCmmbParserTable_NIT(  UINT8* pBuf,
															  UINT32 BufSize,
															  SmsCmmbParserInfo_NIT_ST* pOutNit );

SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E SmsCmmbParserTable_EBT(UINT8* pBuf,
															 UINT32 BufSize,
															 SmsCmmbParserInfo_EBT_ST *pOutEbt);

#endif //_SMS_CMMB_PARSER_H_


#ifdef __cplusplus
}
#endif
