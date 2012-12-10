package com.mediatek.contacts.util;

import com.android.contacts.model.AccountType;

import android.os.SystemProperties;

public class OperatorUtils {

    public static String mOptr = null;
    public static String mSpec = null;
    public static String mSeg = null;

    public static String getOptrProperties() {
        if (null == mOptr) {
            mOptr = SystemProperties.get("ro.operator.optr");
            if (null == mOptr) {
                mOptr = "";
            }
        }
        return mOptr;   
    }

    public static String getSpecProperties() {
        if (null == mSpec) {
            mSpec = SystemProperties.get("ro.operator.spec");
            if (null == mSpec) {
                mSpec = "";
            }
        }
        return mSpec;
    }

    public static String getSegProperties() {
        if (null == mSeg) {
            mSeg = SystemProperties.get("ro.operator.seg");
            if (null == mSeg) {
                mSeg = "";
            }
        }
        return mSeg;
    }
    
    /**
     * check whether "OP03" is enabled.
     * @return return true if "OP03" enabled, otherwise return false.
     */
    public static boolean isOp03Enabled() {
        if ("OP03".equals(getOptrProperties())) {
            return true;
        }
        return false;
    }

    /**
     * check whether "AAS" feature is enabled.
     * @param accountType the contact's account type, it seems like "USIM Account".
     * @return
     */
    public static boolean isAasEnabled(String accountType) {
        if(isOp03Enabled() && AccountType.ACCOUNT_TYPE_USIM.equals(accountType)) {
            return true;
        }
        return false;
    }
    
}
