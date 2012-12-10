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

package com.mediatek.StkSelection;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ITelephony;

import android.os.ServiceManager;
import com.mediatek.CellConnService.CellConnMgr;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class StkSelection extends Activity {
    /** Called when the activity is first created. */
	
	 public static final String LOGTAG = "StkSelection ";
	 public static boolean bSIM1Inserted = false;
	 public static boolean bSIM2Inserted = false;
	 public static String strTargetLoc = null;
	 public static String strTargetClass = null;
	 
	private static final int REQUEST_TYPE = 302;
	
    public static int mSlot = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "[onCreate]+");
        mCellMgr.register(this);
        setContentView(R.layout.main);
        Log.d(LOGTAG, "[onCreate]-");
        }
    
    
//        setContentView(R.layout.main);

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "[onDestroy]+");
		super.onDestroy();
		mCellMgr.unregister();
		Log.d(LOGTAG, "[onDestroy]-");
	}


	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(LOGTAG, "[onResume]+");
		strTargetLoc = null;
		strTargetClass = null;
		PackageManager pm = getApplicationContext().getPackageManager();
       if(false == FeatureOption.MTK_GEMINI_SUPPORT){//single card 
           ComponentName cName = new ComponentName("com.android.stk",
           "com.android.stk.StkLauncherActivity");
           if((hasIccCard(0) == false)){
        	 //Notify user no cards insert
        	   showTextToast(getString(R.string.activity_not_found));
        	   finish();
        	   return;
			}else if(IccCardReady(0) == false){//SIM card ready
				//call Zhiwei
				mSlot = 0;
				int nRet1 = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
			}else{
				if(pm.getComponentEnabledSetting(cName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ){
					showTextToast(getString(R.string.activity_not_found));
					finish();
					return ;
				}else{
					strTargetLoc = "com.android.stk";
					strTargetClass = "com.android.stk.StkLauncherActivity"; 
				}
			}           		 
       	        	
       }else{//gemini card
    	    bSIM1Inserted = hasIccCard(0);
    	    bSIM2Inserted = hasIccCard(1);
if ( FeatureOption.EVDO_DT_VIA_SUPPORT && (TelephonyManager.getDefault().getPhoneTypeGemini(Phone.GEMINI_SIM_1) == Phone.PHONE_TYPE_CDMA) ){
            if(bSIM2Inserted == true){//Only sim2 inserted
                ComponentName cName2 = new ComponentName("com.android.stk2",
                    "com.android.stk2.StkLauncherActivity");
		        
                if(IccCardReady(1) == false){
                    mSlot = 1;
                    //call Zhiwei
                    int nRet3 = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
                }else if(pm.getComponentEnabledSetting(cName2) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                    showTextToast(getString(R.string.activity_not_found));
                    finish();
                    return;
                }else{
                    strTargetLoc = "com.android.stk2";
                    strTargetClass = "com.android.stk2.StkLauncherActivity"; 
                }        		
            } else {
                showTextToast(getString(R.string.activity_not_found));
                finish();
                return;
            }
} else if (FeatureOption.EVDO_DT_VIA_SUPPORT && (TelephonyManager.getDefault().getPhoneTypeGemini(Phone.GEMINI_SIM_2) == Phone.PHONE_TYPE_CDMA) ) {
            if ((bSIM1Inserted == true) ){//Only sim1 inserted
                ComponentName cName1 = new ComponentName("com.android.stk",
		        "com.android.stk.StkLauncherActivity");
                if(IccCardReady(0) == false){
                    mSlot = 0;
                    //call Zhiwei
                    int nRet2 = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
                }else if(pm.getComponentEnabledSetting(cName1) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                    showTextToast(getString(R.string.activity_not_found));
                    finish();
                    return;
                }else{
                    strTargetLoc = "com.android.stk";
                    strTargetClass = "com.android.stk.StkLauncherActivity"; 
                }
            } else {
                showTextToast(getString(R.string.activity_not_found));
                finish();
                return;
            }
} else {//EVDO_DT_VIA_SUPPORT
			if((bSIM1Inserted == false) && (bSIM2Inserted == false) ){//No SIM card inserted
				//Notify user no cards insert
				showTextToast(getString(R.string.activity_not_found));
				finish();
				return;
			}else if ((bSIM1Inserted == true) && (bSIM2Inserted == false)){//Only sim1 inserted
				ComponentName cName1 = new ComponentName("com.android.stk",
		           "com.android.stk.StkLauncherActivity");
				if(IccCardReady(0) == false){
					mSlot = 0;
					//call Zhiwei
					int nRet2 = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
				}else if(pm.getComponentEnabledSetting(cName1) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
		        	showTextToast(getString(R.string.activity_not_found));
		        	finish();
		        	return;
		        }else{
			   		strTargetLoc = "com.android.stk";
			   		strTargetClass = "com.android.stk.StkLauncherActivity"; 
		        }     		
			}else if((bSIM1Inserted == false) && (bSIM2Inserted == true)){//Only sim2 inserted
		        ComponentName cName2 = new ComponentName("com.android.stk2",
		           "com.android.stk2.StkLauncherActivity");
		        
				if(IccCardReady(1) == false){
					mSlot = 1;
					//call Zhiwei
					int nRet3 = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
				}else if(pm.getComponentEnabledSetting(cName2) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
		        	showTextToast(getString(R.string.activity_not_found));
		        	finish();
		        	return;
		        }else{
			   		strTargetLoc = "com.android.stk2";
			   		strTargetClass = "com.android.stk2.StkLauncherActivity"; 
		        }        		
			}else{//Both SIM inserted
				strTargetLoc = "com.android.phone";
				strTargetClass = "com.android.phone.StkListEntrance";
			}
} //end EVDO_DT_VIA_SUPPORT
       }
       
       if(strTargetLoc != null){
           Intent intent = new Intent();
           intent.setClassName(strTargetLoc, strTargetClass);
           startActivity(intent);
           finish();
       }
       
           finish();
	   Log.d(LOGTAG, "[onResume]-");
	}
	

	//deal with SIM status
	private Runnable serviceComplete = new Runnable() {
		public void run() {
			Log.d(LOGTAG, "serviceComplete run");			
			int nRet = mCellMgr.getResult();
			Log.d(LOGTAG, "serviceComplete result = " + CellConnMgr.resultToString(nRet));
			if (mCellMgr.RESULT_ABORT == nRet) {
				finish();
				return;
			} else {
		        finish();
				return;
			}
		}
	};

	private CellConnMgr mCellMgr = new CellConnMgr(serviceComplete);

	public static boolean hasIccCard(int slot) {

		boolean bRet = false;

		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			try {
				final ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (null != iTelephony) {
					bRet = iTelephony.isSimInsert(slot);
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				final ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (null != iTelephony) {
					bRet = iTelephony.isSimInsert(0);
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		}

		return bRet;
	}
	
   

	public static boolean IccCardReady(int slot) {
	
			boolean bRet = false;
	
			if (true == FeatureOption.MTK_GEMINI_SUPPORT) {					
					bRet = (TelephonyManager.SIM_STATE_READY 
				    		== TelephonyManager.getDefault()
				    		.getSimStateGemini(slot));					
			} else {
					bRet = (TelephonyManager.SIM_STATE_READY 
				    		== TelephonyManager.getDefault()
				    		.getSimState());
			}
	
			return bRet;
		}
	
    private void showTextToast(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}
