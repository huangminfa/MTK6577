
package com.mediatek.contacts.extention;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface IContactExtention {

    /**
     * Interface for handling action changed event.
     */
    interface OnPresenceChangedListener {
        void onPresenceChanged(long contactId, int presence);
    }

    public void addOnPresenceChangedListener(OnPresenceChangedListener listener, long contactId);

    /**
     * Actions for one contact. One contact maybe contains more than one action.
     * <p>
     * Type: NUMBER
     * </p>
     */
    static public final class Action {
        public Intent intentAction;

        public Drawable icon;
    }

    /**
     * Check this function enable or not.
     * 
     * @return boolean
     */
    boolean isEnabled();

    /**
     * Get the application icon for set icon width and hight.
     * 
     * @return the application launch icon
     */
    Drawable getAppIcon();

    /**
     * Get the application title.
     * 
     * @return the application title 
     */
    String getAppTitle();

    /**
     * Get one contact's actions for some numbers.
     * 
     * @param 
     * @return actions list
     */
    Action[] getContactActions();

    /**
     * Get mimeTpye which write in Data table. @ * @return mimeType
     */
    String getMimeType();

    /**
     * Get contact's presence by one contact id.
     * 
     * @param contactId
     * @return presence icon
     */
    Drawable getContactPresence(long contactId);

    /**
     * give contactLookupUri to RCS-e.
     * 
     * @param contactLookupUri
     * 
     */
    void onContactDetailOpen(Uri contactLookupUri);
    
    
}
