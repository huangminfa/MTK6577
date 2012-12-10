package com.mediatek.contacts.extention;

import com.android.contacts.PhoneCallDetails;
import com.android.contacts.calllog.CallDetailHistoryAdapter;
import com.android.contacts.calllog.CallTypeHelper;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.contacts.R;
import com.mediatek.contacts.calllog.CallLogListItemView;

public class CallLogExtention {
    
    public int getLayoutResID() {
        return -1;
    }
    
    
    
    public CallDetailHistoryAdapter getCallDetailHistoryAdapter(Context context,
            LayoutInflater layoutInflater, CallTypeHelper callTypeHelper,
            PhoneCallDetails[] phoneCallDetails, boolean showVoicemail, boolean showCallAndSms,
            View controls, String mNumber){
        return null; 
    }
    
    public void setEXtenstionItem(Activity activity,Uri contactUri, PhoneCallDetails firstDetails) {
        
    }
    
    public void disableCallButton(Activity activity){
        activity.findViewById(R.id.call_and_sms).setVisibility(View.GONE);
        activity.findViewById(R.id.separator01).setVisibility(View.GONE);
        activity.findViewById(R.id.separator02).setVisibility(View.GONE);
        activity.findViewById(R.id.video_call).setVisibility(View.GONE);
        activity.findViewById(R.id.ip_call).setVisibility(View.GONE);
    }



    public void setExtentionIcon(CallLogListItemView itemView, PhoneCallDetails details) {
        // TODO Auto-generated method stub
        
    }



    public void measureExtention(ImageView mExtentionIcon) {
        // TODO Auto-generated method stub
        
    }



    public int layoutRCSIcon(int leftBound, int topBound, int bottomBound, int rightBound, int mGapBetweenImageAndText, ImageView mExtentionIcon) {
        // TODO Auto-generated method stub
        return rightBound;
    }
    


}
