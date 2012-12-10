//For Operator Custom
//MTK_OP02_PROTECT_START
package com.android.settings.gemini;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.drawable.Drawable;

import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.preference.ListPreference;

import android.provider.Telephony;

import com.android.settings.R;

import com.android.internal.telephony.Phone;


import android.provider.Telephony.SIMInfo;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.internal.telephony.TelephonyIntents;

import com.mediatek.featureoption.FeatureOption;

import com.mediatek.xlog.Xlog;



public class NetworkModePreference extends ListPreference {
	
    private static final String TAG = "NetworkModePreference";
	private Context mContext;
    private IntentFilter mIntentFilter;
	private LayoutInflater mFlater;
    private boolean IsMultipleCards = false;
    private String mSimNum;
    private int mColor;
    private String mName;
    private int mNumDisplayFormat;
    private int mStatus;
    
	private static final int DISPLAY_NONE = 0;
	private static final int DISPLAY_FIRST_FOUR = 1;
	private static final int DISPLAY_LAST_FOUR = 2;	
	
	
	
    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    private TelephonyManagerEx mTelephonyManager;
    

    public NetworkModePreference(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	mContext = context;
        mFlater = LayoutInflater.from(context);	
        

        mTelephonyManager = TelephonyManagerEx.getDefault();


    }   
    
    public void setMultiple(boolean value) {
    	
    	IsMultipleCards = value;
    	
    	if (IsMultipleCards == true) {
   		
			SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext,
					Phone.GEMINI_SIM_1);
			if (info == null) {
				Xlog.e(TAG, "can not get slot 1 sim info");
				IsMultipleCards = false;
			} else {
				mSimNum = info.mNumber;
				mColor = info.mColor;
				mName = info.mDisplayName;
				mNumDisplayFormat = info.mDispalyNumberFormat;
				
	        	mStatus = mTelephonyManager.getSimIndicatorStateGemini(info.mSlot);

			}

    	}
    }
    

    
    void setStatus(int status) {
    	mStatus = status;
        notifyChanged();
    }



 
    
    public void updateSummary() {
        setSummary(getEntry());

    }
    
 
    @Override
	public View getView(View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
       View view = super.getView(convertView, parent);
       
       if(IsMultipleCards == false) {
           setSummary(getEntry());
           
       } else {
			
			// TODO Auto-generated method stub
			
			   view = mFlater.inflate(R.layout.preference_sim_info,null);
			   
		        TextView viewTitle = (TextView)view.findViewById(android.R.id.title);
	            if ((mName != null)&&(viewTitle!=null)) {
	            	viewTitle.setText(mName);

	                Xlog.i(TAG, "mName is "+mName);
	            }
		        TextView viewSummary = (TextView)view.findViewById(android.R.id.summary);
		        
		        if(viewSummary!=null) {
		            if((mSimNum != null)&&(mSimNum.length() != 0)) {
		            	viewSummary.setText(mSimNum);

		            } else {
		            	viewSummary.setVisibility(View.GONE);
		            }
		        }

		       
		       ImageView imageStatus = (ImageView) view.findViewById(R.id.simStatus);
		       
		       if(imageStatus != null) {
		    	   
		    	   int res = GeminiUtils.getStatusResource(mStatus);
		    	   
		    	   if(res == -1) {
		    		   imageStatus.setVisibility(View.GONE);
		    	   } else {
		        	   imageStatus.setImageResource(res);   		   
		    	   }

		       }
		       
		       TextView text3G = (TextView) view.findViewById(R.id.sim3g);
		       if(text3G != null) {
		    	   if((GeminiUtils.mNeed3GText == false)){
		               text3G.setVisibility(View.GONE);
		    	   }
		       }
		        RelativeLayout viewSim = (RelativeLayout)view.findViewById(R.id.simIcon);
		        
		        if (viewSim != null) {
		        	
		        	int res = GeminiUtils.getSimColorResource(mColor);
		        	
		        	if(res<0) {
		        		viewSim.setBackgroundDrawable(null);
		        	} else {
		        		viewSim.setBackgroundResource(res);
		        	}

		        }    
		        

		        
		        Switch ckRadioOn = (Switch)view.findViewById(R.id.Check_Enable);

		        if (ckRadioOn != null) {
		        	ckRadioOn.setVisibility(View.GONE);
		        }
		        
		        TextView textNum = (TextView)view.findViewById(R.id.simNum);
		        

		        if ((textNum != null) && (mSimNum!=null)) {
		        	
		            switch (mNumDisplayFormat) {
		        	case DISPLAY_NONE: {
		        		textNum.setVisibility(View.GONE);
		        		break;
		        		
		        	}
		        	case DISPLAY_FIRST_FOUR: {
		        		
		        		if (mSimNum.length()>=4) {
		            		textNum.setText(mSimNum.substring(0, 4));
		        		} else {
		            		textNum.setText(mSimNum);
		        		}
		        		break;
		        	}
		        	case DISPLAY_LAST_FOUR: {
		        		
		        		if (mSimNum.length()>=4) {
		            		textNum.setText(mSimNum.substring(mSimNum.length()-4));
		        		} else {
		            		textNum.setText(mSimNum);
		        		}
		        		break;
		        	}
		        		
		        
		        }       	
		        }
		        
		        boolean bEnable = (mStatus == Phone.SIM_INDICATOR_RADIOOFF)?false:true;

				if((view != null)&&(viewTitle != null)&&(viewSummary != null)) {
						view.setEnabled(bEnable);
						viewTitle.setEnabled(bEnable);
						viewSummary.setEnabled(bEnable);
						setEnabled(bEnable);
						Xlog.i(TAG, "Radio state is "+bEnable);
				}

		        


			
			}


		return view;
	}

    


}
//MTK_OP02_PROTECT_END
