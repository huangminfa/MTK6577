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

#ifndef _MTK_VT_LOG_H_
#define _MTK_VT_LOG_H_
#include <android/log.h>
#ifdef __cplusplus
extern "C" {
#endif

#define DEBUG_ENABLE_LOG

	void em_log_init();
	void em_log_deinit();
	int mtk_vt_log_is_enable(int which_tag, int log_type);

	void set_log2file_enable(int, int);
	void set_stack_log_enable(int, int);
	int media_lb_is_enable(int);
	void set_media_lb(int);
	int network_lb_is_enable(int);
	void set_network_lb(int);

	void stack_rx_log(void *data, int size);
	void stack_tx_log(void *data, int size);

	int is_em_indication_enable();
	void set_em_indication_enable(int is_eable, int);
	void em_indicate(const char *str);

#ifdef __cplusplus
};
#endif

enum {
	VT_LOG_VERBOSE = 0x01,
	VT_LOG_DEBUG = 0x02,
	VT_LOG_INFO = 0x04,
	VT_LOG_WARN = 0x08,
	VT_LOG_ERROR = 0x10,
	VT_LOG_GRP_1   = 0x0020,
	VT_LOG_GRP_2   = 0x0040,
	VT_LOG_GRP_3   = 0x0080,
	VT_LOG_GRP_4   = 0x0100,
	VT_LOG_GRP_5   = 0x0200,
	VT_LOG_GRP_6   = 0x0400,
	VT_LOG_GRP_7   = 0x0800,
	VT_LOG_GRP_8   = 0x1000,
	VT_LOG_GRP_9   = 0x2000,
	VT_LOG_GRP_10  = 0x4000,
	VT_LOG_PEER    = 0x8000,
	VT_LOG_ALL = 0xFFFF,
};

enum {
	VT_LOG_DEFAULT_TAG,
	VT_LOG_VTS_CLIENT_TAG,
	VT_LOG_VTS_SERVICE_TAG,
	VT_LOG_SWIP_TAG,
	VT_LOG_MAL_TAG,
	VT_LOG_COUNT
};

struct _vt_log_struct_ {
	int val;
	const char * const tag;
};

extern const struct _vt_log_struct_ VT_TAGS[VT_LOG_COUNT];

#ifndef STATIC_ASSERT
#define STATIC_ASSERT(e) typedef char __STATIC_ASSERT_FIAL__[(e)?1:-1]
#endif

#ifndef LOG_TAG_IND
#define LOG_TAG_IND VT_LOG_DEFAULT_TAG
#endif
#ifdef __cplusplus
extern "C" void mtk_vt_debug_printf(int prio, const char* tag, const char *fmt, ...);
extern "C" void mtk_vt_debug_printf_v(const char* tag, const char *fmt, ...);
extern "C" void mtk_vt_debug_printf_d(const char* tag, const char *fmt, ...);
extern "C" void mtk_vt_debug_printf_i(const char* tag, const char *fmt, ...);
extern "C" void mtk_vt_debug_printf_w(const char* tag, const char *fmt, ...);
extern "C" void mtk_vt_debug_printf_e(const char* tag, const char *fmt, ...);

#else
void mtk_vt_debug_printf(int prio, const char* tag, const char *fmt, ...);
void mtk_vt_debug_printf_v(const char* tag, const char *fmt, ...);
void mtk_vt_debug_printf_d(const char* tag, const char *fmt, ...);
void mtk_vt_debug_printf_i(const char* tag, const char *fmt, ...);
void mtk_vt_debug_printf_w(const char* tag, const char *fmt, ...);
void mtk_vt_debug_printf_e(const char* tag, const char *fmt, ...);

#endif

#define VT_LOG_IS_ENABLE(pro) ((VT_LOG_ERROR == pro) ? 1 : mtk_vt_log_is_enable(LOG_TAG_IND, pro))
//#define VT_LOG_IS_ENABLE(pro) mtk_vt_log_is_enable(LOG_TAG_IND, pro)
#define VT_DEBUG_PRINT(pro, ...) do { \
	if (VT_LOG_VERBOSE == pro) __android_log_print(ANDROID_LOG_VERBOSE, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	else if (VT_LOG_DEBUG == pro) __android_log_print(ANDROID_LOG_DEBUG, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	else if (VT_LOG_INFO == pro) __android_log_print(ANDROID_LOG_INFO, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	else if (VT_LOG_WARN == pro) __android_log_print(ANDROID_LOG_WARN, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	else if (VT_LOG_ERROR == pro) __android_log_print(ANDROID_LOG_ERROR, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	else __android_log_print(ANDROID_LOG_INFO, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	mtk_vt_debug_printf(pro, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);\
	}\
	while(0)

#define LOG_GEN_BY_PRIORITY(pro, ...) do{if(VT_LOG_IS_ENABLE(pro)){VT_DEBUG_PRINT(pro, __VA_ARGS__);}}while(0) 

#ifdef DEBUG_ENABLE_LOG

#define _V(...) do{if(VT_LOG_IS_ENABLE(VT_LOG_VERBOSE)){__android_log_print(ANDROID_LOG_VERBOSE, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);mtk_vt_debug_printf_v(VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);}}while(0)
#define _D(...) do{if(VT_LOG_IS_ENABLE(VT_LOG_DEBUG)){__android_log_print(ANDROID_LOG_DEBUG, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);mtk_vt_debug_printf_d(VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);}}while(0)
#define _I(...) do{if(VT_LOG_IS_ENABLE(VT_LOG_INFO)){__android_log_print(ANDROID_LOG_INFO, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);mtk_vt_debug_printf_i(VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);}}while(0)
#define _W(...) do{if(VT_LOG_IS_ENABLE(VT_LOG_WARN)){__android_log_print(ANDROID_LOG_WARN, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);mtk_vt_debug_printf_w(VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);}}while(0)
#define _E(...) do{if(VT_LOG_IS_ENABLE(VT_LOG_ERROR)){__android_log_print(ANDROID_LOG_ERROR, VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);mtk_vt_debug_printf_e(VT_TAGS[LOG_TAG_IND].tag, __VA_ARGS__);}}while(0)
#define _G(group, ...) LOG_GEN_BY_PRIORITY(group, __VA_ARGS__)

#else

#define _V(...) do{}while(0)
#define _D(...)	do{}while(0)
#define _I(...)	do{}while(0)
#define _W(...)	do{}while(0)
#define _E(...)	do{}while(0)
#define _G(group, ...) do{}while(0)

#endif

#endif // _MTK_VT_LOG_H_
