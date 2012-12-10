package com.mediatek.contacts.util;

import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMAas;

import android.content.Context;
import android.util.Log;

/**
 * Additional number util.
 * M:AAS
 */
public class AnrUtil {
    private static final String TAG = "AnrUtil";

    /**
     * Return a {@link CharSequence} that best AAS describes the given type for USim account.
     * It is for AAS feature only.
     */
    public static final CharSequence getAasTypeLabel(int type, int simId) {
        String aasLabel = "";
        int slot = SIMInfoWrapper.getDefault().getSimSlotById(simId);
        aasLabel = USIMAas.getUSIMAASById(slot, type);
        Log.i(TAG, "getAasTypeLabel, aasLabel=" + aasLabel);
        return aasLabel;
    }
    
    /**
     * Return a {@link CharSequence} that best AAS describes the given type for USim account.
     * It is for AAS feature only.
     */
    public static final String getAccountTypeById(int simId) {
        String accountType = "";
        int slot = SIMInfoWrapper.getDefault().getSimSlotById(simId);
        accountType = getAccountTypeBySlot(slot);
        Log.i(TAG, "getAccountTypeLabel, accountType=" + accountType);
        return accountType;
    }
    
    public static String getAccountTypeBySlot(int slotId) {
        Log.i(TAG, "getAccountTypeBySlot()+ - slotId:" + slotId);
        if (slotId < SimCardUtils.SimSlot.SLOT_ID1 || slotId > SimCardUtils.SimSlot.SLOT_ID2) {
            Log.e(TAG, "Error! - slot id error. slotid:" + slotId);
            return null;
        }
        int simtype = SimCardUtils.SimType.SIM_TYPE_SIM;
        String simAccountType = AccountType.ACCOUNT_TYPE_SIM;

        if (SimCardUtils.isSimInserted(slotId)) {
            simtype = SimCardUtils.getSimTypeBySlot(slotId);
            if (SimCardUtils.SimType.SIM_TYPE_USIM == simtype) {
                simAccountType = AccountType.ACCOUNT_TYPE_USIM;
            }
            //UIM
            else if (SimCardUtils.SimType.SIM_TYPE_UIM == simtype) {
                simAccountType = AccountType.ACCOUNT_TYPE_UIM;
            }
            //UIM
        } else {
            Log.e(TAG, "Error! getAccountTypeBySlot - slotId:" + slotId + " no sim inserted!");
            simAccountType = null;
        }
        Log.i(TAG, "getAccountTypeBySlot()- - slotId:" + slotId + " AccountType:" + simAccountType);
        return simAccountType;
    }

}
