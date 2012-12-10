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

#ifndef __JNI_CACHE_H__
#define __JNI_CACHE_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

    /**
     * Build jni cache.
     */

    typedef struct {
        /* ============================== class cache ============================== */
        jclass java_lang_String;
        jclass com_mediatek_MediatekDM_mdm_MdmException;
        jclass com_mediatek_MediatekDM_mdm_MdmException_MdmError;
        jclass com_mediatek_MediatekDM_mdm_MdmTree;
        jclass com_mediatek_MediatekDM_mdm_MdmEngine;
        jclass com_mediatek_MediatekDM_mdm_NodeIoHandler;
        jclass com_mediatek_MediatekDM_mdm_NodeExecuteHandler;
        jclass com_mediatek_MediatekDM_mdm_MMIFactory;
        jclass com_mediatek_MediatekDM_mdm_MMIChoiceList;
        jclass com_mediatek_MediatekDM_mdm_MMIConfirmation;
        jclass com_mediatek_MediatekDM_mdm_MMIInfoMsg;
        jclass com_mediatek_MediatekDM_mdm_MMIInputQuery;
        jclass com_mediatek_MediatekDM_mdm_MMIObserver;
        jclass com_mediatek_MediatekDM_mdm_NIAMsgHandler;
        jclass com_mediatek_MediatekDM_mdm_NIAMsgHandler_UIMode;
        jclass com_mediatek_MediatekDM_mdm_PLDlPkg;
        jclass com_mediatek_MediatekDM_mdm_PLFile;
        jclass com_mediatek_MediatekDM_mdm_PLFactory;
        jclass com_mediatek_MediatekDM_mdm_PLRegistry;
        jclass com_mediatek_MediatekDM_mdm_PLStorage;
        jclass com_mediatek_MediatekDM_mdm_PLStorage_AccessMode;
        jclass com_mediatek_MediatekDM_mdm_PLStorage_ItemType;
        jclass com_mediatek_MediatekDM_mdm_SessionStateObserver;
        jclass com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionState;
        jclass com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionType;
        jclass com_mediatek_MediatekDM_mdm_MmiResult;
        jclass com_mediatek_MediatekDM_mdm_MmiViewContext;
        jclass com_mediatek_MediatekDM_mdm_MmiFactory;
        jclass com_mediatek_MediatekDM_mdm_MmiObserver;
        jclass com_mediatek_MediatekDM_mdm_MdmEngine_MmiObserverImpl;
        jclass com_mediatek_MediatekDM_mdm_MmiChoiceList;
        jclass com_mediatek_MediatekDM_mdm_MmiConfirmation;
        jclass com_mediatek_MediatekDM_mdm_MmiConfirmation_ConfirmCommand;
        jclass com_mediatek_MediatekDM_mdm_MmiInfoMsg;
        jclass com_mediatek_MediatekDM_mdm_MmiInfoMsg_InfoType;
        jclass com_mediatek_MediatekDM_mdm_MmiInputQuery;
        jclass com_mediatek_MediatekDM_mdm_MmiInputQuery_EchoType;
        jclass com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType;
        jclass com_mediatek_MediatekDM_mdm_MmiProgress;
        jclass com_mediatek_MediatekDM_mdm_MdmConfig;
        jclass com_mediatek_MediatekDM_mdm_MdmConfig_HttpAuthLevel;

        /* ============================== method cache ============================= */
        jmethodID com_mediatek_MediatekDM_mdm_MdmException_getError;
        jmethodID com_mediatek_MediatekDM_mdm_MdmException_init;
        jmethodID com_mediatek_MediatekDM_mdm_NodeIoHandler_read;
        jmethodID com_mediatek_MediatekDM_mdm_NodeIoHandler_write;
        jmethodID com_mediatek_MediatekDM_mdm_NodeExecuteHandler_execute;
        jmethodID com_mediatek_MediatekDM_mdm_PLStorage_delete;
        jmethodID com_mediatek_MediatekDM_mdm_PLStorage_open;
        jmethodID com_mediatek_MediatekDM_mdm_PLFile_read;
        jmethodID com_mediatek_MediatekDM_mdm_PLFile_write;
        jmethodID com_mediatek_MediatekDM_mdm_PLFile_close;
        jmethodID com_mediatek_MediatekDM_mdm_MdmEngine_notifySessionStateObservers;
        jmethodID com_mediatek_MediatekDM_mdm_MdmEngine_notifyNIAMsgHandler;
        jmethodID com_mediatek_MediatekDM_mdm_MmiViewContext_init;
        jmethodID com_mediatek_MediatekDM_mdm_MmiFactory_createInfoMsgDlg;
        jmethodID com_mediatek_MediatekDM_mdm_MmiFactory_createConfirmationDlg;
        jmethodID com_mediatek_MediatekDM_mdm_MmiFactory_createInputQueryDlg;
        jmethodID com_mediatek_MediatekDM_mdm_MmiFactory_createChoiceListDlg;
        jmethodID com_mediatek_MediatekDM_mdm_MmiFactory_createProgress;
        jmethodID com_mediatek_MediatekDM_mdm_MmiInfoMsg_display;
        jmethodID com_mediatek_MediatekDM_mdm_MmiConfirmation_display;
        jmethodID com_mediatek_MediatekDM_mdm_MmiInputQuery_display;
        jmethodID com_mediatek_MediatekDM_mdm_MmiChoiceList_display;
        jmethodID com_mediatek_MediatekDM_mdm_MmiProgress_update;

        /* =============================== field cache ============================= */
        jfieldID com_mediatek_MediatekDM_mdm_MdmException_MdmError_val;
        jfieldID com_mediatek_MediatekDM_mdm_MdmEngine_mPLStorage;
        jfieldID com_mediatek_MediatekDM_mdm_MdmEngine_mInstance;
        jfieldID com_mediatek_MediatekDM_mdm_PLStorage_AccessMode_READ;
        jfieldID com_mediatek_MediatekDM_mdm_PLStorage_AccessMode_WRITE;
        jfieldID com_mediatek_MediatekDM_mdm_PLStorage_ItemType_DLRESUME;
        jfieldID com_mediatek_MediatekDM_mdm_PLStorage_ItemType_DMTREE;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionType_BOOTSTRAP;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionType_DM;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionType_DL;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionType_IDLE;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionState_COMPLETE;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionState_ABORTED;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionState_STARTED;
        jfieldID com_mediatek_MediatekDM_mdm_SessionStateObserver_SessionState_PAUSED;
        jfieldID com_mediatek_MediatekDM_mdm_NIAMsgHandler_UIMode_BACKGROUND;
        jfieldID com_mediatek_MediatekDM_mdm_NIAMsgHandler_UIMode_INFORMATIVE;
        jfieldID com_mediatek_MediatekDM_mdm_NIAMsgHandler_UIMode_NOT_SPECIFIED;
        jfieldID com_mediatek_MediatekDM_mdm_NIAMsgHandler_UIMode_UI;
        jfieldID com_mediatek_MediatekDM_mdm_MdmEngine_mMmiObserver;
        jfieldID com_mediatek_MediatekDM_mdm_MdmEngine_MmiObserverImpl_mScreenType;
        jfieldID com_mediatek_MediatekDM_mdm_MdmEngine_mMmiFactory;
        jfieldID com_mediatek_MediatekDM_mdm_MmiResult_ERROR;
        jfieldID com_mediatek_MediatekDM_mdm_MmiResult_OK;
        jfieldID com_mediatek_MediatekDM_mdm_MmiResult_TEXT_TOO_LONG;
        jfieldID com_mediatek_MediatekDM_mdm_MmiResult_TOO_MANY_OPTIONS;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInfoMsg_InfoType_EXITING;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInfoMsg_InfoType_GENERIC;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInfoMsg_InfoType_STARTUP;
        jfieldID com_mediatek_MediatekDM_mdm_MmiConfirmation_ConfirmCommand_NO;
        jfieldID com_mediatek_MediatekDM_mdm_MmiConfirmation_ConfirmCommand_YES;
        jfieldID com_mediatek_MediatekDM_mdm_MmiConfirmation_ConfirmCommand_UNDEFINED;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_EchoType_MASKED;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_EchoType_PLAIN;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_EchoType_UNDEFINED;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_ALPHANUMERIC;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_DATE;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_IP_ADDRESS;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_NUMERIC;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_PHONE;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_TIME;
        jfieldID com_mediatek_MediatekDM_mdm_MmiInputQuery_InputType_UNDEFINED;
        jfieldID com_mediatek_MediatekDM_mdm_MdmConfig_HttpAuthLevel_BASIC;
        jfieldID com_mediatek_MediatekDM_mdm_MdmConfig_HttpAuthLevel_MD5;
        jfieldID com_mediatek_MediatekDM_mdm_MdmConfig_HttpAuthLevel_HMAC;
        jfieldID com_mediatek_MediatekDM_mdm_MdmConfig_HttpAuthLevel_NONE;
    } jni_cache_struct;

    extern void jni_cache_init(jni_cache_struct *);
    extern void jni_cache_fini(jni_cache_struct *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !__JNI_CACHE_H__ */
