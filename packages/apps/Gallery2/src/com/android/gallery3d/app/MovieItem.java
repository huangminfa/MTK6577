package com.android.gallery3d.app;

import java.util.ArrayList;

import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.MtkLog;
import com.android.gallery3d.util.MtkUtils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;

interface MovieInfoUpdateListener {
    void onListFilled();
}

public class MovieItem {
    private static final String TAG = "MovieItem";
    private static final boolean LOG = true;
    
    private Uri uri;
    private String mimeType;
    private String title;
    private boolean error;
    private Boolean support3D;
    private int stereoLayout = -1;
    private int convergence = -1;
    private int id = -1;
    private MovieList list;
    
    public MovieItem(Uri uri, String mimeType, String title, boolean support3D) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.title = title;
        this.support3D = support3D;
    }
    
    public MovieItem(String uri, String mimeType, String title,  boolean support3D) {
        this(Uri.parse(uri), mimeType, title, support3D);
    }
    
    public Uri getUri() {
        return uri;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public boolean getError() {
        return error;
    }
    
    public boolean getSupport3D() {
        return support3D;
    }
    
    public int getStereoLayout() {
        return stereoLayout;
    }

    public int getId() {
        return id;
    }

    public int getConvergence() {
        return convergence;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setUri(Uri uri) {
        this.uri = uri;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public void setSupport3D(boolean support) {
        this.support3D = support;
    }
    
    public void setStereoLayout(int stereoLayout) {
        this.stereoLayout = stereoLayout;
    }

    public void setConvergence(int convergence) {
        this.convergence = convergence;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setError() {
        error = true;
    }
    
    public void setList(MovieList list) {
        this.list = list;
        if (LOG) MtkLog.v(TAG, "setList(" + list + ")");
    }
    
    public boolean isLast() {
        boolean last = false;
        if (list == null || list.isLast()) {
            last = true;
        }
        if (LOG) MtkLog.v(TAG, "isLast() return " + last);
        return last;
    }
    
    public boolean isFirst() {
        boolean first = false;
        if (list == null || list.isFirst()) {
            first = true;
        }
        if (LOG) MtkLog.v(TAG, "isFirst() return " + first);
        return first;
    }
    
    public MovieItem getNext() {
        MovieItem next = null;
        if (list != null) {
            next = list.moveToNext();
        }
        return next;
    }
    
    public MovieItem getPrevious() {
        MovieItem prev = null;
        if (list != null) {
            prev = list.moveToPrevious();
        }
        return prev;
    }
    
    //for video list feature
    public static final String EXTRA_ALL_VIDEO_FOLDER = "EXTRA_ALL_VIDEO_FOLDER";
    public static final String EXTRA_ORDERBY = "ORDERBY";
    //for video list extra
    public static final String EXTRA_ENABLE_VIDEO_LIST = "mediatek.intent.extra.enableVideoList";
    
    private MovieListFetcherTask mListTask;
    public void fillVideoList(Context context, Intent intent, MovieInfoUpdateListener l) {
        boolean fetechAll = false;
        if (intent.hasExtra(EXTRA_ALL_VIDEO_FOLDER)) {
            fetechAll = intent.getBooleanExtra(EXTRA_ALL_VIDEO_FOLDER, false);
        } else {
            if (LOG) Log.v(TAG, "fillVideoList() no all video folder.");
        }
        //default order by
        String orderBy = MediaStore.Video.Media.DATE_TAKEN + " DESC, " + MediaStore.Video.Media._ID + " DESC ";
        if (intent.hasExtra(EXTRA_ORDERBY)) {
            orderBy = intent.getStringExtra(EXTRA_ORDERBY);
        }
        cancelList();
        mListTask = new MovieListFetcherTask(context, fetechAll, l, orderBy);
        mListTask.execute(this);
    }
    
    public boolean isEnabledVideoList(Intent intent) {
        boolean enable = true;
        if (intent != null && intent.hasExtra(EXTRA_ENABLE_VIDEO_LIST)) {
            enable = intent.getBooleanExtra(EXTRA_ENABLE_VIDEO_LIST, true);
        }
        if (LOG) MtkLog.v(TAG, "isEnabledVideoList() return " + enable);
        return enable;
    }
    
    public void cancelList() {
        if (mListTask != null) {
            mListTask.cancel(true);
        }
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("MovieItem(uri=")
        .append(uri)
        .append(", mime=")
        .append(mimeType)
        .append(", title=")
        .append(title)
        .append(", error=")
        .append(error)
        .append(", support3D=")
        .append(support3D)
        .append(")")
        .toString();
    }
}

class MovieList {
    private static final String TAG = "MovieList";
    private static final boolean LOG = true;
    
    private int mIndex = 0;
    private int mSize = 0;
    private ArrayList<MovieItem> mItems = new ArrayList<MovieItem>();
    
    public void add(MovieItem item) {
        if (LOG) MtkLog.v(TAG, "add(" + item + ")");
        mItems.add(item);
        item.setList(this);
        mSize++;
    }
    
    private boolean validIndex(int index) {
        boolean valid = false;
        if (index >= 0 && index < mSize) {
            valid = true;
        }
        return valid;
    }
    
    public MovieItem get(int index) {
        MovieItem item = null;//use enter item
        if (validIndex(index)) {
            item = mItems.get(index);
        }
        if (LOG) MtkLog.v(TAG, "get(" + index + ") return " + item);
        return item;
    }
    
    public MovieItem moveToNext() {
        mIndex++;
        if (mIndex >= mSize) {
            mIndex -= mSize;
        }
        if (LOG) MtkLog.v(TAG, "moveToNext() end mIndex=" + mIndex + ", mSize=" + mSize);
        return get(mIndex);
    }
    
    public MovieItem moveToPrevious() {
        mIndex--;
        if (mIndex < 0) {
            mIndex += mSize;
        }
        if (LOG) MtkLog.v(TAG, "moveToPrevious() end mIndex=" + mIndex + ", mSize=" + mSize);
        return get(mIndex);
    }
    
    public void moveTo(int position) {
        mIndex = position;
        if (mIndex >= mSize) {
            mIndex -= mSize;
        }
        if (mIndex < 0) {
            mIndex += mSize;
        }
        if (LOG) MtkLog.v(TAG, "moveTo(" + position + ") end mIndex=" + mIndex + ", mSize=" + mSize);
    }
    
    public int index() {
        return mIndex;
    }
    
    public int size() {
        return mSize;
    }
    
    public boolean isLast() {
        return (mIndex == (mSize - 1));
    }
    
    public boolean isFirst() {
        return (mIndex == 0);
    }
}

class MovieListFetcherTask extends AsyncTask<MovieItem, Void, Void> {
    private static final String TAG = "MovieListFetcherTask";
    private static final boolean LOG = true;
    
    public static final String COLUMN_SUPPORT_3D = MediaStore.Video.Media.STEREO_TYPE;
    
    
    private ContentResolver mCr;
    private MovieInfoUpdateListener mFetecherListener;
    private boolean mFetechAll;
    private final String mOrderBy;
    
    public MovieListFetcherTask(Context context, boolean fetechAll, MovieInfoUpdateListener l, String orderBy) {
        mCr = context.getContentResolver();
        mFetecherListener = l;
        mFetechAll = fetechAll;
        mOrderBy = orderBy;
        if (LOG) Log.v(TAG, "MovieListFetcherTask() fetechAll=" + fetechAll + ", orderBy=" + orderBy);
    }
    
    @Override
    protected void onPostExecute(Void params) {
        if (LOG) Log.v(TAG, "onPostExecute() isCancelled()=" + isCancelled());
        if (isCancelled()) return;
        if (mFetecherListener != null) {
            mFetecherListener.onListFilled();
        }
    }
    
    @Override
    protected Void doInBackground(MovieItem... params) {
        if (LOG) Log.v(TAG, "doInBackground() begin");
        if (params[0] == null) {
            return null;
        }
        Uri uri = params[0].getUri();
        String mime = params[0].getMimeType();
        if (mFetechAll) {//get all list
            if (MtkUtils.isLocalFile(uri, mime)) {
                String uristr = String.valueOf(uri);
                if (uristr.toLowerCase().startsWith("content://media")) {
                    //from gallery, gallery3D, videoplayer
                    long curId = Long.parseLong(uri.getPathSegments().get(3));
                    fillUriList(null, null, curId, params[0]);
                } else if (uristr.toLowerCase().startsWith("file://")) {
                    //will not occur
                }
            } else {
                //do nothing
            }
        } else {//get current list
            if (MtkUtils.isLocalFile(uri, mime)) {
                String uristr = String.valueOf(uri);
                if (uristr.toLowerCase().startsWith("content://media")) {
                    Cursor cursor = mCr.query(uri,
                            new String[]{MediaStore.Video.Media.BUCKET_ID},
                            null, null, null);
                    long bucketId = -1;
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            bucketId = cursor.getLong(0);
                        }
                        cursor.close();
                    }
                    long curId = Long.parseLong(uri.getPathSegments().get(3));
                    fillUriList(MediaStore.Video.Media.BUCKET_ID + "=? ",
                            new String[]{String.valueOf(bucketId)}, curId, params[0]);
                } else if (uristr.toLowerCase().startsWith("file://")) {
                    String data = Uri.decode(uri.toString());
                    data = data.replaceAll("'", "''");
                    String where = "_data LIKE '%" + data.replaceFirst("file:///", "") + "'";
                    Cursor cursor = mCr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            new String[]{"_id", MediaStore.Video.Media.BUCKET_ID},
                            where, null, null);
                    long bucketId = -1;
                    long curId = -1;
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            curId = cursor.getLong(0);
                            bucketId = cursor.getLong(1);
                        }
                        cursor.close();
                    }
                    fillUriList(MediaStore.Video.Media.BUCKET_ID + "=? ",
                            new String[]{String.valueOf(bucketId)}, curId, params[0]);
                }
            } else {
                //do nothing
            }
        }
        if (LOG) Log.v(TAG, "doInBackground() done");
        return null;
    }
    
    private MovieList fillUriList(String where, String[] whereArgs, long curId, MovieItem current) {
        MovieList movieList = null;
        Cursor cursor = null;
        try {
            cursor = mCr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{"_id", "mime_type", VideoColumns.TITLE, COLUMN_SUPPORT_3D},
                    where,
                    whereArgs,
                    mOrderBy);
            boolean find = false;
            if (cursor != null) {
                movieList = new MovieList();
                while(cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    if (!find && id == curId) {
                        find = true;
                        movieList.add(current);
                        movieList.moveTo(movieList.size() - 1);
                        continue;
                    }
                    Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    String mimeType = cursor.getString(1);
                    String title = cursor.getString(2);
                    String support3D = cursor.getString(3);
                    boolean support = MtkUtils.isStereo3D(support3D);
                    movieList.add(new MovieItem(uri, mimeType, title, support));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) MtkLog.v(TAG, "fillUriList() cursor=" + cursor + ", return " + movieList);
        return movieList;
    }
}
