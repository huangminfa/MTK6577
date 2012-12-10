package com.android.settings.gemini;

import com.android.settings.R;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GeminiSIMTetherAdapter extends BaseAdapter{
	private static final String TAG = "GeminiSIMTetherAdapter";
	public static final int FLAG_SIM_STATUS_1 = 1;
	public static final int FLAG_SIM_STATUS_2 = 2;
	public static final int FLAG_CHECKBOX_STSTUS_NONE = -1;
	public static final int FLAG_CHECKBOX_STSTUS_UNCHECKED = 0;
	public static final int FLAG_CHECKBOX_STSTUS_CHECKED = 1;
	
	public static final int FLAG_CHECKBOX_MESSAGE = 1;
	
	private static final int BGCOLOR_SIM_ABSENT = 10;
	
	private ArrayList<GeminiSIMTetherItem> mDataList;
	private LayoutInflater mInflater;
	private Context mContext;
	private Handler mClickHandler;
	private GeminiSIMTetherItem item;
	
	public GeminiSIMTetherAdapter(Context context, ArrayList<GeminiSIMTetherItem> data){
		mContext = context;
		this.mDataList = data;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public GeminiSIMTetherAdapter(Context context, ArrayList<GeminiSIMTetherItem> data, Handler handler){
		mContext = context;
		this.mDataList = data;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mClickHandler = handler;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList != null? mDataList.size():0;
	}

	@Override
	public Object getItem(int position) {
		return mDataList != null? mDataList.get(position):null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder;
		String simName="";
		int simColor=-1;
		if(convertView == null){
			holder = new ItemViewHolder();
			convertView = mInflater.inflate(R.layout.gemini_sim_tether_info_item, null);
			
			holder.name = (TextView)convertView.findViewById(R.id.gemini_contact_name);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.gemini_contact_check_btn);
			holder.phoneNum = (TextView)convertView.findViewById(R.id.gemini_contact_phone_num);
			holder.simInfoLayout = (LinearLayout)convertView.findViewById(R.id.gemini_contact_sim_status_layout);
			holder.simInfo = (TextView)convertView.findViewById(R.id.gemini_contact_sim_status);
			holder.phoneNumType = (TextView)convertView.findViewById(R.id.gemini_contact_phone_num_type);
			
			convertView.setTag(holder);
		}else{
			holder = (ItemViewHolder)convertView.getTag();
		}
		item = mDataList.get(position);
		simName = mDataList.get(position).getSimName();
		simColor = mDataList.get(position).getSimColor();
		if(item != null && holder != null){
			holder.name.setText(item.getName());
			holder.phoneNumType.setText(item.getPhoneNumType());
			holder.phoneNum.setText(item.getPhoneNum());
			if(simName==null || simName.equals("")){
			    holder.simInfo.setVisibility(View.GONE);
			    holder.simInfoLayout.setVisibility(View.GONE);
			}else{
			    holder.simInfoLayout.setVisibility(View.VISIBLE);
			    holder.simInfo.setVisibility(View.VISIBLE);
			    holder.simInfo.setText(simName);
			}
			
			//set check box status, visible/invisible, checked/unchecked
			int checkStatus = mDataList.get(position).getCheckedStatus();
			
			if(checkStatus == FLAG_CHECKBOX_STSTUS_CHECKED || checkStatus == FLAG_CHECKBOX_STSTUS_UNCHECKED){
			    holder.checkBox.setVisibility(View.VISIBLE);
			    holder.checkBox.setChecked(checkStatus == FLAG_CHECKBOX_STSTUS_CHECKED ? true:false);
//			    final int tempPoz = position;
//			    holder.checkBox.setOnClickListener(new OnClickListener(){
//			        public void onClick(View v) {
//			            // TODO Auto-generated method stub
//			            Message msg = new Message();
//			            msg.what = FLAG_CHECKBOX_MESSAGE;
//			            boolean isChecked = ((CheckBox)v).isChecked();
//			            int checkBoxNewState = isChecked ? FLAG_CHECKBOX_STSTUS_CHECKED:FLAG_CHECKBOX_STSTUS_UNCHECKED;
//			            msg.arg1 = tempPoz;
//			            msg.arg2 = checkBoxNewState;
//			            mDataList.get(tempPoz).setCheckedStatus(checkBoxNewState);
//			            Log.i(TAG, "check box at "+tempPoz+" is clicked");
//			            if(mClickHandler != null){
//			                mClickHandler.sendMessage(msg);
//			            }
//			        }
//			    });
			}else{
			    holder.checkBox.setVisibility(View.GONE);
			}
			
			//set SIM card status background
			if(simColor>=0 && simColor<=7){
			    holder.simInfo.setBackgroundResource(Telephony.SIMBackgroundRes[simColor]);
			}else if(simColor == BGCOLOR_SIM_ABSENT){
			    holder.simInfo.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_locked);
			}
		}
		return convertView;
	}
	
	static class ItemViewHolder{
		GeminiSIMTetherItem item;
		TextView name;
		TextView phoneNumType;
		TextView phoneNum;
		LinearLayout simInfoLayout;
		TextView simInfo;
		CheckBox checkBox;
	}
}
