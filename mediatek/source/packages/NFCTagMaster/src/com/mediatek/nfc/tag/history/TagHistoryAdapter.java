
package com.mediatek.nfc.tag.history;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.record.ParsedNdefRecord;
import com.mediatek.nfc.tag.utils.Utils;

/**
 * Adapter to display the tag history.
 */
public class TagHistoryAdapter extends CursorAdapter {
    private static final String TAG = Utils.TAG + "/TagHistoryAdapter";

    private final LayoutInflater mInflater;

    private final Activity mActivity;

    public TagHistoryAdapter(Activity activity) {
        super(activity, null, false);
        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int tagType = cursor.getInt(cursor.getColumnIndex(TagContract.COLUMN_TYPE));
        ParsedNdefRecord record = ParsedNdefRecord.getRecordInstance(mActivity, tagType);
        viewHolder.mIconView.setImageResource(record.getTagHistoryItemIconResId());
        viewHolder.mSummaryView.setText(record.getTagHistoryItemSummaryResId());
        viewHolder.mTagType = tagType;

        String tagContent = cursor.getString(cursor
                .getColumnIndex(TagContract.COLUMN_HISTORY_TITLE));
        viewHolder.mTitleView.setText(tagContent);

        long dateInMilli = cursor.getLong(cursor.getColumnIndex(TagContract.COLUMN_DATE));
        viewHolder.mDateView.setText(Utils.translateTime(dateInMilli));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.history_list_item, null);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mIconView = (ImageView) view.findViewById(R.id.history_tag_icon);
        viewHolder.mTitleView = (TextView) view.findViewById(R.id.history_tag_title);
        viewHolder.mSummaryView = (TextView) view.findViewById(R.id.history_tag_summary);
        viewHolder.mDateView = (TextView) view.findViewById(R.id.history_tag_date);

        view.setTag(viewHolder);
        return view;
    }

    @Override
    protected void onContentChanged() {
        Utils.loge(TAG, "History contend changed, need to update page now");
        super.onContentChanged();
    }

    static final class ViewHolder {
        ImageView mIconView;

        TextView mTitleView;

        TextView mSummaryView;

        TextView mDateView;

        int mTagType;
    }
}
