package com.android.providers.contacts;

import com.android.providers.contacts.ContactsDatabaseHelper.DialerSearchLookupType;
import com.android.providers.contacts.ContactsDatabaseHelper.DialerSearchLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.telephony.PhoneNumberUtils;
import android.provider.CallLog.Calls;


public class DialerSearchUtils {
	
    public static String computeNormalizedNumber(String number) {
        String normalizedNumber = null;
        if (number != null) {
            normalizedNumber = PhoneNumberUtils.getStrippedReversed(number);
        }
        return normalizedNumber;
    }
    
	public static String stripSpecialCharInNumberForDialerSearch(String number) {
		if (number == null)
	    	return null;
	    int len = number.length();		
		StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < len; i++) {
	    	char c = number.charAt(i);
			if (PhoneNumberUtils.isNonSeparator(c)) {
				sb.append(c);
			} else if (c == ' ' || c == '-') {
				// strip blank and hyphen
			} else {
				break;
			}
	    }
	    return sb.toString();
	}
	
	/*
	 * Utility function for dialer search database operation
	 */
//	static void  deleteDialerSearchNumByCallLogId(
//			SQLiteStatement sqliteState,
//			SQLiteDatabase db, int latestCallLogId) {
//		if (sqliteState == null) {
//			sqliteState = db.compileStatement(
//        			"DELETE FROM " + Tables.DIALER_SEARCH + 
//        			" WHERE " + DialerSearchLookupColumns.CALL_LOG_ID + " =? " + 
//        			" AND " + DialerSearchLookupColumns.NAME_TYPE + " = " + DialerSearchLookupType.PHONE_EXACT);
//		}
//		sqliteState.bindLong(1, latestCallLogId);
//		sqliteState.execute();
//	}
//
//	static void insertDialerSearchNewRecord(SQLiteStatement sqliteState,
//			SQLiteDatabase db, long rawContactId, long dataId, String number,
//			int nameType, int lastCallLogId) {
//		if (sqliteState == null) {
//			sqliteState = db.compileStatement("INSERT INTO "
//					+ Tables.DIALER_SEARCH + "("
//					+ DialerSearchLookupColumns.RAW_CONTACT_ID + ","
//					+ DialerSearchLookupColumns.DATA_ID + ","
//					+ DialerSearchLookupColumns.NORMALIZED_NAME + ","
//					+ DialerSearchLookupColumns.NAME_TYPE + ","
//					+ DialerSearchLookupColumns.CALL_LOG_ID + ","
//					+ DialerSearchLookupColumns.NORMALIZED_NAME_ALTERNATIVE + ")"
//					+ " VALUES (?,?,?,?,?,?)");
//		}
//		sqliteState.bindLong(1, rawContactId);
//		sqliteState.bindLong(2, dataId);
//		bindString(sqliteState, 3, number);
//		sqliteState.bindLong(4, nameType);
//		sqliteState.bindLong(5, lastCallLogId);
//		bindString(sqliteState, 6, number);
//		sqliteState.executeInsert();
//	}
//	
//	public static int updateCallsInfoForNewInsertNumber(
//			SQLiteStatement stateUpdateCallLog,
//			SQLiteStatement stateLastCallQuery, SQLiteDatabase db,
//			String number, long rawContactId, long dataId) {
//		if (stateUpdateCallLog == null) {
//			stateUpdateCallLog = db.compileStatement(
//        			"UPDATE " + Tables.CALLS + 
//        			" SET " + Calls.DATA_ID + "=?, " + 
//        			Calls.RAW_CONTACT_ID + "=? " + 
//        			" WHERE PHONE_NUMBERS_EQUAL(" + Calls.NUMBER + ", ?) AND " + 
//        			Calls.DATA_ID + " IS NULL ");
//		}
//		if (stateLastCallQuery == null) {
//			stateLastCallQuery = db.compileStatement(
//        			"SELECT " + Calls._ID + " FROM " + Tables.CALLS +
//        			" WHERE " + Calls.DATE + " = (" +
//        			" SELECT MAX( " + Calls.DATE + " ) " +
//        			" FROM " + Tables.CALLS +
//        			" WHERE " + Calls.DATA_ID + " =? )");
//		}
//		stateUpdateCallLog.bindLong(1,dataId);
//		stateUpdateCallLog.bindLong(2,rawContactId);
//        bindString(stateUpdateCallLog,3,number);
//        stateUpdateCallLog.execute();
//        int mCallLogId = 0;
//        try{
//        	stateLastCallQuery.bindLong(1,dataId);
//        	mCallLogId = (int) stateLastCallQuery.simpleQueryForLong();
//        } catch (android.database.sqlite.SQLiteDoneException e) {
//        	return 0;
//        } catch (NullPointerException e){
//        	return 0;
//        }
//
////        Commount out call log notification since ICS call still uses the default one.
////        if (mCallLogId > 0) {
////            notifyCallsChanged();
////        }
//        
//        return mCallLogId;
//    }
//    
//    private static void bindString(SQLiteStatement stmt, int index, String value) {
//        if (value == null) {
//            stmt.bindNull(index);
//        } else {
//            stmt.bindString(index, value);
//        }
//    }
}
