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

#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <pthread.h>
#include <sys/mount.h>
#include <sys/statfs.h>

#include "common.h"
#include "miniui.h"
#include "ftm.h"

#ifdef FEATURE_FTM_IDLE

#define TEST_SLP_FACTORY_MODE 0
#define TAG                 "[IDLE] "

#if defined(MT6516)
static const char *devpath = "/dev/MT6516-SLP_FOP";
#else 
static const char *devpath = "/sys/power/idle_state";
#endif 

int idle_entry(struct ftm_param *param, void *priv)
{
    struct ftm_module *mod = priv;
    int fd = -1;
    int ret = 0;
    char *buf = "0";
    
    LOGD(TAG "idle_entry\n");
    #if defined(MT6516)
        fd = open(devpath,O_RDONLY, 0);
        if (fd == -1) {
            mod->test_result = FTM_TEST_FAIL;
            LOGD(TAG "get_BAT_status - Can't open /dev/MT6516-SLP_FOP\n");
            return -1;
        }
        ret = ioctl(fd, TEST_SLP_FACTORY_MODE, &ret);
    #else
        fd = open(devpath,O_RDWR, 0);
        if (fd == -1) {
            mod->test_result = FTM_TEST_FAIL;
            LOGD(TAG "get_BAT_status - Can't open /sys/power/idle_state\n");
            return -1;
        }
        ret = write(fd, buf, strlen(buf));
    #endif
    
    close(fd);
    
    #if defined(MT6516)
        if (ret >= 0) {
            mod->test_result = FTM_TEST_PASS;
        } else {
            mod->test_result = FTM_TEST_FAIL;
        }
    #else
        if (ret > 0) {
            mod->test_result = FTM_TEST_PASS;
        } else {
            mod->test_result = FTM_TEST_FAIL;
        }
    #endif    
    
    return 0;
}



int idle_init(void)
{
    int ret = 0;
    struct ftm_module *mod;
    
    mod = ftm_alloc(ITEM_IDLE, 0);
    
    if (!mod)
        return -ENOMEM;
    
    LOGD(TAG "idle_init\n");
    
    ret = ftm_register(mod, idle_entry, (void*)mod);
    
    return ret;
}
#endif

