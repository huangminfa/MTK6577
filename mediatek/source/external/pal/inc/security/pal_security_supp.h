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

#ifndef PAL_SECURITY_SUPP_H
#define PAL_SECURITY_SUPP_H

#include "pal_security_hdlr.h"

#define BIT(n) (0x0001 << (n))

#define MAC2STR(a) (a)[0], (a)[1], (a)[2], (a)[3], (a)[4], (a)[5]
#define MACSTR "%x:%x:%x:%x:%x:%x"

#define ntohs(a)	\
		((((a)&0xFF)<<8) + ((((a)>>8)&0xFF)))

#define htons(a) ntohs(a)
#define host_to_le16(n) (n)

#define WPA_CIPHER_CCMP BIT(4) //move from config_ssid.h

#define WPA_REPLAY_COUNTER_LEN 8
#define WPA_NONCE_LEN 32

#define PMK_LEN 32 //move from config_ssid.h

#define MD5_MAC_LEN 16 //move from supc_md5.h

#define ETH_ALEN 6

#define PMKID_LEN 16 //wpa_supplicant_i.h

typedef struct supp_wpa_eapol_key {
	kal_uint8 type;
	kal_uint16 key_info;
	kal_uint16 key_length;
	kal_uint8 replay_counter[WPA_REPLAY_COUNTER_LEN];
	kal_uint8 key_nonce[WPA_NONCE_LEN];
	kal_uint8 key_iv[16];
	kal_uint8 key_rsc[8];
	kal_uint8 key_id[8]; /* Reserved in IEEE 802.11i/RSN */
	kal_uint8 key_mic[16];
	kal_uint16 key_data_length;
	/* followed by key_data_length bytes of key_data */
} supp_wpa_eapol_key;

#define WPA_KEY_INFO_TYPE_MASK (BIT(0) | BIT(1) | BIT(2))
#define WPA_KEY_INFO_TYPE_HMAC_MD5_RC4 BIT(0)
#define WPA_KEY_INFO_TYPE_HMAC_SHA1_AES BIT(1)
#define WPA_KEY_INFO_KEY_TYPE BIT(3) /* 1 = Pairwise, 0 = Group key */
/* bit4..5 is used in WPA, but is reserved in IEEE 802.11i/RSN */
#define WPA_KEY_INFO_KEY_INDEX_MASK (BIT(4) | BIT(5))
#define WPA_KEY_INFO_KEY_INDEX_SHIFT 4
#define WPA_KEY_INFO_INSTALL BIT(6) /* pairwise */
#define WPA_KEY_INFO_TXRX BIT(6) /* group */
#define WPA_KEY_INFO_ACK BIT(7)
#define WPA_KEY_INFO_MIC BIT(8)
#define WPA_KEY_INFO_SECURE BIT(9)
#define WPA_KEY_INFO_ERROR BIT(10)
#define WPA_KEY_INFO_REQUEST BIT(11)
#define WPA_KEY_INFO_ENCR_KEY_DATA BIT(12) /* IEEE 802.11i/RSN only */

#define WPA_CAPABILITY_PREAUTH BIT(0)

#define GENERIC_INFO_ELEM 0xdd
#define RSN_INFO_ELEM 0x30

typedef struct pal_supplicant
{
	kal_uint8 rx_replay_counter[WPA_REPLAY_COUNTER_LEN];
	int rx_replay_counter_set;
    unsigned char own_addr[ETH_ALEN]; //local addr
	kal_uint8 *ap_rsn_ie;    //store rsn ie from ap
	kal_uint16 ap_rsn_ie_len;
    int pairwise_cipher;
    int renew_snonce;
    int keys_cleared;
	kal_uint8 snonce[WPA_NONCE_LEN];
	kal_uint8 anonce[WPA_NONCE_LEN]; /* ANonce from the last 1/4 msg */
    kal_uint8 pmk[PMK_LEN];
	kal_uint16 pmk_len;
    struct wpa_ptk ptk, tptk;
    int ptk_set, tptk_set;
    //struct eapol_sm *eapol;//test
} pal_supplicant;

extern void pal_supp_register_send_security_frame_func( send_sec_frame_funcp funcp );
extern void pal_supp_register_report_security_result_func( report_sec_result_funcp funcp );
extern void pal_supp_register_add_key_func( add_key_funcp funcp );

#endif /* PAL_SECURITY_SUPP_H */

