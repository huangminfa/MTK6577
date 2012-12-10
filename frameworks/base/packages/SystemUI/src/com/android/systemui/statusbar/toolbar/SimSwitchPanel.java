package com.android.systemui.statusbar.toolbar;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipManager;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.R;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "Notification toolbar".
 */
public class SimSwitchPanel extends LinearLayout {
    private static final String TAG = "SimSwitchPanelView";
    private static final boolean DBG = true;
    
    private static final String SIP_CALL = "SIP_CALL";

    private static final String ALWAYS_ASK = "ALWAYS_ASK";
    
    private boolean mUpdating = false;
    
    private boolean mPanelShowing = false;
    
    // This variable should be used combing mPanleShowing, and only meaningful when mPanleShowing is true
    private String mCurrentBussinessType;
    
    // flags used to indicate sim icon views are already inflated
    private boolean mSimIconInflated = false;

    private List<SIMInfo> mSIMInfoList;
    private List<SimIconView> mSimInconViews;

    private String mCurrentServiceType;
    private ToolBarView mToolBarView;
    
    private AlertDialog mSwitchDialog;
    private SIMInfo mChooseSIMInfo;
    
    private SimIconView mSipCallIconView;
    private SimIconView mAlwaysAskIconView;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DBG) {
                Xlog.i(TAG, "sim state changed");
            }
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED) && mSIMInfoList != null && mSIMInfoList.size() > 0) {
            	for (int i = 0; i < mSIMInfoList.size(); i++) {
                    SIMInfo simInfo = mSIMInfoList.get(i);
                    SimIconView simIconView;
                    if(simInfo != null){
                    	simIconView = (SimIconView) findViewBySlotId(simInfo.mSlot);
                    	if(simIconView != null)
                    		simIconView.updateSimIcon(simInfo);
                    }                                        
                }
            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
            	if(mCurrentServiceType!=null && !mCurrentServiceType.equals(Settings.System.GPRS_CONNECTION_SIM_SETTING)){
            	    return;
            	}
            	String reason = intent.getStringExtra(Phone.STATE_CHANGE_REASON_KEY);
                Phone.DataState state = getMobileDataState(intent);
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    int simSlotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
                    if (DBG) {
                        Xlog.i(TAG, "mDataConnectionReceiver simId is : " + simSlotId);
                        Xlog.i(TAG, "mDataConnectionReceiver state is : " + state);
                        Xlog.i(TAG, "mDataConnectionReceiver reason is : " + reason);
                    }
                    if (reason == null){
                        return;
                    }

                    if (reason != null && (reason.equals(Phone.REASON_DATA_ATTACHED) || reason.equals(Phone.REASON_DATA_DETACHED))) {
                        switch (state) {
                            case CONNECTED:
                            	updateMobileConnection();
                                break;
                            case DISCONNECTED:
                            	updateMobileConnection();
                                break;
                        }   
                    } 
                }
            }else if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
            	SIMHelper.updateSIMInfos(mContext);
            	 mSIMInfoList = SIMHelper.getSIMInfoList(mContext);
                 int count = mSIMInfoList.size();
                 for (int i = 0; i < count; i++) {
                     SIMInfo simInfo = mSIMInfoList.get(i);
                     SimIconView simIconView;
                     if(simInfo != null){
                    	simIconView = (SimIconView) findViewBySlotId(simInfo.mSlot);
                    	if(simIconView != null) {
                    		simIconView.setSlotId(simInfo.mSlot);
                    		simIconView.updateSimIcon(simInfo);
                    	}
                     }
                 }
            }
        }
    };
    
    private static Phone.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(Phone.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(Phone.DataState.class, str);
        } else {
            return Phone.DataState.DISCONNECTED;
        }
    }
    
    /**
     * When siminfo changed, for example siminfo's background resource changed, need to reload all related UI.
     */
    public void updateSimInfo() {
    	buildSimIconViews();
    }
    
    private void updateMobileConnection() {
    	long simId = SIMHelper.getDefaultSIM(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING);
    	if (DBG) {
    	    Xlog.i(TAG, "updateMobileConnection, simId is" + simId);
    	}
    	switchSimId(simId);
    }

    public SimSwitchPanel(Context context) {
        this(context, null);
    }

    public SimSwitchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSimInconViews = new ArrayList<SimIconView>();
    }

    protected int getSuggestedMinimumWidth() {
        // makes the large background bitmap not force us to full width
        return 0;
    }

    void setUpdates(boolean update) {
        if (update != mUpdating) {
            mUpdating = update;
            if (update) {
                // Register for Intent broadcasts for the clock and battery
                IntentFilter filter = new IntentFilter();
                filter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
                filter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
                filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
                mContext.registerReceiver(mIntentReceiver, filter, null, null);
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
            }
        }
    }

    public void setToolBar(ToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }
    
    private static boolean isInternetCallEnabled(Context context) {
    	return Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL,0) == 1;
    }
    
    private boolean showSimIconViews(String bussinessType) {
    	mCurrentBussinessType = bussinessType;
    	// no sim switch for video call currently
    	if (bussinessType.equals(Settings.System.VIDEO_CALL_SIM_SETTING)) {
            return false;
        }
    	// if sim icon views are not inflated, should load at once
    	if (!mSimIconInflated) {
    		buildSimIconViews();
    	}
    	if (bussinessType.equals(Settings.System.VOICE_CALL_SIM_SETTING) && mSipCallIconView != null && isInternetCallEnabled(getContext())) {
    		mSipCallIconView.setVisibility(View.VISIBLE);
    		Xlog.i(TAG,"mSIMInfoList.size() 185 "+mSIMInfoList.size()+" mAlwaysAskIconView != null  is "+(mAlwaysAskIconView != null));
    		if(mSIMInfoList.size() >= 1 && mAlwaysAskIconView != null){
    			mAlwaysAskIconView.setVisibility(View.VISIBLE);
    		} else {
			if(mAlwaysAskIconView != null){
    				mAlwaysAskIconView.setVisibility(View.GONE);
    			}
		}
    			
    	} else {
    		if (mSipCallIconView != null) {
    			mSipCallIconView.setVisibility(View.GONE);
    		}
    		
    		if(mSIMInfoList.size() <= 1 && mAlwaysAskIconView != null){
    			mAlwaysAskIconView.setVisibility(View.GONE);
    		}
    	}
    	if(bussinessType.equals(Settings.System.SMS_SIM_SETTING)){
    		Xlog.i(TAG,"mSIMInfoList.size() 198 "+mSIMInfoList.size()+" mAlwaysAskIconView != null  is "+(mAlwaysAskIconView != null));
    		if(mSIMInfoList.size() > 1 && mAlwaysAskIconView != null){
    			mAlwaysAskIconView.setVisibility(View.VISIBLE);
    		} else {
    			if(mAlwaysAskIconView != null){
    				mAlwaysAskIconView.setVisibility(View.GONE);
    			}
    		}
    	}
    	return true;
    }
    
    public void setPanelShowing(boolean showing) {
    	mPanelShowing = showing;
    }
    
    public boolean isPanelShowing() {
    	return mPanelShowing;
    }

    private void buildSimIconViews() {
        this.removeAllViews();
        if (mSimInconViews != null) {
            mSimInconViews.clear();
        }
        mSIMInfoList = SIMHelper.getSIMInfoList(mContext);
        int count = mSIMInfoList.size();
        Xlog.i(TAG, "buildSimIconViews call, mSIMInfoList size is " + count);
        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < count; i++) {
            SIMInfo simInfo = mSIMInfoList.get(i);
            SimIconView simIconView = (SimIconView) View.inflate(mContext, R.layout.zzz_toolbar_sim_icon_view, null);
            simIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(simIconView, layutparams);
            mSimInconViews.add(simIconView);
            if(simInfo != null){
            	simIconView.setSlotId(simInfo.mSlot);
            }
            simIconView.setTagForSimIcon(simInfo);
            simIconView.setClickListener(mSimSwitchListener);
            simIconView.updateSimIcon(mSIMInfoList.get(i));
        }
        if (SipManager.isVoipSupported(mContext)) {
	        final SimIconView simIconView = (SimIconView) View.inflate(mContext, R.layout.zzz_toolbar_sim_icon_view, null);
	        simIconView.setSimIconViewResource(com.mediatek.internal.R.drawable.sim_background_sip);
	        simIconView.setOpName(R.string.gemini_intenet_call);
	        simIconView.setTag(SIP_CALL);
	        simIconView.setOrientation(LinearLayout.VERTICAL);
	        this.addView(simIconView, layutparams);
	        mSimInconViews.add(simIconView);
	        simIconView.setClickListener(new View.OnClickListener() {
	        	@Override
				public void onClick(View v) {
					for (int i = 0; i < mSimInconViews.size(); i++) {
			            mSimInconViews.get(i).setSelected(false);
			        }
					simIconView.setSelected(true);
					Settings.System.putLong(mContext.getContentResolver(), mCurrentServiceType,
	                                            Settings.System.VOICE_CALL_SIM_SETTING_INTERNET);
	                Intent intent = new Intent();
	                intent.putExtra("simid", Settings.System.VOICE_CALL_SIM_SETTING_INTERNET);
	                intent.setAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
	                SimSwitchPanel.this.getContext().sendBroadcast(intent);
	                mToolBarView.getStatusBarService().animateCollapse();
				}
			});
	        mSipCallIconView = simIconView;
	        // if panel is already showing and buildSimIconViews() is called, we should double check if sip call view should be shown
	        if (mPanelShowing) {
	        	String bussinessType = mCurrentBussinessType;
	        	if (bussinessType.equals(Settings.System.VOICE_CALL_SIM_SETTING) && isInternetCallEnabled(getContext())) {
	        		mSipCallIconView.setVisibility(View.VISIBLE);
	        	} else {
	        		mSipCallIconView.setVisibility(View.GONE);
	        	}
	        	long simId = -1;
	            simId = Settings.System.getLong(mContext.getContentResolver(), bussinessType, -1);
	            switchSimId(simId);
	        }
        }
        {
	        final SimIconView simIconView = (SimIconView) View.inflate(mContext, R.layout.zzz_toolbar_sim_icon_view, null);
	        simIconView.setSimIconViewResource(R.drawable.zzz_sim_always_ask);
	        simIconView.setOpName(R.string.gemini_default_sim_always_ask);
	        simIconView.setTag(ALWAYS_ASK);
	        simIconView.setOrientation(LinearLayout.VERTICAL);
	        this.addView(simIconView, layutparams);
	        mSimInconViews.add(simIconView);
	        simIconView.setClickListener(new View.OnClickListener() {
	        	@Override
				public void onClick(View v) {
					for (int i = 0; i < mSimInconViews.size(); i++) {
			            mSimInconViews.get(i).setSelected(false);
			        }
					simIconView.setSelected(true);
					Settings.System.putLong(mContext.getContentResolver(), mCurrentServiceType,
	                                            Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK);
	                Intent intent = new Intent();
	                intent.putExtra("simid", Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK);
	                if (mCurrentServiceType.equals(Settings.System.VOICE_CALL_SIM_SETTING)) {
	                    intent.setAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
	                } else if (mCurrentServiceType.equals(Settings.System.SMS_SIM_SETTING)) {
	                    intent.setAction(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED);
	                }
	                SimSwitchPanel.this.getContext().sendBroadcast(intent);
	                mToolBarView.getStatusBarService().animateCollapse();
//	                mToolBarView.setSimSwitchPanleVisibility(false);
				}
			});
	        mAlwaysAskIconView = simIconView;
	        if (mPanelShowing) {
	        	String bussinessType = mCurrentBussinessType;
	        	if(mSIMInfoList.size() >=2 || (bussinessType.equals(Settings.System.VOICE_CALL_SIM_SETTING) && mSIMInfoList.size() == 1 && SipManager.isVoipSupported(mContext) && isInternetCallEnabled(getContext()))) {
	        		mAlwaysAskIconView.setVisibility(View.VISIBLE);
	        	} else {
	        		mAlwaysAskIconView.setVisibility(View.GONE);
	        	}
	        	if(mSIMInfoList.size() == 1 && bussinessType.equals(Settings.System.SMS_SIM_SETTING)){
	        		mAlwaysAskIconView.setVisibility(View.GONE);
	        	}
	        	long simId = -1;
	            simId = Settings.System.getLong(mContext.getContentResolver(), bussinessType, -1);
	            switchSimId(simId);
	        }
        }
        mSimIconInflated = true;
    }

    private View.OnClickListener mSimSwitchListener = new View.OnClickListener() {
        public void onClick(View v) {
            SIMInfo simInfo = (SIMInfo) v.getTag();
            long simId = simInfo.mSimId;
            int simState = TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(simInfo.mSlot);
            if (DBG) {
                Xlog.i(TAG, "user clicked simIcon, simId is : " + simId + " , simState = " + simState);
            }
            if(simState == Phone.SIM_INDICATOR_RADIOOFF){ 
            	
            	mChooseSIMInfo=simInfo;
            	
            	if(mSwitchDialog==null){
            		mSwitchDialog=createDialog(simInfo);
            	}else{
            		String mText;
                	if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
            		    mText = getResources().getString(R.string.confirm_radio_msg,simInfo.mDisplayName);
                    } else {
                        mText = getResources().getString(R.string.confirm_radio_msg_single);
                    }
                	mSwitchDialog.setMessage(mText);
            	}
            	mSwitchDialog.show();            	
            }else{
            	changeDefaultSim(simInfo);
            }
        }
    };
    
    private AlertDialog createDialog(SIMInfo simInfo) {
    	String mText;
    	if (FeatureOption.MTK_GEMINI_SUPPORT) {
		    mText = getResources().getString(R.string.confirm_radio_msg,simInfo.mDisplayName
				    );
            } else {
            mText = getResources().getString(R.string.confirm_radio_msg_single);
            }
    	AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setCancelable(true)
        .setTitle(R.string.confirm_radio_title)
        .setMessage(mText)
        .setInverseBackgroundForced(true)
        .setNegativeButton(android.R.string.cancel,mRadioOffListener)
        .setPositiveButton(R.string.confirm_radio_lbutton, mRadioOffListener);
        
    	AlertDialog alertDialog = b.create();
    	alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_SUB_PANEL);
    	return alertDialog;
    }
    
   private DialogInterface.OnClickListener mRadioOffListener=new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mSwitchDialog != null) {
				mSwitchDialog.dismiss();
			}
			switch(which){
			case DialogInterface.BUTTON_POSITIVE:
				radioOnBySlot(mChooseSIMInfo.mSlot);
				changeDefaultSim(mChooseSIMInfo);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};
    
   	private void radioOnBySlot(int slot) {
		ContentResolver cr = mContext.getContentResolver();

		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			int dualSimMode = 0;
			if (1 == Settings.System.getInt(cr,
					Settings.System.AIRPLANE_MODE_ON, -1)) {
			    Xlog.d(TAG, "radioOnBySlot powerRadioOn airplane mode on");
				Settings.System.putInt(cr, Settings.System.AIRPLANE_MODE_ON, 0);
				if(slot==Phone.GEMINI_SIM_1)
					Settings.System.putInt(cr, Settings.System.DUAL_SIM_MODE_SETTING, 1);
				if(slot==Phone.GEMINI_SIM_2)
				    Settings.System.putInt(cr, Settings.System.DUAL_SIM_MODE_SETTING, 2);
				mContext.sendBroadcast(new Intent(
						Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra("state",
						false));
				Xlog.d(TAG, "radioOnBySlot powerRadioOn airplane mode off");
				if (0 == slot) {
				    Xlog.d(TAG, "radioOnBySlot powerRadioOn change to SIM1 only");
					dualSimMode = 1;
				} else if (1 == slot) {
				    Xlog.d(TAG, "radioOnBySlot powerRadioOn change to SIM2 only");
					dualSimMode = 2;
				}
			} else {
			    Xlog.d(TAG, "radioOnBySlot powerRadioOn: airplane mode is off");
			    Xlog.d(TAG,"radioOnBySlot powerRadioOn: airplane mode is off and dualSimMode = "+ dualSimMode);
				if (0 == Settings.System.getInt(cr,Settings.System.DUAL_SIM_MODE_SETTING, -1)) {
					dualSimMode = slot + 1;
				} else {
					dualSimMode = 3;
				}
			}
				Settings.System.putInt(cr,Settings.System.DUAL_SIM_MODE_SETTING, dualSimMode);
				Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
				intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, dualSimMode);
				mContext.sendBroadcast(intent);
			
		} else {
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			Settings.System.putInt(cr, Settings.System.AIRPLANE_MODE_ON, 0);
			mContext.sendBroadcast(intent);
		}
	}
    
    private void changeDefaultSim(SIMInfo simInfo){
    	long simId=simInfo.mSimId;
    	if (simId == Settings.System.getLong(mContext.getContentResolver(), mCurrentServiceType, -1)) {
            mToolBarView.getStatusBarService().animateCollapse();
            return;
        } else {
        	if (!mCurrentServiceType.equals(Settings.System.GPRS_CONNECTION_SIM_SETTING)) {
        		Settings.System.putLong(mContext.getContentResolver(), mCurrentServiceType, simId);
        	}
            Intent intent = new Intent();
            if (mCurrentServiceType.equals(Settings.System.VOICE_CALL_SIM_SETTING)) {
                intent.putExtra("simid", simId);
                intent.setAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            } else if (mCurrentServiceType.equals(Settings.System.SMS_SIM_SETTING)) {
                intent.putExtra("simid", simId);
                intent.setAction(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED);
            } else if (mCurrentServiceType.equals(Settings.System.GPRS_CONNECTION_SIM_SETTING)) {
            	intent.putExtra(Phone.MULTI_SIM_ID_KEY, simId);
            	intent.setAction(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
            }
            SimSwitchPanel.this.getContext().sendBroadcast(intent);
            updateSimSelectState(simInfo);
            mToolBarView.getStatusBarService().animateCollapse();
        }
    }

    public final boolean updateSimService(String bussinessType) {
        mCurrentServiceType = bussinessType;
        boolean shouldShowSim = showSimIconViews(bussinessType);
//        if (!shouldShowSim) {
//            mToolBarView.setSimSwitchPanleVisibility(false);
//            return false;
//        }
        long simId = -1;
        simId = Settings.System.getLong(mContext.getContentResolver(), bussinessType, -1);
        if (DBG) {
            Xlog.i(TAG, "updateSimService, bussinessType is: " + bussinessType + ", simId is " + simId);
        }
        switchSimId(simId);
        return true;
    }

    private void switchSimId(long simId) {
    	if ( mSIMInfoList == null) {
    		// this statement may come in when sim switch panel is not ready,but data connection sim changed
    		return;
    	}
	//update3GIconState();

    	if (simId > 0) {
    		updateSimIcons(simId);
    	} else if (simId == 0) {
    		for (int i = 0; i < mSimInconViews.size(); i++) {
                mSimInconViews.get(i).setSelected(false);
            }
    	} else if (simId == -2) {
    		for (int i = 0; i < mSimInconViews.size(); i++) {
                mSimInconViews.get(i).setSelected(false);
            }
    		SimIconView selectedSimIconView = (SimIconView) findViewWithTag(SIP_CALL);
    		if (selectedSimIconView == null) {
    			if (DBG) {
    			    Xlog.i(TAG, "switchSimId failed, bussinessType is: " + mCurrentServiceType + ", simId is " + simId);
    			}
            } else {
            	selectedSimIconView.setSelected(true);
            }
    	} else if (simId == -1) {
    		for (int i = 0; i < mSimInconViews.size(); i++) {
                mSimInconViews.get(i).setSelected(false);
            }
    		SimIconView selectedSimIconView = (SimIconView) findViewWithTag(ALWAYS_ASK);
    		if (selectedSimIconView == null) {
    			if (DBG) {
    			    Xlog.i(TAG, "switchSimId failed, bussinessType is: " + mCurrentServiceType + ", simId is " + simId);
    			}
            } else {
            	selectedSimIconView.setSelected(true);
            }
    	}
    }

    private void updateSimIcons(long simId) {
    	SIMInfo simInfo = SIMHelper.getSIMInfo(mContext, simId);
        if(simInfo != null) {
    	    updateSimSelectState(simInfo);
        }
    }
    
    private void update3GIconState() {
        for (int i = 0; i < mSimInconViews.size(); i++) {
            mSimInconViews.get(i).set3GIconVisibility(false);
        }
        // set 3g slot icon state
        SIMInfo simInfo = SIMHelper.get3GSlotSimInfo(mContext);
        if (simInfo != null) {
	        SimIconView sim3GIconView = (SimIconView) findViewBySlotId(simInfo.mSlot);//xinxin
	        if (sim3GIconView != null) {
	        	sim3GIconView.set3GIconVisibility(true);
	        } else {
	        	if (DBG) {
	        	    Xlog.i(TAG, "update3GIconState failed, bussinessType is: " + mCurrentServiceType + ", simId is " + simInfo.mSimId);
	        	}
	        }
        }
    }
    
    private SimIconView findViewBySlotId(int slotId){
    	for(SimIconView simIconView : mSimInconViews){
    		if(simIconView.getSlotId() == slotId){
    			return simIconView;
    		}
    	}
    	return null;
    }
    
    private void updateSimSelectState(SIMInfo simInfo) {
        if (simInfo == null) {
            Xlog.i(TAG, "updateSimSelectState failed for simInfo is null, bussinessType is: " + mCurrentServiceType);
            return;
        }
    	for (int i = 0; i < mSimInconViews.size(); i++) {
            mSimInconViews.get(i).setSelected(false);
        }
        SimIconView selectedSimIconView = (SimIconView) findViewBySlotId(simInfo.mSlot);
        if (selectedSimIconView != null) {
        	selectedSimIconView.setSelected(true);
        } else {
        	if (DBG) {
        	    Xlog.i(TAG, "updateSimSelectState failed, bussinessType is: " + mCurrentServiceType + ", simId is " + simInfo.mSimId);
        	}
        }
    }
    
    public void enlargeTouchRegion() {
    	if (mSimInconViews == null) {
    		return;
    	}
    	for (int i = 0; i < mSimInconViews.size(); i++) {
            mSimInconViews.get(i).enlargeTouchRegion();
        }
    }
    
    public void updateResources(){
    	if(mSimInconViews!=null && mSimInconViews.size()!=0){
    		if(mSimInconViews.size() >= 2) {
			SimIconView sipIconView=mSimInconViews.get(mSimInconViews.size()-2);
	    		if( sipIconView != null && sipIconView.getTag()!= null && sipIconView.getTag().equals(SIP_CALL)){
    				sipIconView.setOpName(R.string.gemini_intenet_call);
    			}
		}
			SimIconView sipIconViewAlwaysAsk = mSimInconViews.get(mSimInconViews.size() - 1);
			if (sipIconViewAlwaysAsk != null && sipIconViewAlwaysAsk.getTag() != null && sipIconViewAlwaysAsk.getTag().equals(ALWAYS_ASK)) {
				sipIconViewAlwaysAsk.setOpName(R.string.gemini_default_sim_always_ask);
			}
    	}
    	if(mSwitchDialog!=null){
    		mSwitchDialog.setTitle(getResources().getString(R.string.confirm_radio_title));
    		mSwitchDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.confirm_radio_lbutton);
    		mSwitchDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(android.R.string.cancel);
    		if(mChooseSIMInfo!=null){
    			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
    				mSwitchDialog.setMessage(getResources().getString(R.string.confirm_radio_msg,mChooseSIMInfo.mDisplayName));
    			} else {
    				mSwitchDialog.setMessage(getResources().getString(R.string.confirm_radio_msg_single));
    			}
    		}
    	}
    }
}
