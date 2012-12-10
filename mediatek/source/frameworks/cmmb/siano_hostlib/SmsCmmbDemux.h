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

#include "SmsHostLibTypes.h"

#ifdef __cplusplus
extern "C" {
#endif



#ifndef _SMS_CMMB_DEMUX_H_
#define _SMS_CMMB_DEMUX_H_

/*************************************************************************
*			 Macros
*************************************************************************/
#define SMS_CMMB_NUM_SUPPORTED_VIDEO_STREAMS_IN_SUBFRAME (8)
#define SMS_CMMB_NUM_SUPPORTED_AUDIO_STREAMS_IN_SUBFRAME (8)
#define SMS_CMMB_SUPPORTED_UNITS_NUM	(100)

/*************************************************************************
*			 Enums
*************************************************************************/

enum
{
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_INIT_BCAST_TIME	= 7,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_VID_SECT_LEN		= 6,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_AUD_SECT_LEN		= 5,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_DATA_SECT_LEN		= 4,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_EXPANSION_AREA	= 3,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_SCRAMBLING0   	= 2,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_SCRAMBLING1   	= 1,
	SMS_CMMB_SUB_FRAME_INDICATION_BIT_PACK_MODE			= 0,

	SMS_CMMB_VID_EXP_INDICATION_CODE_RATE		= 3,
	SMS_CMMB_VID_EXP_INDICATION_PIC_DISP		= 2,
	SMS_CMMB_VID_EXP_INDICATION_RESOLUTION		= 1,
	SMS_CMMB_VID_EXP_INDICATION_FRAME_FREQ		= 0,

	SMS_CMMB_AUD_EXP_INDICATION_CODE_RATE		= 2,
	SMS_CMMB_AUD_EXP_INDICATION_SAMPLING_RATE	= 1,
	SMS_CMMB_AUD_EXP_INDICATION_STREAM_DESC		= 0,

	SMS_CMMB_VID_UNIT_INDICATION_FRAME_END		= 1,
	SMS_CMMB_VID_UNIT_INDICATION_REL_BCAST_TIME	= 0,
};

typedef enum SmsCmmbSectionType_E
{
	SMS_CMMB_SECTION_TYPE_VIDEO = 0,
	SMS_CMMB_SECTION_TYPE_AUDIO = 1,
	SMS_CMMB_SECTION_TYPE_DATA = 2,
	SMS_CMMB_SECTION_TYPE_RESERVED = 3,
} SmsCmmbSectionType_EN;

typedef enum SMS_CMMB_ALGORYTHM_TYPE_ET
{
	SMS_CMMB_AUD_ALGORYTHM_TYPE_DRA			= 0,
	SMS_CMMB_AUD_ALGORYTHM_TYPE_AAC_PLUS	= 1,
	SMS_CMMB_AUD_ALGORYTHM_TYPE_AAC			= 2,

	SMS_CMMB_VID_ALGORYTHM_TYPE_AVS			= 0,
	SMS_CMMB_VID_ALGORYTHM_TYPE_H264		= 1,

} SMS_CMMB_ALGORYTHM_TYPE_E;

//---------------------
// Control table IDs
enum  CMMB_TABLE_ID_E
{
	CMMB_TID_RESERVED		= 0x00,
	CMMB_TID_NIT			= 0x01,
	CMMB_TID_CMCT			= 0x02,
	CMMB_TID_CSCT			= 0x03,
	CMMB_TID_SMCT			= 0x04,
	CMMB_TID_SSCT			= 0x05,
	CMMB_TID_ESG_BASIC		= 0x06,
	CMMB_TID_CA_EADT		= 0x07,
	CMMB_TID_URGENT_BCAST	= 0x10
};

//---------------------
// Encryption modes
typedef enum SmsCmmbEncryptionIndication_E
{
	SMS_CMMB_ENCRYPTION_MODE_NOT_ENCRYPTED	= 0,
	SMS_CMMB_ENCRYPTION_MODE_DECRYPTED		= 1,
	SMS_CMMB_ENCRYPTION_MODE_ENCRYPTED		= 2,
	SMS_CMMB_ENCRYPTION_MODE_RESERVED		= 3,
} SmsCmmbEncryptionIndication_EN;

//---------------------
// Encapsulation Mode (1 or 2)
typedef enum SmsCmmbEncapsulationMode_EN
{
	SMS_CMMB_ENCAPSULATION_MODE2	= 0,
	SMS_CMMB_ENCAPSULATION_MODE1	= 1,
} SmsCmmbEncapsulationMode_E;

//---------------------
// Data unit type
typedef enum SmsCmmbDataUnitType_E
{
	SMS_CMMB_DATA_UNIT_TYPE_ESG = 0,
	SMS_CMMB_DATA_UNIT_TYPE_XPE = 160, 
	SMS_CMMB_DATA_UNIT_TYPE_XPE_FEC = 161, 
}SmsCmmbDataUnitType_ET; 

//---------------------
// XPE-FEC algorithm type 
typedef enum SmsCmmbFecAlgoTag_E
{
	SMS_CMMB_FEC_ALGO_TAG_RS_255_207 = 0,
}SmsCmmbFecAlgoTag_ET; 

//---------------------
// Data service mode 
typedef enum SmsCmmbDataServiceMode_E
{
	SMS_CMMB_DATA_SERVICE_STREAM_MODE	= 0,
	SMS_CMMB_DATA_SERVICE_FAT_MODE		= 1,
	SMS_CMMB_DATA_SERVICE_FILE_MODE		= 2,
	SMS_CMMB_DATA_SERVICE_RESERVED_MODE	= 3,
}SmsCmmbDataServiceMode_ET; 

//---------------------
// FAT Encoding type
// Values for the "Type of Coding" filed in a FAT slice header
typedef enum SmsCmmbDataServiceEncodingType_E
{
	SMS_CMMB_FAT_ENCODING_TYPE_UNCOMPRESSED = 0,		// Open
	SMS_CMMB_FAT_ENCODING_TYPE_IETF_RFC_1952_GZIP = 1,	// Compressed
	// Rest is reserved
}SmsCmmbDataServiceEncodingType_ET; 

/*************************************************************************
*			 Structs
*************************************************************************/

//*****************************************
// Video expansion area in sub frame header
typedef struct SmsCmmbVidExpArea_S
{
	UINT8	VidType;
	UINT8	VidIndications;

	UINT8	PicDisplayAbscissa;
	UINT8	PicDisplayOrdinate;
	UINT8	PicDisplayPriority;
	UINT8	FrameFreq;

	UINT16	VideoCodeRate;
	UINT16	VidHorizontalResolution;
	UINT16	VidVerticalResolution;
	
	UINT32 RESERVED1;
	UINT32 RESERVED2;
} SmsCmmbVidExpArea_ST;

//*****************************************
// Audio expansion area in sub frame header
typedef struct SmsCmmbAudExpArea_S
{
	UINT8	AudType;		// According to #SMS_CMMB_ALGORYTHM_TYPE_E
	UINT8	Indications;
	UINT8	SamplingRate;
	UINT16	CodeRate;
	UINT32	AudioDesc;
	UINT32	RESERVED1;
	UINT32	RESERVED2;
} SmsCmmbAudExpArea_ST;

//*****************************************
// Multiplex Sub frame header
typedef struct SmsCmmbSubFrameHeader_S
{
	UINT8	SubframeIndex;		// Not from the header in the stream - set by demux
	UINT8	HeaderLengthField;
	UINT8	Indications;
	UINT8	EncryptionMode; // According to SmsCmmbEncryptionIndication_EN
	UINT8	IsEncapMode1;
	UINT8	TotalVidStreams;
	UINT8	TotalAudStreams;
	UINT32	InitBcastTime;
	UINT32	VidSectLen;
	UINT32	AudSectLen;
	UINT32	DataSectLen;
	UINT32  RESERVED1;
	UINT32  RESERVED2;

	SmsCmmbVidExpArea_ST	VidStreamsDesc[SMS_CMMB_NUM_SUPPORTED_VIDEO_STREAMS_IN_SUBFRAME];
	SmsCmmbAudExpArea_ST	AudStreamsDesc[SMS_CMMB_NUM_SUPPORTED_AUDIO_STREAMS_IN_SUBFRAME];
} SmsCmmbSubFrameHeader_ST;

//*****************************************
// Multiplex frame header
typedef struct SmsCmmbMultiplexFrameHeader_S
{
	UINT8	HeaderLengthField;		//8
	UINT8	ProtocolVersion;		//5
	UINT8	MinimumProtocolVersion;	//5
	UINT8	MultiplexFrameID;		//6
	UINT8	EmergencyBroadcastIndication;	//2
	UINT8	NextFrameParameterIndication;	//1
	UINT8	InitialLead;	//2
	UINT8	NIT_SeqNum;		//4
	UINT8	CMCT_SeqNum;	//4
	UINT8	CSCT_SeqNum;	//4
	UINT8	SMCT_SeqNum;	//4
	UINT8	SSCT_SeqNum;	//4
	UINT8	ESG_SeqNum;		//4
	UINT8	CA_SeqNum;		//4 AKA "Update No. of Expansion Controlling Information"
	UINT8	NumOfSubFrames;	//4
	UINT8	NextFrameParamHeaderLength;		//8
	UINT8	NextFrameParamMsf1HeaderLength;	//8
	UINT32	SubFrameLength[16];				//24*n
	UINT32	NextFrameParamMsf1Length;		//24
	UINT32	TotalFrameLength;	// Calculated field - not from header
	UINT32	RESERVED1;
	UINT32	RESERVED2;
} SmsCmmbMultiplexFrameHeader_ST ;

//-------------------------
// Audio/Video/Data unit parameters
// Important note - this struct is held 100 times on stack in an array
// Its size must not be extended.
// This structure holds the same fields for audio, video and data units,
// but not all fields are relevant to each type.
// These parameters are parsed from the section header and delivered
// by the demux with each unit.
typedef struct SmsCmmbUnitParams_S
{
	UINT16	UnitLengthField;	// The length of the unit
	UINT16	RelativeBcastTime;	// Relevant only to audio/video units. The relative time 
								// for broadcasting this unit. 
	UINT8	StreamNum;			// Relevant only to audio/video units. 
	UINT8	VideoFrameType;		// Relevant only to video units
	UINT8	VideoIndications;	// Relevant only to video units
	UINT8   DataUnitType;		// Relevant only to data units
} SmsCmmbUnitParams_ST;

//-------------------------
// Section Header
typedef struct SmsCmmbSectionHeader_S
{
	UINT32 NumUnits;
	UINT32 HeaderLengthIncludingCrc;
	UINT32 RESERVED1;
	UINT32 RESERVED2;

	SmsCmmbUnitParams_ST  UnitsParamsArr[SMS_CMMB_SUPPORTED_UNITS_NUM];
} SmsCmmbSectionHeader_ST;

//-------------------------
// Multiplex Block Header
typedef struct SmsCmmbMpxBlock_S
{
	UINT16 FullLength;
	UINT16 HeaderLength;
	UINT8 BeginTag;
	UINT8 EndTag;
	UINT8 Type;
	UINT8 DataUnitType;
	
	UINT32 RESERVED1;
	UINT32 RESERVED2;
} SmsCmmbMpxBlock_ST; 


//-------------------------
// RTP Header
typedef struct SmsCmmbRtpHdr_S
{
	UINT16 Flags;
	UINT16 Seq;
	UINT32 Timestamp;
	UINT32 SSRC;
} SmsCmmbRtpHdr_ST;


//-----------------
// XPE header information
typedef struct SmsCmmbXpePacket_S
{
	UINT8 HeaderLen;	// Header length including the 1-byte checksum
	UINT8 IsStart;
	UINT8 IsEnd; 
	UINT8 IsErrorCorrection;
	UINT8 IsCrc; 
	UINT8 IsCrcError; 
	UINT8 IsBusinessModeFileModel; 
	UINT8 TransportSequence; 
	UINT16 PayloadLen; 
	UINT16 DataPacketTotalLen; 

	UINT32 RESERVED1;
	UINT32 RESERVED2;
}SmsCmmbXpePacket_ST; 

//-----------------
// FAT information
typedef struct SmsCmmbXpeFatHeader_S
{
	UINT8 HeaderLen;
	UINT8 CurSliceNum;
	UINT8 UpdateNum;
	UINT8 IsCrcError; 
	UINT8 LastSliceNum;


	UINT16 ResourceId;
	UINT16 SliceLen;

	SmsCmmbDataServiceEncodingType_ET EncodeType;
	UINT32 RESERVED1;
	UINT32 RESERVED2;
}SmsCmmbXpeFatHeader_ST;

//-----------------
// File Slice header information
typedef struct SmsCmmbFileSliceHeader_S
{
	UINT8 HeaderLen;
	UINT8 UpdateNum;
	UINT8 IsScrambled;
	UINT8 Reserved;
	UINT16 ResourceId;
	UINT16 BlockNum;
	UINT16 SliceNum;

	UINT32 RESERVED1;
	UINT32 RESERVED2;
}SmsCmmbFileSliceHeader_ST;

//-----------------
// XPE FEC parameter struct
//
//
typedef struct XpeFecParameter_S
{
	// Only one of the fields is actually used - depending on the FEC algorithm type.

	// When FEC algo type is RS 255/207, then this parameter is used - 
	//  Number of interleaver rows
	UINT16 Rs_255_207_NumRows;		
		
	// When FEC algo is not RS 255/207 - this generic parameter is used.
	UINT8 pGenericParamBuf[31]; 

	UINT32 RESERVED1;
	UINT32 RESERVED2;
}XpeFecParameter_ST; 

//-----------------
// XPE header information
typedef struct SmsCmmbXpeFecPacket_S
{
	UINT8 FecParameterLength;
	UINT8 IsStart;
	UINT8 IsEnd; 
	UINT8 IsCrc; 
	UINT8 IsCrcError;
	UINT8 TransportSequence; 
	UINT8 FecAlgoTag;
	UINT16 HeaderLen;	// Header length, including the 1 byte checksum
	UINT16 PayloadLen; 
	UINT16 FecDataTotalLen; 
	XpeFecParameter_ST FecParameter; 

	UINT32 RESERVED1;
	UINT32 RESERVED2;
}SmsCmmbXpeFecPacket_ST; 

/*************************************************************************/
/*			 Demux Callbacks Prototypes Per Format						 */
/*************************************************************************/

/*************************************************************************/
// The following callback prototypes are function that the demux calls
// during the demultiplex process. Each function is used to report
// a different part of a multiplex frame.
// Pointers of these prototypes are a part of the #SmsDemuxCallbacks_ST struct
// filled by the user, and delivered as an argument to the demux.
// See #SmsCmmbDemuxProcessMpxFrame
//


/*************************************************************************/
/*!
Callback to deliver a multiplex frame.
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each multiplex frame
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	pBuf - A buffer containing the frame
	\param[in]	BufSize - The size of the multiplex frame
	\param[in]	pMfHeader - A parsed multiplex frame header. See #SmsCmmbMultiplexFrameHeader_ST
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnMultiplexFrame)( UINT32 ServiceHandleNum,
									UINT8* pBuf,
									UINT32 BufSize,
									const SmsCmmbMultiplexFrameHeader_ST* pMfHeader,
									void* ClientPtr );


/*************************************************************************/
/*!
Callback to deliver a control table from TS0.
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each control table that the demux finds 
in the control channel (time slot 0)
	\param[in]	pBuf - A buffer containing one control table
	\param[in]	BufSize - The size of the control table in pBuf
	\param[in]	SubframeIndex - The count of the control table inside the frame of TS0,
	\c				starting form 0. The first table will have 0, the second 1,
	\c				and so on. This value is not the Table ID.
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnControlTable)( UINT8* pBuf,
									UINT32 BufSize,
									UINT32 SubframeIndex,
									void* ClientPtr );

/*************************************************************************/
/*!
Callback to deliver a multiplex sub frame
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each sub frame
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
	\param[in]	pBuf - A buffer containing one sub frame. 
	\param[in]	BufSize - The size of the sub frame
	\c			When a multiplex frame holds just one service, this will be 0
	\param[in]	pSfHeader - A parsed multiplex sub frame header. See #SmsCmmbSubFrameHeader_ST
	\c						This pointer remains valid until all the callbacks related to 
	\c						this subframe are called. I.e. you can save it and still use it in 
	\c						callbacks for units and blocks of this sub frame.
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void

	\Note		This function is not used for reporting control tables.
*/
typedef void (*SmsDemuxCb_OnSubFrame)( UINT32 ServiceHandleNum,
									UINT32 SubframeIndex,
									UINT8* pBuf,
									UINT32 BufSize,
									const SmsCmmbSubFrameHeader_ST* pSfHeader,
									void* ClientPtr );

/*************************************************************************/
/*!
Callback to deliver a sub frame section
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each section in a sub frame. 
Each sub frame may contain a video section, an audio section and a data section.
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame that the section belongs to
	\param[in]	pBuf - A buffer containing the full section. 
	\param[in]	BufSize - The size of the section
	\param[in]	SectionType - Video, audio or data. See #SmsCmmbSectionType_EN enumerator
	\param[in]	pSectionHeader - A parsed section header. See #SmsCmmbSectionHeader_ST
	\c						This pointer remains valid until all the callbacks related to 
	\c						this section are called. I.e. you can save it and still use it in 
	\c						callbacks for units and blocks of this section.
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnSection)( UINT32 ServiceHandleNum,
									UINT32 SubframeIndex,
									UINT8* pBuf,
									UINT32 BufSize,
									SmsCmmbSectionType_EN SectionType,
									const SmsCmmbSectionHeader_ST* pSectionHeader,
									void* ClientPtr );

/*************************************************************************/
/*!
Callback to deliver a unit
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each unit
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
	\param[in]	pBuf - A buffer containing one unit. 
	\param[in]	BufSize - The size of the unit
	\param[in]	UnitType - Video, audio or data. See #SmsCmmbSectionType_EN enumerator
	\param[in]	pUnitParams - Parameters describing this unit. See #SmsCmmbUnitParams_ST
	\c						This pointer remains valid until all the callbacks related to 
	\c						this unit are called. I.e. you can save it and still use it in 
	\c						callbacks for multiplex blocks of this unit.
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnUnit)( UINT32 ServiceHandleNum,
								    UINT32 SubframeIndex,
									UINT8* pBuf,
									UINT32 BufSize,
									SmsCmmbSectionType_EN UnitType,
									const SmsCmmbUnitParams_ST* pUnitParams,
									void* ClientPtr );

/*************************************************************************/
/*!
Callback to deliver a multiplex block
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each multiplex block 
Note that multiplex blocks exist only in encapsulation mode 2.
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
	\param[in]	pBuf - A buffer containing one multiplex block. 
	\param[in]	BufSize - The size of the block
	\param[in]	pMpxBlockHeader - A parsed multiplex block header. See #SmsCmmbMpxBlock_ST
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnMultiplexBlock)( UINT32 ServiceHandleNum,
									UINT32 SubframeIndex,
									UINT8* pBuf,
									UINT32 BufSize,
									const SmsCmmbMpxBlock_ST* pMpxBlockHeader,
									void* ClientPtr );

/*************************************************************************/
/*!
Callback to deliver an RTP packet
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each RTP packet that the demux constructs.
When the multiplex sub frame is encrypted the demux cannot parse RTP packets,
so this function will not be called. See the #EncryptionMode in #SmsCmmbSubFrameHeader_ST.
In encapsulation mode 2 each RTP packet contains the payload on one multiplex block.
In encapsulation mode 1 each audio RTP packet contains one audio LATM unit.
In encapsulation mode 1 each video RTP packet contains one video NAL unit.
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
	\param[in]	pRtpPacket - A buffer containing one rtp packet. 
					The buffer starts with a 12-byte standard RTP header. There is no IP 
					or UDP headers contained in the buffer.
	\param[in]	PacketSize - The size of the packet
	\param[in]	PacketType - Video or audio. See #SmsCmmbSectionType_EN enumerator
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnRtpPacket)( UINT32 ServiceHandleNum,
									UINT32 SubframeIndex,
									UINT8* pRtpPacket,
									UINT32 PacketSize,
									SmsCmmbSectionType_EN PacketType,
									void* ClientPtr );


/*************************************************************************/
/*!
Callback to deliver an H264 NAL unit fragment
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each NAL unit fragment assembled by the demux.
The call does not have to contain a complete NAL unit. This is determined by
the IsNalUnitStart and IsNalUnitEnd parameters.
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
	\param[in]	pBuf - A buffer containing the NAL payload. 
	\param[in]	BufSize - The size of the payload
	\param[in]	IsNalUnitStart - When true, it means that this buffer is the start of a NAL unit
	\param[in]	IsNalUnitEnd - When true, it means that this buffer is the end of a NAL unit
	\c			When both IsNalUnitStart and IsNalUnitEnd are true it means that the buffer
	\c			contains a complete NAL packet
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnH264Nal)( UINT32 ServiceHandleNum,
								  UINT32 SubframeIndex,
								  UINT8* pBuf,
								  UINT32 BufSize,
								  BOOL IsNalUnitStart,
								  BOOL IsNalUnitEnd,
								  void* ClientPtr );

/*************************************************************************/
/*!
Callback to deliver an AAC LATM audio unit 
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each LATM unit in an audio stream
This call is valid only for AAC or AAC+ audio stream. It will not be called for
audio streams encoded by DRA format.
	\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
	\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
	\param[in]	pBuf - A buffer containing the LATM unit. 
	\param[in]	BufSize - The size of the LATM unit
	\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
	\return		void
*/
typedef void (*SmsDemuxCb_OnAacLatm)( UINT32 ServiceHandleNum,
									 UINT32 SubframeIndex,
									 UINT8* pBuf,
									 UINT32 BufSize,
									 void* ClientPtr );


/*************************************************************************/
/*!
Callback to deliver an XPE packet  
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each XPE packet 
\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
\param[in]	pBuf - A buffer containing the XPE packet 
\param[in]	BufSize - The size of the XPE packet
\param[in]	pXpePacket - A parsed XPE packet header. See #SmsCmmbXpePacket_ST
\param[in]	DataServiceMode - Data Service mode, See #SmsCmmbDataServiceMode_ET
\param[in]	pFatHeader - FAT header, See #SmsCmmbXpeFatHeader_ST
\param[in]	pSliceHeader - Slice header, See #SmsCmmbFileSliceHeader_ST
\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
\return		void
*/
typedef void (*SmsDemuxCb_OnXpe)( UINT32 ServiceHandleNum,
									 UINT32 SubframeIndex,
									 UINT8* pBuf,
									 UINT32 BufSize,
									 SmsCmmbXpePacket_ST *pXpePacket, 
									 SmsCmmbDataServiceMode_ET DataServiceMode,
									 SmsCmmbXpeFatHeader_ST* pFatHeader,
									 SmsCmmbFileSliceHeader_ST* pSliceHeader,
									 void* ClientPtr );


/*************************************************************************/
/*!
Callback to deliver an XPE packet  
This is a prototype of a callback supplied by the user in #SmsDemuxCallbacks_ST
It is called by the demux for each XPE packet 
\param[in]	ServiceHandleNum - The service handle given to #SmsCmmbDemuxProcessMpxFrame
\param[in]	SubframeIndex - the index of the sub frame in the multiplex frame
\param[in]	pBuf - A buffer containing the XPE packet 
\param[in]	BufSize - The size of the XPE packet
\param[in]	pXpePacket - A parsed XPE-FEC packet header. See #SmsCmmbXpeFecPacket_ST
\param[in]	ClientPtr - A user pointer. See comments for ClientPtr member in #SmsDemuxCallbacks_ST 
\return		void
*/
typedef void (*SmsDemuxCb_OnXpeFec)( UINT32 ServiceHandleNum,
									UINT32 SubframeIndex,
									UINT8* pBuf,
									UINT32 BufSize,
									SmsCmmbXpeFecPacket_ST *pXpeFecPacket, 
									void* ClientPtr );

typedef struct SmsDemuxCallbacks_S
{
	UINT32						StructSize;		// Size - use sizeof(SmsDemuxCallbacks_ST)
	void*	ClientPtr;	// Client pointer - set by user. This value is ignored 
						// the demux, but the demux passes it as an argument to each callback. 

	SmsDemuxCb_OnMultiplexFrame pfnOnMultiplexFrame;	// Multiplex frame
	SmsDemuxCb_OnControlTable	pfnOnControlTable;		// Control table
	SmsDemuxCb_OnSubFrame		pfnOnSubFrame;			// Sub Frame
	SmsDemuxCb_OnSection		pfnOnSection;			// Audio/Video/Data Section
	SmsDemuxCb_OnUnit			pfnOnUnit;				// Unit
	SmsDemuxCb_OnMultiplexBlock pfnOnMultiplexBlock;	// Multiplex Block
	SmsDemuxCb_OnRtpPacket		pfnOnRtpPacket;			// RTP Packet
	SmsDemuxCb_OnH264Nal		pfnOnH264Nal;			// H264 NAL fragment
	SmsDemuxCb_OnAacLatm		pfnOnAacLatm;			// AAC LATM frame
	SmsDemuxCb_OnXpe			pfnOnXpe;				// XPE packet
	SmsDemuxCb_OnXpeFec			pfnOnXpeFec;			// XPE-FEC packet

	// Reserved for future use
	void*						pfnRESERVED3;
	void*						pfnRESERVED4;

	// Last UINT32 must be zero
	UINT32						RESERVED_ZERO;
} SmsDemuxCallbacks_ST;

/*
Declare the callbacks object in the user code like this:
SmsDemuxCallbacks_ST CallbacksStruct = { sizeof(SmsDemuxCallbacks_ST), 0 };
*/

/*************************************************************************
*			 API Declarations
*************************************************************************/


/*************************************************************************/
/*!
Demultiplex a multiplex frame

This is the input function of the demux.
It gets a buffer containing one full multiplex frame as an argument.
It then demultiplexes the frame and calls the given callbacks for
each section of the mutliplex frame.

\param[in]	pCbStruct	A struct containing callback function pointers.
						Each pointer is used for delivering a different
						part of the multiplex frame.
						Each callback in the struct can be NULL.
						The user should set legal function pointers according
						to the formats that he wants to get.
						See #SmsDemuxCallbacks_ST
\param[in]	HandleNum	The service handle for the service
						being parsed.

\param[in]	pBuf		A pointer to a buffer containing a multiplex frame
\param[in]	BufSize		The size of the buffer pointed by pBuf	
\param[in]	RequiredSubFrameIndex - The subframe index of the service that 
						is needed. A multiplex frame may contain more than
						one service. This argument restricts the demux to
						one subframe containing the requested service.
						The subframe index of a service is obtained from
						the #SMSHOSTLIB_MSG_CMMB_START_SERVICE_RES response.
						To demultiplex all sub frames in the multiplex frame
						use the value SMS_CMMB_DEMUX_ANY_SUBFRAME_INDEX.

\return		Error code by  #SMSHOSTLIB_ERR_CODES_E enumerator.  

*/
SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E 
	SmsCmmbDemuxProcessMpxFrame( SmsDemuxCallbacks_ST* pCbStruct, 
							UINT32 HandleNum, UINT8* pBuf, UINT32 BufSize, UINT32 RequiredSubFrameIndex );

/*************************************************************************/
/*!
Change descrambled sub-frames in a multiplex frame into clear-to-air encapsulation mode 1 sub-frames. 
Helps players to process descrambled services.
\param[in]	pBuf		A pointer to a buffer containing a multiplex frame
\param[in]	BufSize		The size of the buffer pointed by pBuf	
\param[in]	pNewBufSize The size of the multiplex frames after the change. 

\return		Error code by  #SMSHOSTLIB_ERR_CODES_E enumerator.  

*/
SMSHOSTLIB_API SMSHOSTLIB_ERR_CODES_E SmsCmmbDemuxDescrambledToEncapMode1(UINT8 *pBuf, 
																		  UINT32 BufSize, UINT32 *pNewBufSize);


#endif //_SMS_CMMB_DEMUX_H_

//SMSHOSTLIB_API
#ifdef __cplusplus
}
#endif


