package com.mediatek.media3d;

import android.app.Activity;
import android.os.Bundle;
import android.widget.VideoView;

public class VideoPlayer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        
        VideoView videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setVideoURI(getIntent().getData());
        videoView.start();
    }
}