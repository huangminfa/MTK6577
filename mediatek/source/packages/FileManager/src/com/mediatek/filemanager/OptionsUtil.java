package com.mediatek.filemanager;

import com.mediatek.featureoption.FeatureOption;

import android.os.SystemProperties;


public class OptionsUtil {

    public static boolean isOp02Enabled() {
        FileManagerLog.d("SystemProperties", "ro.operator.optr="+SystemProperties.get("ro.operator.optr"));
        return "OP02".equals(SystemProperties.get("ro.operator.optr"));
    }

    public static boolean isDrmSupported() {
        return FeatureOption.MTK_DRM_APP;
    }
}
