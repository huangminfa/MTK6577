package com.mediatek.contacts.util;

import com.android.contacts.ContactsApplication;

import android.content.Context;
import android.provider.Settings;

public class ContactsSettingsUtils {

    private final static String TAG = "ContactsSettingsUtils";

    public static final long DEFAULT_SIM_SETTING_ALWAYS_ASK = Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK;
    public static final long VOICE_CALL_SIM_SETTING_INTERNET = Settings.System.VOICE_CALL_SIM_SETTING_INTERNET;
    public static final long DEFAULT_SIM_NOT_SET = Settings.System.DEFAULT_SIM_NOT_SET;
    

    private static ContactsSettingsUtils sMe;

    protected Context mContext;

    private ContactsSettingsUtils(Context context) {
        mContext = context;
    }

    public static ContactsSettingsUtils getInstance() {
        if(sMe == null)
            sMe = new ContactsSettingsUtils(ContactsApplication.getInstance());
        return sMe;
    }

    public static long getDefaultSIMForVoiceCall() {
        return DEFAULT_SIM_SETTING_ALWAYS_ASK;
    }

    public static long getDefaultSIMForVideoCall() {
        return 0;
    }

    protected void registerSettingsObserver() {
        //
    }
}
