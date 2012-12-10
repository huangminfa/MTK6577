
package com.mediatek.contacts.extention;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface ICallLogExtention {

    /**
     * Interface for handling action changed event.
     */
    interface OnPresenceChangedListener {
        void onPresenceChanged(String number, int presence);
    }

    public void addOnPresenceChangedListener(OnPresenceChangedListener listener, String number);

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
     * Get one contact's actions for some numbers.
     * 
     * @param number
     * @return actions list
     */
    Action[] getContactActions(String number);

    /**
     * Get one string for chat action.
     * 
     * @param
     * @return chat string
     */
    String getChatString();

    /**
     * Get contact's presence by one contact id.
     * 
     * @param number
     * @return presence icon
     */
    Drawable getContactPresence(String number);
}
