package com.mediatek.nfc.tag.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.record.VCardRecord.DetailItem;

/**
 * Class stand for a line of contact detail info
 */
public class ContactItemView {
    public Context mContext;

    public DetailItem mItem;

    public String mType;

    public boolean mWithCheckBox;

    public TextView mTitleView = null;

    public TextView mSummaryView = null;

    public CheckBox mSelectedStatusView = null;

    public ContactItemView(Context context, DetailItem item, String typeStr, boolean withCheckBox) {
        mContext = context;
        mItem = item;
        mType = typeStr;
        mWithCheckBox = withCheckBox;
    }

    public View getItemView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.contact_item_view, null);
        if (itemView != null && mItem != null) {
            mTitleView = (TextView) itemView.findViewById(R.id.item_title);
            mSummaryView = (TextView) itemView.findViewById(R.id.item_summary);
            mSelectedStatusView = (CheckBox) itemView.findViewById(R.id.item_selected_status);

            mTitleView.setText(mItem.mValue);
            if (TextUtils.isEmpty(mType)) {
                mSummaryView.setVisibility(View.GONE);
            } else {
                mSummaryView.setVisibility(View.VISIBLE);
                mSummaryView.setText(mType);
            }

            if (mWithCheckBox) {
                mSelectedStatusView.setVisibility(View.VISIBLE);
                mSelectedStatusView.setChecked(mItem.mSelected);
                mSelectedStatusView
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                    boolean isChecked) {
                                mItem.mSelected = isChecked;
                            }
                        });
            } else {
                mSelectedStatusView.setVisibility(View.GONE);
            }
        }
        return itemView;
    }

    public boolean isSelected() {
        return mItem.mSelected;
    }

    /**
     * Get the category title view, like Phone, Email, Im et.
     * 
     * @param context
     * @param categoryTitle
     * @return
     */
    public static View getCategoryHeaderView(Context context, String categoryTitle) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View categoryView = inflater.inflate(R.layout.contact_category_header_view, null);
        if (categoryView != null) {
            TextView titleView = (TextView) categoryView.findViewById(R.id.category_title);
            if (titleView != null) {
                titleView.setText(categoryTitle);
            }
        }
        return categoryView;
    }
}
