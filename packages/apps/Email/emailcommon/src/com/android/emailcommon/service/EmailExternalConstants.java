/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.emailcommon.service;

public class EmailExternalConstants {

    public static final String OMACP_SETTING_ACTION = "com.mediatek.omacp.settings";

    public static final String OMACP_SETTING_RESULT_ACTION = "com.mediatek.omacp.settings.result";

    public static final String OMACP_CAPABILITY_ACTION = "com.mediatek.omacp.capability";

    public static final String OMAPCP_CAPABILITY_RESULT_ACTION = "com.mediatek.omacp.capability.result";
    
    public static final String ACTION_BACKGROUND_SEND = "com.android.email.action.BACKGROUND_SEND";

    public static final String ACTION_BACKGROUND_SEND_MULTIPLE = "com.android.email.action.BACKGROUND_SEND_MULTIPLE";

    public static final String ACTION_DIRECT_SEND = "com.android.email.action.DIRECT_SEND";

    public static final String ACTION_UPDATE_INBOX = "com.android.email.action.UPDATE_INBOX";
    
    /** Send result before write data complete */
    public static final String ACTION_SEND_RESULT = "com.android.email.action.SEND_RESULT";

    /** Send result after write data complete */
    public static final String ACTION_DELIVER_RESULT = "com.android.email.action.DELIVER_RESULT";

    public static final String ACTION_UPDATE_INBOX_RESULT = "com.android.email.action.UPDATE_INBOX_RESULT";    

    /** Email account id */
    public static final String EXTRA_ACCOUNT = "com.android.email.extra.ACCOUNT";
    
    /** Success(0) or Fail(1) */
    public static final String EXTRA_RESULT = "com.android.email.extra.RESULT";
    
    /*
     * Result action type for SendMail
     */
    public static final int TYPE_SEND = 1;
    public static final int TYPE_DELIVER = 2;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = 1;
}
