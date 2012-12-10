package com.mediatek.nfc.tag;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;

import com.mediatek.nfc.tag.record.ParsedNdefRecord;

/**
 * This class stand for a available preference when creating a new tag
 */
public class TagTypePreference extends Preference {

    int mIconId;

    String mTitle;

    ImageView mIconView;

    int mTagType = -1;

    ParsedNdefRecord mRecord = null;

    public TagTypePreference(Context context, int iconId, String title) {
        super(context);

        mIconId = iconId;
        mTitle = title;

        setLayoutResource(R.layout.preference_tag_type);
    }

    @Override
    protected void onBindView(View view) {
        setTitle(mTitle);

        mIconView = (ImageView) view.findViewById(R.id.tag_icon);
        mIconView.setImageResource(mIconId);

        super.onBindView(view);
    }

    public int getTagType() {
        return mTagType;
    }

    public void setTagType(int tagType) {
        mTagType = tagType;
    }

    public ParsedNdefRecord getRecord() {
        return mRecord;
    }

    public void setRecord(ParsedNdefRecord record) {
        mRecord = record;
    }

    public String getTitle() {
        return mTitle;
    }
}
