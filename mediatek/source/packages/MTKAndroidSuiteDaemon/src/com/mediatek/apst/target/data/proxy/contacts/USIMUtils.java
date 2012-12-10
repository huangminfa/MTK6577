/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.target.data.proxy.contacts;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;

import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.UsimGroup;

import com.mediatek.apst.target.util.Config;
import com.mediatek.apst.target.util.Debugger;

public class USIMUtils {
    
    private static final String TAG ="APST";
    
    public static final String SIMPHONEBOOK_SERVICE = "simphonebook";
    public static final String SIMPHONEBOOK2_SERVICE = "simphonebook2";
    public static final int SINGLE_SLOT = 0;
    public static final int GEMINI_SLOT1 = com.android.internal.telephony.Phone.GEMINI_SIM_1;
    public static final int GEMINI_SLOT2 = com.android.internal.telephony.Phone.GEMINI_SIM_2;
    public static final int SLOT_COUNT = FeatureOption.MTK_GEMINI_SUPPORT?2:1;
    
    private static int[] MAX_USIM_GROUP_NAME_LENGTH = {-1, -1};
    private static int[] MAX_USIM_GROUP_COUNT = {-1, -1};
    
    
 // The following lines are provided and maintained by Mediatek inc.
    // Added Local Account Type
    public static final String ACCOUNT_TYPE_SIM = "SIM Account";
    public static final String ACCOUNT_TYPE_USIM = "USIM Account";
    public static final String ACCOUNT_TYPE_LOCAL_PHONE = "Local Phone Account";

    // Added Local Account Name - For Sim/Usim Only
    public static final String ACCOUNT_NAME_SIM = "SIM" + SimSlot.SLOT_ID1;
    public static final String ACCOUNT_NAME_SIM2 = "SIM" + SimSlot.SLOT_ID2;
    public static final String ACCOUNT_NAME_USIM = "USIM" + SimSlot.SLOT_ID1;
    public static final String ACCOUNT_NAME_USIM2 = "USIM" + SimSlot.SLOT_ID2;
    public static final String ACCOUNT_NAME_LOCAL_PHONE = "Phone";
    
     public static interface SimType {
            public static final String SIM_TYPE_USIM_TAG = "USIM";

            public static final int SIM_TYPE_SIM = 0;
            public static final int SIM_TYPE_USIM = 1;
        }
     
        public static interface SimSlot {
            public static final int SLOT_NONE = -1;
            public static final int SLOT_SINGLE = 0;
            public static final int SLOT_ID1 = com.android.internal.telephony.Phone.GEMINI_SIM_1;
            public static final int SLOT_ID2 = com.android.internal.telephony.Phone.GEMINI_SIM_2;
        }
    
    public static int getSimTypeBySlot(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        int simType = SimType.SIM_TYPE_SIM;
        try {
            if (Config.MTK_GEMINI_SUPPORT) {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                    simType = SimType.SIM_TYPE_USIM;
            } else {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                    simType = SimType.SIM_TYPE_USIM;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Debugger.logI(new Object[] { slotId }, "simType : " + simType);
        return simType;
    }
    
    public static boolean isSimInserted(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isSimInsert = false;
        try {
            if (iTel != null) {
                if (Config.MTK_GEMINI_SUPPORT) {
                    isSimInsert = iTel.isSimInsert(slotId);
                } else {
                    isSimInsert = iTel.isSimInsert(0);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            isSimInsert = false;
        }
        return isSimInsert;
    }
    
    
    // The following lines are provided and maintained by Mediatek inc.
    public static String getAccountTypeBySlot(int slotId) {
       Debugger.logI("getAccountTypeBySlot()+ - slotId:" + slotId);
        if (slotId < SimSlot.SLOT_ID1 || slotId > SimSlot.SLOT_ID2) {
            Debugger.logE("Error! - slot id error. slotid:" + slotId);
            return null;
        }
        int simtype = SimType.SIM_TYPE_SIM;
        String simAccountType = ACCOUNT_TYPE_SIM;

        if (isSimInserted(slotId)) {
            simtype = getSimTypeBySlot(slotId);
            if (SimType.SIM_TYPE_USIM == simtype) {
                simAccountType = ACCOUNT_TYPE_USIM;
            }
        } else {
            Debugger.logE("Error! getAccountTypeBySlot - slotId:" + slotId + " no sim inserted!");
            simAccountType = null;
        }
        Debugger.logI("getAccountTypeBySlot()- - slotId:" + slotId + " AccountType:" + simAccountType);
        return simAccountType;
    }

    public static String getSimAccountNameBySlot(int slotId) {
        String retSimName = null;
        int simType = SimType.SIM_TYPE_SIM;

        Debugger.logI("getSimAccountNameBySlot()+ slotId:" + slotId);
        if (!isSimInserted(slotId)) {
            Debugger.logE("getSimAccountNameBySlot Error! - SIM not inserted!");
            return retSimName;
        }

        simType = getSimTypeBySlot(slotId);
        Debugger.logI("getSimAccountNameBySlot() slotId:" + slotId + " simType(0-SIM/1-USIM):" + simType);

        if (SimType.SIM_TYPE_SIM == simType) {
            retSimName = ACCOUNT_NAME_SIM;
            if (SimSlot.SLOT_ID2 == slotId) {
                retSimName = ACCOUNT_NAME_SIM2;
            }
        } else if (SimType.SIM_TYPE_USIM == simType) {
            retSimName = ACCOUNT_NAME_USIM;
            if (SimSlot.SLOT_ID2 == slotId) {
                retSimName = ACCOUNT_NAME_USIM2;
            }
        } else {
            Debugger.logE("getSimAccountNameBySlot() Error!  get SIM Type error! simType:" + simType);
        }

        Debugger.logI("getSimAccountNameBySlot()- slotId:" + slotId + " SimName:" + retSimName);
        return retSimName;
    }
    
    /*
     * There are some differences with iccprovider 
     */
    public static boolean isSimUsimType(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isUsim = false;
        try {
            if(slotId == 0){
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                    isUsim = true;
            } else if (slotId > 0){
                // this slotId in deamon equals real slotId + 1
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId-1)))
                    isUsim = true;
            } else {
                Debugger.logE("slotId < 0");
            }
        } catch (Exception e) {
            Debugger.logE("catched exception.");
            e.printStackTrace();
        }
        Debugger.logI(new Object[] { slotId }, "isUsim : " + isUsim);
        return isUsim;
    }
    
// ------------------XXXX ----------------XXXX--------------------XXXX---------------
    
    
    
    public static IIccPhoneBook getIIccPhoneBook(int slotId) {
        Log.d(TAG,"[getIIccPhoneBook]slotId:" + slotId);
        String serviceName;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            serviceName = (slotId == GEMINI_SLOT2) ? SIMPHONEBOOK2_SERVICE : SIMPHONEBOOK_SERVICE;
        } else {
            serviceName = SIMPHONEBOOK_SERVICE;
        }
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub
                .asInterface(ServiceManager.getService(serviceName));
        Log.d(TAG,"[getIIccPhoneBook]iIccPhb:" + iIccPhb);
        return iIccPhb;
    }
    
    public static int getUSIMGrpMaxNameLen(int slot) {
        if (slot<0 || slot >1) {
        	Log.d(TAG, "slot:" + slot);
            return -1;
        }
        Log.d(TAG, "[getUSIMGrpMaxNameLen]slot:" + slot 
                + "|maxNameLen:" + MAX_USIM_GROUP_NAME_LENGTH[slot]);
        if (MAX_USIM_GROUP_NAME_LENGTH[slot] < 0) {
            try {
                final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
                if (iIccPhb != null) {
                    MAX_USIM_GROUP_NAME_LENGTH[slot] = iIccPhb.getUSIMGrpMaxNameLen();
                }
            } catch (android.os.RemoteException e) {
                Log.d(TAG, "catched exception.");
                MAX_USIM_GROUP_NAME_LENGTH[slot] = -1;
            }
        }
        Log.d(TAG, "[getUSIMGrpMaxNameLen]end slot:" + slot 
                + "|maxNameLen:" + MAX_USIM_GROUP_NAME_LENGTH[slot]);
        return MAX_USIM_GROUP_NAME_LENGTH[slot];
    }
    
    
    // Framework interface, here should be change in future.
    public static int hasExistGroup(int slotId, String grpName) throws RemoteException {
        int grpId = -1;
        final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
        Log.d(TAG, "grpName:" + grpName + "|iIccPhb:" + iIccPhb);
        if (TextUtils.isEmpty(grpName) || iIccPhb == null) {
            return grpId;
        }
            List<UsimGroup> uList = iIccPhb.getUsimGroups();
            for(UsimGroup ug: uList) {
                String gName = ug.getAlphaTag();
                int gIndex = ug.getRecordIndex();
                if (!TextUtils.isEmpty(gName) && gIndex > 0) {
                    Log.d(TAG,"[hasExistGroup]gName:" + gName + "||gIndex:" + gIndex);
                    if (gName.equals(grpName)) {
                        grpId = gIndex;
                    }
                }
            }
        return grpId;
    }
    
    /**
     * If a USIM group is created, it should indicate which USIM it 
     * creates on.
     *  
     * @param slotId
     * @param name
     * @return
     */
    public static int createUSIMGroup (int slotId, String name) 
        throws RemoteException, USIMGroupException {
        int nameLen = 0;
        try {
            nameLen = name.getBytes("GBK").length;
        } catch (java.io.UnsupportedEncodingException e) {
            nameLen = name.length();
        }
        if (nameLen > getUSIMGrpMaxNameLen(slotId))
            throw new USIMGroupException(
                    USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                    USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
        final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
        int grpId = -1;
        if (iIccPhb != null)
            grpId = iIccPhb.insertUSIMGroup(name);
            Log.i(TAG, "[createUSIMGroup]inserted grpId:" +grpId);
        if (grpId > 0) {
            UsimGroup usimGroup = new UsimGroup(grpId, name);
        } else {
            switch (grpId) {
            case USIMGroupException.USIM_ERROR_GROUP_COUNT: {
                throw new USIMGroupException(
                        USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
                        USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);
            }
            //Name len has been check before new group.
            //However, do protect here just for full logic.
            case USIMGroupException.USIM_ERROR_NAME_LEN: {
                throw new USIMGroupException(
                        USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                        USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
            }
            default:
                break;
            }
        }
        return grpId;
    }
    
    /**
     *   
     * @param slotId
     * @param name
     * @return
     */
    public static int updateUSIMGroup (int slotId,int nGasId, String name) 
        throws RemoteException, USIMGroupException {
        int nameLen = 0;
        try {
            nameLen = name.getBytes("GBK").length;
        } catch (java.io.UnsupportedEncodingException e) {
            nameLen = name.length();
        }
        if (nameLen > getUSIMGrpMaxNameLen(slotId))
            throw new USIMGroupException(
                    USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                    USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
        final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
        int grpId = -1;
        if (iIccPhb != null) {
            grpId = iIccPhb.updateUSIMGroup(nGasId,name);
        }            
      
        return grpId;
    }   
    
    public static int[] syncUSIMGroupDeleteDualSim(String grpName) {
    	
			int[] errFlag = new int[SLOT_COUNT];
			int flag = -2;
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				if ((flag = deleteUSIMGroup(GEMINI_SLOT1, grpName))>0) {
					errFlag[GEMINI_SLOT1] = flag;
				}
				if ((flag = deleteUSIMGroup(GEMINI_SLOT2, grpName))>0) {
					errFlag[GEMINI_SLOT2] = flag;
				}
			} else {
				if ((flag = deleteUSIMGroup(SINGLE_SLOT, grpName))>0) {
					errFlag[SINGLE_SLOT] = flag;
				}
			}
			return errFlag;
	}   
    
    public static int deleteUSIMGroup(int slotId, String name) {
           
        final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
        int errCode = -2;
        try {
           int grpId = hasExistGroup(slotId, name);
            if (grpId > 0) {
                if (iIccPhb.removeUSIMGroupById(grpId)) {
                    //ugrpListArray.removeItem(slotId, grpId);
                    errCode = 0;
                } else
                    errCode = -1;
            }
        } catch (android.os.RemoteException e) {
            Log.d(TAG, "catched exception");
        }
        return errCode;
        
        
    }
    
    public static boolean addUSIMGroupMember(int slotId, int simIndex, int grpId) {
        boolean succFlag = false;
        try {
            if (grpId > 0) {
                final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                if (iIccPhb != null) {
                    succFlag = iIccPhb.addContactToGroup(simIndex, grpId);
                    succFlag = true;//Only for test, should be removed after framework is ready.
                }
            }
        } catch (android.os.RemoteException e) {
            Log.d(TAG, "catched exception");
            succFlag = false;
        }
        Log.d(TAG, "[addUSIMGroupMember]succFlag" + succFlag);
        return succFlag;
    }

    public static boolean deleteUSIMGroupMember(int slotId, int simIndex, int grpId) {
 		    Log.i(TAG, slotId+"-----deleteUSIMGroupMember[slotId]");
 		    Log.i(TAG, simIndex+"-----deleteUSIMGroupMember[simIndex]");
 		    Log.i(TAG, grpId+"-----deleteUSIMGroupMember[grpId]");
 			boolean succFlag = false;
 			try {
 				if (grpId > 0) {
 					final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
 					if (iIccPhb != null) {
 						succFlag = iIccPhb.removeContactFromGroup(simIndex, grpId);
 						succFlag = true;
 					}
 				}
 			} catch (android.os.RemoteException e) {
 				Log.d(TAG, "catched exception.");
 				succFlag = false;
 			}
 			return succFlag;
 		}    
    
    public static class USIMGroupException extends Exception {

        private static final long serialVersionUID = 1L;
        
        public static final String ERROR_STR_GRP_NAME_OUTOFBOUND = "Group name out of bound";
        public static final String ERROR_STR_GRP_COUNT_OUTOFBOUND = "Group count out of bound";
        public static final int GROUP_NAME_OUT_OF_BOUND = 1;
        public static final int GROUP_NUMBER_OUT_OF_BOUND = 2;
        //Exception type definination in framework. 
        public static final int USIM_ERROR_NAME_LEN = UsimPhoneBookManager.USIM_ERROR_NAME_LEN;
        public static final int USIM_ERROR_GROUP_COUNT = UsimPhoneBookManager.USIM_ERROR_GROUP_COUNT;
        
        int mErrorType;
        int mSlotId;
        
        USIMGroupException() {
            super();
        }

        USIMGroupException(String msg) {
            super(msg);
        }
        
        USIMGroupException(String msg, int errorType, int slotId) {
            super(msg);
            mErrorType = errorType;
            mSlotId = slotId;
        }
        
        public int getErrorType() {
            return mErrorType;
        }
        
        public int getErrorSlotId() {
            return mSlotId;
        }

        @Override
        public String getMessage() {
            return "Details message: errorType:" + mErrorType + "\n"
                    + super.getMessage();
        }
    }

}
