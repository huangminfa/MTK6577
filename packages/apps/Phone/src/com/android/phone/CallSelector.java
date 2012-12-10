package com.android.phone;

import java.util.List;

import android.accounts.Account;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.provider.Telephony;
import com.android.internal.telephony.Call;

public class CallSelector extends BaseAdapter {
    List<Call> mItems;
    Context mContext;
    private String operatorNameFirstCall = null;
    private String operatorNameSecondCall = null;
    private String firstCallerInfoName = null;
    private String secondCallerInfoName = null;

    public CallSelector(Context context, List<Call> items) {
        mContext = context;
        mItems = items;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }
    
    public Object getItem(int position) {
        return mItems.get(position);
    }
    
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.call_select_list_item, null);
            holder.mDisplayName = (TextView)view.findViewById(R.id.displayName);
            holder.mOperator = (TextView)view.findViewById(R.id.operator);
            holder.mPhoneNumber = (TextView)view.findViewById(R.id.phoneNumber);
            holder.mCallStatus = (TextView)view.findViewById(R.id.callStatus);
            view.setTag(holder);
        }
        
        holder = (ViewHolder)view.getTag();
        Call call = mItems.get(position);

        String callStatus = "";
        if (call.getState() == Call.State.ACTIVE)
            callStatus = "Active";
        else if (call.getState() == Call.State.HOLDING)
            callStatus = "Hold";
        String operatorName = getOperatorName(position);
        String displayName = getCallerInfoName(position);
        String address = call.getLatestConnection().getAddress();

        if (displayName != null){
            holder.mDisplayName.setText(displayName);
        }else{
            holder.mDisplayName.setText("");
        }

        if (operatorName != null){
            holder.mOperator.setText(operatorName);
        }else{
            holder.mOperator.setText("");
        }
        if(address.equals(displayName)){
            holder.mPhoneNumber.setText("");
        }else{
            holder.mPhoneNumber.setText(address);
        }

        holder.mCallStatus.setText(callStatus);

        return view;
    }

    public void setOperatorName(String operator1,String operator2){
        operatorNameFirstCall = operator1;
        operatorNameSecondCall = operator2;
    }

    public String getOperatorName(int position){
        if (position == 0){
            return operatorNameFirstCall;
        }else{
            return operatorNameSecondCall;
        }
    }

    public void setCallerInfoName(String callerName1,String callerName2){
        firstCallerInfoName = callerName1;
        secondCallerInfoName = callerName2;
    }

    public String getCallerInfoName(int position){
        if (position == 0){
            return firstCallerInfoName;
        }else{
            return secondCallerInfoName;
        }
    }

    private class ViewHolder {
        TextView  mDisplayName;
        TextView mOperator;
        TextView  mPhoneNumber;
        TextView  mCallStatus;
    }
}