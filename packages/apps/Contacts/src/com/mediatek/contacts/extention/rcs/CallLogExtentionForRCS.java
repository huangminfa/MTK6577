
package com.mediatek.contacts.extention.rcs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.mediatek.pluginmanager.Plugin;
import com.mediatek.pluginmanager.PluginManager;
import com.mediatek.pluginmanager.Plugin.ObjectCreationException;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.contacts.calllog.CallLogListItemView;
import com.mediatek.contacts.extention.CallLogExtention;
import com.mediatek.contacts.extention.ICallLogExtention;
import com.mediatek.contacts.extention.ICallLogExtention.Action;
import com.android.contacts.ContactsApplication;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.R;
import com.android.contacts.calllog.CallDetailHistoryAdapter;
import com.android.contacts.calllog.CallTypeHelper;


public class CallLogExtentionForRCS extends CallLogExtention {

    private static final String TAG = "CallLogExtentionForRCS";
    private static ICallLogExtention mCallLogPlugin;
    private Action [] RCSActions;
    public static final String RCS_DISPLAY_NAME = "rcs_display_name";
    public static final String RCS_PHONE_NUMBER = "rcs_phone_number";
    private Activity mActivity;
    private ImageView mRCSIcon;
    private  int mRCSIconViewWidth;
    private  int mRCSIconViewHeight;
    private boolean mRCSIconViewWidthAndHeightAreReady = false;
    
    
    public CallLogExtentionForRCS() {

    }

    public int getLayoutResID() {
        return -1;
    }

    public void setEXtenstionItem(Activity activity, Uri contactUri, PhoneCallDetails firstDetails) {
        String Number = null;
        String displayName= null;
        String chat= null;
        Drawable RCSicon= null;
        Drawable RCSActionIcon= null;
        mActivity = activity;
        // if it has im and file transfer function the values is true.
        boolean hasIM = false;
        boolean hasFT = false;
        boolean isEnable = false;
        if (firstDetails != null) {
            Number = firstDetails.number.toString();
            if (null != firstDetails.name) {
                displayName = firstDetails.name.toString();
            } else {
                Log.i(TAG, "[setEXtenstionItem] name is null");
            }
        } else {
            Log.e(TAG,"[setEXtenstionItem]firstDetails is null");
        }
        Log.i(TAG,"[setEXtenstionItem] Number = "+Number+" | contactUri : "+contactUri+" | firstDetails : "+firstDetails+" | displayName : "+displayName);
        
        if (mCallLogPlugin != null) {
            chat = mCallLogPlugin.getChatString();
            RCSicon = mCallLogPlugin.getContactPresence(Number);
            isEnable = mCallLogPlugin.isEnabled();
            RCSActions = mCallLogPlugin.getContactActions(Number);
            if (RCSActions[0] != null && RCSActions[1] != null){
                if (null != RCSActions[0].intentAction) {
                    hasIM = true;
                }
                if (null != RCSActions[1].intentAction) {
                    hasFT = true;
                }
            } else {
                Log.e(TAG,"[setEXtenstionItem] RCSActions is null");
            }
            Log.i(TAG, "[setEXtenstionItem] RCSicon : " + (RCSicon != null) + " | isEnable : "
                    + isEnable+" | hasIM , hasFT : "+hasIM+" , "+hasFT);
        } else {
            Log.e(TAG, "[setEXtenstionItem]mCallLogPlugin is null");
        }
        if(RCSActions[1] != null){
            RCSActionIcon = RCSActions[1].icon;
        } else {
            Log.e(TAG,"[setEXtenstionItem] RCSActions[1] is null");
        }
        // add consider the number is support rcs
        boolean result = ((RCSicon != null) && isEnable);
//        boolean result = true;
        View RCSContainer = activity.findViewById(R.id.RCS_container);
        View separator03 = activity.findViewById(R.id.separator03);
        View convertView3 = activity.findViewById(R.id.RCS);
        View RCSACtion = convertView3.findViewById(R.id.RCS_action);


        String RCSTextVaule = chat+" "+Number;
        Log.i(TAG,"[setEXtenstionItem] chat = "+chat+" | RCSTextVaule : "+RCSTextVaule);
        if (!hasIM){
            RCSTextVaule = Number;
        }
        RCSACtion.setTag(firstDetails);
        TextView RCSText = (TextView) convertView3.findViewById(R.id.RCS_text);
        RCSText.setText(RCSTextVaule);
        ImageView icon = (ImageView) convertView3.findViewById(R.id.RCS_icon);
        icon.setOnClickListener(mRCSTransforActionListener);
        icon.setTag(firstDetails);
        View divider = convertView3.findViewById(R.id.RCS_divider);
        RCSACtion.setOnClickListener(mRCSTextActionListener);
        icon.setImageDrawable(RCSActionIcon);
    
        RCSContainer.setVisibility(result ? View.VISIBLE : View.GONE);
        icon.setVisibility(result ? View.VISIBLE : View.GONE);
        divider.setVisibility(result ? View.VISIBLE : View.GONE);
        separator03.setVisibility(result ? View.VISIBLE : View.GONE);
        RCSACtion.setVisibility(result ? View.VISIBLE : View.GONE);
        if (hasIM && !hasFT) {
            icon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else if (!hasIM && hasFT) {
            RCSACtion.setClickable(false);
        } else if (!hasIM && !hasFT){
            RCSContainer.setVisibility(View.GONE);
            icon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            separator03.setVisibility(View.GONE);
            RCSACtion.setVisibility(View.GONE);
        }
    }

    private final View.OnClickListener mRCSTextActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PhoneCallDetails details = (PhoneCallDetails) view.getTag();
            Intent intent = new Intent();
            String name = null;
            String number = null;
            if (RCSActions[0] != null) {
                intent = RCSActions[0].intentAction;
                Log.i(TAG, "[mRCSTextActionListener] intent : " + intent);
            } else {
                Log.e(TAG, "[mRCSTextActionListener] RCSActions[0] is null");
            }
            if(details != null){
                if (null != details.name) {
                    name = details.name.toString();
                }
                number = details.number.toString();
                Log.i(TAG, "[mRCSTextActionListener] name : "+name+" | number : "+number);
                if (TextUtils.isEmpty(name)) {
                    name = number;
                }
                intent.putExtra(RCS_DISPLAY_NAME, name);
                intent.putExtra(RCS_PHONE_NUMBER, number);
            } else {
                Log.e(TAG,"[mRCSTextActionListener] details is null");
            }
            mActivity.startActivity(intent);
            
        }
    };
    
    private final View.OnClickListener mRCSTransforActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PhoneCallDetails details = (PhoneCallDetails) view.getTag();
            Intent intent = new Intent();
            String name = null;
            String number = null;
            if (RCSActions[1] != null) {
                intent = RCSActions[1].intentAction;
                Log.i(TAG, "[mRCSTransforActionListener] intent : " + intent);
            } else {
                Log.e(TAG, "[mRCSTransforActionListener] RCSActions[1] is null");
            }
            if(details != null){
                if (null != details.name) {
                    name = details.name.toString();
                }
                number = details.number.toString();
                Log.i(TAG, "[mRCSTransforActionListener] name : "+name+" | number : "+number);
                if (TextUtils.isEmpty(name)) {
                    name = number;
                }
                intent.putExtra(RCS_DISPLAY_NAME, name);
                intent.putExtra(RCS_PHONE_NUMBER, number);
            } else {
                Log.e(TAG,"[mRCSTransforActionListener] details is null");
            }
            mActivity.startActivity(intent);
        }
    };

    public static boolean isSupport() {
        PluginManager<ICallLogExtention> pm = PluginManager.<ICallLogExtention> create(
                ContactsApplication.getInstance(), ICallLogExtention.class.getName());
        Plugin<ICallLogExtention> callLogPlugin = null;
        int i = pm.getPluginCount();
        if (i == 0){
            Log.e(TAG,"no plugin apk");
            return false;
        }
        callLogPlugin = pm.getPlugin(0);
        if (callLogPlugin != null) {
            try{
            mCallLogPlugin = callLogPlugin.createObject();
            } catch (Exception e){
                Log.e(TAG,"error get object");
            }
        } else {
            Log.e(TAG, "callLogPlugin is null");
        }
        if (mCallLogPlugin != null) {
            return true;
        } else {
            Log.e(TAG, "mCallLogPlugin is null");
            return false;
        }
//        return true;

    }

    public CallDetailHistoryAdapter getCallDetailHistoryAdapter(Context context,
            LayoutInflater layoutInflater, CallTypeHelper callTypeHelper,
            PhoneCallDetails[] phoneCallDetails, boolean showVoicemail, boolean showCallAndSms,
            View controls,String mNumber) {
        return new CallDetailHistoryAdapterForRCS(context, layoutInflater, callTypeHelper, phoneCallDetails,
                showVoicemail, showCallAndSms, controls,mNumber);
    }

    public void disableCallButton(Activity activity) {
        activity.findViewById(R.id.call_and_sms).setVisibility(View.GONE);
        activity.findViewById(R.id.separator01).setVisibility(View.GONE);
        activity.findViewById(R.id.separator02).setVisibility(View.GONE);
        activity.findViewById(R.id.video_call).setVisibility(View.GONE);
        activity.findViewById(R.id.ip_call).setVisibility(View.GONE);
        activity.findViewById(R.id.RCS).setVisibility(View.GONE);
        activity.findViewById(R.id.separator03).setVisibility(View.GONE);
    }
    
    public static ICallLogExtention getCallLogPlugin(){
        return mCallLogPlugin;
    }
    
    public void setExtentionIcon(CallLogListItemView itemView, PhoneCallDetails details) {
        itemView.removeExtentionIconView();
        if (mCallLogPlugin != null && details != null) {
            Drawable a = mCallLogPlugin.getContactPresence(details.number.toString());
            boolean isEnabled = mCallLogPlugin.isEnabled();
            Log.i(TAG, "[setExtentionIcon] isEnabled : " + isEnabled);
            if ((a != null) && isEnabled) {
                itemView.setExtentionIcon(true);
            } else {
                Log.i(TAG,"[setExtentionIcon] details.contactUri : "+details.contactUri+" | a : "+a+" |isEnabled : "+isEnabled);
                itemView.setExtentionIcon(false);
            }
        } else {
            Log.e(TAG,"[setExtentionIcon] mCallLogPlugin : "+mCallLogPlugin+" | details : "+details+" | itemView : "+itemView);
        }

    }
    
    public void measureExtention(ImageView mRCSIcon) {
        if (isVisible(mRCSIcon)) {
            if (!mRCSIconViewWidthAndHeightAreReady) {
                if (mCallLogPlugin != null) {
                    Drawable a = mCallLogPlugin.getAppIcon();
                    if (a != null) {
                        mRCSIconViewWidth = a.getIntrinsicWidth();
                        mRCSIconViewHeight = a.getIntrinsicHeight();
                    } else {
                        mRCSIconViewWidth = 0;
                        mRCSIconViewHeight = 0;
                    }
                } else {
                    mRCSIconViewWidth = 0;
                    mRCSIconViewHeight = 0;
                }
                Log.i(TAG, "measureExtention mRCSIconViewWidth : " + mRCSIconViewWidth
                        + " | mRCSIconViewHeight : " + mRCSIconViewHeight);
                mRCSIconViewWidthAndHeightAreReady = true;
            }
        }
    }
    
    protected boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }
    
    public int layoutRCSIcon(int leftBound, int topBound, int bottomBound, int rightBound, int mGapBetweenImageAndText,ImageView mExtentionIcon) {
        if (this.isVisible(mExtentionIcon) && mExtentionIcon != null) {
            int photoTop1 = topBound + (bottomBound - topBound - mRCSIconViewHeight) / 2;
            mExtentionIcon.layout(rightBound - (mRCSIconViewWidth), photoTop1, rightBound, photoTop1
                    + mRCSIconViewHeight);
            rightBound -= (mRCSIconViewWidth + mGapBetweenImageAndText);
        }
       return rightBound;
        
    }
    
//    public void setRCSIcon(boolean enable) {
//        if (enable) {
//            if (mRCSIcon == null) {
//                getRCSIcon();
//            }
//            mRCSIcon.setVisibility(View.VISIBLE);
//        } else {
//            if (mRCSIcon != null) {
//                mRCSIcon.setVisibility(View.GONE);
//            }
//        }
//    }
//    
//
//    public ImageView getRCSIcon() {
//
//        if (mRCSIcon == null) {
//            mRCSIcon = new ImageView(mContext);
//        }
//        mRCSIcon.setBackgroundDrawable(null);
//        Drawable icon = null;
//        boolean result = mCallLogExtention.isSupport();
//        if (result) {
//            if (null != mCallLogExtention.getCallLogPlugin()) {
//                icon = mCallLogExtention.getCallLogPlugin().getAppIcon();
//
//            }
//        }
//
//        mRCSIcon.setImageDrawable(icon);
//        addView(mRCSIcon);
//        return mRCSIcon;
//
//    }

}
