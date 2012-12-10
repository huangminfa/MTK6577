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

/***********************************************************************
Filename   : rvusrconfig.h
Description: let the user to put his own choices
************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/
#ifndef RV_USRCONFIG_H
#define RV_USRCONFIG_H

/***********************************************************************

    3G-324M Specific definitions

***********************************************************************/
#include "rvosdefs.h"

#define WNSRP_NONE      0
#define WNSRP_NEC       1
#define WNSRP_TYPE WNSRP_NEC

/* H.223 Split add-on module support */
#ifndef RV_H223_USE_SPLIT
#define RV_H223_USE_SPLIT RV_NO
#endif

/* Real-time statistics support */
#undef RV_H223_USE_STATISTICS
#define RV_H223_USE_STATISTICS RV_YES

/* Multiplexing levels support */
#undef RV_H223_USE_MUX_LEVEL_0
#define RV_H223_USE_MUX_LEVEL_0 RV_YES

#undef RV_H223_USE_MUX_LEVEL_1
#define RV_H223_USE_MUX_LEVEL_1 RV_YES

#undef RV_H223_USE_MUX_LEVEL_2
#define RV_H223_USE_MUX_LEVEL_2 RV_YES

#undef RV_H223_USE_MUX_LEVEL_3
#define RV_H223_USE_MUX_LEVEL_3 RV_NO

/* The maximum size of an audio frame size we can easily mux along with segmentable channels */
#undef RV_H223_MAX_AUDIO_FRAME_SIZE
#define RV_H223_MAX_AUDIO_FRAME_SIZE    (33)

/* The maximum number of different audio frame sizes we can have in the mux-table */
#undef RV_H223_MAX_AUDIO_SIZES
#define RV_H223_MAX_AUDIO_SIZES         (8)

/* The maximum size of an H.245 message that is supported. Should be 255-2048 */
#undef RV_H223_MAX_H245_MESSAGE_SIZE
#define RV_H223_MAX_H245_MESSAGE_SIZE   (2048)


/* definitions for supporting H.225 or H.223 */
#define RV_H245_SUPPORT_H225_PARAMS 0
#define RV_H245_SUPPORT_H223_PARAMS 1

/* H.245 definitions for capability set and for capability descriptors arrays */
#define RV_H245_CAP_SET_SIZE 30
#define RV_H245_CAP_DESC_SIZE 30

/* GEF add-on module codecs support */

#undef RV_GEF_USE_AMR
#define RV_GEF_USE_AMR RV_YES

#undef RV_GEF_USE_G7221
#define RV_GEF_USE_G7221 RV_NO

#undef RV_GEF_USE_G7222
#define RV_GEF_USE_G7222 RV_NO

#undef RV_GEF_USE_G726
#define RV_GEF_USE_G726 RV_NO

#undef RV_GEF_USE_H239
#define RV_GEF_USE_H239 RV_NO

#undef RV_GEF_USE_H264
#define RV_GEF_USE_H264 RV_NO

#undef RV_GEF_USE_H324ANNEXI
#define RV_GEF_USE_H324ANNEXI RV_YES

#undef RV_GEF_USE_MPEG4
#define RV_GEF_USE_MPEG4 RV_YES

#undef RV_GEF_USE_H249
#define RV_GEF_USE_H249 RV_YES

/* H.245 AutoCaps add-on module codecs support */
#undef RV_H245_AUTO_CAPS_AMR
#define RV_H245_AUTO_CAPS_AMR RV_YES

/* AMR WIDEBAND */
#undef RV_H245_AUTO_CAPS_G7222
#define RV_H245_AUTO_CAPS_G7222 RV_NO

#undef RV_H245_AUTO_CAPS_G723
#define RV_H245_AUTO_CAPS_G723 RV_NO

#undef RV_H245_AUTO_CAPS_G711
#define RV_H245_AUTO_CAPS_G711 RV_NO

#undef RV_H245_AUTO_CAPS_G722
#define RV_H245_AUTO_CAPS_G722 RV_NO

#undef RV_H245_AUTO_CAPS_G726
#define RV_H245_AUTO_CAPS_G726 RV_NO

#undef RV_H245_AUTO_CAPS_G729
#define RV_H245_AUTO_CAPS_G729 RV_NO

#undef RV_H245_AUTO_CAPS_H263
#define RV_H245_AUTO_CAPS_H263 RV_YES

#undef RV_H245_AUTO_CAPS_H264
#define RV_H245_AUTO_CAPS_H264 RV_NO

#undef RV_H245_AUTO_CAPS_MPEG4
#define RV_H245_AUTO_CAPS_MPEG4 RV_YES


/* ASN.1 related parameters */
#undef RV_ASN1_CODER_USE_Q931
#define RV_ASN1_CODER_USE_Q931 RV_NO

#undef RV_ASN1_CODER_USE_H450
#define RV_ASN1_CODER_USE_H450 RV_NO


/***********************************************************************

    General compilation and interfaces flags

***********************************************************************/
#ifndef RV_ANSI_FOURCE_OUR
#define RV_ANSI_FORCE_OUR RV_NO
#endif

#ifndef RV_TEST_ALL
#define RV_TEST_ALL RV_NO
#endif

/* Validation checking options */
#undef  RV_CHECK_MASK
#define RV_CHECK_MASK RV_CHECK_ALL

/* Logging options */
#undef  RV_LOGMASK
//#define RV_LOGMASK RV_LOGLEVEL_ALL
#define RV_LOGMASK RV_LOGLEVEL_NONE 

/* Log listener */
#undef  RV_LOGLISTENER_TYPE


#define RV_LOGLISTENER_TYPE  RV_LOGLISTENER_TERMINAL/*RV_LOGLISTENER_FILE_AND_TERMINAL RV_LOGLISTENER_UDP*/

/* Threadness mode: Single / Multi */
#undef  RV_THREADNESS_TYPE
#define RV_THREADNESS_TYPE RV_THREADNESS_MULTI

/* Networking support: None, IPv4, IPv6 */
#undef  RV_NET_TYPE
//#define RV_NET_TYPE RV_NET_IPV4  /* + RV_NET_IPV6 */
#define RV_NET_TYPE RV_NET_NONE  /* + RV_NET_IPV6 */

/* Select method: Select, Poll, Devpoll, Win32 WSA */
#undef  RV_SELECT_TYPE
#define RV_SELECT_TYPE RV_SELECT_DEFAULT

/* Defines size of preemption queue 
 * Used only under VxWorks pipe preemption and SMQ preemption
 * mechanism. Default value - 3000
 */

/* #define RV_SELECT_PREEMPTION_QUEUE_SIZE 3000 */

/* DNS support: None, Ares */
#undef  RV_DNS_TYPE
#define RV_DNS_TYPE RV_DNS_NONE

/* TLS support: None, OpenSSL */
#undef  RV_TLS_TYPE
#define RV_TLS_TYPE RV_TLS_NONE/*RV_TLS_OPENSSL*/

/* Set the priority of the blocked network commands for Nucleus (connect, close)
   RV_THREAD_SOCKET_PRIORITY_DEFAULT by default is 3 */
#undef RV_THREAD_SOCKET_PRIORITY_DEFAULT
#define RV_THREAD_SOCKET_PRIORITY_DEFAULT  (2)

/* Select if socket priority should be implemented with kqueue*/
#undef  RV_SELECT_KQUEUE_GROUPS
#define RV_SELECT_KQUEUE_GROUPS RV_NO
/* Set the range of Nucleus default portRange (otherwise, default range is 5000-65535) */
/*#undef RV_PORTRANGE_DEFAULT_START
#define RV_PORTRANGE_DEFAULT_START 10000
#undef RV_PORTRANGE_DEFAULT_FINISH
#define RV_PORTRANGE_DEFAULT_FINISH 65534*/

/* Select engine preemption is usually used in multithreading scenarios
 *  to interrupt RvSelectWaitAndBlock call. As a side effect user defined
 *  callback is called. In single-threaded scenarios the main effect (interrupt
 *  of RvSelectWaitAndBlock) is meaningless, so this mechanism is disabled by default.
 *  On the other hand, some application may find usefull the above mentioned 
 *  side effect. To enable preemption in single-threaded scenarios also - set
 *  uncomment the following lines
 */
#undef RV_SINGLE_THREADED_PREEMPTION
#define RV_SINGLE_THREADED_PREEMPTION RV_YES



/* 'Hosts' file resolution settings */

/* If defined as 1 - enables 'hosts' file based name resolution */
#undef  RV_DNS_USES_HOSTS
#define RV_DNS_USES_HOSTS RV_NO

/* Defines 'hosts' file search order 
 * If set as 
 *  RV_EHD_SEARCH_HOSTS_PLATFORM - platform specific algorithm will be used
 *  RV_EHD_SEARCH_HOSTS_FIRST    - hosts file will be searched before DNS servers
 *  RV_EHD_SEARCH_HOSTS_LAST     - hosts file will be searched after DNS servers
 */
#undef  RV_EHD_SEARCH_ORDER 
#define RV_EHD_SEARCH_ORDER RV_EHD_SEARCH_HOSTS_PLATFORM

#define RV_QUEUE_TYPE RV_QUEUE_STANDARD

#define RV_THREAD_TLS_TYPE  RV_THREAD_TLS_MANUAL /*0*/

#define RV_LOCK_WIN32_DEBUG

/* Square root support mode: None, Fast, */
#define RV_SQRT_TYPE RV_SQRT_ALGR

#define RV_THREAD_USE_AUTOMATIC_INTERNING 0

#undef	RV_MEMORY_DEBUGINFO
#define RV_MEMORY_DEBUGINFO RV_YES

/* Support for resource counting */
#define RV_USE_RESOURCE_COUNTING RV_YES

/*============== DNS caching related constants ==============*/

/* Set RV_DNS_USES_CACHING to enable caching */
#define RV_DNS_USES_CACHING RV_NO

/* Size of page used to hold cache data. Should be >= 512 */
#define RV_DNS_CACHE_PAGE_SIZE 2048

/* Number of preallocated pages */
#define RV_DNS_CACHE_PAGES_NUMBER 512

/* Lower limit on the number of buckets in hash table 
 * Actual number will be calculated by cache module itself as some prime
 * greater or equal RV_DNS_CACHE_HASH_SIZE 
 */
#define RV_DNS_CACHE_HASH_SIZE 1024

/* Maximal number of active queries to cache module */
#define RV_DNS_CACHE_MAX_ACTIVE_QUERIES 512

/* Compiles in support for sanity checks in DNS caching */
#define RV_DNS_CACHE_SANITY RV_YES

/* For debugging purposes only! Uses faked notion of time */
#define RV_DNS_CACHE_DEBUG 1


#endif /* RV_USRCONFIG_H */
