/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import com.android.systemui.R;

import com.android.systemui.statusbar.policy.NetworkControllerGemini.DataNetType;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkControllerGemini;
import com.android.systemui.statusbar.policy.TelephonyIconsGemini;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import com.mediatek.telephony.TelephonyManagerEx;

// [SystemUI] Support dual SIM.
// Intimately tied to the design of res/layout/zzz_signal_cluster_view_gemini.xml
public class SignalClusterViewGemini extends LinearLayout implements NetworkControllerGemini.SignalCluster {
    private static final String TAG = "SignalClusterViewGemini";

    private static final boolean IS_CU = SIMHelper.isCU() || SIMHelper.isCT();
    private static final boolean IS_CT = SIMHelper.isCT();//support CT
    static final boolean DEBUG = false;
    
    NetworkControllerGemini mNC;

    private boolean mRoaming = false, mRoamingGemini = false;
    private boolean mIsAirplaneMode = false;

    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0, mWifiActivityId = 0;
    private String mWifiDescription;

    private boolean mMobileVisible = false;
    private int mMobileStrengthId[] = {0,0}, mMobileActivityId = 0, mMobileTypeId = 0;
    private String mMobileDescription, mMobileTypeDescription;
    private boolean mMobileVisibleGemini = false;
    private int mMobileStrengthIdGemini[] = {0,0}, mMobileActivityIdGemini = 0, mMobileTypeIdGemini = 0;
    private String mMobileDescriptionGemini, mMobileTypeDescriptionGemini;

    ViewGroup mWifiGroup;
    ImageView mWifi, mWifiActivity;
    
    ViewGroup mSignalNetworkTypeCombo;
    ImageView mSignalNetworkType;
    ViewGroup mSignalNetworkTypeComboGemini;
    ImageView mSignalNetworkTypeGemini;
    
    ViewGroup mMobileGroup;
    ImageView mMobile, mMobile2, mMobileActivity, mMobileType;//add signal2 for CT
    View mSpacer;

    ViewGroup mMobileGroupGemini;
    ImageView mMobileGemini, mMobileGemini2, mMobileActivityGemini, mMobileTypeGemini;//add signalGemini2 for CT
    View mSpacerGemini;
    ImageView mMobileTypeCU;
    ImageView mMobileTypeCUGemini;
    View mNullSignalSpacer,mNullSignalSpacerGemini;
    int mSIMBackground = -1, mSIMBackgroundGemini = -1;
    
    int mSIMColorId = -1, mSIMColorIdGemini = -1;

    boolean mDataConnected = false, mDataConnectedGemini = false;
    boolean mIsDataGeminiIcon = false;
	ViewGroup mDataConnectionGroup;

    DataNetType mDataNetType = null, mDataNetTypeGemini = null;
    //boolean mHasService = false, mHasServiceGemini = false;
    boolean mSIMCUSign = false, mSIMCUSignGemini = false;
    ImageView mMobileNetType, mMobileNetTypeGemini;

    public SignalClusterViewGemini(Context context) {
        this(context, null);
    }

    public SignalClusterViewGemini(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterViewGemini(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNetworkControllerGemini(NetworkControllerGemini nc) {
        if (DEBUG) Xlog.d(TAG, "NetworkControllerGemini=" + nc);
        mNC = nc;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWifiGroup                  = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi                       = (ImageView) findViewById(R.id.wifi_signal);
        mWifiActivity               = (ImageView) findViewById(R.id.wifi_inout);
        mMobileGroup                = (ViewGroup) findViewById(R.id.mobile_combo);
        mMobile                     = (ImageView) findViewById(R.id.mobile_signal);
        mMobileActivity             = (ImageView) findViewById(R.id.mobile_inout);
        mMobileType                 = (ImageView) findViewById(R.id.mobile_type);
        mMobileGroupGemini          = (ViewGroup) findViewById(R.id.mobile_combo_gemini);
        mMobileGemini               = (ImageView) findViewById(R.id.mobile_signal_gemini);
        
        mMobileActivityGemini   = (ImageView) findViewById(R.id.mobile_inout_gemini);
        mMobileTypeGemini       = (ImageView) findViewById(R.id.mobile_type_gemini);
        mSpacer                 =             findViewById(R.id.spacer);
        mSpacerGemini           =             findViewById(R.id.spacer_gemini);
        if (IS_CU) {
            mMobileTypeCU   = (ImageView) findViewById(R.id.mobile_type_cu);
            mMobileTypeCUGemini       = (ImageView) findViewById(R.id.mobile_type_cu_gemini);
            mSignalNetworkTypeCombo       = (ViewGroup) findViewById(R.id.network_type_combo);
            mSignalNetworkType            = (ImageView) findViewById(R.id.network_type);
            mSignalNetworkTypeComboGemini = (ViewGroup) findViewById(R.id.network_type_combo_gemini);
            mSignalNetworkTypeGemini      = (ImageView) findViewById(R.id.network_type_gemini);
            mNullSignalSpacer                 =             findViewById(R.id.nullSignalSpacer);
            mNullSignalSpacerGemini           =             findViewById(R.id.nullSignalSpacerGemini);
            if(IS_CT) {
            	mMobile2                     = (ImageView) findViewById(R.id.mobile_signal2);
            	mMobileGemini2               = (ImageView) findViewById(R.id.mobile_signal_gemini2);
            	mMobileTypeCU.setVisibility(View.GONE);
            	mMobileTypeCUGemini.setVisibility(View.GONE);
            }
		}
        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup                  = null;
        mWifi                       = null;
        mWifiActivity               = null;
        mMobileGroup                = null;
        mMobile                     = null;
        mMobileActivity             = null;
        mMobileType                 = null;
        mMobileGroupGemini          = null;
        mMobileGemini               = null;
        
        mMobileActivityGemini   = null;
        mMobileTypeGemini       = null;
        mSpacer                 = null;
        mSpacerGemini           = null;
        if (IS_CU) {

        	mNullSignalSpacer       = null;
        	mNullSignalSpacerGemini = null;
            mDataConnectionGroup    = null;
            mMobileNetType          = null;
            mMobileNetTypeGemini    = null;
            if(IS_CT) {
            	mMobile2                     = null;
            	mMobileGemini2               = null;
            }
        }

        super.onDetachedFromWindow();
    }

    public void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
            String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiActivityId = activityIcon;
        mWifiDescription = contentDescription;

    }

    public void setMobileDataIndicators(int slotId, boolean visible, int []strengthIcon, int activityIcon,
            int typeIcon, String contentDescription, String typeContentDescription) {
        Xlog.d(TAG, "setMobileDataIndicators(" + slotId + "), visible=" + visible + ", strengthIcon[0] ~ [1] " + strengthIcon[0]+" ~ "+strengthIcon[1]);
        if (slotId == Phone.GEMINI_SIM_1) {
            mMobileVisible = visible;
            mMobileStrengthId = strengthIcon;
            mMobileActivityId = activityIcon;
            mMobileTypeId = typeIcon;
            mMobileDescription = contentDescription;
            mMobileTypeDescription = typeContentDescription;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	mIsDataGeminiIcon = false;
            }
            
        } else {
            mMobileVisibleGemini = visible;
            mMobileStrengthIdGemini = strengthIcon;
            mMobileActivityIdGemini = activityIcon;
            mMobileTypeIdGemini = typeIcon;
            mMobileDescriptionGemini = contentDescription;
            mMobileTypeDescriptionGemini = typeContentDescription;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	mIsDataGeminiIcon = true;
            }
        }

    }

    public void setIsAirplaneMode(boolean is) {
        mIsAirplaneMode = is;
    }

    public void setRoamingFlag(boolean roaming, boolean roamingGemini) {
    	mRoaming = roaming;
    	mRoamingGemini = roamingGemini;
    }
    public void setSIMBackground(int slotId, int resId) {
        Xlog.d(TAG, "setSIMBackground(" + slotId + "), resId=" + resId);

        if (slotId == Phone.GEMINI_SIM_1) {
            mSIMBackground = resId;
        } else {
            mSIMBackgroundGemini = resId;
        }

    }
    
    public void setSIMState(int slotId, boolean isSIMCUSignVisible) {
        Xlog.d(TAG, "setSIMState(" + slotId + "), isSIMCUSignVisible= " + isSIMCUSignVisible);

        if (slotId == Phone.GEMINI_SIM_1) {
        	mSIMCUSign = isSIMCUSignVisible;
        } else {
        	mSIMCUSignGemini = isSIMCUSignVisible;
        }
        
        //apply();
    }
    

    public void setDataConnected(int slotId, boolean dataConnected) {
        Xlog.d(TAG, "setDataConnected(" + slotId + "), dataConnected=" + dataConnected);

        if (slotId == Phone.GEMINI_SIM_1) {
            mDataConnected = dataConnected;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	//
                mIsDataGeminiIcon = false;                
            } else {
                if (mDataConnected) {
                    mDataConnectedGemini = false;
                }
            }

        } else {
            mDataConnectedGemini = dataConnected;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	//
                mIsDataGeminiIcon = true;                
            } else {
                if (mDataConnectedGemini) {
                    mDataConnected = false;
                }
            }
        }

    }

    public void setDataNetType3G(int slotId, DataNetType dataNetType) {
        Xlog.d(TAG, "setDataNetType3G(" + slotId + "), dataNetType=" + dataNetType);

        if (slotId == Phone.GEMINI_SIM_1) {
            mDataNetType = dataNetType;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	mIsDataGeminiIcon = false;
            }
        } else {
            mDataNetTypeGemini = dataNetType;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	mIsDataGeminiIcon = true;
            }
        }

    }

    // Run after each indicator change.
    public void apply() {
        if (mWifiGroup == null) return;

        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            mWifi.setImageResource(mWifiStrengthId);
            mWifiActivity.setImageResource(mWifiActivityId);
            mWifiGroup.setContentDescription(mWifiDescription);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Xlog.d(TAG,
                String.format("wifi: %s sig=%d act=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId, mWifiActivityId));

        if (mMobileVisible) {
            mMobileGroup.setVisibility(View.VISIBLE);
            mMobile.setImageResource(mMobileStrengthId[0]);
            if(IS_CT) {
            	mMobile2.setImageResource(mMobileStrengthId[1]);
            }
            
            Xlog.d(TAG, "apply, mMobileVisible=" + mMobileVisible 
            		+" mMobileActivityId=" + mMobileActivityId
            		+" mMobileTypeId=" + mMobileTypeId+" mMobileStrengthId[0] = " +
    				""+mMobileStrengthId[0]+" mMobileStrengthId[1] = "+mMobileStrengthId[1]);
            
            mMobileActivity.setImageResource(mMobileActivityId);
            mMobileType.setImageResource(mMobileTypeId);
            
            if (IS_CU) {
            	Xlog.d("SIMCUSign", "apply(GEMINI_SIM_1) mSIMCUSign= "+mSIMCUSign);
            	if(mSIMCUSign ){
            		mMobileTypeCU.setVisibility(View.VISIBLE);
            		mNullSignalSpacer.setVisibility(View.GONE);
            		mSpacerGemini.setVisibility(View.INVISIBLE);
            	} else {
            		mMobileTypeCU.setVisibility(View.GONE);
            		mNullSignalSpacer.setVisibility(View.INVISIBLE);   
            		mSpacerGemini.setVisibility(View.GONE);
            	}
               // if (isSimInserted(Phone.GEMINI_SIM_1) && Phone.SIM_INDICATOR_RADIOOFF != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_1)) {
                if (isSimInserted(Phone.GEMINI_SIM_1) && Phone.SIM_INDICATOR_LOCKED != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_1) 
                									  && Phone.SIM_INDICATOR_SEARCHING != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_1) 
                									  && Phone.SIM_INDICATOR_INVALID != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_1) 
                									  && Phone.SIM_INDICATOR_RADIOOFF != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_1)) {
                	int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, Phone.GEMINI_SIM_1);
                	if (simColorId > -1 && simColorId < 4 && mDataNetType != null) {
	                    int resId ;
	                    switch(mDataNetType){
	                    case _G:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_G[simColorId];
	                    	break;
	                    case _3G:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_3G[simColorId];
	                    	break;
	                    case _1X:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_1X[simColorId];
	                    	break;
	                    case _1X_3G:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_1X_3G[simColorId];
	                    	break;
	                    default:
	                    	resId = 0;
	                    }
	                    Xlog.d(TAG, "mDataNetType ="+mDataNetType+" resId= "+resId+" simColorId = "+simColorId);
	                    mSignalNetworkType.setImageResource(resId);
	                    mSignalNetworkType.setVisibility(View.VISIBLE);
	                    if(mMobileStrengthId[0] == R.drawable.zzz_stat_sys_signal_null_sim_ct || mMobileStrengthId[0] == 0){
	                    	mSignalNetworkType.setVisibility(View.GONE);
	                    }
                	}
//                    ViewGroup.LayoutParams params = mSignalNetworkTypeCombo.getLayoutParams();
//                    params.width = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_signal_icon_expanded_outer_cu_width);
//                    mSignalNetworkTypeCombo.setLayoutParams(params);
                } else {
                	if (!isSimInserted(Phone.GEMINI_SIM_1)){
                		Xlog.d("SIMCUSign", "apply(GEMINI_SIM_2) mSIMCUSign INVISIBLE ");
                    	mMobileTypeCU.setVisibility(View.INVISIBLE);
                	}
                	mSignalNetworkType.setImageDrawable(null);
                	mSignalNetworkType.setVisibility(View.GONE);
                }
                if(IS_CT) {
                	mMobileTypeCU.setVisibility(View.GONE);
                }
            }
            
            mMobileGroup.setContentDescription(mMobileTypeDescription + " " + mMobileDescription);

        } else {
            mMobileGroup.setVisibility(View.GONE);
        }
        
        if (mMobileVisibleGemini) {
            mMobileGroupGemini.setVisibility(View.VISIBLE);
            mMobileGemini.setImageResource(mMobileStrengthIdGemini[0]);
            if(IS_CT) {
            	mMobileGemini2.setImageResource(mMobileStrengthIdGemini[1]);
            }
            Xlog.d(TAG, "apply, mMobileVisibleGemini=" + mMobileVisibleGemini 
            		+" mMobileActivityIdGemini=" + mMobileActivityIdGemini
            		+" mMobileTypeIdGemini=" + mMobileTypeIdGemini+" mMobileStrengthIdGemini[0] = " +
            				""+mMobileStrengthIdGemini[0]+" mMobileStrengthIdGemini[1] = "+mMobileStrengthIdGemini[1]);
        
                mMobileActivityGemini.setImageResource(mMobileActivityIdGemini);
                mMobileTypeGemini.setImageResource(mMobileTypeIdGemini);

            if (IS_CU) {      
            	Xlog.d("SIMCUSign", "apply(GEMINI_SIM_2) mSIMCUSignGemini= "+mSIMCUSignGemini);
            	if( mSIMCUSignGemini ){
            		mMobileTypeCUGemini.setVisibility(View.VISIBLE);
            		mNullSignalSpacerGemini.setVisibility(View.GONE);
            	} else {
            		mMobileTypeCUGemini.setVisibility(View.GONE);
            		mNullSignalSpacerGemini.setVisibility(View.INVISIBLE);
            	}
                //if (isSimInserted(Phone.GEMINI_SIM_2) && Phone.SIM_INDICATOR_RADIOOFF != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_2)) {
            	if (isSimInserted(Phone.GEMINI_SIM_2) && Phone.SIM_INDICATOR_LOCKED != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_2) 
						  && Phone.SIM_INDICATOR_SEARCHING != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_2) 
						  && Phone.SIM_INDICATOR_INVALID != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_2) 
						  && Phone.SIM_INDICATOR_RADIOOFF != TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(Phone.GEMINI_SIM_2)) {

                	int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, Phone.GEMINI_SIM_2);
                	if (simColorId > -1 && simColorId < 4 && mDataNetTypeGemini != null) {
                		int resId ;
	                    switch(mDataNetTypeGemini){
	                    case _G:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_G[simColorId];
	                    	break;
	                    case _3G:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_3G[simColorId];
	                    	break;
	                    case _1X:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_1X[simColorId];
	                    	break;
	                    case _1X_3G:
	                    	resId = TelephonyIconsGemini.NETWORKTYE_1X_3G[simColorId];
	                    	break;
	                    default:
	                    	resId = 0;
	                    }
	                	Xlog.d(TAG, "mDataNetTypeGemini ="+mDataNetTypeGemini+" resId= "+resId+" simColorId = "+simColorId);
                		mSignalNetworkTypeGemini.setImageResource(resId);
	                    mSignalNetworkTypeGemini.setVisibility(View.VISIBLE);
	                    if(mMobileStrengthIdGemini[0] == R.drawable.zzz_stat_sys_signal_null_sim_ct || mMobileStrengthIdGemini[0] == 0){
	                    	mSignalNetworkTypeGemini.setVisibility(View.GONE);
	                    }
                	}
//                    ViewGroup.LayoutParams params = mSignalNetworkTypeComboGemini.getLayoutParams();
//                    params.width = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_signal_icon_expanded_outer_cu_width);
//                    mSignalNetworkTypeComboGemini.setLayoutParams(params);
                } else {
                	if (!isSimInserted(Phone.GEMINI_SIM_2)){
                		Xlog.d("SIMCUSign", "apply(GEMINI_SIM_2) mSIMCUSignGemini  INVISIBLE ");
                    	mMobileTypeCUGemini.setVisibility(View.INVISIBLE);
                	}
                	mSignalNetworkTypeGemini.setImageDrawable(null);
                	mSignalNetworkTypeGemini.setVisibility(View.GONE);
//                    ViewGroup.LayoutParams params = mSignalNetworkTypeComboGemini.getLayoutParams();
//                    params.width = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_signal_icon_outer_size);
//                    mSignalNetworkTypeComboGemini.setLayoutParams(params);
                 }
				if(IS_CT) {
            		mMobileTypeCUGemini.setVisibility(View.GONE);
                }
             }   
            	 
            mMobileGroupGemini.setContentDescription(mMobileTypeDescriptionGemini + " " + mMobileDescriptionGemini);

        } else {
            mMobileGroupGemini.setVisibility(View.GONE);
        }
        
        //if (!IS_CU) { 
	        Xlog.d(TAG, "apply, mMobileVisible=" + mMobileVisible + ", mWifiVisible=" + mWifiVisible + ", mIsAirplaneMode=" + mIsAirplaneMode);
	        if (mWifiVisible) {
	            mSpacer.setVisibility(View.INVISIBLE);
	        } else {
	            mSpacer.setVisibility(View.GONE);
	        }
	        
	        if (mMobileVisibleGemini && mMobileVisible) {
	            mSpacerGemini.setVisibility(View.INVISIBLE);
	        } else {
	            mSpacerGemini.setVisibility(View.GONE);
	        }
        //}

        if (DEBUG) {
            Xlog.d(TAG, String.format("mobile: %s sig=%d act=%d typ=%d",
                    (mMobileVisible ? "VISIBLE" : "GONE"), mMobileStrengthId, mMobileActivityId, mMobileTypeId));
            Xlog.d(TAG, String.format("mobile_gemini: %s sig_gemini=%d act_gemini=%d typ_gemini=%d",
                    (mMobileVisibleGemini ? "VISIBLE" : "GONE"), mMobileStrengthIdGemini, mMobileActivityIdGemini, mMobileTypeIdGemini));
        }
        
        mMobileType.setVisibility((!mWifiVisible || mRoaming) ? View.VISIBLE : View.GONE);
        mMobileTypeGemini.setVisibility((!mWifiVisible || mRoamingGemini) ? View.VISIBLE : View.GONE);
        if(mWifiVisible){
        	 mMobileActivity.setImageResource(0);
        	 mMobileActivityGemini.setImageResource(0);
        }
        
    }

    private boolean isSimInserted(int slotId) {
        boolean simInserted = false;
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if (phone != null) {
            try {
                simInserted = phone.isSimInsert(slotId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Xlog.d(TAG, "isSimInserted(" + slotId + "), SimInserted=" + simInserted);
        return simInserted;
    }
}

