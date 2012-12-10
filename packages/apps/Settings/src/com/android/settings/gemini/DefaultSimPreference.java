package com.android.settings.gemini;

import java.util.HashMap;
import java.util.List;


import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.preference.DialogPreference;
import android.provider.Telephony.SIMInfo;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView;

import android.text.TextUtils;

import com.android.internal.telephony.Phone;

import com.mediatek.CellConnService.CellConnMgr;

import com.android.settings.R;

import com.mediatek.xlog.Xlog;


public class DefaultSimPreference extends DialogPreference 
			implements AdapterView.OnItemClickListener {
	
    final static String TAG = "DefaultSimPreference";
	private LayoutInflater mFlater;
    private String mValue;

	private List<SimItem> mSimItemList;
	private SelectionListAdapter mAdapter;
	private ListView mListView;
	private int mSelected = -1;
	private int mInitValue = -1;
    private Drawable mIcon;
    private Context mContext;
    
    private CellConnMgr mCellConnMgr;
    
    private int mType = -1;

    private static final String optr = SystemProperties.get("ro.operator.optr");
    
	private static final int DISPLAY_NONE = 0;
	private static final int DISPLAY_FIRST_FOUR = 1;
	private static final int DISPLAY_LAST_FOUR = 2;	
	
    private static final int PIN1_REQUEST_CODE = 302;
	


    public DefaultSimPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }   
    public DefaultSimPreference(Context context,AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mFlater = LayoutInflater.from(context);	

    }
    
    void SetCellConnMgr(CellConnMgr cellConnmgr) {
    	mCellConnMgr = cellConnmgr;
    	
    }
    void SetData (List<SimItem> SimItemList) {
    	mSimItemList = SimItemList;
    	if(getDialog()!=null) {
    		if (mListView != null) {
				mAdapter = new SelectionListAdapter(mSimItemList);    
				mListView.setAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
    		}
    	}
    }
    
    void setType(int type) {
    	mType = type;
    }
    @Override
    public void onBindView(View view) {

        super.onBindView(view);
       
        TextView textSummary = (TextView) view.findViewById(android.R.id.summary);
        
        if(textSummary != null) {
        	textSummary.setSingleLine();
        	textSummary.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
        
        Xlog.i(TAG, "summary is +"+this.getSummary());
      

    }


	@Override
     protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        
        mAdapter = new SelectionListAdapter(mSimItemList);      
        mListView = new ListView(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        mListView.setItemsCanFocus(false);
//        mListView.setCacheColorHint(0);


//        builder.setInverseBackgroundForced(true);
        builder.setView(mListView,0,0,0,0);

        builder.setNegativeButton(android.R.string.cancel, null);
        

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        Xlog.i(TAG, "mSelected = "+mSelected);
        Xlog.i(TAG, "mInitValue = "+mInitValue);    
        if (positiveResult && mSelected >= 0 &&(mSelected != mInitValue)) {
        	
        	Xlog.i(TAG, "callChangeListener");
            long value = mSimItemList.get(mSelected).mSimID;
            callChangeListener(value);
            mInitValue = mSelected;
        }
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		Xlog.i(TAG,"positon is "+position);
//    	View childView = parent.getChildAt(position);
    	
    	if(v != null) {
    		if(v.isEnabled()==false) {
    			return;
    		} else {
    			
    			SimItem simItem = mSimItemList.get(position);
    			if((mType == GeminiUtils.TYPE_GPRS)&&(simItem.mIsSim == true)&&(mCellConnMgr != null)&&(simItem.mState == Phone.SIM_INDICATOR_LOCKED)){
    				mCellConnMgr.handleCellConn(simItem.mSlot, PIN1_REQUEST_CODE);
    			} else {
        	    	mSelected = position;
//        	    	SetRadioCheched(position);
        	    	onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        	    	getDialog().dismiss();  
    			}

    		}
    	}
    	
    }
    
	class SelectionListAdapter extends BaseAdapter {
		
		List<SimItem> mSimItemList;
		

		
		public SelectionListAdapter(List<SimItem> simItemList) {
			mSimItemList = simItemList;
		}
		
		public int getCount() {
			return mSimItemList.size();
		}

		public Object getItem(int position) {
			return mSimItemList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null) {
                convertView=mFlater.inflate(R.layout.preference_sim_default_select, null);
                holder=new ViewHolder();
                setViewHolderId(holder,convertView);
                convertView.setTag(holder);
            }
            else {
                holder=(ViewHolder)convertView.getTag();
            }
            SimItem simItem = (SimItem)getItem(position);
            setNameAndNum(holder.textName,holder.textNum,simItem);
            setText3G(holder.text3G,simItem);
            setImageSim(holder.imageSim,simItem);
            setImageStatus(holder.imageStatus,simItem);
            setTextNumFormat(holder.textNumFormat,simItem);
            holder.ckRadioOn.setChecked(mSelected == position);
            if((simItem.mState == Phone.SIM_INDICATOR_RADIOOFF)
                        ||((mType == GeminiUtils.TYPE_SMS)&&(getCount() == 2)&&(simItem.mSimID == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK))
                        ||((mType == GeminiUtils.TYPE_VOICECALL)&&(getCount() == 2 || getCount() == 1)&&(simItem.mSimID == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK))) {
                    convertView.setEnabled(false);
                    holder.textName.setEnabled(false);
                    holder.textNum.setEnabled(false);
                    holder.ckRadioOn.setEnabled(false);
                }
            else {
                    convertView.setEnabled(true);
                    holder.textName.setEnabled(true);
                    holder.textNum.setEnabled(true);
                    holder.ckRadioOn.setEnabled(true);
                    }
                    
            return convertView;
          }
		private void setTextNumFormat(TextView textNumFormat, SimItem simItem) {
		    if(simItem.mIsSim == true) {
		        if (simItem.mNumber!=null) {
		            switch (simItem.mDispalyNumberFormat) {
		            case DISPLAY_NONE: 
		                textNumFormat.setVisibility(View.GONE);
		                break;
		            case DISPLAY_FIRST_FOUR:
		                textNumFormat.setVisibility(View.VISIBLE);
                        if (simItem.mNumber.length()>=4) {
                            textNumFormat.setText(simItem.mNumber.substring(0, 4));
                        } else {
                            textNumFormat.setText(simItem.mNumber);
                        }
                        break;
                    case DISPLAY_LAST_FOUR:
                        textNumFormat.setVisibility(View.VISIBLE);
                        if (simItem.mNumber.length()>=4) {
                            textNumFormat.setText(simItem.mNumber.substring(simItem.mNumber.length()-4));
                        } else {
                            textNumFormat.setText(simItem.mNumber);
                        }
                        break;
		            }           
                } else {
                    Xlog.d(TAG,"simItem.mNumber="+simItem.mNumber);
                    textNumFormat.setVisibility(View.GONE);
                }
		    } else {
		        textNumFormat.setVisibility(View.GONE);
		    }
            
        }
        private void setImageStatus(ImageView imageStatus, SimItem simItem) {
            if(simItem.mIsSim==true){
                int res = GeminiUtils.getStatusResource(simItem.mState);
                if(res == -1) {
                    imageStatus.setVisibility(View.GONE);
                }
                else {
                    imageStatus.setVisibility(View.VISIBLE);
                    imageStatus.setImageResource(res);
                }
            } else {
                imageStatus.setVisibility(View.GONE);   
            }
    		    
        }
        private void setImageSim(RelativeLayout imageSim, SimItem simItem) {
		    if(simItem.mIsSim == true) {
		        int resColor = GeminiUtils.getSimColorResource(simItem.mColor);
                if(resColor>=0) {
                    imageSim.setVisibility(View.VISIBLE);
                    imageSim.setBackgroundResource(resColor);
                }
		    }
		    else if (simItem.mColor == 8){
		        imageSim.setVisibility(View.VISIBLE);
                imageSim.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
		    }
		    else {
		        imageSim.setVisibility(View.GONE);
		    }
        }

        private void setText3G(TextView text3G, SimItem simItem) {
		    if((GeminiUtils.mNeed3GText == false)||(simItem.mSlot != GeminiUtils.m3GSlotID) || simItem.mColor == 8){
		        text3G.setVisibility(View.GONE);
		        }
		    else
		    {
		        text3G.setVisibility(View.VISIBLE);
		    }
        }

        private void setViewHolderId(ViewHolder holder, View convertView) {
		    holder.textName=(TextView)convertView.findViewById(R.id.simNameSel);
            holder.textNum=(TextView)convertView.findViewById(R.id.simNumSel);
            holder.imageStatus=(ImageView)convertView.findViewById(R.id.simStatusSel);
            holder.textNumFormat=(TextView)convertView.findViewById(R.id.simNumFormatSel);
            holder.text3G=(TextView)convertView.findViewById(R.id.sim3gSel);
            holder.ckRadioOn=(RadioButton)convertView.findViewById(R.id.Enable_select);
            holder.imageSim=(RelativeLayout)convertView.findViewById(R.id.simIconSel);
        }

        private void setNameAndNum(TextView textName,TextView textNum, SimItem simItem) {
		    if(simItem.mName != null) {
		        textName.setVisibility(View.VISIBLE);
		        textName.setText(simItem.mName);
		    }
            else
                textName.setVisibility(View.GONE);
		    
            if((simItem.mIsSim == true) &&((simItem.mNumber != null)&&(simItem.mNumber.length() != 0))) {
                textNum.setVisibility(View.VISIBLE);
                textNum.setText(simItem.mNumber);
            }
            else
                textNum.setVisibility(View.GONE);
        }
        class ViewHolder{
		    TextView textName;
		    TextView textNum;
		    RelativeLayout imageSim;
		    ImageView imageStatus;
		    TextView textNumFormat;
		    TextView text3G;
		    RadioButton ckRadioOn;
		    
		}
	}
	
    void SetRadioCheched(int index) {
    	int listSize = mListView.getCount();
    	
    	for (int k=0; k<listSize; k++) {
    		
        	View ItemView = mListView.getChildAt(k);
    		RadioButton btn = (RadioButton)ItemView.findViewById(R.id.Enable_select);
    		if(btn!=null){
    			btn.setChecked((k == index)?true:false);
    		}
		
    	}
    }
    
    void setInitValue(int value) {
    	
    	mInitValue = value;
    	mSelected = value;
    	
    }
    
    long getValue() {
    	return  mSimItemList.get(mSelected).mSimID;
    }
    
    void setInitData(List<SimItem> SimItemList) {
    	mSimItemList = SimItemList;
    	if(mAdapter!=null)
    	    mAdapter.notifyDataSetChanged();

    }
    
    private void updateData() {
    	
    	int location=0;
    	for (SimItem simitem: mSimItemList) {
    		
    		
    		if (simitem.mIsSim == true) {
    			SIMInfo siminfo = SIMInfo.getSIMInfoById(mContext, simitem.mSimID);
    			
    			if(siminfo != null) {
        			SimItem simitemCopy = new SimItem(siminfo);
        			mSimItemList.set(location, simitemCopy);	
    			}
    			
    		}
    		
    		location++;
    	}
    }
    
}
