package com.mediatek.contacts.extention.rcs;

import java.util.HashMap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.pluginmanager.Plugin;
import com.mediatek.pluginmanager.PluginManager;
import com.mediatek.pluginmanager.Plugin.ObjectCreationException;
import com.mediatek.contacts.extention.ContactExtention;
import com.mediatek.contacts.extention.ContactExtentionManager;
import com.mediatek.contacts.extention.IContactExtention;
import com.mediatek.contacts.extention.IContactExtention.Action;
import com.mediatek.contacts.extention.IContactExtention.OnPresenceChangedListener;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.ContactTileAdapter.ContactEntry;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.BaseAccountType.SimpleInflater;

public class ContactExtentionForRCS<mRCSSecondonClickListener> extends ContactExtention {
    
    private static final String TAG = "ContactExtentionForRCS";
    private static IContactExtention mContactPlugin;
    public static final String RCS_DISPLAY_NAME = "rcs_display_name";
    public static final String RCS_PHONE_NUMBER = "rcs_phone_number";
    private  int mRCSIconViewWidth;
    private  int mRCSIconViewHeight;
    private boolean mRCSIconViewWidthAndHeightAreReady = false;
    private String mNumber;
    private String mName;
    private Activity mActivity;
    private View mDetailView;
    private int mIMValue;
    private int mFTValue;
    public ContactExtentionForRCS(){
        
    }
    public int getLayoutResID() {
        return -1;
    }
    public static boolean isSupport(){
        PluginManager<IContactExtention> pm = PluginManager.<IContactExtention> create(
                ContactsApplication.getInstance(), IContactExtention.class.getName());
        Plugin<IContactExtention> contactPlugin = null;
        int i = pm.getPluginCount();
        if (i == 0){
            Log.e(TAG,"no plugin apk");
            return false;
        }
        contactPlugin = pm.getPlugin(0);
        if (contactPlugin != null) {
            try{
            mContactPlugin = contactPlugin.createObject();
            } catch(Exception e){
                Log.e(TAG,"error in get object");
            }
        } else {
            Log.e(TAG, "contactPlugin is null");
        }
        if (mContactPlugin != null) {
            return true;
        } else {
            Log.e(TAG, "mContactPlugin is null");
            return false;
        }
    }
    
    public static IContactExtention getContactPlugin(){
        return mContactPlugin;
    }
    
    public DataKind getExtentionKind(DataKind kind, String mimeType, Cursor cursor) {
        if (mContactPlugin != null && mContactPlugin.isEnabled()) {
            String newMimeType = mContactPlugin.getMimeType();
            Log.i(TAG, "[getExtentionKind] newMimeType : " + newMimeType);
            if (newMimeType != null && newMimeType.equals(mimeType)) {
                // int Title = mContactPlugin.getAppTitle();
                if (cursor != null) {
                    int i = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
                    mName = cursor.getString(i);
                }
                DataKind newkind = new DataKind(newMimeType, 0, 10, false,
                        R.layout.text_fields_editor_view);
                Log.i(TAG, "[getExtentionKind] newkind : " + newkind + " |Title : " + 0
                        + " | mName : " + mName);
                newkind.actionBody = new SimpleInflater(Data.DATA1);
                newkind.titleRes = 0;
                return newkind;
            } else {
                Log.i(TAG, "[getExtentionKind] retrun kind ");
                return kind;
            }
        } else {
            if (mContactPlugin != null && mimeType != null
                    && mimeType.equals(mContactPlugin.getMimeType())) {
                Log.i(TAG, "[getExtentionKind] rcs is turn off so return null");
                return null;
            }
            Log.e(TAG, "[getExtentionKind] mContactPlugin is null or not enabled mContactPlugin : "
                    + mContactPlugin + " | mimeType : " + mimeType);
            return kind;
        }

    }

    public Intent getExtentionIntent(int im, int ft) {
        Intent intent = null;
        mIMValue = im;
        mFTValue = ft;
        if (mContactPlugin != null) {
            Action[] actions = mContactPlugin.getContactActions();
            if (mIMValue == 1) {
                intent = actions[0].intentAction;
            } else if (mFTValue == 1) {
                intent = actions[1].intentAction;
            }
        } else {
            Log.e(TAG, "[getExtentionIntent] mContactPlugin is null");
        }
        Log.i(TAG,"[getExtentionIntent] intent : "+intent+" | im : "+im+" | ft : "+ft);
        return intent;
    }
    
    public void measureExtentionIcon(ImageView mRCSIcon) {

        if (isVisible(mRCSIcon)) {
            if (!mRCSIconViewWidthAndHeightAreReady) {
                if (mContactPlugin != null) {
                    Drawable a = mContactPlugin.getAppIcon();
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
    
    public int layoutExtentionIcon(int leftBound, int topBound, int bottomBound, int rightBound,
            int mGapBetweenImageAndText,ImageView mExtentionIcon) {
        if (this.isVisible(mExtentionIcon)) {
            int photoTop1 = topBound + (bottomBound - topBound - mRCSIconViewHeight) / 2;
            mExtentionIcon.layout(rightBound - (mRCSIconViewWidth), photoTop1, rightBound, photoTop1
                    + mRCSIconViewHeight);
            rightBound -= (mRCSIconViewWidth + mGapBetweenImageAndText);
        }
       return rightBound;
    }

    public void bindExtentionIcon(ContactListItemView view, int partition, Cursor cursor) {
        view.removeExtentionIconView();
        Log.i(TAG,"[bindExtentionIcon] view : "+view);
        Drawable a;
        if(mContactPlugin != null && mContactPlugin.isEnabled()){
            int i = cursor.getColumnIndex(Contacts._ID);
            long contactId = cursor.getLong(i);
            Log.i(TAG,"[bindExtentionIcon] contactId : "+contactId);
            a = mContactPlugin.getContactPresence(contactId);
            mContactPlugin.addOnPresenceChangedListener(new OnPresenceChangedListener(){

                public void onPresenceChanged(long contactId, int presence) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(ContactExtentionManager.RCS_CONTACT_PRESENCE_CHANGED);
                    ContactsApplication.getInstance().sendBroadcast(intent);
                    Log.i(TAG,"[bindExtentionIcon] contactId : "+contactId+" | presence : "+presence);
                }}, contactId);
            if (a != null){
                view.setExtentionIcon(true);
            } else {
                view.setExtentionIcon(false);
            }
        } else {
            view.setExtentionIcon(false);
        }
    }
    
    public Drawable setExtentionIcon(ContactExtention mExtentionIcon,ContactEntry entry) {
        if(mExtentionIcon != null){
            Drawable icon = null;
            if(mContactPlugin != null && mContactPlugin.isEnabled()){
                long contactId = entry.contact_id;
                icon = mContactPlugin.getContactPresence(contactId);
                Log.i(TAG,"[setExtentionIcon] contactId : "+contactId);
                return icon;
            } else {
                Log.e(TAG,"setExtentionIcon mContactPlugin : "+mContactPlugin);
                return null;
            }
        } else {
            Log.e(TAG,"setExtentionIcon mExtentionIcon is null : ");
            return null;
        }
        
    }
    
    public String getExtentionMimeType() {
        String mimeType = null;
        if (mContactPlugin != null) {
            mimeType = mContactPlugin.getMimeType();
            Log.i(TAG, "getExtentionMimeType mimeType : " + mimeType);
            return mimeType;
        } else {
            Log.e(TAG, "getExtentionMimeType mContactPlugin is null ");
            return mimeType;
        }

    }
    
    public void setExtentionButton(View resultView,CharSequence number, String mimeType,Activity activity) {

        mActivity = activity;
        mNumber = number.toString();
        String RCSMimType = null;
        Drawable a = null;
        Drawable b = null;
        Action [] mRCSAction = null;
        if( mContactPlugin != null && mContactPlugin.isEnabled()){
            RCSMimType = mContactPlugin.getMimeType();
            mRCSAction = mContactPlugin.getContactActions();
            if(mRCSAction[0] != null && mRCSAction[1] != null){
                a = mRCSAction[0].icon;
                b = mRCSAction[1].icon;
            } else {
                Log.e(TAG,"setExtentionButton action is null");
            }
            
            if(mimeType != null && RCSMimType != null && !mimeType.equals(RCSMimType)){
                return;
            }
            final View vewFirstDivider = resultView.findViewById(R.id.vertical_divider_vtcall);
            final ImageView btnFirstAction = (ImageView) resultView
                    .findViewById(R.id.vtcall_action_button);
            
            final View vewSecondDivider = resultView.findViewById(R.id.vertical_divider);
            final ImageView btnSecondButton = (ImageView) resultView.findViewById(
                    R.id.secondary_action_button);
            if (vewFirstDivider != null&& vewSecondDivider != null) {
                Log.i(TAG,"[setExtentionButton] 1");
                vewFirstDivider.setVisibility(View.GONE);
                vewSecondDivider.setVisibility(View.GONE);
            }           
            if (btnFirstAction != null && btnSecondButton != null) { 
                Log.i(TAG,"[setExtentionButton] 2");
                btnFirstAction.setVisibility(View.GONE);
                btnSecondButton.setVisibility(View.GONE);
            }
            
            if (btnFirstAction != null && mIMValue == 1 && mFTValue ==1) {
                Log.i(TAG,"[setExtentionButton] 3");
//                vewSecondDivider.setVisibility(View.VISIBLE);
                vewFirstDivider.setVisibility(View.VISIBLE);
            } 
            
            if (btnFirstAction != null && btnSecondButton != null && mIMValue == 1 && mFTValue ==1) {
                Log.i(TAG,"[setExtentionButton] 4");
                btnFirstAction.setImageDrawable(a);
                btnFirstAction.setVisibility(View.VISIBLE);
                btnFirstAction.setClickable(false);
                btnSecondButton.setTag(mRCSAction[1].intentAction);
//                btnSecondButton.setImageResource(R.drawable.btn_start_share_nor);
                btnSecondButton.setImageDrawable(b);
                btnSecondButton.setVisibility(View.VISIBLE);
                btnSecondButton.setOnClickListener(mRCSSecondonClickListener);
            } 
            
            if (btnFirstAction != null && btnSecondButton != null && mIMValue == 1 && mFTValue !=1) {
                Log.i(TAG,"[setExtentionButton] 5");
                btnFirstAction.setImageDrawable(a);
                btnFirstAction.setVisibility(View.VISIBLE);
                btnFirstAction.setClickable(false);
//                btnSecondButton.setTag(mRCSAction[1].intentAction);
//                btnSecondButton.setImageResource(R.drawable.btn_start_share_nor);
//                btnSecondButton.setImageDrawable(b);
//                btnSecondButton.setVisibility(View.VISIBLE);
//                btnSecondButton.setOnClickListener(mRCSSecondonClickListener);
            } 
            
            if (btnFirstAction != null && btnSecondButton != null && mIMValue != 1 && mFTValue ==1) {
                Log.i(TAG,"[setExtentionButton] 5");
                btnFirstAction.setImageDrawable(b);
                btnFirstAction.setVisibility(View.VISIBLE);
                btnFirstAction.setClickable(false);
//                btnSecondButton.setTag(mRCSAction[1].intentAction);
//                btnSecondButton.setImageResource(R.drawable.btn_start_share_nor);
//                btnSecondButton.setImageDrawable(b);
//                btnSecondButton.setVisibility(View.VISIBLE);
//                btnSecondButton.setOnClickListener(mRCSSecondonClickListener);
            }
        }
        Log.i(TAG,"[setExtentionButton] mimeType : "+mimeType+" | RCSMimType : "+RCSMimType+" | mRCSAction : "+mRCSAction);
        
        
    }
    
    private final View.OnClickListener mRCSSecondonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = (Intent)view.getTag();
           
            if(intent != null){
                Log.i(TAG, "[mRCSSecondonClickListener] name : "+mName+" | number : "+mNumber);
                intent.putExtra(RCS_DISPLAY_NAME, mName);
                intent.putExtra(RCS_PHONE_NUMBER, mNumber);
                mActivity.startActivity(intent);
            } else {
                Log.e(TAG,"[mRCSSecondonClickListener] intent is null");
            }
           
            
        }
    };
    
    public String setExtentionSubTitle(String data, String mimeType,HashMap<String, String> mPhoneAndSubtitle) {
        if (mContactPlugin != null && mimeType != null && mimeType.equals(mContactPlugin.getMimeType())) {
            String subTitle = null;
            if (mPhoneAndSubtitle != null) {
                subTitle = mPhoneAndSubtitle.get(data);
            }
            Log.i(TAG, "[setExtentionSubTitle] subTitle : " + subTitle + "| data : " + data);
            return subTitle;
        } else {
            Log.e(TAG, "setExtentionSubTitle return null");
            return null;
        }

    }
    
    public String getExtentionTitles(String mimeType,String kind) {
        if (mContactPlugin != null && mimeType != null
                && mimeType.equals(mContactPlugin.getMimeType())) {
            String title = mContactPlugin.getAppTitle();
//            String title = "abcdefg";
            Log.i(TAG, "[getExtentionTitles] title : " + title);
            return title;
        } else {
            Log.e(TAG,"getExtentionTitles return null");
            return kind;
        }
    }

    public void setDetailKindView(View view) {
        // TODO Auto-generated method stub
        mDetailView = view;
        Log.i(TAG, "[setDetailKindView] mDetailView : " + mDetailView);
    }

    public void setScondBuotton(String mimetype, String data, String displayName,Activity activity) {
        // TODO Auto-generated method stub
        if (mContactPlugin != null && mContactPlugin.isEnabled()) {
            if (mimetype != null && mimetype.equals(mContactPlugin.getMimeType())
                    && mDetailView != null) {
                View vtcallActionViewContainer = mDetailView
                        .findViewById(R.id.vtcall_action_view_container);
                View vewVtCallDivider = mDetailView.findViewById(R.id.vertical_divider_vtcall);
                ImageView btnVtCallAction = (ImageView) mDetailView
                        .findViewById(R.id.vtcall_action_button);
                View secondaryActionViewContainer = mDetailView
                        .findViewById(R.id.secondary_action_view_container);
                ImageView secondaryActionButton = (ImageView) mDetailView
                        .findViewById(R.id.secondary_action_button);
                View secondaryActionDivider = mDetailView.findViewById(R.id.vertical_divider);
                Action [] mRCSACtions = mContactPlugin.getContactActions();
                Drawable a = null;
                Drawable b = null;
                Intent intent = null;
                mName = displayName;
                mNumber =  data;
                mActivity = activity;
                if (vewVtCallDivider != null && secondaryActionDivider != null
                        && btnVtCallAction != null && secondaryActionButton != null
                        && secondaryActionViewContainer != null
                        && vtcallActionViewContainer != null) {
                    if (mRCSACtions != null){
                        a = mRCSACtions[0].icon;
                        b = mRCSACtions[1].icon;
                        intent = mRCSACtions[1].intentAction;
                    } else {
                        Log.e(TAG,"[setScondBuotton] is null");
                    }
                    
                    if (mIMValue == 1 && mFTValue == 1) {
                        Log.i(TAG,"setScondBuotton 1");
                        vewVtCallDivider.setVisibility(View.GONE);
                        secondaryActionDivider.setVisibility(View.VISIBLE);
                        btnVtCallAction.setVisibility(View.VISIBLE);
                        secondaryActionButton.setVisibility(View.VISIBLE);
                        secondaryActionViewContainer.setVisibility(View.VISIBLE);

                        btnVtCallAction.setImageDrawable(a);
                        secondaryActionButton.setImageDrawable(b);
                        vtcallActionViewContainer.setClickable(false);
                        secondaryActionButton.setTag(intent);
                        secondaryActionButton.setOnClickListener(msetScondBuottononClickListner);
                    } 

                    if (mIMValue == 1 && mFTValue != 1) {
                        Log.i(TAG,"setScondBuotton 2");
                        vewVtCallDivider.setVisibility(View.GONE);
                        secondaryActionDivider.setVisibility(View.GONE);
                        btnVtCallAction.setVisibility(View.GONE);
                        secondaryActionButton.setVisibility(View.VISIBLE);
                        secondaryActionViewContainer.setVisibility(View.VISIBLE);

                        secondaryActionButton.setImageDrawable(a);
                        secondaryActionViewContainer.setClickable(false);
//                        secondaryActionButton.setTag(intent);
//                        secondaryActionButton.setOnClickListener(msetScondBuottononClickListner);
                    } 
                    
                    if (mIMValue != 1 && mFTValue == 1) {
                        Log.i(TAG,"setScondBuotton 3");
                        vewVtCallDivider.setVisibility(View.GONE);
                        secondaryActionDivider.setVisibility(View.GONE);
                        btnVtCallAction.setVisibility(View.GONE);
                        secondaryActionButton.setVisibility(View.VISIBLE);
                        secondaryActionViewContainer.setVisibility(View.VISIBLE);

                        secondaryActionButton.setImageDrawable(b);
                        secondaryActionViewContainer.setClickable(false);
//                        secondaryActionButton.setTag(intent);
//                        secondaryActionButton.setOnClickListener(msetScondBuottononClickListner);
                    }
                } else {
                    Log.e(TAG, "[setScondBuotton] vewVtCallDivider : " + vewVtCallDivider
                            + " | secondaryActionDivider : " + secondaryActionDivider
                            + " | btnVtCallAction : " + btnVtCallAction
                            + " | secondaryActionButton : " + secondaryActionButton
                            + " | secondaryActionViewContainer : " + secondaryActionViewContainer
                            + " | vtcallActionViewContainer : " + vtcallActionViewContainer);
                }

//                vewVtCallDivider.setVisibility(View.GONE);
//                secondaryActionDivider.setVisibility(View.VISIBLE);
//                btnVtCallAction.setVisibility(View.VISIBLE);
//                secondaryActionButton.setVisibility(View.VISIBLE);
//                secondaryActionViewContainer.setVisibility(View.VISIBLE);
//
//                btnVtCallAction.setImageDrawable(a);
//                secondaryActionButton.setImageDrawable(b);
//                vtcallActionViewContainer.setClickable(false);
//                secondaryActionButton.setTag(intent);
//                secondaryActionButton.setOnClickListener(msetScondBuottononClickListner);
            } else {
                Log.e(TAG, "[setScondBuotton] mDetailView or mimetype is not equals mimetype : "
                        + mimetype + " | mDetailView : " + mDetailView);
            }
        } else {
            Log.e(TAG,
                    "[setScondBuotton] mContactPlugin is null or not enabled | mContactPlugin : "
                            + mContactPlugin);
        }
    }
    
    private OnClickListener msetScondBuottononClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = (Intent) view.getTag();

            if (intent != null) {
                Log
                        .i(TAG, "[msetScondBuottononClickListner] name : " + mName + " | number : "
                                + mNumber);
                intent.putExtra(RCS_DISPLAY_NAME, mName);
                intent.putExtra(RCS_PHONE_NUMBER, mNumber);
                mActivity.startActivity(intent);
            } else {
                Log.e(TAG, "[msetScondBuottononClickListner] intent is null");
            }

        }
    };
    @Override
    public boolean isEnabled() {
        if (mContactPlugin != null) {
            boolean result = mContactPlugin.isEnabled();
            Log.i(TAG, "[isEnabled] result : " + result);
            return result;
        } else {
            Log.e(TAG, "isEnabled]mContactPlugin is null");
            return false;
        }
    }
    
    public void onContactDetialOpen(Uri contactLookupUri) {
        if (mContactPlugin != null) {
            mContactPlugin.onContactDetailOpen(contactLookupUri);
        } else {
            Log.e(TAG,"[onContactDetialOpen] mContactPlutin is null");
        }
        Log.i(TAG,"[onContactDetialOpen] contactLookupUri : "+contactLookupUri);
    }
}
