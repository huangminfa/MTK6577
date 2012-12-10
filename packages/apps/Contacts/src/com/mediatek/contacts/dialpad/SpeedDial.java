package com.mediatek.contacts.dialpad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.Toast;

import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.R;

import com.mediatek.contacts.activities.SpeedDialManageActivity;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.util.TelephonyUtils;
import com.mediatek.contacts.list.service.MultiChoiceService;

public class SpeedDial {

    private static final String TAG = "SpeedDial";

    protected Context mContext;

    SharedPreferences mPreferences;

    public SpeedDial(Context context) {
        mContext = context;

        mPreferences = mContext.getSharedPreferences(SpeedDialManageActivity.PREF_NAME, 
                Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    }

    protected String getSpeedDialNumber(int key) {
        return mPreferences.getString(String.valueOf(key), "");
    }

    protected boolean clearSharedPreferences(int key, String number) {
        boolean notReady = false;

        if (MultiChoiceService.isProcessing(MultiChoiceService.TYPE_DELETE)) {
            Log.i(TAG,"delete or copy is processing ");
            Toast.makeText(mContext, R.string.phone_book_busy,
                    Toast.LENGTH_SHORT).show();
            notReady = true;
            return notReady;
        }


        Cursor phoneCursor = mContext.getContentResolver().query(
                Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), 
                new String[]{PhoneLookup._ID}, null, null, null);

        if (phoneCursor == null || phoneCursor.getCount() == 0) {
            log("clear preferences");
            int numOffset = SpeedDialManageActivity.offset(key);
            int simId = mPreferences.getInt(String.valueOf(numOffset), -1);
            if (simId == -1 || TelephonyUtils.isSimReady(simId)) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(String.valueOf(key), "");

                editor.putInt(String.valueOf(numOffset), -1);
                editor.apply();
                notReady = true;
            }else if (!TelephonyUtils.isSimReady(simId)){
                notReady = true;
            }
            if (phoneCursor != null) 
                phoneCursor.close();
        }

        if (phoneCursor != null)
            phoneCursor.close();	
        log("clear preferences canUse" + notReady);		
        return notReady;
    }

    public boolean dial(int key) {
        final String number = getSpeedDialNumber(key);
        log("dial, key = " + key + " number = " + number);
        if(TextUtils.isEmpty(number))
            return false;

        if(!"OP01".equals(OperatorUtils.getOptrProperties())) {
            if(clearSharedPreferences(key, number))
                return false;
        }

        final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel",
                number, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        return true;
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }
}
