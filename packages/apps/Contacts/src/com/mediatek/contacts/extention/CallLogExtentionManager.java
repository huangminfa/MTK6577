
package com.mediatek.contacts.extention;

import android.util.Log;
import com.mediatek.contacts.extention.rcs.CallLogExtentionForRCS;

public class CallLogExtentionManager {

    private static final String TAG = "CallLogExtentionManager";
    private static CallLogExtentionManager mInstance;
    private CallLogExtention mCallLogExtention;

    private CallLogExtentionManager() {
        mCallLogExtention = createCallLogExtention();
    }

    public static CallLogExtentionManager getInstance() {
        if (mInstance == null) {
            mInstance = new CallLogExtentionManager();
        }
        return mInstance;
    }

    public CallLogExtention getCallLogExtention() {
        return mCallLogExtention;
    }

    private CallLogExtention createCallLogExtention() {

        if (CallLogExtentionForRCS.isSupport()) {
            Log.i(TAG, "get CallLogExtentionForRCS");
            return new CallLogExtentionForRCS();
        } else {
            Log.i(TAG, "get CallLogExtention");
            return new CallLogExtention();
        }
    }

}
