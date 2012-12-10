
package com.mediatek.widgetdemos.musicplayer3d;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.mediatek.ngin3d.android.StageActivity;

public class MusicActivity extends StageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_widget_activity_layout);
        View container = this.findViewById(R.id.widget_container);
        this.getLayoutInflater().inflate(R.layout.music_player_layout, (ViewGroup) container, true);
    }

}
