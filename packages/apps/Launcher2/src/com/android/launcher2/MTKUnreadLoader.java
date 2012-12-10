
package com.android.launcher2;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.android.launcher.R;
import com.android.internal.util.XmlUtils;

class UnreadSupportShortcut {
    public UnreadSupportShortcut(String pkgName, String clsName, String keyString, int type) {
        component = new ComponentName(pkgName, clsName);
        key = keyString;
        shortcutType = type;
        unreadNum = 0;
    }

    ComponentName component;
    String key;
    int shortcutType;
    int unreadNum;

    @Override
    public String toString() {
        return "{UnreadSupportShortcut[" + component + "], key = " + key + ",type = "
                + shortcutType + ",unreadNum = " + unreadNum + "}";
    }
}

/**
 * This class is a util class, implemented to do the following two things,:
 * 
 * 1.Read config xml to get the shortcuts which support displaying unread number,
 * then get the initial value of the unread number of each component and update
 * shortcuts and folders through callbacks implemented in Launcher. 
 * 
 * 2. Receive unread broadcast sent by application, update shortcuts and folders in
 * workspace, hot seat and update application icons in app customize paged view.
 */
public class MTKUnreadLoader extends BroadcastReceiver {
    private static final String TAG = "MTKUnreadLoader";
    private static final String TAG_UNREADSHORTCUTS = "unreadshortcuts";

    private static final SpannableStringBuilder sExceedString = new SpannableStringBuilder("99+");
    private static final ArrayList<UnreadSupportShortcut> sUnreadSupportShortcuts = new ArrayList<UnreadSupportShortcut>();

    private static int sUnreadSupportShortcutsNum = 0;
    private static final Object mLogLock = new Object();

    private Context mContext;

    private WeakReference<UnreadCallbacks> mCallbacks;

    public MTKUnreadLoader(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.MTK_ACTION_UNREAD_CHANGED.equals(action)) {
            final ComponentName componentName = (ComponentName) intent
                    .getExtra(Intent.MTK_EXTRA_UNREAD_COMPONENT);
            final int unreadNum = intent.getIntExtra(Intent.MTK_EXTRA_UNREAD_NUMBER, -1);
            if (LauncherLog.DEBUG_UNREAD) {
                LauncherLog.d(TAG, "Receive unread broadcast: componentName = " + componentName
                        + ",unreadNum = " + unreadNum + ",mCallbacks = " + mCallbacks
                        + getUnreadSupportShortcutInfo());
            }

            if (mCallbacks != null && componentName != null && unreadNum != -1) {
                final int index = supportUnreadFeature(componentName);
                if (index >= 0) {
                    setUnreadNumberAt(index, unreadNum);
                    final UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindComponentUnreadChanged(componentName, unreadNum);
                    }
                }
            }
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(UnreadCallbacks callbacks) {
        mCallbacks = new WeakReference<UnreadCallbacks>(callbacks);
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "initialize: callbacks = " + callbacks + ",mCallbacks = " + mCallbacks);
        }
    }

    /**
     * Load and initialize unread shortcuts.
     * 
     * @param context
     */
    void loadAndInitUnreadShortcuts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                loadUnreadSupportShortcuts();
                initUnreadNumberFromSystem();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mCallbacks != null) {
                    UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindUnreadInfoIfNeeded();
                    }
                }
            }
        }.execute();
    }

    /**
     * Initialize unread number by querying system settings provider.
     * 
     * @param context
     */
    private void initUnreadNumberFromSystem() {
        final ContentResolver cr = mContext.getContentResolver();
        for (int i = 0; i < sUnreadSupportShortcutsNum; i++) {
            final UnreadSupportShortcut shortcut = sUnreadSupportShortcuts.get(i);
            try {
                shortcut.unreadNum = android.provider.Settings.System.getInt(cr, shortcut.key);
                LauncherLog.d(TAG, "initUnreadNumberFromSystem: key = " + shortcut.key
                        + ",unreadNum = " + shortcut.unreadNum);
            } catch (android.provider.Settings.SettingNotFoundException e) {
                LauncherLog.e(TAG, "initUnreadNumberFromSystem SettingNotFoundException key = "
                        + shortcut.key + ",e = " + e.getMessage());
            }
        }
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "initUnreadNumberFromSystem end:" + getUnreadSupportShortcutInfo());
        }
    }

    private void loadUnreadSupportShortcuts() {
        long start = System.currentTimeMillis();
        if (LauncherLog.DEBUG_PERFORMANCE) {
            LauncherLog.d(TAG, "loadUnreadSupportShortcuts begin: start = " + start);
        }

        try {
            XmlResourceParser parser = mContext.getResources().getXml(
                    R.xml.unread_support_shortcuts);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_UNREADSHORTCUTS);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.UnreadShortcut);
                synchronized (mLogLock) {
                    sUnreadSupportShortcuts.add(new UnreadSupportShortcut(a
                            .getString(R.styleable.UnreadShortcut_unreadPackageName), a
                            .getString(R.styleable.UnreadShortcut_unreadClassName), a
                            .getString(R.styleable.UnreadShortcut_unreadKey), a.getInt(
                            R.styleable.UnreadShortcut_unreadType, 0)));
                }   
                a.recycle();

            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got XmlPullParserException while parsing unread shortcuts.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got IOException while parsing unread shortcuts.", e);
        } catch (RuntimeException e) {
            Log.w(TAG, "Got RuntimeException while parsing unread shortcuts.", e);
        }
        sUnreadSupportShortcutsNum = sUnreadSupportShortcuts.size();
        if (LauncherLog.DEBUG_PERFORMANCE) {
            LauncherLog.d(TAG, "loadUnreadSupportShortcuts end: time used = "
                    + (System.currentTimeMillis() - start) + ",sUnreadSupportShortcuts = "
                    + sUnreadSupportShortcuts + getUnreadSupportShortcutInfo());
        }
    }
    
    /**
     * Get unread support shortcut information, since the information are stored
     * in an array list, we may query it and modify it at the same time, a lock
     * is needed.
     * 
     * @return
     */
    private static String getUnreadSupportShortcutInfo() {
        String info = " Unread support shortcuts are ";
        synchronized (mLogLock) {
            info += sUnreadSupportShortcuts.toString();
        }
        return info;
    }

    /**
     * Whether the given component support unread feature.
     * 
     * @param component
     * @return
     */
    static int supportUnreadFeature(ComponentName component) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "supportUnreadFeature: component = " + component);
        }
        if (component == null) {
            return -1;
        }

        for (int i = 0, sz = sUnreadSupportShortcuts.size(); i < sz; i++) {
            if (sUnreadSupportShortcuts.get(i).component.equals(component)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Set the unread number of the item in the list with the given unread number.
     * 
     * @param index
     * @param unreadNum
     */
    synchronized static void setUnreadNumberAt(int index, int unreadNum) {
        if (index >= 0 || index < sUnreadSupportShortcutsNum) {
            if (LauncherLog.DEBUG_UNREAD) {
                LauncherLog.d(TAG, "setUnreadNumberAt: index = " + index + ",unreadNum = "
                        + unreadNum + getUnreadSupportShortcutInfo());
            }
            sUnreadSupportShortcuts.get(index).unreadNum = unreadNum;
        }
    }

    /**
     * Get unread number of application at the given position in the supported
     * shortcut list.
     * 
     * @param index
     * @return
     */
    synchronized static int getUnreadNumberAt(int index) {
        if (index < 0 || index >= sUnreadSupportShortcutsNum) {
            return 0;
        }
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "getUnreadNumberAt: index = " + index
                    + getUnreadSupportShortcutInfo());
        }
        return sUnreadSupportShortcuts.get(index).unreadNum;
    }

    /**
     * Get unread number for the given component.
     * 
     * @param component
     * @return
     */
    static int getUnreadNumberOfComponent(ComponentName component) {
        final int index = supportUnreadFeature(component);
        return getUnreadNumberAt(index);
    }

    /**
     * Get the exceed text.
     * 
     * @return a Spannable string with text "99+".
     */
    static CharSequence getExceedText() {
        return sExceedString;
    }

    /**
     * Generate a text contains specified span to display the unread information
     * when the value is more than 99, do not use toString to convert it to
     * string, that may cause the span invalid.
     */
    static {
        sExceedString.setSpan(new SuperscriptSpan(), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sExceedString.setSpan(new AbsoluteSizeSpan(10), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public interface UnreadCallbacks {
        /**
         * Bind shortcuts and application icons with the given component, and
         * update folders unread which contains the given component.
         * 
         * @param component
         * @param unreadNum
         */
        public void bindComponentUnreadChanged(ComponentName component, int unreadNum);

        /**
         * Bind unread shortcut information if needed, this call back is used to
         * update shortcuts and folders when launcher first created.
         */
        public void bindUnreadInfoIfNeeded();
    }
}
