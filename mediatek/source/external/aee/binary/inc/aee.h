/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */


#ifndef __aee_h
#define __aee_h

#include <sys/types.h>

#ifdef __cplusplus
extern "C" {
#endif 

typedef enum {
    AE_KE = 0, /* Fatal Exception */
    AE_NE, 
    AE_JE, 
    AE_SWT,
    AE_EE, 
    AE_EXP_ERR_END,
    AE_ANR, /* Error or Warning or Defect */
    AE_RESMON,  
    AE_WRN_ERR_END,
    AE_MANUAL, /* Manual Raise */
    AE_EXP_CLASS_END,

    AE_KERNEL_DEFECT = 1000,
    AE_PROCESS_DEFECT,
} AE_EXP_CLASS; /* General Program Exception Class */

typedef enum {
    AE_DEFECT_FATAL,
    AE_DEFECT_EXCEPTION,
    AE_DEFECT_WARNING,
    AE_DEFECT_REMINDING,
    AE_DEFECT_ATTR_END
} AE_DEFECT_ATTR;

#define AE_INVALID              0xAEEFF000
#define AE_NOT_AVAILABLE        0xAEE00000
#define AE_DEFAULT              0xAEE00001

// AED Exported Functions

int aee_get_mode(void);

int aee_load_is_eng_built(void);

int aee_load_is_customer_built(void);

/**
 * Check if aee is running
 * 
 * Return TRUE if system running AEE
 **/
extern int aee_aed_is_ready();

/**
 * Raise aa AEE dump
 **/
extern int aee_aed_raise_exception(AE_EXP_CLASS cls,
                                   pid_t pid,
                                   pid_t tid,
                                   const char* type,
                                   const char* process,
                                   const char* module,
                                   const char* backtrace,
                                   const char* detail_mem,
                                   const char* detail_file);


extern int aee_aed_raise_exception2(AE_DEFECT_ATTR att,
				    AE_EXP_CLASS cls,
				    pid_t pid,
				    pid_t tid,
				    const char* process,
				    const char* module,
				    const char* backtrace,
				    const char* detail_mem,
				    const char* detail_file);

/**
 * Raise an AEE dump, this is simplify version of "aee_aed_raise_exception"
 *
 * @att: Specified defect level
 * @cls: 
 * @path: A file content which will include in db.xx(_exp_detail.txt), must not be NULL 
 * @suspect: Message describe defect reason
 * @pid: Process ID whose generate detect
 *
 * Return TRUE if raise detect successfully
 **/
extern int aee_aed_raise_defect(AE_DEFECT_ATTR att, 
                                AE_EXP_CLASS cls, 
                                const char* path, 
                                const char* suspect,
                                pid_t pid);


#ifdef __cplusplus
}
#endif 

#endif
