package com.mediatek.nfc.tag;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.mediatek.nfc.tag.history.TagHistoryList;
import com.mediatek.nfc.tag.write.TagTypeList;

public class TagWriteHost extends TabActivity {
    public static final String TAG = "NfcTag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost host = getTabHost();
        Resources res = getResources();

        Intent newTagIntent = new Intent(this, TagTypeList.class);
        Intent historyListIntent = new Intent(this, TagHistoryList.class);

        host.addTab(host.newTabSpec("newTags")
        // .setIndicator(getString(R.string.write_tag_label_new),
                // res.getDrawable(R.drawable.ic_tab_new_tags))
                .setContent(newTagIntent));
        host.addTab(host.newTabSpec("historyTags")
        // .setIndicator(getString(R.string.write_tag_label_history),
                // res.getDrawable(R.drawable.ic_tab_history))
                .setContent(historyListIntent));
    }
}
