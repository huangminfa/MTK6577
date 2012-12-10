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

#ifndef __ENGINE_H__
#define __ENGINE_H__

#include "common.h"
#include <omc/omc_if.h>
#include <omc/omc_userdata.h>
#include "message_queue.h"


#ifdef __cplusplus
extern "C" {
#endif

    /**
     * Engine states. It reflects the life cycle of MdmEngine singleton.
     */
    typedef enum {
        ENGINE_STATE_CREATED,		/**< MdmEngine singleton is created. */
        ENGINE_STATE_STARTED,		/**< MdmEngine.start() is completed and engine is ready to process message. */
        ENGINE_STATE_STOPPED,		/**< MdmEngine.stop() is completed and engine does not process messages any longer. */
        ENGINE_STATE_DESTROYED,		/**< MdmEngine.destroyed() is completed and resources are all released. */
    } engine_state;

    typedef enum {
        ENGINE_SESSION_TYPE_IDLE,	/**< No running session. */
        ENGINE_SESSION_TYPE_BS,		/**< Bootstrap session. */
        ENGINE_SESSION_TYPE_DM,		/**< OMA DM session. */
        ENGINE_SESSION_TYPE_DL,		/**< OMA DL session. */
    } engine_session_type;

    typedef enum {
        ENGINE_SESSION_STATE_COMPLETE,		/**< Session completed successfully. This is also the default state. */
        ENGINE_SESSION_STATE_STARTED,		/**< There is a session running. */
        ENGINE_SESSION_STATE_PAUSED,		/**< @todo not implemented. */
        ENGINE_SESSION_STATE_ABORTED,		/**< Session is aborted. */
    } engine_session_state;

    typedef enum {
        ENGINE_SESSION_SUB_STATE_NONE,
        ENGINE_SESSION_SUB_STATE_WAIT_FOR_NIA_NOTIFICATION_RESPONSE,
        ENGINE_SESSION_SUB_STATE_WAIT_FOR_MMI_USER_RESPONSE,
    } engine_session_sub_state;

    /**
     * Others should not access the fields directly.
     */
    typedef struct {
        pthread_mutex_t mutex;

        engine_state engine_state;
        engine_session_type session_type;
        engine_session_state session_state;
        engine_session_sub_state session_sub_state;

        OMC_GlobalsPtr omc_globals;
        struct OMC_UserData_s user_data;
        TRG_InitData init_data;
        OMC_SessionDataPtr dm_session_data;
        pthread_t dm_session_tid;
        pthread_t engine_tid;

        /** @todo Java_com_mediatek_MediatekDM_mdm_MdmEngine__1setConnectionTimeout */
        int connection_timeout;


    } engine_context;

    extern engine_context *pec;

    /** Provision engine for running. It should be called when lib is loaded. */
    extern void engine_context_init();
    /** It should be called when lib is unloaded. */
    extern void engine_context_fini();

    extern int engine_init();
    extern int engine_fini();

    extern int engine_thread_init();
    extern int engine_thread_fini();

    extern void set_engine_state(engine_state engine_state);
    extern engine_state get_engine_state(void);

    extern int engine_cancel_session(void);
    extern int engine_cancel_dm_session(void);
    extern int engine_process_dm_nia_session_trigger(char *msg, int msg_length);
    extern int engine_process_dm_nia_proceed_notification(mdm_message *msg);
    extern int engine_process_session_state_notification(mdm_message *msg);

    extern int engine_get_connection_timeout(void);

#define ENGINE_CONTEXT_LOCK		pthread_mutex_lock(&(pec->mutex))
#define ENGINE_CONTEXT_UNLOCK	pthread_mutex_unlock(&(pec->mutex))

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !__ENGINE_H__ */

