package com.mediatek.media3d;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

public abstract class MediaDbItemSet implements MediaItemSet {

    protected Cursor mCursor;
    protected ContentResolver mContentResolver;
    protected ContentObserver mContentObserver;
    protected Uri mContentUri;
    final protected String mBucketId;

    protected MediaDbItemSet(ContentResolver cr, Uri contentUri, String bucketId) {
        mContentResolver = cr;
        mContentUri = contentUri;
        mBucketId = bucketId;
    }

    abstract protected MediaDbItem getItemAtCursor(Cursor c);

    // put selection string here. e.g. bucket id
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected String selection() {
        return null;
    }

    @SuppressWarnings({"PMD.ReturnEmptyArrayRatherThanNull", "PMD.EmptyMethodInAbstractClassShouldBeAbstract"})
    protected String[] selectionArgs() {
        return null;
    }

    // put sort order here. e.g. by date
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected String sortOrder() {
        return null;
    }

    // put projection here. the columns
    @SuppressWarnings({"PMD.ReturnEmptyArrayRatherThanNull", "PMD.EmptyMethodInAbstractClassShouldBeAbstract"})
    protected String[] projection() {
        return null;
    }

    private Cursor getCursor() {
        if (mCursor == null) {
            mCursor = query();
        }
        return mCursor;
    }

    public int getItemCount() {
        Cursor c = getCursor();
        if (c == null) return 0;

        synchronized (this) {
            return c.getCount();
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public MediaDbItem getItem(int i) {
        Cursor c = getCursor();
        MediaDbItem ret = null;
        if (c != null) {
            ret = c.moveToPosition(i) ? getItemAtCursor(c) : null;
        }
        return ret;
    }

    protected Uri getCurrentUri(long id) {
        return ContentUris.withAppendedId(mContentUri, id);
    }

    public void registerObserver(ContentObserver observer) {
        synchronized (this) {
            if (observer != null) {
                Cursor cursor = getCursor();
                if (cursor != null) {
                    cursor.registerContentObserver(observer);
                    mContentObserver = observer;
                }
            }
        }
    }

    public void unregisterObserver() {
        synchronized (this) {
            if (mContentObserver != null) {
                Cursor cursor = getCursor();
                if (cursor != null) {
                    cursor.unregisterContentObserver(mContentObserver);
                    mContentObserver = null;
                }
            }
        }
    }

    public Cursor query() {
       return mContentResolver.query(
               mContentUri, projection(), selection(), selectionArgs(), sortOrder());
    }

    public boolean peekChange() {
        if (mCursor == null) {
            return true;
        }

        boolean isChanged = false;
        final Cursor cursor = query();
        if (cursor != null) {
            isChanged = (cursor.getCount() != getItemCount());
            cursor.close();
        }
        return isChanged;
    }

    /**
     * close the list before discard it.
     */
    public void close() {
        unregisterObserver();
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    /**
     * Dump the media item set.
     *
     * @return dump string
     */
    public String toString() {
        if (mCursor == null || isEmpty()) {
            return "empty Cursor";
        } else {
            StringBuilder sb = new StringBuilder();
            final int size = mCursor.getCount();
            mCursor.moveToFirst();
            for (int i = 0; i < size; i++) {
                sb.append(String.format("%s\n", getItemAtCursor(mCursor).toString()));
                mCursor.moveToNext();
            }
            return sb.toString();
        }
    }
}
