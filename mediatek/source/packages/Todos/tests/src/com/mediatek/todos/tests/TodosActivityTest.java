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
import com.mediatek.todos.QueryListener;
import com.mediatek.todos.TestUtils;
import com.mediatek.todos.TodoAsyncQuery;
import com.mediatek.todos.TodoInfo;
import com.mediatek.todos.TodosActivity;
import com.mediatek.todos.TodosListAdapter;
import com.mediatek.todos.provider.TodosDatabaseHelper.Tables;
import com.mediatek.todos.provider.TodosDatabaseHelper.TodoColumn;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;


public class TodosActivityTest extends ActivityInstrumentationTestCase2<TodosActivity> {

    private String TAG = "TodosActivityTest";
    private TodosActivity mTodosActivity = null;
    private static final int DIALOG_DELETE_ITEMS = 1;

    public TodosActivityTest() {
        super("com.mediatek.todos", TodosActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mTodosActivity = getActivity();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        LogUtils.v(TAG, "tearDown");
        if (mTodosActivity != null) {
            mTodosActivity.finish();
            mTodosActivity = null;
        }
        super.tearDown();
    }

    /**
     * test dialogs life onCreateDialog() & onPrepareDialog()
     */
    public void testDialogsLife() {
        LogUtils.v(TAG, "testDialogsLife");
        mTodosActivity.showDialog(DIALOG_DELETE_ITEMS);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mTodosActivity.removeDialog(DIALOG_DELETE_ITEMS);
    }

    /**
     * Test method onItemClick,expand or unexpanded List; startEditActivity successfully
     */
    public void testOnItemClick() {
        LogUtils.v(TAG, "testOnItemClick");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();

        Instrumentation intru = getInstrumentation();
        ActivityMonitor am = intru.addMonitor(EditTodoActivity.class.getName(), null, false);

        int viewNumber = listView.getChildCount();
        int count = adapter.getCount();
        int todoNumber = adapter.getTodosDataSource().size();
        int doneNumber = adapter.getDonesDataSource().size();
        for (int i = 0; i < viewNumber; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            int viewType = adapter.getItemViewType(i);
            View itemView = listView.getChildAt(i);
            if (viewType == TodosListAdapter.TYPE_DONES_FOOTER) {
                TouchUtils.clickView(this, itemView);
                int showNumber = listView.getAdapter().getCount();
                assertTrue((showNumber == count));
            } else if (viewType == TodosListAdapter.TYPE_TODOS_HEADER
                    || viewType == TodosListAdapter.TYPE_DONES_HEADER) {
                // 1.test expand or unexpanded List
                LogUtils.v(TAG, "itemView" + itemView);
                TouchUtils.clickView(this, itemView);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int showNumber = listView.getAdapter().getCount();
                LogUtils.v(TAG, "showNumber" + showNumber + "count" + count);
                if (todoNumber == 0 && viewType == TodosListAdapter.TYPE_TODOS_HEADER
                        || doneNumber == 0 && viewType == TodosListAdapter.TYPE_DONES_HEADER) {
                    assertTrue((showNumber == count));
                } else {
                    assertTrue((showNumber < count));
                }
                TouchUtils.clickView(this, itemView);
            } else {
                // 2.test startEditActivity successfully
                TouchUtils.clickView(this, itemView);
                EditTodoActivity mEditActivity = null;
                mEditActivity = (EditTodoActivity) am.waitForActivityWithTimeout(500);
                if (mEditActivity != null) {
                    mEditActivity.finish();
                    mEditActivity = null;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
        }
        intru.removeMonitor(am);
    }

    /**
     * Test method onBackPressed, out of edit mode or exit the activity. 1.isEditing,change to
     * EditNull 2.is not Editing,exit the activity
     */
    public void testOnBackPressed() {
        LogUtils.v(TAG, "testOnBackPressed");
        mTodosActivity = getActivity();

        // 1.set Editing, change to EditNull
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        if (adapter.getDonesDataSource().size() == 0 && adapter.getTodosDataSource().size() == 0) {
            return;
        }
        int viewNumber = listView.getChildCount();
        LogUtils.v(TAG, "count" + viewNumber);
        // should longClick todo or done
        int i = 0;
        while (i < viewNumber) {
            int viewType = adapter.getItemViewType(i);
            if (viewType == TodosListAdapter.TYPE_TODOS_ITEM
                    || viewType == TodosListAdapter.TYPE_DONES_ITEM) {
                View view = listView.getChildAt(i);
                clickLongOnView(view);
                break;
            }
            ++i;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        assertTrue(adapter.isEditing());
        sendKeys(KeyEvent.KEYCODE_BACK);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        assertFalse(adapter.isEditing());

        // 2. back to destroy activity.
        sendKeys(KeyEvent.KEYCODE_BACK);
    }

    /**
     * test method OnItemLongClick,change edit mode, views' status should be write
     */
    public void testOnItemLongClick() {
        LogUtils.v(TAG, "testOnItemLongClick");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        int viewNumber = listView.getChildCount();
        for (int i = 0; i < viewNumber; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            int viewType = adapter.getItemViewType(i);
            if (viewType == TodosListAdapter.TYPE_TODOS_ITEM
                    || viewType == TodosListAdapter.TYPE_DONES_ITEM) {
                View view = listView.getChildAt(i);
                clickLongOnView(view);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
                assertTrue(adapter.isEditing());
                assertEquals(1, (adapter.getSeletedTodosNumber() + adapter.getSeletedDonesNumber()));
                this.sendKeys(KeyEvent.KEYCODE_BACK);
            }
        }
    }

    // ====== methods for testing TodosAsyncQuery ======
    /**
     * 
     */
    public void test01Query() {
        LogUtils.v(TAG, "test01Query()");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        preCondition(adapter);
        QueryListenerForTest queryListener = new QueryListenerForTest();
        queryListener.startQuery(null);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {}
        assertEquals(QueryListenerForTest.QUERY, queryListener.getQueryListenerData());
    }

    public void test02Insert() {
        LogUtils.v(TAG, "test02Insert()");
        QueryListenerForTest queryListener = new QueryListenerForTest();
        TodoInfo info = new TodoInfo();
        TestUtils utils = new TestUtils();
        String title = "testInsert insertTitle";
        String description = "testInsert insertDescription";
        utils.setTitle(info, title);
        utils.setDescription(info, description);
        queryListener.startInsert(info);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        assertEquals(QueryListenerForTest.INSERT, queryListener.getQueryListenerData());
    }

    /**
     * there is at least a todoInfo in database
     */
    public void test03Update() {
        LogUtils.v(TAG, "test03Update()");
        QueryListenerForTest queryListener = new QueryListenerForTest();
        TodoInfo info = new TodoInfo();
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        int todosNumber = adapter.getTodosDataSource().size();
        info = adapter.getTodosDataSource().get(todosNumber - 1);
        TestUtils utils = new TestUtils();
        String title = "update new Title";
        utils.setTitle(info, title);

        queryListener.startUpdate(info);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        assertEquals(QueryListenerForTest.UPDATE, queryListener.getQueryListenerData());
    }

    /**
     * there is at least a todoInfo in database
     */
    public void test04Delete() {
        LogUtils.v(TAG, "test04Delete()");
        QueryListenerForTest queryListener = new QueryListenerForTest();
        TodoInfo info = new TodoInfo();
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        int todosNumber = adapter.getTodosDataSource().size();
        info = adapter.getTodosDataSource().get(todosNumber - 1);
        queryListener.startDelete(info);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        assertEquals(QueryListenerForTest.DELETE, queryListener.getQueryListenerData());
    }

    public class QueryListenerForTest implements QueryListener {
        private static final String TAG1 = "QueryListenerForTest";
        public static final int QUERY = 0;
        public static final int INSERT = 1;
        public static final int UPDATE = 2;
        public static final int DELETE = 3;

        private int mQueryListenerData = -1;

        TodoAsyncQuery mQuery = null;

        QueryListenerForTest() {
        }

        public void startQuery(String selection) {
            mQuery = TodoAsyncQuery.getInstatnce(mTodosActivity.getApplicationContext());
            mQuery.startQuery(0, this, TodoAsyncQuery.TODO_URI, null, selection, null, null);
            LogUtils.d(TAG1, "startQuery");
        }

        public void onQueryComplete(int token, Cursor cur) {
            // LogUtils.d(TAG1, "onQueryComplete.");
            mQueryListenerData = QUERY;
            mQuery.free(mTodosActivity.getApplicationContext());
        }

        public void startDelete(TodoInfo info) {
            LogUtils.d(TAG1, "startDelete");
            mQuery = TodoAsyncQuery.getInstatnce(mTodosActivity.getApplicationContext());
            TestUtils mUtils = new TestUtils();
            String selection = TodoColumn._ID + "=" + mUtils.getAttribute(info, TodosInfoTest.ID);
            mQuery.startDelete(0, this, TodoAsyncQuery.TODO_URI, selection, null);
        }

        public void onDeleteComplete(int token, int result) {
            mQueryListenerData = DELETE;
            // LogUtils.d(TAG1, "onDeleteComplete.");
            mQuery.freeAll();
        }

        public void startUpdate(TodoInfo info) {
            LogUtils.d(TAG1, "startUpdate");
            mQuery = TodoAsyncQuery.getInstatnce(mTodosActivity.getApplicationContext());
            TestUtils mUtils = new TestUtils();
            String selection = TodoColumn._ID + "=" + mUtils.getAttribute(info, TodosInfoTest.ID);
            mQuery.startUpdate(0, this, TodoAsyncQuery.TODO_URI, info.makeContentValues(),
                    selection, null);
        }

        public void onUpdateComplete(int token, int result) {

            mQueryListenerData = UPDATE;
            // LogUtils.d(TAG1, "onUpdateComplete.");
        }

        public void startInsert(TodoInfo info) {
            LogUtils.d(TAG1, "startInsert");
            mQuery = TodoAsyncQuery.getInstatnce(mTodosActivity.getApplicationContext());
            mQuery.startInsert(0, this, TodoAsyncQuery.TODO_URI, info.makeContentValues());
        }

        public void onInsertComplete(int token, Uri uri) {

            mQueryListenerData = INSERT;
            // LogUtils.d(TAG1, "onInsertComplete.");
        }

        public int getQueryListenerData() {
            return mQueryListenerData;
        }
    }

    // ====== methods for testing TodosListAdapter======
    /**
     * The adapter shall get all entities from DB.
     */
    public void testConstructure() {
        LogUtils.v(TAG, "testConstructure");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        SQLiteDatabase mDataBase = SQLiteDatabase.openOrCreateDatabase(
                "/data/data/com.mediatek.todos/databases/todos.db", null);
        int count = mDataBase.query(Tables.TODOS, null, null, null, null, null, null).getCount();
        int adapterNumber = adapter.getDonesDataSource().size()
                + adapter.getTodosDataSource().size();
        assertEquals(count, adapterNumber);
    }

    /**
     * Test method: getItemViewType();Input different position values, it shall return correct
     * ViewType values
     */
    public void testGetItemViewType() {
        LogUtils.v(TAG, "testGetItemViewType");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        int viewNumber = listView.getChildCount();
        // 1.Todos is expanded
        getItemViewType(adapter, viewNumber);
        // 2.Todos is unexpanded
        View itemView = listView.getChildAt(0);
        TouchUtils.clickView(this, itemView);
        getItemViewType(adapter, listView.getChildCount());
    }

    /**
     * Test method: getItem(); Input different position values, it shall return correct Item
     * Objects.
     */
    public void testGetItem() {
        LogUtils.v(TAG, "testGetItem");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        int viewNumber = listView.getChildCount();
        // 1.Todos is expanded
        getItem(adapter, viewNumber);
        // 2.Todos is unexpanded
        View itemView = listView.getChildAt(0);
        TouchUtils.clickView(this, itemView);
        getItem(adapter, listView.getChildCount());
    }

    /**
     * Test method: addItem(); Input a item object, it could be inserted in arrayList and order
     * Comparator.
     */
    public void test05AddItem() {
        LogUtils.v(TAG, "test05AddItem");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        final TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        preCondition(adapter);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {}
        final TodoInfo info = new TodoInfo();
        info.copy(adapter.getTodosDataSource().get(0));
        final TestUtils utils = new TestUtils();
        utils.setTitle(info, "add a new info");
        utils.setDescription(info, "add a new info description");
        // utils.setStatus(info, TodoInfo.STATUS_DONE);
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.addItem(info);
                    if (utils.getAttribute(info, TodosInfoTest.STATUS) == TodoInfo.STATUS_DONE) {
                        // compare the completed time
                        TodoInfo temp = new TodoInfo();
                        int doneSize = adapter.getDonesDataSource().size();
                        LogUtils.v(TAG, "doneSize" + doneSize);
                        boolean isInfo = false;
                        for (int i = 0; i < doneSize; i++) {
                            temp = adapter.getDonesDataSource().get(i);
                            LogUtils.v(TAG, "temp" + temp);
                            if (temp == info) {
                                isInfo = true;
                            }
                            long tempCompleteTime = Long.parseLong(utils.getAttribute(temp,
                                    TodosInfoTest.COMPLETE_TIME));
                            long infoCompleteTime = Long.parseLong(utils.getAttribute(info,
                                    TodosInfoTest.COMPLETE_TIME));
                            if (!isInfo) {
                                assertTrue(tempCompleteTime >= infoCompleteTime);
                            } else {
                                assertTrue(tempCompleteTime <= infoCompleteTime);
                            }
                        }
                    } else {
                        // compare the duedate
                        TodoInfo temp = new TodoInfo();
                        int todoSize = adapter.getTodosDataSource().size();
                        LogUtils.v(TAG, "todoSize" + todoSize);
                        boolean isInfo = false;
                        for (int i = 0; i < todoSize; i++) {
                            temp = adapter.getTodosDataSource().get(i);
                            LogUtils.v(TAG, "temp" + temp);
                            if (temp == info) {
                                isInfo = true;
                            }
                            long tempDuedate = Long.parseLong(utils.getAttribute(temp,
                                    TodosInfoTest.DTEND));
                            long infoDuedate = Long.parseLong(utils.getAttribute(info,
                                    TodosInfoTest.DTEND));
                            if (!isInfo) {
                                assertTrue(tempDuedate <= infoDuedate);
                            } else {
                                assertTrue(tempDuedate >= infoDuedate);
                            }
                        }
                    }

                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Test method: updateItemData(); Modify a item's information and update, it shall in the
     * correct position.
     */
    public void test06UpdateItem() {
        LogUtils.v(TAG, "test06UpdateItem");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        final TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        final TodoInfo info = new TodoInfo();
        info.copy(adapter.getTodosDataSource().get(0));
        final TestUtils utils = new TestUtils();
        utils.setTitle(info, "update info");
        utils.setDescription(info, "update info description");
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.updateItemData(info);
                    LogUtils.v(TAG, "update-info" + adapter.getTodosDataSource().get(0));
                    assertEquals(info, adapter.getTodosDataSource().get(0));
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Test method: delteSelectedItems(); It shall remove all checked entities form DB & ArrayList
     */
    public void test07DeleteSelectedItems() {
        LogUtils.v(TAG, "test07DeleteSelectedItems");
        ListView listView = (ListView) mTodosActivity
                .findViewById(com.mediatek.todos.R.id.list_todos);
        final TodosListAdapter adapter = (TodosListAdapter) listView.getAdapter();
        final TestUtils utils = new TestUtils();
        if (adapter.getDonesDataSource().size() == 0 && adapter.getTodosDataSource().size() == 0) {
            return;
        }
        int viewNumber = listView.getChildCount();
        int i = 0;
        while (i < viewNumber) {
            int viewType = adapter.getItemViewType(i);
            if (viewType == TodosListAdapter.TYPE_TODOS_ITEM
                    || viewType == TodosListAdapter.TYPE_DONES_ITEM) {
                View view = listView.getChildAt(i);
                clickLongOnView(view);
                break;
            }
            ++i;
        }
        final TodoInfo info = new TodoInfo();
        info.copy(adapter.getItem(i));
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.deleteSelectedItems();
                    // remove from ArrayList
                    TodoInfo temp = null;
                    if (utils.getAttribute(info, TodosInfoTest.STATUS) == TodoInfo.STATUS_TODO) {
                        int todoSize = adapter.getTodosDataSource().size();
                        for (int i = 0; i < todoSize; i++) {
                            temp = adapter.getTodosDataSource().get(i);
                            if (temp == info) {
                                assertTrue(false);
                            }
                        }
                    } else {
                        int doneSize = adapter.getDonesDataSource().size();
                        for (int i = 0; i < doneSize; i++) {
                            temp = adapter.getDonesDataSource().get(i);
                            if (temp == info) {
                                assertTrue(false);
                            }
                        }
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // remove from DB
        TodoInfo todoInfo = null;
        Context mContext = mTodosActivity.getApplicationContext();
        Cursor cursor = mContext.getContentResolver().query(TodosInfoTest.TODO_URI, null, null,
                null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                todoInfo = TodoInfo.makeTodoInfoFromCursor(cursor);
                LogUtils.v(TAG, "cursor info" + todoInfo);
                if (todoInfo == info) {
                    assertTrue(false);
                }
            } while (cursor.moveToNext());
        }
    }

    private void clickLongOnView(final View view) {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.performLongClick();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void getItemViewType(TodosListAdapter adapter, int viewNumber) {
        int todoNumber = adapter.getTodosDataSource().size();
        int doneNumber = adapter.getDonesDataSource().size();
        int todoShowSize = adapter.isTodosExpand() ? todoNumber : 0;

        int position = 0;
        while (position < viewNumber) {
            int viewType = adapter.getItemViewType(position);
            LogUtils.v(TAG, "position--" + position + "viewType--" + viewType);

            if (position == 0) {
                assertEquals(TodosListAdapter.TYPE_TODOS_HEADER, viewType);
            } else if (position == 1 && todoNumber == 0) {
                assertEquals(TodosListAdapter.TYPE_TODOS_FOOTER, viewType);
            } else if (position <= todoShowSize) {
                assertEquals(TodosListAdapter.TYPE_TODOS_ITEM, viewType);
            } else if ((todoNumber == 0 && position == 2)
                    || (todoNumber != 0 && position == todoShowSize + 1)) {
                assertEquals(TodosListAdapter.TYPE_DONES_HEADER, viewType);
            } else if (doneNumber == 0) {
                assertEquals(TodosListAdapter.TYPE_DONES_FOOTER, viewType);
            } else {
                assertEquals(TodosListAdapter.TYPE_DONES_ITEM, viewType);
            }
            position++;
        }
    }

    private void getItem(TodosListAdapter adapter, int viewNumber) {
        int position = 0;
        while (position < viewNumber) {
            TodoInfo info = adapter.getItem(position);
            int viewType = adapter.getItemViewType(position);
            LogUtils.v(TAG, "position" + position + " info" + info);
            // is this info correct
            if (viewType == TodosListAdapter.TYPE_TODOS_ITEM) {
                assertEquals(adapter.getTodosDataSource().get(position - 1), info);
            } else if (viewType == TodosListAdapter.TYPE_DONES_ITEM) {
                int todoShowNumber = adapter.isTodosExpand() ? adapter.getTodosDataSource().size()
                        : 0;
                if (adapter.getTodosDataSource().size() == 0) {
                    todoShowNumber = 1;
                }
                int index = position - todoShowNumber - 2;
                assertEquals(adapter.getDonesDataSource().get(index), info);
            } else {
                assertNull(info);
            }
            position++;
        }
    }

    private void preCondition(TodosListAdapter adapter) {
        TodoInfo info = new TodoInfo();
        TestUtils utils = new TestUtils();
        String title = "ForAdapter insertTitle";
        String description = "ForAdapter insertDescription";
        utils.setTitle(info, title);
        utils.setDescription(info, description);
        adapter.startInsert(info);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {}
        // update the adapter's datasource
        adapter.startQuery(null);
    }
}
