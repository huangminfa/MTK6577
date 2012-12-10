package com.mediatek.contacts.dialpad;

import android.content.Context;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;

import java.util.ArrayList;

import android.provider.Telephony.SIMInfo;

public class DialerSearchUtils {
    
    private static final String TAG = "DialerSearchUtils";
    
    private static SIMInfoWrapper mSimInfoWrapper;
    
    public static String tripHyphen(String number) {
        if (TextUtils.isEmpty(number)) 
            return number;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c != '-' && c != ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String tripNonDigit(String number) {
        if (TextUtils.isEmpty(number)) 
            return number;

        StringBuilder sb = new StringBuilder();
        int len = number.length();
        
        for (int i = 0; i < len; i++) {
            char c = number.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static long getSimType(int indicate, int isSdnContact) {
        long photoId = 0;
        if (mSimInfoWrapper == null) {
            mSimInfoWrapper = SIMInfoWrapper.getDefault();
        }

        final int slot = mSimInfoWrapper.getSimSlotById(indicate);

        int i = -1;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.i(TAG, "[getSimType] mSlot = " + slot);
            SIMInfo simInfoForColor = mSimInfoWrapper.getSimInfoBySlot(slot);
            if (simInfoForColor != null) {
                i = simInfoForColor.mColor;
            }
            /*
             * Change Feature by Mediatek Begin.
             *   Original Android's code:
             *     xxx
             *   CR ID: ALPS00269801
             *   Descriptions:
             */
            Log.i(TAG, "[getSimType] i = " + i);
//            if (OperatorUtils.getOptrProperties().equals("OP02")) {
//                if (slot == 0) {
//                    return -3;
//                } else {
//                    return -4;
//                }
//            } else if (i == 0) {
            /*
             * Change Feature by Mediatek End.
             */
            if (isSdnContact > 0) {
                if (i == 0) {
                    photoId = -5;
                } else if (i == 1) {
                    photoId = -6;
                } else if (i == 2) {
                    photoId = -7;
                } else if (i == 3) {
                    photoId = -8;
                } else {
                    photoId = -9;
                }
            } else {
				if (i == 0) {
					photoId = -10;
				} else if (i == 1) {
					photoId = -11;
				} else if (i == 2) {
					photoId = -12;
				} else if (i == 3) {
					photoId = -13;
				} else {
					photoId = -1;
				}
            }
        } else {
        	if (isSdnContact > 0) {                    
        		photoId = -9;                    
	        } else {
                photoId = -1;
	        }
        }
        return photoId;
    }
        
    

    public static ArrayList<Integer> adjustHighlitePositionForHyphen(String number,
            String numberMatchedOffsets, String originNumber) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        try {
            int highliteBegin = (int) numberMatchedOffsets.charAt(0);
            int highliteEnd = (int) numberMatchedOffsets.charAt(1);
            int originNumberBegin = 0;		
            String targetTemp = "";
            for (int i = 0; i < number.length(); i++) {
                char c = number.charAt(i);
                if (c == '-' || c == ' ') {
                    continue;
                }
                targetTemp += c;
            }		
            originNumberBegin = originNumber.indexOf(targetTemp);
			
            if (highliteBegin > highliteEnd)
                return res;
			
            if((originNumberBegin >= highliteEnd) && highliteEnd >= 1){
                highliteEnd = 0;
            }
			
            if(highliteEnd > originNumberBegin){
                highliteEnd = highliteEnd - originNumberBegin;
            }		
			
            if(highliteBegin >= originNumberBegin){
                highliteBegin = highliteBegin - originNumberBegin;
            }	
			
            for (int i = 0; i <= highliteBegin; i++) {
                char c = number.charAt(i);
                if (c == '-' || c == ' ') {
                    highliteBegin++;
                    highliteEnd++;
                }
            }
			
            for (int i = highliteBegin + 1; (i <= highliteEnd && i < number.length()); i++) {
                char c = number.charAt(i);
                if (c == '-' || c == ' ') {
                    highliteEnd++;
                }
            }
			
            if(highliteEnd >= number.length()) 
                highliteEnd = number.length() - 1;	
			
            res.add(highliteBegin);
            res.add(highliteEnd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }
    
    
}
