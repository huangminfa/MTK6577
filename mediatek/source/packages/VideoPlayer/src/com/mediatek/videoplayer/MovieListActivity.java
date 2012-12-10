package com.mediatek.videoplayer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.mediatek.videoplayer.CachedThumbnail.MyDrawable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class MovieListActivity extends Activity implements OnItemClickListener {
    private static final String TAG = "MovieListActivity";
    private static final boolean LOG = true;
    
    private static final Uri Video_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = new String[]{
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATE_TAKEN,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.IS_DRM,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.STEREO_TYPE,
    };
    private static final int INDEX_ID = 0;
    private static final int INDEX_DISPLAY_NAME = 1;
    private static final int INDEX_TAKEN_DATE = 2;
    private static final int INDEX_DRUATION = 3;
    private static final int INDEX_MIME_TYPE = 4;
    private static final int INDEX_DATA = 5;
    private static final int INDEX_FILE_SIZE = 6;
    private static final int INDEX_IS_DRM = 7;
    private static final int INDEX_DATE_MODIFIED = 8;
    private static final int INDEX_SUPPORT_3D = 9;
    
    private static final String ORDER_COLUMN =
        MediaStore.Video.Media.DATE_TAKEN + " DESC, " + 
        MediaStore.Video.Media._ID + " DESC ";
    
    private ListView mListView;
    private TextView mEmptyView;
    private ViewGroup mNoSdView;
    private MovieListAdapter mAdapter;
    
    private static final int MENU_DELETE_ALL = 1;
    private static final int MENU_DELETE_ONE = 2;
    private static final int MENU_PROPERTY = 3;
    private static final int MENU_DRM_DETAIL = 4;
    
    private static final String EXTRA_ALL_VIDEO_FOLDER = "EXTRA_ALL_VIDEO_FOLDER";
    private ProgressDialog mProgressDialog;
    private static String[] mExternalStoragePaths;
    
    private CachedThumbnail mCachedManager;
    private Bitmap mDefaultDrawable;
    private CachedVideoInfo mCachedVideoInfo;
    private Bitmap mDefaultOverlay3D;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.movielist);
        StorageManager storageManager =
            (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        mExternalStoragePaths = storageManager.getVolumePaths();

        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mNoSdView = (ViewGroup) findViewById(R.id.no_sdcard);
        mAdapter = new MovieListAdapter(this, R.layout.movielist_item, null, new String[]{}, new int[]{});
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        
        mListView.setOnItemClickListener(this);
        registerForContextMenu(mListView);
        registerStorageListener();
        refreshSdStatus(MtkUtils.isMediaMounted(MovieListActivity.this));
        
        mDefaultDrawable = BitmapFactory.decodeResource(getResources(), R.drawable.ic_video_default);
        mCachedVideoInfo = new CachedVideoInfo();
        
        mDefaultOverlay3D = BitmapFactory.decodeResource(getResources(), R.drawable.ic_three_dimen);
        
        MtkLog.v(TAG, "onCreate(" + savedInstanceState + ") mDefaultDrawable=" + mDefaultDrawable);
        MtkUtils.logMemory("onCreate()");
    }
    
    private void refreshMovieList() {
        mAdapter.getQueryHandler().startQuery(0, null,
                Video_URI,
                PROJECTION,
                null,
                null,
                ORDER_COLUMN);
    }
    
    private void registerStorageListener() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        iFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        iFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        iFilter.addDataScheme("file");
        registerReceiver(mStorageListener, iFilter);
    }
    
    private BroadcastReceiver mStorageListener = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (LOG) MtkLog.v(TAG, "mStorageListener.onReceive(" + intent + ")");
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                refreshSdStatus(MtkUtils.isMediaMounted(MovieListActivity.this));
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                refreshSdStatus(MtkUtils.isMediaMounted(MovieListActivity.this));
            } if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || Intent.ACTION_MEDIA_EJECT.equals(action)){
                StorageVolume storage = (StorageVolume)intent.getParcelableExtra(
                        StorageVolume.EXTRA_STORAGE_VOLUME);
                if (storage.getPath().equals(mExternalStoragePaths[0])) {
                    refreshSdStatus(false);
                    mAdapter.changeCursor(null);
                } else {
                    //ContentObserver will listen it.
                }
                if (LOG) MtkLog.v(TAG, "mStorageListener.onReceive() eject storage=" + storage.getPath());
            }
        };

    };
    
    private void refreshSdStatus(boolean mounted) {
        if (LOG) MtkLog.v(TAG, "refreshSdStatus(" + mounted + ")");
        if (mounted) {
            if (MtkUtils.isMediaScanning(this)) {
                showScanningProgress();
                showList();
                MtkUtils.disableSpinnerState(this);
            } else {
                hideScanningProgress();
                showList();
                refreshMovieList();
                MtkUtils.enableSpinnerState(this);
            }
        } else {
            hideScanningProgress();
            showSdcardLost();
            MtkUtils.disableSpinnerState(this);
        }
    }
    
    private void showScanningProgress() {
        showProgress(getString(R.string.scanning), new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (LOG) MtkLog.v(TAG, "mProgressDialog.onCancel()");
                hideScanningProgress();
                finish();
            }

        });
    }
    
    private void hideScanningProgress() {
        hideProgress();
    }
    
    private void showProgress(String message, OnCancelListener cancelListener) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(cancelListener != null);
            mProgressDialog.setOnCancelListener(cancelListener);
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.show();
    }
    
    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    private void showSdcardLost() {
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mNoSdView.setVisibility(View.VISIBLE);
    }
    
    private void showList() {
        mListView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        mNoSdView.setVisibility(View.GONE);
    }
    
    private void showEmpty() {
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mNoSdView.setVisibility(View.GONE);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = view.getTag();
        ViewHolder holder = null;
        if (o instanceof ViewHolder) {
            holder = (ViewHolder) o;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mime = "video/*";
            if (holder.mimetype == null || "".equals(holder.mimetype.trim())) {
                //do nothing
            } else {
                mime = holder.mimetype;
            }
            intent.setDataAndType(ContentUris.withAppendedId(Video_URI, holder._id), mime);
            intent.putExtra(EXTRA_ALL_VIDEO_FOLDER, true);
            startActivity(intent);
        }
        if (LOG) MtkLog.v(TAG, "onItemClick(" + position + ", " + id + ") holder=" + holder);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Object obj = info.targetView.getTag();
        ViewHolder holder = null;
        if (obj instanceof ViewHolder) {
            holder = (ViewHolder)obj;
            menu.setHeaderTitle(holder.title);
            menu.add(0, MENU_DELETE_ONE, 0, R.string.delete);
            menu.add(0, MENU_PROPERTY, 0, R.string.media_detail);
            if (MtkUtils.isSupportDrm() && holder.isDrm) {
                menu.add(0, MENU_DRM_DETAIL, 0, com.mediatek.R.string.drm_protectioninfo_title);
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Object obj = info.targetView.getTag();
        ViewHolder holder = null;
        if (obj instanceof ViewHolder) {
            holder = (ViewHolder)obj;
        }
        switch(item.getItemId()) {
        case MENU_DELETE_ONE:
            if (holder != null) {
                showDelete(holder.clone());
            } else {
                MtkLog.w(TAG, "wrong context item info " + info);
            }
            return true;
        case MENU_PROPERTY:
            if (holder != null) {
                showDetail(holder.clone());
            } else {
                MtkLog.w(TAG, "wrong context item info " + info);
            }
            return true;
        case MENU_DRM_DETAIL:
            if (holder != null) {
                if (MtkUtils.isSupportDrm()) {
                    MtkUtils.showDrmDetails(this, holder._data);
                }
            } else {
                MtkLog.w(TAG, "wrong context item info " + info);
            }
            break;
        }
        return super.onContextItemSelected(item);
    }
    
    private void showDetail(final ViewHolder holder) {
        DetailDialog detailDialog = new DetailDialog(this, holder);
        detailDialog.setTitle(R.string.media_detail);
        detailDialog.show();
    }
    
    private void showDelete(final ViewHolder holder) {
    	if (LOG) MtkLog.v(TAG, "showDelete(" + holder + ")");
        new AlertDialog.Builder(this)
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.delete_tip, holder.title))    
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (LOG) MtkLog.v(TAG, "Delete.onClick() " + holder);
                new DeleteTask(holder).execute();
            }
            
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create()
        .show();
    }
    
    public class DeleteTask extends AsyncTask<Void, Void, Void> {
        private final ViewHolder mHolder;
        
        public DeleteTask(ViewHolder holder) {
            mHolder = holder;
        }
        
        @Override
        protected void onPreExecute() {
            showDeleteProgress(getString(R.string.delete_progress, mHolder.title));
        }
        
        @Override
        protected void onPostExecute(Void result) {
            hideDeleteProgress();
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            ViewHolder holder = mHolder;
            if (holder == null) {
                MtkLog.w(TAG, "DeleteTask.doInBackground holder=" + holder);
            } else {
                int count = 0;
                try {
                    count = getContentResolver().delete(ContentUris.withAppendedId(Video_URI, holder._id), null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (LOG) MtkLog.v(TAG, "DeleteTask.doInBackground delete count=" + count);
            }
            return null; 
        }
        
    }
    
    private void showDeleteProgress(String message) {
        showProgress(message, null);
    }
    
    private void hideDeleteProgress() {
        hideProgress();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {//update drm icon
            mAdapter.notifyDataSetChanged();
        }
        mCachedVideoInfo.setLocale(Locale.getDefault());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    private CachedThumbnail getCachedManager() {
        if (mCachedManager == null) {
            mCachedManager = CachedThumbnail.getCachedManager(this, mDefaultDrawable, mDefaultOverlay3D);
            mCachedManager.addListener(mAdapter);
        }
        return mCachedManager;
    }
    
    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.clearCachedHolder();
            mAdapter.changeCursor(null);
        }
        if (mCachedManager != null) {
            mCachedManager.removeListener(mAdapter);
            mCachedManager.clearCachedPreview();
        }
        mCachedVideoInfo.setLocale(null);
        unregisterReceiver(mStorageListener);
        super.onDestroy();
        
        MtkUtils.logMemory("onDestroy()");
    }
    
    class MovieListAdapter extends SimpleCursorAdapter implements CachedThumbnail.DrawableStateListener, OnScrollListener {
        private QueryHandler mQueryHandler;
        QueryHandler getQueryHandler() {
            return mQueryHandler;
        }
        
        public MovieListAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to);
            mQueryHandler = new QueryHandler(getContentResolver());
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            ViewHolder holder = new ViewHolder();
            holder.icon = (ImageView) view.findViewById(R.id.item_icon);
            holder.titleView = (TextView) view.findViewById(R.id.item_title);
            holder.fileSizeView = (TextView) view.findViewById(R.id.item_date);
            holder.durationView = (TextView) view.findViewById(R.id.item_duration);
            int width = 60;
            int height = 60;
            if (mDefaultDrawable != null) {
                width = mDefaultDrawable.getWidth();
                height = mDefaultDrawable.getHeight();
            }
            holder.fastDrawable = new FastBitmapDrawable(width, height);
            view.setTag(holder);
            mCachedHolder.add(holder);
            if (LOG) MtkLog.v(TAG, "newView() mCachedHolder.size()=" + mCachedHolder.size());
            return view;
        }
        
        private ArrayList<ViewHolder> mCachedHolder = new ArrayList<ViewHolder>();
        public void onChanged(long rowId, int type, Bitmap drawable) {
            if (LOG) MtkLog.v(TAG, "onChanged(" + rowId + ", " + type + ", " + drawable + ")");
            for(ViewHolder holder : mCachedHolder) {
                if (holder._id == rowId) {
                    refreshDrawable(holder);
                    break;
                }
            }
        }
        
        public void clearCachedHolder() {
            mCachedHolder.clear();
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder._id = cursor.getLong(INDEX_ID);
            holder.title = cursor.getString(INDEX_DISPLAY_NAME);
            holder.dateTaken = cursor.getLong(INDEX_TAKEN_DATE);
            holder.mimetype = cursor.getString(INDEX_MIME_TYPE);
            holder._data = cursor.getString(INDEX_DATA);
            holder.fileSize = cursor.getLong(INDEX_FILE_SIZE);
            holder.duration = cursor.getLong(INDEX_DRUATION);
            holder.isDrm = "1".equals(cursor.getString(INDEX_IS_DRM));
            holder.dateModified = cursor.getLong(INDEX_DATE_MODIFIED);
            holder.support3D = MtkUtils.isStereo3D(cursor.getString(INDEX_SUPPORT_3D));
            
            holder.titleView.setText(holder.title);
            holder.fileSizeView.setText(mCachedVideoInfo.getFileSize(MovieListActivity.this, holder.fileSize));
            holder.durationView.setText(mCachedVideoInfo.getDuration(holder.duration));
            refreshDrawable(holder);
            //if (LOG) MtkLog.v(TAG, "bindeView() " + holder);
        }
        
        private void refreshDrawable(ViewHolder holder) {
            Bitmap bitmap = getCachedManager().getCachedPreview(holder._id, holder.dateModified, holder.support3D, !mFling);
            if (MtkUtils.isSupportDrm() && holder.isDrm) {
                bitmap = MtkUtils.overlayDrmIcon(MovieListActivity.this, holder._data, DrmStore.Action.PLAY, bitmap);
            } else {
                //do nothing
            }
            holder.fastDrawable.setBitmap(bitmap);
            holder.icon.setImageDrawable(holder.fastDrawable);
            holder.icon.invalidate();
        }
        
        @Override
        public void changeCursor(Cursor c) {
            super.changeCursor(c);
        }
        
        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            mQueryHandler.onQueryComplete(0, null, getCursor());
        }
        
        class QueryHandler extends AsyncQueryHandler {

            QueryHandler(ContentResolver cr) {
                super(cr);
            }
            
            @Override
            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                if (LOG) Log.v(TAG, "onQueryComplete(" + token + "," + cookie + "," + cursor + ")");
                MtkUtils.disableSpinnerState(MovieListActivity.this);
                if (cursor == null || cursor.getCount() == 0) {
                    showEmpty();
                    if (cursor != null) {//to observe database change
                        changeCursor(cursor);
                    }
                } else {
                    showList();
                    changeCursor(cursor);
                }
                if (LOG && cursor != null) Log.i(TAG, "onQueryComplete() end");
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            
        }

        private boolean mFling = false;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch(scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                mFling = false;
                //notify data changed to load bitmap from mediastore.
                notifyDataSetChanged();
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mFling = false;
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mFling = true;
                break;
            }
            if (LOG) MtkLog.v(TAG, "onScrollStateChanged(" + scrollState + ") mFling=" + mFling);
        }
    }
    
    class ViewHolder {
        long _id;
        String title;
        String mimetype;
        String _data;
        Long duration;
        Long dateTaken;
        Long fileSize;
        boolean isDrm;
        long dateModified;
        boolean support3D;
        
        ImageView icon;
        TextView titleView;
        TextView fileSizeView;
        TextView durationView;
        FastBitmapDrawable fastDrawable;
        
        @Override
        public String toString() {
            return new StringBuilder()
            .append("ViewHolder(_id=")
            .append(_id)
//            .append(", title=")
//            .append(title)
//            .append(", mimetype=")
//            .append(mimetype)
//            .append(", duration=")
//            .append(duration)
            .append(", isDrm=")
            .append(isDrm)
//            .append(", dateTaken=")
//            .append(dateTaken)
            .append(", _data=")
            .append(_data)
//            .append(", fileSize=")
//            .append(fileSize)
//            .append(", dateModified=")
//            .append(dateModified)
            .append(")")
            .toString();
        }
        
        /**
         * just clone info
         */
        protected ViewHolder clone() {
        	ViewHolder holder = new ViewHolder();
        	holder._id = _id;
        	holder.title = title;
        	holder.mimetype = mimetype;
        	holder._data = _data;
        	holder.duration = duration;
        	holder.dateTaken = dateTaken;
        	holder.fileSize = fileSize;
        	holder.isDrm = isDrm;
        	holder.dateModified = dateModified;
        	return holder;
        }
    }
    
  //copied from com.android.music.MusicUtils.java
  //A really simple BitmapDrawable-like class, that doesn't do
  //scaling, dithering or filtering.
  class FastBitmapDrawable extends Drawable {
      private static final String TAG = "FastBitmapDrawable";
      private static final boolean LOG = true;
      
      private Bitmap mBitmap;
      private final int mWidth;
      private final int mHeight;
      
      public FastBitmapDrawable(int width, int height) {
        mWidth = width;
        mHeight = height;
      }
      
      @Override
      public void draw(Canvas canvas) {
          if (mBitmap != null) {
              canvas.drawBitmap(mBitmap, 0, 0, null);
          }
      }
      
      @Override
      public int getOpacity() {
        return PixelFormat.OPAQUE;
      }
      
      @Override
      public void setAlpha(int alpha) {
      }
      
      @Override
      public void setColorFilter(ColorFilter cf) {
      }
      
      @Override
      public int getIntrinsicWidth() {
          return mWidth;
      }
      
      @Override
      public int getIntrinsicHeight() {
          return mHeight;
      }
      
      public void setBitmap(Bitmap bitmap) {
          mBitmap = bitmap;
      }
      
      public Bitmap getBitmap() {
          return mBitmap;
      }
  }
}
