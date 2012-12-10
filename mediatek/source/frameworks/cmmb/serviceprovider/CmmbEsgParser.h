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

#ifndef CMMB_ESG_PARSER_H______
#define CMMB_ESG_PARSER_H______


#include "zlib.h"
#include "CmmbParser.h"
#include "CmmbHelper.h"


/*************************************************************************
*			macro
*************************************************************************/

#define	MTK_ESG_FIRST_TYPE				 0
#define	MTK_ESG_SERVICE_TYPE				 1
#define   MTK_CMMB_ESG_MAX_DATA_SECTIONS 256


#define HEAD_CRC     0x02 /* bit 1 set: header CRC present */
#define EXTRA_FIELD  0x04 /* bit 2 set: extra field present */
#define ORIG_NAME    0x08 /* bit 3 set: original file name present */
#define COMMENT      0x10 /* bit 4 set: file comment present */
#define RESERVED     0xE0 /* bits 5..7: reserved */
#define ESG_INFLATE_BUF_SIZE 256


typedef struct MTKCmmbEsgService_t
{
	UINT32 IndexId;
	UINT32 ServiceId;
}MTKCmmbEsgService;

typedef struct MTKCmmbEsgDataPayload_t
{
	UINT32 Length;
	BOOL bGzip;
	UINT8 *Data;
}MTKCmmbEsgDataPayload;

typedef struct MTKCmmbEsgDataBlock_t
{
	UINT32 Id;
	UINT32 VersionNumber;
	UINT32 EsgServiceIndexId;
	UINT32 NumSections;
	MTKCmmbEsgDataPayload *Sections[MTK_CMMB_ESG_MAX_DATA_SECTIONS];
}MTKCmmbEsgDataBlock;

typedef struct MTKCmmbEsgDataType_t
{
	UINT32 Id;
	UINT32 DataBlockQuantity;
	MTKCmmbEsgDataBlock *DataBlocks[MTK_ESG_MAX_DATABLOCKS_PER_TYPE];
}MTKCmmbEsgDataType;

typedef struct MTKCmmbEsgSliceHeader_t
{
	UINT8 DataBlockId;
	UINT8 DataTypeId;
	UINT8 CodeType;
	UINT8 SectionNumber;
	UINT8 TotalSectionNumber;
}MTKCmmbEsgSliceHeader;

typedef struct MTKCmmbEsgParser_t
{	
	CmmbResult Status;
	BOOL IsEncapMode1;

	UINT32 EsgServiceQuantity;
	MTKCmmbEsgService  EsgServices[MTK_ESG_MAX_ESG_SERVICES];
	UINT32 DataTypeQuantity;
	MTKCmmbEsgDataType DataTypes[MTK_ESG_MAX_ESG_DATATYPE];
}MTKCmmbEsgParser;

//! ESG XML block
typedef struct MTKCmmbEsgBlockXml_t
{
	UINT32 Len; //!< XML block buffer
	UINT8 *pBuf; //!< XML block buffer length

}MTKCmmbEsgBlockXml; 

//! ESG XML blocks of a single ESG types
typedef struct MTKCmmbEsgTypeXmls_t
{
	UINT32 NumBlocks; //!< Number of XML blocks
	MTKCmmbEsgBlockXml* pBlocks; //!< XML blocks of ESG type

}MTKCmmbEsgTypeXmls; 


// Multiplex Block Header
typedef struct MTKCmmbMpxBlock_t
{
	UINT16 FullLength;
	UINT16 HeaderLength;
	UINT8 BeginTag;
	UINT8 EndTag;
	UINT8 Type;
	UINT8 DataUnitType;
	
	UINT32 RESERVED1;
	UINT32 RESERVED2;
} MTKCmmbMpxBlock; 


CmmbResult  cmmb_esg_parser_init(void);

void  cmmb_esg_parser_deinit(void);

CmmbResult  cmmb_esg_getESGtableinfo(UINT8 *pTs0Buf, UINT32 Ts0BufSize, UINT32 pOutEsgServices[MTK_ESG_MAX_ESG_SERVICES], UINT32 *pOutNumEsgServices);

CmmbResult  cmmb_esg_parse_frame(	UINT8* p_buffer, UINT32 buff_size);

CmmbResult  cmmb_esg_getXmls(MTKCmmbEsgTypeXmls pXmls[MTK_ESG_MAX_ESG_DATATYPE]);

CmmbResult cmmb_esg_parse_unit(UINT8* pBuf,	UINT32 BufSize);

void  cmmb_esg_freeXmls(MTKCmmbEsgTypeXmls pXmls[MTK_ESG_MAX_ESG_DATATYPE]); 


#endif //CMMB_ESG_PARSER_H______


