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

#ifndef CMMB_PARSER_H_
#define CMMB_PARSER_H_

#include "CmmbHelper.h"
#include "CmmbSPCommon.h"

#define DATASERVECE_DEBUG
/*************************************************************************
*			 Macros for control table
*************************************************************************/

#define ARRAY_SIZE(a) (sizeof(a)/sizeof(a[0]))

#define CMMB_MAX_CA_SYSTEMS (20)
#define CMMB_DEMUX_ANY_SUBFRAME_INDEX		0xFF

#define CMMB_MAX_NIT_NEIGH_NETWORKS (20)
#define CMMB_MAX_NIT_OTHER_FREQS (20)
#define CMMB_MAX_NETWORK_NAME_LEN (128)
#define CMMB_MAX_CSCT_SERVICES (80)
#define CMMB_MAX_SUBFRAMES_PER_FRAME (16)

#define CMMB_MAX_VIDEO_STREAMS_IN_SUBFRAME (8)
#define CMMB_MAX_AUDIO_STREAMS_IN_SUBFRAME (8)
#define CMMB_MAX_UNITS_NUM	(100)

/*****************************************************************************
 *   		       streaming 
 ****************************************************************************/
#define CMMB_PACKET_BUFFER_SIZE (30*1024)  //empirical value of the max frame size 30k
#define CMMB_VIDEOMEMPOOL_COUNT (75)
#define CMMB_AUDIOMEMPOOL_COUNT (75)

#define MID_OF_UNIT     0
#define END_OF_UNIT     1
#define START_OF_UNIT   2
#define WHOLE_UNIT      3

#define LEN_ADTS_HEADER 7
#define AUDIO_SAMPLES_PER_AAC_FRAME 1024

/* H.264 constant */
#define H264_START_CODE     0x01000000


#define NALU_TYPE_MASK  		0x1F
#define NALU_TYPE_CODED_SLICE_OF_IDR_PIC 5
#define NALU_TYPE_SPS           7
#define NALU_TYPE_PPS           8
#define NALU_TYPE_FILLER_DATA   12
#define NALU_TYPE_STAP_A        24
#define NALU_TYPE_STAP_B        25
#define NALU_TYPE_MTAP16        26
#define NALU_TYPE_MTAP24        27
#define NALU_TYPE_FU_A          28
#define NALU_TYPE_FU_B          29



/* frame types */
#define CMMB_VIDEO_FRAME_I          0
#define CMMB_VIDEO_FRAME_P          1
#define CMMB_VIDEO_FRAME_B          2

#define CMMB_MUX_MODE_1             1
#define CMMB_MUX_MODE_2             0
#define CMMB_MUX_MODE_2_START_CODE  0x55

#define CMMB_MUX_BLOCK_VIDEO        0
#define CMMB_MUX_BLOCK_AUDIO        1
#define CMMB_MUX_BLOCK_DATA         2
#define CMMB_MUX_BLOCK_OTHER        3
#define CMMB_MUX_BLOCK_ALL          0xFF
#define CMMB_TIMESTAMP_RESOLUTION   22500

/* DRA */
#define DRA_SYNC_WORD_0 0x7F
#define DRA_SYNC_WORD_1 0xFF

//ISMA
#define CMMB_ISMA_AV_CRC_OFFSET 3
#define CMMB_ISMA_HEADER_LEN (4)
#define CMMB_ISMA_V_HEADER_LEN (8)
#define CMMB_ISMA_A_HEADER_LEN (9)

#define CMMB_ISMA_V_TOTAL_LEN (CMMB_ISMA_HEADER_LEN + CMMB_ISMA_V_HEADER_LEN)
#define CMMB_ISMA_A_TOTAL_LEN (CMMB_ISMA_HEADER_LEN + CMMB_ISMA_A_HEADER_LEN)


typedef struct dra_frame_header_t
{
    UINT32 frame_len;
} dra_frame_header;

typedef struct cmmb_demux_nalu_context_t
{
    UINT8* header;      /* 16-bit length header */
} cmmb_demux_nalu_context_t;

/*************************************************************************
*			 Enums
*************************************************************************/

enum CMMB_CONTROL_TABLE_ID
{
	CMMB_NOT_SUPPORTED   = 0,
	CMMB_TABLE_SN_NIT    = 1,
	CMMB_TABLE_SN_CMCT   = 2,
	CMMB_TABLE_SN_CSCT   = 3,
	CMMB_TABLE_SN_SMCT   = 4,
	CMMB_TABLE_SN_SSCT   = 5,
	CMMB_TABLE_SN_ESG    = 6,
	CMMB_TABLE_SN_CA     = 7,
	CMMB_TABLE_SN_EMERGENCY  =0x10
};

enum
{
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_INIT_BCAST_TIME	= 7,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_VID_SECT_LEN		= 6,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_AUD_SECT_LEN		= 5,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_DATA_SECT_LEN		= 4,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_EXPANSION_AREA	= 3,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_SCRAMBLING0   	= 2,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_SCRAMBLING1   	= 1,
	MTK_CMMB_SUB_FRAME_INDICATION_BIT_PACK_MODE			= 0,

	MTK_CMMB_VID_EXP_INDICATION_CODE_RATE		= 3,
	MTK_CMMB_VID_EXP_INDICATION_PIC_DISP		= 2,
	MTK_CMMB_VID_EXP_INDICATION_RESOLUTION		= 1,
	MTK_CMMB_VID_EXP_INDICATION_FRAME_FREQ		= 0,

	MTK_CMMB_AUD_EXP_INDICATION_CODE_RATE		= 2,
	MTK_CMMB_AUD_EXP_INDICATION_SAMPLING_RATE	= 1,
	MTK_CMMB_AUD_EXP_INDICATION_STREAM_DESC		= 0,

	MTK_CMMB_VID_UNIT_INDICATION_FRAME_END		= 1,
	MTK_CMMB_VID_UNIT_INDICATION_REL_BCAST_TIME	= 0,
};

typedef enum MTKCmmbSectionType
{
	CMMB_SECTION_TYPE_VIDEO = 0,
	CMMB_SECTION_TYPE_AUDIO = 1,
	CMMB_SECTION_TYPE_DATA = 2,
	CMMB_SECTION_TYPE_RESERVED = 3,
} MTKCmmbSectionType;

typedef enum MTKCmmbDataUnitType
{
	CMMB_DATA_UNIT_TYPE_ESG = 0,
	CMMB_DATA_UNIT_TYPE_ECM_EMM_START = 128,
       CMMB_DATA_UNIT_TYPE_ECM_EMM_END  = 159,
	CMMB_DATA_UNIT_TYPE_XPE = 160, 
	CMMB_DATA_UNIT_TYPE_XPE_FEC = 161, 
}MTKCmmbDataUnitType; 

/*************************************************************************
*			 Structs for Mpx frame 
*************************************************************************/
// Multiplex frame header
typedef struct MTKCmmbMultiplexFrameHeader
{
	UINT8	HeaderLengthField;		
	UINT8	ProtocolVersion;		
	UINT8	MinimumProtocolVersion;	
	UINT8	MultiplexFrameID;		
	UINT8	EmergencyBroadcastIndication;	
	UINT8	NextFrameParameterIndication;	
	UINT8	InitialLead;
	UINT8	NIT_SeqNum;		//4
	UINT8	CMCT_SeqNum;	//4
	UINT8	CSCT_SeqNum;	//4
	UINT8	SMCT_SeqNum;	//4
	UINT8	SSCT_SeqNum;	//4
	UINT8	ESG_SeqNum;		//4
	UINT8	CA_SeqNum;		//4 
	UINT8	NumOfSubFrames;	//4
	UINT8	NextFrameParamHeaderLength;		
	UINT8	NextFrameParamMsf1HeaderLength;	
	UINT32	SubFrameLength[16];				
	UINT32	NextFrameParamMsf1Length;		
	UINT32	TotalFrameLength;	
	UINT32	RESERVED1;
	UINT32	RESERVED2;
} MTKCmmbMultiplexFrameHeader;

// Video expansion area in sub frame header
typedef struct MTKCmmbVidExpArea
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
} MTKCmmbVidExpArea;

// Audio expansion area in sub frame header
typedef struct MTKCmmbAudExpArea
{
	UINT8	AudType;		
	UINT8	Indications;
	UINT8	SamplingRate;
	UINT16	CodeRate;
	UINT32	AudioDesc;
	UINT32	RESERVED1;
	UINT32	RESERVED2;
} MTKCmmbAudExpArea;

// Sub frame header
typedef struct MTKCmmbSubFrameHeader
{
	UINT8	SubframeIndex;		
	UINT8	HeaderLengthField;
	UINT8	Indications;
	UINT8	EncryptionMode; 
	UINT8	IsEncapMode1;
	UINT8	TotalVidStreams;
	UINT8	TotalAudStreams;
	UINT32	InitBcastTime;
	UINT32	VidSectLen;
	UINT32	AudSectLen;
	UINT32	DataSectLen;
	UINT32  RESERVED1;
	UINT32  RESERVED2;

	MTKCmmbVidExpArea	VidStreamsDesc[CMMB_MAX_VIDEO_STREAMS_IN_SUBFRAME];
	MTKCmmbAudExpArea	AudStreamsDesc[CMMB_MAX_AUDIO_STREAMS_IN_SUBFRAME];
} MTKCmmbSubFrameHeader;

// Audio/Video/Data unit parameters
// Important note - this struct is held 100 times on stack in an array, its size must not be extended.
// This structure holds the same fields for audio, video and data units, but not all fields are relevant to each type.
// These parameters are parsed from the section header.
typedef struct MTKCmmbUnitParams
{
	UINT16	UnitLengthField;	       // The length of the unit
	UINT16	RelativeBcastTime;	// Relevant only to audio/video units. The relative time for broadcasting this unit. 
	UINT8	StreamNum;			// Relevant only to audio/video units. 
	UINT8	VideoFrameType;	// Relevant only to video units
	UINT8	VideoIndications;	// Relevant only to video units
	UINT8   DataUnitType;		// Relevant only to data units
} MTKCmmbUnitParams;

// Section Header
typedef struct MTKCmmbSectionHeader
{
	UINT32 NumUnits;
	UINT32 HeaderLengthIncludingCrc;
	UINT32 RESERVED1;
	UINT32 RESERVED2;
	MTKCmmbUnitParams  UnitsParamsArr[CMMB_MAX_UNITS_NUM];
} MTKCmmbSectionHeader;


//CA--------------------------------
typedef struct MTKCaSysDesc
{
	UINT16 CaSysId;
	UINT16 EmmServiceId;
	UINT8 EmmDataType;
	UINT8 EcmDataType;
	UINT8 EcmTransportType;
} MTKCaSysDesc;

typedef struct MTKCaDescTable
{
	UINT16 HeaderLengthField;
	UINT8 UpdateNumber;
	UINT8 SectionNumber;
	UINT8 SectionQuantity;
	UINT8 NumSystems;
	unsigned char* Section_Orig_Stream;
	MTKCaSysDesc CaSystemsArr[CMMB_MAX_CA_SYSTEMS];
} MTKCaDescTable;

//ESG--------------------------------
#define	MTK_ESG_MAX_ESG_DATATYPE		15
#define   MTK_ESG_MAX_ESG_SERVICES		15
#define   MTK_ESG_MAX_DATABLOCKS_PER_TYPE 128
typedef struct MTKEsgServiceDesc
{
	UINT8 Index;
	UINT16 ServiceId;
} MTKEsgServiceDesc;

typedef struct MTKEsgDataBlock
{
	UINT8 Id;
	UINT8 VerionNum;
	UINT8 EsgServiceIndexId;
} MTKEsgDataBlock;

typedef struct MTKEsgDataType
{
	UINT8 TypeId;
	UINT8 NumBlocks;
	MTKEsgDataBlock BlocksArr[MTK_ESG_MAX_DATABLOCKS_PER_TYPE];
} MTKEsgDataType;

typedef struct MTKEsgTable
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
	MTKEsgServiceDesc EsgServicesArr[MTK_ESG_MAX_ESG_SERVICES];
	MTKEsgDataType DataTypesArr[MTK_ESG_MAX_ESG_DATATYPE];
} MTKEsgTable;

//CMCT--------------------------------
typedef struct MTKCmmbParserMpxFrame
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

	UINT8	SubFrameNumsArr[CMMB_MAX_SUBFRAMES_PER_FRAME];
	UINT16	SubFrameServiceIdsArr[CMMB_MAX_SUBFRAMES_PER_FRAME];
} MTKCmmbParserMpxFrame;

typedef struct MTKCmmbParserInfo_CMCT
{
	UINT8	UpdateSeqNum;
	UINT8	FreqPointNum;
	UINT8	NumMpxFrames;
	MTKCmmbParserMpxFrame MultiplexFramesArr[40];
} MTKCmmbParserInfo_CMCT;

//CSCT--------------------------------
typedef struct MTKCmmbParserInfo_CSCT
{
	UINT8	UpdateSeqNum;	//4
	UINT8	SectionNumber;	
	UINT8	NumSections;
	UINT16	SectionLength;	
	UINT16	NumServices;
	UINT16  ServiceIdArr[CMMB_MAX_CSCT_SERVICES];
	UINT8   ServiceFreqPtArr[CMMB_MAX_CSCT_SERVICES];
} MTKCmmbParserInfo_CSCT;

//NIT--------------------------------
typedef struct MTKCmmbFreqPoint
{
	UINT8	Number;		
	UINT8	Bandwidth;		//4
	UINT32	CenterFreq;		
} MTKCmmbFreqPoint;

typedef struct MTKNetworkDesc
{
	UINT16				NetworkId;
	MTKCmmbFreqPoint	Freq;
} MTKNetworkDesc;

typedef struct MTKCmmbParserInfo_NIT
{
	UINT8				UpdateSeqNum;		
	UINT8				NumOfNeighNetworks;
	UINT8				NumOfOtherFreqPoints;
	UINT8				NetworkIdMode;
	UINT32 				SystemTimeHigh;		
	UINT32 				SystemTimeLowByte;	
	UINT32 				CountryCode;			
	UINT16 				NetworkId;			
	MTKCmmbFreqPoint	ThisFreqPoint; 
	UINT8 				NetworkNameLen;		
	UINT8  				NetworkName[CMMB_MAX_NETWORK_NAME_LEN];
	MTKCmmbFreqPoint      OtherFreqPtsArr[CMMB_MAX_NIT_OTHER_FREQS];
	MTKNetworkDesc	       NeigNetworksArr[CMMB_MAX_NIT_NEIGH_NETWORKS];
} MTKCmmbParserInfo_NIT;

//EB--------------------------------
typedef struct MTKCmmbParserInfo_EB
{
  unsigned char   nTableSn;
  unsigned char   nConcurrentMsgCount;
  unsigned char   nSquence;   
  unsigned short  wLen;     
  unsigned char*  lpData;
}MTKCmmbParserInfo_EB;

typedef struct Demuxer{
 // unsigned char       m_subFrameId;   //The current subframe ID
  MTKCmmbParserInfo_NIT           m_Nit;    //Network Information
  MTKEsgTable          m_Esg;    //ESG Basic Description Table
  MTKCmmbParserInfo_EB            m_Eb;   //Emergency Message
  MTKCmmbParserInfo_CMCT          m_CMct;   //Continual Multiplex Configuration Table
  MTKCmmbParserInfo_CMCT           m_SMct;   // Short time Multiplex Configuration Table
  MTKCmmbParserInfo_CSCT          m_CSct;    // Continual Service Configuration Table
  MTKCmmbParserInfo_CSCT           m_SSct;   // Short time Service Configuration Table
  MTKCaDescTable            m_Ca;   //Conditional Acception
}Demuxer;

//SPS 
typedef struct Bitstream
{  
  int           read_len;           //!< actual position in the codebuffer, CABAC only
  int           code_len;           //!< overall codebuffer length, CABAC only
  int           frame_bitoffset;    //!< actual position in the codebuffer, bit-oriented, UVLC only
  int           bitstream_length;   //!< over codebuffer lnegth, byte oriented, UVLC only
  UINT8      *streamBuffer;      //!< actual codebuffer for read bytes
  int           ei_flag;            //!< error indication, 0: no error, else unspecified error
} Bitstream;


/*****************************************************************************
 * ListNode
 ****************************************************************************/
typedef struct _CMMB_ListNode {
	struct _CMMB_ListNode    *prev;
	struct _CMMB_ListNode    *next;
} CMMB_ListNode;

__inline void cmmb_list_init(CMMB_ListNode *list_head)
{
  list_head->next = list_head;
  list_head->prev = list_head;
}

__inline void cmmb_list_add(CMMB_ListNode *new_node, CMMB_ListNode *head_node)
{  
  CMMB_ListNode *prev_node;
  CMMB_ListNode *next_node;
  
  prev_node = head_node ;
  next_node = head_node->next;
  
  next_node->prev = new_node;
  new_node->next = next_node;
  new_node->prev = prev_node;
  prev_node->next = new_node;
}

__inline void cmmb_list_add_tail(CMMB_ListNode *new_node, CMMB_ListNode *head_node)
{
  CMMB_ListNode *prev_node;
  CMMB_ListNode *next_node;
  
  prev_node = head_node->prev ;
  next_node = head_node;
  
  next_node->prev = new_node;
  new_node->next = next_node;
  new_node->prev = prev_node;
  prev_node->next = new_node;
}

__inline void cmmb_list_del(CMMB_ListNode *entry)
{
  CMMB_ListNode * prev;
  CMMB_ListNode * next;
  prev = entry->prev;
  next = entry->next;
  
  next->prev = prev;
  prev->next = next;  
}

/*****************************************************************************
 *    Struct for A/V frame info
 ****************************************************************************/  
typedef struct _CMMB_AudioUnitStruct {
  CMMB_ListNode     list;
  UINT16        AudioUnitLen;
  UINT8         AudioSeq;
  UINT16        RelatePlayTime;
  UINT8*        AudioUnit;
  UINT32       timestamp;
} CMMB_AudioUnitStruct;

typedef struct _CMMB_AudioFrameParameterStruct {
  CMMB_ListNode     list;
  UINT16        AudioFrameParameterFlag;
  UINT8         AudioAlgo ;
  UINT16        AudioCodeRate;
  UINT8         AudioSampleRate;
  UINT32        AudioLanguage; 
} CMMB_AudioFrameParameterStruct;

typedef struct _CMMB_AudioFrameStruct {
  CMMB_ListNode     AudioFrameParameter_header;     
  CMMB_ListNode     AudioUnit_header;   
}CMMB_AudioFrameStruct;

typedef struct _CMMB_VideoUnitStruct {
  CMMB_ListNode     list;
  UINT16        VideoUnitLen;
  UINT8         VideoFrameType;
  UINT8         VideoSeq;
  UINT8         IsEnd;
  UINT8         ExistRelatePlayTime;
  UINT16        RelatePlayTime;
  UINT8*        VideoUnit;
  UINT32       timestamp;
} CMMB_VideoUnitStruct;

typedef struct _CMMB_VideoFrameParameterStruct{
  CMMB_ListNode   list;
  UINT16      VideoFrameParameterFlag;
  UINT8       VideoAlgo;
  UINT16      VideoCodeRate;
  UINT8       ImageXStart;
  UINT8       ImageYStart;
  UINT8       ImagePriority;
  UINT16      HResolution;
  UINT16      VResolution;
  UINT8       FrameFreq;
} CMMB_VideoFrameParameterStruct;

typedef struct _CMMB_VideoFrameStruct {
  CMMB_ListNode     VideoFrameParameter_header;
  CMMB_ListNode     VideoUnit_header;             
} CMMB_VideoFrameStruct;

typedef struct _CMMB_StreamFrameStruct {
  UINT16               ServiceId ;
  UINT8                EnableScrambling;
  UINT8                PacketMode;
  UINT8                ExistDataECM;
  UINT8                ExistStartPlayTime;
  UINT32               StartPlayTime;        
  CMMB_VideoFrameStruct*   VideoFrame;
  CMMB_AudioFrameStruct*   AudioFrame; 
} CMMB_StreamFrameStruct;

typedef struct cmmb_subframe_video_stream_t
{
    UINT32 algorithm_type ;
    UINT32 bitrate_flag ;
    UINT32 picture_display_flag ;
    UINT32 frame_resolution_flag ;
    UINT32 frame_rate_flag ;

    UINT16 bitrate;
    UINT8 x_coord;
    UINT8 y_coord;
    UINT8 disp_priority;
    UINT16 x_resolution;
    UINT16 y_resolution;
    UINT8 frame_rate;	
    /* extra */
    cmmb_h264_dec_config* h264;
} cmmb_subframe_video_stream_t;

 typedef struct cmmb_subframe_audio_stream_t
{
	UINT32 algorithm_type ;
    UINT32 bitrate_flag;
    UINT32 sample_rate_flag;
    UINT32 stream_desc_flag;

    UINT32 sample_rate;
    UINT16 bitrate;
    UINT32 desc;
} cmmb_subframe_audio_stream_t;

typedef struct cmmb_subframe_header_t
{ 
    UINT8 header_len;
    UINT32 start_time_flag : 1;
    UINT32 video_seg_flag : 1;
    UINT32 audio_seg_flag : 1;
    UINT32 data_seg_flag : 1;
    UINT32 ext_zone_flag : 1;
    UINT32 scramble_flag : 2;
    UINT32 encapsulation_mode : 1;

    UINT32 start_time;

    UINT32 video_seg_len;
    UINT8 video_stream_count;
    cmmb_subframe_video_stream_t video_streams[CMMB_MAX_ELEMENTARY_STREAMS];

    UINT32 audio_seg_len;
    UINT8 audio_stream_count;
    cmmb_subframe_audio_stream_t audio_streams[CMMB_MAX_ELEMENTARY_STREAMS];

    UINT32 data_seg_len;
    UINT32 ecm_flag;
    UINT32 crc32;
} cmmb_subframe_header_t;

// Generic data packet that is used to pass any data with timestamp.
typedef struct cmmb_packet_t
{
    UINT8* data;        /**< data pointer */
    UINT32 size;        /**< data size in bytes */
    UINT32 timestamp;   /**< the timestamp */
    UINT32 flags;       /**< flags to indicate some special packets. @see cmmb_packet_flag_t */
} cmmb_packet_t;

typedef struct cmmb_demux_t
{
    UINT8* packet_buf;          /**< as packet buffer */
    UINT32 packet_buf_size;
	
    cmmb_subframe_header_t *sfh;

    cmmb_packet_t pkt;
    UINT32 current_stream_idx;

    /* video statistics */
    UINT32 num_video_units;
    UINT32 video_unit_bytes;

    UINT32 video_stream_count;
    cmmb_subframe_video_stream_t* video_streams;
    UINT32 audio_stream_count;
    cmmb_subframe_audio_stream_t* audio_streams;

    cmmb_h264_dec_config* h264_dec_config[CMMB_MAX_ELEMENTARY_STREAMS];

    /* audio statistics */
    UINT32 num_audio_units;
    UINT32 audio_unit_bytes;

    /* data statistics */
    UINT32 num_data_units;
    UINT32 data_unit_bytes;

    /* unknown data statistics */
    UINT32 num_unknown_units;
} cmmb_demux_t;

typedef enum
{
    CMMB_DEMUX_VIDEO,
    CMMB_DEMUX_AUDIO    
} cmmb_demux_type_t;

// Flags that can be tagged on a packet.
typedef enum
{
    cmmb_PACKET_FROM_CL_TDMB_DEMUX        = 0x0001,   /**< this packet is from CyberLink's demux */
    cmmb_PACKET_WITH_ADTS_HEADER          = 0x0002,   /**< this packet contains ADTS header */
    cmmb_PACKET_WITH_H264_START_CODE      = 0x0004,   /**< this packet contains H.264 start code */
    cmmb_PACKET_WITH_RANDOM_ACCESS_POINT  = 0x0008,   /**< this packet contains random access point of elementary stream */
    cmmb_PACKET_IS_CONCEALMENT            = 0x0010,   /**< this packet is an audio/video concealment */
    cmmb_PACKET_AGGREGATION               = 0x0020,   /**< this packet contains multiple parts which are prefixed by 16-bit uint size information */
    cmmb_PACKET_RECEIVER_SHOULD_FREE      = 0x0040    /**< indicates that the receiver of this packet should free the buffer */
} cmmb_packet_flag_t;


/*************************************************************************
*			structure for data service
*************************************************************************/
struct CXpe
{
    UINT8 startFlag;
    UINT8 endFlag;
    UINT8 flag; // 0 means XPE, while 1 means XPE_FEC
    UINT8 fecFlag;
    UINT16 payloadlen;    
    
    UINT8 crcFlag;
    UINT8 transSN;
    UINT16 dataLength;  //exist if ((startFlag==1) && (endFlag==0))
    UINT8 serviceMode; //1-bit, if startFlag==1
   
    struct
    {
        UINT8 FEC_type;
        UINT8 FEC_Length;
        UINT8 FEC[255];
    }fec;
    UINT8 checksum8;
    bool ParseHeader(UINT8*& rlpIn, UINT32 len);
    UINT8 header_len;
};


/*************************************************************************
*			structure for CA
*************************************************************************/

/* this is the struct for mtk ecm parser - input */
typedef struct
{
    bool            is_first;                           /* first ecm or not */
    UINT32        odd_ki_len;                         /* odd key indicator length */                             
    UINT32        even_ki_len;                        /* even key indicator length */
    UINT32        ecm_len;                            /* length of ecm */
    UINT8          *ecm;                               /* ecm raw data */
} mbbms_mtk_parse_req_struct;
/* this is the struct for mtk ecm parser - output */
#define MBBMS_MTK_MIKEY_LENGTH                  (256*2+1)   /* as per siano api */
typedef struct
{
    UINT32          id_odd;                             /* key indicator of odd */
    UINT32          id_even;                            /* key indicator of even */
    UINT8           mtk_odd[MBBMS_MTK_MIKEY_LENGTH];    /* mtk mikey of odd */
    UINT8           mtk_even[MBBMS_MTK_MIKEY_LENGTH];   /* mtk mikey of even */
} mbbms_mtk_parse_rsp_struct;

/*************************************************************************
*			parser functions
*************************************************************************/
CmmbResult	cmmb_msf_parse_data_section_header(UINT8* pBuf,	UINT32 BufSize, MTKCmmbSectionHeader* pOutHeader );
CmmbResult	cmmb_msf_parse_video_section_header(UINT8* pBuf, UINT32 BufSize, MTKCmmbSectionHeader* pOutHeader );
CmmbResult	cmmb_msf_parse_audio_section_header(UINT8* pBuf, UINT32 BufSize,MTKCmmbSectionHeader* pOutHeader );
CmmbResult	cmmb_msf_parse_subframe_header( UINT8* pBuf, UINT32 BufSize, MTKCmmbSubFrameHeader* pOutHeader );
CmmbResult	cmmb_mfs_parse_frame_header( UINT8* pBuf, UINT32 BufSize, MTKCmmbMultiplexFrameHeader* pOutFrameHeader );
CmmbResult   cmmb_mfs_parse_frame( UINT8* pBuf, UINT32 BufSize, UINT32 RequiredSubFrameIndex );

CmmbResult	cmmb_msf_parse_CA( UINT8* pBuf, UINT32 BufSize, MTKCaDescTable* pOutHeader );
CmmbResult	cmmb_msf_parse_ESG( UINT8* pBuf, UINT32 BufSize, MTKEsgTable* pOutEsgTable );
CmmbResult   cmmb_msf_parse_xMCT(UINT8* pBuf, UINT32 BufSize, MTKCmmbParserInfo_CMCT* pOutCmct );
CmmbResult   cmmb_msf_parse_xSCT( UINT8* pBuf, UINT32 BufSize, MTKCmmbParserInfo_CSCT* pOutCsct );
CmmbResult   cmmb_msf_parse_NIT( UINT8* pBuf, UINT32 BufSize, MTKCmmbParserInfo_NIT* pOutNit );
CmmbResult	cmmb_msf_parse_EB( UINT8* pBuf, UINT32 BufSize, MTKCmmbParserInfo_EB* pOutEBTable );
void cmmb_send_audiostream(CMMB_AudioUnitStruct* & update_audiounit);
bool cmmb_alloc_audiomem(CMMB_AudioUnitStruct* & update_audiounit);


/*************************************************************************
*			parser functions for service provider
*************************************************************************/

BOOL AudioWaitUnitEvent();
BOOL VideoWaitUnitEvent();
void SetGetFrameEvent();
void ClearUnitEvent();
void SetMpxFramewaitStatus(bool needwait);
void CmmbParserInitForStartService();
void CmmbParserInitForInit();
MTKCmmbParserMpxFrame* FindMpxFrameInfo(UINT32 serviceId);
bool CmmbAVBufferAvaild(UINT32  UsedCount);
#endif //CMMB_PARSER_H_


