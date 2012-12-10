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

#ifndef __TLS_ADP_DEFS_H__
#define __TLS_ADP_DEFS_H__

#ifndef PKI_STRUCT_H
#include "pki_defs.h"
#endif

// certmain_defs.h ///////////////////////////////////////////////////////////////////////////////////////////
#if 0
/* CertMan Root Folder */
#define CERTMAN_ROOT_FOLDER             L"z:\\@certman\\"

/* folder storing all DER encoded Root CA + Intermediate CA + Other User certificates with KeyPurpose=CERTMAN_KP_SERVER_AUTH */
#define CERTMAN_SHARED_CERTS_PATH       (CERTMAN_ROOT_FOLDER L"shared\\")
#else
/* CertMan Root Folder */
#define CERTMAN_ROOT_FOLDER             "/data/agps_supl"
/* folder storing all DER encoded Root CA + Intermediate CA + Other User certificates with KeyPurpose=CERTMAN_KP_SERVER_AUTH */
#define CERTMAN_SHARED_CERTS_PATH       "/system/etc/security/cacerts"   // to use Android's default 'SYSTEM' cacerts because Android uses *.0 files instead of cacerts.bks since ICS
#endif

/* This enum defines the certman errors */
typedef enum
{
    CERTMAN_ERR_NONE = PKI_ERR_NONE,                        /* 0 (pki wrapper error enum) */
    CERTMAN_ERR_FAIL = PKI_ERR_FAIL,                        /* 1 (pki wrapper error enum) */
    CERTMAN_ERR_MEMFULL = PKI_ERR_MEMFULL,                  /* 2 (pki wrapper error enum) */
    CERTMAN_ERR_INVALID_CONTEXT = PKI_ERR_INVALID_CONTEXT,  /* 3 (pki wrapper error enum) */
    CERTMAN_ERR_OUT_OF_RANGE = PKI_ERR_OUT_OF_RANGE,        /* 4 (pki wrapper error enum) */
    CERTMAN_ERR_INCORRECT_PASSWORD = PKI_ERR_INCORRECT_PASSWORD,    /* 5 (pki wrapper error enum) */
    CERTMAN_ERR_FS_ERROR = PKI_ERR_FS_ERROR,                /* 6 (pki wrapper error enum) */
    CERTMAN_ERR_NEED_PASSWORD = PKI_ERR_NEED_PASSWORD,      /* 7 (pki wrapper error enum) */
    CERTMAN_ERR_INVALID_INPUT = PKI_ERR_INVALID_INPUT,      /* 8 (pki wrapper error enum) */
    CERTMAN_ERR_EXT_NOT_FOUND = PKI_ERR_EXT_NOT_FOUND,      /* 9 (pki wrapper error enum) */
    CERTMAN_ERR_ISSUER_UID_NOT_FOUND = PKI_ERR_ISSUER_UID_NOT_FOUND,    /* 10 (pki wrapper error enum) */
    CERTMAN_ERR_SUBJECT_UID_NOT_FOUND = PKI_ERR_SUBJECT_UID_NOT_FOUND,  /* 11 (pki wrapper error enum) */
    CERTMAN_ERR_UNSUPPORTED_CONTENT = PKI_ERR_UNSUPPORTED_CONTENT,      /* 12 (pki wrapper error enum) */
    CERTMAN_ERR_CERT_NOT_FOUND = PKI_ERR_CERT_NOT_FOUND,    /* 13 (pki wrapper error enum) */
    CERTMAN_ERR_CORRUPTED_DATA = PKI_ERR_CORRUPTED_DATA,    /* 14 (pki wrapper error enum) */
    CERTMAN_ERR_EXCEED_MAX_DATA_SIZE = PKI_ERR_EXCEED_MAX_DATA_SIZE,    /* 15 (pki wrapper error enum) */
    CERTMAN_ERR_NOT_SUPPORTED_OP = PKI_ERR_NOT_SUPPORTED_OP,            /* 16 (pki wrapper error enum) */    
    CERTMAN_ERR_FIRST_BOOTUP,               /* 17, First bootup operation for preinstall certs */
    CERTMAN_ERR_KEY_NOT_FOUND,              /* 18, Key not found in certman database */
    CERTMAN_ERR_ISSUER_NOT_FOUND,           /* 19, Issuer name can't be found in provided issuer names file */
    CERTMAN_ERR_ID_ALREADY_EXISTS,          /* 20, The specified certificate ID already exists */
    CERTMAN_ERR_FILE_NOT_FOUND,             /* 21, File not found error */
    CERTMAN_ERR_DISK_FULL,                  /* 22, Disk full error */
    CERTMAN_ERR_FILE_CORRUPTED,             /* 23, The file content is corrupted or not supported */
    CERTMAN_ERR_INVALID_LABEL,              /* 24, Label string invalid */
    CERTMAN_ERR_INVALID_CERT_GROUP,         /* 25, specified certificate group invalid */
    CERTMAN_ERR_INVALID_KEY_PURPOSE,        /* 26, specified key purpose invalid */
    CERTMAN_ERR_INVALID_KEY_TYPE,           /* 27, specified key type invalid */
    CERTMAN_ERR_INVALID_DOMAIN,             /* 28, specified certificate domain invalid */
    CERTMAN_ERR_INVALID_FILENAME,           /* 29, specified filename parameter invalid */
    CERTMAN_ERR_INVALID_DATA,               /* 30, specified data or data length parameter invalid */
    CERTMAN_ERR_INVALID_ENCODING,           /* 31, specified data encoding type invalid */
    CERTMAN_ERR_INVALID_JOB,                /* 32, specified job ID invalid in parsing and import process */
    CERTMAN_ERR_INVALID_CERT_ID,            /* 33, specified Certificate ID is invalid */
    CERTMAN_ERR_INVALID_PASSWORD,           /* 34, specified password parameters invalid */
    CERTMAN_ERR_INVALID_PATH,               /* 35, specified path parameter invalid */
    CERTMAN_ERR_INVALID_VALIDATION_PARAM,   /* 36, specified validation parameters invalid */
    CERTMAN_ERR_NO_PWD_CALLBACK,            /* 37, No specified password callback function for certman */
    CERTMAN_ERR_LABEL_EXISTS,               /* 38, label duplicated error */
    CERTMAN_ERR_CERT_EXISTS,                /* 39, specified certificate had been imported before */
    CERTMAN_ERR_KEY_PURPOSE_DENIED,         /* 40, key purpose denied for specified operation */
    CERTMAN_ERR_ACCESS_DENIED,              /* 41, key usage request denied */
    CERTMAN_ERR_READ_ONLY,                  /* 42, the certificate is read_only one */
    CERTMAN_ERR_CERT_IN_USE,                /* 43, the certificate is inuse */
    CERTMAN_ERR_CHAIN_NOT_ALLOWED,          /* 44, certificate chain validation failed */
    CERTMAN_ERR_CHAIN_TOO_LARGE,            /* 45, certificate chain length exceeds CERTMAN_NUM_CERT_IN_CHAIN_DISP */
    CERTMAN_ERR_TOO_MANY_CERTS,             /* 46, too many certificates in validate request cert chain array */
    CERTMAN_ERR_CERT_EXPIRED,               /* 47, certificate was expired before */
    CERTMAN_ERR_NO_TRUSTED_CERT_FOUND,      /* 48, no trusted certificate found in passed certificate chain */
    CERTMAN_ERR_CONVERT_FAIL,               /* 49, database information file encode/decode error */
    CERTMAN_ERR_FILE_TOO_LARGE,             /* 50, the input text file is too large to parse */
    CERTMAN_ERR_WOULDBLOCK,                 /* 51, the operation cannot finish immediately */
    CERTMAN_ERR_RETRY,                      /* 52, request timeout, retry again */
    CERTMAN_ERR_NO_RESPONSE,                /* 53, no OCSP response from server */
    CERTMAN_ERR_TOTAL
} certman_error_enum;

/* This enum defines the certman certificate group filter settings */
typedef enum
{
    CERTMAN_CERTGRP_NONE = PKI_CERTGRP_NONE,            /* no specified certificate group filter */
    CERTMAN_CERTGRP_ROOTCA = PKI_CERTGRP_ROOTCA,        /* specified certificate group filter for root ca (issuer = subject) */
    CERTMAN_CERTGRP_CA = PKI_CERTGRP_CA,                /* specified certificate group filter for intermediately ca (ver 3 cert with BasicConstraint: CA=TRUE) */
    CERTMAN_CERTGRP_OTHERUSER = PKI_CERTGRP_OTHERUSER,  /* specified certificate group filter for other end-entity certificate (ver 3 cert with BasicConstraint: CA=FALSE) OR 
                                                           ver 1 cert with no private key associated */
    CERTMAN_CERTGRP_PERSONAL = PKI_CERTGRP_PERSONAL,    /* specified certificate group filter for personal certificate(associated with a private key (regardless of whether it's a CA cert)) */
    CERTMAN_CERTGRP_ANY = PKI_CERTGRP_ANY               /* filter group for all certs */
} certman_cert_group_enum;

/* This enum defines the certman domain settings */
typedef enum
{
    CERTMAN_DOMAIN_NONE = PKI_DOMAIN_NONE,                  /* no specified filter domain */
    CERTMAN_DOMAIN_UNTRUSTED = PKI_DOMAIN_UNTRUSTED,        /* certs' with no specified domain, filter for untrusted domain certs */
    CERTMAN_DOMAIN_OPERATOR = PKI_DOMAIN_OPERATOR,          /* Operator installed certs, filter for operator domain certs */
    CERTMAN_DOMAIN_MANUFACTURER = PKI_DOMAIN_MANUFACTURER,  /* Manufacturer installed certs, filter for Manufacturer domain certs */
    CERTMAN_DOMAIN_THIRD_PARTY = PKI_DOMAIN_THIRD_PARTY,    /* Third party certs, filter for third party domain certs */
    CERTMAN_DOMAIN_ANY = PKI_DOMAIN_ANY                     /* filter domain for all certs */
} certman_domain_enum;

/* This enum defines the certman extension key usage extension settings in X509 spec */
typedef enum
{
    CERTMAN_KP_NONE = PKI_EXTKEYUSAGE_NONE,                         /* No specified extension key usage */
    CERTMAN_KP_SERVER_AUTH = PKI_EXTKEYUSAGE_SERVER_AUTH,           /* extension key usage : server authentication assert */
    CERTMAN_KP_CLIENT_AUTH = PKI_EXTKEYUSAGE_CLIENT_AUTH,           /* extension key usage : client authentication assert */
    CERTMAN_KP_CODE_SIGNING = PKI_EXTKEYUSAGE_CODE_SIGNING,         /* extension key usage : code signing assert */
    CERTMAN_KP_EMAIL_PROTECTION = PKI_EXTKEYUSAGE_EMAIL_PROTECTION, /* extension key usage : email protection assert */
    CERTMAN_KP_IPSEC_ENDSYSTEM = PKI_EXTKEYUSAGE_IPSEC_ENDSYSTEM,   /* extension key usage : IPSec end system assert */
    CERTMAN_KP_IPSEC_TUNNEL = PKI_EXTKEYUSAGE_IPSEC_TUNNEL,         /* extension key usage : IPSec tunneling assert */
    CERTMAN_KP_IPSEC_USER = PKI_EXTKEYUSAGE_IPSEC_USER,             /* extension key usage : IPSec user assert */
    CERTMAN_KP_TIME_STAMPING = PKI_EXTKEYUSAGE_TIME_STAMPING,       /* extension key usage : Time stamp assert */
    CERTMAN_KP_OCSP_SIGNING = PKI_EXTKEYUSAGE_OCSP_SIGNING,         /* extension key usage : OCSP signing assert */
    CERTMAN_KP_ALL = PKI_EXTKEYUSAGE_ALL
} certman_keypurpose_enum;


// fs_type.h ///////////////////////////////////////////////////////////////////////////////////////////

//FS_Open Parameter
#define FS_READ_WRITE            0x00000000L
#define FS_READ_ONLY             0x00000100L
#define FS_OPEN_SHARED           0x00000200L
#define FS_OPEN_NO_DIR           0x00000400L
#define FS_OPEN_DIR              0x00000800L
#define FS_CREATE                0x00010000L
#define FS_CREATE_ALWAYS         0x00020000L
#define FS_COMMITTED             0x01000000L
#define FS_CACHE_DATA            0x02000000L
#define FS_LAZY_DATA             0x04000000L
#define FS_NONBLOCK_MODE         0x10000000L
#define FS_PROTECTION_MODE       0x20000000L
#define FS_NOBUSY_CHECK_MODE     0x40000000L

#define FS_FILE_TYPE             0x00000004     // Recursive Type API Common, Public
#define FS_DIR_TYPE              0x00000008     // Recursive Type API Common, Public
#define FS_RECURSIVE_TYPE        0x00000010     // Recursive Type API Common, Public

#endif
