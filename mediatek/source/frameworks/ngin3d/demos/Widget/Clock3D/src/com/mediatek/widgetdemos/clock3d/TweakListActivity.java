package com.mediatek.widgetdemos.clock3d;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// Lists tweak values for user selection
public class TweakListActivity extends ListActivity {
    String[] mIds;
    String[] mNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Extract the tweak values bundled in the intent
        Intent intent = getIntent();
        Bundle tweaks = intent.getBundleExtra("tweaks");
        mIds = tweaks.getStringArray("ids");
        mNames = tweaks.getStringArray("names");

        // Set ListView to show the human-readable tweak names
        setListAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, mNames));

        // When the user selects a tweak, we return a result containing the ID
        // and name of the selected tweak
        ListView listView = getListView();
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                
                Intent intent = new Intent();
                intent.putExtra("id", mIds[position]);
                intent.putExtra("name", mNames[position]);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Log.v("TweakListActivity", "onCreate");
    }
}
