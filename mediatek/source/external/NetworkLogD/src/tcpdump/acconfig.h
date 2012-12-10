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

/* "generated automatically" means DO NOT MAKE CHANGES TO config.h.in --
 * make them to acconfig.h and rerun autoheader */
@TOP@

/* Define if you have SSLeay 0.9.0b with the buggy cast128. */
#undef HAVE_BUGGY_CAST128

/* Define if you enable IPv6 support */
#undef INET6

/* Define if you enable support for the libsmi. */
#undef LIBSMI

/* Define if you have the <smi.h> header file.  */
#undef HAVE_SMI_H

/* define if you have struct __res_state_ext */
#undef HAVE_RES_STATE_EXT

/* define if your struct __res_state has the nsort member */
#undef HAVE_NEW_RES_STATE

/*
 * define if struct ether_header.ether_dhost is a struct with ether_addr_octet
 */
#undef ETHER_HEADER_HAS_EA

/* define if struct ether_arp contains arp_xsha */
#undef ETHER_ARP_HAS_X

/* define if you have the addrinfo function. */
#undef HAVE_ADDRINFO

/* define if you need to include missing/addrinfoh.h. */
#undef NEED_ADDRINFO_H

/* define ifyou have the h_errno variable. */
#undef HAVE_H_ERRNO

/* define if IN6ADDRSZ is defined (XXX not used!) */
#undef HAVE_IN6ADDRSZ

/* define if INADDRSZ is defined (XXX not used!) */
#undef HAVE_INADDRSZ

/* define if this is a development version, to use additional prototypes. */
#undef HAVE_OS_PROTO_H

/* define if <unistd.h> defines __P() */
#undef HAVE_PORTABLE_PROTOTYPE

/* define if RES_USE_INET6 is defined */
#undef HAVE_RES_USE_INET6

/* define if struct sockaddr has the sa_len member */
#undef HAVE_SOCKADDR_SA_LEN

/* define if you have struct sockaddr_storage */
#undef HAVE_SOCKADDR_STORAGE

/* define if you have both getipnodebyname() and getipnodebyaddr() */
#undef USE_GETIPNODEBY

/* define if you have ether_ntohost() and it works */
#undef USE_ETHER_NTOHOST

/* define if libpcap has pcap_version */
#undef HAVE_PCAP_VERSION

/* define if libpcap has pcap_debug */
#undef HAVE_PCAP_DEBUG

/* define if libpcap has yydebug */
#undef HAVE_YYDEBUG

/* define if libpcap has pcap_list_datalinks() */
#undef HAVE_PCAP_LIST_DATALINKS

/* define if libpcap has pcap_set_datalink() */
#undef HAVE_PCAP_SET_DATALINK

/* define if libpcap has pcap_datalink_name_to_val() */
#undef HAVE_PCAP_DATALINK_NAME_TO_VAL

/* define if libpcap has pcap_datalink_val_to_description() */
#undef HAVE_PCAP_DATALINK_VAL_TO_DESCRIPTION

/* define if libpcap has pcap_dump_ftell() */
#undef HAVE_PCAP_DUMP_FTELL

/* define if you have getrpcbynumber() */
#undef HAVE_GETRPCBYNUMBER

/* define if unaligned memory accesses fail */
#undef LBL_ALIGN

/* The successful return value from signal (?)XXX */
#undef RETSIGVAL

/* Define this on IRIX */
#undef _BSD_SIGNALS

/* For HP/UX ANSI compiler? */
#undef _HPUX_SOURCE

/* AIX hack. */
#undef _SUN

/* Workaround for missing 64-bit formats */
#undef PRId64
#undef PRIo64
#undef PRIx64
#undef PRIu64

/* Whether or not to include the possibly-buggy SMB printer */
#undef TCPDUMP_DO_SMB

/* Long story short: aclocal.m4 depends on autoconf 2.13
 * implementation details wrt "const"; newer versions
 * have different implementation details so for now we
 * put "const" here.  This may cause duplicate definitions
 * in config.h but that should be OK since they're the same.
 */
#undef const

/* Define if you have the dnet_htoa function.  */
#undef HAVE_DNET_HTOA

/* Define if you have a dnet_htoa declaration in <netdnet/dnetdb.h>.  */
#undef HAVE_NETDNET_DNETDB_H_DNET_HTOA

/* define if should drop privileges by default */
#undef WITH_USER

/* define if should chroot when dropping privileges */
#undef WITH_CHROOT
