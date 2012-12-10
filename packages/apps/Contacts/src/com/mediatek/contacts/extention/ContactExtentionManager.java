
package com.mediatek.contacts.extention;

import android.util.Log;
import com.mediatek.contacts.extention.rcs.ContactExtentionForRCS;

public class ContactExtentionManager {

    private static final String TAG = "ContactExtentionManager";
    public static final String RCS_CONTACT_PRESENCE_CHANGED = "android.intent.action.RCS_CONTACT_PRESENCE_CHANGED";
    private static ContactExtentionManager mInstance;
    private ContactExtention mContactExtention;

    private ContactExtentionManager() {
        mContactExtention = createContactExtention();
    }

    public static ContactExtentionManager getInstance() {
        if (mInstance == null) {
            mInstance = new ContactExtentionManager();
        }
        return mInstance;
    }

    public ContactExtention getContactExtention() {
        return mContactExtention;
    }

    private ContactExtention createContactExtention() {

        if (ContactExtentionForRCS.isSupport()) {
            Log.i(TAG, "get ContactExtentionForRCS");
            return new ContactExtentionForRCS();
        } else {
            Log.i(TAG, "get ContactExtention");
            return new ContactExtention();
        }
    }

}
