package com.mediatek.ngin3d.demo;

import android.net.Uri;
import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.demo.R;
import com.mediatek.ngin3d.android.StageActivity;

public class VideoTextureDemo extends StageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Image video1 = Image.createFromVideo(this, Uri.parse("android.resource://com.mediatek.ngin3d.tests/" + R.raw.gg_taeyeon), 240, 160);

        Image video2 = Image.createFromVideo(this, Uri.parse("android.resource://com.mediatek.ngin3d.tests/" + R.raw.gg_hyoyeon), 240, 160);

        Image video3 = Image.createFromVideo(this, Uri.parse("android.resource://com.mediatek.ngin3d.tests/" + R.raw.gg_jessica), 240, 160);

        Image video4 = Image.createFromVideo(this, Uri.parse("android.resource://com.mediatek.ngin3d.tests/" + R.raw.weather_video), 240, 160);

        mStage.add(video1);
        video1.setPosition(new Point(0.2f, 0.2f, true));
        new PropertyAnimation(video1, "rotation", new Rotation(0, 0, 0), new Rotation(0, 0, 360)).setDuration(7000).setLoop(true).start();

        mStage.add(video2);
        video2.setPosition(new Point(0.4f, 0.4f, true));
        new PropertyAnimation(video2, "rotation", new Rotation(0, 0, 30), new Rotation(0, 0, 390)).setDuration(7000).setLoop(true).start();

        mStage.add(video3);
        video3.setPosition(new Point(0.6f, 0.6f, true));
        new PropertyAnimation(video3, "rotation", new Rotation(0, 0, 60), new Rotation(0, 0, 420)).setDuration(7000).setLoop(true).start();

        mStage.add(video4);
        video4.setPosition(new Point(0.8f, 0.8f, true));
        new PropertyAnimation(video4, "rotation", new Rotation(0, 0, 90), new Rotation(0, 0, 450)).setDuration(7000).setLoop(true).start();

    }
}
