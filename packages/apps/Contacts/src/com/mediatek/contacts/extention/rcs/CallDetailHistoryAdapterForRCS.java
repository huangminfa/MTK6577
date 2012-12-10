package com.mediatek.contacts.extention.rcs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.CallLog.Calls;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.contacts.R;

import com.android.contacts.PhoneCallDetails;
import com.android.contacts.calllog.CallDetailHistoryAdapter;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.CallTypeIconsView;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.extention.CallLogExtentionManager;
import com.mediatek.contacts.util.OperatorUtils;

public class CallDetailHistoryAdapterForRCS extends CallDetailHistoryAdapter {

    private boolean mShowRCS = false;
    private static final String TAG = "CallDetailHistoryAdapterForRCS";
    private String mNumber;

    public CallDetailHistoryAdapterForRCS(Context context, LayoutInflater layoutInflater,
            CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails,
            boolean showVoicemail, boolean showCallAndSms, View controls, String number) {
        super(context, layoutInflater, callTypeHelper, phoneCallDetails, showVoicemail,
                showCallAndSms, controls);
        mNumber = number;
        // TODO Auto-generated constructor stub
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View header = super.getView(position, convertView, parent);
        if (position == 0) {

            // RCS
            boolean isEnabledRCS = false;
            if (null != CallLogExtentionForRCS.getCallLogPlugin()) {
                isEnabledRCS = CallLogExtentionForRCS.getCallLogPlugin().isEnabled();
                Drawable a = CallLogExtentionForRCS.getCallLogPlugin().getContactPresence(mNumber);
                mShowRCS = (a != null);
                Log.i(TAG, "isEnabledRCS : " + isEnabledRCS + " | mShowRCS : " + mShowRCS);
            } else {
                Log.e(TAG, "CallLogPlugin is null");
            }
            View RCSContainer = header.findViewById(R.id.header_RCS_container);
            RCSContainer.setVisibility((mShowRCS && isEnabledRCS) ? View.VISIBLE : View.GONE);
            View separator03 = header.findViewById(R.id.separator03);
            separator03.setVisibility((mShowRCS && isEnabledRCS) ? View.VISIBLE : View.GONE);
            // RCS
            
            return header;

        }

        return header;

    }

}
