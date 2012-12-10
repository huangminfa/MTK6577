package com.android.phone;

import static com.android.phone.CallRejectContentData.UserTableData.*;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class CallRejectContentProvider extends ContentProvider {  
      
    private CallRejectDBOpenHelper dbOpenHelper = null;            
         
    @Override    
    public boolean onCreate() {   
        dbOpenHelper = new CallRejectDBOpenHelper(this.getContext(), CallRejectContentData.DATABASE_NAME, CallRejectContentData.DATABASE_VERSION);  
        return true;    
    }    
 
    @Override    
    public Uri insert(Uri uri, ContentValues values){    
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();    
        long id = 0;    
          
        switch (uriMatcher.match(uri)) {    
        case REJECTS:    
            id = db.insert("list", null, values);
            return ContentUris.withAppendedId(uri, id);    
        case REJECT:    
            id = db.insert("list", null, values);   
            String path = uri.toString();    
            return Uri.parse(path.substring(0, path.lastIndexOf("/"))+id);
        default:    
            throw new IllegalArgumentException("Unknown URI " + uri);    
        }  
    }    
        
    @Override    
    public int delete(Uri uri, String selection, String[] selectionArgs) {    
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();    
        int count = 0;    
        switch (uriMatcher.match(uri)) {    
        case REJECTS:    
            count = db.delete("list", selection, selectionArgs);    
            break;    
        case REJECT:        
            long personid = ContentUris.parseId(uri);    
            String where = "_ID=" + personid;
            where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
            count = db.delete("list", where, selectionArgs);    
            break;    
        default:    
            throw new IllegalArgumentException("Unknown URI " + uri);    
        }    
        db.close();    
        return count;    
    }    
    
    @Override    
    public int update(Uri uri, ContentValues values, String selection,    
            String[] selectionArgs) {    
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();    
        int count = 0;    
        switch (uriMatcher.match(uri)) {    
        case REJECTS:    
            count = db.update("list", values, selection, selectionArgs);    
            break;    
        case REJECT:        
            long personid = ContentUris.parseId(uri);    
            String where = "_ID=" + personid;
            where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
            count = db.update("list", values, where, selectionArgs);    
            break;    
        default:    
            throw new IllegalArgumentException("Unknown URI " + uri);    
        }    
        db.close();    
        return count;    
    }    
        
    @Override    
    public String getType(Uri uri) {    
        switch (uriMatcher.match(uri)) {    
        case REJECTS:    
            return CONTENT_TYPE;    
        case REJECT:    
            return CONTENT_TYPE_ITME;    
        default:    
            throw new IllegalArgumentException("Unknown URI " + uri);    
        }    
    }    
    
    @Override    
    public Cursor query(Uri uri, String[] projection, String selection,    
            String[] selectionArgs, String sortOrder) {    
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();    
        switch (uriMatcher.match(uri)) {    
        case REJECTS:    
            return db.query("list", projection, selection, selectionArgs, null, null, sortOrder);    
        case REJECT:    
            long personid = ContentUris.parseId(uri);    
            String where = "_ID=" + personid;
            where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
            return db.query("list", projection, where, selectionArgs, null, null, sortOrder);    
        default:    
            throw new IllegalArgumentException("Unknown URI " + uri);    
        }    
    }    
} 
