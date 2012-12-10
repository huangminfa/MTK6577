package com.android.systemui.statusbar.util;

import java.util.List;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.systemui.R;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "dual SIM" and "Notification toolbar".
 */
public class SIMHelper {

    public static final String TAG = "SIMHelper";

    private static final int SIM_STATUS_COUNT = 9;
    private static final int MOBILE_ICON_COUNT = 4;

    private static List<SIMInfo> mSimInfos;

    private static int[] mSimStatusViews;
    private static int[] mMobileIconResIds;

    private static String mIsOptr = null;
    private static String mBaseband = null;

    private static ITelephony iTelephony;

    private SIMHelper() {
    }

    /**
     * Get the default SIM id of the assigned business.
     * 
     * @param context
     * @param businessType
     * @return The default SIM id, or -1 if it is not defined.
     */
    public static long getDefaultSIM(Context context, String businessType) {
        return Settings.System.getLong(context.getContentResolver(), businessType, -1);
    }

    public static void setDefaultSIM(Context context, String businessType, long simId) {
        Settings.System.putLong(context.getContentResolver(), businessType, simId);
    }

    public static List<SIMInfo> getSIMInfoList(Context context) {
        if (mSimInfos == null || mSimInfos.size() == 0) {
            mSimInfos = getSortedSIMInfoList(context);
        }
        return mSimInfos;
    }

    /**
     * Get the SIM info of the assigned SIM id.
     * 
     * @param context
     * @param simId
     * @return The SIM info, or null if it doesn't exist.
     */
    public static SIMInfo getSIMInfo(Context context, long simId) {
        if (mSimInfos == null || mSimInfos.size() == 0) {
            getSIMInfoList(context);
        }
        for (SIMInfo info : mSimInfos) {
            if (info.mSimId == simId) {
                return info;
            }
        }
        return null;
    }

    /**
     * Get the SIM info of the assigned SLOT id.
     * 
     * @param context
     * @param slotId
     * @return The SIM info, or null if it doesn't exist.
     */
    public static SIMInfo getSIMInfoBySlot(Context context, int slotId) {
        if (mSimInfos == null || mSimInfos.size() == 0) {
            getSIMInfoList(context);
        }
	if (mSimInfos == null) return null;
        for (SIMInfo info : mSimInfos) {
            if (info.mSlot == slotId) {
                return info;
            }
        }
        return null;
    }

    private static List<SIMInfo> getSortedSIMInfoList(Context context) {
    	List<SIMInfo> simInfoList = SIMInfo.getInsertedSIMList(context);
        if(simInfoList != null && simInfoList.size() == 2) {
        	if(simInfoList.get(0).mSlot > simInfoList.get(1).mSlot) {
        		SIMInfo temp1 = simInfoList.get(0);
        		SIMInfo temp2 = simInfoList.get(1);
        		simInfoList.clear();
        		simInfoList.add(temp2);
        		simInfoList.add(temp1);
        	}
        }
        return simInfoList;
    }
    
    public static void updateSIMInfos(Context context) {
        mSimInfos = null;
        mSimInfos = getSortedSIMInfoList(context);
    }

    public static void initStatusIcons() {
        if (mSimStatusViews == null) {
            mSimStatusViews = new int[SIM_STATUS_COUNT];
            mSimStatusViews[Phone.SIM_INDICATOR_RADIOOFF] = com.mediatek.internal.R.drawable.sim_radio_off;
            mSimStatusViews[Phone.SIM_INDICATOR_LOCKED] = com.mediatek.internal.R.drawable.sim_locked;
            mSimStatusViews[Phone.SIM_INDICATOR_INVALID] = com.mediatek.internal.R.drawable.sim_invalid;
            mSimStatusViews[Phone.SIM_INDICATOR_SEARCHING] = com.mediatek.internal.R.drawable.sim_searching;
            mSimStatusViews[Phone.SIM_INDICATOR_ROAMING] = com.mediatek.internal.R.drawable.sim_roaming;
            mSimStatusViews[Phone.SIM_INDICATOR_CONNECTED] = com.mediatek.internal.R.drawable.sim_connected;
            mSimStatusViews[Phone.SIM_INDICATOR_ROAMINGCONNECTED] = com.mediatek.internal.R.drawable.sim_roaming_connected;
        }
    }

    public static void initMobileIcons() {
        if (mMobileIconResIds == null) {
            mMobileIconResIds = new int[MOBILE_ICON_COUNT];
            mMobileIconResIds[0] = R.drawable.zzz_mobile_blue;
            mMobileIconResIds[1] = R.drawable.zzz_mobile_orange;
            mMobileIconResIds[2] = R.drawable.zzz_mobile_green;
            mMobileIconResIds[3] = R.drawable.zzz_mobile_purple;
        }
    }

    public static long getSIMIdBySlot(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return 0;
        }
        return simInfo.mSimId;
    }

    public static int getSIMColorIdBySlot(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return -1;
        }
        return simInfo.mColor;
    }

    public static int getSIMStateIcon(SIMInfo simInfo) {
        return getSIMStateIcon(TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(simInfo.mSlot));
    }

    public static int getSIMStateIcon(int simStatus) {
        if (simStatus <= -1 || simStatus >= SIM_STATUS_COUNT) {
            return -1;
        }
        if (mSimStatusViews == null) {
            initStatusIcons();
        }
        return mSimStatusViews[simStatus];
    }

    public static int getDataConnectionIconIdBySlotId(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return -1;
        }
        if (mMobileIconResIds == null) {
            initMobileIcons();
        }
        if (simInfo.mColor == -1) {
            return -1;
        } else {
            return mMobileIconResIds[simInfo.mColor];
        }
    }

    public static boolean checkSimCardDataConnBySlotId(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return false;
        }
        int simState = TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(simInfo.mSlot);
        if (simState == Phone.SIM_INDICATOR_ROAMING
                || simState == Phone.SIM_INDICATOR_CONNECTED
                || simState == Phone.SIM_INDICATOR_ROAMINGCONNECTED
                || simState == Phone.SIM_INDICATOR_NORMAL) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean is3GSupported() {
        if (mBaseband == null) {
            mBaseband = SystemProperties.get("gsm.baseband.capability");
        }
        if ((mBaseband != null) && (mBaseband.length() != 0)
                && (Integer.parseInt(mBaseband) <= 3)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isCU() {
        if (mIsOptr == null) {
            mIsOptr = SystemProperties.get("ro.operator.optr");
        }
        if (mIsOptr != null && mIsOptr.equals("OP02")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCT() {
        return FeatureOption.EVDO_DT_VIA_SUPPORT;
    }
    /**
     * Return the current 3G slot. If not CU load then 3g icon will not show, If
     * CU load and 3G_SWITCH feature is disabled 3g icon will always be the
     * first slot, If CU load and 3G_SWITCH feature is enabled 3g icon will be
     * the actually one got from telephony.
     */
    public static int get3GSlot() {
        if (!(isCU() || isCT())) {
            return -1;
        } else {
            if (!FeatureOption.MTK_GEMINI_3G_SWITCH) {
                return 0;
            }
            if (iTelephony == null) {
                iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            }
            int sim3GSlot = -1;
            try {
                sim3GSlot = iTelephony.get3GCapabilitySIM();
            } catch (RemoteException e) {
                Xlog.e(TAG, "iTelephony exception");
                return -1;
            }
            return sim3GSlot;
        }
    }

    public static SIMInfo get3GSlotSimInfo(Context context) {
        int slotId = get3GSlot();
        if (slotId > -1) {
            return getSIMInfoBySlot(context, slotId);
        } else {
            return null;
        }
    }
}
