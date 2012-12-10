
package com.mediatek.nfc.tag.history;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.provider.TagContract;

public class HistoryHost extends TabActivity {
    public static final String TAG = "NfcTag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost host = getTabHost();
        Resources res = getResources();

        Intent historyReadIntent = new Intent(this, TagHistoryList.class);
        historyReadIntent.putExtra(TagContract.COLUMN_IS_CREATED_BY_ME, 0);
        Intent historyWriteIntent = new Intent(this, TagHistoryList.class);
        historyWriteIntent.putExtra(TagContract.COLUMN_IS_CREATED_BY_ME, 1);

        host.addTab(host.newTabSpec("readTag").setIndicator(
                getString(R.string.history_tab_label_read),
                res.getDrawable(R.drawable.ic_tab_history_read)).setContent(historyReadIntent));
        host.addTab(host.newTabSpec("writeTag").setIndicator(
                getString(R.string.history_tab_label_write),
                res.getDrawable(R.drawable.ic_tab_history_write)).setContent(historyWriteIntent));
    }
}
