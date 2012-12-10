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
 * Copyright (C) 1998-2009 PacketVideo
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
#ifndef PVLOGGER_ACCESSORIES_H_INCLUDED
#define PVLOGGER_ACCESSORIES_H_INCLUDED

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef PVLOGGER_H_INCLUDED
#include "pvlogger.h"
#endif

/**
 * Base class for all message formatters. This class defines the interface to
 * the message formatter. There are two kinds of msg formatting APIs, one to
 * format text messages, and other to format opaque message buffers.
 */
class PVLoggerLayout
{
    public:
        typedef PVLogger::message_id_type message_id_type;

        virtual ~PVLoggerLayout() {}

        /**
         * Formats the string and copies it to the given buffer.
         *
         * @return The length of the string not including the trailing '\0'
         */
        virtual int32 FormatString(char* formatBuf, int32 formatBufSize,
                                   message_id_type msgID, const char * fmt,
                                   va_list va) = 0;

        /**
         * Formats the data and copies it to the given buffer.
         *
         * @return The length of the buffer used.
         */
        virtual int32 FormatOpaqueMessage(char* formatBuf, int32 formatBufSize,
                                          message_id_type msgID, int32 numPairs,
                                          va_list va) = 0;
};

/**
 * Base class for all message filters. This class defines the interface to
 * the message filters. There are two kinds of msg filtering APIs, one to
 * filter text messages, and other to filter opaque message buffers.
 */
class PVLoggerFilter
{
    public:
        virtual ~PVLoggerFilter() {}

        typedef PVLogger::message_id_type message_id_type;
        typedef PVLogger::log_level_type log_level_type;
        typedef PVLogger::filter_status_type filter_status_type;

        virtual filter_status_type FilterString(char* tag, message_id_type msgID, log_level_type level) = 0;
        virtual filter_status_type FilterOpaqueMessge(char* tag, message_id_type msgID, log_level_type level) = 0;
};

const PVLoggerFilter::filter_status_type PVLOGGER_FILTER_ACCEPT = 1;
const PVLoggerFilter::filter_status_type PVLOGGER_FILTER_REJECT = 2;
const PVLoggerFilter::filter_status_type PVLOGGER_FILTER_NEUTRAL = 3;

/**
 * Example filter that allows all messages to be logged.
 */
class AllPassFilter : public PVLoggerFilter
{
    public:
        typedef PVLoggerFilter::message_id_type message_id_type;
        typedef PVLoggerFilter::log_level_type log_level_type;
        typedef PVLoggerFilter::filter_status_type filter_status_type;

        AllPassFilter() {};
        virtual ~AllPassFilter() {};

        filter_status_type FilterString(char* tag, message_id_type msgID, log_level_type level)
        {
            OSCL_UNUSED_ARG(tag);
            OSCL_UNUSED_ARG(msgID);
            OSCL_UNUSED_ARG(level);
            return (PVLOGGER_FILTER_ACCEPT);
        };
        filter_status_type FilterOpaqueMessge(char* tag, message_id_type msgID, log_level_type level)
        {
            OSCL_UNUSED_ARG(tag);
            OSCL_UNUSED_ARG(msgID);
            OSCL_UNUSED_ARG(level);
            return (PVLOGGER_FILTER_ACCEPT);
        };
};

/**
 * Base class for all message appenders. This class defines the interface to
 * the message appenders. There are two kinds of msg appender APIs, one to
 * append text messages, and other to append opaque message buffers.
 */
class PVLoggerAppender
{
    public:
        typedef PVLogger::message_id_type message_id_type;

        virtual ~PVLoggerAppender() {}

        virtual void AppendString(message_id_type msgID, const char *fmt, va_list va) = 0;
        virtual void AppendBuffers(message_id_type msgID, int32 numPairs, va_list va) = 0;
};


#endif

