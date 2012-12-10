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
 * Subset of the OEMCrypto APIs required for L1 support since they are called
 * from libwvm.
 *
 ******************************************************************************/

#ifndef _OEMCRYPTO_L1_H
#define _OEMCRYPTO_L1_H

typedef unsigned char   OEMCrypto_UINT8;
typedef char            OEMCrypto_INT8;
typedef unsigned int    OEMCrypto_UINT32;
typedef unsigned int    OEMCrypto_SECURE_BUFFER;


typedef enum OEMCryptoResult {
  OEMCrypto_SUCCESS = 0
} OEMCryptoResult;


#ifdef __cplusplus
extern "C" {
#endif

#define OEMCrypto_Initialize _oec01
#define OEMCrypto_Terminate _oec02
#define OEMCrypto_DecryptVideo _oec05
#define OEMCrypto_DecryptAudio _oec06

OEMCryptoResult OEMCrypto_Initialize(void);
OEMCryptoResult OEMCrypto_Terminate(void);
OEMCryptoResult OEMCrypto_DecryptVideo(const OEMCrypto_UINT8*,
                                       const OEMCrypto_UINT8*, const OEMCrypto_UINT32,
                                       OEMCrypto_UINT32, OEMCrypto_UINT32, OEMCrypto_UINT32 *);


OEMCryptoResult OEMCrypto_DecryptAudio(const OEMCrypto_UINT8*,
                                       const OEMCrypto_UINT8*, const OEMCrypto_UINT32,
                                       OEMCrypto_UINT8 *, OEMCrypto_UINT32 *);


#ifdef __cplusplus
}
#endif

#endif

/***************************** End of File *****************************/
