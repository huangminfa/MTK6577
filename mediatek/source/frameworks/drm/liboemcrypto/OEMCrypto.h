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
 * Reference APIs needed to support Widevine's crypto algorithms.
 *
 ******************************************************************************/

#ifndef _OEMCRYPTO_AES_H
#define _OEMCRYPTO_AES_H

typedef	unsigned char	OEMCrypto_UINT8;
typedef char    	OEMCrypto_INT8;
typedef	unsigned int	OEMCrypto_UINT32;


typedef enum OEMCryptoResult {
  OEMCrypto_SUCCESS = 0,
  OEMCrypto_FAILURE
} OEMCryptoResult;


#ifdef __cplusplus
extern "C" {
#endif

#define OEMCrypto_GetKeyboxData _oec01
#define OEMCrypto_EncryptAndStoreKeyBox _oec02
#define OEMCrypto_IdentifyDevice _oec03
#define OEMCrypto_GetRandom _oec04

/*
 * OEMCrypto_GetKeyboxData
 *
 * Description:
 *   Retrieve a range of bytes from the Widevine keybox.  This function should decrypt the keybox 
 *   and return the specified bytes.  
 *
 * Parameters:
 *   buffer (out) - Points to the buffer that should receive the keybox data.
 *   offset (in) - Byte offset from the beginning of the keybox for the first byte of data to return
 *   length (in) - The number of bytes in the key data. 
 * 
 * Returns:
 *   OEMCryptoResult indicating success or failure
 * 	If the keybox cannot be accessed (e.g. device is unlocked) this function should return OEMCrypto_FAILURE.
 */

OEMCryptoResult OEMCrypto_GetKeyboxData(OEMCrypto_UINT8* keyData, OEMCrypto_UINT32 offset, OEMCrypto_UINT32 length);


/*
 * OEMCrypto_EncryptAndStoreKeyBox
 *
 * Description:
 *   Encrypt and store the keybox to persistent memory.  The keybox consists of a 128-bit
 *   device key followed by some number of bytes of keyID.  The device key must be stored
 *   securely, encrypted by an OEM root key.  The device key will be decrypted and latched
 *   into a key slot by OEMCrypt_AES128_SetEntitlementKey.  The keybox bytes following the
 *   device key are termed the keyID and shall be returned from the keybox to the CPU in
 *   the clear for use by Widevine DRM software.
 *
 *   This function is used once to load the keybox onto the device at provisioning time.
 *
 * Parameters: 
 *   keybox (in) - Pointer to clear keybox data.  Must be encrypted with an OEM root key.
 *   keyboxLength (in) - Length of the keybox data in bytes
 *
 * Returns:
 *   OEMCryptoResult indicating success or failure
 */

OEMCryptoResult OEMCrypto_EncryptAndStoreKeyBox(OEMCrypto_UINT8 *keybox, 
                                                OEMCrypto_UINT32 keyBoxLength);

/*
 * OEMCrypto_IdentifyDevice
 *
 * Description:
 *   Retrieve the device's unique identifier.  The device identifier shall NOT come
 *   from the widevine keybox. 
 *
 * Parameters:
 *   deviceID (out) - Points to the buffer that should receive the device ID.
 *   idLength (in) - Length of the device ID buffer. Maximum of 32 bytes allowed.
 *
 * Returns:
 *   OEMCryptoResult indicating success or failure
 */

OEMCryptoResult OEMCrypto_IdentifyDevice(OEMCrypto_UINT8* deviceID,
                                         OEMCrypto_UINT32 idLength);

/*
 * OEMCrypto_GetRandom
 *
 * Description:
 *   Returns a buffer filled with hardware-generated random bytes, if supported by the hardware.
 *
 * Parameters:
 *   randomData (out) - Points to the buffer that should recieve the random data.
 *   dataLength (in)  - Length of the random data buffer in bytes.
 * 
 * Returns:
 *   OEMCryptoResult indicating success or failure
 */

OEMCryptoResult OEMCrypto_GetRandom(OEMCrypto_UINT8* randomData,
                                    OEMCrypto_UINT32 dataLength);



#ifdef __cplusplus
}
#endif

#endif

/***************************** End of File *****************************/
