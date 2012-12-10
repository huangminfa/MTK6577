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

package com.mediatek.todos.tests;

import com.mediatek.todos.EditTodoActivity;
import com.mediatek.todos.LogUtils;
import com.mediatek.todos.TestUtils;
import com.mediatek.todos.TodoInfo;
import com.mediatek.todos.TodosActivity;
import com.mediatek.todos.TodosListAdapter;
import com.mediatek.todos.Utils;

import android.app.Dialog;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.test.TouchUtils;


public class EditTodoActivityTest extends ActivityInstrumentationTestCase2<EditTodoActivity> {

    private String TAG = "EditTodoActivityTest";
    private EditTodoActivity mEditActivity = null;
    private static final int DIALOG_DELETE_ITEMS = 1;
    private static final int DIALOG_BACK_MAIN_PAGE = 2;
    private static final int DIALOG_CANCEL_EDIT = 3;

    public EditTodoActivityTest() {
        super(EditTodoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (mEditActivity != null) {
            mEditActivity.finish();
            mEditActivity = null;
        }
        super.tearDown();
    }

    public void testDialogsLife() {
        LogUtils.v(TAG, "testDialogsLife");
        mEditActivity = getActivity();
        mEditActivity.showDialog(DIALOG_DELETE_ITEMS);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEditActivity.removeDialog(DIALOG_DELETE_ITEMS);

        mEditActivity.showDialog(DIALOG_BACK_MAIN_PAGE);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEditActivity.removeDialog(DIALOG_BACK_MAIN_PAGE);

        mEditActivity.showDialog(DIALOG_CANCEL_EDIT);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEditActivity.removeDialog(DIALOG_CANCEL_EDIT);
    }

    /**
     * Test method changeToState(),send different touch or click actions, the status should be OK.
     */
    public void test01ChangeToState() {
        LogUtils.v(TAG, "testChangeToState");
        mEditActivity = getActivity();
        // 1.a new EditTodoActivity
        Instrumentation intru = getInstrumentation();
        EditText title = (EditText) mEditActivity.findViewById(com.mediatek.todos.R.id.title);
        EditText description = (EditText) mEditActivity
                .findViewById(com.mediatek.todos.R.id.details);
        Button done = (Button) mEditActivity.findViewById(com.mediatek.todos.R.id.btn_done);
        TouchUtils.clickView(this, title);
        sendKeys(KeyEvent.KEYCODE_E);
        TouchUtils.clickView(this, description);
        sendKeys(KeyEvent.KEYCODE_F);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEditActivity.finish();
    }

    // the info does not in the DB, EditTodoActivity--status--todo detail
    public void test02ChangeToState() {
        LogUtils.v(TAG, "testChangeToState");
        Intent intentDetails = new Intent();
        TodoInfo info = new TodoInfo();
        TestUtils utils = new TestUtils();
        utils.setTitle(info, "info for EditTodoActivity");
        intentDetails.putExtra(Utils.KEY_PASSED_DATA, info);
        this.setActivityIntent(intentDetails);
        mEditActivity = getActivity();
        // 1.EditTodoActivity--status--todo detail
        Instrumentation intru = getInstrumentation();
        EditText title = (EditText) mEditActivity.findViewById(com.mediatek.todos.R.id.title);
        EditText description = (EditText) mEditActivity
                .findViewById(com.mediatek.todos.R.id.details);
        Button cancel = (Button) mEditActivity.findViewById(com.mediatek.todos.R.id.btn_cancel);
        // 2.EditTodoActivity--status--edit todo
        TouchUtils.clickView(this, title);
        sendKeys(KeyEvent.KEYCODE_C);
        TouchUtils.clickView(this, description);
        sendKeys(KeyEvent.KEYCODE_D);
        mEditActivity.finish();
    }

    // the info does not in the DB,EditTodoActivity--status--done detail
    public void test03ChangeToState() {
        LogUtils.v(TAG, "testChangeToState");
        Intent intentDetails = new Intent();
        TodoInfo info = new TodoInfo();
        TestUtils utils = new TestUtils();
        utils.setTitle(info, "done info for EditTodoActivity");
        utils.setStatus(info, TodoInfo.STATUS_DONE);
        intentDetails.putExtra(Utils.KEY_PASSED_DATA, info);
        this.setActivityIntent(intentDetails);
        mEditActivity = getActivity();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendKeys(KeyEvent.KEYCODE_BACK);
    }
}
