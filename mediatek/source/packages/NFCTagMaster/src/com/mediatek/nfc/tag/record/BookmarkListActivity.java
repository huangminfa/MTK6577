
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Bookmark;
import com.mediatek.nfc.tag.utils.Utils;

public class BookmarkListActivity extends ListActivity {
    private static final String TAG = Utils.TAG + "/BookmarkListActivity";

    private Cursor mCursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.logd(TAG, "-->onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list);

        ListView listView = getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
                    Utils.logd(TAG, "Item at position[" + index + "] is clicked, id=" + id);
                    Intent intent = new Intent();
                    intent.putExtra(Bookmark.EXTRA_SELECTED_BOOKMARK_ID, id);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }

        Utils.logv(TAG, "Target version info: \n model=" + android.os.Build.MODEL
                + ", SDK version=" + android.os.Build.VERSION.SDK + ", Release version="
                + android.os.Build.VERSION.RELEASE);
    }

    @Override
    protected void onResume() {
        Utils.logd(TAG, "-->onResume()");
        super.onResume();
        updateLayout();
    }

    @Override
    protected void onDestroy() {
        Utils.logd(TAG, "-->onDestroy()");
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void updateLayout() {
        Utils.logd(TAG, "-->updateLayout()");
        // drop items those have no URL
        mCursor = getContentResolver().query(Bookmark.BOOKMARK_URI, Bookmark.PROJECTION,
                Bookmark.COLUMN_URL + "!=\'\'", null, null);
        if (mCursor != null) {
            ListAdapter adapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_2, mCursor, new String[] {
                            Bookmark.COLUMN_TITLE, Bookmark.COLUMN_URL
                    }, new int[] {
                            android.R.id.text1, android.R.id.text2 });
            setListAdapter(adapter);
        } else {
            Utils.loge(TAG, "Fail to query bookmark data from content providers.");
        }
    }
}
