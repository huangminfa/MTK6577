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

package  com.android.pqtuningtool.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.util.MtkLog;

public class BookmarkActivity extends Activity implements OnItemClickListener {
    private static final String TAG = "BookmarkActivity";
    private static final boolean LOG = true;
    
    private BookmarkEnhance mBookmark;
    private BookmarkAdapter mAdapter;
    private Cursor mCursor;
    private ListView mListView;
    private TextView mEmptyView;
    
    private static final int MENU_DELETE_ALL = 1;
    private static final int MENU_DELETE_ONE = 2;
    private static final int MENU_EDIT = 3;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark);
        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        
        mBookmark = new BookmarkEnhance(this);
        mCursor = mBookmark.query();
        mAdapter= new BookmarkAdapter(this, R.layout.bookmark_item, null, new String[]{}, new int[]{});
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mAdapter);
        mAdapter.changeCursor(mCursor);
        
        mListView.setOnItemClickListener(this);
        registerForContextMenu(mListView);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_DELETE_ALL, 0, R.string.delete_all)
            .setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case MENU_DELETE_ALL:
            mBookmark.deleteAll();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class BookmarkAdapter extends SimpleCursorAdapter {

        public BookmarkAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to);
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            ViewHolder holder = new ViewHolder();
            holder.titleView = (TextView) view.findViewById(R.id.title);
            holder.dataView = (TextView) view.findViewById(R.id.data);
            view.setTag(holder);
            return view;
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder._id = cursor.getLong(BookmarkEnhance.INDEX_ID);
            holder.title = cursor.getString(BookmarkEnhance.INDEX_TITLE);
            holder.data = cursor.getString(BookmarkEnhance.INDEX_DATA);
            holder.mimetype = cursor.getString(BookmarkEnhance.INDEX_MIME_TYPE);
            holder.titleView.setText(holder.title);
            holder.dataView.setText(holder.data);
        }
        
        @Override
        public void changeCursor(Cursor c) {
            super.changeCursor(c);
        }
        
    }
    
    private class ViewHolder {
        long _id;
        String title;
        String data;
        String mimetype;
        TextView titleView;
        TextView dataView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = view.getTag();
        if (o instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) o;
            finish();
            Intent intent = new Intent(this, MovieActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            String mime = "video/*";
            if (holder.mimetype == null || "".equals(holder.mimetype.trim())) {
                //do nothing
            } else {
                mime = holder.mimetype;
            }
            intent.setDataAndType(Uri.parse(holder.data), mime);
            startActivity(intent);
        }
        if (LOG) MtkLog.v(TAG, "onItemClick(" + position + ", " + id + ")");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_DELETE_ONE, 0, R.string.delete);
        menu.add(0, MENU_EDIT, 0, R.string.edit);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
        case MENU_DELETE_ONE:
            mBookmark.delete(info.id);
            return true;
        case MENU_EDIT:
            Object obj = info.targetView.getTag();
            if (obj instanceof ViewHolder) {
                showEditDialog((ViewHolder)obj);
            } else {
                MtkLog.w(TAG, "wrong context item info " + info);
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }
    
    private void showEditDialog(final ViewHolder holder) {
        if (LOG) MtkLog.v(TAG, "showEditDialog(" + holder + ")");
        if (holder == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.bookmark_edit_dialog, null);
        final EditText titleView = (EditText)v.findViewById(R.id.title);
        final EditText dataView = (EditText)v.findViewById(R.id.data);
        titleView.setText(holder.title);
        dataView.setText(holder.data);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        builder.setView(v);
        builder.setIcon(R.drawable.ic_menu_display_bookmark);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBookmark.update(holder._id, titleView.getText().toString(),
                        dataView.getText().toString(), 0);
            }
            
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setInverseBackgroundForced(true);
        dialog.show();
    }
}
