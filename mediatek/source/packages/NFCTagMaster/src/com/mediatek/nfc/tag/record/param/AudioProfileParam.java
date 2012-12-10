
package com.mediatek.nfc.tag.record.param;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AudioProfileParam extends SpinnerParamItem {
    private static final String TAG = Utils.TAG + "/AudioProfileParam";

    public static AudioProfileParam sInstance;

    private static Activity sActivity;

    public static AudioProfileParam getInstance(Activity activity) {
        sActivity = activity;
        if (sInstance == null) {
            sInstance = new AudioProfileParam();
        }
        return sInstance;
    }

    public AudioProfileParam() {
        mStatusArray = sActivity.getResources().getStringArray(R.array.param_audio_profile_value);
        mParamPrefix = "audio_profile_key=";
    }

    @Override
    public boolean enableParam(Handler handler, String newStatus) {
        Utils.logv(TAG, "-->enableParam(), newStatus=" + newStatus);
        boolean result = false;

        Utils.logd(TAG, "Try to enable audio profile: " + newStatus);

        // Set audio profile value by reflect method
        try {
            Class audioManagerClass = null;
            if (Utils.ANDROID_VERSION_NUMBER < 4) {
                audioManagerClass = Class
                        .forName("com.mediatek.audioprofile.AudioProfileManagerImpl");
            } else {
                audioManagerClass = Class.forName("com.mediatek.audioprofile.AudioProfileManager");
            }

            // First try to get AudioProfileManager from system service,
            // then call AudioProfileManager.setActiveProfile(String key)
            // This method is new added in ICS
            Object audioManagerObj = sActivity.getSystemService("audioprofile");
            if (audioManagerObj != null) {
                try {
                    Method setProfileMethodWithKey = audioManagerClass.getMethod(
                            "setActiveProfile", String.class);
                    setProfileMethodWithKey.invoke(audioManagerObj, newStatus);
                    return true;
                } catch (NoSuchMethodException e) {
                    Utils.loge(TAG,
                            "For ICS version, fail to get setActiveProfile(String key) method", e);
                } catch (IllegalArgumentException e) {
                    Utils
                            .loge(
                                    TAG,
                                    "For ICS version, fail to call setActiveProfile(String key), wrong argument",
                                    e);
                } catch (IllegalAccessException e) {
                    Utils.loge(TAG,
                            "For ICS version, No permission to call setActiveProfile(String key)",
                            e);
                } catch (InvocationTargetException e) {
                    Utils
                            .loge(
                                    TAG,
                                    "For ICS version, InvocationTargetException to call setActiveProfile(String key)",
                                    e);
                }
            }

            // For old implementation version, try:
            // AudioProfileManager.getInstance(),
            // AudioProfileManager.setActiviteProfile(AudioProfile audioProfile)
            Class audioProfileClass = Class.forName("com.mediatek.audioprofile.AudioProfile");

            Method getProfileMethod = audioManagerClass.getMethod("getProfile", String.class);
            Method setProfileMethod = audioManagerClass.getMethod("setActiveProfile",
                    audioProfileClass);
            Method getManagerInstanceMethod = audioManagerClass.getMethod("getInstance",
                    Context.class);
            Object managerInstance = getManagerInstanceMethod.invoke(null, sActivity);

            if (managerInstance == null) {
                Utils.loge(TAG, "Fail to get AudioProfileManager instance");
                return false;
            }

            Object activeProfileInstance = getProfileMethod.invoke(managerInstance, newStatus);
            if (activeProfileInstance == null) {
                Utils.loge(TAG, "Fail to get current active audio profile instance");
            } else {
                Utils.logv(TAG, "Get current active audio profile instance successfully.");
            }
            setProfileMethod.invoke(managerInstance, activeProfileInstance);
            result = true;
            Utils.logi(TAG, "Set new audio profile value successfully.");
        } catch (ClassNotFoundException e) {
            Utils.loge(TAG, "Fail to load AudioProfile related class", e);
        } catch (NoSuchMethodException e) {
            Utils.loge(TAG, "Fail to get needed audio profile method", e);
        } catch (IllegalArgumentException e) {
            Utils.loge(TAG, "Fail to call needed audio profile method, wrong argument", e);
        } catch (IllegalAccessException e) {
            Utils.loge(TAG, "No permission to call needed audio profile method", e);
        } catch (InvocationTargetException e) {
            Utils.loge(TAG, "InvocationTargetException to call needed audio profile method", e);
        }

        return result;
    }

    @Override
    public String getLabel() {
        return sActivity.getResources().getString(R.string.param_audio_profile_title);
    }

    @Override
    public View initLayoutView() {
        Utils.logv(TAG, "-->initLayoutView()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mLayoutView = inflater.inflate(R.xml.param_item_audio_profile, null);
        if (mLayoutView == null) {
            Utils.loge(TAG, "Fail to get layout view");
        }
        return mLayoutView;
    }

}
