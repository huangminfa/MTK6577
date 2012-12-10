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

package com.mediatek.todos;

import com.mediatek.todos.provider.TodosDatabaseHelper.TodoColumn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TodosActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "TodosActivity";

    private static final int REQUEST_ADD_NEW = 1;
    private static final int REQUEST_SHOW_DETAILS = REQUEST_ADD_NEW + 1;

    private static final int DIALOG_DELETE_ITEMS = 1;
    private boolean mShowingDialog = false;

    private TimeChangeReceiver mTimeChangeReceiver = null;

    /** display number of All Todos */
    private TextView mNumberTextView = null;
    private ImageButton mBtnSelectAll = null;
    private ImageButton mBtnDeselectAll = null;
    private ImageButton mBtnChangeState = null;
    private ImageButton mBtnDelete = null;

    /** Read all Todo infos from QB */
    private TodosListAdapter mTodosListAdapter = null;
    /** Show all Todo infos in ListView */
    private ListView mTodosListView = null;

    /** Item click & long click listener */
    private AdapterViewListener mAdapterViewListener = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items);

        initViews();

        mTimeChangeReceiver = TimeChangeReceiver.registTimeChangeReceiver(this);
        mTimeChangeReceiver.addDateChangeListener(mTodosListAdapter);
        LogUtils.d(TAG, "TodosActivity.onCreate() finished.");
    }

    private void initViews() {
        mNumberTextView = (TextView) findViewById(R.id.number);

        mTodosListAdapter = new TodosListAdapter(this, mNumberTextView);

        mAdapterViewListener = new AdapterViewListener();
        mTodosListView = (ListView) findViewById(R.id.list_todos);
        mTodosListView.setAdapter(mTodosListAdapter);
        mTodosListView.setOnItemClickListener(mAdapterViewListener);
        mTodosListView.setOnItemLongClickListener(mAdapterViewListener);

        ImageButton newTodo = (ImageButton) findViewById(R.id.btn_new_todo);
        mBtnSelectAll = (ImageButton) findViewById(R.id.btn_select_all);
        mBtnDeselectAll = (ImageButton) findViewById(R.id.btn_disselect_all);
        mBtnChangeState = (ImageButton) findViewById(R.id.btn_change_state);
        mBtnDelete = (ImageButton) findViewById(R.id.btn_delete);

        newTodo.setOnClickListener(this);
        mBtnSelectAll.setOnClickListener(this);
        mBtnDeselectAll.setOnClickListener(this);
        mBtnChangeState.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.d(TAG, "onActivityResult request=" + requestCode + " result=" + resultCode);
        TodoInfo info = null;
        switch (resultCode) {
        case Utils.OPERATOR_INSERT:
            info = (TodoInfo) data.getSerializableExtra(Utils.KEY_PASSED_DATA);
            mTodosListAdapter.addItem(info);
            final int addPos = mTodosListAdapter.getItemPosition(info);
            mTodosListView.setSelection(addPos);
            break;
        case Utils.OPERATOR_UPDATE:
            info = (TodoInfo) data.getSerializableExtra(Utils.KEY_PASSED_DATA);
            mTodosListAdapter.updateItemData(info);
            final int updatePos = mTodosListAdapter.getItemPosition(info);
            mTodosListView.setSelection(updatePos);
            break;
        case Utils.OPERATOR_DELETE:
            info = (TodoInfo) data.getSerializableExtra(Utils.KEY_PASSED_DATA);
            mTodosListAdapter.removeItem(info);
            break;
        default:
            break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mShowingDialog) {
            showDialog(DIALOG_DELETE_ITEMS);
        }
    }

    @Override
    protected void onPause() {
        if (mShowingDialog) {
            removeDialog(DIALOG_DELETE_ITEMS);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mTimeChangeReceiver.clearChangeListener();
        unregisterReceiver(mTimeChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mTodosListAdapter.isEditing()) {
            updateToEditNull();
            return;
        }
        super.onBackPressed();
    }

    private void updateToEditNull() {
        findViewById(R.id.bottom_default).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_edit).setVisibility(View.GONE);
        mTodosListAdapter.setEditingType(TodosListAdapter.EDIT_NULL);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
        case DIALOG_DELETE_ITEMS:
            dialog = new AlertDialog.Builder(this).setTitle(R.string.delete)
                    .setMessage(R.string.delete_selected_items)
                    .setIconAttribute(android.R.attr.alertDialogIcon).create();
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mShowingDialog = false;
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mTodosListAdapter.deleteSelectedItems();
                            updateToEditNull();
                            updateBottomBarWidgetState();
                            mShowingDialog = false;
                        }
                    });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    mShowingDialog = false;
                }
            });
            return dialog;
        default:
            break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
        case DIALOG_DELETE_ITEMS:
            String msg = "";
            if (mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_TODOS) {
                if (mTodosListAdapter.getSeletedTodosNumber() > 1) {
                    msg = getString(R.string.delete_selected_items);
                } else {
                    msg = getString(R.string.delete_item);
                }
                ((AlertDialog) dialog).setMessage(msg);
            } else if (mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_DONES) {
                if (mTodosListAdapter.getSeletedDonesNumber() > 1) {
                    msg = getString(R.string.delete_selected_items);
                } else {
                    msg = getString(R.string.delete_item);
                }
                ((AlertDialog) dialog).setMessage(msg);
            }
            break;
        default:
            break;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_new_todo:
            Intent intent = new Intent(TodosActivity.this, EditTodoActivity.class);
            startActivityForResult(intent, REQUEST_ADD_NEW);
            break;
        case R.id.btn_select_all:
            mTodosListAdapter.allSelectAction(true);
            updateBottomBarWidgetState();
            break;
        case R.id.btn_disselect_all:
            mTodosListAdapter.allSelectAction(false);
            updateBottomBarWidgetState();
            break;
        case R.id.btn_change_state:
            onChangeItemStateClick();
            updateBottomBarWidgetState();
            break;
        case R.id.btn_delete:
            showDialog(DIALOG_DELETE_ITEMS);
            mShowingDialog = true;
            break;
        default:
            break;
        }
    }

    void updateBottomBarWidgetState() {
        LogUtils.d(TAG, "updateBottomBarWidgetState(), editing=" + mTodosListAdapter.isEditing());
        if (mTodosListAdapter.isEditing()) {
            int selectedNumber = 0;
            int dataSourceNumber = 0;
            if (mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_TODOS) {
                selectedNumber = mTodosListAdapter.getSeletedTodosNumber();
                dataSourceNumber = mTodosListAdapter.getTodosDataSource().size();
            } else if (mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_DONES) {
                selectedNumber = mTodosListAdapter.getSeletedDonesNumber();
                dataSourceNumber = mTodosListAdapter.getDonesDataSource().size();
            }

            LogUtils.d(TAG, "selectedNumber=" + selectedNumber + ", dataSourceNumber="
                    + dataSourceNumber);
            if (dataSourceNumber == 0) {
                updateToEditNull();
            } else {
                if (selectedNumber > 0) {
                    mBtnDeselectAll.setEnabled(true);
                    mBtnChangeState.setEnabled(true);
                    mBtnDelete.setEnabled(true);
                } else {
                    mBtnDeselectAll.setEnabled(false);
                    mBtnChangeState.setEnabled(false);
                    mBtnDelete.setEnabled(false);
                }

                if (selectedNumber == dataSourceNumber) {
                    mBtnSelectAll.setEnabled(false);
                } else {
                    mBtnSelectAll.setEnabled(true);
                }
            }
        }
    }

    private void onChangeItemStateClick() {
        String currentStatus = null;
        String targetStatus = null;
        if (mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_TODOS) {
            currentStatus = TodoInfo.STATUS_TODO;
            targetStatus = TodoInfo.STATUS_DONE;
        } else if (mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_DONES) {
            currentStatus = TodoInfo.STATUS_DONE;
            targetStatus = TodoInfo.STATUS_TODO;
        }
        mTodosListAdapter.updateSelectedStatus(currentStatus, targetStatus);
        updateToEditNull();
    }

    class AdapterViewListener implements AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
            int viewType = mTodosListAdapter.getItemViewType(position);
            LogUtils.d(TAG, "onItemClick viewType =" + viewType + " position=" + position);

            switch (viewType) {
            case TodosListAdapter.TYPE_TODOS_HEADER:
                mTodosListAdapter.setTodosExpand(!mTodosListAdapter.isTodosExpand());
                break;
            case TodosListAdapter.TYPE_TODOS_FOOTER:
                if (mTodosListAdapter.isEditing()) {
                    updateToEditNull();
                }
                Intent intentAdd = new Intent(TodosActivity.this, EditTodoActivity.class);
                startActivityForResult(intentAdd, REQUEST_ADD_NEW);
                break;
            case TodosListAdapter.TYPE_DONES_HEADER:
                mTodosListAdapter.setDonesExpand(!mTodosListAdapter.isDonesExPand());
                break;
            case TodosListAdapter.TYPE_DONES_ITEM:
            case TodosListAdapter.TYPE_TODOS_ITEM:
                if (mTodosListAdapter.isEditing()) {
                    final boolean todosEditAble = viewType == TodosListAdapter.TYPE_TODOS_ITEM
                            && mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_TODOS;
                    final boolean donesEditAble = viewType == TodosListAdapter.TYPE_DONES_ITEM
                            && mTodosListAdapter.getEditType() == TodosListAdapter.EDIT_DONES;
                    if (todosEditAble || donesEditAble) {
                        Object tag = view.getTag();
                        if (tag != null && tag instanceof TodosListAdapter.ViewHolder) {
                            TodosListAdapter.ViewHolder holder = (TodosListAdapter.ViewHolder) tag;
                            final boolean checked = holder.mTodoInfoCheckBox.isChecked();
                            holder.mTodoInfoCheckBox.setChecked(!checked);
                            mTodosListAdapter.selectItem(position, !checked);
                        }
                    }
                    updateBottomBarWidgetState();
                } else {
                    Intent intentDetails = new Intent(TodosActivity.this, EditTodoActivity.class);
                    TodoInfo info = (TodoInfo) adapterView.getAdapter().getItem(position);
                    intentDetails.putExtra(Utils.KEY_PASSED_DATA, info);
                    startActivityForResult(intentDetails, REQUEST_SHOW_DETAILS);
                }
                break;
            default:
                break;
            }
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                long rowId) {
            final int viewType = mTodosListAdapter.getItemViewType(position);
            int editType = TodosListAdapter.EDIT_NULL;
            switch (viewType) {
            case TodosListAdapter.TYPE_TODOS_ITEM:
                editType = TodosListAdapter.EDIT_TODOS;
                break;
            case TodosListAdapter.TYPE_DONES_ITEM:
                editType = TodosListAdapter.EDIT_DONES;
            default:
                break;
            }
            LogUtils.d(TAG, "onItemLongClick viewType =" + viewType + " position=" + position
                    + " editType=" + editType);
            if (editType != TodosListAdapter.EDIT_NULL
                    && editType != mTodosListAdapter.getEditType()) {
                if (mTodosListAdapter.setEditingType(editType)) {
                    findViewById(R.id.bottom_default).setVisibility(View.GONE);
                    View bottomEdit = findViewById(R.id.bottom_edit);
                    bottomEdit.setVisibility(View.VISIBLE);
                    ImageButton btnChangeState = (ImageButton) bottomEdit
                            .findViewById(R.id.btn_change_state);
                    if (editType == TodosListAdapter.EDIT_TODOS) {
                        btnChangeState.setImageResource(R.drawable.todo_mark_todo);
                    } else if (editType == TodosListAdapter.EDIT_DONES) {
                        btnChangeState.setImageResource(R.drawable.todo_mark_done);
                    }
                }
            }
            if (editType != TodosListAdapter.EDIT_NULL) {
                mTodosListAdapter.selectItem(position, true);
            }
            updateBottomBarWidgetState();
            return true;
        }
    }
}
