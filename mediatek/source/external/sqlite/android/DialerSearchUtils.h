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

#ifndef _ANDROID_DIALER_SEARCH_UTILS_H
#define _ANDROID_DIALER_SEARCH_UTILS_H

namespace android {
/*
 * traverse search item in database to check if could return this row
 *
 */
//typedef char DS_BOOL;
typedef unsigned char  DS_BOOL;
// temp definition
#define DS_UNIT_TEST

/* return value */
#define DS_RET_OK          0
#define DS_RET_ERR         (-1)
#define DS_RET_NO_RESULT   (-2)

/* bool value */
#define DS_TRUE   1
#define DS_FALSE  0
/* match status */
#define DS_NOT_MATCH					0
#define DS_RESET_MATCH					0
#define DS_PARTIAL_MATCH				1
#define DS_DONE_MATCH					2
#define DS_HEAD_MATCH					3
#define DS_IN_SUB_MATCH_SAME_OFFSET		4
#define DS_IN_SUB_MATCH					5

/* length and count */
#define DS_MAX_SUB_STR         30
#define DS_MAX_OUTPUT_LENGTH   255
#define DS_TEST_CASE_COUNT     10
#define DS_UT_INPUT_LENGTH  1024
#define DS_MAX_RESULT_LENGTH 1024
/* dialer search result output */
#define DS_RST_COUNT 3
#define DS_RST_POS_OFFEST_BEGIN 0
#define DS_RST_POS_OFFEST_END   1
#define DS_RST_POS_COUNT        2
/* search type */
#define DS_SEARCH_TYPE_NUMBER     8
#define DS_SEARCH_TYPE_NAME   11

int dsProcessSearchKey(const char *searchKey, 
	const char *matchText, 
	const char *matchOffset, 
	int matchType, 
	char *res,
	int *resLen);

int dsProcessSearchKeyFilter(
		const char *searchKey,
		const char *matchText,
		const char *matchOffset,
		int matchType);

}  // namespace android
#endif /* _ANDROID_DIALER_SEARCH_UTILS_H */
