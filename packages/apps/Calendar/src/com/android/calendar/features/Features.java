package com.android.calendar.features;

import java.util.ArrayList;
import java.util.Locale;

import com.android.calendar.LogUtil;
import com.mediatek.featureoption.FeatureOption;

/**
 * A class to manage the features and operators.
 * TODO: to Change this class to some small classes, to get a better performance and comprehension. 
 */
public class Features {

    private static final String TAG = Features.class.getSimpleName();

    private static final int FEATURE_SC_LUNAR_CALENDAR = 1;
    private static final int FEATURE_TC_LUNAR_CALENDAR = 2;
    private static final int FEATURE_CLEAR_ALL_EVENTS = 3;
    private static final int FEATURE_THEME_MANAGER = 4;

    private static final String OP = android.os.SystemProperties
            .get("ro.operator.optr");
    private static final boolean IS_OP01 = ("OP01").equals(OP);
    private static final boolean IS_OP02 = ("OP02").equals(OP);
    static {
        LogUtil.i(TAG, "Operator: IS_OP01 = " + IS_OP01 + ", IS_OP02 = " + IS_OP02);
    }

    private static ArrayList<Integer> OP01_FEATURES = new ArrayList<Integer>();
    static {
        OP01_FEATURES.add(FEATURE_SC_LUNAR_CALENDAR);
        OP01_FEATURES.add(FEATURE_CLEAR_ALL_EVENTS);
    }

    private static ArrayList<Integer> OP02_FEATURES = new  ArrayList<Integer>();
    static {
        OP02_FEATURES.add(FEATURE_SC_LUNAR_CALENDAR);
        OP02_FEATURES.add(FEATURE_TC_LUNAR_CALENDAR);
        OP02_FEATURES.add(FEATURE_CLEAR_ALL_EVENTS);
    }

    private static ArrayList<Integer> COMMON_FEATURES = new ArrayList<Integer>();
    static {
        if (FeatureOption.MTK_THEMEMANAGER_APP) {
            COMMON_FEATURES.add(FEATURE_THEME_MANAGER);
        }
    }

    /**
     * is zh-cn lunar enabled to current env
     * @return
     */
    public static boolean isSCLunarCalendarEnabled() {
        return (isFeatureEnabled(FEATURE_SC_LUNAR_CALENDAR)
                && Locale.SIMPLIFIED_CHINESE.equals(Locale.getDefault()));
    }

    /**
     * is zh-tw lunar enabled to current env
     * @return
     */
    public static boolean isTCLunarCalendarEnabled() {
        return (isFeatureEnabled(FEATURE_TC_LUNAR_CALENDAR)
                && Locale.TRADITIONAL_CHINESE.equals(Locale.getDefault()));
    }

    /**
     * is clear all events feature enabled to current env
     * @return
     */
    public static boolean isClearAllEventEnabled() {
        return isFeatureEnabled(FEATURE_CLEAR_ALL_EVENTS);
    }

    /**
     * is current env suitable for calendar to show lunar features
     * typically, OP01 allows zh-cn lunar only, and OP02 needs more.
     * e.g. when the system language is not zh-cn, the lunar should disappear.
     * @return
     */
    public static boolean canLunarCalendarBeShown() {
        return (isSCLunarCalendarEnabled() 
                || isTCLunarCalendarEnabled());
    }

    public static boolean isThemeManagerEnabled() {
        return isFeatureEnabled(FEATURE_THEME_MANAGER);
    }

    /**
     * is the given feature name is needed by current operator
     * @param feature
     * @return
     */
    private static boolean isFeatureEnabled(int feature) {
        if (COMMON_FEATURES.contains(feature)) {
            return true;
        }

        if (IS_OP01) {
            return OP01_FEATURES.contains(feature);
        } else if (IS_OP02) {
            return OP02_FEATURES.contains(feature);
        }
        return false;
    }
}
