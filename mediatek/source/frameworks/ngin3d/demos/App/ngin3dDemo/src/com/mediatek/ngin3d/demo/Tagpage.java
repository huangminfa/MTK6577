
package com.mediatek.ngin3d.demo;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Tagpage extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.main);
        final TabHost tabHost = getTabHost();

        // View view = this.getTabHost().getCurrentView();
        // ((TextView)view.findViewById(android.R.id.title)).setTextSize(12);
        // ((ImageView)view.findViewById(android.R.id.icon)).setPadding(0, -5,
        // 0, 0);
        // ((TextView)view.findViewById(android.R.id.title)).setGravity(0x11);

        tabHost.addTab(tabHost.newTabSpec("Tab1")
                .setIndicator("Basic(15)", null)
                .setContent(new Intent(this, DemolistBasic.class)));
        tabHost.addTab(tabHost.newTabSpec("Tab2")
                .setIndicator("Animation(18)", null)
                .setContent(new Intent(this, DemolistAnimation.class)));

        tabHost.addTab(tabHost.newTabSpec("Tab3")
                .setIndicator("Glo3D(11)", null)
                .setContent(new Intent(this, DemolistGlo3D.class)));

    }
}
