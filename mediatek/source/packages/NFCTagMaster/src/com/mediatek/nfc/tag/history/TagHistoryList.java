package com.mediatek.nfc.tag.history;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.history.TagHistoryAdapter.ViewHolder;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Utils;

public class TagHistoryList extends ListActivity {
    private static final String TAG = Utils.TAG + "/TagHistoryList";

    private static final int CONTEXT_MENU_VIEW = 1;

    private static final int CONTEXT_MENU_DELETE = 2;

    private static final int OPTION_MENU_CLEAR = 3;

    TagHistoryAdapter mAdapter;

    private int mSelectedUriId;

    private int mTagIsCreatedByMe = 0;

    private String mQueryConditionStr = null;

    private TextView mEmptyView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTagIsCreatedByMe = getIntent().getIntExtra(TagContract.COLUMN_IS_CREATED_BY_ME, 0);
        Utils.logi(TAG, "-->onCreate(), tagIsCreatedByMe=" + mTagIsCreatedByMe);
        mQueryConditionStr = TagContract.COLUMN_IS_CREATED_BY_ME + "=" + mTagIsCreatedByMe;
        setContentView(R.layout.history_list);
        mEmptyView = (TextView) findViewById(R.id.history_list_empty);

        mAdapter = new TagHistoryAdapter(this);
        setListAdapter(mAdapter);

        new HistoryLoadTask().execute(new String[] {
            mQueryConditionStr
        });

        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        Utils.logi(TAG, "-->onResume()");
        super.onResume();
        new HistoryLoadTask().execute(new String[] {
            mQueryConditionStr
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.changeCursor(null);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        Utils.logd(TAG, "-->onListItemClick(), position=" + position + ", id=" + id + ", tag type="
                + viewHolder.mTagType);
        Intent intent = new Intent(this, HistoryViewFrame.class);
        intent.putExtra("tag_type", viewHolder.mTagType);
        intent.putExtra("record_id", (int) id);
        intent.putExtra(TagContract.COLUMN_IS_CREATED_BY_ME, mTagIsCreatedByMe);

        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        Utils.logd(TAG, "-->onCreateContextMenu()");
        if (menuInfo instanceof AdapterContextMenuInfo) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            int recordId = (int) info.id;
            Utils.logi(TAG, "Try to create content menu, whose Uri id=" + recordId);
            if (recordId > 0) {
                Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, recordId);
                Cursor cursor = getContentResolver().query(uri, new String[] {
                    TagContract.COLUMN_HISTORY_TITLE
                }, null, null, null);
                String titleStr = "Options";
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        titleStr = cursor.getString(0);
                    } else {
                        Utils.loge(TAG, "Fail to get history record info for uri whose id="
                                + recordId + ", no record");
                    }
                    cursor.close();
                } else {
                    Utils.loge(TAG, "Fail to get history record info for uri whose id=" + recordId
                            + ", null cursor");
                }
                mSelectedUriId = recordId;
                menu.setHeaderIcon(android.R.drawable.ic_dialog_info);
                menu.setHeaderTitle(titleStr);
                menu.add(Menu.NONE, CONTEXT_MENU_VIEW, Menu.NONE, R.string.history_menu_view);
                menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.history_menu_delete);
            } else {
                Utils.loge(TAG, "Fail to create context menu for item whose uri id = " + recordId);
            }
        } else {
            Utils.loge(TAG, "Menu item is not instance of AdapterContextMenuInfo");
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Utils.logd(TAG, "-->onContextItemSelected(), item id=" + item.getItemId());
        switch (item.getItemId()) {
            case CONTEXT_MENU_VIEW:
                viewRecord();
                break;
            case CONTEXT_MENU_DELETE:
                deleteRecord();
                break;
            default:
                throw new IllegalArgumentException("Unknown item option, id=" + item.getItemId());
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, OPTION_MENU_CLEAR, 0, R.string.history_menu_clear);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem clearItem = menu.findItem(OPTION_MENU_CLEAR);
        if (clearItem != null) {
            clearItem.setEnabled(mAdapter.getCount() > 0);
        } else {
            Utils.loge(TAG, "Could not find clear record menu item.");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == OPTION_MENU_CLEAR) {
            clearAllRecord();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewRecord() {
        Utils.logd(TAG, "-->viewRecord(), mSelectedUriId=" + mSelectedUriId);
        Intent intent = new Intent(this, HistoryViewFrame.class);
        intent.putExtra("record_id", mSelectedUriId);

        startActivity(intent);
    }

    private void deleteRecord() {
        Utils.logd(TAG, "-->deleteRecord(), mSelectedUriId=" + mSelectedUriId);

        Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, mSelectedUriId);
        int deleteRowNum = getContentResolver().delete(uri, null, null);
        if (deleteRowNum <= 0) {
            Utils.logw(TAG, "Could not delete history record with uri _id[" + mSelectedUriId + "]");
        } else {
            Utils.logd(TAG, "[" + deleteRowNum + "] history record is deleted.");
        }
        new HistoryLoadTask().execute(new String[] {
            mQueryConditionStr
        });
    }

    private void clearAllRecord() {
        Utils.logd(TAG, "-->clearAllRecord()");
        int deleteRowNum = getContentResolver().delete(TagContract.TAGS_CONTENT_URI,
                mQueryConditionStr, null);
        if (deleteRowNum <= 0) {
            Utils.logw(TAG, "Could not clear history record with condition [" + mQueryConditionStr
                    + "]");
        } else {
            Utils.logd(TAG, "[" + deleteRowNum + "] history record is deleted.");
        }
        new HistoryLoadTask().execute(new String[] {
            mQueryConditionStr
        });
    }

    static final String[] PROJECTION = new String[] {
            TagContract.COLUMN_ID, // 0
            TagContract.COLUMN_TYPE, // 1
            TagContract.COLUMN_HISTORY_TITLE, // 2
            TagContract.COLUMN_DATE
    // 3
    };

    final class HistoryLoadTask extends AsyncTask<String, Void, Cursor> {
        @Override
        protected Cursor doInBackground(String... params) {
            String orderStr = TagContract.COLUMN_DATE + " DESC ";
            String conditionStr = null;
            if (params != null && params.length > 0) {
                conditionStr = params[0];
            }
            Utils.logd(TAG, "Begin to load history record list in background, conditionStr="
                    + conditionStr);
            Cursor cursor = getContentResolver().query(TagContract.TAGS_CONTENT_URI, PROJECTION,
                    conditionStr, null, orderStr);
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            if (result == null || result.getCount() <= 0) {
                Utils.logw(TAG, "Tag history is empty");
                if (mEmptyView != null) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            } else {
                if (mEmptyView != null) {
                    mEmptyView.setVisibility(View.GONE);
                }
            }
            mAdapter.changeCursor(result);
        }
    }
}

class TagHistoryQuery {
    static final int COLUMN_ID = 0;

    static final int COLUMN_DATE = 1;

    static final int COLUMN_TITLE = 2;

    static final int COLUMN_STARRED = 3;
}
