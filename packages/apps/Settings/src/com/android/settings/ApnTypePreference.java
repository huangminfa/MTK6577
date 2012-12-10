package com.android.settings;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.widget.ListView;
import com.mediatek.xlog.Xlog;
public class ApnTypePreference extends DialogPreference
            implements DialogInterface.OnMultiChoiceClickListener{

    private static final int TYPE_NUMBER_NORMAL = 4;
    private static final int TYPE_NUMBER_ORANGE_TETHER_ONLY = 1;
    private static final int TYPE_NUMBER_ORANGE = 5;
    private static final int TYPE_NUMBER_CMCC = 6;
    private int mApnTypeNum = TYPE_NUMBER_NORMAL;

    private static final String TAG = "ApnTypePreference";
    private boolean[] mCheckState ;
    private String[] mApnTypeArray;        

    
    private String mTypeString;
    
    private ListView mListView;
    public ApnTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);              
        if(Utils.opType == Utils.OpIndex.OP_ORANGE){
            mApnTypeNum = TYPE_NUMBER_ORANGE;
            mApnTypeArray = getContext().getResources().getStringArray(R.array.apn_type_orange);
        }else if(Utils.opType == Utils.OpIndex.OP_CMCC){
            mApnTypeNum = TYPE_NUMBER_CMCC;
            mApnTypeArray = getContext().getResources().getStringArray(R.array.apn_type_cmcc);
        }else{
            mApnTypeArray = getContext().getResources().getStringArray(R.array.apn_type_generic);
        }
        mCheckState = new boolean[mApnTypeNum];
    }
    
    public ApnTypePreference(Context context) {
        this(context, null);
    }
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        
        builder.setMultiChoiceItems(mApnTypeArray, mCheckState, this);

        mListView = builder.create().getListView();
    }
    

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            updateRecord();
            callChangeListener(mTypeString);
        } else {
            intCheckState(mTypeString);
        }
    }

    public void setType(String mcc , String mnc, Intent intent){
        if(Utils.isCmccCard(mcc+mnc)){
            mApnTypeNum = TYPE_NUMBER_CMCC;
            mApnTypeArray = getContext().getResources().getStringArray(R.array.apn_type_cmcc);                
            mCheckState = new boolean[mApnTypeNum];
            return;
        }

        if(intent == null){
            return;	
        }

        String apnType = intent.getStringExtra(ApnSettings.APN_TYPE);    	
        boolean isTethering = ApnSettings.TETHER_TYPE.equals(apnType); 

        if(Utils.opType == Utils.OpIndex.OP_ORANGE){
            forOrange(isTethering);
        }
        mCheckState = new boolean[mApnTypeNum];
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        mCheckState[which] = isChecked;
    }

    private void updateRecord() {
        
        if(mListView != null) {
            
            StringBuilder strTemp = new StringBuilder("");
            
            for(int i=0; i<mApnTypeNum; i++) {

                if(mCheckState[i]) {
                    strTemp.append(mApnTypeArray[i]).append(',');
                }
            }
            
            int length = strTemp.length();
            if(length>1){
                mTypeString = strTemp.substring(0, length-1);
            } else {
                mTypeString = "";
            }
            Xlog.i(TAG, "mTypeString is "+mTypeString);

        }
        
    }


    public void intCheckState(String strType) {
        
        Xlog.d(TAG,"init CheckState: " + strType);
        if(strType == null){          
            return;
        }
        
        mTypeString = strType;
        
        for(int i=0; i<mApnTypeNum; i++){
            mCheckState[i]= strType.contains(mApnTypeArray[i]);
        }
    }   
    
    public String getTypeString(){
        return mTypeString;
    }

    private void forOrange(boolean isTether){
        if(isTether){
            mApnTypeNum = TYPE_NUMBER_ORANGE_TETHER_ONLY;
            mApnTypeArray = getContext().getResources().getStringArray(R.array.apn_type_orange_tethering_only);
        }else{
            mApnTypeNum = TYPE_NUMBER_NORMAL;
            mApnTypeArray = getContext().getResources().getStringArray(R.array.apn_type_generic);
        }
    }
}
