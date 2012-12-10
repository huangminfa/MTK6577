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

/* ------------------------------------------------------------------
 * Copyright (C) Mediatek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
// -*- c++ -*-
// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

//				 O S C L_ S I M P L E_ P I P E	C L A S S
//
//	  This file contains simple pipe which simulate the linux standard 
//		pipe. This simple pipe (OsclSimplePipeImplTemplate) does not support cross- 
//		process in Win32

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclutil OSCL Util
 *
 * @{
 */


/*!
 * \file oscl_simple_pipe.h
 * \brief Provides simple pipe which simulates the linux stantard pipe
 *
 */

#ifndef OSCL_SIMPLE_PIPE_H_INCLUDED
#define OSCL_SIMPLE_PIPE_H_INCLUDED

#include "oscl_mem.h"
#include "oscl_semaphore.h"


enum eOsclPipeStatus
{
	PipeStatus_Err = -1,
	PipeStatus_OK = 0,
	PipeStatus_AlreadyHasData,
	PipeStatus_TimeOut
};

class OsclSimplePipe
{
public:
	OsclSimplePipe(){};
	virtual ~OsclSimplePipe(){};
	virtual bool initCheck() = 0;
	virtual uint32 write(void *pData, const uint32 nSize) = 0;
	virtual eOsclPipeStatus select(const uint32 nWaitMs) = 0;
	virtual uint32 read(void *pData, const uint32 nSize) = 0;
};

#ifdef WIN32
HANDLE CreateWritePipe(LPTSTR lpszPipeName);

class OsclSimplePipeImpl : public OsclSimplePipe
{
public:
	OsclSimplePipeImpl();
	~OsclSimplePipeImpl();
	virtual bool initCheck();
	virtual uint32 write(void *pData, const uint32 nSize);
	virtual eOsclPipeStatus select(const uint32 nWaitMs);
	virtual uint32 read(void *pData, const uint32 nSize);
private:
	HANDLE m_hReadPipe;
	HANDLE m_hWritePipe;
	OVERLAPPED m_ConnectOverlap;
	OVERLAPPED m_ReadOverlap;
	void clear();
	struct {
		bool bAvail;
		char data;
	} m_stub;				//the first byte in pipe if pipe available

};


#else

enum eOsclPipeID
{
	PipeID_Read = 0,
	PipeID_Write = 1
};

class OsclSimplePipeImpl : public OsclSimplePipe
{
public:
	OsclSimplePipeImpl();
	~OsclSimplePipeImpl();
	virtual bool initCheck();
	virtual uint32 write(void *pData, const uint32 nSize);
	virtual eOsclPipeStatus select(const uint32 nWaitMs);
	virtual uint32 read(void *pData, const uint32 nSize);
private:
	int mPipe[2];
	bool mInit;
};


#endif

#endif
