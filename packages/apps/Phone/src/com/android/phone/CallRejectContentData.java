package com.android.phone;

import android.content.UriMatcher;
import android.provider.BaseColumns;

public class CallRejectContentData {  
    public static final String AUTHORITY = "reject";  
    public static final String DATABASE_NAME = "reject.db";  
    public static final int DATABASE_VERSION = 4;  
    public static final String USERS_TABLE_NAME = "list";  
      
    public static final class UserTableData implements BaseColumns {  
        public static final String TABLE_NAME = "list";  
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.android.rejects";  
        public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/com.android.reject";  
        public static final int REJECTS = 1;    
        public static final int REJECT = 2;    
               
        public static final String NUMBER = "Number";
        public static final String TYPE = "Type";
        public static final String NAME = "Name";
                  
        public static final UriMatcher uriMatcher;    
        static {    
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);    
            uriMatcher.addURI(CallRejectContentData.AUTHORITY, "list", REJECTS);    
            uriMatcher.addURI(CallRejectContentData.AUTHORITY, "list/#", REJECT);    
        }  
    }  
}  

