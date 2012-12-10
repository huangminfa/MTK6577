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

/*******************************************************************************
 *
 * Filename:
 * ---------
 *   sharedmem.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of sharedmem
 *
 * Author:
 * -------
 *   Siyang.Miao (MTK80734) 03/08/2011
 *
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 *
 *
 *******************************************************************************/
 
#ifndef __MDL_SHARED_MEM_H__
#define __MDL_SHARED_MEM_H__

#include "mdltypes.h"

namespace mdlogger
{
	typedef unsigned char SIZE_BUF_NUM;

	typedef struct
	{
		unsigned int header;
		unsigned short ctrlBufLen;
		unsigned short version;
		unsigned short mdCmd;
		unsigned short mdAck;
		unsigned short apCmd;
		unsigned short apAck;
		unsigned char apStatus;
		unsigned char mdStatus;
		SIZE_BUF_NUM m2aBufCnt;
		SIZE_BUF_NUM a2mBufCnt;
		unsigned int reserved;
	} SHARED_MEMORY_HEADER;

	typedef struct
	{
		unsigned int ptr;
		unsigned int len;
		unsigned int start;
		unsigned int end;
	} BUF_CTRL_BLOCK;

	class SharedMem
	{
	private:
		unsigned int m_nShmLen;
		unsigned int m_nShmPhyAddr;
		unsigned int m_nShmVirAddr;
		SHARED_MEMORY_HEADER *m_pShmHeader;
		BUF_CTRL_BLOCK **m_pM2ABufCtrlBlocks;
		BUF_CTRL_BLOCK **m_pA2MBufCtrlBlocks;
		int m_nPhy2VirOffset;
	public:
		static const unsigned short MD_CMD_MEM_DUMP_READY 	= 0x0001;
		static const unsigned short MD_CMD_MEM_DUMP_OVER 	= 0x0002;
		static const unsigned short AP_ACK_MEM_DUMP_READY 	= 0x0001;

		static const unsigned char AP_STATUS_BUSY = 0x01;
		static const unsigned char AP_STATUS_IDLE = 0x00;
		SharedMem();
		~SharedMem();
		MDL_BOOL init(unsigned int len, unsigned int phyAddr, unsigned int virAddr);
		MDL_BOOL deinit();
		inline SHARED_MEMORY_HEADER *header(){return m_pShmHeader;}
		BUF_CTRL_BLOCK *m2aBufCtrl(SIZE_BUF_NUM bufId);
		BUF_CTRL_BLOCK *a2mBufCtrl(SIZE_BUF_NUM bufId);
		int offset();
		unsigned char getM2ABufCnt();
		unsigned char getA2MBufCnt();
		unsigned short getMDCmd();
		unsigned short getMDAck();
		void setAPCmd(unsigned short apCmd);
		void setAPAck(unsigned short apAck);
		unsigned int getA2MPtr(SIZE_BUF_NUM bufId);
		unsigned int getA2MLen(SIZE_BUF_NUM bufId);
		unsigned int getA2MStart(SIZE_BUF_NUM bufId);
		unsigned int getA2MEnd(SIZE_BUF_NUM bufId);
//		void setA2MPtr(SIZE_BUF_NUM bufId, unsigned int val);
//		void setA2MLen(SIZE_BUF_NUM bufId, unsigned int val);
//		void setA2MStart(SIZE_BUF_NUM bufId, unsigned int val);
//		void setA2MEnd(SIZE_BUF_NUM bufId, unsigned int val);
		unsigned int getM2APtr(SIZE_BUF_NUM bufId);
		unsigned int getM2ALen(SIZE_BUF_NUM bufId);
		unsigned int getM2AStart(SIZE_BUF_NUM bufId);
		unsigned int getM2AEnd(SIZE_BUF_NUM bufId);
//		void setM2APtr(SIZE_BUF_NUM bufId, unsigned int val);
//		void setM2ALen(SIZE_BUF_NUM bufId, unsigned int val);
//		void setM2AStart(SIZE_BUF_NUM bufId, unsigned int val);
//		void setM2AEnd(SIZE_BUF_NUM bufId, unsigned int val);

	};
}

#endif
