package com.mediatek.contacts.custom;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMAas;

import android.R.bool;
import android.R.integer;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.SIMInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.model.AccountType;

import com.android.internal.telephony.AlphaTag;

public class CustomAasAdapter extends BaseAdapter {
    private final static String TAG = "CustomAasAdapter";
    public final static int MODE_NORMAL = 0;
    public final static int MODE_EDIT = 1;
    private int mMode = MODE_NORMAL;

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private int mSlotId = -1;
    private ToastHelper mToastHelper = null;

    private ArrayList<TagItemInfo> mTagItemInfos = new ArrayList<TagItemInfo>();

    public CustomAasAdapter(Context context, int slot) {
        mContext = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSlotId = slot;
        mToastHelper = new ToastHelper(context);
    }

    public void updateAlphaTags() {
        mTagItemInfos.clear();

        List<AlphaTag> list = (USIMAas.getUSIMAASListWithoutNullTag(mSlotId));
        for (AlphaTag tag : list) {
            TagItemInfo tagItemInfo = new TagItemInfo(tag);
            mTagItemInfos.add(tagItemInfo);
            Log.d(TAG, "updateUSIMAAS: " + tag.getPbrIndex());
            Log.d(TAG, "updateUSIMAAS: " + tag.getRecordIndex());
            Log.d(TAG, "updateUSIMAAS: " + tag.getAlphaTag());
        }
        notifyDataSetChanged();
    }

    public void setMode(int mode) {
        Log.d(TAG, "setMode " + mode);
        if (mMode != mode) {
            mMode = mode;
            if (isMode(MODE_NORMAL)) {
                for (TagItemInfo tagInfo : mTagItemInfos) {
                    tagInfo.mChecked = false;
                }
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMode(int mode) {
        return mMode == mode;
    }

    @Override
    public int getCount() {
        return mTagItemInfos.size();
    }

    @Override
    public TagItemInfo getItem(int position) {
        return mTagItemInfos.get(position);
    }

    public void setChecked(int position, boolean checked) {
        TagItemInfo tagInfo = getItem(position);
        tagInfo.mChecked = checked;
        notifyDataSetChanged();
    }

    public void updateChecked(int position) {
        TagItemInfo tagInfo = getItem(position);
        tagInfo.mChecked = !tagInfo.mChecked;
        notifyDataSetChanged();
    }

    public void setAllChecked(boolean checked) {
        Log.d(TAG, "setAllChecked: " + checked);
        for (TagItemInfo tagInfo : mTagItemInfos) {
            tagInfo.mChecked = checked;
        }
        notifyDataSetChanged();
    }

    public void deleteCheckedAasTag() {
        for (TagItemInfo tagInfo : mTagItemInfos) {
            if (tagInfo.mChecked) {
                boolean success = USIMAas.removeUSIMAASById(mSlotId, tagInfo.mAlphaTag
                        .getRecordIndex(), tagInfo.mAlphaTag.getPbrIndex());
                final int aasIndex=tagInfo.mAlphaTag.getRecordIndex();
                if (!success) {
                    String msg = mContext.getResources().getString(R.string.aas_delete_fail,
                            tagInfo.mAlphaTag.getAlphaTag());
                    mToastHelper.showToast(msg);
                    Log.d(TAG, "delete failed:" + tagInfo.mAlphaTag.getAlphaTag());
                } else {
                    new Handler().post(new Runnable(){
                        @Override
                        public void run() {
                            updateAasInfoToDB(aasIndex);
                        }
                        
                    });
                    
                }
            }
        }
        updateAlphaTags();
    }

    public int getCheckedItemCount() {
        int count = 0;
        if (isMode(MODE_EDIT)) {
            for (TagItemInfo tagInfo : mTagItemInfos) {
                if (tagInfo.mChecked) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Boolean isExist(String text) {
        for (int i = 0; i < mTagItemInfos.size(); i++) {
            if (mTagItemInfos.get(i).mAlphaTag.getAlphaTag().equals(text)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        Log.d(TAG, "isFull():" + getCount() + ", maxCount=" + USIMAas.getUSIMAASMaxCount(mSlotId));
        return getCount() >= USIMAas.getUSIMAASMaxCount(mSlotId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_aas_item, null);
            TextView tagView = (TextView) convertView
                    .findViewById(R.id.aas_item_tag);
            ImageView imageView = (ImageView) convertView
                    .findViewById(R.id.aas_edit);
            CheckBox checkBox = (CheckBox) convertView
                    .findViewById(R.id.aas_item_check);
            viewHolder = new ViewHolder(tagView, imageView, checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TagItemInfo tag = getItem(position);
        viewHolder.mTagView.setText(tag.mAlphaTag.getAlphaTag());

        if (isMode(MODE_NORMAL)) {
            viewHolder.mEditView.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setVisibility(View.GONE);
            // viewHolder.mEditView.setOnClickListener()
        } else {
            viewHolder.mEditView.setVisibility(View.GONE);
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setChecked(tag.mChecked);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView mTagView;
        ImageView mEditView;
        CheckBox mCheckBox;

        public ViewHolder(TextView textView, ImageView imageView,
                CheckBox checkBox) {
            mTagView = textView;
            mEditView = imageView;
            mCheckBox = checkBox;
        }
    }

    public static class TagItemInfo {
        AlphaTag mAlphaTag = null;
        boolean mChecked = false;

        public TagItemInfo(AlphaTag tag) {
            mAlphaTag = tag;
        }
    }
    
    private void updateAasInfoToDB(int aasIndex){
        final ContentResolver resolver = mContext.getContentResolver();
        ContentValues additionalvalues = new ContentValues();

        String whereadditional = Data.RAW_CONTACT_ID
                + " IN (select _id from raw_contacts where "
                + RawContacts.ACCOUNT_NAME + "=?"
                + " AND " + RawContacts.ACCOUNT_TYPE + "=?)"
                + " AND " + Data.IS_ADDITIONAL_NUMBER + " =1"
                + " AND " + Data.DATA2 + "=?";

        //Here, we assume the account type is always USIM account.
        //TODO: check the account name
        String accountName = AccountType.ACCOUNT_NAME_USIM;
        if (SimCardUtils.SimSlot.SLOT_ID2 == mSlotId) {
            accountName = AccountType.ACCOUNT_NAME_USIM2;
        }
        
        String[] selectionArgs = { accountName, AccountType.ACCOUNT_TYPE_USIM,
                String.valueOf(aasIndex) };
        
        Log.i(TAG, "whereadditional is " + whereadditional);
        additionalvalues.put(Phone.TYPE, -1);
        additionalvalues.put(Phone.AAS_INDEX, -1);
        //TODO: change it to async task.
        int upadditional = resolver.update(Data.CONTENT_URI, additionalvalues,
                whereadditional, selectionArgs);
        Log.i(TAG,"upadditional=="+upadditional);
    }
}
