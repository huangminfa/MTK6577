package com.mediatek.vcard.util;

import android.os.SystemProperties;

public class OperatorUtils {

    private static String getOptrProperties() {
        return SystemProperties.get("ro.operator.optr");
    }
    
    /**
     * check whether "OP03" is enabled.
     * return true if "OP03" enabled, otherwise return false.
     * 
     * @hide
     */
    public static boolean isOp03Enabled() {
        if ("OP03".equals(getOptrProperties())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * check whether "AAS" feature is enabled.
     * The accountType is the contact's account type, it seems like "USIM Account".
     * 
     * @hide
     */
    public static boolean isAasEnabled(String accountType) {
        if(isOp03Enabled() && "USIM Account".equals(accountType)) {
            return true;
        } else {
            return false;
        }
    }
    
}
