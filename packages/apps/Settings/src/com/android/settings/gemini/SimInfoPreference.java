package com.android.settings.gemini;


import android.content.Context;
import android.content.Intent;

import android.graphics.drawable.Drawable;

import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;

import android.os.SystemProperties;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;


import com.android.internal.telephony.Phone;

import com.mediatek.xlog.Xlog;

public class SimInfoPreference extends Preference {
	


	private int mStatus;

	private String mSimNum;
	protected final int mSlotIndex;
	private String mName;
	private int mColor;
	private int mNumDisplayFormat;
	private boolean mChecked = true;
	private boolean mNeedCheckbox = true;
	private boolean mNeedStatus = true;	
	private boolean mUseCheckBox=false;
	private Context mContext;
	

	private static final int DISPLAY_NONE = 0;
	private static final int DISPLAY_FIRST_FOUR = 1;
	private static final int DISPLAY_LAST_FOUR = 2;	
    private static final String TAG = "SimInfoPreference";
    public SimInfoPreference(Context context, String name, String number, 
    		int SimSlot, int status, int color, int DisplayNumberFormat, 
    		long key, boolean needCheckBox) {
    	this(context, name, number, SimSlot, status, color, 
    			DisplayNumberFormat, key, needCheckBox, true);



    }
    //because modify the switch to checkbox, but some ui still need to use checkbox, therefore
    //add one more constructor to use the layout with checkbox available.
    public SimInfoPreference(Context context, String name, String number, 
    		int SimSlot, int status, int color, int DisplayNumberFormat, 
    		long key, boolean needCheckBox, boolean needStatus,boolean useCheckBox) {
    	
        
        super(context, null);
        mName = name;
        mSimNum = number;
        mSlotIndex = SimSlot;
        mStatus = status;
        mColor = color;
        mNumDisplayFormat = DisplayNumberFormat;
        mNeedCheckbox = needCheckBox;
        mNeedStatus = needStatus;
        mContext = context;
        mUseCheckBox = useCheckBox;
        setKey(String.valueOf(key));
        
        setLayoutResource(R.layout.preference_sim_info_checkbox);
        
        if (mName != null) {
            setTitle(mName);
        }
        if((mSimNum != null)&&(mSimNum.length() != 0)){
            setSummary(mSimNum);

        }

    }
    public SimInfoPreference(Context context, String name, String number, 
    		int SimSlot, int status, int color, int DisplayNumberFormat, 
    		long key, boolean needCheckBox, boolean needStatus) {
    	
        
        super(context, null);
        mName = name;
        mSimNum = number;
        mSlotIndex = SimSlot;
        mStatus = status;
        mColor = color;
        mNumDisplayFormat = DisplayNumberFormat;
        mNeedCheckbox = needCheckBox;
        mNeedStatus = needStatus;
        mContext = context;
        setKey(String.valueOf(key));

        
        setLayoutResource(R.layout.preference_sim_info);
        
        if (mName != null) {
            setTitle(mName);
        }
        if((mSimNum != null)&&(mSimNum.length() != 0)){
            setSummary(mSimNum);

        }

    }
    @Override
	public View getView(View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
       View view = super.getView(convertView, parent);
       

       TextView textTitle = (TextView) view.findViewById(android.R.id.title);
       
       if((textTitle != null)&&(mName != null)) {
    	   textTitle.setText(mName);
       }
       
      TextView textNum = (TextView) view.findViewById(android.R.id.summary);
      if(textNum != null) {
          if((mSimNum != null)&&(mSimNum.length() != 0)){
              if(!textNum.isShown())
                  textNum.setVisibility(View.VISIBLE);
              textNum.setText(mSimNum);

          } else {

          	textNum.setVisibility(View.GONE);
          }  
      }

       

       
       
       ImageView imageStatus = (ImageView) view.findViewById(R.id.simStatus);
       
       if(imageStatus != null) {
    	   
    	   if(mNeedStatus == true) {
        	   int res = GeminiUtils.getStatusResource(mStatus);
        	   
        	   if(res == -1) {
        		   imageStatus.setVisibility(View.GONE);
        	   } else {
            	   imageStatus.setImageResource(res);   		   
        	   }
    	   } else {
    		   imageStatus.setVisibility(View.GONE);
    	   }
    	   


       }
       
       TextView text3G = (TextView) view.findViewById(R.id.sim3g);
       if(text3G != null) {
    	   if((GeminiUtils.mNeed3GText == false)||(mSlotIndex != GeminiUtils.m3GSlotID)){
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
        Xlog.i(TAG, "mUseCheckBox="+mUseCheckBox+" mChecked="+mChecked);
        if (!mUseCheckBox) {
        	Switch ckRadioOn = (Switch)view.findViewById(R.id.Check_Enable);
        	if (ckRadioOn != null) {
            	if(mNeedCheckbox == true) {
					if ("tablet".equals(SystemProperties.get("ro.build.characteristics"))) {
						if(mStatus != 1) {
	                		ckRadioOn.setChecked(mChecked);
						} else {
							ckRadioOn.setChecked(false);
						}
					} else {
						ckRadioOn.setChecked(mChecked);
					}
            	} else {
            		ckRadioOn.setVisibility(View.GONE);
            	}
            }
        } else {
        	CheckBox ckRadioOn = (CheckBox)view.findViewById(R.id.Check_Enable);
        	if (ckRadioOn != null) {
            	if(mNeedCheckbox == true) {
                	ckRadioOn.setChecked(mChecked);	
            	} else {
            		ckRadioOn.setVisibility(View.GONE);
            	}
            }
        }
        
        TextView textNumForShort = (TextView)view.findViewById(R.id.simNum);
        if ((textNum != null) && (mSimNum!=null)) {
        	
            switch (mNumDisplayFormat) {
        	case DISPLAY_NONE: {
        		textNumForShort.setVisibility(View.GONE);
        		break;
        		
        	}
        	case DISPLAY_FIRST_FOUR: {
        		
        		if (mSimNum.length()>=4) {
        			textNumForShort.setText(mSimNum.substring(0, 4));
        		} else {
        			textNumForShort.setText(mSimNum);
        		}
        		break;
        	}
        	case DISPLAY_LAST_FOUR: {
        		
        		if (mSimNum.length()>=4) {
        			textNumForShort.setText(mSimNum.substring(mSimNum.length()-4));
        		} else {
        			textNumForShort.setText(mSimNum);
        		}
        		break;
        	}
        		
        
        }       	
        }

		return view;
	}
    
	void setCheck(boolean bCheck) {
    	mChecked = bCheck;
        notifyChanged();
    }
    
	boolean getCheck() {
		return mChecked;

    }
	
    void setStatus(int status) {
    	mStatus = status;
        notifyChanged();
    }
    
    void setName(String name) {
    	mName = name;
        notifyChanged();
    	
    }
    
    void setColor(int color) {
    	mColor = color;
        notifyChanged();
    }
    
    void setNumDisplayFormat (int format) {
    	mNumDisplayFormat = format;
        notifyChanged();
    }
 
    void setNumber (String number) {
    	mSimNum = number;
        notifyChanged();
    }
    
    public void setNeedCheckBox(boolean isNeed){
        mNeedCheckbox = isNeed;
        notifyChanged();
    }
        


}
