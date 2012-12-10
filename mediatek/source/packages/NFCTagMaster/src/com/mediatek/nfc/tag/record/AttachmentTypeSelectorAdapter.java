
package com.mediatek.nfc.tag.record;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter to store icons and strings for attachment type list.
 */
public class AttachmentTypeSelectorAdapter extends BaseAdapter {
    private static final String TAG = Utils.TAG + "/AttachmentTypeSelectorAdapter";

    // supported attachment type
    public static final int ADD_IMAGE = 0;

    public static final int ADD_AUDIO = 1;

    public static final int ADD_RINGTONE = 2;

    private static Context sContext = null;

    private static LayoutInflater sInflater = null;

    private static List<AttachItem> sDataArray = new ArrayList<AttachItem>();

    public AttachmentTypeSelectorAdapter(Context context) {
        sContext = context;
        sInflater = LayoutInflater.from(sContext);
        getData();
    }

    public int buttonToCommand(int whichButton) {
        if (whichButton >= 0 && whichButton < sDataArray.size()) {
            return sDataArray.get(whichButton).getCommand();
        } else {
            Utils.loge(TAG, "Selected attachment type[" + whichButton + "] is out of range.");
            return -1;
        }
    }

    private static void getData() {
        sDataArray = new ArrayList<AttachItem>();
        sDataArray.add(new AttachItem(sContext.getString(R.string.mms_attach_image),
                R.drawable.ic_attach_picture_holo_light, ADD_IMAGE));
        sDataArray.add(new AttachItem(sContext.getString(R.string.mms_attach_audio),
                R.drawable.ic_attach_audio_holo_light, ADD_AUDIO));
        sDataArray.add(new AttachItem(sContext.getString(R.string.mms_attach_ringtone),
                R.drawable.ic_attach_audio_holo_light, ADD_RINGTONE));
    }

    @Override
    public int getCount() {
        return sDataArray.size();
    }

    @Override
    public Object getItem(int position) {
        return sDataArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title;
        ImageView imageView;

        View view;
        if (convertView == null) {
            view = sInflater.inflate(R.layout.icon_list_item, null);
        } else {
            view = convertView;
        }
        title = (TextView) view.findViewById(R.id.title);
        imageView = (ImageView) view.findViewById(R.id.icon);

        title.setText(sDataArray.get(position).getTitle());
        imageView.setImageResource(sDataArray.get(position).getIconRes());
        return view;
    }
}

/**
 * Class stand for a kind of attachment type
 */
class AttachItem {
    private String mTitle;

    private final int mIconRes;

    private final int mCommand;

    public AttachItem(String title, int iconRes, int command) {
        this.mTitle = title;
        this.mIconRes = iconRes;
        this.mCommand = command;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public int getCommand() {
        return mCommand;
    }
}
