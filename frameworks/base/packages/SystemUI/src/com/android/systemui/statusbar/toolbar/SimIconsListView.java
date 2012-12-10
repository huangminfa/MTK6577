package com.android.systemui.statusbar.toolbar;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.systemui.R;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "Notification toolbar".
 * AlretDialog used for DISPLAY TEXT commands.
 */
public class SimIconsListView extends ListView {
	
	// members
    private static final String TAG = "SimIconsListView";
    private static final boolean DBG = true;
    
    private Context mContext;
    
    private static final String CUMccMnc = "46001";
	
	private static final int DIALOG_DATA_CONNECTION = 0;
    
    private List<SimItem> mSimItems = new ArrayList<SimItem>();
    
    private long mSelectedSimId;
    
    private String mServiceType;
    
    private SimInfotListAdapter mSimInfotListAdapter;
    
    private TelephonyManagerEx mTelephonyManager;
    
    private static boolean mIsCU = false;
    
    private static boolean IsCU = false;
    private boolean mIsNoCUCard = true;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)){
				initSimList();
				notifyDataChange();
			}
		}
    };
    
    public SimIconsListView(Context context, String serviceType) {
        super(context, null);
        mContext = context;
        mServiceType = serviceType;
        initListViews();
        if(SIMHelper.isCU() || SIMHelper.isCT()) {
        	mIsCU = true;
        }
    }
    
    private void initListViews() {
    	mTelephonyManager = TelephonyManagerEx.getDefault();

    	IntentFilter mIntentFilter=new IntentFilter();
    	mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
    	mContext.registerReceiver(mIntentReceiver, mIntentFilter);
        
        setCacheColorHint(0);
        
    	initSimList();
    	mSimInfotListAdapter = new SimInfotListAdapter(mContext);
    	setAdapter(mSimInfotListAdapter);
    }
    
    private static class SimInfoViewHolder {
		TextView simType;
		TextView simShortNumber;
        ImageView simStatus;
        TextView simOpName;
        TextView simNumber;
        RadioButton simSelectedRadio;
        RelativeLayout mSimBg;
        
	}
    
    private class SimInfotListAdapter extends BaseAdapter {

    	public SimInfotListAdapter(Context context) {
    		mContext = context;
    		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
    	
    	private Context mContext;
    	
    	private LayoutInflater mInflater;
    	

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		SimInfoViewHolder simInfoViewHolder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.zzz_toolbar_dialog_sim_icon, null);
				simInfoViewHolder = new SimInfoViewHolder();
				simInfoViewHolder.mSimBg = (RelativeLayout) convertView.findViewById(R.id.sim_icon_bg);
				simInfoViewHolder.simType = (TextView) convertView.findViewById(R.id.sim_type);
				simInfoViewHolder.simShortNumber = (TextView) convertView.findViewById(R.id.sim_short_number);
				simInfoViewHolder.simStatus = (ImageView) convertView.findViewById(R.id.sim_status);
				simInfoViewHolder.simOpName = (TextView) convertView.findViewById(R.id.sim_op_name);
				simInfoViewHolder.simNumber = (TextView) convertView.findViewById(R.id.sim_number);
				simInfoViewHolder.simSelectedRadio = (RadioButton) convertView.findViewById(R.id.enable_state);
				
    			convertView.setTag(simInfoViewHolder);
			} else {
				simInfoViewHolder = (SimInfoViewHolder)convertView.getTag();
			}
			SimItem simItem = mSimItems.get(position);
			if (!simItem.mIsSim) {
				if (simItem.mColor == 8){
					simInfoViewHolder.mSimBg.setVisibility(View.VISIBLE);
					simInfoViewHolder.mSimBg.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
				} else {
					simInfoViewHolder.mSimBg.setVisibility(View.GONE);
				}
				simInfoViewHolder.simOpName.setText(simItem.mName);
				simInfoViewHolder.simNumber.setVisibility(View.GONE);
				simInfoViewHolder.mSimBg.setVisibility(View.GONE);
				simInfoViewHolder.simType.setVisibility(View.GONE);
			} else {
				simInfoViewHolder.mSimBg.setVisibility(View.VISIBLE);
				simInfoViewHolder.mSimBg.setBackgroundResource(simItem.mColor);
				simInfoViewHolder.simOpName.setText(simItem.mName);
				if(simItem.mState == com.android.internal.telephony.Phone.SIM_INDICATOR_RADIOOFF) {
					simInfoViewHolder.simOpName.setTextColor(Color.GRAY);
				} else {
					simInfoViewHolder.simOpName.setTextColor(Color.WHITE);
				}
				if (simItem.mNumber != null && simItem.mNumber.length() > 0) {
					simInfoViewHolder.simNumber.setVisibility(View.VISIBLE);
					simInfoViewHolder.simNumber.setText(simItem.mNumber);
					if(simItem.mState == com.android.internal.telephony.Phone.SIM_INDICATOR_RADIOOFF) {
						simInfoViewHolder.simNumber.setTextColor(Color.GRAY);
					} else {
						simInfoViewHolder.simNumber.setTextColor(Color.WHITE);
					}
				} else {
					simInfoViewHolder.simNumber.setVisibility(View.GONE);
				}
				simInfoViewHolder.simStatus.setImageResource(SIMHelper.getSIMStateIcon(simItem.mState));
				simInfoViewHolder.simShortNumber.setText(simItem.getFormatedNumber());
				if(!SIMHelper.isCU())
					simInfoViewHolder.simType.setVisibility(SIMHelper.get3GSlot() == simItem.mSlot ? View.VISIBLE : View.GONE);
				else
					simInfoViewHolder.simType.setVisibility(View.GONE);
			}
			simInfoViewHolder.simSelectedRadio.setChecked(simItem.mSimID == mSelectedSimId);
			if (DBG) {
				Xlog.i(TAG, "getVIew called, simItem's simId is " + simItem.mSimID + ", mSelectedSimId is " + mSelectedSimId);
				Xlog.i(TAG, "getVIew called, simItem's simColor is " + simItem.mColor);
			}
			
			if (simItem.mIsSim) {
				int simState = TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(simItem.mSlot);
				if((simState == Phone.SIM_INDICATOR_RADIOOFF || simState == Phone.SIM_INDICATOR_LOCKED)){
				    Xlog.i(TAG, "simItem is radio off");
					simInfoViewHolder.simOpName.setEnabled(false);
					simInfoViewHolder.simNumber.setEnabled(false);
					simInfoViewHolder.simSelectedRadio.setEnabled(false);
					convertView.setEnabled(false);
				} else {
				    Xlog.i(TAG, "simItem is not radio off");
					simInfoViewHolder.simOpName.setEnabled(true);
					simInfoViewHolder.simNumber.setEnabled(true);
					simInfoViewHolder.simSelectedRadio.setEnabled(true);
					convertView.setEnabled(true);
				}
			}
			
			if (simItem.mSimID == Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
				boolean shouldEnable = false;
				for(int i = 0;i < mSimItems.size();i++) {
					SimItem simItemTemp = mSimItems.get(i);
					if(simItemTemp.mIsSim) {
						if(simItemTemp.mState != Phone.SIM_INDICATOR_RADIOOFF) {
							shouldEnable = true;
							break;
						}
					}
				}
				if(!shouldEnable) {
					simInfoViewHolder.simOpName.setTextColor(Color.GRAY);
				} else {
					simInfoViewHolder.simOpName.setTextColor(Color.WHITE);
				}
				simInfoViewHolder.simSelectedRadio.setEnabled(shouldEnable);
				convertView.setEnabled(shouldEnable);	
			}
			return convertView;
    	}

		@Override
		public int getCount() {
			return mSimItems.size();
		}

		@Override
		public SimItem getItem(int position) {
			return mSimItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
    }
    
    class SimItem {
	    public boolean mIsSim = true;
	    public String mName = null;
	    public String mNumber = null;
	    public int mDispalyNumberFormat = 0;
	    public int mColor = -1;
	    public int mSlot = -1;
	    public long mSimID = -1;
	    public int mState = Phone.SIM_INDICATOR_NORMAL;
	    public boolean mIsCU = true;
	    
	    //Constructor for not real sim
	    public SimItem (String name, int color,long simID) {
	    	mName = name;
	    	mColor = color;
	    	mIsSim = false;
	    	mSimID = simID;
	    }
	    //constructor for sim
	    public SimItem (SIMInfo siminfo) {
	    	mIsSim = true;
	    	mName = siminfo.mDisplayName;
	    	mNumber = siminfo.mNumber;
	    	mDispalyNumberFormat = siminfo.mDispalyNumberFormat;
	    	mColor = siminfo.mSimBackgroundRes;
	    	mSlot = siminfo.mSlot;
	    	mSimID = siminfo.mSimId;
	    }
	    
	    private String getFormatedNumber() {
	    	if (mNumber == null || mNumber.isEmpty()) {
	    		return "";
	    	}
	    	if (DBG) {
	    	    Xlog.i(TAG, "getFormatedNumber called, mNumber is " + mNumber);
	    	}
	        switch (mDispalyNumberFormat) {
	        case (SimInfo.DISPLAY_NUMBER_FIRST):
	            if (mNumber.length() <= 4) {
	                return mNumber;
	            }
	            return mNumber.substring(0, 4);
	        case (SimInfo.DISPLAY_NUMBER_LAST):
	            if (mNumber.length() <= 4) {
	                return mNumber;
	            }
	            return mNumber.substring(mNumber.length() - 4, mNumber.length());
	        case (SimInfo.DISPALY_NUMBER_NONE):
	            return "";
	        default:
	            return "";
	        }
	    }
	}
    
    public void initSimList() {
    	
    	mSelectedSimId = SIMHelper.getDefaultSIM(mContext, mServiceType);
        //initialize the default sim preferences
    	mSimItems.clear(); 	

    	SimItem simitem;
    	SIMInfo simInfo;
    	
    	List<SIMInfo> simList = SIMHelper.getSIMInfoList(mContext);
    	if(simList == null || simList.size() == 0) {
    		return;
    	}
    	for(int i=0; i<simList.size(); i++) {
    		simInfo = simList.get(i);
    		String numeric = "";
			if (simInfo.mSlot == Phone.GEMINI_SIM_2) {
				numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2);
			} else {
				numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
			}
			if(numeric.equals(CUMccMnc) == true) {
				mIsNoCUCard = false;
				Xlog.i(TAG, "IsAllCUCard = true;");
			} 
    	}
    	
    	if (mServiceType.equals(Settings.System.VIDEO_CALL_SIM_SETTING)) {
    		simInfo = SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_1);
    		if(simInfo != null) {
    			simitem = new SimItem(simInfo);
	        	int state = mTelephonyManager.getSimIndicatorStateGemini(simInfo.mSlot);
	        	simitem.mState = state;
        		mSimItems.add(simitem);
        	}
    		return;
    	}
    	
        for (int i=0; i<simList.size(); i++) {
        	simInfo = simList.get(i);
        	if (simInfo != null) {
        		
        		simitem = new SimItem(simInfo);
	        	int state = mTelephonyManager.getSimIndicatorStateGemini(simInfo.mSlot);
	        	simitem.mState = state;
	        	mSimItems.add(simitem);
        	}
        }
        
        if (mServiceType.equals(Settings.System.GPRS_CONNECTION_SIM_SETTING)) {
        	simitem = new SimItem (mContext.getString(R.string.gemini_default_sim_never), -1, Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER);
        	mSimItems.add(simitem);	
        }
    }
    
    public void notifyDataChange() {
    	if (mSimInfotListAdapter != null) {
        	mSimInfotListAdapter.notifyDataSetChanged();
        }
    }

    public void updateResources(){
    	if(mSimItems!=null && mSimItems.size()!=0){
    		if(mServiceType.equals(Settings.System.GPRS_CONNECTION_SIM_SETTING)){
    			mSimItems.get(mSimItems.size()-1).mName=mContext.getString(R.string.gemini_default_sim_never);
    		}
    	}
    }    
}
