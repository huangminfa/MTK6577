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

#ifndef __JNI_INTERFACE_H__
#define __JNI_INTERFACE_H__

#include <omc/omc_if.h>
#include <omc/tree/tree_if.h>
#include <omc/trg/trg_info.h>
#include <jni.h>
#include "../mdm/engine.h"
#include "omcdm.h"
#include "jni_cache.h"

#ifdef __cplusplus
extern "C" {
#endif


    /**
     * @struct ci_dm_session_trigger_message
     * Client initiated dm session trigger message.
     */
    typedef struct {
        char *account;
        char *generic_alert_type;
        char *message;
    } ci_dm_session_trigger_message;


    /* Tree */
    extern OMC_Yield jni_TREE_storageOpenForRead(OMC_UserDataPtr udp, OMC_TREE_PersistenceReadFunc *pReadFunc, void **pContext);
    extern OMC_Yield jni_TREE_storageOpenForWrite(OMC_UserDataPtr udp, OMC_TREE_PersistenceWriteFunc *pWriteFunc, void **pContext);
    extern OMC_Yield jni_TREE_storageClose(OMC_UserDataPtr udp, void *context, IBOOL commit);

    /* MMI */
    extern OMC_Error jni_MMI_setScreen(OMC_UserDataPtr pUser, OMC_MMI_ScreenId screenId, OMC_MMI_ScreenOptionsPtr optPtr, OMC_MMI_DataPtr dataPtr);

    /* Session state */
    extern void jni_notify_session_state_observer(engine_session_type type, engine_session_state state, int last_error);

    /* NIA */
    extern int jni_notify_nia_msg_handler(TRG_UIMode mode, short dmVersion, const char *vendorSpecificData, unsigned int bLength);

    /* Exception */
    extern void jni_raise_mdm_exception(JNIEnv *env, jni_cache_struct *jc, MdmError_enum code);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !__JNI_INTERFACE_H__ */
